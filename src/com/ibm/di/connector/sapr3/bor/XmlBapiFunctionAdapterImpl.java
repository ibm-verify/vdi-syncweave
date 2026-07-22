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
 * Adatper for processing BAPI RFC methods. The main extension functionaity
 * added is the processing of the "RETURN" parameter present on all BAPI
 * compliant RFC methods defiend in SAP.
 * 
 */
final class XmlBapiFunctionAdapterImpl implements XmlBapiFunctionAdapter {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String RETURN_TAG_NAME = "RETURN";

	private static final String ITEM_TAG_NAME = "item";

	private static final String TYPE_TAG_NAME = "TYPE";

	private static final String MESSAGE_TAG_NAME = "MESSAGE";

	private static final String NUM_TAG_NAME = "NUMBER";

	private XmlFunctionAdapter xmlFunc;

	private Configuration config;

	private List errorList;

	private List warningList;

	/**
	 * Create new Adapter.
	 * 
	 * @param func
	 *            The function that has been executed.
	 * 
	 * @throws IllegalArgumentException
	 *             if func or cfg are <code>null</code>.
	 */
	XmlBapiFunctionAdapterImpl(XmlFunctionAdapter func, Configuration cfg) {
		super();
		if (func == null) {
			throw new IllegalArgumentException();
		}
		if (cfg == null) {
			throw new IllegalArgumentException();
		}

		xmlFunc = func;
		config = cfg;
		initLists();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlBapiFunctionAdapter#getErrorMessages()
	 */
	public AbapErrorInfo[] getErrorMessages() {
		AbapErrorInfo[] result = (AbapErrorInfo[]) errorList
				.toArray(new AbapErrorInfo[errorList.size()]);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlBapiFunctionAdapter#getWrningMessages()
	 */
	public AbapErrorInfo[] getWarningMessages() {
		AbapErrorInfo[] result = (AbapErrorInfo[]) warningList
				.toArray(new AbapErrorInfo[warningList.size()]);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlFunctionAdapter#execute()
	 */
	public void execute() throws FunctionExecutionException,
			EmptyTransformResultException {
		xmlFunc.execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlFunctionAdapter#getResultAsDocument()
	 */
	public Document getResultAsDocument() throws FunctionExecutionException {
		return xmlFunc.getResultAsDocument();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlFunctionAdapter#getResultAsString()
	 */
	public String getResultAsString() throws FunctionExecutionException {
		return xmlFunc.getResultAsString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlFunctionAdapter#setCriteria(com.ibm.di.connector.sapr3.user.ExecutionCriteria)
	 */
	public void setCriteria(ExecutionCriteria filter) {
		xmlFunc.setCriteria(filter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlFunctionAdapter#getCriteria()
	 */
	public ExecutionCriteria getCriteria() {
		return xmlFunc.getCriteria();
	}

	/*
	 * Dispose of the internal connection to SAP.
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlFunctionAdapter#dispose()
	 */
	public void dispose() throws FunctionExecutionException {
		xmlFunc.dispose();
	}

	private void initLists() {
		errorList = new LinkedList();
		warningList = new LinkedList();
		try {
			Document doc = getResultAsDocument();
			Element root = doc.getDocumentElement();
			Element returnEle = getReturnElement(root);
			initLists(returnEle);
		} catch (FunctionExecutionException x) {
			getConfig().getLog().logwarn(x.getMessage());
		}

	}

	private void initLists(Element retEle) {
		if (isTableStyleReturn(retEle)) {
			NodeList nl = retEle
					.getElementsByTagName(XmlBapiFunctionAdapterImpl.ITEM_TAG_NAME);
			for (int i = 0; i < nl.getLength(); ++i) {
				AbapErrorInfo br = extractData((Element) nl.item(i));
				if (br != null) {
					if (br.isError()) {
						getConfig().getLog().debug(
								LogMessageHelper.getMsgResource().getMessage(
										LogMessageHelper.SAPR3_BOR_0029,
										new Object[] { br.getMsg() }));
						errorList.add(br);
					} else if (br.isWarn()) {
						getConfig().getLog().debug(
								LogMessageHelper.getMsgResource().getMessage(
										LogMessageHelper.SAPR3_BOR_0030,
										new Object[] { br.getMsg() }));
						warningList.add(br);
					}
				}
			}
		} else {
			AbapErrorInfo br = extractData(retEle);
			if (br != null) {
				if (br.isError()) {
					errorList.add(br);
				} else if (br.isWarn()) {
					warningList.add(br);
				}
			}
		}
	}

	private AbapErrorInfo extractData(Element retParent) {
		getConfig().getLog().debug(
				LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0031,
						new Object[] { retParent.getTagName() }));

		NodeList nl = retParent.getChildNodes();
		String errFlag = null;
		String msg = null;
		String errNum = null;
		for (int i = 0; i < nl.getLength(); ++i) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;
				if (e.getTagName().equalsIgnoreCase(
						XmlBapiFunctionAdapterImpl.TYPE_TAG_NAME)) {
					errFlag = extractTextValue(e);
					continue;
				}
				if (e.getTagName().equalsIgnoreCase(
						XmlBapiFunctionAdapterImpl.MESSAGE_TAG_NAME)) {
					msg = extractTextValue(e);
					continue;
				}
				if (e.getTagName().equalsIgnoreCase(
						XmlBapiFunctionAdapterImpl.NUM_TAG_NAME)) {
					errNum = extractTextValue(e);
					continue;
				}
			}
		}

		if (errFlag != null && msg != null && errNum != null) {
			getConfig().getLog().debug(
					LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_BOR_0032,
							new Object[] { msg, errNum, errFlag }));
			return new AbapErrorInfo(msg, errNum, errFlag);
		} else {
			return null;
		}
	}

	private String extractTextValue(Element e) {
		StringBuffer result = new StringBuffer();
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); ++i) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.TEXT_NODE
					|| n.getNodeType() == Node.CDATA_SECTION_NODE) {
				result.append(n.getNodeValue());
			}
		}

		return result.toString();
	}

	private boolean isTableStyleReturn(Element retEle) {
		NodeList nl = retEle
				.getElementsByTagName(XmlBapiFunctionAdapterImpl.ITEM_TAG_NAME);
		boolean result = nl.getLength() > 0;
		getConfig().getLog().debug(
				LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0033,
						new Object[] { "" + result }));
		return result;
	}

	private Element getReturnElement(Element root) {
		NodeList nl = root.getChildNodes();
		for (int i = 0; i < nl.getLength(); ++i) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;
				if (e.getTagName().equalsIgnoreCase(
						XmlBapiFunctionAdapterImpl.RETURN_TAG_NAME)) {
					return e;
				}
			}
		}

		return null;
	}

	private Configuration getConfig() {
		return config;
	}

}
