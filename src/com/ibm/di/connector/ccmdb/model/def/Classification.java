/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model.def;

/**
 * The definition for an item/relationship class type according CCMDB.  
 * 
 * @author yavor.gologanov
 *
 */
public class Classification {

	private String classstructureId = null;
	private String className = null;
	private boolean hasChildren = false;
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isHasChildren() {
		return hasChildren;
	}

	/**
	 * 
	 * @param hasChildren
	 */
	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	/**
	 * 
	 * @return String
	 */
	public String getClassstructureId() {
		return classstructureId;
	}
	
	/**
	 * 
	 * @param classstructureId
	 */
	public void setClassstructureId(String classstructureId) {
		this.classstructureId = classstructureId;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * 
	 * @param className
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[ " + className + ":" + classstructureId + "]");
		return sb.toString();
	}
	
}
