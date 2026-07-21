/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.listener;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Map.Entry;

import com.ibm.di.web.common.atom.AtomText;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.ibm.di.web.common.atom.AtomFeed;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.Listener;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.handler.NotFound;
import com.ibm.di.api.rest.internal.listener.ListenerContext;
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
public class ListenerFeed<L extends Listener> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final ListenerContext<L> ctx;

	public ListenerFeed(ListenerContext<L> ctx) {
		this.ctx = ctx;
	}

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomFeed)
	public AtomFeed getFeedAsJson(@Context UriInfo uri) throws DIException, IOException {
		return getFeed(uri, false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public AtomFeed getFeedAsXml(@Context UriInfo uri) throws DIException, IOException {
		return getFeed(uri, true);
	}

	public AtomFeed getFeed(UriInfo uri, boolean isXml) throws DIException, IOException {
		URI absUri = uri.getAbsolutePath();

		AtomFeed feed = AtomFeedFactory.createAtomFeed();
		feed.setId(absUri.toString());
		feed.setUpdated(System.currentTimeMillis());
		feed.getCategories().add(AppConstants.CAT_RES_LISTENER);

		for (Entry<String, L> l : ctx.list().entrySet()) {
			feed.getEntries().add(
					new ListenerEntry<L>(l.getKey(), l.getValue(), ctx).getSelf(uri.getBaseUri(), uri.getPath(false) + "/"
							+ l.getKey(), isXml));
		}

		return feed;
	}

	@Path("{lId}")
	public Object getListener(@PathParam("lId") String lId) throws RemoteException, DIException {
		L l = ctx.get(lId);
		return l != null ? new ListenerEntry<L>(lId, l, ctx) : NotFound.getInstance();
	}

	@POST
	@Consumes( { AppConstants.MT_LISTENER_JSON, AppConstants.MT_LISTENER_XML })
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public Response createListenerAsJson(@Context UriInfo uri, L listener) throws DIException, IOException {
		return createListener(uri, listener, false);
	}

	@POST
	@Consumes( { AppConstants.MT_LISTENER_JSON, AppConstants.MT_LISTENER_XML })
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response createListenerAsXml(@Context UriInfo uri, L listener) throws DIException, IOException {
		return createListener(uri, listener, true);
	}

	public Response createListener(@Context UriInfo uri, L listener, boolean isXml) throws DIException, IOException {
		String lId = ctx.create(listener);
		String absUri = uri.getAbsolutePath().toString() + "/" + lId;
		return Response.created(URI.create(absUri)).entity(
				new ListenerEntry<L>(lId, listener, ctx).getSelf(uri.getBaseUri(), uri.getPath(false) + "/" + lId, isXml)).build();
	}
}
