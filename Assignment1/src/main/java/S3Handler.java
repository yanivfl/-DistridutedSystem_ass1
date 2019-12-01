import java.io.BufferedReader;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;


public class S3Handler {

    private AmazonS3 s3;
    private EC2Handler ec2;

    /**
     * initialize a connection with our S3
     * params: ec2
     */
    public S3Handler(EC2Handler ec2) {
        this.ec2 = ec2;
        this.s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(ec2.getCredentials())
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    public AmazonS3 getS3() {
        return s3;
    }

    public String getAwsBucketName(String name){
        String bucketName = this.ec2.getCredentials().getCredentials().getAWSAccessKeyId() + "a" + name.
                replace('\\', 'a').
                replace('/', 'a').
                replace(':', 'a').
                replace('.', 'a');

        bucketName = bucketName.toLowerCase();
        return bucketName;
    }

    public String getAwsFileName(String FileName){
        String fileName = FileName.
                replace('\\', '_').
                replace('/', '_').
                replace(':', '_');
        fileName = fileName.toLowerCase();
        return fileName;
    }

    /**
     * Create a bucket in S3
     * @return the bucket's name
     */
    public String createBucket(String name) {
        String bucketName = getAwsBucketName(name);
        this.s3.createBucket(bucketName);
        return bucketName;
    }

    /**
     * Create a key name by the file's path and uploads the file to S3.
     * params: ec2, s3, bucketName, filePath
     * returns: the key name of the file
     */
    public String uploadFileToS3(String bucketName, String fileName) {
        String keyName = getAwsFileName(fileName);
        File file = new File(fileName);
        PutObjectRequest request = new PutObjectRequest(bucketName, keyName, file);
        this.s3.putObject(request);
        return keyName;
    }

    public void uploadJarDirectory(String directoryName) throws IOException {
        String bucketName = getAwsBucketName(directoryName);
        String key = null;

        try {
            /*
             * Create a new S3 bucket - Amazon S3 bucket names are globally unique,
             * so once a bucket name has been taken by any user, you can't create
             * another bucket with that same name.
             *
             * You can optionally specify a location for your bucket if you want to
             * keep your data closer to your applications or users.
             */
            System.out.println("Creating bucket " + bucketName + "\n");
            this.s3.createBucket(bucketName);

            /*
             * List the buckets in your account
             */
            System.out.println("Listing buckets");
            for (Bucket bucket : this.s3.listBuckets()) {
                System.out.println(" - " + bucket.getName());
            }
            System.out.println();

            /*
             * Upload an object to your bucket - You can easily upload a file to
             * S3, or upload directly an InputStream if you know the length of
             * the data in the stream. You can also specify your own metadata
             * when uploading to S3, which allows you set a variety of options
             * like content-type and content-encoding, plus additional metadata
             * specific to your applications.
             */
            System.out.println("Uploading a new object to S3 from a file\n");
            File dir = new File(directoryName);
            for (File file : dir.listFiles()) {
                key = getAwsFileName(file.getName());
                PutObjectRequest req = new PutObjectRequest(bucketName, key, file);
                s3.putObject(req);
            }

        } catch (AmazonServiceException ase) {
            printAseException(ase);
        } catch (AmazonClientException ace) {
            printAceException(ace);
        }
    }

    public void downloadFile(String bucketName, String key) throws IOException {
        /*
         * Download an object - When you download an object, you get all of
         * the object's metadata and a stream from which to read the contents.
         * It's important to read the contents of the stream as quickly as
         * possibly since the data is streamed directly from Amazon S3 and your
         * network connection will remain open until you read all the data or
         * close the input stream.
         *
         * GetObjectRequest also supports several other options, including
         * conditional downloading of objects based on modification times,
         * ETags, and selectively downloading a range of an object.
         */
        try {

            System.out.println("Downloading an object");
            S3Object object = this.s3.getObject(new GetObjectRequest(bucketName, key));
            System.out.println("Content-Type: " + object.getObjectMetadata().getContentType());
            displayTextInputStream(object.getObjectContent());
        } catch (AmazonServiceException ase) {
            printAseException(ase);
        } catch (AmazonClientException ace) {
            printAceException(ace);
        }
    }

    public void deleteFile(String bucketName, String key) {
        try{
            this.s3.deleteObject(bucketName, key);
        } catch (AmazonServiceException ase) {
            printAseException(ase);
        } catch (AmazonClientException ace) {
            printAceException(ace);
        }
    }

    public void deleteBucket(String bucketName) {
        try{
            this.s3.deleteBucket(bucketName);
        } catch (AmazonServiceException ase) {
            printAseException(ase);
        } catch (AmazonClientException ace) {
            printAceException(ace);
        }
    }

    private void printAseException(AmazonServiceException ase){
       System.out.println("Caught an AmazonServiceException, which means your request made it "
               + "to Amazon S3, but was rejected with an error response for some reason.");
       System.out.println("Error Message:    " + ase.getMessage());
       System.out.println("HTTP Status Code: " + ase.getStatusCode());
       System.out.println("AWS Error Code:   " + ase.getErrorCode());
       System.out.println("Error Type:       " + ase.getErrorType());
       System.out.println("Request ID:       " + ase.getRequestId());
   }

    private void printAceException(AmazonClientException ace){
        System.out.println("Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with S3, "
                + "such as not being able to access the network.");
        System.out.println("Error Message: " + ace.getMessage());
    }

    /**
     * Displays the contents of the specified input stream as text.
     * @param input - The input stream to display as text.
     * @throws IOException - when the is an IO error.
     */
    private void displayTextInputStream(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("    " + line);
        }
        System.out.println();
    }


}


