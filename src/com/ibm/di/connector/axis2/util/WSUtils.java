/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.axis2.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.codec.binary.Base64;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL11ToAllAxisServicesBuilder;
import org.apache.axis2.description.WSDL20ToAllAxisServicesBuilder;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * Utility class , providing various methods for managing Axis 2 web services.
 * 
 */
public class WSUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Resource Hash used to access TMS messages.
	 */
	private static ResourceHash resHash = com.ibm.di.connector.axis2.Axis2WSServerConnector
			.getResHash();

	/**
	 * XML namespace URI.
	 */
	private static final String XML_NS_URI = "http://www.w3.org/2000/xmlns/";

	private static Map<URL, byte[]> wsdlContent = new ConcurrentHashMap<URL, byte[]>();
	private static Map<URL, Long> wsdlTimeout = new ConcurrentHashMap<URL, Long>();
	
	/**
	 * Converts Apache AXIOM element to W3C DOM Element, i.e. to TDI
	 * Hierarchical Attribute.
	 * 
	 * @param src
	 *            The AXIOM element which will be converted.
	 * @param doc
	 *            The document used to create all needed DOM information.
	 * @return DOM Element object which corresponds to the AXIOM element.
	 */
	public static Element toDOM(OMElement src, Document doc) {
		// Create the element
		Element dst;
		if (src.getNamespace() != null) {
			dst = doc.createElementNS(src.getNamespace().getNamespaceURI(), src
					.getLocalName());
			dst.setPrefix(src.getNamespace().getPrefix());
		} else {
			dst = doc.createElement(src.getLocalName());
		}

		// Copy namespace declarations
		Iterator namespaceIt = src.getAllDeclaredNamespaces();
		while (namespaceIt.hasNext()) {
			OMNamespace omNamespace = (OMNamespace) namespaceIt.next();
			String contextPrefix = omNamespace.getPrefix();
			String contextNS = omNamespace.getNamespaceURI();

			String attrName;
			if (contextPrefix == null || "".equals(contextPrefix)) {
				attrName = "xmlns";
			} else {
				attrName = "xmlns:" + contextPrefix;
			}

			dst.setAttributeNS(XML_NS_URI, attrName, contextNS);
		}

		// Copy attributes
		Iterator attrIt = src.getAllAttributes();
		while (attrIt.hasNext()) {
			OMAttribute omAttr = (OMAttribute) attrIt.next();

			String localName = omAttr.getLocalName();
			String value = omAttr.getAttributeValue();

			Attr attr;
			if (omAttr.getNamespace() != null) {

				String nsPrefix = omAttr.getNamespace().getPrefix();
				String nsURI = omAttr.getNamespace().getNamespaceURI();

				attr = doc.createAttributeNS(nsURI, localName);
				attr.setPrefix(nsPrefix);
			} else {

				attr = doc.createAttribute(localName);
			}

			attr.setNodeValue(value);
			dst.setAttributeNode(attr);
		}

		// Copy child nodes (elements, text, cdata, ...).
		Iterator childIt = src.getChildren();
		while (childIt.hasNext()) {
			OMNode childOMNode = (OMNode) childIt.next();

			if (childOMNode == null) {
				continue;
			}

			Node childDOMNode = null;

			int omType = childOMNode.getType();
			switch (omType) {

			case OMNode.ELEMENT_NODE:
				OMElement childOMElement = (OMElement) childOMNode;
				childDOMNode = toDOM(childOMElement, doc);
				break;

			case OMNode.CDATA_SECTION_NODE:
				OMText childOMCDATA = (OMText) childOMNode;
				String cdata = childOMCDATA.getText();
				childDOMNode = doc.createCDATASection(cdata);
				break;

			case OMNode.TEXT_NODE:
				OMText childOMText = (OMText) childOMNode;
				String text = childOMText.getText();
				if (text != null && text.contains("\n") && text.trim().length() == 0)
					break;
				childDOMNode = doc.createTextNode(text);
				break;

			case OMNode.COMMENT_NODE:
			case OMNode.DTD_NODE:
			case OMNode.ENTITY_REFERENCE_NODE:
			case OMNode.PI_NODE:
			case OMNode.SPACE_NODE:
				// skip
				break;

			default:
				// unknown node - skip this
			}

			if (childDOMNode != null) {
				dst.appendChild(childDOMNode);
			}
		}

		return dst;
	}

	/**
	 * Converts W3C DOM Element to Apache AXIOM element. It uses the default
	 * OMFactory.
	 * 
	 * @param src
	 *            The element which will be converted.
	 * @return AXIOM element corresponding to the source DOM Element object.
	 */
	public static OMElement toOM(Element src) {

		OMFactory factory = OMAbstractFactory.getOMFactory();
		return toOM(src, factory);
	}

	/**
	 * Converts W3C DOM Element to Apache AXIOM element. It uses the provided
	 * OMFactory.
	 * 
	 * @param src
	 *            The element which will be converted.
	 * @param factory
	 *            The desired OMFacotry for the convertion.
	 * @return AXIOM element corresponding to the source DOM Element object.
	 */
	public static OMElement toOM(Element src, OMFactory factory) {

		String localName = src.getLocalName();
		String nsURI = src.getNamespaceURI();
		String nsPrefix = src.getPrefix();

		// Create the element
		QName qname;
		if (nsURI == null || nsURI.length() == 0) {
			qname = new QName(localName);
		} else {
			if (nsPrefix == null || nsPrefix.length() == 0) {
				qname = new QName(nsURI, localName);
			} else {
				qname = new QName(nsURI, localName, nsPrefix);
			}
		}
		OMElement dst = factory.createOMElement(qname);
		
		NamedNodeMap attrMap = src.getAttributes();
		for (int i = 0; i < attrMap.getLength(); ++i) {
			Attr attr = (Attr) attrMap.item(i);
			copyDOMAttribute(attr, dst, factory);
		}

		NodeList nodes = src.getChildNodes();
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node child = nodes.item(i);

			int nodeType = child.getNodeType();
			switch (nodeType) {
			case Node.ATTRIBUTE_NODE:
				copyDOMAttribute((Attr) child, dst, factory);
				break;
			case Node.CDATA_SECTION_NODE:
				CDATASection cdata = (CDATASection) child;
				OMText cdataOM = factory.createOMText(cdata.getData(),
						OMNode.CDATA_SECTION_NODE);
				dst.addChild(cdataOM);
				break;
			case Node.ELEMENT_NODE:
				OMElement elemOM = toOM((Element) child, factory);
				dst.addChild(elemOM);
				break;
			case Node.TEXT_NODE:
				Text text = (Text) child;
				OMText textOM = factory.createOMText(text.getData(),
						OMNode.TEXT_NODE);
				dst.addChild(textOM);
				break;
			default:
				// unrecognized node - skip it
			}
		}

		return dst;
	}

	/**
	 * Copies the W3C DOM attribute as AXIOM attribute to the destination
	 * element.
	 * 
	 * @param attr
	 *            The attributes which will be copied.
	 * @param dst
	 *            The AXIOM element which will be populated with the given
	 *            attribute information.
	 * @param factory
	 *            The factory which will be used during the conversion process.
	 */
	private static void copyDOMAttribute(Attr attr, OMElement dst,
			OMFactory factory) {

		String attrName = attr.getName();
		String attrValue = attr.getValue();

		if (attrName.equalsIgnoreCase("xmlns")) {

			// the default namespace
			dst.declareDefaultNamespace(attrValue);

		} else if (attrName.startsWith("xmlns:")) {

			// some other namespace
			String pref = attrName.substring("xmlns:".length());
			dst.declareNamespace(attrValue, pref);

		} else {
			// a normal attribute
			OMNamespace ns = null;
			if (attr.getNamespaceURI() != null
					&& attr.getNamespaceURI().length() > 0) {
				// attribute has a namespace
				ns = factory.createOMNamespace(attr.getNamespaceURI(), attr
						.getPrefix());
			}
			dst.addAttribute(attr.getLocalName(), attrValue, ns);
		}
	}
	
	
	/**
	 * This method determines the version of the WSDL file which corresponds to
	 * the given URL.
	 * 
	 * @param wsdlURL
	 *            The URL to the WSDL file.
	 * 	 
	 * @param username
	 * 			 Uername for http auth. 
	 * @param password
	 * 			Password for http auth. 
	 * @return Returns true if the WSDL file version 2.0 and false if it is 1.1.
	 * 
	 * @throws Exception
	 *             The WSDL version cannot be recognized.	 * 
	 */	
	public static boolean isWSDL20(URL wsdlURL) throws Exception {
		return (isWSDL20(wsdlURL, null, null));
	}
	

	/**
	 * This method determines the version of the WSDL file which corresponds to
	 * the given URL.
	 * 
	 * @param wsdlURL
	 *            The URL to the WSDL file.
	 * 	 
	 * @param username
	 * 			 Uername for http auth. 
	 * @param password
	 * 			Password for http auth. 
	 * @return Returns true if the WSDL file version 2.0 and false if it is 1.1.
	 * 
	 * @throws Exception
	 *             The WSDL version cannot be recognized.	 * 
	 */
	public static boolean isWSDL20(URL wsdlURL, String username, String password) throws Exception {
		
		InputStream inp = getInputStream(wsdlURL, username, password);

		try {
			OMNamespace documentElementNS = ((OMElement) XMLUtils.toOM(inp))
					.getNamespace();

			if (documentElementNS != null) {
				if (WSDL2Constants.WSDL_NAMESPACE.equals(documentElementNS
						.getNamespaceURI())) {
					// we have a WSDL 2.0 document here.
					return true;
				} else if (Constants.NS_URI_WSDL11.equals(documentElementNS
						.getNamespaceURI())) {
					return false;
				} else {
					throw new Exception(resHash.getString(
							"WSUtils.Cannot.Recognize.WSDL.Version", wsdlURL));
				}
			} else {
				throw new Exception(resHash.getString(
						"WSUtils.Cannot.Recognize.WSDL.Version", wsdlURL));
			}

		} finally {

			if (inp != null) {
				inp.close();
			}
		}
	}

	/**
	 * Returns all services from the given WSDL file. The services are created
	 * from server point of view - this affects the input/output message
	 * creation.
	 * 
	 * @param wsdl
	 *            The location to the WSDL file.
	 * @return Vector object with all services in the WSDL file.
	 * @throws Exception
	 *             If an Exception occurs during the process. Check the
	 *             Exception message for more details.
	 */
	public static Vector createAllAxisServicesFromWSDLFile(String wsdl)
			throws Exception {
		return createAllAxisServicesFromWSDLFile(wsdl, true, null, null);
	}

	/**
	 * Returns all services from the given WSDL file. The services are created
	 * from server or client point of view - this affects the input/output
	 * message creation.
	 * 
	 * @param wsdl
	 *            The location to the WSDL file.
	 * @param isServerSide
	 *            If true the services will be created form server point of
	 *            view. If false - from client point of view.
	 * @return Vector object with all services in the WSDL file.
	 * @throws Exception
	 *             If an Exception occurs during the process. Check the
	 *             Exception message for more details.
	 */
	public static Vector createAllAxisServicesFromWSDLFile(String wsdl,
			boolean isServerSide) throws Exception {
		return createAllAxisServicesFromWSDLFile(wsdl, isServerSide, null, null);
	}

	/**
	 * /**
	 * Returns all services from the given WSDL file. The services are created
	 * from server or client point of view - this affects the input/output
	 * message creation.
	 * 
	 *
	 * 
	 * @param wsdl			
	 * 				The location to the WSDL file.
	 * @param isServerSide		
	 * 			 If true the services will be created form server point of
	 *            view. If false - from client point of view.
	 * @param username
	 * 			Username for http authentication 
	 * @param password
	 * 			Password for http authentication
	 * @return	Vector object with all services in the WSDL file.
	 * @throws Exception
	 */
	public static Vector createAllAxisServicesFromWSDLFile(String wsdl,
			boolean isServerSide, final String username, final String password) throws Exception {
		return createAllAxisServicesFromWSDLFile(wsdl, isServerSide, username, password, null);
	}

	/**
	 * Returns all services from the given WSDL file, using the supplied
	 * {@link AxisConfiguration} to prevent Axis2 from loading its embedded
	 * axis2.xml / axis2_default.xml (which may reference removed classes).
	 * Pass {@code null} to let Axis2 create its own default configuration.
	 */
	public static Vector createAllAxisServicesFromWSDLFile(String wsdl,
			boolean isServerSide, final String username, final String password,
			AxisConfiguration axisConfig) throws Exception {
		URL wsdlURL;
		File wsdlFile = new File(wsdl);
		if (wsdlFile.exists()) {
			wsdlURL = wsdlFile.toURL();
		} else {
			wsdlURL = new URL(wsdl);
		}
		/*
		 * URI does not accept non-escaped special characters such as spaces as
		 * opposed to URL. URL.toURI fails if there are spaces in the URL, so do
		 * it like this (the resulting URI will be properly encoded):
		 */
		URI wsdlURI = new URI(wsdlURL.getProtocol(), wsdlURL.getUserInfo(),
				wsdlURL.getHost(), wsdlURL.getPort(), wsdlURL.getPath(),
				wsdlURL.getQuery(), wsdlURL.getRef());
		
		if (username != null && username.length() > 0 && password != null) {
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password.toCharArray());
				}
			});
		}

		InputStream inp = getInputStream(wsdlURL, username, password);

		try {

			Vector services;
			if (isWSDL20(wsdlURL, username, password)) {
				WSDL20ToAllAxisServicesBuilder wsdlToAxisServiceBuilder = new WSDL20ToAllAxisServicesBuilder(
						inp);
				wsdlToAxisServiceBuilder.setBaseUri(wsdlURI.toASCIIString());
				wsdlToAxisServiceBuilder.setServerSide(isServerSide);
				if (axisConfig != null) {
					wsdlToAxisServiceBuilder.useAxisConfiguration(axisConfig);
				}
				services = new Vector(wsdlToAxisServiceBuilder.populateAllServices());

			} else {
				WSDL11ToAllAxisServicesBuilder wsdlToAxisServiceBuilder = new WSDL11ToAllAxisServicesBuilder(
						inp);
				wsdlToAxisServiceBuilder.setBaseUri(wsdlURI.toASCIIString());
				wsdlToAxisServiceBuilder.setServerSide(isServerSide);
				if (axisConfig != null) {
					wsdlToAxisServiceBuilder.useAxisConfiguration(axisConfig);
				}
				services = new Vector(wsdlToAxisServiceBuilder.populateAllServices());
			}

			return services;
		} finally {

			if (inp != null) {
				inp.close();
			}

			if (username != null)
				Authenticator.setDefault(null);
		}
	}

	/**
	 * Returns a inputstream depending upon the parameters passed. 
	 * If @param username and @param password not null then the values are used for authentication.
	 * @param wsdlURL
	 * @param username
	 * @param password
	 * @return
	 * @throws IOException
	 */
	private static InputStream getInputStream(URL wsdlURL, String username, String password) throws IOException {
		byte [] content = wsdlContent.get(wsdlURL);
		if (content == null || new Date().getTime() > wsdlTimeout.get(wsdlURL)) {
			storeContent(wsdlURL, username, password, null, null, null, null);
			content = wsdlContent.get(wsdlURL);
		}
		return new ByteArrayInputStream(content);
	}
	
	public static void storeContent(URL wsdlURL, String username, String password, 
			String proxyHost, Object proxyPort, final String proxyUser, final String proxyPass) throws IOException {
		
		URLConnection uc;
		long timeout = new Date().getTime() + 10000; // timeout in 10 seconds
		
		if (proxyHost != null && proxyHost.length()>0) {
			int port = 8080;
			if (proxyPort != null)
				port = Integer.valueOf(proxyPort.toString());
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, port));
			
			if (proxyUser != null && proxyUser.length() > 0) {
				Authenticator authenticator = new Authenticator() {

					public PasswordAuthentication getPasswordAuthentication() {
						return (new PasswordAuthentication(proxyUser, proxyPass.toCharArray()));
					}
				};
				Authenticator.setDefault(authenticator);
			}

			uc = wsdlURL.openConnection(proxy);
			timeout += 3600000; // add an hour to timeout when using proxy for retrieval
		} else {
			uc = wsdlURL.openConnection();
		}
		
		if (username != null && username.length() > 0) {
			String value = new StringBuffer(username).append(":").append(password).toString();
			String authorizationString = "Basic " + new String(Base64.encodeBase64(value.getBytes()));
			uc.setRequestProperty("Authorization", authorizationString);
		}
		
		InputStream is = uc.getInputStream();
		byte[] buf = new byte[1024];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int n;
		while ((n = is.read(buf)) > 0)
			bos.write(buf, 0, n);
		is.close();
		
		wsdlTimeout.put(wsdlURL, timeout);
		wsdlContent.put(wsdlURL, bos.toByteArray());
	}

	/**
	 * Creates a specified service object from the WSDL file. The service is
	 * created from server point of view - this affects the input/output message
	 * creation.
	 * 
	 * @param wsdlFile
	 *            The location to the WSDL file.
	 * @param serviceName
	 *            The service name of the service which will be created.
	 * @return A service object generated from the WSDL file.
	 * @throws Exception
	 *             If an Exception occurs during the process. Check the
	 *             Exception message for more details.
	 */
	public static AxisService createAxisServiceFromWSDLFile(String wsdlFile,
			String serviceName) throws Exception {
		return createAxisServiceFromWSDLFile(wsdlFile, serviceName, true, null, null);
	}

	/**
	 * Creates a specified service object from the WSDL file. The service is
	 * created from server or client point of view - this affects the
	 * input/output message creation.
	 * 
	 * @param wsdlFile
	 *            The location to the WSDL file.
	 * @param serviceName
	 *            The service name of the service which will be created.
	 * @param isServerSide
	 *            If true the service will be created form server point of view.
	 *            If false - from client point of view.
	 * @return A service object generated from the WSDL file.
	 * @throws Exception
	 *             If an Exception occurs during the process. Check the
	 *             Exception message for more details.
	 */
	public static AxisService createAxisServiceFromWSDLFile(String wsdlFile,
			String serviceName, boolean isServerSide) throws Exception {
		return createAxisServiceFromWSDLFile(wsdlFile, serviceName, isServerSide, null, null);
	}

	/**
	 * Creates a specified service object from the WSDL file. The service is
	 * created from server or client point of view - this affects the
	 * input/output message creation.
	 * 
	 * @param wsdlFile
	 *            The location to the WSDL file.
	 * @param serviceName
	 *            The service name of the service which will be created.
	 * @param isServerSide
	 *            If true the service will be created form server point of view.
	 *            If false - from client point of view.
	 * @return A service object generated from the WSDL file.
	 * @throws Exception
	 *             If an Exception occurs during the process. Check the
	 *             Exception message for more details.
	 */
	public static AxisService createAxisServiceFromWSDLFile(String wsdlFile,
			String serviceName, boolean isServerSide, String username, String password) throws Exception {
		return createAxisServiceFromWSDLFile(wsdlFile, serviceName, isServerSide, username, password, null);
	}

	/**
	 * Creates a specified service object from the WSDL file, using the supplied
	 * {@link AxisConfiguration} to prevent Axis2 from loading its embedded
	 * axis2.xml / axis2_default.xml (which may reference removed classes).
	 */
	public static AxisService createAxisServiceFromWSDLFile(String wsdlFile,
			String serviceName, boolean isServerSide, String username, String password,
			AxisConfiguration axisConfig) throws Exception {

		List services = createAllAxisServicesFromWSDLFile(wsdlFile, isServerSide, username, password, axisConfig);

		if (services != null) {
			for (int i = 0; i < services.size(); i++) {
				AxisService as = (AxisService) services.get(i);
				if (as.getName().equals(serviceName)) {
					return as;
				}
			}
		}

		return null;
	}

	/**
	 * Retrieves the endpoint names from the give WSDL file for the given
	 * service.
	 * 
	 * @param wsdlFile
	 *            The location to the WSDL file.
	 * @param serviceName
	 *            The service name of the service from which the endpoint will
	 *            be extracted.
	 * @return Vector with Endpoint names of the service.
	 * @throws Exception
	 *             If an Exception occurs during the process. Check the
	 *             Exception message for more details.
	 */
	public static Vector getServiceEndPointNames(String wsdlFile,
			String serviceName) throws Exception {
		AxisService service = createAxisServiceFromWSDLFile(wsdlFile,
				serviceName);
		if (service != null) {
			Iterator endpoints = service.getEndpoints().values().iterator();
			Vector result = new Vector();
			while (endpoints.hasNext()) {
				AxisEndpoint end = (AxisEndpoint) endpoints.next();
				result.add(end.getName());
			}
			return result;
		}
		return null;
	}

	/**
	 * Returns service operation names from the give WSDL file for the given
	 * service.
	 * 
	 * @param wsdlFile
	 *            The location to the WSDL file.
	 * @param serviceName
	 *            The service name of the service from which the operations will
	 *            be extracted.
	 * @return Vector with Operation names of the service.
	 * @throws Exception
	 *             If an Exception occurs during the process. Check the
	 *             Exception message for more details.
	 */
	public static Vector getServiceOperationNames(String wsdlFile,
			String serviceName) throws Exception {
		AxisService service = createAxisServiceFromWSDLFile(wsdlFile,
				serviceName);
		Iterator operations = service.getOperations();
		Vector result = new Vector();
		while (operations.hasNext()) {
			AxisOperation operation = (AxisOperation) operations.next();
			result.add(operation.getName().getLocalPart());
		}

		return result;
	}

	/**
	 * Returns the SOAP Header as TDI Hierarchical Attribute from a given SOAP
	 * Envelope.
	 * 
	 * @param envelope
	 *            The Envelope from which the headers will be extracted.
	 * @param attributeName
	 *            The name of the TDI Hierarchical Attribute to which the
	 *            Envelope Header will be written.
	 * @param e
	 *            The entry which is used to manipulate the TDI Hierarchical
	 *            Attribute.
	 * @return TDI Hierarchical Attribute which contains the SOAP Header
	 *         information.
	 */
	public static Attribute getSOAPHeader(SOAPEnvelope envelope,
			String attributeName, Entry e) {

		Attribute soapHeaderAttr = null;

		OMElement soapHeader = envelope.getHeader();
		if (soapHeader != null) {

			soapHeaderAttr = (Attribute) WSUtils.toDOM(soapHeader, e);
			e.renameNode(soapHeaderAttr, null, attributeName);
		}

		return soapHeaderAttr;
	}

	/**
	 * Sets the SOAP Envelope header to the provided TDI Hierarchical Attribute.
	 * 
	 * @param envelope
	 *            The SOAP Envelope which will be populated with the headers
	 *            information.
	 * @param soapHeaderHAttr
	 *            The TDI Hierarchical Attribute containing the SOAP Headers.
	 */
	public static void setSOAPHeader(SOAPEnvelope envelope,
			Attribute soapHeaderHAttr) {

		if (envelope == null || soapHeaderHAttr == null) {
			return;
		}

		OMElement soapHeader = WSUtils.toOM(soapHeaderHAttr);
		OMNode child = soapHeader.getFirstOMChild();
		while (child != null) {

			OMNode soapHeaderBlock = child;
			child = child.getNextOMSibling(); // get next sibling before detaching

			// move the block to the SOAP header
			soapHeaderBlock.detach();

			envelope.getHeader().addChild(soapHeaderBlock);
		}
	}

	/**
	 * Verifies that the passed Attribute has the required
	 * prefix/localName/namespace and that its child is a {@link Text} object
	 * 
	 * @param a
	 *            The TDI Attribute.
	 * @param qname
	 *            The name which will be used for the TDI Attribute if it is not
	 *            already set..
	 * @return the correct Attribute to use.
	 */
	public static Attribute verifyAttribute(Attribute a, QName qname) {

		// TODO: this field here is reserved for code that would convert an
		// Attribute to a DOM complient tree, i.e. all of the values of each
		// attribute should be of type com.ibm.di.entry.NodeImpl. Also the
		// resultant tree should be valid against the wsdl schema.

		return a;
	}
}
