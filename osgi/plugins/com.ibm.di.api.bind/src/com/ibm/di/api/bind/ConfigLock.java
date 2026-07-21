/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.config.bind.SolutionBinding;

/**
 * <p>Java class for ConfigLock complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConfigLock">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}solution"/>
 *       &lt;/sequence>
 *       &lt;attribute name="configPassword" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="encrypt" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConfigLock", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = {
    "solution"
})
@XmlRootElement(name = "configLock", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class ConfigLock {

    @XmlElement(required = true)
    protected SolutionBinding solution;
    @XmlAttribute
    protected String configPassword;
    @XmlAttribute
    protected Boolean encrypt;

    /**
     * Gets the value of the solution property.
     * 
     * @return
     *     possible object is
     *     {@link SolutionBinding }
     *     
     */
    public SolutionBinding getSolution() {
        return solution;
    }

    /**
     * Sets the value of the solution property.
     * 
     * @param value
     *     allowed object is
     *     {@link SolutionBinding }
     *     
     */
    public void setSolution(SolutionBinding value) {
        this.solution = value;
    }

    /**
     * Gets the value of the configPassword property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConfigPassword() {
        return configPassword;
    }

    /**
     * Sets the value of the configPassword property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConfigPassword(String value) {
        this.configPassword = value;
    }

    /**
     * Gets the value of the encrypt property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEncrypt() {
        if (encrypt == null) {
            return false;
        } else {
            return encrypt;
        }
    }

    /**
     * Sets the value of the encrypt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEncrypt(Boolean value) {
        this.encrypt = value;
    }

}
