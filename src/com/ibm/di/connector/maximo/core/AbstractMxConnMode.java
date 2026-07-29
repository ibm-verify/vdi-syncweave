/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.core;

import java.util.Date;
import java.util.List;

import com.ibm.di.connector.maximo.exception.MxConnectorException;
import com.ibm.di.connector.maximo.util.HttpClient;
import com.ibm.di.connector.maximo.util.TemplateLoader;
import com.ibm.di.connector.maximo.util.typeconverter.DateConverter;
import com.ibm.di.server.Log;

/**
 * This class provides a base implementation to minimize the effort required to
 * implement a specific connector's mode.
 * 
 * @since 7.1
 * @see HttpClient
 * @see MxConnConfiguration
 */
public abstract class AbstractMxConnMode {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public static final String CHANGED_ATTR = "changed";
	public static final String ACTION_ATTR = "action";
	public static final String ACTION_ATTR_PREFIXED = "#" + ACTION_ATTR;

	public static final String ADD_ACTION = "Add";
	public static final String CHANGE_ACTION = "Change";
	public static final String DELETE_ACTION = "Delete";
	public static final String ADDCHANGE_ACTION = "AddChange";
	public static final String REPLACE_ACTION = "Replace";

	private final MxConnConfiguration cfg;

	private long creation;

	private final HttpClient http;

	private String messageId;

	/**
	 * Logger used by the connector.
	 */
	protected Log logger = null;

	/**
	 * Constructs an {@link AbstractMxConnMode}.
	 * 
	 * @param cfg
	 *            the connector configuration object
	 * @param log
	 *            logger of the connector
	 */
	public AbstractMxConnMode(final MxConnConfiguration cfg, Log log) {
		if (cfg == null) {
			throw new IllegalArgumentException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.NO.CONFIGURATION"));
		}

		logger = log;
		this.cfg = cfg;
		this.http = new HttpClient(log);
	}

	/**
	 * Returns the connector configuration object.
	 * 
	 * @return connector configuration object
	 */
	protected final MxConnConfiguration getCfg() {
		return cfg;
	}

	/**
	 * Posts the message extracted from the specified template to the given
	 * Maximo Enterprise Service. Before posting the message, some common
	 * properties are defined in the template, such as
	 * <tt>creation.date.time</tt>, <tt>message.id</tt>, and
	 * <tt>maximo.version</tt>.
	 * 
	 * @param tlp
	 *            template from which the message will be extracted and posted
	 * @param enterpriseServiceUrlList
	 *            list of Maximo enterprise service URLs that will handle the
	 *            message
	 * @return the response sent back by the Maximo Enterprise Service
	 * @throws MxConnectorException
	 *             if any sort of communication problem occurs
	 * @see #setMessageId(String)
	 */
	protected final String post(final TemplateLoader tlp, final List<String> enterpriseServiceUrlList) throws MxConnectorException {

		// Specify common attributes
		tlp.setProperty(TemplateLoader.CREATION_HOLDER, DateConverter.getInstance().toString(getCreationAsDate()));
		tlp.setProperty(TemplateLoader.MSGID_HOLDER, getMessageId());
		tlp.setProperty(TemplateLoader.VERSION_HOLDER, cfg.getMaximoVersion());
		tlp.setProperty(TemplateLoader.LANG_HOLDER, cfg.getTransLanguage());

		http.setTimeout(cfg.getTimeout());
		http.setAuthenticationRequired(cfg.isAuthenticationRequired());
		http.setUserId(cfg.getUserId());
		http.setPassword(cfg.getPassword());
		http.setXmlCharValidationEnabled(cfg.isXmlCharValidationEnabled());
		http.setTargetUrlList(enterpriseServiceUrlList);

		return http.post(tlp.toString());
	}

	/**
	 * Defines the creation date/time to be set before posting a message. If no
	 * date/time is defined, the {@link System#currentTimeMillis() current}
	 * date/time is used.
	 * 
	 * @param creation
	 *            creation date/time to be set before posting a message,
	 *            expressed as milliseconds
	 * @see #post(TemplateLoader, List)
	 */
	protected final void setCreation(final long creation) {
		this.creation = creation;
	}

	/**
	 * Defines the message ID to be set before posting a message. If no message
	 * ID is defined, the {@link System#currentTimeMillis() current} date/time
	 * expressed as milliseconds is used.
	 * 
	 * Note: Not used
	 * 
	 * @param messageId
	 *            message ID to be set before posting the message
	 * @see #post(TemplateLoader, List)
	 */
	protected final void setMessageId(final String messageId) {
		this.messageId = messageId;
	}

	private long getCreation() {
		if (creation == 0) {
			return System.currentTimeMillis();
		}
		return creation;
	}

	private Date getCreationAsDate() {
		return new Date(getCreation());
	}

	private String getMessageId() {
		if (messageId == null) {
			return String.valueOf(System.currentTimeMillis());
		}
		return messageId;
	}
}
