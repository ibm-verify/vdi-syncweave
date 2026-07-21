/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.aleidocv3;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Trace;
import com.sap.conn.idoc.IDocRepository;
import com.sap.conn.idoc.jco.JCoIDoc;
import com.sap.conn.jco.JCo;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.ext.Environment;

/**
 * <p>
 * The SAP R/3 and ERP ALE IDoc Server Connector.
 * </p>
 * <p>
 * The connector enables external applications, using TDI, to access SAP
 * Intermediate Documents (IDocs) sent from an SAP R/3 or ERP client system. It
 * supports the following TDI Connector Modes: <b>Iterator</b> The connector
 * supports design time schema query via {@link #querySchema}. When parsing is
 * enabled the input attribute mapping must be created manually based on the
 * IDoc message type being processed.
 * </p>
 * <p>
 * The configuration parameters of the connector are described below. <b>IDoc
 * Server SAP Gateway Host</b><br>
 * The SAP Gateway host name or IP address for the R/3 RFC connection. <br>
 * <br>
 * <b>IDoc Server SAP Gateway Service</b><br>
 * The SAP Gateway service name for R/3 RFC connection. Standard naming
 * convention is the string "sapgw" appended with the SAP System Number. i.e. A
 * SAP System with System number "00" would normally have a gateway service of
 * "sapgw00". <br>
 * <br>
 * <b>IDoc Server Program ID</b><br>
 * The SAP JCo Server external program identifier. This is used when configuring
 * the SAP RFC destination to be used by logical system that will represent the
 * TDI IDco Server Connector in SAL ALE distrbution models. <br>
 * <br>
 * <b>IDoc Server Program ID</b><br>
 * The SAP JCo Server external program identifier. This is used when configuring
 * the SAP RFC destination to be used by logical system that will represent the
 * TDI IDco Server Connector in SAL ALE distrbution models. <br>
 * <br>
 * <b>IDoc Server Unicode Connection?</b><br>
 * The SAP JCo Server needs this set when the client SAP R/3 or ERP system
 * requires a unicode RFC connection. <br>
 * <br>
 * <b>IDoc Server Optional Connection Parameters</b><br>
 * The SAP JCo Server optional RFC connection parameters. Delimite with space
 * char. i.e. "jco.server.trace=1 jco.server.sysnr=00" <br>
 * <br>
 * <b>IDoc Server Poll Time</b><br>
 * The Connector waits for incoming IDoc requests to be created on an internal
 * Inbound IDoc queue. This is the period of time between checks on the queue
 * for new Inbound IDoc requests. This time is also used in the TID Management
 * processing when waiting for a particular transaction to be confirmed by the
 * SAP Client and the connectors Iterator mode functionality. <br>
 * <br>
 * <b>IDoc Client Number</b><br>
 * The SAP JCo Client RFC connection parameter that identifies the SAP System
 * client identifier from which IDoc requests are made on the Connector. <br>
 * <br>
 * <b>IDoc Client User</b><br>
 * The SAP JCo Client RFC connection parameter that identifies the SAP User
 * Account used to authenticate the RFC connection. <br>
 * <br>
 * <b>IDoc Client Password</b><br>
 * The SAP JCo Client RFC connection parameter that identifies the SAP User
 * Account's password used to authenticate the RFC connection. <br>
 * <br>
 * <b>IDoc Client Lang</b><br>
 * The SAP JCo Client RFC connection parameter that identifies the RFC
 * connection logon language. <br>
 * <br>
 * <b>IDoc Client Hostname</b><br>
 * The SAP JCo Client RFC connection parameter that identifies the client SAP
 * R/3 or ERP system's hostname or IP address. <br>
 * <br>
 * <b>IDoc Client System Number</b><br>
 * The SAP JCo Client RFC connection parameter that identifies the client SAP
 * R/3 or ERP system number. <br>
 * <br>
 * <b>IDoc Client SAP Gateway Servicer</b><br>
 * The SAP JCo Client RFC connection parameter that identifies the SAP Gateway
 * service name for R/3 RFC connection. Standard naming convention is the string
 * "sapgw" appended with the SAP System Number. i.e. A SAP System with System
 * number "00" would normally have a gateway service of "sapgw00". <br>
 * <br>
 * <b>IDoc Client SAP Gateway Host</b><br>
 * The SAP JCo Client RFC connection parameter that identifies the SAP Gateway
 * host name or IP address for R/3 RFC connection. <br>
 * <br>
 * <b>IDoc Client Max Connections</b><br>
 * The maximum allowed SAP JCo Client RFC connections in the internal JCo Client
 * Connection pool. <br>
 * <br>
 * <b>IDoc Client Optional Connection Parameters</b><br>
 * The SAP JCo Server optional RFC connection parameters. Delimite with space
 * char. i.e. "jco.client.trace=1 jco.client.use_sapgui=1" pool. <br>
 * <br>
 * <b>IDoc As XML Only?</b><br>
 * If set then only one attribute will be provided to represent the IDoc. This
 * attribute's value is an XML representation of the IDoc. pool. <br>
 * <br>
 * <b>Process SAP RFM Requests?</b><br>
 * If set then any Remote Function Module calls made on the JCo Server will be
 * handled. The result is an attribute in the TDI entry who's value is an XML
 * represenation of the RFM invoked. pool. <br>
 * <br>
 * <b>Parse IDoc or RFM XML?</b><br>
 * If set then any if the attributes representing the IDoc or RFM call as XML
 * are available, then they will be parsed with the appropriate TDI XML parser
 * attached. The only parser options are the DOM, SAX and XSLT TDI parsers. <br>
 * <br>
 * <b>Enable JCo Middleware Trace Logging?</b><br>
 * If set then the JCo trace listener will be instantiated and attached to the
 * JCo IDoc Server. The trace messages and their level will be contained within
 * the TDI trace mechanism. <br>
 * <br>
 * <b>JCo Middleware Trace Level</b><br>
 * Sets the minimum JCo trace level allowed to be captured by the TDI trace
 * mechanism. <br>
 * <br>
 * <b>JCo Middleware Trace File Path</b><br>
 * Sets the path where the JCo trace files will be dumped. <br>
 * <br>
 * </p>
 */
