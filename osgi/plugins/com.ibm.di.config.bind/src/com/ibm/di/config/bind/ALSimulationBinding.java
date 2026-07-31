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
 * 				Contains the configuration of the Simulation aspect of
 * 				the AL.
 * 			
 * 
 * <p>Java class for ALSimulationBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ALSimulationBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="proxy" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ProxyALBinding" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ALSimulationBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "proxy"
})
public class ALSimulationBinding implements Serializable {

	private static final long serialVersionUID = 5151341855024563680L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    protected ProxyALBinding proxy;

    /**
     * Gets the value of the proxy property.
     * 
     * @return
     *     possible object is
     *     {@link ProxyALBinding }
     *     
     */
    public ProxyALBinding getProxy() {
        return proxy;
    }

    /**
     * Sets the value of the proxy property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProxyALBinding }
     *     
     */
    public void setProxy(ProxyALBinding value) {
        this.proxy = value;
    }

}
