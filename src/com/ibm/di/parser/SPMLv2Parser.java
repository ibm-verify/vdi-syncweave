/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.apache.xerces.impl.dv.util.Base64;
import org.openspml.v2.msg.Marshallable;
import org.openspml.v2.msg.OpenContentElement;
import org.openspml.v2.msg.XMLMarshaller;
import org.openspml.v2.msg.XMLUnmarshaller;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.AddResponse;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.DeleteResponse;
import org.openspml.v2.msg.spml.ErrorCode;
import org.openspml.v2.msg.spml.Extensible;
import org.openspml.v2.msg.spml.LookupRequest;
import org.openspml.v2.msg.spml.LookupResponse;
import org.openspml.v2.msg.spml.Modification;
import org.openspml.v2.msg.spml.ModificationMode;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spml.ModifyResponse;
import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.msg.spml.StatusCode;
import org.openspml.v2.msg.spmlsearch.Query;
import org.openspml.v2.msg.spmlsearch.Scope;
import org.openspml.v2.msg.spmlsearch.SearchQuery;
import org.openspml.v2.msg.spmlsearch.SearchRequest;
import org.openspml.v2.msg.spmlsearch.SearchResponse;
import org.openspml.v2.profiles.DSMLProfileRegistrar;
import org.openspml.v2.profiles.dsml.And;
import org.openspml.v2.profiles.dsml.ApproxMatch;
import org.openspml.v2.profiles.dsml.AttributeDescription;
import org.openspml.v2.profiles.dsml.AttributeDescriptions;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLModification;
import org.openspml.v2.profiles.dsml.DSMLProfileException;
import org.openspml.v2.profiles.dsml.DSMLValue;
import org.openspml.v2.profiles.dsml.EqualityMatch;
import org.openspml.v2.profiles.dsml.ExtensibleMatch;
import org.openspml.v2.profiles.dsml.Filter;
import org.openspml.v2.profiles.dsml.FilterItem;
import org.openspml.v2.profiles.dsml.FilterSet;
import org.openspml.v2.profiles.dsml.GreaterOrEqual;
import org.openspml.v2.profiles.dsml.LessOrEqual;
import org.openspml.v2.profiles.dsml.Not;
import org.openspml.v2.profiles.dsml.Or;
import org.openspml.v2.profiles.dsml.Present;
import org.openspml.v2.profiles.dsml.Substrings;
import org.openspml.v2.util.xml.ObjectFactory;
import org.openspml.v2.util.xml.ReflectiveDOMXMLUnmarshaller;
import org.openspml.v2.util.xml.ReflectiveXMLMarshaller;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.entry.NodeImpl;
import com.ibm.di.entry.Property;
import com.ibm.di.parser.xml.XMLParser2;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

/**
 * A TDI Parser class for reading and writing Entries in SPMLv2 DSMLv2 Profile
 * format.
 */
public class SPMLv2Parser extends XMLParser2 {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "spmlv2parser";

	/**
	 * <code>binaryAttributes</code> config parameter name.
	 */
	public static final String PARAMETER_BINARY_ATTRIBUTES = "binaryAttributes";

	/**
	 * Name of the SPML operation type attribute.
	 */
	public static final String ATTR_NAME_SPML_OP_TYPE = "spml.operation.type";

	/**
	 * <code>Request</code> type of the operation.
	 */
	public static final String OP_TYPE_REQUEST = "Request";

	/**
	 * <code>Response</code> type of the operation.
	 */
	public static final String OP_TYPE_RESPONSE = "Response";

	/**
	 * Name of the SPML operation attribute.
	 */
	public static final String ATTR_NAME_SPML_OPERATION = "spml.operation";

	/**
	 * <code>Add</code> operation.
	 */
	public static final String OPERATION_ADD = "Add";

	/**
	 * <code>Search</code> operation.
	 */
	public static final String OPERATION_SEARCH = "Search";

	/**
	 * <code>Modify</code> operation.
	 */
	public static final String OPERATION_MODIFY = "Modify";

	/**
	 * <code>Delete</code> operation.
	 */
	public static final String OPERATION_DELETE = "Delete";

	/**
	 * <code>Lookup</code> operation.
	 */
	public static final String OPERATION_LOOKUP = "Lookup";

	/**
	 * Name of the SPML containerID attribute.
	 */
	public static final String ATTR_NAME_CONTAINERID = "spml.containerID";

	/**
	 * Name of the SPML containerID.targetID attribute.
	 */
	public static final String ATTR_NAME_CONTAINERID_TARGETID = "spml.containerID.targetID";

	/**
	 * Name of the SPML scope attribute.
	 */
	public static final String ATTR_NAME_SEARCH_SCOPE = "spml.scope";

	/**
	 * Name of the SPML requestID attribute.
	 */
	public static final String ATTR_NAME_REQUESTID = "spml.requestID";

	/**
	 * Name of the SPML targetID attribute.
	 */
	public static final String ATTR_NAME_TARGETID = "spml.targetID";

	/**
	 * Name of the SPML resultEntries attribute.
	 */
	public static final String ATTR_NAME_RESULT_ENTRIES = "spml.resultEntries";

	/**
	 * Name of the SPML psoID attribute.
	 */
	public static final String ATTR_NAME_PSOID = "spml.psoID";

	/**
	 * Name of the SPML pso.targetID attribute.
	 */
	public static final String ATTR_NAME_PSO_TARGETID = "spml.pso.targetID";

	/**
	 * Name of the SPML status attribute.
	 */
	public static final String ATTR_NAME_STATUS = "spml.status";

	/**
	 * Name of the SPML errorMessages attribute.
	 */
	public static final String ATTR_NAME_ERROR_MESSAGES = "spml.errorMessages";

	/**
	 * Name of the SPML errorCode attribute.
	 */
	public static final String ATTR_NAME_ERROR_CODE = "spml.errorCode";

	/**
	 * Name of the SPML attributeDescription attribute.
	 */
	public static final String ATTR_NAME_ATTR_DESCRIPTIONS = "spml.attributeDescription";

	/**
	 * Name of the SPML filter.substrings.name attribute.
	 */
	public static final String ATTR_NAME_SUBSTRINGS_NAME = "spml.filter.substrings.name";

	/**
	 * Name of the SPML filter.substrings.initial attribute.
	 */
	public static final String ATTR_NAME_SUBSTRINGS_INITIAL = "spml.filter.substrings.initial";

	/**
	 * Name of the SPML filter.substrings.any attribute.
	 */
	public static final String ATTR_NAME_SUBSTRINGS_ANY = "spml.filter.substrings.any";

	/**
	 * Name of the SPML filter.substrings.final attribute.
	 */
	public static final String ATTR_NAME_SUBSTRINGS_FINAL = "spml.filter.substrings.final";

	/**
	 * Name of the SPML filter attribute.
	 * <p>
	 * Note: this is a hierarchical attribute.
	 */
	public static final String ATTR_NAME_FILTER = "spml.filter";

	/**
	 * Name of the XML attribute holding the name of the filter item.
	 */
	public static final String XML_ATTR_NAME = "name";

	/**
	 * Name of the XML attribute holding the dnAttributes value.
	 */
	public static final String XML_ATTR_DNATTR = "dnAttributes";

	/**
	 * Name of the XML attribute holding the matchingRule value.
	 */
	public static final String XML_ATTR_RULE = "matchingRule";

	/**
	 * Name of the XML attribute holding the value of the filter item.
	 */
	public static final String XML_TAG_VALUE = "value";

	/**
	 * Name of the XML tag containing the substrings filter item.
	 */
	public static final String XML_TAG_SUBSTRINGS = "substrings";

	/**
	 * Name of the XML tag containing the final filter item.
	 */
	public static final String XML_TAG_FINAL = "final";

	/**
	 * Name of the XML tag containing the initial filter items.
	 */
	public static final String XML_TAG_INITIAL = "initial";

	/**
	 * Name of the XML tag containing the any filter items.
	 */
	public static final String XML_TAG_ANY = "any";

	/**
	 * Name of the XML tag containing the equalityMatch filter items.
	 */
	public static final String XML_TAG_EQUALITYMATCH = "equalityMatch";

	/**
	 * Name of the XML tag containing the present filter items.
	 */
	public static final String XML_TAG_PRESENT = "present";

	/**
	 * Name of the XML tag containing the greaterOrEqual filter items.
	 */
	public static final String XML_TAG_GREATER = "greaterOrEqual";

	/**
	 * Name of the XML tag containing the lessOrEqual filter items.
	 */
	public static final String XML_TAG_LESS = "lessOrEqual";

	/**
	 * Name of the XML tag containing the extensibleMatch filter items.
	 */
	public static final String XML_TAG_EXTMATCH = "extensibleMatch";

	/**
	 * Name of the XML tag containing the approxMatch filter items.
	 */
	public static final String XML_TAG_APPROXMATCH = "approxMatch";

	/**
	 * Name of the XML tag containing the and filter items.
	 */
	public static final String XML_TAG_AND = "and";

	/**
	 * Name of the XML tag containing the or filter items.
	 */
	public static final String XML_TAG_OR = "or";

	/**
	 * Name of the XML tag containing the not filter items.
	 */
	public static final String XML_TAG_NOT = "not";

	/**
	 * Name of the spml. prefix attribute.
	 */
	private static final String ATTR_NAME_PREFIX_SPML = "spml.";

	/**
	 * {@link XMLUnmarshaller}
	 */
	XMLUnmarshaller unmarshaller = null;

	/**
	 * XMLMarshaller
	 */
	XMLMarshaller marshaller = null;

