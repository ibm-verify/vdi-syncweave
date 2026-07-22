/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import org.w3c.dom.Document;

/**
 * Representation of method that performs write operations against the connected
 * data repository.
 * 
 * Write method is used by the connector during
 * {@link SapR3BorConnector#putEntry()}, {@link SapR3BorConnector#modEntry()},
 * and {@link SapR3BorConnector#deleteEntry()}.
 * 
 */
interface WriteMethod extends ConnectorMethod {

	/**
	 * Executes the write operation method.
	 * 
	 * @param inXml
	 *            The XML representing the object details to be written.
	 * @param crita
	 *            The optional parameters that control execution behaviour.
	 * 
	 * @throws ConnectorMethodException
	 *             if invocation fails.
	 * @throws IllegalArgumentExcepton
	 *             if any parameter is <code>null</code>;
	 */
	void execute(Document inXml, ExecutionCriteria crita)
			throws ConnectorMethodException;

	/**
	 * Executes the write operation method.
	 * 
	 * @param inXml
	 *            The XML representing the object details to be written.
	 * @param crita
	 *            The optional parameters that control execution behaviour.
	 * 
	 * @throws ConnectorMethodException
	 *             if invocation fails.
	 * @throws IllegalArgumentExcepton
	 *             if any parameter is <code>null</code>;
	 */
	void execute(String inXml, ExecutionCriteria crita)
			throws ConnectorMethodException;

}
