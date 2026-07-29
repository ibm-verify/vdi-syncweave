/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Vector;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;

/**
 * The DSML Parser reads and writes XML documents. The Parser silently ignores
 * schema entries.
 * 
 */
public class DSMLParser extends XMLParser {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The name of the properties file
	 */
	private static final String PROPERTIES_FILE = "dsmlparser";

	/**
	 * Namespace
	 */
	private final static String NAMESPACE_URI = "http://www.dsml.org/DSML";

	/**
	 * The name of the root tag
	 */
	private final static String ROOTELEMENT = "dsml";

	/**
	 * The name of the Directory Entries tag
	 */
	private final static String DIRENTRIES = "directory-entries";

	/**
	 * The name of the Entry tag
	 */
	private final static String ENTRY = "entry";

	/**
	 * The name of the Attribute tag
	 */
	private final static String ATTRIBUTE = "attr";

	/**
	 * The name of the Value tag
	 */
	private final static String VALUE = "value";

	/**
	 * The name of the ObjectClass tag
	 */
	private final static String OBJECTCLASS = "objectclass";

	/**
	 * The name of the OCValue tag
	 */
	private final static String OCVALUE = "oc-value";

	/**
	 * The used character set
	 */
	private final static String CHARSET = "UTF-8";

	/**
	 * Entries
	 */
	private Node entries;

	/**
	 * NextNode
	 */
	private Node nextNode;

	/**
	 * DN Attribute
	 */
	private String dnAttribute = "$dn";

	/**
	 * Prefix
	 */
	private String prefix;

	/**
	 * URI
	 */
	private String uri;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Constructor
	 */
	public DSMLParser() {
		super();
		Trace.entrymid(this, "DSMLParser");
		Trace.exitmid(this, "DSMLParser");
	}

	/**
	 * This function is called by the connector containing this parser
	 */
	@Override
	public void initParser() throws Exception {
		Trace.entrymin(this, "initParser");
		resetProperties();
		if (getParam("dnattribute") != null)
			dnAttribute = (String) getParam("dnattribute");

		prefix = (String) getParam("prefix");
		if (prefix == null)
			prefix = "dsml";
		prefix = prefix.trim();
		if (!(prefix.equals("") || prefix.endsWith(":")))
			prefix += ":";

		uri = (String) getParam("uri");
		if (uri == null)
			uri = NAMESPACE_URI;

		super.initParser();
		Trace.exitmin(this, "initParser");
	}

	/**
	 * Initializes the output Document
	 * 
	 * @throws Exception if an error occurs.
	 */
	@Override
	public void initOutput() throws Exception {
		Trace.entrymin(this, "initOutput");
		if (getWriter() == null) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.TRYINGWRITEDSML.NOINPUT.ERROR"));
		}

		outputDoc = db.newDocument();
		toplevel = outputDoc.createElement(prefix + ROOTELEMENT);
		toplevel.setAttribute("xmlns:dsml", uri);
		outputDoc.appendChild(toplevel);

