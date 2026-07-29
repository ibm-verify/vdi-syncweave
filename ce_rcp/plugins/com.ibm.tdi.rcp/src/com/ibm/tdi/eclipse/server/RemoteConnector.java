/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

import com.ibm.di.config.base.MetamergeConfigImpl;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.entry.Entry;
import com.ibm.tdi.eclipse.Utils;

/**
 * This class is used to access a connector on a server via the server api. A temporary
 * config instance is created with an ass
 *
 */
public class RemoteConnector {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ConnectorConfig cc;
	private RestServerAPI api;
	private String configXml;
	
	public RemoteConnector(String server, ConnectorConfig cc) throws Exception {
		super();
		this.api = new RestServerAPI(server);
		this.cc = (ConnectorConfig) cc.getClone();
	}
	
	private Entry callAPI(String method, String data) throws Exception {
		return api.sendCommand("connector/" + method + "/" + cc.getShortName(), data);
	}

	private String callAPIString(String method, String data) throws Exception {
		return api.sendRequest(api.getAddress() + "/connector/" + method + "/" + cc.getShortName(), data);
	}

	public void initialize() throws Exception {
		MetamergeConfig mc = new MetamergeConfigXML();
		mc.initializeConfig();
		mc.bind("/Connectors/" + cc.getShortName(), cc);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mc.commitChanges(bos);
		//callAPI("load", bos.toString());
		configXml = bos.toString();
	}

	public Entry getNextEntry() throws Exception {
		return callAPI("getnext", configXml);
	}
	
	public ConnectorConfig querySchema() throws Exception {
		String result = callAPIString("query", configXml);
		if(result == null || result.trim().length() == 0 || result.startsWith("<Entry"))
			return null;
		
		Hashtable env = new Hashtable();
		env.put(MetamergeConfigFactory.MC_URL, new ByteArrayInputStream(result.getBytes()));
		MetamergeConfigXML mc = new MetamergeConfigXML(env);
		return mc.getConnector(cc.getShortName());
	}
	
	public MetamergeConfig getSystemNS() throws Exception {
		Entry entry = callAPI("getSystemNS", null);
		String result = entry.getString("System");
		if(result == null)
			return null;
		Hashtable env = new Hashtable();
		env.put(MetamergeConfigFactory.MC_URL, new ByteArrayInputStream(result.getBytes()));
		return new MetamergeConfigImpl(env);
	}
	
	public void terminate() throws Exception {
		callAPI("close", null);
	}
	
}
