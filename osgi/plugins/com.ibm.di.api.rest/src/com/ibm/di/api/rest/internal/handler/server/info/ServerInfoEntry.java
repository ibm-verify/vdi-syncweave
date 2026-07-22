/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.server.info;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.annotation.XmlValue;

import com.ibm.di.web.common.atom.AtomContent;
import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.ServerInfo;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.handler.server.info.comp.ComponentFeed;
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
public class ServerInfoEntry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public static final String URL = "info";

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public AtomEntry getSelfAsJson(@Context UriInfo uri, @Context HttpServletRequest req, @Context Providers provs)
			throws DIException, NotBoundException, IOException {
		return getSelf(uri.getAbsolutePath().toString(), true, req, false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public AtomEntry getSelfAsXml(@Context UriInfo uri, @Context HttpServletRequest req) throws DIException, NotBoundException,
			IOException {
		return getSelf(uri.getAbsolutePath().toString(), true, req, true);
	}

	public AtomEntry getSelf(String absUrl, boolean expandContent, final HttpServletRequest req, boolean isXml) throws DIException,
			NotBoundException, IOException {
		AtomEntry e = new AtomEntry();
		e.setId(absUrl);
		e.setUpdated(System.currentTimeMillis());

		AtomLink l = new AtomLink();
		l.setRel(AppConstants.REL_SELF);
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomEntry);
		l.setHref(absUrl);
		e.getLinks().add(l);

		l = new AtomLink();
		l.setRel(AppConstants.REL_COMPONENT);
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomFeed);
		l.setHref(absUrl + "/comp");
		e.getLinks().add(l);

		e.getCategories().add(AppConstants.CAT_SRV_INFO);

		AtomContent c;
		if (expandContent) {
			if (isXml) {
				c = new AtomContent();
				c.setValue(getContent(req));
			} else {
				c = new AtomContent() {
					@XmlValue
					@SuppressWarnings("unused")
					public ServerInfo value = getContent(req);
				};
			}
		} else {
			c = new AtomContent();
			c.setSrc(absUrl + "/content");
		}
		c.setType(isXml ? AppConstants.MT_SERVER_INFO_XML : AppConstants.OBJ_JSON_ServerInfo);
		e.setContent(c);
		return e;
	}

	@GET
	@Path("content")
	@Produces( { AppConstants.OBJ_JSON_ServerInfo, AppConstants.MT_SERVER_INFO_XML })
	public ServerInfo getContent(@Context HttpServletRequest req) throws RemoteException, DIException, NotBoundException {
		Session s = EnvUtils.getServerApiSession(req);
		ServerInfo si = new ServerInfo();
		com.ibm.di.api.remote.ServerInfo apiSi = s.getServerInfo();
		si.setHostname(apiSi.getHostName());
		si.setIpAddress(apiSi.getIPAddress());
		si.setOperatingSystem(apiSi.getOperatingSystem());
		si.setServerBootTime(apiSi.getServerBootTime().getTime());
		si.setServerId(apiSi.getServerID());
		si.setServerVersion(apiSi.getServerVersion());

		return si;
	}

	@Path("comp")
	public ComponentFeed getComponents() {
		return new ComponentFeed();
	}
}
