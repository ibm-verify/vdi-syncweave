package com.ibm.di.connector.axis2;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;
import org.w3c.dom.Element;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

public class Axis2WSServerConnectorTest {

	public static final int SERVICE_PORT = 9998;
	
	public static final String SERVICE_HOST = "localhost";

	@Test
	public void test_with_complex_wrapped_document_style_wsdl() throws Exception {

		test_request_response(getResourcePath("complex_wrapped_doc.wsdl"), getResourcePath("complex_wrapped_doc_request.xml"),
				getResourcePath("complex_wrapped_doc_response.xml"), "", "findPerson");

	}
	
	@Test
	public void test_http_basic_auth_accept_valid_credentials() throws Exception {
		
		final String username = "myuser";
		final String password = "mypass";
		
		WSServerTestSetup test = new WSServerTestSetup();
		test.setServiceLocation(SERVICE_HOST, SERVICE_PORT);
		test.setWSDLFilePath(getResourcePath("simple_doc.wsdl"));
		test.setOperationName("getAge");
		test.loadRequestSOAPFromFile(getResourcePath("simple_doc_request.xml"));
		test.loadResponsePayloadFromFile(getResourcePath("simple_doc_response.xml"));
		test.getResponseEntry().setAttribute("$authResult", "true");
		test.setCredentials(username, password);
		
		test.execute();
		
		test.validateDocStyleRequest();
		test.validateDocStyleResponse();
		
		Entry requestEntry = test.getRequestEntry();
		assertEquals(username, requestEntry.getString("http.remote_user"));
		assertEquals(password, requestEntry.getString("http.remote_pass"));
	}
	
	@Test
	public void test_http_basic_auth_reject_invalid_credentials() throws Exception {
		
		final String username = "myuser";
		final String password = "mypass";
		
		WSServerTestSetup test = new WSServerTestSetup();
		test.setServiceLocation(SERVICE_HOST, SERVICE_PORT);
		test.setWSDLFilePath(getResourcePath("simple_doc.wsdl"));
		test.setOperationName("getAge");
		test.loadRequestSOAPFromFile(getResourcePath("simple_doc_request.xml"));
		test.loadResponsePayloadFromFile(getResourcePath("simple_doc_response.xml"));
		test.getResponseEntry().setAttribute("$authResult", "false");
		test.setCredentials(username, password);
		
		test.execute();
		
		assertEquals(401, test.getHTTPResponseCode());
		
		Entry requestEntry = test.getRequestEntry();
		assertEquals(username, requestEntry.getString("http.remote_user"));
		assertEquals(password, requestEntry.getString("http.remote_pass"));
	}
	
	@Test
	public void test_http_basic_auth_reject_missing_credentials() throws Exception {
		
		WSServerTestSetup test = new WSServerTestSetup();
		test.setServiceLocation(SERVICE_HOST, SERVICE_PORT);
		test.setWSDLFilePath(getResourcePath("simple_doc.wsdl"));
		test.setOperationName("getAge");
		test.loadRequestSOAPFromFile(getResourcePath("simple_doc_request.xml"));
		test.loadResponsePayloadFromFile(getResourcePath("simple_doc_response.xml"));
		test.getResponseEntry().setAttribute("$authResult", "false");
		test.setUseHTTPBasicAuth(true);
		
		test.execute();
		
		assertEquals(401, test.getHTTPResponseCode());
	}
	
	@Test
	public void test_with_multiple_services_wsdl_service_name_specified_explicitly() throws Exception {

		test_request_response(getResourcePath("multiple_services.wsdl"), getResourcePath("multiple_services_request.xml"),
				getResourcePath("multiple_services_response.xml"), "first_service", "getAge");

	}
	
	@Test(expected= Exception.class)
	public void test_with_multiple_services_wsdl_service_name_not_specified() throws Exception {
		
		WSServerTestSetup test = new WSServerTestSetup();
		test.setServiceLocation(SERVICE_HOST, SERVICE_PORT);
		test.setWSDLFilePath(getResourcePath("multiple_services.wsdl"));
		test.setOperationName("getAge");
		
		test.execute();
	}
	
