javac BPR_15.java

java BPR_15 -d 100 -alpha_u 0.001 -alpha_v 0.001 -beta_v 0.001 -gamma 0.01 -fnTrainData ML10M.ExplicitPositive4Ranking.copy1.explicit -fnTestData ML10M.ExplicitPositive4Ranking.copy1.test -n 71567 -m 10681 -num_iterations 500 -topK 15 -fnOutputCandidateItems TOR_BPR_CandidateList_TEST_COPY1_10M_100_0.001_500.txt > TOR_BPR_10M_TEST_COPY1_LOG_100_0.001_500.txt

pause