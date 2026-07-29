/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axis2.AxisFault;
import org.w3c.dom.Element;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.fc.webservice.axis2.SOAPBodyBuilder;
import com.ibm.di.fc.webservice.axis2.SOAPEnvelopeBuilder;
import com.ibm.di.fc.webservice.axis2.SOAPHeaderBuilder;
import com.ibm.di.fc.webservice.axis2.WSDLData;
import com.ibm.di.fc.webservice.axis2.WebServiceClient;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.server.ResourceHash;

/**
 * The Axis InvokeSoapWS Function Component (FC) is part of the TDI Web Services
 * suite. It is used to perform a web service call, given the input message for
 * the call. It has no built-in SOAP parsing functionality and can be used with
 * the Axis Soap To Java Function Component and Axis Java To Soap Function
 * Component to provide a complete web service solution. The InvokeSoapWS
 * Function Component requires a complete SOAP request message. When called with
 * a SOAP message the Function Component invokes the remote web service
 * operation with this message. The Function Component returns the SOAP response
 * message. The Function Component, however, does not perform any XML-Java
 * binding (that is, the SOAP response message is not parsed) - the Function
 * Component only returns the SOAP response message.
 * 
 */
public class InvokeSoapWS extends Function {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The name of the properties file
	 */
	private static final String PROPERTIES_FILE = "invokesoapwsfc";

	/**
	 * The WSDL URL
	 */
	private String mWsdlUrl = null;

	/**
	 * The SOAP operation to be performed
	 */
	private String mSoapOperation = null;

	/**
	 * The target URL
	 */
	private String mProviderUrl = null;

	/**
	 * The data the WSDL contains
	 */
	private WSDLData mWsdlData = null;

	/**
	 * The return XML type
	 */
	private String mReturnXMLType = null;

	/**
	 * The return attributes
	 */
	private String mReturnAttr = null;

	/**
	 * The input XML type
	 */
	private String mInputXMLType = null;

	/**
	 * The input attributes
	 */
	private String mInputAttr = null;

	/**
	 * The specified username
	 */
	private String mUsername = null;

	/**
	 * The specified password
	 */
	private String mPassword = null;

	/**
	 * whether the service is one way
	 */
	private boolean mInvokeOneWay = false;// whether the service is one way

	/**
	 * The WebServiceClient instance
	 */
	private WebServiceClient mClient = null;

	/**
	 * The WSDL URL parameter
	 */
	private static final String PARAM_WSDL_URL = "wsdlUrl";

	/**
	 * The name of the SOAP operation as described in the WSDL file
	 */
	private static final String PARAM_SOAP_OPERATION = "soapOperation";

	/**
	 * The target URL
	 */
	private static final String PARAM_PROVIDER_URL = "providerUrl";

	/**
	 * The XML return type - DOMElement or String
	 */
	private static final String PARAM_RETURN_XML_TYPE = "returnXMLType";

	/**
	 * specifies the input type
	 */
	private static final String PARAM_INPUT_TYPE = "inputType";

	/**
	 * The value of the XML String mode
	 */
	private static final String XML_STRING = "String";

	/**
	 * The value of the XML DOMElement mode
	 */
	private static final String XML_DOM = "DOMElement";

	/**
	 * The name of the XML String attribute
	 */
	private static final String ATTR_XML_STRING = "xmlString";

	/**
	 * The name of the XML DOMElement attribute
	 */
	private static final String ATTR_XML_DOM = "xmlDOMElement";

	/**
	 * The name of the SOAP body attribute
	 */
	private static final String ATTR_SOAP_BODY_STRING = "soapBodyString";

	/**
	 * The name of the SOAP body DOMElement attribute
	 */
	private static final String ATTR_SOAP_BODY_DOM = "soapBodyDOMElement";

	/**
	 * The name of the SOAP header attribute
	 */
	private static final String ATTR_SOAP_HEADER_STRING = "soapHeaderString";

