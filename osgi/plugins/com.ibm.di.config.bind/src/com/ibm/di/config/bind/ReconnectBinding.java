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
 * <p>Java class for ReconnectBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReconnectBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rule" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ReconnectRuleBinding" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="onInitializationError" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="onConnectionError" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="numberOfRetries" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="retryDelay" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="autoSkipForward" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReconnectBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "rule"
})
public class ReconnectBinding implements Serializable {

	private static final long serialVersionUID = 4629804355796775381L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    @XmlElement(name="rule")
    protected List<ReconnectRuleBinding> rule;
    @XmlAttribute
    protected Boolean onInitializationError;
    @XmlAttribute
    protected Boolean onConnectionError;
    @XmlAttribute
    protected Integer numberOfRetries;
    @XmlAttribute
    protected Integer retryDelay;
    @XmlAttribute
    protected Boolean autoSkipForward;

    /**
     * Gets the value of the rule property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rule property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRule().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReconnectRuleBinding }
     * 
     * 
     */
    public List<ReconnectRuleBinding> getRules() {
        if (rule == null) {
            rule = new ArrayList<ReconnectRuleBinding>();
        }
        return this.rule;
    }

    /**
     * Gets the value of the onInitializationError property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOnInitializationError() {
        if (onInitializationError == null) {
            return false;
        } else {
            return onInitializationError;
        }
    }

    /**
     * Sets the value of the onInitializationError property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOnInitializationError(Boolean value) {
        this.onInitializationError = value;
    }

    /**
     * Gets the value of the onConnectionError property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOnConnectionError() {
        if (onConnectionError == null) {
            return false;
        } else {
            return onConnectionError;
        }
    }

    /**
     * Sets the value of the onConnectionError property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOnConnectionError(Boolean value) {
        this.onConnectionError = value;
    }

    /**
     * Gets the value of the numberOfRetries property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfRetries() {
        return numberOfRetries;
    }

    /**
     * Sets the value of the numberOfRetries property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfRetries(Integer value) {
        this.numberOfRetries = value;
    }

    /**
     * Gets the value of the retryDelay property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRetryDelay() {
        return retryDelay;
    }

    /**
     * Sets the value of the retryDelay property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRetryDelay(Integer value) {
        this.retryDelay = value;
    }

    /**
     * Gets the value of the autoSkipForward property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoSkipForward() {
        if (autoSkipForward == null) {
            return false;
        } else {
            return autoSkipForward;
        }
    }

    /**
     * Sets the value of the autoSkipForward property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoSkipForward(Boolean value) {
        this.autoSkipForward = value;
    }

}
