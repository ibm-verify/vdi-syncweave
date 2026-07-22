/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for SolutionContextBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="SolutionContextBinding">
 *   &lt;sequence>
 *     &lt;element name="interface" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}SolutionInterfaceBinding" minOccurs="0"/>
 *     &lt;element name="log" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}LogBinding" minOccurs="0"/>
 *     &lt;element name="libraries" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}SolutionLibraryBinding" minOccurs="0"/>
 *     &lt;element name="tombstone" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ParametersBinding" minOccurs="0"/>
 *     &lt;element name="systemStore" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ParametersBinding" minOccurs="0"/>
 *     &lt;element name="instance" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}SolutionInstanceBinding" minOccurs="0"/>
 *   &lt;/sequence>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SolutionContextBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = { "_interface",
		"log", "libraries", "tombstone", "systemStore", "instance" })
public class SolutionContextBinding implements Serializable {

	private static final long serialVersionUID = 5081743993614677262L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(name = "interface")
	protected SolutionInterfaceBinding _interface;
	protected LogBinding log;
	protected SolutionLibraryBinding libraries;
	protected ParametersBinding tombstone;
	protected ParametersBinding systemStore;
	protected SolutionInstanceBinding instance;

	/**
	 * Gets the value of the interface property.
	 * 
	 * @return possible object is {@link SolutionInterfaceBinding }
	 * 
	 */
	public SolutionInterfaceBinding getInterface() {
		return _interface;
	}

	/**
	 * Sets the value of the interface property.
	 * 
	 * @param value
	 *            allowed object is {@link SolutionInterfaceBinding }
	 * 
	 */
	public void setInterface(SolutionInterfaceBinding value) {
		this._interface = value;
	}

	/**
	 * Gets the value of the log property.
	 * 
	 * @return possible object is {@link LogBinding }
	 * 
	 */
	public LogBinding getLog() {
		return log;
	}

	/**
	 * Sets the value of the log property.
	 * 
	 * @param value
	 *            allowed object is {@link LogBinding }
	 * 
	 */
	public void setLog(LogBinding value) {
		this.log = value;
	}

	/**
	 * Gets the value of the libraries property.
	 * 
	 * @return possible object is {@link SolutionLibraryBinding }
	 * 
	 */
	public SolutionLibraryBinding getLibraries() {
		return libraries;
	}

	/**
	 * Sets the value of the libraries property.
	 * 
	 * @param value
	 *            allowed object is {@link SolutionLibraryBinding }
	 * 
	 */
	public void setLibraries(SolutionLibraryBinding value) {
		this.libraries = value;
	}

	/**
	 * Gets the value of the tombstone property.
	 * 
	 * @return possible object is {@link ParametersBinding }
	 * 
	 */
	public ParametersBinding getTombstone() {
		return tombstone;
	}

	/**
	 * Sets the value of the tombstone property.
	 * 
	 * @param value
	 *            allowed object is {@link ParametersBinding }
	 * 
	 */
	public void setTombstone(ParametersBinding value) {
		this.tombstone = value;
	}

	/**
	 * Gets the value of the systemStore property.
	 * 
	 * @return possible object is {@link ParametersBinding }
	 * 
	 */
	public ParametersBinding getSystemStore() {
		return systemStore;
	}

	/**
	 * Sets the value of the systemStore property.
	 * 
	 * @param value
	 *            allowed object is {@link ParametersBinding }
	 * 
	 */
	public void setSystemStore(ParametersBinding value) {
		this.systemStore = value;
	}

	/**
	 * Gets the value of the instance property.
	 * 
	 * @return possible object is {@link SolutionInstanceBinding }
	 * 
	 */
	public SolutionInstanceBinding getInstance() {
		return instance;
	}

	/**
	 * Sets the value of the instance property.
	 * 
	 * @param value
	 *            allowed object is {@link SolutionInstanceBinding }
	 * 
	 */
	public void setInstance(SolutionInstanceBinding value) {
		this.instance = value;
	}

}
