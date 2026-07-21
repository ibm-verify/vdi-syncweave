/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

/**
 * This class represents an instance of BAPI RFC Return structure information.
 * 
 */
public final class AbapErrorInfo {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String ERROR = "E";

	private static final String WARN = "W";

	private static final String SUCCESS = "S";

	private static final String ABORT = "A";

	private static final String INFO = "I";

	private final String msg;

	private final Integer msgNum;

	private final boolean isError;

	private final boolean isWarn;

	private String internalSource;

	/**
	 * Create a new instance.
	 * 
	 * @param message
	 *            The message returned from SAP RFC.
	 * @param errorNum
	 *            The error number indicator.
	 * @param severityFlag
	 *            The severity indicator from the RFC.
	 * @throws IllegalArgumentException
	 *             if any params are null.
	 */
	public AbapErrorInfo(String message, String errorNum, String severityFlag)
			throws IllegalArgumentException {
		if (message == null) {
			throw new IllegalArgumentException();
		}
		if (errorNum == null) {
			throw new IllegalArgumentException();
		}
		if (severityFlag == null) {
			throw new IllegalArgumentException();
		}

		msg = message;
		msgNum = Integer.valueOf(errorNum);
		isError = severityFlag.equalsIgnoreCase(AbapErrorInfo.ERROR);
		isWarn = severityFlag.equalsIgnoreCase(AbapErrorInfo.WARN);
	}

	/**
	 * Get the message string.
	 * 
	 * @return The message.
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * Get the message number.
	 * 
	 * @return The message number.
	 */
	public Integer getMsgNum() {
		return msgNum;
	}

	/**
	 * Get the error indicator.
	 * 
	 * @return <code>true</code> if ABAP error was returned,
	 *         <code>false</code> otherwise.
	 */
	public boolean isError() {
		return isError;
	}

	/**
	 * Get the warning indicator.
	 * 
	 * @return <code>true</code> if ABAP warning was returned,
	 *         <code>false</code> otherwise.
	 */
	public boolean isWarn() {
		return isWarn;
	}

	/**
	 * Miscellaneous context information.
	 * 
	 * @param s
	 *            The value.
	 */
	public void setInternalSource(String s) {
		internalSource = s;
	}

	/**
	 * Miscellaneous context information.
	 * 
	 * @return s The value.
	 */
	public String getInternalSource() {
		return internalSource;
	}

	/**
	 * Make string representation of this instance.
	 * 
	 * @return A formatted string of the as follows: {SEVERITY}: {MESSAGE}
	 *         {(ERROR_NUMBER)} {(CONTEXT)}.
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		String sev;
		if (isError()) {
			sev = AbapErrorInfo.ERROR;
		} else if (isWarn()) {
			sev = AbapErrorInfo.WARN;
		} else {
			sev = AbapErrorInfo.SUCCESS;
		}

		result.append(sev);
		result.append(": ");
		result.append(getMsg());
		result.append(" (");
		result.append(getMsgNum().intValue());
		result.append(")");

		if (getInternalSource() != null) {
			result.append(" (");
			result.append(getInternalSource());
			result.append(")");
		}

		return result.toString();
	}

}
