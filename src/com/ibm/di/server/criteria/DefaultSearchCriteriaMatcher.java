/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server.criteria;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.SearchCriteria.rscSearch;

/**
 * A default implementation which is performing criteria matches according to
 * the attribute value's type. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class DefaultSearchCriteriaMatcher implements SearchCriteriaMatcher {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The key in the map of customizers which this class will interpret as
	 * instruction to use the provided customizer for all value matches.
	 */
	public static final String MATCH_ALL_NAMES = "*";

	private boolean caseSensitive;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.server.criteria.SearchCriteriaMatcher#match(com.ibm.di.entry
	 * .Entry, com.ibm.di.server.SearchCriteria)
	 */
	public boolean match(Entry entry, SearchCriteria sc) {
		Map<String, MatchCustomizer> custom = new HashMap<String, MatchCustomizer>();
		custom.put(MATCH_ALL_NAMES, new DefaultMatcherCustomizer(caseSensitive));
		return match(entry, sc, custom);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.server.criteria.SearchCriteriaMatcher#match(com.ibm.di.entry
	 * .Entry, com.ibm.di.server.SearchCriteria, java.util.Map)
	 */
	public boolean match(Entry entry, SearchCriteria sc, Map<String, MatchCustomizer> customize) {
		Vector<?> criteria = sc.getCriteria();

		boolean match = false;
		for (int i = 0; i < criteria.size(); i++) {
			rscSearch rscSearch = (rscSearch) criteria.get(i);

			Attribute attr = entry.getAttribute(rscSearch.name);
			if (attr != null && customize != null) {
				MatchCustomizer c = customize.get(rscSearch.name);
				if (c == null) {
					c = customize.get("*");
				}

				if (c != null && attr.size() > 0) {
					if (rscSearch.value instanceof SearchCriteria) {
						// link criteria uses '@' for attribute value
						SearchCriteria newSearch = (SearchCriteria) rscSearch.value;

						Object[] vals = attr.getValues();
						boolean and = newSearch.getType() == SearchCriteria.SEARCH_AND;
						boolean internalMatch = false;
						outer: for (int j = 0; j < newSearch.getCriteria().size(); j++) {
							rscSearch attrSearch = (rscSearch) newSearch.getCriteria().get(j);
							for (int k = 0; k < vals.length; k++) {
								internalMatch = c.match(attrSearch, vals[k]);
								if (and ^ internalMatch) {
									break outer;
								}
							}
						}
						match = internalMatch;
					} else {
						match = c.match(rscSearch, attr.getValue(0));
					}
				}
			}

			if (sc.getType() == SearchCriteria.SEARCH_AND ^ match) {
				break;
			}
		}
		return match;
	}

	/**
	 * Set flag used to determinate case sensitivity.
	 * 
	 * @param caseSensitive
	 *            value to be set.
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	/**
	 * @return the case sensitivity flag. The default is false.
	 */
	public boolean getCaseSensitive() {
		return this.caseSensitive;
	}

}
