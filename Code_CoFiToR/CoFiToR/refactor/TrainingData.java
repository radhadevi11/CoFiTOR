package refactor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;
import java.util.Map;
import java.util.Set;

public class TrainingData {
    private  Set<Integer> itemSetTrain = new HashSet<>();
    private final Map<Integer,Map<Integer, Float>> trainData = new HashMap<>();
    private int numTrain = 0;
    private int[] itemRatingNumTrain;



    public TrainingData(String trainDataFile, int noOfItems) throws IOException {
        itemRatingNumTrain = new int[noOfItems];
        readDataTrainTest(trainDataFile);
    }

    private void readDataTrainTest(String trainDataFile) throws IOException {
        // ------------------------------
        // === Train Data
        Path trainDataPath = new File(trainDataFile).toPath();
        try (Stream<String> lines = Files.lines(trainDataPath)) {
            lines.forEach(this::readTrainDataLine);

        }
    }
    private  void readTrainDataLine(String line) {
        String[] terms = line.split("\\s+|,|;");
        int userID = Integer.parseInt(terms[0]);
        int itemID = Integer.parseInt(terms[1]);
        float rating = Float.parseFloat(terms[2]);
        float when_rated_normalized = Float.parseFloat(terms[4]);
        itemSetTrain.add(itemID);
        trainData.computeIfAbsent(userID, u -> new HashMap<>()).put(itemID, rating);
        itemRatingNumTrain[itemID] += 1;
        this.numTrain++;
    }

    public boolean doesUserExist(int userId) {
        return this.trainData.containsKey(userId);
    }


    public boolean doesItemExist(int itemId) {
        return this.itemSetTrain.contains(itemId);
    }

    public Map<Integer, Float> getRatedItems (int userId) {
        return trainData.get(userId);
    }

    public int getNoOfUsers () {
        return this.trainData.size();
    }

}
