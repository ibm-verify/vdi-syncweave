/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal.proxy.impl;

import com.ibm.di.api.connection.internal.proxy.Converter;
import com.ibm.di.api.connection.internal.proxy.ConverterFactory;
import com.ibm.di.api.connection.internal.track.RemoteReferenceTracker;

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
public class ConverterFactoryImpl implements ConverterFactory {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	protected static final String PACKAGE_PREF_REMOTE = "com.ibm.di.api.remote.";
	protected static final String PACKAGE_PREF_LOCAL = "com.ibm.di.api.local.";

	private final RemoteToLocalConverter remoteConverter;
	private final LocalToRemoteConverter localConverter = new LocalToRemoteConverter(this);
	private final IdentityConverter identityConverter = new IdentityConverter(this);

	public ConverterFactoryImpl(RemoteReferenceTracker rtracker) {
		this.remoteConverter = new RemoteToLocalConverter(this, rtracker);
	}

	public Converter getInstance(Class<?> fromClass) {
		if (fromClass.isArray()) {
			fromClass = fromClass.getComponentType();
		}

		if (!fromClass.isPrimitive()) {
			if ("com.ibm.di.api.remote".equals(fromClass.getPackage().getName())) {
				return remoteConverter;
			} else if ("com.ibm.di.api.local".equals(fromClass.getPackage().getName())) {
				return localConverter;
			}
		}
		return identityConverter;
	}
}
