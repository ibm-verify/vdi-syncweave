/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal.proxy.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.server.Unreferenced;

import com.ibm.di.api.connection.internal.proxy.ApiAdapter;
import com.ibm.di.api.connection.internal.proxy.ConverterFactory;
import com.ibm.di.api.connection.internal.proxy.ApiAdapter.InstanceType;
import com.ibm.di.api.connection.internal.track.RemoteReferenceTracker;

/**
 * Converts a Remote instance to the specified "Local" Type. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class RemoteToLocalConverter extends BaseApiConverter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private final ConverterFactory cf;

	private final RemoteReferenceTracker rtracker;

	public RemoteToLocalConverter(ConverterFactory cf, RemoteReferenceTracker rtracker) {
		super(new RemoteClassSpace());
		this.cf = cf;
		this.rtracker = rtracker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.api.internal.proxy.Converter#convert(java.lang.Object,
	 * java.lang.Class)
	 */
	public Object convert(Object remoteInstance, Class<?> localType) {
		if (remoteInstance == null) {
			return null;
		}
		if (remoteInstance.getClass().isArray()) {
			Class<?> remoteType = getArrayComponentType(remoteInstance.getClass());
			if (localType != null) {
				return convertArray(remoteInstance, remoteType);
			}
		} else if (Proxy.isProxyClass(remoteInstance.getClass())) {
			InvocationHandler h = Proxy.getInvocationHandler(remoteInstance);
			if (h instanceof ApiAdapter && ((ApiAdapter) h).getInstanceType() == InstanceType.LOCAL) {
				return ((ApiAdapter) h).getAdaptedInstance();
			}
		} else if (findFirstOwnedType(remoteInstance.getClass()) != null) {
			// a Remote instance which is not a proxy... this happens when the
			// client side is exporting Remote instances, for example a
			// RemoteListener. We need to adapt that one to the Local API.
			Object proxy = Proxy.newProxyInstance(localType.getClassLoader(), new Class<?>[] { localType }, new ApiAdapter(
					remoteInstance, InstanceType.REMOTE, cf));
			if (remoteInstance instanceof Unreferenced) {
				// track the proxy instance and when GC'ed notify the remote
				// instance
				rtracker.track(proxy, (Unreferenced) remoteInstance);
			}
			return proxy;
		}

		// we shouldn't be here!
		throw new IllegalArgumentException(remoteInstance.getClass().getName());
	}

	static class RemoteClassSpace implements BaseApiConverter.ClassSpace {
		public boolean owns(Class<?> clazz) {
			return clazz.getPackage().getName().equals("com.ibm.di.api.remote");
		}
	}
}
