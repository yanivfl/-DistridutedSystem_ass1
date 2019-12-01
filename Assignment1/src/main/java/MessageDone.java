import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.UUID;

public class MessageDone  extends MessageBase {

    private boolean done;
    private UUID doneID;

    /** Normal constructor */
    public MessageDone(boolean done, UUID doneID) {
        this.done = done;
        this.doneID = doneID;
    }

    /** Unique constructor - turn the string to MessageDone (assumes the msg was JSON stringify */
    public MessageDone(String msg) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(msg);
        this.done = (Boolean) obj.get("done");
        this.doneID = UUID.fromString((String) obj.get("doneID"));
    }


    public boolean isDone() {
        return done;
    }

    public UUID getDoneID() {
        return doneID;
    }

    /** Turns the MessageLocation to string */
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put("done", this.done);
        obj.put("doneID", this.doneID);
        return obj.toJSONString();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return "MessageDone{" +
                "done=" + done +
                ", doneID=" + doneID +
                '}';
    }
}
