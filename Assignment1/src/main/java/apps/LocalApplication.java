package apps;

import messages.Client2Manager;
import messages.Client2Manager_terminate;
import messages.Manager2Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.model.Message;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;
import messages.Worker2Manager;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


/** From the assignment description:
 * 1. Checks if a apps.Manager node is active on the EC2 cloud. If it is not, the application will start the manager node.
 * 2. Uploads the file to S3.
 * 3. Sends a message to an SQS queue, stating the location of the file on S3
 * 4. Checks an SQS queue for a message indicating the process is done and the response (the summary file) is available on S3.
 * 5. Downloads the summary file from S3, and create an html file representing the results.
 * 6. Sends a termination message to the apps.Manager if it was supplied as one of its input arguments.
 */

public class LocalApplication {

    public static void createQueueAndUpload(S3Handler s3, SQSHandler sqs, String queueName, String bucket, String key, boolean shortPolling) throws IOException {

        // start SQS queue
        String QueueURL = sqs.createSQSQueue(queueName, true);

        // create a file containing the queue URL (file name is the key)
        FileOutputStream URLfile = new FileOutputStream(key);
        URLfile.write(QueueURL.getBytes());

        // upload file to s3 - save the queue URL in a known location (known in the constants class)
        s3.uploadFileToS3(bucket, key);
    }

    public static void startManager(EC2Handler ec2, S3Handler s3, SQSHandler sqs) throws IOException {

        // TODO: understand how to run instances with a tag
        // TODO: run manager

        // start SQS queue for Clients -> apps.Manager (CM) messages
        createQueueAndUpload(s3, sqs, "ClientsManagerQueue", Constants.CLIENTS_TO_MANAGER_QUEUE_BUCKET,
                Constants.CLIENTS_TO_MANAGER_QUEUE_KEY, true);

        // start SQS queue for apps.Manager -> Clients (MC) messages ("done" messages) - type long polling
        createQueueAndUpload(s3, sqs, "ManagerClientsQueue", Constants.MANAGER_TO_CLIENTS_QUEUE_BUCKET,
                Constants.MANAGER_TO_CLIENTS_QUEUE_KEY, false);

    }

