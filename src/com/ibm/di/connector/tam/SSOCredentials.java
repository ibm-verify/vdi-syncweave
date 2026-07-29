/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.tam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;
import com.tivoli.pd.jadmin.PDSSOCred;
import com.tivoli.pd.jadmin.PDSSOResource;
import com.tivoli.pd.jutil.PDContext;
import com.tivoli.pd.jutil.PDException;
import com.tivoli.pd.jutil.PDMessages;

/**
 * SSOCredentials class contains the functionality to find, add, modify and
 * delete SSO Resource Info from TAM using the TAM Connector for TDI.
 */
public class SSOCredentials extends CommonBase {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String mUserID;

	private String mResourceName;

	private String mResourcePassword;

	private String mResourceType;

	private String mResourceUser;

	private Entry findEntry = null;

	public static final String SSOCRED_ATTR_USER_ID = "UserName";

	public static final String SSOCRED_ATTR_RESOURCE_NAME = "ResourceName";

	public static final String SSOCRED_ATTR_RESOURCE_PASSWORD = "ResourcePassword";

	public static final String SSOCRED_ATTR_RESOURCE_TYPE = "ResourceType";

	public static final String SSOCRED_ATTR_RESOURCE_USER = "ResourceUser";

	private static final String RSRC_GROUP = "Resource Group";

	private static final String RSRC = "Web Resource";

	/**
	 * SSOCredentials Constructor
	 * 
	 * @param pdSSOCred
	 *            TAM SSO Credentials
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            TDI Connector log
	 * 
	 * @throws PDException
	 */
	public SSOCredentials(PDSSOCred pdSSOCred, PDContext context, Log log)
			throws PDException {
		super(context, log);
		Trace.entrymin(this, "SSOCredentials Constructor #1");
		mUserID = pdSSOCred.getUser();
		mResourceName = pdSSOCred.getResourceName();
		mResourcePassword = new String(pdSSOCred.getResourcePassword());
		mResourceType = pdSSOCred.getResourceType();
		mResourceUser = pdSSOCred.getResourceUser();
		Trace.exitmin(this, "SSOCredentials Constructor #1");
	}

	/**
	 * SSOCredentials Constructor
	 * 
	 * @param credInfo
	 *            The SSO Credentials Info
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The TDI Connector log
	 * 
	 * @throws PDException
	 */
	public SSOCredentials(PDSSOCred.CredInfo credInfo, PDContext context,
			Log log) throws PDException {
		super(context, log);
		Trace.entrymin(this, "SSOCredentials Constructor #2");
		set(credInfo);
		Trace.exitmin(this, "SSOCredentials Constructor #2");
	}

	/**
	 * SSOCredentials Constructor
	 * 
	 * @param entry
	 *            The TDI entry data
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The TAM Connector log
	 */
	public SSOCredentials(Entry entry, PDContext context, Log log) {
		super(context, log);
		Trace.entrymin(this, "SSOCredentials Constructor #3");
		set(entry);
		Trace.exitmin(this, "SSOCredentials Constructor #3");
	}

