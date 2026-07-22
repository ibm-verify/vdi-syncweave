/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Hashtable;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.local.Session;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.connector.HTTPServerConnector;
import com.ibm.di.eclipse.http.commands.RestCommand;
import com.ibm.di.eclipse.http.commands.ScriptCommand;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.RS;

/**
 * This class provides a REST based interface to IBM Tivoli Directory Integrator's server api as well as a few other
 * services the server api currently does not provide. Validation is performed by the local
 * server api (e.g. APIEngine.getLocalSession()) and any username/passwords passed in the request
 * is forwarded to the local server api.
 * 
 * <p>
 * The HTTPServerConnector is used to handle connections. All commands are delegated to a
 * java class named after the command. A command of "status" will result in attempt to instantiate
 * "com.ibm.di.eclipse.http.commands.StatusCommand". If no such class exists an attempt is made to
 * execute a script 
 * <p>
 * This class is not intended to be used by other clients than the IBM Tivoli Directory Integrator configuration editor. It's
 * functions and services are considered internal use only.
 */
public class ServerAPI implements Runnable {
	
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String PROP_REST_ON = "api.rest.on";
	public final static String PROP_REST_PORT = "api.rest.port";
	public final static String PROP_REST_SSL = "api.rest.ssl";

	public final static int PORT = 1098; 
	
	private static final String HTTP_BASE = "http.base";
	private final static String TNAME = "REST_"; 
	private final static String HTTP_BODY = "http.body";
	private final static String HTTP_CONTENT_TYPE = "http.Content-Type";
	private static final String HTTP_STATUS = "http.status";
	
	private HTTPServerConnector server;
	private HTTPServerConnector conn;
	private Session session;
	private boolean debug = Boolean.getBoolean("com.ibm.di.eclipse.serverapi.debug");
	private Entry request;
	private static Hashtable beans = new Hashtable();
	private static Entry notfound = new Entry();
	private static int logid = 1;
	private int mylogid = logid++;
	private RestCommand restCmd;

	static {
		notfound.setAttribute("http.status", "NOT FOUND");
	}

	/**
	 * Initializes this object with the default port (1098)
	 */
	public ServerAPI() throws Exception {
		this(PORT, false);
	}
		
	/**
	 * Initializes this object with the provided port number.
	 * 
	 * @param port The TCP port number for incoming connections
	 * @throws java.lang.Exception 
	 */
	public ServerAPI(int port, boolean ssl) throws Exception {
		MetamergeConfig mc = new MetamergeConfigXML();
		mc.initializeConfig();
		
		ConnectorConfig cc = (ConnectorConfig) mc.newInstanceOf(MetamergeConfig.CONNECTOR_FOLDER);
		cc.init();
		cc.getConnectionConfig().setJavaClass("com.ibm.di.connector.HTTPServerConnector");
		cc.getConnectionConfig().setParameter(HTTPServerConnector.PARAMETER_TCP_PORT, ""+port);
		cc.getConnectionConfig().setParameter(HTTPServerConnector.PARAMETER_USE_SSL, ""+ssl);
		cc.getConnectionConfig().setParameter(HTTPServerConnector.PARAMETER_HTTP_BASIC_AUTH, "false");
		server = (HTTPServerConnector) SystemFunctions.loadConnector(cc);
		server.initialize(null);
		
	}

	/**
	 * Initializes this object with an established connection.
	 * 
	 * @param conn HTTPServerConnector instance
	 * @throws java.lang.Exception 
	 */
	public ServerAPI(HTTPServerConnector conn) throws Exception {
		this.conn = conn;
		MetamergeConfig mc = new MetamergeConfigXML();
		mc.initializeConfig();
	}
	
	/**
	 * Creates a thread to run this object.
	 * 
	 * @return The thread
	 */
	public Thread start() {
		Thread t = new Thread(this);
		t.setName(TNAME + "Server@" + PORT);
		t.start();
		return t;
	}

	public void run() {
		if(conn != null)
			runSession();
		else
			runServer();
	}
	
