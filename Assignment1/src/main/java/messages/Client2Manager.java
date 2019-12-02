package messages;

import apps.Constants;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.UUID;

public class Client2Manager extends Base {

    private Constants.TAGS tag;
    private String bucket;
    private String inKey;
    private String outKey;

    /** Normal constructor */
    public Client2Manager(String bucket, String inKey, String outKey) {
        this.tag = Constants.TAGS.CLIENT_2_MANAGER;
        this.bucket = bucket;
        this.inKey = inKey;
        this.outKey = outKey;
    }

    /** Unique constructor - turn the string to Client2Manager (assumes the msg was JSON stringify) */
    public Client2Manager(String msg) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(msg);

        this.tag = Constants.TAGS.valueOf((String) obj.get("tag"));
        if (this.tag != Constants.TAGS.CLIENT_2_MANAGER)
            throw new RuntimeException("Got an unexpected message");

        this.bucket = (String) obj.get("bucket");
        this.inKey = (String) obj.get("inKey");
        this.outKey = (String) obj.get("outKey");
    }

    /** Turns the Client2Manager to string */
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put("tag", this.tag.toString());
        obj.put("bucket", this.bucket);
        obj.put("inKey", this.inKey);
        obj.put("outKey", this.outKey);
        return obj.toJSONString();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return "Client2Manager{" +
                "bucket='" + bucket + '\'' +
                ", inKey='" + inKey + '\'' +
                ", outKey='" + outKey + '\'' +
                '}';
    }
}
