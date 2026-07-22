/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.tam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;
import com.tivoli.pd.jadmin.PDGroup;
import com.tivoli.pd.jadmin.PDPolicy;
import com.tivoli.pd.jadmin.PDUser;
import com.tivoli.pd.jutil.PDContext;
import com.tivoli.pd.jutil.PDException;
import com.tivoli.pd.jutil.PDMessages;
import com.tivoli.pd.jutil.PDRgyUserName;

/**
 * User class contains the functionality to find, add, modify and delete Users
 * from TAM using the TAM Connector for IBM Tivoli Directory Integrator
 */
public class User extends CommonBase {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String mLoginID;

	private String mDN;

	private String mCN;

	private String mSN;

	private String mDesc;

	private String mPassword;

	private Boolean mIsAccountValid;

	private Boolean mIsPasswordValid;

	private Boolean mIsPDUser;

	private Boolean mIsSSOUser;

	private Boolean mNoPasswordPolicy;

	private Integer mMaxFailedLogins;

	private Integer mMaxConcWebSessions;

	private List mUserGroups;

	private Boolean mReplaceGroups;

	private Attribute mGroupsAtt;
	// Some of these consts values have not been changed so as to keep the
	// functionality of the Connector currently in use intact.
	public static final String USER_ATTR_LOGIN_ID = "UserName";

	private static final String USER_ATTR_DN = "RegistryUID";

	private static final String USER_ATTR_CN = "FirstName";

	private static final String USER_ATTR_SN = "LastName";

	private static final String USER_ATTR_DESCRIPTION = "Description";

	private static final String USER_ATTR_PASSWORD = "Password";

	private static final String USER_ATTR_IS_ACCOUNT_VALID = "IsAccountValid";

	private static final String USER_ATTR_IS_PASSWORD_VALID = "IsPasswordValid";

	private static final String USER_ATTR_IS_PD_USER = "IsPDUser";

	private static final String USER_ATTR_IS_SSO_USER = "IsSSOUser";

	private static final String USER_ATTR_PWD_POLICY = "NoPasswordPolicyOnCreate";

	private static final String USER_ATTR_MAX_FAILED_LOGINS = "MaxFailedLogins";

	private static final String USER_ATTR_MAX_CONCURRENT_WEB_SESSIONS = "MaxConcurrentWebSessions"; //to be deprecated, but remains for compatibility with solutions designed for previous version of connector

	private static final String USER_ATTR_MAX_CONC_WEB_SESSIONS = "MaxConcWebSessions"; //added to be consistent with the attr name used for Policy entry types.

	private static final String USER_ATTR_GROUPS = "Groups";

	private static final String USER = "User";

	private static final String USER_REPLACE_GROUPS = "ReplaceGroupsOnUpdate";

	/**
	 * Constructor used to instantiate a User from the PDUser object.
	 * <p>
	 * Uses the common log object from the TAMConnector class.
	 * 
	 * @param s
	 *            Contains the User name
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The Connector log
	 * 
	 * @throws PDException
	 * 
	 * @see PDUser
	 * @see TAMConnector
	 */
	public User(String s, PDContext context, Log log) throws PDException {
		super(context, log);
		Trace.entrymin(this, "User Constructor #1");
		PDUser user = new PDUser(mPDContext, s, mPDMessages);
		processMsgs(mPDMessages);
		set(user);
		this.mLogProxy = log;
		Trace.exitmin(this, "User:Constructor #1");
	}

