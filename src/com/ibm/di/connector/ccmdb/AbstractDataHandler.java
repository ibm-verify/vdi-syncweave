/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.di.connector.ccmdb.provider.CCMDBActualCIProvider;
import com.ibm.di.connector.ccmdb.provider.ClassificationProvider;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.idml.IdMLConstants;
import com.ibm.di.server.SearchCriteria;

/**
 * This class declares methods for retrieving and modifying of data entries 
 * based on the selected artifact type. <br>
 * There is one implementation of this class for each artifact type. 
 * 
 * @author yavor.gologanov
 *
 */
public abstract class AbstractDataHandler {
	
	public static final String ALL = "ALL";
	
	protected ExecutionContext ctx = null;
	
	/**
	 * 
	 * @param context 
	 * 				the executionContext instance associated with the current execution.
	 */
	public void init(ExecutionContext context) {
		this.ctx = context;
	}	
	
	/**
	 * Returns a list of classification names for a specified artifact type.
	 * 
	 * @param artifactType 
	 * 				Artifact type name
	 * @return List<String>
	 * 				List that contains classification names
	 * @throws CCMDBException 
	 * 				if a problem occurs.
	 */
	public List<String> getClassifications(String artifactType) 
		throws CCMDBException {
		
		List<String> result = new ArrayList<String>();		
		try {
			String className = "";
			if (IdMLConstants.ARTIFACT_CI.equals(artifactType)) {
				className = CCMDBActualCIProvider.CLASS_ACTUAL_CI;
			} else if (IdMLConstants.ARTIFACT_RELATIONSHIP.equals(artifactType)) {
				className = CCMDBActualCIProvider.CLASS_CI_RELATION;	
			}
			
			ClassificationProvider classificationProvider = 
				ctx.getDataProvider().getClassificationProvider();
			
			List<String> typeList = classificationProvider.getClassificationNames(className);
			if (typeList != null) {
				Set<String> tmpSet = new HashSet<String>(typeList);
				result.addAll(tmpSet);
				Collections.sort(result);
			}
		} catch (SQLException e) {
			throw new CCMDBException(e);
		}
		
		result.add(0, ALL);
		return result;
	}	
	
		
	/**
	 * Sets the search criteria used in select operation.
	 * 
	 * @param criteria
	 * 				SearchCriteria object provided by CE
	 * @throws CCMDBException
	 * 				if a problem occurs.
	 */
	public abstract void setSearchCriteria(SearchCriteria criteria) 
		throws CCMDBException;	
	
	/**
	 * Returns the schema that is based on selected artifact type and class name.
	 * 
	 * @return Entry
	 * 				The schema. 
	 * @throws CCMDBException
	 * 				if a problem occurs.
	 */
	public abstract Entry getSchema() 
		throws CCMDBException;
	
	/**
	 * This method will fetch instances of selected artifact type. 
	 * The selection is based on selected class name and search criteria that has been set.
	 * 
	 * @throws CCMDBException
	 * 				if a problem occurs.
	 */
	public abstract void selectEntries() 
		throws CCMDBException;	
	
	/**
	 * This method returns a single entry object for a selected artifact instance,
	 * searched as per criteria. 
	 * 
	 * @return Entry 
	 * 				the next read entry.
	 * @throws CCMDBException
	 * 				if a problem occurs.
	 */
	public abstract Entry getNextEntry() 
		throws CCMDBException;
		
	/**
	 * Inserts an instance of selected artifact type into the data store.
	 * 
	 * @param entry 
	 * 				the Entry to be added.
	 * @return boolean 
	 * 				indicates if the entry has been added successfully. 
	 * 				 
	 * @throws CCMDBException
	 * 				if a problem occurs.
	 */
	public abstract boolean addEntry(Entry entry) 
		throws CCMDBException;
	
	/**
	 * Removes an instance of selected artifact type from the data store.
	 * The instance is identified based on an input entry.
	 * 
	 * @param entry 
	 * 				the Entry to be deleted.
	 * @return boolean 
	 * 				indicates if the specified artifact has been deleted successfully. 
	 * @throws CCMDBException
	 * 				if a problem occurs.
	 */
	public abstract boolean removeEntry(Entry entry) 
		throws CCMDBException;
	
	/**
	 * Updates an instance of selected artifact type in the database.
	 * The instance attributes are extracted from an input entry.
	 * 
	 * @param entry
					The Entry to be updated.
	 * @return boolean 
	 * 				indicates if the specified artifact has been updated successfully. 
	 * @throws CCMDBException
	 * 				if a problem occurs.
	 */
	public abstract boolean updateEntry(Entry entry) 
		throws CCMDBException;
	
}
