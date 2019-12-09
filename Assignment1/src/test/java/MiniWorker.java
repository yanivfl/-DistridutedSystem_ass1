import apps.Constants;
import handlers.EC2Handler;
import handlers.SQSHandler;

public class MiniWorker {

    /**
     * This test should use SQS
     */

    public static void main(String[] args) {
        EC2Handler ec2 = new EC2Handler(false);
        SQSHandler sqs = new SQSHandler(false);

        String W2M_queue = sqs.getURL(Constants.WORKERS_TO_MANAGER_QUEUE);
        sqs.sendMessage(W2M_queue, "test worker to manager");
    }
}
