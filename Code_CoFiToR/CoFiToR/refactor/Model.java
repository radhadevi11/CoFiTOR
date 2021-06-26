package refactor;
//import java.util.Map;

import java.util.function.Function;

public class Model {
    private  float[][] U;
    private  float[][] V;
    private  float[] biasV;
    private Configuration configuration;
    private float ratingWeight[];

    public Model(Configuration configuration, Function<Integer, Integer> itemCountProvider) {
        this.configuration = configuration;
        ratingWeight = new float[this.configuration.getRtype()+1];
        U = new float[this.configuration.getN()+1][this.configuration.getD()];
        V = new float[this.configuration.getM()+1][this.configuration.getD()];
        biasV = new float[this.configuration.getM()+1];
        populateVectorMatrix(this.configuration.getN(), this.configuration.getD(), U);
        populateVectorMatrix(this.configuration.getM(), this.configuration.getD(), V);
        populateBiasV(itemCountProvider);
        populateRatingWeight();
    }

    private void populateRatingWeight() {
        float denominator =(float) Math.pow(2, 5);
        if(configuration.getRtype() == 5){
            for(int i=1; i<=5; i++)
                ratingWeight[i] = (float) ((Math.pow(2, i)-1)/denominator);
        }
        else if(configuration.getRtype() == 10){
            for(float i = 0.5f; i<=5f; i=i+0.5f){
                int loc = (int)i*2;
                ratingWeight[loc] = (float) ((Math.pow(2, i)-1)/denominator);
            }
        }
    }

    public float getRatingWeight (int rating) {
        return this.ratingWeight[rating];
    }

    private  void populateVectorMatrix(int n, int d, float[][] u2) {
        for (int u = 1; u < n + 1; u++) {
            for (int f = 0; f < d; f++) {
                u2[u][f] = (float) ((Math.random() - 0.5) * 0.01);
            }
        }

    }

    private  void populateBiasV(Function<Integer, Integer> itemCountProvider) {
        final float gAvg = computeGAvg(itemCountProvider);
        for (int i=1; i<this.configuration.getM()+1; i++)
        {
            biasV[i] = (float) itemCountProvider.apply(i) / this.configuration.getN() - gAvg;
        }
    }
    private  float computeGAvg(Function<Integer, Integer> itemCountProvider) {
        float g_avg = 0;
        for (int i=1; i<this.configuration.getM()+1; i++)
        {
            g_avg += itemCountProvider.apply(i);
        }
        g_avg = g_avg / this.configuration.getN() / this.configuration.getM();
        System.out.println( "The global average rating:" + g_avg);
        return g_avg;
    }
    public float getUserFeature (int userId, int featureId) {
        return U[userId][featureId];
    }
    public void updateUserFeature (int userId, int featureId, float gamma, float gradientValue) {
         U[userId][featureId] -= gamma * gradientValue;
    }

    public float getItemFeature (int itemId, int featureId) {
        return V[itemId][featureId];
    }
    public void updateItemFeature (int itemId, int featureId, float gamma, float gradientValue) {
        V[itemId][featureId] -= gamma * gradientValue;
    }
    public float getItemBias (int itemId) {
        return biasV[itemId];
    }
    public void updateItemBias (int itemId, float gamma, float gradientValue) {
        biasV[itemId] -= gamma * gradientValue;
    }

    public void updateModel (int i, int u, int j, float rating) {
        float r_ui = 0f;
        if(configuration.getRtype() == 5){
            int loc = (int)rating;
            r_ui = ratingWeight[loc];
        }
        else if(configuration.getRtype() == 10){
            int loc = (int)rating*2;
            r_ui = ratingWeight[loc];
        }
        float r_uij = biasV[i] - biasV[j];
        for (int f = 0; f < this.configuration.getD(); f++) {
            r_uij += U[u][f] * (V[i][f] - V[j][f]);
        }
        // ------------------------------

        // ------------------------------
        float EXP_r_uij = (float) Math.pow(Math.E, r_uij);
        float loss_uij = -1f / (1f + EXP_r_uij);
        // ------------------------------

        // ------------------------------
        for (int f = 0; f < this.configuration.getD(); f++) {

            float grad_U_u_f = r_ui * loss_uij * (V[i][f] - V[j][f]) + configuration.getAlpha_u() * U[u][f];
            float grad_V_i_f = r_ui * loss_uij * U[u][f] + configuration.getAlpha_v() * V[i][f];
            float grad_V_j_f = r_ui * loss_uij * (-U[u][f]) + configuration.getAlpha_v() * V[j][f];

            // --- update $U_{u\cdot}$
            U[u][f] = U[u][f] - configuration.getGamma() * grad_U_u_f;
            // --- update Vi
            V[i][f] = V[i][f] - configuration.getGamma() * grad_V_i_f;
            // --- update Vj
            V[j][f] = V[j][f] - configuration.getGamma() * grad_V_j_f;

        }
        // ------------------------------

        // ------------------------------
        // --- update biasVi
        float grad_biasV_i = r_ui * loss_uij + configuration.getBeta_v() * biasV[i];
        biasV[i] = biasV[i] - configuration.getGamma() * grad_biasV_i;
        // ------------------------------

        // ------------------------------
        // --- update biasVj
        float grad_biasV_j = r_ui * loss_uij * (-1) + configuration.getBeta_v() * biasV[j];
        biasV[j] = biasV[j] - configuration.getGamma() * grad_biasV_j;
    }

}
