/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.tam;

import java.util.List;
import java.util.Vector;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;
import com.tivoli.pd.jadmin.PDSSOResource;
import com.tivoli.pd.jutil.PDContext;
import com.tivoli.pd.jutil.PDException;
import com.tivoli.pd.jutil.PDMessages;

/**
 * SSO Resource class contains the functionality to find, add and delete
 * Resources from TAM using the TAM Connector for IBM Tivoli Directory
 * Integrator.
 */
public class SSOResource extends CommonBase {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String mResourceName;

	private String mDescription;

	public static final String SSORESOURCE_ATTR_NAME = "SSOResourceName";

	public static final String SSORESOURCE_ATTR_DESCRIPTION = "Description";

	/**
	 * SSO Resource Contrustor
	 * 
	 * @param s
	 *            The TAM Resource name
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The IBM Tivoli Directory Integrator Connector Log
	 * 
	 * @throws PDException
	 */
	public SSOResource(String s, PDContext context, Log log) throws PDException {
		super(context, log);
		Trace.entrymin(this, "SSOResource Constructor #1");
		PDSSOResource pdSSOResource = new PDSSOResource(mPDContext, s,
				mPDMessages);
		processMsgs(mPDMessages);
		set(pdSSOResource);
		Trace.exitmin(this, "SSOResource Constructor #1");
	}

	/**
	 * SSO Resource Constructor
	 * 
	 * @param entry
	 *            The Entry data from IBM Tivoli Directory Integrator
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The TAM Connector log
	 */
	public SSOResource(Entry entry, PDContext context, Log log) {
		super(context, log);
		Trace.entrymin(this, "SSOResource Constructor #2");
		mResourceName = getStringEntryAttributeValue(entry,
				SSORESOURCE_ATTR_NAME);
		mDescription = getStringEntryAttributeValue(entry,
				SSORESOURCE_ATTR_DESCRIPTION);
		Trace.exitmin(this, "SSOResource Constructor #2");
	}

	/**
	 * The SSO Resource Constructor
	 * <p>
	 * Constructs a resource from the search criteria
	 * 
	 * @param searchcriteria
	 *            The search criteria, usually the resource name
	 * @param context
	 *            The TAM Contact
	 * @param log
	 *            The TAM Connector log
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public SSOResource(SearchCriteria searchcriteria, PDContext context, Log log)
			throws TAMConnectorException, PDException {
		super(context, log);
		Trace.entrymin(this, "SSOResource Constructor #3");
		String s1 = searchcriteria.getFirstCriteriaValue();
		String s = searchcriteria.getFirstCriteriaName();
		int i = searchcriteria.getFirstCriteriaMatch();
		if (!s.equalsIgnoreCase(SSORESOURCE_ATTR_NAME)
				|| i != SearchCriteria.EXCACT)
			throw new TAMConnectorException(TMSMessageGetter
					.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
		PDSSOResource pdResource = new PDSSOResource(mPDContext, s1,
				mPDMessages);
		processMsgs(mPDMessages);
		set(pdResource);
		Trace.exitmin(this, "SSOResource Constructor #3");
	}

	/**
	 * The SSO Resource Constructor
	 * <p>
	 * Constructs a resource from the search criteria w/o accessing the
	 * underlying PDOBject. Initialisation is done from the searchcriteria only.
	 * 
	 * @param searchcriteria
	 *            The search criteria, usually the resource name
	 * @param context
	 *            The TAM Contact
	 * @param log
	 *            The TAM Connector log
	 * 
	 * @throws TAMConnectorException
	 */
	public SSOResource(PDContext context, SearchCriteria searchcriteria, Log log)
			throws TAMConnectorException {
		super(context, log);
		Trace.entrymin(this, "SSOResource Constructor #4");
		String s1 = searchcriteria.getFirstCriteriaValue();
		String s = searchcriteria.getFirstCriteriaName();
		int i = searchcriteria.getFirstCriteriaMatch();
		if (!s.equalsIgnoreCase(SSORESOURCE_ATTR_NAME)
				|| i != SearchCriteria.EXCACT)
			throw new TAMConnectorException(TMSMessageGetter
					.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
		if (s1 == null)
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, SSORESOURCE_ATTR_NAME));
		mResourceName = s1;
		Trace.exitmin(this, "SSOResource Constructor #4");
	}

	private void set(PDSSOResource pdSSOResource) throws PDException {
		mResourceName = pdSSOResource.getId();
		mDescription = pdSSOResource.getDescription();
	}

	/**
	 * Adds a Resource
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public void put() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "SSOResource.put");
		if (mResourceName == null)
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, SSORESOURCE_ATTR_NAME));
		debug(SSORESOURCE_ATTR_NAME + " is: " + mResourceName);
		PDSSOResource.createSSOResource(mPDContext, mResourceName,
				mDescription, mPDMessages);
		processMsgs(mPDMessages);
		Trace.exitmin(this, "SSOResource.put");
	}

	/**
	 * Deletes a resource
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public void delete() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "SSOResource.delete");
		if (mResourceName == null)
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, SSORESOURCE_ATTR_NAME));
		PDSSOResource.deleteSSOResource(mPDContext, mResourceName, mPDMessages);
		processMsgs(mPDMessages);
		Trace.exitmin(this, "SSOResource.delete");
	}

	/**
	 * Return the attributes in the form of an Entry object
	 * 
	 * @return Entry
	 */
	public Entry getAttributes() {
		Entry entry = new Entry();
		createAndAddEntryAttribute(entry, SSORESOURCE_ATTR_NAME, mResourceName);
		createAndAddEntryAttribute(entry, SSORESOURCE_ATTR_DESCRIPTION,
				mDescription);
		return entry;
	}

	/**
	 * Return the IBM Tivoli Directory Integrator Schema in the form of a Vector
	 * 
	 * @return Vector
	 */
	public static Vector schema() {
		Vector vector = new Vector();
		addSchemaEntry(vector, SSORESOURCE_ATTR_NAME, QSS_STRING, Integer
				.valueOf(256));
		addSchemaEntry(vector, SSORESOURCE_ATTR_DESCRIPTION, QSS_STRING,
				Integer.valueOf(256));
		return vector;
	}

	/**
	 * Return a List (ArrayList) of SSO Resources
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
		return PDSSOResource.listSSOResources(pdContext, msgs);
	}
}
