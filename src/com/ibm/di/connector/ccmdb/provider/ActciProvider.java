/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.provider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ibm.di.connector.ccmdb.CCMDBException;
import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.CIRelation;
import com.ibm.di.connector.ccmdb.model.ClassAttribute;
import com.ibm.di.connector.ccmdb.model.ModelObject;
import com.ibm.di.connector.ccmdb.model.OMPRelation;
import com.ibm.di.connector.ccmdb.model.RelationSet;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.AttributeDefinition;
import com.ibm.di.connector.ccmdb.model.def.Classification;
import com.ibm.di.server.ResourceHash;

/**
 * This class is responsible for reading and writing instances of actual configuration items. 
 * 
 * @author yavor.gologanov
 *
 */
public class ActciProvider {
	
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "ccmdbconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);
	

	private CCMDBActualCIProvider provider = null;
	
	/**
	 * 
	 * @param provider
	 */
	protected ActciProvider(CCMDBActualCIProvider provider) {
		this.provider = provider;
	}		
	
	/**
	 * Selects configuration items by a given item definition.
	 * 
	 * @param definition
	 * @return List<ActualCI>
	 * @throws SQLException
	 */
	public List<ActualCI> select(ActualCIDefinition definition) 
		throws SQLException {
		
		Classification classification = definition.getClassification();
		if (classification.getClassName().equals(CCMDBActualCIProvider.CLASS_ACTUAL_CI)) {
			return selectAll();
		}
		
		List<ActualCI> itemList = new ArrayList<ActualCI>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String sql = provider.getSQL(QuerySet.ACTCI_SELECT_BY_CLASSSTRUCTURE);
		try {
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, classification.getClassstructureId());
			resultSet = statement.executeQuery();

			ResultSetMetaData metaData = resultSet.getMetaData();
			while (resultSet.next()) {
				ActualCI nextItem = new ActualCI();
				nextItem.setClassification(classification);
				provider.loadProperties(resultSet, metaData, nextItem);
				itemList.add(nextItem);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
			
		return itemList;
	}	
	
	/**
	 * Selects configuration items by a given item definition and search query.
	 * 
	 * @param query
	 * @param definition
	 * @return List<ActualCI>
	 * @throws SQLException
	 */
	public List<ActualCI> select(SQLQuery query, ActualCIDefinition definition) 
		throws SQLException {
		
		ClassificationProvider classificationProvider = provider.getClassificationProvider();
		List<ActualCI> itemList = new ArrayList<ActualCI>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String sql = query.getSQL();
		try {
			statement = provider.getConnection().prepareStatement(sql);
		    int paramCount = query.getParameterCount();
			if (paramCount > 0) {
				for (int i=0; i<paramCount; i++) {
					provider.setValue(statement, i+1, query.getParameterValue(i));
				}
			}
			resultSet = statement.executeQuery();

			ResultSetMetaData metaData = resultSet.getMetaData();
			while (resultSet.next()) {
				ActualCI nextItem = new ActualCI();
				provider.loadProperties(resultSet, metaData, nextItem);
				
				String classstructureId = nextItem.getClassstructureId();
				if (classstructureId == null) {
					continue;
				}
				Classification classification =
					classificationProvider.getClasssificationByClassstructure(classstructureId);
				nextItem.setClassification(classification);
				
				itemList.add(nextItem);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
			
		return itemList;
	}			
	
	/**
	 * 
	 * @param configItem
	 * @throws SQLException
	 * @throws DataProcessingException
	 */
	public void loadReferences(ActualCI configItem) 
		throws SQLException, DataProcessingException {
	
		if (configItem.isLoadAttributes()) {
			loadAttributes(configItem);
		}		
		
		if (configItem.isLoadSrcRelation()) {
			loadSourceRelations(configItem);
		}
		
		if (configItem.isLoadTrgRelation()) {
			loadTargetRelations(configItem);
		}
		
		if (configItem.isLoadDeletedActualCIRelation()) {
			loadDeletedActualCI(configItem);
		}
		
		if (configItem.isLoadOMPRelation()) {
			loadOMPRelation(configItem);
		}
	}		
	
	/**
	 * 
	 * @param configItem
	 * @return boolean
	 * @throws SQLException
	 * @throws DataProcessingException
	 */
	public boolean save(ActualCI configItem) 
		throws SQLException, DataProcessingException {	
		
		if (exist(configItem.getGuid())) {
			throw new DataProcessingException(resHash.getString("CCMDB.CONN.CI.EXISTS", new Object[] {configItem.getGuid()}));
		}		
		
		boolean inserted = false;
		provider.getConnection().setAutoCommit(false);
		
		try {			
			saveActualCI(configItem, true, false);			
			provider.getConnection().commit();
			inserted = true;
		} catch (Exception e) {
 			provider.getConnection().rollback();
 			provider.getContext().getLog().logError(e);
 			throw new DataProcessingException(e);
		}  finally {
			provider.getConnection().setAutoCommit(true);
		}
		
		return inserted;
	}		
	
	/**
	 * 
	 * @param configItem
	 * @return boolean
	 * @throws SQLException
	 * @throws DataProcessingException
	 */
	public boolean update(ActualCI configItem) 
		throws SQLException, DataProcessingException {	

		if (!exist(configItem.getGuid())) {
			return save(configItem);
		}
		
		boolean updated = false;
		provider.getConnection().setAutoCommit(false);
		
		try {			
			updateActualCI(configItem);			
			provider.getConnection().commit();
			updated = true;
		} catch (Exception e) {
 			provider.getConnection().rollback();
 			provider.getContext().getLog().logError(e);
 			throw new DataProcessingException(e);
		}  finally {
			provider.getConnection().setAutoCommit(true);
		}
		
		return updated;
	}		
	
	/**
	 * 
	 * @param configItem
	 * @return boolean
	 * @throws SQLException
	 * @throws DataProcessingException
	 */
	public boolean delete(ActualCI configItem) 
		throws SQLException, DataProcessingException {
		
		if (!exist(configItem.getGuid())) {
			return false;
		}
		
		configItem = this.findActualCIByGUID(configItem.getGuid());
		
		boolean inserted = false;
		provider.getConnection().setAutoCommit(false);
		
		try {
			deleteRelations(configItem);
			deleteAttributes(configItem);
			deleteConfigItem(configItem);			
			provider.getConnection().commit();
			inserted = true;
		} catch (Exception e) {
 			provider.getConnection().rollback();
 			provider.getContext().getLog().logError(e);
 			throw new DataProcessingException(e);
		}  finally {
			provider.getConnection().setAutoCommit(true);
		}
		
		return inserted;
	}	
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @return List<ActualCI>
	 * @throws SQLException
	 */
	public List<ActualCI> selectAll() 
		throws SQLException {
		
		ClassificationProvider classificationProvider = provider.getClassificationProvider();
		List<ActualCI> itemList = new ArrayList<ActualCI>();

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String sql = provider.getSQL(QuerySet.ACTCI_SELECT_ALL);
		try {
			statement = provider.getConnection().prepareStatement(sql);
			resultSet = statement.executeQuery();

			ResultSetMetaData metaData = resultSet.getMetaData();
			while (resultSet.next()) {
				ActualCI nextItem = new ActualCI();
				provider.loadProperties(resultSet, metaData, nextItem);
				
				String classstructureId = nextItem.getClassstructureId();
				if (classstructureId == null) {
					continue;
				}
	
				Classification classification =
					classificationProvider.getClasssificationByClassstructure(classstructureId);
				nextItem.setClassification(classification);
				itemList.add(nextItem);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
			
		return itemList;
	}		
	
	/**
	 * 
	 * @param guid
	 * @return ActualCI
	 * @throws SQLException
	 * @throws CCMDBException
	 */
	protected ActualCI findActualCIByGUID(String guid) 
		throws SQLException {

		ClassificationProvider classificationProvider = provider.getClassificationProvider();
		
		ActualCI configItem = null;		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String sql = provider.getSQL(QuerySet.ACTCI_SELECT_BY_GUID);
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, guid.trim());
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				configItem = new ActualCI();
				ResultSetMetaData metaData = resultSet.getMetaData();
				provider.loadProperties(resultSet, metaData, configItem);				
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}	
		
		if (configItem != null) {
			String classstructureId = configItem.getClassstructureId();
			Classification classification = 
				classificationProvider.getClasssificationByClassstructure(classstructureId);
			configItem.setClassification(classification);
			loadAttributes(configItem);
		}
		return configItem;
	}		
	
	/**
	 * 
	 * @param configItem
	 * @param check
	 * @param skip
	 * @throws SQLException
	 * @throws DataProcessingException
	 */
	protected ActualCI saveActualCI(ActualCI configItem, boolean check, boolean skip) 
		throws SQLException, DataProcessingException {	
		
		if (check || skip) {
			boolean exist = exist(configItem.getGuid());
			if (exist) {
				if (check) {
					throw new SQLException(resHash.getString("CCMDB.CONN.ACTCI.EXISTS", new Object[]{configItem.getGuid()}));
				}
			
				if (skip) {
					return this.findActualCIByGUID(configItem.getGuid());
				}
			}
		}
		
		DefinitionProvider definitionProvider = provider.getDefinitionProvider();
		ActualCIDefinition definition = definitionProvider.getActualCIDefinition(configItem.getClassName());
		definitionProvider.loadSourceRelationRules(definition);
		definitionProvider.loadTargetRelationRules(definition);
		
		Integer actciid = provider.getInteger(QuerySet.ACTCI_SELECT_NEXT_ACTCIID);
		configItem.adjust(actciid + 1);
		String changeby = (String) configItem.getProperty(CCMDBActualCISchema.ACTCI_CHANGEBY);
		Object changedate = configItem.getProperty(CCMDBActualCISchema.ACTCI_CHANGEDATE);
		
		SQLQuery insertQuery = CCMDBActualCISchema.createInsertQuery(configItem, definition);
		provider.executeQuery(insertQuery);
		
        Integer attrStartId = provider.getInteger(QuerySet.ACTCISPEC_SELECT_NEXT_ID);
        attrStartId += 1;        
		Collection<AttributeDefinition> attrDefList = definition.getAttributes();
		for (AttributeDefinition attrDef : attrDefList) {
			ClassAttribute attribute = configItem.getAttribute(attrDef.getName());
			if (attribute != null) {
				attribute.setProperty(CCMDBActualCISchema.ACTCISPEC_ACTCISPECID, attrStartId++);
				attribute.setProperty(CCMDBActualCISchema.ACTCISPEC_CHANGEBY, changeby);
				attribute.setProperty(CCMDBActualCISchema.ACTCISPEC_CHANGEDATE, changedate);
				attribute.setProperty(CCMDBActualCISchema.ACTCISPEC_ACTCINUM, configItem.getActcinum());
				attribute.setProperty(CCMDBActualCISchema.ACTCISPEC_CLASSSTRUCTUREID, definition.getClassification().getClassstructureId());
				
				SQLQuery query = CCMDBActualCISchema.createInsertQuery(attrDef, attribute);
				provider.executeQuery(query);
			}
		}        
        
		RelationSet relationSet = configItem.getSourceRelations();
		if (relationSet != null) {
			saveRelationSet(relationSet);
		}
		
		relationSet = configItem.getTargetRelations();
		if (relationSet != null) {
			saveRelationSet(relationSet);
		}
		
		return configItem;
	}			
	
	/**
	 * 
	 * @param configItem
	 * @throws SQLException
	 */
	private void loadAttributes(ActualCI configItem) 
		throws SQLException {

		DefinitionProvider definitionProvider = provider.getDefinitionProvider();
		ActualCIDefinition definition = definitionProvider.getActualCIDefinition(configItem.getClassName());

		String sql = provider.getSQL(QuerySet.ACTCISPEC_SELECT_BY_ACTCINUM);	
		PreparedStatement statement = null;
		ResultSet resultSet = null;				
		try {
			statement =	provider.getConnection().prepareStatement(sql);
			statement.setString(1, configItem.getActcinum());
			resultSet = statement.executeQuery();
			ResultSetMetaData rsMetaData = resultSet.getMetaData();
			while (resultSet.next()) {

				ClassAttribute nextAttribute = new ClassAttribute(null);
				provider.loadProperties(resultSet, rsMetaData, nextAttribute);
				
				String attrName = nextAttribute.getName();
				AttributeDefinition attrDef = definition.getAttribute(attrName);
				
				String valueColumn = attrDef.getValueField();
				String valueType = attrDef.getJavaClassName();

				Object attrValue = null;
				if (valueType.equals(String.class.getCanonicalName())) {
					attrValue = resultSet.getString(valueColumn);
				} else if (valueType.equals(Double.class.getCanonicalName())) {
					attrValue = resultSet.getDouble(valueColumn);
				} else {
					attrValue = resultSet.getObject(valueColumn);
				}
				nextAttribute.setValue(attrValue);
				configItem.addAttribute(nextAttribute);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}				
	}			
	
	/**
	 * 
	 * @param guid
	 * @return boolean
	 * @throws SQLException
	 */
	public boolean exist(String guid) 
		throws SQLException {
		
		if (guid == null) {
			return false;
		}
		
		boolean found = false;		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String sql = provider.getSQL(QuerySet.ACTCI_SELECT_BY_GUID);
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, guid);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				found = true;	
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}	
		
		return found;
	}		
	
	/**
	 * 
	 * @param relationSet
	 * @throws SQLException
	 * @throws DataProcessingException
	 */
	private void saveRelationSet(RelationSet relationSet) 
		throws SQLException, DataProcessingException {	
		Set<String> relationNames = relationSet.getRelationNames();
		if (relationNames == null) {
			return;
		}
		
		ActciRelationProvider relationProvider = provider.getActciRelationProvider();
		for (String relationName : relationNames) {
			List<CIRelation> relationList = relationSet.getRelations(relationName);
			if (relationList == null) {
				continue;
			}
			
			for (CIRelation relation : relationList) {
				relationProvider.saveRelation(relation, false, true);
			}
		}
	}		
	
	/**
	 * 
	 * @param configItem
	 * @throws SQLException
	 */
	private void deleteRelations(ActualCI configItem) throws SQLException {
		String sql = provider.getSQL(QuerySet.ACTCIRELATION_DELETE_BY_GUID);
		PreparedStatement statement = null;
		try {
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, configItem.getGuid());
			statement.setString(2, configItem.getGuid());
			statement.executeUpdate();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}	
	
	/**
	 * 
	 * @param configItem
	 * @throws SQLException
	 */
	private void deleteAttributes(ActualCI configItem) throws SQLException {
		String sql = provider.getSQL(QuerySet.ACTCISPEC_DELETE_BY_ACTCINUM);
		PreparedStatement statement = null;
		try {
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, configItem.getActcinum());
			statement.executeUpdate();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}	
	
	/**
	 * 
	 * @param configItem
	 * @throws SQLException
	 */
	private void deleteConfigItem(ActualCI configItem) throws SQLException {
		String sql = provider.getSQL(QuerySet.ACTCI_DELETE_BY_GUID);
		PreparedStatement statement = null;
		try {
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, configItem.getGuid());
			statement.executeUpdate();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}	
	
	/**
	 * 
	 * @param configItem
	 * @throws SQLException
	 * @throws DataProcessingException
	 */
	private void updateActualCI(ActualCI configItem) 
		throws SQLException, DataProcessingException {	
	
		DefinitionProvider definitionProvider = provider.getDefinitionProvider();
		ActualCIDefinition definition = definitionProvider.getActualCIDefinition(configItem.getClassName());
		definitionProvider.loadSourceRelationRules(definition);
		definitionProvider.loadTargetRelationRules(definition);

        ActualCI oldConfigItem = findActualCIByGUID(configItem.getGuid());
        loadSourceRelations(oldConfigItem);
        loadTargetRelations(oldConfigItem);
        
        configItem.setProperty(CCMDBActualCISchema.ACTCISPEC_ACTCINUM, oldConfigItem.getActcinum());
        
		String changeby = (String) configItem.getProperty(CCMDBActualCISchema.ACTCI_CHANGEBY);
		if (changeby == null) {
			changeby = oldConfigItem.getStringProperty(CCMDBActualCISchema.ACTCI_CHANGEBY);
			configItem.setProperty(CCMDBActualCISchema.ACTCI_CHANGEBY, changeby);
		}
		Object changedate = configItem.getProperty(CCMDBActualCISchema.ACTCI_CHANGEDATE);
		if (changedate == null) {
			changedate = oldConfigItem.getProperty(CCMDBActualCISchema.ACTCI_CHANGEDATE);
			configItem.setProperty(CCMDBActualCISchema.ACTCI_CHANGEDATE, changedate);
		}
      
		Integer attrStartId = provider.getInteger(QuerySet.ACTCISPEC_SELECT_NEXT_ID);
        attrStartId += 1;
		Collection<AttributeDefinition> attributes = definition.getAttributes();
        for (AttributeDefinition attrDef : attributes) {
        	ClassAttribute attribute = configItem.getAttribute(attrDef.getName());
        	ClassAttribute oldAttribute = oldConfigItem.getAttribute(attrDef.getName());
        	if ((oldAttribute == null) && (attribute != null)) {
				attribute.setProperty(CCMDBActualCISchema.ACTCISPEC_ACTCISPECID, attrStartId++);
				attribute.setProperty(CCMDBActualCISchema.ACTCISPEC_CHANGEBY, changeby);
				attribute.setProperty(CCMDBActualCISchema.ACTCISPEC_CHANGEDATE, changedate);
				attribute.setProperty(CCMDBActualCISchema.ACTCISPEC_ACTCINUM, configItem.getActcinum());
				attribute.setProperty(CCMDBActualCISchema.ACTCISPEC_CLASSSTRUCTUREID, definition.getClassification().getClassstructureId());

				SQLQuery query = CCMDBActualCISchema.createInsertQuery(attrDef, attribute);
				provider.executeQuery(query);
        	} else if ((attribute == null) && (oldAttribute != null)) {
        		deleteAttribute(attrDef.getName(), configItem.getActcinum());
			} else if ((attribute != null) && (oldAttribute != null) && !attribute.hasSameValue(oldAttribute)) {
				attribute.setProperty(CCMDBActualCISchema.ACTCISPEC_ACTCINUM, configItem.getActcinum());
				SQLQuery query = CCMDBActualCISchema.createUpdateQuery(attrDef, attribute);
				provider.executeQuery(query);
			}
		}

		RelationSet srcRelationSet = configItem.getSourceRelations();		
		RelationSet trgRelationSet = configItem.getTargetRelations();
		
		updateRelationSet(srcRelationSet, oldConfigItem.getSourceRelations());
		updateRelationSet(trgRelationSet, oldConfigItem.getTargetRelations());
	}			
	
	/**
	 * 
	 * @param attributeName
	 * @param actcinum
	 * @throws SQLException
	 */
	private void deleteAttribute(String attributeName, String actcinum) 
		throws SQLException {

		PreparedStatement statement = null;
		try {
			String sql = provider.getSQL(QuerySet.ACTCISPEC_DELETE);
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, attributeName);
			statement.setString(2, actcinum);
			statement.executeUpdate();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}	
	}		
	
	/**
	 * 
	 * @param relationSet
	 * @param oldRelationSet
	 * @return boolean
	 * @throws SQLException
	 * @throws DataProcessingException
	 */
	private boolean updateRelationSet(RelationSet relationSet, RelationSet oldRelationSet) 
		throws SQLException, DataProcessingException {	
		
		ActciRelationProvider relationProvider = provider.getActciRelationProvider();
		
		if ((relationSet == null) || (relationSet.isEmpty())) {
			if ((oldRelationSet != null) && (!oldRelationSet.isEmpty())) {
				List<CIRelation> relations = oldRelationSet.getAllRelations();
				for (CIRelation relation : relations) {
					relationProvider.deleteRelation(relation);
				}
			}
			return true;
		}
		
		if ((oldRelationSet == null) || (oldRelationSet.isEmpty())) {
			if ((relationSet != null) && (!relationSet.isEmpty())) {
				List<CIRelation> relations = relationSet.getAllRelations();
				for (CIRelation relation : relations) {
					relationProvider.saveRelation(relation, false, true);
				}
			}
			return true;
		}		
		
		boolean updated = false;
		
		List<CIRelation> relations = relationSet.getAllRelations();
		for (CIRelation relation : relations) {
			if (!oldRelationSet.contains(relation)) {
				relationProvider.saveRelation(relation, false, true);
				updated = true;
			}
		}
		
		relations = oldRelationSet.getAllRelations();
		for (CIRelation relation : relations) {
			if (!relationSet.contains(relation)) {
				relationProvider.deleteRelation(relation);
				updated = true;
			}
		}		
		
		return updated;
	}	
	
	/**
	 * 
	 * @param configItem
	 * @throws SQLException
	 */
	private void loadSourceRelations(ActualCI configItem) 
		throws SQLException {

		RelationSet srcRelations = new RelationSet();
		List<CIRelation> relationList = getRelationList(configItem.getGuid(), 
				QuerySet.ACTCIRELATION_SELECT_BY_SRC);
						
		for (CIRelation relation : relationList) {
			relation.setSource(configItem);
			String guid = relation.getTargetGuid();
			ActualCI target = findActualCIByGUID(guid);
			relation.setTarget(target);
			srcRelations.addRelation(relation);
		}
			
		configItem.setSourceRelations(srcRelations);
	}		
	
	/**
	 * 
	 * @param configItem
	 * @throws SQLException
	 */
	private void loadTargetRelations(ActualCI configItem) 
		throws SQLException {

		RelationSet trgRelations = new RelationSet();
			List<CIRelation> relationList = getRelationList(configItem.getGuid(), 
					QuerySet.ACTCIRELATION_SELECT_BY_TRG);
						
		for (CIRelation relation : relationList) {
			relation.setTarget(configItem);
			String guid = relation.getSourceGuid();
			ActualCI source = findActualCIByGUID(guid);
			relation.setSource(source);
			trgRelations.addRelation(relation);
		}
			
		configItem.setTargetRelations(trgRelations);
	}		
	
	/**
	 * 
	 * @param guid
	 * @param queryName
	 * @return List<CIRelation>
	 * @throws SQLException
	 */
	private List<CIRelation> getRelationList(String guid, 
			String queryName) 
		throws SQLException {
		
		List<CIRelation> relations = new ArrayList<CIRelation>();
		
		String sql = provider.getSQL(queryName);
		PreparedStatement statement = null;
		ResultSet resultSet = null;				
		try {
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, guid);
			
			resultSet = statement.executeQuery();
			ResultSetMetaData metaData = resultSet.getMetaData();
			while (resultSet.next()) {
				CIRelation relation = new CIRelation();
				provider.loadProperties(resultSet, metaData, relation);
				relations.add(relation);
				relation.setClassName(relation.getRelationnum());
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}	
		
	    return relations;
	}		
	
	/**
	 * 
	 * @param configItem
	 * @throws SQLException
	 */
	private void loadDeletedActualCI(ActualCI configItem) 
		throws SQLException {

		String sql = provider.getSQL(QuerySet.CCIDELETEDACTCI_SELECT_BY_SRC);
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		try {
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, configItem.getGuid());
			
			resultSet = statement.executeQuery();
			if (resultSet.next()) {		
				ResultSetMetaData metaData = resultSet.getMetaData();
				ModelObject deletedActualCI = new ModelObject();
				provider.loadProperties(resultSet, metaData, deletedActualCI);
				configItem.setDeletedActualCI(deletedActualCI);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}			
	}
		
	/**
	 * 
	 * @param configItem
	 * @throws SQLException
	 */
	private void loadOMPRelation(ActualCI configItem) 
		throws SQLException {

		String sql = provider.getSQL(QuerySet.OMPCIRLN_SELECT_BY_ACTCIGUID);
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		
		OMPRelation ompRelation = null;
		try {
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, configItem.getGuid());
			
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				ResultSetMetaData metaData = resultSet.getMetaData();
				ompRelation = new OMPRelation();
				provider.loadProperties(resultSet, metaData, ompRelation);
			}
		}  finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}	
		
		if (ompRelation != null) {			
			sql = provider.getSQL(QuerySet.OMP_SELECT_BY_GUID);			
			try {
				statement = provider.getConnection().prepareStatement(sql);
				statement.setString(1, ompRelation.getOmpGuid());
				
				resultSet = statement.executeQuery();
				if (resultSet.next()) {
					ResultSetMetaData metaData = resultSet.getMetaData();
					ModelObject omp = new ModelObject();
					provider.loadProperties(resultSet, metaData, omp);
					ompRelation.setOmp(omp);
				}	
				configItem.setOmpRelation(ompRelation);	
			} finally {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
			}	
		}
	}							
	
}
