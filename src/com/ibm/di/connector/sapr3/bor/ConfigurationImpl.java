/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.server.Log;

/**
 * Implementation of the TDI Connector configuration. This class wraps the
 * Connector itself and exposes on the configuration parameter accessors.
 * log4j.logger.com.ibm.di.admin
 */
final class ConfigurationImpl implements Configuration {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	SapR3BorConnector conn;

	Log log;

	List writeStylesheetList;

	Map readStylesheetMap;

	/**
	 * Initialize stylesheet files.
	 * 
	 * @param urc
	 *            The wrapped raw configuration source.
	 */
	ConfigurationImpl(SapR3BorConnector urc) throws ConfigurationException {
		super();

		if (urc == null) {
			throw new IllegalArgumentException();
		}
		conn = urc;
		writeStylesheetList = Collections.synchronizedList(new LinkedList());
		readStylesheetMap = Collections.synchronizedMap(new HashMap());
		if (conn.getLog() == null) {
			log = new Log("log4j.logger.com.ibm.di.admin");
		} else {
			log = conn.getLog();
		}

		initStylesheets();
	}

	private SapR3BorConnector getConn() {
		return conn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.Configuration#getParamAsString(java.lang.String)
	 */
	public String getParamAsString(String paramName) {
		if (paramName == null) {
			throw new IllegalArgumentException();
		}

		return conn.getParam(paramName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.Configuration#getParamAsStringArray(java.lang.String)
	 */
	public String[] getParamAsStringArray(String paramName) {
		if (paramName == null) {
			throw new IllegalArgumentException();
		}

		return ((String[]) (writeStylesheetList
				.toArray(new String[writeStylesheetList.size()])));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.Configuration#getParamAsFile(java.lang.String)
	 */
	public File getParamAsFile(String paramName) {
		if (paramName == null) {
			throw new IllegalArgumentException();
		}

		ConnectorConfig config = (ConnectorConfig) conn.getConfiguration();
		if (config.getMode().equals(ConnectorConfig.ADDONLY_MODE)) {
			throw new IllegalStateException();
		}

		return ((File) readStylesheetMap.get(paramName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.Configuration#getParamAsFileArray(java.lang.String)
	 */
	public File[] getParamAsFileArray(String paramName) {
		if (paramName == null) {
			throw new IllegalArgumentException();
		}

		ConnectorConfig config = (ConnectorConfig) conn.getConfiguration();
		if (config.getMode().equals(ConnectorConfig.ITERATOR_MODE)
				|| config.getMode().equals(ConnectorConfig.LOOKUP_MODE)) {
			throw new IllegalStateException();
		}

		return ((File[]) (writeStylesheetList
				.toArray(new File[writeStylesheetList.size()])));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.Configuration#getLog()
	 */
	public Log getLog() {
		return log;
	}

	/*
	 * Get the raw TDI Config object. @return ConnectorConfig instance.
	 */
	public Object getRawConfig() {
		return conn.getConfiguration();
	}

	private File createFileFromUrl(String url) throws ConfigurationException {
		File f = new File(url);
		if (!f.exists()) {
			Object[] args = new Object[] { url };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0008, args);
			throw new ConfigurationException(msg);
		}

		if (!f.isFile()) {
			Object[] args = new Object[] { url };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0009, args);
			throw new ConfigurationException(msg);
		}

		if (!f.canRead()) {
			Object[] args = new Object[] { url };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0010, args);
			throw new ConfigurationException(msg);
		}

		return f;
	}

	private void initStylesheets() throws ConfigurationException {
		ConnectorConfig config = (ConnectorConfig) conn.getConfiguration();
		if (config.getMode().equals(ConnectorConfig.ADDONLY_MODE)) {
			initStylesheetList(ConfigurationNames.PARAM_PUT_STYLESHEET_LIST);
		} else if (config.getMode().equals(ConnectorConfig.DELETE_MODE)) {
			initStylesheetList(ConfigurationNames.PARAM_DELETE_STYLESHEET_LIST);
			initLookupModeStylesheets();
		} else if (config.getMode().equals(ConnectorConfig.UPDATE_MODE)) {
			initStylesheetList(ConfigurationNames.PARAM_MODIFY_STYLESHEET_LIST);
			initLookupModeStylesheets();
		} else if (config.getMode().equals(ConnectorConfig.LOOKUP_MODE)) {
			initLookupModeStylesheets();
		} else if (config.getMode().equals(ConnectorConfig.ITERATOR_MODE)) {
			initIterateModeStylesheets();
		}
	}

	private void initStylesheetList(String paramName)
			throws ConfigurationException {
		String paramVal = getParamAsString(paramName);
		if (paramVal != null) {
			StringReader sr = new StringReader(paramVal);
			BufferedReader br = new BufferedReader(sr);
			String xslFile;
			try {
				while (br.ready() && ((xslFile = br.readLine()) != null)) {
					xslFile = xslFile.trim();
			        if (xslFile.length() == 0)
						continue;
					File f = createFileFromUrl(xslFile);
					writeStylesheetList.add(f);
				}
			} catch (IOException x) {
				throw new ConfigurationException(x);
			} finally {
				try {
					br.close();
				} catch (IOException x) {
					if (getLog() != null) {
						log.logwarn(LogMessageHelper.getMsgResource().getMessage(LogMessageHelper.SAPR3_BOR_0019,new Object[] { x.getMessage() }));
					}
				}
			}
		}

		if (writeStylesheetList.size() == 0) {
			Object[] args = new Object[] { ((ConnectorConfig) conn
					.getConfiguration()).getMode() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0027, args);
			throw new ConfigurationException(msg);
		}
	}

	private void initIterateModeStylesheets() throws ConfigurationException {
		String paramVal = getParamAsString(ConfigurationNames.PARAM_SELECT_ENTRIES_PRE_STYLESHEET);
		if (paramVal == null || paramVal.length() == 0) {
			Object[] args = new Object[] { ConfigurationNames.PARAM_SELECT_ENTRIES_PRE_STYLESHEET };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0011, args);
			throw new ConfigurationException(msg);
		}

		File f = createFileFromUrl(paramVal);
		readStylesheetMap.put(
				ConfigurationNames.PARAM_SELECT_ENTRIES_PRE_STYLESHEET, f);

		paramVal = getParamAsString(ConfigurationNames.PARAM_SELECT_ENTRIES_POST_STYLESHEET);
		if (paramVal == null || paramVal.length() == 0) {
			Object[] args = new Object[] { ConfigurationNames.PARAM_SELECT_ENTRIES_POST_STYLESHEET };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0011, args);
			throw new ConfigurationException(msg);
		}

		f = createFileFromUrl(paramVal);
		readStylesheetMap.put(
				ConfigurationNames.PARAM_SELECT_ENTRIES_POST_STYLESHEET, f);

		paramVal = getParamAsString(ConfigurationNames.PARAM_GETNEXT_PRE_STYLESHEET);
		if (paramVal == null || paramVal.length() == 0) {
			Object[] args = new Object[] { ConfigurationNames.PARAM_GETNEXT_PRE_STYLESHEET };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0011, args);
			throw new ConfigurationException(msg);
		}

		f = createFileFromUrl(paramVal);
		readStylesheetMap.put(ConfigurationNames.PARAM_GETNEXT_PRE_STYLESHEET,
				f);

		paramVal = getParamAsString(ConfigurationNames.PARAM_GETNEXT_POST_STYLESHEET);
		if (paramVal == null || paramVal.length() == 0) {
			Object[] args = new Object[] { ConfigurationNames.PARAM_GETNEXT_POST_STYLESHEET };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0011, args);
			throw new ConfigurationException(msg);
		}

		f = createFileFromUrl(paramVal);
		readStylesheetMap.put(ConfigurationNames.PARAM_GETNEXT_POST_STYLESHEET,
				f);
	}

	private void initLookupModeStylesheets() throws ConfigurationException {
		String paramVal = getParamAsString(ConfigurationNames.PARAM_FIND_PRE_STYLESHEET);
		if (paramVal == null || paramVal.length() == 0) {
			Object[] args = new Object[] { ConfigurationNames.PARAM_FIND_PRE_STYLESHEET };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0011, args);
			throw new ConfigurationException(msg);
		}

		File f = createFileFromUrl(paramVal);
		readStylesheetMap.put(ConfigurationNames.PARAM_FIND_PRE_STYLESHEET, f);

		paramVal = getParamAsString(ConfigurationNames.PARAM_FIND_POST_STYLESHEET);
		if (paramVal == null || paramVal.length() == 0) {
			Object[] args = new Object[] { ConfigurationNames.PARAM_FIND_POST_STYLESHEET };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0011, args);
			throw new ConfigurationException(msg);
		}

		f = createFileFromUrl(paramVal);
		readStylesheetMap.put(ConfigurationNames.PARAM_FIND_POST_STYLESHEET, f);
	}

}
