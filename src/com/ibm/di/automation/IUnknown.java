/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
/*
 * Created on Aug 16, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.ibm.di.automation;

/**
 * @author Administrator
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class IUnknown {
	public int m_pIUnknown = 0;

	public IUnknown() {
		m_pIUnknown = 0;
	}

	protected IUnknown(int pUnknown) {
		m_pIUnknown = pUnknown;
	}

	public native IUnknown QueryInterface(String iid) throws COMError;

	public native void AddRef() throws COMError;

	public native void Release() throws COMError;

	static {
		System.loadLibrary("COMProxy");
	}
}
