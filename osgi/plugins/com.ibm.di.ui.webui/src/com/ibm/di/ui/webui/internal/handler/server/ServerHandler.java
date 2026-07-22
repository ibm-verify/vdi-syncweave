/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal.handler.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.net.URLEncoder;

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
import javax.ws.rs.core.Response.Status;

import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.security.GetSSLCertificate;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.AssemblyLinePool;
import com.ibm.di.server.Monitor;
import com.ibm.di.server.RS;
import com.ibm.di.ui.webui.bind.Auth;
import com.ibm.di.ui.webui.bind.Logging;
import com.ibm.di.ui.webui.bind.Vmstatus;
import com.ibm.di.ui.webui.internal.AuthHttpContext;
import com.ibm.di.ui.webui.internal.SessionUtils;
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
		auth.setLdapgroup(sess.getJavaProperty(AuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_GROUP));
		auth.setLdapurl(sess.getJavaProperty(AuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_URL));
		return Response.ok(auth).build();
	}
	
	@Path("auth")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void setAuthSettings(@Context HttpServletRequest req, Auth auth) throws Exception {
		setJavaProperties(req, AuthHttpContext.PROP_DASHBOARD_AUTH, ""+auth.isEnabled(), true);
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
				"Tivoli Directory Integrator Dashboard"));
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
	
	@Path("schedules/{config}/{id}")
	@DELETE
	public Response stopSchedule(@Context HttpServletRequest req, @PathParam("config")String config, @PathParam("id")String id) throws Exception {
		RS rs = RS.getServer(config);
		if(rs != null) {
			rs.shutdownScheduler(id);
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok().build();
	}

	@Path("schedules/{config}/{id}")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public Response startSchedule(@Context HttpServletRequest req, @PathParam("config")String config, @PathParam("id")String id) throws Exception {
		RS rs = RS.getServer(config);
		if(rs != null) {
			rs.shutdownScheduler(id);
			int timeout = 10;
			while(rs.getScheduler(id) != null && --timeout > 0) {
				Thread.sleep(1000);
			}
			rs.startScheduler(id);
			return Response.ok(rs.getSchedulerInfo(id)).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
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
	
	@Path("timezone")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTimezone() {
		Hashtable<String, Object> tz = new Hashtable<String, Object>();
		TimeZone t = TimeZone.getDefault();
		tz.put("id", t.getID());
		tz.put("offset", t.getOffset(new Date().getTime()));
		return Response.ok(tz).build();
	}
	
	/**
	 * Returns information about a config instance, its running assemblylines etc.
	 * 
	 * @param req
	 * @param configid
	 * @param command
	 * @return
	 */
	@Path("ci/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response handleConfigRequest(@Context HttpServletRequest req, @PathParam("id")String configid, @QueryParam("command")String command) {
		Hashtable<String, Object> info = new Hashtable<String, Object>();
		info.put("id", URLEncoder.encode(configid));
		try {
			Session sess = SessionUtils.getServerApiSession(req);
			ConfigInstance ci = sess.getConfigInstance(configid);
			
			if(ci != null && "stop".equals(command)) {
				ci.stop();
				info.put("stopped", new Date());
				ci = null;
			} else if(ci == null && "start".equals(command) ) {
				ci = sess.startConfigInstance(configid);
			}
			
			if(ci != null) {
				info.put("started", ci.getInstanceBootTime());
				info.put("schedules", ci.getSchedulersInfo());
				info.put("assemblylines", ci.getAssemblyLineNames());
				List<Object> list = new ArrayList<Object>();
				for(com.ibm.di.api.remote.AssemblyLine al : ci.getAssemblyLines()) {
					Map<String,Object> map = new HashMap<String, Object>();
					String name = al.getName();
					if(name.startsWith("AssemblyLines/"))
						name = name.substring("AssemblyLines/".length());
					map.put("name", name);
					map.put("id", al.getGlobalUniqueID());
					map.put("stats", al.getStatistics().toString());
					list.add(map);
				}
				info.put("active", list);
			}
		} catch (Exception e) {
		}
		
		return Response.ok(info).build();
	}
	
	/**
	 * Terminates an assemblyline
	 * 
	 * @param req
	 * @param configid
	 * @param alid
	 * @return
	 */
	@Path("ci/{id}/{al}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response handleConfigAlRequest(@Context HttpServletRequest req, @PathParam("id")String configid, @PathParam("al")String alid) {
		Hashtable<String, Object> info = new Hashtable<String, Object>();
		info.put("id", URLEncoder.encode(configid));
		info.put("alid", URLEncoder.encode(alid));
		try {
			Session sess = SessionUtils.getServerApiSession(req);
			ConfigInstance ci = sess.getConfigInstance(configid);
			if(ci != null) {
				for(com.ibm.di.api.remote.AssemblyLine al : ci.getAssemblyLines()) {
					String name = al.getName();
					if(name.startsWith("AssemblyLines/"))
						name = name.substring("AssemblyLines/".length());
					
					if(al.getGlobalUniqueID().equals(alid) || name.equals(alid)) {
						al.stop();
						info.put("stopped", new Date());
						return Response.ok(info).build();
					}
				}
			}
		} catch (Exception e) {
		}
		
		return Response.status(Response.Status.NOT_FOUND).build();
	}
	
	/**
	 * 
	 */
	@Path("keystore")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response handleGetKeystoreRequest(@Context HttpServletRequest req) {
		List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		try {
			KeyStore trustStore = loadTrustStore();
			for(Enumeration en = trustStore.aliases(); en.hasMoreElements(); ) {
				map = new HashMap<String, Object>();
				String alias = (String)en.nextElement();
				map.put("alias", alias);
				X509Certificate cert = (X509Certificate)trustStore.getCertificate(alias);
				map.put("type", cert.getType());
				map.put("issuerDN", cert.getIssuerDN());
				map.put("subjectDN", cert.getSubjectDN().toString());
				map.put("serialNumber", cert.getSerialNumber());
				list.add(map);
			}
		} catch (Exception e) {
			map.put("error", e.toString());
			list.add(map);
		}
		return Response.ok(list).build();
	}
	
	/**
	 * 
	 */
	@Path("keystore")
	@PUT
	@Produces(MediaType.TEXT_PLAIN)
	public Response handlePutKeystoreRequest(@Context HttpServletRequest req, @QueryParam("host")String host, @QueryParam("port")int port) {
		String response = GetSSLCertificate.installCertificateFrom("http://"+host, port);
		return Response.ok(response).build();
	}

	/**
	 * 
	 */
	@Path("keystore/{alias}")
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	public Response handleDeleteKeystoreRequest(@Context HttpServletRequest req, @PathParam("alias")String alias) {
		try {
			KeyStore store = loadTrustStore();
			store.deleteEntry(alias);
		} catch(Exception err) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok().build();
	}
	
	private KeyStore loadTrustStore() throws Exception {
		// Load data from truststore
		String type = System.getProperty("javax.net.ssl.trustStoreType");
		if (type == null || type.length() == 0)
			type = KeyStore.getDefaultType();
		String trustStoreFile = System.getProperty("javax.net.ssl.trustStore");
		String pw = System.getProperty("javax.net.ssl.trustStorePassword");
		InputStream in = null;
		KeyStore trustStore = KeyStore.getInstance(type);
		in = new FileInputStream(trustStoreFile);
		trustStore.load(in, pw.toCharArray());
		return trustStore;
	}
}

