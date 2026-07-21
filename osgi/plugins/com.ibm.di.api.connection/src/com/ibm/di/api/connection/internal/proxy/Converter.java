/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal.proxy;

/**
 * Defines the protocol for converting an instance to the specific {@link Class}
 * . <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public interface Converter {
	
	/**
	 * Converts the instance to the specified type. If the instance is
	 * <code>null</code> no conversation will be performed.
	 * 
	 * @param fromInstance
	 *            the instance to convert or <code>null</code>
	 * @param toType
	 *            the type to which the instance should be converted
	 * @return the converted instance or <code>null</code>.
	 */
	public abstract Object convert(Object fromInstance, Class<?> toType);

	/**
	 * Converts the class. If the clazz is <code>null</code> a
	 * {@link NullPointerException} will be thrown.
	 * 
	 * @param clazz
	 *            the class to convert
	 * @return the converted class;
	 */
	public abstract Class<?> convert(Class<?> clazz);
}
