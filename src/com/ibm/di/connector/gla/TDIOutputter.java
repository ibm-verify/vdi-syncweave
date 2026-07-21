/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.gla;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.hyades.logging.adapter.AdapterInvalidConfig;
import org.eclipse.hyades.logging.adapter.IOutputter;
import org.eclipse.hyades.logging.adapter.impl.Outputter;
import org.eclipse.hyades.logging.events.cbe.CommonBaseEvent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.connector.Connector;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * This is an outputter class made to deliver generated CBE objects to a
 * specified connector in IBM Tivoli Directory Integrator.
 * 
 */
public class TDIOutputter extends Outputter implements IOutputter {

	/** Copyright object */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * In this table we register all GLA Connectors from IBM TDI which will
	 * request CBE objects from specified adapter configuration file. The key
	 * which will be assigned to every GLAConnector is set to special property
	 * in the adaptor configuration file and also the same value is specified in
	 * the TDI GLAConnector configuration. If no value is set in the
	 * GLAConnector or TDIOutputter configuration than a default key value will
	 * be used.
	 * 
	 */
	private static Map connectors = new Hashtable();

	/**
	 * This parameter is used later to determine the TDI GLAConnector to which
	 * the CBE objects belong.
	 */
	private String recognizeConnector = null;

	/**
	 * If this is true the TDIOutputter will use the default value for
	 * correlation ID.
	 */
	private static boolean useDefCorrID = false;
	/** Category of the component, used for the {@link ResourceHash} */
	private static final String PROPERTIES_FILE = "glaconnector";
	/** NLS String Property set */
	private static ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * Constructs custom outputter which will be used to send the CBE objects to
	 * the TDI GLAConnector.
	 * 
	 */
	public TDIOutputter() {
	}

	/**
	 * Updates the configuration. This method gets the outputter configuration
	 * and searches through it to find the 'tdi_correlation_id' parameter. If no
	 * such parameter is found than a default value will be set.
	 * 
	 * @throws AdapterInvalidConfig :
	 *             never
	 */
	public void update() throws AdapterInvalidConfig {
		super.update();
		if (useDefCorrID) {
			return;
		}
		Element element = getConfiguration();
		NodeList outputterNodes = element.getChildNodes();
		for (int i = 0; i < outputterNodes.getLength(); i++) {
			if (outputterNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element outputterNode = (Element) outputterNodes.item(i);
				if (outputterNode != null) {
					String pName = outputterNode.getAttribute("propertyName");
					if (pName != null && pName.equals("tdi_correlation_id")) {
						recognizeConnector = outputterNode
								.getAttribute("propertyValue");
						if (recognizeConnector != null
								&& recognizeConnector.trim().length() > 0) {
							recognizeConnector = recognizeConnector.trim();
							useDefCorrID = false;
						} else {
							recognizeConnector = GLAConnector.DEFAULT_CORR_ID;
							useDefCorrID = true;
						}
						break;
					}
				}
			}
		}
	}

	/**
	 * This outputter processes messages represented by an array of
	 * CommonBaseEvent objects where each CommonBaseEvent represents a message.
	 * All CommonBaseEvent objects are sent to the GLAConnector which is
	 * registered to use this instance of the TDIOutputter.
	 * 
	 * @param msgs
	 *            an array of CommonBaseEvent objects representing messages.
	 * @return The same array of CommonBaseEvent objects that was passed in or
	 *         null if the array is not an instance of CommonBaseEvent class or
	 *         is null.
	 * 
	 */
	public Object[] processEventItems(Object[] msgs) {
		if (msgs instanceof CommonBaseEvent[]) {
			GLAConnector myConn = null;

			if (useDefCorrID) {
				myConn = (GLAConnector) connectors
						.get(GLAConnector.DEFAULT_CORR_ID);
			} else {
				myConn = (GLAConnector) connectors.get(recognizeConnector);
			}

			if (myConn == null) {
				// no registered Connector for this name, so inform all
				// GLAConnectors and stop the execution of the outputter.
				GLAConnector.noRegisteredConnector(recognizeConnector);
				throw new Error(sResHash.getString(
						"CONNECTOR.GLA.NO.REGISTERED.CONNECTOR.FOR.NAME",
						recognizeConnector));
			}
			myConn.addEvents(msgs);

			return msgs;
		}

		return null;
	}

	/**
	 * Adds a specified TDI GLAConnector to the Hashtable of the outputter. The
	 * key used is taken from a property in the configuration of the GLA
	 * Connector.
	 * 
	 * @param conn
	 *            the Connector which will be registered.
	 */
	static void addConn(Connector conn) {
		GLAConnector glaConn = (GLAConnector) conn;
		if (glaConn.getCorrelationID().equals(GLAConnector.DEFAULT_CORR_ID)
				|| useDefCorrID) {
			useDefCorrID = true;
			connectors.put(GLAConnector.DEFAULT_CORR_ID, conn);
			return;
		}
		useDefCorrID = false;
		connectors.put(glaConn.getCorrelationID(), conn);
	}

	/**
	 * This method unregisters a TDI GLA Connector. All connectors are kept in a
	 * Hashtable. This method removes the Connector from the Hashtable using its
	 * correlation ID parameter.
	 * 
	 * @param connCorrID
	 *            The correlation ID to which the Connector is registered.
	 */
	static void unregister(String connCorrID) {
		connectors.remove(connCorrID);
	}

}
