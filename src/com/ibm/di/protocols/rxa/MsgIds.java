/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

/**
 * Typesafe enum of the RemoteCLFC message ids
 */
public class MsgIds implements Messages.MessageID {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * ID of the message
	 */
	private final String msgID;

	/**
	 * Constructor using message id
	 * 
	 * @param code
	 *            the ID
	 */
	private MsgIds(String code) {
		msgID = code;
	}

	/**
	 * Return the Remote CLFC message ID
	 * 
	 * @return String Remote CLFC message ID
	 */
	public String toString() {
		return msgID;
	}

	/*
	 * Valid message ids for the Remote CLFC
	 */

	/**
	 * Message ID
	 */
	public static final MsgIds CANNOT_CONNECT_TO_HOST = new MsgIds(
			"CTGDJC001E");

	/**
	 * Message ID
	 */
	public static final MsgIds PATH_NOT_FOUND = new MsgIds(
			"CTGDJC002E");

	/**
	 * Message ID
	 */
	public static final MsgIds FILE_READ_ERROR = new MsgIds(
			"CTGDJC003E");

	/**
	 * Message ID
	 */
	public static final MsgIds CANNOT_REMOVE_PATH = new MsgIds(
			"CTGDJC004E");

	/**
	 * Message ID
	 */
	public static final MsgIds REMOTE_FILESYSTEM_ERROR = new MsgIds(
			"CTGDJC005E");

	/**
	 * Message ID
	 */
	public static final MsgIds UNIQUE_SERVICE_NOT_FOUND = new MsgIds(
			"CTGDJC006E");

	/**
	 * Message ID
	 */
	public static final MsgIds WINDOWS_BINARY_ERROR = new MsgIds(
			"CTGDJC007E");

	/**
	 * Message ID
	 */
	public static final MsgIds RSH_BIND_UNSUCCESSFUL = new MsgIds(
			"CTGDJC008E");

	/**
	 * Message ID
	 */
	public static final MsgIds REMOTE_USER_NOT_FOUND = new MsgIds(
			"CTGDJC009E");

	/**
	 * Message ID
	 */
	public static final MsgIds SHUTDOWN_NOT_AUTHORIZED = new MsgIds(
			"CTGDJC010E");

	/**
	 * Message ID
	 */
	public static final MsgIds REMOTE_EXECUTION_ERROR = new MsgIds(
			"CTGDJC011E");

	/**
	 * Message ID
	 */
	public static final MsgIds NOT_INITIALIZED = new MsgIds(
			"CTGDJC012E");

	/**
	 * Message ID
	 */
	public static final MsgIds TERMINATED = new MsgIds(
			"CTGDJC013E");

	/**
	 * Message ID
	 */
	public static final MsgIds NO_COMMAND = new MsgIds(
			"CTGDJC014E");

	/**
	 * Message ID
	 */
	public static final MsgIds MISSING_PARAMS = new MsgIds(
			"CTGDJC015E");

	/**
	 * Message ID
	 */
	public static final MsgIds SERVICE_NOT_STARTED = new MsgIds(
			"CTGDJC016E");

	/**
	 * Message ID
	 */
	public static final MsgIds GENERAL_RXA_EXCEPTION = new MsgIds(
			"CTGDJC017E");

	/**
	 * Message ID
	 */
	public static final MsgIds INVALID_PERFORM_OBJ = new MsgIds(
			"CTGDJC018E");

	/**
	 * Message ID
	 */
	public static final MsgIds ERROR_READING_REMOTE = new MsgIds(
			"CTGDJC019E");

	/**
	 * Message ID
	 */
	public static final MsgIds TIMEOUT = new MsgIds(
			"CTGDJC020E");

	/**
	 * Message ID
	 */
	public static final MsgIds INVALIDSRCFILE = new MsgIds(
			"CTGDJC021E");

	/**
	 * Message ID
	 */
	public static final MsgIds INVALIDCONNTYPE = new MsgIds(
			"CTGDJC022E");

	/**
	 * Message ID
	 */
	public static final MsgIds INVALID_CREDENTIALS = new MsgIds(
			"CTGDJC023E");

	/**
	 * Message ID
	 */
	public static final MsgIds INPUT_ATTR_EXISTS = new MsgIds(
			"CTGDJC024I");

	/**
	 * Message ID
	 */
	public static final MsgIds OPTION_VALUE_SET = new MsgIds(
			"CTGDJC025I");

	/**
	 * Message ID
	 */
	public static final MsgIds PRIOR_TO_EXECUTION = new MsgIds(
			"CTGDJC026I");

