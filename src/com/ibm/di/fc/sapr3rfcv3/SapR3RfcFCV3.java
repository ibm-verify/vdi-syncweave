/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import java.util.Properties;
import java.io.IOException;
import java.io.StringReader;

import com.ibm.di.server.Trace;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.server.Log;
import com.sap.conn.jco.ext.DestinationDataProvider;

import org.w3c.dom.Document;

/**
 * <p>
 * ITDI Function Component for SAP R/3 RFC Invocations.
 * </p>
 * 
 * <p>
 * This function component provides the ability to invoke an arbitrary ABAP RFC
 * that is exposed by SAP R/3. It supports SAP R/3 4.6C.
 * </p>
 * 
 * <p>
 * This FC establishes connections and invokes RFCs using SAPs middleware JCo.
 * To use this function component the correct version of JCo (currently 2.1.3)
 * must be installed on your system. See http://service.sap.com/connectors for
 * details on how to download and install SAP JCo.
 * </p>
 * 
 * <p>
 * Configuration is accomplished by setting logon parameters for client
 * connections to R/3. The parameters are very similar to the logon parameters
 * for the traditional SAP GUI. See initialize for more details on how to
 * initialize.
 * </p>
 * 
 * <p>
 * {@link #initialize} must be the first operation called in this class.
 * {@link #perform} can then be called one or more times. {@link #terminate}
 * must be called to allow connection cleanup before the class is destroyed.
 * </p>
 * 
 * 
 * <p>
 * A typical connection to SAP requires setting the following SAP client
 * configuration parameters. These should be set prior to calling initialize.
 * <ul>
 * <li> PARAM_CONFIG_CLIENT
 * <li> PARAM_CONFIG_USER
 * <li> PARAM_CONFIG_PASSWORD
 * <li> PARAM_CONFIG_LANGUAGE
 * <li> PARAM_CONFIG_SYSTEM_NUMBER
 * <li> PARAM_CONFIG_APPLICATION_SERVER
 * <li> PARAM_CONFIG_GATEWAY_HOST
 * </ul>
 * </p>
 * 
 * <p>
 * Additional parameters are also available. See the table of PARAM_CONFIG_*
 * values for a brief explanation.
 * </p>
 * 
 * 
 * <p>
 * <b>Using the FC</b><br>
 * It can be placed in an assembly line or invoked directly from script. It is
 * the callers' responsibility to check the returned Entry object for any errors
 * that may have resulted from invoking the RFC. The FC can be invoked directly
 * from script. As an example the following code can be used to invoke an RFC
 * from JavaScript using the XML string style:
 * </p>
 * <code>
 var fc = system.getFunction("ibmdi.SapR3RfcFCV3");<br>
 var docResponse = null;<br>
 var response;<br>
 fc.setParam(fc.PARAM_CONFIG_CLIENT, "200");<br>
 fc.setParam(fc.PARAM_CONFIG_USER, "SMITH");<br>
 fc.setParam(fc.PARAM_CONFIG_PASSWORD, "PASSWORD");<br>
 fc.setParam(fc.PARAM_CONFIG_SYSNUMBER, "11");<br>
 fc.setParam(fc.PARAM_CONFIG_APPLICATION_SERVER, "servername");<br>
 fc.initialize(null);<br>
 var rfc = new java.lang.String("&ltBAPI_COMPANYCODE_GETLIST/&gt");<br>
 var myentry = system.newEntry();<br>
 myentry.setAttribute(fc.PARAM_INPUT, rfc);<br>
 Attribute reqType = new Attribute();<br>
 reqType.addValue(SapR3RfcFCV3.PARAM_VAL_STRING);<br>
 entry.setAttribute(SapR3RfcFCV3.PARAM_INPUT_TYPE, reqType);<br>
 myentry = fc.perform(myentry);<br>
 var output = myentry.getAttribute(fc.PARAM_OUTPUT);<br>
 response = output.getValue(0);<br>
 fc.terminate();<br>
 </code>
 * 
 * <p>
 * Note that configuration parameters must be set before initialize() is called,
 * and terminate() should be called to cleanup.
 * </p>
 * 
 * Here is another example invoking the RFC FC using the multi valued attributes
 * style:<br>
 * <code>
 var rfc = system.newAttribute("BAPI_SALESORDER_GETLIST");<br>
 var attr1 = system.newAttribute("CUSTOMER_NUMBER");<br>
 attr1.addValue("0000000016");<br>
 var attr2 = system.newAttribute("SALES_ORGANIZATION");<br>
 attr2.addValue("AU01");<br>
 rfc.addValue(attr1);<br>
 rfc.addValue(attr2);<br>
 
 var entry = system.newEntry(); <br> 
 entry.setAttribute("requestType", "multiValuedAttributes");<br>
 var reqAttr = entry.newAttribute("request");<br>
 reqAttr.addValue(rfc);<br>
 var result = fc.perform(entry); <br>  
 * </code>
 */
