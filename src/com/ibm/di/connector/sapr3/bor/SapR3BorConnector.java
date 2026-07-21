/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.SearchCriteria;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * The SAP R/3 Business Object Repository Connector. Version
 * %sapborcn.release.version% Build %sapborcn.release.build.version%.
 * </p>
 * <p>
 * The connector enables external applications, using IBM Tivoli Directory Integrator, to access BOR objects
 * in SAP R/3. It supports the following IBM Tivoli Directory Integrator Connector Modes: <b>Add Only,
 * Delete, Update, Iterator, Lookup.</b> The connector supports design time
 * schema query via {@link #querySchema}.
 * </p>
 * <p>
 * <b>A note about ABAP errors and warnings</b><br>
 * The connector invokes BAPI/RFC functions in SAP to perform the connector mode
 * operations. In some cases, data passed to the BAPI/RFC functions from the XML
 * input, may result in ABAP data validation failures. The BAPI/RFC functions
 * return the results of validation checks in the "RETURN" parameter of the RFC.
 * <br>
 * <br>
 * The connector has been desingned to make the RFC return status available to
 * the Assembly Line. The connector does not interpret, or translate, ABAP
 * errors or warnings into thrown exceptions. The connector registers a script
 * bean named "borcAbapErrorCache". The bean is registered for all connector
 * modes and can be accessed in connector hooks. The bean is an instance of
 * {@link AbapErrorCache}. Script code in a connector hook can use this
 * information to perform contingency actions as required. The cache is reset
 * before the execution of each connector method. Example script code is shown
 * below:<br>
 * <br>
 * <code>
 * var errs = borcAbapErrorCache.getLastErrorSet();<br>
 * if (errs.size() &gt 0) {<br>
 * &nbsp    task.logmsg("********** There were ABAP Errors *********");<br>
 * &nbsp    for (var i = 0; i &lt errs.size(); ++i) {<br>
 * &nbsp&nbsp        var errInfo = errs.get(i);<br>
 * &nbsp&nbsp        task.logmsg("The message is: " + errInfo.getMsg());<br>
 * &nbsp&nbsp        task.logmsg("The message number is: " + errInfo.getMsgNum().toString());<br>
 * &nbsp    }<br>
 * }<br>
 * <br>
 * var warns = borcAbapErrorCache.getLastWarningSet();<br>
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
 * A given SAP entry is represented as XML by the connector, and is set as the
 * value of the connector attribute named "sapXml". The value of this attribute
 * is always an XML string. Its XSchema is defined in the user guide for the
 * connector. The format of the XML may be altered by modifying the XSL style
 * sheets that are configured for the connector.
 * </p>
 * <p>
 * The configuration parameters of the connector are described below. The
 * runtime parameters appear within braces ():<br>
 * <br>
 * <b>R3 Client (client)</b><br>
 * SAP R/3 Logon client for R/3 connection (for example, 100). This is passed
 * directly to the IBM Tivoli Directory Integrator SAP R/3 RFC Function Component.
 * <br>
 * <br>
 * <b>R3 User (user)</b><br>
 * SAP R/3 Logon user for R/3 connection. This is passed directly to the Tivoli
 * Directory Integrator SAP R/3 RFC Function Component. <br>
 * <br>
 * <b>Password (passwd)</b><br>
 * SAP R/3 Logon password for R/3 connection. This is passed directly to the
 * IBM Tivoli Directory Integrator SAP R/3 RFC Function Component. <br>
 * <br>
 * <b>R3 System Number (sysnr)</b><br>
 * The SAP R/3 system number for R/3 connection (for example, 100). This is
 * passed directly to the IBM Tivoli Directory Integrator SAP R/3 RFC Function
 * Component. <br>
 * <br>
 * <b>R3 Hostname (ashost)</b><br>
 * SAP R/3 application server name for R/3 connection. This is passed directly
 * to the IBM Tivoli Directory Integrator SAP R/3 RFC Function Component. <br>
 * <br>
 * <b>Gateway host (gwhost)</b><br>
 * Gateway host name for R/3 connection. This is passed directly to the Tivoli
 * Directory Integrator SAP R/3 RFC Function Component. <br>
 * <br>
 * <b>RFC Trace (trace)</b><br>
 * Set to one (1) to enable RFC API tracing. If enabled, the SAP RFC API will
 * produce separate rfc_nnnn.trc files in the working directory of Tivoli
 * Directory Integrator. This option may be useful to help diagnose RFC
 * invocation problems. It logs the activity and data between the Connector and
 * SAP R/3. This should be set to zero (0) for production deployment. <br>
 * <br>
 * <b>BOR Class Name (sapr3.conn.borObjName)</b><br>
 * The name of the BOR class that this connector will be integrating. The names
 * of BOR classes are available using transaction BAPI in SAP R/3. This value is
 * used to obtain the keyfield names of the BOR object when a schema query is
 * performed. <br>
 * <br>
 * <b>RFC Function Component Name (sapr3.conn.rfcFC)</b><br>
 * The name of the RFC Function Component registered with IBM Tivoli Directory
 * Integrator. This option should be changed only on the advice of IBM support.
 * The default value is: ibmdi.SapR3RfcFC <br>
 * <br>
 * <b>Add Mode StyleSheets (sapr3.conn.putStylesheets)</b><br>
 * The list of XSLT style sheets files to be executed by the Connector when
 * deployed in Add Only mode. Each XSLT file must be separated by a new line
 * within the text box. At runtime, each style sheet is applied to the XML
 * contained within the Container Entry. The XSL will be applied to the value of
 * the attribute named sapXml. This configuration parameter should be changed
 * only at the direction of IBM support. <br>
 * <br>
 * <b>Modify Mode StyleSheets (sapr3.conn.modifyStylesheets)</b><br>
 * The list of XSLT style sheets files to be executed by the Connector when
 * deployed in Modify mode. Each XSLT file must be separated by a new line
 * within the text box. At runtime, each style sheet is applied to the XML
 * contained within the Container Entry. The XSL will be applied to the value of
 * the attribute named sapXml. This configuration parameter should be changed
 * only at the direction of IBM support. <br>
 * <br>
 * <b>Delete Mode StyleSheets (sapr3.conn.deleteStylesheets)</b><br>
 * The list of XSLT style sheets files to be executed by the Connector when
 * deployed in Delete mode. Each XSLT file must be separated by a new line
 * within the text box. At runtime, each style sheet is applied to the XML
 * contained within the Container Entry. The XSL will be applied to the value of
 * the attribute named sapXml. This configuration parameter should be changed
 * only at the direction of IBM support. <br>
 * <br>
 * <b>Lookup Mode Pre StyleSheet (sapr3.conn.findPreStylesheet)</b><br>
 * The XSLT style sheet file to be executed by the Connector when creating an
 * RFC XML request able to obtain all user attributes for a given user. This
 * configuration value must be set when the Connector is deployed in Update,
 * Delete, and Lookup modes. This configuration parameter should be changed only
 * at the direction of IBM support. <br>
 * <br>
 * <b>Lookup Mode Post StyleSheet (sapr3.conn.findPostStylesheet)</b><br>
 * The XSLT style sheet file to be executed by the Connector when creating the
 * user XML formatted response from the Connector. This configuration value must
 * be set when the Connector is deployed in Update, Delete, and Lookup modes.
 * The XSLT transforms the response XML from the RFC executed as a result of the
 * XSLT from Lookup Mode Pre StyleSheet configuration. This configuration
 * parameter should be changed only at the direction of IBM support. <br>
 * <br>
 * <b>Select Entries Pre StyleSheet (sapr3.conn.selectEntriesPreStylesheet)</b><br>
 * The XSLT style sheet file to be executed by the Connector when creating an
 * RFC XML request able to obtain all user names from SAP. This configuration
 * value must be set when the Connector is deployed in Iterator mode. This
 * configuration parameter should be changed only at the direction of IBM
 * support. <br>
 * <br>
 * <b>Select Entries Post StyleSheet (sapr3.conn.selectEntriesPostStylesheet)</b><br>
 * The XSLT style sheet file to be executed by the Connector when creating the
 * user XML for the getNextEntry() processing. This configuration value must be
 * set when the Connector is deployed in Iterator mode. The XSLT transforms the
 * response XML from the RFC executed as a result of the XSLT from Select
 * Entries Pre StyleSheet configuration. This configuration parameter should be
 * changed only at the direction of IBM support. <br>
 * <br>
 * <b>Iterator Mode Pre StyleSheet (sapr3.conn.getNextPreStylesheet)</b><br>
 * The XSLT style sheet file to be executed by the Connector when creating an
 * RFC XML request able to obtain all user attributes for a given user. This
 * configuration value must be set when the Connector is deployed in Iterator
 * mode. This configuration parameter should be changed only at the direction of
 * IBM support. <br>
 * <br>
 * <b>Iterator Mode Post StyleSheet (sapr3.conn.getNextPostStylesheet)</b><br>
 * The XSLT style sheet file to be executed by the Connector when creating the
 * user XML formatted response from the Connector. This configuration value must
 * be set when the Connector is deployed in Iterator mode. The XSLT transforms
 * the response XML from the RFC executed as a result of the XSLT from Iterator
 * Mode Pre StyleSheet configuration. This configuration parameter should be
 * changed only at the direction of IBM support.
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
public final class SapR3BorConnector extends Connector implements
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
	private static final String COMPONENT_NAME = "SAP ABAP Application Server Business Object Repository";

	/*
	 * Script bean names
	 */
	private static final String ERROR_CACHE_BEAN_NAME = "borcAbapErrorCache";

	private static final String DUMMY_XML = "<dummy />";

	/*
	 * Supported Entry attribute schema names.
	 */
	private static final String ATTR_NAME_SAP_XML = "sapXml";

	private static final String ATTR_SYNTAX_SAP_XML = "java.lang.String";

	private static final String ATTR_LENGTH_SAP_XML = "*";

	private static final String ATTR_SCHEMA_NAME = "name";

	private static final String ATTR_SCHEMA_SYNTAX = "syntax";

	private static final String ATTR_SCHEMA_LENGTH = "length";

	private Iterator nextInstanceIter;

	private Configuration config;

	private List lastWarningSet;

	private List lastErrorSet;

	/**
	* Required tags for basic SAPXML representation -- used to construct XML from link criteria if 'skip lookup' is enabled for delete
	*/
	private static final String SAPDATA_OPEN_TAG = "<sapPersonalData><sapBorObjIdentifier>";
	private static final String EMPNO_OPEN_TAG = "<EmployeeNumber>";
	private static final String SUBTYPE_OPEN_TAG = "<SubType>";
	private static final String VAL_END_OPEN_TAG = "<ValidityEnd>";
	private static final String VAL_BEG_OPEN_TAG = "<ValidityBegin>";
	private static final String EMPNO_CLOSE_TAG = "</EmployeeNumber>";
	private static final String SUBTYPE_CLOSE_TAG = "</SubType>";
	private static final String VAL_END_CLOSE_TAG = "</ValidityEnd>";
	private static final String VAL_BEG_CLOSE_TAG = "</ValidityBegin>";
	private static final String SAPDATA_CLOSE_TAG = "</sapBorObjIdentifier></sapPersonalData>";

	/**
	 * Construct the Connector.
	 */
	public SapR3BorConnector() {
		setName(SapR3BorConnector.COMPONENT_NAME);
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

	/**
	 * Allows the caller to obtain a list of ABAP warnings that might have
	 * occured during the execution of any supported Connecotor method.
	 * 
	 * @return a list of AbapErrorInfo objects at warning severity. Minimum
	 *         length will be zero.
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
	 * @return a array of error severity errors. Minimum length will be zero.
	 */
	public List getLastErrorSet() {
		return lastErrorSet;
	}

	public void registerScriptBeans(ScriptEngine se) throws Exception {
		try {
			super.registerScriptBeans(se);
		} catch (Exception x) {
			throw new SapR3ConnectorException(x);
		}
		se.declareStaticBean(SapR3BorConnector.ERROR_CACHE_BEAN_NAME,
				(AbapErrorCache) this);
	}

	/**
	 * This method is called once after the connector configuration file has
	 * been provided by the caller.
	 * 
	 * @param o
	 *            IBM Tivoli Directory Integrator config object. Not used.
	 * @throws SapR3ConnectorException
	 *             When an error happens during super class init.
	 * @throws ConfigurationException
	 *             if SAP connection parameters are invalid or XSL files are
	 *             invalid.
	 */
	public void initialize(Object o) throws SapR3ConnectorException,
			ConfigurationException {
		try {
			super.initialize(o);
			setConfig(new ConfigurationImpl(this));
		} catch (Exception x) {
			Object[] args = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0019, args);
			// 
			// IBM Tivoli Directory Integrator's method Connector.initialize declares to
			// throw Exception.
			throw new SapR3ConnectorException(msg, x);
		}

	}

	/**
	 * <p>
	 * Return the IBM Tivoli Directory Integrator Entry schema supported by this connector. The connector
	 * supports one native attribute named "sapXml". sapXml is an XML string
	 * representing the attributes of a BOR object to be operated on.
	 * </p>
	 * <p>
	 * Other attributes reflect the given BOR object keyfield names . A typical
	 * example of extra attributes would be the specification of BOR object key
	 * fields. They are supported to allow the definition of IBM Tivoli Directory Integrator "LinkCriteria"
	 * when the connector is deployed in Lookup, Delete, or Update modes.
	 * 
	 * @param source
	 *            not used.
	 * @throws SapR3ConnectorException
	 *             If an error occurs.
	 * @return A vector containg one Entry for IBM Tivoli Directory Integrator schema display.
	 */
	public Object querySchema(Object source) throws SapR3ConnectorException {
		List result = new Vector();
		try {
			Entry e = new Entry();
			e.addAttributeValue(SapR3BorConnector.ATTR_SCHEMA_NAME,
					SapR3BorConnector.ATTR_NAME_SAP_XML);
			e.addAttributeValue(SapR3BorConnector.ATTR_SCHEMA_SYNTAX,
					SapR3BorConnector.ATTR_SYNTAX_SAP_XML);
			e.addAttributeValue(SapR3BorConnector.ATTR_SCHEMA_LENGTH,
					SapR3BorConnector.ATTR_LENGTH_SAP_XML);
			result.add(e);
			BorSchemaQuery query = new BorSchemaQuery(getConfig());
			BorSchemaQuery.KeyFieldInfo[] keys = query.getKeyFields();
			for (int i = 0; i < keys.length; ++i) {
				e = new Entry();
				e.addAttributeValue(SapR3BorConnector.ATTR_SCHEMA_NAME, keys[i]
						.getBorName());
				e.addAttributeValue(SapR3BorConnector.ATTR_SCHEMA_SYNTAX,
						SapR3BorConnector.ATTR_SYNTAX_SAP_XML);
				e.addAttributeValue(SapR3BorConnector.ATTR_SCHEMA_LENGTH,
						SapR3BorConnector.ATTR_LENGTH_SAP_XML);
				result.add(e);
			}
		} catch (Exception x) {
			Object[] args = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0020, args);
			getConfig().getLog().logwarn(msg);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			x.printStackTrace(pw);
			config.getLog().warn(sw.toString());
		}
		return result;
	}

	/**
	 * Called by IBM Tivoli Directory Integrator AL to add a new entry and associated attributes to SAP R/3.
	 * 
	 * @param entry
	 *            The AL connector entry input. This connector must have an
	 *            attribute named "sapXml". Its value must be an XML string
	 *            conforming to the SAP XML Schema. The XML is transformed by
	 *            the XSL style sheets into mulitple RFC requests that
	 *            altimately create the entry in SAP R/3.<br>
	 *            See user guide for more details.
	 * 
	 * @throws SapR3ConnectorException
	 *             If the attribute is missing from the entry.
	 * @throws ConnectorMethodException
	 *             if the SAP network call fails, or an XSL transform error
	 *             occurs.
	 * @see Connector#putEntry
	 * 
	 */
	public void putEntry(Entry entry) throws ConnectorMethodException,
			SapR3ConnectorException {
		setLastWarningSet(null);
		setLastErrorSet(null);
		try {
			String sapXmlStr = (String) entry
					.getObject(SapR3BorConnector.ATTR_NAME_SAP_XML);
			if (sapXmlStr == null || sapXmlStr.length() == 0) {
				Object[] args = new Object[] { SapR3BorConnector.ATTR_NAME_SAP_XML };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0012, args);
				throw new SapR3ConnectorException(msg);
			}

			WriteMethod method = new XslAddMethod(getConfig());
			ExecutionCriteria crita = new ExecutionCriteriaImpl();
			method.execute(sapXmlStr, crita);
			if (method.hasAbapErrors() || method.hasAbapWarnings()) {
				setLastWarningSet(method.getAbapWarnings());
				setLastErrorSet(method.getAbapErrors());
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0016);
				getConfig().getLog().logwarn(msg);
				logMethodMessages(method);
			}
		} catch (ConnectorMethodException x) {
			Object[] args = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0021, args);
			getConfig().getLog().logwarn(msg);
			throw (ConnectorMethodException) x.fillInStackTrace();
		} catch (SapR3ConnectorException x) {
			Object[] args = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0021, args);
			getConfig().getLog().logwarn(msg);
			throw (SapR3ConnectorException) x.fillInStackTrace();
		}
	}

	/**
	 * Called by IBM Tivoli Directory Integrator AL to remove an existing user and associated attributes
	 * from SAP R/3
	 * 
	 * @param entry
	 *            The AL connector entry input. This connector must have an
	 *            attribute named "sapXml". Its value must be an XML string
	 *            conforming to the SAP XML Schema supported by the configured
	 *            XSL style sheets.<br>
	 *            See user guide for more details.
	 * 
	 * @param search
	 *            Passed through to XSL style sheet as an XSL param. Typically,
	 *            the search criteria include the keyfields of the given BOR
	 *            object to be deleted.
	 * 
	 * @throws SapR3ConnectorException
	 *             If the attribute is missing from the entry.
	 * @throws ConnectorMethodException
	 *             if the SAP network call fails, or an XSL transform error
	 *             occurs.
	 * @see Connector#deleteEntry
	 * 
	 */
	public void deleteEntry(Entry entry, SearchCriteria search)
			throws ConnectorMethodException, SapR3ConnectorException {
		setLastWarningSet(null);
		setLastErrorSet(null);
		try {
			String sapXmlStr = null;
			if (entry != null)
			{
				sapXmlStr = (String) entry
					.getObject(SapR3BorConnector.ATTR_NAME_SAP_XML);
			}
			if ((sapXmlStr == null || sapXmlStr.length() == 0)
					&& search.size() == 0) {
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0028);
				throw new SapR3ConnectorException(msg);
			}
			if (sapXmlStr == null || sapXmlStr.length() == 0) {
				//Need to use the link criteria to create the basic sapXML (no lookup performed)
				sapXmlStr = getBasicSAPXML(search);
				if (sapXmlStr == null)
				{
					String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0028);
							throw new SapR3ConnectorException(msg);
				}
			}

			WriteMethod method = new XslDeleteMethod(getConfig());
			ExecutionCriteria crita = new ExecutionCriteriaImpl(search);
			method.execute(sapXmlStr, crita);
			if (method.hasAbapErrors() || method.hasAbapWarnings()) {
				setLastWarningSet(method.getAbapWarnings());
				setLastErrorSet(method.getAbapErrors());
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0017);
				getConfig().getLog().logwarn(msg);
				logMethodMessages(method);
			}
		} catch (ConnectorMethodException x) {
			Object[] args = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0022, args);
			getConfig().getLog().logwarn(msg);
			throw (ConnectorMethodException) x.fillInStackTrace();
		} catch (SapR3ConnectorException x) {
			Object[] args = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0022, args);
			getConfig().getLog().logwarn(msg);
			throw (SapR3ConnectorException) x.fillInStackTrace();
		}
	}

	/**
	 * Called by the deleteentry method if the LinkCriteria need to be used to 
	 * construct the sapXml representing the user to be deleted.  That is,  
	 * if skip lookup was selected.
	 *
	 * @param search
	 *            Defined in the "LinkCriteria" tab of the AL. Must have a
	 *            criteria names matching the key field names of the BOR object
	 *            as required by the given BOR read methods.
	 * 
	 * @return The basic "sapXml" string representing the BOR object to be deleted.
	 * 
	 */
	private String getBasicSAPXML(SearchCriteria search) {
		String empNo = "";
		String valBegin = "";
		String valEnd = "";
		String subType = "";
		String basicSapXml = "";
		for (int i=0;i<search.size();i++)
		{
			String critName = search.getCriteria(i).name;
			String critVal = (String)search.getCriteria(i).value;
			if (critName.equalsIgnoreCase("EmployeeNumber"))
			{
				empNo = critVal;
			}
			else if (critName.equalsIgnoreCase("ValidityBegin"))
			{
				valBegin = critVal;
			}
			else if (critName.equalsIgnoreCase("ValidityEnd"))
			{
				valEnd = critVal;
			}
			else if (critName.equalsIgnoreCase("SubType"))
			{
				subType = critVal;
			}	
		}
		if (empNo.equals("") || valBegin.equals("") || valEnd.equals(""))
		{
			basicSapXml = null;
		}
		else {
			if (subType.equals(""))
			{
				basicSapXml = SAPDATA_OPEN_TAG + EMPNO_OPEN_TAG + empNo + EMPNO_CLOSE_TAG + VAL_END_OPEN_TAG + valEnd + VAL_END_CLOSE_TAG + VAL_BEG_OPEN_TAG + valBegin + VAL_BEG_CLOSE_TAG + SAPDATA_CLOSE_TAG;
			}
			else {
				basicSapXml = SAPDATA_OPEN_TAG + EMPNO_OPEN_TAG + empNo + EMPNO_CLOSE_TAG + SUBTYPE_OPEN_TAG + subType + SUBTYPE_CLOSE_TAG + VAL_END_OPEN_TAG + valEnd + VAL_END_CLOSE_TAG + VAL_BEG_OPEN_TAG + valBegin + VAL_BEG_CLOSE_TAG + SAPDATA_CLOSE_TAG;
			}
		}
		return basicSapXml;
	}

	/**
	 * Called by IBM Tivoli Directory Integrator AL to update an existing user and associated attributes in
	 * SAP R/3
	 * 
	 * @param entry
	 *            The AL connector entry input. This connector must have an
	 *            attribute named "sapXml". Its value must be an XML string
	 *            conforming to the SAP User XML Schema. supported by the
	 *            configured XSL style sheets.<br>
	 * 
	 * See user guide for more details.
	 * 
	 * @param search
	 *            Passed through to XSL style sheet as an XSL param. Typically,
	 *            the search criteria include the keyfields of the given BOR
	 *            object to be modified.
	 * 
	 * @throws SapR3ConnectorException
	 *             If the attribute is missing from the entry.
	 * @throws ConnectorMethodException
	 *             if the SAP network call fails, or an XSL transform error
	 *             occurs.
	 * @see Connector#modEntry
	 * 
	 */
	public void modEntry(Entry entry, SearchCriteria search)
			throws ConnectorMethodException, SapR3ConnectorException {
		setLastWarningSet(null);
		setLastErrorSet(null);
		try {
			String sapXmlStr = (String) entry
					.getObject(SapR3BorConnector.ATTR_NAME_SAP_XML);
			if (sapXmlStr == null || sapXmlStr.length() == 0) {
				Object[] args = new Object[] { SapR3BorConnector.ATTR_NAME_SAP_XML };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0012, args);
				throw new SapR3ConnectorException(msg);
			}

			WriteMethod method = new XslModifyMethod(getConfig());
			ExecutionCriteria crita = new ExecutionCriteriaImpl(search);
			method.execute(sapXmlStr, crita);
			if (method.hasAbapErrors() || method.hasAbapWarnings()) {
				setLastWarningSet(method.getAbapWarnings());
				setLastErrorSet(method.getAbapErrors());
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0018);
				getConfig().getLog().logwarn(msg);
				logMethodMessages(method);
			}
		} catch (ConnectorMethodException x) {
			Object[] args = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0023, args);
			getConfig().getLog().logwarn(msg);
			throw (ConnectorMethodException) x.fillInStackTrace();
		} catch (SapR3ConnectorException x) {
			Object[] args = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0023, args);
			getConfig().getLog().logwarn(msg);
			throw (SapR3ConnectorException) x.fillInStackTrace();
		}
	}

	/**
	 * Called by IBM Tivoli Directory Integrator AL to find an existing BOR object instance and associated
	 * attributes in SAP R/3.
	 * 
	 * @param search
	 *            Defined in the "LinkCriteria" tab of the AL. Must have a
	 *            criteria names matching the key field names of the BOR object
	 *            as required by the given BOR read methods.
	 * 
	 * @return The entry populated with attribute "sapXml", or <code>null</code>
	 *         if the BOR object instance could not be found.
	 * 
	 * @throws SapR3ConnectorException
	 *             If the attribute is missing from the entry.
	 * @throws ConnectorMethodException
	 *             if the SAP network call fails, or an XSL transform error
	 *             occurs.
	 * @see Connector#findEntry
	 * 
	 */
	public Entry findEntry(SearchCriteria search)
			throws ConnectorMethodException, SapR3ConnectorException {
		setLastWarningSet(null);
		setLastErrorSet(null);
		try {
			if (search.size() == 0) {
				getConfig().getLog().loginfo(
						LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_BOR_0013));
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

				Entry result = new Entry();
				result.setAttribute(SapR3BorConnector.ATTR_NAME_SAP_XML,
						resultStr);

				addKeyfields(result, resultStr);

				getConfig().getLog().debug(
						LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_BOR_0037));
				return result;

			} catch (EmptyTransformResultException x) {
				getConfig().getLog().warn(x.getMessage());
				logMethodMessages(method);
			}
		} catch (ConnectorMethodException x) {
			getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_BOR_0024,
							new Object[] { x.getMessage() }));
			throw (ConnectorMethodException) x.fillInStackTrace();
		}

		getConfig().getLog().debug(
				LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0038));
		return null;
	}

	/**
	 * Called by IBM Tivoli Directory Integrator AL when the connector operates in Iterator mode inside an
	 * assembly line. It finds all BOR object identifiers currently managed by
	 * the connected SAP R/3 instance. It stores all instance ID names and
	 * values in a cached
	 * {@link com.ibm.di.connector.sapr3.bor.BorInstanceIdIterator} following an
	 * RFC lookup to obtain the names. <br>
	 * <br>
	 * The XML value returned from the post XSL, must have an elements named
	 * "sapBorObjIdentifier". The children of this element must be elements
	 * where the tagnames match the keyfields of the given BOR object.
	 * 
	 * @throws SapR3ConnectorException
	 *             If the attribute is missing from the entry.
	 * @throws ConnectorMethodException
	 *             if the SAP network call fails, or an XSL transform error
	 *             occurs.
	 * @see Connector#selectEntries
	 */
	public synchronized void selectEntries() throws ConnectorMethodException,
			SapR3ConnectorException {
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
			nextInstanceIter = new BorInstanceIdIterator(doc);
		} catch (ConnectorMethodException x) {
			Object[] args = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0025, args);
			getConfig().getLog().logwarn(msg);
			throw (ConnectorMethodException) x.fillInStackTrace();
		} catch (SapR3ConnectorException x) {
			Object[] args = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0025, args);
			getConfig().getLog().logwarn(msg);
			throw (SapR3ConnectorException) x.fillInStackTrace();
		}
	}

	/**
	 * This method is called to retrieve the next entry from the connector. When
	 * there are no more entries to retrieve the function should return a null
	 * value indicating a logical end of file. This method uses the cached
	 * Iterator from {@link #selectEntries}.
	 * 
	 * @return An entry containing sapXml attribute. Its value is an XML string
	 *         representing the BOR object instance attributes. The result will
	 *         be <code>null</code> when no more instances are available.
	 * @throws ConnectorMethodException
	 *             if the SAP network call fails, or an XSL transform error
	 *             occurs.
	 */
	public synchronized Entry getNextEntry() throws ConnectorMethodException {
		setLastWarningSet(null);
		setLastErrorSet(null);

		Entry result = null;
		try {
			if (nextInstanceIter.hasNext()) {
				FindMethod method = new XslGetNextMethod(getConfig());
				ExecutionCriteria crita = new ExecutionCriteriaImpl();
				BorInstanceId borId = (BorInstanceId) nextInstanceIter.next();
				Set keys = borId.keySet();
				Iterator keyIter = keys.iterator();
				while (keyIter.hasNext()) {
					String name = (String) keyIter.next();
					crita.setParam(name, borId.getValue(name));
				}
				try {
					String resultStr = (String) method.execute(crita,
							FindMethod.XML_STRING);
					setLastWarningSet(method.getAbapWarnings());
					setLastErrorSet(method.getAbapErrors());
					logMethodMessages(method);

					result = new Entry();
					result.setAttribute(SapR3BorConnector.ATTR_NAME_SAP_XML,
							resultStr);
					keyIter = keys.iterator();
					while (keyIter.hasNext()) {
						String name = (String) keyIter.next();
						result.setAttribute(name, borId.getValue(name));
					}
				} catch (EmptyTransformResultException x) {
					getConfig().getLog().warn(x.getMessage());
					logMethodMessages(method);
				}
			}
		} catch (ConnectorMethodException x) {
			Object[] args = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0026, args);
			getConfig().getLog().logwarn(msg);
			throw (ConnectorMethodException) x.fillInStackTrace();
		}

		return result;
	}

	/**
	 * Get the version string. Used by IBM Tivoli Directory Integrator to log version info at AL startup.
	 * 
	 * @return The version info string for this connector.
	 */
	public String getVersion() {
		return SapR3BorConnector.VERSION_INFO;
	}

	private void logMethodMessages(ConnectorMethod method) {
		List warns = method.getAbapWarnings();
		for (int i = 0; i < warns.size(); ++i) {
			getConfig().getLog().logwarn(warns.get(i).toString());
		}
		List errors = method.getAbapErrors();
		for (int i = 0; i < errors.size(); ++i) {
			getConfig().getLog().logerror(errors.get(i).toString());
		}
	}

	private void addKeyfields(Entry e, String xmlStr) {
		try {
			Document doc = XmlHelper.parse(xmlStr);
			Element root = doc.getDocumentElement();
			NodeList nl = root
					.getElementsByTagName(BorInstanceIdIterator.SAP_BOR_ID_TAG_NAME);
			if (nl.getLength() > 0) {
				Element borKeysEle = (Element) nl.item(0);
				nl = borKeysEle.getElementsByTagName("*");
				for (int i = 0; i < nl.getLength(); ++i) {
					Element keyEle = (Element) nl.item(i);
					String name = keyEle.getTagName();
					String val = XmlHelper.textValue(keyEle);
					e.setAttribute(name, val);
				}
			}
		} catch (ParserConfigurationException x) {
				getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_BOR_0024,
							new Object[] { x.getMessage() }));
		} catch (SAXException x) {
				getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_BOR_0024,
							new Object[] { x.getMessage() }));
		} catch (IOException x) {
				getConfig().getLog().logwarn(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_BOR_0024,
							new Object[] { x.getMessage() }));
		}

	}
}
