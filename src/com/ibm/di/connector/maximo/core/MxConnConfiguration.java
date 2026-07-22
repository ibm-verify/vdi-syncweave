/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.di.connector.maximo.TpaeIFConnector;
import com.ibm.di.connector.maximo.exception.MxConnConfigException;
import com.ibm.di.connector.maximo.exception.MxConnTimeoutException;
import com.ibm.di.connector.maximo.exception.MxConnectorException;
import com.ibm.di.connector.maximo.parsing.Schema;
import com.ibm.di.connector.maximo.parsing.SchemaConfiguration;
import com.ibm.di.connector.maximo.util.AbstractConfigurationParameters;
import com.ibm.di.server.Log;
import com.ibm.di.util.StringUtils;

/**
 * Collection of <b>configuration parameters</b> used by the TPAE IF Connector.
 * 
 * @since 7.1
 * @see SimpleTpaeIFConnector
 * @see #loadParameters()
 */
public final class MxConnConfiguration extends AbstractConfigurationParameters {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Default value for the <b>MAXDOMAIN enterprise service</b> parameter.
	 * 
	 * @see #PARAM_DOMAIN_ES_QUERY
	 */
	public static final String DEFAULT_DOMAIN_ES_QUERY = "MXMaxDomainQuery";

	/**
	 * Default value for the <b>MAXDOMAIN object structure</b> parameter.
	 * 
	 * @see #PARAM_DOMAIN_OS
	 */
	public static final String DEFAULT_DOMAIN_OS = "MXDOMAIN";

	/**
	 * Default value for <b>MAXOBJECT object structure</b> parameter.
	 * 
	 * @see #PARAM_MAXOBJECT_OS
	 */
	public static final String DEFAULT_MAXOBJECT_OS = "MXOBJECTCFG";

	/**
	 * Default value for <b>MAXOBJECT enterprise service</b> parameter.
	 * 
	 * @see #PARAM_MAXOBJECT_ES_QUERY
	 */
	public static final String DEFAULT_MAXOJBECT_ES_QUERY = "MXMaxObjectQuery";

	/**
	 * Default value for <b>base URL</b> parameter.
	 * 
	 * @see #PARAM_MX_BASE_URL
	 */
	public static final String DEFAULT_MX_BASE_URL = "http://localhost";

	/**
	 * Default value for <b>Service Base</b> parameter.
	 * 
	 * @see #PARAM_MX_SERVICE_BASE
	 */
	public static final String DEFAULT_MX_SERVICE_BASE = "meaweb";

	/**
	 * Default value for <b>Maximo version</b> parameter.
	 * 
	 * @see #PARAM_MX_VERSION
	 */
	public static final String DEFAULT_MX_VERSION = "7 1 Harrier 072 7100-001";

	/**
	 * Default value for <b>page size</b> parameter.
	 * 
	 * @see #PARAM_PAGE_SIZE
	 */
	public static final int DEFAULT_PAGE_SIZE = 100;

	/**
	 * Default value for <b>timeout</b> parameter.
	 * 
	 * @see #PARAM_TIMEOUT
	 */
	public static final int DEFAULT_TIMEOUT = 0;

	/**
	 * Default value for <b>translation language</b> parameter.
	 * 
	 * @see #PARAM_TRANS_LANGUAGE
	 */
	public static final String DEFAULT_TRANS_LANGUAGE = "EN";

	/**
	 * The <b>Authentication Required</b> parameter. It indicates if the Maximo
	 * server requires basic user authentication. If this parameter is true,
	 * every HTTP request made by the connector will contain the user's
	 * credentials. The default value is false.
	 * 
	 * @see #PARAM_USER_ID
	 * @see #PARAM_PASSWORD
	 * @see #isAuthenticationRequired()
	 */
	public static final String PARAM_AUTHENTICATION_REQUIRED = "authenticationRequired";

	/**
	 * The <b>domain enterprise service query</b> parameter.
	 * 
	 * @see #DEFAULT_DOMAIN_ES_QUERY
	 * @see #getUrlListForQueryMaxDomain()
	 */
	public static final String PARAM_DOMAIN_ES_QUERY = "domainEnterpriseService";

	/**
	 * The <b>domain object structure</b> parameter.
	 * 
	 * @see #DEFAULT_DOMAIN_OS
	 * @see #getDomainObjectStructure()
	 */
	public static final String PARAM_DOMAIN_OS = "domainObjectStructure";

	/**
	 * The <b>error on excedent size</b> parameter. It indicates if an error
	 * should be thrown when a text field exceeds the maximum size. If this is
	 * not checked, the text will be truncated. The default value is true.
	 * 
	 * @see #isErrorOnExcedentSizeEnabled()
	 */
	public static final String PARAM_ERROR_ON_EXCEDENT_SIZE = "errorOnExcedentSize";

	/**
	 * The <b>CREATE enterprise service</b> parameter. The name of the
	 * enterprise service that performs <b>create</b> operations on the
	 * specified object structure.
	 * <p>
	 * This parameter is required for AddOnly and Update modes.
	 * </p>
	 * 
	 * @see #getUrlListForCreate()
	 */
	public static final String PARAM_ES_CREATE = "enterpriseServiceCreate";

	/**
	 * The <b>DELETE enterprise service</b> parameter. The name of the
	 * enterprise service that performs <b>delete</b> operations on the
	 * specified object structure.
	 * <p>
	 * This parameter is required for Delete mode.
	 * </p>
	 * 
	 * @see #getUrlListForDelete()
	 */
	public static final String PARAM_ES_DELETE = "enterpriseServiceDelete";

