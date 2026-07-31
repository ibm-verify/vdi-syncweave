/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.base.SchemaItemConfigImpl;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.server.ResourceHash;

/**
 * Read/Write {@link SchemaConfig} elements in XML.
 */
public class SchemaFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String SCHEMA_TAG = "Schema";

	public final static String SCHEMA_ITEM_TAG = "SchemaItem";

	public final static String SCHEMA_ITEM_NAME = "Name";

	public final static String SCHEMA_ITEM_SYNTAX = "Syntax";

	public final static String SCHEMA_ITEM_NATIVE = "NativeSyntax";

	public final static String SCHEMA_ITEM_EXCLUDED = "Excluded";

	public final static String SCHEMA_ITEM_REQINP = "RequiredInput";

	public final static String SCHEMA_ITEM_REQOUT = "RequiredOutput";

	public final static String SCHEMA_ITEM_DEFVAL = "DefaultValue";

	public final static String SCHEMA_ITEM_SAMPLE = "Sample";

	public final static String SCHEMA_ITEM_PRESENCE = "Presence";

	public final static String SCHEMA_ITEM_COMMENT = "Comment";

	private final static ResourceHash sResHash = BaseConfigurationImpl.getResHash();

	private static final String SCHEMA_ITEM_MIN_OCCURS = "MinOccurs";

	private static final String SCHEMA_ITEM_MAX_OCCURS = "MaxOccurs";

	private static final String SCHEMA_ITEM_PROPERTY = "Property";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {

		logmsg(sResHash.getString("MMCONFIG.SCHEMAFACTORY.PARSE", elem.getTagName()));

		// Set name and inherit from
		getBaseName(config, elem);
		if (! (config instanceof SchemaConfig))
			return;
		SchemaConfig cc = (SchemaConfig) config;

		// Get SchemaItem elements
		NodeList list = elem.getElementsByTagName(SCHEMA_ITEM_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getParentNode() == elem)
				getSchemaItem((Element) list.item(i), cc);
		}
	}

	public void migrate(ConnectorConfig cc, Element elem) throws Exception {
		migrateSchema(cc.getSchema(true), cc.getAttributeMap(true), elem, true);
		migrateSchema(cc.getSchema(false), cc.getAttributeMap(false), elem,
				false);
	}

	protected void migrateSchema(SchemaConfig cc, AttributeMapConfig amc,
			Element elem, boolean input) throws Exception {

		// Set name and inherit from
		getBaseName(cc, elem);

		// Get SchemaItem elements
		NodeList list = elem.getElementsByTagName(SCHEMA_ITEM_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			migrateSchemaItem((Element) list.item(i), cc, amc, input);
		}

	}

	public void migrateSchemaItem(Element elem, SchemaConfig config,
			AttributeMapConfig amc, boolean input) throws Exception {

		logmsg(sResHash.getString("MMCONFIG.SCHEMAFACTORY.MIGRATESCHEMAITEM",
				elem.getTagName()));
		String str;
		String name = getNodeTextByName(elem, SCHEMA_ITEM_NAME);
		SchemaItemConfig sci = config.newItem(name);

		// Internal Syntax
		str = getNodeTextByName(elem, SCHEMA_ITEM_SYNTAX);
		if (str != null && str.length() > 0)
			sci.setJavaClass(str);

		// External Syntax
		str = getNodeTextByName(elem, SCHEMA_ITEM_NATIVE);
		if (str != null && str.length() > 0)
			sci.setExternalSyntax(str);

		// Presence flag
		str = getNodeTextByName(elem, SCHEMA_ITEM_PRESENCE);
		if (str != null && str.length() > 0)
			sci.setPresenceFlag(str);

		// Sample
		// str = getNodeTextByName ( elem, SCHEMA_ITEM_SAMPLE );
		// if ( str != null && str.length() > 0 )
		// sci.setSample ( str );

		// Default value
		AttributeMapItem ami = null;
		if (input) {
			// Find one that uses simple and references name
			for (String itemName : amc.getAttributeNames()) {
				AttributeMapItem item = amc.getAttributeMapItem(itemName);
				if (item.isSimple() && name.equalsIgnoreCase(item.getSimple())) {
					ami = item;
					break;
				}
			}
		} else {
			ami = amc.getAttributeMapItem(name);
		}

		if (ami == null)
			return;

		str = getNodeTextByName(elem, SCHEMA_ITEM_DEFVAL);
		if (str != null && str.length() > 0 && (!str.equals("null"))) {
			// Default value -> NullBehavior:Value, NullBehaviorValue: str
			ami.setNullBehavior("Value");
			ami.setNullBehaviorValue(str);

		} else if (input) {
			// Input required
			if ("true".equals(getNodeTextByName(elem, SCHEMA_ITEM_REQINP))) {
				ami.setNullBehavior("Error");
			}
		} else {
			// Output required
			if ("true".equals(getNodeTextByName(elem, SCHEMA_ITEM_REQOUT))) {
				ami.setNullBehavior("Error");
			}
		}

	}

	public void getSchemaItem(Element elem, BaseConfiguration config) throws Exception {
		logmsg(sResHash.getString("MMCONFIG.SCHEMAFACTORY.GETITEM", elem.getTagName()));
		SchemaItemConfig sci;
		String name = getNodeTextByName(elem, SCHEMA_ITEM_NAME);

		if (config instanceof SchemaConfig) {
			sci = ((SchemaConfig) config).newItem(name);
		} else {
			sci = new SchemaItemConfigImpl();
			sci.init();
			sci.setAttributeName(name);
			((SchemaItemConfig) config).getChildSchemaList().addConfig(sci);
		}

		parseSchemaItem(elem, sci);

		// Get child schema list
		for (Node n = elem.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (SCHEMA_ITEM_TAG.equals(n.getNodeName())) {
				getSchemaItem((Element) n, sci);
			}
		}
	}

	public void parseSchemaItem(Element elem, SchemaItemConfig sci) throws Exception {
		// Internal Syntax
		String str = getNodeTextByName(elem, SCHEMA_ITEM_SYNTAX);
		if (str != null && str.length() > 0)
			sci.setJavaClass(str);

		// External Syntax
		str = getNodeTextByName(elem, SCHEMA_ITEM_NATIVE);
		if (str != null && str.length() > 0)
			sci.setExternalSyntax(str);

		// Presence flag
		str = getNodeTextByName(elem, SCHEMA_ITEM_PRESENCE);
		if (str != null && str.length() > 0)
			sci.setPresenceFlag(str);

		// Property flag
		str = getNodeTextByName(elem, SCHEMA_ITEM_PROPERTY);
		if (str != null && str.length() > 0)
			sci.setProperty(Boolean.valueOf(str));

		// Min occurs
		str = getNodeTextByName(elem, SCHEMA_ITEM_MIN_OCCURS);
		if (str != null && str.length() > 0)
			sci.setMinOccurrences(Integer.parseInt(str));

		// Max occurs
		str = getNodeTextByName(elem, SCHEMA_ITEM_MAX_OCCURS);
		if (str != null && str.length() > 0)
			sci.setMaxOccurrences(Integer.parseInt(str));

		// User Comment
		str = getNodeTextByName(elem, SCHEMA_ITEM_COMMENT);
		if (str != null && str.length() > 0)
			sci.setUserComment(str);

		// Sample
		str = getNodeTextByName(elem, SCHEMA_ITEM_SAMPLE);
		if (str != null && str.length() > 0)
			sci.setSample(str);
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {

		SchemaConfig cc = (SchemaConfig) config;

		Element e = elem.getOwnerDocument().createElement(SCHEMA_TAG);
		elem.appendChild(e);

		// Set name and inherit from
		setBaseName(cc, e);

		if (! "true".equals(System.getProperty("com.ibm.di.suppressSchema"))) {
		for (Object str : cc.getKeys(BaseConfiguration.SUBTREE)) {
			setSchemaItem(e, cc.getItem(str));
		}
		}

	}

	public void setSchemaItem(Element elem, SchemaItemConfig config) throws Exception {

		Element p = elem.getOwnerDocument().createElement(SCHEMA_ITEM_TAG);

		// Attribute name
		setSingleElement(p, SCHEMA_ITEM_NAME, config.getAttributeName());
		setSingleElement(p, SCHEMA_ITEM_SYNTAX, config,
				InternalSchema.SCHEMA_INTERNAL_SYNTAX);
		setSingleElement(p, SCHEMA_ITEM_NATIVE, config,
				InternalSchema.SCHEMA_EXTERNAL_SYNTAX);
		setSingleElement(p, SCHEMA_ITEM_PRESENCE, config,
				InternalSchema.SCHEMA_PRESENCE);
		setSingleElement(p, SCHEMA_ITEM_COMMENT, config,
				InternalSchema.USER_COMMENT);
		setSingleElement(p, SCHEMA_ITEM_MIN_OCCURS, config,
				InternalSchema.SCHEMA_OCCURS_MIN);
		setSingleElement(p, SCHEMA_ITEM_MAX_OCCURS, config,
				InternalSchema.SCHEMA_OCCURS_MAX);
		setSingleElement(p, SCHEMA_ITEM_PROPERTY, config,
				InternalSchema.SCHEMA_PROPERTY);

		elem.appendChild(p);

		// Get child schema list
		for (int i = 0; i < config.getChildSchemaList().size(); i++) {
			setSchemaItem(p, (SchemaItemConfig) config.getChildSchemaList()
					.getConfig(i));
		}

	}
}
