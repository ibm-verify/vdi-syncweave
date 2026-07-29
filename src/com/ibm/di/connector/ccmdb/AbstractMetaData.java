/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb;

import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;

import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.CIRelation;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.provider.CCMDBActualCIProvider;
import com.ibm.di.connector.ccmdb.provider.DefinitionProvider;
import com.ibm.di.entry.Entry;

/**
 * This is an abstract class designed to implement data transformation between 
 * internal data model and TDI data model. 
 * There is one implementation of this class for each schema supported by the connector.
 * 
 * @author yavor.gologanov
 *
 */
public abstract class AbstractMetaData {
	
	protected ExecutionContext ctx = null;
	
	private Map<String, ActualCIDefinition> configItemDefCache = 
		new WeakHashMap<String, ActualCIDefinition>();
	
	private Map<String, CIRelationDefinition> relationDefCache = 
		new WeakHashMap<String, CIRelationDefinition>();	
	
	/**
	 * 
	 * @param context 
					the ExecutionContext instance associated with current execution.
	 */
	public void init(ExecutionContext context) throws CCMDBException {		
		this.ctx = context;
	}
			
	/**
	 * Add mapping information that is based on the selected schema to 
	 * an ActualCIDefinition instance 
	 * 
	 * @param definition 
	 * 				ActualCIDefinition instance to be updated
	 * @throws CCMDBException
	 * 				if a problem occurs.
	 */
	protected abstract void applyMapping(ActualCIDefinition definition) 
		throws CCMDBException;
	
	/**
	 * Add mapping information that is based on the selected schema to 
	 * a CIRelationDefinition instance. 
	 * 
	 * @param definition 
					CIRelationDefinition instance to be updated
	 * @throws CCMDBException
	 * 				if a problem occurs.
	 */
	protected abstract void applyMapping(CIRelationDefinition definition) 
		throws CCMDBException;	
	
	/**
	 * Creates a schema based on a given definition. 
	 * 
	 * @param definition
	 * 				ActualCIDefinition 
	 * @return Entry
	 * 				the schema Entry
	 * @throws CCMDBException
	 * 				if a problem occurs.
	 */
	public abstract Entry createSchema(ActualCIDefinition definition) 
		throws CCMDBException;	
	
	/**
	 *  Creates a schema based on a given definition. 
	 * 
	 * @param definition
	 * 				CIRelationDefinition
	 * @return Entry
	 * 				the schema Entry
	 * @throws CCMDBException
	 * 				if a problem occurs.
	 */
	public abstract Entry createSchema(CIRelationDefinition definition) 
		throws CCMDBException;	
	
	
	/**
	 * Creates an instance of DI Entry based on a given configuration item.
	 * @param configItem
	 * 				the ActualCI instance to be transformed
	 * @return Entry
	 * 				the data Entry
	 * @throws CCMDBException
	 * 				if a problem occurs.
	 */
	public abstract Entry createEntry(ActualCI configItem)
		throws CCMDBException;
	
	/**
	 * Creates an instance of DI Entry based on a given relation.
	 * 
	 * @param relation
	 * 				CIRelation instance to be transformed
	 * @return Entry
	 * 				the data Entry
	 * @throws CCMDBException
	 * 				if a problem occurs.
	 */
	public abstract Entry createEntry(CIRelation relation)
		throws CCMDBException;
	
	/**
	 * Creates an instance of ActualCI based on a given DI Entry object.
	 * 
	 * @param entry
	 * 				the data Entry
	 * @return ActualCI
	 * 				an instance of ActualCI created from the input entry
	 * @throws CCMDBException
	 * 				if a problem occurs
	 */
	public abstract ActualCI createActualCI(Entry entry) 
		throws CCMDBException;	
	
	/**
	 * Creates an instance of CIRelation based on a given DI Entry object.
	 * 
	 * @param entry
	 * 				the data Entry
	 * 			
	 * @return CIRelation
	 * 				an instance of CIRelation created from the input entry
	 * @throws CCMDBException
	 * 				if a problem occurs
	 */
	public abstract CIRelation createCIRelation(Entry entry) 
		throws CCMDBException;	
	
