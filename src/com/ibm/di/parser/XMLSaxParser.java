/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.ibm.di.entry.Entry;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;
import com.ibm.icu.util.StringTokenizer;

/**
 * A class reading Entries in XML format. XML Sax Parser doesn't support
 * writing.
 *
 * This parser uses a SAX2 parser to read an XML document. For every start-tag
 * read from the input document the parser concatenates this tag name with its
 * parent tag name into a currentTag value. The currentTag value holds the path
 * to the current position in the XML document.
 *
 * When character data is read an attribute is either created or appended to
 * using the currentTag name and the character data as the value. Data
 * collection is only done when a group tag is found (e.g. when any of the group
 * tag values are found).
 *
 * When an end-tag is encountered the parser will first check to see if the
 * currentTag matches the GroupTag configured for the parser, and if it does,
 * the current entry is added to a queue which is read by the readEntry()
 * method. Since SAX2 is event driven, this class creates a thread that performs
 * the XML parsing and notifies the parser class when an entry is ready for
 * consumption.
 *
 * The currentTag is composed of tag names concatenate with the "@" character.
 * The GroupTag specifies which tag-path marks the boundary for an entry. If
 * specified as a string not starting with an asterix, the tag is checked for
 * equality with the currentTag. If the GroupTag starts with "*" then the
 * currentTag is checked for containment of the GroupTag (e.g. "Root@Entry@X"
 * matches "*Root@Entry" but not "Root@Entry").
 *
 * You can specify multiple group tags by comma-separating the tags (e.g. a,b@x
 * etc)
 *
 * <Root> <Entry> <attribute>Big Data</attribute> <attribute>Blue Data</attribute>
 * </Entry> <Entry> <attribute name="big"> <size>12</size> <age>88</age>
 * </attribute> <attribute name="blue">Blue Data</attribute> </Entry> </Root>
 *
 * Using "Root@Entry" as the GroupTag, the above XML document would yield two
 * entries with the following attributes:
 *
 * ENTRY [ "Root@Entry@attribute": [ "Big Data", "Blue Data" ] ]
 *
 * ENTRY [ "Root@Entry@attribute#name": [ "big" "blue" ]
 * "Root@Entry@attribute@size": [ "12" ] "Root@Entry@attribute@age": [ "88" ]
 * "Root@Entry@attribute": [ "Blue Data" ] ]
 *
 * The saxRemovePrefix parameter is a convenience parameter that cause the
 * parser to remove a specific prefix. If the parser was configured with
 * "Root@Entry@" for the saxRemovePrefix the entries would be simpler like
 * "attribute@size" rather than "Root@Entry@attribute@size".
 *
 * The Parser creates Sax Content handler thread. It parses the XML document and
 * in its endElement method puts the Entry object in XMLSaxParser's queue and
 * notifies XMLSaxParser to read the entry. After reading XMLSaxParser deletes
 * the Entry object from queue. Sax Content Handler thread waits in startElement
 * method for Entry to be read. XMLSaxParser, upon request for readEntry,
 * trigger SaxContentHandler thread to go ahead.
 *
 */
public class XMLSaxParser extends ParserImpl {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "xmlsaxparser";

	/**
	 * Default parser name
	 */
	private static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

	/**
	 * End of File flag
	 */
	private boolean bEOFFlag = false;

	/**
	 * Read flag to coordinate addEntry and readEntry methods
	 */
	//private boolean bReadFlag = false;

	/**
	 * The thread that performs the XML parsing
	 */
	private SAXContentHandler saxContentHandler;

	/**
	 * The queue the SAX delivers messages to (entries, exceptions and EOF)
	 */
	private ArrayList<Object> queue;

	/**
	 * Timeout value in seconds
	 */
	private long readEntryWaitTimeout = 0;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * Constructor
	 */
	public XMLSaxParser() {
		super();
		Trace.entrymid(this, "XMLSaxParser");
		System.setProperty("org.xml.sax.driver ", DEFAULT_PARSER_NAME);
		Trace.exitmid(this, "XMLSaxParser");
	}

	/**
	 * Not used
	 *
	 * @param se
	 * @throws Exception
	 */
	public void registerScriptBeans(ScriptEngine se) throws Exception {
	}

