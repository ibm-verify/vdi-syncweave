/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.provider;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class SQLQuery {

	private String sql = null;
	private List<Object> parameters = new ArrayList<Object>();
	
	/**
	 * 
	 * @param sql
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * 
	 * @param value
	 */
	public void addParameterValue(Object value) {
		parameters.add(value);
	}
	
	/**
	 * 
	 * @param value
	 * @param position
	 */
	public void setParameterValue(Object value, int position) {
		parameters.add(position, value);
	}
	
	/**
	 * 
	 * @return int
	 */
	public int getParameterCount() {
		if (parameters == null) {
			return 0;
		}
		
		return parameters.size();
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getSQL() {
		return sql;
	}
	
	/**
	 * 
	 * @param position
	 * @return Object
	 */
	public Object getParameterValue(int position) {
		return parameters.get(position);
	}
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isPrepared() {
		return (parameters.size() > 0);
	}
	
	/**
	 * 
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("\n[" + this.getClass().getCanonicalName());
		str.append("\nsql: ");
		str.append(sql);
		str.append("\nparameters: " + parameters);
		str.append("\n]");
		return str.toString();
	}
	
}
