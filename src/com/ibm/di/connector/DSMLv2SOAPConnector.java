/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Vector;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.parser.Dsmlv2Parser;
import com.ibm.di.parser.HTTPParser;
import com.ibm.di.security.SSL;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.ldap.dsml.Filter;

/**
 * The DSMLv2 SOAP Connector implements the DSMLv2 standard
 * (http://www.oasis-open.org/committees/dsml/docs/DSMLv2.doc). It is able to
 * execute DSMLv2 requests against a DSML Server. It also provides the option to
 * use DSML SOAP binding.
 */
public class DSMLv2SOAPConnector extends Connector implements
		ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "dsmlv2connector";

	/**
	 * Name of the component.
	 */
	private static final String myName = "DSML v2 SOAP Connector";

	/**
	 * A parameter name: {@value #PARAMETER_URL}
	 */
	public static final String PARAMETER_URL = "url";

	/**
	 * A parameter name: {@value #PARAMETER_AUTH_METHOD}
	 */
	public static final String PARAMETER_AUTH_METHOD = "authenticationMethod";

	/**
	 * A parameter name: {@value #PARAMETER_USERNAME}
	 */
	public static final String PARAMETER_USERNAME = "username";

	/**
	 * A parameter name: {@value #PARAMETER_PASSWORD}
	 */
	public static final String PARAMETER_PASSWORD = "password";

	/**
	 * A parameter name: {@value #PARAMETER_BINARY_ATTRIBUTES}
	 */
	public static final String PARAMETER_BINARY_ATTRIBUTES = "binaryAttributes";

	/**
	 * A parameter name: {@value #PARAMETER_SEARCH_BASE}
	 */
	public static final String PARAMETER_SEARCH_BASE = "searchBase";

	/**
	 * A parameter name: {@value #PARAMETER_SEARCH_FILTER}
	 */
	public static final String PARAMETER_SEARCH_FILTER = "searchFilter";

	/**
	 * A parameter name: {@value #PARAMETER_SEARCH_SCOPE}
	 */
	public static final String PARAMETER_SEARCH_SCOPE = "searchScope";

	/**
	 * A parameter name: {@value #PARAMETER_SOAPBINDING}
	 */
	public static final String PARAMETER_SOAPBINDING = "soapbinding";

	/**
	 * A parameter name: {@value #PARAMETER_SOAPACTION}
	 */
	public static final String PARAMETER_SOAPACTION = "soapAction";

	/**
	 * An attribute name: {@value #ATTR_NAME_HTTP_SOAPACTION}
	 */
	public static final String ATTR_NAME_HTTP_SOAPACTION = "http.SOAPAction";

	/**
	 * An attribute name: {@value #ATTR_NAME_HTTP_URL}
	 */
	public static final String ATTR_NAME_HTTP_URL = "http.url";

	/**
	 * An attribute name: {@value #ATTR_NAME_HTTP_METHOD}
	 */
	public static final String ATTR_NAME_HTTP_METHOD = "http.method";

	/**
	 * An attribute name: {@value #ATTR_NAME_HTTP_REMOTE_USER}
	 */
	public static final String ATTR_NAME_HTTP_REMOTE_USER = "http.remote_user";

	/**
	 * An attribute name: {@value #ATTR_NAME_HTTP_REMOTE_PASSWORD}
	 */
	public static final String ATTR_NAME_HTTP_REMOTE_PASSWORD = "http.remote_pass";

	/**
	 * An attribute name: {@value #ATTR_NAME_HTTP_BODY}
	 */
	public static final String ATTR_NAME_HTTP_BODY = "http.body";

	/**
	 * An attribute name: {@value #ATTR_NAME_HTTP_CONTENT_TYPE}
	 */
	public static final String ATTR_NAME_HTTP_CONTENT_TYPE = "http.Content-Type";

	/**
	 * An attribute name: {@value #ATTR_NAME_HTTP_CHARACTER_SET}
	 */
	public static final String ATTR_NAME_HTTP_CHARACTER_SET = "characterSet";

	/**
	 * String constant.
	 */
	public static final String HTTP_BASIC_AUTH = "HTTP basic authentication";

	// From ITDS DSMLv2 library
	/**
	 * IBM TDS DSMLv2 String constant.
	 */
	public static final String BASE_OBJECT = "baseObject";

	/**
	 * IBM TDS DSMLv2 String constant.
	 */
	public static final String SINGLE_LEVEL = "singleLevel";

	/**
	 * IBM TDS DSMLv2 String constant.
	 */
	public static final String WHOLE_SUBTREE = "wholeSubtree";

	/**
	 * 
	 */
	private static final String SEARCH_FILTER_ALL = "(objectClass=*)";

	/**
	 * {@link Dsmlv2Parser} instance
	 */
	private Dsmlv2Parser mDsmlv2Parser;

	/**
	 * {@link HTTPParser} instance
	 */
	private HTTPParser mHttpParser;

	/**
	 * Port number.
	 */
	private int mPort;

	/**
	 * URL address.
	 */
	private String mURL;

	/**
	 * Host name.
	 */
	private String mHost;

	/**
	 * Protocol.
	 */
	private String mProtocol;

	/**
	 * User name.
	 */
	private String mUserName;

	/**
	 * Password.
	 */
	private String mPassword;

	/**
	 * User defined binary attributes.
	 */
	private String mBinaryAttributes;

	/**
	 * Type of HTTP authentication
	 */
	private String mAuthentiocationMethod;

	/**
	 * Starting point for searches when iterating
	 */
	private String mSearchBase;

	/**
	 * LDAP filter to be used when iterating
	 */
	private String mSearchFilter;

	/**
	 * <b>subtree</b> search all levels from search base and below<br>
	 * 
	 * <b>onelevel</b> search only immediate children of search base
	 * 
	 */
	private String mSearchScope;

	/**
	 * SOAP action.
	 */
	private String mSOAPAction = " ";

	/**
	 * Vector containing the individual search result Entries.
	 */
	private Vector<?> mSearchResults;

	/**
	 * DSML SOAP Binding flag
	 */
	private boolean mSoapBinding = true;
	/**
	 * Object used for access of the TMS messages
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Default constructor. Instantiates the internally used DSMLv2 Parser and
	 * HTTP Parser.
	 */
	public DSMLv2SOAPConnector() {
		setName(myName);
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.ADDONLY_MODE, ConnectorConfig.LOOKUP_MODE,
				ConnectorConfig.DELETE_MODE, ConnectorConfig.UPDATE_MODE,
				ConnectorConfig.DELTA_MODE, ConnectorConfig.CALL_REPLY_MODE });

		mHttpParser = new HTTPParser();
		mHttpParser.setUseProperties(false);
		mHttpParser.setClientMode(true);
		mHttpParser.setContext(this);

		mDsmlv2Parser = new Dsmlv2Parser();
		mDsmlv2Parser.setContext(this);
	}

	/**
	 * Initializes the connector. Parses all the configuration parameters.
	 * 
	 * @param o -
	 *            ignored
	 * @throws Exception
	 *             if the {@value #PARAMETER_URL} parameter is missing or the
	 *             protocol used is neither "http" nor "https".
	 */
	public void initialize(Object o) throws Exception {

		mDsmlv2Parser.setDebug(debugMode());
		mHttpParser.setDebug(debugMode());

		mURL = getParam(PARAMETER_URL);
		if (mURL == null || mURL.equals("")) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DSML.MISSING.REQUIRED.PARAMURL.ERROR"));
		}

		mAuthentiocationMethod = getParam(PARAMETER_AUTH_METHOD);
		if (mAuthentiocationMethod == null || mAuthentiocationMethod.equals("")) {
			mAuthentiocationMethod = HTTP_BASIC_AUTH;
		}

		mUserName = getParam(PARAMETER_USERNAME);
		mPassword = getParam(PARAMETER_PASSWORD);
		mBinaryAttributes = getParam(PARAMETER_BINARY_ATTRIBUTES);
		mSearchBase = getParam(PARAMETER_SEARCH_BASE);

		mSearchFilter = getParam(PARAMETER_SEARCH_FILTER);
		if (mSearchFilter == null || mSearchFilter.equals("")) {
			mSearchFilter = SEARCH_FILTER_ALL;
		}

		String scope = getParam(PARAMETER_SEARCH_SCOPE);
		if (scope != null && scope.equalsIgnoreCase("onelevel")) {
			mSearchScope = SINGLE_LEVEL;
		} else {
			mSearchScope = WHOLE_SUBTREE;
		}

		String check = getParam(PARAMETER_SOAPBINDING);
		mSoapBinding = (check == null || check.equalsIgnoreCase("true"));

		if (debugMode()) {
			if (mSoapBinding) {
				debug(sResHash.getString("CONNECTOR.DSML.USESOAP.BINDING.INFO"));
			} else {
				debug(sResHash
						.getString("CONNECTOR.DSML.NOUSESOAP.BINDING.INFO"));
			}

		}

		mDsmlv2Parser.setParam(Dsmlv2Parser.PARAMETER_MODE, "Client");
		mDsmlv2Parser.setParam(PARAMETER_SOAPBINDING, (mSoapBinding) ? "true"
				: "false");
		mDsmlv2Parser.setParam(Dsmlv2Parser.PARAMETER_BINARY_ATTRIBUTES,
				mBinaryAttributes);

		mHttpParser.setParam(ATTR_NAME_HTTP_CHARACTER_SET, "UTF-8");

		// Retrieve host,port and protocol from URL
		URL url = new URL(mURL);
		mHost = url.getHost();
		mPort = url.getPort();
		mProtocol = url.getProtocol();

		if (mProtocol.equalsIgnoreCase("https")) {
			// Use default port if not specified
			if (mPort == -1) {
				mPort = 443;
			}
		} else if (mProtocol.equalsIgnoreCase("http")) {
			// Use default port if not specified
			if (mPort == -1) {
				mPort = 80;
			}
		} else {
			// We only do HTTP
			throw new Exception(sResHash.getString(
					"CONNECTOR.DSML.UNSUPPORTED.PROTOCOL.ERROR", mProtocol));
		}
	}

	/**
	 * Sends the DSML request to the DSML Server. The entry is first parsed. If
	 * the server response indicates that an error has occurred then an
	 * exception with that error will be thrown.
	 * 
	 * @param aPutEntry
	 *            the DSML request as an Entry object, used by the DSMLv2Parser.
	 * @throws Exception
	 *             if the distinguish parameter is missing or the DSML Server
	 *             replied with an error code.
	 */
	public void putEntry(Entry aPutEntry) throws Exception {

		if (aPutEntry.getAttribute(Dsmlv2Parser.ATTR_NAME_DN) == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.DSML.NODNATTRIB.PUTENTRY.ERROR"));
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.DSML.PUTENTRY.DN.INFO",
					aPutEntry.getAttribute(Dsmlv2Parser.ATTR_NAME_DN)));
		}

		aPutEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_OPERATION,
				Dsmlv2Parser.OPERATION_ADD_REQUEST);

		sendRequest(aPutEntry);
		Entry addResponseEntry = readResponse();
		checkDsmlResponse(addResponseEntry);
	}

	/**
	 * Sends the DSML request to the DSML Server. The entry is first parsed. If
	 * the server response indicates that an error has occurred then an
	 * exception with that error will be thrown. If the response is not an error
	 * then that response is parsed back as an {@link Entry} object.
	 * 
	 * @param aQueryEntry
	 *            the DSML request as an Entry object, used by the DSMLv2Parser.
	 * @return the DSML response as an Entry object, used by the DSMLv2Parser.
	 * @throws Exception
	 *             if the DSML Server replied with an error code.
	 */
	public Entry queryReply(Entry aQueryEntry) throws Exception {

		sendRequest(aQueryEntry);
		Entry replayEntry = readResponse();
		checkDsmlResponse(replayEntry);

		return replayEntry;
	}

	/**
	 * Initializes the connection with the server and sends a request based on
	 * the configured parameters. This operation prepares the result set so the
	 * {@link #getNextEntry()} could return single entry at a time.
	 * 
	 * @throws Exception
	 *             if the server responded with an error message.
	 */
	public void selectEntries() throws Exception {

		mSearchResults = null;
		Entry searchRequestEntry = new Entry();

		if (debugMode()) {
			debug(sResHash.getString(
					"CONNECTOR.DSML.SELECTENTRIES.BASEFILTER.INFO",
					new Object[] { mSearchBase, mSearchFilter }));
		}

		searchRequestEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DN, mSearchBase);
		searchRequestEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_FILTER,
				mSearchFilter);
		searchRequestEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_SCOPE,
				mSearchScope);
		searchRequestEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_OPERATION,
				Dsmlv2Parser.OPERATION_SEARCH_REQUEST);

		sendRequest(searchRequestEntry);
		Entry searchResponseEntry = readResponse();

		checkDsmlResponse(searchResponseEntry);
		mSearchResults = getResults(searchResponseEntry);
	}

	/**
	 * Gets an entry form the resultSet. The resultSet is created during the
	 * {@link #selectEntries()} operation.
	 * 
	 * @return The next entry in the resultSet.
	 * @throws Exception -
	 *             never
	 */
	public Entry getNextEntry() throws Exception {

		Entry nextEntry = null;

		if (mSearchResults != null && mSearchResults.size() > 0) {
			nextEntry = (Entry) mSearchResults.elementAt(0);
			setEntryFullDn(nextEntry);
			mSearchResults.remove(0);
		}

		return nextEntry;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry findEntry(SearchCriteria aSearch) throws Exception {

		clearFindEntries();

		Entry searchEntry = new Entry();
		searchEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_OPERATION,
				Dsmlv2Parser.OPERATION_SEARCH_REQUEST);

		if ((Dsmlv2Parser.ATTR_NAME_DN).equalsIgnoreCase(aSearch
				.getFirstCriteriaName())) {
			String checkDN = aSearch.getFirstCriteriaValue();
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.DSML.FINDENTRY.CALLED.DN.INFO",
						new Object[] { Dsmlv2Parser.ATTR_NAME_DN, checkDN }));
			}

			searchEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DN, checkDN);
			searchEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_FILTER,
					SEARCH_FILTER_ALL);
			searchEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_SCOPE,
					BASE_OBJECT);
		} else {
			String ldapFilter = aSearch.getLDAPFilter();
			Filter filter = new Filter(ldapFilter);
			ldapFilter = filter.getLdapFilter();
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.DSML.FINDENTRY.FILTER.INFO", ldapFilter));
			}

			searchEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DN, mSearchBase);
			searchEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_FILTER,
					ldapFilter);
			searchEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_SCOPE,
					WHOLE_SUBTREE);
		}

		sendRequest(searchEntry);
		Entry searchResponseEntry = readResponse();

		checkDsmlResponse(searchResponseEntry);
		mSearchResults = getResults(searchResponseEntry);

		if (mSearchResults == null) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.DSML.FINDENTRY.NOENTRIES.INFO"));
			}
			return null;
		}

		for (int i = 0; i < mSearchResults.size(); i++) {
			Entry entry = (Entry) mSearchResults.elementAt(i);
			setEntryFullDn(entry);
			addFindEntry(entry);
		}

		if (getFindEntryCount() == 1) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.DSML.FINDENTRY.SINGLE.INFO"));
			}
			return getFirstFindEntry();
		} else {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.DSML.FINDENTRY.MULTIPLE.INFO"));
			}
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void modEntry(Entry entry, SearchCriteria search) throws Exception {
		modEntry(entry, search, findEntry(search));
	}

	/**
	 * {@inheritDoc}
	 */
	public void modEntry(Entry entry, SearchCriteria search, Entry old)
			throws Exception {

		String newDN = null;
		String oldDN = null;

		if (entry == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.DSML.NOUPDATEENTRY.MODENTRY.ERROR"));
		}

		newDN = entry.getString(Dsmlv2Parser.ATTR_NAME_DN);

		if (old == null) {
			// Delta mode logic here
			old = entry;

			if (newDN == null
					&& (Dsmlv2Parser.ATTR_NAME_DN).equals(search
							.getFirstCriteriaName())) {
				newDN = search.getFirstCriteriaValue();
			}

			if (newDN == null) {
				throw new Exception(sResHash
						.getString("CONNECTOR.DSML.NODNAPPLIED.MODENTRY.ERROR"));
			}

			oldDN = newDN;
		} else {
			// Update mode logic here
			oldDN = old.getString(Dsmlv2Parser.ATTR_NAME_DN);

			if (oldDN == null) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.DSML.NODN.EXISTINGENTRY.MODENTRY.ERROR"));
			}

			if (newDN == null) {
				newDN = oldDN;
			}
		}

		// modrdn handle
		String newRDN = entry.getString("newrdn");
		if (newRDN != null) {
			newDN = newRDN
					+ newDN.substring(newDN.indexOf(","), newDN.length());
		}

		if (newDN.equals(oldDN)) {
			// DSML modify request here
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.DSML.MODDN.MODENTRY.INFO",
						oldDN));
			}
			String[] names = entry.getAttributeNames();
			for (int i = 0; i < names.length; i++) {
				String attrName = names[i];
				Attribute attrib = entry.getAttribute(attrName);
				old.setAttribute(attrName, attrib);
			}
			old.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_OPERATION,
					Dsmlv2Parser.OPERATION_MODIFY_REQUEST);
			old.setAttribute(Dsmlv2Parser.ATTR_NAME_DN, newDN);
		} else {
			// DSML modify DN request here
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.DSML.MODDN.RENENTRY.INFO",
						new Object[] { oldDN, newDN }));
			}
			old = new Entry();
			old.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_OPERATION,
					Dsmlv2Parser.OPERATION_MODIFYDN_REQUEST);
			old.setAttribute(Dsmlv2Parser.ATTR_NAME_DN, oldDN);
			old.setAttribute(Dsmlv2Parser.ATTR_NAME_NEWRDN, newDN);
			old.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_DELETEOLDRDN, "true");
		}

		sendRequest(old);
		Entry modResponseEntry = readResponse();

		checkDsmlResponse(modResponseEntry);
	}

	/**
	 * This method first look for an attribute with name
	 * {@link Dsmlv2Parser#ATTR_NAME_DN} in the provided entry if not found it
	 * looks the first criteria in the SearchCriteria object for the same name.
	 * If not found an exception is thrown. After the distinguished name is
	 * found the entry is parsed as a DSMLv2 request and sent. If the returned
	 * response indicates that an error had occurred then an exception is
	 * thrown.
	 * 
	 * @param aEntry
	 *            the entry which is the result of the attribute mapping
	 * @param aSearch
	 *            the search criteria object.
	 * @throws Exception
	 *             if an error while deleting the entry has occurred.
	 */
	public void deleteEntry(Entry aEntry, SearchCriteria aSearch)
			throws Exception {

		Entry requestEntry = aEntry;
		String dn = null;

		// First try provided entry
		if (requestEntry != null) {
			dn = requestEntry.getString(Dsmlv2Parser.ATTR_NAME_DN);
		} else {
			requestEntry = new Entry();
		}

		// Next try the search criteria
		if (dn == null
				&& (Dsmlv2Parser.ATTR_NAME_DN).equals(aSearch
						.getFirstCriteriaName())) {
			dn = aSearch.getFirstCriteriaValue();
		}

		// No DN - no delete
		if (dn == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.DSML.DELENTRY.NODN.ERROR"));
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.DSML.DELENTRY.DN.INFO", dn));
		}

		requestEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DN, dn);
		requestEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_OPERATION,
				Dsmlv2Parser.OPERATION_DELETE_REQUEST);

		sendRequest(requestEntry);
		Entry delResponeEntry = readResponse();

		checkDsmlResponse(delResponeEntry);
	}

	/**
	 * Sends a DSMLv2 request to the DSML Server.
	 * 
	 * @param aDsmlRequestEntry
	 *            the Entry holding the DSML request.
	 * @throws Exception
	 *             When an error occurs on sending the request.
	 */
	private void sendRequest(Entry aDsmlRequestEntry) throws Exception {

		// Get an SSL socket for HTTPS or a normal socket for HTTP
		Socket socket = null;

		if (mProtocol.equalsIgnoreCase("https")) {
			socket = SSL.getClientSocket(mHost, mPort);
		} else if (mProtocol.equalsIgnoreCase("http")) {
			socket = new Socket(mHost, mPort);
		} else {
			throw new Exception(sResHash.getString(
					"CONNECTOR.DSML.UNSUPPORTED.PROTOCOL.ERROR", mProtocol));
		}

		String batchRequestId = aDsmlRequestEntry.getString("batch.requestId");
		if (batchRequestId == null)
			batchRequestId = ""; // We cannot store null as a parameter in old versions
		else
			aDsmlRequestEntry.removeAttribute("batch.requestId");
		mDsmlv2Parser.setParam(Dsmlv2Parser.PARAMETER_BATCH_REQUEST_ID, batchRequestId);
			
		StringWriter stringWriter = new StringWriter();
		mDsmlv2Parser.setOutputStream(stringWriter);
		mDsmlv2Parser.initParser();
		mDsmlv2Parser.writeEntry(aDsmlRequestEntry);
		mDsmlv2Parser.closeParser();

		Entry httpRequestEntry = new Entry();

		mSOAPAction = getParam(PARAMETER_SOAPACTION);
		if (mSOAPAction == null) {
			mSOAPAction = " ";
		}

		httpRequestEntry.setAttribute(ATTR_NAME_HTTP_SOAPACTION, mSOAPAction);
		httpRequestEntry.setAttribute(ATTR_NAME_HTTP_URL, mURL);
		httpRequestEntry.setAttribute(ATTR_NAME_HTTP_METHOD, "POST");
		httpRequestEntry.setAttribute(ATTR_NAME_HTTP_CONTENT_TYPE, "text/xml");
		httpRequestEntry.setAttribute(ATTR_NAME_HTTP_BODY, stringWriter
				.toString());

		if (mAuthentiocationMethod.equalsIgnoreCase(HTTP_BASIC_AUTH)) {
			httpRequestEntry
					.setAttribute(ATTR_NAME_HTTP_REMOTE_USER, mUserName);
			httpRequestEntry.setAttribute(ATTR_NAME_HTTP_REMOTE_PASSWORD,
					mPassword);
		}

		mHttpParser.setInputStream(socket.getInputStream());
		mHttpParser.setOutputStream(socket.getOutputStream());
		mHttpParser.writeEntry(httpRequestEntry);
	}

	/**
	 * Reads a DSMLv2 response from a DSML Server and maps the response to an
	 * Entry object.
	 * 
	 * @return The Entry object containing the DSMLv2 response.
	 * @throws Exception
	 *             When an error occurs when receiving the response.
	 */
	private Entry readResponse() throws Exception {

		Entry responseEntry = mHttpParser.readEntry();
		if (responseEntry == null) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.DSML.NORESPONSE.RETURNED.INFO"));
			}
			return null;
		}

		byte[] bodyAsBytes = (byte[]) responseEntry.getObject("http.bodyAsBytes");

		if (bodyAsBytes == null ||bodyAsBytes.length == 0) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.DSML.NOMESSAGEBODY.RETURNED.INFO"));
			}
			return null;
		}

		ByteArrayInputStream inputStream = new ByteArrayInputStream(bodyAsBytes);

		mDsmlv2Parser.setInputStream(inputStream);
		mDsmlv2Parser.initParser();
		Entry resultEntry = mDsmlv2Parser.readEntry();
		mDsmlv2Parser.closeParser();

		return resultEntry;
	}

	/**
	 * Checks if the returned DSMLv2 response indicates normal execution.
	 * 
	 * @param aEntry
	 *            The Entry object representing the DSMlv2 response.
	 * @throws Exception
	 *             When the returned DSMlv2 response indicates an error
	 *             condition.
	 */
	private void checkDsmlResponse(Entry aEntry) throws Exception {
		if (aEntry == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.DSML.NORDSMLESPONSE.RETURNED.INFO"));
		}

		String dsmlError = aEntry
				.getString(Dsmlv2Parser.ATTR_NAME_DSML_ERROR_MESSAGE);
		String dsmlResultCode = aEntry
				.getString(Dsmlv2Parser.ATTR_NAME_DSML_RESULTCODE);
		String dsmlResultDescr = aEntry
				.getString(Dsmlv2Parser.ATTR_NAME_DSML_RESULTDESC);
		int resultCode = 0;
		if (dsmlResultCode != null) {
			try {
				resultCode = Integer.parseInt(dsmlResultCode);
			} catch (NumberFormatException nfe) {
				resultCode = 0;
			}
		}

		String dsmlErrorType = aEntry
				.getString(Dsmlv2Parser.ATTR_NAME_DSML_ERRORTYPE);
		String dsmlMessage = aEntry
				.getString(Dsmlv2Parser.ATTR_NAME_DSML_MESSAGE);

		String dsmlOperation = aEntry
				.getString(Dsmlv2Parser.ATTR_NAME_DSML_OPERATION);
		if (dsmlOperation
				.equalsIgnoreCase(Dsmlv2Parser.OPERATION_ERROR_RESPONSE)) {
			String funcmsg = sResHash.getString(
					"CONNECTOR.DSML.RESPONSE.OPERRMESS.ERROR", new Object[] {
							dsmlOperation, dsmlErrorType, dsmlMessage });
			debug(funcmsg);
			throw new Exception(funcmsg);
		} else if (dsmlOperation
				.equalsIgnoreCase(Dsmlv2Parser.OPERATION_SEARCH_RESPONSE)) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.DSML.SEARCH.RESPONSE.INFO",
						new Object[] { dsmlOperation, dsmlResultCode,
								dsmlResultDescr, dsmlError }));
			}
		} else {
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.DSML.SEARCH.RESPONSE.ERROR", new Object[] {
								dsmlOperation, dsmlResultCode, dsmlResultDescr,
								dsmlError }));
			}
			if (resultCode != 0) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.DSML.SEARCH.RESPONSE.ERROR", new Object[] {
								dsmlOperation, dsmlResultCode, dsmlResultDescr,
								dsmlError }));
			}
		}
	}

	/**
	 * Given a batch search response generates a Vector containing the
	 * individual search result Entries.
	 * 
	 * @param aEntry
	 *            The Entry object containing the batch search response.
	 * @return Vector containing the individual search result Entries.
	 */
	private Vector<?> getResults(Entry aEntry) {

		Attribute attrResults = aEntry
				.getAttribute(Dsmlv2Parser.ATTR_NAME_ACUMULATOR);
		Vector<?> resultVector = null;

		if (attrResults != null && attrResults.getValuesVector() != null
				&& attrResults.getValuesVector().size() != 0) {
			resultVector = attrResults.getValuesVector();
		}

		return resultVector;
	}

	/**
	 * Adds the suffix to the dn of the given Entry if it's missing.
	 * 
	 * @param aEntry
	 *            The Entry object containg the DSMLv2 response.
	 */
	private void setEntryFullDn(Entry aEntry) {

		if (aEntry == null) {
			return;
		}

		if (mSearchBase != null && mSearchBase.length() > 0) {
			String dn = aEntry.getString(Dsmlv2Parser.ATTR_NAME_DN);
			if (!dn.endsWith(mSearchBase)) {
				dn += "," + mSearchBase;
				aEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DN, dn);
			}
		}
	}

	/**
	 * Delta mode supported.
	 * 
	 * @return true
	 */
	public boolean isDeltaSupported() {
		return true;
	}

	/**
	 * Version information.
	 * 
	 * @return version information.
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}
}
