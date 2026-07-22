/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.curi;

import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.di.api.remote.Session;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.parser.JSONParser;
import com.ibm.di.ui.curi.bind.Datasets;
import com.ibm.di.ui.curi.bind.Datasources;
import com.ibm.di.ui.easyetl.internal.SessionUtils;

@Path(CuriHandler.URL)
public class CuriHandler {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public final static String APPLICATION_TIVOLIDIS = "application/vnd.ibm.com.tivolidis.json";
	public final static String URL = "tivoli/rest/providers";

	private List<TDIDatasource> datasources = null;
	private List<TDIDatasource> customDatasources = null;
	private HashMap<String, TDIDatasource> dsmap = new HashMap<String, TDIDatasource>();
	private HashMap<String, TDIDatasource> customdsmap = new HashMap<String, TDIDatasource>();
	
	private static boolean includeSystemNamespace = Boolean.valueOf(System.getProperty("com.ibm.tdi.curi.include.system", "true"));
	private static String customConfigs = System.getProperty("com.ibm.tdi.curi.datasources", "curi");
	
	private static String CUSTOM_PROVIDER_AL = "DS_PROVIDER_ENTRY";

	public CuriHandler() {
		datasources = new ArrayList<TDIDatasource>();
		
		//
		// Include every connector (except those requiring a parser) from the system namespace
		//
		if(includeSystemNamespace) {
			try {
				TDIDatasource ds = new TDIDatasource(MetamergeConfigFactory.SYSTEM_NAMESPACE);
				datasources.add(ds);
				dsmap.put(ds.getId(), ds);
			} catch (Exception e) {
				ConnectorUtils.logerror("While loading system namespace", e);
			}
		}
	}
	
	/**
	 * We run through all configs every time so we can pick up new ones on the fly.
	 * 
	 * @param req
	 * @return
	 * @throws Exception
	 */
	private List<TDIDatasource> findCuriConfigHandlers(HttpServletRequest req) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		if(this.customDatasources == null)
			customDatasources = new ArrayList<TDIDatasource>();
		ArrayList configs = sess.listConfigurations(customConfigs);
		for (Object obj : configs) {
			String id = obj.toString();
			if(!customdsmap.containsKey(id)) {
				ConnectorUtils.logdebug("Adding custom CURI provider: " + id);
				addDatasource(sess, id);
			}
		}
		
