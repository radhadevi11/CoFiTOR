This is a instruction manual for repeating experiments in CoFiToR.
======================================
File Folders
--------------------------------------
There are four algorithms' reported in the paper:

1. BPR (baseline)
2. RSVD (baseline)
3. ToR (baseline)
4. CoFiToR (our proposed)

Corresponding to four folders:

1. baseline_BPR_OCCF
2. baseline_RSVD
3. baseline_TOR
4. CoFiToR

Each folder contains:

1. algorithm code (.java file)
2. test scripts in dataset ML10M and dataset Netflix (.bat file)
======================================


======================================
DataSet
--------------------------------------
ML10M and Netflix

train data file: rating > 0
validation data file: rating == 5
test data: rating == 5
======================================


======================================
Baseline: BPR
--------------------------------------

Train data: records that rating == 5
Test data: records that rating == 5

Users in candidate list: users in initial train data (rating > 0)
Items in candidate list: items in initial train data (rating > 0)

--------------------------------------
Input configurations:

-d: dimension of latent feature vector
-alpha_u: regularization tradeoff of users' feature vectors
-alpha_v: regularization tradeoff of items' feature vectors
-beta_v: regularization tradeoff of item bias
-gamma: learning rate
-fnTrainData: train data file (rating > 0) 
-fnTestData: test data file (rating == 5)
-n: number of users in train data file
-m: number of items in train data file
-num_iterations: number of iterations
-topK: number of recommended items for each user
-fnOutputCandidateList: output file for candidate list of each user
--------------------------------------
Output evaluation metrices:
> BPR_OCCF_10M_TEST_COPY1_LOG_100_0.001_1000.txt
======================================


======================================
Baseline: RSVD
--------------------------------------

Train data: records that rating > 0
Test data: records that rating == 5

Users in candidate list: users in initial train data (rating > 0)
Items in candidate list: items in initial train data (rating > 0)

--------------------------------------
Input configurations

-d: dimension of latent feature vector
-alpha_u: regularization tradeoff of users' feature vectors
-alpha_v: regularization tradeoff of items' feature vectors
-beta_u: regularization tradeoff of user bias
-beta_v: regularization tradeoff of item bias
-gamma: learning rate
-fnTrainData: train data file (rating > 0) 
-fnTestData: test data file (rating = 5)
-n: number of users in train data file
-m: number of items in train data file
-num_iterations: number of iterations
-topK: number of recommended items for each user
--------------------------------------
Output evaluation metrices：
> baseline_RSVD_TEST_COPY1_LOG_100_0.001_100.txt

======================================
Baseline: ToR
--------------------------------------
Step1: BPR
--------------------------------------

Train data: records that rating > 0
Test data: records that rating == 5

Users in candidate list: users in initial train data (rating > 0)
Items in candidate list: items in initial train data (rating > 0)

--------------------------------------
Input configurations

-d: dimension of latent feature vector
-alpha_u: regularization tradeoff of users' feature vectors
-alpha_v: regularization tradeoff of items' feature vectors
-beta_v: regularization tradeoff of item bias
-gamma: learning rate
-fnTrainData: train data file (rating > 0) 
-fnTestData: test data file (rating == 5)
-n: number of users in train data file
-m: number of items in train data file
-num_iterations: number of iterations
-topK: number of recommended items for each user
-fnOutputCandidateItems: output file for candidate list of each user
--------------------------------------
Output evaluation metrices：
> TOR_BPR_10M_TEST_COPY1_LOG_100_0.001_500.txt

--------------------------------------
Step2: RSVD
--------------------------------------
Train data: records that rating > 0
Test data: records that rating == 5

Users in candidate list: users in initial train data (rating > 0)
Items in candidate list: items in initial train data (rating > 0)

--------------------------------------
Input configurations

