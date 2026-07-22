/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.nls.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Collections;

import com.ibm.di.nls.L10N;

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
public class ResourceBundleL10N extends L10N {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private final String resource;

	private final ClassLoader cl;

	public ResourceBundleL10N() {
		this(null, ResourceBundleL10N.class.getClassLoader());
	}

	public ResourceBundleL10N(String resource, ClassLoader cl) {
		this.resource = resource;
		this.cl = cl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.nls.L10N#getResourceBundle(java.util.Locale)
	 */
	@Override
	protected ResourceBundle getResourceBundle(Locale locale) {
		try {
			if (resource != null) {
				ResourceCombiningClassLoader resourceCombiningClassLoader = AccessController
						.doPrivileged(new PrivilegedAction<ResourceCombiningClassLoader>() {
							public ResourceCombiningClassLoader run() {
								return new ResourceCombiningClassLoader(cl);
							}
						});
				return ResourceBundle.getBundle(resource, locale, resourceCombiningClassLoader);
			}
		} catch (MissingResourceException ex) {
			;
		}
		return new ResourceBundle() {

			@Override
			protected Object handleGetObject(String key) {
				return key;
			}

			@Override
			public Enumeration<String> getKeys() {
				return Collections.emptyEnumeration();
			}
		};
	}

	/**
	 * When running outside the OSGi environment we may have multiple
	 * OSGI-INF/l10n/bundle (default name) so we need a {@link ResourceBundle}
	 * that can read them all at once. Use a {@link ClassLoader} to help it find
	 * all of the resources.
	 * 
	 * <br>
	 * <br>
	 * <b>Note:</b> This class is for internal usage only. Any dependency from
	 * the end-user will not be supported. Changes to this class will happen
	 * without a warning.
	 * 
	 * @since 7.2
	 */
	private static class ResourceCombiningClassLoader extends ClassLoader {

		public ResourceCombiningClassLoader(ClassLoader parent) {
			super(parent);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
		 */
		@Override
		public InputStream getResourceAsStream(String resName) {
			InputStream result = null;
			try {
				Enumeration<URL> resEnum = getParent().getResources(resName);
				if (resEnum != null) {
					List<InputStream> iss = new LinkedList<InputStream>();
					while (resEnum.hasMoreElements()) {
						iss.add(resEnum.nextElement().openStream());
					}

					if (iss.size() > 1) {
						result = new SequenceInputStream(Collections.enumeration(iss));
					} else if (iss.size() == 1) {
						result = iss.get(0);
					}
				}
			} catch (IOException e) {
				return null;
			}

			return result;
		}
	}
}
