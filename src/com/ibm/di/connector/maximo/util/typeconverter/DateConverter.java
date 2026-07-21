/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.util.typeconverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.ibm.di.connector.maximo.core.SimpleTpaeIFConnector;
import com.ibm.di.connector.maximo.exception.MxConnTypeConvertionException;
import com.ibm.di.util.StringUtils;

/**
 * Maximo Type Converter for {@link Date} types.
 * 
 * @since 7.1
 * @see Date
 * @see SimpleDateFormat
 */
public final class DateConverter implements IMxTypeConverter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final DateConverter INSTANCE = new DateConverter();

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);

	private static final int SEPARATOR_INDEX = 22;

	/**
	 * Returns an instance of {@link DateConverter}.
	 * 
	 * @return instance of {@link DateConverter}
	 */
	public static DateConverter getInstance() {
		return INSTANCE;
	}

	private DateConverter() {

	}

	/**
	 * Returns a {@link String} object representing the specified <tt>value</tt>
	 * .
	 * 
	 * @param value
	 *            value to be converted, must be a {@link Date} object or a
	 *            {@link String} object with the format
	 *            <code>"2007-08-10'T'23:45:15-03:00"</code>
	 * @return {@link String} object representing the specified <tt>value</tt>
	 * @throws MxConnTypeConvertionException
	 *             if the specified value is not a instance of {@link Date} nor
	 *             {@link String}
	 * @throws MxConnTypeConvertionException
	 *             if the specified value is a {@link String} object that does
	 *             not comply with the format
	 *             <code>"2007-08-10'T'23:45:15-03:00"</code>
	 */
	public String toString(final Object value) throws MxConnTypeConvertionException {

		if (value == null || StringUtils.isBlank(value.toString())) {
			return "";
		}

		if (value instanceof Date) {
			final String s;

			synchronized (SDF) {
				s = SDF.format((Date) value);
			}

			// we need to add extra ':' in the '+0300' part of
			// '2010-04-28T11:33:24+0300' in order to get
			// the correct '2010-04-28T11:33:24+03:00'
			return s.substring(0, SEPARATOR_INDEX) + ":" + s.substring(SEPARATOR_INDEX);
		}

		if (value instanceof String) {
			valueOf((String) value);
			return value.toString();
		}

		throw new MxConnTypeConvertionException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.NOT.VALID.DATE", value));
	}

	/**
	 * Returns a {@link Date} object representing the specified <tt>value</tt>.
	 * 
	 * @param value
	 *            value to be converted, must comply with the format:
	 *            <code>"2007-08-10'T'23:45:15-03:00"</code>
	 * @return {@link Date} object representing the specified <tt>value</tt>
	 * @throws MxConnTypeConvertionException
	 *             if the specified value does not comply with the expected
	 *             format
	 */
	public Object valueOf(final String value) throws MxConnTypeConvertionException {

		if (StringUtils.isBlank(value)) {
			return null;
		}

		if (value.length() <= SEPARATOR_INDEX || value.charAt(SEPARATOR_INDEX) != ':') {
			throw new MxConnTypeConvertionException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.CONVERT.TO.DATE", value));
		}

		try {
			final Date d;

			synchronized (SDF) {
				// remove the extra ':' so we can parse it
				d = SDF.parse(value.substring(0, SEPARATOR_INDEX) + value.substring(SEPARATOR_INDEX + 1));
			}

			return d;
		} catch (final ParseException e) {
			throw new MxConnTypeConvertionException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.CONVERT.TO.DATE", value), e);
		}
	}
}
