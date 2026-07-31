/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

/**
 * <p>Java class for SolutionInterfaceBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SolutionInterfaceBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="userComment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="al" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ExposedAlBinding" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="property" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ExposedPropertyBinding" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="solutionName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="healthAl" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="pollInterval" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SolutionInterfaceBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "userComment",
    "al",
    "property"
})
public class SolutionInterfaceBinding implements Serializable {

	private static final long serialVersionUID = 124480431189572024L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    protected String userComment;
    @XmlElement(name="al")
    protected List<ExposedAlBinding> al;
    @XmlElement(name="property")
    protected List<ExposedPropertyBinding> property;
    @XmlAttribute
    protected Boolean enabled;
    @XmlAttribute
    protected String solutionName;
    @XmlAttribute
    protected String healthAl;
    @XmlAttribute
    protected Integer pollInterval;

    /**
     * Gets the value of the userComment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserComment() {
        return userComment;
    }

    /**
     * Sets the value of the userComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserComment(String value) {
        this.userComment = value;
    }

    /**
     * Gets the value of the al property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the al property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAl().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExposedAlBinding }
     * 
     * 
     */
    public List<ExposedAlBinding> getAls() {
        if (al == null) {
            al = new ArrayList<ExposedAlBinding>();
        }
        return this.al;
    }

    /**
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExposedPropertyBinding }
     * 
     * 
     */
    public List<ExposedPropertyBinding> getProperties() {
        if (property == null) {
            property = new ArrayList<ExposedPropertyBinding>();
        }
        return this.property;
    }

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
     * Gets the value of the solutionName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSolutionName() {
        return solutionName;
    }

    /**
     * Sets the value of the solutionName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSolutionName(String value) {
        this.solutionName = value;
    }

    /**
     * Gets the value of the healthAl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHealthAl() {
        return healthAl;
    }

    /**
     * Sets the value of the healthAl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHealthAl(String value) {
        this.healthAl = value;
    }

    /**
     * Gets the value of the pollInterval property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPollInterval() {
        return pollInterval;
    }

    /**
     * Sets the value of the pollInterval property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPollInterval(Integer value) {
        this.pollInterval = value;
    }

}
