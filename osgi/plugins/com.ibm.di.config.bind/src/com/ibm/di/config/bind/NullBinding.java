/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * 				Contains the configuration of how to recognize "null"
 * 				values and how to treat them when found.
 * 			
 * 
 * <p>Java class for NullBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NullBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="behavior" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="behaviorValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="definition" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="definitionValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NullBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "behavior",
    "behaviorValue",
    "definition",
    "definitionValue"
})
public class NullBinding implements Serializable {

	private static final long serialVersionUID = 2447035785691008834L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    protected String behavior;
    protected String behaviorValue;
    protected String definition;
    protected String definitionValue;

    /**
     * Gets the value of the behavior property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBehavior() {
        return behavior;
    }

    /**
     * Sets the value of the behavior property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBehavior(String value) {
        this.behavior = value;
    }

    /**
     * Gets the value of the behaviorValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBehaviorValue() {
        return behaviorValue;
    }

    /**
     * Sets the value of the behaviorValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBehaviorValue(String value) {
        this.behaviorValue = value;
    }

    /**
     * Gets the value of the definition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * Sets the value of the definition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefinition(String value) {
        this.definition = value;
    }

    /**
     * Gets the value of the definitionValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefinitionValue() {
        return definitionValue;
    }

    /**
     * Sets the value of the definitionValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefinitionValue(String value) {
        this.definitionValue = value;
    }

}
