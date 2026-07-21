/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ALExecutionScheduleBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ALExecutionScheduleBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALStarterBinding">
 *       &lt;attribute name="skipExecIfAlRunning" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="scheduleCancelOnAlFailure" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="execTimePattern" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="failureAl" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ALExecutionScheduleBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlRootElement(name = "scheduleAl", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class ALExecutionScheduleBinding extends ALStarterBinding {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = -6795001076297905095L;

	@XmlAttribute
	protected Boolean skipExecIfAlRunning;
	@XmlAttribute
	protected Boolean cancelScheduleOnAlFailure;
	@XmlAttribute(required = true)
	protected String execTimePattern;
	@XmlAttribute
	protected String failureAl;
	@XmlAttribute
	protected Boolean enabled;

	/**
	 * Gets the value of the skipExecIfAlRunning property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isSkipExecIfAlRunning() {
		if (skipExecIfAlRunning == null) {
			return true;
		} else {
			return skipExecIfAlRunning;
		}
	}

	/**
	 * Sets the value of the skipExecIfAlRunning property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setSkipExecIfAlRunning(Boolean value) {
		this.skipExecIfAlRunning = value;
	}

	/**
	 * Gets the value of the cancelScheduleOnAlFailure property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isCancelScheduleOnAlFailure() {
		if (cancelScheduleOnAlFailure == null) {
			return false;
		} else {
			return cancelScheduleOnAlFailure;
		}
	}

	/**
	 * Sets the value of the cancelScheduleOnAlFailure property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setCancelScheduleOnAlFailure(Boolean value) {
		this.cancelScheduleOnAlFailure = value;
	}

	/**
	 * Gets the value of the execTimePattern property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getExecTimePattern() {
		return execTimePattern;
	}

	/**
	 * Sets the value of the execTimePattern property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setExecTimePattern(String value) {
		this.execTimePattern = value;
	}

	/**
	 * Gets the value of the failureAl property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getFailureAl() {
		return failureAl;
	}

	/**
	 * Sets the value of the failureAl property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setFailureAl(String value) {
		this.failureAl = value;
	}

    /**
     * Gets the value of the enabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEnabled() {
        if (enabled == null) {
            return true;
        } else {
            return enabled;
        }
    }

    /**
     * Sets the value of the enabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnabled(Boolean value) {
        this.enabled = value;
    }
}
