/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import org.w3c.dom.Document;

/**
 * Adapter for IBM Tivoli Directory Integrator Function Components.
 * 
 */
interface XmlFunctionAdapter {

	/**
	 * Execution the function.
	 * 
	 * @throws FunctionExecutionException
	 */
	void execute() throws FunctionExecutionException,
			EmptyTransformResultException;

	/**
	 * Get the result as a DOM Document.
	 * 
	 * @return The result as DOM.
	 * @throws FunctionExecutionException
	 *             If a converstion from <code>String</code> to
	 *             <code>Document</code> is required, this exception will wrap
	 *             the XML parse exception.
	 * @throws IllegalStateException
	 *             if called before {@link #execute} or an exception was thrown
	 *             during {@link #execute}.
	 */
	Document getResultAsDocument() throws FunctionExecutionException;

	/**
	 * Get the result as an XML String.
	 * 
	 * @return The result as String.
	 * @throws FunctionExecutionException
	 *             If a converstion from <code>String</code> to
	 *             <code>Document</code> is required, this exception will wrap
	 *             the XML parse exception.
	 * @throws IllegalStateException
	 *             if called before {@link #execute} or an exception was thrown
	 *             during {@link #execute}.
	 */
	String getResultAsString() throws FunctionExecutionException;

	/**
	 * Sets miscellaneous parameters that may be used by concrete impls during
	 * {@link #execute().
	 * 
	 * @param filter
	 *            The parameters. May be <code>null</code> to unset the
	 *            criteria.
	 */
	void setCriteria(ExecutionCriteria filter);

	/**
	 * Gets miscellaneous parameters that may be used by concrete impls during
	 * {@link #execute().
	 * 
	 * @return crit
	 *            The parameters.
	 */
	ExecutionCriteria getCriteria();

	/**
	 * Dispose of the internal connection to SAP.
	 * 
	 */
	void dispose() throws FunctionExecutionException;

}
