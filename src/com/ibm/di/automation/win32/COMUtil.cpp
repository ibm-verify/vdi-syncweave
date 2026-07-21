/*
 * IBM Confidential
 *
 *  OCO Source Materials
 *
 * 5724-D49
 *
 * (C) Copyright IBM Corporation. 2006
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

#include "COMUtil.h"
#include <iostream>
#include <jni.h>
#include <sstream>
#include <string>
using namespace std;

extern "C" 
{
	void Throw(JNIEnv *jnienv, const char* desc, jint hr)
	{

		jclass failClass = jnienv->FindClass("com/ibm/di/automation/COMError");
		
		logmsg("COMError found");
		LPTSTR s;
			if(::FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
				FORMAT_MESSAGE_FROM_SYSTEM,	NULL, hr, 0, (LPTSTR)&s, 0, NULL)!=0)
			{
				logmsgw((wchar_t*)s);
			}
		char hrStr[15];
		logmsg(_itoa(hr,hrStr,10));
		
		logmsg((char*)desc);
		
		if (!desc) desc = "Java/COM Error";
	
		if (failClass != 0)
			jnienv->ThrowNew(failClass, desc);
	}
	
	IDispatch* getDispatch(JNIEnv *jnienv, jobject arg)
	{
		//logmsg("in getDispatch\n");
		try{
			if(arg == 0) Throw(jnienv, "object null", -1);
			jclass argClass = jnienv->GetObjectClass(arg);
			
			//jclass cl = jnienv->FindClass("com/ibm/di/automation/IDispatch"); 
			
			jfieldID id = jnienv->GetFieldID( argClass, "m_pIDispatch" , "I");
			if(id==NULL)
				Throw(jnienv, "m_pIDispatch field not found",-1);
		
			jint i = jnienv->GetIntField(arg, id);
			if(i==0) Throw(jnienv, "IDispatch not initialized", -1);
			IDispatch *v = (IDispatch*)i;
			return v;
		}
		catch(...)
		{
			Throw(jnienv, "NoSuchFieldError\n", -1);
			return NULL;
		}
	}
	
	
	VARIANT* getVARIANT(JNIEnv *jnienv, jobject arg)
	{
		jclass argClass = jnienv->GetObjectClass(arg);
		try
		{	jfieldID id = jnienv->GetFieldID( argClass, "m_pVARIANT" , "I");
			jint i = jnienv->GetIntField(arg, id);
			VARIANT *v = (VARIANT*)i;
			if(v==NULL) Throw(jnienv, "getVARIANT is null",-1);
			return v;
		}
		catch(...)
		{
			Throw(jnienv, "some error while getting variant", -1);
			return NULL;
		}
	}

	IUnknown* getUnknown(JNIEnv *jnienv, jobject arg)
	{
		jclass argClass = jnienv->GetObjectClass(arg);
		jfieldID id = jnienv->GetFieldID( argClass, "m_pIUnknown" , "I");
		jint i = jnienv->GetIntField(arg, id);
		IUnknown *v = (IUnknown*)i;
		return v;
	}

	BSTR getBSTR(JNIEnv *jnienv, jobject arg)
	{
		jclass argClass = jnienv->GetObjectClass(arg);
		jfieldID id = jnienv->GetFieldID( argClass, "m_pBSTR" , "I");
		jint i = jnienv->GetIntField(arg, id);
		BSTR v = (BSTR)i;
		return v;
	}

	void logmsg(char* msg)
	{
		FILE *fptr = fopen("COMProxy.log","a+");

		if(fptr!=NULL)
		{
			fputs("\n",fptr);
			fputs(msg,fptr);
			fputs("\n",fptr);
		
			fflush(fptr);
			fclose(fptr);
		}
	}

	void logmsgw(wchar_t* msg)
	{
		FILE *fptr = fopen("COMProxy.log"," a+");

		if(fptr != NULL) 
		{
			fputws(L"\n", fptr);
			fputws(msg, fptr);
			fputws(L"\n", fptr);
	
			fflush(fptr);
			fclose(fptr);
		}
	}

	void logmsgv(VARIANT *v){
		char *str = new char[4];
		logmsg("VARIANT TYPE =");
		logmsg(_itoa(v->vt,str,10));
		switch(V_VT(v)){
		case VT_I2:	logmsg("short value =");
					logmsg(_itoa(V_I2(v),str,10));
					break;
		case VT_I4:	logmsg("int value =");
					logmsg(_itoa(V_I4(v),str,10));
					break;
		case VT_R4:	logmsg("float value =");
				//	logmsg((char*)v->fltVal);
					break;
		case VT_R8:	logmsg("int value =");
					//logmsg((char*)v->dblVal);
					break;
		case VT_BSTR :
			logmsg("string value =");
			USES_CONVERSION;
			logmsg(W2A(v->bstrVal));
		}
		delete []str;
	}
	
	
	char* convertWideCharsToMultiBytes(wchar_t* wcString) {
		size_t charCount = wcslen((const wchar_t*) wcString);
		int mbLength = WideCharToMultiByte(CP_UTF8, 0, wcString, charCount, NULL, 0, NULL, NULL);
		char *utfString = (char *) malloc(mbLength + 1);
		int resultCode = WideCharToMultiByte(CP_UTF8, 0, wcString, charCount, utfString, mbLength, NULL, NULL);
		if(resultCode > 0) {
			utfString[mbLength] = '\0';
			return utfString;
		} else {
			return "";
		}
		
	}

	char* TypeDescToString(TYPEDESC* typeDesc, ITypeInfo* pTypeInfo)
	{
		string str ;
		ostringstream oss;

		USES_CONVERSION;

		switch(typeDesc->vt) {
		case VT_I2: return "short";
		case VT_I4: return "long";
		case VT_R4: return "float";
		case VT_R8: return "double";
		case VT_CY: return "CY";
		case VT_DATE: return "DATE";
		case VT_BSTR: return "BSTR";
		case VT_DISPATCH: return "IDispatch*";
		case VT_ERROR: return "SCODE";
		case VT_BOOL: return "VARIANT_BOOL";
		case VT_VARIANT: return "VARIANT";
		case VT_UNKNOWN: return "IUnknown*";
		case VT_UI1: return "BYTE";
		case VT_DECIMAL: return "DECIMAL";
		case VT_I1: return "char";
		case VT_UI2: return "USHORT";
		case VT_UI4: return "ULONG";
		case VT_I8: return "__int64";
		case VT_UI8: return "unsigned __int64";
		case VT_INT: return "int";
		case VT_UINT: return "UINT";
		case VT_HRESULT: return "HRESULT";
		case VT_VOID: return "void";
		case VT_LPSTR: return "char*";
		case VT_LPWSTR: return "wchar_t*";
		}
		
		if(typeDesc->vt == VT_PTR) {
			//str +="VT_PTR";
			char *str=TypeDescToString(typeDesc->lptdesc, pTypeInfo);
			char *newStr = new char[strlen(str)+1];
			strcpy(newStr,str);
			strcat(newStr,"*");
			
			return newStr;
		}
		
		if(typeDesc->vt == VT_SAFEARRAY) {
			char *str = "SAFEARRAY(";
			char *str1 = TypeDescToString(typeDesc->lptdesc, pTypeInfo);
			char *newStr = new char[strlen(str)+strlen(str1)+1];
			strcat(newStr,")");
			return newStr;
		}
		
		if(typeDesc->vt == VT_CARRAY) {
			
			char *str= TypeDescToString(&typeDesc->lpadesc->tdescElem, pTypeInfo);
			char *newStr = new char[strlen(str)+15];
			strcpy(newStr,str);
			for(int dim(0); typeDesc->lpadesc->cDims; ++dim) {
				strcat(newStr,"[");
				strcat(newStr,"...");
        		strcat(newStr,"]");
			}
			return newStr;
		}
	
		if(typeDesc->vt == VT_USERDEFINED) {
			return CustomTypeToString(typeDesc->hreftype, pTypeInfo);;
		}				
		return "I GIVE UP !!!";
	}

	char* CustomTypeToString(HREFTYPE refType, ITypeInfo* pti) {
		
		CComPtr<ITypeInfo> pTypeInfo(pti);
		CComPtr<ITypeInfo> pCustTypeInfo;
		
		HRESULT hr(pTypeInfo->GetRefTypeInfo(refType, &pCustTypeInfo));
		if(hr) return "UnknownCustomType";
		
		CComBSTR bstrType;
		
		hr = pCustTypeInfo->GetDocumentation(-1, &bstrType, 0, 0, 0);
		
		if(hr) return "UnknownCustomType";
		
		char *ansiType = new char[MAX_PATH];
		
		WideCharToMultiByte(CP_ACP, 0, bstrType, bstrType.Length() + 1, 
			ansiType, MAX_PATH, 0, 0);
		
		return ansiType;
			
	}
}
