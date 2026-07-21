/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.ci.al;

import java.io.File;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.AssemblyLineListener;
import com.ibm.di.api.bind.BindUtil;
import com.ibm.di.api.connection.IServerAPIConnection;
import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.api.remote.AssemblyLine;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.al.ManualAssemblyLineDriver;
import com.ibm.di.api.rest.internal.debug.DebugClient;
import com.ibm.di.api.rest.internal.handler.NotFound;
import com.ibm.di.api.rest.internal.handler.listener.ListenerFeed;
import com.ibm.di.api.rest.internal.registry.UserDataRegistry;
import com.ibm.di.api.rest.internal.util.ConfigConvertor;
import com.ibm.di.api.rest.internal.util.EnvUtils;
import com.ibm.di.config.bind.NamedBinding;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.TaskStatistics;
import com.ibm.di.util.Breakpoint;
import com.ibm.di.util.DebugServer;
import com.ibm.di.util.NullValue;

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
public class AssemblyLineEntry {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final ConfigInstance ci;

	private AssemblyLine al;
	private int alId;

	public AssemblyLineEntry(ConfigInstance ci, AssemblyLine al) {
		this.ci = ci;
		this.al = al;
	}

	public AssemblyLineEntry(ConfigInstance ci, int alId) {
		this.ci = ci;
		this.alId = alId;
	}

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public AtomEntry getSelfAsJson(@Context UriInfo uri, @Context HttpServletRequest req) throws Exception {
		return getSelf(uri.getAbsolutePath().toString(), req, false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public AtomEntry getSelfAsXml(@Context UriInfo uri, @Context HttpServletRequest req) throws Exception {
		return getSelf(uri.getAbsolutePath().toString(), req, true);
	}

	public AtomEntry getSelf(String absUrl, HttpServletRequest req, boolean isXml) throws RemoteException, DIException, Exception {
		AtomEntry e = new AtomEntry();
		e.setId(absUrl);
		e.setUpdated(System.currentTimeMillis());
		e.setTitle(createAtomText(getAL().getName() + "." + getAL().hashCode()));

		AtomLink l = new AtomLink();
		l.setRel(AppConstants.REL_SELF);
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomEntry);
		l.setHref(e.getId());
		e.getLinks().add(l);

		e.getCategories().add(AppConstants.CAT_RES_ASSEMBLY_LINE);

		if (getAL().isActive()) {
			e.getCategories().add(AppConstants.CAT_AL_ACTIVE);
		}

		if (getALHandle(req) != null) {
			e.getCategories().add(AppConstants.CAT_AL_MANUAL);

			l = new AtomLink();
			l.setRel(AppConstants.REL_HANDLE);
			l.setType(isXml ? AppConstants.MT_ASSEMBLY_LINE_XML : AppConstants.OBJ_JSON_ALHandle);
			l.setHref(absUrl + "/handle");
			e.getLinks().add(l);

			l = new AtomLink();
			l.setRel("script");
			l.setType("text/plain");
			l.setHref(absUrl + "/script");
			e.getLinks().add(l);
		}

		l = new AtomLink();
		l.setRel(AppConstants.REL_LISTENER);
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomFeed);
		l.setHref(absUrl + "/listener");
		e.getLinks().add(l);

		l = new AtomLink();
		l.setRel(AppConstants.REL_RESULT);
		l.setType(isXml ? AppConstants.MT_ENTRY_XML : AppConstants.OBJ_JSON_Entry);
		l.setHref(absUrl + "/result");
		e.getLinks().add(l);

		l = new AtomLink();
		l.setRel(AppConstants.REL_STATUS);
		l.setType(isXml ? AppConstants.MT_ASSEMBLY_LINE_XML : AppConstants.OBJ_JSON_TaskStatistics);
		l.setHref(absUrl + "/status");
		e.getLinks().add(l);

		l = new AtomLink();
		l.setRel(AppConstants.REL_LOG);
		l.setType(MediaType.TEXT_PLAIN);
		l.setHref(absUrl + "/log");
		e.getLinks().add(l);

		l = new AtomLink();
		l.setRel(AppConstants.REL_CONFIGURATION);
		l.setType(isXml ? AppConstants.MT_CONFIG_XML : AppConstants.OBJ_JSON_AssemblyLineBinding);
		l.setHref(absUrl + "/config");
		e.getLinks().add(l);
		
		//
		// -- Check if there is a debug client object active
		//
		if(getDebugClient(req) != null) {
			l = new AtomLink();
			l.setRel(AppConstants.REL_DEBUG);
			l.setType(AppConstants.MT_ASSEMBLY_LINE_JSON + ";type=debug");
			l.setHref(absUrl + "/debug");
			e.getLinks().add(l);
		}

		return e;
	}
	
