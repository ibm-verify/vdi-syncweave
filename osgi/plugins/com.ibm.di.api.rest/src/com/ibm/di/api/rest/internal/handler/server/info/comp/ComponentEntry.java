/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.server.info.comp;

import java.io.IOException;
import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

import com.ibm.di.web.common.atom.AtomCategory;
import com.ibm.di.web.common.atom.AtomContent;
import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.ServerInfo;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.util.EnvUtils;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.model.descriptor.ComponentDescriptor;
import com.ibm.di.model.descriptor.Label;

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
public class ComponentEntry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private final AtomCategory compCat;
	private final String compName;
	private final String jsonDescriptorType;

	public ComponentEntry(String compName, AtomCategory compCat) {
		this.compName = compName;
		this.compCat = compCat;

		jsonDescriptorType = (compCat == AppConstants.CAT_COMP_CONN ? AppConstants.OBJ_JSON_ConnectorDescriptor
				: (compCat == AppConstants.CAT_COMP_FC ? AppConstants.OBJ_JSON_FunctionComponentDescriptor
						: AppConstants.OBJ_JSON_ParserDescriptor));
	}

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public AtomEntry getSelfAsJson(@Context UriInfo uri, @Context HttpServletRequest req) throws DIException, NotBoundException,
			IOException {
		return getSelf(uri.getAbsolutePath(), true, req, false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public AtomEntry getSelfAsXml(@Context UriInfo uri, @Context HttpServletRequest req) throws DIException, NotBoundException,
			IOException {
		return getSelf(uri.getAbsolutePath(), true, req, true);
	}

	public AtomEntry getSelf(URI absUri, boolean expandContent, HttpServletRequest req, boolean isXml) throws DIException,
			NotBoundException, IOException {
		AtomEntry e = new AtomEntry();
		e.setId(absUri.toString());
		e.setUpdated(System.currentTimeMillis());
		e.setTitle(createAtomText(compName));
		
		try {
			Session s = EnvUtils.getServerApiSession(req);
			ServerInfo si = s.getServerInfo();
			ComponentDescriptor sd = si.getInstalledComponentDescriptor(compName);
			for(Label l : sd.getName()) {
				e.getTitle().getOtherAttributes().put(new QName(l.getLang()), l.getValue());
			}
		} catch (Exception err) {
			SystemFunctions.doNothing();
		}
		
		AtomLink l = new AtomLink();
		l.setRel(AppConstants.REL_SELF);
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomEntry);
		l.setHref(e.getId());
		e.getLinks().add(l);

		e.getCategories().add(AppConstants.CAT_SRV_COMPONENT);
		e.getCategories().add(compCat);

		AtomContent c = null;
		if (expandContent) {
			final Response resp = getContent(req, isXml);
			if (resp.getStatus() != Status.NOT_FOUND.getStatusCode()) {
				if (isXml) {
					c = new AtomContent();
					c.setValue(resp.getEntity());
				} else {
					c = new AtomContent() {
						@XmlValue
						@SuppressWarnings("unused")
						public ComponentDescriptor value = (ComponentDescriptor) resp.getEntity();
					};
				}
			}
		}

		if (c == null) {
			c = new AtomContent();
			c.setSrc(e.getId() + "/content");
		}
		c.setType(isXml ? AppConstants.MT_COMPONENT_XML : jsonDescriptorType);
		e.setContent(c);
		return e;
	}

	@GET
	@Path("content")
	@Produces(AppConstants.MT_COMPONENT_JSON)
	public Response getContentAsJson(@Context HttpServletRequest req) throws RemoteException, DIException, NotBoundException {
		return getContent(req, false);
	}

	@GET
	@Path("content")
	@Produces(AppConstants.MT_COMPONENT_XML)
	public Response getContentAsXml(@Context HttpServletRequest req) throws RemoteException, DIException, NotBoundException {
		return getContent(req, true);
	}

	private Response getContent(HttpServletRequest req, boolean isXml) throws RemoteException, DIException, NotBoundException {
		Session s = EnvUtils.getServerApiSession(req);
		ComponentDescriptor c = s.getServerInfo().getInstalledComponentDescriptor(compName);
		return c == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(c).type(
				isXml ? AppConstants.MT_COMPONENT_XML : jsonDescriptorType).build();
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
