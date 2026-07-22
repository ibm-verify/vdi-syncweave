/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.axis2;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.wsdl.WSDLConstants;
import org.w3c.dom.NodeList;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.connector.HTTPServerConnector;
import com.ibm.di.connector.axis2.util.SchemaUtils;
import com.ibm.di.connector.axis2.util.WSUtils;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * This Connector can be used to provide a SOAP web service, which is accessible
 * via HTTP/HTTPS. It is named after the underlying Axis2 Java library:
 * http://ws.apache.org/axis2/. The Connector uses the HTTP Server Connector as
 * its HTTP transport.
 * 
 * @since TDI 7.0
 */
public class Axis2WSServerConnector extends Connector implements
		ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "axis2wsserverconnector";
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash resHash = new ResourceHash(
			PROPERTIES_FILE);

	/**
	 * A URL of a WSDL document. Both WSDL 1.1 and WSDL 2.0 documents are
	 * allowed.
	 */
	public static final String PARAM_WSDL = "wsdlUrl";

	/** The name of the service description inside the WSDL document. */
	public static final String PARAM_SERVICE = "service";

	/**
	 * This Attribute carries the name of the AL Operation. The static
	 * configuration of this Connector (tdi.xml) declares this Attribute as the
	 * Operation Carrier.
	 */
	public static final String ATTR_OPERATION_CARRIER = "$operation";

	/** The SOAP header of the request/response message. */
	public static final String ATTR_SOAP_HEADER = "$soapHeader";

	/** Whether the authentication of the client is successful. */
	public static final String ATTR_AUTH_RESULT = "$authResult";

	/**
	 * SOAP fault code (the local part of the qualified name). If specified, the
	 * Connector will send a SOAP fault response to the client.
	 */
	public static final String ATTR_FAULT_CODE = "$faultCode";

	/** SOAP fault code (the namespace URI). */
	public static final String ATTR_FAULT_CODE_NS_URI = "$faultCodeNamespaceURI";

	/** SOAP fault code (the namespace prefix). */
	public static final String ATTR_FAULT_CODE_NS_PREFIX = "$faultCodeNamespacePrefix";

	/**
	 * SOAP fault reason. If you specify SOAP fault code, you must specify this
	 * one too.
	 */
	public static final String ATTR_FAULT_REASON = "$faultReason";

	/** SOAP fault node. */
	public static final String ATTR_FAULT_NODE = "$faultNode";

	/** SOAP fault role. */
	public static final String ATTR_FAULT_ROLE = "$faultRole";

	/** SOAP fault detail. This is a Hierarchical Attribute. */
	public static final String ATTR_FAULT_DETAIL = "$faultDetail";

	// Fields used by the server instance, which waits for clients

	/** Whether the Connector is terminating. */
	private boolean terminationRequested = false;

	/**
	 * An instance of the HTTP Server Connector, that is listening for HTTP
	 * requests.
	 */
	private HTTPServerConnector httpServer = null;

	/** The main Axis2 configuration object. */
	private ConfigurationContext axisConfig = null;

	/** The name of the web service. */
	private String serviceName = null;

	// Fields used when servicing a particular client

	/** An instance of the Connector, which waits clients. */
	private Axis2WSServerConnector serverConnector = null;

	/**
	 * An instance of the HTTP Server Connector, which handles a particular
	 * client.
	 */
	private HTTPServerConnector httpClientSession = null;

	/** An Axis2 message context, which represents the current SOAP request. */
	private MessageContext msgContext = null;

	/** Creates the Connector. */
	public Axis2WSServerConnector() {
		super();
		setModes(new String[] { ConnectorConfig.SERVER_MODE });
	}

	/**
	 * Initialize the Connector.
	 * 
	 * @param obj
	 *            Pass an instance of the HTTPServerConnector, which handles a
	 *            particular client to initialize the Connector for a client
	 *            session. Otherwise the Connector will start an HTTP server and
	 *            will wait for clients.
	 * @exception Exception
	 *                If the 'service' parameter is not specified and the WSDL
	 *                document contains multiple service descriptions. If the
	 *                'service' parameter is specified, but does not refer to an
	 *                existing service description from the WSDL document. A
	 *                problem while reading the WSDL document or an Axis2
	 *                related error.
	 */
	public void initialize(Object obj) throws Exception {

		super.initialize(obj);

		terminationRequested = false;

		if (obj instanceof HTTPServerConnector) {

			// service a particular client
			httpClientSession = (HTTPServerConnector) obj;
		} else {

			initializeServer();
		}
	}

	/**
	 * Initialize as the Connector instance, which waits for clients.
	 * 
	 * @exception Exception
	 *                If the 'wsdlUrl' parameter is missing. If the 'service'
	 *                parameter is not specified and the WSDL document contains
	 *                multiple service descriptions. If the 'service' parameter
	 *                is specified, but does not refer to an existing service
	 *                description from the WSDL document. A problem while
	 *                reading the WSDL document or an Axis2 related error.
	 */
	private void initializeServer() throws Exception {

		String wsdl = getParam(PARAM_WSDL);

		if (wsdl == null || wsdl.trim().length() == 0) {
			throw new Exception(
					resHash
							.getString("CONNECTOR.AXIS2WSSERVER.MISSING.WSDL.PARAMETER"));
		}

		axisConfig = createAxisConfigFromWSDL(wsdl);

		serviceName = getParam(PARAM_SERVICE);

		if (serviceName == null || serviceName.trim().length() == 0) {

			// A service is not configured - choose one
			Map serviceMap = axisConfig.getAxisConfiguration().getServices();
			if (serviceMap.size() == 1) {
				serviceName = (String) serviceMap.keySet().iterator().next();
			} else {
				throw new Exception(resHash.getString(
						"CONNECTOR.AXIS2WSSERVER.MULTIPLE.SERVICES", wsdl));
			}
		} else {

			// Verify the service exists
			if (axisConfig.getAxisConfiguration().getService(serviceName) == null) {
				throw new Exception(resHash.getString(
						"CONNECTOR.AXIS2WSSERVER.SERVICE.NOT.FOUND",
						new Object[] { wsdl, serviceName }));
			}
		}

		// Run the HTTP server
		httpServer = new HTTPServerConnector();
		httpServer.setConfiguration(getConfiguration());
		httpServer.setRSInterface(getRSInterface());
		httpServer.setName(getName());
		httpServer.setLog(getLog());
		httpServer.initialize(null);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConnectorInterface getNextClient() throws Exception {

		if (isTerminating()) {
			return null;
		}

		ConnectorInterface httpSession = null;
		while (httpSession == null && !isTerminating()) {
			try {
				httpSession = httpServer.getNextClient();
			} catch (Exception ex) {
				logmsg(resHash.getString(
						"CONNECTOR.AXIS2WSSERVER.CLIENT.CONNECTION.ERROR", ex));
			}
		}

		if (isTerminating()) {
			terminate();
			return null;
		}

		Axis2WSServerConnector clientSession = new Axis2WSServerConnector();
		clientSession.serverConnector = this;
		clientSession.setConfiguration(getConfiguration());
		clientSession.setRSInterface(getRSInterface());
		clientSession.setName(getName());
		clientSession.setLog(getLog());
		clientSession.initialize(httpSession);

		return clientSession;
	}

	/**
	 * Stop servicing clients.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void terminateServer() throws Exception {

		if (serverConnector == null) {

			// At this point we are the server
			terminationRequested = true;
			httpServer.terminateServer();
		} else {

			// At this point we are a client session
			if (!serverConnector.isTerminating()) {
				serverConnector.terminateServer();
			}
		}

		super.terminateServer();
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {

		if (httpServer != null) {
			httpServer.terminate();
		}

		if (httpClientSession != null) {
			httpClientSession.terminate();
		}

		super.terminate();
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getNextEntry() throws Exception {

		Entry httpRequestEntry = httpClientSession.getNextEntry();

		if (httpRequestEntry == null) {
			return null;
		}

		Entry e = httpRequestEntry;

		if (debugMode()) {
			// dump the whole HTTP Entry
			getLog().dumpEntry(httpRequestEntry);
		}

		String soapRequest = httpRequestEntry
				.getString(HTTPServerConnector.ATTR_NAME_HTTP_BODY);
		if (soapRequest == null || soapRequest.length() == 0) {
			// no SOAP message in the HTTP request
			msgContext = null;
			return e;
		}

		ConfigurationContext axisConfig = serverConnector.getAxisConfig();
		String serviceName = serverConnector.getServiceName();
		try {
			msgContext = readSOAPRequest(axisConfig, serviceName,
					httpRequestEntry);
		} catch (Exception ex) {
			throw new Exception(resHash.getString(
					"CONNECTOR.AXIS2WSSERVER.PARSE.REQUEST.ERROR",
					new Object[] { soapRequest, ex }), ex);
		}

		OMElement requestPayload = msgContext.getEnvelope().getBody()
				.getFirstElement();
		requestPayload.build();
		requestPayload.detach();

		Attribute requestAttr = (Attribute) WSUtils.toDOM(requestPayload, e);
		e.setAttribute(requestAttr);

		Attribute soapHeaderAttr = WSUtils.getSOAPHeader(msgContext
				.getEnvelope(), ATTR_SOAP_HEADER, e);
		if (soapHeaderAttr != null) {
			e.setAttribute(soapHeaderAttr);
		}

		// set the operation name
		e.setAttribute(ATTR_OPERATION_CARRIER, getOperationQName()
				.getLocalPart());

		return e;
	}

	/**
	 * {@inheritDoc}
	 */
	public void replyEntry(Entry conn) throws Exception {

		// HTTP basic authentication
		if (Boolean.valueOf(
				getParam(HTTPServerConnector.PARAMETER_HTTP_BASIC_AUTH))
				.booleanValue()) {

			String httpBasicAuthResult = conn.getString(ATTR_AUTH_RESULT);
			if (!Boolean.valueOf(httpBasicAuthResult).booleanValue()) {
				httpClientSession.rejectClientAuthentication();
				return;
			}
		}

		Entry httpResponseEntry = new Entry();

		// Prepare a SOAP response only if there was a SOAP request
		if (msgContext != null) {

			SOAPEnvelope envelope = null;
			if (conn.getAttribute(HTTPServerConnector.ATTR_NAME_HTTP_BODY) == null) {
				envelope = prepareSOAPResponse(conn);
			} else {
				/*
				 * No need to generate a SOAP response - there is already an
				 * HTTP body in 'conn'.
				 */
				logmsg(resHash
						.getString("CONNECTOR.AXIS2WSSERVER.HTTP.BODY.OVERWRITE"));
			}

			if (envelope != null) {

				Attribute soapHeaderAttr = conn.getAttribute(ATTR_SOAP_HEADER);
				WSUtils.setSOAPHeader(envelope, soapHeaderAttr);

				StringWriter soapResponseWriter = new StringWriter();
				envelope.serialize(soapResponseWriter);

				String contentType = (String) msgContext
						.getProperty(org.apache.axis2.Constants.Configuration.CONTENT_TYPE);
				httpResponseEntry.setAttribute(
						HTTPServerConnector.ATTR_NAME_HTTP_CONTENT_TYPE,
						contentType);

				httpResponseEntry.setAttribute(
						HTTPServerConnector.ATTR_NAME_HTTP_BODY,
						soapResponseWriter.toString());
			}

		}

		// allow users to set the HTTP headers of the response
		httpResponseEntry.merge(conn);

		httpClientSession.replyEntry(httpResponseEntry);
	}

	/**
	 * Create a SOAP response message (normal or fault).
	 * 
	 * @param conn
	 *            The 'conn' Entry.
	 * @return The SOAP response.
	 * @throws Exception
	 *             If a SOAP fault is provided in 'conn' and the message
	 *             exchange pattern of the operation does not allow a fault
	 *             response. If not response (neither normal, nor fault) is
	 *             provided in 'conn' and the message exchange pattern of the
	 *             operation requires a response. If both a normal response and
	 *             a fault are provided in 'conn'. If an error occurs while
	 *             composing the SOAP response.
	 */
	private SOAPEnvelope prepareSOAPResponse(Entry conn) throws Exception {

		SOAPEnvelope envelope = null;

		final String mep = getOperation().getMessageExchangePattern();

		String faultCode = conn.getString(ATTR_FAULT_CODE);
		if (faultCode != null && !canRespondWithFault(mep)) {
			throw new Exception(resHash.getString(
					"CONNECTOR.AXIS2WSSERVER.FAULT.RESPONSE.NOT.ALLOWED",
					new Object[] { mep, getOperationQName() }));
		}

		Attribute responseAttr = null;
		if (mustRespond(mep)) {

			QName responseQName = getOperation().getMessage(
					WSDLConstants.MESSAGE_LABEL_OUT_VALUE).getElementQName();
			logmsg(resHash.getString(
					"CONNECTOR.AXIS2WSSERVER.EXPECTED.RESPONSE.ATTRIBUTE",
					responseQName));

			NodeList children = conn.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getLocalName().equals(
						responseQName.getLocalPart())) {
					responseAttr = (Attribute) children.item(i);
					break;
				}
			}

			// response is mandatory
			if (faultCode == null && responseAttr == null) {
				throw new Exception(resHash
						.getString("CONNECTOR.AXIS2WSSERVER.RESPONSE.REQUIRED",
								new Object[] { mep, getOperationQName(),
										responseQName }));
			}

			// either fault or normal response, but not both
			if (faultCode != null && responseAttr != null) {
				throw new Exception(resHash
						.getString("CONNECTOR.AXIS2WSSERVER.MESSAGE.AND.FAULT"));
			}

			if (responseAttr != null) {
				responseAttr = WSUtils.verifyAttribute(responseAttr,
						responseQName);
				OMElement responsePayload = WSUtils.toOM(responseAttr);
				try {
					envelope = prepareSOAPNormalResponse(msgContext,
							responsePayload);
				} catch (AxisFault axisFault) {
					throw new Exception(resHash.getString(
							"CONNECTOR.AXIS2WSSERVER.PREPARE.RESPONSE.ERROR",
							axisFault));
				}
			}
		}

		if (faultCode != null) {
			envelope = prepareSOAPFaultResponse(msgContext, conn);
		}

		// will not generate a SOAP response
		if (faultCode == null && responseAttr == null) {
			logmsg(resHash
					.getString("CONNECTOR.AXIS2WSSERVER.NO.SOAP.RESPONSE"));
		}

		return envelope;
	}

	/**
	 * Version information.
	 * 
	 * @return version of the Connector.
	 */
	public String getVersion() {
		return "1.0-di7.1.1 %I% 20%E%";
	}

	/**
	 * @return Whether the Connector is terminating.
	 */
	public boolean isTerminating() {
		return terminationRequested;
	}

	/**
	 * @return The main Axis2 configuration.
	 */
	private ConfigurationContext getAxisConfig() {
		return axisConfig;
	}

	/**
	 * @return The name of the web service.
	 */
	private String getServiceName() {
		return serviceName;
	}

	/**
	 * @param mepURI
	 *            A message exchange pattern URI.
	 * @return Whether the message exchange pattern allows a fault response.
	 */
	private static boolean canRespondWithFault(String mepURI) {
		return WSDL2Constants.MEP_URI_IN_OUT.equals(mepURI)
				|| WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(mepURI);
	}

	/**
	 * @param mepURI
	 *            A message exchange pattern URI.
	 * @return Whether the message exchange pattern requires a response (normal
	 *         or fault).
	 */
	private static boolean mustRespond(String mepURI) {
		return WSDL2Constants.MEP_URI_IN_OUT.equals(mepURI);
	}

	/**
	 * @return The qualified name of the current web service operation.
	 */
	private QName getOperationQName() {

		QName operationQName = null;
		AxisOperation op = getOperation();
		if (op != null) {
			operationQName = op.getName();
		}

		return operationQName;
	}

	/**
	 * @return The Axis2 configuration of the current web service operation.
	 */
	private AxisOperation getOperation() {

		AxisOperation op = null;

		if (msgContext != null && msgContext.getAxisOperation() != null) {
			op = msgContext.getAxisOperation();
		}

		return op;
	}

	/**
	 * Create a SOAP fault message.
	 * 
	 * @param inMsgContext
	 *            The Axis2 incoming message, for which is the fault response.
	 * @param conn
	 *            The 'conn' Entry.
	 * @return A SOAP fault message. The SOAP version will be the same as the
	 *         incoming message.
	 * @exception Exception
	 *                If the fault reason is not specified or if the fault
	 *                generation fails.
	 */
	private static SOAPEnvelope prepareSOAPFaultResponse(
			MessageContext inMsgContext, Entry conn) throws Exception {

		String faultCode = conn.getString(ATTR_FAULT_CODE);

		if (faultCode == null) {
			return null;
		}

		String faultReason = conn.getString(ATTR_FAULT_REASON);
		if (faultReason == null) {
			throw new Exception(resHash
					.getString("CONNECTOR.AXIS2WSSERVER.MISSING.FAULT.REASON"));
		}

		String faultCodeNSURI = conn.getString(ATTR_FAULT_CODE_NS_URI);
		String faultCodeNSPrefix = conn.getString(ATTR_FAULT_CODE_NS_PREFIX);

		QName faultCodeQName;
		if (faultCodeNSURI != null && faultCodeNSPrefix != null) {
			faultCodeQName = new QName(faultCodeNSURI, faultCode,
					faultCodeNSPrefix);
		} else {
			faultCodeQName = new QName(faultCode);
		}

		String faultNode = conn.getString(ATTR_FAULT_NODE);
		String faultRole = conn.getString(ATTR_FAULT_ROLE);

		Attribute wrappedFaultDetailHAttr = conn
				.getAttribute(ATTR_FAULT_DETAIL);
		OMElement faultDetail = null;
		if (wrappedFaultDetailHAttr != null) {
			OMElement wrappedFaultDetailOM = WSUtils
					.toOM(wrappedFaultDetailHAttr);
			faultDetail = wrappedFaultDetailOM.getFirstElement();
		}

		AxisFault axisFault = new AxisFault(faultCodeQName, faultReason,
				faultNode, faultRole, faultDetail);

		SOAPEnvelope envelope;
		try {
			envelope = prepareSOAPFaultResponse(inMsgContext, axisFault);
		} catch (AxisFault af) {
			throw new Exception(resHash.getString(
					"CONNECTOR.AXIS2WSSERVER.PREPARE.FAULT.ERROR", af), af);
		}

		return envelope;
	}

	/**
	 * Create Axis2 main configuration based on a given WSDL document.
	 * 
	 * @param wsdlUrl
	 *            An URL, which points to a WSDL document.
	 * @return Axis2 configuration.
	 * @exception Exception
	 *                A problem while reading the WSDL document or an Axis2
	 *                related error.
	 */
	private static ConfigurationContext createAxisConfigFromWSDL(String wsdlUrl)
			throws Exception {

		// Load the axis.xml and the modules from the classpath
		ConfigurationContext axisConfig;
		try {
			axisConfig = ConfigurationContextFactory
					.createConfigurationContextFromFileSystem(null, null);
		} catch (AxisFault axisFault) {
			throw new Exception(resHash.getString(
					"CONNECTOR.AXIS2WSSERVER.CREATE.AXIS2.CONFIG.ERROR",
					axisFault), axisFault);
		}

		AxisConfiguration axisConfiguration = axisConfig.getAxisConfiguration();

		/*
		 * Clear all transports - we will inject what is necessary by hand in
		 * the message contexts
		 */
		axisConfiguration.getTransportsIn().clear();
		axisConfiguration.getTransportsOut().clear();

		List services;
		try {
			services = WSUtils.createAllAxisServicesFromWSDLFile(wsdlUrl);
		} catch (Exception ex) {
			throw new Exception(resHash.getString(
					"CONNECTOR.AXIS2WSSERVER.READ.WSDL.ERROR", new Object[] {
							wsdlUrl, ex }), ex);
		}

		MessageReceiver noopReceiver = new NOOPReceiver();

		for (Iterator it = services.iterator(); it.hasNext();) {

			AxisService serviceConfig = (AxisService) it.next();

			// plug-in the no-op receiver into the configuration
			for (Iterator opIt = serviceConfig.getOperations(); opIt.hasNext();) {
				AxisOperation operation = (AxisOperation) opIt.next();
				operation.setMessageReceiver(noopReceiver);
			}

			axisConfiguration.addService(serviceConfig);
		}

		return axisConfig;
	}

	/**
	 * Read a SOAP request out of an HTTP request.
	 * 
	 * @param configContext
	 *            An Axis2 configuration.
	 * @param serviceName
	 *            A name of a service, described in the Axis2 configuration.
	 * @param httpEntry
	 *            An Entry produced by the HTTP Server Connector.
	 * @return A SOAP request.
	 * @exception AxisFault
	 *                An error by the Axis2 library.
	 * @exception UnsupportedEncodingException
	 *                The character encoding of the HTTP request is not
	 *                supported.
	 */
	private static MessageContext readSOAPRequest(
			ConfigurationContext configContext, String serviceName,
			Entry httpEntry) throws AxisFault, UnsupportedEncodingException {

		Object request = httpEntry
				.getObject(HTTPServerConnector.ATTR_NAME_HTTP_BODY);

		String soapAction = httpEntry
				.getString("http.SOAPAction");

		String contentType = httpEntry
				.getString(HTTPServerConnector.ATTR_NAME_HTTP_CONTENT_TYPE);

		String requestURI = httpEntry.getString("http.base");

		String remoteAddress = httpEntry.getString("tcp.remoteHost");

		AxisService serviceConfig = configContext.getAxisConfiguration()
				.getService(serviceName);

		// Create a message context representing the request
		MessageContext msgContext = configContext.createMessageContext();

		/*
		 * Explicitly set the service, so that Axis2 dispatchers do not have to
		 * worry about finding it
		 */
		msgContext.setAxisService(serviceConfig);

		// Do what the AxisServlet.doPost does
		msgContext.setServerSide(true);

		if (requestURI != null) {
			msgContext.setTo(new EndpointReference(requestURI));
		}

		if (remoteAddress != null) {

			msgContext.setFrom(new EndpointReference(remoteAddress));
			msgContext.setProperty(MessageContext.REMOTE_ADDR, remoteAddress);
		}

		// Transfer all HTTP headers into the MessageContext
		Map<String, String> transportHeaders = getHTTPHeaders(httpEntry);
		msgContext.setProperty(MessageContext.TRANSPORT_HEADERS,
				transportHeaders);

		msgContext.setProperty(
				org.apache.axis2.Constants.Configuration.CONTENT_TYPE,
				contentType);

		// Determine how Axis2 expects the request to be encoded
		String requestEncoding = BuilderUtil.getCharSetEncoding(contentType);
		byte[] requestBytes = ((String) request).getBytes(requestEncoding);
		InputStream requestStream = new ByteArrayInputStream(requestBytes);

		/*
		 * The following will call AxisEngine.receive, however we have installed
		 * NOOP message receivers for all operations and as a result the output
		 * transport will not get triggered
		 */
		HTTPTransportUtils.processHTTPPostRequest(msgContext, requestStream,
				null, contentType, soapAction, requestURI);

		/*
		 * The returned message context will have its operation filled in by the
		 * Axis2 dispatchers
		 */
		return msgContext;
	}

	/**
	 * @param httpEntry
	 *            An Entry produced by the HTTP Server Connector.
	 * @return The HTTP headers from the Entry.
	 */
	public static Map<String, String> getHTTPHeaders(Entry httpEntry) {

		final String HTTP_HEADER_ATTRIBUTE_PREFIX = "http.";

		Map<String, String> httpHeaders = new HashMap<String, String>();
		String[] names = httpEntry.getAttributeNames();
		for (int i = 0; i < names.length; ++i) {
			if (names[i].startsWith(HTTP_HEADER_ATTRIBUTE_PREFIX)) {
				String httpHeaderValue = httpEntry.getString(names[i]);
				String httpHeaderName = names[i]
						.substring(HTTP_HEADER_ATTRIBUTE_PREFIX.length());

				httpHeaders.put(httpHeaderName, httpHeaderValue);
			}
		}

		return httpHeaders;
	}

	/**
	 * Create a normal SOAP response message.
	 * 
	 * @param inMsgContext
	 *            The Axis2 incoming message, for which is the response.
	 * @param responsePayload
	 *            The response payload.
	 * @return A normal SOAP response message. The SOAP version will be the same
	 *         as the incoming message.
	 * @exception AxisFault
	 *                An error by the Axis2 library.
	 */
	private static SOAPEnvelope prepareSOAPNormalResponse(
			MessageContext inMsgContext, OMElement responsePayload)
			throws AxisFault {

		// Create an output message context
		MessageContext outMsgContext = MessageContextBuilder
				.createOutMessageContext(inMsgContext);
		outMsgContext.getOperationContext().addMessageContext(outMsgContext);

		/*
		 * This code is based on the functionality of
		 * org.apache.axis2.receivers.RawXMLINOutMessageReceiver.receive.
		 */

		String soapNamespaceURI = inMsgContext.getEnvelope().getNamespace()
				.getNamespaceURI();
		SOAPFactory fac = null;
		if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI
				.equals(soapNamespaceURI)) {
			fac = OMAbstractFactory.getSOAP12Factory();
		} else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI
				.equals(soapNamespaceURI)) {
			fac = OMAbstractFactory.getSOAP11Factory();
		} else {
			throw new AxisFault(resHash.getString(
					"CONNECTOR.AXIS2WSSERVER.UNKNOWN.SOAP.VERSION",
					soapNamespaceURI));
		}

		// Put the response payload in the output message context
		SOAPEnvelope outMsgEnvelope = fac.getDefaultEnvelope();
		outMsgEnvelope.getBody().addChild(responsePayload);
		outMsgContext.setEnvelope(outMsgEnvelope);

		MessageSaver mySender = new MessageSaver();
		TransportOutDescription transportOutDesc = new TransportOutDescription(
				"tdi-sender");
		// In Axis2 1.8.2, we can't use setSender with our custom handler
		// Instead, we'll add the handler to the execution chain
		outMsgContext.setTransportOut(transportOutDesc);
		outMsgContext.getExecutionChain().add(mySender);

		AxisEngine.send(outMsgContext);

		// see what the sender was given to send
		MessageContext msgContextForSend = mySender.getSaved();

		SOAPEnvelope soapResponse = msgContextForSend.getEnvelope();

		return soapResponse;
	}

	/**
	 * Create a SOAP fault message.
	 * 
	 * @param inMsgContext
	 *            The Axis2 incoming message, for which is the fault response.
	 * @param axisFault
	 *            Axis2 object, which represents the SOAP fault.
	 * @return A SOAP message, which contains a SOAP fault. The SOAP version
	 *         will be the same as the incoming message.
	 * @exception AxisFault
	 *                An error by the Axis2 library.
	 */
	private static SOAPEnvelope prepareSOAPFaultResponse(
			MessageContext inMsgContext, AxisFault axisFault) throws AxisFault {

		// do not send stack traces from our code as SOAP fault details
		inMsgContext.getOperationContext().setProperty(
				Constants.Configuration.SEND_STACKTRACE_DETAILS_WITH_FAULTS,
				"false");

		MessageContext faultMsgContext = MessageContextBuilder
				.createFaultMessageContext(inMsgContext, axisFault);

		MessageSaver mySender = new MessageSaver();
		TransportOutDescription transportOutDesc = new TransportOutDescription(
				"tdi-sender");
		// In Axis2 1.8.2, we can't use setSender with our custom handler
		// Instead, we'll add the handler to the execution chain
		faultMsgContext.setTransportOut(transportOutDesc);
		faultMsgContext.getExecutionChain().add(mySender);

		AxisEngine.sendFault(faultMsgContext);

		// see what the sender was given to send
		MessageContext msgContextForSend = mySender.getSaved();

		SOAPEnvelope soapResponse = msgContextForSend.getEnvelope();

		return soapResponse;
	}

	/**
	 * An Axis2 outgoing transport, which remembers the message it is asked to
	 * send. The Connector uses this class to invert the flow control: sending a
	 * response is now triggered by TDI (Connector.replyEntry) and not by Axis2
	 * (AxisEngine.send).
	 *
	 * In Axis2 1.8.2, TransportSender interface has been removed/refactored.
	 * This class now only extends AbstractHandler and implements the necessary
	 * methods for message handling.
	 */
	private static class MessageSaver extends AbstractHandler {

		/**
		 * The message, which this transport sender has been asked to send.
		 */
		private MessageContext saved = null;

		/**
		 * @return The message, which this transport sender has been asked to
		 *         send.
		 */
		public MessageContext getSaved() {
			return saved;
		}

		/**
		 * Saves the message and proceeds with the next {@link Handler}.
		 *
		 * @param msgContext
		 *            message
		 * @return information that the next {@link Handler} should proceed.
		 */
		public InvocationResponse invoke(MessageContext msgContext) {
			saved = msgContext;
			return InvocationResponse.CONTINUE;
		}

		/**
		 * Cleanup method for compatibility.
		 *
		 * @param msgCtx message context
		 */
		public void cleanup(MessageContext msgCtx) {
			// No cleanup needed
		}

		/**
		 * Initialization method for compatibility.
		 *
		 * @param cc configuration context
		 * @param tod transport out description
		 */
		public void init(ConfigurationContext cc, TransportOutDescription tod) {
			// No initialization needed
		}

		/**
		 * Stop method for compatibility.
		 */
		public void stop() {
			// No stop action needed
		}
	}

	/**
	 * An Axis2 message receiver, which does nothing.
	 */
	private static class NOOPReceiver implements MessageReceiver {

		/**
		 * Receives message and does nothing.
		 * 
		 * @param messageCtx
		 *            message
		 */
		public void receive(MessageContext messageCtx) {
		}
	}

	/**
	 * @return The container with Connector's messages.
	 */
	public static ResourceHash getResHash() {
		return resHash;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object querySchema(Object input) throws Exception {
		AxisService service = axisConfig.getAxisConfiguration().getService(
				serviceName);
		List<SchemaConfig> schemas = SchemaUtils
				.getSchemaForAllOperationsInService(service);
		ConnectorConfig config = (ConnectorConfig) getConfiguration();
		for (int i = 0; i < schemas.size(); i++) {
			SchemaConfig schema = schemas.get(i);
			SchemaConfig connectorSchema = config.getSchema(schema.getName()
					.toString());
			Iterator names = schema.getItemNames().iterator();
			while (names.hasNext()) {
				String name = (String) names.next();
				connectorSchema.setItem(name, schema.getItem(name));
			}
		}
		return null;
	}

}
