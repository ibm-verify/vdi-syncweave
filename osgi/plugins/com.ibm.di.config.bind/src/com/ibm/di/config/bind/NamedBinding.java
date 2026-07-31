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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

/**
 * This represents a configuration object which name is a mandatory attribute.
 * 
 * <p>
 * Java class for NamedBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="NamedBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NamedBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlRootElement(name = "config", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlSeeAlso( { InheritingBinding.class, ALComponentBinding.class, ContainerBinding.class, PropertyStoreBinding.class,
		AssemblyLineBinding.class, ALComponentsBinding.class, SchemaItemBinding.class, ALOperationBinding.class,
		ExposedAlBinding.class, ExposedPropertyBinding.class, SchemaItemBinding.class })
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type", defaultImpl = SchemaItemBinding.class)
@JsonSubTypes({
    @Type(value = ConnectorBinding.class, name = "connector"),
    @Type(value = ParserBinding.class, name = "parser"),
    @Type(value = FunctionBinding.class, name = "function"),
    @Type(value = ScriptBinding.class, name = "script"),
    @Type(value = SchemaBinding.class, name = "schema"),
    @Type(value = SchemaItemBinding.class, name = "schemaItem"),
    @Type(value = PropertyStoreBinding.class, name = "propertyStore"),
    @Type(value = AssemblyLineBinding.class, name = "assemblyLine"),
    @Type(value = ContainerBinding.class, name = "container"),
    @Type(value = AttributeMapBinding.class, name = "attributeMap"),
    @Type(value = AttributeMapBinding.class, name = "map"),
    @Type(value = ALExecutionScheduleBinding.class, name = "schedule"),
    @Type(value = ALReviverBinding.class, name = "reviver"),
    @Type(value = ComplexALComponentBinding.class, name = "complex"),
    @Type(value = CompositeALComponentBinding.class, name = "composite"),
    @Type(value = SimpleALComponentBinding.class, name = "simple")
})
public class NamedBinding implements Serializable {

	private static final long serialVersionUID = -1779416566114158354L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	@XmlAttribute(required = true)
	protected String name;

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(String value) {
		this.name = value;
	}

}
