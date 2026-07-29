/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.config;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.ConfigLock;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.registry.UserDataRegistry;
import com.ibm.di.api.rest.internal.util.ConfigConvertor;
import com.ibm.di.api.rest.internal.util.EnvUtils;
import com.ibm.di.config.bind.SolutionBinding;

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
public class ConfigurationFile {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private final String configFile;

	public ConfigurationFile(String configFile) {
		this.configFile = configFile;
	}

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public AtomEntry getSelfAsJson(@Context UriInfo uri, @Context HttpServletRequest req) throws UnsupportedEncodingException,
			RemoteException, DIException, NotBoundException {
		return getSelf(uri.getAbsolutePath().toString(), EnvUtils.getServerApiSession(req), req.getSession(), false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public AtomEntry getSelf(@Context UriInfo uri, @Context HttpServletRequest req) throws UnsupportedEncodingException,
			RemoteException, DIException, NotBoundException {
		return getSelf(uri.getAbsolutePath().toString(), EnvUtils.getServerApiSession(req), req.getSession(), true);
	}

	public AtomEntry getSelf(String absUri, Session s, HttpSession hs, boolean isXml) throws UnsupportedEncodingException,
			RemoteException, DIException {
		AtomEntry e = new AtomEntry();
		e.setId(absUri);
		e.setUpdated(System.currentTimeMillis());
		e.setTitle(createAtomText(configFile));

		AtomLink l = new AtomLink();
		l.setRel(AppConstants.REL_SELF);
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomEntry);
		l.setHref(e.getId());
		e.getLinks().add(l);

		e.getCategories().add(AppConstants.CAT_RES_CONFIG);
		e.getCategories().add(AppConstants.CAT_CONFIG_FILE);

		l = new AtomLink();
		l.setRel(AppConstants.REL_LOCK);
		l.setHref(e.getId() + "/lock");
		l.setType(isXml ? AppConstants.MT_API_CONFIG_XML : AppConstants.OBJ_JSON_ConfigLock);
		e.getLinks().add(l);

		if (s.isConfigurationCheckedOut(configFile)) {
			e.getCategories().add(AppConstants.CAT_CONFIG_LOCKED);
		}

		return e;
	}

	@POST
	@Path("lock")
	@Consumes( { AppConstants.OBJ_JSON_ConfigLock, AppConstants.MT_API_CONFIG_XML })
	@Produces( { AppConstants.OBJ_JSON_ConfigLock, AppConstants.MT_API_CONFIG_XML })
	public Response createLock(@Context HttpServletRequest req, ConfigLock lock, @Context UriInfo uri) throws Exception {
		Session s = EnvUtils.getServerApiSession(req);

		if (s.isConfigurationCheckedOut(configFile)) {
			Response.status(Status.CONFLICT).build();
		}

		SolutionBinding sb = null;
		if (lock.getConfigPassword() != null) {
			sb = ConfigConvertor.fromConfig(s.checkOutConfiguration(configFile, lock.getConfigPassword()), uri.getBaseUri()
					.toString());
		} else {
			sb = ConfigConvertor.fromConfig(s.checkOutConfiguration(configFile), uri.getBaseUri().toString());
		}
		EnvUtils.getUserDataRegistry(req.getSession().getServletContext()).setData(req, configFile, sb);
		return Response.created(uri.getAbsolutePath()).entity(getLock(sb)).build();
	}

	@GET
	@Path("lock")
	@Produces( { AppConstants.OBJ_JSON_ConfigLock, AppConstants.MT_API_CONFIG_XML })
	public Response getLock(@Context HttpServletRequest req) throws Exception {

		Session s = EnvUtils.getServerApiSession(req);
		if (!s.isConfigurationCheckedOut(configFile)) {
			return Response.status(Status.NOT_FOUND).build();
		}

		UserDataRegistry dataReg = EnvUtils.getUserDataRegistry(req.getSession().getServletContext());
		Object sb = dataReg.getData(req, configFile);
		return (sb instanceof SolutionBinding ? Response.ok(getLock((SolutionBinding) sb)) : Response.status(Status.FORBIDDEN))
				.build();
	}

	private ConfigLock getLock(SolutionBinding sb) throws Exception {
		ConfigLock cl = new ConfigLock();
		cl.setSolution(sb);
		return cl;
	}

	@PUT
	@Path("lock")
	@Consumes( { AppConstants.OBJ_JSON_ConfigLock, AppConstants.MT_API_CONFIG_XML })
	@Produces( { AppConstants.OBJ_JSON_ConfigLock, AppConstants.MT_API_CONFIG_XML })
	public Response updateLock(@Context HttpServletRequest req, ConfigLock lock, @Context UriInfo uri) throws Exception {
		Session s = EnvUtils.getServerApiSession(req);

		if (!s.isConfigurationCheckedOut(configFile)) {
			return Response.status(Status.NOT_FOUND).build();
		}

		UserDataRegistry dataReg = EnvUtils.getUserDataRegistry(req.getSession().getServletContext());
		Object sb = dataReg.getData(req, configFile);

		if (!(sb instanceof SolutionBinding)) {
			return Response.status(Status.FORBIDDEN).build();
		}

		if (lock.getSolution() == null) {
			throw new DIException(AppConstants.L10N.getString("REST.API.CONFIG.MISSING"));
		}

		s.checkInAndLeaveCheckedOut(ConfigConvertor.toConfig(lock.getSolution()), configFile, lock.isEncrypt());
		dataReg.setData(req, configFile, lock.getSolution());

		return Response.ok(getLock(lock.getSolution())).build();
	}

	@DELETE
	@Path("lock")
	public Response unlock(@Context HttpServletRequest req) throws Exception {
		Session s = EnvUtils.getServerApiSession(req);

		if (!s.releaseConfigurationLock(configFile)) {
			return Response.status(Status.NOT_FOUND).build();
		}

		return Response.noContent().build();
	}

	@DELETE
	public Response deleteConfig(@Context HttpServletRequest req) throws RemoteException, DIException, NotBoundException {
		Session s = EnvUtils.getServerApiSession(req);
		s.deleteConfiguration(configFile);
		return Response.noContent().build();
	}

	public static String toApiConfigId(String entryId) {
		int startIdx = entryId.indexOf(ConfigurationDir.URL) + ConfigurationDir.URL.length();

		String cfgId = entryId;
		while (cfgId.charAt(++startIdx) == '/')
			;
		cfgId = cfgId.substring(startIdx);
		cfgId = URI.create(cfgId).normalize().toString();
		try {
			cfgId = URLDecoder.decode(cfgId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		int endIdx = cfgId.length();
		while (cfgId.charAt(--endIdx) == '/')
			;

		startIdx = cfgId.lastIndexOf('/', endIdx);

		if (startIdx == -1) {
			startIdx = 0;
		} else {
			startIdx++;
		}

		if (cfgId.startsWith("e:", startIdx)) {
			StringBuilder sb = new StringBuilder(cfgId.length());
			if (startIdx > 0) {
				sb.append(cfgId, 0, startIdx);
			}
			sb.append(cfgId, startIdx + 2, endIdx + 1);
			cfgId = sb.toString();
		}

		return cfgId;
	}

	public static String fromApiConfigId(String baseUri, String configId) {
		String parent = ConfigurationDir.getParentPath(configId);
		String filePath = ConfigurationDir.getLastPath(configId);

		try {
			// baseUri ends with "/"
			return baseUri + ConfigurationDir.URL + (parent != null ? "/" + parent : "") + "/"
					+ URLEncoder.encode("e:" + filePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new InternalError(e.getMessage());
		}
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
