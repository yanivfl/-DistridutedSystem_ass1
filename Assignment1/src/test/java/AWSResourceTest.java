import apps.Constants;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;

import java.util.List;

public class AWSResourceTest {

    public static void main(String[] args) {
        System.out.println("to delete everything, insert paramaters 1 2 3");
        boolean delete_instances = false;
        boolean delete_buckets = false;
        boolean delete_sqs = false;
        if(args.length >0)
            delete_instances = true;
        if (args.length > 1)
            delete_buckets = true;
        if (args.length >2)
            delete_sqs = true;


        //Configurations
        EC2Handler ec2 = new EC2Handler();
        S3Handler s3 = new S3Handler(ec2);
        SQSHandler sqs = new SQSHandler(ec2.getCredentials());

        System.out.println("\nList all instances");
        List<Instance> instancesList = ec2.listInstances();
        if (delete_instances){
            for ( Instance instance: instancesList) {
                ec2.terminateEC2Instance(instance.getInstanceId());
            }
        }
        if (instancesList.isEmpty()) {
            System.out.println("No instances");
        }

        System.out.println("\nList all bucket and objects in them");
        List<Bucket> buckets = s3.listBucketsAndObjects();
        if (delete_buckets){
            for ( Bucket bucket: buckets) {
                s3.deleteBucket(bucket.getName());
            }
        }
        if (buckets.isEmpty()) {
            System.out.println("No buckets");
        }

        System.out.println("\nList all SQS queues (URL)");
        List<String> urls = sqs.listQueues();
        if (delete_sqs){
            for ( String url: urls) {
               sqs.deleteQueue(url);
            }
        }
        if (urls.isEmpty()) {
            System.out.println("No queues");
        }
    }
}