	/**
	 * The name of the SOAP header DOMElement attribute
	 */
	private static final String ATTR_SOAP_HEADER_DOM = "soapHeaderDOMElement";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	private Map<String,String> httpHeaders = new HashMap<String,String>();

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

		mWsdlUrl = (String) getParam(PARAM_WSDL_URL);

		if (mWsdlUrl == null) {
			throw new Exception(sResHash
					.getString("FC.INVOKESOAPWS.PARAMETER.WSDL.URL.EMPTY"));
		} else {
			if (getDebug()) {
				debug(sResHash.getString("FC.INVOKESOAPWS.PARAMETER.WSDL.URL",
						new Object[] { mWsdlUrl }));
			}
		}
		mSoapOperation = (String) getParam(PARAM_SOAP_OPERATION);

		if (mSoapOperation == null) {
			throw new Exception(
					sResHash
							.getString("FC.INVOKESOAPWS.PARAMETER.SOAP.OPERATION.EMPTY"));
		} else {
			if (getDebug()) {
				debug(sResHash.getString(
						"FC.INVOKESOAPWS.PARAMETER.SOAP.OPERATION",
						new Object[] { mSoapOperation }));
			}
		}
		mProviderUrl = (String) getParam(PARAM_PROVIDER_URL);
		if (getDebug()) {
			debug(sResHash.getString("FC.INVOKESOAPWS.PARAMETER.PROVIDER.URL",
					new Object[] { mProviderUrl }));
		}
		mReturnXMLType = (String) getParam(PARAM_RETURN_XML_TYPE);

		if (mReturnXMLType != null) {
			mReturnXMLType = mReturnXMLType.trim();
		} else {
			if (getDebug()) {
				debug(sResHash.getString(
						"FC.INVOKESOAPWS.PARAMETER.RETURN.XML.TYPE",
						new Object[] { mReturnXMLType }));
			}
		}

		if (mReturnXMLType == null
				|| (!mReturnXMLType.equalsIgnoreCase(XML_DOM) && !mReturnXMLType
						.equalsIgnoreCase(XML_STRING))) {
			// todo: log error
			throw new Exception(sResHash.getString(
					"FC.INVOKESOAPWS.INVALID.VALUE.FOR.RETURN.XML.TYPE",
					new Object[] { mReturnXMLType }));
		}
		if (mReturnXMLType.equalsIgnoreCase(XML_DOM)) {
			mReturnAttr = ATTR_XML_DOM;
		} else if (mReturnXMLType.equalsIgnoreCase(XML_STRING)) {
			mReturnAttr = ATTR_XML_STRING;
		}

		mInputXMLType = (String) getParam(PARAM_INPUT_TYPE);
		if (getDebug()) {
			debug(sResHash.getString("FC.INVOKESOAPWS.PARAMETER.INPUT.TYPE",
					new Object[] { mInputXMLType }));
		}
		if (mInputXMLType != null) {
			mInputXMLType = mInputXMLType.trim();
		}
		if (mInputXMLType == null
				|| (!mInputXMLType.equalsIgnoreCase(XML_DOM) && !mInputXMLType
						.equalsIgnoreCase(XML_STRING))) {
			// todo: log error
			throw new Exception(sResHash.getString(
					"FC.INVOKESOAPWS.INVALID.VALUE.FOR.INPUT.TYPE",
					new Object[] { mInputXMLType }));
		}
		if (mInputXMLType.equalsIgnoreCase(XML_DOM)) {
			mInputAttr = ATTR_XML_DOM;
		} else if (mInputXMLType.equalsIgnoreCase(XML_STRING)) {
			mInputAttr = ATTR_XML_STRING;
		}

		if (getDebug()) {
			debug(sResHash.getString("FC.INVOKESOAPWS.SOAP.REQUEST.ATTRIBUTE",
					mInputAttr));
			debug(sResHash.getString("FC.INVOKESOAPWS.SOAP.RESPONSE.ATTRIBUTE",
					mReturnAttr));
		}

