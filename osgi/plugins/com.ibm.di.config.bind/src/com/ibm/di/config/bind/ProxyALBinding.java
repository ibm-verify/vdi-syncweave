/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for ProxyALBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProxyALBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="server" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="configInstance" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="assemblyLine" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="mode" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ProxyALModeEnum" default="Sync" />
 *       &lt;attribute name="debug" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProxyALBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class ProxyALBinding implements Serializable {

	private static final long serialVersionUID = -7413902229795882746L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	
    @XmlAttribute
    protected String server;
    @XmlAttribute
    protected String configInstance;
    @XmlAttribute(required = true)
    protected String assemblyLine;
    @XmlAttribute
    protected ProxyALModeEnum mode;
    @XmlAttribute
    protected Boolean debug;

    /**
     * Gets the value of the server property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServer() {
        return server;
    }

    /**
     * Sets the value of the server property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServer(String value) {
        this.server = value;
    }

    /**
     * Gets the value of the configInstance property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConfigInstance() {
        return configInstance;
    }

    /**
     * Sets the value of the configInstance property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConfigInstance(String value) {
        this.configInstance = value;
    }

    /**
     * Gets the value of the assemblyLine property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssemblyLine() {
        return assemblyLine;
    }

    /**
     * Sets the value of the assemblyLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssemblyLine(String value) {
        this.assemblyLine = value;
    }

    /**
     * Gets the value of the mode property.
     * 
     * @return
     *     possible object is
     *     {@link ProxyALModeEnum }
     *     
     */
    public ProxyALModeEnum getMode() {
        if (mode == null) {
            return ProxyALModeEnum.SYNC;
        } else {
            return mode;
        }
    }

    /**
     * Sets the value of the mode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProxyALModeEnum }
     *     
     */
    public void setMode(ProxyALModeEnum value) {
        this.mode = value;
    }

    /**
     * Gets the value of the debug property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDebug() {
        if (debug == null) {
            return false;
        } else {
            return debug;
        }
    }

    /**
     * Sets the value of the debug property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDebug(Boolean value) {
        this.debug = value;
    }

}
