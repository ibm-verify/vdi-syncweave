/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.ci;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.di.web.common.atom.AtomText;
import javax.naming.InvalidNameException;
import javax.servlet.http.HttpServletRequest;
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

import javax.ws.rs.Path;
import com.ibm.di.web.common.atom.AtomFeed;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.Listener;
import com.ibm.di.api.bind.StartCI;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.LogListener;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.rest.internal.util.AtomFeedFactory;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.handler.NotFound;
import com.ibm.di.api.rest.internal.handler.ci.CiListenerContext.CIListenerAttacher;
import com.ibm.di.api.rest.internal.handler.config.ConfigurationFile;
import com.ibm.di.api.rest.internal.registry.ListenerRegistry.ListenerRegistration;
import com.ibm.di.api.rest.internal.util.ConfigConvertor;
import com.ibm.di.api.rest.internal.util.EnvUtils;
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
@Path(ConfigInstanceFeed.URL)
public class ConfigInstanceFeed {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	public static final String URL = "ci";

	public ConfigInstanceFeed() {	}
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
		List<String> ciIds = s.getConfigInstancesIDs();

		AtomFeed feed = AtomFeedFactory.createAtomFeed();
		feed.setId(uri.getAbsolutePath().toString());
		feed.setUpdated(System.currentTimeMillis());
		feed.getCategories().add(AppConstants.CAT_RES_CI);

		String baseUri = uri.getBaseUri().toString();

		for (String ciId : ciIds) {
			feed.getEntries().add(
					new ConfigInstanceEntry(ciId).getSelf(baseUri, uri.getAbsolutePath() + "/" + URLEncoder.encode(ciId, "UTF-8"),
							isXml, req));
		}

		return Response.ok(feed).build();
	}

	@Path("{ciId}")
	public Object getCIEntry(@PathParam("ciId") String ciId, @Context HttpServletRequest req) throws RemoteException, DIException,
			NotBoundException {
		Session s = EnvUtils.getServerApiSession(req);
		ConfigInstance ci = s.getConfigInstance(URLDecoder.decode(ciId));

		return ci == null ? NotFound.getInstance() : new ConfigInstanceEntry(ci);
	}

	@POST
	@Consumes( { AppConstants.OBJ_JSON_StartCI, AppConstants.MT_API_CONFIG_XML })
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public Response createConfigInstanceAsJson(@Context HttpServletRequest req, @Context UriInfo uri, StartCI start)
			throws Exception {
		return createConfigInstance(req, uri, start, false);
	}

	@POST
	@Consumes( { AppConstants.OBJ_JSON_StartCI, AppConstants.MT_API_CONFIG_XML })
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response createConfigInstanceAsXml(@Context HttpServletRequest req, @Context UriInfo uri, StartCI start)
			throws Exception {
		return createConfigInstance(req, uri, start, true);
	}

	private Response createConfigInstance(HttpServletRequest req, UriInfo uri, final StartCI start, boolean isXml)
			throws DIException, RemoteException, NotBoundException, InvalidNameException, Exception, UnsupportedEncodingException {
		final Session s = EnvUtils.getServerApiSession(req);
		ConfigInstance ci = null;

		String tempCfgRef = start.getConfigRef();
		if (tempCfgRef != null && (tempCfgRef = tempCfgRef.trim()).length() > 0) {
			final String cfgRef = ConfigurationFile.toApiConfigId(tempCfgRef);

			if (start.getLogListener() != null) {
				CIStarter starter = new CIStarter(cfgRef, start, s);
				CiListenerContext ctx = new CiListenerContext(starter, EnvUtils.getListenerFactory(req.getSession()
						.getServletContext()), EnvUtils.getListenerRegistry(req.getSession().getServletContext()));
				ctx.create(start.getLogListener());
				ci = starter.ci;
			} else if (start.getRunName() != null) {
				ci = s.startConfigInstance(cfgRef, start.isKeepAlive(), start.getPassword(), start.getRunName(), null);
			} else if (start.getPassword() != null) {
				ci = s.startConfigInstance(cfgRef, start.isKeepAlive(), start.getPassword());
			} else if (start.isKeepAlive()) {
				ci = s.startConfigInstance(cfgRef, start.isKeepAlive(), null);
			} else {
				ci = s.startConfigInstance(cfgRef);
			}
		} else if (start.getSolution() != null) {
			MetamergeConfig mc = ConfigConvertor.toConfig(start.getSolution());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			mc.commitChanges(bos);
			String cfg = bos.toString("UTF-8");
			if (start.getLogListener() != null) {
				TempCIStarter starter = new TempCIStarter(cfg, start, s);
				CiListenerContext ctx = new CiListenerContext(starter, EnvUtils.getListenerFactory(req.getSession()
						.getServletContext()), EnvUtils.getListenerRegistry(req.getSession().getServletContext()));
				ctx.create(start.getLogListener());
				ci = starter.ci;
			} else {
				ci = s.startTempConfigInstance(cfg, start.isKeepAlive(), start.getRunName(), start.getPassword());
			}
		} else {
			throw new DIException(AppConstants.L10N.getString("REST.API.CI.CONFIG.MISSING"));
		}

		URI absPath = URI.create(uri.getAbsolutePath() + "/" + URLEncoder.encode(ci.getConfigId(), "UTF-8"));
		return Response.created(absPath).entity(
				new ConfigInstanceEntry(ci).getSelf(uri.getBaseUri().toString(), absPath.toString(), isXml, req)).build();
	}

	private static class TempCIStarter extends DelayedCICreator {

		private final StartCI start;
		private final Session s;
		private final String cfg;

		public TempCIStarter(String cfg, StartCI start, Session s) {
			this.cfg = cfg;
			this.start = start;
			this.s = s;
		}

		public void attachListener(LogListener l) throws RemoteException, DIException {
			ci = s.startTempConfigInstance(cfg, start.isKeepAlive(), start.getRunName(), start.getPassword(), l);
		}
	}

	private static class CIStarter extends DelayedCICreator {

		private final String cfgRef;
		private final StartCI start;
		private final Session s;

		public CIStarter(String cfgRef, StartCI start, Session s) {
			this.cfgRef = cfgRef;
			this.start = start;
			this.s = s;
		}

		public void attachListener(LogListener l) throws RemoteException, DIException {
			ci = s.startConfigInstance(cfgRef, start.isKeepAlive(), start.getPassword(), start.getRunName(), null, l);
		}
	}

	private static abstract class DelayedCICreator implements CIListenerAttacher {
		protected ConfigInstance ci;

		public String getConfigId() throws RemoteException, DIException {
			if (ci == null) {
				// exception visible for devs only!
				throw new IllegalStateException("attachListener must be called to provide CI ref.");
			}
			return ci.getConfigId();
		}

		public void detachListener(ListenerRegistration<LogListener, ? extends Listener> r) throws RemoteException, DIException {
		}
	}
}
