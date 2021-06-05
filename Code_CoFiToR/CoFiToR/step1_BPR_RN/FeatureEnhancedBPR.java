import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class FeatureEnhancedBPR
{
	// === Configurations		
	// the number of latent dimensions $d$
	public static int d = 20;
	// tradeoff $\alpha_u$
	public static float alpha_u = 0.01f;
	// tradeoff $\alpha_v$
    public static float alpha_v = 0.01f;
	// tradeoff $\beta_v$
    public static float beta_v = 0.01f;
	// learning rate $\gamma$
    public static float gamma = 0.01f;
        
    // === Input Data files
    public static String fnTrainData = "";
    public static String fnTestData = "";
    public static String fnItemERTData = "";
    public static String fnUserERTData = "";
	public static String fnOutputCandidateItems = "";
	
	// === 
    public static int n = 0; // number of users
	public static int m = 0; // number of items	
	
	// === number of the total (user, item) pairs in training data
//	public static int num_train = 10000;
	public static int num_train = 0;
	
	// === number of iterations (scan number over the whole data)
	public static int num_iterations = 500;  
	
	// === type of rating: 5 or 10
	public static int rtype = 10;
		
	// === Evaluation
	public static int topK = 5; // top k in evaluation
	
    // === users in the training data(>=1/>=0.5)
	public static HashSet<Integer> userSetTrain = new HashSet<>();

	// === items in the training data(>=1/>=0.5)
	public static HashSet<Integer> ItemSetTrain = new HashSet<Integer>(); 
	
	// === training data: user -> item -> rating
		public static HashMap<Integer, HashMap<Integer, Float>> TrainData = new HashMap<Integer,HashMap<Integer, Float>>();
		public static HashMap<Integer, Float> itemERT = new HashMap<>();
		public static HashMap<Integer, Float> userERT = new HashMap<>();

    // === training data used for uniformly random sampling
    public static int[] indexUserTrain; // start from index "0"
    public static int[] indexItemTrain; // start from index "0"
    public static float[] indexRatingTrain; // start from index "0"
    
    // === test data: user -> item set
    public static HashMap<Integer, HashSet<Integer>> TestData = new HashMap<Integer, HashSet<Integer>>(); 

    // === some statistics
    public static int[] itemRatingNumTrain; // start from index "1"
    
    // === model parameters to learn, start from index "1"
    public static float[][] U;
    public static float[][] V;
    public static float[] biasV; // bias of item
	
	// === normalized rating
	public static float[] rating_weight;
       
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void main(String[] args) throws Exception
    {	
		// ------------------------------
		// === Read the configurations
        System.out.println(args.length);
        for (int k=0; k < args.length; k++)
		{
			if (args[k].equals("-d"))
				d = Integer.parseInt(args[++k]);
			else if (args[k].equals("-alpha_u"))
				alpha_u = Float.parseFloat(args[++k]);
			else if (args[k].equals("-alpha_v"))
				alpha_v = Float.parseFloat(args[++k]);
			else if (args[k].equals("-beta_v"))
				beta_v = Float.parseFloat(args[++k]);
			else if (args[k].equals("-gamma")){
                gamma = Float.parseFloat(args[++k]);
            }

			else if (args[k].equals("-fnTrainData")){
                fnTrainData = args[++k];

            }
			else if (args[k].equals("-fnTestData"))
				fnTestData = args[++k];
			else if (args[k].equals("-fnUserERTData"))
				fnUserERTData = args[++k];
			else if (args[k].equals("-fnItemERTData"))
				fnItemERTData = args[++k];
			else if (args[k].equals("-n"))
				n = Integer.parseInt(args[++k]);
			else if (args[k].equals("-m"))
				m = Integer.parseInt(args[++k]);
			else if (args[k].equals("-rtype"))
				rtype = Integer.parseInt(args[++k]);
			else if (args[k].equals("-num_iterations"))
				num_iterations = Integer.parseInt(args[++k]);
			else if (args[k].equals("-topK"))
				topK = Integer.parseInt(args[++k]);
			else if (args[k].equals("-fnOutputCandidateItems"))
				fnOutputCandidateItems = args[++k];
		}
        // ------------------------------
		
        // ------------------------------
    	// === Print the configurations
		System.out.println(Arrays.toString(args));
    	System.out.println("d: " + Integer.toString(d));
    	System.out.println("alpha_u: " + Float.toString(alpha_u));
    	System.out.println("alpha_v: " + Float.toString(alpha_v));
    	System.out.println("beta_v: " + Float.toString(beta_v));
    	System.out.println("gamma: " + Float.toString(gamma)); 
    	System.out.println("fnTrainData: " + fnTrainData);
    	System.out.println("fnTestData: " + fnTestData);
    	System.out.println("n: " + Integer.toString(n));
    	System.out.println("m: " + Integer.toString(m));
		System.out.println("rtype: " + Integer.toString(rtype));
    	System.out.println("num_iterations: " + Integer.toString(num_iterations));
    	System.out.println("topK: " + Integer.toString(topK));
    	System.out.println("fnOutputCandidateItems: " + fnOutputCandidateItems);
    	// ------------------------------
		
    	// ------------------------------ 	
    	// === some statistics 
        itemRatingNumTrain = new int[m+1]; // start from index "1"
        // ------------------------------
		
		
        // ------------------------------
		// === Locate memory for model parameters
        U = new float[n+1][d];
        V = new float[m+1][d];
        biasV = new float[m+1];  // bias of item        
        // ------------------------------
		
		
		rating_weight = new float[rtype+1];		
        
        // ------------------------------
        // === Step 1: Read data
    	long TIME_START_READ_DATA = System.currentTimeMillis();
    	readDataTrainTest();
    	long TIME_FINISH_READ_DATA = System.currentTimeMillis();
    	System.out.println("Elapsed Time (read data):" + 
    				Float.toString((TIME_FINISH_READ_DATA-TIME_START_READ_DATA)/1000F)
    				+ "s");    	
    	// ------------------------------
		
		// ------------------------------
    	System.out.println( "num_train: " + Integer.toString(num_train) );
    	// ------------------------------
		
    	// ------------------------------
		// === construct index arraies for records in train data
    	indexUserTrain = new int[num_train];
    	indexItemTrain = new int[num_train];
    	indexRatingTrain = new float[num_train];	
		
    	int idx = 0;
    	for(int u=1; u<=n; u++)
    	{
    		if (!TrainData.containsKey(u))
    			continue;
    		HashMap<Integer,Float> Item_Rating = new HashMap<>();
    		if (TrainData.containsKey(u))
    		{
    			Item_Rating = TrainData.get(u);
    		}
    		for(int i : Item_Rating.keySet())
    		{
    			indexUserTrain[idx] = u;
    			indexItemTrain[idx] = i;
    			indexRatingTrain[idx] = Item_Rating.get(i);
    			idx += 1;
    		}
    	}
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
    	testRanking(TestData);
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
		// ------------------------------
		// === Train Data
		BufferedReader br = new BufferedReader(new FileReader(fnTrainData));
		String line = null;
		br.readLine();
//        while ((line = br.readLine()) != null) {
//            System.out.println(line);
//        }
		while ((line = br.readLine()) != null) {
			String[] terms = line.split("\\s+|,|;");
			int userID = Integer.parseInt(terms[0]);
			int itemID = Integer.parseInt(terms[1]);
			float rating = Float.parseFloat(terms[2]);
			float when_rated_normalized = Float.parseFloat(terms[4]);

			// --- add to the train user set
			userSetTrain.add(userID);
			
			// --- add to the train item set
			ItemSetTrain.add(itemID);

			if (TrainData.containsKey(userID)) {
				HashMap<Integer,Float> item_rating = TrainData.get(userID);
				item_rating.put(itemID, rating);
				TrainData.put(userID, item_rating);
			} else {
				HashMap<Integer,Float> item_rating = new HashMap<>();
				item_rating.put(itemID, rating);
				TrainData.put(userID, item_rating);
			}
			
			// --- statistics

            itemRatingNumTrain[itemID] += 1;

			num_train += 1; // number of user-item pairs in train data
            /*if(num_train > 100) {
                break;
            }*/

		}
		
		System.out.println("users_Train=" + TrainData.size());
		System.out.println("users_Input=" + userSetTrain.size());
		br.close();
		// ------------------------------

		// ------------------------------
		// === RateTime Data
		br = new BufferedReader(new FileReader(fnItemERTData));
		line = null;

		while ((line = br.readLine()) != null) {
			String[] terms = line.split("\\s+|,|;");
			int movieId = Integer.parseInt(terms[0]);
			float minNormalizedItemRateTime = Float.parseFloat(terms[1]);
			itemERT.put(movieId, minNormalizedItemRateTime);

		}
		br.close();
		// ------------------------------

		// === RateTime Data
		br = new BufferedReader(new FileReader(fnUserERTData));
		line = null;

		while ((line = br.readLine()) != null) {
			String[] terms = line.split("\\s+|,|;");
			int movieId = Integer.parseInt(terms[0]);
			float avgNormalizedUserRateTime = Float.parseFloat(terms[1]);
			userERT.put(movieId, avgNormalizedUserRateTime);

		}
		br.close();
		// ------------------------------

		// ------------------------------
		// === Test Data
		br = new BufferedReader(new FileReader(fnTestData));
		line = null;
        int num_test = 0;
        br.readLine();
		while ((line = br.readLine()) != null) {
		    num_test++;
			String[] terms = line.split("\\s+|,|;");
			int userID = Integer.parseInt(terms[0]);
			int itemID = Integer.parseInt(terms[1]);

			if (TestData.containsKey(userID)) {
				HashSet<Integer> itemSet = TestData.get(userID);
				itemSet.add(itemID);
				TestData.put(userID, itemSet);
			} else {
				HashSet<Integer> itemSet = new HashSet<Integer>();
				itemSet.add(itemID);
				TestData.put(userID, itemSet);
			}
			/*if(num_test > 10000) {
			    break;
            }*/
		} 
		System.out.println( "The number of users in the test data(including cold-sart users): " + TestData.size() );
		br.close();
		// ------------------------------
	}
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void init()
    {	
    	
		// ------------------------------    	
    	// rating normalization
		float denominator =(float) Math.pow(2, 5);
		if(rtype == 5){	
			for(int i=1; i<=5; i++)
				rating_weight[i] = (float) ((Math.pow(2, i)-1)/denominator);
		}
		else if(rtype == 10){
			for(float i = 0.5f; i<=5f; i=i+0.5f){
				int loc = (int)i*2;
				rating_weight[loc] = (float) ((Math.pow(2, i)-1)/denominator);
			}
		}
		
		// ------------------------------  	
    	// --- initialization of U and V
    	for (int u=1; u<n+1; u++)
    	{
    		for (int f=0; f<d-1; f++)
    		{
    			U[u][f] = (float) ( (Math.random()-0.5)*0.01 );
    		}
			if (userERT.containsKey(u)) {

				//Float userFeature = getNormalizeFeature(userERT.get(u));
				U[u][d - 1] = userERT.get(u);
			}

    	}
		//
    	for (int i=1; i<m+1; i++)
    	{
    		for (int f=0; f<d-1; f++)
    		{
    			V[i][f] = (float) ( (Math.random()-0.5)*0.01 );
    		}
    		if(itemERT.containsKey(i)){
    			//Float itemFeature = getNormalizeFeature(itemERT.get(i));

				V[i][d-1] = itemERT.get(i);
			}
    	}
    	// ------------------------------
    	
    	// ------------------------------
    	// --- initialization of biasV
    	float g_avg = 0;
    	for (int i=1; i<m+1; i++)
    	{
    		g_avg += itemRatingNumTrain[i];
    	}
    	g_avg = g_avg/n/m;
    	System.out.println( "The global average rating:" + Float.toString(g_avg) );
    	
    	for (int i=1; i<m+1; i++)
    	{
    		 biasV[i]= (float) itemRatingNumTrain[i] / n - g_avg;
    	}
    	// ------------------------------   
    }

	private static Float getNormalizeFeature(Float feature) {
		if(feature > 0) {
			feature = 0.005f - feature;
		} else {
			feature =  feature - 0.005f;
		}
		return feature;
	}
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void train() throws IOException
	{

		for (int iter = 0; iter < num_iterations; iter++) {
			for (int iter2 = 0; iter2 < num_train; iter2++) {
                /*System.out.println("training iteration "+iter);
                System.out.println("training item "+iter2);*/
				// ------------------------------
				// --- randomly sample a user-item-rating trriple, Math.random(): [0.0, 1.0)
				int idx = (int) Math.floor(Math.random() * num_train);
				if(indexUserTrain[idx] == 0){
				    continue;
                }
				int u = indexUserTrain[idx];
				int i = indexItemTrain[idx];
				float rating = indexRatingTrain[idx];
				
				// ------------------------------
				// --- normalize rating
				float r_ui = 0f;
				if(rtype == 5){
					int loc = (int)rating;
					r_ui = rating_weight[loc];
				}
				else if(rtype == 10){
					int loc = (int)rating*2;
					r_ui = rating_weight[loc];
				}
				// ------------------------------

				// --- item -> rating of user u
				Map<Integer, Float> Item_Rating = TrainData.get(u);
				
				int j = i;
				while (true) {
					
					// --- randomly sample an item $j$, Math.random(): [0.0,1.0)
					j = (int) Math.floor(Math.random() * m) + 1;
					
					// --- check if item j is a negative sample
					if (ItemSetTrain.contains(j) && !Item_Rating.containsKey(j))
					{
						break;
					} else {
						continue;
					}
				}
				// ------------------------------
				
				// ------------------------------
				// --- calculate the loss
				float r_uij = biasV[i] - biasV[j];
				for (int f = 0; f < d; f++) {
					if(f == d -1 ) {
						r_uij += -(U[u][f] * (V[i][f] - V[j][f]));
					}
					else {
						r_uij += U[u][f] * (V[i][f] - V[j][f]);
					}

				}
				// ------------------------------ a*(b-c) = a*b -a*c

				// ------------------------------
				float EXP_r_uij = (float) Math.pow(Math.E, r_uij);
				float loss_uij = -1f / (1f + EXP_r_uij);
				// ------------------------------

				// ------------------------------
				for (int f = 0; f < d; f++) {
					
					float grad_U_u_f = r_ui * loss_uij * (V[i][f] - V[j][f]) + alpha_u * U[u][f];
					float grad_V_i_f = r_ui * loss_uij * U[u][f] + alpha_v * V[i][f];
					float grad_V_j_f = r_ui * loss_uij * (-U[u][f]) + alpha_v * V[j][f];
					
					// --- update $U_{u\cdot}$
					U[u][f] = U[u][f] - gamma * grad_U_u_f;
					// --- update Vi
					V[i][f] = V[i][f] - gamma * grad_V_i_f; 
					// --- update Vj
					V[j][f] = V[j][f] - gamma * grad_V_j_f;

				}
				// ------------------------------

				// ------------------------------
				// --- update biasVi
				float grad_biasV_i = r_ui * loss_uij + beta_v * biasV[i];
				biasV[i] = biasV[i] - gamma * grad_biasV_i;
				// ------------------------------

				// ------------------------------
				// --- update biasVj
				float grad_biasV_j = r_ui * loss_uij * (-1) + beta_v * biasV[j];
				biasV[j] = biasV[j] - gamma * grad_biasV_j;
				// ------------------------------
			}
		}
	}
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
    
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		
	public static void testRanking(HashMap<Integer, HashSet<Integer>> TestData) throws IOException
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
		bwTopKInFile = new BufferedWriter(new FileWriter(fnOutputCandidateItems));
		// ------------------------------
		
		for (int u = 1; u <= n; u++) {
			
			// --- check whether the user $u$ is in the train user set
			if (!userSetTrain.contains(u))
				continue;

			// --- item-rating paris train set of user $u$
			HashMap<Integer,Float> ItemSet_u_TrainData = new HashMap<>();
			if (TrainData.containsKey(u)) {
				ItemSet_u_TrainData = TrainData.get(u);
			}

			// ------------------------------
			// === prediction
			HashMap<Integer, Float> item2Prediction = new HashMap<Integer, Float>();
			item2Prediction.clear();

			for (int i = 1; i <= m; i++) {
				
				// --- (1) check whether item $i$ is in the train item set
				// --- (2) check whether item $i$ appears in the training set of user $u$
				if (!ItemSetTrain.contains(i) || ItemSet_u_TrainData.containsKey(i))
					continue;

				// --- prediction via inner product
				float pred = 0;
				for (int f = 0; f < d; f++) {
					pred += U[u][f] * V[i][f];
				}
				pred += biasV[i];
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
			
			if(!TestData.containsKey(u))
				continue;
			
			UserNum_TestData++;
			
			// --- the number of preferred items of user $u$ in the test data 
			HashSet<Integer> ItemSet_u_TestData = TestData.get(u);
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