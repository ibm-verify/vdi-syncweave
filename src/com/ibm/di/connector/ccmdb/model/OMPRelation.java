/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model;

import com.ibm.di.connector.ccmdb.provider.CCMDBActualCISchema;

/**
 * This class is a representation of an OMP relationship in CCMDB.
 * 
 * @author yavor.gologanov
 *
 */
public class OMPRelation extends ModelObject {

	private ModelObject omp = null;

	/**
	 * 
	 * @return ModelObject
	 */
	public ModelObject getOmp() {
		return omp;
	}

	/**
	 * 
	 * @param omp
	 */
	public void setOmp(ModelObject omp) {
		this.omp = omp;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getOmpGuid() {
		return (String) getProperty(CCMDBActualCISchema.OMPCIRLN_OMPGUID);
	}		
	
}
