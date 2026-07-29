/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.core;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.NodeList;

import com.ibm.di.connector.maximo.exception.MxConnectorException;
import com.ibm.di.connector.maximo.util.Dom;
import com.ibm.di.connector.maximo.util.HttpClient;
import com.ibm.di.connector.maximo.util.TemplateLoader;
import com.ibm.di.server.Log;

/**
 * This class provides utility functions that can be used inside scripts.
 * 
 * @since 7.1
 */
public final class MxConnFunctions {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Log logger;
	
	private final MxConnConfiguration cfg;

	private final Map<String, MxDomain> domains;

	private final HttpClient http;

	private final TemplateLoader maxDomainTlp;

	/**
	 * Constructs a {@link MxConnFunctions}.
	 * 
	 * @param cfg
	 *            connector configuration object
	 */
	public MxConnFunctions(final MxConnConfiguration cfg, Log log) {
		this.cfg = cfg;
		logger = log;
		http = new HttpClient(log);
		maxDomainTlp = new TemplateLoader(TemplateLoader.TYPE_QUERY, logger);
		domains = new HashMap<String, MxDomain>();
	}

	/**
	 * Returns the synonym key for a given value.
	 * 
	 * @param domainId
	 *            domain ID
	 * @param synonymValue
	 *            value associated with the synonym key
	 * @return synonym key for a given value or <code>null</code> if the domain
	 *         ID does not exist
	 * @throws MxConnectorException
	 *             if it is not possible obtain the synonym domain from Maximo
	 */
	public String getSynonymKey(final String domainId, final String synonymValue) throws MxConnectorException {

		final MxDomain domain = getDomain(domainId);
		final String synonymKey = domain.valueToMaxValue.get(synonymValue);

		logger.debug("Domain ID=" + domainId + "; Synonym Key=" + synonymKey + "; Synonym Value=" + synonymValue);
		
		return synonymKey;
	}

	/**
	 * Returns the synonym value for a given key.
	 * 
	 * @param domainId
	 *            domain ID
	 * @param synonymKey
	 *            key associated with the synonym value
	 * @return synonym value for a given key or <code>null</code> if the domain
	 *         ID does not exist
	 * @throws MxConnectorException
	 *             if it is not possible obtain the synonym domain from Maximo
	 */
	public String getSynonymValue(final String domainId, final String synonymKey) throws MxConnectorException {

		final MxDomain domain = getDomain(domainId);
		final String synonymValue = domain.maxValueToDefault.get(synonymKey);

		logger.debug("Domain ID=" + domainId + "; Synonym Key=" + synonymKey + "; Synonym Value=" + synonymValue);

		return synonymValue;
	}

	private MxDomain buildDomain(final String domainId) throws MxConnectorException {

		maxDomainTlp.setProperty(TemplateLoader.MOS_HOLDER, cfg.getDomainObjectStructure());
		maxDomainTlp.setProperty(TemplateLoader.MBO_HOLDER, "<MAXDOMAIN><DOMAINID operator=\"=\">"+domainId+"</DOMAINID></MAXDOMAIN>");
		maxDomainTlp.setProperty(TemplateLoader.UNIQUERES_HOLDER, "true");
		maxDomainTlp.setProperty(TemplateLoader.MAXITEMS_HOLDER, "1");
		maxDomainTlp.setProperty(TemplateLoader.RSSTART_HOLDER, "0");

		http.setAuthenticationRequired(cfg.isAuthenticationRequired());
		http.setUserId(cfg.getUserId());
		http.setPassword(cfg.getPassword());
		http.setXmlCharValidationEnabled(cfg.isXmlCharValidationEnabled());
		http.setTargetUrlList(cfg.getUrlListForQueryMaxDomain());

		final String response = http.post(maxDomainTlp.toString());
		final NodeList synonymNodes = Dom.getElements("SYNONYMDOMAIN", response);
		final MxDomain domain = new MxDomain();

		for (int i = 0; i < synonymNodes.getLength(); i++) {
			final Map<String, String> attrMap;

			attrMap = Dom.getAttributes(synonymNodes.item(i));

			domain.add(attrMap.get("MAXVALUE"), attrMap.get("VALUE"), attrMap.get("DEFAULTS"));
		}

		return domain;
	}

	private MxDomain getDomain(final String domainId) throws MxConnectorException {
		if (!domains.containsKey(domainId)) {
			domains.put(domainId, buildDomain(domainId));
		}
		return domains.get(domainId);
	}
	
	private static final class MxDomain {

		private final Map<String, String> maxValueToDefault = new HashMap<String, String>();

		private final Map<String, String> valueToMaxValue = new HashMap<String, String>();

		private void add(final String maxValue, final String value, final String defaultValue) {

			valueToMaxValue.put(value, maxValue);

			if ("1".equals(defaultValue)) {
				maxValueToDefault.put(maxValue, value);
			}
		}

	}
}
