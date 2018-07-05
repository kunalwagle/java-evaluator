package components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestResult {

    private Map<String, List<CompletedTest>> completedTests;
    private boolean allTestsComplete;

    public TestResult(List<CompletedTest> completedTests, int numberOfTests) {
        this.completedTests = createMap(completedTests);
        this.allTestsComplete = isAllTestsComplete(completedTests, numberOfTests);
    }

    private Map<String,List<CompletedTest>> createMap(List<CompletedTest> completedTests) {
        List<Category> categories = completedTests.stream().map(Test::getCategory).distinct().collect(Collectors.toList());
        Map<String, List<CompletedTest>> result = new HashMap<>();
        for (Category category : categories) {
            List<CompletedTest> tests = completedTests.stream().filter(test -> test.getCategory().equals(category)).collect(Collectors.toList());
            result.put(category.getType(), tests);
        }
        return result;
    }

    private boolean isAllTestsComplete(List<CompletedTest> completedTests, int numberOfTests) {
        return numberOfTests == completedTests.size() && completedTests.stream().allMatch(CompletedTest::isPassed);
    }

    public Map<String, List<CompletedTest>> getCompletedTests() {
        return completedTests;
    }

    public boolean isAllTestsComplete() {
        return allTestsComplete;
    }
}
