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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 * Provides information about the Server.
 * 
 * 
 * <p>
 * Java class for SolutionBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="SolutionBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="context" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}SolutionContextBinding" minOccurs="0"/>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}container" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="idiVersion" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="created" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="createdBy" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="modified" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="modifiedBy" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SolutionBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = { "context", "container" })
@XmlRootElement(name = "solution", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class SolutionBinding implements Serializable {

	private static final long serialVersionUID = -2722921838590181763L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	protected SolutionContextBinding context;
	@XmlElementRef
	protected List<ContainerBinding> container;
	@XmlAttribute
	protected String idiVersion;
	@XmlAttribute
	protected String version;
	@XmlAttribute
	@XmlJavaTypeAdapter(value = XMLGregorianCalendarAdapter.class)
	protected Long created;
	@XmlAttribute
	protected String createdBy;
	@XmlAttribute
	@XmlJavaTypeAdapter(value = XMLGregorianCalendarAdapter.class)
	protected Long modified;
	@XmlAttribute
	protected String modifiedBy;

	/**
	 * Gets the value of the context property.
	 * 
	 * @return possible object is {@link SolutionContextBinding }
	 * 
	 */
	public SolutionContextBinding getContext() {
		return context;
	}

	/**
	 * Sets the value of the context property.
	 * 
	 * @param value
	 *            allowed object is {@link SolutionContextBinding }
	 * 
	 */
	public void setContext(SolutionContextBinding value) {
		this.context = value;
	}

	/**
	 * Gets the value of the container property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the container property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getContainer().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * 
	 * 
	 */
	public List<ContainerBinding> getContainers() {
		if (container == null) {
			container = new ArrayList<ContainerBinding>();
		}
		return this.container;
	}

	/**
	 * Gets the value of the idiVersion property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getIdiVersion() {
		return idiVersion;
	}

	/**
	 * Sets the value of the idiVersion property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setIdiVersion(String value) {
		this.idiVersion = value;
	}

	/**
	 * Gets the value of the version property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the value of the version property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVersion(String value) {
		this.version = value;
	}

	/**
	 * Gets the value of the created property.
	 * 
	 * @return possible object is {@link Long }
	 * 
	 */
	public Long getCreated() {
		return created;
	}

	/**
	 * Sets the value of the created property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	public void setCreated(Long value) {
		this.created = value;
	}

	/**
	 * Gets the value of the createdBy property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * Sets the value of the createdBy property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCreatedBy(String value) {
		this.createdBy = value;
	}

	/**
	 * Gets the value of the modified property.
	 * 
	 * @return possible object is {@link Long }
	 * 
	 */
	public Long getModified() {
		return modified;
	}

	/**
	 * Sets the value of the modified property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	public void setModified(Long value) {
		this.modified = value;
	}

	/**
	 * Gets the value of the modifiedBy property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * Sets the value of the modifiedBy property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setModifiedBy(String value) {
		this.modifiedBy = value;
	}

}
