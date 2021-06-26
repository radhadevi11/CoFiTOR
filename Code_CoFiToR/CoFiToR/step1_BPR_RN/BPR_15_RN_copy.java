import refactor.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class BPR_15_RN_copy
{
	public static int topK = 5;
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
    	// ------------------------------
		
		// ------------------------------
    	System.out.println( "num_train: " + Integer.toString(configuration.getNum_train()) );
    	// ------------------------------
		
    	// ------------------------------
		// === construct index arraies for records in train data


    	// ------------------------------
    	
    	
    	/// ------------------------------
    	// === Step 2: Initialization of U, V, biasV
    	long TIME_START_INITIALIZATION = System.currentTimeMillis();
    	init();
    	long TIME_FINISH_INITIALIZATION = System.currentTimeMillis();
    	System.out.println("Elapsed Time (init):" + 
    				Float.toString((TIME_FINISH_INITIALIZATION-TIME_START_INITIALIZATION)/1000F)
    				+ "s");
    	// ------------------------------
    	
    	// ------------------------------
    	// === Step 3: Training
    	long TIME_START_TRAIN = System.currentTimeMillis();
    	train();
    	long TIME_FINISH_TRAIN = System.currentTimeMillis();
    	System.out.println("Elapsed Time (training):" + 
    				Float.toString((TIME_FINISH_TRAIN-TIME_START_TRAIN)/1000F)
    				+ "s");
    	// ------------------------------
    	
    	// ------------------------------
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
    public static void init()
    {	
    	

		// ------------------------------  	
    	// --- initialization of U and V
		model = new Model(configuration, item -> trainingData.getRatedItemCount(item));

		// ------------------------------
    	
    	// ------------------------------
    	// --- initialization of biasV




		// ------------------------------
    }



	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void train() throws IOException
	{

		for (int iter = 0; iter < configuration.getNum_iterations(); iter++) {

			for (int iter2 = 0; iter2 < configuration.getNum_train(); iter2++) {
                /*System.out.println("training iteration "+iter);
                System.out.println("training item "+iter2);*/
				// ------------------------------
				// --- randomly sample a user-item-rating trriple, Math.random(): [0.0, 1.0)
				int idx = (int) Math.floor(Math.random() * configuration.getNum_train());
//				if(indexUserTrain[idx] == 0){
//				    continue;
//                }
				int u = trainingData.getUserId(idx);
				int i = trainingData.getItemId(idx);
				float rating = trainingData.getRating(idx);
				
				// ------------------------------
				// --- normalize rating

				// ------------------------------

				// --- item -> rating of user u
				Map<Integer, Float> Item_Rating = trainingData.getRatedItems(u);
				
				int j = i;
				while (true) {
					
					// --- randomly sample an item $j$, Math.random(): [0.0,1.0)
					j = (int) Math.floor(Math.random() * configuration.getM()) + 1;
					
					// --- check if item j is a negative sample
					if (trainingData.doesItemExist(j) && !Item_Rating.containsKey(j))
					{
						break;
					} else {
						continue;
					}
				}
				model.updateModel(i, u, j, rating);
			}
		}
	}
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//	float costFunction(int u, int i, int j) {
//
//	}
    
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		
	public static void testRanking(TestingData testingData) throws IOException
    {
		// ------------------------------
		float[] PrecisionSum = new float[topK+1];
		float[] RecallSum = new float[topK+1];	
		float[] F1Sum = new float[topK+1];
		float[] NDCGSum = new float[topK+1];
		float[] OneCallSum = new float[topK+1];
		// ------------------------------
		
		// ------------------------------
		// === calculate the best DCG, which can be used later
		float[] DCGbest = new float[topK+1];
		for (int k=1; k<=topK; k++)
		{
			DCGbest[k] = DCGbest[k-1];
			DCGbest[k] += 1/Math.log(k+1);
		}
		// ------------------------------
		
		// ------------------------------
		// --- number of warm-start test cases
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
			HashMap<Integer, Float> item2Prediction = new HashMap<Integer, Float>();
			item2Prediction.clear();

			for (int i = 1; i <= configuration.getM(); i++) {
				
				// --- (1) check whether item $i$ is in the train item set
				// --- (2) check whether item $i$ appears in the training set of user $u$
				if (!trainingData.doesItemExist(i) || ItemSet_u_TrainData.containsKey(i))
					continue;

				// --- prediction via inner product
				float pred = 0;
				for (int f = 0; f < configuration.getD(); f++) {
					pred += model.getUserFeature(u, f) * model.getItemFeature(i, f);
				}
				pred += model.getItemBias(i);
				item2Prediction.put(i, pred);
			}
			// ------------------------------
			
			// ------------------------------
			// === re-ranking
			List<Entry<Integer, Float>> listY = new ArrayList<Entry<Integer, Float>>(
					item2Prediction.entrySet());
			
			listY = HeapSort.heapSort(listY, topK); // using Lei LI's heapsort
			// ------------------------------
			
			// ------------------------------
			// === output candidatelist of user $u$
			int k = 1;
			int[] TopKResult = new int[topK + 1];
			Iterator<Entry<Integer, Float>> iter = listY.iterator();
			while (iter.hasNext()) {
				if (k > topK)
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
			
			if(!testingData.doesUserExist(u))
				continue;
			
			UserNum_TestData++;
			
			// --- the number of preferred items of user $u$ in the test data 
			Set<Integer> ItemSet_u_TestData = testingData.getRatedItems(u);
    		int ItemNum_u_TestData = ItemSet_u_TestData.size();    
			
			// --- TopK evaluation
			int HitSum = 0;
			float[] DCG = new float[topK + 1];
			float[] DCGbest2 = new float[topK + 1];
			for (k = 1; k <= topK; k++) {
				// ---
				DCG[k] = DCG[k - 1];
				int itemID = TopKResult[k];
				if (ItemSet_u_TestData.contains(itemID)) {
					HitSum += 1;
					DCG[k] += 1 / Math.log(k + 1);
				}
				// --- precision, recall, F1, 1-call
				float prec = (float) HitSum / k;
				float rec = (float) HitSum / ItemNum_u_TestData;
				float F1 = 0;
				if (prec + rec > 0)
					F1 = 2 * prec * rec / (prec + rec);
				PrecisionSum[k] += prec;
				RecallSum[k] += rec;
				F1Sum[k] += F1;
				// --- in case the the number relevant items is smaller than k
				if (ItemSet_u_TestData.size() >= k)
					DCGbest2[k] = DCGbest[k];
				else
					DCGbest2[k] = DCGbest2[k - 1];
				NDCGSum[k] += DCG[k] / DCGbest2[k];
				// ---
				OneCallSum[k] += HitSum > 0 ? 1 : 0;
			}
			// ------------------------------
		}
		bwTopKInFile.flush();
		bwTopKInFile.close();
		
    	// ------------------------------
    	// --- the number of users in the test data
    	System.out.println( "The number of warm-start users in the test data: " + UserNum_TestData );
    	
    	// --- precision@k
    	for(int k=1; k<=topK; k++)
    	{
    		float prec = PrecisionSum[k]/UserNum_TestData;
    		System.out.println("Prec@"+k+":"+prec);
    	}
    	// --- recall@k
    	for(int k=1; k<=topK; k++)
    	{
    		float rec = RecallSum[k]/UserNum_TestData;
    		System.out.println("Rec@"+k+":"+rec);
    	}
    	// --- F1@k
    	for(int k=1; k<=topK; k++)
    	{
    		float F1 = F1Sum[k]/UserNum_TestData;
    		System.out.println("F1@"+k+":"+F1);
    	}
    	// --- NDCG@k
    	for(int k=1; k<=topK; k++)
    	{
    		float NDCG = NDCGSum[k]/UserNum_TestData;
    		System.out.println("NDCG@"+k+":"+NDCG);
    	}
    	// --- 1-call@k
    	for(int k=1; k<=topK; k++)
    	{
    		float OneCall = OneCallSum[k]/UserNum_TestData;
    		System.out.println("1-call@"+k+":"+OneCall);
    	}
    }
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
}