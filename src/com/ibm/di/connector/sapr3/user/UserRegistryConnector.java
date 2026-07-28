/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.user;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.SearchCriteria;

import org.w3c.dom.Document;

/**
 * <p>
 * The SAP R/3 User Registry Connector. Version %sapurcn.release.version% Build
 * %sapurcn.release.build.version%.
 * </p>
 * <p>
 * The connector enables external applications, using SyncWeave, to manage users in
 * SAP R/3. It projects the SAP user database as a "registry" into the SyncWeave
 * infrastructure. It supports the following SyncWeave Connector Modes: <b>Add Only,
 * Delete, Update, Iterator, Lookup.</b> The connector supports design time
 * schema query via {@link #querySchema}.
 * </p>
 * <p>
 * <b>A note about ABAP errors and warnings</b><br>
 * The connector invokes BAPI/RFC functions in SAP to perform the connector mode
 * operations. In some cases, data passed to the BAPI/RFC functions from the XML
 * input, may result in ABAP data validation failures. An example of this case
 * could be the value for post code is not valid within the country region. The
 * BAPI/RFC functions return the results of validation checks in the "RETURN"
 * parameter of the RFC. <br>
 * <br>
 * The connector has been desingned to make the RFC return status available to
 * the Assembly Line. The connector does not interpret, or translate, ABAP
 * errors or warnings into thrown exceptions. The connector registers a script
 * bean named "urcAbapErrorCache". The bean is registered for all connector
 * modes and can be accessed in connector hooks. The bean is an instance of
 * {@link AbapErrorCache}. Script code in a connector hook can use this
 * information to perform contingency actions as required. The cache is reset
 * before the execution of each connector method. Example script code is shown
 * below:<br>
 * <br>
 * <code>
 * var errs = urcAbapErrorCache.getLastErrorSet();<br>
 * if (errs.size() &gt 0) {<br>
 * &nbsp    task.logmsg("********** There were ABAP Errors *********");<br>
 * &nbsp    for (var i = 0; i &lt errs.size(); ++i) {<br>
 * &nbsp&nbsp        var errInfo = errs.get(i);<br>
 * &nbsp&nbsp        task.logmsg("The message is: " + errInfo.getMsg());<br>
 * &nbsp&nbsp        task.logmsg("The message number is: " + errInfo.getMsgNum().toString());<br>
 * &nbsp    }<br>
 * }<br>
 * <br>
 * var warns = urcAbapErrorCache.getLastWarningSet();<br>
 * if (warns.size() &gt 0) {<br>
 * &nbsp    task.logmsg("********** There were ABAP Warnings *********");<br>
 * &nbsp    for (var i = 0; i &lt warns.size(); ++i) {<br>
 * &nbsp&nbsp        var errInfo = warns.get(i);<br>
 * &nbsp&nbsp        task.logmsg("The message is: " + errInfo.getMsg());<br>
 * &nbsp&nbsp        task.logmsg("The message number is: " + errInfo.getMsgNum().toString());<br>
 * &nbsp    }<br>
 * }<br>
 * </code>
 * </p>
 * <p>
 * A given SAP user entry is represented as XML by the connector, and is set as
 * the value of the connector attribute named "sapUserXml". The value if this
 * attribute is always an XML string. Its XSchema is defined in the user guide
 * for the connector. An example XML instance is shown below.
 * </p>
 * <p>
 * 
 * <pre>
 *  &amp;ltUser&amp;gt
 *  &amp;ltsapUserName&amp;gt&amp;lt/sapUserName&amp;gt
 *  &amp;ltsapUserPassword&amp;gt&amp;lt/sapUserPassword&amp;gt
 *  &amp;ltsapUserAlias&amp;gt
 *  &amp;ltaliasName&amp;gt&amp;lt/aliasName&amp;gt
 *  &amp;lt/sapUserAlias&amp;gt     
 *  &amp;ltsapAddress&amp;gt
 *  &amp;lttitle&amp;gt&amp;lt/title&amp;gt
 *  &amp;ltacademicTitle&amp;gt&amp;lt/academicTitle&amp;gt
 *  &amp;ltfirstName&amp;gt&amp;lt/firstName&amp;gt
 *  &amp;ltlastName&amp;gt&amp;lt/lastName&amp;gt
 *  &amp;ltnamePrefix&amp;gt&amp;lt/namePrefix&amp;gt
 *  &amp;ltnameFormat&amp;gt&amp;lt/nameFormat&amp;gt
 *  &amp;ltnameFormatRuleCountry&amp;gt&amp;lt/nameFormatRuleCountry&amp;gt
 *  &amp;ltisoLanguage&amp;gt&amp;lt/isoLanguage&amp;gt
 *  &amp;ltlanguage&amp;gt&amp;lt/language&amp;gt
 *  &amp;ltsearchSortTerm&amp;gt&amp;lt/searchSortTerm&amp;gt
 *  &amp;ltdepartment&amp;gt&amp;lt/department&amp;gt
 *  &amp;ltfunction&amp;gt&amp;lt/function&amp;gt
 *  &amp;ltbuildingNumber&amp;gt&amp;lt/buildingNumber&amp;gt
 *  &amp;ltbuildingFloor&amp;gt&amp;lt/buildingFloor&amp;gt
 *  &amp;ltroomNumber&amp;gt&amp;lt/roomNumber&amp;gt
 *  &amp;ltname&amp;gt&amp;lt/name&amp;gt
 *  &amp;ltname2&amp;gt&amp;lt/name2&amp;gt
 *  &amp;ltname3&amp;gt&amp;lt/name3&amp;gt
 *  &amp;ltname4&amp;gt&amp;lt/name4&amp;gt
 *  &amp;ltcity&amp;gt&amp;lt/city&amp;gt
 *  &amp;ltpostCode&amp;gt&amp;lt/postCode&amp;gt
 *  &amp;ltpoBoxPostCode&amp;gt&amp;lt/poBoxostCode&amp;gt
 *  &amp;ltpoBox&amp;gt&amp;lt/poBox&amp;gt
 *  &amp;ltstreet&amp;gt&amp;lt/street&amp;gt
 *  &amp;ltstreetNumber&amp;gt&amp;lt/streetNumber&amp;gt
 *  &amp;lthouseNumber&amp;gt&amp;lt/houseNumber&amp;gt
 *  &amp;ltcountry&amp;gt&amp;lt/country&amp;gt
 *  &amp;ltcountryIso&amp;gt&amp;lt/countryIso&amp;gt
 *  &amp;ltregion&amp;gt&amp;lt/region&amp;gt
 *  &amp;lttimeZone&amp;gt&amp;lt/timeZone&amp;gt
 *  &amp;ltprimaryPhoneNumber&amp;gt&amp;lt/primaryPhoneNumber&amp;gt
 *  &amp;ltprimaryPhoneExtension&amp;gt&amp;lt/primaryPhoneExtension&amp;gt
 *  &amp;ltprimaryFaxNumber&amp;gt&amp;lt/primaryFaxNumber&amp;gt
 *  &amp;ltprimaryFaxExtension&amp;gt&amp;lt/primaryFaxExtension&amp;gt
 *  &amp;lt/sapAddress&amp;gt
 *  &amp;ltsapCompany&amp;gt
 *  &amp;ltcompanyNameKey&amp;gt&amp;lt/companyNameKey&amp;gt
 *  &amp;lt/sapCompany&amp;gt
 *  &amp;ltsapDefaults&amp;gt
 *  &amp;ltstartMenu&amp;gt&amp;lt/startMenu&amp;gt
 *  &amp;ltoutputDevice&amp;gt&amp;lt/outputDevice&amp;gt
 *  &amp;ltprintTimeAndDate&amp;gt&amp;lt/printTimeAndDate&amp;gt
 *  &amp;ltprintDelete&amp;gt&amp;lt/printDelete&amp;gt
 *  &amp;ltdateFormat&amp;gt&amp;lt/dateFormat&amp;gt
 *  &amp;ltdecimalFormat&amp;gt&amp;lt/decimalFormat&amp;gt
 *  &amp;ltlogonLanguage&amp;gt&amp;lt/logonLanguage&amp;gt
 *  &amp;ltcattTestStatus&amp;gt&amp;lt/cattTestStatus&amp;gt
 *  &amp;ltcostCenter&amp;gt&amp;lt/costCenter&amp;gt
 *  &amp;lt/sapDefaults&amp;gt
 *  &amp;ltsapLogonData&amp;gt
 *  &amp;ltvalidFromDate&amp;gt&amp;lt/validFromDate&amp;gt
 *  &amp;ltvalidToDate&amp;gt&amp;lt/validToDate&amp;gt
 *  &amp;ltuserType&amp;gt&amp;lt/userType&amp;gt
 *  &amp;ltuserGroup&amp;gt&amp;lt/userGroup&amp;gt
 *  &amp;ltaccountId&amp;gt&amp;lt/accountId&amp;gt
 *  &amp;lttimeZone&amp;gt&amp;lt/timeZone&amp;gt
 *  &amp;ltlastLogonTime&amp;gt&amp;lt/lastLogonTime&amp;gt
 *  &amp;ltcodeVerEncryption&amp;gt&amp;lt/codeVerEncryption&amp;gt
 *  &amp;lt/sapLogonData&amp;gt
 *  &amp;ltsapSncData&amp;gt
 *  &amp;ltprintableName&amp;gt&amp;lt/printableName&amp;gt
 *  &amp;ltallowUnsecure&amp;gt&amp;lt/allowUnsecure&amp;gt
 *  &amp;lt/sapSncData&amp;gt
 *  &amp;ltsapUserGroupList&amp;gt
 *  &amp;ltgroup&amp;gt
 *  &amp;ltname&amp;gt&amp;lt/name&amp;gt
 *  &amp;lt/group&amp;gt
 *  &amp;ltgroup&amp;gt
 *  &amp;ltname&amp;gt&amp;lt/name&amp;gt
 *  &amp;lt/group&amp;gt
 *  &amp;lt/sapUserGroupList&amp;gt
 *  &amp;ltsapParameterList&amp;gt
 *  &amp;ltparameter&amp;gt
 *  &amp;ltparameterId&amp;gt&amp;lt/parameterId&amp;gt
 *  &amp;ltparameterValue&amp;gt&amp;lt/parameterValue&amp;gt
 *  &amp;lt/parameter&amp;gt
 *  &amp;ltparameter&amp;gt
 *  &amp;ltparameterId&amp;gt&amp;lt/parameterId&amp;gt
 *  &amp;ltparameterValue&amp;gt&amp;lt/parameterValue&amp;gt
 *  &amp;lt/parameter&amp;gt
 *  &amp;lt/sapParameterList&amp;gt
 *  &amp;ltsapUserEmailAddressList&amp;gt
 *  &amp;ltemail&amp;gt
 *  &amp;ltdefaultNumber&amp;gt&amp;lt/defaultNumber&amp;gt
 *  &amp;ltsmtpAddress&amp;gt&amp;lt/smtpAddress&amp;gt
 *  &amp;ltisHomeAddress&amp;gt&amp;lt/isHomeAddress&amp;gt
 *  &amp;ltsequenceNumber&amp;gt&amp;lt/sequenceNumber&amp;gt
 *  &amp;lt/email&amp;gt
 *  &amp;ltemail&amp;gt
 *  &amp;ltdefaultNumber&amp;gt&amp;lt/defaultNumber&amp;gt
 *  &amp;ltsmtpAddress&amp;gt&amp;lt/smtpAddress&amp;gt
 *  &amp;ltisHomeAddress&amp;gt&amp;lt/isHomeAddress&amp;gt
 *  &amp;ltsequenceNumber&amp;gt&amp;lt/sequenceNumber&amp;gt
 *  &amp;lt/email&amp;gt
 *  &amp;lt/sapUserEmailAddressList&amp;gt
 *  &amp;ltsapRoleList&amp;gt
 *  &amp;ltrole&amp;gt
 *  &amp;ltname&amp;gt&amp;lt/name&amp;gt
 *  &amp;ltvalidFromDate&amp;gt&amp;lt/validFromDate&amp;gt
 *  &amp;ltvalidToDate&amp;gt&amp;lt/validToDate&amp;gt
 *  &amp;lt/role&amp;gt
 *  &amp;ltrole&amp;gt
 *  &amp;ltname&amp;gt&amp;lt/name&amp;gt
 *  &amp;ltvalidFromDate&amp;gt&amp;lt/validFromDate&amp;gt
 *  &amp;ltvalidToDate&amp;gt&amp;lt/validToDate&amp;gt
 *  &amp;lt/role&amp;gt
 *  &amp;lt/sapRoleList&amp;gt
 *  &amp;ltsapProfileList&amp;gt
 *  &amp;ltprofile&amp;gt
 *  &amp;ltname&amp;gt&amp;lt/name&amp;gt
 *  &amp;lt/profile&amp;gt
 *  &amp;ltprofile&amp;gt
 *  &amp;ltname&amp;gt&amp;lt/name&amp;gt
 *  &amp;lt/profile&amp;gt
 *  &amp;lt/sapProfileList&amp;gt
 *  &amp;lt/User&amp;gt
 * </pre>
 * 
 * </p>
 * <p>
 * For a description of <b>Add Only</b> mode requirements, see
 * {@link #putEntry}.
 * </p>
 * <p>
 * For a description of <b>Update</b> mode requirements, see {@link #modEntry}.
 * </p>
 * <p>
 * For a description of <b>Delete</b> mode requirements, see
 * {@link #deleteEntry}.
 * </p>
 * <p>
 * For a description of <b>Lookup</b> mode requirements, see {@link #findEntry}.
 * </p>
 * <p>
 * For a description of <b>Iterator</b> mode requirements, see
 * {@link #getNextEntry} and {@link #selectEntries}.
 * </p>
 */
