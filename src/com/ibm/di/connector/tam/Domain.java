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
import com.tivoli.pd.jadmin.PDDomain;
import com.tivoli.pd.jutil.PDContext;
import com.tivoli.pd.jutil.PDException;
import com.tivoli.pd.jutil.PDMessages;

// Note: CSAR "public domain" keyword hits in this file are false positives.
// All occurrences refer to LDAP/TAM administrative domain names (PDDomain),
// not public domain dedications or license statements.
public class Domain extends CommonBase {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String mDomainName;

	private String mDescription;

	private String mAdminLogin;

	private String mAdminPassword;

	public static final String DOMAIN_ATTR_DOMAIN_NAME = "DomainName";

	private static final String DOMAIN_ATTR_DESCRIPTION = "Description";

	/**
	 * Domain Constructor
	 * 
	 * @param s
	 *            The Domain name
	 * @param context
	 *            The TAM Context
	 * @param adminLogin
	 *            The TAM Admin User
	 * @param adminPassword
	 *            The TAM Admin Password
	 * @param log
	 *            The IBM Tivoli Directory Integrator Connector log
	 * 
	 * @throws PDException
	 */
	public Domain(String s, PDContext context, String adminLogin,
			String adminPassword, Log log) throws PDException {
		super(context, log);
		Trace.entrymin(this, "Domain Constructor #1");
		mAdminLogin = adminLogin;
		mAdminPassword = adminPassword;
		PDDomain pdDomain = new PDDomain(mPDContext, s, mPDMessages);
		processMsgs(mPDMessages);
		set(pdDomain);
		Trace.exitmin(this, "Domain Constructor #1");
	}

	/**
	 * Domain Constructor
	 * 
	 * @param entry
	 *            The Entry object containing IBM Tivoli Directory Integrator
	 *            Data
	 * @param context
	 *            The TAM Context
	 * @param adminLogin
	 *            The TAM Admin User
	 * @param adminPassword
	 *            The TAM Admin Password
	 * @param log
	 *            The IBM Tivoli Directory Integrator Connector Log
	 */
	public Domain(Entry entry, PDContext context, String adminLogin,
			String adminPassword, Log log) {
		super(context, log);
		Trace.entrymin(this, "Domain Constructor #2", log);
		set(entry);
		mAdminLogin = adminLogin;
		mAdminPassword = adminPassword;
		Trace.exitmin(this, "Domain Constructor #2");
	}

