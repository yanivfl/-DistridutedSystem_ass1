package messages;

import apps.Constants;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Manager2Worker extends Base {

    private Constants.TAGS tag;
    private String inBucket;
    private String inKey;
    private long line;

    /** Normal constructor */
    public Manager2Worker(String inBucket, String inKey, String outBucket, long line) {
        this.tag = Constants.TAGS.MANAGER_2_WORKER;
        this.inBucket = inBucket;
        this.inKey = inKey;
        this.line = line;
    }

    /** Unique constructor - turn the string to Messages.MessageWorker2Manager (assumes the msg was JSON stringify) */
    public Manager2Worker(String msg) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(msg);

        this.tag = Constants.TAGS.valueOf((String) obj.get("tag"));
        if (this.tag != Constants.TAGS.MANAGER_2_WORKER)
            throw new RuntimeException("Got an unexpected message");

        this.inBucket = (String) obj.get("inBucket");
        this.inKey = (String) obj.get("inKey");
        this.line = (Long) obj.get("line");
    }

    /** Turns the MessageLocation to string */
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put("tag", this.tag.toString());
        obj.put("inBucket", this.inBucket);
        obj.put("inKey", this.inKey);
        obj.put("line", this.line);
        return obj.toJSONString();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return "Messages.MessageManager2Worker{" +
                "inBucket='" + inBucket + '\'' +
                ", inKey='" + inKey + '\'' +
                ", line=" + line +
                '}';
    }
}
