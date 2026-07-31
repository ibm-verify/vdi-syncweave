/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.itregistry;

import static com.ibm.di.cdm.core.CDMConstants.CDM_PREFIX;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import com.ibm.di.cdm.core.MetaData;
import com.ibm.di.cdm.core.MetaDataFactory;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.fc.idml.IdMLConstants;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.RS;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.util.SchemaUtils;
import com.ibm.tivoli.dataintegration.DataIntegrationServices;
import com.ibm.tivoli.namereconciliation.common.NrsApiException;
import com.ibm.tivoli.namereconciliation.common.NrsDatabaseException;
import com.ibm.tivoli.namereconciliation.guid.Guid;

/**
 * This Component initializes a connection to the IT registry and registers a
 * Management Software System(MSS) in the IT registry database. It also returns
 * the GUID with which MSS has got registered wrapped as a
 * {@link ConfigurationItemId}, thus preventing the user to see its content, but
 * allowing its usage by the other IT registry Components.
 */
public class InitITRegistryFC extends Function {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Parameter name.
	 */
	private static final String PARAM_CDM_VERSION = "cdmVersion";

	/**
	 * Parameter name.
	 */
	private static final String PARAM_BOOK_NAME = "bookName";

	/**
	 * Parameter name.
	 */
	private static final String PARAM_REFRESH = "refresh";

	/**
	 * The name of the parameter from the Components' configuration panel that
	 * determines whether an IT registry should be used for CDM meta-data
	 * definitions.
	 */
	private static final String PARAM_USE_IT_REGISTRY_CDM = "useITRegistryCdm";

	/**
	 * MSS's CDM Version attribute. It is part of the general CDM version
	 * identifier (version.release.identifier).
	 */
	private static final String MSS_CDM_VERSION = "CDMVersion";

	/**
	 * MSS's CDM Release attribute. It is part of the general CDM version
	 * identifier (version.release.identifier).
	 */
	private static final String MSS_CDM_RELEASE = "CDMRelease";

	/**
	 * MSS's CDM Modifier attribute. It is part of the general CDM version
	 * identifier (version.release.identifier).
	 */
	private static final String MSS_CDM_MODIFIER = "CDMModifier";

	/**
	 * Parameter name.
	 */
	private static final String PARAM_JDBC_URL = "jdbcUrl";

	/**
	 * Parameter name.
	 */
	private static final String PARAM_JDBC_DRIVER = "jdbcDriver";

	/**
	 * Parameter name.
	 */
	private static final String PARAM_DB_USER = "dbUsername";

	/**
	 * Parameter name.
	 */
	private static final String PARAM_DB_PASS = "dbPassword";

	/**
	 * The name of the CDM attribute denoting the MSS's name.
	 */
	private static final String IN_MSS_NAME = "cdm:MSSName";

	/**
	 * The name of the CDM attribute denoting the MSS's manufacturer.
	 */
	private static final String IN_MSS_MANUFACTURER_NAME = "cdm:ManufacturerName";

	/**
	 * The name of the CDM attribute denoting the MSS's product name.
	 */
	private static final String IN_MSS_PRODUCT_NAME = "cdm:ProductName";

	/**
	 * The name of the CDM attribute denoting the MSS's hostname.
	 */
	private static final String IN_MSS_HOSTNAME = "cdm:Hostname";

	/**
	 * BookName value.
	 */
	private String bookName = null;

	/**
	 * Determines if this FC has acquired lock over the managed IT registry
	 * book.
	 */
	private boolean hasAcquiredLock;

	/**
	 * Refresh flag value.
	 */
	private boolean refresh = false;

	/**
	 * Determines whether a IT registry should be used for the CDM meta-data
	 * definitions. By default they are obtained from a JAR file.
	 */
	private String useITRegistry;

	/**
	 * CDM Version value.
	 */
	private String cdmVersion = null;

	/**
	 * MSSname value.
	 */
	private String mssName = null;

	/**
	 * Manufacturer value.
	 */
	private String mssManufacturer = null;

	/**
	 * Product Name value.
	 */
	private String mssProductname = null;

	/**
	 * HostName value.
	 */
	private String mssHostName = null;

