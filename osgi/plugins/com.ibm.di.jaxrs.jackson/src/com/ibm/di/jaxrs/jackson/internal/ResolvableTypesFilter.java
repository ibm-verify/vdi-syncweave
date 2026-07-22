/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.jaxrs.jackson.internal;

import com.fasterxml.jackson.databind.JavaType;

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
public interface ResolvableTypesFilter {

	/**
	 * Checks whether the type is resolvable without an additional metadata.
	 * 
	 * @param jt
	 *            the {@link JavaType}
	 * @return true if the particular type can be resolvable without additional
	 *         metadata, false otherwise.
	 */
	public boolean isTypeResolvable(JavaType jt);
}
