import apps.Constants;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.s3.model.Bucket;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;

import java.util.List;

public class AWSResourceTest {

    public static void main(String[] args) {

        //Configurations
        EC2Handler ec2 = new EC2Handler();
        S3Handler s3 = new S3Handler(ec2);
        SQSHandler sqs = new SQSHandler(ec2.getCredentials());

        System.out.println("\nList all instances");
        List<Instance> instancesList = ec2.listInstances();
        if (instancesList.isEmpty()) {
            System.out.println("No instances");
        }

        System.out.println("\nList all bucket and objects in them");
        List<Bucket> buckets = s3.listBucketsAndObjects();
        if (buckets.isEmpty()) {
            System.out.println("No buckets");
        }

        System.out.println("\nList all SQS queues (URL)");
        List<String> urls = sqs.listQueues();
        if (urls.isEmpty()) {
            System.out.println("No queues");
        }



    }
}