	/**
	 * JDBC URL value.
	 */
	private String jdbcUrl = null;

	/**
	 * JDBC Driver value.
	 */
	private String jdbcDriver = null;

	/**
	 * DB User value.
	 */
	private String dbUser = null;

	/**
	 * DB Password value.
	 */
	private String dbPassword = null;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "inititregistryfc";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * The {@link DataIntegrationServices} API.
	 */
	private DataIntegrationServices dis;

	/**
	 * Database connection.
	 */
	Connection connection = null;

	/**
	 * HashMap of the MSS parameters.
	 */
	private HashMap<String, String> mss = new HashMap<String, String>();

	/**
	 * Guid of the registered MSS.
	 */
	private Guid mssGuid;

	/**
	 * Timestamp of MSS registration.
	 */
	private long initTime;

	/**
	 * Called once to initialize the Function Component.
	 * 
	 * @param obj
	 *            ignored.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initialize(Object obj) throws Exception {
		super.initialize(null);

		// read configuration parameters
		bookName = getStringParameter(PARAM_BOOK_NAME);
		if (bookName != null) {
			printDebugMessage("INIT.IT.REGISTRY.FC.BOOKNAME.INITIALIZED", new Object[] { bookName });
		}

		String refreshString = getStringParameter(PARAM_REFRESH);
		if (refreshString != null) {
			if ("true".equalsIgnoreCase(refreshString)) {
				refresh = true;
				printDebugMessage("INIT.IT.REGISTRY.FC.REFRESH.INITIALIZED", new Object[] { refresh });
			} else {
				refresh = false;
				printDebugMessage("INIT.IT.REGISTRY.FC.REFRESH.INITIALIZED", new Object[] { refresh });
			}
		} else {
			throw new Exception(sResHash.getString("INIT.IT.REGISTRY.FC.PARAMETER.NOT.PROVIDED", PARAM_REFRESH));
		}

		cdmVersion = getStringParameter(PARAM_CDM_VERSION);
		if (cdmVersion != null) {
			printDebugMessage("INIT.IT.REGISTRY.FC.CDM.VERSION", new Object[] { cdmVersion });
		} else {
			throw new Exception(sResHash.getString("INIT.IT.REGISTRY.FC.CDM.VERSION.NOT.PROVIDED", PARAM_CDM_VERSION));
		}

		useITRegistry = getStringParameter(PARAM_USE_IT_REGISTRY_CDM);
		if (useITRegistry != null) {
			printDebugMessage("INIT.IT.REGISTRY.FC.USE.IT.REGISTRY", new Object[] { useITRegistry });
		} else {
			throw new Exception(sResHash.getString("INIT.IT.REGISTRY.FC.PARAMETER.NOT.PROVIDED", PARAM_USE_IT_REGISTRY_CDM));
		}

		RSInterface serverInstance = getRSInterface();

		if ((serverInstance != null && serverInstance instanceof RS) || useITRegistry.equalsIgnoreCase("true")) {
			// read the parameters for accessing the IT registry database
			jdbcUrl = getStringParameter(PARAM_JDBC_URL);
			jdbcDriver = getStringParameter(PARAM_JDBC_DRIVER);
			dbUser = getStringParameter(PARAM_DB_USER);
			dbPassword = getStringParameter(PARAM_DB_PASS);

			if (jdbcUrl == null || jdbcUrl.equals("") || jdbcDriver == null || jdbcDriver.equals("") || dbUser == null
					|| dbUser.equals("") || dbPassword == null || dbPassword.equals("")) {
				String[] itRegistryProperties = getPropertiesFromFile(IdMLConstants.IT_REGISTRY_PROPERTIES_FILE);

				if (jdbcUrl == null || jdbcUrl.equals("")) {
					jdbcUrl = itRegistryProperties[0];
				}

				if (jdbcUrl == null || jdbcUrl.equals("")) {
					throw new Exception(sResHash.getString("INIT.IT.REGISTRY.FC.JDBC.PARAM.NOT.PROVIDED", PARAM_JDBC_URL));
				} else {
					printDebugMessage("INIT.IT.REGISTRY.FC.PARAMETER.INITIALIZED", new Object[] { PARAM_JDBC_URL, jdbcUrl });
				}

				if (jdbcDriver == null || jdbcDriver.equals("")) {
					jdbcDriver = itRegistryProperties[1];
				}

				if (jdbcDriver == null || jdbcDriver.equals("")) {
					throw new Exception(sResHash.getString("INIT.IT.REGISTRY.FC.JDBC.PARAM.NOT.PROVIDED", PARAM_JDBC_DRIVER));
				} else {
					printDebugMessage("INIT.IT.REGISTRY.FC.PARAMETER.INITIALIZED", new Object[] { PARAM_JDBC_DRIVER, jdbcDriver });
				}

				if (dbUser == null || dbUser.equals("")) {
					dbUser = itRegistryProperties[2];
				}

				if (dbUser == null || dbUser.equals("")) {
					throw new Exception(sResHash.getString("INIT.IT.REGISTRY.FC.JDBC.PARAM.NOT.PROVIDED", PARAM_DB_USER));
				} else {
					printDebugMessage("INIT.IT.REGISTRY.FC.PARAMETER.INITIALIZED", new Object[] { PARAM_DB_USER, dbUser });
				}

				if (dbPassword == null || dbPassword.equals("")) {
					dbPassword = itRegistryProperties[3];
				}

				if (dbPassword == null || dbPassword.equals("")) {
					throw new Exception(sResHash.getString("INIT.IT.REGISTRY.FC.JDBC.PARAM.NOT.PROVIDED", PARAM_DB_PASS));
				} else {
					printDebugMessage("INIT.IT.REGISTRY.FC.PARAMETER.INITIALIZED", new Object[] { PARAM_DB_PASS,
							IdMLConstants.PASSWORD_MASK });
				}
			}

			Class.forName(jdbcDriver);
		}
	}

	/**
	 * The FC receives the information about MSS data either from the
	 * configuration panel or from its Output Map and registers the MSS in the
	 * IT registry. Also it opens a IT registry book and fill the registration
	 * information in an IT registry Book and shares it statically.
	 * 
	 * @param obj
	 *            the work entry passed to the FC.
	 * @return an empty Entry object.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Object perform(Object obj) throws Exception {
		if (!(obj instanceof Entry)) {
			throw new Exception(sResHash.getString("INIT.IT.REGISTRY.FC.EXPECTS.ENTRY"));
		}

		ITRegistryBook workBook = null;
		Entry work = (Entry) obj;

		// the name of the IT registry book has been overridden
		String oldBookName = null;
		String newBookName = work.getString(ITRegistryConstants.BOOK_NAME_ATTR);
		if (newBookName != null && !newBookName.equals(bookName)) {
			oldBookName = bookName;
			if (hasAcquiredLock) {
				// free and close the old book (if any)
				ITRegistryBook oldBook = ITRegistryBookMapper.freeBook(oldBookName);
				if (oldBook != null && oldBook.isOpened()) {
					oldBook.close();
				}
				hasAcquiredLock = false;
			}
			bookName = newBookName;
			printDebugMessage("INIT.IT.REGISTRY.FC.BOOKNAME.OVERRIDDEN", new Object[] { oldBookName, bookName });
		}

		if (bookName == null) {
			throw new Exception(sResHash.getString("INIT.IT.REGISTRY.FC.PARAMETER.NOT.PROVIDED", PARAM_BOOK_NAME));
		}

		connection = null;
		try {
			// locking phase; it should guarantee that two
			// Init IT registry FCs can not use the same book at the same time
			if (!hasAcquiredLock) {
				// get the new book
				workBook = ITRegistryBookMapper.getExclusiveBook(bookName);
				hasAcquiredLock = true;
			}

			// open phase; attempt to fill the Components
			// function and open the book
			if (workBook == null) {
				workBook = ITRegistryBookMapper.getBook(bookName);
			}

			if (workBook != null) {
				if (!workBook.isOpened()) {
					mss.clear();
					mssGuid = null;

					String[] cdmVersions = cdmVersion.split("\\.");
					if (cdmVersions.length != 3 || !isNumber(cdmVersions[0]) || !isNumber(cdmVersions[1])
							|| !isNumber(cdmVersions[2])) {
						throw new Exception(sResHash.getString("INIT.IT.REGISTRY.FC.INVALID.CDM.VERSION", cdmVersion));
					}

					addMSSParameter(MSS_CDM_VERSION, cdmVersions[0]);
					addMSSParameter(MSS_CDM_RELEASE, cdmVersions[1]);
					addMSSParameter(MSS_CDM_MODIFIER, cdmVersions[2]);

					// Check if MSSName is set output map
					mssName = work.getString(IN_MSS_NAME);
					mssManufacturer = work.getString(IN_MSS_MANUFACTURER_NAME);
					mssHostName = work.getString(IN_MSS_HOSTNAME);
					mssProductname = work.getString(IN_MSS_PRODUCT_NAME);

					// If MSSName is not set then check Manufacturername,
					// Hostname and Productname are set in output map
					if (mssName == null //
							&& (mssManufacturer == null || mssManufacturer.equals("") || mssHostName == null
									|| mssHostName.equals("") || mssProductname == null || mssProductname.equals(""))) {
						throw new Exception(sResHash.getString("INIT.IT.REGISTRY.FC.MSSNAME.MANUFACT.HOSTNAME.PRODNAME.REQUIRED"));
					}
					connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
					dis = new DataIntegrationServices();
					dis.init(connection, null, null, null, null);

					// Adding MSS parameters which are present in Output Map to
					// HashMap.
					String[] attributes = work.getAttributeNames();
					String mssAttributeName;
					String mssAttributeValue;
					for (int i = 0; i < attributes.length; i++) {
						if (attributes[i].startsWith(CDM_PREFIX)) {
							mssAttributeName = attributes[i].substring(attributes[i].indexOf(':') + 1);
							mssAttributeValue = work.getString(attributes[i]);
							addMSSParameter(mssAttributeName, mssAttributeValue);
						}
					}

					// Register MSS
					mssGuid = dis.registerMSS(mss);
					if (mssGuid != null) {
						connection.commit();
						// record the timestamp when MSS was successfully
						// registered
						initTime = System.currentTimeMillis();
					}
					// open the book object and share it statically.
					workBook.open(refresh, initTime);
				} else {
					// the book is already opened
					printDebugMessage("INIT.IT.REGISTRY.FC.BOOK.ALREADY.OPENED", new Object[] { bookName });
				}
			}
		} catch (NrsApiException nE) {
			String errorMessage = sResHash.getString("INIT.IT.REGISTRY.FC.NRS.API.EXCEPTION");
			throw new Exception(errorMessage, nE);
		} catch (NrsDatabaseException nDE) {
			String errorMessage = sResHash.getString("INIT.IT.REGISTRY.FC.NRS.DATABASE.EXCEPTION");
			throw new Exception(errorMessage, nDE);
		} finally {
			if (connection != null) {
				connection.commit();
				connection.close();
			}
		}

		Entry returnEntry = new Entry();
		returnEntry.setAttribute(ITRegistryConstants.ATTR_MSS_GUID, mssGuid);

		return returnEntry;
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
	 * This method returns the username for connecting to the UT registry
	 * database used by the Connector.
	 * 
	 * @return a database username.
	 */
	public String getDbUsername() {
		return dbUser;
	}