	/**
	 * SSOCredentials Constructor
	 * 
	 * @param searchcriteria
	 *            The search criteria
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The log
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public SSOCredentials(SearchCriteria searchcriteria, PDContext context,
			Log log) throws TAMConnectorException, PDException {
		super(context, log);
		Trace.entrymin(this, "SSOCredentials Constructor #4");
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
		debug("Search Criteria 1 = " + userName);
		debug("Search Criteria 2 = " + resourceName);
		debug("Search Criteria 3 = " + resourceType);
		if (mPDContext == null)
			debug("mPDContext is null");
		if (mPDMessages == null)
			debug("mPDMessages is null");
		ArrayList creds = PDSSOCred.listAndShowSSOCreds(mPDContext, userName,
				mPDMessages);
		if (creds.size() > 0) {
			findEntry = new Entry();
		} else {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.NO_CREDS_FOR_USER, userName));
		}
		boolean foundCreds = false;
		for (Iterator iter = creds.iterator(); iter.hasNext();) {
			PDSSOCred.CredInfo element = (PDSSOCred.CredInfo) iter.next();
			if (chooseElement(element,resourceName,resourceType))
			{
				foundCreds = true;
				if (findEntry == null)
				{
					break;
				}
				else {
					set(element);
					createAndAddEntryAttribute(findEntry, SSOCRED_ATTR_USER_ID, mUserID);
					createAndAddEntryAttribute(findEntry, SSOCRED_ATTR_RESOURCE_NAME, mResourceName);
					createAndAddEntryAttribute(findEntry, SSOCRED_ATTR_RESOURCE_PASSWORD, mResourcePassword);
					createAndAddEntryAttribute(findEntry, SSOCRED_ATTR_RESOURCE_TYPE, mResourceType);
					createAndAddEntryAttribute(findEntry, SSOCRED_ATTR_RESOURCE_USER, mResourceUser);
				}
			}
		}
		if (foundCreds == false)
		{
			throw new TAMConnectorException(TMSMessageGetter.getMessage(TMSMsgId.NO_CREDS_FOR_USER, userName));
		}
		Trace.exitmin(this, "SSOCredentials Constructor #4");
	}

	/**
	 * SSOCredentials Constructor. Does not instantiate any PDObject but
	 * initialises the object only from the searchcriteria.
	 * 
	 * @param context
	 *            The TAM Context
	 * @param searchcriteria
	 *            The search criteria
	 * @param log
	 *            The log
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public SSOCredentials(PDContext context, SearchCriteria searchcriteria,
			Log log) throws TAMConnectorException, PDException {
		super(context, log);
		Trace.entrymin(this, "SSOCredentials Constructor #5");
		Vector criteria = searchcriteria.getCriteria();
		for (Iterator iter = criteria.iterator(); iter.hasNext();) {
			SearchCriteria.rscSearch element = (SearchCriteria.rscSearch) iter
					.next();
			debug("Search Criteria Name = " + element.name);
			if (element.name.compareTo(SSOCredentials.SSOCRED_ATTR_USER_ID) == 0) {
				mUserID = (String) element.value;
			} else if (element.name
					.compareTo(SSOCredentials.SSOCRED_ATTR_RESOURCE_NAME) == 0) {
				mResourceName = (String) element.value;
			} else if (element.name
					.compareTo(SSOCredentials.SSOCRED_ATTR_RESOURCE_TYPE) == 0) {
				mResourceType = (String) element.value;
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
		validate_key();
		if (mPDMessages == null)
			debug("mPDMessages is null");
		Trace.exitmin(this, "SSOCredentials Constructor #5");
	}

	/**
	 * Chooses the Credential info belonging to the provided Resource
	 * 
	 * @param element
	 *            The TAM Credential Info
	 * @param resourceName
	 *            The name of the TAM Resource
	 * @param resourceType
	 *            The type of the TAM Resource (ResourceGroup or Web Resource)
	 * 
	 * @throws PDException
	 */
	private boolean chooseElement(PDSSOCred.CredInfo element, String resourceName,
			String resourceType) throws PDException {
		if (null != resourceName && resourceName.length() > 0) {
				debug("ResourceName for Cred = " + element.getResourceName());
				String eName = element.getResourceName();
				if (null != resourceType && resourceType.length() > 0) {
					String eType = element.getResourceType();
					debug("ResourceType for Cred = " + element.getResourceType());
					if (eName.equalsIgnoreCase(resourceName) && (eType.toLowerCase()).startsWith(resourceType.toLowerCase())) {
						set(element);
						findEntry = null;
						return(true);
					} 
				}
				else {
					if (eName.equalsIgnoreCase(resourceName)) {
						return(true);
					}
				}
			}
			else {
				if (null != resourceType && resourceType.length() > 0)
				{
					debug("ResourceType for Cred = " + element.getResourceType());
					String eType = element.getResourceType();
					if (eType.toLowerCase().startsWith(resourceType.toLowerCase())){
						return(true);
					}
				}
				else {
					return(true);
				}
			}
			return(false);
	}

	/**
	 * Sets the private members from the TAM Credentials
	 * 
	 * @param credInfo
	 *            The TAM Credential Info
	 * 
	 * @throws PDException
	 */
	public void set(PDSSOCred.CredInfo credInfo) throws PDException {
		Trace.entrymin(this, "SSOCredentials.set");
		mUserID = credInfo.getUser();
		mResourceName = credInfo.getResourceName();
		mResourcePassword = new String(credInfo.getResourcePassword());
		mResourceType = credInfo.getResourceType();
		mResourceUser = credInfo.getResourceUser();
		Trace.exitmin(this, "SSOCredentials.set");
	}

