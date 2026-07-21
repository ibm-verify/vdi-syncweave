/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.schema.internal.server;

import java.io.InputStream;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.util.DOMUtils;

/**
 * Remote view of local XML Schema documents. References to XML Schema documents
 * are translated so that they can be resolved by web clients.
 * 
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class SchemaRemoteView {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * XML Schema namespace URI.
	 */
	private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";

	/**
	 * XML Schema Instance namespace URI.
	 */
	private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

	private Document doc;

	/**
	 * @param schema
	 * @throws Exception
	 */
	public SchemaRemoteView(InputStream schema) throws Exception {
		DocumentBuilder parser = DOMUtils.getDOMParser();
		try {
			doc = parser.parse(schema);
		} finally {
			schema.close();
		}
	}

	/**
	 * @param contextUri
	 *            the URI to resolve relative paths against.
	 * @return Schema document in which all references to other documents can be
	 *         resolved by web clients.
	 * @throws Exception
	 *             If an error occurs while processing the document.
	 */
	public String getRemoteSchemaAsString(String contextUri) throws Exception {
		Element root = doc.getDocumentElement();

		// process all "import", "include" and "redefine" elements
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node node = children.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType() && XSD_NS.equals(node.getNamespaceURI())) {
				String name = node.getLocalName();
				if ("include".equals(name) || "import".equals(name) || "redefine".equals(name)) {
					updateSchemaLocation((Element) node, contextUri);
				}
			}
		}

		return DOMUtils.elementToString(root);
	}

	/**
	 * @param schemaFile
	 *            The name of the XSD file, e.g. touchpoint.xsd. See the schema
	 *            folder inside the jar of the TP server bundle.
	 * @param contextUri
	 * @return URI of the schema document which can be resolved by web clients,
	 *         e.g. "http://www.example.com/tp/schema/touchpoint.xsd" instead of
	 *         just "touchpoint.xsd". The URI can be used as the value of the
	 *         "schemaLocation" attribute of the "include", "import" or
	 *         "redefine" elements of an XML Schema document.
	 */
	public static String getRemoteSchemaLocation(String schemaFile, String contextUri) {
		if (schemaFile.startsWith("http://") || schemaFile.startsWith("https://")) {
			return schemaFile;
		} else {
			return URI.create(contextUri + "/" + schemaFile).normalize().toString();
		}
	}

	/**
	 * Update the schemaLocation attribute of an element so that all references
	 * to other documents can be resolved by web clients.
	 * 
	 * @param e
	 *            Element of a XML Schema DOM tree which can potentially have a
	 *            schemaLocation attribute. For example include, import or
	 *            redefine XSD element.
	 * @param contextUri
	 */
	private void updateSchemaLocation(Element e, String contextUri) {

		/*
		 * See "4.2 Layer 2: Schema Documents, Namespaces and Composition" for
		 * definition of the "include", "redefine" and "import" elements :
		 * http://www.w3.org/TR/xmlschema-1/#layer2
		 */

		Attr schemaLocationAttr = e.getAttributeNodeNS(XSD_NS, "schemaLocation");
		if (schemaLocationAttr == null) {
			schemaLocationAttr = e.getAttributeNode("schemaLocation");
		}

		String schemaLocation = schemaLocationAttr.getValue();
		if (schemaLocation != null && !"".equals(schemaLocation)) {
			String remoteSchemaLocation = getRemoteSchemaLocation(schemaLocation, contextUri);
			schemaLocationAttr.setValue(remoteSchemaLocation);
		}
	}

	/**
	 * @param xsiSchemaLocationValue
	 *            Value of xsi:schemaLocation attribute.
	 * @return Translated value where all references to documents can be
	 *         resolved by web clients.
	 */
	public static String getRemoteXsiSchemaLocation(String xsiSchemaLocationValue, String contextUri) {

		StringBuilder result = new StringBuilder();

		/*
		 * The xsi:schemaLocation attribute is a list of pairs; each pair
		 * consists of schema namespace followed by schema location; whitespace
		 * is used as separator. See
		 * "4.3.2 How schema definitions are located on the Web" :
		 * http://www.w3.org/TR/xmlschema-1/#schema-loc
		 */

		String[] tokens = xsiSchemaLocationValue.split("\\s");

		// whether the next token is location (the alternative is namespace)
		boolean expectLocationToken = false;

		for (int i = 0; i < tokens.length; ++i) {
			String token = tokens[i];
			// skip empty tokens
			if (!"".equals(tokens[i])) {
				if (expectLocationToken) {
					// this token is location
					String remoteSchemaLocation = getRemoteSchemaLocation(token, contextUri);
					result.append(remoteSchemaLocation);
				} else {
					// the token is namespace
					result.append(token);
				}
				// add separator
				result.append(' ');
				expectLocationToken = !expectLocationToken;
			}
		}

		return result.toString().trim();
	}

	/**
	 * Update the xsi:schemaLocation attribute of the specified element so that
	 * all references to documents can be resolved by web clients.
	 * 
	 * @param e
	 *            Element which can potentially have a xsi:schemaLocation
	 *            attribute.
	 * @throws DOMException
	 *             Error reported by the DOM API.
	 */
	public static void updateXsiSchemaLocation(Element e, String contextUri) throws DOMException {

		// processes only first level
		Attr schemaLocationAttr = e.getAttributeNodeNS(XSI_NS, "schemaLocation");
		if (schemaLocationAttr == null) {
			schemaLocationAttr = e.getAttributeNode("schemaLocation");
		}

		if (schemaLocationAttr != null) {
			String schemaLocation = schemaLocationAttr.getValue();
			if (schemaLocation != null && !"".equals(schemaLocation)) {
				String remoteSchemaLocation = getRemoteXsiSchemaLocation(schemaLocation, contextUri);
				schemaLocationAttr.setValue(remoteSchemaLocation);
			}
		}
	}

}
