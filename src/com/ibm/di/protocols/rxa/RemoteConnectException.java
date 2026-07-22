/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

/**
 * A Remote Command Line Function Component Remote Connect Exception. Indicates
 * that the connection could not be successfully established with the target.
 */
public class RemoteConnectException extends GeneralCLFCException {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Construct exception with a context message.
	 * 
	 * @param msg
	 *            The message text.
	 */
	public RemoteConnectException(String msg) {
		super(msg);
	}

	/**
	 * Construct an exception of this type as result of another lower level
	 * exception. The message of this exception will be adopted from the root
	 * exception.
	 * 
	 * @param root
	 *            The cause of the this exception.
	 */
	public RemoteConnectException(Throwable root) {
		super(root);
	}

	/**
	 * Construct an exception of this type with a context message and lower
	 * level exception cause.
	 * 
	 * @param msg
	 *            The message text.
	 * @param root
	 *            The cause of the this exception.
	 */
	public RemoteConnectException(String msg, Throwable root) {
		super(msg, root);
	}

	/**
	 * Construct exception with a context message and the specified message ID.
	 * 
	 * @param c
	 *            RemoteCLFCMsgIds: Message ID
	 * @param msg
	 *            The message text.
	 */
	public RemoteConnectException(MsgIds c, String msg) {
		super(c, msg);
	}

	/**
	 * Construct an exception of this type with a context message, lower level
	 * exception cause and specified message ID.
	 * 
	 * @param c
	 *            RemoteCLFCMsgIds: Message ID
	 * @param msg
	 *            The message text.
	 * @param root
	 *            The cause of the this exception.
	 */
	public RemoteConnectException(MsgIds c, String msg, Throwable root) {
		super(c, msg, root);
	}

}