	/**
	 * Sets the object from the TDI entry object
	 * 
	 * @param entry
	 *            The TDI entry object
	 */
	public void set(Entry entry) {
		Trace.entrymin(this, "SSOCredentials.set");
		String user = getStringEntryAttributeValue(entry, SSOCRED_ATTR_USER_ID);
		if (user != null && user.length() > 0)
			mUserID = user;
		String rName = getStringEntryAttributeValue(entry,
				SSOCRED_ATTR_RESOURCE_NAME);
		if (rName != null && rName.length() > 0)
			mResourceName = rName;
		String rUser = getStringEntryAttributeValue(entry,
				SSOCRED_ATTR_RESOURCE_USER);
		if (rUser != null && rUser.length() > 0)
			mResourceUser = rUser;
		String paswd = getStringEntryAttributeValue(entry,
				SSOCRED_ATTR_RESOURCE_PASSWORD);
		if (paswd != null)
			mResourcePassword = paswd;
		String rType = getStringEntryAttributeValue(entry,
				SSOCRED_ATTR_RESOURCE_TYPE);
		// if no resource type to date then default it
		if ((rType == null || rType.length() < 1) && mResourceType == null) {
			if (mResourceName != null && mResourceName.length() > 0) {
				// Get the resourceType using the API
				boolean isResourceGroup = true;
				try {
					PDSSOResource ssor = new PDSSOResource(mPDContext,
							mResourceName, mPDMessages);			
					if (ssor != null)
						isResourceGroup = false;
				} catch (PDException pde) {
					isResourceGroup = true;
					//default the resource type to group
				}
				if (isResourceGroup)
					mResourceType = RSRC_GROUP;
				else
					mResourceType = RSRC;
					debug("Resource type deduced as " + mResourceType);
			}
		} else if (rType != null && rType.length() > 0) {
			mResourceType = rType;
		}
		Trace.exitmin(this, "SSOCredentials.set");
	}

	/**
	 * Returns the Credential data in the form of an Entry object
	 * 
	 * @return Entry
	 */
	public Entry getAttributes() {
		Trace.entrymin(this, "SSOCredentials.getAttributes");
		Entry entry;
		if (findEntry != null && findEntry.size() > 0) {
			entry = findEntry;
		} else {
			entry = new Entry();
			createAndAddEntryAttribute(entry, SSOCRED_ATTR_USER_ID, mUserID);
			createAndAddEntryAttribute(entry, SSOCRED_ATTR_RESOURCE_NAME,
					mResourceName);
			createAndAddEntryAttribute(entry, SSOCRED_ATTR_RESOURCE_PASSWORD,
					mResourcePassword);
			createAndAddEntryAttribute(entry, SSOCRED_ATTR_RESOURCE_TYPE,
					mResourceType);
			createAndAddEntryAttribute(entry, SSOCRED_ATTR_RESOURCE_USER,
					mResourceUser);
		}
		Trace.exitmin(this, "SSOCredentials.getAttributes");
		return entry;
	}

