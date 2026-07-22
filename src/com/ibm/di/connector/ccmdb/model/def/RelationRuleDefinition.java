/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model.def;

/**
 * The definition for a relation rule. 
 * 
 * @author yavor.gologanov
 *
 */
public class RelationRuleDefinition {

	private String relationName = null;
	private String sourceClassification = null;
	private String targetClassification = null;
	
	/**
	 * 
	 * @param relationName
	 */
	public RelationRuleDefinition(String relationName) {
		this.relationName = relationName;
	}	
	
	/**
	 * 
	 * @return String
	 */
	public String getRelationName() {
		return relationName;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getSourceClassification() {
		return sourceClassification;
	}
	
	/**
	 * 
	 * @param sourceClassification
	 */
	public void setSourceClassification(String sourceClassification) {
		this.sourceClassification = sourceClassification;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getTargetClassification() {
		return targetClassification;
	}
	
	/**
	 * 
	 * @param targetClassification
	 */
	public void setTargetClassification(String targetClassification) {
		this.targetClassification = targetClassification;
	}
	
}