	/**
	 * The <b>QUERY enterprise service</b> parameter. The name of the enterprise
	 * service that performs <b>query</b> operations on the specified object
	 * structure.
	 * <p>
	 * This parameter is required for Iterator, Lookup, Update and Delete modes.
	 * </p>
	 * 
	 * @see #getUrlListForQuery()
	 */
	public static final String PARAM_ES_QUERY = "enterpriseServiceQuery";

	/**
	 * The <b>UPDATE enterprise service</b> parameter. The name of the
	 * enterprise service that performs <b>update</b> operations on the
	 * specified object structure.
	 * <p>
	 * This parameter is required for Update modes.
	 * </p>
	 * 
	 * @see #getUrlListForUpdate()
	 */
	public static final String PARAM_ES_UPDATE = "enterpriseServiceUpdate";

	/**
	 * The <b>SYNC enterprise service</b> parameter. The name of the enterprise
	 * service that performs <b>synchronize</b> operations on the specified
	 * object structure.
	 * <p>
	 * This parameter is used by the {@link TpaeIFConnector} in AddOnly, Update
	 * and Delete modes.
	 * </p>
	 * 
	 * @see #getUrlListForSync()
	 */
	public static final String PARAM_ES_SYNC = "enterpriseServiceSync";

	/**
	 * The <b>external system</b> parameter. The name of the external system
	 * that groups all the enterprise services used by the connector.
	 * <p>
	 * This parameter is required for Iterator, Lookup, AddOnly, Delete and
	 * Update modes.
	 * </p>
	 */
	public static final String PARAM_EXTSYS = "externalSystem";

	/**
	 * The <b>MAXOBJECT enterprise service</b> parameter. The name of the
	 * enterprise service that performs query operations on the MAXOBJECT object
	 * structure.
	 * <p>
	 * This parameter is required for Iterator, Lookup, AddOnly, Delete and
	 * Update modes.
	 * </p>
	 * 
	 * @see #DEFAULT_MAXOJBECT_ES_QUERY
	 * @see #getUrlListForQueryMaxObject()
	 */
	public static final String PARAM_MAXOBJECT_ES_QUERY = "maxobjEnterpriseService";

	/**
	 * The <b>MAXOBJECT object structure</b> parameter. The name of the object
	 * structure that exposes the MAXOBJECT and MAXATTRIBUTE MBOs. This object
	 * structure is used to obtain complementary MBO's metadata.
	 * <p>
	 * This parameter is required for Iterator, Lookup, AddOnly, Delete and
	 * Update modes.
	 * </p>
	 * 
	 * @see #DEFAULT_MAXOBJECT_OS
	 * @see #getMaxobjectObjectStructure()
	 */
	public static final String PARAM_MAXOBJECT_OS = "maxobjObjectStructure";

	/**
	 * The <b>MBO</b> parameter. Defines the name of the MBO the connector will
	 * work with. Since an object structure might be composed by several MBOs,
	 * the connector must know which MBO inside the object structure it will
	 * work with. Needless to say, the MBO should be part of the specified
	 * object structure.
	 * <p>
	 * For example, the predefined object structure MXASSET is composed by the
	 * MBOs ASSET, ASSETMETER, ASSETUSERCUST, and ASSETSPEC. Therefore, the
	 * connector must be told which MBO it will work with.
	 * </p>
	 * <p>
	 * This parameter must comply with the following syntax:
	 * </p>
	 * <code>
     * &lt;Top-Level MBO&gt;[@&lt;Child MBO Level 1&gt;[@&lt;Child MBO Level 2&gt;[@&lt;Child MBO Level N&gt;]]]
     * </code>
	 * <p>
	 * Example
	 * </p>
	 * <p>
	 * The following example defines ASSET as the selected MBO.
	 * </p>
	 * <code>ASSET
	 * </code>
	 * <p>
	 * The following example defines ASSETMETER as the selected MBO.
	 * </p>
	 * <code>
	 * ASSET@ASSETMETER
	 * </code>
	 * 
	 * <p>
	 * <b>Note:</b> If no value is defined, the connector will work with the
	 * object structure's top-level MBO.
	 * </p>
	 * <p>
	 * This parameter is required for Iterator, Lookup, AddOnly, Delete, Update
	 * modes.
	 * </p>
	 * <p>
	 * The method {@link SimpleTpaeIFConnector#getMboList()} might be used to
	 * retrieve a list of available MBOs in the specified object structure. The
	 * following parameters should be previously defined, before invoking the
	 * method: {@link #PARAM_MX_BASE_URL Base URL}, {@link #PARAM_EXTSYS
	 * External System}, {@link #PARAM_MAXOBJECT_OS MAXOBJECT Object Structure},
	 * {@link #PARAM_MAXOBJECT_ES_QUERY MAXOBJECT QUERY Enterprise Service},
	 * {@link #PARAM_OBJECT_STRUCTURE Object Structure}
	 * </p>
	 * 
	 * @see #getMbo()
	 */
	public static final String PARAM_MBO = "mbo";

	/**
	 * The <b>Maximo server's base URLs</b> parameter. Multiple URLs must be
	 * separated by new line character (<tt>\n</tt>), carriage-return character
	 * (<tt>'\r'</tt>), space, or comma. The connector will try to send the
	 * requests until one of the URLs successfully respond or the list of URLs
	 * ends.
	 * <p>
	 * Examples
	 * </p>
	 * <p>
	 * The following URLs targets the host <i>mxserver1</i> and <i>mxserver2</i>
	 * on default port.
	 * </p>
	 * <code>
     * http://mxserver1 http://mxserver2
     * </code>
	 * <p>
	 * The following URL targets the host <i>mxserver</i> on default port using
	 * HTTP over SSL.
	 * </p>
	 * <code>
     * https://mxserver
     * </code>
	 * <p>
	 * The following URL targets the host <i>localhost</i> on port <i>8080</i>.
	 * </p>
	 * <code>
     * http://localhost:8080
     * </code>
	 * <p>
	 * This parameter is required for Iterator, Lookup, AddOnly, Delete and
	 * Update modes.
	 * </p>
	 * 
	 * @see #DEFAULT_MX_BASE_URL
	 * @see #getUrlList()
	 */
	public static final String PARAM_MX_BASE_URL = "maximoBaseURL";

