/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.tam;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;
import com.ibm.di.server.Trace; // import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Log;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.tivoli.pd.jadmin.PDAdmin;
import com.tivoli.pd.jadmin.PDDomain;
import com.tivoli.pd.jadmin.PDPolicy;
import com.tivoli.pd.jadmin.PDSSOCred;
import com.tivoli.pd.jutil.PDContext;
import com.tivoli.pd.jutil.PDException;
import com.tivoli.pd.jutil.PDMessage;
import com.tivoli.pd.jutil.PDMessages;
import com.tivoli.pd.nls.pdbjamsg;

/**
 * <p>
 * The TAM Connector.
 * </p>
 * <p>
 * The connector enables external applications, using TDI, to access IBM Tivoli
 * Access Manager (TAM) It supports the following TDI ConnectorModes:
 * <b>Iterator</b> <b>AddOnly</b> <b>Update</b> <b>Delete</b> <b>Lookup</b>
 * The connector supports design time schema query via {@link #querySchema}.
 * </p>
 * <p>
 * The configuration parameters of the connector are described below. <b>TAM ID</b><br>
 * TAM Administrative ID. <br>
 * <br>
 * <b>TAM Password</b><br>
 * Password for TAM Administrative logon ID. <br>
 * <br>
 * <b>Domain</b><br>
 * The TAM Domain. <br>
 * <br>
 * <b>TAM Program Name</b><br>
 * TAM Program Name specified in the SvrSslCfg configuration utility. <br>
 * <br>
 * <b>�TAM Configuration File</b><br>
 * TAM Configuration File created by the SvrSslCfg configuration utility. <br>
 * <br>
 * <b>Entry Type</b><br>
 * Specifies the object type to work with. The supported types are: <b>User</b>
 * <b>Group</b> <b>Policy</b> <b>SSO Credential</b> <b>SSO Resource</b>
 * <b>SSO Resource Group</b> <br>
 * <br>
 * <b>Import Users or Groups from Registry</b><br>
 * If checked, Users or Groups will be imported from the registry. <br>
 * <br>
 * <b>Import Users or Groups from Registry</b><br>
 * If checked, Users or Groups/Domains will be imported from the registry. <br>
 * <br>
 * </p>
 */
public class TAMConnector extends Connector implements ConnectorInterface {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String COMPONENT_NAME = "TAMConnector";

	private static final String VERSION_INFO = "2.0-di7.1.1 %I%, 20%E%";

	//
	// Made public for use within TDI AL Hooks.
	//
	public static final String PARAM_PROG_NAME = "ProgName";

	public static final String PARAM_DOMAIN = "Domain";

	public static final String PARAM_USER_NAME = "UserName";

	public static final String PARAM_PASSWORD = "Password";

	private static final String PARAM_VALUE_ENTRY_USER = "User";

	private static final String PARAM_VALUE_ENTRY_GROUP = "Group";

	private static final String PARAM_VALUE_ENTRY_POLICY = "Policy";

	private static final String PARAM_VALUE_ENTRY_SSOCRED = "SSOCred";

	private static final String PARAM_VALUE_ENTRY_SSORESOURCE = "SSOResource";

	private static final String PARAM_VALUE_ENTRY_SSORESOURCE_GROUP = "SSOResourceGroup";

	private static final String PARAM_VALUE_ENTRY_DOMAIN = "Domain";

	public static final String PARAM_ENTRY_TYPE = "EntryType";

	public static final String PARAM_FILTER = "ListFilter";

	public static final String PARAM_CONFIG_URL = "ConfigURL";

	public static final String PARAM_IMPORT_CREATE = "ImportCreate";

	public static final String PARAM_DELETE_REG = "DeleteReg";

	public static final String DEFAULT_DOMAIN = "Default";

	private static final int ENTRY_USER = 0;

	private static final int ENTRY_GROUP = 1;

	private static final int ENTRY_POLICY = 2;

	private static final int ENTRY_DOMAIN = 3;

	private static final int ENTRY_SSOCRED = 4;

	private static final int ENTRY_SSORESOURCE = 5;

	private static final int ENTRY_SSORESOURCE_GROUP = 6;

	private String mUserName;

	private String mPassword;

	private String mProgName;

	private String mDomain;

	private String mEntryTypeName;

	private int mEntryType;

	private String mListFilter;

	private PDMessages mMsgs;

	private String mConfigURLStr;

	private URL mConfigURL;

	private PDContext mPDContext;

	private Iterator mUsersIterator;

	private Iterator mGroupsIterator;

	private Iterator mDomainsIterator;

	private Iterator mSSOResourcesIterator;

	private Iterator mSSOResourceGroupsIterator;

	private Iterator mPoliciesIterator;

	private Iterator mSSOCredsIterator;

	private Iterator mUserSSOCredsIterator;

	private boolean mImportReg;

	private boolean mDeleteReg;

	private boolean mConnected;

	private Log mLogProxy = null;

