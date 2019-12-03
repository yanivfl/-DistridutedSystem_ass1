import apps.ClientInfo;
import apps.Constants;
import apps.ManageWorkers;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.Message;
import handlers.EC2Handler;
import handlers.SQSHandler;
import messages.Manager2Worker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ManageWorkersTest {

    public static void main(String[] args) {
        ConcurrentMap<String, ClientInfo> clientsInfo = new ConcurrentHashMap<>();
        clientsInfo.put("bucket_1", new ClientInfo(5, 5));


//        Runnable manageWorkers = new ManageWorkers(clientsInfo);
//        Thread t1 = new Thread(manageWorkers);
//        t1.start();
    }
}




