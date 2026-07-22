/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.TreeMap;

import com.ibm.di.config.interfaces.*;

/**
 * Implements the configuration of a component used to branch the
 * business logic
 */
public class BranchingConfigImpl extends ContainerConfigImpl implements
		BranchingConfig {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private final static String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = -1013588884381133944L;

	/**
	 * A container with the conditions of the branch component.
	 */
	protected ContainerConfig conditions;

	/**
	 * Constructs a BranchingConfigImpl object
	 * 
	 */
	public BranchingConfigImpl() {
		super();
	}

	/**
	 * Constructs a BranchingConfigImpl object
	 * 
	 * @param data
	 *            TreeMap with config data
	 */
	public BranchingConfigImpl(Object data) {
		super(data);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getEnabled() {
		return getBooleanParameter(InternalSchema.ENABLED, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public int totalSize() {
		int count = 0;
		for (int i = 0; i < size(); i++) {
			BaseConfiguration c = getConfig(i);
			if (c instanceof BranchingConfig) {
				if (c.getEnabled())
					count += 1 + ((BranchingConfig) c).totalSize();
			} else if (c instanceof ConnectorConfig) {
				if (!ConnectorConfig.DISABLED_STATE
						.equals(((ConnectorConfig) c).getState()))
					count++;
			} else if (c.getEnabled()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	public int numberLoops() {
		int count = 1;

		for (int i = 0; i < size(); i++) {
			BaseConfiguration c = getConfig(i);
			if (!c.getEnabled())
				continue;

			if (c instanceof BranchingConfig)
				count += ((BranchingConfig) c).numberLoops();
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init() {
		try {
			super.init();
		} catch (Exception ignore) {}
		if (conditions == null) {
			conditions = new ContainerConfigImpl(getParameter("Conditions",
					new TreeMap<String,Object>()));
			conditions.setParent(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ContainerConfig getConditions() {
		return conditions;
	}

	/**
	 * Set the conditions of the branch component.
	 * 
	 * @param conditions
	 *            A container with {@link BranchCondition} configurations.
	 */
	public void setConditions(ContainerConfig conditions) {
		this.conditions = conditions;
	}

	/**
	 * {@inheritDoc}
	 */
	public BranchCondition newCondition() {
		BranchCondition b = new com.ibm.di.config.base.BranchConditionImpl();
		try {
			b.init();
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
		return b;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getMatchAny() {
		return getBooleanParameter("MatchAny", false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMatchAny(boolean matchAny) {
		setBooleanParameter("MatchAny", matchAny);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getBranchType() {
		return getIntegerParameter("BranchType", BRANCH_IF);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBranchType(int type) {
		setIntegerParameter("BranchType", type);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getClone() throws Exception {
		BranchingConfig bc = new BranchingConfigImpl(deepClone(null));
		bc.setName(getName());
		bc.init();
		bc.setMetamergeConfig(getMetamergeConfig());
		bc.setModTS(getModTS());

		return bc;
	}
}
