/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import java.util.Iterator;

import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.TDIProperties;
import com.ibm.di.config.interfaces.TDIPropertyStore;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.eclipse.http.XML;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

/**
 * This command is used to manipulate the standard shared property stores as well as
 * configuration specific property stores.
 * <ul>
 * <li>properties/[instance:]storename/propname[/value] - Get or set value in specific
 * store
 * <li>properties/[instance:]storename - Get all values in store (HTTP GET)
 * <li>properties/[instance:]storename - Put all posted values to store (HTTP POST)
 * <li>properties/&star;/propname[/value] - Get or set value in default store
 * <li>properties - List all property stores
 * </ul>
 * The instance ID is optional. If one is specified, the instance ID is used to access the
 * property store. If the instance is running, the properties for the running configuration
 * is used, otherwise the configuration file for the instance is loaded to update the property
 * store.
 */
public class PropertiesCommand extends RestCommand {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public void execute() throws Exception {
		int i = 0;
		String propStore = getPath(i++);
		String propName = getPath(i++);
		String propValue = getPath(i++);
		String instanceID = null;
		Entry values = null;
		
		// Split instance and store name
		if(propStore != null && propStore.indexOf(":") != -1) {
			instanceID = propStore.substring(0, propStore.indexOf(":"));
			propStore = propStore.substring(propStore.indexOf(":")+1);
		}

		// Props are posted or in the URL
		if(propName == null && propValue == null) {
			propValue = getRequestBody();
			getApi().debugMsg("Posted XMLEntry: " + propValue);
			if(propValue != null) {
				values = XML.fromXML(propValue);
				propValue = null;
				if(values != null) {
					for(String str : values.getAttributeNames()) {
						getApi().debugMsg("-- " + str + ": " + values.getString(str));
					}
				}
			}
		}
		
		// -- Create a dummy config to access shared property stores
		MetamergeConfig mc;
		ConfigInstance cci = null;
		if(instanceID != null) {
			cci = getSession().getConfigInstance(instanceID);
			if(cci == null)
				cci = getSession().createNewConfigurationAndLoad(instanceID, true);

			mc = cci.getConfiguration();
		} else {
			mc = new MetamergeConfigXML();
			mc.initializeConfig();
		}
		TDIProperties props = mc.getTDIProperties();
		
		//
		// If no property store provided just return a list of available property stores
		//
		if(propStore == null) {
			for(String name: props.getPropertyStoreNames())
				appendResult("stores", name);
			return;
		} else {
			// permit upper-lower case matching of store name
			String str = findStore(props, propStore);
			if(str == null)
				throw new Exception(sRes.getString("Unknown.property.store", propStore));
			propStore = str;
		}
		

		Object oldValue = null;
		Object store = (propStore.equals("*") ? (Object)props : (Object)props.getPropertyStore(propStore));

		// SetProperty
		if(propValue != null) {
			oldValue = setPropertyValue(store, propName, propValue);
			commitStore(store);
			
		// GetProperty
		} else if (propName != null) {
			oldValue = getPropertyValue(store, propName);
			
		// Set or get multiple props in specified store
		} else if (propStore != null) {
			// If user posted an entry with prop/values we write those
			// otherwise return all prop/values in the store.
			if(values != null) {
				for(String str : values.getAttributeNames()) {
					setPropertyValue(store, str, values.getString(str),
							Boolean.valueOf("" + values.getProperty(str)));
				}
				commitStore(store);
			} else {
				addPropertyValues(props.getPropertyStore(propStore));
			}
			return;
		}

		if(propName != null)
			appendResult(propName, (oldValue == null ? "" : oldValue.toString()));
	}
	
	private String findStore(TDIProperties props, String propStore) {
		for(String name: props.getPropertyStoreNames()) { 
			if(propStore.equalsIgnoreCase(name))
				return name;
		}
		return null;
	}

	/**
	 * Returns the value for key and sets the new value.
	 * @param store TDIProperties or TDIPropertyStore
	 * @param key
	 * @param value if null the property is removed (if applicable)
	 * @return the value before setting the new one
	 * @throws Exception
	 */
	public Object setPropertyValue(Object store, String key, String value) throws Exception {
		Object oldValue = getPropertyValue(store, key); 
		if(store instanceof TDIProperties) {
			((TDIProperties)store).setProperty(key, value);
		} else if (value!=null) {
			((TDIPropertyStore)store).setProperty(key, value);
		} else {
			((TDIPropertyStore)store).removeProperty(key);			
		}
		return oldValue;
	}
	
	/**
	 * Sets a new value for the key in the store.
	 * @param store TDIProperties or TDIPropertyStore
	 * @param key
	 * @param value if null the property is removed (if applicable)
	 * @param protect if true, the property should be encrypted
	 * @throws Exception
	 */
	public void setPropertyValue(Object store, String key, String value, boolean protect) throws Exception {
		if(store instanceof TDIProperties) {
			((TDIProperties)store).setProperty(key, value, protect);
		} else if (value != null ) {
			((TDIPropertyStore)store).setProperty(key, value, protect);
		} else {
			((TDIPropertyStore)store).removeProperty(key);			
		}
	}
	
	public Object getPropertyValue(Object store, String key) throws Exception {
		if(store instanceof TDIProperties) {
			return ((TDIProperties)store).getProperty(key);
		} else {
			return ((TDIPropertyStore)store).getProperty(key);
		}
	}
	
	private void addPropertyValues(TDIPropertyStore store) {
		if (store == null)
			return;
		Entry result = getResponse();
		for(Iterator<Entry> iter = store.entries(); iter.hasNext(); ) {
			Entry e = iter.next();
			String key = e.getString(TDIProperties.KEY_ATTRIBUTE);
			Attribute value = e.getAttribute(TDIProperties.VALUE_ATTRIBUTE);
			result.addAttributeValue(key, value.getValue());
			if (value.getProtected())
				result.setProperty(key, "true");
		}	
	}
	
	public void commitStore(Object store) throws Exception {
		if(store instanceof TDIProperties) {
			((TDIProperties)store).commit();
		} else {
			((TDIPropertyStore)store).commit();
		}
	}
}
