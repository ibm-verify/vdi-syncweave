/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.util;

import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomFeed;

import com.ibm.di.web.common.atom.AtomText;
/**
 * Factory class to create AtomFeed and AtomEntry instances.
 * Uses custom Atom implementation to avoid OSGi/JAXB classloading issues.
 * 
 * The custom implementation is in the same OSGi bundle as the REST handlers,
 * eliminating cross-bundle resource access issues that occurred with Apache Wink.
 */
public class AtomFeedFactory {
	
	/**
	 * Creates a new AtomFeed instance.
	 *
	 * @return new AtomFeed instance
	 */
	public static AtomFeed createAtomFeed() {
		return new AtomFeed();
	}
	
	/**
	 * Creates a new AtomEntry instance.
	 *
	 * @return new AtomEntry instance
	 */
	public static AtomEntry createAtomEntry() {
		return new AtomEntry();
	}
}