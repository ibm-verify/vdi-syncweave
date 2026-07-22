/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice.axis2;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;

import com.ibm.di.server.Log;

/**
 * This class provides functionality for creating and manipulating SOAP envelopes
 * using Axis2's AXIOM-based classes. It combines the functionality of the original
 * ITDISOAPEnvelope and SOAPEnvelopeOutputNS classes from Axis 1.x.
 */
public class SOAPEnvelopeBuilder {
    /**
     * Copyright.
     */
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
    
    /**
     * SOAP envelope.
     */
    private SOAPEnvelope envelope;
    
    /**
     * SOAP factory.
     */
    private SOAPFactory factory;
    
    /**
     * Message context.
     */
    private MessageContext messageContext;
    
    /**
     * Logger.
     */
    private Log log;
    
    /**
     * SOAP body builder.
     */
    private SOAPBodyBuilder bodyBuilder;
    
    /**
     * SOAP header builder.
     */
    private SOAPHeaderBuilder headerBuilder;
    
    /**
     * Flag indicating whether to use SOAP 1.2.
     */
    private boolean useSOAP12;
    
    /**
     * Creates a new SOAPEnvelopeBuilder with the specified message context and logger.
     * 
     * @param messageContext The message context
     * @param log The logger
     */
    public SOAPEnvelopeBuilder(MessageContext messageContext, Log log) {
        this(messageContext, log, false);
    }
    
    /**
     * Creates a new SOAPEnvelopeBuilder with the specified message context, logger,
     * and SOAP version.
     * 
     * @param messageContext The message context
     * @param log The logger
     * @param useSOAP12 Whether to use SOAP 1.2
     */
    public SOAPEnvelopeBuilder(MessageContext messageContext, Log log, boolean useSOAP12) {
        this.messageContext = messageContext;
        this.log = log;
        this.useSOAP12 = useSOAP12;
        
        // Create the SOAP envelope
        createEnvelope();
    }
    
    /**
     * Creates a new SOAPEnvelopeBuilder with the specified logger.
     * 
     * @param log The logger
     */
    public SOAPEnvelopeBuilder(Log log) {
        this(null, log, false);
    }
    
    /**
     * Creates a new SOAPEnvelopeBuilder with the specified logger and SOAP version.
     * 
     * @param log The logger
     * @param useSOAP12 Whether to use SOAP 1.2
     */
    public SOAPEnvelopeBuilder(Log log, boolean useSOAP12) {
        this(null, log, useSOAP12);
    }
    
    /**
     * Default constructor. Creates a new SOAPEnvelopeBuilder with no message context or logger.
     */
    public SOAPEnvelopeBuilder() {
        this(null, null, false);
    }
    
    /**
     * Creates a SOAP envelope.
     *
     * @return The created SOAP envelope
     */
    public SOAPEnvelope createEnvelope() {
        // Create the SOAP factory based on the SOAP version
        if (useSOAP12) {
            envelope = SOAPUtils.createSOAP12Envelope();
            factory = (SOAPFactory) envelope.getOMFactory();
        } else {
            envelope = SOAPUtils.createSOAP11Envelope();
            factory = (SOAPFactory) envelope.getOMFactory();
        }
        
        // Create the body and header builders
        bodyBuilder = new SOAPBodyBuilder(envelope.getBody(), log);
        headerBuilder = new SOAPHeaderBuilder(envelope.getHeader(), log);
        
        return envelope;
    }
    
    /**
     * Gets the SOAP envelope.
     * 
     * @return The SOAP envelope
     */
    public SOAPEnvelope getEnvelope() {
        return envelope;
    }
    
    /**
     * Gets the SOAP body.
     * 
     * @return The SOAP body
     */
    public SOAPBody getBody() {
        return envelope.getBody();
    }
    
    /**
     * Gets the SOAP header.
     * 
     * @return The SOAP header
     */
    public SOAPHeader getHeader() {
        return envelope.getHeader();
    }
    
    /**
     * Gets the SOAP body builder.
     * 
     * @return The SOAP body builder
     */
    public SOAPBodyBuilder getBodyBuilder() {
        return bodyBuilder;
    }
    
