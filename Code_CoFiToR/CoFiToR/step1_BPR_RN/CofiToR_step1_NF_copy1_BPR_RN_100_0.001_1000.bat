javac BPR_15_RN.java
java BPR_15_RN -d 100 -alpha_u 0.001 -alpha_v 0.001 -beta_v 0.001 -gamma 0.01 -fnTrainData Netflix.ExplicitPositive4Ranking.copy1.explicit -fnTestData Netflix.ExplicitPositive4Ranking.copy1.test -n 480189 -m 17770 -num_iterations 1000 -topK 15 -fnOutputCandidateItems CandidateItems_CofiToR_step1_BPR_RN_NF_copy1_100_0.001_1000 > LOG_CofiToR_step1_NF_BPR_RN_copy1_100_0.001_1000.txt
pause