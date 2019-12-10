import apps.LocalApplication;
import messages.SummeryLine;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

public class HtmlTest {



    public static void main(String[] args) throws IOException, ParseException {

        simpleTest();

    }

    private static void simpleTest() throws IOException, ParseException {

        SummeryLine summeryLine1 = new SummeryLine("review 1", 0, "[ent1;ent2;ent3]", true);
        SummeryLine summeryLine2 = new SummeryLine("review 2", 1, "[ent1;ent2;ent3]", false);
        SummeryLine summeryLine3 = new SummeryLine("review 3", 2, "[ent1;ent2;ent3]", true);
        SummeryLine summeryLine4 = new SummeryLine("review 4", 3, "[ent1;ent2;ent3]", true);
        SummeryLine summeryLine5 = new SummeryLine("review 5", 4, "[ent1;ent2;ent3]", false);

        String input = summeryLine1.stringifyUsingJSON() + "\n" +
                summeryLine2.stringifyUsingJSON() + "\n" +
                summeryLine3.stringifyUsingJSON() + "\n" +
                summeryLine4.stringifyUsingJSON() + "\n" +
                summeryLine5.stringifyUsingJSON();

        LocalApplication.createHtml(UUID.randomUUID(), "output", new ByteArrayInputStream(input.getBytes()));
    }
}
