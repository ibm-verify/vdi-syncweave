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
 * Requests the Server to shutdown.
 * 
 * <p>Java class for Shutdown complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Shutdown">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="exitCode" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *       &lt;attribute name="sync" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Shutdown", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
@XmlRootElement(name = "shutdown", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class Shutdown {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    @XmlAttribute
    protected Integer exitCode;
    @XmlAttribute
    protected Boolean sync;

    /**
     * Gets the value of the exitCode property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getExitCode() {
        if (exitCode == null) {
            return  0;
        } else {
            return exitCode;
        }
    }

    /**
     * Sets the value of the exitCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setExitCode(Integer value) {
        this.exitCode = value;
    }

    /**
     * Gets the value of the sync property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSync() {
        if (sync == null) {
            return true;
        } else {
            return sync;
        }
    }

    /**
     * Sets the value of the sync property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSync(Boolean value) {
        this.sync = value;
    }

}
