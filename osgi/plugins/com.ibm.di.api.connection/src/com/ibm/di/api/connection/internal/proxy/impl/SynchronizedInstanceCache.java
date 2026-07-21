/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal.proxy.impl;

import java.util.HashMap;
import java.util.Map;

import com.ibm.di.api.connection.internal.proxy.InstanceCache;

/**
 * Cache using plain old Java synchronization mechanism. <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class SynchronizedInstanceCache<T> implements InstanceCache<T> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Map<String, T> cache = new HashMap<String, T>();

	private final InstanceFactory<T> fact;

	public SynchronizedInstanceCache(InstanceFactory<T> fact) {
		this.fact = fact;
	}

	public T getInstance(String key) {
		return getInstance(key, fact);
	}

	public T getInstance(String key, InstanceFactory<T> fact) {
		synchronized (cache) {
			T inst = cache.get(key);
			if (inst == null) {
				inst = fact.newInstance(key);
				cache.put(key, inst);
			}
			return inst;
		}
	}
}
