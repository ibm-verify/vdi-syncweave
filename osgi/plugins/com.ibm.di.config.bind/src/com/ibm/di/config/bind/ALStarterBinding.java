/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ALStarterBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ALStarterBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}NamedBinding">
 *       &lt;sequence>
 *         &lt;element name="initParams" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ParametersBinding" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="assemblyLine" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="operation" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="configInstance" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="server" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ALStarterBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = { "initParams" })
@XmlSeeAlso( { ALReviverBinding.class, ALExecutionScheduleBinding.class })
public class ALStarterBinding extends NamedBinding {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 6748468274885742934L;

	protected ParametersBinding initParams;
	@XmlAttribute(required = true)
	protected String assemblyLine;
	@XmlAttribute
	protected String operation;
	@XmlAttribute
	protected String configInstance;
	@XmlAttribute
	protected String server;

	/**
	 * Gets the value of the initParams property.
	 * 
	 * @return possible object is {@link ParametersBinding }
	 * 
	 */
	public ParametersBinding getInitParams() {
		return initParams;
	}

	/**
	 * Sets the value of the initParams property.
	 * 
	 * @param value
	 *            allowed object is {@link ParametersBinding }
	 * 
	 */
	public void setInitParams(ParametersBinding value) {
		this.initParams = value;
	}

	/**
	 * Gets the value of the assemblyLine property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAssemblyLine() {
		return assemblyLine;
	}

	/**
	 * Sets the value of the assemblyLine property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAssemblyLine(String value) {
		this.assemblyLine = value;
	}

	/**
	 * Gets the value of the operation property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getOperation() {
		return operation;
	}

	/**
	 * Sets the value of the operation property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setOperation(String value) {
		this.operation = value;
	}

	/**
	 * Gets the value of the configInstance property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getConfigInstance() {
		return configInstance;
	}

	/**
	 * Sets the value of the configInstance property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setConfigInstance(String value) {
		this.configInstance = value;
	}

	/**
	 * Gets the value of the server property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getServer() {
		return server;
	}

	/**
	 * Sets the value of the server property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setServer(String value) {
		this.server = value;
	}

}
