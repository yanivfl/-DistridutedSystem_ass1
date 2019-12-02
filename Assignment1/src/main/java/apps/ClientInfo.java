package apps;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class ClientInfo {

    private Map<String, Pair<String, Integer>> in2outMap;
    private int outputFileCounter;
    private int reviewsPerWorker;

    public ClientInfo(String[] inputKeys, String[] outputKeys, int reviewsPerWorker) {
        if (inputKeys.length != outputKeys.length) {
            throw new RuntimeException("Different length of input and outputs");
        }

        this.outputFileCounter = 0;
        this.reviewsPerWorker = reviewsPerWorker;
        this.in2outMap = new HashMap <>();

        for (int i=0; i<inputKeys.length; i++) {
            Pair <String, Integer> outputAndCounter = new Pair<>(outputKeys[i], 0);
            in2outMap.put(inputKeys[i], outputAndCounter);
        }
    }

    public String getOutputKeyByInputKey(String inputKey) {
        Pair <String, Integer> outputAndCounter = in2outMap.get(inputKey);
        return outputAndCounter.getKey();
    }

    public int getOutputKeyCountbyInputKey(String inputKey) {
        Pair <String, Integer> outputAndCounter = in2outMap.get(inputKey);
        return outputAndCounter.getValue();
    }

    public void incOutputKeyCountbyInputKey(String inputKey, int incVal) {
        Pair <String, Integer> outputAndCounter = in2outMap.get(inputKey);
        in2outMap.replace(inputKey, new Pair<>(outputAndCounter.getKey(), outputAndCounter.getValue() + incVal));
    }

    public int getOutputFileCounter() {
        return outputFileCounter;
    }

    public int getReviewsPerWorker() {
        return reviewsPerWorker;
    }
}
