/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.fc.webservice.axis2.SOAPBodyBuilder;
import com.ibm.di.fc.webservice.axis2.SOAPEnvelopeBuilder;
import com.ibm.di.fc.webservice.axis2.SOAPHeaderBuilder;
import com.ibm.di.fc.webservice.axis2.WebServiceClient;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

/**
 * The WrapSoap Function Component (FC) is part of the TDI Web Services suite.
 * This component is used to generate a complete SOAP message given the SOAP
 * Body and optionally a SOAP Header. Such a component is useful when the user
 * customizes the content of the SOAP Body or creates it completely on his own
 * (using Castor binding for example). This component will accept the contents
 * of the SOAP Body and the SOAP Header and attributes for the SOAP Envelope,
 * Header and Body XML elements (usually namespace declarations) and will create
 * the complete SOAP message. This is actually a helper FC that will save the
 * user from error-prone processing of string or DOM objects to wrap his SOAP
 * data into a complete SOAP message.
 * 
 */
public class WrapSoap extends Function {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The name of the properties file
	 */
	private static final String PROPERTIES_FILE = "wrapsoapfc";

	/**
	 * specifies the return type
	 */
	private String mReturnXMLType = null;

	/**
	 * The Return SOAP message attribute
	 */
	private String mReturnSoapMsgAttr = null;

	/**
	 * specifies the input type
	 */
	private String mInputXMLType = null;

	/**
	 * The input SOAP Body attribute
	 */
	private String mInputSoapBodyAttr = null;

	/**
	 * The input SOAP Header attribute
	 */
	private String mInputSoapHeaderAttr = null;

	/**
	 * If checked the input Header and Body must include the <Header> and <Body>
	 * tags as well as their corresponding closing tags.
	 */
	private boolean mHeaderAndBodyTagsPresent = true;

	/**
	 * Specifies attributes to add to the SOAP Envelope
	 */
	private String mSoapEnvelopeAttributes = null;

	/**
	 * Specifies Namespace declarations to add to the SOAP Envelope
	 */
	private String mSoapEnvelopeNSDecl = null;

	/**
	 * Specifies attributes to add to the SOAP Body
	 */
	private String mSoapBodyAttributes = null;

	/**
	 * Specifies Namespace declarations to add to the SOAP Body
	 */
	private String mSoapBodyNSDecl = null;

	/**
	 * Specifies attributes to add to the SOAP Header
	 */
	private String mSoapHeaderAttributes = null;

	/**
	 * Specifies Namespace declarations to add to the SOAP Header
	 */
	private String mSoapHeaderNSDecl = null;

	/**
	 * The XML return type - DOMElement or String
	 */
	private static final String PARAM_RETURN_XML_TYPE = "returnXMLType";

	/**
	 * The XML input type - DOMElement or String
	 */
	private static final String PARAM_INPUT_TYPE = "inputXMLType";

	/**
	 * The name of the attribute "headerAndBodyTagsPre"
	 */
	private static final String PARAM_HEADER_AND_BODY_TAGS_PRESENT = "headerAndBodyTagsPresent";

	/**
	 * The name of the attribute "soapEnvelopeAttributes"
	 */
	private static final String PARAM_SOAP_ENVELOPE_ATTRIBUTES = "soapEnvelopeAttributes";

	/**
	 * The name of the attribute "soapEnvelopeNSDecl"
	 */
	private static final String PARAM_SOAP_ENVELOPE_NAMESPACE_DECLARATIONS = "soapEnvelopeNSDecl";

	/**
	 * The name of the attribute "soapBodyAttributes"
	 */
	private static final String PARAM_SOAP_BODY_ATTRIBUTES = "soapBodyAttributes";

	/**
	 * The name of the attribute "soapBodyNSDecl"
	 */
	private static final String PARAM_SOAP_BODY_NAMESPACE_DECLARATIONS = "soapBodyNSDecl";

	/**
	 * The name of the attribute "soapHeaderAttributes"
	 */
	private static final String PARAM_SOAP_HEADER_ATTRIBUTES = "soapHeaderAttributes";

	/**
	 * The name of the attribute "soapHeaderNSDecl"
	 */
	private static final String PARAM_SOAP_HEADER_NAMESPACE_DECLARATIONS = "soapHeaderNSDecl";

	/**
	 * The mode is String
	 */
	private static final String XML_STRING = "String";

	/**
	 * The mode is DOMElement
	 */
	private static final String XML_DOM = "DOMElement";

