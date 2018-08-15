package JNI;

import components.CompletedTest;
import components.Test;
import components.TestResult;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


public class Utilities {

    // Used for the JNI script that builds the Native java code from C
    private static File createTempScript(String command) throws IOException {

        File tempScript = File.createTempFile("script", null);

        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(tempScript));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        // Header of BASH script
        printWriter.println("#!/bin/bash");

        if (command.compareTo("javac") == 0) {
            // JavaC and JavaH compilation
            printWriter.println("javac TempJNICpp.java");
            printWriter.println("javah -jni TempJNICpp");
            //printWriter.println("javac -h . TempJNICpp.java");
        } else if (command.compareTo("gcc") == 0) {
            // GCC compilation of the resulting C script
            if ("mac os x".equals(System.getProperty("os.name").toLowerCase())) {
                printWriter.println("gcc -I\"/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.13.sdk/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers/\" -I\"$JAVA_HOME/include/darwin/\" -o libtempjnicpp.jnilib -shared TempJNICpp.c");
            } else {
                printWriter.println("gcc -fPIC -I\"$JAVA_HOME/include\" -I\"$JAVA_HOME/include/linux\" -shared -o libhello.so TempJNICpp.c");
            }
        } else if (command.compareTo("run") == 0) {
            // Run the damn build, ffs
            printWriter.println("java -Djava.library.path=. TempJNICpp");
        }

        printWriter.close();

