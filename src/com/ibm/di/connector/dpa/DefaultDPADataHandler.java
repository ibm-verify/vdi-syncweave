/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa;

import java.sql.SQLException;
import java.util.Iterator;

import com.ibm.di.connector.dpa.provider.DeployedAssetsSchema;
import com.ibm.di.connector.dpa.provider.SQLQuery;
import com.ibm.di.connector.dpa.schema.ClassInstance;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;

/**
 * The implementation of AbstractDataHandler designed to work with DPA data schema.
 * 
 * @author yavor.gologanov
 *
 */
public class DefaultDPADataHandler extends AbstractDataHandler {
	
	
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "dpaconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);	
	
	protected Iterator<ClassInstance> classIterator = null;
	protected SQLQuery searchQuery = null;
	
	
	/**
	 * 
	 * @param assetClass
	 */
	public DefaultDPADataHandler(String assetClass) {
		super(assetClass);
	}
	
	/**
	 * 
	 */
	public void selectEntries() throws DPAException {
		try {
			if (searchQuery == null) {
				classIterator = dataProvider.selectAssets(assetClass);
			} else {
				classIterator = dataProvider.selectAssets(searchQuery, assetClass);
			}
		} catch (SQLException e) {
			log.logError(e);
			throw new DPAException(e);
		}
	}
	
	/**
	 * 
	 */
	public void setSearchCriteria(SearchCriteria criteria) 
		throws DPAException {
		searchQuery = metaData.createSearchQuery(criteria, assetClass);	
	}
	
	/**
	 * 
	 */
	public Entry getNextEntry(boolean loadReferences) throws DPAException {
		
		if (classIterator == null || !classIterator.hasNext()) {
			classIterator = null;
			return null;
		} else {
			try {
				ClassInstance classInstance = classIterator.next();
				dataProvider.loadInstance(classInstance, loadReferences);
				Entry entry = metaData.createEntry(classInstance);
				log.debug(resHash.getString("DPA.CONN.DEBUG.GETNEXT", new Object[]{entry.toString()}));
				classInstance.clear();
				return entry;
			} catch (DPAException e) {
				throw e;
			} catch (Exception e) {
				log.logError(e);
				throw new DPAException(e);
			}
		}		
	}
	
	/**
	 * 
	 */
	public boolean addEntry(Entry entry) 
		throws DPAException {
		
		boolean saved = false;
 		try {
			ClassInstance classInstance = metaData.createClassInstance(entry);
			if (classInstance == null) {
				throw new DPAException(resHash.getString("DPA.CONN.INVALID.INPUT.ENTRY", new Object[]{entry.toString()}));
			}
			
			log.debug(resHash.getString("DPA.CONN.DEBUG.ADD.ENTRY", new Object[]{classInstance.toString()}));
			
			if (super.assetClass != DeployedAssetsSchema.DEPLOYED_ASSET
					&& !classInstance.getDefinition().getClassName().equals(super.assetClass)) {
				throw new DPAException(resHash.getString("DPA.CONN.ASSET.INCOMPATIBLE", new Object[]{super.assetClass + " and " + classInstance.getDefinition().getClassName()}));
			}

			saved = dataProvider.saveInstance(classInstance);
            log.debug(resHash.getString("DPA.CONN.DEBUG.SAVED.ENTRY", new Object[]{classInstance.toString()}));	
		} catch (SQLException e) {
			log.logError(e);
			throw new DPAException(e);
		} 

		return saved;
	}
	
	/**
	 * 
	 */
	public boolean removeEntry(Entry entry) throws DPAException {
		boolean removed = false;
		try {
			ClassInstance classInstance = metaData.createClassInstance(entry);
			if (classInstance == null) {
				throw new DPAException(resHash.getString("DPA.CONN.INVALID.INPUT.ENTRY", new Object[]{entry.toString()}));
			}
			
			removed = dataProvider.deleteInstance(classInstance);
            log.debug(resHash.getString("DPA.CONN.DEBUG.REMOVE.ENTRY", new Object[]{classInstance.toString()}));	
		} catch (Exception e) {
			log.logError(e);
			throw new DPAException(e);
		} 

		return removed;
	}
	
}