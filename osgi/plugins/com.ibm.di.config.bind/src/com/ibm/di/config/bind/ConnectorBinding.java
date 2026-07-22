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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for ConnectorBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConnectorBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}ComplexComponentBinding">
 *       &lt;sequence>
 *         &lt;element name="modeConfig" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ConnectorModeBinding" minOccurs="0"/>
 *         &lt;element name="deltaConfig" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}DeltaBinding" minOccurs="0"/>
 *         &lt;element name="linkCriteria" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}LinkCriteriaBinding" minOccurs="0"/>
 *         &lt;element name="poolDef" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}PoolDefinitionBinding" minOccurs="0"/>
 *         &lt;element name="poolInst" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}PoolInstanceBinding" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="mode" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ConnectorModeEnum" default="Iterator" />
 *       &lt;attribute name="state" type="{http://www.w3.org/2001/XMLSchema}string" default="Enabled" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConnectorBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "modeConfig",
    "deltaConfig",
    "linkCriteria",
    "poolDef",
    "poolInst",
    "state"
})
@XmlRootElement(name = "connector", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class ConnectorBinding
    extends ComplexComponentBinding implements Serializable
{

	private static final long serialVersionUID = -894100064576955861L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    protected ConnectorModeBinding modeConfig;
    protected DeltaBinding deltaConfig;
    protected LinkCriteriaBinding linkCriteria;
    protected PoolDefinitionBinding poolDef;
    protected PoolInstanceBinding poolInst;
    @XmlAttribute
    protected ConnectorModeEnum mode;
    @XmlAttribute
    protected String state;

    /**
     * Gets the value of the modeConfig property.
     * 
     * @return
     *     possible object is
     *     {@link ConnectorModeBinding }
     *     
     */
    public ConnectorModeBinding getModeConfig() {
        return modeConfig;
    }

    /**
     * Sets the value of the modeConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConnectorModeBinding }
     *     
     */
    public void setModeConfig(ConnectorModeBinding value) {
        this.modeConfig = value;
    }

    /**
     * Gets the value of the deltaConfig property.
     * 
     * @return
     *     possible object is
     *     {@link DeltaBinding }
     *     
     */
    public DeltaBinding getDeltaConfig() {
        return deltaConfig;
    }

    /**
     * Sets the value of the deltaConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeltaBinding }
     *     
     */
    public void setDeltaConfig(DeltaBinding value) {
        this.deltaConfig = value;
    }

    /**
     * Gets the value of the linkCriteria property.
     * 
     * @return
     *     possible object is
     *     {@link LinkCriteriaBinding }
     *     
     */
    public LinkCriteriaBinding getLinkCriteria() {
        return linkCriteria;
    }

    /**
     * Sets the value of the linkCriteria property.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkCriteriaBinding }
     *     
     */
    public void setLinkCriteria(LinkCriteriaBinding value) {
        this.linkCriteria = value;
    }

    /**
     * Gets the value of the poolDef property.
     * 
     * @return
     *     possible object is
     *     {@link PoolDefinitionBinding }
     *     
     */
    public PoolDefinitionBinding getPoolDef() {
        return poolDef;
    }

    /**
     * Sets the value of the poolDef property.
     * 
     * @param value
     *     allowed object is
     *     {@link PoolDefinitionBinding }
     *     
     */
    public void setPoolDef(PoolDefinitionBinding value) {
        this.poolDef = value;
    }

    /**
     * Gets the value of the poolInst property.
     * 
     * @return
     *     possible object is
     *     {@link PoolInstanceBinding }
     *     
     */
    public PoolInstanceBinding getPoolInst() {
        return poolInst;
    }

    /**
     * Sets the value of the poolInst property.
     * 
     * @param value
     *     allowed object is
     *     {@link PoolInstanceBinding }
     *     
     */
    public void setPoolInst(PoolInstanceBinding value) {
        this.poolInst = value;
    }

    /**
     * Gets the value of the mode property.
     * 
     * @return
     *     possible object is
     *     {@link ConnectorModeEnum }
     *     
     */
    public ConnectorModeEnum getMode() {
        if (mode == null) {
            return ConnectorModeEnum.ITERATOR;
        } else {
            return mode;
        }
    }

    /**
     * Sets the value of the mode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConnectorModeEnum }
     *     
     */
    public void setMode(ConnectorModeEnum value) {
        this.mode = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getState() {
    	return this.state;
    }
    
    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setState(String state) {
    	this.state = state;
    }
}
