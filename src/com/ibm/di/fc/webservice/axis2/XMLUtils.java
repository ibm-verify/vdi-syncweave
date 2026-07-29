/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice.axis2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Utility class for XML manipulation using AXIOM.
 * This class provides methods for converting between DOM and AXIOM,
 * XML parsing and serialization, namespace handling, and XML validation.
 */
public class XMLUtils {
    /**
     * Copyright.
     */
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

    /**
     * Converts an AXIOM OMElement to a DOM Element.
     * 
     * @param omElement The AXIOM OMElement to convert
     * @return The DOM Element
     * @throws Exception If an error occurs during conversion
     */
    public static Element toDOM(OMElement omElement) throws Exception {
        if (omElement == null) {
            return null;
        }
        
        try {
            // Convert AXIOM to String
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            omElement.serialize(baos);
            
            // Parse String to DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(baos.toByteArray()));
            
            return document.getDocumentElement();
        } catch (Exception e) {
            LogUtil.logerror("Failed to convert AXIOM to DOM", e);
            throw e;
        }
    }
    
    /**
     * Converts a DOM Element to an AXIOM OMElement.
     * 
     * @param element The DOM Element to convert
     * @return The AXIOM OMElement
     * @throws Exception If an error occurs during conversion
     */
    public static OMElement toOMElement(Element element) throws Exception {
        if (element == null) {
            return null;
        }
        
        try {
            // Convert DOM to String
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            String xml = writer.toString();
            
            // Parse String to AXIOM
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new StringReader(xml));
            
            return OMXMLBuilderFactory.createStAXOMBuilder(xmlStreamReader).getDocumentElement();
        } catch (Exception e) {
            LogUtil.logerror("Failed to convert DOM to AXIOM", e);
            throw e;
        }
    }
    
    /**
     * Creates an AXIOM OMElement from an XML string.
     * 
     * @param xml The XML string
     * @return The AXIOM OMElement
     * @throws Exception If an error occurs during parsing
     */
    public static OMElement fromString(String xml) throws Exception {
        if (xml == null || xml.trim().isEmpty()) {
            return null;
        }
        
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new StringReader(xml));
            
            return OMXMLBuilderFactory.createStAXOMBuilder(xmlStreamReader).getDocumentElement();
        } catch (Exception e) {
            LogUtil.logerror("Failed to parse XML string", e);
            throw e;
        }
    }
    
    /**
     * Converts an AXIOM OMElement to an XML string.
     * 
     * @param element The AXIOM OMElement
     * @return The XML string
     * @throws Exception If an error occurs during serialization
     */
    public static String toString(OMElement element) throws Exception {
        if (element == null) {
            return null;
        }
        
        try {
            StringWriter writer = new StringWriter();
            element.serialize(writer);
            return writer.toString();
        } catch (Exception e) {
            LogUtil.logerror("Failed to serialize AXIOM to string", e);
            throw e;
        }
    }
    
    /**
     * Creates an AXIOM OMElement from an input stream.
     * 
     * @param inputStream The input stream containing XML
     * @return The AXIOM OMElement
     * @throws Exception If an error occurs during parsing
     */
    public static OMElement fromInputStream(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return null;
        }
        
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);
            
            return OMXMLBuilderFactory.createStAXOMBuilder(xmlStreamReader).getDocumentElement();
        } catch (Exception e) {
            LogUtil.logerror("Failed to parse XML from input stream", e);
            throw e;
        }
    }
    
    /**
     * Creates an AXIOM namespace.
     * 
     * @param namespaceURI The namespace URI
     * @param prefix The namespace prefix
     * @return The AXIOM namespace
     */
    public static OMNamespace createNamespace(String namespaceURI, String prefix) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        return factory.createOMNamespace(namespaceURI, prefix);
    }
    
    /**
     * Creates an AXIOM element with the specified namespace.
     * 
     * @param localName The local name of the element
     * @param namespace The namespace of the element
     * @return The AXIOM element
     */
    public static OMElement createElement(String localName, OMNamespace namespace) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        return factory.createOMElement(localName, namespace);
    }
    
    /**
     * Creates an AXIOM element with the specified QName.
     * 
     * @param qname The QName of the element
     * @return The AXIOM element
     */
    public static OMElement createElement(QName qname) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        return factory.createOMElement(qname);
    }
    
    /**
     * Validates an XML document against an XML Schema.
     * 
     * @param xml The XML document to validate
     * @param schemaSource The XML Schema source
     * @return true if the XML document is valid, false otherwise
     */
    public static boolean validateXML(Source xml, Source schemaSource) {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = schemaFactory.newSchema(schemaSource);
            
            Validator validator = schema.newValidator();
            validator.validate(xml);
            return true;
        } catch (Exception e) {
            LogUtil.logerror("XML validation failed", e);
            return false;
        }
    }
    
    /**
     * Validates an XML string against an XML Schema.
     * 
     * @param xml The XML string to validate
     * @param schemaXml The XML Schema string
     * @return true if the XML string is valid, false otherwise
     */
    public static boolean validateXML(String xml, String schemaXml) {
        try {
            Source xmlSource = new StreamSource(new StringReader(xml));
            Source schemaSource = new StreamSource(new StringReader(schemaXml));
            
            return validateXML(xmlSource, schemaSource);
        } catch (Exception e) {
            LogUtil.logerror("XML validation failed", e);
            return false;
        }
    }
    
    /**
     * Creates a DOM document from an XML string.
     * 
     * @param xml The XML string
     * @return The DOM document
     * @throws Exception If an error occurs during parsing
     */
    public static Document parseXML(String xml) throws Exception {
        if (xml == null || xml.trim().isEmpty()) {
            return null;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            LogUtil.logerror("Failed to parse XML string to DOM", e);
            throw e;
        }
    }
    
    /**
     * Converts a DOM document to an XML string.
     * 
     * @param document The DOM document
     * @return The XML string
     * @throws Exception If an error occurs during serialization
     */
    public static String toString(Document document) throws Exception {
        if (document == null) {
            return null;
        }
        
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            
            return writer.toString();
        } catch (Exception e) {
            LogUtil.logerror("Failed to serialize DOM to string", e);
            throw e;
        }
    }
}
