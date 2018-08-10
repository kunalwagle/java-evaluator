package JNI;

import components.CompletedTest;
import components.Test;
import components.TestResult;

import java.io.*;
import java.util.ArrayList;


public class Utilities {

    // Used for the JNI script that builds the Native java code from C
    public static File createTempScript(String command) throws IOException {
        //TODO: make sure it overwrites whatever

        File tempScript = File.createTempFile("script", null);

        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(tempScript));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        // Header of BASH script
        printWriter.println("#!/bin/bash");

        if (command.compareTo("javac") == 0) {
            // JavaC and JavaH compilation
            printWriter.println("javac -h . TempJNICpp.java");
        } else if (command.compareTo("gcc") == 0){
            // GCC compilation of the resulting C script
            if ("mac os x".equals(System.getProperty("os.name").toLowerCase())){
                printWriter.println("gcc -I\"/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.13.sdk/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers/\" -I\"$JAVA_HOME/include/darwin/\" -o libjniexample.jnilib -shared TempJNICpp.c");
            } else {
                printWriter.println("gcc -fPIC -I\"$JAVA_HOME/include\" -I\"$JAVA_HOME/include/linux\" -shared -o libhello.so TempJNICpp.c");
            }
        } else if (command.compareTo("run") == 0){
            // Run the damn build, ffs
            printWriter.println("java -Djava.library.path=. TempJNICpp");
        }

        printWriter.close();

        return tempScript;
    }

    // Used to create the Native Java Class that calls the C/Cpp
    public static void createNativeJavaTestClass (Test test) throws IOException{

        File nativeFile = new File("TempJNICpp.java");

        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(nativeFile));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        // TODO: move all this to a file in the scope of the project to improve cleanness
        printWriter.println("public class TempJNICpp {");
        printWriter.println("static {");
        printWriter.println("System.loadLibrary(\"jniexample\");"); // This will be referenced as .so as we run on Unix server
        printWriter.println("}");
        printWriter.println();
        printWriter.println("private native int[] answer(int arg1, int arg2, int arg3);"); // Native method declaration
        printWriter.println("public static void main(String[] args){"); // Test Driver
        printWriter.println("int[] a = new TempJNICpp().answer("+ test.getFirstInput() + ", " + test.getSecondInput() + ", " + test.getThirdInput() + "); \n for(int i = 0; i < 3; i++) { System.out.println(a[i]); } \n}\n}"); // Invoke native method TODO: signature should use input

        printWriter.close();

    }

    // Used to create the native C++ header for implementation of the C++ code received
    public static void createHeaderFile () throws IOException{

        File headerFile = new File("TempJNICppImpl.h");
  /*      if (headerFile.exists()){
            return;
        }
*/
        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(headerFile));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        // TODO: move all this to a file in the scope of the project to improve cleanness
        printWriter.println("#ifndef _TEMP_JNI_CPP_IMPL_H");
        printWriter.println("#define _TEMP_JNI_CPP_IMPL_H");
        printWriter.println("");
        printWriter.println("#ifdef __cplusplus");
        printWriter.println("extern \"C\" {");
        printWriter.println("#endif");
        printWriter.println("int* answer(int arg1, int arg2, int arg3);");
        printWriter.println("#ifdef __cplusplus");
        printWriter.println("}");
        printWriter.println("#endif");
        printWriter.println("");
        printWriter.println("#endif");

        printWriter.close();

    }

    // Used to create the C++ file to be tested
    public static void createImplementationFile(String input) throws IOException{
        //TODO: make sure it overwrites whatever // Is this the wrong TODO? I didn't touch it just in case

        File cppFile = new File("TempJNICppImpl.c");

        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(cppFile));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        // TODO: move hardcoded header to a file in the scope of the project to improve cleanness
        printWriter.println("#include \"TempJNICppImpl.h\"");
        printWriter.println("#include <iostream>");
        printWriter.println();
        printWriter.println("using namespace std;");
        printWriter.println();
        printWriter.println(input);

        printWriter.close();
    }

    // Creates the C interface that communicates with Java
    public static void createInterfaceFile(String input) throws IOException{

        File interfaceFile = new File("TempJNICpp.c");
 /*       if (interfaceFile.exists()){
            return;
        }
*/

        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(interfaceFile));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        printWriter.println("#include <jni.h>");
        printWriter.println("#include \"TempJNICpp.h\"");
        printWriter.println("#include \"TempJNICppImpl.h\"\n");
        printWriter.println(input);
        printWriter.println("JNIEXPORT jintArray JNICALL Java_TempJNICpp_answer (JNIEnv *env, jobject thisObj, jint input1, jint input2, jint input3) {"); // not sure about this line
        printWriter.println("jintArray output;\nint i;\njint result[3];");
        printWriter.println("output = (*env)->NewIntArray(env, 3);");
        printWriter.println("int* temp = answer(input1, input2, input3);");
        printWriter.println("for (i = 0; i < 3; i ++){ result[i] = temp[i]; }");
        printWriter.println("(*env)->SetIntArrayRegion(env, output, 0, 3, result);");// TODO: this should use the signature passed in the input with the 3 arguments
        printWriter.println("return output;");
        printWriter.println("}");

        printWriter.close();
    }


    // Compilation orchestrator
    public static String compile (Test test, String input) throws IOException{
        Utilities.createNativeJavaTestClass(test);
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

        return compilationOutput.toString();
    }

    // Test run orchestrator
    public static int[] runCTest(Test test) throws IOException {
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
            // TODO: handle runtime errors
            //completedTests.add(new CompletedTest(test, false, "Got a runtime error. Message: Output should be integers"));
        }

        // Convert to int[] for assertion
        int[] response = new int[outputList.size()];
        for (int i = 0; i < outputList.size(); i++){
            response[i] = outputList.get(i);
        }

        return response;
    }


}
