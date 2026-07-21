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

#pragma warning (disable: 4100)
#include "IDispatch.h"
#include "COMUtil.h"
#include <iostream>
#include <jni.h>
#include <string>
using namespace std;

extern "C"
{


	#define SETDISPPARAMS(dp, numArgs, pvArgs, numNamed, pNamed) \
			{\
			   (dp).cArgs  = numArgs; \
			   (dp).rgvarg = pvArgs; \
			   (dp).cNamedArgs = numNamed; \
			   (dp).rgdispidNamedArgs = pNamed; \
			}


	JNIEXPORT jobject JNICALL Java_com_ibm_di_automation_IDispatch_QueryInterface
	  (JNIEnv *jnienv, jobject jthis, jstring jiid)
	{
		 // get the current IDispatch
		IDispatch *pIDispatch = getDispatch(jnienv,jthis);
		
		if (!pIDispatch) return NULL;
		
  		const char *siid = jnienv->GetStringUTFChars(jiid, NULL);
  		USES_CONVERSION;
  		LPOLESTR bsIID = A2W(siid);
  		jnienv->ReleaseStringUTFChars(jiid, siid);
  		IID iid;
  		HRESULT hr = IIDFromString(bsIID, &iid);
  		if (FAILED(hr)) {
    			Throw(jnienv, "Can't get IID from String", hr);
    			return NULL;
  		}

  		IDispatch *disp;
  		hr = pIDispatch->QueryInterface(iid, (void **)&disp);
  		if (FAILED(hr)) {
			Throw(jnienv, "QI on IID from String Failed", hr);

   			 return NULL;
  		}

  		jclass autoClass = jnienv->FindClass("com/ibm/di/automation/IDispatch");
  		jmethodID autoCons =
  		jnienv->GetMethodID(autoClass, "<init>", "(I)V");

		if (disp) disp->AddRef();
 		jobject newAuto = jnienv->NewObject(autoClass, autoCons, disp);
  		return newAuto;
	}



	JNIEXPORT void JNICALL Java_com_ibm_di_automation_IDispatch_createInstance
	(JNIEnv *jnienv, jobject jthis, jstring progID)
	{
		CoInitialize(NULL); 

		jclass cls = jnienv->GetObjectClass(jthis);
		jfieldID jf = jnienv->GetFieldID( cls, "m_pIDispatch", "I");
	

		const char *progid = jnienv->GetStringUTFChars(progID, NULL);
		logmsg((char*)progid);

		CLSID clsid;
		HRESULT hr;
		IUnknown *ppv = NULL;
		IDispatch *pIDispatch;
		USES_CONVERSION;


		LPOLESTR bsProgId = A2W(progid);

		
		

		//pattern match - if progid is a moniker
		if (strchr(progid,':')) 
		{
			jnienv->ReleaseStringUTFChars(progID, progid);
			logmsg("Its a moniker !! PROGID = ");
			logmsg((char*)progid);
			// it's a moniker
			hr = CoGetObject(bsProgId, NULL, IID_IUnknown, (LPVOID *)&ppv);
			if (FAILED(hr)) {
				Throw(jnienv, "Can't find moniker", hr);
				return;
			}

			IClassFactory *pIClass;
			//get the class factory
			hr = ppv->QueryInterface(IID_IClassFactory, (void **)&pIClass);
			if (SUCCEEDED(hr))
			{
				ppv->Release();

				// try to create an instance
				hr = pIClass->CreateInstance(NULL, IID_IUnknown, (void **)&ppv);
				if (FAILED(hr)) {
					Throw(jnienv, "Can't create moniker class instance", hr);
					return;
				}
				pIClass->Release();
			}
		}
		else
		{
			jnienv->ReleaseStringUTFChars(progID, progid);
			hr = CLSIDFromProgID(bsProgId, &clsid);
			if (FAILED(hr)) {

				LPTSTR s;
					if(::FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
						FORMAT_MESSAGE_FROM_SYSTEM, NULL, hr, 0, (LPTSTR)&s, 0, NULL)!=0)
					{
						logmsgw((wchar_t*) s);
					}
				Throw(jnienv, "Cannot get clsid from progid", hr);
				return;
			}
			hr = CoCreateInstance(clsid,NULL,CLSCTX_LOCAL_SERVER|CLSCTX_INPROC_SERVER,IID_IUnknown, (void **)&ppv);
			if (!SUCCEEDED(hr)) {
				Throw(jnienv, "CoCreateInstance failed", hr);
				return;
			}
		}		
		hr = ppv->QueryInterface(IID_IDispatch, (void **)&pIDispatch);
		if (!SUCCEEDED(hr)) {
			Throw(jnienv, "QueryInterface for IDispatch failed", hr);
			hr = ppv->QueryInterface(IID_IUnknown, (void **)&pIDispatch);
			if (!SUCCEEDED(hr)) {
				Throw(jnienv, "QueryInterface for IDispatch failed", hr);
				return;
			}
		}
		ppv->Release();

		jnienv->SetIntField(jthis, jf, (unsigned int)pIDispatch); 

	}



	JNIEXPORT void JNICALL Java_com_ibm_di_automation_IDispatch_release
	(JNIEnv *jnienv, jobject jthis)
	{
		jclass cls = jnienv->GetObjectClass(jthis);
		jfieldID jf = jnienv->GetFieldID( cls, "m_pIDsipatch", "I");
		
		IDispatch *disp = getDispatch(jnienv,jthis);
		if (disp) {
			disp->Release();
			jnienv->SetIntField(jthis, jf, (unsigned int)0);
		}
	}



	JNIEXPORT jintArray JNICALL Java_com_ibm_di_automation_IDispatch_getIDsOfNames
	  (JNIEnv *jnienv, jclass jthis, jobject disp, jint lcid, jobjectArray names)
	{	
		int i;
		IDispatch *pIDispatch = getDispatch(jnienv,disp);

		if (!pIDispatch) return NULL;
		
		int len = jnienv->GetArrayLength(names);

		LPOLESTR *lps = (LPOLESTR *)CoTaskMemAlloc(len * sizeof(LPOLESTR));
		DISPID *dispid = (DISPID *)CoTaskMemAlloc(len * sizeof(DISPID));
		
		for(i=0;i<len;i++) 
		{
			USES_CONVERSION;
			jstring s = (jstring)jnienv->GetObjectArrayElement(names, i);
			char temp[64];
			if(s==NULL)
			{
				_snprintf(temp, 64, "Function name at index %d null", i);
				Throw(jnienv,temp,-1);
				return NULL;
			}
			const char *str = jnienv->GetStringUTFChars(s, NULL);
			LPOLESTR olestr = A2W(str);
			jnienv->ReleaseStringUTFChars(s, str);
			lps[i] = olestr;
			jnienv->DeleteLocalRef(s);
		}

		HRESULT hr = pIDispatch->GetIDsOfNames(IID_NULL,lps,len,lcid,dispid);
		
		if (FAILED(hr)) {
			Throw(jnienv, "GetIDsOfNames failed", hr);
			CoTaskMemFree(lps);
			CoTaskMemFree(dispid);
			
			char buf[1024];
			
			strcpy(buf, "Can't map names to dispid:");
			
			for(i=0;i<len;i++) 
			{
				USES_CONVERSION;
				jstring s = (jstring)jnienv->GetObjectArrayElement(names, i);
				const char *nm = jnienv->GetStringUTFChars(s, NULL);
				strcat(buf, nm);
				jnienv->ReleaseStringUTFChars(s, nm);
				jnienv->DeleteLocalRef(s);
			}
			Throw(jnienv, buf, hr);
			return NULL;
		}
		jintArray iarr = jnienv->NewIntArray(len);
		jnienv->SetIntArrayRegion(iarr, 0, len, (jint*) dispid);
		CoTaskMemFree(lps);
		CoTaskMemFree(dispid);
		return iarr;
	}



	JNIEXPORT jobject JNICALL Java_com_ibm_di_automation_IDispatch_invokev
	(JNIEnv *jnienv, jclass jthis, jobject disp, jstring name, jint dispID, jint lcid, jint wFlags, jobjectArray vArg, jintArray uArgErr)
	{
			DISPPARAMS  dispparams;
			EXCEPINFO   excepInfo;
			
//			logmsg("inside invoke");
//			logmsg((char*)jnienv->GetStringUTFChars(name, NULL));
			
			IDispatch *pIDispatch = getDispatch(jnienv,disp);

			if (!pIDispatch) return NULL;

//			logmsg("got dispatch!!");
			
			long dispid = dispID;
			if (name != NULL) 
			{
				const char *nm = jnienv->GetStringUTFChars(name, NULL);
				USES_CONVERSION;
				LPOLESTR propOle = A2W(nm);
				HRESULT hr = pIDispatch->GetIDsOfNames(IID_NULL,(LPOLESTR*)&propOle,1,lcid,&dispid);
				
				if (FAILED(hr)) {

					char buf[512];
					_snprintf(buf, 512, "Can't map name to dispid: %s", nm);
					logmsg(buf);
					LPTSTR s;
					if(::FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
						FORMAT_MESSAGE_FROM_SYSTEM,	NULL, hr, 0, (LPTSTR)&s, 0,	NULL)!=0)
					{
						logmsgw((wchar_t*) s);
					}
					Throw(jnienv, buf, -1);
					return NULL;
				}
				jnienv->ReleaseStringUTFChars(name, nm);
			}
					
			int num_args = jnienv->GetArrayLength(vArg);
			VARIANT *varr = NULL;
			if (num_args) 
			{
				varr = (VARIANT *)CoTaskMemAlloc(num_args*sizeof(VARIANT));
				/*defect 4260 reversing the VARRIANT array*/
				for(int i=num_args-1,j=0;0<=i;i--,j++) 
				{
					VariantInit(&varr[j]);
					jobject arg = jnienv->GetObjectArrayElement(vArg, i);
					
					VARIANT *v = getVARIANT(jnienv,arg);
					
					logmsg("VARARG");
					logmsgv(v);

					VariantCopy(&varr[j], v);
					jnienv->DeleteLocalRef(arg);
				}

				
			}	
			
			// prepare a new return value
			jclass variantClass = jnienv->FindClass("com/ibm/di/automation/VARIANT");
			jmethodID variantCons = 
				jnienv->GetMethodID(variantClass, "<init>", "()V");
			// construct a variant to return
			jobject newVariant = jnienv->NewObject(variantClass, variantCons);
			// get the VARIANT from the newVariant
			VARIANT *ret = getVARIANT(jnienv, newVariant);
			DISPID  dispidProperty = DISPID_PROPERTYPUT;
			
			// determine how to dispatch
			switch (wFlags) 
			{
			case DISPATCH_PROPERTYGET: // GET  
			case DISPATCH_METHOD: // METHOD
			case DISPATCH_METHOD|DISPATCH_PROPERTYGET:
				{
//					logmsg("in DISPATCH_PROPERTYGET");
					SETDISPPARAMS(dispparams, num_args, varr, 0, (DISPID*)&wFlags);
					break;
				}
			case DISPATCH_PROPERTYPUT:
				{
//					logmsg("in DISPATCH_PROPERTYPUT");
					SETDISPPARAMS(dispparams, num_args, varr, 1, &dispidProperty);
					break;
				}
			}
			
			jint *uAE = jnienv->GetIntArrayElements(uArgErr, NULL);
			HRESULT hr = pIDispatch->Invoke(dispid,IID_NULL,
				lcid,wFlags,&dispparams,ret,&excepInfo,(unsigned int *)uAE);

