// The NTMetaDataException.h defines an exception that is thrown
// whenever an error occurs during the execution of any WinAPI Net function.

#ifndef INCLUDED_NTMETADATA_EXCEPTION_H
#define INCLUDED_NTMETADATA_EXCEPTION_H

#include <windows.h>
#include <lm.h>
#include <string>
#include "Wrapper_LPWSTR.h"

class NTMetaDataException
{
private:
	std::wstring m_message;

public:

	static Wrapper_LPWSTR FormatAPIErrorCode(Wrapper_LPWSTR p_api_function_name, NET_API_STATUS p_code = GetLastError(), LPDWORD p_parm_err = NULL);

	NTMetaDataException(const std::wstring p_msg) : m_message(p_msg) {}
	NTMetaDataException(const std::wstring p_msg, NET_API_STATUS p_code, LPDWORD p_parm_err = NULL) :
		m_message(FormatAPIErrorCode(p_msg, p_code, p_parm_err)) {}

	std::wstring getMessage() const {
		return m_message;
	}
};

#endif