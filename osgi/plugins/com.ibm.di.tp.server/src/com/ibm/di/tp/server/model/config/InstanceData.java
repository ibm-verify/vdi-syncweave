/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/scmp}touchpoint"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "touchpoint" })
@XmlRootElement(name = "data")
public class InstanceData {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(required = true)
	protected Touchpoint touchpoint;

	/**
	 * Gets the value of the touchpoint property.
	 * 
	 * @return possible object is {@link Touchpoint }
	 * 
	 */
	public Touchpoint getTouchpoint() {
		if (touchpoint == null) {
			touchpoint = new Touchpoint();
		}
		return touchpoint;
	}

	/**
	 * Sets the value of the touchpoint property.
	 * 
	 * @param value
	 *            allowed object is {@link Touchpoint }
	 * 
	 */
	public void setTouchpoint(Touchpoint value) {
		this.touchpoint = value;
	}

}
