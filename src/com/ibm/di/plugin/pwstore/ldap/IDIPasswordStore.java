/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.ldap;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.plugin.pwstore.BasePasswordChange;
import com.ibm.di.plugin.pwstore.PasswordChange;
import com.ibm.di.plugin.security.SecurityHelper;
import com.ibm.di.plugin.security.pki.IDIPasswordCrypto;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.StringTokenizer;

/**
 * <code>IDIPasswordStore</code> is the class that provides function to access
 * LDAP servers for the purpose of updating a specified server with userid and
 * password information. A properties file is read in when the object is
 * constructed. Information in the properties file specifies the credentials for
 * access to the server as well as other tailorable configuration information.
 * This information includes location of keystore files for SSL access and
 * asymmetric encryption using RSA of the password data (see the
 * IDIPasswordCrypto class for decryption).
 * 
 * The SSL connection processing assumes that the client keystore file which
 * contains both client's certificate and servers signer certification.
 * 
 * A simple usage would be as follows: When
 * stowPassword(uid,userfullname,password) is invoked, the ibm-diPerson object
 * defined in the LDAP DIT is modified to have the specified password. If the
 * ibm-diPerson object for the specified uid does not exist, a new one is
 * created.
 */
public class IDIPasswordStore {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private static final ResourceHash resHash = ResourceHash.getHash("ldappwstore");

	private static final String GENERALIZED_TIME_SYNTAX = "yyyyMMddHHmmss.SZ";

	// The jndi connection to the server.
	// established when the connection is initially made. All operations
	// to this server will use this ctx to avoid the overhead of continually
	// opening and closing sockets.

	private boolean bFirst_time = true; // used to validate suffix once.

	// private LdapContext ctx = null;

	// the environment used when instantiating this context.
	private Properties env = null;

	// variables also in the environment
	private String serverUrl = null;

	private String ldapLogInUserId = null;

	private String ldapLogInPassword = null;

	private boolean waitEnabled; // return true and thread for stowPassword

	private int delayMillis;

	private boolean sslEnabled;

	private boolean encEnabled; // encrypt passwords

	private String encKeyStorePath = null;

	private String encKeyStorePassword = null;

	private String encKeyStoreCertificate = null;

	private String encKeyStoreKeyPassword = null;

	private String suffix = "";

	// names used in schema, may be set in properties file
	private String diPersonObject = null;

	private String diUseridAttribute = null;

	private String diPasswordAttribute = null;

	private String diExtendedDataAttribute = null;

	private String diCustomDataAttribute = null;

	private String diTimestampAttribute = null;

	// holds encoded passwords for diagnostics
	private String ldapLogInPasswordENC = null;

	private String encKeyStorePasswordENC = null;

	private String encKeyStoreKeyPasswordENC = null;

	/*
	 * Constant to use for a Empty string
	 */
	private static final String EMPTY_STRING = "";

	/*
	 * Constant to use for a LDAP address
	 */
	private static final String ADDRESS_LDAP = "ldap://";

	/*
	 * Constant to use for a LDAP address colon
	 */
	private static final String ADDRESS_COLON = ":";

	/*
	 * default delay time when doing asyncrhonous store
	 */
	private int DELAY_TIME_DEFAULT = 2000;

	/*
	 * key names used in properties file
	 */
	// required properteies
	private static final String PF_HOST = "ldap.hostname";

	private static final String PF_PORT = "ldap.port";

	private static final String PF_LDAPLOGINID = "ldap.admindn";

	private static final String PF_LDAPLOGINPW = "ldap.password";

	private static final String PF_LDAPSUFFIX = "ldap.suffix";

	private static final String PF_SSL = "ldap.ssl";

	private static final String PF_ENC = "encrypt";

	private static final String PF_ENCKSPATH = "encryptKeyStoreFilePath";

	private static final String PF_ENCKSPW = "encryptKeyStoreFilePassword";

	private static final String PF_ENCKSCERT = "encryptKeyStoreCertificate";

	private static final String PF_ENCKSKEYPW = "encryptKeyPassword";

	private static final String PF_WAIT = "ldap.waitForStore";

	private static final String PF_DELAY = "ldap.delayMillis";

	// schema name keys
	private static final String PF_PERSONOBJECT = "ldap.schemaPersonObjectName";

	private static final String PF_USERIDATTR = "ldap.schemaUseridAttributeName";

	private static final String PF_PASSWORDATTR = "ldap.schemaPasswordAttributeName";

	private static final String PF_EXTDATAATTR = "schemaExtendedDataAttributeName";

	private static final String PF_CUSTDATAATTR = "schemaCustomDataAttributeName";

	private static final String PF_TIMESTAMPATTR = "schemaTimestampAttributeName";

	/*
	 * PASSWORD_ATTRIBUTE_NAME
	 */
	// note future changes to password encryption using
	// public key encryption will use ibm-diPassword as name
	private final static String PASSWORD_ATTRIBUTE_NAME = "ibm-diPassword";

	/*
	 * USERID_ATTRIBUTE_NAME
	 */
	private static final String USERID_ATTRIBUTE_NAME = "ibm-diUserId";

	/*
	 * EXTENDED_DATA_ATTRIBUTE_NAME
	 */
	private static final String EXTENDED_DATA_ATTRIBUTE_NAME = "ibm-diExtendedData";

