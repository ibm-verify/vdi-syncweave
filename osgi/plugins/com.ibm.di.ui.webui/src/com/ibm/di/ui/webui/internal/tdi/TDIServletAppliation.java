/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal.tdi;

import org.glassfish.jersey.server.ResourceConfig;

import com.ibm.di.ui.webui.internal.AppInitializer;
import com.ibm.di.ui.webui.internal.CustomMedia2JaxbJSONProvider;

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
public class TDIServletAppliation extends ResourceConfig {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public TDIServletAppliation() {
		// Lifecycle / bootstrap
		register(AppInitializer.class);

		// Providers
		register(CustomMedia2JaxbJSONProvider.class);

		// Resources (singletons)
		register(new TDIAlHandler());
	}
}
