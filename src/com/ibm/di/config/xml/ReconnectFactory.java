/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.config.base.ReconnectConfigImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.ReconnectConfig;
import com.ibm.di.config.interfaces.ReconnectRuleConfig;
import com.ibm.di.function.SystemFunctions;

/**
 * Read/write a ReconnectConfig.
 *
 * @since 7.0
 */
public class ReconnectFactory extends Factories {
	
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/** XML element that contains a ReconnectConfig. */
	public final static String RECONNECT_TAG = "Reconnect";
	
	/** XML element that contains a list of reconnect rules. */
	public final static String RECONNECT_RULES_TAG = "ReconnectRules";
	
	/** XML element that describes a single reconnect rule. */
	public final static String RULE_TAG = "Rule";
	
	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {
		
		ReconnectConfig rc = (ReconnectConfig) config;
		rc.init();
		
		getBaseName(rc, elem);
		getParameters(elem, rc);
		
		migrateConfig(rc);
		
		// Parse the reconnect rules
		Element rrElem = getSingleElement(elem, RECONNECT_RULES_TAG);
		if (rrElem != null) {
			
			NodeList list = rrElem.getElementsByTagName(RULE_TAG);
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (node.getParentNode() == rrElem) {
					
					Element ruleElem = (Element) list.item(i);
					ReconnectRuleConfig rule = rc.newReconnectRule();
					getParameters(ruleElem, rule);
					rule.validate();
				}
			}
		}
	}
	
	/**
	 * Migrate parameters to new version.
	 * @param config
	 * @since 7.1
	 */
	private void migrateConfig(ReconnectConfig config) {
		MetamergeConfig mc = config.getMetamergeConfig();
		if (mc == null)
			return;
		String configVersion = mc.getConfigVersion();
		if ("6.1.1".equals(configVersion) || "7.0".equals(configVersion)) {
			if ("false".equals(config.getParameterRaw(ReconnectConfigImpl.INIT_RECONNECT)) &&
					"1".equals(config.getParameterRaw(ReconnectConfigImpl.NUMBER_OF_RETRIES)) &&
					"10".equals(config.getParameterRaw(ReconnectConfigImpl.RETRY_DELAY)) &&
					! config.getAutoReconnect() ) {
				// These parameters were accidentally given a value in earlier versions.
				config.removeParameter(ReconnectConfigImpl.INIT_RECONNECT);
				config.removeParameter(ReconnectConfigImpl.NUMBER_OF_RETRIES);
				config.removeParameter(ReconnectConfigImpl.RETRY_DELAY);
			}
			return;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {
		
		setBaseName(config, elem);
		setParameters(elem, config, null);
		
		ReconnectConfig rc = (ReconnectConfig) config;
		
		// Write the reconnect rules
		Element rrElem = elem.getOwnerDocument().createElement(RECONNECT_RULES_TAG);
		elem.appendChild(rrElem);
		
		ContainerConfig reconnectRules = rc.getReconnectRules();
		for (int i = 0; i < reconnectRules.size(); ++i) {
			
			Element ruleElem = elem.getOwnerDocument().createElement(RULE_TAG);
			
			setParameters(ruleElem, reconnectRules.getConfig(i), null);
			
			rrElem.appendChild(ruleElem);
		}
	}
}
