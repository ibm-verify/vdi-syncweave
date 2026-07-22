/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.user;

import java.util.List;

/**
 * Represents a cache for ABAP RFC return results. It it used by the Connector
 * to cache AbapErrorInfo instances recorded during the execution of any of the
 * connector methods. The cache will be registered as a scripting bean with IBM Tivoli Directory Integrator
 * scripting engine. It will allow connector hooks to access the ABAP errors
 * that might have occured during connector processing. The hook code can then
 * execute required behaviour.
 */
public interface AbapErrorCache {

	/**
	 * Allows the caller to obtain a list of ABAP warnings that might have
	 * occured during the execution of any supported Connector method.
	 * 
	 * @return a list of {@link AbapErrorInfo} at warning severity. Minimum
	 *         length will be zero.
	 */
	List getLastWarningSet();

	/**
	 * Allows the caller to obtain a list of ABAP errors that might have occured
	 * during the execution of any supported Connector method.
	 * 
	 * @return a list of {@link AbapErrorInfo} at error severity. Minimum length
	 *         will be zero.
	 */
	List getLastErrorSet();
}
