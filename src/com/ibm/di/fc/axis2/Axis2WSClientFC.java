/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.axis2;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.SSLSession;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.transport.http.impl.httpclient4.HttpTransportPropertiesImpl;
import org.apache.axis2.wsdl.WSDLConstants;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.w3c.dom.NodeList;

import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.connector.axis2.Axis2WSServerConnector;
import com.ibm.di.connector.axis2.util.SchemaUtils;
import com.ibm.di.connector.axis2.util.WSUtils;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.ResourceHash;

/**
 * This is a WebService client based on the Axis2 library. For more information
 * see the on-line documentation of the Axis2 WebService Client Function
 * Component.
 * 
 * @since 7.0
 */
public class Axis2WSClientFC extends Function {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/** Path to the WSDL file */
	private String wsdl;

	/** The Service which will be invoked */
	private AxisService service;

	/** The Endpoint to the web service server */
	private AxisEndpoint endpoint;

	/** The SOAP Operation which will be invoked */
	private AxisOperation operation;

	/** The base for the client which invokes the server */
	private ServiceClient sender;

	/** The SOAP Factory for the SOAP Envelopes */
	private SOAPFactory factory;

	/** This the QName of the request element */
	private QName element;

	/** Indicates whether we have a one way operation */
	private boolean outonly = false;

	/** The TMS properties file of the Axis2WSClientFC */
	private static final String PROPERTIES_FILE = "axis2wsclientfc";

