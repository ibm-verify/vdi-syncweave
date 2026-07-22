/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.*;

import javax.naming.Name;

import com.ibm.di.config.interfaces.*;
import com.ibm.di.function.SystemFunctions;

/**
 * Implements {@link SchemaConfig}
 *
 */
public class SchemaConfigImpl extends BaseConfigurationImpl implements
		SchemaConfig {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 1778816095104785134L;

	private Hashtable<String,SchemaItemConfig> items = new Hashtable<String,SchemaItemConfig>();
	
	private Map<String, String> lowerCaseMap = new HashMap<String, String>();
	
	/**
	 * The current Schema will include all attributes from its attached Schemas.
	 */
	private List<SchemaConfig> attachedSchemas = new ArrayList<SchemaConfig>();

	public SchemaConfigImpl() {
		super();
	}

	public SchemaConfigImpl(Object config) {
		super(config);
	}

	/**
	 * Override since the user comment may be a schema item
	 */
	public String getUserComment() {
		return null;
	}

	/**
	 * Override, we cannot set any user comment
	 */
	public void setUserComment(String comment) {
	}

	/**
	 * @return The List of item names
	 */
	public List<String> getItemNames() {
		
		// ensure there are no duplications
		Set<String> itemNames = new TreeSet<String>(getKeys(BaseConfiguration.RECURSIVE_SUBTREE));
		
		// add all items from the attached Schemas
		Iterator<SchemaConfig> it = attachedSchemas.iterator();
		while (it.hasNext()) {
			SchemaConfig attachedSchema = it.next();
			itemNames.addAll(attachedSchema.getItemNames());
		}
		
		return new ArrayList<String>(itemNames);
	}

	/**
	 * Returns a named SchemItemConfig
	 * 
	 * @param name
	 *            The name of the schema item
	 * @return The item value
	 */
	public SchemaItemConfig getItem(Object name) {

		if (name == null)
			return null;

		if(name instanceof Name) {
			Name sn = (Name) name;
			SchemaItemConfig sic = getItem(sn.get(0));
			for(int i = 1; i < sn.size() && sic != null; i++) {
				sic = (SchemaItemConfig) sic.getChildSchemaList().getConfig(sn.get(i));
			}
			return sic;
		}

		String itemName = name.toString();
		Object o = getParameter(itemName);

		if (o == null) {
			// Try to ignore case
			String s = lowerCaseMap.get(itemName.toLowerCase());
			if (s == null && getInheritsFrom() instanceof SchemaConfig){
				SchemaItemConfig sic = ((SchemaConfig)getInheritsFrom()).getItem(itemName);
				if (sic != null)
					s = sic.getAttributeName();
			}
			if (s != null) {
				o = getParameter(s);
				if (o != null)
					itemName = s;
			} 
		}
		
		TreeMap obj = null;
		// We only know what to do with a TreeMap, maybe add an error message here?
		if ( o instanceof TreeMap )
			obj = (TreeMap) o;

		// Try to use a SchemaName
		if (obj == null) {
			try {
				SchemaName sn = new SchemaName(itemName);
				if(sn.size() > 1) {
					SchemaItemConfig sic = getItem(sn);
					if ( sic != null)
						obj = sic.getData();
				}
			} catch (Exception e) {
				// TODO: handle exception
				// e.printStackTrace();
			}
		}
		
		// search the attached Schemas
		if (obj == null) {
			Iterator<SchemaConfig> it = attachedSchemas.iterator();
			while (it.hasNext() && obj == null ) {
				SchemaConfig schema = it.next();
				SchemaItemConfig item = schema.getItem(itemName);
				if (item != null) {
					obj = item.getData();
				}
			}
		}

		if (obj == null)
			return null;

		SchemaItemConfig csi = items.get(itemName);
		if (csi != null) {
			// If regetting a cloned copy from inherited make sure it is updated
			if (!hasParameter(itemName))
				csi.setData(obj);
			return csi;
		}

		// Save handle and make child notify us of changes
		csi = new SchemaItemConfigImpl(obj);
		try {
			csi.setName(MetamergeConfigFactory.simpleName(itemName));
		} catch (Exception ignore) {
		}
		csi.setParent(this);
		items.put(itemName, csi);
		lowerCaseMap.put(itemName.toLowerCase(), itemName);

		// If we inherit this item then we receive a clone of the data and we
		// need to add it
		// to our local store when it changes.
		if (!hasParameter(itemName)) {
			if (getInheritsFrom() instanceof SchemaConfig)
				copySubItems(csi, ((SchemaConfig) getInheritsFrom()).getItem(itemName));

			csi.addListener(new DefaultConfigChangeListener() {
				public void configurationChanged(MetamergeConfigChange mcc) {
					if (mcc.getSource() instanceof SchemaItemConfig) {
						SchemaItemConfig csi = (SchemaItemConfig) mcc
								.getSource();
						csi.removeListener(this);
						setItem(csi.getShortName(), csi);
					}
				}
			});
		}

		return csi;
	}

	private void copySubItems(SchemaItemConfig target, SchemaItemConfig source) {
		if (source == null)
			return;
		try {
			ContainerConfig tc = target.getChildSchemaList();
			ContainerConfig sc = source.getChildSchemaList();
			for (int i = 0; i < sc.size(); i++) {
				SchemaItemConfig sic = (SchemaItemConfig) sc.getConfig(i);
				SchemaItemConfig copy = (SchemaItemConfig) sic.getClone();
				tc.addConfig(copy);
				copySubItems(copy, sic);
			}
		} catch (Exception ignore) {
 			SystemFunctions.doNothing();
		}
	}

	/**
	 * Removes an item attribute from the schema
	 * 
	 * @param name
	 *            The name of the item to remove
	 */
	public void removeItem(Object name) {
		String itemName = name.toString();
		removeParameter(itemName);
		items.remove(itemName);
		lowerCaseMap.remove(itemName);
		//attached schemas are read-only so skip them
	}

	/**
	 * Sets the item attribute of the SchemaConfig object
	 * 
	 * @param name
	 *            The name of the new item
	 * @param item
	 *            The SchemaConfig object to put in
	 */
	public void setItem(Object name, SchemaItemConfig item) {
		String itemName = name.toString();
		item.setAttributeName(itemName);
		item.setParent(this);
		items.put(itemName, item);
		lowerCaseMap.put(itemName.toLowerCase(), itemName);
		setParameter(itemName, item.getData());
	}

	/**
	 * Constructs a new SchemaItemConfig object. The object contains an item
	 * attribute with name
	 * 
	 * @param name
	 *            The name of the item attribute
	 * @return New or existing SchemaItemConfig object
	 * @exception Exception
	 *                if the construction does not succeed 
	 */
	public SchemaItemConfig newItem(Object name) throws Exception {
		SchemaItemConfig sic = getItem(name);
		if (sic != null)
			return sic;

		if (name instanceof Name) {
			Name sn = (Name) name;
			sic = newItem(sn.get(0));
			for(int i = 1; i < sn.size(); i++) {
				ContainerConfig cc = sic.getChildSchemaList();
				sic = (SchemaItemConfig) cc.getConfig(sn.get(i));
				if (sic == null) {
					sic = new SchemaItemConfigImpl();
					sic.setAttributeName(sn.get(i));
					cc.addConfig(sic);
				}
			}
		} else {			
			sic = new SchemaItemConfigImpl();
			sic.setName(MetamergeConfigFactory.simpleName(name.toString()));
			setItem(name, sic);
		}

		return sic;
	}

	/**
	 * We override this method to change the inherited object if we inherit from
	 * a connector.
	 */
	public void setInheritsFrom(BaseConfiguration inheritFrom) {
		if (inheritFrom instanceof ConnectorConfig) {
			super.setInheritsFrom(((ConnectorConfig) inheritFrom)
					.getSchema(getShortName()));
		} else if (inheritFrom instanceof ParserConfig) {
			super.setInheritsFrom(((ParserConfig) inheritFrom)
					.getSchema(getShortName()));
		} else {
			super.setInheritsFrom(inheritFrom);
		}
	}

	public boolean flatten(List<String> excludedNS) throws Exception {
		if (!super.flatten(excludedNS))
			return false;

		List<String> list = getItemNames();
		for (int i = 0; i < list.size(); i++) {
			SchemaItemConfig sc = (SchemaItemConfig) getItem(list.get(i));
			setItem(sc.getName(), sc);
		}
		return true;
	}
	
	/**
	 * Attach a Schema. The attributes of the attached schema will be listed as
	 * part of the current object. Attached schemas are perceived as read-only -
	 * attributes will not be removed nor added to them. The attachment
	 * relationship is runtime only - it will not be persisted.
	 * 
	 * @param schema
	 *            Schema to attach
	 * 
	 * @since 7.0
	 */
	public void attachSchema(SchemaConfig schema) {

		if (!attachedSchemas.contains(schema)) {

			attachedSchemas.add(schema);
			schema.addListener(this);
			notifyAttachmentChange();
		}
	}

	/**
	 * Detach a Schema. If the specified Schema is not attached to the current
	 * one, the method will have no effect.
	 * 
	 * @param schema
	 *            Schema to detach
	 * 
	 * @since 7.0
	 */
	public void detachSchema(SchemaConfig schema) {

		if (attachedSchemas.contains(schema)) {

			schema.removeListener(this);
			attachedSchemas.remove(schema);
			notifyAttachmentChange();
		}
	}

	/**
	 * Notify that the list of attached Schemas has been modified. Try to ensure we do
	 * not change the modification status - the attachment of schema is a
	 * runtime-only relationship.
	 * 
	 * @since 7.0
	 */
	private void notifyAttachmentChange() {

//		boolean save = getModified();

		notifyChange(this, InternalSchema.INHERITS_FROM,
				MetamergeConfigChange.MCC_REPLACE, SETINHERITSFROM);

//		setModified(save);
	}

	/**
	 * {@inheritDoc}
	 */
	public BaseConfiguration getChild(Object name) {
		if (hasParameter(name))
			return getItem(name);
		else
			return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getChildNames() {
		return getItemNames();
	}
	}
