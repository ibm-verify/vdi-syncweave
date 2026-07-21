#ifndef UNICODE
#define UNICODE
#endif
#include "NTMetaDataException.h"

#define FORMAT_API_ERROR_CODE_MSG L"() WinAPI function returned error code: "
#define FORMAT_API_ERROR_CODE_MSG_ERROR L"FormatMessage() API function: The system cannot find message text for message number: "
#define FORMAT_API_ERROR_CODE_MSG_BUF_MAX_SIZE 4096



Wrapper_LPWSTR  NTMetaDataException::FormatAPIErrorCode(Wrapper_LPWSTR p_api_function_name, NET_API_STATUS p_code, LPDWORD p_parm_err)
{
	Wrapper_LPWSTR s;
	WCHAR msg_buff[FORMAT_API_ERROR_CODE_MSG_BUF_MAX_SIZE];

	DWORD result = FormatMessage(
			FORMAT_MESSAGE_FROM_SYSTEM |
			FORMAT_MESSAGE_MAX_WIDTH_MASK |
			FORMAT_MESSAGE_IGNORE_INSERTS,					// source and processing options
			NULL,											// pointer to  message source
			p_code,											// requested message identifier
			MAKELANGID(LANG_NEUTRAL, SUBLANG_SYS_DEFAULT), 	// language identifier for requested message
			(LPTSTR) &msg_buff,								// pointer to message buffer
			FORMAT_API_ERROR_CODE_MSG_BUF_MAX_SIZE,			// maximum size of message buffer
			NULL											// pointer to array of message inserts
			);

	
	s += p_api_function_name;
	s += FORMAT_API_ERROR_CODE_MSG;
	if (!result)
	{
		s += FORMAT_API_ERROR_CODE_MSG_ERROR;
	}

	WCHAR tmp_char_buff[20];	
	s += _itow(p_code,tmp_char_buff,10);
	
	if (result)
	{
		s += L" (";
		s += msg_buff;
		s += L").";
	}

	if (p_parm_err) 
	{
		WCHAR tmp_char_buff[20];	

		s += L"Parameter Index(";
		s += _itow(*p_parm_err,tmp_char_buff,10);
		s += L").";
	}

	return s;
}
