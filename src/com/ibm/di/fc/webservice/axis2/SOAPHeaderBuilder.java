/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice.axis2;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;

import com.ibm.di.server.Log;

/**
 * This class provides functionality for building SOAP headers using Axis2's AXIOM-based classes.
 * It replaces the functionality of the original SOAPHeaderOutputAttributes class from Axis 1.x.
 */
public class SOAPHeaderBuilder {
    /**
     * Copyright.
     */
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
    
    /**
     * SOAP header.
     */
    private SOAPHeader header;
    
    /**
     * Logger.
     */
    private Log log;
    
    /**
     * OM factory for creating elements.
     */
    private OMFactory factory;
    
    /**
     * Creates a new SOAPHeaderBuilder with the specified header and logger.
     * 
     * @param header The SOAP header
     * @param log The logger
     */
    public SOAPHeaderBuilder(SOAPHeader header, Log log) {
        this.header = header;
        this.log = log;
        this.factory = header.getOMFactory();
    }
    
    /**
     * Creates a new SOAPHeaderBuilder with the specified envelope and logger.
     * 
     * @param envelope The SOAP envelope
     * @param log The logger
     */
    public SOAPHeaderBuilder(SOAPEnvelope envelope, Log log) {
        this(envelope.getHeader(), log);
    }
    
    /**
     * Gets the SOAP header.
     * 
     * @return The SOAP header
     */
    public SOAPHeader getHeader() {
        return header;
    }
    
    /**
     * Adds a header element.
     * 
     * @param element The element to add
     * @return The added element
     */
    public OMElement addHeaderElement(OMElement element) {
        if (element != null) {
            header.addChild(element);
        }
        return element;
    }
    
    /**
     * Creates and adds a header element.
     * 
     * @param qname The QName of the element to create
     * @return The created element
     */
    public OMElement createHeaderElement(QName qname) {
        OMElement element = factory.createOMElement(qname);
        header.addChild(element);
        return element;
    }
    
    /**
     * Creates and adds a header block.
     * 
     * @param qname The QName of the header block to create
     * @return The created header block
     */
    public SOAPHeaderBlock createHeaderBlock(QName qname) {
        return header.addHeaderBlock(qname.getLocalPart(), 
                factory.createOMNamespace(qname.getNamespaceURI(), qname.getPrefix()));
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
        OMElement element = factory.createOMElement(localName, 
                factory.createOMNamespace(namespaceURI, prefix));
        header.addChild(element);
        return element;
    }
    
    /**
     * Adds security headers for basic authentication.
     * 
     * @param username The username
     * @param password The password
     * @return The security header element
     */
    public OMElement addSecurityHeader(String username, String password) {
        try {
            // Create WS-Security namespace
            OMNamespace wsseNS = factory.createOMNamespace(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", 
                "wsse");
            
            // Create Security element
            OMElement securityElement = factory.createOMElement("Security", wsseNS);
            header.addChild(securityElement);
            
            // Create UsernameToken element
            OMElement usernameTokenElement = factory.createOMElement("UsernameToken", wsseNS);
            securityElement.addChild(usernameTokenElement);
            
            // Create Username element
            OMElement usernameElement = factory.createOMElement("Username", wsseNS);
            usernameElement.setText(username);
            usernameTokenElement.addChild(usernameElement);
            
            // Create Password element
            OMElement passwordElement = factory.createOMElement("Password", wsseNS);
            passwordElement.setText(password);
            
            // Add password type attribute
            OMNamespace wsuNS = factory.createOMNamespace(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", 
                "wsu");
            passwordElement.addAttribute("Type", 
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText", 
                null);
            
            usernameTokenElement.addChild(passwordElement);
            
            return securityElement;
        } catch (Exception e) {
            if (log != null) {
                log.logerror("Failed to add security header", e);
            }
            return null;
        }
    }
    
    /**
     * Adds WS-Addressing headers.
     * 
     * @param to The destination endpoint
     * @param action The SOAP action
     * @param messageId The message ID
     * @return The addressing header element
     */
    public OMElement addAddressingHeaders(String to, String action, String messageId) {
        try {
            // Create WS-Addressing namespace
            OMNamespace wsaNS = factory.createOMNamespace(
                "http://www.w3.org/2005/08/addressing", 
                "wsa");
            
            // Create To element
            OMElement toElement = factory.createOMElement("To", wsaNS);
            toElement.setText(to);
            header.addChild(toElement);
            
            // Create Action element
            OMElement actionElement = factory.createOMElement("Action", wsaNS);
            actionElement.setText(action);
            header.addChild(actionElement);
            
            // Create MessageID element
            OMElement messageIdElement = factory.createOMElement("MessageID", wsaNS);
            messageIdElement.setText(messageId != null ? messageId : "urn:uuid:" + java.util.UUID.randomUUID().toString());
            header.addChild(messageIdElement);
            
            return toElement;
        } catch (Exception e) {
            if (log != null) {
                log.logerror("Failed to add addressing headers", e);
            }
            return null;
        }
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
        OMElement element = createHeaderElement(localName, namespaceURI, prefix);
        element.setText(text);
        return element;
    }
    
    /**
     * Default constructor. Creates a new SOAPHeaderBuilder with a new SOAP header.
     */
    public SOAPHeaderBuilder() {
        this.header = OMAbstractFactory.getSOAP11Factory().createSOAPHeader();
        this.log = null;
        this.factory = header.getOMFactory();
    }
    
    /**
     * Adds a header block to the header.
     *
     * @param headerBlock The header block to add
     * @return The added header block
     */
    public SOAPHeaderBlock addHeaderBlock(SOAPHeaderBlock headerBlock) {
        if (headerBlock != null) {
            header.addChild(headerBlock);
        }
        return headerBlock;
    }
}
