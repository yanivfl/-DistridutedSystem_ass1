package apps;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.model.Message;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;
import handlers.SentimentAnalysisHandler;
import messages.Manager2Client;
import messages.Manager2Worker;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.List;

public class MainWorkerClass {

    public static void main(String[] args) throws Exception {
        EC2Handler ec2 = new EC2Handler();
        S3Handler s3 = new S3Handler(ec2);
        SQSHandler sqs = new SQSHandler(ec2.getCredentials());
        SentimentAnalysisHandler sa = new SentimentAnalysisHandler();
        JSONParser jsonParser = new JSONParser();
        String review;
        int sentiment;
        String entities;

        // Get the (Manager -> Worker), (Worker -> Manager) SQS queues URLs from s3
        S3Object M2W_object = s3.getS3().getObject(new GetObjectRequest(
                Constants.MANAGER_TO_WORKERS_QUEUE_BUCKET,Constants.MANAGER_TO_WORKERS_QUEUE_KEY));
        String M2W_QueueURL = Constants.inputStreamToString(M2W_object.getObjectContent());

        S3Object W2M_object = s3.getS3().getObject(new GetObjectRequest(
                Constants.WORKERS_TO_MANAGER_QUEUE_BUCKET,Constants.WORKERS_TO_MANAGER_QUEUE_KEY));
        String W2M_QueueURL = Constants.inputStreamToString(W2M_object.getObjectContent());


        while(true){
            //recieve reviews from Manager
            List<Message> managerMessages = sqs.receiveMessages(M2W_QueueURL, true, true);
            for (Message managerMsg: managerMessages) {
                JSONObject msgObj = (JSONObject) jsonParser.parse(managerMsg.getBody());
                review = (String) msgObj.get(Constants.REVIEW);
                sentiment =  sa.findSentiment(review);
                entities = getEntities(review);

            }




        }
    }


    private static String getEntities(String review){

    }

    private static boolean getIsSarcastic(){

    }

}