		entries = outputDoc.createElement(prefix + DIRENTRIES);
		toplevel.appendChild(entries);
		Trace.exitmin(this, "initOutput");

	}

	/**
	 * Initializes the properties from the input Document
	 * 
	 * @throws Exception if an error occurs.
	 */
	@Override
	public void initInput() throws Exception {
		super.initInput();
		Trace.entrymin(this, "initInput");
		entries = getFirstElement(toplevelInput);
		if (entries == null)
			return;

		nextNode = getFirstElement(entries);
		Trace.exitmin(this, "initInput");

	}

	/**
	 * Sets the input stream with the given request object if the object is of
	 * type InputStream, Reader or StringReader.
	 * 
	 * @param request
	 *            Object of type InputStream Reader or StringReader
	 * 
	 * @return the next entry
	 * @throws Exception
	 *             if an error occurs
	 */
	public Entry parseRequest(Object request) throws Exception {
		Trace.entrymax(this, "parseRequest", request);
		if (request instanceof InputStream)
			setInputStream((InputStream) request);
		if (request instanceof Reader)
			setInputStream((Reader) request);
		if (request instanceof String)
			setInputStream(new StringReader((String) request));

		initParser();
		Trace.exitmax(this, "parseRequest");
		return readEntry();
	}

	/**
	 * Finds element by name
	 * 
	 * @param list
	 *            NodeList
	 * @param name
	 *            String
	 * @return the Node
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
	 * Retrieves children elements
	 * 
	 * @param parent
	 *            Node
	 * @return the Element children of the given node
	 */
	public Vector getElementList(Node parent) {
		NodeList list = parent.getChildNodes();
		Vector v = new Vector();

		for (int i = 0; i < list.getLength(); i++) {
			Node x = list.item(i);
			if (x.getNodeType() == Node.ELEMENT_NODE) {
				v.add(x);
			}
		}

		return v;
	}

	/**
	 * Retrieves first child element
	 * 
	 * @param parent
	 *            Node
	 * @return the first Element child of the given node
	 */
	public Node getFirstElement(Node parent) {
		NodeList list = parent.getChildNodes();

		for (int i = 0; i < list.getLength(); i++) {
			Node x = list.item(i);
			if (x.getNodeType() == Node.ELEMENT_NODE) {
				return x;
			}
		}

		return null;
	}

	/**
	 * Retrieves first first Text node child
	 * 
	 * @param parent
	 *            Node
	 * @return the first Text node child of the given node
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
	 * Prints all nodes and its children from the given list with the given
	 * indent
	 * 
	 * @param indent
	 *            String
	 * @param list
	 *            NodeList
	 */
	public void dumpNodes(String indent, NodeList list) {
		for (int i = 0; i < list.getLength(); i++) {

			Node x = list.item(i);
			printNode(indent, x);

			if (x.getChildNodes() != null)
				dumpNodes(indent + "  ", x.getChildNodes());

		}
	}

	/**
	 * Prints a node with a given indent
	 * 
	 * @param indent
	 *            String
	 * @param n
	 *            Node
	 */
	public void printNode(String indent, Node n) {
		switch (n.getNodeType()) {
		case Node.DOCUMENT_NODE:
			p(sResHash.getString("PARSER.DSML.DOCUMENT.NODE.TYPE", indent));
			break;
		case Node.ELEMENT_NODE:
			p(sResHash.getString("PARSER.DSML.ELEMENT.NODE", new Object[] {
					indent, n.getNodeName() }));
			break;
		case Node.TEXT_NODE:
			p(sResHash.getString("PARSER.DSML.NODE.TEXT", new Object[] {
					indent, n.getNodeValue() }));
			break;
		default:
			p(sResHash.getString("PARSER.DSML.NODE.DEFAULT", indent));
			break;
		}
	}

	/**
	 * Prints a String
	 * 
	 * @param s
	 *            the String to print
	 */
	public void p(String s) {
		logmsg(s);
	}

	/**
	 * Constructs the next entry from the data structure read in initially. If
	 * end of data encountered a null value is returned.
	 * 
	 * @return the next Entry
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Entry readEntry() throws Exception {
		Trace.entrymax(this, "readEntry");
		if (entries == null)
			return null;

		if (nextNode == null) {
			entries = entries.getNextSibling();
			if (entries == null)
				return null;

			nextNode = getFirstElement(entries);
			if (nextNode == null)
				return null;
		}
		if (debugMode()) {
			debug(sResHash.getString("PARSER.DSML.PARENTNODE.INFO",
					new Object[] { entries.getNodeName(),
							nextNode.getNodeName() }));
		}
		// Only read directory entries for now ....
		if (!nextNode.getNodeName().equals(prefix + ENTRY)) {
			nextNode = getNextNode(nextNode);
			return readEntry();
		}

		Entry e = new Entry();
		e.setProperty("entry-type", entries.getNodeName());
		e.setProperty("entry-name", nextNode.getNodeName());

		if (((Element) nextNode).getAttribute("dn") != null)
			e
					.setAttribute(dnAttribute, ((Element) nextNode)
							.getAttribute("dn"));

		Node node = getFirstElement(nextNode);

		while (node != null) {

			com.ibm.di.entry.Attribute attr = null;

			String name = node.getNodeName();
			if (name.equals(prefix + ATTRIBUTE)) {
				attr = new com.ibm.di.entry.Attribute(((Element) node)
						.getAttribute("name"));
			}

			if (name.equals(prefix + OBJECTCLASS)) {
				attr = new com.ibm.di.entry.Attribute("objectclass");
			}

			if (attr != null) {

				Node value = getFirstElement(node);
				while (value != null) {
					if (value.getFirstChild() != null) {
						if (value instanceof Element
								&& "base64".equals(((Element) value)
										.getAttribute("encoding")))
							attr.addValue(getBytes(value.getFirstChild()
									.getNodeValue()));
						else
							attr.addValue(value.getFirstChild().getNodeValue());
					}
					value = getNextNode(value);
				}

				e.setAttribute(attr);

			}

			node = getNextNode(node);

		}

		nextNode = getNextNode(nextNode);
		Trace.exitmax(this, "readEntry", e);
		return e;
	}

	/**
	 * Retrieves the next Node
	 * 
	 * @param node
	 *            Node
	 * @return the next sibling node of the given one
	 */
	public Node getNextNode(Node node) {
		Node n = node;

		while ((n = n.getNextSibling()) != null) {
			if (n.getNodeType() == Node.ELEMENT_NODE)
				break;
		}

		return n;

	}

	/**
	 * Constructs the data structure from the next entry read in initially.
	 * 
	 * @param entry
	 *            the next Entry
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void writeEntry(Entry entry) throws Exception {
		Trace.entrymax(this, "writeEntry", entry);
		Element out = outputDoc.createElement(prefix + ENTRY);

		String[] attributes = entry.getAttributeNames();

		if (entry.getString(dnAttribute) == null) {
			logmsg(sResHash.getString(
					"PARSER.DSML.TRYINGWRITEDSML.NOINPUT.ERROR", dnAttribute));
			out.setAttribute("dn", "");
		} else {
			out.setAttribute("dn", entry.getString(dnAttribute));
		}

		if (entry.getAttribute("objectclass") != null)
			out
					.appendChild(createObjectClass(entry
							.getAttribute("objectclass")));

		for (int i = 0; i < attributes.length; i++) {

			if (attributes[i].equalsIgnoreCase(dnAttribute))
				continue;

			if (attributes[i].equalsIgnoreCase("objectclass"))
				continue;

			out.appendChild(createAttribute(entry.getAttribute(attributes[i])));
		}

		entries.appendChild(out);
		Trace.exitmax(this, "writeEntry");
	}

	/**
	 * Creates Element with the given attribute .
	 * 
	 * @param attr
	 *            Attribute
	 * 
	 * @return the created Element
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Element createAttribute(com.ibm.di.entry.Attribute attr)
			throws Exception {
		Trace.entrymax(this, "createAttribute", attr);
		Element a = outputDoc.createElement(prefix + ATTRIBUTE);
		a.setAttribute("name", attr.getName());
		for (int i = 0; i < attr.size(); i++) {
			a.appendChild(createValue(prefix + VALUE, attr.getValue(i)));
		}
		Trace.exitmax(this, "createAttribute", a);
		return a;
	}

	/**
	 * Creates Element with <code>ObjectClass</code> tag name. Then appends a
	 * child Element to it with name <code>oc-value</code> and value the given
	 * attribute.
	 * 
	 * @param attr
	 *            Attribute
	 * 
	 * @return the created Element
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Element createObjectClass(com.ibm.di.entry.Attribute attr)
			throws Exception {
		Trace.entrymax(this, "createObjectClass", attr);
		Element a = outputDoc.createElement(prefix + OBJECTCLASS);
		for (int i = 0; i < attr.size(); i++) {
			a.appendChild(createValue(prefix + OCVALUE, attr.getValue(i)));
		}
		Trace.exitmax(this, "createAttribute", a);
		return a;
	}

	/**
	 * Creates an Element with the given name and elemValue. The type of the
	 * Element could be TextNode or CDATASection depending on the type of the
	 * elemValue parameter.
	 * 
	 * @param name
	 *            String object
	 * @param elemValue
	 *            value of the Element
	 * 
	 * @return the created Element
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Element createValue(String name, Object elemValue) throws Exception {
		Trace.entrymax(this, "createValue", name, elemValue);
		Element v = outputDoc.createElement(name);
		Object value = elemValue;
		String str;

		if (value instanceof byte[]) {
			str = encodeBytes((byte[]) value);
			v.setAttribute("encoding", "base64");
			v.appendChild(outputDoc.createTextNode(str));
		} else {
			if (value instanceof String)
				str = (String) value;
			else
				str = value.toString();

			if (str.indexOf("\n") != -1)
				v.appendChild(outputDoc.createCDATASection(str));
			else
				v.appendChild(outputDoc.createTextNode(str));
		}
		Trace.exitmax(this, "createValue", v);
		return v;
	}

	/**
	 * Converts the output Document to String using parser's encoding.
	 * 
	 * @return the String with the XML Document
	 * @throws Exception
	 *             if an error occurs.
	 */
	public String getXML() throws Exception {

		Trace.entrymax(this, "getXML");
		StringWriter osw = new StringWriter();
		String charSet = getParam("characterSet");
		if (charSet == null || charSet.equals(""))
			charSet = CHARSET;

		OutputFormat format = new OutputFormat("xml", charSet, true); // Serialize
		// DOM
		format.setOmitXMLDeclaration(getOmitXMLDeclaration());
		XMLSerializer serial = new XMLSerializer(osw, format);
		serial.asDOMSerializer(); // As a DOM Serializer
		serial.serialize(outputDoc.getDocumentElement());

		// outputDoc.write (osw, "UTF-8");
		osw.close();
		// doc.write (getWriter(), "UTF-8");
		Trace.exitmax(this, "getXML", osw);
		return osw.toString();
	}

	/**
	 * Array used for base64 encoding
	 */
	private char[] encode = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
			'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
			'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
			'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'+', '/', };

	/**
	 * Encodes the given byte[] array using the base64 encoding
	 * 
	 * @param b
	 *            byte array
	 * 
	 * @return the encoded String
	 */
	private String encodeBytes(byte[] b) {

		StringWriter w = new StringWriter();
		int res = 0;
		int i = 0;

		while (i < b.length) {
			int ch = b[i] & 0xff;
			switch (i % 3) {
			case 0:
				w.write(encode[ch >> 2]);
				res = (ch & 3) << 4;
				break;
			case 1:
				w.write(encode[res | (ch >> 4)]);
				res = (ch & 0xf) << 2;
				break;
			case 2:
				w.write(encode[res | (ch >> 6)]);
				w.write(encode[ch & 0x3f]);
			}
			i++;
		}

		i %= 3;
		if (i != 0) {
			w.write(encode[res]);
			if (i == 1)
				w.write("==");
			else
				w.write("=");
		}
		return w.toString();
	}

	/**
	 * Array used to decode base64 encoding
	 */
	private int[] decode = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1,
			-1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1,
			-1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
			17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27,
			28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44,
			45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1 };

	/**
	 * Decodes the given String into byte[] array using the base64 encoding
	 * 
	 * @param str
	 *            String
	 * 
	 * @return the decoded byte[] array
	 */
	private byte[] getBytes(String str) {
		ByteArrayOutputStream w = new ByteArrayOutputStream();

		int mode = 0;
		int res = 0;

		for (int i = 0; i < str.length(); i++) {
			int ch = (int) str.charAt(i);
			if (ch > 0 && ch < 128)
				ch = decode[ch];
			else
				continue;

			if (ch < 0)
				continue;

			switch (mode) {
			case 0:
				res = ch << 2;
				break;
			case 1:
				w.write(res | (ch >> 4));
				res = (ch << 4) & 0xff;
				break;
			case 2:
				w.write(res | (ch >> 2));
				res = (ch << 6) & 0xff;
				break;
			case 3:
				w.write(res | ch);
				break;
			}
			mode = (mode + 1) % 4;
		}

		return w.toByteArray();
	}

	/**
	 * Version Information.
	 * @return version information
	 */
	public String getVersion() {
		return "2.1-di7.1.1 %I% 20%E%";
	}

	/**
	 * Resets all properties
	 */
	private void resetProperties() {
		entries = null;
		nextNode = null;
		dnAttribute = "$dn";
		prefix = null;
		uri = null;
	}

}
