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
 * <p>Java class for ConnectorModeBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConnectorModeBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="computeChanges" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="skipLookup" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="lookupLimit" type="{http://www.w3.org/2001/XMLSchema}int" default="10" />
 *       &lt;attribute name="skipDeltaEntryDelete" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="processDeltaEntryOnly" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConnectorModeBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class ConnectorModeBinding implements Serializable {

	private static final long serialVersionUID = 4580866283289137653L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    @XmlAttribute
    protected Boolean computeChanges;
    @XmlAttribute
    protected Boolean skipLookup;
    @XmlAttribute
    protected Integer lookupLimit;
    @XmlAttribute
    protected Boolean skipDeltaEntryDelete;
    @XmlAttribute
    protected Boolean processDeltaEntryOnly;

    /**
     * Gets the value of the computeChanges property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isComputeChanges() {
        if (computeChanges == null) {
            return false;
        } else {
            return computeChanges;
        }
    }

    /**
     * Sets the value of the computeChanges property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setComputeChanges(Boolean value) {
        this.computeChanges = value;
    }

    /**
     * Gets the value of the skipLookup property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSkipLookup() {
        if (skipLookup == null) {
            return false;
        } else {
            return skipLookup;
        }
    }

    /**
     * Sets the value of the skipLookup property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSkipLookup(Boolean value) {
        this.skipLookup = value;
    }

    /**
     * Gets the value of the lookupLimit property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getLookupLimit() {
        if (lookupLimit == null) {
            return  10;
        } else {
            return lookupLimit;
        }
    }

    /**
     * Sets the value of the lookupLimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLookupLimit(Integer value) {
        this.lookupLimit = value;
    }

    /**
     * Gets the value of the skipDeltaEntryDelete property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSkipDeltaEntryDelete() {
        if (skipDeltaEntryDelete == null) {
            return false;
        } else {
            return skipDeltaEntryDelete;
        }
    }

    /**
     * Sets the value of the skipDeltaEntryDelete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSkipDeltaEntryDelete(Boolean value) {
        this.skipDeltaEntryDelete = value;
    }

    /**
     * Gets the value of the processDeltaEntryOnly property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isProcessDeltaEntryOnly() {
        if (processDeltaEntryOnly == null) {
            return true;
        } else {
            return processDeltaEntryOnly;
        }
    }

    /**
     * Sets the value of the processDeltaEntryOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setProcessDeltaEntryOnly(Boolean value) {
        this.processDeltaEntryOnly = value;
    }

}
