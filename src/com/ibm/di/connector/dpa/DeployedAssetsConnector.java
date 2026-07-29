/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa;

import java.io.IOException;
import java.sql.SQLException;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.connector.dpa.provider.DeployedAssetsProvider;
import com.ibm.di.connector.dpa.provider.DeployedAssetsSchema;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.idml.IdMLConstants;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;
import com.ibm.di.util.SchemaUtils;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class DeployedAssetsConnector extends Connector implements ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String TYPE_ALL = "ALL";
	private static final String TYPE_COMPUTERS = "COMPUTERS";
	private static final String TYPE_NETWORK_DEVICES = "NETWORK_DEVICES";
	private static final String TYPE_NETWORK_PRINTERS = "NETWORK_PRINTERS";	
	
	/**
	 * Name of the Connector.
	 */
	private static final String CONN_NAME = "Deployed Assests Connector";

	/**
	 * Represents empty string.
	 */
	private static final String EMPTY_STRING = "";	
	
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "dpaconnector";

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
	 * The name of asset class parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_ASSET_TYPE = "assetType";	
	
	/**
	 * 
	 */
	private static final String PARAM_LOAD_REFERENCES = "loadReferences";	
	
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
	 * 
	 */
	private String assetType;
	
	/**
	 * 
	 */
	private boolean loadReferences;
	
	/**
	 * 
	 */
	private DeployedAssetsProvider dataProvider = null;
	
	/**
	 * 
	 */
	private AbstractDataHandler	dataHandler = null;
	
	/**
	 * Constructor which populates the modes supported by the Connector.
	 */
	public DeployedAssetsConnector() {
		super();
		Trace.entrymid(this, CONN_NAME);
		
		setName(CONN_NAME);
		setModes(new String[] { 
				ConnectorConfig.ITERATOR_MODE, 
				ConnectorConfig.LOOKUP_MODE,
				ConnectorConfig.DELETE_MODE,
				ConnectorConfig.ADDONLY_MODE
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
	 * Returns the Connector mode.
	 * 
	 * @return mode.
	 */
	public String getMode() {
		return ((ConnectorConfig) this.getConfiguration()).getMode();
	}
	
	/**
	 * Checks if the Connector is in AddOnly mode.
	 * 
	 * @return whether the Connector is in AddOnly mode.
	 */
	public boolean isAddOnlyMode() {
		return ((ConnectorConfig) this.getConfiguration()).getMode().equals(ConnectorConfig.ADDONLY_MODE);
	}
	
	/**
	 * This standard method initializes the Connector with values present in its
	 * Configuration panel. If database related parameters are not present in
	 * the Connector's configuration, they are taken from the
	 * {@link #PROPERTIES_FILE} file.
	 * 
	 * @param entry
	 * 			an initial entry provided to the Connector.
	 * @throws Exception
	 *          if a problem occurs.
	 */
	public void initialize(Object entry) throws Exception {
		
		String assetTypeString = getStringParameter(PARAM_ASSET_TYPE);
		if (assetTypeString != null && assetTypeString.length() > 0) {
			assetType = assetTypeString;
			printDebugMessage("DPA.CONN.PARAMETER.INITIALIZED", 
					new Object[] { PARAM_ASSET_TYPE, assetType });
		}
		
		Boolean param = getBoolean(PARAM_LOAD_REFERENCES);
		if (param != null) {
			loadReferences = param.booleanValue();
			printDebugMessage("DPA.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_LOAD_REFERENCES, param.booleanValue() });
		}
	
		ConnectorLog log = new ConnectorLog() {

			public void logmsg(String msg) {
				DeployedAssetsConnector.this.logmsg(msg);
			}

			public void debug(String msg) {
				DeployedAssetsConnector.this.debug(msg);
			}

			public void logError(String msg) {
				DeployedAssetsConnector.this.logError(msg);
			}
			
			public void logError(Exception e) {
				DeployedAssetsConnector.this.logError(e.getMessage());
			}
			
		};
		
		initDataProvider(log);
		initDataHandler(log);
		
	}
		
	/**
	 * This method populates Output Map for CallReply mode and Input Map for
	 * Iterator\Lookup mode. The attributes for a CI\Relationship are fetched 
	 * from Maximo database.
	 * 
	 * @param arg0
	 *            an object parameter not used by this method.
	 * @return <code>null</code>, since the Connector handles the schema
	 *         population on its own.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Object querySchema(Object arg0) throws Exception {
		
		ConnectorConfig config = (ConnectorConfig) getConfiguration();
		SchemaConfig schemaConfig = null;

		// populate the Output Map of the Connector, while leaving the Input Map
		// empty
		if (config.getMode().equalsIgnoreCase(ConnectorConfig.CALL_REPLY_MODE)) {
			// retrieve the Output Map
			schemaConfig = config.getSchema(false);
		} else {
			// retrieve the Input Map
			schemaConfig = config.getSchema(true);
		}

		// populate the Map
		schemaConfig.notifyChange(schemaConfig, EMPTY_STRING, MetamergeConfigChange.BEGIN_CHANGES);
		SchemaUtils.convertEntryToSchemaHier(dataHandler.getSchema(), config, !isAddOnlyMode());
        debug("QUERY SCHEMA: [" + dataHandler.getSchema().toString() + "]");
		return null;
	}	
	
	/**
	 * This method will close Connection to the Maximo database. 
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
				debug(resHash.getString("DPA.CONN.UNABLE.TO.CLOSE", sqe.getMessage()));
			}
		}

		dataProvider = null;
		dataHandler = null;
		
		super.terminate();
	}

	/**
	 * Checks if with the currently configured properties, a connection to the
	 * Maximo database can be established.
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
			initDataProvider(null);
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
	 * deployed assets from the Maximo database. 
	 * 
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public void selectEntries() throws Exception {
		dataHandler.selectEntries();
	}

	/**
	 * This method returns a single entry object for a deployed asset searched
	 * as per criteria. Refer to selectEntries for more details.
	 * 
	 * @return the next read entry.
	 */
	public Entry getNextEntry() throws Exception {
		Entry entry = dataHandler.getNextEntry(loadReferences);
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
	public void deleteEntry(Entry entry, SearchCriteria search) throws Exception {
		if (entry == null) {
			throw new Exception(resHash.getString("DPA.CONN.NO.ENTRY.PROVIDED"));
		}

		dataHandler.removeEntry(entry);
	}	
	
	/**
	 * {@inheritDoc}
	 */
	public void putEntry(Entry entry) throws Exception {
		if (entry == null) {
			throw new Exception(resHash.getString("DPA.CONN.NO.ENTRY.PROVIDED"));
		}
		
		dataHandler.addEntry(entry);
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
	 * This method checks for Database parameters. It throws exception for
	 * missing database properties.
	 * 
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void initDataProvider(ConnectorLog log) throws Exception {
		
		jdbcUrl = getStringParameter(PARAM_JDBC_URL);
		jdbcDriver = getStringParameter(PARAM_JDBC_DRIVER);
		dbUsername = getStringParameter(PARAM_DB_USERNAME);
		dbPassword = getStringParameter(PARAM_DB_PASSWORD);

		if (jdbcUrl == null || jdbcUrl.trim().equals(EMPTY_STRING)) {
			throw new Exception(resHash.getString("DPA.CONN.PARAMETER.NOT.PROVIDED", PARAM_JDBC_URL));
		} else {
			printDebugMessage("DPA.CONN.PARAMETER.INITIALIZED", 
					new Object[] { PARAM_JDBC_URL, jdbcUrl });
		}

		if (jdbcDriver == null || jdbcDriver.trim().equals(EMPTY_STRING)) {
			throw new Exception(resHash.getString("DPA.CONN.PARAMETER.NOT.PROVIDED", PARAM_JDBC_DRIVER));
		} else {
			printDebugMessage("DPA.CONN.PARAMETER.INITIALIZED", 
					new Object[] { PARAM_JDBC_DRIVER, jdbcDriver });
		}

		if ((dbUsername == null) || dbUsername.trim().equals(EMPTY_STRING)) {
			throw new Exception(resHash.getString("DPA.CONN.PARAMETER.NOT.PROVIDED", PARAM_DB_USERNAME));

		} else {
			printDebugMessage("DPA.CONN.PARAMETER.INITIALIZED", 
					new Object[] { PARAM_DB_USERNAME, dbUsername });
		}

		if (dbPassword == null || dbPassword.trim().equals(EMPTY_STRING)) {
			throw new Exception(resHash.getString("DPA.CONN.PARAMETER.NOT.PROVIDED", PARAM_DB_PASSWORD));

		} else {
			printDebugMessage("DPA.CONN.PARAMETER.INITIALIZED", 
					new Object[] { PARAM_DB_PASSWORD, IdMLConstants.PASSWORD_MASK });
		}

		Class.forName(jdbcDriver);
		dataProvider = new DeployedAssetsProvider(jdbcUrl, dbUsername, dbPassword);
		dataProvider.setLog(log);
		dataProvider.init();
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
	 * 
	 * @param log
	 * @throws Exception
	 */
	private void initDataHandler(ConnectorLog log) throws Exception {
		
		if (TYPE_ALL.equals(assetType)) {
			dataHandler = new DefaultDPADataHandler(DeployedAssetsSchema.DEPLOYED_ASSET);			
		} else if (TYPE_COMPUTERS.equals(assetType)) {
			dataHandler = new DefaultDPADataHandler(DeployedAssetsSchema.COMPUTER);			
		} else if (TYPE_NETWORK_DEVICES.equals(assetType)) {
			dataHandler = new DefaultDPADataHandler(DeployedAssetsSchema.NETWORK_DEVICE);
		} else if (TYPE_NETWORK_PRINTERS.equals(assetType)) {
			dataHandler = new DefaultDPADataHandler(DeployedAssetsSchema.NETWORK_PRINTER);
		}	
		dataHandler.setLog(log);
		
		AbstractMetaData metaData = new DefaultDPAMetaData();	
		metaData.setLog(log);
		
		metaData.init(((ConnectorConfig) this.getConfiguration()).getMode());
		dataHandler.init(metaData, dataProvider);
		dataProvider.setClassdefFactory(metaData.getClassDefinitionFactory());
	}	

}

