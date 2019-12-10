package handlers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import apps.Constants;
import apps.RunnableManager;
import apps.RunnableWorker;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.GetInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleResult;
import com.amazonaws.services.ec2.model.Tag;
import org.apache.commons.codec.binary.Base64;


public class EC2Handler {

    private AWSCredentialsProvider credentials;
    private AmazonEC2 ec2;

    /**
     * For a client - create our credentials file at ~/.aws/credentials
     * For non client - gets the credentials from the role used to create this instance
     * Initialize a connection with our EC2
     */
    public EC2Handler(boolean isClient) {
        createCredentials(isClient);
        this.ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentials)
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    public void createCredentials(boolean isClient) {
        if (isClient)
            this.credentials = new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        else
            this.credentials = new InstanceProfileCredentialsProvider(false);
    }

    public void setCredentials(AWSCredentialsProvider credentials) {
        this.credentials = credentials;
    }

    /**
     * Creates a Base64 string from a given filePath of a userdata.
     * params: filePath
     * returns: userData string in base64
     */
    public String encodeUserDataFile(String filePath) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        String userDataContent = new String(encoded, Charset.defaultCharset());
        return Base64.encodeBase64String(userDataContent.getBytes());
    }

    public AWSCredentialsProvider getCredentials() {
        return credentials;
    }

    public String getRoleARN(String roleName) {
        AmazonIdentityManagement client = AmazonIdentityManagementClientBuilder.standard()
                .withCredentials(getCredentials())
                .withRegion(Regions.US_EAST_1)
                .build();
        GetRoleRequest request = new GetRoleRequest().withRoleName(roleName);
        GetRoleResult response = client.getRole(request);
        String arn = response.getRole().getArn();
        System.out.println("Got ARN: " + arn);
        return arn;
    }

    public AmazonEC2 getEc2() {
        return ec2;
    }

    /**
     * launch a manager machine instances
     * params: managerArn with EC2, S3, SQS permissions
     * returns: the manager instance
     */
    public Instance launchManager_EC2Instance(String managerArn, String userDataPath) throws IOException {
        if(Constants.DEBUG_MODE){
            Runnable manager = new RunnableManager();
            Thread managerThread = new Thread(manager);
            managerThread.setName("Manager-Thread");
            managerThread.start();
            System.out.println("Launch instance: " + "DEBUG" + ", with tag: " + Constants.INSTANCE_TAG.MANAGER);
            return null;
        }

        try {
            String userData = encodeUserDataFile(userDataPath);

            // launch instances
            RunInstancesRequest runInstanceRequest = new RunInstancesRequest(Constants.AMI, 1, 1)
                    .withIamInstanceProfile(new IamInstanceProfileSpecification().withArn(managerArn.replaceFirst("role", "instance-profile")))
                    .withUserData(userData)
                    .withInstanceType(InstanceType.T2Micro.toString());
            List<Instance> instances = this.ec2.runInstances(runInstanceRequest).getReservation().getInstances();
            Instance manager = instances.get(0);

            // tag the manager with tag manager
            Tag tag = new Tag().withKey("Type").withValue(Constants.INSTANCE_TAG.MANAGER.toString());
            CreateTagsRequest createTagsRequest = new CreateTagsRequest()
                    .withResources(manager.getInstanceId())
                    .withTags(tag);
            ec2.createTags(createTagsRequest);

            System.out.println("Launch instance: " + manager + ", with tag: " + Constants.INSTANCE_TAG.MANAGER);
            return manager;
        }
        catch (AmazonServiceException ase) {
            printASEException(ase);
            return null;
        }
    }

    /**
     * launch workers machine instances as requested in machineCount
     * params: machineCount - number of machine instances to launch, managerArn - with EC2, S3, SQS permissions
     * returns: List<Instance> list of machines instances we launched
     */
    public List<Instance> launchWorkers_EC2Instances(int machineCount, String workersArn) {
        if(Constants.DEBUG_MODE){
            for (int i = 0; i < machineCount; i++) {
                Runnable worker = new RunnableWorker();
                Thread workerThread = new Thread(worker);
                workerThread.setName("Worker-Thread");
                workerThread.start();
                System.out.println("Launch instances: " + "DEBUG");
                System.out.println("You launched: " + machineCount + " instances" + ", with tag: " + Constants.INSTANCE_TAG.WORKER);
            }
            return new LinkedList<>();
        }
        try {
            // launch instances
            RunInstancesRequest runInstanceRequest = new RunInstancesRequest(Constants.AMI, machineCount, machineCount)
                    .withIamInstanceProfile(new IamInstanceProfileSpecification().withArn(workersArn))
//                    .withUserData()   // TODO
                    .withInstanceType(InstanceType.T2Micro.toString());
            List<Instance> instances = this.ec2.runInstances(runInstanceRequest).getReservation().getInstances();

            // tag instances with the given tag
            for (Instance inst: instances) {
                Tag tag = new Tag().withKey("Type").withValue(Constants.INSTANCE_TAG.WORKER.toString());
                CreateTagsRequest createTagsRequest = new CreateTagsRequest()
                        .withResources(inst.getInstanceId())
                        .withTags(tag);
                ec2.createTags(createTagsRequest);
            }

            System.out.println("Launch instances: " + instances);
            System.out.println("You launched: " + instances.size() + " instances" + ", with tag: " + Constants.INSTANCE_TAG.WORKER);
            return instances;

        }
        catch (AmazonServiceException ase) {
            printASEException(ase);
            return null;
        }
    }

    /**
     * terminate requested machine instance
     * @param instanecID
     * @return boolean: true  iff instance was terminated
     */
    public boolean terminateEC2Instance(String instanecID) {
        if (Constants.DEBUG_MODE){
            System.out.println("The Instance is terminated with id: "+ "DEBUG BRUHHHH");
            return true;
        }

        try {
            TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest()
                    .withInstanceIds(instanecID);
                        this.ec2.terminateInstances(terminateInstancesRequest)
                    .getTerminatingInstances()
                    .get(0)
                    .getPreviousState()
                    .getName();
            System.out.println("The Instance is terminated with id: "+ instanecID);
            return true;

        } catch (AmazonServiceException ase) {
            printASEException(ase);
            return false;
        }

    }

    /**
     * Go through the list of instances in search of a given tag
     * params: ec2, tag
     * returns: True: There is an instance with the requested tag , False: otherwise
     */
    public boolean isTagExists(Constants.INSTANCE_TAG tag) {
        if (Constants.DEBUG_MODE){
            if(Constants.INSTANCE_TAG.MANAGER.toString().equals(tag.toString())){
                return !Constants.IS_MANAGER_ON.compareAndSet(false,true);
            }
            return false;
        }

        boolean done = false;   // done = True - when finished going over all the instances.
        DescribeInstancesRequest instRequest = new DescribeInstancesRequest();

        try {
            while (!done) {

                // Go through all instances
                DescribeInstancesResult response = this.ec2.describeInstances(instRequest);

                for (Reservation reservation : response.getReservations()) {
                    for (Instance instance : reservation.getInstances()) {

                        // check instance status - look for a running status
                        boolean isRunning = instance.getState().getName().equals("running");

                        // if the instance status is pending, then wait for it to turn to running and then test it again
                        if (!isRunning && instance.getState().getName().equals("pending")) {
                            System.out.println("pending, trying again");
                            DescribeInstancesRequest pendingInstRequest = new DescribeInstancesRequest()
                                    .withInstanceIds(instance.getInstanceId());
                            DescribeInstancesResult pendingInstResponse = this.ec2.describeInstances(pendingInstRequest);
                            Instance pendingInstance = pendingInstResponse.getReservations().get(0).getInstances().get(0);

                            while (pendingInstance.getState().getName().equals("pending")) {
                                pendingInstRequest = new DescribeInstancesRequest()
                                        .withInstanceIds(instance.getInstanceId());
                                pendingInstResponse = this.ec2.describeInstances(pendingInstRequest);
                                pendingInstance = pendingInstResponse.getReservations().get(0).getInstances().get(0);
                            }

                            System.out.println("now status is: " + pendingInstance.getState().getName());
                            isRunning = pendingInstance.getState().getName().equals("running");
                        }

                        if (isRunning) {
                            Filter filter = new Filter().withName("resource-id").withValues(instance.getInstanceId());
                            DescribeTagsRequest tagRequest = new DescribeTagsRequest().withFilters(filter);
                            DescribeTagsResult tagResult = this.ec2.describeTags(tagRequest);
                            List<TagDescription> tags = tagResult.getTags();
                            for (TagDescription tagDesc : tags) {
                                if (tagDesc.getValue().equals(tag.toString()))
                                    return true;
                            }
                        }
                    }
                }

                instRequest.setNextToken(response.getNextToken());
                if (response.getNextToken() == null) {
                    done = true;
                }
            }
        }

        catch (AmazonServiceException ase) {
            printASEException(ase);
        }

        return false;
    }

    /**
     * List all ec2 instances with their status and tags
     * */
    public List<Instance> listInstances(boolean print) {
        List<Instance> instances = new LinkedList<>();
        boolean done = false;   // done = True - when finished going over all the instances.
        DescribeInstancesRequest instRequest = new DescribeInstancesRequest();

        try {
            while (!done) {

                // Go through all instances
                DescribeInstancesResult response = this.ec2.describeInstances(instRequest);

                for (Reservation reservation : response.getReservations()) {
                    for (Instance instance : reservation.getInstances()) {

                        String state = instance.getState().getName();

                        StringBuilder tagsBuilder = new StringBuilder();
                        tagsBuilder.append("tags: ");
                        Filter filter = new Filter().withName("resource-id").withValues(instance.getInstanceId());
                        DescribeTagsRequest tagRequest = new DescribeTagsRequest().withFilters(filter);
                        DescribeTagsResult tagResult = this.ec2.describeTags(tagRequest);
                        List<TagDescription> tags = tagResult.getTags();
                        for (TagDescription tagDesc : tags) {
                            tagsBuilder.append(tagDesc.getValue());
                            tagsBuilder.append(" ");
                        }

                        if (print)
                            System.out.println("instance: " + instance.getInstanceId() + ", state: " + state + ", with tags: " + tagsBuilder.toString());
                        instances.add(instance);
                    }
                }

                instRequest.setNextToken(response.getNextToken());
                if (response.getNextToken() == null) {
                    done = true;
                }
            }
            return instances;
        }

        catch (AmazonServiceException ase) {
            printASEException(ase);
            return null;
        }
    }

    /**
     * prints AmazonServiceException description
     * @param ase - AmazonServiceException
     */
    private void printASEException(AmazonServiceException ase) {
        System.out.println("Caught Exception: " + ase.getMessage());
        System.out.println("Response Status Code: " + ase.getStatusCode());
        System.out.println("Error Code: " + ase.getErrorCode());
        System.out.println("Request ID: " + ase.getRequestId());

    }



    // ************************* For tests usage ***************************

    /** For tests usage
     * launch machine instances as requested in machineCount
     * @param tagName: instance of EC2
     * @param machineCount: number of machine instances to launch
     * @return List<Instance>: list of machines instances we launched
     */
    public List<Instance> launchEC2Instances(int machineCount, Constants.INSTANCE_TAG tagName) {
        try {
            // launch instances
            RunInstancesRequest runInstanceRequest = new RunInstancesRequest(Constants.AMI, machineCount, machineCount)
//                    .withIamInstanceProfile(new IamInstanceProfileSpecification().withArn())     // TODO
//                    .withUserData()   // TODO
                    .withInstanceType(InstanceType.T2Micro.toString());
            List<Instance> instances = this.ec2.runInstances(runInstanceRequest).getReservation().getInstances();

            // tag instances with the given tag
            for (Instance inst: instances) {
                Tag tag = new Tag().withKey("Type").withValue(tagName.toString());
                CreateTagsRequest createTagsRequest = new CreateTagsRequest().
                        withResources(inst.getInstanceId())
                        .withTags(tag);
                ec2.createTags(createTagsRequest);
            }

            System.out.println("Launch instances: " + instances);
            System.out.println("You launched: " + instances.size() + " instances" + ", with tag: " + tagName);
            return instances;

        } catch (AmazonServiceException ase) {
            printASEException(ase);
            return null;
        }
    }
}

