/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ReconnectConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.ReconnectRuleConfig;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.ContainerConfigImpl;

import java.util.TreeMap;

/**
 * Implements (@link ReconnectConfig}
 */
public class ReconnectConfigImpl extends BaseConfigurationImpl implements
		ReconnectConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private final static String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -7935628947261477628L;
	
	public static final String AUTO_RECONNECT = "autoreconnect";
	public static final String INIT_RECONNECT = "initreconnect";
	public static final String NUMBER_OF_RETRIES = "numberOfRetries";
	public static final String RETRY_DELAY = "retryDelay";
	public static final String SKIP_FORWARD = "skipForwardAfterReconnect";
	
	private ContainerConfig reconnectRules;

	public ReconnectConfigImpl() {
		super();
	}

	public ReconnectConfigImpl(Object config) {
		super(config);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void init() throws Exception {
		
		super.init();
		if (reconnectRules == null) {
			reconnectRules = new ContainerConfigImpl(getParameter(InternalSchema.CONNECTOR_RECONNECT_RULES, new TreeMap()));
			reconnectRules.setName(InternalSchema.CONNECTOR_RECONNECT_RULES);
			reconnectRules.init();
			reconnectRules.setParent(this);
			setChild(InternalSchema.CONNECTOR_RECONNECT_RULES, reconnectRules);
		}
	}

	/**
	 * We override this method to change the inherited object if we inherit from
	 * a connector.
	 */
	public void setInheritsFrom(BaseConfiguration inheritFrom) {
		
		ReconnectConfig rc = null;
		
		if (inheritFrom instanceof ConnectorConfig) {
			rc = ((ConnectorConfig) inheritFrom).getReconnectConfig();
		} else if (inheritFrom instanceof ReconnectConfig) {
			rc = (ReconnectConfig) inheritFrom;
		}
		
		if (rc != null) {
			getReconnectRules().setInheritsFrom(rc.getReconnectRules());
			super.setInheritsFrom(rc);
		}
	}

	/**
	 * Return self clone
	 */
	public Object getClone() throws Exception {
		ReconnectConfig rc = new ReconnectConfigImpl(deepClone(null));
		rc.init();
		rc.setupInheritanceChain();
		rc.setModTS(getModTS());
		return rc;
	}

	public boolean getAutoReconnect() {
		return getBooleanParameter(AUTO_RECONNECT, false);
	}

	public boolean getInitReconnect() {
		return getBooleanParameter(INIT_RECONNECT, false);
	}

	public int getRetries() {
		return getIntegerParameter(NUMBER_OF_RETRIES, 1);
	}

	public int getDelay() {
		return getIntegerParameter(RETRY_DELAY, 10);
	}

	public boolean getAutoSkipForward() {
		return getBooleanParameter(SKIP_FORWARD, false);
	}

	public void setAutoSkipForwardUnlessAlreadySet(Object value) {
		if ( hasParameter(SKIP_FORWARD) )
			return;
		if (value!=null)
			setParameter(SKIP_FORWARD, value, false);
	}

	public void removeParameterValues() {
		removeParameter(AUTO_RECONNECT);
		removeParameter(INIT_RECONNECT);
		removeParameter(NUMBER_OF_RETRIES);
		removeParameter(RETRY_DELAY);
		removeParameter(SKIP_FORWARD);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ContainerConfig getReconnectRules() {
		
		if (reconnectRules == null) {
			try {
				init();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		
		return reconnectRules;
	}

	/**
	 * {@inheritDoc}
	 */
	public ReconnectRuleConfig newReconnectRule() throws Exception {
		
		ReconnectRuleConfig rule = new ReconnectRuleConfigImpl();
		rule.setName("Rule");
		rule.init();
		
		getReconnectRules().addConfig(rule);
		
		return rule;
	}

	public int getFailbackAfter() {
		return getIntegerParameter("FailBackAfter", 0);
	}

	public String getFailoverConnectorName() {
		return getStringParameter("FailOverConnector");
	}

	public boolean getFailoverOption() {
		return getBooleanParameter("FailOverOption", false);
	}
}
