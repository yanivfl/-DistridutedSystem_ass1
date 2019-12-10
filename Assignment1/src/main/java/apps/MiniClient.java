package apps;

import apps.Constants;
import com.amazonaws.services.sqs.model.Message;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;

import java.io.IOException;
import java.util.List;

public class MiniClient {

    /**
     * This test should create a Manager with EC2, SQS, S3 permissions that will create a Worker with SQS permissions.
     */

    public static void main(String[] args) throws IOException {
        EC2Handler ec2 = new EC2Handler(true);
        S3Handler s3 = new S3Handler(true);
        SQSHandler sqs = new SQSHandler(true);

        System.out.println("create queues");
        String W2M_queue = sqs.createSQSQueue(Constants.WORKERS_TO_MANAGER_QUEUE, false);
        String M2C_queue = sqs.createSQSQueue(Constants.MANAGER_TO_CLIENTS_QUEUE, false);

        System.out.println("launch manager");
        String roleArn = ec2.getRoleARN(Constants.MANAGER_ROLE);
        ec2.launchManager_EC2Instance(roleArn, Constants.USER_DATA_PATH);

//        System.out.println("get done message from the manager");
//        List<Message> messages;
//        while (true) {
//            System.out.println("Waiting for a message");
//            messages = sqs.receiveMessages(M2C_queue, false, false);
//            if (!messages.isEmpty())
//                break;
//        }
//
//        for (Message message: messages)
//            System.out.println("Received message: " + message.getBody());
    }
}
