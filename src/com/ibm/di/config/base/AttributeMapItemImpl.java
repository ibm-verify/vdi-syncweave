/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.ArrayList;
import java.util.List;

import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;

/**
 * The implementation class of the {@link AttributeMapItem} interface.
 */
public class AttributeMapItemImpl extends BaseConfigurationImpl implements
		AttributeMapItem {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = 949475069666020874L;

	/**
	 * A list of child Attribute Map items.
	 */
	private List childAttributeMapItems;

	/**
	 * Default Constructor.
	 */
	public AttributeMapItemImpl() {
		super();
		try {
			init();
		} catch (Exception e) {
			// this should not happen
			e.printStackTrace();
		}
	}

	/**
	 * Constructor.
	 *
	 * @param config
	 *            TreeMap of attribute/value pairs.
	 */
	public AttributeMapItemImpl(Object config) {
		super(config);
		try {
			init();
		} catch (Exception e) {
			// this should not happen
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void init() throws Exception {
		super.init();
		if (childAttributeMapItems == null) {
			childAttributeMapItems = (ArrayList) getParameter(
					InternalSchema.ATTRIBUTE_MAP_CHILDREN, new ArrayList());
		}
	}

	/**
	 * Clone an Attribute Map Item.
	 *
	 * @param ami
	 *            Attribute Map Item, which will be cloned.
	 * @return The created clone.
	 */
	public static AttributeMapItem clone(AttributeMapItem ami) {
		AttributeMapItem c = new AttributeMapItemImpl();
		c.setEnabled(ami.getEnabled());
		c.setScript(ami.getScript());
		c.setSubstitution(ami.getSubstitution());
		c.setSimple(ami.getSimple());
		c.setModify(ami.getModify());
		c.setAdd(ami.getAdd());
		c.setType(ami.getType());
		c.setInheritsFromRef(ami.getInheritsFromRef());
		c.setName(ami.getName());
		if (ami.getNullBehavior() != null)
					c.setNullBehavior(ami.getNullBehavior());
				if (ami.getNullBehaviorValue() != null)
					c.setNullBehaviorValue(ami.getNullBehaviorValue());
				if (ami.getNullDefinition() != null)
					c.setNullDefinition(ami.getNullDefinition());
				if (ami.getNullDefinitionValue() != null)
		c.setNullDefinitionValue(ami.getNullDefinitionValue());
		return c;
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
	public void setEnabled(boolean enabled) {
		setBooleanParameter(InternalSchema.ENABLED, enabled);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getScript() {
		String s = getStringParameter(InternalSchema.AMI_SCRIPT);
		return s == null ? "" : s;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setScript(String script) {
		setStringParameter(InternalSchema.AMI_SCRIPT, script);
		setType(AttributeMapItem.ADVANCED_MAPPING);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getModify() {
		return getBooleanParameter(InternalSchema.AMI_MODIFY, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setModify(boolean modify) {
		setBooleanParameter(InternalSchema.AMI_MODIFY, modify);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getAdd() {
		return getBooleanParameter(InternalSchema.AMI_ADD, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAdd(boolean add) {
		setBooleanParameter(InternalSchema.AMI_ADD, add);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSimple() {
		String s = getStringParameter(InternalSchema.AMI_SIMPLE);
		return s == null ? "" : s;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSimple(String attribute) {
		setStringParameter(InternalSchema.AMI_SIMPLE, attribute);
		setType(AttributeMapItem.SIMPLE_MAPPING);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setType(String type) {
		if (type == null)
			type = AttributeMapItem.ADVANCED_MAPPING;
		setStringParameter(InternalSchema.AMI_TYPE, type);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType() {
		return (String) getParameter(InternalSchema.AMI_TYPE,
				AttributeMapItem.ADVANCED_MAPPING);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSimple() {
		return getType().equals(AttributeMapItem.SIMPLE_MAPPING);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAdvanced() {
		return getType().equals(AttributeMapItem.ADVANCED_MAPPING);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSubstitution() {
		return getType().equals(AttributeMapItem.SUBSTITUTION_MAPPING);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSubstitution(String str) {
		setStringParameter(InternalSchema.AMI_SUBSTITUTION, str);
		setType(AttributeMapItem.SUBSTITUTION_MAPPING);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSubstitution() {
		return getStringParameter(InternalSchema.AMI_SUBSTITUTION);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean flatten(List<String> excludedNS) throws Exception {

		boolean didFlatten = false;

		BaseConfiguration inheritFrom = getInheritsFrom();

		while (inheritFrom != null) {

			didFlatten = true;

			// Copy all simple values from inherited object
			List<String> simple = inheritFrom.getKeys(BaseConfiguration.ONE_LEVEL);
			for (int i = 0; i < simple.size(); i++) {
				String param = simple.get(i);
				if (!hasParameter(param))
					setParameter(param, inheritFrom.getParameter(param));
			}

			// Set inheritFrom to the next in the chain
			inheritFrom = inheritFrom.getInheritsFrom();
		}

		return didFlatten;
	}

	/**
	 * Detach this Attribute Map item from its parent Attribute Map. Note that
	 * the modification affects only the Attribute Map, the item itself still
	 * keeps its knowledge that the Attribute Map is its parent. This behavior
	 * is used by the {@link #reattachToParent(int)} method.
	 *
	 * @return Always true.
	 */
	public boolean detachFromParent() {
		AttributeMapConfig parent = (AttributeMapConfig) getParent();
		parent.removeAttributeMapItem(getShortName());
		return true;
	}

	/**
	 * Re-attach this Attribute Map item to its parent Attribute Map.
	 *
	 * @param position
	 *            This parameter is ignored.
	 * @return False if the Attribute Map already contains this Attribute Map
	 *         item, true otherwise.
	 */
	public boolean reattachToParent(int position) {
		AttributeMapConfig parent = (AttributeMapConfig) getParent();
		if (parent.hasAttributeMapItem(getShortName())) {
			return false;
		}

		parent.setAttributeMapItem(this);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getChildAttributeMaps() {
		return childAttributeMapItems;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInheritsFrom(BaseConfiguration inheritFrom) {
		if (inheritFrom instanceof ConnectorConfig) {
			String input = null;
			if (getParent() instanceof AttributeMapConfig)
				input = getParent().getShortName();
			inheritFrom = ((ConnectorConfig) inheritFrom)
					.getAttributeMap(input);
		}

		if (inheritFrom instanceof AttributeMapConfig) {
			inheritFrom = ((AttributeMapConfig) inheritFrom)
					.getAttributeMapItem(getShortName());
		}

		if (inheritFrom == this)
			inheritFrom = null;
		super.setInheritsFrom(inheritFrom);
	}
}
