/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.schema;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.ibm.di.server.ResourceHash;

/**
 * An instance of a class from the data model.
 * This class contains actual data (properties and relations). 
 * Each class instance object has a corresponding ClassDefinition object.
 * 
 * @author yavor.gologanov
 *
 */
public class ClassInstance {
	
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "dpaconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);	

	private ClassDefinition definition = null;
	private String className = null;
	
	private Map<String, Object> properties = null;	
	private Map<String, PropertySetInstance> additionalProperties = null;
	private Map<String, ReferenceInstance> references = null;
	private ReferenceInstance parent = null;

	/**
	 * 
	 * @param definition
	 */
	public ClassInstance(ClassDefinition definition) {
		this.definition = definition;
		this.className = definition.getClassName();
	}
	
	/**
	 * 
	 * @param parent
	 */
	public void setParent(ReferenceInstance parent) {
		this.parent = parent;
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
	 * @return Object
	 */
	public Object getPrimaryKeyValue() {
		return this.getProperty(definition.getPrimaryKey().getName());
	}

	/**
	 * 
	 * @param value
	 */
	public void setPrimaryKeyValue(Object value) {
		if (definition.getPrimaryKey() == null) {
			throw new InconsistentInstanceException(resHash.getString("DPA.CONN.PKEY.NOT.SPECIFIED", new Object[]{definition.getClassName()}));
		}
		setProperty(definition.getPrimaryKey().getName(), value);
	}	
	
	/**
	 * 
	 * @return int
	 */
	public int getPropertiesCount() {
		if (properties != null) {
			return properties.keySet().size();
		} else {
			return 0;
		}
	}
	
	/**
	 * 
	 * @return Set<String>
	 */
	public Set<String> getPropertyNames() {
		if (properties != null) {
			return properties.keySet();
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param propertyName
	 * @return Object
	 */
	public Object getProperty(String propertyName) {
		if (properties != null) {
			return properties.get(propertyName);
		} else {
			return null;
		}
	}	
	
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void setProperty(String name, Object value) {
		if (properties == null) {
			properties = new TreeMap<String, Object>();
		}
		this.properties.put(name, value);
	}	
	
	/**
	 * 
	 * @return int
	 */
	public int getReferenceCount() {
		if (references == null) {
			return 0;
		}
		
		return references.keySet().size();
	}
	
	/**
	 * 
	 * @return Set<String>
	 */
	public Set<String> getReferenceNames() {
		if (references != null) {
			return references.keySet();
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param referenceName
	 * @return ReferenceInstance
	 */
	public ReferenceInstance getReference(String referenceName) {
		if (references != null) {
			return references.get(referenceName);
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param reference
	 */
	public void addReference(ReferenceInstance reference) {
		if (references == null) {
			references = new TreeMap<String, ReferenceInstance>();
		}
		this.references.put(reference.getName(), reference);
	}		
	
	/**
	 * 
	 * @param propertySet
	 */
	public void addAdditionalProperties(PropertySetInstance propertySet) {
		if (additionalProperties == null) {
			additionalProperties = new TreeMap<String, PropertySetInstance>();
		}
		this.additionalProperties.put(propertySet.getName(), propertySet);
	}
	
	/**
	 * 
	 * @return int
	 */
	public int getAdditionalPropertiesCount() {
		if (additionalProperties == null) {
			return 0;
		}
		
		return additionalProperties.keySet().size();
	}
	
	/**
	 * 
	 * @return Map<String, PropertySetInstance>
	 */
	public Map<String, PropertySetInstance> getAdditionalPropertiesMap() {
		return additionalProperties;
	}
	
	/**
	 * 
	 * @return Collection<PropertySetInstance>
	 */
	public Collection<PropertySetInstance> getAdditionalProperties() {
		if (additionalProperties == null) {
			return null;
		}
		
		return additionalProperties.values();
	}	
	
	/**
	 * 
	 */
	public void clear() {
		this.references = null;
		this.properties = null;
		this.additionalProperties = null;
	}
		
	/**
	 * 
	 * @return boolean
	 */
	public boolean isEmpty() {
		boolean emptyProps = ((properties == null) 
				|| (properties.isEmpty()));
		
		boolean emptyADProps = ((additionalProperties == null) 
				|| (additionalProperties.isEmpty()));
		
		boolean emptyRefs = ((references == null) 
				|| (references.isEmpty()));
		
		return (emptyProps && emptyRefs && emptyADProps);
	}	
	
	/**
	 * 
	 * @return ClassDefinition
	 */
	public ClassDefinition getDefinition() {
		return definition;
	}	
	
	/**
	 * 
	 * @return parent
	 */
	public ReferenceInstance getParent() {
		return parent;
	}	
	
	/**
	 * 
	 * @param required
	 * @return ClassInstance
	 */
	public ClassInstance getParentInstance(boolean required) {
		if (required && (parent == null)) {
			throw new InconsistentInstanceException(resHash.getString("DPA.CONN.PARENT.CLASS.REF.NOT.FOUND", new Object[] {getDescription()}));
		}
		
		if (parent != null) {
			ClassInstance parentInstance = parent.getFirstClassInstance();
			if (required && (parentInstance == null)) {
				throw new InconsistentInstanceException(resHash.getString("DPA.CONN.PARENT.CLASS.INST.NOT.FOUND", new Object[] {getDescription()}));
			}
			return parentInstance;
		}
		
		return null;
	}	
	
	/**
	 * 
	 */
	public void validate() {
		
		if (definition.getParent() != null && parent == null) {
			throw new InconsistentInstanceException(resHash.getString("DPA.CONN.PARENT.NULL", new Object[] {getDescription()}));
		}

		PropertySetDefinition dpropertySet = definition.getProperties();
		List<PropertyDefinition> propDefList = dpropertySet.getPropertyList();
		if (propDefList != null) {
			Iterator<PropertyDefinition> propDefIt = propDefList.iterator();
			while (propDefIt.hasNext()) {
				PropertyDefinition nextPropDef = propDefIt.next();
				if (nextPropDef.isRequired()) {
					Object value = properties.get(nextPropDef.getName());
					if (value == null) {
						throw new InconsistentInstanceException(resHash.getString("DPA.CONN.PROP.MISSING", new Object[] {nextPropDef.getName()}));
					}
				}
			}
		}
		
		List<ReferenceDefinition> refdefs = definition.getReferences();
		if (refdefs != null) {
			Iterator<ReferenceDefinition> refdefsIt = refdefs.iterator();
			while (refdefsIt.hasNext()) {
				ReferenceDefinition nextRefDef = refdefsIt.next();
				ReferenceInstance refInst = null;
				if (references != null) {
					refInst = references.get(nextRefDef.getName());
				}
				
				if (nextRefDef.isRequired() && (refInst == null)) {
					throw new InconsistentInstanceException(resHash.getString("DPA.CONN.REF.MISSING", new Object[] {nextRefDef.getName()}));
				}

				if (!nextRefDef.isMultiple() && (refInst != null) && (refInst.getClassInstances() != null)
						&& refInst.getClassInstances().size() > 1) {
					throw new InconsistentInstanceException(resHash.getString("DPA.CONN.MULTI.REF", new Object[] {nextRefDef.getName()}));
				}
			}
		}
	}	
	
	/**
	 * 
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("\n[CLASS INSTANCE[");
		str.append("\nclassName: " + className);
		
		str.append("\nproperties: ");
		if (properties != null) {
			Set<String> propNameSet = properties.keySet();
			for (String nextName : propNameSet) {
				Object value = properties.get(nextName);
				str.append("\n\t" + nextName + " : " + value);
			}
		}
		
		if (parent != null) {
			str.append("\n[PARENT[ " + parent.getFirstClassInstance());
			str.append("\n] ");
		}
		
		if (references != null) {
			Set<String> refNameSet = references.keySet();
			for (String nextRefName : refNameSet) {
				ReferenceInstance nextRef = references.get(nextRefName);
				str.append("\n[REFERENCE[ " + nextRef.getName());
				List<ClassInstance> instList = nextRef.getClassInstances();
				if (instList != null) {
					for (ClassInstance nextInst : instList) {
						str.append(nextInst.toString());						
					}
				}
				str.append("\n] ");	
			}
		}		
		return str.toString();
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getDescription() {
		StringBuffer str = new StringBuffer();
		str.append("class: " + className);
		str.append(";ID: " + getPrimaryKeyValue());
		if ((parent != null) && (parent.getClassInstanceCount() > 0)){
			str.append("; parent: " + parent.getFirstClassInstance());
		} 
		return str.toString();
	}	
	
}
