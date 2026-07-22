/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.io.BufferedWriter;

import org.eclipse.hyades.logging.events.cbe.CommonBaseEvent;
import org.eclipse.hyades.logging.events.cbe.FormattingException;
import org.eclipse.hyades.logging.events.cbe.ValidationException;
import org.eclipse.hyades.logging.events.cbe.util.EventFormatter;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.cbe.CBEGeneratorFC;
import com.ibm.di.parser.xml.XMLParser2;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;

/**
 * 
 * A class that reads and writes CBE objects in XML format.
 * 
 */
public class CBEParser extends XMLParser2 {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "cbeparser";

	/**
	 * XML version/encoding declaration .
	 * 
	 */
	// The <encoding> expression will be changed with the correct encoding set
	// by the user
	private static final String CBE_XMLDECLARATION = "<?xml version=\"1.0\" encoding=\"<encoding>\"?>";

	/**
	 * Namespace
	 */
	private static final String CBE_XMLNS = "http://www.ibm.com/AC/commonbaseevent1_0_1";

	// by default the CBE library uses internal schema for validating, the below
	// two rows are left in case in future that is changed
	// private static final String CBE_CMLNS_XSI =
	// "http://www.w3.org/2001/XMLSchema-instance";

	// private static final String CBE_XSI_SCHEMALOCATION =
	// "http://www.ibm.com/AC/commonbaseevent1_0_1 commonbaseevent1_0_1.xsd";

	/**
	 * Default encoding - UTF-8
	 */
	private static final String CBE_DEFAULT_ENCODING = "UTF-8";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = null;

	/**
	 * CBE Generator Function Component
	 */
	private CBEGeneratorFC cbeFC = null;

	/**
	 * {@link BufferedWriter} object used to write XML data
	 */
	private BufferedWriter writer = null;

	/**
	 * Indicates whether CBE opening tag is written.
	 */
	private boolean isCBEHeaderWritten = false;

	static {
		resHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Default constructor.
	 */
	public CBEParser() {
		Trace.entrymid(this, "CBEParser");
		Trace.exitmid(this, "CBEParser");
	}

	/**
	 * {@inheritDoc}
	 */
	public void initInput() throws Exception {
		// set the parent XMLParser to read CommonBaseEvent Objects
		xPathStr = "CommonBaseEvent | /CommonBaseEvents/CommonBaseEvent";
		super.initInput();
	}

	/**
	 * Reads the inputStream and fill the Entry Object that it returns
	 * 
	 * @return Entry - the Entry object containing the CBE attributes and the
	 *         CBE object itself or null if Input is exhausted.
	 * @throws Exception
	 *             <li>Exception - If unable to parse a document to a CBE
	 *             object</li>
	 *             <li>FormattingException - incorrect XML is reached. </li>
	 *             <li>DOMException - If unable to move or copy a node from one
	 *             document to another </li>
	 * 
	 */
	public Entry readEntry() throws Exception {
		Trace.entrymax(this, "readEntry");

		Entry cbeEntry = super.readEntry();
		if (cbeEntry == null) {
			return null;
		}

		CommonBaseEvent cbe = null;
		try {
			boolean validateXML = Boolean.parseBoolean(getParam("validateXML"));

			String str = getCurrentEntryAsXMLString();
			cbe = EventFormatter.eventFromCanonicalXML(str.substring(str
					.indexOf(">") + 1), validateXML);
		} catch (FormattingException fe) {
			throw new FormattingException(resHash.getString(
					"PARSER.CBE.INVALID.XML.ERR", fe.getMessage()));
		}

		Entry resultEntry = new Entry();

		CBEGeneratorFC.mapCbeToEntry(cbe, resultEntry);

		resultEntry.addAttributeValue("event", cbe);

		Trace.exitmax(this, "readEntry", resultEntry);
		return resultEntry;
	}

	/**
	 * Initialize and prepare the outputStream and prepare the outputDoc.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */

	public void initOutput() throws Exception {
		Trace.entrymax(this, "initOutput");

		// initialize the CBE FC
		cbeFC = new CBEGeneratorFC();
		cbeFC.setConfiguration(new BaseConfigurationImpl());
		cbeFC.initialize();
		cbeFC.setParam("mode", "0");

		writer = getWriter();
		if (writer == null) {
			throw new Exception(resHash
					.getString("PARSER.CBE.WRITER.NOT.INITIALIZED"));
		}

		Trace.exitmax(this, "initOutput");
	}

	/**
	 * Create/get the CBE object and convert it to XML. Then update outpuDoc and
	 * prepare it for writing. When the parser is done the flush() method will
	 * write the document to the outputStream.
	 * 
	 * @param entry -
	 *            The entry that is going to be written to the outputStream as
	 *            XML
	 * @throws Exception -
	 *             in the following cases:
	 *             <li>Invalid Document Object retrieved from the XML </li>
	 *             <li>If unable to generate CBE object from attributes </li>
	 *             <li>If unable to parse a XML to a Document </li>
	 *             <li>If Unable to work with CBE Node from the Document </li>
	 *             <li>DOMException - If unable to move or copy a node from one
	 *             document to another </li>
	 *             <li>ValidationException - If the passed CBE object is
	 *             invalid. </li>
	 */

	public void writeEntry(Entry entry) throws Exception {
		Trace.entrymax(this, "writeEntry", entry);

		if (!isCBEHeaderWritten) {
			if (!"true".equalsIgnoreCase(getParam("omitxmldeclaration"))) {
				String encoding = getParam("characterSet");
				if (encoding == null || encoding.trim().length() == 0) {
					encoding = CBE_DEFAULT_ENCODING;
				}

				writer.write(CBE_XMLDECLARATION.replaceFirst("<encoding>",
						encoding));
				writer.write("\n");
				writer.flush();
			}

			// write the opening tag with the default CBE namespace
			writer.write("<CommonBaseEvents xmlns=\"" + CBE_XMLNS + "\">");
			writer.write("\n");
			writer.flush();

			isCBEHeaderWritten = true;
		}

		String cbeXML = null;

		Object o = entry.getObject("event");

		CommonBaseEvent cbeObj = null;

		if (o instanceof CommonBaseEvent) {

			cbeObj = (CommonBaseEvent) o;

			try {
				cbeObj.validate();
			} catch (ValidationException ve) {
				throw new ValidationException(resHash
						.getString("PARSER.CBE.INVALID.CBE")
						+ ve.getMessage());
			}
			cbeXML = EventFormatter.toCanonicalXMLString(cbeObj);
		} else {
			// get the XML of the CBE
			Entry cbeEntry = (Entry) cbeFC.perform(entry);
			if (cbeEntry != null) {
				cbeXML = cbeEntry.getString("eventXml");
			} else {
				throw new Exception(
						resHash
								.getString("PARSER.CBE.ERROR.GENERATING.CBE.FROM.ATTRIBUTES"));
			}
		}

		writer.write(cbeXML);
		writer.write("\n");
		writer.flush();

		Trace.exitmax(this, "writeEntry");
	}

	/**
	 * {@inheritDoc}
	 */
	public void closeParser() throws Exception {
		if (isCBEHeaderWritten) {
			// write the closing tag
			writer.write("</CommonBaseEvents>");
			writer.flush();
			writer.close();
			writer = null;
		}

		super.closeParser();
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 * 
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I% 20%E%";
	}

	/**
	 * This Parser does not support dynamic schema discovery and uses the
	 * default Query Schema implementation.
	 */
	public Object querySchema(Object source) {
		return null;
	}
}
