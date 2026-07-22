/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.config.bind.SolutionBinding;

/**
 * <p>
 * Java class for StartCI complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="StartCI">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="logListener" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}LogListener" minOccurs="0"/>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}solution" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="configRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="keepAlive" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="password" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="runName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StartCI", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = { "logListener", "solution" })
@XmlRootElement(name = "startCI", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class StartCI {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	protected LogListener logListener;
	protected SolutionBinding solution;
	@XmlAttribute
	@XmlSchemaType(name = "anyURI")
	protected String configRef;
	@XmlAttribute
	protected Boolean keepAlive;
	@XmlAttribute
	protected String password;
	@XmlAttribute
	protected String runName;

	/**
	 * Gets the value of the solution property.
	 * 
	 * @return possible object is {@link SolutionBinding }
	 * 
	 */
	public SolutionBinding getSolution() {
		return solution;
	}

	/**
	 * Sets the value of the solution property.
	 * 
	 * @param value
	 *            allowed object is {@link SolutionBinding }
	 * 
	 */
	public void setSolution(SolutionBinding value) {
		this.solution = value;
	}

	/**
	 * Gets the value of the logListener property.
	 * 
	 * @return
	 * 
	 */
	public LogListener getLogListener() {
		return logListener;
	}

	/**
	 * Sets the value of the logListener property.
	 * 
	 * @param value
	 */
	public void setLogListener(LogListener value) {
		this.logListener = value;
	}

	/**
	 * Gets the value of the configRef property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getConfigRef() {
		return configRef;
	}

	/**
	 * Sets the value of the configRef property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setConfigRef(String value) {
		this.configRef = value;
	}

	/**
	 * Gets the value of the keepAlive property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isKeepAlive() {
		if (keepAlive == null) {
			return false;
		} else {
			return keepAlive;
		}
	}

	/**
	 * Sets the value of the keepAlive property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setKeepAlive(Boolean value) {
		this.keepAlive = value;
	}

	/**
	 * Gets the value of the password property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the value of the password property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPassword(String value) {
		this.password = value;
	}

	/**
	 * Gets the value of the runName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getRunName() {
		return runName;
	}

	/**
	 * Sets the value of the runName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setRunName(String value) {
		this.runName = value;
	}

}