public final class SapR3RfcFCV3 extends Function {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/*
	 * The version string returned by getVersion(). The %% tokens are replaced
	 * at compile time by ANT script.
	 */
	private static final String VERSION_INFO = "3.0-di7.1.1 %I% 20%E%";

	/*
	 * The connector name as reported in the log files. The %% tokens are
	 * replaced at compile time by ANT script.
	 */
	private static final String COMPONENT_NAME = "SAP ABAP AS RFC Functional Component V3";

	private static final String SAPCLIENT_POOL_ID = "IDISAPR3_POOL";

	private static final int SAPCLIENT_MAX_POOLED_CONNECTIONS = 3;

	/*
	 * These parameters are set for configuration of the FC.
	 */
	public static final String PARAM_JCO_CLIENT_OPTIONS_PREFIX = "jco.client.";

	/** SAP client (jco.client.client). */
	public static final String PARAM_CONFIG_CLIENT = "client";

	/** Logon user (jco.client.user). */
	public static final String PARAM_CONFIG_USER = "user";

	/** Alias user name (jco.client.alias_user). */
	public static final String PARAM_CONFIG_ALIAS_USER = "alias_user";

	/** Logon password (jco.client.passwd). */
	public static final String PARAM_CONFIG_PASSWORD = "passwd";

	/** SAP system number (jco.client.sysnr). */
	public static final String PARAM_CONFIG_SYSNUMBER = "sysnr";

	/** SAP application server (jco.client.ashost). */
	public static final String PARAM_CONFIG_APPLICATION_SERVER = "ashost";

	/** SAP message server (jco.client.mshost). */
	public static final String PARAM_CONFIG_MESSAGE_SERVER = "mshost";

	/** Gateway host (jco.client.gwhost). */
	public static final String PARAM_CONFIG_GATEWAY_HOST = "gwhost";

	/** Gateway service (jco.client.gwserv). */
	public static final String PARAM_CONFIG_GATEWAY_SERVICE = "gwserv";

	/** Logon language (jco.client.lang). */
	public static final String PARAM_CONFIG_LANGUAGE = "lang";

	/** 1 (Enable) or 0 (disable) RFC trace (jco.client.trace). */
	public static final String PARAM_CONFIG_TRACE = "trace";

	/** Initial codepage in SAP notation (jco.client.codepage). */
	public static final String PARAM_CONFIG_CODEPAGE = "codepage";

	/**
	 * Secure network connection (SNC) mode, 0 (off) or 1 (on)
	 * (jco.client.snc_mode).
	 */
	public static final String PARAM_CONFIG_SNC_MODE = "snc_mode";

	/** SNC partner, e.g. p:CN=R3, O=XYZ-INC, C=EN (jco.client.snc_partnername). */
	public static final String PARAM_CONFIG_SNC_PARTNERNAME = "snc_partnername";

	/** SNC level of security, 1 to 9 (jco.client.snc_qop). */
	public static final String PARAM_CONFIG_SNC_QOP = "snc_qop";

	/** SNC name. Overrides default SNC partner (jco.client.snc_myname). */
	public static final String PARAM_CONFIG_SNC_MYNAME = "snc_myname";