	public static final String PARAM_MX_SERVICE_BASE = "maximoServiceBase";
	
	/**
	 * The <b>Maximo version</b> parameter. It indicates the Maximo's version
	 * that each message exchanged with the server should contain.
	 * 
	 * @see #DEFAULT_MX_VERSION
	 * @see #getMaximoVersion()
	 */
	public static final String PARAM_MX_VERSION = "maximoVersion";

	/**
	 * The <b>object structure</b> parameter. The name of the object structure
	 * the connector will work with. The object structure could be composed by a
	 * single MBO or a group of related MBOs.
	 * <p>
	 * This parameter is required for Iterator, Lookup, AddOnly, Delete and
	 * Update modes.
	 * </p>
	 * 
	 * @see #getObjectStructure()
	 */
	public static final String PARAM_OBJECT_STRUCTURE = "objectStructure";

	/**
	 * The <b>page size</b> parameter. It limits the number of records retrieved
	 * from Maximo, obligating the connector to make several requests in order
	 * to get all the records selected by the query criteria.
	 * <p>
	 * <b>Note:</b> The page size applies only to the top-level MBO in the
	 * object structure.
	 * </p>
	 * <p>
	 * For example, if Maximo has 1000 assets in its database and the page size
	 * is defined as 100, a query against the predefined <i>MXASSET</i> object
	 * structure would be accomplished by 10 requests.
	 * </p>
	 * <p>
	 * This parameter is required for Iterator, Lookup, Update and Delete modes.
	 * </p>
	 * 
	 * @see #DEFAULT_PAGE_SIZE
	 * @see #getPageSize()
	 */
	public static final String PARAM_PAGE_SIZE = "pageSize";

	/**
	 * The <b>password</b> parameter. It is the password the connector will use
	 * to authenticate itself on the Maximo server. Mandatory only if
	 * <i>Authentication Required</i> is checked.
	 * 
	 * @see #PARAM_AUTHENTICATION_REQUIRED
	 * @see #getPassword()
	 */
	public static final String PARAM_PASSWORD = "password";

