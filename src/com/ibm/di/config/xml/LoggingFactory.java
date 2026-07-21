/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.di.config.base.LogConfigItemImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.LogConfig;
import com.ibm.di.config.interfaces.LogConfigItem;
import com.ibm.di.log.LogUtils;

/**
 * Read/Write {@link LogConfig} elements in XML format.
 * 
 */
public class LoggingFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String LOGGING_TAG = "LogConfig";

	public final static String LOGGER_TAG = "Logger";

	public final static String LOG_LEVEL = "Level";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {

		getBaseName(config, elem);

		// Appenders
		NodeList list;
		if ((list = elem.getElementsByTagName(LOGGER_TAG)) == null)
			return;

		for (int i = 0; i < list.getLength(); i++) {
			parseLogger((Element) list.item(i), (LogConfig) config);
		}
	}

	public void parseLogger(Element e, LogConfig config) throws Exception {
		LogConfigItem lci = new LogConfigItemImpl();
		getBaseName(lci, e);
		getParameters(e, lci);

		// Remove MOBJ Appenders
		if ("MOBJ".equals(lci.getStringParameter(LogUtils.APPENDER)))
			return;
		
		// Work around a problem where Loggers were saved with no name. 
		if (lci.getShortName() == null)
			lci.setName(lci.getStringParameter(LogUtils.APPENDER));

		config.addItem(lci);
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {

		setBaseName(config, elem);

		for (LogConfigItem logger: ((LogConfig) config).getItems()) {
			Element e = elem.getOwnerDocument().createElement(LOGGER_TAG);
			elem.appendChild(e);
			buildLogger(e, logger);
		}

	}

	public void buildLogger(Element e, LogConfigItem config) throws Exception {
		setBaseName(config, e);
		setParameters(e, config, null);
	}
}
