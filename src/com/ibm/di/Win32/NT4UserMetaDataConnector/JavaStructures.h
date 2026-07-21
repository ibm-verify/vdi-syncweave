// The JavaStructures.h defines classes that represent Java objects
// and capsulates methods for getting and setting java objects members

#ifndef INCLUDED_JAVA_STRUCTURES_H
#define INCLUDED_JAVA_STRUCTURES_H

#include <vector>

#include "jni.h"
#include "Wrapper_LPWSTR.h"

#define JAVA_DEFAULT_CONSTRUCTOR_SIGNATURE "()V"


class JavaObject
{
	static bool dummy_is_null;
protected:
	jclass m_java_class;
	jobject m_java_object;
	JNIEnv *m_env;
	jmethodID m_constructor_id;

public:
	JavaObject(JNIEnv *p_env, const char *p_class_name, const char *p_constructor_signature = JAVA_DEFAULT_CONSTRUCTOR_SIGNATURE);
	JavaObject(JNIEnv *p_env, const jobject p_jobject, const char *p_constructor_signature = JAVA_DEFAULT_CONSTRUCTOR_SIGNATURE);

	virtual const jobject GetObjectField (const char *p_field_name, const char *p_field_sig) const;
	virtual const Wrapper_LPWSTR GetStringField (const char *p_field_name, bool &is_null = dummy_is_null) const;
	virtual const bool GetBooleanObjectField(const char *p_field_name) const;
	virtual const int GetIntegerObjectField(const char *p_field_name) const;
	virtual const jlong GetLongObjectField(const char *p_field_name) const;
	virtual const jlong GetDateField(const char *p_field_name) const;
	virtual void GetByteArrayField(const char *p_field_name, jbyte *p_field_value, const jsize p_array_length, bool &is_byte_array_null) const;

	virtual void SetObjectField (const char *p_field_name, const jobject p_field_value, const char *p_field_sig);
	virtual void SetStringField (const char *p_field_name, const WCHAR *p_field_value);
	virtual void SetBooleanField(const char *p_field_name, const bool p_field_value);
	virtual void SetBooleanObjectField(const char *p_field_name, const bool p_field_value);
	virtual void SetIntField(const char *p_field_name, const int p_field_value);
	virtual void SetIntegerObjectField(const char *p_field_name, const int p_field_value);
	virtual void SetLongField(const char *p_field_name, const jlong p_field_value);
	virtual void SetLongObjectField(const char *p_field_name, const jlong p_field_value);
	virtual void SetDateField(const char *p_field_name, const jlong p_field_value);
	virtual void SetDateFieldToNull(const char *p_field_name);
	virtual void SetByteArrayField(const char *p_field_name, const jbyte p_field_value[],const jsize p_array_length);

	virtual jobject GetJavaObject();
};



class JavaVector : public JavaObject
{
	jmethodID m_mid_addElement;
	jmethodID m_mid_elementAt;
	jmethodID m_mid_size;
	jmethodID m_mid_clear;

	void Init();

public:
	JavaVector(JNIEnv *p_env);
	JavaVector(JNIEnv *p_env, const jobject p_jobject);

	void addElement(jobject p_element);
	const jobject elementAt(const int index) const;
	const int size() const;
	void clear();
};


class JavaInteger : public JavaObject
{
public:
	JavaInteger(JNIEnv *p_env, const int p_int_value);
};


class JavaLong : public JavaObject
{
public:
	JavaLong(JNIEnv *p_env, const jlong p_long_value);
};


class JavaBoolean : public JavaObject
{
public:
	JavaBoolean(JNIEnv *p_env, const bool p_bool_value);
};


class JavaDate : public JavaObject
{
public:
	//Milliseconds since January 1, 1970, 00:00:00 GMT
	JavaDate(JNIEnv *p_env, const jlong p_milliseconds); 
};


struct JavaStruct
{
	virtual jobject GetJObject(JNIEnv *)const = 0;
};


template <class T> jobject Conv_Vector_To_JavaVector(JNIEnv *env, std::vector<T> p_vector)
{
	JavaVector *v = new JavaVector(env);

	for (std::vector<Wrapper_LPWSTR>::size_type i=0; i<p_vector.size(); i++) 
	{
		v->addElement((p_vector)[i].GetJObject(env));
	};
	jobject jv = v->GetJavaObject();
	return jv;
}


std::vector<Wrapper_LPWSTR> Conv_JavaVectorOfStrings_To_VectorOfWrapper_LPWSTR(JNIEnv *env, jobject p_vector);


#endif