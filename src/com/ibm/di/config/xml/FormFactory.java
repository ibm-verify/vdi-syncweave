/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ibm.di.config.base.FormItemConfigImpl;
import com.ibm.di.config.base.FormSectionImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.base.ValidatorConfigImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.FormSection;
import com.ibm.di.config.interfaces.ValidatorConfig;

/**
 * Read/write {@link FormConfig} elements in XML format.
 * 
 */
public class FormFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String FORM_TAG = "Form";
	public final static String FORM_ITEM_NAMES = "FormItemNames";
	public final static String FORM_ITEM_TAG = "FormItem";
	public final static String FORM_ITEM_VALUES = "Values";
	public final static String FORM_ITEM_LOCAL_VALUES = "LocalizedValues";
	public final static String FORM_SECTION_NAMES = "FormSectionNames";
	public final static String FORM_SECTION = "FormSection";
	public final static String LIST_ITEM = "ListItem";
	public final static String ITEM = "Item";
	public final static String KEY = "Key";
	public final static String VALUE = "Value";
	public final static String TRANSLATION = "TranslationFile";

	/**
	 * Validator tag.
	 */
	public final static String VALIDATORS_TAG = "Validators";

	/**
	 * Attribute type.
	 */
	public final static String ATTRIBUTE_TYPE = "type";

	/**
	 * Attribute name.
	 */
	public final static String ATTRIBUTE_NAME = "name";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {

		FormConfig form = (FormConfig) config;

		// Set name and inherit from
		getBaseName(form, elem);

		getList(elem, form.getFormItemNames(), FORM_ITEM_NAMES);
		getList(elem, form.getSectionNames(), FORM_SECTION_NAMES);

		// Get FormItems
		// We only want to get those directly below us, so we do not use getElementsbyTagName()
		for (Node n = elem.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && FORM_ITEM_TAG.equals(((Element)n).getTagName())) {
				form.addFormItem(getFormItem((Element) n));
			}
		}

		// Get FormSections
		NodeList list = elem.getElementsByTagName(FORM_SECTION);
		for (int i = 0; i < list.getLength(); i++) {
			form.addSection(getSection((Element) list.item(i)));
		}

		// Get Validators
		getValidators(form, elem);

		// Get general parameters
		getParameters(elem, form);

		// Translation
		String str = getNodeTextByName(elem, TRANSLATION);
		if (str != null && str.length() > 0)
			form.setTranslationFile(str);
	}

	/**
	 * Pull all validators from the provided XML element and add validator
	 * configuration for them in FormConfig.
	 * 
	 * @param form
	 *            where validator configurations will be added.
	 * @param element
	 *            XML element contains validators tags.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void getValidators(FormConfig form, Element element) throws Exception {
		NodeList list = element.getElementsByTagName(VALIDATORS_TAG);
		List<ValidatorConfig> validators = form.getValidators();
		ValidatorConfig validatorConfig = null;
		for (int i = 0; i < list.getLength(); i++) {
			NodeList validatorsList = ((Element) list.item(i)).getElementsByTagName(LIST_ITEM);
			for (int j = 0; j < validatorsList.getLength(); j++) {
				validatorConfig = new ValidatorConfigImpl();
				validatorConfig.setValidatorClass(getNodeText(validatorsList.item(j)));
				validatorConfig.setType(((Element) validatorsList.item(j)).getAttribute(ATTRIBUTE_TYPE));
				validatorConfig.setParameter(ATTRIBUTE_NAME, ((Element) validatorsList.item(j)).getAttribute(ATTRIBUTE_NAME));
				validators.add(validatorConfig);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void build(BaseConfiguration config, Element elem) throws Exception {

		FormConfig form = (FormConfig) config;

		// Set name and inherit from
		setBaseName(form, elem);

		setList(elem, form.getFormItemNames(), FORM_ITEM_NAMES);
		setList(elem, form.getSectionNames(), FORM_SECTION_NAMES);

		for (Iterator<String> i = form.getLocalFormItemNames(); i.hasNext();)
			setFormItem(elem, form.getFormItem(i.next()));

		// Set general parameters
		for (Iterator<Map.Entry<String, Object>> i = form.getData().entrySet()
				.iterator(); i.hasNext();) {
			Map.Entry<String, Object> mapEntry = i.next();
			String key = mapEntry.getKey();
			if (key.equals(InternalSchema.INHERITS_FROM) || key.equals("parameter"))
				continue;
			Object obj = mapEntry.getValue();
			if (obj instanceof String) {
				setParameter(elem, form, key);
			} else if (obj instanceof TreeMap) {
				setSection(elem, form.getSection(key));
			}
		}

		// Set Validators
		setValidators(form, elem);

		String str = form.getTranslationFile();
		if (str != null)
			setSingleElement(elem, TRANSLATION, str);
	}

	/**
	 * Put all validatos configuration in to provided XML element.
	 * 
	 * @param form
	 *            contains validator configurations.
	 * @param parentElement
	 *            XML element where validators will be added.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void setValidators(FormConfig form, Element parentElement) throws Exception {
		Element validatorElement = parentElement.getOwnerDocument().createElement(VALIDATORS_TAG);
		List<ValidatorConfig> validators = form.getValidators();
		String value = null;
		String attr = null;
		Element child = null;
		for (ValidatorConfig validator : validators) {
			value = validator.getValidatorClass();
			if (value != null && value.length() > 0) {
				child = createElement(validatorElement, value, LIST_ITEM);
			}
			attr = validator.getType();
			if (child != null && attr != null && attr.length() > 0) {
				child.setAttribute(ATTRIBUTE_TYPE, attr);
			}
			attr = validator.getStringParameter(ATTRIBUTE_NAME);
			if (child != null && attr != null && attr.length() > 0) {
				child.setAttribute(ATTRIBUTE_NAME, attr);
			}
			validatorElement.appendChild(child);
		}
		if (validatorElement.hasChildNodes()) {
			parentElement.appendChild(validatorElement);
		}
	}

	/**
	 * Create DOM Element.
	 * 
	 * @param parentElement
	 *            the parent element.
	 * @param value
	 *            the value of the element.
	 * @param elementTag
	 *            the tag of the element.
	 * @return child DOM element.
	 */
	private Element createElement(Element parentElement, String value, String elementTag) {
		Text text;
		if (value.indexOf("\n") != -1) {
			text = parentElement.getOwnerDocument().createCDATASection(value.replaceAll("\r", ""));
		} else {
			text = parentElement.getOwnerDocument().createTextNode(value);
		}

		Element child = parentElement.getOwnerDocument().createElement(elementTag);
		child.appendChild(text);
		return child;
	}

	public void getList(Element p, List<String> list, String tag) throws Exception {

		Element param = getSingleElement(p, tag);
		if (param == null)
			return;

		NodeList nodes = param.getElementsByTagName(LIST_ITEM);
		for (int i = 0; i < nodes.getLength(); i++)
			list.add(getNodeText(nodes.item(i)));
	}

	public void setList(Element p, List<String> list, String tag)
			throws Exception {
		if (list == null || list.size() == 0)
			return;

		Element param = p.getOwnerDocument().createElement(tag);
		p.appendChild(param);

		for (int i = 0; i < list.size(); i++)
			setSingleElement(param, LIST_ITEM, list.get(i));
	}

	public Map<String, String> getMap(Element p, String tag) throws Exception {

		Element elem = getSingleElement(p, tag);
		if (elem == null)
			return null;

		Map<String, String> map = new TreeMap<String, String>();

		NodeList list = elem.getElementsByTagName(ITEM);
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String key = getNodeText(getSingleElement(e, KEY));
			String val = getNodeText(getSingleElement(e, VALUE));
			map.put(key, val);
		}
		return map;
	}

	public void setMap(Element p, Map<String, String> map, String tag)
			throws Exception {
		if (map == null || map.size() == 0)
			return;

		Element elem = p.getOwnerDocument().createElement(tag);
		p.appendChild(elem);

		for (Iterator<Map.Entry<String, String>> i = map.entrySet().iterator(); i
				.hasNext();) {
			Map.Entry<String, String> mapEntry = i.next();
			Element item = p.getOwnerDocument().createElement(ITEM);
			setSingleElement(item, KEY, mapEntry.getKey());
			setSingleElement(item, VALUE, mapEntry.getValue());
			elem.appendChild(item);
		}
	}

	private FormItemConfig getFormItem(Element elem) throws Exception {
		FormItemConfig item = new FormItemConfigImpl();

		getBaseName(item, elem);

		getList(elem, item.getValues(), FORM_ITEM_VALUES);

		Map<String, String> map = getMap(elem, FORM_ITEM_LOCAL_VALUES);
		if (map != null)
			item.setLocalizedValues(map);

		// Get general parameters
		getParameters(elem, item);

		return item;
	}

	@SuppressWarnings("unchecked")
	private void setFormItem(Element elem, FormItemConfig item)
			throws Exception {
		if (item == null)
			return;
		Element e = elem.getOwnerDocument().createElement(FORM_ITEM_TAG);
		elem.appendChild(e);

		setBaseName(item, e);

		setList(e, item.getValues(), FORM_ITEM_VALUES);

		// Save localized values
		Object o = item.getParameterRaw(InternalSchema.FORM_LOCALIZEDVALUES);
		if (o instanceof Map)
			setMap(e, (Map<String, String>) o, FORM_ITEM_LOCAL_VALUES);

		// Set general parameters
		for (Iterator<Map.Entry<String, Object>> i = item.getData().entrySet()
				.iterator(); i.hasNext();) {
			Map.Entry<String, Object> mapEntry = i.next();
			String key = mapEntry.getKey();
			if (key.equals(InternalSchema.INHERITS_FROM)
					|| key.equals(InternalSchema.FORM_VALUES)
					|| key.equals(InternalSchema.FORM_LOCALIZEDVALUES))
				continue;
			Object obj = mapEntry.getValue();
			if (obj instanceof String)
				setParameter(e, item, key);
		}
	}

	public FormSection getSection(Element elem) throws Exception {

		FormSection section = new FormSectionImpl();

		// Set name and inherit from
		getBaseName(section, elem);

		getList(elem, section.getNames(), FORM_SECTION_NAMES);

		// Get FormItems
		NodeList list = elem.getElementsByTagName(FORM_ITEM_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			section.addFormItem(getFormItem((Element) list.item(i)));
		}

		// Get general parameters
		getParameters(elem, section);

		return section;
	}

	@SuppressWarnings("unchecked")
	public void setSection(Element p, FormSection section) throws Exception {
		if (section == null)
			return;

		Element elem = p.getOwnerDocument().createElement(FORM_SECTION);
		p.appendChild(elem);

		// Set name and inherit from
		setBaseName(section, elem);

		setList(elem, section.getNames(), FORM_SECTION_NAMES);

		for (Iterator<FormItemConfig> i = section.getFormItems().iterator(); i
				.hasNext();)
			setFormItem(elem, i.next());

		// Set general parameters
		for (Iterator<Map.Entry<String, Object>> i = section.getData()
				.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, Object> mapEntry = i.next();
			String key = mapEntry.getKey();
			if (key.equals(InternalSchema.INHERITS_FROM) || key.equals("parameterlist"))
				continue;
			Object obj = mapEntry.getValue();
			if (obj instanceof String)
				setParameter(elem, section, key);
		}

	}

}