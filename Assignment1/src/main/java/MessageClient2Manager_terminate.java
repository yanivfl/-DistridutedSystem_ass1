import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.UUID;

public class MessageClient2Manager_terminate extends MessageBase {

    private Constants.TAGS tag;
    private UUID senderID;

    /** Normal constructor */
    public MessageClient2Manager_terminate(UUID senderID) {
        this.tag = Constants.TAGS.CLIENT_2_MANAGER_terminate;
        this.senderID = senderID;
    }

    /** Unique constructor - turn the string to MessageClient2Manager_terminate (assumes the msg was JSON stringify) */
    public MessageClient2Manager_terminate(String msg) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(msg);

        this.tag = Constants.TAGS.valueOf((String) obj.get("tag"));
        if (this.tag != Constants.TAGS.CLIENT_2_MANAGER_terminate)
            throw new RuntimeException("Got an unexpected message");

        this.senderID = UUID.fromString((String) obj.get("senderID"));
    }

    public UUID getSenderID() {
        return senderID;
    }

    /** Turns the MessageClient2Manager to string */
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put("tag", this.tag.toString());
        obj.put("senderID", this.senderID.toString());
        return obj.toJSONString();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return "MessageManager2Client{" +
                ", senderID=" + senderID +
                '}';
    }
}
