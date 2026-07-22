/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for EventListener complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EventListener">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/api}Listener">
 *       &lt;attribute name="typeFilter" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="idFilter" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventListener", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
@XmlRootElement(name = "eventListener", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class EventListener
    extends Listener
{
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    @XmlAttribute
    protected String typeFilter;
    @XmlAttribute
    protected String idFilter;

    /**
     * Gets the value of the typeFilter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeFilter() {
        return typeFilter;
    }

    /**
     * Sets the value of the typeFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeFilter(String value) {
        this.typeFilter = value;
    }

    /**
     * Gets the value of the idFilter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdFilter() {
        return idFilter;
    }

    /**
     * Sets the value of the idFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdFilter(String value) {
        this.idFilter = value;
    }

}