	/**
	 * Initializes queue object, reads configuration parameters, creates
	 * SaxContentHandler thread and starts the thread.
	 *
	 * @throws Exception
	 *             if getInputStream() and getReader() both returns null.
	 */
	public synchronized void initParser() throws Exception {
		Trace.entrymin(this, "initParser");
		resetProperties();
		bEOFFlag = false;

		// for now only reading is accepted
		if (getInputStream() == null && getReader() == null) {
			throw new Exception(sResHash
					.getString("PARSER.SAXXML.NO.INPUT.STREAM.PROVIDED.TO.SAX"));
		}

		// Queue where thread posts messages
		queue = new ArrayList<Object>();

		// Thread that parses XML document
		InputSource inp;

		String charSet = getParam("characterSet");

		if (charSet == null || charSet.equalsIgnoreCase("UTF-8"))
			charSet = "";

		// To work around a problem, we cannot use inputStream with UTF-16 and possibly other multibyte characterSets

		if (getInputStream() != null &&  !charSet.startsWith("UTF-16") && !charSet.startsWith("UTF-32"))
		{
			inp = new InputSource(getInputStream());

			// Set the encoding. This will only have effect if the document does not provide an encoding.

			if (charSet.length() > 0)
				inp.setEncoding(charSet);
		} else
		{
			inp = new InputSource(getReader());
		}

		saxContentHandler = new SAXContentHandler(this, inp);
		saxContentHandler.setGroupTag(getParam("saxGroupTag"));
		saxContentHandler.setRemovePrefix(getParam("saxRemovePrefix"));
		saxContentHandler.setIgnoreAttributes(getParam("saxIgnoreAttribute"));
		// saxContentHandler.setCharactetSet(getParam("characterSet"));
		saxContentHandler.setValidation(getParam("isvalidating"));
		saxContentHandler.setSchemaValidation(getParam("schemaValidating"));
		saxContentHandler.setNamespaceAware(getParam("isnamespaceaware"));
		// saxContentHandler.setDebug(debugMode());

		String strReadTimeout = getParam("saxReadEntryTimeout");
		if (strReadTimeout != null && strReadTimeout.trim().length() > 0) {
			try {
				readEntryWaitTimeout = Long.parseLong(strReadTimeout) * 1000;
				if (readEntryWaitTimeout < 0)
					throw new Exception(
							sResHash
									.getString("PARSER.SAXXML.READ.TIMEOUT.VALUE.HAS.TO.BE.A.POSITIVE"));
			} catch (NumberFormatException e) {
				throw new Exception(
						sResHash
								.getString("PARSER.SAXXML.READ.TIMEOUT.VALUE.HAS.TO.BE.NUMBER"));
			}
		}
		// Kick of thread
		new Thread(saxContentHandler).start();
		Trace.exitmin(this, "initParser");
	}

	/**
	 * Stops the Sax Content Handler thread.
	 *
	 * @throws Exception
	 */
	public synchronized void closeParser() throws Exception {
		Trace.entrymin(this, "closeParser");

		if (saxContentHandler != null)
			saxContentHandler.TerminateThread();

		saxContentHandler = null;
		bEOFFlag = true;
		super.closeParser();
		Trace.exitmin(this, "closeParser");
	}

	/**
	 * readEntry() uses the queue object to check for new entries provided by
	 * the xml-parser thread. The object found in the queue governs when an
	 * entry is ready, an end-of-file has been reached or if there is an error
	 * parsing the document. The method simply checks the class type for the
	 * object and decides what to do next.
	 *
	 * @return The next Entry in the XML document.
	 * @throws Exception
	 *             if an error occurs.
	 */

