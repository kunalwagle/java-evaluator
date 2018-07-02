package components;

import java.util.List;

public class TestResult {

    private List<CompletedTest> completedTests;
    private boolean allTestsComplete;

    public TestResult(List<CompletedTest> completedTests, int numberOfTests) {
        this.completedTests = completedTests;
        this.allTestsComplete = numberOfTests == completedTests.size();
    }

    public List<CompletedTest> getCompletedTests() {
        return completedTests;
    }

    public boolean isAllTestsComplete() {
        return allTestsComplete;
    }
}