		// now search for all configurations that start with CURI_
		configs = sess.listConfigurations("");
		for (Object obj : configs) {
			String id = obj.toString();
			if(!customdsmap.containsKey(id) && id.toUpperCase().startsWith("CURI_")) {
				ConnectorUtils.logdebug("Adding custom CURI provider: " + id);
				addDatasource(sess, id);
			}
		}
		return customDatasources;
	}
	
	private TDIDatasource addDatasource(Session sess, String solid) throws Exception {
		MetamergeConfig mc = null;
		TDIDatasource ds = null;
		try {
			mc = sess.checkOutConfiguration(solid);
			ds = new TDICustomDatasource(mc);
			// -- Only add those that actually have datasets
			if(ds.getDatasets().size() > 0) {
				customDatasources.add(ds);
				customdsmap.put(solid, ds);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sess.undoCheckOut(solid);
		}
		return ds;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProviders(@Context HttpServletRequest req) throws Exception {
		ConnectorUtils.logdebug("Request for: " + req.getRequestURL().toString());

		List<Entry> items = new ArrayList<Entry>();
		items.add(getTDIProviderItem(req));
		
		Entry e = new Entry();
		e.setAttribute("identifier", "id");
		e.setAttribute("numRows", items.size());
		e.setAttribute("totalRows", items.size());
		e.setAttribute("filteredRows", items.size());
		e.setAttribute("items", items);

		return Response.ok(toJson(e)).build();
	}

	@GET
	@Path("TDI")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTDIProvider(@Context HttpServletRequest req) throws Exception {
		ConnectorUtils.logdebug("Request for: " + req.getRequestURL().toString());
		String item = toJson(getTDIProviderItem(req));
		return Response.ok(item).build();
	}

	@GET
	@Path("TDI/cache")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTDIProviderStatus(@Context HttpServletRequest req) throws Exception {
		ConnectorUtils.logdebug("Request for: " + req.getRequestURL().toString());
		List<Entry> list = new ArrayList<Entry>();
		for(TDIDatasource ds : customDatasources) {
			HashMap<String, TDIDataset> instances = ds.getDatasetInstances();
			Entry entry = new Entry();
			entry.setAttribute("datasource", ds.getId());
			List<Entry> dslist = new ArrayList<Entry>();
			entry.setAttribute("instances", dslist);
			for(String key : instances.keySet()) {
				Entry dsentry = new Entry();
				dsentry.setAttribute("instance", key);
				TDIDataset tds = instances.get(key);
				List<Entry> cachelist = new ArrayList<Entry>();
				dsentry.setAttribute("cacheStatus", key);
				for(String cacheKey : tds.getCacheKeys()) {
					Entry cacheEntry = new Entry();
					cacheEntry.setAttribute("cacheKey", cacheKey);
					cacheEntry.setAttribute("cacheStatus", tds.getCacheStatus(cacheKey));
					cachelist.add(cacheEntry);
				}
				dslist.add(dsentry);
			}
			list.add(entry);
		}
		return Response.ok(list).build();
	}
	
	/**
	 * Returns the list of registered datasets.
	 * 
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("TDI/datasources")
	@Produces({ APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON })
	public Response getTDIDatasources(@Context HttpServletRequest req) throws Exception {
		ConnectorUtils.logdebug("Request for: " + req.getRequestURL().toString());

		Datasources dss = new Datasources();
		dss.setIdentifier("id");

		for (TDIDatasource ds : datasources) {
			dss.getItems().add(ds.getDatasource());
		}

		for (TDIDatasource ds : findCuriConfigHandlers(req)) {
			dss.getItems().add(ds.getDatasource());
		}

		dss.setFilteredRows(dss.getItems().size());
		dss.setNumRows(dss.getFilteredRows());
		dss.setTotalRows(dss.getFilteredRows());

		return Response.ok(dss).build();
	}

	@Path("TDI/datasources/{source}")
	public TDIDatasource getTDIDatasource(@Context HttpServletRequest req, @PathParam("source") String source) throws Exception {
		ConnectorUtils.logdebug("Request for: " + req.getRequestURL().toString());
		TDIDatasource ds = dsmap.get(source);
		if(ds == null) {
			findCuriConfigHandlers(req);
			ds = customdsmap.get(source);
		}
		return ds;
	}

	/**
	 * Returns an empty list of datasets. Datasets are organized by Datasource
	 * in TDI.
	 * 
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("TDI/datasets")
	@Produces({ APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON })
	public Response getTDIToplevelDatasets(@Context HttpServletRequest req) throws Exception {
		ConnectorUtils.logdebug("Request for: " + req.getRequestURL().toString());
		Datasets ds = new Datasets();
		ds.setNumRows(0);
		ds.setFilteredRows(0);
		ds.setTotalRows(0);
		return Response.ok(ds).build();
	}
	
	@GET
	@Path("reload/{config}")
	@Produces({MediaType.TEXT_PLAIN})
	public void reloadConfiguration(@Context HttpServletRequest req, @PathParam("config") String config) throws Exception {
		TDIDatasource ds = getTDIDatasource(req, config);
		if(ds instanceof TDICustomDatasource) {
			((TDICustomDatasource)ds).reloadConfiguration(req);
		}
	}

	@GET
	@Path("register/{config}")
	@Produces({MediaType.TEXT_PLAIN})
	public void registerConfiguration(@Context HttpServletRequest req, @PathParam("config") String config) throws Exception {
		TDIDatasource ds = getTDIDatasource(req, config);
		if(ds == null) {
			Session sess = SessionUtils.getServerApiSession(req);
			addDatasource(sess, config);
		}
	}
	
	private String getBaseUrl(HttpServletRequest req) {
		String str = req.getRequestURL().toString();
		return str.substring(0, str.indexOf("/providers"));
	}

	private Entry getTDIProviderItem(HttpServletRequest req) {
		Entry item = new Entry();
		item.setAttribute("id", "TDI");
		item.setAttribute("label", "Security Verify Directory Integrator");
		item.setAttribute("remote", false);
		item.setAttribute("useFIPS", false);
		item.setAttribute("baseUrl", getBaseUrl(req));
		item.setAttribute("datasetsUr(", "/providers/TDI/datasets");
		item.setAttribute("datasourcesUri", "/providers/TDI/datasources");
		item.setAttribute("uri", "/providers/TDI");
		item.setAttribute("sso", false);
		item.setAttribute("externalProviderId", "TDI");
		item.setAttribute("type", "TDI");
		try {
			item.setAttribute("MSSName", "ibm-cdm:///CDMSS/Hostname=" +
					getHostName() +	"+ManufacturerName=IBM+ProductName=IBM Security Verify Directory Integrator");
		} catch (UnknownHostException e1) {
			ConnectorUtils.logerror(e1.getLocalizedMessage(), e1);
		}
		
		try {
			Session sess = SessionUtils.getServerApiSession(req);
			
			// refresh handlers
			findCuriConfigHandlers(req);
			
			// check each curi config it is has a provider al
			for(String solid : customdsmap.keySet()) {
				ConnectorUtils utils = new ConnectorUtils();
				try {
					MetamergeConfig mc = sess.checkOutConfiguration(solid);
					try {
						mc.getAssemblyLine(CUSTOM_PROVIDER_AL);
						HashMap<String, String> params = new HashMap<String, String>();
						utils.startAL(req, solid, CUSTOM_PROVIDER_AL, params);
						item = utils.getNextEntry(item);
						utils.terminate();
					} catch (NameNotFoundException nfe) {
						SystemFunctions.doNothing(); // ignore
					} catch (Exception e) {
						ConnectorUtils.logerror(e.getLocalizedMessage(), e);
					}
				} catch (Exception e) {
					ConnectorUtils.logerror(e.getLocalizedMessage(), e);
				}
			}
		} catch (Exception e) {
			ConnectorUtils.logerror(e.getLocalizedMessage(), e);
		}
		
		return item;
	}

	private String getHostName() throws UnknownHostException {
		String hostname = InetAddress.getLocalHost().getHostName();
		if(hostname != null && hostname.indexOf(".") != -1)
			return hostname.substring(0, hostname.indexOf("."));
		else
			return hostname;
	}

	protected String toJson(Entry e) throws Exception {
		StringWriter writer = new StringWriter();
		JSONParser p = new JSONParser();
		p.setOutputStream(writer);
		p.initParser();
		p.writeEntry(e);
		p.flush();
		return writer.getBuffer().toString();
	}
}
