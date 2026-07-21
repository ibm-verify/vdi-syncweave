/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import javax.naming.CannotProceedException;
import javax.naming.CommunicationException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.LimitExceededException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;
import javax.naming.directory.Attribute;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortKey;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.icu.util.StringTokenizer;

/**
 * The LDAP connector provides full access to LDAP based directories. It allows
 * operations on user entries and schema.
 */
public class LDAPConnector extends Connector implements ConnectorInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private final static String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "ldapconnector";

	/**
	 * UTF-16LE encoding
	 */
	private final static String LE_UNICODE = "UTF-16LE";

	/**
	 * Parameter name unicode password.
	 */
	private final static String UNICODE_PASSWORD = "unicodePwd";

	/**
	 * Component name
	 */
	private final static String myName = "LDAP Connector";

	/**
	 * Parameter name
	 */
	private final static String INITCTX = "com.sun.jndi.ldap.LdapCtxFactory";

	/**
	 * Value of binary attributes.
	 */
	private final static String ADBinaryAttributes = "photo personalSignature audio jpegPhoto javaSerializedData thumbnailPhoto thumbnailLogo userPassword userCertificate authorityRevocationList certificateRevocationList crossCertificatePair x500UniqueIdentifier objectGUID objectSid deltaRevocationList";

	/**
	 * {@link InitialLdapContext}
	 */
	private InitialLdapContext ctx;

	/**
	 * An enumeration of SearchResults for the objects that satisfy the filter.
	 */
	private NamingEnumeration<SearchResult> results;

	/**
	 * Current search result.
	 */
	private SearchResult current = null;

	/**
	 * int value of connector flag
	 */
	private int updateFlags = 0;

	/**
	 * Cookie
	 */
	private byte[] cookie;

	/**
	 * If the server supports paged-find you can specify the number of entries
	 * returned per page
	 */
	private int ldapPageSize = 0;

	/**
	 * provides constraints for search result.
	 */
	private SearchControls constraints;

	/**
	 * Attribute syntax map
	 */
	private Hashtable<String, String> attributeSyntaxMap;

	/**
	 * Timeout in seconds (0 Forever)
	 */
	private int timeLimit = 0;

	/**
	 * Max number of entries to return during a search (0 - All)
	 */
	private int sizeLimit = 0;

	/**
	 * Auto map AD password flag
	 */
	private boolean automapADPassword = false;

	/**
	 * Simulate rename flag
	 */
	private boolean simulateRename = true;

	/**
	 * boolean variable for invoking error hook on AttributeInUseException
	 */
	private boolean callErrorHook = false;

	/**
	 * LDAP virtual list view
	 */

	private String ldapVLV;

	/**
	 * LDAP virtual list view target.
	 */
	private int ldapVLVTarget = 1;

	/**
	 * LDAP virtual list view page size
	 */
	private int ldapVLVPageSize = 10000;

	/**
	 * LDAP virtual list view list size
	 */
	private int ldapVLVListSize = 0;

	// Subtree Delete flag
	private boolean subtreeDeleteSupported = false;
	
	private boolean serverAdminControl = false;

	private boolean hasSetDeleteRdn;
	
	// Connector flags
	/**
	 * ES delete flag
	 */
	final static int FLAG_ES_DELETE = 1;

	/**
	 * ES skip flag
	 */
	final static int FLAG_ES_SKIP = 2;

	/**
	 * pre ignore flag
	 */
	final static int FLAG_PRE_IGNORE = 4;

	/**
	 * Control oid values
	 */
	private final static String[] CONTROL_OID = { "1.2.840.113556.1.4.319", "1.2.840.113556.1.4.473", "2.16.840.1.113730.3.4.9",
			"2.16.840.1.113730.3.4.3", "1.2.840.113556.1.4.805", "1.2.840.113556.1.4.841","1.3.18.0.2.10.15",
			"2.16.840.1.113730.3.4.18"};

	/**
	 * Control label values
	 */
	private final static String[] CONTROL_LABEL = { "Paged Search Control", "Server Side Sorting", "Virtual List View",
			"Persistent Search Control", "Tree Delete Control", "Dirsync Control","Server Admin Control", 
			"Proxy Authorization Control", };

	/**
	 * Paged search control index
	 */
	private final static int PAGED_SEARCH_CONTROL = 0;

	/**
	 * Server side sorting index
	 */
	private final static int SERVER_SIDE_SORTING = 1;

	/**
	 * Virtual list view index
	 */
	private final static int VIRTUAL_LIST_VIEW = 2;

	/**
	 * Persistent search index
	 */
	private final static int PERSISTANT_SEARCH = 3;

	/**
	 * Tree Delete Control index
	 */
	private final static int TREE_DELETE_CONTROL = 4;
	
	/**
	 * Dir Sync Control index
	 */
	//private final static int DIRSYNC_CONTROL = 5;

	/**
	 * Server Admin Control index
	 */
	private final static int SERVER_ADMIN_CONTROL = 6;
	
	/**
	 * Proxy Authorization Control index
	 */
	private final static int PROXY_AUTH_CONTROL = 7;

	/**
	 * Last control index
	 */
	private final static int LAST_CONTROL = 7;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Class absolute name
	 */
	private final static String VIRTUAL_LISTVIEW_CONTROL_CLASS_NAME = "com.sun.jndi.ldap.ctl.VirtualListViewControl";

	/**
	 * Class absolute path name
	 */
	private final static String VIRTUAL_LISTVIEW_RESPONSE_CONTROL_CLASS_NAME = "com.sun.jndi.ldap.ctl.VirtualListViewResponseControl";

	/**
	 * Constructor for the LDAPConnector object
	 */
	public LDAPConnector() {
		setName(myName);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE, ConnectorConfig.DELETE_MODE, ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.LOOKUP_MODE, ConnectorConfig.UPDATE_MODE, ConnectorConfig.DELTA_MODE, });
	}

	/**
	 * Close the connection to the LDAP server
	 */
	public void terminate() {
		if (ctx != null) {
			try {
				ctx.close();
			} catch (NamingException ignore) {
				logmsg(sResHash.getString("CONNECTOR.LDAP.CANNOT.CLOSE.CONTEXT", ignore.toString()));
			}
		}
	}

	/**
	 * Returns the InitialLdapContext (Connection to the LDAP Server)
	 * 
	 * @return The connection to the LDAP Server
	 */
	public InitialLdapContext getLdapContext() {
		return ctx;
	}
	
	/**
	 * Returns  true if Server Adming control has been set
	 * @return Boolean 
	 */
	
	public boolean isServerAdminControl() {
		return serverAdminControl;
	  }

