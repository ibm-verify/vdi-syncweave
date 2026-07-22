/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm;

import static com.ibm.di.cdm.core.CDMConstants.CDM_CLASSTYPE_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.CDM_ID_SYSTEM_ATTRIBUTE;

import java.security.Policy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.collation.platform.model.Guid;
import com.collation.platform.model.GuidFormatException;
import com.collation.platform.model.ModelObject;
import com.collation.platform.model.topology.process.ManagementSoftwareSystem;
import com.collation.proxy.api.client.ApiConnection;
import com.collation.proxy.api.client.ApiException;
import com.collation.proxy.api.client.ApiSession;
import com.collation.proxy.api.client.CMDBApi;
import com.collation.proxy.api.client.DataResultSet;
import com.ibm.cdb.api.ApiFactory;
import com.ibm.cdb.topomgr.MissingKeyException;
import com.ibm.di.cdm.core.MessageUtils;
import com.ibm.di.cdm.core.MetaData;
import com.ibm.di.cdm.core.NamingRule;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.taddm.cdm.TADDMMetaData;
import com.ibm.di.connector.taddm.cdm.model.EntryConverter;
import com.ibm.di.connector.taddm.cdm.model.ModelObjectConverter;
import com.ibm.di.connector.taddm.cdm.query.TADDMQueryBuilder;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.SearchCriteria.rscSearch;
import com.ibm.di.server.criteria.DefaultSearchCriteriaMatcher;
import com.ibm.di.server.validate.ValidationException;
import com.ibm.di.util.ParameterSubstitution;
import com.ibm.di.util.SchemaUtils;

/**
 * This Connector is used for working with TADDM. It supports reading, writing
 * and deleting data from it. In addition the Connector supports "IdML mode" in
 * addition to native mode. In this case, all read data complies with CDM
 * (names, naming rules, etc.) and is fully identifiable in the IdML sense.
 */
public class TADDMWorkerConnector extends Connector {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * If this value is used, the default TADDM Java API port specified in
	 * <taddm-sdk>/etc/collation.properties will be used.
	 */
	private static final int DEFAULT_TADDM_PORT = -1;

	/**
	 * The defaut value used for numerical parameters.
	 */
	protected static final int UNKNOWN = -1;

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
	 * The name of the Hostname parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_HOSTNAME = "hostname";

	/**
	 * The name of the Port parameter from the Connector's configuration panel.
	 */
	private static final String PARAM_PORT = "port";

	/**
	 * The name of the Username parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_USERNAME = "username";

	/**
	 * The name of the Password parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_PASSWORD = "password";

	/**
	 * The name of the Depth parameter from the Connector's configuration panel.
	 */
	private static final String PARAM_DEPTH = "depth";

	/**
	 * The name of the Get Extended Attributes parameter from the Connector's
	 * configuration panel.
	 */
	private static final String PARAM_EXTENDED_ATTRIBUTES = "extendedAttrs";

	/**
	 * The name of the Get Domain Attributes parameter from the Connector's
	 * configuration panel.
	 */
	private static final String PARAM_DOMAIN_ATTRIBUTES = "domainAttrs";

	/**
	 * The name of the Enable IdML Mode parameter from the Connector's
	 * configuration panel.
	 */
	private static final String PARAM_IDML_MODE = "idmlMode";

	/**
	 * The name of the Get Explicit Relationships parameter from the Connector's
	 * configuration panel.
	 */
	private static final String PARAM_EXPLICIT_RELNS = "explicitRelns";

	/**
	 * The name of the Get MSS parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_MSS = "mssAttrs";

	/**
	 * The name of the Fetch Size parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_FETCH_SIZE = "fetchSize";

	/**
	 * The name of the MSS GUID parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_MSS_GUID = "mssName";

	/**
	 * The name of the MQL Select parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_MQL_SELECT = "mqlSelect";

	/**
	 * The name of the Use SSL parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_USE_SSL = "useSSL";

	/**
	 * The name of the ModelObject property.
	 */
	private static final String MODEL_OBJECT_SYSTEM_PROPERTY = "modelObject";

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
	 * Enables parameter substitution in the MQL Select field.
	 */
	private boolean enableParamSubstitution;

