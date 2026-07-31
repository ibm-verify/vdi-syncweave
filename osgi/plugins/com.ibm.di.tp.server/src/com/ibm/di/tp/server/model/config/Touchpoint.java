/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref=&quot;{http://www.ibm.com/xmlns/prod/scmp}admin-state&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element ref=&quot;{http://www.ibm.com/xmlns/prod/scmp}propertySheet&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "adminState", "touchpointID", "version", "propertySheet" })
@XmlRootElement(name = "touchpoint")
public class Touchpoint {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(name = "admin-state")
	private EnumAdminState adminState;

	@XmlElement(required = true)
	private PropertySheet propertySheet;

	@XmlElement(required = true)
	private String touchpointID;

	@XmlElement(required = true)
	private String version;

	/**
	 * Gets the value of the adminState property.
	 * 
	 * @return possible object is {@link EnumAdminState }
	 * 
	 */
	public EnumAdminState getAdminState() {
		if (adminState == null) {
			adminState = EnumAdminState.ENABLED;
		}
		return adminState;
	}

	/**
	 * Sets the value of the adminState property.
	 * 
	 * @param value
	 *            allowed object is {@link EnumAdminState }
	 * 
	 */
	public void setAdminState(EnumAdminState value) {
		this.adminState = value;
	}

	/**
	 * Gets the value of the propertySheet property.
	 * 
	 * @return possible object is {@link PropertySheet }
	 * 
	 */
	public PropertySheet getPropertySheet() {
		if (propertySheet == null) {
			propertySheet = new PropertySheet();
		}
		return propertySheet;
	}

	/**
	 * Sets the value of the propertySheet property.
	 * 
	 * @param value
	 *            allowed object is {@link PropertySheet }
	 * 
	 */
	public void setPropertySheet(PropertySheet value) {
		this.propertySheet = value;
	}

	/**
	 * @param touchpointID
	 *            the touchpointID to set
	 */
	public void setTouchpointID(String touchpointID) {
		this.touchpointID = touchpointID;
	}

	/**
	 * @return the touchpointID
	 */
	public String getTouchpointID() {
		return touchpointID;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

}
