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
import java.util.List;

import com.ibm.di.connector.ccmdb.CCMDBException;
import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.CIRelation;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.Classification;
import com.ibm.di.server.ResourceHash;

/**
 * This class is responsible for reading and writing instances of relationships. 
 * 
 * @author yavor.gologanov
 *
 */
public class ActciRelationProvider {
	
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
	protected ActciRelationProvider(CCMDBActualCIProvider provider) {
		this.provider = provider;
	}			

	/**
	 * 
	 * @param definition
	 * @return List<CIRelation>
	 * @throws SQLException
	 */
	public List<CIRelation> select(CIRelationDefinition definition) 
		throws SQLException {
		
		Classification classification = definition.getClassification();
		if (classification.getClassName().equals(CCMDBActualCIProvider.CLASS_CI_RELATION)) {
			return selectAll();
		}
		
		List<CIRelation> relList = new ArrayList<CIRelation>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String sql = provider.getSQL(QuerySet.ACTCIRELATION_SELECT_BY_RELATIONNUM);		
		try {
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, classification.getClassName());
			resultSet = statement.executeQuery();
			
			ResultSetMetaData metaData = resultSet.getMetaData();
			while (resultSet.next()) {
				CIRelation nextRelation = new CIRelation();
				nextRelation.setClassName(classification.getClassName());
				provider.loadProperties(resultSet, metaData, nextRelation);
				relList.add(nextRelation);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}

		return relList;
	}	

	/**
	 * 
	 * @param query
	 * @param definition
	 * @return List<CIRelation>
	 * @throws SQLException
	 */
	public List<CIRelation> select(SQLQuery query, CIRelationDefinition definition) 
		throws SQLException {
		
		List<CIRelation> relList = new ArrayList<CIRelation>();
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
				CIRelation nextRelation = new CIRelation();
				provider.loadProperties(resultSet, metaData, nextRelation);
				relList.add(nextRelation);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
	
		return relList;
	}			
	
