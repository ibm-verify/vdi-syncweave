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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for BranchBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BranchBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}CompositeComponentBinding">
 *       &lt;sequence>
 *         &lt;element name="condition" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ConditionBinding" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}BranchTypeEnum" default="If" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BranchBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "condition"
})
@XmlRootElement(name = "branch", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class BranchBinding
    extends CompositeComponentBinding implements Serializable
{

	private static final long serialVersionUID = 2614088014803521788L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    protected ConditionBinding condition;
    @XmlAttribute
    protected BranchTypeEnum type;

    /**
     * Gets the value of the condition property.
     * 
     * @return
     *     possible object is
     *     {@link ConditionBinding }
     *     
     */
    public ConditionBinding getCondition() {
        return condition;
    }

    /**
     * Sets the value of the condition property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConditionBinding }
     *     
     */
    public void setCondition(ConditionBinding value) {
        this.condition = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object  is
     *     {@link BranchTypeEnum }
     *     
     */
    public BranchTypeEnum getType() {
        if (type == null) {
            return BranchTypeEnum.IF;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link BranchTypeEnum }
     *     
     */
    public void setType(BranchTypeEnum value) {
        this.type = value;
    }

}
