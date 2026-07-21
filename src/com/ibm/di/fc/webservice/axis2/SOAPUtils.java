/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice.axis2;

import java.io.StringWriter;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;

/**
 * Utility class for SOAP message handling using AXIOM.
 * This class provides methods for SOAP envelope creation, manipulation, and serialization.
 */
public class SOAPUtils {
    /**
     * Copyright.
     */
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
    
    /**
     * SOAP 1.1 namespace URI.
     */
    public static final String SOAP11_NAMESPACE_URI = "http://schemas.xmlsoap.org/soap/envelope/";
    
    /**
     * SOAP 1.2 namespace URI.
     */
    public static final String SOAP12_NAMESPACE_URI = "http://www.w3.org/2003/05/soap-envelope";
    
    /**
     * Creates a SOAP 1.1 envelope.
     * 
     * @return The SOAP 1.1 envelope
     */
    public static SOAPEnvelope createSOAP11Envelope() {
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.createSOAPEnvelope();
        
        // Add header and body
        factory.createSOAPHeader(envelope);
        factory.createSOAPBody(envelope);
        
        return envelope;
    }
    
    /**
     * Creates a SOAP 1.2 envelope.
     * 
     * @return The SOAP 1.2 envelope
     */
    public static SOAPEnvelope createSOAP12Envelope() {
        SOAPFactory factory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope envelope = factory.createSOAPEnvelope();
        
        // Add header and body
        factory.createSOAPHeader(envelope);
        factory.createSOAPBody(envelope);
        
        return envelope;
    }
    
    /**
     * Adds an element to the SOAP body.
     * 
     * @param envelope The SOAP envelope
     * @param element The element to add to the body
     * @return The added element
     */
    public static OMElement addToBody(SOAPEnvelope envelope, OMElement element) {
        if (envelope == null || element == null) {
            return null;
        }
        
        SOAPBody body = envelope.getBody();
        body.addChild(element);
        
        return element;
    }
    
    /**
     * Creates and adds an element to the SOAP body.
     * 
     * @param envelope The SOAP envelope
     * @param qname The QName of the element to create
     * @return The created element
     */
    public static OMElement addToBody(SOAPEnvelope envelope, QName qname) {
        if (envelope == null || qname == null) {
            return null;
        }
        
        SOAPBody body = envelope.getBody();
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(qname);
        body.addChild(element);
        
        return element;
    }
    
    /**
     * Adds an element to the SOAP header.
     * 
     * @param envelope The SOAP envelope
     * @param element The element to add to the header
     * @return The added element
     */
    public static OMElement addToHeader(SOAPEnvelope envelope, OMElement element) {
        if (envelope == null || element == null) {
            return null;
        }
        
        SOAPHeader header = envelope.getHeader();
        header.addChild(element);
        
        return element;
    }
    
    /**
     * Creates and adds a header block to the SOAP header.
     * 
     * @param envelope The SOAP envelope
     * @param qname The QName of the header block to create
     * @return The created header block
     */
    public static SOAPHeaderBlock addHeaderBlock(SOAPEnvelope envelope, QName qname) {
        if (envelope == null || qname == null) {
            return null;
        }
        
        SOAPHeader header = envelope.getHeader();
        return header.addHeaderBlock(qname.getLocalPart(), 
                                    envelope.getOMFactory().createOMNamespace(qname.getNamespaceURI(), qname.getPrefix()));
    }
    
    /**
     * Serializes a SOAP envelope to a string.
     * 
     * @param envelope The SOAP envelope
     * @return The serialized SOAP envelope as a string
     * @throws Exception If an error occurs during serialization
     */
    public static String toString(SOAPEnvelope envelope) throws Exception {
        if (envelope == null) {
            return null;
        }
        
        try {
            StringWriter writer = new StringWriter();
            envelope.serialize(writer);
            return writer.toString();
        } catch (Exception e) {
            LogUtil.logerror("Failed to serialize SOAP envelope", e);
            throw e;
        }
    }
    
    /**
     * Gets the first element in the SOAP body.
     * 
     * @param envelope The SOAP envelope
     * @return The first element in the SOAP body, or null if the body is empty
     */
    public static OMElement getFirstBodyElement(SOAPEnvelope envelope) {
        if (envelope == null || envelope.getBody() == null) {
            return null;
        }
        
        return envelope.getBody().getFirstElement();
    }
    
