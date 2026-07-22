/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.ibm.di.config.bind.XMLGregorianCalendarAdapter;

/**
 * <p>
 * Java class for Error complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="Error">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="created" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="code" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="msgId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="details" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="detail" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}ErrorDetail" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="exception" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}Exception" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Error", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = { "created", "code", "msgId",
		"details", "exception" })
@XmlRootElement(name = "error", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class Error {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", required = true)
	@XmlJavaTypeAdapter(value = XMLGregorianCalendarAdapter.class)
	protected Long created;
	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", required = true)
	protected String code;
	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected String msgId;
	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected Error.Details details;
	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected Exception exception;

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
	 * Gets the value of the code property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the value of the code property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCode(String value) {
		this.code = value;
	}

	/**
	 * Gets the value of the msgId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMsgId() {
		return msgId;
	}

	/**
	 * Sets the value of the msgId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMsgId(String value) {
		this.msgId = value;
	}

	/**
	 * Gets the value of the details property.
	 * 
	 * @return possible object is {@link Error.Details }
	 * 
	 */
	public Error.Details getDetails() {
		return details;
	}

	/**
	 * Sets the value of the details property.
	 * 
	 * @param value
	 *            allowed object is {@link Error.Details }
	 * 
	 */
	public void setDetails(Error.Details value) {
		this.details = value;
	}

	/**
	 * Gets the value of the exception property.
	 * 
	 * @return possible object is {@link Exception }
	 * 
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * Sets the value of the exception property.
	 * 
	 * @param value
	 *            allowed object is {@link Exception }
	 * 
	 */
	public void setException(Exception value) {
		this.exception = value;
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content contained
	 * within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="detail" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}ErrorDetail" maxOccurs="unbounded" minOccurs="0"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "detail" })
	public static class Details {

		@XmlElement(name = "detail", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
		protected List<ErrorDetail> detail;

		/**
		 * Gets the value of the detail property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the detail property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getDetail().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link ErrorDetail }
		 * 
		 * 
		 */
		public List<ErrorDetail> getDetail() {
			if (detail == null) {
				detail = new ArrayList<ErrorDetail>();
			}
			return this.detail;
		}

	}

}
