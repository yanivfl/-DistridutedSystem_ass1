import apps.Constants;
import com.amazonaws.services.sqs.model.Message;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;

import java.util.LinkedList;
import java.util.List;

public class MiniManager {

    /**
     * This test should use EC2, S3, SQS
     */

    public static void main(String[] args) {
        EC2Handler ec2 = new EC2Handler(false);
        S3Handler s3 = new S3Handler(false);
        SQSHandler sqs = new SQSHandler(false);

        // use ec2 - create worker



        // use s3



        // use sqs
        String W2M_queue = sqs.createSQSQueue(Constants.WORKERS_TO_MANAGER_QUEUE, false);
        List<Message> messages = null;

        while (true) {
            messages = sqs.receiveMessages(W2M_queue, false, false);
            if (!messages.isEmpty())
                break;
        }

        for (Message message: messages)
            System.out.println("Received message: " + message.getBody());

    }
}