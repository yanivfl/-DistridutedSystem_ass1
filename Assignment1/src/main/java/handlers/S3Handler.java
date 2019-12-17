package handlers;

import java.io.*;
import java.util.List;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;


public class S3Handler {

    private AmazonS3 s3;
    private AWSCredentialsProvider credentials;

    /**
     * initialize a connection with our S3
     * params: isClient
     * For a client - create our credentials file at ~/.aws/credentials
     * For non client - gets the credentials from the role used to create this instance
     */
    public S3Handler(boolean isClient) {
        createCredentials(isClient);
        this.s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(credentials)
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    private void createCredentials(boolean isClient) {
        if (isClient)
            this.credentials = new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        else
            this.credentials = new InstanceProfileCredentialsProvider(false);
    }

    public AmazonS3 getS3() {
        return s3;
    }

    public String getAwsBucketName(String name){
        String bucketName = credentials.getCredentials().getAWSAccessKeyId() + "a" + name.
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

    /**
     * Create a key name by keyName and uploads the fileName to S3.
     * params: ec2, s3, bucketName, filePath
     * returns: the key name of the file
     */
    public String uploadLocalToS3(String bucketName, String fileName, String keyName) {
        File file = new File(fileName);
        PutObjectRequest request = new PutObjectRequest(bucketName, keyName, file);
        this.s3.putObject(request);
        return keyName;
    }

    public BufferedReader downloadFile(String bucketName, String key) throws IOException {
        try {
            System.out.println("Downloading an object");
            S3Object object = this.s3.getObject(new GetObjectRequest(bucketName, key));
            System.out.println("Content-Type: " + object.getObjectMetadata().getContentType());
            return new BufferedReader(new InputStreamReader(object.getObjectContent()));
        } catch (AmazonServiceException ase) {
            printAseException(ase);
        } catch (AmazonClientException ace) {
            printAceException(ace);
        }
        return null;
    }

    public void displayFile(String bucketName, String key) throws IOException {
        try {

            System.out.println("Displaying an object");
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

    public void deleteAllFilesInBucket(String bucketName){
        //erase all files
        ObjectListing objectListing = getS3().listObjects(new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(""));
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            deleteFile(bucketName, objectSummary.getKey());
        }
    }

    public void deleteBucket(String bucketName) {
        try{
            deleteAllFilesInBucket(bucketName);
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

    /** List all s3 buckets with their files */
    public List<Bucket> listBucketsAndObjects() {
        List<Bucket> buckets = s3.listBuckets();
        System.out.println("S3 buckets are:");
        for (Bucket bucket : buckets) {
            System.out.println("* " + bucket.getName());
            System.out.println("    Objects in this bucket are:");

            ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
                    .withBucketName(bucket.getName())
                    .withPrefix(""));
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                System.out.println("    - " + objectSummary.getKey() + "  (size = " + objectSummary.getSize() + ")");
            }
        }
        return buckets;
    }
}
