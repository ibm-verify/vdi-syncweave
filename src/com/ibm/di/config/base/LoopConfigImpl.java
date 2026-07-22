/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Binding;

import com.ibm.di.config.interfaces.*;

/**
 * Implements the configuration for a Loop Component in an AssemblyLine.
 */
public class LoopConfigImpl extends BranchingConfigImpl implements LoopConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private final static String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -8174541074510481418L;

	protected ConnectorConfig loopConnector;

	/**
	 * Constructor for the loopConnectorImpl object
	 *
	 */
	public LoopConfigImpl() {
		super();
	}

	/**
	 * Constructor for the loopConnectorImpl object
	 *
	 * @param data
	 *            TreeMap with config data
	 */
	public LoopConfigImpl(Object data) {
		super(data);
	}

	/**
	 * Returns the Loop connector configuration
	 */
	public ConnectorConfig getLoopConnector() throws Exception {
		if (loopConnector == null) {
			ConnectorConfig cc = (ConnectorConfig) getParameter(
					"loopConnector", new ConnectorConfigImpl());
			cc.init();
			cc.getConnectionConfig().setInheritsFromRef(
					BaseConfiguration.INHERIT_PARENT);
			cc.getParserConfig().setInheritsFromRef(
					BaseConfiguration.INHERIT_PARENT);
			cc.getSchema(ConnectorConfig.SCHEMA_INPUT).setInheritsFromRef(
					BaseConfiguration.INHERIT_PARENT);
			cc.getAttributeMap(true).setInheritsFromRef(
					BaseConfiguration.INHERIT_PARENT);
			cc.getHooks().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
			cc.setupInheritanceChain();
			cc.setName(getName());
			cc.setParent(this);
			setLoopConnector(cc);
		}
		return loopConnector;
	}

	public void setLoopConnector(ConnectorConfig cc) {
		this.loopConnector = cc;
		cc.setParent(this);
	}

	/**
	 * Returns the type of loop we are doing
	 */
	public int getLoopType() {
		return getIntegerParameter(InternalSchema.LOOP_TYPE,
				LoopConfig.LOOP_CONNECTOR_FC);
	}

	/**
	 * Sets the loop type
	 */
	public void setLoopType(int type) {
		setIntegerParameter(InternalSchema.LOOP_TYPE, type);
	}

	/**
	 * Returns the type of loop we are doing
	 */
	public int getInitConnectorOption() {
		return getIntegerParameter(InternalSchema.LOOP_INIT_OPTION,
				LoopConfig.OPTION_INITIALIZE);
	}

	/**
	 * Sets the loop type
	 */
	public void setInitConnectorOption(int option) {
		setIntegerParameter(InternalSchema.LOOP_INIT_OPTION, option);
	}

	/**
	 * Returns the name of the work attribute whose values to loop over
	 */
	public String getWorkAttributeName() {
		return getStringParameter(InternalSchema.LOOP_WORK_NAME);
	}

	/**
	 * Sets the name of the work attribute whose values to loop over
	 */
	public void setWorkAttributeName(String name) {
		setStringParameter(InternalSchema.LOOP_WORK_NAME, name);
	}

	/**
	 * Returns the name of the loop attribute that has one value from the work
	 * attribute for each loop
	 */
	public String getLoopAttributeName() {
		return getStringParameter(InternalSchema.LOOP_ATTR_NAME);
	}

	/**
	 * Sets the name of the work attribute whose values to loop over
	 */
	public void setLoopAttributeName(String name) {
		setStringParameter(InternalSchema.LOOP_ATTR_NAME, name);
	}

	public void notifyChange(Object source, Object key, int operation,
			Object userObject) {
		if (source == loopConnector)
			super.notifyChange(this, "loopConnector", operation, null);
		else
			super.notifyChange(source, key, operation, userObject);
	}

	/**
	 * Return self clone
	 *
	 * @return A cloned object of this
	 */
	public Object getClone() throws Exception {
		LoopConfigImpl bc = new LoopConfigImpl(deepClone(null));
		bc.setName(getName());
		bc.init();
		bc.setLoopConnector((ConnectorConfig) getLoopConnector().getClone());
		bc.setMetamergeConfig(getMetamergeConfig());
		bc.setModTS(getModTS());
		return bc;
	}

	/**
	 * flatten - combines all values from this object and its inherited objects
	 * into one single config object. After flattening, the object is a complete
	 * object with no inherited values except those from the excludedNS list.
	 *
	 * @param excludedNS
	 *            List of namespaces to exclude from flattening
	 */
	public boolean flatten(List<String> excludedNS) throws Exception {
		if (loopConnector != null)
			loopConnector.flatten(excludedNS);

		return super.flatten(excludedNS);
	}

	public List<String> getReferences(List<String> list) {
		List<String> refs = (list == null ? new ArrayList<String>() : list);
		if (loopConnector != null)
			loopConnector.getReferences(refs);
		return refs;
	}

	public List<Binding> search(String text, int options, int sizelimit, List<Binding>  results) {
		if (loopConnector != null)
			results = loopConnector.search(text, options, sizelimit, results);
		return super.search(text, options, sizelimit, results);
	}

	@Override
	public void setupInheritanceChain() throws Exception {
		super.setupInheritanceChain();
		if (loopConnector != null)
			loopConnector.setupInheritanceChain();
	}
}
