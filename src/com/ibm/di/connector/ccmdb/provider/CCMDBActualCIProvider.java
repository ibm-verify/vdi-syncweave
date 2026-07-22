/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.provider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.InvalidPropertiesFormatException;

import com.ibm.di.connector.ccmdb.ExecutionContext;
import com.ibm.di.connector.ccmdb.model.ModelObject;

/**
 * This class defines CRUD methods for Actual CIs and relationships. 
 * It is responsible for connection to the CCMDB database. 
 * 
 * @author yavor.gologanov
 *
 */
public class CCMDBActualCIProvider {

	public static final String ITDI = "ITDI";
	
	public static final String CLASS_ACTUAL_CI = "ActualCI";
	public static final String CLASS_CI_RELATION = "CIRelation";	
	public static final String CLASSS_DELETED_ACTUAL_CI = "DeletedActualCI";
	public static final String CLASS_OMPRELATION = "OMPRelation";
	public static final String CLASS_OMP = "OMP";
		
	//-------------------------------------------------------------------------
	
	/**
	 * The connection object for Maximo database.
	 */
	private Connection connection = null;
	
	/**
	 * 
	 */
	private boolean ignoreFieldErrors = true;	
	
	/**
	 * 
	 */
	private DefinitionProvider definitionProvider = null;
	
	/**
	 * 
	 */
	private ClassificationProvider classificationProvider = null;
	
	/**
	 * 
	 */
	private ActciProvider actciProvider = null;
	
	/**
	 * 
	 */
	private ActciRelationProvider actciRelationProvider = null;
	
	/**
	 * 
	 */
	private ExecutionContext context = null;
	
	/**
	 * 
	 */
	private QuerySet querySet = null;
	
	/**
	 * 
	 * @param context
	 * @throws ClassNotFoundException
	 * @throws InvalidPropertiesFormatException
	 * @throws IOException
	 */
	public void init(ExecutionContext context) 
		throws ClassNotFoundException, 
			InvalidPropertiesFormatException, 
			IOException,
			SQLException {
		this.context = context;	
		
		Class.forName(context.getJdbcDriver());
		querySet = new QuerySet();
		
		definitionProvider = new DefinitionProvider(this);
		
		classificationProvider = new ClassificationProvider(this);
		classificationProvider.init();
		
		actciProvider = new ActciProvider(this);
		actciRelationProvider = new ActciRelationProvider(this);
	}
	
	/**
	 * 
	 * @return Connection
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		if (connection == null) {
			connection = DriverManager.getConnection(context.getJdbcUrl(), 
					context.getDbUsername(), 
					context.getDbPassword());
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
	 * @return ExecutionContext
	 */
	public ExecutionContext getContext() {
		return context;
	}
	
	/**
	 * 
	 * @return ClassificationProvider
	 */
	public ClassificationProvider getClassificationProvider() {
		return classificationProvider;
	}
	
	/**
	 * 
	 * @return DefinitionProvider
	 */
	public DefinitionProvider getDefinitionProvider() {
		return definitionProvider;
	}	
	
	/**
	 * 
	 * @return ActciRelationProvider
	 */
	public ActciRelationProvider getActciRelationProvider() {
		return actciRelationProvider;
	}
		
	/**
	 * 
	 * @return ActciProvider
	 */
	public ActciProvider getActciProvider() {
		return actciProvider;
	}	
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param queryName
	 * @return String
	 */
	protected String getSQL(String queryName) {
		return querySet.getSQLQuery(queryName);
	}
							
