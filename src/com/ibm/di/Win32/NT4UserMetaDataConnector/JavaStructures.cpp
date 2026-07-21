#ifndef UNICODE
#define UNICODE
#endif
#include <windows.h>

#include "JavaStructures.h"


bool JavaObject::dummy_is_null;

std::vector<Wrapper_LPWSTR> Conv_JavaVectorOfStrings_To_VectorOfWrapper_LPWSTR(JNIEnv *p_env, jobject p_vector)
{
	std::vector<Wrapper_LPWSTR> result;
	JavaVector jv(p_env, p_vector);

	for(int i=0; i<jv.size(); i++)
	{
		jstring jstr = (jstring) jv.elementAt(i);

		const char* utfString = p_env->GetStringUTFChars(jstr, NULL);
		size_t charCount = _mbstrlen(utfString);
		WCHAR *wcs_str = (WCHAR *) malloc(sizeof(WCHAR) * (charCount + 1));
		int resultCode = MultiByteToWideChar(CP_UTF8, 0, utfString, -1, wcs_str, charCount + 1);
		if(resultCode > 0) {
			Wrapper_LPWSTR str(wcs_str, charCount);
			result.insert(result.end(), str);
		}
		p_env->ReleaseStringUTFChars(jstr, utfString);
		free(wcs_str);
	}

	return result;
}


JavaObject::JavaObject(JNIEnv *p_env, const char *p_class_name, const char *p_constructor_signature):m_env(p_env)
{
	m_java_class = m_env->FindClass(p_class_name);
	m_constructor_id = m_env->GetMethodID(m_java_class, "<init>", p_constructor_signature);
	m_java_object = m_env->NewObject(m_java_class, m_constructor_id);
}


JavaObject::JavaObject(JNIEnv *p_env, const jobject p_jobject, const char *p_constructor_signature):m_env(p_env),m_java_object(p_jobject)
{
	m_java_class = m_env->GetObjectClass(p_jobject);
	m_constructor_id = m_env->GetMethodID(m_java_class, "<init>", p_constructor_signature);
}


const Wrapper_LPWSTR JavaObject::GetStringField(const char *p_field_name,bool &is_null) const
{
	jstring jstr = (jstring) GetObjectField(p_field_name, "Ljava/lang/String;");
	if (!jstr) 
	{
		is_null = true;
		return L"";
	}
	else {
		is_null = false;

		const char* utfString = m_env->GetStringUTFChars(jstr, NULL);
		size_t charCount = _mbstrlen(utfString);
		WCHAR *wcs_str = (WCHAR *) malloc(sizeof(WCHAR) * (charCount + 1));
		// Convert the multi byte string into wide char string. 
		// Convert until the null-terminating char is reaced. 
		// (GetStringUTFChars should give a null-terminated string.
		int resultCode = MultiByteToWideChar(CP_UTF8, 0, utfString, -1, wcs_str, charCount + 1);
		if(resultCode > 0) {
			Wrapper_LPWSTR result(wcs_str, charCount);
			m_env->ReleaseStringUTFChars(jstr, utfString);
			free(wcs_str);
			return result;
		} else {
			Wrapper_LPWSTR result;
			m_env->ReleaseStringUTFChars(jstr, utfString);
			free(wcs_str);
			return result;
		}
	}
}



const jobject JavaObject::GetObjectField(const char *p_field_name, const char *p_field_sig) const
{
	jfieldID field_id = m_env->GetFieldID(m_java_class, p_field_name, p_field_sig);
	return m_env->GetObjectField(m_java_object, field_id);
}



const bool JavaObject::GetBooleanObjectField(const char *p_field_name) const
{
	jobject jo = GetObjectField(p_field_name, "Ljava/lang/Boolean;");
	if (!jo) return false;
	jclass java_class = m_env->FindClass("java/lang/Boolean");
	jmethodID m_id = m_env->GetMethodID(java_class, "booleanValue", "()Z");
	return m_env->CallBooleanMethod(jo, m_id)?true:false;
}



