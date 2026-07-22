/*
 * Copyright contributors to the SyncWeave project
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
 * Java class for CIEvent complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CIEvent">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/api}DIEvent">
 *       &lt;attribute name="ciStart" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="ciGuid" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="tombstoneCreated" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CIEvent", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
@XmlRootElement(name = "ciEvent", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class CIEvent extends DIEvent {

	private static final long serialVersionUID = -7574310822062565712L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(value = XMLGregorianCalendarAdapter.class)
	protected Long ciStart;
	@XmlAttribute(required = true)
	protected String ciGuid;
	@XmlAttribute
	protected Boolean tombstoneCreated;

	/**
	 * Gets the value of the ciStart property.
	 * 
	 * @return possible object is {@link Long }
	 * 
	 */
	public Long getCiStart() {
		return ciStart;
	}

	/**
	 * Sets the value of the ciStart property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	public void setCiStart(Long value) {
		this.ciStart = value;
	}

	/**
	 * Gets the value of the ciGuid property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCiGuid() {
		return ciGuid;
	}

	/**
	 * Sets the value of the ciGuid property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCiGuid(String value) {
		this.ciGuid = value;
	}

	/**
	 * Gets the value of the tombstoneCreated property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isTombstoneCreated() {
		if (tombstoneCreated == null) {
			return false;
		} else {
			return tombstoneCreated;
		}
	}

	/**
	 * Sets the value of the tombstoneCreated property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setTombstoneCreated(Boolean value) {
		this.tombstoneCreated = value;
	}

}