	/**
	 * Message ID
	 */
	public static final MsgIds EXECUTION_COMPLETE = new MsgIds(
			"CTGDJC027I");

	/**
	 * Message ID
	 */
	public static final MsgIds NO_CONNECTION = new MsgIds(
			"CTGDJC028I");

	/**
	 * Message ID
	 */
	public static final MsgIds START_INIT = new MsgIds(
			"CTGDJC029I");

	/**
	 * Message ID
	 */
	public static final MsgIds COMPLETE_INIT = new MsgIds(
			"CTGDJC030I");

	/**
	 * Message ID
	 */
	public static final MsgIds INITIALIZING_PARAMS = new MsgIds(
			"CTGDJC031I");

	/**
	 * Message ID
	 */
	public static final MsgIds INIT_OPTION = new MsgIds(
			"CTGDJC032I");

	/**
	 * Message ID
	 */
	public static final MsgIds FINDING_PROTOCOL = new MsgIds(
			"CTGDJC033I");

	/**
	 * Message ID
	 */
	public static final MsgIds PROTOCOL_CHOSEN = new MsgIds(
			"CTGDJC034I");

	/**
	 * Message ID
	 */
	public static final MsgIds CONNECTION_UNSUCCESSFUL = new MsgIds(
			"CTGDJC035I");

	/**
	 * Message ID
	 */
	public static final MsgIds INIT_PARAMS_DONE = new MsgIds(
			"CTGDJC036I");

	/**
	 * Message ID
	 */
	public static final MsgIds REMOTE_CONNECT_ERROR = new MsgIds(
			"CTGDJC037E");

	/**
	 * Message ID
	 */
	public static final MsgIds CREATING_CONNECTION = new MsgIds(
			"CTGDJC038I");

	/**
	 * Message ID
	 */
	public static final MsgIds SESSION_BEGIN = new MsgIds(
			"CTGDJC039I");

	/**
	 * Message ID
	 */
	public static final MsgIds SESSION_STARTED = new MsgIds(
			"CTGDJC040I");

	/**
	 * Message ID
	 */
	public static final MsgIds NO_SUITABLE_PROTOCOL = new MsgIds(
			"CTGDJC041I");

	/**
	 * Message ID
	 */
	public static final MsgIds CREATE_CONN_NO_PASSWD = new MsgIds(
			"CTGDJC042I");

	/**
	 * Message ID
	 */
	public static final MsgIds SSH_CONN_KEYSTORE = new MsgIds(
			"CTGDJC043I");

	/**
	 * Message ID
	 */
	public static final MsgIds NOSUCHFILE = new MsgIds(
			"CTGDJC044I");

	/**
	 * Message ID
	 */
	public static final MsgIds EITHER_SSH_OR_RSH = new MsgIds(
			"CTGDJC045I");

	/**
	 * Message ID
	 */
	public static final MsgIds RSH_ONLY = new MsgIds(
			"CTGDJC046I");

	/**
	 * Message ID
	 */
	public static final MsgIds DESTN_DIR_SET = new MsgIds(
			"CTGDJC047I");

	/**
	 * Message ID
	 */
	public static final MsgIds STDIN_PROVIDED = new MsgIds(
			"CTGDJC048I");

	/**
	 * Message ID
	 */
	public static final MsgIds COMPLETE_CMD = new MsgIds(
			"CTGDJC049");

	/**
	 * Message ID
	 */
	public static final MsgIds FILE_TRANSFERRED = new MsgIds(
			"CTGDJC050I");

	/**
	 * Message ID
	 */
	public static final MsgIds REMOVE_DIR = new MsgIds(
			"CTGDJC051I");

	/**
	 * Message ID
	 */
	public static final MsgIds WRONG_TYPE_FOR_ATTR = new MsgIds(
			"CTGDJC052I");

	/**
	 * Message ID
	 */
	public static final MsgIds WIN_NO_PORT_ALLOWED = new MsgIds(
			"CTGDJC053I");

	/**
	 * Message ID
	 */
	public static final MsgIds RXA_DEBUG_MSG = new MsgIds(
			"CTGDJC054I");

	/**
	 * Message ID
	 */
	public static final MsgIds RXA_INFO_MSG = new MsgIds(
			"CTGDJC055I");

	/**
	 * Message ID
	 */
	public static final MsgIds RXA_WARN_MSG = new MsgIds(
			"CTGDJC056I");

	/**
	 * Message ID
	 */
	public static final MsgIds RXA_ERROR_MSG = new MsgIds(
			"CTGDJC057E");
	
	/**
	 * Message ID
	 */
	public static final MsgIds CREATE_DIR = new MsgIds(
			"CTGDJC058I");
}
