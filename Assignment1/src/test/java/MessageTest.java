import messages.Base;
import messages.Client2Manager;
import messages.Client2Manager_init;
import messages.Manager2Client;
import org.json.simple.parser.ParseException;

import java.util.UUID;

public class MessageTest {

    public static void main(String[] args) throws ParseException {


        UUID id = UUID.randomUUID();
        Base client2Manager = new Client2Manager("1", "2", "4");
        Base manager2Client = new Manager2Client(true, id);
        Base client2Manager_init = new Client2Manager_init("1", false, 3);

        checkJSON(client2Manager);
        checkJSON(manager2Client);
        checkJSON(client2Manager_init);


    }

    public static void checkJSON(Base msg) throws ParseException {

        String msgStr = msg.stringifyUsingJSON();
        Base parsedStr = null;



        if (msg instanceof Client2Manager) {
            parsedStr = new Client2Manager(msgStr);
        }
        else {
            if (msg instanceof Manager2Client) {
                parsedStr = new Manager2Client(msgStr);
            }
            else {
                if (msg instanceof Client2Manager_init) {
                    parsedStr = new Client2Manager_init(msgStr);
                }
            }
        }

        System.out.println(parsedStr);

    }
}
