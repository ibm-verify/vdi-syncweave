/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model.def;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A set of RelationRuleDefinition instances grouped by relation name.
 * 
 * @author yavor.gologanov
 *
 */
public class RelationRuleSet {

	private Map<String, List<RelationRuleDefinition>> rulesMap = null;	
	
	/**
	 * 
	 * @param rule
	 */
	public void addRelationRule(RelationRuleDefinition rule) {
		if (rulesMap == null) {
			rulesMap = new HashMap<String, List<RelationRuleDefinition>>();
		}
		
		List<RelationRuleDefinition> rules = rulesMap.get(rule.getRelationName());
		if (rules == null) {
			rules = new ArrayList<RelationRuleDefinition>();
			rulesMap.put(rule.getRelationName(), rules);
		}
		rules.add(rule);
	}
	
	/**
	 * 
	 * @return Collection<String>
	 */
	public Collection<String> getRelationNames() {
		if (rulesMap != null) {
			return rulesMap.keySet();
		}
		
		return null;
	}	
		
	/**
	 * 
	 * @param relationName
	 * @return List<RelationRuleDefinition>
	 */
	public List<RelationRuleDefinition> getRelationRules(String relationName) {
		if (rulesMap != null) {
			return rulesMap.get(relationName);
		}
		
		return null;
	}		
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isEmpty() {
		return ((rulesMap == null) || (rulesMap.isEmpty()));
	}
	
}
