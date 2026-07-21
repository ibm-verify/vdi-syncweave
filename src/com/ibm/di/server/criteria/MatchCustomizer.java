/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server.criteria;

import com.ibm.di.server.SearchCriteria.rscSearch;

/**
 * Defines a contract for plugging in a customization of the default matching
 * mechanism. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public interface MatchCustomizer {

	/**
	 * Matches all values of the specified attribute against the specified
	 * criterion. If more than one value of an attribute must be matched this
	 * method will be called several times with different values for
	 * {@link rscSearch#value}.
	 * 
	 * @param criterion
	 *            the defined criteria
	 * @param value
	 *            the value found in the Attribute of the provided entry
	 * @return true if the specified value match, false otherwise
	 * @throws IllegalArgumentException
	 *             thrown to denote unexpected data is provided. Make sure an
	 *             appropriate message is displayed as the exception will be
	 *             propagated back to the user.
	 */
	public boolean match(rscSearch criterion, Object value);
}
