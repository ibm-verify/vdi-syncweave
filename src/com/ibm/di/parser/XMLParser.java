/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;

/**
 * A class reading and writing Entries in XML format.
 * 
 */

public class XMLParser extends ParserImpl implements ErrorHandler {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "simplexmlparser";

	/**
	 * If we are reading a document, <code>inputDoc</code> will be available
	 * as the XML DOM object in JavaScript
	 */
	public Document inputDoc;

	/**
	 * If we are writing a document, outputDoc will be available as the XML DOM
	 * object in JavaScript
	 */
	public Document outputDoc;

	/**
	 * The root element
	 */
	public Element toplevel;

	/**
	 * The document element of the input Document
	 */
	public Element toplevelInput;

	/**
	 * The children of the document element of the input Document
	 */
	public NodeList children;

	/**
	 * Variable containing the current index
	 */
	public int curindex;

	/**
	 * Whether using CDATASection or TextNode. Default is CDATASection
	 */
	public boolean useCData = true;

	/**
	 * The value of the "xmlEntryTag" attribute
	 */
	public String entrytag;

	/**
	 * The value of the "xmlValueTag" attribute
	 */
	public String valuetag;

	/**
	 * The document builder used to build the document
	 */
	public DocumentBuilder db;

	/**
	 * Whether to omit the XML declaration
	 */
	private boolean omitxmldeclaration = false;

	/**
	 * Whether to use indents in the output. Default is true.
	 */
	private boolean indentOutput = true;

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
	public XMLParser() {
		Trace.entrymid(this, "XMLParser");
		Trace.exitmid(this, "XMLParser");
	}

	/**
	 * Registers inputDoc or outputDoc as the XML DOM object in JavaScript
	 * 
	 * @param se
	 *            ScriptEngine
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void registerScriptBeans(ScriptEngine se) throws Exception {
		if (inputDoc != null)
			se.declareStaticBean("xmldom", inputDoc);
		if (outputDoc != null)
			se.declareStaticBean("xmldom", outputDoc);
	}

	/**
	 * This function is called by the connector containing this parser
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initParser() throws Exception {
		Trace.entrymin(this, "initParser");
		resetProperties();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		String check = getParam("isvalidating");
		if (check != null && check.equalsIgnoreCase("true")) {
			dbf.setValidating(true);
			if (debugMode()) {
				debug(sResHash.getString("PARSER.XML.VALIDATE.REQUEST.INFO"));
			}
		}

		check = getParam("isnamespaceaware");
		if (check != null && check.equalsIgnoreCase("true")) {
			dbf.setNamespaceAware(true);
			if (debugMode()) {
				debug(sResHash
						.getString("PARSER.XML.VALIDATE.NAMESPACEAWARE.INFO"));
			}
		}

		check = getParam("indentoutput");
		if (check != null && check.equalsIgnoreCase("false")) {
			indentOutput = false;
			if (debugMode()) {
				debug(sResHash.getString("PARSER.XML.VALIDATE.NOINDENT.INFO"));
			}
		}

		// Try to reduce memory usage by setting this parameter
		try {
			dbf.setAttribute(
					"http://apache.org/xml/features/dom/defer-node-expansion",
					Boolean.FALSE);
			if (debugMode()) {
				debug(sResHash.getString("PARSER.XML.VALIDATE.DEFDOMOFF.INFO"));
			}
		} catch (Exception ignore) {
			if (debugMode()) {
				debug(sResHash
						.getString("PARSER.XML.VALIDATE.NODEFDOMOFF.INFO"));
			}
		}

		db = dbf.newDocumentBuilder();
		db.setErrorHandler(this);

		check = getParam("omitxmldeclaration");
		if (check != null)
			setOmitXMLDeclaration(check.equalsIgnoreCase("true"));

		if (getReader() != null)
			initInput();

		if (getWriter() != null)
			initOutput();
		Trace.exitmin(this, "initParser");
	}

	/**
	 * Close the parser
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void closeParser() throws Exception {
		Trace.entrymin(this, "closeParser");
		flush();
		super.closeParser();
		Trace.exitmin(this, "closeParser");
	}

	/**
	 * Flush any data to the output
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	@Override
	public void flush() throws Exception {
		Trace.entrymin(this, "flush");
		if (outputDoc != null && (getWriter() != null || getOutputStream() != null)) {
			String charSet = getParam("characterSet");
			if (charSet == null || charSet.equals(""))
				charSet = "UTF-8";

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, charSet);
			transformer.setOutputProperty(OutputKeys.INDENT, indentOutput ? "yes" : "no");
			if (getOmitXMLDeclaration()) {
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			} else {
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			}

			DOMSource source = new DOMSource(outputDoc);
			StreamResult result;
			if (getOutputStream() != null)
				result = new StreamResult(getOutputStream());
			else
				result = new StreamResult(getWriter());

			transformer.transform(source, result);

			if (getWriter() != null)
				getWriter().flush();
		}
		Trace.entrymin(this, "flush");
	}

	/**
	 * Initializes the output Document
	 * 
	 * @throws Exception
	 *             If could not find output stream.
	 */
	public void initOutput() throws Exception {
		Trace.entrymax(this, "initOutput");
		if (getWriter() == null) {
			throw new Exception(sResHash.getString("PARSER.XML.NOOUTPUT.ERROR"));
		}
		if (debugMode()) {
			debug(sResHash.getString("PARSER.XML.INITOUTPUTDOC.INFO"));
		}
		outputDoc = db.newDocument();

		String root = (String) getParam("xmlRootTag");
		if (root == null || root.equals(""))
			root = "DocRoot";

		entrytag = getParam("xmlEntryTag");
		valuetag = getParam("xmlValueTag");

		if (entrytag == null || entrytag.equals(""))
			entrytag = "Entry";
		if (valuetag == null || valuetag.equals(""))
			valuetag = "ValueTag";

		toplevel = outputDoc.createElement(root);
		outputDoc.appendChild(toplevel);
		Trace.exitmax(this, "initOutput");
	}

