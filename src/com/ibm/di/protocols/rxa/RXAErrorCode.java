/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

/**
 * Typesafe enum of the expected RXA toolkit error codes
 */
public class RXAErrorCode {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * RXA toolkit error ID
	 */
	private final String errCode;

	/**
	 * Remote CLFC error ID
	 */
	private final MsgIds correspondingCLFCerr;

	/**
	 * Constructor
	 * 
	 * @param e
	 *            RXA toolkit error ID
	 * @param corresp
	 *            Remote CLFC error ID
	 */
	private RXAErrorCode(String e, MsgIds corresp) {
		errCode = e;
		correspondingCLFCerr = corresp;
	}

	/**
	 * Return the RXA toolkit error ID associated with this ErrorCode object
	 * 
	 * @return String RXA toolkit error ID
	 */
	public String toString() {
		return errCode;
	}

	/**
	 * Return the Remote CLFC error ID that corresponds to this RXA error
	 * 
	 * @return String Remote CLFC error ID
	 */
	public MsgIds getCorrespondingCode() {
		return correspondingCLFCerr;
	}

	/*
	 * RXA Toolkit Error Codes
	 */

	/**
	 * Error code for invalid credentials
	 */
	public static final RXAErrorCode RXATOOLKIT_0E = new RXAErrorCode(
			"CTGRI0000E", MsgIds.INVALID_CREDENTIALS);

	/**
	 * Error code for connection failure
	 */
	public static final RXAErrorCode RXATOOLKIT_1E = new RXAErrorCode(
			"CTGRI0001E", MsgIds.CANNOT_CONNECT_TO_HOST);

	/**
	 * Error code for path not found
	 */
	public static final RXAErrorCode RXATOOLKIT_3E = new RXAErrorCode(
			"CTGRI0003E", MsgIds.PATH_NOT_FOUND);

	/**
	 * Error code for file read error
	 */
	public static final RXAErrorCode RXATOOLKIT_4E = new RXAErrorCode(
			"CTGRI0004E", MsgIds.FILE_READ_ERROR);

	/**
	 * Error code for remote reading problem
	 */
	public static final RXAErrorCode RXATOOLKIT_7E = new RXAErrorCode(
			"CTGRI0007E", MsgIds.ERROR_READING_REMOTE);

	/**
	 * Error code for path removal failure
	 */
	public static final RXAErrorCode RXATOOLKIT_9E = new RXAErrorCode(
			"CTGRI0009E", MsgIds.CANNOT_REMOVE_PATH);

	/**
	 * Error code for remote file system error
	 */
	public static final RXAErrorCode RXATOOLKIT_10E = new RXAErrorCode(
			"CTGRI0010E", MsgIds.REMOTE_FILESYSTEM_ERROR);

	/**
	 * Error code for unique service not found
	 */
	public static final RXAErrorCode RXATOOLKIT_14E = new RXAErrorCode(
			"CTGRI0014E", MsgIds.UNIQUE_SERVICE_NOT_FOUND);

	/**
	 * Error code for windows binary error
	 */
	public static final RXAErrorCode RXATOOLKIT_15E = new RXAErrorCode(
			"CTGRI0015E", MsgIds.WINDOWS_BINARY_ERROR);

	/**
	 * Error code if service is not started
	 */
	public static final RXAErrorCode RXATOOLKIT_16E = new RXAErrorCode(
			"CTGRI0016E", MsgIds.SERVICE_NOT_STARTED);

	/**
	 * Error code for rsh binding unsuccessful
	 */
	public static final RXAErrorCode RXATOOLKIT_19E = new RXAErrorCode(
			"CTGRI0019E", MsgIds.RSH_BIND_UNSUCCESSFUL);

	/**
	 * Error code for remote user not found
	 */
	public static final RXAErrorCode RXATOOLKIT_20E = new RXAErrorCode(
			"CTGRI0020E", MsgIds.REMOTE_USER_NOT_FOUND);

	/**
	 * Error code for unauthorized shutdown
	 */
	public static final RXAErrorCode RXATOOLKIT_22E = new RXAErrorCode(
			"CTGRI0022E", MsgIds.SHUTDOWN_NOT_AUTHORIZED);

	/**
	 * Error code for remote execution error
	 */
	public static final RXAErrorCode RXATOOLKIT_23E = new RXAErrorCode(
			"CTGRI0023E", MsgIds.REMOTE_EXECUTION_ERROR);

}
