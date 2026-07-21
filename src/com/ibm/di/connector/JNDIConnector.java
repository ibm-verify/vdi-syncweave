/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.naming.CannotProceedException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.LimitExceededException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;
import javax.naming.directory.Attribute;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.icu.util.StringTokenizer;

/**
 * The JNDI Connector provides access to a variety of JNDI services; it uses the
 * javax.naming and javax.naming.directory packages to work with different
 * directory services. To reach a specific system, you must install the JNDI
 * driver for that system, for example com.sun.jndi.ldap.LdapCtxFactory for
 * LDAP. The driver is typically distributed as one or more jar or zip files.
 * Place these file in a place where the Java(TM) runtime can reach them, for
 * example, in the <TDI_install>/lib/ext directory. This Connector supports
 * Delta Tagging at the Attribute level. This means that provided a previous
 * Connector in the AssemblyLine has provided Delta information at the Attribute
 * level, the JNDI Connector will be able to use it in order to make the changes
 * needed in the target JNDI directory. When using the JNDI Connector for
 * querying an LDAP Server, a SizeLimitExceededException may occur if the number
 * of entries satisfying the search criteria is greater than the maximum limit
 * set by the LDAP Server. To work around this situation, either increase the
 * LDAP Server's maximum result limit, or set the java.naming.batchsize provider
 * parameter to some value smaller than the maximum limit of the server. For
 * more information on the java.naming.batchsize parameter refer to:
 * http://java.sun.com/products/jndi/tutorial/ldap/search/batch.html
 * 
 */
