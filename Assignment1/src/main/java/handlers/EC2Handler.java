package handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import apps.Constants;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.ec2.model.Tag;



public class EC2Handler {

    private AWSCredentialsProvider credentials;
    private AmazonEC2 ec2;

    /**
     * Create our credentials file at ~/.aws/credentials
     * Initialize a connection with our EC2
     */
    public EC2Handler() {
        this.credentials = new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        this.ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentials)
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    public AWSCredentialsProvider getCredentials() {
        return credentials;
    }

    public AmazonEC2 getEc2() {
        return ec2;
    }

    /**
     * launch machine instances as requested in machineCount
     * @param tagName: instance of EC2
     * @param machineCount: number of machine instances to launch
     * @return List<Instance>: list of machines instances we launched
     */
    public List<Instance> launchEC2Instances(int machineCount, Constants.INSTANCE_TAG tagName) {
        try {
            // launch instances
            RunInstancesRequest runInstrequest = new RunInstancesRequest(Constants.AMI, machineCount, machineCount);
            runInstrequest.setInstanceType(InstanceType.T2Micro.toString());
            List<Instance> instances = this.ec2.runInstances(runInstrequest).getReservation().getInstances();

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



    /**
     * terminate requested machine instance
     * @param instanecID
     * @return boolean: true  iff instance was terminated
     */
    public boolean terminateEC2Instance(String instanecID) {
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

    /** List all ec2 instances with their status and tags */
    public List<Instance> listInstances() {
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
}

