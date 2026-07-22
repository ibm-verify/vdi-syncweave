/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.ibm.di.cdm.core.MetaData;
import com.ibm.di.cdm.core.MetaDataFactory;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.idml.IdMLConstants;
import com.ibm.di.fc.idml.ItdiBook;
import com.ibm.di.fc.idml.ItdiBookMapper;
import com.ibm.di.fc.idml.IdMLConstants.Operations;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;
import com.ibm.di.util.SchemaUtils;

/**
 * This Connector is used for adding Configuration Items(CIs)/ Relationships to
 * an IdML book.
 */
public class IdMLConnector extends Connector implements ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "idmlconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Component name.
	 */
	private static final String CONN_NAME = "IdML Ci And Relationship Connector";

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
	 * The name of book name parameter from the Connector's configuration panel.
	 */
	private static final String PARAM_BOOK_NAME = "bookName";

	/**
	 * The name of the parameter from the Connector's configuration panel that
	 * determines whether IT registry should be used for CDM meta-data
	 * definitions.
	 */
	private static final String PARAM_USE_IT_REGISTRY_CDM = "useITRegistryCdm";

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
	 * The name of the IdML book, this Component works with.
	 */
	private String bookName;

	/**
	 * The operation with which the Connector will add a CI/Relationship.
	 */
	private IdMLConstants.Operations operationType;

	/**
	 * Determines whether an IT registry should be used for the CDM meta-data
	 * definitions. By default they are obtained from a jar file.
	 */
	private String useITRegistry;

	/**
	 * The type of artifact created by this Connector. Its values are either
	 * {@link IdMLConstants#ARTIFACT_CI} or
	 * {@link IdMLConstants#ARTIFACT_RELATIONSHIP}.
	 */
	private String artifactType;

	/**
	 * the class type of the created artifact. It is determined by CDM.
	 */
	private String classType;

	/**
	 * The JDBC URL used for connecting to the IT registry.
	 */
	private String jdbcUrl;

	/**
	 * The JDBC Driver used for connecting to the IT registry.
	 */
	private String jdbcDriver;

	/**
	 * The user name used for connecting to the IT registry.
	 */
	private String dbUsername;

	/**
	 * The password used for connecting to the IT registry.
	 */
	private String dbPassword;

	/**
	 * Constructor. Initializes the connector to work in CallReply mode.
	 */
	public IdMLConnector() {
		super();
		Trace.entrymid(this, "IdMLConnector");
		setName(CONN_NAME);
		setModes(new String[] { ConnectorConfig.CALL_REPLY_MODE });
		Trace.exitmid(this, "IdMLConnector");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(final Object obj) throws Exception {
		// read configuration parameters
		bookName = getStringParameter(PARAM_BOOK_NAME);
		if (bookName != null) {
			printDebugMessage("IDML.CONN.BOOKNAME.INITIALIZED", new Object[] { bookName });
		}

		artifactType = getStringParameter(PARAM_ARTIFACT_TYPE);
		if (artifactType != null) {
			printDebugMessage("IDML.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_ARTIFACT_TYPE, artifactType });
		} else {
			throw new Exception(resHash.getString("IDML.CONN.PARAMETER.NOT.PROVIDED", PARAM_ARTIFACT_TYPE));
		}

		classType = getStringParameter(PARAM_CLASS_TYPE);
		if (classType != null) {
			printDebugMessage("IDML.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_CLASS_TYPE, classType });
		} else {
			throw new Exception(resHash.getString("IDML.CONN.PARAMETER.NOT.PROVIDED", PARAM_CLASS_TYPE));
		}

		useITRegistry = getStringParameter(PARAM_USE_IT_REGISTRY_CDM);
		if (useITRegistry != null) {
			printDebugMessage("IDML.CONN.USE.IT.REGISTRY", new Object[] { useITRegistry });
		} else {
			throw new Exception(resHash.getString("IDML.CONN.PARAMETER.NOT.PROVIDED", PARAM_USE_IT_REGISTRY_CDM));
		}

		// if IT registry should be used read the parameters for
		// accessing it
		if ("true".equals(useITRegistry)) {
			jdbcUrl = getStringParameter(PARAM_JDBC_URL);
			jdbcDriver = getStringParameter(PARAM_JDBC_DRIVER);
			dbUsername = getStringParameter(PARAM_DB_USERNAME);
			dbPassword = getStringParameter(PARAM_DB_PASSWORD);

			if (jdbcUrl == null || jdbcUrl.equals("") || jdbcDriver == null || jdbcDriver.equals("") || dbUsername == null
					|| dbUsername.equals("") || dbPassword == null || dbPassword.equals("")) {
				String[] itRegistryProperties = getPropertiesFromFile(IdMLConstants.IT_REGISTRY_PROPERTIES_FILE);

				if (jdbcUrl == null || jdbcUrl.equals("")) {
					jdbcUrl = itRegistryProperties[0];
				}
				if (jdbcDriver == null || jdbcDriver.equals("")) {
					jdbcDriver = itRegistryProperties[1];
				}
				if (dbUsername == null || dbUsername.equals("")) {
					dbUsername = itRegistryProperties[2];
				}
				if (dbPassword == null || dbPassword.equals("")) {
					dbPassword = itRegistryProperties[3];
				}
			}
			printDebugMessage("IDML.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_JDBC_URL, jdbcUrl });
			printDebugMessage("IDML.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_JDBC_DRIVER, jdbcDriver });
			printDebugMessage("IDML.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_DB_USERNAME, dbUsername });
			printDebugMessage("IDML.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_DB_PASSWORD, IdMLConstants.PASSWORD_MASK });

			Class.forName(jdbcDriver);
		}

	}

	/**
	 * Adds the CI/Relationship to the IdML book. The Connector first checks if
	 * any of its primary parameters are not overridden.
	 * 
	 * @param aEntry
	 *            the work entry passed to the Connector.
	 * @return an Entry object that can contain the id of the CI/Relationship
	 *         registered by the Connector.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	@Override
	public Entry queryReply(Entry aEntry) throws Exception {
		ItdiBook book = null;

		// The name of the IdML book has been overridden.
		String newBookName = aEntry.getString(IdMLConstants.BOOK_NAME_ATTR);
		if (newBookName != null && !bookName.equals(newBookName)) {
			// The Component should work with a different book
			String oldBookName = bookName;
			bookName = newBookName;
			printDebugMessage("IDML.CONN.BOOKNAME.OVERRIDDEN", new Object[] { oldBookName, newBookName });
		}

		if (bookName == null) {
			throw new Exception(resHash.getString("IDML.CONN.PARAMETER.NOT.PROVIDED", PARAM_BOOK_NAME));
		}

		// Check if the book is statically shared
		book = ItdiBookMapper.getBook(bookName);

		String artifactId = null;
		if (book != null && book.isOpened()) {
			// The book has been opened

			// determine the 'operation' this Component will perform
			final char operationChar = aEntry.getOp();
			switch (operationChar) {
			case Entry.OP_ADD:
				operationType = Operations.CREATE;
				break;
			case Entry.OP_MOD:
				operationType = Operations.MODIFY;
				break;
			case Entry.OP_DEL:
				operationType = Operations.DELETE;
				break;
			default:
				String attribute = aEntry.getString(Operations.PARAM_NAME);
				if (attribute != null) {
					try {
						operationType = Operations.valueOf(attribute.toUpperCase());
					} catch (IllegalArgumentException iae) {
						throw new Exception(resHash.getString("IDML.CONN.INCORRECT.ATTR.VALUE", new Object[] {
								Operations.PARAM_NAME, attribute }), iae);
					}
				}
			}
			if (operationType == null) {
				operationType = Operations.CREATE;
			}
			printDebugMessage("IDML.CONN.PARAMETER.INITIALIZED", new Object[] { Operations.PARAM_NAME, operationType });

			if (book.isRefreshMode() && operationType != Operations.CREATE) {
				throw new Exception(resHash.getString("IDML.CONN.UNSUPPORTED.OPERATION"));
			}

			// add to the IdML
			if (IdMLConstants.ARTIFACT_CI.equals(artifactType)) {
				artifactId = book.addConfigurationItem(classType, operationType, aEntry);
			} else if (IdMLConstants.ARTIFACT_RELATIONSHIP.equals(artifactType)) {
				book.addRelationship(classType, operationType, aEntry);
			}
		} else {
			throw new Exception(resHash.getString("IDML.CONN.BOOK.ALREADY.CLOSED", new Object[] { bookName }));
		}

		final Entry returnEntry = new Entry();
		if (artifactId != null) {
			returnEntry.setAttribute(IdMLConstants.ID_ATTR, artifactId);
		}
		return returnEntry;
	}

	/**
	 * This method displays the attributes supported by a chosen
	 * CI/Relationship. For a source of this meta-data it uses either a local
	 * jar file or meta-data calls to an IT registry (depending on its
	 * configuration).
	 * 
	 * @param input
	 *            entry object
	 * @return <b>null</b>, since this Components has itself populated the
	 *         Schemas.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	@Override
	public Object querySchema(Object input) throws Exception {

		// populate the Output Map of the Connector, while leaving the Input Map
		// empty
		Vector<Entry> listedAttributes = new Vector<Entry>();
		MetaData metaData = null;
		if (classType != null && !classType.equals("")) {
			if ("false".equals(useITRegistry)) {
				// using a jar file for meta data
				metaData = MetaDataFactory.getJarMetaData();
			} else {
				// using the specified IT registry for meta data
				metaData = MetaDataFactory.getITRegistryMetaData(jdbcUrl, jdbcDriver, dbUsername, dbPassword);
			}
			listedAttributes.addAll(metaData.getAttributes(artifactType, classType));

			if (artifactType.equalsIgnoreCase(IdMLConstants.ARTIFACT_CI)) {
				// add the $id attribute if the user want to override the
				// auto generated numeric CI ids
				Entry idAttribute = new Entry();
				idAttribute.addAttributeValue("name", IdMLConstants.ID_ATTR);
				idAttribute.addAttributeValue("syntax", String.class.getName());
				listedAttributes.add(idAttribute);
			}
		} else {
			throw new Exception(resHash.getString("IDML.CONN.NO.CLASSTYPE.SPECIFIED"));
		}

		ConnectorConfig config = (ConnectorConfig) getConfiguration();
		// populate the Output Map
		SchemaConfig sc = config.getSchema(false);
		sc.notifyChange(sc, "", MetamergeConfigChange.BEGIN_CHANGES);
		for (Entry e : listedAttributes) {
			try {
				SchemaUtils.addSchemaItem(sc, e.getString("name"), e.getString("syntax"), null);
			} catch (Exception ex) {
				// One of the schema items had an incorrect name.
				// It will just be skipped during the schema population.
				ex.printStackTrace();
			}
		}
		sc.notifyChange(sc, "", MetamergeConfigChange.END_CHANGES);

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersion() {
		return "1.0-di7.1.1 %I%, 20%E%";
	}

	/**
	 * This method returns the JDBC Url used by the Connector.
	 * 
	 * @return a JDBC Url.
	 */
	public String getJdbcUrl() {
		return jdbcUrl;
	}

	/**
	 * This method returns the JDBC Driver used by the Connector.
	 * 
	 * @return a JDBC Driver.
	 */
	public String getJdbcDriver() {
		return jdbcDriver;
	}

	/**
	 * This method returns the username for connecting to the IT registry used
	 * by the Connector.
	 * 
	 * @return a database username.
	 */
	public String getDbUsername() {
		return dbUsername;
	}

	/**
	 * Checks if with the currently configured IT registry properties, a
	 * connection to the IT registry can be established.
	 * 
	 * @return if the connection was established successfully <b>null</b> is
	 *         returned, otherwise the method returns a String containing the
	 *         exception's text.
	 */
	public String checkDbConnection() {
		String exceptionText = null;
		try {
			Connection conn = null;
			try {
				conn = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		} catch (Exception ex) {
			exceptionText = ex.toString();
		}
		return exceptionText;
	}

	/**
	 * Returns the version of the CDM that this Connector is using. depending on
	 * its configuration this can be either the version of CDM that IT registry
	 * is using or the version of the CDM meta-data stored in the locally used
	 * jar file.
	 * 
	 * @return the CDM version, with format
	 *         '&ltversion&gt.&ltrelease&gt.&ltmodifier&gt'.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public String getCdmVersion() throws Exception {
		MetaData metadata = null;

		if ("true".equals(useITRegistry)) {
			metadata = MetaDataFactory.getITRegistryMetaData(jdbcUrl, jdbcDriver, dbUsername, dbPassword);
		} else {
			metadata = MetaDataFactory.getJarMetaData();
		}

		return metadata.getCdmVersion();
	}

	/**
	 * Returns the types of CIs/Relationships available for this Connector.
	 * Depending on its Configuration we can get the names of all supported CIs,
	 * or of all Relationships.
	 * 
	 * @return a List with all CI/Relationship types.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Map<String, Object> getTypes() throws Exception {
		MetaData metadata = null;

		if ("true".equals(useITRegistry)) {
			metadata = MetaDataFactory.getITRegistryMetaData(jdbcUrl, jdbcDriver, dbUsername, dbPassword);
		} else {
			metadata = MetaDataFactory.getJarMetaData();
		}

		return metadata.getTypes(artifactType);
	}

	/**
	 * Returns the current contents of the in-memory IdML book and emties the
	 * buffer.
	 * 
	 * @return the current contents of the in-memory book.
	 * @throws IOException
	 *             if a problem occurs.
	 */
	public String resetBook() throws IOException {
		// Check if the book is statically shared
		ItdiBook book = ItdiBookMapper.getBook(bookName);
		String bookContents = null;
		if (book != null && book.isOpened()) {
			bookContents = book.getContents();
			book.reset();
		}
		return bookContents;
	}

	/**
	 * Read the properties for connecting to an IT registry from the file
	 * {@link IdMLConstants#IT_REGISTRY_PROPERTIES_FILE}, and return them as an
	 * array of Strings..
	 * 
	 * @param file
	 *            the name of the file containing the needed properties.
	 * @return an array with four strings holding the IT registry properties.
	 *         They are: [0] -> JDBC URL, [1] -> JDBC Driver, [2] -> Username,
	 *         [3] -> Password.
	 * @throws FileNotFoundException
	 *             if the properties file cannot be found.
	 * @throws IOException
	 *             if there is a problem reading the properties file.
	 */
	private String[] getPropertiesFromFile(String file) throws FileNotFoundException, IOException {
		String[] itRegistryProperties = new String[4];
		final Properties props = new Properties();
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(file);
			props.load(fin);
			itRegistryProperties[0] = props.getProperty(IdMLConstants.IT_REGISTRY_PREFIX + PARAM_JDBC_URL);
			itRegistryProperties[1] = props.getProperty(IdMLConstants.IT_REGISTRY_PREFIX + PARAM_JDBC_DRIVER);
			itRegistryProperties[2] = props.getProperty(IdMLConstants.IT_REGISTRY_PREFIX + PARAM_DB_USERNAME);
			itRegistryProperties[3] = props.getProperty(IdMLConstants.IT_REGISTRY_PREFIX + PARAM_DB_PASSWORD);
		} finally {
			if (fin != null) {
				fin.close();
			}
		}
		return itRegistryProperties;
	}

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

}
