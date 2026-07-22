/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.automation;

/**
 * @author Vishakha
 * 
 * Filename : BSTR.java
 */
public class BSTR {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public int m_pBSTR = 0;
	public BSTR(String s) throws COMError{
		createBSTR(s);
	}
	protected BSTR(int addr)throws COMError{
		m_pBSTR = addr;
	}
	public native void createBSTR(String s)throws COMError;
	
	public native String toString();
	
	static {
			System.loadLibrary("COMProxy");
		}
}
