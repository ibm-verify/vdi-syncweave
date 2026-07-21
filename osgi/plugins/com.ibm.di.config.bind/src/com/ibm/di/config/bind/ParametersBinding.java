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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

/**
 * Represents a list of config parameters.
 * 			
 * 
 * <p>Java class for ParametersBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ParametersBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}InheritingBinding">
 *       &lt;sequence>
 *         &lt;element name="parameter" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ParameterBinding" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParametersBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "parameter"
})
@XmlSeeAlso({
    LogItemBinding.class,
    JavaClassBinding.class
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type", defaultImpl = JavaClassBinding.class)
@JsonSubTypes({
    @Type(value = JavaClassBinding.class, name = "JavaClassBinding"),
    @Type(value = LogItemBinding.class, name = "LogItemBinding")
})
public class ParametersBinding
    extends InheritingBinding implements Serializable
{

	private static final long serialVersionUID = 599685132454778827L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    @XmlElement(name="parameter")
    protected List<ParameterBinding> parameter = new ArrayList<ParameterBinding>();

    /**
     * Gets the value of the parameter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parameter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParameter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ParameterBinding }
     * 
     * 
     */
    public List<ParameterBinding> getParameters() {
        if (parameter == null) {
            parameter = new ArrayList<ParameterBinding>();
        }
        return this.parameter;
    }

}
