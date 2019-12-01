import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.UUID;

public class MessageClientToManager extends MessageBase {

    private Constants.TAGS tag;
    private String inBucket;
    private String inKey;
    private String outBucket;
    private String outKey;
    private long line;
    private boolean terminate;
    private UUID senderID;

    /** Normal constructor */
    public MessageClientToManager(String inBucket, String inKey, String outBucket, String outKey, long line,
                                  boolean terminate, UUID senderID) {
        this.tag = Constants.TAGS.CLIENT_2_MANAGER;
        this.inBucket = inBucket;
        this.inKey = inKey;
        this.outBucket = outBucket;
        this.outKey = outKey;
        this.line = line;
        this.terminate = terminate;
        this.senderID = senderID;
    }

    /** Unique constructor - turn the string to MessageClientToManager (assumes the msg was JSON stringify) */
    public MessageClientToManager(String msg) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(msg);

        this.tag = Constants.TAGS.valueOf((String) obj.get("tag"));
        if (this.tag != Constants.TAGS.CLIENT_2_MANAGER)
            throw new RuntimeException("Got an unexpected message");

        this.inBucket = (String) obj.get("inBucket");
        this.inKey = (String) obj.get("inKey");
        this.outBucket = (String) obj.get("outBucket");
        this.outKey = (String) obj.get("outKey");
        this.line = (Long) obj.get("line");
        this.terminate = (Boolean) obj.get("terminate");
        this.senderID = UUID.fromString((String) obj.get("senderID"));
    }

    /** Turns the MessageClientToManager to string */
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put("tag", this.tag.toString());
        obj.put("inBucket", this.inBucket);
        obj.put("inKey", this.inKey);
        obj.put("outBucket", this.outBucket);
        obj.put("outKey", this.outKey);
        obj.put("line", this.line);
        obj.put("terminate", this.terminate);
        obj.put("senderID", this.senderID.toString());
        return obj.toJSONString();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return "MessageClientToManager{" +
                "inBucket='" + inBucket + '\'' +
                ", inKey='" + inKey + '\'' +
                ", outBucket='" + outBucket + '\'' +
                ", outKey='" + outKey + '\'' +
                ", line=" + line +
                ", terminate=" + terminate +
                ", senderID=" + senderID +
                '}';
    }
}