	/**
	 * The <b>query criteria</b> parameter. It contains the selection criteria
	 * for the Iterator mode. Queries must be specified in XML syntax and can
	 * select records based on single value or a range of values.
	 * <p>
	 * <b>Note:</b> The query criteria can apply only to the top-level MBO in
	 * the object structure.
	 * </p>
	 * <p>
	 * For example, a query against the predefined <i>MXASSET</i> object
	 * structure must select values in the top-level MBO (<i>ASSET</i>). For
	 * this object structure it is not possible build a query that selects
	 * records based on, for example, the average attribute in the
	 * <i>ASSETMETER</i> MBO.
	 * </p>
	 * <b><i>The Operator Attribute</i></b>
	 * <p>
	 * The operator attribute compares the value of a field with one or more
	 * other values. It has the following format:
	 * </p>
	 * <code>operator = &quot;value&quot;</code>
	 * <p>
	 * The operator attribute compares the value of a field with one or more
	 * other values. It has the following format:
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <th>Value</th>
	 * <th>Description</th>
	 * </tr>
	 * <tr>
	 * <td>=</td>
	 * <td>equal</td>
	 * </tr>
	 * <tr>
	 * <td>!=</td>
	 * <td>not equal</td>
	 * </tr>
	 * <tr>
	 * <td>&amp;lt;</td>
	 * <td>less than</td>
	 * </tr>
	 * <tr>
	 * <td>&amp;lt;=</td>
	 * <td>less than or equal</td>
	 * </tr>
	 * <tr>
	 * <td>&amp;gt;</td>
	 * <td>greater than</td>
	 * </tr>
	 * <tr>
	 * <td>&amp;gt;=</td>
	 * <td>greater than or equal</td>
	 * </tr>
	 * <tr>
	 * <td>SW</td>
	 * <td>starts with</td>
	 * </tr>
	 * <tr>
	 * <td>EW</td>
	 * <td>ends with</td>
	 * </tr>
	 * </table>
	 * <p>
	 * Use the less than and the greater than attributes with numeric and date
	 * fields only.
	 * </p>
	 * <p>
	 * Example
	 * </p>
	 * <p>
	 * To find all assets in a type other than IT, format the query as follows:
	 * </p>
	 * 
	 * <pre>
	 * &lt;ASSET&gt;
	 *   &lt;ASSETTYPE operator=&quot;!=&quot;&gt;IT&lt;/ASSETTYPE&gt;
	 * &lt;/ASSET&gt;
	 * </pre>
	 * 
	 * <b><i>Field Selection</i></b>
	 * <p>
	 * A field-based query compares the value in a field with a specified value.
	 * The value is not case-sensitive.
	 * </p>
	 * <p>
	 * Example
	 * </p>
	 * <p>
	 * The following query searches for assets where <i>VENDOR</i> is equal to
	 * ATI and <i>STATUS</i> is equal to OPERATING.
	 * </p>
	 * 
	 * <pre>
	 * &lt;ASSET&gt;
	 *   &lt;VENDOR operator=&quot;=&quot;&gt;ATI&lt;/VENDOR&gt;
	 *   &lt;STATUS operator=&quot;=&quot;&gt;OPERATING&lt;/STATUS&gt;
	 * &lt;/ASSET&gt;
	 * </pre>
	 * 
	 * <p>
	 * The following query searches for assets where <i>VENDOR</i> is like to
	 * %ATI% and <i>STATUS</i> is like to %OPER%.
	 * </p>
	 * 
	 * <pre>
	 * &lt;ASSET&gt;
	 *   &lt;VENDOR&gt;ATI&lt;/VENDOR&gt;
	 *   &lt;STATUS&gt;OPERATING&lt;/STATUS&gt;
	 * &lt;/ASSET&gt;
	 * </pre>
	 * 
	 * <p>
	 * <b>Note:</b> The preceding query format treats ATI and OPER as if %
	 * wildcards exist before and after the hard-coded values. It is not
	 * possible restrict the search criteria, that is %ATI or ATI%.
	 * </p>
	 * <p>
	 * The following queries search for assets that do not have a tag. The first
	 * uses the operator attribute; the second one does not.
	 * </p>
	 * 
	 * <pre>
	 * &lt;ASSET&gt;
	 *   &lt;ASSETTAG operator=&quot;NULL&quot;&gt;&lt;/ASSETTAG&gt;
	 * &lt;/ASSET&gt;
	 * 
	 * &lt;ASSET&gt;
	 *   &lt;ASSETTAG&gt;NULL&lt;/ASSETTAG&gt;
	 * &lt;/ASSET&gt;
	 * </pre>
	 * 
	 * <p>
	 * The following query searches for assets with asset number starting with
	 * the text "711".
	 * 
	 * <pre>
	 * &lt;ASSET&gt;
	 *   &lt;ASSETNUM operator=&quot;SW&quot;&gt;711&lt;/ASSETNUM&gt;
	 * &lt;/ASSET&gt;
	 * </pre>
	 * 
	 * <p>
	 * The following query searches for assets with a status NOT READY or
	 * OPERATING, by using the equivalent of an SQL IN clause.
	 * </p>
	 * 
	 * <pre>
	 * &lt;ASSET&gt;
	 *   &lt;STATUS&gt;NOT READY, OPERATING&lt;/STATUS&gt;
	 * &lt;/ASSET&gt;
	 * </pre>
	 * 
	 * <b><i>Range Selection</i></b>
	 * <p>
	 * A query can search for records with a value that falls within a range of
	 * values. The format varies, depending on whether the selection criteria is
	 * open ended or contains an upper and lower range.
	 * </p>
	 * <p>
	 * Example
	 * </p>
	 * <p>
	 * The following query searches for assets where <i>BUDGETCOST</i> is
	 * greater than $1000.
	 * </p>
	 * 
	 * <pre>
	 * &lt;ASSET&gt;
	 *   &lt;BUDGETCOST operator=&quot;&amp;gt;&quot;&gt;1000&lt;/BUDGETCOST&gt;
	 * &lt;/ASSET&gt;
	 * </pre>
	 * 
	 * <p>
	 * The following query searches for assets where <i>BUDGETCOST</i> is
	 * greater than $1000 and less than $20000.
	 * </p>
	 * 
	 * <pre>
	 * &lt;ASSET&gt;
	 *   &lt;BUDGETCOST operator=&quot;&amp;gt;&quot;&gt;1000&lt;/BUDGETCOST&gt;
	 *   &lt;BUDGETCOST operator=&quot;&amp;lt;&quot;&gt;20000&lt;/BUDGETCOST&gt;
	 * &lt;/ASSET&gt;
	 * </pre>
	 * 
	 * <p>
	 * <b>Note:</b> A query can contain a maximum of two references for the same
	 * attribute.
	 * </p>
	 * <p>
	 * This parameter is used by the <a
	 * href="GenericMaximoConnector.html#iterator">Iterator</a> mode.
	 * </p>
	 * 
	 * @see #getQueryCriteria()
	 */
	public static final String PARAM_QUERY_CRITERIA = "queryCriteria";

	/**
	 * This is the <b>timeout</b> parameter, in milliseconds. This is used when
	 * communicating with the MEA server. If the timeout expires before the
	 * connection can be established or before there is data available for read,
	 * a {@link MxConnTimeoutException} is raised. A timeout of zero is
	 * interpreted as infinite timeout.
	 * 
	 * @see #DEFAULT_TIMEOUT
	 * @see #getTimeout()
	 */
	public static final String PARAM_TIMEOUT = "timeout";

	/**
	 * This is the <b>getReplaceOnUpdate</b> flag. It is used only in Update
	 * mode. When enabled entries will be replaced in the Tpae server. This
	 * means that child MBOs that are not present in the entry will deleted from
	 * the target entry.
	 * 
	 * @see #getReplaceOnUpdate()
	 */
	public static final String PARAM_REPLACE_ON_UPDATE = "replaceOnUpdate";

	/**
	 * This is the <b>transaction language</b> parameter. This is the language
	 * in which the content values for multi-language enabled fields are
	 * supplied.
	 * 
	 * @see #DEFAULT_TRANS_LANGUAGE
	 * @see #getTransLanguage()
	 */
	public static final String PARAM_TRANS_LANGUAGE = "transLanguage";

	/**
	 * The <b>user ID</b> parameter. It is the user's identification the
	 * connector will use to authenticate itself on the Maximo server. Mandatory
	 * only if <i>Authentication Required</i> is checked.
	 * 
	 * @see #PARAM_AUTHENTICATION_REQUIRED
	 * @see #getUserId()
	 */
	public static final String PARAM_USER_ID = "userId";

	/**
	 * The <b>XML character validation</b> parameter. It indicates if invalid
	 * Unicode characters should be removed from XML content before parsing it.
	 * The default value is false.
	 * 
	 * @see #isXmlCharValidationEnabled()
	 */
	public static final String PARAM_XML_CHAR_VALIDATION = "xmlCharacterValidation";

	public static final String PARAM_QUERY_ARGS = "queryArgs";

