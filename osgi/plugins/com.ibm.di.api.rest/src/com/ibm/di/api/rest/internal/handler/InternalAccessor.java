/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler;

import javax.naming.NameNotFoundException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import com.ibm.di.web.common.atom.AtomText;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.util.ConfigConvertor;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;

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
@Path("internal")
public class InternalAccessor {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@GET
	@Path("{ns}")
	@Produces(AppConstants.MT_CONFIG_JSON)
	public Response getConfigAsJson(@Context UriInfo uri, @PathParam("ns") String ns) throws Exception {
		MetamergeConfig resp = com.ibm.di.config.interfaces.MetamergeConfigFactory.getNamespace(ns);
		if (resp == null) {
			if(com.ibm.di.config.interfaces.MetamergeConfigFactory.loadRegisteredSolution(ns))
				resp = com.ibm.di.config.interfaces.MetamergeConfigFactory.getNamespace(ns);
			else
				return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(ConfigConvertor.fromConfig(resp, uri.getBaseUri().toString())).build();
	}
	
	@GET
	@Path("{ns}/{folder}/{name}")
	@Produces(AppConstants.MT_CONFIG_JSON)
	public Response getConfigAsJson(@Context UriInfo uri, @PathParam("ns") String ns, @PathParam("folder") String folder,
			@PathParam("name") String name) throws Exception {
		return getConfig(ns, folder, name, uri);
	}

	@GET
	@Path("{ns}/{folder}/{name}")
	@Produces(AppConstants.MT_CONFIG_XML)
	public Response getConfigAsXml(@Context UriInfo uri, @PathParam("ns") String ns, @PathParam("folder") String folder,
			@PathParam("name") String name) throws Exception {
		return getConfig(ns, folder, name, uri);
	}

	private Response getConfig(String ns, String folder, String name, UriInfo uri) throws Exception {
		// NOT A REMOTE SERVER API CALL!
		// This will render the handler not portable because it does not depend
		// on the Remote Server API but on the local TDI context directly.
		// Must be reworked when we are ready to expose this through the Server
		// API.
		MetamergeConfig resp = com.ibm.di.config.interfaces.MetamergeConfigFactory.getNamespace(ns);
		if (resp == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		Object cfg;
		try {
			cfg = resp.lookup(folder + "/" + name);
		} catch (NameNotFoundException e) {
			cfg = null;
		}

		if (cfg == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		return Response.ok(ConfigConvertor.fromConfig((BaseConfiguration) cfg, folder, name, uri.getBaseUri().toString())).build();
	}
}
