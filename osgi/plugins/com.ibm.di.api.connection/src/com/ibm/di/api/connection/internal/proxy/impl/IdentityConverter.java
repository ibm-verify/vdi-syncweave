/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal.proxy.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import com.ibm.di.api.connection.internal.proxy.Converter;
import com.ibm.di.api.connection.internal.proxy.ConverterFactory;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class IdentityConverter implements Converter {
	/**
	 * 
	 */
	private static final Class<?>[] CONSTRUCTOR_PARAM_SIZE = new Class[] { int.class };

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final ConverterFactory cf;

	public IdentityConverter(ConverterFactory cf) {
		this.cf = cf;
	}

	public Object convert(Object fromInstance, Class<?> toType) {
		if (fromInstance == null) {
			return null;
		}

		Object converted = fromInstance;
		if (Map.class.isAssignableFrom(fromInstance.getClass())) {
			converted = convertMap(fromInstance);
		} else if (Collection.class.isAssignableFrom(fromInstance.getClass())) {
			converted = convertCollection(fromInstance);
		}
		return converted;
	}

	@SuppressWarnings("unchecked")
	private Object convertCollection(Object fromInstance) {
		Collection<Object> col = (Collection<Object>) fromInstance;
		Collection<Object> newCol = null;
		try {
			Constructor<? extends Collection> constructor;
			try {
				constructor = col.getClass().getConstructor(CONSTRUCTOR_PARAM_SIZE);
			} catch (NoSuchMethodException e) {
				constructor = null;
			}
			if (constructor != null) {
				newCol = constructor.newInstance(col.size());
			} else {
				newCol = (Collection<Object>) col.getClass().newInstance();
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		Converter converter;
		Class<?> convetedClass;
		for (Object val : col) {
			if (val != null) {
				converter = cf.getInstance(val.getClass());
				convetedClass = converter.convert(val.getClass());
				val = converter.convert(val, convetedClass);
				newCol.add(val);
			} else {
				col.add(null);
			}
		}

		return newCol;
	}

	@SuppressWarnings("unchecked")
	private Object convertMap(Object fromInstance) {
		Map<Object, Object> map = (Map<Object, Object>) fromInstance;
		Map<Object, Object> newMap = null;
		try {
			Constructor<? extends Map> constructor = map.getClass().getConstructor(CONSTRUCTOR_PARAM_SIZE);
			if (constructor != null) {
				newMap = constructor.newInstance(map.size());
			} else {
				newMap = (Map<Object, Object>) map.getClass().newInstance();
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		Object key;
		Object val;
		Converter conveter;
		Class<?> convetedClass;
		for (Map.Entry<Object, Object> e : map.entrySet()) {
			key = e.getKey();
			val = e.getValue();

			if (key != null) {
				conveter = cf.getInstance(key.getClass());
				convetedClass = conveter.convert(key.getClass());
				key = conveter.convert(key, convetedClass);
			}

			if (val != null) {
				conveter = cf.getInstance(val.getClass());
				convetedClass = conveter.convert(val.getClass());
				val = conveter.convert(val, convetedClass);
			}
			newMap.put(key, val);
		}

		return newMap;
	}

	public Class<?> convert(Class<?> clazz) {
		return clazz;
	}
}
