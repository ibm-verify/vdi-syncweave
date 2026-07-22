/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.context.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ibm.di.tp.server.context.TPServerContext;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class ConcurrentTPServerContext implements TPServerContext {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	private Map<String, Object> map;

	/**
	 * 
	 */
	public ConcurrentTPServerContext() {
		map = new ConcurrentHashMap<String, Object>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.context.TPServerContext#getAttribute(java.lang.String
	 * )
	 */
	public Object getAttribute(String key) {
		return map.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.context.TPServerContext#removeAttribute(java.lang
	 * .String)
	 */
	public Object removeAttribute(String key) {
		return map.remove(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.context.TPServerContext#setAttribute(java.lang.String
	 * , java.lang.Object)
	 */
	public Object setAttribute(String key, Object value) {
		return map.put(key, value);
	}
}
