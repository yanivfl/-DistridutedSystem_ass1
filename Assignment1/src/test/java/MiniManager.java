import apps.Constants;
import com.amazonaws.services.sqs.model.Message;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;
import java.util.List;

public class MiniManager {

    /**
     * This test should use EC2, S3, SQS
     */

    public static void main(String[] args) {
        EC2Handler ec2 = new EC2Handler(false);
        S3Handler s3 = new S3Handler(false);
        SQSHandler sqs = new SQSHandler(false);

        System.out.println("use ec2 - create worker");
        ec2.launchWorkers_EC2Instances(1, ec2.getRoleARN(Constants.WORKERS_ROLE));

        System.out.println("use s3 - create and delete bucket");
        String testBucket = "TestBucket";
        s3.createBucket(testBucket);
        s3.deleteBucket(testBucket);

        System.out.println("use sqs");
        String W2M_queue = sqs.getURL(Constants.WORKERS_TO_MANAGER_QUEUE);
        List<Message> messages;

        while (true) {
            System.out.println("Waiting to receive a message");
            messages = sqs.receiveMessages(W2M_queue, false, false);
            if (!messages.isEmpty())
                break;
        }

        for (Message message: messages)
            System.out.println("Received message: " + message.getBody());

        System.out.println("Send message to the MiniClient");
        String M2C_queue = sqs.getURL(Constants.MANAGER_TO_CLIENTS_QUEUE);
        sqs.sendMessage(M2C_queue, "I received a test message from the worker");
    }
}