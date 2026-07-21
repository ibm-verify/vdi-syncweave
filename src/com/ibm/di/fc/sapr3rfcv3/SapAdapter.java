/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import com.sap.conn.jco.*;
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
		SendReceiveState state = SendReceiveState.INIT;
		IfrRfcFunction func = null;

		// connection can go down at any time during one of these calls
		while (state.compareTo(SendReceiveState.END) != 0) {
			if (log.getDebug()) {
				log.debug(LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0028));
			}

			try {
				JCoFunction jcoFunction = null;
				if (state.compareTo(SendReceiveState.INIT) == 0) {
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0029));
					}

					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0030));
					}

					// get the RFC dynamic proxy
					func = IfrFunctionFactory.createFunction(sapClient
							.getRfcRepository());
					func.setDestinationName(sapClient.getDestinationName());

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
					
					JCoDestination destination = JCoDestinationManager.getDestination(sapClient.getDestinationName());
					JCoRepository repository = destination.getRepository();
					JCoFunctionTemplate template = repository.getFunctionTemplate(importer.getFunctionName());
					jcoFunction = template.getFunction();
//					System.out.println("Repository name: " + repository.getName());
//					System.out.println("Function name: " + importer.getFunctionName());
//					System.out.println("Getting SAP function: " + repository.getFunctionTemplate(importer.getFunctionName()).getFunction().toString());
					
					func.importRequestData(importer, jcoFunction);
					state = SendReceiveState.EXECUTE;
				}
				if (state.compareTo(SendReceiveState.EXECUTE) == 0) {
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0033,
								new Object[] { getFuncName(importer) }));
					}
					// call the RFC in R/3
//					System.out.println(" Executing Function ");
					func.execute(jcoFunction);
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
				throw new SapR3RfcFCException(x);
			} catch (JCoException x) {
				state = SendReceiveState.INIT;

				Object[] msgArgs = new Object[] { getFuncName(importer),
						"" + retries, x.getMessage() };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0022, msgArgs);
				log.error(msg);

				if (retries >= sapClient.maxRetries())
					throw new SapR3RfcFCException(
						SapR3RfcFCErrorCodes.CONNECTION_DROPPED, msg, x);
                
			} finally {
				if (retries >= sapClient.maxRetries()
						|| state.compareTo(SendReceiveState.END) == 0) {
					state = SendReceiveState.END;
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0035));
					}
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
	public String sendReceive(SapClientConnection sapClient, String xmlRequest,
			LogProxy log) throws SapR3RfcFCException {
		StringWriter result = new StringWriter();
		PrintWriter pw = new PrintWriter(result);
		IfrImporter importer = null;
		SendReceiveState state = SendReceiveState.INIT;
		int retries = 0;
		IfrRfcFunction func = null;
		JCoFunction jcoFunction = null;
		// connection can go down at any time during one of these calls
		while (state.compareTo(SendReceiveState.END) != 0) {
			try {
				if (state.compareTo(SendReceiveState.INIT) == 0) {
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0036));
					}

					// get the RFC dynamic proxy
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0037));
					}
//					log.info("Repo " + sapClient.getRfcRepository());		
					func = IfrFunctionFactory.createFunction(sapClient
							.getRfcRepository());
					func.setDestinationName(sapClient.getDestinationName());
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
					
					JCoDestination destination = JCoDestinationManager.getDestination(sapClient.getDestinationName());
					JCoRepository repository = destination.getRepository();
					JCoFunctionTemplate template = repository.getFunctionTemplate(importer.getFunctionName());
					jcoFunction = template.getFunction();
//					System.out.println("Repository name: " + repository.getName());
//					System.out.println("Function name: " + importer.getFunctionName());
//					System.out.println("Getting SAP function: " + repository.getFunctionTemplate(importer.getFunctionName()).getFunction().toString());

					func.importRequestData(importer, jcoFunction);
					state = SendReceiveState.EXECUTE;
				}
				if (state.compareTo(SendReceiveState.EXECUTE) == 0) {
					// call the RFC in R/3					
					func.execute(jcoFunction);
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
				throw new SapR3RfcFCException(x);
			} catch (JCoException x) {
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0022,
						new Object[] { getFuncName(importer),
								"" + retries, x.getMessage() });
				log.error(msg);

				// we can retry only so many times, but start from the
				// beginning
				state = SendReceiveState.INIT;
				if (retries >= sapClient.maxRetries())
					throw new SapR3RfcFCException(
						SapR3RfcFCErrorCodes.CONNECTION_DROPPED, msg, x);
			} catch (IfrImporterException x) {
				throw new SapR3RfcFCException(x);
			} finally {
				if (retries >= sapClient.maxRetries()
						|| state.compareTo(SendReceiveState.END) == 0) {
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0040));
					}
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
	 * @param xmlRequest -
	 *            ABAP IFR formatted RFC request
	 * @return A string conforming to IFR ABAP serialization for the given
	 *         function.
	 * @throws SapAdapterException
	 */
	public String sendReceive(SapClientConnection sapClient, File xmlRequest,
			LogProxy log) throws SapR3RfcFCException,IOException {
		StringWriter result = new StringWriter();
		PrintWriter pw = new PrintWriter(result);
		IfrImporter importer = null;
		SendReceiveState state = SendReceiveState.INIT;
		int retries = 0;
		IfrRfcFunction func = null;
		JCoFunction jcoFunction = null;
		// connection can go down at any time during one of these calls
		while (state.compareTo(SendReceiveState.END) != 0) {
			try {
				if (state.compareTo(SendReceiveState.INIT) == 0) {
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0036));
					}

					// get the RFC dynamic proxy
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0037));
					}
