package messages;

import apps.Constants;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Client2Manager_init extends Base {

    private Constants.TAGS tag;
    private String bucket;
    private long reviewsPerWorker;

    /** Normal constructor */
    public Client2Manager_init(String bucket, long reviewsPerWorker) {
        this.tag = Constants.TAGS.CLIENT_2_MANAGER_init;
        this.bucket = bucket;
        this.reviewsPerWorker = reviewsPerWorker;
    }

    /** Unique constructor - turn the string to Client2Manager_init (assumes the msg was JSON stringify) */
    public Client2Manager_init(String msg) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(msg);

        this.tag = Constants.TAGS.valueOf((String) obj.get("tag"));
        if (this.tag != Constants.TAGS.CLIENT_2_MANAGER_init)
            throw new RuntimeException("Got an unexpected message");

        this.bucket = (String) obj.get("bucket");
        this.reviewsPerWorker = (Long) obj.get("reviewsPerWorker");
    }

    /** Turns the Client2Manager_init to string */
    public String stringifyUsingJSON() {
        JSONObject obj = new JSONObject();
        obj.put("tag", this.tag.toString());
        obj.put("bucket", this.bucket);
        obj.put("reviewsPerWorker", this.reviewsPerWorker);
        return obj.toJSONString();
    }

    /** This is for debug purpose */
    @Override
    public String toString() {
        return "Client2Manager_init{" +
                "bucket='" + bucket + '\'' +
                ", reviewsPerWorker='" + reviewsPerWorker + '\'' +
                '}';
    }
}
