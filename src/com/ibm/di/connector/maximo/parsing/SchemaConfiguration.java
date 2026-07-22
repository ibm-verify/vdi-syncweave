/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.parsing;

import java.util.Collections;
import java.util.List;

import com.ibm.di.connector.maximo.core.MxConnConfiguration;
import com.ibm.di.util.StringUtils;

/**
 * Configuration parameters required by {@link Schema schema} objects.
 * 
 * @since 7.1
 * @see Schema
 */
public final class SchemaConfiguration {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private boolean authenticationRequired = false;

	private String maxobjObjectStructure;

	private List<String> maxObjUrlList = Collections.emptyList();

	private String mosName;

	private String password;

	private int timeout;

	private List<String> urlList = Collections.emptyList();

	private String userId;

	private String xsdSufix;
	private String translang;

	private String serviceBase;

	private List<String> xsdUrlList = Collections.emptyList();

	/**
	 * Returns the name of the object structure that exposes the MAXOBJECT and
	 * MAXATTRIBUTE MBOs.
	 * 
	 * @return the name of the object structure that exposes the MAXOBJECT and
	 *         MAXATTRIBUTE MBOs
	 */
	public String getMaxobjObjectStructure() {
		return maxobjObjectStructure;
	}

	/**
	 * Returns a list of URLs to perform query operations on the MAXOBJECT
	 * object structure.
	 * 
	 * @return a list of URLs to perform query operations on the MAXOBJECT
	 *         object structure
	 * @see #setMaxobjUrlList(List)
	 * @since 1.2.0
	 */
	public List<String> getMaxObjUrlList() {
		return maxObjUrlList;
	}

	/**
	 * Returns the Maximo Object Structure's name.
	 * 
	 * @return Maximo Object Structure's name
	 * @see #setMosName(String)
	 */
	public String getMosName() {
		return mosName;
	}

	/**
	 * Returns the user's password.
	 * 
	 * @return user's password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns the timeout.
	 * 
	 * @return timeout in milliseconds
	 * @since 1.4.0
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Returns a list of URLs.
	 * 
	 * @return a list of URLs
	 * @since 1.4.0
	 */
	public List<String> getUrlList() {
		return urlList;
	}

	/**
	 * Returns the user's identification.
	 * 
	 * @return user's identification
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Returns the XSD URL suffix.
	 * 
	 * @return XSD URL suffix
	 * @since 1.4.0
	 */
	public String getXsdSuffix() {
		return xsdSufix;
	}

	/**
	 * Returns a list of XSD URLs.
	 * 
	 * @return a list of XSD URLs
	 * @see #setXsdUrlList(List)
	 * @since 1.2.0
	 */
	public List<String> getXsdUrlList() {
		return xsdUrlList;
	}

	/**
	 * Indicates if the authentication header containing the user's credentials
	 * should be sent.
	 * 
	 * @return <code>true</code> if the authentication header should be sent,
	 *         otherwise <code>false</code>
	 */
	public boolean isAuthenticationRequired() {
		return authenticationRequired;
	}

	/**
	 * Defines if the authentication header containing the user's credentials
	 * should be sent.
	 * 
	 * @param authenticationRequired
	 *            <code>true</code> if the authentication header should be sent,
	 *            otherwise <code>false</code>
	 */
	public void setAuthenticationRequired(final boolean authenticationRequired) {
		this.authenticationRequired = authenticationRequired;
	}

	/**
	 * Defines the name of the object structure that exposes the MAXOBJECT and
	 * MAXATTRIBUTE MBOs.
	 * 
	 * @param maxobjObjectStructure
	 *            the name of the object structure that exposes the MAXOBJECT
	 *            and MAXATTRIBUTE MBOs
	 */
	public void setMaxobjObjectStructure(final String maxobjObjectStructure) {
		this.maxobjObjectStructure = maxobjObjectStructure;
	}

	/**
	 * Defines a list of URLs to perform query operations on the MAXOBJECT
	 * object structure.
	 * 
	 * @param urlList
	 *            list of URLs to perform query operations on the MAXOBJECT
	 *            object structure
	 */
	public void setMaxobjUrlList(final List<String> urlList) {
		this.maxObjUrlList = Collections.unmodifiableList(urlList);
	}

	/**
	 * Defines the Maximo Object Structure's name.
	 * 
	 * @param mosName
	 *            Maximo Object Structure's name
	 * @see SchemaConfiguration#getMosName()
	 */
	public void setMosName(final String mosName) {
		this.mosName = mosName;
	}

	/**
	 * Defines the user's password.
	 * 
	 * @param password
	 *            user's password
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * Defines the timeout.
	 * 
	 * @param timeout
	 *            timeout in milliseconds
	 */
	public void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Defines a list of URLs.
	 * 
	 * @param urlList
	 *            a list of URLs
	 */
	public void setUrlList(final List<String> urlList) {
		this.urlList = Collections.unmodifiableList(urlList);
	}

	/**
	 * Defines the user's identification.
	 * 
	 * @param userId
	 *            user's identification
	 */
	public void setUserId(final String userId) {
		this.userId = userId;
	}

	/**
	 * Defines the XSD URL suffix.
	 * 
	 * @param xsdSuffix
	 *            XSD URL suffix
	 */
	public void setXsdSufix(final String xsdSuffix) {
		this.xsdSufix = xsdSuffix;
	}

	/**
	 * Defines a list of XSD URLs.
	 * 
	 * @param urlList
	 *            a list of XSD URLs
	 */
	public void setXsdUrlList(final List<String> urlList) {
		this.xsdUrlList = Collections.unmodifiableList(urlList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();

		sb.append(SchemaConfiguration.class.getName());
		sb.append('{');

		sb.append("xsdUrlList=");
		sb.append(xsdUrlList).append("; ");

		sb.append("maxObjUrlList=");
		sb.append(maxObjUrlList).append("; ");

		sb.append(MxConnConfiguration.PARAM_MAXOBJECT_OS + "=");
		sb.append(maxobjObjectStructure).append("; ");

		sb.append(MxConnConfiguration.PARAM_OBJECT_STRUCTURE + "=");
		sb.append(mosName).append("; ");

		sb.append(MxConnConfiguration.PARAM_AUTHENTICATION_REQUIRED + "=");
		sb.append(authenticationRequired).append("; ");

		sb.append(MxConnConfiguration.PARAM_USER_ID + "=").append(userId).append("; ");

		sb.append(MxConnConfiguration.PARAM_PASSWORD + "=");
		if (StringUtils.isBlank(password)) {
			sb.append(password);
		} else {
			sb.append("(********)");
		}
		sb.append("; ");

		sb.append(MxConnConfiguration.PARAM_TIMEOUT + "=").append(timeout).append("; ");
		sb.append('}');

		return sb.toString();
	}
	/**
	 * Defines the user defined transaction language.
	 * 
	 * @param transaction Language
	 *            Transaction Language
	 */
	public void setTransactionLang(String transLanguage) {
		this.translang = transLanguage;
		
	}
	
	/**
	 * Returns the user defined transaction language.
	 * 
	 * @return transaction Language
	 */
	public String getTransactionLang() {
		return translang;
	}

	/**
	 * Defines the user defined service base
	 * @param value
	 */
	public void setServiceBase(String value) {
		serviceBase = value;
	}

	/**
	 * Return the user defined service base, default is "/meaweb"
	 * @return
	 */
	public String getServiceBase() {
		return serviceBase;
	}
}
