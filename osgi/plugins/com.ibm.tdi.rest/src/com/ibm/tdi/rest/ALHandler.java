/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.rest;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.api.local.ServerInfo;
import com.ibm.di.api.local.Session;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.TaskCallBlock;

@Path("")
public class ALHandler {
	
	public final static String API_VERSION = "10.0.0.6";
	
	private final static ALCache cache = new ALCache();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response handleTopLevelRequest(@Context HttpServletRequest req) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		Map<String,Object> map = new HashMap<String, Object>();
		ServerInfo serverInfo = sess.getServerInfo();
		map.put("version", API_VERSION);
		map.put("osname", serverInfo.getOperatingSystem());
		map.put("sdi_version", serverInfo.getServerVersion());
		map.put("booted", serverInfo.getServerBootTime());
		map.put("configurations", sess.listAllConfigurations());
		map.put("admin", "admin");
		map.put("config", "config");
		return Response.ok(map).build();
	}
	
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response handleClearCache(@Context HttpServletRequest req) throws Exception {
		Map<String,String> map = new HashMap<String, String>();
		if(cache != null) {
			cache.clearCache();
		}
		map.put("status", "Cached cleared");
		return Response.ok(map).build();
	}

	@Path("config/{config}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response handleConfigRequest(@Context HttpServletRequest req, @PathParam("config") String config) throws Exception {
		MetamergeConfig mc = getConfig(req, config);
		List<String> list = new ArrayList<String>();
		for(String str : mc.getDefaultFolder(1).getNames()) {
			list.add(str);
		}
		return Response.ok(list).build();
	}
	
	@Path("config/{config}/{al}/parameters")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response handleConfigAlInfoRequest(@Context HttpServletRequest req, @PathParam("config") String config, @PathParam("al") String al)  throws Exception {
		MetamergeConfig mc = getConfig(req, config);
		Map<String, Object> result = newMap();
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		AssemblyLineConfig alconfig = mc.getAssemblyLine(al);
		SchemaConfig params = alconfig.getPublishedInitParams();
		for(String p : params.getItemNames()) {
			Map<String, Object> pm = newMap();
			pm.put("name", p);
			pm.put("type", params.getItem(p).getExternalSyntax());
			pm.put("comment", params.getItem(p).getUserComment());
			list.add(pm);
		}
		
		Map<String, Object> ops = newMap();
		for(int i = 0; i < alconfig.getOperations().size(); i++) {
			OperationConfig oc = (OperationConfig) alconfig.getOperations().getConfig(i);
			Map<String, Object> map = newMap();
			map.put("input", oc.getSchema(true).getItemNames()); 
			map.put("output", oc.getSchema(false).getItemNames());
			ops.put(oc.getShortName(), map);
		}
		
		result.put("initParams", list);
		result.put("operations", ops);
		
		return Response.ok(result).build();
	}
	
	@Path("config/{config}/{al}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response handleConfigAlRequestJson(@Context HttpServletRequest req, @PathParam("config") String config, @PathParam("al") String al)  throws Exception {
		return executeAL(req, config, al, null);
	}
	
	@Path("config/{config}/{al}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response handleConfigAlRequestPost(@Context HttpServletRequest req, @PathParam("config") String config, @PathParam("al") String al, Hashtable<String, Object> data)  throws Exception {
		return executeAL(req, config, al, data);
	}
	
	private Response executeAL(HttpServletRequest req, String config, String al, Object input) throws Exception {
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		ALCacheEntry ce = null;
		boolean hasFeed = false;
		try {
			ce = cache.getAssemblyLine(config, al);
			
			// -- verify that handle/config/al is still valid
			if(ce.handle != null && (ce.handle.getAssemblyLine() == null || ce.handle.getAssemblyLine().getConfigInstance() == null) ) {
				ce.handle = null;
			}

			Entry iwe = null;
			
			if(ce.handle == null) {
			
				Session sess = SessionUtils.getServerApiSession(req);
				ConfigInstance ci = sess.getConfigInstance(config);
				if(ci == null) {
					ci = sess.startConfigInstance(config);
				}
				
				AssemblyLineConfig alc = ci.getConfiguration().getAssemblyLine(al);
				for(int i = 0; i < alc.getEntryFeedComponents().size(); i++) {
					if(alc.getEntryFeedComponents().getConfig(i).getEnabled()) {
						hasFeed = true;
					}
				}
				
				TaskCallBlock tcb = buildTCB(req, hasFeed, input);
				if(input != null) {
					tcb.setOperationInitParam("http_body", input);
				}
				
				// -- initial work entry (null out empty iwe)
				iwe = tcb.getInitialWorkEntry();
				if(iwe != null && iwe.size() == 0) {
					iwe = null;
				}
				
				ce.handle = ci.startAssemblyLineManual(al, tcb);
			}
			
			Entry e = null;
			
			//
			// -- If AL has feed component then we loop over it to build
			// -- an array of objects, otherwise return the first/single entry.
			//
			do {
				e = ce.handle.executeCycle((Entry)iwe, (iwe != null));
				iwe = null;
				if(e != null) {
					list.add(entryToMap(e));
				}
			} while(e != null && hasFeed);
			
		} finally {
			cache.cacheAssemblyLine(ce, hasFeed);
		}
		
		if(list.size() == 1) {
			return Response.ok(list.get(0)).build();
		} else {
			return Response.ok(list).build();
		}
	}
	
	private Map<String,Object> entryToMap(Entry entry) {
		Map<String,Object> map = new HashMap<String, Object>();
		for(String str : entry.getAttributeNames()) {
			Attribute attr = entry.getAttribute(str);
			List list = new ArrayList<Object>();
			if(attr != null) {
				for(int i = 0; i < attr.size(); i++) {
					Object value = attr.getValue(i);
					if(value instanceof Entry) {
						list.add(entryToMap((Entry)value));
					} else {
						list.add(value);
					}
				}
			}
			if(list.size() == 1) {
				map.put(str, list.get(0));
			} else if (list.size() > 1) {
				map.put(str,  list);
			}
		}
		return map;
	}
	
	private Entry mapToEntry(Object input) {
		if(input instanceof Map<?,?>) {
			Entry e = new Entry();
			Map map = (Map)input;
			for(Iterator keys = map.keySet().iterator(); keys.hasNext(); ) {
				Object key = keys.next();
				Object value = map.get(key);
				if(value instanceof Map<?,?>) {
					value = mapToEntry(value);
				} else if(value instanceof List<?>) {
					List<?> list = (List<?>)value;
					Attribute attr = new Attribute();
					for(int i = 0; i < list.size(); i++) {
						attr.addValue(list.get(i));
					}
					value = attr;
				}
				e.setAttribute(key.toString(), value);
			}
			return e;
		}
		return null;
	}

	private TaskCallBlock buildTCB(HttpServletRequest req, boolean hasFeed, Object input) {
		TaskCallBlock tcb = new TaskCallBlock();
		tcb.setOperationInitParam("http_request",req);
		
		Entry headers = new Entry();
		for(Enumeration<String> en = req.getHeaderNames(); en.hasMoreElements(); ) {
			String hdr = en.nextElement();
			headers.setAttribute(hdr, req.getHeader(hdr));
		}
		tcb.setOperationInitParam("http_headers", headers);
		
		Entry e = mapToEntry(input);
		if(e != null) {
			for(String str : e.getAttributeNames()) {
				if(str.equalsIgnoreCase("initparams") && e.getObject(str) instanceof Entry) {
					tcb.getOperationInitParams().merge((Entry) e.getObject(str));
				} else if(str.equalsIgnoreCase("iwe") && e.getObject(str) instanceof Entry) {
					tcb.setInitialWorkEntry((Entry)e.getObject(str));
				}
			}
		}
		return tcb;
	}
	
	/**
	 * Retrieves the configuration for a given config
	 * 
	 * @param req
	 * @param config
	 * @return
	 * @throws Exception
	 */
	private MetamergeConfig getConfig(HttpServletRequest req, String config) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		ConfigInstance ci = sess.getConfigInstance(config);
		boolean didStart = false;
		try {
			if(ci == null) {
				ci = sess.startConfigInstance(config);
				didStart = true;
			}
			return ci.getConfiguration();
		} finally {
			if(didStart)
				ci.stop();
		}
		
	}
	
	/**
	 * Returns a new Map<String,Object> instance
	 * @return
	 */
	private Map<String,Object> newMap() {
		return new HashMap<String, Object>();
	}
}