//					log.info("Repo " + sapClient.getRfcRepository());		
					func = IfrFunctionFactory.createFunction(sapClient
							.getRfcRepository());
					func.setDestinationName(sapClient.getDestinationName());
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
					Reader inp = new FileReader(xmlRequest);
					try {
						importer = IfrFunctionFactory.createImporter(inp, log);
					} finally {
						inp.close();
					}
					
					JCoDestination destination = JCoDestinationManager.getDestination(sapClient.getDestinationName());
					JCoRepository repository = destination.getRepository();
					JCoFunctionTemplate template = repository.getFunctionTemplate(importer.getFunctionName());
					jcoFunction = template.getFunction();
//					System.out.println("Repository name: " + repository.getName());
//					System.out.println("Function name: " + importer.getFunctionName());
//					System.out.println("Getting SAP function: " + repository.getFunctionTemplate(importer.getFunctionName()).getFunction().toString());

					func.importRequestData(importer, jcoFunction);
					state = SendReceiveState.EXECUTE;
				}
				if (state.compareTo(SendReceiveState.EXECUTE) == 0) {
					// call the RFC in R/3					
					func.execute(jcoFunction);
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
				throw new SapR3RfcFCException(x);
			} catch (JCoException x) {
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0022,
						new Object[] { getFuncName(importer),
								"" + retries, x.getMessage() });
				log.error(msg);

				// we can retry only so many times, but start from the
				// beginning
				state = SendReceiveState.INIT;
				if (retries >= sapClient.maxRetries())
					throw new SapR3RfcFCException(
						SapR3RfcFCErrorCodes.CONNECTION_DROPPED, msg, x);
			} catch (IfrImporterException x) {
				throw new SapR3RfcFCException(x);
			} finally {
				if (retries >= sapClient.maxRetries()
						|| state.compareTo(SendReceiveState.END) == 0) {
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0040));
					}
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
	 * badly formed XML). An attribute of name SapR3RfcFCV3.PARAM_INPUT should
	 * have index 0 as a java.lang.String which is the name of the RFC. The
	 * values at index 1 of attribute named SapR3RfcFCV3.PARAM_INPUT is the the
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
		Entry result = new Entry();
		JCoFunction jcoFunction = null; 
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

					// get the RFC dynamic proxy
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0043));
					}

					func = IfrFunctionFactory.createFunction(sapClient
							.getRfcRepository());
					
					func.setDestinationName(sapClient.getDestinationName());
					state = SendReceiveState.IMPORT;
				}
				if (state.compareTo(SendReceiveState.IMPORT) == 0) {
					importer = IfrFunctionFactory.createImporter(request);
					
					JCoDestination destination = JCoDestinationManager.getDestination(sapClient.getDestinationName());
					JCoRepository repository = destination.getRepository();
					JCoFunctionTemplate template = repository.getFunctionTemplate(importer.getFunctionName());
					jcoFunction = template.getFunction();
//					System.out.println("Repository name: " + repository.getName());
//					System.out.println("Function name: " + importer.getFunctionName());
//					System.out.println("Getting SAP function: " + repository.getFunctionTemplate(importer.getFunctionName()).getFunction().toString());
					
					
					func.importRequestData(importer, jcoFunction);
					state = SendReceiveState.EXECUTE;
				}
				if (state.compareTo(SendReceiveState.EXECUTE) == 0) {
					// call the RFC in R/3
					func.execute(jcoFunction);
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

				throw new SapR3RfcFCException(x);
			} catch (JCoException x) {
				state = SendReceiveState.INIT;
				Object[] msgArgs = new Object[] { getFuncName(importer),
						"" + retries, x.getMessage() };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0022, msgArgs);
				log.error(msg);
				if (retries >= sapClient.maxRetries())
					throw new SapR3RfcFCException(
						SapR3RfcFCErrorCodes.CONNECTION_DROPPED, msg, x);
			} finally {
				if (retries >= sapClient.maxRetries()
						|| state.compareTo(SendReceiveState.END) == 0) {
					state = SendReceiveState.END;
					if (log.getDebug()) {
						log.debug(LogMessageHelper.getMsgResource().getMessage(
								LogMessageHelper.SAPR3_RFCFC_0044));
					}
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
}
