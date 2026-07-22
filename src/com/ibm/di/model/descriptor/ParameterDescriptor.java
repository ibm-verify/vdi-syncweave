/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.model.descriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>
 * Java class for ParameterDescriptor complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;ParameterDescriptor&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;label&quot; type=&quot;{http://www.ibm.com/xmlns/prod/tdi/71/core}Label&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *         &lt;element name=&quot;description&quot; type=&quot;{http://www.ibm.com/xmlns/prod/tdi/71/core}Label&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *         &lt;element name=&quot;option&quot; type=&quot;{http://www.ibm.com/xmlns/prod/tdi/71/core}Option&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;defaultValue&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}anySimpleType&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;key&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}ID&quot; /&gt;
 *       &lt;attribute name=&quot;type&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;hidden&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}boolean&quot; default=&quot;false&quot; /&gt;
 *       &lt;attribute name=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}boolean&quot; default=&quot;false&quot; /&gt;
 *       &lt;attribute name=&quot;section&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; default=&quot;general&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@XmlType(name = "ParameterDescriptor", namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core", propOrder = { "label",
		"description", "option", "defaultValue", "script", "scriptLabel", "script2", "scriptLabel2" })
@XmlSeeAlso( { ModeParameterDescriptor.class })
public class ParameterDescriptor implements Serializable {

	private static final long serialVersionUID = -481825368157393077L;

	@XmlElement(required = true)
	protected List<Label> label;
	@XmlElement(required = true)
	protected List<Label> description;
	protected List<Option> option;
	@XmlSchemaType(name = "anySimpleType")
	protected Object defaultValue;
	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlID
	@XmlSchemaType(name = "ID")
	protected String key;
	@XmlAttribute(required = true)
	protected String type;
	@XmlAttribute
	protected Boolean hidden;
	@XmlAttribute
	protected Boolean required;
	@XmlAttribute
	protected String section;
	@XmlAttribute
	protected String modes;
	@XmlAttribute
	protected String script;
	@XmlElement
	protected List<Label> scriptLabel;
	@XmlAttribute
	protected String script2;
	@XmlElement
	protected List<Label> scriptLabel2;
	@XmlAttribute
	protected String panel;
	@XmlAttribute
	protected Boolean indexBased;
	@XmlAttribute
	protected String leadText;
	@XmlAttribute
	protected Boolean noLabel;

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

	/**
	 * Gets the value of the description property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the description property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDescription().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Label }
	 * 
	 * 
	 */
	public List<Label> getDescriptions() {
		if (description == null) {
			description = new ArrayList<Label>();
		}
		return this.description;
	}

	/**
	 * Gets the value of the option property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the option property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getOption().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Option }
	 * 
	 * 
	 */
	public List<Option> getOptions() {
		if (option == null) {
			option = new ArrayList<Option>();
		}
		return this.option;
	}

	/**
	 * Gets the value of the defaultValue property.
	 * 
	 * @return possible object is {@link Object }
	 * 
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Sets the value of the defaultValue property.
	 * 
	 * @param value
	 *            allowed object is {@link Object }
	 * 
	 */
	public void setDefaultValue(Object value) {
		this.defaultValue = value;
	}

	/**
	 * Gets the value of the key property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Sets the value of the key property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setKey(String value) {
		this.key = value;
	}

	/**
	 * Gets the value of the type property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the value of the type property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setType(String value) {
		this.type = value;
	}

	/**
	 * Gets the value of the hidden property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isHidden() {
		if (hidden == null) {
			return false;
		} else {
			return hidden;
		}
	}

	/**
	 * Sets the value of the hidden property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setHidden(Boolean value) {
		this.hidden = value;
	}

	/**
	 * Gets the value of the required property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isRequired() {
		if (required == null) {
			return false;
		} else {
			return required;
		}
	}

	/**
	 * Sets the value of the required property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setRequired(Boolean value) {
		this.required = value;
	}

	/**
	 * Gets the value of the section property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSection() {
		if (section == null) {
			return "general";
		} else {
			return section;
		}
	}

	/**
	 * Sets the value of the section property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSection(String value) {
		this.section = value;
	}

	/**
	 * Gets the value of the modes property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getModes() {
		return section;
	}

	/**
	 * Sets the value of the modes property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setModes(String value) {
		this.modes = value;
	}
	
	/**
	 * Getter/Setter for script/scriptLabel
	 */
	public void setScript(String script) {
		this.script = script;
	}
	
	public String getScript() {
		return this.script;
	}
	
	public List<Label> getScriptLabels() {
		if (scriptLabel == null) {
			scriptLabel = new ArrayList<Label>();
		}
		return this.scriptLabel;
	}
	
	/**
	 * Getter/Setter for script/scriptLabel
	 */
	public void setScript2(String script) {
		this.script2 = script;
	}
	
	public String getScript2() {
		return this.script2;
	}
	
	public List<Label> getScriptLabels2() {
		if (scriptLabel2 == null) {
			scriptLabel2 = new ArrayList<Label>();
		}
		return this.scriptLabel2;
	}

	public void setPanel(String panel) {
		this.panel = panel;
	}
	
	public String getPanel() {
		return this.panel;
	}

	/**
	 * Gets the value of the indexBased property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isIndexBased() {
		if (indexBased == null) {
			return false;
		} else {
			return indexBased;
		}
	}

	/**
	 * Sets the value of the indexBased property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setIndexBased(Boolean value) {
		this.indexBased = value;
	}

	public String getLeadText() {
		return leadText;
	}

	public void setLeadText(String leadText) {
		this.leadText = leadText;
	}

	public synchronized Boolean getNoLabel() {
		return noLabel;
	}

	public synchronized void setNoLabel(Boolean noLabel) {
		this.noLabel = noLabel;
	}

}
