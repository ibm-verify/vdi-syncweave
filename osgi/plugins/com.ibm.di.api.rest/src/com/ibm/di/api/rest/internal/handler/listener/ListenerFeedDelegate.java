/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.listener;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.Response.Status.Family;

import javax.ws.rs.Path;
import com.ibm.di.web.common.atom.AtomFeed;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.ALEvent;
import com.ibm.di.api.bind.AssemblyLineEvent;
import com.ibm.di.api.bind.BatchEvent;
import com.ibm.di.api.bind.CIEvent;
import com.ibm.di.api.bind.ConfigFileEvent;
import com.ibm.di.api.bind.DIEvent;
import com.ibm.di.api.bind.Event;
import com.ibm.di.api.bind.Listener;
import com.ibm.di.api.bind.LogEvent;
import com.ibm.di.api.bind.PollChannel;
import com.ibm.di.api.remote.RemoteListener;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.listener.ListenerContext;
import com.ibm.di.api.rest.internal.listener.ListenerFactory;
import com.ibm.di.api.rest.internal.listener.QueueConsumer;
import com.ibm.di.api.rest.internal.registry.ListenerRegistry;
import com.ibm.di.api.rest.internal.registry.ListenerRegistry.ListenerRegistration;
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
@Path(ListenerFeedDelegate.URL)
public class ListenerFeedDelegate {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public static final String URL = "listener";

