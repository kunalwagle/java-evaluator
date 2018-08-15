#include <jni.h>
#include "TempJNICpp.h"
#include "TempJNICppImpl.h"

JNIEXPORT jintArray JNICALL Java_TempJNICpp_shuffle (JNIEnv *env, jobject thisObj, jstring input1, jint len1, jstring input2, jint len2, jstring input3, jint len3) {
jintArray output;
int i;
jint result[3];
output = (*env)->NewIntArray(env, 3);
const char *cInput1 = (*env)->GetStringUTFChars(env, input1,0);
const char *cInput2 = (*env)->GetStringUTFChars(env, input2, 0);
const char *cInput3 = (*env)->GetStringUTFChars(env, input3, 0);
int* temp = shuffle(cInput1, len1, cInput2, len2, cInput3, len3);
for (i = 0; i < 3; i ++){ result[i] = temp[i]; }
(*env)->SetIntArrayRegion(env, output, 0, 3, result);
return output;
}
  
int* shuffle(char* input1, int len1, char* input2, int len2, char* input3, int len3){
	int a[3];
	
	printf("%s", input1);
	int dec = 0; int i;
	for(i=0; i<len1; i++){
		dec = dec * 10 + ( input1[i] - '0' );
	}
	a[0] = -1;
	
	dec = 0;
	for(i=0; i<len2; i++){
		dec = dec * 10 + ( input2[i]  -'0' );
	}
	a[1] = dec;
	
	dec = 0;
	for(i=0; i<len3; i++){
		dec = dec * 10 + ( input3[i] - '0' );
	}
	a[2] = dec;
	printf("-1");
	printf("Input is not a number");

	return a;
}
		
