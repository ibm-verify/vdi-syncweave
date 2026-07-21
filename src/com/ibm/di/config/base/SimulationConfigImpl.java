/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.SimulationConfig;

/**
 * 
 * This class is used to configure the AssemblyLine's simulation mode.
 * 
 */
public class SimulationConfigImpl extends BaseConfigurationImpl implements
		SimulationConfig {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 9183168548485157621L;

	private BaseConfiguration proxySettings = null;

	public SimulationConfigImpl() {
		super();
	}

	public SimulationConfigImpl(Object config) {
		super(config);
	}

	public void init() throws Exception {

		// ProxyALSettings
		if (proxySettings == null) {
			proxySettings = new BaseConfigurationImpl(getParameter(
					InternalSchema.AL_SETTINGS, new TreeMap<Object, Object>()));
		}
		proxySettings.setParent(this);
	}

	public String getComponentSimState(String name) {

		// get the state from previosly saved configuration
		String state = getStringParameter(name);

		if (state == null) {
			// get the simulate parameter's value from the idi.inf

			BaseConfiguration cfg = ((AssemblyLineConfig) getParent())
					.getComponent(name);
			if (cfg instanceof FunctionConfig) {
				state = ((FunctionConfig) cfg).getFunctionConfig()
						.getStringParameter("simulate");
			} else if (cfg instanceof ConnectorConfig) {
				state = ((ConnectorConfig) cfg).getConnectionConfig()
						.getStringParameter("simulate");
			}

			if (state != null) {
				// this means that the simulate parameter was found
				setComponentSimState(name, state);

			}
		}
		if (state == null) {
			// check the component's state
			BaseConfiguration bc = ((AssemblyLineConfig) getParent())
					.getComponent(name);

			if (bc instanceof FunctionConfig) {
				// If we are here this means that no state was found in the
				// FunctionConfig section of idi.inf and we should set the
				// default simulation state to Simulated since FCs not market
				// explicitly as safe (in the idi.inf) are treated as dangerous
				state = SIM_SIMULATED_STATE;
				setComponentSimState(name, state);
			} else if (bc instanceof ConnectorConfig) {
				String mode = ((ConnectorConfig) bc).getMode();
				if (ConnectorConfig.ADDONLY_MODE.equalsIgnoreCase(mode)
						|| ConnectorConfig.UPDATE_MODE.equalsIgnoreCase(mode)
						|| ConnectorConfig.DELETE_MODE.equalsIgnoreCase(mode)
						|| ConnectorConfig.DELTA_MODE.equalsIgnoreCase(mode)) {

					// if we are here then our component is a connector and is
					// in one of the dangerous modes so set its simulateion
					// state to Simulated
					state = SIM_SIMULATED_STATE;
					setComponentSimState(name, state);
				} else {
					state = SIM_ENABLED_STATE;
					setComponentSimState(name, state);
				}
			} else {
				state = SIM_ENABLED_STATE;
				setComponentSimState(name, state);
			}
		}
		return state;
	}

	public HookConfig getHook(String name) {

		BaseConfiguration cc = ((AssemblyLineConfig) getParent())
				.getComponent(name);

		if (cc instanceof ConnectorConfig) {
			return ((ConnectorConfig) cc).getHooks()
					.getHook(SIMULATE_HOOK_NAME);
		}

		return null;
	}

	public String getProxyALName() {
		return proxySettings.getStringParameter(AL_SIMULATE_PROXY_NAME);
	}

	public void setComponentSimState(String name, String state) {

		setStringParameter(name, state);

	}

	public void setProxyALName(String name) {
		proxySettings.setStringParameter(AL_SIMULATE_PROXY_NAME, name);
	}

	public String getProxyALConfigInstance() {
		return proxySettings.getStringParameter(AL_SIMULATE_PROXY_CONFIG);
	}

	public String getProxyALServer() {
		return proxySettings.getStringParameter(AL_SIMULATE_PROXY_SERVER);
	}

	public void setProxyALConfigInstance(String config) {
		proxySettings.setStringParameter(AL_SIMULATE_PROXY_CONFIG, config);

	}

	public void setProxyALServer(String server) {
		proxySettings.setStringParameter(AL_SIMULATE_PROXY_SERVER, server);

	}

	public int getProxyALMode() {
		return proxySettings.getIntegerParameter(AL_SIMULATE_PROXY_MODE, 0);
	}

	public void setProxyALMode(int mode) {
		proxySettings.setIntegerParameter(AL_SIMULATE_PROXY_MODE, mode);

	}

	public boolean getProxyALDebug() {
		return proxySettings
				.getBooleanParameter(AL_SIMULATE_PROXY_DEBUG, false);
	}

	public void setProxyALDebug(boolean debug) {
		proxySettings.setBooleanParameter(AL_SIMULATE_PROXY_DEBUG, debug);

	}

	public BaseConfiguration getProxySettings() throws Exception {
		if (proxySettings == null) {
			proxySettings = new BaseConfigurationImpl(getParameter(
					InternalSchema.AL_SETTINGS, new TreeMap<Object, Object>()));
		}
		proxySettings.setParent(this);
		proxySettings.init();
		return proxySettings;
	}

	public AssemblyLineConfig createOrUpdateProxyAL() throws Exception {

		AssemblyLineConfig proxyAL = null;
		try {
			proxyAL = getMetamergeConfig().getAssemblyLine(getProxyALName());
		} catch (Exception e) {
			// something wrong happened while trying to lookup the AL or it
			// simply does not exists. Anyway go ahead and try to create it
		}

		AssemblyLineConfig issuerAL = ((AssemblyLineConfig) getParent());

		if (proxyAL == null) {
			proxyAL = new AssemblyLineConfigImpl();
			proxyAL.setName(getProxyALName());
			proxyAL.init();
		}

		// flatten list of all the component configs that belong to the
		// parentAL
		List<BaseConfiguration> components = new ArrayList<BaseConfiguration>();
		issuerAL.getEntryFeedComponents().getConfigurations(
				issuerAL.getDataFlowComponents().getConfigurations(components));

		for (int i = 0; i < components.size(); i++) {

			String compName = ((BaseConfiguration) components.get(i))
					.getShortName();
			if (SIM_PROXY_STATE.equals(getComponentSimState(compName))) {

				String[] method = null;
				if (components.get(i) instanceof FunctionConfig) {
					method = METHODS_FC_PERFORM;
				} else if (components.get(i) instanceof ConnectorConfig) {
					ConnectorConfig cfg = (ConnectorConfig) components.get(i);
					if (ConnectorConfig.ADDONLY_MODE.equals(cfg.getMode())) {
						method = METHODS_CON_ADDONLY;
					} else if (ConnectorConfig.UPDATE_MODE
							.equals(cfg.getMode())) {
						method = METHODS_CON_UPDATE;
					} else if (ConnectorConfig.DELETE_MODE
							.equals(cfg.getMode())) {
						method = METHODS_CON_DELETE;
					} else if (ConnectorConfig.DELTA_MODE.equals(cfg.getMode())) {
						method = METHODS_CON_DELTA;
					} else if (ConnectorConfig.ITERATOR_MODE.equals(cfg
							.getMode())) {
						method = METHODS_CON_ITERATOR;
					} else if (ConnectorConfig.REPLY_MODE.equals(cfg.getMode())) {
						method = METHODS_CON_REPLY;
					} else if (ConnectorConfig.CALL_REPLY_MODE.equals(cfg
							.getMode())) {
						method = METHODS_CON_CALL_REPLY;
					} else if (ConnectorConfig.SERVER_MODE
							.equals(cfg.getMode())) {
						method = METHODS_CON_SERVER;
					} else if (ConnectorConfig.LOOKUP_MODE
							.equals(cfg.getMode())) {
						method = METHODS_CON_LOOKUP;
					}
				} else {
					// Only FCs and some Connectors can use proxyAL
					continue;
				}

				BaseConfiguration comp = proxyAL.getComponent(compName);
				BranchingConfig bc = null;
				if (comp == null) {
					bc = new BranchingConfigImpl();
					bc.init();
					bc.setName(compName);
					bc.setBranchType(BranchingConfig.BRANCH_IF);
					bc
							.setScript("ret.value = "
									+ "task.getOpEntry().getString(\"$operation\").equals(\""
									+ compName + "\");");
					bc.setScriptEngine("javascript");
					proxyAL.addComponent(bc);

				} else if (comp instanceof BranchingConfig) {
					bc = (BranchingConfig) comp;
				} else {
					// exists but it is probably something else that the user
					// have modified so don't touch it
					continue;
				}

				// till here we should have a branch
				// check if the op exists if not create it
				if (!proxyAL.getOperations().containsConfig(compName, false)) {
					proxyAL.createOperation(compName);
				}

				for (int j = 0; method != null && j < method.length; j++) {
					String methName = compName + "_" + method[j];

					BaseConfiguration meth = bc.getConfig(methName);
					BranchingConfig mb = null;

					if (meth == null) {
						mb = new BranchingConfigImpl();
						mb.init();
						mb.setName(methName);
						mb.setBranchType(BranchingConfig.BRANCH_IF);
						mb
								.setScript("ret.value = "
										+ "task.getOpEntry().getString(\"$method\").equals(\""
										+ method[j] + "\");");
						mb.setScriptEngine("javascript");
						bc.addConfig(mb);
					} else {
						// there is something there with the same name but we
						// don't care (*_*)
						continue;
					}
				}
			}
		}

		// if no errors this should be either a new ALConfig instance or a
		// reference to an existing ALConfig
		return proxyAL;
	}
}
