/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.tombstone;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.ibm.di.web.common.atom.AtomFeed;

import com.ibm.di.api.DIException;
import com.ibm.di.api.Tombstone;
import com.ibm.di.api.rest.internal.AppConstants;
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
public class TombstoneFeed {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private final TombstoneContext ctx;

	public TombstoneFeed(TombstoneContext ctx) {
		this.ctx = ctx;
	}

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomFeed)
	public Response getFeedAsJson(@Context UriInfo uri, @Context HttpServletRequest req) throws RemoteException, DIException,
			NotBoundException, UnsupportedEncodingException {
		return getFeed(uri, req, false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response getFeedAsXml(@Context UriInfo uri, @Context HttpServletRequest req) throws RemoteException, DIException,
			NotBoundException, UnsupportedEncodingException {
		return getFeed(uri, req, true);
	}

	private Response getFeed(UriInfo uri, HttpServletRequest req, boolean isXml) throws DIException, RemoteException,
			NotBoundException, UnsupportedEncodingException {
		Tombstone[] ts = ctx.getTombstones();

		AtomFeed feed = AtomFeedFactory.createAtomFeed();
		feed.setId(uri.getAbsolutePath().toString());
		feed.setUpdated(System.currentTimeMillis());
		feed.getCategories().add(AppConstants.CAT_RES_TOMBSTONE);

		String baseUri = uri.getBaseUri().resolve(TsCiFeed.URL + "/inst").normalize().toString();

		for (Tombstone t : ts) {
			feed.getEntries().add(
					new TombstoneEntry(t).getSelf(baseUri + "/" + URLEncoder.encode(t.getGUID(), "UTF-8"), isXml, false));
		}

		return Response.ok(feed).build();
	}

	static interface TombstoneContext {
		public Tombstone[] getTombstones() throws RemoteException, DIException;
	}
}
