/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.model.descriptor;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for BaseDescriptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BaseDescriptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/71/core}parameterMapDescriptor" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BaseDescriptor", namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core", propOrder = { "parameterMapDescriptor" })
@XmlSeeAlso( { ComponentDescriptor.class })
public class BaseDescriptor implements Serializable {

	private static final long serialVersionUID = -1441642777445152850L;

    @XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core")
	protected ParameterMapDescriptor parameterMapDescriptor;

	/**
	 * Gets the value of the parameterMapDescriptor property.
	 * 
	 * @return possible object is {@link ParameterMapDescriptor }
	 * 
	 */
	public ParameterMapDescriptor getParameterMapDescriptor() {
		if (parameterMapDescriptor == null) {
			parameterMapDescriptor = new ParameterMapDescriptor();
		}
		return parameterMapDescriptor;
	}
}
