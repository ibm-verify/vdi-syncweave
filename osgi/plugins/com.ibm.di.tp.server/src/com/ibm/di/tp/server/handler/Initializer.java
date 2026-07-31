/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler;

import java.io.File;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.jaxrs.storage.atom.AtomStorage;
import com.ibm.di.jaxrs.storage.atom.AtomStorageFactory;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.TPServerApplication;
import com.ibm.di.tp.server.config.TPServerConfig;
import com.ibm.di.tp.server.config.TPServerConfigFile;
import com.ibm.di.tp.server.context.TPServerContext;
import com.ibm.di.tp.server.context.impl.ConcurrentTPServerContext;

/**
 * Used to initialize the TP Server Application's state. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
@Path("init")
public class Initializer {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@Context
	private ServletContext sCtx;

	@Context
	private Providers providers;

	private volatile boolean initialized;

	@GET
	// custom content type
	@Consumes("tdi/init")
	public Object initApplication(@Context UriInfo u) throws Exception {
		try {
			initialize();
			return Response.ok().build();
		} catch (Exception e) {
			// make sure the initialization error is logged.
			TPServerApplication.getLog().error(e.getMessage(), e);

			// re-throw the exception so a proper stack trace is put in the
			// servlet response in case the initialization is done by a real
			// client and not by a mock.
			throw e;
		}
	}

	private void initialize() throws Exception {
		if (!initialized) {
			// we are initializing on the first request as the ctx field is not
			// available during construction.
			initialized = true;

			// respect overrides because unit_tests might inject its own
			// context/config

			// initialize TPServerContext
			TPServerContext tpCtx = (TPServerContext) sCtx.getAttribute(TPServerContext.class.getCanonicalName());
			if (tpCtx == null) {
				tpCtx = new ConcurrentTPServerContext();
				sCtx.setAttribute(TPServerContext.class.getCanonicalName(), tpCtx);
			}

			// initialize TPServerConfig
			TPServerConfig cfg = (TPServerConfig) tpCtx.getAttribute(TPServerConfig.class.getCanonicalName());
			if (cfg == null) {
				String configFilePath = System.getProperty("tp.server.config");
				if (configFilePath != null) {
					try {
						TPServerConfigFile file = new TPServerConfigFile(new File(configFilePath));
						cfg = file.getTPServerConfig();
					} catch (Exception e) {
						TPServerApplication.getLog().warn(
								ServerActivator.L10N.getString("TP.CONFIG.COULD.NOT.READ.FILE", configFilePath), e);
						cfg = new TPServerConfig();
					}
				} else {
					TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.CONFIG.NOT.SPECIFIED"));
					cfg = new TPServerConfig();
				}

				tpCtx.setAttribute(TPServerConfig.class.getCanonicalName(), cfg);
			}

			// initialize the AtomStorage
			AtomStorage as = (AtomStorage) tpCtx.getAttribute(AtomStorage.class.getCanonicalName());
			if (as == null) {
				if (cfg.getPersistenceConfig().isEnabled() && cfg.getPersistenceConfig().getLocation() != null) {
					// we are doing this now because the Providers instance
					// just became available on the first request.
					tpCtx.setAttribute(AtomStorage.class.getCanonicalName(), AtomStorageFactory.createAtomStorage(providers, cfg
							.getPersistenceConfig().getLocation()));
				} else {
					if (!cfg.getPersistenceConfig().isEnabled()) {
						TPServerApplication.getLog().debug(ServerActivator.L10N.getString("TP.PERSISTENCE.DISABLED"));
					} else if (cfg.getPersistenceConfig().getLocation() == null) {
						TPServerApplication.getLog().warn(
								ServerActivator.L10N.getString("TP.SERVER.CONFIG.MISSING.PARAMETER",
										"persistenceConfig/location"));
					}
					tpCtx.setAttribute(AtomStorage.class.getCanonicalName(), AtomStorageFactory.createAtomStorage());
				}
			}

			// we need an IServerApiConnectionService in the TPServerContext so
			// get the one that was binded for at the ServletContext
			tpCtx.setAttribute(IServerAPIConnectionService.class.getCanonicalName(), sCtx
					.getAttribute(IServerAPIConnectionService.class.getCanonicalName()));
		}
	}
}
