/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.axis2.util;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.naming.Name;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAllMember;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaChoiceMember;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeList;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.di.config.base.SchemaConfigImpl;
import com.ibm.di.config.base.SchemaItemConfigImpl;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.server.ResourceHash;

/**
 * Converts TDI Schema to XML Schema and vice versa. Generates WSDL document
 * from an Assembly Line configuration.
 * 
 * @since 7.0
 */
public class SchemaUtils {

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

	/** XML Schema namspace URI */
	private static final String XSD_NS_URI = "http://www.w3.org/2001/XMLSchema";

	/** XML Namespaces namespace URI */
	private static final String XML_NS_URI = "http://www.w3.org/2000/xmlns/";

	/** Namespace prefix for XML Schema elements. */
	private static final String XSD_NS_PREFIX = "xs";

	/**
	 * Namespace prefix for WSDL 1.1 elements.
	 */
	private static final String WSDL11_NS_PREFIX = "http://schemas.xmlsoap.org/wsdl/";

	/**
	 * Namespace prefix for WSDL 2.0 elements.
	 */
	private static final String WSDL20_NS_PREFIX = "http://www.w3.org/ns/wsdl";

	/**
	 * Namespace prefix for SOAP 1.1 elements.
	 */
	private static final String SOAP11_NS_PREFIX = "http://schemas.xmlsoap.org/wsdl/soap/";

	/**
	 * Namespace prefix for SOAP 1.2 elements.
	 */
	private static final String SOAP12_NS_PREFIX = "http://schemas.xmlsoap.org/wsdl/soap12/";

	/**
	 * Namespace prefix for addressing elements.
	 */
	private static final String WSAW_NS_PREFIX = "http://www.w3.org/2006/05/addressing/wsdl/";

	/**
	 * Namespace prefix for SOAP elements in WSDL 2.0 documents.
	 */
	private static final String WSOAP_NS_PREFIX = "http://www.w3.org/ns/wsdl/soap";

