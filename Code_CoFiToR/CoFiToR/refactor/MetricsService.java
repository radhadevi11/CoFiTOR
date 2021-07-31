package refactor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class MetricsService {
    public Metrics calculateMetrics(Configuration configuration, TrainingData trainingData,
                                    Model model) {
        Metrics metrics = new Metrics(configuration.getTopK());
        for (int u = 1; u <= configuration.getN(); u++) {
            if (!trainingData.doesUserExist(u))
                continue;

            // --- item-rating paris train set of user $u$4
            Map<Integer,Float> ItemSet_u_TrainData = new HashMap<>();
            if (trainingData.doesUserExist(u)) {
                ItemSet_u_TrainData = trainingData.getRatedItems(u);
            }
            final List<Map.Entry<Integer, Float>> listY = predictForUser(u, ItemSet_u_TrainData, configuration,
                    trainingData, model);

            // === output candidatelist of user $u$
            int[] TopKResult = getTopKResultsForUser(bwTopKInFile, u, listY, configuration);

        }
    }

    private  List<Map.Entry<Integer, Float>> predictForUser(int u, Map<Integer, Float> itemSet_u_TrainData, Configuration configuration, TrainingData trainingData, Model model) {
        HashMap<Integer, Float> item2Prediction = new HashMap<>();
        item2Prediction.clear();

        for (int i = 1; i <= configuration.getM(); i++) {

            // --- (1) check whether item $i$ is in the train item set
            // --- (2) check whether item $i$ appears in the training set of user $u$
            if (!trainingData.doesItemExist(i) || itemSet_u_TrainData.containsKey(i))
                continue;

            // --- prediction via inner product
            float pred = 0;
            for (int f = 0; f < configuration.getD(); f++) {
                pred += model.getUserFeature(u, f) * model.getItemFeature(i, f);
            }
            pred += model.getItemBias(i);
            item2Prediction.put(i, pred);
        }
        List<Map.Entry<Integer, Float>> listY = new ArrayList<>(
                item2Prediction.entrySet());

        listY = HeapSort.heapSort(listY, configuration.getTopK()); // using Lei LI's heapsort
        return listY;
    }

    private static int[] getTopKResultsForUser(BufferedWriter bwTopKInFile, int u, List<Map.Entry<Integer, Float>> listY, Configuration configuration) throws IOException {
        int k = 1;
        int[] TopKResult = new int[configuration.getTopK() + 1];
        Iterator<Map.Entry<Integer, Float>> iter = listY.iterator();
        while (iter.hasNext()) {
            if (k > configuration.getTopK())
                break;

            Map.Entry<Integer, Float> entry = (Map.Entry<Integer, Float>) iter.next();
            int itemID = entry.getKey();
            float preRating = entry.getValue();
            TopKResult[k] = itemID;
            k++;

            String tmp = ",";
            String line = Integer.toString(u);
            line += tmp + Integer.toString(itemID) + tmp + Float.toString(preRating);
            bwTopKInFile.write(line);
            bwTopKInFile.newLine();
            // ------------------------------
        }
        return TopKResult;
    }

}
