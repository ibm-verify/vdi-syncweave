/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/scmp}request-in" minOccurs="0"/>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/scmp}op-state"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "requestIn",
    "opState",
    "any"
})
@XmlRootElement(name = "touchpoint-status")
public class TouchpointStatus {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	
    @XmlElement(name = "request-in")
    @XmlSchemaType(name = "anyURI")
    protected String requestIn;
    @XmlElement(name = "op-state", required = true)
    protected EnumOpState opState;
    @XmlAnyElement(lax = true)
    protected List<Object> any;

    /**
     * Gets the value of the requestIn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestIn() {
        return requestIn;
    }

    /**
     * Sets the value of the requestIn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestIn(String value) {
        this.requestIn = value;
    }

    /**
     * Gets the value of the opState property.
     * 
     * @return
     *     possible object is
     *     {@link EnumOpState }
     *     
     */
    public EnumOpState getOpState() {
        return opState;
    }

    /**
     * Sets the value of the opState property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnumOpState }
     *     
     */
    public void setOpState(EnumOpState value) {
        this.opState = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

}
