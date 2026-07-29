/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.di.connector.dpa.provider.DeployedAssetsSchema;
import com.ibm.di.connector.dpa.provider.SQLQuery;
import com.ibm.di.connector.dpa.schema.ClassDefinition;
import com.ibm.di.connector.dpa.schema.ClassDefinitionFactory;
import com.ibm.di.connector.dpa.schema.PropertyDefinition;
import com.ibm.di.connector.dpa.schema.PropertySetDefinition;
import com.ibm.di.connector.dpa.schema.ReferenceDefinition;
import com.ibm.di.server.SearchCriteria;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class SearchQueryBuilder {

	private static String CLASS_TABLE_ALIAS = "DA";
	private static String PARENT_TABLE_ALIAS = "PDA";
	private static String JOIN_TABLE_PREFIX = "TAB";
	
	/**
	 * 
	 * @param classDefinition
	 * @param classdefFactory
	 * @return SearchQueryBuilder
	 */
	public static SearchQueryBuilder createQueryBuilder(ClassDefinition classDefinition, 
			ClassDefinitionFactory classdefFactory) {
		
		SearchQueryBuilder queryBuilder = new SearchQueryBuilder();
		queryBuilder.classDefinition = classDefinition;
		queryBuilder.classdefFactory = classdefFactory;
		queryBuilder.attributeInfoMap = DPAsset.getAttributeInfoMap(classDefinition, classdefFactory);
		
		return queryBuilder;
	}
	
	//-------------------------------------------------------------------------
	
	private Map<String, String> joinedTables = new HashMap<String, String>();	
	private List<String> joinStatements = new ArrayList<String>();
	private List<String> conditionStatements = new ArrayList<String>();
	private boolean joinParent = false;
	
	private Map<String, SearchAttributeInfo> attributeInfoMap = null;
	private ClassDefinition classDefinition = null;
	private ClassDefinitionFactory classdefFactory = null;
	private List<Object> parameters = new ArrayList<Object>();
	
	/**
	 * 
	 */
	private SearchQueryBuilder() {
	}
	
	/**
	 * 
	 * @param criteria
	 * @return SQLQuery
	 */
	public SQLQuery buildQuery(SearchCriteria criteria, String assetClass) {
		
		SearchCriteria.rscSearch searchedAssetClass = null;
		List<SearchCriteria.rscSearch> tmpCriterionList = new ArrayList<SearchCriteria.rscSearch>();
		Iterator<?> criterionIt = criteria.getCriteria().iterator();
		
		while (criterionIt.hasNext()) {
			SearchCriteria.rscSearch criterion = (SearchCriteria.rscSearch) criterionIt.next();
			tmpCriterionList.add(criterion);
			if (DeployedAssetsSchema.CLASS_PROPERTY_NAME.equals(criterion.name)) {
				searchedAssetClass = criterion;
			}
		}
		
		if (!DeployedAssetsSchema.DEPLOYED_ASSET.equalsIgnoreCase(assetClass)) {
			if (searchedAssetClass != null)	{	
				searchedAssetClass.value = DeployedAssetsSchema.getClassName(assetClass);
				searchedAssetClass.match = SearchCriteria.EXACT;
			} else {
				searchedAssetClass = new SearchCriteria.rscSearch(DeployedAssetsSchema.CLASS_PROPERTY_NAME,
						SearchCriteria.EXACT, 
						DeployedAssetsSchema.getClassName(assetClass));				
			}
			tmpCriterionList.add(searchedAssetClass);
		} 
		
		SQLQuery query = new SQLQuery();		
		extractClauses(tmpCriterionList);
		
		String operation = " AND\n";
		if (criteria.getType() == SearchCriteria.SEARCH_OR) {
			operation = " OR\n";
		}
		
		String pkName = CLASS_TABLE_ALIAS + "." 
			+ classDefinition.getPrimaryKey().getColumnName();
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ").append(pkName);
		sql.append("\nFROM ").append(classDefinition.getTable());
		sql.append(" ").append(CLASS_TABLE_ALIAS).append("\n");
		
		if (joinParent) {
			sql.append("\n" + getJoinParentStatement());
		}

		if (joinStatements.size() > 0) {
			for (String nextStat : joinStatements) {
				sql.append("\n" + nextStat);
			}
		}

		sql.append(" WHERE \n");
		Iterator<String> conditionStatementsIt = conditionStatements.iterator();
		while (conditionStatementsIt.hasNext()) {
			sql.append("\n" + conditionStatementsIt.next());	
			if (conditionStatementsIt.hasNext()) {
				sql.append("\n" + operation);
			}
		}
		
		query.setSql(sql.toString());
		for (Object parameter : parameters) {
			query.addParameterValue(parameter);
		}
		
		return query;
	}

	/**
	 * 
	 * @param searchCriterionList
	 * @return List<Object>
	 */
	private List<Object> extractClauses(List<SearchCriteria.rscSearch> searchCriterionList) {
		
		List<Object> parameters = new ArrayList<Object>();
		int index = 0;
		Iterator<SearchCriteria.rscSearch> criterionIt = searchCriterionList.iterator();
		while (criterionIt.hasNext()) {
			index++;
			SearchCriteria.rscSearch criterion = criterionIt.next();
			String attrName = criterion.name;
			SearchAttributeInfo attrInfo = attributeInfoMap.get(attrName);
			if (attrInfo != null) {
				
				// Class property
				if (isPrimaryProperty(attrInfo)) {
					String columnName = CLASS_TABLE_ALIAS + "." + attrInfo.getColumnName();
					addClause(columnName, criterion);					
					continue;
				} 
				
				// Parent properties
				if (isParentPrimaryProperty(attrInfo)) {
					joinParent = true;
					
					String columnName = PARENT_TABLE_ALIAS + "." + attrInfo.getColumnName();
					addClause(columnName, criterion);
					continue;
				} 
			
				// Additional properties!
				if (isAdditionalProperty(attrInfo)) {
					String psname = attrInfo.getPropertySetName();
					PropertySetDefinition propSet = classDefinition.getAdditionalProperties(psname);
					if (propSet != null) {
						String tableAlias = joinedTables.get(propSet.getTable());
						if (tableAlias == null) {
							tableAlias = JOIN_TABLE_PREFIX + joinedTables.size() + 1;
							String onColumn = CLASS_TABLE_ALIAS + "." + getColumnName(propSet.getOnProperty());
							String joinColumn = tableAlias + "." + propSet.getJoinColumn();
						
							StringBuffer joinStatement = new StringBuffer();
							joinStatement.append(" JOIN ");
							joinStatement.append(propSet.getTable());
							joinStatement.append(" "+ tableAlias + " ");
							joinStatement.append("ON (");
							joinStatement.append(onColumn);
							joinStatement.append("=").append(joinColumn).append(")");
							joinStatements.add(joinStatement.toString());
						
							joinedTables.put(propSet.getTable(), tableAlias);
						}
						
						String columnName = tableAlias + "." + attrInfo.getColumnName();
						addClause(columnName, criterion);
						continue;						
					}
				}
				
				// Additional properties from reference
				// To be done!
			}
		}
		
		return parameters;
	}
	
	/**
	 * 
	 * @param columnName
	 * @param criterion
	 */
	private void addClause(String columnName, SearchCriteria.rscSearch criterion) {
		switch (criterion.match) {
		case SearchCriteria.NOT_STRING:
			parameters.add(criterion.value);
			conditionStatements.add(" " + columnName + " != ?");
			break;
		case SearchCriteria.SUBSTRING:
			parameters.add("%" + criterion.value + "%");
			conditionStatements.add(" " + columnName + " LIKE ?");
			break;
		case SearchCriteria.INITIAL_STRING:
			parameters.add(criterion.value + "%");
			conditionStatements.add(" " + columnName + " LIKE ?");
			break;
		case SearchCriteria.FINAL_STRING:
			parameters.add("%" + criterion.value );
			conditionStatements.add(" " + columnName + " LIKE ?");
			break;
		case SearchCriteria.GREATER_THAN:
			parameters.add(criterion.value );
			conditionStatements.add(" " + columnName + " > ?");
			break;
		case SearchCriteria.GREATER_THAN_OR_EQUAL:
			parameters.add(criterion.value );
			conditionStatements.add(" " + columnName + " >= ?");
			break;
		case SearchCriteria.LESS_THAN:
			parameters.add(criterion.value );
			conditionStatements.add(" " + columnName + " < ?");
			break;
		case SearchCriteria.LESS_THAN_OR_EQUAL:
			parameters.add(criterion.value );
			conditionStatements.add(" " + columnName + " <= ?");
			break;
		default:
			parameters.add(criterion.value );
			conditionStatements.add(" " + columnName + " = ?");
		}

	}	
	
	/**
	 * 
	 * @param propertyName
	 * @return String
	 */
	private String getColumnName(String propertyName) {
		List<PropertyDefinition> properties = classDefinition.getPropertyList();
		if (properties != null) {
			for (PropertyDefinition nextProp : properties) {
				if (nextProp.getName().equals(propertyName)) {
					return nextProp.getColumnName();
				}
			}
		}
		return null;
	}	
	
	/**
	 * 
	 * @return String
	 */
	private String getJoinParentStatement() {
		ReferenceDefinition parentRefDef = classDefinition.getParent(); 
		ClassDefinition parentClassDefinition = classdefFactory.getDefinition(parentRefDef.getClassName());
		
		String onColumn = getColumnName(parentRefDef.getOnProperty());		
		onColumn = CLASS_TABLE_ALIAS + "." + onColumn;
		String joinColumn = PARENT_TABLE_ALIAS  + "." + parentRefDef.getColumnName();	
		
		StringBuffer joinStatement = new StringBuffer();
		joinStatement.append(" JOIN ");
		joinStatement.append(parentClassDefinition.getTable());
		joinStatement.append(" "+ PARENT_TABLE_ALIAS + " ");
		joinStatement.append("ON (");
		joinStatement.append(onColumn);
		joinStatement.append("=").append(joinColumn).append(")");
		return joinStatement.toString();
	}		
	
	/**
	 * 
	 * @param attrInfo
	 * @return boolean
	 */
	private boolean isPrimaryProperty(SearchAttributeInfo attrInfo) {
		if (attrInfo.getReferencePath() != null) {
			return false;
		}
		
		if (attrInfo.getPropertySetName() != null) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param attrInfo
	 * @return boolean
	 */
	private boolean isAdditionalProperty(SearchAttributeInfo attrInfo) {
		if (attrInfo.getReferencePath() != null) {
			return false;
		}
		
		if (attrInfo.getPropertySetName() == null) {
			return false;
		}
		
		return true;
	}	
	
	/**
	 * 
	 * @param attrInfo
	 * @return boolean
	 */
	private boolean isParentPrimaryProperty(SearchAttributeInfo attrInfo) {
		
		if (attrInfo.getPropertySetName() != null) {
			return false;
		}
		
		String parentRelationName = ClassDefinitionFactory.PARENT_RELATION_NAME;
		if (parentRelationName.equalsIgnoreCase(attrInfo.getReferencePath())) {
			return true;
		}
		
		return false;
	}	
	
}