	/**
	 * The name of the SOAP message string attribute
	 */
	private static final String ATTR_SOAP_MSG_STRING = "xmlString";

	/**
	 * The name of the SOAP message DOMElement attribute
	 */
	private static final String ATTR_SOAP_MSG_DOM = "xmlDOMElement";

	/**
	 * The name of the SOAP Body String attribute
	 */
	private static final String ATTR_SOAP_BODY_STRING = "soapBodyString";

	/**
	 * The name of the SOAP Body DOMElement attribute
	 */
	private static final String ATTR_SOAP_BODY_DOM = "soapBodyDOMElement";

	/**
	 * The name of the SOAP Header String attribute
	 */
	private static final String ATTR_SOAP_HEADER_STRING = "soapHeaderString";

	/**
	 * The name of the SOAP Header DOMElement attribute
	 */
	private static final String ATTR_SOAP_HEADER_DOM = "soapHeaderDOMElement";

	/**
	 * The list of delimiters between attributes
	 */
	private static final String ATTR_LIST_DELIMITERS = " ,;\r\n";

	/**
	 * The delimiter between name and value pair
	 */
	private static final String ATTR_NAME_VALUE_DELIMITER = "=";

	/**
	 * The XMLS prefix, including delimiter
	 */
	private static final String XMLNS_PREFIX = XMLConstants.XMLNS_ATTRIBUTE + ":";

	/**
	 * Double quote
	 */
	private static final String DOUBLE_QUOTE = "\"";

	/**
	 * The type is attribute
	 */
	private static final String ATTR_TYPE_ATTRIBUTE = "attribute";

	/**
	 * The type is namespace declaration
	 */
	private static final String ATTR_TYPE_NAMESPACE_DECLARATION = "namespace declaration";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Initializes the function component by using the parameters in the Config
	 * Tab.
	 * 
	 * @param obj
	 *            not used in this method
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initialize(Object obj) throws Exception {

		mReturnXMLType = (String) getParam(PARAM_RETURN_XML_TYPE);
		if (mReturnXMLType != null) {
			mReturnXMLType = mReturnXMLType.trim();
		}
		if (mReturnXMLType == null
				|| (!mReturnXMLType.equalsIgnoreCase(XML_DOM) && !mReturnXMLType
						.equalsIgnoreCase(XML_STRING))) {
			// todo: log error
			throw new Exception(sResHash.getString(
					"FC.WRAPSOAP.INVALID.VALUE.FOR.RETURN.XML.TYPE",
					new Object[] { mReturnXMLType }));
		}
		if (mReturnXMLType.equalsIgnoreCase(XML_DOM)) {
			mReturnSoapMsgAttr = ATTR_SOAP_MSG_DOM;
		} else if (mReturnXMLType.equalsIgnoreCase(XML_STRING)) {
			mReturnSoapMsgAttr = ATTR_SOAP_MSG_STRING;
		}

		mInputXMLType = (String) getParam(PARAM_INPUT_TYPE);
		if (mInputXMLType != null) {
			mInputXMLType = mInputXMLType.trim();
		}
		if (mInputXMLType == null
				|| (!mInputXMLType.equalsIgnoreCase(XML_DOM) && !mInputXMLType
						.equalsIgnoreCase(XML_STRING))) {
			// todo: log error
			throw new Exception(sResHash.getString(
					"FC.WRAPSOAP.INVALID.VALUE.FOR.INPUT.TYPE",
					new Object[] { mInputXMLType }));
		}
		if (mInputXMLType.equalsIgnoreCase(XML_DOM)) {
			mInputSoapBodyAttr = ATTR_SOAP_BODY_DOM;
			mInputSoapHeaderAttr = ATTR_SOAP_HEADER_DOM;
		} else if (mInputXMLType.equalsIgnoreCase(XML_STRING)) {
			mInputSoapBodyAttr = ATTR_SOAP_BODY_STRING;
			mInputSoapHeaderAttr = ATTR_SOAP_HEADER_STRING;
		}

		String headerAndBodyTagsPresent = (String) getParam(PARAM_HEADER_AND_BODY_TAGS_PRESENT);
		if (headerAndBodyTagsPresent == null) {
			throw new Exception(
					sResHash
							.getString("FC.WRAPSOAP.PARAMETER.HEADER.AND.BODY.TAGS.PRESENT.MISSING"));
		}
		mHeaderAndBodyTagsPresent = Boolean.valueOf(headerAndBodyTagsPresent)
				.booleanValue();

		mSoapEnvelopeAttributes = (String) getParam(PARAM_SOAP_ENVELOPE_ATTRIBUTES);
		mSoapEnvelopeNSDecl = (String) getParam(PARAM_SOAP_ENVELOPE_NAMESPACE_DECLARATIONS);
		mSoapBodyAttributes = (String) getParam(PARAM_SOAP_BODY_ATTRIBUTES);
		mSoapBodyNSDecl = (String) getParam(PARAM_SOAP_BODY_NAMESPACE_DECLARATIONS);
		mSoapHeaderAttributes = (String) getParam(PARAM_SOAP_HEADER_ATTRIBUTES);
		mSoapHeaderNSDecl = (String) getParam(PARAM_SOAP_HEADER_NAMESPACE_DECLARATIONS);

		super.initialize(null);
	}