	/** Path to library which provides SNC service (jco.client.snc_lib). */
	public static final String PARAM_CONFIG_SNC_LIB = "snc_lib";

	/** SAP R/3 name (jco.client.r3name). */
	public static final String PARAM_CONFIG_R3NAME = "r3name";

	/** Group of SAP application servers (jco.client.group). */
	public static final String PARAM_CONFIG_GROUP = "group";

	/** Program ID of external server program (jco.client.tpname). */
	public static final String PARAM_CONFIG_TPNAME = "tpname";

	/** Host of external server program (jco.client.tphost). */
	public static final String PARAM_CONFIG_TPHOST = "tphost";

	/** Type of remote host 2 = R/2, 3 = R/3, E = External (jco.client.type). */
	public static final String PARAM_CONFIG_TYPE = "type";

	/** Enable ABAP debugging 0 or 1 (jco.client.abap_debug). */
	public static final String PARAM_CONFIG_ABAP_DEBUG = "abap_debug";

	/** Use remote SAP graphical user interface (0/1/2) (jco.client.use_sapgui). */
	public static final String PARAM_CONFIG_USE_SAPGUI = "use_sapgui";

	/** Get/Don't get a SSO ticket after logon (1 or 0) (jco.client.getsso2). */
	public static final String PARAM_CONFIG_GETSSO2 = "getsso2";

	/**
	 * Use the specified SAP Cookie Version 2 as logon ticket
	 * (jco.client.mysapsso2).
	 */
	public static final String PARAM_CONFIG_MYSAPSS02 = "mysapsso2";

	/** Use the specified X509 certificate as logon ticket (jco.client.x509cert). */
	public static final String PARAM_CONFIG_X509_CERTFICATE = "x509cert";

	/**
	 * Enable/Disable logon check at open time, 1 (enable) or 0 (disable)
	 * (jco.client.lcheck).
	 */
	public static final String PARAM_CONFIG_LOGON_CHECK = "lcheck";

	/** String defined for SAPLOGON on 32-bit Windows (jco.client.saplogon_id). */
	public static final String PARAM_CONFIG_SAPLOGON_ID = "saplogon_id";

	/** Data for external authentication (PAS) (jco.client.extiddata). */
	public static final String PARAM_CONFIG_EXTERNAL_AUTHENTICATION_DATA = "extiddata";

	/** Type of external authentication (PAS) (jco.client.extidtype). */
	public static final String PARAM_CONFIG_EXTERNAL_ID_TYPE = "extidtype";

	/**
	 * Idle timeout (in seconds) for the connection after which it will be
	 * closed by R/3. Only positive values are allowed.
	 * (jco.client.idle_timeout).
	 */
	public static final String PARAM_CONFIG_IDLE_TIMEOUT = "idle_timeout";

	/** Enable (1) or Disable (0) dsr support (jco.client.dsr). */
	public static final String PARAM_CONFIG_DSR = "dsr";

	private final String[] PARAM_JCO_CLIENT_OPTIONS = { PARAM_CONFIG_CLIENT,
			PARAM_CONFIG_USER, PARAM_CONFIG_ALIAS_USER, PARAM_CONFIG_PASSWORD,
			PARAM_CONFIG_SYSNUMBER, PARAM_CONFIG_APPLICATION_SERVER,
			PARAM_CONFIG_MESSAGE_SERVER, PARAM_CONFIG_GATEWAY_HOST,
			PARAM_CONFIG_GATEWAY_SERVICE, PARAM_CONFIG_LANGUAGE,
			PARAM_CONFIG_TRACE, PARAM_CONFIG_CODEPAGE, PARAM_CONFIG_SNC_MODE,
			PARAM_CONFIG_SNC_PARTNERNAME, PARAM_CONFIG_SNC_QOP,
			PARAM_CONFIG_SNC_MYNAME, PARAM_CONFIG_SNC_LIB, PARAM_CONFIG_R3NAME,
			PARAM_CONFIG_GROUP, PARAM_CONFIG_TPNAME, PARAM_CONFIG_TPHOST,
			PARAM_CONFIG_TYPE, PARAM_CONFIG_ABAP_DEBUG,
			PARAM_CONFIG_USE_SAPGUI, PARAM_CONFIG_GETSSO2,
			PARAM_CONFIG_MYSAPSS02, PARAM_CONFIG_X509_CERTFICATE,
			PARAM_CONFIG_LOGON_CHECK, PARAM_CONFIG_SAPLOGON_ID,
			PARAM_CONFIG_EXTERNAL_AUTHENTICATION_DATA,
			PARAM_CONFIG_EXTERNAL_ID_TYPE, PARAM_CONFIG_IDLE_TIMEOUT,
			PARAM_CONFIG_DSR };

