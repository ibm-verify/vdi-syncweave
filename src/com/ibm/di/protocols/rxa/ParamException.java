/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

/**
 * A Remote Command Line Function Component Parameter Exception. Indicates that
 * incorrect/insufficient configuration parameters have been specified.
 */
public class ParamException extends GeneralCLFCException {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Create a ParamException with the specified error message
	 * 
	 * @param msg
	 *            The error message
	 */
	public ParamException(String msg) {
		super(msg);
	}

	/**
	 * Create a ParamException with the specified error message and MsgId
	 * 
	 * @param c
	 *            RemoteCLFCMsgIds: Message ID for this message
	 * @param msg
	 *            Message content
	 */
	public ParamException(MsgIds c, String msg) {
		super(c, msg);
	}

}
