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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
    After the manger receives response messages from the workers on all the files on an input file, then it:
        Creates a summary output file accordingly,
        Uploads the output file to S3,
        Sends a message to the application with the location of the file.
*/

public class ManageWorkers implements Runnable {
    private ConcurrentMap<String, ClientInfo> clientsInfo;
    private AtomicInteger clientsCount;
    private AtomicInteger workersCount;
    private AtomicInteger extraWorkersCount;
    private PriorityQueue<Integer> maxWorkersPerClient;
    private Object waitingObject;
    private EC2Handler ec2;
    private S3Handler s3;
    private SQSHandler sqs;

    public ManageWorkers(ConcurrentMap<String, ClientInfo> clientsInfo, AtomicInteger clientsCount, AtomicInteger workersCount, AtomicInteger extraWorkersCount, PriorityQueue<Integer> maxWorkersPerClient, Object waitingObject,
                         EC2Handler ec2, S3Handler s3, SQSHandler sqs) {
        this.clientsInfo = clientsInfo;
        this.clientsCount = clientsCount;
        this.workersCount = workersCount;
        this.extraWorkersCount = extraWorkersCount;
        this.maxWorkersPerClient = maxWorkersPerClient;
        this.waitingObject = waitingObject;
        this.ec2 = ec2;
        this.s3 = s3;
        this.sqs = sqs;
    }

    @Override
    public void run() {
        JSONParser jsonParser = new JSONParser();

        // Get the (Worker -> Manager) ( Manager -> Clients) SQS queues URLs
        String W2M_QueueURL = sqs.getURL(Constants.WORKERS_TO_MANAGER_QUEUE);
        String M2C_QueueURL = sqs.getURL(Constants.MANAGER_TO_CLIENTS_QUEUE);

        while (true){
            List<Message> workerMessages = sqs.receiveMessages(W2M_QueueURL, true, true);
            System.out.println("Manager received " + workerMessages.size() + " Messages from W2M Queue");

            for (Message workerMsg : workerMessages) {

                // parse json
                JSONObject msgObj= Constants.validateMessageAndReturnObj(workerMsg , Constants.TAGS.WORKER_2_MANAGER, true);
                if (msgObj == null)
                    continue;
                String inBucket = (String) msgObj.get(Constants.IN_BUCKET);
                String inKey = (String) msgObj.get(Constants.IN_KEY);

                ClientInfo clientInfo = clientsInfo.get(inBucket);
                if(clientInfo == null)
                    continue;

                boolean isUpdated = clientInfo.updateLocalOutputFile(inBucket,inKey, msgObj.toJSONString());
                if (isUpdated) {

                    // check if there are more reviews for this file
                   long reviewsLeft = clientInfo.decOutputCounter(inKey);
                   if (reviewsLeft == 0){
                       String outKey = clientInfo.getOutKey(inKey);
                       s3.uploadLocalToS3(inBucket, clientInfo.getLocalFileName(inBucket,inKey), outKey);
                      clientInfo.deleteLocalFile(inBucket, inKey);

                      // check if there are no more files for this client
                      int outputFilesLeft = clientInfo.decOutputFiles();
                      if (outputFilesLeft == 0){
                          sqs.sendMessage(M2C_QueueURL,
                                  new Manager2Client(true, inBucket )
                                          .stringifyUsingJSON());

                          clientsInfo.remove(inBucket);
                          removeWorkersIfNeeded(clientInfo);
                      }
                   }
                }
            }

            //delete received messages
            if(!workerMessages.isEmpty())
                sqs.deleteMessage(workerMessages, W2M_QueueURL);
        }

    }

    private void removeWorkersIfNeeded(ClientInfo clientInfo) {
        // tell the manager there is one less client to serve
        synchronized (waitingObject) {

            int numWorkerToRemove = 0;
            boolean removeExtra = false;
            if(clientsCount.get() % 3 == 0){
              extraWorkersCount.decrementAndGet();
              removeExtra = true;
              numWorkerToRemove = 1;
            }

            clientsCount.decrementAndGet();

            // terminate unneeded workers
            long workersPerClient = clientInfo.getTotalReviews() / clientInfo.getReviewsPerWorker();
            maxWorkersPerClient.remove((int)workersPerClient);
            Integer currMax = maxWorkersPerClient.peek();
            if (currMax==null){
                waitingObject.notifyAll();
                return;
            }

            numWorkerToRemove += workersCount.get() - currMax;
            if (numWorkerToRemove <= 0 && removeExtra){
                numWorkerToRemove = 1;
            }

            if (numWorkerToRemove > 0) {
                List<Instance> instances = ec2.listInstances(false);
                for (Instance instance: instances) {
                    for (Tag tag: instance.getTags()) {
                        if (tag.getValue().equals(Constants.INSTANCE_TAG.TAG_WORKER.toString())
                                && instance.getState().getName().equals("running")) {
                            ec2.terminateEC2Instance(instance.getInstanceId());
                            numWorkerToRemove --;
                        }
                    }
                    if (numWorkerToRemove == 0)
                        break;
                }
            }
            waitingObject.notifyAll();
        }
    }

}
