package controllers;


//import components.Answer;

import components.Category;
import components.CompletedTest;
import components.Test;
import components.TestResult;
import org.mdkt.compiler.InMemoryJavaCompiler;
import org.python.core.PyException;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@RestController
public class EvaluationController {

    private List<Test> tests;

    public EvaluationController() {
        this.tests = generateTests();
    }

    public EvaluationController(List<Test> tests) {
        this.tests = tests;
    }

    @CrossOrigin
    @RequestMapping(path = "/evaluate/{language}", method = RequestMethod.POST)
    public TestResult evaluate(@PathVariable String language, @RequestBody String input) {

        List<CompletedTest> completedTests = new ArrayList<>();

        Class<?> compiledClass = null;
        boolean successfullyCompiled = true;

        PyObject shuffleFunction = null;

        if (language.equals("java")) {
            try {
                compiledClass = compileInput(input);
            } catch (Exception e) {
                completedTests.add(new CompletedTest(tests.get(0), false, e.getMessage()));
            }
            successfullyCompiled = compiledClass != null;
        } else {
            try {
                Properties p = new Properties();
                p.setProperty("python.import.site", "false");
                PythonInterpreter.initialize(System.getProperties(), p, new String[] {});
                PythonInterpreter interpreter = new PythonInterpreter();
                interpreter.exec(input);

                shuffleFunction = interpreter.get("shuffle");
            } catch (Exception  e) {
                completedTests.add(new CompletedTest(tests.get(0), false, e.toString()));
                successfullyCompiled = false;
            }

        }

        if (successfullyCompiled) {

            assert tests != null;

            for (Test test : tests) {
                CompletedTest completedTest = runTest(compiledClass, shuffleFunction, test, language);
                completedTests.add(completedTest);
                if (!completedTest.isPassed()) {
                    break;
                }
            }

        }

        return new TestResult(completedTests, tests.size());
    }

    private int[] runPythonTest(PyObject function, Test test) {

        PyObject result = function.__call__(new PyInteger(test.getFirstInput()), new PyInteger(test.getSecondInput()), new PyInteger(test.getThirdInput()));

        return (int[]) result.__tojava__(int[].class);

    }

    private CompletedTest runTest(Class<?> compiledClass, PyObject shuffleFunction, Test test, String language) {
        try {
            int[] response;
            if (language.equals("java")) {
                response = runJavaTest(compiledClass, test);
            } else {
                response = runPythonTest(shuffleFunction, test);
            }
            if (Arrays.equals(test.getExpected(), response)) {
                return new CompletedTest(test, true, "Test Passed");
            } else {
                return new CompletedTest(test, false, "Expected " + Arrays.toString(test.getExpected()) + ", Got " + Arrays.toString(response));
            }
        } catch (InvocationTargetException e) {
            return checkError(test, e.getTargetException().toString());
        } catch (PyException e) {
            return checkError(test, e.toString());
        } catch (Exception e) {
            return new CompletedTest(test, false, "Got an exception. Message: " + e.getMessage());
        }
    }

    private CompletedTest checkError(Test test, String exceptionMessage) {
        if (test.isExceptionExpected()) {
            if (exceptionMessage.contains(test.getExceptionText())) {
                return new CompletedTest(test, true, "Test Passed");
            }
            return new CompletedTest(test, false, "Expected exception with message: " + test.getExceptionText() + ", but got message: " + exceptionMessage);
        }
        return new CompletedTest(test, false, "Got an exception. Message: " + exceptionMessage);
    }

    private int[] runJavaTest(Class<?> compiledClass, Test test) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Object answer = compiledClass.newInstance();
        return (int[]) compiledClass.getMethods()[0].invoke(answer, test.getFirstInput(), test.getSecondInput(), test.getThirdInput());
    }

    private List<Test> generateTests() {
        List<Test> tests = new ArrayList<>();
//        tests.add(new Test("Test 1", Category.BASIC, 10, 20, 30, new int[]{10, 20, 30}, false, ""));
        tests.add(new Test("shuffle should throw error if action is not an integer", Category.BASIC, "",
                "number", "", "invalid action type", true,
                "invalid action type"));
        tests.add(new Test("shuffle should throw error is action is outside 1 and 4", Category.BASIC, "",
                "5", "", "action is out of range", true,
                "action is out of range"));
        tests.add(new Test("when action is 1 move number of chars specified in optionalArgs" +
                " from the end of inputString to beginning", Category.MEDIUM, "string", "1", "3",
                "ingstr", false, ""));
        tests.add(new Test("when action is 2 reverse a string", Category.MEDIUM, "string", "2", "",
                "gnirts", false, ""));
        return tests;
    }

    private Class<?> compileInput(String input) throws Exception {
        return InMemoryJavaCompiler.newInstance().compile("components.Answer", input);
    }

}
