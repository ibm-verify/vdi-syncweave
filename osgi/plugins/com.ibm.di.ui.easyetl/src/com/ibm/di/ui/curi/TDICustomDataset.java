/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.curi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.ui.curi.bind.Dataset;
import com.ibm.di.ui.curi.bind.DatasetColumn;
import com.ibm.di.ui.curi.bind.DatasetColumns;
import com.ibm.di.ui.curi.bind.DatasetParameter;

/**
 * This class overrides TDIDataset to provide more detailed calls into assemblylines in a configuration.
 * 
 * Dataset assemblyline use the following features of the assemblyline to map against CURI/REST structures.
 * 
 * <ul>
 * <li>
 * First, any parameters required by the assemblyline should be entered in the "Published Init Params" operation. The entries in
 * that section provide the identifier, label and syntax for the parameter. When the assemblyline is invoked by a consumer it will
 * have access to the provided parameter values in the assemblyline's opEntry.
 * <p>
 * <code>
 * 		var searchBase = task.opEntry.param_searchBase;
 * </code>
 * <p>
 * 
 * <li>
 * The columns in the dataset is either computed based on input mapping the assemblyline components, or explicitly defined
 * in the assemblyline's operation named "Default". This operation should either use the output schema or output attribute map
 * to specify the columns of the dataset.
 * 
 * <li>
 * </ul>
 */
public class TDICustomDataset extends TDIDataset {

	private AssemblyLineConfig config;
	private Entry customDS;

	public TDICustomDataset(TDIDatasource datasource, AssemblyLineConfig config) {
		super(datasource);
		this.config = config;
	}

	public TDIDataset createClone(TDIDatasource tdiDatasource, HttpServletRequest req, String datasetRef) throws Exception {
		TDIDataset tds = new TDICustomDataset(tdiDatasource, this.config);
		tds.setRequestParameters(req);
		tds.setDatasetRef(datasetRef);
		return tds;
	}
	
	@Override
	public String getId() {
		return config.getShortName();
	}
	
	@Override
	public int getCacheTimeout() {
		// TODO Auto-generated method stub
		if(this.customDS != null) {
			Object cacheTimeout = this.customDS.getObject("cacheTimeout");
			if(cacheTimeout instanceof Integer) {
				return ((Integer)cacheTimeout).intValue() * 1000;
			} else if(cacheTimeout instanceof String) {
				try {
					return Integer.parseInt(cacheTimeout.toString());
				} catch (Exception e) {
					ConnectorUtils.logerror("Custom cache timeout", e);
				}
			}
		}
		return super.getCacheTimeout();
	}

	@Override
	public String getLabel() {
		String label = config.getShortName();
		if(label != null && label.toUpperCase().startsWith("DS_"))
			return label.substring("DS_".length());
		else
			return label;
	}
	
	@Override
	public String getDescription() {
		String comment = config.getUserComment();
		if(comment == null || comment.trim().length() == 0)
			return getLabel();
		else
			return comment;
	}

	
	@Override
	public boolean isHierarchical() {
		//
		// -- If there is a custom assemblyline for related items then it's hierarchical.
		//
		return hasHelperAssemblyLine(REQ_RELATEDITEMS);
	}

	/**
	 * This method makes a best guess at what columns are available after executing a cycle
	 * of the assemblyline. It scan operations, schemas and input maps.
	 * 
	 * @return
	 * @throws Exception
	 */
	private List<String> getWorkAttributes() throws Exception {
		// Add attribute names from input connectors
		List<BaseConfiguration> items = config.getEntryFeedComponents().getConfigurations(null);
		config.getDataFlowComponents().getConfigurations(items);
		
		List<String> list = new ArrayList<String>();

		for (BaseConfiguration bc : items) {
			if (bc instanceof LoopConfig && bc.getEnabled()) {
				LoopConfig lc = (LoopConfig) bc;
				if (lc.getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
					// Get loop connector
					bc = lc.getLoopConnector();
				} else if (lc.getLoopType() == LoopConfig.LOOP_COLLECTION) {
					String name = lc.getLoopAttributeName();
					if(list.indexOf(name) == -1)
						list.add(name);
					continue;
				}
			}

			if (!(bc instanceof ConnectorConfig && bc.getEnabled()))
				continue;

			ConnectorConfig cc = (ConnectorConfig) bc;

			if (ConnectorConfig.ITERATOR_MODE.equals(cc.getMode()) || ConnectorConfig.LOOKUP_MODE.equals(cc.getMode()) ||
					ConnectorConfig.CALL_REPLY_MODE.equals(cc.getMode())) {

				boolean hasStarMap = false;
				for(String str : cc.getAttributeMap(true).getAttributeNames()) {
					if(str.equals("*")) {
						hasStarMap = true;
					} else if(!list.contains(str)) {
						list.add(str);
					}
				}
				
				//
				// -- If the map explicitly maps all (hasStarMap) or
				// -- the current list is empty and automap is configured then
				// -- we map all schema items as valid columns.
				//
				if (hasStarMap || (list.size() == 0 && config.autoMapAllAttributes(cc.getName()))) {
					for(String str : cc.getSchema(true).getItemNames()) {
						if(!list.contains(str))
							list.add(str);
					}
				}
			}
		}
		return list;
	}

