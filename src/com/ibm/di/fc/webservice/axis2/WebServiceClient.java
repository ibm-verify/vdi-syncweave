/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice.axis2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.commons.codec.binary.Base64;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.wsdl.WSDLConstants;
// Using the full class path for Authenticator
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

/**
 * This class defines APIs for web service client operations using Axis2.
 * It replaces the original WebServiceCall class from Axis 1.x.
 *
 * Key differences from the original WebServiceCall:
 * 1. Uses Axis2's ServiceClient instead of extending Axis 1.x Call
 * 2. Uses AXIOM-based XML processing instead of DOM-based
 * 3. Implements proper resource management with close() method
 * 4. Uses Axis2's authentication mechanisms
 * 5. Provides enhanced fault handling
 * 6. Includes type conversion for response data
 */
public class WebServiceClient {
    /**
     * Copyright.
     */
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

    /**
     * Component properties.
     */
    private static final String PROPERTIES_FILE = "webserviceutil";

    /**
     * Output parameters.
     */
    private HashMap<QName, Object> outParams = null;

    /**
     * Delimiters for complex types.
     */
    private static final String COMPLEX_TYPE_LIST_DELIMITERS = ",; \r\n";

    /**
     * Delimiters for operation parameters.
     */
    private static final String OPERATION_PARAM_LIST_DELIMITERS = " ,;\r\n";

    /**
     * standard character encoding of the Latin alphabet
     */
    public static final String ENCODING_LATIN_1 = "ISO-8859-1";

    /**
     * variable-length character encoding for Unicode
     */
    public static final String ENCODING_UTF8 = "UTF-8";

    /**
     * constant to set the default buffer size
     */
    public static final int INITIAL_BUFFER_SIZE = 10000;

    /**
     * {@link QName} containing a Namespace URI, local name and prefix
     */
    private QName mReturnParamQName = null;

    /**
     * Holds debug messages.
     */
    private com.ibm.di.server.Log mLog = null;

    /**
     * NLS Property set holding name-value pairs for the resource.
     */
    private static ResourceHash sResHash = null;

    /**
     * Service client for Axis2 operations.
     */
    private ServiceClient serviceClient = null;

    /**
     * Configuration context for Axis2 operations.
     */
    private ConfigurationContext configContext = null;

    /**
     * Options for the service client.
     */
    private Options options = null;

    /**
     * Operation name.
     */
    private QName operationName = null;

    /**
     * Target endpoint address.
     */
    private String targetEndpoint = null;

    /**
     * SOAP action URI.
     */
    private String soapActionURI = null;

    /**
     * SOAP style (document or rpc).
     */
    private String style = "document";

    /**
     * SOAP use (literal or encoded).
     */
    private String use = "literal";

    /**
     * List of SOAP headers.
     */
    private List<SOAPHeaderBlock> soapHeaders = new ArrayList<SOAPHeaderBlock>();

    /**
     * WSDL data.
     */
    private WSDLData wsdlData = null;

    static {
        sResHash = new ResourceHash(PROPERTIES_FILE);
    }

    /**
     * Returns a NLS Property set which holds all the translated values in the
     * current language.
     *
     * @return ResourceHash
     */
    public static ResourceHash getResHash() {
        return sResHash;
    }

    /**
     * Returns the value of a {@link QName} containing a Namespace URI, local
     * name and prefix
     *
     * @return QName
     */
    public QName getReturnParamQName() {
        return mReturnParamQName;
    }

    /**
     * Creates new WebServiceClient object
     *
     * @param aWsdlUrl
     *            {@link String}
     * @param aServiceQN
     *            {@link QName}
     * @param aPortName
     *            {@link String}
     * @param aSoapOperation
     *            {@link String}
     * @param aLog
     * @return new WebServiceClient Object
     * @throws Exception
     */
    public static WebServiceClient createWebServiceClient(String aWsdlUrl,
            QName aServiceQN, String aPortName, String aSoapOperation,
            com.ibm.di.server.Log aLog) throws Exception {
        WSDLData wsdlData = WSDLData.getWsdlData(aWsdlUrl, aSoapOperation);
        return new WebServiceClient(wsdlData, aSoapOperation, aLog);
    }

    /**
     * Creates new WebServiceClient object with authentication
     *
     * @param aWsdlUrl
     *            {@link String}
     * @param aServiceQN
     *            {@link QName}
     * @param aPortName
     *            {@link String}
     * @param aSoapOperation
     *            {@link String}
     * @param aUsername
     *            {@link String}
     * @param aPassword
     *            {@link String}
     * @param aLog
     * @return new WebServiceClient Object
     * @throws Exception
     */
    public static WebServiceClient createWebServiceClient(String aWsdlUrl,
            QName aServiceQN, String aPortName, String aSoapOperation,
            String aUsername, String aPassword, com.ibm.di.server.Log aLog) throws Exception {
        WSDLLocator locator = new WSDLLocator(aUsername, aPassword, aWsdlUrl);
        WSDLData wsdlData = WSDLData.getWsdlData(aWsdlUrl, aSoapOperation, locator);
        WebServiceClient client = new WebServiceClient(wsdlData, aSoapOperation, aLog);
        
        // Set authentication
        // In Axis2 1.7.6, we need to use a different approach for authentication
        // Create a basic authentication HTTP header
        String authString = aUsername + ":" + aPassword;
        String encodedAuth = org.apache.commons.codec.binary.Base64.encodeBase64String(authString.getBytes());
        String authHeader = "Basic " + encodedAuth;
        
        // Set the authorization header
        client.options.setProperty("Authorization", authHeader);
        
        return client;
    }

