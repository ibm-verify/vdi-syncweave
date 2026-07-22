/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.ibm.di.config.bind.XMLGregorianCalendarAdapter;

/**
 * Provides information about the Server.
 * 
 * <p>
 * Java class for ServerInfo complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ServerInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="serverId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="serverVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ipAddress" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hostname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="operatingSystem" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="serverBootTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServerInfo", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = { "serverId", "serverVersion",
		"ipAddress", "hostname", "operatingSystem", "serverBootTime" })
@XmlRootElement(name = "serverInfo", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class ServerInfo {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", required = true)
	protected String serverId;
	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", required = true)
	protected String serverVersion;
	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", required = true)
	protected String ipAddress;
	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", required = true)
	protected String hostname;
	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", required = true)
	protected String operatingSystem;
	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", required = true)
	@XmlJavaTypeAdapter(value = XMLGregorianCalendarAdapter.class)
	protected Long serverBootTime;

	/**
	 * Gets the value of the serverId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getServerId() {
		return serverId;
	}

	/**
	 * Sets the value of the serverId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setServerId(String value) {
		this.serverId = value;
	}

	/**
	 * Gets the value of the serverVersion property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getServerVersion() {
		return serverVersion;
	}

	/**
	 * Sets the value of the serverVersion property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setServerVersion(String value) {
		this.serverVersion = value;
	}

	/**
	 * Gets the value of the ipAddress property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Sets the value of the ipAddress property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setIpAddress(String value) {
		this.ipAddress = value;
	}

	/**
	 * Gets the value of the hostname property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * Sets the value of the hostname property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setHostname(String value) {
		this.hostname = value;
	}

	/**
	 * Gets the value of the operatingSystem property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getOperatingSystem() {
		return operatingSystem;
	}

	/**
	 * Sets the value of the operatingSystem property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setOperatingSystem(String value) {
		this.operatingSystem = value;
	}

	/**
	 * Gets the value of the serverBootTime property.
	 * 
	 * @return possible object is {@link Long }
	 * 
	 */
	public Long getServerBootTime() {
		return serverBootTime;
	}

	/**
	 * Sets the value of the serverBootTime property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	public void setServerBootTime(Long value) {
		this.serverBootTime = value;
	}

}