	private static final String[] EXT_PARAM_JCO_CLIENT_OPTIONS = { PARAM_CONFIG_CLIENT,
			PARAM_CONFIG_USER, PARAM_CONFIG_ALIAS_USER, PARAM_CONFIG_PASSWORD,
			PARAM_CONFIG_SYSNUMBER, PARAM_CONFIG_APPLICATION_SERVER,
			PARAM_CONFIG_MESSAGE_SERVER, PARAM_CONFIG_GATEWAY_HOST,
			PARAM_CONFIG_GATEWAY_SERVICE, PARAM_CONFIG_LANGUAGE,
			PARAM_CONFIG_TRACE, PARAM_CONFIG_CODEPAGE, PARAM_CONFIG_SNC_MODE,
			PARAM_CONFIG_SNC_PARTNERNAME, PARAM_CONFIG_SNC_QOP,
			PARAM_CONFIG_SNC_MYNAME, PARAM_CONFIG_SNC_LIB, PARAM_CONFIG_R3NAME,
			PARAM_CONFIG_GROUP, PARAM_CONFIG_TPNAME, PARAM_CONFIG_TPHOST,
			PARAM_CONFIG_TYPE, PARAM_CONFIG_ABAP_DEBUG,
			PARAM_CONFIG_USE_SAPGUI, PARAM_CONFIG_GETSSO2,
			PARAM_CONFIG_MYSAPSS02, PARAM_CONFIG_X509_CERTFICATE,
			PARAM_CONFIG_LOGON_CHECK, PARAM_CONFIG_SAPLOGON_ID,
			PARAM_CONFIG_EXTERNAL_AUTHENTICATION_DATA,
			PARAM_CONFIG_EXTERNAL_ID_TYPE, PARAM_CONFIG_IDLE_TIMEOUT,
			PARAM_CONFIG_DSR };

	/* keep track of whether we have been initialized */
	private boolean initialized;

	/*
	 * Schema response values for XML or DOM mode attribute mapping. the value
	 * for attribute "requestType" is either "xmlDomElement", "xmlString",
	 * "multiValuedAttributes" the value for attribute "responseType" is one of
	 * the same values as above.
	 */
	/** Attribute named requestType set on Entry input. */
	public static final String PARAM_INPUT_TYPE = "requestType";

	/** Name of attribute on output with string value indicating response type. */
	public static final String PARAM_OUTPUT_TYPE = "responseType";

	/** Used to indicate response/request of type DOM document. */
	public static final String PARAM_VAL_DOM_DOCUMENT = "xmlDomDocument";

	/** Used to indicate response/request of type XML string. */
	public static final String PARAM_VAL_STRING = "xmlString";

	/** Used to identify response/request of type Attribute. */
	public static final String PARAM_VAL_MVA = "multiValuedAttributes";

	/*
	 * the attributes below have the schema indicated by the input
	 * 
	 */
	/** Attribute name of the RFC request. */
	public static final String PARAM_INPUT = "request";

	/** Attribute name of the RFC response. */
	public static final String PARAM_OUTPUT = "response";

	/*
	 * The R/3 connection properties
	 */
	private Properties jcoProps;

	/*
	 * The connectors' representation of the R/3 system. @modelguid
	 * {6B3F19E4-B198-46D0-82FD-2F7890D43329}
	 */
	private SapAdapter sapAdapter;

