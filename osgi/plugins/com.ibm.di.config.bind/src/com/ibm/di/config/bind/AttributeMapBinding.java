/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * 				Represents the configuration data required to perform
 * 				mapping between one set of attributes to another.
 * 			
 * 
 * <p>Java class for AttributeMapBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttributeMapBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}SimpleComponentBinding">
 *       &lt;sequence>
 *         &lt;element name="null" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}NullBinding" minOccurs="0"/>
 *         &lt;element name="item" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}AttributeMapItemBinding" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttributeMapBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "_null",
    "item"
})
@XmlRootElement(name = "map", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class AttributeMapBinding
    extends SimpleComponentBinding implements Serializable
{

	private static final long serialVersionUID = 8040511138661272357L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    @XmlElement(name = "null")
    protected NullBinding _null;
    @XmlElement(name="item")
    protected List<AttributeMapItemBinding> item;

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
     * Gets the value of the items property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the item property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AttributeMapItemBinding }
     * 
     * 
     */
    public List<AttributeMapItemBinding> getItems() {
        if (item == null) {
            item = new ArrayList<AttributeMapItemBinding>();
        }
        return this.item;
    }

}
