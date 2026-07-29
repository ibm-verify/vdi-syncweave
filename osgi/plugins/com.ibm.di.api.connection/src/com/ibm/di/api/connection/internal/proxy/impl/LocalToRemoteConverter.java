/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal.proxy.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.ibm.di.api.connection.internal.proxy.ApiAdapter;
import com.ibm.di.api.connection.internal.proxy.ConverterFactory;
import com.ibm.di.api.connection.internal.proxy.ApiAdapter.InstanceType;

/**
 * Converts a "Local" instance to the specified Remote Type. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class LocalToRemoteConverter extends BaseApiConverter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private final ConverterFactory cf;

	public LocalToRemoteConverter(ConverterFactory cf) {
		super(new LocalClassSpace());
		this.cf = cf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.api.internal.proxy.Converter#convert(java.lang.Object,
	 * java.lang.Class)
	 */
	public Object convert(Object localInstance, Class<?> remoteType) {
		if (localInstance == null) {
			return null;
		}
		if (localInstance.getClass().isArray()) {
			Class<?> localType = getArrayComponentType(localInstance.getClass());
			if (localType != null) {
				return convertArray(localInstance, remoteType);
			}
		} else if (Proxy.isProxyClass(localInstance.getClass())) {
			InvocationHandler h = Proxy.getInvocationHandler(localInstance);
			if (h instanceof ApiAdapter && ((ApiAdapter) h).getInstanceType() == InstanceType.REMOTE) {
				return ((ApiAdapter) h).getAdaptedInstance();
			}
		} else if (findFirstOwnedType(localInstance.getClass()) != null) {
			// a Local instance which is not a proxy... currently undefined
			// case, but theoretically should be possible. Just stay symmetric
			// to RemoteToLocal converter and create a proxy.
			return Proxy.newProxyInstance(remoteType.getClassLoader(), new Class<?>[] { remoteType }, new ApiAdapter(localInstance,
					InstanceType.LOCAL, cf));
		}

		// we shouldn't be here!
		throw new IllegalArgumentException(localInstance.getClass().getName());
	}

	static class LocalClassSpace implements BaseApiConverter.ClassSpace {
		public boolean owns(Class<?> clazz) {
			return clazz.getPackage().getName().equals("com.ibm.di.api.local");
		}
	}
}
