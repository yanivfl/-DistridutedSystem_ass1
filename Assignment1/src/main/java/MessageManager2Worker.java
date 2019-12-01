import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.UUID;

public class MessageManager2Worker extends MessageBase {

    private Constants.TAGS tag;
    private String inBucket;
    private String inKey;
    private long line;

    /** Normal constructor */
    public MessageManager2Worker(String inBucket, String inKey, String outBucket, long line) {
        this.tag = Constants.TAGS.MANAGER_2_WORKER;
        this.inBucket = inBucket;
        this.inKey = inKey;
        this.line = line;
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
        return "MessageManager2Worker{" +
                "inBucket='" + inBucket + '\'' +
                ", inKey='" + inKey + '\'' +
                ", line=" + line +
                '}';
    }
}
