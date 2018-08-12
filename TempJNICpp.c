#include <jni.h>
#include "TempJNICpp.h"
#include "TempJNICppImpl.h"

JNIEXPORT jintArray JNICALL Java_TempJNICpp_answer (JNIEnv *env, jobject thisObj, jint input1, jint input2, jint input3) {
jintArray output;
int i;
jint result[3];
output = (*env)->NewIntArray(env, 3);
int* temp = answer(input1, input2, input3);
for (i = 0; i < 3; i ++){ result[i] = temp[i]; }
(*env)->SetIntArrayRegion(env, output, 0, 3, result);
return output;
}
int* answer(int input1, int input2, int input3){
		int a[6];
		printf("Input is not an int");
		a[0] = input1;
		a[1] = input2;
		a[2] = input3;
		
		return a;
		

