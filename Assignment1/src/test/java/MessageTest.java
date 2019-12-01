import messages.MessageBase;
import messages.MessageClientToManager;
import messages.MessageManagerToClient;
import org.json.simple.parser.ParseException;

import java.util.UUID;

public class MessageTest {

    public static void main(String[] args) throws ParseException {


        UUID id = UUID.randomUUID();
        MessageBase msgLocation = new MessageClient2Manager("1", "2", "3", "4", 5, true, id);
        MessageBase msgDone = new MessageManager2Client(true, id);

        checkJSON(msgLocation);
        checkJSON(msgDone);


    }

    public static void checkJSON(MessageBase msg) throws ParseException {

        String msgStr = msg.stringifyUsingJSON();
        MessageBase parsedStr;

        if (msg instanceof MessageClient2Manager) {
            parsedStr = new MessageClient2Manager(msgStr);
        }
        else {
            parsedStr = new MessageManager2Client(msgStr);
        }

        System.out.println(parsedStr);

    }
}
