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

// COMProxy.cpp : Defines the entry point for the DLL application.

#include <windows.h>

BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
	switch(ul_reason_for_call)
	{
	case DLL_PROCESS_ATTACH : CoInitialize(NULL); break;
	case DLL_PROCESS_DETACH : CoUninitialize(); break;

	}
	return TRUE;
}