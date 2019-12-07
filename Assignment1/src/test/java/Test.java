import apps.Constants;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.sqs.model.Message;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;


public class Test {

    public static void main(String[] args) throws Exception{

        // initial configurations
        System.out.println("connect to EC2");
        EC2Handler ec2 = new EC2Handler(true);

        System.out.println("connect to S3");
        S3Handler s3 = new S3Handler(ec2);

//        testInstances(ec2);
        String fileName = "DemoFileToS3.txt";
        testS3(s3, fileName);
//        testSQS(ec2.getCredentials());


    }

    // Test EC2 instances - launch and terminate
    public static void testInstances(EC2Handler ec2) throws Exception {
        System.out.println("\n\n*** test EC2 ***");

        List<Instance> myInstances = ec2.launchEC2Instances(1, Constants.INSTANCE_TAG.MANAGER);
        if(myInstances != null){
            Instance manager = myInstances.get(0);
            String instanceIdToTerminate = manager.getInstanceId();
            ec2.terminateEC2Instance(instanceIdToTerminate);
        }
    }

    private static void displayTextInputStream(InputStream input) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("    " + line);
        }
        System.out.println();
    }

    // Test S3 - uploading a file
    public static void testS3(S3Handler s3, String fileName) throws Exception {
        System.out.println("\n\n*** test S3 ***");
        String bucketName = null;
        String keyName = null;

        try {

            System.out.println("\nCreate a bucket");
            bucketName = s3.createBucket(fileName);

            System.out.println("\nUpload file to S3");
            keyName = s3.uploadFileToS3(bucketName, fileName);

            System.out.println("\nListing buckets");
            for (Bucket bucket : s3.getS3().listBuckets()) {
                System.out.println(" - " + bucket.getName());
            }

            // This works, but is heavy so I commented it out
//        System.out.println("\nDownloading an object");
//        S3Object object = s3.getObject(new GetObjectRequest(bucketName, keyName));
//        System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());
//        displayTextInputStream(object.getObjectContent());

            System.out.println("\nListing objects");
            ObjectListing objectListing = s3.getS3().listObjects(new ListObjectsRequest()
                    .withBucketName(bucketName)
                    .withPrefix(""));
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                System.out.println(" - " + objectSummary.getKey() + "  " +
                        "(size = " + objectSummary.getSize() + ")");
            }
        }
        finally {

            if (keyName != null) {
                System.out.println("\nDelete file from S3");
                s3.deleteFile(bucketName, keyName);
            }

            if (bucketName != null) {
                System.out.println("\nDelete bucket");
                s3.deleteBucket(bucketName);
            }
        }
    }


    public static void testSQS(AWSCredentialsProvider credentials) {

        SQSHandler sqs = null;
        String myQueueURL = null;
        List<Message> messages = null;


        try {
            System.out.println("connect to SQS");
            sqs = new SQSHandler(credentials);

            System.out.println("Creating a new SQS queue called MyQueue.\n");
            myQueueURL = sqs.createSQSQueue("MyQueue", true);

            System.out.println("Listing all queues in the account.\n");
            sqs.listQueues();

            System.out.println("Sending a message to MyQueue.\n");
            sqs.sendMessage(myQueueURL, "This is my message text.");

            System.out.println("Receiving messages from MyQueue.\n");
            messages = sqs.receiveMessages(myQueueURL, true, false);
            for (Message message : messages) {
                System.out.println("  Message");
                System.out.println("    MessageId:     " + message.getMessageId());
                System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
                System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
                System.out.println("    Body:          " + message.getBody());

                for (Map.Entry<String, String> entry : message.getAttributes().entrySet()) {
                    System.out.println("  Attribute");
                    System.out.println("    Name:  " + entry.getKey());
                    System.out.println("    Value: " + entry.getValue());
                }
            }
        }

        catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        }

        catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }

        finally {

            // TODO: where should this be?
            if (sqs != null && messages != null) {
                System.out.println("Deleting a message.\n");
                sqs.deleteMessage(messages, myQueueURL);
            }

            if (sqs != null && myQueueURL != null) {
                System.out.println("Deleting the test queue.\n");
                sqs.deleteQueue(myQueueURL);
            }
        }
    }



}
