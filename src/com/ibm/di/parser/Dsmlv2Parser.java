/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.xerces.impl.dv.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;
import com.ibm.icu.util.StringTokenizer;
import com.ibm.ldap.dsml.AbandonRequest;
import com.ibm.ldap.dsml.AddRequest;
import com.ibm.ldap.dsml.AttributeDescriptions;
import com.ibm.ldap.dsml.AttributeValueAssertion;
import com.ibm.ldap.dsml.AuthRequest;
import com.ibm.ldap.dsml.BatchMessage;
import com.ibm.ldap.dsml.CompareRequest;
import com.ibm.ldap.dsml.DeleteRequest;
import com.ibm.ldap.dsml.DsmlAttr;
import com.ibm.ldap.dsml.DsmlControl;
import com.ibm.ldap.dsml.DsmlMessage;
import com.ibm.ldap.dsml.DsmlModification;
import com.ibm.ldap.dsml.DsmlRequest;
import com.ibm.ldap.dsml.DsmlResponse;
import com.ibm.ldap.dsml.DsmlValue;
import com.ibm.ldap.dsml.ErrorResponse;
import com.ibm.ldap.dsml.ExtendedRequest;
import com.ibm.ldap.dsml.ExtendedResponse;
import com.ibm.ldap.dsml.Filter;
import com.ibm.ldap.dsml.LdapResult;
import com.ibm.ldap.dsml.ModifyDNRequest;
import com.ibm.ldap.dsml.ModifyRequest;
import com.ibm.ldap.dsml.SearchRequest;
import com.ibm.ldap.dsml.SearchResponse;
import com.ibm.ldap.dsml.SearchResultEntry;

/**
 * A TDI Parser class for reading and writing Entries in DSMLv2 format
 * 
 */
public class Dsmlv2Parser extends ParserImpl {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "dsmlv2parser";

	/**
	 * Parameter mode.
	 */
	public static final String PARAMETER_MODE = "mode";

	/**
	 * <code>binaryAttributes</code> config parameter name.
	 */
	public static final String PARAMETER_BINARY_ATTRIBUTES = "binaryAttributes";

	/**
	 * <code>batchRequestId</code> config parameter name.
	 */
	public static final String PARAMETER_BATCH_REQUEST_ID = "batchRequestId";

	/**
	 * <code>resultEntries</code> config parameter name.
	 */
	public static final String ATTR_NAME_ACUMULATOR = "resultEntries";

	/**
	 * Attribute name for distinguish number.
	 */
	public static final String ATTR_NAME_DN = "$dn";

	/**
	 * Attribute name for new RDN.
	 */
	public static final String ATTR_NAME_NEWRDN = "newrdn";

	/**
	 * Name of the DSML base attribute.
	 */
	public static final String ATTR_NAME_DSML_BASE = "dsml.base";

	/**
	 * Name of the DSML operation attribute.
	 */
	public static final String ATTR_NAME_DSML_OPERATION = "dsml.operation";

	/**
	 * Attribute name for compare_name attribute.
	 */
	public static final String ATTR_NAME_DSML_COMPARE_NAME = "dsml.compare_name";

	/**
	 * Attribute name for compare_value attribute.
	 */
	public static final String ATTR_NAME_DSML_COMPARE_VALUE = "dsml.compare_value";

	/**
	 * Attribute name for compare_value attribute.
	 */
	public static final String ATTR_NAME_DSML_COMPARE_RESULT = "dsml.compare_value";

	/**
	 * Attribute name for error attribute.
	 */
	public static final String ATTR_NAME_DSML_ERROR_MESSAGE = "dsml.error";

	/**
	 * Attribute name for resultcode attribute.
	 */
	public static final String ATTR_NAME_DSML_RESULTCODE = "dsml.resultcode";

	/**
	 * Attribute name for resultdescr attribute.
	 */
	public static final String ATTR_NAME_DSML_RESULTDESC = "dsml.resultdescr";

	/**
	 * Attribute name for filter attribute.
	 */
	public static final String ATTR_NAME_DSML_FILTER = "dsml.filter";

	/**
	 * Attribute name for attributes attribute.
	 */
	public static final String ATTR_NAME_DSML_ATTRIBUTES = "dsml.attributes";

	/**
	 * Attribute name for scope attribute.
	 */
	public static final String ATTR_NAME_DSML_SCOPE = "dsml.scope";

	/**
	 * Attribute name for requestID attribute.
	 */
	public static final String ATTR_NAME_DSML_REQUEST_ID = "dsml.requestID";

	/**
	 * Attribute name for controls attribute.
	 */
	public static final String ATTR_NAME_DSML_CONTROLS = "dsml.controls";

	/**
	 * Attribute name for exception attribute.
	 */
	public static final String ATTR_NAME_DSML_EXCEPTION = "dsml.exception";

	/**
	 * Attribute name for newSuperior attribute.
	 */
	public static final String ATTR_NAME_DSML_NEWSUPERIOR = "dsml.newSuperior";

	/**
	 * Attribute name for deleteOldRDN attribute.
	 */
	public static final String ATTR_NAME_DSML_DELETEOLDRDN = "dsml.deleteOldRDN";

	/**
	 * Attribute name for derefAliases attribute.
	 */
	public static final String ATTR_NAME_DSML_DEREFALIASES = "dsml.derefAliases";

	/**
	 * Attribute name for sizeLimit attribute.
	 */
	public static final String ATTR_NAME_DSML_SIZELIMIT = "dsml.sizeLimit";

	/**
	 * Attribute name for timeLimit attribute.
	 */
	public static final String ATTR_NAME_DSML_TIMELIMIT = "dsml.timeLimit";

	/**
	 * Attribute name for typesOnly attribute.
	 */
	public static final String ATTR_NAME_DSML_TYPESONLY = "dsml.typesOnly";

	/**
	 * Attribute name for referral attribute.
	 */
	public static final String ATTR_NAME_DSML_REFFERAL = "dsml.referral";

	/**
	 * Attribute name for principal attribute.
	 */
	public static final String ATTR_NAME_DSML_PRINCIPAL = "dsml.principal";

	/**
	 * Attribute name for responseName attribute.
	 */
	public static final String ATTR_NAME_DSML_RESPONSE_NAME = "dsml.responseName";

	/**
	 * Attribute name for response attribute.
	 */
	public static final String ATTR_NAME_DSML_RESPONSE = "dsml.response";

	/**
	 * Attribute name for errorType attribute.
	 */
	public static final String ATTR_NAME_DSML_ERRORTYPE = "dsml.errorType";

	/**
	 * Attribute name for details attribute.
	 */
	public static final String ATTR_NAME_DSML_DETAILS = "dsml.details";

	/**
	 * Attribute name for message attribute.
	 */
	public static final String ATTR_NAME_DSML_MESSAGE = "dsml.message";

	/**
	 * Attribute name for extended.requestname attribute.
	 */
	public static final String ATTR_NAME_DSML_REQUEST_NAME = "dsml.extended.requestname";

	/**
	 * Attribute name for extended.requestvalue attribute.
	 */
	public static final String ATTR_NAME_DSML_REQUEST_VALUE = "dsml.extended.requestvalue";

	/**
	 * Attribute name for abandonID attribute.
	 */
	public static final String ATTR_NAME_DSML_ABANDON_ID = "dsml.abandonID";

	/**
	 * Name of the add request.
	 */
	public static final String OPERATION_ADD_REQUEST = "addRequest";

	/**
	 * Name of the delete request.
	 */
	public static final String OPERATION_DELETE_REQUEST = "deleteRequest";

	/**
	 * Name of the search request.
	 */
	public static final String OPERATION_SEARCH_REQUEST = "searchRequest";

	/**
	 * Name of the compare request.
	 */
	public static final String OPERATION_COMPARE_REQUEST = "compareRequest";

	/**
	 * Name of the modify request.
	 */
	public static final String OPERATION_MODIFY_REQUEST = "modifyRequest";

	/**
	 * Name of the modify distinguish number request.
	 */
	public static final String OPERATION_MODIFYDN_REQUEST = "modDnRequest";

	/**
	 * Name of the extended request.
	 */
	public static final String OPERATION_EXTENDED_REQUEST = "extendedRequest";

	/**
	 * Name of the abandon request.
	 */
	public static final String OPERATION_ABANDON_REQUEST = "abandonRequest";

	/**
	 * Name of the authorization request.
	 */
	public static final String OPERATION_AUTH_REQUEST = "authRequest";

	/**
	 * Name of the add response.
	 */
	public static final String OPERATION_ADD_RESPONSE = "addResponse";

	/**
	 * Name of the delete response.
	 */
	public static final String OPERATION_DELETE_RESPONSE = "deleteResponse";

	/**
	 * Name of the search response.
	 */
	public static final String OPERATION_SEARCH_RESPONSE = "searchResponse";

	/**
	 * Name of the compare response.
	 */
	public static final String OPERATION_COMPARE_RESPONSE = "compareResponse";

	/**
	 * Name of the modify response.
	 */
	public static final String OPERATION_MODIFY_RESPONSE = "modifyResponse";

	/**
	 * Name of the modify distinguish number response.
	 */
	public static final String OPERATION_MODIFYDN_RESPONSE = "modDnResponse";

	/**
	 * Name of the extended response.
	 */
	public static final String OPERATION_EXTENDED_RESPONSE = "extendedResponse";

	/**
	 * Name of the authorization response.
	 */
	public static final String OPERATION_AUTH_RESPONSE = "authResponse";

	/**
	 * Name of the error response.
	 */
	public static final String OPERATION_ERROR_RESPONSE = "errorResponse";

	/**
	 * Indicates search scope - whole tree.
	 * <p>
	 * Note: From ITDS DSMLv2 library.
	 */
	public static final String WHOLE_SUBTREE = "wholeSubtree";

	/**
	 * Default value used for search request.
	 */
	public static final String DEREF_NEVER = "neverDerefAliases";

	/**
	 * Namespace URI for SOAP.
	 * <p>
	 * For SOAP support.
	 */
	public static final String NS_URI_SOAP_ENV = "http://schemas.xmlsoap.org/soap/envelope/";

	/**
	 * Indicates SOAP envelope.
	 */
	public static final String SOAP_ENV_QN = "SOAP-ENV:Envelope";

	/**
	 * XML namespace URI.
	 */
	public static final String XMLNS_NS = "http://www.w3.org/2000/xmlns/";

	/**
	 * SOAP namespace attribute.
	 */
	public static final String ATTR_SOAP_NS = "xmlns:SOAP-ENV";

	/**
	 * SOAP body attribute.
	 */
	public static final String SOAP_BODY_QN = "SOAP-ENV:Body";

	/**
	 * DSML namespace attribute.
	 */
	public static final String ATTR_DSML_NS = "xmlns:dsml";

