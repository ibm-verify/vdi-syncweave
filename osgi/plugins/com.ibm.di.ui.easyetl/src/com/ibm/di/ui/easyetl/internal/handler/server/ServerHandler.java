/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.easyetl.internal.handler.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.AssemblyLinePool;
import com.ibm.di.server.Monitor;
import com.ibm.di.ui.easyetl.bind.Auth;
import com.ibm.di.ui.easyetl.bind.Logging;
import com.ibm.di.ui.easyetl.bind.Vmstatus;
import com.ibm.di.ui.easyetl.internal.AuthHttpContext;
import com.ibm.di.ui.easyetl.internal.SessionUtils;
import com.ibm.di.util.PropertiesFile;
import com.ibm.di.web.common.internal.auth.LocalApiAuthHttpContext;

@Path(ServerHandler.URL)

public class ServerHandler {
	
	public final static String URL = "server";

	@Path("log")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getServerLog(@Context HttpServletRequest req, @QueryParam("lines") @DefaultValue("100") int lines) throws Exception {
		BufferedReader fis = (BufferedReader) req.getSession().getAttribute(ServerHandler.class.getCanonicalName());
		if(fis == null && lines != 0) {
			fis = new BufferedReader(new FileReader(new File("logs/ibmdi.log")));
			req.getSession().setAttribute(ServerHandler.class.getCanonicalName(), fis);
		}
		if(fis != null && lines == 0) {
			fis.close();
			fis = null;
			req.getSession().removeAttribute(ServerHandler.class.getCanonicalName());
		}
		
		ArrayList<String> list = new ArrayList<String>();
		while(fis != null && fis.ready()) {
			list.add(fis.readLine() + "\n");
			if(list.size() > lines)
				list.remove(0);
		}
		
		StringBuffer buf = new StringBuffer();
		for(String str : list) {
			buf.append(str);
		}
		return buf.toString();
	}

	@Path("java/{prop}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getJavaProperty(@Context HttpServletRequest req, @PathParam("prop") String property) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		return sess.getJavaProperty(property);
	}

	@Path("java/{prop}/{value}")
	@PUT
	public void setJavaProperties(@Context HttpServletRequest req, @PathParam("prop") String property, @PathParam("value") String value, @QueryParam("persist") @DefaultValue("false") boolean persist) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		sess.setJavaProperty(property, value != null ? value : "");
		if(persist) {
			// Also set in memory in this JVM.
			if (value != null)
				System.setProperty(property, value);
			else
				System.clearProperty(property);

			PropertiesFile propsFile = new PropertiesFile("solution.properties", false);
			propsFile.setProperty(property, value);
			propsFile.store("solution.properties");
		}
	}

	@Path("java/{prop}")
	@DELETE
	public void deleteJavaProperty(@Context HttpServletRequest req, @PathParam("prop") String property, @QueryParam("persist") @DefaultValue("false") boolean persist) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		sess.setJavaProperty(property, "");
		if(persist) {
			PropertiesFile propsFile = new PropertiesFile("solution.properties", false);
			propsFile.removeProperty(property);
			propsFile.store("solution.properties");
		}
	}
	
	@Path("tombstonesmanager")
	@DELETE
	public void stopTombstoneManager(@Context HttpServletRequest req) throws Exception {
		setJavaProperties(req, "com.ibm.di.tm.on", "false", true);
	}

	@Path("tombstonesmanager")
	@PUT
	public void startTombstoneManager(@Context HttpServletRequest req) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		if(sess.getTombstoneManager() == null) {
			setJavaProperties(req, "com.ibm.di.tm.on", "true", true);
			sess.startTombstoneManager();
		}
	}

	@Path("tombstonemanager")
	@GET
	public String getTombstoneManagerStatus(@Context HttpServletRequest req) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		if(sess.getTombstoneManager() == null) {
			return "- not started";
		} else {
			return "+ running";
		}
	}
	
	@Path("logging")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogSettings(@Context HttpServletRequest req) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		Logging logging = new Logging();
		logging.setEnabled(Boolean.valueOf(sess.getJavaProperty("SystemLog.defaultCreateLog")));
		String str = sess.getJavaProperty("SystemLog.defaultMaxGenerations");
		if(str == null || str.equals(""))
			str = "10";
		logging.setGenerations(Integer.valueOf(str));
		logging.setFormat("%d{DEFAULT} %-5p - %m%n"); // sess.getJavaProperty("SystemLog.defaultLogPattern"));
		logging.setLevel("INFO"); // sess.getJavaProperty("SystemLog.defaultLevel"));
		
