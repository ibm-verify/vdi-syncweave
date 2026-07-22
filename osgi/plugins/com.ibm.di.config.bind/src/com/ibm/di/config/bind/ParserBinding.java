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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

/**
 * <p>Java class for ParserBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ParserBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}InheritingBinding">
 *       &lt;sequence>
 *         &lt;element name="userComment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rawConfig" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}JavaClassBinding" minOccurs="0"/>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}schema" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParserBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "userComment",
    "rawConfig",
    "schema"
})
@XmlRootElement(name = "parser", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class ParserBinding
    extends InheritingBinding implements Serializable
{
	private static final long serialVersionUID = 5537006184880219241L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    protected String userComment;
    protected JavaClassBinding rawConfig;
    @XmlElement(name="schema")
    protected List<SchemaBinding> schema;

    /**
     * Gets the value of the userComment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserComment() {
        return userComment;
    }

    /**
     * Sets the value of the userComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserComment(String value) {
        this.userComment = value;
    }

    /**
     * Gets the value of the rawConfig property.
     * 
     * @return
     *     possible object is
     *     {@link JavaClassBinding }
     *     
     */
    public JavaClassBinding getRawConfig() {
        return rawConfig;
    }

    /**
     * Sets the value of the rawConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link JavaClassBinding }
     *     
     */
    public void setRawConfig(JavaClassBinding value) {
        this.rawConfig = value;
    }

    /**
     * Gets the value of the schema property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the schema property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSchema().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SchemaBinding }
     * 
     * 
     */
    public List<SchemaBinding> getSchemas() {
        if (schema == null) {
            schema = new ArrayList<SchemaBinding>();
        }
        return this.schema;
    }

}
