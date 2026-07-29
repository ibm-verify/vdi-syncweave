/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.ibm.di.entry.Entry;
import com.sap.mw.jco.IRepository;

final class IfrFunctionFactory {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IfrFunctionFactory() {
		super();
	}

	public static IfrSerializer createSerializer(PrintWriter pw) {
		return new IfrXmlSerializerImpl(pw);
	}

	public static IfrSerializer createSerializer(Entry e) {
		return new IfrEntrySerializerImpl(e);
	}

	public static IfrImporter createImporter(Document doc, LogProxy log) {
		return new IfrDOMImporterImpl(doc, log);
	}

	public static IfrImporter createImporter(Reader xmlReader, LogProxy log)
			throws IfrImporterException {
		Document doc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(xmlReader);
			doc = db.parse(is);
		} catch (ParserConfigurationException x) {
			throw new IfrImporterException(x.getMessage(), x);
		} catch (SAXException x) {
			throw new IfrImporterException(x.getMessage(), x);
		} catch (IOException x) {
			throw new IfrImporterException(x.getMessage(), x);
		}

		return IfrFunctionFactory.createImporter(doc, log);
	}

	public static IfrImporter createImporter(String xmlStr, LogProxy log)
			throws IfrImporterException {
		return IfrFunctionFactory.createImporter(new StringReader(xmlStr), log);
	}

	public static IfrImporter createImporter(Entry e) {
		return new IfrEntryImporterImpl(e);
	}

	public static AbapIfrDecoder createDecoder() {
		return new AbapIfrDecoderImpl();
	}

	public static AbapIfrEncoder createEncoder() {
		return new AbapIfrEncoderImpl();
	}

	public static IfrRfcFunction createFunction(IRepository repos) {
		return new IfrRfcFunctionImpl(repos);
	}

}
