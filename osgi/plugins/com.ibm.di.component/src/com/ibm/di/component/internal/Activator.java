/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.component.internal;

import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

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
public class Activator implements BundleActivator {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(final BundleContext context) throws Exception {
		Bundle[] bundles = context.getBundles();
		for (Bundle b : bundles) {
			Dictionary<?, ?> dict = b.getHeaders();
			String name = (String) dict.get("Bundle-SymbolicName");
			int endIdx = name.indexOf(';');
			if (endIdx > -1) {
				name = name.substring(0, endIdx);
			}
			if (((name.startsWith("com.ibm.di.connector") || name.startsWith("com.ibm.di.function") || name
					.startsWith("com.ibm.di.parser")) && ((b.getState() & (Bundle.ACTIVE | Bundle.STARTING)) == 0 ||
			/*
			 * check lazy activation policy as if set the state will be STARTING
			 * but not active
			 */
			"lazy".equals(dict.get("Bundle-ActivationPolicy"))))) {
				try {
					b.start();
				} catch (BundleException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}
}
