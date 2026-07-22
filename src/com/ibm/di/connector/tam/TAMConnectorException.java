/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.tam;

import java.util.HashMap;

/**
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TAMConnectorException extends Exception {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final int CAPACITY = 5;

	private HashMap mFailed;

	/**
	 * Default Constructor
	 */
	public TAMConnectorException() {
		super();
		mFailed = null;
	}

	/**
	 * Constructor
	 * 
	 * @param arg0
	 *            The Error Message
	 */
	public TAMConnectorException(String arg0) {
		super(arg0);
		mFailed = null;
	}

	/**
	 * Constructor
	 * 
	 * @param failed
	 *            The Failed Attributes and their associated error messages
	 * @param arg0
	 *            The Error Message
	 */
	public TAMConnectorException(HashMap failed, String arg0) {
		super(arg0);
		mFailed = failed;
	}

	/**
	 * Builds a HashMap of failed attributes with corresponding error messages
	 * 
	 * @param attribute
	 *            The Attribute which failed
	 * @param msg
	 *            The Error Message associated with the failed attribute
	 */
	public void setFailed(String attribute, String msg) {
		if (mFailed == null)
			mFailed = new HashMap(CAPACITY);
		mFailed.put(attribute, msg);
	}

	/**
	 * Returns a HashMap of failed attributes with corresponding error messages
	 * 
	 * @return HashMap
	 */
	public HashMap getFailed() {
		return mFailed;
	}
}