	/**
	 * {@link ArrayList} with the binary attributes.
	 */
	private ArrayList<String> binaryAttributes = null;

	/**
	 * Indicates whether a declaration should be added before writing.
	 */
	private boolean firstWrite = false;

	/**
	 * Request / Response operation type.
	 */
	private String writeType = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Initializes the Parser
	 * 
	 * @exception Exception
	 *                if the initialization fails
	 */

	public void initParser() throws Exception {

		marshaller = new ReflectiveXMLMarshaller();
		binaryAttributes = new ArrayList<String>();
		firstWrite = true;
		writeType = OP_TYPE_REQUEST;

		String binaryAttrsParam = getParam(PARAMETER_BINARY_ATTRIBUTES);
		if (binaryAttrsParam != null && binaryAttrsParam.length() > 0) {
			StringTokenizer tokenizer = new StringTokenizer(binaryAttrsParam,
					",");
			while (tokenizer.hasMoreTokens()) {
				binaryAttributes.add(tokenizer.nextToken().trim());
			}
		}

		setParam("xpath.expr", "/");

		unmarshaller = new ReflectiveDOMXMLUnmarshaller();
		ObjectFactory.getInstance().register(new DSMLProfileRegistrar());

		super.initParser();

	}

	/**
	 * {@inheritDoc}
	 */
	protected void initInput() throws Exception {

		StringBuilder xPathExpr = new StringBuilder();

		xPathExpr.append("addRequest | addResponse | ");
		xPathExpr.append("lookupRequest | lookupResponse | ");
		xPathExpr.append("modifyRequest | modifyResponse | ");
		xPathExpr.append("deleteRequest | deleteResponse | ");
		xPathExpr.append("searchRequest | searchResponse | ");
		xPathExpr.append("batchRequest/addRequest | ");
		xPathExpr.append("batchRequest/lookupRequest | ");
		xPathExpr.append("batchRequest/modifyRequest | ");
		xPathExpr.append("batchRequest/deleteRequest | ");
		xPathExpr.append("batchRequest/searchRequest | ");
		xPathExpr.append("batchResponse/addResponse | ");
		xPathExpr.append("batchResponse/lookupResponse |");
		xPathExpr.append("batchResponse/modifyResponse |");
		xPathExpr.append("batchResponse/deleteResponse |");
		xPathExpr.append("batchResponse/searchResponse");

		xPathStr = xPathExpr.toString();

		super.initInput();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initOutput() throws Exception {
		// the parser is writing directly to the stream so do not use the
		// XMLParser2
	}

	/**
	 * Reads a request or response from a batch message
	 * 
	 * @return the SPML message as Entry.
	 * @throws Exception
	 *             if the SPML could not be parsed correctly or any other error
	 *             occurs.
	 */
	public Entry readEntry() throws Exception {

		Entry res = super.readEntry();
		String xml = getCurrentEntryAsXMLString();

		if (res == null || xml.length() <= 0) {
			return null;
		}

		Marshallable next = unmarshaller.unmarshall(xml);

		Entry entry = null;
		if (next instanceof Request) {
			entry = readRequest((Request) next);
		} else if (next instanceof Response) {
			entry = readResponse((Response) next);
		} else {
			throw new Exception(sResHash.getString(
					"PARSER.SPMLV2.ERROR.UNKNOWN.TYPE", next.getClass()
							.getName()));
		}

		try {
			base64decode(entry);
		} catch (Exception ex) {
			logmsg(sResHash.getString("PARSER.SPMLV2.ERROR.BASE64DEC.WARNING",
					entry.toString()));
		}

		return entry;
	}

	/**
	 * Determines the type of the request and process it
	 * 
	 * @param aRequest
	 *            the request to be processed
	 * @return Entry TDI Entry that presents the result of parsing
	 * @throws Exception
	 *             if the request parsing fails or an unknown request is
	 *             processed
	 */
	private Entry readRequest(Request aRequest) throws Exception {

		Entry requestEntry = new Entry();

		Attribute operationType = requestEntry
				.newAttribute(ATTR_NAME_SPML_OP_TYPE);
		operationType.addValue(OP_TYPE_REQUEST);
		Attribute operation = requestEntry
				.newAttribute(ATTR_NAME_SPML_OPERATION);

		if (aRequest instanceof AddRequest) {

			AddRequest addRequest = (AddRequest) aRequest;
			operation.addValue(OPERATION_ADD);
			readAddRequest(addRequest, requestEntry);

		} else if (aRequest instanceof SearchRequest) {

			SearchRequest searchRequest = (SearchRequest) aRequest;
			operation.addValue(OPERATION_SEARCH);
			readSearchRequest(searchRequest, requestEntry);

		} else if (aRequest instanceof ModifyRequest) {

			ModifyRequest modifyRequest = (ModifyRequest) aRequest;
			operation.addValue(OPERATION_MODIFY);
			readModifyRequest(modifyRequest, requestEntry);

		} else if (aRequest instanceof DeleteRequest) {

			DeleteRequest delteRequest = (DeleteRequest) aRequest;
			operation.addValue(OPERATION_DELETE);
			readDeleteRequest(delteRequest, requestEntry);

		} else if (aRequest instanceof LookupRequest) {

			LookupRequest lookupRequest = (LookupRequest) aRequest;
			operation.addValue(OPERATION_LOOKUP);
			readLookupRequest(lookupRequest, requestEntry);

		} else {
			throw new Exception(sResHash.getString(
					"PARSER.SPMLV2.ERROR.UNKNOWN.REQUEST", aRequest.getClass()
							.getName()));
		}

		if (aRequest.getRequestID() != null) {
			requestEntry.newAttribute(ATTR_NAME_REQUESTID).addValue(
					aRequest.getRequestID());
		}

		return requestEntry;
	}

	/**
	 * Parses an add request
	 * 
	 * @param aAddRequest
	 *            the add request to be processed
	 * @param aEntry
	 *            TDI Entry that presents the result of parsing
	 * @throws Exception
	 *             if the request parsing fails
	 */
	private void readAddRequest(AddRequest aAddRequest, Entry aEntry)
			throws Exception {

		if (aAddRequest.getContainerID() != null) {
			aEntry.newAttribute(ATTR_NAME_CONTAINERID).addValue(
					aAddRequest.getContainerID().getID());
			if (aAddRequest.getContainerID().getTargetID() != null) {
				aEntry.newAttribute(ATTR_NAME_CONTAINERID_TARGETID).addValue(
						aAddRequest.getContainerID().getTargetID());
			}
		}

		if (aAddRequest.getTargetId() != null) {
			aEntry.newAttribute(ATTR_NAME_TARGETID).addValue(
					aAddRequest.getTargetId());
		}

		// the addRequest must contain a <data> element
		Extensible extData = aAddRequest.getData();
		if (extData != null) {
			OpenContentElement[] addReqDataElements = extData
					.getOpenContentElements();
			for (int i = 0; i < addReqDataElements.length; i++) {
				DSMLAttr dsmlAttr = (DSMLAttr) addReqDataElements[i];
				addDSMLAttrToEntry(dsmlAttr, aEntry);
			}
		}
	}

	/**
	 * Determines the type of the response and process it
	 * 
	 * @param aResponse
	 *            the response to be processed
	 * @return TDI Entry with attributes according to the content of the
	 *         response
	 * @throws Exception
	 *             if the response parsing fails or an unknown request is
	 *             processed
	 */
	private Entry readResponse(Response aResponse) throws Exception {

		Entry entry = new Entry();
		Attribute operationType = entry.newAttribute(ATTR_NAME_SPML_OP_TYPE);
		operationType.addValue(OP_TYPE_RESPONSE);
		Attribute operation = entry.newAttribute(ATTR_NAME_SPML_OPERATION);

		if (aResponse instanceof AddResponse) {

			AddResponse addResponse = (AddResponse) aResponse;
			operation.addValue(OPERATION_ADD);
			readAddResponse(addResponse, entry);

		} else if (aResponse instanceof SearchResponse) {

			SearchResponse searchResponse = (SearchResponse) aResponse;
			operation.addValue(OPERATION_SEARCH);
			readSearchResponse(searchResponse, entry);

		} else if (aResponse instanceof ModifyResponse) {

			ModifyResponse modifyResponse = (ModifyResponse) aResponse;
			operation.addValue(OPERATION_MODIFY);
			readModifyResponse(modifyResponse, entry);

		} else if (aResponse instanceof LookupResponse) {

			LookupResponse lookupResponse = (LookupResponse) aResponse;
			operation.addValue(OPERATION_LOOKUP);
			readLookupResponse(lookupResponse, entry);

		} else if (aResponse instanceof DeleteResponse) {

			DeleteResponse deleteResponse = (DeleteResponse) aResponse;
			operation.addValue(OPERATION_DELETE);
			readDeleteResponse(deleteResponse, entry);

		} else {
			throw new Exception(sResHash.getString(
					"PARSER.SPMLV2.ERROR.UNKNOWN.RESPONSE", aResponse
							.getClass().getName()));
		}

		if (aResponse.getRequestID() != null) {
			entry.newAttribute(ATTR_NAME_REQUESTID).addValue(
					aResponse.getRequestID());
		}
		if (aResponse.getStatus() != null) {
			entry.addAttributeValue(ATTR_NAME_STATUS, aResponse.getStatus());
		}
		// do we have errors?
		if (aResponse.getError() != null) {
			entry.newAttribute(ATTR_NAME_ERROR_CODE).addValue(
					aResponse.getError().toString());
		}
		if (aResponse.getErrorMessages() != null
				&& aResponse.getErrorMessages().length > 0) {
			entry.newAttribute(ATTR_NAME_ERROR_MESSAGES).addValue(
					aResponse.getErrorMessages());
		}

		return entry;
	}

	/**
	 * Parses an add response
	 * 
	 * @param aAddResponse
	 *            the add response to be processed
	 * @param aEntry
	 *            TDI Entry that presents the result of parsing
	 * @throws Exception
	 *             if the response parsing fails
	 */
	private void readAddResponse(AddResponse aAddResponse, Entry aEntry)
			throws Exception {

		if (aAddResponse.getPso() != null) {

			// get the psoID
			if (aAddResponse.getPso().getPsoID() != null) {
				if (aAddResponse.getPso().getPsoID().getID() != null) {
					aEntry.newAttribute(ATTR_NAME_PSOID).addValue(
							aAddResponse.getPso().getPsoID().getID());
				}
				// get the pso targetID attribute, if it is present
				if (aAddResponse.getPso().getPsoID().getTargetID() != null) {
					aEntry.newAttribute(ATTR_NAME_PSO_TARGETID).addValue(
							aAddResponse.getPso().getPsoID().getTargetID());
				}
			}

			// get the open content elements of the <data> element
			if (aAddResponse.getPso().getData() != null) {
				Extensible extData = aAddResponse.getPso().getData();
				OpenContentElement[] ocDataEls = extData
						.getOpenContentElements();
				for (int i = 0; i < ocDataEls.length; i++) {
					DSMLAttr dsmlAttr = (DSMLAttr) ocDataEls[i];
					addDSMLAttrToEntry(dsmlAttr, aEntry);
				}
			}
		}
	}

	/**
	 * Parses a search response
	 * 
	 * @param aSearchResponse
	 *            the response to be processed
	 * @param aEntry
	 *            TDI Entry, containing other Entry object that presents the
	 *            result from the parse
	 */
	private void readSearchResponse(SearchResponse aSearchResponse, Entry aEntry) {

		PSO[] psos = aSearchResponse.getPSOs();
		Attribute resultEntries = aEntry.newAttribute(ATTR_NAME_RESULT_ENTRIES);
		for (int i = 0; i < psos.length; i++) {

			Entry result = new Entry();
			Extensible ext = psos[i].getData();
			if (ext != null) {
				OpenContentElement[] oce = ext.getOpenContentElements();
				result = oceToEntry(oce);
			}
			if (psos[i].getPsoID() != null) {
				if (psos[i].getPsoID().getID() != null) {
					result.newAttribute(ATTR_NAME_PSOID).addValue(
							psos[i].getPsoID().getID());
				}
				// get the pso targetID attribute, if it is present
				if (psos[i].getPsoID().getTargetID() != null) {
					result.newAttribute(ATTR_NAME_PSO_TARGETID).addValue(
							psos[i].getPsoID().getTargetID());
				}
			}
			resultEntries.addValue(result);
		}
	}

	/**
	 * Parses a modify response
	 * 
	 * @param aModifyResponse
	 *            the response to be processed
	 * @param aEntry
	 *            TDI Entry that presents the result of parsing
	 * @throws Exception
	 *             if the response parsing fails
	 */
	private void readModifyResponse(ModifyResponse aModifyResponse, Entry aEntry)
			throws Exception {

		if (aModifyResponse.getPso() != null) {
			aEntry.newAttribute(ATTR_NAME_PSOID).addValue(
					aModifyResponse.getPso().getPsoID().getID());
			if (aModifyResponse.getPso().getPsoID().getTargetID() != null) {
				aEntry.newAttribute(ATTR_NAME_PSO_TARGETID).addValue(
						aModifyResponse.getPso().getPsoID().getTargetID());
			}
		}
	}

	/**
	 * Parses a delete response
	 * 
	 * @param aDeleteResponse
	 *            the response to be processed
	 * @param aEntry
	 *            TDI Entry that contains the result of parsing
	 * @throws Exception
	 *             if the response parsing fails
	 */
	private void readDeleteResponse(DeleteResponse aDeleteResponse, Entry aEntry)
			throws Exception {
		// nothing specific at the moment to process here
	}

	/**
	 * Parses a lookup response
	 * 
	 * @param aLookupResponse
	 *            the response to be processed
	 * @param aEntry
	 *            TDI Entry that presents the result of parsing
	 */
	private void readLookupResponse(LookupResponse aLookupResponse, Entry aEntry) {

		if (aLookupResponse.getPso() != null) {
			// get the psoID
			if (aLookupResponse.getPso().getPsoID() != null) {
				if (aLookupResponse.getPso().getPsoID().getID() != null) {
					aEntry.newAttribute(ATTR_NAME_PSOID).addValue(
							aLookupResponse.getPso().getPsoID().getID());
				}
				// get the pso targetID attribute, if it is present
				if (aLookupResponse.getPso().getPsoID().getTargetID() != null) {
					aEntry.newAttribute(ATTR_NAME_PSO_TARGETID).addValue(
							aLookupResponse.getPso().getPsoID().getTargetID());
				}
			}
			// get the open content elements of the <data> element
			if (aLookupResponse.getPso().getData() != null) {
				Extensible extData = aLookupResponse.getPso().getData();
				OpenContentElement[] ocDataEls = extData
						.getOpenContentElements();
				for (int i = 0; i < ocDataEls.length; i++) {
					DSMLAttr dsmlAttr = (DSMLAttr) ocDataEls[i];
					for (int j = 0; j < dsmlAttr.getValues().length; j++) {
						aEntry.addAttributeValue(dsmlAttr.getName(), (dsmlAttr
								.getValues()[j]).getValue());
					}
				}
			}
		}
	}

	/**
	 * Parses a search request
	 * 
	 * @param aSearchRequest
	 *            the request to be processed
	 * @param aEntry
	 *            TDI Entry that presents the result of parsing
	 */
	private void readSearchRequest(SearchRequest aSearchRequest, Entry aEntry) {

		SearchQuery query = aSearchRequest.getQuery();

		aEntry.addAttributeValue(ATTR_NAME_SEARCH_SCOPE, query.getScope());
		if (query.getBasePsoID() != null) {
			aEntry.addAttributeValue(ATTR_NAME_CONTAINERID, query
					.getBasePsoID().getID());
			aEntry.addAttributeValue(ATTR_NAME_CONTAINERID_TARGETID, query
					.getBasePsoID().getTargetID());
		}

		OpenContentElement[] oce = query.getOpenContentElements();
		if ((oce.length > 0) && (oce[0] instanceof Filter)) {
			Filter filter = (Filter) oce[0];
			if (filter.getItem() instanceof Substrings) {
				// for backward compatibility
				Substrings ss = (Substrings) filter.getItem();
				aEntry.addAttributeValue(ATTR_NAME_SUBSTRINGS_NAME, ss
						.getName());
				if (ss.getInitial() != null) {
					aEntry.addAttributeValue(ATTR_NAME_SUBSTRINGS_INITIAL, ss
							.getInitial().getValue());
				}
				if (ss.getFinal() != null) {
					aEntry.addAttributeValue(ATTR_NAME_SUBSTRINGS_FINAL, ss
							.getFinal().getValue());
				}
				if (ss.getAny() != null && ss.getAny().length > 0) {
					DSMLValue[] dsmlValues = ss.getAny();
					Attribute attrAny = aEntry
							.newAttribute(ATTR_NAME_SUBSTRINGS_ANY);
					for (int i = 0; i < dsmlValues.length; i++) {
						attrAny.addValue(dsmlValues[i].getValue());
					}
				}
			} else if (filter.getItem() != null) {
				FilterItem fi = filter.getItem();
				parseFilterItem(aEntry.newAttribute(ATTR_NAME_FILTER), fi);
			}
		}

		// OCE elements are fixed and second element
		// holds always the Attribute Descriptions
		if ((oce.length > 1) && (oce[1] instanceof AttributeDescriptions)) {
			org.openspml.v2.profiles.dsml.AttributeDescription[] aDs = ((AttributeDescriptions) oce[1])
					.getAttributeDescriptions();
			for (int i = 0; i < aDs.length; i++) {
				aEntry.addAttributeValue(ATTR_NAME_ATTR_DESCRIPTIONS, aDs[i]
						.getName());
			}

		}
	}

	/**
	 * This method decodes base64 encoded value of specified attribute.
	 * 
	 * @param attrName
	 *            name of the Attribute
	 * @param attrValue
	 *            String representation of the attribute value
	 * @return decoded value if <code>attrName</code> is binary attribute;
	 *         otherwise <code>attrValue</code> as String;
	 */
	private Object base64decodeValue(String attrName, String attrValue) {
		if (binaryAttributes.contains(attrName)) {
			if (debugMode()) {
				debug(sResHash.getString(
						"PARSER.SPMLV2.BASE64.DECODINGATTRIB.INFO", attrName));
			}

			byte[] binaryValue = Base64.decode(attrValue);
			if (binaryValue == null) {
				logmsg(sResHash.getString("PARSER.SPMLV2.BINNOT64ENCODED.INFO",
						attrName));
			}
			return binaryValue;
		}
		return attrValue;
	}

	/**
	 * Creates Attribute object with specified name, assigns one or more values
	 * that are decoded if needed and appends the created Attribute to the
	 * provided parent Attribute.
	 * 
	 * @param tagName
	 *            node name of the Attribute object which will be created
	 * @param parent
	 *            parent Attribute object
	 * @param attrName
	 *            name of the attribute as specified in the XML file
	 * @param val
	 *            array of DSMLValue representing the value of the Attribute;
	 *            this could be the value of the &lt;value&gt; tag or an array
	 *            containing the values of all &lt;any&gt; tags inside the
	 *            &lt;substring&gt; tag;
	 */
	private void appendAttribute(String tagName, Attribute parent,
			String attrName, DSMLValue[] val) {
		Attribute valueTag = new Attribute(tagName);
		for (int i = 0; i < val.length; i++) {
			if (val[i] != null) {
				Object value = base64decodeValue(attrName, val[i].toString());
				valueTag.addValue(value);
				parent.appendChild(valueTag);
			}
		}
	}

	/**
	 * Creates Attribute object named 'value', assigns a value to it and appends
	 * the created Attribute to the provided parent Attribute object.
	 * 
	 * @param parent
	 *            parent Attribute object
	 * @param attrName
	 *            name of attribute
	 * @param val
	 *            value of the attribute
	 * @see #appendAttribute(String, NodeImpl, String, DSMLValue[])
	 */
	private void appendValueAttribute(Attribute parent, String attrName,
			DSMLValue val) {
		DSMLValue[] values = { val };
		appendAttribute(XML_TAG_VALUE, parent, attrName, values);
	}

	/**
	 * Creates a hierarchical Attribute from a specified FilterItem object. The
	 * <code>item</code> could represent one of these XML elements:<br>
	 * 
	 * <li>&lt;not&gt; - negative of contained filter item;</li><br>
	 * <li>&lt;and&gt; - logical 'and' containing several filter items</li><br>
	 * <li>&lt;or&gt; - logical 'or' containing several filter items</li><br>
	 * <li>&lt;substrings&gt; - criterion for searching inside attributes</li><br>
	 * <li>&lt;equalityMatch - filter item indicating equal match</li><br>
	 * <li>&lt;approxMatch - filter item indicating approximate match</li><br>
	 * <li>&lt;extensibleMatch - filter item indicating extensible match</li><br>
	 * <li>&lt;greaterOrEqual - filter item indicating match if greater or equal
	 * </li><br>
	 * <li>&lt;lessOrEqual - filter item indicating match if less or equal</li><br>
	 * <li>&lt;present - filter item indicating presence of attribute</li><br>
	 * 
	 * @param parent
	 *            the parent node to which we are going to append created
	 *            Attribute
	 * @param item
	 *            FilterItem object to parse into Attribute object
	 * @see #getFilterItem(Attribute)
	 */
	private void parseFilterItem(NodeImpl parent, FilterItem item) {
		Attribute newChild = new Attribute();
		Attribute nameAttr = new Attribute(XML_ATTR_NAME);

		if (item instanceof Not) {
			Not n = (Not) item;
			newChild.setName(XML_TAG_NOT);
			// Not can contain only one attribute or filter item.
			parseFilterItem(newChild, n.getItem());
		} else if (item instanceof FilterSet) {
			String name = null;
			if (item instanceof And) {
				name = XML_TAG_AND;
			} else if (item instanceof Or) {
				name = XML_TAG_OR;
			}
			newChild.setName(name);
			FilterItem[] items = ((FilterSet) item).getItems();
			for (int i = 0; i < items.length; i++)
				parseFilterItem(newChild, items[i]);
		} else if (item instanceof Substrings) {
			Substrings ss = (Substrings) item;
			String name = ss.getName();
			newChild.setName(XML_TAG_SUBSTRINGS);
			nameAttr.setValue(name);
			newChild.appendChild(nameAttr);
			if (ss.getFinal() != null) {
				Attribute finalTag = new Attribute(XML_TAG_FINAL);
				finalTag.setValue(base64decodeValue(name, ss.getFinal()
						.getValue()));
				newChild.appendChild(finalTag);
			}
			if (ss.getInitial() != null) {
				Attribute initialTag = new Attribute(XML_TAG_INITIAL);
				initialTag.setValue(base64decodeValue(name, ss.getInitial()
						.getValue()));
				newChild.appendChild(initialTag);
			}
			if (ss.getAny() != null && ss.getAny().length > 0) {
				appendAttribute(name, newChild, XML_TAG_ANY, ss.getAny());
			}
		} else if (item instanceof EqualityMatch) {
			EqualityMatch em = (EqualityMatch) item;
			newChild.setName(XML_TAG_EQUALITYMATCH);
			nameAttr.setValue(em.getName());
			newChild.appendChild(nameAttr);
			appendValueAttribute(newChild, em.getName(), em.getValue());
		} else if (item instanceof GreaterOrEqual) {
			GreaterOrEqual ge = (GreaterOrEqual) item;
			newChild.setName(XML_TAG_GREATER);
			nameAttr.setValue(ge.getName());
			newChild.appendChild(nameAttr);
			appendValueAttribute(newChild, ge.getName(), ge.getValue());
		} else if (item instanceof LessOrEqual) {
			LessOrEqual le = (LessOrEqual) item;
			newChild.setName(XML_TAG_LESS);
			nameAttr.setValue(le.getName());
			newChild.appendChild(nameAttr);
			appendValueAttribute(newChild, le.getName(), le.getValue());
		} else if (item instanceof ApproxMatch) {
			ApproxMatch am = (ApproxMatch) item;
			newChild.setName(XML_TAG_APPROXMATCH);
			nameAttr.setValue(am.getName());
			newChild.appendChild(nameAttr);
			appendValueAttribute(newChild, am.getName(), am.getValue());
		} else if (item instanceof ExtensibleMatch) {
			ExtensibleMatch em = (ExtensibleMatch) item;
			newChild.setName(XML_TAG_EXTMATCH);
			nameAttr.setValue(em.getName());
			newChild.appendChild(nameAttr);
			appendValueAttribute(newChild, em.getName(), em.getValue());

			Attribute dnAttr = new Attribute(XML_ATTR_DNATTR);
			dnAttr.setValue(em.getDnAttributes());
			newChild.appendChild(dnAttr);

			if (em.getMatchingRule() != null
					&& em.getMatchingRule().length() > 0) {
				Attribute rule = new Attribute(XML_ATTR_RULE);
				rule.setValue(em.getMatchingRule());
				newChild.appendChild(rule);
			}

		} else if (item instanceof Present) {
			Present p = (Present) item;
			newChild = new Attribute(XML_TAG_PRESENT);
			nameAttr.setValue(p.getName());
			newChild.appendChild(nameAttr);
		}

		parent.appendChild(newChild);
	}

	/**
	 * Adds given open content elements to the TDI Entry
	 * 
	 * @param aOCE
	 *            array of open content elements that are to be added to the TDI
	 *            Entry
	 * @return TDI Entry with attributes corresponding to the open content
	 *         elements
	 */
	private Entry oceToEntry(OpenContentElement[] aOCE) {
		Entry result = new Entry();
		for (int i = 0; i < aOCE.length; i++) {
			String name = ((DSMLAttr) aOCE[i]).getName();
			Attribute attr = result.newAttribute(name);
			DSMLValue[] values = ((DSMLAttr) aOCE[i]).getValues();
			for (int j = 0; j < values.length; j++) {
				attr.addValue(values[j].getValue());
			}
		}

		try {
			base64decode(result);
		} catch (Exception ex) {
			logmsg(sResHash.getString("PARSER.SPMLV2.ERROR.BASE64DEC.WARNING",
					result.toString()));
		}

		return result;
	}

	/**
	 * Parses a modify request
	 * 
	 * @param aModifyRequest
	 *            the request to be processed
	 * @param aEntry
	 *            TDI Entry that presents the result of parsing
	 * @throws Exception
	 *             if the request parsing fails
	 */
	private void readModifyRequest(ModifyRequest aModifyRequest, Entry aEntry)
			throws Exception {

		if (aModifyRequest.getPsoID() == null) {
			throw new Exception(sResHash
					.getString("PARSER.SPMLV2.ERROR.REQUEST.PSO.MISSING",
							OPERATION_MODIFY));
		} else {
			if (aModifyRequest.getPsoID().getID() == null) {
				throw new Exception(sResHash.getString(
						"PARSER.SPMLV2.ERROR.REQUEST.PSO.ID.MISSING",
						OPERATION_MODIFY));
			}
			aEntry.newAttribute(ATTR_NAME_PSOID).addValue(
					aModifyRequest.getPsoID().getID());
			if (aModifyRequest.getPsoID().getTargetID() != null) {
				aEntry.newAttribute(ATTR_NAME_PSO_TARGETID).addValue(
						aModifyRequest.getPsoID().getTargetID());
			}
		}

		Modification[] mods = aModifyRequest.getModifications();
		if (mods != null && mods.length > 0) {
			for (int i = 0; i < mods.length; i++) {
				OpenContentElement[] oce = mods[i].getOpenContentElements();
				for (int j = 0; j < oce.length; j++) {
					DSMLModification mod = (DSMLModification) oce[j];
					com.ibm.di.entry.Attribute a = aEntry.newAttribute(mod
							.getName());
					if (mod.getValues().length > 0) {
						for (int k = 0; k < mod.getValues().length; k++) {
							a.addValue(mod.getValues()[k].getValue());
						}
					}
					if (mod.getOperation() != null) {
						a.setOperation(mod.getOperation().toString());
					}
				}
			}
		}
	}

	/**
	 * Parses a delete request
	 * 
	 * @param aDeleteRequest
	 *            the request to be processed
	 * @param aEntry
	 *            TDI Entry that presents the result of parsing
	 * @throws Exception
	 *             if the request parsing fails
	 */
	private void readDeleteRequest(DeleteRequest aDeleteRequest, Entry aEntry)
			throws Exception {

		if (aDeleteRequest.getPsoID() == null) {
			throw new Exception(sResHash
					.getString("PARSER.SPMLV2.ERROR.REQUEST.PSO.MISSING",
							OPERATION_DELETE));
		} else {
			if (aDeleteRequest.getPsoID().getID() == null) {
				throw new Exception(sResHash.getString(
						"PARSER.SPMLV2.ERROR.REQUEST.PSO.ID.MISSING",
						OPERATION_DELETE));
			}
			aEntry.newAttribute(ATTR_NAME_PSOID).addValue(
					aDeleteRequest.getPsoID().getID());
			if (aDeleteRequest.getPsoID().getTargetID() != null) {
				aEntry.newAttribute(ATTR_NAME_PSO_TARGETID).addValue(
						aDeleteRequest.getPsoID().getTargetID());
			}
		}
	}

	/**
	 * Parses the lookup request
	 * 
	 * @param aLookupRequest
	 *            the request to be processed
	 * @param aEntry
	 *            TDI Entry that presents the result of parsing
	 * @throws Exception
	 *             if the request parsing fails
	 */
	private void readLookupRequest(LookupRequest aLookupRequest, Entry aEntry)
			throws Exception {
		if (aLookupRequest.getPsoID() == null) {
			throw new Exception(sResHash
					.getString("PARSER.SPMLV2.ERROR.REQUEST.PSO.MISSING",
							OPERATION_LOOKUP));
		} else {
			if (aLookupRequest.getPsoID().getID() == null) {
				throw new Exception(sResHash.getString(
						"PARSER.SPMLV2.ERROR.REQUEST.PSO.ID.MISSING",
						OPERATION_LOOKUP));
			}
			aEntry.newAttribute(ATTR_NAME_PSOID).addValue(
					aLookupRequest.getPsoID().getID());
			if (aLookupRequest.getPsoID().getTargetID() != null) {
				aEntry.newAttribute(ATTR_NAME_PSO_TARGETID).addValue(
						aLookupRequest.getPsoID().getTargetID());
			}
		}
	}

	/**
	 * Writes down a request or response to a batch message
	 * 
	 * @param aEntry
	 *            TDI Entry that presents the request/response that is to be
	 *            written to a batch message
	 * @throws Exception
	 *             if some attributes are missing.
	 */
	public void writeEntry(Entry aEntry) throws Exception {

		if (aEntry.getAttribute(ATTR_NAME_SPML_OPERATION) == null) {
			throw new Exception(sResHash.getString(
					"PARSER.SPMLV2.ERROR.MISSING.ATTRIBUTE",
					ATTR_NAME_SPML_OPERATION));
		}
		if (aEntry.getAttribute(ATTR_NAME_SPML_OP_TYPE) == null) {
			throw new Exception(sResHash
					.getString("PARSER.SPMLV2.ERROR.MISSING.ATTRIBUTE"
							+ ATTR_NAME_SPML_OP_TYPE));
		}
		String operationType = aEntry.getAttribute(ATTR_NAME_SPML_OP_TYPE)
				.getValue();
		if (operationType.equalsIgnoreCase(OP_TYPE_REQUEST)) {
			writeType = OP_TYPE_REQUEST;
			writeRequest(aEntry);
		} else if (operationType.equalsIgnoreCase(OP_TYPE_RESPONSE)) {
			writeType = OP_TYPE_RESPONSE;
			writeResponse(aEntry);
		} else {
			throw new Exception(sResHash.getString(
					"PARSER.SPMLV2.ERROR.UNKNOWN.OPERATION", operationType));
		}
	}

	/**
	 * Decides what the type of the request is and writes it down to a batch
	 * message
	 * 
	 * @param aEntry
	 *            TDI Entry that presents the request
	 * @throws Exception
	 *             if the writing of a request fails or the operation is of an
	 *             unknown type
	 */
	private void writeRequest(Entry aEntry) throws Exception {

		String operation = aEntry.getAttribute(ATTR_NAME_SPML_OPERATION)
				.getValue();
		if (operation.equalsIgnoreCase(OPERATION_ADD)) {

			writeAddRequest(aEntry);

		} else if (operation.equalsIgnoreCase(OPERATION_MODIFY)) {

			writeModifyRequest(aEntry);

		} else if (operation.equalsIgnoreCase(OPERATION_DELETE)) {

			writeDeleteRequest(aEntry);

		} else if (operation.equalsIgnoreCase(OPERATION_LOOKUP)) {

			writeLookupRequest(aEntry);

		} else if (operation.equalsIgnoreCase(OPERATION_SEARCH)) {

			writeSearchRequest(aEntry);

		} else {
			throw new Exception(sResHash.getString(
					"PARSER.SPMLV2.ERROR.UNKNOWN.REQUEST", operation));
		}

	}

	/**
	 * Writes down a delete request to a batch message
	 * 
	 * @param aEntry
	 *            TDI Entry that presents the request
	 */
	private void writeDeleteRequest(Entry aEntry) throws Exception {

		DeleteRequest deleteRequest = new DeleteRequest();
		// add containerID
		PSOIdentifier psoID = new PSOIdentifier();
		Attribute containerIDAttr = aEntry.getAttribute(ATTR_NAME_PSOID);
		if (containerIDAttr != null) {
			String containerIDValue = containerIDAttr.getValue();
			if (containerIDValue != null) {
				psoID.setID(containerIDValue);
			}
		}
		// add targetID
		Attribute targetID = aEntry.getAttribute(ATTR_NAME_PSO_TARGETID);
		if (targetID != null) {
			String targetIDValue = targetID.getValue();
			if (targetIDValue != null) {
				psoID.setTargetID(targetIDValue);
			}
		}
		deleteRequest.setPsoID(psoID);

		writeXML(deleteRequest);
	}

	/**
	 * Writes down an add request to a batch message
	 * 
	 * @param aEntry
	 *            TDI Entry that presents the request
	 * @throws Exception
	 *             if writing of the request fails
	 */
	private void writeAddRequest(Entry aEntry) throws Exception {

		AddRequest addRequest = new AddRequest();
		// add containerID
		PSOIdentifier containerID = new PSOIdentifier();
		Attribute containerIDAttr = aEntry.getAttribute(ATTR_NAME_CONTAINERID);
		if (containerIDAttr != null) {
			String containerIDValue = containerIDAttr.getValue();
			if (containerIDValue != null) {
				containerID.setID(containerIDValue);
			}
		}
		// add targetID
		Attribute targetID = aEntry
				.getAttribute(ATTR_NAME_CONTAINERID_TARGETID);
		if (targetID != null) {
			String targetIDValue = targetID.getValue();
			if (targetIDValue != null) {
				containerID.setTargetID(targetIDValue);
			}
		}
		addRequest.setContainerID(containerID);

		if (aEntry.getAttribute(ATTR_NAME_REQUESTID) != null) {
			addRequest.setRequestID(aEntry.getAttribute(ATTR_NAME_REQUESTID)
					.getValue());
		}

		// add open content to the data element
		Extensible data = new Extensible();
		// add dsml:attr attribute
		// skip the attributes whose name starts with spml.
		String[] attributeNames = aEntry.getAttributeNames();
		for (int i = 0; i < attributeNames.length; i++) {
			if (!attributeNames[i].startsWith(ATTR_NAME_PREFIX_SPML)) {
				Attribute attr = aEntry.getAttribute(attributeNames[i]);
				data.addOpenContentElement(attrToDSMLAttr(attr));
			}
		}
		addRequest.setData(data);

		writeXML(addRequest);
	}

	/**
	 * Writes down a lookup request to a batch message
	 * 
	 * @param aEntry
	 *            TDI Entry that presents the request
	 * @throws Exception
	 *             if the writing of the request fails
	 */
	private void writeLookupRequest(Entry aEntry) throws Exception {

		LookupRequest lookupRequest = new LookupRequest();
		// add containerID
		PSOIdentifier psoID = new PSOIdentifier();
		Attribute containerIDAttr = aEntry.getAttribute(ATTR_NAME_PSOID);
		if (containerIDAttr != null) {
			String containerIDValue = containerIDAttr.getValue();
			if (containerIDValue != null) {
				psoID.setID(containerIDValue);
			}
		}
		// add targetID
		Attribute targetID = aEntry.getAttribute(ATTR_NAME_PSO_TARGETID);
		if (targetID != null) {
			String targetIDValue = targetID.getValue();
			if (targetIDValue != null) {
				psoID.setTargetID(targetIDValue);
			}
		}
		lookupRequest.setPsoID(psoID);
		writeXML(lookupRequest);
	}

	/**
	 * Writes down a search request to a batch message
	 * 
	 * @param aEntry
	 *            TDI Entry that presents the request
	 * @throws Exception
	 *             if the writing of the request fails
	 */
	private void writeSearchRequest(Entry aEntry) throws Exception {

		SearchRequest searchRequest = new SearchRequest();
		Query query = new Query();

		// add containerID
		PSOIdentifier containerID = new PSOIdentifier();
		Attribute containerIDAttr = aEntry.getAttribute(ATTR_NAME_CONTAINERID);
		if (containerIDAttr != null) {
			String containerIDValue = containerIDAttr.getValue();
			if (containerIDValue != null) {
				containerID.setID(containerIDValue);
			}
		}
		// add targetID
		Attribute targetID = aEntry
				.getAttribute(ATTR_NAME_CONTAINERID_TARGETID);
		if (targetID != null) {
			String targetIDValue = targetID.getValue();
			if (targetIDValue != null) {
				containerID.setTargetID(targetIDValue);
			}
		}

		query.setBasePsoID(containerID);

		// add scope
		Attribute scope = aEntry.getAttribute(ATTR_NAME_SEARCH_SCOPE);
		if (scope != null) {
			String scopeValue = scope.getValue();
			if (scopeValue != null) {
				query.setScope((Scope) Scope.getConstant(Scope.class,
						scopeValue));
			}
		}

		// add filter and attribute descriptions as OCE !
		OpenContentElement[] oce = new OpenContentElement[2];
		Filter filter = new Filter();

		// if no spml.filter specified keep backward compatibility
		if (aEntry.getAttribute(ATTR_NAME_FILTER) == null) {
			Substrings substrings = null;
			// since name is required no need to create Substrings object
			// if name is not provided
			if (aEntry.getAttribute(ATTR_NAME_SUBSTRINGS_NAME) != null) {
				String substringsName = aEntry.getAttribute(
						ATTR_NAME_SUBSTRINGS_NAME).getValue();
				substrings = new Substrings(substringsName);

				if (aEntry.getAttribute(ATTR_NAME_SUBSTRINGS_INITIAL) != null) {
					substrings.setInitial(new DSMLValue(aEntry.getAttribute(
							ATTR_NAME_SUBSTRINGS_INITIAL).getValue()));
				}
				if (aEntry.getAttribute(ATTR_NAME_SUBSTRINGS_FINAL) != null) {
					substrings.setFinal(new DSMLValue(aEntry.getAttribute(
							ATTR_NAME_SUBSTRINGS_FINAL).getValue()));
				}
				if (aEntry.getAttribute(ATTR_NAME_SUBSTRINGS_ANY) != null) {
					Attribute attrAny = aEntry
							.getAttribute(ATTR_NAME_SUBSTRINGS_ANY);
					DSMLValue[] dsmlValues = new DSMLValue[attrAny.size()];
					for (int i = 0; i < attrAny.size(); i++) {
						dsmlValues[i] = new DSMLValue(attrAny.getValue(i)
								.toString());
					}
					substrings.setAny(dsmlValues);
				}
			}
			if (substrings != null)
				filter.setItem(substrings);
		} else {
			// create a FilterItem representing the 'spml.filter' attribute
			Attribute filterItemAttr = getFirstChildNode((Attribute) (aEntry
					.getAttribute(ATTR_NAME_FILTER)));
			FilterItem item = getFilterItem(filterItemAttr);
			if (item != null)
				filter.setItem(item);
		}

		oce[0] = filter;

		AttributeDescriptions attrDescriptions = new AttributeDescriptions();
		oce[1] = attrDescriptions;
		Attribute attrDescriptionsAttr = aEntry
				.getAttribute(ATTR_NAME_ATTR_DESCRIPTIONS);
		if (attrDescriptionsAttr != null) {
			for (int i = 0; i < attrDescriptionsAttr.size(); i++) {
				AttributeDescription attDescription = new AttributeDescription(
						attrDescriptionsAttr.getValue(i).toString());
				attrDescriptions.addAttributeDescription(attDescription);
			}
		}
		query.setOpenContentElements(oce);
		searchRequest.setQuery(query);

		if (aEntry.getAttribute(ATTR_NAME_REQUESTID) != null) {
			searchRequest.setRequestID(aEntry.getAttribute(ATTR_NAME_REQUESTID)
					.getValue());
		}

		writeXML(searchRequest);
	}

	/**
	 * Creates a FilterItem object from a specified hierarchical Attribute
	 * object.
	 * 
	 * @param attr
	 *            Attribute object
	 * @return created FilterItem object from the provided Attribute object
	 * @see #parseFilterItem(NodeImpl, FilterItem)
	 * @throws DSMLProfileException
	 */
	private FilterItem getFilterItem(Attribute attr)
			throws DSMLProfileException {
		String xmlTag = attr.getLocalName();
		String childNodeName = "";
		FilterItem item = null;

		if (XML_TAG_NOT.equalsIgnoreCase(xmlTag)) {
			item = new Not(null);
			Attribute notChild = getFirstChildNode(attr);
			FilterItem notItem = getFilterItem(notChild);
			if (notItem != null) {
				((Not) item).setItem(notItem);
			}
		} else if (XML_TAG_AND.equalsIgnoreCase(xmlTag)) {
			item = new And();
			Attribute andChild = getFirstChildNode(attr);
			do {
				FilterItem andItem = getFilterItem(andChild);
				if (andItem != null) {
					((And) item).addItem(andItem);
				}
				andChild = getNextSiblingNode(andChild);
			} while (andChild != null);

		} else if (XML_TAG_OR.equalsIgnoreCase(xmlTag)) {
			item = new Or();
			Attribute orChild = getFirstChildNode(attr);
			do {
				FilterItem orItem = getFilterItem(orChild);
				if (orItem != null)
					((Or) item).addItem(orItem);
				orChild = getNextSiblingNode(orChild);
			} while (orChild != null);

		} else if (XML_TAG_SUBSTRINGS.equalsIgnoreCase(xmlTag)) {
			Attribute ssChild = getFirstChildNode(attr);
			Substrings substrings = new Substrings();

			Property name = attr.getAttributeNode(XML_ATTR_NAME);
			if (name != null) {
				substrings.setName(name.getValue());
			}

			do {
				childNodeName = ssChild.getLocalName();
				String tagValue = ssChild.getValue();

				if (ssChild.getValue(0) instanceof byte[]) {
					tagValue = encodeValue((byte[]) ssChild.getValue(0));
				}

				if (XML_ATTR_NAME.equalsIgnoreCase(childNodeName))
					substrings.setName(ssChild.getValue());
				else if (XML_TAG_FINAL.equalsIgnoreCase(childNodeName))
					substrings.setFinal(new DSMLValue(tagValue));
				else if (XML_TAG_INITIAL.equalsIgnoreCase(childNodeName))
					substrings.setInitial(new DSMLValue(tagValue));
				else if (XML_TAG_ANY.equalsIgnoreCase(childNodeName)) {
					Object[] valList = ssChild.getValues();
					DSMLValue[] values = new DSMLValue[valList.length];

					for (int i = 0; i < valList.length; i++) {
						if (valList[i] instanceof byte[]) {
							values[i] = new DSMLValue(
									encodeValue((byte[]) valList[i]));
						} else {
							values[i] = new DSMLValue(valList[i].toString());
						}
					}

					substrings.setAny(values);
				}
				ssChild = getNextSiblingNode(ssChild);
			} while (ssChild != null);

			item = substrings;
		} else if (XML_TAG_EQUALITYMATCH.equalsIgnoreCase(xmlTag)) {
			Attribute emChild = getFirstChildNode(attr);
			String name = "", value = "";

			Property nameProp = attr.getAttributeNode(XML_ATTR_NAME);
			if (nameProp != null) {
				name = nameProp.getValue();
			}

			do {
				childNodeName = emChild.getLocalName();
				if (XML_ATTR_NAME.equalsIgnoreCase(childNodeName))
					name = emChild.getValue();
				else if (XML_TAG_VALUE.equalsIgnoreCase(childNodeName)) {
					value = emChild.getValue();
					if (emChild.getValue(0) instanceof byte[]) {
						value = encodeValue((byte[]) emChild.getValue(0));
					}
				}
				emChild = getNextSiblingNode(emChild);
			} while (emChild != null);

			EqualityMatch equalMatch = new EqualityMatch(name, value);
			item = equalMatch;
		} else if (XML_TAG_GREATER.equalsIgnoreCase(xmlTag)) {
			Attribute geChild = getFirstChildNode(attr);
			GreaterOrEqual greater = new GreaterOrEqual();

			Property name = attr.getAttributeNode(XML_ATTR_NAME);
			if (name != null) {
				greater.setName(name.getValue());
			}

			do {
				childNodeName = geChild.getLocalName();
				if (XML_ATTR_NAME.equalsIgnoreCase(childNodeName))
					greater.setName(geChild.getValue());
				else if (XML_TAG_VALUE.equalsIgnoreCase(childNodeName)) {
					String value = geChild.getValue();
					if (geChild.getValue(0) instanceof byte[]) {
						value = encodeValue((byte[]) geChild.getValue(0));
					}
					greater.setValue(new DSMLValue(value));
				}
				geChild = getNextSiblingNode(geChild);
			} while (geChild != null);
			item = greater;
		} else if (XML_TAG_LESS.equalsIgnoreCase(xmlTag)) {
			Attribute leChild = getFirstChildNode(attr);
			LessOrEqual less = new LessOrEqual();

			Property name = attr.getAttributeNode(XML_ATTR_NAME);
			if (name != null) {
				less.setName(name.getValue());
			}

			do {
				childNodeName = leChild.getLocalName();
				if (XML_ATTR_NAME.equalsIgnoreCase(childNodeName))
					less.setName(leChild.getValue());
				else if (XML_TAG_VALUE.equalsIgnoreCase(childNodeName)) {
					String value = leChild.getValue();
					if (leChild.getValue(0) instanceof byte[]) {
						value = encodeValue((byte[]) leChild.getValue(0));
					}
					less.setValue(new DSMLValue(value));
				}

				leChild = getNextSiblingNode(leChild);
			} while (leChild != null);

			item = less;
		} else if (XML_TAG_APPROXMATCH.equalsIgnoreCase(xmlTag)) {
			Attribute amChild = getFirstChildNode(attr);
			ApproxMatch approxMatch = new ApproxMatch();

			Property name = attr.getAttributeNode(XML_ATTR_NAME);
			if (name != null) {
				approxMatch.setName(name.getValue());
			}

			do {
				childNodeName = amChild.getLocalName();
				if (XML_ATTR_NAME.equalsIgnoreCase(childNodeName))
					approxMatch.setName(amChild.getValue());
				else if (XML_TAG_VALUE.equalsIgnoreCase(childNodeName)) {
					String value = amChild.getValue();
					if (amChild.getValue(0) instanceof byte[]) {
						value = encodeValue((byte[]) amChild.getValue(0));
					}
					approxMatch.setValue(new DSMLValue(value));
				}

				amChild = getNextSiblingNode(amChild);
			} while (amChild != null);
			item = approxMatch;
		} else if (XML_TAG_EXTMATCH.equalsIgnoreCase(xmlTag)) {
			Attribute emChild = getFirstChildNode(attr);
			String name = "", value = "", rule = "";
			boolean dnAttr = false;

			Property nameProp = attr.getAttributeNode(XML_ATTR_NAME);
			if (nameProp != null) {
				name = nameProp.getValue();
			}

			do {
				childNodeName = emChild.getLocalName();

				if (XML_ATTR_NAME.equalsIgnoreCase(childNodeName))
					name = emChild.getValue();
				else if (XML_TAG_VALUE.equalsIgnoreCase(childNodeName)) {
					value = emChild.getValue();
					if (emChild.getValue(0) instanceof byte[]) {
						value = encodeValue((byte[]) emChild.getValue(0));
					}
				} else if (XML_ATTR_DNATTR.equalsIgnoreCase(childNodeName))
					dnAttr = emChild.getValue().equalsIgnoreCase("true") ? true
							: false;
				else if (XML_ATTR_RULE.equalsIgnoreCase(childNodeName))
					rule = emChild.getValue();
				emChild = getNextSiblingNode(emChild);
			} while (emChild != null);

			ExtensibleMatch extMatch = new ExtensibleMatch(name, value, rule,
					dnAttr);
			item = extMatch;
		} else if (XML_TAG_PRESENT.equalsIgnoreCase(xmlTag)) {
			Present present = new Present();
			Property name = attr.getAttributeNode(XML_ATTR_NAME);
			if (name != null) {
				present.setName(name.getValue());
			}
			Attribute pChild = getFirstChildNode(attr);
			if (pChild != null && XML_ATTR_NAME.equalsIgnoreCase(pChild.getLocalName()))
				present.setName(pChild.getValue());
			item = present;
		}
		return item;
	}

	private Attribute getFirstChildNode(Attribute parent) {
		if (parent == null) {
			return null;
		}

		NodeList children = parent.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
				return (Attribute) children.item(i);
			}
		}
		return null;
	}

	private Attribute getNextSiblingNode(Attribute sibling) {
		if (sibling == null) {
			return null;
		}

		Node next = sibling.getNextSibling();

		while (next != null) {
			if (next.getNodeType() == Node.ELEMENT_NODE) {
				return (Attribute) next;
			}
		}
		return null;
	}

	/**
	 * Writes down a modify request to a batch message
	 * 
	 * @param aEntry
	 *            TDI Entry that presents the request
	 * @throws Exception
	 *             if the writing of the request fails
	 */
	private void writeModifyRequest(Entry aEntry) throws Exception {

		ModifyRequest modifyRequest = new ModifyRequest();
		PSOIdentifier psoID = new PSOIdentifier();
		Attribute containerIDAttr = aEntry.getAttribute(ATTR_NAME_PSOID);
		if (containerIDAttr != null) {
			String containerIDValue = containerIDAttr.getValue();
			if (containerIDValue != null) {
				psoID.setID(containerIDValue);
			}
		}
		// add targetID
		Attribute targetID = aEntry.getAttribute(ATTR_NAME_PSO_TARGETID);
		if (targetID != null) {
			String targetIDValue = targetID.getValue();
			if (targetIDValue != null) {
				psoID.setTargetID(targetIDValue);
			}
		}
		modifyRequest.setPsoID(psoID);

		if (aEntry.getAttribute(ATTR_NAME_REQUESTID) != null) {
			modifyRequest.setRequestID(aEntry.getAttribute(ATTR_NAME_REQUESTID)
					.getValue());
		}

		Modification mod = new Modification();
		// skip the attributes whose name starts with spml.
		String[] attributeNames = aEntry.getAttributeNames();
		for (int i = 0; i < attributeNames.length; i++) {
			if (!attributeNames[i].startsWith(ATTR_NAME_PREFIX_SPML)) {
				Attribute attr = aEntry.getAttribute(attributeNames[i]);
				String attrValue = attr.getValue();
				if (attrValue != null) {
					DSMLModification dsmlMod = new DSMLModification(
							attributeNames[i], attrValue, ModificationMode
									.getConstant(attr.getOperation()));
					if (attr.size() > 1) {
						for (int j = 1; j < attr.size(); j++) {
							Object value = attr.getValue(j);
							if (value instanceof String) {
								dsmlMod.addValue(new DSMLValue((String) value));
							} else if (value instanceof byte[]) {
								String encodedValue = encodeValue((byte[]) value);
								dsmlMod.addValue(new DSMLValue(encodedValue));
							} else {
								dsmlMod
										.addValue(new DSMLValue(value
												.toString()));
							}
						}
					}
					mod.addOpenContentElement(dsmlMod);
				}
			}
		}
		modifyRequest.addModification(mod);

		writeXML(modifyRequest);
	}

	/**
	 * Decides what the type of the response is and writes it down to a batch
	 * message
	 * 
	 * @param aEntry
	 *            TDI Entry that presents the response
	 * @throws Exception
	 *             if the writing of a response fails or the operation is of
	 *             unknown type
	 */
	private void writeResponse(Entry aEntry) throws Exception {

		String operation = aEntry.getAttribute(ATTR_NAME_SPML_OPERATION)
				.getValue();
		if (operation.equalsIgnoreCase(OPERATION_ADD)) {

			writeAddResponse(aEntry);

		} else if (operation.equalsIgnoreCase(OPERATION_MODIFY)) {

			writeModifyResponse(aEntry);

		} else if (operation.equalsIgnoreCase(OPERATION_LOOKUP)) {

			writeLookupResponse(aEntry);

		} else if (operation.equalsIgnoreCase(OPERATION_DELETE)) {

			writeDeleteResponse(aEntry);

		} else if (operation.equalsIgnoreCase(OPERATION_SEARCH)) {

			writeSearchResponse(aEntry);

		} else {
			throw new Exception(sResHash.getString(
					"PARSER.SPMLV2.ERROR.UNKNOWN.RESPONSE", operation));
		}
	}

	/**
	 * Writes down an add response to a batch message
	 * 
	 * @param aEntry
	 *            TDI Entry that presents the response
	 * @throws Exception
	 *             if the writing of the response fails
	 */
	private void writeAddResponse(Entry aEntry) throws Exception {

		AddResponse addResponse = new AddResponse();
		processCommonResponseAttr(addResponse, aEntry);

		String[] attrs = aEntry.getAttributeNames();
		Extensible ext = new Extensible();
		PSO pso = new PSO();
		PSOIdentifier psoid = new PSOIdentifier();

		for (int i = 0; i < attrs.length; i++) {
			if (attrs[i].equalsIgnoreCase(ATTR_NAME_PSOID)) {
				psoid.setID(aEntry.getAttribute(ATTR_NAME_PSOID).getValue());
				if (aEntry.getAttribute(ATTR_NAME_PSO_TARGETID) != null) {
					psoid.setTargetID(aEntry.getAttribute(
							ATTR_NAME_PSO_TARGETID).getValue());
				}
				pso.setPsoID(psoid);
			} else if (!attrs[i].startsWith(ATTR_NAME_PREFIX_SPML)) {
				ext.addOpenContentElement(attrToDSMLAttr(aEntry
						.getAttribute(attrs[i])));
			}
		}
		pso.setData(ext);
		addResponse.setPso(pso);
		writeXML(addResponse);
	}

	/**
	 * Writes down a modify response to a batch message
	 * 
	 * @param aEntry
	 *            TDI Entry that presents the response
	 * @throws Exception
	 *             if the writing of the response fails
	 */
	private void writeModifyResponse(Entry aEntry) throws Exception {

		ModifyResponse modifyResponse = new ModifyResponse();
		processCommonResponseAttr(modifyResponse, aEntry);
		String[] attrs = aEntry.getAttributeNames();
		Extensible ext = new Extensible();
		PSO pso = new PSO();
		PSOIdentifier psoid = new PSOIdentifier();
		for (int i = 0; i < attrs.length; i++) {
			if (attrs[i].equalsIgnoreCase(ATTR_NAME_PSOID)) {
				psoid.setID(aEntry.getAttribute(ATTR_NAME_PSOID).getValue());
				if (aEntry.getAttribute(ATTR_NAME_PSO_TARGETID) != null) {
					psoid.setTargetID(aEntry.getAttribute(
							ATTR_NAME_PSO_TARGETID).getValue());
				}
				pso.setPsoID(psoid);
			} else if (!attrs[i].startsWith(ATTR_NAME_PREFIX_SPML)) {
				// do we have DSMLAttr at all?
				ext.addOpenContentElement(attrToDSMLAttr(aEntry
						.getAttribute(attrs[i])));
			}
		}
		if (ext.getOpenContentElements().length > 0) {
			pso.setData(ext);
		}
		modifyResponse.setPso(pso);
		writeXML(modifyResponse);
	}

	/**
	 * Writes down a lookup response to a batch message
	 * 
	 * @param aEntry
	 *            TDI Entry that presents the response
	 * @throws Exception
	 *             if the writing of the response fails
	 */
	private void writeLookupResponse(Entry aEntry) throws Exception {

		LookupResponse lookupResponse = new LookupResponse();
		processCommonResponseAttr(lookupResponse, aEntry);

		String[] attrs = aEntry.getAttributeNames();
		Extensible ext = new Extensible();
		PSO pso = new PSO();
		PSOIdentifier psoid = new PSOIdentifier();

		for (int i = 0; i < attrs.length; i++) {
			if (attrs[i].equalsIgnoreCase(ATTR_NAME_PSOID)) {
				psoid.setID(aEntry.getAttribute(ATTR_NAME_PSOID).getValue());
				if (aEntry.getAttribute(ATTR_NAME_PSO_TARGETID) != null) {
					psoid.setTargetID(aEntry.getAttribute(
							ATTR_NAME_PSO_TARGETID).getValue());
				}
				pso.setPsoID(psoid);
			} else if (!attrs[i].startsWith(ATTR_NAME_PREFIX_SPML)) {
				ext.addOpenContentElement(attrToDSMLAttr(aEntry
						.getAttribute(attrs[i])));
			}
		}
		pso.setData(ext);
		lookupResponse.setPso(pso);
		writeXML(lookupResponse);
	}

	/**
	 * Writes down a delete response to a batch message
	 * 
	 * @param aEntry
	 *            TDI Entry that presents the response
	 * @throws Exception
	 *             if the writing of the response fails
	 */
	private void writeDeleteResponse(Entry aEntry) throws Exception {

		DeleteResponse deleteResponse = new DeleteResponse();
		processCommonResponseAttr(deleteResponse, aEntry);

		writeXML(deleteResponse);
	}

	/**
	 * Writes down a search response to a batch message
	 * 
	 * @param aEntry
	 *            TDI entry that presents the response
	 * @throws Exception
	 *             if the writing of the response fails
	 */
	private void writeSearchResponse(Entry aEntry) throws Exception {

		SearchResponse searchResponse = new SearchResponse();
		processCommonResponseAttr(searchResponse, aEntry);

		Attribute resultEntries = aEntry.getAttribute(ATTR_NAME_RESULT_ENTRIES);
		for (int i = 0; i < resultEntries.size(); i++) {
			Entry result = (Entry) resultEntries.getValue(i);
			String[] attrs = result.getAttributeNames();
			Extensible ext = new Extensible();
			PSO pso = new PSO();
			PSOIdentifier psoid = new PSOIdentifier();
			for (int j = 0; j < attrs.length; j++) {
				if (attrs[j].equalsIgnoreCase(ATTR_NAME_PSOID)) {
					psoid
							.setID(result.getAttribute(ATTR_NAME_PSOID)
									.getValue());
					if (result.getAttribute(ATTR_NAME_PSO_TARGETID) != null) {
						psoid.setTargetID(result.getAttribute(
								ATTR_NAME_PSO_TARGETID).getValue());
					}
					pso.setPsoID(psoid);
				} else if (!attrs[j].startsWith(ATTR_NAME_PREFIX_SPML)) {
					ext.addOpenContentElement(attrToDSMLAttr(result
							.getAttribute(attrs[j])));
				}
			}
			pso.setData(ext);
			searchResponse.addPSO(pso);
		}
		writeXML(searchResponse);
	}

	/**
	 * Writes down a request or response to a physical xml file
	 * 
	 * @param marshallable
	 *            {@link Marshallable}
	 */
	private void writeXML(Marshallable marshallable) throws Exception {
		String res = marshallable.toXML(marshaller);
		if (firstWrite) {
			if (writeType.equals(OP_TYPE_REQUEST)) {
				res = "<spmlbatch:batchRequest xmlns:spmlbatch=\"urn:oasis:names:tc:SPML:2:0:batch\">"
						+ res;
			} else if (writeType.equals(OP_TYPE_RESPONSE)) {
				res = "<spmlbatch:batchResponse xmlns:spmlbatch=\"urn:oasis:names:tc:SPML:2:0:batch\">"
						+ res;
			}
			firstWrite = false;
		}
		getWriter().write(res);
		getWriter().flush();
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "1.0-di7.1.1 %I% 20%E%";
	}

	/**
	 * Closes the parser
	 * 
	 * @exception Exception
	 *                if an error occurs.
	 */
	public void closeParser() throws Exception {
		flush();
		super.closeParser();
	}

	/**
	 * Flushes the parser's outputstream or writer
	 */
	public void flush() {

		if (getWriter() != null) {
			try {
				if (writeType.equals(OP_TYPE_REQUEST)) {
					getWriter().write("</spmlbatch:batchRequest>");
				} else if (writeType.equals(OP_TYPE_RESPONSE)) {
					getWriter().write("</spmlbatch:batchResponse>");
				}
				getWriter().flush();
			} catch (Exception e) {
				logmsg(sResHash.getString("PARSER.SPMLV2.RSPXML.WARNING", e));
			}
		}
	}

	/**
	 * Processes the TDI Entry's attributes that are common for the responses
	 * 
	 * @param aResponse
	 *            The current response
	 * @param aEntry
	 *            TDI Entry that presents the response
	 */
	private void processCommonResponseAttr(Response aResponse, Entry aEntry) {

		if (aEntry.getAttribute(ATTR_NAME_REQUESTID) != null) {
			aResponse.setRequestID(aEntry.getAttribute(ATTR_NAME_REQUESTID)
					.getValue());
		}
		if (aEntry.getAttribute(ATTR_NAME_STATUS) != null) {
			aResponse.setStatus(getStatusCode(aEntry.getAttribute(
					ATTR_NAME_STATUS).getValue()));
		}
		if (aEntry.getAttribute(ATTR_NAME_ERROR_CODE) != null) {
			aResponse.setError(getErrorCode(aEntry.getAttribute(
					ATTR_NAME_ERROR_CODE).getValue()));
		}
		if (aEntry.getAttribute(ATTR_NAME_ERROR_MESSAGES) != null) {
			Object[] messages = aEntry.getAttribute(ATTR_NAME_ERROR_MESSAGES)
					.getValues();
			for (int j = 0; j < messages.length; j++) {
				aResponse.addErrorMessage(messages[j].toString());
			}
		}
	}

	/**
	 * Returns an error code for a given error message
	 * 
	 * @param aErrorCode
	 *            Error message string
	 * @return Object of type org.openspml.v2.msg.spml.ErrorCode that represents
	 *         the error message
	 */
	private ErrorCode getErrorCode(String aErrorCode) {
		return ((ErrorCode) ErrorCode.getConstant(ErrorCode.class, aErrorCode));
	}

	/**
	 * Returns a status code
	 * 
	 * @param aStatusCode
	 *            String that represents the status
	 * @return Object of type org.openspml.v2.msg.spml.StatusCode that
	 *         represents the status
	 */
	private StatusCode getStatusCode(String aStatusCode) {
		return ((StatusCode) StatusCode.getConstant(StatusCode.class,
				aStatusCode));
	}

	/**
	 * Converts a TDI Entry Attribute to object of type
	 * org.openspml.v2.profiles.dsml.DSMLAttr. If the TDI Entry Attribute's
	 * value is a Java byte array, the Attribute's value is Base64 encoded to
	 * String before being written to the SPML message. If the Attribute's value
	 * is neither String, nor Java byte array, the value is converted to string
	 * by calling the object's toString method and written down to the SPML
	 * message
	 * 
	 * @param aAttribute
	 *            TDI Entry Attribute that written down to the SPML message
	 * @return Object of type org.openspml.v2.profiles.dsml.DSMLAttr that
	 *         represents the TDI Entry Attribute
	 * @throws Exception
	 *             if the conversion fails
	 */
	private DSMLAttr attrToDSMLAttr(Attribute aAttribute) throws Exception {
		DSMLAttr dsmlAttr = new DSMLAttr(aAttribute.getName(), aAttribute
				.getValue());
		if (aAttribute.size() > 1) {
			for (int i = 1; i < aAttribute.size(); i++) {
				if (aAttribute.getValue(i) != null) {
					Object value = aAttribute.getValue(i);
					if (value instanceof String) {
						dsmlAttr.addValue(new DSMLValue((String) value));
					} else if (value instanceof byte[]) {
						String encodedValue = encodeValue((byte[]) value);
						dsmlAttr.addValue(new DSMLValue(encodedValue));
					} else {
						dsmlAttr.addValue(new DSMLValue(value.toString()));
					}
				}
			}
		}
		return dsmlAttr;
	}

	/**
	 * Adds the DSML attribute to the TDI Entry
	 * 
	 * @param aDSMLAttr
	 *            the attribute to be added
	 * @param aEntry
	 *            the TDI Entry
	 * @throws Exception
	 *             if the operation fails
	 */
	private void addDSMLAttrToEntry(DSMLAttr aDSMLAttr, Entry aEntry)
			throws Exception {
		Attribute attr = aEntry.newAttribute(aDSMLAttr.getName());
		for (int i = 0; i < aDSMLAttr.getValues().length; i++) {
			attr.addValue(aDSMLAttr.getValues()[i].getValue());
		}
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

		Iterator<String> names = binaryAttributes.iterator();

		while (names.hasNext()) {
			String name = names.next();
			Attribute encodedAttr = aEntry.getAttribute(name);

			if ((encodedAttr != null) && (encodedAttr.size() > 0)) {
				if (debugMode()) {
					debug(sResHash.getString(
							"PARSER.SPMLV2.BASE64.DECODINGATTRIB.INFO", name));
				}

				Object[] encodedValues = encodedAttr.getValues();
				Vector<Object> binaryValues = new Vector<Object>();

				for (int j = 0; j < encodedValues.length; j++) {
					if (encodedValues[j] instanceof String) {
						byte[] binaryValue = Base64
								.decode((String) encodedValues[j]);
						if (binaryValue == null) {
							logmsg(sResHash.getString(
									"PARSER.SPMLV2.BINNOT64ENCODED.INFO", name));
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

}