//			logmsg("just before putting vector");
//			logmsgv(ret);
			if (FAILED(hr)) {
				logmsg("INVOKE FAILED");
				if (excepInfo.bstrDescription != NULL)
					logmsgw((wchar_t*)excepInfo.bstrDescription);
				LPTSTR s;
				if(::FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
					FORMAT_MESSAGE_FROM_SYSTEM,	NULL, hr, 0, (LPTSTR)&s, 0, NULL)!=0)
				{
					logmsgw((wchar_t*)s);
				}
				logmsg("FAILED");
				char buf[512];
				const char* nameStr = jnienv->GetStringUTFChars(name, NULL);
				if (excepInfo.bstrDescription != NULL)
					_snprintf(buf, 512, "Can't invoke: %s with dispID %d: %S", nameStr, dispid, excepInfo.bstrDescription);
				else
					_snprintf(buf, 512, "Can't invoke: %s with dispID %d", nameStr, dispid);
				jnienv->ReleaseStringUTFChars(name, NULL);
				Throw(jnienv, buf, -1);
				return NULL;
			}
//			logmsg("ReleaseIntArrayElements");
			jnienv->ReleaseIntArrayElements(uArgErr, uAE, 0);
//			logmsg("num_args = " + num_args);			
			if (num_args) 
			{
				//this is for in out type vars
				int i,j;
				for(i=num_args-1,j=0;0<=i;i--,j++) 
				{
					jobject arg = jnienv->GetObjectArrayElement(vArg, i);
					VARIANT *var = getVARIANT(jnienv, arg);
					// reverse copy
					VariantCopy(var, &varr[j]);
					// clear out the temporary variant
					VariantClear(&varr[j]);
					jnienv->DeleteLocalRef(arg);
				}
			}
			
			if (varr) CoTaskMemFree(varr);
			
			return newVariant;		
	}

	JNIEXPORT jstring JNICALL Java_com_ibm_di_automation_IDispatch_CLSIDfromProgID
		(JNIEnv *jnienv, jclass jthis, jstring jprogid)
	{
		const char *siid = jnienv->GetStringUTFChars(jprogid, NULL);

//		logmsg("Inside CLSIDfromProgID");
//		logmsg((char*)siid);

  		USES_CONVERSION;
  		
		LPOLESTR bsIID = A2W(siid);
		LPCLSID clsid=new _GUID();
		::CLSIDFromProgID(bsIID, clsid);
		
		LPOLESTR lpsz ;

		::StringFromCLSID(*clsid,&lpsz);
		jnienv->ReleaseStringUTFChars(jprogid, siid);
		
		const char *sCLSID = W2A(lpsz);

//		logmsg((char*)sCLSID);
		delete clsid;
		return jnienv->NewStringUTF(sCLSID);
	}
	
	JNIEXPORT jobject JNICALL Java_com_ibm_di_automation_IDispatch_enumMethods
		(JNIEnv *jnienv, jclass jthis, jobject disp)
	{
		string str;
		IDispatch *pIDispatch = getDispatch(jnienv,disp);
		if (!pIDispatch) return NULL;
		
		ITypeInfo *pTypeInfo ;
		
		HRESULT hr = pIDispatch->GetTypeInfo(0,0,&pTypeInfo);
		if(hr) { return NULL; }
		
		TYPEATTR* typeAttr =NULL;
		
		pTypeInfo->GetTypeAttr(&typeAttr);
		
		CComBSTR iName;
		
		hr = pTypeInfo->GetDocumentation(-1, &iName, 0, 0, 0);
		
		USES_CONVERSION;
		if(hr) Throw(jnienv,"Unknown default interface:\n",-1);
				
		//create the vector to return
		jclass autoClass = jnienv->FindClass("java/util/Vector");

  		jmethodID autoCons = jnienv->GetMethodID(autoClass, "<init>", "(I)V");

		jobject newAuto = jnienv->NewObject(autoClass, autoCons, typeAttr->cFuncs);
		jmethodID midAddElement = jnienv->GetMethodID(autoClass, "addElement", "(Ljava/lang/Object;)V");
		if (jnienv->ExceptionOccurred()) return newAuto;
		
		


		for(UINT curFunc=0; curFunc < typeAttr->cFuncs; ++curFunc) {
			
			FUNCDESC* funcDesc;
			hr = pTypeInfo->GetFuncDesc(curFunc, &funcDesc);
			CComBSTR methodName;
			hr |= pTypeInfo->GetDocumentation(funcDesc->memid, &methodName, 0, 0, 0);
			if(hr) { Throw(jnienv, "Error In Name",-1); 
            pTypeInfo->ReleaseFuncDesc(funcDesc); continue; }
			
			str = '\t';
			str+= TypeDescToString(&funcDesc->elemdescFunc.tdesc,pTypeInfo) ;
			str+=  ' ';
			USES_CONVERSION;
			str+= W2A(methodName.m_str);
			
			str+= "(";
			for(UINT curParam(0); curParam < (unsigned)funcDesc->cParams; ++curParam) {
				str+= TypeDescToString(
					&funcDesc->lprgelemdescParam[curParam].tdesc, pTypeInfo);
				if(curParam < (unsigned)funcDesc->cParams - 1) str+= ", ";
			}
			str+= ')';
			switch(funcDesc->invkind) {
			case INVOKE_PROPERTYGET: str+= " propget"; break;
			case INVOKE_PROPERTYPUT: str+= " propput"; break;
			case INVOKE_PROPERTYPUTREF: str+= " propputref"; break;
			}
			pTypeInfo->ReleaseFuncDesc(funcDesc);
//			logmsg((char*)str.c_str());
			char* s = (char*)str.c_str();
			CComBSTR bs(s);
			jstring js = jnienv->NewStringUTF(str.c_str());//dragon

			jnienv->CallVoidMethod(newAuto, midAddElement, js);
			if (jnienv->ExceptionOccurred()) return newAuto;
			
//			logmsg("added  to vector");
			
		}
		return newAuto;
	}

}