	/**
	 * Initializes the properties from the input Document
	 * 
	 * @throws Exception
	 *             if no input stream is found.
	 */
	public void initInput() throws Exception {
		Trace.entrymax(this, "initInput");
		if (getReader() == null) {
			throw new Exception(sResHash.getString("PARSER.XML.NOINPUT.ERROR"));
		}
		if (debugMode()) {
			debug(sResHash.getString("PARSER.XML.INITINPUTDOC.INFO"));
		}
		InputSource inp;
		String charSet = getParam("characterSet");
		if (charSet == null)
			charSet = "";

		// To work around a problem, we cannot use inputStream with UTF-16
		// and possibly other multibyte characterSets
		if (getInputStream() != null && !charSet.startsWith("UTF-16")
				&& !charSet.startsWith("UTF-32")) {
			inp = new InputSource(getInputStream());
			// Set the encoding. This will only have effect if
			// the document does not provide an encoding.
			if (charSet.length() > 0)
				inp.setEncoding(charSet);

		} else {
			inp = new InputSource(getReader());
		}

		inputDoc = db.parse(inp);

		toplevelInput = inputDoc.getDocumentElement();
		if (toplevelInput == null) {
			throw new Exception(sResHash.getString("PARSER.XML.NOROOT.ERROR"));
		} else {
			toplevelInput.normalize();
		}

		children = toplevelInput.getChildNodes();
		if (children == null) {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.XML.NOCHILDREN.INFO",
						toplevelInput.getTagName()));
			}
		} else {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.XML.NUMCHILDREN.INFO", ""
						+ children.getLength()));
			}
		}

		curindex = 0;
		Trace.exitmax(this, "initInput");
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
		// EOF?
		if (children == null || curindex >= children.getLength())
			return null;

		Node n = children.item(curindex++);

		if (n == null) {
			throw new Exception(sResHash.getString("PARSER.XML.BADINDEX.ERROR",
					"" + (curindex - 1)));
		}

		if (n.getChildNodes() == null || n.getChildNodes().getLength() < 1) {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.XML.SKIPCHILD.INFO"));
			}
			return readEntry();
		}

		Entry e = new Entry();

		Node x = n.getFirstChild();
		while (x != null) {
			if (x.getNodeType() == Node.ELEMENT_NODE) {
				Node values = x.getFirstChild();
				Attribute ba = new Attribute(x.getNodeName());
				String val = "";
				while (values != null) {
					val = "";
					if (values.getNodeType() == Node.CDATA_SECTION_NODE) {
						val = (String) values.getNodeValue();
					} else if (values.getNodeType() == Node.TEXT_NODE) {
						val = ((String) values.getNodeValue()).trim();
					} else if (values.getNodeType() == Node.ELEMENT_NODE) {
						Node child = values.getFirstChild();
						if (child != null
								&& child.getNodeType() == Node.TEXT_NODE) {
							val = ((String) child.getNodeValue()).trim();
						}
					}
					if (val.length() > 0) {
						ba.addValue(val);
					}
					values = values.getNextSibling();
				}
				if (ba.size() == 0) {
					ba.addValue(val);
				}
				e.setAttribute(ba);
			}
			x = x.getNextSibling();
		}
		Trace.exitmax(this, "readEntry", e);
		return e;
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
		Element rec = outputDoc.createElement(entrytag);
		Element child;

		String[] names = entry.getAttributeNames();
		if (names == null) {
			names = new String[0];
		}
		for (int i = 0; i < names.length; i++) {
			if (names[i] == null || names[i].isEmpty()) {
				continue;
			}
			Attribute a = entry.getAttribute(names[i]);
			if (a == null) {
				continue;
			}
			child = outputDoc.createElement(names[i]);
			if (a.size() > 1) {
				for (int j = 0; j < a.size(); j++) {
					Element value = outputDoc.createElement(valuetag);
					child.appendChild(value);
					String val = a.getValue(j) != null ? a.getValue(j).toString() : "";
					if (val.indexOf("\n") != -1 && useCData)
						value.appendChild(outputDoc.createCDATASection(val));
					else
						value.appendChild(outputDoc.createTextNode(val));
				}
			}

			if (a.size() == 1) {

				String val = a.getValue();
				if (val != null && val.indexOf("\n") != -1 && useCData)
					child.appendChild(outputDoc.createCDATASection(val));
				else
					child.appendChild(outputDoc.createTextNode(val != null ? val : ""));

			}
			rec.appendChild(child);
		}

		toplevel.appendChild(rec);
		Trace.exitmax(this, "writeEntry");
	}

	/**
	 * Sets whether to omit XML declaration or not.
	 * 
	 * @param omit
	 *            if <code>true</code> omits XML declaration header in output
	 *            stream; otherwise does not omit XML declaration;
	 */
	public void setOmitXMLDeclaration(boolean omit) {
		omitxmldeclaration = omit;
	}

	/**
	 * Returns whether we omit XML declaration or not.
	 * 
	 * @return <code>true</code> if omits XML declaration header in output
	 *         stream; <code>false</code> otherwise.
	 */
	public boolean getOmitXMLDeclaration() {
		return omitxmldeclaration;
	}

	/**
	 * Ignore this since an error is called immediately afterwards
	 * 
	 * @param exception
	 *            {@link SAXParseException}
	 */
	public void fatalError(SAXParseException exception) {
	}

	/**
	 * Rethrows the given exception
	 * 
	 * @param exception
	 *            {@link SAXParseException}
	 * @throws SAXException
	 */
	public void error(SAXParseException exception) throws SAXException {
		throw exception;
	}

	/**
	 * Logs the given warning
	 * 
	 * @param err
	 *            SAXParseException
	 * @throws SAXException
	 */
	public void warning(SAXParseException err) throws SAXException {
		logmsg(sResHash.getString("PARSER.XML.SAX.WARNING", new Object[] {
				Integer.valueOf(err.getLineNumber()), err.getSystemId(),
				err.getMessage() }));
	}

	/**
	 * Version information.
	 * @return version information
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I% 20%E%";
	}

	/**
	 * Resets all properties
	 */
	private void resetProperties() {
		inputDoc = null;
		outputDoc = null;
		toplevel = null;
		toplevelInput = null;
		children = null;
		curindex = 0;
		useCData = true;
		entrytag = null;
		valuetag = null;
		db = null;
		omitxmldeclaration = false;
		indentOutput = true;
	}
}
