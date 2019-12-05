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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ManageClientsTest {

    private static ConcurrentMap<String, ClientInfo> clientsInfo;
    private static AtomicInteger workersCount;
    private static ReentrantLock workersCountLock;
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

    /** Before this  test make sure not to run any real instances on amazon (workers!) */
    private static void testInputeFileMessage() {
        String jsonPath1 = "/Users/Yuval/Desktop/מבוזרות/test_json1.json";
        String jsonPath2 = "/Users/Yuval/Desktop/מבוזרות/test_json2.json";

        // create manager parameters - as the Manager would do
        clientsInfo = new ConcurrentHashMap<>();
        workersCount = new AtomicInteger(0);
        workersCountLock = new ReentrantLock();
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
            Runnable manageClients = new ManageClients(clientsInfo, workersCount, workersCountLock, ec2, s3, sqs);
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

    public static void main(String[] args) throws InterruptedException, IOException, ParseException {

        // initial configurations
        ec2 = new EC2Handler();
        s3 = new S3Handler(ec2);
        sqs = new SQSHandler(ec2.getCredentials());

        // Get the (Clients -> Manager), (Manager -> Clients) SQS queues URLs
        C2M_QueueURL = sqs.getURL(Constants.CLIENTS_TO_MANAGER_QUEUE);
        M2C_QueueURL = sqs.getURL(Constants.MANAGER_TO_CLIENTS_QUEUE);
        M2W_QueueURL = sqs.getURL(Constants.MANAGER_TO_WORKERS_QUEUE);

        testInputeFileMessage();



        //        testParsing(jsonPath1);




//        clientsInfo.put("bucket_1", new ClientInfo(5));






    }


}

