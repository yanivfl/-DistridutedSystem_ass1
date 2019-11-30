import java.util.List;
import java.util.Map.Entry;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SQSHandler {

    private AWSCredentialsProvider credentials;
    private AmazonSQS sqs;

    /**
     * Initialize a connection with our SQS
     * params: credentials
     */
    public SQSHandler(AWSCredentialsProvider credentials){

        // connect to SQS
        this.credentials = credentials;
        this.sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(credentials)
                .withRegion("us-west-2")
                .build();
    }

    /**
     * Create an SQS queue
     * params: sqs, queueName
     * returns: the new queue's URL
     */
    public String createSQSQueue(String queueName) {
        CreateQueueRequest createQueueRequest = new CreateQueueRequest("MyQueue"+ queueName);
        return this.sqs.createQueue(createQueueRequest).getQueueUrl();
    }

    public void deleteQueue(String myQueueUrl) {
        this.sqs.deleteQueue(new DeleteQueueRequest(myQueueUrl));
    }

    public void sendMessage(String myQueueUrl, String messageBody) {
        this.sqs.sendMessage(new SendMessageRequest(myQueueUrl, messageBody));
    }

    public List<Message> receiveMessages(String myQueueUrl) {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
        return this.sqs.receiveMessage(receiveMessageRequest).getMessages();
    }

    /**
     * Create a message that describes a list of locations
     * Note: the locations are from this convention: "[bucket name 0,key name 0;...;bucket name n,key name n;]"
     * for example: a list of 3 locations: [buck0,key0;buck1,key3;buck2,key2;]
     * params: myQueueUrl, bucketNames, keyNames
     * return: the location message
     */
    public String createS3LocationMessage(String myQueueUrl, String[] bucketNames, String[] keyNames) {

        // make sure the arrays are from the same length
        if (bucketNames.length != keyNames.length)
            throw new RuntimeException("buckets names and key names lists are not from the same length");
        int len = bucketNames.length;

        // create the message string
        StringBuilder msg = new StringBuilder();
        msg.append("[");

        for (int i=0; i<len; i++) {
            String locationStr = bucketNames[i] + "," + keyNames[i] + ";";
            msg.append(locationStr);
        }
        msg.append("]");
        return msg.toString();
    }

    /**
     * Parse a message (string) that describes a list of locations
     * Note: the locations msg are from this convention: "[bucket name 0,key name 0;...;bucket name n,key name n;]"
     *       for example: a list of 3 locations: [buck0,key0;buck1,key3;buck2,key2;]
     * params: msg
     * returns: a 2 dimentional string array:
     *          [0] = buckets list
     *          [1] = keys list
     */
    public String[][] parseS3LocationMessages(String msg) {
        String[] buckets_keys = msg.substring(1, msg.length()-1).split(";");
        String [][] output = new String[2][buckets_keys.length];

        for (int i=0; i<buckets_keys.length; i++) {
            String[] bucket_key = buckets_keys[0].split(",");
            output[0][i] = bucket_key[0];
            output[1][i] = bucket_key[1];
        }

        return output;
    }

    public void deleteMessage(List<Message> messages, String myQueueUrl) {
        String messageRecieptHandle = messages.get(0).getReceiptHandle();
        this.sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageRecieptHandle));
    }

    public void listQueues() {
        for (String queueUrl : sqs.listQueues().getQueueUrls()) {
            System.out.println("  QueueUrl: " + queueUrl);
        }
    }







}
