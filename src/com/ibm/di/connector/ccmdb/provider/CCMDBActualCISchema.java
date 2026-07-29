/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.ibm.di.connector.ccmdb.CCMDBException;
import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.CIRelation;
import com.ibm.di.connector.ccmdb.model.ClassAttribute;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.AttributeDefinition;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.PropertyDefinition;
import com.ibm.di.connector.ccmdb.search.QueryClause;
import com.ibm.di.server.SearchCriteria;
import com.ibm.tivoli.namereconciliation.guid.Guid;
import com.ibm.tivoli.namereconciliation.guid.GuidFactory;

/**
 * An utility class for generating database specific SQL queries.
 * 
 * @author yavor.gologanov
 *
 */
public class CCMDBActualCISchema {

	public static final String ATTR_NUM_VALUE = "NUMVALUE";
	
	// Database columns constants 
	public static final String ACTCI_ACTCINUM = "ACTCINUM";
	public static final String ACTCI_ACTCINAME = "ACTCINAME";
	public static final String ACTCI_GUID = "GUID";	
	public static final String ACTCI_ACTCIID = "ACTCIID";	
	public static final String ACTCI_CLASSSTRUCTUREID = "CLASSSTRUCTUREID";	
	public static final String ACTCI_CHANGEBY = "CHANGEBY";	
	public static final String ACTCI_CHANGEDATE = "CHANGEDATE";	
	public static final String ACTCI_LASTSCANDT = "LASTSCANDT";	
	public static final String ACTCI_LANGCODE = "LANGCODE";	
	public static final String ACTCI_HASLD = "HASLD";	
	public static final String ACTCI_DESCRIPTION = "DESCRIPTION";
	public static final String ACTCI_PLUSPCUSTOMER = "PLUSPCUSTOMER";
		
	public static final String ACTCISPEC_ACTCISPECID = "ACTCISPECID";
	public static final String ACTCISPEC_ASSETATTRID = "ASSETATTRID";	
	public static final String ACTCISPEC_CHANGEBY = "CHANGEBY";
	public static final String ACTCISPEC_CHANGEDATE = "CHANGEDATE";
	public static final String ACTCISPEC_ACTCINUM = "ACTCINUM";
	public static final String ACTCISPEC_CLASSSTRUCTUREID = "CLASSSTRUCTUREID";
	
	public static final String ACTCIRELATION_ACTCIRELATIONID = "ACTCIRELATIONID";
	public static final String ACTCIRELATION_RELATIONNUM = "RELATIONNUM";
	public static final String ACTCIRELATION_ANCESTORCI = "ANCESTORCI";
	public static final String ACTCIRELATION_SOURCECI = "SOURCECI";
	public static final String ACTCIRELATION_SOURCECIGUID = "SOURCECIGUID";
	public static final String ACTCIRELATION_TARGETCI = "TARGETCI";
	public static final String ACTCIRELATION_TARGETCIGUID = "TARGETCIGUID";
	public static final String ACTCIRELATION_CHANGEBY = "CHANGEBY";	
	public static final String ACTCIRELATION_CHANGEDATE = "CHANGEDATE";	
	public static final String ACTCIRELATION_SWAPPED = "SWAPPED";	
	
	public static final String OMPCIRLN_OMPCIRLNID = "OMPCIRLNID";
	public static final String OMPCIRLN_OMPGUID = "OMPGUID";
	
	public static final String OMP_OMPID = "OMPID";
	public static final String OMP_OMPGUID = "OMPGUID";
	public static final String OMP_VERSION = "VERSION";
	public static final String OMP_CHANGEDATE = "CHANGEDATE";
	public static final String OMP_DISPLAYLABEL = "DISPLAYLABEL";
	public static final String OMP_CHANGEBY = "CHANGEBY";
	public static final String OMP_NAME = "NAME";
	public static final String OMP_PRODUCTNAME = "PRODUCTNAME";	
	
