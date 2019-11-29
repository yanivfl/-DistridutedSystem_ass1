import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import java.io.File;
import java.util.List;
import java.util.UUID;


/** From the assignment description:
 * 1. Checks if a Manager node is active on the EC2 cloud. If it is not, the application will start the manager node.
 * 2. Uploads the file to S3.
 * 3. Sends a message to an SQS queue, stating the location of the file on S3
 * 4. Checks an SQS queue for a message indicating the process is done and the response (the summary file) is available on S3.
 * 5. Downloads the summary file from S3, and create an html file representing the results.
 * 6. Sends a termination message to the Manager if it was supplied as one of its input arguments.
 */

public class LocalApplication {

    public static void main(String[] args) throws Exception {

        // initial configurations
        AWSCredentialsProvider credentials = EC2Handler.getCredentials();
        AmazonEC2 ec2 = EC2Handler.connectEC2(credentials);
        AmazonS3 s3 = S3Handler.connectS3(credentials);

        // extract input file name, output file names and optional termination message from args
        // example args: inputFileName1… inputFileNameN outputFileName1… outputFileNameN n terminate(optional)
        boolean terminate = (args.length % 2 == 0);
        int n = (args.length-1)/2;

        // TODO - Check if a Manager node is active on the EC2 cloud. If it is not, the application will start the manager node
        if (!EC2Handler.isTagExists(ec2, Constants.TAG_MANAGER)) {

            // TODO: understand how to run instances with a tag
//            startManager(ec2);
        }

        // Create a bucket for this local application
        UUID LocalApplicationID = UUID.randomUUID();
        String bucketName = S3Handler.createBucket(credentials, s3, LocalApplicationID.toString());

        // Upload all the input files to S3
        String[] keyNames = new String[n];
        for (int i=0; i<n; i++) {
            String fileName = args[i];
            keyNames[i] = S3Handler.uploadFileToS3(ec2, s3, bucketName, fileName);
        }


        // TODO: Send a message to an SQS queue, stating the location of the file on S3
        // TODO: Check an SQS queue for a message indicating the process is done and the response (the summary file) is available on S3.
        // TODO: Download the summary file from S3, and create an html file representing the results.
        // TODO: Send a termination message to the Manager if it was supplied as one of its input arguments.


        // delete all input files from S3 and the bucket for this local application
        for (int i=0; i<n; i++) {
            S3Handler.deleteFile(s3, bucketName, keyNames[i]);
        }
        S3Handler.deleteBucket(s3, bucketName);

    }
}
