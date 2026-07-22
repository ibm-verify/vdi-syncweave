/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal;

import org.glassfish.jersey.server.ResourceConfig;

import com.ibm.di.ui.webui.internal.handler.ds.DeltaStoreHandler;
import com.ibm.di.ui.webui.internal.handler.etl.ConfigHandler;
import com.ibm.di.ui.webui.internal.handler.files.FileHandler;
import com.ibm.di.ui.webui.internal.handler.ldapsync.LDAPSync;
import com.ibm.di.ui.webui.internal.handler.log.LogfilesHandler;
import com.ibm.di.ui.webui.internal.handler.nls.NLSHandler;
import com.ibm.di.ui.webui.internal.handler.server.ScriptHandler;
import com.ibm.di.ui.webui.internal.handler.server.ServerHandler;
import com.ibm.di.ui.webui.internal.handler.ts.TombstoneHandler;
import com.ibm.di.ui.webui.internal.templates.TemplatesHandler;

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
public class WebUiAppliation extends ResourceConfig {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public WebUiAppliation() {
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
		register(new FileHandler());
		register(new LDAPSync());
		register(new ScriptHandler());
	}
}
