javac RSVD_15.java
java RSVD_15 -d 100 -alpha_u 0.001 -alpha_v 0.001 -beta_u 0.001 -beta_v 0.001 -gamma 0.01 -fnTrainData ML10M.ExplicitPositive4Ranking.copy1.explicit -fnTestData ML10M.ExplicitPositive4Ranking.copy1.test -n 71567 -m 10681 -num_iterations 100 -topK 15 > baseline_RSVD_TEST_COPY1_LOG_100_0.001_100.txt
pause