	// Model object attributes
	public static final String MODELOBJECT_ADMINSTATE = "MODELOBJECT_ADMINSTATE";
	public static final String MODELOBJECT_BIDIFLAG = "MODELOBJECT_BIDIFLAG";
	public static final String MODELOBJECT_BIDIFORMAT = "MODELOBJECT_BIDIFORMAT";
	public static final String MODELOBJECT_CDMSOURCE = "MODELOBJECT_CDMSOURCE";
	public static final String MODELOBJECT_CONTEXTIP = "MODELOBJECT_CONTEXTIP";
	public static final String MODELOBJECT_CREATEDBY = "MODELOBJECT_CREATEDBY";
	public static final String MODELOBJECT_DESCRIPTION = "MODELOBJECT_DESCRIPTION";
	public static final String MODELOBJECT_DISPLAYNAME = "MODELOBJECT_DISPLAYNAME";
	public static final String MODELOBJECT_EXTENDEDATTRIBUTES = "MODELOBJECT_EXTENDEDATTRIBUTES";
	public static final String MODELOBJECT_LABEL = "MODELOBJECT_LABEL";
	public static final String MODELOBJECT_LASTMODIFIEDBY = "MODELOBJECT_LASTMODIFIEDBY";
	public static final String MODELOBJECT_LASTMODIFIEDTIME = "MODELOBJECT_LASTMODIFIEDTIME";
	public static final String MODELOBJECT_OBJECTTYPE = "MODELOBJECT_OBJECTTYPE";
	public static final String MODELOBJECT_SOURCETOKEN = "MODELOBJECT_SOURCETOKEN";
	
	
	/**
	 * 
	 * @param attrType
	 * @return String
	 */
	public static String getAttrJavaClass(String attrType) {
		if ("ALN".equals(attrType)) {
			return String.class.getCanonicalName();
		} else if ("NUMERIC".equals(attrType)) {
			return Double.class.getCanonicalName();
		} else if ("TABLE".equals(attrType)) {
			return String.class.getCanonicalName();
		}
		
		return Object.class.getCanonicalName();
	}
	
	/**
	 * 
	 * @param attrType
	 * @return String
	 */
	public static String getAttrValueColumn(String attrType) {
		if ("ALN".equals(attrType)) {
			return "ALNVALUE";
		} else if ("NUMERIC".equals(attrType)) {
			return "NUMVALUE";
		} else if ("TABLE".equals(attrType)) {
			return "TABLEVALUE";
		}
		
		return "ALNVALUE";
	}	
	
	/**
	 * 
	 * @param searchObject
	 * @param definition
	 * @param matchAny
	 * @return SQLQuery
	 */
	public static SQLQuery getSearchQuery(ActualCI searchObject, 
			ActualCIDefinition definition,
			boolean matchAny) {
		
		StringBuffer sql = new StringBuffer();
		SQLQuery searchQuery = new SQLQuery();
		String operation = " AND\n";
		if (matchAny) {
			operation = " OR\n";
		}		
		
		sql.append("SELECT CI.* FROM MAXIMO.ACTCI CI");
		sql.append("\nWHERE CI.ACTCIID\n"); 
		sql.append("\nIN");
		sql.append("\n( SELECT DISTINCT ACI.ACTCIID FROM MAXIMO.ACTCI ACI");
		if (searchObject.getAttributeCount() > 0) {
			sql.append("\nJOIN MAXIMO.ACTCISPEC ASP ON (ACI.ACTCINUM=ASP.ACTCINUM)");
		}
		
		String classstructureClause = null;
		if (!definition.getClassification().getClassName().equals(CCMDBActualCIProvider.CLASS_ACTUAL_CI)) {
			classstructureClause = "\n\t\tACI.CLASSSTRUCTUREID = '" +
				definition.getClassification().getClassstructureId() + "'";
		}
		
		List<String> whereClauses = new ArrayList<String>();
		if ((searchObject.getPropertyCount() > 0) ||(searchObject.getAttributeCount() > 0))
		{
			Set<String> propertyNames = searchObject.getPropertyNames();
			if (propertyNames != null) {
				Iterator<String> propNamesIt = propertyNames.iterator();
				while (propNamesIt.hasNext()) {
					String propertyName = propNamesIt.next();
					
					List<SearchCriteria.rscSearch> criterionList = 
						(List<SearchCriteria.rscSearch>) searchObject.getProperty(propertyName);
					
					Iterator<SearchCriteria.rscSearch> criterionIt = criterionList.iterator();
					while (criterionIt.hasNext()) {
						SearchCriteria.rscSearch criterion = criterionIt.next();
						QueryClause clause = QueryClause.getClause("CI." + propertyName, criterion);
						whereClauses.add("\n\t\t" + clause.getExpression());
						searchQuery.addParameterValue(clause.getValue());
					}
				}
			}
		
			Set<String> attributeNames = searchObject.getAttributeNames();
			if (attributeNames != null) {
				Iterator<String> attrNamesIt = attributeNames.iterator();
				while (attrNamesIt.hasNext()) {
					String attrName = attrNamesIt.next();
					AttributeDefinition attrDef = definition.getAttribute(attrName);
					
					ClassAttribute attribute = searchObject.getAttribute(attrName);					
					List<SearchCriteria.rscSearch> criterionList = 
						(List<SearchCriteria.rscSearch>) attribute.getValue();
					
					Iterator<SearchCriteria.rscSearch> criterionIt = criterionList.iterator();
					StringBuilder strBuild = new StringBuilder ();
					while (criterionIt.hasNext()) {
						SearchCriteria.rscSearch criterion = criterionIt.next();
						strBuild.append("\n\t\t ((ASP.ASSETATTRID = '" + attrName + "') AND ");
						QueryClause clause = QueryClause.getClause("ASP." + attrDef.getValueField(), criterion);
						strBuild.append(clause.getExpression() + ")");
						whereClauses.add(strBuild.toString());
						
						searchQuery.addParameterValue(clause.getValue());
						strBuild.setLength(0);
					}
				}
			}
		}				
		
		if ((classstructureClause != null) || (!whereClauses.isEmpty())){
			sql.append("\n\tWHERE ");
		}
		
		if (classstructureClause != null) {
			sql.append(classstructureClause);
			if (!whereClauses.isEmpty()) {
				sql.append(" AND ");
			}
		}
		
		if (!whereClauses.isEmpty()) {
			sql.append(" ( ");
			Iterator<String> clauseIt = whereClauses.iterator();
			while (clauseIt.hasNext()) {
				sql.append(clauseIt.next());
				if (clauseIt.hasNext()) {
					sql.append(operation);
				}
			}
			sql.append(" ) ");
		}
		sql.append(" )");	
		
		searchQuery.setSql(sql.toString());
		return searchQuery;
	}	
	
