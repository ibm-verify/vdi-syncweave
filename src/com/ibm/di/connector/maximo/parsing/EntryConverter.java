/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.parsing;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.connector.maximo.core.AbstractMxConnMode;
import com.ibm.di.connector.maximo.core.SimpleTpaeIFConnector;
import com.ibm.di.connector.maximo.exception.MxConnExcedentSizeException;
import com.ibm.di.connector.maximo.exception.MxConnSchemaException;
import com.ibm.di.connector.maximo.exception.MxConnTypeConvertionException;
import com.ibm.di.connector.maximo.util.Dom;
import com.ibm.di.connector.maximo.util.XmlBuilder;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.Log;
import com.ibm.di.server.SearchCriteria;

/**
 * This class consists exclusively of static methods that operate on or return
 * {@link Entry} objects and DOM elements.
 * 
 * @since 7.1
 * @see Schema
 * @see SchemaElement
 */
public final class EntryConverter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Copies the attributes/values that compose the unique key of the specified
	 * MBO from <code>oldEntry</code> to <code>newEntry</code>.
	 * 
	 * @param rootMbo
	 *            MBO specification
	 * @param newEntry
	 *            entry target
	 * @param oldEntry
	 *            entry source
	 */
	public static void copyUniqueKeys(final SchemaElement rootMbo, final Entry newEntry, final Entry oldEntry) {

		final String[] attributeNames = oldEntry.getAttributeNames();

		for (final String name : attributeNames) {
			final SchemaElement mbo = rootMbo.getChild(name);

			if (mbo != null && mbo.isUniqueKey()) {
				newEntry.setAttribute(oldEntry.getAttribute(name));
			}
		}
	}

	/**
	 * Check if user has tried to change any unique keys
	 * 
	 * @param rootMbo
	 *            MBO specification
	 * @param newEntry
	 *            entry target
	 * @param oldEntry
	 *            entry source
	 * @param logger
	 *            Log object used to log warnings
	 */
	public static void checkForOverridenUniqueKeys(final SchemaElement rootMbo, final Entry newEntry, final Entry oldEntry,
			Log logger) {

		// and log a warning because this is not supported
		final String[] attributeNames = newEntry.getAttributeNames();

		for (final String name : attributeNames) {
			final SchemaElement mbo = rootMbo.getChild(name);

			if (mbo != null && mbo.isUniqueKey()) {
				// Unique keys can not be multi-valued
				String newValue = newEntry.getString(name);
				String orgValue = oldEntry.getString(name);

				// User has tried to map value to a unique key.
				// This can not be executed so we will log a warning.
				if (newValue != null && orgValue != null && !newValue.equals(orgValue)) {
					logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.CHANGE.UNIQUE.ATTRIBUTE",
							new Object[] { name, orgValue, newValue }));
				}
			}
		}
	}

	/**
	 * Converts an {@link Entry} object into XML content.
	 * 
	 * @param rootMbo
	 *            root MBO definition
	 * @param selectedMbo
	 *            schema element that defines the XML content
	 * @param entry
	 *            entry to be converted
	 * @param errorOnExcedentSize
	 *            indicates the behavior when a field's value exceeds the
	 *            maximun length: <code>true</code> if an exception should be
	 *            thrown, or <code>false</code> if the field's value should be
	 *            truncated
	 * @return XML content as a {@link String} object
	 * @throws MxConnSchemaException
	 *             if the specified entry does not comply to the schema
	 *             definition
	 */
	public static String entryToXml(final SchemaElement rootMbo, final SchemaElement selectedMbo, final Entry entry,
			final boolean errorOnExcedentSize) throws MxConnSchemaException {

		final XmlBuilder xml = new XmlBuilder(rootMbo.getName());
		final String[] attributeNames = entry.getAttributeNames();
		final List<SchemaElement> keys = selectedMbo.getUniqueKeyList();

		for (final String name : attributeNames) {
			final Object value = entry.getObject(name);
			final SchemaElement se = getSchemaElement(rootMbo, name);
			final String attr = getValueAndCheckForExcedentSize(se, value, errorOnExcedentSize);

			if (se.isAttribute()) {
				xml.setAttribute(name, attr);
			} else {
				xml.tag(name, attr);

				// remove every unique key we find
				keys.remove(se);
			}
		}

		// check for presence of unique keys
		checkKeys(rootMbo, keys);

		return xml.toString();
	}

	/**
	 * This method converts a hierarchical {@link Entry} object into a valid XML
	 * representation.
	 * 
	 * @param schema
	 *            schema of the current object structure
	 * @param entry
	 * @param errorOnExcedentSize
	 *            indicates the behavior when a field's value exceeds the
	 *            maximun length: <code>true</code> if an exception should be
	 *            thrown, or <code>false</code> if the field's value should be
	 *            truncated
	 * @param checkUniqueKeys
	 *            if <code>true</code> presence of unique keys will be checked
	 * @return XML representation of Entry object
	 * @throws MxConnSchemaException
	 *             if a unique key is not provided
	 * @throws MxConnExcedentSizeException
	 *             if a string field contains value longer than the maximum
	 *             allowed value for this field
	 */
	public static String entryToXml(final Schema schema, final Entry entry, final boolean errorOnExcedentSize,
			boolean checkUniqueKeys) throws MxConnSchemaException {
		SchemaElement mos = schema.getMos();

		if (checkUniqueKeys) {
			Set<SchemaElement> keys = getUniqueKeys(entry, schema);
			String parentName;

			for (SchemaElement uniqueKey : keys) {
				// name of MBO that should possess this unique key
				parentName = uniqueKey.getParent().getEntryPathRelativeTo(mos);

				// make sure all MBOs of this type has this unique key
				for (Node sibling : getSiblingsWithSameName(entry.getAttribute(parentName))) {
					if (!findNode(sibling, uniqueKey.getName())) {
						throw new MxConnSchemaException(SimpleTpaeIFConnector.getResHash().getString(
								"MXCONN.MISSING.ATTRIBUTE.UNIQUE.KEY", uniqueKey.getEntryPathRelativeTo(mos)));
					}

				}

			}
		}

		// Check length of all fields. Throw exception if errorOnExcedentSize is
		// checked, else just truncate them.
		for (String mboName : schema.getMboNameList()) {
			SchemaElement mbo = schema.getMboByName(mboName);

			String mboAttrName = mbo.getEntryPathRelativeTo(mos);
			Attribute mboAttr = entry.getAttribute(mboAttrName);

			// Iterate over all MBOs of specific type
			for (Node sibling : getSiblingsWithSameName(mboAttr)) {
				NodeList nl = sibling.getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					Node child = nl.item(i);
					if (child.getNodeType() != Node.ELEMENT_NODE)
						continue;

					String newValue = getValueAndCheckForExcedentSize(mbo.getChild(child.getLocalName()), child.getNodeValue(),
							errorOnExcedentSize);

					// The value isn't longer or it is truncated, in either way
					// update the child.
					child.setNodeValue(newValue);
				}
			}
		}

		final StringWriter sw = new StringWriter();
		OutputFormat format = new OutputFormat("xml", "UTF-8", true);
		format.setOmitXMLDeclaration(true);

		XMLSerializer serial = new XMLSerializer(sw, format);
		try {
			serial.asDOMSerializer();
			serial.serialize(entry);
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		return sw.toString();
	}

	/**
	 * 
	 * @param entry
	 *            Entry object
	 * @param schema
	 *            schema for chosen OS
	 * @return unique keys of all MBOs present in the <code>entry</code> object
	 */
	private static Set<SchemaElement> getUniqueKeys(Entry entry, Schema schema) {
		Set<SchemaElement> keys = new HashSet<SchemaElement>();
		for (String mbo : schema.getMboNameList()) {
			String path = schema.getMboByName(mbo).getEntryPathRelativeTo(schema.getMos());

			// Entry has at least one MBO of this type - add its unique keys
			if (entry.getAttribute(path) != null) {
				keys.addAll(schema.getMboByName(mbo).getUniqueKeyList());
			}
		}
		return keys;
	}

	/**
	 * @param parent
	 *            parent Node object
	 * @param name
	 *            name of child node to look for
	 * @return <code>true</code> if node with <code>name</code> was found and
	 *         contains text;<code>false</code> otherwise
	 */
	private static boolean findNode(Node parent, String name) {
		NodeList nl = parent.getChildNodes();
		Node n = null;
		for (int i = 0; i < nl.getLength(); i++) {
			n = nl.item(i);

			// empty values for unique keys are supported so don't check that
			if (n != null && n.getNodeType() == Element.ELEMENT_NODE && n.getLocalName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Converts a {@link SearchCriteria} object into XML content.
	 * 
	 * @param searchCriteria
	 *            {@link SearchCriteria} to be converted
	 * @param rootMbo
	 *            schema element that defines the XML content to which the
	 *            specified <code>criteria</code> will be converted
	 * @param errorOnExcedentSize
	 *            indicates the behavior when a field's value exceeds the
	 *            maximun length: <code>true</code> if an exception should be
	 *            thrown, or <code>false</code> if the field's value should be
	 *            truncated
	 * @return XML content as a {@link String} object
	 * @throws MxConnSchemaException
	 *             if
	 *             <code>criteria.getType() != SearchCriteria.SEARCH_AND</code>
	 * @throws MxConnSchemaException
	 *             if <tt>criteria</tt> has any SearchCriteria.rscSearch with
	 *             the <tt>match</tt> attribute different than
	 *             <tt>SearchCriteria.EXACT</tt> and
	 *             <tt>SearchCriteria.NOT_STRING</tt>
	 */
	@SuppressWarnings("unchecked")
	public static String searchCriteriaToXml(final SearchCriteria searchCriteria, final SchemaElement rootMbo,
			final boolean errorOnExcedentSize) throws MxConnSchemaException {

		final Vector criteria = searchCriteria.getCriteria();
		final XmlBuilder xml = new XmlBuilder(rootMbo.getName());

		for (final Iterator i = criteria.iterator(); i.hasNext();) {
			final Object obj = i.next();

			if (!(obj instanceof SearchCriteria.rscSearch)) {
				continue;
			}

			final SearchCriteria.rscSearch rsc = (SearchCriteria.rscSearch) obj;
			final SchemaElement se = getSchemaElement(rootMbo, rsc.name);

			// no XML attributes are allowed in search criteria
			if (se.isAttribute()) {
				throw new MxConnSchemaException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.ILLEGAL.ELEMENT.IN.CRITERIA",
						rsc.name));
			}

			xml.tag(rsc.name, getValueAndCheckForExcedentSize(se, rsc.value, errorOnExcedentSize));

			if (rsc.match == SearchCriteria.EXACT) {
				xml.setAttribute(rsc.name + "#operator", "=");
			} else if (rsc.match == SearchCriteria.NOT_STRING) {
				xml.setAttribute(rsc.name + "#operator", "!=");
			} else {
				throw new MxConnSchemaException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.USUPPORTED.OPERATOR"));
			}
		}
		return xml.toString();
	}

	/**
	 * Converts a {@link SearchCriteria} object into XML content.
	 * 
	 * @param searchCriteria
	 *            {@link SearchCriteria} to be converted
	 * @param schema
	 *            schema of the current Object structure
	 * @param errorOnExcedentSize
	 *            indicates the behavior when a field's value exceeds the
	 *            maximun length: <code>true</code> if an exception should be
	 *            thrown, or <code>false</code> if the field's value should be
	 *            truncated
	 * @return entry representation of the link criteria
	 * @throws MxConnSchemaException
	 *             if criteria type is different than <code>AND</code>
	 */
	@SuppressWarnings("unchecked")
	public static Entry searchCriteriaToEntry(final SearchCriteria searchCriteria, final Schema schema,
			final boolean errorOnExcedentSize) throws MxConnSchemaException {

		final Vector criteria = searchCriteria.getCriteria();
		Entry result = new Entry();

		for (final Iterator i = criteria.iterator(); i.hasNext();) {
			final Object obj = i.next();

			if (!(obj instanceof SearchCriteria.rscSearch)) {
				continue;
			}

			final SearchCriteria.rscSearch rsc = (SearchCriteria.rscSearch) obj;

			Attribute existCrit = result.getAttribute(rsc.name);
			Attribute newCrit = null;
			String newCritName = null;
			if (existCrit != null) {
				// We have the same attribute name, just add a sibling
				Attribute parent = existCrit.getParentNode();
				newCritName = existCrit.getLocalName();

				// No parent found
				if (parent == null) {
					throw new MxConnSchemaException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.CREATE.CRITERIA",
							rsc.name));
				}

				// Only first two criteria for attribute are evaluated so if we
				// have more than two throw exception
				if (getSiblingsWithSameName(existCrit).size() > 1) {
					throw new MxConnSchemaException(SimpleTpaeIFConnector.getResHash().getString(
							"MXCONN.CRITERIA.CONTAINS.MORE.THAT.TWO.REFERENCES.OF.ATTRIBUTE", rsc.name));
				}

				newCrit = result.createElement(newCritName);
				parent.appendChild(newCrit);
			} else {
				newCrit = result.newAttribute(rsc.name);
				newCritName = newCrit.getLocalName();
			}

			final String rootMBOName = schema.getRootMbo().getName();

			// Each Link Criteria can select attributes from the TOP TWO level
			// of MBOs. The criteria name must be in this format:
			// <rootMBO_name>.[<first_level_childMBO_name>].<attr_name>
			// If this format is not followed we throw an exception and abort.
			Node parentNode = newCrit.getParentNode();
			if (!rsc.name.startsWith(rootMBOName + ".") || parentNode == null || 
					(parentNode.getParentNode() == null && !rootMBOName.equals(parentNode.getLocalName())) || 
					(parentNode.getParentNode() != null && !rootMBOName.equals(parentNode.getParentNode().getLocalName()))) {
				throw new MxConnSchemaException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.ILLEGAL.ELEMENT.IN.CRITERIA",
						rsc.name));
			}

			// Construct the MBO name.
			// Schema.getMboByName expects MBO names in this format:
			// <parent>@<first_level_child>@<second_level_child>
			String mboName = rootMBOName;
			if (!parentNode.getLocalName().equals(schema.getRootMbo().getName())) {
				mboName = rootMBOName + "@" + parentNode.getLocalName();
			}
			final SchemaElement se = getSchemaElement(schema.getMboByName(mboName), newCritName);

			// Name is defined as XML attribute in the retrieved schema
			if (se.isAttribute()) {
				throw new MxConnSchemaException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.ILLEGAL.ELEMENT.IN.CRITERIA",
						rsc.name));
			}

			newCrit.addValue(getValueAndCheckForExcedentSize(se, rsc.value, errorOnExcedentSize));

			// "starts with", "ends with" and "contains" support only string
			if ((rsc.match == SearchCriteria.SUBSTRING || rsc.match == SearchCriteria.INITIAL_STRING || rsc.match == SearchCriteria.FINAL_STRING)
					&& (!se.getClassName().equals(String.class.getName()))) {
				throw new MxConnSchemaException(SimpleTpaeIFConnector.getResHash().getString(
						"MXCONN.INVALID.MATCH.OPERATOR.FOR.STRING.ATTRIBUTE", rsc.name));
			}

			// Support for all operators.
			// Note: SearchCriteria.SUBSTRING is supported implicitly i.e having
			// no 'operator' XML attribute is interpreted by Maximo as a
			// substring match.
			if (rsc.match == SearchCriteria.EXACT) {
				newCrit.setAttribute("operator", "=");
			} else if (rsc.match == SearchCriteria.NOT_STRING) {
				newCrit.setAttribute("operator", "!=");
			} else if (rsc.match == SearchCriteria.INITIAL_STRING) {
				newCrit.setAttribute("operator", "SW"); // starts with
			} else if (rsc.match == SearchCriteria.FINAL_STRING) {
				newCrit.setAttribute("operator", "EW"); // ends with
			} else if (rsc.match == SearchCriteria.LESS_THAN) {
				newCrit.setAttribute("operator", "<");
			} else if (rsc.match == SearchCriteria.LESS_THAN_OR_EQUAL) {
				newCrit.setAttribute("operator", "<=");
			} else if (rsc.match == SearchCriteria.GREATER_THAN) {
				newCrit.setAttribute("operator", ">");
			} else if (rsc.match == SearchCriteria.GREATER_THAN_OR_EQUAL) {
				newCrit.setAttribute("operator", ">=");
			}
		}
		return result;
	}

	/**
	 * @param sibling
	 *            Attribute object
	 * @return this sibling plus all siblings with with the same name at the
	 *         same level of hierarchy
	 */
	private static ArrayList<Node> getSiblingsWithSameName(Attribute sibling) {
		ArrayList<Node> nl = new ArrayList<Node>();

		if (sibling == null)
			return nl;

		nl.add(sibling);
		Node n = sibling;
		do {
			n = n.getNextSibling();
			if (n != null && n.getNodeType() == Element.ELEMENT_NODE && n.getLocalName().equals(sibling.getLocalName())) {
				nl.add(n);
			}
		} while (n != null);
		return nl;
	}

	/**
	 * Converts XML content into an {@link Entry} object.
	 * 
	 * @param rootMbo
	 *            root MBO definition
	 * @param selectedMbo
	 *            schema element that defines the XML content
	 * @param xmlElement
	 *            XML element to be converted
	 * @return {@link Entry} object converted from XML content
	 * @throws MxConnSchemaException
	 *             if the specified XML content does not comply to the schema
	 *             definition
	 */
	public static Entry xmlToEntry(final SchemaElement rootMbo, final SchemaElement selectedMbo, final Node xmlElement)
			throws MxConnSchemaException {

		final Entry entry = new Entry();
		Node currentNode = xmlElement;
		SchemaElement mbo = selectedMbo;

		while (!mbo.equals(rootMbo.getParent())) {
			collectElements(entry, currentNode, mbo, rootMbo);
			currentNode = currentNode.getParentNode();
			mbo = mbo.getParent();
		}

		return entry;
	}

	/**
	 * This method circles trough all attributes (except leafs) of a
	 * hierarchical entry and if some of the attributes has operation Add,
	 * Modify or Delete the proper Maximo action will be added as XML attribute.
	 * Also all parents in the above hierarchy will be marked with "Change"
	 * action.
	 * 
	 * @param e
	 *            Entry object
	 */
	public static void setAttributeActions(Entry e) {
		// Returns names of all leave nodes, no duplicates
		String[] attrNames = e.getAttributeNames();

		for (int i = 0; i < attrNames.length; i++) {
			// Get all siblings with this name
			Attribute leaf = e.getAttribute(attrNames[i]);
			for (Node sibling : getSiblingsWithSameName(leaf.getParentNode())) {
				if (sibling instanceof Attribute) {
					Attribute attr = (Attribute) sibling;

					if (attr.getOper() != Attribute.ATTRIBUTE_REPLACE && attr.getOper() != Attribute.ATTRIBUTE_UNCHANGED) {
						attr.setAttribute(AbstractMxConnMode.ACTION_ATTR, attrOperToAction(attr.getOper()));
					}

					// all parents above in the hierarchy are marked as changed
					if (attr.getAttribute(AbstractMxConnMode.ACTION_ATTR) != null) {
						Attribute parent = attr.getParentNode();
						while (parent != null) {
							parent.setAttribute(AbstractMxConnMode.ACTION_ATTR, AbstractMxConnMode.CHANGE_ACTION);
							parent = parent.getParentNode();
						}
					}
				}
			}
		}
	}

	/**
	 * Possible return values and their interpretation by Tpae are as follows:
	 * <li>Add - Add the child record; if it exists, an error results.</li> <li>
	 * Delete - Delete the child record; if it does not exist, an error results.
	 * </li> <li>Change - Update the child record; if it does not exist, an
	 * error results.</li> <li>Null - If the child record exists, update it; if
	 * child record does not exist, add it.
	 * 
	 * @param attrOper
	 *            Attribute operation in terms of Tivoli Directory Integrator
	 * @return value for "action" attribute for this Attribute in terms of Tpae
	 */
	private static String attrOperToAction(char attrOper) {
		String attrAction = null;

		switch (attrOper) {
		case Entry.OP_ADD:
			attrAction = AbstractMxConnMode.ADD_ACTION;
			break;
		case Entry.OP_DEL:
			attrAction = AbstractMxConnMode.DELETE_ACTION;
			break;
		case Entry.OP_MOD:
			attrAction = AbstractMxConnMode.CHANGE_ACTION;
			break;
		}

		return attrAction;
	}

	private static void checkKeys(final SchemaElement rootMbo, final List<SchemaElement> keys) throws MxConnSchemaException {

		// If this is empty we have found all unique attributes and
		// removed them from the keys list
		if (keys.isEmpty()) {
			return;
		}
		
		// Only verify that the required keys are present
		List<SchemaElement> requiredKeys = new ArrayList<SchemaElement>();
		for (SchemaElement e:keys) {
			if (e.isRequired())
				requiredKeys.add(e);
		}
		if (requiredKeys.isEmpty()) {
			return;
		}

		// Create a list with full paths for the unspecified keys
		final StringBuilder sb = new StringBuilder(requiredKeys.get(0).getPathRelativeTo(rootMbo));
		for (int i = 1; i < requiredKeys.size(); i++) {
			sb.append(", ");
			sb.append(requiredKeys.get(i).getPathRelativeTo(rootMbo));
		}

		throw new MxConnSchemaException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.UNIQUE.KEY.NOT.FOUND", sb.toString()));
	}

	private static void collectAttributes(final Entry entry, final Node rootNode, final SchemaElement schemaElement,
			final SchemaElement rootMbo) throws MxConnSchemaException {

		final NamedNodeMap attributes = rootNode.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType() != Node.ATTRIBUTE_NODE) {
				continue;
			}

			final SchemaElement e = schemaElement.getChild(node.getNodeName());
			if (e == null) {
				continue;
			}

			final Object value = e.valueOf(Dom.getTextValue(node));
			entry.addAttributeValue(e.getPathRelativeTo(rootMbo), value);
		}
	}

	private static void collectElements(final Entry entry, final Node rootNode, final SchemaElement schemaElement,
			final SchemaElement rootMbo) throws MxConnSchemaException {

		final NodeList nodes = rootNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final Node node = nodes.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			final SchemaElement e = schemaElement.getChild(node.getNodeName());
			if (e == null || e.isMboDefinition()) {
				continue;
			}

			final Object value = e.valueOf(Dom.getTextValue(node));
			entry.addAttributeValue(e.getPathRelativeTo(rootMbo), value);

			collectAttributes(entry, node, e, rootMbo);
		}
	}

	/**
	 * @param sc
	 *            parent SchemaElement object
	 * @param name
	 *            name of requested child
	 * @return valid child of particular SchemaElement
	 * @throws MxConnSchemaException
	 *             if the child is a MBO
	 */
	private static SchemaElement getSchemaElement(final SchemaElement sc, final String name) throws MxConnSchemaException {
		final SchemaElement child = sc.getChild(name);
		if (child == null) {
			throw new MxConnSchemaException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.ELEMENT.NOT.FOUND", name));
		}
		if (child.isMboDefinition()) {
			throw new MxConnSchemaException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.ILLEGAL.ELEMENT", name));
		}

		return child;
	}

	/**
	 * Convert <code>value</code> to the proper class and in case of String and
	 * <code>errorOnExcedentSize</code> is <code>true</code> its size is also
	 * validated.
	 * 
	 * @param se
	 *            SchemaElement object
	 * @param value
	 *            value to be converted and checked
	 * @param errorOnExcedentSize
	 *            indicates the behavior when a field's value exceeds the
	 *            maximun length: <code>true</code> if an exception should be
	 *            thrown, or <code>false</code> if the field's value should be
	 *            truncated
	 * @throws MxConnTypeConvertionException
	 *             if the specified <tt>value</tt> can not be converted
	 * @throws MxConnExcedentSizeException
	 *             if the <tt>value</tt> exceeds the maximum size defined and
	 *             this schema element is configured to make such validation
	 */
	private static String getValueAndCheckForExcedentSize(SchemaElement se, Object value, boolean errorOnExcedentSize)
			throws MxConnExcedentSizeException, MxConnTypeConvertionException {

		// The element is not part of the read schema. This should not happen.
		if (se == null) {
			if (value == null)
				return null;
			return value.toString();
		}
		String result = se.toString(value);
		if (String.class.getName().equals(se.getClassName()) && result != null && result.length() > se.getSize()) {
			if (errorOnExcedentSize) {
				throw new MxConnExcedentSizeException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.EXCEDENT.SIZE",
						new Object[] { se.getName(), se.getSize() }), se.getName(), se.getSize(), result.length());
			}
			result = result.substring(0, se.getSize());
		}
		return result;
	}
}
