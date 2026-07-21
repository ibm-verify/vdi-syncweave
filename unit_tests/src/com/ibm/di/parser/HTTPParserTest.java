package com.ibm.di.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.internet.MimeUtility;

import org.junit.Test;

import com.ibm.di.entry.Entry;

/**
 * <pre>
 * What is tested: - some reading scenarios
 * 
 * What is still not tested: - reading multiple messages from the same stream -
 * writing - writing multiple messages to the same stream - writing when
 * http.body is java.io.File - chunks - methods which are not from the
 * ParserInterface - authentication/forbidden - headers as properties - change
 * history of HTTPParser.java in CMVC (defects/features)
 * 
 * </pre>
 */
public class HTTPParserTest {

	public static final String PARAM_CLIENT_MODE = "clientMode";

	public static final String PARAM_CHARACTER_SET = "characterSet";

	public static final String ATTR_HTTP_BASE = "http.base";

	public static final String ATTR_HTTP_URL = "http.url";

	public static final String ATTR_HTTP_HOST = "http.host";

	public static final String ATTR_HTTP_METHOD = "http.method";

	public static final String ATTR_HTTP_BODY = "http.body";

	public static final String ATTR_HTTP_CONTENT_LENGTH = "http.Content-Length";

	public static final String ATTR_HTTP_RESPONSE_CODE = "http.responseCode";

	public static final String ATTR_HTTP_RESPONSE_MSG = "http.responseMsg";

	public static final String NEW_LINE = "\r\n";

