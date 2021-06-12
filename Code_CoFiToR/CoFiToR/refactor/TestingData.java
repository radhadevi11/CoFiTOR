package refactor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;


public class TestingData {

    private final Map<Integer, Set<Integer>> testData = new HashMap<>();

    public TestingData(String testDataFile) throws IOException {
        readTestData(testDataFile);
    }

    private void readTestData(String testDataFile) throws IOException {

        Path testDataPath = new File(testDataFile).toPath();
        try (Stream<String> lines = Files.lines(testDataPath)) {
            lines.forEach(this::readTestDataLine);
        }
    }

    private void readTestDataLine(String line) {

        String[] terms = line.split("\\s+|,|;");
        int userID = Integer.parseInt(terms[0]);
        int itemID = Integer.parseInt(terms[1]);

        testData.computeIfAbsent(userID, u -> new HashSet<>()).add(itemID);
    }
    public boolean doesUserExist (int userId) {
        return testData.containsKey(userId);
    }
    public Set<Integer> getRatedItems (int userId) {
        return this.testData.get(userId);
    }
    public int getSize () {
        return this.testData.size();
    }
}
