package messages;

import apps.Constants;
import org.json.simple.JSONObject;

public class Manager2Client extends Base {

    private Constants.TAGS tag;
    String inBucket;
    boolean isDone;

    /** Normal constructor */
    public Manager2Client(boolean isDone, String bucketName) {
        this.tag = Constants.TAGS.MANAGER_2_CLIENT;
        this.isDone = isDone;
        this.inBucket = bucketName;
    }


    /** Turns the Manager2Client to string */
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put(Constants.TAG, this.tag.toString());
        obj.put(Constants.IS_DONE, this.isDone);
        obj.put(Constants.IN_BUCKET, this.inBucket);
        return obj.toJSONString();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return "Manager2Client{" +
                "tag=" + tag +
                ", inBucket='" + inBucket + '\'' +
                ", isDone=" + isDone +
                '}';
    }
}
