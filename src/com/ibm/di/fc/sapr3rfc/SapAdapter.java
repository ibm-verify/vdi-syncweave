/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import com.sap.mw.jco.JCO;
import com.ibm.di.entry.Entry;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The class represents connectivity to the R/3 system. It defines overloaded
 * methods for sending and receiving RFC invocations and a corresponding
 * JCO.Client proxy for connection establishment.
 */
final class SapAdapter {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public SapAdapter() {
		super();
	}

	/**
	 * Synchronously invoke an RFC and get a response.
	 * 
	 * @param xmlRequest -
	 *            ABAP IFR formatted RFC request
	 * 
	 * @return A DOM document conforming to IFR ABAP serialization for the given
	 *         function.
	 * @throws SapAdapterException
	 */
	public Document sendReceive(SapClientConnection sapClient,
			Document xmlRequest, LogProxy log) throws SapR3RfcFCException {
		StringWriter resultString = new StringWriter();
		PrintWriter pw = new PrintWriter(resultString);
		IfrImporter importer = null;
		int retries = 0;
		JCO.Client conn = null;
		SendReceiveState state = SendReceiveState.INIT;
		IfrRfcFunction func = null;

		// connection can go down at any time during one of these calls
		while (state.compareTo(SendReceiveState.END) != 0) {
			if (log.getDebug()) {
				log.debug(LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0028));
			}

			try {
				if (state.compareTo(SendReceiveState.INIT) == 0) {
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0029));
					}
					conn = sapClient.connect();

					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0030));
					}

					// get the RFC dynamic proxy
					func = IfrFunctionFactory.createFunction(sapClient
							.getRfcRepository());

					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0031));
					}
					// populate the proxy with the request data values
					importer = IfrFunctionFactory.createImporter(xmlRequest,
							log);

					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0032));
					}
					func.importRequestData(importer);
					state = SendReceiveState.EXECUTE;
				}
				if (state.compareTo(SendReceiveState.EXECUTE) == 0) {
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0033,
								new Object[] { getFuncName(importer) }));
					}
					// call the RFC in R/3
					func.execute(conn);
					state = SendReceiveState.END;

					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0034));
					}
					// Export the RFC data values into the response XML string.
					// For efficency we should create a DOM document here.
					IfrSerializer serializer = IfrFunctionFactory
							.createSerializer(pw);
					func.exportResponseData(serializer);
					// parse the response string into the response DOM document.
					pw.flush();
					pw.close();
				}
			} catch (SapRfcFunctionException x) {
				Object[] msgArgs = new Object[] { getFuncName(importer),
						x.getMessage() };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0019, msgArgs);
				state = SendReceiveState.END;
				log.fatal(msg);

				throw new SapR3RfcFCException(
						SapR3RfcFCErrorCodes.RFC_FUNCTION_EXECUTION, msg, x);
			} catch (SapR3RfcFCException x) {
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0020,
						new Object[] { x.getMessage() });
				// Only ever received if there is a communication error during
				// the connect.
				if (retries >= sapClient.maxRetries()) {
					log.fatal(msg);
					throw x;
				} else {
					log.error(msg);
				}
			} catch (JCO.Exception x) {
				if (retryConnection(conn, x)
						&& retries < sapClient.maxRetries()
						&& state.compareTo(SendReceiveState.EXECUTE) != 0) {
					// we can retry only so many times.
					// We get here because the connection is down
					// or could not be established.
					state = SendReceiveState.INIT;

					Object[] msgArgs = new Object[] { getFuncName(importer),
							"" + retries };
					String msg = LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_RFCFC_0021, msgArgs);
					log.error(msg);
				} else {
					// Any other error, don't bother retrying
					state = SendReceiveState.END;
					Object[] msgArgs = new Object[] { getFuncName(importer),
							"" + retries, x.getMessage() };
					String msg = LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_RFCFC_0022, msgArgs);
					log.fatal(msg);
					throw new SapR3RfcFCException(
							SapR3RfcFCErrorCodes.CONNECTION_DROPPED, msg, x);
				}
			} finally {
				if (retries >= sapClient.maxRetries()
						|| state.compareTo(SendReceiveState.END) == 0) {
					state = SendReceiveState.END;
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0035));
					}
					sapClient.disconnect(conn);
				}
			}
			retries++;
		}

		return createDocument(resultString.toString());
	}

	private Document createDocument(String xmlStr) throws SapR3RfcFCException {
		if (xmlStr == null) {
			throw new IllegalArgumentException();
		}

		Document result = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlStr));
			Document doc = db.parse(is);
			result = doc;
		} catch (ParserConfigurationException x) {
			Object[] msgArgs = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0023, msgArgs);
			throw new SapR3RfcFCException(
					SapR3RfcFCErrorCodes.DOM_DOCUMENT_PARSER, msg, x);
		} catch (SAXException x) {
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0024,
					new Object[] { x.getMessage() });
			throw new SapR3RfcFCException(
					SapR3RfcFCErrorCodes.DOM_DOCUMENT_PARSING, msg, x);
		} catch (IOException x) {
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0025,
					new Object[] { x.getMessage() });
			throw new SapR3RfcFCException(
					SapR3RfcFCErrorCodes.DOM_DOCUMENT_READ, msg, x);
		}

		return result;
	}

	/**
	 * Synchronously invoke an RFC and get a response.
	 * 
	 * @param xmlRequest -
	 *            ABAP IFR formatted RFC request
	 * @return A string conforming to IFR ABAP serialization for the given
	 *         function.
	 * @throws SapAdapterException
	 */
	public String sendReceive(SapClientConnection sapClient, Reader xmlRequest,
			LogProxy log) throws SapR3RfcFCException {
		StringWriter result = new StringWriter();
		PrintWriter pw = new PrintWriter(result);
		IfrImporter importer = null;
		SendReceiveState state = SendReceiveState.INIT;
		int retries = 0;
		IfrRfcFunction func = null;
		JCO.Client conn = null;

		// connection can go down at any time during one of these calls
		while (state.compareTo(SendReceiveState.END) != 0) {
			try {
				if (state.compareTo(SendReceiveState.INIT) == 0) {
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0036));
					}
					conn = sapClient.connect();

					// get the RFC dynamic proxy
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0037));
					}
					func = IfrFunctionFactory.createFunction(sapClient
							.getRfcRepository());

					state = SendReceiveState.IMPORT;
				}
				if (state.compareTo(SendReceiveState.IMPORT) == 0) {
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0041));
						// populate the proxy with the request data values
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0038));
					}
					importer = IfrFunctionFactory.createImporter(xmlRequest,
							log);

					func.importRequestData(importer);
					state = SendReceiveState.EXECUTE;
				}
				if (state.compareTo(SendReceiveState.EXECUTE) == 0) {
					// call the RFC in R/3
					func.execute(conn);
					state = SendReceiveState.END;
					// Export the RFC data values into the response XML string.
					IfrSerializer serializer = IfrFunctionFactory
							.createSerializer(pw);
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0039));
					}

					func.exportResponseData(serializer);

					pw.flush();
					pw.close();
				}
			} catch (SapRfcFunctionException x) {
				Object[] msgArgs = new Object[] { getFuncName(importer),
						x.getMessage() };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0019, msgArgs);
				state = SendReceiveState.END;
				log.fatal(msg);

				throw new SapR3RfcFCException(
						SapR3RfcFCErrorCodes.RFC_FUNCTION_EXECUTION, msg, x);
			} catch (SapR3RfcFCException x) {
				Object[] msgArgs = new Object[] { x.getMessage() };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0020, msgArgs);

				// Only ever received if there is a communication error during
				// the connect.

				if (retries >= sapClient.maxRetries()) {
					log.fatal(msg);
					throw x;
				} else {
					log.error(msg);
				}
			} catch (JCO.Exception x) {
				if (retryConnection(conn, x)
						&& retries < sapClient.maxRetries()
						&& state.compareTo(SendReceiveState.EXECUTE) != 0) {
					// we can retry only so many times, but start from the
					// beginning
					state = SendReceiveState.INIT;
					String msg = LogMessageHelper.getMsgResource()
							.getMessage(
									LogMessageHelper.SAPR3_RFCFC_0021,
									new Object[] { getFuncName(importer),
											"" + retries });
					log.error(msg);

				} else {
					// Any other error, don't bother retrying
					state = SendReceiveState.END;
					String msg = LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_RFCFC_0022,
							new Object[] { getFuncName(importer), "" + retries,
									x.getMessage() });
					log.fatal(msg);

					throw new SapR3RfcFCException(
							SapR3RfcFCErrorCodes.CONNECTION_DROPPED, msg, x);
				}
			} catch (IfrImporterException x) {
				Object[] msgArgs = new Object[] { x.getMessage() };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0026, msgArgs);
				log.fatal(msg);
				throw new SapR3RfcFCException(
						SapR3RfcFCErrorCodes.RFC_PARAM_PREPARE, msg, x);
			} finally {
				if (retries >= sapClient.maxRetries()
						|| state.compareTo(SendReceiveState.END) == 0) {
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0037));
					}

					sapClient.disconnect(conn);
					state = SendReceiveState.END;
				}
			}
			retries++;
		}

		return result.toString();
	}

	/**
	 * Synchronously invoke an RFC and get a response.
	 * 
	 * <p>
	 * Entry will processed as a series of nested and multivalued attributes
	 * representing the name and parameters of a given SAP RFC. The names of the
	 * parameters must be encoded according to the rules for ABAP XML
	 * serialization (i.e. names will not have characters that could result in
	 * badly formed XML). An attribute of name SapR3RfcFC.PARAM_INPUT should
	 * have index 0 as a java.lang.String which is the name of the RFC. The
	 * values at index 1 of attribute named SapR3RfcFC.PARAM_INPUT is the the
	 * names of the import, export, and table parameters of the RFC. For simple
	 * ABAP parameter types, an attribute with a single value should be present.
	 * For structure types, an attributue with mulitple values representing the
	 * fields of the struture. For example, if the structure parameter name is
	 * Customer with fields Name and Address, the Attribute syntax should be
	 * <br>
	 * Customer[Name[Mr Smith], Address[3 High Street]] </br> For table
	 * parameters, each row should be represented by an Attribute named
	 * <code>itemN</code>, where N represents the row index. For example, if
	 * the table is named Customers and represents a repeating structure
	 * containing the fields Name and Address, the the Attribute syntax should
	 * be <br>
	 * Customers[item0[Name[Mr Smith], Address[3 High Street]], item1[Name[Mr
	 * Jones], Address[2 Low Street]]] </br>
	 * </p>
	 * 
	 * @param sapClient
	 *            the client that we use to connect to the SAP R/3 server
	 * @param request
	 *            IDI Entry containing
	 * @return An IDI Entry.
	 * @throws SapAdapterException
	 */
	public Entry sendReceive(SapClientConnection sapClient, Entry request,
			LogProxy log) throws SapR3RfcFCException {
		// Do we really need to return a new Entry here ?
		Entry result = new Entry();
		JCO.Client conn = null;
		SendReceiveState state = SendReceiveState.INIT;
		int retries = 0;
		IfrRfcFunction func = null;
		IfrImporter importer = null;

		// connection can go down at any time during one of these calls
		while (state.compareTo(SendReceiveState.END) != 0) {
			try {
				if (state.compareTo(SendReceiveState.INIT) == 0) {
					// connect to the client
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0042));
					}

					conn = sapClient.connect();

					// get the RFC dynamic proxy
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0043));
					}

					func = IfrFunctionFactory.createFunction(sapClient
							.getRfcRepository());

					state = SendReceiveState.IMPORT;
				}
				if (state.compareTo(SendReceiveState.IMPORT) == 0) {
					importer = IfrFunctionFactory.createImporter(request);
					func.importRequestData(importer);
					state = SendReceiveState.EXECUTE;
				}
				if (state.compareTo(SendReceiveState.EXECUTE) == 0) {
					// call the RFC in R/3
					func.execute(conn);
					state = SendReceiveState.END;

					// Export the RFC data values into the response Entry.
					IfrSerializer serializer = IfrFunctionFactory
							.createSerializer(result);
					func.exportResponseData(serializer);
				}
			} catch (SapRfcFunctionException x) {
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0019,
						new Object[] { getFuncName(importer), x.getMessage() });
				state = SendReceiveState.END;
				log.fatal(msg);

				throw new SapR3RfcFCException(
						SapR3RfcFCErrorCodes.RFC_FUNCTION_EXECUTION, msg, x);
			} catch (SapR3RfcFCException x) {
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0020,
						new Object[] { x.getMessage() });

				// Only ever received if there is a communication error during
				// the connect.
				if (retries >= sapClient.maxRetries()) {
					log.fatal(msg);
					throw x;
				} else {
					log.error(msg);
				}
			} catch (JCO.Exception x) {
				// Can get a runtime exception at any time,
				// we will only retry under special circumstances.
				if (retryConnection(conn, x)
						&& retries < sapClient.maxRetries()
						&& state.compareTo(SendReceiveState.EXECUTE) != 0) {

					// Most likely cause is that the connection has gone away,
					// so
					// allow a retry, but start from beginning
					state = SendReceiveState.INIT;
					Object[] msgArgs = new Object[] { getFuncName(importer),
							"" + retries };
					String msg = LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_RFCFC_0021, msgArgs);
					log.error(msg);

				} else {
					// bad news -
					Object[] msgArgs = new Object[] { getFuncName(importer),
							"" + retries, x.getMessage() };
					String msg = LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_RFCFC_0022, msgArgs);
					state = SendReceiveState.END;
					log.fatal(msg);

					// any other error, don't bother retrying
					throw new SapR3RfcFCException(
							SapR3RfcFCErrorCodes.CONNECTION_DROPPED, msg, x);
				}
			} finally {
				if (retries >= sapClient.maxRetries()
						|| state.compareTo(SendReceiveState.END) == 0) {
					state = SendReceiveState.END;
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0044));
					}
					sapClient.disconnect(conn);
				}
			}
			retries++;
		}

		return result;
	}

	private String getFuncName(IfrImporter importer) {
		String result;

		if (importer != null) {
			result = importer.getFunctionName();
		} else {
			result = "?";
		}

		return result;
	}

	private boolean retryConnection(JCO.Client conn, JCO.Exception x) {

		if (x.getGroup() == JCO.Exception.JCO_ERROR_COMMUNICATION) {
			// we can retry only so many times
			return true;
		} else if (conn != null && conn.getState() == JCO.STATE_DISCONNECTED) {
			// most likely cause is that the connection has gone away, so
			// allow a retry, but state from the beginning

			return true;
		}
		return false;
	}
}
