package controllers;


//import components.Answer;
import components.CompletedTest;
import components.Test;
import components.TestResult;
import org.mdkt.compiler.InMemoryJavaCompiler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class EvaluationController {

    @RequestMapping(path="/evaluate", method= RequestMethod.POST)
    public TestResult evaluate(@RequestBody String input) {

        Class<?> compiledClass = compileInput(input);

        boolean successfullyCompiled = compiledClass != null;

        if (!successfullyCompiled) {
            return null;
        }

        List<Test> tests = generateTests();
        List<CompletedTest> completedTests = new ArrayList<>();

        assert tests != null;

        for (Test test : tests) {
            CompletedTest completedTest = runTest(compiledClass, test);
            completedTests.add(completedTest);
            if (!completedTest.isPassed()) {
                break;
            }
        }

        return new TestResult(completedTests, tests.size());
    }

    private CompletedTest runTest(Class<?> compiledClass, Test test) {
        try {
            Object answer = compiledClass.newInstance();
            int[] response = (int[]) compiledClass.getMethods()[0].invoke(answer, test.getFirstInput(), test.getSecondInput(), test.getThirdInput()); ///(test.getFirstInput(), test.getSecondInput(), test.getThirdInput());
            if (Arrays.equals(test.getExpected(), response)) {
                return new CompletedTest(test, true, "components.Test Passed");
            } else {
                return new CompletedTest(test, false, "components.Test Failed");
            }
        } catch (Exception e) {
            if (test.isExceptionExpected()) {
                if (test.getExceptionText().equals(e.getCause().toString())) {
                    return new CompletedTest(test, true, "components.Test Passed");
                }
            }
            return new CompletedTest(test, false, e.getMessage());
        }
    }

    private List<Test> generateTests() {
        List<Test> tests = new ArrayList<>();
        tests.add(new Test(10, 20, 30, new int[]{10, 20, 30}, false, ""));
        tests.add(new Test(10, 20, 30, new int[]{10, 20, 40}, false, ""));
        tests.add(new Test(10, 20, 30, new int[]{10, 20, 50}, false, ""));
        tests.add(new Test(10, 20, 30, new int[]{10, 20, 60}, false, ""));

        return tests;
    }

    private Class<?> compileInput(String input) {

        Class<?> compiledClass = null;
        try {
            compiledClass = InMemoryJavaCompiler.newInstance().compile("components.Answer", input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return compiledClass;
    }

}
