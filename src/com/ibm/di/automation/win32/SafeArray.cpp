#include <jni.h>
#include <stdio.h>

JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_init
  (JNIEnv *, jobject, jint, jintArray, jintArray)
{
}

// not impl
//JNIEXPORT jobject JNICALL Java_com_ibm_di_automation_SafeArray_clone
//  (JNIEnv *, jobject)
//{
//}

JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_destroy
  (JNIEnv *, jobject)
{
}

JNIEXPORT jint JNICALL Java_com_ibm_di_automation_SafeArray_getvt
  (JNIEnv *, jobject)
{
	return 0;
}

JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_reinit
  (JNIEnv *, jobject, jobject)
{
}

JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_reinterpretType
  (JNIEnv *, jobject, jint)
{
}

JNIEXPORT jint JNICALL Java_com_ibm_di_automation_SafeArray_getLBound__
  (JNIEnv *, jobject)
{
	return 0;
}

JNIEXPORT jint JNICALL Java_com_ibm_di_automation_SafeArray_getLBound__I
  (JNIEnv *, jobject, jint)
{
	return 0;
}

JNIEXPORT jint JNICALL Java_com_ibm_di_automation_SafeArray_getUBound__
  (JNIEnv *, jobject)
{
	return 0;
}

JNIEXPORT jint JNICALL Java_com_ibm_di_automation_SafeArray_getUBound__I
  (JNIEnv *, jobject, jint)
{
	return 0;
}


JNIEXPORT jint JNICALL Java_com_ibm_di_automation_SafeArray_getNumDim
  (JNIEnv *, jobject)
{
	return 0;
}


JNIEXPORT jint JNICALL Java_com_ibm_di_automation_SafeArray_getFeatures
  (JNIEnv *, jobject)
{
	return 0;
}


