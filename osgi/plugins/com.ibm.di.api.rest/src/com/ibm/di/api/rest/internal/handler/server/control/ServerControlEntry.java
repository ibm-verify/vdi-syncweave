/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.server.control;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.Session;
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
public class ServerControlEntry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public static final String URL = "control";

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public AtomEntry getSelfAsJson(@Context UriInfo uri) {
		return getSelf(uri.getAbsolutePath().toString(), false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public AtomEntry getSelfAsXml(@Context UriInfo uri) {
		return getSelf(uri.getAbsolutePath().toString(), true);
	}

	public AtomEntry getSelf(String absUrl, boolean isXml) {
		AtomEntry e = new AtomEntry();
		{
			e.setId(absUrl);
			e.setUpdated(System.currentTimeMillis());

			AtomLink l = new AtomLink();
			l.setRel(AppConstants.REL_SELF);
			l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomEntry);
			l.setHref(absUrl);
			e.getLinks().add(l);

			l = new AtomLink();
			l.setRel(AppConstants.REL_SHUTDOWN);
			l.setType(isXml ? AppConstants.MT_SERVER_CONTROL_XML : AppConstants.OBJ_JSON_Shutdown);
			l.setHref(absUrl + "/shutdown");
			e.getLinks().add(l);

			e.getCategories().add(AppConstants.CAT_SRV_CONTROL);
		}
		return e;
	}

	@POST
	@Path("shutdown")
	@Consumes( { AppConstants.OBJ_JSON_Shutdown, AppConstants.MT_SERVER_CONTROL_XML })
	public Response postShutdown(@Context HttpServletRequest req, com.ibm.di.api.bind.Shutdown shut) throws RemoteException,
			DIException, NotBoundException {
		Session s = EnvUtils.getServerApiSession(req);
		s.shutDownServer(shut.getExitCode(), shut.isSync());
		return Response.ok().build();
	}
}
