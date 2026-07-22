/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import static com.ibm.di.cdm.core.CDMConstants.CDM_PREFIX;

import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.connector.IdMLConnector;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.idml.CloseIdMLFC;
import com.ibm.di.fc.idml.IdMLConstants;
import com.ibm.di.fc.idml.ItdiBook;
import com.ibm.di.fc.idml.OpenIdMLFC;
import com.ibm.di.fc.idml.RollingIdMLFC;
import com.ibm.di.fc.idml.IdMLConstants.Operations;
import com.ibm.di.parser.xml.XMLParser2;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;

/**
 * The Parser used to parse IdML XML documents (or books). It is designed only
 * for reading IdML files. For more information how they can be written, see
 * {@link OpenIdMLFC}, {@link CloseIdMLFC}, {@link RollingIdMLFC} and
 * {@link IdMLConnector}.
 * 
 * @since 7.1
 */
public class IdMLParser extends XMLParser2 {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * IdML Type attribute name. Its values are 'delta' and 'refresh'.
	 */
	private static final String IDML_MODE_ATTR = "$idmlType";

	/**
	 * Attribute name of Class Type of a CI/Relationship.
	 */
	private static final String CLASS_TYPE_ATTR = "$classType";

	/**
	 * Attribute name for the artifact type, which determines whether the read
	 * element is a CI or a Relationship.
	 */
	private static final String ARTIFACT_TYPE_ATTR = "$artifactType";

	/**
	 * Attribute name of CDM Schema Version.
	 */
	private static final String CDM_VERSION_ATTR = "$cdmVersion";

	/**
	 * The name of the XML attribute for the CDM version used in the IdML.
	 */
	private static final String CDM_VERSION_XML_ATTR_NAME = "CDMSchemaVersion";

	/**
	 * Prefix used for all MSS attributes.
	 */
	private static final String MSS_PREFIX = "mss.";

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "idmlparser";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * CDM Schema Version
	 */
	private String cdmSchemaVersion;

	/**
	 * An Entry which will store the MSS data.
	 */
	private Entry mss;

	/**
	 * Default constructor.
	 */
	public IdMLParser() {
		Trace.entrymid(this, "IdMLParser");
		Trace.exitmid(this, "IdMLParser");
	}

	/**
	 * {@inheritDoc}
	 */
	public void initInput() throws Exception {
		// Initialize xPaths to parse IdML XML elements
		nsMap = "i=" + IdMLConstants.IDML_NAMESPACE + " | c=" + IdMLConstants.CDM_NAMESPACE;
		xPathStr = "i:idml/i:source/c:* | " //
				+ "i:idml/i:operationSet/i:create/c:CDM-ER-Specification/c:* | " //
				+ "i:idml/i:operationSet/i:modify/c:CDM-ER-Specification/c:* | " //
				+ "i:idml/i:operationSet/i:delete/c:CDM-ER-Specification/c:* | " //
				+ "i:idml/i:operationSet/i:refresh/i:create/c:CDM-ER-Specification/c:* ";

		super.initInput();
	}

