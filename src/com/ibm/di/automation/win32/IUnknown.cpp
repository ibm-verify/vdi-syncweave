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
#include "IUnknown.h"
#include <jni.h>
#include <stdio.h>

extern "C"
{

	JNIEXPORT jobject JNICALL Java_com_ibm_di_automation_IUnknown_QueryInterface
	  (JNIEnv *jnienv, jobject jthis, jstring jiid)
	{
		IUnknown *pIUnknown = getUnknown(jnienv,jthis);
		
		if (!pIUnknown) return NULL;
		
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

  		IUnknown *ppv;
  		hr = pIUnknown->QueryInterface(iid, (void **)&ppv);
  		if (FAILED(hr)) {
		 	Throw(jnienv, "QI on IID from String Failed", hr);
   			 return NULL;
  		}

  		jclass autoClass = jnienv->FindClass("com/ibm/di/auomation/IUnknown");

  		jmethodID autoCons =
  		jnienv->GetMethodID(autoClass, "<init>", "(I)V");

		if (ppv) ppv->AddRef();
 		jobject newAuto = jnienv->NewObject(autoClass, autoCons, ppv);
  		return newAuto;
	}

	JNIEXPORT void JNICALL Java_com_ibm_di_automation_IUnknown_AddRef
	  (JNIEnv *jnienv, jobject jthis)
	{
		IUnknown *pIUnknown = (IDispatch *)getUnknown(jnienv,jthis);
		pIUnknown->AddRef();
	}

	JNIEXPORT void JNICALL Java_com_ibm_di_automation_IUnknown_Release
	  (JNIEnv *jnienv, jobject jthis)
	{
		jclass cls = jnienv->GetObjectClass(jthis);
		jfieldID jf = jnienv->GetFieldID( cls, "m_pIUnknown", "I");
		
		IUnknown *pIUnknown = (IDispatch *)getUnknown(jnienv,jthis);
		if (pIUnknown) {
			pIUnknown->Release();
			jnienv->SetIntField(jthis, jf, (unsigned int)0);
		}
	}
}