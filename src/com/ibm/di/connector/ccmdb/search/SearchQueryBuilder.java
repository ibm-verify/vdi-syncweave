/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.CIRelation;
import com.ibm.di.connector.ccmdb.model.ClassAttribute;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.AttributeDefinition;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.PropertyDefinition;
import com.ibm.di.connector.ccmdb.provider.CCMDBActualCISchema;
import com.ibm.di.connector.ccmdb.provider.SQLQuery;
import com.ibm.di.server.SearchCriteria;

/**
 * This class is a factory for search queries
 * 
 * @author yavor.gologanov
 *
 */
public class SearchQueryBuilder {

	/**
	 * Creates an instance of SQLQuery based on the given criteria object 
	 * and item definition.
	 * 
	 * @param criteria
	 * @param definition
	 * @return SQLQuery
	 */
	public SQLQuery buildQuery(SearchCriteria criteria, ActualCIDefinition definition) {		
		ActualCI searchObject = getSearchObject(criteria, definition);
		boolean matchAny = (criteria.getType() == SearchCriteria.SEARCH_OR);
		return CCMDBActualCISchema.getSearchQuery(searchObject, definition, matchAny);
	}
	
	/**
	 * Creates an instance of SQLQuery based on the given criteria object 
	 * and relation definition.
	 * 
	 * @param criteria
	 * @param definition
	 * @return SQLQuery
	 */
	public SQLQuery buildQuery(SearchCriteria criteria, CIRelationDefinition definition) {
		CIRelation searchObject = getSearchObject(criteria, definition);
		boolean matchAny = (criteria.getType() == SearchCriteria.SEARCH_OR);
		return CCMDBActualCISchema.getSearchQuery(searchObject, definition, matchAny);
	}	
	
	/**
	 * 
	 * @param criteria
	 * @param definition
	 * @return ActualCI
	 */
	private ActualCI getSearchObject(SearchCriteria criteria, 
			ActualCIDefinition definition) {
		ActualCI searchObject = new ActualCI();
		
		Map<String, List<SearchCriteria.rscSearch>> criterionMap = new HashMap<String, List<SearchCriteria.rscSearch>>();
		Iterator<?> criterionIt = criteria.getCriteria().iterator();		
		while (criterionIt.hasNext()) {
			SearchCriteria.rscSearch criterion = (SearchCriteria.rscSearch) criterionIt.next();
			List<SearchCriteria.rscSearch> criterionList = (List<SearchCriteria.rscSearch>) criterionMap.get(criterion.name);
			if (criterionList == null) {
				criterionList = new ArrayList<SearchCriteria.rscSearch>();
				criterionMap.put(criterion.name, criterionList);
			}			
			criterionList.add(criterion);
		}		

		Collection<PropertyDefinition> properties = definition.getProperties();
		for (PropertyDefinition property : properties) {
			String searchName = property.getDisplayName();
			if (property.getDisplayPrefix() != null) {
				searchName = property.getDisplayPrefix() + ":" + searchName;
			}
			List<SearchCriteria.rscSearch> criterionList = criterionMap.get(searchName);
			if (criterionList != null) {
				searchObject.setProperty(property.getName(), criterionList);
			}
		}
		
		Collection<AttributeDefinition> attrDefs = definition.getAttributes();
		if (attrDefs != null) {
			for (AttributeDefinition attDef : attrDefs) {
				String searchName = attDef.getDisplayName();
				if (attDef.getDisplayPrefix() != null) {
					searchName = attDef.getDisplayPrefix() + ":" + searchName;
				}				
				List<SearchCriteria.rscSearch> criterionList = criterionMap.get(searchName);
				if (criterionList != null) {
					ClassAttribute attribute = new ClassAttribute(attDef.getName());
					attribute.setValue(criterionList);
					searchObject.addAttribute(attribute);
				}
			}
		}
		
		return searchObject;
	}
	
	/**
	 * 
	 * @param criteria
	 * @param definition
	 * @return CIRelation
	 */
	private CIRelation getSearchObject(SearchCriteria criteria, CIRelationDefinition definition) {
		CIRelation searchObject = new CIRelation();
		
		Map<String, List<SearchCriteria.rscSearch>> criterionMap = new HashMap<String, List<SearchCriteria.rscSearch>>();
		Iterator<?> criterionIt = criteria.getCriteria().iterator();		
		while (criterionIt.hasNext()) {
			SearchCriteria.rscSearch criterion = (SearchCriteria.rscSearch) criterionIt.next();
			List<SearchCriteria.rscSearch> criterionList = (List<SearchCriteria.rscSearch>) criterionMap.get(criterion.name);
			if (criterionList == null) {
				criterionList = new ArrayList<SearchCriteria.rscSearch>();
				criterionMap.put(criterion.name, criterionList);
			}			
			criterionList.add(criterion);
		}		
		
		Collection<PropertyDefinition> properties = definition.getProperties();
		for (PropertyDefinition property : properties) {
			String searchName = property.getDisplayName();
			if (property.getDisplayPrefix() != null) {
				searchName = property.getDisplayPrefix() + ":" + searchName;
			}			
			List<SearchCriteria.rscSearch> criterionList = criterionMap.get(searchName);
			if (criterionList != null) {
				searchObject.setProperty(property.getName(), criterionList);
			}
		}
		
		return searchObject;
	}		
	
}
