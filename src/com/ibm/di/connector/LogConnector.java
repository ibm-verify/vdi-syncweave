/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.util.HashMap;
import java.util.Map;

import com.ibm.di.config.base.LogConfigItemImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LogConfigItem;
import com.ibm.di.entry.Entry;
import com.ibm.di.log.LogInterface;
import com.ibm.di.log.LogUtils;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;

/**
 * This class implements a Connector that interfaces with the LogInterface. The
 * class only supports AddOnly Mode.
 */
public class LogConnector extends Connector {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * name of the component
	 */
	private static final String MYNAME = "LogConnector";

	/**
	 * name of attribute message in the entry
	 */
	private static final String MESSAGE = "message";
	/**
	 * name of attribute level in the entry
	 */
	private static final String LEVEL = "level";
	/**
	 * name of attribute exception in the entry
	 */
	private static final String EXCEPTION = "exception";

	/**
	 * Resource hash object for accessing TMS messages
	 */
	private static final ResourceHash sResHash = ResourceHash
			.getHash("logconnector");

	/**
	 * logging object
	 */
	private LogInterface logger;

	/**
	 * The constructor for this class
	 */
	public LogConnector() {
		super();
		Trace.entrymid(this, MYNAME);
		setName(MYNAME);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE, });
		Trace.exitmid(this, MYNAME);
	}

	/**
	 * Initializes the Connector.
	 * 
	 * @param o -
	 *            Ignored
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initialize(Object o) throws Exception {
		Trace.entrymin(this, "initialize", o);

		String param = getParam(LogUtils.LOGGING_INTERFACE);
		if (param == null || param.trim().length() == 0)
			throw new Exception(sResHash.getString("NO.INTERFACE.PARAMETER"));

		logger = (LogInterface) Class.forName(param).newInstance();
		BaseConfiguration config = getRawConnectorConfiguration();

		if (config.getBooleanParameter(LogUtils.CATEGORY_BASED, false)) {
			logger.setCategory(config
					.getStringParameter(LogUtils.CATEGORY_NAME));
		} else {
			LogConfigItem lci = new LogConfigItemImpl(
					getRawConnectorConfiguration().getData());
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(LogInterface.TYPE, "Connector");
			map.put(LogInterface.NAME, getName());
			map.put(LogInterface.CONFIG_INSTANCE, getRSInterface());
			map.put(LogInterface.TIME, "" + System.currentTimeMillis());

			logger.addAppender(lci, map);
		}

		Trace.exitmin(this, "initialize");
	}

	/**
	 * Log a single Entry to the log
	 * 
	 * @param entry
	 *            The entry with the information to log
	 * @throws Exception
	 *             if an error occurs
	 */
	public void putEntry(Entry entry) throws Exception {
		Trace.entrymin(this, "putEntry", entry);
		String msg = entry.getString(MESSAGE);
		if (msg == null)
			msg = ""; // An empty message
		Object exception = entry.getObject(EXCEPTION);
		String level = entry.getString(LEVEL);

		if (exception instanceof Throwable)
			logger.error(msg, (Throwable) exception);
		else if (level != null)
			logger.log(level, msg);
		else
			logger.info(msg);

		Trace.exitmin(this, "putEntry");
	}

	/**
	 * Terminates the Connector and tries to free up resources
	 */
	public void terminate() {
		Trace.entrymin(this, "terminate");
		if (logger != null)
			logger.close();
		logger = null;
		Trace.exitmin(this, "terminate");
	}

	/**
	 * Returns the LogInterface we are logging to
	 * 
	 * @return the logging object
	 */
	public LogInterface getLogger() {
		return logger;
	}

	/**
	 * Return version information
	 * 
	 * @return version info
	 */
	public String getVersion() {
		return "2.1-di7.1.1 %I% 20%E%";
	}

}