	/**
	 * Constructor used to instantiate a User from the Entry object.
	 * <p>
	 * Uses the common log object from the TAMConnector class.
	 * 
	 * @param entry
	 *            the Entry object
	 * @param context
	 *            the TAM context
	 * @param log
	 *            the LogProxyImpl object
	 * 
	 * @see Entry
	 * @see TAMConnector
	 */
	public User(Entry entry, PDContext context, Log log) {
		super(context, log);
		Trace.entrymin(this, "User Constructor #2");
		mLoginID = getStringEntryAttributeValue(entry, USER_ATTR_LOGIN_ID);
		mDN = getStringEntryAttributeValue(entry, USER_ATTR_DN);
		mCN = getStringEntryAttributeValue(entry, USER_ATTR_CN);
		mSN = getStringEntryAttributeValue(entry, USER_ATTR_SN);
		mDesc = getStringEntryAttributeValue(entry, USER_ATTR_DESCRIPTION);
		mPassword = getStringEntryAttributeValue(entry, USER_ATTR_PASSWORD);
		mIsAccountValid = getBooleanEntryAttributeValue(entry,
				USER_ATTR_IS_ACCOUNT_VALID);
		mIsPasswordValid = getBooleanEntryAttributeValue(entry,
				USER_ATTR_IS_PASSWORD_VALID);
		mIsPDUser = getBooleanEntryAttributeValue(entry, USER_ATTR_IS_PD_USER);
		mIsSSOUser = getBooleanEntryAttributeValue(entry, USER_ATTR_IS_SSO_USER);
		mNoPasswordPolicy = getBooleanEntryAttributeValue(entry,
				USER_ATTR_PWD_POLICY);
		mMaxFailedLogins = getIntegerEntryAttributeValue(entry,
				USER_ATTR_MAX_FAILED_LOGINS);
		mMaxConcWebSessions = getIntegerEntryAttributeValue(entry, USER_ATTR_MAX_CONC_WEB_SESSIONS);
		if (mMaxConcWebSessions == null){
				debug("###### Using the attribute maxConcurrentWebSessions since maxConcWebSessions was null");
				mMaxConcWebSessions = getIntegerEntryAttributeValue(entry, USER_ATTR_MAX_CONCURRENT_WEB_SESSIONS);
		}
		if (mMaxConcWebSessions != null)
		{
			debug("###### " + USER_ATTR_MAX_CONC_WEB_SESSIONS + " = " + mMaxConcWebSessions.intValue());
		}
		mReplaceGroups = getBooleanEntryAttributeValue(entry,
				USER_REPLACE_GROUPS);
		if (mReplaceGroups == null)
			mReplaceGroups = Boolean.TRUE;
		debug("####### " + USER_REPLACE_GROUPS + " = "
				+ mReplaceGroups.toString());
		// Assign/Clear the groups
		mGroupsAtt = entry.getAttribute(USER_ATTR_GROUPS);
		if (mGroupsAtt != null) {
			// Code checks attribute operation type delete, but currently this
			// seems redundant
			// as IBM Tivoli Directory Integrator doesn't factor this in to it's "Compute Changes" function
			// for multi-valued
			// attributes. But does check the operation type for each of the
			// value objects.
			// The other value clear condition is when the operation type is
			// replace and an empty
			// list of values is provided.
			if ((mGroupsAtt.getOperation().equalsIgnoreCase("delete") || mGroupsAtt
					.getOper() == Attribute.ATTRIBUTE_DELETE)
					|| ((mGroupsAtt.getOperation().equalsIgnoreCase("replace") || mGroupsAtt
							.getOper() == Attribute.ATTRIBUTE_REPLACE) && (mGroupsAtt
							.getValuesVector().size() < 1))) {
				mUserGroups = new ArrayList(0);
			} else {
				List groups = new ArrayList(mGroupsAtt.getValuesVector());
				// purge empty strings
				for (int i = groups.size() - 1; i >= 0; i--) {
					String group = (String) groups.get(i);
					if (group == null || group.length() == 0)
						groups.remove(i);
				}
				mUserGroups = groups;
			}
		} else {
			mUserGroups = new ArrayList(0);
		}
		Trace.exitmin(this, "User Constructor #2");
	}

