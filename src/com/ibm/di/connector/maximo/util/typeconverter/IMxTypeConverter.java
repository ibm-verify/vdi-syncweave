/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.util.typeconverter;

import com.ibm.di.connector.maximo.exception.MxConnTypeConvertionException;

/**
 * This interface specifies the contract that a Maximo Type Converter should
 * comply, which is basically, the capability for converting a {@link String}
 * into a object of a specific type and <i>vice versa</i>.
 * 
 * @since 7.1
 */
public interface IMxTypeConverter {

	/**
	 * Returns a {@link String} object representing the specified <tt>value</tt>
	 * .
	 * 
	 * @param value
	 *            value to be converted
	 * @return {@link String} object representing the specified <tt>value</tt>,
	 *         or <code>null</code> if <tt>value</tt> is <code>null</code>
	 * @throws MxConnTypeConvertionException
	 *             if the specified <tt>value</tt> can not be converted
	 */
	String toString(Object value) throws MxConnTypeConvertionException;

	/**
	 * Returns an object representing the specified <tt>value</tt>.
	 * 
	 * @param value
	 *            value to be converted
	 * @return object representing the specified <tt>value</tt>, or
	 *         <code>null</code> if <tt>value</tt> is <code>null</code>
	 * @throws MxConnTypeConvertionException
	 *             if the specified <tt>value</tt> can not be converted
	 */
	Object valueOf(String value) throws MxConnTypeConvertionException;
}