	/*
	 * CUSTOM_DATA_ATTRIBUTE_NAME
	 */
	private static final String CUSTOM_DATA_ATTRIBUTE_NAME = "ibm-diCustomData";

	/*
	 * TIMESTAMP_ATTRIBUTE_NAME
	 */
	private static final String TIMESTAMP_ATTRIBUTE_NAME = "ibm-diTimestamp";

	/*
	 * PERSON_OBJECT_NAME
	 */
	private static final String PERSON_OBJECT_NAME = "ibm-diPerson";

	private static final String PREFIX = "LDAPStore";

	private PWSyncLog log = null;

	/**
	 * Construct and initialize an IDIPasswordStore object. The initialization
	 * includes loading the properties file.
	 * 
	 * The properties file: idipwsync.props must be located in a directory on
	 * the CLASSPATH environment setting. To generate a template properties file
	 * that encodes passwords for the keystore and ldap login use "java
	 * com.ibm.di.plugin.idipwsync.GenPropertiesFile". Refer to
	 * readme_idipwsync.html for details on setting up a properties file.
	 * 
	 * @param log
	 *            the place to log in
	 * 
	 * @exception java.io.IOException
	 *                Thrown when attempting to load properties file
	 * 
	 */
	public IDIPasswordStore(PWSyncLog log) throws java.io.IOException {
		this.log = log;
		this.init();
	}

	/*
	 * add new instance of person to tree for specified userId.
	 * 
	 * @param uid a String representing the stored uid, must have lenth > 0
	 * 
	 * @param password a Vector of Strings representing password (encrypted
	 * based on properties file settings) @return boolean true if succesful
	 */
	private boolean addNewObject(String uid, Vector<String> newPasswords, LdapContext ctx) {
		return addNewObject(uid, newPasswords, null,null, ctx);
	}

