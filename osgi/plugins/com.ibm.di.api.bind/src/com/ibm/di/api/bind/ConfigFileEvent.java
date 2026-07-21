/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ConfigFileEvent complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ConfigFileEvent">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/api}Event">
 *       &lt;attribute name="eventType" use="required" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}ConfigFileEventTypeEnum" />
 *       &lt;attribute name="configFileId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="userId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConfigFileEvent", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
@XmlRootElement(name = "configFileEvent", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class ConfigFileEvent extends Event {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = -7329242429061981322L;

	@XmlAttribute(required = true)
	protected ConfigFileEventTypeEnum eventType;
	@XmlAttribute(required = true)
	protected String configFileId;
	@XmlAttribute
	protected String userId;

	/**
	 * Gets the value of the eventType property.
	 * 
	 * @return possible object is {@link ConfigFileEventTypeEnum }
	 * 
	 */
	public ConfigFileEventTypeEnum getEventType() {
		return eventType;
	}

	/**
	 * Sets the value of the eventType property.
	 * 
	 * @param value
	 *            allowed object is {@link ConfigFileEventTypeEnum }
	 * 
	 */
	public void setEventType(ConfigFileEventTypeEnum value) {
		this.eventType = value;
	}

	/**
	 * Gets the value of the configFileId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getConfigFileId() {
		return configFileId;
	}

	/**
	 * Sets the value of the configFileId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setConfigFileId(String value) {
		this.configFileId = value;
	}

	/**
	 * Gets the value of the userId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the value of the userId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUserId(String value) {
		this.userId = value;
	}

}
