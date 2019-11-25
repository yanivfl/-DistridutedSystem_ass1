import java.util.List;

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



public class EC2Handler {

    private static final String AMI = "ami-b66ed3de";
    private static final String TAG_MANAGER = "TAG_MANAGER";
    private static final String TAG_WORKER = "TAG_WORKER";

    public static AmazonEC2 connectEC2(){
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        return  AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    public static List<Instance> launchEC2Instance(AmazonEC2 ec2) throws Exception {
        try {
            RunInstancesRequest request = new RunInstancesRequest(AMI, 1, 1);
            request.setInstanceType(InstanceType.T2Micro.toString());
            List<Instance> instances = ec2.runInstances(request).getReservation().getInstances();
            System.out.println("Launch instances: " + instances);
            System.out.println("You launched: " + instances.size() + " instances");
            return instances;

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Response Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
            return null;
        }
    }

    public static boolean terminateEC2Instance(AmazonEC2 ec2Client, String instanecID) throws Exception {
        try {
            TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest()
                    .withInstanceIds(instanecID);
            ec2Client.terminateInstances(terminateInstancesRequest)
                    .getTerminatingInstances()
                    .get(0)
                    .getPreviousState()
                    .getName();
            System.out.println("The Instance is terminated with id: "+ instanecID);
            return true;

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Response Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
            return false;
        }

    }

    public static void describeInstances(AmazonEC2 ec2) {
        boolean done = false;
        DescribeInstancesRequest instRequest = new DescribeInstancesRequest();

        try {
            while (!done) {
                DescribeInstancesResult response = ec2.describeInstances(instRequest);

                for (Reservation reservation : response.getReservations()) {
                    for (Instance instance : reservation.getInstances()) {

                        Filter filter = new Filter().withName("resource-id").withValues(instance.getInstanceId());
                        DescribeTagsRequest tagRequest = new DescribeTagsRequest().withFilters(filter);
                        DescribeTagsResult tagResult = ec2.describeTags(tagRequest);
                        List<TagDescription> tags = tagResult.getTags();
                        boolean isManagerInst = isMangaer(tags);

//                        // The app should use the existing manager node
//                        if (isManagerInst) {
//                            // TODO
//                        }
//                        // The app should start the manager node
//                        else {
//                            // TODO
//                        }

                        System.out.printf(
                                "Found instance with id %s, " +
                                        "AMI %s, " +
                                        "type %s, " +
                                        "state %s " +
                                        "and monitoring state %s",
                                instance.getInstanceId(),
                                instance.getImageId(),
                                instance.getInstanceType(),
                                instance.getState().getName(),
                                instance.getMonitoring().getState());
                    }
                }

                instRequest.setNextToken(response.getNextToken());
                if (response.getNextToken() == null) {
                    done = true;
                }
            }
        }

        catch (AmazonServiceException ase) {
            printException(ase);
        }
    }

    /**
     * Go through the list of tags and searches a manager
     * @param tags - a list of tags
     * @return True: The is describes a manager, False: otherwise
     */
    private static boolean isMangaer(List<TagDescription> tags) {
        for (TagDescription tag: tags) {
            if (tag.getValue().equals(TAG_MANAGER))
                return true;
        }
        return false;
    }

    private static void printException(AmazonServiceException ase) {
        System.out.println("Caught Exception: " + ase.getMessage());
        System.out.println("Response Status Code: " + ase.getStatusCode());
        System.out.println("Error Code: " + ase.getErrorCode());
        System.out.println("Request ID: " + ase.getRequestId());

    }
}

