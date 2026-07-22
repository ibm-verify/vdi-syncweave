/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model.def;

import java.util.List;

/**
 * The definition for an relationship according CCMDB. 
 * 
 * @author yavor.gologanov
 *
 */
public class CIRelationDefinition extends ModelObjectDefinition {

	private Classification classification = null;
	private List<RelationRuleDefinition> relationRules = null;
	
	/**
	 * 
	 * @param classification
	 */
	public CIRelationDefinition(Classification classification) {
		super(classification.getClassName());
		this.classification = classification;
	}
	
	/**
	 * 
	 * @return Classification
	 */
	public Classification getClassification() {
		return classification;
	}
	
	/**
	 * 
	 * @return List<RelationRuleDefinition>
	 */
	public List<RelationRuleDefinition> getRelationRules() {
		return relationRules;
	}

	/**
	 * 
	 * @param relationRules
	 */
	public void setRelationRules(List<RelationRuleDefinition> relationRules) {
		this.relationRules = relationRules;
	}	

}