        return tempScript;
    }

    // Used to create the Native Java Class that calls the C/Cpp
    private static void createNativeJavaTestClass(Test test) throws IOException {

        File nativeFile = new File("TempJNICpp.java");

        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(nativeFile));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        // TODO: move all this to a file in the scope of the project to improve cleanness
        printWriter.println("public class TempJNICpp {");
        printWriter.println("static {");
        printWriter.println("System.loadLibrary(\"tempjnicpp\");"); // This will be referenced as .so as we run on Unix server
        printWriter.println("}");
        printWriter.println();
        printWriter.println("private native int[] shuffle(String arg1, int length1, String arg2, int length2, String arg3, int length3);"); // Native method declaration
        printWriter.println("public static void main(String[] args){"); // Test Driver
        printWriter.println("int[] a = new TempJNICpp().shuffle(" + "\"" + test.getFirstInput() + "\"" + ", " + test.getFirstInput().length() + ", " + "\"" + test.getSecondInput() + "\"" + ", " + test.getSecondInput().length() + ", " + "\"" + test.getThirdInput() + "\"" + ", " + test.getThirdInput().length() + "); \n for(int i = 0; i < 3; i++) { System.out.println(a[i]); } \n"); // Invoke native method
        printWriter.println("a = new int[]{};\n}\n}\n");

        printWriter.close();

    }

    // Used to create the native C++ header for implementation of the C++ code received
    private static void createHeaderFile() throws IOException {

        File headerFile = new File("TempJNICppImpl.h");

        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(headerFile));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        // TODO: move all this to a file in the scope of the project to improve cleanness
        printWriter.println("#ifndef _TEMP_JNI_CPP_IMPL_H");
        printWriter.println("#define _TEMP_JNI_CPP_IMPL_H");
        printWriter.println("");
        printWriter.println("#ifdef __cplusplus");
        printWriter.println("extern \"C\" {");
        printWriter.println("#endif");
        printWriter.println("int* shuffle(char* arg1, int len1, char* arg2, int len2, char* arg3, int len3);");
        printWriter.println("#ifdef __cplusplus");
        printWriter.println("}");
        printWriter.println("#endif");
        printWriter.println("");
        printWriter.println("#endif");

        printWriter.close();

    }

    // Creates the C interface that communicates with Java
    private static void createInterfaceFile(String input) throws IOException {

        File interfaceFile = new File("TempJNICpp.c");

        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(interfaceFile));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        printWriter.println("#include <jni.h>");
        printWriter.println("#include \"TempJNICpp.h\"");
        printWriter.println("#include \"TempJNICppImpl.h\"\n");

        printWriter.println("JNIEXPORT jintArray JNICALL Java_TempJNICpp_shuffle (JNIEnv *env, jobject thisObj, jstring input1, jint len1, jstring input2, jint len2, jstring input3, jint len3) {"); // not sure about this line
        printWriter.println("jintArray output;\nint i;\njint result[3];");
        printWriter.println("output = (*env)->NewIntArray(env, 3);");
        printWriter.println("const char *cInput1 = (*env)->GetStringUTFChars(env, input1,0);");
        printWriter.println("const char *cInput2 = (*env)->GetStringUTFChars(env, input2, 0);");
        printWriter.println("const char *cInput3 = (*env)->GetStringUTFChars(env, input3, 0);");
        printWriter.println("int* temp = shuffle(cInput1, len1, cInput2, len2, cInput3, len3);");
        /*printWriter.println("(*env)->ReleaseStringUTFChars(env, input1, cInput1);");
        printWriter.println("(*env)->ReleaseStringUTFChars(env, input2, cInput2);");
        printWriter.println("(*env)->ReleaseStringUTFChars(env, input3, cInput3);");*/
        printWriter.println("for (i = 0; i < 3; i ++){ result[i] = temp[i]; }");
        printWriter.println("(*env)->SetIntArrayRegion(env, output, 0, 3, result);");
        printWriter.println("return output;");
        printWriter.println("}");
        printWriter.println(input);
        printWriter.close();
    }


    // Compilation orchestrator
    public static String compile(Test test, String input) throws IOException {
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

        StringBuilder compilationOutput = new StringBuilder();

        tempScript = createTempScript("gcc");
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                compilationOutput.append(line).append("\n");
            }
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            tempScript.delete();
        }

        //System.out.println(compilationOutput);
        return secureCompilationOutput(compilationOutput.toString());
    }

    // Removes unnecessary information from compilation output
    private static String secureCompilationOutput(String compilationOutput) {
        if (!compilationOutput.equals("") && compilationOutput.contains("TempJNICpp.c:")) {

            String builder = "";

            String[] lines = compilationOutput.split("\n");
            for (int i = 0; i < lines.length - 1; i++) {
                if (lines[i].contains("error")) {
                    builder += lines[i] + "\n";
                    if (i < lines.length - 2) builder += lines[i + 1];
                }
            }

            System.out.println(builder);
            lines = builder.split("\n");
            if (lines.length > 0 && !builder.equals("")) {
                String[] details = lines[0].split(":");
                lines[0] = details[3] + ": " + details[4];

                if (lines.length > 1) {
                    builder = lines[0] + "\n" + lines[1].trim();
                } else {
                    builder = lines[0];
                }


                return builder;
            }
        }
        return compilationOutput;
    }

    // Test run orchestrator
    public static C_Response runCTest(Test test) throws IOException {

        C_Response response = new C_Response();

        createNativeJavaTestClass(test);

        // Build the class again
        File tempScript = createTempScript("javac");
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
            Process process = pb.start();
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            tempScript.delete();
        }

        StringBuilder runtimeOutput = new StringBuilder();

        // Run the test, catch the output
        tempScript = createTempScript("run");
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                runtimeOutput.append(line).append("\n");
            }

            process.waitFor();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            tempScript.delete();
        }

        // Validate and check the output
        String lines[] = runtimeOutput.toString().split("\n");

        ArrayList<Integer> outputList = new ArrayList<>();

        if (lines.length > 2 && !runtimeOutput.toString().contains("-1\n") && !runtimeOutput.toString().contains("error")) {
            try {
                for (int i = 0; i < 3; i++) outputList.add(Integer.parseInt(lines[i]));
            } catch (Exception e) {
                response.error = runtimeOutput.toString();
            }
        } else {
            String[] runtimeOutputLines = runtimeOutput.toString().split("\n");
            response.error = runtimeOutput.toString();
        }


        // Convert to int[] for assertion
        int[] values = new int[outputList.size()];
        for (int i = 0; i < outputList.size(); i++) {
            values[i] = outputList.get(i);
        }

        response.values = values;

        return response;
    }


}
