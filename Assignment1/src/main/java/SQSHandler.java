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
    public String createSQSQueue(String queueName, boolean shortPolling) {
        CreateQueueRequest createQueueRequest;

        if (shortPolling)
            createQueueRequest = new CreateQueueRequest("MyQueue"+ queueName);
        else
            createQueueRequest = new CreateQueueRequest("MyQueue"+ queueName).
                    addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");

        return this.sqs.createQueue(createQueueRequest).getQueueUrl();
    }

    public void deleteQueue(String myQueueUrl) {
        this.sqs.deleteQueue(new DeleteQueueRequest(myQueueUrl));
    }

    public void sendMessage(String myQueueUrl, String messageBody) {
        this.sqs.sendMessage(new SendMessageRequest(myQueueUrl, messageBody));
    }

    public List<Message> receiveMessages(String myQueueUrl, boolean shortPolling) {
        ReceiveMessageRequest receiveMessageRequest;

        if (shortPolling)
            receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
        else
            receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl).withWaitTimeSeconds(40);

        return this.sqs.receiveMessage(receiveMessageRequest).getMessages();
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
