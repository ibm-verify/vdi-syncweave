/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

/**
 * Interface implemented by connectors that have the ability modify/delete
 * entries without doing a lookup first.
 */
public interface SkipLookupInterface {

	/**
	 * Returns the number of entries affected when the lookup is skipped
	 * 
	 * @return number of entries affected
	 */
	public int getNumSkipLookupAffected();
}
