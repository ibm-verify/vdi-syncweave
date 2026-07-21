// The Wrapper_LPWSTR.h defines class that wrapps the LPWSTR type.
// It allows the LPWSTR type to be accessed as the standard wstring type.

#ifndef INCLUDED_WRAPPER_LPWSTR_H
#define INCLUDED_WRAPPER_LPWSTR_H

#ifndef UNICODE
#define UNICODE
#endif

#include <windows.h>
#include <string>
#include "jni.h"

class Wrapper_LPWSTR
{
private:
	LPWSTR m_lpwstr;
	std::wstring m_str;
	char * m_pchStr;

private:
	void init(const std::wstring p_data);
	void init(const WCHAR * p_data, jsize length);
	void initASCIIString(const std::wstring p_data);
	void initEmpty();

public:
	void initFromJNI(const WCHAR * p_data, jsize length);


public:
	Wrapper_LPWSTR()
	{
		initEmpty();
	}


	// Overriding the default copy constructor
	Wrapper_LPWSTR(const Wrapper_LPWSTR& p_data) 
	{
		if( p_data.m_lpwstr == NULL ) {
			initEmpty();
		}
		else {
			init(p_data.m_str);
		}
	}


	Wrapper_LPWSTR(const std::wstring p_data)
	{
		init(p_data);	
	}


	Wrapper_LPWSTR(const LPWSTR p_data)
	{
		if( p_data == NULL ) {
			initEmpty();
		}
		else {
			init(p_data);
		}
	}


	Wrapper_LPWSTR(const WCHAR * p_data, jsize length)
	{
		initFromJNI(p_data, length);
	}

	
	operator const LPWSTR() const
	{
		return m_lpwstr; 
	}


	operator std::wstring() const 
	{
		return m_str;
	}


	operator const char *() const
	{
		return m_pchStr;
	}

	
	Wrapper_LPWSTR& operator=(const Wrapper_LPWSTR& p_data)
	{
		if (m_lpwstr != NULL) {
			delete[] m_lpwstr;
		}
		if (m_pchStr != NULL) { 
			delete[] m_pchStr;
		}

		if( p_data.m_lpwstr == NULL ) {
			initEmpty();
		}
		else {
			init(p_data.m_str);
		}
		return *this;
	}


	Wrapper_LPWSTR& operator=(const WCHAR p_data[])
	{
		if (m_lpwstr != NULL) {
			delete[] m_lpwstr;
		}
		if (m_pchStr != NULL) { 
			delete[] m_pchStr;
		}

		if( p_data == NULL ) {
			initEmpty();
		}
		else {
			init(p_data);
		}
		return *this;
	}

	Wrapper_LPWSTR& operator+=(const Wrapper_LPWSTR& p_data)
	{
		if (m_lpwstr != NULL) {
			delete[] m_lpwstr;
		}
		if (m_pchStr != NULL) {
			delete[] m_pchStr;
		}

		std::wstring s = m_str;
		s += p_data.m_str;
		init(s);
		return *this;
	}

	Wrapper_LPWSTR& operator+=(const int p_data)
	{
		if (m_lpwstr != NULL) {
			delete[] m_lpwstr;
		}
		if (m_pchStr != NULL) { 
			delete[] m_pchStr;
		}

		std::wstring s = m_str;
		WCHAR buf[64];
		s += _itow(p_data,buf,10);
		init(s);
		return *this;
	}

	Wrapper_LPWSTR operator+ (Wrapper_LPWSTR s1)
	{
		Wrapper_LPWSTR s = m_str;
		s += s1;
		return s;
	}

	const Wrapper_LPWSTR ToUpperCase() const
	{
		WCHAR* tmpbuf = _wcsdup(m_str.c_str());

		Wrapper_LPWSTR new_string = _wcsupr(tmpbuf);
		free(tmpbuf);
		return new_string;
	}

	virtual jobject GetJObject(JNIEnv *) const;

	virtual ~Wrapper_LPWSTR()
	{
		if (m_lpwstr != NULL) {
			delete[] m_lpwstr;
		}
		if (m_pchStr != NULL) { 
			delete[] m_pchStr;
		}
	}

};

#endif