	@Override
	protected List<DatasetParameter> getDatasetParameters() {
		List<DatasetParameter> list = new ArrayList<DatasetParameter>();
		for (String str : config.getPublishedInitParams().getItemNames()) {
			list.add(getDatasetParameter(str));
		}
		return list;
	}

	@Override
	protected DatasetParameter getDatasetParameter(String param) {
		SchemaItemConfig sic = config.getPublishedInitParams().getItem(param);
		DatasetParameter p = new DatasetParameter();
		p.setType("string");
		p.setId(param);
		p.setLabel(param);
		if (sic != null) {
			p.setRequired(sic.isRequired());
			String syntax = sic.getExternalSyntax();
			if ("boolean".equalsIgnoreCase(syntax))
				p.setType("boolean");
			else if ("number".equalsIgnoreCase(syntax))
				p.setType("int");
			else if ("date".equalsIgnoreCase(syntax))
				p.setType("date");
		}
		return p;
	}

	@Override
	protected List<DatasetColumn> getDatasetColumns(HttpServletRequest req, HashMap<String, String> params) {
		
		if(hasHelperAssemblyLine(REQ_COLUMNS)) {
			try {
				callHelperAssemblyLine(REQ_COLUMNS, new Entry(), req);
				return this._datasetColumns.getItems();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		OperationConfig cols = config.getOperation("Default");
		List<DatasetColumn> list = new ArrayList<DatasetColumn>();
		if (cols != null) {
			// Try Output schema for an explicit list of returned attributes
			for (String str : cols.getSchema(false).getItemNames()) {
				SchemaItemConfig sic = cols.getSchema(false).getItem(str);
				DatasetColumn col = new DatasetColumn();
				col.setId(sic.getAttributeName());
				col.setLabel(col.getId());
				col.setValueType("string");
				list.add(col);
			}

			// Try output map if schema isn't defined
			if (list.size() == 0) {
				for (String str : cols.getAttributeMap(false).getAttributeNames()) {
					AttributeMapItem ami = cols.getAttributeMap(false).getAttributeMapItem(str);
					DatasetColumn col = new DatasetColumn();
					col.setId(ami.getShortName());
					col.setLabel(col.getId());
					col.setValueType("string");
					list.add(col);
				}
			}
		}

		// If no output schema/map defined for GET operation
		// derive columns from work entry mappings
		if (list.size() == 0) {
			try {
				for (String str : getWorkAttributes()) {
					DatasetColumn col = new DatasetColumn();
					col.setId(str);
					col.setLabel(col.getId());
					col.setValueType("string");
					list.add(col);
				}
			} catch (Exception e) {
				e.printStackTrace();
				DatasetColumn col = new DatasetColumn();
				col.setId("**error**");
				col.setLabel(col.getId());
				col.setDescription(e.toString());
				col.setValueType("string");
				list.add(col);
			}
		}

		return list;
	}

	@Override
	protected void getDatasetItems(HttpServletRequest req, HashMap<String, String> params, List<Entry> items, int start, int count) {
		ConnectorUtils utils = new ConnectorUtils();
		try {
			utils.startAL(req, getDatasource().getId(), getId(), params);
			iterateEntries(utils, items, start, count, req);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			utils.terminate();
		}
	}

	@Override
	protected Object callAL(String operation, Entry inputData, HttpServletRequest req) throws Exception {
		
		if(REQ_RELATEDITEMS.equals(operation) && hasHelperAssemblyLine(REQ_RELATEDITEMS)) {

			Entry ds = (Entry) callHelperAssemblyLine(operation, inputData, req);
			List<Entry> links = generateLinksFromItems(inputData, (List<Entry>) ds.getObject("items"));
			ds.setAttribute("links", links);
			
			return toJson(ds);
			
			
		} else if(hasHelperAssemblyLine(operation)) {
			return callHelperAssemblyLine(operation, inputData, req);
		
		} else if(REQ_DATASET.equals(operation) &&
				"GET".equalsIgnoreCase(req.getMethod())) {
			Dataset dataset = getDataset();
			Entry ds = null;

			if(hasHelperAssemblyLine(REQ_DATASET)) {
				Object value = callHelperAssemblyLine(REQ_DATASET, toEntry(dataset), req);
				if(value instanceof Entry) {
					this.customDS = (Entry)value;
				}
			}

			List<Entry> items = getConnectorItems(req);
			if(Boolean.valueOf(req.getParameter("items"))) {
				ds = toEntry(dataset);
				List<Entry> itemList = toItems(items, req.getParameter("itemProperties"));
				ds.setAttribute("items", itemList);
			}

			if(Boolean.valueOf(req.getParameter("links"))) {
				List<Entry> links = new ArrayList(); // generateLinkItems(items, req);
				ds.setAttribute("links", links);
				ds.setAttribute("relationshipCount", links.size());
			}
			if(ds != null)
				return toJson(ds);
			else
				return dataset;
		}
		
		return super.callAL(operation, inputData, req);
	}

	/**
	 * Calls the helper assemblyline. The DS_*_operation assemblyline can return one of the following:
	 * <ul>
	 * <li>json - The json payload is returned as is
	 * <li>items - A standard list of items is generated with items as contents
	 * </ul> 
	 * @param operation
	 * @param inputData
	 * @param req
	 * @return
	 * @throws Exception
	 */
	private Object callHelperAssemblyLine(String operation, Entry inputData, HttpServletRequest req) throws Exception {
		ConnectorUtils utils = startAssemblyLine(operation, inputData, req);
		Entry entry = utils.getNextEntry();
		Object result = entry.getObject("result");
		
		if(REQ_COLUMNS.equals(operation)) {
			updateDatasetColumns((List)result);
			
		} else if(REQ_DATASET.equals(operation) && result instanceof Entry) {
			return result;
			
		} else if(REQ_RELATEDITEMS.equals(operation) &&	result instanceof List) {
			return createListEntry(toItems((List)result, req.getParameter("properties")));		
		}

		if(result instanceof List)
			return toJson(createListEntry((List)result));
		
		else if(result instanceof String)
			return result;
		
		else 
			return toJson(entry);
	}
	
	private ConnectorUtils startAssemblyLine(String operation, Entry inputData, HttpServletRequest req) throws Exception {
		String helperAl = getId() + "_" + operation;
		this.config.getMetamergeConfig().lookup(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + helperAl);
		ConnectorUtils utils = new ConnectorUtils();
		HashMap<String, String> params = getRequestParameters();
		for(String str : inputData.getAttributeNames()) {
			params.put(str, inputData.getString(str));
		}
		utils.startAL(req, getDatasource().getId(), helperAl, params);
		return utils;
	}
	
	
	private List<Entry> generateLinksFromItems(Entry parent, List<Entry> children) throws Exception {
		List<Entry> list = new ArrayList<Entry>();
		for(Entry child : children) {
			list.add(createLinkItem(parent, child));
		}
		return list;
	}

	private List<Entry> generateLinkItems(List<Entry> topLevel, HttpServletRequest req) throws Exception {
		List<Entry> list = new ArrayList<Entry>();
		for(Entry top : topLevel) {
			Entry inputData = new Entry();
			inputData.setAttribute("id", top.getString("id"));
			ConnectorUtils utils = startAssemblyLine(REQ_RELATEDITEMS, inputData, req);
			Entry entry = utils.getNextEntry();
			Object result = entry.getObject("result");
			if(result instanceof List) {
				for(Object obj : (List)result) {
					list.add(createLinkItem(top, (Entry) obj));
				}
			}
		}
		return list;
	}

	private Entry createLinkItem(Entry source, Entry target) {
		String item1 = source.getString("id");
		String item2 = target.getString("id");
		Entry e = new Entry();
		e.setAttribute("direction", "1_2");
		e.setAttribute("id", item1 + "_" + item2);
		e.setAttribute("item1", item1);
		e.setAttribute("item2", item2);
		e.setAttribute("label", "Link label");
		e.setAttribute("supportsProperties", false);
		e.setAttribute("tooltip", "Link type tooltip");
		e.setAttribute("type", "Default");
		e.setAttribute("uri", getDataset().getUri() + "/links/" + e.getString("id"));
		
		List<Entry> list = new java.util.ArrayList<Entry>();
		e.setAttribute("relationships", list);
		
		Entry rel = new Entry();
		rel.setAttribute("id", item1 + "_Default_" + item2);
		rel.setAttribute("linkId", e.getString("id"));
		rel.setAttribute("label", "Link label");
		rel.setAttribute("sourceItem", item1);
		rel.setAttribute("targetItem", item2);
		rel.setAttribute("tooltip", "Link tooltip");
		rel.setAttribute("type", "Default");
		//rel.uri = ds.relationshipsUri + "/" + rel.id;
		list.add(rel);
		
		return e;
	}
	
	private void updateDatasetColumns(List result) {
		this._datasetColumns = new DatasetColumns();
		for(Object obj : result) {
			Entry e = (Entry)obj;
			DatasetColumn dc = new DatasetColumn();
			dc.setDescription(e.getString("description"));
			dc.setId(e.getString("id"));
			dc.setLabel(e.getString("label"));
			dc.setValueType(e.getString("valueType"));
			this._datasetColumns.getItems().add(dc);
		}
		this._datasetColumns.setFilteredRows(result.size());
		this._datasetColumns.setTotalRows(result.size());
		this._datasetColumns.setNumRows(result.size());
	}
	
	private boolean hasHelperAssemblyLine(String operation) {
		String helperAl = getId() + "_" + operation;
		try {
			this.config.getMetamergeConfig().lookup(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + helperAl);
			return true;
		} catch(Exception nfe) {
			return false;
		}
	}

}
