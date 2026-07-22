/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * The instances of this class contain one or more ClassInstance objects referred by 
 * another ClassInstance object. 
 * Each reference instance object has a corresponding ReferenceDefinition. 
 * @author yavor.gologanov
 *
 */
public class ReferenceInstance {

	private String name = null;
	private ReferenceDefinition definition = null;
	private List<ClassInstance> classInstanceList = null;
	
	/**
	 * 
	 * @param definition
	 */
	public ReferenceInstance(ReferenceDefinition definition) {
		this.definition = definition;
		this.name = definition.getName();
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return int
	 */
	public int getClassInstanceCount() {
		if (classInstanceList == null) {
			return 0;
		}
		
		return classInstanceList.size();
	}
	
	/**
	 * 
	 * @return List<ClassInstance>
	 */
	public List<ClassInstance> getClassInstances() {
		return classInstanceList;
	}
	
	/**
	 * 
	 * @return ClassInstance
	 */
	public ClassInstance getFirstClassInstance() {
		if (classInstanceList != null) {
			return classInstanceList.get(0);
		} else {
			return null;
		}
	}	
	
	/**
	 * 
	 * @param instance
	 */
	public void addClassInstance(ClassInstance instance) {
		if (classInstanceList == null) {
			classInstanceList = new ArrayList<ClassInstance>();
		}
		classInstanceList.add(instance);
	}
	
	/**
	 * 
	 * @return ReferenceDefinition
	 */
	public ReferenceDefinition getDefinition() {
		return definition;
	}	
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isComposition() {
		return ReferenceDefinition.TYPE_COMPOSITION.equals(definition.getType());
	}
	
}