	public synchronized Entry readEntry() throws Exception {
		Trace.entrymax(this, "readEntry");
		Object message = null;
		Entry entryObject = null;

		// if EOF is reached return null
		if (bEOFFlag) {
			return null;
		}

		saxContentHandler.triggerReadNextEntry();
		if (queue.size() == 0) {
			try {
				wait(readEntryWaitTimeout);
			} catch (InterruptedException e) {
				throw new Exception(sResHash.getString(
						"PARSER.SAXXML.ERROR.IN.READENTRY.MESSAGECLASS",
						new Object[] { e.getClass().getName(), e }));
			}
		}

		if (queue.size() == 0) {
			// TODO Find a way to stop Runnable object, making it null may cause
			// error.
			saxContentHandler = null;
			closeParser();
			throw new Exception(
					sResHash
							.getString("PARSER.SAXXML.ERROR.IN.READENTRY.EXCEEDED.STIPULATED"));
		}

		// -- get the Entry from queue.
		message = queue.remove(0);
		if (message == null) {
			entryObject = null;
		} else if (message instanceof String) {
			if (message.equals("END OF FILE")) {
				bEOFFlag = true;
				entryObject = null;
			}
		} else if (message instanceof Entry) {
			entryObject = (Entry) message;
		} else if (message instanceof Exception) {
			closeParser();
			throw new Exception(sResHash.getString(
					"PARSER.SAXXML.ERROR.IN.READENTRY.MESSAGECLASS.EXCEPTION",
					new Object[] { message.getClass().getName(), message }));
		}

		//bReadFlag = false;
		Trace.exitmax(this, "readEntry", entryObject);
		return entryObject;
	}

	/**
	 * Not supported
	 *
	 * @param entry
	 * @throws Exception
	 *
	 */
	public void writeEntry(Entry entry) throws Exception {
		throw new Exception(
				sResHash
						.getString("PARSER.SAXXML.WRITEENTRY.NOT.SUPPORTED.BY.XMLPARSER"));
	}

	/**
	 * Used by SAXContentHandler thread to put the Entry into XMLSaxParser's
	 * queue and notify XMLSaxParser.
	 *
	 * @param message
	 *            Object
	 *
	 */
	private synchronized void putEntry(Object message) {
		if (message != null) { // Method modified for defect 11727
			queue.add(message);
			notifyAll();
		}
	}

	/**
	 * @return version information
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I% 20%E%";
	}

	/**
	 * Resets the values of all properties
	 */
	private synchronized void resetProperties() {
		bEOFFlag = false;
		//bReadFlag = false;
		saxContentHandler = null;
		queue = null;
		readEntryWaitTimeout = 0;
	}

	// -------------------------------------------------------------------------
	// SAXContentHandler inner class
	//
	//
	// -------------------------------------------------------------------------

	/**
	 * Private inner class, derived from org.xml.sax.helpers.DefaultHandler and
	 * implements Runnable interface.
	 */
	private class SAXContentHandler extends DefaultHandler implements Runnable {
		/**
		 * flag used by startElement to wait/notify
		 */
		private boolean bReadNextEntry = false;

		/**
		 * parent class
		 */
		private XMLSaxParser listener;

		/**
		 * javax.xml.parsers.SaxParser instance
		 */

		private XMLReader reader;

		/**
		 * {@link InputSource}
		 */
		//
		private InputSource input;

		/**
		 * entry object
		 */
		private Entry entry;

		/**
		 * XML tag
		 */

		private String tag;

		/**
		 * Group tag
		 */
		private String groupTag;

		/**
		 * This is the list of groupTag markers
		 */

		private ArrayList<String> groupTags;

		/**
		 * This the root tag (first startElement call)
		 */

		private String rootTag;

		// debug flag
		// private boolean debug;

		/**
		 * Prefix name
		 */
		private String removePrefix;

		/**
		 * ignoreAttribute flag
		 */
		private boolean ignoreAttributes = false;

		/**
		 * true if we have found a group tag
		 */

		private boolean bGroupTag = false;

		/**
		 * validation feature id
		 */
		private static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

		/**
		 * XSD validation feature id
		 */
		private static final String SCHEMA_VALIDATION_ID = "http://apache.org/xml/features/validation/schema";

		/**
		 * namespace feature id
		 */
		private static final String NAMESPACE_FEATURE_ID = "http://xml.org/sax/features/namespaces";

		/**
		 * validation flag
		 */
		private boolean bValidation = false;

		/**
		 * Schema validation (only used when we are validating
		 */
		private boolean schemaValidation;
		
		/**
		 * namespace flag
		 */
		private boolean bNamespaceAware = false;

		/**
		 * counts nested level
		 */
		private int iNestingCount = 0;