	/**
	 * Constructor used to instantiate a User from the search criteria.
	 * <p>
	 * Uses the common log object from the TAMConnector class.
	 * 
	 * @param searchcriteria
	 *            the user login search criteria
	 * @param context
	 *            the TAM context
	 * @param log
	 *            the LogProxyImpl object
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 * 
	 * @see SearchCriteria
	 * @see PDContext
	 * @see TAMConnector
	 */
	public User(SearchCriteria searchcriteria, PDContext context, Log log)
			throws TAMConnectorException, PDException {
		super(context, log);
		Trace.entrymin(this, "User Constructor #3");
		this.mLogProxy = log;
		String s = searchcriteria.getFirstCriteriaName();
		int i = searchcriteria.getFirstCriteriaMatch();
		if (!s.equalsIgnoreCase(USER_ATTR_LOGIN_ID)
				|| i != SearchCriteria.EXCACT) {
			throw new TAMConnectorException(TMSMessageGetter
					.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
		}
		debug("User is (" + searchcriteria.getFirstCriteriaValue() + ")");
		PDUser user = new PDUser(mPDContext, searchcriteria
				.getFirstCriteriaValue(), mPDMessages);
		processMsgs(mPDMessages);
		set(user);
		Trace.exitmin(this, "User Constructor #3");
	}

	/**
	 * Constructor used to instantiate a User from the search criteria. It
	 * populates the User only with the attributes it can get from the search
	 * criteria.
	 * <p>
	 * Uses the common log object from the TAMConnector class.
	 * 
	 * @param context
	 *            the TAM context
	 * @param searchcriteria
	 *            the user login search criteria
	 * @param log
	 *            the LogProxyImpl object
	 * 
	 * @throws TAMConnectorException
	 * 
	 * @see SearchCriteria
	 * @see PDContext
	 * @see TAMConnector
	 */
	public User(PDContext context, SearchCriteria searchcriteria, Log log)
			throws TAMConnectorException {
		super(context, log);
		Trace.entrymin(this, "User Constructor #4");
		this.mLogProxy = log;
		String s = searchcriteria.getFirstCriteriaName();
		String sv = searchcriteria.getFirstCriteriaValue();
		int i = searchcriteria.getFirstCriteriaMatch();
		if (!s.equalsIgnoreCase(USER_ATTR_LOGIN_ID)
				|| i != SearchCriteria.EXCACT) {
			throw new TAMConnectorException(TMSMessageGetter
					.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
		}
		if (sv == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, USER_ATTR_LOGIN_ID));
		}
		debug("User is (" + sv + ")");
		mLoginID = sv;
		Trace.exitmin(this, "User Constructor #4");
	}

	private void set(PDUser pdUser) throws PDException {
		Trace.entrymin(this, "User.set");
		mLoginID = pdUser.getId();
		mDN = pdUser.getRgyName();
		mCN = pdUser.getFirstName();
		mSN = pdUser.getLastName();
		mDesc = pdUser.getDescription();
		mPassword = null;
		mIsAccountValid = Boolean.valueOf(pdUser.isAccountValid());
		mIsPasswordValid = Boolean.valueOf(pdUser.isPasswordValid());
		mIsPDUser = Boolean.valueOf(pdUser.isPDUser());
		mIsSSOUser = Boolean.valueOf(pdUser.isSSOUser());
		mNoPasswordPolicy = Boolean.TRUE;
		mMaxFailedLogins = Integer.valueOf(pdUser.getPolicy()
				.getMaxFailedLogins());
		if (mMaxFailedLogins.intValue() == 0) {
			// Policy may not be enfortced
			mMaxFailedLogins = null;
		}
		mMaxConcWebSessions = Integer.valueOf(pdUser.getPolicy()
				.getMaxConcurrentWebSessions());
		if (mMaxConcWebSessions.intValue() == 0) {
			// Policy may not be enfortced
			mMaxConcWebSessions = null;
		}
		// set user groups
		mUserGroups = pdUser.getGroups();
		if (mUserGroups == null)
			mUserGroups = new ArrayList(0);
		Trace.exitmin(this, "User.set");
	}

	/**
	 * Returns the Groups associated with the user
	 * 
	 * @return mUserGroups
	 * 
	 * @see Vector
	 */
	public List getGroups() {
		return mUserGroups;
	}

	/**
	 * Return the Login ID for the User
	 * 
	 * @return mLoginID
	 */
	public String getLoginID() {
		return mLoginID;
	}

