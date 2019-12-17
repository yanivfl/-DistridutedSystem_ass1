package messages;


import apps.Constants;
import org.json.simple.JSONObject;

public class Client2Manager_terminate extends Base {

    private Constants.TAGS tag;
    private String senderBucket;

    /** Normal constructor */
    public Client2Manager_terminate(String senderID) {
        this.tag = Constants.TAGS.CLIENT_2_MANAGER_terminate;
        this.senderBucket = senderID;
    }

    public String getSenderBucket() {
        return senderBucket;
    }

    /** Turns the Client2Manager_terminate to string */
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put(Constants.TAG, this.tag.toString());
        obj.put(Constants.SENDER_BUCKET, this.senderBucket);
        return obj.toJSONString();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return "Client2Manager_terminate{" +
                ", senderID=" + senderBucket +
                '}';
    }
}
