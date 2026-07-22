/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.nls.impl;

import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

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
public class EclipseL10N extends L10N {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * IMPORTANT: In order for this class to work, make sure the Manifest.mf
	 * specifies: "Bundle-ActivationPolicy: lazy", which creates a BundleContext
	 * needed for accessing the Service Registry.
	 */
	private static final Bundle thisBundle = FrameworkUtil.getBundle(L10N.class);

	private ServiceTracker locTracker;
	private Bundle bundle;

	private BundleListener bListener;

	/**
	 * Creates an empty instance that can later be populated using the
	 * corresponding setters.
	 */
	public EclipseL10N() {
		this((Bundle) null);
	}

	public EclipseL10N(Class<?> bundledClass) {
		this(FrameworkUtil.getBundle(bundledClass));
	}

	/**
	 * Creates a function-ready instance that needs no further tinkering.
	 * 
	 * @param bundle
	 *            the bundle whose localization to manage.
	 */
	public EclipseL10N(Bundle bundle) {
		setBundle(bundle);
	}

	/**
	 * Sets a bundle which l10n to use. This method supports switching/disabling
	 * of bundles, which allows for reuse of the instance.
	 * 
	 * @param bundle
	 *            the bundle to set
	 */
	public synchronized void setBundle(final Bundle bundle) {
		if (locTracker != null) {
			locTracker.close();
			locTracker = null;

			if (bListener != null) {
				thisBundle.getBundleContext().removeBundleListener(bListener);
				bListener = null;
			}
		}

		if (bundle != null) {
			locTracker = new ServiceTracker(thisBundle.getBundleContext(), BundleLocalization.class.getCanonicalName(), null);
			locTracker.open();

			thisBundle.getBundleContext().addBundleListener((bListener = new BundleListener() {

				public void bundleChanged(BundleEvent event) {
					switch (event.getType()) {
					case BundleEvent.STARTING:
						synchronized (EclipseL10N.this) {
							if (locTracker == null) {
								// reactivate the tracker as this bundle has
								// already been started once and then stopped
								// and now starting again.
								locTracker = new ServiceTracker(thisBundle.getBundleContext(), BundleLocalization.class
										.getCanonicalName(), null);
								locTracker.open();
							}
						}
						break;
					case BundleEvent.STOPPED:
						synchronized (EclipseL10N.this) {
							if (locTracker != null) {
								// stop the tracker to make sure we free up the
								// bundle localization reference(s).
								locTracker.close();
								locTracker = null;
							}
						}
					}

				}
			}));
		}
		this.bundle = bundle;
	}

	protected synchronized ResourceBundle getResourceBundle(Locale locale) {
		BundleLocalization loc = (BundleLocalization) (locTracker == null ? null : locTracker.getService());
		return loc == null ? null : loc.getLocalization(bundle, locale != null ? locale.toString() : null);
	}
}
