/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.connector.HTTPServerConnector;
import com.ibm.di.connector.JDBCConnector;
import com.ibm.di.connector.maximo.core.AbstractMxConnMode;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.parser.HTTPParser;
import com.ibm.di.parser.xml.XMLParser2;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;
import com.ibm.icu.text.SimpleDateFormat;

/**
 * The Tpae IF Change Detection Connector listens on specified TCP port for HTTP
 * requests. This connector returns hierarchical entries and supports only
 * Server mode. The connector uses internally {@link HTTPServerConnector} and
 * {@link XMLParser2} for parsing the received XML representation of the actual
 * MBO.
 */
public class TpaeIFCDConnector extends Connector implements ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Name of Maximo table holding information about stalled messages.
	 * <p>
	 * <b><Note</b>: This name is internal and might be changed in future
	 * versions. The connector has been tested with Maximo Asset Management 7.1.
	 */
	private static final String ERROR_TABLE = "MAXINTERROR";
	
	/**
	 * Minimum value limit for errorCheckInterval parameter as said in tdi.xml
	 */
	private static final long MINVALUE = 0;
	
	/**
	 * Maximum value limit for errorCheckInterval parameter as said in tdi.xml
	 */
	private static final long MAXVALUE = 999999;

	/**
	 * Name of 'Error check interval' parameter.
	 */
	private static final String PARAM_CHECK_INTERVAL = "errorCheckInterval";

	/**
	 * Name of 'External system' parameter.
	 */
	private static final String PARAM_EXT_SYSTEM = "extSystem";

	/**
	 * Name of 'Action on error' parameter.
	 */
	private static final String PARAM_ACTION = "action";

	/**
	 * Name of 'None' action.
	 */
	private static final String ACTION_NONE = "NONE";

	/**
	 * Name of 'Retry' action.
	 */
	private static final String ACTION_RETRY = "RETRY";

	/**
	 * Name of 'Delete' action.
	 */
	private static final String ACTION_DELETE = "DELETE";

	/**
	 * Name of column holding the unique error id.
	 */
	private static final String COLUMN_ERRORID = "MAXINTERRORID";

	/**
	 * Name of column holding the message's status.
	 */
	private static final String COLUMN_STATUS = "STATUS";

	/**
	 * Name of column holding the message's delete flag.
	 */
	private static final String COLUMN_DELETEFLAG = "DELETEFLAG";

	/**
	 * Value for STATUS column. Indicate that a message is flagged with ERROR
	 * and cannot be processed by the queue.
	 */
	private static final String STATUS_HOLD = "HOLD";

	/**
	 * Value for STATUS column. Indicates that the IF server will try for
	 * predefined number of times to reprocess a message. If the reprocessing
	 * fails the message will be marked as {@link TpaeIFCDConnector#STATUS_HOLD}
	 * .
	 */
	private static final String STATUS_RETRY = "RETRY";

	/**
	 * Name of column holding the External system's name.
	 */
	private static final String COLUMN_EXT_SYSTEM = "EXTSYSNAME";

	/**
	 * Name of column holding the External system's name.
	 */
	private static final String COLUMN_MSGID = "MESSAGEID";

	/**
	 * Formatter for dates in this format: '2010-09-09 13:41:35.736'
	 */
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "tpaeifcdconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash resHash = new ResourceHash(PROPERTIES_FILE);

	// Fields used by the server instance, which waits for clients

	/**
	 * Flag indicating whether the Connector is terminating.
	 */
	private AtomicBoolean terminationRequested = new AtomicBoolean(false);

	/**
	 * An instance of the HTTP Server Connector, that is listening for HTTP
	 * change notifications from Maximo.
	 */
	private HTTPServerConnector httpServer = null;

	/**
	 * An instance of the JDBC Connector, that checks the MAXINTERROR table for
	 * stalled messages send to us.
	 */
	private JDBCConnector jdbcConnector = null;

	/**
	 * An Timer instance used to schedule the checks of the MAXINTERROR table.
	 */
	private Timer timer = null;

	/**
	 * Minimum number of seconds before we check the MAXINTERROR table for
	 * stalled messages.
	 */
	private long errorCheckInterval = 0;

	/**
	 * Flag indicating whether the connector will delete stalled messages.
	 */
	private boolean deleteHoldedMessages = false;

	/**
	 * Flag indicating whether the connector will try to reprocess stalled
	 * messages.
	 */
	private boolean retryHoldedMessages = false;

	// Fields used when servicing a particular client

	/**
	 * An instance of the Connector, which waits for clients.
	 */
	private TpaeIFCDConnector serverConnector = null;

	/**
	 * An instance of the HTTP Server Connector, which handles a particular
	 * client.
	 */
	private HTTPServerConnector httpClientSession = null;

	/**
	 * {@link XMLParser2} instance used to parse the received XML.
	 */
	private XMLParser2 xmlParser;

	/**
	 * The name of the Connector
	 */
	private static final String myName = "Tpae IF Change Detection Connector";
	
	/**
	 * Name of 'jdbc URL' parameter.
	 */
	private static final String PARAM_JDBC_SOURCE = "jdbcSource";

	/**
	 * Name of 'jdbc driver' parameter.
	 */
	private static final String PARAM_JDBC_DRIVER = "jdbcDriver";
	
	/**
	 * Label of 'jdbc URL' parameter.
	 */
	private static final String PARAM_JDBC_URL = "jdbc url";

	/**
	 * Default constructor.
	 */
	public TpaeIFCDConnector() {
		setName(myName);
		setModes(new String[] { ConnectorConfig.SERVER_MODE });
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(Object obj) throws Exception {
		Trace.entrymin(this, "initialize", obj);

		String jdbcUrl = getParam(PARAM_JDBC_SOURCE);
		String jdbcDriver = getParam(PARAM_JDBC_DRIVER);

		if (jdbcUrl == null || jdbcUrl.length() == 0) {
			throw new Exception(resHash.getString("REQUIRED.PARAMETER.NOT.SET", PARAM_JDBC_URL));
		}

		if (jdbcDriver == null || jdbcDriver.length() == 0) {
			throw new Exception(resHash.getString("REQUIRED.PARAMETER.NOT.SET", PARAM_JDBC_DRIVER));
		}

		super.initialize(obj);

		terminationRequested.set(false);

		if (obj instanceof HTTPServerConnector) {

			// service a particular client
			httpClientSession = (HTTPServerConnector) obj;

		} else {
			// Run the HTTP server
			httpServer = new HTTPServerConnector();
			httpServer.setConfiguration(getConfiguration());
			httpServer.setParam(HTTPServerConnector.PARAMETER_TCP_DATA_AS_PROPS, "true");
			httpServer.setParam(HTTPServerConnector.PARAMETER_HEADERS_AS_PROPS, "true");
			httpServer.setRSInterface(getRSInterface());
			httpServer.setDebugMode(debugMode());
			httpServer.setName(getName());
			httpServer.setLog(getLog());
			httpServer.initialize(null);

			String str = getParam(PARAM_CHECK_INTERVAL);
			if (str != null) {
				// Parse and turn into milliseconds for use with timer later
				try {
					errorCheckInterval = Long.parseLong(str) * 1000;
				} catch (NumberFormatException nfe) {
					throw new Exception(resHash.getString("ERRORCHECK.PARAMETER.INVALID", PARAM_CHECK_INTERVAL));
				}
				if(!((errorCheckInterval >= MINVALUE)&&((errorCheckInterval/1000) <= MAXVALUE ))){
					throw new Exception(resHash.getString("ERRORCHECK.PARAMETER.EXCEEDED.LIMITS", new Object[]{PARAM_CHECK_INTERVAL, MINVALUE, MAXVALUE}));
				}
			}

			if (errorCheckInterval > 0) {
				// Initialize JDBC Connector for reading(writing) to
				// MAXINTERROR table
				jdbcConnector = new JDBCConnector();
				jdbcConnector.setConfiguration(getConfiguration());
				jdbcConnector.setCommitMode("After every database operation (Including Select)");

				// Interested only in messages with status 'HOLD' because they
				// block the sequential output queue
				String selectSQL = "SELECT * FROM " + getParam("jdbcSchema") + "." + ERROR_TABLE + " WHERE " + COLUMN_STATUS + "='"
						+ STATUS_HOLD + "'";

				// If we have external system specified add additional condition
				str = getParam(PARAM_EXT_SYSTEM);
				if (str != null && str.trim().length() > 0) {
					StringBuilder extSysCondition = new StringBuilder(" AND " + COLUMN_EXT_SYSTEM + " IN " + "(");
					for (String extSys : str.split(",")) {
						extSysCondition.append("'" + extSys.trim() + "',");
					}
					// remove last comma
					extSysCondition.deleteCharAt(extSysCondition.length() - 1);
					selectSQL = selectSQL + extSysCondition.toString() + ")";
				}
				jdbcConnector.setParam("jdbcSelect", selectSQL);
				jdbcConnector.setParam("jdbcTable", ERROR_TABLE);
				jdbcConnector.setRSInterface(getRSInterface());
				jdbcConnector.setDebugMode(debugMode());
				jdbcConnector.setLog(getLog());

				str = getParam(PARAM_ACTION);
				if (str != null) {
					if (str.equals(ACTION_NONE)) {
						retryHoldedMessages = false;
						deleteHoldedMessages = false;
					} else if (str.equals(ACTION_RETRY)) {
						retryHoldedMessages = true;
					} else if (str.equals(ACTION_DELETE)) {
						deleteHoldedMessages = true;
					}
				}
			}
		}

		Trace.exitmin(this, "initialize", obj);
	}

	/**
	 * This class checks the MAXINTERROR table for errors. This table contains
	 * records only when an internal error occurred.<br>
	 * We scan this table for messages with HOLD status. If such message exist a
	 * warning message is logged and optionally we can try to reprocess it.
	 * <p>
	 * Note: This class extends the {@link TimerTask} because a CheckTask
	 * instance is passed to a timer configured to check the status in specific
	 * interval of time.
	 */

	private class CheckTask extends TimerTask {
		public void run() {
			try {
				
				jdbcConnector.initialize(null);
				// Select every time in case table have changed
				jdbcConnector.selectEntries();

				Entry e = null;
				while ((e = jdbcConnector.getNextEntry()) != null) {
					String extSys = e.getString(COLUMN_EXT_SYSTEM);
					String msgID = e.getString(COLUMN_MSGID);

					// When action is set to 'None' only warning is displayed
					getLog().logwarn(resHash.getString("MXCDCONN.FOUND.MESSAGE.WITH.STATUS.HOLD", new Object[] { msgID, extSys }));

					if (retryHoldedMessages || deleteHoldedMessages) {
						String date = "";

						// since the SimpleDateFormat is not synchronized we
						// need to synchronized it manually.
						synchronized (SDF) {
							date = SDF.format(new Date());
						}
						Entry updEntry = new Entry();
						updEntry.setAttribute("CHANGEDATE", date);
						updEntry.setAttribute("STATUSDATE", date);

						if (retryHoldedMessages) {
							logmsg(resHash.getString("MXCDCONN.TRY.REPROCESS.MESSAGE", msgID));

							// Set message status to RETRY in order to be send
							// again
							updEntry.setAttribute(COLUMN_STATUS, STATUS_RETRY);

						} else if (deleteHoldedMessages) {
							logmsg(resHash.getString("MXCDCONN.DELETE.MESSAGE", msgID));

							// Marking message with deleteflag=1 causes Maximo
							// to delete it
							updEntry.setAttribute(COLUMN_DELETEFLAG, "1");
						}

						SearchCriteria criteria = new SearchCriteria(COLUMN_ERRORID, SearchCriteria.EXACT, e
								.getString(COLUMN_ERRORID));
						jdbcConnector.modEntry(updEntry, criteria);
					}
					jdbcConnector.terminate();
				}
			} catch (Exception ex) {
				logmsg(resHash.getString("MXCDCONN.ERROR.WHILE.ERROR.CHECKING", ex));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ConnectorInterface getNextClient() throws Exception {
		Trace.entrymin(this, "getNextClient");

		if (isTerminating()) {
			return null;
		}

		ConnectorInterface httpSession = null;
		while (httpSession == null && !isTerminating()) {
			try {
				// start timer if we have a valid interval
				if (errorCheckInterval > 0 && timer == null) {
					timer = new Timer();

					// Execute after errorCheckInterval seconds for the first
					// time and repeat in every errorCheckInterval seconds.
					timer.schedule(new CheckTask(), errorCheckInterval, errorCheckInterval);
				}

				// wait for a client to connect
				httpSession = httpServer.getNextClient();

				// stop timer if we have a client
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
			} catch (Exception ex) {
				logmsg(resHash.getString("MXCDCONN.CLIENT.CONNECTION.ERROR", ex));
			}
		}

		if (isTerminating()) {
			terminate();
			return null;
		}

		// Create instance to be used as Iterator in the spawned AL
		TpaeIFCDConnector clientSession = new TpaeIFCDConnector();
		clientSession.serverConnector = this;
		clientSession.setConfiguration(getConfiguration());
		clientSession.setRSInterface(getRSInterface());
		clientSession.setName(getName());
		clientSession.setLog(getLog());
		clientSession.initialize(httpSession);

		Trace.exitmin(this, "getNextClient", clientSession);
		return clientSession;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectEntries() throws Exception {
		Trace.entrymin(this, "selectEntries");
		if (httpClientSession == null)
			return;

		Entry entry = httpClientSession.getNextEntry();

		if (entry == null) {
			return;
		}

		xmlParser = new XMLParser2();

		// Get all 3rd level children - the root MBOs elements
		// For example: "PublishMXASSET/MXASSETSet/ASSET"
		xmlParser.setParam("xpath.expr", "*/*/*");
		xmlParser.setParam("entry.tag", "");
		xmlParser.setContext(this);
		xmlParser.setDebug(debugMode());

		String httpBody = entry.getString(HTTPServerConnector.ATTR_NAME_HTTP_BODY);
		if (httpBody != null && httpBody.trim().length() > 0) {
			xmlParser.setInputStream(httpBody);
			xmlParser.initParser();
			httpClientSession.replyEntry(getOKResponse());
		}
		httpClientSession.terminate();
		Trace.exitmin(this, "selectEntries");
	}

	/**
	 * This method sets the entry operation based on the 'action' XML attribute
	 * of the root MBO.
	 * <p>
	 * When an entry is modified every modified attribute of the <b>root MBO</b>
	 * is marked with 'changed' XML attribute. Therefore we check every first
	 * level attribute for this XML attribute and if present and equal to '1'
	 * (true) we set the Attribute operation to 'modify'.
	 * 
	 * @param entry
	 */
	private void setEntryOperation(Entry entry) {
		Attribute rootMBO = entry.getFirstChild();

		// get 'action' XML attribute of root MBO
		String action = rootMBO.getAttribute(AbstractMxConnMode.ACTION_ATTR);

		if (AbstractMxConnMode.REPLACE_ACTION.equals(action) || AbstractMxConnMode.CHANGE_ACTION.equals(action)
				|| AbstractMxConnMode.ADDCHANGE_ACTION.equals(action)) {
			action = Entry.OP_MOD2;
		} else {
			// for Add and Delete
			action = action.toLowerCase();
		}
		entry.setOperation(action);

		// When entry is modified the 'changed' XML attribute indicates exactly
		// which attributes have been modified (updated, added)
		if (action.equals(Entry.OP_MOD2)) {
			String[] attrNames = entry.getAttributeNames();
			Attribute attr = null;

			// it is sufficient to check only 1st level children
			// since only attributes of root MBO can be marked as 'changed'
			for (String attrName : attrNames) {
				attr = entry.getAttribute(attrName);
				String changedAttr = attr.getAttribute(AbstractMxConnMode.CHANGED_ATTR);
				if (changedAttr != null && changedAttr.equals("1")) {
					// this attribute has been modified, the type of
					// modification is unknown, so mark as modified always
					attr.setOper(Attribute.ATTRIBUTE_MOD);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getNextEntry() throws Exception {
		Trace.entrymin(this, "getNextEntry");

		Entry e = xmlParser.readEntry();

		if (e != null) {
			setEntryOperation(e);
		}

		Trace.exitmin(this, "getNextEntry", e);

		return e;
	}

	/**
	 * @return entry for "HTTP OK" response.
	 */
	private Entry getOKResponse() {
		Entry response = new Entry();
		response.setAttribute("http.body", "");
		response.setAttribute("http.content-type", "text/html");
		response.setAttribute("http.status", HTTPParser.HTTP_OK);

		/*
		 * Maximo sends 3 request for each modification. The first POST request
		 * contains the changes and "Connection: keep-alive". The next two
		 * request are empty. However after the third request Maximo closes the
		 * connection. Therefore we will close the connection after the first
		 * request discarding the "keep-alive".
		 */
		response.setAttribute(HTTPServerConnector.ATTR_NAME_HTTP_CONNECTION, "close");
		return response;
	}

	/**
	 * @return <code>true</code> if the Connector is terminating;
	 *         <code>false</code> otherwise
	 */
	private boolean isTerminating() {
		return terminationRequested.get();
	}

	/**
	 * Stop servicing clients.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	@Override
	public void terminateServer() throws Exception {
		Trace.entrymin(this, "terminateServer");
		if (serverConnector == null) {

			// we are the server here
			terminationRequested.set(true);
			httpServer.terminateServer();
		} else {

			// we are a client session here
			if (!serverConnector.isTerminating()) {
				serverConnector.terminateServer();
			}
		}

		super.terminateServer();

		Trace.exitmin(this, "terminateServer");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void terminate() throws Exception {
		Trace.entrymin(this, "terminate");

		if (xmlParser != null) {
			xmlParser.closeParser();
		}

		if (httpServer != null) {
			httpServer.terminate();
		}

		if (httpClientSession != null) {
			httpClientSession.terminate();
		}

		if (jdbcConnector != null) {
			jdbcConnector.terminate();
		}

		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		super.terminate();

		Trace.exitmin(this, "terminate");
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I%, 20%E%";
	}
}
