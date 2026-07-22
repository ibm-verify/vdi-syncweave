/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.curi;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ibm.di.entry.Entry;
import com.ibm.di.parser.JSONParser;
import com.ibm.di.ui.curi.bind.Dataset;
import com.ibm.di.ui.curi.bind.DatasetColumn;
import com.ibm.di.ui.curi.bind.DatasetColumns;
import com.ibm.di.ui.curi.bind.DatasetItems;
import com.ibm.di.ui.curi.bind.DatasetParameter;
import com.ibm.di.ui.curi.bind.DatasetParameters;

/**
 * This is the base implementation of a dataset. This implementation uses a connector configuration and generates an assemblyline
 * config to run the read operations. It only supports read operations.
 * 
 * Sub classes should mostly override callAL to override default behaviour.
 *
 */
public abstract class TDIDataset {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public static final String REQ_PARAMETERS = "parameters";
	public static final String REQ_DATASET = "dataset";
	public static final String REQ_COLUMNS = "columns";
	public static final String REQ_COLUMN = "column";
	public static final String REQ_LINKS = "links";
	public static final String REQ_LINKSTYLES = "linkstyles";
	public static final String REQ_ITEMS = "items";
	public static final String REQ_ITEM = "item";
	public static final String REQ_ITEMSTYLES = "itemstyles";
	public static final String REQ_STATUS = "status";
	public static final String REQ_TASKS = "tasks";
	public static final String REQ_RELATIONSHIPS = "relationships";
	public static final String REQ_RELSTYLES = "relstyles";
	public static final String REQ_ORIGINITEMS = "originitems";
	public static final String REQ_RELATEDITEMS = "relateditems";
	
	// TDI extension to let users deliver images
	public static final String REQ_IMAGES = "images";
	
	// CURI Item field prefix (used in returned item entries)
	public static final String CURI_PREFIX = "curi_";
	
	// default cache timeout is 5 minutes 
	public int CACHE_TIMEOUT = Integer.getInteger("com.ibm.di.curi.cache.timeout", 300);

	/**
	 * The dataset definition 
	 */
	private Dataset ds;
	
	/**
	 * Parent dataset
	 */
	private TDIDatasource datasource;

	private HashMap<String, String> requestParameters = new HashMap<String, String>();

	/**
	 * The dataset instance identifier
	 */
	private String datasetRef;

	/**
	 * Dataset items cache
	 */
	private HashMap<String, CacheItem> cachedItems = new HashMap<String, CacheItem>();

	/**
	 * The column definitions 
	 */
	protected DatasetColumns _datasetColumns;

	private static class CacheItem {
		public long timestamp;
		public Object cachedItem;
	}
	
	public TDIDataset(TDIDatasource datasource) {
		this.datasource = datasource;
	}
	
	/**
	 * Returns true if the dataset is hierachical. On hierarchical datasets we add the "lazyLoad" and "relatedItemsUri"
	 * do dataset items.
	 * 
	 * @return
	 */
	public boolean isHierarchical() {
		return false;
	}
	
	protected Dataset createDataset() {
		Dataset ds = new Dataset();
		ds.setId(getId());
		ds.setLabel(getLabel());
		ds.setType("");
		ds.setViewType(isHierarchical() ? "graph" : "table");
		ds.setDatasourceId(datasource.getId());
		ds.setDescription(getDescription());
		ds.setEventsEnabled(false);
		ds.setItemCount(1);
		ds.setLinkCount(0);
		if(isHierarchical())
			ds.setRelationshipCount(1);
		else
			ds.setRelationshipCount(0);
		
		ds.setUri("/providers/TDI/datasources/" + datasource.getId() + "/datasets/" + ds.getId());
		ds.setItemsUri(ds.getUri() + "/items");
		ds.setColumnsUri(ds.getUri() + "/columns");
		ds.setProviderId("TDI");
		ds.setItemStylesUri(ds.getUri() + "/itemStyles");
		ds.setLinkStylesUri(ds.getUri() + "/linkStyles");
		ds.setLinksUri(ds.getUri() + "/links");
		ds.setRelationshipStylesUri(ds.getUri() + "/relStyles");
		ds.setRelationshipsUri(ds.getUri() + "/relationships");
		ds.setStatusUri(ds.getUri() + "/status");
		ds.setTasksUri(ds.getUri() + "/tasks");
		return ds;
	}

