/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import com.sap.mw.jco.JCO;

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
		return getDecoder()
				.decode(getDom().getDocumentElement().getLocalName());
	}

	public void importData(JCO.Function func) throws IfrImporterException {
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

	void importXml(JCO.ParameterList pList, Document doc)
			throws IfrImporterException {
		JCO.FieldIterator fi = pList.fields();
		fi.reset();
		while (fi.hasMoreFields()) {
			JCO.Field f = fi.nextField();
			String encodedName = getEncoder().encode(f.getName());
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
				if (!f.isOptional() && f.isImport()) {
					throw new IfrImporterException(LogMessageHelper
							.getMsgResource().getMessage(
									LogMessageHelper.SAPR3_RFCFC_0009,
									new Object[] { f.getName() }));
				}
			}
		}
	}

	void importXml(JCO.Field field, Element ele) throws IfrImporterException {
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

		field.setValue(sb.toString());
	}

	void importXml(JCO.Record rec, Element ele) throws IfrImporterException {
		JCO.FieldIterator fi = rec.fields();
		fi.reset();
		while (fi.hasMoreFields()) {
			JCO.Field f = fi.nextField();
			String encodedName = getEncoder().encode(f.getName());
			NodeList nList = ele.getElementsByTagName(encodedName);
			if ((nList != null) && (nList.getLength() > 0)) {
				if ((nList.getLength() > 1) && (log.getDebug())) {
					log.debug(LogMessageHelper.SAPR3_RFCFC_0027,
							new Object[] { rec.getName() });
				}
				Element childEle = (Element) nList.item(0); // just grab the
															// first element
				importXml(f, childEle); // call JCO.Field overload
			}
		}
	}

	void importXml(JCO.Table tab, Element ele) throws IfrImporterException {
		NodeList nList = ele.getElementsByTagName(ITEM);
		if (nList != null) {
			for (int i = 0; i < nList.getLength(); ++i) {
				tab.appendRow();
				importXml((JCO.Record) tab, (Element) nList.item(i));
			}
		}

	}

	void importXml(JCO.Structure struct, Element ele)
			throws IfrImporterException {
		importXml((JCO.Record) struct, ele);
	}
}
