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

        this.outputFilesCounter.set(numFiles);
        this.reviewsPerWorker = reviewsPerWorker;
        this.in2outMap = new ConcurrentHashMap<>();
    }

    public String getLocalFileName(String inBucket, String outkey){
        return inBucket + "_" + outkey;
    }

    private ReentrantLock getLockForInputKey(String inputKey) {
        return (ReentrantLock) in2outMap.get(inputKey).get(Constants.LOCK);
    }

    private boolean isNewMessage(String localFileName, String msg) throws IOException {
        if(! new File(localFileName).isFile()){
            return true;
        }

        BufferedReader outputfileReader = new BufferedReader(new FileReader(localFileName));;
        while (true) {
            String line = outputfileReader.readLine();
            if (line.equals(msg)){
                System.out.println("msg already exists in file");
                return false;
            }
            if (line == null) return true;
        }
    }

    private void appendToLocalFile(String localFileName, String msg){
        try(FileWriter fw = new FileWriter(localFileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(msg);
        } catch (IOException e) {
            throw new RuntimeException("couldn't write \n" + msg + "\n to localFileName " + localFileName + "\n with exception" + e);
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
            String outkey = (String) in2outMap.get(inputKey).get(Constants.OUTPUT_KEY);
            String localFileName = getLocalFileName(inputBucket,outkey);
            if (isNewMessage(localFileName, msg)){
                appendToLocalFile(localFileName, msg);
                isUpdated = true;
            }

        } finally {
            getLockForInputKey(inputKey).unlock();
            return isUpdated;
        }
    }

//    public long incOutputCounter(String inputKey) {
//        if(getLockForInputKey(inputKey).isHeldByCurrentThread()){
//            System.out.println("Entering incOutputCounter with lock");
//            getLockForInputKey(inputKey).unlock();
//        }
//        long newCounter = -1; //default non zero value
//        getLockForInputKey(inputKey).lock();
//        try {
//            newCounter = (Long) in2outMap.get(inputKey).get(Constants.COUNTER) +1;
//            in2outMap.get(inputKey).put(Constants.COUNTER, newCounter);
//        } finally {
//            getLockForInputKey(inputKey).unlock();
//            return newCounter ;
//        }
//
//    }

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

    public int incOutputFiles() {
        return outputFilesCounter.incrementAndGet();
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
}
