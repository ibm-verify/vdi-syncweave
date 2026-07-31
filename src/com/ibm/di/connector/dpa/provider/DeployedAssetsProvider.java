/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.provider;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.ibm.di.connector.dpa.ConnectorLog;
import com.ibm.di.connector.dpa.DPAException;
import com.ibm.di.connector.dpa.schema.ClassDefinition;
import com.ibm.di.connector.dpa.schema.ClassDefinitionFactory;
import com.ibm.di.connector.dpa.schema.ClassInstance;
import com.ibm.di.connector.dpa.schema.PropertyDefinition;
import com.ibm.di.util.ResourceLocator;

/**
 * This class defines CRUD methods for deployed assets. 
 * 
 * @author yavor.gologanov
 *
 */
public class DeployedAssetsProvider {

	private static final String QUERY_FILE = "dpaqueries.xml";
	
 	protected static final String SQL_SELECT_DEPLOYEDASSET = "SELECT_DEPLOYEDASSET";
	
	/**
	 * The connection object for Maximo database.
	 */
	private Connection connection = null;
	
	/**
	 * 
	 */
	private Properties queryProperties = null;	
	
	/**
	 * 
	 */
	private boolean ignoreFieldErrors = true;	
	
	/**
	 * The JDBC URL used for connecting to the database.
	 */
	private String jdbcUrl;

	/**
	 * The user name used for connecting to the database.
	 */
	private String dbUsername;

	/**
	 * The password used for connecting to the database.
	 */
	private String dbPassword;		
	
	/**
	 * 
	 */
	private ClassDefinitionFactory classdefFactory = null;

	/**
	 * 
	 */
	private ConnectorLog log = null;
	
