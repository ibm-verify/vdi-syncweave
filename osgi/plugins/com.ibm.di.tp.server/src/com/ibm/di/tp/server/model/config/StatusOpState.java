/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.tp.server.Constants;

/**
 * <p>
 * Java class for status_OpState complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="status_OpState">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="op-state" type="{http://www.ibm.com/xmlns/prod/scmp}enum_OpState"/>
 *         &lt;element name="common-msgid" type="{http://www.ibm.com/xmlns/prod/scmp}type_CommonMsgID" minOccurs="0"/>
 *         &lt;element name="native-msgid" type="{http://www.ibm.com/xmlns/prod/scmp}type_NativeMsgID" minOccurs="0"/>
 *         &lt;element name="reason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "status_OpState", namespace = Constants.NS_SCMP, propOrder = { "opState", "commonMsgid", "nativeMsgid", "reason" })
public class StatusOpState {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	
	@XmlElement(name = "op-state", namespace = "", required = true)
	protected EnumOpState opState;
	@XmlElement(name = "common-msgid", namespace = "")
	protected String commonMsgid;
	@XmlElement(name = "native-msgid", namespace = "")
	protected String nativeMsgid;
	@XmlElement(namespace = "")
	protected String reason;

	/**
	 * Gets the value of the opState property.
	 * 
	 * @return possible object is {@link EnumOpState }
	 * 
	 */
	public EnumOpState getOpState() {
		return opState;
	}

	/**
	 * Sets the value of the opState property.
	 * 
	 * @param value
	 *            allowed object is {@link EnumOpState }
	 * 
	 */
	public void setOpState(EnumOpState value) {
		this.opState = value;
	}

	/**
	 * Gets the value of the commonMsgid property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCommonMsgid() {
		return commonMsgid;
	}

	/**
	 * Sets the value of the commonMsgid property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCommonMsgid(String value) {
		this.commonMsgid = value;
	}

	/**
	 * Gets the value of the nativeMsgid property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNativeMsgid() {
		return nativeMsgid;
	}

	/**
	 * Sets the value of the nativeMsgid property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNativeMsgid(String value) {
		this.nativeMsgid = value;
	}

	/**
	 * Gets the value of the reason property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Sets the value of the reason property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setReason(String value) {
		this.reason = value;
	}

}
