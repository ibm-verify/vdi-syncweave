/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.exceptions;

import com.ibm.di.server.ResourceHash;

public class DOMException extends org.w3c.dom.DOMException {

	protected static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = -544026061994680684L;

	private static final ResourceHash resHash = new ResourceHash("miserver");

	public DOMException(String key) {
		super((short) 0, resHash.getString(key));
	}

	public DOMException(String key, Object param) {
		super((short) 0, resHash.getString(key, param));
	}

	public DOMException(String key, Object[] params) {
		super((short) 0, resHash.getString(key, params));
	}

}
