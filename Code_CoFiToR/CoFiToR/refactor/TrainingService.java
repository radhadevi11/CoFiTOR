package refactor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TrainingService {

    public void trainModel(Configuration configuration) throws IOException {
        //Read the CSV File
        //Train the data
        //Write the output
        TrainingData trainingData = new TrainingData(configuration.getFnTrainData());
        TestingData testingData = new TestingData(configuration.getFnTestData());
        Model model = new Model(configuration, item -> trainingData.getRatedItemCount(item));
        train(configuration, trainingData, model);
        testRanking(testingData, configuration, trainingData, model);
    }
    public  void train(Configuration configuration, TrainingData trainingData, Model model) throws IOException
    {

        for (int iter = 0; iter < configuration.getNum_iterations(); iter++) {

            for (int iter2 = 0; iter2 < trainingData.getNumTrain(); iter2++) {

                int idx = (int) Math.floor(Math.random() * trainingData.getNumTrain());
                int u = trainingData.getUserId(idx);
                int i = trainingData.getItemId(idx);
                float rating = trainingData.getRating(idx);

                Map<Integer, Float> Item_Rating = trainingData.getRatedItems(u);

                int j = i;
                j = getNonExaminedItemId(Item_Rating, configuration, trainingData);
                model.updateModel(i, u, j, rating);
            }
        }

    }
    private  int getNonExaminedItemId(Map<Integer, Float> item_Rating, Configuration configuration, TrainingData trainingData) {
        int j;
        while (true) {
            j = (int) Math.floor(Math.random() * configuration.getM()) + 1;
            if (trainingData.doesItemExist(j) && !item_Rating.containsKey(j))
            {
                break;
            } else {
                continue;
            }
        }
        return j;
    }

    public  void testRanking(TestingData testingData, Configuration configuration, TrainingData trainingData, Model model) throws IOException
    {
        // ------------------------------

        Metrics metrics = new Metrics(configuration.getTopK());


        // ------------------------------

        // ------------------------------
        // === calculate the best DCG, which can be used later

        int UserNum_TestData = 0;

        // ------------------------------
        // === output candidatelist file
        BufferedWriter bwTopKInFile = null;
        bwTopKInFile = new BufferedWriter(new FileWriter(configuration.getFnOutputCandidateItems()));
        // ------------------------------

        for (int u = 1; u <= configuration.getN(); u++) {

            // --- check whether the user $u$ is in the train user set
            if (!trainingData.doesUserExist(u))
                continue;

            // --- item-rating paris train set of user $u$4
            Map<Integer,Float> ItemSet_u_TrainData = new HashMap<>();
            if (trainingData.doesUserExist(u)) {
                ItemSet_u_TrainData = trainingData.getRatedItems(u);
            }

            // ------------------------------
            // === prediction
            final List<Map.Entry<Integer, Float>> listY = predictForUser(u, ItemSet_u_TrainData, configuration,
                    trainingData, model);

            // === output candidatelist of user $u$
            int[] TopKResult = getTopKResultsForUser(bwTopKInFile, u, listY, configuration);

            if(!testingData.doesUserExist(u))
                continue;

            UserNum_TestData++;

            // --- the number of preferred items of user $u$ in the test data
            Set<Integer> ItemSet_u_TestData = testingData.getRatedItems(u);
            int ItemNum_u_TestData = ItemSet_u_TestData.size();

            // --- TopK evaluation

            metrics.populateMetrics(TopKResult, ItemSet_u_TestData);
            // ------------------------------
        }
        bwTopKInFile.flush();
        bwTopKInFile.close();

        // ------------------------------
        // --- the number of users in the test data
        System.out.println( "The number of warm-start users in the test data: " + UserNum_TestData );

        metrics.print(UserNum_TestData);
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


    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
}

