/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.util.typeconverter;

import com.ibm.di.util.StringUtils;

/**
 * Maximo Type Converter for {@link String} types.
 * 
 * @since 7.1
 * @see Long
 */
public final class StringConverter implements IMxTypeConverter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final StringConverter INSTANCE = new StringConverter();

	/**
	 * Returns an instance of {@link StringConverter}.
	 * 
	 * @return instance of {@link StringConverter}
	 */
	public static StringConverter getInstance() {

		return INSTANCE;
	}

	private StringConverter() {

	}

	/**
	 * {@inheritDoc}
	 */
	public String toString(final Object value) {

		if (value == null || StringUtils.isBlank(value.toString())) {
			return "";
		}

		return value.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object valueOf(final String value) {

		if (StringUtils.isBlank(value)) {
			return "";
		}

		return value;
	}
}
