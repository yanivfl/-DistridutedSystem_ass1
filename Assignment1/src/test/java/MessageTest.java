import messages.MessageBase;
import messages.MessageClientToManager;
import messages.MessageManagerToClient;
import org.json.simple.parser.ParseException;

import java.util.UUID;

public class MessageTest {

    public static void main(String[] args) throws ParseException {


        UUID id = UUID.randomUUID();
        MessageBase msgLocation = new MessageClientToManager("1", "2", "3", "4", 5, true, id);
        MessageBase msgDone = new MessageManagerToClient(true, id);

        checkJSON(msgLocation);
        checkJSON(msgDone);


    }

    public static void checkJSON(MessageBase msg) throws ParseException {

        String msgStr = msg.stringifyUsingJSON();
        MessageBase parsedStr;

        if (msg instanceof MessageClientToManager) {
            parsedStr = new MessageClientToManager(msgStr);
        }
        else {
            parsedStr = new MessageManagerToClient(msgStr);
        }

        System.out.println(parsedStr);

    }
}
