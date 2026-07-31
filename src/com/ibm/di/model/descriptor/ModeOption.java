/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.ibm.di.model.descriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ModeOption complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;ModeOption&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;value&quot; type=&quot;{http://www.ibm.com/xmlns/prod/tdi/71/core}ConnectorModesEnum&quot;/&gt;
 *         &lt;element name=&quot;label&quot; type=&quot;{http://www.ibm.com/xmlns/prod/tdi/71/core}Label&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModeOption", namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core", propOrder = { "value", "label" })
public class ModeOption implements Serializable {

	private static final long serialVersionUID = 2598811919454474591L;

	@XmlElement(required = true)
	protected ConnectorModesEnum value;
	@XmlElement(required = true)
	protected List<Label> label;

	/**
	 * Gets the value of the value property.
	 * 
	 * @return possible object is {@link ConnectorModesEnum }
	 * 
	 */
	public ConnectorModesEnum getValue() {
		return value;
	}

	/**
	 * Sets the value of the value property.
	 * 
	 * @param value
	 *            allowed object is {@link ConnectorModesEnum }
	 * 
	 */
	public void setValue(ConnectorModesEnum value) {
		this.value = value;
	}

	/**
	 * Gets the value of the label property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the label property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getLabel().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Label }
	 * 
	 * 
	 */
	public List<Label> getLabels() {
		if (label == null) {
			label = new ArrayList<Label>();
		}
		return this.label;
	}

}
