import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;


public class BPR2RSVD
{
	// === Configurations	
	// the number of latent dimensions
	public static int d = 20; 
	
	// tradeoff $\alpha_u$, $\alpha_v$, $\alpha_p$
	public static float alpha_u = 0.01f;
    public static float alpha_v = 0.01f;
    public static float alpha_p = 0.01f; 
    
    // tradeoff $\beta_u$, $\beta_v$
    public static float beta_u = 0.01f;
    public static float beta_v = 0.01f;
      
    // learning rate $\gamma$
    public static float gamma = 0.01f;
    
    // file names
    public static String fnTrainData = "";
    public static String fnTestData = "";
	public static String fnInputCandidateItems = "";
	public static String fnOutputCandidateItems = "";
   
    // 
    public static int n; // number of users
	public static int m; // number of items
	public static int num_train_target; // number of target training triples of (user,item,rating)
	public static int num_test; // number of test triples of (user,item,rating)
		
	// scan number over the whole data
    public static int num_iterations = 100;
    
	// === Evaluation
    // 
	public static int topK = 5; // top k in evaluation
	
    // === training data
	public static HashSet<Integer> userSetTrain = new HashSet<>();
	
    public static int[] indexUserTrain; // start from index "0"
    public static int[] indexItemTrain; 
    public static float[] ratingTrain;

    public static HashMap<Integer, HashSet<Integer>> TestDataforRank = new HashMap<Integer, HashSet<Integer>>();
    public static HashMap<Integer, ArrayList<Integer>> CandidateList = new HashMap<Integer, ArrayList<Integer>>();//add by ycduan 2015/11/21 
    
    public static HashMap<Integer, HashSet<Integer>> TrainData = new HashMap<Integer, HashSet<Integer>>();
            
	// === some statistics
    public static float[] userRatingSumTrain; // start from index "1"
    public static float[] itemRatingSumTrain;
    public static int[] userRatingNumTrain;
    public static int[] itemRatingNumTrain;
    public static float[] UserNumAuxiliary;
    public static float[] userAvgRating;  // 
    
    // === model parameters to learn
    public static float[][] U;
    public static float[][] V;
    
    public static float g_avg; // global average rating $\mu$
    public static float[] biasU;  // bias of user
    public static float[] biasV;  // bias of item
    