	/**
	 * Constructs an Entry object from the user details.
	 * <p>
	 * The Entry object is used by IBM Tivoli Directory Integrator
	 * 
	 * @return Entry
	 * 
	 * @see Entry
	 */
	public Entry getAttributes() {
		Trace.entrymin(this, "User.getAttributes");
		Entry entry = new Entry();
		createAndAddEntryAttribute(entry, USER_ATTR_LOGIN_ID, mLoginID);
		createAndAddEntryAttribute(entry, USER_ATTR_DN, mDN);
		createAndAddEntryAttribute(entry, USER_ATTR_CN, mCN);
		createAndAddEntryAttribute(entry, USER_ATTR_SN, mSN);
		createAndAddEntryAttribute(entry, USER_ATTR_DESCRIPTION, mDesc);
		createAndAddEntryAttribute(entry, USER_ATTR_PASSWORD, mPassword);
		createAndAddEntryAttribute(entry, USER_ATTR_IS_ACCOUNT_VALID,
				mIsAccountValid);
		createAndAddEntryAttribute(entry, USER_ATTR_IS_PASSWORD_VALID,
				mIsPasswordValid);
		createAndAddEntryAttribute(entry, USER_ATTR_IS_PD_USER, mIsPDUser);
		createAndAddEntryAttribute(entry, USER_ATTR_IS_SSO_USER, mIsSSOUser);
		createAndAddEntryAttribute(entry, USER_ATTR_PWD_POLICY,
				mNoPasswordPolicy);
		createAndAddEntryAttribute(entry, USER_ATTR_MAX_FAILED_LOGINS,
				mMaxFailedLogins);
		createAndAddEntryAttribute(entry, USER_ATTR_MAX_CONC_WEB_SESSIONS,
				mMaxConcWebSessions);
		createAndAddEntryAttribute(entry, USER_ATTR_MAX_CONCURRENT_WEB_SESSIONS,
				mMaxConcWebSessions);
		createAndAddEntryAttribute(entry, USER_REPLACE_GROUPS, mReplaceGroups);
		Attribute attribute = entry.newAttribute(USER_ATTR_GROUPS);
		if (mUserGroups != null) {
			int i = mUserGroups.size();
			for (int j = 0; j < i; j++)
				attribute.addValue(mUserGroups.get(j).toString());
		}
		Trace.exitmin(this, "User.getAttributes");
		return entry;
	}

