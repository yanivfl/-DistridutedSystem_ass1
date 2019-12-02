package messages;

import apps.Constants;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class SummeryLine extends Base {

    private Constants.TAGS tag;
    private String review;
    private long sentiment;
    private String entities;
    private boolean isSarcastic;

    /** Normal constructor */
    public SummeryLine(String review, int sentiment, String entities, boolean isSarcastic) {
        this.tag = Constants.TAGS.SUMMERY_LINE;
        this.review = review;
        this.sentiment = sentiment;
        this.entities = entities;
        this.isSarcastic = isSarcastic;
    }

    /** Unique constructor - turn the string to Messages.MessageWorker2Manager (assumes the msg was JSON stringify) */
    public SummeryLine(String msg) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(msg);

        this.tag = Constants.TAGS.valueOf((String) obj.get("tag"));
        if (this.tag != Constants.TAGS.SUMMERY_LINE)
            throw new RuntimeException("Got an unexpected message");

        this.review = (String) obj.get("review");
        this.sentiment = (Long) obj.get("sentiment");;
        this.entities = (String) obj.get("entities");
        this.isSarcastic = (Boolean) obj.get("isSarcastic");
    }

    /** Turns the MessageLocation to string */
    @Override
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put(Constants.TAG, this.tag.toString());
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
                ", review=" + review + '\'' +
                ", sentiment=" + sentiment + '\'' +
                ", entities=" + entities + '\'' +
                ", isSarcastic=" + isSarcastic +
                '}';
    }

}