	/**
	 * Reads the data from the IdML book and returns it, one artifact at a time
	 * (i.e. CI or Relationship). Also, each returned entry contains the data of
	 * the MSS owning that artifact.
	 * 
	 * @return the Entry object containing the IdML elements.
	 * @throws Exception
	 *             if unable to parse an IdML file.
	 */
	public Entry readEntry() throws Exception {
		Trace.entrymax(this, "readEntry");
		if (mss == null) {
			mss = readMSSInformation(super.readEntry());
		}

		// clear the returned entry
		Entry returnEntry = null;

		Entry idmlEntry = super.readEntry();
		if (idmlEntry != null) {
			returnEntry = new Entry();
			List<QName> qualifiedPath = getCurrentEntryPath();
			if (qualifiedPath.size() < 4) { // no such xPath
				throw new Exception(sResHash.getString("PARSER.IDML.INVALID.XML.ERR"));
			}

			addIdMLModeAndOperation(returnEntry, qualifiedPath);

			// Add class type
			Node artifact = idmlEntry.getChildNodes().item(0);
			String classType = artifact.getLocalName();
			if (classType != null && !classType.equals("")) {
				returnEntry.setAttribute(CLASS_TYPE_ATTR, classType);
			} else {
				throw new Exception(sResHash.getString("PARSER.IDML.INVALID.XML.ERR"));
			}

			// Determine whether it is a CI/Relationship
			NamedNodeMap attributes = artifact.getAttributes();
			if (!isRelationship(attributes)) {
				returnEntry.setAttribute(ARTIFACT_TYPE_ATTR, IdMLConstants.ARTIFACT_CI);
				addCiAttributes(returnEntry, attributes, "$");
				addCiElements(returnEntry, artifact, CDM_PREFIX);
			} else {
				returnEntry.setAttribute(ARTIFACT_TYPE_ATTR, IdMLConstants.ARTIFACT_RELATIONSHIP);
				addRelationshipAttributes(returnEntry, attributes);
			}

			// Add the CDM version to the returned Entry
			if (cdmSchemaVersion != null && !cdmSchemaVersion.equals("")) {
				returnEntry.setAttribute(CDM_VERSION_ATTR, cdmSchemaVersion);
			}

			// Add the MSS data to the returned Entry
			String[] mssData = mss.getAttributeNames();
			for (String attr : mssData) {
				returnEntry.setAttribute(attr, mss.getString(attr));
			}
		}

		Trace.exitmax(this, "readEntry", returnEntry);
		return returnEntry;
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 * 
	 */
	public String getVersion() {
		return "1.1-di7.1.1 %I% 20%E%";
	}

	/**
	 * {@inheritDoc}
	 */
	public void closeParser() throws Exception {
		mss = null;
		super.closeParser();
	}

	/**
	 * Retrieves the MSS data from the entry read by the XML Parser.
	 * 
	 * @param mssEntry
	 *            the incoming entry
	 * @return a map containing the MSS data.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private Entry readMSSInformation(Entry mssEntry) throws Exception {
		Entry mssData = new Entry();

		// check if this is indeed the MSS element
		if (getCurrentEntryPath().size() == 2) {

			NodeList nodeList = mssEntry.getChildNodes();
			// 'source' must have only one child element i.e.
			// cdm:process.ManagementSoftwareSystem
			if (nodeList.getLength() != 1) {
				throw new Exception(sResHash.getString("PARSER.IDML.INVALID.XML.ERR"));
			}

			Node mssElement = nodeList.item(0);
			addCiAttributes(mssData, mssElement.getAttributes(), MSS_PREFIX);
			addCiElements(mssData, mssElement, MSS_PREFIX);
		} else {
			throw new Exception(sResHash.getString("PARSER.IDML.INVALID.XML.ERR"));
		}
		return mssData;
	}

	/**
	 * Retrieves the IdML mode and current operation and adds them to the
	 * provided entry.
	 * 
	 * @param entry
	 *            provided entry.
	 * @param qualifiedPath
	 *            the qualified path from where the needed information is taken.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void addIdMLModeAndOperation(Entry entry, List<QName> qualifiedPath) throws Exception {
		String token = qualifiedPath.get(2).getLocalPart();
		String idmlMode = null;
		String idmlOperation = null;
		if (isValidIdMLOperation(token)) {
			// this is a 'delta' IdML
			idmlMode = ItdiBook.DELTA_TYPE_IDML;
			idmlOperation = token;
		} else if (isRefreshIdML(token)) {
			// this is a 'refresh' IdML
			idmlMode = ItdiBook.REFRESH_TYPE_IDML;
			idmlOperation = Operations.CREATE.name();
		} else {
			throw new Exception(sResHash.getString("PARSER.IDML.INVALID.XML.ERR"));
		}
		entry.setAttribute(IDML_MODE_ATTR, idmlMode.toLowerCase(Locale.ENGLISH));
		entry.setAttribute(Operations.PARAM_NAME, idmlOperation.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * Checks if the provided operation is a valid IdML operation.
	 * 
	 * @param operationName
	 *            the name of the operation.
	 * @return true if there is an IdML operation with that name
	 */
	private boolean isValidIdMLOperation(String operationName) {
		boolean isValid = false;
		try {
			Operations.valueOf(operationName.toUpperCase(Locale.ENGLISH));
			isValid = true;
		} catch (IllegalArgumentException iae) {
			isValid = false;
		}
		return isValid;
	}

	/**
	 * Checks if the provided type is of a 'refresh' IdML.
	 * 
	 * @param type
	 *            the type literal.
	 * @return true if this is a refresh IdML, false otherwise.
	 */
	private boolean isRefreshIdML(String type) {
		return ItdiBook.REFRESH_TYPE_IDML.equalsIgnoreCase(type);
	}

	/**
	 * Check if the attributes of an artifacts match those of a Relationship.
	 * This method can be used for distinguishing between CIs and Relationships.
	 * 
	 * @param artifactAttrs
	 *            attribute map.
	 * @return true if the Relationship conditions are met.
	 */
	private boolean isRelationship(NamedNodeMap artifactAttrs) {
		return artifactAttrs.getNamedItem(IdMLConstants.RELATIONSHIP_SOURCE_ATTR) != null
				&& artifactAttrs.getNamedItem(IdMLConstants.RELATIONSHIP_TARGET_ATTR) != null;
	}

	/**
	 * Adds the attributes of the CI to the provided entry.
	 * 
	 * @param entry
	 *            entry for the attributes.
	 * @param ciAttrs
	 *            a list of attributes.
	 * @param attrPrefix
	 *            prefix used for the attribute names when added to the entry.
	 */
	private void addCiAttributes(Entry entry, NamedNodeMap ciAttrs, String attrPrefix) {
		for (int i = 0; i < ciAttrs.getLength(); i++) {
			Attr attr = (Attr) ciAttrs.item(i);
			String attrName = attr.getLocalName();
			String attrValue = attr.getValue();
			if (attr.getName().startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
				// skip namespace attributes
				continue;
			} else if (CDM_VERSION_XML_ATTR_NAME.equals(attrName) && cdmSchemaVersion == null) {
				// only used for MSS
				cdmSchemaVersion = attr.getValue();
				continue;
			}

			entry.setAttribute(prefixAttrName(attrName, attrPrefix), attrValue);
			debug(sResHash.getString("PARSER.IDML.ELEMENT.NODE", attrName));
		}
	}

	/**
	 * Add the Relationship's attributes to the provided entry.
	 * 
	 * @param entry
	 *            where the data will be stored.
	 * @param relnAttrs
	 *            the XML attributes of the Relationship.
	 */
	private void addRelationshipAttributes(Entry entry, NamedNodeMap relnAttrs) {
		String sourceId = ((Attr) relnAttrs.getNamedItem(IdMLConstants.RELATIONSHIP_SOURCE_ATTR)).getValue();
		String targetId = ((Attr) relnAttrs.getNamedItem(IdMLConstants.RELATIONSHIP_TARGET_ATTR)).getValue();

		entry.setAttribute(IdMLConstants.RELATIONSHIP_SOURCE_ATTR, sourceId);
		entry.setAttribute(IdMLConstants.RELATIONSHIP_TARGET_ATTR, targetId);

		debug(sResHash.getString("PARSER.IDML.ELEMENT.NODE", sourceId));
		debug(sResHash.getString("PARSER.IDML.ELEMENT.NODE", targetId));
	}

	/**
	 * Add a prefix before the attribute name. If either null or empty string is
	 * passed as prefix, the original attribute name is returned. Also, if the
	 * attribute is already prefixed, it again is returned unmodified.
	 * 
	 * @param attrName
	 *            attribute name.
	 * @param prefix
	 *            the prefix.
	 * @return prefixed attribute name.
	 */
	private String prefixAttrName(String attrName, String prefix) {
		if (prefix != null && !"".equals(prefix) && !attrName.startsWith(prefix)) {
			attrName = prefix + attrName;
		}

		return attrName;
	}

	/**
	 * Extracts the XML child elements of the CI and add them to the provided
	 * entry.
	 * 
	 * @param entry
	 *            where the read data is stored.
	 * @param ciNode
	 *            the CI's node.
	 * @param prefix
	 *            prefix used for the data added to the entry.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void addCiElements(Entry entry, Node ciNode, String prefix) throws Exception {
		NodeList elements = ciNode.getChildNodes();
		for (int j = 0; j < elements.getLength(); j++) {
			Node element = elements.item(j);
			if (!IdMLConstants.CDM_NAMESPACE.equals(element.getNamespaceURI())) {
				continue;
			}
			debug(sResHash.getString("PARSER.IDML.PARENTNODE.INFO", new Object[] { ciNode.getLocalName(), element.getLocalName() }));

			if ("extension".equals(element.getLocalName())) {
				// extended attributes
				NodeList extendedAttrs = element.getChildNodes();
				for (int i = 0; i < extendedAttrs.getLength(); i++) {
					Node extendedAttrNode = extendedAttrs.item(i);
					NamedNodeMap xmlAttrs = extendedAttrNode.getAttributes();
					if (xmlAttrs != null) {
						Attr xmlAttr = (Attr) xmlAttrs.getNamedItem("name");
						if (xmlAttr != null) {
							// the extended attributes format is
							// 'cdm:extattr:<name>'
							String extendedAttrName = prefix + extendedAttrNode.getLocalName() + ":" + xmlAttr.getValue();
							String extendedAttrValue = extendedAttrNode.getNodeValue();
							entry.setAttribute(extendedAttrName, extendedAttrValue);
						} else {
							throw new Exception(sResHash.getString("PARSER.IDML.INVALID.XML.ERR"));
						}
					} else {
						throw new Exception(sResHash.getString("PARSER.IDML.INVALID.XML.ERR"));
					}
				}
			} else {
				// normal attribute
				entry.setAttribute(prefix + element.getLocalName(), element.getNodeValue());
			}
		}
	}

}
