package apps;


import edu.stanford.nlp.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientInfo {

    // the map is build from: <input key, <output key, count done reviews in this file>>
    private Map<String, Pair<String, Integer>> in2outMap;
    private int outputFileCounter;
    private int reviewsPerWorker;

    public ClientInfo(int reviewsPerWorker) {

        this.outputFileCounter = 0;
        this.reviewsPerWorker = reviewsPerWorker;
        this.in2outMap = new HashMap <>();
    }

    public String getOutputKeyByInputKey(String inputKey) {
        Pair <String, Integer> outputAndCounter = in2outMap.get(inputKey);
        return outputAndCounter.first;
    }

    public int getOutputKeyCountbyInputKey(String inputKey) {
        Pair <String, Integer> outputAndCounter = in2outMap.get(inputKey);
        return outputAndCounter.second();
    }

    public void incOutputKeyCountbyInputKey(String inputKey, int incVal) {
        Pair <String, Integer> outputAndCounter = in2outMap.get(inputKey);
        in2outMap.replace(inputKey, new Pair(outputAndCounter.first(), outputAndCounter.second() + incVal));
    }

    public int getOutputFileCounter() {
        return outputFileCounter;
    }

    public int getReviewsPerWorker() {
        return reviewsPerWorker;
    }

    public void putInputOutputKeys(String inputKey, String outputKey) {
        Pair<String, Integer> pair = new Pair<>(outputKey, 0);
        in2outMap.put(inputKey, pair);
    }
}
