import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LocationMessage {

    private String inBucket;
    private String inKey;
    private String outBucket;
    private String outKey;
    private long line;

    /** Normal constructor */
    public LocationMessage(String inBucket, String inKey, String outBucket, String outKey, long line) {
        this.inBucket = inBucket;
        this.inKey = inKey;
        this.outBucket = outBucket;
        this.outKey = outKey;
        this.line = line;
    }

    /** Unique constructor - turn the string to LocationMessage (assumes the msg was JSON stringify */
    public LocationMessage(String msg) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(msg);

        this.inBucket = (String) obj.get("inBucket");
        this.inKey = (String) obj.get("inKey");
        this.outBucket = (String) obj.get("outBucket");
        this.outKey = (String) obj.get("outKey");
        this.line = (Long) obj.get("line");
    }

    /** Turns the LocationMessage to string */
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put("inBucket", this.inBucket);
        obj.put("inKey", this.inKey);
        obj.put("outBucket", this.outBucket);
        obj.put("outKey", this.outKey);
        obj.put("line", line);

        String str = obj.toJSONString();
        System.out.println(str);

        return obj.toJSONString();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return "LocationMessage{" +
                "inBucket='" + inBucket + '\'' +
                ", inKey='" + inKey + '\'' +
                ", outBucket='" + outBucket + '\'' +
                ", outKey='" + outKey + '\'' +
                ", line=" + line +
                '}';
    }
}
