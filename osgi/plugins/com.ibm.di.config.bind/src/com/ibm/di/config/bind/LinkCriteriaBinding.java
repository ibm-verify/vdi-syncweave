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
 * <p>Java class for LinkCriteriaBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LinkCriteriaBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}InheritingBinding">
 *       &lt;sequence>
 *         &lt;element name="script" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="item" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}LinkCriteriaItemBinding" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="matchAny" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="advanced" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LinkCriteriaBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "script",
    "item"
})
public class LinkCriteriaBinding
    extends InheritingBinding implements Serializable
{

	private static final long serialVersionUID = 9048846753302703520L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    protected String script;
    @XmlElement(name="item")
    protected List<LinkCriteriaItemBinding> item;
    @XmlAttribute
    protected Boolean matchAny;
    @XmlAttribute
    protected Boolean advanced;

    /**
     * Gets the value of the script property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScript() {
        return script;
    }

    /**
     * Sets the value of the script property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScript(String value) {
        this.script = value;
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
     * {@link LinkCriteriaItemBinding }
     * 
     * 
     */
    public List<LinkCriteriaItemBinding> getItems() {
        if (item == null) {
            item = new ArrayList<LinkCriteriaItemBinding>();
        }
        return this.item;
    }

    /**
     * Gets the value of the matchAny property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMatchAny() {
        if (matchAny == null) {
            return false;
        } else {
            return matchAny;
        }
    }

    /**
     * Sets the value of the matchAny property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMatchAny(Boolean value) {
        this.matchAny = value;
    }

    /**
     * Gets the value of the advanced property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAdvanced() {
        if (advanced == null) {
            return false;
        } else {
            return advanced;
        }
    }

    /**
     * Sets the value of the advanced property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAdvanced(Boolean value) {
        this.advanced = value;
    }

}