	/**
	 * Properties file in the classpath.
	 */
	public static final String PROPS_CLASSPATH = "generic-maximo-connector.properties";

	/**
	 * Properties file in the TDI solution directory.
	 */
	public static final String PROPS_SOLUTION_DIRECTORY = "etc/generic-maximo-connector.properties";

	/**
	 * Prefix of the property keys that should be loaded.
	 */
	public static final String PROPS_PREFIX = "com.ibm.di.maximo.";

	/**
	 * Name of the system property that indicates the location of external
	 * properties file.
	 */
	public static final String PROPS_SYSTEM = "generic-maximo-connector.configuration";

	/**
	 * Set of the parameter keys defined by TDI.
	 */
	public static final Set<String> TDI_PARAMETER_KEYS;

	private static final String URL_EXTERNAL_SYSTEM_PREFIX = "/es/";

	private static final String URL_OS_PREFIX = "/os/";

	private static final String URL_XSD_PREFIX = "/schema/service/";

	private static final String URL_XSD_SUFFIX = "Service.xsd";

	static {
		final Set<String> params = new HashSet<String>();
		params.add(PARAM_AUTHENTICATION_REQUIRED);
		params.add(PARAM_ERROR_ON_EXCEDENT_SIZE);
		params.add(PARAM_EXTSYS);
		params.add(PARAM_ES_CREATE);
		params.add(PARAM_ES_QUERY);
		params.add(PARAM_ES_UPDATE);
		params.add(PARAM_ES_SYNC);
		params.add(PARAM_MX_BASE_URL);
		params.add(PARAM_MX_VERSION);
		params.add(PARAM_MAXOBJECT_ES_QUERY);
		params.add(PARAM_MAXOBJECT_OS);
		params.add(PARAM_MBO);
		params.add(PARAM_OBJECT_STRUCTURE);
		params.add(PARAM_PAGE_SIZE);
		params.add(PARAM_PASSWORD);
		params.add(PARAM_QUERY_CRITERIA);
		params.add(PARAM_USER_ID);
		params.add(PARAM_XML_CHAR_VALIDATION);
		params.add(PARAM_TIMEOUT);
		params.add(PARAM_REPLACE_ON_UPDATE);
		params.add(PARAM_TRANS_LANGUAGE);
		params.add(PARAM_QUERY_ARGS);
		params.add(PARAM_MX_SERVICE_BASE);
		TDI_PARAMETER_KEYS = Collections.unmodifiableSet(params);
	}

	private static String[] extractURLs(final String urlList) {
		return urlList.split("[\n\r ,]");
	}

	private final SchemaConfiguration schCfg;

	/**
	 * Constructs a new {@link MxConnConfiguration}.
	 */
	public MxConnConfiguration(Log logger) {
		super(logger);
		schCfg = new SchemaConfiguration();
	}

	/**
	 * Checks if the mandatory parameters are defined.
	 * <p>
	 * Parameters checked:
	 * </p>
	 * <ul>
	 * <li>{@link #PARAM_MX_BASE_URL base URL}</li>
	 * <li>{@link #PARAM_OBJECT_STRUCTURE object structure}</li>
	 * </ul>
	 * These parameters does not have default values and MUST be specified in
	 * the configuration for all modes.
	 * 
	 * @throws MxConnConfigException
	 *             if any of the mandatory parameters is missing
	 */
	private void checkAllModes() throws MxConnConfigException {
		checkParamAndThrow(PARAM_MX_BASE_URL);
		checkParamAndThrow(PARAM_OBJECT_STRUCTURE);
	}

	/**
	 * Checks if the mandatory parameters for AddOnly mode are defined.
	 * <p>
	 * Parameters checked:
	 * </p>
	 * <ul>
	 * <li>all the parameters checked by {@link #checkAllModes()}</li>
	 * <li>{@link #PARAM_ES_CREATE CREATE enterprise service}</li>
	 * </ul>
	 * 
	 * @throws MxConnConfigException
	 *             if any of the mandatory parameters is missing
	 * @see #checkAllModes()
	 */
	public void checkAddOnly() throws MxConnConfigException {
		checkAllModes();

		if (isDefined(PARAM_EXTSYS)) {
			checkParamAndThrow(PARAM_ES_CREATE);
		}
	}

	/**
	 * Checks if the mandatory parameters for Delete mode are defined.
	 * <p>
	 * Parameters checked:
	 * </p>
	 * <ul>
	 * <li>all the parameters checked by {@link #checkIterator()}</li>
	 * <li>{@link #PARAM_ES_DELETE DELETE enterprise service}</li>
	 * </ul>
	 * 
	 * @throws MxConnConfigException
	 *             if any of the mandatory parameters is missing
	 * @see #checkAllModes()
	 */
	public void checkDelete() throws MxConnConfigException {
		checkIterator();

		if (isDefined(PARAM_EXTSYS)) {
			//Added as part of defect 15327
			//checkParamAndThrow(PARAM_ES_DELETE);
			checkParamAndThrow(PARAM_ES_SYNC);
		}
	}

	/**
	 * Checks if the mandatory parameters for Iterator mode are defined and
	 * valid.
	 * <p>
	 * Parameters checked:
	 * </p>
	 * <ul>
	 * <li>all the parameters checked by {@link #checkAllModes()}</li>
	 * <li>{@link #PARAM_ES_QUERY QUERY enterprise service}</li>
	 * <li>{@link #PARAM_PAGE_SIZE page size}</li>
	 * </ul>
	 * 
	 * @throws MxConnConfigException
	 *             if any of the mandatory parameters is missing or invalid
	 * @see #checkAllModes()
	 */
	public void checkIterator() throws MxConnConfigException {
		checkAllModes();

		if (isDefined(PARAM_EXTSYS)) {
			checkParamAndThrow(PARAM_ES_QUERY);
		}

		if (getPageSize() <= 0) {
			throw new MxConnConfigException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.INVALID.PAGE.SIZE.VALUE"));
		}
	}