    // =====================================================================================
    public static void main(String[] args) throws Exception
    {	
    	// ======================================================	
		// --- Read the configurations
        for (int k=0; k < args.length; k++) 
        {
    		if (args[k].equals("-d")) d = Integer.parseInt(args[++k]);
    		else if (args[k].equals("-alpha_u")) alpha_u = Float.parseFloat(args[++k]);
    		else if (args[k].equals("-alpha_v")) alpha_v = Float.parseFloat(args[++k]);        		
    		else if (args[k].equals("-beta_u")) beta_u = Float.parseFloat(args[++k]);
    		else if (args[k].equals("-beta_v")) beta_v = Float.parseFloat(args[++k]);
    		else if (args[k].equals("-gamma")) gamma = Float.parseFloat(args[++k]);
    		else if (args[k].equals("-fnTrainData")) fnTrainData = args[++k];
    		else if (args[k].equals("-fnTestData")) fnTestData = args[++k];  
    		else if (args[k].equals("-fnInputCandidateItems")) fnInputCandidateItems = args[++k];
			else if (args[k].equals("-fnOutputCandidateItems")) fnOutputCandidateItems = args[++k];
    		else if (args[k].equals("-n")) n = Integer.parseInt(args[++k]);
    		else if (args[k].equals("-m")) m = Integer.parseInt(args[++k]);
    		else if (args[k].equals("-num_iterations")) num_iterations = Integer.parseInt(args[++k]);
    		else if (args[k].equals("-topK")) topK = Integer.parseInt(args[++k]);
        }
        
    	// ======================================================
        // System.out.println(Arrays.toString(args));
    	System.out.println("d: " + Integer.toString(d));    	
    	System.out.println("alpha_u: " + Float.toString(alpha_u));
    	System.out.println("alpha_v: " + Float.toString(alpha_v));
    	System.out.println("beta_u: " + Float.toString(beta_u));
    	System.out.println("beta_v: " + Float.toString(beta_v));
    	System.out.println("gamma: " + Float.toString(gamma));  	
    	System.out.println("fnTrainData: " + fnTrainData);
    	System.out.println("fnTestData: " + fnTestData);
		System.out.println("fnInputCandidateItems: " + fnInputCandidateItems);
		System.out.println("fnOutputCandidateItems: " + fnOutputCandidateItems);
    	System.out.println("n: " + Integer.toString(n));
    	System.out.println("m: " + Integer.toString(m));    	    	
    	System.out.println("num_iterations: " + Integer.toString(num_iterations));
    	System.out.println("topK: " + Integer.toString(topK));
    	
    	// ======================================================
		// === Locate memory for the data structure     
    	// --- some statistics
        userRatingSumTrain = new float[n+1]; // start from index "1"
        itemRatingSumTrain = new float[m+1];
        userRatingNumTrain = new int[n+1];
        itemRatingNumTrain = new int[m+1];        
        UserNumAuxiliary = new float[n+1];
        userAvgRating = new float[n+1];
        
        // --- model parameters to learn
        U = new float[n+1][d];  // start from index "1"
        V = new float[m+1][d];

        g_avg = 0; // global average rating $\mu$
        biasU = new float[n+1];  // bias of user
        biasV = new float[m+1];  // bias of item
    	// ======================================================
        
    	// ======================================================
        // --- Step 1: Read data
    	long TIME_START_READ_DATA = System.currentTimeMillis();
    	readData(fnTrainData, fnTestData);
    	long TIME_FINISH_READ_DATA = System.currentTimeMillis();
    	System.out.println("Elapsed Time (read data):" + 
    				Float.toString((TIME_FINISH_READ_DATA-TIME_START_READ_DATA)/1000F)
    				+ "s");
    	// ======================================================

    	// ======================================================
    	// === Step 2: Initialization of U,V
    	long TIME_START_INITIALIZATION = System.currentTimeMillis();
    	initialize();
    	long TIME_FINISH_INITIALIZATION = System.currentTimeMillis();
    	System.out.println("Elapsed Time (initialization):" + 
    				Float.toString((TIME_FINISH_INITIALIZATION-TIME_START_INITIALIZATION)/1000F)
    				+ "s");
    	// ======================================================
    	
    	// ======================================================
    	// === Step 3: Training
    	long TIME_START_TRAIN = System.currentTimeMillis();
    	train();
    	long TIME_FINISH_TRAIN = System.currentTimeMillis();
    	System.out.println("Elapsed Time (training):" + 
    				Float.toString((TIME_FINISH_TRAIN-TIME_START_TRAIN)/1000F)
    				+ "s");
    	// ======================================================
    	
		// ======================================================
    	// === Step 4: testRanking
    	long TIME_START_TEST = System.currentTimeMillis();
    	testRanking(TestDataforRank);
    	long TIME_FINISH_TEST = System.currentTimeMillis();
    	System.out.println("Elapsed Time (test):" + 
				Float.toString((TIME_FINISH_TEST-TIME_START_TEST)/1000F)
				+ "s");
    	// ======================================================
    }
	// =====================================================================================
	
