/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.connector.ccmdb.provider.CCMDBActualCIProvider;
import com.ibm.di.connector.ccmdb.schema.base.CCMDBMetaData;
import com.ibm.di.connector.ccmdb.schema.cdm.CDMMetaData;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.idml.IdMLConstants;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;
import com.ibm.di.util.SchemaUtils;

/**
 * This Connector will read Actual Config Items and relations from MAXIMO
 * database. <br>
 * 
 * This Connector supports two modes: 
 * 	<ul> 
 * 		<li>Iterator</li> 
 * 		<li>Lookup</li> 
 * 	</ul>
 */
public class CCMDBConnector extends Connector implements ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Name of the Connector.
	 */
	private static final String CONN_NAME = "CCMDB Connector";

	/**
	 * Represents empty string.
	 */
	private static final String EMPTY_STRING = "";	
	
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "ccmdbconnector";

	/**
	 * The name of JDBC Url parameter from the Connector's configuration panel.
	 */
	private static final String PARAM_JDBC_URL = "jdbcUrl";

	/**
	 * The name of JDBC Drivers parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_JDBC_DRIVER = "jdbcDriver";

	/**
	 * The name of db username parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_DB_USERNAME = "dbUsername";

	/**
	 * The name of db password parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_DB_PASSWORD = "dbPassword";		

	/**
	 * The name of artifact type parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_ARTIFACT_TYPE = "artifactType";

	/**
	 * The name of class type parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_CLASS_TYPE = "classType";	
	
	/**
	 * The name of the Enable IDML Mode parameter from the Connector's
	 * configuration panel.
	 */
	private static final String PARAM_IDML_MODE = "idmlMode";	
	
	/**
	 * 
	 */
	private static final String PARAM_REL_SRC = "relSrc";
	
	/**
	 * 
	 */
	private static final String PARAM_REL_TRG = "relTrg";
	
	//-------------------------------------------------------------------------
	
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);	
	
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
	 * The type of artifact created by this Connector. Its values are either
	 * {@link IdMLConstants#ARTIFACT_CI} or
	 * {@link IdMLConstants#ARTIFACT_RELATIONSHIP}.
	 */
	private String artifactType;

	/**
	 * The class type of the created artifact. It is determined by CDM.
	 */
	private String classType;
	
	/**
	 * 
	 */
	boolean idmlMode = false;
	
	/**
	 * 
	 */
	private CCMDBActualCIProvider dataProvider = null;
	
	/**
	 * 
	 */
	private AbstractDataHandler	dataHandler = null;
	
	/**
	 * Constructor which populates the modes supported by the Connector.
	 */
	public CCMDBConnector() {
		super();
		Trace.entrymid(this, CONN_NAME);
		
		setName(CONN_NAME);
		setModes(new String[] { 
				ConnectorConfig.ITERATOR_MODE, 
				ConnectorConfig.LOOKUP_MODE,
				ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.UPDATE_MODE,
				ConnectorConfig.DELETE_MODE
			});
	
		Trace.exitmid(this, CONN_NAME);
	}	
		
	/**
	 * {@inheritDoc}
	 */
	public String getVersion() {
		return "1.0-di7.1.1 %I%, 20%E%";
	}
	
	/**
	 * Returns the username for connection to the Maximo database.
	 * 
	 * @return the dbUsername.
	 */
	public String getDbUsername() {
		return dbUsername;
	}

	/**
	 * Returns the JDBC driver for connection to the Maximo database.
	 * 
	 * @return the jdbcDriver.
	 */
	public String getJdbcDriver() {
		return jdbcDriver;
	}

	/**
	 * Returns the JDBC URL for connection to the Maximo database.
	 * 
	 * @return the jdbcUrl.
	 */
	public String getJdbcUrl() {
		return jdbcUrl;
	}	
	
	/**
	 * Returns the JDBC Connection to the Maximo database.
	 * 
	 * @return the Connection.
	 */
	public Connection getConnection() throws SQLException {
		if (dataProvider != null) {
			return dataProvider.getConnection();
		} else {
			return null;
		}
	}	
	
	/**
	 * 
	 * @param classification
	 * @return
	 * @throws Exception
	 */
	public String getCDMType(String classification) throws Exception {
		return CDMMetaData.getCDMType(classification);
	}
	
	/**
	 * 
	 * @param cdmType
	 * @return
	 */
	public String getClassification(String cdmType) {
		return CDMMetaData.getClassification(cdmType);
	}
	
	/**
	 * 
	 * @param artifactType
	 * @return List<String>
	 * @throws CCMDBException
	 */
	public List<String> getClassTypes(String artifactType) throws CCMDBException {
		return dataHandler.getClassifications(artifactType);
	}	
	
	/**
	 * This standard method initializes the Connector with values present in its
	 * Configuration panel. If database related parameters are not present in
	 * the Connector's configuration.
	 * 
	 * @param entry
	 * 			an initial entry provided to the Connector.
	 * @throws Exception
	 *          if a problem occurs.
	 */
	public void initialize(Object entry) throws Exception {

		ExecutionContext context = new ExecutionContext();
		
		artifactType = getStringParameter(PARAM_ARTIFACT_TYPE);
		if (artifactType != null) {
			printDebugMessage("CCMDB.CONN.PARAMETER.INITIALIZED", 
					new Object[] { PARAM_ARTIFACT_TYPE, artifactType });
		} else {
			throw new Exception(resHash.getString("CCMDB.CONN.PARAMETER.NOT.PROVIDED", 
					PARAM_ARTIFACT_TYPE));
		}
		context.setArtifactType(artifactType);

		String classTypeString = getStringParameter(PARAM_CLASS_TYPE);
		if (classTypeString != null && classTypeString.length() > 0) {
			classType = classTypeString;
			printDebugMessage("CCMDB.CONN.PARAMETER.INITIALIZED", 
					new Object[] { PARAM_CLASS_TYPE, classType });
		}
		
		context.setClassification(classType);
				
		Boolean param = getBoolean(PARAM_IDML_MODE);
		if (param != null) {
			idmlMode = param.booleanValue();
			printDebugMessage("CCMDB.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_IDML_MODE, param.booleanValue() });
		}
		
		param = getBoolean(PARAM_REL_SRC);
		if (param != null) {
			printDebugMessage("CCMDB.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_REL_SRC, param.booleanValue() });
			context.setLoadSrcRelations(param.booleanValue());
		}
		
		param = getBoolean(PARAM_REL_TRG);
		if (param != null) {
			printDebugMessage("CCMDB.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_REL_TRG, param.booleanValue() });
			context.setLoadTrgRelations(param.booleanValue());
		}		

		if (idmlMode && isLookupMode() && (IdMLConstants.ARTIFACT_RELATIONSHIP.equals(artifactType))) {
			throw new Exception(resHash.getString("CCMDB.CONN.UNSUPPORTED_MODE", 
					new Object[]{ConnectorConfig.LOOKUP_MODE, PARAM_ARTIFACT_TYPE}));
		}
		
		if (IdMLConstants.ARTIFACT_RELATIONSHIP.equals(artifactType) && isUpdateMode()) {
			throw new Exception(resHash.getString("CCMDB.CONN.UNSUPPORTED_MODE", 
					new Object[]{ConnectorConfig.LOOKUP_MODE, PARAM_ARTIFACT_TYPE}));
		}
		
		ConnectorLog log = new ConnectorLog() {

			public void logmsg(String msg) {
				CCMDBConnector.this.logmsg(msg);
			}

			public void debug(String msg) {
				CCMDBConnector.this.debug(msg);
			}

			public void logError(String msg) {
				CCMDBConnector.this.logError(msg);
			}
			
			public void logError(Exception e) {
				CCMDBConnector.this.logError(e.getMessage());
			}
			
		};	
		
		ConnectorConfig config = (ConnectorConfig) getConfiguration();
		if ((config.getMode().equals(ConnectorConfig.ADDONLY_MODE))
				|| (config.getMode().equals(ConnectorConfig.UPDATE_MODE))
				|| (config.getMode().equals(ConnectorConfig.DELETE_MODE))) {
			context.setLoadSrcRelations(true);
			context.setLoadTrgRelations(true);
		}
		
		context.setLog(log);		
		context.setConnectorMode(config.getMode());
		
		initDataProvider(context);
		initDataHandler(context);		
	}
		
	/**
	 * This method populates Output Map for CallReply mode and Input Map for
	 * Iterator\Lookup mode. The attributes for a CI\Relationship are fetched 
	 * from MAXIMO database.
	 * 
	 * @param arg0
	 *            an object parameter not used by this method.
	 * @return <code>null</code>, since the Connector handles the schema
	 *         population on its own.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Object querySchema(Object arg0) throws Exception {
		
		try {
			ConnectorConfig config = (ConnectorConfig) getConfiguration();

			// populate the Map
			SchemaUtils.convertEntryToSchemaHier(dataHandler.getSchema(), 
					config, 
					hasInputMap() );	
		} catch (Exception e) {
			debug(e.getMessage());
			throw e;
		}
		
		return null;
	}	
	
	/**
	 * This method will close Connection to the MAXIMO database. 
	 * It will also clear the counter for Iterator mode.
	 * 
	 * @throws Exception
	 *             if a problem occur.
	 */
	public void terminate() throws Exception {
		try {
			if (dataProvider != null) {
				dataProvider.closeConnection();
			}			
			
		} catch (SQLException sqe) {
			if (debugMode()) {
				debug(resHash.getString("CCMDB.CONN.UNABLE.TO.CLOSE", sqe.getMessage()));
			}
		}

		dataProvider = null;
		dataHandler = null;
		
		super.terminate();
	}

	/**
	 * Checks if with the currently configured properties, a connection to the
	 * MAXIMO database can be established.
	 * 
	 * @return if the connection was established successfully <b>null</b> is
	 *         returned, otherwise the method returns a String containing the
	 *         exception's text.
	 * @throws IOException
	 *             if a problem occurs.
	 */
	public String checkDbConnection() {
		String exceptionText = null;
		try {
			initDataProvider(new ExecutionContext());
			try {
				dataProvider.getConnection();
			} finally {
				dataProvider.closeConnection();
			}
		} catch (Exception ex) {
			exceptionText = ex.toString();
		}
		return exceptionText;
	}	
	
	/**
	 * This is preparation method for Iterator mode. This will fetch values of
	 * CI\Relationship from the MAXIMO database. 
	 * 
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public void selectEntries() throws Exception {
		dataHandler.selectEntries();
	}

	/**
	 * This method returns a single entry object for a CI\Relationship searched
	 * as per criteria. Refer to selectEntries for more details.
	 * 
	 * @return the next read entry.
	 */
	public Entry getNextEntry() throws Exception {
		Entry entry = dataHandler.getNextEntry();
		return entry;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Entry findEntry(SearchCriteria criteria) throws Exception {		
		clearFindEntries();				
		dataHandler.setSearchCriteria(criteria);

		selectEntries();
		Entry entry = getNextEntry();
		while (entry != null) {
			addFindEntry(entry);
			entry = getNextEntry();
		}

		if (getFindEntryCount() == 1) {
			return getFirstFindEntry();
		} else {
			return null;
		}
	}	
	
	/**
	 * {@inheritDoc}
	 */
	public void putEntry(Entry entry) throws Exception {
		if (entry == null) {
			throw new Exception(resHash.getString("CCMDB.CONN.NO.ENTRY.PROVIDED"));
		}
	
		dataHandler.addEntry(entry);
	}		
	
	/**
	 * 
	 */
	public void modEntry(Entry entry, SearchCriteria search, Entry oldEntry) throws Exception {
		if (entry == null) {
			throw new Exception(resHash.getString("CCMDB.CONN.NO.ENTRY.PROVIDED"));
		}

		dataHandler.updateEntry(entry);
	}
 
	/**
	 * 
	 */
	public void modEntry(Entry entry, SearchCriteria search) throws Exception {
		if (entry == null) {
			throw new Exception(resHash.getString("CCMDB.CONN.NO.ENTRY.PROVIDED"));
		}
	
		dataHandler.updateEntry(entry);
	}	
	
	/**
	 * 
	 */
	public void deleteEntry(Entry entry, SearchCriteria search) throws Exception {
		if (entry == null) {
			throw new Exception(resHash.getString("CCMDB.CONN.NO.ENTRY.PROVIDED"));
		}

		dataHandler.removeEntry(entry);
	}		
	
	//-------------------------------------------------------------------------
	
	/**
	 * Retrieves a value, specified by the user.
	 * 
	 * @param parameterName
	 *            name of the parameter , String.
	 * @return the value of the parameter.
	 */
	private String getStringParameter(String parameterName) {
		String parameter = getParam(parameterName);
		if (parameter != null) {
			parameter = parameter.trim();
		}
		return parameter;
	}	
	
	/**
	 * Prints a debug message if debug mode for the Components is enabled.
	 * 
	 * @param msgKey
	 *            message key
	 * @param params
	 *            place holder for debug messages
	 */
	private void printDebugMessage(String msgKey, Object[] params) {
		if (params == null || params.length == 0) {
			debug(resHash.getString(msgKey));
		} else if (params.length == 1) {
			debug(resHash.getString(msgKey, params[0]));
		} else {
			debug(resHash.getString(msgKey, params));
		}
	}		
	
	/**
	 * This method checks for Database parameters. It throws exception for
	 * missing database properties.
	 * 
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void initDataProvider(ExecutionContext context) throws Exception {		
		jdbcUrl = getStringParameter(PARAM_JDBC_URL);
		jdbcDriver = getStringParameter(PARAM_JDBC_DRIVER);
		dbUsername = getStringParameter(PARAM_DB_USERNAME);
		dbPassword = getStringParameter(PARAM_DB_PASSWORD);

		if (jdbcUrl == null || jdbcUrl.trim().equals(EMPTY_STRING)) {
			throw new Exception(resHash.getString("CCMDB.CONN.PARAMETER.NOT.PROVIDED", PARAM_JDBC_URL));
		} else {
			printDebugMessage("CCMDB.CONN.PARAMETER.INITIALIZED", 
					new Object[] { PARAM_JDBC_URL, jdbcUrl });
		}
		
		if (jdbcDriver == null || jdbcDriver.trim().equals(EMPTY_STRING)) {
			throw new Exception(resHash.getString("CCMDB.CONN.PARAMETER.NOT.PROVIDED", PARAM_JDBC_DRIVER));
		} else {
			printDebugMessage("CCMDB.CONN.PARAMETER.INITIALIZED", 
					new Object[] { PARAM_JDBC_DRIVER, jdbcDriver });
		}
		
		if ((dbUsername == null) || dbUsername.trim().equals(EMPTY_STRING)) {
			throw new Exception(resHash.getString("CCMDB.CONN.PARAMETER.NOT.PROVIDED", PARAM_DB_USERNAME));

		} else {
			printDebugMessage("CCMDB.CONN.PARAMETER.INITIALIZED", 
					new Object[] { PARAM_DB_USERNAME, dbUsername });
		}
		
		if (dbPassword == null || dbPassword.trim().equals(EMPTY_STRING)) {
			throw new Exception(resHash.getString("CCMDB.CONN.PARAMETER.NOT.PROVIDED", PARAM_DB_PASSWORD));

		} else {
			printDebugMessage("CCMDB.CONN.PARAMETER.INITIALIZED", 
					new Object[] { PARAM_DB_PASSWORD, IdMLConstants.PASSWORD_MASK });
		}
		
		dataProvider = new CCMDBActualCIProvider();
		context.setJdbcUrl(jdbcUrl);
		context.setJdbcDriver(jdbcDriver);
		context.setDbPassword(dbPassword);		
		context.setDbUsername(dbUsername);
		dataProvider.init(context);
		context.registerDataProvider(dataProvider);
	}	
	
	/**
	 * 
	 * @param context
	 * @throws Exception
	 */
	private void initDataHandler(ExecutionContext context) throws Exception {		
		AbstractMetaData metaData = null;
		if (idmlMode) {
			metaData = new CDMMetaData();
		} else {
			metaData = new CCMDBMetaData();
		}
		metaData.init(context);
		context.registerMetaData(metaData);
		
		if (IdMLConstants.ARTIFACT_CI.equals(artifactType)) {
			dataHandler = new ActualCIDataHandler();
		}		
		if (IdMLConstants.ARTIFACT_RELATIONSHIP.equals(artifactType)) {
			dataHandler = new CIRelationDataHandler();
		}		
		dataHandler.init(context);
	}	
	
	/**
	 * Checks if the Connector is in AddOnly mode.
	 * 
	 * @return whether the Connector is in AddOnly mode.
	 */
	private boolean isAddOnlyMode() {
		return ((ConnectorConfig) this.getConfiguration()).getMode().equals(ConnectorConfig.ADDONLY_MODE);
	}	
	
	/**
	 * Checks if the Connector is in Lookup mode.
	 * 
	 * @return whether the Connector is in Lookup mode.
	 */
	private boolean isLookupMode() {
		return ((ConnectorConfig) this.getConfiguration()).getMode().equals(ConnectorConfig.LOOKUP_MODE);
	}
	
	/**
	 * Checks if the Connector is in Lookup mode.
	 * 
	 * @return whether the Connector is in Lookup mode.
	 */
	private boolean isUpdateMode() {
		return ((ConnectorConfig) this.getConfiguration()).getMode().equals(ConnectorConfig.UPDATE_MODE);
	}
	
	/**
	 * Checks if the Connector has Input Map.
	 * 
	 * @return whether the Connector has Input Map.
	 */
	private boolean hasInputMap() {
		String mode = ((ConnectorConfig) this.getConfiguration()).getMode();
		return !(mode.equals(ConnectorConfig.UPDATE_MODE) || mode.equals(ConnectorConfig.ADDONLY_MODE));
	}	
	
	protected static ResourceHash getResHash() {
		return resHash;
	}
}
