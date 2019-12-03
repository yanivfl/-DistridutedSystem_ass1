package apps;
import com.amazonaws.services.sqs.model.Message;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;
import handlers.SentimentAnalysisHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
    After the manger receives response messages from the workers on all the files on an input file, then it:
        Creates a summary output file accordingly,
        Uploads the output file to S3,
        Sends a message to the application with the location of the file.
*/

public class ManageWorkers implements Runnable {
    private ConcurrentMap<String, ClientInfo> clientsInfo;
    private EC2Handler ec2;
    private S3Handler s3;
    private SQSHandler sqs;

    public ManageWorkers(ConcurrentMap<String, ClientInfo> clientsInfo, EC2Handler ec2, S3Handler s3, SQSHandler sqs) {
        this.clientsInfo = clientsInfo;
        this.ec2 = ec2;
        this.s3 = s3;
        this.sqs = sqs;
    }

    @Override
    public void run() {
        JSONParser jsonParser = new JSONParser();

        // Get the (Worker -> Manager) ( Manager -> Clients) SQS queues URLs
        String W2M_QueueURL = sqs.getURL(Constants.WORKERS_TO_MANAGER_QUEUE);
        String M2C_QueueURL = sqs.getURL(Constants.MANAGER_TO_CLIENTS_QUEUE);

        while(true){
            List<Message> workerMessages = sqs.receiveMessages(W2M_QueueURL, false, false);
            System.out.println("Manager recieved " + workerMessages.size() + " Messages from W2M Queue");
            for (Message workerMsg : workerMessages) {
                JSONObject msgObj= Constants.validateMessageAndReturnObj(workerMsg , Constants.TAGS.WORKER_2_MANAGER);
                if(msgObj==null)
                    continue;




            }

            //delete recieved messages
            if(!workerMessages.isEmpty())
                sqs.deleteMessage(workerMessages, W2M_QueueURL);

        }

    }


}
