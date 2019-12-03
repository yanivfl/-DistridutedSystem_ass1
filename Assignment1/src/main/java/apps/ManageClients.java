package apps;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.concurrent.ConcurrentMap;

/**
 * Manage the different messages that comes from clients
 */


public class ManageClients implements Runnable {

    private ConcurrentMap<String, ClientInfo> clientsInfo;

    public ManageClients(ConcurrentMap<String, ClientInfo> clientInfo) {
        this.clientsInfo = clientInfo;
    }

    /**
     * 1. Downloads the input file from S3.
     * 2. Distributes the operations to be performed on the reviews to the workers using SQS queue/s.
     * 3. Checks the SQS message count and starts Worker processes (nodes) accordingly.
     */
    public void inputFileMessege(String message, JSONObject msgObj) {
        //TODO
    }

    /**
     * Initialize this local app client in the clients info map.
     */
    public void initMessage(String message, JSONObject msgObj) {

        // parse json
        String bucket = (String) msgObj.get(Constants.BUCKET);
        long reviewsPerWorker = (Long) msgObj.get(Constants.REVIEWS_PER_WORKER);

        // add this client if it doesn't exists already
        // (if it exists then another one of it's messages arrived first and it's info is null, or this msg arrived twice)
        if (!clientsInfo.containsKey(bucket)) {
            ClientInfo clientInfo = new ClientInfo((int)reviewsPerWorker);
            clientsInfo.putIfAbsent(bucket, clientInfo);
        }
        else
        {
            if (clientsInfo.get(bucket) == null) {
                ClientInfo clientInfo = new ClientInfo((int)reviewsPerWorker);
                clientsInfo.replace(bucket, clientInfo);
            }
        }
    }

    /**
     * 1. Does not accept any more input files from local applications.
     * 2. Serve the local application that sent the termination message.
     * 3. Waits for all the workers to finish their job, and then terminates them.
     * 4. Creates response messages for the jobs, if needed.
     * 5. Terminates.
     */
    public void terminateMessege(String message, JSONObject msgObj) {
        //TODO
    }

    public void messageHandler(String message) {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject msgObj = (JSONObject) jsonParser.parse(message);
            if (Constants.TAGS.valueOf((String) msgObj.get(Constants.TAG)) == Constants.TAGS.CLIENT_2_MANAGER_init){
                initMessage(message, msgObj);
            }
            else {
                if (Constants.TAGS.valueOf((String) msgObj.get(Constants.TAG)) == Constants.TAGS.CLIENT_2_MANAGER) {
                    System.out.println("Got an unexpected message");
                    inputFileMessege(message, msgObj);
                }
                else {
                    if (Constants.TAGS.valueOf((String) msgObj.get(Constants.TAG)) == Constants.TAGS.CLIENT_2_MANAGER_terminate) {

                        terminateMessege(message, msgObj);
                    }
                    else {
                        System.out.println("Got an unexpected message");
                    }
                }
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {


        // TODO: change this
        String message = "";
        messageHandler(message);

    }
}
