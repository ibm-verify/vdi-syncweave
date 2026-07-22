/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.core;

import java.util.Iterator;
import java.util.Vector;

import com.ibm.di.connector.maximo.exception.MxConnSchemaException;
import com.ibm.di.connector.maximo.exception.MxConnectorException;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.SearchCriteria;

/**
 * This class verifies if an Entry object matches a SearchCriteria object.
 * 
 * @since 7.1
 */
public final class EntryFilter {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	/**
	 * Indicates if an Entry object matches a SearchCriteria object.
	 * 
	 * @param searchCriteria
	 *            set of conditions which the Entry object should match
	 * @param entry
	 *            Entry object to be checked
	 * @return <code>true</code> if the Entry object matches the criteria,
	 *         otherwise <code>false</code>
	 * @throws MxConnectorException
	 *             if
	 *             <code>criteria.getType() != SearchCriteria.SEARCH_AND</code>
	 */
	@SuppressWarnings("unchecked")
	public static boolean match(final SearchCriteria searchCriteria, final Entry entry) throws MxConnectorException {

		checkSearchCriteria(searchCriteria);

		final Vector criteria = searchCriteria.getCriteria();

		for (final Iterator i = criteria.iterator(); i.hasNext();) {
			final Object obj = i.next();

			if (!(obj instanceof SearchCriteria.rscSearch)) {
				continue;
			}

			final SearchCriteria.rscSearch rsc = (SearchCriteria.rscSearch) obj;
			final Object entryValue = entry.getObject(rsc.name);
			final boolean result = equals(rsc.value, entryValue);

			if (rsc.match == SearchCriteria.EXACT && !result || rsc.match == SearchCriteria.NOT_STRING && result) {
				return false;
			}
		}

		return true;
	}

	private static boolean equals(final Object v1, final Object v2) {

		if (v1 == null || v2 == null) {
			return false;
		}

		if (v1.getClass().equals(v2.getClass())) {
			return v1.equals(v2);
		}

		return v1.toString().equals(v2.toString());
	}
	
	public static void checkSearchCriteria(final SearchCriteria criteria) throws MxConnSchemaException{
		if (criteria.getType() != SearchCriteria.SEARCH_AND) {
			throw new MxConnSchemaException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.UNSUPPORTED.CRITERIA.TYPE", criteria.getType()));
		}
	}
}
