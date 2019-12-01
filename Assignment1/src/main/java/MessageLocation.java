import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MessageLocation extends MessageBase {

    private String inBucket;
    private String inKey;
    private String outBucket;
    private String outKey;
    private long line;
    private boolean terminate;

    /** Normal constructor */
    public MessageLocation(String inBucket, String inKey, String outBucket, String outKey, long line, boolean terminate) {
        this.inBucket = inBucket;
        this.inKey = inKey;
        this.outBucket = outBucket;
        this.outKey = outKey;
        this.line = line;
        this.terminate = terminate;
    }

    /** Unique constructor - turn the string to MessageLocation (assumes the msg was JSON stringify */
    public MessageLocation(String msg) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(msg);

        this.inBucket = (String) obj.get("inBucket");
        this.inKey = (String) obj.get("inKey");
        this.outBucket = (String) obj.get("outBucket");
        this.outKey = (String) obj.get("outKey");
        this.line = (Long) obj.get("line");
        this.terminate = (Boolean) obj.get("terminate");
    }

    /** Turns the MessageLocation to string */
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put("inBucket", this.inBucket);
        obj.put("inKey", this.inKey);
        obj.put("outBucket", this.outBucket);
        obj.put("outKey", this.outKey);
        obj.put("line", this.line);
        obj.put("terminate", this.terminate);
        return obj.toJSONString();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return "MessageLocation {" +
                "inBucket='" + inBucket + '\'' +
                ", inKey='" + inKey + '\'' +
                ", outBucket='" + outBucket + '\'' +
                ", outKey='" + outKey + '\'' +
                ", line=" + line +
                ", terminate=" + terminate +
                '}';
    }
}
