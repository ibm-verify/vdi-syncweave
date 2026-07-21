/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.tombstone;

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

import com.ibm.di.web.common.atom.AtomContent;
import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.Tombstone;
import com.ibm.di.api.bind.BindUtil;
import com.ibm.di.api.remote.TombstoneManager;
import com.ibm.di.api.rest.internal.AppConstants;
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
public class TombstoneEntry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private final Tombstone t;

	public TombstoneEntry(Tombstone t) {
		this.t = t;
	}

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public AtomEntry getSelf(@Context UriInfo uri) throws UnsupportedEncodingException, RemoteException, DIException,
			NotBoundException {
		return getSelf(uri.getAbsolutePath().toString(), false, true);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public AtomEntry getSelfAsXml(@Context UriInfo uri) throws UnsupportedEncodingException, RemoteException, DIException,
			NotBoundException {
		return getSelf(uri.getAbsolutePath().toString(), true, true);
	}

	public AtomEntry getSelf(String absUrl, boolean isXml, boolean expandContent) throws UnsupportedEncodingException,
			RemoteException, DIException, NotBoundException {
		AtomEntry e = new AtomEntry();
		e.setId(absUrl);
		e.setUpdated(t.getTombstoneCreateTime().getTime());
		e.setTitle(createAtomText(t.getGUID()));
		e.getCategories().add(AppConstants.CAT_RES_TOMBSTONE);

		AtomLink l = new AtomLink();
		l.setRel(AppConstants.REL_SELF);
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomEntry);
		l.setHref(e.getId());
		e.getLinks().add(l);

		AtomContent c = new AtomContent();
		c.setType(isXml ? AppConstants.MT_TOMBSTONE_XML : AppConstants.OBJ_JSON_Tombstone);
		e.setContent(c);
		if (expandContent) {
			c.setValue(BindUtil.fromTombstone(t));
		} else {
			c.setSrc(e.getId() + "/content");
		}

		return e;
	}

	@DELETE
	public Response delete(@Context HttpServletRequest req) throws RemoteException, DIException, NotBoundException {
		TombstoneManager tm = EnvUtils.getServerApiSession(req).getTombstoneManager();
		tm.deleteTombstone(t.getGUID());
		return Response.noContent().build();
	}

	@GET
	@Path("content")
	@Produces( { AppConstants.OBJ_JSON_Tombstone, AppConstants.MT_TOMBSTONE_XML })
	public Response content() {
		com.ibm.di.api.bind.Tombstone ts = BindUtil.fromTombstone(t);
		return Response.ok(ts).build();
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
