/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.di.config.base.PropertyStoreConfigImpl;
import com.ibm.di.config.interfaces.*;

/**
 * Read/Write {@link PropertyManager} elements in XML
 */
public class PropertyStoreFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String PROPERTIES_TAG = "Properties";

	public final static String PROPERTY_STORE_TAG = "PropertyStore";

	public final static String PROPERTY_RC = "RawConnector";

	public final static String PROPERTY_KEY = "Key";

	public final static String PROPERTY_VALUE = "Value";

	public final static String PROPERTY_RO = "ReadOnly";

	public final static String PROPERTY_NAMEFILTER = "Filter";

	public final static String PROPERTY_INITIAL_LOAD = "InitialLoad";

	public final static String PROPERTY_CACHE_TIMEOUT = "CacheTimeout";

	public final static String PROPERTY_DEFAULT_STORE = "DefaultStore";

	public final static String PROPERTY_PASSWORD_STORE = "PasswordStore";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {
		PropertyManager pm = (PropertyManager) config;
		String str;

		config.init();
		getBaseName(config, elem);

		NodeList list = elem.getElementsByTagName(PROPERTY_STORE_TAG);
		for (int i = 0; i < list.getLength(); i++)
			pm.addPropertyStore(parse(pm, (Element) list.item(i)));

		if ((str = getNodeTextByName(elem, PROPERTY_DEFAULT_STORE)) != null) {
			try {
				pm.setDefaultPropertyStore(pm.getPropertyStore(str));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ((str = getNodeTextByName(elem, PROPERTY_PASSWORD_STORE)) != null) {
			try {
				pm.setDefaultPasswordStore(pm.getPropertyStore(str));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public PropertyStoreConfig parse(PropertyManager pm, Element elem)
			throws Exception {
		PropertyStoreConfig cc;
		String str;
		Element e;

		cc = new PropertyStoreConfigImpl();
		cc.setParent(pm);
		cc.init();

		getBaseName(cc, elem);

		// Add Parser settings
		if ((e = getSingleElement(elem, ParserFactory.PARSER_TAG)) != null) {
			getFactory(ParserFactory.PARSER_TAG).parse(cc.getParserConfig(), e);
			cc.getParserConfig().setupInheritanceChain();
		}

		// Add Raw Connector
		if ((e = getSingleElement(elem, PROPERTY_RC)) != null) {
			getBaseName(cc.getConnectionConfig(), e);
			getParameters(e, cc.getConnectionConfig());
			cc.getConnectionConfig().setupInheritanceChain();
		}

		// Key Attribute name
		if ((str = getNodeTextByName(elem, PROPERTY_KEY)) != null)
			cc.setKeyAttribute(str);

		// Value Attribute name
		if ((str = getNodeTextByName(elem, PROPERTY_VALUE)) != null)
			cc.setValueAttribute(str);

		// ReadOnly
		if ((str = getNodeTextByName(elem, PROPERTY_RO)) != null)
			cc.setReadOnly(Boolean.valueOf(str).booleanValue());

		// Name filters
		if ((str = getNodeTextByName(elem, PROPERTY_NAMEFILTER)) != null)
			cc.setNameFilters(str);

		// Initial load
		if ((str = getNodeTextByName(elem, PROPERTY_INITIAL_LOAD)) != null)
			cc.setInitialLoad(Boolean.valueOf(str).booleanValue());

		// Cache timeout
		if ((str = getNodeTextByName(elem, PROPERTY_CACHE_TIMEOUT)) != null)
			cc.setCacheTimeout(Integer.valueOf(str).intValue());

		return cc;
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {
		PropertyManager pm = (PropertyManager) config;
		setBaseName(config, elem);
		config.init();

		PropertyStoreConfig psc;
		if ((psc = pm.getDefaultPropertyStore()) != null)
			setSingleElement(elem, PROPERTY_DEFAULT_STORE, psc.getShortName());

		if ((psc = pm.getPasswordPropertyStore()) != null)
			setSingleElement(elem, PROPERTY_PASSWORD_STORE, psc.getShortName());

		Element e = elem.getOwnerDocument().createElement("Stores");
		elem.appendChild(e);
		ContainerConfig cc = pm.getPropertyStores();
		for (int i = 0; i < cc.size(); i++) {
			Element child = e.getOwnerDocument().createElement(
					PROPERTY_STORE_TAG);
			e.appendChild(child);
			build((PropertyStoreConfig) cc.getConfig(i), child);
		}
	}

	public void build(PropertyStoreConfig cc, Element elem) throws Exception {

		setBaseName(cc, elem);

		// Parser
		if (cc.getParserConfig() != null) {
			Element pc = elem.getOwnerDocument().createElement(
					ParserFactory.PARSER_TAG);
			elem.appendChild(pc);
			getFactory(ParserFactory.PARSER_TAG)
					.build(cc.getParserConfig(), pc);
		}

		// Connector
		if (cc.getConnectionConfig() != null) {
			Element rc = elem.getOwnerDocument().createElement(PROPERTY_RC);
			elem.appendChild(rc);
			setBaseName(cc.getConnectionConfig(), rc);
			setParameters(rc, cc.getConnectionConfig(), null);
		}

		// Key
		setSingleElement(elem, PROPERTY_KEY, cc.getKeyAttribute());

		// Value
		setSingleElement(elem, PROPERTY_VALUE, cc.getValueAttribute());

		// Readonly
		setSingleElement(elem, PROPERTY_RO, "" + cc.getReadOnly());

		// Name filters
		if (cc.getNameFilters() != null)
			setSingleElement(elem, PROPERTY_NAMEFILTER, ""
					+ cc.getNameFilters());

		// Initial load
		setSingleElement(elem, PROPERTY_INITIAL_LOAD, "" + cc.getInitialLoad());

		// Cache timeout
		setSingleElement(elem, PROPERTY_CACHE_TIMEOUT, ""
				+ cc.getCacheTimeout());
	}
}
