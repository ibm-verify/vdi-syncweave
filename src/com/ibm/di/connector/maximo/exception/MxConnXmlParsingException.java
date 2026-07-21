/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.exception;

/**
 * TPAE IF Connector's XML parsing exception. Thrown to indicate the
 * connector could not parse an XML content.
 * 
 * @since 7.1
 */
public class MxConnXmlParsingException extends MxConnectorException {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 1;

	private final String xml;

	/**
	 * Constructs a new {@link MxConnXmlParsingException}.
	 * 
	 * @param msg
	 *            the detail message
	 * @param xml
	 *            XML contents
	 * @param cause
	 *            the cause
	 */
	public MxConnXmlParsingException(final String msg, final String xml, final Throwable cause) {
		super(msg, cause, xml);
		this.xml = xml;
	}

	/**
	 * Returns the XML content.
	 * 
	 * @return XML content
	 */
	public String getXml() {
		return xml;
	}
}