	/**
	 * This method displays the attributes belonging to the MSS class. For a
	 * source of this meta-data it uses either a local jar file or meta-data
	 * calls to an IT registry system (depending on its configuration).
	 * 
	 * @param input
	 *            entry object
	 * @return <b>null</b>, since this Components has itslf populated the
	 *         Schemas.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	@Override
	public Object querySchema(Object input) throws Exception {
		Vector<Entry> listedAttributes = new Vector<Entry>();

		// populate the Output Map of the FC, while leaving the Input Map empty

		final String MSS_CLASS = "cdm:process.ManagementSoftwareSystem";

		MetaData metaData = null;
		if ("false".equals(useITRegistry)) {
			// using a jar file for meta data
			metaData = MetaDataFactory.getJarMetaData();
		} else {
			// using the specified IT registry system for meta data
			metaData = MetaDataFactory.getITRegistryMetaData(jdbcUrl, jdbcDriver, dbUser, dbPassword);
		}
		listedAttributes.addAll(metaData.getAttributes(IdMLConstants.ARTIFACT_CI, MSS_CLASS));

		FunctionConfig config = (FunctionConfig) getConfiguration().getParent();
		// populate the Output Map
		SchemaConfig sc = config.getSchema(false);
		sc.notifyChange(sc, "", MetamergeConfigChange.BEGIN_CHANGES);
		for (Entry e : listedAttributes) {
			try {
				SchemaUtils.addSchemaItem(sc, e.getString("name"), e.getString("syntax"), null);
			} catch (Exception ex) {
				// wrong item name
				SystemFunctions.doNothing();
			}
		}
		sc.notifyChange(sc, "", MetamergeConfigChange.END_CHANGES);

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersion() {
		return "1.0-di7.1.1 %I% 20%E%";
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
			debug(sResHash.getString(msgKey));
		} else if (params.length == 1) {
			debug(sResHash.getString(msgKey, params[0]));
		} else {
			debug(sResHash.getString(msgKey, params));
		}
	}

	/**
	 * Retrieves a value, specified by the user.
	 * 
	 * @param parameterName
	 *            name of the parameter , String.
	 * @return the value of the parameter.
	 */
	private String getStringParameter(String parameterName) {
		String parameter = (String) getParam(parameterName);
		if (parameter != null) {
			parameter = parameter.trim();
		}
		return parameter;
	}