public class JNDIConnector extends Connector implements ConnectorInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "jndiconnector";

	/**
	 * Private class used for retrieving the next Entry.
	 */
	private static class jndiIterator {
		/**
		 * First character of the search string
		 */
		private char firstChar;
		/**
		 * Last character of the search string
		 */
		private char lastChar;

		/**
		 * search string
		 */
		private StringBuffer search;

		/**
		 * Class constructor
		 * 
		 * @param track
		 *            the tracker string.
		 */
		public jndiIterator(String track) {
			firstChar = track.charAt(0);
			lastChar = track.charAt(1);
			search = new StringBuffer("");
			increase();
		}

		/** Increase size */
		public void increase() {
			search.append((char) (firstChar - 1));
		}

		/** Decrease size */
		public void decrease() {
			search.delete(search.length() - 1, 1);
		}

		/** @return the next one */
		public String next() {
			int index = search.length() - 1;
			char ch = search.charAt(index);

			search.setCharAt(index, ++ch);

			if (ch > lastChar) {
				if (index == 0) {
					return null;
				} else {
					decrease();
					return next();
				}
			}

			return search.toString();
		}
	}

	/**
	 * Name of the component.
	 */
	private static final String myName = "Generic JNDI Connector";

	/**
	 * {@link DirContext}
	 */
	private DirContext ctx;

	/**
	 * An enumeration of SearchResults for the objects that satisfy the filter.
	 */
	private NamingEnumeration<SearchResult> results;

	/**
	 * Current search result.
	 */
	private SearchResult current;

	/**
	 * {@link jndiIterator} object, used for retrieving the next Entry.
	 */
	private jndiIterator pump = null;

	/**
	 * The attribute (in conn and work entries) to denote the JNDI name.
	 */
	private String nameParameter = "$dn";

	/**
	 * Used to map oid to name
	 */
	private Hashtable<String, String> attributeSyntaxMap;

	/**
	 * Filter expression to use for search
	 */
	private String mSearchFilterAll = "objectClass=*";
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Constructor.
	 */
	public JNDIConnector() {
		setName(myName);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.DELETE_MODE, ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.LOOKUP_MODE, ConnectorConfig.UPDATE_MODE,
				ConnectorConfig.DELTA_MODE });
	}

	/**
	 * Closes the context.
	 */
	@Override
	public void terminate() {
		if (ctx != null) {
			try {
				ctx.close();
			} catch (NamingException ignore) {
			}
		}
	}

	/**
	 * Initializes {@link DirContext}.
	 */
	@Override
	public void initialize(Object o) throws Exception {
		Hashtable<String, String> env = new Hashtable<String, String>();

		// In case of size-limit
		String[] providerParams = { "java.naming.factory.initial",
				"java.naming.provider.url", "java.naming.referral",
				"java.naming.batchsize", "java.naming.security.authentication",
				"java.naming.security.principal",
				"java.naming.security.credentials" };

		if (getParam("ldapUseSSL") != null
				&& getParam("ldapUseSSL").equals("true")) {
			env.put(Context.SECURITY_PROTOCOL, "ssl");
			env.put("java.naming.ldap.factory.socket",
					"javax.net.ssl.SSLSocketFactory");
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JNDI.USING.LDAP.CONNECTION.INFO"));
			}
		}

		// Standard JNDI parameters
		for (int i = 0; i < providerParams.length; i++) {
			String param = (String) getParam(providerParams[i]);
			// If anonymous, we do not want more params
			if (i == 4 && "Anonymous".equalsIgnoreCase(param))
				break;
			if (param != null) {
				env.put(providerParams[i], param);
			}
		}

		// Name parameter
		String s = getParam("jndiNameParameter");
		if (s != null && s.trim().length() > 0)
			nameParameter = s.trim();

		// Provide parameters
		String str = getParam("jndiExtraProviderParams");
		String sep = getParam("paramSeparator");
		if (sep == null)
			sep = ":";

		StringTokenizer st = new StringTokenizer(str == null ? "" : str, "\r\n");
		while (st.hasMoreTokens()) {

			String nt = st.nextToken();
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.JNDI.NEXT.JNDI.CONTEXT.INFO", nt));
			}
			if (nt.length() < 1)
				continue;

			StringTokenizer s2 = new StringTokenizer(nt, sep);
			if (!s2.hasMoreTokens()) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.JNDI.BAD.EXTRAPROVIDER.PARAMETER.ERROR", nt));
			}

			String param = s2.nextToken();
			String value = "";
			if (s2.hasMoreTokens())
				value = s2.nextToken();

			env.put(param, value);
		}

		// get a handle to an Initial DirContext
		ctx = new InitialDirContext(env);
	}

	/**
	 * Prepare the Connector for sequential read. Begin retrieving records
	 * matching the Search Base
	 * 
	 * @exception Exception
	 *                Any Exception by the underlying library
	 */
	@Override
	public void selectEntries() throws Exception {
		SearchControls constraints = new SearchControls();

		String scope = getParam("jndiSearchScope");
		if (scope == null || scope.compareToIgnoreCase("subtree") == 0)
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		if (scope != null && scope.compareToIgnoreCase("onelevel") == 0)
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);

		results = null;

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.JNDI.SELECT.ENTRIES.INFO",
					new Object[] { getParam("jndiSearchBase"),
							getParam("jndiSearchFilter") }));
		}
		try {
			results = ctx.search(getParam("jndiSearchBase"),
					getParam("jndiSearchFilter"), constraints);
			if (results.hasMore()) {
				if (debugMode()) {
					debug(sResHash
							.getString("CONNECTOR.JNDI.SELECT.ENTRIES.HASMORETRUE.INFO"));
				}
			} else {
				if (debugMode()) {
					debug(sResHash
							.getString("CONNECTOR.JNDI.SELECT.ENTRIES.NOENTRIES.INFO"));
				}
				results = null;
			}
			return;
		} catch (javax.naming.LimitExceededException lee) {
			String pmp = getParam("ldapPump");
			if (pmp != null) {
				pump = new jndiIterator(pmp);
				results = null;
				return;
			} else {
				if (debugMode()) {
					debug(sResHash
							.getString("CONNECTOR.JNDI.THROWLIMIT.EXCEEDED.INFO"));
				}
				throw lee;
			}
		} catch (NamingException e) {
			logmsg(sResHash.getString("CONNECTOR.JNDI.CATCHALL.WARNING", e
					.toString()));
			throw e;
		}
	}

	/**
	 * Internal method used for searching a directory.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void searchDirectory() throws Exception {
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.JNDI.SEARCHDIRECTORY.INFO"));
		}

		SearchControls constraints = new SearchControls();

		String scope = getParam("jndiSearchScope");
		if (scope == null || scope.compareToIgnoreCase("subtree") == 0)
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		if (scope != null && scope.compareToIgnoreCase("onelevel") == 0)
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);

		while (true) {
			String flt = pump.next();
			if (flt == null) {
				results = null;
				pump = null;
				return;
			}

			String filter = "cn=" + flt + "*";
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JNDI.SEARCHDIRFILTER.INFO",
						filter));
			}
			try {
				results = ctx.search(getParam("jndiSearchBase"), filter,
						constraints);
				if (results.hasMore())
					return;
			} catch (LimitExceededException lee) {
				pump.increase();
			}
		}
	}

	/**
	 * Returns the next Entry from the connector. The entry is populated with
	 * attributes and values from the next entry in the input set.
	 * 
	 * @return - the next Entry, or null if no more data
	 * @see #selectEntries()
	 * @throws Exception
	 *             if an error occurs.
	 */
	@Override
	public Entry getNextEntry() throws Exception {

		if (results == null && pump != null)
			searchDirectory();

		if (results == null)
			return null;

		current = results.next();
		if (!results.hasMore())
			results = null;
		return getCurrentEntry();
	}

	/**
	 * Retrieves current entry.
	 * 
	 * @return an Entry which contains the current search result
	 */
	public Entry getCurrentEntry() {
		if (current == null)
			return null;
		return sr2entry(current);
	}

	/**
	 * Adds a new entry to the data source. The entry parameter must have a $dn
	 * Attribute, specifying the distinguished name.
	 * 
	 * @param entry
	 *            An Entry populated with values that are to be sent to the LDAP
	 *            server.
	 * @exception Exception
	 *                If there is no distinguished name
	 */
	@Override
	public void putEntry(Entry entry) throws Exception {
		BasicAttributes attrs = entry2attrs(entry);

		if (entry.getString(nameParameter) == null) {
			throw new com.ibm.di.exceptions.NonFatalException(sResHash
					.getString("CONNECTOR.JNDI.CANNOTADDNONAME.PARAM.ERROR",
							nameParameter));
		}

		attrs.remove(nameParameter);
		ctx.createSubcontext(entry.getString(nameParameter), attrs);
	}

	/**
	 * Modifies an existing entry. The new entry data is given by the <i>entry</i>
	 * parameter and the search criteria specifies which entry to modify. This
	 * call is equivalent to modEntry(entry, search, findEntry(search))
	 * 
	 * @param entry
	 *            The entry data
	 * @param search
	 *            The search criteria used to locate the entry to be modified
	 * @exception Exception
	 *                if an error occurs.
	 */
	@Override
	public void modEntry(Entry entry, SearchCriteria search) throws Exception {
		modEntry(entry, search, findEntry(search));
	}

	/**
	 * Modify an Object in the DirContext. The supplied entry should contain a
	 * $dn Attribute with the distinguished name. If it does not, then either
	 * the SearchCriteria must be $dn equals some value, or the old Entry must
	 * contain a $dn Attribute. If the $dn Attribute in entry and old are
	 * different, we will try to rename the object in the DirContext. The easy
	 * way to use this method is to populate entry with the values you want to
	 * modify, and in particular supply a distinguished name. $dn Attribute, and
	 * let search and old be null.
	 * 
	 * @param entry
	 *            An Entry containing the new values to be set in the LDAP
	 *            Server
	 * @param search
	 *            Only used if there is no $dn Attribute in entry.
	 * @param old
	 *            The old values, used to supply $dn if not present in Entry.
	 * 
	 * @exception Exception
	 *                If no distinguished name can be found
	 */
	@Override
	public void modEntry(Entry entry, SearchCriteria search, Entry old)
			throws Exception {
		if (old == null)
			old = entry;

		String dn = entry.getString(nameParameter);

		// Get old DN
		String olddn = old.getString(nameParameter);

		if (dn == null && olddn != null) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JNDI.NODNGIVEN.USEEXISTING.INFO"));
			}
			dn = olddn;
		}

		// Try the search criteria
		if ((dn == null)
				&& (nameParameter.equals(search.getFirstCriteriaName()))) {
			dn = search.getFirstCriteriaValue();
		}

		// If Skip Lookup is used and olddn is left null.
		if (dn != null && olddn == null) {
			olddn = dn;
		}

		if (dn == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.JNDI.NODNGIVEN.ERROR"));
		}

		if (!equalsDN(dn, olddn)) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JNDI.RENAMEENTRY.INFO",
						new Object[] { olddn, dn }));
			}
			ctx.rename(olddn, dn);
		}

		ctx.modifyAttributes(dn, entry2mods(entry, nameParameter));
	}

	/**
	 * Delete an entry. The distinguished name is provided by the $dn Attribute
	 * in the entry parameter. If not found there. the SearchCriteria. must be
	 * of the form $dn equals value.
	 * 
	 * @param entry
	 *            An Entry object containing the distinguished name of the entry
	 *            to delete.
	 * @param search
	 *            Used if the entry parameter is null, or does not contain a
	 *            distinguished name.
	 * @exception Exception
	 *                If no distinguished name can be found.
	 */
	@Override
	public void deleteEntry(Entry entry, SearchCriteria search)
			throws Exception {
		String dn = null;

		if (entry != null) {
			dn = entry.getString(nameParameter);
		}

		// Try the search criteria
		if ((dn == null)
				&& (nameParameter.equals(search.getFirstCriteriaName()))) {
			dn = search.getFirstCriteriaValue();
		}

		if (dn == null) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.JNDI.DELENTRY.NONAMEPARAM.ERROR", nameParameter));
		}

		ctx.destroySubcontext(dn);
	}

	/**
	 * Find an entry matching a SearchCriteria. Returns an entry if exactly one
	 * match is found. If more than one Entry is found, getFindEntryCount() will
	 * say how many matches were found.
	 * 
	 * @param search
	 *            The SearchCriteria containing the values to match.
	 * @return The entry that matches the SearchCriteria
	 * @exception Exception
	 *                Any Exception thrown by the underlying libraries
	 */
	@Override
	public Entry findEntry(SearchCriteria search) throws Exception {
		clearFindEntries();

		SearchControls constraints = new SearchControls();
		NamingEnumeration<SearchResult> results = null;
		String checkDN = null;

		try {
			if (nameParameter.equalsIgnoreCase(search.getFirstCriteriaName())) {
				checkDN = search.getFirstCriteriaValue();
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JNDI.FINDENTRY.INFO",
							new Object[] { nameParameter, checkDN }));
				}

				constraints.setSearchScope(SearchControls.OBJECT_SCOPE);
				results = ctx.search(checkDN, mSearchFilterAll, constraints);
			} else {
				String filter = search.getLDAPFilter();
				if (debugMode()) {
					debug(sResHash.getString(
							"CONNECTOR.JNDI.FINDENTRY.FILTER.INFO", filter));
				}

				constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
				results = ctx.search(getParam("jndiSearchBase"), filter,
						constraints);
			}
		} catch (javax.naming.NameNotFoundException nnf) {
			// Must catch this since it is a search operation
		}

		if (results == null) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JNDI.FINDENTRY.NOENTRIES.INFO"));
			}
			return null;
		}

		while (results.hasMore()) {
			Entry entry = sr2entry(current = results.next());
			if (checkDN != null)
				entry.setAttribute(nameParameter, checkDN);
			addFindEntry(entry);
		}

		if (getFindEntryCount() == 1) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JNDI.FINDENTRY.SINGLEFOUND.INFO"));
			}
			return getFirstFindEntry();
		}

		if (debugMode()) {
			if (getFindEntryCount() == 0) {
				debug(sResHash
						.getString("CONNECTOR.JNDI.FINDENTRY.NOFOUND.INFO"));
			} else {
				debug(sResHash
						.getString("CONNECTOR.JNDI.FINDENTRY.MULTIPLEFOUND.INFO"));
			}
		}

		return null;
	}

	/**
	 * Returns true if the exception is considered to be fatal. This governs
	 * whether the AssemblyLine logs the error as a warning or terminates.
	 * 
	 * @return true if the Exception is one of the following:
	 *         CommunicationException, CannotProceedException,
	 *         LimitExceededException, ServiceUnavailableException
	 */
	@Override
	public boolean isExceptionFatal(Exception e) {
		return e instanceof CommunicationException
				|| e instanceof CannotProceedException
				|| e instanceof LimitExceededException
				|| e instanceof ServiceUnavailableException;
	}

	/**
	 * Adds a given value to an attribute
	 * 
	 * @param moddn
	 *            String representing the DN to which to add the attribute value
	 * @param modattr
	 *            String representing the name of the attribute to add a value
	 *            to
	 * @param modval
	 *            String representing the value of the attribute add
	 * 
	 * @throws java.lang.Exception
	 *             when underlying modify operation fails
	 */
	public void addAttributeValue(String moddn, String modattr, String modval)
			throws Exception {
		try {
			ModificationItem[] mods = new ModificationItem[1];
			Attribute mod = new BasicAttribute(modattr, modval);
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, mod);

			ctx.modifyAttributes(moddn, mods);

			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JNDI.MOD.ADD.SUCCESSFUL.INFO"));
			}
		} catch (AttributeInUseException e) {
			logmsg(sResHash.getString(
					"CONNECTOR.JNDI.ATTRIB.ALLREADY.EXIST.WARNING", e
							.toString()));
			throw (e);
		} catch (NamingException e) {
			logmsg(sResHash.getString("CONNECTOR.JNDI.MOD.ADD.FAILED.WARNING",
					e));
			throw (e);
		}
	}

	/**
	 * replaceAttributeValue: replaces a given attribute with a certain value
	 * 
	 * @param moddn
	 *            String representing the DN to which to replace the attribute
	 *            value
	 * @param modattr
	 *            String representing the name of the attribute to replace the
	 *            value for
	 * @param modval
	 *            String representing the desired value for the attribute
	 * 
	 * @throws java.lang.Exception
	 *             when underlying modify operation fails
	 */
	public void replaceAttributeValue(String moddn, String modattr,
			String modval) throws Exception {
		try {
			ModificationItem[] mods = new ModificationItem[1];
			Attribute mod = new BasicAttribute(modattr, modval);
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod);

			ctx.modifyAttributes(moddn, mods);

			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JNDI.MOD.REPLACE.SUCCESSFUL.INFO"));
			}
		} catch (AttributeInUseException e) {
			logmsg(sResHash.getString(
					"CONNECTOR.JNDI.ATTRIB.ALLREADY.EXIST.WARNING.2", e
							.toString()));
			throw (e);
		} catch (NamingException e) {
			logmsg(sResHash.getString(
					"CONNECTOR.JNDI.MOD.REPLACE.FAILED.WARNING", e));
			throw (e);
		}
	}

	/**
	 * removeAttributeValue: removes a given attribute value from an entry
	 * 
	 * @param moddn
	 *            String representing the DN to which to remove the attribute
	 *            value
	 * @param modattr
	 *            String representing the name of the attribute to change
	 * @param modval
	 *            String representing the value you wish to have removed from
	 *            the attribute
	 * 
	 * @throws java.lang.Exception
	 *             when underlying modify operation fails
	 */
	public void removeAttributeValue(String moddn, String modattr, String modval)
			throws Exception {
		try {
			ModificationItem[] mods = new ModificationItem[1];
			Attribute mod = new BasicAttribute(modattr, modval);
			mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, mod);

			ctx.modifyAttributes(moddn, mods);

			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JNDI.MOD.REMOVE.SUCCESSFUL.INFO"));
			}
		} catch (NamingException e) {
			logmsg(sResHash.getString(
					"CONNECTOR.JNDI.MOD.REMOVE.FAILED.WARNING", e));
			throw (e);
		}
	}

	/**
	 * removeAttribute : removes the attribute
	 * 
	 * @param moddn
	 *            String representing the DN to which to remove the attribute
	 *            values
	 * @param modattr
	 *            String representing the name of the attribute to remove all
	 *            values from
	 * 
	 * @throws java.lang.Exception
	 *             when underlying modify operation fails
	 */
	public void removeAttribute(String moddn, String modattr) throws Exception {
		try {
			ModificationItem[] mods = new ModificationItem[1];
			Attribute mod = new BasicAttribute(modattr, null);
			mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, mod);

			ctx.modifyAttributes(moddn, mods);

			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JNDI.MOD.REM.ATTR.SUCCESSFUL.INFO"));
			}
		} catch (NamingException e) {
			logmsg(sResHash.getString(
					"CONNECTOR.JNDI.MOD.REM.ATTR.FAILED.WARNING", e));
			throw (e);
		}
	}

	/**
	 * Converts {@link SearchResult} to an {@link Entry} object
	 * 
	 * @param sr
	 *            {@link SearchResult}
	 * @return Entry
	 */
	private Entry sr2entry(SearchResult sr) {
		Entry entry = new Entry();

		try {
			for (NamingEnumeration<? extends Attribute> ea = sr.getAttributes()
					.getAll(); ea.hasMore();) {
				BasicAttribute ba = (BasicAttribute) ea.next();
				com.ibm.di.entry.Attribute a = new com.ibm.di.entry.Attribute(
						ba.getID());
				for (NamingEnumeration<?> ev = ba.getAll(); ev.hasMore();) {
					a.addValue(ev.next());
				}
				entry.setAttribute(a);
			}
			String name = sr.getName();
			String base = getParam("jndiSearchBase");

			if (base != null) {
				base = base.trim();
				if (base.length() == 0) {
					base = null;
				}
			}

			if (name != null && name.length() > 0) {
				if (base == null) {
					entry.setAttribute(nameParameter, name);
				} else {
					if (name.startsWith("\"") && name.endsWith("\"")) {
						entry.setAttribute(nameParameter, name.substring(0,
								name.length() - 1)
								+ "," + base + "\"");
					} else {
						entry.setAttribute(nameParameter, name + "," + base);
					}
				}
			} else
				entry.setAttribute(nameParameter, base);

		} catch (NamingException exp) {
			entry.setAttribute("NAMING_EXCEPTION", exp.toString());
		}
		return entry;
	}

	/**
	 * Converts {@link Entry} to a {@link BasicAttributes} object
	 * 
	 * @param entry
	 *            {@link Entry} instance
	 * @return BasucAtt
	 */
	private BasicAttributes entry2attrs(Entry entry) {
		BasicAttributes ba = new BasicAttributes();

		String[] names = entry.getAttributeNames();
		for (int i = 0; i < names.length; i++) {
			BasicAttribute a = new BasicAttribute(names[i]);
			com.ibm.di.entry.Attribute ea = entry.getAttribute(names[i]);
			for (int j = 0; j < ea.size(); j++) {
				a.add(ea.getValue(j));
			}
			ba.put(a);
		}

		return ba;
	}

	/**
	 * Convert {@link Entry} to {@link ModificationItem} array
	 * 
	 * @param entry
	 *            {@link Entry} to convert
	 * @param dnParm
	 *            distinguished name
	 * @return the converted ModificationItem array
	 */
	// Convert entry to ModificationItem Tai 2142
	private ModificationItem[] entry2mods(Entry entry, String dnParm) {
		int opCode = DirContext.REPLACE_ATTRIBUTE;

		String modOperation = (String) entry.getProperty("modOperation");
		if (modOperation == null)
			modOperation = getParam("modOperation");

		if (modOperation != null) {
			if (modOperation.equalsIgnoreCase("delete"))
				opCode = DirContext.REMOVE_ATTRIBUTE;
			else if (modOperation.equalsIgnoreCase("add"))
				opCode = DirContext.ADD_ATTRIBUTE;
			else if (modOperation.equalsIgnoreCase("replace"))
				opCode = DirContext.REPLACE_ATTRIBUTE;
			else
				modOperation = null;
		}

		String[] names = entry.getAttributeNames();
		ArrayList<ModificationItem> mods = new ArrayList<ModificationItem>(
				names.length);

		for (int i = 0; i < names.length; i++) {

			String name = names[i];
			// Don't add distinguished name to the ModificationItem list. DN
			// won't be modified by mod operation.
			if (name.equalsIgnoreCase(dnParm))
				continue;

			com.ibm.di.entry.Attribute ea = entry.getAttribute(name);

			if (modOperation == null) {
				switch (ea.getOper()) {
				case com.ibm.di.entry.Attribute.ATTRIBUTE_ADD:
					opCode = DirContext.ADD_ATTRIBUTE;
					break;
				case com.ibm.di.entry.Attribute.ATTRIBUTE_DELETE:
					opCode = DirContext.REMOVE_ATTRIBUTE;
					break;
				case com.ibm.di.entry.Attribute.ATTRIBUTE_MOD:
					boolean wasAV = false;
					for (int j = 0; j < ea.size(); j++) {
						Object val = ea.getValueAV(j);
						if (val instanceof AttributeValue) {
							wasAV = true;
							AttributeValue av = (AttributeValue) val;
							switch (av.getOper()) {
							case AttributeValue.AV_ADD:
								mods.add(new ModificationItem(DirContext.ADD_ATTRIBUTE,
												new BasicAttribute(name, av.getValue())));
								break;
							case AttributeValue.AV_DELETE:
								mods.add(0, new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
												new BasicAttribute(name, av.getValue())));
								break;
							}
						}
					}
					if (wasAV)
						continue;
					// fall through (cannot happen?)
				default:
					opCode = DirContext.REPLACE_ATTRIBUTE;
				}
			}

			BasicAttribute a = new BasicAttribute(name);
			for (int j = 0; j < ea.size(); j++) {
				Object val = ea.getValueAV(j);
				if (val instanceof AttributeValue) {
					AttributeValue av = (AttributeValue) val;
					if (av.getOper() != AttributeValue.AV_DELETE || opCode == DirContext.REMOVE_ATTRIBUTE )
						a.add(av.getValue());
				} else if (val != null) {
					a.add(val);
				}
			}

			mods.add(new ModificationItem(opCode, a));
		}

		return mods.toArray(new ModificationItem[mods.size()]);
	}

	/**
	 * Determines whether the two parameter are equal ignoring case and
	 * whitespace.
	 * 
	 * @param p1
	 *            String
	 * @param p2
	 *            String
	 * @return <code>true</code> if parameters are equal , <code>false</code>
	 *         otherwise.
	 */
	private boolean equalsDN(String p1, String p2) {
		// Quick comparison
		if (p1 == null) {
			return false;
		}
		if (p1.equalsIgnoreCase(p2)) {
			return true;
		}	

		// Trim insignificant spaces
		String d1 = compactDN(p1);
		String d2 = compactDN(p2);

		if (d1 == null) {
			return false;
		} else {
			return (d1.equalsIgnoreCase(d2));
		}
	}

	/**
	 * Removes insignificant whitespace from dn parameter.
	 * 
	 * @param p1
	 *            String to modify
	 * @return dn without whitespace.
	 */
	private String compactDN(String p1) {
		if (p1 == null) {
			return null;
		}
		StringBuffer str = new StringBuffer();
		int i = 0;
		while (i < p1.length()) {
			str.append(p1.charAt(i));
			if (p1.charAt(i) == ',') {
				while (p1.charAt(i + 1) == ' ') {
					i++;
				}
			}
			i++;
		}
		return str.toString();
	}

	/**
	 * private utility method, used for interpreting a returned schema
	 * 
	 * @param result
	 *            vector containing entries with added syntax.
	 * @param thisEntry
	 *            {@link Entry}
	 * @param schema
	 *            The schema to be added
	 * @param must
	 *            must/may
	 */
	private void addSyntax(Vector<Entry> result, Entry thisEntry,
			DirContext schema, String must) {
		com.ibm.di.entry.Attribute attr = thisEntry.getAttribute(must);
		if (attr != null) {
			for (int i = 0; i < attr.size(); i++) {
				String attrname = (String) attr.getValue(i);
				Entry e = new Entry();
				e.setAttribute("name", attrname);
				e.setAttribute("syntax", must + "/"
						+ getAttributeSyntax(schema, attrname));
				result.add(e);
			}
		}
	}

	/**
	 * Retrieves a Vector with entries , holding all attibute types.
	 * 
	 * @return vector of arrays.
	 * @throws NamingException
	 */
	private Vector<Entry> queryAllAttributeTypes() throws NamingException {

		Vector<Entry> result = new Vector<Entry>();

		DirContext schema = ctx.getSchema("");

		if (schema != null) {
			DirContext attributeContext = (DirContext) schema
					.lookup("AttributeDefinition");

			if (attributeContext != null) {
				// List all defined attributes in a vector
				// name: attribute name
				// value: SYNTAX/DESCRIPTION
				NamingEnumeration<NameClassPair> attributes = attributeContext
						.list("");
				while (attributes.hasMoreElements()) {
					NameClassPair pair = attributes.nextElement();
					String name = pair.getName();
					Entry e = new Entry();
					e.setAttribute("name", name);
					StringBuffer value = new StringBuffer("");
					Attributes attrs = attributeContext.getAttributes(name);
					Attribute syntax = attrs.get("SYNTAX");
					if (syntax != null)
						value
								.append(mapAttributeSyntax(syntax.get()
										.toString()));
					Attribute desc = attrs.get("DESC");
					if (desc != null) {
						value.append("/");
						value.append(mapAttributeSyntax(desc.get().toString()));
					}
					e.setAttribute("value", value.toString());
					result.add(e);
				}
			}
		}

		return result;
	}

	/**
	 * Query the Schema. If source is null, get the schema for the current
	 * Entry. If source is not null, it should be a distinguished name
	 * 
	 * @param source
	 *            A distinguished name
	 * 
	 * @return The schema. If nothing is found, an empty Vector is returned.
	 * @exception Exception
	 *                Any Exception thrown by underlying libraries
	 */
	@Override
	public Object querySchema(Object source) throws Exception {
		DirContext schema = null;
		Vector<Entry> result = new Vector<Entry>();
		String searchdn = null;

		if (source == null) {
			Entry next = getNextEntry();
			if (next == null) {
				// This is new for TIM.
				return queryAllAttributeTypes();
			} else
				searchdn = next.getString(nameParameter);
		} else {
			searchdn = source.toString();
		}

		try {
			schema = ctx.getSchemaClassDefinition(searchdn);
		} catch (javax.naming.NameNotFoundException nnf) {
			logmsg(sResHash.getString("CONNECTOR.JNDI.SEARCHDN.WARNING",
					new Object[] { searchdn, nnf }));
		}

		if (schema == null)
			return result;

		NamingEnumeration<SearchResult> bd = schema.search("", null);
		DirContext dc = ctx.getSchema("");

		try {
			while (bd.hasMore()) {

				Entry e = sr2entry(bd.next());
				if (debugMode()) {
					debug(sResHash
							.getString("CONNECTOR.JNDI.NEXTSCHEMACLASS.INFO"));
					getLog().dump(e);
				}
				addSyntax(result, e, dc, "MUST");
				addSyntax(result, e, dc, "MAY");
			}
		} catch (Throwable ignore) {
		}

		return result;
	}

	/**
	 * Gets an Attribute Syntax from the schema of the DirContext.
	 * 
	 * @param schema
	 *            A DirContext
	 * @param attributeName
	 *            Name of the attribute
	 * @return The attributeSyntax value
	 */
	public String getAttributeSyntax(DirContext schema, String attributeName) {
		String ret = "String";
		try {
			DirContext ad = (DirContext) schema.lookup("AttributeDefinition/"
					+ attributeName);
			if (ad != null) {
				Attribute syntax = ad.getAttributes("").get("SYNTAX");
				if (syntax != null) {
					ret = mapAttributeSyntax(syntax.get().toString());
				}
				Attribute desc = ad.getAttributes("").get("DESC");
				if (desc != null) {
					ret += "/" + mapAttributeSyntax(desc.get().toString());
				}
			}
		} catch (NamingException error) {
			logmsg(sResHash.getString("CONNECTOR.JNDI.ATTRIBNAME.WARNING",
					new Object[] { attributeName, error.toString() }));
		}

		return ret;
	}

	/**
	 * Look up an objectclass in the schema of the DirContext.
	 * 
	 * @param objectClass
	 *            The name of the objectclass
	 * 
	 * @return A Vector containing all attributes of the object class
	 * @exception Exception
	 *                Any Exception thrown by the underlying libraries
	 */
	public Vector<Object> queryObjectClassAttributes(String objectClass)
			throws Exception {
		DirContext s1 = ctx.getSchema("");
		DirContext schema = (DirContext) s1.lookup("ClassDefinition/"
				+ objectClass);
		NamingEnumeration<?> bd = schema.list("");
		Vector<Object> result = new Vector<Object>();

		Attributes attrs = schema.getAttributes("");
		bd = attrs.getAll();
		while (bd.hasMore()) {
			result.add(bd.next());
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.JNDI.CLASSDEFINITION.INFO",
					new Object[] { objectClass, result }));
		}
		return result;
	}

	/**
	 * Utility method used for parsing attribute syntax
	 * 
	 * @param oid
	 *            operation id.
	 * @return The name matching that oid
	 */
	public String mapAttributeSyntax(String oid) {
		String length = "";
		int i = oid.indexOf('{');
		if (i > 0) {
			length = oid.substring(i);
			oid = oid.substring(0, i);
		}

		if (attributeSyntaxMap == null)
			buildAttributeSyntaxMap();

		if (attributeSyntaxMap.get(oid) != null)
			return attributeSyntaxMap.get(oid) + length;
		else
			return oid + length;
	}

	/**
	 * Internal method used to build table to map from oid to name
	 */
	private void buildAttributeSyntaxMap() {
		attributeSyntaxMap = new Hashtable<String, String>();

		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.1", "ACI Item");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.2", "Access Point");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.3",
				"Attribute Type Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.4", "Audio");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.5", "Binary");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.6", "Bit String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.7", "Boolean");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.8", "Certificate");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.9",
				"Certificate List");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.10",
				"Certificate Pair");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.11",
				"Country String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.12", "DN");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.13",
				"Data Quality Syntax");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.14",
				"Delivery Method");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.15",
				"Directory String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.16",
				"DIT Content Rule Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.17",
				"DIT Structure Rule Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.18",
				"DL Submit Permission");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.19",
				"DSA Quality Syntax");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.20", "DSE Type");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.21",
				"Enhanced Guide");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.22",
				"Facsimile Telephone Number");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.23", "Fax");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.24",
				"Generalized Time");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.25", "Guide");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.26", "IA5 String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.27", "INTEGER");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.28", "JPEG");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.54",
				"LDAP Syntax Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.56",
				"LDAP Schema Definition");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.57",
				"LDAP Schema Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.29",
				"Master And Shadow Access Points");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.30",
				"Matching Rule Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.31",
				"Matching Rule Use Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.32",
				"Mail Preference");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.33",
				"MHS OR Address");
		attributeSyntaxMap
				.put("1.3.6.1.4.1.1466.115.121.1.55", "Modify Rights");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.34",
				"Name And Optional UID");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.35",
				"Name Form Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.36",
				"Numeric String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.37",
				"Object Class Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.40", "Octet String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.38", "OID");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.39",
				"Other MailboxConnector");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.41",
				"Postal Address");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.42",
				"Protocol Information");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.43",
				"Presentation Address");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.44",
				"Printable String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.58",
				"Substring Assertion");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.45",
				"Subtree Specification");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.46",
				"Supplier Information");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.47",
				"Supplier Or Consumer");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.48",
				"Supplier And Consumer");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.49",
				"Supported Algorithm");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.50",
				"Telephone Number");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.51",
				"Teletex Terminal Identifier");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.52", "Telex Number");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.53", "UTC Time");
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.4-di7.1.1 %I%, 20%E%";
	}

	/**
	 * Verifies that delta mode is supported.
	 * 
	 * @return true.
	 */
	public boolean isDeltaSupported() {
		return true;
	}

	/**
	 * Retrieves search filter.
	 * 
	 * @return String which contains the SearchFilter
	 */
	protected String getSearchFilterAll() {
		return mSearchFilterAll;
	}

	/**
	 * Sets search filter.
	 * 
	 * @param aSearchFilterAll
	 *            the String to set the SearchFilter
	 */
	protected void setSearchFilterAll(String aSearchFilterAll) {
		mSearchFilterAll = aSearchFilterAll;
	}
}
