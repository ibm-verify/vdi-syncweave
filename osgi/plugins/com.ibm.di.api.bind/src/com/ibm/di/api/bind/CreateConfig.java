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
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.config.bind.SolutionBinding;

/**
 * <p>Java class for CreateConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CreateConfig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}solution"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}FileNameRestrictionType" />
 *       &lt;attribute name="overwrite" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="encrypt" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="leaveCheckOut" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateConfig", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = {
    "solution"
})
@XmlRootElement(name = "createConfig", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class CreateConfig {

    @XmlElement(required = true)
    protected SolutionBinding solution;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected Boolean overwrite;
    @XmlAttribute
    protected Boolean encrypt;
    @XmlAttribute
    protected Boolean leaveCheckOut;

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
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the overwrite property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOverwrite() {
        if (overwrite == null) {
            return false;
        } else {
            return overwrite;
        }
    }

    /**
     * Sets the value of the overwrite property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOverwrite(Boolean value) {
        this.overwrite = value;
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

    /**
     * Gets the value of the leaveCheckOut property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLeaveCheckOut() {
        if (leaveCheckOut == null) {
            return true;
        } else {
            return leaveCheckOut;
        }
    }

    /**
     * Sets the value of the leaveCheckOut property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLeaveCheckOut(Boolean value) {
        this.leaveCheckOut = value;
    }

}
