/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Enables client code to query the metadata of a given BOR Object Class in SAP
 * R/3.
 */
final class BorSchemaQuery {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String BEGIN_SWO_QUERY_KEY_XML = "<SWO_QUERY_KEYFIELDS><OBJTYPE>";

	private static final String END_SWO_QUERY_KEY_XML = "</OBJTYPE></SWO_QUERY_KEYFIELDS>";

	private static final String INFO_TAG_NAME = "INFO";

	private static final String KEYFIELD_TAG_NAME = "KEYFIELD";

	private static final String EDITELEM_TAG_NAME = "EDITELEM";

	private static final String ITEM_TAG_NAME = "item";

	static final class KeyFieldInfo {
		private final String abapName;

		private final String borName;

		KeyFieldInfo(String nameAbap, String nameBor) {
			if (nameAbap == null) {
				throw new IllegalArgumentException();
			}
			if (nameBor == null) {
				throw new IllegalArgumentException();
			}

			abapName = nameAbap;
			borName = nameBor;
		}

		String getBorName() {
			return borName;
		}

		String getAbapName() {
			return abapName;
		}
	}

	private final Configuration config;

	BorSchemaQuery(Configuration cfg) {
		super();
		if (cfg == null) {
			throw new IllegalArgumentException();
		}

		config = cfg;
	}

	KeyFieldInfo[] getKeyFields() throws FunctionExecutionException {
		StringBuffer sb = new StringBuffer();
		sb.append(BorSchemaQuery.BEGIN_SWO_QUERY_KEY_XML);
		sb.append(config
				.getParamAsString(ConfigurationNames.PARAM_BOR_CLASS_NAME));
		sb.append(BorSchemaQuery.END_SWO_QUERY_KEY_XML);

		XmlFunctionAdapter xmlFunc = new XmlFunction(sb.toString(), config);
		try {
			xmlFunc.execute();
			Document doc = xmlFunc.getResultAsDocument();
			return getKeyFields(doc);
		} catch (EmptyTransformResultException x) {
			// not possible with XmlFunction implementation.
			throw new FunctionExecutionException(x);
		} finally {
			xmlFunc.dispose();
		}

	}

	private KeyFieldInfo[] getKeyFields(Document doc) {
		List tmpResult = new LinkedList();
		if (doc == null) {
			throw new IllegalArgumentException();
		}

		Element root = doc.getDocumentElement();
		NodeList nl = root.getElementsByTagName(BorSchemaQuery.INFO_TAG_NAME);
		if (nl.getLength() > 0) {
			Element infoEle = (Element) nl.item(0);
			NodeList itemNL = infoEle
					.getElementsByTagName(BorSchemaQuery.ITEM_TAG_NAME);
			for (int i = 0; i < itemNL.getLength(); ++i) {
				Element itemEle = (Element) itemNL.item(i);
				NodeList childNL = itemEle.getChildNodes();
				String borName = null;
				String abapName = null;
				for (int j = 0; j < childNL.getLength(); ++j) {
					Node n = childNL.item(j);
					if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = (Element) n;
						if (e.getTagName().equals(
								BorSchemaQuery.EDITELEM_TAG_NAME)) {
							borName = XmlHelper.textValue((Element) n);
						} else if (e.getTagName().equals(
								BorSchemaQuery.KEYFIELD_TAG_NAME)) {
							abapName = XmlHelper.textValue((Element) n);
						}
					}
				}

				if ((borName != null) && (abapName != null)) {
					tmpResult.add(new KeyFieldInfo(abapName, borName));
				}
			}
		}

		KeyFieldInfo[] result = new KeyFieldInfo[tmpResult.size()];
		for (int i = 0; i < tmpResult.size(); i++) {
			result[i] = (KeyFieldInfo) tmpResult.get(i);
		}
		return result;
	}

}
