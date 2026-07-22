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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.ibm.di.config.bind.XMLGregorianCalendarAdapter;

/**
 * <p>
 * Java class for CITombstoneData complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CITombstoneData">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/api}TombstoneData">
 *       &lt;attribute name="configInstanceId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="startedOn" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CITombstoneData", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
@XmlRootElement(name = "ciData", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class CITombstoneData extends TombstoneData {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlAttribute
	protected String configInstanceId;
	@XmlAttribute
	@XmlJavaTypeAdapter(value = XMLGregorianCalendarAdapter.class)
	protected Long startedOn;

	/**
	 * Gets the value of the configInstanceId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getConfigInstanceId() {
		return configInstanceId;
	}

	/**
	 * Sets the value of the configInstanceId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setConfigInstanceId(String value) {
		this.configInstanceId = value;
	}

	/**
	 * Gets the value of the startedOn property.
	 * 
	 * @return the time as Long
	 * 
	 */
	public Long getStartedOn() {
		return startedOn;
	}

	/**
	 * Sets the value of the startedOn property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	public void setStartedOn(Long value) {
		this.startedOn = value;
	}

}