    // =====================================================================================
    @SuppressWarnings("resource")
	public static void readData(String fnTrainData, String fnTestData) throws Exception
    {
    	// ======================================================   	
    	// --- number of target training records
    	num_train_target = 0;
    	BufferedReader brTrain = new BufferedReader(new FileReader(fnTrainData));    	
    	String line = null;
    	while ((line = brTrain.readLine())!=null)
    	{
    		num_train_target += 1;
    	}
    	System.out.println("num_train_target: " + num_train_target);
    	
    	// --- number of test records
    	num_test = 0;
    	BufferedReader brTest = new BufferedReader(new FileReader(fnTestData));
    	line = null;
    	while ((line = brTest.readLine())!=null)
    	{
    		num_test += 1;
    	}
    	System.out.println("num_test: " + num_test);
    	// ======================================================
    	
    	// ======================================================
		// --- Locate memory for the data structure    	
        // --- train data
        indexUserTrain = new int[num_train_target]; // start from index "0"
        indexItemTrain = new int[num_train_target];
        ratingTrain = new float[num_train_target];        
    	// ======================================================
        
    	// ======================================================
        int id_case=0;
    	double ratingSum=0;
    	// ======================================================
    	// Training data: (userID,itemID,rating)
		brTrain = new BufferedReader(new FileReader(fnTrainData));    	
    	line = null;
    	while ((line = brTrain.readLine())!=null)
    	{	
    		String[] terms = line.split("\\s+|,|;");
    		int userID = Integer.parseInt(terms[0]);
    		int itemID = Integer.parseInt(terms[1]);
    		float rating = Float.parseFloat(terms[2]);
    		
    		indexUserTrain[id_case] = userID;
    		indexItemTrain[id_case] = itemID;
    		ratingTrain[id_case] = rating;
    		id_case+=1;
    		    		
    		// ---
    		userRatingSumTrain[userID] += rating;
    		userRatingNumTrain[userID] += 1;    			
    		itemRatingSumTrain[itemID] += rating;
    		itemRatingNumTrain[itemID] += 1;
    		
    		ratingSum+=rating;
    		
			if(TrainData.containsKey(userID))
	    	{
	    		HashSet<Integer> itemSet = TrainData.get(userID);
	    		itemSet.add(itemID);
	    		TrainData.put(userID, itemSet);
	    	}
	    	else
	    	{
	    		HashSet<Integer> itemSet = new HashSet<Integer>();
	    		itemSet.add(itemID);
	    		TrainData.put(userID, itemSet);
	    	}
    	}
    	brTrain.close();
    	System.out.println("numusers_Train=" + TrainData.size());
    	
    	g_avg = (float) (ratingSum/num_train_target);
    	System.out.println(	"average rating value: " + Float.toString(g_avg));
    	// ======================================================
    	
    	// ======================================================
    	// Test data: (userID,itemID,rating)
    	brTest = new BufferedReader(new FileReader(fnTestData));
    	line = null;
    	while ((line = brTest.readLine())!=null)
    	{
    		String[] terms = line.split("\\s+|,|;");
    		int userID = Integer.parseInt(terms[0]);
    		int itemID = Integer.parseInt(terms[1]);
	    	
			if(TestDataforRank.containsKey(userID))
	    	{
	    		HashSet<Integer> itemSet = TestDataforRank.get(userID);
	    		itemSet.add(itemID);
	    		TestDataforRank.put(userID, itemSet);
	    	}
	    	else
	    	{
	    		HashSet<Integer> itemSet = new HashSet<Integer>();
	    		itemSet.add(itemID);
	    		TestDataforRank.put(userID, itemSet);
	    	}
    	}
    	brTest.close();
    	System.out.println( "The number of users in the test data: " + TestDataforRank.size() );
    	
    	BufferedReader brUserTopKItem = new BufferedReader(new FileReader(fnInputCandidateItems));//add by ycduan 2015/11/21 
    	line = null;
    	while ((line = brUserTopKItem.readLine())!=null)
    	{
    		String[] terms = line.split("\\s+|,|;");
    		int userID = Integer.parseInt(terms[0]);
    		int itemID = Integer.parseInt(terms[1]);
    		
			if(CandidateList.containsKey(userID))
	    	{
	    		ArrayList<Integer> itemList = CandidateList.get(userID);
	    		itemList.add(itemID);
	    		CandidateList.put(userID, itemList);
	    	}
	    	else
	    	{
	    		ArrayList<Integer> itemList = new ArrayList<Integer>();
	    		itemList.add(itemID);
	    		CandidateList.put(userID, itemList);
	    	}
    	}
    	brUserTopKItem.close();
    	System.out.println( "The number of users in the Candidate lists: " + CandidateList.size() );
    	// ======================================================
    }
	// =====================================================================================
	