	/**
	 * 
	 * @param jdbcUrl
	 * @param dbUsername
	 * @param dbPassword
	 */
	public DeployedAssetsProvider(String jdbcUrl,
			String dbUsername,
			String dbPassword) {		
		this.jdbcUrl = jdbcUrl;
		this.dbUsername = dbUsername;
		this.dbPassword = dbPassword;
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException {
		URL url = ResourceLocator.getResourceURL(QUERY_FILE);
		queryProperties = new Properties();
		queryProperties.loadFromXML(url.openStream());
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
	 * @param classdefFactory
	 */
	public void setClassdefFactory(ClassDefinitionFactory classdefFactory) {
		this.classdefFactory = classdefFactory;
	}	
	
	/**
	 * 
	 * @return Connection
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		if (connection == null) {
			connection = DriverManager.getConnection(jdbcUrl, 
					dbUsername, 
					dbPassword);
			//fix for the defect 14929
			connection.setAutoCommit(false);
		}
		return connection;
	}

	/**
	 * 
	 * @throws SQLException
	 */
	public void closeConnection() throws SQLException {
		if (connection != null) {
			connection.commit();
			connection.close();
			connection = null;
		}		
	}
	

	/**
	 * 
	 * @return boolean
	 */
	public boolean isIgnoreFieldErrors() {
		return ignoreFieldErrors;
	}

	/**
	 * 
	 * @param ignoreFieldErrors
	 */
	public void setIgnoreFieldErrors(boolean ignoreFieldErrors) {
		this.ignoreFieldErrors = ignoreFieldErrors;
	}
	
	/**
	 * 
	 * @param assetClass
	 * @return Iterator<ClassInstance>
	 * @throws SQLException
	 */
	public Iterator<ClassInstance> selectAssets(String assetClass) 
		throws SQLException {

		SelectClassInstancesCommand command = new SelectClassInstancesCommand(this);
		List<ClassInstance> instanceList = command.find(assetClass);
				
		if ((instanceList != null) && (instanceList.size() > 0)) {
			return instanceList.iterator();
		}
		
		return null;
		
	}
	
	/**
	 * 
	 * @param query
	 * @param assetClass
	 * @return Iterator<ClassInstance>
	 * @throws SQLException
	 */
	public Iterator<ClassInstance> selectAssets(SQLQuery query, String assetClass) 
		throws SQLException {

		SelectClassInstancesCommand command = new SelectClassInstancesCommand(this);
		List<ClassInstance> instanceList = command.find(query, assetClass);
		
		if ((instanceList != null) && (instanceList.size() > 0)) {
			return instanceList.iterator();
		}	
		return null;
	}	
	
	/**
	 * 
	 * @param instance
	 * @param loadReferences
	 * @throws SQLException
	 * @throws IOException
	 */
	public void loadInstance(ClassInstance instance, boolean loadReferences) 
		throws SQLException, IOException {
		
		LoadClassInstanceCommand command = new LoadClassInstanceCommand(this);
		command.load(instance, loadReferences);
	}
	
	/**
	 * 
	 * @param instance
	 * @return boolean
	 * @throws SQLException
	 * @throws DPAException
	 */
	public boolean saveInstance(ClassInstance instance) 
		throws SQLException, DPAException {
		
		InsertClassInstanceCommand command = new InsertClassInstanceCommand(this);
		return command.insert(instance);
	}	
	
	/**
	 * 
	 * @param instance
	 * @return boolean
	 * @throws SQLException
	 * @throws IOException
	 * @throws DPAException
	 */
	public boolean deleteInstance(ClassInstance instance) 
		throws SQLException, IOException, DPAException {
		
		DeleteClassInstanceCommand command = new DeleteClassInstanceCommand(this);
		return command.delete(instance);
	}		
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @return ClassDefinitionFactory
	 */
	protected ClassDefinitionFactory getClassdefFactory() {
		return classdefFactory;
	}	
	
	/**
	 * 
	 * @param queryName
	 * @return String
	 */
	protected String getSQL(String queryName) {
		return queryProperties.getProperty(queryName);
	}	
	
	/**
	 * 
	 * @param instance
	 * @return boolean
	 * @throws SQLException
	 * @throws IOException
	 */
	protected boolean exists(ClassInstance instance) 
		throws SQLException, IOException {
		
		boolean exists = false;		
		ClassDefinition classDefinition = instance.getDefinition();
		if (classDefinition.getUniqueKey() != null) {
			Object pk = findByUniqueKey(instance);
			exists = (pk != null);
		} 		
		return exists;
	}
	
	/**
	 * 
	 * @param sql
	 * @param defaultValue
	 * @return Object
	 * @throws SQLException
	 */
	protected Object getObject(String sql, Object defaultValue) throws SQLException {

		Object result = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().createStatement();
			resultSet = statement.executeQuery(sql);

			if (resultSet.next()) {
				result = resultSet.getObject(1);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}			
		
		if (result != null) {
			return result;
		} else {
			return defaultValue;
		}
	}
	
	/**
	 * 
	 * @param query
	 * @return int
	 * @throws SQLException
	 */
	protected int executeQuery(SQLQuery query) throws SQLException {
		log.debug("EXECUTE QUERY: " + query);
		
		int result = 0;
		if (query.getParameterCount() == 0) {
			String sql = query.getSQL();		
			Statement statement = null;
			try {
				statement = getConnection().createStatement();
				statement.executeUpdate(sql);				
			} finally {
				if (statement != null) {
					statement.close();
				}
			}	
		} else {
			PreparedStatement statement = null;
			String sql = query.getSQL();
			try {
				statement = getConnection().prepareStatement(sql);
			    int paramCount = query.getParameterCount();
				if (paramCount > 0) {
					for (int i=0; i<paramCount; i++) {
						SQLUtilities.setValue(statement, i+1, query.getParameterValue(i));
					}
				}

				result = statement.executeUpdate();
			} finally {
				if (statement != null) {
					statement.close();
				}
			}			
		}
		return result;
	}		
	
	/**
	 * 
	 * @param instance
	 * @return Object
	 * @throws SQLException
	 */
	private Object findByUniqueKey(ClassInstance instance) throws SQLException {
		
		ClassDefinition classDefinition = instance.getDefinition();
		List<PropertyDefinition> uniqueKeyList = classDefinition.getUniqueKey();
		
		StringBuffer query = new StringBuffer();
		query.append("SELECT ").append(classDefinition.getPrimaryKey().getColumnName());
		query.append("\nFROM ").append(classDefinition.getTable());
		query.append("\nWHERE ");
		Iterator<PropertyDefinition> uniqueKeyIt = uniqueKeyList.iterator();
		while (uniqueKeyIt.hasNext()) {
			PropertyDefinition nextUK = uniqueKeyIt.next();
			String column = nextUK.getColumnName();
			Object value = instance.getProperty(nextUK.getName());
			query.append("\n\t").append(column).append(" = '");
			query.append(value).append("'");
			if (uniqueKeyIt.hasNext()) {
				query.append("\n\t AND");
			}
		}

		Object pk = getObject(query.toString(), null);
		return pk;
	}	
	
}
