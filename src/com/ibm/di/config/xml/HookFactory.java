/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.server.ResourceHash;

/**
 * Read/Write {@link HookConfig} elements in XML format
 *
 */
public class HookFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String HOOK_TAG = "Hooks";

	public final static String HOOK_ITEM_TAG = "Hook";

	public final static String HOOK_ITEM_NAME = "Name";

	public final static String HOOK_ITEM_SCRIPT = "Script";

	public final static String HOOK_ITEM_ENABLED = "Enabled";

	public final static String HOOK_ITEM_DEBUG = "DebugBreak";

	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {

		logmsg(sResHash.getString("MMCONFIG.HOOKFACTORY.PARSE", elem
				.getTagName()));
		HooksConfig cc = (HooksConfig) config;

		// Set name and inherit from
		getBaseName(cc, elem);

		// Get SchemaItem elements
		NodeList list = elem.getElementsByTagName(HOOK_ITEM_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			getHookItem((Element) list.item(i), cc);
		}

	}

	public void getHookItem(Element elem, HooksConfig config) throws Exception {

		logmsg(sResHash.getString("MMCONFIG.HOOKFACTORY.GETITEM", elem
				.getTagName()));

		String name = getNodeTextByName(elem, HOOK_ITEM_NAME);
		if (name == null) {
			throw new Exception(
					sResHash
							.getString("MMCONFIG.HOOKFACTORY.HOOK.ITEM.TAG.WITHOUT.A.NAME"));
		}
		HookConfig hc = config.getHook(name);

		String script = getNodeTextByName(elem, HOOK_ITEM_SCRIPT);
		if (script != null)
			hc.setScript(script);

		String enabled = getNodeTextByName(elem, HOOK_ITEM_ENABLED);
		if (enabled != null)
			hc.setEnabled(Boolean.valueOf(enabled).booleanValue());

		String debug = getNodeTextByName(elem, HOOK_ITEM_DEBUG);
		if (debug != null)
			hc.setDebugBreak(Boolean.valueOf(debug).booleanValue());

		String inheritFrom = getNodeTextByName(elem, INHERIT_TAG);
		if (inheritFrom != null)
			hc.setInheritsFromRef(inheritFrom);

		// User comment
		String str = getNodeTextByName(elem, USER_COMMENT_ATTRIBUTE);
		if (str != null && str.length() > 0)
			hc.setUserComment(str);
		
		config.setHook(hc);
		/*
		 * logmsg(sResHash.getString(
		 * "MMCONFIG.HOOKFACTORY.HOOK.NAME.ENABLED.DEBUG", new Object[] { name,
		 * enabled, debug }));
		 */

	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {

		HooksConfig cc = (HooksConfig) config;

		Element e = elem.getOwnerDocument().createElement(HOOK_TAG);
		elem.appendChild(e);

		// Set name and inherit from
		setBaseName(cc, e);

		for (String hook: config.getKeys(BaseConfiguration.SUBTREE)) {
			setHookItem(e, cc.getHook(hook));
		}
	}

	public void setHookItem(Element elem, HookConfig config) throws Exception {

		if (config.size() <= 1)
			return;

		Element p = elem.getOwnerDocument().createElement(HOOK_ITEM_TAG);

		// Set name and inherit from
		setBaseName(config, p);
		// Attribute name
		setSingleElement(p, HOOK_ITEM_NAME, config.getHookName().toString());
		setSingleElement(p, HOOK_ITEM_SCRIPT, config, InternalSchema.HC_SCRIPT);
		setSingleElement(p, HOOK_ITEM_ENABLED, config,
				InternalSchema.HC_ENABLED);
		setSingleElement(p, HOOK_ITEM_DEBUG, config, InternalSchema.DEBUG_BREAK);

		elem.appendChild(p);

	}
}
