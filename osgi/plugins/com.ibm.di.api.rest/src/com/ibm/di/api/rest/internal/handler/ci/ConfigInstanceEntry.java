/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.ci;

import java.io.UnsupportedEncodingException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.LogListener;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.handler.ci.al.AssemblyLineFeed;
import com.ibm.di.api.rest.internal.handler.ci.ps.PropertyStoreFeed;
import com.ibm.di.api.rest.internal.handler.config.ConfigurationFile;
import com.ibm.di.api.rest.internal.handler.listener.ListenerFeed;
import com.ibm.di.api.rest.internal.util.ConfigConvertor;
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
public class ConfigInstanceEntry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private ConfigInstance ci;
	private String ciId;

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public AtomEntry getSelf(@Context UriInfo uri, @Context HttpServletRequest req) throws UnsupportedEncodingException,
			RemoteException, DIException, NotBoundException {
		return getSelf(uri.getBaseUri().toString(), uri.getAbsolutePath().toString(), false, req);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public AtomEntry getSelfAsXml(@Context UriInfo uri, @Context HttpServletRequest req) throws UnsupportedEncodingException,
			RemoteException, DIException, NotBoundException {
		return getSelf(uri.getBaseUri().toString(), uri.getAbsolutePath().toString(), true, req);
	}

	public ConfigInstanceEntry(ConfigInstance ci) {
		this.ci = ci;
	}

	public ConfigInstanceEntry(String ciId) {
		this.ciId = ciId;
	}

	public AtomEntry getSelf(String baseUri, String absUrl, boolean isXml, HttpServletRequest req)
			throws UnsupportedEncodingException, RemoteException, DIException, NotBoundException {
		AtomEntry e = new AtomEntry();
		e.setId(absUrl);
		e.setUpdated(System.currentTimeMillis());
		e.setTitle(createAtomText(getCiId()));

		// Updated reflects when the config instance was started
		try {
			Session s = EnvUtils.getServerApiSession(req);
			e.setUpdated(s.getConfigInstance(this.ciId).getInstanceBootTime().getTime());
		} catch(Exception errIgnore) {
			errIgnore.printStackTrace();
		}
		
		AtomLink l = new AtomLink();
		l.setRel(AppConstants.REL_SELF);
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomEntry);
		l.setHref(e.getId());
		e.getLinks().add(l);

		e.getCategories().add(AppConstants.CAT_RES_CI);

		l = new AtomLink();
		l.setRel(AppConstants.REL_PROPERTY_STORE);
		l.setHref(e.getId() + "/ps");
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomFeed);
		e.getLinks().add(l);

		l = new AtomLink();
		l.setRel(AppConstants.REL_ASSEMBLY_LINE);
		l.setHref(e.getId() + "/al");
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomFeed);
		e.getLinks().add(l);

		l = new AtomLink();
		l.setRel(AppConstants.REL_LISTENER);
		l.setHref(e.getId() + "/listener");
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomFeed);
		e.getLinks().add(l);

		l = new AtomLink();
		l.setRel(AppConstants.REL_CONFIGURATION);
		l.setHref(e.getId() + "/config");
		l.setType(isXml ? AppConstants.MT_CONFIG_XML : AppConstants.OBJ_JSON_SolutionBinding);
		e.getLinks().add(l);

		if (req != null) {
			String configFileToken = getCI(req).getConfigurationFile();
			if (configFileToken != null) {
				l = new AtomLink();
				l.setRel("file");
				l.setHref(ConfigurationFile.fromApiConfigId(baseUri, configFileToken));
				l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : MediaType.APPLICATION_JSON);
				e.getLinks().add(l);
			}
		}

		return e;
	}

	@DELETE
	public Response stop(@Context HttpServletRequest req) throws RemoteException, DIException, NotBoundException {
		getCI(req).stop(true);
		return Response.ok().build();
	}

	@GET
	@Path("config")
	@Produces( { AppConstants.OBJ_JSON_SolutionBinding, AppConstants.MT_CONFIG_XML })
	public Response getConfiguration(@Context HttpServletRequest req, @Context UriInfo uri) throws Exception {
		return Response.ok(ConfigConvertor.fromConfig(getCI(req).getConfiguration(), uri.getBaseUri().toString())).build();
	}

	@Path("ps")
	public PropertyStoreFeed getPropertyStoreFeed(@Context HttpServletRequest req) throws RemoteException, DIException,
			NotBoundException {
		return new PropertyStoreFeed(getCI(req));
	}

	@Path("al")
	public AssemblyLineFeed getAssemblyLineFeed(@Context HttpServletRequest req) throws RemoteException, DIException,
			NotBoundException {
		return new AssemblyLineFeed(getCI(req));
	}

	@Path("listener")
	public ListenerFeed<LogListener> getListenerFeed(@Context HttpServletRequest req) throws RemoteException, DIException,
			NotBoundException {
		return new ListenerFeed<LogListener>(new CiListenerContext(getCI(req), EnvUtils.getListenerFactory(req.getSession()
				.getServletContext()), EnvUtils.getListenerRegistry(req.getSession().getServletContext())));
	}

	private ConfigInstance getCI(HttpServletRequest req) throws RemoteException, DIException, NotBoundException {
		if (ci == null) {
			Session s = EnvUtils.getServerApiSession(req);
			ci = s.getConfigInstance(ciId);
			if (ci == null) {
				throw new DIException(AppConstants.L10N.getString("REST.API.OBJECT.UNAVAILABLE"));
			}
		}
		return ci;
	}

	private String getCiId() throws RemoteException, DIException {
		if (ciId == null) {
			ciId = ci.getConfigId();
		}
		return ciId;
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
