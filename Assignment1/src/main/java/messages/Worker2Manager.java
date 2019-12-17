package messages;

import apps.Constants;
import org.json.simple.JSONObject;

public class Worker2Manager extends Base {

    private Constants.TAGS tag;
    private String inBucket;
    private String inKey;
    private String review;
    private int sentiment;
    private String entities;
    private boolean isSarcastic;

    /** Normal constructor */
    public Worker2Manager(String inBucket, String inKey, String review,
                          int sentiment, String entities, boolean isSarcastic) {
        this.tag = Constants.TAGS.WORKER_2_MANAGER;
        this.inBucket = inBucket;
        this.inKey = inKey;
        this.review = review;
        this.sentiment = sentiment;
        this.entities = entities;
        this.isSarcastic = isSarcastic;
    }

    /** Turns the MessageLocation to string */
    @Override
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put(Constants.TAG, this.tag.toString());
        obj.put(Constants.IN_BUCKET, this.inBucket);
        obj.put(Constants.IN_KEY, this.inKey);
        obj.put(Constants.REVIEW, this.review);
        obj.put(Constants.SENTIMENT, this.sentiment);
        obj.put(Constants.ENTITIES, this.entities);
        obj.put(Constants.IS_SARCASTIC, this.isSarcastic);
        return obj.toJSONString();
    }

    /**
     * This is for debug purpose
     */
    @Override
    public String toString() {
        return "Messages.MessageWorker2Manager{" +
                "tag=" + tag +
                ", inBucket='" + inBucket + '\'' +
                ", inKey='" + inKey + '\'' +
                ", review=" + review + '\'' +
                ", sentiment=" + sentiment + '\'' +
                ", entities=" + entities + '\'' +
                ", isSarcastic=" + isSarcastic +
                '}';
    }

}
