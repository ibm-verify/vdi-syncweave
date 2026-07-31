/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for ALHandle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ALHandle">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="workEntry" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}Entry" minOccurs="0"/>
 *         &lt;element name="resultEntry" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}Entry" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="state" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}ALHandleStateEnum" default="init" />
 *       &lt;attribute name="processTcb" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ALHandle", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = {
    "workEntry",
    "resultEntry"
})
@XmlRootElement(name = "alHandle", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class ALHandle {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    @XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
    protected Entry workEntry;
    @XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
    protected Entry resultEntry;
    @XmlAttribute
    protected ALHandleStateEnum state;
    @XmlAttribute
    protected Boolean processTcb;

    /**
     * Gets the value of the workEntry property.
     * 
     * @return
     *     possible object is
     *     {@link Entry }
     *     
     */
    public Entry getWorkEntry() {
        return workEntry;
    }

    /**
     * Sets the value of the workEntry property.
     * 
     * @param value
     *     allowed object is
     *     {@link Entry }
     *     
     */
    public void setWorkEntry(Entry value) {
        this.workEntry = value;
    }

    /**
     * Gets the value of the resultEntry property.
     * 
     * @return
     *     possible object is
     *     {@link Entry }
     *     
     */
    public Entry getResultEntry() {
        return resultEntry;
    }

    /**
     * Sets the value of the resultEntry property.
     * 
     * @param value
     *     allowed object is
     *     {@link Entry }
     *     
     */
    public void setResultEntry(Entry value) {
        this.resultEntry = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link ALHandleStateEnum }
     *     
     */
    public ALHandleStateEnum getState() {
        if (state == null) {
            return ALHandleStateEnum.INIT;
        } else {
            return state;
        }
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link ALHandleStateEnum }
     *     
     */
    public void setState(ALHandleStateEnum value) {
        this.state = value;
    }

    /**
     * Gets the value of the processTcb property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isProcessTcb() {
        if (processTcb == null) {
            return true;
        } else {
            return processTcb;
        }
    }

    /**
     * Sets the value of the processTcb property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setProcessTcb(Boolean value) {
        this.processTcb = value;
    }

}
