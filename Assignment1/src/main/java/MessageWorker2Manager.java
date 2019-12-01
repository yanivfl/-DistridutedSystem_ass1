import org.json.simple.JSONObject;

import java.util.List;

public class MessageWorker2Manager extends MessageManager2Worker {

    private Constants.TAGS tag;
    private String inBucket;
    private String inKey;
    private long line;
    private int sentiment;
    private List<String> entities;

    /** Normal constructor */
    public MessageWorker2Manager(String inBucket, String inKey, String outBucket, long line,
                                 int sentiment, List<String> entities) {
        super(inBucket,inKey,outBucket,line);
        this.tag = Constants.TAGS.WORKER_2_MANAGER;
        this.sentiment = sentiment;
        this.entities = entities;
    }

    /** Turns the MessageLocation to string */
    public String stringifyUsingJSON() {
        return super.stringifyUsingJSON();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return super.toString();
    }
}
