package com.ibm.di.connector.axis2;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.config.base.ConnectorConfigImpl;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.RS;
import com.ibm.di.server.ReconnectRuleEngine;
import com.ibm.di.test.utils.NOOPLog;
import com.ibm.di.test.utils.RSMock;
import com.ibm.di.util.FileUtils;

public class WSServerTestSetup {
	
	private String serviceHost = "http://localhost";
	private int servicePort = 80;
	private String wsdlFilePath = null;
	private String serviceName = null;
	private String operationName = null;
	private String requestSOAP = null;
	private Entry responseEntry = new Entry();
	private String username = null;
	private String password = null;
	private boolean useSSL = false;
	private boolean useHTTPBasicAuth = false;
	private String requestContentType = "text/xml;charset=utf-8";
	
	private String soapResponse = null;
	private int httpResponseCode = 0;
	private Entry requestEntry = null;
	
	public void setServiceLocation(String host, int port) {
		this.serviceHost = host;
		this.servicePort = port;
	}
	
	public void setWSDLFilePath(String wsdlFilePath) {
		this.wsdlFilePath = wsdlFilePath;
	}
	
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}
	
	public void setCredentials(String username, String password) {
		this.username = username;
		this.password = password;
		this.useHTTPBasicAuth = (username != null);

	}
	
	public void setUseHTTPBasicAuth(boolean useAuth) {
		this.useHTTPBasicAuth = useAuth;
	}
	 
	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}
	
	public void loadResponsePayloadFromFile(String responseFilePath) throws Exception {
		Entry payloadEntry = createEntryFromXMLFile(responseFilePath);
		responseEntry.merge(payloadEntry);
	}
	
	public void loadResponseHeaderFromFile(String headerFilePath) throws Exception {
			
		Element elem = SOAPUtils.parseFile(headerFilePath);
		Element tdiElem = SOAPUtils.convertDOM(elem, responseEntry);
		
		Attribute headerAttr = responseEntry.newAttribute(Axis2WSServerConnector.ATTR_SOAP_HEADER);
		headerAttr.appendChild(tdiElem);
	}
	
	public void loadRequestSOAPFromFile(String requestFilePath) throws Exception {
		this.requestSOAP = FileUtils.loadFile(requestFilePath);
	}
	
	public void setRequestContentType(String requestContentType) {
		this.requestContentType = requestContentType;
	}
	
	/**
	 * Run a simple exchange of request response with the Connector.
	 */
	public void execute() throws Exception {
		ConnectorInterface serverConnector = createAxis2WSServerConnector();

		SimpleRequestHandler requestHandler = new SimpleRequestHandler(responseEntry);
		ServerModeConnectorTestDriver testDriver = new ServerModeConnectorTestDriver(serverConnector, requestHandler);
		testDriver.initialize();
		
		Thread serverThread = new Thread(testDriver);
		serverThread.start();

		SOAPCall soapCall;
		if (useHTTPBasicAuth) {
			soapCall = SOAPCall.call(getServiceURL(), requestSOAP, requestContentType, username, password);
		} else {
			soapCall = SOAPCall.call(getServiceURL(), requestSOAP, requestContentType);
		}
		testDriver.close();
		
		assertNull(testDriver.getExitError());
		
		soapResponse = soapCall.getBody();
		httpResponseCode = soapCall.getResponseCode();
		requestEntry = requestHandler.getRequestEntry();
	}
	
	
	private ConnectorInterface createAxis2WSServerConnector() throws Exception {
		ConnectorInterface conn = new Axis2WSServerConnector();
		ConnectorConfig cc = new ConnectorConfigImpl();
		cc.init();
		cc.setState(ConnectorConfig.ENABLED_STATE);
		cc.setMode(ConnectorConfig.SERVER_MODE);
		cc.getConnectionConfig().setJavaClass(Axis2WSServerConnector.class.getName());
		cc.getConnectionConfig().setParameter("wsdlUrl", wsdlFilePath);
		cc.getConnectionConfig().setParameter("tcpPort", "" + servicePort);
		cc.getConnectionConfig().setParameter("httpAuth", ""+useHTTPBasicAuth);
		cc.getConnectionConfig().setParameter("useSSL", ""+useSSL);
		if (serviceName != null && serviceName.trim().length() > 0) {
			cc.getConnectionConfig().setParameter("service", serviceName);
		}
		conn.setConfiguration(cc);
		conn.setLog(new Log(""));
		conn.setRSInterface(new RSMock());
		return conn;
	}

	private static Entry createEntryFromXMLFile(String xmlFilePath) throws Exception {
		Element elem = SOAPUtils.parseFile(xmlFilePath);
		Entry e = new Entry();
		e.appendChild(SOAPUtils.convertDOM(elem, e));
		return e;
	}

	private static Element getAttributeByLocalName(Entry entry, String localName) {
		Element attr = null;
		NodeList childNodes = entry.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); ++i) {
			Node child = childNodes.item(i);
			if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && localName.equalsIgnoreCase(child.getLocalName())) {
				attr = (Element) child;
				break;
			}
		}
		return attr;
	}
	
	private String getServiceURL() {
		String protocol = useSSL ? "https" : "http";
		return protocol + "://" + serviceHost + ":" + servicePort;
	}

	/**
	 * Validate the request Entry, which the Connector has parsed out of the incoming HTTP message.
	 * Works only for document style operations.
	 */
	public void validateDocStyleRequest() throws Exception {

		Element requestElem = getRequestPayloadElement();
		
		try {
			SOAPUtils.validateDocStyleSOAPRequestPayload(requestElem, wsdlFilePath, operationName);
		} catch (Exception ex) {
			throw new Exception("The 'conn' Entry, which the Connector returned does not match the actual request. The 'conn' Entry contains: "+SOAPUtils.convertDOMToString(requestElem), ex);
		}
	}

	/**
	 * Validate the response SOAP message, which the Connector has sent to the test HTTP client.
	 * Works only for document style operations.
	 */
	public void validateDocStyleResponse() throws Exception {
		assertEquals(200, httpResponseCode);

		Element soapResponseElem = getSOAPResponseElement();
		
		SOAPUtils.validateDocStyleSOAPResponse(soapResponseElem, wsdlFilePath, operationName);
	}
	
	public Element getSOAPResponseElement() throws Exception {
		Document doc = SOAPUtils.getDOMParser().newDocument();
		Element soapResponseElem = SOAPUtils.convertDOM(SOAPUtils.parseString(soapResponse), doc);
		doc.appendChild(soapResponseElem);
		return soapResponseElem;
	}
	
	public String getSOAPResponseString() throws Exception {
		return soapResponse;
	}
	
	public Element getRequestPayloadElement() throws Exception {
		
		String requestAttrName = SOAPUtils.getMessageElementName(wsdlFilePath, operationName, true);
		Element requestAttr = getAttributeByLocalName(requestEntry, requestAttrName);
		
		if (requestAttr == null) {
			throw new Exception("Could not find attribute with localName '"+requestAttrName+"' in the 'conn' Entry. The 'conn' Entry is:"+requestEntry.toString());
		}

		Document doc = SOAPUtils.getDOMParser().newDocument();
		Element requestElem = SOAPUtils.convertDOM(requestAttr, doc);
		doc.appendChild(requestElem);
		return requestElem;
	}
	
	public int getHTTPResponseCode() {
		return httpResponseCode;
	}
	
	public Entry getRequestEntry() {
		return requestEntry;
	}
	
	public Entry getResponseEntry() {
		return responseEntry;
	}
		
}