    private static String inputStreamToString(InputStream input) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(input);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while(result != -1) {
            buf.write((byte) result);
            result = bis.read();
        }
        return buf.toString("UTF-8");
    }

    /**
     * Create an html file representing the results from the summery.
     * params: appID, summery
     */
    public static void createHtml(UUID appID, int numOutput, InputStream summery) throws IOException, ParseException {

        // create the string
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n<html>\n<head>\n<title>Page Title</title>\n</head>\n<body>\n<h1>Amazon Reviews - Sarcasm Detector</h1><ul>");

        // go through the summery output file line by line
        BufferedReader reader = new BufferedReader(new InputStreamReader(summery));
        while(reader.ready()) {
            String line = reader.readLine();

            Worker2Manager msg = new Worker2Manager(line);
            
        }





        String color="@@", review="@@", entityList="@@", true_false="@@", link="@@";

        String li =
                "<li>\n" +
                        "    <span style=\"color: "+ color +"\">** "+ review +" **</span>\n" +
                        "    "+ entityList +"\n" +
                        "    - This is a "+ true_false +" sarcastic review.\n" +
                        "    <a href=\""+ link +"\">Link</a>\n" +
                        "  </li>";


        html.append(li);



        html.append("</ul>\n</body>\n</html>");

        // create the file
        String fileName = "Output_for_app_" + appID.toString() + "_no._" + numOutput;
        File htmlFile = new File(fileName);
        Files.write(Paths.get(fileName), html.toString().getBytes());
    }

    public static void main(String[] args) throws Exception {

        // initial configurations
        EC2Handler ec2 = new EC2Handler();
        S3Handler s3 = new S3Handler(ec2);
        SQSHandler sqs = new SQSHandler(ec2.getCredentials());

        // extract input file name, output file names and optional termination message from args
        // example args: inputFileName1… inputFileNameN outputFileName1… outputFileNameN n terminate(optional)
        boolean terminate = (args.length % 2 == 0);
        int num_files = (args.length-1)/2;
        int n;

        if (terminate)
            n = Integer.parseInt(args[args.length-2]);
        else
            n =Integer.parseInt(args[args.length-1]);

        // TODO - Check if a apps.Manager node is active on the EC2 cloud. If it is not, the application will start the manager node
        if (!ec2.isTagExists(Constants.TAG_MANAGER)) {
            startManager(ec2, s3, sqs);
        }

        // Create a bucket for this local application
        UUID appID = UUID.randomUUID();
        String bucketName = s3.createBucket(appID.toString());

        // Get the (Clients -> apps.Manager), (apps.Manager -> Clients) SQS queues URLs from s3
        S3Object CM_object = s3.getS3().getObject(new GetObjectRequest(
                Constants.CLIENTS_TO_MANAGER_QUEUE_BUCKET,Constants.CLIENTS_TO_MANAGER_QUEUE_KEY));
        String CM_QueueURL = inputStreamToString(CM_object.getObjectContent());

        S3Object MC_object = s3.getS3().getObject(new GetObjectRequest(
                Constants.CLIENTS_TO_MANAGER_QUEUE_BUCKET,Constants.CLIENTS_TO_MANAGER_QUEUE_KEY));
        String MC_QueueURL = inputStreamToString(MC_object.getObjectContent());

        // Upload all the input files to S3
        String[] keyNamesIn = new String[num_files];
        String[] keyNamesOut = new String[num_files];

        for (int i=0; i<num_files; i++) {
            String fileName = args[i];

            // upload the input file
            keyNamesIn[i] = s3.uploadFileToS3(bucketName, fileName);

            // this will be the keyName of the output file
            keyNamesOut[i] = s3.getAwsFileName(fileName) + "out";
        }

        // Send a message to the (Clients -> apps.Manager) SQS queue, stating the location of the files on S3
        for (int i=0; i<num_files; i++) {
            Client2Manager messageClientToManager = new Client2Manager(bucketName, keyNamesIn[i], bucketName, keyNamesOut[i], -1, terminate, appID);
            sqs.sendMessage(CM_QueueURL, messageClientToManager.stringifyUsingJSON());
        }

        // Check on the (apps.Manager -> Clients) SQS queue for a message indicating the process is done and the response
        // (the summary file) is available on S3.
        boolean done = false;
        while (!done) {
            List<Message> doneMessages = sqs.receiveMessages(MC_QueueURL, false);
            for (Message msg: doneMessages) {
                Manager2Client msgDone = new Manager2Client(msg.getBody());
                if (msgDone.isDone() && msgDone.getDoneID().equals(appID))
                    done = true;
            }
        }

        // Download the summary file from S3
        for (int i=0; i<num_files; i++) {
            String keyNameOut = keyNamesOut[i];
            S3Object object = s3.getS3().getObject(new GetObjectRequest(bucketName, keyNameOut));


//            String summery = inputStreamToString(object.getObjectContent());

            // TODO: Create an html file representing the results.
            createHtml(appID, i, object.getObjectContent());
        }

        // Send a termination message to the Manager if it was supplied as one of its input arguments.
        if (terminate) {
            Client2Manager_terminate terminateMsg = new Client2Manager_terminate(appID);
            sqs.sendMessage(CM_QueueURL, terminateMsg.stringifyUsingJSON());
        }

        // delete all input files, output files and the bucket from S3 for this local application
        for (int i=0; i<num_files; i++) {
            s3.deleteFile(bucketName, keyNamesIn[i]);
            s3.deleteFile(bucketName, keyNamesOut[i]);
        }
        s3.deleteBucket(bucketName);

    }
}
