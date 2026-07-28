/*
 * IBM Confidential
 *
 *  OCO Source Materials
 *
 *
 * (C) oration. 2006
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     %I%, %G%
 * @owner       Vishakha
 * @history
 */

#pragma warning (disable: 4100)
#include "VARIANT.h"
#include "COMUtil.h"
#include <jni.h>
#include <stdio.h>
#include <string>
#include <stdlib.h>

using namespace std;

extern "C"
{
	
	JNIEXPORT jint JNICALL Java_com_ibm_di_automation_VARIANT_toInt
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			HRESULT hr;
			if (FAILED(hr = VariantChangeType(v, v, 0, VT_I4))) {
				Throw(jnienv, "Cannot convert to int", hr);
				return NULL;
			}
			return (jint)V_I4(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jdouble JNICALL Java_com_ibm_di_automation_VARIANT_toDate
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			HRESULT hr;
			if (FAILED(hr = VariantChangeType(v, v, 0, VT_DATE))) {
				Throw(jnienv, "Cannot convert to date", hr);
				return NULL;
			}
			return (jint)V_DATE(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jboolean JNICALL Java_com_ibm_di_automation_VARIANT_toBoolean
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			HRESULT hr;
			if (FAILED(hr = VariantChangeType(v, v, 0, VT_BOOL))) {
				Throw(jnienv, "Cannot convert to date", hr);
				return NULL;
			}
			return (jboolean)V_BOOL(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jobject JNICALL Java_com_ibm_di_automation_VARIANT_cloneIndirect
		(JNIEnv *jnienv, jobject jthis)
	{
		Throw(jnienv, "Not yet implemented", -1);
		return NULL;
	}
	
	
	JNIEXPORT jdouble JNICALL Java_com_ibm_di_automation_VARIANT_toDouble
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			HRESULT hr;
			if (FAILED(hr = VariantChangeType(v, v, 0, VT_R8))) {
				Throw(jnienv, "Cannot convert to date", hr);
				return NULL;
			}
			return (jint)V_R8(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jlong JNICALL Java_com_ibm_di_automation_VARIANT_toCurrency
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			HRESULT hr;
			if (FAILED(hr = VariantChangeType(v, v, 0, VT_CY))) {
				Throw(jnienv, "Cannot convert to date", hr);
				return NULL;
			}
			return (jlong)V_CY(v).int64;
		}
		return NULL;
	}
	
	
	JNIEXPORT jstring JNICALL Java_com_ibm_di_automation_VARIANT_toString
		(JNIEnv *jnienv, jobject jthis)
	{
		//convert to BSTR
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			switch (V_VT(v))
			{
			case VT_EMPTY:
			case VT_NULL:
			case VT_ERROR:
				return jnienv->NewStringUTF("null");
			}
			HRESULT hr;
			if (FAILED(hr = VariantChangeType(v, v, 0, VT_BSTR))) {
				return jnienv->NewStringUTF("null");
			}
			BSTR bs = V_BSTR(v);
			char* utfString = convertWideCharsToMultiBytes((wchar_t*) bs);
			jstring js = jnienv->NewStringUTF((const char*) utfString);//dragon
			free(utfString);
			return js;
		}
		return NULL;
	}
	
	
	JNIEXPORT jobject JNICALL Java_com_ibm_di_automation_VARIANT_toDispatch
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {

//			logmsg("Converting toDispatch");
//			logmsgv(v);
			HRESULT hr;
			if (FAILED(hr = VariantChangeType(v, v, 0, VT_DISPATCH))) {
				Throw(jnienv, "Cannot convert to dispatch", hr);
				return NULL;
			}
			jclass dispCls = jnienv->FindClass("com/ibm/di/automation/IDispatch");
			jmethodID dispCtor = jnienv->GetMethodID(dispCls, "<init>", "(I)V");
    
			IDispatch *pDisp = V_DISPATCH(v);
			
			if (NULL != pDisp) pDisp->AddRef();
			
			jobject newAuto = jnienv->NewObject(dispCls, dispCtor, pDisp);
			return newAuto;
		}
//		logmsg("cannot convert to Dispatch");
		return NULL;
	}
	
	
	JNIEXPORT jbyte JNICALL Java_com_ibm_di_automation_VARIANT_toByte
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			HRESULT hr;
			if (FAILED(hr = VariantChangeType(v, v, 0, VT_UI1))) {
				Throw(jnienv, "Cannot convert to date", hr);
				return NULL;
			}
			return (jbyte)V_UI1(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jint JNICALL Java_com_ibm_di_automation_VARIANT_toError
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			HRESULT hr;
			if (FAILED(hr = VariantChangeType(v, v, 0, VT_ERROR))) {
				Throw(jnienv, "Cannot convert to date", hr);
				return NULL;
			}
			return (jint)V_ERROR(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jfloat JNICALL Java_com_ibm_di_automation_VARIANT_toFloat
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			HRESULT hr;
			if (FAILED(hr = VariantChangeType(v, v, 0, VT_R4))) {
				Throw(jnienv, "Cannot convert to date", hr);
				return NULL;
			}
			return (jfloat)V_R4(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jobject JNICALL Java_com_ibm_di_automation_VARIANT_toSafeArray
		(JNIEnv *jnienv, jobject jthis, jboolean)
	{
		Throw(jnienv, "Not yet implemented", -1);
		return NULL;
	}
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putBSTR
		(JNIEnv *jnienv, jobject jthis, jobject bstr)
	{
		try{
			VARIANT *v = getVARIANT(jnienv, jthis);
			if (NULL != v) {
				BSTR b= getBSTR(jnienv, bstr);
				USES_CONVERSION;
				if(b==NULL)
					Throw(jnienv,"BSTR null",0);
				
				V_VT(v) = VT_BSTR;
				V_BSTR(v) = SysAllocString(b);
				logmsg("initializing variant with BSTR");
				logmsgv(v);

			}
		}
		catch(...)
		{
			Throw(jnienv,"Cannot initialize Variant with BSTR",0);
		}
	}
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putShortRef
		(JNIEnv *jnienv, jobject jthis, jshort s)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			short *ps = (short *)CoTaskMemAlloc(sizeof(short));
			*ps = s;
			V_VT(v) = VT_I2|VT_BYREF;
			V_I2REF(v) = ps;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putIntRef
		(JNIEnv *jnienv, jobject jthis, jint i)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			long *pi = (long *)CoTaskMemAlloc(sizeof(long));
			*pi = i;
			V_VT(v) = VT_I4|VT_BYREF;
			V_I4REF(v) = pi;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putDoubleRef
		(JNIEnv *jnienv, jobject jthis, jdouble d)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			double *pd = (double *)CoTaskMemAlloc(sizeof(double));
			*pd = d;
			V_VT(v) = VT_R8|VT_BYREF;
			V_R8REF(v) = pd;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putDateRef
		(JNIEnv *jnienv, jobject jthis, jdouble dt)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			double *pd = (double *)CoTaskMemAlloc(sizeof(double));
			*pd = dt;
			V_VT(v) = VT_DATE|VT_BYREF;
			V_DATE(v) = *pd;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putStringRef
		(JNIEnv *jnienv, jobject jthis, jstring str)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			jclass strcls = jnienv->FindClass("java/lang/String");
			jmethodID getBytes = jnienv->GetMethodID(strcls, "getBytes", "()[B");
			jbyteArray ba = (jbyteArray)jnienv->CallObjectMethod(str, getBytes);
			int len = jnienv->GetArrayLength(ba);
			jbyte* buf = (jbyte*)alloca(len + 1);
			jnienv->GetByteArrayRegion(ba, 0, len, buf);
			buf[len] = '\0';
			
			CComBSTR bs((char*)buf);
			
			BSTR *pbs = (BSTR *)CoTaskMemAlloc(sizeof(BSTR));
			bs.CopyTo(pbs);
			V_VT(v) = VT_BSTR|VT_BYREF;
			V_BSTRREF(v) = pbs;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putShort
		(JNIEnv *jnienv, jobject jthis, jshort s)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			short *ps = (short*)CoTaskMemAlloc(sizeof(short));
			*ps = s;
			V_VT(v) = VT_I2;
			V_I2(v) = *ps;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putInt
		(JNIEnv *jnienv, jobject jthis, jint i)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			long *pi = (long*)CoTaskMemAlloc(sizeof(long));
			*pi = i;
			V_VT(v) = VT_I4;
			V_I4(v) = *pi;
		}
	}
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putLong
		(JNIEnv *jnienv, jobject jthis, jlong l)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			long long *pi = (long long*)CoTaskMemAlloc(sizeof(long long));
			*pi = l;
			V_VT(v) = VT_I8;
			V_I8(v) = *pi;
		}
	}
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putDate
		(JNIEnv *jnienv, jobject jthis, jdouble dt)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			double *pd = (double*)CoTaskMemAlloc(sizeof(double));
			*pd = dt;
			V_VT(v) = VT_R8;
			V_R8(v) = *pd;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putBoolean
		(JNIEnv *jnienv, jobject jthis, jboolean b)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			V_VT(v) = VT_BOOL;
			V_BOOL(v) = b == JNI_TRUE ? VARIANT_TRUE : VARIANT_FALSE;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putByte
		(JNIEnv *jnienv, jobject jthis, jbyte byte)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			V_VT(v) = VT_UI1;
			V_UI1(v) = byte;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putEmpty
		(JNIEnv *jnienv, jobject jthis)
	{
		
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putError
		(JNIEnv *jnienv, jobject jthis, jint i)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			V_VT(v) = VT_ERROR;
			V_ERROR(v) = (int)i;
		}
	}
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putNoParam
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			V_VT(v) = VT_ERROR;
			V_ERROR(v) = DISP_E_PARAMNOTFOUND;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putCurrency
		(JNIEnv *jnienv, jobject jthis, jlong cy)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			V_VT(v) = VT_CY;
			V_CY(v).int64 = (LONGLONG)cy;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putObject
		(JNIEnv *jnienv, jobject jthis, jobject jobj)
	{
		
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putDouble
		(JNIEnv *jnienv, jobject jthis, jdouble d)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			double *pd = (double*)CoTaskMemAlloc(sizeof(double));
			*pd = d;
			V_VT(v) = VT_R8;
			V_R8(v) = *pd;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putFloatRef
		(JNIEnv *jnienv, jobject jthis, jfloat f)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			float *pf = (float*)CoTaskMemAlloc(sizeof(float));
			*pf = f;
			V_VT(v) = VT_R4|VT_BYREF;
			V_R4(v) = *pf;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putCurrencyRef
		(JNIEnv *jnienv, jobject jthis, jlong cy)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			V_VT(v) = VT_CY | VT_BYREF;
			V_CY(v).int64 = cy;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putErrorRef
		(JNIEnv *jnienv, jobject jthis, jint s)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			int *ps = (int*)CoTaskMemAlloc(sizeof(int));
			*ps = s;
			V_VT(v) = VT_I2|VT_BYREF;
			V_I2(v) = *ps;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putBooleanRef
		(JNIEnv *jnienv, jobject jthis, jboolean b)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			V_VT(v) = VT_BOOL | VT_BYREF;
			V_BOOL(v) = b == JNI_TRUE ? VARIANT_TRUE : VARIANT_FALSE;
		}
	}
	
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putByteRef
		(JNIEnv *jnienv, jobject jthis, jbyte byte)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			V_VT(v) = VT_UI1 | VT_BYREF;
			V_UI1(v) = byte;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putString
		(JNIEnv *jnienv, jobject jthis, jstring str)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			jclass strcls = jnienv->FindClass("java/lang/String");
			jmethodID getBytes = jnienv->GetMethodID(strcls, "getBytes", "()[B");
			jbyteArray ba = (jbyteArray)jnienv->CallObjectMethod(str, getBytes);
			int len = jnienv->GetArrayLength(ba);
			jbyte* buf = (jbyte*)alloca(len + 1);
			jnienv->GetByteArrayRegion(ba, 0, len, buf);
			buf[len] = '\0';
			
