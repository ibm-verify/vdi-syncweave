/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal.proxy.impl;

import static com.ibm.di.api.connection.internal.proxy.impl.ConverterFactoryImpl.PACKAGE_PREF_LOCAL;
import static com.ibm.di.api.connection.internal.proxy.impl.ConverterFactoryImpl.PACKAGE_PREF_REMOTE;

import java.lang.reflect.Array;
import java.rmi.Remote;

import com.ibm.di.api.connection.internal.proxy.Converter;
import com.ibm.di.api.connection.internal.proxy.InstanceCache;
import com.ibm.di.api.connection.internal.proxy.InstanceCache.InstanceFactory;

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
public abstract class BaseApiConverter implements Converter {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Static cache for holding the remote-to-local and local-to-remote
	 * mappings. Note: Make sure you pass in only interface names to avoid
	 * getting ClasNotFoundException.
	 */
	protected static final InstanceCache<Class<?>> cache = new ReadWriteLockInstanceCache<Class<?>>(new InstanceFactory<Class<?>>() {
		public Class<?> newInstance(String type) {
			try {
				if (type.startsWith(PACKAGE_PREF_REMOTE)) {
					return Class.forName(PACKAGE_PREF_LOCAL.concat(type.substring(PACKAGE_PREF_REMOTE.length())));
				} else {
					return Class.forName(PACKAGE_PREF_REMOTE.concat(type.substring(PACKAGE_PREF_LOCAL.length())));
				}
			} catch (ClassNotFoundException e) {
				// we couldn't find the corresponding type in the
				// local package.
				throw new RuntimeException(e);
			}
		}
	}, 40 /*
		 * Number of Local and Remote interfaces we have currently in the Server
		 * API
		 */);

	protected final ClassSpace classSpace;

	public BaseApiConverter(ClassSpace classSpace) {
		this.classSpace = classSpace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.api.internal.proxy.Converter#convert(java.lang.Class)
	 */
	public Class<?> convert(Class<?> localType) {
		if (localType.isArray()) {
			Class<?> ownedType = getArrayComponentType(localType);
			if (ownedType != null) {
				Object arrayInstance = Array.newInstance(convert(ownedType), 0);
				return arrayInstance.getClass();
			}
		} else {
			Class<?> ownedType = findFirstOwnedType(localType);
			if (ownedType != null) {
				return cache.getInstance(ownedType.getName());
			}
		}
		return localType;
	}

	protected Class<?> getArrayComponentType(Class<?> arrayClass) {
		Class<?> clazz = arrayClass;
		while (clazz.isArray()) {
			clazz = clazz.getComponentType();
		}
		return findFirstOwnedType(clazz);
	}

	/**
	 * Finds the first interface the provided class implements that is within
	 * the class space this instance represents.
	 * 
	 * @param clazz
	 *            the class and its supertypes to check.
	 * @return the first interface from the class space found in the class
	 *         hierarchy or null.
	 */
	protected Class<?> findFirstOwnedType(Class<?> clazz) {
		if (clazz == null || clazz == Object.class) {
			return null;
		}

		if (classSpace.owns(clazz)) {
			return clazz;
		}

		Class<?>[] interfaces = clazz.getInterfaces();
		if (interfaces != null) {
			for (Class<?> iface : interfaces) {
				if (classSpace.owns(iface)) {
					return iface;
				}
			}
		}
		return findFirstOwnedType(clazz.getSuperclass());
	}

	/**
	 * Converts the provided array of {@link Remote} instances to the
	 * corresponding array of local instances.
	 * 
	 * @param arrayInst
	 *            the array to convert
	 * @param arrayType
	 *            this is a class representing an array of the remote types.
	 * @return the converted array
	 */
	protected Object convertArray(Object arrayInst, Class<?> arrayType) {
		Object array = arrayInst;
		if (arrayType.isArray()) {
			// create a new Array where each element if of the arrayType and
			// cycle through the actual instances and convert them
			int size = Array.getLength(arrayInst);
			array = Array.newInstance(arrayType.getComponentType(), size);

			for (int i = 0; i < size; i++) {
				Array.set(array, i, convertArray(Array.get(arrayInst, i), arrayType.getComponentType()));
			}
		} else {
			// array is now a single element!
			// each element of the array is a remote instance which needs to be
			// converted.
			array = convert(array, arrayType);
		}

		return array;
	}

	/**
	 * Instances of this interface represent a set of classes grouped by common
	 * criteria. <br>
	 * 
	 * <br>
	 * <br>
	 * <b>Note:</b> This class is for internal usage only. Any dependency from
	 * the end-user will not be supported. Changes to this class will happen
	 * without a warning.
	 * 
	 * @since 7.2
	 */
	static interface ClassSpace {
		/**
		 * @param clazz
		 *            the type to check
		 * @return true if the passed in type is one of the owned Classes or is
		 *         an array of owned Class.
		 */
		public boolean owns(Class<?> clazz);
	}
}
