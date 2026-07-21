/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal.handler.ldapsync;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ibm.di.api.remote.AssemblyLineHandler;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.server.TaskCallBlock;
import com.ibm.di.server.TaskStatistics;
import com.ibm.di.ui.webui.internal.SessionUtils;

@Path(LDAPSync.URL)
public class LDAPSync {
	
	public final static String URL = "ldapsync";
	
	public final static String LDAPSYNC_DIR = "LDAPSync";
	public final static String LDAP_SYNC_CONFIGID = "LDAPSync";
	public final static String LDAP_SYNC_ID = "LDAPSync/LDAPSync.xml";
	public final static String LDAP_SYNC_PATH = "LDAPSync/LDAPSync.xml";
	public final static String ISAM_PLUGIN_PATH = "LDAPSync/FDS_ISAM_Plugin.xml";
	public final static String LDAPSYNC_LOGPATH = "LDAPSync/logs";

	
	private void installFDS(Session sess, HashMap<String, Object> status) throws Exception {
		File source = new File(System.getProperty("com.ibm.di.installdir") + "/LDAPSync");
		File target = new File(LDAPSYNC_DIR);
		UserFunctions uf = new UserFunctions();
		boolean recursive = false;
		boolean overwrite = !target.exists(); // copy missing files only when target exists

		uf.copyDirectory(source.getAbsolutePath(), target.getAbsolutePath(), recursive, overwrite, null);
		status.put("fds", "created files in " + target.getAbsolutePath());
		
		installConfig(sess, status, LDAP_SYNC_PATH, "configs/LDAPSync.xml");
		installConfig(sess, status, ISAM_PLUGIN_PATH, "configs/FDS_ISAM_Plugin.xml");
	}
	
	private void installConfig(Session sess, HashMap<String,Object> status, String source, String target) throws Exception {
		File fds = new File(target);
		if(!fds.exists()) {
			MetamergeConfig mc = MetamergeConfigFactory.getFileInstance(source);
			String id = mc.getSolutionInterface().getInstanceID();
			String filename = id + ".xml";
			sess.createNewConfiguration(filename, false);
			sess.checkInConfiguration(mc, filename);
			status.put("fdsconfig", "created " + fds.getAbsolutePath());
			// make sure the config is available
			try {
				sess.checkOutConfiguration(id);
				sess.undoCheckOut(id);
			} catch (Exception e) {
				System.out.println("Exception:"+ e.toString());
				System.err.println("Exception:"+ e.toString());
				//status.put("error", e.toString());
			}
			
			// We need to load the config for the server api to detect that
			// it has the identifier installed.
			sess.startConfigInstance(filename);
		}
	}

	@GET
	public Response getStatus(@Context HttpServletRequest req) throws Exception {
		HashMap<String, Object> status = new HashMap<String, Object>();
		Session sess = SessionUtils.getServerApiSession(req);
		
		try {
			installFDS(sess, status);
		} catch (Exception e) {
			System.out.println("Exception:"+ e.toString());
			System.err.println("Exception:"+ e.toString());
			//status.put("error", e.toString());
		}
		
		status.put("active", false);
		if(sess.getConfigInstance(LDAP_SYNC_ID) != null) {
			status.put("active", true);
		}
		
		return Response.ok(status).build();
	}
	
	@GET
	@Path("reloadconfig")
	@Produces(MediaType.TEXT_PLAIN)
	public String reloadConfig(@Context HttpServletRequest req, @QueryParam("id") String id) throws Exception {
		try {
			Session sess = SessionUtils.getServerApiSession(req);
			ConfigInstance ci = sess.getConfigInstance(id);
			id=encodeHTML(id);
			if(ci != null) {
				ci.reload();
			}
		} catch (Exception e) {
			System.out.println("error:"+ e.toString());
			System.err.println("error:"+ e.toString());			
			return e.toString();
		}
		return "OK";
	}
	