/**
 * Sets the server admin control to either true or false. 
 * @param serverAdminControl
 */
	  public void setServerAdminControl(boolean valueOfserverAdminControl) {
		serverAdminControl = valueOfserverAdminControl;
	  }

	/**
	 * Utility method to create a control of class VirtualListViewControl using
	 * reflection.
	 * 
	 * @param targetOffset
	 *            Parameter of the VirtualListViewControl constructor
	 * @param listSize
	 *            Parameter of the VirtualListViewControl constructor
	 * @param beforeCount
	 *            Parameter of the VirtualListViewControl constructor
	 * @param afterCount
	 *            Parameter of the VirtualListViewControl constructor
	 * @param criticality
	 *            Parameter of the VirtualListViewControl constructor
	 * 
	 * @exception Exception
	 *                Any Exception by the reflection mechanism or by the
	 *                invoked constructor
	 * 
	 * @return the created control
	 */
	private static Control createVirtualListViewControl(int targetOffset, int listSize, int beforeCount, int afterCount,
			boolean criticality) throws Exception {

		Control vlvc = null;

		try {
			Class<?> vlvcClass = Class.forName(VIRTUAL_LISTVIEW_CONTROL_CLASS_NAME);

			Constructor<?> vlvcConstructor = vlvcClass.getConstructor(new Class[] { int.class, int.class, int.class, int.class,
					boolean.class, });
			vlvc = (Control) vlvcConstructor.newInstance(new Object[] { Integer.valueOf(targetOffset), Integer.valueOf(listSize),
					Integer.valueOf(beforeCount), Integer.valueOf(afterCount), Boolean.valueOf(criticality) });
		} catch (ClassNotFoundException ex) {
			throw new Exception(sResHash.getString("CONNECTOR.LDAP.VIRTUAL.LISTVIEW.CONTROL.CLASS.MISSING"), ex);
		} catch (NoSuchMethodException ex) {
			throw new Exception(sResHash.getString("CONNECTOR.LDAP.VIRTUAL.LISTVIEW.CONTROL.METHOD.MISSING"), ex);
		}

		return vlvc;
	}

	/**
	 * Initialize the Connector, connect to the LDAP Server
	 * 
	 * @param o
	 *            An Object sent to the initialize method, ignored.
	 * 
	 * @exception Exception
	 *                Any Exception by the underlying methods to connect to the
	 *                LDAP Server
	 */
	@Override
	public void initialize(Object o) throws Exception {
		Hashtable<String, Object> env = new Hashtable<String, Object>();

		// Old style connector flags
		updateFlags = 0; // make sure that the update flags is 0 before setting the flag values.
		String str = getParam("connectorFlags");
		if (str != null) {
			str = str.toLowerCase(Locale.ENGLISH);
			if (str.indexOf("{deleteemptystrings}") != -1) {
				updateFlags |= FLAG_ES_DELETE;
			}
			if (str.indexOf("{skipemptystrings}") != -1) {
				updateFlags |= FLAG_ES_SKIP;
			}
			if (str.indexOf("{ignorepartialresults}") != -1) {
				updateFlags |= FLAG_PRE_IGNORE;
			}
		}

		env.put("java.naming.ldap.version", "3");
		env.put("java.naming.ldap.derefAliases", "never");

		// Use Sun's Initial LDAP provider
		env.put(Context.INITIAL_CONTEXT_FACTORY, INITCTX);

		// LDAP Host & Port
		if (getParam("ldapUrl") != null && getParam("ldapUrl").length() > 0)
			env.put(Context.PROVIDER_URL, getParam("ldapUrl"));
		else
			throw new Exception(sResHash.getString("CONNECTOR.LDAP.LDAPURL.NULL"));

		// LDAP Batch Size
		if (getParam("ldapBatchSize") != null) {
			env.put(Context.BATCHSIZE, getParam("ldapBatchSize"));
		} else {
			// finneyj -- i would think we should leave this up to the provider
			// if not specified (ie, not set)
			env.put(Context.BATCHSIZE, "1");
		}

		// LDAP Username
		String userName = getParam("ldapUsername");
		if (userName == null) {
			userName = "";
		}

		// Authentication Mechanism
		String authMethod = getParam("ldapAuthenticationMethod");
		if (authMethod == null) {
			authMethod = (userName.length() > 0) ? "Simple" : "Anonymous";
		}

		// Backward compatibility fix
		if (authMethod.equalsIgnoreCase("Anonymous") && userName.length() > 0) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.USING.SIMPLE.AUTHENTICATION"));
			authMethod = "Simple";
		}

		if (authMethod.equalsIgnoreCase("Anonymous")) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.USING.ANONYMOUS.BIND"));
			}
		} else if (userName.length() == 0) {
			if (!(authMethod.equalsIgnoreCase("SASL") || authMethod.equalsIgnoreCase("CRAM-MD5")))
				logmsg(sResHash.getString("CONNECTOR.LDAP.NO.USERNAME.SPECIFIED.USING.ANONYMOUS"));
		} else {
			env.put(Context.SECURITY_PRINCIPAL, userName);
			env.put(Context.SECURITY_CREDENTIALS, getParam("ldapPassword"));
			env.put(Context.SECURITY_AUTHENTICATION, authMethod);
		}

		// Referral behavior
		if (getParam("ldapReferrals") != null) {
			env.put(Context.REFERRAL, getParam("ldapReferrals"));
		}

		// SSL Connection?
		if ("true".equals(getParam("ldapUseSSL"))) {
			env.put(Context.SECURITY_PROTOCOL, "ssl");
			env.put("java.naming.ldap.factory.socket", "javax.net.ssl.SSLSocketFactory");
			logmsg(sResHash.getString("CONNECTOR.LDAP.SSLINFO"));
		}

		// JNDI extra parameters
		str = getParam("jndiExtraProviderParams");
		if (str != null) {
			StringTokenizer st = new StringTokenizer(str, "\r\n");
			while (st.hasMoreTokens()) {

				String nt = st.nextToken();
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.NEXT.EXTRA.PARAMETER", nt));
				}
				if (nt.length() < 1) {
					continue;
				}

				int index = nt.indexOf(":");
				if (index == -1) {
					logmsg(sResHash.getString("CONNECTOR.LDAP.JNDI.EXTRA.PARAM.NO.COLON.SEPARATOR.IN.LINE", nt));
					continue;
				}

				String param = nt.substring(0, index);
				String value = nt.substring(index + 1);
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.JNDI.PROVIDER.PARAM", new Object[] { param, value }));
				}
				env.put(param, value);
			}
		}

		// Add binary attributes for ActiveDirectory (just in case)
		str = getParam("ldapBinaryAttributes");
		if (env.get("java.naming.ldap.attributes.binary") == null && str != null) {
			StringTokenizer st = new StringTokenizer(str, "\r\n");
			StringBuffer binattr = new StringBuffer();
			while (st.hasMoreTokens()) {
				binattr.append(" ");
				binattr.append(st.nextToken().trim());
			}
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.SETTING.BINARY.ATTRIBUTES.TO", binattr.toString().trim()));
			}
			env.put("java.naming.ldap.attributes.binary", binattr.toString().trim());
		}

		// Verify binary attributes // finneyj -- why do do we need to do this??
		// (AND above)
		if (env.get("java.naming.ldap.attributes.binary") == null) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.NO.BINARY.ATTRIBUTES.SPECIFIED.USING.DEFAULT", ADBinaryAttributes));
			}
			env.put("java.naming.ldap.attributes.binary", ADBinaryAttributes);
		}

		// LDAP Time Limit (read from form in seconds)
		if (hasConfigValue("ldapTimeLimit")) {
			timeLimit = Integer.parseInt(getParam("ldapTimeLimit")) * 1000;
		}

		// LDAP Size Limit
		if (hasConfigValue("ldapSizeLimit")) {
			sizeLimit = Integer.parseInt(getParam("ldapSizeLimit"));
		}

		// ActiveDirectory Password Mapping
		if (hasConfigValue("automapADPassword")) {
			automapADPassword = Boolean.valueOf(getParam("automapADPassword")).booleanValue();
		}

		// Simulate rename by delete/add
		if (hasConfigValue("simulateRename")) {
			simulateRename = Boolean.valueOf(getParam("simulateRename")).booleanValue();
		}

		// LDAP Tracing
		str = getParam("ldapBERTrace");
		if (str != null && str.trim().length() > 0) {
			env.put("com.sun.jndi.ldap.trace.ber", new FileOutputStream(str.trim()));
		}
		if (hasConfigValue("setOprAttributes") && Boolean.valueOf(getParam("setOprAttributes"))) {
			serverAdminControl = true;
		}

		// get a handle to an Initial DirContext
		ctx = new InitialLdapContext(env, null);

		// Virtual List View (specifies the sorting attribute)
		str = getParam("ldapVLVPageSize");
		if (str != null && str.trim().length() > 0) {
			ldapVLVPageSize = Integer.parseInt(str);
			if (ldapVLVPageSize > 0) {
				ldapVLV = getParam("ldapSortAttribute");
				if (ldapVLV == null) {
					throw new Exception(sResHash.getString("CONNECTOR.LDAP.CANNOT.PERFORM.VIRTUALLISTVIEW.SEARCH"));
				}

				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.USING.VIRTUAL.LIST.VIEW", new Object[] { ldapVLV,
							Integer.valueOf(ldapVLVPageSize) }));
				}

				// Request VLV from 0% and 100 entries above
				// ldapVLV parameter specifies the attribute to sort on (sort
				// control required with VLV)
				ctx.setRequestControls(new Control[] { new SortControl(new String[] { ldapVLV }, Control.CRITICAL),
						createVirtualListViewControl(1
						/*
						 * Target
						 */
						, 0
						/*
						 * ListSize unknown
						 */
						, 0
						/*
						 * before
						 */
						, ldapVLVPageSize
						/*
						 * Number of entries pr page
						 */
						, Control.CRITICAL) });
			}
		}

		// Only sort selection if VLV is not used
		if ((ldapVLV == null) && ((str = getParam("ldapSortAttribute")) != null) && (str.trim().length() > 0)) {
			setSortControl(str, true, sizeLimit);
		}

		if (debugMode()) {
			showServerInfo();
		}
	}

	/**
	 * Sets a Sort Control using the specified attribute.
	 * This will be used for the coming requests.
	 * @param attrName The attribute to sort by. If null, remove all request controls
	 * @param ascending if true, use ascending sort
	 * @param limit Limits number of results. Use 0 to specify no limit
	 * @throws Exception
	 * @since 7.2.0.2
	 */
	public void setSortControl(String attrName, boolean ascending, int limit) throws Exception {
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.LDAP.REQUESTING.SORT.CONTROL.FOR.ATTRIBUTE", attrName));
		}
		
		if (attrName != null) {
			SortKey key = new SortKey(attrName, ascending, null);
			ctx.setRequestControls(new Control[] {new SortControl(new SortKey[] { key }, false) });	
		}// else {
		// 	ctx.setRequestControls(null);			
		// }
		sizeLimit = limit;
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

		results = null;
		current = null;
		constraints = new SearchControls();

		String scope = getParam("ldapSearchScope");
		if (scope == null || scope.compareToIgnoreCase("subtree") == 0) {
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		} else if (scope.compareToIgnoreCase("onelevel") == 0) {
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
		} else if (scope.equalsIgnoreCase("baselevel")  || scope.equalsIgnoreCase("base")){
			constraints.setSearchScope(SearchControls.OBJECT_SCOPE);
		}
		// backward compatibility

		constraints.setTimeLimit(timeLimit);
		constraints.setCountLimit(sizeLimit);

		if (hasConfigValue("ldapReturnAttributes")) {
			String[] retattr = com.ibm.di.util.StringUtils.splitstringArr(getParam("ldapReturnAttributes"), "\r\n");
			if (retattr != null) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.LDAP.RETURN.ATTRIBUTES"));
					for (int lr = 0; lr < retattr.length; lr++) {
						debug(sResHash.getString("CONNECTOR.LDAP.ATTRIBUTES", retattr[lr]));
					}
				}
				constraints.setReturningAttributes(retattr);
			}
		}

		String pageSize = getParam("ldapPageSize");
		if (pageSize != null && pageSize.length() > 0) {
			ldapPageSize = Integer.parseInt(getParam("ldapPageSize"));
			if (ldapPageSize > 0) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.USING.PAGED.SEARCH.CONTROL.PAGE.SIZE", Integer.valueOf(ldapPageSize)));
				}
				if (!supportsPagedResults()) {
					logmsg(sResHash.getString("CONNECTOR.LDAP.LDAP.SERVER.DOES.NOT.CONFIRM.WARNING"));
				}
				ctx.setRequestControls(new Control[] { new PagedResultsControl(ldapPageSize, Control.CRITICAL) });
			}
		}

		/*
		 * String sortKey = getParam("ldapSortKey"); if ( sortKey != null &&
		 * sortKey.length() > 0 ) { Control sortCtl = new SortControl(new
		 * String[]{sortKey}, Control.CRITICAL); ctx.setRequestControls ( new
		 * Control[] {sortCtl} ); }
		 */

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.LDAP.SELECTENTRIES.SEARCH.IN", new Object[] { getParam("ldapSearchBase"),
					getParam("ldapSearchFilter") }));
		}
		try {
			results = ctx.search(getCompName(getParam("ldapSearchBase")), getParam("ldapSearchFilter"), constraints);
			cookie = parseControls(ctx.getResponseControls()); // Defect 12741

			if (results.hasMore()) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.SELECTENTRIES.HASMORE"));
				}
				return;
			} else {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.SELECTENTRIES.NO.ENTRIES.RETURNED"));
				}
				results = null;
				return;
			}
		} catch (javax.naming.PartialResultException pre) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.SELECTENTRIES.RETURNS.NO.ENTRIES"));
				debug(pre.toString());
			}
			results = null;
			return;

			// } catch (javax.naming.LimitExceededException lee) {
			// throw lee;
		} catch (Exception e) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.SELECTENTRIES.SEARCH.EXC", e.toString()));
			results = null;
			throw e;
		} finally {
			ctx.setRequestControls(null);
		}
	}

	/**
	 * Get the next entry that was retrieved by selectEntries(). The Entry
	 * returned is populated with attributes and values from the next entry in
	 * the input set. The $dn Attribute of the returned Entry is the
	 * distinguished name. If we are using paged-find, possibly retrieve more
	 * results.
	 * 
	 * @return The next Entry populated with values, or null if nore more
	 *         Entries
	 * @exception Exception
	 *                Any Exception thrown by the underlying libraries
	 */
	@Override
	public Entry getNextEntry() throws Exception {
		boolean hasMore;

		if (results == null) {
			return null;
		}

		// Can only call hasMore() once (!!!)
		try {
			hasMore = results.hasMore();
		} catch (javax.naming.PartialResultException pre) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.PARTIALRESULTEXCEPTION.IGNORED"));
			}
			hasMore = false;
		} catch (Exception error) {
			hasMore = false;
			results = null; // We have reached the end of the NamingEnumeration
			// Guess that when the user has specified a sizelimit, a
			// SizeLimitExceededException is not an error
			if (sizeLimit > 0 && (error instanceof javax.naming.SizeLimitExceededException)) {
				// TODO: log a debug message
				return null;
			}

			throw error;
			// defect 305 & 345 exception needed to tell user
			// about sizelimit,timelimit or referrals
		}

		// If we are using paged-find try next page
		while (!hasMore && ldapPageSize > 0) {
			cookie = parseControls(ctx.getResponseControls());
			if (cookie == null || cookie.length == 0)
				break;
			// reset the paged results control
			ctx.setRequestControls(new Control[] { new PagedResultsControl(ldapPageSize, cookie, Control.CRITICAL) });
			try {
				results = ctx.search(getCompName(getParam("ldapSearchBase")), getParam("ldapSearchFilter"), constraints);
				cookie = parseControls(ctx.getResponseControls());
			} finally {
				ctx.setRequestControls(null);
			}
			hasMore = results.hasMore();
		}

		// If we are using virtual list view
		if (!hasMore && ldapVLV != null) {
			cookie = parseControls(ctx.getResponseControls());

			if ((cookie != null) && (ldapVLVTarget < ldapVLVListSize)) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.REQUEST.NEXT.VLV", Integer.valueOf(ldapVLVTarget)));
				}
				// reset the paged results control
				Control vlvc = createVirtualListViewControl(ldapVLVTarget, ldapVLVListSize, 0, ldapVLVPageSize, Control.CRITICAL);
				if (cookie.length > 0) {
					try {
						Class<?> vlvcClass = Class.forName(VIRTUAL_LISTVIEW_CONTROL_CLASS_NAME);
						vlvcClass.getMethod("setContextID", new Class[] { byte[].class }).invoke(vlvc, new Object[] { cookie });
					} catch (ClassNotFoundException ex) {
						throw new Exception(sResHash.getString("CONNECTOR.LDAP.VIRTUAL.LISTVIEW.CONTROL.CLASS.MISSING"), ex);
					} catch (NoSuchMethodException ex) {
						throw new Exception(sResHash.getString("CONNECTOR.LDAP.VIRTUAL.LISTVIEW.CONTROL.METHOD.MISSING"), ex);
					}
				}

				ctx.setRequestControls(new Control[] { new SortControl(new String[] { ldapVLV }, Control.CRITICAL), vlvc });
				try {
					results = ctx.search(getCompName(getParam("ldapSearchBase")), getParam("ldapSearchFilter"), constraints);
					cookie = parseControls(ctx.getResponseControls());
				} finally {
					ctx.setRequestControls(null);
				}
				hasMore = results.hasMore();
			}
		}

		if (!hasMore) {
			results = null;
			return null;
		}

		try {
			current = results.next();
			ldapVLVTarget++;
			return getCurrentEntry();
		} catch (javax.naming.PartialResultException pre) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.LDAP.GETNEXT.RETURNS.PARTIAL.RESULT"));
				debug(pre.toString());
			}
			results = null;
			return null;
		}
	}

	/**
	 * Gets the current Entry. This is the last Entry returned by getNextEntry()
	 * )
	 * 
	 * @return The current Entry
	 */
	public Entry getCurrentEntry() {
		return entry2at(current);
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
		BasicAttributes attrs = at2entry(entry, true);

		if (entry.getString("$dn") == null) {
			throw new com.ibm.di.exceptions.NonFatalException(sResHash.getString("CONNECTOR.LDAP.PUTENTRY.NO.DISTINGUISHED.NAME"));
		}

		attrs.remove("$dn");
		if (isServerAdminControl()) { // This allows operational attributes to be added. 
			Control[] saveControls = ctx.getRequestControls();
			try {
				ctx.setRequestControls(new Control[] { new ServerAdminControl() });
				ctx.createSubcontext (getCompName(entry.getString("$dn")), attrs);
			} finally {
				ctx.setRequestControls(saveControls);
			}
		}else{
			ctx.createSubcontext(getCompName(entry.getString("$dn")), attrs);
		}
	}

	/**
	 * Modify an Object in the LDAP Server. This call is equivalent to
	 * modEntry(entry, search, findEntry(search))
	 * 
	 * @see #modEntry(Entry, SearchCriteria, Entry)
	 * 
	 * @param entry
	 *            An Entry populated with the values to modify
	 * @param search
	 *            The SearchCriteria
	 */
	@Override
	public void modEntry(Entry entry, SearchCriteria search) throws Exception {
		modEntry(entry, search, findEntry(search));
	}

	/**
	 * Modify an Object in the LDAP Server. The supplied entry should contain a
	 * $dn Attribute with the distinguished name. If it does not, then either
	 * the SearchCriteria must be $dn equals some value, or the old Entry must
	 * contain a $dn Attribute. If the $dn Attribute in entry and old are
	 * different, we will try to rename the object in the LDAP Server. The easy
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
	public void modEntry(Entry entry, SearchCriteria search, Entry old) throws Exception {

		// We may be called with old=null entry if we got here through
		// delta-optimize
		String olddn = null;
		String dn = entry.getString("$dn");
		if (dn == null) {
			// Try the search criteria
			if ("$dn".equals(search.getFirstCriteriaName()))
				dn = search.getFirstCriteriaValue();
			if (dn == null && old == null)
				old = findEntry(search);
			if (dn == null && old == null) {
				throw new Exception(sResHash.getString("CONNECTOR.LDAP.DELTA.MODIFY.NO.DN.PROVIDED"));
			}
		}

		// We have to do this in order to avoid optimizing away the first test
		// below (would replace but current ....)
		if (old == null) {
			old = entry;
			olddn = dn;
		} else {
			olddn = old.getString("$dn");
		}

		if (dn == null && olddn != null) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.MODIFYENTRY.NO.DISTINGUISHED.NAME"));
			}
			dn = olddn;
		}

		if (dn == null) {
			// This should really be an error, but I guess we cannot change this
			// now
			throw new com.ibm.di.exceptions.SkipEntryException(sResHash
					.getString("CONNECTOR.LDAP.MODENTRY.NO.DISTINGUISHED.NAME.NO.EXISTING.ENTRY"));
		}

		// modrdn handle - replace the rdn of $dn with the value of newrdn
		String newrdn = entry.getString("newrdn");
		if (newrdn != null) {
			dn = newrdn + dn.substring(dn.indexOf(","), dn.length());
			entry.removeAttribute("newrdn");
		}

		boolean isRDNmodified = false;

		if (!equalsDN(dn, olddn)) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.RENAME.ENTRY", new Object[] { olddn, dn }));
			}
			isRDNmodified = true;
			setDeleteOldRDN(entry);
			try {
				ctx.rename(getCompName(olddn), getCompName(dn));
			} catch (javax.naming.OperationNotSupportedException ons) {
				if (simulateRename) {
					if (debugMode()) {
						debug(ons.toString());
						debug(sResHash.getString("CONNECTOR.LDAP.WILL.TRY.MANUAL.MOVE.OF.ENTRY"));
					}
					moveEntry(olddn, dn);
				} else {
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.LDAP.WILL.NOT.TRY.MANUAL.MOVE.OF.ENTRY"));
					}
					throw ons;
				}
			}
		}

		BasicAttributes ba = at2entry(entry, false);
		ArrayList<ModificationItem> modlist = new ArrayList<ModificationItem>();
		boolean addAttribute = Boolean.valueOf(getParam("ldapAddAttr")).booleanValue();

		try {
			ba.remove("$dn");
			ba.remove("newrdn");

			for (Enumeration<Attribute> e = ba.getAll(); e.hasMoreElements();) {
				Attribute a = e.nextElement();
				if (a.size() < 1) {
					// If empty values then DELETE attribute value
					// But only if server already has a value for the attribute
					if (old.getAttribute(a.getID()) != null) {
						modlist.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, a));
					} else {
						if (debugMode())
							debug(sResHash.getString("CONNECTOR.LDAP.REMOVING.MODIFY.ITEM", a.getID()));
					}
					continue;
				}

				boolean wasAV = false;
				for (int i = 0; i < a.size(); i++) {
					Object obj = a.get(i);
					if (obj instanceof AttributeValue) {
						AttributeValue av = (AttributeValue) obj;
						wasAV = true;
						switch (av.getOper()) {
						case AttributeValue.AV_UNDEFINED:
							throw new Exception(sResHash.getString("CONNECTOR.LDAP.ATTRIBUTEVALUE.WITH.UNDEFINED.OPERATION.CODE",
									new Object[] { a.getID(), Integer.valueOf(i) }));
						case AttributeValue.AV_ADD:
							modlist
									.add(new ModificationItem(DirContext.ADD_ATTRIBUTE,
											new BasicAttribute(a.getID(), av.getValue())));
							break;
						case AttributeValue.AV_DELETE:
							modlist.add(0, new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(a.getID(), av
									.getValue())));
							break;
						case AttributeValue.AV_UNCHANGED:
							// no need to modify unchanged values
							break;
						}
					}
				}

				if (!wasAV) {
					if (addAttribute) {
						modlist.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, a));
					} else {
						modlist.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, a));
					}
				}
			}

		} catch (Exception e) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.MODENTRY.ERROR.BUILDING.MODIFICATIONITEM", e.toString()));
			throw e;
		}

		if (modlist.size() == 0) {
			if (isRDNmodified)
				return;
			if (debugMode())
				debug(sResHash.getString("CONNECTOR.LDAP.NO.MODIFICATION.ITEMS.FOR.ENTRY", dn));
			throw new com.ibm.di.exceptions.NoChangesException("");
		}

		ModificationItem[] mods = modlist.toArray(new ModificationItem[modlist.size()]);

		if (debugMode()) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.CONNECTOR.MODIFICATION.LIST"));
			for (int i = 0; i < mods.length; i++)
				logmsg(sResHash.getString("CONNECTOR.LDAP.MODS", mods[i]));
			logmsg(sResHash.getString("CONNECTOR.LDAP.ASTERISKS"));
		}

		modifyAttributes(getCompName(dn), mods);

	}
	
	private void setDeleteOldRDN(Entry entry) throws NamingException {
		String value = entry.getString("deleteOldRdn");
		if (value != null)
			entry.removeAttribute("deleteOldRdn");
		if (value == null && !hasSetDeleteRdn) {
			return;
		}
		
		if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
			if (!hasSetDeleteRdn) {
				ctx.addToEnvironment("java.naming.ldap.deleteRDN", "false");
			}
			hasSetDeleteRdn = true;
		} else {
			if (hasSetDeleteRdn) {
				ctx.removeFromEnvironment("java.naming.ldap.deleteRDN");
			}
			hasSetDeleteRdn = false;		
		}
		
	}

	/**
	 * Check if Server Admin Control is set and accordingly modify  attributes. 
	 */
	private void modifyAttributes(Name compName, ModificationItem[] mods) throws NamingException {
		try{
			if (isServerAdminControl()) {
				Control[] saveControls = ctx.getRequestControls();
				try {
					ctx.setRequestControls(new Control[] { new ServerAdminControl() });
					ctx.modifyAttributes(compName, mods);
				} finally {
					ctx.setRequestControls(saveControls);
				}
			} else{    	
				ctx.modifyAttributes(compName, mods);
			}
		}catch (AttributeInUseException e) {
			if(debugMode() || getCallErrorHook())
				logmsg(sResHash.getString("CONNECTOR.LDAP.ATTRIBUTE.ALREADY.EXISTS.WITH.GIVEN.VALUE", e.toString()));			
			if (getCallErrorHook())
				throw e;
		}
	}

	/**
	 * Allows deleting subtrees. If the parameter is true, any call to
	 * deleteEntry() will try to delete the entire specified subtree, rather
	 * than only a single element. If the parameter is false, no longer delete
	 * subtrees.
	 * 
	 * @param subtreeDeleteSupported
	 *            If true, delete subtrees.
	 */

	public void setsubtreeDeleteSupported(boolean subtreeDeleteSupported) throws Exception {
		this.subtreeDeleteSupported = subtreeDeleteSupported;
	}

	/**
	 * Delete an entry in the LDAP Server. The distinguished name is provided by
	 * the $dn Attribute in the entry parameter. If not found there. the
	 * SearchCriteria. must be of the form $dn equals value.
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
	public void deleteEntry(Entry entry, SearchCriteria search) throws Exception {
		String dn = null;

		// First try provided entry
		if (entry != null)
			dn = entry.getString("$dn");

		// Next try the search criteria
		if (dn == null && "$dn".equals(search.getFirstCriteriaName()))
			dn = search.getFirstCriteriaValue();

		// Next try to lookup the entry and use the dn from that entry
		if (dn == null) {
			entry = findEntry(search);
			if (entry != null)
				dn = entry.getString("$dn");
		}

		// No DN - no delete
		if (dn == null) {
			throw new Exception(sResHash.getString("CONNECTOR.LDAP.DELETEENTRY.CALLED.WITH.NO.DN.OR.LINKCRITERIA"));
		}
		if (subtreeDeleteSupported) {
			Control[] saveControls = ctx.getRequestControls();
			ctx.setRequestControls(new Control[] { new TreeDeleteControl() });
			try {
				ctx.destroySubcontext(getCompName(dn));
			} finally {
				ctx.setRequestControls(saveControls);
			}
		} else {
			ctx.destroySubcontext(getCompName(dn));
		}

	}

	static class TreeDeleteControl implements Control {
		private static final long serialVersionUID = -5814766140486088567L;

		public byte[] getEncodedValue() {
			return new byte[] {};
		}

		public String getID() {
			return (CONTROL_OID[TREE_DELETE_CONTROL]);
		}

		public boolean isCritical() {
			return true;
		}
	}
	
	static class ServerAdminControl implements Control
	{
	  private static final long serialVersionUID = -4206278647121039038L;

	  public byte[] getEncodedValue() {
	    return new byte[] {};
	  }

	  public String getID() {
		  return (CONTROL_OID[SERVER_ADMIN_CONTROL]);
	  }

	  public boolean isCritical() {
	    return true;
	  }
	}

	static class ProxyAuthControl implements Control
	{
	  private static final long serialVersionUID = -1;

	  private byte[] dnBytes;
	  
	  ProxyAuthControl(String dn) {
		  try {
			  dnBytes = ("dn: " + dn).getBytes("UTF-8");
		  } catch (Exception e) {
			  dnBytes =  new byte[] {};
		  }
	  }
	 
	  public byte[] getEncodedValue() {
		    return dnBytes;
		  }

	  public String getID() {
		  return (CONTROL_OID[PROXY_AUTH_CONTROL]);
	  }

	  public boolean isCritical() {
	    return true;
	  }
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
		// Clear list of multiple entries found
		clearFindEntries();

		SearchControls constraints = new SearchControls();
		constraints.setTimeLimit(timeLimit);
		constraints.setCountLimit(sizeLimit);

		if (hasConfigValue("ldapReturnAttributes")) {
			String[] retattr = com.ibm.di.util.StringUtils.splitstringArr((String) getParam("ldapReturnAttributes"), "\r\n");
			if (retattr != null) {
				constraints.setReturningAttributes(retattr);
			}
		}

		if ("$dn".equalsIgnoreCase(search.getFirstCriteriaName())) {

			Vector<String> dnlist = new Vector<String>();
			addDNFromCriteria(dnlist, search.getCriteria());

			for (int i = 0; i < dnlist.size(); i++) {

				String checkDN = dnlist.elementAt(i);
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.SEARCH.FOR.DN", checkDN));
				}
				constraints.setSearchScope(SearchControls.OBJECT_SCOPE);

				NamingEnumeration<SearchResult> results = null;
				try {
					results = ctx.search(getCompName(checkDN), "objectClass=*", constraints);
					if (results != null && results.hasMore()) {
						if (debugMode()) {
							debug(sResHash.getString("CONNECTOR.LDAP.OBJECT.FOUND", checkDN));
						}
						Entry entry = entry2at(results.next(), checkDN);
						if (!addFindEntry(entry)) {
							i = dnlist.size();
						}
					}
				} catch (javax.naming.PartialResultException pre) {
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.LDAP.FINDENTRY.PARTIALRESULTEXCEPTION", pre.toString()));
					}
				} catch (javax.naming.NameNotFoundException nnf) {
					// Must catch this since it is a search operation
					debug(sResHash.getString("CONNECTOR.LDAP.FINDENTRY.NAMENOTFOUNDEXCEPTION2", nnf.toString()));
				} finally {
					if (results != null)
						results.close();
				}
			}

		} else {
			String filter;

			if ("onelevel".equalsIgnoreCase(getParam("ldapSearchScope"))) {
				constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			} else if ("subtree".equalsIgnoreCase(getParam("ldapSearchScope"))) {
				constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			} else if ("baselevel".equalsIgnoreCase(getParam("ldapSearchScope")) || ("base".equalsIgnoreCase(getParam("ldapSearchScope")))) {
				constraints.setSearchScope(SearchControls.OBJECT_SCOPE);
			}

			if (constraints.getSearchScope() == SearchControls.OBJECT_SCOPE
					&& "".equalsIgnoreCase(getParam("ldapSearchBase").trim())
					&& "objectClass".equalsIgnoreCase(search.getFirstCriteriaName().trim())
					&& "*".equalsIgnoreCase(search.getFirstCriteriaValue().trim())
					&& search.getFirstCriteriaMatch() == SearchCriteria.EXACT) {
				filter = "objectClass=*";
			} else {
				filter = search.getLDAPFilter();
			}

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.FINDENTRY.WHERE.FILTER.IN.BASE", new Object[] { filter,
						getParam("ldapSearchBase") }));
			}

			NamingEnumeration<SearchResult> results = null;
			try {
				results = ctx.search(getCompName(getParam("ldapSearchBase")), filter, constraints);
				while (results != null && results.hasMore()) {
					if (!addFindEntry(entry2at(results.next())))
						break;
				}
			} catch (javax.naming.PartialResultException pre) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.FINDENTRY.PARTIALRESULTEXCEPTION2", pre.toString()));
				}
			} catch (javax.naming.NameNotFoundException nnf) {
				logmsg(sResHash.getString("CONNECTOR.LDAP.FINDENTRY.NAMENOTFOUNDEXCEPTION", nnf.toString()));
			} catch (javax.naming.SizeLimitExceededException sle) {
				// This is not an error, we have finished the search
				SystemFunctions.doNothing();
			} finally {
				if (results != null)
					results.close();
			}
		}

		if (getFindEntryCount() == 1) {
			return getFirstFindEntry();
		} else {
			return null;
		}
	}

	/**
	 * Utility method, that checks if an Exception thrown by underlying
	 * libraries is fatal
	 * 
	 * @param e
	 *            The Exception
	 * 
	 * @return true if this is a fatal Exception
	 */
	@Override
	public boolean isExceptionFatal(Exception e) {
		if (e.getClass() == CommunicationException.class) {
			return true;
		}

		if (e.getClass() == CannotProceedException.class) {
			return true;
		}

		if (e.getClass() == LimitExceededException.class) {
			return true;
		}

		if (e.getClass() == ServiceUnavailableException.class) {
			return true;
		}
		return false;
	}

	/**
	 * Internal method used to convert a SearchResult from the LDAP Server to an
	 * Entry.
	 * 
	 * @param sr
	 *            The SearchResult
	 * @return The sr parameter converted to an Entry.
	 */
	public Entry entry2at(SearchResult sr) {
		return entry2at(sr, null);
	}
	
	private Entry entry2at(SearchResult sr, String dn) {
		Entry entry = new Entry();

		try {
			if (sr.getAttributes() != null) {
				for (NamingEnumeration<? extends Attribute> ea = sr.getAttributes().getAll(); ea.hasMore();) {
					BasicAttribute ba = (BasicAttribute) ea.next();
					com.ibm.di.entry.Attribute a = new com.ibm.di.entry.Attribute(ba.getID());
					for (NamingEnumeration<?> ev = ba.getAll(); ev.hasMore();) {
						a.addValue(ev.next());
					}
					entry.setAttribute(a);
				}
			}

			if (dn != null) {
				entry.setAttribute("$dn", dn);
				return entry;				
			}
			
			String name = sr.getName();

			if (!sr.isRelative()) {
				try {
					String s = new URI(name).getPath();
					if (s != null) {
						if (s.startsWith("/"))
							s = s.substring(1);
						entry.setAttribute("$dn", s);
						return entry;
					}
				} catch (Exception e) {
					logmsg(sResHash.getString("UNABLE.TO.PARSE.URL", name));
				}
			}

			if (! Boolean.getBoolean("com.ibm.di.ldapUseOldDN")) {
				try {
					entry.setAttribute("$dn", sr.getNameInNamespace());
					return entry;
				} catch (Exception e) {
					SystemFunctions.doNothing();		
				}
			}

			String base = (String) getParam("ldapSearchBase");

			if (base == null || base.trim().length() == 0) {
				entry.setAttribute("$dn", name);
			} else if (name.length() == 0) {
				entry.setAttribute("$dn", base);
			} else if (name.startsWith("\"") && name.endsWith("\"")) {
				entry.setAttribute("$dn", name.substring(1, name.length() - 1) + "," + base);
			} else {
				entry.setAttribute("$dn", name + "," + base);
			}

		} catch (NamingException exp) {
			entry.setAttribute("NAMING_EXCEPTION", exp.toString());
		}
		return entry;
	}

	/**
	 * Internal method used to convert an Entry into BasicAttributes that can be
	 * sent to the LDAP Server
	 * 
	 * @param entry
	 *            The Entry to convert to BasicAttributes
	 * @param removeEmptyAttrs
	 *            If true, do not include empty Attributes in the
	 *            BasicAttributes
	 * @return The Entry converted to BasicAttributes
	 * @exception Exception
	 *                If the userPassword Attribute is multivalued, and the Auto
	 *                Map AD Password parameter is set
	 */
	public BasicAttributes at2entry(Entry entry, boolean removeEmptyAttrs) throws Exception {

		BasicAttributes ba = new BasicAttributes();

		boolean modifyMode = (entry.getOp() == Entry.OP_MOD ? true : false); // Determine
		// whether
		// called
		// from
		// modEntry();

		String[] names = entry.getAttributeNames();
		for (int i = 0; i < names.length; i++) {

			com.ibm.di.entry.Attribute ea = entry.getAttribute(names[i]);
			if (ea == null)
				continue;

			if (automapADPassword && names[i].equalsIgnoreCase("userPassword")) {
				if (ea.size() > 1) {
					throw new Exception(sResHash.getString("CONNECTOR.LDAP.AD.UNICODE.PASSWORD.IS.NOT.A.MULTIVALUE"));
				}
				ba.put(setADPassword(ea.getValue(0)));
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.ATTRIBUTE.USERPASSWORD.WAS.ENCODED.AND.AUTOMAPPED"));
				}
				continue;
			}

			// D4421 & D4929 Ignoring those attributes which have NOT CHANGED
			// (when processing a Delta stream and connector in Delta mode) .
			if (modifyMode && (ea.getOper() == com.ibm.di.entry.Attribute.ATTRIBUTE_UNCHANGED)
					&& (((ConnectorConfig) getConfiguration()).getMode().equals(ConnectorConfig.DELTA_MODE))) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.IGNORING.UNCHANGED.ATTRIBUTE", ea.getName()));
				}
				continue;
			}

			BasicAttribute a = new BasicAttribute(names[i]);

			for (int j = 0; j < ea.size(); j++) {
				Object obj = ea.getValueAV(j);
				if (flagSet(FLAG_ES_DELETE) && obj.toString().length() == 0) {
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.LDAP.REMOVING.EMPTY.STRING.VALUE", ea.getName()));
					}
				} else if (removeEmptyAttrs && obj instanceof AttributeValue) {
					AttributeValue av = (AttributeValue) obj;
					if (av.getOper() != AttributeValue.AV_DELETE)
						a.add(av.getValue());
				} else {
					a.add(obj);
				}
			}
			if (a.size() < 1 && removeEmptyAttrs) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.REMOVING.EMPTY.ATTRIBUTE", a.getID()));
				}
			} else {
				ba.put(a);
			}
		}

		return ba;
	}

	/**
	 * Retrieves the composite name of the provided parameter
	 * 
	 * @param str
	 *            String
	 * @return The updated CompositeName, not a new one. Cannot be null.
	 * @throws InvalidNameException
	 */
	private Name getCompName(String str) throws InvalidNameException {
		if (str == null || str.length() == 0)
			return new CompositeName();
		else
			return new CompositeName().add(str);
	}

	/**
	 * Utility method to compare to distinguished names for equality
	 * 
	 * @param p1
	 *            One distinguished name
	 * @param p2
	 *            Another distinguished name
	 * 
	 * @return true if the names can be considered equal
	 */
	private boolean equalsDN(String p1, String p2) {
		// Quick comparison
		if (p1.compareToIgnoreCase(p2) == 0) {
			return true;
		}

		// Trim insignificant spaces
		String d1 = compactDN(p1);
		String d2 = compactDN(p2);

		return (d1.compareToIgnoreCase(d2) == 0);
	}

	/**
	 * Utility method that removes some insignificant spaces in a distinguished
	 * name
	 * 
	 * @param p1
	 *            A distinguished name
	 * @return The distinguished name with insignificant spaces removed
	 */
	private String compactDN(String p1) {
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
	 * If the LDAP Server does not support renaming, this method can be used to
	 * delete an object and recreating it with another name
	 * 
	 * @param p1
	 *            The old distinguished name
	 * @param p2
	 *            The new distinguished name
	 * 
	 * @throws Exception
	 *             If the old entry could not be deleted
	 * @throws Exception
	 *             If the old entry could be deleted, but the new entry could
	 *             not be added
	 */
	public void moveEntry(String p1, String p2) throws Exception {
		SearchCriteria src = new SearchCriteria("$dn", SearchCriteria.EXACT, p1);
		Entry old = findEntry(src);
		old.removeAttribute("$dn");
		old.setAttribute("$dn", p2);

		boolean wasAdded = false;
		boolean wasDeleted = false;

		try {
			ctx.destroySubcontext(getCompName(p1));
			wasDeleted = true;
			putEntry(old);
			wasAdded = true;

		} catch (Exception e) {
			// Unable to remove old entry
			if (!wasDeleted) {
				throw e;
			}

			// Deleted current entry but failed to create new
			// Try to recreate old entry
			if (wasDeleted && !wasAdded) {
				try {
					old.setAttribute("$dn", p1);
					putEntry(old);
				} catch (Exception fatal) {
					logmsg(sResHash.getString("CONNECTOR.LDAP.FATAL.EXCEPTION", fatal.toString()));
					logmsg(sResHash.getString("CONNECTOR.LDAP.MOVEENTRY.REMOVED.BUT.FAILED.TO.ADD.AGAIN", p1));
					throw fatal;
				}
				throw e;
			}
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.LDAP.SUCCESSFULLY.MOVED", new Object[] { p1, p2 }));
		}
	}

	/**
	 * compare: performs an ldapcompare operation for the given parameters.
	 * This is equivalent to compare(compdn, attname, attvalue, false).
	 * 
	 * @param compdn
	 *            String representing the DN for which to do a compare
	 * @param attname
	 *            String representing the name of the attribute to compare (left
	 *            side of ldap filter)
	 * @param attvalue
	 *            String representing the value of the attribute to compare
	 *            (right side of LDAP filter)
	 * 
	 * @return boolean True if the compare evaluates as true, False otherwise
	 * @throws Exception
	 *             when underlying compare function fails
	 */

	public boolean compare(String compdn, String attname, String attvalue) throws Exception {
		return compare(compdn, attname, attvalue, false);
	}

		/**
		 * Performs an LDAP compare operation for the given parameters
		 * 
		 * @param compDN
		 *            String representing the DN for which to do a compare
		 * @param attrName
		 *            String representing the name of the attribute to compare (left
		 *            side of LDAP filter)
		 * @param attrValue
		 *            String representing the value of the attribute to compare
		 *            (right side of LDAP filter)
		 * @param escapeValue
		 * 			  If true, special characters in the attrValue will be escaped.
		 * 			  If false, the attrValue string is already in the format accepted by LDAP.
		 * 
		 * @return boolean True if the compare evaluates as true, False otherwise
		 * @throws Exception
		 *             when underlying compare function fails
		 */

	public boolean compare(String compDN, String attrName, String attrValue, boolean escapeValue) throws Exception {
		SearchControls constraints = new SearchControls();

		constraints.setSearchScope(SearchControls.OBJECT_SCOPE);

		constraints.setReturningAttributes(new String[0]);
		// Return no attrs

		String compFilter = attrName + "=";
		if (escapeValue)
			compFilter += formatSearchValue(attrValue);
		else
			compFilter += attrValue;

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.LDAP.DOING.A.COMPARE.OF.DN", new Object[] { compDN, compFilter }));
		}

		NamingEnumeration<SearchResult> answer = null;
		try {
			answer = ctx.search(getCompName(compDN), compFilter, constraints);

			if (answer != null && answer.hasMoreElements()) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.COMPARISON.IS.TRUE", compFilter));
				}
				return true;
			}
		} catch (Exception e) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.COMPARE.SEARCH.EXC", e.toString()));
			throw e;
		} finally {
			if (answer != null)
				answer.close();
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.LDAP.COMPARISON.IS.FALSE", compFilter));
		}
		return false;
	}

	
	/**
	 * Format the LDAP search string according to RFC 2254.
	 * The special characters star, backslash, parenthesis and nul will be escaped.
	 *
	 * @param str
	 *            The String containing the LDAP search string.
	 */
	public static String formatSearchValue(String str) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			switch (c) {
			case '(':
				ret.append("\\28");
				break;
			case ')':
				ret.append("\\29");
				break;
			case '*':
				ret.append("\\2A");
				break;
			case '\\':
				ret.append("\\5C");
				break;
			case 0:
				ret.append("\\00");
				break;
			default:
				ret.append(c);
				break;
			}
		}
		return ret.toString();
	}

	/**
	 * Adds a given value to an attribute.
	 * 
	 * @param moddn
	 *            String representing the DN to which to add the attribute value
	 * @param modattr
	 *            String representing the name of the attribute to add a value
	 *            to
	 * @param modval
	 *            String representing the value of the attribute add
	 * 
	 * @throws Exception
	 *             when underlying modify operation fails
	 */

	public void addAttributeValue(String moddn, String modattr, String modval) throws Exception {

		try {

			ModificationItem[] mods = new ModificationItem[1];
			Attribute mod = new BasicAttribute(modattr, modval);
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, mod);

			// ctx.modifyAttributes(moddn, mods);
			modifyAttributes(getCompName(moddn), mods);

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.ADD.MODIFICATION.WAS.SUCCESSFUL"));
			}

		} catch (AttributeInUseException e) {
			if(debugMode() || getCallErrorHook())
				logmsg(sResHash.getString("CONNECTOR.LDAP.ADD.ATTRIBUTE.ALREADY.EXISTS.WITH.GIVEN.VALUE", e.toString()));			
			if(getCallErrorHook())
				throw (e);
		} catch (NamingException e) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.ADD.MODIFICATION.FAILED", e));
			throw (e);
		}

	}

	/**
	 * replaceAttributeValue: replaces a given attribute with a certain value
	 * 
	 * 
	 * @param moddn
	 *            String representing the DN to which to replace the attribute
	 *            value
	 * @param modattr
	 *            String representing the name of the attribute to replace the
	 *            value for
	 * @param modval
	 *            String representing the desired value for the attribute
	 * @throws Exception
	 *             when underlying modify operation fails
	 */

	public void replaceAttributeValue(String moddn, String modattr, String modval) throws Exception {

		try {

			ModificationItem[] mods = new ModificationItem[1];
			Attribute mod = new BasicAttribute(modattr, modval);
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod);

			//ctx.modifyAttributes(getCompName(moddn), mods); older
			modifyAttributes(getCompName(moddn), mods);

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.REPLACE.MODIFICATION.WAS.SUCCESSFUL"));
			}

		} catch (AttributeInUseException e) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.REPLACE.ATTRIBUTE.ALREADY.EXISTS.WITH.GIVEN.VALUE", e.toString()));
			throw (e);
		} catch (NamingException e) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.REPLACE.MODIFICATION.FAILED", e));
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
	 * @throws Exception
	 *             when underlying modify operation fails
	 */

	public void removeAttributeValue(String moddn, String modattr, String modval) throws Exception {

		try {

			ModificationItem[] mods = new ModificationItem[1];
			Attribute mod = new BasicAttribute(modattr, modval);
			mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, mod);

			modifyAttributes(getCompName(moddn), mods);

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.REMOVE.MODIFICATION.WAS.SUCCESSFUL"));
			}

		} catch (NamingException e) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.REMOVE.MODIFICATION.FAILED", e));
			throw (e);
		}

	}

	/**
	 * removeAllAttributeValues: removes all values for a given attribute
	 * 
	 * @param moddn
	 *            String representing the DN to which to remove the attribute
	 *            values
	 * @param modattr
	 *            String representing the name of the attribute to remove all
	 *            values from
	 * 
	 * @throws Exception
	 *             when underlying modify operation fails
	 */

	public void removeAllAttributeValues(String moddn, String modattr) throws Exception {

		try {

			ModificationItem[] mods = new ModificationItem[1];
			Attribute mod = new BasicAttribute(modattr, null);
			mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, mod);

			// ctx.modifyAttributes(moddn, mods);
			modifyAttributes(getCompName(moddn), mods);

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.REMOVEALL.MODIFICATION.WAS.SUCCESSFUL"));
			}

		} catch (NamingException e) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.REMOVEALL.MODIFICATION.FAILED", e));
			throw (e);
		}

	}

	/**
	 * setProxyAuthControl: function to setup the proxyAuth control
	 * 
	 * 
	 * @param targetUserDN
	 *            The Target user's DN that will be used for proxy Auth.
	 * @throws Exception
	 *             when underlying setProxyAuthControl operation fails
	 */

	public void setProxyAuthControl(String targetUserDN) throws Exception {

		try {

			ctx.setRequestControls(new Control[] { new ProxyAuthControl(targetUserDN) });

		} catch (Exception e) {
			// logmsg(sResHash.getString("CONNECTOR.LDAP.REPLACE.ATTRIBUTE.ALREADY.EXISTS.WITH.GIVEN.VALUE", e.toString()));
			throw (e);
		} 

	}

	/**
	 * replaceAttributeValueProxy: replaces a given attribute with a certain value, using the proxyAuth control
	 * 
	 * 
	 * @param moddn
	 *            String representing the DN to which to replace the attribute
	 *            value. Also the DN that will be used for proxy Auth.
	 * @param modattr
	 *            String representing the name of the attribute to replace the
	 *            value for
	 * @param modval
	 *            String representing the desired value for the attribute
	 * @throws Exception
	 *             when underlying modify operation fails
	 */

	public void replaceAttributeValueProxy(String moddn, String modattr, String modval) throws Exception {

		Control[] saveControls = ctx.getRequestControls();

		try {

			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(modattr, modval));

			ctx.setRequestControls(new Control[] { new ProxyAuthControl(moddn) });
			ctx.modifyAttributes(getCompName(moddn), mods);

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.REPLACE.MODIFICATION.WAS.SUCCESSFUL"));
			}

		} catch (AttributeInUseException e) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.REPLACE.ATTRIBUTE.ALREADY.EXISTS.WITH.GIVEN.VALUE", e.toString()));
			throw (e);
		} catch (NamingException e) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.REPLACE.MODIFICATION.FAILED", e));
			throw (e);
		} finally {
			ctx.setRequestControls(saveControls);
		}

	}

	/**
	 * Utility method to see if a flag is set
	 * 
	 * @param flag
	 *            the flag we want to check for
	 * 
	 * @return true if that flag is set
	 */
	public boolean flagSet(int flag) {
		return ((updateFlags & flag) > 0);
	}

	/**
	 * Private method to parse Controls returned from the LDAP Server
	 * 
	 * @param controls
	 *            An array of LDAP Control objects to parse
	 * 
	 * @return A cookie
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	private byte[] parseControls(Control[] controls) throws Exception {

		byte[] cookie = null;

		if (controls != null) {

			for (int i = 0; i < controls.length; i++) {

				if (controls[i] instanceof PagedResultsResponseControl) {
					PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];

					int resultSize = prrc.getResultSize();

					if (debugMode()) {
						if (resultSize != 0) {
							debug(sResHash.getString("CONNECTOR.LDAP.END.OF.PAGE", Integer.valueOf(resultSize)));
						} else {
							debug(sResHash.getString("CONNECTOR.LDAP.END.OF.PAGE.UNKNOWN"));
						}
					}

					cookie = prrc.getCookie();
				} else {
					// check if the control is a
					// com.sun.jndi.ldap.ctl.VirtualListViewResponseControl
					try {
						final Class<?> vlvrcClass = Class.forName(VIRTUAL_LISTVIEW_RESPONSE_CONTROL_CLASS_NAME);

						if (vlvrcClass.isInstance(controls[i])) {

							final Control vlvrc = controls[i];
							final Integer rc = (Integer) vlvrcClass.getMethod("getResultCode", new Class[] {}).invoke(vlvrc,
									new Object[] {});
							if (rc.intValue() != 0) {
								throw new NamingException(sResHash.getString(
										"CONNECTOR.LDAP.SORTED.VIEW.DID.NOT.COMPLETE.SUCCESSFULLY.ERROR", rc));
							}

							final Integer vlvrcListSize = (Integer) vlvrcClass.getMethod("getListSize", new Class[] {}).invoke(
									vlvrc, new Object[] {});
							final Integer vlvrcTargetOffset = (Integer) vlvrcClass.getMethod("getTargetOffset", new Class[] {})
									.invoke(vlvrc, new Object[] {});

							if (debugMode()) {
								debug(sResHash.getString("CONNECTOR.LDAP.VLV.LIST.SIZE", vlvrcListSize));
								debug(sResHash.getString("CONNECTOR.LDAP.VLV.TARGET.OFFSET", vlvrcTargetOffset));
								debug(sResHash.getString("CONNECTOR.LDAP.MY.TARGET.OFFSET", Integer.valueOf(ldapVLVTarget)));
							}

							if (ldapVLVListSize > 0 && ldapVLVListSize != vlvrcListSize.intValue()) {
								logmsg(sResHash.getString("CONNECTOR.LDAP.VIRTUAL.LIST.VIEW.CHANGED.RESULT.SET.SIZE", new Object[] {
										Integer.valueOf(ldapVLVListSize), vlvrcListSize }));
							}

							ldapVLVListSize = vlvrcListSize.intValue();

							cookie = (byte[]) vlvrcClass.getMethod("getContextID", new Class[] {}).invoke(vlvrc, new Object[] {});
							if (cookie == null) {
								if (debugMode()) {
									debug(sResHash.getString("CONNECTOR.LDAP.VLV.CONTEXT.ID.IS.NULL"));
								}
								cookie = new byte[0];
							}
						}
					} catch (ClassNotFoundException ex) {
						throw new Exception(sResHash.getString("CONNECTOR.LDAP.VIRTUAL.LISTVIEW.RESPONSE.CONTROL.CLASS.MISSING"),
								ex);
					} catch (NoSuchMethodException ex) {
						throw new Exception(sResHash.getString("CONNECTOR.LDAP.VIRTUAL.LISTVIEW.RESPONSE.CONTROL.METHOD.MISSING"),
								ex);
					}
				}

			}
		}

		return cookie;
		// return (cookie == null) ? new byte[0] : cookie;
	}

	/**
	 * Query the schema of the LDAP server. If source is null, get the schema
	 * for the current Entry. If source is not null, it should be a
	 * distinguished name
	 * 
	 * @param source
	 *            A distinguished name
	 * 
	 * @return The schema
	 * @exception Exception
	 *                Any Exception thrown by underlying libraries
	 */
	@Override
	public Object querySchema(Object source) throws Exception {
		DirContext schema = null;
		Vector<Entry> result = new Vector<Entry>();
		String searchdn = null;

		if (ctx == null)
			return result;

		if (source == null) {
			Entry ex;
			try {
				if (current != null) {
					ex = getCurrentEntry();
				} else {
					ex = getNextEntry();
					current = null;
				}
			} catch (Exception e) {
				ex = null;
			}
			if (ex == null) {
				return result;
			}
			searchdn = ex.getString("$dn");
		} else {
			searchdn = source.toString();
		}

		try {
			if (searchdn != null)
				schema = ctx.getSchemaClassDefinition(searchdn);
		} catch (Exception nnf) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.COLON.1", new Object[] { searchdn, nnf }));
		}

		if (schema == null) {
			return result;
		}

		try {
			NamingEnumeration<SearchResult> bd = schema.search("", null);
			DirContext dc = ctx.getSchema("");

			while (bd.hasMore()) {

				Entry e = entry2at(bd.next());
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.NEXT.SCHEMA.CLASS"));
					getLog().dump(e);
				}

				if (!isAD()) {
					addSyntax(result, e, dc, "MUST");
					addSyntax(result, e, dc, "MAY");
				} else {
					String baseSchemaAD = getADSchema();

					String className = e.getString("Name");
					className = className.substring(className.lastIndexOf(":") + 1, className.length()).trim();

					dc = (DirContext) ctx.getSchema("").lookup("ClassDefinition/" + className);

					String govID = dc.getAttributes("").get("NUMERICOID").toString();
					govID = govID.substring(govID.lastIndexOf(":") + 1, govID.length()).trim();

					getClassAttrib(govID, baseSchemaAD, result, new Vector<String>());
				}
			}
		} catch (Throwable ignore) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.QUERY.SCHEMA.WARNING", ignore.toString()));
		}

		return result;
	}

	/**
	 * Checks if the server is Active Directory or not. Returns true if the
	 * server is Active Directory and false otherwise.
	 * 
	 * @return true if target server is Active Directory
	 * @throws NamingException
	 */
	private boolean isAD() throws NamingException {
		Attributes attrs = ctx.getAttributes("");
		if (attrs != null && attrs.get("highestCommittedUSN") != null) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the Active Directory schema base.
	 * 
	 * @return Schema DN as String
	 * @throws NamingException
	 */
	private String getADSchema() throws NamingException {
		Attributes attrs = ctx.getAttributes("");
		if (attrs != null) {
			javax.naming.directory.Attribute attr = attrs.get("schemaNamingContext");
			String res = "";
			if (attr != null) {
				res = attr.toString();
			}
			if (res != null && res.length() > 0) {
				return res.substring(res.lastIndexOf(":") + 1, res.length());
			}
		}
		return null;
	}

	/**
	 * Get class attributes and put them into Vector object. The class is
	 * recognized by it's governsID (unique number).
	 * 
	 * This method is used only when connected to Active Directory server.
	 * 
	 * @param govID
	 *            governs ID
	 * @param searchBase
	 *            search base
	 * @param result
	 *            vector that holds the result
	 * @param checked
	 *            vector that holds checked classes
	 * @throws NamingException
	 *             if an error occurs.
	 */
	private void getClassAttrib(String govID, String searchBase, Vector<Entry> result, Vector<String> checked)
			throws NamingException {
		NamingEnumeration<SearchResult> entriesEnum = ctx.search(searchBase, "governsID=" + govID, null);
		try {
			while (entriesEnum.hasMore()) {
				SearchResult srClass = entriesEnum.next();

				Entry entry = entry2at(srClass);

				if (checked.contains(srClass.getName())) {
					continue; // if this class is checked, continue with
					// others
				}

				checked.add(srClass.getName());

				DirContext classDef = (DirContext) ctx.getSchema("");
				addSyntax(result, entry, classDef, "mustContain");
				addSyntax(result, entry, classDef, "systemMustContain");
				addSyntax(result, entry, classDef, "mayContain");
				addSyntax(result, entry, classDef, "systemMayContain");

				Attribute auxAttrib = srClass.getAttributes().get("auxiliaryClass");
				Attribute sysAuxAttrib = srClass.getAttributes().get("systemAuxiliaryClass");
				if (sysAuxAttrib != null) {
					if (auxAttrib != null) {
						NamingEnumeration<?> elem = sysAuxAttrib.getAll();
						while (elem.hasMore()) {
							auxAttrib.add(elem.next());
						}
					} else {
						auxAttrib = sysAuxAttrib;
					}
				}

				if (srClass.getAttributes().get("objectClassCategory").toString().equals("objectClassCategory: 3")) {
					// we need the parent classes of a class only if it is
					// auxiliary
					Attribute auxSupperiorAttrib = srClass.getAttributes().get("subClassOf"); // gets
																								// the
																								// parent
																								// class
					if (auxSupperiorAttrib != null) {
						NamingEnumeration<?> elem = auxSupperiorAttrib.getAll();
						if (auxAttrib != null) {
							while (elem.hasMore()) {
								auxAttrib.add(elem.next());
							}
						} else {
							auxAttrib = auxSupperiorAttrib;
						}
					}
				}

				if (auxAttrib == null) {
					continue;
				}

				NamingEnumeration<?> attEnumeration = auxAttrib.getAll();
				while (attEnumeration.hasMore()) {
					getClassAttrib(attEnumeration.next().toString(), searchBase, result, checked);
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
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
	private void addSyntax(Vector<Entry> result, Entry thisEntry, DirContext schema, String must) {
		com.ibm.di.entry.Attribute attr = thisEntry.getAttribute(must);
		if (attr != null) {
			for (int i = 0; i < attr.size(); i++) {
				String attrname = (String) attr.getValue(i);
				Entry e = new Entry();
				e.setAttribute("name", attrname);
				e.setAttribute("syntax", must + "/" + getAttributeSyntax(schema, attrname));
				result.add(e);
			}
		}
	}

	/**
	 * Gets an Attribute Syntax from the LDAP Server
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
			DirContext ad = (DirContext) schema.lookup("AttributeDefinition/" + attributeName);
			if (ad != null) {
				Attribute syntax = ad.getAttributes("").get("SYNTAX");
				if (syntax != null) {
					ret = mapAttributeSyntax(syntax.get().toString());
				}
				Attribute desc = ad.getAttributes("").get("DESC");
				if (desc != null) {
					ret += "/" + mapAttributeSyntax(desc.get().toString());
				}
				/*
				 * ret += "/" + (
				 * ad.getAttributes("").get("SINGLE-VALUE").get().
				 * toString().equalsIgnoreCase("true") ? "single" : "multi");
				 */
			}
		} catch (Exception error) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.COLON.2", new Object[] { attributeName, error.toString() }));
		}

		return ret;
	}

	/**
	 * Look up an objectclass in the LDAP Server.
	 * 
	 * @param objectClass
	 *            The name of the objectclass
	 * 
	 * @return A Vector containing all attributes of the object class
	 * @exception Exception
	 *                Any Exception thrown by the underlying libraries
	 */
	public Vector<Object> queryObjectClassAttributes(String objectClass) throws Exception {
		DirContext s1 = ctx.getSchema("");
		DirContext schema = (DirContext) s1.lookup("ClassDefinition/" + objectClass);
		NamingEnumeration<?> bd = schema.list("");
		Vector<Object> result = new Vector<Object>();

		Attributes attrs = schema.getAttributes("");
		bd = attrs.getAll();
		while (bd.hasMore()) {
			result.add(bd.next());
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.LDAP.CLASSDEFINITION.FOR", new Object[] { objectClass, result }));
		}
		return result;
	}

	/**
	 * Utility method used for parsing attribute syntax
	 * 
	 * @param oid
	 *            An oid
	 * @return The name matching that oid
	 */
	public String mapAttributeSyntax(String oid) {
		String length = "";
		int i = oid.indexOf('{');
		if (i > 0) {
			length = oid.substring(i);
			oid = oid.substring(0, i);
		}

		if (attributeSyntaxMap == null) {
			buildAttributeSyntaxMap();
		}

		if (attributeSyntaxMap.get(oid) != null) {
			return attributeSyntaxMap.get(oid) + length;
		} else {
			return oid + length;
		}
	}

	/**
	 * Internal method used to build table to map from oid to name
	 */
	private void buildAttributeSyntaxMap() {
		attributeSyntaxMap = new Hashtable<String, String>();

		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.1", "ACI Item");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.2", "Access Point");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.3", "Attribute Type Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.4", "Audio");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.5", "Binary");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.6", "Bit String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.7", "Boolean");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.8", "Certificate");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.9", "Certificate List");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.10", "Certificate Pair");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.11", "Country String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.12", "DN");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.13", "Data Quality Syntax");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.14", "Delivery Method");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.15", "Directory String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.16", "DIT Content Rule Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.17", "DIT Structure Rule Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.18", "DL Submit Permission");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.19", "DSA Quality Syntax");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.20", "DSE Type");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.21", "Enhanced Guide");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.22", "Facsimile Telephone Number");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.23", "Fax");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.24", "Generalized Time");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.25", "Guide");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.26", "IA5 String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.27", "INTEGER");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.28", "JPEG");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.54", "LDAP Syntax Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.56", "LDAP Schema Definition");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.57", "LDAP Schema Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.29", "Master And Shadow Access Points");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.30", "Matching Rule Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.31", "Matching Rule Use Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.32", "Mail Preference");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.33", "MHS OR Address");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.55", "Modify Rights");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.34", "Name And Optional UID");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.35", "Name Form Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.36", "Numeric String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.37", "Object Class Description");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.40", "Octet String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.38", "OID");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.39", "Other MailboxConnector");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.41", "Postal Address");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.42", "Protocol Information");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.43", "Presentation Address");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.44", "Printable String");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.58", "Substring Assertion");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.45", "Subtree Specification");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.46", "Supplier Information");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.47", "Supplier Or Consumer");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.48", "Supplier And Consumer");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.49", "Supported Algorithm");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.50", "Telephone Number");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.51", "Teletex Terminal Identifier");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.52", "Telex Number");
		attributeSyntaxMap.put("1.3.6.1.4.1.1466.115.121.1.53", "UTC Time");
	}

	/**
	 * Query the LDAP Server for naming contexts
	 * 
	 * @return A Vector containing the naming contexts
	 * 
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public Vector<Object> queryNamingContexts() throws Exception {

		Vector<Object> list = new Vector<Object>();
		String[] nc = new String[] { "namingcontexts" };
		Attribute attr = ctx.getAttributes("", nc).get("namingcontexts");
		if (attr == null) {
			return list;
		}

		for (int i = 0; i < attr.size(); i++) {
			list.add(attr.get(i));
		}

		return list;
	}

	/**
	 * Get the supported server Controls from the LDAP Server
	 * 
	 * @return The serverControls value
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public Vector<Object> getServerControls() throws Exception {

		Vector<Object> list = new Vector<Object>();
		Attribute attr = ctx.getAttributes("").get("supportedcontrol");
		if (attr == null) {
			return list;
		}

		for (int i = 0; i < attr.size(); i++) {
			list.add(attr.get(i));
		}

		return list;
	}

	/**
	 * Gets the serverInfo from the LDAP Server
	 * 
	 * @return The serverInfo value
	 */
	public Entry getServerInfo() {

		Entry e = new Entry();

		try {
			Attributes attrs = ctx.getAttributes("");
			for (NamingEnumeration<? extends Attribute> ea = attrs.getAll(); ea.hasMore();) {
				BasicAttribute ba = (BasicAttribute) ea.next();
				com.ibm.di.entry.Attribute a = new com.ibm.di.entry.Attribute(ba.getID());
				for (NamingEnumeration<?> ev = ba.getAll(); ev.hasMore();) {
					a.addValue(ev.next());
				}
				e.setAttribute(a);
			}
		} catch (Exception error) {
			e.setAttribute("ERROR", error);
		}

		return e;
	}

	/**
	 * Check if the LDAP Server supports a Control
	 * 
	 * @param oid
	 *            The Control oid
	 * @return true if the server supports that Control
	 */
	public boolean supportsControl(String oid) {
		Entry e = getServerInfo();
		if (e.getString("error") != null) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.UNABLE.TO.QUERY.SERVER.INFORMATION", e.getString("error")));
			return false;
		}
		if (e.getAttribute("supportedcontrol") == null) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.SERVER.DOES.NOT.TELL.US.SUPPORTED.CONTROLS"));
			return false;
		}

		if (e.getAttribute("supportedcontrol").hasValue(oid)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check if the LDAP Server supports an Extension
	 * 
	 * @param oid
	 *            The Extension oid
	 * @return true if the server supports that Extension
	 */
	public boolean supportsExtension(String oid) {
		Entry e = getServerInfo();
		if (e.getString("error") != null) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.UNABLE.TO.QUERY.SERVER.INFORMATION", e.getString("error")));
			return false;
		}
		if (e.getAttribute("supportedextension") == null) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.SERVER.DOES.NOT.TELL.US.SUPPORTED.EXTENSIONS"));
			return false;
		}

		if (e.getAttribute("supportedextension").hasValue(oid)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check if the LDAP Server supports paged results
	 * 
	 * @return true if the LDAP Server supports paged results
	 */
	public boolean supportsPagedResults() {
		return supportsControl(CONTROL_OID[PAGED_SEARCH_CONTROL]);
	}

	/**
	 * Check if the LDAP Server supports sorting
	 * 
	 * @return true if the LDAP Server supports sorting
	 */
	public boolean supportsSorting() {
		return supportsControl(CONTROL_OID[SERVER_SIDE_SORTING]);
	}

	/**
	 * Check if the LDAP Server supports virtual list view
	 * 
	 * @return true if the LDAP Server supports virtual list view
	 */
	public boolean supportsVirtualListView() {
		return supportsControl(CONTROL_OID[VIRTUAL_LIST_VIEW]);
	}

	/**
	 * Check if the LDAP Server supports Persistant Search
	 * 
	 * @return true if the LDAP Server supports Persistant Search
	 */
	public boolean supportsPersistantSearch() {
		return supportsControl(CONTROL_OID[PERSISTANT_SEARCH]);
	}

	/**
	 * Logs server information, supported controls and naming contexts
	 */
	public void showServerInfo() {

		int i;
		logmsg(sResHash.getString("CONNECTOR.LDAP.BEGIN.LDAP.SERVER.INFORMATION"));
		Entry e = getServerInfo();
		if (e.getString("error") != null) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.UNABLE.TO.GET.SERVER.INFO"));
			return;
		}

		logmsg(sResHash.getString("CONNECTOR.LDAP.SUPPORTED.CONTROLS.INFO"));
		for (i = 0; i <= LAST_CONTROL; i++) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.SUPPORTED.CONTROLS", new Object[] { CONTROL_LABEL[i],
					Boolean.valueOf(supportsControl(CONTROL_OID[i])) }));
		}

		logmsg(sResHash.getString("CONNECTOR.LDAP.NAMING.CONTEXTS.USE.IN.YOUR.SEARCH.BASE.PARAMETER"));

		com.ibm.di.entry.Attribute a = e.getAttribute("namingcontexts");
		if (a == null) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.NO.INFO.ON.NAMING.CONTEXTS"));
		} else {
			for (i = 0; i < a.size(); i++) {
				logmsg(sResHash.getString("CONNECTOR.LDAP.NAMINGCONTEXTS.VALUES",
						new Object[] { Integer.valueOf(i), a.getValue(i) }));
			}
		}
		logmsg(sResHash.getString("CONNECTOR.LDAP.END.LDAP.SERVER.INFORMATION"));
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.3-di7.1.1 %I%, 2017-09-06";
	}

	/**
	 * This method recursively calls itself to traverse the SearchCriteria
	 * vector held by a search object. The intenetion is to extract all $dn
	 * values and also abort if the search object contains other attributes than
	 * $dn. This is because we cannot/will not perform internal searching of
	 * entries returned by a $dn read.
	 * 
	 * @param dnlist
	 *            The feature to be added to the DNFromCriteria attribute
	 * @param crit
	 *            The feature to be added to the DNFromCriteria attribute
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	private void addDNFromCriteria(Vector<String> dnlist, Object crit) throws Exception {

		if (crit instanceof Vector<?>) {
			for (int i = 0; i < ((Vector<?>) crit).size(); i++) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAP.ADD.DN.FROM.CRITERIA.VECTOR.INSTANCE"));
				}
				addDNFromCriteria(dnlist, ((Vector<?>) crit).elementAt(i));
			}

		} else if (crit instanceof SearchCriteria) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.ADD.DN.FROM.CRITERIA.SEARCH.CRITERIA"));
			}
			addDNFromCriteria(dnlist, ((SearchCriteria) crit).getCriteria());

		} else if (crit instanceof SearchCriteria.rscSearch) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.ADD.DN.FROM.CRITERIA.SEARCH.RSCSEARCH"));
			}
			if (!((SearchCriteria.rscSearch) crit).name.equals("$dn")) {
				throw new Exception(sResHash.getString("CONNECTOR.LDAP.CANNOT.MIX.DN.MATCHING.WITH.OTHER.ATTR"));
			}

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAP.ADD.DN.FROM.CRITERIA.ADD.DN.VALUE",
						((SearchCriteria.rscSearch) crit).value.toString()));
			}
			dnlist.add(((SearchCriteria.rscSearch) crit).value.toString());

		} else {
			throw new Exception(sResHash.getString("CONNECTOR.LDAP.UNKNOWN.OBJECT.TYPE.IN.SEARCH.FILTER", new Object[] {
					crit.getClass(), crit }));
		}
	}

	/**
	 * setADPassword - Returns an Attribute (JNDI) containing the UNICODE
	 * version of a password for ActiveDirectory. The attribute name is
	 * pre-defined by AD to UNICODE_PASSWORD.
	 * 
	 * 
	 * @param password
	 *            The new aDPassword value
	 * @return Returns an Attribute (JNDI) containing the UNICODE version of a
	 *         password for ActiveDirectory. The attribute name is pre-defined
	 *         by AD to UNICODE_PASSWORD.
	 * @exception java.io.UnsupportedEncodingException
	 *                An exception is thrown if the encoding required by Active
	 *                Directory is not supported on this platform.
	 */
	public Attribute setADPassword(Object password) throws java.io.UnsupportedEncodingException {
		if (password == null) {
			return new BasicAttribute(UNICODE_PASSWORD);
		}
		String newPwd = "\"" + password.toString() + "\"";
		// Get a UTF-16LE encoding of the password string. UTF-16 would result
		// in
		// a Byte Order Mark (BOM) in the first two bytes, UTF-16LE does not.
		// The password string sent to Active Directory (AD) cannot have a BOM
		// in it.
		// AD requires Little Endian (LE).
		byte _bytes[] = newPwd.getBytes(LE_UNICODE);
		BasicAttribute attribute = new BasicAttribute(UNICODE_PASSWORD);
		attribute.add((byte[]) _bytes);
		return attribute;
	}

	/**
	 * Attempts to reauthenticate using the currently open connection. Calls
	 * rebind(dn,password,null).
	 * 
	 * @param dn
	 *            the distinguished name to rebind with
	 * @param password
	 *            the password for this distinguished name
	 * @throws NamingException
	 *             when underlying reconnect function fails
	 */
	public void rebind(String dn, String password) throws NamingException {
		rebind(dn, password, null);
	}

	/**
	 * Attempts to reauthenticate using the currently open connection.
	 * 
	 * @param dn
	 *            The DN used in the bind request
	 * @param password
	 *            The password used in the bind request
	 * @param authMethod
	 *            The authentication method
	 * @throws NamingException
	 *             when underlying reconnect function fails
	 */
	public void rebind(String dn, String password, String authMethod) throws NamingException {
		try {
			if (dn != null) {
				ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
			}
			if (password != null) {
				ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
			}
			if (authMethod != null) {
				ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, authMethod);
			}

			ctx.reconnect(null);
		} catch (NamingException err) {
			logmsg(sResHash.getString("CONNECTOR.LDAP.EXCEPTION.IN.LDAPCONNECTOR.REBIND"));
			throw (err);
		}
	}

	/**
	 * This connector is able to perform delta updates
	 * 
	 * @return true
	 */
	public boolean isDeltaSupported() {
		return true;
	}

	/**
	 * Sets the value for invoking error hook on attribute in use exception
	 * 
	 * @param errorHook
	 *            value to be set
	 * 
	 */
	public void callErrorHookOnAttributeInUseException(boolean errorHook) {
		callErrorHook = errorHook;
	}

	/**
	 * Returns true if Error hooks should be called on AttributeInUseException
	 * 
	 * @return true if Error hooks should be called on AttributeInUseException
	 */
	public boolean getCallErrorHook() {
		return callErrorHook;
	}

}
