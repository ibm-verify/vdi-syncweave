/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.gla;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.hyades.logging.adapter.Adapter;
import org.eclipse.hyades.logging.adapter.AdapterException;
import org.eclipse.hyades.logging.events.cbe.CommonBaseEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.cbe.CBEGeneratorFC;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * This is the GLAConnector class. GLAConnector is used to process a log file
 * and transform its contents to CommonBaseEvent objects. The Connector needs an
 * adapter configuration file to be provided as a Connector parameter. The
 * Connector than uses the GLA run-time to validate and start the given
 * configuration file. The Connector uses a specially made outputter, called
 * TDIOutputter, to receive the generated from the log file CBE objects. The
 * TDIOutputter must be configured to the adapter configuration file. For more
 * information about configuring the Connector and the TDIOutputter refer to the
 * GLAConnector documentation.
 * 
 */
public class GLAConnector extends Connector implements ConnectorInterface {

	/** Copyright object */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/** This Queue holds the CBE objects */
	private ArrayBlockingQueue elements = null;

	/** This is the path to the configuration file */
	private String configFile = null;

	/**
	 * The correlation ID of the connector used by the TDIOutputter to recognize
	 * the GLAConnector to which the CBE objects must be supplied.
	 */
	private String corrID = null;

	/** The current CommonBaseEvent object */
	private CommonBaseEvent currentCBE = null;

	/**
	 * The default correlation ID is used from both the GLAConnector and the
	 * TDIOutputter. It is used if one (or both) of the Connector correlationID
	 * or the Outputter tdi_correlation_id parameter is not set.
	 */
	public static final String DEFAULT_CORR_ID = "default_correlation_id";
	/** Category of the component, used for the {@link ResourceHash} */
	private static final String PROPERTIES_FILE = "glaconnector";
	/** NLS String Property set */
	private static ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * This Exception is thrown when the TDIOutputter does not find GLAConnector
	 * with the same correlation ID.
	 */
	private static Exception noConnException = null;