		/**
		 * parse error flag
		 */
		private boolean bParseError = false;

		/**
		 * Holds message for parse exception
		 */
		private String strParseException = "";

		// defect 2526
		// Keep appending data to this string across multiple calls to
		// characters() in a single tag.
		/**
		 * Holds and appends data across multiple calls to characters()
		 */
		private String strData = "";

		/**
		 * flag for following characters.
		 */
		private boolean bCharactersAgain = false;

		private boolean isTerminated = false;

		/**
		 * Elements map.
		 */
		private Map<String, String> elementsCharMap;

		// end-fix 2526

		/**
		 * Constructor
		 *
		 * @param listener
		 *            parent object
		 * @param input
		 *            InputSource object
		 * @throws Exception
		 */
		public SAXContentHandler(XMLSaxParser listener, InputSource input)
				throws Exception {
			Trace.entrymid(this, "SAXContentHandler");
			this.listener = listener;
			this.input = input;
			this.elementsCharMap = new HashMap<String, String>();
			reader = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
			reader.setContentHandler(this);
			reader.setErrorHandler(this);
			Trace.exitmid(this, "SAXContentHandler");
		}

		// the debug flag was never used, thus removed
		// /**
		// * setDebug()
		// *
		// * @param debug
		// */
		// public void setDebug(boolean debug) {
		// this.debug = debug;
		// }

		/**
		 * Sets Group tag
		 *
		 * @param groupTag
		 *            String
		 */
		public void setGroupTag(String groupTag) {
			if (groupTag == null || groupTag.length() == 0) {
				this.groupTag = null;
			} else {
				this.groupTag = groupTag;
				this.groupTags = new ArrayList<String>();
				StringTokenizer st = new StringTokenizer(groupTag, ",");
				while (st.hasMoreTokens())
					groupTags.add(st.nextToken());
			}
		}

		/**
		 * Sets prefix
		 *
		 * @param removePrefix
		 *            String
		 */
		public void setRemovePrefix(String removePrefix) {
			if (removePrefix == null || removePrefix.length() == 0) {
				this.removePrefix = null;
			} else {
				this.removePrefix = removePrefix;
			}
		}

		/**
		 * setIgnoreAttributes()
		 *
		 * @param ignore
		 */
		public void setIgnoreAttributes(String ignore) {
			if (ignore != null && ignore.equalsIgnoreCase("true")) {
				setIgnoreAttributes(true);
			} else {
				setIgnoreAttributes(false);
			}
		}

		/**
		 * setIgnoreAttributes() is called by XMLSaxParser to set the
		 * ignoreAttribute
		 *
		 * @param ignoreAttributes
		 */
		public synchronized void setIgnoreAttributes(boolean ignoreAttributes) {
			this.ignoreAttributes = ignoreAttributes;
		}

		// the charSet was never used, thus this method is removed.
		// public void setCharactetSet(String charSet) {
		// this.charSet = charSet;
		// }

		/**
		 * Sets validation flag
		 *
		 * @param validation
		 *            String , true/false
		 */
		public void setValidation(String validation) {
			if (validation != null && validation.equalsIgnoreCase("true")) {
				setValidation(true);
			} else {
				setValidation(false);
			}
		}

		/**
		 * Sets validation flag
		 *
		 * @param validation
		 *            boolean
		 */
		public void setValidation(boolean validation) {
			this.bValidation = validation;
		}

		/**
		 * Sets XSD Schema validation flag
		 *
		 * @param validation
		 *            String , true/false
		 */
		public void setSchemaValidation(String value) {
			schemaValidation = Boolean.valueOf(value);
		}

		/**
		 * Sets namespace flag
		 *
		 * @param namespace
		 *            String , true/false.
		 */
		public void setNamespaceAware(String namespace) {
			if (namespace != null && namespace.equalsIgnoreCase("true")) {
				setNamespaceAware(true);
			} else {
				setNamespaceAware(false);
			}
		}

		/**
		 * Sets namespace flag
		 *
		 * @param namespaceAware
		 *            boolean.
		 */
		public void setNamespaceAware(boolean namespaceAware) {
			this.bNamespaceAware = namespaceAware;
		}

