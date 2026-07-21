#include <jni.h>

#include <atlbase.h>
//#include <oleauto.h>
//#include <olectl.h>
#include <wchar.h>
#include <windows.h>

extern "C" {
	
	void Throw(JNIEnv *env, const char* desc, jint hr);
	
	IDispatch* getDispatch(JNIEnv *env, jobject arg);
	IUnknown* getUnknown(JNIEnv *env, jobject arg);
	VARIANT* getVARIANT(JNIEnv *env, jobject arg);
	BSTR getBSTR(JNIEnv *jnienv, jobject arg);
	void logmsg(char* msg);
	void logmsgw(wchar_t* v);
	void logmsgv(VARIANT *v);
	char* TypeDescToString(TYPEDESC* typeDesc, ITypeInfo* pTypeInfo);
	char* CustomTypeToString(HREFTYPE refType, ITypeInfo* pti);
	char* convertWideCharsToMultiBytes(wchar_t* wcString);
}