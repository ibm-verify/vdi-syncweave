/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.model.descriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ParameterMapDescriptor complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * 
 * <pre>
 * &lt;complexType name="ParameterMapDescriptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sectionDescriptor" type="{http://www.ibm.com/xmlns/prod/tdi/71/core}SectionDescriptor" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/71/core}parameterDescriptor" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParameterMapDescriptor", namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core", propOrder = {
    "sectionDescriptor",
    "parameterDescriptor"
})
public class ParameterMapDescriptor implements Serializable {

	private static final long serialVersionUID = -7873204900509759218L;

    @XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core")
    protected List<SectionDescriptor> sectionDescriptor;
	@XmlElementRef
	protected List<ParameterDescriptor> parameterDescriptor;
	@XmlAttribute
	protected String name;

    /**
     * Gets the value of the sectionDescriptor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sectionsDescriptor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSectionsDescriptor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SectionDescriptor }
     * 
     * 
     */
    public List<SectionDescriptor> getSectionDescriptors() {
        if (sectionDescriptor == null) {
            sectionDescriptor = new ArrayList<SectionDescriptor>();
        }
        return this.sectionDescriptor;
    }

	/**
	 * Gets the value of the parameterDescriptor property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the parameterDescriptor property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getParameterDescriptor().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JAXBElement }{@code <}{@link ParameterDescriptor }{@code >}
	 * {@link JAXBElement }{@code <}{@link ModeParameterDescriptor }{@code >}
	 * 
	 * 
	 */
	public List<ParameterDescriptor> getParameterDescriptors() {
		if (parameterDescriptor == null) {
			parameterDescriptor = new ArrayList<ParameterDescriptor>();
		}
		return this.parameterDescriptor;
	}

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
