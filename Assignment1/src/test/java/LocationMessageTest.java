import org.json.simple.parser.ParseException;

public class LocationMessageTest {

    public static void main(String[] args) throws ParseException {
        LocationMessage msg = new LocationMessage("1", "2", "3", "4", 5, true);
        String msgStr = msg.stringifyUsingJSON();
        LocationMessage parsedStr = new LocationMessage(msgStr);
        System.out.println(parsedStr);

    }
}
