package messages;

import apps.Constants;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Worker2Manager extends Base {

    private Constants.TAGS tag;
    private String inBucket;
    private String inKey;
    private long line;
    private int sentiment;
    private String entities;
    private boolean isSarcastic;

    /** Normal constructor */
    public Worker2Manager(String inBucket, String inKey, String outBucket, long line,
                          int sentiment, String entities, boolean isSarcastic) {
        this.tag = Constants.TAGS.WORKER_2_MANAGER;
        this.inBucket = inBucket;
        this.inKey = inKey;
        this.line = line;
        this.sentiment = sentiment;
        this.entities = entities;
        this.isSarcastic = isSarcastic;
    }

    /** Unique constructor - turn the string to Messages.MessageWorker2Manager (assumes the msg was JSON stringify) */
    public Worker2Manager(String msg) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(msg);

        this.tag = Constants.TAGS.valueOf((String) obj.get("tag"));
        if (this.tag != Constants.TAGS.WORKER_2_MANAGER)
            throw new RuntimeException("Got an unexpected message");

        this.inBucket = (String) obj.get("inBucket");
        this.inKey = (String) obj.get("inKey");
        this.line = (Long) obj.get("line");
        this.sentiment = (int) obj.get("sentiment");
        this.sentiment = (int) obj.get("sentiment");
        this.entities = (String) obj.get("sentiment");
        this.isSarcastic = (Boolean) obj.get("isSarcastic");
    }




        /** Turns the MessageLocation to string */
    @Override
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put("tag", this.tag.toString());
        obj.put("inBucket", this.inBucket);
        obj.put("inKey", this.inKey);
        obj.put("line", this.line);
        obj.put("sentiment", this.sentiment);
        obj.put("entities", this.entities);
        obj.put("isSarcastic", this.isSarcastic);
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
                ", line=" + line +
                ", sentiment=" + sentiment +
                ", entities=" + entities +
                ", isSarcastic=" + isSarcastic +
                '}';
    }

}