public final class UserRegistryConnector extends Connector implements
		ConnectorInterface, AbapErrorCache {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/*
	 * The version string returned by getVersion(). The %% tokens are replaced
	 * at compile time by ANT script.
	 */
	private static final String VERSION_INFO = "2.0-di7.1.1 %I% 20%E%";

	/*
	 * The connector name as reported in the log files. The %% tokens are
	 * replaced at compile time by ANT script.
	 */
	private static final String COMPONENT_NAME = "SAP ABAP Application Server User Registry";

	/*
	 * Script bean names
	 */
	private static final String ERROR_CACHE_BEAN_NAME = "urcAbapErrorCache";

	/*
	 * Supported Entry attribute schema names.
	 */
	private static final String ATTR_NAME_USER_XML = "sapUserXml";

	private static final String ATTR_SYNTAX_USER_XML = "java.lang.String";

	private static final String ATTR_LENGTH_USER_XML = "*";

	private static final String ATTR_NAME_USER_NAME = "sapUserName";

	private static final String ATTR_SYNTAX_USER_NAME = "java.lang.String";

	private static final String ATTR_LENGTH_USER_NAME = "12";

	private static final String ATTR_SCHEMA_NAME = "name";

	private static final String ATTR_SCHEMA_SYNTAX = "syntax";

	private static final String ATTR_SCHEMA_LENGTH = "length";

	/**
	* Required tags for basic XML user representation
	*/
	private static final String USER_OPEN_TAG = "<User>";
	private static final String USERNAME_OPEN_TAG = "<sapUserName>";
	private static final String USER_CLOSE_TAG = "</User>";
	private static final String USERNAME_CLOSE_TAG = "</sapUserName>";

	private Iterator nextUserIter;

	private Configuration config;

	private List lastWarningSet;

	private List lastErrorSet;

	/**
	 * Construct the Connector.
	 */
	public UserRegistryConnector() {
		setName(UserRegistryConnector.COMPONENT_NAME);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.DELETE_MODE, ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.LOOKUP_MODE, ConnectorConfig.UPDATE_MODE });
		setLastWarningSet(null);
		setLastErrorSet(null);
	}

	private Configuration getConfig() {
		return config;
	}

	private void setConfig(Configuration cfg) {
		if (cfg == null) {
			throw new IllegalArgumentException();
		}

		config = cfg;
	}

	private void setLastWarningSet(List set) {
		if (set == null) {
			lastWarningSet = Collections.synchronizedList(new LinkedList());
		} else {
			lastWarningSet = set;
		}
	}

	public void registerScriptBeans(ScriptEngine se) throws Exception {
		try {
			super.registerScriptBeans(se);
		} catch (Exception x) {
			throw new UserRegistryConnectorException(x);
		}
		se.declareStaticBean(UserRegistryConnector.ERROR_CACHE_BEAN_NAME,
				(AbapErrorCache) this);
	}

	/**
	 * Allows the caller to obtain a list of ABAP warnings that might have
	 * occured during the execution of any supported Connector method.
	 * 
	 * @return a list of warning severity AbapErrorInfo. Minimum length will be
	 *         zero.
	 */
	public List getLastWarningSet() {
		return lastWarningSet;
	}

	private void setLastErrorSet(List set) {
		if (set == null) {
			lastErrorSet = Collections.synchronizedList(new LinkedList());
		} else {
			lastErrorSet = set;
		}
	}

	/**
	 * Allows the caller to obtain a list of ABAP errors that might have occured
	 * during the execution of any supported Connector method.
	 * 
	 * @return a list of error severity AbapErrorInfo. Minimum length will be
	 *         zero.
	 */
	public List getLastErrorSet() {
		return lastErrorSet;
	}

	/**
	 * This method is called once after the connector configuration file has
	 * been provided by the caller.
	 * 
	 * @param o
	 *            SyncWeave config object. Not used.
	 * 
	 * @throws UserRegistryConnectorException
	 *             When an error happens during super class init.
	 * @throws ConfigurationException
	 *             if SAP connection parameters are invalid or XSL files are
	 *             invalid.
	 */
	public void initialize(Object o) throws UserRegistryConnectorException,
			ConfigurationException {
		try {
			super.initialize(o);
			setConfig(new ConfigurationImpl(this));
		} catch (Exception x) {
			// SyncWeave's method Connector.initialize declares to
			// throw Exception.
			throw new UserRegistryConnectorException(LogMessageHelper
					.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_UR_0019,
							new Object[] { x.getMessage() }), x);
		}
	}

	/**
	 * Return the SyncWeave Entry schema supported by this connector. The connector
	 * supports two attributes named "sapUserXml" and "sapUserName". sapUserXml
	 * is an XML string representing the attributes of a user to be operated on.
	 * sapUserName is a string representing a given user name in SAP. It is
	 * supported to allow the definition of SyncWeave "LinkCriteria" when the
	 * connector is deployed in Lookup, Delete, or Update modes.
	 * 
	 * @param source
	 *            not used.
	 * 
	 * @throws UserRegistryConnectorException
	 *             If an error occurs.
	 * @return A vector containg one Entry for SyncWeave schema display.
	 */
	public Object querySchema(Object source)
			throws UserRegistryConnectorException {
		List result = new Vector();
		try {
			Entry e = new Entry();
			e.addAttributeValue(UserRegistryConnector.ATTR_SCHEMA_NAME,
					UserRegistryConnector.ATTR_NAME_USER_XML);
			e.addAttributeValue(UserRegistryConnector.ATTR_SCHEMA_SYNTAX,
					UserRegistryConnector.ATTR_SYNTAX_USER_XML);
			e.addAttributeValue(UserRegistryConnector.ATTR_SCHEMA_LENGTH,
					UserRegistryConnector.ATTR_LENGTH_USER_XML);
			result.add(e);
			e = new Entry();
			e.addAttributeValue(UserRegistryConnector.ATTR_SCHEMA_NAME,
					UserRegistryConnector.ATTR_NAME_USER_NAME);
			e.addAttributeValue(UserRegistryConnector.ATTR_SCHEMA_SYNTAX,
					UserRegistryConnector.ATTR_SYNTAX_USER_NAME);
			e.addAttributeValue(UserRegistryConnector.ATTR_SCHEMA_LENGTH,
					UserRegistryConnector.ATTR_LENGTH_USER_NAME);
			result.add(e);
		} catch (Exception x) {
			Object[] args = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_UR_0020, args);
			getConfig().getLog().logwarn(msg);
			throw new UserRegistryConnectorException(msg, x);
		}
		return result;
	}

	/**
	 * Called by SyncWeave AL to add a new user and associated attributes to SAP R/3.
	 * 
	 * @param entry
	 *            The AL connector entry input. This connector must have an
	 *            attribute named "sapUserXml". Its value must be an XML string
	 *            conforming to the SAP User XML Schema. See user guide for more
	 *            details.
	 * 
	 * @throws UserRegistryConnectorException
	 *             If the attribute is missing from the entry.
	 * @throws ConnectorMethodException
	 *             if the SAP network call fails, or an XSL transform error
	 *             occurs.
	 * 
	 * @see Connector#putEntry
	 */
	public void putEntry(Entry entry) throws ConnectorMethodException,
			UserRegistryConnectorException {
		setLastWarningSet(null);
		setLastErrorSet(null);
		try {
			String userXmlStr = (String) entry
					.getObject(UserRegistryConnector.ATTR_NAME_USER_XML);
			if (userXmlStr == null || userXmlStr.length() == 0) {
				throw new UserRegistryConnectorException(
						LogMessageHelper
								.getMsgResource()
								.getMessage(
										LogMessageHelper.SAPR3_UR_0012,
										new Object[] { UserRegistryConnector.ATTR_NAME_USER_XML }));
			}

			WriteMethod method = new XslAddMethod(getConfig());
			ExecutionCriteria crita = new ExecutionCriteriaImpl();
			method.execute(userXmlStr, crita);
			if (method.hasAbapErrors() || method.hasAbapWarnings()) {
				setLastWarningSet(method.getAbapWarnings());
				setLastErrorSet(method.getAbapErrors());
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_UR_0016);
				getLog().logwarn(msg);
				logMethodMessages(method);
			}
		} catch (ConnectorMethodException x) {
			getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_UR_0021,
							new Object[] { x.getMessage() }));
			throw (ConnectorMethodException) x.fillInStackTrace();
		} catch (UserRegistryConnectorException x) {
			getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_UR_0021,
							new Object[] { x.getMessage() }));
			throw (UserRegistryConnectorException) x.fillInStackTrace();
		}
	}

	/**
	 * Called by SyncWeave AL to remove an existing user and associated attributes
	 * from SAP R/3.
	 * 
	 * @param entry
	 *            The AL connector entry input. This connector must have an
	 *            attribute named "sapUserXml". Its value must be an XML string
	 *            conforming to the SAP User XML Schema. The XML must, at a
	 *            minimum, contain the "sapUserName" element. Typically, the
	 *            entry will be populated by the findEntry method, which is
	 *            called by the AL before this method.
	 * 
	 * See user guide for more details.
	 * 
	 * @param search
	 *            Passed through to XSL style sheet as an XSL param.
	 * 
	 * @throws UserRegistryConnectorException
	 *             If the attribute is missing from the entry.
	 * @throws ConnectorMethodException
	 *             if the SAP network call fails, or an XSL transform error
	 *             occurs.
	 * 
	 * @see Connector#deleteEntry
	 * @see #findEntry
	 */
	public void deleteEntry(Entry entry, SearchCriteria search)
			throws ConnectorMethodException, UserRegistryConnectorException {
		setLastWarningSet(null);
		setLastErrorSet(null);
		try {

			String userXmlStr = null;
			//first try provided entry
			if (entry != null)
			{			
				userXmlStr = (String) entry.getObject(UserRegistryConnector.ATTR_NAME_USER_XML);
			}
			//next try the search criteria
			if ((userXmlStr == null)&&(search.size() != 0)&&(UserRegistryConnector.ATTR_NAME_USER_NAME.equals(search.getFirstCriteriaName())))
			{	
				userXmlStr = getBasicUserXML(search.getFirstCriteriaValue());
			}
			// Next try to lookup the entry and use the userXmlStr from that entry
			if (userXmlStr == null) {
				entry = findEntry(search);
				if (entry != null) {
					userXmlStr = (String) entry.getObject(UserRegistryConnector.ATTR_NAME_USER_XML);
				}
			}
			
			if (userXmlStr == null || userXmlStr.length() == 0) {
				Object[] args = new Object[] { UserRegistryConnector.ATTR_NAME_USER_XML };
				throw new UserRegistryConnectorException(LogMessageHelper
						.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_UR_0012, args));
			}

			WriteMethod method = new XslDeleteMethod(getConfig());
			ExecutionCriteria crita = new ExecutionCriteriaImpl(search);
			method.execute(userXmlStr, crita);
			if (method.hasAbapErrors() || method.hasAbapWarnings()) {
				setLastWarningSet(method.getAbapWarnings());
				setLastErrorSet(method.getAbapErrors());
				getLog().logwarn(
						LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_UR_0017));
				logMethodMessages(method);
			}
		} catch (ConnectorMethodException x) {
			Object[] args = new Object[] { x.getMessage() };
			getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_UR_0022, args));
			throw (ConnectorMethodException) x.fillInStackTrace();
		} catch (UserRegistryConnectorException x) {
			Object[] args = new Object[] { x.getMessage() };
			getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_UR_0022, args));
			throw (UserRegistryConnectorException) x.fillInStackTrace();
		}
	}

	/**
	*
	* If SkipLookup is configured then the entry object passed to the deleteentry() 
	* method will not be populated with a sapUserXml attribute.
	*
	* @param name - The value assigned to "sapUserName" in the configured LinkCriteria
	* @return String - The basic XML string representing the user and conforming to the required schema.
	*/
	private String getBasicUserXML(String name) {
		String userXML = USER_OPEN_TAG + USERNAME_OPEN_TAG + name + USERNAME_CLOSE_TAG + USER_CLOSE_TAG;
		return userXML;
	}
	
	/**
	 * Called by SyncWeave AL to update an existing user and associated attributes in
	 * SAP R/3.
	 * 
	 * @param entry
	 *            The AL connector entry input. This connector must have an
	 *            attribute named "sapUserXml". Its value must be an XML string
	 *            conforming to the SAP User XML Schema. The XML must, at a
	 *            minimum, contain the "sapUserName" element. Typically, the
	 *            entry will be populated by the findEntry method, which is
	 *            called by the AL before this method.
	 * 
	 * See user guide for more details.
	 * 
	 * @param search
	 *            Passed through to XSL style sheet as an XSL param.
	 * 
	 * @throws UserRegistryConnectorException
	 *             If the attribute is missing from the entry.
	 * @throws ConnectorMethodException
	 *             if the SAP network call fails, or an XSL transform error
	 *             occurs.
	 * 
	 * @see Connector#modEntry
	 * @see #findEntry
	 */
	public void modEntry(Entry entry, SearchCriteria search)
			throws ConnectorMethodException, UserRegistryConnectorException {
		setLastWarningSet(null);
		setLastErrorSet(null);
		try {
			String userXmlStr = (String) entry
					.getObject(UserRegistryConnector.ATTR_NAME_USER_XML);
			if (userXmlStr == null || userXmlStr.length() == 0) {
				throw new UserRegistryConnectorException(
						LogMessageHelper
								.getMsgResource()
								.getMessage(
										LogMessageHelper.SAPR3_UR_0012,
										new Object[] { UserRegistryConnector.ATTR_NAME_USER_XML }));
			}

			WriteMethod method = new XslModifyMethod(getConfig());
			ExecutionCriteria crita = new ExecutionCriteriaImpl(search);
			method.execute(userXmlStr, crita);
			if (method.hasAbapErrors() || method.hasAbapWarnings()) {
				setLastWarningSet(method.getAbapWarnings());
				setLastErrorSet(method.getAbapErrors());
				getLog().logwarn(
						LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_UR_0018));
				logMethodMessages(method);
			}
		} catch (ConnectorMethodException x) {
			getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_UR_0023,
							new Object[] { x.getMessage() }));
			throw (ConnectorMethodException) x.fillInStackTrace();
		} catch (UserRegistryConnectorException x) {
			getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_UR_0023,
							new Object[] { x.getMessage() }));
			throw (UserRegistryConnectorException) x.fillInStackTrace();
		}
	}

	/**
	 * Called by SyncWeave AL to find an existing user and associated attributes in
	 * SAP R/3.
	 * 
	 * @param search
	 *            Defined in the "LinkCriteria" tab of the AL. Must have a
	 *            criteria name "sapUserName". Its value is the name of the user
	 *            to be found.
	 * 
	 * @return The entry populated with attribute "sapUserXml", or
	 *         <code>null</code> if the user could not be found.
	 * 
	 * @throws UserRegistryConnectorException
	 *             If the attribute is missing from the entry.
	 * @throws ConnectorMethodException
	 *             if the SAP network call fails, or an XSL transform error
	 *             occurs.
	 * 
	 * @see Connector#findEntry
	 */
	public Entry findEntry(SearchCriteria search)
			throws ConnectorMethodException, UserRegistryConnectorException {
		setLastWarningSet(null);
		setLastErrorSet(null);
		try {
			if (search.size() == 0) {
				config.getLog().loginfo(
						LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_UR_0013));
				return null;
			}

			FindMethod method = new XslFindMethod(getConfig());
			ExecutionCriteria crita = new ExecutionCriteriaImpl(search);
			try {
				String resultStr = (String) method.execute(crita,
						FindMethod.XML_STRING);
				logMethodMessages(method);
				setLastWarningSet(method.getAbapWarnings());
				setLastErrorSet(method.getAbapErrors());
				if (method.hasAbapErrors()) {
					List errs = method.getAbapErrors();
					for (int i = 0; i < errs.size(); ++i) {
						AbapErrorInfo err = (AbapErrorInfo) errs.get(i);
						if (err.isMissingUserError()) {
							getLog()
									.debug(
											LogMessageHelper
													.getMsgResource()
													.getMessage(
															LogMessageHelper.SAPR3_UR_0028));
							return null;
						}
					}
				}

				Entry result = new Entry();
				result.setAttribute(UserRegistryConnector.ATTR_NAME_USER_XML,
						resultStr);
				getLog().debug(
						LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_UR_0029));
				return result;

			} catch (EmptyTransformResultException x) {
				getLog().warn(x.getMessage());
				logMethodMessages(method);
			}
		} catch (ConnectorMethodException x) {
			getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_UR_0024,
							new Object[] { x.getMessage() }));
			throw (ConnectorMethodException) x.fillInStackTrace();
		}

		getLog().debug(
				LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_UR_0030));
		return null;
	}

	/**
	 * Called by SyncWeave AL when the connector operates in Iterator mode inside an
	 * assembly line. It finds all user names currently managed by the connected
	 * SAP R/3 instance. It stores all users names in a cached
	 * R3UsernameIterator following a RFC lookup to obtain the names.
	 * 
	 * @throws UserRegistryConnectorException
	 *             If the attribute is missing from the entry.
	 * @throws ConnectorMethodException
	 *             if the SAP network call fails, or an XSL transform error
	 *             occurs.
	 * 
	 * @see Connector#selectEntries
	 * @see com.ibm.di.connector.sapr3.user.R3UsernameIterator
	 */
	public synchronized void selectEntries() throws ConnectorMethodException,
			UserRegistryConnectorException {
		setLastWarningSet(null);
		setLastErrorSet(null);
		try {
			FindMethod method = new XslSelectMethod(getConfig());
			ExecutionCriteria crita = new ExecutionCriteriaImpl();
			Document doc = (Document) method.execute(crita,
					FindMethod.XML_DOM_DOC);
			setLastWarningSet(method.getAbapWarnings());
			setLastErrorSet(method.getAbapErrors());
			logMethodMessages(method);
			nextUserIter = new R3UsernameIterator(doc);
		} catch (ConnectorMethodException x) {
			getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_UR_0025,
							new Object[] { x.getMessage() }));
			throw (ConnectorMethodException) x.fillInStackTrace();
		} catch (UserRegistryConnectorException x) {
			getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_UR_0025,
							new Object[] { x.getMessage() }));
			throw (UserRegistryConnectorException) x.fillInStackTrace();
		}
	}

	/**
	 * This method is called to retrieve the next entry from the connector. When
	 * there are no more entries to retrieve the function should return a null
	 * value indicating a logical end of file. This method uses the cached
	 * Iterator from {@link #selectEntries}.
	 * 
	 * @return An entry containing sapUserXml attribute. Its value is an XML
	 *         string representing the user attributes. The result will be
	 *         <code>null</code> when no more user entries are available.
	 * @throws ConnectorMethodException
	 *             if the SAP network call fails, or an XSL transform error
	 *             occurs.
	 */
	public synchronized Entry getNextEntry() throws ConnectorMethodException {
		setLastWarningSet(null);
		setLastErrorSet(null);

		Entry result = null;
		try {
			if (nextUserIter.hasNext()) {
				FindMethod method = new XslGetNextMethod(getConfig());
				ExecutionCriteria crita = new ExecutionCriteriaImpl();
				String username = (String) nextUserIter.next();
				crita.setParam(R3UsernameIterator.SAP_USER_NAME_TAG_NAME,
						username);
				try {
					String resultStr = (String) method.execute(crita,
							FindMethod.XML_STRING);
					setLastWarningSet(method.getAbapWarnings());
					setLastErrorSet(method.getAbapErrors());
					logMethodMessages(method);
					if (method.hasAbapErrors()) {
						List errs = method.getAbapErrors();
						for (int i = 0; i < errs.size(); ++i) {
							AbapErrorInfo err = (AbapErrorInfo) errs.get(i);
							if (err.isMissingUserError()) {
								return null;
							}
						}
					}

					result = new Entry();
					result
							.setAttribute(
									UserRegistryConnector.ATTR_NAME_USER_XML,
									resultStr);
					result
							.setAttribute(
									UserRegistryConnector.ATTR_NAME_USER_NAME,
									username);

				} catch (EmptyTransformResultException x) {
					getLog().warn(x.getMessage());
					logMethodMessages(method);
				}
			}
		} catch (ConnectorMethodException x) {
			getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_UR_0026,
							new Object[] { x.getMessage() }));
			throw (ConnectorMethodException) x.fillInStackTrace();
		}

		return result;
	}

	/**
	 * Get the version string. Used by SyncWeave to log version info at AL startup.
	 * 
	 * @return The version info string for this connector.
	 */
	public String getVersion() {
		return UserRegistryConnector.VERSION_INFO;
	}

	private void logMethodMessages(ConnectorMethod method) {
		List warns = method.getAbapWarnings();
		for (int i = 0; i < warns.size(); ++i) {
			getLog().logwarn(warns.get(i).toString());
		}
		List errors = method.getAbapErrors();
		for (int i = 0; i < errors.size(); ++i) {
			getLog().logerror(errors.get(i).toString());
		}
	}
}
