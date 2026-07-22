/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.nls;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.ibm.di.nls.impl.ResourceBundleL10N;

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
public final class L10NFactory {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private L10NFactory() {
	}

	public static L10N getInstance(Class<?> cls) {
		return getInstance(cls, "OSGI-INF/l10n/bundle");
	}

	public static L10N getInstance(Class<?> cls, String fallbackResHash) {
		try {
			Class<?> eclipse = Class.forName("com.ibm.di.nls.impl.EclipseL10N");
			Constructor<?> con = eclipse.getConstructor(Class.class);
			return (L10N) con.newInstance(cls);
		} catch (ClassNotFoundException e) {
			;
		} catch (SecurityException e) {
			;
		} catch (NoSuchMethodException e) {
			;
		} catch (IllegalArgumentException e) {
			;
		} catch (InstantiationException e) {
			;
		} catch (IllegalAccessException e) {
			;
		} catch (InvocationTargetException e) {
			;
		} catch (NoClassDefFoundError e) {
			;
		}

		return new ResourceBundleL10N(fallbackResHash, cls.getClassLoader());
	}
}
