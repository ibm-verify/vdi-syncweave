/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy.impl;

import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.ibm.di.plugin.pwstore.itim.policy.PasswordPolicyFactory;
import com.ibm.di.plugin.pwstore.itim.policy.PasswordPolicyService;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyConnectionException;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyInitializationException;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceConnection;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceRequest;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceResponse;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceMessage.ServiceOp;
import com.ibm.di.plugin.security.SecurityHelper;

/**
 * <p>
 * The ITIM specific implementation of the PasswordPolicyFactory.
 * </p>
 * <p>
 * This factory is configured by setting the following properties:<br>
 * <br>
 * <b>itimPasswordUrl</b> - The HTTP url of the ITIM password policy servlet,
 * e.g. https://<host>/passwordsynch/synch <br>
 * <b>itimPrincipalName</b> - The user name of an ITIM princpal with authority
 * to perform password validation requests against the policy servlet. <br>
 * <b>itimPrincipalPassword</b> - The password of the ITIM principal. This
 * property value will be decrypted as needed. <br>
 * <b>itimSourceDN</b> - The ITIM service DN that identifies the source of
 * password operations detected by clients of this object family, e.g.
 * erservicename=SpmlRaProxyToCA, o=International Business Machines, ou=IBM,
 * dc=com. <br>
 * </p>
 */
