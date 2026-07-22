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
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;

import com.ibm.di.server.Log;

/**
 * This class provides functionality for building SOAP bodies using Axis2's AXIOM-based classes.
 * It replaces the functionality of the original SOAPBodyOutputNS class from Axis 1.x.
 */
public class SOAPBodyBuilder {
    /**
     * Copyright.
     */
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
    
    /**
     * SOAP body.
     */
    private SOAPBody body;
    
    /**
     * Logger.
     */
    private Log log;
    
    /**
     * OM factory for creating elements.
     */
    private OMFactory factory;
    
    /**
     * Creates a new SOAPBodyBuilder with the specified body and logger.
     * 
     * @param body The SOAP body
     * @param log The logger
     */
    public SOAPBodyBuilder(SOAPBody body, Log log) {
        this.body = body;
        this.log = log;
        this.factory = body.getOMFactory();
    }
    
    /**
     * Creates a new SOAPBodyBuilder with the specified envelope and logger.
     * 
     * @param envelope The SOAP envelope
     * @param log The logger
     */
    public SOAPBodyBuilder(SOAPEnvelope envelope, Log log) {
        this(envelope.getBody(), log);
    }
    
    /**
     * Default constructor. Creates a new SOAPBodyBuilder with a new SOAP body.
     */
    public SOAPBodyBuilder() {
        this.body = OMAbstractFactory.getSOAP11Factory().createSOAPBody();
        this.log = null;
        this.factory = body.getOMFactory();
    }
    
    /**
     * Gets the SOAP body.
     * 
     * @return The SOAP body
     */
    public SOAPBody getBody() {
        return body;
    }
    
    /**
     * Adds a body element.
     * 
     * @param element The element to add
     * @return The added element
     */
    public OMElement addBodyElement(OMElement element) {
        if (element != null) {
            body.addChild(element);
        }
        return element;
    }
    
    /**
     * Creates and adds a body element.
     * 
     * @param qname The QName of the element to create
     * @return The created element
     */
    public OMElement createBodyElement(QName qname) {
        OMElement element = factory.createOMElement(qname);
        body.addChild(element);
        return element;
    }
    
    /**
     * Creates and adds a text element to the body.
     * 
     * @param qname The QName of the element to create
     * @param text The text content
     * @return The created element
     */
    public OMElement addTextElement(QName qname, String text) {
        OMElement element = factory.createOMElement(qname);
        element.setText(text);
        body.addChild(element);
        return element;
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
        OMElement element = factory.createOMElement(localName, 
                factory.createOMNamespace(namespaceURI, prefix));
        body.addChild(element);
        return element;
    }
    
    
    /**
     * Gets the first element in the body.
     * 
     * @return The first element in the body
     */
    public OMElement getFirstElement() {
        return body.getFirstElement();
    }
    
    /**
     * Checks if the body has a fault.
     * 
     * @return true if the body has a fault, false otherwise
     */
    public boolean hasFault() {
        return body.hasFault();
    }
    
    /**
     * Gets the fault from the body.
     * 
     * @return The fault, or null if the body does not have a fault
     */
    public org.apache.axiom.soap.SOAPFault getFault() {
        return body.getFault();
    }
    
    /**
     * Creates a fault in the body.
     * 
     * @param faultCode The fault code
     * @param faultReason The fault reason
     * @return The created fault
     */
    public org.apache.axiom.soap.SOAPFault createFault(String faultCode, String faultReason) {
        try {
            SOAPFactory soapFactory = (SOAPFactory) factory;
            org.apache.axiom.soap.SOAPFault fault = soapFactory.createSOAPFault(body);
            
            // Add fault code
            org.apache.axiom.soap.SOAPFaultCode code = soapFactory.createSOAPFaultCode(fault);
            code.setText(new QName(body.getNamespace().getNamespaceURI(), faultCode, 
                    body.getNamespace().getPrefix()));
            
            // Add fault reason
            org.apache.axiom.soap.SOAPFaultReason reason = soapFactory.createSOAPFaultReason(fault);
            reason.setText(faultReason);
            
            return fault;
        } catch (Exception e) {
            if (log != null) {
                log.logerror("Failed to create SOAP fault", e);
            }
            return null;
        }
    }
    
    /**
     * Declares a namespace with the specified URI and prefix.
     *
     * @param namespaceURI The namespace URI
     * @param prefix The namespace prefix
     * @return The created namespace
     */
    public OMNamespace declareNamespace(String namespaceURI, String prefix) {
        return factory.createOMNamespace(namespaceURI, prefix);
    }
    
    /**
     * Creates and adds a body element with the specified local name and namespace.
     *
     * @param localName The local name
     * @param namespace The namespace
     * @return The created element
     */
    public OMElement addBodyElement(String localName, OMNamespace namespace) {
        OMElement element = factory.createOMElement(localName, namespace);
        body.addChild(element);
        return element;
    }
    
    /**
     * Adds a parameter element to a parent element.
     *
     * @param parent The parent element
     * @param paramName The parameter name
     * @param paramValue The parameter value
     * @param namespace The namespace
     * @return The created parameter element
     */
    public OMElement addParameter(OMElement parent, String paramName, Object paramValue, OMNamespace namespace) {
        OMElement paramElement = factory.createOMElement(paramName, namespace);
        
        if (paramValue != null) {
            if (paramValue instanceof OMElement) {
                paramElement.addChild((OMElement) paramValue);
            } else {
                paramElement.setText(paramValue.toString());
            }
        }
        
        parent.addChild(paramElement);
        return paramElement;
    }
}
