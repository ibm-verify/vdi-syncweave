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
 * 				Contains the configuration of the Initialization aspect
 * 				of the AL
 * 			
 * 
 * <p>Java class for ALInitParamsBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ALInitParamsBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="schema" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}SchemaBinding" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ALInitParamsBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "schema"
})
public class ALInitParamsBinding implements Serializable {

	private static final long serialVersionUID = 1921150916422963179L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    protected SchemaBinding schema;

    /**
     * Gets the value of the schema property.
     * 
     * @return
     *     possible object is
     *     {@link SchemaBinding }
     *     
     */
    public SchemaBinding getSchema() {
        return schema;
    }

    /**
     * Sets the value of the schema property.
     * 
     * @param value
     *     allowed object is
     *     {@link SchemaBinding }
     *     
     */
    public void setSchema(SchemaBinding value) {
        this.schema = value;
    }

}
