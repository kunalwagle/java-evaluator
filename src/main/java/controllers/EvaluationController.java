package controllers;


//import components.Answer;

import JNI.C_Response;
import JNI.Utilities;
import components.Category;
import components.CompletedTest;
import components.Test;
import components.TestResult;
import org.mdkt.compiler.InMemoryJavaCompiler;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    EvaluationController(List<Test> tests) {
        this.tests = tests;
    }

    @CrossOrigin
    @RequestMapping(path = "/evaluate/{language}", method = RequestMethod.POST)
    public TestResult evaluate(@PathVariable String language, @RequestBody String input) throws IOException {

        List<CompletedTest> completedTests = new ArrayList<>();

        Class<?> compiledClass = null;
        boolean successfullyCompiled = true;

        PyObject shuffleFunction = null;

        switch (language) {
            case "java":
                try {
                    compiledClass = compileInput(input);
                } catch (Exception e) {
                    completedTests.add(new CompletedTest(tests.get(0), false, e.getMessage()));
                }
                successfullyCompiled = compiledClass != null;
                break;
            case "python":
                try {
                    Properties p = new Properties();
                    p.setProperty("python.import.site", "false");
                    PythonInterpreter.initialize(System.getProperties(), p, new String[]{});
                    PythonInterpreter interpreter = new PythonInterpreter();
                    interpreter.exec(input);

                    shuffleFunction = interpreter.get("shuffle");
                } catch (Exception e) {
                    completedTests.add(new CompletedTest(tests.get(0), false, e.toString()));
                    successfullyCompiled = false;
                }
                break;
            case "c":
                String compilationOutput = Utilities.compile(tests.get(0), input);

                // Stop if error occurred
                if (compilationOutput.contains("error")) {
                    completedTests.add(new CompletedTest(tests.get(0), false, compilationOutput));
                    return new TestResult(completedTests, tests.size());
                } else {
                    successfullyCompiled = true;
                    System.out.println("Compilation successful.");
                }
                break;
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

        PyObject result = function.__call__(new PyString(test.getFirstInput()), new PyString(test.getSecondInput()), new PyString(test.getThirdInput()));

        return (int[]) result.__tojava__(int[].class);

    }


    private CompletedTest runTest(Class<?> compiledClass, PyObject shuffleFunction, Test test, String language) {
        try {
            int[] response;

            switch (language) {
                case "java":
                    response = runJavaTest(compiledClass, test);
                    break;
                case "python":
                    response = runPythonTest(shuffleFunction, test);
                    break;
                default:
                    C_Response cResponse = Utilities.runCTest(test);

                    System.out.println(cResponse.error);

                    if (cResponse.error.equals("") && cResponse.values.length > 1) {
                        response = cResponse.values;
                    } else if (cResponse.error.equals("") || cResponse.error.contains("TempJNICpp") || cResponse.error.contains("SIGSEV")) {
                        return new CompletedTest(test, false, "Got an exception. Message: " + "Runtime error. Please check your code.");
                    } else {
                        if (cResponse.error.contains("-1\n")) {
                            if (cResponse.error.contains("Input is not a number")) return checkError(test, new NumberFormatException("Input is not a number").getMessage());
                            return checkError(test, cResponse.error);
                        } else {
                            return new CompletedTest(test, false, "Got an exception. Message: " + cResponse.error);
                        }
                    }

                    break;
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
        tests.add(new Test("Test 1", Category.BASIC, "10", "20", "30", new int[]{}, true, "not a number"));
        tests.add(new Test("Test 2", Category.BASIC, "10", "20", "40", new int[]{10, 20, 40}, false, ""));
        tests.add(new Test("Test 3", Category.MEDIUM, "10", "20", "50", new int[]{10, 20, 50}, false, ""));
        tests.add(new Test("Test 4", Category.DIFFICULT, "10", "20", "60", new int[]{10, 20, 60}, false, ""));

        return tests;
    }

    private Class<?> compileInput(String input) throws Exception {
        return InMemoryJavaCompiler.newInstance().compile("components.Answer", input);
    }

}
