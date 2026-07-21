/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.automation;

import com.ibm.di.server.ResourceHash;

/**
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class COMError extends Exception {

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	public COMError() {
		super();
	}

	public COMError(String message) {
		super(message);

		description = message;
	}

	public COMError(String message, String src) {
		source = src;
		description = message;
	}

	public COMError(long ID, String message, String src) {
		GUID = ID;
		source = src;
		description = message;
	}

	public String getMessage() {
		return sResHash.getString("MISERVER.COMERROR.ERROR.IN.COMPROXY.DLL",
				description);
	}

	private long GUID;

	private String description;

	private String source;

}