const int JavaObject::GetIntegerObjectField(const char *p_field_name) const
{
	jobject jo = GetObjectField(p_field_name, "Ljava/lang/Integer;");
	if (!jo) return 0;
	jclass java_class = m_env->FindClass("java/lang/Integer");
	jmethodID m_id = m_env->GetMethodID(java_class,"intValue","()I");
	return m_env->CallIntMethod(jo, m_id);
}



const jlong JavaObject::GetLongObjectField(const char *p_field_name) const
{
	jobject jo = GetObjectField(p_field_name, "Ljava/lang/Long;");
	if (!jo) return 0;
	jclass java_class = m_env->FindClass("java/lang/Long");
	jmethodID m_id = m_env->GetMethodID(java_class,"longValue","()J");
	return m_env->CallLongMethod(jo, m_id);
}



const jlong JavaObject::GetDateField(const char *p_field_name) const
{
	jobject jo = GetObjectField(p_field_name, "Ljava/util/Date;");
	if (!jo) return 0;
	jclass java_class = m_env->FindClass("java/util/Date");
	jmethodID m_id = m_env->GetMethodID(java_class,"getTime","()J");
	return m_env->CallLongMethod(jo, m_id);
}



void JavaObject::GetByteArrayField(const char *p_field_name, jbyte *p_field_value, const jsize p_array_length, bool &is_byte_array_null) const
{
	jbyteArray arr = (jbyteArray) GetObjectField(p_field_name, "[B");
	if(arr)
	{
		is_byte_array_null = false;
		jbyte *body = m_env->GetByteArrayElements(arr, NULL);
		for (int i=0; i < p_array_length; i++) 
		{
			p_field_value[i] = body[i];
		}
		m_env->ReleaseByteArrayElements(arr, body, JNI_ABORT);
	}
	else
	{
		is_byte_array_null = true;
		for (int i=0; i < p_array_length; i++) 
		{
			p_field_value[i] = 0;
		}
	}
}



void JavaObject::SetObjectField(const char *p_field_name, const jobject p_field_value, const char *p_field_sig)
{
	jfieldID field_id = m_env->GetFieldID(m_java_class, p_field_name, p_field_sig);
	m_env->SetObjectField(m_java_object, field_id, p_field_value);
}




void JavaObject::SetStringField(const char *p_field_name, const WCHAR *p_field_value)
{
	jstring str_value = NULL;
	if( p_field_value != NULL ) {
		size_t charCount = wcslen(p_field_value);
		int mbLength = WideCharToMultiByte(CP_UTF8, 0, p_field_value, charCount, NULL, 0, NULL, NULL);
		char *utfString = (char *) malloc(mbLength + 1);
		int resultCode = WideCharToMultiByte(CP_UTF8, 0, p_field_value, charCount, utfString, mbLength, NULL, NULL);
		if(resultCode > 0) {
			utfString[mbLength] = '\0';//termanate yourself
			str_value = m_env->NewStringUTF(utfString);
		} else {
			str_value = m_env->NewStringUTF("");
		}
		free(utfString);
	}
    SetObjectField(p_field_name, str_value, "Ljava/lang/String;");
}




void JavaObject::SetBooleanField(const char *p_field_name, const bool p_field_value)
{
	jfieldID field_id = m_env->GetFieldID(m_java_class, p_field_name, "Z");
	m_env->SetBooleanField(m_java_object, field_id, p_field_value);
}




void JavaObject::SetBooleanObjectField(const char *p_field_name, const bool p_field_value)
{
	JavaBoolean field(m_env, p_field_value);
	SetObjectField(p_field_name, field.GetJavaObject(), "Ljava/lang/Boolean;");
}




void JavaObject::SetIntField(const char *p_field_name, const int p_field_value)
{
	jfieldID field_id = m_env->GetFieldID(m_java_class, p_field_name, "I");
	m_env->SetIntField(m_java_object, field_id, p_field_value);
}



