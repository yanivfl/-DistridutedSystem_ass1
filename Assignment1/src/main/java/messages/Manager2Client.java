package messages;

import apps.Constants;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.UUID;

public class Manager2Client extends Base {

    private Constants.TAGS tag;
    private boolean done;
    private UUID doneID;

    /** Normal constructor */
    public Manager2Client(boolean done, UUID doneID) {
        this.tag = Constants.TAGS.MANAGER_2_CLIENT;
        this.done = done;
        this.doneID = doneID;
    }

    /** Unique constructor - turn the string to Manager2Client (assumes the msg was JSON stringify */
    public Manager2Client(String msg) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(msg);

        this.tag = Constants.TAGS.valueOf((String) obj.get("tag"));
        if (this.tag != Constants.TAGS.MANAGER_2_CLIENT)
            throw new RuntimeException("Got an unexpected message");

        this.done = (Boolean) obj.get("done");
        this.doneID = UUID.fromString((String) obj.get("doneID"));
    }


    public boolean isDone() {
        return done;
    }

    public UUID getDoneID() {
        return doneID;
    }

    /** Turns the Manager2Client to string */
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put("tag", this.tag.toString());
        obj.put("done", this.done);
        obj.put("doneID", this.doneID.toString());
        return obj.toJSONString();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return "Manager2Client{" +
                "done=" + done +
                ", doneID=" + doneID +
                '}';
    }
}
