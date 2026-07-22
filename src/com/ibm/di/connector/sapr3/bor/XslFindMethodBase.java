/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.Result;
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

import com.ibm.di.fc.Function;

/**
 * Implementation of the FindMethod interface. This class requires XSL style
 * sheets to be configured that produce RFC XML requests and XML formatted
 * responses.
 * 
 */
abstract class XslFindMethodBase implements FindMethod {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String SYSTEM_LINE_SEPARATOR_PROP = "line.separator";

	private static final String DUMMY_XML = "<sapDummyXml />";

	private Configuration config;

	private List errorMessages;

	private List warningMessages;

	/**
	 * Create a XSL driven find method.
	 * 
	 * @param cfg
	 *            The configuration information.
	 * @throws IllegalArgumentException
	 *             id <code>cfg</code> equals <code>null</code>.
	 */
	XslFindMethodBase(Configuration cfg) {
		super();

		if (cfg == null) {
			throw new IllegalArgumentException();
		}

		config = cfg;
		errorMessages = Collections.synchronizedList(new ArrayList());
		warningMessages = Collections.synchronizedList(new ArrayList());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.FindMethod#execute(org.w3c.dom.Document,
	 *      com.ibm.di.connector.sapr3.user.ExecutionCriteria,
	 *      com.ibm.di.connector.sapr3.user.FindMethod.ResponseFormat)
	 */
	public final Object execute(Document inXml, ExecutionCriteria filter,
			ResponseFormat format) throws ConnectorMethodException,
			EmptyTransformResultException {
		if (inXml == null) {
			throw new IllegalArgumentException();
		}
		if (filter == null) {
			throw new IllegalArgumentException();
		}
		if (format == null) {
			throw new IllegalArgumentException();
		}
		if (format != FindMethod.XML_DOM_DOC && format != FindMethod.XML_STRING) {
			throw new IllegalArgumentException();
		}

		XmlFunctionAdapter func = null;
		try {
			func = executeFunction(inXml, filter);
			Source funcRespXml = new DOMSource(func.getResultAsDocument());
			Result result = createTransformResult(format);

			transform(funcRespXml, result, getPostCallXsl());
			checkEmptyTransformResult(result);

			if (format == FindMethod.XML_DOM_DOC) {
				DOMResult r = (DOMResult) result;
				return r.getNode();
			} else {
				StreamResult r = (StreamResult) result;
				StringWriter swr = (StringWriter) r.getWriter();
				return swr.toString();
			}

		} catch (RfcFunctionFactoryException x) {
			throw new ConnectorMethodException(x);
		} catch (FunctionExecutionException x) {
			throw new ConnectorMethodException(x);
		} catch (TransformerConfigurationException x) {
			throw new ConnectorMethodException(x);
		} catch (TransformerException x) {
			throw new ConnectorMethodException(x);
		} finally {
			if (func != null) {
				try {
					func.dispose();
				} catch (FunctionExecutionException x) {
					getConfig().getLog().error("", x);
				} finally {
					func = null;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.FindMethod#execute(java.lang.String,
	 *      com.ibm.di.connector.sapr3.user.ExecutionCriteria,
	 *      com.ibm.di.connector.sapr3.user.FindMethod.ResponseFormat)
	 */
	public final Object execute(String inXml, ExecutionCriteria filter,
			ResponseFormat format) throws ConnectorMethodException,
			EmptyTransformResultException {
		if (inXml == null) {
			throw new IllegalArgumentException();
		}
		if (filter == null) {
			throw new IllegalArgumentException();
		}
		if (format == null) {
			throw new IllegalArgumentException();
		}
		if (format != FindMethod.XML_DOM_DOC && format != FindMethod.XML_STRING) {
			throw new IllegalArgumentException();
		}

		XmlFunctionAdapter func = null;
		try {
			func = executeFunction(inXml, filter);
			StringReader sr = new StringReader(func.getResultAsString());
			Source funcRespXml = new StreamSource(sr);
			Result result = createTransformResult(format);

			transform(funcRespXml, result, getPostCallXsl());
			checkEmptyTransformResult(result);

			if (format == FindMethod.XML_DOM_DOC) {
				DOMResult r = (DOMResult) result;
				return r.getNode();
			} else {
				StreamResult r = (StreamResult) result;
				StringWriter swr = (StringWriter) r.getWriter();
				return swr.toString();
			}

		} catch (RfcFunctionFactoryException x) {
			throw new ConnectorMethodException(x);
		} catch (FunctionExecutionException x) {
			throw new ConnectorMethodException(x);
		} catch (TransformerConfigurationException x) {
			throw new ConnectorMethodException(x);
		} catch (TransformerException x) {
			throw new ConnectorMethodException(x);
		} finally {
			if (func != null) {
				try {
					func.dispose();
				} catch (FunctionExecutionException x) {
					getConfig().getLog().error("", x);
				} finally {
					func = null;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.FindMethod#execute(com.ibm.di.connector.sapr3.user.ExecutionCriteria,
	 *      com.ibm.di.connector.sapr3.user.FindMethod.ResponseFormat)
	 */
	public Object execute(ExecutionCriteria filter, ResponseFormat format)
			throws ConnectorMethodException, EmptyTransformResultException {
		return execute(XslFindMethodBase.DUMMY_XML, filter, format);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.ConnectorMethod#getConfig()
	 */
	public final Configuration getConfig() {
		return config;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.ConnectorMethod#setConfig(com.ibm.di.connector.sapr3.user.Configuration)
	 */
	public final void setConfig(Configuration cfg) {
		if (config == null) {
			throw new IllegalArgumentException();
		}

		config = cfg;
	}

	/**
	 * Determine if execution detected any R/3 ABAP application level errors.
	 * 
	 * @return <code>true</code> if ABAP errors occurred during execution,
	 *         <code>false</code> otherwise.
	 */
	public final boolean hasAbapErrors() {
		return (errorMessages.size() > 0);
	}

	/**
	 * Determine if execution detected any R/3 ABAP application level warnings.
	 * 
	 * @return <code>true</code> if ABAP warnings occurred during execution,
	 *         <code>false</code> otherwise.
	 */
	public final boolean hasAbapWarnings() {
		return (warningMessages.size() > 0);
	}

	/**
	 * Get all error messages returned during the execution of the method.
	 * 
	 * @return A list of AbapErrorInfo objects that occured as a result of
	 *         executing R/3 ABAP code. The length will be zero if not errors
	 *         occurred.
	 */
	public final List getAbapErrors() {
		return errorMessages;
	}

	/**
	 * Get all warning messages returned during the execution of the method.
	 * 
	 * @return A list of AbapErrorInfo objects that occured as a result of
	 *         executing R/3 ABAP code. The length will be zero if not errors
	 *         occurred.
	 */
	public final List getAbapWarnings() {
		return warningMessages;
	}

	private void appendErrorAndWarningMessages(XmlFunctionAdapter func,
			File xslFile) {
		XmlBapiFunctionAdapter bapiFunc = new XmlBapiFunctionAdapterImpl(func,
				getConfig());
		AbapErrorInfo[] msgs = bapiFunc.getWarningMessages();
		for (int i = 0; i < msgs.length; ++i) {
			msgs[i].setInternalSource(xslFile.getAbsolutePath());

			if (msgs[i].isError()) {
				errorMessages.add(msgs[i]);
			} else if (msgs[i].isWarn()) {
				warningMessages.add(msgs[i]);
			}
		}

		msgs = bapiFunc.getErrorMessages();
		for (int i = 0; i < msgs.length; ++i) {
			msgs[i].setInternalSource(xslFile.getAbsolutePath());

			if (msgs[i].isError()) {
				errorMessages.add(msgs[i]);
			} else if (msgs[i].isWarn()) {
				warningMessages.add(msgs[i]);
			}
		}
	}

	private void transform(Source inXml, Result outXml, File xslFile)
			throws RfcFunctionFactoryException,
			TransformerConfigurationException, TransformerException {
		StreamSource xsl = new StreamSource(xslFile);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer(xsl);
		t.transform(inXml, outXml);
	}

	private void checkEmptyTransformResult(Result outXml)
			throws EmptyTransformResultException {
		if (outXml instanceof DOMResult) {
			Document docResult = (Document) ((DOMResult) outXml).getNode();
			if (docResult == null) {
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0014);
				throw new EmptyTransformResultException(msg);
			}

			if (docResult.getDocumentElement() == null) {
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0014);
				throw new EmptyTransformResultException(msg);
			}
		} else if (outXml instanceof StreamResult) {
			StreamResult strXml = (StreamResult) outXml;
			if (strXml.getWriter().toString() == null
					|| strXml.getWriter().toString().length() == 0
					|| strXml
							.getWriter()
							.toString()
							.equals(
									System
											.getProperty(XslFindMethodBase.SYSTEM_LINE_SEPARATOR_PROP))) {
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_BOR_0014);
				throw new EmptyTransformResultException(msg);
			}
		}
	}

	private XmlFunctionAdapter executeFunction(String inXml,
			ExecutionCriteria filter) throws ConnectorMethodException {
		try {
			Function sapFunc = RfcFunctionFactory.createFC(getConfig());
			XmlFunctionAdapter func = new XslFunction(inXml, getPreCallXsl(),
					sapFunc, getConfig());
			func.setCriteria(filter);
			func.execute();
			appendErrorAndWarningMessages(func, getPreCallXsl());
			return func;
		} catch (FunctionExecutionException x) {
			throw new ConnectorMethodException(x);
		} catch (RfcFunctionFactoryException x) {
			throw new ConnectorMethodException(x);
		} catch (EmptyTransformResultException x) {
			throw new ConnectorMethodException(x);
		}
	}

	private XmlFunctionAdapter executeFunction(Document inXml,
			ExecutionCriteria filter) throws ConnectorMethodException {
		try {
			Function sapFunc = RfcFunctionFactory.createFC(getConfig());
			XmlFunctionAdapter func = new XslFunction(inXml, getPreCallXsl(),
					sapFunc, getConfig());
			func.setCriteria(filter);
			func.execute();
			appendErrorAndWarningMessages(func, getPreCallXsl());
			return func;
		} catch (FunctionExecutionException x) {
			throw new ConnectorMethodException(x);
		} catch (RfcFunctionFactoryException x) {
			throw new ConnectorMethodException(x);
		} catch (EmptyTransformResultException x) {
			throw new ConnectorMethodException(x);
		}
	}

	private Result createTransformResult(FindMethod.ResponseFormat format) {
		if (format == FindMethod.XML_DOM_DOC) {
			return new DOMResult();
		} else {
			return new StreamResult(new StringWriter());
		}
	}
}
