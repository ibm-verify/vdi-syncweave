/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * 				Represents a config object holding a configuration
 * 				data
 * 				and also attributes which are only valid for a component
 * 				configurable in the AL.
 * 			
 * 
 * <p>Java class for ALComponentBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ALComponentBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}NamedBinding">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ALComponentBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlSeeAlso({
    CompositeALComponentBinding.class,
    ComplexALComponentBinding.class,
    SimpleALComponentBinding.class
})
public abstract class ALComponentBinding
    extends NamedBinding implements Serializable
{

	private static final long serialVersionUID = 6295104106225219298L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
}