	/**
	 * XML declaration attribute.
	 */
	private static final String XML_DECLAR = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";

	/**
	 * Batch response begin attribute.
	 */
	private static final String BATCH_RESPONSE_BEGIN = "<batchResponse";
	
	/**
	 * Default namespaces
	 */
	private static final String BATCH_DEFAULT_NAMESPACES = " xmlns=\"urn:oasis:names:tc:DSML:2:0:core\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
	
	/**
	 * A Tag end.
	 */
	private static final String TAG_END = ">\r\n";
	
	/**
	 * Batch response end attribute.
	 */
	private static final String BATCH_RESPONSE_END = "</batchResponse>";
	/**
	 * Batch request begin attribute.
	 */
	private static final String BATCH_REQUEST_BEGIN = "<batchRequest";
	/**
	 * Batch request end attribute.
	 */
	private static final String BATCH_REQUEST_END = "</batchRequest>";
	/**
	 * SOAP end attribute.
	 */
	private static final String SOAP_END = "</SOAP-ENV:Body></SOAP-ENV:Envelope>\r\n";
	/**
	 * SOAP begin attribute.
	 */
	private static final String SOAP_BEGIN = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
			+ "<SOAP-ENV:Body xmlns:dsml=\"urn:oasis:names:tc:DSML:2:0:core\">";

	// requests
	/**
	 * Authentication request index.
	 */
	private static final int AUTH_REQUEST = 0;

	/**
	 * Search request index.
	 */
	private static final int SEARCH_REQUEST = 1;
	/**
	 * Modify request index.
	 */
	private static final int MODIFY_REQUEST = 2;
	/**
	 * Add request index.
	 */
	private static final int ADD_REQUEST = 3;
	/**
	 * Delete request index.
	 */
	private static final int DEL_REQUEST = 4;
	/**
	 * Modify DN request index.
	 */
	private static final int MOD_DN_REQUEST = 5;
	/**
	 * Compare request index.
	 */
	private static final int COMPARE_REQUEST = 6;
	/**
	 * Abandon request index.
	 */
	private static final int ABANDON_REQUEST = 7;
	/**
	 * Extended request index.
	 */
	private static final int EXTENDED_REQUEST = 8;

	// responses
	/**
	 * Search response index.
	 */
	private static final int SEARCH_RESPONSE = 9;
	/**
	 * Authentication response index.
	 */
	private static final int AUTH_RESPONSE = 10;
	/**
	 * Modify response index.
	 */
	private static final int MODIFY_RESPONSE = 11;
	/**
	 * Add response index.
	 */
	private static final int ADD_RESPONSE = 12;
	/**
	 * Delete response index.
	 */
	private static final int DEL_RESPONSE = 13;
	/**
	 * Modify DN response index.
	 */
	private static final int MOD_DN_RESPONSE = 14;
	/**
	 * Compare response index.
	 */
	private static final int COMPARE_RESPONSE = 15;
	/**
	 * Extended response index.
	 */
	private static final int EXTENDED_RESPONSE = 16;
	/**
	 * Error response index.
	 */
	private static final int ERROR_RESPONSE = 17;
	/**
	 * Search result done response index.
	 */
	private static final int SEARCH_RESULT_DONE = 20;

	/**
	 * List of binary Attributes
	 */
	private ArrayList<String> mBinaryAttributes = new ArrayList<String>();

	/**
	 * Holds mapped pairs for request/respond
	 */
	private static Map<String, String> mRequestResponseMap = new HashMap<String, String>();

	static {
		mRequestResponseMap.put(OPERATION_ADD_REQUEST, OPERATION_ADD_RESPONSE);
		mRequestResponseMap.put(OPERATION_SEARCH_REQUEST,
				OPERATION_SEARCH_RESPONSE);
		mRequestResponseMap.put(OPERATION_DELETE_REQUEST,
				OPERATION_DELETE_RESPONSE);
		mRequestResponseMap.put(OPERATION_COMPARE_REQUEST,
				OPERATION_COMPARE_RESPONSE);
		mRequestResponseMap.put(OPERATION_MODIFY_REQUEST,
				OPERATION_MODIFY_RESPONSE);
		mRequestResponseMap.put(OPERATION_MODIFYDN_REQUEST,
				OPERATION_MODIFYDN_RESPONSE);
		mRequestResponseMap.put(OPERATION_EXTENDED_REQUEST,
				OPERATION_EXTENDED_RESPONSE);
		mRequestResponseMap
				.put(OPERATION_AUTH_REQUEST, OPERATION_AUTH_RESPONSE);
	}

	/**
	 * Input {@link Document}
	 */
	private Document mInputDoc;
	/**
	 * Output {@link Document}
	 */
	private Document mOutputDoc;
	/**
	 * Top level {@link Element}
	 */
	private Element mToplevelInput;

	/**
	 * {@link DocumentBuilder}
	 */
	private DocumentBuilder mDocumentBuilder;

	/**
	 * Enable/disable indent output
	 */
	private boolean mIndentOutput = true;

	/**
	 * If true XML declaration is omitted.
	 */
	private boolean mOmitXmlDecl = false;

	/**
	 * This parameter enables parser compatibility with SOAP protocol.
	 */
	private boolean mSoapBinding = false;

	/**
	 * Is the parser in server mode, default true.
	 */
	private boolean mServerMode = true;

	/**
	 * Batch request parameter
	 */
	private DsmlRequest mBatchRequest;
	/**
	 * Batch response parameter
	 */
	private DsmlResponse mBatchResponse;

	/**
	 * Character encoding to use when reading or writing
	 */
	private String mCharSet;

	/**
	 * Inner node from the output XML document.
	 */
	private Node mLastNode = null;

	/**
	 * Holds batch requests
	 */
	private Vector<AuthRequest> mRequestsVector;

	/**
	 * Holds batch responses
	 */
	private Vector<AuthRequest> mResponsesVector;

	/**
	 * Enumeration on the elements of the request/response Vector
	 */
	private Enumeration<AuthRequest> mEntriesEnum;

	/**
	 * Used for transforming DOM to String
	 */
	private Transformer mTrans = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * This method maps request and response operations Used from DSMLv2
	 * EventHandler
	 * 
	 * @param aRequestOperation
	 *            The request operation that will be mapped
	 * @return response of corresponding request operation
	 */
	public static String getResponseOperation(String aRequestOperation) {

		Object op = mRequestResponseMap.get(aRequestOperation);
		if (op != null) {
			return (String) op;
		} else {
			return aRequestOperation;
		}
	}

	/**
	 * Initializes the parser.
	 * 
	 * @throws Exception
	 *             if initialization fails
	 */
	public void initParser() throws Exception {
		Trace.entrymin(this, "initParser");
		resetProperties();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		String binaryAttributes = (String) getParam(PARAMETER_BINARY_ATTRIBUTES);
		if ((binaryAttributes != null) && (binaryAttributes.length() > 0)) {
			StringTokenizer tokenizer = new StringTokenizer(binaryAttributes,
					",");
			while (tokenizer.hasMoreTokens()) {
				mBinaryAttributes.add(tokenizer.nextToken().trim());
			}
		}

		dbf.setValidating(false);
		dbf.setNamespaceAware(true);

		mCharSet = getParam("characterSet");
		if (mCharSet == null || mCharSet.equals("")) {
			mCharSet = "UTF-8";
		}

		String mode = (String) getParam(PARAMETER_MODE);
		if (mode == null) {
			throw new Exception(sResHash.getString(
					"PARSER.DSML.REQUIRED.PARAMMODE.ERROR", PARAMETER_MODE));
		}
		if (mode.equalsIgnoreCase("Client")) {
			mServerMode = false;
			if (debugMode()) {
				debug(sResHash.getString("PARSER.DSML.SERVERMODE.INFO"));
			}
		}

		String check = getParam("indentoutput");
		if (check != null && check.equalsIgnoreCase("false")) {
			mIndentOutput = false;
			if (debugMode()) {
				debug(sResHash
						.getString("PARSER.DSML.WILLNOTINDENTOUTPUT.INFO"));
			}
		}

		check = getParam("omitxmldeclaration");
		if (check != null) {
			mOmitXmlDecl = check.equalsIgnoreCase("true");
		}

		check = getParam("soapbinding");
		if (check != null && check.equalsIgnoreCase("true")) {
			mSoapBinding = true;
			if (debugMode()) {
				debug(sResHash.getString("PARSER.DSML.WILLMAKESOAPENV.INFO"));
			}
		}

		mDocumentBuilder = dbf.newDocumentBuilder();

		if (getReader() != null) {
			initInput();
		}

		if (getWriter() != null) {
			initOutput();
		}
		Trace.exitmin(this, "initParser");
	}

	/**
	 * Initializes the input with the batch message.
	 * 
	 * @exception Exception
	 *                Throws exception if input initialization fails
	 */
	@SuppressWarnings("unchecked")
	private void initInput() throws Exception {

		Trace.entrymin(this, "initInput");
		if (getReader() == null) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.TRYINGREADDSML.NOINPUT.ERROR"));
		}

		if (debugMode()) {
			debug(sResHash.getString("PARSER.DSML.INITINPUTDOC.INFO"));
		}

		InputSource inputSource = null;

		if (getInputStream() != null && !mCharSet.startsWith("UTF-16")
				&& !mCharSet.startsWith("UTF-32")) {
			inputSource = new InputSource(getInputStream());
			if (mCharSet.length() > 0) {
				inputSource.setEncoding(mCharSet);
			}
		} else {
			inputSource = new InputSource(getReader());
		}

		if (mSoapBinding) {		        
	        // following code refactors the soap envelope creating using Axis2 and Axiom libraries
	        SOAPFactory factory = null;
	        factory = OMAbstractFactory.getSOAP12Factory();
	        SOAPEnvelope envelope = factory.getDefaultEnvelope();
	        XMLInputFactory xmlfactory = XMLInputFactory.newInstance();
	        XMLStreamReader reader = xmlfactory.createXMLStreamReader(getInputStream(inputSource));
	        OMElement omElement = OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
	        envelope.getBody().addChild(omElement);
	        //        
	        OMElement body = envelope.getBody();
	
	        String rootString = envelope.getBody().toString();
	
			StringReader stringReader = new StringReader(rootString);
			inputSource = new InputSource(stringReader);	       
		}

		mInputDoc = mDocumentBuilder.parse(inputSource);

		mToplevelInput = mInputDoc.getDocumentElement();