	/**
	 * If the "Input the SOAP Body and Header as" FC parameter is String then
	 * the SOAP Body is passed in the "soapBodyString" Attribute and the SOAP
	 * Header is passed in the "soapHeaderString" Attribute. If the "Input the
	 * SOAP Body and Header as" FC parameter is DOMElement then the SOAP Body is
	 * passed in the "soapBodyDOMElement" Attribute and the SOAP Header is
	 * passed in the "soapHeaderDOMElement" Attribute.
	 * 
	 * If the "Return the SOAP message as" FC parameter is String then the
	 * complete SOAP message is returned in the "xmlString" Attribute; however
	 * if it is specified as DOMElement then the complete SOAP message is
	 * returned in the "xmlDOMElement" Attribute.
	 * 
	 * Each of the Attributes to add... parameters expects a list of XML
	 * attributes to be added to the target SOAP message element (envelope,
	 * header or body) tag in the created SOAP message. Each attribute-value
	 * pair is separated from the other attribute-value pairs by one of the
	 * following: a space, a comma, a semicolon, carriage return or a line feed.
	 * The attribute name in an attribute-value pair is separated from the
	 * attribute value by an equals sign "=".
	 * 
	 * Each of the "Namespace declarations to add to..." parameters expects a
	 * list of XML namespace declarations to be added to the SOAP message
	 * element (envelope, header or body) tag in the created SOAP message. Each
	 * namespace prefix-value pair is separated from the other namespace
	 * prefix-value pairs by one of the following: a space, a comma, a
	 * semicolon, carriage return or a line feed. The namespace prefix in a
	 * prefix-value pair is separated from the namespace value by an equals sign
	 * "=".
	 * 
	 * @param obj
	 *            Entry
	 * @return the complete SOAP message.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Object perform(Object obj) throws Exception {
		verifyInitialized();

		Object unwrappedSoapBodyObject = null;
		Object unwrappedSoapHeaderObject = null;
		final boolean useEntry = (obj instanceof Entry); 
		if (useEntry) {
			Entry e = (Entry) obj;
			Attribute attrSoapBody = e.getAttribute(mInputSoapBodyAttr);
			if (attrSoapBody != null) {
				unwrappedSoapBodyObject = attrSoapBody.getValue(0);
			} else {
				throw new Exception(sResHash.getString(
						"FC.WRAPSOAP.ENTRY.ATTRIBUTE.MISSING",
						mInputSoapBodyAttr));
			}

			Attribute attrSoapHeader = e.getAttribute(mInputSoapHeaderAttr);
			if (attrSoapHeader != null) {
				unwrappedSoapHeaderObject = attrSoapHeader.getValue(0);
			}
		} else {
			throw new Exception(sResHash
					.getString("FC.WRAPSOAP.INVALID.OBJECT.RECEIVED",
							new Object[] { obj }));
		}

		if (getDebug()) {
			debug(sResHash.getString("FC.WRAPSOAP.UNWRAPPEDSOAPBODYOBJECT",
					unwrappedSoapBodyObject));
			debug(sResHash.getString("FC.WRAPSOAP.UNWRAPPEDSOAPHEADEROBJECT",
					unwrappedSoapHeaderObject));
		}

		String soapBody = null;
		String soapHeader = null;
		try {
			if (mInputXMLType.equalsIgnoreCase(XML_DOM)) {
				if (mHeaderAndBodyTagsPresent) {
					soapBody = WebServiceClient
							.getChildNodesAsString((Node) unwrappedSoapBodyObject);
					if (unwrappedSoapHeaderObject != null) {
						soapHeader = WebServiceClient
								.getChildNodesAsString((Node) unwrappedSoapHeaderObject);
					}
				} else {
					soapBody = WebServiceClient
							.getAsString((Element) unwrappedSoapBodyObject);
					if (unwrappedSoapHeaderObject != null) {
						soapHeader = WebServiceClient
								.getAsString((Element) unwrappedSoapHeaderObject);
					}
				}
			} else if (mInputXMLType.equalsIgnoreCase(XML_STRING)) {
				if (mHeaderAndBodyTagsPresent) {
					Node node = WebServiceClient
							.getAsDOM((String) unwrappedSoapBodyObject);
					soapBody = WebServiceClient.getChildNodesAsString(node);
					if (unwrappedSoapHeaderObject != null) {
						node = WebServiceClient
								.getAsDOM((String) unwrappedSoapHeaderObject);
						soapHeader = WebServiceClient.getChildNodesAsString(node);
					}
				} else {
					soapBody = (String) unwrappedSoapBodyObject;
					soapHeader = (String) unwrappedSoapHeaderObject;
				}
			}
		} catch (ClassCastException e) {
			throw new Exception(sResHash.getString(
					"FC.WRAPSOAP.INVALID.OBJECT.RECEIVED.2", new Object[] { e
							.toString() }));
		}

		if (getDebug()) {
			debug(sResHash.getString("FC.WRAPSOAP.SOAPBODY", soapBody));
			debug(sResHash.getString("FC.WRAPSOAP.SOAP.HEADER", soapHeader));
		}

		String soapMsg = null;
		if (soapBody != null) {
			// Create SOAP envelope
			SOAPEnvelopeBuilder envelopeBuilder = new SOAPEnvelopeBuilder();
			
			// Add attributes and namespace declarations to envelope
			addAttributesAndNSDecl(envelopeBuilder, mSoapEnvelopeAttributes, ATTR_TYPE_ATTRIBUTE);
			addAttributesAndNSDecl(envelopeBuilder, mSoapEnvelopeNSDecl, ATTR_TYPE_NAMESPACE_DECLARATION);
			
			// Get body builder
			SOAPBodyBuilder bodyBuilder = envelopeBuilder.getBodyBuilder();
			
			// Add attributes and namespace declarations to body
			addAttributesAndNSDecl(bodyBuilder, mSoapBodyAttributes, ATTR_TYPE_ATTRIBUTE);
			addAttributesAndNSDecl(bodyBuilder, mSoapBodyNSDecl, ATTR_TYPE_NAMESPACE_DECLARATION);
			
			// Add body content
			try {
				// Convert body content to OMElement
				OMElement bodyElement = null;
				if (mInputXMLType.equalsIgnoreCase(XML_DOM)) {
					// Convert DOM to OMElement
					String bodyXml = WebServiceClient.getAsString((Element) unwrappedSoapBodyObject);
					StringReader reader = new StringReader(bodyXml);
					XMLInputFactory factory = XMLInputFactory.newInstance();
					XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
					bodyElement = OMXMLBuilderFactory.createStAXOMBuilder(xmlReader).getDocumentElement();
				} else {
					// Convert String to OMElement
					StringReader reader = new StringReader(soapBody);
					XMLInputFactory factory = XMLInputFactory.newInstance();
					XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
					bodyElement = OMXMLBuilderFactory.createStAXOMBuilder(xmlReader).getDocumentElement();
				}
				
				// Add body element
				bodyBuilder.addBodyElement(bodyElement);
				
				// Copy namespace declarations from body element if present
				if (mHeaderAndBodyTagsPresent) {
					if (unwrappedSoapBodyObject instanceof Node) {
						copyAttributes((Node) unwrappedSoapBodyObject, bodyBuilder);
					} else {
						Element bodyDom = WebServiceClient.getAsDOM((String) unwrappedSoapBodyObject);
						copyAttributes(bodyDom, bodyBuilder);
					}
				}
			} catch (XMLStreamException e) {
				throw new Exception("Failed to parse body content: " + e.getMessage(), e);
			}
			
			// Add header if present
			if (soapHeader != null) {
				SOAPHeaderBuilder headerBuilder = envelopeBuilder.getHeaderBuilder();
				
				// Add attributes and namespace declarations to header
				addAttributesAndNSDecl(headerBuilder, mSoapHeaderAttributes, ATTR_TYPE_ATTRIBUTE);
				addAttributesAndNSDecl(headerBuilder, mSoapHeaderNSDecl, ATTR_TYPE_NAMESPACE_DECLARATION);
				
				try {
					if (mHeaderAndBodyTagsPresent) {
						// Parse header content
						Document headerDoc;
						if (unwrappedSoapHeaderObject instanceof Node) {
							String headerXml = WebServiceClient.getAsString((Node) unwrappedSoapHeaderObject);
							headerDoc = WebServiceClient.getAsDOM(headerXml).getOwnerDocument();
						} else {
							headerDoc = WebServiceClient.getAsDOM((String) unwrappedSoapHeaderObject).getOwnerDocument();
						}
						
						// Add each child element of the header to the SOAP header
						Element root = headerDoc.getDocumentElement();
						NodeList nl = root.getChildNodes();
						for (int i = 0; i < nl.getLength(); i++) {
							if (nl.item(i) instanceof Element) {
								Element headerElement = (Element) nl.item(i);
								String headerXml = WebServiceClient.getAsString(headerElement);
								
								// Convert to OMElement
								StringReader reader = new StringReader(headerXml);
								XMLInputFactory factory = XMLInputFactory.newInstance();
								XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
								OMElement omHeaderElement = OMXMLBuilderFactory.createStAXOMBuilder(xmlReader).getDocumentElement();
								
								// Add to header
								headerBuilder.addHeaderElement(omHeaderElement);
							}
						}
						
						// Copy namespace declarations from header element
						copyAttributes(root, headerBuilder);
					} else {
						// Parse header content
						OMElement headerElement;
						if (unwrappedSoapHeaderObject instanceof Element) {
							String headerXml = WebServiceClient.getAsString((Element) unwrappedSoapHeaderObject);
							StringReader reader = new StringReader(headerXml);
							XMLInputFactory factory = XMLInputFactory.newInstance();
							XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
							headerElement = OMXMLBuilderFactory.createStAXOMBuilder(xmlReader).getDocumentElement();
						} else {
							StringReader reader = new StringReader(soapHeader);
							XMLInputFactory factory = XMLInputFactory.newInstance();
							XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
							headerElement = OMXMLBuilderFactory.createStAXOMBuilder(xmlReader).getDocumentElement();
						}
						
						// Check namespace
						if (headerElement.getNamespace() == null) {
							throw new Exception(sResHash.getString("FC.WRAPSOAP.THE.SOAP.HEADER.ELEMENT.MUST.BE"));
						}
						
						// Add to header
						headerBuilder.addHeaderElement(headerElement);
					}
				} catch (XMLStreamException e) {
					throw new Exception("Failed to parse header content: " + e.getMessage(), e);
				}
			}
			
			// Get the SOAP envelope as string
			soapMsg = envelopeBuilder.toString();
		}

		if (getDebug()) {
			debug(sResHash.getString("FC.WRAPSOAP.SOAP.MESSAGE", soapMsg));
		}

		Object result = null;
		if (mReturnXMLType.equalsIgnoreCase(XML_DOM)) {
			result = WebServiceClient.getAsDOM(soapMsg);
		} else if (mReturnXMLType.equalsIgnoreCase(XML_STRING)) {
			result = soapMsg;
		}

		Object returnObj = null;
		if (useEntry) {
			Entry resultEntry = new Entry();
			resultEntry.addAttributeValue(mReturnSoapMsgAttr, result);
			returnObj = resultEntry;
		} else {
			returnObj = result;
		}

		return returnObj;
	}

	private void copyAttributes(Node node, Object target) {
		if (node == null)
			return;
		
		NamedNodeMap map = node.getAttributes();
		if (map == null)
			return;
		
		for (int i = 0; i < map.getLength(); i++) {
			try {
				Node attrNode = map.item(i);
				String name = attrNode.getNodeName();
				if (name.startsWith(XMLNS_PREFIX)) {
					String prefix = name.substring(XMLNS_PREFIX.length());
					String uri = attrNode.getNodeValue();
					
					if (target instanceof SOAPEnvelopeBuilder) {
						((SOAPEnvelopeBuilder)target).createNamespace(uri, prefix);
					} else if (target instanceof SOAPBodyBuilder) {
						((SOAPBodyBuilder)target).declareNamespace(uri, prefix);
					} else if (target instanceof SOAPHeaderBuilder) {
						// In Axis2 1.7.6, we need to use a different approach for namespace declarations
						OMNamespace ns = ((SOAPHeaderBuilder)target).getHeader().getOMFactory().createOMNamespace(uri, prefix);
						((SOAPHeaderBuilder)target).getHeader().declareNamespace(ns);
					}
				} else {
					if (target instanceof SOAPEnvelopeBuilder) {
						// In Axis2 1.7.6, we need to create an element with the attribute
						OMElement element = ((SOAPEnvelopeBuilder)target).getEnvelope().getBody();
						element.addAttribute(name, attrNode.getNodeValue(), null);
					} else if (target instanceof SOAPBodyBuilder) {
						// In Axis2 1.7.6, we need to add the attribute to the body element
						OMElement element = ((SOAPBodyBuilder)target).getBody();
						element.addAttribute(name, attrNode.getNodeValue(), null);
					} else if (target instanceof SOAPHeaderBuilder) {
						// In Axis2 1.7.6, we need to add the attribute to the header element
						OMElement element = ((SOAPHeaderBuilder)target).getHeader();
						element.addAttribute(name, attrNode.getNodeValue(), null);
					}
				}
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
		}
	}

	/**
	 * Adds attributes or namespace declaration to the given SOAP element
	 * 
	 * @param target -
	 *            the element to which you add
	 * @param aAttrList -
	 *            the list of attribute names or namespace declarations and
	 *            their values
	 * @param aAttrType -
	 *            the type of the Attributes in the aAttrList - either
	 *            "attribute" or "namespace declaration"
	 * @throws Exception
	 */
	private void addAttributesAndNSDecl(Object target, String aAttrList,
			String aAttrType) throws Exception {
		if ((target != null) && (aAttrList != null)) {
			StringTokenizer attrListTokenizer = new StringTokenizer(aAttrList,
					ATTR_LIST_DELIMITERS);
			while (attrListTokenizer.hasMoreTokens()) {
				String attr = attrListTokenizer.nextToken();
				StringTokenizer attrNameValueTokenizer = new StringTokenizer(
						attr, ATTR_NAME_VALUE_DELIMITER);
				if (attrNameValueTokenizer.hasMoreTokens()) {
					String attrName = attrNameValueTokenizer.nextToken();
					if (attrNameValueTokenizer.hasMoreTokens()) {
						String attrValue = attrNameValueTokenizer.nextToken();
						if (attrValue.startsWith(DOUBLE_QUOTE)
								&& attrValue.endsWith(DOUBLE_QUOTE)) {
							attrValue = attrValue.substring(1, attrValue
									.length() - 1);
						}

						if (ATTR_TYPE_ATTRIBUTE.equals(aAttrType)) {
							if (target instanceof SOAPEnvelopeBuilder) {
								// In Axis2 1.7.6, we need to create an element with the attribute
								OMElement element = ((SOAPEnvelopeBuilder)target).getEnvelope();
								element.addAttribute(attrName, attrValue, null);
							} else if (target instanceof SOAPBodyBuilder) {
								// In Axis2 1.7.6, we need to add the attribute to the body element
								OMElement element = ((SOAPBodyBuilder)target).getBody();
								element.addAttribute(attrName, attrValue, null);
							} else if (target instanceof SOAPHeaderBuilder) {
								// In Axis2 1.7.6, we need to add the attribute to the header element
								OMElement element = ((SOAPHeaderBuilder)target).getHeader();
								element.addAttribute(attrName, attrValue, null);
							}
						} else if (ATTR_TYPE_NAMESPACE_DECLARATION
								.equals(aAttrType)) {
							String namespacePrefix = attrName;
							String namespaceURI = attrValue;
							if (namespacePrefix.startsWith(XMLNS_PREFIX))
								namespacePrefix = namespacePrefix.substring(XMLNS_PREFIX.length());
							
							if (target instanceof SOAPEnvelopeBuilder) {
								((SOAPEnvelopeBuilder)target).createNamespace(namespaceURI, namespacePrefix);
							} else if (target instanceof SOAPBodyBuilder) {
								((SOAPBodyBuilder)target).declareNamespace(namespaceURI, namespacePrefix);
							} else if (target instanceof SOAPHeaderBuilder) {
								// In Axis2 1.7.6, we need to use a different approach for namespace declarations
								OMNamespace ns = ((SOAPHeaderBuilder)target).getHeader().getOMFactory().createOMNamespace(namespaceURI, namespacePrefix);
								((SOAPHeaderBuilder)target).getHeader().declareNamespace(ns);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "3.0-di7.1.1 1.29 2025/10/08";
	}
}
