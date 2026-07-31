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
 * Java class for ComplexALComponentBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ComplexALComponentBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALComponentBinding">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}complex" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="state" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ComplexALComponentStateEnum" default="Enabled" />
 *       &lt;attribute name="initialize" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALComponentInitializeEnum" default="onStartup" />
 *       &lt;attribute name="sandboxRecord" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="sandboxPlayback" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="simulateState" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALComponentSimulateStateEnum" default="Simulated" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComplexALComponentBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = { "complexConfig" })
@XmlRootElement(name = "complex", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class ComplexALComponentBinding extends ALComponentBinding {

	private static final long serialVersionUID = 5911834630606990895L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElementRef
	protected ComplexComponentBinding complexConfig;
	@XmlAttribute
	protected String state;
	@XmlAttribute
	protected ALComponentInitializeEnum initialize;
	@XmlAttribute
	protected Boolean sandboxRecord;
	@XmlAttribute
	protected Boolean sandboxPlayback;
	@XmlAttribute
	protected String simulateState;

	/**
	 * Gets the value of the complexConfig property.
	 * 
	 * @return
	 * 
	 */
	public ComplexComponentBinding getComplexConfig() {
		return complexConfig;
	}

	/**
	 * Sets the value of the complexConfig property.
	 * 
	 * @param value
	 */
	public void setComplex(ComplexComponentBinding value) {
		this.complexConfig = value;
	}

	/**
	 * Gets the value of the state property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getState() {
		if (state == null) {
			return "Enabled";
		} else {
			return state;
		}
	}

	/**
	 * Sets the value of the state property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setState(String value) {
		this.state = value;
	}

	/**
	 * Gets the value of the initialize property.
	 * 
	 * @return possible object is {@link ALComponentInitializeEnum }
	 * 
	 */
	public ALComponentInitializeEnum getInitialize() {
		if (initialize == null) {
			return ALComponentInitializeEnum.ON_STARTUP;
		} else {
			return initialize;
		}
	}

	/**
	 * Sets the value of the initialize property.
	 * 
	 * @param value
	 *            allowed object is {@link ALComponentInitializeEnum }
	 * 
	 */
	public void setInitialize(ALComponentInitializeEnum value) {
		this.initialize = value;
	}

	/**
	 * Gets the value of the sandboxRecord property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isSandboxRecord() {
		if (sandboxRecord == null) {
			return false;
		} else {
			return sandboxRecord;
		}
	}

	/**
	 * Sets the value of the sandboxRecord property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setSandboxRecord(Boolean value) {
		this.sandboxRecord = value;
	}

	/**
	 * Gets the value of the sandboxPlayback property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isSandboxPlayback() {
		if (sandboxPlayback == null) {
			return false;
		} else {
			return sandboxPlayback;
		}
	}

	/**
	 * Sets the value of the sandboxPlayback property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setSandboxPlayback(Boolean value) {
		this.sandboxPlayback = value;
	}

	/**
	 * Gets the value of the simulateState property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSimulateState() {
		if (simulateState == null) {
			return "Simulated";
		} else {
			return simulateState;
		}
	}

	/**
	 * Sets the value of the simulateState property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSimulateState(String value) {
		this.simulateState = value;
	}

}
