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
 * <p>Java class for ConditionItemBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConditionItemBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="leftHand" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rightHand" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="operator" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="negate" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="matchAny" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="caseSensitive" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConditionItemBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "leftHand",
    "rightHand"
})
public class ConditionItemBinding implements Serializable {

	private static final long serialVersionUID = -7555191749150100241L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    protected String leftHand;
    protected String rightHand;
    @XmlAttribute
    protected String operator;
    @XmlAttribute
    protected Boolean negate;
    @XmlAttribute
    protected Boolean matchAny;
    @XmlAttribute
    protected Boolean caseSensitive;

    /**
     * Gets the value of the leftHand property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLeftHand() {
        return leftHand;
    }

    /**
     * Sets the value of the leftHand property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLeftHand(String value) {
        this.leftHand = value;
    }

    /**
     * Gets the value of the rightHand property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRightHand() {
        return rightHand;
    }

    /**
     * Sets the value of the rightHand property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRightHand(String value) {
        this.rightHand = value;
    }

    /**
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperator(String value) {
        this.operator = value;
    }

    /**
     * Gets the value of the negate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNegate() {
        if (negate == null) {
            return false;
        } else {
            return negate;
        }
    }

    /**
     * Sets the value of the negate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNegate(Boolean value) {
        this.negate = value;
    }

    /**
     * Gets the value of the matchAny property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMatchAny() {
        if (matchAny == null) {
            return false;
        } else {
            return matchAny;
        }
    }

    /**
     * Sets the value of the matchAny property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMatchAny(Boolean value) {
        this.matchAny = value;
    }

    /**
     * Gets the value of the caseSensitive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCaseSensitive() {
        if (caseSensitive == null) {
            return true;
        } else {
            return caseSensitive;
        }
    }

    /**
     * Sets the value of the caseSensitive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCaseSensitive(Boolean value) {
        this.caseSensitive = value;
    }

}
