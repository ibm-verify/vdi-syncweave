/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model.def;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The definition for an actual configuration item according CCMDB.  
 * 
 * @author yavor.gologanov
 *
 */
public class ActualCIDefinition extends ModelObjectDefinition {

	private Classification classification = null;
	private Map<String, AttributeDefinition> attributes = null;
	private RelationRuleSet sourceRelationRules = null;	
	private RelationRuleSet targetRelationRules = null;	
	private ModelObjectDefinition deletedActualCI = null;
	private OMPRelationDefinition ompRelation = null;
	
	/**
	 * 
	 * @param classification
	 */
	public ActualCIDefinition(Classification classification) {
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
	 * @return OMPRelationDefinition
	 */
	public OMPRelationDefinition getOmpRelation() {
		return ompRelation;
	}

	/**
	 * 
	 * @param ompRelation
	 */
	public void setOmpRelation(OMPRelationDefinition ompRelation) {
		this.ompRelation = ompRelation;
	}

	/**
	 * 
	 * @return ModelObjectDefinition
	 */
	public ModelObjectDefinition getDeletedActualCI() {
		return deletedActualCI;
	}

	/**
	 * 
	 * @param deletedActualCI
	 */
	public void setDeletedActualCI(ModelObjectDefinition deletedActualCI) {
		this.deletedActualCI = deletedActualCI;
	}

	/**
	 * 
	 * @param attribute
	 */
	public void addAttribute(AttributeDefinition attribute) {
		if (attributes == null) {
			attributes = new TreeMap<String, AttributeDefinition>();
		}
		attributes.put(attribute.getName(), attribute);
	}
	
	/**
	 * 
	 * @param name
	 * @return AttributeDefinition
	 */
	public AttributeDefinition getAttribute(String name) {
		if (attributes != null) {
			return attributes.get(name);
		}
		
		return null;
	}
	
	/**
	 * 
	 * @return Collection<AttributeDefinition>
	 */
	public Collection<AttributeDefinition> getAttributes() {
		if (attributes != null) {
			return attributes.values();
		}
		
		return null;
	}
	
	/**
	 * 
	 * @return RelationRuleSet
	 */
	public RelationRuleSet getSourceRelationRules() {
		return sourceRelationRules;
	}

	/**
	 * 
	 * @param relationRules
	 */
	public void setSourceRelationRules(RelationRuleSet relationRules) {
		this.sourceRelationRules = relationRules;
	}

	/**
	 * 
	 * @return RelationRuleSet
	 */
	public RelationRuleSet getTargetRelationRules() {
		return targetRelationRules;
	}

	/**
	 * 
	 * @param relationRules
	 */
	public void setTargetRelationRules(RelationRuleSet relationRules) {
		this.targetRelationRules = relationRules;
	}	
	
	/**
	 * 
	 * @return Set<String>
	 */
	public Set<String> getRelationNames() {
		Set<String> relationNames = new HashSet<String>();
		if ((sourceRelationRules != null) && (!sourceRelationRules.isEmpty())) {
			relationNames.addAll(sourceRelationRules.getRelationNames());
		}
		if ((targetRelationRules != null) && (!targetRelationRules.isEmpty()))  {
			relationNames.addAll(targetRelationRules.getRelationNames());
		}
		
		if (relationNames.size() > 0) {
			return relationNames;
		}
		return null;
	}
	
	/**
	 * 
	 * @param relationName
	 * @return List<RelationRuleDefinition>
	 */
	public List<RelationRuleDefinition> getSourceRelationRules(String relationName) {
		if (sourceRelationRules != null) {
			return sourceRelationRules.getRelationRules(relationName);
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param relationName
	 * @return List<RelationRuleDefinition>
	 */
	public List<RelationRuleDefinition> getTargetRelationRules(String relationName) {
		if (targetRelationRules != null) {
			return targetRelationRules.getRelationRules(relationName);
		}
		
		return null;
	}	
	
}
