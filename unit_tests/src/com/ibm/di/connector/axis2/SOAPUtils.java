package com.ibm.di.connector.axis2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Validates only SOAP responses for document style operations. Works with both
 * WSDL 1.1 and WSDL 2.0.
 */
public class SOAPUtils {
	
	public static final String XML_XSD_FILE_PATH = "./resources/Axis2WSServerConnectorTest/xml.xsd";
	
	public static final String SOAP11_XSD_FILE_PATH = "./resources/Axis2WSServerConnectorTest/soapEnvelope11.xsd";
	
	public static final String SOAP12_XSD_FILE_PATH = "./resources/Axis2WSServerConnectorTest/soapEnvelope12.xsd";
	
	public static Element getSOAPMessagePayload(Element soapMsg) throws Exception {
		
		Element soapBody = getChildElement(soapMsg, "Body");

		if (soapBody == null) {
			throw new Exception("Missing SOAP body.");
		}

		List<Element> children = getChildElements(soapBody);

		if (children.size() != 1) {
			throw new Exception(
					"The SOAP body must contain exactly one child element. Found "
							+ children.size() + " elements.");
		}

		Element payload = children.get(0);
		
		return payload;
	}
	
	public static List<Element> getSOAPMessageHeaders(Element soapMsg) throws Exception {
		
		Element soapHeader = getChildElement(soapMsg, "Header");

		if (soapHeader == null) {
			throw new Exception("Missing SOAP header.");
		}

		List<Element> children = getChildElements(soapHeader);
		
		return children;
	}

	/**
	 * Validate a SOAP response message for a document style operation. The
	 * method does not work for RPC style operations.
	 */
	public static void validateDocStyleSOAPResponse(Element soapResponse,
			String wsdlFilePath, String operationName) throws Exception {

		validateSOAPEnvelope(soapResponse);

		Element payload = getSOAPMessagePayload(soapResponse);

		validateWSDLType(payload, wsdlFilePath);

		String responseElementName = getMessageElementName(wsdlFilePath,
				operationName, false);

		if (responseElementName == null) {
			throw new RuntimeException(
					"Could not find the name of the element, which corresponds to the output message of operation "
							+ operationName);
		}

		if (!responseElementName.equals(payload.getLocalName())) {
			throw new Exception(
					"The name of the element inside the SOAP body does not match the WSDL definition. Expected "
							+ responseElementName
							+ ". Found "
							+ payload.getLocalName());
		}
	}

	/**
	 * Validate the payload from the SOAP body of a request for a document style
	 * operation. The methods does not work for RPC style operations.
	 */
	public static void validateDocStyleSOAPRequestPayload(Element payload,
			String wsdlFilePath, String operationName) throws Exception {

		validateWSDLType(payload, wsdlFilePath);

		String requestElementName = getMessageElementName(wsdlFilePath,
				operationName, true);

		if (requestElementName == null) {
			throw new RuntimeException(
					"Could not find the name of the element, which corresponds to the input message of operation "
							+ operationName
							+ " in the WSDL document at "
							+ wsdlFilePath);
		}

		if (!requestElementName.equals(payload.getLocalName())) {
			throw new Exception(
					"The name of the element inside the SOAP body does not match the WSDL definition. Expected "
							+ requestElementName
							+ ". Found "
							+ payload.getLocalName());
		}
	}

	public static Element parseFile(String xmlFilePath) throws SAXException,
			IOException {

		return getDOMParser().parse(new File(xmlFilePath)).getDocumentElement();
	}

	public static Element parseString(String str) throws SAXException,
			IOException {

		return getDOMParser().parse(new ByteArrayInputStream(str.getBytes()))
				.getDocumentElement();
	}

	public static DocumentBuilder getDOMParser() {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder parser;
		try {
			parser = factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException("Cannot create XML parser: " + ex, ex);
		}

		return parser;
	}

