/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.ibm.di.web.common.atom.app.AppCollection;
import com.ibm.di.web.common.atom.app.AppService;
import com.ibm.di.web.common.atom.app.AppWorkspace;
import com.ibm.di.web.common.atom.AtomText;

import com.ibm.di.api.rest.internal.AppConstants;

/**
 * Reuse Collections gathering mechanism to <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
@Path("/")
public class ServiceDocument {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@GET
	@Produces( { AppConstants.OBJ_JSON_AppService, AppConstants.MT_ATOM_APP_SRVC_XML })
	public AppService getServiceDocument(@Context UriInfo uriInfo) {
		// Create service document manually
		AppService srvc = new AppService();
		AppWorkspace ws = new AppWorkspace();
		ws.setTitle(createAtomText("Security Verify Directory Integrator Workspace"));
		
		// Add collections - these correspond to the @Workspace annotated resources
		// Note: In Jersey, these resources are registered via ResourceConfig, not auto-discovered
		addCollection(ws, "Server", uriInfo.getBaseUri() + "server");
		addCollection(ws, "Tombstone", uriInfo.getBaseUri() + "tombstone");
		addCollection(ws, "Configurations", uriInfo.getBaseUri() + "config");
		addCollection(ws, "ConfigInstances", uriInfo.getBaseUri() + "ci");
		addCollection(ws, "Listeners", uriInfo.getBaseUri() + "listener");
		
		srvc.getWorkspace().add(ws);
		return srvc;
	}
	
	private void addCollection(AppWorkspace ws, String title, String href) {
		AppCollection col = new AppCollection();
		col.setTitle(createAtomText(title));
		col.setHref(href);
		ws.getCollection().add(col);
	}

	/**
	 * Helper method to create AtomText with TEXT type.
	 */
	private AtomText createAtomText(String value) {
		AtomText text = new AtomText();
		text.setType("text");
		text.setValue(value);
		return text;
	}

}
