import apps.ClientInfo;
import apps.Constants;
import apps.ManageClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class ManageClientsTest {

    public void testParsing() throws FileNotFoundException, ParseException {
        String json_path = "/Users/Yuval/Desktop/מבוזרות/test_json.json";

        BufferedReader objReader = new BufferedReader(new FileReader(json_path));
        JSONParser jsonParser = new JSONParser();

        String file = objReader.lines().collect(Collectors.joining());

        JSONObject jsonObject = (JSONObject) jsonParser.parse(file);

        JSONArray reviewsArray = (JSONArray) jsonObject.get(Constants.REVIEWS);
        for (Object obj: reviewsArray) {
            JSONObject singleReview = (JSONObject) obj;
            String text = (String) singleReview.get(Constants.TEXT);
            int rating = ((Long) singleReview.get(Constants.RATING)).intValue();

            System.out.println("text: " + text);
            System.out.println("rating: " + rating);
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException, ParseException {

//        ConcurrentMap<String, ClientInfo> clientsInfo = new ConcurrentHashMap<>();


//        clientsInfo.put("bucket_1", new ClientInfo(5));


//        Runnable manageClients = new ManageClients(clientsInfo);
//        Thread t1 = new Thread(manageClients);
//        t1.start();
//
//        t1.join();
//
//        System.out.println(clientsInfo);



    }


}

