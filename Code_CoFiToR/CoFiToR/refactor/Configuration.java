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

    // === number of iterations (scan number over the whole data)
    private  int num_iterations = 500;

    // === type of rating: 5 or 10
    private  int rtype = 10;

    // === Evaluation
    private  int topK = 5; // top k in evaluation

    private float ratingWeight[];

    private Configuration(ConfigBuilder configBuilder) {
        this.d = configBuilder.d;
        this.alpha_u = configBuilder.alpha_u;
        this.alpha_v = configBuilder.alpha_v;
        this.beta_v = configBuilder.beta_v;
        this.gamma = configBuilder.gamma;
        this.fnTrainData = configBuilder.fnTrainData;
        this.fnTestData = configBuilder.fnTestData;
        this.fnItemERTData = configBuilder.fnItemERTData;
        this.fnUserERTData = configBuilder.fnUserERTData;
        this.fnOutputCandidateItems = configBuilder.fnOutputCandidateItems;
        this.n = configBuilder.n;
        this.m = configBuilder.m;
        this.num_iterations = configBuilder.num_iterations;
        this.rtype = configBuilder.rtype;
        this.topK = configBuilder.topK;
        ratingWeight = new float[this.getRtype()+1];
        populateRatingWeight();
    }
    private void populateRatingWeight() {
        float denominator =(float) Math.pow(2, 5);
        if(this.getRtype() == 5){
            for(int i=1; i<=5; i++)
                ratingWeight[i] = (float) ((Math.pow(2, i)-1)/denominator);
        }
        else if(this.getRtype() == 10){
            for(float i = 0.5f; i<=5f; i=i+0.5f){
                int loc = (int)i*2;
                ratingWeight[loc] = (float) ((Math.pow(2, i)-1)/denominator);
            }
        }
    }



    public static class ConfigBuilder {
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

        // === number of iterations (scan number over the whole data)
        private  int num_iterations = 500;

        // === type of rating: 5 or 10
        private  int rtype = 10;

        // === Evaluation
        private  int topK = 5; // top k in evaluation

        public ConfigBuilder(String fnTrainData, String fnTestData, String fnItemERTData,
                             String fnUserERTData, String fnOutputCandidateItems, int n, int m) {
            this.fnTrainData = fnTrainData;
            this.fnTestData = fnTestData;
            this.fnItemERTData = fnItemERTData;
            this.fnUserERTData = fnUserERTData;
            this.fnOutputCandidateItems = fnOutputCandidateItems;
            this.n = n;
            this.m = m;
        }

        public ConfigBuilder d(int dimension) {
            d = dimension;
            return this;
        }

        public ConfigBuilder Alpha_u(float alpha_u) {
            this.alpha_u = alpha_u;
            return this;
        }

        public ConfigBuilder Alpha_v(float alpha_v) {
            this.alpha_v = alpha_v;
            return this;
        }

        public ConfigBuilder Beta_v(float beta_v) {
            this.beta_v = beta_v;
            return this;

        }

        public ConfigBuilder Gamma(float gamma) {
            this.gamma = gamma;
            return this;
        }

        public ConfigBuilder Num_iterations(int num_iterations) {
            this.num_iterations = num_iterations;
            return this;
        }

        public ConfigBuilder Rtype(int rtype) {
            this.rtype = rtype;
            return this;
        }

        public ConfigBuilder TopK(int topK) {
            this.topK = topK;
            return this;
        }

        public Configuration build() {
            return new Configuration(this);
        }
    }



    public float getRatingWeight (int rating) {
        return this.ratingWeight[rating];
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