	/**
	 * 
	 * @param searchObject
	 * @param definition
	 * @param matchAny
	 * @return SQLQuery
	 */
	public static SQLQuery getSearchQuery(CIRelation searchObject, 
			CIRelationDefinition definition,
			boolean matchAny) {
		
		StringBuffer sql = new StringBuffer();
		SQLQuery searchQuery = new SQLQuery();
		
		String operation = " AND\n";
		if (matchAny) {
			operation = " OR\n";
		}	
		
		sql.append("SELECT AR.* FROM MAXIMO.ACTCIRELATION AR ");
		
		String classstructureClause = null;
		if (!definition.getClassification().getClassName().equals(CCMDBActualCIProvider.CLASS_CI_RELATION)) {
			classstructureClause = "\n\tAR.RELATIONNUM = '" + 
			definition.getClassification().getClassName() + "'";
		}
		
		List<String> whereClauses = new ArrayList<String>();
		if (searchObject.getPropertyCount() > 0)
		{
			Set<String> propertyNames = searchObject.getPropertyNames();
			if (propertyNames != null) {
				Iterator<String> propNamesIt = propertyNames.iterator();
				while (propNamesIt.hasNext()) {
					String propertyName = propNamesIt.next();
					List<SearchCriteria.rscSearch> criterionList = 
						(List<SearchCriteria.rscSearch>) searchObject.getProperty(propertyName);					
					Iterator<SearchCriteria.rscSearch> criterionIt = criterionList.iterator();
					while (criterionIt.hasNext()) {
						SearchCriteria.rscSearch criterion = criterionIt.next();
						QueryClause clause = QueryClause.getClause("AR." + propertyName, criterion);
						whereClauses.add("\n\t\t" + clause.getExpression());
						searchQuery.addParameterValue(clause.getValue());
					}
				}
			}
		}
		
		if ((classstructureClause != null) || (!whereClauses.isEmpty())){
			sql.append("\n\tWHERE ");
		}
		
		if (classstructureClause != null) {
			sql.append(classstructureClause);
			if (!whereClauses.isEmpty()) {
				sql.append(" AND ");
			}
		}
		
		if (!whereClauses.isEmpty()) {
			sql.append(" ( ");
			Iterator<String> clauseIt = whereClauses.iterator();
			while (clauseIt.hasNext()) {
				sql.append(clauseIt.next());
				if (clauseIt.hasNext()) {
					sql.append(operation);
				}
			}
			sql.append(" ) ");
		}
				
		searchQuery.setSql(sql.toString());
		return searchQuery;
	}		
		
	
	/**
	 * 
	 * @param relation
	 * @param definition
	 * @return SQLQuery
	 */
	public static SQLQuery createInsertQuery(CIRelation relation, 
			CIRelationDefinition definition) {
		
		StringBuffer columnsStatement = new StringBuffer("\n\t(");
		StringBuffer valuesStatement = new StringBuffer("\n\t(");
		
		Map<String, Object> columns = new TreeMap<String, Object>();		
		Collection<PropertyDefinition> properties = definition.getProperties();
		Iterator<PropertyDefinition> propertiesIt = properties.iterator();
		while (propertiesIt.hasNext()) {
			Object value = null;
			PropertyDefinition nextProp = propertiesIt.next();
			value = relation.getProperty(nextProp.getName());
			if (value != null) {
				columns.put(nextProp.getName(), value);
			}
		}
		
		SQLQuery query = new SQLQuery();
		Iterator<Map.Entry<String,Object>> columnsIt = columns.entrySet().iterator();	
		while (columnsIt.hasNext()) {
			Map.Entry<String,Object> column = columnsIt.next();
			
			columnsStatement.append(column.getKey());
			
			if (column.getKey().equalsIgnoreCase(ACTCIRELATION_SOURCECI) 
					|| column.getKey().equalsIgnoreCase(ACTCIRELATION_TARGETCI)
					|| column.getKey().equalsIgnoreCase(ACTCIRELATION_RELATIONNUM)
					|| column.getKey().equalsIgnoreCase(ACTCIRELATION_ANCESTORCI)
					|| column.getKey().equalsIgnoreCase(ACTCIRELATION_CHANGEBY)
					|| column.getKey().equalsIgnoreCase(ACTCIRELATION_SOURCECIGUID)
					|| column.getKey().equalsIgnoreCase(ACTCIRELATION_TARGETCIGUID)) {
				valuesStatement.append(" VARGRAPHIC(" + format((String) column.getValue()) +  ")");
			} else {
				valuesStatement.append("?");
				query.addParameterValue(column.getValue());
			}
							
			if (columnsIt.hasNext()) {
				columnsStatement.append(", ");
				valuesStatement.append(", ");
			} else {
				columnsStatement.append(")");
				valuesStatement.append(")");
			}
		}		
		
		StringBuffer sql = new StringBuffer("INSERT INTO MAXIMO.ACTCIRELATION ");
		sql.append(columnsStatement.toString());
		sql.append("\n\t VALUES ");
		sql.append(valuesStatement.toString());		
		
		query.setSql(sql.toString());
		return query;
	}	
		
