/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import com.sap.conn.jco.*;

/**
 * Deserializes RFC DOM request data.
 */
final class IfrDOMImporterImpl implements IfrImporter {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private AbapIfrDecoder abapDecoder;

	private AbapIfrEncoder abapEncoder;

	/* The input document */
	private Document xmlDoc;

	private final LogProxy log;

	IfrDOMImporterImpl(Document doc, LogProxy logger) {
		super();
		if (doc == null) {
			throw new IllegalArgumentException();
		}
		if (logger == null) {
			throw new IllegalArgumentException();
		}
		setDecoder(IfrFunctionFactory.createDecoder());
		setEncoder(IfrFunctionFactory.createEncoder());
		setDom(doc);
		log = logger;
	}

	private void setDom(Document doc) {
		xmlDoc = doc;
	}

	private Document getDom() {
		return xmlDoc;
	}

	private AbapIfrEncoder getEncoder() {
		return abapEncoder;
	}

	private void setEncoder(AbapIfrEncoder encoder) {
		if (encoder == null) {
			throw new IllegalArgumentException();
		}
		abapEncoder = encoder;
	}

	private AbapIfrDecoder getDecoder() {
		return abapDecoder;
	}

	private void setDecoder(AbapIfrDecoder decoder) {
		if (decoder == null) {
			throw new IllegalArgumentException();
		}
		abapDecoder = decoder;
	}

	public String getFunctionName() {
		
		String funcName = getDecoder().decode(getDom().getDocumentElement().getLocalName());
//		System.out.println("Class IfrDOMImporter: function name: " + funcName);
		return funcName;
	}

	public void importData(JCoFunction func) throws IfrImporterException {
		if (!func.getName().equals(getFunctionName())) {
			throw new IfrImporterException(LogMessageHelper.getMsgResource()
					.getMessage(LogMessageHelper.SAPR3_RFCFC_0008,
							new Object[] { func.getName(), getFunctionName() }));
		}

		if (func.getImportParameterList() != null) {
			importXml(func.getImportParameterList(), getDom());
		}

		if (func.getTableParameterList() != null) {
			importXml(func.getTableParameterList(), getDom());
		}
	}

	private Element getChildElementWithName(String name, Element parent) {
		NodeList nl = parent.getChildNodes();
		for (int i = 0; i < nl.getLength(); ++i) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;
				if (e.getTagName().equals(name)) {
					return e;
				}
			}
		}

		return null;
	}

	void importXml(JCoParameterList pList, Document doc)
			throws IfrImporterException {
		JCoFieldIterator fi = pList.getFieldIterator();
		JCoListMetaData metadata = pList.getListMetaData();
		fi.reset();
		while (fi.hasNextField()) {
			JCoField f = fi.nextField();
			String fieldName = f.getName();
			String encodedName = getEncoder().encode(f.getName());
//			System.out.println("In class IfrDOMImporter, method importXml(JCoParameterList pList, Document doc) ");
//			System.out.println("FieldName: " + fieldName);
//			System.out.println("Encoded Name: " + encodedName);
			Element ele = getChildElementWithName(encodedName, doc
					.getDocumentElement());
			if (ele != null) {
				if (f.isStructure()) {
					// import structure
					importXml(f.getStructure(), ele);
				} else if (f.isTable()) {
					// import table
					importXml(f.getTable(), ele);
				} else {
					// import simple field
					importXml(f, ele);
				}
			} else {
				
				if (!metadata.isOptional(fieldName) && metadata.isImport(fieldName)) {
					throw new IfrImporterException(LogMessageHelper
							.getMsgResource().getMessage(
									LogMessageHelper.SAPR3_RFCFC_0009,
									new Object[] { f.getName() }));
				}
			}
		}
	}

	void importXml(JCoField field, Element ele) throws IfrImporterException {
		Node child = ele.getFirstChild();
		StringBuffer sb = new StringBuffer();
		while (child != null) {
			if ((child.getNodeType() == Node.TEXT_NODE)
					|| (child.getNodeType() == Node.CDATA_SECTION_NODE)) {
				sb.append(child.getNodeValue());
			}
			child = child.getNextSibling();
		}

		if (sb.length() > field.getLength()) {
			throw new IfrImporterException(LogMessageHelper.getMsgResource()
					.getMessage(
							LogMessageHelper.SAPR3_RFCFC_0010,
							new Object[] { field.getName(),
									"" + field.getLength() }));
		}
//		System.out.println("In class IfrDOMImporter method mportXml(JCoField field, Element ele)" );
//		System.out.println("Setting value of field " + field.toString() + " with value " + sb.toString());
		field.setValue(sb.toString());
	}

	void importXml(JCoRecord rec, Element ele) throws IfrImporterException {
		JCoFieldIterator fi = rec.getFieldIterator();
		fi.reset();
		while (fi.hasNextField()) {
			JCoField f = fi.nextField();
			String encodedName = getEncoder().encode(f.getName());
//			System.out.println("In class IfrDOMImporter, method importXml(JCoRecord rec, Element ele) ");
//			System.out.println("FieldName: " + f.getName());
//			System.out.println("Encoded Name: " + encodedName);
			NodeList nList = ele.getElementsByTagName(encodedName);
			if ((nList != null) && (nList.getLength() > 0)) {
				if ((nList.getLength() > 1) && (log.getDebug())) {
					log.debug(LogMessageHelper.SAPR3_RFCFC_0027,
							new Object[] { rec.toString() });
				}
				Element childEle = (Element) nList.item(0); // just grab the
															// first element
				importXml(f, childEle); // call JCO.Field overload
			}
		}
	}

	void importXml(JCoTable tab, Element ele) throws IfrImporterException {
		NodeList nList = ele.getElementsByTagName(ITEM);
		if (nList != null) {
			for (int i = 0; i < nList.getLength(); ++i) {
				tab.appendRow();
				importXml((JCoRecord) tab, (Element) nList.item(i));
			}
		}

	}

	void importXml(JCoStructure struct, Element ele)
			throws IfrImporterException {
		importXml((JCoRecord) struct, ele);
	}
}
