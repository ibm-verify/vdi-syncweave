/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb;

import com.ibm.di.connector.ccmdb.provider.CCMDBActualCIProvider;
import com.ibm.di.fc.idml.IdMLConstants;

/**
 * This class keeps configuration information and references to main class instances 
 * associated with the current execution.
 * 
 * @author yavor.gologanov
 *
 */
public class ExecutionContext {

	private String artifactType = null;
	private String classification = null;	
	private String connectorMode = null;	
	private boolean loadSrcRelations = false;
	private boolean loadTrgRelations = false;	
	private ConnectorLog log = null;
	
	/**
	 * The CCMDBActualCIProvider instance associated with the current execution.
	 */
	private CCMDBActualCIProvider dataProvider = null;
	
	/**
	 * The AbstractMetaData implementation associated with the current execution.
	 */
	private AbstractMetaData metaData = null;
	
	/**
	 * The JDBC Driver used for connecting to the Maximo database.
	 */
	private String jdbcDriver;	
	
	/**
	 * The JDBC URL used for connecting to the Maximo database.
	 */
	private String jdbcUrl;

	/**
	 * The user name used for connecting to the Maximo database.
	 */
	private String dbUsername;

	/**
	 * The password used for connecting to the Maximo database.
	 */
	private String dbPassword;		
	
	/**
	 * 
	 * @return AbstractMetaData
	 */
	public AbstractMetaData getMetaData() {
		return metaData;
	}

	/**
	 * 
	 * @param metaData
	 */
	public void registerMetaData(AbstractMetaData metaData) {
		this.metaData = metaData;
	}

	/**
	 * 
	 * @return CCMDBActualCIProvider
	 */
	public CCMDBActualCIProvider getDataProvider() {
		return dataProvider;
	}
	
	/**
	 * 
	 * @param dataProvider
	 */
	public void registerDataProvider(CCMDBActualCIProvider dataProvider) {
		this.dataProvider = dataProvider;
	}		
	
	/**
	 * 
	 * @return String
	 */
	public String getArtifactType() {
		return artifactType;
	}
	
	/**
	 * 
	 * @param artifactType
	 */
	public void setArtifactType(String artifactType) {
		this.artifactType = artifactType;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getClassification() {
		if (classification == null) {
			if (artifactType.equals(IdMLConstants.ARTIFACT_CI)) {
				return CCMDBActualCIProvider.CLASS_ACTUAL_CI;
			} else if (artifactType.equals(IdMLConstants.ARTIFACT_RELATIONSHIP)) {
				return CCMDBActualCIProvider.CLASS_CI_RELATION;
			}
		}
		return classification;
	}
	
	/**
	 * 
	 * @param classification
	 */
	public void setClassification(String classification) {
		this.classification = classification;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getConnectorMode() {
		return connectorMode;
	}
	
	/**
	 * 
	 * @param connectorMode
	 */
	public void setConnectorMode(String connectorMode) {
		this.connectorMode = connectorMode;
	}
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isLoadSrcRelations() {
		return loadSrcRelations;
	}
	
	/**
	 * 
	 * @param loadSrcRelations
	 */
	public void setLoadSrcRelations(boolean loadSrcRelations) {
		this.loadSrcRelations = loadSrcRelations;
	}
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isLoadTrgRelations() {
		return loadTrgRelations;
	}
	
	/**
	 * 
	 * @param loadTrgRelations
	 */
	public void setLoadTrgRelations(boolean loadTrgRelations) {
		this.loadTrgRelations = loadTrgRelations;
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
	 * @return String
	 */
	public String getJdbcDriver() {
		return jdbcDriver;
	}

	/**
	 * 
	 * @param jdbcDriver
	 */
	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	/**
	 * 
	 * @return String
	 */
	public String getJdbcUrl() {
		return jdbcUrl;
	}

	/**
	 * 
	 * @param jdbcUrl
	 */
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	/**
	 * 
	 * @return String
	 */
	public String getDbUsername() {
		return dbUsername;
	}

	/**
	 * 
	 * @param dbUsername
	 */
	public void setDbUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}

	/**
	 * 
	 * @return String
	 */
	public String getDbPassword() {
		return dbPassword;
	}

	/**
	 * 
	 * @param dbPassword
	 */
	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}	
	
}