	/**
	 * The number of implicit relationship levels that will traversed for each
	 * CI.
	 */
	protected Integer depth;

	/**
	 * Determines the size of each fetch retrieved from TADDM while querying
	 * data. The default is 10. Can be use for fine tune TADDM behavior.
	 */
	private int fetchSize;

	/**
	 * Determines whether SSL is used when connecting to TADDM.
	 */
	private boolean useSSL;

	/**
	 * The GUID of the MSS whose data we would like to query.
	 */
	private Guid mssGuid;

	/**
	 * The TADDM session.
	 */
	private ApiSession session;

	/**
	 * The API used for communicating to TADDM.
	 */
	protected CMDBApi api;

	/**
	 * The current result set.
	 */
	private DataResultSet resultSet;

	/**
	 * The Log used for logging messages.
	 */
	private Log log;

	/**
	 * The used meta data API.
	 */
	private TADDMMetaData metaData;

	/**
	 * The API used for building entries from Model Objects.
	 */
	protected ModelObjectConverter objectConverter;

	/**
	 * The API used for building Model Objects from entries.
	 */
	private EntryConverter entryConverter;

	/**
	 * This builder is used for creating TADDM queries. In addition it supports
	 * filtering.
	 */
	protected TADDMQueryBuilder queryBuilder;

	/**
	 * A list of all TADDM classes which can be queried. Used when all TADDM
	 * items must be traversed.
	 */
	protected final List<String> allPersistableClasses;

	/**
	 * The class types configured in the UI of the Connector.
	 */
	private List<String> classTypes;

	/**
	 * To traverse all TADDM items or only the ones from the specified type(s).
	 */
	private boolean queryAllClasses;

	/**
	 * Constructor.
	 */
	public TADDMWorkerConnector() {
		enableParamSubstitution = true;
		allPersistableClasses = new LinkedList<String>();
		classTypes = new LinkedList<String>();
	}

