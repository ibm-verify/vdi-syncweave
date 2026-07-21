/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa;

import com.ibm.di.connector.dpa.provider.SQLQuery;
import com.ibm.di.connector.dpa.schema.ClassDefinitionFactory;
import com.ibm.di.connector.dpa.schema.ClassInstance;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.SearchCriteria;

/**
 * An abstract class that is designed to implement data transformation 
 * between internal data model and TDI data model. 
 * There is one implementation of this class for each schema supported by the connector.
 * 
 * @author yavor.gologanov
 *
 */
public abstract class AbstractMetaData {
	
	protected ConnectorLog log = null;
	protected String connectorMode = null;
	
	/**
	 * 
	 * @param connectorMode
	 * @throws DPAException
	 */
	public void init(String connectorMode) throws DPAException {
		this.connectorMode = connectorMode;
	}
	
	/**
	 * 
	 * @return ConnectorLog
	 */
	public ConnectorLog getLog() {
		return log;
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
	 * @param assetClass
	 * @return Entry
	 * @throws DPAException
	 */
	public abstract Entry createSchema(String assetClass) 
		throws DPAException;	
	
	/**
	 * 
	 * @param classInstance
	 * @return Entry
	 * @throws DPAException
	 */
	public abstract Entry createEntry(ClassInstance classInstance)
		throws DPAException;
	
	/**
	 * 
	 * @param entry
	 * @return ClassInstance
	 * @throws DPAException
	 */
	public abstract ClassInstance createClassInstance(Entry entry)
		throws DPAException;
	
	/**
	 * 
	 * @param criteria
	 * @param assetClass
	 * @return SQLQuery
	 * @throws DPAException
	 */
	public abstract SQLQuery createSearchQuery(SearchCriteria criteria, 
			String assetClass)
		throws DPAException;
	
	/**
	 * 
	 * @return ClassDefinitionFactory
	 */
	public abstract ClassDefinitionFactory getClassDefinitionFactory();
	
}
