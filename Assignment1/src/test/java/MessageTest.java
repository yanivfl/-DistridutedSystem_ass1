import org.json.simple.parser.ParseException;

public class MessageTest {

    public static void main(String[] args) throws ParseException {



        MessageBase msgLocation = new MessageLocation("1", "2", "3", "4", 5, true);
        MessageBase msgDone = new MessageDone(true);

        checkJSON(msgLocation);
        checkJSON(msgDone);


    }

    public static void checkJSON(MessageBase msg) throws ParseException {

        String msgStr = msg.stringifyUsingJSON();
        MessageBase parsedStr;

        if (msg instanceof MessageLocation) {
            parsedStr = new MessageLocation(msgStr);
        }
        else {
            parsedStr = new MessageDone(msgStr);
        }

        System.out.println(parsedStr);

    }
}
