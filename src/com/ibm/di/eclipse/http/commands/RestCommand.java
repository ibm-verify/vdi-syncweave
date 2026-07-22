/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.di.api.local.Session;
import com.ibm.di.connector.HTTPServerConnector;
import com.ibm.di.eclipse.http.ServerAPI;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

public abstract class RestCommand {

	public static final String RES_CONFIGURATION = "Configuration";
	public static final String RES_ASSEMBLY_LINE_LOG = "AssemblyLineLog";
	public static final String RES_ASSEMBLY_LINE = "AssemblyLine";
	public static final String RES_CONFIG_INSTANCE = "ConfigInstance";
	public static final String RES_CONNECTOR_LOAD = "load";
	public static final String RES_CONNECTOR_GETNEXT = "getnext";
	public static final String RES_CONNECTOR_QUERYSCHEMA = "query";
	public static final String RES_CONNECTOR_CLOSE = "close";
	public static final String RES_CONFIG_RUNAL = "ActiveAssemblyLine";
	public static final String RES_CONFIG_AL = "AssembyLineConfig";
	public final static String HTTP_BODY = "http.body";
	public final static String HTTP_QS = "http.qs";
	public final static String HTTP_CONTENT_TYPE = "http.Content-Type";

	private Entry response;
	private List path;
	private String command;
	private ServerAPI api;

	/**
	 * For error messages and Exceptions
	 */
	public static ResourceHash sRes = ResourceHash.getHash("miserver");

	public RestCommand() {}

	public void setApi(ServerAPI api) {
		this.api = api;
	}

	public ServerAPI getApi() {
		return api;
	}

	public Session getSession() {
		return getApi().getSession();
	}

	public Entry getResponse() {
		if(response == null)
			response = new Entry();
		return response;
	}

	public void setResponse(Entry response) {
		this.response = response;
	}

	public Entry getRequest() {
		return getApi().getRequest();
	}

	public void setPath(List path) {
		this.path = new ArrayList();
		this.path.addAll(path);
		this.command = (String) this.path.remove(0);
	}
	
	public List getPath() {
		return path;
	}
	
	public String getPath(int index) {
		if(index < path.size())
			return (String) path.get(index);
		else
			return null;
	}
	
	public String getCommand() {
		return command;
	}

	protected Attribute appendResult(String attr, String value) {
		getResponse().addAttributeValue(attr, value);
		return getResponse().getAttribute(attr);
	}
	
	public abstract void execute() throws Exception;

	public HTTPServerConnector getConnector() {
		return getApi().getConn();
	}

	public void addPendingCommand(String name, RestCommand obj) {
		getApi().addPendingCommand(name, obj);
	}
	
	public RestCommand getPendingCommand(String name) {
		return getApi().getPendingCommand(name);
	}
	
	public String getRequestParam(String name) {
		return getRequest().getString(HTTP_QS + "." + name);
	}
	
	public boolean isParamTrue(String name) {
		return Boolean.valueOf(getRequestParam(name)).booleanValue();
	}
	
	public String getRequestBody() {
		return getRequest().getString(HTTP_BODY);
	}
	
	public void appendBody(String body) {
		setBody(body, true);
	}
	
	public void setBody(String body) {
		setBody(body, false);
	}
	
	public void setBody(String body, boolean append) {
		String ebody = getResponse().getString(HTTP_BODY);
		if(ebody == null)
			ebody = "";
		getResponse().setAttribute(HTTP_BODY, (append ? ebody+body : body));
	}

	public String readFile(File file) throws IOException {
		FileReader reader = null;
		BufferedReader inp = null;
		try {
			reader = new FileReader(file);
			inp = new BufferedReader(reader);
			StringBuffer buf = new StringBuffer();
			String str;
			while ((str = inp.readLine()) != null) {
				buf.append(str);
				buf.append("\n");
			}
			return buf.toString();
		} finally {
			if (inp != null) {
				inp.close();
			}
			if(reader != null) {
				reader.close();
			}
		}
	}

	public boolean isReusable() {
		return false;
	}

	public void dispose() {
	}
	
}