	/*
	 * The client used to establish connection to SAP.
	 * 
	 */
	private SapClientConnection sapClient;

	/* The way to perform logging. */
	private LogProxy logproxy;

	private boolean terminated;

	/**
	 * Constructor.
	 */
	public SapR3RfcFCV3() {
		super();
		logproxy = new LogProxyImpl();
		initialized = false;
		terminated = false;
	}

	public void setParam(String param, Object value) {
		super.setParam(param, value);
	}

	private Properties getR3ConnectionProperties() {
		if (jcoProps == null) {
			setR3ConnectionProperties(new Properties());
		}

		return jcoProps;
	}

	private void setR3ConnectionProperties(Properties p) {
		if (p == null) {
			throw new IllegalArgumentException();
		}

		jcoProps = p;
	}

	/**
	 * This function is called when the connector is no longer needed by the
	 * user. Always call terminate which will take care of releasing resources,
	 * closing parsers etc.
	 * 
	 * @throws Exception
	 *             If super class terminate fails.
	 * @throws SapR3RfcFCException
	 *             If internal SAP connection closure fails.
	 */
	public void terminate() throws SapR3RfcFCException, Exception {
		if (!isInitialized()) {
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0001);
			throw new SapR3RfcFCException(msg);
		}

		try {
			getSapClient().terminate();
		} finally {
			terminated = true;
		}