	/**
	 * Checks if the mandatory parameters for Update mode are defined.
	 * <p>
	 * Parameters checked:
	 * </p>
	 * <ul>
	 * <li>all the parameters checked by {@link #checkIterator()} and
	 * {@link #checkAddOnly()}</li>
	 * <li>{@link #PARAM_ES_UPDATE UPDATE enterprise service}</li>
	 * </ul>
	 * 
	 * @throws MxConnConfigException
	 *             if any of the mandatory parameters is missing
	 * @see #checkAddOnly()
	 * @see #checkIterator()
	 */
	public void checkUpdate() throws MxConnConfigException {
		checkIterator();
		checkAddOnly();
		
		if (isDefined(PARAM_EXTSYS)) {
			checkParamAndThrow(PARAM_ES_UPDATE);
		}
	}

	/**
	 * Checks if the mandatory parameters for AddOnly, Update and Delete mode
	 * are defined.
	 * <p>
	 * Parameters checked:
	 * </p>
	 * <ul>
	 * <li>all the parameters checked by {@link #checkAllModes()}</li>
	 * <li>{@link #PARAM_ES_SYNC SYNC Enterprise service}</li>
	 * </ul>
	 * <p>
	 * This method is used by the {@link TpaeIFConnector} to check required
	 * parameters for AddOnly, Update and Delete modes.
	 * 
	 * @throws MxConnConfigException
	 *             if any of the mandatory parameters is missing
	 * @see #checkAllModes()
	 */
	public void checkAUDmodes() throws MxConnConfigException {
		checkAllModes();

		if (isDefined(PARAM_EXTSYS)) {
			checkParamAndThrow(PARAM_ES_SYNC);
		}
	}

	/**
	 * Returns the DOMAIN Object Structure.
	 * 
	 * @return DOMAIN Object Structure
	 * @see #PARAM_DOMAIN_OS
	 */
	public String getDomainObjectStructure() {
		return getParameter(PARAM_DOMAIN_OS, DEFAULT_DOMAIN_OS);
	}

	/**
	 * Returns the Maximo version.
	 * 
	 * @return Maximo version
	 * @see #PARAM_MX_VERSION
	 */
	public String getMaximoVersion() {
		return getParameter(PARAM_MX_VERSION, DEFAULT_MX_VERSION);
	}

	/**
	 * Returns the MAXOBJECT object structure.
	 * 
	 * @return MAXOBJECT object structure
	 * @see #PARAM_MAXOBJECT_OS
	 */
	public String getMaxobjectObjectStructure() {
		return getParameter(PARAM_MAXOBJECT_OS, DEFAULT_MAXOBJECT_OS);
	}

	/**
	 * Returns the MBO.
	 * 
	 * @return MBO
	 * @throws MxConnectorException
	 *             if no MBO is defined and it is not possible discover the
	 *             top-level MBO
	 * @see #PARAM_MBO
	 * @see #getSchema()
	 */
	public String getMbo() throws MxConnectorException {
		if (isDefined(PARAM_MBO)) {
			return getParams().get(PARAM_MBO);
		}
		return getSchema().getRootMbo().getName();
	}

	/**
	 * Returns the object structure.
	 * 
	 * @return object structure
	 * @see #PARAM_OBJECT_STRUCTURE
	 */
	public String getObjectStructure() {
		return getParams().get(PARAM_OBJECT_STRUCTURE);
	}

	/**
	 * Returns the page size.
	 * 
	 * @return page size
	 * @see #PARAM_PAGE_SIZE
	 */
	public int getPageSize() {
		return getParameterAsInt(PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);
	}

	/**
	 * Returns the password the connector will use to authenticate itself on the
	 * Maximo server.
	 * 
	 * @return password the connector will use to authenticate itself on the
	 *         Maximo server
	 * @see #PARAM_PASSWORD
	 */
	public String getPassword() {
		return getParams().get(PARAM_PASSWORD);
	}

	/**
	 * Returns the query criteria.
	 * <p>
	 * The default value is an empty tag of the top-level MBO. For example: "
	 * <code>&lt;ASSET&gt;&lt;/ASSET&gt;</code>"
	 * </p>
	 * 
	 * @return the query criteria
	 * @throws MxConnectorException
	 *             if no query is defined and it is not possible discover the
	 *             top-level MBO
	 * @see #PARAM_QUERY_CRITERIA
	 * @see #getSchema()
	 */
	public String getQueryCriteria() throws MxConnectorException {
		if (isDefined(PARAM_QUERY_CRITERIA)) {
			return getParams().get(PARAM_QUERY_CRITERIA);
		}
		final String mbo = getSchema().getRootMbo().getName();
		return "<" + mbo + "></" + mbo + ">";
	}

	/**
	 * Returns the schema object from the configuration parameters.
	 * 
	 * @return schema object
	 * @throws MxConnectorException
	 *             if any of the mandatory parameters is missing or if it is not
	 *             possible build the schema object
	 * @see #checkAllModes()
	 */
	public Schema getSchema() throws MxConnectorException {
		checkAllModes();

		schCfg.setAuthenticationRequired(isAuthenticationRequired());
		schCfg.setMaxobjObjectStructure(getMaxobjectObjectStructure());
		schCfg.setMaxobjUrlList(getUrlListForQueryMaxObject());
		schCfg.setMosName(getObjectStructure());
		schCfg.setUserId(getUserId());
		schCfg.setPassword(getPassword());
		schCfg.setXsdSufix(getXsdSufix());
		schCfg.setXsdUrlList(getUrlListForXSD());
		schCfg.setUrlList(getUrlList());
		schCfg.setTimeout(getTimeout());
		schCfg.setTransactionLang(getTransLanguage());
		schCfg.setServiceBase(getServiceBase());

		return Schema.getInstance(schCfg, logger);
	}

