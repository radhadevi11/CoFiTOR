package refactor;

public class ConfigMapper {

    public Configuration toConfiguration(String[] args) {
          int d = 20;
        // tradeoff $\alpha_u$
          float alpha_u = 0.01f;
        // tradeoff $\alpha_v$
          float alpha_v = 0.01f;
        // tradeoff $\beta_v$
          float beta_v = 0.01f;
        // learning rate $\gamma$
          float gamma = 0.01f;

        // === Input Data files
          String fnTrainData = "";
          String fnTestData = "";
          String fnItemERTData = "";
          String fnUserERTData = "";
          String fnOutputCandidateItems = "";

        // === 
          int n = 0; // number of users
          int m = 0; // number of items	

        // === number of the total (user, item) pairs in training data
//	  int num_train = 10000;
          int num_train = 0;

        // === number of iterations (scan number over the whole data)
          int num_iterations = 500;

        // === type of rating: 5 or 10
          int rtype = 10;

        // === Evaluation
          int topK = 5; // top k in evaluation

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
        return new Configuration.ConfigBuilder(fnTrainData, fnTestData, fnItemERTData, fnUserERTData,
                fnOutputCandidateItems, n, m)
                .d(d)
                .Alpha_u(alpha_u)
                .Alpha_v(alpha_v)
                .Beta_v(beta_v)
                .Gamma(gamma)
                .Num_iterations(num_iterations)
                .Rtype(rtype).TopK(topK)
                .build();
    }
}
