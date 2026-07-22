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
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/scmp}destination"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "destination" })
@XmlRootElement(name = "data")
public class DestinationData {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(required = true)
	protected Destination destination;

	/**
	 * Gets the value of the destination property.
	 * 
	 * @return possible object is {@link Destination }
	 * 
	 */
	public Destination getDestination() {
		if (destination == null) {
			destination = new Destination();
		}
		return destination;
	}

	/**
	 * Sets the value of the destination property.
	 * 
	 * @param value
	 *            allowed object is {@link Destination }
	 * 
	 */
	public void setDestination(Destination value) {
		this.destination = value;
	}

}
