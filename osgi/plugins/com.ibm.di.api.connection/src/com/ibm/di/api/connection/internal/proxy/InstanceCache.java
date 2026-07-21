/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal.proxy;

/**
 * A cache of instances that a referenced by a string key. Use an
 * {@link InstanceFactory} to create new instances in case one does not exist.<br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public interface InstanceCache<T> {

	/**
	 * Gets an instance mapped under the specified key. Creates a new one if the
	 * instance does not exist using a statically linked {@link InstanceFactory}
	 * 
	 * @param key
	 *            the key of the instance in the cache.
	 * @return the new instance.
	 */
	public T getInstance(String key);

	/**
	 * Gets an instance mapped under the specified key. Creates a new one if the
	 * instance does not exist using the dynamically provided
	 * {@link InstanceFactory}
	 * 
	 * @param key
	 *            the key of the instance in the cache.
	 * @param fact
	 *            the factory to use for creating new instance
	 * @return the new instance.
	 */
	public T getInstance(String key, InstanceFactory<T> fact);

	public static interface InstanceFactory<I> {
		public I newInstance(String key);
	}
}
