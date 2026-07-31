/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.tombstone;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.ibm.di.web.common.atom.AtomFeed;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.util.AtomFeedFactory;
import com.ibm.di.api.rest.internal.handler.NotFound;
import com.ibm.di.api.rest.internal.util.EnvUtils;

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
public class TsAlFeed {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private final String ciId;

	/**
	 * @param ciId
	 */
	public TsAlFeed(String ciId) {
		this.ciId = ciId;
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
		Session s = EnvUtils.getServerApiSession(req);
		List<String> alNames = s.getTombstoneManager().getAssemblyLineNames(ciId);

		AtomFeed feed = AtomFeedFactory.createAtomFeed();
		feed.setId(uri.getAbsolutePath().toString());
		feed.setUpdated(System.currentTimeMillis());
		feed.getCategories().add(AppConstants.CAT_RES_ASSEMBLY_LINE);

		for (String alName : alNames) {
			feed.getEntries().add(
					new TsAlEntry(ciId, alName).getSelf(uri.getAbsolutePath() + "/" + URLEncoder.encode(alName, "UTF-8"), isXml));
		}

		return Response.ok(feed).build();
	}

	@Path("{alName}")
	public Object getALEntry(@PathParam("alName") String alName, @Context HttpServletRequest req) throws RemoteException,
			DIException, NotBoundException {
		Session s = EnvUtils.getServerApiSession(req);
		return s.getTombstoneManager().hasTombstones(ciId, alName) ? new TsAlEntry(ciId, alName) : NotFound.getInstance();
	}
}
