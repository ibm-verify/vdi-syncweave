/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.SimulationConfig;

/**
 * Read/Write {@link SimulationConfig} elements in XML
 */
public class SimulationFactory extends Factories {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String SIMULATE_TAG = "SimulationConfig";

	public static final String PROXY_SETTINGS = "ProxySettings";

	public static final String SIMULATION_STATES = "SimulationStates";

	public static final String COMPONENT_TAG = "Component";

	public static final String COMPONENT_NAME_ATTR = "name";

	public static final String COMPONENT_STATE_ATTR = "state";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {

		Element e = null;
		SimulationConfig cfg = (SimulationConfig) config;

		cfg.init();
		getBaseName(cfg, elem);

		// Components SimulationState
		e = getSingleElement(elem, SIMULATION_STATES);
		if (e != null) {
			Node component = e.getFirstChild();
			while (component != null) {

				if (component.getNodeType() == Node.ELEMENT_NODE) {
					String name = ((Element) component)
							.getAttribute(COMPONENT_NAME_ATTR);
					String state = ((Element) component)
							.getAttribute(COMPONENT_STATE_ATTR);
					if (name != null && name.length() > 0 && state != null
							&& state.length() > 0)
						cfg.setComponentSimState(name, state);
				}

				component = component.getNextSibling();
			}
		}

		// ProxySettings
		if ((e = getSingleElement(elem, PROXY_SETTINGS)) != null)
			getParameters(e, cfg.getProxySettings());
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {

		SimulationConfig cfg = (SimulationConfig) config;

		setBaseName(cfg, elem);

		// Components simulation states
		Element states = elem.getOwnerDocument().createElement(
				SIMULATION_STATES);

		AssemblyLineConfig alc = (AssemblyLineConfig) cfg.getParent();

		// get all the component configs
		List<BaseConfiguration> components = new ArrayList<BaseConfiguration>();
		alc.getEntryFeedComponents().getConfigurations(
				alc.getDataFlowComponents().getConfigurations(components));

		for (int i = 0; i < components.size(); i++) {
			String name = components.get(i).getShortName();

			Element comp = states.getOwnerDocument().createElement(
					COMPONENT_TAG);

			comp.setAttribute(COMPONENT_NAME_ATTR, name);
			comp.setAttribute(COMPONENT_STATE_ATTR, cfg
					.getComponentSimState(name));
			states.appendChild(comp);
		}

		elem.appendChild(states);

		// ProxyAL Settings
		setParameters(elem, cfg.getProxySettings(), PROXY_SETTINGS);
	}
}