	private void validate() throws TAMConnectorException {
		Trace.entrymin(this, "SSOCredentials.validate");
		validate_key();
		debug(SSOCRED_ATTR_RESOURCE_USER + " = " + mResourceUser);
		if (mResourceUser == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, SSOCRED_ATTR_RESOURCE_USER));
		}
		debug(SSOCRED_ATTR_RESOURCE_PASSWORD + " = " + mResourcePassword);
		if (mResourcePassword == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, SSOCRED_ATTR_RESOURCE_PASSWORD));
		}
		Trace.exitmin(this, "SSOCredentials.validate");
	}

	private void validate_key() throws TAMConnectorException {
		Trace.entrymin(this, "SSOCredentials.validate_key");
		debug(SSOCRED_ATTR_RESOURCE_NAME + " = " + mResourceName);
		if (mResourceName == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, SSOCRED_ATTR_RESOURCE_NAME));
		}
		debug(SSOCRED_ATTR_RESOURCE_TYPE + " = " + mResourceType);
		if (mResourceType == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, SSOCRED_ATTR_RESOURCE_TYPE));
		}
		debug(SSOCRED_ATTR_USER_ID + " = " + mUserID);
		if (mUserID == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, SSOCRED_ATTR_USER_ID));
		}
		if (mPDContext == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, "PD Context"));
		} else
			debug("Context = " + mPDContext.toString());
		Trace.exitmin(this, "SSOCredentials.validate_key");
	}

	/**
	 * Create a TAM SSO credential for a user.
	 * 
	 * @throws TAMConnectorException
	 */
	public void put() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "SSOCredentials.put");
		logmsg(TMSMessageGetter.getMessage(TMSMsgId.SSOCRED_CREATE, mUserID
				+ "->" + mResourceName));
		validate();
		PDSSOCred.createSSOCred(mPDContext, mResourceName, mResourceType,
				mUserID, mResourceUser, mResourcePassword.toCharArray(),
				mPDMessages);
		processMsgs(mPDMessages);
		Trace.exitmin(this, "SSOCredentials.put");
	}

	/**
	 * Modify the Password for the User's SSO Credentials
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public void modify() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "SSOCredentials.modify");
		validate();
		PDSSOCred.setSSOCred(mPDContext, mUserID, mResourceName, mResourceType,
				mResourceUser, mResourcePassword.toCharArray(), mPDMessages);
		processMsgs(mPDMessages);
		Trace.exitmin(this, "SSOCredentials.modify");
	}

	/**
	 * Delete a Resource/ResourceGroup for a User from the SSO Credentials.
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public void delete() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "SSOCredentials.delete");
		if (mResourceName == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, SSOCRED_ATTR_RESOURCE_NAME));
		}
		if (mResourceType == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, SSOCRED_ATTR_RESOURCE_TYPE));
		}
		if (mUserID == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, SSOCRED_ATTR_USER_ID));
		}
		logmsg(TMSMessageGetter.getMessage(TMSMsgId.SSOCRED_DELETE,
				mResourceName + "->" + mResourceUser));
		PDSSOCred.deleteSSOCred(mPDContext, mResourceName.trim(), mResourceType
				.trim(), mUserID.trim(), mPDMessages);
		processMsgs(mPDMessages);
		Trace.exitmin(this, "SSOCredentials.delete");
	}

	/**
	 * Returns the TDI Schema
	 * 
	 * @return Vector
	 */
	public static Vector schema() {
		Vector vector = new Vector();
		addSchemaEntry(vector, SSOCRED_ATTR_USER_ID, QSS_STRING, Integer
				.valueOf(256));
		addSchemaEntry(vector, SSOCRED_ATTR_RESOURCE_NAME, QSS_STRING, Integer
				.valueOf(256));
		addSchemaEntry(vector, SSOCRED_ATTR_RESOURCE_PASSWORD, QSS_STRING,
				Integer.valueOf(256));
		addSchemaEntry(vector, SSOCRED_ATTR_RESOURCE_TYPE, QSS_STRING, Integer
				.valueOf(256));
		addSchemaEntry(vector, SSOCRED_ATTR_RESOURCE_USER, QSS_STRING, Integer
				.valueOf(256));
		return vector;
	}

	/**
	 * Return a List (ArrayList) of TAM SSO USer Credentials
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
		ArrayList credUsers = null;
		List users = User.list(pdContext);
		for (int i = 0; i < users.size(); i++) {
			List ssoCreds = PDSSOCred.listSSOCreds(pdContext, (String) users
					.get(i).toString(), msgs);
			if (credUsers == null)
				credUsers = new ArrayList(ssoCreds.size());
			if (ssoCreds.size() > 0)
				credUsers.add(users.get(i).toString());
		}
		return credUsers;
	}

	/**
	 * Return a List (ArrayList) of TAM SSO USer Credentials based on search criteria
	 * 
	 * @param pdContext
	 *           The TAM Context
	 * 
	 * @param searchCriteria
	 *           The provided Search Criteria
	 *
	 * @return List (ArrayList)
	 * 
	 * @throws PDException
	 */
	public static List list(SearchCriteria searchCriteria, PDContext pdContext) throws PDException {
		PDMessages msgs = new PDMessages();
		ArrayList credUsers = null;
		
		List users = User.list(searchCriteria, pdContext);
		for (int i = 0; i < users.size(); i++) {
			List ssoCreds = PDSSOCred.listSSOCreds(pdContext, (String) users.get(i).toString(), msgs);
			if (credUsers == null)
				credUsers = new ArrayList(ssoCreds.size());
			if (ssoCreds.size() > 0)
				credUsers.add(users.get(i).toString());
		}
		return credUsers;
	}
}