		String wsdlAuthUser = (String) getParam("wsdlAuthUser");
		String wsdlAuthPass = (String) getParam("wsdlAuthPass");

		if (wsdlAuthUser != null && wsdlAuthUser.trim().length() > 0) {
			// Create WebServiceClient with authentication for WSDL
			mClient = WebServiceClient.createWebServiceClient(mWsdlUrl, null, null, 
					mSoapOperation, wsdlAuthUser, wsdlAuthPass, logger);
		} else {
			// Create WebServiceClient without authentication
			mClient = WebServiceClient.createWebServiceClient(mWsdlUrl, null, null, 
					mSoapOperation, logger);
		}

		// Get WSDL data
		mWsdlData = WSDLData.getWsdlData(mWsdlUrl, mSoapOperation);

		if (mProviderUrl == null || mProviderUrl.trim().length() == 0) {
			mProviderUrl = mWsdlData.getLocationUrl();
		}

		mInvokeOneWay = mWsdlData.isOperationOneWay();
		if (getDebug()) {
			debug(sResHash.getString("FC.INVOKESOAPWS.SOAP.INVOKEONEWAY",
					Boolean.valueOf(mInvokeOneWay)));
		}

		mUsername = (String) getParam("Username");
		if (mUsername != null && mUsername.length() > 0) {
			mPassword = (String) getParam("Password");
			setAuthorization(UserFunctions.base64Encode((mUsername + ":" + mPassword).getBytes("ISO-8859-1")));
		}

