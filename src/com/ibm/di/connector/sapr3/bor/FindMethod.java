/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import java.io.File;

import org.w3c.dom.Document;

/**
 * Representation of method that performs read or find operations against the
 * connected data repository.
 * 
 * A read method is used by the connector during
 * {@link SapR3BorConnector#findEntry()}, and
 * {@link SapR3BorConnector#selectEnties()}.
 * 
 * 
 */
interface FindMethod extends ConnectorMethod {

	/**
	 * Typesafe enum for indicating the response format required by clients
	 * following a find operation.
	 */
	static final class ResponseFormat {
		ResponseFormat() {
			super();
		}
	}

	/**
	 * The typesafe enum for XML string response format.
	 */
	ResponseFormat XML_STRING = new ResponseFormat();

	/**
	 * The typesafe enum for XML DOM document response format.
	 */
	ResponseFormat XML_DOM_DOC = new ResponseFormat();

	/**
	 * Executes the read operation method.
	 * 
	 * @param inXml
	 *            The input XML.
	 * @param filter
	 *            The filter used to narrow the result set range.
	 * @param format
	 *            The preferred format of the return value.
	 * 
	 * @return The response. Formatted according to <code>format</code>. If
	 *         <code>format</code> is {@link XML_STRING} then the type will be
	 *         <code>String</code>. If <code>format</code> is
	 *         {@link XML_DOM_DOC} then the type will be {@link Document}.
	 * 
	 * @throws ConnectorMethodException
	 *             if invocation fails.
	 * @throws IllegalArgumentExcepton
	 *             if any parameters are <code>null</code>.
	 * @throws EmptyTransformResultException
	 *             if post XSL returns empty document.
	 */
	Object execute(Document inXml, ExecutionCriteria filter,
			ResponseFormat format) throws ConnectorMethodException,
			EmptyTransformResultException;

	/**
	 * Executes the read operation method.
	 * {@see #execute(Document, ExecutionCriteria, ResponseFormat)}.
	 */
	Object execute(String inXml, ExecutionCriteria filter, ResponseFormat format)
			throws ConnectorMethodException, EmptyTransformResultException;

	/**
	 * Executes the read operation method.
	 * 
	 * @param filter
	 *            The filter used to narrow the result set range.
	 * @param format
	 *            The preferred format of the return value.
	 * 
	 * @return The response. Formatted according to <code>format</code>. If
	 *         <code>format</code> is {@link XML_STRING} then the type will be
	 *         <code>String</code>. If <code>format</code> is
	 *         {@link XML_DOM_DOC} then the type will be {@link Document}.
	 * 
	 * @throws ConnectorMethodException
	 *             if invocation fails.
	 * @throws IllegalArgumentExcepton
	 *             if any parameters are <code>null</code>.
	 * @throws EmptyTransformResultException
	 *             if post XSL returns empty document.
	 */
	Object execute(ExecutionCriteria filter, ResponseFormat format)
			throws ConnectorMethodException, EmptyTransformResultException;

	/**
	 * Get the pre RFC call XSL style sheet file. The XSL must produce an XML
	 * RFC document that can be executed by the SAP RFC Function Component.
	 * 
	 * @return The XSL file form the configuration.
	 */
	File getPreCallXsl();

	/**
	 * Get the post RFC call XSL style sheet file. The XSL must produce an XML
	 * document that conforms the XSchema for SAP user XML.
	 * 
	 * @return The XSL file form the configuration.
	 */
	File getPostCallXsl();

}
