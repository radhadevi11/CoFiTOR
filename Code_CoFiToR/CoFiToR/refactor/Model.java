package refactor;
//import java.util.Map;

import java.util.Map;
import java.util.function.Function;

public class Model {
    private  float[][] U;
    private  float[][] V;
    private  float[] biasV;
    private int m;
    private int n;
    private int d;

    public Model(int d, int n, int m, Function<Integer, Integer> itemCountProvider) {
        this.m = m;
        this.n = n;
        this.d = d;
        populateVectorMatrix(n, d, U);
        populateVectorMatrix(m, d, V);
        populateBiasV(itemCountProvider);
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
        for (int i=1; i<m+1; i++)
        {
            biasV[i] = (float) itemCountProvider.apply(i) / n - gAvg;
        }
    }
    private  float computeGAvg(Function<Integer, Integer> itemCountProvider) {
        float g_avg = 0;
        for (int i=1; i<m+1; i++)
        {
            g_avg += itemCountProvider.apply(i);
        }
        g_avg = g_avg / n / m;
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

    public void updateModel (int i, int u, int j, float r_ui, float alpha_u, float alpha_v, float gamma, float beta_v) {
        float r_uij = biasV[i] - biasV[j];
        for (int f = 0; f < d; f++) {
            r_uij += U[u][f] * (V[i][f] - V[j][f]);
        }
        // ------------------------------

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
    }

}