	/*
	 * add new instance of person to tree for userId.
	 * 
	 * @param uid a String representing the stored uid, must have lenth > 0
	 * 
	 * @param password a Vector of Strings representing password (encrypted
	 * based on properties file settings) @param extendedData a String
	 * representing extra data (eg.the username as displayed by Windows NT, eg.
	 * 
	 * @return boolean true if succesful
	 */
	private boolean addNewObject(String uid, Vector<String> newPasswords, String extendedData, String customData,
			LdapContext ctx) {

		String FULL_ATTR_NAME = diUseridAttribute + "=" + uid + "," + suffix;
		boolean bNoErrors = true; // assume the best

		log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.ADDING.PERSON.ENTRY", FULL_ATTR_NAME));

		{

			try {

				Attributes attrs = new BasicAttributes(true); // case-ignore

				Attribute objclass = new BasicAttribute("objectclass");
				if (diPersonObject.indexOf(",") == -1) {
					objclass.add(diPersonObject);
				} else {
					for (StringTokenizer st = new StringTokenizer(diPersonObject, ","); st.hasMoreTokens();) {
						String token = st.nextToken().trim();
						objclass.add(token);
					}
				}

				if (notNull(extendedData)) {
					if (extendedData.length() == 0) {
						// change empty string to one blank, because Active
						// Directory does not support empty string values
						extendedData = " ";
					}
					attrs.put(diExtendedDataAttribute, extendedData);
				}

				attrs.put(objclass);
				attrs.put(diUseridAttribute, uid);
				// attrs.put(diPasswordAttribute,(String)
				// newPasswords.elementAt(0));

				ctx.createSubcontext(FULL_ATTR_NAME, attrs);

				if (newPasswords != null && newPasswords.size() > 0) {

					// put multiple values
					List<ModificationItem> modAttrs = new ArrayList<ModificationItem>();
					Enumeration<String> e = newPasswords.elements();
					String pw = null;
					int i = 0;
					while (e.hasMoreElements()) {
						pw = e.nextElement();
						if (notNull(pw)) {
							modAttrs.add(new ModificationItem(DirContext.ADD_ATTRIBUTE,
									new BasicAttribute(diPasswordAttribute, pw)));
							i = i + 1;
						}

					}
					
					if (notNull(customData) && customData.length() > 0) {
						modAttrs.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(diCustomDataAttribute,
								customData)));
					}
					
					// Modify person object
					ctx.modifyAttributes(FULL_ATTR_NAME, modAttrs.toArray(new ModificationItem[modAttrs.size()]));
				}

				log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.ADDED.PERSON.ENTRY", uid));

			} catch (Exception e) {
				bNoErrors = false;
				log.error(PREFIX, resHash.getString("PWSTORE.LDAP.ADD.PERSON.ENTRY.FAILED", new Object[] { FULL_ATTR_NAME, e }));
			}

		}

		return bNoErrors;
	}

	/*
	 * conditionalTranslate: If input vector of strings exist, and if encryption
	 * is enabled, and if bEncrypt is true, encrypt the entries. Any null
	 * entries are removed. Empty strings are returned as received. If bEncrypt
	 * is false, decrpyt the entries If encryption is disabled, return vector
	 * as-is If empty vector is recieved, return null;
	 * 
	 * @param passwordsIn a Vector of Strings representing password
	 * (encrypted/decrypted based on properties file settings) @param bEncrypt a
	 * boolean, if true perform encryption if enabled; if false, perform
	 * decryption. @return Vector of strings
	 */
	private Vector<String> conditionalTranslate(Vector<String> passwordsIn, boolean bEncrypt) {

		if (!exists(passwordsIn)) {
			return null;
		}

		Vector<String> passwordsOut = new Vector<String>(passwordsIn.size());

		try {

			String currentStr = null;
			for (int i = 0; i < passwordsIn.size(); i++) {
				currentStr = passwordsIn.elementAt(i);
				if (notNull(currentStr)) {
					if (encEnabled) {
						// ENCRYPT or Decrypt
						if (bEncrypt) {
							// encrypt input and place in output
							passwordsOut.addElement(IDIPasswordCrypto.encrypt(currentStr, this.encKeyStorePath,
									this.encKeyStorePassword, this.encKeyStoreCertificate));
						} else {
							// decrypt input and place in output
							passwordsOut.addElement(IDIPasswordCrypto.decrypt(currentStr, this.encKeyStorePath,
									this.encKeyStorePassword, this.encKeyStoreCertificate, this.encKeyStoreKeyPassword));

						}
					} else {
						// Translation not required
						passwordsOut.addElement(currentStr);
					}
				} // end notNull(currentStr)
			} // end for

		} catch (java.lang.Exception e) {
			if (bEncrypt) {
				log.error(PREFIX, resHash.getString("PWSTORE.LDAP.PASSWORD.ENCRYPTION.FAILED", e));
			} else {
				log.error(PREFIX, resHash.getString("PWSTORE.LDAP.PASSWORD.DECRYPTION.FAILED", e));
			}
			return null; // exit now
		}

		return passwordsOut;
	}

	/*
	 * Initialzes using information: Obtain host, port, ldapLogInUserId,
	 * ldapLogInPassword, keyStoreFilePath and keyStoreFilePassword from a
	 * properties file and do setup.
	 * 
	 * 
	 * @exception java.io.IOException Thrown when attempting to load properties
	 * file, or setting up trace file
	 * 
	 * @exception IDIPasswordStoreMissingPropertyException Thrown when
	 * attempting to load a required property
	 */
	private void init() throws java.io.IOException {

		this.loadProperties();

		// initialize the environment.
		this.env = new Properties();

		setNonSSLEnv();

		// Now, set up the environment based on whether we are
		// using SSL or not.
		if (sslEnabled) {
			setSSLEnv();
		}

		// invoke encryption of a string to initialize encryption provider
		// this moves the time delay necessary to initialize the encryption
		// provider
		// from the time of the first password update in the initialization of
		// the Password Store
		if (encEnabled) {
			try {
				IDIPasswordCrypto.encrypt("init", this.encKeyStorePath, this.encKeyStorePassword, this.encKeyStoreCertificate);
			} catch (Exception e) {
				log.error(PREFIX, resHash.getString("PWSTORE.LDAP.CRYPTO.INIT.FAILED", e));
			}
		}
	}

	/*
	 * Load and verify properties.
	 * 
	 * @exception java.io.IOException Thrown when attempting to load properties
	 * file, or setting up trace file.
	 * 
	 * @exception IDIPasswordStoreMissingPropertyException Thrown when
	 * attempting to load a required property.
	 */
	private void loadProperties() throws java.io.IOException {

		// local work variables
		String host;
		String port;
		String suffixProp;
		String sslProp;
		String encProp;
		String waitForProp;

		// check for required properties
		host = getRequiredProperty(PF_HOST);
		port = getRequiredProperty(PF_PORT);
		ldapLogInUserId = getRequiredProperty(PF_LDAPLOGINID);
		ldapLogInPasswordENC = getRequiredProperty(PF_LDAPLOGINPW);
		suffixProp = getRequiredProperty(PF_LDAPSUFFIX);
		sslProp = System.getProperty(PF_SSL, "false").trim();
		encProp = System.getProperty(PF_ENC, "false").trim();
		waitForProp = getRequiredProperty(PF_WAIT);

		// verify setting to control async store of password
		if ("true".equalsIgnoreCase(waitForProp) || "1".equals(waitForProp)) {
			this.waitEnabled = true;
		} else {
			this.waitEnabled = false;
			// if not waiting, get delay time used
			// for wait prior to starting thread to
			// avoid contention with Active Directory
			// (for scenario when AD is on same machine)
			try {
				String delayMillisString = System.getProperty(PF_DELAY).trim();
				delayMillis = Integer.parseInt(delayMillisString);

			} catch (NullPointerException e) {
				// no delay specified, set using default
				delayMillis = DELAY_TIME_DEFAULT;
			} catch (Exception e) {
				// mis-specification use default
				delayMillis = DELAY_TIME_DEFAULT;
			}
		}

		// verify ssl settings
		if ("true".equalsIgnoreCase(sslProp) || "1".equals(sslProp)) {
			this.sslEnabled = true;
		} else {
			this.sslEnabled = false;
		}

		// verify encrytion settings
		if ("true".equalsIgnoreCase(encProp) || "1".equals(encProp)) {
			this.encEnabled = true;
			// optional encryption related parms, but if flag is set
			// both are required
			encKeyStorePath = getRequiredProperty(PF_ENCKSPATH);
			encKeyStorePasswordENC = getRequiredProperty(PF_ENCKSPW);
			encKeyStoreCertificate = getRequiredProperty(PF_ENCKSCERT);
			encKeyStoreKeyPasswordENC = getRequiredProperty(PF_ENCKSKEYPW);

		} else {
			this.encEnabled = false;
			this.encKeyStorePath = EMPTY_STRING;
			this.encKeyStorePasswordENC = EMPTY_STRING;
			this.encKeyStoreKeyPasswordENC = EMPTY_STRING;
		}

		// optional schema name mappings
		diPersonObject = System.getProperty(PF_PERSONOBJECT, PERSON_OBJECT_NAME).trim();
		diUseridAttribute = System.getProperty(PF_USERIDATTR, USERID_ATTRIBUTE_NAME).trim();
		diPasswordAttribute = System.getProperty(PF_PASSWORDATTR, PASSWORD_ATTRIBUTE_NAME).trim();
		diExtendedDataAttribute = System.getProperty(PF_EXTDATAATTR, EXTENDED_DATA_ATTRIBUTE_NAME).trim();
		diCustomDataAttribute = System.getProperty(PF_CUSTDATAATTR, CUSTOM_DATA_ATTRIBUTE_NAME).trim();
		diTimestampAttribute = System.getProperty(PF_TIMESTAMPATTR, TIMESTAMP_ATTRIBUTE_NAME).trim();

		// build ldap server URL
		StringBuffer sbHost = new StringBuffer();
		sbHost.append(ADDRESS_LDAP).append(host).append(ADDRESS_COLON).append(port);
		this.serverUrl = sbHost.toString();

		// save work variables
		this.suffix = suffixProp;
		// decode the passwords
		this.ldapLogInPassword = SecurityHelper.getClearText(ldapLogInPasswordENC);
		this.encKeyStorePassword = SecurityHelper.getClearText(encKeyStorePasswordENC);
		this.encKeyStoreKeyPassword = SecurityHelper.getClearText(encKeyStoreKeyPasswordENC);
	}

	/*
	 * attempt to obtain property and verify it has length >0
	 */
	private String getRequiredProperty(String propname) throws NoSuchElementException {
		String propvalue = "";

		propvalue = System.getProperty(propname, "").trim();
		if (propvalue.length() <= 0) {
			throw new NoSuchElementException(resHash.getString("PWSTORE.LDAP.MISSING.REQUIRED.PROPERTY", propname));
		}

		return propvalue;
	}

	/*
	 * Opens connection to the LDAP server using specified environment, verifies
	 * suffix/container against directory configuration.
	 * 
	 * @exception javax.naming.NamingException Thrown when obtaining connection
	 * to LDAP server @exception javax.naming.AuthenticationException Thrown
	 * when obtaining connection to LDAP server probably invalid credentials
	 * provided or missing keyStore file @exception
	 * javax.naming.CommunicationException Thrown when obtaining connection to
	 * LDAP server probably incorrect port specified
	 * 
	 * @return LdapContext a directory context object, null if not obtainable
	 */
	private synchronized LdapContext openConnection() throws Exception {

		LdapContext context = null;

		try {
			context = new InitialLdapContext(env, null);
		} catch (Exception e) {
			String msg = resHash.getString("PWSTORE.LDAP.CONNECT.FAILED", e);
			log.error(msg);
			throw new Exception(msg, e);
		}

		// first time verify that the container/suffix specified in properties
		// file is valid in the
		// directories schema
		if (bFirst_time && context != null) {
			bFirst_time = false;
			verifySuffix(context);
		}

		return context;
	}

	/*
	 * close a specified context/connection with server
	 */
	private synchronized int closeConnection(LdapContext aContext) {
		int rc = 0;
		try {
			if (aContext != null) {
				aContext.close();
			}
		} catch (Exception e) {

			rc = 1;
		}
		return rc;
	}

	/*
	 * Method sets up the environment for non-ssl binding.
	 */
	private void setNonSSLEnv() {
		env.put("java.naming.security.principal", this.ldapLogInUserId);
		env.put("java.naming.security.credentials", this.ldapLogInPassword);
		env.put("java.naming.ldap.version", "3");
		env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
		env.put("java.naming.provider.url", this.serverUrl);
		env.put(Context.REFERRAL, "ignore");
		env.put("java.naming.ldap.derefAliases", "never");

	}

	/*
	 * Method sets up the environment for ssl binding.
	 */
	private void setSSLEnv() {
		env.put("java.naming.security.authentication", "simple");
		env.put(Context.SECURITY_PROTOCOL, "ssl");
		env.put("java.naming.ldap.factory.socket", "javax.net.ssl.SSLSocketFactory");
	}

	/**
	 * This method adds the password values specified, for specified uid.
	 * 
	 * The clear text password to be optionally encrypted (see properties file
	 * documentation) before LDAP server stores it, and a decryption method (see
	 * IDIPasswordCrypto class) is available for decrypting via an IDI
	 * AssemblyLine or other strategy.
	 * 
	 * Null passwords will not be stored. Zero length passwords will be encoded
	 * and encrypted and will required decoding via IDIPasswordCrypto class.
	 * 
	 * Other functional behavior controlled includes performing LDAP updates in
	 * asynchronous mode with a configurable delay time (necessary when dealing
	 * with certain AD configurations (due to locking mechanism).
	 * 
	 * @param uid
	 *            A String representing the stored uid, must have lenth > 0, eg.
	 *            bcampbell.
	 * @param newPasswords
	 *            A vector representing stored, decoded passwords, vector must
	 *            have length > 0, null entries are not stored.
	 * 
	 * @return boolean true if successful.
	 */
	public boolean addPasswordValues(final PasswordChange change) {

		if (this.waitEnabled) {
			return (inner_addPasswordValues(change));
		} else {
			Thread stowThread = new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(delayMillis);
					} catch (InterruptedException e) {
					}

					inner_addPasswordValues(change);
				}
			});

			stowThread.start();

			return true;
		}
	}

	@Deprecated
	public boolean addPasswordValues(final String uid, final Vector<String> newPasswords) {
		return addPasswordValues(new BasePasswordChange(uid, newPasswords));
	}

	/**
	 * inner_addPasswordValues: see addPasswordValues(String,Vector) for
	 * description
	 * 
	 * @param uid
	 *            a String representing the stored uid, must have length > 0,
	 *            eg. bcampbell
	 * @param addPasswordsIn
	 *            a vector of Strings representing stored, decoded passwords
	 */
	private synchronized boolean inner_addPasswordValues(final PasswordChange change) {

		LdapContext ctx = null;
		String uid = change.getID();

		String FULL_ATTR_NAME = diUseridAttribute + "=" + uid + "," + suffix;
		boolean bNoErrors = true; // assume the best

		Vector<String> addPasswordsENC = null;

		log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.ADDING.PASSWORD.VALUES", FULL_ATTR_NAME));

		// check input parms
		if (!exists(uid)) {
			bNoErrors = false; // parm failure
			log.error(PREFIX, resHash.getString("PWSTORE.LDAP.ADD.VALUES.FAILED.EMPTY.USER"));
			return bNoErrors; // exit now
		}

		// encrypt if requested, filter out null entries.
		addPasswordsENC = conditionalTranslate(change.getPasswords(), true);

		// validate encrypt if necessary
		if (exists(addPasswordsENC)) {
			try {
				bNoErrors = ((ctx = openConnection()) != null);
			} catch (Exception e) {
				log.error(PREFIX, resHash.getString("PWSTORE.LDAP.ADD.VALUES.FAILED.CANNOT.CONNECT", new Object[] { uid, e }));
				bNoErrors = false;
			}

			// add password values,
			if (bNoErrors) {
				try {
					// attempt to add passwords; put multiple values
					List<ModificationItem> modAttrs = new ArrayList<ModificationItem>();
					Enumeration<String> e = addPasswordsENC.elements();

					// at this point, null entries have already been filtered
					while (e.hasMoreElements()) {
						modAttrs.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(diPasswordAttribute, e
								.nextElement())));
					}

					if (change.getCustomData() != null && change.getCustomData().length() > 0) {
						modAttrs.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(diCustomDataAttribute,
								change.getCustomData())));
					}

					SimpleDateFormat sdf = new SimpleDateFormat(GENERALIZED_TIME_SYNTAX);

					// always replace because we need timestamp only for the
					// last change
					modAttrs.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(diTimestampAttribute, sdf
							.format(new Date(change.getTimestamp())))));

					// Modify person object
					ctx.modifyAttributes(FULL_ATTR_NAME, modAttrs.toArray(new ModificationItem[modAttrs.size()]));

				} catch (javax.naming.NameNotFoundException e) {
					log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.ADD.VALUES.NO.PERSON.ENTRY", uid));
					bNoErrors = addNewObject(uid, addPasswordsENC, null, change.getCustomData(), ctx);
				} catch (Exception e) {
					log.error(PREFIX, resHash.getString("PWSTORE.LDAP.ADD.VALUES.FAILED", new Object[] { uid, e }));
					bNoErrors = false;
				}
			}
		} else {
			// null vector, or encryption failure
			// message already logged in conditionalEncrypt
			bNoErrors = false;
		}

		closeConnection(ctx);

		return bNoErrors;
	}

	/**
	 * deletePasswordValues: removes the password values specified, for
	 * specified ui
	 * 
	 * The clear text password to be optionally encrypted (see properties file
	 * documentation) before LDAP server stores it, and a decryption method (see
	 * IDIPasswordCrypto class) is available for decrypting via an IDI
	 * assemblyline or other strategy.
	 * 
	 * Null passwords will not be processed. Zero length passwords will be
	 * encoded and encrypted for matching via the IDIPasswordCrypto class.
	 * 
	 * Other functional behavior controlled includes performing LDAP udates in
	 * async mode with a configurable delay time (necessary when dealing with
	 * certain AD configurations (due to locking mechanism).
	 * 
	 * @param uid
	 *            A String representing the stored uid, must have lenth > 0, eg.
	 *            bcampbell
	 * @param newPasswords
	 *            A vector representing decoded passwords to be removed, must
	 *            have length > 0, null entries are not processed.
	 * 
	 * @return boolean true if succesful
	 */
	public boolean deletePasswordValues(final PasswordChange change) {
		if (this.waitEnabled) {
			return (inner_deletePasswordValues(change));
		} else {
			Thread stowThread = new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(delayMillis);
					} catch (InterruptedException e) {
					}

					inner_deletePasswordValues(change);
				}
			});
			stowThread.start();

			return true;
		}
	}

	@Deprecated
	public boolean deletePasswordValues(final String uid, final Vector<String> newPasswords) {
		return deletePasswordValues(new BasePasswordChange(uid, newPasswords));
	}

	/**
	 * inner_deletePasswordValues: see deletePasswordValues(String,Vector) for
	 * description.
	 * 
	 * @param uid
	 *            a String representing the stored uid, must have lenth > 0, eg.
	 *            bcampbell
	 * @param password
	 *            a vector representing decoded passwords to be removed, must
	 *            have length > 0, null entries not processed
	 * 
	 * @return boolean true if successful
	 */
	private synchronized boolean inner_deletePasswordValues(PasswordChange change) {

		LdapContext ctx = null;
		String uid = change.getID();
		String FULL_ATTR_NAME = diUseridAttribute + "=" + uid + "," + suffix;
		boolean bNoErrors = true; // assume the best

		log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.DELETING.PASSWORD.VALUES", FULL_ATTR_NAME));

		// check input parms
		if (!exists(uid)) {
			bNoErrors = false; // parm failure
			log.error(PREFIX, resHash.getString("PWSTORE.LDAP.DELETE.VALUES.FAILED.EMPTY.USER"));
			return bNoErrors; // exit now
		}

		// delPasswordsENC = conditionalEncrypt(delPasswordsIn);
		// validate encrypt if necessary

		if (exists(change.getPasswords())) {

			Attributes attrs = null;
			String[] attrIDs = { diPasswordAttribute };
			Vector<String> rawList = new Vector<String>();
			Vector<String> addList = new Vector<String>(); // grow as needed

			try {
				// *** initialize context *****
				bNoErrors = ((ctx = openConnection()) != null);

				// obtain a collection of 1: diPasswordAttribute
				attrs = ctx.getAttributes(FULL_ATTR_NAME, attrIDs);

				BasicAttribute aMVPW = null;
				NamingEnumeration<?> existingPWS = null;

				// obtain the diPassword multi-valued attribute
				aMVPW = (BasicAttribute) attrs.get(diPasswordAttribute);
				// obtain enumeration of all attributes
				existingPWS = aMVPW.getAll();
				// multi-values for diPasswordAttribute

				String existingValue = null; // hold the Object (a string)

				// create vector of existing password strings
				while (existingPWS.hasMore()) {
					existingValue = ((Object) existingPWS.nextElement()).toString();
					rawList.addElement(existingValue);
				}

			} catch (Exception e) {
				log.error(PREFIX, resHash.getString("PWSTORE.LDAP.DELETE.VALUES.FAILED.CANNOT.OBTAIN.ORIGINAL.VALUES",
						new Object[] { uid, e }));
				bNoErrors = false;
			}

			if (bNoErrors) {

				// obtain decrypted version
				Vector<String> translatedList = conditionalTranslate(rawList, false);
				Enumeration<String> translatedEnum = translatedList.elements();

				String translatedValue = null;
				String deleteValue = null;
				boolean found = false;

				// find items to remove
				while (translatedEnum.hasMoreElements()) {
					translatedValue = translatedEnum.nextElement();
					Enumeration<String> deletePWS = change.getPasswords().elements();
					// refresh each iteration
					found = false;
					while (deletePWS.hasMoreElements() && found == false) {
						deleteValue = deletePWS.nextElement();
						// if NOT in delete set, keep it
						if (deleteValue.equals(translatedValue)) {
							found = true; // force exit
						}
					}
					if (found == false) {
						addList.addElement(translatedValue);
					}
				}

				// encode the addlist
				addList = conditionalTranslate(addList, true);
			}

			try {
				// attempt to remove password attribute
				// if no entries are left to add back, ok
				ModificationItem[] mods = new ModificationItem[1];
				Attribute mod0 = new BasicAttribute(diPasswordAttribute);
				mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, mod0);

				// Modify person object
				// bNoErrors = this.initCtx();
				ctx.modifyAttributes(FULL_ATTR_NAME, mods);
				// success, now add new values for password attribute
			} catch (Exception e) {
				log.error(PREFIX, resHash.getString("PWSTORE.LDAP.DELETE.VALUES.FAILED.1", new Object[] { uid, e }));
				bNoErrors = false;
			}

			if (bNoErrors && exists(addList)) {
				// only add items if addlist has entries
				// create modification list;
				ModificationItem[] addAttrs = new ModificationItem[addList.size()];
				if (addList.size() > 0) {
					int i = 0;
					Enumeration<String> addPWS = addList.elements();
					while (addPWS.hasMoreElements()) {
						addAttrs[i] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(diPasswordAttribute, addPWS
								.nextElement()));
						i = i + 1;
					}
				}

				try {

					// add back non-deleted passwords
					// bNoErrors = this.initCtx();
					ctx.modifyAttributes(FULL_ATTR_NAME, addAttrs);
				} catch (Exception e) {
					log.error(PREFIX, resHash.getString("PWSTORE.LDAP.DELETE.VALUES.FAILED.1", new Object[] { uid, e }));
					bNoErrors = false;
				}
			}

		} // end if
		else {
			// null delete item vector, or encryption failure
			// message already logged in conditionalEncrypt
			bNoErrors = false;
		}

		closeConnection(ctx);

		return bNoErrors;
	}

	/**
	 * readyToSync: attempt initctx to see if LDAP server is available
	 * 
	 * @return boolean true if succesf
	 * 
	 * 
	 */
	public boolean readyToSync() {
		boolean retval = false;

		// test for lock problem
		if (waitEnabled) {
			LdapContext ctx = null;

			log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.CHECK.REPOSITORY"));
			try {
				ctx = openConnection();
				if (ctx != null) {
					retval = true;
					closeConnection(ctx);
				}
			} catch (Exception e) {
				retval = false;
			}

		} else {
			retval = true; // always return true when not waiting for store
		}
		return retval;
	}

	/**
	 * stowPassword: Changes the password if the user id exists. If password
	 * vector specified is null or zero-length, password attribute will be
	 * removed from the object for specified uid.
	 * 
	 * Otherwise, create a new entry.
	 * 
	 * The clear text password to be optionally encrypted (see properties file
	 * documentation) before LDAP server stores it, and a decryption method (see
	 * IDIPasswordCrypto class) is available for decrypting via an IDI
	 * assemblyline or other strategy.
	 * 
	 * Null passwords will not be stored. Zero length passwords will be encoded
	 * and encrypted and will required decodeing via IDIPasswordCrypto class.
	 * 
	 * Other functional behavior controlled includes performing LDAP udates in
	 * async mode with a configurable delay time (necessary when dealing with
	 * certain AD configurations (due to locking mechanism).
	 * 
	 * @param uid
	 *            A String representing the stored uid, must have lenth > 0, eg.
	 *            bcampbell
	 * @param newPasswords
	 *            A vector representing stored, decoded password, vector must
	 *            have length > 0, null entries will not be stored.
	 * 
	 * @return boolean true if successful
	 * 
	 * 
	 */
	public boolean modifyPassword(final PasswordChange change) {
		if (this.waitEnabled) {
			return (inner_stowPassword(change));
		} else {
			Thread stowThread = new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(delayMillis);
					} catch (InterruptedException e) {
					}

					inner_stowPassword(change);
				}
			});
			stowThread.start();

			return true;
		}
	}

	@Deprecated
	public boolean stowPassword(final String uid, final Vector<String> newPasswords) {
		return inner_stowPassword(new BasePasswordChange(uid, newPasswords));
	}

	/*
	 * see stowPassword(string,vector) for description
	 * 
	 * @param uid a String representing the stored uid, must have lenth > 0, eg.
	 * bcampbell @param password a vector of Strings representing stored,
	 * decoded passwo
	 */
	private synchronized boolean inner_stowPassword(PasswordChange change) {

		LdapContext ctx = null;
		String uid = change.getID();
		String FULL_ATTR_NAME = diUseridAttribute + "=" + uid + "," + suffix;
		boolean bNoErrors = true; // assume the best

		Vector<String> newPasswordsENC = null;

		log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.MODIFYING.PASSWORD.VALUES", FULL_ATTR_NAME));

		// check input parms
		if (!exists(uid)) {
			bNoErrors = false; // parm failure
			log.error(PREFIX, resHash.getString("PWSTORE.LDAP.MODIFYING.VALUES.FAILED.EMPTY.USER"));
			return bNoErrors; // exit now
		}

		if (!exists(change.getPasswords())) {
			log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.MODIFYING.PASSWORD.VALUES.EMPTY.PASS.LIST"));
			// remove password attribute for this userid
			try {
				bNoErrors = removePassword(uid);
				return bNoErrors; // exit now
			} catch (javax.naming.NameNotFoundException e) {

				log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.MODIFY.VALUES.NO.PERSON.ENTRY", uid));

				try {
					bNoErrors = (ctx = openConnection()) != null;
					if (bNoErrors) {
						bNoErrors = addNewObject(uid, null, ctx);
						closeConnection(ctx);
					}
				} catch (Exception ex) {
					log.error(PREFIX, resHash.getString("PWSTORE.LDAP.MODIFY.VALUES.FAILED", new Object[] { uid, ex }));
					bNoErrors = false;
				}
				return bNoErrors; // exit now
			}
		} else {
			log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.MODIFY.VALUES.COUNT", change.getPasswords().size()));
			newPasswordsENC = conditionalTranslate(change.getPasswords(), true);
			// encrypt if necessary
		}

		if (exists(newPasswordsENC)) {
			// do replace
			try {

				// **** initialize context ******/
				bNoErrors = ((ctx = openConnection()) != null);

				// replace password values, if object doesn't exist, create it
				if (bNoErrors) {
					BasicAttribute encPassword = new BasicAttribute(diPasswordAttribute);
					for (int i = 0; i < newPasswordsENC.size(); i++) {
						encPassword.add(newPasswordsENC.get(i));
					}

					List<ModificationItem> modAttrs = new ArrayList<ModificationItem>();
					modAttrs.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, encPassword));

					// custom data
					if (change.getCustomData() != null && change.getCustomData().length() > 0) {
						modAttrs.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(diCustomDataAttribute,
								change.getCustomData())));
					}

					SimpleDateFormat sdf = new SimpleDateFormat(GENERALIZED_TIME_SYNTAX);

					// always replace because we need timestamp only for the
					// last change
					modAttrs.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(diTimestampAttribute, sdf
							.format(new Date(change.getTimestamp())))));

					// Modify person object
					ctx.modifyAttributes(FULL_ATTR_NAME, modAttrs.toArray(new ModificationItem[1]));
				}
			} catch (javax.naming.NameNotFoundException e) {
				log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.MODIFY.VALUES.NO.PERSON.ENTRY.2", uid));
				bNoErrors = addNewObject(uid, newPasswordsENC, null, change.getCustomData(), ctx);

			} catch (Exception e) {
				log.error(PREFIX, resHash.getString("PWSTORE.LDAP.MODIFY.VALUES.FAILED.2", new Object[] { uid, e }));
				bNoErrors = false;
			}
		} else {
			// null vector, or encryption failure
			// message already logged in conditionalEncrypt
			bNoErrors = false;
		}

		closeConnection(ctx);

		return bNoErrors;
	}

	/*
	 * Removes the dipassword attribute from the object for specified userid. If
	 * multiple values exist, all will be removed. If attribute isn't found, no
	 * action taken. If named object not found, no action taken
	 * 
	 * @param uid a String representing the stored uid, must have lenth > 0, eg.
	 * bcampbell @return boolean true if succesf
	 */
	private boolean removePassword(String uid) throws javax.naming.NameNotFoundException {
		boolean bNoErrors = true; // assume the best
		LdapContext ctx = null;
		String FULL_ATTR_NAME = diUseridAttribute + "=" + uid + "," + suffix;

		try {
			bNoErrors = ((ctx = openConnection()) != null);
			// replace attribute, one is created if it doesn't exist
			if (bNoErrors) {
				ModificationItem[] mods = new ModificationItem[1];
				Attribute mod0 = new BasicAttribute(diPasswordAttribute);
				mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, mod0);

				// Modify person object
				ctx.modifyAttributes(FULL_ATTR_NAME, mods);
			}
		} catch (javax.naming.directory.NoSuchAttributeException e) {
			log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.REMOVE.PASS.ATTR.NOT.FOUND", uid));
			// not an error
		} catch (javax.naming.NameNotFoundException e) {
			// not an error, rethrow
			throw new javax.naming.NameNotFoundException(e.getMessage());
			// rethrow it
		} catch (Exception e) {
			log.error(PREFIX, resHash.getString("PWSTORE.LDAP.REMOVE.PASS.FAILED", new Object[] { uid, e }));
			bNoErrors = false;
		}

		closeConnection(ctx);
		return bNoErrors;
	}

	/*
	 * return true if string value is NOT null @param testString String to be
	 * tested @return boolean true if input is NOT null
	 */
	private boolean notNull(String testString) {

		return (testString != null);

	}

	/*
	 * check if string value exists, not null, not 0-length @param testString
	 * String to be tested @return boolean false if input is null or 0 length
	 */
	private boolean exists(String testString) {
		if ((testString == null) || (testString.length() == 0)) {
			return false; // parm failure
		} else {
			return true;
		}

	}

	/*
	 * check if vector value exists, not null, not 0-length @param testVector
	 * Vector to be tested @return boolean false if input is null or 0 size
	 */
	private boolean exists(Vector<String> testVector) {
		if ((testVector == null) || (testVector.size() == 0)) {
			return false; // parm failure
		} else {
			return true;
		}

	}

	/*
	 * verify that sufix/container exists within directory configuration
	 */
	private boolean verifySuffix(LdapContext context) {
		boolean bResult = true; // assume best
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.OBJECT_SCOPE);

		log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.VERIFYING.LDAP.SUFFIX"));

		try {
			context.search(suffix, "objectClass=*", constraints);
		} catch (javax.naming.NameNotFoundException nnf) {
			// Must catch this since it is a search operation
			// now we know our suffix from the properties file wasn't set up in
			// the directory
			// log error to tell user.....
			log.error(PREFIX, resHash.getString("PWSTORE.LDAP.SUFFIX.DOES.NOT.EXIST", new Object[] { suffix, nnf }));
			bResult = false;

		} catch (Exception e) {
			// log error unxpected
			log.error(PREFIX, resHash.getString("PWSTORE.LDAP.VERIFY.SUFFIX.FAILED", e));
			bResult = false;
		}
		return bResult;
	}

	/**
	 * Write additional information about a user to the Password Store.
	 * 
	 * @param id
	 *            The user identifier.
	 * @param extendedData
	 *            The information.
	 * @return Whether the operation succeeded.
	 */
	public boolean setExtendedData(PasswordChange change) {

		boolean bNoErrors = true;
		LdapContext ctx = null;
		String extendedData = change.getExtData();
		String id = change.getID();
		String FULL_ATTR_NAME = diUseridAttribute + "=" + id + "," + suffix;

		log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.SET.EXT.DATA", new Object[] { extendedData, FULL_ATTR_NAME }));

		try {
			ctx = openConnection();

			try {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(diExtendedDataAttribute,
						extendedData));

				ctx.modifyAttributes(FULL_ATTR_NAME, mods);
			} catch (javax.naming.NameNotFoundException e) {
				log.debug(PREFIX, resHash.getString("PWSTORE.LDAP.SET.EXT.DATA.NO.PERSON.ENTRY", id));
				bNoErrors = addNewObject(id, null, extendedData, null, ctx);
			}

		} catch (Exception ex) {
			log.error(PREFIX, resHash.getString("PWSTORE.LDAP.SET.EXT.DATA.FAILED", new Object[] { FULL_ATTR_NAME, ex }), ex);
			bNoErrors = false;
		} finally {
			closeConnection(ctx);
		}

		return bNoErrors;
	}

	@Deprecated
	public boolean setExtendedData(String id, String extendedData) {
		return setExtendedData(new BasePasswordChange(PasswordChange.MODIFY_EXTENDED_DATA_CHANGE, id, extendedData));
	}

}