-d: dimension of latent feature vector
-alpha_u: regularization tradeoff of users' feature vectors
-alpha_v: regularization tradeoff of items' feature vectors
-beta_u: regularization tradeoff of user bias
-beta_v: regularization tradeoff of item bias
-gamma: learning rate
-fnTrainData: train data file (rating > 0) 
-fnTestData: test data file (rating = 5)
-n: number of users in train data file
-m: number of items in train data file
-num_iterations: number of iterations
-topK: number of recommended items for each user
-fnInputCandidateItems: input candidate list file from Step1 BPR
-fnOutputCandidateItems: output file for re-ranked candidate list of each user
--------------------------------------
Output evaluation metrices：
> ToR_RSVD_TEST_10M_COPY1_LOG_100_0.01_100.txt
======================================


======================================
Proposed: CoFiToR
--------------------------------------
Step1: BPR(rating normalization)
--------------------------------------

Train data: records that rating > 0
Test data: records that rating == 5

Users in candidate list: users in initial train data (rating > 0)
Items in candidate list: items in initial train data (rating > 0)

--------------------------------------
Input configurations

-d: dimension of latent feature vector
-alpha_u: regularization tradeoff of users' feature vectors
-alpha_v: regularization tradeoff of items' feature vectors
-beta_v: regularization tradeoff of item bias
-gamma: learning rate
-fnTrainData: train data file (rating > 0) 
-fnTestData: test data file (rating == 5)
-n: number of users in train data file
-m: number of items in train data file
-num_iterations: number of iterations
-topK: number of recommended items for each user
-fnOutputCandidateItems: output file for candidate list of each user
--------------------------------------
Output evaluation metrices：
> LOG_CofiToR_step1_ML10M_BPR_RN_copy1_100_0.001_1000.txt

--------------------------------------
Step2: RSVD
--------------------------------------
Train data: records that rating > 0
Test data: records that rating == 5

Users in candidate list: users in initial train data (rating > 0)
Items in candidate list: items in initial train data (rating > 0)

--------------------------------------
Input configurations

-d: dimension of latent feature vector
-alpha_u: regularization tradeoff of users' feature vectors
-alpha_v: regularization tradeoff of items' feature vectors
-beta_u: regularization tradeoff of user bias
-beta_v: regularization tradeoff of item bias
-gamma: learning rate
-fnTrainData: train data file (rating > 0) 
-fnTestData: test data file (rating == 5)
-n: number of users in train data file
-m: number of items in train data file
-num_iterations: number of iterations
-topK: number of recommended items for each user
-fnInputCandidateItems: input candidate list file from Step1 BPR
-fnOutputCandidateItems: output file for re-ranked candidate list of each user
--------------------------------------
Output evaluation metrices：
> CofiToR_step2_TEST_10M_COPY1_LOG_100_0.01_100.txt

--------------------------------------
Step3: BPR
--------------------------------------
Train data: records that rating == 5
Test data: records that rating == 5

Users in candidate list: users in initial train data (rating > 0)
Items in candidate list: items in initial train data (rating > 0)

--------------------------------------
Input configurations

-d: dimension of latent feature vector
-alpha_u: regularization tradeoff of users' feature vectors
-alpha_v: regularization tradeoff of items' feature vectors
-beta_v: regularization tradeoff of item bias
-gamma: learning rate
-fnTrainData: train data file (rating > 0) 
-fnTestData: test data file (rating = 5)
-n: number of users in train data file
-m: number of items in train data file
-num_iterations: number of iterations
-topK: number of recommended items for each user
-fnInputCandidateList: input candidate list file from Step2 RSVD
-fnOutputCandidateItems: output file for re-ranked candidate list of each user
--------------------------------------
Output evaluation metrices：
> CofiToR_step3_TEST_10M_COPY1_LOG_100_0.001_1000.txt
======================================


======================================
Running Script
--------------------------------------
There are two running scripts for each algorithm, which can run directly.
======================================


======================================
Related Information
--------------------------------------
paper: Transfer to Rank for Top-N Recommendation
Authors: Wei Dai, Qing Zhang, Weike Pan and Zhong Ming
Email: daiwei20171@email.szu.edu.cn, qingzhang1992@qq.com, panweike@szu.ed.cn, mingz@szu.edu.cn
Date: 2018/10/11