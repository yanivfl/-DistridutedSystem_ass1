package apps;

import com.amazonaws.services.ec2.model.Instance;
import messages.Client2Manager;
import messages.Client2Manager_init;
import messages.Client2Manager_terminate;
import messages.Manager2Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.model.Message;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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

    /**
     * starts the manager instanse and creates the queues
     * params: ec2, s3, sqs
     */
    public static void startManager(EC2Handler ec2, S3Handler s3, SQSHandler sqs) throws IOException {

        // start the manager
        ec2.launchEC2Instances(1, Constants.INSTANCE_TAG.TAG_MANAGER);

        // start the queues
        sqs.createSQSQueue(Constants.CLIENTS_TO_MANAGER_QUEUE, true);
        sqs.createSQSQueue(Constants.MANAGER_TO_CLIENTS_QUEUE, false);
        sqs.createSQSQueue(Constants.WORKERS_TO_MANAGER_QUEUE, true);
        sqs.createSQSQueue(Constants.MANAGER_TO_WORKERS_QUEUE, true);

    }

    /**
     * Create an html file representing the results from the summery.
     * params: appID, numOutput, summery
     */
    public static void createHtml(UUID appID, int numOutput, InputStream summery) throws IOException, ParseException {

        // create the string
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n<html>\n<head>\n<title>Page Title</title>\n</head>\n<body>\n<h1>Amazon Reviews - Sarcasm Detector</h1><ul>");

        // go through the summery output file line by line
        BufferedReader reader = new BufferedReader(new InputStreamReader(summery));
        while(reader.ready()) {
            String line = reader.readLine();



            // parse line using JSON
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(line);

            if (Constants.TAGS.valueOf((String) obj.get("tag")) != Constants.TAGS.WORKER_2_MANAGER)
                throw new RuntimeException("Got an unexpected message - couldn't create an HTML file");

            String review = (String) obj.get(Constants.REVIEW);
            long sentiment = (Long) obj.get(Constants.SENTIMENT);
            String entityList = (String) obj.get(Constants.ENTITIES);
            String isSarcastic;
            if ((Boolean) obj.get(Constants.IS_SARCASTIC))
                isSarcastic = "";
            else
                isSarcastic = "not";

            String li =
                    "<li>\n" +
                            "    <span style=\"color: "+ Constants.HTML_COLORS[(int)sentiment] +"\">** "+ review +" **</span>\n" +
                            "    "+ entityList +"\n" +
                            "    - This is "+ isSarcastic +" a sarcastic review.\n" +
                            "  </li>";
            html.append(li);
        }

        html.append("</ul>\n</body>\n</html>");

        // create the HTML file
        String fileName = "Output_for_app_" + appID.toString() + "_no._" + numOutput + ".html";
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
        int reviewsPerWorker;       // (n)

        if (terminate)
            reviewsPerWorker = Integer.parseInt(args[args.length-2]);
        else
            reviewsPerWorker =Integer.parseInt(args[args.length-1]);

        // Check if a Manager node is active on the EC2 cloud. If it is not, the application will start the manager node and create the queues
        if (!ec2.isTagExists(Constants.INSTANCE_TAG.TAG_MANAGER)) {
            startManager(ec2, s3, sqs);
        }

        // Create a bucket for this local application - the bucket name is unique for this local app
        UUID appID = UUID.randomUUID();
        String myBucket = s3.createBucket(appID.toString());

        // Get the (Clients -> Manager), (Manager -> Clients) SQS queues URLs
        String C2M_QueueURL = sqs.getURL(Constants.CLIENTS_TO_MANAGER_QUEUE);
        String M2C_QueueURL = sqs.getURL(Constants.MANAGER_TO_CLIENTS_QUEUE);

        // Send an initial message to the Manager
        Client2Manager_init initMessage = new Client2Manager_init(myBucket, reviewsPerWorker);
        sqs.sendMessage(C2M_QueueURL, initMessage.stringifyUsingJSON());

        // Upload all the input files to S3
        String[] keyNamesIn = new String[num_files];
        String[] keyNamesOut = new String[num_files];

        for (int i=0; i<num_files; i++) {
            String fileName = args[i];

            // upload the input file
            keyNamesIn[i] = s3.uploadFileToS3(myBucket, fileName);

            // this will be the keyName of the output file
            keyNamesOut[i] = s3.getAwsFileName(fileName) + "out";
        }

        // Send a message to the (Clients -> apps.Manager) SQS queue, stating the location of the files on S3
        for (int i=0; i<num_files; i++) {
            Client2Manager messageClientToManager = new Client2Manager(myBucket, keyNamesIn[i], keyNamesOut[i]);
            sqs.sendMessage(C2M_QueueURL, messageClientToManager.stringifyUsingJSON());
        }

        // Check on the (Manager -> Clients) SQS queue for a message indicating the process is done and the response
        // (the summary file) is available on S3.
        boolean done = false;
        while (!done) {
            List<Message> doneMessages = sqs.receiveMessages(M2C_QueueURL, false, false);
            for (Message msg: doneMessages) {
                JSONObject msgObj= Constants.validateMessageAndReturnObj(msg , Constants.TAGS.MANAGER_2_CLIENT);
                boolean isDoneJson = (boolean)msgObj.get(Constants.IS_DONE);
                String inBucketJson = (String)msgObj.get(Constants.IN_BUCKET);
                if ( isDoneJson && inBucketJson.equals(myBucket))
                    done = true;
            }
        }

        // Download the summary file from S3
        for (int i=0; i<num_files; i++) {
            String keyNameOut = keyNamesOut[i];
            S3Object object = s3.getS3().getObject(new GetObjectRequest(myBucket, keyNameOut));
            createHtml(appID, i, object.getObjectContent());
        }

        // Send a termination message to the Manager if it was supplied as one of its input arguments.
        if (terminate) {
            Client2Manager_terminate terminateMsg = new Client2Manager_terminate(appID);
            sqs.sendMessage(C2M_QueueURL, terminateMsg.stringifyUsingJSON());
        }

        // delete all input files, output files and the bucket from S3 for this local application
        for (int i=0; i<num_files; i++) {
            s3.deleteFile(myBucket, keyNamesIn[i]);
            s3.deleteFile(myBucket, keyNamesOut[i]);
        }
        s3.deleteBucket(myBucket);

    }
}