	/**
	 * Returns the datset description
	 * @return
	 */
	public String getDescription() {
		return getLabel();
	}

	/**
	 * Returns the dataset label
	 * @return
	 */
	public String getLabel() {
		return getId();
	}

	/**
	 * Returns the datasource to which this dataset belongs
	 * 
	 * @return
	 */
	public TDIDatasource getDatasource() {
		return this.datasource;
	}
	
	/**
	 * Returns the dataset definition
	 * @return
	 */
	public Dataset getDataset() {
		if(this.ds == null)
			this.ds = createDataset();

		return this.ds;
	}

	/**
	 * Returns the identifier for this dataset
	 * @return
	 */
	public String getId() {
		if(this.ds == null)
			return "Default";
		else
			return this.ds.getId();
	}
	
	/**
	 * Returns the dataset instance reference
	 * @return
	 */
	public String getDatasetRef() {
		return datasetRef;
	}

	/**
	 * Sets the dataset instance reference
	 * @param datasetRef
	 */
	public void setDatasetRef(String datasetRef) {
		this.datasetRef = datasetRef;
	}

	/**
	 * Returns the request most recent http query parameters 
	 * @return
	 */
	public HashMap<String, String> getRequestParameters() {
		return requestParameters;
	}

	/**
	 * Sets the request query parameters
	 * 
	 * @param req
	 * @return 
	 * @throws Exception
	 */
	public HashMap<String, String> setRequestParameters(HttpServletRequest req) throws Exception {
		this.requestParameters = new HashMap<String, String>();
		for(Enumeration<String> e = req.getParameterNames(); e.hasMoreElements(); ) {
			String str = e.nextElement();
			this.requestParameters.put(str, req.getParameter(str));
		}

		return this.requestParameters;
	}
	
	
	/**
	 * Returns the cache timeout in milliseconds
	 * 
	 * @return
	 */
	public int getCacheTimeout() {
		return CACHE_TIMEOUT * 1000;
	}
	
	/**
	 * Returns the internal cache keys for this instance
	 * 
	 * @return
	 */
	public List<String> getCacheKeys() {
		List<String> list = new ArrayList<String>();
		for(String str : cachedItems.keySet()) 
			list.add(str);
		return list;
	}

	/**
	 * Returns a string describing the cache status
	 * @param cacheKey 
	 * 
	 * @return
	 */
	public Object getCacheStatus(String cacheKey) {
		CacheItem cache = this.cachedItems.get(cacheKey);
		if(cache != null) {
			Entry e = new Entry();
			e.setAttribute("timeoutAt", new Date(cache.timestamp).toString());
			e.setAttribute("cachedEntriesCount", ((List<Entry>)cache.cachedItem).size());
			return e;
		} else {
			return "cache for key " + cacheKey + " does not exist";
		}
	}

	/**
	 * Returns a cache based on cacheKey
	 * 
	 * @param cacheKey
	 * @return
	 */
	private List<Entry> getDatasetCache(String cacheKey) {
		CacheItem cache = this.cachedItems.get(cacheKey);
		if(cache != null) {
			if((System.currentTimeMillis() - cache.timestamp) < getCacheTimeout()) {
				return (List<Entry>) cache.cachedItem;
			}
			this.cachedItems.remove(cacheKey);
		}
		return null;
	}
	
	
	/**
	 * Refreshes the timestamp on a cache entry
	 * 
	 * @param cacheKey
	 */
	private void touchDatasetCache(String cacheKey) {
		CacheItem cache = this.cachedItems.get(cacheKey);
		if(cache != null) {
			cache.timestamp = System.currentTimeMillis();
		}
	}

	/**
	 * Adds a dataset to the cache - only if this is an instance (e.g. getDatasetRef() != null)
	 * 
	 * @param cacheKey
	 * @param items
	 */
	private void addDatasetCache(String cacheKey, List<Entry> items) {
		if(getDatasetRef() != null && CACHE_TIMEOUT != 0) {
			CacheItem cache = new CacheItem();
			cache.timestamp = System.currentTimeMillis();
			cache.cachedItem = items;
			this.cachedItems.put(cacheKey, cache);
		}
	}