		/**
		 * run() parses XML documents and in case of error it puts the error in
		 * XMLSaxParser's queue.
		 */
		public void run() {
			Trace.entrymid(this, "run");
			if (debugMode()) {
				debug(sResHash.getString("PARSER.SAXXML.NAMESPACE.INFO"));
			}

			try {
				reader.setFeature(VALIDATION_FEATURE_ID, bValidation);
				if (schemaValidation)
					reader.setFeature(SCHEMA_VALIDATION_ID, true);
				reader.setFeature(NAMESPACE_FEATURE_ID, bNamespaceAware);
				reader.parse(input);
			} catch (Exception error) {
				listener.putEntry(error);
			}

			if (debugMode()) {
				debug(sResHash.getString("PARSER.SAXXML.SAX.PARSER.TERMINATED"));
			}

			Trace.exitmid(this, "run");
		}

		/**
		 * isGroupTag() checks tag is group tag or not. If not returns false
		 * otherwise returns true.
		 *
		 * @param tag
		 * @return boolean
		 */
		public boolean isGroupTag(String tag) {
			String compare = tag;
			if (debugMode()) {
				debug(sResHash.getString("PARSER.SAXXML.ISGROUPTAG.TAG",
						new Object[] { tag, groupTag }));
			}

			if (compare == null) {
				return false;
			}

			if (groupTag == null) {
				// if no group-tag specified we treat endofdoc as group tag
				return true;
			}

			// Any descendent
			for (int i = 0; i < groupTags.size(); i++) {
				boolean match = false;
				String gt = groupTags.get(i);
				if (gt.startsWith("*")) {
					match = (compare.indexOf(gt.substring(1)) != -1);
				} else {
					match = (compare.equals(gt));
				}

				// Update to last known group tag
				if (match) {
					groupTag = tag;
					return true;
				}
			}
			return false;
		}

		/**
		 * Retrieves tag name without prefix.
		 *
		 * @param tag
		 *            String
		 * @return String
		 */
		public String getAttributeName(String tag) {
			if (removePrefix == null) {
				return tag;
			}

			if (tag == null) {
				return "";
			}

			if (tag.startsWith(removePrefix)) {
				return tag.substring(removePrefix.length());
			} else {
				return tag;
			}
		}

		/**
		 * characters() receives notification of character data inside an
		 * element.
		 *
		 * @param ch -
		 *            The characters.
		 * @param start -
		 *            The start position in the character array.
		 * @param length -
		 *            The number of characters to use from the character array.
		 */
		public synchronized void characters(char[] ch, int start, int length) {
			String str = (new String(ch, start, length));
			if (debugMode()) {
				debug(sResHash.getString("PARSER.SAXXML.CHARACTERS", str));
			}

			// D9241
			// if (str.equals("")) {
			// return;
			// }

			// -- if data belongs to Group Tag then only save it in Entry object
			if (bGroupTag) {
				// TODO take this out and put it in endElement if groupTag is
				// true.
				// String name = getAttributeName (tag);

				// if (entry == null)
				// {
				// logmsg("INCREDIBLE BUG: entry null in characters() method of
				// XMLSaxParser????");
				// entry = new Entry();
				// }

				// to fix 2526
				if (bCharactersAgain == true) {
					strData += str;
					elementsCharMap.put(tag, "characters");
				} else {
					strData = str;
					elementsCharMap.put(tag, "nocharacters");
					bCharactersAgain = true;
				}
				// end-fix 2526

				// TODO take this out and put it in endElement if groupTag is
				// true.
				// entry.addAttributeValue (name, str);
			}
		}

		/**
		 * endDocument() receives notification of the end of the document
		 *
		 */
		public void endDocument() {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.SAXXML.ENDDOCUMENT"));
			}

			bCharactersAgain = false;

//			synchronized (this) {
//
//				// Simulate end of element for doc root
//				if (groupTag == null) {
//					endElement("", rootTag, rootTag);
//				}
//
//				while (!bReadNextEntry) {
//					try {
//						wait();
//					} catch (InterruptedException e) {
//						if (debugMode()) {
//							debug(sResHash
//									.getString(
//											"PARSER.SAXXML.ENDDOCUMENT.INTERRUPTEDEXCEPTION",
//											e.getMessage()));
//						}
//						return;
//					}
//				}

