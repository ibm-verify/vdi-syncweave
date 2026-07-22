/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Vector;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;

/**
 *
 * The SOAP Parser reads and writes SOAP XML documents. The Parser converts SOAP
 * XML documents to or from entry objects in a simple, straightforward fashion.
 *
 */
public class SOAPParser extends XMLParser {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component's properties.
	 */
	private static final String PROPERTIES_FILE = "soapparser";

	/**
	 * Attribute name.
	 */
	private final static String SOAP_ENV = "SOAP-ENV:Envelope";
	/**
	 * Attribute name.
	 */
	private final static String SOAP_BODY = "SOAP-ENV:Body";
	/**
	 * Attribute name.
	 */
	private final static String SOAP_CALL = "SOAP_CALL";
	/**
	 * Attribute name.
	 */
	private final static String SOAP_REPLY = "SOAP_REPLY_XML";
	/**
	 * Attribute name.
	 */
	private final static String SOAP_NS_ENV = "http://schemas.xmlsoap.org/soap/envelope/";
	/**
	 * Attribute name.
	 */
	private final static String SOAP_NS_XSI = "http://www.w3.org/1999/XMLSchema-instance";
	/**
	 * Attribute name.
	 */
	private final static String SOAP_NS_XSD = "http://www.w3.org/1999/XMLSchema";
	/**
	 * Attribute name.
	 */
	private final static String SOAP_NS_ENC = "http://schemas.xmlsoap.org/soap/encoding/";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Creates SOAPParser.
	 */
	public SOAPParser() {

		super();
		Trace.entrymid(this, "SOAPParser");
		Trace.exitmid(this, "SOAPParser");
	}

	/**
	 * {@inheritDoc}
	 */
	public void initOutput() throws Exception {
		if (getWriter() == null) {
			throw new Exception(sResHash
					.getString("PARSER.SOAP.WRITE.NOINPUT.ERROR"));
		}
	}

	/**
	 * Sets the current input stream and initializes the parser.
	 *
	 * @param request
	 *            the requested <code>Reader</code>.
	 * @return first read entry containing the parsed attributes.
	 * @throws Exception
	 *             if I/O error occurs.
	 */
	public Entry parseRequest(Object request) throws Exception {

		if (request instanceof InputStream)
			setInputStream((InputStream) request);
		if (request instanceof Reader)
			setInputStream((Reader) request);
		if (request instanceof String)
			setInputStream(new StringReader((String) request));

		initParser();

		return readEntry();
	}

	/**
	 * Finds node element by given name.
	 *
	 * @param list
	 *            <code>NodeList</code> to search into.
	 * @param name
	 *            the name of the searched node.
	 * @return first <code>Element</code> node named <code>name</code>.
	 */
	public Node findNamedElement(NodeList list, String name) {
		for (int i = 0; i < list.getLength(); i++) {
			Node x = list.item(i);
			if ((x.getNodeType() == Node.ELEMENT_NODE) && (x.getNodeName().equalsIgnoreCase(name))) {
				return x;
			}
		}

		return null;
	}

	/**
	 * Return list containing all children of the <code>parent</code> node.
	 *
	 * @param parent
	 *            the parent <code>Node</code>.
	 * @return <code>Vector</code> with all <code>Element</code> children
	 */
	public Vector<Element> getElementList(Node parent) {
		NodeList list = parent.getChildNodes();
		Vector<Element> v = new Vector<Element>();

		for (int i = 0; i < list.getLength(); i++) {
			Node x = list.item(i);
			if (x.getNodeType() == Node.ELEMENT_NODE) {
				v.add((Element) x);
			}
		}

		return v;
	}

	/**
	 * Returns <code>Node</code> object by given index.
	 *
	 * @param parent
	 *            the parent <code>Node</code>.
	 * @param index
	 *            the index of the desired <code>Node</code>.
	 * @return <code>Node</code> object at <code>index</code> position.
	 */
	public Node getElement(Node parent, int index) {
		NodeList list = parent.getChildNodes();

		for (int i = 0; i < list.getLength(); i++) {
			Node x = list.item(i);
			if ((x.getNodeType() == Node.ELEMENT_NODE) && (index-- <= 0)) {
				return x;
			}
		}

		return null;
	}

	/**
	 * Gets the text of the first child of a given <code>Node</code>.
	 *
	 * @param parent
	 *            the parent <code>Node</code>
	 * @return string containing the text value.
	 */
	public Node getFirstText(Node parent) {
		NodeList list = parent.getChildNodes();

		for (int i = 0; i < list.getLength(); i++) {
			Node x = list.item(i);
			if (x.getNodeType() == Node.TEXT_NODE) {
				return x;
			}
		}

		return null;
	}

