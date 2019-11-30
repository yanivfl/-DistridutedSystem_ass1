import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import java.io.File;
import java.util.List;
import java.util.UUID;


/** From the assignment description:
 * 1. Checks if a Manager node is active on the EC2 cloud. If it is not, the application will start the manager node.
 * 2. Uploads the file to S3.
 * 3. Sends a message to an SQS queue, stating the location of the file on S3
 * 4. Checks an SQS queue for a message indicating the process is done and the response (the summary file) is available on S3.
 * 5. Downloads the summary file from S3, and create an html file representing the results.
 * 6. Sends a termination message to the Manager if it was supplied as one of its input arguments.
 */

public class LocalApplication {

    public static void main(String[] args) throws Exception {

        // initial configurations
        EC2Handler ec2 = new EC2Handler();
        S3Handler s3 = new S3Handler(ec2);

//        AmazonS3 s3 = S3Handler.connectS3(ec2.getCredentials());

        // extract input file name, output file names and optional termination message from args
        // example args: inputFileName1… inputFileNameN outputFileName1… outputFileNameN n terminate(optional)
        boolean terminate = (args.length % 2 == 0);
        int n = (args.length-1)/2;

        // TODO - Check if a Manager node is active on the EC2 cloud. If it is not, the application will start the manager node
        if (!ec2.isTagExists(Constants.TAG_MANAGER)) {

            // TODO: understand how to run instances with a tag
//            startManager(ec2);
        }

        // Create a bucket for this local application
        UUID LocalApplicationID = UUID.randomUUID();
        String bucketName = s3.createBucket(LocalApplicationID.toString());

        // Upload all the input files to S3
        String[] keyNames = new String[n];
        for (int i=0; i<n; i++) {
            String fileName = args[i];
            keyNames[i] = s3.uploadFileToS3(bucketName, fileName);
        }

        // Send a message to an SQS queue, stating the location of the files on S3
        SQSHandler sqs = new SQSHandler(ec2.getCredentials());
        String queueName = LocalApplicationID.toString() + "Queue";
        String appQueueURL = sqs.createSQSQueue(queueName);

        for (String keyName: keyNames) {
            LocationMessage locationMessage = new LocationMessage(bucketName, keyName, null, null, -1, false);
            sqs.sendMessage(appQueueURL, locationMessage.stringifyUsingJSON());
        }

        // TODO: Check an SQS queue for a message indicating the process is done and the response (the summary file) is available on S3.
//        boolean done = false;
//        while (!done) {
//
//        }

        // TODO: Download the summary file from S3, and create an html file representing the results.
        // TODO: Send a termination message to the Manager if it was supplied as one of its input arguments.


        // delete all input files from S3 and the bucket for this local application
        for (int i=0; i<n; i++) {
            s3.deleteFile(bucketName, keyNames[i]);
        }
        s3.deleteBucket(bucketName);

    }
}
