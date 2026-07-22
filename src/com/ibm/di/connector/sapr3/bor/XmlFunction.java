/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;

/**
 * Function Adapter that performs an XSL transformation on an input XML source,
 * and then uses that result as input to a IBM Tivoli Directory Integrator Function Component.
 * 
 */
final class XmlFunction implements XmlFunctionAdapter {

	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String FCPARAM_NAME_REQUEST_TYPE = "requestType";

	private static final String FCPARAM_NAME_REQUEST = "request";

	private static final String FCPARAM_NAME_RESPONSE_TYPE = "responseType";

	private static final String FCPARAM_NAME_RESPONSE = "response";

	private static final String FCPARAM_VALUE_XML_STRING = "xmlString";

	private static final String FCPARAM_VALUE_DOM_DOC = "xmlDomDocument";

	private final Configuration config;

	private final Document inDomXml;

	private final String inStringXml;

	private final Function fc;

	private Entry executeResult;

	private ExecutionCriteria criteria;

	/**
	 * Create Function adapter.
	 * 
	 * @param inXml
	 *            The input data. Will be transformed in XML to be executed by
	 *            sapFC.
	 * @param sapFC
	 *            The function component that will execute the result of the
	 *            style sheet.
	 * @throws IllegalArgumentException
	 *             if any parameters are null.
	 */
	XmlFunction(Document inXml, Configuration cfg)
			throws FunctionExecutionException {
		super();
		if (inXml == null) {
			throw new IllegalArgumentException();
		}
		if (cfg == null) {
			throw new IllegalArgumentException();
		}

		config = cfg;
		inDomXml = inXml;
		inStringXml = null;
		try {
			fc = RfcFunctionFactory.createFC(config);
		} catch (RfcFunctionFactoryException x) {
			throw new FunctionExecutionException(x);
		}
	}

	/**
	 * Create Function adapter.
	 * 
	 * @param inXml
	 *            The input data. Will be transformed in XML to be executed by
	 *            sapFC.
	 * @param sapFC
	 *            The function component that will execute the result of the
	 *            style sheet.
	 * @throws IllegalArgumentException
	 *             is any parameters are null.
	 */
	XmlFunction(String inXml, Configuration cfg)
			throws FunctionExecutionException {
		super();
		if (inXml == null) {
			throw new IllegalArgumentException();
		}
		if (cfg == null) {
			throw new IllegalArgumentException();
		}

		config = cfg;
		inDomXml = null;
		inStringXml = inXml;
		try {
			fc = RfcFunctionFactory.createFC(config);
		} catch (RfcFunctionFactoryException x) {
			throw new FunctionExecutionException(x);
		}
	}

	/**
	 * Execute the function.
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlFunctionAdapter#execute()
	 */
	public synchronized void execute() throws FunctionExecutionException {
		try {
			Entry e = new Entry();
			String reqType = XmlFunction.FCPARAM_NAME_REQUEST_TYPE;
			String reqTypeVal;
			Object reqVal;
			if (inDomXml != null) {
				reqTypeVal = XmlFunction.FCPARAM_VALUE_DOM_DOC;
				reqVal = inDomXml;
			} else if (inStringXml != null) {
				reqTypeVal = XmlFunction.FCPARAM_VALUE_XML_STRING;
				reqVal = inStringXml;
			} else {
				throw new IllegalStateException();
			}

			e.setAttribute(reqType, reqTypeVal);
			e.setAttribute(XmlFunction.FCPARAM_NAME_REQUEST, reqVal);
			executeResult = (Entry) fc.perform(e);
		} catch (Exception x) {
			// 
			// IBM Tivoli Directory Integrator's method Function.perform declares to
			// throw Exception.
			throw new FunctionExecutionException(x);
		}
	}

	/**
	 * Get the result as a DOM Document.
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlFunctionAdapter#getResultAsDocument()
	 */
	public synchronized Document getResultAsDocument()
			throws FunctionExecutionException {
		Attribute attr = executeResult
				.getAttribute(XmlFunction.FCPARAM_NAME_RESPONSE_TYPE);
		if (attr == null) {
			throw new IllegalStateException();
		}

		String val = attr.getValue();
		if (val == null || val.length() == 0) {
			throw new IllegalStateException();
		}

		attr = executeResult.getAttribute(XmlFunction.FCPARAM_NAME_RESPONSE);
		if (attr == null) {
			throw new IllegalStateException();
		}

		Object obj = attr.getValue(0);
		if (obj instanceof Document) {
			return ((Document) obj);
		} else if (obj instanceof String) {
			try {
				return XmlHelper.parse((String) obj);
			} catch (ParserConfigurationException x) {
				throw new FunctionExecutionException(x);
			} catch (SAXException x) {
				throw new FunctionExecutionException(x);
			} catch (IOException x) {
				throw new FunctionExecutionException(x);
			}
		} else {
			throw new IllegalStateException();
		}

	}

	/**
	 * Get the result as a String.
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlFunctionAdapter#getResultAsString()
	 */
	public synchronized String getResultAsString()
			throws FunctionExecutionException {
		Attribute attr = executeResult
				.getAttribute(XmlFunction.FCPARAM_NAME_RESPONSE_TYPE);
		if (attr == null) {
			throw new IllegalStateException();
		}

		String val = attr.getValue();
		if (val == null || val.length() == 0) {
			throw new IllegalStateException();
		}

		attr = executeResult.getAttribute(XmlFunction.FCPARAM_NAME_RESPONSE);
		if (attr == null) {
			throw new IllegalStateException();
		}

		Object obj = attr.getValue(0);
		if (obj instanceof String) {
			return ((String) obj);
		} else if (obj instanceof Document) {
			try {
				return XmlHelper.serialize((Document) obj);
			} catch (IOException x) {
				throw new FunctionExecutionException(x);
			}
		} else {
			throw new IllegalStateException();
		}
	}

	public synchronized void setCriteria(ExecutionCriteria crita) {
		criteria = crita;
	}

	public synchronized ExecutionCriteria getCriteria() {
		return criteria;
	}

	/*
	 * Dispose of the internal connection to SAP.
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlFunctionAdapter#dispose()
	 */
	public void dispose() throws FunctionExecutionException {
		try {
			fc.terminate();
		} catch (Exception x) {
			throw new FunctionExecutionException(x);
		}
	}

}
