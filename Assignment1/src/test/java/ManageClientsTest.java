import apps.ClientInfo;
import apps.Constants;
import apps.ManageClients;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;
import messages.Client2Manager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ManageClientsTest {

    private static ConcurrentMap<String, ClientInfo> clientsInfo;
    private static AtomicInteger workersCount;
    private static ReentrantLock workersCountLock;
    private static AtomicBoolean terminate;
    private static EC2Handler ec2;
    private static S3Handler s3;
    private static SQSHandler sqs;
    private static String C2M_QueueURL;
    private static String M2C_QueueURL;
    private static String M2W_QueueURL;

    /** Take a local small json file and parse it to reviews */
    private static void testParsing(String jsonPath) throws FileNotFoundException, ParseException {

        BufferedReader objReader = new BufferedReader(new FileReader(jsonPath));
        JSONParser jsonParser = new JSONParser();

        String file = objReader.lines().collect(Collectors.joining());

        JSONObject jsonObject = (JSONObject) jsonParser.parse(file);

        JSONArray reviewsArray = (JSONArray) jsonObject.get(Constants.REVIEWS);
        for (Object obj: reviewsArray) {
            JSONObject singleReview = (JSONObject) obj;
            String text = (String) singleReview.get(Constants.TEXT);
            int rating = ((Long) singleReview.get(Constants.RATING)).intValue();

            System.out.println("text: " + text);
            System.out.println("rating: " + rating);
        }
    }

    private static void test() {
        String jsonPath1 = "/Users/Yuval/Desktop/מבוזרות/DistridutedSystem_ass1/Assignment1/json_tests/test_json1.json";
        String jsonPath2 = "/Users/Yuval/Desktop/מבוזרות/DistridutedSystem_ass1/Assignment1/json_tests/test_json2.json";

        // create manager parameters - as the Manager would do
        clientsInfo = new ConcurrentHashMap<>();
        workersCount = new AtomicInteger(0);
        workersCountLock = new ReentrantLock();
        terminate = new AtomicBoolean(false);
        String myBucket = null;
        String keyJson1 = null;
        String keyJson2 = null;

        try {
            // create a bucket and 2 input files on s3 - as the client would do
            UUID appID = UUID.randomUUID();
            myBucket = s3.createBucket(appID.toString());
            keyJson1 = s3.uploadFileToS3(myBucket, jsonPath1);
            keyJson2 = s3.uploadFileToS3(myBucket, jsonPath2);

            // Send 2 messages to the (Clients -> apps.Manager) SQS queue, stating the location of the files on S3
            Client2Manager messageClientToManager;
            messageClientToManager = new Client2Manager(myBucket, keyJson1, "none", 1, 2);
            sqs.sendMessage(C2M_QueueURL, messageClientToManager.stringifyUsingJSON());

            messageClientToManager = new Client2Manager(myBucket, keyJson2, "none", 1, 2);
            sqs.sendMessage(C2M_QueueURL, messageClientToManager.stringifyUsingJSON());

            // Create manageClients Runnable
            Runnable manageClients = new ManageClients(clientsInfo, new AtomicInteger(0), workersCount, new AtomicInteger(0), new PriorityQueue<Integer>(), terminate, null, ec2, s3, sqs); //TODO: this is wrong
            Thread t1 = new Thread(manageClients);
            t1.start();
            t1.join();

            System.out.println("\n\n**** Test results: ****");
            System.out.println("* Clients info:");
            System.out.println(clientsInfo);


        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            s3.deleteFile(myBucket, keyJson1);
            s3.deleteFile(myBucket, keyJson2);
            s3.deleteBucket(myBucket);
        }
    }

    private static void testInputFileMessage() throws IOException, ParseException {

        String jsonPath1 = "/Users/Yuval/Desktop/מבוזרות/DistridutedSystem_ass1/Assignment1/json_tests/test_json1.json";
        String jsonPath2 = "/Users/Yuval/Desktop/מבוזרות/DistridutedSystem_ass1/Assignment1/json_tests/test_json2.json";

        // create manager parameters - as the Manager would do
        clientsInfo = new ConcurrentHashMap<>();
        workersCount = new AtomicInteger(5);
        workersCountLock = new ReentrantLock();
        terminate = new AtomicBoolean(false);
        String myBucket = null;
        String keyJson1 = null;
        String keyJson2 = null;

        try {
            // create a bucket and 2 input files on s3 - as the client would do
            UUID appID = UUID.randomUUID();
            myBucket = s3.createBucket(appID.toString());
            keyJson1 = s3.uploadFileToS3(myBucket, jsonPath1);
            keyJson2 = s3.uploadFileToS3(myBucket, jsonPath2);

            // Send 2 messages to the (Clients -> apps.Manager) SQS queue, stating the location of the files on S3
            Client2Manager messageClientToManager1 = new Client2Manager(myBucket, keyJson1, "none", 1, 2);
            sqs.sendMessage(C2M_QueueURL, messageClientToManager1.stringifyUsingJSON());

            Client2Manager messageClientToManager2 = new Client2Manager(myBucket, keyJson2, "none", 1, 2);
            sqs.sendMessage(C2M_QueueURL, messageClientToManager2.stringifyUsingJSON());

            // Create manageClients Runnable
            ManageClients manageClients = new ManageClients(clientsInfo, new AtomicInteger(0), workersCount, new AtomicInteger(0), new PriorityQueue<Integer>(), terminate, null, ec2, s3, sqs);    // TODO: this is wrong

            JSONParser jsonParser = new JSONParser();
            JSONObject msgObj;
            msgObj = (JSONObject) jsonParser.parse(messageClientToManager1.stringifyUsingJSON());
            manageClients.inputFileMessage(msgObj);

            msgObj = (JSONObject) jsonParser.parse(messageClientToManager2.stringifyUsingJSON());
            manageClients.inputFileMessage(msgObj);

            System.out.println("\n\n**** Test results: Clients info ****");
            for (Map.Entry<String, ClientInfo> entry : clientsInfo.entrySet()) {
                System.out.println("Client bucket = "+ entry.getKey() + ":\n" + entry.getValue() + "\n");
            }
            System.out.println();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            s3.deleteFile(myBucket, keyJson1);
            s3.deleteFile(myBucket, keyJson2);
            s3.deleteBucket(myBucket);
        }

    }

    public static void main(String[] args) throws InterruptedException, IOException, ParseException {

        // initial configurations
        ec2 = new EC2Handler(true);
        s3 = new S3Handler(ec2);
        sqs = new SQSHandler(ec2.getCredentials());

        // Start queues
        sqs.createSQSQueue(Constants.CLIENTS_TO_MANAGER_QUEUE, true);
        sqs.createSQSQueue(Constants.MANAGER_TO_CLIENTS_QUEUE, true);
        sqs.createSQSQueue(Constants.MANAGER_TO_WORKERS_QUEUE, true);

        try {
            // Get the (Clients -> Manager), (Manager -> Clients) SQS queues URLs
            C2M_QueueURL = sqs.getURL(Constants.CLIENTS_TO_MANAGER_QUEUE);
            M2C_QueueURL = sqs.getURL(Constants.MANAGER_TO_CLIENTS_QUEUE);
            M2W_QueueURL = sqs.getURL(Constants.MANAGER_TO_WORKERS_QUEUE);

            testInputFileMessage();
        }
        finally {
            // delete queues
            sqs.deleteQueue(C2M_QueueURL);
            sqs.deleteQueue(M2C_QueueURL);
            sqs.deleteQueue(M2W_QueueURL);
        }



        //        testParsing(jsonPath1);




//        clientsInfo.put("bucket_1", new ClientInfo(5));






    }


}

