/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.provider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.di.connector.ccmdb.model.def.Classification;

/**
 * This class defines methods for reading of classification types.  
 * 
 * @author yavor.gologanov
 *
 */
public class ClassificationProvider {

	public static final String UNCLASSIFIED = "unclassified";
	
	public static final String UNCLASSIFIED_RELATION = "unclassifiedRelation";
	
	private CCMDBActualCIProvider provider = null;
	
	private Map<String, Classification> classificationMap = 
		new HashMap<String, Classification>();
	
	/**
	 * 
	 * @param provider
	 */
	protected ClassificationProvider(CCMDBActualCIProvider provider) {
		this.provider = provider;
	}
	
	/**
	 * 
	 * @throws SQLException
	 */
	public void init() throws SQLException {

		List<Classification> classList = getActualCIClassifications();
		
		Classification baseActualCI = new Classification();
		baseActualCI.setClassName(CCMDBActualCIProvider.CLASS_ACTUAL_CI);
		baseActualCI.setClassstructureId("undefined");
		classList.add(baseActualCI);		
		
		Classification baseCIRelation = new Classification();
		baseCIRelation.setClassName(CCMDBActualCIProvider.CLASS_CI_RELATION);
		baseCIRelation.setClassstructureId("undefined");
		classList.add(baseCIRelation);		
		
		for (Classification classification : classList) {
			classificationMap.put(classification.getClassName(), classification);
		}		
		
		// Load relation classifications
		classList = getCIRelationClassifications();
		for (Classification classification : classList) {
			classificationMap.put(classification.getClassName(), classification);
		}
		
		Classification unclassified = new Classification();
		unclassified.setClassstructureId("-1");
		unclassified.setClassName("unclassified");
		classificationMap.put(UNCLASSIFIED, unclassified);
	}
	
	/**
	 * 
	 * @param className
	 * @return List<Classification>
	 * @throws SQLException
	 */
	public List<Classification> getClassifications(String className) 
		throws SQLException {

		if (CCMDBActualCIProvider.CLASS_ACTUAL_CI.equals(className)) {
			return getActualCIClassifications() ;
		} else if (CCMDBActualCIProvider.CLASS_CI_RELATION.equals(className)) {
			return getCIRelationClassifications();
		}
		
		return null;
	}	
	
	/**
	 * 
	 * @param className
	 * @return List<String>
	 * @throws SQLException
	 */
	public List<String> getClassificationNames(String className) 
		throws SQLException {

		List<Classification> classificationList = getClassifications(className) ;
		if (classificationList != null) {
			List<String> classifications = new ArrayList<String>();
			for (Classification classification : classificationList) {
				classifications.add(classification.getClassName());
			}

			Collections.sort(classifications);
			return classifications;
		} 
		
		return null;
	}	
	
	/**
	 * 
	 * @param name
	 * @return Classification
	 */
	public Classification getClasssification(String name) {
		if (classificationMap.containsKey(name)) {
			return classificationMap.get(name);
		} 
		
		return null;
	}	
	
	/**
	 * 
	 * @param classstructureId
	 * @return Classification
	 */
	public Classification getClasssificationByClassstructure(String classstructureId) {
		try
		{
			for (Classification classification : classificationMap.values()) {
				if (classification.getClassstructureId().equals(classstructureId)) {
					return classification;
				}
			}
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}	
	
	
	/**
	 * 
	 * @param className
	 * @return Classification
	 */
	public Classification getClasssificationByClassType(String className) {
		try
		{
			for (Classification classification : classificationMap.values()) {
				if (classification.getClassName().equalsIgnoreCase(className)) {
					return classification;
				}
			}
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}	
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @return List<Classification>
	 * @throws SQLException
	 */
	private List<Classification> getActualCIClassifications() 
		throws SQLException {

		List<Classification> allClassificationList = getActualCIClassificationList();
		
		List<Classification> classificationList = getActualCIClassificationList();
		allClassificationList.addAll(classificationList);
		
		for (Classification classification : classificationList) {
			loadClassificationTree(classification, allClassificationList);
		}

		return allClassificationList;
	}	
	
	/**
	 * 
	 * @return List<Classification>
	 * @throws SQLException
	 */
	private List<Classification> getCIRelationClassifications() 
		throws SQLException {

		String sql = provider.getSQL(QuerySet.ACTCIRELATION_SELECT_CLASSIFICATIONS);
		List<Classification> list = new ArrayList<Classification>();		
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		try {
			statement = provider.getConnection().prepareStatement(sql);
			resultSet = statement.executeQuery();

			while (resultSet.next()) {
				String classsificationId = resultSet.getString(1);
				String classstructureId = resultSet.getString(2);
				int hasChildren = resultSet.getInt(3);
				Classification classification = new Classification();
				classification.setClassName(classsificationId);
				classification.setClassstructureId(classstructureId);
				classification.setHasChildren(hasChildren > 0);
				list.add(classification);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
			
		return list;
	}	
	
    /**
     *  
     * @return List<Classification>
     * @throws SQLException
     */
	private List<Classification> getActualCIClassificationList() throws SQLException {
		String sql = provider.getSQL(QuerySet.ACTCI_SELECT_CLASSIFICATIONS);
		List<Classification> list = new ArrayList<Classification>();
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		try {
			//defect 15062
			provider.getConnection().setAutoCommit(false);
			statement = provider.getConnection().prepareStatement(sql);
			resultSet = statement.executeQuery();

			while (resultSet.next()) {
				String classsificationId = resultSet.getString(1);
				String classstructureId = resultSet.getString(2);
				int hasChildren = resultSet.getInt(3);
				Classification classification = new Classification();
				classification.setClassName(classsificationId);
				classification.setClassstructureId(classstructureId);
				classification.setHasChildren(hasChildren > 0);
				list.add(classification);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			//defect 15062
			provider.getConnection().setAutoCommit(true);
		}
		
		return list;
	}	
	
	/**
	 * 
	 * @param classification
	 * @param allClassifications
	 * @throws SQLException
	 */
	private void loadClassificationTree(Classification classification, 
			List<Classification> allClassifications) throws SQLException {
		
		if (classification.isHasChildren()) {
			List<Classification> children = getClassificationsByParent(classification.getClassstructureId());
			allClassifications.addAll(children);
			
			for (Classification nextClassification : children) {
				loadClassificationTree(nextClassification, allClassifications);
			}
		}		
	}
	
    /**
     * 
     * @param parent
     * @return List<Classification>
     * @throws SQLException
     */
	private List<Classification> getClassificationsByParent(String parent) 
		throws SQLException {
		
		String sql = provider.getSQL(QuerySet.ACTCI_SELECT_CLASSIFICATIONS_BY_PARENT);
		List<Classification> list = new ArrayList<Classification>();
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		try {
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, parent);
			resultSet = statement.executeQuery();

			while (resultSet.next()) {
				String classsificationId = resultSet.getString(1);
				String classstructureId = resultSet.getString(2);
				int hasChildren = resultSet.getInt(3);
				Classification classification = new Classification();
				classification.setClassName(classsificationId);
				classification.setClassstructureId(classstructureId);
				classification.setHasChildren(hasChildren > 0);
				list.add(classification);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		
		return list;
	}		
	
}
