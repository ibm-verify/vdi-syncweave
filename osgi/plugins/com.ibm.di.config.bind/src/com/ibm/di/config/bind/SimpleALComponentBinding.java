/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for SimpleALComponentBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="SimpleALComponentBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALComponentBinding">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}simple" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="state" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALComponentStateEnum" default="Enabled" />
 *       &lt;attribute name="simulateState" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALComponentStateEnum" default="Enabled" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleALComponentBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = { "simpleConfig" })
@XmlRootElement(name = "simple", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class SimpleALComponentBinding extends ALComponentBinding {

	private static final long serialVersionUID = -1095233225137675510L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElementRef
	protected SimpleComponentBinding simpleConfig;
	@XmlAttribute
	protected ALComponentStateEnum state;
	@XmlAttribute
	protected ALComponentStateEnum simulateState;

	/**
	 * Gets the value of the simple property.
	 * 
	 * @return
	 * 
	 */
	public SimpleComponentBinding getSimpleConfig() {
		return simpleConfig;
	}

	/**
	 * Sets the value of the simple property.
	 * 
	 * @param value
	 */
	public void setSimple(SimpleComponentBinding value) {
		this.simpleConfig = value;
	}

	/**
	 * Gets the value of the state property.
	 * 
	 * @return possible object is {@link ALComponentStateEnum }
	 * 
	 */
	public ALComponentStateEnum getState() {
		if (state == null) {
			return ALComponentStateEnum.ENABLED;
		} else {
			return state;
		}
	}

	/**
	 * Sets the value of the state property.
	 * 
	 * @param value
	 *            allowed object is {@link ALComponentStateEnum }
	 * 
	 */
	public void setState(ALComponentStateEnum value) {
		this.state = value;
	}

	/**
	 * Gets the value of the simulateState property.
	 * 
	 * @return possible object is {@link ALComponentStateEnum }
	 * 
	 */
	public ALComponentStateEnum getSimulateState() {
		if (simulateState == null) {
			return ALComponentStateEnum.ENABLED;
		} else {
			return simulateState;
		}
	}

	/**
	 * Sets the value of the simulateState property.
	 * 
	 * @param value
	 *            allowed object is {@link ALComponentStateEnum }
	 * 
	 */
	public void setSimulateState(ALComponentStateEnum value) {
		this.simulateState = value;
	}

}