	/**
	 * 
	 * @param configItem
	 * @param definition
	 * @return SQLQuery
	 */
	public static SQLQuery createInsertQuery(ActualCI configItem, 
			ActualCIDefinition definition) {
		
		StringBuffer columnsStatement = new StringBuffer("\n\t(");
		StringBuffer valuesStatement = new StringBuffer("\n\t(");
		
		Map<String, Object> columns = new TreeMap<String, Object>();		
		Collection<PropertyDefinition> properties = definition.getProperties();
		Iterator<PropertyDefinition> propertiesIt = properties.iterator();		
		while (propertiesIt.hasNext()) {
			Object value = null;
			
			PropertyDefinition nextProp = propertiesIt.next();
			value = configItem.getProperty(nextProp.getName());			 
			if (value != null) {
				columns.put(nextProp.getName(), value);
			}
		}		
		
		SQLQuery query = new SQLQuery();
		Iterator<Map.Entry<String,Object>> columnsIt = columns.entrySet().iterator();	
		while (columnsIt.hasNext()) {
			Map.Entry<String,Object> column = columnsIt.next();
			
			columnsStatement.append(column.getKey());
			
			if (column.getKey().equalsIgnoreCase(ACTCI_ACTCINUM) 
					|| column.getKey().equalsIgnoreCase(ACTCI_DESCRIPTION)
					|| column.getKey().equalsIgnoreCase(ACTCI_LANGCODE)
					|| column.getKey().equalsIgnoreCase(ACTCI_GUID)
					|| column.getKey().equalsIgnoreCase(ACTCI_CHANGEBY)
					|| column.getKey().equalsIgnoreCase(ACTCI_CLASSSTRUCTUREID)
					|| column.getKey().equalsIgnoreCase(ACTCI_ACTCINAME)
					|| column.getKey().equalsIgnoreCase(ACTCI_PLUSPCUSTOMER)) {
				valuesStatement.append(" VARGRAPHIC(" + format((String) column.getValue()) +  ")");
			} else {
				valuesStatement.append("?");
				query.addParameterValue(column.getValue());
			}
				
			if (columnsIt.hasNext()) {
				columnsStatement.append(", ");
				valuesStatement.append(", ");
			} else {
				columnsStatement.append(")");
				valuesStatement.append(")");
			}
		}
		
		StringBuffer sql = new StringBuffer("INSERT INTO MAXIMO.ACTCI ");
		sql.append(columnsStatement.toString());
		sql.append("\n\t VALUES ");
		sql.append(valuesStatement.toString());
		
		query.setSql(sql.toString());
		return query;
	}	
	