JNIEXPORT jint JNICALL Java_com_ibm_di_automation_SafeArray_getElemSize
  (JNIEnv *, jobject)
{
	return 0;
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_fromCharArray
  (JNIEnv *, jobject, jcharArray)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_fromIntArray
  (JNIEnv *, jobject, jintArray)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_fromShortArray
  (JNIEnv *, jobject, jshortArray)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_fromDoubleArray
  (JNIEnv *, jobject, jdoubleArray)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_fromStringArray
  (JNIEnv *, jobject, jobjectArray)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_fromByteArray
  (JNIEnv *, jobject, jbyteArray)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_fromFloatArray
  (JNIEnv *, jobject, jfloatArray)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_fromBooleanArray
  (JNIEnv *, jobject, jbooleanArray)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_fromVariantArray
  (JNIEnv *, jobject, jobjectArray)
{
}


JNIEXPORT jcharArray JNICALL Java_com_ibm_di_automation_SafeArray_toCharArray
  (JNIEnv *, jobject)
{
	return NULL;
}


JNIEXPORT jintArray JNICALL Java_com_ibm_di_automation_SafeArray_toIntArray
  (JNIEnv *, jobject)
{
	return NULL;
}


JNIEXPORT jshortArray JNICALL Java_com_ibm_di_automation_SafeArray_toShortArray
  (JNIEnv *, jobject)
{
	return NULL;
}


JNIEXPORT jdoubleArray JNICALL Java_com_ibm_di_automation_SafeArray_toDoubleArray
  (JNIEnv *, jobject)
{
	return NULL;
}


JNIEXPORT jobjectArray JNICALL Java_com_ibm_di_automation_SafeArray_toStringArray
  (JNIEnv *, jobject)
{
	return NULL;
}


JNIEXPORT jbyteArray JNICALL Java_com_ibm_di_automation_SafeArray_toByteArray
  (JNIEnv *, jobject)
{
	return NULL;
}


JNIEXPORT jfloatArray JNICALL Java_com_ibm_di_automation_SafeArray_toFloatArray
  (JNIEnv *, jobject)
{
	return NULL;
}


JNIEXPORT jbooleanArray JNICALL Java_com_ibm_di_automation_SafeArray_toBooleanArray
  (JNIEnv *, jobject)
{
	return NULL;
}


JNIEXPORT jobjectArray JNICALL Java_com_ibm_di_automation_SafeArray_toVariantArray
  (JNIEnv *, jobject)
{
	return NULL;
}


JNIEXPORT jchar JNICALL Java_com_ibm_di_automation_SafeArray_getChar__I
  (JNIEnv *, jobject, jint)
{	
	return 0;
}


JNIEXPORT jchar JNICALL Java_com_ibm_di_automation_SafeArray_getChar__II
  (JNIEnv *, jobject, jint, jint)
{
	return 0;
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setChar__IC
  (JNIEnv *, jobject, jint, jchar)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setChar__IIC
  (JNIEnv *, jobject, jint, jint, jchar)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_getChars
  (JNIEnv *, jobject, jint, jint, jcharArray, jint)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setChars
  (JNIEnv *, jobject, jint, jint, jcharArray, jint)
{
}


JNIEXPORT jint JNICALL Java_com_ibm_di_automation_SafeArray_getInt__I
  (JNIEnv *, jobject, jint)
{
	return 0;
}


JNIEXPORT jint JNICALL Java_com_ibm_di_automation_SafeArray_getInt__II
  (JNIEnv *, jobject, jint, jint)
{
	return 0;
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setInt__II
  (JNIEnv *, jobject, jint, jint)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setInt__III
  (JNIEnv *, jobject, jint, jint, jint)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_getInts
  (JNIEnv *, jobject, jint, jint, jintArray, jint)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setInts
  (JNIEnv *, jobject, jint, jint, jintArray, jint)
{
}


JNIEXPORT jshort JNICALL Java_com_ibm_di_automation_SafeArray_getShort__I
  (JNIEnv *, jobject, jint)
{
	return 0;
}


JNIEXPORT jshort JNICALL Java_com_ibm_di_automation_SafeArray_getShort__II
  (JNIEnv *, jobject, jint, jint)
{
	return 0;
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setShort__IS
  (JNIEnv *, jobject, jint, jshort)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setShort__IIS
  (JNIEnv *, jobject, jint, jint, jshort)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_getShorts
  (JNIEnv *, jobject, jint, jint, jshortArray, jint)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setShorts
  (JNIEnv *, jobject, jint, jint, jshortArray, jint)
{
}


JNIEXPORT jdouble JNICALL Java_com_ibm_di_automation_SafeArray_getDouble__I
  (JNIEnv *, jobject, jint)
{
	return 0;
}


JNIEXPORT jdouble JNICALL Java_com_ibm_di_automation_SafeArray_getDouble__II
  (JNIEnv *, jobject, jint, jint)
{
	return 0;
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setDouble__ID
  (JNIEnv *, jobject, jint, jdouble)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setDouble__IID
  (JNIEnv *, jobject, jint, jint, jdouble)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_getDoubles
  (JNIEnv *, jobject, jint, jint, jdoubleArray, jint)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setDoubles
  (JNIEnv *, jobject, jint, jint, jdoubleArray, jint)
{
}


JNIEXPORT jstring JNICALL Java_com_ibm_di_automation_SafeArray_getString__I
  (JNIEnv *, jobject, jint)
{
	return NULL;
}


JNIEXPORT jstring JNICALL Java_com_ibm_di_automation_SafeArray_getString__II
  (JNIEnv *, jobject, jint, jint)
{
	return NULL;
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setString__ILjava_lang_String_2
  (JNIEnv *, jobject, jint, jstring)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setString__IILjava_lang_String_2
  (JNIEnv *, jobject, jint, jint, jstring)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_getStrings
  (JNIEnv *, jobject, jint, jint, jobjectArray, jint)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setStrings
  (JNIEnv *, jobject, jint, jint, jobjectArray, jint)
{
}


JNIEXPORT jbyte JNICALL Java_com_ibm_di_automation_SafeArray_getByte__I
  (JNIEnv *, jobject, jint)
{
	return 0;
}


JNIEXPORT jbyte JNICALL Java_com_ibm_di_automation_SafeArray_getByte__II
  (JNIEnv *, jobject, jint, jint)
{
	return 0;
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setByte__IB
  (JNIEnv *, jobject, jint, jbyte)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setByte__IIB
  (JNIEnv *, jobject, jint, jint, jbyte)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_getBytes
  (JNIEnv *, jobject, jint, jint, jbyteArray, jint)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setBytes
  (JNIEnv *, jobject, jint, jint, jbyteArray, jint)
{
}


JNIEXPORT jfloat JNICALL Java_com_ibm_di_automation_SafeArray_getFloat__I
  (JNIEnv *, jobject, jint)
{
	return 0;
}


JNIEXPORT jfloat JNICALL Java_com_ibm_di_automation_SafeArray_getFloat__II
  (JNIEnv *, jobject, jint, jint)
{
	return 0;
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setFloat__IF
  (JNIEnv *, jobject, jint, jfloat)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setFloat__IIF
  (JNIEnv *, jobject, jint, jint, jfloat)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_getFloats
  (JNIEnv *, jobject, jint, jint, jfloatArray, jint)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setFloats
  (JNIEnv *, jobject, jint, jint, jfloatArray, jint)
{
}


JNIEXPORT jboolean JNICALL Java_com_ibm_di_automation_SafeArray_getBoolean__I
  (JNIEnv *, jobject, jint)
{
	return 0;
}


JNIEXPORT jboolean JNICALL Java_com_ibm_di_automation_SafeArray_getBoolean__II
  (JNIEnv *, jobject, jint, jint)
{
	return 0;
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setBoolean__IZ
  (JNIEnv *, jobject, jint, jboolean)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setBoolean__IIZ
  (JNIEnv *, jobject, jint, jint, jboolean)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_getBooleans
  (JNIEnv *, jobject, jint, jint, jbooleanArray, jint)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setBooleans
  (JNIEnv *, jobject, jint, jint, jbooleanArray, jint)
{
}


JNIEXPORT jobject JNICALL Java_com_ibm_di_automation_SafeArray_getVariant__I
  (JNIEnv *, jobject, jint)
{
	return NULL;
}


JNIEXPORT jobject JNICALL Java_com_ibm_di_automation_SafeArray_getVariant__II
  (JNIEnv *, jobject, jint, jint)
{
	return NULL;
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setVariant__ILcom_ibm_di_automation_VARIANT_2
  (JNIEnv *, jobject, jint, jobject)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setVariant__IILcom_ibm_di_automation_VARIANT_2
  (JNIEnv *, jobject, jint, jint, jobject)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_getVariants
  (JNIEnv *, jobject, jint, jint, jobjectArray, jint)
{
}


JNIEXPORT void JNICALL Java_com_ibm_di_automation_SafeArray_setVariants
  (JNIEnv *, jobject, jint, jint, jobjectArray, jint)
{
}
