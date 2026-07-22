/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server.criteria;

import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.SearchCriteria.rscSearch;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class DefaultMatcherCustomizer implements MatchCustomizer {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private final boolean caseSensitive;

	public DefaultMatcherCustomizer(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.ibm.di.server.criteria.MatchCustomizer#match(com.ibm.di.server.
	 * SearchCriteria.rscSearch, java.lang.Object)
	 */
	@Override
	public boolean match(rscSearch criterion, Object value) {
		Object criteriaValue = criterion.value;
		if (value == null || criteriaValue == null) {
			return false;
		}

		boolean match = false;
		switch (criterion.match) {
		case SearchCriteria.EXACT: {
			match = equals(value, criteriaValue);
			break;
		}
		case SearchCriteria.INITIAL_STRING: {
			match = initialString(value, criteriaValue);
			break;
		}
		case SearchCriteria.FINAL_STRING: {
			match = finalString(value, criteriaValue);
			break;
		}
		case SearchCriteria.NOT_STRING: {
			match = notEquals(value, criteriaValue);
			break;
		}
		case SearchCriteria.SUBSTRING: {
			match = subString(value, criteriaValue);
			break;
		}
		case SearchCriteria.LESS_THAN: {
			match = lessThan(value, criteriaValue);
			break;
		}
		case SearchCriteria.LESS_THAN_OR_EQUAL: {
			match = lessThanOrEqual(value, criteriaValue);
			break;
		}
		case SearchCriteria.GREATER_THAN: {
			match = greaterThan(value, criteriaValue);
			break;
		}
		case SearchCriteria.GREATER_THAN_OR_EQUAL: {
			match = greaterThanOrEqual(value, criteriaValue);
			break;
		}
		default: {
			match = false;
			break;
		}
		}

		if (criterion.negate) {
			match = !match;
		}
		return match;
	}

	/**
	 * @param value
	 * @param criteriaValue
	 * @return
	 */
	private boolean equals(Object value, Object criteriaValue) {
		if (value instanceof CharSequence) {
			return caseSensitive ? value.toString().equals(criteriaValue.toString()) : value.toString().equalsIgnoreCase(
					criteriaValue.toString());
		}
		return criteriaValue.equals(value);
	}

	/**
	 * @param value
	 * @param criteriaValue
	 * @return
	 */
	private boolean initialString(Object value, Object criteriaValue) {
		String val = value.toString();
		String critVal = criteriaValue.toString();
		return val.regionMatches(!caseSensitive, 0, critVal, 0, critVal.length());
	}

	/**
	 * @param value
	 * @param criteriaValue
	 * @return
	 */
	private boolean finalString(Object value, Object criteriaValue) {
		String val = value.toString();
		String critVal = criteriaValue.toString();
		return val.regionMatches(!caseSensitive, val.length() - critVal.length(), critVal, 0, critVal.length());
	}

	/**
	 * @param value
	 * @param criteriaValue
	 * @return
	 */
	private boolean notEquals(Object value, Object criteriaValue) {
		return !equals(value, criteriaValue);
	}

	/**
	 * @param value
	 * @param criteriaValue
	 * @return
	 */
	private boolean subString(Object value, Object criteriaValue) {
		String val = value.toString();
		String critVal = criteriaValue.toString();

		if (!caseSensitive) {
			val = val.toLowerCase();
			critVal = critVal.toLowerCase();
		}

		return val.contains(critVal);
	}

	private int compareVals(Object value, Object criteriaValue) {
		if (!(criteriaValue instanceof Comparable)) {
			criteriaValue = criteriaValue.toString();
		}
		if (!(value instanceof Comparable)) {
			value = value.toString();
		}

		return ((Comparable) value).compareTo(criteriaValue);
	}

	/**
	 * @param value
	 * @param criteriaValue
	 * @return
	 */
	private boolean lessThan(Object value, Object criteriaValue) {
		return compareVals(value, criteriaValue) < 0;
	}

	/**
	 * @param value
	 * @param criteriaValue
	 * @return
	 */
	private boolean lessThanOrEqual(Object value, Object criteriaValue) {
		return compareVals(value, criteriaValue) <= 0;
	}

	/**
	 * @param value
	 * @param criteriaValue
	 * @return
	 */
	private boolean greaterThan(Object value, Object criteriaValue) {
		return compareVals(value, criteriaValue) > 0;
	}

	/**
	 * @param value
	 * @param criteriaValue
	 * @return
	 */
	private boolean greaterThanOrEqual(Object value, Object criteriaValue) {
		return compareVals(value, criteriaValue) >= 0;
	}

}
