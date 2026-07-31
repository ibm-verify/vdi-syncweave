/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.idml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.Vector;

import com.ibm.di.cdm.core.MetaData;
import com.ibm.di.cdm.core.MetaDataFactory;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.util.SchemaUtils;

/**
 * This IdML Component creates an IdML book and opens it for writing. It
 * supports both in-memory books (kept in memory, while new data is accumulated
 * to them) and ones that are directly stored to a file. The created IdML can be
 * either delta or refresh one.
 */
public class OpenIdMLFC extends Function {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "openidmlfc";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * The name of the storage parameter form FC's configuration.
	 */
	private static final String PARAM_STORE_IDML = "storeIdML";

	/**
	 * The name of the directory parameter form FC's configuration.
	 */
	private static final String PARAM_DIRECTORY_NAME = "directoryName";

	/**
	 * The name of the book parameter form FC's configuration.
	 */
	private static final String PARAM_BOOK_NAME = "bookName";

	/**
	 * The name of the refresh parameter form FC's configuration.
	 */
	private static final String PARAM_REFRESH = "refresh";

	/**
	 * The name of the validate parameter form FC's configuration.
	 */
	private static final String PARAM_VALIDATE = "validate";

	/**
	 * The name of the application code parameter form FC's configuration.
	 */
	private static final String PARAM_APPLICATION_CODE = "applicationCode";

	/**
	 * The name of the hostname parameter form FC's configuration.
	 */
	private static final String PARAM_HOSTNAME = "hostname";

	/**
	 * The name of the cdm version parameter form FC's configuration.
	 */
	private static final String PARAM_CDM_VERSION = "cdmVersion";

	/**
	 * The name of the application code attribute from FC's map.
	 */
	private static final String MSS_APPLICATION_CODE = "applicationCode";

	/**
	 * The name of the hostname attribute from FC's map.
	 */
	private static final String MSS_HOSTNAME = "cdm:Hostname";

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
	 * The name of the book that this Component works with.
	 */
	private String bookName;

	/**
	 * The type of storage used by the IdML (in-memory or as file).
	 */
	private String storage;

	/**
	 * The directory where the IdML file is stored. Not applicable for in-memory
	 * IdMLs.
	 */
	private String directory;

	/**
	 * Determines whether this is a refresh IdML or a delta one.
	 */
	private boolean refresh;

	/**
	 * Determines whether validation of the generated IdML should be performed,
	 * once it is closed.
	 */
	private boolean validate;

	/**
	 * The MSS's application code parameter.
	 */
	private String mssAppicationCode;

	/**
	 * The MSS's hostname parameter.
	 */
	private String mssHostname;

	/**
	 * The version of the CDM used in this IdML document.
	 */
	private String cdmVersion;

