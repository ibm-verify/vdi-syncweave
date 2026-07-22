/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import static com.ibm.di.cdm.core.CDMConstants.CDM_PREFIX;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.ibm.di.cdm.core.CDMConstants;
import com.ibm.di.cdm.core.MetaData;
import com.ibm.di.cdm.core.MetaDataFactory;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.idml.IdMLConstants;
import com.ibm.di.fc.idml.IdMLConstants.Operations;
import com.ibm.di.fc.itregistry.ConfigurationItemId;
import com.ibm.di.fc.itregistry.ITRegistryBook;
import com.ibm.di.fc.itregistry.ITRegistryBookMapper;
import com.ibm.di.fc.itregistry.ITRegistryConstants;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.RS;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;
import com.ibm.di.util.SchemaUtils;
import com.ibm.tivoli.dataintegration.DataIntegrationServices;
import com.ibm.tivoli.namereconciliation.guid.Guid;

/**
 * This Connector will add a CI (Configuration Item) or a Relationship to a
 * centralized IT registry database. It depends upon IT registry for working
 * properly.<br>
 * 
 * This Connector supports three modes: <ul> <li>CallReply</li>
 * <li>Iterator</li> <li>Lookup</li> </ul>
 * 
 * <p>Note: The IT registry API uses multiple maps without generic types which
 * cause multiple warnings.</p>
 */
