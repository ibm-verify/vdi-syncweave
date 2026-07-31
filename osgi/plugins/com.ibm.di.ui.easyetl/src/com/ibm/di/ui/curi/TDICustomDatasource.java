/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.curi;

import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Binding;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.ui.curi.bind.Datasource;
import com.ibm.di.ui.easyetl.internal.SessionUtils;

public class TDICustomDatasource extends TDIDatasource {

	private MetamergeConfig mc;
	
	/**
	 * When executing a DS_* assemblyline we also check for the presence of an DS_*_<oper> assemblyline
	 * to handle a specific request.
	 */
	public final static String[] REST_PATH = {
		"columns",
		"items",
		"itemtyles",
		"relationships",
		"relstyles",
		"links",
		"linkstyles",
		"tasks",
		"parameters",
		"originitems",
		"relateditems"
	};

	public TDICustomDatasource(MetamergeConfig config) {
		this.mc = config;
		createDatasource();
		createDatasets();
	}

	private void createDatasets() {
		/*
		 * If there is a "CURI_Handler" 
		 */
		try {
			AssemblyLineConfig alc = mc.getAssemblyLine("CURI_Handler");
			ConnectorUtils.logdebug("-- Adding TDICustomDataset2 dataset: " + alc.getShortName());
			getDatasets().add(new TDICustomDataset2(this, alc));
			return;
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		
		try {
			Enumeration<Binding> en = mc.list(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER);
			while(en.hasMoreElements()) {
				AssemblyLineConfig alc = (AssemblyLineConfig) en.nextElement().getObject();
				String shortName = alc.getShortName().toLowerCase();
				if(shortName.startsWith("ds_") && !isDSHelperAssemblyline(shortName)) {
					ConnectorUtils.logdebug("-- Adding TDICustomDataset dataset: " + alc.getShortName());
					getDatasets().add(new TDICustomDataset(this, alc));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

//		try {
//			while(en.hasMoreElements()) {
//				ConnectorConfig cc = (ConnectorConfig) en.nextElement().getObject();
//				String shortName = cc.getShortName().toLowerCase();
//				if(shortName.startsWith("ibmdi.")) {
//					if(cc.getConnectionConfig().getParserOption() != RawConnectorConfig.PARSER_REQUIRED)
//						continue;
//				} else if(!shortName.startsWith("ds_")) {
//					continue;
//				}
//				ConnectorUtils.logdebug("-- Adding TDICustomDataset dataset: " + cc.getShortName());
//				getDatasets().add(new TDIConnectorDataset(this, cc));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * Returns true if the name starts with "DS_" and ends with "_<oper>" where <i>oper</i> is a CURI
	 * REST path (e.g. _columns, _linestyles etc).
	 * 
	 * @param shortName
	 * @return
	 */
	private boolean isDSHelperAssemblyline(String shortName) {
		Matcher m = Pattern.compile("ds_(.*)_(.*)", Pattern.CASE_INSENSITIVE).matcher(shortName.toLowerCase());
		if(m.matches()) {
			String ext = m.group(2);
			for(String str : REST_PATH) {
				if(str.equalsIgnoreCase(ext)) {
					ConnectorUtils.logdebug(shortName + " in " + getId() + " is a CURI helper (not published as datasource)");
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getId() {
		String id = mc.getSolutionInterface().getInstanceID();
		try {
			if (id == null || "".equals(id))
				id = mc.getDriverParameter(MetamergeConfigFactory.MC_URL).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	
	@Override
	public String getLabel() {
		if(getId().startsWith("CURI_"))
			return ds.getId().substring("CURI_".length());
		else
			return ds.getId();
	}

	@Override
	protected Datasource createDatasource() {
		ConnectorUtils.logdebug("Adding datasource: " + getId());
		this.ds = new Datasource();
		ds.setDatasourceUri("/providers/TDI/datasources/" + getId());
		ds.setId(getId());
		try {
			ds.setLabel(getLabel());
			ds.setType("any");
			if (mc.getSolutionInterface().getUserComment() != null)
				ds.setDescription(mc.getSolutionInterface().getUserComment());
			else
				ds.setDescription("");
			ds.setUri("/providers/TDI/datasources/" + getId());
			ds.setDatasetsUri("/providers/TDI/datasources/" + getId() + "/datasets");
		} catch (Exception e) {
			this.ds = null;
			e.printStackTrace();
		}

		return this.ds;
	}

	@GET
	@Path("reload")
	@Produces({MediaType.TEXT_PLAIN})
	public void reloadConfiguration(@Context HttpServletRequest req) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		ConfigInstance ci = sess.getConfigInstance(getDatasource().getId());
		boolean didStart = false;
		if(ci == null) {
			ci = sess.startConfigInstance(getId());
			didStart = true;
		}
		ci.reload();
		
		if(didStart)
			ci.stop();
	}
}
