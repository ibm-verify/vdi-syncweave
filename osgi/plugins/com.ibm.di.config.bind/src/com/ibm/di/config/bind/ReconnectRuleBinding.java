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
 * <p>Java class for ReconnectRuleBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReconnectRuleBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="action" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="exceptionClass" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="exceptionMsgRegEx" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReconnectRuleBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class ReconnectRuleBinding implements Serializable {

	private static final long serialVersionUID = 1427582031671283770L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    @XmlAttribute
    protected String action;
    @XmlAttribute
    protected String exceptionClass;
    @XmlAttribute
    protected String exceptionMsgRegEx;

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAction(String value) {
        this.action = value;
    }

    /**
     * Gets the value of the exceptionClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExceptionClass() {
        return exceptionClass;
    }

    /**
     * Sets the value of the exceptionClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExceptionClass(String value) {
        this.exceptionClass = value;
    }

    /**
     * Gets the value of the exceptionMsgRegEx property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExceptionMsgRegEx() {
        return exceptionMsgRegEx;
    }

    /**
     * Sets the value of the exceptionMsgRegEx property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExceptionMsgRegEx(String value) {
        this.exceptionMsgRegEx = value;
    }

}
