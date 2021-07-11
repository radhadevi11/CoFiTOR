package refactor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.IntStream;

public class TrainingData {
    private  Set<Integer> itemSetTrain = new HashSet<>();
    private final Map<Integer,Map<Integer, Float>> trainData = new HashMap<>();
    private int numTrain = 0;
    private Map<Integer, Integer> ratedItemCount = new HashMap<>();
    private List<ItemRatedByUser> itemRatedByUserList ;



    public TrainingData(String trainDataFile) throws IOException {
        readDataTrainTest(trainDataFile);
        populateItemRatedByUserList();
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
        ratedItemCount.merge(itemID, 1, (countSoFar, newCount) -> countSoFar+1);
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

    public void populateItemRatedByUserList() {
        itemRatedByUserList = IntStream.range(1, getNoOfUsers() + 1)
                .boxed()
                .filter(this::doesUserExist)
                .flatMap(u -> this.getRatedItems(u).entrySet().stream()
                        .map(entry -> new ItemRatedByUser(u, entry.getKey(), entry.getValue())))
                .collect(Collectors.toList());

    }

    public int getUserId(int index) {
        return this.itemRatedByUserList.get(index).getUserId();
    }
    public int getItemId(int index) {
        return this.itemRatedByUserList.get(index).getItemId();
    }
    public float getRating (int index) {
        return this.itemRatedByUserList.get(index).getRatings();
    }
    public Map<Integer, Integer> getRatedItemCount() {
        return ratedItemCount;
    }

    public int getRatedItemCount (int itemId) {
        return this.ratedItemCount.get(itemId);
    }

    public int getNumTrain() {
        return numTrain;
    }


}
