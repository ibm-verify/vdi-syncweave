/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server.criteria;

import java.util.Map;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.SearchCriteria;

/**
 * An interface defining a contract for matching an Entry's values against a
 * built {@link SearchCriteria}.
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public interface SearchCriteriaMatcher {

	/**
	 * Matches the specified entry against the defined search criteria using the
	 * default mechanism.
	 * 
	 * @param entry
	 *            the actual entry to match
	 * @param sc
	 *            the criteria definition
	 * @return true if the entry matches the defined search criteria
	 */
	boolean match(Entry entry, SearchCriteria sc);

	/**
	 * Matches specified entry against the defined search criteria using the
	 * specified customization mechanism.
	 * 
	 * @param entry
	 *            the actual entry to match
	 * @param sc
	 *            the criteria definition
	 * @param customize
	 *            a map containing the name of the attributes for which a
	 *            customization should be applied. A match-all * can be used to
	 *            match all attributes.
	 * @return
	 */
	boolean match(Entry entry, SearchCriteria sc, Map<String, MatchCustomizer> customize);
}
