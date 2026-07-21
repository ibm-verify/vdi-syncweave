/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.*;
import com.ibm.di.config.interfaces.*;
import com.ibm.di.server.ResourceHash;
/**
 * This class implements the old Connector Schema.
 * Connectors currently use the standard Schema implementation {@link SchemaConfigImpl}.
 * @deprecated
 */
public class ConnectorSchemaConfigImpl extends BaseConfigurationImpl implements
		ConnectorSchemaConfig, MetamergeConfigChangeListener {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 930161291800752910L;

	private Hashtable items = new Hashtable();

	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	/**
	 * 
	 */
	public ConnectorSchemaConfigImpl() {
		super();
	}

	public ConnectorSchemaConfigImpl(Object config) {
		super(config);
	}

	public java.util.List getItemNames() {
		return getKeys(BaseConfiguration.RECURSIVE_SUBTREE);
	}

	public ConnectorSchemaItemConfig getItem(Object name) {
		return getItem(name, false);
	}

	public ConnectorSchemaItemConfig getItem(Object name, boolean create) {

		Object obj = getParameter(name);
		if (obj == null)
			return null;

		ConnectorSchemaItemConfig csi;
		if ((csi = (ConnectorSchemaItemConfig) items.get(name.toString())) != null) {
			// If regetting a cloned copy from inherited make sure it is updated
			if (!hasParameter(name))
				csi.setData((TreeMap) obj);
			return csi;
		}

		// Save handle and make child notify us of changes
		csi = new ConnectorSchemaItemConfigImpl(obj);
		csi.setParent(this);
		try {
			csi.setName(MetamergeConfigFactory.parseName(name));
		} catch (Exception ignore) {
		}
		items.put(name.toString(), csi);

		// If we inherit this item then we receive a clone of the data and we
		// need to add it
		// to our local store when it changes.
		if (!hasParameter(name)) {
			csi.addListener(new DefaultConfigChangeListener() {
				public void configurationChanged(MetamergeConfigChange mcc) {
					if (mcc.getSource() instanceof ConnectorSchemaItemConfig) {
						ConnectorSchemaItemConfig csi = (ConnectorSchemaItemConfig) mcc
								.getSource();
						csi.removeListener(this);
						setItem(csi.getShortName(), csi);
					}
				}
			});
		}

		return csi;
	}

	public void removeItem(Object name) {
		removeParameter(name);
		items.remove(name.toString());
	}

	public void setItem(Object name, ConnectorSchemaItemConfig item) {
		setParameter(name, item.getData());
		item.setParent(this);
		items.put(name.toString(), item);
	}

	public ConnectorSchemaItemConfig newItem(Object name) throws Exception {
		if (getItem(name) != null)
			throw new javax.naming.NameAlreadyBoundException(name.toString());

		ConnectorSchemaItemConfigImpl cc = new ConnectorSchemaItemConfigImpl();
		cc.setAttributeName(name.toString());
		cc.setExcluded(false);
		cc.setName(MetamergeConfigFactory.parseName(name));

		setItem(name, cc);

		return cc;
	}

	/**
	 * We override this method to change the inherited object if we inherit from
	 * a connector.
	 */
	public void setInheritsFrom(BaseConfiguration inheritFrom) {

		System.out
				.println(sResHash
						.getString("MMCONFIG.CONNSCHEMACONFIMPL.THIS.CLASS.IS.NO.LONGER.IN.USE"));

		/*
		 * if ( inheritFrom instanceof ConnectorConfig ) { super.setInheritsFrom (
		 * ((ConnectorConfig) inheritFrom).getSchema() ); ((ConnectorConfig)
		 * inheritFrom).getSchema().addListener ( this ); } else {
		 * super.setInheritsFrom ( inheritFrom ); }
		 */
	}

	public void configurationChanged(MetamergeConfigChange changeEvent) {
		performNotifyChange(changeEvent);
	}

}