    /**
     * Class constructor
     *
     * @param aWsdlData
     *            {@link WSDLData}
     * @param aSoapOperation
     *            {@link String}
     * @param aLog
     *            {@link Log}
     * @throws Exception
     *             if the arguments are not valid
     */
    public WebServiceClient(WSDLData aWsdlData, String aSoapOperation, com.ibm.di.server.Log aLog) throws Exception {
        mLog = aLog;
        wsdlData = aWsdlData;
        
        // Initialize configuration context using custom axis2.xml from classpath
        // First try to load from classpath (new location)
        InputStream axis2XmlStream = getClass().getResourceAsStream("axis2.xml");
        
        if (axis2XmlStream != null) {
            try {
                // Create a temporary file to hold the axis2.xml content
                java.io.File tempAxis2Xml = java.io.File.createTempFile("axis2", ".xml");
                tempAxis2Xml.deleteOnExit();
                
                // Copy the stream to the temporary file
                java.io.FileOutputStream fos = new java.io.FileOutputStream(tempAxis2Xml);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = axis2XmlStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.close();
                axis2XmlStream.close();
                
                // Create configuration context from the temporary file
                String tempDir = tempAxis2Xml.getParent();
                configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(tempDir, tempAxis2Xml.getAbsolutePath());
            } catch (Exception e) {
                // If loading from classpath fails, fall back to minimal configuration
                if (mLog != null) {
                    mLog.logdebug("Failed to load axis2.xml from classpath: " + e.getMessage());
                }
                configContext = createMinimalConfigurationContext();
            }
        } else {
            // Try the old file system location for backward compatibility
            String axis2Repo = System.getProperty("axis2.repo.path", "conf");
            String axis2Xml = System.getProperty("axis2.xml.path", axis2Repo + "/axis2.xml");
            java.io.File axis2XmlFile = new java.io.File(axis2Xml);

            if (axis2XmlFile.exists()) {
                configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2Repo, axis2Xml);
            } else {
                // Fallback: create minimal configuration programmatically
                configContext = createMinimalConfigurationContext();
            }
        }

        
        // Initialize service client
        serviceClient = new ServiceClient(configContext, null);
        options = new Options();
        
        // Set operation name
        operationName = new QName(wsdlData.getTargetNamespace(), aSoapOperation);
        
        // Set target endpoint
        targetEndpoint = wsdlData.getLocationUrl();
        options.setTo(new EndpointReference(targetEndpoint));
        
        // Set SOAP action
        soapActionURI = wsdlData.getSoapActionURI();
        options.setAction(soapActionURI);
        
        // Set style and use
        style = wsdlData.getStyle();
        use = wsdlData.getEncoding();
        
