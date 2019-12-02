package apps;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Constants {

    public static final String AMI = "ami-b66ed3de";
    public static final String TAG = "tag";
    public static final String TAG_MANAGER = "TAG_MANAGER";
    public static final String TAG_WORKER = "TAG_WORKER";
    public static final String REVIEWS= "reviews";
    public static final String REVIEW= "review";
    public static final String TEXT= "text";
    public static final String IN_BUCKET= "inBucket";
    public static final String IN_KEY= "inKey";
    public static final String SENTIMENT= "sentiment";
    public static final String ENTITIES= "entities";
    public static final String IS_SARCASTIC= "isSarcastic";
    public static final String RATING= "rating";

    public static final String CLIENTS_TO_MANAGER_QUEUE_BUCKET = "ClientsToManagerQueueBucket";
    public static final String CLIENTS_TO_MANAGER_QUEUE_KEY = "ClientsToManagerQueueKey.txt";

    public static final String MANAGER_TO_CLIENTS_QUEUE_BUCKET = "ManagerToClientsQueueBucket";
    public static final String MANAGER_TO_CLIENTS_QUEUE_KEY = "ManagerToClientsQueueKey.txt";

    public static final String WORKERS_TO_MANAGER_QUEUE_BUCKET = "WorkersToManagerQueueBucket";
    public static final String WORKERS_TO_MANAGER_QUEUE_KEY = "WorkersToManagerQueueKey.txt";

    public static final String MANAGER_TO_WORKERS_QUEUE_BUCKET = "ManagerToWorkersQueueBucket";
    public static final String MANAGER_TO_WORKERS_QUEUE_KEY = "ManagerToWorkersQueueKey.txt";

    public enum TAGS {
        CLIENT_2_MANAGER, CLIENT_2_MANAGER_terminate, CLIENT_2_MANAGER_init, MANAGER_2_CLIENT,
        MANAGER_2_WORKER, WORKER_2_MANAGER, SUMMERY_LINE;
    }

    public static final String[] HTML_COLORS = new String[]{"#990000", "#e60000", "#000000", "#8cff1a", "#4d9900"};

    public static String inputStreamToString(InputStream input) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(input);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while(result != -1) {
            buf.write((byte) result);
            result = bis.read();
        }
        return buf.toString("UTF-8");
    }

}


