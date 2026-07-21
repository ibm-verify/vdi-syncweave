/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.curi;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.di.api.remote.AssemblyLine;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.ui.curi.bind.DatasetColumn;
import com.ibm.di.ui.curi.bind.DatasetParameter;
import com.ibm.di.ui.easyetl.internal.SessionUtils;

/**
 * This is a passthrough handler that sends all requests to CURI_Handler in a configuration.
 *
 */
public class TDICustomDataset2 extends TDIDataset {

	private AssemblyLineConfig config;

	public TDICustomDataset2(TDIDatasource datasource, AssemblyLineConfig config) {
		super(datasource);
		this.config = config;
	}

	public TDIDataset createClone(TDIDatasource tdiDatasource, HttpServletRequest req, String datasetRef) throws Exception {
		TDIDataset tds = new TDICustomDataset2(tdiDatasource, this.config);
		tds.setRequestParameters(req);
		tds.setDatasetRef(datasetRef);
		return tds;
	}
	
	@Override
	public String getId() {
		return config.getShortName();
	}
	
	/**
	 * Calls the DS assemblyline with request parameters. The assemblyline should return a string
	 * payload in the "result" attribute and other http headers in the "headers" attribute.
	 * 
	 * @param operation
	 * @param inputData
	 * @param req
	 * @return
	 * @throws Exception
	 */
	protected Object callAL(String operation, Entry inputData, HttpServletRequest req) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		ConfigInstance ci = sess.getConfigInstance(getDatasource().getId());
		if(ci == null)
			ci = sess.startConfigInstance(getDatasource().getId());
		
		inputData.setAttribute("request", operation);
		inputData.setAttribute("method", req.getMethod());
		
		// HTTP Headers
		Entry headers = new Entry();
		inputData.setAttribute("headers", headers);
		for(Enumeration<String> en = req.getHeaderNames(); en.hasMoreElements(); ) {
			String p = en.nextElement();
			for(Enumeration<String> values = req.getHeaders(p); values.hasMoreElements(); ) {
				headers.addAttributeValue(p, values.nextElement());
			}
		}
		
		// Query Parameters
		Entry params = new Entry();
		inputData.setAttribute("params", params);
		for(Enumeration<String> en = req.getParameterNames(); en.hasMoreElements(); ) {
			String p = en.nextElement();
			for(String str : req.getParameterValues(p)) {
				params.addAttributeValue(p, str);
			}
		}
		
		
		if("POST".equals(req.getMethod())) {
			StringBuffer buf = new StringBuffer();
			String str;
			while((str = req.getReader().readLine()) != null) {
				buf.append(str+"\n");
			}
			inputData.setAttribute("body", buf.toString());
		}
		
		AssemblyLine al = ci.startAssemblyLine(getId(), inputData, true);
		Entry result = al.getResult();
		if(result != null) {
			return result.getString("result");
		}
		return "";
	}
	
	@GET
	@Path("items/{itemid}")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getItemById(@Context HttpServletRequest req, @PathParam("itemid") String itemid) throws Exception {
		printRequestParameters(req, "GET item by id: " + itemid);
		Entry inputData = new Entry();
		inputData.setAttribute("id", itemid);
		return Response.ok(callAL("items", inputData, req)).build();
	}

	@Override
	protected List<DatasetParameter> getDatasetParameters() {
		return null;
	}

	@Override
	protected DatasetParameter getDatasetParameter(String param) {
		return null;
	}

	@Override
	protected List<DatasetColumn> getDatasetColumns(HttpServletRequest req, HashMap<String, String> params) {
		return null;
	}

	@Override
	protected void getDatasetItems(HttpServletRequest req, HashMap<String, String> params, List<Entry> items, int start, int count) {
	}

}
