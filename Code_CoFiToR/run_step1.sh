cd /home/ubuntu
java -Xmx10240m BPR_15_RN_copy -d 20 -alpha_u 0.001 -alpha_v 0.001 -beta_v 0.001 -gamma 0.01 -fnTrainData /var/lib/mysql-files/set1.csv -fnTestData /var/lib/mysql-files/set2.csv -n 60827 -m 10438 -num_iterations 100 -topK 10 -fnOutputCandidateItems step1_output > step1_log.txt