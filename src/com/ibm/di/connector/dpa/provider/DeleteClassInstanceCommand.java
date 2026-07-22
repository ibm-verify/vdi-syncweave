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
import java.util.List;
import java.util.Set;

import com.ibm.di.connector.dpa.DPAException;
import com.ibm.di.connector.dpa.schema.ClassDefinition;
import com.ibm.di.connector.dpa.schema.ClassInstance;
import com.ibm.di.connector.dpa.schema.ReferenceInstance;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class DeleteClassInstanceCommand {
	
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
	public DeleteClassInstanceCommand(DeployedAssetsProvider provider) {
		this.provider = provider;
	}
	
	/**
	 * 
	 * @param instance
	 * @return boolean
	 * @throws DPAException
	 * @throws SQLException
	 * @throws IOException
	 */
	public boolean delete(ClassInstance instance) 
		throws DPAException, SQLException, IOException {

		boolean deleted = false;
		Object pkValue = instance.getPrimaryKeyValue();
		if (pkValue == null) {
			//String msg = "Primary key not found while deleting an deployed asset!";
			throw new DPAException(resHash.getString("DPA.CONN.PKEY.NOT.FOUND"));
		}
		
		ClassInstance instanceToDelete = new ClassInstance(instance.getDefinition());
		instanceToDelete.setPrimaryKeyValue(pkValue);
		provider.loadInstance(instanceToDelete, true);
		
		//fix for the defect 14929
		//provider.getConnection().setAutoCommit(false);		
		try {			
			deleteClassInstance(instanceToDelete);			
			ClassInstance parent = instanceToDelete.getParentInstance(false);
			if (parent != null) {
				deleteClassInstance(parent);
			}			
			
			provider.getConnection().commit();
			deleted = true;
		} catch (Exception e) {
			provider.getConnection().rollback();
			provider.getLog().logError(e);
			throw new DPAException(e);
		} 
		//fix for the defect 14929
		/*finally {
			provider.getConnection().setAutoCommit(true);
		}*/
		
		return deleted;
	}		
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param instance
	 * @throws SQLException
	 */
	private void deleteClassInstance(ClassInstance instance) 
		throws SQLException {
		
		// delete referred classes		
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
						deleteClassInstance(nextInstance); 
					}			
				}
			}
		}

		deleteRow(instance);
	}

	/**
	 * 
	 * @param instance
	 * @return int
	 * @throws SQLException
	 */
	private int deleteRow(ClassInstance instance) 
		throws SQLException {
	
		int deleted = 0;
		ClassDefinition definition = instance.getDefinition();

		StringBuffer sql = new StringBuffer();
		sql.append("DELETE FROM ").append(definition.getTable());
		sql.append("\nWHERE " + definition.getPrimaryKey().getColumnName());
		sql.append(" = ?");
		
        provider.getLog().debug(resHash.getString("DPA.CONN.DEBUG.DELETE", new Object[] {sql.toString()}));		
		Object pkValue = instance.getProperty(definition.getPrimaryKey().getName());
	    PreparedStatement statement = null;
		try {
			statement = provider.getConnection().prepareStatement(sql.toString());
			SQLUtilities.setValue(statement, 1, pkValue);
			deleted = statement.executeUpdate();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}	
		
		return deleted;
	}
	
}
