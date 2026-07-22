/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.XSDComponent;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.impl.XSDElementDeclarationImpl;
import org.eclipse.xsd.impl.XSDTypeDefinitionImpl;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ibm.di.connector.Connector;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.entry.Property;
import com.ibm.di.parser.ParserImpl;
import com.ibm.di.parser.ParserInterface;
import com.ibm.di.server.Log;
import com.ibm.di.server.RS;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;
import com.ibm.icu.util.StringTokenizer;

/**
 * The Parser used to parse XML documents using the XLXP implementation of the
 * StAX XML Parser. This parser is able to write XML using the same library.
 * 
 * @since 7.0
 */
public class XMLParser2 extends ParserImpl implements ParserInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash resHash = new ResourceHash("xmlparser2");

	/**
	 * The default encoding to use if the user have not specified one or we were
	 * unable to find the actual encoding
	 */
	public static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * Try reusing the inputFactory. Volatile makes sure the variable is stored
	 * in the common memory-space for all the threads. This instance is
	 * synchronized by the staticFactoryLock object.
	 */
	private static volatile XMLInputFactory inpFactory;

	/**
	 * Try reusing the outputFactory. Volatile makes sure the variable is stored
	 * in the common memory-space for all the threads. This instance is
	 * synchronized by the staticFactoryLock object.
	 */
	private static volatile XMLOutputFactory outFactory;

	/**
	 * Used to protect the shared {@link XMLInputFactory} and
	 * {@link XMLOutputFactory} objects.
	 */
	private static final Object staticFactoryLock = new Object();

	/**
	 * This is the name of the root tag that will be plugged in to handle
	 * multiple root tag XMLs. This should never be visible to the end-user
	 */
	protected static final String DUMMY_ROOT_NAME = "TDIDummyRoot";

	/** XMLStreamReader object for parsing the XML */
	private XMLStreamReader reader;

	/** XMLStreamWriter object for building the XML */
	private XMLStreamWriter writer;

	/**
	 * When parsing the XML the Parser will write in this stream so we have
	 * String representation of the returned Entry
	 */
	protected ByteArrayOutputStream currentEntryAsXML;

	/**
	 * This variable just holds the current Entry as XML string so we do not
	 * decode the stream twice or more.
	 */
	protected String currentEntryAsXMLString;

	/** the XSD URLs String */
	protected String xsdPath;

	/** the raw Simple xPath string */
	protected String xPathStr;

	/** the prefix to namespaceURI map as String */
	protected String nsMap;

	/**
	 * String which value is used for decoding the InputStream and encoding the
	 * OutputStream
	 */
	protected String charEncoding;

	/** the XML version to put in XML declaration when writing */
	protected String xmlVersion = "1.0";

	/** the attributes declaration parameter as String */
	protected String attrsDeclaration;

	/**
	 * the value of the Entry Tag parameter.<br>
	 * position 0 holds the declared prefix or null if none<br>
	 * position 1 holds the declared localName or null if none<br>
	 * position 2 holds the declared namespaceURI or null if none defined<br>
	 */
	protected String[] entryTag;

	/**
	 * the value of the Value Tag parameter. If nothing specified "value" will
	 * be assumed.<br>
	 * position 0 holds the declared prefix or null if none<br>
	 * position 1 holds the declared localName or null if none<br>
	 * position 2 holds the declared namespaceURI or null if none defined<br>
	 */
	protected String[] valueTag;
	private Attribute valueTagAttr;

	/**
	 * Flag that shows whether the entry will be wrapped in a tag or not.
	 */
	protected boolean wrapUnwrapEntry;

	/** omit XML declaration in the beginning of the output */
	protected boolean skipXMLOnWriting;

	/** skip repeating XML declarations when reading */
	protected boolean skipXMLOnReading;

	/**
	 * shows whether any text characters and the CDATA section will be treated
	 * as text.
	 */
	protected boolean coalescing;

	/** shows if the start root elements are to be written for first time */
	protected boolean firstWriteStart = true;

	/** shows if the static roots should be output on each entry */
	protected boolean standaloneRoot = true;

	/** shows whether the output should be indented */
	protected boolean indentOutput = true;
	
	/** shows whether the XML tags may contain invalid XML characters */
	private boolean permitInvalidXmlChar = false;

	/**
	 * this object is used for compiling the input parameters and when
	 * navigating through XML elements
	 */
	protected SimpleXPathEvaluator xPath;

	/**
	 * Used for tracking the written namespaces
	 */
	protected NamespacesTracker nsTracker;

	/**
	 * Constructs the XMLParser2 object.
	 */
	public XMLParser2() {
		Trace.entrymid(this, "XMLParser2");
		Trace.exitmid(this, "XMLParser2");
	}

	/**
	 * Initializes the parser.
	 * 
	 * @exception Exception
	 *                - if initialization error occurs
	 */
	public void initParser() throws Exception {
		Trace.entrymin(this, "initParser");

		firstWriteStart = true;

		String temp = getParam("characterSet");
		if (checkParamExist(temp)) {
			charEncoding = temp.trim();
		}

		temp = getParam("ns.map");
		if (checkParamExist(temp)) {
			nsMap = temp.trim();
		}

		temp = getParam("static.decl");
		if (checkParamExist(temp)) {
			attrsDeclaration = temp.trim();
		}

		temp = getParam("xpath.expr");
		if (checkParamExist(temp)) {
			xPathStr = temp.trim();
		}

		temp = getParam("schema.url");
		if (checkParamExist(temp)) {
			xsdPath = temp.trim();
		}

		temp = getParam("entry.tag");
		if (checkParamExist(temp)) {
			entryTag = new String[3];
			SimpleXPathEvaluator.separatePrefixAndLocalName(temp.trim(), entryTag);
			wrapUnwrapEntry = true;
		}
		
		temp = getParam("value.tag");
		// For now, do not accept "*" as a value tag.
		if (checkParamExist(temp) && ! temp.equals("*")) {
			valueTag = new String[3];
			SimpleXPathEvaluator.separatePrefixAndLocalName(temp.trim(), valueTag);
		}

		temp = getParam("omit.xml.decl.on.reading");
		if (checkParamExist(temp)) {
			skipXMLOnReading = Boolean.parseBoolean(temp.trim());
		}

		temp = getParam("omit.xml.decl.on.writing");
		if (checkParamExist(temp)) {
			skipXMLOnWriting = Boolean.parseBoolean(temp.trim());
		}

		temp = getParam("coalescing");
		if (checkParamExist(temp)) {
			coalescing = Boolean.parseBoolean(temp.trim());
		}

		temp = getParam("standalone.root");
		if (checkParamExist(temp)) {
			standaloneRoot = Boolean.parseBoolean(temp.trim());
		}

		temp = getParam("indent.output");
		if (checkParamExist(temp)) {
			indentOutput = Boolean.parseBoolean(temp.trim());
		}
		
		temp = getParam("invalid.xml.char");
		if (checkParamExist(temp)) {
			permitInvalidXmlChar = Boolean.parseBoolean(temp.trim());
		}

		if (getReader() != null) {
			initInput();
		}

		if (getWriter() != null) {
			initOutput();
		}

		super.initParser();
		Trace.exitmin(this, "initParser");
	}

	/**
	 * Initializes the Input. Note: this parser relies on a Reader object to
	 * read the XML document.
	 * 
	 * @see #setInputStream(java.io.Reader)
	 * 
	 * @throws Exception
	 *             - in case of a read error occurs.
	 */
	protected void initInput() throws Exception {
		Trace.entrymax(this, "initInput");

		if (getReader() == null) {
			throw new Exception(resHash.getString("XML.PARSER.2.NOINPUT.ERROR"));
		}

		xPath = new SimpleXPathEvaluator(xPathStr, nsMap, attrsDeclaration, entryTag, getLogger());

		printDebugMessage("XML.PARSER.2.INIT.INPUT", null);

		xPath.compileForReading();
		Reader xmlReader = getXMLReader();

		synchronized (staticFactoryLock) {

			// reuse the input factory. Each initialization is a performance hit
			if (inpFactory == null) {
				inpFactory = XMLInputFactory.newInstance();
			}

			inpFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.valueOf(coalescing));

			// we are always namespaceURI aware
			inpFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);

			inpFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);

			inpFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);

			if (reader != null)
				reader.close();

			reader = inpFactory.createXMLStreamReader(xmlReader);

		}

		Trace.exitmax(this, "initInput");
	}

	/**
	 * Get a Reader object after the input stream encoding is found.
	 * 
	 * @return an instance of the TDIReaderProxy class that wraps the standard
	 *         input to provide some workarounds.
	 * @throws IOException
	 *             derived from operating over the underlying input stream.
	 */
	private Reader getXMLReader() throws IOException {
		Reader tdiReader = null;

		if (getInputStream() != null) {
			if (charEncoding == null) {
				// if we are in here this means that the user have not provided
				// the characterSet parameter so we need to find it from the
				// provided inputStream
				XMLInputStreamDecoder decoder = new XMLInputStreamDecoder(getInputStream(), true);

				// set the encoding found form the stream so it will be used
				// when writing
				charEncoding = decoder.getEncoding();
				printDebugMessage(decoder.getStatus(), new Object[] { charEncoding });
				tdiReader = decoder;
			}

		} else {
			// at this point the charEncoding flag might not be set if the
			// parser was initialized by a script with a StringReader
			// object.
			// Just set the default encoding which will be used when saving
			// the parsed entry as a xml string.
			charEncoding = charEncoding == null ? DEFAULT_ENCODING : charEncoding;
		}

		if (tdiReader == null) {
			tdiReader = getReader();
		}
		return new TDIReaderProxy(tdiReader, skipXMLOnReading);
	}

	/**
	 * Initializes the Output.
	 * 
	 * @throws Exception
	 *             - in case of write error occurs.
	 */
	protected void initOutput() throws Exception {
		Trace.entrymax(this, "initOutput");

		if (getWriter() == null) {
			throw new Exception(resHash.getString("XML.PARSER.2.NOOUTPUT.ERROR"));
		}

		if (entryTag != null) {
			if (entryTag[0] != null && entryTag[0].contains(SimpleXPathEvaluator.WILDCARD)) {
				entryTag[0] = null;
			}
			if (entryTag[1] != null && entryTag[1].contains(SimpleXPathEvaluator.WILDCARD)) {
				entryTag[1] = "Entry";
			}
		}
		if (valueTag != null) {
			if (valueTag[0] != null && valueTag[0].contains(SimpleXPathEvaluator.WILDCARD)) {
				valueTag[0] = null;
			}
			if (valueTag[1] != null && valueTag[1].contains(SimpleXPathEvaluator.WILDCARD)) {
				valueTag[1] = "ValueTag";
			}

			valueTagAttr = new Attribute(valueTag[0] == null ? valueTag[1] : valueTag[0] + ":" + valueTag[1], valueTag[2], false);
		}

		xPath = new SimpleXPathEvaluator(xPathStr, nsMap, attrsDeclaration, entryTag, getLogger());

		printDebugMessage("XML.PARSER.2.INIT.OUTPUT", null);

		xPath.compileForWriting();

		synchronized (staticFactoryLock) {

			// reuse the output factory. Each initialization is performance hit
			if (outFactory == null) {
				outFactory = XMLOutputFactory.newInstance();
			}

			if (writer != null)
				writer.close();

			if (getOutputStream() != null) {
				// from performance point of view it is better to use the output
				// stream than the writer
				if (charEncoding == null)
					writer = outFactory.createXMLStreamWriter(getOutputStream());
				else
					writer = outFactory.createXMLStreamWriter(getOutputStream(), charEncoding);
			} else {
				writer = outFactory.createXMLStreamWriter(getWriter());
			}
		}

		nsTracker = new NamespacesTracker();
		Trace.exitmax(this, "initOutput");
	}

	/**
	 * Retrieves an Entry object from the XML document.
	 * 
	 * @return an Object Model of the parsed XML, or null if no more data could
	 *         be retrieved
	 * 
	 * @exception Exception
	 *                - if an error occurs while parsing
	 */
	public Entry readEntry() throws Exception {
		Trace.entrymin(this, "readEntry");
		if (xPath == null) {
			// at this phase of the project we cannot define new messages so I
			// am reusing an existing one to avoid NPE when the user has not
			// called initParser().
			throw new Exception(resHash.getString("XML.PARSER.2.NOINPUT.ERROR"));
		}

		Entry result = null;

		// place the cursor on the opening tag
		if (gotoNextElement()) {
			printDebugMessage("XML.PARSER.2.ELEMENT.FOUND", null);
			result = parseElement();

		} else {
			printDebugMessage("XML.PARSER.2.ELEMENT.NOT.FOUND", null);
			result = null;
		}

		Trace.exitmin(this, "readEntry", result);
		return result;
	}

	/**
	 * Writes the passed as parameter Entry object as a XML data.
	 * 
	 * @param entry
	 *            - the entry that should be written as XML
	 * @exception Exception
	 *                - in case of write error occurs
	 */
	public void writeEntry(Entry entry) throws Exception {
		Trace.entrymin(this, "writeEntry");

		if (xPath == null) {
			// at this phase of the project we cannot define new messages so I
			// am reusing an existing one to avoid NPE when the user has not
			// called initParser().
			throw new Exception(resHash.getString("XML.PARSER.2.INIT.OUTPUT"));
		}

		// write the static parent tags
		int tabCounter = writeStartTags(entry);

		// just write the xml declaration and exit
		if (entry == null) {
			printDebugMessage("XML.PARSER.2.NULL.ENTRY.PASSED", null);
			return;
		}
		
		Attribute attr = entry.getFirstChild();
		while (attr != null) {
			if (!permitInvalidXmlChar && isInValidXMLCharacters(attr.getNodeName())) {
				throw new Exception(resHash.getString("XML.PARSER.2.INVALID.XML.CHARACTER.FOUND.IN.ENTRY.TAG"));
			}
			attr = (Attribute) attr.getNextSibling();
		}

		// entry wrapping...
		if (wrapUnwrapEntry) {
			writeStaticStartTag(tabCounter++, entryTag);
		}

		attr = entry.getFirstChild();
		// output the entry information
		while (attr != null) {
			writeAttribute(attr, tabCounter, wrapUnwrapEntry);
			attr = (Attribute) attr.getNextSibling();
		}

		// entry wrapping...
		if (wrapUnwrapEntry) {
			writeStaticEndTag(--tabCounter);
		}

		if (standaloneRoot)
			// close the static parent tags
			writeEndTags();

		writer.flush();
		Trace.exitmin(this, "writeEntry");
	}
	
	/**
	 * This method detects invalid xml characters Tag names cannot contain any
	 * of the characters !"#$%&'()*+,/;<=>?@[\]^`{|}~, nor a space character,
	 * and cannot start with -, ., or a numeric digit. 
	 * XML 1.0 standard. For reference, please see <a
	 * href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
	 * standard</a>. This method will return false if the input is null or
	 * empty.
	 * 
	 * @param attributeName
	 *            The String whose non-valid characters are to be found.
	 * @return boolean invalid character found or not found.
	 */
	private boolean isInValidXMLCharacters(String attributeName) {
		if (attributeName == null || "".equals(attributeName))
			return false;

		for (int i = 0; i < attributeName.length(); i++) {
			char current = attributeName.charAt(i);

			if (i == 0) {
				// Start character
				if (current >= '0' && current <= '9')
					return true; // Cannot start with number
				if (current == '-' || current == '.')
					return true; // Cannot start with - or .
			}

			if ( (current >= 0x20 && current <= 0x2C) ||
					(current == '/') ||
					(current >= 0x3B && current <= 0x40) ||
					(current >= 0x5B && current <= 0x5E) ||
					(current == 0x60) ||
					(current >= 0x7B && current <= 0x7F))
				return true;
		}
		return false;
	}

	/**
	 * print the version of the component
	 * 
	 * @return the version as String
	 */
	public String getVersion() {
		return "1.3-di7.1.1 %I%, 20%E%";
	}

	/**
	 * The method moves the {@link XMLStreamReader} object to the next element.
	 * 
	 * @return true if the next element is found.
	 * @throws XMLStreamException
	 *             if error during reading occurs.
	 */
	private boolean gotoNextElement() throws XMLStreamException {
		if (reader.getEventType() == XMLStreamConstants.END_DOCUMENT)
			return false;

		String localName = null;
		String prefix = null;
		String namespaceURI = null;
		boolean foundElement = false;

		for (int event = reader.next(); reader.hasNext(); event = reader.next()) {

			switch (event) {
			case XMLStreamConstants.START_DOCUMENT: {

				xmlVersion = reader.getVersion() == null ? xmlVersion : reader.getVersion();
				break;
			}
			case XMLStreamConstants.START_ELEMENT: {

				prefix = reader.getPrefix();
				localName = reader.getLocalName();
				namespaceURI = isNamespaceNull(reader.getNamespaceURI()) ? null : reader.getNamespaceURI();

				if (isPrefixNull(prefix) && localName.equals(DUMMY_ROOT_NAME) && xPath.getLevelSize() == 0)
					// advance to the next element and ignore the dummy one
					break;

				switch (xPath.match(prefix, localName, namespaceURI)) {
				case SimpleXPathEvaluator.EXACT_MATCH_FOUND: {
					// so we are there
					return true;
				}
				case SimpleXPathEvaluator.PARTIAL_MATCH_FOUND: {
					// go ahead and check the next child start tag
					continue;
				}
				case SimpleXPathEvaluator.NO_MATCH_FOUND: {
					// take a step back
					exitElement(reader, namespaceURI, localName);
				}
				}
				break;

			}
			case XMLStreamConstants.END_ELEMENT: {

				// the only time when we get here is when we have entered in an
				// element which is part of the path but not the last one (the
				// last one have been exited at the end of handleElement
				// method). We won't get here if the element is not part of the
				// specified path. That element is handled by the exitElement
				// method

				prefix = reader.getPrefix();
				localName = reader.getLocalName();
				namespaceURI = isNamespaceNull(reader.getNamespaceURI()) ? null : reader.getNamespaceURI();

				if (isPrefixNull(prefix) && localName.equals(DUMMY_ROOT_NAME) && xPath.getLevelSize() == 0) {
					// found the last dummy root tag
					break;
				}

				if (xPath.checkEquality(new String[] { prefix, localName, namespaceURI })) {

					xPath.decreaseCurrentLevel();
					break;
				}

				break;
			}
			case XMLStreamConstants.END_DOCUMENT: {
				printDebugMessage("XML.PARSER.2.END.OF.DOCUMENT.REACHED", null);
			}
			} // end switch
		}

		return foundElement;
	}

	/**
	 * The method exits from the current and all child elements
	 * 
	 * @param reader
	 *            {@link XMLStreamReader} object.
	 * @param namespaceURI
	 *            namespace
	 * @param localName
	 *            local name of the tag.
	 * @throws XMLStreamException
	 *             if error during reading next element occurs.
	 */
	private void exitElement(XMLStreamReader reader, String namespaceURI, String localName) throws XMLStreamException {
		boolean isOut = false;
		do {
			try {
				reader.require(XMLStreamConstants.END_ELEMENT, namespaceURI, localName);
				isOut = true;
			} catch (XMLStreamException xmlse) {
				// thrown if the criteria don't match the required one
			}
		} while (!isOut && reader.next() != XMLStreamConstants.END_DOCUMENT);
	}

	/**
	 * The method parses XML element and passes its values (Encoding , Version ,
	 * Starting tag, Text information and CDATA) to an Entry Object.
	 * 
	 * @return Entry
	 * @throws XMLStreamException
	 *             if error during writing/reading of data occurs.
	 */
	private Entry parseElement() throws XMLStreamException {

		if (currentEntryAsXML != null) {
			currentEntryAsXML.reset();
			currentEntryAsXMLString = null;
		} else {
			// create an XML holder
			currentEntryAsXML = new ByteArrayOutputStream();
			currentEntryAsXMLString = null;
		}

		XMLStreamWriter sw = null;
		Entry result = new Entry(true);
		Element currentElement = null;
		NamespacesTracker parseNSTracker = new NamespacesTracker();

		synchronized (staticFactoryLock) {
			if (outFactory == null) {
				outFactory = XMLOutputFactory.newInstance();
			}

			outFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.FALSE);

			// create XML writer form the default factory
			sw = outFactory.createXMLStreamWriter(currentEntryAsXML, charEncoding);
		}

		// write the XML declaration
		sw.writeStartDocument(charEncoding, xmlVersion);
		sw.writeCharacters("\n");

		result.setXmlEncoding(charEncoding);
		result.setXmlVersion(xmlVersion);

		if (entryTag == null) {
			currentElement = parseStartTag(sw, result, null, parseNSTracker);
		} else {
			// we need to skip the wrapping element so we use dummy objects to
			// avoid adding the new element to the real entry object.
			parseStartTag(sw, new Entry(true), null, new NullNamespacesTracker());
		}

		// the event identifier
		int event = -1;
		// this one tracks the level of deepness
		int level = 0;
		// this is set to true when we have reached the closing tag of the
		// element
		boolean endOfElement = false;

		StringBuilder textContainer = null;

		// This shows whether we are currently unwrapping an attribute value.
		// -1 means not unwrapping, otherwise the value is the level
		// where we started unwrapping
		int unwrappingValue = -1;

		while (reader.hasNext() && !endOfElement) {
			event = reader.next();

			if (event != XMLStreamConstants.CHARACTERS && textContainer != null) {

				// write the text buffer as a Text element.
				String val = textContainer.toString().trim();
				if (currentElement != null && val.length() > 0) {
					currentElement.appendChild(result.createTextNode(val));
				}

				sw.writeCharacters(val);

				textContainer = null;
			}

			switch (event) {
			case XMLStreamConstants.START_ELEMENT: {
				// write the opening tag in the XML

				if (valueTag != null &&	unwrappingValue == -1 &&
						SimpleXPathEvaluator.internalCheckEquality(
								new String[] {isPrefixNull(reader.getPrefix()) ? null : reader.getPrefix(),
										reader.getLocalName(),
										isNamespaceNull(reader.getNamespaceURI()) ? null : reader.getNamespaceURI() },
								valueTag)) {
					unwrappingValue = level;
				} else {
					parseNSTracker.increaseLevel();

					currentElement = parseStartTag(sw, result, currentElement, parseNSTracker);

					level++;
				}
				break;
			}
			case XMLStreamConstants.CDATA: {
				if (currentElement != null) {
					currentElement.appendChild(result.createCDATASection(reader.getText()));
				}

				sw.writeCData(reader.getText());

				break;
			}
			case XMLStreamConstants.SPACE: {
				// ignore this
				break;
			}
			case XMLStreamConstants.CHARACTERS: {
				// if coalescing is true CDATA sections will be handled here
				// as Characters

				// if the text contains entities then each of them would be
				// reported separately which would create several AttributeText
				// objects. This is why we store the text in a buffer to work
				// around this.
				if (textContainer != null) {
					textContainer.append(reader.getText());
				} else {
					textContainer = new StringBuilder(reader.getText());
				}

				break;
			}
			case XMLStreamConstants.COMMENT: {
				sw.writeComment(reader.getText());
				break;
			}
			case XMLStreamConstants.DTD: {
				sw.writeDTD(reader.getText());
				break;
			}
			case XMLStreamConstants.PROCESSING_INSTRUCTION: {
				sw.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
				break;
			}
			case XMLStreamConstants.END_ELEMENT: {
				if (level == 0) {
					endOfElement = true;

					// decrease the xpath.level tracker
					xPath.decreaseCurrentLevel();

					sw.writeEndElement();
					break;
				}

				if (unwrappingValue == level) {
					// we are exiting the skipped value element.
					unwrappingValue = -1;
				} else {
					sw.writeEndElement();
					parseNSTracker.decreaseLevel();
					if (currentElement != null) {
						currentElement = (Element) currentElement.getParentNode();
					}
					level--;
				}
				break;
			}
			}

		}

		sw.writeEndDocument();
		sw.close();

		return result;

	}

	/**
	 * Parses start tag.
	 * 
	 * @param writer
	 *            {@link XMLStreamWriter}
	 * @param doc
	 *            {@link Entry}
	 * @param currentElement
	 *            {@link Attribute}
	 * @param parseNSTracker
	 *            {@link NamespacesTracker}
	 * @return Element in terms of XML concepts
	 * @throws XMLStreamException
	 */
	private Element parseStartTag(XMLStreamWriter writer, Document doc, Element currentElement, NamespacesTracker parseNSTracker)
			throws XMLStreamException {

		Element resultElement = null;

		// the element identifiers
		String prefix = reader.getPrefix();
		String localName = reader.getLocalName();
		String namespaceURI = reader.getNamespaceURI();

		// write the opening tag in the XML
		if (isNamespaceNull(namespaceURI)) {
			writer.writeStartElement(localName);
			resultElement = doc.createElement(localName);
		} else if (isPrefixNull(prefix)) {

			boolean notExists = !parseNSTracker.contains(null, namespaceURI);

			if (namespaceURI != null) {
				writer.setDefaultNamespace(namespaceURI);
			}

			writer.writeStartElement(namespaceURI, localName);
			resultElement = doc.createElementNS(namespaceURI, localName);

			if (notExists) {
				writer.writeDefaultNamespace(namespaceURI);
				parseNSTracker.addPrefix(null, namespaceURI);
				resultElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, namespaceURI);
			}

		} else {
			writer.writeStartElement(prefix, localName, namespaceURI);
			resultElement = doc.createElementNS(namespaceURI, prefix + ':' + localName);

			if (!parseNSTracker.contains(prefix, namespaceURI)) {
				// declare the prefix since this is the first time the prefix is
				// used in this XML part
				parseNSTracker.addPrefix(prefix, namespaceURI);

				// declare the prefix in the XML
				writer.writeNamespace(prefix, namespaceURI);

				// put it as a Property to the Attribute
				resultElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix,
						namespaceURI);
			}
		}

		// attach the newly created Element
		if (currentElement == null) {
			Attribute old = null;
			// first element
			if (doc instanceof Entry && resultElement instanceof Attribute ) {
				old = ((Entry)doc).getAttribute(((Attribute)resultElement).getName());
			}
			if (old != null) {
				resultElement = old;
			} else {
				doc.appendChild(resultElement);
			}
		} else {
			currentElement.appendChild(resultElement);
		}

		// write namespaceURI declarations as attributes to the new Element
		for (int i = 0; i < reader.getNamespaceCount(); i++) {
			String contextPrefix = isPrefixNull(reader.getNamespacePrefix(i)) ? null : reader.getNamespacePrefix(i);
			String contextNS = isNamespaceNull(reader.getNamespaceURI(i)) ? null : reader.getNamespaceURI(i);

			if (!parseNSTracker.contains(contextPrefix, contextNS)) {

				writer.writeNamespace(contextPrefix, contextNS);

				resultElement
						.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, (contextPrefix == null ? XMLConstants.XMLNS_ATTRIBUTE
								: XMLConstants.XMLNS_ATTRIBUTE + ":" + contextPrefix), contextNS);

				parseNSTracker.addPrefix(contextPrefix, contextNS);
			}
		}

		// write other attributes
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			String attPrefix = reader.getAttributePrefix(i);
			String attLocalName = reader.getAttributeLocalName(i);
			String attNS = reader.getAttributeNamespace(i);
			String attValue = reader.getAttributeValue(i);

			attNS = isPrefixNull(attPrefix) && isNamespaceNull(attNS) ? reader.getNamespaceURI("") : attNS;

			if (isNamespaceNull(attNS)) {
				writer.writeAttribute(attLocalName, attValue);

				resultElement.setAttribute(attLocalName, attValue);

			} else if (isPrefixNull(attPrefix)) {

				boolean notExists = !parseNSTracker.contains(null, attNS);

				if (attNS != null) {
					writer.setDefaultNamespace(attNS);
				}

				writer.writeAttribute(attNS, attLocalName, attValue);

				resultElement.setAttributeNS(attNS, attLocalName, attValue);

				if (notExists) {

					writer.writeDefaultNamespace(attNS);

					parseNSTracker.addPrefix(null, attNS);

					resultElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, attNS);
				}

			} else {

				boolean notExists = !parseNSTracker.contains(attPrefix, attNS);

				if (notExists) {
					writer.writeNamespace(attPrefix, attNS);
				}

				writer.writeAttribute(attPrefix, attNS, attLocalName, attValue);

				resultElement.setAttributeNS(attNS, attPrefix + ":" + attLocalName, attValue);

				if (notExists) {

					parseNSTracker.addPrefix(attPrefix, attNS);

					resultElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE + ":"
							+ attPrefix, attNS);
				}
			}
		}

		return resultElement;
	}

	/**
	 * Writes opening static parent tags.
	 * 
	 * @param entry
	 *            input Entry to read from.
	 * @return the number of tags written.
	 * @throws Exception
	 *             if an error occurs
	 */
	private int writeStartTags(Entry entry) throws Exception {
		int tabCounter = 0;
		if (standaloneRoot || firstWriteStart) {
			firstWriteStart = false;

			if (!skipXMLOnWriting) {

				// write XML declaration
				if (entry == null)
					writer.writeStartDocument();
				else if (charEncoding == null)
					writer.writeStartDocument(entry.getXmlVersion());
				else
					writer.writeStartDocument(charEncoding, entry.getXmlVersion());
				newLine();
				newLine();
			}

			// write entry's parent tags
			if (xPath.getPath(0) != null) {
				String[] element = null;
				for (int i = 0; i < xPath.getPath(0).length; i++) {
					element = xPath.getPath(0)[i];
					writeStaticStartTag(tabCounter++, element);
				}
			}
		} else if (xPath.getPath(0) != null) {
			tabCounter = xPath.getPath(0).length;
		}
		return tabCounter;
	}

	/**
	 * Writes an opening tag based on the element array passed as argument.<br>
	 * The element array is checked whether the first element is null or not.
	 * The second element must be present and should be the local name of the
	 * element to output. This method uses both
	 * {@link SimpleXPathEvaluator#getDecl(int)} and
	 * {@link SimpleXPathEvaluator#getAttr(int)} to output the statically
	 * defined prefixes and attributes on this element level.
	 * 
	 * @param tabCounter
	 *            - this is both the number of tabs to prepend to the element
	 *            and the level of depth of the tree, the element will be placed
	 *            into.
	 * @param element
	 *            - a String array which first position holds the prefix or null
	 *            if none is defined, and the second is the local name of the
	 *            element.
	 * @throws Exception
	 *             if error occurs.
	 */
	private void writeStaticStartTag(int tabCounter, String[] element) throws Exception {
		// the tabCounter should match the level we are currently writing
		int currentLevel = tabCounter;
		nsTracker.increaseLevel();

		if (element[0] == null) {
			// default NS declaration
			// check current level
			String defaultNS = xPath.getNSForLevelUP(currentLevel, null);

			if (defaultNS == null) {

				// no default ns declared!
				indentElement(tabCounter++);
				writer.writeStartElement(element[1]);
			} else {
				writer.setDefaultNamespace(defaultNS);

				indentElement(tabCounter++);
				writer.writeStartElement(defaultNS, element[1]);

				if (!nsTracker.contains(null, defaultNS)) {
					nsTracker.addPrefix(null, defaultNS);
					writer.writeDefaultNamespace(defaultNS);
				}
			}
		} else {
			// check current level
			String namespaceURI = xPath.getNSForLevelUP(currentLevel, element[0]);

			if (namespaceURI == null) {
				throw new Exception(resHash.getString("XML.PARSER.2.NO.PREFIX.DECLARED", element[0]));
			}

			indentElement(tabCounter++);
			writer.writeStartElement(element[0], element[1], namespaceURI);

			if (!nsTracker.contains(element[0], namespaceURI)) {
				nsTracker.addPrefix(element[0], namespaceURI);
				writer.writeNamespace(element[0], namespaceURI);
			}
		}

		// write prefix:namespaces declarations
		Iterator<String> it = xPath.getDecl(currentLevel).keySet().iterator();
		while (it.hasNext()) {
			String prefix = it.next();

			String namespaceURI = xPath.getDecl(currentLevel).get(prefix);

			if (!nsTracker.contains(prefix, namespaceURI)) {
				writer.writeNamespace(prefix, namespaceURI);
				nsTracker.addPrefix(prefix, namespaceURI);
			}
		}

		// write attributes declarations
		Iterator<SimpleXPathEvaluator.AttrEntityKey> it2 = xPath.getAttr(currentLevel).keySet().iterator();
		while (it2.hasNext()) {
			SimpleXPathEvaluator.AttrEntityKey key = it2.next();

			if (key.getPrefix() != null) {
				String namespaceURI = xPath.getNSForLevelUP(currentLevel, key.getPrefix());

				if (namespaceURI == null) {
					throw new Exception(resHash.getString("XML.PARSER.2.NO.PREFIX.DECLARED", key.getPrefix()));
				}

				writer.writeAttribute(key.getPrefix(), namespaceURI, key.getLocalName(), xPath.getAttr(currentLevel).get(key));
			} else {
				writer.writeAttribute(key.getLocalName(), xPath.getAttr(currentLevel).get(key));
			}
		}
		newLine();
	}

	private void writeStaticEndTag(int tabCounter) throws XMLStreamException {
		indentElement(tabCounter);
		writer.writeEndElement();
		newLine();
		nsTracker.decreaseLevel();
	}

	/**
	 * Writes the static parent closing tags.
	 * 
	 * @throws XMLStreamException
	 *             if error occurs.
	 */
	private void writeEndTags() throws XMLStreamException {

		if (firstWriteStart) {
			// oops no Start Elements have been written yet
			return;
		}
		if (xPath.getPath(0) != null) {

			for (int i = xPath.getPath(0).length; --i >= 0;) {
				writeStaticEndTag(i);
			}

			writer.writeEndDocument();
			newLine();
		}
	}

	/**
	 * Routine for writing the startElement to the output stream.
	 * 
	 * @param element
	 *            the element to output. First index holds prefix, second -
	 *            localName and the third one holds the namespace.
	 * @param empty
	 *            specify whether the element that will be output is an empty
	 *            one.
	 * @throws XMLStreamException
	 */
	private void writeAttributeStart(Attribute element, boolean empty) throws XMLStreamException {
		if (element.getNamespaceURI() == null) {

			String namespaceURI = nsTracker.getNamespace(element.getPrefix());

			if (element.getPrefix() != null && namespaceURI != null) {
				printDebugMessage("XML.PARSER.2.WARNING.ELEMENT.WITHOUT.NS", new Object[] { element.getPrefix() + ":"
						+ element.getLocalName() });

				if (!empty) {
					writer.writeStartElement(element.getPrefix(), element.getLocalName(), namespaceURI);
				} else {
					writer.writeEmptyElement(element.getPrefix(), element.getLocalName(), namespaceURI);
				}
			} else {
				if (element.getPrefix() != null && namespaceURI == null) {
					printDebugMessage("XML.PARSER.2.WARNING.NO.PREFIX.DECLARATION.FOUND", new Object[] { element.getPrefix() });
				}

				if (!empty) {
					writer.writeStartElement(element.getLocalName());
				} else {
					writer.writeEmptyElement(element.getLocalName());
				}
			}

		} else if (element.getPrefix() == null) {

			boolean dontExist = !nsTracker.contains(null, element.getNamespaceURI());
			if (dontExist)
				writer.setDefaultNamespace(element.getNamespaceURI());

			if (!empty) {
				writer.writeStartElement(element.getNamespaceURI(), element.getLocalName());
			} else {
				writer.writeEmptyElement(element.getNamespaceURI(), element.getLocalName());
			}

			if (dontExist) {
				writer.writeDefaultNamespace(element.getNamespaceURI());
				nsTracker.addPrefix(null, element.getNamespaceURI());
			}

		} else {

			if (!empty) {
				writer.writeStartElement(element.getPrefix(), element.getLocalName(), element.getNamespaceURI());
			} else {
				writer.writeEmptyElement(element.getPrefix(), element.getLocalName(), element.getNamespaceURI());
			}

			if (!nsTracker.contains(element.getPrefix(), element.getNamespaceURI())) {
				writer.writeNamespace(element.getPrefix(), element.getNamespaceURI());
				nsTracker.addPrefix(element.getPrefix(), element.getNamespaceURI());
			}
		}
	}

	/**
	 * Writes the information from the entry's Attribute (Element, CDATA or
	 * Text).
	 * 
	 * @param element
	 *            Attribute to read from
	 * @param tabCounter
	 *            number of tabs to be written
	 * @throws XMLStreamException
	 *             if unable to complete a XML Stream operation.
	 */
	private void writeAttribute(Attribute element, int tabCounter, boolean wrapValues) throws XMLStreamException {
		if (writeSimpleAttribute(element, tabCounter))
			return;
		nsTracker.increaseLevel();

		boolean empty = element.getChildNodes().getLength() == 0;

		indentElement(tabCounter);

		// write start tag
		writeAttributeStart(element, empty);

		// write Properties as attributes
		writeElementProperties(element);

		if (!empty) {
			boolean indent = true;

			// write children
			indent = writeChildrenAndValues(element, tabCounter + 1, wrapValues);

			if (indent) {
				indentElement(tabCounter);
			}

			writer.writeEndElement();

		}

		newLine();
		nsTracker.decreaseLevel();
	}

	/**
	 * Write Properties as attributes
	 * @param element
	 * @throws XMLStreamException
	 */
	private void writeElementProperties(Attribute element) throws XMLStreamException {

		if (!element.hasAttributes())
			return;

		NamedNodeMap attributes = element.getAttributes();
		Property prop;
		
		// Write namespaces first
		for (int i = 0; i < attributes.getLength(); i++) {
			prop = (Property) attributes.item(i);

			// look for "xmlns" attributes.
			if (XMLConstants.XMLNS_ATTRIBUTE.equals(prop.getPrefix())) {
				// see if this default namespace has been declared for this
				// context
				if (!nsTracker.contains(prop.getLocalName(), prop.getValue())) {
					// prefix to NS declaration.
					writer.writeNamespace(prop.getLocalName(), prop.getValue());
					nsTracker.addPrefix(prop.getLocalName(), prop.getValue());
				}
			} else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prop.getLocalName()) && !nsTracker.contains(null, prop.getValue())) {
				// see if this namespace has been declared for this context
				writer.writeDefaultNamespace(prop.getValue());
				nsTracker.addPrefix(null, prop.getValue());
			}
		}

		// Then write the attributes that are not namespaces
		for (int i = 0; i < attributes.getLength(); i++) {
			prop = (Property) attributes.item(i);

			if (XMLConstants.XMLNS_ATTRIBUTE.equals(prop.getPrefix()) ||
					XMLConstants.XMLNS_ATTRIBUTE.equals(prop.getLocalName()))
				continue;

			// regular attribute declaration.
			String namespaceURI = nsTracker.getNamespace(prop.getPrefix());

			if (namespaceURI == null && prop.getNamespaceURI() != null) {
				if (prop.getPrefix() != null) {
					// implicit prefix to NS declaration.
					writer.writeNamespace(prop.getPrefix(), prop.getValue());
					nsTracker.addPrefix(prop.getPrefix(), prop.getValue());

				} else {
					// implicit default NS declaration
					writer.writeDefaultNamespace(prop.getValue());
					nsTracker.addPrefix(null, prop.getValue());
				}
				namespaceURI = prop.getNamespaceURI();
			}

			if (namespaceURI != null) {
				if (prop.getPrefix() != null) {
					writer.writeAttribute(prop.getPrefix(), namespaceURI, prop.getLocalName(), prop.getValue());
				} else {
					writer.writeAttribute(namespaceURI, prop.getLocalName(), prop.getValue());
				}
			} else {
				writer.writeAttribute(prop.getNodeName(), prop.getValue());
			}
		}
	}

	/**
	 * Return true if the Attribute can be written in a simple way.
	 * E.g. 
	 * <Element>value1</ELement>
	 * <Element>value2</Element>
	 * The Attribute is also written out, if possible.
	 * The conditions for this to happen is:
	 * No valueTag.
	 * At least two values in the Attribute.
	 * No named attributes in the Attribute.
	 * @param element
	 * @param tabCounter
	 * @return true if we wrote the Attribute
	 * @throws XMLStreamException
	 */
	private boolean writeSimpleAttribute(Attribute element, int tabCounter)  throws XMLStreamException {
		if (valueTag != null)
			return false;

		NodeList children = element.getChildNodes();
		if (children.getLength() <= 1)
			return false;

		for (int i = 0; i < children.getLength(); i++) {
			Node value = children.item(i);
			if (value != null && value.getNodeType() ==  Node.ELEMENT_NODE)
				return false;
		}

		boolean hasWritten = false;
		for (int i = 0; i < children.getLength(); i++) {
			Node value = children.item(i);
			if (value == null)
				continue;

			String data = null;
			if (value.getNodeType() == Node.TEXT_NODE ||
				value.getNodeType() == Node.CDATA_SECTION_NODE) {
				data = value.getNodeValue();
			}

			if (data == null)
				continue;
			
			// write start tag
			indentElement(tabCounter);
			writeAttributeStart(element, false);
			writeElementProperties(element);

			// output the text
			if (data.indexOf('\n') >= 0) {
				writer.writeCData(data);
			} else {
				writer.writeCharacters(data);
			}

			// write end tag
			writer.writeEndElement();
			newLine();
			hasWritten = true;
		}

		return hasWritten;
	}

	private boolean isPrefixNull(String prefix) {
		return prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX);
	}

	private boolean isNamespaceNull(String ns) {
		return ns == null || ns.equals(XMLConstants.NULL_NS_URI);
	}

	/**
	 * Output the value at the specified position within the parent.
	 * 
	 * @param parent
	 *            the Attribute holding the value.
	 * @param tabCount
	 *            the count of "\t" characters to write to indent the value.
	 * @param wrapValues
	 *            specify whether the values of an Attribute will be wrapped
	 * @return true if the output value was output and the next element should
	 *         be indented or false if the next element should not be indented.
	 * @throws XMLStreamException
	 *             if an error writing to the output stream occurs or if the
	 *             valueTag has a prefix which is not defined for the current
	 *             element context.
	 */
	private boolean writeChildrenAndValues(Attribute parent, int tabCount, boolean wrapValues) throws XMLStreamException {

		boolean indent = false;
		NodeList children = parent.getChildNodes();
		Node value = null;
		boolean writeValueTag = valueTag != null && children.getLength() > 1;

		for (int i = 0; i < children.getLength(); i++) {
			value = children.item(i);
			if (value == null) {
				continue;
			}

			if (value.getNodeType() == Node.ELEMENT_NODE) {
				if (i == 0) {
					newLine();
				}
				writeAttribute((Attribute) value, tabCount, false);
				indent = true;
			} else if (value.getNodeType() == Node.TEXT_NODE) {

				// write Text Section
				Text text = (Text) value;

				if (text.getNodeValue() != null && text.getNodeValue().length() > 0) {

					if (i != 0) {
						indentElement(tabCount);
					} else if (children.getLength() > 1) {
						newLine();
						indentElement(tabCount);
					}

					if (writeValueTag) {
						writeAttributeStart(valueTagAttr, false);
					}

					// output the text
					String data = ((Text) value).getNodeValue();
					writer.writeCharacters(data == null ? "" : data);

					if (!writeValueTag) {
						boolean endsWithLF = text.getNodeValue().endsWith("\n");

						if (!endsWithLF && children.getLength() == 1) {
							indent = false;
						} else if (!endsWithLF && children.getLength() > 1) {
							newLine();
							indent = true;
						} else if (endsWithLF) {
							indent = true;
						}
					} else {
						writer.writeEndElement();
						newLine();
						indent = true;
					}
				}
			} else if (value.getNodeType() == Node.CDATA_SECTION_NODE) {
				// write CDATA Section
				newLine();
				indentElement(tabCount);

				if (writeValueTag) {
					writeAttributeStart(valueTagAttr, false);
				}

				String data = ((CDATASection) value).getNodeValue();
				writer.writeCData(data == null ? "" : data);

				if (writeValueTag) {
					writer.writeEndElement();
				}

				newLine();
				indent = true;
			}
		}

		return indent;
	}

	/**
	 * Checks whether the parameter is <code>null</code> or just whitespace and
	 * returns <code>false</code> , otherwise <code>true</code>.
	 * 
	 * @param temp
	 *            String
	 * @return true if parameter contains meaningful content.
	 */
	private boolean checkParamExist(String temp) {

		if (temp != null && temp.trim().length() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * This method closes the parser's streams
	 * 
	 * @exception Exception
	 *                in case an error occurs
	 */
	public void closeParser() throws Exception {

		if (reader != null) {
			reader.close();
			reader = null;
		}

		if (writer != null) {
			if (!standaloneRoot)
				writeEndTags();
			writer.close();
			writer = null;
		}

		super.closeParser();
	}

	/**
	 * Puts the specified number of tab spaces. Note: Indent output must be
	 * enabled.
	 * 
	 * @param count
	 *            number of tab spaces
	 * @throws XMLStreamException
	 *             if object to write to is not valid.
	 */
	private void indentElement(int count) throws XMLStreamException {
		for (int i = 0; indentOutput && i < count; i++)
			writer.writeCharacters("\t");
	}

	/**
	 * Puts a line separator.
	 * 
	 * @throws XMLStreamException
	 *             if object to write to is not valid.
	 */
	private void newLine() throws XMLStreamException {
		if (indentOutput)
			writer.writeCharacters("\n");
	}

	/**
	 * @return XML representation of the last retrieved entry (or null if no
	 *         entry was found) as a ByteArrayInputStream object encoded using
	 *         the specified in the characterSet parameter encoding. If the
	 *         specified encoding is UTF-16LE, UTF-16BE, UTF-32LE, UTF-32BE then
	 *         the first few bytes will be BOM bytes.
	 */
	public ByteArrayInputStream getCurrentEntryAsXMLStream() {
		if (currentEntryAsXML != null)
			return new ByteArrayInputStream(currentEntryAsXML.toByteArray());
		return null;
	}

	/**
	 * Converts current entry to XML.
	 * 
	 * @return XML representation of the last retrieved entry (or null if no
	 *         entry was found) as a decoded String object with removed BOM
	 * @throws UnsupportedEncodingException
	 *             - if the specified encoding is not supported
	 */
	public String getCurrentEntryAsXMLString() throws UnsupportedEncodingException {

		if (currentEntryAsXMLString != null) {
			return currentEntryAsXMLString;
		}

		if (currentEntryAsXML != null) {
			String str = currentEntryAsXML.toString(charEncoding);
			if (str.charAt(0) == (char) 0xfeff) {
				return (currentEntryAsXMLString = str.substring(1));
			}
			return (currentEntryAsXMLString = str);
		}
		return null;
	}

	/**
	 * Retrieves current xPath's nodes parser is working on.
	 * 
	 * @return current xPath's nodes parser is working on
	 * 
	 */
	public List<QName> getCurrentEntryPath() {

		return xPath.getCurrentXPath();
	}

	/**
	 * Prints a debug message.
	 * 
	 * @param msgKey
	 *            message key
	 * @param params
	 *            place holder for debug messages
	 */
	void printDebugMessage(String msgKey, Object[] params) {
		if (getDebug()) {
			if (params == null || params.length == 0)
				debug(resHash.getString(msgKey));
			else if (params.length == 1)
				debug(resHash.getString(msgKey, params[0]));
			else
				debug(resHash.getString(msgKey, params));
		}
	}

	/**
	 * Retrieves log object.
	 * 
	 * @return the Log used for logging.
	 */
	protected Log getLogger() {
		Object ctx = getContext();
		if (ctx instanceof Connector) {
			Connector parent = ((Connector) ctx);
			Log log = parent.getLog();
			if (log != null) {
				return log;
			}

			if (parent.getRSInterface() instanceof RS) {
				return ((RS) parent.getRSInterface()).getLog();
			}
		}

		return null;
	}

	/*
	 * Start of the QuerySchema implementation
	 */

	/**
	 * save the names of the elements which we have found
	 */
	private ArrayList<String> pathUntilNow = new ArrayList<String>();

	/**
	 * save the types of the elements which we have found
	 */
	private ArrayList<String> typesUntilNow = new ArrayList<String>();

	/**
	 * all schemas used
	 */
	private HashMap<String, XSDSchema> includes = new HashMap<String, XSDSchema>();

	/**
	 * the schema with its corresponding namespace
	 */
	private Map<String, String> schemaToNamespace = new HashMap<String, String>();

	/**
	 * the declared in the main schema namespaceURIs
	 */
	private Map<String, String> nameSpaceMap = null;

	/**
	 * the main schema object
	 */
	private XSDSchema xsd = null;

	/**
	 * the path to the main schema
	 */
	private String mainXSDPath = null;

	/**
	 * {@inheritDoc}
	 */
	public Object querySchema(Object schema) throws Exception {
		Trace.entrymax(this, "querySchema");
		Vector<Entry> results = new Vector<Entry>();
		String simpleXPathExpr = getParam("xpath.expr");

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xsd", new XSDResourceFactoryImpl());

		try {
			schemaToNamespace = getSchemaLocationFromXML();
		} catch (Exception ignore) {
			// ignore if the Parser fails to read the Schema from the XML file
			// they are not required.
		}

		if (mainXSDPath != null) {
			xsd = createSchema(mainXSDPath);
		}

		if (xsdPath != null) {
			parseSchemaString(xsdPath, schemaToNamespace);
		}

		if (xsd != null) {
			String[][][] searchForPaths = null;

			xPath = new SimpleXPathEvaluator(simpleXPathExpr, nsMap, attrsDeclaration, null, getLogger());

			xPath.compileForReading();

			searchForPaths = getConcretePaths();

			String prefix = null;
			String localName = null;
			String tags[][] = null;

			nameSpaceMap = xsd.getQNamePrefixToNamespaceMap();
			for (int path = 0; path < searchForPaths.length; path++) {
				pathUntilNow = new ArrayList<String>();
				XSDTypeDefinition xsdType = null;
				XSDElementDeclaration xsdElement = null;

				tags = searchForPaths[path];
				for (int tag = 0; tag < tags.length; tag++) {
					prefix = tags[tag][0];
					localName = tags[tag][1];
					prefix = findPrefix(xsd);
					if (prefix != "") {
						prefix += SimpleXPathEvaluator.PREFIX_LOCALNAME_SEPARATOR;
					}
					if (xsdElement == null) {
						xsdElement = findElement(xsd.getElementDeclarations(), localName);
					} else {
						Element element = findElement(xsdElement.getElement().getChildNodes(), localName);
						if (element == null) {
							element = findElement(xsdType.getElement().getChildNodes(), localName);
							if (element == null) {
								element = findElement(xsdType.getElement().getChildNodes(), removePrefix(xsdElement.getElement()
										.getAttribute("type")));
							}
							xsdElement = XSDElementDeclarationImpl.createElementDeclaration(findElement(xsdType.getElement()
									.getChildNodes(), localName));
						} else {
							xsdElement = XSDElementDeclarationImpl.createElementDeclaration(element);
						}
					}

					if (xsdElement != null) {
						XSDTypeDefinition type = findElement(xsd.getTypeDefinitions(), removePrefix(xsdElement.getElement()
								.getAttribute("type")));
						if (type != null) {
							xsdType = type;
						} else {
							if (xsdType != null) {
								xsdType = XSDTypeDefinitionImpl.createTypeDefinition(findElement(xsdType.getElement()
										.getChildNodes(), localName));
							} else {
								xsdType = XSDTypeDefinitionImpl.createTypeDefinition(xsdElement.getElement());
							}
						}
					} else if (xsdType != null) {
						pathUntilNow.add(localName);
						typesUntilNow.add(xsdType.getElement().getAttribute("name"));
					}
				}

				Entry inner = new Entry();
				if (xsdType == null && xsdElement != null) {
					String type = removePrefix(xsdElement.getElement().getAttribute("type"));
					if (type != "") {
						typesUntilNow.add(type);
						Element xsdTypeElement = checkInIncludes(type, prefix, true);
						if (xsdTypeElement != null) {
							pathUntilNow.add(localName);
							fillSchema(xsdTypeElement, inner, prefix, xsd.getTypeDefinitions());
							pathUntilNow.remove(localName);
						}
						typesUntilNow.remove(type);
					} else {
						fillNode(xsdElement.getElement(), inner, prefix, xsd.getTypeDefinitions());
					}
				} else if (xsdType != null && xsdType.getElement() != null) {
					pathUntilNow.add(localName);
					fillSchema(xsdType.getElement(), inner, prefix, xsd.getTypeDefinitions());
					pathUntilNow.remove(localName);
				} else {
					throw new Exception(resHash.getString("XML.PARSER.2.SCHEMA.ELEMENT.NOT.FOUND", localName));
				}

				Entry finals = new Entry();
				finals.setAttribute("Name", localName);
				if (inner.getAttributeNames().length == 0 && xsdElement != null) {
					finals.setAttribute("syntax", xsdElement.getElement().getAttribute("type"));
				} else if (xsdElement != null && xsdElement.getElement() != null) {
					String name = xsdElement.getElement().getAttribute("name");
					if (inner.getAttribute(name) != null) {
						finals.setAttribute("syntax", inner.getAttribute(name));
					} else {
						finals.setAttribute("syntax", inner);
					}
				} else {
					finals.setAttribute("syntax", inner);
				}
				results.add(finals);
			}
		}

		Trace.exitmax(this, "querySchema", results);
		return results;
	}

	private String[][][] getConcretePaths() {
		String[][][] paths = null;
		boolean[] validPaths = new boolean[xPath.pathsSize()];
		int validPathsCount = 0;

		for (int i = 0; i < xPath.pathsSize(); i++) {
			for (int j = 0; j < xPath.getPath(i).length; j++) {
				if ((xPath.getPath(i)[j][0] != null && xPath.getPath(i)[j][0].contains(SimpleXPathEvaluator.WILDCARD))
						|| (xPath.getPath(i)[j][1] != null && xPath.getPath(i)[j][1].contains(SimpleXPathEvaluator.WILDCARD))) {
					validPaths[i] = false;
					continue;
				}
			}

			validPaths[i] = true;
			validPathsCount++;
		}

		paths = new String[validPathsCount][][];

		for (int i = 0, j = 0; i < xPath.pathsSize(); i++) {
			if (validPaths[i]) {
				paths[j++] = xPath.getPath(i);
			}
		}

		return paths;
	}

	/**
	 * This method runs through the XML and tries to find any XSD URLs
	 * 
	 * @return HashMap - the mapping is from the xsdURL to xsdNamespace
	 * @throws Exception
	 *             - in case parsing exception occurs
	 */
	public HashMap<String, String> getSchemaLocationFromXML() throws Exception {
		HashMap<String, String> xsdMap = new HashMap<String, String>();

		for (int event = reader.next(); reader.hasNext(); event = reader.next()) {

			switch (event) {
			case XMLStreamConstants.START_ELEMENT: {
				String xsi = reader.getNamespaceContext().getPrefix("http://www.w3.org/2001/XMLSchema-instance");
				if (xsi == null) {
					continue;
				}

				for (int i = 0; i < reader.getAttributeCount(); i++) {
					if (xsi.equals(reader.getAttributePrefix(i))) {
						if ("noNamespaceSchemaLocation".equals(reader.getAttributeLocalName(i))) {

							if (mainXSDPath == null) {
								mainXSDPath = reader.getAttributeValue(i);
							} else {
								xsdMap.put(reader.getAttributeValue(i), null);
							}
						} else if ("schemaLocation".equals(reader.getAttributeLocalName(i))) {

							String val = reader.getAttributeValue(i).trim();
							int spacePos = val.indexOf(' ');
							String namespace = null;
							String path = null;

							if (spacePos < 0) {
								path = val;
							} else if (spacePos > 0) {
								path = val.substring(spacePos + 1).trim();
								namespace = val.substring(0, spacePos).trim();
							}

							if (mainXSDPath == null) {
								mainXSDPath = path;
							} else {
								xsdMap.put(path, namespace);
							}
						}
					}
				}
			}
			}
		}

		return xsdMap;
	}

	/**
	 * Will return a list with names of the elements which are causing a
	 * recursion.
	 * 
	 * @param type
	 *            - the type for which we want to check for recursion.
	 * @return - null if recursion will not happen and list with the element
	 *         names which are causing the recursion.
	 */
	private List<String> enteringEndlessRecursion(String type) {
		for (int index = 0; index < typesUntilNow.size(); index++) {
			if ((typesUntilNow.get(index).equals(type)) && (pathUntilNow.size() > index)) {
				return pathUntilNow.subList(0, index + 1);
			}
		}

		return null;
	}

	/**
	 * This method returns URI object of a given path.
	 * 
	 * @param path
	 *            - the URI path.
	 * @return - URI object representation of the given path.
	 * @throws Exception
	 *             if the method fails to create the URI.
	 */
	private URI createURI(String path) throws Exception {
		File file = new File(path);
		URI uri;
		if (file.isFile()) {
			uri = URI.createFileURI(file.getCanonicalFile().toString());
		} else {
			uri = URI.createURI(path);
		}

		return uri;
	}

	/**
	 * Creates a schema object from the given location.
	 * 
	 * @param schemaLocation
	 *            - the path to the schema we want to create.
	 * @return Schema object corresponding to the provided schema path.
	 * @throws Exception
	 *             if the schema creation fails.
	 */
	private XSDSchema createSchema(String schemaLocation) throws Exception {
		ResourceSet resourceSet = new ResourceSetImpl();
		XSDResourceImpl xsdResource = (XSDResourceImpl) resourceSet.createResource(URI.createURI("*.xsd"));

		URI uri = createURI(schemaLocation);
		xsdResource.setURI(uri);

		try {
			xsdResource.load(resourceSet.getLoadOptions());
		} catch (IOException exc) {
			printDebugMessage("XMLPARSER2.XSDSCHEMA.NOT.FOUND", new Object[] { schemaLocation });
			return null;
		}

		XSDSchema xsdSchema = xsdResource.getSchema();
		xsdSchema.updateDocument();
		xsdSchema.setElement(null);
		xsdSchema.updateElement();

		if (xsdSchema.getElement() != null) {
			checkForIncludes(xsdSchema.getElement(), schemaLocation);
		}

		return xsdSchema;
	}

	/**
	 * This method will map all schemas from the given string to the provided
	 * Map object.
	 * 
	 * @param schemas
	 *            - the string containing the schema locations.
	 * @param schemaToNamespace
	 *            - the map to which the schemas will be inserted.
	 * @throws Exception
	 *             - if an Exception occurs during the mapping process.
	 */
	private void parseSchemaString(String schemas, Map<String, String> schemaToNamespace) throws Exception {
		if (schemas == null) {
			return;
		}

		StringTokenizer tokens = new StringTokenizer(schemas, "|");
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken().trim();
			int hasNamespace = token.indexOf(" ");
			String schemaNamespace = null;
			String schemaPath = token;
			if (hasNamespace > -1) {
				schemaNamespace = token.substring(0, hasNamespace + 1);
				schemaPath = token.substring(hasNamespace + 1);
			}

			if (xsd == null) {
				xsd = createSchema(schemaPath);
				return;
			}

			schemaToNamespace.put(schemaPath, schemaNamespace);
			includes.put(schemaPath, null);
		}
	}

	/**
	 * Removes the prefix from the type.
	 * 
	 * @param type
	 *            - the type from which we want to remove the prefix.
	 * @return The type without prefix. If the type does not contain prefix then
	 *         the type itself is returned.
	 */
	private String removePrefix(String type) {
		int prefixindex = type.indexOf(':');
		if (prefixindex > -1 && prefixindex + 1 < type.length()) {
			return type.substring(prefixindex + 1);
		} else {
			return type;
		}
	}

	/**
	 * Find the namespace prefix corresponding to the schema declaration.
	 * 
	 * @param xsd
	 *            - the schema from which we will extract the namespace prefix.
	 * @return The namespace prefix of the schema declaration.
	 */
	private String findPrefix(XSDSchema xsd) {
		Map<String, String> prefixToNamespace = xsd.getQNamePrefixToNamespaceMap();
		Iterator<Map.Entry<String, String>> entries = prefixToNamespace.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<String, String> prefix = entries.next();
			if ((prefix.getValue()).equals("http://www.w3.org/2001/XMLSchema")) {
				String key = prefix.getKey();
				if (key != null) {
					return key;
				}
			}
		}
		return "";
	}

	/**
	 * Checks for type inside includes/imports.
	 * 
	 * @param type
	 *            - the type which will be search in the included schemas.
	 * @param prefix
	 *            - the prefix of the type.
	 * @param element
	 *            - if true we will search for element type declaration,
	 *            otherwise we are searching for groups
	 * @return The type as element or null if the type is not found.
	 * @throws Exception
	 *             If an exception occurs during the search.
	 */
	private Element checkInIncludes(String type, String prefix, boolean element) throws Exception {
		if (!isSimpleType(type, prefix)) {
			Iterator<Map.Entry<String, XSDSchema>> elements = includes.entrySet().iterator();
			HashMap<String, XSDSchema> createdSchemas = new HashMap<String, XSDSchema>();
			while (elements.hasNext()) {
				Map.Entry<String, XSDSchema> include = elements.next();
				XSDSchema incSchema = include.getValue();
				if (incSchema == null) {
					incSchema = createSchema(include.getKey());

					if (incSchema == null) {
						// schema not found, continue with other schema
						continue;
					}

					createdSchemas.put(include.getKey(), incSchema);
				}

				if (element) {
					XSDTypeDefinition xsdType = incSchema.resolveTypeDefinition(type);
					if (xsdType == null || xsdType.getElement() == null) {
						XSDElementDeclaration xsdElement = findElement(incSchema.getElementDeclarations(), removePrefix(type));
						if (xsdElement != null && xsdElement.getElement() != null) {
							includes.putAll(createdSchemas);
							return xsdElement.getElement();
						}
					}
					if (xsdType != null && xsdType.getElement() != null) {
						includes.putAll(createdSchemas);
						return xsdType.getElement();
					}
				} else {
					Element group = getGroups(incSchema, type);
					if (group != null) {
						includes.putAll(createdSchemas);
						return group;
					}
				}
			}
			includes.putAll(createdSchemas);
		}
		return null;
	}

	/**
	 * Checks if the provided type is one of the most commonly used simple
	 * types.
	 * 
	 * @param type
	 *            - the type which will be checked if it is simple one.
	 * @param prefix
	 *            - the namespace prefix of the schema declaration
	 * @return True if the type is simple type and false otherwise.
	 */
	private boolean isSimpleType(String type, String prefix) {
		if ((prefix + "string").equals(type) || (prefix + "decimal").equals(type) || (prefix + "integer").equals(type)
				|| (prefix + "boolean").equals(type) || (prefix + "time").equals(type) || (prefix + "date").equals(type)
				|| (prefix + "duration").equals(type) || (prefix + "dateTime").equals(type) || (prefix + "token").equals(type)
				|| (prefix + "normalizedString").equals(type) || (prefix + "double").equals(type)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks a given schema for includes and puts them to the includes map.
	 * 
	 * @param element
	 *            - The element of the schema object.
	 * @param parent
	 *            - the path to the parent schema.
	 * @throws Exception
	 *             - if an Exception occurs during the checking process.
	 */
	private void checkForIncludes(Element element, String parent) throws Exception {
		NodeList childNodes = element.getChildNodes();
		for (int child = 0; child < childNodes.getLength(); child++) {
			Node childNode = childNodes.item(child);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) childNode;
				if (removePrefix(childElement.getNodeName()).equals("include")
						|| removePrefix(childElement.getNodeName()).equals("import")) {
					String path = childElement.getAttribute("schemaLocation");
					if (!path.equals("")) {
						int index = path.indexOf(" ");
						String namespace = null;
						if (index > -1) {
							namespace = path.substring(0, index);
							path = path.substring(index + 1);
						}
						schemaToNamespace.put(path, namespace);
						URI uriParent = createURI(parent);
						URI uriPath = createURI(path);
						if (uriPath.isRelative()) {
							path = "";
							if (uriParent.device() != null) {
								path = uriParent.device();
							}
							path += uriParent.path().substring(0, uriParent.path().lastIndexOf(uriParent.lastSegment()))
									+ uriPath.path();
						}
						includes.put(path, null);
					}
				}
			}
		}
	}

	/**
	 * Fills the schema of a give type inside an Entry.
	 * 
	 * @param xsdType
	 *            - the type which schema will be filled in the entry.
	 * @param result
	 *            - the result entry containing the schema of the given type.
	 * @param prefix
	 *            - the prefix for the schema elements.
	 * @param types
	 *            - the type declaration of the schema.
	 * @throws Exception
	 *             If an Exception occurs during the schema fill.
	 */
	private void fillSchema(Element xsdType, Entry result, String prefix, EList<XSDTypeDefinition> types) throws Exception {
		NodeList list = xsdType.getChildNodes();
		for (int child = 0; child < list.getLength(); child++) {
			Node childNode = list.item(child);
			if ((childNode != null) && (childNode.getNodeType() == Node.ELEMENT_NODE)) {
				fillNode((Element) childNode, result, prefix, types);
			}

		}
	}

	/**
	 * Fills the schema of a given Element inside an Entry.
	 * 
	 * @param childNode
	 *            - the Element which schema will be found and filled in the
	 *            Entry.
	 * @param result
	 *            - the Entry which will contain the Element schema.
	 * @param prefix
	 *            - the prefix for the schema elements.
	 * @param types
	 *            - list of element types definition which the schema contains.
	 * @throws Exception
	 *             If an Exception occurs during the fill process.
	 */
	private void fillNode(Element childNode, Entry result, String prefix, EList<XSDTypeDefinition> types) throws Exception {

		putAttributes(childNode, result);
		String nodeName = removePrefix(childNode.getNodeName());

		if (nodeName.equals("all")) {
			fillSchema(childNode, result, prefix, types);
			result.setProperty("#indicator", "all");
		} else if (nodeName.equals("choice")) {
			fillSchema(childNode, result, prefix, types);
			result.setProperty("#indicator", "choice");
		} else if (nodeName.equals("sequence")) {
			fillSequence(childNode, result, prefix, types);
			// fillSchema(childNode, result, prefix, types);
			result.setProperty("#indicator", "sequence");
		} else if (nodeName.equals("element")) {
			String type = childNode.getAttribute("type");
			String ref = childNode.getAttribute("ref");
			if (ref != "") {
				XSDElementDeclaration xsdElement = xsd.resolveElementDeclaration(removePrefix(ref));
				Element xsdTypeElement = null;
				if (xsdElement != null && xsdElement.getElement() != null) {
					xsdTypeElement = xsdElement.getElement();
				} else {
					xsdTypeElement = checkInIncludes(removePrefix(ref), prefix, true);
				}

				if (xsdTypeElement != null) {
					fillNode(xsdTypeElement, result, prefix, types);
				} else {
					throw new Exception(resHash.getString("XML.PARSER.2.SCHEMA.ELEMENT.NOT.FOUND", ref));
				}
			} else if (type == "") {
				NodeList elementChildren = childNode.getChildNodes();
				for (int elementChild = 0; elementChild < elementChildren.getLength(); elementChild++) {
					if (elementChildren.item(elementChild).getNodeType() == Node.ELEMENT_NODE) {
						Node elementNode = elementChildren.item(elementChild);
						if (removePrefix(elementNode.getNodeName()).equals("simpleType")) {
							result.setAttribute(childNode.getAttribute("name"), putSimpleType((Element) elementNode, types));
						} else if (removePrefix(elementNode.getNodeName()).equals("complexType")) {
							Entry inner = new Entry();
							pathUntilNow.add(childNode.getAttribute("name"));
							fillSchema((Element) elementNode, inner, prefix, types);
							pathUntilNow.remove(childNode.getAttribute("name"));
							result.setAttribute(childNode.getAttribute("name"), inner);
						}
					}
				}
			} else {
				XSDTypeDefinition typeDef = getTypeByPrefix(type, getPrefix(type));
				type = removePrefix(type);
				if (typeDef == null) {
					typeDef = findElement(types, type);
				}

				if (typeDef != null) {
					Entry inner = new Entry();
					pathUntilNow.add(childNode.getAttribute("name"));
					List<String> er = enteringEndlessRecursion(type);
					if (er != null) {
						// we have an endless recursion
						StringBuilder names = new StringBuilder("#");
						for (int i = 0; i < er.size() - 1; i++) {
							names.append(er.get(i));
							names.append((char) ',');
							names.append((char) ' ');
						}
						names.append(er.get(er.size() - 1));
						result.setAttribute(childNode.getAttribute("name"), names.toString());
					} else {
						typesUntilNow.add(type);
						fillSchema(typeDef.getElement(), inner, prefix, types);
						result.setAttribute(childNode.getAttribute("name"), inner);
						typesUntilNow.remove(type);
					}
					pathUntilNow.remove(childNode.getAttribute("name"));

				} else {
					Element xsdTypeElement = checkInIncludes(type, prefix, true);
					if (xsdTypeElement != null) {
						Entry inner = new Entry();
						pathUntilNow.add(childNode.getAttribute("name"));
						typesUntilNow.add(type);
						fillSchema(xsdTypeElement, inner, prefix, types);
						pathUntilNow.remove(childNode.getAttribute("name"));
						typesUntilNow.remove(type);
						result.setAttribute(childNode.getAttribute("name"), inner);
					} else {
						result.setAttribute(childNode.getAttribute("name"), type);
					}
				}
			}
			result.setProperty("#type", "element");
		} else if (nodeName.equals("attribute")) {
			putAttributeDeclaration(childNode, result, types);
		} else if (nodeName.equals("restriction")) {
			NodeList restrictions = childNode.getChildNodes();
			for (int restriction = 0; restriction < restrictions.getLength(); restriction++) {
				if (restrictions.item(restriction).getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}

				if (removePrefix(restrictions.item(restriction).getNodeName()).equals("simpleType")) {
					result.addAttributeValue(childNode.getAttribute("base"), putSimpleType(
							(Element) restrictions.item(restriction), types));
				} else {
					Entry inner = new Entry();
					inner.addAttributeValue(restrictions.item(restriction).getNodeName(),
							((Element) restrictions.item(restriction)).getAttribute("value"));
					result.addAttributeValue(childNode.getAttribute("base"), inner);
				}
			}
		} else if (nodeName.equals("simpleContent") || nodeName.equals("complexContent")) {
			fillSchema(childNode, result, prefix, types);
		} else if (nodeName.equals("extension")) {
			fillExtentions(childNode, result, prefix, types);
		} else if (nodeName.equals("group") || nodeName.equals("attributeGroup")) {
			String ref = removePrefix(childNode.getAttribute("ref"));
			if (ref != "") {
				Element group = getGroups(xsd, ref);
				if (group == null) {
					group = checkInIncludes(ref, "", false);
					if (group == null) {
						return;
					}
				}
				fillSchema(group, result, prefix, types);
			} else {
				ref = removePrefix(childNode.getAttribute("name"));
				Entry inner = new Entry();
				fillSchema(childNode, inner, prefix, types);
				result.setAttribute(ref, inner);
			}
		}
		// skip annotation because it does not contains "valuable" schema
		// information
	}

	/**
	 * This method will be called if a sequence is found. The sequence requires
	 * the elements to be in the order specified in the schema. That is why we
	 * use a little different mechanism when we handle the sequence fields. They
	 * are put inside a Vector object (which is ordered) and put in attribute
	 * with name "#sequence".
	 * 
	 * @param sequence
	 *            - the Element which contains the sequence.
	 * @param result
	 *            - the results where the sequence will be put as Vector object.
	 * @param prefix
	 *            - the schema prefix.
	 * @param types
	 *            - the types definition for the schema elements.
	 * @throws Exception
	 *             If an Exception occurs during the sequence filling.
	 */
	private void fillSequence(Element sequence, Entry result, String prefix, EList<XSDTypeDefinition> types) throws Exception {
		NodeList nl = sequence.getChildNodes();
		Vector<Object> v = new Vector<Object>();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element seq = (Element) nl.item(i);
				Entry inn = new Entry();
				fillNode(seq, inn, prefix, types);
				String names[] = inn.getAttributeNames();
				for (int n = 0; n < names.length; n++) {
					String name = names[n];
					v.add(name + ":" + inn.getAttribute(name).getValue(0));
				}
			}
		}
		result.setAttribute("#sequence", v);
	}

	/**
	 * Handles group tags.
	 * 
	 * @param schema
	 *            - the schema which will be checked for groups.
	 * @param ref
	 *            - the reference to the group.
	 * @return Element containing the group or null if the group is not found.
	 */
	private Element getGroups(XSDSchema schema, String ref) {
		XSDComponent attribute = schema.resolveAttributeGroupDefinition(ref);
		if (attribute == null || attribute.getElement() == null) {
			attribute = schema.resolveModelGroupDefinition(ref);
			if (attribute != null && attribute.getElement() != null) {
				return attribute.getElement();
			}
		} else {
			return attribute.getElement();
		}

		return null;
	}

	/**
	 * This method puts an attribute schema inside an Entry object.
	 * 
	 * @param childNode
	 *            - the Element containing the attribute information.
	 * @param result
	 *            - the Entry in which the schema will be written.
	 * @param types
	 *            - the types definition for the schema elements.
	 * @throws Exception
	 *             If an Exception occurs during the attribute fill.
	 */
	private void putAttributeDeclaration(Element childNode, Entry result, EList<XSDTypeDefinition> types) throws Exception {

		String name = childNode.getAttribute("name");
		String type = childNode.getAttribute("type");
		String ref = childNode.getAttribute("ref");
		boolean canContainSimple = true;
		String fixed = childNode.getAttribute("fixed");
		String def = childNode.getAttribute("default");
		if (type != "") {
			canContainSimple = false;
			XSDTypeDefinition xsdType = getTypeByPrefix(type, getPrefix(type));
			if (xsdType == null || xsdType.getElement() == null) {
				result.setAttribute(name, type);
			} else {
				result.setAttribute(name, putSimpleType(xsdType.getElement(), types));
			}
		} else if (ref != "") {
			canContainSimple = false;
			XSDComponent comp = xsd.resolveAttributeDeclaration(ref);
			if (comp == null || comp.getElement() == null) {
				Element att = checkInIncludes(ref, "", false);
				if (att != null) {
					putAttributeDeclaration(att, result, types);
				}
			} else {
				putAttributeDeclaration(comp.getElement(), result, types);
			}
		} else if (fixed != "") {
			result.setAttribute(name, fixed);
			result.setProperty("#value", "fixed");
		} else if (def != "") {
			result.setAttribute(name, def);
			result.setProperty("#value", "default");
		} else if (name != "") {
			result.setAttribute(name, "");
		}

		if (canContainSimple) {
			NodeList simple = childNode.getChildNodes();
			for (int child = 0; child < simple.getLength(); child++) {
				if (removePrefix(simple.item(child).getNodeName()).equals("simpleType")) {
					result.setAttribute(name, putSimpleType((Element) simple.item(child), types));
				}
			}
		}
		result.setProperty("#type", "attribute");
		putAttributes(childNode, result);
	}

	/**
	 * This method gets all attributes from an Element an puts them as
	 * "#attributes" property. This property is a Map containing all Element
	 * attributes.
	 * 
	 * @param element
	 *            - the Element which attributes will be put as Entry
	 *            "#attributes" property.
	 * @param result
	 *            - the Entry containing all attributes as properties.
	 * @throws Exception
	 *             If an Exception occurs during the get/set process.
	 */
	@SuppressWarnings("unchecked")
	private void putAttributes(Element element, Entry result) throws Exception {
		NamedNodeMap attributes = element.getAttributes();
		if (attributes != null) {
			Map<String, String> table = (Map<String, String>) result.getProperty("#attributes");
			if (table == null) {
				table = new HashMap<String, String>();
			}
			for (int attribute = 0; attribute < attributes.getLength(); attribute++) {
				table.put(attributes.item(attribute).getLocalName(), attributes.item(attribute).getNodeValue());
			}
		}
	}

	/**
	 * Returns prefix for given type.
	 * 
	 * @param type
	 *            - the type which prefix will be returned.
	 * @return The prefix or empty string if the type contains no prefix.
	 */
	private String getPrefix(String type) {
		int index = type.indexOf(":");
		if (index > -1) {
			return type.substring(0, index);
		}

		return "";
	}

	/**
	 * Returns specified type considering its prefix. If in the schema there are
	 * two types with equal names but with a different namespace prefix the type
	 * with the specified prefix will be returned.
	 * 
	 * @param type
	 *            - the type which we want to find.
	 * @param prefix
	 *            - the prefix of the type.
	 * @return XSDTypeDefinition of the type with the specified prefix.
	 * @throws Exception
	 *             If an Exception occurs during the type search.
	 */
	private XSDTypeDefinition getTypeByPrefix(String type, String prefix) throws Exception {
		XSDTypeDefinition xsdType = null;
		if (nameSpaceMap.get(prefix) != null) {
			String namespace = nameSpaceMap.get(prefix);
			Iterator<Map.Entry<String, String>> schemas = schemaToNamespace.entrySet().iterator();
			while (schemas.hasNext()) {
				Map.Entry<String, String> schema = schemas.next();
				String value = schema.getValue();
				if (value != null && value.equals(namespace)) {
					XSDSchema xsd = includes.get(schema.getKey());
					if (xsd == null) {
						xsd = createSchema(schema.getKey());
					}

					xsdType = findElement(xsd.getTypeDefinitions(), removePrefix(type));
					if (xsdType != null) {
						break;
					}
				}
			}
		}

		return xsdType;
	}

	/**
	 * This method is called when an extension element is found.
	 * 
	 * @param element
	 *            - the Element which contains the sequence.
	 * @param result
	 *            - the result where the extension will be put as Vector object.
	 * @param prefix1
	 *            - the schema prefix.
	 * @param types
	 *            - the types definition for the schema elements.
	 * @throws Exception
	 *             If an Exception occurs during the extension filling.
	 */
	private void fillExtentions(Element element, Entry result, String prefix1, EList<XSDTypeDefinition> types) throws Exception {
		String base = element.getAttribute("base");
		String prefix = getPrefix(base);
		XSDTypeDefinition type = getTypeByPrefix(base, prefix);
		Element xsdTypeElement = null;
		if (type == null) {
			type = findElement(types, removePrefix(base));
		} else {
			xsdTypeElement = type.getElement();
		}

		if (type == null) {
			xsdTypeElement = checkInIncludes(removePrefix(base), prefix1, true);
		} else {
			xsdTypeElement = type.getElement();
		}

		Entry inner = new Entry();
		if (xsdTypeElement != null) {
			fillSchema(xsdTypeElement, inner, prefix1, types);
		}

		fillSchema(element, inner, prefix1, types);
		result.setAttribute(element.getAttribute("base"), inner);
	}

	/**
	 * Puts a simple type element inside an entry.
	 * 
	 * @param element
	 *            - the element containing the simple type.
	 * @param types
	 *            - the types definition for the schema elements.
	 * @return The simple type inside an entry or an empty entry if no simple
	 *         type is found.
	 * @throws Exception
	 *             If an Exception occurs during the simple type search.
	 */
	private Entry putSimpleType(Element element, EList<XSDTypeDefinition> types) throws Exception {
		NodeList insideST = element.getChildNodes();
		Entry result = new Entry();
		for (int child = 0; child < insideST.getLength(); child++) {
			Node restriction = insideST.item(child);
			Entry inner = new Entry();

			if (restriction.getNodeType() == Node.ELEMENT_NODE) {
				if (removePrefix(restriction.getNodeName()).equals("restriction")) {
					fillRestrictions((Element) restriction, types, result);
				} else if (removePrefix(restriction.getNodeName()).equals("list")) {
					Element list = (Element) restriction;
					if (list.getAttribute("itemType") != "") {
						result.setAttribute(element.getAttribute("name"), list.getAttribute("itemType"));
					} else {
						NodeList nl = restriction.getChildNodes();
						for (int i = 0; i < nl.getLength(); i++) {
							if (removePrefix(nl.item(i).getNodeName()).equals("simpleType")) {
								inner = putSimpleType((Element) nl.item(i), types);
							}
							result.addAttributeValue(((Element) restriction).getAttribute("base"), inner);
						}
					}
				} else if (removePrefix(restriction.getNodeName()).equals("union")) {
					Element union = (Element) restriction;
					if (union.getAttribute("memberTypes") != "") {
						StringTokenizer tokens = new StringTokenizer(union.getAttribute("memberTypes"), " ");
						while (tokens.hasMoreTokens()) {
							String cur = tokens.nextToken();
							XSDTypeDefinition xsdType = findElement(types, removePrefix(cur));
							Element xsdTypeElement = null;
							if (xsdType == null || xsdType.getElement() == null) {
								xsdTypeElement = checkInIncludes(cur, "", true);
							} else {
								xsdTypeElement = xsdType.getElement();
							}

							if (xsdTypeElement == null) {
								result.addAttributeValue(((Element) union.getParentNode()).getAttribute("name"), cur);
							} else {
								fillSchema(xsdTypeElement, inner, "", types);
								result.addAttributeValue(((Element) union.getParentNode()).getAttribute("name"), inner);
							}
						}
					}
				} else if (removePrefix(restriction.getNodeName()).equals("attribute")) {
					putAttributeDeclaration((Element) restriction, result, types);
				} else if (removePrefix(restriction.getNodeName()).equals("extension")) {
					fillExtentions((Element) restriction, result, null, types);
				}
			}
		}
		return result;
	}

	/**
	 * Fills the information for restriction of an element inside an entry
	 * object.
	 * 
	 * @param element
	 *            - the element containing restrictions.
	 * @param types
	 *            - the types definition for the schema elements.
	 * @param result
	 *            - the entry where the restriction information will be written.
	 * @throws Exception
	 *             If an Exception occurs during the restriction filling.
	 */
	private void fillRestrictions(Element element, EList<XSDTypeDefinition> types, Entry result) throws Exception {

		Entry inner = new Entry();

		NodeList nl = element.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if (removePrefix(item.getNodeName()).equals("enumeration")) {
					fillEnumerations((Element) item, types, inner);
				} else {
					inner.addAttributeValue(nl.item(i).getNodeName(), ((Element) nl.item(i)).getAttribute("value"));
				}
			}
		}
		result.addAttributeValue(element.getAttribute("base"), inner);
	}

	/**
	 * Fills the enumeration information of an element inside an entry object.
	 * 
	 * @param enumeration
	 *            - the element containing enumeration
	 * @param types
	 *            - the types definition for the schema elements.
	 * @param result
	 *            - the result entry which will contain the enumeration
	 *            information.
	 * @throws Exception
	 *             If an Exception occurs during the filling process.
	 */
	private void fillEnumerations(Element enumeration, EList<XSDTypeDefinition> types, Entry result) throws Exception {
		String type = enumeration.getAttribute("value");
		XSDTypeDefinition xsdType = findElement(types, removePrefix(type));
		Element elType = null;
		if (xsdType == null || xsdType.getElement() == null) {
			elType = checkInIncludes(type, "", true);
		} else {
			elType = xsdType.getElement();
		}

		Entry inner = new Entry();
		if (elType == null) {
			result.addAttributeValue(enumeration.getNodeName(), type);
		} else {
			fillSchema(elType, inner, "", types);
			result.addAttributeValue(enumeration.getNodeName(), inner);
		}
	}

	/**
	 * Finds element inside a list. The list type should be EList.
	 * 
	 * @param <T>
	 *            anything that extends EObject
	 * 
	 * @param innerList
	 *            - the list which will be search for element.
	 * @param name
	 *            - the name of the element which we want to find.
	 * @return The element which is found or null otherwise. If the input list
	 *         is EList then the result element is of type EObject. If the input
	 *         list is NodeList then the result element is of type Element.
	 */
	private <T extends EObject> T findElement(EList<T> innerList, String name) {

		for (int i = 0; i < innerList.size(); i++) {
			T object = innerList.get(i);
			if (object instanceof XSDNamedComponent) {
				XSDNamedComponent named = (XSDNamedComponent) object;
				if (named.getName().equals(name)) {
					return object;
				}
			}
		}

		return null;
	}

	/**
	 * Finds element inside a list. Overloaded version that accepts NodeList and
	 * returns {@link Element}.
	 * 
	 * @param innerList
	 *            - the list which will be search for element.
	 * @param name
	 *            - the name of the element which we want to find.
	 * @return The element which is found or null otherwise. If the input list
	 *         is EList then the result element is of type EObject. If the input
	 *         list is NodeList then the result element is of type Element.
	 */
	private Element findElement(NodeList innerList, String name) {

		Element result = null;
		for (int i = 0; result == null && i < innerList.getLength(); i++) {
			if ((innerList.item(i).getNodeType() == Node.ELEMENT_NODE)
					&& (removePrefix(innerList.item(i).getNodeName()).equals("element"))) {
				Element element = (Element) innerList.item(i);
				if (element.getAttribute("name").equals(name)) {
					result = element;
				} else if (removePrefix(element.getAttribute("ref")).equals(name)) {
					result = xsd.resolveElementDeclaration(name).getElement();
				} else {
					result = findElement(element.getChildNodes(), name);
				}
			}
		}

		return result;
	}
	/*
	 * End of the QuerySchema implementation
	 */
}
