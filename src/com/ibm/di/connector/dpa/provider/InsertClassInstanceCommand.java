/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.provider;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.di.connector.dpa.DPAException;
import com.ibm.di.connector.dpa.schema.ClassDefinition;
import com.ibm.di.connector.dpa.schema.ClassInstance;
import com.ibm.di.connector.dpa.schema.PropertyDefinition;
import com.ibm.di.connector.dpa.schema.ReferenceDefinition;
import com.ibm.di.connector.dpa.schema.ReferenceInstance;
import com.ibm.di.connector.dpa.schema.UIDDefinition;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class InsertClassInstanceCommand {

	
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "dpaconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);	
	
	
	private DeployedAssetsProvider provider = null;
	
	/**
	 * 
	 * @param provider
	 */
	public InsertClassInstanceCommand(DeployedAssetsProvider provider) {
		this.provider = provider;
	}
	
	/**
	 * 
	 * @param instance
	 * @return boolean
	 * @throws SQLException
	 * @throws DPAException
	 */
	public boolean insert(ClassInstance instance) 
		throws SQLException, DPAException {

		boolean inserted = false;
		
		//fix for the defect 14929
		//provider.getConnection().setAutoCommit(false);
		
		try {
			ClassInstance parent = instance.getParentInstance(false);
			if (parent != null) {
				saveClassInstance(parent);
			}
			
			saveClassInstance(instance);
			
			provider.getConnection().commit();
			inserted = true;
		} catch (Exception e) {
 			provider.getConnection().rollback();
 			provider.getLog().logError(e);
 			throw new DPAException(e);
		} 
		//fix for the defect 14929
		/*finally {
			provider.getConnection().setAutoCommit(true);
		}*/
		
		return inserted;
	}		
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param instance
	 * @throws IOException
	 * @throws SQLException
	 * @throws DPAException
	 */
	private void saveClassInstance(ClassInstance instance) 
		throws IOException, SQLException, DPAException {
				
		if (provider.exists(instance)) {
			throw new DPAException(resHash.getString("DPA.CONN.DPA.EXISTS", new Object[] {instance.getDescription()}));			
		}
		
		ClassDefinition classDefinition = instance.getDefinition();
		UIDDefinition uidDefinition = classDefinition.getUidDefinition();
		if (uidDefinition != null) {
			// generate primary key
			generateKey(instance, uidDefinition);
			
			// Update foreign keys
			updateReferences(instance);
		}
		
		// insert class instance
		if (classDefinition.getProperties() != null) {
			Map<PropertyDefinition, Object> columnValueMap = new HashMap<PropertyDefinition, Object>();
			List<PropertyDefinition> propDefList = classDefinition.getPropertyList();
			for (PropertyDefinition nextPropDef : propDefList) {
				Object value = instance.getProperty(nextPropDef.getName());
				if (value != null) {
					columnValueMap.put(nextPropDef, value);
				}
			}
			
			insert(classDefinition.getTable(), columnValueMap);
		}
		
		// insert referred instance		
		if (instance.getReferenceCount() > 0) {
			Set<String> referenceNames = instance.getReferenceNames();
			for (String nextRefName: referenceNames) {
				ReferenceInstance refInstance = instance.getReference(nextRefName);
				if (refInstance.isComposition()) {
					continue;
				}
				List<ClassInstance> refInstanceList = refInstance.getClassInstances();
				if (refInstanceList != null) {
					for (ClassInstance nextInstance : refInstanceList ) {
						saveClassInstance(nextInstance); 
					}			
				}
			}
		}
		provider.getLog().debug(resHash.getString("DPA.CONN.DEBUG.SAVE", new Object[] {instance.toString()}));		
	}

	/**
	 * 
	 * @param tableName
	 * @param columnValueMap
	 * @return boolean
	 * @throws IOException
	 * @throws SQLException
	 */
	private boolean insert(String tableName, Map<PropertyDefinition, Object> columnValueMap) 
		throws IOException, SQLException {
	
		boolean inserted = false;
		SQLQuery query = new SQLQuery();
		
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO ").append(tableName).append(" (");
		
		StringBuffer values = new StringBuffer();
		
		Iterator<Map.Entry<PropertyDefinition, Object>> propDefIt = columnValueMap.entrySet().iterator();
		while (propDefIt.hasNext()) {
			Map.Entry<PropertyDefinition, Object> nextPropDef = propDefIt.next();
			if (nextPropDef.getValue() == null) {
				continue;
			}
			
			sql.append(nextPropDef.getKey().getColumnName());			
			if (SQLUtilities.TYPE_VARGRAPHIC.equals(nextPropDef.getKey().getNativeType())) {
				//fix for defect 14931 = (String) obj to String.valueOf(obj)
				values.append(" " + SQLUtilities.formatVargraphic(String.valueOf(nextPropDef.getValue())));
			} else {
				values.append(" ?");
				query.addParameterValue(nextPropDef.getValue());
			}
			if (propDefIt.hasNext()) {
				sql.append(", ");
				values.append(", ");
			}
		}

		sql.append(" )\n VALUES (");
		sql.append(values.toString()).append(")");
		
		query.setSql(sql.toString());
		inserted = (provider.executeQuery(query) > 0);
		
		return inserted;
	}
	
	/**
	 * 
	 * @param instance
	 * @param uidDefinition
	 * @throws SQLException
	 */
	private void generateKey(ClassInstance instance, UIDDefinition uidDefinition) 
		throws SQLException {
		String type = uidDefinition.getType();
		if (type.equals(UIDDefinition.TYPE_SQL)) {
			String sqlQueryName = uidDefinition.getValue();
			String sqlQuery = provider.getSQL(sqlQueryName);
			Object pkValue = provider.getObject(sqlQuery, 1);
			instance.setPrimaryKeyValue(pkValue);
			String pkprop = instance.getDefinition().getPrimaryKey().getName();
			instance.setProperty(pkprop, pkValue);
		} else 	if (type.equals(UIDDefinition.TYPE_INHERIT)) {
			String parentPropertyName = uidDefinition.getValue();
			ClassInstance parent = instance.getParentInstance(true);
			Object pkValue = parent.getProperty(parentPropertyName);
			instance.setPrimaryKeyValue(pkValue);
			String pkprop = instance.getDefinition().getPrimaryKey().getName();
			instance.setProperty(pkprop, pkValue);
		}
	}	
	
	/**
	 * 
	 * @param instance
	 */
	private void updateReferences(ClassInstance instance) {
		
		if (instance.getReferenceCount() == 0) {
			return;
		}
		
		Set<String> refNames = instance.getReferenceNames();
		for (String refName : refNames) {
			ReferenceInstance refInst = instance.getReference(refName);
			if (refInst.getClassInstanceCount() == 0) {
				continue;
			}
			
			ReferenceDefinition refDef = refInst.getDefinition();
			Object joinValue = instance.getProperty(refDef.getOnProperty());
			List<ClassInstance> classInstances = refInst.getClassInstances();
			for (ClassInstance nextInstance : classInstances) {
				String propertyName = getPropertyName(refDef.getColumnName(), 
						nextInstance.getDefinition()); 
				nextInstance.setProperty(propertyName, joinValue);
			}			
		}
	}
	
	/**
	 * 
	 * @param columnName
	 * @param definition
	 * @return String
	 */
	private String getPropertyName(String columnName, ClassDefinition definition) {
		List<PropertyDefinition> properties = definition.getPropertyList();
		if (properties != null) {
			for (PropertyDefinition propDef : properties) {
				if (propDef.getColumnName().equals(columnName)) {
					return propDef.getName();
				}
			}
		}
		
		return null;
	}
	
}