	/**
	 * Prints the contents of the <code>NodeList</code> depending on the types
	 * of the .
	 *
	 * @param prefix
	 *            prefix of the log message.
	 * @param list
	 *            the <code>NodeList</code> object to be printed.
	 * @see #printNode(String, Node)
	 */
	public void dumpNodes(String prefix, NodeList list) {
		for (int i = 0; i < list.getLength(); i++) {

			Node x = list.item(i);
			printNode(prefix, x);

			if (x.getChildNodes() != null)
				dumpNodes(prefix + "  ", x.getChildNodes());

		}
	}

	/**
	 * Prints the contents of the <code>Node</code> depending on its type.
	 *
	 * @param prefix
	 *            prefix of the log message.
	 * @param n
	 *            the <code>Node</code> to be printed.
	 */
	public void printNode(String prefix, Node n) {
		switch (n.getNodeType()) {
		case Node.DOCUMENT_NODE:
			p(sResHash.getString("PARSER.SOAP.DOCUMENT.NODE.TYPE", prefix));
			break;
		case Node.ELEMENT_NODE:
			p(sResHash.getString("PARSER.SOAP.ELEMENT.NODE", new Object[] {
					prefix, n.getNodeName() }));
			break;
		case Node.TEXT_NODE:
			p(sResHash.getString("PARSER.SOAP.NODE.TEXT", new Object[] {
					prefix, n.getNodeValue() }));
			break;
		default:
			p(sResHash.getString("PARSER.SOAP.NODE.DEFAULT", prefix));
			break;
		}
	}

	/**
	 * Writes a message to the log.
	 *
	 * @param s
	 *            The log message
	 * @see #logmsg(String)
	 */
	public void p(String s) {
		logmsg(s);
	}

	/**
	 * Reads SOAP XML documents. The attribute <code>SOAP_CALL</code> is set
	 * to reflect the first tag following the <code>SOAP-ENV:Body</code> tag.
	 * Each tag under the SOAP_CALL tag translates to an attribute in the entry
	 * object.
	 *
	 * @return the entry containing the read attributes.
	 * @throws Exception
	 *             if the XML file is invalid or malformed SOAP XML document.
	 */
	public Entry readEntry() throws Exception {
		Trace.entrymax(this, "readEntry");
		if (children == null)
			return null;

		//Node call = findNamedElement(children, SOAP_BODY);
		Node call = findBodyElement();

		if (call == null) {
			throw new Exception(sResHash
					.getString("PARSER.SOAP.XML.DOCUMENT.HAS.NO.SOAP.BODY"));
		}

		Node soapFunction = getElement(call, curindex);
		if (soapFunction == null) {
			if (curindex == 0) {
				throw new Exception(
						sResHash
								.getString("PARSER.SOAP.SOAP.ENV.BODY.TAG.HAS.NO.ELEMENTS"));
			}
			return null; // EOF
		}
		curindex++;

		if (debugMode()) {
			debug(sResHash.getString("PARSER.SOAP.SOAP.CALL", soapFunction
					.getNodeName()));
		}

		Entry e = new Entry();
		e.setAttribute(SOAP_CALL, soapFunction.getNodeName());

		Vector<Element> soapCall = getElementList(soapFunction);
		for (int i = 0; i < soapCall.size(); i++) {
			Element x = soapCall.elementAt(i);
			String encoding = x.getAttribute("xsi:type");
			if (encoding == null)
				encoding = "xsi:string";

			Node y = getFirstText(x);
			if (y != null) {
				String nodeValue = y.getNodeValue();
				if (nodeValue != null) {
					if (encoding.equals("xsi:string"))
						e.setAttribute(x.getNodeName(), nodeValue);
					if (encoding.equals("xsi:double"))
						e.setAttribute(x.getNodeName(), new Double(nodeValue));
					if (encoding.equals("xsi:int"))
						e.setAttribute(x.getNodeName(), new Integer(nodeValue));

					// Catch all
					if (e.getAttribute(x.getNodeName()) == null)
						e.setAttribute(x.getNodeName(), nodeValue);
				} else {
					if (debugMode()) {
						debug(sResHash.getString(
								"PARSER.SOAP.ELEMENT.HAS.NO.VALUE", x
										.getNodeName()));
					}
				}
			} else {
				if (debugMode()) {
					debug(sResHash.getString(
							"PARSER.SOAP.ELEMENT.HAS.NO.VALUE.2", x
									.getNodeName()));
				}
			}

		}
		Trace.exitmax(this, "readEntry", e);
		return e;
	}

	/**
	 * Finds the SOAP Body element.
	 * @return The SOAP Body element
	 */
	public Node findBodyElement() {
		for (int i = 0; i < children.getLength(); i++) {
			Node x = children.item(i);
			if (x.getNodeType() == Node.ELEMENT_NODE && x.getNodeName().endsWith(":Body")) {
				return x;
			}
		}

		return null;
	}

