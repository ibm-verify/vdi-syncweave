/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for InheritingBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="InheritingBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}NamedBinding">
 *       &lt;attribute name="inheritFrom" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InheritingBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlSeeAlso( { SchemaBinding.class, CompositeComponentBinding.class, ComplexComponentBinding.class, SimpleComponentBinding.class,
		ParserBinding.class, AttributeMapItemBinding.class, LinkCriteriaBinding.class, HookBinding.class, ParametersBinding.class,
		HooksBinding.class })
public class InheritingBinding extends NamedBinding {
	private static final long serialVersionUID = -3734761333905503554L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlAttribute
	@XmlSchemaType(name = "anyURI")
	protected String inheritFrom;

	/**
	 * Gets the value of the inheritFrom property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getInheritFrom() {
		return inheritFrom;
	}

	/**
	 * Sets the value of the inheritFrom property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setInheritFrom(String value) {
		this.inheritFrom = value;
	}

}