	private DebugClient getDebugClient(HttpServletRequest req) throws Exception {
		Object obj = req.getSession(true).getAttribute(getAL().getName() + "." + getAL().getUniqueCode());
		if(obj instanceof DebugClient) {
			return (DebugClient) obj;
		}
		return null;
	}

	private void removeDebugClient(HttpServletRequest req) throws Exception {
		DebugClient client = getDebugClient(req);
		if(client != null) {
			client.shutdown();
		}
		req.getSession(true).removeAttribute(getAL().getName() + "." + getAL().getUniqueCode());
	}

	private ManualAssemblyLineDriver getALHandle(HttpServletRequest req) throws Exception {
		UserDataRegistry data = EnvUtils.getUserDataRegistry(req.getSession().getServletContext());
		String alCode = Integer.toString(getAL().getUniqueCode());
		return (ManualAssemblyLineDriver) data.getData(req, ci.getConfigId() + "/" + alCode);
	}

	private AssemblyLine getAL() throws Exception {
		if (al == null) {
			al = ci.getAssemblyLineByUniqueCode(alId);
			if (al == null) {
				throw new NoSuchObjectException(AppConstants.L10N.getString("REST.API.OBJECT.UNAVAILABLE"));
			}
		}
		return al;
	}

	@DELETE
	public Response delete(@Context HttpServletRequest req) throws RemoteException, Exception {
		UserDataRegistry data = EnvUtils.getUserDataRegistry(req.getSession().getServletContext());
		String alCode = Integer.toString(getAL().getUniqueCode());
		ManualAssemblyLineDriver alh = (ManualAssemblyLineDriver) data.removeData(req, ci.getConfigId() + "/" + alCode);
		if (alh != null) {
			alh.close();
		}

		getAL().stop(true);
		
		removeDebugClient(req);
		
		return Response.ok().build();
	}

	@Path("listener")
	public ListenerFeed<AssemblyLineListener> getListenerFeed(@Context ServletContext ctx) throws Exception {
		return new ListenerFeed<AssemblyLineListener>(new AlListenerContext(ci.getConfigId(), getAL(), EnvUtils
				.getListenerFactory(ctx), EnvUtils.getListenerRegistry(ctx)));
	}

	@GET
	@Path("result")
	@Produces( { AppConstants.OBJ_JSON_Entry, AppConstants.MT_ENTRY_XML })
	public Response getResultEntry() throws RemoteException, DIException, Exception {
		com.ibm.di.entry.Entry e = getAL().getResult();
		return e == null ? Response.status(Status.GONE).build() : Response.ok(BindUtil.fromEntry(e)).build();
	}

	@GET
	@Path("status")
	@Produces( { AppConstants.OBJ_JSON_TaskStatistics, AppConstants.MT_ASSEMBLY_LINE_XML })
	public Response getStatus() throws RemoteException, DIException, Exception {
		TaskStatistics stats = getAL().getStatistics();
		return stats == null ? Response.status(Status.GONE).build() : Response.ok(BindUtil.fromTaskStatistics(stats)).build();
	}

	@GET
	@Path("log")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getLog(@Context ServletContext ctx) throws RemoteException, Exception {
		// optimization to avoid loading the full systemlog in memory as string.
		IServerAPIConnection conn = EnvUtils.getServerApiConnection(ctx);
		IServerAPIConnectionService srvc = EnvUtils.getServerApiConnectionService(ctx);

		ResponseBuilder resp;
		if (srvc.isConnectionLocal(conn)) {
			String systemLogFilePath = getAL().getSystemLogFilePath();
			if (systemLogFilePath == null) {
				resp = Response.status(Status.NOT_FOUND);
			} else {
				resp = Response.ok(new File(systemLogFilePath));
			}
		} else {
			resp = Response.ok(getAL().getSystemLog());
		}
		return resp.build();

	}

	@Path("handle")
	public Object getHandle(@Context HttpServletRequest req) throws Exception {
		ManualAssemblyLineDriver alh = getALHandle(req);
		return alh == null ? NotFound.getInstance() : new AssemblyLineHandle(alh);
	}

	@GET
	@Path("config")
	@Produces( { AppConstants.OBJ_JSON_AssemblyLineBinding, AppConstants.MT_CONFIG_XML })
	public Response getConfiguration(@Context HttpServletRequest req, @Context UriInfo uri) throws Exception {
		NamedBinding alc = ConfigConvertor.fromConfig(getAL().getConfig(), MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER, getAL()
				.getName().substring(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER.length() + 1), uri.getBaseUri().toString());
		return Response.ok(alc).build();
	}

