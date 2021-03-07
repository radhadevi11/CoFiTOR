javac *.java

java BPR_OCCF -d 100 -alpha_u 0.001 -alpha_v 0.001 -beta_v 0.001 -gamma 0.01 -fnTrainData ML10M.ExplicitPositive4Ranking.copy1.explicit -fnTestData ML10M.ExplicitPositive4Ranking.copy1.test -n 71567 -m 10681 -num_iterations 1000 -topK 15 -fnOutputCandidateList BPR_OCCF_10M_TEST_COPY1_CandidateList_100_0.001_1000.txt > BPR_OCCF_10M_TEST_COPY1_LOG_100_0.001_1000.txt

pause