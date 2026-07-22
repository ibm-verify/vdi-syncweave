/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.CIRelation;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.provider.ActciRelationProvider;
import com.ibm.di.connector.ccmdb.provider.CCMDBActualCIProvider;
import com.ibm.di.connector.ccmdb.provider.SQLQuery;
import com.ibm.di.connector.ccmdb.schema.base.CCMDBMetaData;
import com.ibm.di.connector.ccmdb.schema.base.CCMDBSchemaFactory;
import com.ibm.di.connector.ccmdb.search.SearchQueryBuilder;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;

/**
 * An implementation of AbstrcatDataHander that is designed to work with relationships.
 * 
 * @author yavor.gologanov
 *
 */
public class CIRelationDataHandler extends AbstractDataHandler {

	private Entry schema = null;
	private SQLQuery searchQuery = null;	
	private Iterator<CIRelation> relationIterator = null;
	private static ResourceHash resHash = CCMDBConnector.getResHash();
	
	/**
	 * 
	 */
	public void init(ExecutionContext context) {
		super.init(context);
		
		if (ALL.equalsIgnoreCase(context.getClassification())) {
			context.setClassification(CCMDBActualCIProvider.CLASS_CI_RELATION);
		}
	}
	
	/**
	 * 
	 */
	public Entry getSchema() throws CCMDBException {
		if (schema == null) {
			CIRelationDefinition definition = getDefinition();
			schema = ctx.getMetaData().createSchema(definition);
		}
		return schema;
	}	
	
	/**
	 * 
	 */
	public Entry getNextEntry() throws CCMDBException {
		
		if (relationIterator == null || !relationIterator.hasNext()) {
			relationIterator = null;
			return null;
		} 

		Entry entry = null;
		try {
			CIRelation relation = relationIterator.next();
			relation.setLoadRelatedItems(true);
			
			ActciRelationProvider relationProvider = ctx.getDataProvider().getActciRelationProvider();
			relationProvider.load(relation);

			entry = ctx.getMetaData().createEntry(relation);
			relation.clear();
		} catch (Exception e) {
			throw new CCMDBException(e);
		} 
		
		return entry;	
	}

	/**
	 * 
	 */
	public void setSearchCriteria(SearchCriteria criteria) throws CCMDBException {
		AbstractMetaData metaData = ctx.getMetaData();
		CIRelationDefinition definition = 
			metaData.getCIRelationDefinition(ctx.getClassification(), true);
		SearchQueryBuilder queryBuilder = new SearchQueryBuilder();
		searchQuery = queryBuilder.buildQuery(criteria, definition);
        ctx.getLog().debug(resHash.getString("CCMDB.CONN.SEARCH.QUERY", new Object[]{"CI Relation", searchQuery}));
	}	
	
	/**
	 * 
	 */
	public void selectEntries() throws CCMDBException {
		
		ActciRelationProvider relationProvider = ctx.getDataProvider().getActciRelationProvider();
		CIRelationDefinition definition = getDefinition();
		try {
			List<CIRelation> relationList = null;
			if (searchQuery == null) {
				relationList = relationProvider.select(definition);	
			} else {
				relationList = relationProvider.select(searchQuery, definition);	
			}
			
			if (relationList.size() > 0) {
				relationIterator = relationList.iterator();
			}
		} catch (Exception e) {
			throw new CCMDBException(e);
		}
	}	
	