@SuppressWarnings("unchecked")
public class ITRegistryConnector extends Connector implements ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Name of the Connector.
	 */
	private static final String CONN_NAME = "IT registry Ci and Relationship Connector";

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "itregistryconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * The name of the artifact type parameter from the Connector's
	 * configuration panel.
	 */
	private static final String PARAM_ARTIFACT_TYPE = "artifactType";

	/**
	 * The name of the class type parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_CLASS_TYPE = "classType";

	/**
	 * The name of the book name parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_BOOK_NAME = "bookName";

	/**
	 * The name of MSS name parameter from the Connector's configuration panel.
	 */
	private static final String PARAM_MSS_NAME = "mssName";

	/**
	 * The name of 'Use Date' from the Connector's configuration Panel.
	 */
	private static final String PARAM_USE_DATE = "useDate";

	/**
	 * The name of 'Date Filter' from the Connector's configuration Panel.
	 */
	private static final String PARAM_DATE_FILTER = "dateFilter";

	/**
	 * The name of 'Attrbute Filter' from the Connector's configuration Panel.
	 */
	private static final String PARAM_ATTRIBUTE_FILTER = "attributeFilter";

	/**
	 * The name of the parameter from the Connector's configuration panel that
	 * determines whether IT registry should be used for CDM meta-data
	 * definitions.
	 */
	private static final String PARAM_USE_IT_REGISTRY_CDM = "useITRegistryCdm";
	
	/**
	 * The level of search to be performed 
	 */
	private static final String PARAM_SCOPE = "scope";
	
	/**
	 * The name of parameter on Connector's Configuration Panel to enable fetching deleted CI.  
	 */
	private static final String PARAM_IS_DELETED = "isDeleted";
	
	/**
	 * The name of parameter on Connector's config panel
	 */
	 private static final String PARAM_RETURN_GUID = "returnGuid";

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
	 * Constant for attributes separation in Attribute Filter.
	 */
	private static final String ATTR_SEPARATOR = ",";

	/**
	 * Constant for equals in Attribute Filter.
	 */
	private static final String MATCH = "=";

	/**
	 * Separator for input
	 */
	private static final String GENERIC_SEPARATOR = ":";

	/**
	 * Naming Context Prefix
	 */
	private static final String NAMING_CONTEXT_PREFIX = "nc:";

	/**
	 * Constant for Naming Context
	 */
	private static final String NAMING_CONTEXT_KEY = "NamingContext";

	/**
	 * MSS host name.
	 */
	private static final String HOST_NAME = "HostName";

	/**
	 * MSS Product Name.
	 */
	private static final String PRODUCT_NAME = "ProductName";

	/**
	 * MSS Manufacturer Name.
	 */
	private static final String MANUFACTURER_NAME = "ManufacturerName";

	/**
	 * MSS Name.
	 */
	private static final String MSSNAME = "MSSName";

	/**
	 * Prefix for internal attributes of the Connector.
	 */
	private static final String INTERNAL_ATTRIBUTES = "$";

	/**
	 * Represents empty string.
	 */
	private static final String EMPTY_STRING = "";

	/**
	 * Represents boolean false.
	 */
	private static final String FALSE_VALUE = "false";

	/**
	 * Represents boolean true.
	 */
	private static final String TRUE_VALUE = "true";

	/**
	 * Attribute used to store the HashMap of a Management Software System in
	 * the returned entry.
	 */
	private static final String ATTR_MSS = "$managementSoftwareSystem";// ManagementSoftwareSystem

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
	 * The name of the book shared with Init IT registry FC.
	 */
	private String bookName;

	/**
	 * The Guid of MSS selected by user. Applicable for Iterator\Lookup mode.
	 */
	private Guid mssGuid;

	/**
	 * The Name attribute of the MSS chosen by the user. Applicable for
	 * Iterator\Lookup mode.
	 */
	private String mssName;

	/**
	 * The value of check box 'Use Date' parameter.
	 */
	private String useDate;

	/**
	 * The value for 'Date Filter' parameter in Short Date Format (M/D/YY).
	 */
	private String dateFilter;

	/**
	 * The 'Attribute Filter' which holds list for Attributes. Applicable for
	 * Iterator mode.
	 */
	private String attributeFilter;

	/**
	 * Determines whether a IT registry should be used for the CDM meta-data
	 * definitions. By default they are obtained from a jar file.
	 */
	private String useITRegistry;

	/**
	 * The JDBC URL used for connecting to the IT registry database.
	 */
	private String jdbcUrl;

	/**
	 * The JDBC Driver used for connecting to the IT registry database.
	 */
	private String jdbcDriver;

	/**
	 * The user name used for connecting to the IT registry database.
	 */
	private String dbUsername;

	/**
	 * The password used for connecting to the IT registry database.
	 */
	private String dbPassword;

	/**
	 * The connection object for IT registry database.
	 */
	private Connection connection;

	/**
	 * The Assembly Line operation executed for current cycle.
	 */
	private Operations operationType;

	/**
	 * The output of the Connector in Iterator mode.
	 */
	private List<HashMap> results;

	/**
	 * The Data Integration Service.
	 */
	private DataIntegrationServices dis;

	/**
	 * The parsed value of 'Class Type' parameter.
	 */
	private String parsedClassType;

	/**
	 * Whether this Connector is configured to work with CIs or Relationships.
	 */
	private boolean worksWithCIs = false;;

	/**
	 * Whether this Connector will read/search IT registry for a particular type
	 * of items or all of them.
	 */
	private boolean iterateAllTypes = false;

	/**
	 * A list of class types to be iterated by the Connector.
	 */
	private List<String> classTypes;
	
	/**
	*	The value of Parameter  'ReturnGuid'.
	*/
	private String returnGuid;
	
	/**
	*	The value of Parameter  'scope'.
	*/
	private String scope;
	
	/**
	*	The value of Parameter  'isDeleted'.
	*/
	private String isDeleted;
	
	/**
	*	The value of Parameter  'DateFilter'.
	*/
	private Date sinceDate;

	/**
	 * Constructor which populates the modes supported by the Connector.
	 */
	public ITRegistryConnector() {
		super();
		Trace.entrymid(this, CONN_NAME);
		setName(CONN_NAME);
		setModes(new String[] { ConnectorConfig.CALL_REPLY_MODE, ConnectorConfig.ITERATOR_MODE, ConnectorConfig.LOOKUP_MODE });
		Trace.exitmid(this, CONN_NAME);
	}

	/**
	 * Returns the username for connection to the IT registry database.
	 * 
	 * @return the dbUsername.
	 */
	public String getDbUsername() {
		return dbUsername;
	}

	/**
	 * Returns the JDBC driver for connection to the IT registry database.
	 * 
	 * @return the jdbcDriver.
	 */
	public String getJdbcDriver() {
		return jdbcDriver;
	}

	/**
	 * Returns the JDBC URL for connection to the IT registry database.
	 * 
	 * @return the jdbcUrl.
	 */
	public String getJdbcUrl() {
		return jdbcUrl;
	}

	/**
	 * Returns whether the Connector relies on a IT registry for its meta-data.
	 * 
	 * @return the useITRegistry parameter.
	 */
	public String getUseITRegistry() {
		return useITRegistry;
	}

	/**
	 * This standard method initializes the Connector with values present in its
	 * Configuration panel. If database related parameters are not present in
	 * the Connector's configuration, they are taken from the
	 * {@link IdMLConstants#IT_REGISTRY_PROPERTIES_FILE} file.
	 * 
	 * @param entry
	 *            initial entry provided to the Connector.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	@Override
	public void initialize(Object entry) throws Exception {
		// read configuration parameters
		bookName = getStringParameter(PARAM_BOOK_NAME);
		if (bookName != null) {
			printDebugMessage("IT.REGISTRY.CONN.BOOKNAME.INITIALIZED", new Object[] { bookName });
		}

		// display the MSS name instead of its Guid to avoid explicitly showing
		// the IT registry Guids
		mssName = getStringParameter(PARAM_MSS_NAME);
		if (mssName != null) {
			printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_MSS_NAME, mssName });
		}

		artifactType = getStringParameter(PARAM_ARTIFACT_TYPE);
		if (null != artifactType) {
			printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_ARTIFACT_TYPE, artifactType });
			if (IdMLConstants.ARTIFACT_CI.equalsIgnoreCase(artifactType)) {
				worksWithCIs = true;
			}
		} else {
			throw new Exception(resHash.getString("IT.REGISTRY.CONN.PARAMETER.NOT.PROVIDED", PARAM_ARTIFACT_TYPE));
		}

		classType = getStringParameter(PARAM_CLASS_TYPE);
		if (classType != null && classType.length() > 0) {
			printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_CLASS_TYPE, classType });
			parsedClassType = parseCDMClassType(classType);
		} else {
			printDebugMessage("IT.REGISTRY.CONN.QUERYING.ALL.ITEMS");
			iterateAllTypes = true;
		}
		classTypes = new ArrayList<String>();
		results = new ArrayList<HashMap>();

		scope = getStringParameter(PARAM_SCOPE);
	    if (scope != null) {
	      printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_SCOPE, scope });
	    }

	    isDeleted = getStringParameter(PARAM_IS_DELETED);
	    if (isDeleted != null) {
	      printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_IS_DELETED, isDeleted });
	    }
	    
	    returnGuid = getStringParameter(PARAM_RETURN_GUID);
	    if (returnGuid != null) {
		      printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_RETURN_GUID, returnGuid });
		}
		
		useITRegistry = getStringParameter(PARAM_USE_IT_REGISTRY_CDM);
		if (useITRegistry != null) {
			printDebugMessage("IT.REGISTRY.CONN.USE.IT.REGISTRY", new Object[] { useITRegistry });
		}

		useDate = getStringParameter(PARAM_USE_DATE);
		if (useDate != null) {
			printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_USE_DATE, useDate });
		}

		dateFilter = getStringParameter(PARAM_DATE_FILTER);
		if (dateFilter != null) {
			printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_DATE_FILTER, dateFilter });
		}
		
		if ((this.useDate != null) && (this.dateFilter != null) && ("true".equalsIgnoreCase(this.useDate))) {
		      this.sinceDate = parseDateFilter(this.dateFilter);
		}
		
		attributeFilter = getStringParameter(PARAM_ATTRIBUTE_FILTER);
		if (attributeFilter != null) {
			printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_ATTRIBUTE_FILTER, attributeFilter });
		}

		RSInterface serverInstance = getRSInterface();
		// Check if the Connector is invoked from the TDI Server and if so,
		// initialize the NRS API.
		// In the CE this is not always required.
		if ((serverInstance != null && serverInstance instanceof RS) || TRUE_VALUE.equalsIgnoreCase(useITRegistry)) {
			initDataIntergrationServices();
		}
	}

	/**
	 * This method checks for Database parameters. It throws exception for
	 * missing database properties.
	 * 
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void initDatabase() throws Exception {
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

		if (jdbcUrl == null || jdbcUrl.trim().equals(EMPTY_STRING)) {
			throw new Exception(resHash.getString("IT.REGISTRY.CONN.PARAMETER.NOT.PROVIDED", PARAM_JDBC_URL));
		} else {
			printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_JDBC_URL, jdbcUrl });
		}

		if (jdbcDriver == null || jdbcDriver.trim().equals(EMPTY_STRING)) {
			throw new Exception(resHash.getString("IT.REGISTRY.CONN.PARAMETER.NOT.PROVIDED", PARAM_JDBC_DRIVER));
		} else {
			printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_JDBC_DRIVER, jdbcDriver });
		}

		if ((dbUsername == null) || dbUsername.trim().equals(EMPTY_STRING)) {
			throw new Exception(resHash.getString("IT.REGISTRY.CONN.PARAMETER.NOT.PROVIDED", PARAM_DB_USERNAME));

		} else {
			printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_DB_USERNAME, dbUsername });
		}

		if (dbPassword == null || dbPassword.trim().equals(EMPTY_STRING)) {
			throw new Exception(resHash.getString("IT.REGISTRY.CONN.PARAMETER.NOT.PROVIDED", PARAM_DB_PASSWORD));

		} else {
			printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_DB_PASSWORD,
					IdMLConstants.PASSWORD_MASK });
		}

		Class.forName(jdbcDriver);
	}

	/**
	 * This method initializes the Naming And Reconciliation Service using the
	 * database parameters.
	 * 
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void initDataIntergrationServices() throws Exception {
		// checks database properties.
		initDatabase();
		connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
		// Access the NRS Service.
		dis = new DataIntegrationServices();
		dis.init(connection, null, null, null, null);
	}

	/**
	 * This method populates Output Map for CallReply mode and Input Map for
	 * Iterator\Lookup mode. Depending upon the value of "Use IT registry"
	 * parameter the attributes for a CI\Relationship are fetched from JAR or
	 * the IT registry database.
	 * 
	 * @param arg0
	 *            an object parameter not used by this method.
	 * @return <code>null</code>, since the Connector handles the schema
	 *         population on its own.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	@Override
	public Object querySchema(Object arg0) throws Exception {
		// populate the Output Map of the Connector, while leaving the Input Map
		// empty
		Vector<Entry> listedAttributes = new Vector<Entry>();
		MetaData metaData = null;
		if (classType != null && !classType.equals(EMPTY_STRING)) {
			if (FALSE_VALUE.equals(useITRegistry)) {
				// using a jar file for meta data
				metaData = MetaDataFactory.getJarMetaData();
			} else {
				// using the specified IT registry for meta data
				metaData = MetaDataFactory.getITRegistryMetaData(jdbcUrl, jdbcDriver, dbUsername, dbPassword);
			}
			listedAttributes.addAll(metaData.getAttributes(artifactType, classType));
		}

		ConnectorConfig config = (ConnectorConfig) getConfiguration();
		SchemaConfig sc = null;

		if (config.getMode().equalsIgnoreCase(ConnectorConfig.CALL_REPLY_MODE)) {
			// retrieve the Output Map
			sc = config.getSchema(false);
		} else {
			// retrieve the Input Map
			sc = config.getSchema(true);
		}

		// populate the Map
		sc.notifyChange(sc, EMPTY_STRING, MetamergeConfigChange.BEGIN_CHANGES);
		for (Entry e : listedAttributes) {
			try {
				SchemaUtils.addSchemaItem(sc, e.getString("name"), e.getString("syntax"), null);
			} catch (Exception ex) {
				// wrong item name
				SystemFunctions.doNothing();
			}
		}
		sc.notifyChange(sc, EMPTY_STRING, MetamergeConfigChange.END_CHANGES);

		return null;
	}

	/**
	 * This method is invoke for CallReply mode. In this mode the Connector will
	 * be used together with an Init IT registry FC in the AssemblyLine. It
	 * depends upon Init IT registry FC for statically sharing a book - an
	 * object holding needed information, which it looks up using a 'Book Name'
	 * parameter. In this mode the Connector can perform the following
	 * operations:<ul>
	 * 
	 * <li>create: add a CI or Relationship. If Init IT registry FC has set a
	 * Refresh flag, then the Connector will first remove all entries in the IT
	 * registry database which are older that a given timestamp. The timestamp
	 * is detemined by the Init IT registry FC and made available through the
	 * shared book. To add a CI the Connector should satisfy at least one of its
	 * Naming Rules. A naming rule is a specific list of identifying attributes
	 * that should be passed to the IT registry for a CI to be successfully
	 * registered in its database (please refer to the Tivoli Common Data Model
	 * documentation for more details). <br>The supported attributes for a CI
	 * (identifying and non-identifying) can be discovered in Output Map of the
	 * Connector. After registering a CI the Connector will return a $id
	 * attribute - a wrapper of the actual GUID returned by the IT registry. It
	 * could be latter passed to another IT registry Connector for registering
	 * Relationships. However, the value of the wrapped GUID cannot and should
	 * not be extracted by users. To add a Relationship the &id-s of two CIs
	 * should be passed for the 'source' and 'target' attributes in the Output
	 * Map of the Connector. </li>
	 * 
	 * <li>modify: currently this operation is not supported neither for CIs nor
	 * Relationships. </li>
	 * 
	 * <li>delete: delete a CI\Relationship. To delete a Relationship the $id-s
	 * of the 'source' and 'target' CIs should be supplied.</ul></li><br>
	 * 
	 * @param entry
	 *            entry mapped in the Output Map of the FC.
	 * @return the returned entry object. It contains an attribute $id which
	 *         will hold a wrapper of the GUID returned by the IT registry upon
	 *         registering a CI. For the rest of the cases (update and delete)
	 *         it is null.
	 * @throws Exception
	 *             If Book Name is not supplied. If MSS Guid is not supplied. If
	 *             operation is not supplied.
	 */
	@Override
	public Entry queryReply(Entry entry) throws Exception {
		String newBookName = entry.getString(ITRegistryConstants.BOOK_NAME_ATTR);
		if (newBookName != null && !newBookName.equals(bookName)) {
			// The Component should work with a different book
			String oldBookName = bookName;
			bookName = newBookName;
			printDebugMessage("IT.REGISTRY.CONN.BOOKNAME.OVERRIDDEN", new Object[] { oldBookName, newBookName });
		}

		if (bookName == null) {
			throw new Exception(resHash.getString("IT.REGISTRY.CONN.PARAMETER.NOT.PROVIDED", PARAM_BOOK_NAME));
		}

		// retrieve the String GUID of the MSS used by this Connector.
		HashMap<String, Object> attributeMaps = new HashMap<String, Object>();
		Guid mssGuid = null;
		Attribute mssGuidAttribute = entry.getAttribute(ITRegistryConstants.ATTR_MSS_GUID);
		if (mssGuidAttribute == null || (mssGuid = parseGuid(mssGuidAttribute.getValue(0))) == null) {
			throw new Exception(resHash.getString("IT.REGISTRY.CONN.PARAMETER.NOT.PROVIDED", ITRegistryConstants.ATTR_MSS_GUID));
		}

		ITRegistryBook book = ITRegistryBookMapper.getBook(bookName);
		Guid[] managedElements = null;

		if (book != null && book.isOpened()) {
			// determine the 'operation' this Component will perform
			final char operationChar = entry.getOp();
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
				String attribute = entry.getString(Operations.PARAM_NAME);
				if (attribute != null) {
					try {
						operationType = Operations.valueOf(attribute.toUpperCase());
					} catch (IllegalArgumentException iae) {
						throw new Exception(resHash.getString("IT.REGISTRY.CONN.INCORRECT.ATTR.VALUE", new Object[] {
								Operations.PARAM_NAME, attribute }), iae);
					}
				}
			}
			if (operationType == null) {
				operationType = Operations.CREATE;
			}
			printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { Operations.PARAM_NAME, operationType });

			// Check if $operation is not create and yet refresh flag is set by
			// Init IT registry FC. In this scenario throw exception.
			if (book.isRefresh() && operationType != Operations.CREATE) {
				throw new Exception(resHash.getString("IT.REGISTRY.CONN.UNSUPPORTED.OPERATION"));
			}

			if (Operations.CREATE.equals(operationType)) {
				if (book.isRefresh() && !book.isRemovedStale()) {
					Date initDate = new Date(book.getInitTime());
					dis.removeStale(mssGuid, initDate);
					book.setRemovedStale(true);
				}
				if (worksWithCIs) {
					attributeMaps = populateCI(entry);
					if ("AbstractResource".equals(attributeMaps.get(ITRegistryConstants.ATTR_CLASS_TYPE))) {
						// register AbstractResource-s using the dedicated DIS
						// API
						printDebugMessage("IT.REGISTRY.CONN.ABSTRACT.RESOURCE");
						managedElements = dis.registerAbstractResources(mssGuid, new Map[] { attributeMaps });
					} else {
						managedElements = dis.register(mssGuid, new HashMap[] { attributeMaps });
					}
				} else {
					attributeMaps = populateReln(entry, false);
					// Ignore the returned values. For now it is always zero.
					dis.addRelationships(mssGuid, new HashMap[] { attributeMaps });
				}
			} else if (Operations.DELETE.equals(operationType)) {
				if (worksWithCIs) {
					attributeMaps = populateCI(entry);
					Vector<Guid> vGuid = new Vector<Guid>();
					// Invoke the get method to fetch Guid of CI to be deleted.
					HashMap<?, ?>[] outputMaps = dis.get(mssGuid, attributeMaps, 0);

					for (int iter = 0; iter < outputMaps.length; iter++) {
						Guid meGuid = (Guid) outputMaps[iter].get(ITRegistryConstants.ATTR_OUTPUT_GUID);
						vGuid.add(meGuid);
					}

					if (vGuid.size() != 0) {
						Guid[] aGuid = new Guid[vGuid.size()];
						aGuid = vGuid.toArray(aGuid);
						dis.delete(mssGuid, aGuid);
					}
				} else {
					attributeMaps = populateReln(entry, true);
					dis.deleteRelationships(mssGuid, attributeMaps);
				}
			} else if (Operations.MODIFY.equals(operationType)) {
				if (worksWithCIs) {
					// IT registry v1.1 does not support registering of
					// non-identifying attributes. This combined with the
					// restriction that UPDATE operations can modify only
					// non-identifying attributes, means that this functionality
					// cannot be implemented. Thus, in this case we throw an
					// exception.
					throw new Exception(resHash.getString("IT.REGISTRY.CONN.CI.UPDATE.ERROR", new Object[] { classType }));
				} else {
					throw new Exception(resHash.getString("IT.REGISTRY.CONN.RELATION.UPDATE.ERROR", new Object[] { classType }));
				}
			}
			connection.commit();
		} else {
			throw new Exception(resHash.getString("IT.REGISTRY.CONN.BOOK.ALREADY.CLOSED", new Object[] { bookName }));
		}

		final Entry retEntry = new Entry();
		if (managedElements != null) {
			retEntry.setAttribute(ITRegistryConstants.ATTR_GUID, managedElements[0]);
		}
		return retEntry;
	}

	// TO BE CONSIDER WHEN IDENTIFYING AND NON-IDENTIFYING ATRIBUTES API ARE
	// ADDED TO NRS
	/*
	 * This creates an array of HashMap as required which removes
	 * "Identifying attrbutes" and "ManagementSoftwareSystem"
	 * 
	 * @param HashMap[] Array of HashMap containing
	 * 
	 * @return HashMap[]
	 */
	/*
	 * private HashMap[] buildModifyCI(HashMap[] arrMap){ for(HashMap
	 * map:arrMap){
	 * //printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new
	 * Object[]{"INPUT MAP",map}); Set<Map.Entry> set = map.entrySet();
	 * //traverse over every HashMap to remove keys for (Map.Entry me:set ){
	 * String sKey = (String)me.getKey(); //Object sValue = me.getValue();
	 * if(((String
	 * )sKey).equalsIgnoreCase(ITRegistryConstants.IDENTIFYING_ATTRIBUTE) ||
	 * ((String
	 * )sKey).equalsIgnoreCase(ITRegistryConstants.MANAGEMENT_SOFTWARE_SYSTEM))
	 * map.remove(sKey); } } return arrMap; }
	 */

	/**
	 * This method creates a HashMap for creating/modifying/deleting a CI
	 * (Configuration Item). The HashMap should contain 'ClassType' and
	 * identifying attributes. The internal attributes (starting with '$') are
	 * ignored.
	 * 
	 * @param entry
	 *            an entry object containing the needed attributes.
	 * @return a Map that is used for passing the attributes to the IT registry.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private HashMap<String, Object> populateCI(Entry entry) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		HashMap<String, Object> namingContextMap = new HashMap<String, Object>();

		Collection<String> attrKeys = entry.getAttributeCollection();
		String classType = entry.getString(CDMConstants.CDM_CLASSTYPE_SYSTEM_ATTRIBUTE);
		if (classType == null || classType.length() == 0) {
			classType = parsedClassType;
		} else {
			classType = parseCDMClassType(classType);
		}

		map.put(ITRegistryConstants.ATTR_CLASS_TYPE, classType);
		for (String attrName : attrKeys) {
			if (attrName.startsWith(INTERNAL_ATTRIBUTES)) {
				continue;
			}

			Attribute attribute = entry.getAttribute(attrName);
			if (attribute != null) {
				Object attrValue = attribute.getValue(0);
				if (attrValue != null) {
					String parsedAttributeName = parseAttributeName(attrName);
					if (checkCDMAttribute(attrName) && attrValue instanceof String) {
						map.put(parsedAttributeName, attrValue);
					} else if (checkNamingContext(attrName)) {
						Guid guid = parseGuid(attrValue);
						if (guid != null) {
							namingContextMap.put(parsedAttributeName, guid);
						} else {
							throw new Exception(resHash.getString("IT.REGISTRY.CONN.INCORRECT.GUID.FORMAT", attrName));
						}
					}
					printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { attrName, attrValue });
				}
			}
		}

		if (namingContextMap.size() > 0) {
			map.put(NAMING_CONTEXT_KEY, namingContextMap);
		}
		return map;
	}

	/**
	 * Converts the provided object to an IT registry Guid. <br>The supported
	 * input objects
	 * are:<ul><li>Guid</li><li>ConfigurationItemId</li><li>String</li></ul>
	 * 
	 * @param value
	 *            the object to convert.
	 * @return the converted Guid, or <code>null</code> if invalid.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private Guid parseGuid(Object value) throws Exception {
		Guid guid = null;
		if (value instanceof Guid) {
			guid = (Guid) value;
		} else if (value instanceof ConfigurationItemId) {
			ConfigurationItemId attrId = (ConfigurationItemId) value;
			guid = unwrapConfigurationItemId(attrId);
		} else if (value instanceof String) {
			guid = new Guid((String) value);
		}
		return guid;
	}

	/**
	 * This method populates a HashMap (resource filter) required for
	 * creating\deleting a relationship from the entry object. For adding a
	 * relationship ($operation=create) the HashMap should contain values for
	 * 'Source', 'Target' and 'RelationshipType'. For deleting a
	 * relationship($operation=delete) the HashMap should contain values for
	 * 'SourceGuid', 'TargetGuid' and 'RelationshipType'.
	 * 
	 * @param entry
	 *            The Output Map entry object
	 * @param isDelete
	 *            if true , create HashMap for deleting a relationship.
	 * @return HashMap which contains values as required for creating\deleting a
	 *         relationship.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private HashMap<String, Object> populateReln(Entry entry, Boolean isDelete) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		Collection<String> attrKeys = entry.getAttributeCollection();
		String classType = entry.getString(CDMConstants.CDM_CLASSTYPE_SYSTEM_ATTRIBUTE);
		if (classType == null || classType.length() == 0) {
			classType = parsedClassType;
		} else {
			classType = parseCDMClassType(classType);
		}

		map.put(ITRegistryConstants.ATTR_RELATIONSHIP_TYPE, classType);
		for (String attrName : attrKeys) {
			Object attrValue = entry.getAttribute(attrName).getValue(0);
			if (attrName.startsWith(INTERNAL_ATTRIBUTES)) {
				continue;
			}
			if (attrValue != null) {
				if (attrName.equals(IdMLConstants.RELATIONSHIP_SOURCE_ATTR)) {
					Guid sourceGuid = parseGuid(attrValue);
					if (sourceGuid == null) {
						throw new Exception(resHash.getString("IT.REGISTRY.CONN.INCORRECT.GUID.FORMAT", attrName));
					}
					String specificAttributeName = null;
					if (isDelete) {
						specificAttributeName = ITRegistryConstants.RELATIONSHIP_SOURCE_GUID_ATTR;
					} else {
						specificAttributeName = ITRegistryConstants.RELATIONSHIP_SOURCE_ATTR;
					}
					map.put(specificAttributeName, sourceGuid);
				} else if (attrName.equals(IdMLConstants.RELATIONSHIP_TARGET_ATTR)) {
					Guid targetGuid = parseGuid(attrValue);
					if (targetGuid == null) {
						throw new Exception(resHash.getString("IT.REGISTRY.CONN.INCORRECT.GUID.FORMAT", attrName));
					}
					String specificAttributeName = null;
					if (isDelete) {
						specificAttributeName = ITRegistryConstants.RELATIONSHIP_TARGET_GUID_ATTR;
					} else {
						specificAttributeName = ITRegistryConstants.RELATIONSHIP_TARGET_ATTR;
					}
					map.put(specificAttributeName, targetGuid);
				} else {
					map.put(attrName, attrValue);
				}
				printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { attrName, attrValue });
			}
		}

		return map;
	}

	/**
	 * This is preparation method for Iterator mode. This will fetch values of
	 * CI\Relationship from the IT registry database. The values will be
	 * selected based on 'Attribute Filter' parameter or 'Date Filter'
	 * parameter. The 'MSS Name' parameter determines the artifacts for whose
	 * MSSs should be searched. All CIs\Relationships will be fetched if no
	 * value is supplied in either of the above parameters.
	 * 
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public void selectEntries() throws Exception {
		if (iterateAllTypes) {
			classTypes.addAll(getTypes().keySet());
		} else {
			classTypes.add(classType);
		}
		executeQuery(parseCDMClassType(classTypes.remove(0)));
	}

	/**
	 * Queries IT registry for a particular type of CI/Relationship.
	 * 
	 * @param classType
	 *            the class type which items will be read.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void executeQuery(String classType) throws Exception {
		
		
		if (TRUE_VALUE.equalsIgnoreCase(isDeleted)){
	      if (this.worksWithCIs) {
	        
	        Guid[] aGuid = this.dis.getDeleted(classType, this.sinceDate);
	        
	        for (Guid guid : aGuid) {
	          HashMap hm = new HashMap();
	          hm.put(ITRegistryConstants.ATTR_CLASS_TYPE, classType);
	          hm.put(ITRegistryConstants.ATTR_OUTPUT_GUID, guid);
	          results.add(hm);
	        }
	        
	        return;
	      }
	      throw new Exception(resHash.getString("IT.REGISTRY.CONN.RELATION.IS.DELETE.ERROR", new Object[] { classType }));
	    }
		
		if (TRUE_VALUE.equalsIgnoreCase(useDate)) {
			Date sinceDate = parseDateFilter(dateFilter);
			if (worksWithCIs) {
				results.addAll(Arrays.asList(dis.getManagedElements(classType, sinceDate)));
			} else {
				results.addAll(Arrays.asList(dis.getRelationships(classType, null, null, sinceDate)));
			}
		} else {
			results.addAll(Arrays.asList(getResourcesUsingFilter(classType, parseAttributeFilter(attributeFilter))));
		}
		
		/* if (this.worksWithCIs) {
			 
			 addSourceToken();
		 }*/
	}
	
	/*
	private void addSourceToken()throws Exception{
		
	    for (HashMap hm : this.results) {
	      Guid meGuid = (Guid)hm.get(ITRegistryConstants.ATTR_OUTPUT_GUID);
	      if (this.mssGuid != null) {
	        
	        String sourceToken = this.dis.getSourceToken(this.mssGuid, meGuid);
	        
	        hm.put(ITRegistryConstants.SOURCE_TOKEN, sourceToken);
	        printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { sourceToken, meGuid });
	      } else {
	        
	        HashMap[] sourceToken = this.dis.getSourceTokens(meGuid);
	        
	        for (HashMap st : sourceToken) {
	          hm.put(ITRegistryConstants.SOURCE_TOKEN, st.get(ITRegistryConstants.SOURCE_TOKEN));
	          printDebugMessage("IT.REGISTRY.CONN.PARAMETER.INITIALIZED", new Object[] { sourceToken, meGuid });
	        }
	      }
	    }
	  }

	*/
	
	/**
	 * Reads CIs/Relationships from the IT registry database.
	 * 
	 * @param classType
	 *            the resource class type.
	 * @param filter
	 *            limits the returned resources. For relationships this is done
	 *            through their source and target CIs. For CIs through a set of
	 *            their attributes.
	 * @return a array with CIs/Relationships, each represented as a map.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private HashMap[] getResourcesUsingFilter(String classType, HashMap<String, Object> filter) throws Exception {
		// fetch MSS Guids
		HashMap[] result = null;
		int iScope = 0;
		if (worksWithCIs) {
			// at least the ClassType attribute should be present.
			filter.put(ITRegistryConstants.ATTR_CLASS_TYPE, classType);
			if (mssGuid == null) {
				mssGuid = getMSSGuid(mssName);
			}
			if (TRUE_VALUE.equals(scope))
		        iScope = 1;
		      else {
		        iScope = 0;
		      }
			// last parameter 0 implies current class.
			result = dis.get(mssGuid, filter,iScope);
		} else {
			String sourceClass = (String) filter.get(ITRegistryConstants.RELATIONSHIP_SOURCE_CLASS_ATTR);
			String targetClass = (String) filter.get(ITRegistryConstants.RELATIONSHIP_TARGET_CLASS_ATTR);
			result = dis.getRelationships(classType, sourceClass, targetClass, null);
		}
		return result;
	}

	/**
	 * This method returns a single entry object for a CI\Relationship searched
	 * as per criteria. Refer to selectEntries for more details.
	 * 
	 * @return the next read entry.
	 */
	public Entry getNextEntry() throws Exception {
		Entry entry = null;
		if (results.isEmpty()) {
			if (iterateAllTypes && !classTypes.isEmpty()) {
				executeQuery(parseCDMClassType(classTypes.remove(0)));
				entry = getNextEntry();
			}
		} else {
			entry = buildNextEntry(results.remove(0));
		}
		return entry;
	}

	/**
	 * This creates an Entry Object for Input Map of Connector.
	 * 
	 * @param entryMap
	 *            a map containing information for the read CI/Relationship.
	 * @return an entry object.
	 */
	private Entry buildNextEntry(HashMap entryMap) {
		Entry entry = new Entry();
		Set<Map.Entry<?, ?>> set = entryMap.entrySet();
		for (Map.Entry<?, ?> mapEntry : set) {
			String key = (String) mapEntry.getKey();
			if (worksWithCIs) {
				addCIAttribute(entry, key, mapEntry.getValue());
			} else {
				addRelationshipAttribute(entry, key, mapEntry.getValue());
			}
		}
		return entry;
	}

	/**
	 * Adds a new attribute to the entry of a Configuration Item.
	 * 
	 * @param entry
	 *            the entry.
	 * @param key
	 *            the new attribute's key.
	 * @param value
	 *            the new attribute's value.
	 */
	private void addRelationshipAttribute(Entry entry, String key, Object value) {
		if (key.equals(ITRegistryConstants.RELATIONSHIP_SOURCE_GUID_ATTR)) {
			entry.setAttribute(IdMLConstants.RELATIONSHIP_SOURCE_ATTR, value);
		} else if (key.equals(ITRegistryConstants.RELATIONSHIP_TARGET_GUID_ATTR)) {
			entry.setAttribute(IdMLConstants.RELATIONSHIP_TARGET_ATTR, value);
		} else if (key.equalsIgnoreCase(ITRegistryConstants.ATTR_RELATIONSHIP_TYPE)) {
			entry.setAttribute(CDMConstants.CDM_CLASSTYPE_SYSTEM_ATTRIBUTE, value);
		}
	}

	/**
	 * Adds a new attribute to the entry of a Relationship.
	 * 
	 * @param entry
	 *            the entry.
	 * @param key
	 *            the new attribute's key.
	 * @param value
	 *            the new attribute's value.
	 */
	private void addCIAttribute(Entry entry, String key, Object value) {
		if (key.equals(ITRegistryConstants.ATTR_OUTPUT_GUID)) {
			// guid
			entry.setAttribute(ITRegistryConstants.ATTR_GUID, value);
		}
		if (key.equals(ITRegistryConstants.SOURCE_TOKEN)) {
		      //entry.setAttribute(ITRegistryConstants.SOURCE_TOKEN, value);
		      entry.setAttribute(CDM_PREFIX+ITRegistryConstants.SOURCE_TOKEN, value);
		}
		if (key.equalsIgnoreCase(ITRegistryConstants.IDENTIFYING_ATTRIBUTE)) {
			// identifying attributes
			Set<Map.Entry> identifyingAttributesMap = ((HashMap) value).entrySet();
			for (Map.Entry<?, ?> identifyingAttribute : identifyingAttributesMap) {
				String aKey = (String) identifyingAttribute.getKey();
				Object aValue = identifyingAttribute.getValue();
				entry.addAttributeValue(CDM_PREFIX + aKey, aValue);
			}
		} else if (key.equalsIgnoreCase(ITRegistryConstants.MANAGEMENT_SOFTWARE_SYSTEM)) {
			// MSS information
			entry.setAttribute(ATTR_MSS, value);
		} else if (key.equalsIgnoreCase(ITRegistryConstants.ATTR_CLASS_TYPE)) {
			// class type
			entry.setAttribute(CDMConstants.CDM_CLASSTYPE_SYSTEM_ATTRIBUTE, value);
		} else {
			// default
			entry.addAttributeValue(CDM_PREFIX + key, value);
		}
	}

	/**
	 * This will return an Entry (CI\Relationship) based on the conditions
	 * specified in the Link Criteria of the Connector. The returned attributes
	 * will comply with the Common Data Model. In case more than one entry is
	 * found 'On Multiple Entries' hook should be enabled. <br>
	 * 
	 * The search criteria should use logical AND conditions between the
	 * separate criterion and the comparison should be "equals". Otherwise, an
	 * Exception will be thrown.
	 * 
	 * @param searchCrit
	 *            the search criteria.
	 * @return the found entry.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Entry findEntry(SearchCriteria searchCrit) throws Exception {
		clearFindEntries();
		Vector<?> criteria = searchCrit.getCriteria();
		Entry entry = null;
		boolean checkAddEntry = false;

		HashMap<String, Object> attrMap = new HashMap<String, Object>();
		// Build up the search filter. Only the AND logical operator is
		// supported.
		if (searchCrit.getType() == SearchCriteria.SEARCH_AND) {
			for (int i = 0; i < criteria.size(); i++) {
				SearchCriteria.rscSearch crit = (SearchCriteria.rscSearch) criteria.get(i);
				if (crit.match == SearchCriteria.EXACT && checkCDMAttribute(crit.name)) {
					String attributeName = parseAttributeName(crit.name);
					Object attributeValue = crit.value;
					//commented the below code as part of 15168 feature
					/*if (ITRegistryConstants.ATTR_OUTPUT_GUID.equals(attributeName)) {
						attributeValue = parseGuid(attributeValue);
					}*/
					attrMap.put(attributeName, attributeValue);
				} else {
					throw new Exception(resHash.getString("IT.REGISTRY.CONN.CRITERIA_ERROR", searchCrit.getSimpleFilter()));
				}
			}
		} else if (searchCrit.getType() == SearchCriteria.SEARCH_OR) {
			throw new Exception(resHash.getString("IT.REGISTRY.CONN.CRITERIA_ERROR", searchCrit.getSimpleFilter()));
		}

		classTypes.clear();
		if (iterateAllTypes) {
			classTypes.addAll(getTypes().keySet());
		} else {
			classTypes.add(classType);
		}

		for (String classType : classTypes) {
			HashMap<?, ?>[] resultMap = getResourcesUsingFilter(parseCDMClassType(classType), attrMap);
			for (int i = 0; i < resultMap.length; i++) {
				entry = buildNextEntry(resultMap[i]);
				checkAddEntry = addFindEntry(entry);
				if (!checkAddEntry) {
					break;
				}
			}
		}
		
		/*
		//commented the below code as part of 15168 feature
		//SUPPORT FOR ALIAS GUID
	    for (String classType : this.classTypes) {
	        HashMap[] resultMap = getResourcesUsingFilter(parseCDMClassType(classType), attrMap);
	        for (int i = 0; i < resultMap.length; ++i) {
	          
	        	if (returnGuid.equalsIgnoreCase(ITRegistryConstants.MASTER)) {
	            
	        		Guid guid = dis.getMaster((Guid)resultMap[i].get(ITRegistryConstants.ATTR_OUTPUT_GUID));
	        		if (guid != null) {
	            	
	        			entry = buildNextEntry(resultMap[i]);
	        			entry.setAttribute(ITRegistryConstants.ATTR_GUID, guid);
	            }
	          } //Master -if
	          else  {
	            Guid[] amGuid;
	            Guid[] aGuid;
	            if (this.returnGuid.equalsIgnoreCase(ITRegistryConstants.ALIASES)) {
	              
	              amGuid = dis.getAliases((Guid)resultMap[i].get(ITRegistryConstants.ATTR_OUTPUT_GUID));
	              if ((amGuid != null) && (amGuid.length > 1)) {
	            	 
	            	aGuid = new Guid[amGuid.length];
	                System.arraycopy(amGuid, 1, aGuid, 0, amGuid.length-1 );
	                entry = buildNextEntry(resultMap[i]);
	                entry.setAttribute(ITRegistryConstants.ALIAS_GUID, aGuid);
	              }
	            }// aliases if 
	            else if (this.returnGuid.equalsIgnoreCase(ITRegistryConstants.MASTER_AND_ALIASES)) {
	              
	              amGuid = dis.getMasterAndAliases((Guid)resultMap[i].get(ITRegistryConstants.ATTR_OUTPUT_GUID));
	              
	              if (amGuid != null) {
	                entry = buildNextEntry(resultMap[i]);
	                entry.setAttribute(ITRegistryConstants.ATTR_GUID, amGuid[0]);
	                
	                if (amGuid.length > 1) {
	                  
	                	aGuid = new Guid[amGuid.length - 1];
	                	System.arraycopy(amGuid, 1, aGuid, 0, amGuid.length - 1);
	                	entry.setAttribute(ITRegistryConstants.ALIAS_GUID, aGuid);
	              }
	            }
	          }
	       } //Master - else
	       checkAddEntry = addFindEntry(entry);
		   if (!(checkAddEntry)) {
		            break;
		   }
	    }
	  }*/

		
		

		if (getFindEntryCount() == 1) {
			return getFirstFindEntry();
		} else {
			return null;
		}
	}

	/**
	 * This method discovers the MSS Guid for the selected MSSName.
	 * 
	 * @param mssName
	 *            name of the MSS.
	 * @return GUID of MSS.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private Guid getMSSGuid(String mssName) throws Exception {
		Guid guid = null;
		if (mssName != null) {
			Map<String, Map<String, Object>> mssMap = getMSS();
			if (mssMap.containsKey(mssName)) {
				Map<?, ?> map = mssMap.get(mssName);
				guid = (Guid) map.get(ITRegistryConstants.ATTR_OUTPUT_GUID);
			}
		}

		return guid;
	}

	/**
	 * This method will close Connection to the IT registry database and
	 * shutdown NRS service. It will also clear the counter for Iterator mode.
	 * 
	 * @throws Exception
	 *             if a problem occur.
	 */
	@Override
	public void terminate() throws Exception {
		classTypes.clear();
		results.clear();
		try {
			if (connection != null && !connection.isClosed()) {
				connection.commit();
				connection.close();
			}
		} catch (Exception e) {
			if (debugMode()) {
				debug(resHash.getString("IT.REGISTRY.CONN.UNABLE.TO.CLOSE", e.getMessage()));
			}
		}
		super.terminate();
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
			initDatabase();
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

		if (TRUE_VALUE.equals(useITRegistry)) {
			metadata = MetaDataFactory.getITRegistryMetaData(jdbcUrl, jdbcDriver, dbUsername, dbPassword);
		} else {
			metadata = MetaDataFactory.getJarMetaData();
		}
		return metadata.getTypes(artifactType);
	}

	/**
	 * Returns the version of the CDM that this Connector is using. depending on
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
		MetaData metadata = null;

		if (TRUE_VALUE.equals(useITRegistry)) {
			metadata = MetaDataFactory.getITRegistryMetaData(jdbcUrl, jdbcDriver, dbUsername, dbPassword);
		} else {
			metadata = MetaDataFactory.getJarMetaData();
		}

		return metadata.getCdmVersion();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersion() {
		return "1.1-di7.1.1 %I% 20%E%";
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
	private void printDebugMessage(String msgKey, Object... params) {
		if (params == null || params.length == 0) {
			debug(resHash.getString(msgKey));
		} else if (params.length == 1) {
			debug(resHash.getString(msgKey, params[0]));
		} else {
			debug(resHash.getString(msgKey, params));
		}
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
	 * Returns the attribute filter configured for an IT registry Ci and
	 * Relationship Connector in Iterator mode.
	 * 
	 * @return the value of "Attrbute Filter" parameter.
	 */
	public String getAttributeFilter() {
		return attributeFilter;
	}

	/**
	 * Returns the date filter configured for an IT registry Ci and Relationship
	 * Connector in Iterator mode.
	 * 
	 * @return the value of "Date Filter" parameter.
	 */
	public String getDateFilter() {
		return dateFilter;
	}

	/**
	 * Parses the provided attribute filter. The attributes in the filter will
	 * use the following format:<br> cdm:Model=T61p,cdm:Manufacturer=IBM <br>
	 * Note:The name-value pair is separated by "=" (equals sign) and the
	 * attributes are separated by ',' (comma).
	 * 
	 * @param attrFilter
	 *            string that needs to be parsed
	 * @return if a valid string is passed, a HashMap with the name-value pairs
	 *         will be returned. Otherwise, an empty HashMap will be returned.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private HashMap<String, Object> parseAttributeFilter(String attrFilter) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (attrFilter.length() == 0) {
			return map;
		}
		// checks if attributes are separated by ',' and name-va;ue pair is
		// separated by '='. if not throw an execption.
		if (attrFilter.contains(ATTR_SEPARATOR) || attrFilter.contains(MATCH)) {
			String[] str = attrFilter.split(ATTR_SEPARATOR);
			for (String s : str) {
				String[] attrNameValue = s.split(MATCH);
				if (attrNameValue != null && attrNameValue.length == 2 && checkCDMAttribute(attrNameValue[0])) {
					boolean isPresent = map.containsKey(parseAttributeName(attrNameValue[0]));
					// Check for duplicate attributes.
					if (isPresent) {
						throw new Exception(resHash.getString("IT.REGISTRY.CONN.FILTER.ERROR", new Object[] { s }));
					}
					map.put(parseAttributeName(attrNameValue[0]), attrNameValue[1]);
				} else {
					throw new Exception(resHash.getString("IT.REGISTRY.CONN.FILTER.ERROR", new Object[] { s }));
				}
			}
		} else {
			throw new Exception(resHash.getString("IT.REGISTRY.CONN.FILTER.ERROR", new Object[] { attrFilter }));
		}
		return map;
	}

	/**
	 * Validates "Date Filter" parameter. The date should be provided in Short
	 * Format.
	 * 
	 * @param dateString
	 *            the String that needs to be parsed.
	 * @return if the parameter is populated, else returns null.
	 * @throws Exception
	 *             if the date filter is not a valid date in short format.
	 */
	private Date parseDateFilter(String dateString) throws Exception {
		if (dateString == null || dateString.trim().equalsIgnoreCase(EMPTY_STRING)) {
			throw new Exception(resHash.getString("IT.REGISTRY.CONN.PARAMETER.NOT.PROVIDED", ITRegistryConnector.PARAM_DATE_FILTER));
		}
		DateFormat dtFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		Date date = dtFormat.parse(dateString);
		Date currentDate = new Date();
		if (date.after(currentDate)) {
			throw new Exception(resHash.getString("IT.REGISTRY.CONN.INCORRECT.ATTR.VALUE", new Object[] {
					ITRegistryConnector.PARAM_DATE_FILTER, date }));
		}
		return date;
	}

	/**
	 * Removes the prefix for CDM and Naming Context Attribute
	 * 
	 * @param attrName
	 *            the name to be parsed.
	 * @return the parsed attribute name.
	 */
	private String parseAttributeName(String attrName) {
		int colonIndex = attrName.indexOf(GENERIC_SEPARATOR);
		if (colonIndex == -1) {
			return attrName;
		}
		return attrName.substring(colonIndex + 1);
	}

	/**
	 * Check if the attribute is a CDM attribute
	 * 
	 * @param attrName
	 *            the attribute to be checked.
	 * @return whether the attribute starts with 'cdm:'.
	 */
	private boolean checkCDMAttribute(String attrName) {
		return attrName.startsWith(CDM_PREFIX);
	}

	/**
	 * Checks if the attribute is a Naming Context
	 * 
	 * @param attrName
	 *            the attribute to be checked.
	 * @return whether this attribute starts with 'nc:'.
	 */
	private boolean checkNamingContext(String attrName) {
		return attrName.startsWith(NAMING_CONTEXT_PREFIX);
	}

	/**
	 * Parses CDM class present in 'Class Type' parameter. i.e. converts
	 * 'cdm:sys.ComputerSystem' into 'ComputerSystem'
	 * 
	 * @param inputClassType
	 *            the class to be parsed.
	 * 
	 * @return a String containing the parsed class type.
	 */
	private String parseCDMClassType(String inputClassType) {
		String resultClassType = inputClassType;
		int index = inputClassType.lastIndexOf(ITRegistryConstants.PACKAGE_SEPARATOR);
		if (index >= 0) {
			index += ITRegistryConstants.PACKAGE_SEPARATOR.length();
			resultClassType = inputClassType.substring(index);
		} else {
			index = inputClassType.lastIndexOf(CDM_PREFIX);
			if (index >= 0) {
				index += CDM_PREFIX.length();
				resultClassType = inputClassType.substring(index);
			}
		}
		return resultClassType;
	}

	/**
	 * Returns the Management Software Systems (MSSs) present in IT registry
	 * database. This will help populate 'MSS Guid' parameter and is applicable
	 * only for Lookup and Iterator mode.
	 * 
	 * @return a HashMap of MSSs. For each entry the key is either the MSSName
	 *         of the MSS or a combination of its identifying attributes (the
	 *         format is [ManufacturerName, ProductName, Hostname], and the
	 *         value is a HashMap containing the full information for the MSS.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Map<String, Map<String, Object>> getMSS() throws Exception {
		if (dis == null) {
			initDataIntergrationServices();
		}
		Map<String, Object>[] maps = dis.getMSS(null);
		Map<String, Map<String, Object>> mssMap = new TreeMap<String, Map<String, Object>>();

		for (int iter = 0; iter < maps.length; iter++) {
			String sKey = (String) maps[iter].get(MSSNAME);
			if (sKey == null) {
				sKey = "[" + (String) maps[iter].get(MANUFACTURER_NAME) + ":";
				sKey += (String) maps[iter].get(PRODUCT_NAME) + ":";
				sKey += (String) maps[iter].get(HOST_NAME) + "]";
			}
			mssMap.put(sKey, maps[iter]);
		}
		return mssMap;
	}

	/**
	 * Unwraps the passed ConfigurationItemId using reflection.
	 * 
	 * @param ciId
	 *            the GUID wrapper used.
	 * @return the GUID value wrapped by the ConfigurationItemId.
	 * 
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private Guid unwrapConfigurationItemId(ConfigurationItemId ciId) throws Exception {
		Class<?> wrapperClass = ciId.getClass();
		Method getValueMethod = wrapperClass.getDeclaredMethod("getValue", (Class[]) null);
		getValueMethod.setAccessible(true);
		return (Guid) getValueMethod.invoke(ciId, (Object[]) null);
	}

}
