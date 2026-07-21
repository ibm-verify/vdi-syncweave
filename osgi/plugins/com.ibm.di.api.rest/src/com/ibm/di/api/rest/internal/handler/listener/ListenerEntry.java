/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.listener;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;

import com.ibm.di.web.common.atom.AtomText;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlValue;

import com.ibm.di.web.common.atom.AtomContent;
import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.AssemblyLineListener;
import com.ibm.di.api.bind.DIEventListener;
import com.ibm.di.api.bind.Listener;
import com.ibm.di.api.bind.LogListener;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.listener.ListenerContext;

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
public class ListenerEntry<L extends Listener> {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final L l;
	private final ListenerContext<L> ctx;
	private final String id;
	private String jsonListenerType;

	public ListenerEntry(String id, L l, ListenerContext<L> ctx) {
		this.id = id;
		this.l = l;
		this.ctx = ctx;

		jsonListenerType = AppConstants.MT_LISTENER_JSON;
		if (l instanceof AssemblyLineListener) {
			jsonListenerType = AppConstants.OBJ_JSON_AssemblyLineListener;
		} else if (l instanceof LogListener) {
			jsonListenerType = AppConstants.OBJ_JSON_LogListener;
		} else if (l instanceof DIEventListener) {
			jsonListenerType = AppConstants.OBJ_JSON_DIEventListener;
		}
	}

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public AtomEntry getSelfAsJson(@Context UriInfo uri) throws DIException, IOException {
		return getSelf(uri.getBaseUri(), uri.getPath(false), false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public AtomEntry getSelfAsXml(@Context UriInfo uri) throws DIException, IOException {
		return getSelf(uri.getBaseUri(), uri.getPath(false), true);
	}

	public AtomEntry getSelf(URI baseUri, String pathToEntry, boolean isXml) throws DIException, RemoteException {
		AtomEntry e = new AtomEntry();
		e.setId(URI.create(baseUri.toString() + "/" + pathToEntry).normalize().toString());
		e.setUpdated(System.currentTimeMillis());
		e.setTitle(createAtomText(id));

		e.getCategories().add(AppConstants.CAT_RES_LISTENER);
		e.getCategories().addAll(ctx.getCategories(id));
		e.getLinks().addAll(ctx.getLinks(baseUri, id));

		AtomLink self = new AtomLink();
		self.setRel(AppConstants.REL_SELF);
		self.setHref(e.getId());
		e.getLinks().add(self);

		AtomContent c;
		if (isXml) {
			c = new AtomContent();
			c.setType(AppConstants.MT_LISTENER_XML);
			c.setValue(l);
		} else {
			c = new AtomContent() {
				@XmlValue
				@SuppressWarnings("unused")
				protected Listener value = l;
			};
			c.setType(jsonListenerType);
		}

		e.setContent(c);

		return e;
	}

	@DELETE
	public Response deleteListener() throws RemoteException, DIException {
		ctx.delete(id);
		return Response.ok().build();
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