	/**
	 * 
	 */
	public boolean addEntry(Entry entry) throws CCMDBException {
		boolean saved = false;
		
		try {
			CIRelation relation = ctx.getMetaData().createCIRelation(entry);
			if (relation == null) {
				throw new CCMDBException(resHash.getString("CCMDB.CONN.UNABLE.TO.CREATE.INSTANCE", "CI relation"));
			}
			
			checkClass(relation);
			
			ActciRelationProvider relationProvider = ctx.getDataProvider().getActciRelationProvider();
			
			/* 
			 *  When we add a relation, either the source and/or target can exist. If they do not exist
			 *  the source and/or target CI is created
			 */
			// If source/target CI does not exist, get all details of the source/target CI for creating the CI
			boolean sourceCIExists = (relationProvider.getProvider().getActciProvider()).exist(relation.getSource().getGuid());
			boolean targetCIExists = (relationProvider.getProvider().getActciProvider()).exist(relation.getTarget().getGuid());
			if(!sourceCIExists || !targetCIExists){		
				NodeList nodeList = entry.getChildNodes();
				for (int i=0; i<nodeList.getLength();i++) {
					Node nextNode = nodeList.item(i);
					
					if (nextNode.getNodeName().equals(CCMDBSchemaFactory.ATTR_REL_SOURCE) && !sourceCIExists) {
						NodeList sourceCINodeList = nextNode.getChildNodes();
						relation.setSource(getCI(sourceCINodeList));
					}
					else if (nextNode.getNodeName().equals(CCMDBSchemaFactory.ATTR_REL_TARGET) && !targetCIExists) {
						NodeList targetCINodeList = nextNode.getChildNodes();
						relation.setTarget(getCI(targetCINodeList));
					}
				}	
			}
			saved = relationProvider.save(relation);
		} catch (Exception e) {
			throw new CCMDBException(e);
		} 
		
		return saved;
	}	

    /**
     * 
     */

	private ActualCI getCI(NodeList ciNodeList) throws CCMDBException{
		CCMDBMetaData metaData = (CCMDBMetaData)ctx.getMetaData();
		return metaData.getObjectFactory().createConfigItem(ciNodeList, true);
	}	
	
	/**
	 * 
	 */
	public boolean updateEntry(Entry entry) throws CCMDBException {
		boolean updated = false;
		try {
			CIRelation relation = ctx.getMetaData().createCIRelation(entry);
			if (relation == null) {
				throw new CCMDBException(resHash.getString("CCMDB.CONN.UNABLE.TO.CREATE.INSTANCE", "CI relation"));
			}
			
			checkClass(relation);
			
			ActciRelationProvider relationProvider = ctx.getDataProvider().getActciRelationProvider();
			updated = relationProvider.update(relation);
		} catch (Exception e) {
			throw new CCMDBException(e);
		} 
		return updated;
	}	
	
	/**
	 * 
	 */
	public boolean removeEntry(Entry entry) throws CCMDBException {
		try {
			CIRelation relation = ctx.getMetaData().createCIRelation(entry);
			if (relation == null) {
				throw new CCMDBException(resHash.getString("CCMDB.CONN.UNABLE.TO.CREATE.INSTANCE", "CI relation"));
			}
			
			checkClass(relation);
			
			ActciRelationProvider relationProvider = ctx.getDataProvider().getActciRelationProvider();
			return relationProvider.delete(relation);
		} catch (Exception e) {
			throw new CCMDBException(e);
		} 
	}	
	
	/**
	 * 
	 * @return
	 * @throws CCMDBException
	 */
	private CIRelationDefinition getDefinition() throws CCMDBException {
		AbstractMetaData metaData = ctx.getMetaData();
		String className = ctx.getClassification();
		CIRelationDefinition definition = metaData.getCIRelationDefinition(className, true);
		return definition;
	}
	
	/**
	 * 
	 * @param relation
	 * @throws CCMDBException 
	 */
	private void checkClass(CIRelation relation) throws Exception {
		if (!ctx.getClassification().equals(CCMDBActualCIProvider.CLASS_CI_RELATION)) {
			String classname = ctx.getClassification();
			if (!relation.getClassName().equals(classname)) {
				throw new CCMDBException(resHash.getString("CCMDB.CONN.INCOMPATIBLE.CLASSIFICATION", 
						new Object[]{classname, relation.getClassName()}));
			}
		}
	}	
	
}
