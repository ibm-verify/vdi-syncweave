/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm.cdm.query;

/**
 * Generic Filter for TADDM Query filters.
 * 
 */
public interface QueryFilter {

	/**
	 * Filters the provided MQL query and returns the result.
	 * 
	 * @param query
	 *            input MQL query.
	 * @return filtered MQL query.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public String filter(String query) throws Exception;
}