	private void validate() throws TAMConnectorException {
		if (mLoginID == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, USER_ATTR_LOGIN_ID));
		}
		debug(USER_ATTR_LOGIN_ID + " is: " + mLoginID);
		if (mDN == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, USER_ATTR_DN));
		}
		debug(USER_ATTR_DN + " is: " + mDN);
		if (mCN == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, USER_ATTR_CN));
		}
		debug(USER_ATTR_CN + " is: " + mCN);
		if (mSN == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, USER_ATTR_SN));
		}
		debug(USER_ATTR_SN + " is: " + mSN);
	}

	/**
	 * Creates a user in TAM from the IBM Tivoli Directory Integrator input
	 * details
	 * 
	 * @param importReg
	 *            <code>true</code> if the user should be import created,
	 *            <code>false</code> if the use should be created without
	 *            importing.
	 * 
	 * @throws TAMConnectorException
	 */
	public void put(boolean importReg) throws TAMConnectorException {
		Trace.entrymin(this, "User.put");
		HashMap failed = new HashMap(5);
		List groups = new ArrayList(mUserGroups);
		validate();
		PDRgyUserName pdRgyUserName = new PDRgyUserName(mDN, mCN, mSN);
		debug("importReg = " + (importReg == true ? "true" : "false"));
		debug(TMSMessageGetter.getMessage(TMSMsgId.USER_GROUPS, mUserGroups
				.toString()));
		if (importReg) {
			String initialGroup = null;
			if (groups.size() > 0) {
				initialGroup = groups.get(0).toString();
			}
			try {
				PDUser.importUser(mPDContext, mLoginID, pdRgyUserName,
						initialGroup, (mIsSSOUser == null ? false : mIsSSOUser
								.booleanValue()), mPDMessages);
				processMsgs(mPDMessages);
			} catch (PDException pde) {
				throw new TAMConnectorException(getPDMessage(pde));
			}
			// remove the initial group from the list
			if (groups.size() > 0) {
				groups.remove(0);
				// add the rest of the groups to the user
				addGroups(groups, failed);
			}
		} else {
			// have to cast as method expects an ArrayList
			try {
				PDUser
						.createUser(mPDContext, mLoginID, pdRgyUserName, null,
								(mPassword == null ? "".toCharArray()
										: mPassword.toCharArray()),
								(ArrayList) groups, (mIsSSOUser == null ? false
										: mIsSSOUser.booleanValue()),
								(mNoPasswordPolicy == null ? true
										: mNoPasswordPolicy.booleanValue()),
								mPDMessages);
				processMsgs(mPDMessages);
			} catch (PDException pde) {
				throw new TAMConnectorException(getPDMessage(pde));
			}
		}
		try {
			if (mDesc != null && mDesc.length() > 0) {
				PDUser.setDescription(mPDContext, mLoginID, mDesc, mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(USER_ATTR_DESCRIPTION, getPDMessage(pde));
		}
		try {
			if (mIsPasswordValid != null) {
				PDUser.setPasswordValid(mPDContext, mLoginID, mIsPasswordValid
						.booleanValue(), mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(USER_ATTR_IS_PASSWORD_VALID, getPDMessage(pde));
		}
		try {
			if (mIsAccountValid != null) {
				PDUser.setAccountValid(mPDContext, mLoginID, mIsAccountValid
						.booleanValue(), mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(USER_ATTR_IS_ACCOUNT_VALID, getPDMessage(pde));
		}
		try {
			if (mMaxFailedLogins != null) {
				// Setting the Max Failed Logins for the User and enforce.
				PDPolicy.setMaxFailedLogins(mPDContext, mLoginID,
						mMaxFailedLogins.intValue(), true, mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(USER_ATTR_MAX_FAILED_LOGINS, getPDMessage(pde));
		}
		try {
			if (mMaxConcWebSessions != null) {
				// Setting the Max Conc Web Sessions for the User and enforce.
				PDPolicy.setMaxConcurrentWebSessions(mPDContext, mLoginID,
						mMaxConcWebSessions.intValue(), true, false, false, mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(USER_ATTR_MAX_CONC_WEB_SESSIONS, getPDMessage(pde));
		}
		// if we have failed attributes, throw an exception.
		if (failed.size() > 0) {
			String msg = TMSMessageGetter.getMessage(TMSMsgId.CREATE_ERROR,
					USER);
			mLogProxy.logerror(msg);
			throw new TAMConnectorException(failed, msg);
		}
		Trace.exitmin(this, "User.put");
	}

	private void addGroups(List groups, HashMap failed) {
		Trace.entrymin(this, "User.addGroups");
		if (groups.size() > 0) {
			logmsg(TMSMessageGetter.getMessage(TMSMsgId.GROUPS_TO_ADD, groups
					.toString()));
			List userList = new ArrayList();
			userList.add(mLoginID);
			for (int i = 0; i < groups.size(); i++) {
				logmsg(TMSMessageGetter.getMessage(TMSMsgId.ADD_USER_TO_GROUP,
						mLoginID + "->" + groups.get(i).toString()));
				try {
					PDGroup.addMembers(mPDContext, groups.get(i).toString(),
							(ArrayList) userList, mPDMessages);
					processMsgs(mPDMessages);
				} catch (PDException pde) {
					failed.put(USER_ATTR_GROUPS + i, getPDMessage(pde));
				}
			}
		}
		Trace.exitmin(this, "User.addGroups");
	}

	public String desc() {
		return mDesc;
	}

	public String password() {
		return mPassword;
	}

	public Boolean isAccountValid() {
		return mIsAccountValid;
	}

	public Boolean isPasswordValid() {
		return mIsPasswordValid;
	}

	public Boolean isSSOUser() {
		return mIsSSOUser;
	}

	public Integer maxFailedLogins() {
		return mMaxFailedLogins;
	}

	public Integer maxConcurrentWebSessions() {
		return mMaxConcWebSessions;
	}

	public Boolean replaceGroups() {
		return mReplaceGroups;
	}

	public Attribute groupsAttribute() {
		return mGroupsAtt;
	}

	/**
	 * Modify a user entry.
	 * 
	 * @param changes
	 *            The user entry to modify.
	 * 
	 * @throws TAMConnectorException
	 */
	public void modify(Entry changes) throws TAMConnectorException {
		Trace.entrymin(this, "User.modify");
		PDUser oldUser;
		HashMap failed = new HashMap(5);
		User newUser = new User(changes, mPDContext, mLogProxy);
		if (mLoginID == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, USER_ATTR_LOGIN_ID));
		}
		try {
			oldUser = new PDUser(mPDContext, mLoginID, mPDMessages);
		} catch (PDException pde) {
			throw new TAMConnectorException(getPDMessage(pde));
		}
		// Update mLoginID (not possible - API limitation)
		// Update mDN (not possible - API limitation)
		// Update mCN (not possible - API limitation)
		// Update mSN (not possible - API limitation)
		// Update mDesc;
		if (newUser.desc() != null && newUser.desc().length() > 0) {
			try {
				oldUser.setDescription(mPDContext, newUser.desc(), mPDMessages);
			} catch (PDException pde) {
				failed.put(USER_ATTR_DESCRIPTION, getPDMessage(pde));
			}
		}
		// Update mPassword;
		if (newUser.password() != null && newUser.password().length() > 0) {
			try {
				oldUser.setPassword(mPDContext, newUser.password()
						.toCharArray(), mPDMessages);
			} catch (PDException pde) {
				failed.put(USER_ATTR_PASSWORD, getPDMessage(pde));
			}
		}
		// Update mIsAccountValid;
		if (newUser.isAccountValid() != null) {
			try {
				oldUser.setAccountValid(mPDContext, newUser.isAccountValid()
						.booleanValue(), mPDMessages);
			} catch (PDException pde) {
				failed.put(USER_ATTR_IS_ACCOUNT_VALID, getPDMessage(pde));
			}
		}
		// Update mIsPasswordValid;
		if (newUser.isPasswordValid() != null) {
			try {
				oldUser.setPasswordValid(mPDContext, newUser.isPasswordValid()
						.booleanValue(), mPDMessages);
			} catch (PDException pde) {
				failed.put(USER_ATTR_IS_PASSWORD_VALID, getPDMessage(pde));
			}
		}
		// Update mIsPDUser (not possible - API limitation)
		// Update mIsSSOUser;
		if (newUser.isSSOUser() != null) {
			try {
				oldUser.setSSOUser(mPDContext, newUser.isSSOUser()
						.booleanValue(), mPDMessages);
			} catch (PDException pde) {
				failed.put(USER_ATTR_IS_SSO_USER, getPDMessage(pde));
			}
		}
		// Update mPasswordPolicy (not possible - API limitation)
		// Update mMaxFailedLogins
		if (newUser.maxFailedLogins() != null) {
			try {
				// Setting the Max Failed Logins for the User and enforce.
				PDPolicy.setMaxFailedLogins(mPDContext, mLoginID, newUser
						.maxFailedLogins().intValue(), true, mPDMessages);
				processMsgs(mPDMessages);
			} catch (PDException pde) {
				failed.put(USER_ATTR_MAX_FAILED_LOGINS, getPDMessage(pde));
			}
		}
		// Update mMaxConcWebSessions
		if (newUser.maxConcurrentWebSessions() != null) {
			try {
				// Setting the Max Conc Web Sessions for the User and enforce.
				PDPolicy.setMaxConcurrentWebSessions(mPDContext, mLoginID, newUser
						.maxConcurrentWebSessions().intValue(), true, false, false, mPDMessages);
				processMsgs(mPDMessages);
			} catch (PDException pde) {
				failed.put(USER_ATTR_MAX_CONC_WEB_SESSIONS, getPDMessage(pde));
			}
		}
		if (newUser.getGroups().size() > 0) {
			if (newUser.replaceGroups().booleanValue() == true) {
				debug("mReplaceGroups = true");
				// original functionality to replace all then add all.
				try {
					// Update the group membership
					if (newUser.getGroups().toString().length() > 0
							&& !newUser.getGroups().toString()
									.equalsIgnoreCase(
											oldUser.getGroups().toString())) {
						removeGroups(oldUser.getGroups(), failed);
						addGroups(newUser.getGroups(), failed);
					}
				} catch (PDException pde) {
					failed.put(USER_ATTR_GROUPS, getPDMessage(pde));
				}
			} else {
				// this functionality is for ITIM where the Attribute List
				// specifies the operation for each value.
				debug("mReplaceGroups = false");
				ArrayList list = new ArrayList();
				list.add(mLoginID);
				Attribute groupsAtt = newUser.groupsAttribute();
				debug("##### processing " + groupsAtt.size() + " groups");
				for (int i = 0; i < groupsAtt.size(); i++) {
					try {
						// check if the operation code is for deletion or
						// addition of group
						if (groupsAtt.getValueOperation(i).equalsIgnoreCase(
								Attribute.OPER[AttributeValue.AV_DELETE])) {
							// remove the group membership (remove the member
							// form the group)
							PDGroup.removeMembers(mPDContext, groupsAtt
									.getValue(i).toString(), list, mPDMessages);
							processMsgs(mPDMessages);
							logmsg(TMSMessageGetter.getMessage(
									TMSMsgId.DELETE_USER_FROM_GROUP, mLoginID
											+ "->"
											+ groupsAtt.getValue(i).toString()));
						} else if (groupsAtt.getValueOperation(i)
								.equalsIgnoreCase(
										Attribute.OPER[AttributeValue.AV_ADD])) {
							// add to this group membership
							PDGroup.addMembers(mPDContext, groupsAtt
									.getValue(i).toString(), list, mPDMessages);
							processMsgs(mPDMessages);
							logmsg(TMSMessageGetter.getMessage(
									TMSMsgId.ADD_USER_TO_GROUP, mLoginID + "->"
											+ groupsAtt.getValue(i).toString()));
						} else {
							logmsg(TMSMessageGetter.getMessage(
									TMSMsgId.INVALID_OPER_CODE,
									Attribute.OPER[groupsAtt.getValueOper(i)]));
						}
					} catch (PDException pde) {
						failed.put(USER_ATTR_GROUPS + i
								+ Attribute.OPER[groupsAtt.getValueOper(i)],
								getPDMessage(pde));
					}
				}
			}
		} else if (newUser.replaceGroups().booleanValue() == true
				&& newUser.groupsAttribute() != null) {
			// Groups attribute present but empty and one of the 2 replace
			// groups flags is set
			debug("##### clearing group assignment.");
			// clearing group assignment for this account
			try {
				removeGroups(oldUser.getGroups(), failed);
			} catch (PDException pde) {
				failed.put(USER_ATTR_GROUPS, getPDMessage(pde));
			}
		} else {
			debug("##### unchanged group assignment.");
		}
		if (failed.size() > 0) {
			logmsg(TMSMessageGetter.getMessage(TMSMsgId.MODIFY_ERROR, USER));
			throw new TAMConnectorException(failed, TMSMessageGetter
					.getMessage(TMSMsgId.MODIFY_ERROR, USER));
		}
		Trace.exitmin(this, "User.modify");
	}

	/**
	 * Delete user entry.
	 * 
	 * @param deleteReg
	 *            <code>true</code> to delete the user details from TAM and
	 *            the registry <code>false</code> to delete from TAM and not
	 *            the registry
	 * 
	 * @throws TAMConnectorException
	 */
	public void delete(boolean deleteReg) throws TAMConnectorException,
			PDException {
		Trace.entrymin(this, "User.delete");
		if (mLoginID == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, USER_ATTR_LOGIN_ID));
		}
		if (mLoginID.equalsIgnoreCase("sec_master")
				|| mLoginID.equalsIgnoreCase("iv-mgrd/master")
				|| mLoginID.startsWith("ivacld")
				|| mLoginID.startsWith("amwpm")) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.USER_RESERVED, mLoginID));
		}
		logmsg(TMSMessageGetter.getMessage(TMSMsgId.DELETE_USER, mLoginID));
		PDUser.deleteUser(mPDContext, mLoginID, deleteReg, mPDMessages);
		processMsgs(mPDMessages);
		Trace.exitmin(this, "User.delete");
	}

	private void removeGroups(List groups, HashMap failed) {
		Trace.entrymin(this, "User.removeGroups");
		if (groups.size() > 0) {
			ArrayList list = new ArrayList();
			list.add(mLoginID);
			logmsg(TMSMessageGetter.getMessage(TMSMsgId.GROUPS_TO_DELETE,
					groups.toString()));
			for (int i = 0; i < groups.size(); i++) {
				logmsg(TMSMessageGetter.getMessage(
						TMSMsgId.DELETE_USER_FROM_GROUP, mLoginID + "->"
								+ groups.get(i).toString()));
				try {
					PDGroup.removeMembers(mPDContext, groups.get(i).toString(),
							list, mPDMessages);
					processMsgs(mPDMessages);
				} catch (PDException pde) {
					failed
							.put(USER_ATTR_GROUPS + i + "_del",
									getPDMessage(pde));
				}
			}
		}
		Trace.exitmin(this, "User.removeGroups");
	}

	/**
	 * returns the schema for IBM Tivoli Directory Integrator.
	 * 
	 * @return Vector
	 */
	public static Vector schema() {
		Vector vector = new Vector();
		addSchemaEntry(vector, USER_ATTR_LOGIN_ID, QSS_STRING, Integer
				.valueOf(256));
		addSchemaEntry(vector, USER_ATTR_DN, QSS_STRING, Integer.valueOf(256));
		addSchemaEntry(vector, USER_ATTR_CN, QSS_STRING, Integer.valueOf(256));
		addSchemaEntry(vector, USER_ATTR_SN, QSS_STRING, Integer.valueOf(256));
		addSchemaEntry(vector, USER_ATTR_DESCRIPTION, QSS_STRING, Integer
				.valueOf(256));
		addSchemaEntry(vector, USER_ATTR_PASSWORD, QSS_STRING, Integer
				.valueOf(256));
		addSchemaEntry(vector, USER_ATTR_IS_ACCOUNT_VALID, QSS_BOOLEAN, null);
		addSchemaEntry(vector, USER_ATTR_IS_PASSWORD_VALID, QSS_BOOLEAN, null);
		addSchemaEntry(vector, USER_ATTR_IS_PD_USER, QSS_BOOLEAN, null);
		addSchemaEntry(vector, USER_ATTR_IS_SSO_USER, QSS_BOOLEAN, null);
		addSchemaEntry(vector, USER_ATTR_PWD_POLICY, QSS_BOOLEAN, null);
		addSchemaEntry(vector, USER_ATTR_MAX_FAILED_LOGINS, QSS_INTEGER, null);
		addSchemaEntry(vector, USER_ATTR_MAX_CONC_WEB_SESSIONS, QSS_INTEGER, null);
		addSchemaEntry(vector, USER_ATTR_GROUPS, QSS_STRING, Integer
				.valueOf(256));
		addSchemaEntry(vector, USER_REPLACE_GROUPS, QSS_BOOLEAN, null);
		return vector;
	}

	/**
	 * Returns a list (ArrayList) of all the Users for the TAM Context
	 * 
	 * @param pdContext
	 *            The TAM Context
	 * 
	 * @return List (ArrayList)
	 * 
	 * @throws PDException
	 */
	public static List list(PDContext pdContext) throws PDException {
		PDMessages msgs = new PDMessages();
		return PDUser.listUsers(pdContext, PDUser.PDUSER_ALLPATTERN,
				PDUser.PDUSER_MAXRETURN, false, msgs);
	}

	/**
	 * Returns a list (ArrayList) of all the Users for the TAM Context matching
	 * search criteria value
	 * 
	 * @param pdContext
	 *            The TAM Context
	 * 
	 * @param searchCriteria
	 *            The IDI search criteria
	 * 
	 * @return List (ArrayList)
	 * 
	 * @throws PDException
	 */
	public static List list(SearchCriteria searchCriteria, PDContext pdContext)
			throws PDException {
		int size = searchCriteria.size();
		int posn = 0;
		String name = "";
		boolean found = false;
		while((!found)&&(posn < size)) {
			name = searchCriteria.getCriteria(posn).name;
			if(name.equals(USER_ATTR_LOGIN_ID)) {
				found = true;
			} else {
				posn = posn + 1;
			}
		}
		if (found) {	
			return User.list(searchCriteria.getCriteria(posn).value.toString(), pdContext);
		}
		else {
			return User.list("", pdContext);
		}
	}

	/**
	 * Returns a filtered list (ArrayList) of all the Users for the TAM Context
	 * 
	 * @param filter
	 *            A TAM User filter string. The filter is a case-sensitive
	 *            mixture of string constants and wildcards. Filtering is on the
	 *            principal name
	 * @param pdContext
	 *            The TAM Contex
	 * 
	 * @return List (ArrayList)
	 * 
	 * @throws PDException
	 */
	public static List list(String filter, PDContext pdContext)
			throws PDException {
		PDMessages msgs = new PDMessages();
		if (filter == null || filter.length() == 0) {
			return PDUser.listUsers(pdContext, PDUser.PDUSER_ALLPATTERN,
					PDUser.PDUSER_MAXRETURN, false, msgs);
		} else {
			return PDUser.listUsers(pdContext, filter, PDUser.PDUSER_MAXRETURN,
					false, msgs);
		}
	}
}
