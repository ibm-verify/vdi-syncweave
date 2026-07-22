/*
 * Copyright contributors to the SyncWeave project
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
 * <p>Java class for PollChannel complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PollChannel">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/api}ListenerChannel">
 *       &lt;attribute name="waitTimeout" type="{http://www.w3.org/2001/XMLSchema}int" default="120" />
 *       &lt;attribute name="batchCap" type="{http://www.w3.org/2001/XMLSchema}int" default="1" />
 *       &lt;attribute name="fillBatch" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="onTimeoutGetAll" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PollChannel", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
@XmlRootElement(name = "pollChannel", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class PollChannel
    extends TransportChannel
{

    @XmlAttribute
    protected Integer waitTimeout;
    @XmlAttribute
    protected Integer batchCap;
    @XmlAttribute
    protected Boolean fillBatch;
    @XmlAttribute
    protected Boolean onTimeoutGetAll;

    /**
     * Gets the value of the waitTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getWaitTimeout() {
        if (waitTimeout == null) {
            return  120;
        } else {
            return waitTimeout;
        }
    }

    /**
     * Sets the value of the waitTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setWaitTimeout(Integer value) {
        this.waitTimeout = value;
    }

    /**
     * Gets the value of the batchCap property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getBatchCap() {
        if (batchCap == null) {
            return  1;
        } else {
            return batchCap;
        }
    }

    /**
     * Sets the value of the batchCap property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBatchCap(Integer value) {
        this.batchCap = value;
    }

    /**
     * Gets the value of the fillBatch property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFillBatch() {
        if (fillBatch == null) {
            return true;
        } else {
            return fillBatch;
        }
    }

    /**
     * Sets the value of the fillBatch property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFillBatch(Boolean value) {
        this.fillBatch = value;
    }

    /**
     * Gets the value of the onTimeoutGetAll property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOnTimeoutGetAll() {
        if (onTimeoutGetAll == null) {
            return true;
        } else {
            return onTimeoutGetAll;
        }
    }

    /**
     * Sets the value of the onTimeoutGetAll property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOnTimeoutGetAll(Boolean value) {
        this.onTimeoutGetAll = value;
    }

}