	/**
	 * Returns the timeout.
	 * 
	 * @return timeout
	 * @see #PARAM_TIMEOUT
	 */
	public int getTimeout() {
		return getParameterAsInt(PARAM_TIMEOUT, DEFAULT_TIMEOUT);
	}

	/**
	 * Returns whether a replace or merge operation will be performed upon update.
	 * 
	 * @return <code>true</code> if replace will be performed, <code>false</code> otherwise.
	 * @see #PARAM_REPLACE_ON_UPDATE
	 */
	public boolean getReplaceOnUpdate() {
		return getParameterAsBoolean(PARAM_REPLACE_ON_UPDATE);
	}

	/**
	 * Returns the transaction language.
	 * 
	 * @return transaction language
	 * @see #PARAM_TRANS_LANGUAGE
	 */
	public String getTransLanguage() {
		String translang = getParams().get(PARAM_TRANS_LANGUAGE);
		if(translang == null || translang.length() == 0)
			return getParameter(PARAM_TRANS_LANGUAGE, DEFAULT_TRANS_LANGUAGE);
		else 
			return translang;
	}

	/**
	 * Returns a list of URLs.
	 * 
	 * @return a list of URLs
	 * @see #PARAM_MX_BASE_URL
	 */
	public List<String> getUrlList() {
		return buildUrlList("");
	}

	/**
	 * Returns a list of URLs to perform create operations.
	 * 
	 * @return a list of URLs to perform create operations
	 * @see #PARAM_MX_BASE_URL
	 * @see #PARAM_EXTSYS
	 * @see #PARAM_ES_CREATE
	 */
	public List<String> getUrlListForCreate() {

		if (hasExternalSystem()) {
			List<String> l = buildEsUrlList(getParams().get(PARAM_ES_CREATE));
			return l;
		}

		// If no External System is defined, use Object Structure
		return buildOsUrlList(getParams().get(PARAM_OBJECT_STRUCTURE));
	}

	/**
	 * Returns a list of URLs to perform delete operations.
	 * 
	 * @return a list of URLs to perform delete operations
	 * @see #PARAM_MX_BASE_URL
	 * @see #PARAM_EXTSYS
	 * @see #PARAM_ES_DELETE
	 */
	public List<String> getUrlListForDelete() {

		if (hasExternalSystem()) {
			return buildEsUrlList(getParams().get(PARAM_ES_DELETE));
		}

		// If no External System is defined, use Object Structure
		return buildOsUrlList(getParams().get(PARAM_OBJECT_STRUCTURE));
	}

	/**
	 * Returns a list of URLs to perform query operations.
	 * 
	 * @return a list of URLs to perform query operations
	 * @see #PARAM_MX_BASE_URL
	 * @see #PARAM_EXTSYS
	 * @see #PARAM_ES_QUERY
	 */
	public List<String> getUrlListForQuery() {
		if (hasExternalSystem()) {
			return buildEsUrlList(getParams().get(PARAM_ES_QUERY));
		}

		// If no External System is defined, use Object Structure
		return buildOsUrlList(getParams().get(PARAM_OBJECT_STRUCTURE));
	}

	/**
	 * Returns a list of URLs to perform query operations on the MAXDOMAIN
	 * object structure.
	 * 
	 * @return a list of URLs to perform query operations on the MAXDOMAIN
	 *         object structure
	 * @see #PARAM_MX_BASE_URL
	 * @see #PARAM_EXTSYS
	 * @see #PARAM_DOMAIN_ES_QUERY
	 */
	public List<String> getUrlListForQueryMaxDomain() {
		if (hasExternalSystem()) {
			return buildEsUrlList(getParameter(PARAM_DOMAIN_ES_QUERY, DEFAULT_DOMAIN_ES_QUERY));
		}

		// If no External System is defined, use Object Structure
		return buildOsUrlList(getParameter(PARAM_DOMAIN_OS, DEFAULT_DOMAIN_OS));
	}

	/**
	 * Returns a list of URLs to perform query operations on the MAXOBJECT
	 * object structure.
	 * 
	 * @return a list of URLs to perform query operations on the MAXOBJECT
	 *         object structure
	 * @see #PARAM_MX_BASE_URL
	 * @see #PARAM_EXTSYS
	 * @see #PARAM_MAXOBJECT_ES_QUERY
	 */
	public List<String> getUrlListForQueryMaxObject() {

		if (this.hasExternalSystem()) {
			return buildEsUrlList(getParameter(PARAM_MAXOBJECT_ES_QUERY, DEFAULT_MAXOJBECT_ES_QUERY));
		}

		// If no External System is defined, use Object Structure
		return buildOsUrlList(getParameter(PARAM_MAXOBJECT_OS, DEFAULT_MAXOBJECT_OS));
	}

	/**
	 * Returns a list of URLs to perform update operations.
	 * 
	 * @return a list of URLs to perform update operations
	 * @see #PARAM_MX_BASE_URL
	 * @see #PARAM_EXTSYS
	 * @see #PARAM_ES_UPDATE
	 */
	public List<String> getUrlListForUpdate() {
		if (this.hasExternalSystem()) {
			return buildEsUrlList(getParams().get(PARAM_ES_UPDATE));
		}

		// If no External System is defined, use Object Structure
		return buildOsUrlList(getParams().get(PARAM_OBJECT_STRUCTURE));
	}