	/**
	 * The GLAConnector constructor. It sets the mode of the connector. The only
	 * supported mode is Iterator.
	 * 
	 */
	public GLAConnector() {
		super();
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE });
	}

	/**
	 * This method initializes the connector parameters. It gets the
	 * "configFile" parameter and checks if it has any assigned value. If no
	 * value is assigned then an Exception is thrown. Then the "correlationID"
	 * of the Connector is taken. If no value is specified to this parameter
	 * than automatically a default value is used.
	 * 
	 * @param obj
	 *            not used.
	 * @throws Exception
	 *             An Exception is thrown if some of the requred parameters is
	 *             missing.
	 */
	public void initialize(Object obj) throws Exception {
		elements = new ArrayBlockingQueue(1000);

		configFile = getParam("configFile");
		if (configFile == null || configFile.length() == 0) {
			throw new Exception(sResHash
					.getString("CONNECTOR.GLA.MISSING.CONFIG.FILE"));

		}

		configFile = configFile.trim();

		corrID = getID();
		if (corrID == null || corrID.trim().length() == 0) {
			corrID = DEFAULT_CORR_ID;
		}

	}

	/**
	 * This method starts the Adapter and fills the queue with CBE objects.
	 * Before starting the Adapter a validation is made to ensure that the
	 * configuration adapter file is valid.
	 * 
	 * @throws Exception
	 *             If the configuration adapter file is not valid or an error
	 *             occurs during execution.
	 */
	public void selectEntries() throws Exception {
		final Adapter adapter = new Adapter();// creating adapter

		/* Setting the adapter configuration file */
		adapter.setContextConfigPath(configFile);
		adapter.setComponentConfigPath(configFile);

		System.setProperty("GLA_HOME", System
				.getProperty("com.ibm.di.installdir")
				+ "/xsd/gla");

		/* Validate the adapter configuration file */
		try {
			adapter.validate();
		} catch (AdapterException e) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.GLA.CONFIG.FILE.NOT.VALID", e.getMessage()));
		}
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.GLA.CONFIG.FILE.VALIDATED"));
		}

		add(); // register the connector to the TDIOutputter

		/* Start the adapter in separate thread */
		Thread t = new Thread() {
			public void run() {
				try {
					adapter.start(false, false);
					if (noConnException != null) {
						elements.put("command.error.noconn");
						return;
					}
					elements.put("command.finished"); // at this point the
					// adapter has
					// finished its execution
				} catch (Exception exc) {
					adapter.stop();
					try {
						elements.put("command.error" + exc.getMessage());
					} catch (InterruptedException ie) {
					}
				}
			}
		};

		t.start();
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.GLA.CONFIG.FILE.STARTED"));
		}

	}

	/**
	 * This method gets the next CBE object from the queue and creates an Entry.
	 * To the Entry is set an attribute with name "rawCBEObject" and value the
	 * CBE object. Also to the same Entry object are added the CommonBaseEvent
	 * properties as (name,value) pair. This Entry is passed to the
	 * AssemblyLine.
	 * 
	 * @return Entry object containing the next CBE object.
	 * @throws Exception if an error occurs.
	 */
	public Entry getNextEntry() throws Exception {
		Entry result = new Entry();

		Object obj = null;
		try {
			obj = elements.take();
		} catch (InterruptedException ie) {
		}

		if (obj instanceof String) {
			String str = (String) obj;
			if (str.startsWith("command.error.start")) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.GLA.ADAPTER.FAIL.TO.START", str
								.substring(19)));
			} else {
				if (str.startsWith("command.error.noconn")) {
					throw noConnException;
				}
			}
			return null;
		}

		CommonBaseEvent cbe = (CommonBaseEvent) obj;

		currentCBE = cbe;

		result.addAttributeValue("$rawCBE", cbe);
		CBEGeneratorFC.mapCbeToEntry(cbe, result);

		return result;
	}

	/**
	 * This method adds the current connector to the Hashtable of the
	 * TDIOutputter.
	 * 
	 */
	private void add() {
		TDIOutputter.addConn(this);
	}

	/**
	 * This method is used by the TDIOutputter to add the next array of CBE
	 * objects in the queue. If there is no available space in the queue the
	 * method blocks until additional space is available.
	 * 
	 * @param newElements
	 *            Array of CBE objects which are added to the queue.
	 */
	void addEvents(Object[] newElements) {
		for (int i = 0; i < newElements.length; i++) {
			try {
				if (newElements[i] != null) {
					elements.put(newElements[i]);
				}
			} catch (InterruptedException ie) {
			}
		}
	}

	/**
	 * This method creates an Exception if the TDIOutputter does not find any
	 * registered connector in its Hashtable corresponding to the value of the
	 * "tdi_correlation_id" property. The Exception is created when there are
	 * some events available in the outputter.
	 * 
	 * @param connName
	 *            This is the name which is missing from the outputter's
	 *            Hashtable.
	 */
	static void noRegisteredConnector(String connName) {
		noConnException = new Exception(sResHash.getString(
				"CONNECTOR.GLA.NO.REGISTERED.CONNECTOR.FOR.NAME", connName));
	}

	/**
	 * Retrieves the correlation ID of the connector. The correlation ID is used
	 * by the TDIOutputter to recognize the connector to which the CBE objects
	 * must be delivered.
	 * 
	 * @return String representing the connector correlation ID.
	 */
	String getCorrelationID() {
		return corrID;
	}

	/**
	 * Returns the connector version.
	 * 
	 * @return String representing the connector version.
	 */
	public String getVersion() {
		return "1.0-di7.1.1  %I% 20%E%";
	}

	/**
	 * Terminates the Connector and unregisters it from the TDIOutputter's
	 * Hashtable.
	 * 
	 * @throws Exception :
	 *             never
	 */
	public void terminate() throws Exception {
		TDIOutputter.unregister(corrID);
	}

	/**
	 * This method gets the "tdi_correlation_id" parameter from the
	 * configuration of the TDIOutputter and sets it to the GLAConnector
	 * configuration.
	 * 
	 * @return A String representation of the correlation ID parameter.
	 * @throws Exception
	 *             If in the adapter configuration file more than one
	 *             TDIOutputters are configured.
	 */
	private String getID() throws Exception {
		Document doc = parseXMLFile(configFile);

		/* Constructs the xPath to the Component containing the TDIOutputter */
		String xpath = "/*//Component[@executableClass = 'com.ibm.di.connector.gla.TDIOutputter']";
		NodeList nodelist = org.apache.xpath.XPathAPI
				.selectNodeList(doc, xpath);

		// if no TDIOutputter is configured than throw an Exception.
		if (nodelist.getLength() == 0) {
			throw new Exception(sResHash
					.getString("CONNECTOR.GLA.NO.CONFIGURED.OUTPUTTER"));
		}

		// we do not allow more than one TDIOutputter in the adapter
		// configuration
		// file.
		if (nodelist.getLength() > 1) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.GLA.MORE.THAN.ONE.CONFIGURED.OUTPUTTERS"));
		}
		String uniqueID = null;
		for (int i = 0; i < nodelist.getLength(); i++) {
			Element el = (Element) nodelist.item(i);
			// get the ID to recognize the TDIOutputter later.
			uniqueID = el.getAttribute("uniqueID");
		}

		/*
		 * Constructs the xPath to the "tdi_correlation_id" parameter of the
		 * TDIOutputter found earlier.
		 */
		xpath = "//Outputter[@uniqueID = '" + uniqueID
				+ "']/*[@propertyName = 'tdi_correlation_id']";

		nodelist = org.apache.xpath.XPathAPI.selectNodeList(doc, xpath);

		for (int i = 0; i < nodelist.getLength(); i++) {
			Node currNode = nodelist.item(i);
			if ((currNode != null)
					&& (currNode.getNodeType() == Node.ELEMENT_NODE)) {
				Element elem = (Element) currNode;
				if (elem != null) {
					String pName = elem.getAttribute("propertyName");
					if (pName != null && pName.equals("tdi_correlation_id")) {
						return elem.getAttribute("propertyValue");
					}
				}
			}
		}

		return null;
	}

	/**
	 * This method returns Document from a XML file.
	 * 
	 * @param filename
	 *            the path to the file which will be transformed to Document
	 *            object.
	 * @return org.w3c.dom.Document object which is generated from the given XML
	 *         file.
	 * @throws Exception :
	 *             Possible Exceptions are SAXException,
	 *             ParserConfigurationException, IOException
	 */
	private static Document parseXMLFile(String filename) throws Exception {
		try {
			// Create a builder factory
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			// Create the builder and parse the file
			Document doc = factory.newDocumentBuilder().parse(
					new File(filename));
			return doc;

		} catch (Exception e) {
			// Possible Exceptions are SAXException,
			// ParserConfigurationException,
			// IOException
			throw new Exception(sResHash.getString(
					"CONNECTOR.GLA.ERROR.PARSING.CONFIG.FILE", e.toString()));
		}
	}

	/**
	 * Returns the last taken CBE object. Users may use this method to get the
	 * current CBE object.
	 * 
	 * @return CBE object which is last taken from the Queue.
	 */
	public CommonBaseEvent getCurrentCBEObject() {
		return currentCBE;
	}
}
