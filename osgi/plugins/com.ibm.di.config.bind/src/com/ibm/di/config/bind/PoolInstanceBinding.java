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
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for PoolInstanceBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PoolInstanceBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="onExhausted" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}PoolInstanceExhaustedEnum" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PoolInstanceBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class PoolInstanceBinding implements Serializable {

	private static final long serialVersionUID = 8166292211073018758L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    @XmlAttribute
    protected Boolean enabled;
    @XmlAttribute
    protected PoolInstanceExhaustedEnum onExhausted;

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
     * Gets the value of the onExhausted property.
     * 
     * @return
     *     possible object is
     *     {@link PoolInstanceExhaustedEnum }
     *     
     */
    public PoolInstanceExhaustedEnum getOnExhausted() {
        return onExhausted;
    }

    /**
     * Sets the value of the onExhausted property.
     * 
     * @param value
     *     allowed object is
     *     {@link PoolInstanceExhaustedEnum }
     *     
     */
    public void setOnExhausted(PoolInstanceExhaustedEnum value) {
        this.onExhausted = value;
    }

}
