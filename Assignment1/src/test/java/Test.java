import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.model.DescribeTagsRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.TagDescription;
import com.amazonaws.services.ec2.model.DescribeTagsResult;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;


public class Test {

    public static void main(String[] args) throws Exception{

        // initial configurations
        AWSCredentialsProvider credentials = EC2Handler.getCredentials();

        System.out.println("connect to EC2");
        AmazonEC2 ec2 = EC2Handler.connectEC2(credentials);

        System.out.println("connect to S3");
        AmazonS3 s3 = S3Handler.connectS3(credentials);

//        testInstances(ec2);
//        String fileName = "DemoFileToS3.txt";
//        testS3(credentials, ec2, s3, fileName);


    }

    // Test EC2 instances - launch and terminate
    public static void testInstances(AmazonEC2 ec2) throws Exception {
        System.out.println("\n\n*** test EC2 ***");

        List<Instance> myInstances = EC2Handler.launchEC2Instances(ec2, 1, "");
        if(myInstances != null){
            Instance manager = myInstances.get(0);
            String instanceIdToTerminate = manager.getInstanceId();
            EC2Handler.terminateEC2Instance(ec2, instanceIdToTerminate);
        }
    }

    private static void displayTextInputStream(InputStream input) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("    " + line);
        }
        System.out.println();
    }

    // Test S3 - uploading a file
    public static void testS3(AWSCredentialsProvider credentials, AmazonEC2 ec2, AmazonS3 s3, String fileName) throws Exception {
        System.out.println("\n\n*** test S3 ***");
        String bucketName = null;
        String keyName = null;

        try {

            System.out.println("\nCreate a bucket");
            bucketName = S3Handler.createBucket(credentials, s3, fileName);

            System.out.println("\nUpload file to S3");
            keyName = S3Handler.uploadFileToS3(ec2, s3, bucketName, fileName);

            System.out.println("\nListing buckets");
            for (Bucket bucket : s3.listBuckets()) {
                System.out.println(" - " + bucket.getName());
            }

            // This works, but is heavy so I commented it out
//        System.out.println("\nDownloading an object");
//        S3Object object = s3.getObject(new GetObjectRequest(bucketName, keyName));
//        System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());
//        displayTextInputStream(object.getObjectContent());

            System.out.println("\nListing objects");
            ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
                    .withBucketName(bucketName)
                    .withPrefix(""));
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                System.out.println(" - " + objectSummary.getKey() + "  " +
                        "(size = " + objectSummary.getSize() + ")");
            }
        }
        finally {

            if (keyName != null) {
                System.out.println("\nDelete file from S3");
                S3Handler.deleteFile(s3, bucketName, keyName);
            }

            if (bucketName != null) {
                System.out.println("\nDelete bucket");
                S3Handler.deleteBucket(s3, bucketName);
            }
        }
    }


    public static void testJars(AWSCredentialsProvider credentials, AmazonEC2 ec2, AmazonS3 s3) {

        String keyName = "";
        String bucketName = "akiaj24cwsltdpfv43lqajars";

        System.out.println("Download jars zip");
        S3Object object = s3.getObject(new GetObjectRequest(bucketName, keyName));
        System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());


    }
}
