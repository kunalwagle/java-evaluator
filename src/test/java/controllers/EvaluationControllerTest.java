package controllers;

import components.Category;
import components.CompletedTest;
import components.TestResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EvaluationControllerTest {

    private List<components.Test> generateTests() {
        List<components.Test> tests = new ArrayList<>();
        tests.add(new components.Test("Test 1", Category.BASIC, 10, 20, 30, new int[]{10, 20, 30}, false, ""));
        tests.add(new components.Test("Test 2", Category.BASIC, 10, 20, 30, new int[]{10, 20, 40}, false, ""));
        return tests;
    }

    @Test
    public void compilationErrorGetsReturned() {
        String code = "package components;\n" +
                "\n" +
                "public class Answer {\n" +
                "\n" +
                "    public int[] answer(int input1, int input2, int input3) {\n" +
                "        return new int[]{10, 20, 30}\n" +
                "    }\n" +
                "\n" +
                "}";
        TestResult testResult = new EvaluationController(generateTests()).evaluate(code);
        Map<String, List<CompletedTest>> map = testResult.getCompletedTests();
        List<CompletedTest> completedTests = map.get(Category.BASIC.getType());
        CompletedTest completedTest = completedTests.get(0);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(1, completedTests.size());
        Assert.assertEquals("Unable to compile the source\n" +
                "[kind=ERROR, line=6, message=';' expected]", completedTest.getMessage());
    }

    @Test
    public void compilesAndRunsTestsUntilFailure() {
        String code = "package components;\n" +
                "\n" +
                "public class Answer {\n" +
                "\n" +
                "    public int[] answer(int input1, int input2, int input3) {\n" +
                "        return new int[]{10, 20, 30};\n" +
                "    }\n" +
                "\n" +
                "}";
        TestResult testResult = new EvaluationController(generateTests()).evaluate(code);
        Map<String, List<CompletedTest>> map = testResult.getCompletedTests();
        List<CompletedTest> completedTests = map.get(Category.BASIC.getType());
        CompletedTest completedTest1 = completedTests.get(0);
        CompletedTest completedTest2 = completedTests.get(1);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(2, completedTests.size());
        Assert.assertEquals(completedTest1.isPassed(), true);
        Assert.assertEquals(completedTest1.getMessage(), "Test Passed");
        Assert.assertEquals(completedTest2.isPassed(), false);
    }

    @Test
    public void givesSensibleErrorMessage() {
        String code = "package components;\n" +
                "\n" +
                "public class Answer {\n" +
                "\n" +
                "    public int[] answer(int input1, int input2, int input3) {\n" +
                "        return new int[]{10, 20, 40};\n" +
                "    }\n" +
                "\n" +
                "}";
        TestResult testResult = new EvaluationController(generateTests()).evaluate(code);
        Map<String, List<CompletedTest>> map = testResult.getCompletedTests();
        List<CompletedTest> completedTests = map.get(Category.BASIC.getType());
        CompletedTest completedTest = completedTests.get(0);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(1, completedTests.size());
        Assert.assertEquals(completedTest.isPassed(), false);
        Assert.assertEquals(completedTest.getMessage(), "Expected [10, 20, 30], Got [10, 20, 40]");
    }

    @Test
    public void passesTestWhenExceptionIsThrownCorrectly() {
        String code = "package components;\n" +
                "\n" +
                "public class Answer {\n" +
                "\n" +
                "    public int[] answer(int input1, int input2, int input3) {\n" +
                "        throw new NullPointerException(\"Something is null\");\n" +
                "    }\n" +
                "\n" +
                "}";
        components.Test test = new components.Test("Exception", Category.BASIC, 0, 0, 0, null, true, "java.lang.NullPointerException: Something is null");
        List<components.Test> tests = new ArrayList<>();
        tests.add(test);
        TestResult testResult = new EvaluationController(tests).evaluate(code);
        Map<String, List<CompletedTest>> map = testResult.getCompletedTests();
        List<CompletedTest> completedTests = map.get(Category.BASIC.getType());
        CompletedTest completedTest = completedTests.get(0);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(1, completedTests.size());
        Assert.assertEquals(completedTest.isPassed(), true);
        Assert.assertEquals(completedTest.getMessage(), "Test Passed");
    }

    @Test
    public void failsTestWhenExceptionIsThrownIncorrectly() {
        String code = "package components;\n" +
                "\n" +
                "public class Answer {\n" +
                "\n" +
                "    public int[] answer(int input1, int input2, int input3) {\n" +
                "        int[] arr = new int[]{2};\n" +
                "        int x = arr[42];\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "}";
        TestResult testResult = new EvaluationController(generateTests()).evaluate(code);
        Map<String, List<CompletedTest>> map = testResult.getCompletedTests();
        List<CompletedTest> completedTests = map.get(Category.BASIC.getType());
        CompletedTest completedTest = completedTests.get(0);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(1, completedTests.size());
        Assert.assertEquals(completedTest.isPassed(), false);
        Assert.assertEquals(completedTest.getMessage(), "Got an exception. Message: java.lang.ArrayIndexOutOfBoundsException: 42");
    }

    @Test
    public void failsTestWhenWrongExceptionIsThrown() {
        String code = "package components;\n" +
                "\n" +
                "public class Answer {\n" +
                "\n" +
                "    public int[] answer(int input1, int input2, int input3) {\n" +
                "        int[] arr = new int[]{2};\n" +
                "        int x = arr[42];\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "}";
        components.Test test = new components.Test("Exception", Category.BASIC, 0, 0, 0, null, true, "java.lang.NullPointerException: Something is null");
        List<components.Test> tests = new ArrayList<>();
        tests.add(test);
        TestResult testResult = new EvaluationController(tests).evaluate(code);
        Map<String, List<CompletedTest>> map = testResult.getCompletedTests();
        List<CompletedTest> completedTests = map.get(Category.BASIC.getType());
        CompletedTest completedTest = completedTests.get(0);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(1, completedTests.size());
        Assert.assertEquals(completedTest.isPassed(), false);
        Assert.assertEquals(completedTest.getMessage(), "Expected exception with message: java.lang.NullPointerException: Something is null, but got message: java.lang.ArrayIndexOutOfBoundsException: 42");
    }


}