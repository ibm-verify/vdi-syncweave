/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.provider;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.di.connector.dpa.schema.ClassDefinition;
import com.ibm.di.connector.dpa.schema.ClassInstance;
import com.ibm.di.connector.dpa.schema.InconsistentInstanceException;
import com.ibm.di.connector.dpa.schema.PropertyDefinition;
import com.ibm.di.connector.dpa.schema.PropertySetDefinition;
import com.ibm.di.connector.dpa.schema.PropertySetInstance;
import com.ibm.di.connector.dpa.schema.ReferenceDefinition;
import com.ibm.di.connector.dpa.schema.ReferenceInstance;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class LoadClassInstanceCommand {
	
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
	public LoadClassInstanceCommand(DeployedAssetsProvider provider) {
		this.provider = provider;
	}
	
	/**
	 * 
	 * @param instance
	 * @param loadReferences
	 * @throws SQLException
	 * @throws IOException
	 */
	public void load(ClassInstance instance, boolean loadReferences) 
		throws SQLException, IOException {
		
		ClassDefinition definition = instance.getDefinition();
		loadProperties(instance, definition);
		
		ReferenceDefinition parent = definition.getParent();
		if (parent != null) {
			loadParent(instance, parent, loadReferences);			
		}		
		
		if (loadReferences) {
			loadReferences(instance, definition, loadReferences);
		}
		//instance.validate();
	}	
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param instance
	 * @param parentRefDef
	 * @param loadReferences
	 * @throws SQLException
	 * @throws IOException
	 */
	private void loadParent(ClassInstance instance, 
			ReferenceDefinition parentRefDef,
			boolean loadReferences) 
		throws SQLException, IOException {
		
		String parentClass = parentRefDef.getClassName();
		ClassDefinition parentDefinition = provider.getClassdefFactory().getDefinition(parentClass);
		Object fkValue = instance.getProperty(parentRefDef.getOnProperty());
		if (fkValue == null) {
			throw new InconsistentInstanceException(resHash.getString("DPA.CONN.FKEY.INVALID", new Object[] {instance.getDescription()}));
		}
		
		ClassInstance parentInstance = new ClassInstance(parentDefinition);
		String columnName = parentRefDef.getColumnName();
		Object parentPrimaryKey = null;
			
		if (columnName.equalsIgnoreCase(parentDefinition.getPrimaryKey().getColumnName())) {				
			parentPrimaryKey = fkValue;
		} else {			
			List<Object> pkList = getReferedInstancePKs(parentRefDef, parentDefinition, fkValue);
			if (pkList.size() == 1) {
				parentPrimaryKey = pkList.get(0);
			} else {
				throw new InconsistentInstanceException(resHash.getString("DPA.CONN.MULTI.PKEYS", new Object[] {pkList, instance.getDescription()}));
			}
		}		
			
		if (parentPrimaryKey != null) {
			parentInstance.setPrimaryKeyValue(fkValue);
			load(parentInstance, loadReferences);
			if (!parentInstance.isEmpty()) {					
				ReferenceInstance parentReference = new ReferenceInstance(parentRefDef);
				parentReference.addClassInstance(parentInstance);
				instance.setParent(parentReference);
			}
		} else {
			throw new InconsistentInstanceException(resHash.getString("DPA.CONN.PARENT.NOT.FOUND", new Object[] {instance.getDescription()}));
		}

	}		
	
	/**
	 * 
	 * @param instance
	 * @param definition
	 * @throws SQLException
	 * @throws IOException
	 */
	private void loadProperties(ClassInstance instance, ClassDefinition definition) 
		throws SQLException, IOException {
		
		if (definition.getPropertyCount() > 0) {
			PropertySetDefinition propertySet = definition.getProperties();
			String sql = getSelectPropertiesQuery(propertySet);
			Object selectKeyValue = instance.getPrimaryKeyValue();
			loadProperties(sql, instance, selectKeyValue, propertySet);
		}
		
		if (definition.getAdditionalPropertyCount() > 0) {
			List<PropertySetDefinition> additionalPropertySets 
				= definition.getAdditionalProperties();
    		for (PropertySetDefinition nextPropSet : additionalPropertySets) {
				String sql = getSelectPropertiesQuery(nextPropSet);
				Object selectKeyValue = instance.getProperty(nextPropSet.getOnProperty());
				loadAdditionalProperties(sql, instance, selectKeyValue, nextPropSet);
			}
		}
	}
	
	/**
	 * 
	 * @param sql
	 * @param instance
	 * @param selectKeyValue
	 * @param propertySet
	 * @throws SQLException
	 * @throws IOException
	 */
	private void loadProperties(String sql, 
			ClassInstance instance, 
			Object selectKeyValue,
			PropertySetDefinition propertySet) throws SQLException, IOException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;	
		try {
			statement = provider.getConnection().prepareStatement(sql);
			SQLUtilities.setValue(statement, 1, selectKeyValue);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {	
				Map<String, Object> values = SQLUtilities.getRowData(resultSet, provider.isIgnoreFieldErrors());
				List<PropertyDefinition> propertyList = propertySet.getPropertyList();
				for (PropertyDefinition nextDef : propertyList) {
					Object value = values.get(nextDef.getColumnName());
					instance.setProperty(nextDef.getName(), value);
				}
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}		
	}	
	
	/**
	 * 
	 * @param instance
	 * @param definition
	 * @param loadReferences
	 * @throws SQLException
	 * @throws IOException
	 */
	private void loadReferences(ClassInstance instance, 
			ClassDefinition definition,
			boolean loadReferences) 
		throws SQLException, IOException {
		
		if (definition.getReferenceCount() == 0) {
			return;
		}
			
		List<ReferenceDefinition> references = definition.getReferences();
		for (ReferenceDefinition refDef : references) {
			String onProperty = refDef.getOnProperty();
			Object fkValue = instance.getProperty(onProperty);
			if (fkValue == null) {
				continue;
			}				
				
			String columnName = refDef.getColumnName();
			String referedClassName = refDef.getClassName();				
			ClassDefinition referedClassDefinition = provider.getClassdefFactory().getDefinition(referedClassName); 
			ReferenceInstance refInstance = new ReferenceInstance(refDef);
				
			// Reference by primary key (1:1)
			if (columnName.equalsIgnoreCase(referedClassDefinition.getPrimaryKey().getColumnName())) {
				ClassInstance refClassInstance = new ClassInstance(referedClassDefinition);
				refClassInstance.setPrimaryKeyValue(fkValue);
				load(refClassInstance, loadReferences);
				if (!refClassInstance.isEmpty()) {						
					refInstance.addClassInstance(refClassInstance);						
				}
			} else {
				// Reference by foreign key	(1:n)	
				List<Object> pkList = getReferedInstancePKs(refDef, referedClassDefinition, fkValue);
				if (pkList.size() > 0) {
					for (Object nextPK : pkList) {
						ClassInstance refClassInstance = new ClassInstance(referedClassDefinition);
						refClassInstance.setPrimaryKeyValue(nextPK);
						load(refClassInstance, loadReferences);
						if (!refClassInstance.isEmpty())  {
							refInstance.addClassInstance(refClassInstance);
						}
					}
				}
			}
				
			if (refInstance.getClassInstanceCount() > 0) {
				instance.addReference(refInstance);
			}
		}			
	}	
	
	/**
	 * 
	 * @param propertySet
	 * @return String
	 */
	private String getSelectPropertiesQuery(PropertySetDefinition propertySet) {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT \n\t");
		
		List<PropertyDefinition> properties = propertySet.getPropertyList();
		Iterator<PropertyDefinition> propertyIt = properties.iterator();
		while (propertyIt.hasNext()) {
			PropertyDefinition nextdef = propertyIt.next();
			sql.append(nextdef.getColumnName());
			if (propertyIt.hasNext()) {
				sql.append(", ");
			}
		}
		
		sql.append("\nFROM " + propertySet.getTable());
		sql.append("\nWHERE " + propertySet.getJoinColumn() + " = ?");		
		
		return sql.toString();
	}	
	
	/**
	 * 
	 * @param refDef
	 * @param referedClassDefinition
	 * @param selectKeyValue
	 * @return List<Object>
	 * @throws SQLException
	 */
	private List<Object> getReferedInstancePKs(ReferenceDefinition refDef, 
			ClassDefinition referedClassDefinition,
			Object selectKeyValue) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT " + referedClassDefinition.getPrimaryKey().getColumnName());
		sql.append("\nFROM " + referedClassDefinition.getTable());
		sql.append("\nWHERE " + refDef.getColumnName() + " = ?");
		List<Object> pkList = new ArrayList<Object>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		try {
			statement = provider.getConnection().prepareStatement(sql.toString());
			SQLUtilities.setValue(statement, 1, selectKeyValue);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {	
				Object nextKey = resultSet.getObject(1);
				pkList.add(nextKey);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}		
		
		return pkList;
	}	
	
	/**
	 * 
	 * @param sql
	 * @param instance
	 * @param selectKeyValue
	 * @param propertySetDefinition
	 * @throws SQLException
	 * @throws IOException
	 */
	private void loadAdditionalProperties(String sql, 
			ClassInstance instance, 
			Object selectKeyValue,
			PropertySetDefinition propertySetDefinition) throws SQLException, IOException {
		
		PropertySetInstance propertySetInstance = new PropertySetInstance(propertySetDefinition);
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;	
		try {
			statement = provider.getConnection().prepareStatement(sql);
			SQLUtilities.setValue(statement, 1, selectKeyValue);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {	
				Map<String, Object> values = SQLUtilities.getRowData(resultSet, provider.isIgnoreFieldErrors());
				List<PropertyDefinition> propertyList = propertySetDefinition.getPropertyList();
				for (PropertyDefinition nextDef : propertyList) {
					Object value = values.get(nextDef.getColumnName());
					propertySetInstance.addProperty(nextDef.getName(), value);
				}
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}		
		
		instance.addAdditionalProperties(propertySetInstance);
	}		
	
}