	/**
	 * Initializes the Connector and creates an API session to TADDM.
	 * 
	 * @param sessionObject
	 *            can be used for providing a session ID String to the
	 *            Connector. This, way
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public void initialize(Object sessionObject) throws Exception {
		super.initialize(null);
		long sessionId = getSessionId(sessionObject);
		boolean useSessionId = sessionId != UNKNOWN;

		resetCurrentPolicy();

		artifactType = getStringParameter(PARAM_ARTIFACT_TYPE);
		if (artifactType != null) {
			printDebugMessage("TADDM.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_ARTIFACT_TYPE, artifactType });
		} else {
			throw new Exception(getMessage("TADDM.CONN.PARAMETER.NOT.PROVIDED", PARAM_ARTIFACT_TYPE));
		}

		String classTypeString = getStringParameter(PARAM_CLASS_TYPE);
		if (isSet(classTypeString)) {
			printDebugMessage("TADDM.CONN.PARAMETER.INITIALIZED", new Object[] { PARAM_CLASS_TYPE, classType });
			String classTypeValues[] = classTypeString.split(",");
			if (classTypeValues.length == 1) {
				classType = classTypeValues[0];
			}
			classTypes.addAll(Arrays.asList(classTypeValues));
		} else {
			queryAllClasses = true;
		}

		String hostname = getStringParameter(PARAM_HOSTNAME);
		if (isSet(hostname)) {
			printDebugMessage("TADDM.CONN.PARAMETER.INITIALIZED", PARAM_HOSTNAME, hostname);
		} else {
			throw new Exception(getMessage("TADDM.CONN.PARAMETER.NOT.PROVIDED", PARAM_HOSTNAME));
		}

		int port = getIntegerParameter(PARAM_PORT, false);
		if (port == UNKNOWN) {
			port = DEFAULT_TADDM_PORT;
		}

		String username = getStringParameter(PARAM_USERNAME);
		if (isSet(username)) {
			printDebugMessage("TADDM.CONN.PARAMETER.INITIALIZED", PARAM_USERNAME, username);
		} else if (!useSessionId) {
			throw new Exception(getMessage("TADDM.CONN.PARAMETER.NOT.PROVIDED", PARAM_USERNAME));
		}

		String password = getStringParameter(PARAM_PASSWORD);
		if (isSet(password)) {
			printDebugMessage("TADDM.CONN.PARAMETER.INITIALIZED", PARAM_PASSWORD, "*****");
		} else if (!useSessionId) {
			throw new Exception(getMessage("TADDM.CONN.PARAMETER.NOT.PROVIDED", PARAM_PASSWORD));
		}

		depth = getIntegerParameter(PARAM_DEPTH, false);
		if (depth == UNKNOWN) {
			depth = CMDBApi.DEPTH_INFINITE;
		}
		fetchSize = getIntegerParameter(PARAM_FETCH_SIZE, false);

		boolean idmlMode = false;
		Boolean param = getBoolean(PARAM_IDML_MODE);
		if (param != null) {
			idmlMode = param.booleanValue();
			printDebugMessage("TADDM.CONN.PARAMETER.INITIALIZED", PARAM_IDML_MODE, param.booleanValue());
		}
		if (!idmlMode) {
			setParam(PARAM_EXPLICIT_RELNS, "false");
		}

		String mssGuidString = getStringParameter(PARAM_MSS_GUID);
		if (isSet(mssGuidString)) {
			mssGuid = parseGuid(mssGuidString);
			printDebugMessage("TADDM.CONN.PARAMETER.INITIALIZED", PARAM_MSS_GUID, mssGuidString);
		}

		useSSL = getBooleanParameter(PARAM_USE_SSL);
		String trustStoreLocation = System.getProperty("javax.net.ssl.trustStore");

		printDebugMessage("TADDM.CONN.CONNECTING");
		ApiConnection connection = ApiFactory.getInstance().getApiConnection(hostname, port, trustStoreLocation, useSSL);

		if (useSessionId) {
			printDebugMessage("TADDM.CONN.OPEN.SESSION.WITH.ID", sessionId);
			session = ApiFactory.getInstance().getSession(connection, sessionId, ApiSession.DEFAULT_VERSION);
		} else {
			printDebugMessage("TADDM.CONN.OPEN.SESSION.WITH.CREDENTIALS");
			session = ApiFactory.getInstance().getSession(connection, username, password, ApiSession.DEFAULT_VERSION);
		}

		api = session.createCMDBApi();
		metaData = new TADDMMetaData(api, idmlMode);

		queryBuilder = new TADDMQueryBuilder(metaData.createQueryFilter());
		String mqlSelect = getStringParameter(PARAM_MQL_SELECT);
		if (isIteratorMode() && isSet(mqlSelect)) {
			queryBuilder.setMQLSelect(mqlSelect);
			queryAllClasses = false;
		}
		queryBuilder.setDepth(depth);

		log = this.getLog();
		if (log == null) {
			log = new Log(this.getClass().getCanonicalName());
		}

		entryConverter = new EntryConverter(metaData, log);
	}

	/**
	 * <p>
	 * By default TADDM installs its own Security Manager with an all-permissive
	 * policy file (can be found in the SDK under /etc), if they are not already
	 * set. However, after setting the policy file TADDM does not refresh the
	 * current java.security.Policy. In TDI's case we have already installed and
	 * un-installed a Security Manager, so the Policy is already cached.
	 * Therefore, the new policy file specified by TADDM is not taken into
	 * account.
	 * </p>
	 * 
	 * <p>
	 * As a workaround, we clear the Policy completely, if there is not Security
	 * Manager installed. This, way the Policy will be created again when
	 * another Security Manager is installed.
	 * </p>
	 */
	private synchronized void resetCurrentPolicy() {
		if (System.getSecurityManager() == null) {
			Policy.setPolicy(null);
		}
	}