	/**
	 * Generate a WSDL file for an Assembly Line configuration.
	 * 
	 * @param config
	 *            Either an AssemblyLineConfig object or a configuration object,
	 *            whose hierarchy contains an AssemblyLineConfig object.
	 * 
	 * @param wsdlFileName
	 *            The name of the WSDL file, which will be generated.
	 * @param serviceAddress
	 *            The URL of the web service, which will be described in the
	 *            WSDL file.
	 * @param wsdlVersion
	 *            WSDL version - either "1.1" or "2.0".
	 * @exception Exception
	 *                The passed configuration object is invalid. Cannot
	 *                generate a WSDL from the AL configuration. Or writing the
	 *                WSDL file failed.
	 */
	public static void generateWsdl(BaseConfiguration config,
			String wsdlFileName, String serviceAddress, String wsdlVersion)
			throws Exception {

		AssemblyLineConfig alc = null;
		// find the first AL configuration in the configuration hierarchy
		while (config != null) {

			if (config instanceof AssemblyLineConfig) {
				alc = (AssemblyLineConfig) config;
				break;
			}

			config = config.getParent();

		}
		if (alc == null) {
			throw new Exception(resHash.getString("SCHEMAUTILS.NO.AL.CONFIG"));
		}

		boolean genWSDL11 = wsdlVersion.equals("1.1");

		FileOutputStream fos = new FileOutputStream(wsdlFileName);
		try {
			Document doc = null;
			if (genWSDL11) {
				doc = createWsdl11(alc, serviceAddress, true);
				printXML(doc, fos);
			} else {
				doc = createWsdl20(alc, serviceAddress, true);
				printXML(doc, fos);
			}

			fos.flush();

		} catch (Exception ex) {
			throw new Exception(resHash.getString(
					"SCHEMAUTILS.CANNOT.WRITE.WSDL", new Object[] { ex,
							wsdlFileName }), ex);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	/**
	 * Creates a custom WSDL version 1.1 document.
	 * 
	 * @param alc
	 *            the assemblyLine configuration that will be a template for the
	 *            WSDL document.
	 * @param serviceAddress
	 *            the service provider address.
	 * @param useSoap12Binding
	 *            whether a SOAP 1.2 binding is needed for the WSDL document. If
	 *            false only SOAP 1.1 binding will be created.
	 * @return the WSDL document.
	 * @throws Exception
	 *             if a problem occurs during the generation of the WSDL
	 *             document.
	 */
	private static Document createWsdl11(AssemblyLineConfig alc,
			String serviceAddress, boolean useSoap12Binding) throws Exception {
		String alName = getALName(alc);
		String thisNamespace = serviceAddress;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document xsdDoc = builder.newDocument();

		String targetNamespace = thisNamespace;

		Element definitionsElement = SchemaUtils.createDefinitionsElement(
				xsdDoc, targetNamespace);
		xsdDoc.appendChild(definitionsElement);
		Element typesElement = xsdDoc.createElementNS(WSDL11_NS_PREFIX,
				"wsdl:types");
		definitionsElement.appendChild(typesElement);
		Element xsdElement = SchemaUtils.createXSD(xsdDoc, targetNamespace);
		typesElement.appendChild(xsdElement);
		Element portTypeElement = xsdDoc.createElementNS(WSDL11_NS_PREFIX,
				"wsdl:portType");
		portTypeElement.setAttributeNS(WSDL11_NS_PREFIX, "name", alName
				+ "PortType");

		Element soap11Binding = xsdDoc.createElementNS(WSDL11_NS_PREFIX,
				"wsdl:binding");
		soap11Binding.setAttributeNS(WSDL11_NS_PREFIX, "name", alName
				+ "SOAP11Binding");
		soap11Binding.setAttributeNS(WSDL11_NS_PREFIX, "type", "ns0:" + alName
				+ "PortType");
		Element bindingEl = xsdDoc.createElementNS(SOAP11_NS_PREFIX,
				"soap:binding");
		bindingEl.setAttributeNS(SOAP11_NS_PREFIX, "transport",
				"http://schemas.xmlsoap.org/soap/http");
		bindingEl.setAttributeNS(SOAP11_NS_PREFIX, "style", "document");
		soap11Binding.appendChild(bindingEl);

		Element soap12Binding = null;
		if (useSoap12Binding) {
			soap12Binding = xsdDoc.createElementNS(WSDL11_NS_PREFIX,
					"wsdl:binding");
			soap12Binding.setAttributeNS(WSDL11_NS_PREFIX, "name", alName
					+ "SOAP12Binding");
			soap12Binding.setAttributeNS(WSDL11_NS_PREFIX, "type", "ns0:"
					+ alName + "PortType");
			Element binding = xsdDoc.createElementNS(SOAP12_NS_PREFIX,
					"soap12:binding");
			binding.setAttributeNS(SOAP12_NS_PREFIX, "transport",
					"http://schemas.xmlsoap.org/soap/http");
			binding.setAttributeNS(SOAP12_NS_PREFIX, "style", "document");
			soap12Binding.appendChild(binding);
		}

		ContainerConfig alOperations = alc.getOperations();
		for (int i = 0; i < alOperations.size(); ++i) {

			OperationConfig opConfig = (OperationConfig) alOperations
					.getConfig(i);
			String wsdlOperationName = getWSDLOperationName(alc, opConfig
					.getShortName());

			Element operation = SchemaUtils.createWsdl11Operation(
					wsdlOperationName, xsdDoc);
			SchemaUtils.addOperationToSchema(opConfig, wsdlOperationName,
					xsdElement, xsdDoc);
			SchemaUtils.addWSDL11OperationMessages(wsdlOperationName,
					definitionsElement, xsdDoc);
			SchemaUtils.addWSDL11OperationToBinding(wsdlOperationName,
					soap11Binding, soap12Binding, xsdDoc);

			portTypeElement.appendChild(operation);
		}

		definitionsElement.appendChild(portTypeElement);
		definitionsElement.appendChild(soap11Binding);
		if (useSoap12Binding) {
			definitionsElement.appendChild(soap12Binding);
		}

		Element serviceElement = SchemaUtils.createWsdl11Service(xsdDoc,
				alName, targetNamespace);
		definitionsElement.appendChild(serviceElement);

		return xsdDoc;
	}

	/**
	 * Creates the 'definitions' element of the WSDL 1.1 document.
	 * 
	 * @param doc
	 *            the WSDL document.
	 * @param targetNamespace
	 *            the targetNamespace used.
	 * @return the 'definitions' element.
	 */
	private static Element createDefinitionsElement(Document doc,
			String targetNamespace) {
		Element definitions = doc.createElementNS(WSDL11_NS_PREFIX,
				"wsdl:definitions");
		definitions.setAttribute("xmlns:ns0", targetNamespace);
		definitions.setAttributeNS(WSDL11_NS_PREFIX, "targetNamespace", targetNamespace);
		return definitions;
	}

	/**
	 * Creates an 'operation' element for the WSDL 1.1 document. It sould be
	 * added to the 'portType' element of the document.
	 * 
	 * @param opConfig
	 *            the operation configuration.
	 * @param wsdlOperationName
	 *            the operation name.
	 * @param xsdElement
	 *            the XSD schema element.
	 * @param definitionsElement
	 *            the 'definitions' element.
	 * @param soap11Binding
	 *            the 'binding' element for SOAP 1.1
	 * @param soap12Binding
	 *            the 'binding' element for SOAP 1.2
	 * @param doc
	 *            the WSDL document.
	 * @return the created 'operation' element for the 'portType' of the WSDL
	 *         document.
	 */
	private static Element createWsdl11Operation(String wsdlOperationName,
			Document doc) {
		Element operationElement = doc.createElementNS(WSDL11_NS_PREFIX,
				"wsdl:operation");
		operationElement.setAttributeNS(WSDL11_NS_PREFIX, "name",
				wsdlOperationName);
		Element inputElement = doc.createElementNS(WSDL11_NS_PREFIX,
				"wsdl:input");
		inputElement.setAttributeNS(WSDL11_NS_PREFIX, "message", "ns0:"
				+ wsdlOperationName + "Request");
		inputElement.setAttributeNS(WSAW_NS_PREFIX, "wsaw:Action", "urn:"
				+ wsdlOperationName);
		Element outputElement = doc.createElementNS(WSDL11_NS_PREFIX,
				"wsdl:output");
		outputElement.setAttributeNS(WSDL11_NS_PREFIX, "message", "ns0:"
				+ wsdlOperationName + "Response");
		outputElement.setAttributeNS(WSAW_NS_PREFIX, "wsaw:Action", "urn:"
				+ wsdlOperationName + "Response");
		operationElement.appendChild(inputElement);
		operationElement.appendChild(outputElement);

		return operationElement;
	}

	/**
	 * Adds an 'operation' element to the schema.
	 * 
	 * @param opConfig
	 *            the assemblyLine configuration that will be a template for the
	 *            WSDL document.
	 * @param wsdlOperationName
	 *            the name of the operation.
	 * @param xsdElement
	 *            the schema element.
	 * @param doc
	 *            the WSDL document.
	 */
	private static void addOperationToSchema(OperationConfig opConfig,
			String wsdlOperationName, Element xsdElement, Document doc) {

		SchemaConfig schemaConfigIn = opConfig.getSchema(true);
		SchemaConfig schemaConfigOut = opConfig.getSchema(false);

		Element xsdElementIn = SchemaUtils.convertSchemaConfigToXSDElement(doc,
				schemaConfigIn, wsdlOperationName);
		Element xsdElementOut = SchemaUtils.convertSchemaConfigToXSDElement(
				doc, schemaConfigOut, wsdlOperationName + "Response");

		xsdElement.appendChild(xsdElementIn);
		xsdElement.appendChild(xsdElementOut);
	}

	/**
	 * Adds the 'operation' to the 'binding' of the document.
	 * 
	 * @param wsdlOperationName
	 *            the operation name.
	 * @param soap11Binding
	 *            the 'binding' element for SOAP 1.1
	 * @param soap12Binding
	 *            the 'binding' element for SOAP 1.2
	 * @param doc
	 *            the WSDL document.
	 */

	private static void addWSDL11OperationToBinding(String wsdlOperationName,
			Element soap11Binding, Element soap12Binding, Document doc) {
		Element soap11BindingOpElement = doc.createElementNS(WSDL11_NS_PREFIX,
				"wsdl:operation");
		soap11BindingOpElement.setAttributeNS(WSDL11_NS_PREFIX, "name",
				wsdlOperationName);
		SchemaUtils.fillSOAPBinding(doc, soap11BindingOpElement,
				wsdlOperationName, "11");
		soap11Binding.appendChild(soap11BindingOpElement);

		if (soap12Binding != null) {
			Element soap12BindingOpElement = doc.createElementNS(
					WSDL11_NS_PREFIX, "wsdl:operation");
			soap12BindingOpElement.setAttributeNS(WSDL11_NS_PREFIX, "name",
					wsdlOperationName);
			SchemaUtils.fillSOAPBinding(doc, soap12BindingOpElement,
					wsdlOperationName, "12");
			soap12Binding.appendChild(soap12BindingOpElement);
		}
	}

	/**
	 * Adds the messages needed for an operation
	 * 
	 * @param wsdlOperationName
	 *            the operation name.
	 * @param definitionsElement
	 *            the 'definitions' element of the document.
	 * @param doc
	 *            the WSDL document.
	 */
	private static void addWSDL11OperationMessages(String wsdlOperationName,
			Element definitionsElement, Document doc) {
		Element messageIn = doc.createElementNS(WSDL11_NS_PREFIX,
				"wsdl:message");
		messageIn.setAttributeNS(WSDL11_NS_PREFIX, "name", wsdlOperationName
				+ "Request");
		Element messageInPart = doc.createElementNS(WSDL11_NS_PREFIX,
				"wsdl:part");
		messageInPart.setAttributeNS(WSDL11_NS_PREFIX, "name", "parameters");
		messageInPart.setAttributeNS(WSDL11_NS_PREFIX, "element", "ns0:"
				+ wsdlOperationName);
		messageIn.appendChild(messageInPart);
		definitionsElement.appendChild(messageIn);

		Element messageOut = doc.createElementNS(WSDL11_NS_PREFIX,
				"wsdl:message");
		messageOut.setAttributeNS(WSDL11_NS_PREFIX, "name", wsdlOperationName
				+ "Response");
		Element messageOutPart = doc.createElementNS(WSDL11_NS_PREFIX,
				"wsdl:part");
		messageOutPart.setAttributeNS(WSDL11_NS_PREFIX, "name", "parameters");
		messageOutPart.setAttributeNS(WSDL11_NS_PREFIX, "element", "ns0:"
				+ wsdlOperationName + "Response");
		messageOut.appendChild(messageOutPart);
		definitionsElement.appendChild(messageOut);
	}

	/**
	 * Fills the binding 'operation' element with the description of the input
	 * and output communication.
	 * 
	 * @param doc
	 *            the WSDL document.
	 * @param bindingOperation
	 *            the binding 'operation' to be populated.
	 * @param wsdlOperationName
	 *            the name of the WSDL operation.
	 * @param soapVersion
	 *            the SOAP version used. Pass "12" for SOAP 1.2 and "11" (or "")
	 *            for SOAP 1.1.
	 */
	private static void fillSOAPBinding(Document doc, Element bindingOperation,
			String wsdlOperationName, String soapVersion) {
		String soapNamespace = "";
		if (soapVersion.equalsIgnoreCase("11")
				|| soapVersion.equalsIgnoreCase("")) {
			soapNamespace = SOAP11_NS_PREFIX;
			soapVersion = "";
		} else {
			soapNamespace = SOAP12_NS_PREFIX;
		}
		Element soapOperation = doc.createElementNS(soapNamespace, "soap"
				+ soapVersion + ":operation");
		soapOperation.setAttributeNS(soapNamespace, "soapAction", "urn:"
				+ wsdlOperationName);
		soapOperation.setAttributeNS(soapNamespace, "style", "document");
		bindingOperation.appendChild(soapOperation);

		Element input = doc.createElementNS(WSDL11_NS_PREFIX, "wsdl:input");
		Element soapBodyA = doc.createElementNS(soapNamespace, "soap"
				+ soapVersion + ":body");
		soapBodyA.setAttributeNS(soapNamespace, "use", "literal");
		input.appendChild(soapBodyA);
		bindingOperation.appendChild(input);
		Element output = doc.createElementNS(WSDL11_NS_PREFIX, "wsdl:output");
		Element soapBodyB = doc.createElementNS(soapNamespace, "soap"
				+ soapVersion + ":body");
		soapBodyB.setAttributeNS(soapNamespace, "use", "literal");
		output.appendChild(soapBodyB);
		bindingOperation.appendChild(output);
	}

	/**
	 * Creates the 'service' element of the WSDL document.
	 * 
	 * @param doc
	 *            the WSDL document.
	 * @param alName
	 *            the name of the AssemblyLine we use as a template for the WSDL
	 *            document.
	 * @param serviceAddress
	 *            the service address used.
	 * @return the 'service' element for the WSDL document.
	 */
	private static Element createWsdl11Service(Document doc, String alName,
			String serviceAddress) {
		Element service = doc.createElementNS(WSDL11_NS_PREFIX, "wsdl:service");
		service.setAttributeNS(WSDL11_NS_PREFIX, "name", alName);
		Element port11 = doc.createElementNS(WSDL11_NS_PREFIX, "wsdl:port");
		port11.setAttributeNS(WSDL11_NS_PREFIX, "name", alName + "SOAP11Port");
		String binding11 = alName + "SOAP11Binding";
		port11.setAttributeNS(WSDL11_NS_PREFIX, "binding", "ns0:" + binding11);
		Element address11 = doc.createElementNS(SOAP11_NS_PREFIX,
				"soap:address");
		address11.setAttributeNS(SOAP11_NS_PREFIX, "location", serviceAddress);
		port11.appendChild(address11);
		service.appendChild(port11);

		Element port12 = doc.createElementNS(WSDL11_NS_PREFIX, "wsdl:port");
		port12.setAttributeNS(WSDL11_NS_PREFIX, "name", alName + "SOAP12Port");
		String binding12 = alName + "SOAP12Binding";
		port12.setAttributeNS(WSDL11_NS_PREFIX, "binding", "ns0:" + binding12);
		Element address12 = doc.createElementNS(SOAP12_NS_PREFIX,
				"soap12:address");
		address12.setAttributeNS(SOAP12_NS_PREFIX, "location", serviceAddress);
		port12.appendChild(address12);
		service.appendChild(port12);

		return service;
	}

	/**
	 * Creates a custom WSDL version 2.0 document.
	 * 
	 * @param alc
	 *            the assemblyLine configuration that will be a template for the
	 *            WSDL document.
	 * @param serviceAddress
	 *            the service provider address.
	 * @param useSoap12Binding
	 *            whether a SOAP 1.2 binding is needed for the WSDL document. If
	 *            false only SOAP 1.1 binding will be created.
	 * @return the WSDL document.
	 * @throws Exception
	 *             if a problem occurs during the generation of the WSDL
	 *             document.
	 */
	private static Document createWsdl20(AssemblyLineConfig alc,
			String serviceAddress, boolean useSoap12Binding) throws Exception {
		String alName = getALName(alc);
		String thisNamespace = serviceAddress;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document xsdDoc = builder.newDocument();

		String targetNamespace = thisNamespace;

		Element descriptionElement = SchemaUtils.createDescriptionElement(
				xsdDoc, targetNamespace);
		xsdDoc.appendChild(descriptionElement);
		Element typesElement = xsdDoc.createElementNS(WSDL20_NS_PREFIX, "wsdl2:types");
		descriptionElement.appendChild(typesElement);
		Element xsdElement = SchemaUtils.createXSD(xsdDoc, targetNamespace);
		typesElement.appendChild(xsdElement);

		Element interfaceElement = xsdDoc.createElementNS(WSDL20_NS_PREFIX, "wsdl2:interface");
		interfaceElement.setAttributeNS(WSDL20_NS_PREFIX, "name", alName + "Interface");

		Element soap11Binding = xsdDoc.createElementNS(WSDL20_NS_PREFIX, "wsdl2:binding");
		soap11Binding.setAttributeNS(WSDL20_NS_PREFIX, "name", alName + "SOAP11Binding");
		soap11Binding.setAttributeNS(WSDL20_NS_PREFIX, "interface", "tns:" + alName + "Interface");
		soap11Binding.setAttributeNS(WSDL20_NS_PREFIX, "type", "http://www.w3.org/ns/wsdl/soap");
		soap11Binding.setAttributeNS(WSOAP_NS_PREFIX, "wsoap:version", "1.1");

		Element soap12Binding = null;
		if (useSoap12Binding) {
			soap12Binding = xsdDoc.createElementNS(WSDL20_NS_PREFIX, "wsdl2:binding");
			soap12Binding.setAttributeNS(WSDL20_NS_PREFIX, "name", alName + "SOAP12Binding");
			soap12Binding.setAttributeNS(WSDL20_NS_PREFIX, "interface", "tns:" + alName
					+ "Interface");
			soap12Binding
					.setAttributeNS(WSDL20_NS_PREFIX, "type", "http://www.w3.org/ns/wsdl/soap");
			soap12Binding.setAttributeNS(WSOAP_NS_PREFIX, "wsoap:version", "1.2");
		}

		ContainerConfig alOperations = alc.getOperations();
		for (int i = 0; i < alOperations.size(); ++i) {

			OperationConfig opConfig = (OperationConfig) alOperations
					.getConfig(i);
			String wsdlOperationName = getWSDLOperationName(alc, opConfig
					.getShortName());

			Element operation = SchemaUtils.createWsdl20Operation(
					wsdlOperationName, xsdDoc);
			SchemaUtils.addOperationToSchema(opConfig, wsdlOperationName,
					xsdElement, xsdDoc);
			SchemaUtils.addWSDL20OperationToBinding(wsdlOperationName,
					soap11Binding, soap12Binding, xsdDoc);
			interfaceElement.appendChild(operation);
		}

		descriptionElement.appendChild(interfaceElement);
		descriptionElement.appendChild(soap11Binding);
		if (useSoap12Binding) {
			descriptionElement.appendChild(soap12Binding);
		}

		Element serviceElement = SchemaUtils.createWsdl20Service(xsdDoc,
				alName, targetNamespace);
		descriptionElement.appendChild(serviceElement);

		return xsdDoc;
	}

	/**
	 * Creates the 'description' element for the WSDL document.
	 * 
	 * @param doc
	 *            the WSDL document.
	 * @param targetNamespace
	 *            the targetNamespace used.
	 * @return the 'description' element.
	 */
	private static Element createDescriptionElement(Document doc,
			String targetNamespace) {
		Element description = doc.createElementNS(WSDL20_NS_PREFIX, "wsdl2:description");
		description.setAttributeNS(XML_NS_URI, "xmlns:ns0", targetNamespace);
		description.setAttributeNS(XML_NS_URI, "xmlns:tns", targetNamespace);
		description.setAttributeNS(WSDL20_NS_PREFIX, "targetNamespace", targetNamespace);

		return description;
	}

	/**
	 * Creates an 'operation' element for the WSDL 2.0 document.
	 * 
	 * @param wsdlOperationName
	 *            the operation name.
	 * @param doc
	 *            the WSDL document.
	 * @return the created 'operation' element for the 'interface' of the WSDL
	 *         document.
	 */
	private static Element createWsdl20Operation(String wsdlOperationName,
			Document doc) {
		Element operationElement = doc.createElementNS(WSDL20_NS_PREFIX, "wsdl2:operation");
		operationElement.setAttributeNS(WSDL20_NS_PREFIX, "name", wsdlOperationName);
		operationElement.setAttributeNS(WSDL20_NS_PREFIX, "pattern",
				"http://www.w3.org/ns/wsdl/in-out");
		Element inputElement = doc.createElementNS(WSDL20_NS_PREFIX, "wsdl2:input");
		inputElement.setAttributeNS(WSDL20_NS_PREFIX, "element", "ns0:" + wsdlOperationName);
		inputElement.setAttributeNS(WSAW_NS_PREFIX, "wsaw:Action", "urn:" + wsdlOperationName);
		Element outputElement = doc.createElementNS(WSDL20_NS_PREFIX, "wsdl2:output");
		outputElement.setAttributeNS(WSDL20_NS_PREFIX, "element", "ns0:" + wsdlOperationName
				+ "Response");
		outputElement.setAttributeNS(WSDL20_NS_PREFIX, "wsaw:Action", "urn:" + wsdlOperationName
				+ "Response");
		operationElement.appendChild(inputElement);
		operationElement.appendChild(outputElement);

		return operationElement;
	}

	/**
	 * Adds the 'operation' to the 'binding' of the document.
	 * 
	 * @param wsdlOperationName
	 *            the operation name.
	 * @param soap11Binding
	 *            the 'binding' element for SOAP 1.1
	 * @param soap12Binding
	 *            the 'binding' element for SOAP 1.2
	 * @param doc
	 *            the WSDL document.
	 */
	private static void addWSDL20OperationToBinding(String wsdlOperationName,
			Element soap11Binding, Element soap12Binding, Document doc) {
		Element soap11BindingOpElement = doc.createElementNS(WSDL20_NS_PREFIX, "wsdl2:operation");
		soap11BindingOpElement.setAttributeNS(WSDL20_NS_PREFIX, "ref", "tns:" + wsdlOperationName);
		soap11BindingOpElement.setAttributeNS(WSOAP_NS_PREFIX, "wsoap:action", "urn:"
				+ wsdlOperationName);
		soap11Binding.appendChild(soap11BindingOpElement);

		if (soap12Binding != null) {
			Element soap12BindingOpElement = doc
					.createElementNS(WSDL20_NS_PREFIX, "wsdl2:operation");
			soap12BindingOpElement.setAttributeNS(WSDL20_NS_PREFIX, "ref", "tns:"
					+ wsdlOperationName);
			soap12BindingOpElement.setAttributeNS(WSOAP_NS_PREFIX, "wsoap:action", "urn:"
					+ wsdlOperationName);
			soap12Binding.appendChild(soap12BindingOpElement);
		}
	}

	/**
	 * Create 'service' element for the WSDL document.
	 * 
	 * @param doc
	 *            the WSDL document.
	 * @param alName
	 *            the name of the AssemblyLine
	 * @param serviceAddress
	 *            the service address used.
	 * @return a 'service' element for the WSDL document.
	 */
	private static Element createWsdl20Service(Document doc, String alName,
			String serviceAddress) {
		Element service = doc.createElementNS(WSDL20_NS_PREFIX, "wsdl2:service");
		service.setAttributeNS(WSDL20_NS_PREFIX, "name", alName);
		service.setAttributeNS(WSDL20_NS_PREFIX, "interface", "tns:" + alName + "Interface");

		Element endpoint11 = doc.createElementNS(WSDL20_NS_PREFIX, "wsdl2:endpoint");
		endpoint11.setAttributeNS(WSDL20_NS_PREFIX, "name", alName + "SOAP11Endpoint");
		String binding11 = alName + "SOAP11Binding";
		endpoint11.setAttributeNS(WSDL20_NS_PREFIX, "binding", "tns:" + binding11);
		endpoint11.setAttributeNS(WSDL20_NS_PREFIX, "address", serviceAddress);
		service.appendChild(endpoint11);

		Element endpoint12 = doc.createElementNS(WSDL20_NS_PREFIX, "wsdl2:endpoint");
		endpoint12.setAttributeNS(WSDL20_NS_PREFIX, "name", alName + "SOAP12Endpoint");
		String binding12 = alName + "SOAP12Binding";
		endpoint12.setAttributeNS(WSDL20_NS_PREFIX, "binding", "tns:" + binding12);
		endpoint12.setAttributeNS(WSDL20_NS_PREFIX, "address", serviceAddress);
		service.appendChild(endpoint12);

		return service;
	}

	/**
	 * Prints the given document to the output stream.
	 * 
	 * @param document
	 *            the document to be printed.
	 * @param out
	 *            the output stream used for printing.
	 * @throws Exception
	 *             if a problem occurs during the printing.
	 */
	private static void printXML(Document document, OutputStream out)
			throws Exception {
		Transformer transformer = null;
		transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "3");
		Source source = new DOMSource(document);
		Result output = new StreamResult(out);
		transformer.transform(source, output);
	}

	/**
	 * Retrieves the AL name.
	 * 
	 * @param alc
	 *            Assembly Line configuration.
	 * @return Assembly Line name.
	 */
	private static String getALName(AssemblyLineConfig alc) {
		Name name = alc.getName();
		return name.get(name.size() - 1);
	}

	/**
	 * Retrieves web service operation from AL configuration.
	 * 
	 * @param alc
	 *            Assembly Line configuration.
	 * @param alOperationName
	 *            Name of an Assembly Line Operation.
	 * @return A web service operation name.
	 */
	private static String getWSDLOperationName(AssemblyLineConfig alc,
			String alOperationName) {

		String wsdlOperationName = alOperationName;

		String alName = getALName(alc);
		if (alOperationName.equalsIgnoreCase("Default")
				&& alc.getOperation(alName) == null) {

			wsdlOperationName = alName;
		}

		return wsdlOperationName;
	}

	/**
	 * Retrieves the XML schema from DOM document.
	 * 
	 * @param doc
	 *            XML Schema as a DOM document.
	 * @return XML Schema object recognized by Axis2.
	 */
	public static XmlSchema fromDOM(Document doc) {

		XmlSchemaCollection xsc = new XmlSchemaCollection();
		XmlSchema xmlSchema = xsc.read(doc.getDocumentElement());

		return xmlSchema;
	}

	/**
	 * Convert a TDI Schema to a XML Schema complex type element definition.
	 * 
	 * @param doc
	 *            A DOM document, used to create XML nodes.
	 * @param schemaConfig
	 *            TDI Schema.
	 * @param wrapperName
	 *            The name of the complex type element.
	 * @return DOM representation of a XML Schema definition of a complex
	 *         element.
	 */
	public static Element convertSchemaConfigToXSDElement(Document doc,
			SchemaConfig schemaConfig, String wrapperName) {

		List schemaItemNames = schemaConfig.getItemNames();
		int itemsCount = schemaItemNames.size();

		List<Element> xsdElements = new ArrayList<Element>();
		for (int i = 0; i < itemsCount; ++i) {
			SchemaItemConfig sic = schemaConfig.getItem(schemaItemNames.get(i));
			Element e = convertSchemaConfigItemToXSDElement(doc, sic);
			xsdElements.add(e);
		}

		Element wrapper = createXSDComplexElement(doc, wrapperName, xsdElements);

		return wrapper;
	}

	/**
	 * Create an empty XML Schema.
	 * 
	 * @param doc
	 *            Used to create DOM nodes.
	 * @param targetNamespace
	 *            The target namespace of the XML Schema.
	 * @return Empty XML Schema as DOM.
	 */
	public static Element createXSD(Document doc, String targetNamespace) {

		Element xsd = doc.createElementNS(XSD_NS_URI, XSD_NS_PREFIX + ":"
				+ "schema");
		xsd.setAttribute("targetNamespace", targetNamespace);
		xsd.setAttributeNS(XML_NS_URI, "xmlns:" + XSD_NS_PREFIX, XSD_NS_URI);

		return xsd;
	}

	/**
	 * Convert TDI Schema item to XML Schema element definition.
	 * 
	 * @param doc
	 *            Used to create DOM nodes.
	 * @param sic
	 *            TDI Schema item.
	 * @return XML Schema element definition.
	 */
	public static Element convertSchemaConfigItemToXSDElement(Document doc,
			SchemaItemConfig sic) {

		Element result;

		if (sic.isProperty()) {

			result = createXSDAttribute(doc, sic.getAttributeName(),
					getXSDType(sic), sic.isRequired());

		} else {

			if (sic.isLeaf()) {

				result = createXSDSimpleElement(doc, sic.getAttributeName(),
						getXSDType(sic));
			} else {

				ContainerConfig children = sic.getChildSchemaList();

				List<Element> xsdElements = new ArrayList<Element>();
				for (int i = 0; i < children.size(); ++i) {

					SchemaItemConfig child = (SchemaItemConfig) children
							.getConfig(i);
					Element e = convertSchemaConfigItemToXSDElement(doc, child);
					xsdElements.add(e);
				}

				result = createXSDComplexElement(doc, sic.getAttributeName(),
						xsdElements);
			}

			// see minOccurs/maxOccurs only if they differ from the default
			if (sic.getMinOccurrences() != 1) {
				result.setAttribute("minOccurs", "" + sic.getMinOccurrences());
			}
			if (sic.getMaxOccurrences() != 1) {
				if (sic.getMaxOccurrences() <= 0) {
					result.setAttribute("maxOccurs", "unbounded");
				} else {
					result.setAttribute("maxOccurs", ""
							+ sic.getMaxOccurrences());
				}
			}

		}

		return result;
	}

	/**
	 * Retrieves XML schema type from TDI schema item.
	 * 
	 * @param sic
	 *            TDI Schema item.
	 * @return Corresponding XML Schema type.
	 */
	private static String getXSDType(SchemaItemConfig sic) {

		String javaType = sic.getExternalSyntax();
		if (javaType == null) {
			javaType = "java.lang.String";
		}
		javaType = javaType.trim();
		return getXSDType(javaType);
	}

	/**
	 * Retrieves XML schema from a java class.
	 * 
	 * @param javaType
	 *            Fully qualified Java class name.
	 * @return Corresponding XML Schema type.
	 */
	private static String getXSDType(String javaType) {

		QName xsdTypeQName = new TypeTable().getSimpleSchemaTypeName(javaType);
		return xsdTypeQName.getLocalPart();
	}

	/**
	 * Create XML Schema complex type element definition.
	 * 
	 * @param doc
	 *            Used to create DOM nodes.
	 * @param elementName
	 *            The name of the element, whose definition will be created.
	 * @param childElements
	 *            The XML Schema definitions of the child elements.
	 * @return XML Schema element definition.
	 */
	private static Element createXSDComplexElement(Document doc,
			String elementName, List<Element> childElements) {

		Element e = doc.createElementNS(XSD_NS_URI, XSD_NS_PREFIX + ":"
				+ "element");
		e.setAttribute("name", elementName);

		Element complexType = doc.createElementNS(XSD_NS_URI, XSD_NS_PREFIX
				+ ":" + "complexType");
		Element sequence = doc.createElementNS(XSD_NS_URI, XSD_NS_PREFIX + ":"
				+ "sequence");

		for (Iterator<Element> i = childElements.iterator(); i.hasNext();) {

			Element child = i.next();
			sequence.appendChild(child);
		}

		complexType.appendChild(sequence);
		e.appendChild(complexType);

		return e;
	}

	/**
	 * Create XML Schema simple type element definition.
	 * 
	 * @param doc
	 *            Used to create DOM nodes.
	 * @param elementName
	 *            The name of the element, whose definition will be created.
	 * @param xsdType
	 *            The XML Schema type of the element.
	 * @return XML Schema element definition.
	 */
	private static Element createXSDSimpleElement(Document doc,
			String elementName, String xsdType) {

		Element e = doc.createElementNS(XSD_NS_URI, XSD_NS_PREFIX + ":"
				+ "element");
		e.setAttribute("name", elementName);
		e.setAttribute("type", XSD_NS_PREFIX + ":" + xsdType);

		return e;
	}

	/**
	 * Create XML Schema attribute definition.
	 * 
	 * @param doc
	 *            Used to create DOM nodes.
	 * @param attributeName
	 *            The name of the attribute, whose definition will be created.
	 * @param xsdType
	 *            The XML Schema type of the attribute. Must be a simple type.
	 * @param required
	 *            Whether the attribute is required.
	 * @return XML Schema attribute definition.
	 */
	private static Element createXSDAttribute(Document doc,
			String attributeName, String xsdType, boolean required) {

		Element e = doc.createElementNS(XSD_NS_URI, XSD_NS_PREFIX + ":"
				+ "attribute");

		e.setAttribute("name", attributeName);
		e.setAttribute("type", XSD_NS_PREFIX + ":" + xsdType);
		if (required) {
			e.setAttribute("use", "required");
		}

		return e;
	}

	/**
	 * Retrieves the input and output message schema for the given operation
	 * from the give service.
	 * 
	 * @param serviceInp
	 *            The input service from which we will get the operation from.
	 * @param operation
	 *            The name of the operation for which the input and output
	 *            message schema will be extracted.
	 * @return List with SchemaConfig objects containing the extracted schema.
	 * @throws Exception
	 *             If the schema retrieving fails.
	 */
	public static List<SchemaConfig> getInAndOutMessageSchema(
			AxisService serviceInp, String operation) throws Exception {
		List<SchemaConfig> schemas = new Vector<SchemaConfig>();

		// create the schema container for the input message
		SchemaConfig schemaInput = new SchemaConfigImpl();
		schemas.add(schemaInput);
		getInOrOutMessageSchema(serviceInp, operation, true, schemaInput);

		// create the schema container for the output message
		SchemaConfig schemaOutput = new SchemaConfigImpl();
		schemas.add(schemaOutput);
		getInOrOutMessageSchema(serviceInp, operation, false, schemaOutput);
		return schemas;
	}

	/**
	 * Returns a schema information for all operations in the given service. The
	 * schema is returned in TDI Schema (SchemaConfig) object and each operation
	 * schema is a separate item (SchemaConfigItem) in it.
	 * 
	 * @param service
	 *            The service object from which the operations will be extracted
	 *            and their schema will be returned.
	 * @return List with Schema objects with schema for all operations in the
	 *         service.
	 * @throws Exception
	 *             If the schema retrieving fails.
	 */
	public static List<SchemaConfig> getSchemaForAllOperationsInService(
			AxisService service) throws Exception {
		List<SchemaConfig> schemas = new Vector<SchemaConfig>();

		Iterator operations = service.getOperations();
		while (operations.hasNext()) {
			AxisOperation op = (AxisOperation) operations.next();
			// create the schema container for the input message
			SchemaConfig schemaInput = new SchemaConfigImpl();
			schemas.add(schemaInput);
			getInOrOutMessageSchema(service, op.getName().getLocalPart(), true,
					schemaInput);
			// create the schema container for the output message
			SchemaConfig schemaOutput = new SchemaConfigImpl();
			schemas.add(schemaOutput);
			getInOrOutMessageSchema(service, op.getName().getLocalPart(),
					false, schemaOutput);
		}

		return schemas;
	}

	/**
	 * Fills TDI Schema (SchemaConfig) object with the XML Schema for the given
	 * operation.
	 * 
	 * @param serviceInp
	 *            The service where the operation can be found.
	 * @param opName
	 *            The operation name for which the schema will be returned.
	 * @param in
	 *            If true the input message schema will be returned. If false
	 *            the output message schema will be returned.
	 * @param schema
	 *            The object which will be populated with the message schema
	 *            information.
	 * @throws Exception
	 *             If the schema retrieving fails.
	 */
	public static void getInOrOutMessageSchema(AxisService serviceInp,
			String opName, boolean in, SchemaConfig schema) throws Exception {
		AxisOperation operation = serviceInp.getOperation(new QName(opName));

		if (operation == null) {
			throw new Exception(resHash.getString(
					"SchemaUtils.No.Such.Operation.Exception", opName));
		}

		AxisMessage message = null;
		String mep = operation.getMessageExchangePattern();
		if (in) {
			if (mep.endsWith("in-only") || mep.endsWith("in-out")
					|| mep.endsWith("out-in")) {
				message = operation
						.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			}
		} else {
			if (mep.endsWith("out-only") || mep.endsWith("in-out")
					|| mep.endsWith("out-in")) {
				message = operation
						.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
			}
		}

		// the schema name is formed with the operation name
		// and the .Input/.Output postfix. Example: Operation.Input or
		// Operation.Output
		String schemaName = opName + ((in) ? ".Input" : ".Output");
		schema.setName(schemaName);
		if (message != null) {
			SchemaItemConfig sic = schema.newItem(message.getElementQName()
					.getLocalPart());
			handleElement(sic.getChildSchemaList(), message.getSchemaElement(),
					serviceInp.getSchema());
		}
	}

	/**
	 * Fills the container with XSD element specific information.
	 * 
	 * @param cc
	 *            The container which will be filled with the schema
	 *            information.
	 * @param element
	 *            The element from which the schema information will be
	 *            extracted.
	 * @param schemas
	 *            A list with XSD schemas which will be searched when we find an
	 *            extension element.
	 * @throws Exception
	 *             If an Exception occurs while retrieving the schema from
	 *             element.
	 */
	private static void handleElement(ContainerConfig cc,
			XmlSchemaElement element, ArrayList schemas) throws Exception {
		SchemaItemConfig sic = new SchemaItemConfigImpl();
		sic.setAttributeName(element.getName());
		sic.setName(MetamergeConfigFactory.parseName(element.getName()));
		cc.addConfig(sic);
		sic.setMaxOccurrences((int) element.getMaxOccurs());
		sic.setMinOccurrences((int) element.getMinOccurs());
		if (element.getSchemaTypeName() != null)
			sic.setExternalSyntax(element.getSchemaTypeName().getLocalPart());

		if (element.getSchemaType() instanceof XmlSchemaComplexType) {
			XmlSchemaComplexType complex = (XmlSchemaComplexType) element
					.getSchemaType();
			if (complex.getParticle() != null) {
				handleParticle(cc, sic, complex.getParticle(), schemas);
			} else if (complex.getContentModel() != null) {
				handleContent(cc, sic, complex.getContentModel().getContent(),
						schemas);
			}
		} else if (element.getSchemaType() instanceof XmlSchemaSimpleType) {
			handleSimpleType(cc, sic, (XmlSchemaSimpleType) element
					.getSchemaType(), schemas);
		}
	}

	/**
	 * Fills the container with XSD specific information. The information
	 * includes XSD element, any, group, groupref type information.
	 * 
	 * @param cc
	 *            The container which will be filled with the schema
	 *            information.
	 * @param sic
	 *            The element which references this information.
	 * @param particle
	 *            XSD sub elements.
	 * @param schemas
	 *            A list with XSD schemas which will be searched when we find an
	 *            extension element.
	 * @throws Exception
	 *             If an Exception occurs while retrieving the schema from the
	 *             given particle.
	 */
	private static void handleParticle(ContainerConfig cc,
			SchemaItemConfig sic, XmlSchemaParticle particle, ArrayList schemas)
			throws Exception {
		if (particle instanceof XmlSchemaAny) {
			handleAny(cc, (XmlSchemaAny) particle, schemas);
		} else if (particle instanceof XmlSchemaElement) {
			XmlSchemaElement el = (XmlSchemaElement) particle;
			handleElement(sic.getChildSchemaList(), el, schemas);
		} else if (particle instanceof XmlSchemaGroupParticle) {
			handleGroupParticle(sic.getChildSchemaList(),
					(XmlSchemaGroupParticle) particle, schemas);
		} else if (particle instanceof XmlSchemaGroupRef) {
			XmlSchemaGroupRef ref = (XmlSchemaGroupRef) particle;
			handleGroupParticle(cc, ref.getParticle(), schemas);
		}
	}

	/**
	 * Fills the container with XSD simple/complex content specific information.
	 * 
	 * @param cc
	 *            The container which will be filled with the schema
	 *            information.
	 * @param sic
	 *            The element which references the simple/complex content.
	 * @param model
	 *            The simple/complex content.
	 * @param schemas
	 *            A list with XSD schemas which will be searched when we find an
	 *            extension element.
	 * @throws Exception
	 *             If an Exception occurs while retrieving the schema from
	 *             simple/complex content.
	 */
	private static void handleContent(ContainerConfig cc, SchemaItemConfig sic,
			XmlSchemaContent model, ArrayList schemas) throws Exception {
		if (model instanceof XmlSchemaComplexContentExtension) {
			XmlSchemaComplexContentExtension cExt = (XmlSchemaComplexContentExtension) model;
			XmlSchemaType extensionSchemaType = null;
			XmlSchema schema = null;
			for (Iterator iter = schemas.iterator(); iter.hasNext();) {
				schema = (XmlSchema) iter.next();
				extensionSchemaType = getSchemaType(schema, cExt
						.getBaseTypeName());
				if (extensionSchemaType != null) {
					break;
				}
			}
			if (extensionSchemaType instanceof XmlSchemaComplexType) {
				XmlSchemaComplexType complex = (XmlSchemaComplexType) extensionSchemaType;
				if (complex.getParticle() != null) {
					handleParticle(cc, sic, complex.getParticle(), schemas);
				} else if (complex.getContentModel() != null) {
					handleContent(cc, sic, complex.getContentModel()
							.getContent(), schemas);
				}
			}
			handleParticle(cc, sic, cExt.getParticle(), schemas);
		} else if (model instanceof XmlSchemaSimpleContentExtension) {
			XmlSchemaSimpleContentExtension sExt = (XmlSchemaSimpleContentExtension) model;
			XmlSchemaType extensionSchemaType = null;
			XmlSchema schema = null;
			for (Iterator iter = schemas.iterator(); iter.hasNext();) {
				schema = (XmlSchema) iter.next();
				extensionSchemaType = getSchemaType(schema, sExt
						.getBaseTypeName());
				if (extensionSchemaType != null) {
					break;
				}
			}
			if (extensionSchemaType instanceof XmlSchemaSimpleType) {
				XmlSchemaSimpleType simple = (XmlSchemaSimpleType) extensionSchemaType;
				handleSimpleType(cc, sic, simple, schemas);
			}
		}
	}

	/**
	 * Fills the container with XSD simple type specific information.
	 * 
	 * @param cc
	 *            The container which will be filled with the schema
	 *            information.
	 * @param sic
	 *            The element which references the simple type.
	 * @param simple
	 *            The XSD simple type information.
	 * @param schemas
	 *            A list with XSD schemas which will be searched when we find an
	 *            extension element.
	 * @throws Exception
	 *             If an Exception occurs while retrieving the schema from
	 *             simple type.
	 */
	private static void handleSimpleType(ContainerConfig cc,
			SchemaItemConfig sic, XmlSchemaSimpleType simple, ArrayList schemas)
			throws Exception {
		XmlSchemaSimpleTypeContent content = simple.getContent();
		if (content instanceof XmlSchemaSimpleTypeList) {
			// TODO unit tests with list and with SimpleType instead
			XmlSchemaSimpleTypeList list = (XmlSchemaSimpleTypeList) content;
			if (list.getItemType() != null) {
				handleSimpleType(cc, sic, list.getItemType(), schemas);
			} else if (list.getItemTypeName() != null) {
				sic.setExternalSyntax(list.getItemTypeName().getLocalPart());
			}
		}
	}

	/**
	 * Fills the container with XSD complex type specific information.
	 * 
	 * @param cc
	 *            The container which will be filled with the schema
	 *            information.
	 * @param base
	 *            The XSD complex type sub element.
	 * @param schemas
	 *            A list with XSD schemas which will be searched when we find an
	 *            extension element.
	 * @throws Exception
	 *             If an Exception occurs while retrieving the schema from
	 *             complex type.
	 */
	private static void handleGroupParticle(ContainerConfig cc,
			XmlSchemaGroupParticle particle, ArrayList schemas) throws Exception {
		if (particle instanceof XmlSchemaAll)
			handleSchemaAll(cc, (XmlSchemaAll) particle, schemas);
		else if (particle instanceof XmlSchemaChoice)
			handleSchemaChoice(cc, (XmlSchemaChoice) particle, schemas);
		else if (particle instanceof XmlSchemaSequence)
			handleSchemaSequence(cc, (XmlSchemaSequence) particle, schemas);
			
	}

	private static void handleSchemaSequence(ContainerConfig cc,
			XmlSchemaSequence sequence, ArrayList schemas) throws Exception {
		for(XmlSchemaSequenceMember item:  sequence.getItems()) {
			if (item instanceof XmlSchemaAny) {
				handleAny(cc, (XmlSchemaAny) item, schemas);
			} else if (item instanceof XmlSchemaChoice) {
				handleSchemaChoice(cc, (XmlSchemaChoice) item, schemas);
			} else if (item instanceof XmlSchemaElement) {
				handleElement(cc, (XmlSchemaElement) item, schemas);
			} else if (item instanceof XmlSchemaGroup) {
				XmlSchemaGroup group = (XmlSchemaGroup) item;
				handleGroupParticle(cc, group.getParticle(), schemas);
			} else if (item instanceof XmlSchemaGroupRef) {
				XmlSchemaGroupRef ref = (XmlSchemaGroupRef) item;
				handleGroupParticle(cc, ref.getParticle(), schemas);
			} else if (item instanceof XmlSchemaSequence) {
				handleSchemaSequence(cc, (XmlSchemaSequence) item, schemas);
			}
		}
	}

	private static void handleSchemaChoice(ContainerConfig cc,
			XmlSchemaChoice choice, ArrayList schemas) throws Exception {
		for(XmlSchemaChoiceMember item:  choice.getItems()) {
			if (item instanceof XmlSchemaAny) {
				handleAny(cc, (XmlSchemaAny) item, schemas);
			} else if (item instanceof XmlSchemaChoice) {
				handleSchemaChoice(cc, (XmlSchemaChoice) item, schemas);
			} else if (item instanceof XmlSchemaElement) {
				handleElement(cc, (XmlSchemaElement) item, schemas);
			} else if (item instanceof XmlSchemaGroup) {
				XmlSchemaGroup group = (XmlSchemaGroup) item;
				handleGroupParticle(cc, group.getParticle(), schemas);
			} else if (item instanceof XmlSchemaGroupRef) {
				XmlSchemaGroupRef ref = (XmlSchemaGroupRef) item;
				handleGroupParticle(cc, ref.getParticle(), schemas);
			} else if (item instanceof XmlSchemaSequence) {
				handleSchemaSequence(cc, (XmlSchemaSequence) item, schemas);
			}
		}
	}

	private static void handleSchemaAll(ContainerConfig cc,
			XmlSchemaAll base, ArrayList schemas) throws Exception {
		for(XmlSchemaAllMember item:  base.getItems()) {
			if (item instanceof XmlSchemaElement) {
				handleElement(cc, (XmlSchemaElement) item, schemas);
			} else if (item instanceof XmlSchemaGroupRef) {
				XmlSchemaGroupRef ref = (XmlSchemaGroupRef) item;
				handleGroupParticle(cc, ref.getParticle(), schemas);
			} else if (item instanceof XmlSchemaGroup) {
				XmlSchemaGroup group = (XmlSchemaGroup) item;
				handleGroupParticle(cc, group.getParticle(), schemas);
			} else if (item instanceof XmlSchemaAny) {
				handleAny(cc, (XmlSchemaAny) item, schemas);
			}
		}
	}

	/**
	 * Search for a type inside a schema.
	 * 
	 * @param schema
	 *            The schema in which the type will be searched.
	 * @param typeName
	 *            The type which will be searched.
	 * @return Returns schema type if the type is found in the provided schema
	 *         (or its includes) or null if the type was not found.
	 */
	private static XmlSchemaType getSchemaType(XmlSchema schema, QName typeName) {
		XmlSchemaType xmlSchemaType = null;
		if (schema != null) {
			xmlSchemaType = schema.getTypeByName(typeName);
			if (xmlSchemaType == null) {
				// try to find in an import or an include
				List<XmlSchemaExternal> externals = schema.getExternals();
				if (externals != null) {
					for(XmlSchemaExternal object: externals) {
						if (object instanceof XmlSchemaImport) {
							XmlSchema schema1 = ((XmlSchemaImport) object)
									.getSchema();
							xmlSchemaType = getSchemaType(schema1, typeName);
						}
						if (object instanceof XmlSchemaInclude) {
							XmlSchema schema1 = ((XmlSchemaInclude) object)
									.getSchema();
							xmlSchemaType = getSchemaType(schema1, typeName);
						}
						if (xmlSchemaType != null) {
							break;
						}
					}
				}
			}
		}
		return xmlSchemaType;
	}

	/**
	 * Fills the container with XSD any type specific information.
	 * 
	 * @param cc
	 *            The container which will be filled with the schema
	 *            information.
	 * @param any
	 *            The any XSD element.
	 * @param schemas
	 *            A list with XSD schemas which will be searched when we find an
	 *            extension element.
	 * @throws Exception
	 *             If an Exception occurs while retrieving the schema from the
	 *             XSD any element.
	 */
	private static void handleAny(ContainerConfig cc, XmlSchemaAny any,
			ArrayList schemas) throws Exception {
		SchemaItemConfig sic = new SchemaItemConfigImpl();
		sic.setAttributeName("*");
		sic.setName("*");
		sic.setMaxOccurrences((int) any.getMaxOccurs());
		sic.setMinOccurrences((int) any.getMinOccurs());
		cc.addConfig(sic);
	}
}
