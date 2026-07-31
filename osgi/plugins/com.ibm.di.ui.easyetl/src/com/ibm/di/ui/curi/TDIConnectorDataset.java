/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.curi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.ui.curi.bind.Dataset;
import com.ibm.di.ui.curi.bind.DatasetColumn;
import com.ibm.di.ui.curi.bind.DatasetParameter;

public class TDIConnectorDataset extends TDIDataset {

	private ConnectorConfig config;

	public TDIConnectorDataset(TDIDatasource datasource, ConnectorConfig cc) {
		super(datasource);
		this.config = cc;
	}

	private FormConfig form;
	private FormConfig sysform;

	public FormConfig getForm() {
		return this.form;
	}

	public Object getConnectionParam(String param) {
		return config.getConnectionConfig().getParameter(param);
	}
	
	public FormItemConfig getFormItem(String name) {
		FormItemConfig fic = this.form.getFormItem(name);
		if(fic == null && name.startsWith("$GLOBAL.")) {
			fic = sysform.getFormItem(name.substring(8));
		}
		return fic;
	}

	@Override
	protected Dataset createDataset() {
		Dataset dataset = super.createDataset();
		try {
			this.sysform = (FormConfig)MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.STDFORMS_NAMESPACE).lookup(MetamergeConfig.DEFAULT_FORM_FOLDER + "/__GLOBAL__");
			this.form = (FormConfig)MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE).lookup(MetamergeConfig.DEFAULT_FORM_FOLDER + "/" + config.getConnectionConfig().getJavaClass());
			dataset.setLabel(this.form.getTitle());
		} catch (Exception e) {
			ConnectorUtils.logdebug(getId() + ": " + e.toString());
		}
		return dataset;
	}
	
	@Override
	public String getId() {
		return this.config.getShortName();
	}

	protected List<DatasetParameter> getDatasetParameters() {
		FormConfig form = getForm();
		ArrayList<DatasetParameter> list = new ArrayList<DatasetParameter>();
		if(form != null) {
			for(String str : form.getFormItemNames()) {
				list.add(getDatasetParameter(str));
			}
		}
		return list;
	}
	
	protected DatasetParameter getDatasetParameter(String param) {
		FormItemConfig fic = getFormItem(param);
		DatasetParameter p = new DatasetParameter();
		if(fic != null) {
			p.setId(fic.getShortName());
			p.setLabel(fic.getLabel());
			p.setDescription(fic.getToolTip());
			p.setRequired(fic.isRequired());
			String syntax = fic.getSyntax();
			if("boolean".equalsIgnoreCase(syntax))
				p.setType("boolean");
			else
				p.setType("string");
			Object defaultValue = getConnectionParam(fic.getShortName());
			if(defaultValue != null)
				p.setDefault(defaultValue.toString());
		} else {
			ConnectorUtils.logdebug("Warning: no form item config for parameter: '" + param + "'");
		}
		return p;
	}

	@Override
	public TDIDataset createClone(TDIDatasource tdiDatasource, HttpServletRequest req, String datasetRef) throws Exception {
		TDIConnectorDataset tds = new TDIConnectorDataset(tdiDatasource, this.config);
		tds.setRequestParameters(req);
		tds.setDatasetRef(datasetRef);
		return tds;
	}

	@Override
	protected List<DatasetColumn> getDatasetColumns(HttpServletRequest req, HashMap<String, String> params) {
		List<DatasetColumn> list = new ArrayList<DatasetColumn>();
		ConnectorUtils util = new ConnectorUtils();
		try {
			util.startAL(req, getId(), params);
			Entry entry = util.getNextEntry();
			for(String str : entry.getAttributeNames()) {
				Object value = entry.getObject(str);
				DatasetColumn dsc = new DatasetColumn();
				dsc.setId(str);
				dsc.setLabel(str);
				if(value instanceof Integer)
					dsc.setValueType("int");
				else if(value instanceof Boolean)
					dsc.setValueType("boolean");
				else if(value instanceof Double)
					dsc.setValueType("double");
				else if(value instanceof Date)
					dsc.setValueType("isodatetime");
				else
					dsc.setValueType("string");
				list.add(dsc);
			}
			
		} catch (Exception e) {
			// TODO: do something?
			SystemFunctions.doNothing();
		}
		util.terminate();
		return list;
	}

	@Override
	protected void getDatasetItems(HttpServletRequest req, HashMap<String, String> params, List<Entry> items, int start, int count) {
		ConnectorUtils util = new ConnectorUtils();
		try {
			util.startAL(req, getId(), params);
			iterateEntries(util, items, start, count, req);
		} catch (Exception e) {
			e.printStackTrace();
		}
		util.terminate();		
	}
	
}
