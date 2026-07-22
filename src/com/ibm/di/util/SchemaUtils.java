/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import javax.naming.NameAlreadyBoundException;

import org.w3c.dom.NodeList;

import com.ibm.di.config.base.SchemaItemConfigImpl;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;
import com.ibm.di.exceptions.IgnorableException;
import com.ibm.di.function.SystemFunctions;

/**
 * Provides methods for working with the Connector/FC schema. <br> <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class SchemaUtils {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Creates a schema item for each attribute in the entry. If the entry is
	 * domEnabled we call convertEntryToSchemaHier to generate a hierarchical
	 * schema representation.
	 * 
	 * @param entry
	 *            The Entry to use for creating Schema Items.
	 * @param cc
	 *            The ConnectorConfig where the SchemaItem will be added
	 * @param input
	 *            If true, add to Input Schema, else Output Schema.
	 * @throws IgnorableException
	 *             if any of the specified schema item names has incorrect
	 *             syntax this exception is thrown. It will contain the stack
	 *             traces of all occurred problems (if several items had
	 *             incorrect names).
	 */
	public static void convertEntryToSchema(Entry entry, ConnectorConfig cc, boolean input) throws IgnorableException {
		if (entry.isDOMEnabled()) {
			convertEntryToSchemaHier(entry, cc, input);
		} else {
			IgnorableException ignoreEx = new IgnorableException();
			for (String str : entry.getAttributeNames()) {
				Attribute attr = entry.getAttribute(str);
				try {
					addSchemaItem(cc.getSchema(input), attr.getName(), null, attr);
				} catch (Exception ex) {
					ignoreEx.addThrowable(ex);
				}
			}

			if (!ignoreEx.isEmpty()) {
				throw ignoreEx;
			}
		}
	}

	/**
	 * Creates a schema item for each attribute in the entry. If an attribute
	 * has child nodes (e.g. Attribute in its values) they are appended to the
	 * schema item as a child node.
	 * 
	 * @param entry
	 *            The Entry to use for creating Schema Items.
	 * @param cc
	 *            The ConnectorConfig where the SchemaItem will be added
	 * @param input
	 *            If true, add to Input Schema, else Output Schema.
	 * @throws IgnorableException
	 *             if any of the specified schema item names has incorrect
	 *             syntax this exception is thrown. It will contain the stack
	 *             traces of all occurred problems (if several items had
	 *             incorrect names).
	 */
	public static void convertEntryToSchemaHier(Entry entry, ConnectorConfig cc, boolean input) throws IgnorableException {
		if (!entry.hasChildNodes())
			return;
		SchemaConfig sc = cc.getSchema(input);
		sc.notifyChange(sc, "", MetamergeConfigChange.BEGIN_CHANGES);

		NodeList nl = entry.getChildNodes();
		IgnorableException ignoreEx = new IgnorableException();
		for (int i = 0, n = nl.getLength(); i < n; i++) {
			if (!(nl.item(i) instanceof Attribute))
				continue;
			Attribute attr = (Attribute) nl.item(i);
			SchemaItemConfig sic = null;
			try {
				sic = addSchemaItem(sc, attr.getNodeName(), null, attr);
			} catch (Exception ex) {
				ignoreEx.addThrowable(ex);
			}

			if (sic != null)
				addChildSchemaItem(attr, sic);
		}
		if (!ignoreEx.isEmpty()) {
			throw ignoreEx;
		}

		sc.notifyChange(sc, "", MetamergeConfigChange.END_CHANGES);
	}

	/**
	 * Creates a child schema item in the the schema item config for each
	 * Attribute value found in the attribute. This method calls itself for each
	 * Attribute value found to build the hierarchical tree of schema items.
	 * 
	 * @param attr
	 *            the attribute to be added.
	 * @param sic
	 *            the schema where the attribute will be added.
	 */
	public static void addChildSchemaItem(Attribute attr, SchemaItemConfig sic) {
		ContainerConfig children = sic.getChildSchemaList();
		NodeList nl = attr.getChildNodes();
		for (int i = nl.getLength() - 1; i >= 0; i--) {
			Object obj = nl.item(i);
			if (obj instanceof Attribute) {
				Attribute a = (Attribute) obj;
				try {

					SchemaItemConfig s = (SchemaItemConfig) children.getConfig(a.getName());
					if (s == null) {
						s = new SchemaItemConfigImpl();
						s.init();
						s.setName(a.getName());
						s.setAttributeName(a.getName());
						children.addConfig(s);
					}
					addChildSchemaItem(a, s);
				} catch (Exception e) {
					// already have this schema item
					SystemFunctions.doNothing();
				}
			} else if (obj != null) {
				if (obj instanceof AttributeValue)
					obj = ((AttributeValue)obj).getValue();
				sic.setSample(obj.toString());
				sic.setJavaClass(obj.getClass().getName());
			}
		}
	}

	/**
	 * Creates a schema item in the the schema config.
	 * 
	 * @param config
	 *            the schema config where the new item should be added.
	 * @param name
	 *            the name of the new item.
	 * @param syntax
	 *            the syntax for the schema items.
	 * @param attr
	 *            contains data which is populated to the schema item.
	 * @return the created SchemaItemConfig, or <b>null</b> if it is not
	 *         created.
	 * 
	 * @throws Exception
	 *             if the specified schema item name has incorrect syntax.
	 */
	public static SchemaItemConfig addSchemaItem(SchemaConfig config, String name, String syntax, Object attr) throws Exception {
		try {
			SchemaItemConfig sic = config.getItem(name);
			if (sic == null) {
				sic = config.newItem(name);
				sic.setAttributeName(name);
			}

			// -- retain syntax from querySchema unless we get more info
			if (syntax != null && syntax.length() > 0 && valuesDiffer(syntax, sic.getExternalSyntax())) {
				sic.setExternalSyntax(syntax);
			}

			if (attr != null) {
				if (attr instanceof Attribute) {
					Attribute a = (Attribute) attr;
					Object value = a.getValue(0);
					if (value == null && a.isDOMEnabled())
						value = a.getFirstChild();
					if (value instanceof AttributeValue)
						value = ((AttributeValue)value).getValue();

					if (value != null && ! (value instanceof Attribute) && valuesDiffer(value.toString(), sic.getSample()))
						sic.setSample(value.toString());

					if (value != null)
						value = value.getClass().getName();

					if (valuesDiffer(value, sic.getJavaClass()))
						sic.setJavaClass((String) value);

				} else {
					if (attr instanceof AttributeValue)
						attr = ((AttributeValue)attr).getValue();
					if (valuesDiffer(attr.toString(), sic.getSample()))
						sic.setSample(attr.toString());
					if (valuesDiffer(attr.getClass().getName(), sic.getJavaClass()))
						sic.setJavaClass(attr.getClass().getName());
				}
			}
			return sic;
		} catch (NameAlreadyBoundException nabe) {
			// Already have this schema item
			SystemFunctions.doNothing();
		}
		return null;
	}

	/**
	 * Checks if two values are different or not.
	 * 
	 * @param first
	 *            value to be compared.
	 * @param second
	 *            value to be compared.
	 * @return <b>true</b> if the provided values are different, <b>false</b>
	 *         otherwise.
	 */
	private static boolean valuesDiffer(Object first, Object second) {
		boolean different = true;
		if (first == null && second == null) {
			different = false;
		} else if (first != null) {
			different = !first.equals(second);
		}

		return different;
	}
}
