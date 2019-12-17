package handlers;

import java.util.List;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SQSHandler {

    private AWSCredentialsProvider credentials;
    private AmazonSQS sqs;

    /**
     * Initialize a connection with our SQS
     * params: isClient
     * For a client - create our credentials file at ~/.aws/credentials
     * For non client - gets the credentials from the role used to create this instance
     */
    public SQSHandler(boolean isClient){
        // connect to SQS
        createCredentials(isClient);
        this.sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(credentials)
                .withRegion("us-west-2")
                .build();
    }

    private void createCredentials(boolean isClient) {
        if (isClient)
            this.credentials = new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        else
            this.credentials = new InstanceProfileCredentialsProvider(false);
    }

    public AmazonSQS getSqs() {
        return sqs;
    }

    public String getURL(String QueueName){
        return sqs.getQueueUrl(QueueName).getQueueUrl();
    }

    /**
     * Create an SQS queue
     * params: sqs, queueName
     * returns: the new queue's URL
     */
    public String createSQSQueue(String queueName, boolean shortPolling) {
        CreateQueueRequest createQueueRequest;

        if (shortPolling)
            createQueueRequest = new CreateQueueRequest(queueName);
        else
            createQueueRequest = new CreateQueueRequest(queueName).
                    addAttributesEntry("ReceiveMessageWaitTimeSeconds", "10");

        System.out.println("Created queue with the name: " + queueName);

        return this.sqs.createQueue(createQueueRequest).getQueueUrl();
    }

    public void deleteQueue(String myQueueUrl) {
        this.sqs.deleteQueue(new DeleteQueueRequest(myQueueUrl));
        System.out.println("Deleted queue with the URL: " + myQueueUrl);
    }

    public void sendMessage(String myQueueUrl, String messageBody) {
        this.sqs.sendMessage(new SendMessageRequest(myQueueUrl, messageBody));
    }

    public List<Message> receiveMessages(String myQueueUrl, boolean shortPolling, boolean visibility_timeout) {
        ReceiveMessageRequest receiveMessageRequest;

        if (shortPolling) {
            if (visibility_timeout)
                receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl)
                        .withVisibilityTimeout(20);
            else
                receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
        } else {
            if (visibility_timeout)
                receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl)
                        .withWaitTimeSeconds(20)
                        .withVisibilityTimeout(20);
            else receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl)
                        .withWaitTimeSeconds(20);
        }

        return this.sqs.receiveMessage(receiveMessageRequest).getMessages();
    }

    public boolean safelySendMessage(String myQueueUrl, String message) {
        try{
            sqs.sendMessage(myQueueUrl, message);
            return true;
        }
        catch (Exception e) {
            if (Thread.interrupted()) {
                sqs.sendMessage(myQueueUrl, message);
                System.out.println("Thread interrupted, killing it softly");
                return false;
            }
            else {
                e.printStackTrace();
            }
        }
        return true;
    }


    public boolean safelyDeleteMessages(List<Message> messages, String myQueueUrl) {
        try {
            deleteMessages(messages, myQueueUrl);
            return true;
        }
        catch (Exception e) {
            if (Thread.interrupted()) {
                deleteMessages(messages, myQueueUrl);
                return false;
            }
            else{
                e.printStackTrace();
            }
        }
        return true;
    }

    public void deleteMessages(List<Message> messages, String myQueueUrl){
        for (Message msg : messages) {
            sqs.deleteMessage(myQueueUrl, msg.getReceiptHandle());
            System.out.println("Deleted message from queue (URL): " + myQueueUrl);
        }
    }

    public List<String> listQueues() {
        List<String> urls = sqs.listQueues().getQueueUrls();
        for (String queueUrl : urls) {
            System.out.println("  QueueUrl: " + queueUrl);
        }
        return urls;
    }

}