	/**
	 * Convert an XML element between two DOM implementations.
	 * 
	 * @param src
	 *            An XML element of the source implementation.
	 * @param doc
	 *            A document of the target implementation.
	 * @return An XML element of the target implementation.
	 */
	public static Element convertDOM(Element src, Document doc) {

		Element dst;
		if (src.getNamespaceURI() != null) {
			dst = doc.createElementNS(src.getNamespaceURI(), src.getNodeName());
		} else {
			dst = doc.createElement(src.getLocalName());
		}

		NamedNodeMap attrMap = src.getAttributes();
		for (int i = 0; i < attrMap.getLength(); ++i) {
			Attr attr = (Attr) attrMap.item(i);

			if (attr.getNamespaceURI() != null) {

				String qualifiedName = attr.getLocalName();
				if (attr.getPrefix() != null && attr.getPrefix().length() > 0) {
					qualifiedName = attr.getPrefix() + ":" + qualifiedName;
				}

				dst.setAttributeNS(attr.getNamespaceURI(), qualifiedName, attr
						.getValue());
			} else {
				dst.setAttribute(attr.getLocalName(), attr.getValue());
			}
		}

		NodeList nodes = src.getChildNodes();
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node child = nodes.item(i);

			Node copyChild = null;
			int nodeType = child.getNodeType();
			switch (nodeType) {
			case Node.CDATA_SECTION_NODE:
				CDATASection cdata = (CDATASection) child;
				copyChild = doc.createCDATASection(cdata.getData());
				break;
			case Node.ELEMENT_NODE:
				copyChild = convertDOM((Element) child, doc);
				break;
			case Node.TEXT_NODE:
				Text text = (Text) child;
				copyChild = doc.createTextNode(text.getData());
				break;
			default:
				// unrecognized node - skip it
			}

			if (copyChild != null) {
				dst.appendChild(copyChild);
			}
		}

