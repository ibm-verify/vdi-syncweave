/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.user;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utlilty class for XML operations.
 * 
 */
final class XmlHelper {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final String CHAR_SET = "UTF-8";

	static Document parse(String xmlStr) throws ParserConfigurationException,
			SAXException, IOException {
		if (xmlStr == null) {
			throw new IllegalArgumentException();
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xmlStr));
		return (db.parse(is));
	}

	static String serialize(Document doc) throws IOException {
		if (doc == null) {
			throw new IllegalArgumentException();
		}

		OutputFormat format = new OutputFormat(Method.XML, CHAR_SET, false);
		SerializerFactory factory = SerializerFactory
				.getSerializerFactory(Method.XML);
		Serializer ser = factory.makeSerializer(format);
		StringWriter result = new StringWriter();
		ser.setOutputCharStream(result);
		DOMSerializer domSer = ser.asDOMSerializer();
		domSer.serialize(doc);
		return result.toString();
	}

	/**
	 * Disabled.
	 */
	private XmlHelper() {
		super();
	}

}
