package apps;

import com.amazonaws.services.sqs.model.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Constants {

    public static final String MANAGER_ROLE = "EC2_S3_SQS_role";
    public static final String WORKERS_ROLE = "SQS_role";

    public static final int ADD_EXTRA_WORKER = 3;
    public static final String AMI = "ami-b66ed3de";
    public static final String TAG = "tag";
    public static final String SENDER_BUCKET = "senderBucket";
    public static final String REVIEWS= "reviews";
    public static final String REVIEW= "review";
    public static final String TEXT= "text";
    public static final String IN_BUCKET= "inBucket";
    public static final String IN_KEY= "inKey";
    public static final String SENTIMENT= "sentiment";
    public static final String ENTITIES= "entities";
    public static final String IS_SARCASTIC= "isSarcastic";
    public static final String RATING= "rating";
    public static final String BUCKET= "bucket";
    public static final String TERMINATE= "terminate";
    public static final String REVIEWS_PER_WORKER= "reviewsPerWorker";
    public static final String NUM_FILES= "numFiles";
    public static final String IS_DONE = "isDone";
    public static final String OUT_KEY = "outKey";
    public static final String COUNTER = "counter";
    public static final String LOCK = "lock";

    public static final String LOCAL = "local";
    public static final String REMOTE = "remote";

    public static final String CLIENTS_TO_MANAGER_QUEUE= "Clients2ManagerQueue";
    public static final String MANAGER_TO_CLIENTS_QUEUE= "Manager2ClientsQueue";
    public static final String WORKERS_TO_MANAGER_QUEUE = "Workers2ManagerQueue";
    public static final String MANAGER_TO_WORKERS_QUEUE = "Manager2WorkersQueue";

    public static final String SQSpolicyPath = "RolePolicies/SQSpolicy.json";
    public static final String EC2policyPath = "RolePolicies/EC2policy.json";
    public static final String S3policyPath = "RolePolicies/S3policy.json";
    public static final String USER_DATA_PATH = "user_data.sh";

    public enum INSTANCE_TAG {
        MANAGER, WORKER
    }


    public enum TAGS {
        CLIENT_2_MANAGER, CLIENT_2_MANAGER_terminate, MANAGER_2_CLIENT,
        MANAGER_2_WORKER, WORKER_2_MANAGER, SUMMERY_LINE
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

    /**
     * validates Message and returnes Json body
     * params: msg, tag
     * returns: Json body of Message if validation was successful
     */

    public static JSONObject validateMessageAndReturnObj(Message msg , TAGS tag, boolean printError){
        JSONParser jsonParser = new JSONParser();
        JSONObject msgObj = null;
        try {
            msgObj = (JSONObject) jsonParser.parse(msg.getBody());
        }
        catch (ParseException e) {
            System.out.println("Can't parse Message. got exception: "+ e);
            return null;
        }

        if (Constants.TAGS.valueOf((String) msgObj.get(Constants.TAG)) != tag) {
            if (printError)
                System.out.println("Got an unexpected message, should get tag " + tag.toString());
            return null;
        }
        return msgObj;
    }



    //********************************* DEBUG ***************************************
    //TODO change to false
    public static boolean DEBUG_MODE = true;




}


