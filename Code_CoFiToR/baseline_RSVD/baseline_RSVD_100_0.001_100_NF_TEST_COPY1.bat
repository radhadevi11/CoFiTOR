javac RSVD_15.java
java RSVD_15 -d 100 -alpha_u 0.01 -alpha_v 0.01 -beta_u 0.01 -beta_v 0.01 -gamma 0.01 -fnTrainData Netflix.ExplicitPositive4Ranking.copy1.explicit -fnTestData Netflix.ExplicitPositive4Ranking.copy1.test -n 480189 -m 17770 -num_iterations 500 -topK 15 > baseline_RSVD_TEST_COPY1_NF_LOG_100_0.01_500.txt
pause