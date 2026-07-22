/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal.proxy.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ibm.di.api.connection.internal.proxy.InstanceCache;

/**
 * A synchronized cache using unfair {@link ReadWriteLock} for optimized
 * reading. <br>
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class ReadWriteLockInstanceCache<T> implements InstanceCache<T> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	// HashMap default init size
	private static final int DEFAULT_SIZE = 16;

	private static final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
	private Map<String, T> cache;;

	private final InstanceFactory<T> fact;

	public ReadWriteLockInstanceCache(InstanceFactory<T> fact) {
		this(fact, DEFAULT_SIZE);
	}

	public ReadWriteLockInstanceCache(InstanceFactory<T> fact, int initSize) {
		this.fact = fact;
		cache = new HashMap<String, T>(initSize);
	}

	public T getInstance(String key) {
		return getInstance(key, fact);
	}

	public T getInstance(String key, InstanceFactory<T> fact) {
		cacheLock.readLock().lock();
		T inst = cache.get(key);
		if (inst == null) {
			cacheLock.readLock().unlock();
			cacheLock.writeLock().lock();
			if ((inst = cache.get(key)) == null) {
				inst = fact.newInstance(key);
				cache.put(key, inst);
			}
			cacheLock.readLock().lock();
			cacheLock.writeLock().unlock();
		}
		cacheLock.readLock().unlock();
		return inst;
	}
}
