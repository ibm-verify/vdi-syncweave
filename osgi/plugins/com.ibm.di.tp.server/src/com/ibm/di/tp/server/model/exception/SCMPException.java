/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class SCMPException extends Exception {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = -6757050057193128370L;

	private Map<String, String> details;

	private final ErrorCode code;

	private final long creationDate;

	private final int httpStatus;

	public SCMPException(ErrorCode code, String message, int httpStatus) {
		super(message);
		this.code = code;
		this.httpStatus = httpStatus;
		creationDate = System.currentTimeMillis();
	}

	public SCMPException(ErrorCode code, String message, int httpStatus, Throwable t) {
		super(message, t);
		this.code = code;
		this.httpStatus = httpStatus;
		creationDate = System.currentTimeMillis();
	}

	/**
	 * @return the {@link ErrorCode} set to this object
	 */
	public ErrorCode getCode() {
		return code;
	}

	/**
	 * @return the timestamp for the error.
	 */
	public long getCreationDate() {
		return creationDate;
	}

	/**
	 * @return the httpStatus
	 */
	public int getHttpStatus() {
		return httpStatus;
	}

	/**
	 * Adds a new detail record based on the suggested by {@link ErrorCode}
	 * details.
	 * 
	 * @param name
	 *            the name of the detail
	 * @param value
	 *            the value of the detail
	 */
	public void setDetail(String name, String value) {
		getDetailsMap().put(name, value);
	}

	/**
	 * @return the {@link Set} of details names already set for this object.
	 */
	public Set<String> getDetailsNames() {
		return getDetailsMap().keySet();
	}

	/**
	 * @param name
	 *            the name of the detail to get.
	 * @return the value of the detail for the corresponding name.
	 */
	public String getDetail(String name) {
		return getDetailsMap().get(name);
	}

	private Map<String, String> getDetailsMap() {
		if (details == null) {
			details = new HashMap<String, String>();
		}

		return details;
	}
}
