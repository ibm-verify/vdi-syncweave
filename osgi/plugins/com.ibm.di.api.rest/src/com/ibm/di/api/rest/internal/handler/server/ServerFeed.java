/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.server;

import java.io.IOException;
import java.net.URI;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.di.web.common.atom.AtomText;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import javax.ws.rs.Path;
import com.ibm.di.web.common.atom.AtomFeed;

import com.ibm.di.api.DIException;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.handler.server.control.ServerControlEntry;
import com.ibm.di.api.rest.internal.handler.server.info.ServerInfoEntry;
import com.ibm.di.api.rest.internal.handler.server.notify.CustomNotifyEntry;
import com.ibm.di.api.rest.internal.util.AtomFeedFactory;

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
@Path(ServerFeed.URL)
public class ServerFeed {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public static final String URL = "server";
	public ServerFeed() {	}

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomFeed)
	public Response getFeedAsJson(@Context UriInfo uri) throws DIException, NotBoundException, IOException {
		return getFeed(uri, false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response getFeedAsXml(@Context UriInfo uri) throws DIException, NotBoundException, IOException {
		return getFeed(uri, true);
	}

	private Response getFeed(UriInfo uri, boolean isXml) throws DIException, NotBoundException, IOException {
		AtomFeed feed = AtomFeedFactory.createAtomFeed();
		URI absPath = uri.getAbsolutePath();
		feed.setId(absPath.toString());
		feed.setUpdated(System.currentTimeMillis());
		feed.getCategories().add(AppConstants.CAT_RES_SERVER);
		feed.getEntries().add(new ServerInfoEntry().getSelf(absPath + "/" + ServerInfoEntry.URL, false, null, isXml));
		feed.getEntries().add(new ServerControlEntry().getSelf(absPath + "/" + ServerControlEntry.URL, isXml));
		feed.getEntries().add(new CustomNotifyEntry().getSelf(absPath + "/" + CustomNotifyEntry.URL, isXml));
		return Response.ok(feed).build();
	}

	@Path(ServerInfoEntry.URL)
	public ServerInfoEntry getInfo() {
		return new ServerInfoEntry();
	}

	@Path(ServerControlEntry.URL)
	public ServerControlEntry getControl() {
		return new ServerControlEntry();
	}

	@Path(CustomNotifyEntry.URL)
	public CustomNotifyEntry getNotification() {
		return new CustomNotifyEntry();
	}
}
