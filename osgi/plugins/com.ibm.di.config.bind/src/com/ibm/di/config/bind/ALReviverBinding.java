/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ALReviverBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ALReviverBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALStarterBinding">
 *       &lt;attribute name="failIfAlDiedIn" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="failureAl" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ALReviverBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlRootElement(name = "reviveAl", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class ALReviverBinding extends ALStarterBinding {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 2286754927072757333L;

	@XmlAttribute
	protected Integer failIfAlDiedIn;
	@XmlAttribute
	protected String failureAl;
	@XmlAttribute
	protected Boolean enabled;

	/**
	 * Gets the value of the failIfAlDiedIn property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getFailIfAlDiedIn() {
		return failIfAlDiedIn;
	}

	/**
	 * Sets the value of the failIfAlDiedIn property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setFailIfAlDiedIn(Integer value) {
		this.failIfAlDiedIn = value;
	}

	/**
	 * Gets the value of the failureAl property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getFailureAl() {
		return failureAl;
	}

	/**
	 * Sets the value of the failureAl property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setFailureAl(String value) {
		this.failureAl = value;
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
            return true;
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

}
