/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.ci.ps;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.List;

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
import com.ibm.di.api.rest.internal.util.AtomFeedFactory;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.handler.NotFound;
import com.ibm.di.api.rest.internal.util.ConfigConvertor;
import com.ibm.di.config.bind.PropertyStoreBinding;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.TDIPropertyStore;

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
public class PropertyStoreFeed {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private final ConfigInstance ci;

	public PropertyStoreFeed(ConfigInstance ci) {
		this.ci = ci;
	}

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomFeed)
	public Response getFeedAsJson(@Context UriInfo uri) throws Exception {
		return getFeed(uri, false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response getFeedAsXml(@Context UriInfo uri) throws Exception {
		return getFeed(uri, true);
	}

	private Response getFeed(UriInfo uri, boolean isXml) throws Exception, RemoteException, UnsupportedEncodingException {
		List<String> storeNames = ci.getTDIProperties().getPropertyStoreNames();

		URI absUrl = uri.getAbsolutePath();

		AtomFeed feed = AtomFeedFactory.createAtomFeed();
		feed.setId(absUrl.toString());
		feed.setUpdated(System.currentTimeMillis());
		feed.getCategories().add(AppConstants.CAT_RES_PROPERTY_STORE);

		for (String storeName : storeNames) {
			feed.getEntries().add(
					new PropertyStoreEntry(ci, storeName).getSelf(absUrl + "/" + URLEncoder.encode(storeName, "UTF-8"), isXml));
		}

		return Response.ok(feed).build();
	}

	@Path("{psId}")
	public Object getPropertyStoreEntry(@PathParam("psId") String psId) throws Exception {
		TDIPropertyStore ps = ci.getTDIProperties().getPropertyStore(psId);
		return ps == null ? NotFound.getInstance() : new PropertyStoreEntry(ci, ps);
	}

	@POST
	@Consumes( { AppConstants.OBJ_JSON_PropertyStoreBinding, AppConstants.MT_CONFIG_XML })
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public Response createAsJson(@Context UriInfo uri, PropertyStoreBinding psb) throws Exception {
		return create(uri, psb, false);
	}

	@POST
	@Consumes( { AppConstants.OBJ_JSON_PropertyStoreBinding, AppConstants.MT_CONFIG_XML })
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response createAsXml(@Context UriInfo uri, PropertyStoreBinding psb) throws Exception {
		return create(uri, psb, true);
	}

	private Response create(UriInfo uri, PropertyStoreBinding psb, boolean isXml) throws DIException, Exception, RemoteException,
			UnsupportedEncodingException {
		if (psb.getName() == null) {
			throw new DIException(AppConstants.L10N.getString("REST.API.PS.NAME.MISSING"));
		}
		PropertyStoreConfig psConfig = (PropertyStoreConfig) ConfigConvertor
				.toConfig(psb, MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
		ci.getTDIProperties().addPropertyStore(psConfig);

		URI absPath = URI.create(uri.getAbsolutePath() + "/" + URLEncoder.encode(psConfig.getShortName(), "UTF-8"));
		return Response.created(absPath).entity(
				new PropertyStoreEntry(ci, psConfig.getShortName()).getSelf(absPath.toString(), isXml)).build();
	}
}
