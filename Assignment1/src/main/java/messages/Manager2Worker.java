package messages;

import apps.Constants;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Manager2Worker extends Base {

    private Constants.TAGS tag;
    private String inBucket;
    private String inKey;
    private String review;
    private int rating;

    /** Normal constructor */
    public Manager2Worker(String inBucket, String inKey, String review, int rating) {
        this.tag = Constants.TAGS.MANAGER_2_WORKER;
        this.inBucket = inBucket;
        this.inKey = inKey;
        this.review = review;
        this.rating = rating;
    }

    /** Turns the MessageLocation to string */
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put(Constants.TAG, this.tag.toString());
        obj.put(Constants.IN_BUCKET, this.inBucket);
        obj.put(Constants.IN_KEY, this.inKey);
        obj.put(Constants.REVIEW, this.review);
        obj.put(Constants.RATING, this.rating);
        return obj.toJSONString();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return "Messages.MessageManager2Worker{" +
                "inBucket='" + inBucket + '\'' +
                ", inKey='" + inKey + '\'' +
                ", rating='" + rating + '\'' +
                ", review=" + review +
                '}';
    }
}
