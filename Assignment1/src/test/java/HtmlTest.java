import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HtmlTest {



    public static void main(String[] args) throws IOException {

        String html = createHtml();

        String fileName = "output.html";
        File htmlFile = new File(fileName);
        Files.write(Paths.get(fileName), html.getBytes());

    }

    public static String createHtml() {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n<html>\n<head>\n<title>Page Title</title>\n</head>\n<body>\n<h1>Amazon Reviews - Sarcasm Detector</h1><ul>");



        String color="@@", review="@@", entityList="@@", true_false="@@", link="@@";

        String li =
                "<li>\n" +
                "    <span style=\"color: "+ color +"\">** "+ review +" **</span>\n" +
                "    "+ entityList +"\n" +
                "    - This is a "+ true_false +" sarcastic review.\n" +
                "    <a href=\""+ link +"\">Link</a>\n" +
                "  </li>";


        html.append(li);



        html.append("</ul>\n</body>\n</html>");
        return html.toString();
    }
}
