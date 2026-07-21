/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.provider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ibm.di.connector.dpa.schema.ClassDefinition;
import com.ibm.di.connector.dpa.schema.ClassInstance;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class SelectClassInstancesCommand {

	private DeployedAssetsProvider provider = null;
	
	/**
	 * 
	 * @param provider
	 */
	public SelectClassInstancesCommand(DeployedAssetsProvider provider) {
		this.provider = provider;
	}
	
	/**
	 * 
	 * @param assetClass
	 * @return List<ClassInstance>
	 * @throws SQLException
	 */
	public List<ClassInstance> find(String assetClass) 
		throws SQLException {
		
		String className = DeployedAssetsSchema.getClassName(assetClass);
		
		List<ClassInstance> instanceList = null;
		if ((className != null) && (!assetClass.equals(DeployedAssetsSchema.DEPLOYED_ASSET))) {
			instanceList = findByClass(assetClass); 
		} else {
			instanceList = findAll(); 
		}
		
		return instanceList;
	}
	
	/**
	 * 
	 * @param query
	 * @param assetClass
	 * @return List<ClassInstance>
	 * @throws SQLException
	 */
	public List<ClassInstance> find(SQLQuery query, String assetClass) 
		throws SQLException {

		List<ClassInstance> instanceList = new ArrayList<ClassInstance>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = provider.getConnection().prepareStatement(query.getSQL());
		    int paramCount = query.getParameterCount();
			if (paramCount > 0) {
				for (int i=0; i<paramCount; i++) {
					SQLUtilities.setValue(statement, i+1, query.getParameterValue(i));
				}
			}
			resultSet = statement.executeQuery();

			while (resultSet.next()) {
				int nodeId = resultSet.getInt(1);
				ClassDefinition classDefinition = provider.getClassdefFactory().getDefinition(assetClass);
				ClassInstance nextInstance = new ClassInstance(classDefinition);
				nextInstance.setPrimaryKeyValue(nodeId);
				instanceList.add(nextInstance);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}			
		
		return instanceList;
	}		
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @return List<ClassInstance>
	 * @throws SQLException
	 */
	private List<ClassInstance> findAll() 
		throws SQLException {
		
		List<ClassInstance> instanceList = new ArrayList<ClassInstance>();

		Statement statement = null;
		ResultSet resultSet = null;
		String sql = provider.getSQL(DeployedAssetsProvider.SQL_SELECT_DEPLOYEDASSET);

		try {
			statement = provider.getConnection().createStatement();
			resultSet = statement.executeQuery(sql);

			while (resultSet.next()) {
				int nodeId = resultSet.getInt(1);
				String className = resultSet.getString(2);
				String assetClass = DeployedAssetsSchema.getAssetClass(className);
				ClassDefinition classDefinition = provider.getClassdefFactory().getDefinition(assetClass);
				ClassInstance nextInstance = new ClassInstance(classDefinition);
				nextInstance.setPrimaryKeyValue(nodeId);
				instanceList.add(nextInstance);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}			
		
		return instanceList;
	}
	
	/**
	 * 
	 * @param className
	 * @return List<ClassInstance>
	 * @throws SQLException
	 */
	private List<ClassInstance> findByClass(String assetClass) 
		throws SQLException {
		
		ClassDefinition classDefinition = provider.getClassdefFactory().getDefinition(assetClass);
		
		List<ClassInstance> instanceList = new ArrayList<ClassInstance>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String sql = getSelectQuery(classDefinition);
        provider.getLog().debug("findByClass(...); Query: " + sql);
		try {
			statement = provider.getConnection().prepareStatement(sql);
			resultSet = statement.executeQuery();

			while (resultSet.next()) {
				ClassInstance nextInstance = new ClassInstance(classDefinition);
				int id = resultSet.getInt(1);
				nextInstance.setPrimaryKeyValue(id);
				instanceList.add(nextInstance);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}	
		
		return instanceList;
	}
		
	/**
	 * 
	 * @param classDefinition
	 * @return String
	 */
	public String getSelectQuery(ClassDefinition classDefinition) {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append(classDefinition.getPrimaryKey().getColumnName());
		sql.append(" FROM ");
		sql.append(classDefinition.getTable());
		return sql.toString();
	}
	
}
