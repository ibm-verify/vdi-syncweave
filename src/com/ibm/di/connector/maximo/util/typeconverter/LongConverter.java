/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.util.typeconverter;

import com.ibm.di.connector.maximo.core.SimpleTpaeIFConnector;
import com.ibm.di.connector.maximo.exception.MxConnTypeConvertionException;
import com.ibm.di.util.StringUtils;

/**
 * Maximo Type Converter for {@link Long} types.
 * 
 * @since 7.1
 * @see Long
 */
public final class LongConverter implements IMxTypeConverter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final LongConverter INSTANCE = new LongConverter();

	/**
	 * Returns an instance of {@link LongConverter}.
	 * 
	 * @return instance of {@link LongConverter}
	 */
	public static LongConverter getInstance() {

		return INSTANCE;
	}

	private LongConverter() {

	}

	/**
	 * {@inheritDoc}
	 */
	public String toString(final Object value) throws MxConnTypeConvertionException {

		if (value == null || StringUtils.isBlank(value.toString())) {
			return "";
		}

		if (value instanceof Long) {
			return value.toString();
		}

		try {
			Long.parseLong(value.toString());
			return value.toString();
		} catch (final NumberFormatException e) {
			throw new MxConnTypeConvertionException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.NOT.VALID.LONG", value), e);
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
			return Long.valueOf(value);
		} catch (final NumberFormatException e) {
			throw new MxConnTypeConvertionException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.CONVERT.TO.LONG", value), e);
		}
	}
}
