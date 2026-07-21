/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.ConnectorMode;
import com.ibm.di.server.RS;
import com.ibm.di.server.ServerConstants;

public class ConnectorCommand extends RestCommand {

	private ConnectorInterface connectorInstance;
	
	public void execute() throws Exception {
		
		String cmd = getPath(0);
		if(cmd == null)
			throw new Exception(sRes.getString("Sub.command.required"));
		
		String name = getPath(1);
		String body = getRequest().getString(HTTP_BODY);

		if(cmd.equalsIgnoreCase(RES_CONNECTOR_LOAD) || (body != null && body.length() > 0)) {
			if(body == null || body.trim().length() == 0 || name == null) {
				throw new Exception(sRes.getString("Must.post.configuration"));
			}
			
//			if(getApi().isDebugOn())
//				getApi().logmsg("Posted Configuration\n" + body);
//			
			Hashtable<String, Object> env = new Hashtable<String, Object>();
			env.put(MetamergeConfigFactory.MC_URL, body.getBytes());
			MetamergeConfigXML mc = new MetamergeConfigXML(env);
			
			// Load the connector
			ConnectorConfig cc = mc.getConnector(name);
			connectorInstance = SystemFunctions.loadConnector(cc);
			connectorInstance.initialize(new ConnectorMode(ServerConstants.TYPE_ITERATOR));
			try {
				connectorInstance.selectEntries();
			} catch (Exception e) {
				if(getApi().isDebugOn())
					getApi().logmsg("selectEntries: " + e.toString());
			}
			connectorInstance.setLog(RS.gRS.getLog());
			if(cmd.equalsIgnoreCase(RES_CONNECTOR_LOAD))
				return;
		}
		
		if (cmd.equalsIgnoreCase(RES_CONNECTOR_GETNEXT)) {
			Entry e = connectorInstance.getNextEntry();

			// debug message
			if(getApi().isDebugOn()) {
				getApi().logmsg("ConnectorCommand.getnext: " + (e == null ? "null" : e.toDeltaString()));
			}
			
			if(e != null)
				getResponse().setAttribute("NextEntry", e);
			else
				getRequest().setAttribute("NextEntry", "EOD");
			
		} else if (cmd.equalsIgnoreCase(RES_CONNECTOR_QUERYSCHEMA)) {
			querySchema();
			
		} else if (cmd.equalsIgnoreCase(RES_CONNECTOR_CLOSE)) {
			connectorInstance.terminate();
			connectorInstance = null;
		} else {
			throw new Exception(sRes.getString("Unknown.sub.command", cmd));
		}
	}

	public boolean isReusable() {
		return true;
	}

	public void dispose() {
		if(connectorInstance != null) {
			try {
				connectorInstance.terminate();
				connectorInstance = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.dispose();
	}
	
	public void querySchema() throws Exception {
		Object result = connectorInstance.querySchema(null);
		if(getApi().isDebugOn())
			getApi().logmsg("QuerySchema: " + result);
		
		if(result instanceof Vector)
			getResponse().setAttribute("Schema", buildSchema((Vector) result));
	}

	public String buildSchema(Vector v) throws Exception {
		ConnectorConfig config = (ConnectorConfig) connectorInstance.getConfiguration();
		boolean input = true;
		for (int i = 0; i < v.size(); i++) {
			String str;
			Entry e = (Entry) v.elementAt(i);
			SchemaItemConfig csi = config.getSchema(input).getItem(
					e.getString("name"));
			if (csi == null)
				csi = config.getSchema(input).newItem(
						e.getString("name"));
			if ((str = e.getString("syntax")) != null)
				csi.setExternalSyntax(str);
		}
		
		// Replace connector config with updated schema
		MetamergeConfigXML mc = new MetamergeConfigXML();
		mc.initializeConfig();
		mc.bind("/Connectors/" + getPath(1), config);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mc.commitChanges(bos);
		return bos.toString();
		
	}
}