    /**
     * Gets the SOAP header builder.
     * 
     * @return The SOAP header builder
     */
    public SOAPHeaderBuilder getHeaderBuilder() {
        return headerBuilder;
    }
    
    /**
     * Adds a body element to the SOAP envelope.
     * 
     * @param element The element to add
     */
    public void addBodyElement(OMElement element) {
        bodyBuilder.addBodyElement(element);
    }
    
    /**
     * Creates and adds a body element to the SOAP envelope.
     * 
     * @param qname The QName of the element to create
     * @return The created element
     */
    public OMElement addBodyElement(QName qname) {
        return bodyBuilder.createBodyElement(qname);
    }
    
    /**
     * Adds a header element to the SOAP envelope.
     * 
     * @param element The element to add
     */
    public void addHeaderElement(OMElement element) {
        headerBuilder.addHeaderElement(element);
    }
    
    /**
     * Creates and adds a header element to the SOAP envelope.
     * 
     * @param qname The QName of the element to create
     * @return The created element
     */
    public OMElement addHeaderElement(QName qname) {
        return headerBuilder.createHeaderElement(qname);
    }
    
    /**
     * Adds security headers for basic authentication.
     * 
     * @param username The username
     * @param password The password
     */
    public void addSecurityHeader(String username, String password) {
        headerBuilder.addSecurityHeader(username, password);
    }
    
    /**
     * Adds WS-Addressing headers.
     * 
     * @param to The destination endpoint
     * @param action The SOAP action
     * @param messageId The message ID
     */
    public void addAddressingHeaders(String to, String action, String messageId) {
        headerBuilder.addAddressingHeaders(to, action, messageId);
    }
    
    /**
     * Converts the SOAP envelope to a string.
     * 
     * @return The string representation of the SOAP envelope
     */
    @Override
    public String toString() {
        try {
            return SOAPUtils.toString(envelope);
        } catch (Exception e) {
            if (log != null) {
                log.logerror("Failed to convert SOAP envelope to string", e);
            }
            return null;
        }
    }
    
    /**
     * Converts the SOAP envelope to a string using the specified style.
     * 
     * @param isDocStyle Whether to use document style
     * @return The string representation of the SOAP envelope
     */
    public String toString(boolean isDocStyle) {
        try {
            if (messageContext != null && isDocStyle) {
                // Set document style properties on the message context
                Options options = messageContext.getOptions();
                if (options == null) {
                    options = new Options();
                    messageContext.setOptions(options);
                }
                
                // Set the appropriate properties for document style
                messageContext.setProperty(Constants.Configuration.ENABLE_REST, Boolean.FALSE);
                // In Axis2 1.7.6, use Constants.Configuration.ENABLE_REST with FALSE value
                // instead of the removed ENABLE_RPC constant
                
                // Set empty encoding style
                options.setSoapVersionURI(useSOAP12 ? 
                        Constants.URI_SOAP12_ENV : Constants.URI_SOAP11_ENV);
            }
            
            return toString();
        } catch (Exception e) {
            if (log != null) {
                log.logerror("Failed to convert SOAP envelope to string using style", e);
            }
            return null;
        }
    }
    
    /**
     * Sets the target endpoint for the message context.
     * 
     * @param endpoint The endpoint URL
     */
    public void setTargetEndpoint(String endpoint) {
        if (messageContext != null) {
            Options options = messageContext.getOptions();
            if (options == null) {
                options = new Options();
                messageContext.setOptions(options);
            }
            options.setTo(new EndpointReference(endpoint));
        }
    }
    
    /**
     * Sets the SOAP action for the message context.
     * 
     * @param soapAction The SOAP action
     */
    public void setSoapAction(String soapAction) {
        if (messageContext != null) {
            Options options = messageContext.getOptions();
            if (options == null) {
                options = new Options();
                messageContext.setOptions(options);
            }
            options.setAction(soapAction);
        }
    }
    
    /**
     * Creates a namespace with the specified URI and prefix.
     * 
     * @param namespaceURI The namespace URI
     * @param prefix The namespace prefix
     * @return The created namespace
     */
    public OMNamespace createNamespace(String namespaceURI, String prefix) {
        return factory.createOMNamespace(namespaceURI, prefix);
    }
    