//			logmsg("inside VARIANT::putString");
//			if (buf!= NULL )logmsg((char*)buf);

			CComBSTR pbs((char*)buf);

			V_VT(v) = VT_BSTR;
			V_BSTR(v) = pbs.Copy();
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putNull
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			V_VT(v) = VT_NULL;
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_getNull
		(JNIEnv *jnienv, jobject jthis)
	{
		
	}
	
	
	JNIEXPORT jshort JNICALL Java_com_ibm_di_automation_VARIANT_getShortRef
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v && V_VT(v) ==  VT_I2) {
			return (jshort)V_I2REF(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jint JNICALL Java_com_ibm_di_automation_VARIANT_getIntRef
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v && V_VT(v) ==  VT_I4) {
			return (jint)V_I4REF(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jshort JNICALL Java_com_ibm_di_automation_VARIANT_getShort
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v && V_VT(v) ==  VT_I2 ) {
			return (jshort)V_I2(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jdouble JNICALL Java_com_ibm_di_automation_VARIANT_getDoubleRef
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v && V_VT(v) ==  VT_R8 ) {
			return (double)*V_R8REF(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jdouble JNICALL Java_com_ibm_di_automation_VARIANT_getDateRef
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v && V_VT(v) ==  VT_DATE ) {
			return (jdouble)V_DATE(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jstring JNICALL Java_com_ibm_di_automation_VARIANT_getStringRef
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			if (V_VT(v) ==  (VT_BSTR|VT_BYREF)) {
				BSTR *bs = V_BSTRREF(v);
				char* utfString = convertWideCharsToMultiBytes((wchar_t*) *bs);
				jstring js = jnienv->NewStringUTF((const char*) utfString);//dragon
				free(utfString);
				return js;
			}
		}
		return NULL;
	}
	
	
	JNIEXPORT jint JNICALL Java_com_ibm_di_automation_VARIANT_getInt
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v && V_VT(v) ==  VT_I4 ) {
			return (jint)V_I4(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jdouble JNICALL Java_com_ibm_di_automation_VARIANT_getDate
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v && V_VT(v) ==  VT_DATE ) {
			return (jdouble)V_DATE(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jboolean JNICALL Java_com_ibm_di_automation_VARIANT_getBoolean
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v && V_VT(v) ==  VT_BOOL ) {
			return (jboolean)V_BOOL(v) == VARIANT_TRUE ? JNI_TRUE : JNI_FALSE;
		}
		return NULL;
	}
	
	
	JNIEXPORT jbyte JNICALL Java_com_ibm_di_automation_VARIANT_getByte
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v && V_VT(v) ==  VT_UI1) {
			return (jbyte) V_UI1(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_getEmpty
		(JNIEnv *jnienv, jobject jthis)
	{
		
	}
	
	
	JNIEXPORT jint JNICALL Java_com_ibm_di_automation_VARIANT_getError
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (v) {
			if (V_VT(v) ==  VT_ERROR) {
				return (jint)V_ERROR(v);
			}
		}
		return NULL;
	}
	
	
	JNIEXPORT jdouble JNICALL Java_com_ibm_di_automation_VARIANT_getDouble
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v && V_VT(v) ==  VT_R8 ) {
			return (double)V_R8(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jlong JNICALL Java_com_ibm_di_automation_VARIANT_getCurrency
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (v) {
			if (V_VT(v) ==  VT_CY) {
				return (jlong)V_CY(v).int64;
			}
		}
		return NULL;
	}
	
	
	JNIEXPORT jstring JNICALL Java_com_ibm_di_automation_VARIANT_getString
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			if (V_VT(v) ==  (VT_BSTR)) {
				BSTR bs = V_BSTR(v);
				char* utfString = convertWideCharsToMultiBytes((wchar_t*) bs);
				jstring js = jnienv->NewStringUTF((const char*) utfString);//dragon
				free(utfString);
				return js;
			}
		}
		return NULL;
	}
	
	
	JNIEXPORT jfloat JNICALL Java_com_ibm_di_automation_VARIANT_getFloatRef
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v && V_VT(v) ==  VT_R4) {
			return (float)*V_R4REF(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jlong JNICALL Java_com_ibm_di_automation_VARIANT_getCurrencyRef
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (v) {

			if (V_VT(v) ==  VT_CY) {
				return (jlong)V_CYREF(v);
			}
		}
		return NULL;
	}
	
	
	JNIEXPORT jint JNICALL Java_com_ibm_di_automation_VARIANT_getErrorRef
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (v) {
			if (V_VT(v) ==  VT_ERROR) {
				return (jint)V_ERRORREF(v);
			}
		}
		return NULL;
	}
	
	
	JNIEXPORT jboolean JNICALL Java_com_ibm_di_automation_VARIANT_getBooleanRef
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v && V_VT(v) ==  VT_BOOL ) {
			return (jboolean)V_BOOLREF(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jobject JNICALL Java_com_ibm_di_automation_VARIANT_getObjectRef
		(JNIEnv *jnienv, jobject jthis)
	{
		Throw(jnienv, "Not yet implemented", -1);
		return NULL;
	}
	
	
	JNIEXPORT jbyte JNICALL Java_com_ibm_di_automation_VARIANT_getByteRef
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v && V_VT(v) ==  VT_UI1) {
			return (jbyte) V_UI1REF(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putSafeArrayRef
		(JNIEnv *jnienv, jobject jthis, jobject)
	{
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putSafeArray
		(JNIEnv *jnienv, jobject jthis, jobject)
	{
	}
	
	
	JNIEXPORT jfloat JNICALL Java_com_ibm_di_automation_VARIANT_getFloat
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v && V_VT(v) ==  VT_R4) {
			return (float)V_R4(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_putFloat
		(JNIEnv *jnienv, jobject jthis, jfloat f)
	{
		VARIANT *v = getVARIANT(jnienv,jthis);
		if(NULL != v){
			float *pf = (float*)CoTaskMemAlloc(sizeof(float));
			*pf = f;
			V_VT(v) = VT_R4;
			V_R4(v) = *pf;
		}
	}
	
	
	JNIEXPORT jshort JNICALL Java_com_ibm_di_automation_VARIANT_getvt
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			return (jshort)V_VT(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT jshort JNICALL Java_com_ibm_di_automation_VARIANT_toShort
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			HRESULT hr = VariantChangeType(v, v, 0, VT_I2);
			if (FAILED(hr)) {
				Throw(jnienv, "Cannot convert to short", hr);
				return NULL;
			}
			return (jshort)V_I2(v);
		}
		return NULL;
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_release
		(JNIEnv *jnienv, jobject jthis)
	{
		jclass cls = jnienv->GetObjectClass(jthis);
		jfieldID jf = jnienv->GetFieldID(cls, "m_pVARIANT", "I");
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) {
			if (V_VT(v) == (VT_BYREF|VT_BSTR))
			{
				BSTR  *pBstr = V_BSTRREF(v);;
				if (*pBstr)
					SysFreeString(*pBstr);// release bstr
			}
		}
		VariantClear(v);
		delete v;
		jnienv->SetIntField(jthis, jf, (unsigned int)0);
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_init
		(JNIEnv *jnienv, jobject jthis)
	{
		jclass cls = jnienv->GetObjectClass(jthis);
		jfieldID jf = jnienv->GetFieldID( cls, "m_pVARIANT", "I");
		VARIANT *v = new VARIANT();
		VariantInit(v);
		jnienv->SetIntField(jthis, jf, (unsigned int)v);
	}
	
	
	JNIEXPORT jboolean JNICALL Java_com_ibm_di_automation_VARIANT_isNull
		(JNIEnv *jnienv, jobject jthis)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL==v) return JNI_TRUE;
		switch (V_VT(v))
		{
		case VT_EMPTY:
		case VT_NULL:
		case VT_ERROR:
			return JNI_TRUE;
		}
		return JNI_FALSE;
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_Save
		(JNIEnv *jnienv, jobject jthis, jobject ostream)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL != v) 
		{
			DWORD flags = MSHCTX_LOCAL;
			jint size = VARIANT_UserSize(&flags, 0L, v);
			
			jbyte* pBuf = new jbyte[size];
			ZeroMemory(pBuf, size);
			
			VARIANT_UserMarshal(&flags, (unsigned char *)pBuf, v);
			jbyteArray ba = jnienv->NewByteArray(size);
			jnienv->SetByteArrayRegion(ba, 0, size, pBuf);
			delete [] pBuf;
			
			jclass ostreamCls = jnienv->FindClass("java/io/DataOutputStream");
			jmethodID ostreamCtor =
				jnienv->GetMethodID(ostreamCls, "<init>", "(Ljava/io/OutputStream;)V");
			jmethodID ostreamWriteInt =
				jnienv->GetMethodID(ostreamCls, "writeInt", "(I)V");
			jmethodID ostreamWriteBytes =
				jnienv->GetMethodID(ostreamCls, "write", "([B)V");
			jobject dos = jnienv->NewObject(ostreamCls, ostreamCtor, ostream);
			
			jnienv->CallVoidMethod(dos, ostreamWriteInt, size);
			jnienv->CallVoidMethod(dos, ostreamWriteBytes, ba);
		}
	}
	
	
	JNIEXPORT void JNICALL Java_com_ibm_di_automation_VARIANT_Load
		(JNIEnv *jnienv, jobject jthis, jobject istream)
	{
		VARIANT *v = getVARIANT(jnienv, jthis);
		if (NULL == v) 
		{
			jclass cls = jnienv->GetObjectClass(jthis);
			jfieldID jf = jnienv->GetFieldID( cls, "m_pVARIANT", "I");
			v = new VARIANT();
			VariantInit(v);
			jnienv->SetIntField(jthis, jf, (unsigned int)v);
		}
		if (NULL != v) 
		{
			jclass istreamCls = jnienv->FindClass("java/io/DataInputStream");
			jmethodID istreamCtor =
				jnienv->GetMethodID(istreamCls, "<init>", "(Ljava/io/InputStream;)V");
			jmethodID istreamReadInt =
				jnienv->GetMethodID(istreamCls, "readInt", "()I");
			jmethodID istreamReadBytes =
				jnienv->GetMethodID(istreamCls, "readFully", "([B)V");
			jobject dis = jnienv->NewObject(istreamCls, istreamCtor, istream);
			
			jint size = jnienv->CallIntMethod(dis, istreamReadInt);
			jbyteArray ba = jnienv->NewByteArray(size);
			jnienv->CallVoidMethod(dis, istreamReadBytes, ba);
			jbyte *pBuf = jnienv->GetByteArrayElements(ba, 0);
			
			DWORD flags = MSHCTX_LOCAL;
			VARIANT_UserUnmarshal(&flags, (unsigned char *)pBuf, v);
			jnienv->ReleaseByteArrayElements(ba, pBuf, 0);
		}
	}
	
}
