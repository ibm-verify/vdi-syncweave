/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa;

import com.ibm.di.connector.dpa.provider.DeployedAssetsProvider;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.SearchCriteria;

/**
 * This class declares methods for retrieving and modifying the data in MAXIMO
 * database. <br>
 * There is one implementation of this class for each data schema 
 * supported by the connector.
 * 
 * @author yavor.gologanov
 *
 */
public abstract class AbstractDataHandler {
	
	protected String assetClass = null;
	protected AbstractMetaData metaData = null;
	protected Entry schema = null;
	protected DeployedAssetsProvider dataProvider = null;	
	protected ConnectorLog log = null;
	
	/**
	 * 
	 * @param assetClass
	 */
	public AbstractDataHandler(String assetClass) {
		this.assetClass = assetClass;
	}	
	
	/**
	 * 
	 * @param log
	 */
	public void setLog(ConnectorLog log) {
		this.log = log;
	}
	
	/**
	 * 
	 * @return AbstractMetaData
	 */
	public AbstractMetaData getMetaData() {
		return metaData;
	}
	
	/**
	 * 
	 * @return Entry
	 */
	public Entry getSchema() {
		return schema;
	}
	
	/**
	 * 
	 * @param metaData
	 * @param dataProvider
	 * @throws DPAException
	 */
	public void init(AbstractMetaData metaData, 
			DeployedAssetsProvider dataProvider) 
		throws DPAException {
		this.metaData = metaData;
		this.dataProvider = dataProvider;		
		this.schema = metaData.createSchema(assetClass);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public abstract void selectEntries() throws Exception;
	
	/**
	 * 
	 * @param criteria
	 * @throws DPAException
	 */
	public abstract void setSearchCriteria(SearchCriteria criteria) 
		throws DPAException;
	
	/**
	 * 
	 * @param loadReferences
	 * @return Entry
	 * @throws DPAException
	 */
	public abstract Entry getNextEntry(boolean loadReferences) 
		throws DPAException;
	
	/**
	 * 
	 * @param entry
	 * @return boolean
	 * @throws DPAException
	 */
	public abstract boolean addEntry(Entry entry) 
		throws DPAException;
	
	/**
	 * 
	 * @param entry
	 * @return boolean
	 * @throws DPAException
	 */
	public abstract boolean removeEntry(Entry entry) throws DPAException;	
	
}
