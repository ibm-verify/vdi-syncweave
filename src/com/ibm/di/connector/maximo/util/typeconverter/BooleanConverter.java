/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.util.typeconverter;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.ibm.di.connector.maximo.core.SimpleTpaeIFConnector;
import com.ibm.di.connector.maximo.exception.MxConnTypeConvertionException;
import com.ibm.di.util.StringUtils;

/**
 * Maximo Type Converter for {@link Boolean} types.
 * 
 * @since 7.1
 * @see Boolean
 */
public final class BooleanConverter implements IMxTypeConverter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final Set<String> FALSES = new HashSet<String>();

	private static final BooleanConverter INSTANCE = new BooleanConverter();

	private static final Set<String> TRUES = new HashSet<String>();

	static {
		TRUES.add("true");
		TRUES.add("1");

		FALSES.add("false");
		FALSES.add("0");
	}

	/**
	 * Returns an instance of {@link BooleanConverter}.
	 * 
	 * @return instance of {@link BooleanConverter}
	 */
	public static BooleanConverter getInstance() {

		return INSTANCE;
	}

	private BooleanConverter() {

	}

	/**
	 * Returns a {@link String} object representing the specified <tt>value</tt>
	 * .
	 * <table border="1">
	 * <tr>
	 * <th>Value</th>
	 * <th>Returns</th>
	 * </tr>
	 * <tr>
	 * <td>{@link Boolean#TRUE}, {@link String "1"}, {@link String "true"}
	 * {@link String "TRUE"}</td>
	 * <td>{@link String "1"}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link Boolean#FALSE}, {@link String "0"}, {@link String "false"}
	 * {@link String "FALSE"}</td>
	 * <td>{@link String "0"}</td>
	 * </tr>
	 * <tr>
	 * <td><tt>null</tt>, {@link String empty string}, {@link String white
	 * spaces}</td>
	 * <td>{@link String empty string}</td>
	 * </tr>
	 * </table>
	 * 
	 * @param value
	 *            value to be converted
	 * @return {@link String} object representing the specified <tt>value</tt>
	 * @throws MxConnTypeConvertionException
	 *             if the specified value can not be converted
	 */
	public String toString(final Object value) throws MxConnTypeConvertionException {

		if (value == null || StringUtils.isBlank(value.toString())) {
			return "";
		}
		if (TRUES.contains(value.toString().toLowerCase())) {
			return "1";
		}
		if (FALSES.contains(value.toString().toLowerCase())) {
			return "0";
		}
		throw new MxConnTypeConvertionException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.NOT.VALID.BOOLEAN", value));
	}

	/**
	 * Returns a {@link Boolean} object representing the specified
	 * <tt>value</tt>.
	 * <table border="1">
	 * <tr>
	 * <th>Value</th>
	 * <th>Returns</th>
	 * </tr>
	 * <tr>
	 * <td>{@link String "1"}, {@link String "true"} {@link String "TRUE"}</td>
	 * <td>{@link Boolean#TRUE}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link String "0"}, {@link String "false"} {@link String "FALSE"}</td>
	 * <td>{@link Boolean#FALSE}</td>
	 * </tr>
	 * <tr>
	 * <td><tt>null</tt>, {@link String empty string}, {@link String white
	 * spaces}</td>
	 * <td><tt>null</tt></td>
	 * </tr>
	 * </table>
	 * 
	 * @param value
	 *            value to be converted
	 * @return {@link Boolean} object representing the specified <tt>value</tt>
	 * @throws MxConnTypeConvertionException
	 *             if the specified value can not be converted
	 */
	public Object valueOf(final String value) throws MxConnTypeConvertionException {

		if (StringUtils.isBlank(value)) {
			return null;
		}
		if (TRUES.contains(value.toLowerCase(Locale.ENGLISH))) {
			return Boolean.TRUE;
		}
		if (FALSES.contains(value.toLowerCase(Locale.ENGLISH))) {
			return Boolean.FALSE;
		}
		throw new MxConnTypeConvertionException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.CONVERT.TO.BOOLEAN", value));
	}
}