	/**
	 * Checks if the Connector is in Iterator mode.
	 * 
	 * @return whether the Connector is in Iterator mode.
	 */
	private boolean isIteratorMode() {
		return ((ConnectorConfig) this.getConfiguration()).getMode().equals(ConnectorConfig.ITERATOR_MODE);
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

	/**
	 * Retrieves a integer value, specified by the user.
	 * 
	 * @param parameterName
	 *            name of the integer parameter.
	 * @param supportsNegative
	 *            whether negative values are supported.
	 * @return the integer value of the parameter.
	 * @throws Exception
	 *             if the provided parameter is negative.
	 */
	protected int getIntegerParameter(String parameterName, boolean supportsNegative) throws Exception {
		int value = UNKNOWN;
		String stringValue = getStringParameter(parameterName);
		if (isSet(stringValue)) {
			try {
				value = Integer.parseInt(stringValue);
				if (value < 0 && !supportsNegative) {
					throw new Exception(getMessage("TADDM.CONN.NEGATIVE.INTEGER.PARAMETER", parameterName));
				}
			} catch (NumberFormatException nfe) {
				throw new Exception(getMessage("TADDM.CONN.INVALID.INTEGER.PARAMETER", parameterName), nfe);
			}
			printDebugMessage("TADDM.CONN.PARAMETER.INITIALIZED", parameterName, value);
		}
		return value;
	}

	/**
	 * Retrieves a boolean value, specified by the user.
	 * 
	 * @param parameterName
	 *            name of the boolean parameter.
	 * @return the boolean value of the parameter.
	 */
	protected boolean getBooleanParameter(String parameterName) {
		Boolean param = getBoolean(parameterName);
		if (param != null) {
			printDebugMessage("TADDM.CONN.PARAMETER.INITIALIZED", parameterName, param.booleanValue());
			return param.booleanValue();
		}
		return false;
	}

	/**
	 * Retrieves a String value, specified by the user.
	 * 
	 * @param parameterName
	 *            name of the String parameter.
	 * @return the String value of the parameter.
	 */
	protected String getStringParameter(String parameterName) {
		String parameter = getParam(parameterName);
		if (parameter != null) {
			parameter = parameter.trim();
		}
		return parameter;
	}

	/**
	 * Checks if this string is actually set or is just blank/null.
	 * 
	 * @param value
	 *            the string.
	 * @return whether this string holds an actual value.
	 */
	protected boolean isSet(String value) {
		return value != null && value.length() > 0;
	}

	/**
	 * Gets the TADDM session ID from the provided object. It can either be
	 * provided as a Long number or in its String form.
	 * 
	 * @param sessionObject
	 *            the session Object.
	 * @return the TADDm session ID.
	 * @throws Exception
	 *             if the provided Object in not formatted correctly.
	 */
	private long getSessionId(Object sessionObject) throws Exception {
		long sessionId = UNKNOWN;
		if (sessionObject instanceof Long) {
			sessionId = ((Long) sessionObject).longValue();
		} else if (sessionObject instanceof String) {
			try {
				sessionId = Long.parseLong((String) sessionObject);
			} catch (NumberFormatException nfe) {
				throw new Exception(getMessage("TADDM.CONN.INVALID.SESSION.ID"), nfe);
			}
		}
		return sessionId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectEntries() throws Exception {
		objectConverter = createObjectConverter();
		if (queryAllClasses) {
			printDebugMessage("TADDM.CONN.QUERYING.ALL.ITEMS");
			allPersistableClasses.addAll(metaData.getTypes(artifactType).keySet());
		} else {
			allPersistableClasses.addAll(classTypes);
		}
	}

	/**
	 * Creates the converter from TADDM model objects to entries. The additional
	 * options which enhance the returned items are also set (where available).
	 * 
	 * @return the Entry builder.
	 */
	protected ModelObjectConverter createObjectConverter() {
		ModelObjectConverter converter = new ModelObjectConverter(api, metaData, depth, fetchSize, mssGuid, log);
		converter.setDomainAttributes(getBooleanParameter(PARAM_DOMAIN_ATTRIBUTES));
		converter.setExplicitRelationships(getBooleanParameter(PARAM_EXPLICIT_RELNS));
		converter.setExtendedAttributes(getBooleanParameter(PARAM_EXTENDED_ATTRIBUTES));
		converter.setManagementSoftwareSystems(getBooleanParameter(PARAM_MSS));
		return converter;
	}

	/**
	 * Executes the provided MQL query and returns the result. The query is
	 * retrieved from the query builder.
	 * 
	 * If a previous result set still exists it will be closed.
	 * 
	 * @return the generated ResultSet.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private DataResultSet executeQuery() throws Exception {
		DataResultSet set = null;
		boolean success = false;
		while (!success && allPersistableClasses.size() > 0) {
			// use the first class
			String type = metaData.getClassType(allPersistableClasses.remove(0));
			queryBuilder.setClassType(type);
			String query = queryBuilder.buildQuery();
			// apply parameter substitution
			if (enableParamSubstitution) {
				query = ParameterSubstitution.substitute(query, getSubstitutionMap());
			}
			printDebugMessage("TADDM.CONN.QUERYING.TADDM", query);
			try {
				// The permission parameter is 'null' because we want all
				// objects that we have access to
				set = api.executeQuery(query, mssGuid, null);
				if (fetchSize != UNKNOWN) {
					set.setFetchSize(fetchSize);
				}
				success = true;
			} catch (ApiException ae) {
				set = null;
				success = false;
				if (queryMultipleClasses()) {
					// re-execute the query with the next class type
					printDebugMessage("TADDM.CONN.ERROR.QUERYING.TADDM", query);
				} else {
					throw new Exception(getMessage("TADDM.CONN.ERROR.QUERYING.TADDM", query), ae);
				}
			}
		}
		return set;
	}

	/**
	 * Checks whether more than one class type is queried by the Connector.
	 * 
	 * @return <code>true</code> if all CDM class types or a subset is used; in
	 *         case of a single class type <code>false</code> is returned.
	 */
	protected boolean queryMultipleClasses() {
		return queryAllClasses || classTypes.size() > 1;
	}

	/**
	 * Get the substitution map supported for MQL queries.
	 * 
	 * @return the substitution map.
	 */
	private HashMap<String, Object> getSubstitutionMap() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("config", getRawConnectorConfiguration());
		if (getConfiguration() != null) {
			map.put("mc", ((ConnectorConfig) getConfiguration()).getMetamergeConfig());
		}
		map.put("Connector", this);
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getNextEntry() throws Exception {
		Entry entry = null;
		do {
			entry = getNextUnfilteredEntry();
		} while (entry != null && entry.getAttributeNames().length == 0);

		return entry;
	}

	/**
	 * Unlike getNextEntry() this method can return Entries without any
	 * attributes. This can occur if in IdML mode we do not have enough
	 * attributes to satisfy any of the naming rules for that class type. In
	 * this case the getNextEntry() method will get the next model object. The
	 * UI of the Connector will use it to give detailed information for the lost
	 * objects in IdML mode.
	 * 
	 * @return a hierarchical Entry (possibly empty).
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private Entry getNextUnfilteredEntry() throws Exception {
		if (resultSet == null) {
			resultSet = executeQuery();
		}

		// drain empty class types.
		boolean hasNext = false;
		while (resultSet != null && !(hasNext = resultSet.next())) {
			resultSet.close();
			resultSet = executeQuery();
		}

		Entry entry = null;
		if (resultSet != null && hasNext) {
			ModelObject modelObject = resultSet.getModelObject(depth);
			entry = objectConverter.convert(modelObject);
			entry.setProperty(MODEL_OBJECT_SYSTEM_PROPERTY, modelObject);

		}
		return entry;
	}

	/**
	 * Returns the objects skipped during the last getNextBuilderEntry() call.
	 * 
	 * @return an Entry containing the GUID of each skipped item and a one-level
	 *         list of its explicit attributes.
	 */
	@SuppressWarnings("unused")
	private Entry getObjectsSkippedOnLastIteration() {
		return objectConverter.getSkippedModelObjects();
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry findEntry(SearchCriteria sc) throws Exception {
		clearFindEntries();

		String linkCriteriaClassType = getClassType(sc);
		if (linkCriteriaClassType != null) {
			classType = linkCriteriaClassType;
			queryAllClasses = false;
		}

		queryBuilder.setSearchCriteria(sc);
		Entry entry = null;
		Guid guid = getGuid(sc);
		if (guid != null) {
			printDebugMessage("TADDM.CONN.FOUND.GUID.IN.CRITERIA", guid);
			try {
				ModelObject modelObject = api.find(guid, depth, mssGuid, null);

				if (objectConverter == null) {
					objectConverter = createObjectConverter();
				}

				DefaultSearchCriteriaMatcher matcher = new DefaultSearchCriteriaMatcher();
				entry = objectConverter.convert(modelObject);
				if (matcher.match(entry, sc)) {
					entry.setProperty(MODEL_OBJECT_SYSTEM_PROPERTY, modelObject);
					addFindEntry(entry);
				}
			} catch (ApiException apiEx) {
				printDebugMessage("TADDM.CONN.ERROR.FIND.MODEL.OBJECT", guid, apiEx.getMessage());
			}
		} else {
			selectEntries();
			do {
				entry = getNextEntry();
			} while (addFindEntry(entry));
		}

		if (getFindEntryCount() == 1) {
			return getFirstFindEntry();
		} else {
			return null;
		}
	}

	/**
	 * Try to get class type from search criteria if there is only one.
	 * 
	 * @param searchCrit
	 *            The search criteria used to locate the entry to be modified.
	 * @return The class type found, or null if no or multiple class types are
	 *         found.
	 */
	private String getClassType(SearchCriteria searchCrit) {
		String classType = null;
		Vector<?> criteria = searchCrit.getCriteria();
		int classTypeCount = 0;
		if (searchCrit.getType() == SearchCriteria.SEARCH_AND || criteria.size() == 1) {
			int i = 0;
			while (i < criteria.size()) {
				rscSearch rscSearch = (rscSearch) criteria.get(i++);
				if (rscSearch.match == SearchCriteria.EXACT && (CDM_CLASSTYPE_SYSTEM_ATTRIBUTE.equals(rscSearch.name))) {
					classType = (String) rscSearch.value;
					classTypeCount++;
				}
			}
		}

		if (classTypeCount == 1) {
			return classType;
		} else {
			return null;
		}
	}

	/**
	 * Try to get guid from search criteria if there is only one.
	 * 
	 * @param searchCrit
	 *            The search criteria used to locate the entry to be modified.
	 * @return The guid found, or null if no or multiple guids are found.
	 * @throws GuidFormatException
	 *             if an error occurs.
	 */
	private Guid getGuid(SearchCriteria searchCrit) throws GuidFormatException {
		String guidString = null;
		Vector<?> criteria = searchCrit.getCriteria();
		int guidCount = 0;
		if (searchCrit.getType() == SearchCriteria.SEARCH_AND || criteria.size() == 1) {
			int i = 0;
			while (i < criteria.size()) {
				rscSearch rscSearch = (rscSearch) criteria.get(i++);
				if (rscSearch.match == SearchCriteria.EXACT
						&& ("cdm:Guid".equals(rscSearch.name) || "guid".equals(rscSearch.name) || CDM_ID_SYSTEM_ATTRIBUTE
								.equals(rscSearch.name))) {
					guidString = (String) rscSearch.value;
					guidCount++;
				}
			}
		}

		if (guidCount == 1) {
			return new Guid(guidString);
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteEntry(Entry entry, SearchCriteria search) throws Exception {

		Guid[] toDelete = getGuidsToDelete(entry, search);
		printDebugMessage("TADDM.CONN.DELETE.OBJECTS", toDelete.length);
		try {
			int count = api.delete(toDelete, mssGuid);
			if (count < toDelete.length) {
				printDebugMessage("TADDM.CONN.NOT.ALL.OBJECTS.DELETED", count, toDelete.length);
			}
		} catch (ApiException ae) {
			throw new Exception(getMessage("TADDM.CONN.DELETE.FAILED"), ae);
		}
	}

	/**
	 * Return array of guids that will be deleted.
	 * 
	 * @param entry
	 *            The entry data
	 * @param search
	 *            The search criteria used to locate the guids to be deleted
	 * 
	 * @return Array of Guids that will be deleted
	 * @throws Exception
	 *             if an error occurs.
	 */
	private Guid[] getGuidsToDelete(Entry entry, SearchCriteria search) throws Exception {
		List<Guid> toDelete = new ArrayList<Guid>();
		Guid guid = null;
		if (entry == null && (guid = getGuid(search)) != null) {
			toDelete.add(guid);
		} else {
			if (entry == null) {
				entry = findEntry(search);
			}
			if (entry == null) {
				throw new Exception(getMessage("TADDM.CONN.NO.ENTRY.PROVIDED"));
			}
			Attribute guidAttribute = entry.getAttribute(CDM_ID_SYSTEM_ATTRIBUTE);
			if (guidAttribute == null || guidAttribute.getValues().length == 0) {
				throw new Exception(getMessage("TADDM.CONN.NO.GUID.IN.ENTRY"));
			}
			Object[] guids = guidAttribute.getValues();
			for (int i = 0; i < guids.length; i++) {
				toDelete.add(parseGuid(guids[i]));
			}
		}
		return toDelete.toArray(new Guid[0]);
	}

	/**
	 * Parses the provided GUID object to a valid TADDM Guid.
	 * 
	 * @param guidObject
	 *            the Guid object.
	 * @return the TADDM Guid.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private Guid parseGuid(Object guidObject) throws Exception {
		Guid result = null;
		if (guidObject instanceof Guid) {
			result = (Guid) guidObject;
		} else {
			try {
				result = new Guid((String) guidObject);
			} catch (GuidFormatException gfe) {
				throw new Exception(getMessage("TADDM.CONN.INVALID.GUID", guidObject.toString()), gfe);
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putEntry(Entry newEntry) throws Exception {
		printDebugMessage("TADDM.CONN.ADD.OPERATION");
		try {
			Guid guid = updateModelObject(newEntry, null);
			printDebugMessage("TADDM.CONN.ADD.OPERATION.SUCCESS", guid);
		} catch (ApiException ae) {
			throw new Exception(getMessage("TADDM.CONN.ADD.FAILED"), ae);
		}
	}

	/**
	 * Convert Entry to ModelObject and add or update it in TADDM server.
	 * 
	 * @param newEntry
	 *            that will be added in TADDM server.
	 * @param oldEntry
	 *            the entry that will be updated in TADDM server.
	 * @return guid of added or updated ModelObject.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private Guid updateModelObject(Entry newEntry, Entry oldEntry) throws Exception {
		ModelObject modelObject = null;
		if (oldEntry != null) {
			modelObject = (ModelObject) oldEntry.getProperty(MODEL_OBJECT_SYSTEM_PROPERTY);
		}
		classType = metaData.getClassType(classType);
		modelObject = entryConverter.convert(classType, newEntry, modelObject);
		Guid guid = null;
		try {
			guid = api.update(modelObject, mssGuid);
		} catch (ApiException ae) {
			if (isMissingKeyException(ae)) {
				checkRequiredAttributes(newEntry.getAttributeNames());
			}
			throw ae;
		}
		newEntry.setAttribute(CDM_ID_SYSTEM_ATTRIBUTE, guid);
		return guid;
	}

	/**
	 * Log messages for each unsatisfied naming rule with information how to
	 * satisfy it.
	 * 
	 * @param mappedAttributes
	 *            the attributes mapped to the Entry.
	 * @throws ValidationException
	 *             if validation error occurs.
	 * 
	 */
	private void checkRequiredAttributes(String[] mappedAttributes) throws ValidationException {
		Set<String> attributes = new HashSet<String>();
		attributes.addAll(Arrays.asList(mappedAttributes));
		List<NamingRule> namingRules = null;
		try {
			namingRules = metaData.getUnsatisfiedNamingRules(classType, attributes);
			String message = null;
			for (NamingRule rule : namingRules) {
				if (rule.getIdentifiers().size() > 0) {
					message = MessageUtils.getUnsatisfiedMessage(rule);
					logmsg(message);
				}
			}
		} catch (Exception e) {
			throw new ValidationException(e);
		}
	}

	/**
	 * Check if an exception contains wrapped exception of type
	 * <code>com.ibm.cdb.topomgr.MissingKeyException</code>
	 * 
	 * @param exception
	 *            to be checked
	 * @return <b>true</b> if exception contains
	 *         <code>com.ibm.cdb.topomgr.MissingKeyException</code>, otherwise
	 *         <b>false</b>.
	 */
	private boolean isMissingKeyException(Throwable exception) {
		return exception.getMessage().contains(MissingKeyException.class.getCanonicalName());
	}

	/**
	 * Checks if this Connector supports parameter substitution.
	 * 
	 * @return whether parameter substitution is supported.
	 */
	public boolean getParameterSubstitution() {
		return enableParamSubstitution;
	}

	/**
	 * Enables/ disables parameter substitution.
	 * 
	 * @param enableParamSubstitute
	 *            whether parameter substitution should be turned on/off.
	 */
	public void setParameterSubstitution(boolean enableParamSubstitute) {
		this.enableParamSubstitution = enableParamSubstitute;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object querySchema(Object source) throws Exception {
		if (api == null || metaData == null) {
			this.initialize(null);
		}
		if (classType != null && depth != CMDBApi.DEPTH_INFINITE) {
			objectConverter = createObjectConverter();
			Entry schema = objectConverter.convertClassType(classType);
			ConnectorConfig config = (ConnectorConfig) getConfiguration();
			SchemaUtils.convertEntryToSchemaHier(schema, config, hasInputMap());
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void modEntry(Entry newEntry, SearchCriteria search, Entry oldEntry) throws Exception {
		if (oldEntry == null && search.size() > 0) {
			oldEntry = findEntry(search);
		}

		printDebugMessage("TADDM.CONN.UPDATE.OPERATION");
		try {
			Guid guid = updateModelObject(newEntry, oldEntry);
			printDebugMessage("TADDM.CONN.UPDATE.OPERATION.SUCCESS", guid);
		} catch (ApiException ae) {
			throw new Exception(getMessage("TADDM.CONN.UPDATE.FAILED"), ae);
		}
	}

	/**
	 * Returns the Management Software Systems (MSSs) present in TADDM. This
	 * will help populate 'MSS Guid' parameter.
	 * 
	 * @return a Map of MSSs and attributes.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Map<String, Map<String, Object>> getMSS() throws Exception {
		if (api == null) {
			initialize(null);
		}

		ManagementSoftwareSystem[] mssArray = api.getManagementSoftwareSystems(null, null);
		ModelObjectConverter mssConverter = createObjectConverter();

		Map<String, Map<String, Object>> mssMap = new TreeMap<String, Map<String, Object>>();
		Entry mssAttributes = null;
		String[] attrNames = null;
		Map<String, Object> attributesMap = null;
		for (ManagementSoftwareSystem mss : mssArray) {
			mssAttributes = mssConverter.convert(mss);

			attrNames = mssAttributes.getAttributeNames();
			attributesMap = new HashMap<String, Object>();

			for (String attrName : attrNames) {
				if (attrName != null) {
					attributesMap.put(attrName, mssAttributes.getString(attrName));
				}
			}
			mssMap.put(mss.getGuid().toString(), attributesMap);
		}
		return mssMap;
	}

	/**
	 * This method makes sure that
	 * {@link InternalSchema#CONNECTOR_COMPUTE_CHANGES} is set to since compute
	 * changes logic is not supported for hierarchical entries yet.
	 */
	@Override
	public void setConfiguration(Object config) {
		// FIXME: If compute changes for hierarchical entries was fixed in
		// AssemblyLineComponent this method can be removed.
		((ConnectorConfig) config).setComputeChanges(false);
		super.setConfiguration(config);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void terminate() throws Exception {
		if (resultSet != null) {
			resultSet.close();
			resultSet = null;
		}

		if (api != null) {
			api.close();
			api = null;
		}

		if (session != null) {
			session.close();
			session = null;
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
	 * Prints a debug message, if debug mode for the owning Components is
	 * enabled.
	 * 
	 * @param msgKey
	 *            message key
	 * @param params
	 *            the message;s parameters.
	 */
	protected void printDebugMessage(String msgKey, Object... params) {
		debug(getMessage(msgKey, params));
	}

	/**
	 * Gets a localized message using the provided key and adding the available
	 * values.
	 * 
	 * @param key
	 *            the message's key.
	 * @param values
	 *            the values to be added to the message.
	 * @return the formatted localized string.
	 */
	private static String getMessage(String key, Object... values) {
		return TADDMConnector.L10N.getString(key, values);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDeltaSupported() {
		return true;
	}

	/**
	 * Return used meta data.
	 * 
	 * @return the meta data.
	 */
	public MetaData getMetaData() {
		return metaData;
	}

}