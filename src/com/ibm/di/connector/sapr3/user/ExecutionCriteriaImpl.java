/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ibm.di.server.SearchCriteria;

/**
 * Implementation of Execution Criteria. This class wraps the TDI
 * SearchCriteria instance.
 */
final class ExecutionCriteriaImpl implements ExecutionCriteria {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final Map searchCriteria;

	/**
	 * Create a new default instance.
	 * 
	 */
	ExecutionCriteriaImpl() {
		super();
		searchCriteria = new HashMap();
	}

	/**
	 * Create a new instance.
	 * 
	 * @param sc
	 *            The raw TDI search criteria.
	 */
	ExecutionCriteriaImpl(SearchCriteria sc) {
		super();
		searchCriteria = new HashMap();
		if (sc != null) {
			for (int i = 0; i < sc.size(); ++i) {
				searchCriteria.put(sc.getCriteria(i).name,
						sc.getCriteria(i).value);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.ExecutionCriteria#getParam(java.lang.String)
	 */
	public String getParam(String name) {
		return ((String) searchCriteria.get(name));
	}

	public void setParam(String name, String value) {
		searchCriteria.put(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.ExecutionCriteria#getParamNames()
	 */
	public String[] getParamNames() {
		Set keys = searchCriteria.keySet();
		return ((String[]) keys.toArray(new String[keys.size()]));
	}

}