	/**
	 * Returns an instance of ActualCIDefinition that is common for all 
	 * configuration items.
	 * 
	 * @return ActualCIDefinition
	 * 				the common definition 
	 * @throws CCMDBException
	 * 				if a problem occurs
	 */
	public ActualCIDefinition getActualCIDefinition() 
		throws CCMDBException {
		
		String className = CCMDBActualCIProvider.CLASS_ACTUAL_CI;
		ActualCIDefinition definition = configItemDefCache.get(className);
		if (definition == null) {
			try {
				DefinitionProvider definitionProvider = ctx.getDataProvider().getDefinitionProvider();
				definition = definitionProvider.getActualCIDefinition(className);
				if (definition != null) {
					applyMapping(definition);
					configItemDefCache.put(className, definition);
				}
			} catch (Exception e) {
				throw new CCMDBException(e);
			}
		}
		return definition;
	}
	
	/**
	 * Returns an ActualCIDefinition instance for the specified configuration item class. 
	 * 
	 * @param className  
	 *				the actual CI class name
	 * @return ActualCIDefinition
	 * 				the corresponding ActualCIDefinition instance
	 * @throws CCMDBException
	 * 				if a problem occurs
	 */
	public ActualCIDefinition getActualCIDefinition(String className) 
		throws CCMDBException {
		
		return getActualCIDefinition(className, false, false);
	}
	
	/**
	 * Returns a definition object for the specified configuration item class. 
	 * 
	 * @param className 
					the actual CI class name
	 * @param loadSrcRelations 
	 *				a boolean that determines whether source relation rules should be loaded.
	 * @param loadTrgRelations 
	 *				a boolean that determines whether target relation rules should be loaded.
	 * @return ActualCIDefinition
	 * 				the corresponding ActualCIDefinition instance
	 * @throws CCMDBException
	 * 				if a problem occurs
	 */
	public ActualCIDefinition getActualCIDefinition(String className, 
			boolean loadSrcRelations,
			boolean loadTrgRelations) 
		throws CCMDBException {

		DefinitionProvider definitionProvider = ctx.getDataProvider().getDefinitionProvider();
		ActualCIDefinition definition = configItemDefCache.get(className);
		if (definition == null) {
			try {
				definition = definitionProvider.getActualCIDefinition(className);				
				applyMapping(definition);
				configItemDefCache.put(className, definition);	
			} catch (Exception e) {
				throw new CCMDBException(e);
			}
		}
				
		try {
			if (loadSrcRelations) {
				definitionProvider.loadSourceRelationRules(definition);
			}
			if (loadTrgRelations) {
				definitionProvider.loadTargetRelationRules(definition);
			}
		} catch (SQLException e) {
			throw new CCMDBException(e);
		}
	
		return definition;
	}
	
	/**
	 * Returns a definition object for the specified relation class. 
	 * 
	 * @param className 
	 * 				the relation class name
	 * @return CIRelationDefinition 
	 * 				the corresponding CIRelationDefinition instance
	 * @throws CCMDBException
	 * 				if a problem occurs
	 */
	public CIRelationDefinition getCIRelationDefinition(String className) 
		throws CCMDBException {
		return getCIRelationDefinition(className, false);
	}
	
	/**
	 * Returns a definition object for the specified relation class. 
	 * 
	 * @param className 
	 *				the relation class name
	 * @param loadRelationRules 
	 * 				a boolean that determines whether relation rules should be loaded.
	 * @return CIRelationDefinition
	 * 				the corresponding CIRelationDefinition instance
	 * @throws CCMDBException
	 * 				if a problem occurs
	 */
	public CIRelationDefinition getCIRelationDefinition(String className,
			boolean loadRelationRules) 
		throws CCMDBException {
		
		DefinitionProvider definitionProvider = ctx.getDataProvider().getDefinitionProvider();
		CIRelationDefinition definition = relationDefCache.get(className);
		if (definition == null) {
			try {				
				definition = definitionProvider.getCIRelationDefinition(className);
				applyMapping(definition);		
				relationDefCache.put(className, definition);
			} catch (Exception e) {
				throw new CCMDBException(e);
			}
		}
		
		if (loadRelationRules) {
			try {
				definitionProvider.loadRelationRules(definition);
			} catch (SQLException e) {
				throw new CCMDBException(e);
			}
		}
		
		return definition;
	}
	
	public String getSelectedClassification()
	{
		if(ctx != null)
		{
			return ctx.getClassification();
		}
		return null;
	}

	
}