    /**
     * Creates a SOAP fault.
     * 
     * @param envelope The SOAP envelope
     * @param faultCode The fault code
     * @param faultReason The fault reason
     * @return The created SOAP fault
     */
    public static SOAPFault createFault(SOAPEnvelope envelope, String faultCode, String faultReason) {
        if (envelope == null) {
            return null;
        }
        
        try {
            SOAPFactory factory;
            if (envelope.getNamespace().getNamespaceURI().equals(SOAP11_NAMESPACE_URI)) {
                factory = OMAbstractFactory.getSOAP11Factory();
            } else {
                factory = OMAbstractFactory.getSOAP12Factory();
            }
            
            SOAPBody body = envelope.getBody();
            SOAPFault fault = factory.createSOAPFault(body);
            
            // Add fault code
            SOAPFaultCode code = factory.createSOAPFaultCode(fault);
            if (envelope.getNamespace().getNamespaceURI().equals(SOAP11_NAMESPACE_URI)) {
                code.setText(new QName(envelope.getNamespace().getNamespaceURI(), faultCode, envelope.getNamespace().getPrefix()));
            } else {
                code.setText(new QName(envelope.getNamespace().getNamespaceURI(), "Sender", envelope.getNamespace().getPrefix()));
                factory.createSOAPFaultSubCode(code).setText(new QName(faultCode));
            }
            
            // Add fault reason
            SOAPFaultReason reason = factory.createSOAPFaultReason(fault);
            if (envelope.getNamespace().getNamespaceURI().equals(SOAP11_NAMESPACE_URI)) {
                reason.setText(faultReason);
            } else {
                SOAPFaultText text = factory.createSOAPFaultText(reason);
                text.setText(faultReason);
                text.setLang("en");
            }
            
            return fault;
        } catch (Exception e) {
            LogUtil.logerror("Failed to create SOAP fault", e);
            return null;
        }
    }
    
    /**
     * Adds detail to a SOAP fault.
     * 
     * @param fault The SOAP fault
     * @param detailElement The detail element to add
     * @return The SOAP fault detail
     */
    public static SOAPFaultDetail addFaultDetail(SOAPFault fault, OMElement detailElement) {
        if (fault == null || detailElement == null) {
            return null;
        }
        
        try {
            SOAPFactory factory;
            if (fault.getNamespace().getNamespaceURI().equals(SOAP11_NAMESPACE_URI)) {
                factory = OMAbstractFactory.getSOAP11Factory();
            } else {
                factory = OMAbstractFactory.getSOAP12Factory();
            }
            
            SOAPFaultDetail detail = factory.createSOAPFaultDetail(fault);
            detail.addChild(detailElement);
            
            return detail;
        } catch (Exception e) {
            LogUtil.logerror("Failed to add detail to SOAP fault", e);
            return null;
        }
    }
    
    /**
     * Creates an AxisFault from a SOAP fault.
     * 
     * @param fault The SOAP fault
     * @return The AxisFault
     */
    public static AxisFault createAxisFault(SOAPFault fault) {
        if (fault == null) {
            return new AxisFault("Unknown fault");
        }
        
        try {
            AxisFault axisFault = new AxisFault(fault);
            return axisFault;
        } catch (Exception e) {
            LogUtil.logerror("Failed to create AxisFault from SOAP fault", e);
            return new AxisFault("Error creating AxisFault", e);
        }
    }
    
    /**
     * Checks if a SOAP envelope contains a fault.
     * 
     * @param envelope The SOAP envelope
     * @return true if the envelope contains a fault, false otherwise
     */
    public static boolean hasFault(SOAPEnvelope envelope) {
        if (envelope == null || envelope.getBody() == null) {
            return false;
        }
        
        return envelope.getBody().hasFault();
    }
    
    /**
     * Gets the SOAP fault from an envelope.
     * 
     * @param envelope The SOAP envelope
     * @return The SOAP fault, or null if the envelope does not contain a fault
     */
    public static SOAPFault getFault(SOAPEnvelope envelope) {
        if (envelope == null || envelope.getBody() == null) {
            return null;
        }
        
        return envelope.getBody().getFault();
    }
    
    /**
     * Creates a QName with the SOAP envelope namespace.
     * 
     * @param envelope The SOAP envelope
     * @param localName The local name
     * @return The QName
     */
    public static QName createQNameWithEnvelopeNS(SOAPEnvelope envelope, String localName) {
        if (envelope == null || localName == null) {
            return null;
        }
        
        return new QName(envelope.getNamespace().getNamespaceURI(), localName, envelope.getNamespace().getPrefix());
    }
    
    /**
     * Determines if a SOAP envelope is SOAP 1.1.
     * 
     * @param envelope The SOAP envelope
     * @return true if the envelope is SOAP 1.1, false otherwise
     */
    public static boolean isSOAP11(SOAPEnvelope envelope) {
        if (envelope == null || envelope.getNamespace() == null) {
            return false;
        }
        
        return SOAP11_NAMESPACE_URI.equals(envelope.getNamespace().getNamespaceURI());
    }
    
    /**
     * Determines if a SOAP envelope is SOAP 1.2.
     * 
     * @param envelope The SOAP envelope
     * @return true if the envelope is SOAP 1.2, false otherwise
     */
    public static boolean isSOAP12(SOAPEnvelope envelope) {
        if (envelope == null || envelope.getNamespace() == null) {
            return false;
        }
        
        return SOAP12_NAMESPACE_URI.equals(envelope.getNamespace().getNamespaceURI());
    }
}
