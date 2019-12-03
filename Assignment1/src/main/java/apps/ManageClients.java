package apps;

import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;
import messages.Manager2Client;
import messages.Manager2Worker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.plugin2.message.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Manage the different messages that comes from clients
 */


public class ManageClients implements Runnable {

    private ConcurrentMap<String, ClientInfo> clientsInfo;
    private AtomicInteger workersCount;
    private ReentrantLock workersCountLock;
    private EC2Handler ec2;
    private S3Handler s3;
    private SQSHandler sqs;

    public ManageClients(ConcurrentMap<String, ClientInfo> clientInfo, AtomicInteger workersCount,
                         ReentrantLock workersCountLock, EC2Handler ec2, S3Handler s3, SQSHandler sqs) {
        this.clientsInfo = clientInfo;
        this.workersCount = workersCount;
        this.workersCountLock = workersCountLock;
        this.ec2 = ec2;
        this.s3 = s3;
        this.sqs = sqs;
    }

    private long countReviewsPerFile(BufferedReader outputReader) throws IOException, ParseException {
        long reviewsCounter = 0;
        String line;
        while ((line = outputReader.readLine())  != null) {

            // parse the json
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(line);
            JSONArray reviewsArray = (JSONArray) jsonObject.get(Constants.REVIEWS);
            reviewsCounter += reviewsArray.size();
        }
        return reviewsCounter;
    }

    private void sendMessagesToWorkers(BufferedReader outputReader, String M2W_QueueURL, String bucket, String inKey) throws IOException, ParseException {
        String line;
        while ((line = outputReader.readLine())  != null) {

            // parse the json
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(line);

            JSONArray reviewsArray = (JSONArray) jsonObject.get(Constants.REVIEWS);
            for (Object obj: reviewsArray) {
                JSONObject singleReview = (JSONObject) obj;

                String text = (String) singleReview.get(Constants.TEXT);
                int rating = ((Long) singleReview.get(Constants.RATING)).intValue();

                // Create message to worker and add it to the queue
                Manager2Worker M2W_message = new Manager2Worker(bucket, inKey, text, rating);
                sqs.sendMessage(M2W_QueueURL, M2W_message.stringifyUsingJSON());
            }
        }
    }

    /**
     * If there is a need for more workers, initiates them.
     * (If there are k active workers, and the new job requires m workers, then the manager should create m-k new workers, if possible).
     * params: currAppReviewsPerWorker - the current local app's reviewsPerWorker parameter (taken from it's client info)
     */
    private void addWorkersIfNeeded(long currAppReviewsPerWorker, long numReviews) {
        workersCountLock.lock();
        int diff = workersCount.get() - (int)numReviews/(int)currAppReviewsPerWorker;

        // should start more [diff] workers
        if (diff < 0) {
            ec2.launchEC2Instances(diff, Constants.INSTANCE_TAG.TAG_WORKER);
        }
        workersCountLock.unlock();
    }

    /**
     * 1. Downloads the input file from S3.
     * 2. Distributes the operations to be performed on the reviews to the workers using SQS queue/s.
     * 3. Checks the SQS message count and starts Worker processes (nodes) accordingly.
     */
    public void inputFileMessage(JSONObject msgObj) throws IOException, ParseException {

        // parse json
        String bucket = (String) msgObj.get(Constants.BUCKET);
        String inKey = (String) msgObj.get(Constants.IN_KEY);
        String outKey = (String) msgObj.get(Constants.OUT_KEY);
        long reviewsPerWorker = (Long) msgObj.get(Constants.REVIEWS_PER_WORKER);
        int numFiles = ((Long) msgObj.get(Constants.NUM_FILES)).intValue();

        // Initialize this local app client in the clients info map if it wasn't initialized yet.
        // (first message initialize the ClientInfo)
        ClientInfo localApp = new ClientInfo((int)reviewsPerWorker, numFiles);
        ClientInfo tmp = clientsInfo.putIfAbsent(bucket, localApp);
        if (tmp != null) {
            localApp = tmp;
        }

        // Downloads the input file from S3.
        BufferedReader outputReader = s3.downloadFile(bucket, inKey);

        // count how many review there are in this file and update ClientInfo for this local app
        long reviewsCounter = countReviewsPerFile(outputReader);

        // update client info
        localApp.putOutputKey(inKey, outKey, reviewsCounter);

        // Downloads the input file (again) from S3.
        outputReader = s3.downloadFile(bucket, inKey);

        // Get the (Manager -> Workers) queue
        String M2W_QueueURL = sqs.getURL(Constants.MANAGER_TO_WORKERS_QUEUE);

        // For each line of the file, go through the reviews array and for each review create a message to the workers and add it to the queue
        sendMessagesToWorkers(outputReader, M2W_QueueURL, bucket, inKey);

        addWorkersIfNeeded(reviewsPerWorker, reviewsCounter);
    }

    /**
     * 1. Does not accept any more input files from local applications.
     * 2. Serve the local application that sent the termination message.
     * 3. Waits for all the workers to finish their job, and then terminates them.
     * 4. Creates response messages for the jobs, if needed.
     * 5. Terminates.
     */
    public void terminateMessage(JSONObject msgObj) {
        //TODO
    }

    public void messageHandler(String message) throws IOException {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject msgObj = (JSONObject) jsonParser.parse(message);
            if (Constants.TAGS.valueOf((String) msgObj.get(Constants.TAG)).equals(Constants.TAGS.CLIENT_2_MANAGER)) {
                System.out.println("Got an unexpected message");
                inputFileMessage(msgObj);
            }
            else {
                if (Constants.TAGS.valueOf((String) msgObj.get(Constants.TAG)).equals(Constants.TAGS.CLIENT_2_MANAGER_terminate)) {
                    terminateMessage(msgObj);
                }
                else {
                    System.out.println("Got an unexpected message");
                }
            }
        }
        catch (ParseException e) {
            System.out.println("Ignored a message");
        }
    }

    @Override
    public void run() {


//        Client2Manager_init message = new Client2Manager_init("bucket_test", 5);


        // TODO: change this
//        messageHandler(message.stringifyUsingJSON());

    }
}
