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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FullTest {

    public static void main(String[] args) {
        String[] localArgs;
        Constants.IS_MANAGER_ON = new AtomicBoolean(false);
        try {
            //test1();
            test2();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void test1() throws InterruptedException {
        String[] localArgs;
        for (int i = 0; i <3 ; i++) {
            localArgs = getARgs1(i+1);
            Runnable client = new RunnableLocalApp(localArgs);
            Thread clientThread = new Thread(client);
            clientThread.setName("Local-App-Thread");
            if (i==2)
                Thread.sleep(120000); //sleep for 120 seconds
            clientThread.start();
            System.out.println("client " + (i+1) + " started");
        }
    }

    private static void test2(){
        String[] localArgs = getARgs2();
        Runnable client = new RunnableLocalApp(localArgs);
        Thread clientThread = new Thread(client);
        clientThread.setName("Local-App-Thread");
        clientThread.start();
        System.out.println("client started");
    }


    private static String[] getARgs1(int num){
        String[] localArgs;
        switch (num){
            case 1:
                localArgs = new String[]{"/home/yaniv/workSpace/dsps/reviews/test_json", "output_1", "5"};
                return localArgs;
            case 2:
                localArgs = new String[]{"/home/yaniv/workSpace/dsps/reviews/test_json", "output_terminate", "10", "terminate"};
                return localArgs;
            case 3:
            localArgs = new String[]{"/home/yaniv/workSpace/dsps/reviews/test_json", "output_2", "10"};
            return localArgs;

            default:
                return new String[0];
        }
    }

    private static String[] getARgs2(){
        String[] localArgs = {"/home/yaniv/workSpace/dsps/reviews/B0047E0EII",
        "/home/yaniv/workSpace/dsps/reviews/B01LYRCIPG",
        "output1.html",
        "output2.html",
        "100" };
        return localArgs;
    }
}




