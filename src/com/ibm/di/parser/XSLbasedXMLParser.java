/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
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
 */
public class XSLbasedXMLParser extends ParserImpl implements ErrorHandler {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "xslxmlparser";

	/**
	 * The name of the root tag
	 */
	private static final String ROOT = "DocRoot";

	/**
	 * The name of the Entry tag
	 */
	private static final String ENTRY = "Entry";

	/**
	 * The name of the Attribute tag
	 */
	private static final String ATTRIBUTE = "Attribute";

	/**
	 * The name of the Value tag
	 */
	private static final String VALUE = "Value";

	/**
	 * The input Document
	 */
	public Document inputDoc;

	/**
	 * The output Document
	 */
	public Document outputDoc;

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
     * Entry tag
     */
       	public String entrytag;
          
    /**
     * Value tag
     */
	public String valuetag;
          
	/**
	 * The document builder used to build the document
	 */
	public DocumentBuilder db;

	/**
	 * Whether to omit the xml declaration
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
	public XSLbasedXMLParser() {
		Trace.entrymid(this, "XSLBasedXMLParser");
		Trace.exitmid(this, "XSLBasedXMLParser");
	}

	/**
	 * Registers "static" script variables for input or output Document in the
	 * given ScriptEngine with the name "xmldom"
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
				debug(sResHash
						.getString("PARSER.XSLXML.WILL.REQUEST.VALIDATING.PARSER"));
			}
		}

		check = getParam("isnamespaceaware");
		if (check != null && check.equalsIgnoreCase("true")) {
			dbf.setNamespaceAware(true);
			if (debugMode()) {
				debug(sResHash.getString("PARSER.XSLXML.NAMESPACE.INFO"));
			}
		}

		check = getParam("indentoutput");
		if (check != null && check.equalsIgnoreCase("false")) {
			indentOutput = false;
			if (debugMode()) {
				debug(sResHash
						.getString("PARSER.XSLXML.WILL.NOT.INDENT.OUTPUT"));
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
	 * Closes the parser
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
	 * This function is called when the parser is closing to serialize the in
	 * memory DOM tree, for a connector in add/update mode
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
	public void flush() throws Exception {
		Trace.entrymin(this, "flush");
		if (getWriter() != null && outputDoc != null) {
			String charSet = getParam("characterSet");
			if (charSet == null || charSet.equals(""))
				charSet = "UTF-8";
			OutputFormat format = new OutputFormat("xml", charSet, indentOutput); // Serialize
			// DOM
			format.setOmitXMLDeclaration(getOmitXMLDeclaration());
			XMLSerializer serial = new XMLSerializer(getWriter(), format);
			serial.asDOMSerializer(); // As a DOM Serializer
			// transform the tree
			if (getParam("Out_XSL_isFile").equals("true")) {
				String xslFname = getParam("OutputXSL_File");
				if (xslFname == null || xslFname.equals("")) {
					throw new Exception(sResHash
							.getString("PARSER.XSLXML.NO.XSL.FILE.SPECIFIED"));
				}
				outputDoc = (Document) xslTransform(xslFname, outputDoc)
						.getNode();
			} else
				outputDoc = (Document) xslTransform(
						new StringReader(getParam("OutputXSL")), outputDoc)
						.getNode();
			if ( outputDoc.getDocumentElement()!= null ) 
				serial.serialize(outputDoc.getDocumentElement());

			getWriter().flush();
		}
		Trace.exitmin(this, "flush");
	}

	/**
	 * This function is called as a part of parser initialization, when the
	 * connector having this parser is in Add mode
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initOutput() throws Exception {
		Trace.entrymax(this, "initOutput");
		if (getWriter() == null) {
			throw new Exception(
					sResHash
							.getString("PARSER.XSLXML.TRYING.TO.WRITE.XML.WITH.NO.OUTPUT"));
		}
		if (debugMode()) {
			debug(sResHash
					.getString("PARSER.XSLXML.INITIALIZE.OUTPUT.DOCUMENT"));
		}

		outputDoc = db.newDocument();

		Element toplevel = outputDoc.createElement(ROOT);
		outputDoc.appendChild(toplevel);
		Trace.exitmax(this, "initOutput");
	}

	/**
	 * This function is called as a part of parser initialization, when the
	 * connector having this parser is in Iterator mode
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initInput() throws Exception {
		Trace.entrymax(this, "initInput");
		if (getReader() == null) {
			throw new Exception(
					sResHash
							.getString("PARSER.XSLXML.TRYING.TO.READ.XML.WITH.NO.INPUT"));
		}

		if (debugMode()) {
			debug(sResHash.getString("PARSER.XSLXML.INITIALIZE.INPUT.DOCUMENT"));
		}

InputSource is =null;

		// To work around a problem, we cannot use inputStream with UTF-16
		// and possibly other multibyte characterSets

    		String charSet = getParam("characterSet");
    		if ( charSet == null )
			charSet = "";
		
    		if (getInputStream() != null && ! charSet.startsWith("UTF-16") && ! charSet.startsWith("UTF-32") ) {

    		is = new org.xml.sax.InputSource(getInputStream());
		// Set the encoding. This will only have effect if 
		// the document does not provide an encoding.
		if ( charSet.length() > 0 )
			is.setEncoding( charSet );

		} 
    		else {
    		is = new org.xml.sax.InputSource(getReader());
	 	}

		if (getParam("In_XSL_isFile").equals("true")) {
			String xslFname = getParam("InputXSL_File");
			if (xslFname == null || xslFname.equals("")) {
				throw new Exception(sResHash
						.getString("PARSER.XSLXML.NO.INPUT.XSL.FILE"));
			}
			inputDoc = (Document) xslTransform(xslFname, db.parse(is))
					.getNode();
		} else
			inputDoc = (Document) xslTransform(
					new StringReader(getParam("InputXSL")), db.parse(is))
					.getNode();

		toplevelInput = inputDoc.getDocumentElement();
		if (toplevelInput == null) {
			throw new Exception(sResHash
					.getString("PARSER.XSLXML.XML.DOCUMENT.HAS.NO.ROOT"));
		} else
			toplevelInput.normalize();

		children = toplevelInput.getChildNodes();
		if (children == null) {
			if (debugMode()) {
				debug(sResHash.getString(
						"PARSER.XSLXML.XML.DOCUMENT.HAS.NO.CHILDREN",
						toplevelInput.getTagName()));
			}
		} else {
			if (debugMode()) {
				debug(sResHash.getString(
						"PARSER.XSLXML.XML.DOCUMENT.HAS.CHILDREN", String
								.valueOf(children.getLength())));
			}
		}

		curindex = 0;

		Trace.exitmax(this, "initInput");

	}

	/**
	 * This function transforms the DOM tree created in the initInput from the
	 * {@link InputStream} to DOM in the internal format using the XSL file
	 * supplied by the user.
	 * 
	 * @param xsl
	 *            The name of XSL file
	 * @param xmlDoc
	 *            Document created from input XML
	 * @return the transformed DOM tree
	 * @throws TransformerException
	 * @throws TransformerConfigurationException
	 * @throws FileNotFoundException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private DOMResult xslTransform(Object xsl, Document xmlDoc)
			throws TransformerException, TransformerConfigurationException,
			FileNotFoundException, ParserConfigurationException, SAXException,
			IOException

	{
		Trace.entrymax(this, "xslTransform", xsl, xmlDoc);
		// xsl = "I:\\Build\\lib\\xalan-j_2_5_2\\samples\\DOM2DOM\\birds.xsl";
		TransformerFactory tFactory = TransformerFactory.newInstance();

		if (tFactory.getFeature(DOMSource.FEATURE)
				&& tFactory.getFeature(DOMResult.FEATURE)) {
			// Instantiate a DocumentBuilderFactory.
			DocumentBuilderFactory dFactory = DocumentBuilderFactory
					.newInstance();

			// And setNamespaceAware, which is required when parsing xsl files
			dFactory.setNamespaceAware(true);

			// Use the DocumentBuilderFactory to create a DocumentBuilder.
			DocumentBuilder dBuilder = dFactory.newDocumentBuilder();

			// Use the DocumentBuilder to parse the XSL stylesheet.
			Document xslDoc;
			if (xsl instanceof String)
				xslDoc = dBuilder.parse((String) xsl);
			else if (xsl instanceof StringReader)
				xslDoc = dBuilder.parse(new InputSource((StringReader) xsl));
			else {
				throw new IOException(sResHash
						.getString("PARSER.XSLXML.CANNOT.PARSE.INPUT.XSL"));
			}

			// Use the DOM Document to define a DOMSource object.
			DOMSource xslDomSource = new DOMSource(xslDoc);

			// Set the systemId: note this is actually a URL, not a local
			// filename
			xslDomSource.setSystemId("xsl");

			// Process the stylesheet DOMSource and generate a Transformer.
			Transformer transformer = tFactory.newTransformer(xslDomSource);

			// Use the DocumentBuilder to parse the XML input.
			// Document xmlDoc = dBuilder.parse(getInputStream());

			// Use the DOM Document to define a DOMSource object.
			DOMSource xmlDomSource = new DOMSource(xmlDoc);

			// Set the base URI for the DOMSource so any relative URIs it
			// contains can
			// be resolved.
			xmlDomSource.setSystemId("xml");

			// Create an empty DOMResult for the Result.
			DOMResult domResult = new DOMResult();
			domResult.setSystemId("result");

			// Perform the transformation, placing the output in the DOMResult.
			transformer.transform(xmlDomSource, domResult);
			Trace.exitmax(this, "xslTransform", domResult);
			return domResult;
		} else {
			throw new org.xml.sax.SAXNotSupportedException(
					sResHash
							.getString("PARSER.XSLXML.DOM.NODE.PROCESSING.NOT.SUPPORTED"));
		}
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
		if (children == null || curindex >= children.getLength()) {
			// System.out.println("children null");
			return null;
		}

		Node n = children.item(curindex++);
		if (n == null) {
			throw new Exception(sResHash.getString(
					"PARSER.XSLXML.EXPECTED.CHILD.ITEM.AT.INDEX", String
							.valueOf(curindex - 1)));
		}

		if (n.getChildNodes() == null || n.getChildNodes().getLength() < 1) {
			if (debugMode()) {
				debug(sResHash
						.getString("PARSER.XSLXML.XML.CHILD.NODE.HAS.NO.CHILDREN"));
			}
			return readEntry();
		}

		Entry e = new Entry();

		Node x = n.getFirstChild();
		while (x != null) {
			if (x.getNodeType() == Node.ELEMENT_NODE) {
				Node values = x.getFirstChild();

				if (!x.getNodeName().equals("Attribute")) {
					throw new Exception(
							sResHash
									.getString("PARSER.XSLXML.XSL.DOES.NOT.CONFIRM.TO.THE.DEFINED.FORMAT.1"));
				}
				String attr = x.getAttributes().item(0).getNodeValue();
				Attribute ba = new Attribute(attr);
				String val = "";
				while (values != null) {
					val = "";

					if (values.getNodeType() == Node.TEXT_NODE
							|| values.getNodeType() == Node.CDATA_SECTION_NODE) {
						val = ((String) values.getNodeValue()).trim();
					}
					if (values.getNodeType() == Node.ELEMENT_NODE) {

						if (!values.getNodeName().equals("Value")) {
							throw new Exception(
									sResHash
											.getString("PARSER.XSLXML.XSL.DOES.NOT.CONFIRM.TO.THE.DEFINED.FORMAT.2"));
						}
						Node child = values.getFirstChild();

						if (child != null
								&& child.getNodeType() == Node.TEXT_NODE) {
							val = ((String) child.getNodeValue()).trim();
						}
					} else {
						throw new Exception(
								sResHash
										.getString("PARSER.XSLXML.DONT.KNOW.HOW.TO.PROCESS"));
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
		Trace.exitmax(this, "readEntry");
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
		Element rec = outputDoc.createElement(ENTRY);
		Element child;

		String[] names = entry.getAttributeNames();
		for (int i = 0; i < names.length; i++) {
			Attribute a = entry.getAttribute(names[i]);
			child = outputDoc.createElement(ATTRIBUTE);
			child.setAttribute("name", names[i]);

			for (int j = 0; j < a.size(); j++) {
				Element value = outputDoc.createElement(VALUE);
				child.appendChild(value);
				String val = a.getValue(j).toString();
				if (val.indexOf("\n") != -1 && useCData)
					value.appendChild(outputDoc.createCDATASection(val));
				else
					value.appendChild(outputDoc.createTextNode(val));
			}
			rec.appendChild(child);
		}
		outputDoc.getDocumentElement().appendChild(rec);
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
	 *            SAXParseException
	 */
	public void fatalError(SAXParseException exception) {
	}

	/**
	 * Rethrows the given exception
	 * 
	 * @param exception
	 *            SAXParseException
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
		logmsg(sResHash.getString("PARSER.XSLXML.SAX.PARSE.WARNING.LINE",
				new Object[] { String.valueOf(err.getLineNumber()),
						err.getSystemId(), err.getMessage() }));
	}

	/**
	 * Version information.
	 * @return version information
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I% 20%E%";
	}

	/**
	 * Resets all properties
	 */
	private void resetProperties() {
		inputDoc = null;
		outputDoc = null;
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
