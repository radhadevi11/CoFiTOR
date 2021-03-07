import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class BPR_OCCF
{
	// === Configurations		
	// the number of latent dimensions
	public static int d = 20;
	public static float alpha_u = 0.01f;
    public static float alpha_v = 0.01f;
    public static float beta_v = 0.01f;
    public static float gamma = 0.01f;
        
    // === Data
    public static String fnTrainData = "";
    public static String fnTestData = "";
	public static String fnOutputCandidateList = "";
    
    // ===
    public static int n = 0; // number of users
	public static int m = 0; // number of items	
	public static int num_train = 0; // number of the total (user, item) pairs in training data
	public static int num_iterations = 500; // scan number over the whole data 
	public static int Num_Users_TestWarmStart = 0;
	
	// === Evaluation
	public static int topK = 5; // top k in evaluation
    
	// === candidatelist output flag
	public static boolean flagOutputCandidateList = false;
	
    // === train data (rating == 5): user -> items
    public static HashMap<Integer, HashSet<Integer>> TrainData = new HashMap<Integer, HashSet<Integer>>();
	
	// === initial train data (rating > 0): user -> items
	public static HashMap<Integer, HashSet<Integer>> InitialTrainData = new HashMap<Integer, HashSet<Integer>>();
    
    // === train data used for uniformly random sampling
    public static int[] indexUserTrain; // start from index "0",
    public static int[] indexItemTrain; // start from index "0", 
    
    // === test data: user -> items
    public static HashMap<Integer, HashSet<Integer>> TestData = new HashMap<Integer, HashSet<Integer>>();
	
	// === items in train data (rating == 5)
    public static HashSet<Integer> ItemSetWhole = new HashSet<Integer>(); 
	
	// === items in initial train data (rating > 0)
	public static HashSet<Integer> InitialItemSetWhole = new HashSet<Integer>();

    // === some statistics, start from index "1"
    public static int[] itemRatingNumTrain; 
    
    // === model parameters to learn, start from index "1"
    public static float[][] U;
    public static float[][] V;
    public static float[] biasV;  // bias of item
       
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void main(String[] args) throws Exception
    {	
    	// =========================================================
		// === Read the configurations
        for (int k=0; k < args.length; k++)
        {
    		if (args[k].equals("-d")) d = Integer.parseInt(args[++k]);
    		else if (args[k].equals("-alpha_u")) alpha_u = Float.parseFloat(args[++k]);
    		else if (args[k].equals("-alpha_v")) alpha_v = Float.parseFloat(args[++k]);        		
    		else if (args[k].equals("-beta_v")) beta_v = Float.parseFloat(args[++k]);
    		else if (args[k].equals("-gamma")) gamma = Float.parseFloat(args[++k]);
    		else if (args[k].equals("-fnTrainData")) fnTrainData = args[++k];    		
    		else if (args[k].equals("-fnTestData")) fnTestData = args[++k];
    		else if (args[k].equals("-n")) n = Integer.parseInt(args[++k]);
    		else if (args[k].equals("-m")) m = Integer.parseInt(args[++k]);
    		else if (args[k].equals("-num_iterations")) num_iterations = Integer.parseInt(args[++k]);
    		else if (args[k].equals("-topK")) topK = Integer.parseInt(args[++k]);
			else if(args[k].equals("-fnOutputCandidateList"))
			{
                flagOutputCandidateList=true;
                fnOutputCandidateList=args[++k];
            }			
        }
        
		// =========================================================
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
    	System.out.println("num_iterations: " + Integer.toString(num_iterations));
    	System.out.println("topK: " + Integer.toString(topK));
		System.out.println("fnOutputCandidateList: " + fnOutputCandidateList);	
    	// =========================================================
    	
    	// --- some statistics 
        itemRatingNumTrain = new int[m+1]; // start from index "1"
        
        // =========================================================
		// === Locate memory for the data structure of the model parameters
        U = new float[n+1][d];
        V = new float[m+1][d];
        biasV = new float[m+1];  // bias of item        
        // =========================================================
        
        // =========================================================
        // === Step 1: Read data
    	long TIME_START_READ_DATA = System.currentTimeMillis();
    	readDataTrainTest();
    	long TIME_FINISH_READ_DATA = System.currentTimeMillis();
    	System.out.println("Elapsed Time (read data):" + 
    				Float.toString((TIME_FINISH_READ_DATA-TIME_START_READ_DATA)/1000F)
    				+ "s");   	
    	// =========================================================
    	System.out.println( "num_train: " + Integer.toString(num_train) );
    	
    	// =========================================================
    	indexUserTrain = new int[num_train];
    	indexItemTrain = new int[num_train];
    	    	
    	int idx = 0;
    	for(int u=1; u<=n; u++)
    	{
    		if (!TrainData.containsKey(u))
    			continue;
    		HashSet<Integer> ItemSet = new HashSet<Integer>();
    		if (TrainData.containsKey(u))
    		{
    			ItemSet = TrainData.get(u);
    		}
    		for(int i : ItemSet)
    		{
    			indexUserTrain[idx] = u;
    			indexItemTrain[idx] = i;
    			idx += 1;
    		}
    	}
    	// =========================================================
    	
    	// =========================================================
    	// === Step 2: Initialization of U, V, biasV
    	long TIME_START_INITIALIZATION = System.currentTimeMillis();
    	init();
    	long TIME_FINISH_INITIALIZATION = System.currentTimeMillis();
    	System.out.println("Elapsed Time (init):" + 
    				Float.toString((TIME_FINISH_INITIALIZATION-TIME_START_INITIALIZATION)/1000F)
    				+ "s");
    	// =========================================================
    	
    	// =========================================================
    	// === Step 3: Training
    	long TIME_START_TRAIN = System.currentTimeMillis();
    	train();
    	long TIME_FINISH_TRAIN = System.currentTimeMillis();
    	System.out.println("Elapsed Time (training):" + 
    				Float.toString((TIME_FINISH_TRAIN-TIME_START_TRAIN)/1000F)
    				+ "s");
    	// =========================================================
    	
    	// =========================================================
    	// === Step 4: Prediction and Evaluation    	
    	long TIME_START_TEST = System.currentTimeMillis();
    	testRanking(TestData);
    	long TIME_FINISH_TEST = System.currentTimeMillis();
    	System.out.println("Elapsed Time (test):" + 
    				Float.toString((TIME_FINISH_TEST-TIME_START_TEST)/1000F)
    				+ "s");
    	// =========================================================
    }
        
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void readDataTrainTest() throws Exception
    {	
    	// =========================================================
		// ===  Train data
    	BufferedReader br = new BufferedReader(new FileReader(fnTrainData));
    	String line = null;
    			
    	while ( (line = br.readLine())!=null )
    	{
    		String[] terms = line.split("\\s+|,|;");
    		int userID = Integer.parseInt(terms[0]);  
    		int itemID = Integer.parseInt(terms[1]); 
    		float rating = Float.parseFloat(terms[2]);
			
			// --- add items to initial train data(rating>0)
			InitialItemSetWhole.add(itemID);
			
			// --- initial data: rating > 0
			if(InitialTrainData.containsKey(userID))
			{
				HashSet<Integer> itemSet = InitialTrainData.get(userID);
				itemSet.add(itemID);
				InitialTrainData.put(userID, itemSet);
			}
			else
			{
				HashSet<Integer> itemSet = new HashSet<Integer>();
				itemSet.add(itemID);
				InitialTrainData.put(userID, itemSet);
			}
			
			// --- train data: rating = 5
			if (rating == 5) {
			
				// --- add items to train data(rating=5)
				ItemSetWhole.add(itemID);
				
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
				itemRatingNumTrain[itemID] += 1;
				num_train += 1; // the number of user-item pairs in train data (rating=5)
			}


    	} // --- Finish reading the training data
    	br.close();

    	// =========================================================
		// === Test data
    	br = new BufferedReader(new FileReader(fnTestData));
    	line = null;
    	
    	while ( (line = br.readLine())!=null )
    	{
    		String[] terms = line.split("\\s+|,|;");
    		int userID = Integer.parseInt(terms[0]);  
    		int itemID = Integer.parseInt(terms[1]); 
			
			if(TestData.containsKey(userID))
	    	{
	    		HashSet<Integer> itemSet = TestData.get(userID);
	    		itemSet.add(itemID);
	    		TestData.put(userID, itemSet);
	    	}
	    	else
	    	{
	    		HashSet<Integer> itemSet = new HashSet<Integer>();
	    		itemSet.add(itemID);
	    		TestData.put(userID, itemSet);
	    	}

    	}
    	br.close();    	    	
    	// =========================================================
    }
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void init()
    {	
    	// ======================================================    	
    	// --- initialization of U and V
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
    	// --- initialization of biasV
    	float g_avg = 0;
    	for (int i=1; i<m+1; i++)
    	{
    		g_avg += itemRatingNumTrain[i];
    	}
    	g_avg = g_avg/n/m;
    	
    	for (int i=1; i<m+1; i++)
    	{
    		 biasV[i]= (float) itemRatingNumTrain[i] / n - g_avg;
    	}
    	// ======================================================   
    }
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void train() throws FileNotFoundException
    {  	    
    	for (int iter = 0; iter < num_iterations; iter++)
    	{	
	    	
    		for (int iter2 = 0; iter2 < num_train; iter2++)
    		{
		    	int idx = (int) Math.floor(Math.random() * num_train);
		    	int u = indexUserTrain[idx];
		    	int i = indexItemTrain[idx];
	
	    		// ===================================================
				// --- items of u in train data
		    	HashSet<Integer> ItemSet = TrainData.get(u);				
				int j = i;
				while(true)
				{
					// --- randomly sample an item $j$, Math.random(): [0.0, 1.0)
					j = (int) Math.floor(Math.random() * m) + 1;
					
					if (ItemSetWhole.contains(j) && !ItemSet.contains(j) )
					{
						break;
					}
					else
					{
						continue;
					}
				}
		    	// ===================================================
				// --- calculate the loss
				float r_uij = biasV[i] - biasV[j];
		    	for (int f=0; f<d; f++)
		    	{
		    		r_uij += U[u][f] * (V[i][f] - V[j][f]);    		
		    	}
		     	// ---------------------------------------------------

		    	// ---------------------------------------------------
			    float EXP_r_uij = (float) Math.pow(Math.E, r_uij);
			    float loss_uij = - 1f / (1f + EXP_r_uij); 	
		    	// ===================================================
			    
			    
				// ===================================================
				for (int f=0; f<d; f++)
		    	{
					float grad_U_u_f = loss_uij * ( V[i][f]- V[j][f] ) + alpha_u * U[u][f];
					float grad_V_i_f = loss_uij * U[u][f] + alpha_v * V[i][f];
					float grad_V_j_f = loss_uij * (-U[u][f]) + alpha_v * V[j][f];
					
					U[u][f] = U[u][f] - gamma * grad_U_u_f;	 // --- update U   	
					V[i][f] = V[i][f] - gamma * grad_V_i_f;  // --- update Vi
					V[j][f] = V[j][f] - gamma * grad_V_j_f;  // --- update Vj
		    	}		
				// ===================================================
				
				
				// ===================================================		    		
				// --- update biasVi
				float grad_biasV_i = loss_uij + beta_v * biasV[i];
				biasV[i] = biasV[i] - gamma * grad_biasV_i;
				// ===================================================

				// ===================================================
				// --- update biasVj
				float grad_biasV_j = loss_uij*(-1) + beta_v * biasV[j];
				biasV[j] = biasV[j] - gamma * grad_biasV_j;		    	
    		}
			
    	}
    }
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
            
			
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		
	public static void testRanking(HashMap<Integer, HashSet<Integer>> TestData) throws IOException
    {
		
		// candidatelist output file
		BufferedWriter bwOutputCandidateList = null;
    	if(flagOutputCandidateList)
    	{
    		bwOutputCandidateList = new BufferedWriter(new FileWriter(fnOutputCandidateList));
    	}
		
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
    	    	
    	for(int u=1; u<=n; u++)
    	{
    		// --- check if user $u$ is in the initial train data(rating > 0)
			// --- check if user $u$ is in the test data
    		if(!InitialTrainData.containsKey(u))
				continue;
			
    		// --- items of user u in initial train data(rating > 0)
    		HashSet<Integer> ItemSet_u_InitialTrainData = new HashSet<Integer>();
    		ItemSet_u_InitialTrainData = InitialTrainData.get(u); 		
    		
    		// =========================================================  		
    		// --- prediction    		
    		HashMap<Integer, Float> item2Prediction = new HashMap<Integer, Float>();
    		item2Prediction.clear();
    		
    		for(int i=1; i<=m; i++)
    		{
    			
				// --- (1) check whether item $i$ is in the initial whole item set
    			// --- (2) check whether item $i$ appears in the initial training set of user $u$
    			if (!InitialItemSetWhole.contains(i)||ItemSet_u_InitialTrainData.contains(i))
					continue;

    			// --- prediction via inner product
        		float pred = 0;
        		for (int f=0; f<d; f++)
        		{
        			pred += U[u][f]*V[i][f];
        		}
       			pred += biasV[i];
        		item2Prediction.put(i, pred);
        	}
			List<Map.Entry<Integer, Float>> listY = new ArrayList<Map.Entry<Integer, Float>>(
					item2Prediction.entrySet());
			// --- sort
			listY = HeapSort.heapSort(listY, topK); // using Lei LI's heapsort
    		
    		// ===========================================================
    		// === Evaluation: TopK Result 
    		// --- Extract the topK recommended items    		
    		int k=1;
    		int[] TopKResult = new int [topK+1];    		
    		Iterator<Entry<Integer, Float>> iter = listY.iterator();
    		while (iter.hasNext())
    		{
    			if(k>topK)
    				break;
    			
    			Map.Entry<Integer, Float> entry = (Map.Entry<Integer, Float>) iter.next(); 
    			int itemID = entry.getKey();
    			TopKResult[k] = itemID;

    			k++;
				
				if(flagOutputCandidateList)
                {                
                	float preRating = entry.getValue();                	
	                String tmp = ",";
	        		String line = Integer.toString(u);
	    			line += tmp + Integer.toString(itemID) + tmp + Float.toString(preRating);
	        		bwOutputCandidateList.write(line);
	        		bwOutputCandidateList.newLine();
                }
    		}
			bwOutputCandidateList.flush();
			
			// === Evaluation
			if(!TestData.containsKey(u)) //check if user u is in test data
				continue;
				
			// --- number of warm-start test users
			Num_Users_TestWarmStart++;
			
			// --- the number of preferred items of user $u$ in the test data 
    		HashSet<Integer> ItemSet_u_TestData = TestData.get(u);
    		int ItemNum_u_TestData = ItemSet_u_TestData.size();   

    		// --- TopK evaluation
    		int HitSum = 0;
    		float[] DCG = new float[topK+1];
    		float[] DCGbest2 = new float[topK+1];
    		for(k=1; k<=topK; k++)
    		{
    			// ---
    			DCG[k] = DCG[k-1];
    			int itemID = TopKResult[k];
    			if ( ItemSet_u_TestData.contains(itemID) )
    			{
        			HitSum += 1;
        			DCG[k] += 1 / Math.log(k+1);
    			}
    			// --- precision, recall, F1, 1-call
    			float prec = (float) HitSum / k;
    			float rec = (float) HitSum / ItemNum_u_TestData;    			
    			float F1 = 0;
    			if (prec+rec>0)
    				F1 = 2 * prec*rec / (prec+rec);
    			PrecisionSum[k] += prec;
    			RecallSum[k] += rec;
    			F1Sum[k] += F1;
    			// --- in case the the number relevant items is smaller than k 
    			if (ItemSet_u_TestData.size()>=k)
    				DCGbest2[k] = DCGbest[k];
    			else
    				DCGbest2[k] = DCGbest2[k-1];
    			NDCGSum[k] += DCG[k]/DCGbest2[k];
    			// ---
    			OneCallSum[k] += HitSum>0 ? 1:0; 
    		}
    		// ===========================================================
    	}    	

		bwOutputCandidateList.close();
		
    	// =========================================================
    	
    	// --- precision@k
    	for(int k=1; k<=topK; k++)
    	{
    		float prec = PrecisionSum[k]/Num_Users_TestWarmStart;
    		System.out.println("Prec@"+Integer.toString(k)+":"+Float.toString(prec));    		
    	}
    	// --- recall@k
    	for(int k=1; k<=topK; k++)
    	{
    		float rec = RecallSum[k]/Num_Users_TestWarmStart;
    		System.out.println("Rec@"+Integer.toString(k)+":"+Float.toString(rec));    		
    	}
    	// --- F1@k
    	for(int k=1; k<=topK; k++)
    	{
    		float F1 = F1Sum[k]/Num_Users_TestWarmStart;
    		System.out.println("F1@"+Integer.toString(k)+":"+Float.toString(F1));    		
    	}
    	// --- NDCG@k
    	for(int k=1; k<=topK; k++)
    	{
    		float NDCG = NDCGSum[k]/Num_Users_TestWarmStart;
    		System.out.println("NDCG@"+Integer.toString(k)+":"+Float.toString(NDCG));    		
    	}
    	// --- 1-call@k
    	for(int k=1; k<=topK; k++)
    	{
    		float OneCall = OneCallSum[k]/Num_Users_TestWarmStart;
    		System.out.println("1-call@"+Integer.toString(k)+":"+Float.toString(OneCall));    		
    	}
    }
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
}