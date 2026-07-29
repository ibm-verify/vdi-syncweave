/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.server.info.comp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.rmi.NotBoundException;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.ibm.di.web.common.atom.AtomCategory;
import com.ibm.di.web.common.atom.AtomFeed;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.ServerInfo;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.util.AtomFeedFactory;
import com.ibm.di.api.rest.internal.util.EnvUtils;
import com.ibm.di.api.rest.internal.util.TDIUtils;

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
public class ComponentFeed {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public static final String URL = "comp";

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomFeed)
	public Response getFeedAsJson(@Context UriInfo uri, @Context HttpServletRequest req) throws DIException, NotBoundException,
			IOException {
		return getFeed(uri, req, false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response getFeedAsXml(@Context UriInfo uri, @Context HttpServletRequest req) throws DIException, NotBoundException,
			IOException {
		return getFeed(uri, req, true);
	}

	private Response getFeed(UriInfo uri, HttpServletRequest req, boolean isXml) throws DIException, NotBoundException, IOException {
		AtomFeed feed = AtomFeedFactory.createAtomFeed();
		feed.setId(uri.getAbsolutePath().toString());
		feed.setUpdated(System.currentTimeMillis());
		feed.getCategories().add(AppConstants.CAT_SRV_COMPONENT);

		Session s = EnvUtils.getServerApiSession(req);
		ServerInfo si = s.getServerInfo();

		String entrySelfPref = uri.getAbsolutePath() + "/";

		String compSelfPref = entrySelfPref + "co/";
		for (String connName : si.getInstalledConnectorsNames()) {
			feed.getEntries().add(
					new ComponentEntry(connName, AppConstants.CAT_COMP_CONN).getSelf(URI.create(compSelfPref
							+ URLEncoder.encode(TDIUtils.getConnectorName(connName), "UTF-8")), false, req, isXml));
		}

		compSelfPref = entrySelfPref + "fc/";
		for (String fcName : si.getInstalledFunctionComponentsNames()) {
			feed.getEntries().add(
					new ComponentEntry(fcName, AppConstants.CAT_COMP_FC).getSelf(URI.create(compSelfPref
							+ URLEncoder.encode(TDIUtils.getFunctionName(fcName), "UTF-8")), false, req, isXml));
		}

		compSelfPref = entrySelfPref + "ps/";
		for (String parserName : si.getInstalledParsersNames()) {
			feed.getEntries().add(
					new ComponentEntry(parserName, AppConstants.CAT_COMP_PARSER).getSelf(URI.create(compSelfPref
							+ URLEncoder.encode(TDIUtils.getParsernName(parserName), "UTF-8")), false, req, isXml));
		}
		return Response.ok(feed).build();
	}

	@Encoded
	@Path("{catId}/{compId}")
	public ComponentEntry getComponent(@PathParam("compId") String compId, @PathParam("catId") String catId)
			throws UnsupportedEncodingException {
		// manually decode as wink don't decode the params either way.
		compId = URLDecoder.decode(compId, "UTF-8");
		catId = URLDecoder.decode(catId, "UTF-8");

		AtomCategory compCat = null;
		if ("co".equalsIgnoreCase(catId)) {
			compCat = AppConstants.CAT_COMP_CONN;
		} else if ("fc".equalsIgnoreCase(catId)) {
			compCat = AppConstants.CAT_COMP_FC;
		} else if ("ps".equalsIgnoreCase(catId)) {
			compCat = AppConstants.CAT_COMP_PARSER;
		} else {
			throw new IllegalArgumentException(catId);
		}

		return new ComponentEntry(compId, compCat);
	}
}
