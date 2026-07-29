/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.ci.al;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;

import com.ibm.di.web.common.atom.AtomText;
import javax.naming.InvalidNameException;
import javax.servlet.http.HttpServletRequest;
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

import com.ibm.di.web.common.atom.AtomFeed;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.EntryProperty;
import com.ibm.di.api.bind.Listener;
import com.ibm.di.api.bind.StartAL;
import com.ibm.di.api.remote.AssemblyLine;
import com.ibm.di.api.remote.AssemblyLineHandler;
import com.ibm.di.api.remote.AssemblyLineListener;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.al.ManualAssemblyLineDriver;
import com.ibm.di.api.rest.internal.debug.DebugClient;
import com.ibm.di.api.rest.internal.handler.NotFound;
import com.ibm.di.api.rest.internal.handler.ci.al.AlListenerContext.ALListenerAttacher;
import com.ibm.di.api.rest.internal.registry.UserDataRegistry;
import com.ibm.di.api.rest.internal.registry.ListenerRegistry.ListenerRegistration;
import com.ibm.di.api.rest.internal.util.EnvUtils;
import com.ibm.di.api.rest.internal.util.InheritFromRewriter;
import com.ibm.di.api.rest.internal.util.AtomFeedFactory;

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
public class AssemblyLineFeed {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final ConfigInstance ci;

