package controllers;


//import components.Answer;

import JNI.Utilities;
import components.Category;
import components.CompletedTest;
import components.Test;
import components.TestResult;
import org.mdkt.compiler.InMemoryJavaCompiler;
import org.mdkt.compiler.SourceCode;
import org.python.core.PyException;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static JNI.Utilities.createNativeJavaTestClass;
import static JNI.Utilities.createTempScript;

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
    public TestResult evaluate(@PathVariable String language, @RequestBody String input) throws IOException {

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
        } else if (language.equals("python")) {
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
        } else if (language.equals("c")) {

            Utilities.createNativeJavaTestClass(tests.get(0));
            Utilities.createHeaderFile();
            Utilities.createInterfaceFile(input);

            // Create file script for JNI stuff
            File tempScript = createTempScript("javac");
            try {
                ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
                pb.inheritIO();
                Process process = pb.start();
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                tempScript.delete();
            }

            StringBuffer compilationOutput = new StringBuffer();

            tempScript = createTempScript("gcc");
            try {
                ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
                pb.redirectErrorStream(true);

                Process process = pb.start();
                process.waitFor();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    compilationOutput.append(line + "\n");
                }
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                tempScript.delete();
            }

            // Stop if error occurred
            if (compilationOutput.toString().contains("error")) {
                completedTests.add(new CompletedTest(tests.get(0), false, compilationOutput.toString()));
                System.out.println(compilationOutput.toString());
                return new TestResult(completedTests, tests.size());
            } else {
                successfullyCompiled = true;
                System.out.println("Compilation successful.");
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

    private int[] runCTest(Test test) throws IOException {
        createNativeJavaTestClass(test);

        // Build the class again
        File tempScript = createTempScript("javac");
        try{
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
            Process process = pb.start();
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            tempScript.delete();
        }

        StringBuffer runtimeOutput = new StringBuffer();

        tempScript = createTempScript("run");
        try{
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                runtimeOutput.append(line + "\n");
            }

            process.waitFor();

            //System.out.println(runtimeOutput.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            tempScript.delete();
        }

        String lines[] = runtimeOutput.toString().split("\n");
        ArrayList<Integer> outputList = new ArrayList<>();
        try {
            for (String stringNumber : lines){
                int number = Integer.parseInt(stringNumber);
                outputList.add(number);
            }
        } catch (Exception e){
            // TODO: handle runtime error
            //completedTests.add(new CompletedTest(test, false, "Got a runtime error. Message: Output should be integers"));
        }

        // Convert to int[] for assertion
        int[] response = new int[outputList.size()];
        for (int i = 0; i < outputList.size(); i++){
            response[i] = outputList.get(i);
        }

        return response;
    }

    private CompletedTest runTest(Class<?> compiledClass, PyObject shuffleFunction, Test test, String language) {
        try {
            int[] response;

            if (language.equals("java")) response = runJavaTest(compiledClass, test);
            else if (language.equals("python")) response = runPythonTest(shuffleFunction, test);
            else response = runCTest(test);

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
        tests.add(new Test("Test 1", Category.BASIC, 10, 20, 30, new int[]{10, 20, 30}, false, ""));
        tests.add(new Test("Test 2", Category.BASIC, 10, 20, 40, new int[]{10, 20, 40}, false, ""));
        tests.add(new Test("Test 3", Category.MEDIUM, 10, 20, 50, new int[]{10, 20, 50}, false, ""));
        tests.add(new Test("Test 4", Category.DIFFICULT, 10, 20, 200, new int[]{10, 20, 60}, false, ""));

        return tests;
    }

    private Class<?> compileInput(String input) throws Exception {
        return InMemoryJavaCompiler.newInstance().compile("components.Answer", input);
    }

}
