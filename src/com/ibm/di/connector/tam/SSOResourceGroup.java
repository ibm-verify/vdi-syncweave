/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.tam;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;
import com.tivoli.pd.jadmin.PDSSOResourceGroup;
import com.tivoli.pd.jutil.PDContext;
import com.tivoli.pd.jutil.PDException;
import com.tivoli.pd.jutil.PDMessages;

/**
 * SSO Resource Groups class contains the functionality to find, add, modify and
 * delete Resource Groups from TAM using the TAM Connector for IBM Tivoli
 * Directory Integrator.
 */
public class SSOResourceGroup extends CommonBase {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String mSSOResourceGroupName;

	private String mDescription;

	private List mSSOResources;

	public static final String SSORESOURCEGROUP_ATTR_GROUP_NAME = "SSOResourceGroupName";

	public static final String SSORESOURCEGROUP_ATTR_DESCRIPTION = "Description";

	public static final String SSORESOURCEGROUP_ATTR_MEMBERS = "SSOResources";

	/**
	 * SSOResourceGroup Constructor
	 * <p>
	 * Constructs a SSOResourceGroup object from a PDSSOResourceGroup object
	 * 
	 * @param s
	 *            The PDSSOResourceGroup name
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The IBM Tivoli Directory Integrator Connector log
	 * 
	 * @throws PDException
	 */
	public SSOResourceGroup(String s, PDContext context, Log log)
			throws PDException {
		super(context, log);
		Trace.entrymin(this, "SSOResourceGroup Constructor #1");
		PDSSOResourceGroup pdSSOResourceGroup = new PDSSOResourceGroup(
				mPDContext, s, mPDMessages);
		processMsgs(mPDMessages);
		set(pdSSOResourceGroup);
		Trace.exitmin(this, "SSOResourceGroup Constructor #1");
	}

	/**
	 * SSOResourceGroup Constructor
	 * <p>
	 * Constructs a SSOResourceGroup object from an Entry object
	 * 
	 * @param entry
	 *            The Entry object containing data from IBM Tivoli Directory
	 *            Integrator
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The IBM Tivoli Directory Integrator Connecto log
	 * 
	 */
	public SSOResourceGroup(Entry entry, PDContext context, Log log) {
		super(context, log);
		Trace.entrymin(this, "SSOResourceGroup Constructor #2");
		set(entry);
		Trace.exitmin(this, "SSOResourceGroup Constructor #2");
	}

