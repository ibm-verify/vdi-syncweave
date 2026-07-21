/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.error.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.handler.error.data.Error.Details.Detail;

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
 *         &lt;element name="creation-time" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="code">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;pattern value="[0-9]{6}"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="details" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="detail" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="native-msgid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="summary" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Error", namespace = Constants.NS_SCMP, propOrder = { "creationTime", "code", "details", "nativeMsgid", "summary" })
@XmlRootElement(name = "error")
public class Error {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(name = "creation-time", required = true, namespace = Constants.NS_SCMP)
	protected XMLGregorianCalendar creationTime;
	@XmlElement(required = true, namespace = Constants.NS_SCMP)
	protected String code;
	protected Error.Details details;
	@XmlElement(name = "native-msgid", namespace = Constants.NS_SCMP)
	protected String nativeMsgid;
	@XmlElement(namespace = Constants.NS_SCMP)
	protected String summary;

	/**
	 * Gets the value of the creationTime property.
	 * 
	 * @return possible object is {@link XMLGregorianCalendar }
	 * 
	 */
	public XMLGregorianCalendar getCreationTime() {
		return creationTime;
	}

	/**
	 * Sets the value of the creationTime property.
	 * 
	 * @param value
	 *            allowed object is {@link XMLGregorianCalendar }
	 * 
	 */
	public void setCreationTime(XMLGregorianCalendar value) {
		this.creationTime = value;
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
	 * Gets the value of the details property.
	 * 
	 * @return possible object is {@link Error.Details }
	 * 
	 */
	public List<Detail> getDetails() {
		if (details == null) {
			details = new Details();
		}
		return details.getDetail();
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
	 * Gets the value of the summary property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * Sets the value of the summary property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSummary(String value) {
		this.summary = value;
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
	 *         &lt;element name="detail" maxOccurs="unbounded">
	 *           &lt;complexType>
	 *             &lt;complexContent>
	 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                 &lt;sequence>
	 *                   &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *                   &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *                 &lt;/sequence>
	 *               &lt;/restriction>
	 *             &lt;/complexContent>
	 *           &lt;/complexType>
	 *         &lt;/element>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "detail" }, namespace = Constants.NS_SCMP)
	public static class Details {

		@XmlElement(name = "detail", required = true, namespace = Constants.NS_SCMP)
		protected List<Error.Details.Detail> detail;

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
		 * {@link Error.Details.Detail }
		 * 
		 * 
		 */
		public List<Error.Details.Detail> getDetail() {
			if (detail == null) {
				detail = new ArrayList<Error.Details.Detail>();
			}
			return this.detail;
		}

		/**
		 * <p>
		 * Java class for anonymous complex type.
		 * 
		 * <p>
		 * The following schema fragment specifies the expected content
		 * contained within this class.
		 * 
		 * <pre>
		 * &lt;complexType>
		 *   &lt;complexContent>
		 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
		 *       &lt;sequence>
		 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
		 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string"/>
		 *       &lt;/sequence>
		 *     &lt;/restriction>
		 *   &lt;/complexContent>
		 * &lt;/complexType>
		 * </pre>
		 * 
		 * 
		 */
		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "", propOrder = { "name", "value" }, namespace = "http://www.ibm.com/xmlns/prod/scmp")
		public static class Detail {

			@XmlElement(required = true, namespace = Constants.NS_SCMP)
			protected String name;
			@XmlElement(required = true, namespace = Constants.NS_SCMP)
			protected String value;

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

			/**
			 * Gets the value of the value property.
			 * 
			 * @return possible object is {@link String }
			 * 
			 */
			public String getValue() {
				return value;
			}

			/**
			 * Sets the value of the value property.
			 * 
			 * @param value
			 *            allowed object is {@link String }
			 * 
			 */
			public void setValue(String value) {
				this.value = value;
			}
		}

	}

}
