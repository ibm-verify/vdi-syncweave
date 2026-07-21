/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.ibm.daml.jndi.DAMLContextFactory;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;

/**
 * The ITIM Agent Connector is an adapter between the TDI Connector API and the
 * ITIM DAML JNDI package. Rather than using the built-in TDI JNDI Connector,
 * this Connector allows the specifics of configuration to be controlled and can
 * hide details in the JNDI setup that are irrelevant when talking to a DAML
 * endpoint.
 */
public class ITIMAgentConnector extends Connector implements ConnectorInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "itimagentconnector";

	/**
	 * Distinguish name.
	 */
	private static final String NAMING_ATTRIBUTE_NAME = "$dn";

	/**
	 * Parameter name.
	 */
	private static final String PARAM_AGENT_URL = "agentUrl";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_AGENT_USER = "userName";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_AGENT_PASSWORD = "password";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_CONNECTION_RETRY_COUNT = "connRetryCount";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_SEARCH_FILTER = "searchFilter";

	public static final String PARAMETER_SEARCH_BASE = "searchBase";

	/**
	 * Name of the connector.
	 */
	private static final String CONNECTOR_NAME = "ITIM Agent Connector";

	/**
	 * The current result set.
	 */
	private NamingEnumeration<SearchResult> mResultSet;

	/**
	 * The JNDI context.
	 */
	private DirContext mCtx;

	/**
	 * Directory search scope used when selecting and looking up Entries.
	 */
	private int mSearchScope = SearchControls.SUBTREE_SCOPE;

	/**
	 * Directory search base used when selecting and looking up Entries.
	 */
	private String mSearchBase = "";

	/**
	 * Search filter used in Iterator mode.
	 */
	private String mSearchFilter = "(objectclass=*)";
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Constructor of the ITIM Agent Connector.
	 */
	public ITIMAgentConnector() {
		setName(CONNECTOR_NAME);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE, ConnectorConfig.DELETE_MODE, ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.LOOKUP_MODE, ConnectorConfig.UPDATE_MODE });
	}

	/**
	 * Reads Connector parameters and creates the JNDI context.
	 * 
	 * @param aObject
	 *            - ignored
	 * @throws Exception
	 *             if a required parameter is missing or the JNDI context
	 *             initialization fails.
	 */
	public void initialize(Object aObject) throws Exception {
		Hashtable<String, String> env = new Hashtable<String, String>();

		String agentUrl = getParam(PARAM_AGENT_URL);
		checkRequiredParameter(agentUrl, PARAM_AGENT_URL);
		env.put(Context.PROVIDER_URL, agentUrl);

		String userName = getParam(PARAM_AGENT_USER);
		checkRequiredParameter(userName, PARAM_AGENT_USER);
		env.put(Context.SECURITY_PRINCIPAL, userName);

		String password = getParam(PARAM_AGENT_PASSWORD);
		checkRequiredParameter(password, PARAM_AGENT_PASSWORD);
		env.put(Context.SECURITY_CREDENTIALS, password);

		// String certFile = getParam(PARAM_AGENT_CERTFILE);
		// checkRequiredParameter(certFile, PARAM_AGENT_CERTFILE);
		// certFile = certFile.trim();
		// File file = new File(certFile);
		// env.put("com.ibm.daml.jndi.DAMLContext.CA_CERT_DIR",
		// file.getParent());

		String retryCount = getParam(PARAM_CONNECTION_RETRY_COUNT);
		if (retryCount != null) {
			env.put("com.ibm.daml.jndi.DAMLContext.CONNECTION_RETRY_COUNT", retryCount);
		}

		if (hasConfigValue(PARAM_SEARCH_FILTER)) {
			mSearchFilter = getParam(PARAM_SEARCH_FILTER).trim();
			if (!mSearchFilter.startsWith("(")) {
				mSearchFilter = "(" + mSearchFilter;
			}
			if (!mSearchFilter.endsWith(")")) {
				mSearchFilter = mSearchFilter + ")";
			}
		}

		mSearchBase = getParam(PARAMETER_SEARCH_BASE);
		if (mSearchBase == null)
			mSearchBase = "";

		// we need to disable enroleAgent.jar pooling as otherwise we see some
		// major consistency problems in it.
		env.put("com.ibm.daml.jndi.DAMLContext.POOL_MAX_SIZE", "0");

		// The JNDI (DAML) initial context
		DAMLContextFactory factory = new DAMLContextFactory();

		// get a handle to an Initial DirContext
		mCtx = (DirContext) factory.getInitialContext(env);
	}

	/**
	 * Verifies that a required Connector parameter is non empty.
	 * 
	 * @param aParamValue
	 *            value to be checked
	 * @param aParamName
	 *            name of the parameter
	 * @throws Exception
	 *             if the value is <code>null</code> or the size is '0'
	 */
	private void checkRequiredParameter(String aParamValue, String aParamName) throws Exception {
		if ((aParamValue == null) || (aParamValue.trim().length() == 0)) {
			String funcmsg = sResHash.getString("CONNECTOR.ITIMAGENT.REQUIRED.PARAMETER.MISSING", aParamName);
			logmsg(funcmsg);
			throw new Exception(funcmsg);
		}
	}

	/**
	 * Performs a JNDI search operation with the search controls specified.
	 * 
	 * @throws Exception
	 *             if the search operation fails.
	 */
	public void selectEntries() throws Exception {
		SearchControls controls = new SearchControls();

		controls.setSearchScope(mSearchScope);
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.ITIMAGENT.SELECTENTRIES.WILL.PERFORM.AGENT.SEARCH.WITH.FILTER", mSearchFilter));
		}

		mResultSet = mCtx.search(mSearchBase, mSearchFilter, controls);

		if (mResultSet != null) {
			if (mResultSet.hasMore()) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.ITIMAGENT.SELECTENTRIES.SEARCH.RETURNED.AT"));
				}
			} else {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.ITIMAGENT.SELECTENTRIES.NO.ENTRIES.RETURNED"));
				}
				mResultSet = null;
			}
		}
	}

	/**
	 * Retrieves the next Entry object from the search results.
	 * 
	 * @return the next entry or <code>null</code> if no more entries are found.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Entry getNextEntry() throws Exception {
		Entry entry = null;

		if ((mResultSet != null) && mResultSet.hasMore()) {
			SearchResult result = mResultSet.next();
			entry = searchResultToEntry(result);
		} else {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.ITIMAGENT.GETNEXTENTRY.NO.MORE.ENTRIES.TO"));
			}
		}

		return entry;
	}

	/**
	 * Converts Search Result object to Directory Integrator Entry object.
	 * 
	 * @param aSearchResult
	 *            {@link SearchResult} instance
	 * @return Entry
	 * @throws NamingException
	 */
	private Entry searchResultToEntry(SearchResult aSearchResult) throws NamingException {
		Entry entry = null;

		if ((aSearchResult != null) && (aSearchResult.getName() != null)) {
			entry = new Entry();
			entry.addAttributeValue(NAMING_ATTRIBUTE_NAME, aSearchResult.getName());
			populateEntry(entry, aSearchResult.getAttributes());
		}

		return entry;
	}

	/**
	 * Adds Attributes to a Directory Integrator Entry from a JNDI Attributes
	 * object.
	 * 
	 * @param aEntry
	 *            {@link Entry} instance
	 * @param aAttrs
	 *            {@link Attributes} instance
	 * 
	 * @return The number of Attributes added to the Entry object.
	 * @throws NamingException
	 */
	private int populateEntry(Entry aEntry, Attributes aAttrs) throws NamingException {
		int attributesAdded = 0;

		NamingEnumeration<String> attributeNames = aAttrs.getIDs();

		while (attributeNames.hasMore()) {
			String name = attributeNames.next();
			Attribute attr = aAttrs.get(name);

			// Get all of the attribute's values and stuff them into the entry
			NamingEnumeration<?> values = attr.getAll();
			while (values.hasMore()) {
				aEntry.addAttributeValue(name, values.next());
			}

			attributesAdded++;
		}

		return attributesAdded;
	}

	/**
	 * Adds the given Entry as a new JNDI Subcontext.
	 * 
	 * @param aEntry
	 *            the entry to add
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void putEntry(Entry aEntry) throws Exception {
		// Get the DN from the entry
		String dn = aEntry.getString(NAMING_ATTRIBUTE_NAME);

		if (dn != null) {
			// get the entry attributes into an Attributes object
			Attributes attrs = entryToAttr(aEntry);
			attrs.remove(NAMING_ATTRIBUTE_NAME);

			// create the new JNDI Entry
			try {
				mCtx.createSubcontext(dn, attrs);
			} catch (Exception e) {
				logmsg(sResHash.getString("CONNECTOR.ITIMAGENT.PUTENTRY.COULD.NOT.ADD.NEW.ENTRY", e.toString()));
				throw e;
			}
		} else {
			String funcmsg = sResHash.getString("CONNECTOR.ITIMAGENT.NO.NAMING.ATTRIBUTE.SPECIFIED");
			logmsg(funcmsg);
			throw new Exception(funcmsg);
		}
	}

	/**
	 * Convert Entry Attributes to a BasicAttributes object.
	 * 
	 * @param aEntry
	 *            {@link Entry} instance
	 * @return {@link BasicAttributes} instance.
	 */
	private BasicAttributes entryToAttr(Entry aEntry) {
		BasicAttributes retAttrs = new BasicAttributes();

		for (String name: aEntry.getAttributeNames()) {
			retAttrs.put(attrToBasicAttr(aEntry.getAttribute(name)));
		}

		return retAttrs;
	}

	/**
	 * Convert Entry Attribute to a BasicAttribute object.
	 * 
	 * @param entryAttr
	 *            {@link Attribute} instance
	 * @return {@link BasicAttribute} instance.
	 */
	private BasicAttribute attrToBasicAttr(com.ibm.di.entry.Attribute entryAttr) {
		BasicAttribute basicAttr = new BasicAttribute(entryAttr.getName());
		for (int j = 0; j < entryAttr.size(); j++) {
			basicAttr.add(entryAttr.getValue(j));
		}

		return basicAttr;
	}

	/**
	 * Modifies a JNDI Entry.
	 * 
	 * @param aEntry
	 *            the entry, result of the Output Mapping
	 * @param aSearch
	 *            the search criteria used to find the entry in the back-end
	 *            server.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void modEntry(Entry aEntry, SearchCriteria aSearch) throws Exception {
		Entry entry = findEntry(aSearch);
		if (entry == null) {
			throw new Exception(sResHash.getString("CONNECTOR.ITIMAGENT.MODENTRY.ENTRY.FOR.MODIFICATION"));
		}

		modEntry(aEntry, aSearch, entry);
	}

	/**
	 * Modifies a JNDI Entry.
	 * 
	 * @param aEntry
	 *            the entry, result of the Output Mapping
	 * @param aSearch
	 *            the search criteria used to find the entry in the back-end
	 *            server.
	 * @param aOldEntry
	 *            the found entry in the back-end server.
	 * @throws Exception
	 *             if the old entry is <code>null</code>, a distinguished name
	 *             is not provided or other type of an error occurs.
	 * 
	 */
	public void modEntry(Entry aEntry, SearchCriteria aSearch, Entry aOldEntry) throws Exception {
		if (aOldEntry == null) {
			throw new Exception(sResHash.getString("CONNECTOR.ITIMAGENT.MODENTRY.MISSING.ENTRY.FOR.MODIFICATION"));
		}

		String dn = aEntry.getString(NAMING_ATTRIBUTE_NAME);
		String oldDn = aOldEntry.getString(NAMING_ATTRIBUTE_NAME);

		if (dn == null && oldDn != null) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.ITIMAGENT.MODENTRY.NO.DISTINGUISHED.NAME"));
			}
			dn = oldDn;
		}

		if (dn == null) {
			throw new Exception(sResHash.getString("CONNECTOR.ITIMAGENT.MODENTRY.MISSING.NAMING.ATTRIBUTE"));
		}

		if (!equalsDN(dn, oldDn)) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.ITIMAGENT.MODENTRY.RENAME.ENTRY.FROM.TO", new Object[] { oldDn, dn }));
			}
			mCtx.rename(oldDn, dn);
		}

		BasicAttributes basicAttrs = entryToAttr(aEntry);

		ModificationItem[] mods = null;
		int modsIdx = 0;

		try {
			basicAttrs.remove(NAMING_ATTRIBUTE_NAME);

			// get modification items count
			int count = 0;
			for (Enumeration<Attribute> e = basicAttrs.getAll(); e.hasMoreElements();) {
				Attribute attr = e.nextElement();

				if (attr.size() < 1 && aOldEntry.getAttribute(attr.getID()) == null) {
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.ITIMAGENT.MODENTRY.IGNORING.ATTRIBUTE", attr.getID()));
					}
				} else {
					count++;
				}
			}

			if (count == 0) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.ITIMAGENT.MODENTRY.NO.MODIFICATION.ITEMS.FOR.ENTRY", dn));
				}
				throw new Exception(sResHash.getString("CONNECTOR.ITIMAGENT.NO.MODIFICATION.ITEMS"));
			}

			mods = new ModificationItem[count];

			for (Enumeration<Attribute> e = basicAttrs.getAll(); e.hasMoreElements();) {
				Attribute attr = e.nextElement();

				// if empty value then delete the attribute
				if (attr.size() < 1) {
					// but only if the server already has a value for the attribute
					com.ibm.di.entry.Attribute oldAttr = aOldEntry.getAttribute(attr.getID());
					if (oldAttr != null) {
						mods[modsIdx++] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attrToBasicAttr(oldAttr));
					}
				} else {
					mods[modsIdx++] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);
				}
			}
		} catch (Exception e) {
			logmsg(sResHash.getString("CONNECTOR.ITIMAGENT.MODENTRY.ERROR.BUILDING.MODIFICATION.ITEMS", e.toString()));
			throw e;
		}

		// make the modification
		mCtx.modifyAttributes(workAroundDN(dn), mods);
	}

	/**
	 * Check if two distinguished names are equal.
	 * 
	 * @param aDn1
	 *            String
	 * @param aDn2
	 *            String
	 * @return <code>true</code> if the strings are equal.
	 */
	private boolean equalsDN(String aDn1, String aDn2) {
		// Quick comparison
		if (aDn1.compareToIgnoreCase(aDn2) == 0) {
			return true;
		}

		// Trim insignificant spaces
		String d1 = compactDN(aDn1);
		String d2 = compactDN(aDn2);

		return (d1.compareToIgnoreCase(d2) == 0);
	}

	/**
	 * Clears leading spaces in DN parts.
	 * 
	 * @param aDn
	 *            String to be modified
	 * @return modified String.
	 */
	private String compactDN(String aDn) {
		StringBuffer strBuf = new StringBuffer();
		int i = 0;
		while (i < aDn.length()) {
			strBuf.append(aDn.charAt(i));
			if (aDn.charAt(i) == ',') {
				while ((i + 1 < aDn.length()) && (aDn.charAt(i + 1) == ' ')) {
					i++;
				}
			}
			i++;
		}
		return strBuf.toString();
	}

	/**
	 * Deletes a JNDI Entry.
	 * 
	 * @param aEntry
	 *            the entry, result of the Otuput Mapping
	 * @param aSearch
	 *            the search criteria used to find the entry to delete.
	 * @throws Exception
	 *             if the <code>aEntry</code> parameter is <code>null</code>,
	 *             the distinguished name is not provided or other type of an
	 *             error occurs.
	 */
	public void deleteEntry(Entry aEntry, SearchCriteria aSearch) throws Exception {
		if (aEntry == null) {
			// delete all entries according to search criteria
			String funcmsg = sResHash.getString("CONNECTOR.ITIMAGENT.ERROR.DELETION.OF.MULTIPLE.ENTRIES");
			logmsg(funcmsg);
			throw new Exception(funcmsg);
		}

		String dn = (String) aEntry.getString(NAMING_ATTRIBUTE_NAME);
		if (dn == null) {
			throw new Exception(sResHash.getString("CONNECTOR.ITIMAGENT.DELETEENTRY.DELETEENTRY.CALLED.WITH.NO.ATTRIBUTE"));
		}

		// delete the Entry
		mCtx.destroySubcontext(workAroundDN(dn));
	}

	/**
	 * Finds a JNDI Entry given search criteria.
	 * 
	 * @param aSearch
	 *            the search criteria used to find the entry.
	 * 
	 * @return the found entry object, or <code>null</code>.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Entry findEntry(SearchCriteria aSearch) throws Exception {
		// clear list of multiple entries found
		clearFindEntries();

		SearchControls constraints = new SearchControls();

		if (NAMING_ATTRIBUTE_NAME.equalsIgnoreCase(aSearch.getFirstCriteriaName())) {
			// search for all the objects
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

			Vector<String> dnList = new Vector<String>();
			// get all the $dn values from the search criteria
			addDnFromCriteria(dnList, aSearch.getCriteria());

			for (int i = 0; i < dnList.size(); i++) {
				String checkDN = dnList.elementAt(i);
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.ITIMAGENT.FINDENTRY.SEARCH.FOR.CHECKDN", new Object[] { checkDN }));
				}

				try {
					NamingEnumeration<SearchResult> results = mCtx.search(mSearchBase, "(" + checkDN + ")", constraints);
					if ((results == null || !results.hasMore()) && (debugMode())) {
						debug(sResHash.getString("CONNECTOR.ITIMAGENT.DID.NOT.RETURN.ANY.ENTRIES", checkDN));
					}

					while (results != null && results.hasMore()) {
						Entry entry = searchResultToEntry(results.next());
						if (entry != null) {
							if (debugMode()) {
								debug(sResHash.getString("CONNECTOR.ITIMAGENT.FINDENTRY.ENTRY.FOUND", entry.toString()));
							}
							if (!addFindEntry(entry)) {
								logmsg(sResHash.getString("CONNECTOR.ITIMAGENT.FINDENTRY.COULD.NOT.ADD.FOUND.ENTRY", entry
										.toString()));
							}
						} else {
							if (debugMode()) {
								debug(sResHash.getString("CONNECTOR.ITIMAGENT.FINDENTRY.NULL.ENTRY.RETURNED"));
							}
						}
					}
				} catch (javax.naming.PartialResultException e) {
					logmsg(sResHash.getString("CONNECTOR.ITIMAGENT.FINDENTRY.PARTIALRESULTEXCEPTION", e.toString()));
				} catch (javax.naming.NameNotFoundException e) {
					// must catch this since it is a search operation
					logmsg(sResHash.getString("CONNECTOR.ITIMAGENT.FINDENTRY.NAMENOTFOUNDEXCEPTION", e.toString()));
				}
			}
		} else {
			String filter = aSearch.getLDAPFilter();

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.ITIMAGENT.FINDENTRY.FILTER.IN.BASE", new Object[] { filter, mSearchBase }));
			}
			constraints.setSearchScope(mSearchScope);

			try {
				NamingEnumeration<SearchResult> results = mCtx.search(mSearchBase, filter, constraints);
				if ((results == null || !results.hasMore()) && (debugMode())) {
					debug(sResHash.getString("CONNECTOR.ITIMAGENT.DID.NOT.RETURN.ANY.ENTRIES.2", filter));
				}

				while (results != null && results.hasMore()) {
					Entry entry = searchResultToEntry(results.next());
					if (entry != null) {
						if (debugMode()) {
							debug(sResHash.getString("CONNECTOR.ITIMAGENT.FINDENTRY.ENTRY.FOUND.2", entry.toString()));
						}
						if ((!addFindEntry(entry)) && (debugMode())) {
							logmsg(sResHash
									.getString("CONNECTOR.ITIMAGENT.FINDENTRY.COULD.NOT.ADD.FOUND.ENTRY.2", entry.toString()));
						}
					} else {
						if (debugMode()) {
							debug(sResHash.getString("CONNECTOR.ITIMAGENT.FINDENTRY.NULL.ENTRY.RETURNED.2"));
						}
					}
				}
			} catch (javax.naming.PartialResultException e) {
				logmsg(sResHash.getString("CONNECTOR.ITIMAGENT.FINDENTRY.PARTIALRESULTEXCEPTION.2", e.toString()));
			} catch (javax.naming.NameNotFoundException e) {
				logmsg(sResHash.getString("CONNECTOR.ITIMAGENT.FINDENTRY.NAMENOTFOUNDEXCEPTION.2", e.toString()));
			}
		}

		if (getFindEntryCount() == 1) {
			return getFirstFindEntry();
		} else {
			return null;
		}
	}

	/**
	 * This method recursively calls itself to traverse the SearchCriteria
	 * vector held by a search object. The intenetion is to extract all $dn
	 * values and also abort if the search object contains other attributes than
	 * $dn. This is because we cannot/will not perform internal searching of
	 * entries returned by a $dn read.
	 * 
	 * @param aDnList
	 * @param aCrit
	 * @throws Exception
	 *             if an error occurs.
	 * 
	 */
	private void addDnFromCriteria(Vector<String> aDnList, Object aCrit) throws Exception {
		if (aCrit instanceof Vector) {
			for (int i = 0; i < ((Vector<?>) aCrit).size(); i++) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.ITIMAGENT.ADDDNFROMCRITERIA.VECTOR.INSTANCE"));
				}
				addDnFromCriteria(aDnList, ((Vector<?>) aCrit).elementAt(i));
			}
		} else if (aCrit instanceof SearchCriteria) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.ITIMAGENT.ADDDNFROMCRITERIA.SEARCHCRITERIA"));
			}
			addDnFromCriteria(aDnList, ((SearchCriteria) aCrit).getCriteria());
		} else if (aCrit instanceof SearchCriteria.rscSearch) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.ITIMAGENT.ADDDNFROMCRITERIA.SEARCHCRITERIARSCSEARCH"));
			}
			if (!((SearchCriteria.rscSearch) aCrit).name.equals(NAMING_ATTRIBUTE_NAME)) {
				throw new Exception(sResHash.getString("CONNECTOR.ITIMAGENT.CANNOT.MIX.DN.MATCHING.WITH.OTHER"));
			}

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.ITIMAGENT.ADDDNFROMCRITERIA.ADD.DN.VALUE",
						((SearchCriteria.rscSearch) aCrit).value.toString()));
			}
			aDnList.add(((SearchCriteria.rscSearch) aCrit).value.toString());
		} else {
			throw new Exception(sResHash.getString("CONNECTOR.ITIMAGENT.UNKNOWN.OBJECT.TYPE.IN.SEARCH.FILTER", new Object[] {
					aCrit.getClass().getName(), aCrit }));
		}
	}

	/**
	 * Closes the JNDI context.
	 */
	public void terminate() {
		try {
			if (mCtx != null) {
				mCtx.close();
				mCtx = null;
				mResultSet = null;
			}
		} catch (NamingException e) {
			logmsg(sResHash.getString("CONNECTOR.ITIMAGENT.EXCEPTION.ON.TERMINATE", e.toString()));
		}
	}

	/**
	 * Version information.
	 * 
	 * @return the version information.
	 */
	public String getVersion() {
		return "2.4-di7.1.1 %I%, 20%E%";
	}
	
	/**
	 * Create a DN that will work for the current version of the ITIM library.
	 * There is a bug in the current ITIM library, where it will fail to parse
	 * a DN if it is not in the format
	 * <delimiter>|<objectclass>:<objectclass>:...:<objectclass><delimiter><name>
	 * We cannot fix the ITIM library, so instead we try to add the magic token that is needed.
	 * @param dn The DN that may need a magic token
	 * @return The DN with the token added (if needed)
	 */
	private String workAroundDN(String dn) {
		// First check if the dn has already been "fixed"
		if (dn.contains("|")) {
			String token = dn.substring(0, dn.indexOf('|'));
			if (token.length() > 0) {
				if (dn.substring(token.length() + 1).contains(token))
					return dn;
			} else {
				return dn; // TODO: Probably this is an error, not sure what we can do.
			}
		}
		// Add a magic token, this is good enough
		return "@|@" + dn;
	}
}
