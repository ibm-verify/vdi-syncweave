/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.easyetl.internal;

import org.glassfish.jersey.server.ResourceConfig;

import com.ibm.di.ui.easyetl.internal.handler.ds.DeltaStoreHandler;
import com.ibm.di.ui.easyetl.internal.handler.etl.ConfigHandler;
import com.ibm.di.ui.easyetl.internal.handler.log.LogfilesHandler;
import com.ibm.di.ui.easyetl.internal.handler.nls.NLSHandler;
import com.ibm.di.ui.easyetl.internal.handler.server.ServerHandler;
import com.ibm.di.ui.easyetl.internal.handler.ts.TombstoneHandler;
import com.ibm.di.ui.easyetl.internal.templates.TemplatesHandler;

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
public class EasyEtlApplication extends ResourceConfig {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public EasyEtlApplication() {
		// Lifecycle / bootstrap
		register(AppInitializer.class);

		// Providers
		register(CustomMedia2JaxbJSONProvider.class);

		// Resources (singletons)
		register(new ConfigHandler());
		register(new TemplatesHandler());
		register(new DeltaStoreHandler());
		register(new TombstoneHandler());
		register(new LogfilesHandler());
		register(new ServerHandler());
		register(new NLSHandler());
	}
}
