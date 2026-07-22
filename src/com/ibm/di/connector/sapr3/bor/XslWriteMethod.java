/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;

import com.ibm.di.fc.Function;

/**
 * This write method updates the data repository by transforming input XML using
 * XSL style sheets. This externalizes control of data repository flow logic.
 * 
 * Concrete impls of this class are required to implement {@link #getXslFiles()}.
 * This is used by the execute methods on this class to obtain the correct list
 * of XSL files to applied to the input XML.
 * 
 */
abstract class XslWriteMethod implements WriteMethod {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Configuration config;

	private List errorMessages;

	private List warningMessages;

	/**
	 * Create a write method instance. This write method updates the data
	 * repository by transforming input XML using XSL style sheets.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>config</code> is <code>null</code>.
	 */
	XslWriteMethod(Configuration config) {
		super();
		errorMessages = Collections.synchronizedList(new ArrayList());
		warningMessages = Collections.synchronizedList(new ArrayList());
		setConfig(config);
	}

	/**
	 * Executes the write operation method. This method iterates through the
	 * list of XSL files as defined by the config for the given write i.e. add,
	 * delete, modify. For each file, it uses an {@link XslFunction} to
	 * transform the <code>inData</code> and use the result to execute an RFC.
	 * 
	 * The list of XSL files is obtained from {@link #getXslFiles()}.
	 * 
	 * @param inXml
	 *            The XML representing the object details to be written.
	 * @param crita
	 *            The optional parameters that control execution behaviour.
	 * 
	 * @throws ConnectorMethodException
	 *             if invocation fails.
	 * @throws IllegalArgumentExcepton
	 *             if any parameter is <code>null</code>;
	 */
	public final void execute(Document inData, ExecutionCriteria crita)
			throws ConnectorMethodException {
		if (inData == null) {
			throw new IllegalArgumentException();
		}

		File[] xslFiles = getXslFiles();
		Function funcComp;
		try {
			funcComp = RfcFunctionFactory.createFC(getConfig());
		} catch (RfcFunctionFactoryException x) {
			throw new ConnectorMethodException(x);
		}
		try {
			for (int i = 0; i < xslFiles.length; ++i) {
				XmlFunctionAdapter func = new XslFunction(inData, xslFiles[i],
						funcComp, getConfig());
				func.setCriteria(crita);
				try {
					func.execute();
					appendErrorAndWarningMessages(func, xslFiles[i]);
				} catch (EmptyTransformResultException x) {
					getConfig().getLog().logwarn(
							LogMessageHelper.getMsgResource().getMessage(
									LogMessageHelper.SAPR3_BOR_0015,
									new Object[] { xslFiles[i]
											.getAbsoluteFile() }));
				}
			}
		} catch (FunctionExecutionException x) {
			throw new ConnectorMethodException(x);
		} finally {
			if (funcComp != null) {
				try {
					funcComp.terminate();
				} catch (Exception x) {
					getConfig().getLog().error("", x);
				}
				funcComp = null;
			}
		}
	}

	/**
	 * Executes the write operation method.
	 * 
	 * @param inXml
	 *            The XML representing the object details to be written.
	 * @param crita
	 *            The optional parameters that control execution behaviour.
	 * 
	 * @throws ConnectorMethodException
	 *             if invocation fails.
	 * @throws IllegalArgumentExcepton
	 *             if any parameter is <code>null</code>;
	 * 
	 * {@see #execute(Document)}.
	 */
	public final void execute(String inXml, ExecutionCriteria crita)
			throws ConnectorMethodException {
		if (inXml == null) {
			throw new IllegalArgumentException();
		}

		File[] xslFiles = getXslFiles();
		Function funcComp;
		try {
			funcComp = RfcFunctionFactory.createFC(getConfig());
		} catch (RfcFunctionFactoryException x) {
			throw new ConnectorMethodException(x);
		}
		try {
			for (int i = 0; i < xslFiles.length; ++i) {
				getConfig().getLog()
						.debug(
								LogMessageHelper.getMsgResource().getMessage(
										LogMessageHelper.SAPR3_BOR_0036,
										new Object[] { xslFiles[i]
												.getAbsolutePath() }));
				XmlFunctionAdapter func = new XslFunction(inXml, xslFiles[i],
						funcComp, getConfig());
				func.setCriteria(crita);
				try {
					func.execute();
					appendErrorAndWarningMessages(func, xslFiles[i]);
				} catch (EmptyTransformResultException x) {
					getConfig().getLog().logwarn(
							LogMessageHelper.getMsgResource().getMessage(
									LogMessageHelper.SAPR3_BOR_0015,
									new Object[] { xslFiles[i]
											.getAbsoluteFile() }));
				}
			}
		} catch (FunctionExecutionException x) {
			throw new ConnectorMethodException(x);
		} finally {
			if (funcComp != null) {
				try {
					funcComp.terminate();
				} catch (Exception x) {
					getConfig().getLog().error("", x);
				}
				funcComp = null;
			}
		}
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

	/**
	 * Overridden method.
	 * 
	 * @see com.ibm.di.connector.sapr3.user.ConnectorMethod#getConfig()
	 */
	public final Configuration getConfig() {
		return config;
	}

	/**
	 * Overridden method.
	 * 
	 * @see com.ibm.di.connector.sapr3.user.ConnectorMethod#setConfig(com.ibm.di.connector.sapr3.user.Configuration)
	 * @throws IllegalArgumentException
	 *             if <code>cfg</code> is <code>null</code>.
	 */
	public final void setConfig(Configuration cfg) {
		if (cfg == null) {
			throw new IllegalArgumentException();
		}

		config = cfg;
	}

	/**
	 * Return the correct list of XSL files to be applied by the
	 * {@link execute(Document)} methods. It is suggested that implementations
	 * obtain the list from the <code>Configuration</code> object,
	 * {@see #getConfig()}.
	 * 
	 * @return The list of XSL files.
	 * @throws ConnectorMethodException
	 *             Most likely caused by invalid configuration.
	 */
	abstract File[] getXslFiles() throws ConnectorMethodException;

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
}