		// Set endpoint URL
		mClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(mProviderUrl));

		super.initialize(null);
	}

	/**
	 * Makes a web service call by sending a SOAP request message and receiving
	 * a SOAP response message.
	 * 
	 * If an Entry was passed to the FC, then if the value of the "Return the
	 * SOAP message as" FC parameter is String then the SOAP response message is
	 * stored in the "xmlString" Attribute; however, If the value of the "Return
	 * the SOAP message as" FC parameter is DOMElement then the SOAP response
	 * message is stored in the "xmlDOMElement" Attribute.
	 * 
	 * Additionally, if this FC was passed an Entry object, then the FC stores
	 * the SOAP response Header and SOAP response Body (apart from the entire
	 * SOAP response message) as Attributes in the returned Entry. If the value
	 * of the "Return the SOAP message as" FC parameter is String then the SOAP
	 * Header and Body are stored in the "soapHeaderString" and "soapBodyString"
	 * Attributes respectively as java.lang.String objects. If the value of the
	 * "Return the SOAP message as" FC parameter is DOMElement then the SOAP
	 * Header and Body are stored in the "soapHeaderDOMElement" and
	 * "soapBodyDOMElement" Attributes respectively as org.w3c.dom.Element
	 * objects.
	 * 
	 * If a non-Entry object was passed to this FC, then the return value of the
	 * FC is either a java.lang.String object (when the value of the Return the
	 * SOAP message as FC parameter is String) or an org.w3c.dom.Element object
	 * (when the value is DOMElement).
	 * 
	 * @param obj
	 * @return a Java object
	 * @throws Exception
	 */
	public Object perform(Object obj) throws Exception {
		verifyInitialized();

		Object unwrappedObject = obj;
		if (obj instanceof Entry) {
			Entry e = (Entry) obj;
			Attribute attrSoap = e.getAttribute(mInputAttr);
			if (attrSoap != null) {
				unwrappedObject = attrSoap.getValue(0);
			} else {
				throw new Exception(sResHash.getString(
						"FC.INVOKESOAPWS.ENTRY.ATTRIBUTE.MISSING", mInputAttr));
			}
		}

		String soapRequest = null;
		try {
			if (mInputXMLType.equalsIgnoreCase(XML_DOM)) {
				soapRequest = WebServiceClient.getAsString((Element) unwrappedObject);
			} else if (mInputXMLType.equalsIgnoreCase(XML_STRING)) {
				soapRequest = (String) unwrappedObject;
			}
		} catch (ClassCastException e) {
			throw new Exception(sResHash.getString(
					"FC.INVOKESOAPWS.INVALID.OBJECT", this.toString()));
		}

		if (soapRequest == null) {
			throw new Exception(sResHash
					.getString("FC.INVOKESOAPWS.SOAPREQUEST.MISSING"));
		}

		// The soapActionURI should have quotes, refer WI DI01825 
		String soapActionURI = addQuotes(mWsdlData.getSoapActionURI());			

		if (getDebug()) {
			debug(sResHash.getString(
					"FC.INVOKESOAPWS.ABOUT.TO.CALL.WEB.SERVICE.AT",
					mProviderUrl));
			debug(sResHash.getString("FC.INVOKESOAPWS.SOAP.ACTION", soapActionURI));
			debug(sResHash.getString("FC.INVOKESOAPWS.SOAP.REQUEST",
					soapRequest));
		}

		// Set SOAP Action
		mClient.getOptions().setAction(soapActionURI);

		// Set HTTP headers
		if (!httpHeaders.isEmpty()) {
			mClient.getOptions().setProperty("HTTP_HEADERS", httpHeaders);
		}

		// Use WebServiceClient.callSoap for direct SOAP message invocation
		// This bypasses the invoke() method which expects operation parameters, not a full SOAP envelope
		String soapResponse = null;
		try {
			if (mInvokeOneWay) {
				// For one-way operations, just send the message
				WebServiceClient.callSoap(mProviderUrl, soapRequest, soapActionURI, httpHeaders);
				soapResponse = "";
			} else {
				// For request-response operations, get the response
				soapResponse = WebServiceClient.callSoap(mProviderUrl, soapRequest, soapActionURI, httpHeaders);
			}
		} catch (AxisFault e) {
			// Handle SOAP faults
			SOAPEnvelopeBuilder envelopeBuilder = new SOAPEnvelopeBuilder();
			SOAPHeaderBuilder headerBuilder = envelopeBuilder.getHeaderBuilder();
			SOAPBodyBuilder bodyBuilder = envelopeBuilder.getBodyBuilder();
			
			// Add fault to body
			bodyBuilder.createFault(e.getFaultCode().getLocalPart(), e.getMessage());
			
			// Get the SOAP envelope as string
			soapResponse = envelopeBuilder.toString();
		}

		logmsg(sResHash.getString(
				"FC.INVOKESOAPWS.CALLED.SUCCESSFULLY.WEB.SERVICE.AT.URL",
				mProviderUrl));

		if (getDebug()) {
			debug(sResHash.getString("FC.INVOKESOAPWS.SOAP.RESPONSE",
					soapResponse));
		}

		boolean returnObjAsEntry = (obj instanceof Entry);
		Object returnObj = mInvokeOneWay ? prepareReturnObjForOneWayWS(
				soapResponse, returnObjAsEntry) : prepareReturnObjForTwoWayWS(
				soapResponse, returnObjAsEntry);

		return returnObj;
	}

	private void setAuthorization(String base64Encode) {
		setHttpHeader("authorization", "Basic " + base64Encode);
	}

	/*
	 * Add quotes to the SOAPAction
	 */
	private String addQuotes(String s) {
		if (s == null)
			return "";
		if (s.startsWith("\""))
			return s;

		return "\"" + s + "\"";		
	}
	 	
	/**
	 * Prepares a proper return Object for the use of the 'perform' method. It
	 * is supposed to be used with two way (request/response) web services.
	 * 
	 * @param soapResponse
	 *            soap response
	 * @param returnAsEntry
	 *            should the result be an Entry
	 * @return String, Element or Entry object
	 * @throws Exception
	 */
	private Object prepareReturnObjForTwoWayWS(String soapResponse,
			boolean returnAsEntry) throws Exception {
		Object result = null;
		if (mReturnXMLType.equalsIgnoreCase(XML_DOM)) {
			result = WebServiceClient.getAsDOM(soapResponse);
		} else if (mReturnXMLType.equalsIgnoreCase(XML_STRING)) {
			result = soapResponse;
		}

		Object returnObj = null;
		if (returnAsEntry) {
			Entry resultEntry = new Entry();
			resultEntry.addAttributeValue(mReturnAttr, result);
			java.util.Vector<?> v = WebServiceClient.getSoapHeaderAndBodyAsDOM(soapResponse);
			if (v.size() > 0) {
				Element soapResponseBody = (Element) v.get(0);
				Element soapResponseHeader = null;
				if (v.size() > 1) {
					soapResponseHeader = (Element) v.get(1);
					if (getDebug()) {
						debug(sResHash.getString(
								"FC.INVOKESOAPWS.SOAP.RESPONSE.HEADER",
								WebServiceClient.getAsString(soapResponseHeader)));
					}
				} else {
					if (getDebug()) {
						debug(sResHash.getString("FC.INVOKESOAPWS.SOAPBODY"));
					}
				}

				if (getDebug()) {
					debug(sResHash.getString(
							"FC.INVOKESOAPWS.SOAP.RESPONSE.BODY",
							WebServiceClient.getAsString(soapResponseBody)));
				}

				if (mReturnXMLType.equalsIgnoreCase(XML_DOM)) {
					resultEntry.addAttributeValue(ATTR_SOAP_BODY_DOM,
							soapResponseBody);
					if (soapResponseHeader != null) {
						resultEntry.addAttributeValue(ATTR_SOAP_HEADER_DOM,
								soapResponseHeader);
					}
				} else if (mReturnXMLType.equalsIgnoreCase(XML_STRING)) {
					resultEntry.addAttributeValue(ATTR_SOAP_BODY_STRING,
							WebServiceClient.getAsString(soapResponseBody));
					if (soapResponseHeader != null) {
						resultEntry.addAttributeValue(ATTR_SOAP_HEADER_STRING,
								WebServiceClient.getAsString(soapResponseHeader));
					}
				}
			}
			returnObj = resultEntry;
		} else {
			returnObj = result;
		}

		return returnObj;
	}

	/**
	 * Prepares a proper return Object for the use of the 'perform' method. It
	 * is supposed to be used with one way (no response) web services. At the
	 * moment, the method returns just an empty Entry object if returnAsEntry is
	 * true, or null if false.
	 * 
	 * @param soapResponse
	 * @param returnAsEntry
	 *            if <code>true</code> returns Entry object; otherwise
	 *            <code>null</code>
	 * @return Entry object or null
	 */
	private Object prepareReturnObjForOneWayWS(String soapResponse,
			boolean returnAsEntry) {
		return returnAsEntry ? new Entry() : null;
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "3.0-di11.0.0.1 2025/10/08";
	}
	
	/**
	 * Return the HTTP headers.
	 */
	public Map<String, String> getHttpHeaders() {
		return httpHeaders;
	}
	
	/**
	 * Use this method to set a custom HTTP header.
	 * @param key
	 * @param value
	 */
	public void setHttpHeader(String key, String value) {
		httpHeaders.put(key.toLowerCase(Locale.ENGLISH), value);
	}
	
	/**
	 * Cleanup resources when this function component is no longer needed.
	 */
	@Override
	public void terminate() throws Exception {
		if (mClient != null) {
			try {
				mClient.close();
				mClient = null;
			} catch (Exception e) {
				if (logger != null) {
					logger.logerror("Error closing WebServiceClient", e);
				}
			}
		}
		super.terminate();
	}
}
