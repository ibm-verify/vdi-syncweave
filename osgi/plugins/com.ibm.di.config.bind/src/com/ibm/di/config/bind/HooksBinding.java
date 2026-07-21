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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * <p>Java class for HooksBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HooksBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}InheritingBinding">
 *       &lt;sequence>
 *         &lt;element name="hook" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}HookBinding" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HooksBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "hook"
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type", defaultImpl = HooksBinding.class)
public class HooksBinding
    extends InheritingBinding implements Serializable
{

	private static final long serialVersionUID = -3461698082880974886L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    @XmlElement(name="hook")
    protected List<HookBinding> hook;

    /**
     * Gets the value of the hook property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hook property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHook().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HookBinding }
     * 
     * 
     */
    public List<HookBinding> getHooks() {
        if (hook == null) {
            hook = new ArrayList<HookBinding>();
        }
        return this.hook;
    }

}
