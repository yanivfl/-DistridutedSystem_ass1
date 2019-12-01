import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MessageDone  extends MessageBase {

    private boolean done;

    /** Normal constructor */
    public MessageDone(boolean done) {
        this.done = done;
    }

    /** Unique constructor - turn the string to MessageDone (assumes the msg was JSON stringify */
    public MessageDone(String msg) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(msg);
        this.done = (Boolean) obj.get("done");
    }

    /** Turns the MessageLocation to string */
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put("done", this.done);
        return obj.toJSONString();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return "MessageDone {" +
                "done=" + done +
                '}';
    }
}