	public AssemblyLineFeed(ConfigInstance ci) {
		this.ci = ci;
	}

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomFeed)
	public Response getFeedAsJson(@Context UriInfo uri, @Context HttpServletRequest req) throws Exception {
		return getFeed(uri, req, false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response getFeedAsXml(@Context UriInfo uri, @Context HttpServletRequest req) throws Exception {
		return getFeed(uri, req, true);
	}

	private Response getFeed(UriInfo uri, HttpServletRequest req, boolean isXml) throws DIException, RemoteException, Exception {
		int[] alIds = ci.getAssemblyLineUniqueCodes();

		URI absUrl = uri.getAbsolutePath();

		AtomFeed feed = AtomFeedFactory.createAtomFeed();
		feed.setId(absUrl.toString());
		feed.setUpdated(System.currentTimeMillis());
		feed.getCategories().add(AppConstants.CAT_RES_ASSEMBLY_LINE);

		for (int alId : alIds) {
			feed.getEntries().add(new AssemblyLineEntry(ci, alId).getSelf(absUrl + "/" + Integer.toString(alId), req, isXml));
		}

		return Response.ok(feed).build();
	}

	@Path("{alId}")
	public Object getAssemblyLineEntry(@PathParam("alId") int alId) throws Exception {
		AssemblyLine al = ci.getAssemblyLineByUniqueCode(alId);
		return al == null ? NotFound.getInstance() : new AssemblyLineEntry(ci, al);
	}

	@POST
	@Consumes( { AppConstants.OBJ_JSON_StartAL, AppConstants.MT_ASSEMBLY_LINE_XML })
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public Response createAsJson(@Context HttpServletRequest req, @Context UriInfo uri, StartAL start) throws Exception {
		try {
			return create(req, uri, start, false);
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	@POST
	@Consumes( { AppConstants.OBJ_JSON_StartAL, AppConstants.MT_ASSEMBLY_LINE_XML })
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response createAsXml(@Context HttpServletRequest req, @Context UriInfo uri, StartAL start) throws Exception {
		return create(req, uri, start, true);
	}

	private Response create(HttpServletRequest req, UriInfo uri, StartAL start, boolean isXml) throws DIException, RemoteException,
			InvalidNameException, Exception {
		if (start.getName() == null) {
			throw new DIException(AppConstants.L10N.getString("REST.API.AL.NAME.INVALID"));
		}

		AlStarter starter = null;
		if (start.isManual()) {
			starter = new ManualAlStarter(start, req);
		} else {
			if (start.getTcb() != null) {
				starter = new TcbAlStarter(start, req);
			} else {
				starter = new AlStarter(start, req);
			}
		}

		if (start.getAssemblyLineListener() != null) {
			AlListenerContext ctx = new AlListenerContext(ci.getConfigId(), starter, EnvUtils.getListenerFactory(req.getSession()
					.getServletContext()), EnvUtils.getListenerRegistry(req.getSession().getServletContext()));
			ctx.create(start.getAssemblyLineListener());
		} else {
			starter.startAlOnly();
		}

		URI absPath = URI.create(uri.getAbsolutePath().toString() + "/" + starter.getAL().getUniqueCode());
		return Response.created(absPath).entity(new AssemblyLineEntry(ci, starter.al).getSelf(absPath.toString(), req, isXml))
				.build();
	}
	
	private class ManualAlStarter extends AlStarter {

		public ManualAlStarter(StartAL start, HttpServletRequest req) {
			super(start, req);
		}

		public void attachListener(AssemblyLineListener l) throws RemoteException, DIException {
			startAlOnly();

			if (l != null) {
				// KK: create server api for attaching listeners on startup and
				// not after that.
				getAL().addListener(l, start.getAssemblyLineListener().isDeliverLogs(),
						start.getAssemblyLineListener().isDeliverEntry());
			}
		}

		@Override
		public void startAlOnly() throws RemoteException, DIException {
			AssemblyLineHandler alh;
			if(start.getTcb() != null) {
				try {
					alh = ci.startAssemblyLineManual(start.getName(), com.ibm.di.api.bind.BindUtil.toTCB(start.getTcb()));
				} catch (Exception e) {
					throw new DIException(e);
				}
			} else {
				alh = ci.startAssemblyLineManual(start.getName(), com.ibm.di.api.bind.BindUtil.toEntry(start.getIwe()));
			}

			UserDataRegistry data = EnvUtils.getUserDataRegistry(req.getSession().getServletContext());
			ExecutorService executor = (ExecutorService) req.getSession().getServletContext().getAttribute(
					ExecutorService.class.getName());

			al = alh.getAssemblyLine();
			data.setData(req, ci.getConfigId() + "/" + al.getUniqueCode(), new ManualAssemblyLineDriver(alh, executor));
		}
	}

	private class TcbAlStarter extends AlStarter {

		public TcbAlStarter(StartAL start, HttpServletRequest req) throws DIException {
			super(start, req);
			if (start.getTcb().getAssemblyLine() != null) {
				InheritFromRewriter.rewrite(start.getTcb().getAssemblyLine(), null);
			}
		}

		public void attachListener(AssemblyLineListener l) throws RemoteException, DIException {
			startAlOnly();
			if (l != null) {
				getAL().addListener(l, start.getAssemblyLineListener().isDeliverLogs(),
						start.getAssemblyLineListener().isDeliverEntry());
			}
		}

		@Override
		public void startAlOnly() throws RemoteException, DIException {
			try {
				//
				// -- User request debug session.
				// -- Create a debug client and update TCB with connection details
				//
				EntryProperty debugPort = null;
				for(EntryProperty prop : start.getTcb().getProperties()) {
					if("assemblyline.debugport".equals(prop.getName())) {
						debugPort = prop;
					}
				}
				
				DebugClient client = null;
				if(debugPort != null) {
					System.out.println("Create DebugClient");
					client = new DebugClient(req.getSession(true));
					debugPort.setValue(""+client.getPort());
					EntryProperty p = new EntryProperty();
					p.setName("assemblyline.debughost");
					p.setValue(client.getHostname());
					start.getTcb().getProperties().add(p);
					System.out.println("DebugClient state: " + client.getState());
				}
				
				al = ci.startAssemblyLine(start.getName(), com.ibm.di.api.bind.BindUtil.toTCB(start.getTcb()), start.isSync());
				if(client != null) {
					System.out.println("Save debug session for " + al.getName() + "." + al.getUniqueCode());
					System.out.println("client state: " + client.getState());
					//
					// -- Save the debug client for use in later requests to /debug handle
					//
					req.getSession(true).setAttribute(al.getName() + "." + al.getUniqueCode(), client);
				}
			} catch (Exception e) {
				throw new DIException(e);
			}
		}
	}

	private class AlStarter implements ALListenerAttacher {

		protected final StartAL start;
		protected final HttpServletRequest req;
		protected AssemblyLine al;

		public AlStarter(StartAL start, HttpServletRequest req) {
			this.start = start;
			this.req = req;
		}

		public void attachListener(AssemblyLineListener l) throws RemoteException, DIException {
			al = ci.startAssemblyLine(start.getName(), com.ibm.di.api.bind.BindUtil.toEntry(start.getIwe()), l, start
					.getAssemblyLineListener().isDeliverLogs(), start.isSync(), start.getAssemblyLineListener().isDeliverEntry());
		}

		public void startAlOnly() throws RemoteException, DIException {
			al = ci.startAssemblyLine(start.getName(), com.ibm.di.api.bind.BindUtil.toEntry(start.getIwe()), start.isSync());
		}

		public AssemblyLine getAL() {
			return al;
		}

		public void detachListener(ListenerRegistration<AssemblyLineListener, ? extends Listener> r) throws RemoteException,
				DIException {
		}
	}
}
