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

public class FullTest {

    public static void main(String[] args) {
//        String[] localArgs = {"/home/yaniv/workSpace/dsps/reviews/test_json", "output1.html", "10" };
        String[] localArgs = {"/home/yaniv/workSpace/dsps/reviews/0689835604",
                "/home/yaniv/workSpace/dsps/reviews/B001DZTJRQ",
                "output1.html",
                "output2.html",
                "100" };
        try {
            Runnable client = new RunnableLocalApp(localArgs);
            Thread clientThread = new Thread(client);
            clientThread.setName("Local-App-Thread");
            clientThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




