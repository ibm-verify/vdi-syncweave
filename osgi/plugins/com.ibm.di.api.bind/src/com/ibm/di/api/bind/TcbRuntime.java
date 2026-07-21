/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for TcbRuntime complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TcbRuntime">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="initParam" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}TcbInitParam" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="components" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}TcbComponents" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="mode" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="operation" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TcbRuntime", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = {
    "initParam",
    "components"
})
public class TcbRuntime implements Serializable {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 7399171885894317983L;

    @XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
    protected Entry initParam;
    @XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
    protected TcbComponents components;
    @XmlAttribute
    protected String mode;
    @XmlAttribute
    protected String operation;

    /**
     * Gets the value of the initParam property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the initParam property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInitParam().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TcbInitParam }
     * 
     * 
     */
    public Entry getInitParam() {
        if (initParam == null) {
            initParam = new Entry();
        }
        return this.initParam;
    }

    /**
     * Gets the value of the components property.
     * 
     * @return
     *     possible object is
     *     {@link TcbComponents }
     *     
     */
    public TcbComponents getComponents() {
        return components;
    }

    /**
     * Sets the value of the components property.
     * 
     * @param value
     *     allowed object is
     *     {@link TcbComponents }
     *     
     */
    public void setComponents(TcbComponents value) {
        this.components = value;
    }

    /**
     * Gets the value of the mode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMode() {
        return mode;
    }

    /**
     * Sets the value of the mode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMode(String value) {
        this.mode = value;
    }

    /**
     * Gets the value of the operation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Sets the value of the operation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperation(String value) {
        this.operation = value;
    }

}