//		if(logging.getFormat() == null)
//			logging.setFormat("%d{DEFAULT} %-5p - %m%n");
//		if(logging.getLevel() == null)
//			logging.setLevel("INFO");
		
		return Response.ok(logging).build();
	}
	
	@Path("logging")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void setLogSettings(@Context HttpServletRequest req, Logging logging) throws Exception {
		setJavaProperties(req, "SystemLog.defaultCreateLog", ""+logging.isEnabled(), true);
		setJavaProperties(req, "SystemLog.defaultMaxGenerations", ""+logging.getGenerations(), true);
//		setJavaProperties(req, "SystemLog.defaultLogPattern", logging.getFormat(), true);
//		setJavaProperties(req, "SystemLog.defaultLevel", logging.getLevel(), true);
	}
	
	@Path("vmstatus")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVMStatus(@Context HttpServletRequest req) throws Exception {
		Vmstatus vm = new Vmstatus();
		vm.setActiveThreads(Thread.activeCount());
		vm.setFreeMemory(Runtime.getRuntime().freeMemory());
		vm.setTotalMemory(Runtime.getRuntime().totalMemory());
		vm.setMaxMemory(Runtime.getRuntime().maxMemory());
		vm.setNumProcessors(Runtime.getRuntime().availableProcessors());
		return Response.ok(vm).build();
	}

