/*
 * Copyright contributors to the SyncWeave project
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

import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.Tombstone;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.remote.TombstoneManager;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.handler.tombstone.TombstoneFeed.TombstoneContext;
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
public class TsCiEntry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private String ciId;

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public AtomEntry getSelf(@Context UriInfo uri) throws UnsupportedEncodingException, RemoteException, DIException,
			NotBoundException {
		return getSelf(uri.getAbsolutePath().toString(), false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public AtomEntry getSelfAsXml(@Context UriInfo uri) throws UnsupportedEncodingException, RemoteException, DIException,
			NotBoundException {
		return getSelf(uri.getAbsolutePath().toString(), true);
	}

	public TsCiEntry(String ciId) {
		this.ciId = ciId;
	}

	public AtomEntry getSelf(String absUrl, boolean isXml) throws UnsupportedEncodingException, RemoteException, DIException,
			NotBoundException {
		AtomEntry e = new AtomEntry();
		e.setId(absUrl);
		e.setUpdated(System.currentTimeMillis());
		e.setTitle(createAtomText(ciId));

		AtomLink l = new AtomLink();
		l.setRel(AppConstants.REL_SELF);
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomEntry);
		l.setHref(e.getId());
		e.getLinks().add(l);

		e.getCategories().add(AppConstants.CAT_RES_CI);

		l = new AtomLink();
		l.setRel(AppConstants.REL_ASSEMBLY_LINE);
		l.setHref(e.getId() + "/al");
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomFeed);
		e.getLinks().add(l);

		l = new AtomLink();
		l.setRel(AppConstants.REL_TOMBSTONE);
		l.setHref(e.getId() + "/ts");
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomFeed);
		e.getLinks().add(l);

		return e;
	}

	@DELETE
	public Response delete(@Context HttpServletRequest req) throws RemoteException, DIException, NotBoundException {
		TombstoneManager tm = EnvUtils.getServerApiSession(req).getTombstoneManager();
		// A hackish deletion of all CI and AL Tombstones based on the config id
		tm.deleteALTombstones("", ciId);
		return Response.noContent().build();
	}

	@Path("ts")
	public TombstoneFeed getTombstones(@Context final HttpServletRequest req) throws RemoteException, DIException,
			NotBoundException {
		final Session s = EnvUtils.getServerApiSession(req);
		return new TombstoneFeed(new TombstoneContext() {
			private TombstoneManager tm = s.getTombstoneManager();

			public Tombstone[] getTombstones() throws RemoteException, DIException {
				return tm.getConfigInstanceTombstones(ciId);
			}
		});
	}

	@Path("al")
	public TsAlFeed getAssemblyLineFeed(@Context HttpServletRequest req) throws RemoteException, DIException {
		return new TsAlFeed(ciId);
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
