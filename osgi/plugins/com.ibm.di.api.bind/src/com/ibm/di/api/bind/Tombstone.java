/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.ibm.di.config.bind.XMLGregorianCalendarAdapter;

/**
 * <p>
 * Java class for Tombstone complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="Tombstone">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="errorDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/api}tsData"/>
 *       &lt;/sequence>
 *       &lt;attribute name="guid" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="createdOn" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="exitCode" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Tombstone", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = { "errorDescription", "tsData" })
@XmlRootElement(name = "tombstone", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class Tombstone {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected String errorDescription;
	@XmlElementRef
	protected TombstoneData tsData;
	@XmlAttribute(required = true)
	protected String guid;
	@XmlAttribute
	@XmlJavaTypeAdapter(value = XMLGregorianCalendarAdapter.class)
	protected Long createdOn;
	@XmlAttribute
	protected Integer exitCode;

	/**
	 * Gets the value of the errorDescription property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getErrorDescription() {
		return errorDescription;
	}

	/**
	 * Sets the value of the errorDescription property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setErrorDescription(String value) {
		this.errorDescription = value;
	}

	/**
	 * Gets the value of the tsData property.
	 * 
	 * @return possible object is {@link TombstoneData }
	 * 
	 */
	public TombstoneData getData() {
		return tsData;
	}

	/**
	 * Sets the value of the tsData property.
	 * 
	 * @param value
	 *            allowed object is {@link TombstoneData }
	 * 
	 */
	public void setData(TombstoneData value) {
		this.tsData = value;
	}

	/**
	 * Gets the value of the guid property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * Sets the value of the guid property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setGuid(String value) {
		this.guid = value;
	}

	/**
	 * Gets the value of the createdOn property.
	 * 
	 * @return the time as Long
	 * 
	 */
	public Long getCreatedOn() {
		return createdOn;
	}

	/**
	 * Sets the value of the createdOn property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	public void setCreatedOn(Long value) {
		this.createdOn = value;
	}

	/**
	 * Gets the value of the exitCode property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getExitCode() {
		return exitCode;
	}

	/**
	 * Sets the value of the exitCode property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setExitCode(Integer value) {
		this.exitCode = value;
	}

}
