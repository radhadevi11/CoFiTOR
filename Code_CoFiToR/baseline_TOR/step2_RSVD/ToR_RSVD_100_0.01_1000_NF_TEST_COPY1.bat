javac BPR2RSVD.java
java BPR2RSVD -d 100 -alpha_u 0.01 -alpha_v 0.01 -beta_u 0.01 -beta_v 0.01 -gamma 0.01 -fnTrainData Netflix.ExplicitPositive4Ranking.copy1.explicit -fnTestData Netflix.ExplicitPositive4Ranking.copy1.test -n 480189 -m 17770 -num_iterations 1000 -topK 15 -fnInputCandidateItems TOR_BPR_CandidateList_TEST_COPY1_NF_100_0.001_1000.txt -fnOutputCandidateItems ToR_RSVD_TEST_NF_COPY1_CandidateList_100_0.01_1000.txt> ToR_RSVD_TEST_NF_COPY1_LOG_100_0.01_1000.txt
pause