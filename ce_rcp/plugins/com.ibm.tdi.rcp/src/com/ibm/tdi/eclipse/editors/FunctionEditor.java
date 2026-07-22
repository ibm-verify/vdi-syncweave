/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import com.ibm.tdi.eclipse.Messages;


public class FunctionEditor extends ConnectorEditor {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String[] buttons = new String[]{Messages.getString("FunctionEditor.1"), Messages.getString("FunctionEditor.2"), Messages.getString("FunctionEditor.3")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	public FunctionEditor() {
		super();
		setTabButtonNames(buttons);
	}
}
