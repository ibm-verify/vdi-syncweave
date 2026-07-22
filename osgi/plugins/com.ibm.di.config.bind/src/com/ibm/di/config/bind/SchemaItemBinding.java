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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

/**
 * <p>Java class for SchemaItemBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SchemaItemBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}NamedBinding">
 *       &lt;sequence>
 *         &lt;element name="sample" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="item" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}SchemaItemBinding" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}SchemaItemTypeEnum" default="Attribute" />
 *       &lt;attribute name="syntax" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}JavaClassConstraint" />
 *       &lt;attribute name="nativeSyntax" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="minOccurs" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *       &lt;attribute name="maxOccurs" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}UnboundedInt" default="unbounded" />
 *       &lt;attribute name="comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SchemaItemBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "sample",
    "item"
})
public class SchemaItemBinding
    extends NamedBinding implements Serializable
{

	private static final long serialVersionUID = -3281926501832773250L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	protected String sample;
    @XmlElement(name="item")
    protected List<SchemaItemBinding> item;
    @XmlAttribute
    protected SchemaItemTypeEnum type;
    @XmlAttribute
    protected String syntax;
    @XmlAttribute
    protected String nativeSyntax;
    @XmlAttribute
    protected Integer minOccurs;
    @XmlAttribute
    protected String maxOccurs;
    @XmlAttribute
    protected String comment;

    /**
     * Gets the value of the sample property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSample() {
        return sample;
    }

    /**
     * Sets the value of the sample property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSample(String value) {
        this.sample = value;
    }

    /**
     * Gets the value of the item property.
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
     * {@link SchemaItemBinding }
     * 
     * 
     */
    public List<SchemaItemBinding> getItems() {
        if (item == null) {
            item = new ArrayList<SchemaItemBinding>();
        }
        return this.item;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link SchemaItemTypeEnum }
     *     
     */
    public SchemaItemTypeEnum getType() {
        if (type == null) {
            return SchemaItemTypeEnum.ATTRIBUTE;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link SchemaItemTypeEnum }
     *     
     */
    public void setType(SchemaItemTypeEnum value) {
        this.type = value;
    }

    /**
     * Gets the value of the syntax property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSyntax() {
        return syntax;
    }

    /**
     * Sets the value of the syntax property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSyntax(String value) {
        this.syntax = value;
    }

    /**
     * Gets the value of the nativeSyntax property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNativeSyntax() {
        return nativeSyntax;
    }

    /**
     * Sets the value of the nativeSyntax property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNativeSyntax(String value) {
        this.nativeSyntax = value;
    }

    /**
     * Gets the value of the minOccurs property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getMinOccurs() {
        if (minOccurs == null) {
            return  0;
        } else {
            return minOccurs;
        }
    }

    /**
     * Sets the value of the minOccurs property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMinOccurs(Integer value) {
        this.minOccurs = value;
    }

    /**
     * Gets the value of the maxOccurs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxOccurs() {
        if (maxOccurs == null) {
            return "unbounded";
        } else {
            return maxOccurs;
        }
    }

    /**
     * Sets the value of the maxOccurs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxOccurs(String value) {
        this.maxOccurs = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
    	return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

}
