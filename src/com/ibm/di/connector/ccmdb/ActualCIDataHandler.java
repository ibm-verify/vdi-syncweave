/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb;

import java.util.Iterator;
import java.util.List;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.provider.ActciProvider;
import com.ibm.di.connector.ccmdb.provider.CCMDBActualCIProvider;
import com.ibm.di.connector.ccmdb.provider.SQLQuery;
import com.ibm.di.connector.ccmdb.search.SearchQueryBuilder;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;

/**
 * An implementation of AbstrcatDataHander that is designed to work with Actual CIs.
 * 
 * @author yavor.gologanov
 *
 */

public class ActualCIDataHandler extends AbstractDataHandler {

	private Entry schema = null;
	private Iterator<ActualCI> itemIterator = null;
	private SQLQuery searchQuery = null;	
	private static ResourceHash resHash = CCMDBConnector.getResHash();
	
	/**
	 * 
	 */
	public void init(ExecutionContext context) {
		super.init(context);
		
		if (ALL.equalsIgnoreCase(context.getClassification())) {
			context.setClassification(CCMDBActualCIProvider.CLASS_ACTUAL_CI);
		}
	}
	
	/**
	 * 
	 */
	public Entry getSchema() throws CCMDBException {
		if (schema == null) {
			schema = ctx.getMetaData().createSchema(getDefinition());
		}
		return schema;
	}
	
	/**
	 * 
	 */
	public Entry getNextEntry() throws CCMDBException {		
		if (itemIterator == null || !itemIterator.hasNext()) {
			itemIterator = null;
			return null;
		} 

		Entry entry = null;
		try {
			ActualCI configItem = itemIterator.next();
			configItem.setLoadAttributes(true);
			configItem.setLoadDeletedActualCIRelation(true);
			configItem.setLoadOMPRelation(true);
			configItem.setLoadSrcRelation(ctx.isLoadSrcRelations());
			configItem.setLoadTrgRelation(ctx.isLoadTrgRelations());
			
			ActciProvider actciProvider = ctx.getDataProvider().getActciProvider();
			actciProvider.loadReferences(configItem);
			entry = ctx.getMetaData().createEntry(configItem);
			configItem.clear();
		} catch (Exception e) {
			throw new CCMDBException(e);
		} 
		
		return entry;
	}

	/**
	 * 
	 */
	public void setSearchCriteria(SearchCriteria criteria) throws CCMDBException {
		SearchQueryBuilder queryBuilder = new SearchQueryBuilder();
		searchQuery = queryBuilder.buildQuery(criteria, getDefinition());
        ctx.getLog().debug(resHash.getString("CCMDB.CONN.SEARCH.QUERY", new Object[]{"ActualCI", searchQuery}));
	}		
	
	/**
	 * 
	 */
	public void selectEntries() throws CCMDBException {		
		ActciProvider actciProvider = ctx.getDataProvider().getActciProvider();
		try {
			List<ActualCI> itemList = null;
			if (searchQuery == null) {
				itemList = actciProvider.select(getDefinition());
			} else {
				itemList = actciProvider.select(searchQuery, getDefinition());
			}
			
			if (itemList.size() > 0) {
				itemIterator = itemList.iterator();
			}
		} catch (Exception e) {
			throw new CCMDBException(e);
		} 	
	}
	
	/**
	 * 
	 */
	public boolean addEntry(Entry entry) throws CCMDBException {		
		ActciProvider actciProvider = ctx.getDataProvider().getActciProvider();
		boolean saved = false;
		try {
			ActualCI configItem = ctx.getMetaData().createActualCI(entry);
			if (configItem == null) {
				throw new CCMDBException(resHash.getString("CCMDB.CONN.UNABLE.TO.CREATE.INSTANCE", "Actual CI"));
			}
			
			checkClass(configItem);
						
			saved = actciProvider.save(configItem);
		} catch (Exception e) {
			throw new CCMDBException(e);
		} 
		
		return saved;
	}
	
	/**
	 * 
	 */
	public boolean removeEntry(Entry entry) throws CCMDBException {		
		ActciProvider actciProvider = ctx.getDataProvider().getActciProvider();
		boolean deleted = false;
		try {
			ActualCI configItem = ctx.getMetaData().createActualCI(entry);
			if (configItem == null) {
				throw new CCMDBException(resHash.getString("CCMDB.CONN.UNABLE.TO.CREATE.INSTANCE", "Actual CI"));
			}
			
			checkClass(configItem);
			
			deleted = actciProvider.delete(configItem);
		} catch (Exception e) {
			throw new CCMDBException(e);
		} 
		return deleted;
	}	
	
	/**
	 * 
	 */
	public boolean updateEntry(Entry entry) throws CCMDBException {
		boolean updated = false;
		ActciProvider actciProvider = ctx.getDataProvider().getActciProvider();
		try {
			ActualCI configItem = ctx.getMetaData().createActualCI(entry);
			if (configItem == null) {
				throw new CCMDBException(resHash.getString("CCMDB.CONN.UNABLE.TO.CREATE.INSTANCE", "Actual CI"));
			}
			
			checkClass(configItem);
			
			updated = actciProvider.update(configItem);
		} catch (Exception e) {
			throw new CCMDBException(e);
		}	
		return updated;
	}	
	
	/**
	 * 
	 * @return
	 * @throws CCMDBException
	 */
	private ActualCIDefinition getDefinition() throws CCMDBException {
		AbstractMetaData metaData = ctx.getMetaData();
		
		ActualCIDefinition definition = metaData.getActualCIDefinition(
				ctx.getClassification(),
				ctx.isLoadSrcRelations(),
				ctx.isLoadTrgRelations());		
		return definition;
	}	
	
	/**
	 * 
	 * @param configItem
	 * @throws CCMDBException 
	 */
	private void checkClass(ActualCI configItem) throws Exception {
		if (!ctx.getClassification().equals(CCMDBActualCIProvider.CLASS_ACTUAL_CI)) {
			String classname = ctx.getClassification();
			if (!configItem.getClassName().equals(classname)) {
				throw new CCMDBException(resHash.getString("CCMDB.CONN.INCOMPATIBLE.CLASSIFICATION", 
						new Object[]{classname, configItem.getClassName()}));
			}
		}
	}	
	
}
