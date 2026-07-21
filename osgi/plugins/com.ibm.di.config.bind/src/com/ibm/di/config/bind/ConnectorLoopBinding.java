/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ConnectorLoopBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ConnectorLoopBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}connector"/>
 *       &lt;/sequence>
 *       &lt;attribute name="initialize" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ConnectorLoopInitializeEnum" default="onEveryUse" />
 *       &lt;attribute name="selectEntries" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ConnectorLoopSelectEntriesEnum" default="onInitialize" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConnectorLoopBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = { "connector" })
public class ConnectorLoopBinding implements Serializable {

	private static final long serialVersionUID = 6942386738396607039L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(required = true)
	protected ConnectorBinding connector;
	@XmlAttribute
	protected ConnectorLoopInitializeEnum initialize;
	@XmlAttribute
	protected ConnectorLoopSelectEntriesEnum selectEntries;

	/**
	 * Gets the value of the connector property.
	 * 
	 * @return possible object is {@link ConnectorBinding }
	 * 
	 */
	public ConnectorBinding getConnector() {
		return connector;
	}

	/**
	 * Sets the value of the connector property.
	 * 
	 * @param value
	 *            allowed object is {@link ConnectorBinding }
	 * 
	 */
	public void setConnector(ConnectorBinding value) {
		this.connector = value;
	}

	/**
	 * Gets the value of the initialize property.
	 * 
	 * @return possible object is {@link ConnectorLoopInitializeEnum }
	 * 
	 */
	public ConnectorLoopInitializeEnum getInitialize() {
		if (initialize == null) {
			return ConnectorLoopInitializeEnum.ON_EVERY_USE;
		} else {
			return initialize;
		}
	}

	/**
	 * Sets the value of the initialize property.
	 * 
	 * @param value
	 *            allowed object is {@link ConnectorLoopInitializeEnum }
	 * 
	 */
	public void setInitialize(ConnectorLoopInitializeEnum value) {
		this.initialize = value;
	}

	/**
	 * Gets the value of the selectEntries property.
	 * 
	 * @return possible object is {@link ConnectorLoopSelectEntriesEnum }
	 * 
	 */
	public ConnectorLoopSelectEntriesEnum getSelectEntries() {
		if (selectEntries == null) {
			return ConnectorLoopSelectEntriesEnum.ON_INITIALIZE;
		} else {
			return selectEntries;
		}
	}

	/**
	 * Sets the value of the selectEntries property.
	 * 
	 * @param value
	 *            allowed object is {@link ConnectorLoopSelectEntriesEnum }
	 * 
	 */
	public void setSelectEntries(ConnectorLoopSelectEntriesEnum value) {
		this.selectEntries = value;
	}

}
