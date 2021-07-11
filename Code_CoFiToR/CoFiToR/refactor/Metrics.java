package refactor;

import java.util.Set;

public class Metrics {
    private float[] precisionSum;
    private float[] recallSum;
    private float[] f1Sum;
    private float[] DCG ;
    private float[] DCGbest2 ;
    private float[] DCGbest ;
    private float[] NDCGSum;
    private float[] oneCallSum;
    private int topK;

    public Metrics(int topK) {
        precisionSum = new float[topK + 1];
        recallSum = new float[topK + 1];
        f1Sum = new float[topK + 1];
        DCG = new float[topK + 1];
        DCGbest2 = new float[topK + 1];
        DCGbest = new float[topK + 1];
        NDCGSum = new float[topK + 1];
        oneCallSum = new float[topK + 1];
        this.topK = topK;
        for (int k=1; k<=topK; k++)
        {
            DCGbest[k] = DCGbest[k-1];
            DCGbest[k] += 1/Math.log(k+1);
        }
    }

    public void populateMetrics(int[] TopKResult, Set<Integer> ItemSet_u_TestData) {
        int HitSum = 0;
        for (int k = 1; k <= topK; k++) {
            // ---
            DCG[k] = DCG[k - 1];
            int itemID = TopKResult[k];
            if (ItemSet_u_TestData.contains(itemID)) {
                HitSum += 1;
                DCG[k] += 1 / Math.log(k + 1);
            }
            // --- precision, recall, F1, 1-call
            float prec = (float) HitSum / k;
            float rec = (float) HitSum / ItemSet_u_TestData.size();
            float F1 = 0;
            if (prec + rec > 0)
                F1 = 2 * prec * rec / (prec + rec);
            precisionSum[k] += prec;
            recallSum[k] += rec;
            f1Sum[k] += F1;
            // --- in case the the number relevant items is smaller than k
            if (ItemSet_u_TestData.size() >= k)
                DCGbest2[k] = DCGbest[k];
            else
                DCGbest2[k] = DCGbest2[k - 1];
            NDCGSum[k] += DCG[k] / DCGbest2[k];
            // ---
            oneCallSum[k] += HitSum > 0 ? 1 : 0;
        }
    }

    public void print(int userNum_TestData) {
        // --- precision@k
        for(int k=1; k<=topK; k++)
        {
            float prec = precisionSum[k]/ userNum_TestData;
            System.out.println("Prec@"+k+":"+prec);
        }
        // --- recall@k
        for(int k=1; k<=topK; k++)
        {
            float rec = recallSum[k]/ userNum_TestData;
            System.out.println("Rec@"+k+":"+rec);
        }
        // --- F1@k
        for(int k=1; k<=topK; k++)
        {
            float F1 = f1Sum[k]/ userNum_TestData;
            System.out.println("F1@"+k+":"+F1);
        }
        // --- NDCG@k
        for(int k=1; k<=topK; k++)
        {
            float NDCG = NDCGSum[k]/ userNum_TestData;
            System.out.println("NDCG@"+k+":"+NDCG);
        }
        // --- 1-call@k
        for(int k=1; k<=topK; k++)
        {
            float OneCall = oneCallSum[k]/ userNum_TestData;
            System.out.println("1-call@"+k+":"+OneCall);
        }
    }
}