//	@Path("keystore")
//	public KeystoreHandler getKeystoreHandler() {
//		return new KeystoreHandler();
//	}

	@Path("auth")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAuthSettings(@Context HttpServletRequest req) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		Auth auth = new Auth();
		auth.setEnabled(Boolean.valueOf(sess.getJavaProperty(AuthHttpContext.PROP_DASHBOARD_AUTH)));
		auth.setLocalhost(sess.getJavaProperty(AuthHttpContext.PROP_DASHBOARD_AUTH_LOCALHOST));
		auth.setRemotehost(sess.getJavaProperty(AuthHttpContext.PROP_DASHBOARD_AUTH_REMOTE));
		auth.setLdapurl(sess.getJavaProperty(AuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_URL));
		String val=sess.getJavaProperty(AuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_GROUP);
		if (val == null)
			val = sess.getJavaProperty(AuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_GROUP_OLD);
		auth.setLdapgroup(val);
		return Response.ok(auth).build();
	}
	
	@Path("auth")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void setAuthSettings(@Context HttpServletRequest req, Auth auth) throws Exception {
// The UI currently does not allow this value to be set, so it is always false, which is bad.
//		setJavaProperties(req, AuthHttpContext.PROP_DASHBOARD_AUTH, ""+auth.isEnabled(), true);
		setJavaProperties(req, AuthHttpContext.PROP_DASHBOARD_AUTH_LOCALHOST, ""+auth.getLocalhost(), true);
		setJavaProperties(req, AuthHttpContext.PROP_DASHBOARD_AUTH_REMOTE, ""+auth.getRemotehost(), true);
		setJavaProperties(req, AuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_GROUP, ""+auth.getLdapgroup(), true);
		setJavaProperties(req, AuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_URL, ""+auth.getLdapurl(), true);
		if(req.getSession().getServletContext() instanceof AuthHttpContext) {
			((AuthHttpContext)req.getSession().getServletContext()).refresh(getDashboardProps());
		}
	}
	
	private Map<String, String> getDashboardProps() {
		Map<String, String> ctxConf = new HashMap<String, String>();
		ctxConf.put(LocalApiAuthHttpContext.PROP_AUTH, System.getProperty("dashboard.auth", "true"));
		ctxConf.put(LocalApiAuthHttpContext.PROP_AUTH_REALM, System.getProperty("dashboard.auth.realm",
				"Security Verify Directory Integrator Dashboard"));
		ctxConf.put(LocalApiAuthHttpContext.PROP_ATTACH_SESSION, "true");
		ctxConf.put(AuthHttpContext.PROP_DASHBOARD_AUTH, System.getProperty(AuthHttpContext.PROP_DASHBOARD_AUTH, "true"));
		ctxConf.put(AuthHttpContext.PROP_DASHBOARD_AUTH_LOCALHOST, System.getProperty(AuthHttpContext.PROP_DASHBOARD_AUTH_LOCALHOST, AuthHttpContext.authTypeNone));
		ctxConf.put(AuthHttpContext.PROP_DASHBOARD_AUTH_REMOTE, System.getProperty(AuthHttpContext.PROP_DASHBOARD_AUTH_REMOTE, AuthHttpContext.authTypeNotAllowed));
		ctxConf.put(AuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_URL, System.getProperty(AuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_URL, "ldap://localhost:389"));
		ctxConf.put(AuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_GROUP, System.getProperty(AuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_GROUP, ""));
		
		for(Enumeration<Object> en = System.getProperties().keys(); en.hasMoreElements(); ) {
			String key = en.nextElement().toString();
			if(key.startsWith("dashboard.auth.user.")) {
				ctxConf.put(key, System.getProperty(key));
			}
		}
		return ctxConf;
	}
	
	@Path("schedules")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSchedules(@Context HttpServletRequest req) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		Map<String, List<Map<String,Object>>> schedules = new HashMap<String, List<Map<String,Object>>>();
		for(ConfigInstance ci : sess.getConfigInstances()) {
			schedules.put(ci.getConfigId(), ci.getSchedulersInfo()); 
		}
		return Response.ok(schedules).build();
	}

	/**
	 * This method deletes the configuration along with all tombstones and logfiles for the config.
	 *  
	 * @param req
	 * @param id
	 * @throws Exception
	 */
	@Path("configdata/{id}")
	@DELETE
	public void deleteInstanceData(@Context HttpServletRequest req, @PathParam("id") String id) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		try {
			MetamergeConfig mc = sess.checkOutConfiguration(id);
			for(String str : mc.getDefaultFolder(MetamergeConfig.ASSEMBLYLINE_FOLDER).getNames()) {
				sess.getTombstoneManager().deleteALTombstones(str, id);
				sess.getSystemLog().cleanOldALLogs(id, str, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				sess.undoCheckOut(id);
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
		}
		sess.deleteConfiguration(id);
	}
	
	/**
	 * This method returns the child assemblylines for a given assemblyline 
	 */
	@Path("assemblyline/{id}/children")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChildAssemblylines(@Context HttpServletRequest req, @PathParam("id") int id) throws Exception {
		List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		for(AssemblyLine al : Monitor.runningALs()) {
			if(al.hashCode() == id && al.getALPool() != null) {
				AssemblyLinePool pool = al.getALPool();
				for(AssemblyLine child : pool.getActiveAssemblyLines()) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("id", child.hashCode());
					map.put("parentId", id);
					HashMap<String, Object> stats = new HashMap<String, Object>();
					map.put("stats", stats);
					stats.put("get", child.getStats().get);
					stats.put("start", child.getStats().start);
					if(child.getTombstoneUserMessage() != null)
						stats.put("usermessage", child.getTombstoneUserMessage());
					list.add(map);
				}
			}
		}
		return Response.ok(list).build();
	}

	/**
	 * This method returns the currently active threads 
	 */
	@Path("threads")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getActiveThreads(@Context HttpServletRequest req) throws Exception {
		List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		Thread[] threads = new Thread[Thread.activeCount()];
		Thread.enumerate(threads);
		for(Thread t : threads) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("group", t.getThreadGroup() != null ? t.getThreadGroup().getName() : "");
			map.put("name", t.getName());
			map.put("id", t.getId());
			map.put("state", t.getState().toString());
			list.add(map);
		}
		
		return Response.ok(list).build();
	}
}