	/**
	 * Default Constructor
	 */
	public TAMConnector() {
		Trace.entrymin(this, "TAMConnector constructor");
		mUserName = null;
		mPassword = null;
		mProgName = null;
		mDomain = null;
		mEntryTypeName = null;
		mListFilter = null;
		mEntryType = -1;
		mMsgs = new PDMessages();
		mConfigURLStr = null;
		mConfigURL = null;
		mPDContext = null;
		mUsersIterator = null;
		mGroupsIterator = null;
		mPoliciesIterator = null;
		mDomainsIterator = null;
		mSSOCredsIterator = null;
		mUserSSOCredsIterator = null;
		mSSOResourcesIterator = null;
		mSSOResourceGroupsIterator = null;
		mImportReg = false;
		mDeleteReg = false;
		mConnected = false;
		setName(COMPONENT_NAME);
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.ADDONLY_MODE, ConnectorConfig.UPDATE_MODE,
				ConnectorConfig.LOOKUP_MODE, ConnectorConfig.DELETE_MODE });
		Trace.exitmin(this, "TAMConnector constructor");
	}

	/**
	 * This method is called once to initialize all the required internal
	 * members.
	 * 
	 * @param arg0
	 *            TDI config object. Not used.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	public void initialize(Object arg0) throws TAMConnectorException {
		Trace.entrymin(this, "TAMConnector.initialize");
		if (null == mLogProxy) {
			mLogProxy = getLog();
			if (null == mLogProxy) {
				mLogProxy = new Log(TMSMessageGetter.MESSAGE_PROPERTIES_NAME);
				setLog(mLogProxy);
				// throw new TAMConnectorException("The Connectors getLog()
				// method returned null");
			}
		}
		mUserName = getParam(PARAM_USER_NAME);
		if (mUserName != null) {
			mUserName = mUserName.trim();
			if (mUserName.length() == 0)
				mUserName = null;
			if (mUserName != null) {
				mPassword = getParam(PARAM_PASSWORD);
				if (mPassword != null) {
					mPassword = mPassword.trim();
				} else {
					String msg = TMSMessageGetter.getMessage(
							TMSMsgId.INVALID_CONFIG_PARAM, PARAM_PASSWORD);
					mLogProxy.logerror(msg);
					throw new TAMConnectorException(msg);
				}
			}
		} else {
			String msg = TMSMessageGetter.getMessage(
					TMSMsgId.INVALID_CONFIG_PARAM, PARAM_USER_NAME);
			mLogProxy.logerror(msg);
			throw new TAMConnectorException(msg);
		}
		mDomain = getParam(PARAM_DOMAIN);
		if (mDomain != null) {
			mDomain = mDomain.trim();
			if (mDomain.length() == 0)
				mDomain = DEFAULT_DOMAIN;
		}
		mProgName = getParam(PARAM_PROG_NAME);
		if (mProgName != null) {
			mProgName = mProgName.trim();
			if (mProgName.length() == 0) {
				String msg = TMSMessageGetter.getMessage(
						TMSMsgId.INVALID_CONFIG_PARAM, PARAM_PROG_NAME);
				mLogProxy.logerror(msg);
				throw new TAMConnectorException(msg);
			}
		}
		mConfigURLStr = getParam(PARAM_CONFIG_URL);
		if (mConfigURLStr != null) {
			mConfigURLStr = mConfigURLStr.trim();
			if (mConfigURLStr.length() == 0) {
				String msg = TMSMessageGetter.getMessage(
						TMSMsgId.INVALID_CONFIG_PARAM, PARAM_CONFIG_URL);
				mLogProxy.logerror(msg);
				throw new TAMConnectorException(msg);
			}
			try {
				File configFile = new File(mConfigURLStr);
				if (!configFile.exists()) {
					String msg = TMSMessageGetter.getMessage(
							TMSMsgId.INVALID_CONFIGFILE, configFile.toString());
					mLogProxy.logerror(msg);
					throw new TAMConnectorException(msg);
				}
				mConfigURL = configFile.toURL();
			} catch (MalformedURLException e) {
				mLogProxy.logerror(e.getMessage());
				throw new TAMConnectorException(e.getMessage());
			}
		}
		mEntryTypeName = getParam(PARAM_ENTRY_TYPE);
		if (mEntryTypeName != null) {
			mEntryTypeName = mEntryTypeName.trim();
			if (!mEntryTypeName.equalsIgnoreCase(PARAM_VALUE_ENTRY_USER)
					&& !mEntryTypeName
							.equalsIgnoreCase(PARAM_VALUE_ENTRY_GROUP)
					&& !mEntryTypeName
							.equalsIgnoreCase(PARAM_VALUE_ENTRY_POLICY)
					&& !mEntryTypeName
							.equalsIgnoreCase(PARAM_VALUE_ENTRY_DOMAIN)
					&& !mEntryTypeName
							.equalsIgnoreCase(PARAM_VALUE_ENTRY_SSOCRED)
					&& !mEntryTypeName
							.equalsIgnoreCase(PARAM_VALUE_ENTRY_SSORESOURCE)
					&& !mEntryTypeName
							.equalsIgnoreCase((PARAM_VALUE_ENTRY_SSORESOURCE_GROUP))) {
				String msg = TMSMessageGetter.getMessage(
						TMSMsgId.INVALID_ENTRY_TYPE, mEntryTypeName);
				mLogProxy.logerror(msg);
				throw new TAMConnectorException(msg);
			}
			if (mEntryTypeName.equalsIgnoreCase(PARAM_VALUE_ENTRY_USER))
				mEntryType = ENTRY_USER;
			else if (mEntryTypeName.equalsIgnoreCase(PARAM_VALUE_ENTRY_GROUP))
				mEntryType = ENTRY_GROUP;
			else if (mEntryTypeName.equalsIgnoreCase(PARAM_VALUE_ENTRY_POLICY))
				mEntryType = ENTRY_POLICY;
			else if (mEntryTypeName.equalsIgnoreCase(PARAM_VALUE_ENTRY_SSOCRED))
				mEntryType = ENTRY_SSOCRED;
			else if (mEntryTypeName.equalsIgnoreCase(PARAM_VALUE_ENTRY_DOMAIN))
				mEntryType = ENTRY_DOMAIN;
			else if (mEntryTypeName
					.equalsIgnoreCase(PARAM_VALUE_ENTRY_SSORESOURCE))
				mEntryType = ENTRY_SSORESOURCE;
			else if (mEntryTypeName
					.equalsIgnoreCase(PARAM_VALUE_ENTRY_SSORESOURCE_GROUP))
				mEntryType = ENTRY_SSORESOURCE_GROUP;
			else {
				String msg = TMSMessageGetter.getMessage(
						TMSMsgId.INVALID_ENTRY_TYPE, mEntryTypeName);
				mLogProxy.logerror(msg);
				throw new TAMConnectorException(msg);
			}
		} else {
			String msg = TMSMessageGetter
					.getMessage(TMSMsgId.MISSING_ENTRY_TYPE);
			mLogProxy.logerror(msg);
			throw new TAMConnectorException(msg);
		}
		try {
			if (!mConnected) {
				String msg = TMSMessageGetter.getMessage(
						TMSMsgId.TAM_INITIALIZE, mProgName);
				mLogProxy.loginfo(msg);
				PDAdmin.initialize(mProgName, mMsgs);
				processMsgs(mMsgs);
				mLogProxy.loginfo(TMSMessageGetter.getMessage(
						TMSMsgId.CREATING_CONTEXT, mDomain + "->" + mUserName));
				mPDContext = new PDContext(mUserName, mPassword.toCharArray(),
						mDomain, mConfigURL);
				mConnected = true;
			}
		} catch (PDException e) {
			mLogProxy.logerror(TMSMessageGetter
					.getMessage(TMSMsgId.COULD_NOT_LOG_ON));
			handlePDException(e);
		}
		String s1 = (String) getParam(PARAM_IMPORT_CREATE);
		if (s1 != null && s1.equalsIgnoreCase("true"))
			mImportReg = true;
		else
			mImportReg = false;
		s1 = (String) getParam(PARAM_DELETE_REG);
		if (s1 != null && s1.equalsIgnoreCase("true"))
			mDeleteReg = true;
		else
			mDeleteReg = false;
		if (mEntryType == ENTRY_GROUP || mEntryType == ENTRY_USER) {
			mListFilter = getParam(PARAM_FILTER);
		}
		Trace.exitmin(this, "TAMConnector.initialize");
	}

	/**
	 * Available for use by TDI Assembly Line.
	 * 
	 * @param importReg
	 *            true for IMPORT, false for CREATE
	 */
	public void setImportReg(boolean importReg) {
		mImportReg = importReg;
	}

	/**
	 * Shuts down the Connection to the TAM Server using PDAdmin.shutdown().
	 * 
	 * @throws Exception
	 *             When an unrecoverable error occurs.
	 */
	public void terminate() throws Exception {
		Trace.entrymin(this, "TAMConnector.terminate");
		mLogProxy.loginfo(TMSMessageGetter.getMessage(TMSMsgId.TAM_SHUTDOWN));
		
		mConnected = false;

		/*
		 * Closing the context is not supported by the TAM 5.1 JRTE but is by
		 * the TAM 6 JRTE. This code would have to be removed to support TAM 5.1
		 */
		if (mPDContext != null) {
			try {
				mPDContext.close();
			} catch (PDException e) {
				handlePDException(e);
				Object[] args = new Object[] { "Context" };
				throw new Exception("TAM Context: "
						+ TMSMessageGetter.getMessage(
								TMSMsgId.COULD_NOT_SHUT_DOWN, args));
			}
		}
		/*
		 * Shut down PDAdmin. This call is required for proper cleanup of the
		 * PDAdmin facility
		 */
		try {
			PDAdmin.shutdown(mMsgs);
			processMsgs(mMsgs);
		} catch (PDException e) {
			Object[] args = new Object[] { "Admin" };
			mLogProxy.logerror(TMSMessageGetter.getMessage(
					TMSMsgId.COULD_NOT_SHUT_DOWN, args));
			handlePDException(e);
		}
		Trace.exitmin(this, "TAMConnector.terminate");
	}

	/**
	 * Prepares a list of the required Entry Type to iterate over.
	 * 
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	public void selectEntries() throws TAMConnectorException {
		Trace.entrymin(this, "TAMConnector.selectEntries");
		try {
			switch (mEntryType) {
			case ENTRY_USER:
				mUsersIterator = User.list(mListFilter, mPDContext).iterator();
				break;
			case ENTRY_GROUP:
				mGroupsIterator = Group.list(mListFilter, mPDContext)
						.iterator();
				break;
			case ENTRY_POLICY:
				mPoliciesIterator = Policy.list(mPDContext).iterator();
				break;
			case ENTRY_SSOCRED:
				mSSOCredsIterator = SSOCredentials.list(mPDContext).iterator();
				mUserSSOCredsIterator = null;
				break;
			case ENTRY_DOMAIN:
				mDomainsIterator = Domain.list(mPDContext).iterator();
				break;
			case ENTRY_SSORESOURCE:
				mSSOResourcesIterator = SSOResource.list(mPDContext).iterator();
				break;
			case ENTRY_SSORESOURCE_GROUP:
				mSSOResourceGroupsIterator = SSOResourceGroup.list(mPDContext)
						.iterator();
				break;
			default: {
				String msg = TMSMessageGetter.getMessage(
						TMSMsgId.INVALID_ENTRY_TYPE, mEntryTypeName);
				mLogProxy.logerror(msg);
				throw new TAMConnectorException(msg);
			}
			}
		} catch (PDException e) {
			mLogProxy.logerror(TMSMessageGetter.getMessage(
					TMSMsgId.SELECT_ERROR, mEntryTypeName));
			handlePDException(e);
		}
		Trace.exitmin(this, "TAMConnector.selectEntries");
	}

	/**
	 * Using the list of the configured Entry Type, returns the next available
	 * entry on the list. When either the end of the list has been passed, or
	 * the list is empty, returns NULL.
	 * 
	 * @return The next available entry, or NULL.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	public Entry getNextEntry() throws TAMConnectorException {
		Trace.entrymin(this, "TAMConnector.getNextEntry");
		try {
			switch (mEntryType) {
			case ENTRY_USER:
				return getNextUserEntry();
			case ENTRY_GROUP:
				return getNextGroupEntry();
			case ENTRY_POLICY:
				return getNextPolicyEntry();
			case ENTRY_SSOCRED:
				return getNextSSOCredEntry();
			case ENTRY_SSORESOURCE:
				return getNextSSOResourceEntry();
			case ENTRY_SSORESOURCE_GROUP:
				return getNextSSOResourceGroupEntry();
			case ENTRY_DOMAIN:
				return getNextDomainEntry();
			default: {
				String msg = TMSMessageGetter.getMessage(
						TMSMsgId.INVALID_ENTRY_TYPE, mEntryTypeName);
				mLogProxy.logerror(msg);
				throw new TAMConnectorException(msg);
			}
			}
		} catch (PDException e) {
			mLogProxy.logerror(TMSMessageGetter.getMessage(
					TMSMsgId.SELECT_NEXT_ERROR, mEntryTypeName));
			handlePDException(e);
			return null;
		}
	}

	/**
	 * Using the User Entry Type list, returns the next available entry on the
	 * list. When either the end of the list has been passed, or the list is
	 * empty, returns NULL.
	 * 
	 * @return The next available entry, or NULL.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	private Entry getNextUserEntry() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "TAMConnector.getNextUserEntry");
		Entry entry = null;
		if (mUsersIterator.hasNext()) {
			String s = (String) mUsersIterator.next();
			Object[] args = new Object[] { mEntryTypeName, s };
			mLogProxy.logdebug(TMSMessageGetter.getMessage(TMSMsgId.DATA_GET,
					args));
			User userinfo = new User(s, mPDContext, mLogProxy);
			entry = userinfo.getAttributes();
		}
		Trace.exitmin(this, "TAMConnector.getNextUserEntry");
		return entry;
	}

	/**
	 * Using the Group Entry Type list, returns the next available entry on the
	 * list. When either the end of the list has been passed, or the list is
	 * empty, returns NULL.
	 * 
	 * @return The next available entry, or NULL.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	private Entry getNextGroupEntry() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "TAMConnector.getNextGroupEntry");
		Entry entry = null;
		if (mGroupsIterator.hasNext()) {
			String s = (String) mGroupsIterator.next();
			Object[] args = new Object[] { mEntryTypeName, s };
			mLogProxy.logdebug(TMSMessageGetter.getMessage(TMSMsgId.DATA_GET,
					args));
			Group groupInfo = new Group(s, mPDContext, mLogProxy);
			entry = groupInfo.getAttributes();
		}
		Trace.exitmin(this, "TAMConnector.getNextGroupEntry");
		return entry;
	}

	/**
	 * Using the Domain Entry Type list, returns the next available entry on the
	 * list. When either the end of the list has been passed, or the list is
	 * empty, returns NULL.
	 * 
	 * @return The next available entry, or NULL.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	private Entry getNextDomainEntry() throws TAMConnectorException,
			PDException {
		Trace.entrymin(this, "TAMConnector.getNextDomainEntry");
		Entry entry = null;
		if (mDomainsIterator.hasNext()) {
			String s = (String) mDomainsIterator.next();
			Object[] args = new Object[] { mEntryTypeName, s };
			mLogProxy.logdebug(TMSMessageGetter.getMessage(TMSMsgId.DATA_GET,
					args));
			Domain domain = new Domain(s, mPDContext, mUserName, mPassword,
					mLogProxy);
			entry = domain.getAttributes();
		}
		Trace.exitmin(this, "TAMConnector.getNextDomainEntry");
		return entry;
	}

	/**
	 * Using the Policy Entry Type list, returns the next available entry on the
	 * list. When either the end of the list has been passed, or the list is
	 * empty, returns NULL.
	 * 
	 * @return The next available entry, or NULL.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	private Entry getNextPolicyEntry() throws TAMConnectorException,
			PDException {
		Trace.entrymin(this, "TAMConnector.getNextPolicyEntry");
		Entry entry = null;
		if (mPoliciesIterator.hasNext()) {
			String s = (String) mPoliciesIterator.next();
			Object[] args = new Object[] { mEntryTypeName, s };
			mLogProxy.logdebug(TMSMessageGetter.getMessage(TMSMsgId.DATA_GET,
					args));
			Policy policy = new Policy(s, mPDContext, mLogProxy);
			entry = policy.getAttributes();
		}
		Trace.exitmin(this, "TAMConnector.getNextPolicyEntry");
		return entry;
	}

	/**
	 * Using the SSO Credential Entry Type list, returns the next available
	 * entry on the list. When either the end of the list has been passed, or
	 * the list is empty, returns NULL.
	 * 
	 * @return The next available entry, or NULL.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	private Entry getNextSSOCredEntry() throws TAMConnectorException,
			PDException {
		Trace.entrymin(this, "TAMConnector.getNextSSOCredEntry");
		Entry entry = null;
		if (mSSOCredsIterator.hasNext() && mUserSSOCredsIterator == null) {
			String s = (String) mSSOCredsIterator.next();
			// get the list of credentials for this user
			List creds = PDSSOCred.listAndShowSSOCreds(mPDContext, s, mMsgs);
			mUserSSOCredsIterator = creds.iterator();
			if (mUserSSOCredsIterator.hasNext()) {
				PDSSOCred.CredInfo cred = (PDSSOCred.CredInfo) mUserSSOCredsIterator
						.next();
				Object[] args = new Object[] { mEntryTypeName, s };
				mLogProxy.logdebug(TMSMessageGetter.getMessage(
						TMSMsgId.DATA_GET, args));
				SSOCredentials ssocredinfo = new SSOCredentials(cred,
						mPDContext, mLogProxy);
				entry = ssocredinfo.getAttributes();
			}
		} else if (mSSOCredsIterator.hasNext() && mUserSSOCredsIterator != null) {
			if (mUserSSOCredsIterator.hasNext()) {
				PDSSOCred.CredInfo cred = (PDSSOCred.CredInfo) mUserSSOCredsIterator
						.next();
				SSOCredentials ssocredinfo = new SSOCredentials(cred,
						mPDContext, mLogProxy);
				entry = ssocredinfo.getAttributes();
			} else {
				String s = (String) mSSOCredsIterator.next();
				// get the list of credentials for this user
				List creds = PDSSOCred
						.listAndShowSSOCreds(mPDContext, s, mMsgs);
				mUserSSOCredsIterator = creds.iterator();
				if (mUserSSOCredsIterator.hasNext()) {
					PDSSOCred.CredInfo cred = (PDSSOCred.CredInfo) mUserSSOCredsIterator
							.next();
					Object[] args = new Object[] { mEntryTypeName, s };
					mLogProxy.logdebug(TMSMessageGetter.getMessage(
							TMSMsgId.DATA_GET, args));
					SSOCredentials ssocredinfo = new SSOCredentials(cred,
							mPDContext, mLogProxy);
					entry = ssocredinfo.getAttributes();
				}
			}
		} else if ((!mSSOCredsIterator.hasNext())
				&& (mUserSSOCredsIterator != null)
				&& (mUserSSOCredsIterator.hasNext())) {
			PDSSOCred.CredInfo cred = (PDSSOCred.CredInfo) mUserSSOCredsIterator
					.next();
			SSOCredentials ssocredinfo = new SSOCredentials(cred,
					mPDContext, mLogProxy);
			entry = ssocredinfo.getAttributes();
		}
		Trace.exitmin(this, "TAMConnector.getNextSSOCredEntry");
		return entry;
	}

	/**
	 * Using the SSO Resource Entry Type list, returns the next available entry
	 * on the list. When either the end of the list has been passed, or the list
	 * is empty, returns NULL.
	 * 
	 * @return The next available entry, or NULL.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	private Entry getNextSSOResourceEntry() throws TAMConnectorException,
			PDException {
		Trace.entrymin(this, "TAMConnector.getNextSSOResourceEntry");
		Entry entry = null;
		if (mSSOResourcesIterator.hasNext()) {
			String s = (String) mSSOResourcesIterator.next();
			Object[] args = new Object[] { mEntryTypeName, s };
			mLogProxy.logdebug(TMSMessageGetter.getMessage(TMSMsgId.DATA_GET,
					args));
			SSOResource ssoResource = new SSOResource(s, mPDContext, mLogProxy);
			entry = ssoResource.getAttributes();
		}
		Trace.exitmin(this, "TAMConnector.getNextSSOResourceEntry");
		return entry;
	}

	/**
	 * Using the SSO Resource Group Entry Type list, returns the next available
	 * entry on the list. When either the end of the list has been passed, or
	 * the list is empty, returns NULL.
	 * 
	 * @return The next available entry, or NULL.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	private Entry getNextSSOResourceGroupEntry() throws TAMConnectorException,
			PDException {
		Trace.entrymin(this, "TAMConnector.getNextSSOResourceGroupEntry");
		Entry entry = null;
		if (mSSOResourceGroupsIterator.hasNext()) {
			String s = (String) mSSOResourceGroupsIterator.next();
			Object[] args = new Object[] { mEntryTypeName, s };
			mLogProxy.logdebug(TMSMessageGetter.getMessage(TMSMsgId.DATA_GET,
					args));
			SSOResourceGroup ssoResourceGroup = new SSOResourceGroup(s,
					mPDContext, mLogProxy);
			entry = ssoResourceGroup.getAttributes();
		}
		Trace.exitmin(this, "TAMConnector.getNextSSOResourceGroupEntry");
		return entry;
	}

	/**
	 * Using the provided search criteria and based on the configured Entry
	 * Type, attempts to find the object in TAM. If no object can be found the
	 * returns NULL.
	 * 
	 * @param searchcriteria -
	 *            Provides details of the attribute to search with and the
	 *            search operand.
	 * @return An entry representing the TAM object, else NULL.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	public Entry findEntry(SearchCriteria searchcriteria)
			throws TAMConnectorException {
		Trace.entrymin(this, "TAMConnector.findEntry");
		if (searchcriteria == null || searchcriteria.size() == 0) {
			return null;
		}
		clearFindEntries();
		try {
			switch (mEntryType) {
			case ENTRY_USER:
				if (preLookupCheck(User.list(searchcriteria, mPDContext)
						.iterator(), searchcriteria)) {
					User user = new User(searchcriteria, mPDContext, mLogProxy);
					return user.getAttributes();
				}
				return null;
			case ENTRY_GROUP:
				if (preLookupCheck(Group.list(searchcriteria, mPDContext)
						.iterator(), searchcriteria)) {
					Group group = new Group(searchcriteria, mPDContext,
							mLogProxy);
					return group.getAttributes();
				}
				return null;
			case ENTRY_POLICY:
				if (preLookupCheck(Policy.list(searchcriteria, mPDContext)
						.iterator(), searchcriteria)) {
					Policy policy = new Policy(searchcriteria, mPDContext,
							mLogProxy);
					return policy.getAttributes();
				}
				return null;
			case ENTRY_SSOCRED:
			if (SSOCredentials.list(searchcriteria, mPDContext) != null) {
					if (preLookupCheck(SSOCredentials.list(searchcriteria, mPDContext).iterator(), searchcriteria)) {
						SSOCredentials sso = new SSOCredentials(searchcriteria, mPDContext, mLogProxy);
						return sso.getAttributes();
					}
				}
				return null;
			case ENTRY_DOMAIN:
				if (preLookupCheck(Domain.list(mPDContext).iterator(),
						searchcriteria)) {
					Domain domain = new Domain(searchcriteria, mPDContext,
							mUserName, mPassword, mLogProxy);
					return domain.getAttributes();
				}
				return null;
			case ENTRY_SSORESOURCE:
				if (preLookupCheck(SSOResource.list(mPDContext).iterator(),
						searchcriteria)) {
					SSOResource resource = new SSOResource(searchcriteria,
							mPDContext, mLogProxy);
					return resource.getAttributes();
				}
				return null;
			case ENTRY_SSORESOURCE_GROUP:
				if (preLookupCheck(
						SSOResourceGroup.list(mPDContext).iterator(),
						searchcriteria)) {
					SSOResourceGroup resourceGroup = new SSOResourceGroup(
							searchcriteria, mPDContext, mLogProxy);
					return resourceGroup.getAttributes();
				}
				return null;
			default: {
				String msg = TMSMessageGetter.getMessage(
						TMSMsgId.INVALID_ENTRY_TYPE, mEntryTypeName);
				mLogProxy.logerror(msg);
				throw new TAMConnectorException(msg);
			}
			}
		} catch (PDException e) {
			mLogProxy.logerror(TMSMessageGetter.getMessage(TMSMsgId.FIND_ERROR,
					mEntryTypeName));
			handlePDException(e);
		}
		return null;
	}

	/**
	 * Using the provided object Iterator and based on the configured Entry
	 * Type, attempts to find the object in TAM. If no object can be found then
	 * return false.
	 * 
	 * @param listIter -
	 *            object list Iterator
	 * @param searchcriteria -
	 *            Provides details of the attribute to search with and the
	 *            search operand.
	 * @return True if object exists, else false.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 * @throws PDException
	 */
	private boolean preLookupCheck(Iterator listIter,
			SearchCriteria searchcriteria) throws TAMConnectorException,
			PDException {
		String scName = searchcriteria.getFirstCriteriaName();
		String scVal = searchcriteria.getFirstCriteriaValue();
		int scMatch = searchcriteria.getFirstCriteriaMatch();
		switch (mEntryType) {
		case ENTRY_USER:
			if (!scName.equalsIgnoreCase(User.USER_ATTR_LOGIN_ID)
					|| scMatch != SearchCriteria.EXCACT) {
				throw new TAMConnectorException(TMSMessageGetter
						.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
			}
			while (listIter.hasNext()) {
				String s = (String) listIter.next();
				if (scVal.equalsIgnoreCase(s))
					return true;
			}
			return false;
		case ENTRY_GROUP:
			if (!scName.equalsIgnoreCase(Group.GROUP_ATTR_GROUP_ID)
					|| scMatch != SearchCriteria.EXCACT)
				throw new TAMConnectorException(TMSMessageGetter.getMessage(
						TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA,
						scName));
			while (listIter.hasNext()) {
				String s = (String) listIter.next();
				if (scVal.equalsIgnoreCase(s))
					return true;
			}
			return false;
		case ENTRY_POLICY:
			if (!scName.equalsIgnoreCase(Policy.POLICY_ATTR_USER_ID)
					|| scMatch != SearchCriteria.EXCACT)
				throw new TAMConnectorException(TMSMessageGetter
						.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
			while (listIter.hasNext()) {
				String s = (String) listIter.next();
				if (scVal.equalsIgnoreCase(s)
						|| scVal
								.equalsIgnoreCase(PDPolicy.PDPOLICY_GLOBAL_POLICY))
					return true;
			}
			return false;
		case ENTRY_SSOCRED:
			String userName = null;
			String resourceName = null;
			String resourceType = null;
			Vector criteria = searchcriteria.getCriteria();
			for (Iterator iter = criteria.iterator(); iter.hasNext();) {
				SearchCriteria.rscSearch element = (SearchCriteria.rscSearch) iter
						.next();
				debug("Search Criteria Name = " + element.name);
				if (element.name.compareTo(SSOCredentials.SSOCRED_ATTR_USER_ID) == 0) {
					userName = (String) element.value;
				} else if (element.name
						.compareTo(SSOCredentials.SSOCRED_ATTR_RESOURCE_NAME) == 0) {
					resourceName = (String) element.value;
				} else if (element.name
						.compareTo(SSOCredentials.SSOCRED_ATTR_RESOURCE_TYPE) == 0) {
					resourceType = (String) element.value;
				}
				if (element.match != SearchCriteria.EXCACT) {
					throw new TAMConnectorException(TMSMessageGetter
							.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
				}
			}
			if (searchcriteria.getType() != SearchCriteria.SEARCH_AND) {
				throw new TAMConnectorException(TMSMessageGetter
						.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
			}
			ArrayList creds = PDSSOCred.listAndShowSSOCreds(mPDContext,
					userName, mMsgs);
			if (creds.size() < 1) {
				return false;
			}
			listIter = null;
			listIter = creds.iterator();
			while (listIter.hasNext()) {
				PDSSOCred.CredInfo element = (PDSSOCred.CredInfo) listIter
						.next();
				// If resourceName and resourceType are provided then we only want a particular SSO cred for that resource and
				// type.
				if (null != resourceName && resourceName.length() > 0
						&& resourceType != null && resourceType.length() > 0) {
					if (element.getResourceName().equalsIgnoreCase(resourceName) &&
						element.getResourceType().toLowerCase().startsWith(resourceType.toLowerCase())) {
						return true;
					} else {
						if (listIter.hasNext()) {
							continue;
						} else {
							return false;
						}
					}
				}
				return true;
			}
			return false;
		case ENTRY_DOMAIN:
			if (!scName.equalsIgnoreCase(Domain.DOMAIN_ATTR_DOMAIN_NAME)
					|| scMatch != SearchCriteria.EXCACT)
				throw new TAMConnectorException(TMSMessageGetter
						.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
			if (scVal == null || scVal.length() == 0)
				throw new TAMConnectorException(TMSMessageGetter
						.getMessage(TMSMsgId.MISSING_ENTRY_TYPE));
			while (listIter.hasNext()) {
				String s = (String) listIter.next();
				if (scVal.equalsIgnoreCase(s))
					return true;
			}
			return false;
		case ENTRY_SSORESOURCE:
			if (!scName.equalsIgnoreCase(SSOResource.SSORESOURCE_ATTR_NAME)
					|| scMatch != SearchCriteria.EXCACT)
				throw new TAMConnectorException(TMSMessageGetter
						.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
			while (listIter.hasNext()) {
				String s = (String) listIter.next();
				if (scVal.equalsIgnoreCase(s))
					return true;
			}
			return false;
		case ENTRY_SSORESOURCE_GROUP:
			if (!scName
					.equalsIgnoreCase(SSOResourceGroup.SSORESOURCEGROUP_ATTR_GROUP_NAME)
					|| scMatch != SearchCriteria.EXCACT)
				throw new TAMConnectorException(TMSMessageGetter
						.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
			while (listIter.hasNext()) {
				String s = (String) listIter.next();
				if (scVal.equalsIgnoreCase(s))
					return true;
			}
			return false;
		default: {
			String msg = TMSMessageGetter.getMessage(
					TMSMsgId.INVALID_ENTRY_TYPE, mEntryTypeName);
			mLogProxy.logerror(msg);
			return false;
		}
		}
	}

	/**
	 * Using the provided entry and based on the configured Entry Type, attempts
	 * to create the specified object in TAM.
	 * 
	 * @param entry -
	 *            A TDI Entry containing the attributes and values to use when
	 *            creating the required object, of the required Entry Type in
	 *            TAM.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	public void putEntry(Entry entry) throws TAMConnectorException {
		Trace.entrymin(this, "TAMConnector.putEntry");
		if (entry == null || entry.size() == 0) {
			return;
		}
		try {
			switch (mEntryType) {
			case ENTRY_USER:
				User user = new User(entry, mPDContext, mLogProxy);
				user.put(mImportReg);
				break;
			case ENTRY_GROUP:
				Group group = new Group(entry, mPDContext, mLogProxy);
				group.put(mImportReg);
				break;
			case ENTRY_POLICY:
				Policy policy = new Policy(entry, mPDContext, mLogProxy);
				policy.put();
				break;
			case ENTRY_SSOCRED:
				SSOCredentials sso = new SSOCredentials(entry, mPDContext,
						mLogProxy);
				sso.put();
				break;
			case ENTRY_SSORESOURCE:
				SSOResource ssoResource = new SSOResource(entry, mPDContext,
						mLogProxy);
				ssoResource.put();
				break;
			case ENTRY_SSORESOURCE_GROUP:
				SSOResourceGroup resourceGroup = new SSOResourceGroup(entry,
						mPDContext, mLogProxy);
				resourceGroup.put();
				break;
			case ENTRY_DOMAIN:
				Domain domain = new Domain(entry, mPDContext, mUserName,
						mPassword, mLogProxy);
				domain.put();
				break;
			default: {
				String msg = TMSMessageGetter.getMessage(
						TMSMsgId.INVALID_ENTRY_TYPE, mEntryTypeName);
				mLogProxy.logerror(msg);
				throw new TAMConnectorException(msg);
			}
			}
		} catch (PDException pde) {
			mLogProxy.logerror(TMSMessageGetter.getMessage(
					TMSMsgId.CREATE_ERROR, mEntryTypeName));
			handlePDException(pde);
		}
		Trace.exitmin(this, "TAMConnector.putEntry");
	}

	/**
	 * The old Entry is now redundant so shoehorn the original modEntry into the
	 * new code which just uses searchcriteria with no loss of functionality.
	 */
	public void modEntry(Entry changes, SearchCriteria searchcriteria, Entry old)
			throws TAMConnectorException {
		Trace.entrymin(this, "TAMConnector.modEntry");
		modEntry(changes, searchcriteria);
		Trace.exitmin(this, "TAMConnector.modEntry");
	}

	/**
	 * Using the provided changes and based on the configured Entry Type,
	 * attempts to modify the specified object in TAM. It does this with only
	 * one instance of the underlying PDObject rather than relying on the
	 * Locate() to already have provided an initial instance of the object.
	 * 
	 * @param changes -
	 *            A TDI Entry containing the attributes and values to use when
	 *            modifying the required object, of the required Entry Type in
	 *            TAM.
	 * @param searchcriteria -
	 * 
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	public void modEntry(Entry changes, SearchCriteria searchcriteria)
			throws TAMConnectorException {
		Trace.entrymin(this, "TAMConnector.modEntry#2");
		if (searchcriteria == null || searchcriteria.size() == 0) {
			return;
		}
		try {
			switch (mEntryType) {
			case ENTRY_USER:
				User oldUser = new User(mPDContext, searchcriteria, mLogProxy);
				oldUser.modify(changes);
				break;
			case ENTRY_GROUP:
				Group oldGroup = new Group(mPDContext, searchcriteria,
						mLogProxy);
				oldGroup.modify(changes);
				break;
			case ENTRY_POLICY:
				Policy policy = new Policy(mPDContext, searchcriteria,
						mLogProxy);
				policy.set(changes);
				policy.modify_postset();
				break;
			case ENTRY_SSOCRED:
				// use original search
				SSOCredentials sso = new SSOCredentials(searchcriteria,
						mPDContext, mLogProxy);
				sso.set(changes);
				sso.modify();
				break;
			case ENTRY_SSORESOURCE_GROUP:
				SSOResourceGroup resourceGroup = new SSOResourceGroup(
						mPDContext, searchcriteria, mLogProxy);
				resourceGroup.set(changes);
				resourceGroup.modify();
				break;
			case ENTRY_DOMAIN:
				Domain domain = new Domain(mPDContext, searchcriteria,
						mLogProxy);
				domain.set(changes);
				domain.modify();
				break;
			default: {
				String msg = TMSMessageGetter
						.getMessage(TMSMsgId.INVALID_ENTRY_TYPE, mEntryTypeName);
				mLogProxy.logerror(msg);
				throw new TAMConnectorException(msg);
			}
			}
		} catch (PDException pde) {
			mLogProxy.logerror(TMSMessageGetter.getMessage(
					TMSMsgId.MODIFY_ERROR, mEntryTypeName));
			handlePDException(pde);
		}
		Trace.exitmin(this, "TAMConnector.modEntry#2");
	}

	/**
	 * The old Entry is now redundant so shoehorn the original deleteEntry into
	 * the new code which just uses searchcriteria with no loss of
	 * functionality.
	 */
	public void deleteEntry(Entry entry, SearchCriteria searchcriteria)
			throws TAMConnectorException {
		Trace.entrymin(this, "TAMConnector.deleteEntry");
		deleteEntry(searchcriteria);
		Trace.exitmin(this, "TAMConnector.deleteEntry");
	}

	/**
	 * Deletes the entry determined by the SearchCriteria if it exists
	 * 
	 * @param searchcriteria -
	 *            Used to provide the unique key to access the PDObject
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	public void deleteEntry(SearchCriteria searchcriteria)
			throws TAMConnectorException {
		Trace.entrymin(this, "TAMConnector.deleteEntry#2");
		try {
			switch (mEntryType) {
			case ENTRY_USER:
				User user = new User(mPDContext, searchcriteria, mLogProxy);
				user.delete(mDeleteReg);
				break;
			case ENTRY_GROUP:
				Group group = new Group(mPDContext, searchcriteria, mLogProxy);
				group.delete(mDeleteReg);
				break;
			case ENTRY_POLICY:
				Policy policy = new Policy(mPDContext, searchcriteria,
						mLogProxy);
				policy.delete();
				break;
			case ENTRY_SSOCRED:
				SSOCredentials sso = new SSOCredentials(mPDContext,
						searchcriteria, mLogProxy);
				sso.delete();
				break;
			case ENTRY_SSORESOURCE:
				SSOResource ssoResource = new SSOResource(mPDContext,
						searchcriteria, mLogProxy);
				ssoResource.delete();
				break;
			case ENTRY_SSORESOURCE_GROUP:
				SSOResourceGroup resourceGroup = new SSOResourceGroup(
						mPDContext, searchcriteria, mLogProxy);
				resourceGroup.delete();
				break;
			case ENTRY_DOMAIN:
				Domain domain = new Domain(mPDContext, searchcriteria,
						mLogProxy);
				domain.delete(mDeleteReg);
				break;
			default: {
				String msg = TMSMessageGetter.getMessage(
						TMSMsgId.INVALID_ENTRY_TYPE, mEntryTypeName);
				mLogProxy.logerror(msg);
				throw new TAMConnectorException(msg);
			}
			}
		} catch (PDException e) {
			mLogProxy.logerror(TMSMessageGetter.getMessage(
					TMSMsgId.DELETE_ERROR, mEntryTypeName));
			handlePDException(e);
		}
		Trace.exitmin(this, "TAMConnector.deleteEntry");
	}

	/**
	 * Based on the configured Entry Type returns a list of TDI entries that
	 * define the schema for attribute mapping.
	 * 
	 * @param obj -
	 *            Not used.
	 * @return Vector of TDI Entries that represent the attribute mapping
	 *         schema.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	public Object querySchema(Object obj) throws TAMConnectorException {
		Trace.entrymin(this, "TAMConnector.querySchema");
		Vector ret;
		/*
		 * * if not initialised, user will have to look in user guide * as the
		 * EntryType would not have been set
		 */
		if (mEntryType == -1) {
			ret = new Vector();
			CommonBase.addSchemaEntry(ret, TMSMessageGetter
					.getMessage(TMSMsgId.SCHEMA_INFO), CommonBase.QSS_STRING,
					Integer.valueOf(256));
			return ret;
		}
		switch (mEntryType) {
		case ENTRY_USER:
			ret = User.schema();
			break;
		case ENTRY_GROUP:
			ret = Group.schema();
			break;
		case ENTRY_POLICY:
			ret = Policy.schema();
			break;
		case ENTRY_SSOCRED:
			ret = SSOCredentials.schema();
			break;
		case ENTRY_SSORESOURCE:
			ret = SSOResource.schema();
			break;
		case ENTRY_SSORESOURCE_GROUP:
			ret = SSOResourceGroup.schema();
			break;
		case ENTRY_DOMAIN:
			ret = Domain.schema();
			break;
		default: {
			String msg = TMSMessageGetter.getMessage(
					TMSMsgId.INVALID_ENTRY_TYPE, mEntryTypeName);
			mLogProxy.logerror(msg);
			throw new TAMConnectorException(msg);
		}
		}
		Trace.exitmin(this, "TAMConnector.querySchema");
		return ret;
	}

	/**
	 * Returns current version of the Connector.
	 * 
	 * @return String representation of Connector's current version.
	 */
	public String getVersion() {
		return VERSION_INFO;
	}

	/**
	 * Used to query TAM for the set of configured Domains.
	 * 
	 * @return Vector of PDDomain objects.
	 * @throws TAMConnectorException
	 *             When an unrecoverable error occurs.
	 */
	public Vector queryTAMDomains() throws TAMConnectorException {
		try {
			Vector domains = new Vector(PDDomain.listDomains(mPDContext, mMsgs));
			domains.add(PDDomain.getMgmtDomainName());
			processMsgs(mMsgs);
			return domains;
		} catch (PDException e) {
			mLogProxy.logerror(TMSMessageGetter
					.getMessage(TMSMsgId.COULD_NOT_QUERY_DOMAINS));
			handlePDException(e);
			return null;
		}
	}

	/**
	 * Used to log messages returned from a TAM operation.
	 * 
	 * @param msgs -
	 *            TAM Messages to be logged.
	 */
	private void processMsgs(PDMessages msgs) {
		Trace.entrymin(this, "TAMConnector.processMsgs");
		if (msgs != null) {
			Iterator iter = msgs.iterator();
			while (iter.hasNext()) {
				mLogProxy.logwarn(((PDMessage) iter.next()).getMsgText());
			}
			msgs.clear();
		}
		Trace.exitmin(this, "TAMConnector.processMsgs");
	}

	/**
	 * Used to handle exceptions returned from a TAM operation.
	 * 
	 * @param e -
	 *            TAM Exception to be handled.
	 * @throws TAMConnectorException
	 *             that represents the TAM API Exception.
	 */
	private void handlePDException(Exception e) throws TAMConnectorException {
		Trace.entrymin(this, "TAMConnector.handlePDException");
		PDException pd = (PDException) e;
		PDMessages msgs = pd.getMessages();
		if (msgs != null) {
			Iterator pdi = msgs.iterator();
			PDMessage msg = null;
			int msgCode = 0;
			/*---------------------------------------------------------------
			 * The Tivoli Access Manager Java Admin API will throw PDExceptions
			 * that have a single message code in the member PDMessages.
			 * However, the PDException class is designed so that multiple codes
			 * can be returned.  This way, a caller of the Java Admin API can
			 * "stack" another error code in the PDException, if desired,
			 * and rethrow the exception to its caller.
			 *---------------------------------------------------------------
			 */
			while (pdi.hasNext()) {
				msg = (PDMessage) pdi.next();
				msgCode = msg.getMsgCode();
				/*---------------------------------------------------------------
				 * Here are examples of generic Tivoli Access Manager message codes.
				 * They are available by importing one or more of the
				 * com.tivoli.pd.nls.pd*msg classes. These message codes are
				 * documented in the Error Message Reference.
				 *---------------------------------------------------------------
				 */
				String error = "";
				switch (msgCode) {
				case pdbjamsg.bja_invalid_msgs:
					error = TMSMessageGetter.getMessage(
							TMSMsgId.PD_INVALID_MSG, msg.getMsgText());
					mLogProxy.logerror(error);
					break;
				case pdbjamsg.bja_invalid_ctxt:
					error = TMSMessageGetter.getMessage(
							TMSMsgId.PD_INVALID_CONTEXT, msg.getMsgText());
					mLogProxy.logerror(error);
					break;
				case pdbjamsg.bja_cannot_contact_server:
					error = TMSMessageGetter.getMessage(
							TMSMsgId.PD_SERVER_ERROR, msg.getMsgText());
					mLogProxy.logerror(error);
					break;
				default:
					error = TMSMessageGetter.getMessage(
							TMSMsgId.PD_UNKNOWN_MSG_TYPE, msg.getMsgText());
					mLogProxy.logerror(error);
					break;
				}
				throw new TAMConnectorException(error);
			}
		} else {
			/*---------------------------------------------------------------
			 * A PDException with no messages typically means that a Java
			 * exception or error was thrown and wrappered in a PDException.
			 * To get the underlying exception or error, use the PDException
			 * getCause() method.
			 *---------------------------------------------------------------
			 */
			Throwable t = pd.getCause();
			if (t != null) {
				mLogProxy.logerror(TMSMessageGetter
						.getMessage(TMSMsgId.JAVA_ERROR));
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				t.printStackTrace(pw);
				getLog().loginfo(sw.toString());
			}
		}
		Trace.exitmin(this, "TAMConnector.handlePDException");
	}
}