public final class ITIMPasswordPolicyFactoryImpl implements
		PasswordPolicyFactory {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	public static final String PROP_NAME_ITIM_PASSWORD_URL = "itimPasswordUrl";
	public static final String PROP_NAME_ITIM_PRINCIPAL_NAME = "itimPrincipalName";
	public static final String PROP_NAME_ITIM_PRINCIPAL_PASSWORD = "itimPrincipalPassword";
	public static final String PROP_NAME_ITIM_SOURCE_DN = "itimSourceDN";

	private String itimPasswordUrl;
	private String itimPrincipalName;
	private String itimPrincipalPassword;
	private String itimSourceDN;

	/**
	 * 
	 */
	public ITIMPasswordPolicyFactoryImpl() {
		super();
	}

	void checkObjectState() throws IllegalStateException {
		if (itimPasswordUrl == null || itimPrincipalName == null
				|| itimPrincipalPassword == null || itimSourceDN == null) {
			throw new IllegalStateException();
		}

		if (itimPasswordUrl.length() == 0 || itimPrincipalName.length() == 0
				|| itimPrincipalPassword.length() == 0
				|| itimSourceDN.length() == 0) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Configure this factory.
	 * 
	 * @throws PolicyInitializationException
	 *             if required property names are missing or values are not
	 *             valid.
	 */
	public void configure() throws PolicyInitializationException {

		itimPasswordUrl = System
				.getProperty(ITIMPasswordPolicyFactoryImpl.PROP_NAME_ITIM_PASSWORD_URL);
		itimPrincipalName = System
				.getProperty(ITIMPasswordPolicyFactoryImpl.PROP_NAME_ITIM_PRINCIPAL_NAME);
		itimPrincipalPassword = System
				.getProperty(ITIMPasswordPolicyFactoryImpl.PROP_NAME_ITIM_PRINCIPAL_PASSWORD);
		itimSourceDN = System
				.getProperty(ITIMPasswordPolicyFactoryImpl.PROP_NAME_ITIM_SOURCE_DN);
	}

	/**
	 * Factory method for PasswordPolicyService instances.
	 * 
	 * @return new PasswordPolicyService instance.
	 * 
	 * @throws PolicyConnectionException
	 *             if underlying connection cannot be created.
	 * @throws IllegalStateException
	 *             if not configured.
	 */
	public PasswordPolicyService newPasswordPolicyService()
			throws PolicyConnectionException {
		checkObjectState();

		PolicyServiceConnection conn = newPolicyServiceConnection();
		if (conn == null) {
			throw new PolicyConnectionException();
		}

		return newPasswordPolicyService(conn);
	}

	/**
	 * Factory method for PasswordPolicyService instances.
	 * 
	 * @param conn
	 *            The conn to the policy service provider. Stored config is
	 *            ignored if using this overload.
	 * 
	 * @return new PasswordPolicyService instance.
	 * 
	 * @throws IllegalArgumentException
	 *             if conn is null.
	 * @throws IllegalStateException
	 *             if not configured.
	 */
	public PasswordPolicyService newPasswordPolicyService(
			PolicyServiceConnection conn) {
		checkObjectState();

		ITIMPasswordPolicyImpl result = new ITIMPasswordPolicyImpl();
		result.setItimPrincipalName(this.itimPrincipalName);
		result.setItimPrincipalPassword(SecurityHelper
				.getClearText(this.itimPrincipalPassword));
		result.setItimSourceDn(this.itimSourceDN);
		result.setConnection(conn);

		return result;

	}

	/**
	 * Factory method for request objects.
	 * 
	 * @param op
	 *            The opeation type.
	 * 
	 * @return a new request object.
	 * 
	 * @throws IllegalStateException
	 *             if not configured.
	 */
	public PolicyServiceRequest newPolicyServiceRequest(ServiceOp op) {
		if (op == null) {
			throw new IllegalArgumentException();
		}
		ITIMPolicyServiceRequestImpl result = new ITIMPolicyServiceRequestImpl();
		result.setOperation(op);
		result.setPrincipalName(this.itimPrincipalName);
		result.setPrincipalPswd(SecurityHelper
				.getClearText(this.itimPrincipalPassword));
		result.setSourceDn(this.itimSourceDN);

		return result;
	}

	/**
	 * Factory method for response objects.
	 * 
	 * @param req
	 *            The original request.
	 * 
	 * @return new response object instance.
	 * 
	 * @throws IllegalStateException
	 *             if not configured.
	 */
	public PolicyServiceResponse newPolicyServiceResponse(
			PolicyServiceRequest req) {
		if (req == null) {
			throw new IllegalArgumentException();
		}

		ITIMPolicyServiceResponseImpl result = new ITIMPolicyServiceResponseImpl();
		result.setReqMsg(req);

		return result;
	}

	/**
	 * Factory method for connection objects.
	 * 
	 * @return new connection based on stored configuration.
	 * 
	 * @throws PolicyConnectionException
	 * @throws IllegalStateException
	 *             if not configured.
	 */
	public PolicyServiceConnection newPolicyServiceConnection()
			throws PolicyConnectionException {
		checkObjectState();

		try {
			URL url = new URL(this.itimPasswordUrl);
			return newPolicyServiceConnection(url);
		} catch (MalformedURLException x) {
			throw new PolicyConnectionException(x);
		}
	}

	/**
	 * Factory method for connection objects.
	 * 
	 * @param url
	 *            The url
	 * 
	 * @return new connection based on URL. Stored config is ignored if this
	 *         overload is used.
	 * 
	 * @throws PolicyConnectionException
	 * @throws IllegalArgumentException
	 *             if url is null, or protocol is not HTTP.
	 * @throws IllegalStateException
	 *             if not configured.
	 */
	public PolicyServiceConnection newPolicyServiceConnection(URL url)
			throws PolicyConnectionException {
		if (url == null) {
			throw new IllegalArgumentException();
		}

		if ((url.getProtocol().compareToIgnoreCase("http") != 0)
				&& (url.getProtocol().compareToIgnoreCase("https") != 0)) {
			throw new IllegalArgumentException(url.getProtocol());
		}

		checkObjectState();

		if (url.getProtocol().compareToIgnoreCase("https") == 0) {
			HttpsURLConnection
					.setDefaultHostnameVerifier(new HostnameVerifierImpl());
		}

		ITIMPolicyServiceConnectionImpl result = null;
		result = new ITIMPolicyServiceConnectionImpl(url);

		return result;
	}

	static SAXParser newSAXParser() throws SAXException,
			ParserConfigurationException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);

		return factory.newSAXParser();
	}

}
