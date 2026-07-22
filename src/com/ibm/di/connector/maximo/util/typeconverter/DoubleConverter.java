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
 * Maximo Type Converter for {@link Double} types.
 * 
 * @since 7.1
 * @see Double
 */
public final class DoubleConverter implements IMxTypeConverter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final DoubleConverter INSTANCE = new DoubleConverter();

	/**
	 * Returns an instance of {@link DoubleConverter}.
	 * 
	 * @return instance of {@link DoubleConverter}
	 */
	public static DoubleConverter getInstance() {

		return INSTANCE;
	}

	private DoubleConverter() {

	}

	/**
	 * {@inheritDoc}
	 */
	public String toString(final Object value) throws MxConnTypeConvertionException {

		if (value == null || StringUtils.isBlank(value.toString())) {
			return "";
		}

		if (value instanceof Double) {
			return value.toString();
		}

		try {
			Double.parseDouble(value.toString());
			return value.toString();
		} catch (final NumberFormatException e) {
			throw new MxConnTypeConvertionException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.NOT.VALID.DOUBLE", value), e);
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
			return Double.valueOf(value);
		} catch (final NumberFormatException e) {
			throw new MxConnTypeConvertionException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.CONVERT.TO.DOUBLE", value), e);
		}
	}
}
