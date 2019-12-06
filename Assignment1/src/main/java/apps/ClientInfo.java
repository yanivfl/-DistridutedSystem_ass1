package apps;


import edu.stanford.nlp.util.Pair;
import handlers.S3Handler;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ClientInfo {

    // the map is build from: <input key, <output key, count done reviews in this file>>
    private ConcurrentMap<String, Map <String, Object>>  in2outMap;
    private AtomicInteger outputFilesCounter;
    private int reviewsPerWorker;

    public ClientInfo(int reviewsPerWorker, int numFiles) {

        this.outputFilesCounter = new AtomicInteger(numFiles);
        this.reviewsPerWorker = reviewsPerWorker;
        this.in2outMap = new ConcurrentHashMap<>();
    }

    public String getLocalFileName(String inBucket, String inputKey){
        String outkey = getOutKey(inputKey);
        return inBucket + "_" + outkey;
    }

    public String getOutKey(String inputKey){
        return (String) in2outMap.get(inputKey).get(Constants.OUTPUT_KEY);
    }

    public boolean deleteLocalFile(String inBucket, String inputKey){
        if(getLockForInputKey(inputKey).isHeldByCurrentThread()){
            System.out.println("Entering deleteLocalFile with lock");
            getLockForInputKey(inputKey).unlock();
        }
        getLockForInputKey(inputKey).lock();
        boolean isDeleted = false;
        try {
            String localFileName = getLocalFileName(inBucket, inputKey);
            isDeleted = new File(localFileName).delete();
        } finally {
            getLockForInputKey(inputKey).unlock();
            return isDeleted  ;
        }
    }

    private ReentrantLock getLockForInputKey(String inputKey) {
        return (ReentrantLock) in2outMap.get(inputKey).get(Constants.LOCK);
    }

    private boolean isNewMessage(String localFileName, String msg) {
        System.out.println("in new message");
        try{
            if(! new File(localFileName).isFile()){
                System.out.println("File doesn't exit, return true");
                return true;
            }

            BufferedReader outputfileReader = new BufferedReader(new FileReader(localFileName));;
            while (outputfileReader.ready()) {
                String line = outputfileReader.readLine();
                if (line.equals(msg)){
                    System.out.println("msg already exists in file");
                    return false;
                }
            }
            return true;
        } catch (IOException e ){
            System.out.println("exception is: "+ e);
            return false;
        }

    }

    private void appendToLocalFile(String localFileName, String msg){
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(localFileName, true)));
            out.println(msg);
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public boolean updateLocalOutputFile(String inputBucket, String inputKey, String msg) {
        if(getLockForInputKey(inputKey).isHeldByCurrentThread()){
            System.out.println("Entering updateLocalOutputFile with lock");
            getLockForInputKey(inputKey).unlock();
        }
        boolean isUpdated = false;
        getLockForInputKey(inputKey).lock();
        try{
            String localFileName = getLocalFileName(inputBucket,inputKey);
            if (isNewMessage(localFileName, msg)){
                System.out.println("writing new message");
                appendToLocalFile(localFileName, msg);
                isUpdated = true;
            }

        } finally {
            getLockForInputKey(inputKey).unlock();
            return isUpdated;
        }
    }


    public long decOutputCounter(String inputKey) {
        if(getLockForInputKey(inputKey).isHeldByCurrentThread()){
            System.out.println("Entering decOutputCounter with lock");
            getLockForInputKey(inputKey).unlock();
        }
        long newCounter = -1; //default non zero value
        getLockForInputKey(inputKey).lock();
        try {

            // TODO: consider using replace
        newCounter = (Long) in2outMap.get(inputKey).get(Constants.COUNTER) -1;
        in2outMap.get(inputKey).put(Constants.COUNTER, newCounter);
        } finally {
            getLockForInputKey(inputKey).unlock();
            return newCounter ;
        }
    }


    public int decOutputFiles() {
        return outputFilesCounter.decrementAndGet();
    }

    public int getReviewsPerWorker() {
        return reviewsPerWorker;
    }

    public void putOutputKey(String inputKey, String outputKey, long counter) {
        Map outputDict = new HashMap<>();
        outputDict.put(Constants.OUTPUT_KEY, outputKey);
        outputDict.put(Constants.COUNTER, counter);
        outputDict.put(Constants.LOCK, new ReentrantLock());
        in2outMap.put(inputKey, outputDict);
    }

    @Override
    public String toString() {

        StringBuilder output = new StringBuilder();
        for (Map.Entry<String, Map <String, Object>> entry : in2outMap.entrySet()) {

            StringBuilder outputIn = new StringBuilder();
            for (Map.Entry<String, Object> entryIn : entry.getValue().entrySet()) {
                String pairIn = "         (" + entryIn.getKey() + ", " + entryIn.getValue() + ")\n";
                outputIn.append(pairIn);
            }

            String pair = "     * " + entry.getKey() + ":\n" + outputIn + "\n";
            output.append(pair);
        }

        return "ClientInfo{" +
                " \n    in2outMap=\n" + output +
                "    outputFilesCounter=" + outputFilesCounter +
                " \n    reviewsPerWorker=" + reviewsPerWorker +
                "\n}\n";
    }
}
