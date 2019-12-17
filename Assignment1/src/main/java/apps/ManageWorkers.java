package apps;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.sqs.model.Message;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;
import messages.Manager2Client;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
    After the manger receives response messages from the workers on all the files on an input file, then it:
        Creates a summary output file accordingly,
        Uploads the output file to S3,
        Sends a message to the application with the location of the file.
*/

public class ManageWorkers implements Runnable {
    private ConcurrentMap<String, ClientInfo> clientsInfo;
    private AtomicInteger filesCount;
    private AtomicInteger regulerWorkersCount;
    private AtomicInteger extraWorkersCount;
    private PriorityQueue<Integer> maxWorkersPerFile;
    private AtomicBoolean terminate;
    private Object waitingObject;
    private EC2Handler ec2;
    private S3Handler s3;
    private SQSHandler sqs;


    public ManageWorkers(ConcurrentMap<String, ClientInfo> clientsInfo, AtomicInteger filesCount,
                         AtomicInteger regulerWorkersCount, AtomicInteger extraWorkersCount,
                         PriorityQueue<Integer> maxWorkersPerFile, AtomicBoolean terminate, Object waitingObject,
                         EC2Handler ec2, S3Handler s3, SQSHandler sqs) {
        this.clientsInfo = clientsInfo;
        this.filesCount = filesCount;
        this.regulerWorkersCount = regulerWorkersCount;
        this.extraWorkersCount = extraWorkersCount;
        this.maxWorkersPerFile = maxWorkersPerFile;
        this.terminate = terminate;
        this.waitingObject = waitingObject;
        this.ec2 = ec2;
        this.s3 = s3;
        this.sqs = sqs;
    }

    @Override
    public void run() {
        Constants.printDEBUG("Manage-workers: started running");

        // Get the (Worker -> Manager) ( Manager -> Clients) SQS queues URLs
        String W2M_QueueURL = sqs.getURL(Constants.WORKERS_TO_MANAGER_QUEUE);
        String M2C_QueueURL = sqs.getURL(Constants.MANAGER_TO_CLIENTS_QUEUE);
        boolean running = true;

        while (running && !Thread.interrupted()){
            List<Message> workerMessages = new LinkedList<>();
            try{
                workerMessages = sqs.receiveMessages(W2M_QueueURL,false, true);
            }catch (Exception e) {
                if (Thread.interrupted()) {
                    Constants.printDEBUG("Thread interrupted, killing it softly");
                    break;
                } else {
                    e.printStackTrace();
                }
            }

            Constants.printDEBUG("Manager received " + workerMessages.size() + " Messages from W2M Queue");

            for (Message workerMsg : workerMessages) {

                // parse json
                JSONObject msgObj= Constants.validateMessageAndReturnObj(workerMsg , Constants.TAGS.WORKER_2_MANAGER, true);
                if (msgObj == null){
                    Constants.printDEBUG("DEBUG Manage WORKERs: couldn't parse this message!!!");
                    continue;
                }

                String inBucket = (String) msgObj.get(Constants.IN_BUCKET);
                String inKey = (String) msgObj.get(Constants.IN_KEY);

                ClientInfo clientInfo = clientsInfo.get(inBucket);
                if(clientInfo == null){
                    if (Constants.DEBUG_MODE){
                        Constants.printDEBUG("DEBUG Manage WORKERs: clientInfo is null!!!");
                    }
                    continue;
                }

                boolean isUpdated = clientInfo.updateLocalOutputFile(inBucket,inKey, msgObj.toJSONString());
                if (isUpdated) {

                    // check if there are more reviews for this file
                   long reviewsLeft = clientInfo.decOutputCounter(inKey);
                   if (reviewsLeft == 0){
                       String outKey = clientInfo.getOutKey(inKey);
                       s3.uploadLocalToS3(inBucket, clientInfo.getLocalFileName(inBucket,inKey), outKey);
                       clientInfo.deleteLocalFile(inBucket, inKey);
                       filesCount.decrementAndGet();
                       removeWorkersIfNeeded(clientInfo, inKey);
                       synchronized (waitingObject){
                           waitingObject.notifyAll();
                       }

                      // check if there are no more files for this client
                      int outputFilesLeft = clientInfo.decOutputFilesLeft();
                      if (outputFilesLeft == 0){
                          Constants.printDEBUG("sending done mail to client");
                          running = sqs.safelySendMessage(M2C_QueueURL,new Manager2Client(true, inBucket)
                                  .stringifyUsingJSON());
                          clientsInfo.remove(inBucket);
                          synchronized (waitingObject){
                              waitingObject.notifyAll();
                          }
                      }
                   }
                }
            }

            // delete received messages (after handling them)
            if (!workerMessages.isEmpty()){
                running = sqs.safelyDeleteMessages(workerMessages, W2M_QueueURL);
            }
            if (clientsInfo.isEmpty() && terminate.get()) {
                running = false;
            }
        }
        Constants.printDEBUG("DEBUG MANAGE-WORKERS: Thread left safely, terminate is: " + terminate.get());
        synchronized (waitingObject){
            waitingObject.notifyAll();
        }

    }

    private void removeWorkersIfNeeded(ClientInfo clientInfo, String inKey) {
        // tell the manager there is one less client to serve
        synchronized (waitingObject) {

            int extraWorkersToTerminate= 0;
            // add scalability
            int totalExtraWorkers = filesCount.get() / Constants.ADD_EXTRA_WORKER; //extra worker for every 3 files
            while(extraWorkersCount.get() > totalExtraWorkers ){
                extraWorkersCount.decrementAndGet();
                extraWorkersToTerminate++;
            }

            // terminate unneeded workers
            long workersPerClient = clientInfo.getTotalFileReviews(inKey) / clientInfo.getReviewsPerWorker();
            maxWorkersPerFile.remove((int)workersPerClient);
            Integer currMax = maxWorkersPerFile.peek();
            if (currMax==null){
                waitingObject.notifyAll();
                return;
            }

            int regulerWorkersToTerminate = Math.max(regulerWorkersCount.get() - currMax,0);
            int numberOfWorkersToTerminate = regulerWorkersToTerminate + extraWorkersToTerminate;

            if (numberOfWorkersToTerminate > 0) {
                List<Instance> instances = ec2.listInstances(false);
                for (Instance instance: instances) {
                    for (Tag tag: instance.getTags()) {
                        if (tag.getValue().equals(Constants.INSTANCE_TAG.WORKER.toString())
                                && instance.getState().getName().equals("running")) {
                            ec2.terminateEC2Instance(instance.getInstanceId());
                            numberOfWorkersToTerminate --;
                            break;
                        }
                    }
                    if (numberOfWorkersToTerminate == 0){
                        break;
                    }
                }
            }
            regulerWorkersCount.set(regulerWorkersCount.get() - regulerWorkersToTerminate);
            waitingObject.notifyAll();
        }
    }

}
