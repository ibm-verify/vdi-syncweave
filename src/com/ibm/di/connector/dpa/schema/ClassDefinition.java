/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains meta data about a class from the model. 
 * A representation of the 'class' element from dpaschema.xml.
 * 
 * @author yavor.gologanov
 *
 */
public class ClassDefinition {

	private String className = null;
	private String table = null;
	private PropertyDefinition primaryKey = null;
	private UIDDefinition uidDefinition = null; 
	
	private ReferenceDefinition parent = null;	
	private PropertySetDefinition properties = null;
	private List<PropertySetDefinition> additionalProperties = null;
	private List<ReferenceDefinition> references = null;
	private List<PropertyDefinition> uniqueKey = null;
	
	
	/**
	 * 
	 */
	protected ClassDefinition() {
		
	}
	
	/**
	 * 
	 * @param uidDefinition
	 */
	protected void setUidDefinition(UIDDefinition uidDefinition) {
		this.uidDefinition = uidDefinition;
	}
	
	/**
	 * 
	 * @param parent
	 */
	protected void setParentDefinition(ReferenceDefinition parent) {
		this.parent = parent;
	}

	/**
	 * 
	 * @param className
	 */
	protected void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * 
	 * @param reference
	 */
	protected void addReference(ReferenceDefinition reference) {
		if (references == null) {
			references = new ArrayList<ReferenceDefinition>();
		}
		references.add(reference);
	}
	
	/**
	 * 
	 * @param properties
	 */
	protected void setProperties(PropertySetDefinition properties) {
		this.properties = properties;
		
		List<PropertyDefinition> propDefList = properties.getPropertyList();
		if (propDefList != null) {
			for(PropertyDefinition nextPropDef : propDefList) {
				if (nextPropDef.isUnique()) {
					if (uniqueKey == null) {
						uniqueKey = new ArrayList<PropertyDefinition>();
					}
					uniqueKey.add(nextPropDef);
				}
				if (nextPropDef.isPrimary()) {
					primaryKey = nextPropDef;
				}
			}
		}
	}	
	
	/**
	 * 
	 * @param properties
	 */
	protected void addAdditionalProperties(PropertySetDefinition properties) {
		if (additionalProperties == null) {
			additionalProperties = new ArrayList<PropertySetDefinition>();
		}
		this.additionalProperties.add(properties);
	}	
	
	/**
	 * 
	 * @param table
	 */
	protected void setTable(String table) {
		this.table = table;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getClassName() {
		return className;
	}
		
	/**
	 * 
	 * @return int
	 */
	public int getReferenceCount() {
		if (references == null) {
			return 0;
		} 

		return references.size();
	}
	
	/**
	 * 
	 * @return List<ReferenceDefinition> 
	 */
	public List<ReferenceDefinition> getReferences() {
		return references;
	}	
	
	/**
	 * 
	 * @return int
	 */
	public int getPropertyCount() {
		if (properties == null) {
			return 0;
		}
			
		if (properties.getPropertyList() == null) {
			return 0;
		}

		return properties.getPropertyList().size();
	}
	
	/**
	 * 
	 * @return PropertySetDefinition
	 */
	public PropertySetDefinition getProperties() {
		return properties;
	}

	/**
	 * 
	 * @return List<PropertyDefinition>
	 */
	public List<PropertyDefinition> getPropertyList() {
		if (properties != null) {
			return properties.getPropertyList();
		} else {
			return null;
		}
	}	
	
	/**
	 * 
	 * @return int
	 */
	public int getAdditionalPropertyCount() {
		if (additionalProperties == null)  {
			return 0;
		}

		return additionalProperties.size();
	}	
	
	/**
	 * 
	 * @return List<PropertySetDefinition>
	 */
	public List<PropertySetDefinition> getAdditionalProperties() {
		return additionalProperties;
	}

	
	/**
	 * 
	 * @param propertySetName
	 * @return PropertySetDefinition
	 */
	public PropertySetDefinition getAdditionalProperties(String propertySetName) {
		if (additionalProperties != null) {
			for (PropertySetDefinition propSetDef : additionalProperties) {
				if (propSetDef.getName().equals(propertySetName)) {
					return propSetDef;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getTable() {
		return table;
	}

	/**
	 * 
	 * @return PropertyDefinition
	 */
	public PropertyDefinition getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * 
	 * @return ReferenceDefinition
	 */
	public ReferenceDefinition getParent() {
		return parent;
	}	
		
	/**
	 * 
	 * @return List<PropertyDefinition>
	 */
	public List<PropertyDefinition> getUniqueKey() {
		return uniqueKey;
	}	
	
	/**
	 * 
	 * @return UIDDefinition
	 */
	public UIDDefinition getUidDefinition() {
		return uidDefinition;
	}		
	
	/**
	 * 
	 * @param propertyName
	 * @return PropertyDefinition
	 */
	public PropertyDefinition getPropertyDefinition(String propertyName) {
		if ((properties != null) && (properties.getPropertyCount() > 0)) {
			for (PropertyDefinition propDef: properties.getPropertyList()) {
				if (propDef.getName().equals(propertyName)) {
					return propDef;
				}
			}
		}
		
		if (additionalProperties != null) {
			for (PropertySetDefinition propSetDef: additionalProperties) {
				if (propSetDef.getPropertyCount() > 0) {
					for (PropertyDefinition propDef: propSetDef.getPropertyList()) {
						if (propDef.getName().equals(propertyName)) {
							return propDef;
						}
					}
				}
			}
		}
		
		return null;
	}
	
}
