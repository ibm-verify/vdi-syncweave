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
 * <p>Java class for PoolDefinitionBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PoolDefinitionBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="minSize" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="maxSize" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="purgeInterval" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="initializeAttempts" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="initializeSleepInterval" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PoolDefinitionBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class PoolDefinitionBinding implements Serializable {

	private static final long serialVersionUID = -4391783393177425741L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    @XmlAttribute
    protected Boolean enabled;
    @XmlAttribute
    protected Integer minSize;
    @XmlAttribute
    protected Integer maxSize;
    @XmlAttribute
    protected Integer purgeInterval;
    @XmlAttribute
    protected Integer initializeAttempts;
    @XmlAttribute
    protected Integer initializeSleepInterval;

    /**
     * Gets the value of the enabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEnabled() {
        if (enabled == null) {
            return false;
        } else {
            return enabled;
        }
    }

    /**
     * Sets the value of the enabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnabled(Boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the minSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMinSize() {
        return minSize;
    }

    /**
     * Sets the value of the minSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMinSize(Integer value) {
        this.minSize = value;
    }

    /**
     * Gets the value of the maxSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the value of the maxSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxSize(Integer value) {
        this.maxSize = value;
    }

    /**
     * Gets the value of the purgeInterval property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPurgeInterval() {
        return purgeInterval;
    }

    /**
     * Sets the value of the purgeInterval property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPurgeInterval(Integer value) {
        this.purgeInterval = value;
    }

    /**
     * Gets the value of the initializeAttempts property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getInitializeAttempts() {
        return initializeAttempts;
    }

    /**
     * Sets the value of the initializeAttempts property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setInitializeAttempts(Integer value) {
        this.initializeAttempts = value;
    }

    /**
     * Gets the value of the initializeSleepInterval property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getInitializeSleepInterval() {
        return initializeSleepInterval;
    }

    /**
     * Sets the value of the initializeSleepInterval property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setInitializeSleepInterval(Integer value) {
        this.initializeSleepInterval = value;
    }

}
