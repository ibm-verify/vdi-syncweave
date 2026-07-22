/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal.tdi;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import com.ibm.di.api.remote.AssemblyLineHandler;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.TaskCallBlock;
import com.ibm.di.ui.webui.internal.SessionUtils;

@Path("")
public class TDIAlHandler {

	@Path("{config}")
	@GET
	public Response handleConfigRequest() {
		return null;
	}
	
	@Path("{config}/{al}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String handleConfigAlRequest(@Context HttpServletRequest req, @PathParam("config") String config, @PathParam("al") String al) {
		String result = "(no data)";
		try {
			Session sess = SessionUtils.getServerApiSession(req);
			ConfigInstance ci = sess.getConfigInstance(config);
			if(ci == null) {
				ci = sess.startConfigInstance(config);
			}
			AssemblyLineHandler handle = ci.startAssemblyLineManual(al, null);
			Entry e = handle.executeCycle();
			if(e != null) {
				result = e.toDeltaString();
				handle.close();
			}
		} catch (Exception e) {
			result = e.toString();
		}
		return result;
	}

	@Path("{config}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String handleConfigAlRequestIndex(@Context HttpServletRequest req, @PathParam("config") String config) throws Exception {
		return handleConfigAlRequestHtml(req, config, "index.html");
	}
	
	@Path("{config}/{al}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String handleConfigAlRequestHtml(@Context HttpServletRequest req, @PathParam("config") String config, @PathParam("al") String al) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		ConfigInstance ci = sess.getConfigInstance(config);
		if(ci == null) {
			ci = sess.startConfigInstance(config);
		}
	
		ScriptConfig sc = ci.getConfiguration().getScript(al);
		if(sc != null) {
			return sc.getScript();
		}
		return executeAL(req, config, al, null);
	}
	
	@Path("{config}/{al}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String handleConfigAlRequestJson(@Context HttpServletRequest req, @PathParam("config") String config, @PathParam("al") String al)  throws Exception {
		return executeAL(req, config, al, null);
	}
	
	@Path("{config}/{al}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String handleConfigAlRequestPost(@Context HttpServletRequest req, @PathParam("config") String config, @PathParam("al") String al, Hashtable<String, Object> data)  throws Exception {
		return executeAL(req, config, al, data);
	}
	
	@Path("{config}/{al}")
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public String handleConfigAlRequestPostPlain(@Context HttpServletRequest req, @PathParam("config") String config, @PathParam("al") String al, String data)  throws Exception {
		return executeAL(req, config, al, data);
	}
	
	private String executeAL(HttpServletRequest req, String config, String al, Object input) throws Exception {
		List<Entry> list = new ArrayList<Entry>();
		AssemblyLineHandler handle = null;
		try {
			Session sess = SessionUtils.getServerApiSession(req);
			ConfigInstance ci = sess.getConfigInstance(config);
			if(ci == null) {
				ci = sess.startConfigInstance(config);
			}
			
			AssemblyLineConfig alc = ci.getConfiguration().getAssemblyLine(al);
			boolean hasFeed = false;
			for(int i = 0; i < alc.getEntryFeedComponents().size(); i++) {
				if(alc.getEntryFeedComponents().getConfig(i).getEnabled()) {
					hasFeed = true;
				}
			}
			
			TaskCallBlock tcb = buildTCB(req, hasFeed);
			
			if(input != null) {
				tcb.setOperationInitParam("http_body", input);
			}
			
			handle = ci.startAssemblyLineManual(al, tcb);
			Entry e = null;
			
			// -- Try to convert json payload to Entry and use as initial work
			Object iwe = mapToEntry(input);
			
			//
			// -- If AL has feed component then we loop over it to build
			// -- an array of objects, otherwise return the first/single entry.
			//
			do {
				if(iwe instanceof Entry) {
					e = handle.executeCycle((Entry)iwe);
					iwe = null;
				} else {
					e = handle.executeCycle();
				}
				if(e != null) {
					list.add(e);
				}
			} while(e != null && hasFeed);
			handle.close();
		} finally {
			if(handle != null)
				handle.close();
		}
		StringBuffer buf = new StringBuffer();
		if(list.size() == 1) {
			buf.append(list.get(0).toJSON());
		} else {
			buf.append("[\n");
			for(int i = 0; i < list.size(); i++) {
				if(i > 0)
					buf.append(",\n");
				buf.append(list.get(i).toJSON());
			}
			buf.append("]\n");
		}
		return buf.toString();		
	}
	
	private Object mapToEntry(Object input) {
		if(input instanceof Map<?,?>) {
			Entry e = new Entry();
			Map map = (Map)input;
			for(Iterator keys = map.keySet().iterator(); keys.hasNext(); ) {
				Object key = keys.next();
				e.setAttribute(key.toString(), map.get(key));
			}
			return e;
		}
		return input;
	}

	private TaskCallBlock buildTCB(HttpServletRequest req, boolean hasFeed) {
		TaskCallBlock tcb = new TaskCallBlock();
		tcb.setOperationInitParam("request",req);
		for(Enumeration<String> en = req.getHeaderNames(); en.hasMoreElements(); ) {
			String hdr = en.nextElement();
			tcb.setOperationInitParam("http_" + hdr, req.getHeader(hdr));
		}
		return tcb;
	}
}
