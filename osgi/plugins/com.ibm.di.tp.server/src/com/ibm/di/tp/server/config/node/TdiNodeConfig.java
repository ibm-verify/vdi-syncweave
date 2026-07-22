/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.config.node;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.config.security.EncryptedString;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
@XmlType(namespace = Constants.NS_TDI_71_TP)
@XmlAccessorType(XmlAccessType.FIELD)
public class TdiNodeConfig extends NodeConfig {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlAttribute(name = "local", required = false, namespace = Constants.NS_TDI_71_TP)
	private Boolean isLocal = Boolean.FALSE;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String host;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private Integer port = 0;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String user;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String contact;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String location;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String organization;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private EncryptedString password;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String providerHost;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private Integer providerPort;

	/**
	 * @return the isLocal
	 */
	public Boolean isLocal() {
		return isLocal;
	}

	/**
	 * @param isLocal
	 *            the isLocal to set
	 */
	public void setLocal(Boolean isLocal) {
		this.isLocal = isLocal;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the pass
	 */
	public EncryptedString getPassword() {
		if (password == null) {
			password = new EncryptedString();
		}
		return password;
	}

	/**
	 * @param pass
	 *            the pass to set
	 */
	public void setPassword(EncryptedString pass) {
		this.password = pass;
	}

	/**
	 * @return the conatct
	 */
	public String getContact() {
		return contact;
	}

	/**
	 * @param conatct
	 *            the conatct to set
	 */
	public void setConatct(String contact) {
		this.contact = contact;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the organization
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * @param organization
	 *            the organization to set
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * @return the providerPort
	 */
	public Integer getProviderPort() {
		return providerPort;
	}

	/**
	 * @param providerPort
	 *            the port to use for the touchpoint provider
	 */
	public void setProviderPort(Integer providerPort) {
		this.providerPort = providerPort;
	}

	/**
	 * @param providerHost
	 *            the providerHost to set
	 */
	public void setProviderHost(String providerHost) {
		this.providerHost = providerHost;
	}

	/**
	 * @return the providerHost
	 */
	public String getProviderHost() {
		return providerHost;
	}
}