	private void runServer() {
		while(true) {
			try {
				if(debug)
					logmsg("Waiting for next client connection");
				conn = (HTTPServerConnector) server.getNextClient();
				Thread t = new Thread(new ServerAPI(conn));
				t.setName(TNAME + "Session");
				t.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void runSession() {
		
		try {
			if(debug)
				logmsg("******* START Connection from " + conn);
			
			if(conn.getUserName() != null)
				session = APIEngine.getLocalSession(conn.getUserName(), conn.getPassword());
			else
				session = APIEngine.getLocalSession();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				conn.terminate();
			} catch (Exception ignore) {}
			return;
		}
		
		while(true) {
			try {
				request = conn.getNextEntry();
				if(request != null) {
					conn.replyEntry(handleRequest(request));
					if(restCmd != null && !restCmd.isReusable()) {
						restCmd.dispose();
						restCmd = null;
					}
				} else {
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		
		if(restCmd != null)
			restCmd.dispose();
		
		if(debug)
			logmsg("******* END Connection from " + conn + " terminated");
		try {
			conn.terminate();
		} catch (Exception e1) {}
		
	}

	public void logmsg(String string) {
		RS.gRS.getLog().loginfo("[" + mylogid + "] " + string);
	}
	
	public void debugMsg(String string) {
		if(debug)
			logmsg(string);
	}

	/**
	 * Parse the request and invoke proper method
	 * @param entry the request entry
	 * @return A response entry
	 */
	private Entry handleRequest(Entry entry) {
		
		if(debug)
			logmsg("Request:\n" + entry.getString(HTTP_BASE));
		
		String path = entry.getString(HTTP_BASE);
		if(path == null || path.trim().length() == 0)
			return notfound;

		if(path.startsWith("/"))
			path = path.substring(1);
		
		String[] str = path.split("/");
		if(str == null || str.length == 0)
			return notfound;

		if(debug) {
			for(int i = 0; i < str.length; i++) {
				logmsg("REST["+i+"] = " + str[i]);
			}
		}
		
		Entry reply = new Entry();
		try {
			// -- Construct RestCommand or pull it from saved beans
			if(restCmd == null) {
				restCmd = (RestCommand)getPendingCommand(path);
				if(restCmd != null) {
					if(debug)
						logmsg("retrieved command: " + restCmd);
				} else if (ScriptCommand.hasHandler(str[0])) {
					restCmd = new ScriptCommand();
				} else {
					String cmd = str[0].toLowerCase();
					cmd = cmd.substring(0,1).toUpperCase() + cmd.substring(1);
					String cls = "com.ibm.di.eclipse.http.commands." + cmd + "Command";
					if(debug)
						logmsg("Create " + cls);
					restCmd = (RestCommand) Class.forName(cls).newInstance();
				}
				restCmd.setApi(this);
			}
			restCmd.setPath(Arrays.asList(str));
			
			// -- Execute command
			Entry rep = null;
			try {
				restCmd.execute();
				rep = restCmd.getResponse();
			} catch (Throwable err) {
				rep = new Entry();
				rep.setAttribute("error", errmsg(err));
				reply.setAttribute(HTTP_STATUS, "401 " + err.toString());
			}

			// -- Create response Entry
			if(debug)
				logmsg("Reply: " + (rep != null ? rep.toDeltaString() : "[null response object]"));
			
			if(rep == null) {
				reply = null;

			} else if (rep.get(HTTP_BODY) != null) {
				reply.setAttribute(rep.getAttribute(HTTP_BODY));
				if(rep.getAttribute(HTTP_CONTENT_TYPE) != null)
					reply.setAttribute(rep.getAttribute(HTTP_CONTENT_TYPE));
				else
					reply.setAttribute(HTTP_CONTENT_TYPE, "text/xml");
				
			} else {
				String sw = XML.toXML(rep);
				if(debug)
					logmsg("Body: " + sw);
				
				reply.setAttribute(HTTP_BODY, sw);
				reply.setAttribute(HTTP_CONTENT_TYPE, "text/xml");
			}
			
		} catch (Throwable err) {
			reply.setAttribute(HTTP_BODY, errmsg(err));
			reply.setAttribute(HTTP_STATUS, "401 " + err.toString());
		}

		return reply;
	}
	
	private String errmsg(Throwable cause) {
		StringWriter sw = new StringWriter();
		cause.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public HTTPServerConnector getConn() {
		return conn;
	}

	public Session getSession() {
		return session;
	}

	public Entry getRequest() {
		return request;
	}

	/**
	 * Adds a command to be retrieved by a subsequent URL request.
	 * @param url The name of the command
	 * @param obj The value for the command
	 */
	public void addPendingCommand(String url, RestCommand obj) {
		if(debug)
			logmsg("addPendingCommand: '" + url + "' -> " + obj);
		
		beans.put(url, obj);
		
		// TODO: add a timer to remove the object after 1 minute
	}
	
	/**
	 * This method returns a pending RestCommand. The object is removed upon retrieval.
	 * 
	 * @param name The name of the command to retrieve and remove.
	 * @return The value for the removed command.
	 */
	public RestCommand getPendingCommand(String name) {
		Object obj = beans.remove(name);
		if(debug)
			logmsg("getPendingCommand: '" + name + "' -> " + obj);
		return (RestCommand) obj;
	}

	/**
	 * Returns true if debug level logging is enabled
	 * @return debug logging on/off
	 */
	public boolean isDebugOn() {
		return debug;
	}
}

