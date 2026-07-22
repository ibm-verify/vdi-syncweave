/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.curi;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.naming.Binding;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.ui.curi.bind.Datasets;
import com.ibm.di.ui.curi.bind.Datasource;

public class TDIDatasource {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	
	private final static String CLIENT_REF_ID = "param_refId";
	
	protected Datasource ds;

	private List<TDIDataset> datasets = new ArrayList<TDIDataset>();
	
	private HashMap<String, TDIDataset> datasetInstances = new HashMap<String, TDIDataset>();

	private MetamergeConfig mc;

	private String configPath;

	public TDIDatasource() {
	}
	
	public TDIDatasource(String configPath) throws Exception {
		this.configPath = configPath;
		this.mc = MetamergeConfigFactory.loadNamespace(configPath);
		this.createDatasource();
		try {
			createDatasets(MetamergeConfigFactory.SYSTEM_NAMESPACE.equals(getId()));
		} catch (Exception e) {
			ConnectorUtils.logerror("While creating datasets for: " + configPath, e);
		}
	}
	
	private void createDatasets(boolean isSystem) throws Exception {
		for (Enumeration<Binding> e = mc.list(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER); e.hasMoreElements();) {
			ConnectorConfig cc = (ConnectorConfig) e.nextElement().getObject();
			if( isSystem && cc.getConnectionConfig().getParserOption() != RawConnectorConfig.PARSER_USELESS) {
				ConnectorUtils.logdebug("Skipping " + cc.getName() + " from " + getId() + ": requires parser");
			} else {
				// only include Iterator mode library connectors in custom configs with a DS_ prefix
				if(!isSystem && !ConnectorConfig.ITERATOR_MODE.equals(cc.getMode()) && !cc.getShortName().startsWith("DS_")) {
					continue;
				}
				TDIConnectorDataset ds = new TDIConnectorDataset(this, cc);
				if (ds.getDatasource() != null) {
					datasets.add(ds);
				}
			}
		}
	}

	public HashMap<String, TDIDataset> getDatasetInstances() {
		return this.datasetInstances;
	}
	
	public String getId() {
		String id = null;
		if(this.mc.getSolutionInterface() != null)
			id = this.mc.getSolutionInterface().getInstanceID();
		if(id == null)
			id = this.configPath;
		
		return id;
	}
	
	public String getLabel() {
		String label = this.ds.getLabel();
		if(label == null)
			label = getId();
		return label;
	}
	
	public Datasource getDatasource() {
		return this.ds;
	}
	
	protected Datasource createDatasource() {
		this.ds = new Datasource();
		ds.setDatasourceUri("/providers/TDI/datasources/" + getId());
		ds.setId(getId());
		ds.setLabel("System Connectors");
		ds.setDescription("Installed TDI system connectors");
		ds.setUri("/providers/TDI/datasources/" + getId());
		ds.setDatasetsUri("/providers/TDI/datasources/" + getId() + "/datasets");
		ds.setType("");
		return this.ds;
	}
	
	protected List<TDIDataset> getDatasets() {
		return datasets;
	}

	@GET
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getDatasourceRest(@Context HttpServletRequest req) {
		return Response.ok(getDatasource()).build();
	}
	
	@GET
	@Path("datasets")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getDatasetsRest(@Context HttpServletRequest req) {
		Datasets ds = new Datasets();
		for(TDIDataset tds : getDatasets()) {
			ds.getItems().add(tds.getDataset());
		}
		
		ds.setFilteredRows(ds.getItems().size());
		ds.setNumRows(ds.getFilteredRows());
		ds.setTotalRows(ds.getFilteredRows());
		ds.setIdentifier("id");
		return Response.ok(ds).build();
	}
	
	@Path("datasets/{dataset}")
	public TDIDataset getDefaultDatasetRest(@Context HttpServletRequest req, @PathParam("dataset") String dataset) {
		String datasetRef = req.getParameter(CLIENT_REF_ID);
		if(datasetRef != null && datasetInstances.get(datasetRef) != null) {
			return datasetInstances.get(datasetRef);
		}
		
		for(TDIDataset tds : getDatasets()) {
			if(tds.getId().equals(dataset)) {
				if(datasetRef != null) {
					try {
						tds = tds.createClone(this, req, datasetRef);
						datasetInstances.put(datasetRef, tds);
						ConnectorUtils.logdebug("Create dataset instance for: " + dataset + ":" + datasetRef);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return tds;
			}
		}
		ConnectorUtils.logdebug(req.getMethod() + " request for unknown dataset '" + dataset + "'");
		return null;
	}

	public void removeDataset(TDIDataset tds) {
		if(tds.getDatasetRef()!= null) {
			ConnectorUtils.logdebug("Removed dataset instance for: " + tds.getId() + ":" + tds.getDatasetRef());
			datasetInstances.remove(tds.getDatasetRef());
		}
	}
	
}