	public static String encodeHTML(String s) {
       StringBuffer out = new StringBuffer();
       for(int i=0; i<s.length(); i++)
       {
           char c = s.charAt(i);
           if( c=='<' ){
              out.append("&lt;");
           }else if( c=='>' ){
              out.append("&gt;");
           }else if( c=='"' ){
              out.append("&quot;");
           }else if( c=='&' ){
              out.append("&amp;");
           }else if( c > 127 ){
              out.append("&#"+(int)c+";");
           }
           else
           {
               out.append(c);
           }
       }
	   return out.toString();
   }

	@GET
	@Path("createconfig")
	@Produces(MediaType.TEXT_PLAIN)
	public String createConfig(@Context HttpServletRequest req, @QueryParam("name") String name) throws Exception {
		try {
			name=encodeHTML(name);
			Session sess = SessionUtils.getServerApiSession(req);
			MetamergeConfig mx = sess.createNewConfiguration(name + ".xml", false);
			mx.getSolutionInterface().setInstanceID(name);
			sess.checkInConfiguration(mx, name + ".xml");
		} catch (Exception e) {
			System.out.println("error:"+ e.toString());
			System.err.println("error:"+ e.toString());
			return e.toString();
		}
		return "OK";
	}
	
	@GET
	@Path("start")
	public Response startLDAPSync(@Context HttpServletRequest req) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		if(sess.getConfigInstance(LDAP_SYNC_ID) != null) {
			return Response.ok().build();
		}
		