	/**
	 * 
	 * @param attrDef
	 * @param attribute
	 * @return SQLQuery
	 */
	public static SQLQuery createInsertQuery(AttributeDefinition attrDef, 
			ClassAttribute attribute) {
		StringBuffer sql = new StringBuffer("INSERT INTO MAXIMO.ACTCISPEC");
		sql.append(" (ACTCISPECID, ASSETATTRID, ACTCINUM, CHANGEBY, ");
		sql.append("CHANGEDATE, CLASSSTRUCTUREID, DISPLAYSEQUENCE, MANDATORY, ");
		sql.append(attrDef.getValueField());
		sql.append(")");
		if (!attrDef.getValueField().equalsIgnoreCase("NUMVALUE")) {
			sql.append("\n VALUES (?, ");
			sql.append("VARGRAPHIC(").append(format(attribute.getStringProperty(ACTCISPEC_ASSETATTRID))).append("), ");
			sql.append("VARGRAPHIC(").append(format(attribute.getStringProperty(ACTCISPEC_ACTCINUM))).append("), ");
			sql.append("VARGRAPHIC(").append(format(attribute.getStringProperty(ACTCISPEC_CHANGEBY))).append("), ");
			sql.append(" ?,");
			sql.append("VARGRAPHIC(").append(format(attribute.getStringProperty(ACTCISPEC_CLASSSTRUCTUREID))).append("), ");
			sql.append("0, 0, ");
			sql.append("VARGRAPHIC(").append(format((String) attribute.getValue())).append(")) ");
		} else {
			sql.append("\n VALUES (?, ");
			sql.append("VARGRAPHIC(").append(format(attribute.getStringProperty(ACTCISPEC_ASSETATTRID))).append("), ");
			sql.append("VARGRAPHIC(").append(format(attribute.getStringProperty(ACTCISPEC_ACTCINUM))).append("), ");
			sql.append("VARGRAPHIC(").append(format(attribute.getStringProperty(ACTCISPEC_CHANGEBY))).append("), ");
			sql.append(" ?,");
			sql.append("VARGRAPHIC(").append(format(attribute.getStringProperty(ACTCISPEC_CLASSSTRUCTUREID))).append("), ");
			sql.append("0, 0, ");
			sql.append("? )");
		}
				
		SQLQuery query = new SQLQuery();
		if (!attrDef.getValueField().equalsIgnoreCase("NUMVALUE")) {
			query.addParameterValue(attribute.getProperty(ACTCISPEC_ACTCISPECID));
			query.addParameterValue(attribute.getProperty(ACTCISPEC_CHANGEDATE));
		} else {
			query.addParameterValue(attribute.getProperty(ACTCISPEC_ACTCISPECID));
			query.addParameterValue(attribute.getProperty(ACTCISPEC_CHANGEDATE));
			query.addParameterValue(attribute.getValue());
		}
		
		query.setSql(sql.toString());
		return query;
	}	
	
	/**
	 * 
	 * @param attrDef
	 * @param attribute
	 * @return SQLQuery
	 */
	public static SQLQuery createUpdateQuery(AttributeDefinition attrDef, 
			ClassAttribute attribute) {
		SQLQuery query = new SQLQuery();
		
		StringBuffer sql = new StringBuffer("UPDATE MAXIMO.ACTCISPEC");
		sql.append(" SET ");
		sql.append(attrDef.getValueField());
		if (!attrDef.getValueField().equalsIgnoreCase("NUMVALUE")) {
			sql.append(" = VARGRAPHIC(" + format((String) attribute.getValue()) +") ");
		} else {
			sql.append(" = ? ");
			query.addParameterValue(attribute.getValue());
		}		
		sql.append(" WHERE ASSETATTRID = ? AND ACTCINUM = ?");		
		query.addParameterValue(attrDef.getName());
		query.addParameterValue(attribute.getProperty(ACTCISPEC_ACTCINUM));
				
		query.setSql(sql.toString());
		return query;
	}		
	
	/**
	 * 
	 * @param item
	 * @return String
	 * @throws CCMDBException
	 */
	public static String createGuid(ActualCI item) throws CCMDBException {
		try {
			Guid guid = GuidFactory.getDefaultGuidFactory().createGuid(item.toUniqueString());
			return guid.toString();
		} catch (Exception e) {
			throw new CCMDBException(e);
		}
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	private static String format(String value) {
		if (value == null) {
			return "''";
		}
		return "'" + value.replace("'", "''").trim() + "'";
	}
	
}
