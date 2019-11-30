import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sqs.model.Message;

import java.util.List;
import java.util.Map;

public class SQSTest {

    public static void main(String[] args) {

        // initial configurations
        AWSCredentialsProvider credentials = EC2Handler.getCredentials();

        SQSHandler sqs = null;
        String myQueueURL = null;
        List<Message> messages = null;


        try {
            System.out.println("connect to SQS");
            sqs = new SQSHandler(credentials);

            System.out.println("Creating a new SQS queue called MyQueue.\n");
            myQueueURL = sqs.createSQSQueue("MyQueue1");

//            System.out.println("Sending a location message to MyQueue.\n");
//            String [] bucks = {"buck1", "buck2"};
//            String [] keys = {"key1", "key2"};
//            String locationMsg = sqs.createS3LocationMessage(myQueueURL, bucks, keys);
//            sqs.sendMessage(myQueueURL, locationMsg);
//
//            System.out.println("Receiving messages from MyQueue.\n");
//            messages = sqs.receiveMessages(myQueueURL);
//            for (Message message : messages) {
//                System.out.println("  Message");
//                System.out.println("    MessageId:     " + message.getMessageId());
//                System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
//                System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
//                System.out.println("    Body:          " + message.getBody());
//                for (Map.Entry<String, String> entry : message.getAttributes().entrySet()) {
//                    System.out.println("  Attribute");
//                    System.out.println("    Name:  " + entry.getKey());
//                    System.out.println("    Value: " + entry.getValue());
//                }
//
//                String[][] parsedMsg = sqs.parseS3LocationMessages(message.getBody());
//                String[] bucketsMsg = parsedMsg[0];
//                String[] keysMsg = parsedMsg[1];
//
//                for (int i=0; i<bucketsMsg.length; i++) {
//                    System.out.println("Location " + i + ": "+ "bucket: " + bucketsMsg[i] + ", key: "+ keysMsg[i]);
//                }
//            }
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
            if (sqs != null && myQueueURL != null) {
                System.out.println("Deleting the test queue.\n");
                sqs.deleteQueue(myQueueURL);
            }
        }
    }
}