	/** The resource manager for the TMS Messages */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);
	
	/** Http authentication User name */
	String httpAuthUserName = null;	
	
	/** Http Authentication Password */
	String httpAuthPassword = null;
	
	/** Axis2 timeout default value */
	long timeout = 60000;

	/** Fault from last perform call */
	AxisFault axisFault;

	/** Remember if we have registered MySSLProtocolSocketFactory */
	private static boolean registeredMyFactory;
	
	/**
	 * Returns the Username set for http authentication using the method setHttpAuthUserName
	 * @return Username 
	 * @since 7.1 FP4. 
	 */
	public String getHttpAuthUserName() {
		return httpAuthUserName != null ? httpAuthUserName : (String) getParam("wsdlAuthUser");
	}

	/**
	 * Sets the username for http authenticaton when WSDL is http auth protected. 
	 * @param httpAuthUserName
	 * @since 7.1 FP4. 
	 */
	public void setHttpAuthUserName(String httpAuthUserName) {
		this.httpAuthUserName = httpAuthUserName;
	}
	
	/**
	 * 
	 * @return
	 */	
	public String getHttpAuthPassword() {
		return httpAuthPassword != null ? httpAuthPassword : (String) getParam("wsdlAuthPass");
	}
	
	/**
	 * Sets the password for http authenticaton when WSDL is http auth  protected. 
	 * @param httpAuthPassword
	 * @since 7.1 FP4. 
	 */
	public void setHttpAuthPassword(String httpAuthPassword) {
		this.httpAuthPassword = httpAuthPassword;
	}

	/**
	 * Initializes the invocation process. Creates the client which will be used
	 * as a basis for the invocation. During the initialization all mandatory
	 * fields are gathered and the internal objects dependent from them are
	 * created. The mandatory fields are: WSDL URL, Service, Endpoint and
	 * Operation.
	 * 
	 * @param initParams
	 *            not used.
	 * @exception Exception
	 *                If a mandatory field value is missing or if exception
	 *                occurs during the internal objects initialization.
	 */
	public void initialize(Object initParams) throws Exception {

		wsdl = (String) getParam("wsdlUrl");
		if (wsdl == null || wsdl.trim().length() == 0) {
			throw new Exception(resHash
					.getString("Axis2.WS.FC.Missing.WSDL.URL"));
		}
		
		// SSL configuration is now handled by Axis2's built-in SSL support
		// No need for custom protocol registration with HttpClient 4.x

		String serviceName = (String) getParam("service");
		if (serviceName == null || serviceName.trim().length() == 0) {
			throw new Exception(resHash
					.getString("Axis2.WS.FC.No.Configured.Service"));
		}

		Options options = new Options();
		addProxy(options, wsdl);

		service = WSUtils.createAxisServiceFromWSDLFile(wsdl, serviceName, false, 
					getHttpAuthUserName(), getHttpAuthPassword());			

		String endpointName = (String) getParam("endpoint");
		if (endpointName == null || endpointName.trim().length() == 0) {
			throw new Exception(resHash
					.getString("Axis2.WS.FC.No.Configured.Endpoint"));
		}

		endpoint = service.getEndpoint(endpointName);

		String operationName = (String) getParam("operation");
		if (operationName == null || operationName.trim().length() == 0) {
			throw new Exception(resHash
					.getString("Axis2.WS.FC.No.Configured.Operation"));
		}

		operation = service.getOperation(new QName(operationName));
		if (operation.getMessageExchangePattern().equals(
				WSDL2Constants.MEP_URI_OUT_ONLY)) {
			outonly = true;
		}

		
		if (!WSUtils.isWSDL20(getWsdlUrl(wsdl), getHttpAuthUserName(), getHttpAuthPassword())) {
			// There is a problem in the Axis2 library.
			// The message elements created from client point of view
			// are not created in the proper way.
			element = WSUtils.createAxisServiceFromWSDLFile(wsdl, serviceName,
						true, getHttpAuthUserName(), getHttpAuthPassword()).
					getOperation(new QName(operationName)).
					getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE).
					getElementQName();
		} else {
			element = operation.getMessage(	WSDLConstants.MESSAGE_LABEL_OUT_VALUE).getElementQName();
		}

		String username = (String) getParam("username");
		String password = (String) getParam("password");
		Object timeoutVal = getParam("axis2.connection.timeout");
		if (timeoutVal instanceof Number)
			timeout = ((Number) timeoutVal).longValue();
		else if (timeoutVal != null)
			timeout = Long.parseLong(timeoutVal .toString());

		EndpointReference epr = new EndpointReference(endpoint.getName());
		epr.setAddress(endpoint.getEndpointURL());
		options.setTo(epr);
		options.setProperty(
				"Transfer-Encoding",
				Boolean.FALSE);
		
		boolean https = (endpoint.getEndpointURL() != null) && endpoint.getEndpointURL().toLowerCase().startsWith("https");

		options.setTransportInProtocol(https ? Constants.TRANSPORT_HTTPS : Constants.TRANSPORT_HTTP);

		// The next call does not work anymore? the two next may be correct?
		options.setTimeOutInMilliSeconds(timeout);
		options.setProperty("SO_TIMEOUT", new Integer((int)timeout));
		options.setProperty("CONNECTION_TIMEOUT", new Integer((int)timeout));

		// set the SOAP version. The Axis2 library handles the default SOAP
		// version
		// if it is not set explicitly in the WSDL document
		options.setSoapVersionURI(endpoint.getBinding().getProperty(
				WSDL2Constants.ATTR_WSOAP_VERSION).toString());

		if (username != null && username.trim().length() != 0) {
			HttpTransportPropertiesImpl.Authenticator basicAuthentication = new HttpTransportPropertiesImpl.Authenticator();
			basicAuthentication.setUsername(username);
			basicAuthentication.setPassword(password);
			basicAuthentication.setPreemptiveAuthentication(true);
			options.setProperty(
					"AUTHENTICATE",
					basicAuthentication);
			if (getDebug()) {
				logmsg(resHash.getString(
						"Axis2.WS.FC.Using.HTTP.Basic.Authentication.Header",
						username));
			}
		}

		// create the context using the default axis configuration
		ConfigurationContext configContext = ConfigurationContextFactory
				.createDefaultConfigurationContext();

		// create the base for the client which will send the request
		sender = new ServiceClient(configContext, service);
		sender.setOptions(options);
	}

	private void addProxy(Options options, String wsdl) throws Exception {
		String proxyHost = (String) getParam("proxyHost");
		if (proxyHost == null || proxyHost.isEmpty())
			return;

		HttpTransportProperties.ProxyProperties pp = new HttpTransportProperties.ProxyProperties();

		pp.setProxyName(proxyHost);

		Object proxyPort = getParam("proxyPort");
		if (proxyPort != null)
			pp.setProxyPort(Integer.valueOf(proxyPort.toString()));

		String proxyUser = (String) getParam("proxyUser");
		if (proxyUser != null)
			pp.setUserName(proxyUser);

		String proxyPass = (String) getParam("proxyPassword");
		if (proxyPass != null)
			pp.setPassWord(proxyPass);

		options.setProperty("PROXY",pp);
		
		WSUtils.storeContent(getWsdlUrl(wsdl), getHttpAuthUserName(), getHttpAuthPassword(),
				proxyHost, proxyPort, proxyUser, proxyPass);
	}

	/**
	 * Returns valid URL object for given string.
	 * 
	 * @param url
	 *            The string which will be parsed to URL.
	 * @return The URL object corresponding to the given URL.
	 * @throws Exception
	 *             If an Exception occurs during the URL parsing.
	 */
	private URL getWsdlUrl(String url) throws Exception {
		URL wsdlURL;
		File wsdlFile = new File(wsdl);
		if (wsdlFile.exists()) {
			wsdlURL = wsdlFile.toURI().toURL();
		} else {
			wsdlURL = new URL(wsdl);
		}
		return wsdlURL;
	}

	/**
	 * Gathers the passed information and creates invocation payload for the
	 * client. The FC expects a SyncWeave Attribute which
	 * corresponds to the input message of the configured operation. If the
	 * attribute is not found an Exception will be thrown. The FC expects only
	 * SyncWeave Entry object for input and Exception will
	 * be thrown if other type is given.
	 * 
	 * @param input
	 *            The input SyncWeave Entry.
	 * @return Entry object with the server response.
	 * @exception Exception
	 *                If the required SyncWeave Attribute
	 *                is missing, if the input is not an entry or if an Axis
	 *                specific fault occurs.
	 */
	public Object perform(Object input) throws Exception {

		setAxisFault(null);
		
		if (!(input instanceof Entry)) {
			throw new Exception(resHash
					.getString("Axis2.WS.FC.Expects.Only.Entry"));
		}

		try {
			Entry work = (Entry) input;

			OperationClient op = sender.createClient(operation.getName());

			Attribute requestAttr = work.getAttribute(element.getLocalPart());

			NodeList children = work.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getLocalName().equals(
						element.getLocalPart())) {
					requestAttr = (Attribute) children.item(i);
					break;
				}
			}

			if (requestAttr == null) {
				throw new Exception(resHash.getString(
						"Axis2.WS.FC.The.Input.Operation.Parameter.Not.Found",
						element.getLocalPart()));
			}
			Attribute requestHAttr = WSUtils.verifyAttribute(requestAttr,
					element);
			OMElement feed = WSUtils.toOM(requestHAttr, OMAbstractFactory
					.getOMFactory());

			// prepare the request
			MessageContext mc = new MessageContext();

			HTTPTransportUtils.initializeMessageContext(mc, operation
					.getOutputAction(), sender.getOptions().getTo()
					.getAddress(), null);

			// add http headers to the request
			Map<String, String> map = Axis2WSServerConnector
					.getHTTPHeaders(work);
			mc.setProperty("HTTP_HEADERS", map);

			fillSOAPEnvelope(mc, feed, sender);

			// add headers the headers
			Attribute soapHeader = work.getAttribute("$soapHeader");
			WSUtils.setSOAPHeader(mc.getEnvelope(), soapHeader);
			mc.setServerSide(false);

			op.addMessageContext(mc);

			// send request
			op.execute(true);
			// response received

			Entry resEntry = new Entry();

			if (!outonly) {
				// get response if we do NOT have in-only operation
				SOAPEnvelope response = op.getMessageContext(
						WSDLConstants.MESSAGE_LABEL_IN_VALUE).getEnvelope();

				// get the http headers from the response and add them to the
				// entry
				Object httpHeaders = op.getMessageContext(
						WSDLConstants.MESSAGE_LABEL_IN_VALUE).getProperty(
						MessageContext.TRANSPORT_HEADERS);
				if (httpHeaders instanceof Map) {
					Map httpHeadersMap = (Map) httpHeaders;
					for (Object it : httpHeadersMap.keySet()) {
						String value = (String) httpHeadersMap.get(it);
						String key = "http." + (String) it;
						resEntry.addAttributeValue(key, value);
					}
				}

				Attribute resSoapHeader = WSUtils.getSOAPHeader(response,
						"$soapHeader", resEntry);
				if (resSoapHeader != null) {
					resEntry.appendChild(resSoapHeader);
				}

				OMElement result = response.getBody();
				if (result != null && result.getFirstElement() != null) {
					resEntry.appendChild(WSUtils.toDOM(
							result.getFirstElement(), resEntry));
				}
			}

			return resEntry;

		} catch (AxisFault fault) {
			setAxisFault(fault);
			throw new Exception(resHash.getString("Axis2.WS.FC.Axis.Fault",
					fault.getMessage()));
		}
	}

	/**
	 * Fills the MessageContext's SOAP Envelope with the payload information.
	 * 
	 * @param messageContext
	 *            The Context which will be filled.
	 * @param xmlPayload
	 *            The source payload information.
	 * @param service
	 *            The service client which generates the service context for the
	 *            message.
	 * @throws AxisFault
	 *             If an error occurs during the process.
	 */
	private void fillSOAPEnvelope(MessageContext messageContext,
			OMElement xmlPayload, ServiceClient service) throws AxisFault {
		ServiceContext serviceContext = service.getServiceContext();
		messageContext.setServiceContext(serviceContext);

		if (factory == null) {
			factory = getSOAPFactory(service.getOptions());
		}

		SOAPEnvelope envelope = factory.getDefaultEnvelope();

		if (xmlPayload != null) {
			envelope.getBody().addChild(xmlPayload);
		}

		service.addHeadersToEnvelope(envelope);
		messageContext.setEnvelope(envelope);
	}

	/**
	 * Retrieves the SOAP Factory from the given options.
	 * 
	 * @param options
	 *            The options containing the SOAP Factory information.
	 * @return SOAP 12 or 11 factory depending on the information provided in
	 *         the options input.
	 */
	private SOAPFactory getSOAPFactory(Options options) {
		String soapVersionURI = options.getSoapVersionURI();

		if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
			return OMAbstractFactory.getSOAP12Factory();
		} else {
			return OMAbstractFactory.getSOAP11Factory();
		}
	}

	/**
	 * Retrieves the input and output message schema of the configured
	 * operation.
	 * 
	 * @param src
	 *            not used.
	 * @return SyncWeave Schema (SchemaConfig) object
	 *         filled with the schema extracted for the operations.
	 * @throws Exception
	 *             If an error occurs during the process.
	 */
	public Object querySchema(Object src) throws Exception {
		List<SchemaConfig> schemas = SchemaUtils.getInAndOutMessageSchema(
				service, operation.getName().getLocalPart());
		FunctionConfig config = (FunctionConfig) getConfiguration().getParent();
		for (int i = 0; i < schemas.size(); i++) {
			SchemaConfig schema = schemas.get(i);
			SchemaConfig connectorSchema = config.getSchema(schema.getName()
					.toString());
			Iterator<String> names = schema.getItemNames().iterator();
			while (names.hasNext()) {
				String name = names.next();
				connectorSchema.setItem(name, schema.getItem(name));
			}
		}
		return null;
	}

	/**
	 * This method returns the current version of the component.
	 * 
	 * @return the current version of the component as a String object.
	 */
	public String getVersion() {
		return "1.0-di7.1.1 %I% 2017-12-07";
	}
	
	/**
	 * Returns the ServiceClient
	 */
	public ServiceClient getServiceClient() {
		return sender;
	}
	
	/**
	 * Returns the AxisFault that just occurred.
	 * @return The AxisFault from the last perform(), or null if no AxisFault was caught.
	 */
	public AxisFault getAxisFault() {
		return axisFault;
	}

	private void setAxisFault(AxisFault axisFault) {
		this.axisFault = axisFault;
	}
	
	/**
	 * Custom SSL socket factory that applies SSL protocol verification.
	 * This is used with Axis2's built-in SSL support.
	 */
	private static class CustomSSLSocketFactory extends SSLSocketFactory {
		private final SSLSocketFactory delegate;
		
		public CustomSSLSocketFactory() {
			this.delegate = (SSLSocketFactory) SSLSocketFactory.getDefault();
		}
		
		@Override
		public Socket createSocket() throws IOException {
			Socket s = delegate.createSocket();
			if (s instanceof SSLSocket) {
				SystemFunctions.verifySSLProtocols(s);
			}
			return s;
		}
		
		@Override
		public Socket createSocket(String host, int port) throws IOException {
			Socket s = delegate.createSocket(host, port);
			if (s instanceof SSLSocket) {
				SystemFunctions.verifySSLProtocols(s);
				setHostname(s, host);
			}
			return s;
		}
		
		@Override
		public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
			Socket s = delegate.createSocket(host, port, localHost, localPort);
			if (s instanceof SSLSocket) {
				SystemFunctions.verifySSLProtocols(s);
				setHostname(s, host);
			}
			return s;
		}
		
		@Override
		public Socket createSocket(InetAddress host, int port) throws IOException {
			Socket s = delegate.createSocket(host, port);
			if (s instanceof SSLSocket) {
				SystemFunctions.verifySSLProtocols(s);
			}
			return s;
		}
		
		@Override
		public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
			Socket s = delegate.createSocket(address, port, localAddress, localPort);
			if (s instanceof SSLSocket) {
				SystemFunctions.verifySSLProtocols(s);
			}
			return s;
		}
		
		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
			Socket s = delegate.createSocket(socket, host, port, autoClose);
			if (s instanceof SSLSocket) {
				SystemFunctions.verifySSLProtocols(s);
				setHostname(s, host);
			}
			return s;
		}
		
		@Override
		public String[] getDefaultCipherSuites() {
			return delegate.getDefaultCipherSuites();
		}
		
		@Override
		public String[] getSupportedCipherSuites() {
			return delegate.getSupportedCipherSuites();
		}
		
		private void setHostname(Socket ssl, String host) {
			try {
				java.lang.reflect.Method setHostnameMethod = ssl.getClass().getMethod("setHostname", String.class);
				setHostnameMethod.invoke(ssl, host);
			} catch (Throwable e) {
				// Ignore if method not available
			}
		}
	}
}