	/**
	 * Domain Constructor
	 * 
	 * @param searchcriteria
	 *            The specified Domain Name
	 * @param context
	 *            The TAM Context
	 * @param adminLogin
	 *            The TAM Admin User
	 * @param adminPassword
	 *            The TAM Admin Password
	 * @param log
	 *            The IBM Tivoli Directory Integrator Connector log
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public Domain(SearchCriteria searchcriteria, PDContext context,
			String adminLogin, String adminPassword, Log log)
			throws TAMConnectorException, PDException {
		super(context, log);
		Trace.entrymin(this, "Domain Constructor #3", log);
		String s1 = searchcriteria.getFirstCriteriaValue();
		String s = searchcriteria.getFirstCriteriaName();
		int i = searchcriteria.getFirstCriteriaMatch();
		PDMessages pdMsgs = new PDMessages();
		if (!s.equalsIgnoreCase(DOMAIN_ATTR_DOMAIN_NAME)
				|| i != SearchCriteria.EXCACT)
			throw new TAMConnectorException(TMSMessageGetter
					.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
		if (s1 == null || s1.length() == 0)
			throw new TAMConnectorException(TMSMessageGetter
					.getMessage(TMSMsgId.MISSING_ENTRY_TYPE));
		PDDomain pdDomain = new PDDomain(mPDContext, s1, pdMsgs);
		processMsgs(pdMsgs);
		set(pdDomain);
		mAdminLogin = adminLogin;
		mAdminPassword = adminPassword;
		Trace.exitmin(this, "Domain Constructor #3");
	}

	/**
	 * Domain Constructor.
	 * 
	 * @param context
	 *            The TAM Context
	 * @param searchcriteria
	 *            The specified Domain Name
	 * @param log
	 *            The IBM Tivoli Directory Integrator Connector log
	 * 
	 * @throws TAMConnectorException
	 */
	public Domain(PDContext context, SearchCriteria searchcriteria, Log log)
			throws TAMConnectorException {
		super(context, log);
		Trace.entrymin(this, "Domain Constructor #4", log);
		String s1 = searchcriteria.getFirstCriteriaValue();
		String s = searchcriteria.getFirstCriteriaName();
		int i = searchcriteria.getFirstCriteriaMatch();
		if (!s.equalsIgnoreCase(DOMAIN_ATTR_DOMAIN_NAME)
				|| i != SearchCriteria.EXCACT)
			throw new TAMConnectorException(TMSMessageGetter
					.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
		if (s1 == null || s1.length() == 0)
			throw new TAMConnectorException(TMSMessageGetter
					.getMessage(TMSMsgId.MISSING_ENTRY_TYPE));
		mDomainName = s1;
		Trace.exitmin(this, "Domain Constructor #4");
	}

	private void set(PDDomain pdDomain) throws PDException {
		mDomainName = pdDomain.getId();
		mDescription = pdDomain.getDescription();
	}

	/**
	 * Set the object attributes from the IBM Tivoli Directory Integrator entry
	 * object
	 * 
	 * @param entry
	 *            The IBM Tivoli Directory Integrator Entry object
	 */
	public void set(Entry entry) {
		String dName = getStringEntryAttributeValue(entry,
				DOMAIN_ATTR_DOMAIN_NAME);
		if (dName != null && dName.length() > 0)
			mDomainName = dName;
		mDescription = getStringEntryAttributeValue(entry,
				DOMAIN_ATTR_DESCRIPTION);
	}

	/**
	 * Creates a Domain in TAM
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public void put() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "Domain.put");
		PDMessages pdMsgs = new PDMessages();
		if (mDomainName == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, DOMAIN_ATTR_DOMAIN_NAME));
		}
		debug(DOMAIN_ATTR_DOMAIN_NAME + " is: " + mDomainName);
		logmsg(TMSMessageGetter.getMessage(TMSMsgId.DOMAIN_CREATE, mDomainName));
		PDDomain.createDomain(mPDContext, mDomainName, mDescription,
				mAdminLogin, mAdminPassword.toCharArray(), pdMsgs);
		processMsgs(pdMsgs);
		Trace.exitmin(this, "Domain.put");
	}

	/**
	 * Modifies a TAM Domain Description
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public void modify() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "Domain.modify");
		PDMessages pdMsgs = new PDMessages();
		PDDomain pdDomain = new PDDomain(mPDContext, mDomainName, pdMsgs);
		processMsgs(pdMsgs);
		// only update the domain description
		if (mDescription != null && mDescription.length() > 0)
			pdDomain.setDescription(mPDContext, mDescription, pdMsgs);
		processMsgs(pdMsgs);
		Trace.exitmin(this, "Domain.modify");
	}

	/**
	 * Deletes a TAM Domain
	 * 
	 * @param deleteReg
	 *            <code>true</code> to delete the domain from TAM and the
	 *            registry <code>false</code> to delete from TAM only and
	 *            leave in the registry
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public void delete(boolean deleteReg) throws TAMConnectorException,
			PDException {
		Trace.entrymin(this, "Domain.delete");
		PDMessages pdMsgs = new PDMessages();
		logmsg(TMSMessageGetter.getMessage(TMSMsgId.DOMAIN_DELETE, mDomainName));
		PDDomain.deleteDomain(mPDContext, mDomainName, deleteReg, pdMsgs);
		processMsgs(pdMsgs);
		Trace.exitmin(this, "Domain.delete");
	}

	/**
	 * Returns the Domain in the form of an Entry object
	 * 
	 * @return Entry
	 */
	public Entry getAttributes() {
		Trace.entrymin(this, "Domain.getAttributes");
		Entry entry = new Entry();
		createAndAddEntryAttribute(entry, DOMAIN_ATTR_DOMAIN_NAME, mDomainName);
		createAndAddEntryAttribute(entry, DOMAIN_ATTR_DESCRIPTION, mDescription);
		Trace.exitmin(this, "Domain.getAttributes");
		return entry;
	}

	/**
	 * Return the IBM Tivoli Directory Integrator schema in the form of a Vector
	 * 
	 * @return Vector
	 */
	public static Vector schema() {
		Vector vector = new Vector();
		addSchemaEntry(vector, DOMAIN_ATTR_DOMAIN_NAME, QSS_STRING, Integer
				.valueOf(256));
		addSchemaEntry(vector, DOMAIN_ATTR_DESCRIPTION, QSS_STRING, Integer
				.valueOf(256));
		return vector;
	}

	/**
	 * Return a List (ArrayList) of Domains
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
		return PDDomain.listDomains(context, msgs);
	}
}