	/**
	 * Returns an array of the cached datasets
	 * @return
	 */
	public List<List<Entry>> getCachedDatasets() {
		List<List<Entry>> list = new ArrayList<List<Entry>>();
		List<String> remove = new ArrayList<String>();
		for(String key : this.cachedItems.keySet()) {
			CacheItem cache = this.cachedItems.get(key);
			if((System.currentTimeMillis() - cache.timestamp) < getCacheTimeout()) {
				list.add((List<Entry>) cache.cachedItem);
			} else {
				remove.add(key);
			}
		}
		for(String key : remove)
			this.cachedItems.remove(key);
		
		return list;
	}

	/**
	 * Create a clone of this dataset - used when a new dataset instance is needed
	 * 
	 * @param tdiDatasource
	 * @param req
	 * @param datasetRef
	 * @return
	 * @throws Exception
	 */
	public abstract TDIDataset createClone(TDIDatasource tdiDatasource, HttpServletRequest req, String datasetRef) throws Exception;

	/**
	 * Prints out the request parameters to the log.
	 * 
	 * @param req
	 * @param title
	 */
	protected void printRequestParameters(HttpServletRequest req, String title) {
		ConnectorUtils.logdebug("*** " + title);
		ConnectorUtils.logdebug("[url] " + req.getRequestURL());
		for(Enumeration<String> e = req.getParameterNames(); e.hasMoreElements(); ) {
			String str = e.nextElement();
			ConnectorUtils.logdebug("  " + str + ": " + req.getParameter(str));
		}
		ConnectorUtils.logdebug("[http headers]");
		for(Enumeration<String> e = req.getHeaderNames(); e.hasMoreElements(); ) {
			String str = e.nextElement();
			ConnectorUtils.logdebug("  " + str + ": " + req.getHeader(str));
		}
		
		ConnectorUtils.logdebug("");
	}
	
	/**
	 * Returns the list of parameters for this dataset
	 * @return
	 */
	protected abstract List<DatasetParameter> getDatasetParameters();
	
	/**
	 * Returns a specific parameter for this dataset
	 * @param param
	 * @return
	 */
	protected abstract DatasetParameter getDatasetParameter(String param);

	/**
	 * Returns the columns for this dataset
	 * @param req
	 * @param params
	 * @return
	 */
	protected abstract List<DatasetColumn> getDatasetColumns(HttpServletRequest req, HashMap<String, String> params);
	
	/**
	 * Returns a specific column for this dataset
	 * @param name
	 * @param req
	 * @param params
	 * @return
	 */
	protected DatasetColumn getDatasetColumn(String name, HttpServletRequest req, HashMap<String, String> params) {
		for(DatasetColumn dc : _getDatasetColumns(req, params).getItems()) {
			if(dc.getId().equals(name))
				return dc;
		}
		return null;
	}
	
	private DatasetColumns _getDatasetColumns(HttpServletRequest req, HashMap<String, String> params) {
		if(this._datasetColumns == null) {
			DatasetColumns cols = new DatasetColumns();
			cols.setIdentifier("id");
			cols.getItems();
			
			for(DatasetColumn dc : getDatasetColumns(req, params)) {
				cols.getItems().add(dc);
			}
			
			cols.setFilteredRows(cols.getItems().size());
			cols.setNumRows(cols.getFilteredRows());
			cols.setTotalRows(cols.getFilteredRows());
			this._datasetColumns = cols;
		}
		return this._datasetColumns;
	}
	
	/**
	 * Retrieves the specified items from the dataset 
	 * 
	 * @param req
	 * @param params
	 * @param items
	 * @param start
	 * @param count
	 */
	protected abstract void getDatasetItems(HttpServletRequest req, HashMap<String,String> params, List<Entry> items, int start, int count);
	
	protected List<Entry> getConnectorItems(HttpServletRequest req) throws Exception {
		
		HashMap<String, String> params = setRequestParameters(req);
		
		int start = req.getParameter("start") != null ? Integer.parseInt(req.getParameter("start")) : 1;
		int count = req.getParameter("count") != null ? Integer.parseInt(req.getParameter("count")) : Integer.MAX_VALUE;
		String conditions = req.getParameter("conditions");
		String cacheKey = start + "_" + count + "_" + conditions;
		
		ConnectorUtils.logdebug("Returning dataset items: start=" + start + "; count=" + count);

		List<Entry> entries = getDatasetCache(cacheKey);
		if(entries == null) {
			ConnectorUtils.logdebug("TDIDataset: call assemblyline to retrieve data: " + getId());
			entries = new ArrayList<Entry>();
			getDatasetItems(req, params, entries, start, count);
			addDatasetCache(cacheKey, entries);
		} else {
			touchDatasetCache(cacheKey);
			ConnectorUtils.logdebug("TDIDataset: reusing cached dataset: " + getId());
		}
		
		//
		// -- sort 
		//
		sortDatasetItems(req.getParameter("sort"), entries);

		//
		// -- filter
		//
		// applyConditions(req.getParameter("conditions"), entries);
		
		//
		// -- make sure we have columns so we can create the correct output
		//
		_getDatasetColumns(req, params);
		
		ConnectorUtils.logdebug("Return numRows=" + entries.size());
		return entries;
	}
	