	/**
	 * Determines whether a IT registry system should be used for the CDM
	 * meta-data definitions. By default they are obtained from a jar file.
	 */
	private String useITRegistry;

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
	 * Determines if this FC has acquired lock over the managed book.
	 */
	private boolean hasLockedBook;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(Object object) throws Exception {
		super.initialize(object);

		// read configuration parameters
		bookName = getStringParameter(PARAM_BOOK_NAME);
		if (bookName != null) {
			printDebugMessage("OPEN.IDML.FC.BOOKNAME.INITIALIZED", new Object[] { bookName });
		}

		storage = getStringParameter(PARAM_STORE_IDML);
		if (storage != null) {
			printDebugMessage("OPEN.IDML.FC.PARAMETER.INITIALIZED", new Object[] { PARAM_STORE_IDML, storage });

			directory = getStringParameter(PARAM_DIRECTORY_NAME);
			if (directory != null) {
				if ("".equals(directory)) {
					if (ItdiBook.STORE_AS_FILE == Integer.parseInt(storage)) {
						directory = new File("").getAbsolutePath();
					} else {
						directory = "";
					}
				}
				printDebugMessage("OPEN.IDML.FC.PARAMETER.INITIALIZED", new Object[] { PARAM_DIRECTORY_NAME, directory });
			}
		} else {
			throw new Exception(resHash.getString("OPEN.IDML.FC.PARAMETER.NOT.PROVIDED", PARAM_STORE_IDML));
		}

		String refreshString = getStringParameter(PARAM_REFRESH);
		if (refreshString != null) {
			if ("true".equalsIgnoreCase(refreshString)) {
				refresh = true;
				printDebugMessage("OPEN.IDML.FC.IDML.OPERATION.TYPE", new Object[] { ItdiBook.REFRESH_TYPE_IDML });
			} else {
				refresh = false;
				printDebugMessage("OPEN.IDML.FC.IDML.OPERATION.TYPE", new Object[] { ItdiBook.DELTA_TYPE_IDML });
			}
		} else {
			throw new Exception(resHash.getString("OPEN.IDML.FC.PARAMETER.NOT.PROVIDED", PARAM_REFRESH));
		}

		mssAppicationCode = getStringParameter(PARAM_APPLICATION_CODE);
		if (mssAppicationCode != null) {
			printDebugMessage("OPEN.IDML.FC.MSS.PARAMETER.INITIALIZED", new Object[] { PARAM_APPLICATION_CODE, mssAppicationCode });
		}

		mssHostname = getStringParameter(PARAM_HOSTNAME);
		if (mssHostname != null) {
			printDebugMessage("OPEN.IDML.FC.MSS.PARAMETER.INITIALIZED", new Object[] { PARAM_HOSTNAME, mssHostname });
		}

		cdmVersion = getStringParameter(PARAM_CDM_VERSION);
		if (cdmVersion != null) {
			printDebugMessage("OPEN.IDML.FC.CDM.VERSION", new Object[] { cdmVersion });
		} else {
			throw new Exception(resHash.getString("OPEN.IDML.FC.PARAMETER.NOT.PROVIDED", PARAM_CDM_VERSION));
		}

		String validateString = getStringParameter(PARAM_VALIDATE);
		if (validateString != null) {
			validate = Boolean.parseBoolean(validateString);
			if (validate) {
				printDebugMessage("OPEN.IDML.FC.VALIDATE.IDML", null);
			}
		} else {
			throw new Exception(resHash.getString("OPEN.IDML.FC.PARAMETER.NOT.PROVIDED", PARAM_VALIDATE));
		}

		useITRegistry = getStringParameter(PARAM_USE_IT_REGISTRY_CDM);
		if (useITRegistry != null) {
			printDebugMessage("OPEN.IDML.FC.USE.IT.REGISTRY", new Object[] { useITRegistry });
		} else {
			throw new Exception(resHash.getString("OPEN.IDML.FC.PARAMETER.NOT.PROVIDED", PARAM_USE_IT_REGISTRY_CDM));
		}

		// if IT registry should be used, read the parameters
		// for accessing it
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
			printDebugMessage("OPEN.IDML.FC.PARAMETER.INITIALIZED", new Object[] { PARAM_JDBC_URL, jdbcUrl });
			printDebugMessage("OPEN.IDML.FC.PARAMETER.INITIALIZED", new Object[] { PARAM_JDBC_DRIVER, jdbcDriver });
			printDebugMessage("OPEN.IDML.FC.PARAMETER.INITIALIZED", new Object[] { PARAM_DB_USERNAME, dbUsername });
			printDebugMessage("OPEN.IDML.FC.PARAMETER.INITIALIZED", new Object[] { PARAM_DB_PASSWORD, IdMLConstants.PASSWORD_MASK });

			Class.forName(jdbcDriver);
		}
	}

	/**
	 * The FC check if the book it is configured to use is not existent or not
	 * opened yet. In this case the FC prepares the book and opens it for
	 * writing. Otherwise it just logs a message that the book is already
	 * prepared for use.
	 * 
	 * @param obj
	 *            the work entry passed to the FC.
	 * @return an empty Entry object.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Object perform(Object obj) throws Exception {
		if (!(obj instanceof Entry)) {
			throw new Exception(resHash.getString("OPEN.IDML.FC.EXPECTS.ENTRY"));
		}

		ItdiBook workBook = null;

		Entry work = (Entry) obj;

		// the name of the IdML book has been overridden
		String oldBookName = null;
		String newBookName = work.getString(IdMLConstants.BOOK_NAME_ATTR);
		if (newBookName != null && !newBookName.equals(bookName)) {
			oldBookName = bookName;
			if (hasLockedBook) {
				// free and close the old book (if any)
				ItdiBook oldBook = ItdiBookMapper.freeBook(oldBookName);
				if (oldBook != null && oldBook.isOpened()) {
					oldBook.close();
				}
				hasLockedBook = false;
			}
			bookName = newBookName;
			printDebugMessage("OPEN.IDML.FC.BOOKNAME.OVERRIDDEN", new Object[] { oldBookName, bookName });
		}

		if (bookName == null) {
			throw new Exception(resHash.getString("OPEN.IDML.FC.PARAMETER.NOT.PROVIDED", PARAM_BOOK_NAME));
		}

		// locking phase; it should guarantee that two
		// OpenIdMLFCs cannot use the same book at the same time
		if (!hasLockedBook) {
			// get the new book
			workBook = ItdiBookMapper.getExclusiveBook(bookName);
			hasLockedBook = true;
		}

		String mssId = null;
		if (workBook == null) {
			workBook = ItdiBookMapper.getBook(bookName);
		}
		if (workBook != null) {
			if (!workBook.isOpened()) {
				// the book has not been opened
				workBook.setStorageType(Integer.parseInt(storage));
				workBook.setDirectoryName(directory);
				workBook.setRefresh(refresh);
				workBook.setValidate(validate);

				// check if attributes in the Input Map have overridden the MSS
				// data from the FC configuration panel
				String attribute = work.getString(MSS_APPLICATION_CODE);
				if (attribute != null) {
					mssAppicationCode = attribute;
					printDebugMessage("OPEN.IDML.FC.MSS.PARAMETER.OVERRIDDEN", new Object[] { PARAM_APPLICATION_CODE,
							mssAppicationCode });
				}

				attribute = work.getString(MSS_HOSTNAME);
				if (attribute != null) {
					mssHostname = attribute;
					printDebugMessage("OPEN.IDML.FC.MSS.PARAMETER.OVERRIDDEN", new Object[] { PARAM_HOSTNAME, mssHostname });
				}

				mssId = work.getString(IdMLConstants.ID_ATTR);
				if (mssId != null) {
					printDebugMessage("OPEN.IDML.FC.PARAMETER.INITIALIZED", new Object[] { IdMLConstants.ID_ATTR, mssId });
				}

				// open the book object and share it statically.
				mssId = workBook.open(mssAppicationCode, mssHostname, cdmVersion, mssId, work);
				printDebugMessage("OPEN.IDML.FC.BOOK.OPENED", new Object[] { workBook.getName() });
			} else {
				// the book is already opened
				printDebugMessage("OPEN.IDML.FC.BOOK.ALREADY.OPENED", new Object[] { bookName });
			}
		}

		Entry returnEntry = new Entry();
		if (mssId != null) {
			returnEntry.setAttribute(IdMLConstants.ID_ATTR, mssId);
		}
		return returnEntry;
	}

	/**
	 * This method frees any resources allocated by the Component.
	 * 
	 * @exception Exception
	 *                an exception is thrown if this method fails
	 */
	@Override
	public void terminate() throws Exception {
		if (hasLockedBook) {
			// this FC has locked this book so it should free it
			ItdiBook book = ItdiBookMapper.freeBook(bookName);
			hasLockedBook = false;
			if (book != null && book.isOpened()) {
				book.close();
			}
		}
		super.terminate();
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
	 * This method displays the attributes belonging to the MSS class. For a
	 * source of this meta-data it uses either a local jar file or meta-data
	 * calls to an IT registry (depending on its configuration).
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
		Vector<Entry> listedAttributes = new Vector<Entry>();

		// populate the Output Map of the FC, while leaving the Input Map empty

		final String MSS_CLASS = "cdm:process.ManagementSoftwareSystem";

		MetaData metaData = null;
		if ("false".equals(useITRegistry)) {
			// using a jar file for meta data
			metaData = MetaDataFactory.getJarMetaData();
		} else {
			// using the specified IT registry for meta data
			metaData = MetaDataFactory.getITRegistryMetaData(jdbcUrl, jdbcDriver, dbUsername, dbPassword);
		}
		listedAttributes.addAll(metaData.getAttributes(IdMLConstants.ARTIFACT_CI, MSS_CLASS));

		// add the $id attribute if the user want to override the
		// auto generated mss id
		Entry idAttribute = new Entry();
		idAttribute.addAttributeValue("name", IdMLConstants.ID_ATTR);
		idAttribute.addAttributeValue("syntax", String.class.getName());
		listedAttributes.add(idAttribute);

		FunctionConfig config = (FunctionConfig) getConfiguration().getParent();
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
		} catch (Throwable ex) {
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
		MetaData md = null;

		if ("true".equals(useITRegistry)) {
			md = MetaDataFactory.getITRegistryMetaData(jdbcUrl, jdbcDriver, dbUsername, dbPassword);
		} else {
			md = MetaDataFactory.getJarMetaData();
		}

		return md.getCdmVersion();
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
	 * Read the properties for connecting to an IT registry from the file
	 * {@link IdMLConstants#IT_REGISTRY_PROPERTIES_FILE}, and return them as an
	 * array of Strings..
	 * 
	 * @param file
	 *            the name of the file containing the needed properties.
	 * @return an array with four strings holding the IT registry properties. They are:
	 *         [0] -> JDBC URL, 
	 *         [1] -> JDBC Driver, 
	 *         [2] -> Username, 
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
