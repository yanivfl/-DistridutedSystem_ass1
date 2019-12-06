import apps.ClientInfo;
import apps.Constants;
import apps.ManageWorkers;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.Message;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;
import messages.Manager2Worker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ManageWorkersTest {

    public static void main(String[] args) throws IOException, ParseException {

        if (args.length < 1) {
            System.out.println("To activate this test, put file path as first argument. Activate Worker as well");
            System.out.println("for example: /home/yaniv/workSpace/dsps/reviews/test_json");
            return;
        }
        String fileName = args[0];
//        clientInfoTest(fileName);

        EC2Handler ec2 = new EC2Handler();
        System.out.println("connected to EC2");

        SQSHandler sqs = new SQSHandler(ec2.getCredentials());
        System.out.println("connected to sqs");

        S3Handler s3 = new S3Handler(ec2);
        System.out.println("connected to s3");

        String W2M_QueueURL = null;
        String M2C_QueueURL = null;
        String M2W_QueueURL = null;
        Object waitingObject = new Object();
        AtomicInteger a = new AtomicInteger(1);
        AtomicInteger b = new AtomicInteger(1);
        AtomicInteger c = new AtomicInteger(0);
        AtomicInteger d= new AtomicInteger(1);

        PriorityQueue<Integer> pq = new PriorityQueue<>();

        try {
            ConcurrentMap<String, ClientInfo> clientsInfo = new ConcurrentHashMap<>();
            Runnable manageWorkers = new ManageWorkers(clientsInfo, a,b,c,pq, waitingObject , ec2, s3, sqs);
            Thread manager_thread = new Thread(manageWorkers);
            Runnable worker = new RunnableWorker();
            Thread worker_thread = new Thread(worker);
            System.out.println("Created Threads");

            M2W_QueueURL = sqs.createSQSQueue(Constants.MANAGER_TO_WORKERS_QUEUE, false);
            M2C_QueueURL = sqs.createSQSQueue(Constants.MANAGER_TO_CLIENTS_QUEUE, false);
            W2M_QueueURL = sqs.createSQSQueue(Constants.WORKERS_TO_MANAGER_QUEUE, false);
            String my_bucket = s3.createBucket("myBucket");
            String inKey = s3.getAwsFileName("inKey");
            String outKey = s3.getAwsFileName("outKey");
            System.out.println("Created Queues and bucket");



            long num_of_reviews = 0;

            //send messages to workers
            JSONParser parser = new JSONParser();
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            while (line != null) {
                Object obj = parser.parse(line);
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray reviewsList = (JSONArray) jsonObject.get(Constants.REVIEWS);
                for (Object review : reviewsList) {
                    System.out.println("sending Message #" + (num_of_reviews+1));
                    JSONObject jsonReview = (JSONObject) review;
                    String text = (String) jsonReview.get(Constants.TEXT);
                    int rating = ((Long) jsonReview.get(Constants.RATING)).intValue();
                    sqs.sendMessage(M2W_QueueURL, new Manager2Worker(
                            my_bucket,
                            inKey,
                            text,
                            rating)
                            .stringifyUsingJSON());
                    num_of_reviews++;
                }

                // read next line
                line = reader.readLine();
            }
            reader.close();
            System.out.println("sent " + num_of_reviews + " messages");

            clientsInfo.put(my_bucket, new ClientInfo(1, 1));
            ClientInfo clientInfo = clientsInfo.get(my_bucket);
            clientsInfo.get(my_bucket).putOutputKey(inKey, outKey, num_of_reviews);


            manager_thread.start();
            worker_thread.start();
            System.out.println("threads started running");



            List<Message> managerMessages= sqs.receiveMessages(M2C_QueueURL, false, true);
            while(managerMessages.isEmpty()) {
                System.out.println("test recieved " + managerMessages.size() + " Messages");
                managerMessages= sqs.receiveMessages(M2C_QueueURL, false, true);
            }

            System.out.println("Manager sent to Client the following message");
            for (Message managerMsg : managerMessages) {
                    System.out.println(managerMsg.getBody());
            }

            sqs.deleteMessage(managerMessages, M2C_QueueURL);

            s3.displayFile(my_bucket, outKey);
            s3.deleteBucket(my_bucket);




//        } catch (InterruptedException consumed) {
//            System.out.println("thread exited");
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (Exception ex) {
            System.out.println("Exception handled " + ex);
        } finally {
            String[] deleteArgs = {"1","2","3"};
                AWSResourceTest.main(deleteArgs);
            }
        }




    public static void clientInfoTest(String fileName) throws IOException, ParseException {
        ConcurrentMap<String, ClientInfo> clientsInfo = new ConcurrentHashMap<>();
        clientsInfo.put("myBucket", new ClientInfo(1, 1));
        ClientInfo clientInfo = clientsInfo.get("myBucket");
        clientInfo.putOutputKey("inKey", "outKey", 10);

        long num_of_reviews = 0;

        JSONParser parser = new JSONParser();
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(fileName));
        String line = reader.readLine();
        while (line != null) {
            Object obj = parser.parse(line);
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray reviewsList = (JSONArray) jsonObject.get(Constants.REVIEWS);
            for (Object review : reviewsList) {
                System.out.println("writing review #" + num_of_reviews);
                JSONObject jsonReview = (JSONObject) review;
                String msg = jsonReview.toJSONString();
                System.out.println("review is: "+ msg);
                clientInfo.updateLocalOutputFile("myBucket", "inKey", msg);
                num_of_reviews++;
            }
            line = reader.readLine();
        }

    }

}





