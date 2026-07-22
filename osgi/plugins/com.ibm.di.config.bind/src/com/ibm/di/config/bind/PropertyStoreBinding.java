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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for PropertyStoreBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PropertyStoreBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}NamedBinding">
 *       &lt;sequence>
 *         &lt;element name="nameFilters" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="connector" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}JavaClassBinding" minOccurs="0"/>
 *         &lt;element name="parser" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}JavaClassBinding" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="keyName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="valueName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="readOnly" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="initialLoad" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="cacheTimeout" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PropertyStoreBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "nameFilters",
    "connector",
    "parser"
})
@XmlRootElement(name = "propertyStore", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class PropertyStoreBinding
    extends NamedBinding implements Serializable
{

	private static final long serialVersionUID = -4644463853626599638L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    protected String nameFilters;
    protected JavaClassBinding connector;
    protected JavaClassBinding parser;
    @XmlAttribute
    protected String keyName;
    @XmlAttribute
    protected String valueName;
    @XmlAttribute
    protected Boolean readOnly;
    @XmlAttribute
    protected Boolean initialLoad;
    @XmlAttribute
    protected Integer cacheTimeout;

    /**
     * Gets the value of the nameFilters property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNameFilters() {
        return nameFilters;
    }

    /**
     * Sets the value of the nameFilters property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNameFilters(String value) {
        this.nameFilters = value;
    }

    /**
     * Gets the value of the connector property.
     * 
     * @return
     *     possible object is
     *     {@link JavaClassBinding }
     *     
     */
    public JavaClassBinding getConnector() {
        return connector;
    }

    /**
     * Sets the value of the connector property.
     * 
     * @param value
     *     allowed object is
     *     {@link JavaClassBinding }
     *     
     */
    public void setConnector(JavaClassBinding value) {
        this.connector = value;
    }

    /**
     * Gets the value of the parser property.
     * 
     * @return
     *     possible object is
     *     {@link JavaClassBinding }
     *     
     */
    public JavaClassBinding getParser() {
        return parser;
    }

    /**
     * Sets the value of the parser property.
     * 
     * @param value
     *     allowed object is
     *     {@link JavaClassBinding }
     *     
     */
    public void setParser(JavaClassBinding value) {
        this.parser = value;
    }

    /**
     * Gets the value of the keyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * Sets the value of the keyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyName(String value) {
        this.keyName = value;
    }

    /**
     * Gets the value of the valueName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueName() {
        return valueName;
    }

    /**
     * Sets the value of the valueName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueName(String value) {
        this.valueName = value;
    }

    /**
     * Gets the value of the readOnly property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isReadOnly() {
        if (readOnly == null) {
            return false;
        } else {
            return readOnly;
        }
    }

    /**
     * Sets the value of the readOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReadOnly(Boolean value) {
        this.readOnly = value;
    }

    /**
     * Gets the value of the initialLoad property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInitialLoad() {
        if (initialLoad == null) {
            return false;
        } else {
            return initialLoad;
        }
    }

    /**
     * Sets the value of the initialLoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInitialLoad(Boolean value) {
        this.initialLoad = value;
    }

    /**
     * Gets the value of the cacheTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getCacheTimeout() {
        if (cacheTimeout == null) {
            return  0;
        } else {
            return cacheTimeout;
        }
    }

    /**
     * Sets the value of the cacheTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCacheTimeout(Integer value) {
        this.cacheTimeout = value;
    }

}
