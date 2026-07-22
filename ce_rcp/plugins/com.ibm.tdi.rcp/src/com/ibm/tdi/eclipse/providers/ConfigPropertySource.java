/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;

public class ConfigPropertySource implements IPropertySource2 {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BaseConfiguration config;
	private PropertyDescriptor[] props;

	public ConfigPropertySource(BaseConfiguration config) {
		this.config = config;
	}

	public boolean isPropertyResettable(Object id) {
		return false;
	}

	public boolean isPropertySet(Object id) {
		return config.hasParameter(id);
	}

	public Object getEditableValue() {
		return this;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		ArrayList<PropertyDescriptor> pdesc;
		if(props == null) {
			PropertyDescriptor pd;
			pdesc = new ArrayList<PropertyDescriptor>();
			
			// BaseConfiguration properties
			pd = new TextPropertyDescriptor("name", "Name");
			pd.setCategory("Common");
			pdesc.add(pd);
			pd = new TextPropertyDescriptor("comment", "Comment");
			pd.setCategory("Common");
			pdesc.add(pd);
			pd = new TextPropertyDescriptor("enabled", "Enabled");
			pd.setCategory("Common");
			pdesc.add(pd);
		
			// SchemaItemConfig 
			if(config instanceof SchemaItemConfig) {
				pd = new TextPropertyDescriptor("OccursMin", "Min Occurs");
				pd.setCategory("Schema");
				pdesc.add(pd);
				
				pd = new TextPropertyDescriptor("OccursMax", "Max Occurs");
				pd.setCategory("Schema");
				pdesc.add(pd);
				
				pd = new TextPropertyDescriptor("Property", "Property");
				pd.setCategory("Schema");
				pdesc.add(pd);
				
				pd = new TextPropertyDescriptor("sample", "Sample");
				pd.setCategory("Schema");
				pdesc.add(pd);
				
				pd = new TextPropertyDescriptor("syntax", "Java Class");
				pd.setCategory("Schema");
				pdesc.add(pd);
			}
			
			// Attribute map info
			if(config instanceof AttributeMapItem) {
				pd = new TextPropertyDescriptor("Script", "Script");
				pd.setCategory("Attribute Map");
				pdesc.add(pd);

				pd = new TextPropertyDescriptor("Simple", "Work Reference");
				pd.setCategory("Attribute Map");
				pdesc.add(pd);
			}
			
			// Connector config
			if((!(config instanceof FunctionConfig) && !(config instanceof ALMappingConfig))
					&& (config instanceof ConnectorConfig)) {
				pd = new TextPropertyDescriptor("Mode", "Mode");
				pd.setCategory("Connector");
				pdesc.add(pd);
			}
		
			props = pdesc.toArray(new PropertyDescriptor[pdesc.size()]); 
		}
		
		return props;
	}

	public Object getPropertyValue(Object id) {
		String str;
		try {
			if(id.equals("enabled"))
				return "" + config.getEnabled();
			else if(id.equals("name"))
				return "" + config.getShortName();
			else
				str = config.getStringParameter(id);
		} catch (Throwable t) {
			return t.toString();
		}
		
		return (str == null ? "" : str);
	}

	public void resetPropertyValue(Object id) {
	}

	public void setPropertyValue(Object id, Object value) {
		if(id.equals("name")) {
			try {
				config.setName(value.toString());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else {
			config.setParameter(id, value);
		}
	}

	public static IPropertySource getInstance(BaseConfiguration config) {
		return new ConfigPropertySource(config);
	}

}
