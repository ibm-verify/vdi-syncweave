#ifndef __TDI_UTIL
#define __TDI_UTIL

#include <jni.h>
#include <jni_convert.h>
#include <iconv.h>     

#define CONV_UTF2JOB 1
#define CONV_JOB2UTF 2
#define CALL_FAIL       -10001
#define RECORD_LENGTH   4096


int StringConvert(char *sourceStr, char *targetStr);

char* getOSCharArray(JNIEnv* jenv, jstring jstr);
void getCharset(JNIEnv* jEnv, jbyteArray jbEnc);
void ReleaseOSCharArray(JNIEnv *jEnv, jstring jstr, char* element);
void ReleaseNativeCharArray(JNIEnv *jEnv, jobjectArray jStrArray, char** array); 
void jstrcpy(char *to, jchar *from, int len);
char** copyJStringArray2NativeArray(JNIEnv* jEnv, jobjectArray jStrArray);
#endif

