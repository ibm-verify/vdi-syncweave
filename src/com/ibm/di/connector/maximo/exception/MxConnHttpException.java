/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.exception;

/**
 * TPAE IF Connector's HTTP exception. Thrown to indicate that the HTTP server
 * returns a response code different than 200 (HTTP OK).
 * 
 * @since TDI 7.1
 */
public class MxConnHttpException extends MxConnIOException {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 1;

    private final int responseCode;
	
    private final String responseMessage;
    
    private final String body;

	/**
	 * Constructs a new {@link MxConnHttpException}.
	 * 
	 * @param targetUrl
	 *            target URL
	 * @param responseCode
	 *            HTTP response code
	 */
	public MxConnHttpException(final String msg, final String targetUrl, final int responseCode, final String responseMessage,
			final String body) {
		super(msg, targetUrl, responseCode, responseMessage, body, targetUrl);
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.body = body;
	}

	/**
	 * Returns the HTTP response code.
	 * 
	 * @return HTTP response code
	 */
	public final int getResponseCode() {
		return responseCode;
	}

	/**
	 * Returns the HTTP response message.
	 * 
	 * @return HTTP response message
	 */
	public final String getResponseMessage() {
		return responseMessage;
	}

	/**
	 * Returns the body content of the HTTP response message.
	 * 
	 * @return body content of the HTTP response message
	 */
	public final String getBody() {
		return body;
	}
}