	@Test
	public void test_with_rpc_style_wsdl() throws Exception {
	
		WSServerTestSetup test = new WSServerTestSetup();
		test.setServiceLocation(SERVICE_HOST, SERVICE_PORT);
		test.setWSDLFilePath(getResourcePath("rpc.wsdl"));
		test.setOperationName("Square_SimpleWebService");
		test.loadRequestSOAPFromFile(getResourcePath("rpc_request.xml"));
		test.loadResponsePayloadFromFile(getResourcePath("/rpc_response.xml"));
		
		test.execute();
		
		// validate request
		Entry requestEntry = test.getRequestEntry();
		Attribute opAttr = requestEntry.getAttribute("Square_SimpleWebService");
		assertNotNull(opAttr);
		assertEquals("ns:Square_SimpleWebService_thisNamespace", opAttr.getNamespaceURI());
		Element number = SOAPUtils.getChildElement(opAttr, "number");
		assertNotNull(number);
		assertEquals("ns:Square_SimpleWebService_thisNamespace", number.getNamespaceURI());
		assertEquals("5", number.getTextContent().trim());
		
		// validate response
		assertEquals(200, test.getHTTPResponseCode());
		Element soapResponseElem = test.getSOAPResponseElement();
		SOAPUtils.validateSOAPEnvelope(soapResponseElem);
		Element payload = SOAPUtils.getSOAPMessagePayload(soapResponseElem);
		assertEquals("Square_SimpleWebServiceResponse", payload.getLocalName());
		assertEquals("ns:Square_SimpleWebService_thisNamespace", payload.getNamespaceURI());
		List<Element> children = SOAPUtils.getChildElements(payload);
		assertEquals(1, children.size());
		Element child = children.get(0);
		assertEquals("square", child.getLocalName());
		assertEquals("ns:Square_SimpleWebService_thisNamespace", child.getNamespaceURI());
		assertEquals("25", child.getTextContent().trim());
	}
	
	@Test
	public void test_custom_soap_header() throws Exception {
		
		WSServerTestSetup test = new WSServerTestSetup();
		test.setServiceLocation(SERVICE_HOST, SERVICE_PORT);
		test.setWSDLFilePath(getResourcePath("simple_doc.wsdl"));
		test.setOperationName("getAge");
		test.loadRequestSOAPFromFile(getResourcePath("simple_doc_customheader_request.xml"));
		test.loadResponsePayloadFromFile(getResourcePath("/simple_doc_response.xml"));
		
		// add a custom header to the response entry
		Entry responseEntry = test.getResponseEntry();
		Attribute headerAttr = responseEntry.newAttribute(Axis2WSServerConnector.ATTR_SOAP_HEADER);
		Element header1 = responseEntry.createElementNS("ns:first", "ns:first");
		header1.appendChild(responseEntry.createTextNode("firsttext"));
		Element header2 = responseEntry.createElementNS("ns:second", "ns:second");
		header2.appendChild(responseEntry.createTextNode("secondtext"));
		headerAttr.appendChild(header1);
		headerAttr.appendChild(header2);
		
		test.execute();
		
		test.validateDocStyleRequest();
		test.validateDocStyleResponse();

		// validate the custom headers in the request
		Entry requestEntry = test.getRequestEntry();
		headerAttr = requestEntry.getAttribute(Axis2WSServerConnector.ATTR_SOAP_HEADER);
		validateCustomHeaders(SOAPUtils.getChildElements(headerAttr));
		
		// validate the custom headers in the response
		Element soapResponseElem = test.getSOAPResponseElement();
		validateCustomHeaders(SOAPUtils.getSOAPMessageHeaders(soapResponseElem));
	}
	
	@Test
	public void test_soap11_fault() throws Exception {
		
		WSServerTestSetup test = new WSServerTestSetup();
		test.setServiceLocation(SERVICE_HOST, SERVICE_PORT);
		test.setWSDLFilePath(getResourcePath("simple_doc.wsdl"));
		test.setOperationName("getAge");
		test.loadRequestSOAPFromFile(getResourcePath("simple_doc_request.xml"));
		
		// add fault info to the response entry
		Entry responseEntry = test.getResponseEntry();
		responseEntry.setAttribute("$faultCode", "testcode");
		responseEntry.setAttribute("$faultCodeNamespaceURI", "ns:test");
		responseEntry.setAttribute("$faultCodeNamespacePrefix", "test");
		responseEntry.setAttribute("$faultNode", "testNode");
		responseEntry.setAttribute("$faultRole", "testRole");
		responseEntry.setAttribute("$faultReason", "testcode");
		
		test.execute();
		
		test.validateDocStyleRequest();
		
		// validate the fault response
		Element soapResponseElem = test.getSOAPResponseElement();
		Element soapBody = SOAPUtils.getChildElement(soapResponseElem, "body");
		Element payload = SOAPUtils.getChildElements(soapBody).get(0);
		assertEquals("fault", payload.getLocalName().toLowerCase());
	}
	
