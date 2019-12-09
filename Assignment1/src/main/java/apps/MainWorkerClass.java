package apps;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;
import handlers.SentimentAnalysisHandler;
import messages.Worker2Manager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.List;

public class MainWorkerClass {

    public static void main(String[] args) throws Exception {
        if (args.length < 1 ||  (!args[0].equals(Constants.LOCAL) && !args[0].equals(Constants.REMOTE)) ) {
            System.out.println("To activate this Worker, put  local or remote as first argument.");
            return;
        }

        boolean isClient = args[0].equals(Constants.LOCAL);

        EC2Handler ec2 = new EC2Handler(isClient);
        SQSHandler sqs = new SQSHandler(ec2.getCredentials());
        SentimentAnalysisHandler sa = new SentimentAnalysisHandler();
        JSONParser jsonParser = new JSONParser();
        String review;
        int sentiment;


        // Get the (Manager -> Worker), (Worker -> Manager) SQS queues URLs
        String M2W_QueueURL = sqs.getURL(Constants.MANAGER_TO_WORKERS_QUEUE);
        String W2M_QueueURL = sqs.getURL(Constants.WORKERS_TO_MANAGER_QUEUE);

        while(true){
            //receive reviews from Manager
            List<Message> managerMessages = sqs.receiveMessages(M2W_QueueURL, true, true);
            System.out.println("worker received " + managerMessages.size() + " Messages");
            for (Message managerMsg: managerMessages) {
                JSONObject msgObj = Constants.validateMessageAndReturnObj(managerMsg, Constants.TAGS.MANAGER_2_WORKER, true);
                if(msgObj==null)
                    continue;

                review = (String) msgObj.get(Constants.REVIEW);
                sentiment = sa.findSentiment(review);

                //send message to manager with results
                sqs.sendMessage(W2M_QueueURL,new Worker2Manager(
                                (String) msgObj.get(Constants.IN_BUCKET),
                                (String) msgObj.get(Constants.IN_KEY),
                                 review,
                                 sentiment,
                                 getEntities(sa, review),
                                 getIsSarcastic(sentiment, ((Long) msgObj.get(Constants.RATING)).intValue()))
                                .stringifyUsingJSON());
            }

            //delete received messages
            if(!managerMessages.isEmpty())
                sqs.deleteMessage(managerMessages, M2W_QueueURL);


        }

    }

    private static String getEntities(SentimentAnalysisHandler sa, String review){
        return sa
                .getListOfEntities(review)
                .toString();
    }

    /**
     * get if the review is sarcastic or not
     * @param sentiment
     * @param ratings
     * @return if rating is negative {1,2} and sentiment is positive {3,4} return true
     *         if rating is positive {4,5} and sentiment is negative {0,1} return true
     *         else return false
     */

    private static boolean getIsSarcastic(int sentiment, int ratings){
        if(  Math.abs( (sentiment +1) - ratings)  >= 2 )
            return true;
        else return false;
    }

}