	/**
	 * Writes SOAP XML document. The SOAPParser uses attributes from the
	 * <code>entry</code> to build the document. The <code>SOAP_CALL</code>
	 * attribute is expected to contain the value for the SOAP call. For all
	 * others attribute in the <code>entry</code>, a tag with that name and
	 * value is created.
	 *
	 * @param entry
	 *            the entry containing the attributes to write.
	 * @throws Exception
	 *             if could not build the DOM tree or serialize the DOM element.
	 * @see org.w3c.dom.DOMException
	 */
	public void writeEntry(Entry entry) throws Exception {
		/*
		 * <SOAP-ENV:Envelope
		 * xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
		 * xmlns:xsi="http://www.w3.org/1999/XMLSchema-instance"
		 * xmlns:xsd="http://www.w3.org/1999/XMLSchema"> <SOAP-ENV:Body>
		 * <ns1:updateLDAP xmlns:ns1=""
		 * SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
		 * <uid xsi:type="xsd:string">bjst</uid> <mail
		 * xsi:type="xsd:string">bjorn@metamerge.com</mail> </ns1:updateLDAP>
		 * </SOAP-ENV:Body> </SOAP-ENV:Envelope>
		 */
		Trace.entrymax(this, "writeEntry", entry);
		Document doc = db.newDocument();

		Element envelope = doc.createElement(SOAP_ENV);
		envelope.setAttribute("xmlns:SOAP-ENV", SOAP_NS_ENV);
		envelope.setAttribute("xmlns:xsi", SOAP_NS_XSI);
		envelope.setAttribute("xmlns:xsd", SOAP_NS_XSD);
		doc.appendChild(envelope);

		Element body = doc.createElement(SOAP_BODY);
		envelope.appendChild(body);

		Element reply = doc.createElement(entry.getString(SOAP_CALL));
		reply.setAttribute("SOAP-ENV:encodingStyle", SOAP_NS_ENC);
		body.appendChild(reply);

		// If use provides additional
		if (entry.getString(SOAP_REPLY) != null) {
			reply.appendChild(parseXML(entry.getString(SOAP_REPLY)));
		} else {

			// Otherwise, just dump every attribute as an element to the reply
			String[] names = entry.getAttributeNames();
			for (int i = 0; i < names.length; i++) {

				if (names[i].equalsIgnoreCase(SOAP_BODY))
					continue;
				if (names[i].equalsIgnoreCase(SOAP_CALL))
					continue;

				Element param = doc.createElement(names[i]);
				Object pval = entry.getObject(names[i]);
				if (pval instanceof String)
					param.setAttribute("xsi:type", "xsd:string");
				if (pval instanceof Double)
					param.setAttribute("xsi:type", "xsd:double");
				if (pval instanceof Integer)
					param.setAttribute("xsi:type", "xsd:int");

				reply.appendChild(param);

				Text value = doc.createTextNode(entry.getString(names[i]));
				param.appendChild(value);

			}
		}

		String charSet = getParam("characterSet");
		if (charSet == null || charSet.equals(""))
			charSet = "UTF-8";
		OutputFormat format = new OutputFormat("xml", charSet, true); // Serialize
		// DOM
		format.setOmitXMLDeclaration(getOmitXMLDeclaration());
		XMLSerializer serial = new XMLSerializer(getWriter(), format);
		serial.asDOMSerializer(); // As a DOM Serializer
		serial.serialize(doc.getDocumentElement());
		getWriter().flush();
		Trace.exitmax(this, "writeEntry");
	}

	/**
	 * Parses XML file to <code>Document</code> element.
	 *
	 * @param xml
	 *            xml file to parse.
	 * @return parsed <code>Document</code> element.
	 * @throws Exception
	 *             If any I/O or parse errors occur; if <code>xml</code> is
	 *             <code>null</code>
	 */
	public Node parseXML(String xml) throws Exception {
		Trace.entrymax(this, "parseXML", xml);
		Document doc = db.parse(new org.xml.sax.InputSource(new StringReader(
				xml)));

		logmsg(sResHash.getString("PARSER.SOAP.PARSEXML.RETURNS.NODE.NAME", doc
				.getDocumentElement().getNodeName()));

		Trace.exitmax(this, "parseXML");
		return doc.getDocumentElement();
	}

	/**
	 * Returns string representation of <code>Entry</code> object as a XML
	 * file.
	 *
	 * @param e
	 *            the <code>Entry</code> object
	 * @return the buffer of the current writer object as a string.
	 * @throws Exception
	 *             if could not build the DOM tree or serialize the DOM element.
	 */
	public String getXML(Entry e) throws Exception {

		Writer old = getWriter();
		StringWriter sw = new StringWriter();

		try {
			setOutputStream(sw);
			writeEntry(e);
		} catch (Exception err) {
			setOutputStream(old);
			throw err;
		}
		setOutputStream(old);
		return sw.toString();

	}

	/**
	 * Version information.
	 * @return version information
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I% 20%E%";
	}
}
