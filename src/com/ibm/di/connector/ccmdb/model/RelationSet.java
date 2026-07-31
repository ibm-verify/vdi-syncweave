/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This is an utility class that keeps relation instances grouped by relation name.
 * 
 * @author yavor.gologanov
 *
 */
public class RelationSet {

	private Map<String, List<CIRelation>> relations = null;
	
	/**
	 * 
	 * @param relation
	 */
	public void addRelation(CIRelation relation) {
		if (relations == null) {
			relations = new TreeMap<String, List<CIRelation>>();
		}
		
		List<CIRelation> relationList = relations.get(relation.getRelationnum());
		if (relationList == null) {
			relationList = new ArrayList<CIRelation>();
			relations.put(relation.getRelationnum(), relationList);
		}
		
		relationList.add(relation);
	}	
	
	/**
	 * 
	 * @return int
	 */
	public int getRelationCount() {
		if (relations != null) {
			return relations.size();
		}
		return 0;
	}	
	
	/**
	 * 
	 * @return Set<String>
	 */
	public Set<String> getRelationNames() {
		if (relations != null) {
			return relations.keySet();
		}
		return null;
	}	
	
	/**
	 * 
	 * @return List<CIRelation>
	 */
	public List<CIRelation> getRelations(String relationName) {
		if (relations != null) {
			return relations.get(relationName);
		}
		return null;
	}	
	
	/**
	 * 
	 * @return List<CIRelation>
	 */
	public List<CIRelation> getAllRelations() {
		List<CIRelation> allRelations = new ArrayList<CIRelation>();
		if (relations != null) {
			Set<String> relationNames = getRelationNames();
			for (String relationName : relationNames) {
				List<CIRelation> relList = getRelations(relationName);
				if (relList != null) {
					allRelations.addAll(relList);
				}
			}
		}
		
		return allRelations;
	}	
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isEmpty() {
		if (relations != null) {
			return relations.isEmpty();
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param relation
	 * @return boolean
	 */
	public boolean contains(CIRelation relation) {
		List<CIRelation> allRelations = getAllRelations();
		for (CIRelation nextRelation : allRelations) {
			if (nextRelation.isIdentical(relation)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * 
	 */
	public String toString() {
		if (relations != null) {
			return relations.toString();
		}
		
		return RelationSet.class.getCanonicalName();
	}	
	
}
