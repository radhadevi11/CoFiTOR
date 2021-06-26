package refactor;

public class Configuration {
    private  int d = 20;
    // tradeoff $\alpha_u$
    private  float alpha_u = 0.01f;
    // tradeoff $\alpha_v$
    private  float alpha_v = 0.01f;
    // tradeoff $\beta_v$
    private  float beta_v = 0.01f;
    // learning rate $\gamma$
    private  float gamma = 0.01f;

    // === Input Data files
    private  String fnTrainData = "";
    private  String fnTestData = "";
    private  String fnItemERTData = "";
    private  String fnUserERTData = "";
    private  String fnOutputCandidateItems = "";

    // === 
    private  int n = 0; // number of users
    private  int m = 0; // number of items	

    // === number of the total (user, item) pairs in training data
//	private  int num_train = 10000;
    private  int num_train = 0;

    // === number of iterations (scan number over the whole data)
    private  int num_iterations = 500;

    // === type of rating: 5 or 10
    private  int rtype = 10;

    // === Evaluation
    private  int topK = 5; // top k in evaluation

    public Configuration(int d, float alpha_u, float alpha_v, float beta_v, float gamma, String fnTrainData, String fnTestData, String fnItemERTData, String fnUserERTData,
                         String fnOutputCandidateItems, int n, int m, int num_train, int num_iterations, int rtype, int topK) {
        this.d = d;
        this.alpha_u = alpha_u;
        this.alpha_v = alpha_v;
        this.beta_v = beta_v;
        this.gamma = gamma;
        this.fnTrainData = fnTrainData;
        this.fnTestData = fnTestData;
        this.fnItemERTData = fnItemERTData;
        this.fnUserERTData = fnUserERTData;
        this.fnOutputCandidateItems = fnOutputCandidateItems;
        this.n = n;
        this.m = m;
        this.num_train = num_train;
        this.num_iterations = num_iterations;
        this.rtype = rtype;
        this.topK = topK;
    }

    public int getD() {
        return d;
    }

    public float getAlpha_u() {
        return alpha_u;
    }

    public float getAlpha_v() {
        return alpha_v;
    }

    public float getBeta_v() {
        return beta_v;
    }

    public float getGamma() {
        return gamma;
    }

    public String getFnTrainData() {
        return fnTrainData;
    }

    public String getFnTestData() {
        return fnTestData;
    }

    public String getFnItemERTData() {
        return fnItemERTData;
    }

    public String getFnUserERTData() {
        return fnUserERTData;
    }

    public String getFnOutputCandidateItems() {
        return fnOutputCandidateItems;
    }

    public int getN() {
        return n;
    }

    public int getM() {
        return m;
    }

    public int getNum_train() {
        return num_train;
    }

    public int getNum_iterations() {
        return num_iterations;
    }

    public int getRtype() {
        return rtype;
    }

    public int getTopK() {
        return topK;
    }
}