void JavaObject::SetIntegerObjectField(const char *p_field_name, const int p_field_value)
{
	JavaInteger field(m_env, p_field_value);
	SetObjectField(p_field_name, field.GetJavaObject(), "Ljava/lang/Integer;");
}



void JavaObject::SetLongField(const char *p_field_name, const jlong p_field_value)
{
	jfieldID field_id = m_env->GetFieldID(m_java_class, p_field_name, "J");
	m_env->SetLongField(m_java_object, field_id, p_field_value);
}



void JavaObject::SetLongObjectField(const char *p_field_name, const jlong p_field_value)
{
	JavaLong field(m_env, p_field_value);
	SetObjectField(p_field_name, field.GetJavaObject(), "Ljava/lang/Long;");
}



void JavaObject::SetDateField(const char *p_field_name, const jlong p_field_value)
{
	JavaDate field(m_env, p_field_value);
	SetObjectField(p_field_name, field.GetJavaObject(), "Ljava/util/Date;");
}



void JavaObject::SetDateFieldToNull(const char *p_field_name)
{
	SetObjectField(p_field_name, NULL, "Ljava/util/Date;");
}



void JavaObject::SetByteArrayField(const char *p_field_name,const jbyte p_field_value[],const jsize p_array_length)
{
	jbyteArray byte_array = NULL;
	if(p_field_value)
	{
		byte_array = m_env->NewByteArray(p_array_length);
		m_env->SetByteArrayRegion(byte_array, 0, p_array_length, (jbyte *) &p_field_value[0]);
	}
	SetObjectField(p_field_name, byte_array, "[B");
}



jobject JavaObject::GetJavaObject()
{
	return m_java_object;
}



void JavaVector::Init()
{
	m_mid_addElement = m_env->GetMethodID(m_java_class,"addElement","(Ljava/lang/Object;)V");
	m_mid_elementAt = m_env->GetMethodID(m_java_class,"elementAt","(I)Ljava/lang/Object;");
	m_mid_size = m_env->GetMethodID(m_java_class,"size","()I");
	m_mid_clear = m_env->GetMethodID(m_java_class,"clear","()V");
}



JavaVector::JavaVector(JNIEnv *p_env) : JavaObject(p_env,"java/util/Vector")
{
	Init();
}



JavaVector::JavaVector(JNIEnv *p_env, const jobject p_jobject) : JavaObject(p_env, p_jobject)
{
	Init();
}



const jobject JavaVector::elementAt(const int index) const
{
	return m_env->CallObjectMethod(m_java_object, m_mid_elementAt, index);
}



const int JavaVector::size() const
{
	return m_env->CallIntMethod(m_java_object, m_mid_size);
}



void JavaVector::addElement(jobject p_element)
{
	m_env->CallVoidMethod(m_java_object, m_mid_addElement, p_element);
}


void JavaVector::clear()
{
	m_env->CallVoidMethod(m_java_object, m_mid_clear);
}


JavaInteger::JavaInteger(JNIEnv *p_env, const int p_int_value):JavaObject(p_env,"java/lang/Integer","(I)V")
{
	m_java_object = m_env->NewObject(m_java_class, m_constructor_id, p_int_value);
}



JavaLong::JavaLong(JNIEnv *p_env, const jlong p_long_value):JavaObject(p_env,"java/lang/Long","(J)V")
{
	m_java_object = m_env->NewObject(m_java_class, m_constructor_id, p_long_value);
}



JavaBoolean::JavaBoolean(JNIEnv *p_env, const bool p_bool_value):JavaObject(p_env,"java/lang/Boolean","(Z)V")
{
	m_java_object = m_env->NewObject(m_java_class, m_constructor_id, p_bool_value);
}



JavaDate::JavaDate(JNIEnv *p_env, const jlong p_milliseconds):JavaObject(p_env,"java/util/Date","(J)V")
{
	m_java_object = m_env->NewObject(m_java_class, m_constructor_id, p_milliseconds);
}
