/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.context;

/**
 * This is the context object of the TP Server. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public interface TPServerContext {

	/**
	 * Retrieves the specific attribute for the provided key.
	 * 
	 * @param key
	 *            the key for which an attribute is mapped.
	 * @return the mapped attribute of the current context.
	 */
	public abstract Object getAttribute(String key);

	/**
	 * Sets a value to the corresponding key
	 * 
	 * @param key
	 *            the key to map the value under
	 * @param value
	 *            the value of the attribute
	 * @return the old value if one exists, null otherwise.
	 */
	public abstract Object setAttribute(String key, Object value);

	/**
	 * Removes the mapped value under the provided key.
	 * 
	 * @param key
	 *            the key of the attribute to remove.
	 * @return the old value if one exists, null otherwise.
	 */
	public abstract Object removeAttribute(String key);
}
