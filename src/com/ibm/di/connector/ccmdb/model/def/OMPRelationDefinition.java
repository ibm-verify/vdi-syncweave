/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model.def;

/**
 * The base definition of the OMP relation according to CCMDB. 
 * 
 * @author yavor.gologanov
 *
 */
public class OMPRelationDefinition extends ModelObjectDefinition {

	private ModelObjectDefinition omp = null;
	
	/**
	 * 
	 * @param className
	 */
	public OMPRelationDefinition(String className) {
		super(className);
	}
	
	/**
	 * 
	 * @return ModelObjectDefinition
	 */
	public ModelObjectDefinition getOmp() {
		return omp;
	}

	/**
	 * 
	 * @param omp
	 */
	public void setOmp(ModelObjectDefinition omp) {
		this.omp = omp;
	}
	
}