	private static final MediaType MT_XML = MediaType.valueOf(AppConstants.MT_LISTENER_XML);
	private static final MediaType MT_JSON_AlEvent = MediaType.valueOf(AppConstants.OBJ_JSON_AlEvent);
	private static final MediaType MT_JSON_CIEvent = MediaType.valueOf(AppConstants.OBJ_JSON_CiEvent);
	private static final MediaType MT_JSON_DIEvent = MediaType.valueOf(AppConstants.OBJ_JSON_DiEvent);
	private static final MediaType MT_JSON_AssemblyLineEvent = MediaType.valueOf(AppConstants.OBJ_JSON_AssemblyLineEvent);
	private static final MediaType MT_JSON_LogEvent = MediaType.valueOf(AppConstants.OBJ_JSON_LogEvent);
	private static final MediaType MT_JSON_ConfigFileEvent = MediaType.valueOf(AppConstants.OBJ_JSON_ConfigFileEvent);
	private static final MediaType MT_JSON_BatchEvent = MediaType.valueOf(AppConstants.OBJ_JSON_BatchEvent);
	public ListenerFeedDelegate() {	}
	@GET
	@Produces(AppConstants.OBJ_JSON_AtomFeed)
	public AtomFeed getFeedAsJson(@Context HttpServletRequest req, @Context UriInfo uri) throws DIException, NotBoundException,
			IOException {
		return getListenerFeed(req.getSession()).getFeed(uri, false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public AtomFeed getFeedAsXml(@Context HttpServletRequest req, @Context UriInfo uri) throws DIException, NotBoundException,
			IOException {
		return getListenerFeed(req.getSession()).getFeed(uri, true);
	}

	@Path("{lId}")
	public Object getListener(@Context HttpServletRequest req, @PathParam("lId") String lId) throws RemoteException, DIException,
			NotBoundException {
		return getListenerFeed(req.getSession()).getListener(lId);
	}

	@SuppressWarnings("unchecked")
	private ListenerFeed<Listener> getListenerFeed(HttpSession sess) throws RemoteException, DIException, NotBoundException {
		ListenerContext<Listener> ctx = (ListenerContext<Listener>) sess.getAttribute(DIEventListenerContext.class.getName());
		if (ctx == null) {
			synchronized (sess) {
				ctx = (ListenerContext<Listener>) sess.getAttribute(DIEventListenerContext.class.getName());
				if (ctx == null) {
					Session session = EnvUtils.getServerApiSession(sess);
					ListenerRegistry lr = EnvUtils.getListenerRegistry(sess.getServletContext());
					ListenerFactory lf = EnvUtils.getListenerFactory(sess.getServletContext());
					ctx = new DIEventListenerContext(session, lf, lr);
					sess.setAttribute(DIEventListenerContext.class.getName(), ctx);
				}
			}
		}
		return new ListenerFeed<Listener>(ctx);
	}

	@POST
	@Consumes( { AppConstants.MT_LISTENER_JSON, AppConstants.MT_LISTENER_XML })
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public Response createListenerAsJson(@Context HttpServletRequest req, @Context UriInfo uri, Listener listener)
			throws DIException, IOException, NotBoundException {
		return getListenerFeed(req.getSession()).createListener(uri, listener, false);
	}

	@POST
	@Consumes( { AppConstants.MT_LISTENER_JSON, AppConstants.MT_LISTENER_XML })
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response createListenerAsXml(@Context HttpServletRequest req, @Context UriInfo uri, Listener listener)
			throws DIException, IOException, NotBoundException {
		return getListenerFeed(req.getSession()).createListener(uri, listener, true);
	}

	@GET
	@Path("poll/{lId}")
	@Produces(AppConstants.MT_LISTENER_JSON)
	public Response pollAsJson(@PathParam("lId") String lId, @Context HttpServletRequest req) throws DIException {
		return poll_internal(null, null, lId, req, false);
	}

	@GET
	@Path("poll/{lId}")
	@Produces(AppConstants.MT_LISTENER_XML)
	public Response pollAsXml(@PathParam("lId") String lId, @Context HttpServletRequest req) throws DIException {
		return poll_internal(null, null, lId, req, true);
	}

	@GET
	@Path("poll/{ciId}/{lId}")
	@Produces(AppConstants.MT_LISTENER_JSON)
	public Response pollAsJson(@PathParam("ciId") String ciId, @PathParam("lId") String lId, @Context HttpServletRequest req)
			throws DIException {
		return poll_internal(ciId, null, lId, req, false);
	}

	@GET
	@Path("poll/{ciId}/{lId}")
	@Produces(AppConstants.MT_LISTENER_XML)
	public Response pollAsXml(@PathParam("ciId") String ciId, @PathParam("lId") String lId, @Context HttpServletRequest req)
			throws DIException {
		return poll_internal(ciId, null, lId, req, true);
	}

	@GET
	@Path("poll/{ciId}/{alId}/{lId}")
	@Produces(AppConstants.MT_LISTENER_JSON)
	public Response pollAsJson(@PathParam("ciId") String ciId, @PathParam("alId") String alId, @PathParam("lId") String lId,
			@Context HttpServletRequest req) throws DIException {
		return poll_internal(ciId, alId, lId, req, false);
	}

	@GET
	@Path("poll/{ciId}/{alId}/{lId}")
	@Produces(AppConstants.MT_LISTENER_XML)
	public Response pollAsXml(@PathParam("ciId") String ciId, @PathParam("alId") String alId, @PathParam("lId") String lId,
			@Context HttpServletRequest req) throws DIException {
		return poll_internal(ciId, alId, lId, req, true);
	}

	private Response poll_internal(String ciId, String alId, String lId, HttpServletRequest req, boolean isXml) throws DIException {
		ListenerRegistry reg = EnvUtils.getListenerRegistry(req.getSession().getServletContext());
		ListenerRegistration<? extends RemoteListener, ? extends Listener> lr;
		if (ciId == null) {
			lr = reg.getListenerReg(lId);
		} else if (alId == null) {
			lr = reg.getListenerReg(ciId, lId);
		} else {
			lr = reg.getListenerReg(ciId, alId, lId);
		}

		Response notOk = checkOk(lr);
		return notOk != null ? notOk : readQueue(lr.getListener(), (PollChannel) lr.getBinding().getChannel(), req.getSession(),
				isXml);
	}

	private Response checkOk(ListenerRegistration<? extends RemoteListener, ? extends Listener> lr) {
		if (lr == null) {
			return Response.status(Status.NOT_FOUND).build();
		} else if (!(lr.getBinding().getChannel() instanceof PollChannel)) {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
		return null;
	}

	private Response readQueue(RemoteListener l, PollChannel channel, HttpSession s, boolean isXml) throws DIException {
		Event evt = QueueConsumer.consume(l, channel, s);

		return evt != null ? Response.ok(evt, getEventMediaType(evt, isXml)).build() : Response.status(new StatusType() {
			public Family getFamily() {
				return Family.OTHER;
			}

			public String getReasonPhrase() {
				return "Request Timeout";
			}

			public int getStatusCode() {
				return 408;
			}
		}).build();
	}

	private static MediaType getEventMediaType(Event e, boolean isXml) {
		if (isXml) {
			return MT_XML;
		}

		if (e instanceof BatchEvent) {
			return MT_JSON_BatchEvent;
		} else if (e instanceof AssemblyLineEvent) {
			return MT_JSON_AssemblyLineEvent;
		} else if (e instanceof LogEvent) {
			return MT_JSON_LogEvent;
		} else if (e instanceof ConfigFileEvent) {
			return MT_JSON_ConfigFileEvent;
		} else if (e instanceof ALEvent) {
			return MT_JSON_AlEvent;
		} else if (e instanceof CIEvent) {
			return MT_JSON_CIEvent;
		} else if (e instanceof DIEvent) {
			return MT_JSON_DIEvent;
		}

		// should not reach to here
		return MediaType.valueOf(AppConstants.MT_LISTENER_JSON);
	}
}
