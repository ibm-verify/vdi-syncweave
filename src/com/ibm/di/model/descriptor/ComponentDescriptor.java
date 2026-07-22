/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.model.descriptor;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>Java class for ComponentDescriptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ComponentDescriptor">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/71/core}BaseDescriptor">
 *       &lt;sequence>
 *         &lt;element name="javaClass" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="name" type="{http://www.ibm.com/xmlns/prod/tdi/71/core}Label" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.ibm.com/xmlns/prod/tdi/71/core}Label" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComponentDescriptor", namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core", propOrder = {
    "javaClass",
    "name",
    "description"
})
@XmlSeeAlso({
    ParserDescriptor.class,
    ConnectorDescriptor.class,
    FunctionComponentDescriptor.class
})
public abstract class ComponentDescriptor extends BaseDescriptor {

	private static final long serialVersionUID = -1644710714937235578L;

    @XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core", required = true)
    protected String javaClass;
    @XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core")
    protected List<Label> name;
    @XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core")
    protected List<Label> description;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute
    protected String version;

    /**
     * Gets the value of the javaClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJavaClass() {
        return javaClass;
    }

    /**
     * Sets the value of the javaClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJavaClass(String value) {
        this.javaClass = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the name property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Label }
     * 
     * 
     */
    public List<Label> getName() {
        if (name == null) {
            name = new ArrayList<Label>();
        }
        return this.name;
    }

    /**
     * Gets the value of the description property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Label }
     * 
     * 
     */
    public List<Label> getDescription() {
        if (description == null) {
            description = new ArrayList<Label>();
        }
        return this.description;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

}