	/**
	 * @return a list of URLs to perform synchronize operations
	 * @see #PARAM_EXTSYS
	 * @see #PARAM_ES_SYNC
	 */
	public List<String> getUrlListForSync() {
		if (this.hasExternalSystem()) {
			return buildEsUrlList(getParams().get(PARAM_ES_SYNC));
		}

		// If no External System is defined, use Object Structure
		return buildOsUrlList(getParams().get(PARAM_OBJECT_STRUCTURE));
	}

	/**
	 * Returns a list of XSD URLs.
	 * 
	 * @return a list of XSD URLs
	 * @see #PARAM_MX_BASE_URL
	 * @see #PARAM_OBJECT_STRUCTURE
	 * @see #getXsdSufix()
	 */
	public List<String> getUrlListForXSD() {
		return buildUrlList(getXsdSufix());
	}

	/**
	 * Returns the user ID.
	 * 
	 * @return user ID
	 * @see #PARAM_USER_ID
	 */
	public String getUserId() {
		return getParams().get(PARAM_USER_ID);
	}

	/**
	 * Returns the XSD URL suffix.
	 * 
	 * @return XSD URL suffix
	 * @see #PARAM_OBJECT_STRUCTURE
	 */
	public String getXsdSufix() {
		return getServiceBase() + URL_XSD_PREFIX + getObjectStructure() + URL_XSD_SUFFIX;
	}

	/**
	 * Indicates if the Maximo server requires basic user authentication.
	 * 
	 * @return <code>true</code> if the Maximo server requires basic user
	 *         authentication, otherwise <code>false</code>
	 * @see #PARAM_AUTHENTICATION_REQUIRED
	 */
	public boolean isAuthenticationRequired() {
		return getParameterAsBoolean(PARAM_AUTHENTICATION_REQUIRED);
	}

	/**
	 * Indicates if an error should be thrown when a text field exceeds the
	 * maximum size.
	 * 
	 * @return <code>true</code> if an error should be thrown when a text field
	 *         exceeds the maximum size. otherwise <code>false</code>
	 * @see #PARAM_ERROR_ON_EXCEDENT_SIZE
	 */
	public boolean isErrorOnExcedentSizeEnabled() {
		return getParameterAsBoolean(PARAM_ERROR_ON_EXCEDENT_SIZE);
	}

	/**
	 * Indicates if invalid Unicode characters should be removed from XML
	 * content before parsing it.
	 * 
	 * @return <code>true</code> if invalid Unicode characters should be removed
	 *         from XML content before parsing it, otherwise <code>false</code>
	 * @see #PARAM_XML_CHAR_VALIDATION
	 */
	public boolean isXmlCharValidationEnabled() {
		return getParameterAsBoolean(PARAM_XML_CHAR_VALIDATION);
	}

	/**
	 * Loads configuration parameters from properties files.
	 * <p>
	 * The configuration parameters are loaded from the following resources
	 * (respecting this precedence):
	 * </p>
	 * <ul>
	 * <li>{@link #PROPS_CLASSPATH properties file in the classpath} (in order
	 * to be loaded, the properties file should be inside a ZIP file)</li>
	 * <li>{@link #PROPS_SOLUTION_DIRECTORY properties file in the TDI solution
	 * directory}</li>
	 * <li>{@link #PROPS_SYSTEM system property} that indicates the path to the
	 * properties file</li>
	 * </ul>
	 * <p>
	 * <b>Note</b>: only the properties starting with {@link #PROPS_PREFIX} will
	 * be loaded.
	 * </p>
	 */
	public void loadParameters() {
		loadClasspathParameters();
		loadSolutionDirParameters();
		loadSystemParameters();
	}

	private List<String> buildOsUrlList(final String objectStructure) {
		return buildUrlList(getServiceBase() + URL_OS_PREFIX + objectStructure);
	}

	private List<String> buildEsUrlList(final String enterpriseService) {
		return buildUrlList(getServiceBase() + URL_EXTERNAL_SYSTEM_PREFIX + getParams().get(PARAM_EXTSYS) + "/" + enterpriseService);
	}

	private List<String> buildUrlList(final String suffix) {
		final String[] urls;
		final List<String> result;
		final Set<String> urlSet = new HashSet<String>();

		urls = extractURLs(getParameter(PARAM_MX_BASE_URL, DEFAULT_MX_BASE_URL));
		result = new ArrayList<String>(urls.length);

		for (final String url : urls) {
			if (StringUtils.isBlank(url) || urlSet.contains(url)) {
				continue;
			}
			result.add(url + suffix);
			urlSet.add(url);
		}
		return result;
	}

	private void loadClasspathParameters() {
		loadFromResource(PROPS_CLASSPATH, PROPS_PREFIX);
		logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CONFIG.PARAMS.CLASSPATH", toString()));
	}

	private void loadSolutionDirParameters() {
		loadFromFile(PROPS_SOLUTION_DIRECTORY, PROPS_PREFIX);
		logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CONFIG.PARAMS.SOLDIR", toString()));
	}

	private void loadSystemParameters() {
		final String fileName = System.getProperty(PROPS_SYSTEM);
		if (!StringUtils.isBlank(fileName)) {

			logger.info(SimpleTpaeIFConnector.getResHash().getString("MXCONN.LOAD.PROPERTIES", fileName));

			if (loadFromFile(fileName, PROPS_PREFIX)) {
				logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CONFIG.PARAMS.SYSTEM", toString()));
			} else {
				logger.info(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.LOAD.PROPERTIES", fileName));
			}
		}
	}

	private boolean hasExternalSystem() {
		return isDefined(PARAM_EXTSYS);
	}
	
	private String getServiceBase() {
		String s = getParameter(PARAM_MX_SERVICE_BASE, DEFAULT_MX_SERVICE_BASE);
		return s.startsWith("/") ? s : "/" + s;
	}
}