    // =====================================================================================
    public static void initialize() 
    {	
    	// ======================================================    	
    	// --- initialization of U, V
    	for (int u=1; u<n+1; u++)
    	{
    		for (int f=0; f<d; f++)
    		{
    			U[u][f] = (float) ( (Math.random()-0.5)*0.01 );
    		}
    	}
    	// 
    	for (int i=1; i<m+1; i++)
    	{
    		for (int f=0; f<d; f++)
    		{
    			V[i][f] = (float) ( (Math.random()-0.5)*0.01 );    			
    		}
    	}
    	// ======================================================
    	    	
    	// ======================================================
    	// --- initialization of biasU, biasV
    	for (int u=1; u<n+1; u++)
    	{
    		if(userRatingNumTrain[u]>0)
    		{
    			biasU[u]= ( userRatingSumTrain[u]-g_avg*userRatingNumTrain[u] ) / userRatingNumTrain[u]; 
    			
    			userAvgRating[u] = userRatingSumTrain[u] / userRatingNumTrain[u];
    		}
    		else
    		{
    			userAvgRating[u] = g_avg;
    		}
    	}
    	//
    	for (int i=1; i<m+1; i++)
    	{
    		if(itemRatingNumTrain[i]>0)
    		{
    			biasV[i]= ( itemRatingSumTrain[i]-g_avg*itemRatingNumTrain[i] ) / itemRatingNumTrain[i];  
    		}
    	}
    	// ======================================================    	
    }
	// =====================================================================================
	
	
    // =====================================================================================
    public static void train()
    {  	    
    	for (int iter = 0; iter < num_iterations; iter++)
    	{	
	    		    	
	    	for (int iter_rand = 0; iter_rand < num_train_target; iter_rand++) 
	    	{   	    		
	    		// ======================================================
	    		// --- random sampling one triple of (userID,itemID,rating): Math.random(): [0.0, 1.0)
	    		int rand_case = (int) Math.floor( Math.random() * num_train_target );
	    		// ======================================================	    		
	    		
	    		// ======================================================
	    		int userID = indexUserTrain[rand_case];	    		
	    		int itemID = indexItemTrain[rand_case];
	    		float rating = ratingTrain[rand_case];
	    		// ======================================================
	    			    	

	    		// ======================================================
	    		// --- prediction and error
	    		float pred = 0;
	    		float err = 0;
	    		for (int f=0; f<d; f++)
	    		{	
	    			pred += U[userID][f] * V[itemID][f];	
	    		}
	    		pred += g_avg + biasU[userID] + biasV[itemID];
	    		err = rating-pred;

	    		// --- update \mu    			
	    		g_avg = g_avg - gamma * ( -err );

	    		// --- biasU, biasV
	    		biasU[userID] = biasU[userID] - gamma * ( -err + beta_u * biasU[userID] );
	    		biasV[itemID] = biasV[itemID] - gamma * ( -err + beta_v * biasV[itemID] );

	    		// --- update U, V	    			
	    		float [] V_before_update = new float[d];
	    		for(int f=0; f<d; f++)
	    		{	
	    			V_before_update[f] = V[itemID][f];

	    			float grad_U_f = -err * V[itemID][f] + alpha_u * U[userID][f];
	    			float grad_V_f = -err * U[userID][f] + alpha_v * V[itemID][f];
	    			U[userID][f] = U[userID][f] - gamma * grad_U_f;
	    			V[itemID][f] = V[itemID][f] - gamma * grad_V_f;		    			
	    		}
	    	}
	    	gamma = gamma*0.9f;
    	}
    }    

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static void testRanking(HashMap<Integer, HashSet<Integer>> TestData) throws IOException
    {
 		BufferedWriter bwTopKInFile = null;
 		bwTopKInFile = new BufferedWriter(new FileWriter(fnOutputCandidateItems));

		// ==========================================================
		float[] PrecisionSum = new float[topK+1];
		float[] RecallSum = new float[topK+1];	
		float[] F1Sum = new float[topK+1];
		float[] NDCGSum = new float[topK+1];
		float[] OneCallSum = new float[topK+1];
		
		// --- calculate the best DCG, which can be used later
		float[] DCGbest = new float[topK+1];
		for (int k=1; k<=topK; k++)
		{
			DCGbest[k] = DCGbest[k-1];
			DCGbest[k] += 1/Math.log(k+1);
		}
		
		// --- number of test cases
    	int UserNum_TestData = 0;
		
    	for(int u=1; u<=n; u++)
		{
			// --- check whether the user $u$ is in the train set
			if (!TrainData.containsKey(u)) { // warm-start
				continue;
			}

			// =========================================================
			// --- prediction
			HashMap<Integer, Float> item2Prediction = new HashMap<Integer, Float>();
			item2Prediction.clear();

			ArrayList<Integer> ItemSet_u_TopKItem = CandidateList.get(u);
			for (int item : ItemSet_u_TopKItem) {
				// --- prediction via inner product
				float pred = g_avg + biasU[u] + biasV[item];
				for (int f = 0; f < d; f++) {
					pred += U[u][f] * V[item][f];
				}
				item2Prediction.put(item, pred);
			}

			List<Map.Entry<Integer, Float>> listY = new ArrayList<Map.Entry<Integer, Float>>(
					item2Prediction.entrySet());
			// --- sort
			listY = HeapSort.heapSort(listY, topK); // using Lei LI's heapsort

			// ===========================================================
			// === Evaluation: TopK Result
			// --- Extract the topK recommended items
			int k = 1;
			int[] TopKResult = new int[topK + 1];
			Iterator<Entry<Integer, Float>> iter = listY.iterator();
			while (iter.hasNext()) {
				if (k > topK)
					break;

				Map.Entry<Integer, Float> entry = (Map.Entry<Integer, Float>) iter.next();
				int itemID = entry.getKey();
				float preRating = entry.getValue(); // add by ycduan
													// 2015/11/21
				TopKResult[k] = itemID;
				k++;

				String tmp = ",";
				String line = Integer.toString(u);
				line += tmp + Integer.toString(itemID) + tmp + Float.toString(preRating);
				bwTopKInFile.write(line);
				bwTopKInFile.newLine();
			}
			
			if(!TestDataforRank.containsKey(u))
				continue;
			
			UserNum_TestData++; // the number of warm-start users in test data
			
			// --- the number of preferred items of user $u$ in the test data
			HashSet<Integer> ItemSet_u_TestData = TestDataforRank.get(u);
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
			// ===========================================================
		}
    	
		bwTopKInFile.flush();
    	bwTopKInFile.close();
		
    	// =========================================================
    	// --- the number of users in the test data
    	System.out.println( "The number of warm-start users in the test data: " + Integer.toString(UserNum_TestData) );
    	
    	// --- precision@k
    	for(int k=1; k<=topK; k++)
    	{
    		float prec = PrecisionSum[k]/UserNum_TestData;
    		System.out.println("Prec@"+Integer.toString(k)+":"+Float.toString(prec));    		
    	}
    	// --- recall@k
    	for(int k=1; k<=topK; k++)
    	{
    		float rec = RecallSum[k]/UserNum_TestData;
    		System.out.println("Rec@"+Integer.toString(k)+":"+Float.toString(rec));    		
    	}
    	// --- F1@k
    	for(int k=1; k<=topK; k++)
    	{
    		float F1 = F1Sum[k]/UserNum_TestData;
    		System.out.println("F1@"+Integer.toString(k)+":"+Float.toString(F1));    		
    	}
    	// --- NDCG@k
    	for(int k=1; k<=topK; k++)
    	{
    		float NDCG = NDCGSum[k]/UserNum_TestData;
    		System.out.println("NDCG@"+Integer.toString(k)+":"+Float.toString(NDCG));    		
    	}
    	// --- 1-call@k
    	for(int k=1; k<=topK; k++)
    	{
    		float OneCall = OneCallSum[k]/UserNum_TestData;
    		System.out.println("1-call@"+Integer.toString(k)+":"+Float.toString(OneCall));    		
    	}
    	// =========================================================   	
    }	
    // =====================================================================================
}