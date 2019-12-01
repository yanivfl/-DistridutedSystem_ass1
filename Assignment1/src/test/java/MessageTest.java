import messages.Base;
import messages.Client2Manager;
import messages.Manager2Client;
import org.json.simple.parser.ParseException;

import java.util.UUID;

public class MessageTest {

    public static void main(String[] args) throws ParseException {


        UUID id = UUID.randomUUID();
        Base msgLocation = new Client2Manager("1", "2", "3", "4", 5, true, id);
        Base msgDone = new Manager2Client(true, id);

        checkJSON(msgLocation);
        checkJSON(msgDone);


    }

    public static void checkJSON(Base msg) throws ParseException {

        String msgStr = msg.stringifyUsingJSON();
        Base parsedStr;

        if (msg instanceof Client2Manager) {
            parsedStr = new Client2Manager(msgStr);
        }
        else {
            parsedStr = new Manager2Client(msgStr);
        }

        System.out.println(parsedStr);

    }
}