public class SapALEIDocConnectorV3 extends Connector implements
		ConnectorInterface {
	// Connector Constants
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String COMPONENT_NAME = "SapALEIDocConnector";

	private static final String VERSION_INFO = "2.0-di11.0.0.1 %I% 20%E%";

	public static final String LINE_SEP = System.getProperty("line.separator");

	private static final String IREPOS_NAME = "SapALEIDocIReposName";

	private static final String IDOC_REPOS_NAME = "SapALEIDocIDocReposName";

	// private static final String CLIENT_POOL_NAME = "ClientPoolNameFor_";
	//
	// private static final int DEFAULT_MAX_CLIENT_CONNECTIONS = 5;

	public static final String UNKNOWN_TID = "UnknownRFMTID";

	// AL Entry Attribute Name Constants
	public static final String ATTR_SCHEMA_NAME = "name";

	public static final String ATTR_SCHEMA_SYNTAX = "syntax";

	public static final String ATTR_SYNTAX_STRING = "java.lang.String";

	public static final String ATTR_SCHEMA_LENGTH = "length";

	public static final String ATTR_LENGTH_STRING = "*";

	public static final String ATTR_IDOC_TID = "idoc.tid";

	public static final String ATTR_IDOC_AS_XML = "idoc.xml";

	public static final String ATTR_ARCKEY = "idoc.ctrl.ArchiveKey"; // java.lang.String
																		// value

	public static final String ATTR_MANDT = "idoc.ctrl.Client"; // java.lang.String
																// value

	public static final String ATTR_CREDAT = "idoc.ctrl.CreationDate"; // java.lang.String
																		// value

	public static final String ATTR_CRETIM = "idoc.ctrl.CreationTime"; // java.lang.String
																		// value

	public static final String ATTR_DIRECT = "idoc.ctrl.Direction"; // java.lang.String
																	// value

	public static final String ATTR_REFMES = "idoc.ctrl.EDIMessage"; // java.lang.String
																		// value

	public static final String ATTR_REFGRP = "idoc.ctrl.EDIMessageGroup"; // java.lang.String
																			// value

	public static final String ATTR_STDMES = "idoc.ctrl.EDIMessageType"; // (java.lang.String
																			// value)

	public static final String ATTR_STD = "idoc.ctrl.EDIStandardFlag"; // (java.lang.String
																		// value)

	public static final String ATTR_STDVRS = "idoc.ctrl.EDIStandardVersion"; // (java.lang.String
																				// value)

	public static final String ATTR_REFINT = "idoc.ctrl.EDITransmissionFile"; // (java.lang.String
																				// value)

	public static final String ATTR_EXPRSS = "idoc.ctrl.ExpressFlag"; // (java.lang.String
																		// value)

	public static final String ATTR_DOCTYP = "idoc.ctrl.IDocCompoundType"; // (java.lang.String
																			// value)

	public static final String ATTR_DOCNUM = "idoc.ctrl.IDocNumber"; // (java.lang.String
																		// value)

	public static final String ATTR_DOCREL = "idoc.ctrl.IDocSAPRelease"; // (java.lang.String
																			// value)

	public static final String ATTR_IDOCTYP = "idoc.ctrl.IDocType"; // (java.lang.String
																	// value)

	public static final String ATTR_CIMTYP = "idoc.ctrl.IDocTypeExtension"; // (java.lang.String
																			// value)

	public static final String ATTR_MESCOD = "idoc.ctrl.MessageCode"; // (java.lang.String
																		// value)

	public static final String ATTR_MESFCT = "idoc.ctrl.MessageFunction"; // (java.lang.String
																			// value)

	public static final String ATTR_MESTYP = "idoc.ctrl.MessageType"; // (java.lang.String
																		// value)

	public static final String ATTR_OUTMOD = "idoc.ctrl.OutputMode"; // (java.lang.String
																		// value)

	public static final String ATTR_RCVSAD = "idoc.ctrl.RecipientAddress"; // (java.lang.String
																			// value)

	public static final String ATTR_RCVLAD = "idoc.ctrl.RecipientLogicalAddress"; // (java.lang.String
																					// value)

	public static final String ATTR_RCVPFC = "idoc.ctrl.RecipientPartnerFunction"; // (java.lang.String
																					// value)

	public static final String ATTR_RCVPRN = "idoc.ctrl.RecipientPartnerNumber"; // (java.lang.String
																					// value)

	public static final String ATTR_RCVPRT = "idoc.ctrl.RecipientPartnerType"; // (java.lang.String
																				// value)

	public static final String ATTR_RCVPOR = "idoc.ctrl.RecipientPort"; // (java.lang.String
																		// value)

	public static final String ATTR_SNDSAD = "idoc.ctrl.SenderAddress"; // (java.lang.String
																		// value)

	public static final String ATTR_SNDLAD = "idoc.ctrl.SenderLogicalAddress"; // (java.lang.String
																				// value)

	public static final String ATTR_SNDPFC = "idoc.ctrl.SenderPartnerFunction"; // (java.lang.String
																				// value)

	public static final String ATTR_SNDPRN = "idoc.ctrl.SenderPartnerNumber"; // (java.lang.String
																				// value)

	public static final String ATTR_SNDPRT = "idoc.ctrl.SenderPartnerType"; // (java.lang.String
																			// value)

	public static final String ATTR_SNDPOR = "idoc.ctrl.SenderPort"; // (java.lang.String
																		// value)

	public static final String ATTR_SERIAL = "idoc.ctrl.Serialization"; // (java.lang.String
																		// value)

	public static final String ATTR_TABNAM = "idoc.ctrl.TableStructureName"; // (java.lang.String
																				// value)

	public static final String ATTR_STATUS = "idoc.ctrl.Status"; // (java.lang.String
																	// value)

	public static final String ATTR_TEST = "idoc.ctrl.TestFlag"; // (java.lang.String
																	// value)

	public static final String ATTR_RFM_AS_XML = "rfm.xml";

	private static final String[] IDOC_ATTRS_LIST = { ATTR_IDOC_TID,
			ATTR_ARCKEY, ATTR_MANDT, ATTR_CREDAT, ATTR_CRETIM, ATTR_DIRECT,
			ATTR_REFMES, ATTR_REFGRP, ATTR_STDMES, ATTR_STD, ATTR_STDVRS,
			ATTR_REFINT, ATTR_EXPRSS, ATTR_DOCTYP, ATTR_DOCNUM, ATTR_DOCREL,
			ATTR_IDOCTYP, ATTR_CIMTYP, ATTR_MESCOD, ATTR_MESFCT, ATTR_MESTYP,
			ATTR_OUTMOD, ATTR_RCVSAD, ATTR_RCVLAD, ATTR_RCVPFC, ATTR_RCVPRN,
			ATTR_RCVPRT, ATTR_RCVPOR, ATTR_SNDSAD, ATTR_SNDLAD, ATTR_SNDPFC,
			ATTR_SNDPRN, ATTR_SNDPRT, ATTR_SNDPOR, ATTR_SERIAL, ATTR_STATUS,
			ATTR_TABNAM, ATTR_TEST, ATTR_IDOC_AS_XML, ATTR_RFM_AS_XML };

	public static final String ATTR_CHILD_SEG_PREFIX = "idoc.child.";

	public static final String ATTR_DESC_SEG_PREFIX = "idoc.descendant.";

	// General Connector Config Param Names.
	private static final String CONFIG_PARAM_SYS_REL = "conn.idoc.out.SystemRelease";

	private static final String CONFIG_PARAM_APP_REL = "conn.idoc.out.ApplicationRelease";

	private static final String CONFIG_PARAM_CONSTR_NAME = "conn.idoc.out.ControlStructName";

	public static final String CONFIG_PARAM_QUEUE_POLE_TIME = "conn.server.poll";

	private static final String CONFIG_PARAM_MAX_CLIENT_CONNECTIONS = "conn.client.max";

	public static final String CONFIG_PARAM_IDOC_XMLONLY = "conn.idoc.xmlonly";

	public static final String CONFIG_PARAM_RFM_XML = "conn.rfm.xml";

	public static final String CONFIG_PARAM_PARSE_IDOC_XML = "conn.idoc.parsexml";

	// Connector Config Param Names for Server RFC connection properties
	private static final String JCO_SERVER_GWHOST = "jco.server.gwhost";

	private static final String JCO_SERVER_GWSERV = "jco.server.gwserv";

	private static final String JCO_SERVER_PROGID = "jco.server.progid";

	private static final String JCO_SERVER_UNICODE = "jco.server.unicode";

	private static final String JCO_SERVER_OPTIONAL = "conn.server.optional";

	// Connector Config Param Names for client RFC connection properties
	private static final String JCO_CLIENT_CLIENT = "jco.client.client";

	private static final String JCO_CLIENT_USER = "jco.client.user";

	private static final String JCO_CLIENT_PASSWD = "jco.client.passwd";

	private static final String JCO_CLIENT_LANG = "jco.client.lang";

	private static final String JCO_CLIENT_ASHOST = "jco.client.ashost";

	private static final String JCO_CLIENT_SYSNR = "jco.client.sysnr";

	private static final String JCO_CLIENT_GWSERV = "jco.client.gwserv";

	private static final String JCO_CLIENT_GWHOST = "jco.client.gwhost";

	private static final String JCO_CLIENT_OPTIONAL = "conn.client.optional";

	// Connector Config Param Names for JCo tracing
	private static final String JCO_TRACE = "conn.idoc.jco.trace";

	private static final String JCO_TRACE_LEVEL = "conn.idoc.jco.tracelevel";

	public static final String JCO_TRACE_PATH = "conn.idoc.jco.tracepath";

	public static final String YES = "1";

	public static final String NO = "0";

	public static final String ON = "1";

	public static final String OFF = "0";

	// Connector variables
	protected SapIDocServerListenerV3 sapALEIdocSrvrListener;

	protected java.util.Properties serverConnProps;

	// protected java.util.Properties serverConnProps;

	private String jcoServerProgId = "NotSpecified";

	// protected IRepository sapALEIRepos;
	protected JCoRepository sapALEIRepos;

	// protected IDoc.Repository sapALEIDocRepos;
	protected IDocRepository sapALEIDocRepos;

	protected SapIDocServerImplV3 sapALEIDocServer;

	private Map tidIDocMap = null;

	private boolean isTerminated = true;
	
	private File destinationFile;
	
	public String destinationName;
	
	boolean processSingleRequest = false;

	public boolean mDebugEnabled = false;

	/**
	 * Default constucter for SapALEIDocConnector object.
	 */
	public SapALEIDocConnectorV3() {
		Trace.entrymin(this, "SapALEIDocConnectorV3");
		setName(COMPONENT_NAME);
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE
		// , ConnectorConfig.SERVER_MODE
		});
		isTerminated = true;
		Trace.exitmin(this, "SapALEIDocConnectorV3");
	}

	/**
	 * Returns the version of the connector based on source repository
	 * variables.
	 * 
	 * @return A String format for the version of this released Connector.
	 */
	public String getVersion() {
		return VERSION_INFO;
	}

	/**
	 * This method is called once after the connector configuration file has
	 * been provided by the caller.
	 * 
	 * @param o
	 *            TDI config object. Not used.
	 * @throws SapALEIDocConnectorException
	 *             When an error happens during class initialization.
	 */
	public void initialize(Object o) throws SapALEIDocConnectorException {
		Trace.entrymin(this, "initialize");
		if (getRawConnectorConfiguration() != null) {
			mDebugEnabled = getRawConnectorConfiguration().getDebug(false);
		}
		try {
			// add server exception listener to be notified about server
			// exceptions
			if (null == sapALEIdocSrvrListener) {
				sapALEIdocSrvrListener = new SapIDocServerListenerV3(this);
			}

			// server properties to log on at the gateway server
			if (null == serverConnProps) {
				serverConnProps = new Properties();
				if (isConnParamValueValid(JCO_SERVER_GWHOST, true))
					serverConnProps.put(JCO_SERVER_GWHOST,
							getParam(JCO_SERVER_GWHOST));
				else {
					Object[] args = new Object[] { JCO_SERVER_GWHOST };
					String error = LogMessageHelper.getMessage(
							LogMessageHelper.SAP_ALEIDOC_0003, args);
					logmsg(error);
					throw new SapALEIDocConnectorException(error);
				}
				if (isConnParamValueValid(JCO_SERVER_GWSERV, true))
					serverConnProps.put(JCO_SERVER_GWSERV,
							getParam(JCO_SERVER_GWSERV));
				else {
					Object[] args = new Object[] { JCO_SERVER_GWSERV };
					String error = LogMessageHelper.getMessage(
							LogMessageHelper.SAP_ALEIDOC_0003, args);
					logmsg(error);
					throw new SapALEIDocConnectorException(error);
				}
				if (isConnParamValueValid(JCO_SERVER_PROGID, true)) {
					jcoServerProgId = getParam(JCO_SERVER_PROGID);
					serverConnProps.put(JCO_SERVER_PROGID, jcoServerProgId);
				} else {
					Object[] args = new Object[] { JCO_SERVER_PROGID };
					String error = LogMessageHelper.getMessage(
							LogMessageHelper.SAP_ALEIDOC_0003, args);
					logmsg(error);
					throw new SapALEIDocConnectorException(error);
				}

				if (isConnParamValueValid(JCO_SERVER_OPTIONAL, false)) {
					String[] props = getParam(JCO_SERVER_OPTIONAL).split(" ");
					for (int i = 0; i < props.length; i++) {
						if ((props[i].length() > 0)
								&& (props[i].indexOf('=') != -1)
								&& (props[i].length() > props[i].indexOf('='))) {
							String key = props[i].substring(0,
									props[i].indexOf('='));
							String value = props[i].substring(props[i]
									.indexOf('=') + 1);
							// There is no check for performed to see if the
							// optional connection
							// parameter is valid. Check the RFC trace file for
							// errors.
							if (key.length() > 0 && value.length() > 0) {
								serverConnProps.put(key, value);
								if (mDebugEnabled) {
									Object[] args = new Object[] { key, value };
									debug(LogMessageHelper.getMessage(
											LogMessageHelper.SAP_ALEIDOC_0004,
											args));
								}
							}
						}
					}
				}
			}
			
			// first create the .jcoServer file before adding client properties
			try {
				destinationName = getDestinationName();
				serverConnProps.setProperty(SapIDocServerImplV3.JCO_SERVER_DEST, destinationName);
				serverConnProps.store(new FileOutputStream(destinationName + ".jcoServer"), destinationName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			// client properties to log on for repository queries
			if (isConnParamValueValid(JCO_CLIENT_CLIENT, true)) {
				String jcoClientClient = getParam(JCO_CLIENT_CLIENT);
				serverConnProps.put(JCO_CLIENT_CLIENT, jcoClientClient);
			}

			if (isConnParamValueValid(JCO_CLIENT_CLIENT, true))
				serverConnProps.put(JCO_CLIENT_CLIENT,
						getParam(JCO_CLIENT_CLIENT));
			else {
				Object[] args = new Object[] { JCO_CLIENT_CLIENT };
				String error = LogMessageHelper.getMessage(
						LogMessageHelper.SAP_ALEIDOC_0003, args);
				logmsg(error);
				throw new SapALEIDocConnectorException(error);
			}
			if (isConnParamValueValid(JCO_CLIENT_USER, true))
				serverConnProps.put(JCO_CLIENT_USER, getParam(JCO_CLIENT_USER));
			else {
				Object[] args = new Object[] { JCO_CLIENT_USER };
				String error = LogMessageHelper.getMessage(
						LogMessageHelper.SAP_ALEIDOC_0003, args);
				logmsg(error);
				throw new SapALEIDocConnectorException(error);
			}
			if (isConnParamValueValid(JCO_CLIENT_PASSWD, true))
				serverConnProps.put(JCO_CLIENT_PASSWD,
						getParam(JCO_CLIENT_PASSWD));
			else {
				Object[] args = new Object[] { JCO_CLIENT_PASSWD };
				String error = LogMessageHelper.getMessage(
						LogMessageHelper.SAP_ALEIDOC_0003, args);
				logmsg(error);
				throw new SapALEIDocConnectorException(error);
			}
			if (isConnParamValueValid(JCO_CLIENT_ASHOST, true))
				serverConnProps.put(JCO_CLIENT_ASHOST,
						getParam(JCO_CLIENT_ASHOST));
			else {
				Object[] args = new Object[] { JCO_CLIENT_ASHOST };
				String error = LogMessageHelper.getMessage(
						LogMessageHelper.SAP_ALEIDOC_0003, args);
				logmsg(error);
				throw new SapALEIDocConnectorException(error);
			}
			if (isConnParamValueValid(JCO_CLIENT_SYSNR, true))
				serverConnProps.put(JCO_CLIENT_SYSNR,
						getParam(JCO_CLIENT_SYSNR));
			else {
				Object[] args = new Object[] { JCO_CLIENT_SYSNR };
				String error = LogMessageHelper.getMessage(
						LogMessageHelper.SAP_ALEIDOC_0003, args);
				logmsg(error);
				throw new SapALEIDocConnectorException(error);
			}
			if (isConnParamValueValid(JCO_CLIENT_LANG, true))
				serverConnProps.put(JCO_CLIENT_LANG, getParam(JCO_CLIENT_LANG));
			else {
				Object[] args = new Object[] { JCO_CLIENT_LANG };
				String error = LogMessageHelper.getMessage(
						LogMessageHelper.SAP_ALEIDOC_0003, args);
				logmsg(error);
				throw new SapALEIDocConnectorException(error);
			}
//			if (isConnParamValueValid(JCO_CLIENT_GWHOST, false))
//				serverConnProps.put(JCO_CLIENT_GWHOST,
//						getParam(JCO_CLIENT_GWHOST));
//			if (isConnParamValueValid(JCO_CLIENT_GWSERV, true))
//				serverConnProps.put(JCO_CLIENT_GWSERV,
//						getParam(JCO_CLIENT_GWSERV));
//			if (isConnParamValueValid(JCO_CLIENT_OPTIONAL, false)) {
//				String[] props = getParam(JCO_CLIENT_OPTIONAL).split(" ");
//				for (int i = 0; i < props.length; i++) {
//					if ((props[i].length() > 0)
//							&& (props[i].indexOf('=') != -1)
//							&& (props[i].length() > props[i].indexOf('='))) {
//						String key = props[i].substring(0,
//								props[i].indexOf('='));
//						String value = props[i]
//								.substring(props[i].indexOf('=') + 1);
//						// There is no check for performed to see if the
//						// optional connection
//						// parameter is valid. Check the RFC trace file for
//						// errors.
//						if (key.length() > 0 && value.length() > 0) {
//							serverConnProps.put(key, value);
//							if (mDebugEnabled) {
//								Object[] args = new Object[] { key, value };
//								debug(LogMessageHelper
//										.getMessage(
//												LogMessageHelper.SAP_ALEIDOC_0004,
//												args));
//							}
//						}
//					}
//				}
//			}

			// create a JCo client pool. The poolname must be unique for SAP
			// system and client.
			/*
			 * JCo3 - no more pools String poolName = CLIENT_POOL_NAME +
			 * getParam(JCO_CLIENT_CLIENT) + getParam(JCO_CLIENT_ASHOST); if
			 * (null == JCO.getClientPoolManager().getPool(poolName)) { Integer
			 * maxConns = new Integer(
			 * getParam(CONFIG_PARAM_MAX_CLIENT_CONNECTIONS)); if
			 * (maxConns.intValue() > 0) JCO.addClientPool(poolName,
			 * maxConns.intValue(), serverConnProps); else
			 * JCO.addClientPool(poolName, DEFAULT_MAX_CLIENT_CONNECTIONS,
			 * serverConnProps); }
			 */

			// create a JCo repository that will be used for querying
			// meta data for standard function requests
			if (null == sapALEIRepos)
				// sapALEIRepos = JCO.createRepository(IREPOS_NAME, poolName);
				sapALEIRepos = JCo.getRepository(IREPOS_NAME);

			// create an IDoc repository that will be used for querying
			// IDoc meta data
			if (null == sapALEIDocRepos)
				// sapALEIDocRepos = JCoIDoc.createRepository(IDOC_REPOS_NAME,
				// poolName);
				sapALEIDocRepos = JCoIDoc.getIDocFactory().getRepository(
						IDOC_REPOS_NAME);

			// create a JCoIDoc.Server instance
			if (null == sapALEIDocServer) {
				sapALEIDocServer = new SapIDocServerImplV3(this,
						serverConnProps, sapALEIRepos, sapALEIDocRepos);
				if ((isConnParamValueValid(JCO_TRACE, true))
						&& (getParam(JCO_TRACE).equalsIgnoreCase(YES))) {
					sapALEIDocServer.setTrace(true);
				}

				// server listener
				sapALEIDocServer.getServer().addServerErrorListener(
						sapALEIdocSrvrListener);
				sapALEIDocServer.getServer().addServerExceptionListener(
						sapALEIdocSrvrListener);
				sapALEIDocServer.getServer().addServerStateChangedListener(
						sapALEIdocSrvrListener);

			}
			if (null == tidIDocMap)
				tidIDocMap = Collections.synchronizedMap(new HashMap());
			
//			Environment.unregisterDestinationDataProvider(sapALEIDocServer.getDestinationProvider());
		} catch (java.lang.Exception ex) {
			Object[] args = new Object[] { ex.getMessage() };
			String msg = LogMessageHelper.getMessage(
					LogMessageHelper.SAP_ALEIDOC_0002, args);
			logmsg(msg);
			if (mDebugEnabled) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				debug(sw.toString());
			}
			SapALEIDocConnectorException eToThrow = new SapALEIDocConnectorException(
					ex.getMessage());
			throw eToThrow;
		}
		Trace.exitmin(this, "initialize");
	}

	/**
	 * Checks if a Connector configuration parameter is valid for use.
	 * 
	 * @param key
	 *            - The key for the Connector configuration parameter being
	 *            checked.
	 * @param mandatory
	 *            - flag indicating if the Connector configuration parameter is
	 *            obligatory.
	 * @return boolean result on the validity of the Connector configuration
	 *         parameter.
	 */
	public boolean isConnParamValueValid(String key, boolean mandatory) {
		String value = getParam(key);
		if (mDebugEnabled) {
			Object[] args = null;
			if (key.equals(JCO_CLIENT_PASSWD))
				args = new Object[] { key, "secret:)" };
			else
				args = new Object[] { key, value };
			debug(LogMessageHelper.getMessage(
					LogMessageHelper.SAP_ALEIDOC_0005, args));
		}
		if (mandatory) {
			if (null == value || value.length() < 1)
				return false;
		} else {
			if (null == value)
				return false;
		}
		return true;
	}

	/**
	 * Checks if the Inbound IDoc queue contains a TID which is yet to be
	 * confirmed.
	 * 
	 * @return boolean result on the availability of a TID that has yet to be
	 *         confirmed.
	 */
	private boolean isTIDAvailForProcessing() {
		if (getTidIDocMap().size() > 0) {
			Set procKeys = getTidIDocMap().keySet();
			Iterator tidProcIter = procKeys.iterator();
			while (tidProcIter.hasNext()) {
				String tid = (String) tidProcIter.next();
				Object[] args = new Object[] { tid };
				if (mDebugEnabled) {
					debug(LogMessageHelper.getMessage(
							LogMessageHelper.SAP_ALEIDOC_0006, args));
				}
				TIDManager tidMgr = (TIDManager) getTidIDocMap().get(tid);
				if (tidMgr == null) {
					String msg = LogMessageHelper.getMessage(
							LogMessageHelper.SAP_ALEIDOC_0007, args);
					logmsg(msg);
					continue;
					// possibly should throw exception here. For this release
					// skip the TIDManager
					// throw new SapALEIDocConnectorException(msg);
				}
				if (tidMgr.getTidStatus() == TIDManager.TID_STAT_PROCESSING
						|| tidMgr.getTidStatus() == TIDManager.TID_STAT_INITIAL) {
					logmsg(LogMessageHelper.getMessage(
							LogMessageHelper.SAP_ALEIDOC_0008, args));
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns a TDI entry that represents either an SAP client IDoc or RFM
	 * request. The entries are pulled from TIDManager objects sitting in the
	 * Inbound IDoc queue.
	 * 
	 * @return TDI entry or null.
	 * @throws SapALEIDocConnectorException
	 *             When an error occurs.
	 */
	public Entry getNextEntry() throws SapALEIDocConnectorException {
		Trace.entrymin(this, "getNextEntry");
		ConnectorConfig currentConfig = (ConnectorConfig) this
				.getConfiguration();
		if (currentConfig.getMode().equals(ConnectorConfig.ITERATOR_MODE)) {
			// if only processing a single request and that request has already
			// been processed
			// then this flag will have already been set to return null.
			if (processSingleRequest) {
				Trace.exitmin(this, "getNextEntry");
				return null;
			}
			// Pole time in milli-secs
			long pollTime = 60000;
			if (isConnParamValueValid(CONFIG_PARAM_QUEUE_POLE_TIME, true))
				pollTime = new Long(getParam(CONFIG_PARAM_QUEUE_POLE_TIME))
						.longValue() * 1000;
			// If the given in poll time was 0 or less then only except one IDoc
			// request.
			// This is how to configure standard iterator mode which will fall
			// out with null
			// Entry eventually allowing other iterators to be processed.
			if (pollTime < 1) {
				logmsg(LogMessageHelper
						.getMessage(LogMessageHelper.SAP_ALEIDOC_0009));
				processSingleRequest = true;
				pollTime = 60000;
			}
			while (!isTIDAvailForProcessing()) {
				if (mDebugEnabled)
					debug(LogMessageHelper
							.getMessage(LogMessageHelper.SAP_ALEIDOC_0010));
				try {
					Thread.sleep(pollTime);
				} catch (InterruptedException e) {
					String error = LogMessageHelper
							.getMessage(LogMessageHelper.SAP_ALEIDOC_0011);
					logmsg(error);
					throw new SapALEIDocConnectorException(error);
				}
			}
		}
		// First finish processing TIDManager's that are in the processing
		// state.
		// Should only ever be one at any one time.
		Set procKeys = getTidIDocMap().keySet();
		Iterator tidProcIter = procKeys.iterator();
		while (tidProcIter.hasNext()) {
			String tid = (String) tidProcIter.next();
			Object[] args = new Object[] { tid };
			TIDManager tidMgr = (TIDManager) getTidIDocMap().get(tid);
			if (tidMgr == null) {
				String error = LogMessageHelper.getMessage(
						LogMessageHelper.SAP_ALEIDOC_0007, args);
				logmsg(error);
				throw new SapALEIDocConnectorException(error);
			}
			if (tidMgr.getTidStatus() != TIDManager.TID_STAT_PROCESSING) {
				continue;
			}
			Entry jcoIDocEntry = (Entry) tidMgr.getIdocEntries().get(0);
			if (null == jcoIDocEntry) {
				logmsg(LogMessageHelper
						.getMessage(LogMessageHelper.SAP_ALEIDOC_0012));
				tidMgr.getIdocEntries().remove(0);
			} else {
				// If no IDoc entries left in list after this one, move
				// TIDManager to confirmed.
				if (tidMgr.getIdocEntries().size() == 1) {
					logmsg(LogMessageHelper.getMessage(
							LogMessageHelper.SAP_ALEIDOC_0015, args));
					tidMgr.setTidStatus(TIDManager.TID_STAT_CONFIRM);
					if (tidMgr.getTid().equals(UNKNOWN_TID)) {
						logmsg(LogMessageHelper
								.getMessage(LogMessageHelper.SAP_ALEIDOC_0013));
						// This is a function call Entry that did not come with
						// a TID.
						// Therefore onConfirmTID() will not be called to remove
						// the
						// hashmap. Remove it here now.
						getTidIDocMap().remove(tidMgr.getTid());
					}
				}
				// Process the first IDoc entry by removing it from the TID
				// Manager list then return it.
				tidMgr.getIdocEntries().remove(0);
				Trace.exitmin(this, "getNextEntry");
				if (isConnParamValueValid(
						SapALEIDocConnectorV3.CONFIG_PARAM_PARSE_IDOC_XML, true)
						&& (getParam(SapALEIDocConnectorV3.CONFIG_PARAM_PARSE_IDOC_XML)
								.equalsIgnoreCase(SapALEIDocConnectorV3.YES))) {
					return parseEntry(jcoIDocEntry);
				} else {
					if (mDebugEnabled)
						debug(LogMessageHelper
								.getMessage(LogMessageHelper.SAP_ALEIDOC_0014));
					return jcoIDocEntry;
				}
			}
		}
		// If finished processing TIDManager's that are in the processing state,
		// Get the next one that is still in the initial state.
		Set initKeys = getTidIDocMap().keySet();
		Iterator tidInitIter = initKeys.iterator();
		while (tidInitIter.hasNext()) {
			String tid = (String) tidInitIter.next();
			Object[] args = new Object[] { tid };
			TIDManager tidMgr = (TIDManager) getTidIDocMap().get(tid);
			if (tidMgr == null) {
				String error = LogMessageHelper.getMessage(
						LogMessageHelper.SAP_ALEIDOC_0007, args);
				logmsg(error);
				throw new SapALEIDocConnectorException(error);
			}
			if (tidMgr.getTidStatus() != TIDManager.TID_STAT_INITIAL) {
				continue;
			}
			Entry jcoIDocEntry = (Entry) tidMgr.getIdocEntries().get(0);
			if (null == jcoIDocEntry) {
				logmsg(LogMessageHelper
						.getMessage(LogMessageHelper.SAP_ALEIDOC_0012));
				tidMgr.getIdocEntries().remove(0);
			} else {
				if (tidMgr.getIdocEntries().size() == 1) {
					logmsg(LogMessageHelper.getMessage(
							LogMessageHelper.SAP_ALEIDOC_0015, args));
					tidMgr.setTidStatus(TIDManager.TID_STAT_CONFIRM);
					if (tidMgr.getTid().equals(UNKNOWN_TID)) {
						logmsg(LogMessageHelper
								.getMessage(LogMessageHelper.SAP_ALEIDOC_0013));
						// This is a function call Entry that did not come with
						// a TID.
						// Therefore onConfirmTID() will not be called to remove
						// the
						// hashmap. Remove it here now.
						getTidIDocMap().remove(tidMgr.getTid());
					}
				} else {
					if (tidMgr.getTidStatus() != TIDManager.TID_STAT_PROCESSING) {
						tidMgr.setTidStatus(TIDManager.TID_STAT_PROCESSING);
					}
				}
				// Process the first IDoc entry by removing it from the TID
				// Manager
				// list, set the TIDManager status to processing, then return
				// it.
				tidMgr.getIdocEntries().remove(0);
				Trace.exitmin(this, "getNextEntry");
				if (isConnParamValueValid(
						SapALEIDocConnectorV3.CONFIG_PARAM_PARSE_IDOC_XML, true)
						&& (getParam(SapALEIDocConnectorV3.CONFIG_PARAM_PARSE_IDOC_XML)
								.equalsIgnoreCase(SapALEIDocConnectorV3.YES))) {
					return parseEntry(jcoIDocEntry);
				} else {
					if (mDebugEnabled)
						debug(LogMessageHelper
								.getMessage(LogMessageHelper.SAP_ALEIDOC_0014));
					return jcoIDocEntry;
				}
			}
		}
		Trace.exitmin(this, "getNextEntry");
		if (currentConfig.getMode().equals(ConnectorConfig.ITERATOR_MODE)
				&& !processSingleRequest) {
			String error = LogMessageHelper
					.getMessage(LogMessageHelper.SAP_ALEIDOC_0016);
			logmsg(error);
			throw new SapALEIDocConnectorException(error);
		} else {
			return null;
		}
	}

	/**
	 * If called attempts to initalize an attached parser and parse the XML
	 * value inside the provided TDI entry. The XML value represents either an
	 * SAP IDoc or SAP RFM.
	 * 
	 * @param jcoIDocEntry
	 *            - The TDI Entry that contains the complete IDoc or RFM XML as
	 *            a TDI attribute with a string value.
	 * @return
	 */
	private Entry parseEntry(Entry jcoIDocEntry) {
		String xmlStr = null;
		if (null != jcoIDocEntry.getAttribute(ATTR_IDOC_AS_XML))
			xmlStr = jcoIDocEntry.getAttribute(ATTR_IDOC_AS_XML).getValue();
		if (null == xmlStr || xmlStr.length() < 1)
			xmlStr = jcoIDocEntry.getAttribute(ATTR_RFM_AS_XML).getValue();
		if (null == xmlStr || xmlStr.length() < 1) {
			logmsg(LogMessageHelper
					.getMessage(LogMessageHelper.SAP_ALEIDOC_0017));
			return jcoIDocEntry;
		}
		InputStream xmlRequestInputStream = new BufferedInputStream(
				new ByteArrayInputStream(xmlStr.getBytes()));
		try {
			initParser(xmlRequestInputStream, null);
			if (null != getParser()) {
				if (mDebugEnabled)
					debug(LogMessageHelper
							.getMessage(LogMessageHelper.SAP_ALEIDOC_0018));
				Entry parsedIDoc = getParser().readEntry();
				if (null != parsedIDoc)
					return parsedIDoc;
				else {
					logmsg(LogMessageHelper
							.getMessage(LogMessageHelper.SAP_ALEIDOC_0019));
					return jcoIDocEntry;
				}
			} else {
				if (mDebugEnabled)
					debug(LogMessageHelper
							.getMessage(LogMessageHelper.SAP_ALEIDOC_0014));
				return jcoIDocEntry;
			}
		} catch (Exception e) {
			logmsg(LogMessageHelper
					.getMessage(LogMessageHelper.SAP_ALEIDOC_0020));
			if (mDebugEnabled) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				debug(sw.toString());
			}
			return jcoIDocEntry;
		}
	}

	/**
	 * This method is to populate the available connector variables for input
	 * and output mapping into AL work entry attributes.
	 * 
	 * @param o
	 *            TDI schema object. Not used.
	 * @throws SapALEIDocConnectorException
	 *             When an error occurs.
	 */
	public Object querySchema(Object o) throws SapALEIDocConnectorException {
		Trace.entrymin(this, "querySchema");
		List result = new Vector();
		try {
			boolean idocXMLOnly = true;
			if ((isConnParamValueValid(
					SapALEIDocConnectorV3.CONFIG_PARAM_IDOC_XMLONLY, true))
					&& (getParam(SapALEIDocConnectorV3.CONFIG_PARAM_IDOC_XMLONLY)
							.equalsIgnoreCase(SapALEIDocConnectorV3.NO))) {
				idocXMLOnly = false;
			}
			// schema attributes that form the members of the IDoc.
			for (int ind = 0; ind < IDOC_ATTRS_LIST.length; ind++) {
				if (SapALEIDocConnectorV3.IDOC_ATTRS_LIST[ind] == SapALEIDocConnectorV3.ATTR_RFM_AS_XML) {
					if (isConnParamValueValid(
							SapALEIDocConnectorV3.CONFIG_PARAM_RFM_XML, true)
							&& (getParam(SapALEIDocConnectorV3.CONFIG_PARAM_RFM_XML)
									.equalsIgnoreCase(SapALEIDocConnectorV3.YES))) {
						Entry e1 = new Entry();
						e1.addAttributeValue(
								SapALEIDocConnectorV3.ATTR_SCHEMA_NAME,
								SapALEIDocConnectorV3.IDOC_ATTRS_LIST[ind]);
						e1.addAttributeValue(
								SapALEIDocConnectorV3.ATTR_SCHEMA_SYNTAX,
								SapALEIDocConnectorV3.ATTR_SYNTAX_STRING);
						e1.addAttributeValue(
								SapALEIDocConnectorV3.ATTR_SCHEMA_LENGTH,
								SapALEIDocConnectorV3.ATTR_LENGTH_STRING);
						result.add(e1);
					}
				} else if (SapALEIDocConnectorV3.IDOC_ATTRS_LIST[ind] != SapALEIDocConnectorV3.ATTR_IDOC_AS_XML) {
					if (!idocXMLOnly) {
						Entry e1 = new Entry();
						e1.addAttributeValue(
								SapALEIDocConnectorV3.ATTR_SCHEMA_NAME,
								SapALEIDocConnectorV3.IDOC_ATTRS_LIST[ind]);
						e1.addAttributeValue(
								SapALEIDocConnectorV3.ATTR_SCHEMA_SYNTAX,
								SapALEIDocConnectorV3.ATTR_SYNTAX_STRING);
						e1.addAttributeValue(
								SapALEIDocConnectorV3.ATTR_SCHEMA_LENGTH,
								SapALEIDocConnectorV3.ATTR_LENGTH_STRING);
						result.add(e1);
					}
				} else {
					Entry e1 = new Entry();
					e1.addAttributeValue(
							SapALEIDocConnectorV3.ATTR_SCHEMA_NAME,
							SapALEIDocConnectorV3.IDOC_ATTRS_LIST[ind]);
					e1.addAttributeValue(
							SapALEIDocConnectorV3.ATTR_SCHEMA_SYNTAX,
							SapALEIDocConnectorV3.ATTR_SYNTAX_STRING);
					e1.addAttributeValue(
							SapALEIDocConnectorV3.ATTR_SCHEMA_LENGTH,
							SapALEIDocConnectorV3.ATTR_LENGTH_STRING);
					result.add(e1);
				}
			}
		} catch (Exception x) {
			Object[] args = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMessage(
					LogMessageHelper.SAP_ALEIDOC_0021, args);
			logmsg(msg);
			if (mDebugEnabled) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				x.printStackTrace(pw);
				debug(sw.toString());
			}
		}
		Trace.exitmin(this, "querySchema");
		return result;
	}

	/**
	 * This method is to called for both Iterator and Server modes. Normally
	 * this method would block until there was an IDoc to process. This
	 * connector has a non-standard iterator mode where getNextEntry() will
	 * block and not return null to mimick an asynchronous Server mode.
	 * 
	 * @throws SapALEIDocConnectorException
	 *             When an error occurs.
	 */
	public void selectEntries() throws SapALEIDocConnectorException {
		Trace.entrymin(this, "selectEntries");
		if (isTerminated) {
			sapALEIDocServer.start();
			isTerminated = false;
			logmsg(LogMessageHelper
					.getMessage(LogMessageHelper.SAP_ALEIDOC_0001));
		}
		if (isTerminated) {
			logmsg(LogMessageHelper
					.getMessage(LogMessageHelper.SAP_ALEIDOC_0022));
			throw new SapALEIDocConnectorException(
					LogMessageHelper
							.getMessage(LogMessageHelper.SAP_ALEIDOC_0016));
		}
		Trace.exitmin(this, "selectEntries");
	}

	/**
	 * This method is to called for Server modes. This method blocks until there
	 * is an IDoc to process. If the IDoc Server can't be started then this
	 * method will return null.
	 * 
	 * @return An instance of this connector for a seperate thread in the AL.
	 * @throws SapALEIDocConnectorException
	 *             When an error occurs.
	 */
	public ConnectorInterface getNextClient()
			throws SapALEIDocConnectorException {
		Trace.entrymin(this, "getNextClient");
		if (isTerminated) {
			sapALEIDocServer.start();
			isTerminated = false;
			logmsg(LogMessageHelper
					.getMessage(LogMessageHelper.SAP_ALEIDOC_0001));
		}
		if (isTerminated) {
			logmsg(LogMessageHelper
					.getMessage(LogMessageHelper.SAP_ALEIDOC_0022));
			Trace.exitmin(this, "getNextClient");
			return null;
		}
		ConnectorConfig currentConfig = (ConnectorConfig) getConfiguration();
		if (currentConfig.getMode().equals(ConnectorConfig.SERVER_MODE)) {
			// Pole time in milli-secs
			long pollTime = 5000;
			if (isConnParamValueValid(CONFIG_PARAM_QUEUE_POLE_TIME, true))
				pollTime = new Long(getParam(CONFIG_PARAM_QUEUE_POLE_TIME))
						.longValue() * 1000;
			if (pollTime < 1) {
				pollTime = 5000;
			}
			while (!isTIDAvailForProcessing()) {
				if (mDebugEnabled)
					debug(LogMessageHelper
							.getMessage(LogMessageHelper.SAP_ALEIDOC_0023));
				try {
					Thread.sleep(pollTime);
				} catch (InterruptedException e) {
					String error = LogMessageHelper
							.getMessage(LogMessageHelper.SAP_ALEIDOC_0011);
					logmsg(error);
					throw new SapALEIDocConnectorException(error);
				}
			}
		}
		Trace.exitmin(this, "getNextClient");
		return this;
	}

	/**
	 * This method is to called for Server modes. This method would normally be
	 * used to build and send a response to the client. As the IDoc
	 * communication is asynchronous this method does nothing for this release
	 * of the Connector.
	 */
	public void replyEntry(Entry requestResult) {
		Trace.entrymin(this, "getNextClient");
		/*
		 * Since Connectors in Server mode handle client requests which require
		 * a response, the AssemblyLine will call the �replyEntry(...)�
		 * Connector method at the end of the AssemblyLine. Use this method to
		 * place your code that returns response to the client. If the SAP
		 * system who is the IDoc client required a response, then this method
		 * would be required. As the client is sending a asynchronise request,
		 * no response is required.
		 */
		Trace.exitmin(this, "getNextClient");
	}

	/**
	 * This method is to called for all modes. This method is used to stop the
	 * IDoc Server.
	 */
	public void terminateServer() {
		Trace.entrymin(this, "terminateServer");
		if (sapALEIDocServer != null)
			sapALEIDocServer.stop();
		isTerminated = true;
		Trace.exitmin(this, "terminateServer");
	}

	/**
	 * This method is to called for all modes. This method is used to stop the
	 * IDoc Server. Stopping the IDoc Server affects the TID management cycle so
	 * this method does nothing for this release of the Connector.
	 */
	public void terminate() throws SapALEIDocConnectorException {
		Trace.entrymin(this, "terminate");
		// Have to call suspend to be able to restart the Server. Commented out
		// as suspending the Server messes with the TID management and prevents
		// onConfirmTID() from being invoked.
		// sapALEIDocServer.suspend();
		// isTerminated = true;
		logmsg("shutting down SAP/R3 connection");
		if (sapALEIDocServer != null) {
			sapALEIDocServer.stop();
			sapALEIDocServer.unregister();
		}

		// Delete the temporary .jcoServer file
		if (destinationFile.exists()) {
			if (!destinationFile.delete()) {
				logmsg("Unable to delete temporary .jcoServer file: " + destinationFile.getAbsolutePath());
			} 
		}
		Trace.exitmin(this, "terminate");
	}

	/**
	 * This method is to called for all modes. This method is used to stop and
	 * start the IDoc Server. If the IDoc server is stopped it can't be
	 * restarted so this method suspends then restarts the IDoc Server.
	 */
	public void reconnect() throws SapALEIDocConnectorException {
		Trace.entrymin(this, "reconnect");
		/*
		 * Restart the IDoc server
		 */
		if (!isTerminated) {
			sapALEIDocServer.suspend();
			isTerminated = true;
		}
		if (isTerminated) {
			sapALEIDocServer.start();
			isTerminated = false;
		}
		Trace.exitmin(this, "reconnect");
	}

	/**
	 * @return Returns the IDoc Server listener object.
	 */
	public SapIDocServerListenerV3 getSapALEIdocApp() {
		return sapALEIdocSrvrListener;
	}

	/**
	 * @return
	 * @return Returns the IDoc Server IDoc repository object.
	 */
	public IDocRepository getSapALEIDocRepos() {
		return sapALEIDocRepos;
	}

	/**
	 * @return Returns the IDoc Server object
	 */
	public SapIDocServerImplV3 getSapALEIDocServer() {
		return sapALEIDocServer;
	}

	/**
	 * @return Returns the IDoc Server JCo repository object.
	 */
	public JCoRepository getSapALEIRepos() {
		return sapALEIRepos;
	}

	/**
	 * @return Returns the IDoc Server RFC Connection properties.
	 */
	public java.util.Properties getServerConnProps() {
		return serverConnProps;
	}

	/**
	 * @return Returns the IDoc Server Inbound IDoc request queue.
	 */
	public Map getTidIDocMap() {
		return tidIDocMap;
	}

	/**
	 * @return Returns the IDoc Server program ID. Required when configuring the
	 *         RFC Destination on the SAP Client.
	 */
	public String getJcoServerProgId() {
		return jcoServerProgId;
	}
	
	/**
	 * @return Returns a temporary java.io.File to be used to save the jcoServer properties
	 *         and which is deleted on exit.
	 * @throws IOException 
	 */
	public String getDestinationName() throws IOException {
		String destinationName = File.createTempFile("SAPALE_", "").getName();
		destinationFile = new File(destinationName + ".jcoServer");
		destinationFile.deleteOnExit();
		return destinationName; 
	}
}