	/**
	 * This method frees any resources allocated by the Component.
	 * 
	 * @exception Exception
	 *                an exception is thrown if this method fails
	 */
	@Override
	public void terminate() throws Exception {
		if (hasAcquiredLock) {
			// this FC has locked this book so it should free it
			ITRegistryBook book = ITRegistryBookMapper.freeBook(bookName);
			if (book != null && book.isOpened()) {
				book.close();
			}

		}
		if (connection != null && !connection.isClosed()) {
			connection.commit();
			connection.close();
		}

		super.terminate();
	}

	/**
	 * Read the properties for connecting to a IT registry from the file
	 * {@link IdMLConstants#IT_REGISTRY_PROPERTIES_FILE}, and return them as an
	 * array of Strings.
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
			itRegistryProperties[2] = props.getProperty(IdMLConstants.IT_REGISTRY_PREFIX + PARAM_DB_USER);
			itRegistryProperties[3] = props.getProperty(IdMLConstants.IT_REGISTRY_PREFIX + PARAM_DB_PASS);
		} finally {
			if (fin != null) {
				fin.close();
			}
		}
		return itRegistryProperties;
	}

	/**
	 * Checks if with the currently configured properties, a connection to the
	 * IT registry database can be established.
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
				conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		} catch (Throwable ex) {
			exceptionText = ex.toString();
		}
		return exceptionText;
	}

	/**
	 * Returns the version of the CDM that this component is using. depending on
	 * its configuration this can be either the version of CDM that the IT
	 * registry is using or the version of the CDM meta-data stored in the
	 * locally used jar file.
	 * 
	 * @return the CDM version, with format
	 *         '&ltversion&gt.&ltrelease&gt.&ltmodifier&gt'.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public String getCdmVersion() throws Exception {
		MetaData md = null;

		if ("true".equals(useITRegistry)) {
			md = MetaDataFactory.getITRegistryMetaData(jdbcUrl, jdbcDriver, dbUser, dbPassword);
		} else {
			md = MetaDataFactory.getJarMetaData();
		}

		return md.getCdmVersion();
	}

	/**
	 * Adds an element in MSS Hashmap
	 * 
	 * @param Key
	 *            the Key parameter which needs to be added
	 * @param Value
	 *            the value of the Key parameter
	 */
	private void addMSSParameter(String Key, String Value) {
		if (Value != null) {
			printDebugMessage("INIT.IT.REGISTRY.FC.MSS.PARAMETER.INITIALIZED", new Object[] { Key, Value });
			mss.put(Key, Value);
		}
	}

	/**
	 * Determines if the argument passed is a number or not
	 * 
	 * @param number
	 *            the string which need to be checked.
	 * @return returns true if passed string is a number else false.
	 */
	private boolean isNumber(String number) {
		try {
			Integer.parseInt(number);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