	/**
	 * 
	 * @param relation
	 * @return boolean
	 * @throws SQLException
	 * @throws CCMDBException
	 */
	public boolean save(CIRelation relation) 
		throws SQLException, DataProcessingException {
		
		if (exist(relation.getRelationnum(),
				relation.getSourceGuid(),
				relation.getTargetGuid())) {
			throw new DataProcessingException(resHash.getString("CCMDB.CONN.RELATION.EXISTS", new Object[]{relation.toString()}));
		}
		
		boolean inserted = false;
		provider.getConnection().setAutoCommit(false);
	
		try {			
			saveRelation(relation, true, false);			
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
	 * @param relation
	 * @return boolean
	 * @throws SQLException
	 * @throws DataProcessingException
	 */
	public boolean update(CIRelation relation) 
		throws SQLException, DataProcessingException {	
		
		boolean updated = false;
		provider.getConnection().setAutoCommit(false);
		
		try {			
			deleteRelation(relation);
			saveRelation(relation, true, false);					
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
	 * @param relation
	 * @return boolean
	 * @throws SQLException
	 * @throws DataProcessingException
	 */
	public boolean delete(CIRelation relation) 
		throws SQLException, DataProcessingException  {
		
		boolean deleted = false;
		provider.getConnection().setAutoCommit(false);
		
		try {			
			int deletedRecords = deleteRelation(relation);
			if (deletedRecords > 0) {
				 deleted = true;
			}
		} catch (Exception e) {
 			provider.getConnection().rollback();
 			provider.getContext().getLog().logError(e);
 			throw new DataProcessingException(e);
		}  finally {
			provider.getConnection().setAutoCommit(true);
		}
		
		return deleted;
	}		
	
	/**
	 * 
	 * @param relation
	 * @throws SQLException
	 */
	public void load(CIRelation relation) 
		throws SQLException {	

		ActciProvider actciProvider = provider.getActciProvider();
		
		String guid = relation.getSourceGuid();
		ActualCI source = actciProvider.findActualCIByGUID(guid);
		relation.setSource(source);
		
		guid = relation.getTargetGuid();
		ActualCI target = actciProvider.findActualCIByGUID(guid);
		relation.setTarget(target);
	}			
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param definition
	 * @return List<CIRelation>
	 * @throws SQLException
	 */
	private List<CIRelation> selectAll() 
		throws SQLException {
		
		List<CIRelation> relList = new ArrayList<CIRelation>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String sql = provider.getSQL(QuerySet.ACTCIRELATION_SELECT_ALL);		
		try {
			statement = provider.getConnection().prepareStatement(sql);
			resultSet = statement.executeQuery();
			
			ResultSetMetaData metaData = resultSet.getMetaData();
			while (resultSet.next()) {
				CIRelation nextRelation = new CIRelation();
				provider.loadProperties(resultSet, metaData, nextRelation);
				relList.add(nextRelation);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}

		return relList;
	}	
	
	/**
	 * 
	 * @param relation
	 * @throws SQLException
	 */
	protected int deleteRelation(CIRelation relation) 
		throws SQLException {

		PreparedStatement statement = null;
		try {
			String sql = provider.getSQL(QuerySet.ACTCIRELATION_DELETE);
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, relation.getRelationnum());
			statement.setString(2, relation.getSourceGuid());
			statement.setString(3, relation.getTargetGuid());
			return statement.executeUpdate();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}	
	}	
	
	/**
	 * 
	 * @param relation
	 * @param check
	 * @throws SQLException
	 * @throws DataProcessingException
	 */
	protected void saveRelation(CIRelation relation, boolean check, boolean skip) 
		throws SQLException, DataProcessingException {	
		
		ActualCI source = relation.getSource();
		ActualCI target = relation.getTarget();
				
		if (check || skip) {
			boolean exist = exist(relation.getRelationnum(),
					relation.getSourceGuid(), 
					relation.getTargetGuid());
			if (exist) {
				if (check) {
					throw new SQLException(resHash.getString("CCMDB.CONN.CI.RELATION.EXISTS", new Object[] {relation.toString()}));
				}
			
				if (skip) {
					return;
				}
			}
		}	
			
		ActciProvider actciProvider = provider.getActciProvider();
		source = actciProvider.saveActualCI(source, false, true);
		target = actciProvider.saveActualCI(target, false, true);
		
		Integer actcirelationid = provider.getInteger(QuerySet.ACTCIRELATION_SELECT_NEXT_ACTCIRELID);
		relation.setProperty(CCMDBActualCISchema.ACTCIRELATION_ACTCIRELATIONID, actcirelationid + 1);
		relation.setProperty(CCMDBActualCISchema.ACTCIRELATION_SOURCECIGUID, source.getGuid());
		relation.setProperty(CCMDBActualCISchema.ACTCIRELATION_SOURCECI, source.getActcinum());
		relation.setProperty(CCMDBActualCISchema.ACTCIRELATION_TARGETCIGUID, target.getGuid());
		relation.setProperty(CCMDBActualCISchema.ACTCIRELATION_TARGETCI, target.getActcinum());
		
		DefinitionProvider definitionProvider = provider.getDefinitionProvider();
		CIRelationDefinition definition = definitionProvider.getCIRelationDefinition(relation.getClassName());
		definitionProvider.loadRelationRules(definition);
		
		SQLQuery insertQuery = CCMDBActualCISchema.createInsertQuery(relation, definition);
		provider.executeQuery(insertQuery);
	}		
	
	/**
	 * 
	 * @param relationName
	 * @param sourceGuid
	 * @param targetGuid
	 * @return boolean
	 * @throws SQLException
	 */
	private boolean exist(String relationName, String sourceGuid, String targetGuid) 
		throws SQLException {
		
		boolean found = false;		
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try {
			String sql = provider.getSQL(QuerySet.ACTCIRELATION_SELECT_BY_RST);
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, relationName);
			statement.setString(2, sourceGuid);
			statement.setString(3, targetGuid);			
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

	public CCMDBActualCIProvider getProvider() {
		return provider;
	}	
	
	
	
}
