javac BPR_15.java

java BPR_15 -d 100 -alpha_u 0.001 -alpha_v 0.001 -beta_v 0.001 -gamma 0.01 -fnTrainData Netflix.ExplicitPositive4Ranking.copy1.explicit -fnTestData Netflix.ExplicitPositive4Ranking.copy1.test -n 480189 -m 17770 -num_iterations 1000 -topK 15 -fnOutputCandidateItems TOR_BPR_CandidateList_TEST_COPY1_NF_100_0.001_1000.txt > TOR_BPR_NF_TEST_COPY1_LOG_100_0.001_1000.txt
pause