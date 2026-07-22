/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import com.ibm.di.connector.sapr3.bor.i18n.DefaultMessagesImpl;
import com.ibm.di.connector.sapr3.bor.i18n.Messages;

/**
 * Compile time constants for message bundle keys.
 * 
 */
final class LogMessageHelper {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final String MESSAGE_PROPERTIES_NAME = "sapborconnector";

	//
	// urcmessages must be loadable by this classes classloader.
	//  
	private static final Messages MESSAGE_RESOURCE = new DefaultMessagesImpl(
			MESSAGE_PROPERTIES_NAME);

	/*
	 * Messages keys loaded within the message resource.
	 */
	static final String SAPR3_BOR_0001 = "SAPR3_BOR_0001";

	static final String SAPR3_BOR_0002 = "SAPR3_BOR_0002";

	static final String SAPR3_BOR_0003 = "SAPR3_BOR_0003";

	static final String SAPR3_BOR_0004 = "SAPR3_BOR_0004";

	static final String SAPR3_BOR_0005 = "SAPR3_BOR_0005";

	static final String SAPR3_BOR_0006 = "SAPR3_BOR_0006";

	static final String SAPR3_BOR_0007 = "SAPR3_BOR_0007";

	static final String SAPR3_BOR_0008 = "SAPR3_BOR_0008";

	static final String SAPR3_BOR_0009 = "SAPR3_BOR_0009";

	static final String SAPR3_BOR_0010 = "SAPR3_BOR_0010";

	static final String SAPR3_BOR_0011 = "SAPR3_BOR_0011";

	static final String SAPR3_BOR_0012 = "SAPR3_BOR_0012";

	static final String SAPR3_BOR_0013 = "SAPR3_BOR_0013";

	static final String SAPR3_BOR_0014 = "SAPR3_BOR_0014";

	static final String SAPR3_BOR_0015 = "SAPR3_BOR_0015";

	static final String SAPR3_BOR_0016 = "SAPR3_BOR_0016";

	static final String SAPR3_BOR_0017 = "SAPR3_BOR_0017";

	static final String SAPR3_BOR_0018 = "SAPR3_BOR_0018";

	static final String SAPR3_BOR_0019 = "SAPR3_BOR_0019";

	static final String SAPR3_BOR_0020 = "SAPR3_BOR_0020";

	static final String SAPR3_BOR_0021 = "SAPR3_BOR_0021";

	static final String SAPR3_BOR_0022 = "SAPR3_BOR_0022";

	static final String SAPR3_BOR_0023 = "SAPR3_BOR_0023";

	static final String SAPR3_BOR_0024 = "SAPR3_BOR_0024";

	static final String SAPR3_BOR_0025 = "SAPR3_BOR_0025";

	static final String SAPR3_BOR_0026 = "SAPR3_BOR_0026";

	static final String SAPR3_BOR_0027 = "SAPR3_BOR_0027";

	static final String SAPR3_BOR_0028 = "SAPR3_BOR_0028";

	static final String SAPR3_BOR_0029 = "SAPR3_BOR_0029";

	static final String SAPR3_BOR_0030 = "SAPR3_BOR_0030";

	static final String SAPR3_BOR_0031 = "SAPR3_BOR_0031";

	static final String SAPR3_BOR_0032 = "SAPR3_BOR_0032";

	static final String SAPR3_BOR_0033 = "SAPR3_BOR_0033";

	static final String SAPR3_BOR_0034 = "SAPR3_BOR_0034";

	static final String SAPR3_BOR_0035 = "SAPR3_BOR_0035";

	static final String SAPR3_BOR_0036 = "SAPR3_BOR_0036";

	static final String SAPR3_BOR_0037 = "SAPR3_BOR_0037";

	static final String SAPR3_BOR_0038 = "SAPR3_BOR_0038";

	/**
	 * Get a reference to the loaded messages resources.
	 * 
	 * @return The loaded messages
	 */
	static Messages getMsgResource() {
		return MESSAGE_RESOURCE;
	}

	/**
	 * Disabled.
	 */
	private LogMessageHelper() {
		super();
	}

}