    /**
     * Creates an element with the specified local name and namespace.
     * 
     * @param localName The local name
     * @param namespace The namespace
     * @return The created element
     */
    public OMElement createElement(String localName, OMNamespace namespace) {
        return factory.createOMElement(localName, namespace);
    }
    
    /**
     * Creates an element with the specified QName.
     * 
     * @param qname The QName
     * @return The created element
     */
    public OMElement createElement(QName qname) {
        return factory.createOMElement(qname);
    }
    
    /**
     * Creates and adds a body element with the specified local name and namespace.
     * 
     * @param localName The local name
     * @param namespaceURI The namespace URI
     * @param prefix The namespace prefix
     * @return The created element
     */
    public OMElement createBodyElement(String localName, String namespaceURI, String prefix) {
        return bodyBuilder.createBodyElement(localName, namespaceURI, prefix);
    }
    
    /**
     * Creates and adds a header element with the specified local name and namespace.
     * 
     * @param localName The local name
     * @param namespaceURI The namespace URI
     * @param prefix The namespace prefix
     * @return The created element
     */
    public OMElement createHeaderElement(String localName, String namespaceURI, String prefix) {
        return headerBuilder.createHeaderElement(localName, namespaceURI, prefix);
    }
    
    /**
     * Adds a custom header with text content.
     * 
     * @param localName The local name
     * @param namespaceURI The namespace URI
     * @param prefix The namespace prefix
     * @param text The text content
     * @return The created element
     */
    public OMElement addTextHeader(String localName, String namespaceURI, String prefix, String text) {
        return headerBuilder.addTextHeader(localName, namespaceURI, prefix, text);
    }
    
    /**
     * Creates and adds a text element to the body.
     * 
     * @param qname The QName of the element to create
     * @param text The text content
     * @return The created element
     */
    public OMElement addTextBodyElement(QName qname, String text) {
        return bodyBuilder.addTextElement(qname, text);
    }
    
    /**
     * Adds a fault to the SOAP envelope.
     *
     * @param faultCode The fault code
     * @param faultReason The fault reason
     * @return The created fault
     */
    public org.apache.axiom.soap.SOAPFault addFault(String faultCode, String faultReason) {
        return bodyBuilder.createFault(faultCode, faultReason);
    }
    
    /**
     * Sets the SOAP body.
     *
     * @param body The SOAP body
     */
    public void setBody(SOAPBody body) {
        if (body != null) {
            // In Axis2/AXIOM, we cannot detach the SOAP body from the envelope
            // Instead, we need to remove all children from the existing body
            // and add the children from the new body
            SOAPBody existingBody = envelope.getBody();
            if (existingBody != null) {
                // Remove all children from the existing body
                existingBody.removeChildren();
                
                // Collect all children from the new body into a list first
                // to avoid ConcurrentModificationException
                java.util.List<org.apache.axiom.om.OMNode> childrenList =
                    new java.util.ArrayList<org.apache.axiom.om.OMNode>();
                java.util.Iterator<?> children = body.getChildren();
                while (children.hasNext()) {
                    Object child = children.next();
                    if (child instanceof org.apache.axiom.om.OMNode) {
                        childrenList.add((org.apache.axiom.om.OMNode) child);
                    }
                }
                
                // Now move all children from the list to the existing body
                for (org.apache.axiom.om.OMNode node : childrenList) {
                    // Detach from old parent and add to existing body
                    node.detach();
                    existingBody.addChild(node);
                }
                
                // Update the body builder to use the existing body
                this.bodyBuilder = new SOAPBodyBuilder(existingBody, log);
            }
        }
    }
    
    /**
     * Sets the SOAP header.
     *
     * @param header The SOAP header
     */
    public void setHeader(SOAPHeader header) {
        if (header != null) {
            // Replace the existing header with the new one
            if (envelope.getHeader() != null) {
                envelope.getHeader().detach();
            }
            envelope.addChild(header);
            
            // Update the header builder
            this.headerBuilder = new SOAPHeaderBuilder(header, log);
        }
    }
}