	/**
	 * 
	 * @param columnName
	 * @param columnType
	 * @param resultSet
	 * @return Object
	 * @throws SQLException
	 */
	protected Object getValue(String columnName, 
			int columnType, 
			ResultSet resultSet) 
		throws SQLException {

		Object val = null;

		switch (columnType) {

		// String & Character Data
		case Types.CHAR:
			
		case Types.VARCHAR:
			val = resultSet.getString(columnName);
			break;
			
		// Probably NCHAR or NVARCHAR ?
		case Types.OTHER:
			val = resultSet.getString(columnName);
			break;
			
		// Numbers
		case Types.INTEGER:
		case Types.TINYINT:
		case Types.SMALLINT:
			val = Integer.valueOf(resultSet.getInt(columnName));

			if (resultSet.wasNull()) {
				val = null;
			}
			break;

		// Real & Float
		case Types.BIGINT:
		case Types.DECIMAL:
		case Types.NUMERIC:
			val = resultSet.getObject(columnName);

			if (resultSet.wasNull()) {
				val = null;
			}
			break;
			
		case Types.REAL:
		case Types.FLOAT:
		case Types.DOUBLE:
			val = new java.lang.Double(resultSet.getDouble(columnName));
			if (resultSet.wasNull()) {
				val = null;
			}
			break;
			
		// Boolean
		case Types.BIT:
			val = Boolean.valueOf(resultSet.getBoolean(columnName));
			break;
			
		// Date/Time
		case Types.DATE:
			val = resultSet.getDate(columnName);
			break;			
		case Types.TIME:
			val = resultSet.getTime(columnName);
			break;			
		case Types.TIMESTAMP:
			
		case 11:
			val = resultSet.getTimestamp(columnName);
			break;
			
		case Types.VARBINARY:
			try {
				val = resultSet.getBytes(columnName);
			} catch (Exception ignore) {
				val = resultSet.getObject(columnName);
			}
			break;

		case Types.BINARY:
		case Types.JAVA_OBJECT:
		case Types.DISTINCT:
		case Types.STRUCT:
		case Types.ARRAY:

		case Types.REF:
		default:
			val = resultSet.getObject(columnName);
			break;
		}

		return val;
	}	
		
	/**
	 * 
	 * @param statement
	 * @param paramIndex
	 * @param value
	 * @throws SQLException
	 */
	protected void setValue(PreparedStatement statement,
			int paramIndex, 
			Object value) 
		throws SQLException {

		if (value == null) {
			statement.setNull(paramIndex, Types.VARCHAR);
			return;
		}
		
		if (value instanceof String) {
			statement.setString(paramIndex, (String) value); 
		} else if (value instanceof Integer) {
			statement.setInt(paramIndex, (Integer) value); 
		} else if (value instanceof Double) {
			statement.setDouble(paramIndex, (Double) value); 
		} else if (value instanceof Timestamp) {
			statement.setTimestamp(paramIndex, (Timestamp) value); 
		} else {
			statement.setObject(paramIndex, value);
		}
	}		
	
	/**
	 * 
	 * @param query
	 * @throws SQLException
	 */
	protected void executeQuery(SQLQuery query) throws SQLException {
		context.getLog().debug("EXECUTE STATEMENT: " + query);
		
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
						setValue(statement, i+1, query.getParameterValue(i));
					}
				}

				statement.executeUpdate();
			} finally {
				if (statement != null) {
					statement.close();
				}
			}			
		}
	}	
		
	/**
	 * 
	 * @param queryName
	 * @return Integer
	 * @throws SQLException
	 */
	protected Integer getInteger(String queryName) 
		throws SQLException {
		
		String query = querySet.getSQLQuery(queryName);
		Integer result = 0;		
		Statement statement = null;
		ResultSet resultSet = null;			
		try {
			statement = getConnection().createStatement();
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				result = resultSet.getInt(1);
			} 
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}	
		
		return result;
	} 			
	  	
	/**
	 * 
	 * @param resultSet
	 * @param metaData
	 * @param object
	 * @throws SQLException
	 */
	protected void loadProperties(ResultSet resultSet, 
			ResultSetMetaData metaData,
			ModelObject object) 
		throws SQLException {	
				
		for (int i = 1; i <= metaData.getColumnCount(); i++) {		
			String columnName = metaData.getColumnName(i);
			Object columnValue = getValue(columnName, 
					metaData.getColumnType(i), 
					resultSet);		
			object.setProperty(columnName, columnValue);
		}
	}	
	
}
