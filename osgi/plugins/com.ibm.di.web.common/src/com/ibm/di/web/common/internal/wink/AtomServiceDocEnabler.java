/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.internal.wink;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Responsible for enabling Atom Service Document auto-generation as the root
 * resource. For this purpose we need to configure Jersey's
 * technology appropriately.<br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 *
 * @since 7.2
 */
public class AtomServiceDocEnabler extends ResourceConfig {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * Constructor that configures Jersey for Atom Service Document support.
	 * In Jersey, we configure this through ResourceConfig properties.
	 */
	public AtomServiceDocEnabler() {
		super();
		
		// Jersey configuration for Atom Service Document
		// Note: Jersey doesn't have a direct equivalent to Wink's "wink.rootResource"
		// The Atom service document functionality will need to be implemented
		// through JAX-RS resources and providers registered in the Application class
		property("jersey.config.server.wadl.disableWadl", "false");
	}
}
