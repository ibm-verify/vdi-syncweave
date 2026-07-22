/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.server.notify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.NotBoundException;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.CustomNotification;
import com.ibm.di.api.bind.Data;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.util.EnvUtils;
import com.ibm.di.function.UserFunctions;

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
public class CustomNotifyEntry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public static final String URL = "notify";

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
		e.setId(absUrl);
		e.setUpdated(System.currentTimeMillis());

		AtomLink l = new AtomLink();
		l.setRel(AppConstants.REL_SELF);
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomEntry);
		l.setHref(absUrl);
		e.getLinks().add(l);

		l = new AtomLink();
		l.setRel(AppConstants.REL_NOTIFY);
		l.setType(isXml ? AppConstants.MT_SERVER_NOTIFY_XML : AppConstants.OBJ_JSON_CustomNotification);
		l.setHref(absUrl);
		e.getLinks().add(l);

		e.getCategories().add(AppConstants.CAT_SRV_NOTIFICATION);
		return e;
	}

	@POST
	@Consumes( { AppConstants.OBJ_JSON_CustomNotification, AppConstants.MT_SERVER_NOTIFY_XML })
	public Response postNotification(@Context HttpServletRequest req, @Context Providers ps, CustomNotification notify)
			throws DIException, NotBoundException, IOException, ClassNotFoundException {

		if (notify.getId() == null || notify.getType() == null) {
			throw new IllegalArgumentException();
		}

		Data data = notify.getData();
		Object val = null;

		if (data != null) {
			String media = data.getType();
			val = data.getValue();

			if (val != null) {
				if ("application/octet-stream".equals(media)) {
					val = UserFunctions.base64Decode((String) val);
				} else if ("application/octet-stream+object".equals(media)) {
					ObjectInputStream ois = new ObjectInputStream(
							new ByteArrayInputStream(UserFunctions.base64Decode((String) val)));
					val = ois.readObject();
				}
			}
		}

		Session s = EnvUtils.getServerApiSession(req);
		s.sendCustomNotification(notify.getType(), notify.getId(), val);

		return Response.ok().build();
	}
}