		return dst;
	}

	/**
	 * Get the local name of the XML element which corresponds to the message of
	 * the specified operation.
	 */
	public static String getMessageElementName(String wsdlFilePath,
			String operationName, boolean isRequest) throws SAXException,
			IOException {

		Element wsdl = parseFile(wsdlFilePath);

		String result;
		if ("description".equals(wsdl.getLocalName())) {
			result = getMessageElementNameWSDL20(wsdl, operationName, isRequest);
		} else if ("definitions".equals(wsdl.getLocalName())) {
			result = getMessageElementNameWSDL11(wsdl, operationName, isRequest);
		} else {
			throw new RuntimeException(
					"Unknown WSDL version. The root element name is "
							+ wsdl.getLocalName());
		}

		return result;
	}

	private static String getMessageElementNameWSDL11(Element wsdl,
			String operationName, boolean isRequest) {

		String result = null;

		// Find the message name
		String messageName = null;
		List<Element> portTypes = getChildElements(wsdl, "portType");
		for (Iterator<Element> i = portTypes.iterator(); i.hasNext();) {

			Element portType = i.next();

			List<Element> opers = getChildElements(portType, "operation");
			for (Iterator<Element> j = opers.iterator(); j.hasNext();) {

				Element oper = j.next();

				if (operationName.equals(oper.getAttribute("name"))) {

					if (result != null) {
						throw new RuntimeException(
								"Found multiple definitions of operation with name "
										+ operationName);
					}

					final String msgType = isRequest ? "input" : "output";

					Element msg = getChildElement(oper, msgType);

					messageName = noPrefix(msg.getAttribute("message"));
				}
			}
		}

		if (messageName == null) {
			throw new RuntimeException(
					"Could not find the name of the output message for operation "
							+ operationName);
		}

		// Find the message definition and get the element name
		List<Element> messages = getChildElements(wsdl, "message");
		for (Iterator<Element> i = messages.iterator(); i.hasNext();) {

			Element message = i.next();

			if (messageName.equals(message.getAttribute("name"))) {

				List<Element> parts = getChildElements(message, "part");
				if (parts.size() != 1) {
					throw new RuntimeException(
							"Each message in WSDL 1.1 document style must have exactly one part. Found "
									+ parts.size() + " parts for message "
									+ messageName);
				}
				Element part = parts.get(0);

				result = noPrefix(part.getAttribute("element"));
			}
		}

		return result;
	}

	private static String getMessageElementNameWSDL20(Element wsdl,
			String operationName, boolean isRequest) {

		String result = null;

		List<Element> interfaces = getChildElements(wsdl, "interface");
		for (Iterator<Element> i = interfaces.iterator(); i.hasNext();) {

			Element interf = i.next();

			List<Element> opers = getChildElements(interf, "operation");
			for (Iterator<Element> j = opers.iterator(); j.hasNext();) {

				Element oper = j.next();

				if (operationName.equals(oper.getAttribute("name"))) {

					final String msgType = isRequest ? "input" : "output";

					Element msg = getChildElement(oper, msgType);

					if (result != null) {
						throw new RuntimeException(
								"Found multiple definitions of operation with name "
										+ operationName);
					}

					result = noPrefix(msg.getAttribute("element"));
				}
			}
		}

		return result;
	}

	/**
	 * Validate a XML element against the inline XML Schema of a WSDL document.
	 */
	private static void validateWSDLType(Element e, String wsdlFilePath)
			throws SAXException, IOException {

		List<Element> wsdlXSD = new ArrayList<Element>();

		Element xsd = getXSDFromWSDL(wsdlFilePath);

		if (xsd == null) {
			throw new RuntimeException(
					"Did not find a XML Schema definition inside the WSDL document at "
							+ wsdlFilePath);
		} else {
			wsdlXSD.add(xsd);
		}

		validateDOM(e, wsdlXSD);
	}

	/**
	 * Validate a SOAP envelope against the SOAP 1.1 XML Schemas.
	 * 
	 * @param soapMsg
	 *            A whole SOAP envelope.
	 * @throws SAXException
	 *             A validation assertion.
	 */
	public static void validateSOAP11Envelope(Element soapMsg)
			throws SAXException {

		List<Element> xsds = new ArrayList<Element>();
		try {
			xsds.add(parseFile(XML_XSD_FILE_PATH));
			xsds.add(parseFile(SOAP11_XSD_FILE_PATH));
		} catch (Exception ex) {
			throw new RuntimeException(
					"Cannot load XML Schemas for SOAP 1.1 validation: " + ex,
					ex);
		}

		validateDOM(soapMsg, xsds);
	}

	/**
	 * Validate a SOAP envelope against the SOAP 1.2 XML Schemas.
	 * 
	 * @param soapMsg
	 *            A whole SOAP envelope.
	 * @throws SAXException
	 *             A validation assertion.
	 */
	public static void validateSOAP12Envelope(Element soapMsg)
			throws SAXException {

		List<Element> xsds = new ArrayList<Element>();
		try {
			xsds.add(parseFile(XML_XSD_FILE_PATH));
			xsds.add(parseFile(SOAP12_XSD_FILE_PATH));
		} catch (Exception ex) {
			throw new RuntimeException(
					"Cannot load XML Schemas for SOAP 1.2 validation: " + ex,
					ex);
		}

		validateDOM(soapMsg, xsds);
	}

	/**
	 * Validate a SOAP envelope against the SOAP 1.1 and SOAP 1.2 XML Schemas.
	 * 
	 * @param soapMsg
	 *            A whole SOAP envelope.
	 * @throws SAXException
	 *             A validation assertion.
	 */
	public static void validateSOAPEnvelope(Element soapMsg)
			throws SAXException {

		List<Element> xsds = new ArrayList<Element>();
		try {
			xsds.add(parseFile(XML_XSD_FILE_PATH));
			xsds.add(parseFile(SOAP11_XSD_FILE_PATH));
			xsds.add(parseFile(SOAP12_XSD_FILE_PATH));
		} catch (Exception ex) {
			throw new RuntimeException(
					"Cannot load standard XML Schemas for SOAP validation: "
							+ ex, ex);
		}

		validateDOM(soapMsg, xsds);
	}

	/**
	 * Get the inline XML Schema definition from a WSDL document as a XML
	 * element.
	 */
	private static Element getXSDFromWSDL(String wsdlFilePath)
			throws SAXException, IOException {

		Element wsdlRoot = parseFile(wsdlFilePath);

		Element typesElem = getChildElement(wsdlRoot, "types");

		Element xsd = null;
		if (typesElem != null) {
			xsd = getChildElement(typesElem, "schema");
		}

		return xsd;
	}

	/**
	 * Get the first child element, whose local name is the same as the
	 * specified name (ignore case).
	 */
	public static Element getChildElement(Element e, String name) {

		Element result = null;

		List<Element> children = getChildElements(e, name);
		if (children.size() > 0) {
			result = children.get(0);
		}

		return result;
	}

	/**
	 * Get a list of all child elements, whose local name is the same as the
	 * specified name (ignore case).
	 */
	public static List<Element> getChildElements(Element e, String name) {

		List<Element> result = new ArrayList<Element>();

		List<Element> children = getChildElements(e);
		for (Iterator<Element> i = children.iterator(); i.hasNext();) {
			Element child = i.next();
			if (name.equalsIgnoreCase(child.getLocalName())) {
				result.add(child);
			}
		}

		return result;
	}

	/**
	 * Get a list of all child elements.
	 */
	public static List<Element> getChildElements(Element e) {

		List<Element> result = new ArrayList<Element>();

		NodeList childNodes = e.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); ++i) {

			Node child = childNodes.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				result.add((Element) child);
			}
		}

		return result;
	}

	/**
	 * Validate an XML element against an XML Schema definition.
	 */
	public static void validateDOM(Element elem, String xsdFilePath)
			throws SAXException, IOException {
		Element xsd = parseFile(xsdFilePath);
		List<Element> xsdList = new ArrayList<Element>(1);
		xsdList.add(xsd);
		validateDOM(elem, xsdList);
	}

	/**
	 * Validate an XML element against a collection of XML Schema definitions.
	 * 
	 * @param elem
	 *            The XML element to validate.
	 * @param xsds
	 *            A list of the XML Schema definitions. Each XML Schema is
	 *            represented as an XML element named 'schema'.
	 * @throws SAXException
	 *             A validation assertion.
	 */
	private static void validateDOM(Element elem, List<Element> xsds)
			throws SAXException {

		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		Source[] schemaSources = new Source[xsds.size()];
		for (int i = 0; i < schemaSources.length; ++i) {
			schemaSources[i] = new DOMSource(xsds.get(i));
		}

		Schema schema = factory.newSchema(schemaSources);

		Validator validator = schema.newValidator();
		if (elem == null) {
			throw new RuntimeException("element is null");
		}
		try {
			validator.validate(new DOMSource(elem));
		} catch (IOException ex) {
			throw new RuntimeException("Unexpected IO problem: " + ex, ex);
		}
	}

	/**
	 * Remove the prefix (if any) of a qualified name.
	 */
	private static String noPrefix(String str) {

		if (str == null) {
			return null;
		}

		int colonIndex = str.indexOf(':');
		if (colonIndex != -1) {
			// remove the namespace prefix
			return str.substring(colonIndex + 1);
		} else {
			return str;
		}
	}
	
	public static String convertDOMToString(Node node) throws Exception {
		TransformerFactory tFactory =
		    TransformerFactory.newInstance();
		  Transformer transformer = tFactory.newTransformer();

		  DOMSource source = new DOMSource(node);
		  StringWriter strWriter = new StringWriter();
		  StreamResult result = new StreamResult(strWriter);
		  transformer.transform(source, result);
		  transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		  return strWriter.toString();
	}
	
}
