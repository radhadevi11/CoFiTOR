import refactor.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class BPR_15_RN_copy
{

	public static TrainingData trainingData;
	public static TestingData testingData;
	public static Model model;
	public static ConfigMapper configMapper = new ConfigMapper();
	public static Configuration configuration;
    public static void main(String[] args) throws Exception
    {

        configuration = configMapper.toConfiguration(args);
    	long TIME_START_READ_DATA = System.currentTimeMillis();
    	readDataTrainTest();
    	long TIME_FINISH_READ_DATA = System.currentTimeMillis();
    	System.out.println("Elapsed Time (read data):" + 
    				Float.toString((TIME_FINISH_READ_DATA-TIME_START_READ_DATA)/1000F)
    				+ "s");    	

    	System.out.println( "num_train: " + trainingData.getNumTrain());

    	// === Step 2: Initialization of U, V, biasV
    	long TIME_START_INITIALIZATION = System.currentTimeMillis();
    	initializeModel();
    	long TIME_FINISH_INITIALIZATION = System.currentTimeMillis();
    	System.out.println("Elapsed Time (init):" + 
    				Float.toString((TIME_FINISH_INITIALIZATION-TIME_START_INITIALIZATION)/1000F)
    				+ "s");

    	// === Step 3: Training
    	long TIME_START_TRAIN = System.currentTimeMillis();
    	train();
    	long TIME_FINISH_TRAIN = System.currentTimeMillis();
    	System.out.println("Elapsed Time (training):" + 
    				Float.toString((TIME_FINISH_TRAIN-TIME_START_TRAIN)/1000F)
    				+ "s");

    	// === Step 4: Re-ranking and Evaluation    	
    	long TIME_START_TEST = System.currentTimeMillis();
    	testRanking(testingData);
    	long TIME_FINISH_TEST = System.currentTimeMillis();
    	System.out.println("Elapsed Time (test):" + 
    				Float.toString((TIME_FINISH_TEST-TIME_START_TEST)/1000F)
    				+ "s");
        // ------------------------------
    }
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void readDataTrainTest() throws Exception
	{
		trainingData = new TrainingData(configuration.getFnTrainData());
		testingData = new TestingData(configuration.getFnTestData());
		System.out.println("users_Train=" + trainingData.getNoOfUsers());
		System.out.println("users_Input=" + trainingData.getNoOfUsers());


		System.out.println( "The number of users in the test data(including cold-sart users): " + testingData.getSize() );

	}




    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void initializeModel()
    {
    	// --- initialization of U and V
		model = new Model(configuration, item -> trainingData.getRatedItemCount(item));

    }



	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void train() throws IOException
	{

		for (int iter = 0; iter < configuration.getNum_iterations(); iter++) {

			for (int iter2 = 0; iter2 < trainingData.getNumTrain(); iter2++) {

				int idx = (int) Math.floor(Math.random() * trainingData.getNumTrain());
				int u = trainingData.getUserId(idx);
				int i = trainingData.getItemId(idx);
				float rating = trainingData.getRating(idx);

				Map<Integer, Float> Item_Rating = trainingData.getRatedItems(u);
				
				int j = i;
				j = getNonExaminedItemId(Item_Rating);
				model.updateModel(i, u, j, rating);
			}
		}
	}

	private static int getNonExaminedItemId(Map<Integer, Float> item_Rating) {
		int j;
		while (true) {

			// --- randomly sample an item $j$, Math.random(): [0.0,1.0)
			j = (int) Math.floor(Math.random() * configuration.getM()) + 1;

			// --- check if item j is a negative sample
			if (trainingData.doesItemExist(j) && !item_Rating.containsKey(j))
			{
				break;
			} else {
				continue;
			}
		}
		return j;
	}
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//	float costFunction(int u, int i, int j) {
//
//	}
    
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		
	public static void testRanking(TestingData testingData) throws IOException
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

			// --- item-rating paris train set of user $u$
			Map<Integer,Float> ItemSet_u_TrainData = new HashMap<>();
			if (trainingData.doesUserExist(u)) {
				ItemSet_u_TrainData = trainingData.getRatedItems(u);
			}

			// ------------------------------
			// === prediction
			final List<Entry<Integer, Float>> listY = predictForUser(u, ItemSet_u_TrainData);

			// === output candidatelist of user $u$
			int[] TopKResult = getTopKResultsForUser(bwTopKInFile, u, listY);

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


	private static int[] getTopKResultsForUser(BufferedWriter bwTopKInFile, int u, List<Entry<Integer, Float>> listY) throws IOException {
		int k = 1;
		int[] TopKResult = new int[configuration.getTopK() + 1];
		Iterator<Entry<Integer, Float>> iter = listY.iterator();
		while (iter.hasNext()) {
			if (k > configuration.getTopK())
				break;

			Entry<Integer, Float> entry = (Entry<Integer, Float>) iter.next();
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


	private static List<Entry<Integer, Float>> predictForUser(int u, Map<Integer, Float> itemSet_u_TrainData) {
		HashMap<Integer, Float> item2Prediction = new HashMap<Integer, Float>();
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
		List<Entry<Integer, Float>> listY = new ArrayList<>(
				item2Prediction.entrySet());

		listY = HeapSort.heapSort(listY, configuration.getTopK()); // using Lei LI's heapsort
		return listY;
	}


	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
}