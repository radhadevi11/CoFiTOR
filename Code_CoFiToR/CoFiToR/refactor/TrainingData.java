package refactor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class TrainingData {
    private  Set<Integer> itemSetTrain = new HashSet<>();
    private final Map<Integer,Map<Integer, Float>> trainData = new HashMap<>();
    private int numTrain = 0;
    private int[] itemRatingNumTrain;
    List<OneRow> rows = new ArrayList<>();



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

    public void populateTriplets() {
        for(int u=1; u<=numTrain; u++)
        {
            if (!this.doesUserExist(u))
                continue;
            Map<Integer,Float> item_Rating = new HashMap<>();
            if (this.doesUserExist(u))
            {
                item_Rating = this.getRatedItems(u);
            }
            for(int i : item_Rating.keySet())
            {
                rows.add(new OneRow(u, i, item_Rating.get(i)));
            }
        }
    }

    public int getUserId(int index) {
        return this.rows.get(index).getUserId();
    }
    public int getItemId(int index) {
        return this.rows.get(index).getItemId();
    }
    public float getRating (int index) {
        return this.rows.get(index).getRatings();
    }

}