		if (mToplevelInput == null) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.DOCHASNOROOT.ERROR"));
		}

		if (mServerMode) {
			if (debugMode()) {
				debug(sResHash
						.getString("PARSER.DSML.READING.BATCHREQUEST.INFO"));
			}
			mBatchRequest = new DsmlRequest(mToplevelInput);
			mRequestsVector = mBatchRequest.getRequests();

			if (mBatchRequest.getAuthRequest() != null) {
				mRequestsVector.add(0, mBatchRequest.getAuthRequest());
			}

			mEntriesEnum = mRequestsVector.elements();
		} else {
			if (debugMode()) {
				debug(sResHash
						.getString("PARSER.DSML.READING.BATCHRESPONSE.INFO"));
			}
			mBatchResponse = new DsmlResponse(mToplevelInput);
			mResponsesVector = mBatchResponse.getResponses();
			mEntriesEnum = mResponsesVector.elements();
		}
		Trace.exitmin(this, "initInput");
	}

	private InputStream getInputStream(InputSource inputSource) throws IOException {
		if (inputSource.getByteStream() != null)
			return inputSource.getByteStream();

		// There should be a class to convert a Reader to a Stream...
		Reader r = inputSource.getCharacterStream();
		if (r == null)
			return null;

		StringBuilder s = new StringBuilder();
		int i;
		while ((i = r.read()) != -1)
			s.append((char)i);
		return new ByteArrayInputStream(s.toString().getBytes("UTF-8"));				
	}

	/**
	 * Initializes the output.
	 * 
	 * @exception Exception
	 *                Throws exception if output initialization fails
	 */
	private void initOutput() throws Exception {
		Trace.entrymin(this, "initOutput");

		mTrans = TransformerFactory.newInstance().newTransformer();

		if (mIndentOutput) {
			mTrans.setOutputProperty(OutputKeys.INDENT, "yes");
			mTrans.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");
		} else {
			mTrans.setOutputProperty(OutputKeys.INDENT, "no");
		}

		mTrans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		if (getWriter() == null) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.TRYINGWRITEDSML.NOINPUT.ERROR"));
		}

		if (debugMode()) {
			debug(sResHash.getString("PARSER.DSML.INITOUTPUT.DOC.INFO"));
		}
		mOutputDoc = mDocumentBuilder.newDocument();

		StringBuilder msg = new StringBuilder();
		
		if (!mOmitXmlDecl) {
			msg.append(XML_DECLAR);
		}

		if (mSoapBinding) {
			msg.append(SOAP_BEGIN);
		}
			
		if (mServerMode) {
			msg.append(BATCH_RESPONSE_BEGIN);
		} else {
			msg.append(BATCH_REQUEST_BEGIN);
		}
		
		msg.append(BATCH_DEFAULT_NAMESPACES);
		
		if (! mServerMode) {
			String check = getParam("processing");
			if (check != null && check.equalsIgnoreCase("parallel")) {
				msg.append(" processing=\"parallel\"");
				if (debugMode()) {
					debug(sResHash.getString("PARSER.DSML.PARALLELPROCESSING.INFO"));
				}
			}

			check = getParam("onError");
			if (check != null && check.equalsIgnoreCase("resume")) {
				msg.append(" onError=\"resume\"");
				if (debugMode()) {
					debug(sResHash.getString("PARSER.DSML.RESUMEONERROR.INFO"));
				}
			}

			check = getParam("responseOrder");
			if (check != null && check.equalsIgnoreCase("unordered")) {
				msg.append(" responseOrder=\"unordered\"");
				if (debugMode()) {
					debug(sResHash.getString("PARSER.DSML.RESPONSESUGNORED.INFO"));
				}
			}
		}

		String batchRequestId = getParam(PARAMETER_BATCH_REQUEST_ID);
		if (batchRequestId != null && batchRequestId.trim().length() > 0) {
			msg.append(" requestID=\"");
			msg.append(batchRequestId);
			msg.append("\"");
		}
		
		msg.append(TAG_END);
		getWriter().write(msg.toString());
		Trace.exitmin(this, "initOutput");
	}

	/**
	 * Reads a request or response from BatchMessage.
	 * 
	 * @return next entry
	 * @exception Exception
	 *                Throws exception if type of request or response not
	 *                recognized
	 */
	public Entry readEntry() throws Exception {
		Trace.entrymax(this, "readEntry");
		if (!mEntriesEnum.hasMoreElements()) {
			return null;
		}

		Entry entry = null;

		BatchMessage batchMessage = mEntriesEnum.nextElement();
		int msgType = batchMessage.getMsgType();

		switch (msgType) {
		case AUTH_REQUEST:
			entry = readAuthReq(batchMessage);
			break;
		case SEARCH_REQUEST:
			entry = readSearchReq(batchMessage);
			break;
		case MODIFY_REQUEST:
			entry = readModifyReq(batchMessage);
			break;
		case ADD_REQUEST:
			entry = readAddReq(batchMessage);
			break;
		case DEL_REQUEST:
			entry = readDeleteReq(batchMessage);
			break;
		case MOD_DN_REQUEST:
			entry = readModDNReq(batchMessage);
			break;
		case COMPARE_REQUEST:
			entry = readCompareReq(batchMessage);
			break;
		case ABANDON_REQUEST:
			entry = readAbandonReq(batchMessage);
			break;
		case EXTENDED_REQUEST:
			entry = readExtendedReq(batchMessage);
			break;
		case AUTH_RESPONSE:
			entry = readLdapResult(batchMessage, OPERATION_AUTH_RESPONSE);
			break;
		case MODIFY_RESPONSE:
			entry = readLdapResult(batchMessage, OPERATION_MODIFY_RESPONSE);
			break;
		case ADD_RESPONSE:
			entry = readLdapResult(batchMessage, OPERATION_ADD_RESPONSE);
			break;
		case DEL_RESPONSE:
			entry = readLdapResult(batchMessage, OPERATION_DELETE_RESPONSE);
			break;
		case MOD_DN_RESPONSE:
			entry = readLdapResult(batchMessage, OPERATION_MODIFYDN_RESPONSE);
			break;
		case COMPARE_RESPONSE:
			entry = readLdapResult(batchMessage, OPERATION_COMPARE_RESPONSE);
			break;
		case EXTENDED_RESPONSE:
			entry = readExtendedResp(batchMessage);
			break;
		case ERROR_RESPONSE:
			entry = readErrorResp(batchMessage);
			break;
		case SEARCH_RESPONSE:
			entry = readSearchResp(batchMessage);
			break;
		default:
			throw new Exception(sResHash.getString(
					"PARSER.DSML.MESSAGENOGSUPPORTED.ERROR",
					BatchMessage.ELEMENT_NAMES[msgType]));
		}
		Trace.exitmax(this, "readEntry", entry);
		return entry;
	}

	/**
	 * Writes a request or response to the batch message.
	 * 
	 * @param aEntry
	 *            The entry which will be written
	 * @exception Exception
	 *                Throws exception if type of request or response not
	 *                recognized
	 */
	public void writeEntry(Entry aEntry) throws Exception {
		Trace.entrymax(this, "writeEntry", aEntry);

		String dsmlOperation = aEntry.getString(ATTR_NAME_DSML_OPERATION);
		if (dsmlOperation == null) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.ATTRIBUTE.MISSING.ERROR"));
		}

		if (dsmlOperation.equals(OPERATION_AUTH_REQUEST)) {

			AuthRequest req = writeAuthReq(aEntry);
			marshal(req.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_SEARCH_REQUEST)) {

			SearchRequest req = writeSearchReq(aEntry);
			marshal(req.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_MODIFY_REQUEST)) {

			ModifyRequest req = writeModifyReq(aEntry);
			marshal(req.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_ADD_REQUEST)) {

			AddRequest req = writeAddReq(aEntry);
			marshal(req.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_DELETE_REQUEST)) {

			DeleteRequest req = writeDeleteReq(aEntry);
			marshal(req.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_MODIFYDN_REQUEST)) {

			ModifyDNRequest req = writeModDNReq(aEntry);
			marshal(req.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_COMPARE_REQUEST)) {

			CompareRequest req = writeCompareReq(aEntry);
			marshal(req.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_ABANDON_REQUEST)) {

			AbandonRequest req = writeAbandonReq(aEntry);
			marshal(req.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_EXTENDED_REQUEST)) {

			ExtendedRequest req = writeExtendedReq(aEntry);
			marshal(req.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_SEARCH_RESPONSE)) {

			SearchResponse resp = writeSearchResp(aEntry);
			marshal(resp.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_AUTH_RESPONSE)) {

			LdapResult resp = writeLdapResult(AUTH_RESPONSE, aEntry);
			marshal(resp.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_MODIFY_RESPONSE)) {

			LdapResult resp = writeLdapResult(MODIFY_RESPONSE, aEntry);
			marshal(resp.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_ADD_RESPONSE)) {

			LdapResult resp = writeLdapResult(ADD_RESPONSE, aEntry);
			marshal(resp.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_DELETE_RESPONSE)) {

			LdapResult resp = writeLdapResult(DEL_RESPONSE, aEntry);
			marshal(resp.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_MODIFYDN_RESPONSE)) {

			LdapResult resp = writeLdapResult(MOD_DN_RESPONSE, aEntry);
			marshal(resp.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_COMPARE_RESPONSE)) {

			LdapResult resp = writeLdapResult(COMPARE_RESPONSE, aEntry);
			marshal(resp.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_EXTENDED_RESPONSE)) {

			ExtendedResponse resp = writeExtendedResp(aEntry);
			marshal(resp.toElement(mOutputDoc));

		} else if (dsmlOperation.equals(OPERATION_ERROR_RESPONSE)) {

			ErrorResponse resp = writeErrorResp(aEntry);
			marshal(resp.toElement(mOutputDoc));

		} else {
			throw new Exception(sResHash.getString(
					"PARSER.DSML.MESSAGENOGSUPPORTED.ERROR", dsmlOperation));
		}
		Trace.exitmax(this, "writeEntry");
	}

	/**
	 * Closes the parser
	 * 
	 * @exception Exception
	 *                Throws exception if closing fails
	 */
	public void closeParser() throws Exception {
		flush();
		super.closeParser();
	}

	/**
	 * Flushes the parser's outputstream or writer
	 */
	public void flush() {

		if (getWriter() != null && mOutputDoc != null) {
			try {
				StringBuilder msg = new StringBuilder();
				if (mServerMode) {
					msg.append(BATCH_RESPONSE_END);
				} else {
					msg.append(BATCH_REQUEST_END);
				}
				if (mSoapBinding) {
					msg.append(SOAP_END);
				}
			 
				getWriter().write(msg.toString());
				getWriter().flush();
			} catch (Exception e) {
				logmsg(sResHash.getString("PARSER.DSML.RSPXML.WARNING", e));
			}
		}
	}

	/**
	 * Constructs a DOM from the provided {@link Element} and transforms it to
	 * String
	 * 
	 * @param aElement {@link Element}
	 */
	private void marshal(Element aElement) {
		mLastNode = aElement;
		if (getWriter() != null && mOutputDoc != null) {
			try {
				try {
					mTrans.transform(new DOMSource(aElement), new StreamResult(
							getWriter()));
				} catch (Exception e) {
					throw new Exception(sResHash.getString(
							"PARSER.DSML.TRASFORMING.DOM.TO.STRING.ERROR", e
									.toString()));
				}

				getWriter().flush();
			} catch (Exception e) {
				logmsg(sResHash.getString("PARSER.DSML.RSPXML.WARNING", e));
			}
		}
	}

	/**
	 * Retrieves inner node from the output xml document.
	 * 
	 * @return the last node.
	 */
	public Node getSingleNode() {
		return mLastNode;
	}

	/**
	 * Retrieves single search result only.
	 * 
	 * @param aEntry
	 *            the entry from which to retrieve single search result.
	 * @return SearchResultEntry object
	 */
	public static SearchResultEntry getSearchResultEntry(Entry aEntry) {

		if (aEntry == null) {
			return null;
		}

		SearchResultEntry searchResultEntry = new SearchResultEntry();

		String requestID = aEntry
				.getString(Dsmlv2Parser.ATTR_NAME_DSML_REQUEST_ID);
		if (requestID != null && requestID.length() > 0) {
			aEntry.removeAttribute(Dsmlv2Parser.ATTR_NAME_DSML_REQUEST_ID);
		}

		if (aEntry.getString(Dsmlv2Parser.ATTR_NAME_DN) != null) {
			searchResultEntry
					.setDN(aEntry.getString(Dsmlv2Parser.ATTR_NAME_DN));
			aEntry.removeAttribute(Dsmlv2Parser.ATTR_NAME_DN);
		}

		String[] entryAttributes = aEntry.getAttributeNames();
		for (int j = 0; j < entryAttributes.length; j++) {
			Attribute attribute = (Attribute) aEntry
					.getAttribute(entryAttributes[j]);
			DsmlAttr dsmlAttr = new DsmlAttr(attribute.getName());
			Object[] valsObj = attribute.getValues();
			for (int k = 0; k < valsObj.length; k++) {

				DsmlValue dsmlVal = null;

				if (valsObj[k] instanceof java.lang.String) {
					dsmlVal = new DsmlValue((String) valsObj[k]);
				} else if (valsObj[k] instanceof byte[]) {
					// we need to Base64 encode the value
					String encodedValue = Base64.encode((byte[]) valsObj[k]);
					dsmlVal = new DsmlValue(encodedValue);
				} else {
					// process other types
					dsmlVal = new DsmlValue(valsObj[k].toString());
				}

				dsmlAttr.setValue(dsmlVal);
			}
			searchResultEntry.setDsmlAttribute(dsmlAttr);
		}

		return searchResultEntry;
	}

	/**
	 * Retrieves search result done.
	 * 
	 * @param aEntry
	 *            the entry from which to retrieve search result.
	 * @return LDAP result
	 */
	public static LdapResult getSearchResultDoneFromEntry(Entry aEntry) {
		LdapResult ldapResult = new LdapResult(SEARCH_RESULT_DONE);

		if (aEntry.getString(Dsmlv2Parser.ATTR_NAME_DSML_RESULTCODE) != null) {
			ldapResult.setResultCode(Integer.parseInt(aEntry
					.getString(Dsmlv2Parser.ATTR_NAME_DSML_RESULTCODE)));
		} else {
			ldapResult.setResultCode(0); // "success" by default
		}

		String error = aEntry
				.getString(Dsmlv2Parser.ATTR_NAME_DSML_ERROR_MESSAGE);
		String result = UserFunctions.removeInvalidXMLChars(error);
		ldapResult.setErrorMessage(result);

		addControlstoMessage(aEntry, ldapResult);
		addReqIDtoMessage(aEntry, ldapResult);
		addReferralstoResult(aEntry, ldapResult);

		return ldapResult;
	}

	/**
	 * Version information.
	 * @return version information.
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I% 20%E%";
	}

	/**
	 * Decode the entry. All binary value attribute values will be decoded.
	 * 
	 * @param aEntry
	 *            The Entry to be decoded
	 * @exception Exception
	 *                Throws exception if decoding fails
	 */
	private void base64decode(Entry aEntry) throws Exception {

		Trace.entrymax(this, "base64decode", aEntry);

		Iterator<String> names = mBinaryAttributes.iterator();

		while (names.hasNext()) {
			String name = names.next();
			Attribute encodedAttr = aEntry.getAttribute(name);

			if ((encodedAttr != null) && (encodedAttr.size() > 0)) {
				if (debugMode()) {
					debug(sResHash.getString(
							"PARSER.DSML.BASE64.DECODINGATTRIB.INFO", name));
				}

				Object[] encodedValues = encodedAttr.getValues();
				Vector<Object> binaryValues = new Vector<Object>();

				for (int j = 0; j < encodedValues.length; j++) {
					if (encodedValues[j] instanceof String) {
						byte[] binaryValue = Base64
								.decode((String) encodedValues[j]);
						if (binaryValue == null) {
							logmsg(sResHash.getString(
									"PARSER.DSML.BINNOT64ENCODED.INFO", name));
						}
						binaryValues.add(binaryValue);
					} else {
						binaryValues.add(encodedValues[j]);
					}
				}

				encodedAttr.setValues(binaryValues);
				aEntry.setAttribute(encodedAttr);
			}
		}
		Trace.exitmax(this, "base64decode");
	}

	/**
	 * Encode byte array
	 * 
	 * @param aValue
	 *            Byte array to be encoded
	 * @return encoded string
	 */
	private String encodeValue(byte[] aValue) {
		return Base64.encode(aValue);
	}

	/**
	 * Transfer requestID from DsmlMessage to Entry
	 * 
	 * @param aMessage
	 *            dsml message containing requestID
	 * @param aEntry
	 *            entry to fill
	 */
	private static void addReqIDtoEntry(DsmlMessage aMessage, Entry aEntry) {

		String requestID = aMessage.getRequestID();
		if (requestID != null && requestID.trim().length() > 0) {
			aEntry.setAttribute(ATTR_NAME_DSML_REQUEST_ID, requestID);
		}
	}

	/**
	 * Transfer controls from DsmlMessage to Entry
	 * 
	 * @param aMessage
	 *            dsml message containing controls
	 * @param aEntry
	 *            entry to fill
	 */
	private static void addControlstoEntry(DsmlMessage aMessage, Entry aEntry) {

		if (aMessage.getControls().size() > 0) {
			aEntry.addAttributeValue(ATTR_NAME_DSML_CONTROLS, aMessage
					.getControls());
		}
	}

	/**
	 * Transfer referrals from LdapResult to Entry
	 * 
	 * @param ldapResult
	 *            dsml message containing referrals
	 * @param aEntry
	 *            entry to fill
	 */
	private static void addReferralstoEntry(LdapResult ldapResult, Entry aEntry) {

		if (ldapResult.getReferrals().size() > 0) {
			aEntry.setAttribute(ATTR_NAME_DSML_REFFERAL, ldapResult
					.getReferrals());
		}
	}

	/**
	 * Transfer requestID from Entry to DsmlMessage
	 * 
	 * @param aEntry
	 *            entry containing requestID
	 * @param aMessage
	 *            dsml message to fill
	 */
	private static void addReqIDtoMessage(Entry aEntry, DsmlMessage aMessage) {

		String requestID = aEntry.getString(ATTR_NAME_DSML_REQUEST_ID);
		if (requestID != null && requestID.trim().length() > 0) {
			aMessage.setRequestID(requestID);
		}
	}

	/**
	 * Transfer controls from Entry to DsmlMessage
	 * 
	 * @param aEntry
	 *            entry containing controls
	 * @param aMessage
	 *            dsml message to fill
	 */
	private static void addControlstoMessage(Entry aEntry, DsmlMessage aMessage) {

		if (aEntry.getAttribute(Dsmlv2Parser.ATTR_NAME_DSML_CONTROLS) != null) {
			Vector<?> controls = (Vector<?>) aEntry.getAttribute(
					ATTR_NAME_DSML_CONTROLS).getValue(0);
			for (int j = 0; j < controls.size(); j++) {
				DsmlControl dsmlControl = (DsmlControl) controls.elementAt(j);
				aMessage.setControl(dsmlControl);
			}
		}
	}

	/**
	 * Transfer referrals from Entry to LdapResult
	 * 
	 * @param aEntry
	 *            entry containing referrals
	 * @param ldapResult
	 *            LdapResult to fill
	 */
	private static void addReferralstoResult(Entry aEntry, LdapResult ldapResult) {

		Vector<?> refferalsVector = (Vector<?>) aEntry
				.getObject(ATTR_NAME_DSML_REFFERAL);
		if (refferalsVector != null) {
			for (int j = 0; j < refferalsVector.size(); j++) {
				String uri = (String) refferalsVector.elementAt(j);
				ldapResult.setReferral(uri);
			}
		}
	}

	/**
	 * Removes attributes from Entry which starts with "dsml."
	 * 
	 * @param aEntry
	 *            Entry to be stripped
	 */
	private void stripDSMLAttributes(Entry aEntry) {

		String[] names = aEntry.getAttributeNames();
		for (int i = 0; i < names.length; i++) {
			if (names[i].startsWith("dsml.")) {
				aEntry.removeAttribute(names[i]);
			}
		}
	}

	/**
	 * Removes invalid XML characters in dsml.error message
	 * 
	 * @param aEntry
	 *            Entry to be processed
	 * @return valid error message
	 */
	private String getErrorMessage(Entry aEntry) {
		String result = aEntry.getString(ATTR_NAME_DSML_ERROR_MESSAGE);
		return UserFunctions.removeInvalidXMLChars(result);
	}

	/**
	 * Reads authorization request
	 * 
	 * @param aBatchMessage
	 *            batch message containing request
	 * @return entry containing authorization request
	 */
	private Entry readAuthReq(BatchMessage aBatchMessage) {
		Trace.entrymax(this, "readAuthReq", aBatchMessage);
		Entry entry = new Entry();

		AuthRequest authRequest = (AuthRequest) aBatchMessage;
		entry.addAttributeValue(ATTR_NAME_DSML_PRINCIPAL, authRequest
				.getPrincipal());
		entry.setAttribute(ATTR_NAME_DSML_OPERATION, OPERATION_AUTH_REQUEST);

		addControlstoEntry(authRequest, entry);
		addReqIDtoEntry(authRequest, entry);
		Trace.exitmax(this, "readAuthReq", entry);
		return entry;
	}

	/**
	 * Writes authorization request
	 * 
	 * @param aEntry
	 *            entry containing request
	 * @return Authorization request
	 * @exception Exception
	 *                Throws exception if principal not set
	 */
	private AuthRequest writeAuthReq(Entry aEntry) throws Exception {
		Trace.entrymax(this, "writeAuthReq", aEntry);
		AuthRequest authRequest = new AuthRequest();
		if (!authRequest.setPrincipal(aEntry
				.getString(ATTR_NAME_DSML_PRINCIPAL))) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.PRINCIPAL.NOTSET.ERROR"));
		}

		addControlstoMessage(aEntry, authRequest);
		addReqIDtoMessage(aEntry, authRequest);
		Trace.exitmax(this, "writeAuthReq", authRequest);
		return authRequest;
	}

	/**
	 * Reads search request
	 * 
	 * @param aBatchMessage
	 *            batch message containing request
	 * @return entry containing search request
	 */
	private Entry readSearchReq(BatchMessage aBatchMessage) {
		Trace.entrymax(this, "readSearchReq", aBatchMessage);
		Entry entry = new Entry();

		SearchRequest searchRequest = (SearchRequest) aBatchMessage;
		entry.setAttribute(ATTR_NAME_DN, searchRequest.getDN());
		entry.setAttribute(ATTR_NAME_DSML_BASE, searchRequest.getDN());
		entry.setAttribute(ATTR_NAME_DSML_OPERATION, OPERATION_SEARCH_REQUEST);

		entry.setAttribute(ATTR_NAME_DSML_SCOPE, searchRequest.getScope());
		entry.setAttribute(ATTR_NAME_DSML_DEREFALIASES, searchRequest
				.getDerefAliases());
		entry.setAttribute(ATTR_NAME_DSML_SIZELIMIT, String
				.valueOf(searchRequest.getSizeLimit()));
		entry.setAttribute(ATTR_NAME_DSML_TIMELIMIT, String
				.valueOf(searchRequest.getTimeLimit()));
		entry.setAttribute(ATTR_NAME_DSML_TYPESONLY, String
				.valueOf(searchRequest.getTypesOnly()));
		entry.setAttribute(ATTR_NAME_DSML_FILTER, searchRequest.getFilter()
				.getLdapFilter());

		addControlstoEntry(searchRequest, entry);
		addReqIDtoEntry(searchRequest, entry);

		AttributeDescriptions attrDescriptions = searchRequest.getAttributes();
		if (attrDescriptions != null) {
			Vector<?> attrs = attrDescriptions.getAttributeValues();
			if (attrs != null && attrs.size() > 0) {
				entry.addAttributeValue(ATTR_NAME_DSML_ATTRIBUTES, attrs);
			}
		}
		Trace.exitmax(this, "readSearchReq", entry);
		return entry;
	}

	/**
	 * Writes search request
	 * 
	 * @param aEntry
	 *            entry containing request
	 * @return search request
	 * @exception Exception
	 *                Throws exception if parameters not set
	 */
	private SearchRequest writeSearchReq(Entry aEntry) throws Exception {

		Trace.entrymax(this, "writeSearchReq", aEntry);

		SearchRequest searchRequest = new SearchRequest();

		if (!searchRequest.setDN(aEntry.getString(ATTR_NAME_DN))) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.DN.NOTSET.SEARCH.ERROR"));
		}

		if (!searchRequest.setScope(aEntry.getString(ATTR_NAME_DSML_SCOPE))) {
			searchRequest.setScope(WHOLE_SUBTREE); // default value for
			// searchRequest
		}

		if (!searchRequest.setDerefAliases(aEntry
				.getString(ATTR_NAME_DSML_DEREFALIASES))) {
			searchRequest.setDerefAliases(DEREF_NEVER); // default value for
			// searchRequest
		}

		if (!searchRequest.setSizeLimit(aEntry
				.getString(ATTR_NAME_DSML_SIZELIMIT))) {
			searchRequest.setSizeLimit(0); // default value for searchRequest
		}

		if (!searchRequest.setTimeLimit(aEntry
				.getString(ATTR_NAME_DSML_TIMELIMIT))) {
			searchRequest.setTimeLimit(0); // default value for searchRequest
		}

		if (!searchRequest.setTypesOnly(aEntry
				.getString(ATTR_NAME_DSML_TYPESONLY))) {
			searchRequest.setTypesOnly(false); // default value for
			// searchRequest
		}

		String dsmlFilter = aEntry.getString(ATTR_NAME_DSML_FILTER);
		Filter filter = new Filter(dsmlFilter);
		searchRequest.setFilter(filter);

		Object attrObj = aEntry.getObject(ATTR_NAME_DSML_ATTRIBUTES);

		if (attrObj instanceof Vector) {
			Vector<?> attrVector = (Vector<?>) attrObj;
			if (attrVector != null) {
				AttributeDescriptions attrdesc = new AttributeDescriptions();
				for (int i = 0; i < attrVector.size(); i++) {
					attrdesc.setAttributeValue((String) attrVector.get(i));
				}

				searchRequest.setAttributes(attrdesc);
			}

		} else

		{
			Attribute attr = aEntry.getAttribute(ATTR_NAME_DSML_ATTRIBUTES);
			if (attr != null) {
				AttributeDescriptions attrdesc = new AttributeDescriptions();
				for (int i = 0; i < attr.size(); i++) {
					attrdesc.setAttributeValue((String) attr.getValue(i));
				}

				searchRequest.setAttributes(attrdesc);
			}

		}

		addControlstoMessage(aEntry, searchRequest);
		addReqIDtoMessage(aEntry, searchRequest);
		Trace.exitmax(this, "writeSearchReq", searchRequest);
		return searchRequest;
	}

	/**
	 * Reads modify request
	 * 
	 * @param aBatchMessage
	 *            batch message containing request
	 * @return entry containing modify request
	 */
	private Entry readModifyReq(BatchMessage aBatchMessage) {
		Trace.entrymax(this, "readModifyReq", aBatchMessage);
		Entry entry = new Entry();

		ModifyRequest modRequest = (ModifyRequest) aBatchMessage;
		String dn = modRequest.getDN();

		entry.setOp(Entry.OP_MOD);
		entry.setAttribute(ATTR_NAME_DN, dn);
		entry.setAttribute(ATTR_NAME_DSML_BASE, dn);
		entry.setAttribute(ATTR_NAME_DSML_OPERATION, OPERATION_MODIFY_REQUEST);

		addControlstoEntry(modRequest, entry);
		addReqIDtoEntry(modRequest, entry);

		Vector<?> modificationVect = modRequest.getModifications();

		for (int i = 0; i < modificationVect.size(); i++) {
			DsmlModification modification = (DsmlModification) modificationVect
					.elementAt(i);
			Attribute attribute = entry.getAttribute(modification.getName());
			Vector<?> values = modification.getValues();
			char prevOp = Attribute.ATTRIBUTE_MOD;
			if (attribute == null) {
				attribute = entry.newAttribute(modification.getName());
			} else {
				prevOp = attribute.getOper();
			}

			char nextOp = modification.getOperation().charAt(0);
			switch (nextOp) {
			case Attribute.ATTRIBUTE_ADD:
				switch (prevOp) {
				case Attribute.ATTRIBUTE_MOD:
					for (int j = 0; j < values.size(); j++) {
						DsmlValue dsmlValue = (DsmlValue) values.elementAt(j);
						attribute.addValue(dsmlValue.getValue(),
								AttributeValue.AV_ADD);
					}
					break;
				default: // if previous is ADD or REPLACE we just add new
					// values
					for (int j = 0; j < values.size(); j++) {
						DsmlValue dsmlValue = (DsmlValue) values.elementAt(j);
						attribute.addValue(dsmlValue.getValue());
					}
					break;
				}
				break;

			case Attribute.ATTRIBUTE_DELETE:
				if (values.size() > 0) {
					switch (prevOp) {
					case Attribute.ATTRIBUTE_REPLACE:
						for (int j = 0; j < values.size(); j++) {
							DsmlValue dsmlValue = (DsmlValue) values
									.elementAt(j);
							attribute.removeValue(dsmlValue.getValue());
						}
						break;
					default:
						attribute.setOper(Attribute.ATTRIBUTE_MOD);
						for (int j = 0; j < values.size(); j++) {
							DsmlValue dsmlValue = (DsmlValue) values
									.elementAt(j);
							attribute.addValue(dsmlValue.getValue(),
									AttributeValue.AV_DELETE);
						}
						break;
					}
				} else {
					attribute.clear(); // tag Attribute as Replace
				}
				break;

			default: // Attribute.ATTRIBUTE_REPLACE
				attribute.clear(); // tag Attribute as Replace
				for (int j = 0; j < values.size(); j++) {
					DsmlValue dsmlValue = (DsmlValue) values.elementAt(j);
					attribute.addValue(dsmlValue.getValue());
				}
				break;
			}
		}

		try {
			base64decode(entry);
		} catch (Exception ex) {
			logmsg(sResHash.getString("PARSER.DSML.ERROR.BASE64DEC.WARNING",
					entry.toString()));
		}
		Trace.exitmax(this, "readModifyReq", entry);
		return entry;
	}

	/**
	 * Writes modify request
	 * 
	 * @param aEntry
	 *            entry containing request
	 * 
	 * @return modify request
	 * @exception Exception
	 *                Throws exception if parameters not set
	 */
	private ModifyRequest writeModifyReq(Entry aEntry) throws Exception {

		Trace.entrymax(this, "writeModifyReq", aEntry);
		ModifyRequest modRequest = new ModifyRequest();

		String dn = aEntry.getString(ATTR_NAME_DN);
		if (!modRequest.setDN(dn)) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.DN.NOTSET.MODIFYREQUEST.ERROR"));
		}

		aEntry.removeAttribute(ATTR_NAME_DN);

		addControlstoMessage(aEntry, modRequest);
		addReqIDtoMessage(aEntry, modRequest);

		stripDSMLAttributes(aEntry);

		String[] aNames = aEntry.getAttributeNames();
		for (int i = 0; i < aNames.length; i++) {

			Attribute attribute = aEntry.getAttribute(aNames[i]);

			if (attribute.getOper() == Attribute.ATTRIBUTE_MOD) {

				DsmlModification modificationAdd = new DsmlModification();
				modificationAdd.setName(attribute.getName());
				DsmlModification modificationDelete = new DsmlModification();
				modificationDelete.setName(attribute.getName());

				for (int index = 0; index < attribute.size(); index++) {
					Object value = attribute.getValue(index);
					DsmlValue dsmlValue = null;
					if (value instanceof String) {
						dsmlValue = new DsmlValue((String) value);
					} else if (value instanceof byte[]) {
						String encodedValue = encodeValue((byte[]) value);
						dsmlValue = new DsmlValue(encodedValue);
					} else {
						dsmlValue = new DsmlValue(value.toString());
					}

					if (attribute.getValueOper(index) == AttributeValue.AV_DELETE) {
						modificationDelete.setValue(dsmlValue);
					} else if (attribute.getValueOper(index) != AttributeValue.AV_UNCHANGED) {
						modificationAdd.setValue(dsmlValue);
					}
				}

				modificationAdd.setOperation("add");
				if (!modRequest.setModification(modificationAdd)) {
					throw new Exception(sResHash
							.getString("PARSER.DSML.MODIFICATION.NOTSET.ERROR"));
				}
				modificationDelete.setOperation("delete");
				if (!modRequest.setModification(modificationDelete)) {
					throw new Exception(sResHash
							.getString("PARSER.DSML.MODIFICATION.NOTSET.ERROR"));
				}

			} else {
				DsmlModification modification = new DsmlModification();
				modification.setName(attribute.getName());

				for (int index = 0; index < attribute.size(); index++) {
					Object value = attribute.getValue(index);

					if (value instanceof String) {
						modification.setValue(new DsmlValue((String) value));
					} else if (value instanceof byte[]) {
						String encodedValue = encodeValue((byte[]) value);
						modification.setValue(new DsmlValue(encodedValue));
					} else {
						modification.setValue(new DsmlValue(value.toString()));
					}
				}

				modification.setOperation(attribute.getOperation());
				if (!modRequest.setModification(modification)) {
					throw new Exception(sResHash
							.getString("PARSER.DSML.MODIFICATION.NOTSET.ERROR"));
				}
			}
		}
		Trace.exitmax(this, "writeModifyReq", modRequest);
		return modRequest;
	}

	/**
	 * Reads add request
	 * 
	 * @param aBatchMessage
	 *            batch message containing request
	 * @return entry containing add request
	 */
	private Entry readAddReq(BatchMessage aBatchMessage) {
		Trace.entrymax(this, "readAddReq", aBatchMessage);

		Entry entry = new Entry();

		AddRequest addRequest = (AddRequest) aBatchMessage;
		String dn = addRequest.getDN();
		entry.setOp(Entry.OP_ADD);
		entry.addAttributeValue(ATTR_NAME_DN, dn);
		entry.addAttributeValue(ATTR_NAME_DSML_BASE, dn);
		entry.setAttribute(ATTR_NAME_DSML_OPERATION, OPERATION_ADD_REQUEST);

		Vector<?> attrs = addRequest.getDsmlAttributes();
		for (int i = 0; i < attrs.size(); i++) {
			DsmlAttr attr = (DsmlAttr) attrs.elementAt(i);
			String name = attr.getName();
			Vector<?> vals = attr.getValues();
			for (int j = 0; j < vals.size(); j++) {
				DsmlValue val = (DsmlValue) vals.elementAt(j);
				entry.addAttributeValue(name, val.getValue());
			}
		}

		addControlstoEntry(addRequest, entry);
		addReqIDtoEntry(addRequest, entry);

		try {
			base64decode(entry);
		} catch (Exception ex) {
			logmsg(sResHash.getString("PARSER.DSML.ERROR.BASE64DEC.WARNING",
					entry.toString()));
		}
		Trace.exitmax(this, "readAddReq", entry);
		return entry;
	}

	/**
	 * Writes add request
	 * 
	 * @param aEntry
	 *            entry containing request
	 * 
	 * @return add request
	 * @exception Exception
	 *                Throws exception if parameters not set
	 */
	private AddRequest writeAddReq(Entry aEntry) throws Exception {

		Trace.entrymax(this, "writeAddReq", aEntry);

		AddRequest addRequest = new AddRequest();
		String dn = aEntry.getString(ATTR_NAME_DN);
		if (!addRequest.setDN(dn)) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.DNNOTSET.ADDREQUEST.ERROR"));
		}

		aEntry.removeAttribute(ATTR_NAME_DN);

		addControlstoMessage(aEntry, addRequest);
		addReqIDtoMessage(aEntry, addRequest);

		stripDSMLAttributes(aEntry);

		String[] attrs = aEntry.getAttributeNames();
		for (int i = 0; i < attrs.length; i++) {
			DsmlAttr dsmlAttr = new DsmlAttr(attrs[i]);
			Object[] valsObj = aEntry.getAttribute(attrs[i]).getValues();
			for (int j = 0; j < valsObj.length; j++) {

				DsmlValue dsmlVal = null;

				if (valsObj[j] instanceof java.lang.String) {
					dsmlVal = new DsmlValue((String) valsObj[j]);
				} else if (valsObj[j] instanceof byte[]) {
					String encodedValue = encodeValue((byte[]) valsObj[j]);
					dsmlVal = new DsmlValue(encodedValue);
				} else {
					dsmlVal = new DsmlValue(valsObj[j].toString());
				}

				dsmlAttr.setValue(dsmlVal);
			}

			if (!addRequest.setDsmlAttribute(dsmlAttr)) {
				throw new Exception(sResHash.getString(
						"PARSER.DSML.ATTRIB.NOTSET.ERROR", dsmlAttr));
			}
		}
		Trace.exitmax(this, "writeAddReq", addRequest);
		return addRequest;
	}

	/**
	 * Reads delete request
	 * 
	 * @param aBatchMessage
	 *            batch message containing request
	 * 
	 * @return entry containing delete request
	 */
	private Entry readDeleteReq(BatchMessage aBatchMessage) {
		Trace.entrymax(this, "readDeleteReq", aBatchMessage);
		Entry entry = new Entry();

		DeleteRequest delRequest = (DeleteRequest) aBatchMessage;
		String dn = delRequest.getDN();
		entry.setOp(Entry.OP_DEL);
		entry.setAttribute(ATTR_NAME_DN, dn);
		entry.setAttribute(ATTR_NAME_DSML_BASE, dn);
		entry.setAttribute(ATTR_NAME_DSML_OPERATION, OPERATION_DELETE_REQUEST);

		addControlstoEntry(delRequest, entry);
		addReqIDtoEntry(delRequest, entry);
		Trace.exitmax(this, "readDeleteReq", entry);
		return entry;
	}

	/**
	 * Writes delete request
	 * 
	 * @param aEntry
	 *            entry containing request
	 * @return delete request
	 * @exception Exception
	 *                Throws exception if parameters not set
	 */
	private DeleteRequest writeDeleteReq(Entry aEntry) throws Exception {
		Trace.entrymax(this, "writeDeleteReq", aEntry);
		DeleteRequest delRequest = new DeleteRequest();
		String dn = aEntry.getString(ATTR_NAME_DN);
		if (!delRequest.setDN(dn)) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.DNNOTSET.DELREQUEST.ERROR"));
		}

		addControlstoMessage(aEntry, delRequest);
		addReqIDtoMessage(aEntry, delRequest);
		Trace.exitmax(this, "writeDeleteReq", delRequest);
		return delRequest;
	}

	/**
	 * Reads modifyDN request
	 * 
	 * @param aBatchMessage
	 *            batch message containing request
	 * @return entry containing modifyDN request
	 */
	private Entry readModDNReq(BatchMessage aBatchMessage) {
		Trace.entrymax(this, "readModDNReq", aBatchMessage);
		Entry entry = new Entry();

		ModifyDNRequest modDnRequest = (ModifyDNRequest) aBatchMessage;
		String dn = modDnRequest.getDN();
		String newRdn = modDnRequest.getNewRDN();
		String newSuperior = modDnRequest.getNewSuperior();
		String delOldDN = modDnRequest.getDeleteOldRDN();

		entry.setOp(Entry.OP_MOD);
		entry.addAttributeValue(ATTR_NAME_DN, dn);
		entry.addAttributeValue(ATTR_NAME_DSML_BASE, dn);
		entry.addAttributeValue(ATTR_NAME_NEWRDN, newRdn);
		entry.addAttributeValue(ATTR_NAME_DSML_NEWSUPERIOR, newSuperior);
		entry.addAttributeValue(ATTR_NAME_DSML_DELETEOLDRDN, delOldDN);
		entry
				.setAttribute(ATTR_NAME_DSML_OPERATION,
						OPERATION_MODIFYDN_REQUEST);

		addControlstoEntry(modDnRequest, entry);
		addReqIDtoEntry(modDnRequest, entry);
		Trace.exitmax(this, "readModDNReq", entry);
		return entry;
	}

	/**
	 * Writes modifyDN request
	 * 
	 * @param aEntry
	 *            entry containing request
	 * @return modifyDN request
	 * @exception Exception
	 *                Throws exception if parameters not set
	 */
	private ModifyDNRequest writeModDNReq(Entry aEntry) throws Exception {

		Trace.entrymax(this, "writeModDNReq", aEntry);
		ModifyDNRequest modDnRequest = new ModifyDNRequest();
		String newSuperior = aEntry.getString(ATTR_NAME_DSML_NEWSUPERIOR);
		String delOldRDN = aEntry.getString(ATTR_NAME_DSML_DELETEOLDRDN);

		if (!modDnRequest.setDN(aEntry.getString(ATTR_NAME_DN))) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.DNNOTSET.MODDNREQUEST.ERROR"));
		}

		if (!modDnRequest.setNewRDN(aEntry.getString(ATTR_NAME_NEWRDN))) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.NEWRDN.NOTSET.MODDNREQUEST.ERROR"));
		}

		modDnRequest.setNewSuperior(newSuperior);
		modDnRequest.setDeleteOldRDN(delOldRDN);

		addControlstoMessage(aEntry, modDnRequest);
		addReqIDtoMessage(aEntry, modDnRequest);
		Trace.exitmax(this, "writeModDNReq", modDnRequest);
		return modDnRequest;
	}

	/**
	 * Reads compare request
	 * 
	 * @param aBatchMessage
	 *            batch message containing request
	 * @return entry containing compare request
	 */
	private Entry readCompareReq(BatchMessage aBatchMessage) {
		Trace.entrymax(this, "readCompareReq", aBatchMessage);
		Entry entry = new Entry();

		CompareRequest compRequest = (CompareRequest) aBatchMessage;

		String dn = compRequest.getDN();
		String compName = compRequest.getAssertion().getName();
		String compValue = compRequest.getAssertion().getValue().getValue();

		entry.addAttributeValue(ATTR_NAME_DN, dn);
		entry.addAttributeValue(ATTR_NAME_DSML_BASE, dn);
		entry.addAttributeValue(ATTR_NAME_DSML_COMPARE_NAME, compName);
		entry.addAttributeValue(ATTR_NAME_DSML_COMPARE_VALUE, compValue);
		entry.setAttribute(ATTR_NAME_DSML_OPERATION, OPERATION_COMPARE_REQUEST);

		addControlstoEntry(compRequest, entry);
		addReqIDtoEntry(compRequest, entry);
		Trace.exitmax(this, "readCompareReq", entry);
		return entry;
	}

	/**
	 * Writes compare request
	 * 
	 * @param aEntry
	 *            entry containing request
	 * @return compare request
	 * @exception Exception
	 *                Throws exception if parameters not set
	 */
	private CompareRequest writeCompareReq(Entry aEntry) throws Exception {
		Trace.entrymax(this, "witeCompareReq", aEntry);
		CompareRequest compRequest = new CompareRequest();
		String dn = aEntry.getString(ATTR_NAME_DN);
		if (!compRequest.setDN(dn)) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.DN.NOTSET.COMPREQUEST.ERROR"));
		}

		AttributeValueAssertion valueAssert = new AttributeValueAssertion();
		valueAssert.setAssertionName(aEntry
				.getString(ATTR_NAME_DSML_COMPARE_NAME));
		DsmlValue dsmlVal = new DsmlValue(aEntry
				.getString(ATTR_NAME_DSML_COMPARE_VALUE));
		valueAssert.setValue(dsmlVal);

		if (!compRequest.setAssertion(valueAssert)) {
			throw new Exception(
					sResHash
							.getString("PARSER.DSML.ASSERTION.NOTSET.COMPREQUEST.ERROR"));
		}

		addControlstoMessage(aEntry, compRequest);
		addReqIDtoMessage(aEntry, compRequest);
		Trace.exitmax(this, "writeCompareReq", compRequest);
		return compRequest;
	}

	/**
	 * Reads abandon request
	 * 
	 * @param aBatchMessage
	 *            batch message containing request
	 * @return entry containing abandon request
	 */
	private Entry readAbandonReq(BatchMessage aBatchMessage) {
		Trace.entrymax(this, "readAbandonReq", aBatchMessage);
		Entry entry = new Entry();

		AbandonRequest abandonReq = (AbandonRequest) aBatchMessage;
		entry
				.setAttribute(ATTR_NAME_DSML_ABANDON_ID, abandonReq
						.getAbandonID());
		entry.setAttribute(ATTR_NAME_DSML_OPERATION, OPERATION_ABANDON_REQUEST);

		addControlstoEntry(abandonReq, entry);
		addReqIDtoEntry(abandonReq, entry);
		Trace.exitmax(this, "readAbandonReq", entry);
		return entry;
	}

	/**
	 * Writes abandon request
	 * 
	 * @param aEntry
	 *            entry containing request
	 * @return abandon request
	 * @exception Exception
	 *                Throws exception if parameters not set
	 */
	private AbandonRequest writeAbandonReq(Entry aEntry) throws Exception {
		Trace.entrymax(this, "writeAbandonReq", aEntry);
		AbandonRequest abandonReq = new AbandonRequest();
		if (!abandonReq.setAbandonID(aEntry
				.getString(ATTR_NAME_DSML_ABANDON_ID))) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.ABANDONID.NOTSET.ERROR"));
		}

		addControlstoMessage(aEntry, abandonReq);
		addReqIDtoMessage(aEntry, abandonReq);
		Trace.exitmax(this, "writeAbandonReq", abandonReq);
		return abandonReq;
	}

	/**
	 * Reads extended request
	 * 
	 * @param aBatchMessage
	 *            batch message containing request
	 * @return entry containing extended request
	 */
	private Entry readExtendedReq(BatchMessage aBatchMessage) {
		Trace.entrymax(this, "readExtendedReq", aBatchMessage);
		Entry entry = new Entry();

		ExtendedRequest extendReq = (ExtendedRequest) aBatchMessage;
		entry.setAttribute(ATTR_NAME_DSML_REQUEST_NAME, extendReq
				.getRequestName());
		entry.setAttribute(ATTR_NAME_DSML_REQUEST_VALUE, extendReq
				.getEncodedValue());
		entry
				.setAttribute(ATTR_NAME_DSML_OPERATION,
						OPERATION_EXTENDED_REQUEST);

		addControlstoEntry(extendReq, entry);
		addReqIDtoEntry(extendReq, entry);
		Trace.exitmax(this, "readExtendedReq", entry);
		return entry;
	}

	/**
	 * Writes extended request
	 * 
	 * @param aEntry
	 *            entry containing request
	 * @return extended request
	 * @exception Exception
	 *                Throws exception if parameters not set
	 */
	private ExtendedRequest writeExtendedReq(Entry aEntry) throws Exception {
		Trace.entrymax(this, "writeExtendedReq", aEntry);
		ExtendedRequest extendReq = new ExtendedRequest();
		if (!extendReq.setRequestName(aEntry
				.getString(ATTR_NAME_DSML_REQUEST_NAME))) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.REQNAME.NOTSET.EXTREQUEST.ERROR"));
		}

		byte[] requestValue = (byte[]) aEntry
				.getObject(ATTR_NAME_DSML_REQUEST_VALUE);
		if (requestValue != null) {
			DsmlValue val = new DsmlValue(requestValue);
			if (!extendReq.setRequestValue(val)) {
				throw new Exception(
						sResHash
								.getString("PARSER.DSML.REQVALUE.NOTSET.EXTREQUEST.ERROR"));
			}
		}

		addControlstoMessage(aEntry, extendReq);
		addReqIDtoMessage(aEntry, extendReq);
		Trace.exitmax(this, "writeExtendedReq", extendReq);
		return extendReq;
	}

	/**
	 * Reads LdapResult
	 * 
	 * @param aBatchMessage
	 *            batch message containing response
	 * @param aOperation
	 *            corresponding dsml operation
	 * @return entry containing response
	 */
	private Entry readLdapResult(BatchMessage aBatchMessage, String aOperation) {
		Trace.entrymax(this, "readLdapResult", aBatchMessage, aOperation);
		Entry entry = new Entry();

		LdapResult ldapResult = (LdapResult) aBatchMessage;

		String resultCodeMsg = ldapResult.getResultCodeMSG();
		entry.setAttribute(ATTR_NAME_DN, ldapResult.getMatchedDN());
		entry.setAttribute(ATTR_NAME_DSML_RESULTDESC, resultCodeMsg);
		entry.setAttribute(ATTR_NAME_DSML_RESULTCODE, String.valueOf(ldapResult
				.getResultCodeEC()));
		entry.setAttribute(ATTR_NAME_DSML_ERROR_MESSAGE, ldapResult
				.getErrorMessage());
		entry.setAttribute(ATTR_NAME_DSML_OPERATION, aOperation);

		if (resultCodeMsg != null) {
			if (resultCodeMsg.equalsIgnoreCase("compareTrue")) {
				entry.setAttribute(ATTR_NAME_DSML_COMPARE_RESULT, "true");
			}
			if (resultCodeMsg.equalsIgnoreCase("compareFalse")) {
				entry.setAttribute(ATTR_NAME_DSML_COMPARE_RESULT, "false");
			}
		}

		addControlstoEntry(ldapResult, entry);
		addReqIDtoEntry(ldapResult, entry);
		addReferralstoEntry(ldapResult, entry);
		Trace.exitmax(this, "readLdapResult", entry);
		return entry;
	}

	/**
	 * Writes LdapResult
	 * 
	 * @param aMsgType
	 *            type of response
	 * @param aEntry
	 *            entry containing response
	 * @return LdapResult with response
	 * @exception Exception
	 *                Throws exception if parameters not set
	 */
	private LdapResult writeLdapResult(int aMsgType, Entry aEntry)
			throws Exception {
		Trace.entrymax(this, "writeLdapResult", "" + aMsgType, aEntry);
		Object exception = aEntry.getObject(ATTR_NAME_DSML_EXCEPTION);
		LdapResult ldapResult;
		if ((exception != null)
				&& !(exception.getClass().getName()
						.equals("java.lang.NullPointerException"))) {
			try {
				ldapResult = new LdapResult(aMsgType,
						(NamingException) exception);
			} catch (java.lang.ClassCastException classCastEx) {
				ldapResult = new LdapResult(aMsgType);
				ldapResult.setResultCode(1); // "operationsError" result code
				// message
				ldapResult.setErrorMessage(getErrorMessage(aEntry));
			}

		} else {
			ldapResult = new LdapResult(aMsgType);

			if (aEntry.getString(ATTR_NAME_DSML_RESULTCODE) != null) {
				ldapResult.setResultCode(Integer.parseInt(aEntry
						.getString(ATTR_NAME_DSML_RESULTCODE)));
			} else {
				ldapResult.setResultCode(0); // "success" by default
			}

			if (aEntry.getString(ATTR_NAME_DSML_COMPARE_RESULT) != null) {
				if (aEntry.getString(ATTR_NAME_DSML_COMPARE_RESULT).equals(
						"true")) {
					ldapResult.setResultCode(6); // compareTrue
				} else if (aEntry.getString(ATTR_NAME_DSML_COMPARE_RESULT)
						.equals("false")) {
					ldapResult.setResultCode(5); // compareFalse
				}
			}

			ldapResult.setErrorMessage(getErrorMessage(aEntry));
		}

		ldapResult.setMatchedDN(aEntry.getString(ATTR_NAME_DN));

		addControlstoMessage(aEntry, ldapResult);
		addReqIDtoMessage(aEntry, ldapResult);
		addReferralstoResult(aEntry, ldapResult);
		Trace.exitmax(this, "writeLdapResult", ldapResult);
		return ldapResult;
	}

	/**
	 * Reads search response
	 * 
	 * @param aBatchMessage
	 *            batch message containing response
	 * @return entry containing search response
	 */
	private Entry readSearchResp(BatchMessage aBatchMessage) {
		Trace.entrymax(this, "readSearchResp", aBatchMessage);
		Entry entry = new Entry();

		SearchResponse search = (SearchResponse) aBatchMessage;

		LdapResult resultDone = search.getSearchResultDone();
		Vector<?> resultEntries = search.getSearchResultEntry();

		entry.setAttribute(ATTR_NAME_DSML_OPERATION, OPERATION_SEARCH_RESPONSE);
		entry.setAttribute(ATTR_NAME_DSML_REQUEST_ID, search.getRequestID());
		entry.setAttribute(ATTR_NAME_DN, resultDone.getMatchedDN());

		Attribute attrResults = entry.newAttribute(ATTR_NAME_ACUMULATOR);
		for (int i = 0; i < resultEntries.size(); i++) {
			SearchResultEntry searchResultEntry = (SearchResultEntry) resultEntries
					.elementAt(i);
			Entry resultEntry = new Entry();
			resultEntry.setAttribute(ATTR_NAME_DN, searchResultEntry.getDN());
			entry.addAttributeValue(ATTR_NAME_DSML_BASE, searchResultEntry
					.getDN());
			Enumeration<?> attributes = searchResultEntry.getDsmlAttributes()
					.elements();

			while (attributes.hasMoreElements()) {
				DsmlAttr dsmlAttr = (DsmlAttr) attributes.nextElement();
				Enumeration<?> values = dsmlAttr.getValues().elements();

				Attribute attribute = resultEntry.newAttribute(dsmlAttr
						.getName());

				while (values.hasMoreElements()) {
					DsmlValue val = (DsmlValue) values.nextElement();
					attribute.addValue(val.getValue());
				}
			}

			try {
				base64decode(resultEntry);
			} catch (Exception ex) {
				logmsg(sResHash
						.getString("PARSER.DSML.ERROR.BASE64DEC.WARNING", entry
								.toString()));
			}

			attrResults.addValue(resultEntry);
		}

		entry.addAttributeValue(ATTR_NAME_DSML_ERROR_MESSAGE, resultDone
				.getErrorMessage());
		entry.addAttributeValue(ATTR_NAME_DSML_RESULTCODE, String
				.valueOf(resultDone.getResultCodeEC()));
		entry.addAttributeValue(ATTR_NAME_DSML_RESULTDESC, resultDone
				.getResultCodeMSG());

		addControlstoEntry(resultDone, entry);
		Trace.exitmax(this, "readSearchResp", entry);
		return entry;
	}

	/**
	 * Writes search response
	 * 
	 * @param aEntry
	 *            entry containing response
	 * 
	 * @return search response
	 * @exception Exception
	 *                Throws exception if parameters not set
	 */
	private SearchResponse writeSearchResp(Entry aEntry) throws Exception {
		Trace.entrymax(this, "writeSearchResp", aEntry);
		SearchResponse searchResponse = new SearchResponse();
		Attribute attrResults = aEntry.getAttribute(ATTR_NAME_ACUMULATOR);

		searchResponse
				.setRequestID(aEntry.getString(ATTR_NAME_DSML_REQUEST_ID));

		if (attrResults != null) {
			for (int i = 0; i < attrResults.size(); i++) {
				Entry resultEntry = (Entry) attrResults.getValue(i);

				if (!searchResponse
						.setSearchResultEntry(getSearchResultEntry(resultEntry))) {
					throw new Exception(
							sResHash
									.getString("PARSER.DSML.SEARCH.REQENTRY.NOTSET.ERROR"));
				}
			}
		}

		// Adds a searchResultDone at end of search response
		Object exception = aEntry.getObject(ATTR_NAME_DSML_EXCEPTION);
		LdapResult ldapResult;
		if ((exception != null)
				&& !(exception.getClass().getName()
						.equals("java.lang.NullPointerException"))) {
			try {
				ldapResult = new LdapResult(SEARCH_RESULT_DONE,
						(NamingException) exception);
			} catch (java.lang.ClassCastException classCastEx) {
				ldapResult = new LdapResult(SEARCH_RESULT_DONE);
				ldapResult.setResultCode(1); // "operationsError" result code
				// message
				ldapResult.setErrorMessage(getErrorMessage(aEntry));
			}
		} else {
			ldapResult = new LdapResult(SEARCH_RESULT_DONE);

			if (aEntry.getString(ATTR_NAME_DSML_RESULTCODE) != null) {
				ldapResult.setResultCode(Integer.parseInt(aEntry
						.getString(ATTR_NAME_DSML_RESULTCODE)));
			} else {
				ldapResult.setResultCode(0); // "success" by default
			}

			ldapResult.setErrorMessage(getErrorMessage(aEntry));
		}

		addControlstoMessage(aEntry, ldapResult);

		if (!searchResponse.setSearchResultDone(ldapResult)) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.SEARCH.RESDONE.NOTSET.ERROR"));
		}
		Trace.exitmax(this, "writeSearchResp", searchResponse);
		return searchResponse;
	}

	/**
	 * Reads extended response
	 * 
	 * @param aBatchMessage
	 *            batch message containing response
	 * 
	 * @return entry containing extended response
	 */
	private Entry readExtendedResp(BatchMessage aBatchMessage) {
		Trace.entrymax(this, "readExtendedResp", aBatchMessage);
		Entry entry = readLdapResult(aBatchMessage, OPERATION_EXTENDED_RESPONSE);

		ExtendedResponse extendResponse = (ExtendedResponse) aBatchMessage;

		entry.setAttribute(ATTR_NAME_DSML_RESPONSE_NAME, extendResponse
				.getResponseName());
		entry.setAttribute(ATTR_NAME_DSML_RESPONSE, extendResponse
				.getEncodedValue());
		Trace.exitmax(this, "readExtendedResp", entry);
		return entry;
	}

	/**
	 * Writes extended response
	 * 
	 * @param aEntry
	 *            entry containing response
	 * 
	 * @return extended response
	 * @exception Exception
	 *                Throws exception if parameters not set
	 */
	private ExtendedResponse writeExtendedResp(Entry aEntry) throws Exception {
		Trace.entrymax(this, "writeExtendedResp", aEntry);
		Object exception = aEntry.getObject(ATTR_NAME_DSML_EXCEPTION);
		ExtendedResponse extendResponse;
		if (exception != null) {
			try {
				extendResponse = new ExtendedResponse(
						(NamingException) exception);
			} catch (java.lang.ClassCastException classCastEx) {
				extendResponse = new ExtendedResponse();
				extendResponse.setResultCode(1); // "operationsError" result
				// code message
				extendResponse.setErrorMessage(getErrorMessage(aEntry));
			}
		} else {
			extendResponse = new ExtendedResponse();

			if (aEntry.getString(ATTR_NAME_DSML_RESULTCODE) != null) {
				extendResponse.setResultCode(Integer.parseInt(aEntry
						.getString(ATTR_NAME_DSML_RESULTCODE)));
			} else {
				extendResponse.setResultCode(0); // "success" by default
			}

			extendResponse.setErrorMessage(getErrorMessage(aEntry));
		}

		extendResponse.setMatchedDN(aEntry.getString(ATTR_NAME_DN));

		addControlstoMessage(aEntry, extendResponse);
		addReqIDtoMessage(aEntry, extendResponse);
		addReferralstoResult(aEntry, extendResponse);

		extendResponse.setResponseName(aEntry
				.getString(ATTR_NAME_DSML_RESPONSE_NAME));
		DsmlValue dsmlVal = new DsmlValue((byte[]) aEntry
				.getObject(ATTR_NAME_DSML_RESPONSE));
		extendResponse.setResponse(dsmlVal);
		Trace.exitmax(this, "writeExtendedResp", extendResponse);
		return extendResponse;
	}

	/**
	 * Reads error response
	 * 
	 * @param aBatchMessage
	 *            batch message containing response
	 * @return entry containing error response
	 */
	private Entry readErrorResp(BatchMessage aBatchMessage) {
		Trace.entrymax(this, "readErrorResp", aBatchMessage);
		Entry entry = new Entry();

		ErrorResponse errorResponse = (ErrorResponse) aBatchMessage;
		String[] details = errorResponse.getDetail();

		if (details != null) {
			for (int i = 0; i < details.length; i++) {
				entry.addAttributeValue(ATTR_NAME_DSML_DETAILS, details[i]);
			}
		}

		entry.setAttribute(ATTR_NAME_DSML_MESSAGE, errorResponse.getMessage());
		entry.setAttribute(ATTR_NAME_DSML_ERRORTYPE, errorResponse.getType());
		entry.setAttribute(ATTR_NAME_DSML_REQUEST_ID, errorResponse
				.getRequestID());
		entry.setAttribute(ATTR_NAME_DSML_OPERATION, OPERATION_ERROR_RESPONSE);
		Trace.exitmax(this, "readErrorResp", entry);
		return entry;
	}

	/**
	 * Writes error response
	 * 
	 * @param aEntry
	 *            entry containing response
	 * 
	 * @return error response
	 * @exception Exception
	 *                Throws exception if parameters not set
	 */
	private ErrorResponse writeErrorResp(Entry aEntry) throws Exception {
		Trace.entrymax(this, "writeErrorResp", aEntry);
		Object exception = aEntry.getObject(ATTR_NAME_DSML_EXCEPTION);
		ErrorResponse errorResponse;
		if (exception != null) {
			errorResponse = new ErrorResponse((Exception) exception);
		} else {
			errorResponse = new ErrorResponse();
			if (!errorResponse.setType(aEntry
					.getString(ATTR_NAME_DSML_ERRORTYPE))) {
				throw new Exception(sResHash
						.getString("PARSER.DSML.TYPE.NOTSET.ERROR"));
			}
		}

		if (aEntry.getAttribute(ATTR_NAME_DSML_DETAILS) != null) {
			Object[] valsObj = aEntry.getAttribute(ATTR_NAME_DSML_DETAILS)
					.getValues();
			String[] details = new String[valsObj.length];
			for (int i = 0; i < valsObj.length; i++) {
				details[i] = (String) valsObj[i];
			}
			errorResponse.setDetail(details);
		}

		errorResponse.setMessage(aEntry.getString(ATTR_NAME_DSML_MESSAGE));
		errorResponse.setRequestID(aEntry.getString(ATTR_NAME_DSML_REQUEST_ID));
		Trace.exitmax(this, "writeErrorResp", errorResponse);
		return errorResponse;
	}

	/**
	 * Resets Parser's parameters
	 */
	private void resetProperties() {
		mBinaryAttributes = new ArrayList<String>();

		mInputDoc = null;
		mOutputDoc = null;
		mToplevelInput = null;
		mDocumentBuilder = null;

		mIndentOutput = true;
		mOmitXmlDecl = false;
		mSoapBinding = false;
		mServerMode = true;
		mBatchRequest = null;
		mBatchResponse = null;
		mCharSet = null;

		mRequestsVector = null;
		mResponsesVector = null;
		mEntriesEnum = null;
	}

	/**
	 * Gives meaningful string representation of a XML node.
	 * 
	 * @param aNode
	 *            the Node object.
	 * @param aIndentOutput
	 *            if <code>true</code> the output will be indented.
	 * @return String object
	 * @throws Exception
	 *             If an unrecoverable error occurs during the course of the
	 *             transformation.
	 */
	public String xmlNodeToString(Node aNode, boolean aIndentOutput)
			throws Exception {

		StringWriter sw = new StringWriter();
		try {
			mTrans.transform(new DOMSource(aNode), new StreamResult(sw));
		} catch (Exception e) {
			throw new Exception(sResHash
					.getString("PARSER.DSML.TRASFORMING.DOM.TO.STRING.ERROR", e
							.toString()));
		}

		String nodeString = sw.toString();
		return nodeString;
	}

	/**
	 * Converts DSML message to Element.
	 * 
	 * @param aDsmlMessage
	 *            the DSML message.
	 * @return Element object
	 * @throws Exception
	 *             if a DocumentBuilder cannot be created which satisfies the
	 *             configuration requested.
	 */
	public static Element dsmlMessageToElement(DsmlMessage aDsmlMessage)
			throws Exception {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		dbf.setValidating(false);
		dbf.setNamespaceAware(true);
		documentBuilder = dbf.newDocumentBuilder();

		Document outputDoc = documentBuilder.newDocument();

		Element element = aDsmlMessage.toElement(outputDoc);
		return element;
	}

}