	@Test
	public void test_read_request_with_no_body() throws Exception {

		final String request = "" + // 
				"GET /encrypted-area HTTP/1.1" + "\r\n" + //
				"Host: www.example.com" + "\r\n" + //
				"\r\n";

		Entry entry = readRequest(request);

		assertEquals("GET", entry.getString(ATTR_HTTP_METHOD));
		assertEquals("/encrypted-area", entry.getString(ATTR_HTTP_BASE));
		assertEquals("/encrypted-area", entry.getString(ATTR_HTTP_URL));
		assertEquals("www.example.com", entry.getString(ATTR_HTTP_HOST));
		assertNull(entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_read_request_whose_request_uri_is_absolute_path() throws Exception {

		// see http://tools.ietf.org/html/rfc2616#section-5.1.2

		final String request = "" + // 
				"GET http://www.w3.org/pub/WWW/TheProject.html HTTP/1.1" + "\r\n" + //
				"Host: www.w3.org" + "\r\n" + //
				"\r\n";

		Entry entry = readRequest(request);

		assertEquals("GET", entry.getString(ATTR_HTTP_METHOD));
		assertEquals("http://www.w3.org/pub/WWW/TheProject.html", entry.getString(ATTR_HTTP_BASE));
		assertEquals("http://www.w3.org/pub/WWW/TheProject.html", entry.getString(ATTR_HTTP_URL));
		assertEquals("www.w3.org", entry.getString(ATTR_HTTP_HOST));
		assertNull(entry.getString(ATTR_HTTP_BODY));
	}

	/**
	 * CMVC Defect 9749
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_read_request_whose_request_uri_is_absolute_uri() throws Exception {

		// see http://tools.ietf.org/html/rfc2616#section-5.1.2

		final String request = "" + // 
				"GET http://www.w3.org/pub/WWW/TheProject.html HTTP/1.1" + "\r\n" + //
				"Host: www.w3.org" + "\r\n" + //
				"\r\n";

		Entry entry = readRequest(request);

		assertEquals("GET", entry.getString(ATTR_HTTP_METHOD));
		assertEquals("http://www.w3.org/pub/WWW/TheProject.html", entry.getString(ATTR_HTTP_BASE));
		assertEquals("http://www.w3.org/pub/WWW/TheProject.html", entry.getString(ATTR_HTTP_URL));
		assertEquals("www.w3.org", entry.getString(ATTR_HTTP_HOST));
		assertNull(entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_read_request_whose_request_uri_is_asterisk() throws Exception {

		// see http://tools.ietf.org/html/rfc2616#section-5.1.2

		final String request = "" + // 
				"OPTIONS * HTTP/1.1" + "\r\n" + //
				"Host: www.example.com" + "\r\n" + //
				"\r\n";

		Entry entry = readRequest(request);

		assertEquals("OPTIONS", entry.getString(ATTR_HTTP_METHOD));
		assertEquals("*", entry.getString(ATTR_HTTP_BASE));
		assertEquals("*", entry.getString(ATTR_HTTP_URL));
		assertEquals("www.example.com", entry.getString(ATTR_HTTP_HOST));
		assertNull(entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_read_request_whose_request_uri_contains_encoded_characters() throws Exception {

		final String request = "" + // 
				"GET http://www.w3.org/pub/WWW/The%20%2f%20Project.html HTTP/1.1" + "\r\n" + //
				"Host: www.w3.org" + "\r\n" + //
				"\r\n";

		Entry entry = readRequest(request);

		assertEquals("GET", entry.getString(ATTR_HTTP_METHOD));
		assertEquals("http://www.w3.org/pub/WWW/The / Project.html", entry.getString(ATTR_HTTP_BASE));
		assertEquals("http://www.w3.org/pub/WWW/The / Project.html", entry.getString(ATTR_HTTP_URL));
		assertEquals("www.w3.org", entry.getString(ATTR_HTTP_HOST));
		assertNull(entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_read_request_with_header_with_empty_value() throws Exception {

		final String request = "" + // 
				"GET /test HTTP/1.1" + "\r\n" + //
				"Host: www.example.org" + "\r\n" + //
				"TestHeader1:" + "\r\n" + //
				"TestHeader2:\t   \t\t " + "\r\n" + //
				"\r\n";

		Entry entry = readRequest(request);

		assertEquals("", entry.getString("http.TestHeader1"));
		assertEquals("", entry.getString("http.TestHeader2"));
		assertEquals("GET", entry.getString(ATTR_HTTP_METHOD));
		assertEquals("/test", entry.getString(ATTR_HTTP_BASE));
		assertEquals("/test", entry.getString(ATTR_HTTP_URL));
		assertEquals("www.example.org", entry.getString(ATTR_HTTP_HOST));
		assertNull(entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_read_request_with_query_string() throws Exception {

		final String request = "" + // 
				"GET /people?sn=smith&age=51 HTTP/1.1" + "\r\n" + //
				"Host: www.example.com" + "\r\n" + //
				"\r\n";

		Entry entry = readRequest(request);

		assertEquals("/people", entry.getString(ATTR_HTTP_BASE));
		assertEquals("/people?sn=smith&age=51", entry.getString(ATTR_HTTP_URL));

		assertEquals("smith", entry.getString("http.qs.sn"));
		assertEquals("51", entry.getString("http.qs.age"));

		// there must be no other query string parameters
		int http_qs_count = 0;
		for (String attrName : entry.getAttributeCollection()) {
			if (attrName.startsWith("http.qs.")) {
				++http_qs_count;
			}
		}
		assertEquals(2, http_qs_count);
	}

	@Test
	public void test_read_response_with_no_body() throws Exception {

		/*
		 * This status code and the reason phrase are not standard. They are
		 * completely imaginary.
		 */
		final String response = "" + // 
				"HTTP/1.1 250 Test Response" + "\r\n" + //
				"\r\n";

		Entry entry = readResponse(response);

		assertEquals("250", entry.getString(ATTR_HTTP_RESPONSE_CODE));
		assertEquals("Test Response", entry.getString(ATTR_HTTP_RESPONSE_MSG));
		assertNull(entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_read_header_value_with_leadng_whitespace() throws Exception {

		final String request = "" + // 
				"GET /somepath HTTP/1.1" + "\r\n" + //
				"Host: www.example.com" + "\r\n" + //
				"TestHeader: \t\ttestvalue" + "\r\n" + //
				"\r\n";

		Entry entry = readRequest(request);

		assertEquals("testvalue", entry.getString("http.TestHeader"));

		assertEquals("GET", entry.getString(ATTR_HTTP_METHOD));
		assertEquals("/somepath", entry.getString(ATTR_HTTP_BASE));
		assertEquals("/somepath", entry.getString(ATTR_HTTP_URL));
		assertEquals("www.example.com", entry.getString(ATTR_HTTP_HOST));
		assertNull(entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_default_is_text_if_missing_content_type_header_when_reading_response() throws Exception {

		final String body = "testbody";
		final String response = "" + // 
				"HTTP/1.1 201 Created" + "\r\n" + //
				"Content-Length: " + body.length() + "\r\n" + //
				"\r\n" + //
				body;

		Entry entry = readResponse(response);

		assertEquals("201", entry.getString(ATTR_HTTP_RESPONSE_CODE));
		assertEquals("Created", entry.getString(ATTR_HTTP_RESPONSE_MSG));
		assertEquals("" + body.length(), entry.getString(ATTR_HTTP_CONTENT_LENGTH));
		assertEquals(body, entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_default_is_text_if_missing_content_type_header_when_reading_request() throws Exception {

		final String body = "testbody";
		final String request = "" + // 
				"POST /encrypted-area HTTP/1.1" + "\r\n" + //
				"Host: www.example.com" + "\r\n" + //
				"Content-Length: " + body.length() + "\r\n" + //
				"\r\n" + //
				body;

		Entry entry = readRequest(request);

		assertEquals("POST", entry.getString(ATTR_HTTP_METHOD));
		assertEquals("/encrypted-area", entry.getString(ATTR_HTTP_BASE));
		assertEquals("/encrypted-area", entry.getString(ATTR_HTTP_URL));
		assertEquals("www.example.com", entry.getString(ATTR_HTTP_HOST));
		assertEquals("" + body.length(), entry.getString(ATTR_HTTP_CONTENT_LENGTH));
		assertEquals(body, entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_characterset_parser_parameter_default_is_used_if_content_type_header_does_not_specify_charset_when_reading()
			throws Exception {
		/*
		 * Do not configure "characterSet" parameter to test the default value
		 * (which is supposed to be iso-8859-1).
		 */
		final String body = "some ascii data \r\n \r \r \n\n with new all kinds of whitespace\t\t";
		final String expectedCharacterSet = "iso-8859-1";
		final String parserParamCharacterSet = null;
		final String contentTypeCharacterSet = null;

		test_read_message_with_text_body(body, expectedCharacterSet, parserParamCharacterSet, contentTypeCharacterSet);
	}

	@Test
	public void test_characterset_parser_parameter_as_utf8_is_used_if_content_type_header_does_not_specify_charset_when_reading()
			throws Exception {
		/*
		 * Use some Chinese characters. The default encoding (iso-8859-1) won't
		 * handle them, so we would really see whether the "characterSet"
		 * parameter has any effect.
		 */
		final String body = "\u4E0A\u6D77";
		final String expectedCharacterSet = "UTF-8";
		final String parserParamCharacterSet = expectedCharacterSet;
		final String contentTypeCharacterSet = null;

		test_read_message_with_text_body(body, expectedCharacterSet, parserParamCharacterSet, contentTypeCharacterSet);
	}

	@Test
	public void test_characterset_parser_parameter_as_ebcdic_is_used_if_content_type_header_does_not_specify_charset_when_reading()
			throws Exception {
		/*
		 * EBCDIC (http://en.wikipedia.org/wiki/Ebcdic) is default encoding on
		 * z/OS, so make sure it works. It is an 8-bit character encoding so no
		 * fancy characters.
		 */
		final String body = "some ebcdic text for mainframes";
		final String expectedCharacterSet = "IBM-1047";
		final String parserParamCharacterSet = expectedCharacterSet;
		final String contentTypeCharacterSet = null;

		test_read_message_with_text_body(body, expectedCharacterSet, parserParamCharacterSet, contentTypeCharacterSet);
	}

	@Test
	public void test_characterset_parser_parameter_is_overridden_by_content_type_header_charset_when_reading() throws Exception {
		final String body = "\u4E0A\u6D77";
		final String expectedCharacterSet = "UTF-8";
		final String parserParamCharacterSet = "IBM-1047";
		final String contentTypeCharacterSet = "text/html; charset=" + expectedCharacterSet;

		test_read_message_with_text_body(body, expectedCharacterSet, parserParamCharacterSet, contentTypeCharacterSet);
	}

	@Test
	public void test_characterset_parser_parameter_default_is_overridden_by_content_type_header_charset_when_reading()
			throws Exception {
		final String body = "\u4E0A\u6D77";
		final String expectedCharacterSet = "UTF-8";
		final String parserParamCharacterSet = null; // leave the default
		final String contentTypeCharacterSet = "text/html; charset=" + expectedCharacterSet;

		test_read_message_with_text_body(body, expectedCharacterSet, parserParamCharacterSet, contentTypeCharacterSet);
	}

	@Test
	public void test_text_plain_content_type_returns_string_http_body_when_reading() throws Exception {
		final String contentTypeHeaderValue = "text/plain";
		test_read_message_with_text_body(contentTypeHeaderValue);
	}

	@Test
	public void test_text_html_content_type_returns_string_http_body_when_reading() throws Exception {
		final String contentTypeHeaderValue = "text/html";
		test_read_message_with_text_body(contentTypeHeaderValue);
	}

	@Test
	public void test_text_xml_content_type_returns_string_http_body_when_reading() throws Exception {
		final String contentTypeHeaderValue = "text/xml";
		test_read_message_with_text_body(contentTypeHeaderValue);
	}

	@Test
	public void test_application_form_urlencoded_content_type_returns_url_decoded_http_body_when_reading() throws Exception {

		final String bodyEncoded = "some%20data";
		final String bodyDecoded = "some data";

		final String response = "" + // 
				"HTTP/1.1 200 OK" + "\r\n" + //
				"Host: 192.168.11.8" + "\r\n" + //
				"Content-Type: application/x-www-form-urlencoded" + "\r\n" + //
				"Content-Length: " + bodyEncoded.length() + "\r\n" + //
				"\r\n" + //
				bodyEncoded;

		Entry entry = readResponse(response);

		assertEquals("" + bodyEncoded.length(), entry.getString(ATTR_HTTP_CONTENT_LENGTH));
		assertTrue(entry.getObject(ATTR_HTTP_BODY) instanceof java.lang.String);
		assertEquals(bodyDecoded, entry.getObject(ATTR_HTTP_BODY).toString());
	}

	@Test
	public void test_application_soap_xml_content_type_returns_string_http_body_when_reading() throws Exception {
		final String contentTypeHeaderValue = "application/soap+xml";
		test_read_message_with_text_body(contentTypeHeaderValue);
	}

	@Test
	public void test_image_gif_content_type_returns_binary_http_body_when_reading() throws Exception {
		final String contentTypeHeaderValue = "image/gif";
		test_read_message_with_binary_body(contentTypeHeaderValue);
	}

	@Test
	public void test_image_png_content_type_returns_binary_http_body_when_reading() throws Exception {
		final String contentTypeHeaderValue = "image/png";
		test_read_message_with_binary_body(contentTypeHeaderValue);
	}

	@Test
	public void test_image_jpeg_content_type_returns_binary_http_body_when_reading() throws Exception {
		final String contentTypeHeaderValue = "image/jpeg";
		test_read_message_with_binary_body(contentTypeHeaderValue);
	}

	@Test
	public void test_application_octet_stream_content_type_returns_binary_http_body_when_reading() throws Exception {
		final String contentTypeHeaderValue = "application/octet-stream";
		test_read_message_with_binary_body(contentTypeHeaderValue);
	}

	private void test_read_message_with_text_body(String contentTypeHeaderValue) throws Exception {

		final String body = "somedata";
		final String expectedCharacterSet = "iso-8859-1";
		final String parserParamCharacterSet = null;

		test_read_message_with_text_body(body, expectedCharacterSet, parserParamCharacterSet, contentTypeHeaderValue);
	}

	private void test_read_message_with_text_body(String body, String expectedCharacterSet, String parserParamCharacterSet,
			String contentTypeHeaderValue) throws Exception {
		final String expectedBody = body;
		test_read_message_with_text_body(body, expectedBody, expectedCharacterSet, parserParamCharacterSet, contentTypeHeaderValue);
	}

	private void test_read_message_with_text_body(String body, String expectedBody, String expectedCharacterSet,
			String parserParamCharacterSet, String contentTypeHeaderValue) throws Exception {

		boolean clientMode;

		// request
		clientMode = false;
		test_read_message_with_text_body(clientMode, body, expectedBody, expectedCharacterSet, parserParamCharacterSet,
				contentTypeHeaderValue);

		// response
		clientMode = true;
		test_read_message_with_text_body(clientMode, body, body, expectedCharacterSet, parserParamCharacterSet,
				contentTypeHeaderValue);
	}

	private void test_read_message_with_text_body(boolean clientMode, String body, String expectedBody,
			String expectedCharacterSet, String parserParamCharacterSet, String contentTypeHeaderValue) throws Exception {

		final String requestHeader = "" + // 
				"POST /somepath HTTP/1.1" + "\r\n" + //
				"Host: 192.168.11.8" + "\r\n";

		final String responseHeader = "HTTP/1.1 200 OK" + "\r\n";

		String messageHeader = clientMode ? requestHeader : responseHeader;

		final byte[] bodyBytes = body.getBytes(expectedCharacterSet);

		// add a test header to check if it will be preserved
		messageHeader += "TestHeader:   testvalue" + "\r\n";

		if (contentTypeHeaderValue != null) {
			messageHeader += "Content-Type: " + contentTypeHeaderValue + "\r\n";
		}

		messageHeader += "Content-Length: " + bodyBytes.length + "\r\n";

		// end of header
		messageHeader += "\r\n";

		byte[] requestBytes = getMessage(messageHeader, bodyBytes);

		Entry entry = readEntry(requestBytes, clientMode, parserParamCharacterSet);

		assertEquals("testvalue", entry.getString("http.TestHeader"));
		assertEquals("" + bodyBytes.length, entry.getString(ATTR_HTTP_CONTENT_LENGTH));
		assertTrue(entry.getObject(ATTR_HTTP_BODY) instanceof java.lang.StringBuffer);
		assertEquals(expectedBody, entry.getObject(ATTR_HTTP_BODY).toString());
	}

	private void test_read_message_with_binary_body(String contentTypeHeaderValue) throws Exception {

		final byte[] bodyBytes = { 0, -1, -10, 118, 0 };

		String responseHeader = "" + // 
				"HTTP/1.1 200 OK" + "\r\n" + //
				"Content-Type: " + contentTypeHeaderValue + "\r\n" + //
				"Content-Length: " + bodyBytes.length + "\r\n" + //
				"\r\n";

		byte[] responseBytes = getMessage(responseHeader, bodyBytes);

		Entry entry = readResponse(responseBytes, null);

		assertEquals("" + bodyBytes.length, entry.getString(ATTR_HTTP_CONTENT_LENGTH));
		assertTrue(entry.getObject(ATTR_HTTP_BODY) instanceof byte[]);
		byte[] actualBodyBytes = (byte[]) entry.getObject(ATTR_HTTP_BODY);
		assertTrue(java.util.Arrays.equals(bodyBytes, actualBodyBytes));

	}

	@Test
	public void test_Write_Entry_Overwritten_Host_Without_Proxy() throws Exception {
		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.host", "localhost:8090");

		String expected = "" + // 
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: localhost:8090" + NEW_LINE + //
				"Content-Length: 0" + NEW_LINE + //
				NEW_LINE;

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Entry_Overwritten_Host_With_Proxy() throws Exception {
		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.host", "localhost:8090");
		e.setProperty("http.proxy", "proxy");

		String expected = "" + // 
				"GET http://www.example.com:8080/Test HTTP/1.1" + NEW_LINE + //
				"Host: localhost:8090" + NEW_LINE + //
				"Content-Length: 0" + NEW_LINE + //
				NEW_LINE;

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Read_Request_With_Folded_Header() throws Exception {
		final String request = "" + // 
				"GET /test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.org" + NEW_LINE + //
				"TestHeader1: testvalue1\n \ttestvalue2" + NEW_LINE + //
				"TestHeader2:\n\ttestvalue3\n\ttestvalue4\n testvalue5" + NEW_LINE + //
				NEW_LINE;

		Entry entry = readRequest(request);

		assertEquals("testvalue1 testvalue2", entry.getString("http.TestHeader1"));
		assertEquals("testvalue3 testvalue4 testvalue5", entry.getString("http.TestHeader2"));
	}

	@Test
	public void test_Read_Request_With_Folded_Header_Leadng_Whitespace() throws Exception {
		final String request = "" + // 
				"GET /somepath HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com" + NEW_LINE + //
				"TestHeader: \t\n \ttestvalue" + NEW_LINE + //
				NEW_LINE;

		Entry entry = readRequest(request);
		assertEquals("testvalue", entry.getString("http.TestHeader"));
	}

	@Test
	public void test_Read_Request_With_Header_With_Multiple_Message_Values() throws Exception {
		final String request = "" + // 
				"GET /test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.org" + NEW_LINE + //
				"testvalue0" + NEW_LINE + //
				"TestHeader: testvalue1" + NEW_LINE + //
				"TestHeader: testvalue2\n\ttestvalue3" + NEW_LINE + //
				"OtherTestHeader: testvalue6" + NEW_LINE + //
				"TestHeader: testvalue4 \n testvalue5" + NEW_LINE + //
				NEW_LINE;

		Entry entry = readRequest(request);

		assertEquals("www.example.org", entry.getString("http.Host"));
		assertEquals("testvalue6", entry.getString("http.OtherTestHeader"));
		assertEquals("testvalue1, testvalue2 testvalue3, testvalue4 testvalue5", entry.getString("http.TestHeader"));
	}

	@Test
	public void test_Read_Request_With_Chinese_Body_From_String() throws Exception {
		final String body = new String("a\u4E80\u4E81");

		final String request = "" + // 
				"GET /test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.org" + NEW_LINE + //
				"Content-Length: " + body.getBytes("UTF-8").length + NEW_LINE + //
				"Content-Type: text/plain; charset=UTF-8" + NEW_LINE + //
				NEW_LINE + //
				body;

		Entry entry = readEntry(request, false, null);

		assertEquals(String.valueOf(body.getBytes("UTF-8").length), entry.getString(ATTR_HTTP_CONTENT_LENGTH));
		assertNotNull(entry.getString(ATTR_HTTP_BODY));
		assertEquals(body, entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_Read_Request_With_Chinese_Body_From_Stream() throws Exception {
		final String body = new String("a\u4E80\u4E81");

		final String request = "" + // 
				"GET /test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.org" + NEW_LINE + //
				"Content-Length: " + body.getBytes("UTF-8").length + NEW_LINE + //
				"Content-Type: text/plain; charset=UTF-8" + NEW_LINE + //
				NEW_LINE + //
				body;

		Entry entry = readEntry(request.getBytes("UTF-8"), false, null);

		assertEquals(String.valueOf(body.getBytes("UTF-8").length), entry.getString(ATTR_HTTP_CONTENT_LENGTH));
		assertNotNull(entry.getString(ATTR_HTTP_BODY));
		assertEquals(body, entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_Read_Request_With_Chinese_Body_From_Reader() throws Exception {
		final String body = new String("a\u4E33\u4e34");
		
		final String request = "" + // 
		"GET /test HTTP/1.1" + NEW_LINE + //
		"Host: www.example.org" + NEW_LINE + //
		"Content-Length: " + body.getBytes("UTF-8").length + NEW_LINE + //
		"Content-Type: text/plain; charset=UTF-8" + NEW_LINE + //
		NEW_LINE + //
		body;

		BufferedReader in = new BufferedReader(new StringReader(request));

		Entry entry = readEntry(in, false, null);

		assertEquals(String.valueOf(body.getBytes("UTF-8").length), entry.getString(ATTR_HTTP_CONTENT_LENGTH));
		assertNotNull(entry.getString(ATTR_HTTP_BODY));
		assertEquals(body, entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_Read_Request_Multiple_Messages_From_The_Same_Stream() throws Exception {
		HTTPParser parser = new HTTPParser();
		parser.setParam(PARAM_CLIENT_MODE, String.valueOf(false));
		parser.setParam(PARAM_CHARACTER_SET, null);

		final String body = "Some text";
		final String body2 = "Some new text";

		final String request = "" + // 
				"GET /test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.org" + NEW_LINE + //
				"Content-Length: " + body.length() + NEW_LINE + //
				NEW_LINE + //
				body + // 
				"GET /test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.org" + NEW_LINE + //
				"Content-Length: " + body2.length() + NEW_LINE + //
				NEW_LINE + //
				body2;

		ByteArrayInputStream baisRequest = new ByteArrayInputStream(request.getBytes());

		parser.setInputStream(baisRequest);
		parser.initParser();

		Entry entry = parser.readEntry();
		assertEquals(body.length(), Integer.parseInt(entry.getString(ATTR_HTTP_CONTENT_LENGTH)));
		assertNotNull(entry.getString(ATTR_HTTP_BODY));
		assertEquals(body, entry.getString(ATTR_HTTP_BODY));

		entry = parser.readEntry();
		assertEquals(body2.length(), Integer.parseInt(entry.getString(ATTR_HTTP_CONTENT_LENGTH)));
		assertNotNull(entry.getString(ATTR_HTTP_BODY));
		assertEquals(body2, entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_Read_Request_Multiple_Messages_From_The_Same_Reader() throws Exception {
		HTTPParser parser = new HTTPParser();
		parser.setParam(PARAM_CLIENT_MODE, "" + false);
		parser.setParam(PARAM_CHARACTER_SET, null);

		final String body = new String("Some text");
		final String body2 = new String("Some new text");

		final String request = "" + // 
				"GET /test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.org" + NEW_LINE + //
				"Content-Length: " + body.length() + NEW_LINE + //
				NEW_LINE + //
				body + // 
				"GET /test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.org" + NEW_LINE + //
				"Content-Length: " + body2.length() + NEW_LINE + //
				NEW_LINE + //
				body2;

		StringReader sReader = new StringReader(request);

		parser.setInputStream(sReader);
		parser.initParser();

		Entry entry = parser.readEntry();
		assertEquals(body.length(), Integer.parseInt(entry.getString(ATTR_HTTP_CONTENT_LENGTH)));
		assertNotNull(entry.getString(ATTR_HTTP_BODY));
		assertEquals(body, entry.getString(ATTR_HTTP_BODY));

		entry = parser.readEntry();
		assertEquals(body2.length(), Integer.parseInt(entry.getString(ATTR_HTTP_CONTENT_LENGTH)));
		assertNotNull(entry.getString(ATTR_HTTP_BODY));
		assertEquals(body2, entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_Write_Multiple_Entries_In_The_Same_Writer() throws Exception {
		StringWriter sw = new StringWriter();
		HTTPParser parser = new HTTPParser();
		parser.setOutputStream(sw);
		parser.setClientMode(true);

		Entry e = new Entry();
		String body1 = "Some text";
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", body1);

		parser.writeEntry(e);

		String expected = "" + // 
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: " + body1.length() + NEW_LINE + //
				NEW_LINE + //
				body1;
		String result = sw.toString();
		assertEquals(expected, result);

		String body2 = "Some new text";
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", body2);
		parser.writeEntry(e);

		expected = expected + // 
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: " + body2.length() + NEW_LINE + //
				NEW_LINE + //
				body2;
		result = sw.toString();
		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Multiple_Entries_In_The_Same_Output_Stream() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		HTTPParser parser = new HTTPParser();
		parser.setOutputStream(baos);
		parser.setClientMode(true);

		Entry e = new Entry();
		String body1 = "Some text";
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", body1);

		parser.writeEntry(e);

		String expected = "" + // 
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: " + body1.length() + NEW_LINE + //
				NEW_LINE + //
				body1;
		String result = baos.toString();
		assertEquals(expected, result);

		String body2 = "Some new text";
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", body2);
		parser.writeEntry(e);

		expected = expected + // 
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: " + body2.length() + NEW_LINE + //
				NEW_LINE + //
				body2;
		result = baos.toString();
		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Entry_Using_OutputStream_And_HttpBody_Is_A_File_Object() throws Exception {
		// Note: this test fails if using a Writer for the output.

		String inBodyFileName = "./resources/HTTP_Input/testbody.txt";
		File inBodyFile = new File(inBodyFileName);
		new FileInputStream(inBodyFile);

		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.host", "localhost:8090");
		e.setAttribute("http.body", inBodyFile);

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: localhost:8090" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, true, true);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Entry_Using_Writer() throws Exception {
		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", "abcdefg");

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Entry_Using_OutputStream() throws Exception {
		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", "abcdefg");

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, true, true);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Request_Entry_With_No_Body() throws Exception {
		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Length: 0" + NEW_LINE + //
				NEW_LINE;

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Request_Entry_With_Charset_Latin1() throws Exception {
		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", "abcdefg");

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Request_Entry_With_Charset_From_Configuration() throws Exception {
		StringWriter sw = new StringWriter();
		HTTPParser parser = new HTTPParser();
		parser.setOutputStream(sw);
		parser.setClientMode(true);
		parser.setParam("characterSet", "Windows-1251");

		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", "abcdefg");

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=Windows-1251" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		parser.writeEntry(e);

		String result = sw.toString();

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Request_Entry_With_Method_POST() throws Exception {
		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", "abcdefg");
		e.setAttribute("http.method", "POST");

		String expected = "" + //
				"POST /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Request_Entry_With_Proxy() throws Exception {
		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", "abcdefg");

		e.setProperty("http.proxy", "proxy");

		String expected = "" + //
				"GET http://www.example.com:8080/Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Overwritten_Host_Without_Proxy() throws Exception {
		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.host", "www.example.com");
		e.setAttribute("http.body", "abcdefg");

		String expected = "" + // 
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Use_Server_Mode_NoBody() throws Exception {
		Entry e = new Entry();

		String expected = "" + //
				"HTTP/1.1 200 OK" + NEW_LINE + //
				"Content-Length: 0" + NEW_LINE + //
				NEW_LINE;

		String result = writeEntry(e, false, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Use_Server_Mode_With_Body() throws Exception {
		Entry e = new Entry();
		e.setAttribute("http.body", "abcdefg");

		String expected = "" + //
				"HTTP/1.1 200 OK" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, false, false);

		assertEquals(expected, result);
	}

	/**
	 * CMVC Defect 13428
	 */
	@Test
	public void test_Write_Use_Server_Mode_Status_Not_Found() throws Exception {
		Entry e = new Entry();

		e.setAttribute("http.status", "NOT FOUND");

		String expected = "" + //
				"HTTP/1.1 404 File Not Found" + NEW_LINE + //
				"Content-Length: 0" + NEW_LINE + //
				NEW_LINE;

		String result = writeEntry(e, false, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Use_Server_Mode_Status_Forbidden_Default_Realm() throws Exception {
		Entry e = new Entry();

		e.setAttribute("http.status", "FORBIDDEN");

		String expected = "" + //
				"HTTP/1.1 401 Forbidden" + NEW_LINE + //
				"WWW-Authenticate: Basic realm=\"IBM-Directory-Integrator\"" + NEW_LINE + //
				"Content-Length: 0" + NEW_LINE + //
				NEW_LINE;

		String result = writeEntry(e, false, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Use_Server_Mode_Status_Forbidden_With_Realm() throws Exception {
		Entry e = new Entry();

		e.setAttribute("http.status", "FORBIDDEN");
		e.setAttribute("http.auth-realm", "Some realm");

		String expected = "" + //
				"HTTP/1.1 401 Forbidden" + NEW_LINE + //
				"WWW-Authenticate: Basic realm=\"Some realm\"" + NEW_LINE + //
				"Content-Length: 0" + NEW_LINE + //
				NEW_LINE;

		String result = writeEntry(e, false, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Entry_With_Test_Header() throws Exception {
		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.test_header", "test_value");
		e.setAttribute("http.body", "abcdefg");

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"test_header: test_value" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Entry_With_Content_Type() throws Exception {
		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", "abcdefg");
		e.setAttribute("http.Content-Type", "text/*; charset=iso-8859-1");

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/*; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Entry_Which_Body_Is_String() throws Exception {
		String body = "abcdefg";
		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", body);

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Entry_Which_Body_Is_InputStream() throws Exception {
		ByteArrayInputStream body = new ByteArrayInputStream("abcdefg".getBytes("iso-8859-1"));

		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", body);

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Entry_With_Body_Is_Reader() throws Exception {
		StringReader body = new StringReader("abcdefg");

		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", body);

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Entry_With_Body_Is_Entry() throws Exception {
		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		Entry body = new Entry();
		body.setAttribute("Test atribut1", "Test value1");
		body.setAttribute("Test atribut2", "Test value2");
		e.setAttribute("http.body", body);

		String stringEntry = new com.ibm.di.util.XMLUtils().entry2XML(body);

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: " + stringEntry.getBytes("iso-8859-1").length + NEW_LINE + //
				NEW_LINE + //
				stringEntry;

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Entry_With_Body_Is_ByteArray_Use_OutPutStream_() throws Exception {
		String stringBody = "abcdefg";
		byte[] body = stringBody.getBytes("iso-8859-1");

		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", body);

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: 7" + NEW_LINE + //
				NEW_LINE + //
				"abcdefg";

		String result = writeEntry(e, true, true);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Entry_With_Body_Is_Generic_Object() throws Exception {
		String stringBody = "abcdefg";
		List<Character> body = new ArrayList<Character>();
		for (int i = 0; i < stringBody.length(); i++) {
			body.add(stringBody.charAt(i));
		}

		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", body);

		int contentLenght = body.toString().getBytes("iso-8859-1").length;

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: text/plain; charset=iso-8859-1" + NEW_LINE + //
				"Content-Length: " + contentLenght + NEW_LINE + //
				NEW_LINE + //
				body.toString();

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Read_Response_With_Chunked_Body_Without_FootHeader() throws Exception {

		final String response = "" + // 	
				"HTTP/1.1 200 OK" + "\r\n" + //
				"Date: Fri, 31 Dec 1999 23:59:59 GMT" + "\r\n" + //
				"Content-Type: text/plain" + "\r\n" + //
				"Transfer-Encoding: chunked" + "\r\n" + //
				"\r\n" + //
				"1a; ignore-stuff-here" + "\r\n" + //
				"abcdefghijklmnopqrstuvwxyz" + "\r\n" + //
				"10" + "\r\n" + //
				"1234567890abcdef" + "\r\n" + //
				"0" + "\r\n" + //
				"\r\n";

		Entry entry = readResponse(response.getBytes(), null);

		assertEquals("200", entry.getString(ATTR_HTTP_RESPONSE_CODE));
		assertEquals("OK", entry.getString(ATTR_HTTP_RESPONSE_MSG));
		assertEquals("text/plain", entry.getString("http.Content-Type"));
		assertEquals("chunked", entry.getString("http.Transfer-Encoding"));
		assertEquals("Fri, 31 Dec 1999 23:59:59 GMT", entry.getString("http.Date"));
		assertNotNull(entry.getString(ATTR_HTTP_BODY));
		assertEquals("abcdefghijklmnopqrstuvwxyz1234567890abcdef", entry.getString(ATTR_HTTP_BODY));
	}

	@Test
	public void test_Read_Response_With_Chunked_Body_With_FootHeader() throws Exception {

		final String response = "" + // 	
				"HTTP/1.1 200 OK" + "\r\n" + //
				"Date: Fri, 31 Dec 1999 23:59:59 GMT" + "\r\n" + //
				"Content-Type: text/plain" + "\r\n" + //
				"Transfer-Encoding: chunked" + "\r\n" + //
				"\r\n" + //
				"1a; ignore-stuff-here" + "\r\n" + //
				"abcdefghijklmnopqrstuvwxyz" + "\r\n" + //
				"10" + "\r\n" + //
				"1234567890abcdef" + "\r\n" + //
				"0" + "\r\n" + //
				"some-footer: some-value" + "\r\n" + //
				"another-footer: another-value" + "\r\n" + //
				"\r\n";

		Entry entry = readResponse(response.getBytes(), null);

		assertEquals("200", entry.getString(ATTR_HTTP_RESPONSE_CODE));
		assertEquals("OK", entry.getString(ATTR_HTTP_RESPONSE_MSG));
		assertEquals("text/plain", entry.getString("http.Content-Type"));
		assertEquals("chunked", entry.getString("http.Transfer-Encoding"));
		assertEquals("Fri, 31 Dec 1999 23:59:59 GMT", entry.getString("http.Date"));
		assertNotNull(entry.getString(ATTR_HTTP_BODY));
		assertEquals("abcdefghijklmnopqrstuvwxyz1234567890abcdef", entry.getString(ATTR_HTTP_BODY));

		assertNotNull(entry.getString("http.some-footer"));
		assertEquals("some-value", entry.getString("http.some-footer"));
		assertNotNull(entry.getString("http.another-footer"));
		assertEquals("another-value", entry.getString("http.another-footer"));
	}

	@Test
	public void test_Read_Request_With_Header_As_Properties_With_Multiple_Message_Value() throws Exception {

		final String request = "" + // 
				"GET /test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.org" + NEW_LINE + //
				"TestHeader: testvalue1" + NEW_LINE + //
				"TestHeader: testvalue2" + NEW_LINE + //
				NEW_LINE;
		ByteArrayInputStream requestStream = new ByteArrayInputStream(request.getBytes());

		HTTPParser parser = new HTTPParser();
		parser.setParam(PARAM_CHARACTER_SET, "iso-8859-1");
		parser.setInputStream(requestStream);
		parser.initParser();
		parser.setClientMode(false);
		parser.setUseProperties(true);

		Entry entry = parser.readEntry();

		assertEquals("www.example.org", entry.getProperty("http.Host"));
		assertEquals("testvalue1, testvalue2", entry.getProperty("http.TestHeader"));
	}

	@Test
	public void test_Read_Request_With_Header_As_Properties() throws Exception {

		final String request = "" + // 
				"GET /test HTTP/1.1" + NEW_LINE + //
				" testvalue0" + NEW_LINE + //
				"Host: www.example.org" + NEW_LINE + //
				"TestHeader1: testvalue1\n \ttestvalue2" + NEW_LINE + //
				"TestHeader2:\n\ttestvalue3\n\ttestvalue4\n testvalue5" + NEW_LINE + //
				NEW_LINE;
		ByteArrayInputStream requestStream = new ByteArrayInputStream(request.getBytes());

		HTTPParser parser = new HTTPParser();
		parser.setParam(PARAM_CHARACTER_SET, "iso-8859-1");
		parser.setInputStream(requestStream);
		parser.initParser();
		parser.setClientMode(false);
		parser.setUseProperties(true);

		Entry entry = parser.readEntry();

		assertEquals("testvalue1 testvalue2", entry.getProperty("http.TestHeader1"));
		assertEquals("testvalue3 testvalue4 testvalue5", entry.getProperty("http.TestHeader2"));

	}

	@Test
	public void test_Read_Request_With_Authorization() throws Exception {
		String user = "user";
		String pass = "password";
		String authorization = MimeUtility.encodeText("\u00c6\u00d8\u00c5" + user + ":" + pass, "iso-8859-1", "B");
		authorization = "Basic " + authorization.substring(authorization.indexOf("?B?") + 7, authorization.indexOf("?="));

		final String request = "" + // 
				"GET /test HTTP/1.1" + NEW_LINE + //
				" testvalue0" + NEW_LINE + //
				"Host: www.example.org" + NEW_LINE + //
				"TestHeader1: testvalue1\n \ttestvalue2" + NEW_LINE + //
				"TestHeader2:\n\ttestvalue3\n\ttestvalue4\n testvalue5" + NEW_LINE + //
				"Authorization:" + authorization + //
				NEW_LINE;
		ByteArrayInputStream requestStream = new ByteArrayInputStream(request.getBytes());

		HTTPParser parser = new HTTPParser();
		parser.setParam(PARAM_CHARACTER_SET, "iso-8859-1");
		parser.setInputStream(requestStream);
		parser.initParser();
		parser.setClientMode(false);

		Entry entry = parser.readEntry();

		assertEquals("testvalue1 testvalue2", entry.getString("http.TestHeader1"));
		assertEquals("testvalue3 testvalue4 testvalue5", entry.getString("http.TestHeader2"));
		assertEquals(user, entry.getString("http.remote_user"));
		assertEquals(pass, entry.getString("http.remote_pass"));
	}

	/**
	 * CMVC Defect 6581
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_Write_Use_Server_Mode_Status_Redirect() throws Exception {
		Entry e = new Entry();

		e.setAttribute("http.status", "302 Found");
		e.setAttribute("http.redirect", "example.com");

		String expected = "" + //
				"HTTP/1.1 302 Found" + NEW_LINE + //
				"Location: example.com" + NEW_LINE + //
				"Content-Length: 0" + NEW_LINE + //
				NEW_LINE;

		String result = writeEntry(e, false, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Use_Server_Mode_httpForbidden() throws Exception {
		StringWriter sw = new StringWriter();
		HTTPParser parser = new HTTPParser();
		parser.setOutputStream(sw);
		parser.setClientMode(false);

		parser.httpForbidden();

		String expected = "" + //
				"HTTP/1.1 401 Forbidden" + NEW_LINE + //
				"Content-Length: 0" + NEW_LINE + //
				NEW_LINE;
		String result = sw.toString();
		assertEquals(expected, result);
	}

	@Test
	public void test_Write_Use_Client_Mode_With_Authorization() throws Exception {
		String user = "user";
		String pass = "password";
		String authorization = MimeUtility.encodeText("\u00c6\u00d8\u00c5" + user + ":" + pass, "iso-8859-1", "B");
		authorization = "Basic " + authorization.substring(authorization.indexOf("?B?") + 7, authorization.indexOf("?="));

		Entry e = new Entry();

		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.remote_user", user);
		e.setAttribute("http.remote_pass", pass);

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Authorization: " + authorization + NEW_LINE + //
				"Content-Length: 0" + NEW_LINE + //
				NEW_LINE;

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_setProp_getProp_As_Attribute() throws Exception {

		StringWriter sw = new StringWriter();
		HTTPParser parser = new HTTPParser();
		parser.setOutputStream(sw);
		parser.setClientMode(true);

		Entry e = new Entry();

		parser.setProp(e, "http.url", "http://www.example.com:8080/Test");
		parser.setProp(e, "http.Testheader", "Testvalue");

		assertEquals(parser.getProp(e, "http.url"), "http://www.example.com:8080/Test");
		assertEquals(parser.getProp(e, "http.Testheader"), "Testvalue");
	}

	@Test
	public void test_setProp_getProp_As_Properties() throws Exception {

		StringWriter sw = new StringWriter();
		HTTPParser parser = new HTTPParser();
		parser.setOutputStream(sw);
		parser.setUseProperties(true);
		parser.setClientMode(true);

		Entry e = new Entry();

		parser.setProp(e, "http.url", "http://www.example.com:8080/Test");
		parser.setProp(e, "http.Testheader", "Testvalue");

		assertEquals(parser.getProp(e, "http.url"), "http://www.example.com:8080/Test");
		assertEquals(parser.getProp(e, "http.Testheader"), "Testvalue");
	}

	/**
	 * CMVC Defect 12323
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_Write_Entry_With_Body_Is_ByteArray_Image_Use_OutPutStream() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		HTTPParser parser = new HTTPParser();
		parser.setOutputStream(baos);
		parser.setClientMode(true);

		String stringBody = "Used instead of a GIF file";
		byte[] body = stringBody.getBytes("UTF-8");

		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.content-type", "image/gif");
		e.setAttribute("http.body", body);

		parser.writeEntry(e);

		String expectedHeader = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"content-type: image/gif" + NEW_LINE + //
				"Content-Length: " + body.length + NEW_LINE + //
				NEW_LINE;
		byte[] resultBytes = baos.toByteArray();
		int headerLenght = resultBytes.length - body.length;

		String resultHeaderString = new String(Arrays.copyOfRange(resultBytes, 0, headerLenght), "UTF-8");
		assertEquals(expectedHeader, resultHeaderString);

		byte[] resultBodyBytes = Arrays.copyOfRange(resultBytes, headerLenght, resultBytes.length);
		assertArrayEquals(body, resultBodyBytes);
	}

	/**
	 * CMVC Defect 9749
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_Write_Entry_With_Content_Type_URLEncoded() throws Exception {
		String body = "It%20is%20a%20test20string";

		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", body);
		e.setAttribute("http.Content-Type", "application/x-www-form-urlencoded");

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: application/x-www-form-urlencoded" + NEW_LINE + //
				"Content-Length: " + body.length() + NEW_LINE + //
				NEW_LINE + //
				body;

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	/**
	 * CMVC Defect 9749
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_Write_Entry_With_Content_Type_SOAP_XML() throws Exception {
		String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ NEW_LINE
				+ //
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sim=\"http://www.example.org/simple_wrapped_doc/\">"
				+ NEW_LINE + //
				"	<soapenv:Header/>" + NEW_LINE + //
				"		<soapenv:Body>" + NEW_LINE + //
				"		<sim:getAge>" + NEW_LINE + //
				"			<sim:name>gosho</sim:name>" + NEW_LINE + //
				"		</sim:getAge>" + NEW_LINE + //
				"	</soapenv:Body>" + NEW_LINE + //
				"</soapenv:Envelope>";

		Entry e = new Entry();
		e.setAttribute("http.url", "http://www.example.com:8080/Test");
		e.setAttribute("http.body", body);
		e.setAttribute("http.Content-Type", "application/soap+xml");

		String expected = "" + //
				"GET /Test HTTP/1.1" + NEW_LINE + //
				"Host: www.example.com:8080" + NEW_LINE + //
				"Content-Type: application/soap+xml" + NEW_LINE + //
				"Content-Length: " + body.length() + NEW_LINE + //
				NEW_LINE + //
				body;

		String result = writeEntry(e, true, false);

		assertEquals(expected, result);
	}

	@Test
	public void test_Read_Request_With_Incorrect_Headers() throws Exception {

		// Incorrect headers should be skipped
		final String request = "" + // 
				"GET /test HTTP/1.1" + NEW_LINE + //
				"testvalue0" + NEW_LINE + //
				":testvalue0" + NEW_LINE + //
				"Host: www.example.org" + NEW_LINE + //
				"testvalue0" + NEW_LINE + //
				":testvalue0" + NEW_LINE + //
				"TestHeader: testvalue1" + NEW_LINE + //
				NEW_LINE;

		Entry entry = readRequest(request);

		assertEquals("www.example.org", entry.getString("http.Host"));
		assertEquals("testvalue1", entry.getString("http.TestHeader"));
	}

	private static byte[] getMessage(String messageHeader, byte[] bodyBytes) throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		// headers are always ASCII
		stream.write(messageHeader.getBytes("iso-8859-1"));
		stream.write(bodyBytes);
		byte[] messageBytes = stream.toByteArray();
		return messageBytes;
	}

	private static Entry readResponse(String response) throws Exception {
		final String characterSet = null;
		return readResponse(response.getBytes("iso-8859-1"), characterSet);
	}

	private static Entry readResponse(byte[] responseBytes, String characterSet) throws Exception {
		final boolean clientMode = true;
		return readEntry(responseBytes, clientMode, characterSet);
	}

	private static Entry readRequest(String request) throws Exception {
		final String characterSet = null;
		return readRequest(request.getBytes("iso-8859-1"), characterSet);
	}

	private static Entry readRequest(byte[] requestBytes, String characterSet) throws Exception {
		final boolean clientMode = false;
		return readEntry(requestBytes, clientMode, characterSet);
	}

	private static Entry readEntry(byte[] requestBytes, boolean clientMode, String characterSet) throws Exception {

		ByteArrayInputStream requestStream = new ByteArrayInputStream(requestBytes);

		HTTPParser parser = new HTTPParser();
		parser.setParam(PARAM_CLIENT_MODE, "" + clientMode);
		if (characterSet != null) {
			parser.setParam(PARAM_CHARACTER_SET, characterSet);
		}
		parser.setInputStream(requestStream);
		parser.initParser();
		return parser.readEntry();
	}

	private static Entry readEntry(String request, boolean clientMode, String characterSet) throws Exception {

		HTTPParser parser = new HTTPParser();
		parser.setParam(PARAM_CLIENT_MODE, "" + clientMode);
		if (characterSet != null) {
			parser.setParam(PARAM_CHARACTER_SET, characterSet);
		}
		parser.setInputStream(request);
		parser.initParser();

		return parser.readEntry();
	}

	private static Entry readEntry(Reader request, boolean clientMode, String characterSet) throws Exception {

		HTTPParser parser = new HTTPParser();
		parser.setParam(PARAM_CLIENT_MODE, "" + clientMode);
		if (characterSet != null) {
			parser.setParam(PARAM_CHARACTER_SET, characterSet);
		}
		parser.setInputStream(request);
		parser.initParser();

		return parser.readEntry();
	}

	private static String writeEntry(Entry e, boolean clientMode, boolean useOutputStream) throws Exception {
		HTTPParser parser = new HTTPParser();
		ByteArrayOutputStream baos = null;
		StringWriter sw = null;
		if (useOutputStream) {
			baos = new ByteArrayOutputStream();
			parser.setOutputStream(baos);
		} else {
			sw = new StringWriter();
			parser.setOutputStream(sw);
		}
		
		parser.setClientMode(clientMode);

		parser.writeEntry(e);

		String result = null;
		if (useOutputStream) {
			result = baos.toString();
		} else {
			result = sw.toString();
		}
		return result;
	}
}
