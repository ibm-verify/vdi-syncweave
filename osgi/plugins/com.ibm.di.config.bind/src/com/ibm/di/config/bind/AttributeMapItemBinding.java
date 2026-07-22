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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * 				Represents the configuration data required to map a
 * 				single attribute to another
 * 			
 * 
 * <p>Java class for AttributeMapItemBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttributeMapItemBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}InheritingBinding">
 *       &lt;sequence>
 *         &lt;element name="mapsTo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="null" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}NullBinding" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}AttributeMapItemTypeEnum" default="simple" />
 *       &lt;attribute name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="add" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="modify" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttributeMapItemBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "mapsTo",
    "_null"
})
public class AttributeMapItemBinding
    extends InheritingBinding implements Serializable
{

	private static final long serialVersionUID = 6263053251877952545L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    @XmlElement(required = true)
    protected String mapsTo;
    @XmlElement(name = "null")
    protected NullBinding _null;
    @XmlAttribute
    protected AttributeMapItemTypeEnum type;
    @XmlAttribute
    protected Boolean enabled;
    @XmlAttribute
    protected Boolean add;
    @XmlAttribute
    protected Boolean modify;

    /**
     * Gets the value of the mapsTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMapsTo() {
        return mapsTo;
    }

    /**
     * Sets the value of the mapsTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMapsTo(String value) {
        this.mapsTo = value;
    }

    /**
     * Gets the value of the null property.
     * 
     * @return
     *     possible object is
     *     {@link NullBinding }
     *     
     */
    public NullBinding getNull() {
        return _null;
    }

    /**
     * Sets the value of the null property.
     * 
     * @param value
     *     allowed object is
     *     {@link NullBinding }
     *     
     */
    public void setNull(NullBinding value) {
        this._null = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link AttributeMapItemTypeEnum }
     *     
     */
    public AttributeMapItemTypeEnum getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributeMapItemTypeEnum }
     *     
     */
    public void setType(AttributeMapItemTypeEnum value) {
        this.type = value;
    }

    /**
     * Gets the value of the enabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEnabled() {
        if (enabled == null) {
            return true;
        } else {
            return enabled;
        }
    }

    /**
     * Sets the value of the enabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnabled(Boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the add property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAdd() {
        if (add == null) {
            return true;
        } else {
            return add;
        }
    }

    /**
     * Sets the value of the add property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAdd(Boolean value) {
        this.add = value;
    }

    /**
     * Gets the value of the modify property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isModify() {
        if (modify == null) {
            return true;
        } else {
            return modify;
        }
    }

    /**
     * Sets the value of the modify property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setModify(Boolean value) {
        this.modify = value;
    }

}
