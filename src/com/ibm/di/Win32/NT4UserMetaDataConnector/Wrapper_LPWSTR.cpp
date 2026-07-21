#ifndef UNICODE
#define UNICODE
#endif
#include "NTMetaDataException.h"
#include "Wrapper_LPWSTR.h"
#include "JavaStructures.h"

using namespace std;



void Wrapper_LPWSTR::initEmpty()
{
	m_lpwstr = NULL;
	m_str = L"";
	m_pchStr = NULL;
}

	

void Wrapper_LPWSTR::initASCIIString(const std::wstring p_data)
{
	int nLen = WideCharToMultiByte( CP_ACP, 0, p_data.c_str(), p_data.size(), NULL, 0, NULL, NULL );
	m_pchStr = new char[nLen+1];
	WideCharToMultiByte( CP_ACP, 0, p_data.c_str(), p_data.size(), m_pchStr, nLen, NULL, NULL );
	m_pchStr[nLen] = 0;
}



void Wrapper_LPWSTR::init(const wstring p_data)
{
	int nLen = p_data.size() + 1;
	m_lpwstr = new WCHAR[nLen];
	wcscpy( m_lpwstr, p_data.c_str() );
	
	m_str = p_data;

	initASCIIString( p_data );
}



void Wrapper_LPWSTR::init(const WCHAR * p_data, jsize length)
{
	m_lpwstr = new WCHAR[length+1];
	memcpy( m_lpwstr, p_data, length*sizeof(WCHAR) );
	m_lpwstr[length] = 0;
	
	m_str = m_lpwstr;

	initASCIIString( m_str );
}


void Wrapper_LPWSTR::initFromJNI(const WCHAR * p_data, jsize length)
{
	if( p_data == NULL ) {
		initEmpty();
	}
	else {
		init(p_data, length);
	}
}



jobject Wrapper_LPWSTR::GetJObject(JNIEnv *p_env) const
{
	if (m_lpwstr == NULL) {
		return NULL;
	}
	else {
		jobject returnValue;
		const wchar_t* wcString = m_str.c_str();
		size_t charCount = wcslen(wcString);
		int mbLength = WideCharToMultiByte(CP_UTF8, 0, wcString, charCount, NULL, 0, NULL, NULL);
		char *utfString = (char *) malloc(mbLength + 1);
		int resultCode = WideCharToMultiByte(CP_UTF8, 0, wcString, charCount, utfString, charCount, NULL, NULL);		
		if(resultCode > 0) {
			utfString[mbLength] = '\0'; //termanate yourself
			returnValue = p_env->NewStringUTF(utfString);
		} else {
			returnValue = p_env->NewStringUTF("");
		}
		free(utfString);
		return returnValue;
		//return p_env->NewString(m_str.c_str(), m_str.size());
	}
}
