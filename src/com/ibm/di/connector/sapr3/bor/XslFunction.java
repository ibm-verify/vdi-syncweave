/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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
final class XslFunction implements XmlFunctionAdapter {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String SYSTEM_LINE_SEPARATOR_PROP = "line.separator";

	private static final String FCPARAM_NAME_REQUEST_TYPE = "requestType";

	private static final String FCPARAM_NAME_REQUEST = "request";

	private static final String FCPARAM_NAME_RESPONSE_TYPE = "responseType";

	private static final String FCPARAM_NAME_RESPONSE = "response";

	private static final String FCPARAM_VALUE_XML_STRING = "xmlString";

	private static final String FCPARAM_VALUE_DOM_DOC = "xmlDomDocument";

	private final Source xmlSource;

	private final Transformer transformer;

	private final Function fc;

	private final Configuration config;

	private final String xslFileName;

	private Entry executeResult;

	private ExecutionCriteria criteria;

	/**
	 * Create Function adapter.
	 * 
	 * @param inXml
	 *            The input data. Will be transformed in XML to be executed by
	 *            sapFC.
	 * @param xsl
	 *            The style sheet to be applied to inXml.
	 * @param sapFC
	 *            The function component that will execute the result of the
	 *            style sheet.
	 * 
	 * @throws IllegalArgumentException
	 *             is any parameters are null.
	 */
	XslFunction(Document inXml, File xsl, Function sapFC, Configuration cfg)
			throws FunctionExecutionException {
		super();
		if (inXml == null) {
			throw new IllegalArgumentException();
		}
		if (xsl == null) {
			throw new IllegalArgumentException();
		}
		if (sapFC == null) {
			throw new IllegalArgumentException();
		}
		if (cfg == null) {
			throw new IllegalArgumentException();
		}

		config = cfg;
		String s;
		try {
			s = xsl.getCanonicalPath();
		} catch (IOException x) {
			s = xsl.getAbsolutePath();
		}
		xslFileName = s;
		xmlSource = new DOMSource(inXml);
		fc = sapFC;
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			transformer = tf.newTransformer(new StreamSource(xsl));
		} catch (TransformerConfigurationException x) {
			throw new FunctionExecutionException(x);
		}
	}

	/**
	 * Create Function adapter.
	 * 
	 * @param inXml
	 *            The input data. Will be transformed in XML to be executed by
	 *            sapFC.
	 * @param xsl
	 *            The style sheet to be applied to inXml.
	 * @param sapFC
	 *            The function component that will execute the result of the
	 *            style sheet.
	 * 
	 * @throws IllegalArgumentException
	 *             is any parameters are null.
	 */
	XslFunction(String inXml, File xsl, Function sapFC, Configuration cfg)
			throws FunctionExecutionException {
		super();
		if (inXml == null) {
			throw new IllegalArgumentException();
		}
		if (xsl == null) {
			throw new IllegalArgumentException();
		}
		if (sapFC == null) {
			throw new IllegalArgumentException();
		}

		if (cfg == null) {
			throw new IllegalArgumentException();
		}

		config = cfg;
		String s;
		try {
			s = xsl.getCanonicalPath();
		} catch (IOException x) {
			s = xsl.getAbsolutePath();
		}
		xslFileName = s;
		xmlSource = new StreamSource(new StringReader(inXml));
		fc = sapFC;
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			transformer = tf.newTransformer(new StreamSource(xsl));
		} catch (TransformerConfigurationException x) {
			throw new FunctionExecutionException(x);
		}
	}

	/**
	 * Execute the function.
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlFunctionAdapter#execute()
	 */
	public synchronized void execute() throws FunctionExecutionException,
			EmptyTransformResultException {
		try {
			if (getXmlSource() instanceof DOMSource) {
				execute((DOMSource) getXmlSource());
			} else {
				execute((StreamSource) getXmlSource());
			}
		} catch (TransformerException x) {
			throw new FunctionExecutionException(LogMessageHelper
					.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_BOR_0003,
							new Object[] { getXslFileName() }), x);
		}
	}

	private synchronized void setTransformerParameters() {
		if (criteria != null) {
			String[] paramNames = criteria.getParamNames();
			for (int i = 0; i < paramNames.length; ++i) {
				getTransformer().setParameter(paramNames[i],
						criteria.getParam(paramNames[i]));
			}
		}
	}

	private synchronized void execute(DOMSource inXml)
			throws FunctionExecutionException, EmptyTransformResultException,
			TransformerException {
		DOMResult xslResult = new DOMResult();

		setTransformerParameters();
		getTransformer().transform(inXml, xslResult);

		Document docResult = (Document) xslResult.getNode();
		if (docResult == null) {
			throw new EmptyTransformResultException(LogMessageHelper
					.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_BOR_0014));
		}

		if (docResult.getDocumentElement() == null) {
			throw new EmptyTransformResultException(LogMessageHelper
					.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_BOR_0014));
		}

		Entry e = new Entry();
		e.setAttribute(XslFunction.FCPARAM_NAME_REQUEST_TYPE,
				XslFunction.FCPARAM_VALUE_DOM_DOC);
		e.setAttribute(XslFunction.FCPARAM_NAME_REQUEST, xslResult.getNode());
		try {
			executeResult = (Entry) fc.perform(e);
		} catch (Exception x) {
			// 
			// IBM Tivoli Directory Integrator's method Function.perform declares to
			// throw Exception.
			throw new FunctionExecutionException(x);
		}
	}

	private synchronized void execute(StreamSource inXml)
			throws FunctionExecutionException, EmptyTransformResultException,
			TransformerException {
		StreamResult xslResult = new StreamResult(new StringWriter());

		setTransformerParameters();
		getTransformer().transform(inXml, xslResult);

		if (xslResult.getWriter() == null) {
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0014);
			throw new EmptyTransformResultException(msg);
		}

		if (xslResult.getWriter().toString() == null
				|| xslResult.getWriter().toString().length() == 0
				|| xslResult
						.getWriter()
						.toString()
						.equals(
								System
										.getProperty(XslFunction.SYSTEM_LINE_SEPARATOR_PROP))) {
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0014);
			throw new EmptyTransformResultException(msg);
		}

		getConfig().getLog().debug(
				LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0034,
						new Object[] { xslResult.getWriter().toString() }));

		Entry e = new Entry();
		e.setAttribute(XslFunction.FCPARAM_NAME_REQUEST_TYPE,
				XslFunction.FCPARAM_VALUE_XML_STRING);
		e.setAttribute(XslFunction.FCPARAM_NAME_REQUEST, xslResult.getWriter()
				.toString());
		try {
			executeResult = (Entry) fc.perform(e);
		} catch (Exception x) {
			// 
			// IBM Tivoli Directory Integrator's method Function.perform declares to
			// throw Exception.
			throw new FunctionExecutionException(x);
		}

		getConfig().getLog().debug(
				LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0035,
						new Object[] { getResultAsString() }));
	}

	/**
	 * Get the result as a DOM Document.
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XmlFunctionAdapter#getResultAsDocument()
	 */
	public synchronized Document getResultAsDocument()
			throws FunctionExecutionException {
		Attribute attr = executeResult
				.getAttribute(XslFunction.FCPARAM_NAME_RESPONSE_TYPE);
		if (attr == null) {
			throw new IllegalStateException();
		}

		String val = attr.getValue();
		if (val == null || val.length() == 0) {
			throw new IllegalStateException();
		}

		attr = executeResult.getAttribute(XslFunction.FCPARAM_NAME_RESPONSE);
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
				.getAttribute(XslFunction.FCPARAM_NAME_RESPONSE_TYPE);
		if (attr == null) {
			throw new IllegalStateException();
		}

		String val = attr.getValue();
		if (val == null || val.length() == 0) {
			throw new IllegalStateException();
		}

		attr = executeResult.getAttribute(XslFunction.FCPARAM_NAME_RESPONSE);
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

	private Function getFc() {
		return fc;
	}

	private Transformer getTransformer() {
		return transformer;
	}

	private Source getXmlSource() {
		return xmlSource;
	}

	private String getXslFileName() {
		return xslFileName;
	}

	private Configuration getConfig() {
		return config;
	}

}