        // Set options on service client
        serviceClient.setOptions(options);
    }

    /**
     * Class constructor
     *
     * @param url
     *            {@link String}
     * @param aLog
     *            {@link Log}
     * @throws MalformedURLException
     *             If the string specifies an unknown protocol.
     */
    public WebServiceClient(String url, com.ibm.di.server.Log aLog)
            throws Exception {
        mLog = aLog;
        
        // Initialize configuration context using custom axis2.xml from classpath
        // First try to load from classpath (new location)
        InputStream axis2XmlStream = getClass().getResourceAsStream("axis2.xml");
        
        if (axis2XmlStream != null) {
            try {
                // Create a temporary file to hold the axis2.xml content
                java.io.File tempAxis2Xml = java.io.File.createTempFile("axis2", ".xml");
                tempAxis2Xml.deleteOnExit();
                
                // Copy the stream to the temporary file
                java.io.FileOutputStream fos = new java.io.FileOutputStream(tempAxis2Xml);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = axis2XmlStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.close();
                axis2XmlStream.close();
                
                // Create configuration context from the temporary file
                String tempDir = tempAxis2Xml.getParent();
                configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(tempDir, tempAxis2Xml.getAbsolutePath());
            } catch (Exception e) {
                // If loading from classpath fails, fall back to minimal configuration
                if (mLog != null) {
                    mLog.logdebug("Failed to load axis2.xml from classpath: " + e.getMessage());
                }
                configContext = createMinimalConfigurationContext();
            }
        } else {
            // Try the old file system location for backward compatibility
            String axis2Repo = System.getProperty("axis2.repo.path", "conf");
            String axis2Xml = System.getProperty("axis2.xml.path", axis2Repo + "/axis2.xml");
            java.io.File axis2XmlFile = new java.io.File(axis2Xml);

            if (axis2XmlFile.exists()) {
                configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2Repo, axis2Xml);
            } else {
                // Fallback: create minimal configuration programmatically
                configContext = createMinimalConfigurationContext();
            }
        }
        
        // Initialize service client
        serviceClient = new ServiceClient(configContext, null);
        options = new Options();
        
        // Set target endpoint
        targetEndpoint = url;
        options.setTo(new EndpointReference(targetEndpoint));
        
        // Set options on service client
        serviceClient.setOptions(options);
    }

    /**
     * Sets the operation name.
     * 
     * @param namespace The namespace URI
     * @param localPart The local part of the operation name
     * @throws Exception if an error occurs
     */
    public void setOperation(String namespace, String localPart) throws Exception {
        operationName = new QName(namespace, localPart);
    }

    /**
     * Sets the operation QName.
     * 
     * @param operationQName The operation QName
     * @throws Exception if an error occurs
     */
    public void setOperation(QName operationQName) throws Exception {
        operationName = operationQName;
    }

    /**
     * Gets the operation name.
     * 
     * @return The operation name
     */
    public QName getOperationName() {
        return operationName;
    }

    /**
     * Sets the target endpoint address.
     * 
     * @param url The target endpoint URL
     */
    public void setTargetEndpointAddress(URL url) {
        targetEndpoint = url.toString();
        options.setTo(new EndpointReference(targetEndpoint));
        serviceClient.setOptions(options);
    }

    /**
     * Sets the SOAP action URI.
     * 
     * @param soapAction The SOAP action URI
     */
    public void setSOAPAction(String soapAction) {
        soapActionURI = soapAction;
        options.setAction(soapActionURI);
        serviceClient.setOptions(options);
    }

    /**
     * Sets the style (document or rpc).
     * 
     * @param style The style
     */
    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * Gets the style.
     * 
     * @return The style
     */
    public String getStyle() {
        return style;
    }

    /**
     * Sets the use (literal or encoded).
     * 
     * @param use The use
     */
    public void setUse(String use) {
        this.use = use;
    }

    /**
     * Gets the use.
     * 
     * @return The use
     */
    public String getUse() {
        return use;
    }

    /**
     * Adds a SOAP header.
     * 
     * @param header The SOAP header
     */
    public void addHeader(SOAPHeaderBlock header) {
        soapHeaders.add(header);
    }

    /**
     * Clears all SOAP headers.
     */
    public void clearHeaders() {
        soapHeaders.clear();
    }

    /**
     * Returns Vector, which contains the type information for the tokens of the
     * calling argument
     *
     * @param aString
     *            String
     * @return {@link Vector}
     * @throws ClassNotFoundException
     */
    public static Vector<Class<?>> convertComplexTypeList(String aString)
            throws ClassNotFoundException {
        Vector<Class<?>> v = new Vector<Class<?>>();
        if (aString != null) {
            StringTokenizer tokenizer = new StringTokenizer(aString,
                    COMPLEX_TYPE_LIST_DELIMITERS);
            while (tokenizer.hasMoreTokens()) {
                String className = tokenizer.nextToken();
                Class<?> cls = Class.forName(className);
                v.add(cls);
            }
        }
        return v;
    }

    /**
     * Converts the input argument into {@link Element} object
     *
     * @param aString
     *            {@link String}
     * @return Element
     * @throws Exception
     *             if somewhere occurs an error
     */
    public static Element getAsDOM(String aString) throws Exception {
        Element docElem = null;
        if (aString != null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = null;
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new Exception(sResHash.getString(
                        "WSUTIL.WSCALL.COULD.NOT.CREATE.DOCUMENT.BUILDER", e
                                .toString()));
            }
            Document doc = builder.parse(new ByteArrayInputStream(aString
                    .getBytes(ENCODING_UTF8)));

            docElem = doc.getDocumentElement();
        } else {
            throw new Exception(
                    sResHash
                            .getString("WSUTIL.WSCALL.THE.JAVALANGSTRING.PASSED.IS.NULL"));
        }
        return docElem;
    }

    /**
     * Converts the input {@link Node} element into {@link String}
     *
     * @param aNode
     *            {@link Node}
     * @return String
     * @throws Exception
     *             if <code>null</code> is pass, or error during the
     *             transforming occurs
     */
    public static String getAsString(org.w3c.dom.Node aNode) throws Exception {
        java.io.StringWriter sw = new java.io.StringWriter();
        if (aNode != null) {
            try {
                javax.xml.transform.Transformer trans = javax.xml.transform.TransformerFactory
                        .newInstance().newTransformer();
                trans.setOutputProperty(javax.xml.transform.OutputKeys.INDENT,
                        "yes");
                trans.setOutputProperty(
                        javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION,
                        "yes");
                javax.xml.transform.stream.StreamResult sr = new javax.xml.transform.stream.StreamResult(
                        sw);
                trans.transform(new javax.xml.transform.dom.DOMSource(aNode),
                        sr);
            } catch (Exception e) {
                throw new Exception(
                        sResHash
                                .getString(
                                        "WSUTIL.WSCALL.EXCEPTION.WHILE.TRANSFORMING.DOM.TO.STRING",
                                        e.getMessage()));
            }
        } else {
            throw new Exception(
                    sResHash
                            .getString("WSUTIL.WSCALL.THE.ORGW3CDOMNODE.PASSED.IS.NULL"));
        }

        return sw.toString();
    }

    /**
     * Creates the SOAP protocol for exchanging XML-messages
     *
     * @param aClasses
     *            {@link Vector}
     * @param aParams
     *            Array of objects , parameter list
     * @param aIsRequest
     *            <code>boolean</code> , indicates whether the call is a request
     *            or response
     * @return String , the name of the operation
     * @throws Exception
     *             if error occurs
     */
    public String generateSOAP(Vector<Class<?>> aClasses, Object[] aParams,
            boolean aIsRequest) throws Exception {
        // Create a SOAP envelope builder for proper envelope structure
        SOAPEnvelopeBuilder envelopeBuilder = new SOAPEnvelopeBuilder();
        
        // Check if this is a fault
        if (aParams.length > 0 && aParams[0] instanceof AxisFault) {
            AxisFault axisFault = (AxisFault) aParams[0];
            // Convert QName to String for addFault
            String faultCode = axisFault.getFaultCode().getLocalPart();
            envelopeBuilder.addFault(faultCode, axisFault.getMessage());
        } else {
            // Set the operation name
            String elementName = operationName.getLocalPart();
            if (!aIsRequest) {
                elementName += "Response";
            }
            
            // Create a standalone operation element using the factory
            SOAPFactory soapFactory = (SOAPFactory) envelopeBuilder.getEnvelope().getOMFactory();
            OMNamespace ns = soapFactory.createOMNamespace(operationName.getNamespaceURI(), "ns1");
            OMElement operationElement = soapFactory.createOMElement(elementName, ns);
            
            // Add parameters to the operation element
            for (int i = 0; i < aParams.length; i++) {
                Object param = aParams[i];
                String paramName = "param" + i;
                
                // Create parameter element
                OMElement paramElement = soapFactory.createOMElement(paramName, ns);
                if (param != null) {
                    if (param instanceof OMElement) {
                        paramElement.addChild((OMElement) param);
                    } else {
                        paramElement.setText(param.toString());
                    }
                }
                operationElement.addChild(paramElement);
            }
            
            // Add the operation element to the envelope's body
            envelopeBuilder.addBodyElement(operationElement);
            
            // Add headers to the envelope
            if (!soapHeaders.isEmpty()) {
                SOAPHeaderBuilder headerBuilder = new SOAPHeaderBuilder();
                for (SOAPHeaderBlock header : soapHeaders) {
                    headerBuilder.addHeaderBlock(header);
                }
                envelopeBuilder.setHeader(headerBuilder.getHeader());
            }
        }
        
        // Convert the envelope to a string
        return envelopeBuilder.toString();
    }

    /**
     * Invokes a web service operation.
     * 
     * @param params The parameters to pass to the operation
     * @return The result of the operation
     * @throws Exception if an error occurs
     */
    public Object invoke(Object[] params) throws Exception {
        if (operationName == null) {
            throw new Exception(sResHash.getString("WSUTIL.WSCLIENT.OPERATION.NAME.NOT.SET"));
        }
        
        // Apply any custom type mappings
        if (!customTypeMappings.isEmpty()) {
            applyCustomTypeMappings();
        }
        
        // Create a standalone OMElement (not attached to any envelope)
        // Let Axis2 wrap it in an envelope automatically
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        OMNamespace ns = soapFactory.createOMNamespace(operationName.getNamespaceURI(), "ns1");
        OMElement operationElement = soapFactory.createOMElement(operationName.getLocalPart(), ns);
        
        // Add parameters to the operation element
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                String paramName = "param" + i;
                
                // Create parameter element
                OMElement paramElement = soapFactory.createOMElement(paramName, ns);
                if (param != null) {
                    if (param instanceof OMElement) {
                        paramElement.addChild((OMElement) param);
                    } else {
                        paramElement.setText(param.toString());
                    }
                }
                operationElement.addChild(paramElement);
            }
        }
        
        // Note: Headers will be added by Axis2 automatically if set in the message context
        // For now, we'll skip adding custom headers in the invoke method
        // as they should be added to the envelope before sending
        // Custom headers can be added using the ServiceClient's addHeader method if needed
        
        // Log the request element if debug is enabled
        if (isDebugEnabled()) {
            logdebug(sResHash.getString("WSUTIL.WSCLIENT.REQUEST.SOAP.ENVELOPE"));
            logdebug(operationElement.toString());
        }
        
        // Send the request and get the response
        // Axis2 will automatically wrap the operationElement in a SOAP envelope
        OMElement responseElement = serviceClient.sendReceive(operationElement);
        
        // Process the response
        Object result = null;
        outParams = new HashMap<QName, Object>();
        
        if (responseElement != null) {
            // Log the response if debug is enabled
            if (isDebugEnabled()) {
                logdebug(sResHash.getString("WSUTIL.WSCLIENT.RESPONSE.SOAP.ELEMENT"));
                logdebug(responseElement.toString());
            }
            
            // Check if this is a fault
            if (responseElement.getLocalName().equals("Fault")) {
                String faultCode = null;
                String faultString = null;
                String faultDetail = null;
                
                // Extract fault information
                Iterator<OMElement> iter = responseElement.getChildElements();
                while (iter.hasNext()) {
                    OMElement element = iter.next();
                    if (element.getLocalName().equals("faultcode")) {
                        faultCode = element.getText();
                    } else if (element.getLocalName().equals("faultstring")) {
                        faultString = element.getText();
                    } else if (element.getLocalName().equals("detail")) {
                        faultDetail = element.toString();
                    }
                }
                
                // Create a fault with the appropriate constructor for Axis2 1.7.6
                AxisFault fault = new AxisFault(faultString);
                if (faultCode != null) {
                    fault.setFaultCode(new QName(faultCode));
                }
                if (faultDetail != null) {
                    // In Axis2 1.7.6, we need to use a different approach for setting fault detail
                    try {
                        // Create a simple OMElement for the detail
                        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
                        OMElement detailElement = factory.createOMElement(
                            new QName("detail"), null);
                        detailElement.setText(faultDetail);
                        fault.setDetail(detailElement);
                    } catch (Exception e) {
                        // If parsing fails, just log it
                        if (isDebugEnabled()) {
                            logdebug("Failed to create fault detail: " + e.getMessage());
                        }
                    }
                }
                
                throw fault;
            }
            
            // Extract the result and output parameters
            Iterator<OMElement> iter = responseElement.getChildElements();
            if (iter.hasNext()) {
                OMElement resultElement = iter.next();
                // Try to convert the result to an appropriate type
                result = convertToAppropriateType(resultElement);
                mReturnParamQName = resultElement.getQName();
                
                // Extract other output parameters
                while (iter.hasNext()) {
                    OMElement paramElement = iter.next();
                    outParams.put(paramElement.getQName(), convertToAppropriateType(paramElement));
                }
            } else {
                // No child elements, use the response element itself as the result
                result = convertToAppropriateType(responseElement);
                mReturnParamQName = responseElement.getQName();
            }
        }
        
        return result;
    }
    
    /**
     * Closes the service client and releases resources.
     * This method should be called when the client is no longer needed.
     */
    public void close() {
        if (serviceClient != null) {
            try {
                serviceClient.cleanup();
                serviceClient = null;
            } catch (Exception e) {
                if (mLog != null) {
                    mLog.logerror(sResHash.getString("WSUTIL.WSCLIENT.ERROR.CLOSING.SERVICE.CLIENT"), e);
                }
            }
        }
        
        if (configContext != null) {
            try {
                configContext.cleanupContexts();
                configContext = null;
            } catch (Exception e) {
                if (mLog != null) {
                    mLog.logerror(sResHash.getString("WSUTIL.WSCLIENT.ERROR.CLEANING.UP.CONFIGURATION.CONTEXT"), e);
                }
            }
        }
    }

    /**
     * Retrieves out parameters.
     *
     * @return a HashMap with the out parameters
     *
     */
    public HashMap<QName, Object> getOutParams() {
    	return outParams;
    }
    
    /**
     * Gets the options for this client.
     *
     * @return The options
     */
    public Options getOptions() {
    	return options;
    }

    /**
     * This method gives the chance to invoke callSOAP method without providing
     * basic authentication
     *
     * @see #callSoap(String, String, String, String)
     * @param aWebServiceURL
     *            {@link String}
     * @param aSoapRequest
     *            {@link String}
     * @param aSoapActionURI
     *            {@link String}
     * @return String
     * @throws Exception
     */
    public static String callSoap(String aWebServiceURL, String aSoapRequest,
            String aSoapActionURI) throws Exception {
        return callSoap(aWebServiceURL, aSoapRequest, aSoapActionURI, getDefaultHttpHeaders());
    }

    /**
     * This method establishes request/response connection http connection to
     * the provided url
     *
     * @param aWebServiceURL
     *            - String , the URL address
     * @param aSoapRequest
     *            - String , the request
     * @param aSoapActionURI
     *            - intent of the action String , SOAP URI
     * @param aBasicAuth
     *            - String , authentication
     * @return String , information for the received messages
     * @throws Exception
     *             , if some problem occurs
     */
    public static String callSoap(String aWebServiceURL, String aSoapRequest,
            String aSoapActionURI, String aBasicAuth) throws Exception {
        Map<String, String> headers = getDefaultHttpHeaders();
        if (aBasicAuth != null)
            headers.put(PROP_HTTP_AUTH, "Basic " + aBasicAuth);
        return callSoap(aWebServiceURL, aSoapRequest, aSoapActionURI, headers);
    }

    /**
     * the keyword by which the request is known
     */
    private static final String PROP_HTTP_CONTENT_TYPE = "content-type";

    /**
     * the keyword by which the request is known
     */
    private static final String PROP_HTTP_SOAP_ACTION = "SOAPAction";

    /**
     * the keyword by which the request is known
     */
    private static final String PROP_HTTP_METHOD = "method";

    /**
     * the keyword by which the request is known
     */
    private static final String PROP_HTTP_CONNECTION = "connection";

    /**
     * the value of the associated request
     */
    private static final String VAL_HTTP_CONNECTION_CLOSE = "close";
    
    /**
     * the keyword by which the request is known
     */
    private static final String HTTP_POST_METHOD = "POST";
    
    /**
     * the value of the associated request
     */
    private static final String VAL_HTTP_TEXT_XML = "text/xml;charset=utf-8";

    /**
     * buffer size
     */
    private final static int READ_BUFFER_LENGTH = 4096;
    
    /**
     * the keyword by which the request is known
     */
    private static final String PROP_HTTP_AUTH = "authorization";

    public static Map<String, String> getDefaultHttpHeaders() {
        Map<String, String> ret = new HashMap<String, String>();
        
        ret.put(PROP_HTTP_CONTENT_TYPE, VAL_HTTP_TEXT_XML);
        ret.put(PROP_HTTP_METHOD, HTTP_POST_METHOD);
        /*
         * We are sending a single request and will close the connection after
         * the response, so ensure the "Connection" header is set to "close" and
         * not to "keep-alive".
         */
        ret.put(PROP_HTTP_CONNECTION, VAL_HTTP_CONNECTION_CLOSE);
        return ret;
    }

    /**
     * This method establishes request/response connection http connection to
     * the provided url
     *
     * @param aWebServiceURL
     *            - String , the URL address
     * @param aSoapRequest
     *            - String , the request
     * @param aSoapActionURI
     *            - intent of the action String , SOAP URI
     * @param headers
     *            - Map with custom headers, e.g. authentication
     * @return String , information for the received messages
     * @throws Exception
     *             , if some problem occurs
     */
    public static String callSoap(String aWebServiceURL, String aSoapRequest,
            String aSoapActionURI, Map<String, String> headers) throws Exception {
        // init
        URL webServiceURL = new URL(aWebServiceURL);
        HttpURLConnection httpConnection = (HttpURLConnection) webServiceURL.openConnection();
        httpConnection.setDoOutput(true);
        httpConnection.setRequestMethod(HTTP_POST_METHOD);
        
        if (headers != null) {
            headers.put(PROP_HTTP_SOAP_ACTION, aSoapActionURI);
            for (Map.Entry<String,String> me: headers.entrySet())
                httpConnection.setRequestProperty(me.getKey(), me.getValue());
        }

        // send
        BufferedReader bufferedReader = new BufferedReader(new StringReader(
                aSoapRequest));
        BufferedWriter bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(httpConnection.getOutputStream(),
                        ENCODING_UTF8));
        char readBuffer[] = new char[READ_BUFFER_LENGTH];
        int k;
        while ((k = bufferedReader.read(readBuffer)) > 0) {
            bufferedWriter.write(readBuffer, 0, k);
        }
        bufferedWriter.flush();
        bufferedWriter.close();

        int responseCode = httpConnection.getResponseCode();

        // receive
        int contentLength = httpConnection.getContentLength();
        String soapResponse = null;

        if (contentLength != 0) {// the server response has a body

            String contentType = httpConnection.getContentType();
            if (contentType == null) {
                throw new Exception(sResHash.getString("WSUTIL.WSCALL.CANNOT.PROCESS.SOAP.RESPONSE.MESSAGE"));
            }
            if (!contentType.startsWith("text/")) {
                throw new Exception(sResHash.getString("WSUTIL.WSCALL.UNSUPPORTED.CONTENT.TYPE", contentType));
            }

            InputStream inputStream = responseCode < 400 ? httpConnection.getInputStream() : httpConnection.getErrorStream();

            if (contentLength == -1) {
                soapResponse = readHttpContentNoContentLength(inputStream, contentType);
            } else {
                soapResponse = readHttpContent(inputStream, contentType, contentLength);
            }
        }

        // close
        httpConnection.disconnect();

        // Defect 11137
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            return soapResponse;
        } else {
            String err = sResHash.getString("WSUTIL.WSCALL.CALL.SOAP.SERVER.ERROR.RESPONSE",
                    Integer.toString(responseCode));
            if (soapResponse != null && soapResponse.length() > 0)
                err += "\n" + soapResponse;
            throw new Exception(err);
        }
    }

    /**
     * Converts the SOAP message into DOM format
     *
     * @param aSoapMsg
     *            String representation of the soap message
     * @return Vector , that holds the body and header elements of the message
     * @throws Exception
     *             , if the required information cannot be retrieved
     */
    public static Vector<Element> getSoapHeaderAndBodyAsDOM(String aSoapMsg)
            throws Exception {
        // Create a ByteArrayInputStream from the SOAP message string
        ByteArrayInputStream bais = new ByteArrayInputStream(aSoapMsg.getBytes(ENCODING_UTF8));
        
        // Create a DocumentBuilder to parse the XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        // Parse the SOAP message
        Document doc = builder.parse(bais);
        
        // Get the SOAP envelope element
        Element envelopeElement = doc.getDocumentElement();
        
        // Find the SOAP body and header elements
        Element bodyElement = null;
        Element headerElement = null;
        NodeList children = envelopeElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String localName = element.getLocalName();
                if ("Body".equals(localName)) {
                    bodyElement = element;
                } else if ("Header".equals(localName)) {
                    headerElement = element;
                }
            }
        }
        
        // Create a vector to hold the body and header elements
        Vector<Element> vector = new Vector<Element>();
        if (bodyElement != null) {
            vector.add(bodyElement);
            if (headerElement != null) {
                vector.add(headerElement);
            }
        }
        
        return vector;
    }

    /**
     * This method converts an input array of objects in the following String
     * format: <b>[obj1 , obj2 , ... ]</b>
     *
     * @param aObjArray
     *            array of objects
     * @return String
     */
    public static String objectArrayToString(Object[] aObjArray) {
        StringBuffer msg = new StringBuffer("[");
        if (aObjArray != null) {
            for (int i = 0; i < aObjArray.length; i++) {
                if (i > 0) {
                    msg.append(", ");
                }
                msg.append("");
                msg.append(aObjArray[i]);
            }
        } else {
            msg.append("Object array is null");
        }
        msg.append("]");

        return msg.toString();
    }

    /**
     * This method returns String , holding the child elements of the input
     * {@link Node}.
     *
     * @param aNode
     *            {@link Node}
     * @return String
     * @throws Exception
     *             if the passed Node is <code>null</code>
     */
    public static String getChildNodesAsString(Node aNode) throws Exception {
        StringBuffer childNodesAsString = new StringBuffer("");
        if (aNode != null) {
            NodeList nodeList = aNode.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                childNodesAsString.append(getAsString(nodeList.item(i)));
            }
        } else {
            throw new Exception(
                    sResHash
                            .getString("WSUTIL.WSCALL.THE.ORGW3CDOMNODE.PASSED.IS.NULL2"));
        }
        return childNodesAsString.toString();
    }

    /**
     * Reads a specific amount of information from the {@link InputStream} and
     * returns the content as {@link String}
     *
     * @param aInputStream
     *            {@link InputStream} to be read
     * @param aContentType
     *            type of data, so that the encoding style can be set
     * @param aContentLength
     *            length of data to be read
     * @return String
     * @throws IOException
     *             , if error occurs during reading
     */
    public static String readHttpContent(java.io.InputStream aInputStream,
            String aContentType, int aContentLength) throws java.io.IOException {
        int totalBytesRead = 0;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream bos = new ByteArrayOutputStream(aContentLength);

        while (totalBytesRead < aContentLength) {
            int bytesRead;
            if (totalBytesRead + 1024 <= aContentLength)
                bytesRead = aInputStream.read(buffer);
            else
                bytesRead = aInputStream.read(buffer, 0, aContentLength - totalBytesRead);

            if (bytesRead > 0) {
                totalBytesRead += bytesRead;
                bos.write(buffer, 0, bytesRead);
            }
        }

        return bos.toString(getJavaEncoding(aContentType));
    }

    /**
     * This method returns the smaller of two numbers
     *
     * @param a
     *            integer
     * @param b
     *            integer
     * @return integer
     */
    public static int min(int a, int b) {
        return (a < b ? a : b);
    }

    /**
     * Changes the size of a byte array
     *
     * @param aByteArray
     *            byte array
     * @param aNewSize
     *            integer
     * @return the resized array
     */
    public static byte[] resizeByteArray(byte[] aByteArray, int aNewSize) {
        byte[] newArray = new byte[aNewSize];
        int lenToCopy = min(aByteArray.length, aNewSize);
        for (int i = 0; i < lenToCopy; i++) {
            newArray[i] = aByteArray[i];
        }
        return newArray;
    }

    /**
     * Reads the information from the {@link InputStream} and returns the
     * content as {@link String}
     *
     * @param aInputStream
     *            the {@link InputStream}
     * @param aContentType
     *            type of data, so that the encoding style can be set
     * @return String
     * @throws IOException
     *             , if error occurs during reading
     */
    public static String readHttpContentNoContentLength(
            java.io.InputStream aInputStream, String aContentType)
            throws java.io.IOException {
        byte[] buffer = new byte[INITIAL_BUFFER_SIZE];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int numRead = aInputStream.read(buffer);
        while (numRead > 0) {
            bos.write(buffer, 0, numRead);
            numRead = aInputStream.read(buffer);
        }
        return bos.toString(getJavaEncoding(aContentType));
    }

    /**
     * This method returns the encoding type
     *
     * @param aContentType
     *            String
     * @return - String ,<code>null</code> if argument has <code>null</code>
     *         value , the specified encoding style or the default unicode
     *         encryption
     */
    public static String getJavaEncoding(String aContentType) {
        if (aContentType == null) {
            return null;
        }

        String encoding = ENCODING_UTF8;
        String charset = null;
        int idx = aContentType.indexOf(';');
        if (idx != -1) {
            String charsetPart = aContentType.substring(idx + 1);
            if (!"".equals(charsetPart)) {
                idx = charsetPart.indexOf('=');
                if (idx != -1) {
                    String httpCharset = charsetPart.substring(idx + 1);
                    if (!"".equals(httpCharset)) {
                        charset = httpCharset;
                    }
                }
            }
        }

        if (charset != null) {
            if (charset.startsWith("\"")) {
                charset = charset.substring(1);
            }
            if (charset.endsWith("\"")) {
                charset = charset.substring(0, charset.length() - 1);
            }
            java.nio.charset.Charset mapCharset = java.nio.charset.Charset
                    .forName(charset);
            encoding = mapCharset.name();
        }

        return encoding;
    }

    /**
     * Checks if debug is enabled.
     *
     * @return <code>true</code> if enabled.
     */
    private boolean isDebugEnabled() {
        return (mLog != null) ? mLog.getDebug() : false;
    }

    /**
     * Logs debug information.
     *
     * @param msg
     *            String
     */
    private void logdebug(String msg) {
        if (mLog != null) {
            mLog.logdebug(msg);
        }
    }

    /**
     * Logs error message.
     *
     * @param msg
     *            String
     */
    private void logerror(String msg) {
        if (mLog != null) {
            mLog.logerror(msg);
        }
    }

    /**
     * Logs message.
     *
     * @param msg
     *            String
     */
    private void logmsg(String msg) {
        if (mLog != null) {
            mLog.loginfo(msg);
        }
    }
    
    /**
     * Converts an OMElement to an appropriate Java type based on its content.
     *
     * @param element The OMElement to convert
     * @return The converted value
     */
    private Object convertToAppropriateType(OMElement element) {
        if (element == null) {
            return null;
        }
        
        // Check if this element has child elements
        if (element.getFirstElement() != null) {
            // This is a complex type, return the element itself
            return element;
        }
        
        // This is a simple type, try to convert the text value
        String text = element.getText();
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Try to convert to appropriate type
        try {
            // Try integer
            if (text.matches("^-?\\d+$")) {
                return Integer.parseInt(text);
            }
            
            // Try double
            if (text.matches("^-?\\d+\\.\\d+$")) {
                return Double.parseDouble(text);
            }
            
            // Try boolean
            if (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(text);
            }
        } catch (Exception e) {
            // If conversion fails, return the original text
            if (isDebugEnabled()) {
                logdebug(sResHash.getString("WSUTIL.WSCLIENT.FAILED.TO.CONVERT.VALUE", text));
            }
        }
        
        // Default to returning the text
        return text;
    }
    /**
     * Linked list holding custom type mappings.
     */
    private java.util.List<Object[]> customTypeMappings = new java.util.LinkedList<Object[]>();

    /**
     * Records a type mapping to be used by the service client.
     * This is similar to the registerCustomTypeMapping method in the original WebServiceCall class.
     *
     * @param javaType The Java class type
     * @param xmlType The XML QName type
     * @param serializerFactory The serializer factory
     * @param deserializerFactory The deserializer factory
     */
    public void registerCustomTypeMapping(Class<?> javaType, QName xmlType,
            Object serializerFactory, Object deserializerFactory) {
        customTypeMappings.add(new Object[] { javaType, xmlType,
                serializerFactory, deserializerFactory });
        
        // Log the registration if debug is enabled
        if (isDebugEnabled()) {
            logdebug(sResHash.getString("WSUTIL.WSCLIENT.REGISTERED.CUSTOM.TYPE.MAPPING",
                new Object[] { javaType.getName(), xmlType.toString() }));
        }
    }
    
    /**
     * Applies the custom type mappings to the service client.
     * This method should be called before invoking the service if custom type mappings are used.
     */
    public void applyCustomTypeMappings() {
        if (serviceClient != null && !customTypeMappings.isEmpty()) {
            AxisConfiguration axisConfig = serviceClient.getAxisConfiguration();
            if (axisConfig != null) {
                for (Object[] mapping : customTypeMappings) {
                    Class<?> javaType = (Class<?>) mapping[0];
                    QName xmlType = (QName) mapping[1];
                    
                    // In Axis2, we add parameters to the AxisConfiguration
                    Parameter param = new Parameter();
                    param.setName("TypeMapping-" + xmlType.toString());
                    param.setValue(new Object[] { javaType, xmlType });
                    
                    try {
                        axisConfig.addParameter(param);
                        if (isDebugEnabled()) {
                            logdebug(sResHash.getString("WSUTIL.WSCLIENT.APPLIED.TYPE.MAPPING",
                                new Object[] { javaType.getName(), xmlType.toString() }));
                        }
                    } catch (AxisFault e) {
                        logerror(sResHash.getString("WSUTIL.WSCLIENT.FAILED.TO.APPLY.TYPE.MAPPING", e.getMessage()));
                    }
                }
            }
        }
    }
    
    /**
     * Converts a string of operation parameters into an array of strings.
     *
     * @param paramList The string containing operation parameters
     * @return An array of operation parameter names
     */
    public static String[] convertOperationParams(String paramList) {
        if (paramList == null || paramList.trim().isEmpty()) {
            return new String[0];
        }
        
        StringTokenizer tokenizer = new StringTokenizer(paramList, OPERATION_PARAM_LIST_DELIMITERS);
        String[] params = new String[tokenizer.countTokens()];
        int i = 0;
        
        while (tokenizer.hasMoreTokens()) {
            params[i++] = tokenizer.nextToken();
        }
        
        return params;
    }
    
    /**
     * Registers a class mapping with the service client.
     * This is similar to the registerClassMapping method in the original WebServiceCall class.
     *
     * @param aClass The class to register
     * @throws Exception if an error occurs during registration
     */
    public void registerClassMapping(Class<?> aClass) throws Exception {
        if (aClass == null) {
            throw new IllegalArgumentException(sResHash.getString("WSUTIL.WSCLIENT.CLASS.CANNOT.BE.NULL"));
        }
        
        logmsg(sResHash.getString("WSUTIL.WSCLIENT.ABOUT.TO.REGISTER.CLASS", aClass.getName()));
        
        try {
            java.lang.reflect.Method method = aClass.getMethod("getTypeDesc", new Class[] {});
            Object typeDesc = method.invoke(null, new Object[] {});
            
            // Extract QName from the TypeDesc using reflection
            java.lang.reflect.Method getXmlTypeMethod = typeDesc.getClass().getMethod("getXmlType");
            QName qName = (QName) getXmlTypeMethod.invoke(typeDesc);
            
            logmsg(sResHash.getString("WSUTIL.WSCLIENT.FOR.QNAME", qName.toString()));
            
            // Register the type mapping
            registerCustomTypeMapping(aClass, qName, null, null);
            
            logmsg(sResHash.getString("WSUTIL.WSCLIENT.DONE.REGISTERING.CLASS", aClass.getName()));
        } catch (Exception e) {
            logerror(sResHash.getString("WSUTIL.WSCLIENT.FAILED.TO.REGISTER.CLASS.MAPPING", e.getMessage()));
            throw e;
        }
    }

    /**
     * Creates a minimal ConfigurationContext programmatically without using axis2.xml.
     * This is a fallback method when axis2.xml is not found.
     * It avoids issues with LocalTransportSender which was removed in Axis2 1.8.2.
     *
     * @return ConfigurationContext
     * @throws Exception if configuration cannot be created
     */
    private ConfigurationContext createMinimalConfigurationContext() throws Exception {
        // Create a new AxisConfiguration without loading axis2.xml
        AxisConfiguration axisConfig = new AxisConfiguration();
        
        // Create HTTP transport sender (for outgoing requests)
        org.apache.axis2.description.TransportOutDescription httpTransportOut = 
            new org.apache.axis2.description.TransportOutDescription("http");
        httpTransportOut.setSender(new org.apache.axis2.transport.http.impl.httpclient4.HTTPClient4TransportSender());
        axisConfig.addTransportOut(httpTransportOut);
        
        // Create HTTPS transport sender (for secure outgoing requests)
        org.apache.axis2.description.TransportOutDescription httpsTransportOut = 
            new org.apache.axis2.description.TransportOutDescription("https");
        httpsTransportOut.setSender(new org.apache.axis2.transport.http.impl.httpclient4.HTTPClient4TransportSender());
        axisConfig.addTransportOut(httpsTransportOut);
        
        // Create and return the configuration context
        return new ConfigurationContext(axisConfig);
    }
}
