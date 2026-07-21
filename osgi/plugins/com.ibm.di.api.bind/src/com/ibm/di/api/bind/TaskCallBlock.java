/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.config.bind.AssemblyLineBinding;

/**
 * <p>Java class for TaskCallBlock complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TaskCallBlock">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/api}Entry">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}assemblyLine" minOccurs="0"/>
 *         &lt;element name="runtime" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}TcbRuntime" minOccurs="0"/>
 *         &lt;element name="iwe" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}Entry" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskCallBlock", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = {
    "assemblyLine",
    "runtime",
    "iwe"
})
@XmlRootElement(name = "taskCallBlock", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class TaskCallBlock
    extends Entry
{
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * 
	 */
	private static final long serialVersionUID = 5568048910852343458L;

   protected AssemblyLineBinding assemblyLine;
    @XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
    protected TcbRuntime runtime;
    @XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
    protected Entry iwe;

    /**
     * Gets the value of the assemblyLine property.
     * 
     * @return
     *     possible object is
     *     {@link AssemblyLineBinding }
     *     
     */
    public AssemblyLineBinding getAssemblyLine() {
        return assemblyLine;
    }

    /**
     * Sets the value of the assemblyLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link AssemblyLineBinding }
     *     
     */
    public void setAssemblyLine(AssemblyLineBinding value) {
        this.assemblyLine = value;
    }

    /**
     * Gets the value of the runtime property.
     * 
     * @return
     *     possible object is
     *     {@link TcbRuntime }
     *     
     */
    public TcbRuntime getRuntime() {
        return runtime;
    }

    /**
     * Sets the value of the runtime property.
     * 
     * @param value
     *     allowed object is
     *     {@link TcbRuntime }
     *     
     */
    public void setRuntime(TcbRuntime value) {
        this.runtime = value;
    }

    /**
     * Gets the value of the iwe property.
     * 
     * @return
     *     possible object is
     *     {@link Entry }
     *     
     */
    public Entry getIwe() {
        return iwe;
    }

    /**
     * Sets the value of the iwe property.
     * 
     * @param value
     *     allowed object is
     *     {@link Entry }
     *     
     */
    public void setIwe(Entry value) {
        this.iwe = value;
    }

}
