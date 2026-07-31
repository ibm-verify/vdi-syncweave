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
 *     &lt;extension base=&quot;{http://www.ibm.com/xmlns/prod/tdi/71/core}ComponentDescriptor&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;supportedModes&quot;&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name=&quot;mode&quot; type=&quot;{http://www.ibm.com/xmlns/prod/tdi/71/core}ModeOption&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name=&quot;useParser&quot; type=&quot;{http://www.ibm.com/xmlns/prod/tdi/71/core}UseParserEnum&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "supportedModes", "useParser" })
@XmlRootElement(name = "connectorDescriptor")
public class ConnectorDescriptor extends ComponentDescriptor {

	private static final long serialVersionUID = 6199189144298547195L;

	@XmlElement(required = true)
	protected ConnectorDescriptor.SupportedModes supportedModes;
	@XmlElement(defaultValue = "prohibit")
	protected UseParserEnum useParser;

	/**
	 * Gets the value of the supportedModes property.
	 * 
	 * @return possible object is {@link ConnectorDescriptor.SupportedModes }
	 * 
	 */
	public List<ModeOption> getSupportedModes() {
		if (supportedModes == null) {
			supportedModes = new SupportedModes();
		}
		return supportedModes.getMode();
	}

	/**
	 * Gets the value of the useParser property.
	 * 
	 * @return possible object is {@link UseParserEnum }
	 * 
	 */
	public UseParserEnum getUseParser() {
		return useParser;
	}

	/**
	 * Sets the value of the useParser property.
	 * 
	 * @param value
	 *            allowed object is {@link UseParserEnum }
	 * 
	 */
	public void setUseParser(UseParserEnum value) {
		this.useParser = value;
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
	 * &lt;complexType&gt;
	 *   &lt;complexContent&gt;
	 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
	 *       &lt;sequence&gt;
	 *         &lt;element name=&quot;mode&quot; type=&quot;{http://www.ibm.com/xmlns/prod/tdi/71/core}ModeOption&quot; maxOccurs=&quot;unbounded&quot;/&gt;
	 *       &lt;/sequence&gt;
	 *     &lt;/restriction&gt;
	 *   &lt;/complexContent&gt;
	 * &lt;/complexType&gt;
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "mode" })
	public static class SupportedModes implements Serializable {

		private static final long serialVersionUID = 4244128257237182637L;
		
		@XmlElement(required = true)
		protected List<ModeOption> mode;

		/**
		 * Gets the value of the mode property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the mode property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getMode().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link ModeOption }
		 * 
		 * 
		 */
		public List<ModeOption> getMode() {
			if (mode == null) {
				mode = new ArrayList<ModeOption>();
			}
			return this.mode;
		}
	}
}