	/**
	 * SSOResourceGroup Constructor
	 * <p>
	 * Constructs a SSOResourceGroup object from search criteria
	 * 
	 * @param searchcriteria
	 *            The Search criteria (usually the name of the Resource Group).
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The IBM Tivoli Directory Integrator Connector log
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public SSOResourceGroup(SearchCriteria searchcriteria, PDContext context,
			Log log) throws TAMConnectorException, PDException {
		super(context, log);
		Trace.entrymin(this, "SSOResourceGroup Constructor #3");
		String s1 = searchcriteria.getFirstCriteriaValue();
		String s = searchcriteria.getFirstCriteriaName();
		int i = searchcriteria.getFirstCriteriaMatch();
		if (!s.equalsIgnoreCase(SSORESOURCEGROUP_ATTR_GROUP_NAME)
				|| i != SearchCriteria.EXCACT)
			throw new TAMConnectorException(TMSMessageGetter
					.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
		PDSSOResourceGroup group = new PDSSOResourceGroup(mPDContext, s1,
				mPDMessages);
		processMsgs(mPDMessages);
		set(group);
		Trace.exitmin(this, "SSOResourceGroup Constructor #3");
	}

	/**
	 * SSOResourceGroup Constructor
	 * <p>
	 * Constructs a SSOResourceGroup object from search criteria. Only the
	 * attribute values from the search criteria are used to initialise the
	 * object. No PDObject is accessed.
	 * 
	 * 
	 * @param context
	 *            The TAM Context
	 * @param searchcriteria
	 *            The Search criteria (usually the name of the Resource Group).
	 * @param log
	 *            The IBM Tivoli Directory Integrator Connector log
	 * 
	 * @throws TAMConnectorException
	 */
	public SSOResourceGroup(PDContext context, SearchCriteria searchcriteria,
			Log log) throws TAMConnectorException {
		super(context, log);
		Trace.entrymin(this, "SSOResourceGroup Constructor #4");
		String s1 = searchcriteria.getFirstCriteriaValue();
		String s = searchcriteria.getFirstCriteriaName();
		int i = searchcriteria.getFirstCriteriaMatch();
		if (!s.equalsIgnoreCase(SSORESOURCEGROUP_ATTR_GROUP_NAME)
				|| i != SearchCriteria.EXCACT)
			throw new TAMConnectorException(TMSMessageGetter
					.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
		if (s1 == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE,
					SSORESOURCEGROUP_ATTR_GROUP_NAME));
		}
		mSSOResourceGroupName = s1;
		Trace.exitmin(this, "SSOResourceGroup Constructor #4");
	}

	private void set(PDSSOResourceGroup pdGroup) throws PDException {
		Trace.entrymin(this, "SSOResourceGroup.set");
		mSSOResourceGroupName = pdGroup.getId();
		mDescription = pdGroup.getDescription();
		// set the Resources
		mSSOResources = new ArrayList(pdGroup.getSSOResources());
		Trace.exitmin(this, "SSOResourceGroup.set");
	}

	private void reset(PDSSOResourceGroup pdGroup) throws PDException {
		Trace.entrymin(this, "SSOResourceGroup.reset");
		mSSOResourceGroupName = pdGroup.getId();
		if (mDescription == null) {
			mDescription = pdGroup.getDescription();
		}
		// set the Resources
		if (mSSOResources == null) {
			mSSOResources = new ArrayList(pdGroup.getSSOResources());
		}
		Trace.exitmin(this, "SSOResourceGroup.reset");
	}

	public void set(Entry entry) {
		Trace.entrymin(this, "SSOResourceGroup.set");
		String rgName = getStringEntryAttributeValue(entry,
				SSORESOURCEGROUP_ATTR_GROUP_NAME);
		if (rgName != null && rgName.length() > 0)
			mSSOResourceGroupName = rgName;
		mDescription = getStringEntryAttributeValue(entry,
				SSORESOURCEGROUP_ATTR_DESCRIPTION);
		// build a list of SSO resources in this group
		Attribute attribute = entry
				.getAttribute(SSOResourceGroup.SSORESOURCEGROUP_ATTR_MEMBERS);
		if (attribute != null) {
			mSSOResources = new ArrayList(attribute.getValuesVector());
			// purge empy values from the list
			for (int i = mSSOResources.size() - 1; i >= 0; i--) {
				String r = (String) mSSOResources.get(i);
				if (r == null || r.length() == 0)
					mSSOResources.remove(i);
			}
		} else
			mSSOResources = new ArrayList(0);
		debug("The number of SSO resources to add to the SSO resource group is "
				+ mSSOResources.size());
		Trace.exitmin(this, "SSOResourceGroup.set");
	}

	/**
	 * Add an SSO Resource Group in TAM
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public void put() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "SSOResourceGroup.put");
		PDSSOResourceGroup.createSSOResourceGroup(mPDContext,
				mSSOResourceGroupName, mDescription, mPDMessages);
		processMsgs(mPDMessages);
		if (mSSOResources != null && mSSOResources.size() > 0) {
			for (int i = 0; i < mSSOResources.size(); i++) {
				String element = mSSOResources.get(i).toString();
				if (element != null && element.length() > 0) {
					PDSSOResourceGroup.addSSOResource(mPDContext,
							mSSOResourceGroupName, element, mPDMessages);
					processMsgs(mPDMessages);
				}
			}
		}
		Trace.exitmin(this, "SSOResourceGroup.put");
	}

	/**
	 * Modify an SSO Resource Group in TAM
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public void modify() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "SSOResourceGroup.modify");
		// do the deletes first
		PDSSOResourceGroup group = new PDSSOResourceGroup(mPDContext,
				mSSOResourceGroupName, mPDMessages);
		List resources = group.getSSOResources();
		// update the internal values here as they may not have been initialised
		// from the PDObject
		reset(group);
		if (resources != null && resources.size() > 0) {
			for (int i = 0; i < resources.size(); i++) {
				String resourceName = (String) resources.get(i);
				if ((resourceName != null) && (resourceName.length() > 0)) {
					PDSSOResourceGroup.removeSSOResource(mPDContext,
							mSSOResourceGroupName, resourceName, mPDMessages);
					processMsgs(mPDMessages);
				}
			}
		}
		// now do the adds
		if (mSSOResources != null) {
			for (int i = 0; i < mSSOResources.size(); i++) {
				String resourceName = (String) mSSOResources.get(i).toString();
				if ((resourceName != null) && (resourceName.length() > 0)) {
					PDSSOResourceGroup.addSSOResource(mPDContext,
							mSSOResourceGroupName, resourceName, mPDMessages);
					processMsgs(mPDMessages);
				}
			}
		}
		Trace.exitmin(this, "SSOResourceGroup.modify");
	}

	/**
	 * Deletes an SSO Resource Group from TAM
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public void delete() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "SSOResourceGroup.delete");
		if (mSSOResourceGroupName == null)
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE,
					SSORESOURCEGROUP_ATTR_GROUP_NAME));
		PDSSOResourceGroup.deleteSSOResourceGroup(mPDContext,
				mSSOResourceGroupName, mPDMessages);
		processMsgs(mPDMessages);
		Trace.exitmin(this, "SSOResourceGroup.delete");
	}

	/**
	 * Return an Entry object with the SSO Resource Group attributes
	 * 
	 * @return Entry
	 */
	public Entry getAttributes() {
		Entry entry = new Entry();
		createAndAddEntryAttribute(entry, SSORESOURCEGROUP_ATTR_GROUP_NAME,
				mSSOResourceGroupName);
		createAndAddEntryAttribute(entry, SSORESOURCEGROUP_ATTR_DESCRIPTION,
				mDescription);
		Attribute attribute = entry.newAttribute(SSORESOURCEGROUP_ATTR_MEMBERS);
		if (mSSOResources != null) {
			int i = mSSOResources.size();
			for (int j = 0; j < i; j++)
				attribute.addValue(mSSOResources.get(j).toString());
		}
		return entry;
	}

	/**
	 * Return a Vector of Schema details for IBM Tivoli Directory Integrator
	 * 
	 * @return Vector
	 */
	public static Vector schema() {
		Vector vector = new Vector();
		addSchemaEntry(vector, SSORESOURCEGROUP_ATTR_GROUP_NAME, QSS_STRING,
				Integer.valueOf(256));
		addSchemaEntry(vector, SSORESOURCEGROUP_ATTR_DESCRIPTION, QSS_STRING,
				Integer.valueOf(256));
		addSchemaEntry(vector, SSORESOURCEGROUP_ATTR_MEMBERS, QSS_STRING,
				Integer.valueOf(256));
		return vector;
	}

	/**
	 * Return a List (ArrayList) of SSO Resource Groups
	 * 
	 * @param context
	 *            The TAM Context
	 * 
	 * @return List (ArrayList)
	 * 
	 * @throws PDException
	 */
	public static List list(PDContext context) throws PDException {
		PDMessages msgs = new PDMessages();
		return PDSSOResourceGroup.listSSOResourceGroups(context, msgs);
	}
}
