/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/scmp}request-out"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "requestOut", "requestError" })
@XmlRootElement(name = "destination")
public class Destination {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(name = "request-out", required = true)
	@XmlSchemaType(name = "anyURI")
	protected String requestOut;

	@XmlElement(name = "request-error", required = false)
	@XmlSchemaType(name = "anyURI")
	private String requestError;

	/**
	 * Gets the value of the requestOut property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getRequestOut() {
		return requestOut;
	}

	/**
	 * Sets the value of the requestOut property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setRequestOut(String value) {
		this.requestOut = value;
	}

	/**
	 * @param requestErr
	 *            the requestErr to set
	 */
	public void setRequestError(String requestError) {
		this.requestError = requestError;
	}

	/**
	 * @return the requestErr
	 */
	public String getRequestError() {
		return requestError;
	}

}
