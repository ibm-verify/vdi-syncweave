/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.util.typeconverter;

import com.ibm.di.connector.maximo.core.SimpleTpaeIFConnector;
import com.ibm.di.connector.maximo.exception.MxConnTypeConvertionException;
import com.ibm.di.util.StringUtils;

/**
 * Maximo Type Converter for {@link Integer} types.
 * 
 * @since 7.1
 * @see Integer
 */
public final class IntegerConverter implements IMxTypeConverter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final IntegerConverter INSTANCE = new IntegerConverter();

	/**
	 * Returns an instance of {@link IntegerConverter}.
	 * 
	 * @return instance of {@link IntegerConverter}
	 */
	public static IntegerConverter getInstance() {

		return INSTANCE;
	}

	private IntegerConverter() {

	}

	/**
	 * {@inheritDoc}
	 */
	public String toString(final Object value) throws MxConnTypeConvertionException {

		if (value == null || StringUtils.isBlank(value.toString())) {
			return "";
		}

		if (value instanceof Integer) {
			return value.toString();
		}

		try {
			Integer.parseInt(value.toString());
			return value.toString();
		} catch (final NumberFormatException e) {
			throw new MxConnTypeConvertionException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.NOT.VALID.INTEGER", value), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object valueOf(final String value) throws MxConnTypeConvertionException {

		if (StringUtils.isBlank(value)) {
			return null;
		}

		try {
			return Integer.valueOf(value);
		} catch (final NumberFormatException e) {
			throw new MxConnTypeConvertionException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.CONVERT.TO.INTEGER", value), e);
		}
	}
}