		sess.startConfigInstance(LDAP_SYNC_PATH, true, null);
		return Response.ok().build();
	}
	
	@GET
	@Path("summary/{flow}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getFlowSummary(@Context HttpServletRequest req, @PathParam("flow") String flow, @QueryParam("logpath")String path) throws Exception {
		path=encodeHTML(path);
		flow=encodeHTML(flow);
		
		if(path == null || path.length() == 0)
			path = LDAPSYNC_LOGPATH;
		
		StringBuffer buf = new StringBuffer();
		String str = getSummaryFor(path + "/" + flow);
		if(str != null) {
			buf.append(str);
		}
		return Response.ok(buf.toString()).build();
	}
	
	private String getSummaryFor(String file) throws Exception {
		file=encodeHTML(file);
		File f = new File(file);
		StringBuffer summary = null;
		if(f.exists()) {
			BufferedReader inp = new BufferedReader(new FileReader(f));
			String str;
			while((str = inp.readLine()) != null) {
				if(str.indexOf("-o@o-o@o-o@o-o@o-o@o-o@o-o@o-") != -1) {
					summary = new StringBuffer();
					summary.append(str + "\n");
				} else if(summary != null && str.startsWith("=========")) {
					summary.append(str + "\n");
					break;
				} else if(summary != null) {
					summary.append(str + "\n");
				}
			}
			inp.close();
		}
		return summary != null ? summary.toString() : null;
	}
	
	@GET
	@Path("log")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogs(@Context HttpServletRequest req, @QueryParam("logpath")String dir, @QueryParam("detailed")boolean detailed) throws Exception {
		dir=encodeHTML(dir);
		String path = dir;
		if(dir == null || dir.length() == 0)
			path = LDAPSYNC_LOGPATH;
		List<String> list = new ArrayList<String>();
		List<Map<String,Object>> details = new ArrayList<Map<String,Object>>();
		try {
			for(File file : new File(path).listFiles()) {
				if(detailed) {
					Map<String,Object> map = new HashMap<String, Object>();
					map.put("name", file.getName());
					map.put("size", file.length());
					map.put("modified", file.lastModified());
					details.add(map);
				} else {
					list.add(file.getName());
				}
			}
		} catch(Exception err) {
			SystemFunctions.doNothing();
		}
		if(detailed)
			return Response.ok(details).build();
		else
			return Response.ok(list).build();
	}
	
	@GET
	@Path("log/{file:.*}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getLogFile(@Context HttpServletRequest req, @PathParam("file") String file) throws Exception {
		file=encodeHTML(file);
		File f = new File(file);
		if(f.exists()) {
			byte[] bytes = new byte[(int) f.length()];
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
				fis.read(bytes);
			} finally {
				if (fis != null)
					fis.close();
			}
			return Response.ok(new String(bytes)).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}
	
	
	@POST
	@Path("listpta")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ptaListAll(@Context HttpServletRequest req, HashMap<String, Object> params) throws Exception {
		TaskCallBlock tcb = new TaskCallBlock();
		tcb.setAssemblyLineName("PTA_Servers");
		Entry e = new Entry();
		for (Map.Entry<String,Object> param: params.entrySet()) {
			e.setAttribute(param.getKey(), param.getValue());
		}
		tcb.setOperationInitParams(e);
		return executeAL(req, tcb, LDAP_SYNC_CONFIGID, false);
	}
	
	@GET
	@Path("pta/{suffix}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ptaEntry(@Context HttpServletRequest req, @PathParam("suffix") String suffix) throws Exception {
		TaskCallBlock tcb = new TaskCallBlock();
		tcb.setAssemblyLineName("PTA_ServerEntry");
		Entry e = new Entry();
		e.setAttribute("ibm-slapdptasubtree", suffix);
		tcb.setInitialWorkEntry(e);
		return executeAL(req, tcb, LDAP_SYNC_CONFIGID, true);
	}
	
	@POST
	@Path("pta")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response ptaList(@Context HttpServletRequest req, HashMap<String, Object> params) throws Exception {
		TaskCallBlock tcb = new TaskCallBlock();
		tcb.setAssemblyLineName("PTA_ServerEntry");
		Entry e = new Entry();
		for (Map.Entry<String,Object> param: params.entrySet()) {
			e.setAttribute(param.getKey(), param.getValue());
		}
		tcb.setInitialWorkEntry(e);
		tcb.getOperationInitParams().merge(e);
		
		return executeAL(req, tcb, LDAP_SYNC_CONFIGID, true);
	}
	
	@POST
	@Path("maps")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateMaps(@Context HttpServletRequest req, HashMap<String, Object> maps) throws Exception {
		Object omap = maps.get("map");
		Object name = maps.get("name");
		if(omap instanceof Map && name instanceof String) {
			HashMap<String,Object> map = (HashMap<String, Object>) omap;
			StringBuffer buf = new StringBuffer();
			for(Iterator<String> iter = map.keySet().iterator(); iter.hasNext(); ) {
				String key = iter.next();
				HashMap<String, Object> item = (HashMap<String, Object>) map.get(key);
				
				buf.append(item.get("name"));
				boolean add = false;
				if(item.get("add") instanceof Boolean)
					add = ((Boolean)item.get("add"));
				boolean mod = false;
				if(item.get("mod") instanceof Boolean)
					mod = ((Boolean)item.get("mod"));
				boolean enabled = true;
				if(item.get("enabled") instanceof Boolean)
					enabled = ((Boolean)item.get("enabled"));
				
				if( (add && !mod) || (mod && !add) || (!enabled)) {
					buf.append("{");
					if(add)
						buf.append("A");
					if(mod)
						buf.append("M");
					if(!enabled)
						buf.append("!");
					buf.append("}");
				}
				
				buf.append("=");
				String str = (String) item.get("script");
				if(str != null) {
					if(str.indexOf("\n") != -1) {
						buf.append("[\n");
					}
					buf.append(str);
					if(str.indexOf("\n") != -1) {
						buf.append("\n]");
					}
				}
				buf.append("\n");	
			}
			
			File file = new File(LDAPSYNC_DIR + "/" + name);

			if (!file.getCanonicalPath().equals(file.getAbsolutePath())) {
				return Response.status(Status.FORBIDDEN).build();
			}

			File backup = new File(LDAPSYNC_DIR + "/" + name + ".old");
			
			// Delete backup file before renaming
			if(file.exists() && backup.exists()) {
				if(!backup.delete()) {
					SystemFunctions.doNothing();
				}
			}
			
			if(file.exists() && ! file.renameTo(backup)) {
				// TODO: Give a warning that rename failed?
				SystemFunctions.doNothing();
			}
			
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				fos.write(buf.toString().getBytes());
			} finally {
				if (fos != null)
					fos.close();
			}
		}
		return Response.ok().build();
	}
	
	@DELETE
	@Path("maps/{file}")
	public Response deleteMap(@Context HttpServletRequest req, @PathParam("file") String file) throws Exception {
		file=encodeHTML(file);
		File f = new File(LDAPSYNC_DIR + "/" + file);
		
		if(!f.getName().endsWith(".map"))
			return Response.status(Status.NOT_ACCEPTABLE).build();
		
		if(!f.exists())
			return Response.status(Status.NOT_FOUND).build();
		else if(!f.delete())
			return Response.status(Status.NOT_ACCEPTABLE).build();
		else
			return Response.ok().build();
	}
	
	@GET
	@Path("maps")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listMaps(@Context HttpServletRequest req) throws Exception {
		File dir = new File(LDAPSYNC_DIR);
		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".map");
			}
		});
		ArrayList<Object> list = new ArrayList<Object>();
		for(File f : files) {
			HashMap<String, Object> map = getMap(f);
			list.add(map);
		}
		return Response.ok(list).build();
	}
	
	@GET
	@Path("maps/{file}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listMaps(@Context HttpServletRequest req, @PathParam("file") String file) throws Exception {
		file=encodeHTML(file);
		File dir = new File(LDAPSYNC_DIR);
		HashMap<String,Object> map = getMap(new File(dir, file));
		return Response.ok(map).build();
	}
	
	public HashMap<String,Object> getMap(File file) throws Exception {
		HashMap<String, Object> result = new HashMap<String, Object>();
		HashMap<String, Object> maps = new HashMap<String, Object>();
		result.put("map", maps);
		result.put("name", file.getName());
		result.put("modified", file.lastModified());
		BufferedReader inp = null;
		try {
			// -- Read in file
			inp = new BufferedReader(new FileReader(file));
			String str;
			while ((str = inp.readLine()) != null) {
				int index = str.indexOf("=");
				if (index == -1)
					continue;

				// attribute=value
				String attr = str.substring(0, index).trim();
				String value = str.substring(index + 1).trim();

				// attribute= [
				// .... script ....
				// ]
				if (value.equals("[")) {
					StringBuffer buf = new StringBuffer();
					while ((str = inp.readLine()) != null) {
						if (str.trim().equals("]")) {
							break;
						}
						buf.append(str + "\n");
					}
					value = buf.toString();
				}

				// Curly braces are used for flags.
				boolean mod = false;
				boolean add = false;
				boolean subst = false;
				boolean disabled = false;
				if (attr.contains("{")) {
					index = attr.indexOf('{');
					String flags = attr.substring(index+1).toUpperCase();
					attr = attr.substring(0, index);
					subst = flags.contains("S");
					mod = flags.contains("M");
					add = flags.contains("A");
					disabled = flags.contains("!");
				}
				
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("name", attr);
				if (value.length() == 0) {
					map.put("simple", attr);
				} else if (subst) {
					map.put("subst", value);
				} else {
					map.put("script", value);
				}

				map.put("add", add);
				map.put("mod", mod);
				map.put("enabled", !disabled);
				maps.put(attr, map);
			}
		} finally {
			if (inp != null) {
				inp.close();
			}
		}
		return result;
	}
	

	@POST
	@Path("runal/{config}/{assemblyline}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runAssemblyLine(@Context HttpServletRequest req, @PathParam("config")String config, @PathParam("assemblyline")String assemblyline, HashMap<String, Object> params) throws Exception {
		int start = params.containsKey("start") ? Integer.parseInt(params.get("start").toString()) : 0;
		int count = params.containsKey("count") ? Integer.parseInt(params.get("count").toString()) : 1;
		
		TaskCallBlock tcb = new TaskCallBlock();
		tcb.setAssemblyLineName(assemblyline);
		
		Object initParams = params.get("initParams");
		if(initParams instanceof Map) {
			tcb.setOperationInitParams(toEntry((Map<String, Object>) initParams, null));
		}
		
		Object iwe = params.get("iwe");
		if(iwe instanceof Map) {
			tcb.setInitialWorkEntry(toEntry((Map<String, Object>) iwe, "$operation"));
			
			if(params.get("$operation") instanceof String) {
				tcb.getInitialWorkEntry().setOperation((String) params.get("$operation"));
			}
		}
		
		return executeAL(req, tcb, config, start, count);
	}	
	
	@GET
	@Path("writeback/items")
	@Produces(MediaType.APPLICATION_JSON)
	public String wbGetItems(@Context HttpServletRequest req, @QueryParam("logpath")String path, @QueryParam("count")int count, @QueryParam("config")String config) throws Exception {
		path=encodeHTML(path);
		config=encodeHTML(config);
		if(path == null || path.length() == 0)
			path = LDAPSYNC_LOGPATH;
		
		if(count == 0)
			count = 100;

		String signature = "- INFO - JSON: ";
		ArrayList<String> json = new ArrayList<String>();
		File log = new File(path + "/WritebackHistory.log");
		if(log.exists()) {
			BufferedReader inp = new BufferedReader(new FileReader(log));
			String str = null;
			while( (str = inp.readLine()) != null) {
				int index = str.indexOf(signature);
				if(index != -1) {
					String jsonStr = str.substring(index + signature.length());
					if(config == null || jsonStr.indexOf("\"config\":\"" + config + "\"") != -1) {
						if(json.size() > count)
							json.remove(0);
						json.add(jsonStr);
					}
				}
			}
		}
		StringBuffer result = new StringBuffer();
		result.append("[\n");
		for(int i = 0; i < json.size(); i++) {
			if(i > 0)
				result.append(",\n");
			result.append(json.get(i));
		}
		result.append("\n]");
		return result.toString();
	}
	
	@PUT
	@Path("snapshot/{config}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ssCreateSnapshot(@Context HttpServletRequest req, @PathParam("config")String config, @QueryParam("title")String title) throws Exception {
		title=encodeHTML(title);
		config=encodeHTML(config);
		File snapDir = getSnapshotDirectory();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		SimpleDateFormat sdfinfo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timestamp = sdf.format(new Date());
		String tsinfo = sdfinfo.format(new Date());
		String snapname = config + ".xml." + timestamp; 
		String infoname = config + ".info." + timestamp; 
		File snapshot = new File(snapDir, snapname);
		
		String info = title;
		if(info == null || info.length() == 0) {
			info = tsinfo;
		} else {
			info = info + " (" + tsinfo + ")";
		}
		
		BufferedReader inp = new BufferedReader(new FileReader("configs/" + config + ".xml"));
		BufferedWriter out = new BufferedWriter(new FileWriter(snapshot));
		String str;
		while( (str = inp.readLine()) != null) {
			out.write(str);
			out.newLine();
		}
		inp.close();
		out.close();
		
		out = new BufferedWriter(new FileWriter(new File(snapDir,infoname)));
		out.write(info);
		out.close();
		
		return Response.ok(getSnapshotFile(snapshot)).build();
	}
	
	@GET
	@Path("snapshot/{config}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ssGetSnapshots(@Context HttpServletRequest req, @PathParam("config")String config) throws Exception {
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		File snapDir = getSnapshotDirectory();
		String match = config + ".xml.";
		for(File f : snapDir.listFiles()) {
			if(f.getName().startsWith(match)) {
				list.add(getSnapshotFile(f));
			}
		}
		return Response.ok(list).build();
	}
	
	@GET
	@Path("snapshot/{config}/restore")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ssGetSnapshot(@Context HttpServletRequest req, @PathParam("config")String config, @QueryParam("path")String path) throws Exception {
		path=encodeHTML(path);
		Session sess = SessionUtils.getServerApiSession(req);
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(MetamergeConfigFactory.MC_URL, getSnapshotDirectory().getAbsolutePath() + "/" + path);
		env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
		MetamergeConfig mc = MetamergeConfigFactory.getInstance(env);
		sess.checkOutConfiguration(config + ".xml");
		sess.checkInConfiguration(mc, config + ".xml");
		return Response.ok().build();
	}
	
	@DELETE
	@Path("snapshot")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ssDeleteSnapshot(@Context HttpServletRequest req, @QueryParam("path")String file) throws Exception {
		file=encodeHTML(file);
		//new File(getSnapshotDirectory(), file).delete();
		//fix for pen test issue - DELETE snapshot request can delete any file on system
		if ( !(file.contains("..")) ){ //user has mentioned direct path
			File xmlFile = new File(getSnapshotDirectory(), file);
				if(xmlFile.exists()) {
				System.out.println("DELETE:xmlFile="+xmlFile);
				xmlFile.delete();
			}
		
			File info = new File(getSnapshotDirectory(), file.replace(".xml.", ".info."));
			if(info.exists()) {
				System.out.println("DELETE:infoFile="+info);
				info.delete();
			}
			return Response.ok().build();
		}
		else { //user has mentioned relative pathe - app should not delete such file. 
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
	}
	
	private Map<String,Object> getSnapshotFile(File file) throws IOException {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("config", file.getName());
		map.put("modified", file.lastModified());
		map.put("size", file.length());
		
		String infopath = file.getName();
		infopath = infopath.replace(".xml.", ".info.");
		File info = new File(file.getParent(), infopath);
		if(info.exists()) {
			BufferedReader inp = new BufferedReader(new FileReader(info));
			map.put("description", inp.readLine());
			inp.close();
		}
		
		return map;
	}
	
	private File getSnapshotDirectory() throws Exception {
		File snapDir = new File("snapshots");
		if(!snapDir.exists()) {
			if(!snapDir.mkdirs()) {
				throw new Exception("Unable to create 'snapshots' directory in solution directory");
			}
		}
		return snapDir;
	}

	public Response executeAL(@Context HttpServletRequest req, TaskCallBlock tcb, String config, boolean singleCycle) throws Exception {
		return executeAL(req, tcb, config, 0, singleCycle ? 1 : -1);
	}
	
	public Response executeAL(@Context HttpServletRequest req, TaskCallBlock tcb, String config, int start, int count) throws Exception {
		List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		Session sess = SessionUtils.getServerApiSession(req);
		ConfigInstance ci = sess.getConfigInstance(config);
		
		if(ci == null) {
			// Add timeout protection for startConfigInstance to prevent indefinite hanging
			// This addresses issues with JMS/ActiveMQ blocking and slow LDAP connections
			ExecutorService executor = null;
			try {
				final Session finalSession = sess;
				final String finalConfig = config;
				
				executor = Executors.newSingleThreadExecutor();
				Future<ConfigInstance> future = executor.submit(new Callable<ConfigInstance>() {
					public ConfigInstance call() throws Exception {
						return finalSession.startConfigInstance(finalConfig, true, null);
					}
				});
				
				try {
					// Wait up to 30 seconds for the config instance to start
					// (longer than ConfigurationRegistry's 15s due to LDAP connection overhead)
					ci = future.get(30, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					future.cancel(true);
					Entry result = new Entry();
					result.setAttribute("status", "fail");
					result.setAttribute("exception", "TimeoutException");
					result.setAttribute("message", "Test connection timed out after 30 seconds. " +
						"Please verify LDAP server connectivity and configuration.");
					return Response.serverError().entity(toHashMap(result)).build();
				} catch (ExecutionException e) {
					Throwable cause = e.getCause();
					Entry result = new Entry();
					result.setAttribute("status", "fail");
					result.setAttribute("exception", cause != null ? cause.toString() : e.toString());
					result.setAttribute("message", cause != null ? cause.getLocalizedMessage() : e.getLocalizedMessage());
					return Response.serverError().entity(toHashMap(result)).build();
				}
			} finally {
				if (executor != null) {
					executor.shutdownNow();
				}
			}
		}

		AssemblyLineHandler al = null;
		Entry iwe = tcb.getInitialWorkEntry();
		if(iwe != null && iwe.size() == 0)
			iwe = null;
		
		int remaining = count != -1 ? count : Integer.MAX_VALUE;
		int current = 0;
		
		try {
			al = ci.startAssemblyLineManual(tcb.getAssemblyLineName(), tcb);
			Entry e = null;
			do {
				if(iwe != null)
					e = al.executeCycle(iwe);
				else
					e = al.executeCycle();
				
				if(e != null && e.size() > 0) {
					if(current >= start) {
						if(remaining-- > 0)
							list.add(toHashMap(e));
					}
					current++;
				}
				
			} while(e != null && e.size() > 0 && remaining > 0);
			
		} catch(Exception err) {
			Throwable error = null;
			if(al != null && al.getAssemblyLine() != null) {
				TaskStatistics stats = al.getAssemblyLine().getStatistics();
				if(stats != null) {
					if(stats.getError() != null) {
						error = stats.getError();
					}
				}
			}
			
			// server api puts itself on top of the error 
			if(error == null) {
				if(err.getCause() != null)
					error = err.getCause();
				else
					error = err;
			}

			Entry result = new Entry();
			result = new Entry();
			result.setAttribute("status", "fail");
			result.setAttribute("exception", error.toString());
			result.setAttribute("message", error.getLocalizedMessage());
			return Response.serverError().entity(toHashMap(result)).build();
			
		} finally {
			try {
				al.close();
			} catch (Exception e2) {
			}
		}
		return Response.ok(list).build();
	}
	
	private Entry toEntry(Map<String,Object> map, String operAttr) {
		Entry entry = new Entry();
		for (Map.Entry<String,Object> param: map.entrySet()) {
			Object value = param.getValue();
			if(param.getKey().equals(operAttr)) {
				entry.setOperation(value.toString());
				
			} else if(value instanceof List<?>) {
				List<Object> list = (List<Object>) value;
				for(int i = 0; i < list.size(); i++) {
					entry.addAttributeValue(param.getKey(), list.get(i));
				}
				
			} else {
				entry.setAttribute(param.getKey(), param.getValue());
			}
		}
		return entry;
	}
	
	private HashMap<String,Object> toHashMap(Entry e) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		for(String str : e.getAttributeNames()) {
			Attribute attr = e.getAttribute(str);
			if(attr.size() == 1) {
				data.put(str, toJsonObject(e.getObject(str)));
			} else if(attr.size() > 0) {
				ArrayList<Object> values = new ArrayList<Object>();
				for(int i = 0; i < attr.size(); i++) {
					values.add(toJsonObject(attr.getValue(i)));
				}
				data.put(str, values);
			}
		}
		return data;
	}
	
	private Object toJsonObject(Object data) {
		if(data instanceof Entry) {
			return toHashMap((Entry)data);
		} else if(data instanceof byte[]) {
			return UserFunctions.base64Encode((byte[])data);
		} else {
			return data != null ? data.toString() : null;
		}
	}
}
