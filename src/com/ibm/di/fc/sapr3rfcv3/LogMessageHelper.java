/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import com.ibm.di.fc.sapr3rfcv3.i18n.DefaultMessagesImpl;
import com.ibm.di.fc.sapr3rfcv3.i18n.Messages;

/**
 * Compile time constants for message bundle keys.
 * 
 */
final class LogMessageHelper {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final String MESSAGE_PROPERTIES_NAME = "saprfcfc";

	//
	// urcmessages must be loadable by this classes classloader.
	//  
	private static final Messages MESSAGE_RESOURCE = new DefaultMessagesImpl(
			MESSAGE_PROPERTIES_NAME);

	/*
	 * Messages keys loaded within the message resource.
	 */
	static final String SAPR3_RFCFC_0001 = "SAPR3_RFCFC_0001";

	static final String SAPR3_RFCFC_0002 = "SAPR3_RFCFC_0002";

	static final String SAPR3_RFCFC_0003 = "SAPR3_RFCFC_0003";

	static final String SAPR3_RFCFC_0004 = "SAPR3_RFCFC_0004";

	static final String SAPR3_RFCFC_0005 = "SAPR3_RFCFC_0005";

	static final String SAPR3_RFCFC_0006 = "SAPR3_RFCFC_0006";

	static final String SAPR3_RFCFC_0007 = "SAPR3_RFCFC_0007";

	static final String SAPR3_RFCFC_0008 = "SAPR3_RFCFC_0008";

	static final String SAPR3_RFCFC_0009 = "SAPR3_RFCFC_0009";

	static final String SAPR3_RFCFC_0010 = "SAPR3_RFCFC_0010";

	static final String SAPR3_RFCFC_0011 = "SAPR3_RFCFC_0011";

	static final String SAPR3_RFCFC_0012 = "SAPR3_RFCFC_0012";

	static final String SAPR3_RFCFC_0013 = "SAPR3_RFCFC_0013";

	static final String SAPR3_RFCFC_0014 = "SAPR3_RFCFC_0014";

	static final String SAPR3_RFCFC_0015 = "SAPR3_RFCFC_0015";

	static final String SAPR3_RFCFC_0016 = "SAPR3_RFCFC_0016";

	static final String SAPR3_RFCFC_0017 = "SAPR3_RFCFC_0017";

	static final String SAPR3_RFCFC_0018 = "SAPR3_RFCFC_0018";

	static final String SAPR3_RFCFC_0019 = "SAPR3_RFCFC_0019";

	static final String SAPR3_RFCFC_0020 = "SAPR3_RFCFC_0020";

	static final String SAPR3_RFCFC_0021 = "SAPR3_RFCFC_0021";

	static final String SAPR3_RFCFC_0022 = "SAPR3_RFCFC_0022";

	static final String SAPR3_RFCFC_0023 = "SAPR3_RFCFC_0023";

	static final String SAPR3_RFCFC_0024 = "SAPR3_RFCFC_0024";

	static final String SAPR3_RFCFC_0025 = "SAPR3_RFCFC_0025";

	static final String SAPR3_RFCFC_0026 = "SAPR3_RFCFC_0026";

	static final String SAPR3_RFCFC_0027 = "SAPR3_RFCFC_0027";

	static final String SAPR3_RFCFC_0028 = "SAPR3_RFCFC_0028";

	static final String SAPR3_RFCFC_0029 = "SAPR3_RFCFC_0029";

	static final String SAPR3_RFCFC_0030 = "SAPR3_RFCFC_0030";

	static final String SAPR3_RFCFC_0031 = "SAPR3_RFCFC_0031";

	static final String SAPR3_RFCFC_0032 = "SAPR3_RFCFC_0032";

	static final String SAPR3_RFCFC_0033 = "SAPR3_RFCFC_0033";

	static final String SAPR3_RFCFC_0034 = "SAPR3_RFCFC_0034";

	static final String SAPR3_RFCFC_0035 = "SAPR3_RFCFC_0035";

	static final String SAPR3_RFCFC_0036 = "SAPR3_RFCFC_0036";

	static final String SAPR3_RFCFC_0037 = "SAPR3_RFCFC_0037";

	static final String SAPR3_RFCFC_0038 = "SAPR3_RFCFC_0038";

	static final String SAPR3_RFCFC_0039 = "SAPR3_RFCFC_0039";

	static final String SAPR3_RFCFC_0040 = "SAPR3_RFCFC_0040";

	static final String SAPR3_RFCFC_0041 = "SAPR3_RFCFC_0041";

	static final String SAPR3_RFCFC_0042 = "SAPR3_RFCFC_0042";

	static final String SAPR3_RFCFC_0043 = "SAPR3_RFCFC_0043";

	static final String SAPR3_RFCFC_0044 = "SAPR3_RFCFC_0044";

	static final String SAPR3_RFCFC_0045 = "SAPR3_RFCFC_0045";

	static final String SAPR3_RFCFC_0046 = "SAPR3_RFCFC_0046";

	static final String SAPR3_RFCFC_0047 = "SAPR3_RFCFC_0047";

	static final String SAPR3_RFCFC_0048 = "SAPR3_RFCFC_0048";

	static final String SAPR3_RFCFC_0049 = "SAPR3_RFCFC_0049";

	static final String SAPR3_RFCFC_0050 = "SAPR3_RFCFC_0050";

	static final String SAPR3_RFCFC_0051 = "SAPR3_RFCFC_0051";
	
	static final String SAPR3_RFCFC_0052 = "SAPR3_RFCFC_0052";
	
	static final String SAPR3_RFCFC_0053 = "SAPR3_RFCFC_0053";

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
