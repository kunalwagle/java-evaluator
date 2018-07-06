package controllers;


//import components.Answer;
import components.Category;
import components.CompletedTest;
import components.Test;
import components.TestResult;
import org.mdkt.compiler.InMemoryJavaCompiler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class EvaluationController {

    private List<Test> tests;

    public EvaluationController() {
        this.tests = generateTests();
    }

    public EvaluationController(List<Test> tests) {
        this.tests = tests;
    }

    @RequestMapping(path="/evaluate", method= RequestMethod.POST)
    public TestResult evaluate(@RequestBody String input) {

        List<CompletedTest> completedTests = new ArrayList<>();

        Class<?> compiledClass = null;
        try {
            compiledClass = compileInput(input);
        } catch (Exception e) {
            completedTests.add(new CompletedTest(tests.get(0), false, e.getMessage()));
        }

        boolean successfullyCompiled = compiledClass != null;

        if (successfullyCompiled) {

            assert tests != null;

            for (Test test : tests) {
                CompletedTest completedTest = runTest(compiledClass, test);
                completedTests.add(completedTest);
                if (!completedTest.isPassed()) {
                    break;
                }
            }

        }

        return new TestResult(completedTests, tests.size());
    }

    private CompletedTest runTest(Class<?> compiledClass, Test test) {
        try {
            Object answer = compiledClass.newInstance();
            int[] response = (int[]) compiledClass.getMethods()[0].invoke(answer, test.getFirstInput(), test.getSecondInput(), test.getThirdInput()); ///(test.getFirstInput(), test.getSecondInput(), test.getThirdInput());
            if (Arrays.equals(test.getExpected(), response)) {
                return new CompletedTest(test, true, "Test Passed");
            } else {
                return new CompletedTest(test, false, "Expected " + Arrays.toString(test.getExpected()) + ", Got " + Arrays.toString(response));
            }
        } catch (InvocationTargetException e) {
            Throwable exception = e.getTargetException();
            if (test.isExceptionExpected()) {
                if (test.getExceptionText().equals(exception.toString())) {
                    return new CompletedTest(test, true, "Test Passed");
                }
                return new CompletedTest(test, false, "Expected exception with message: " + test.getExceptionText() + ", but got message: " + exception.toString());
            }
            return new CompletedTest(test, false, "Got an exception. Message: " + exception.toString());
        } catch (Exception e) {
            return new CompletedTest(test, false, "Got an exception. Message: " + e.getMessage());
        }
    }

    private List<Test> generateTests() {
        List<Test> tests = new ArrayList<>();
        tests.add(new Test("Test 1", Category.BASIC, 10, 20, 30, new int[]{10, 20, 30}, false, ""));
        tests.add(new Test("Test 2", Category.BASIC, 10, 20, 30, new int[]{10, 20, 40}, false, ""));
        tests.add(new Test("Test 3", Category.MEDIUM, 10, 20, 30, new int[]{10, 20, 50}, false, ""));
        tests.add(new Test("Test 4", Category.DIFFICULT, 10, 20, 30, new int[]{10, 20, 60}, false, ""));

        return tests;
    }

    private Class<?> compileInput(String input) throws Exception {
        return InMemoryJavaCompiler.newInstance().compile("components.Answer", input);
    }

}