			// The method was modified for defec # 11727 the wait has been coomented out.
			//The wait resuted  in too many threads being created and ultimately resulting in a OutOfMemoryError.
				listener.putEntry("END OF FILE");

		}

		/**
		 * endElement() receives notification of the end of an element. It also
		 * adds the Entry to listner's queue if element is of type group tag.
		 *
		 * @param uri -
		 *            The Namespace URI, or the empty string if the element has
		 *            no Namespace URI or if Namespace processing is not being
		 *            performed.
		 * @param localName -
		 *            The local name (without prefix), or the empty string if
		 *            Namespace processing is not being performed.
		 * @param qName -
		 *            The qualified name (with prefix), or the empty string if
		 *            qualified names are not available.
		 */
		public void endElement(String uri, String localName, String qName) {
			Trace.entrymax(this, "endElement");
			bCharactersAgain = false;
			if (debugMode()) {
				debug(sResHash.getString(
						"PARSER.SAXXML.ENDELEMENT.QUALIFIED.NAME", qName));
			}
			synchronized (this) {
				String nodeName = getAttributeName(tag);
				String tagHasCharacters = elementsCharMap.remove(tag);
				if ((strData.equals("") && tagHasCharacters == null)) {
					strData = " ";
					tagHasCharacters = "characters";
				}
				if (tagHasCharacters != null
						&& tagHasCharacters.equals("characters")
						&& entry != null) {
					entry.addAttributeValue(nodeName, strData.trim());
					strData = ""; // D9241
				}

				if (isGroupTag(qName)) {
					iNestingCount--;
					if (iNestingCount == 0) {
						// if (entry != null)
						// {
						if (bParseError == false) {
							listener.putEntry(entry);
						} else {
							listener.putEntry(new Exception(strParseException));
						}
						// }

						strData = "";
						entry = null;
						bGroupTag = false;
						bReadNextEntry = false;
					}
				}

				if (tag != null && tag.lastIndexOf("@") != -1) {
					tag = tag.substring(0, tag.lastIndexOf("@"));
				} else {
					tag = null;
				}
			}
			Trace.exitmax(this, "endElement");
		}

		/**
		 * not used endPrefixMapping
		 *
		 * @param prefix
		 *            String
		 *
		 */
		public void endPrefixMapping(String prefix) {
			if (debugMode()) {
				debug(sResHash.getString(
						"PARSER.SAXXML.ENDPREFIXMAPPING.PREFIX", prefix));
			}

			bCharactersAgain = false;
		}

		/**
		 * used for debug only.
		 *
		 * @param ch
		 * @param start
		 * @param length
		 *
		 *
		 */
		public void ignorableWhitespace(char[] ch, int start, int length) {
			if (debugMode()) {
				debug(sResHash.getString(
						"PARSER.SAXXML.IGNORABLEWHITESPACE.LENGTH", String
								.valueOf(length)));
			}
		}

		/**
		 * not used. processingInstruction()
		 *
		 * @param target
		 *            String
		 * @param data
		 *            String
		 *
		 */
		public void processingInstruction(String target, String data) {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.SAXXML.PROCESSINGINSTRUCTION"));
			}