	/**
	 * This method will start an assemblyline that provides the items for the dataset.
	 * The assemblyline is called in manual mode (e.g. executeCycle) and each cycle will deliver a single
	 * item. If the returned item contains an "id" attribute this is used for the meta ID for the item itself.
	 * 
	 * @param util
	 * @param items
	 * @param start
	 * @param count
	 * @param req
	 * @throws Exception
	 */
	protected void iterateEntries(ConnectorUtils util, List<Entry> items, int start, int count, HttpServletRequest req) throws Exception {
		int id = 0;
		
		while(count-- > 0) {
			Entry entry = util.getNextEntry();
			if(entry == null)
				break;
			else if(entry.size() == 0)
				break;
			
			id++;
			
			//
			// If requesting specific ID then check if this is handled by assemblyline
			//
			String providedIdentifier = getEntryString(entry, "id");
			if(providedIdentifier == null && id < start)
				continue;

			//
			// Add id based on 
			if(providedIdentifier == null)
				entry.setAttribute("id", ""+id);
			
			items.add(entry);
		}		
	}
	
	@GET
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getDatasetRest(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "GET dataset: " + getId());
		return Response.ok(callAL(REQ_DATASET, req)).build();
	}
	
	@DELETE
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response deleteDatasetRest(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "DELETE dataset: " + getId());
		if(getDatasource() != null)
			getDatasource().removeDataset(this);
		return Response.ok(callAL(REQ_DATASET, req)).build();
	}
	
	@PUT
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response putDatasetRest(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "PUT dataset: " + getId());
		return Response.ok(callAL(REQ_DATASET, req)).build();
	}
	
	@GET
	@Path("relationships")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getRelationships(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "GET RELATIONSHIPS");
		return Response.ok(callAL(REQ_RELATIONSHIPS, req)).build();
	}
	
	@GET
	@Path("relStyles")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getRelstyles(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "GET relstyles");
		return Response.ok(callAL(REQ_RELSTYLES, req)).build();
	}
	
	@GET
	@Path("links")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getLinks(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "GET links");
		return Response.ok(callAL(REQ_LINKS, req)).build();
	}
	
	@GET
	@Path("linkStyles")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getLinkstyles(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "GET linkstyles");
		return Response.ok(callAL(REQ_LINKSTYLES, req)).build();
	}
	
	@GET
	@Path("tasks")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getTasks(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "GET tasks");
		return Response.ok(callAL(REQ_TASKS, req)).build();
	}
	
	@POST
	@Path("tasks")
	@Consumes({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response postTasks(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "POST tasks");
		return Response.ok(callAL(REQ_TASKS, req)).build();
	}
	
	@DELETE
	@Path("tasks")
	@Consumes({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response deleteTasks(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "POST tasks");
		return Response.ok(callAL(REQ_TASKS, req)).build();
	}
	
	@GET
	@Path("itemStyles")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getItemstyles(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "GET itemstyles");
		return Response.ok(callAL(REQ_ITEMSTYLES, req)).build();
	}
	
	@GET
	@Path("parameters")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getDefaultDatasetParametersRest(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "GET dataset/parameters");
		return Response.ok(callAL(REQ_PARAMETERS, req)).build();
	}

	@GET
	@Path("parameters/{param}")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getDefaultDatasetParameterRest(@Context HttpServletRequest req, @PathParam("param") String param) throws Exception {
		printRequestParameters(req, "GET dataset/parameters/" + param);
		Entry input = new Entry();
		input.setAttribute("id", param);
		return Response.ok(callAL(REQ_PARAMETERS, input, req)).build();
	}
	
	@GET
	@Path("columns")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getColumns(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "GET dataset/columns");
		return Response.ok(callAL(REQ_COLUMNS, req)).build();
	}
	
	@GET
	@Path("columns/{name}")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getColumnsByName(@Context HttpServletRequest req, @PathParam("name") String name) throws Exception {
		printRequestParameters(req, "GET dataset/columns");
		Entry input = new Entry();
		input.setAttribute("id", name);
		return Response.ok(callAL(REQ_COLUMN, input, req)).build();
	}
	
	@GET
	@Path("originItems")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getOriginItems(@Context HttpServletRequest req) throws Exception {
		return Response.ok(callAL(REQ_ORIGINITEMS, req)).build();
	}
	
	@GET
	@Path("status")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getStatus(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "GET dataset/status");
		return Response.ok(callAL(REQ_STATUS, req)).build();
	}
	
	@GET
	@Path("items")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getItems(@Context HttpServletRequest req) throws Exception {
		printRequestParameters(req, "GET dataset/items");
		getDatasetColumns(req, setRequestParameters(req));
		return Response.ok(callAL(REQ_ITEMS, req)).build();
	}
	
	@GET
	@Path("items/{itemid}")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getItemById(@Context HttpServletRequest req, @PathParam("itemid") String itemid) throws Exception {
		printRequestParameters(req, "GET item by id: " + itemid);
		
		getDatasetColumns(req, setRequestParameters(req));

		for(List<Entry> cache : getCachedDatasets()) {
			for(Entry e: cache) {
				if(itemid.equals(getEntryString(e, "id"))) {
					ConnectorUtils.logdebug("Returning cached item: " + itemid + ": " + e);
					Entry item = toItem(e, req.getParameter("properties"));
					String json = toJson(item);
					ConnectorUtils.logdebug("Returning item " + itemid + "\n" + json);
					return Response.ok(json).build();
				}
			}
		}
		
		
		HashMap<String, String> params = setRequestParameters(req);
		params.put("id", itemid);
		int start;
		
		// We may have generated this id ourselves
		try {
			start = Integer.parseInt(itemid);
		} catch (Exception e) {
			start = 1;
		}

		ConnectorUtils.logdebug("Returning dataset item: itemid=" + itemid);
		ArrayList<Entry> items = new ArrayList<Entry>();
		getDatasetItems(req, params, items, start, 1);
		if(items.size() == 1) {
			Entry item = toItem(items.get(0), req.getParameter("properties"));
			String json = toJson(item);
			ConnectorUtils.logdebug("Returning item " + itemid + "\n" + json);
			return Response.ok(json).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@GET
	@Path("items/{itemid}/relatedItems")
	@Produces({CuriHandler.APPLICATION_TIVOLIDIS, MediaType.APPLICATION_JSON})
	public Response getRelatedItemsById(@Context HttpServletRequest req, @PathParam("itemid") String itemid) throws Exception {
		printRequestParameters(req, "GET items/relatedItems: " + itemid);
		Entry inputData = new Entry();
		inputData.setAttribute("id", itemid);
		return Response.ok(callAL("relateditems", inputData, req)).build();
	}

	private void setDefaultValue(Entry item, String field, String value, String defaultField) {
		if(item.getAttribute(field) == null) {
			if(value == null)
				value = getEntryString(item, defaultField);
			item.setAttribute(field, value);
		}
	}
		
	/**
	 * Creates a DatasetItem from an Entry with the specified properties
	 * 
	 * @param itemid
	 * @param entry
	 * @param properties
	 * @return
	 */
	private Entry toItem(Entry entry, String properties) {

		String label = getEntryString(entry, "label");
		if(label == null)
			label = getEntryString(entry, "id");
		
		String tooltip = getEntryString(entry, "tooltip");
		if(tooltip == null)
			tooltip = label;

		String description = getEntryString(entry, "description");
		if(description == null)
			description = tooltip;

		String name = getEntryString(entry, "name");
		if(name == null)
			name = label;

		//
		// -- CURI Item
		//
		Entry item = new Entry();
		
		for(String str : entry.getAttributeNames()) {
			if(str.startsWith(CURI_PREFIX)) {
				item.setAttribute(str.substring(CURI_PREFIX.length()), entry.getAttribute(str));
			}
		}
		
		if(entry.getAttribute("id") == null) {
			ConnectorUtils.logerror("Entry does not contain required attribute 'id': " + entry.toString());
		}
		
		item.setAttribute("id", getEntryString(entry, "id"));
		
		setDefaultValue(item, "name", name, "id");
		setDefaultValue(item, "label", label, "id");
		setDefaultValue(item, "tooltip", tooltip, "label");
		setDefaultValue(item, "description", description, "tooltip");
		
		if(item.getAttribute("type") == null)
			item.setAttribute("type", "Default");
		if(item.getAttribute("typeLabel") == null)
			item.setAttribute("typeLabel", "Default");
		if(item.getAttribute("uri") == null)
			item.setAttribute("uri", datasource.getDatasource().getDatasetsUri() + "/" + getId() + "/items/" + item.getString("id"));
		
		if(isHierarchical()) {
			if(item.getAttribute("lazyLoad") == null)
				item.setAttribute("lazyLoad", true);
			item.setAttribute("relatedItemsUri", item.getString("uri") + "/relatedItems");
			item.setAttribute("relationshipsKey", "child");
		}
		
//		//
//		// -- If user passes java objects then we convert the basic ones
//		// -- to make life easier when building structures.
//		// -- This should really go into the JSONParser code.
//		//
//		try {
//			JavascriptUtils.convertJavascriptObjects(item);
//		} catch (Exception e) {
//			ConnectorUtils.logerror("While converting javascript objects: " + item.toString(), e);
//		}
		
		//
		// -- CURI Property array
		//
		if(properties != null && properties.length() > 0 && this._datasetColumns != null) {
			
			// when asked for all we have to return what the columns url returned
			List<DatasetColumn> list = new ArrayList<DatasetColumn>();
			if(properties.equals("all")) {
				list = this._datasetColumns.getItems();
			} else {
				List<String> props = new ArrayList<String>();
				for(String str : properties.split(",")) {
					props.add(str);
				}
				for(DatasetColumn dc : this._datasetColumns.getItems()) {
					if(props.contains(dc.getId())) {
						list.add(dc);
					}
				}
			}
			
			List<Entry> props = new ArrayList<Entry>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			for(DatasetColumn dc : list) {
				
				Object value = entry.getObject(dc.getId());
				if(value == null)
					value = "";
				
				//
				// -- AL may return a Property structure for the value
				//
				Entry prop = new Entry();
				if(value instanceof Entry) {
					prop = ((Entry) value).clone();
					value = prop.getObject("value");
				}
				
				// Set required fields if not already set
				if(prop.getAttribute("id") == null)
					prop.setAttribute("id", dc.getId());
				
				if(prop.getAttribute("label") == null)
					prop.setAttribute("label", dc.getLabel());
				
				if(prop.getAttribute("valueType") == null)
					prop.setAttribute("valueType", dc.getValueType());
				
				//
				// -- Do basic conversion of the value
				//
				if("isodatetime".equals(dc.getValueType())) {
					if(value instanceof Date) {
						prop.setAttribute("value", sdf.format((Date)value));
					} else {
						prop.setAttribute("value", value.toString());
					}
					
				} else if ("int".equals(dc.getValueType())) {
					if(value instanceof Number)
						prop.setAttribute("value", ((Number)value).intValue());
					else
						prop.setAttribute("value", Integer.parseInt(value.toString()));
					
				} else if ("double".equals(dc.getValueType())) {
					if(value instanceof Number)
						prop.setAttribute("value", ((Number)value).doubleValue());
					else
						prop.setAttribute("value", Double.parseDouble(value.toString()));
					
				} else if ("boolean".equals(dc.getValueType())) {
					if(value instanceof Boolean)
						prop.setAttribute("value", value);
					else
						prop.setAttribute("value", Boolean.valueOf(value.toString()));
					
				} else if ("string".equals(dc.getValueType())) {
					prop.setAttribute("value", value.toString());
					
				} else if(value instanceof Number || value instanceof String || value instanceof Boolean) {
					prop.setAttribute("value", value);
					
				} else if(value instanceof Entry) {
					prop.setAttribute("value", value);
					
				} else if(value instanceof List<?>) {
					prop.setAttribute("value", value);
					
				} else {
					prop.setAttribute("value", value.toString());
					
				}
				if(prop.getAttribute("displayValue") == null)
					prop.setAttribute("displayValue", prop.getString("value"));
				
				if(prop.getAttribute("valueState") == null)
					prop.setAttribute("valueState", "ok");
				
				props.add(prop);
			}
			
			if(props.size() > 0)
				item.setAttribute("properties", props);
		}		
		return item;
	}
	
	/**
	 * Calls callAL(operation, new Entry(), req);
	 * 
	 * @param operation
	 * @param req
	 * @return
	 * @throws Exception
	 */
	protected Object callAL(String operation, HttpServletRequest req) throws Exception {
		return callAL(operation, new Entry(), req);
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
		
		
		if(REQ_DATASET.equals(operation)) {
			if("DELETE".equalsIgnoreCase(req.getMethod())) {
				return "{}";
				
			} else if ("GET".equalsIgnoreCase(req.getMethod())) {
				Dataset dataset = getDataset();
				Entry ds = null;
				
				if(Boolean.valueOf(req.getParameter("items"))) {
					ds = toEntry(dataset);
					ds.setAttribute("items", toItems(getConnectorItems(req), req.getParameter("itemProperties")));
				}
				
				if(Boolean.valueOf(req.getParameter("links"))) {
					if(ds == null)
						ds = toEntry(dataset);
					ds.setAttribute("items", new ArrayList()); // entries2items(getConnectorItems(req), req.getParameter("linkProperties")));
				}
				if(ds != null)
					return toJson(ds);
				else
					return dataset;
			}

		} else if (REQ_TASKS.equals(operation)) {
			DatasetItems ret = new DatasetItems();
			ret.setIdentifier("id");
			ret.getItems();
			return ret;
			
		} else if (REQ_PARAMETERS.equals(operation)) {
			String param = inputData.getString("id");
			if(param != null) {
				DatasetParameter p = getDatasetParameter(param);
				if(p == null) {
					throw new Exception("Unknown parameter: " + param + " in component " + getId()); 
				}
				return p;
			} else {
				DatasetParameters dp = new DatasetParameters();
				dp.setIdentifier("id");
				dp.getItems();
				for(DatasetParameter p : getDatasetParameters()) {
					dp.getItems().add(p);
					dp.setTotalRows(dp.getTotalRows()+1);
				}
				dp.setNumRows(dp.getTotalRows());
				ConnectorUtils.logdebug(getId() + " returns " + dp.getNumRows() + " parameters");
				return dp;
			}
			
		} else if(REQ_COLUMNS.equals(operation)) {
			HashMap<String, String> params = setRequestParameters(req);
			return _getDatasetColumns(req, params);

		} else if(REQ_COLUMN.equals(operation)) {
			return getDatasetColumn(inputData.getString("id"), req, setRequestParameters(req));

		} else if(REQ_ORIGINITEMS.equals(operation)) {
			List<Entry> obj = getConnectorItems(req);
			return listToJson((List<Entry>)obj, req.getParameter("properties"));

		} else if(REQ_ITEMS.equals(operation)) {
			List<Entry> obj = getConnectorItems(req);
			return listToJson((List<Entry>)obj, req.getParameter("properties"));

		} else if(REQ_ITEMSTYLES.equals(operation)) {
			Entry itemStyle = new Entry();
			itemStyle.setAttribute("description",  "Default item style description");
			itemStyle.setAttribute("id",  "Default");
			itemStyle.setAttribute("label",  "Default item style");
			itemStyle.setAttribute("type",  "Default");
			return toJson(createListEntry(itemStyle));

		} else if(REQ_LINKSTYLES.equals(operation)) {
			Entry linkStyle = new Entry();
			linkStyle.setAttribute("arrowColor", "#0000FF");
			linkStyle.setAttribute("id",  "Default");
			linkStyle.setAttribute("label",  "Manages");
			linkStyle.setAttribute("lineColor",  "#00AA00");
			linkStyle.setAttribute("lineStyle",  "solid");
			linkStyle.setAttribute("lineWidth",  1);
			linkStyle.setAttribute("type",  "Default");
			return toJson(createListEntry(linkStyle));
			
		} else if(REQ_STATUS.equals(operation)) {
			return toJson(createListEntry(new ArrayList<Entry>()));
			
		}

		return "{}";
	}

	/**
	 * Due to varying data types we want to make sure values use quotation only when
	 * the syntax requires it.
	 * 
	 * @param obj
	 * @return
	 * @throws Exception 
	 */
	protected Object listToJson(List<Entry> entries, String properties) throws Exception {
		return toJson( createListEntry(toItems(entries, properties)) );
	}
	
	/**
	 * Converts a list of entries to DatasetItem style entries.
	 *  
	 * @param entries
	 * @param properties
	 * @return List of converted entries
	 */
	protected List<Entry> toItems(List<Entry> entries, String properties) {
		List<Entry> items = new ArrayList<Entry>();
		for(Entry entry : entries) {
			items.add(toItem(entry, properties));
		}
		return items;
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
	
	protected Entry createListEntry(List<Entry> items) {
		Entry e = new Entry();
		e.setAttribute("identifier", "id");
		e.setAttribute("numRows", items.size());
		e.setAttribute("totalRows", items.size());
		e.setAttribute("filteredRows", items.size());
		e.setAttribute("items", items);
		return e;
	}

	private Entry createListEntry(Entry itemStyle) {
		List<Entry> list = new ArrayList<Entry>();
		list.add(itemStyle);
		return createListEntry(list);
	}

	/**
	 * Sorts the items according to the "sort" parameter value in the request.
	 * 
	 * @param sort Comma separated list of attributes (with '-' prefix for descending)
	 * @param entries
	 */
	private void sortDatasetItems(String sort, List<Entry> entries) {
		if(sort == null || sort.equals(""))
			return;

		//
		// Sort attributes can be ID, Label or Description so we need to find those first
		// so we have a list of real attribute names (with the ascending/descending prefix)
		//
		final ArrayList<String> sortAttributes = new ArrayList<String>();
		if(this._datasetColumns != null) {
			for(String str : sort.split(",")) {
				boolean ascending = str.charAt(0) != '-';
				String key = ascending ? str : str.substring(1);
				for(DatasetColumn dc : _datasetColumns.getItems()) {
					if(key.equals(dc.getId()))
						sortAttributes.add(str);
					else if(str.equals(dc.getLabel()))
						sortAttributes.add((ascending ? dc.getId() : "-" + dc.getId()));
					else if(str.equals(dc.getLabel()))
						sortAttributes.add((ascending ? dc.getId() : "-" + dc.getId()));
				}
			}
		}
		
		Collections.sort(entries, new Comparator<Entry>() {
			public int compare(Entry a, Entry b) {
				for(int i = 0; i < sortAttributes.size(); i++) {
					String sort = sortAttributes.get(i);
					boolean ascending = sort.charAt(0) != '-';
					String attribute = ascending ? sort : sort.substring(1);
					Object aVal = getEntryObject(a, attribute);
					Object bVal = getEntryObject(b, attribute);
					// If neither has a value use the next nested sort attribute
					if(aVal == null && bVal == null)
						break;
					// If a is missing value return a < b
					if(aVal == null)
						return ascending ? -1 : 1;
					// If b is missing value return a > b
					if(bVal == null)
						return ascending ? 1 : -1;

					if(aVal.getClass().isInstance(bVal)) {
						if(aVal instanceof Comparable<?>) {
							int result = ((Comparable)aVal).compareTo(bVal);
							if(result != 0)
								return ascending ? result : (result * -1);
						}
					} else {
						// catch all - compare strings
						int result = aVal.toString().compareTo(bVal.toString());
						if(result != 0)
							return ascending ? result : (result * -1);
					}
				}
				return 0;
			}
		});
	}

	/**
	 * Create an Entry object with the value from teh getters of the object. Only numeric, boolean and string fields
	 * are returned.
	 * 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	protected Entry toEntry(Object obj) throws Exception {
		Entry ds = new Entry();
		for(Method m : obj.getClass().getMethods()) {
			String name = m.getName();
			if(name.startsWith("get")) {
				Object value = m.invoke(obj);
				if(value instanceof Number || value instanceof Boolean || value instanceof String) {
					name = name.substring(3,4).toLowerCase() + name.substring(4);
					ds.setAttribute(name, value);
				}
			}
		}
		return ds;
	}
	
	/**
	 * Returns the value object from an attribute. An attribute may be represented as single values
	 * or as an entry in which case it is a curi property structure.
	 * 
	 * @param entry
	 * @param attribute
	 * @return
	 */
	protected Object getEntryObject(Entry entry, String attribute) {
		Object value = entry.getObject(attribute);
		if(value instanceof Entry) {
			return ((Entry)value).getObject("value");
		} else if (value != null) {
			return value;
		} else {
			return null;
		}
	}

	/**
	 * Returns the string value from an attribute. An attribute may be represented as single values
	 * or as an entry in which case it is a curi property structure.
	 * 
	 * @param entry
	 * @param attribute
	 * @return
	 */
	protected String getEntryString(Entry entry, String attribute) {
		Object value = getEntryObject(entry, attribute);
		if (value != null) {
			return value.toString();
		} else {
			return null;
		}
	}
}