	@Test
	public void test_soap12_fault() throws Exception {
		
		WSServerTestSetup test = new WSServerTestSetup();
		test.setServiceLocation(SERVICE_HOST, SERVICE_PORT);
		test.setWSDLFilePath(getResourcePath("simple_doc.wsdl"));
		test.setOperationName("getAge");
		test.loadRequestSOAPFromFile(getResourcePath("simple_doc_soap12_request.xml"));
		// set appropriate content type for SOAP 1.2, otherwise Axis2 complains
		test.setRequestContentType("application/soap+xml");
		
		// add fault info to the response entry
		Entry responseEntry = test.getResponseEntry();
		responseEntry.setAttribute("$faultCode", "Sender");
		responseEntry.setAttribute("$faultCodeNamespaceURI", "http://www.w3.org/2003/05/soap-envelope");
		responseEntry.setAttribute("$faultCodeNamespacePrefix", "env");
		responseEntry.setAttribute("$faultReason", "testreason");
		Attribute detailAttr = responseEntry.newAttribute("$faultDetail");
		detailAttr.appendChild( responseEntry.createElementNS("ns:test", "test:testdetail") );
		
		test.execute();
		
		test.validateDocStyleRequest();
		
		// validate the fault response
		Element soapResponseElem = test.getSOAPResponseElement();
		SOAPUtils.validateSOAP12Envelope(soapResponseElem);
		Element soapBody = SOAPUtils.getChildElement(soapResponseElem, "body");
		Element payload = SOAPUtils.getChildElements(soapBody).get(0);
		assertEquals("fault", payload.getLocalName().toLowerCase());
		Element detailElem = SOAPUtils.getChildElement(payload, "detail");
		Element detailContent = SOAPUtils.getChildElement(detailElem, "testdetail");
		assertEquals("ns:test", detailContent.getNamespaceURI().trim());
	}
	
	@Test
	public void test_one_way() throws Exception {
		
		WSServerTestSetup test = new WSServerTestSetup();
		test.setServiceLocation(SERVICE_HOST, SERVICE_PORT);
		test.setWSDLFilePath(getResourcePath("simple_doc_one_way.wsdl"));
		test.setOperationName("getAge");
		test.loadRequestSOAPFromFile(getResourcePath("simple_doc_request.xml"));
		
		test.execute();
		
		test.validateDocStyleRequest();
		
		assertEquals(200, test.getHTTPResponseCode());
		
		String response = test.getSOAPResponseString();
		if (response == null) {
			response = "";
		}
		assertEquals("", response);
	}
	
	@Test
	public void test_with_simple_doc_style_wsdl_soap11() throws Exception {
		
		test_request_response(getResourcePath("simple_doc.wsdl"), getResourcePath("simple_doc_request.xml"),
				getResourcePath("simple_doc_response.xml"), "", "getAge");
	}
	
	@Test
	public void test_with_simple_doc_style_wsdl_soap12() throws Exception {
		test_request_response(getResourcePath("simple_doc_soap12.wsdl"), getResourcePath("simple_doc_soap12_request.xml"),
				getResourcePath("simple_doc_soap12_response.xml"), "", "getAge", "application/soap+xml");
	}
	
	@Test
	public void test_with_wsdl_20() throws Exception {
		
		test_request_response(getResourcePath("simple_doc_wsdl20.wsdl"), getResourcePath("simple_doc_request.xml"),
				getResourcePath("simple_doc_response.xml"), "", "getAge");
	}
	
	@Test
	public void test_with_simple_wrapped_doc_style_wsdl() throws Exception {
		
		test_request_response(getResourcePath("simple_wrapped_doc.wsdl"), getResourcePath("simple_wrapped_doc_request.xml"),
				getResourcePath("simple_wrapped_doc_response.xml"), "", "getAge");
	}
	
	@Test
	public void test_library_example() throws Exception {

		test_request_response(getResourcePath("library_wsdl2.wsdl"), getResourcePath("library_request.xml"),
				getResourcePath("library_response.xml"), "LibraryService", "GetBookAvailability");

	}
	
	private static String getResourcePath(String resourceName) {
		return "./resources/Axis2WSServerConnectorTest/"+resourceName;
	}
	
	private void validateCustomHeaders(List<Element> headers) {
		assertEquals(2, headers.size());
		assertEquals("ns:first", headers.get(0).getNamespaceURI());
		assertEquals("first", headers.get(0).getLocalName());
		assertEquals("firsttext", headers.get(0).getTextContent());
		assertEquals("ns:second", headers.get(1).getNamespaceURI());
		assertEquals("second", headers.get(1).getLocalName());
		assertEquals("secondtext", headers.get(1).getTextContent());
	}
	
	private void test_request_response(String wsdlFilePath, String requestFilePath, String responseFilePath, String serviceName, String operationName) throws Exception {
		test_request_response(wsdlFilePath, requestFilePath, responseFilePath, serviceName, operationName, "text/xml;charset=utf-8");
	}
	
	private void test_request_response(String wsdlFilePath, String requestFilePath, String responseFilePath, String serviceName, String operationName, String requestContentType) throws Exception {
		
		WSServerTestSetup test = new WSServerTestSetup();
		test.setServiceLocation(SERVICE_HOST, SERVICE_PORT);
		test.setWSDLFilePath(wsdlFilePath);
		test.setOperationName(operationName);
		test.setServiceName(serviceName);
		test.loadRequestSOAPFromFile(requestFilePath);
		test.loadResponsePayloadFromFile(responseFilePath);
		test.setRequestContentType(requestContentType);
		
		test.execute();
		
		test.validateDocStyleRequest();
		test.validateDocStyleResponse();
	}

}