	@POST
	@Path("script")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(AppConstants.OBJ_JSON_Entry)
	public Response evaluateScriptAsJson(String script, @Context HttpServletRequest req) throws Exception {
		return evaluateScript(script, req, false);
	}

	@POST
	@Path("script")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(AppConstants.MT_ENTRY_XML)
	public Response evaluateScriptAsXml(String script, @Context HttpServletRequest req) throws Exception {
		return evaluateScript(script, req, true);
	}

	public Response evaluateScript(String script, @Context HttpServletRequest req, boolean isXml) throws Exception {
		ManualAssemblyLineDriver alHandle = getALHandle(req);
		if (alHandle == null) {
			return NotFound.getInstance().notFound();
		}
		Object obj = alHandle.eval(script);

		if (obj != null) {
			if (!(obj instanceof Entry)) {
				Entry e = new Entry();
				e.setAttribute("value", obj);
				obj = e;
			}

			obj = BindUtil.fromEntry((Entry) obj);
			return Response.ok(obj).build();
		} else {
			return Response.noContent().build();			
		}
	}

	@GET
	@Path("debug")
	@Produces( MediaType.APPLICATION_JSON )
	public Response debugGet(@Context HttpServletRequest req, @Context UriInfo uri) throws Exception {
		Map<String, Object> status = getDebugStatus(req);
		if(status == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(status).build();
	}
	
	@GET
	@Path("debug/{command}")
	@Produces( MediaType.APPLICATION_JSON )
	public Response evaluateDebug(@Context HttpServletRequest req, @Context UriInfo uri, @PathParam("command")String command, @QueryParam("param")String param) throws Exception {
		DebugClient client = getDebugClient(req);
		if(client == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		if(DebugServer.CONT.equals(command))
			client.continueAssemblyLine();
		else if(DebugServer.STEP_OVER.equals(command))
			client.stepOverAssemblyLine();
		else if(DebugServer.QUIT.equals(command))
			client.stopAssemblyLine();
		else if("rununtil".equals(command))
			client.runUntilAssemblyLine(param);
		else if(DebugServer.BREAKAT.equals(command))
			setBreakpoints(client, param);
		else if("watch".equals(command))
			setWatchList(client, param);
		else
			return Response.status(Status.NOT_FOUND).build();
		
		return Response.ok(getDebugStatus(req)).build();
	}
	
	private void setWatchList(DebugClient client, String param) throws Exception {
		for(String str : param.split(",")) {
			client.addWatch(str);
		}
	}

	private void setBreakpoints(DebugClient client, String locations) throws Exception {
		for(String str : locations.split(",")) {
			client.addBreakpoint(new Breakpoint(str, true, null));
		}
	}

	private Map<String, Object> getDebugStatus(HttpServletRequest req) throws Exception {
		DebugClient client = getDebugClient(req);
		if(client == null) {
			return null;
		}
		
		Map<String, Object> status = new HashMap<String, Object>();
		status.put("currentCycle", client.getCurrentCycle());
		// status.put("watchList", client.getWatchList());
		if(client.isWaiting())
			status.put("status", "waiting");
		else if (client.isPending())
			status.put("status", "pending");
		else if (client.isIdle())
			status.put("status", "idle");
		else if (client.isRunning())
			status.put("status", "running");
		else
			status.put("status", "unknown");
		
		//
		// -- Add watch expressions
		//
		Map<String, Object> watch = new HashMap<String, Object>();
		for(Iterator<String> keys = client.getWatchList().keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			watch.put(key, convertValue(client.getWatchValue(key)));
		}
		status.put("watch", watch);
		
		//
		// -- Current breakpoint
		//
		status.put("breakpoint", client.getLastBreak() == null ? "" : client.getLastBreak());
		
		return status;
	}

	private Map<String, Object> convertEntry(Entry entry) {
		Map<String,Object> map = new HashMap<String, Object>();
		for(String str : entry.getAttributeNames()) {
			map.put(str, convertValue(entry.getAttribute(str)));
		}
		return map;
	}
	
	private Object convertAttribute(Attribute attribute) {
		List<Object> list = new ArrayList<Object>();
		for(int i = 0; i < attribute.size(); i++) {
			list.add(convertValue(attribute.getValue(i)));
		}
		if(list.size() == 0)
			return "";
		else if(list.size() == 1)
			return list.get(0);
		else
			return list;
	}

	private Object convertValue(Object value) {
		if(value instanceof Attribute)
			return convertAttribute((Attribute)value);
		if(value instanceof Boolean || value instanceof Integer || value instanceof String)
			return value;
		else if(value instanceof Entry)
			return convertEntry((Entry)value);
		else if(value instanceof NullValue)
			return "[null value]";
		else
			return "" + value;
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