			bCharactersAgain = false;
		}

		/**
		 * not used
		 *
		 * @param locator
		 *
		 */
		public void setDocumentLocator(Locator locator) {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.SAXXML.SETDOCUMENTLOCATOR"));
			}
			bCharactersAgain = false;
		}

		/**
		 * Writes log for skipped entry
		 *
		 * @param name
		 *            String
		 */
		public void skippedEntity(String name) {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.SAXXML.SKIPPEDENTITY"));
			}
			bCharactersAgain = false;
		}

		/**
		 * Receives notification of the beginning of the document
		 *
		 */
		public void startDocument() {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.SAXXML.STARTDOCUMENT"));
			}
		}

		/**
		 * Receives notification of the start of an element. It waits for
		 * trigger from listner. On trigger from listener it reads the XML
		 * element and its attributes and saves it in Entry object.
		 *
		 * @param uri
		 *            The Namespace URI, or the empty string if the element has
		 *            no Namespace URI or if Namespace processing is not being
		 *            performed.
		 * @param localName
		 *            The local name (without prefix), or the empty string if
		 *            Namespace processing is not being performed.
		 * @param qName
		 *            The qualified name (with prefix), or the empty string if
		 *            qualified names are not available.
		 * @param attrs
		 *            The attributes attached to the element. If there are no
		 *            attributes, it shall be an empty Attributes object.
		 */
		public void startElement(String uri, String localName, String qName,
				Attributes attrs) {
			Trace.entrymax(this, "startElement");
			if (debugMode()) {
				debug(sResHash.getString("PARSER.SAXXML.STARTELEMENT.LN.QN",
						new Object[] { localName, qName }));
			}
			bCharactersAgain = true; // D9241
			// bStillInsideTag = true;
			synchronized (this) {
			  while ( !bReadNextEntry  && !isTerminated ) {
					try {
						wait();
					} catch (InterruptedException e) {
						if (debugMode()) {
							debug(sResHash
									.getString(
											"PARSER.SAXXML.STARTELEMENT.INTERRUPTEDEXCEPTION",
											e.getMessage()));
						}
						return;
					}
				}

				if (tag == null) {
					tag = qName;
				} else {
					tag += "@" + qName;
				}

				// save root tag
				if (rootTag == null)
					rootTag = tag;

				if (isGroupTag(qName) || bGroupTag == true) {
					if (isGroupTag(qName))
						iNestingCount++;

					bGroupTag = true;
					if (entry == null) {
						entry = new Entry();
					}

					if (!ignoreAttributes) {
						for (int i = 0; i < attrs.getLength(); i++) {
							String attr = getAttributeName(tag + "#"
									+ attrs.getQName(i));
							entry.addAttributeValue(attr, attrs.getValue(i));
						}
					}

					// added to fix 2526
					strData = "";
					// end-fix 2526
				}

			}
			Trace.exitmax(this, "startElement");
		}

		/**
		 * triggerReadNextEntry(), a synchronized private method, used by
		 * listener to trigger waiting thread in startElement to read one entry
		 * from XML document.
		 *
		 */
		private void triggerReadNextEntry() {
			synchronized (this) {
				if (bReadNextEntry == false) {
					bReadNextEntry = true;
					notifyAll();
					return;
				}
			}
		}

		/**
				 *  Added new method, which is being called from closeParser() to stop the thread created.
				 */

				private void TerminateThread() {
							synchronized (this) {
									if (isTerminated == false) {
										isTerminated = true;
										notifyAll();
										return;
										}
								}
			}

		/**
		 * not used
		 *
		 * @param prefix
		 *            String
		 * @param uri
		 *            String
		 */
		public void startPrefixMapping(String prefix, String uri) {
			if (debugMode()) {
				debug(sResHash.getString(
						"PARSER.SAXXML.STARTPREFIXMAPPING.URI", new Object[] {
								prefix, uri }));
			}
			bCharactersAgain = false;
		}

		/**
		 * Logs error.
		 *
		 * @param exception
		 *            SAXParseException
		 */
		public synchronized void fatalError(SAXParseException exception) {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.SAXXML.FATALERROR.EXCEPTION",
						exception));
			}
			bParseError = true;
			strParseException = exception.getMessage();
		}

		/**
		 * Logs error.
		 *
		 * @param exception
		 *            SAXParseException
		 * @throws SAXException
		 */
		public synchronized void error(SAXParseException exception)
				throws SAXException {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.SAXXML.ERROR"));
			}
			bParseError = true;
			strParseException = exception.getMessage();
		}

		/**
		 * Logs warning
		 *
		 * @param exception
		 *            SAXParseException
		 * @throws SAXException
		 */
		public synchronized void warning(SAXParseException exception)
				throws SAXException {
			String funcmsg = sResHash.getString(
					"PARSER.SAXXML.SAX.PARSE.WARNING.LINE.NUMBER",
					new Object[] { String.valueOf(exception.getLineNumber()),
							exception.getSystemId() });
			logmsg(funcmsg);
			bParseError = true;
			strParseException = funcmsg;
		}
	} // -- end of inner class SaxContentHandler
} // -- end of class XMLSaxParser