		super.terminate();
	}

	/**
	 * This function is called once after the connector configuration file has
	 * been provided by the caller.
	 * 
	 * @param o
	 *            The configuration object from TDI.
	 * @throws Exception
	 *             If super class initialize fails.
	 */
	public void initialize(Object o) throws Exception {
		super.initialize(o);
//		logmsg("Initialize SapR3fcFCV3");
		Trace.entrymid(this, "initialize");
//		System.out.println("Initialize");
//		logmsg("Initialize initJcoConfigProperties");
		initJcoConfigProperties();
//		logmsg("Initialize initJcoClientConnection");
		initJcoClientConnection();

		// Allow client to be setup at the beginning
		// Client imlementation can choose when to establish a connection.
		getSapClient().setup();
		setInitialized(true);

//		sapClient.unregister(); // unregister the destinationName
		
		Trace.exitmid(this, "initialize");
	}

	/**
	 * Returns the version of this FC.
	 * 
	 * @return String version + build date
	 */
	public String getVersion() {
		return VERSION_INFO;

	}

	/**Returns thelist of available JCO Client Options
	*
	*@return String[] PARAM_JCO_CLIENT_OPTIONS
	*/

	public static String[] getJCOClientOptions() {
		return EXT_PARAM_JCO_CLIENT_OPTIONS;
	}

	private RequestResponseType getRequestType(Object obj)
			throws SapR3FCParameterException {
		RequestResponseType result;
		if (obj instanceof Entry) {
			Entry entry = (Entry) obj;
			/*
			 * Check that we have an input parameter
			 */
			Attribute attrReqType = entry
					.getAttribute(SapR3RfcFCV3.PARAM_INPUT_TYPE);
			Attribute attrRequest = entry.getAttribute(SapR3RfcFCV3.PARAM_INPUT);
			if (attrReqType == null) {
				Object[] msgArgs = new Object[] { SapR3RfcFCV3.PARAM_INPUT_TYPE };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0002, msgArgs);
				throw new SapR3FCParameterException(msg);
			}
			if (attrRequest == null) {
				Object[] msgArgs = new Object[] { SapR3RfcFCV3.PARAM_INPUT };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0002, msgArgs);
				throw new SapR3FCParameterException(msg);
			}

			String reqTypeVal = attrReqType.getValue();
			if (reqTypeVal == null) {
				Object[] msgArgs = new Object[] { SapR3RfcFCV3.PARAM_INPUT_TYPE };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0002, msgArgs);
				throw new SapR3FCParameterException(msg);
			}

			if (SapR3RfcFCV3.PARAM_VAL_DOM_DOCUMENT.equals(reqTypeVal)) {
				Object objVal = attrRequest.getValueAV(0);
				if (objVal != null && objVal instanceof Document) {
					result = RequestResponseType.DOMDOC;
				} else {
					Object[] msgArgs = new Object[] { SapR3RfcFCV3.PARAM_INPUT,
							SapR3RfcFCV3.PARAM_VAL_DOM_DOCUMENT };
					String msg = LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_RFCFC_0002, msgArgs);
					throw new SapR3FCParameterException(msg);
				}
			} else if (SapR3RfcFCV3.PARAM_VAL_STRING.equals(reqTypeVal)) {
				Object objVal = attrRequest.getValueAV(0);
				if (objVal != null && objVal instanceof String) {
					result = RequestResponseType.XMLSTRING;
				} else {
					Object[] msgArgs = new Object[] { SapR3RfcFCV3.PARAM_INPUT,
							SapR3RfcFCV3.PARAM_VAL_STRING };
					String msg = LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_RFCFC_0002, msgArgs);
					throw new SapR3FCParameterException(msg);
				}
			} else if (SapR3RfcFCV3.PARAM_VAL_MVA.equals(reqTypeVal)) {
				Object objVal = attrRequest.getValueAV(0);
				if (objVal != null && objVal instanceof Attribute) {
					result = RequestResponseType.MVA;
				} else {
					Object[] msgArgs = new Object[] { SapR3RfcFCV3.PARAM_INPUT,
							SapR3RfcFCV3.PARAM_VAL_MVA };
					String msg = LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_RFCFC_0003, msgArgs);
					throw new SapR3FCParameterException(msg);
				}
			} else {
				Object[] msgArgs = new Object[] { SapR3RfcFCV3.PARAM_INPUT_TYPE };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0004, msgArgs);
				throw new SapR3FCParameterException(msg);
			}

			debug(LogMessageHelper.getMsgResource().getMessage(LogMessageHelper.SAPR3_RFCFC_0053, new Object[] {entry.toString()}));

			return result;
		}

		/*
		 * for now, dissallow anything but Entry objects!
		 */
		String msg = LogMessageHelper.getMsgResource().getMessage(
				LogMessageHelper.SAPR3_RFCFC_0005);
		throw new SapR3FCParameterException(msg);
	}

	/**
	 * <p>
	 * Execute the RFC. Must be called prior to calling this method.
	 * </p>
	 * 
	 * <p>
	 * The perform() method accepts an Entry object. If anything else is passed
	 * an Exception is thrown.
	 * </p>
	 * 
	 * <p>
	 * The Entry object contains two attributes: <code>request</code> and
	 * <code>requestType</code>. It supports three styles of invocation.
	 * </p>
	 * 
	 * <p>
	 * The value of <code>requestType</code> can be one of xmlDomDocument,
	 * xmlString, multiValuesAttributes. It indicates the type of the value
	 * associated with <code>request</code>.
	 * </p>
	 * 
	 * <p>
	 * The value of attribute <code>request</code> is a type of
	 * java.lang.String, org.w3c.dom.Document or com.ibm.di.entry.Attribute
	 * which contains the request as either a XML String, DOM Document, or
	 * multi-valued Attribute. Any other value will result in an Exception being
	 * thrown.
	 * </p>
	 * 
	 * <p>
	 * If <code>request</code> is of type org.w3c.dom.Document, then its
	 * associated value must be an org.w3c.dom.Document containing an XSchema
	 * which conforms to the specification for ABAP RFC XML serialization (see
	 * http://ifr.sap.com).
	 * </p>
	 * 
	 * <p>
	 * If <code>request</code> is of type java.lang.String, then its
	 * associated value must be an XML string. The string value will be parsed
	 * by a DOM parser. Its XSchema must also conform to the specification for
	 * Serialization of ABAP Data in XML (see http://ifr.sap.com).
	 * </p>
	 * 
	 * <p>
	 * If <code>request</code> is a multi-valued attribute the first value of
	 * attribute <code>request</code> must be of type java.lang.String
	 * containing the name of the RFC, while the second value of the attribute
	 * <code>request</code> must be com.ibm.di.entry.Attribute whose values
	 * contain additional attributes for the SAP RFC parameters as a series of
	 * nested and multi-valued attributes representing the names of the import
	 * and table parameters of the RFC. The names of the parameters must be
	 * encoded according to the rules for Serialization of ABAP Data in XML
	 * (i.e. names will not have characters that could result in badly formed
	 * XML).
	 * </p>
	 * 
	 * <p>
	 * For example Java code for BAPI_USER_GET_DETAIL where USERNAME = SAP*
	 * would be:
	 * </p>
	 * <code>
	 Attribute inputAttrib = new Attribute();<br>
	 Attribute username = new Attribute();<br>
	 Attribute user = new Attribute();<br>
	 Entry entry = new Entry();<br>
	 
	 inputAttrib.addValue("BAPI_USER_GET_DETAIL");<br>
	 inputAttrib.addValue(username);<br>
	 username.addValue(user);<br>
	 user.setName("USERNAME");<br>
	 user.addValue("SAP*");<br>
	 entry.setAttribute(SapR3RfcFCV3.PARAM_INPUT, inputAttrib);<br>
	 Attribute reqType = new Attribute();<br>
	 reqType.addValue(SapR3RfcFCV3.PARAM_VAL_MVA);<br>
	 entry.setAttribute(SapR3RfcFCV3.PARAM_INPUT_TYPE, reqType);<br>
	 </code>
	 * 
	 * 
	 * <p>
	 * On response the Entry will contain the attributes
	 * <code>responseType</code> and <code>response</code>. Attribute
	 * <code>responseType</code> will have a java.lang.String value
	 * corresponding to the input request type.
	 * </p>
	 * 
	 * <p>
	 * If the Entry contains an attribute <code>responseType</code> with value
	 * <code>xmlDomResponse</code>. The value of Attribute
	 * <code>response</code> is an org.w3c.dom.Document containing the RFC
	 * response.
	 * </p>
	 * 
	 * <p>
	 * If the Entry contains an attribute <code>responseType</code> with value
	 * <code>xmlString</code>. The value of Attribute <code>response</code>
	 * is a XML java.lang.String containing the RFC response.
	 * </p>
	 * 
	 * <p>
	 * If the Entry contains an attribute <code>responseType</code> with value
	 * <code>multiValuedAttributes</code>. The value of Attribute
	 * <code>response</code> is a nested and multi-valued Attribute where the
	 * first value is a java.lang.String which has the name of the RFC that was
	 * invoked, the second value contains the results of the RFC as a series of
	 * nested multi-valued attributes.
	 * </p>
	 * 
	 * @param obj
	 *            Must be an Entry object.
	 * @return Entry
	 * @throws SapR3RfcFCException
	 *             thrown when errors occur invoking RFC
	 * @throws SapR3FCParameterException
	 *             thrown when format of Entry on input is incorrect
	 * @see SapR3RfcFCV3#initialize
	 */
	public Object perform(Object obj) throws SapR3RfcFCException,
			SapR3FCParameterException {
		Trace.entrymid(this, "perform");
//		System.out.println("perform");
		RequestResponseType requestType;
		Entry response = null;

		if (!isInitialized()) {
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0006);
			throw new SapR3RfcFCException(msg);
		}
		if (terminated) {
			throw new SapR3RfcFCException(LogMessageHelper.getMsgResource()
					.getMessage(LogMessageHelper.SAPR3_RFCFC_0007));
		}

		requestType = getRequestType(obj);
		Entry entry = (Entry) obj;

		/*
		 * Extract the request
		 */
		if (requestType.compareTo(RequestResponseType.DOMDOC) == 0) {
			debug(LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0046));
			Document reqDoc = (Document) entry.getAttribute(PARAM_INPUT)
					.getValue(0);

			debug(LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0047));

			Document xmlResult = getSapAdapter().sendReceive(getSapClient(),
					reqDoc, getLogProxy());
			response = new Entry();
			response.addAttributeValue(PARAM_OUTPUT_TYPE,
					PARAM_VAL_DOM_DOCUMENT);
			response.addAttributeValue(PARAM_OUTPUT, xmlResult);
		} else if (requestType.compareTo(RequestResponseType.XMLSTRING) == 0) {
			debug(LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0048));

			String reqString = (String) entry.getAttribute(PARAM_INPUT)
					.getValue(0);

			debug(LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0049));

			String xmlResult = getSapAdapter().sendReceive(getSapClient(),
					reqString, getLogProxy());
			response = new Entry();
			response.addAttributeValue(PARAM_OUTPUT_TYPE, PARAM_VAL_STRING);
			response.addAttributeValue(PARAM_OUTPUT, xmlResult);
		} else if (requestType.compareTo(RequestResponseType.MVA) == 0) {
			debug(LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0050));

			response = getSapAdapter().sendReceive(getSapClient(), entry,
					getLogProxy());
			response.addAttributeValue(PARAM_OUTPUT_TYPE, PARAM_VAL_MVA);
		} else {
			// coding error, unexpected type
			throw new IllegalArgumentException();
		}

		Trace.exitmid(this, "perform");
		return response;
	}

	private SapAdapter getSapAdapter() {
		if (sapAdapter == null) {
			sapAdapter = new SapAdapter();
		}

		return sapAdapter;
	}

	private void initJcoConfigProperties() {
		Trace.entrymid(this, "initJcoConfigProperties");
//		System.out.println("init JCO config props ");
		
		int i;

		/*
		 * Get all known JCO parameters. Pitty there isn't some way to get a list
		 * of parameters ...
		 */
		
		for (i = 0; i < PARAM_JCO_CLIENT_OPTIONS.length; i++) {
			String paramVal = (String) super
					.getParam(PARAM_JCO_CLIENT_OPTIONS[i]);
			if (paramVal != null && paramVal.length() > 0) {
				getR3ConnectionProperties().put(
						PARAM_JCO_CLIENT_OPTIONS_PREFIX
								+ PARAM_JCO_CLIENT_OPTIONS[i], paramVal);

				// Don't print out password
				if (PARAM_JCO_CLIENT_OPTIONS[i].equals(PARAM_CONFIG_PASSWORD)) {
					debug(LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_RFCFC_0051,
							new Object[] {
									PARAM_JCO_CLIENT_OPTIONS_PREFIX
											+ PARAM_JCO_CLIENT_OPTIONS[i],
									"******" }));
				} else {
					debug(LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_RFCFC_0051,
							new Object[] {
									PARAM_JCO_CLIENT_OPTIONS_PREFIX
											+ PARAM_JCO_CLIENT_OPTIONS[i],
									paramVal }));
				}
			}
		}
		Trace.exitmid(this, "initJcoConfigProperties");

	}

	public void logError(String msg) {
		if (getLog() != null) {
			getLog().error(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.FunctionInterface#setLog(com.ibm.di.server.Log)
	 */
	public void setLog(Log arg0) {
		super.setLog(arg0);
		logproxy = new LogProxyImpl(arg0);
	}

	private LogProxy getLogProxy() {
		return logproxy;
	}

	/*
	 * (non-Javadoc) @modelguid {03321EBD-C0C1-41D7-B4D9-B67CB520A279}
	 */
	private void initJcoClientConnection() throws IOException {
		// setSapClient(
		// SapClientConnectionFactory.create(
		// SAPCLIENT_POOL_ID,
		// SAPCLIENT_MAX_POOLED_CONNECTIONS,
		// getR3ConnectionProperties()));
		setSapClient(SapClientConnectionFactory
				.create(getR3ConnectionProperties()));
	}

	private SapClientConnection getSapClient() {
		return sapClient;
	}

	private void setSapClient(SapClientConnection client) {
		sapClient = client;
	}

	private boolean isInitialized() {
		return initialized;
	}

	private void setInitialized(boolean b) {
		initialized = b;
	}
	
}
