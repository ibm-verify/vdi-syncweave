/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model;

import com.ibm.di.connector.ccmdb.provider.CCMDBActualCISchema;

/**
 *  This class is a representation of a relationship in CCMDB
 * 
 * @author yavor.gologanov
 *
 */
public class CIRelation extends ModelObject {

	private boolean loadRelatedItems = false;	
	private ActualCI source = null;
	private ActualCI target = null;

	/**
	 * 
	 */
	public void clear() {
		this.source = null;
		this.target = null;
	}	
	
	/**
	 * 
	 * @return String
	 */
	public String getClassName() {
		return getRelationnum();
	}

	/**
	 * 
	 * @param className
	 */
	public void setClassName(String className) {
		setProperty(CCMDBActualCISchema.ACTCIRELATION_RELATIONNUM, className);
	}	
	
	/**
	 * 
	 * @return ActualCI
	 */
	public ActualCI getTarget() {
		return target;
	}

	/**
	 * 
	 * @param target
	 */
	public void setTarget(ActualCI target) {
		this.target = target;
		setProperty(CCMDBActualCISchema.ACTCIRELATION_TARGETCIGUID, target.getGuid());
		setProperty(CCMDBActualCISchema.ACTCIRELATION_TARGETCI, target.getActcinum());
	}
	
	/**
	 * 
	 * @return ActualCI
	 */
	public ActualCI getSource() {
		return source;
	}

	/**
	 * 
	 * @param source
	 */
	public void setSource(ActualCI source) {
		this.source = source;
		setProperty(CCMDBActualCISchema.ACTCIRELATION_SOURCECIGUID, source.getGuid());
		setProperty(CCMDBActualCISchema.ACTCIRELATION_SOURCECI, source.getActcinum());
	}	
	
	/**
	 * 
	 * @return String
	 */
	public String getRelationnum() {
		return getStringProperty(CCMDBActualCISchema.ACTCIRELATION_RELATIONNUM);
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getSourceGuid() {
		return getStringProperty(CCMDBActualCISchema.ACTCIRELATION_SOURCECIGUID);
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getTargetGuid() {
		return getStringProperty(CCMDBActualCISchema.ACTCIRELATION_TARGETCIGUID);
	}
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isLoadRelatedItems() {
		return loadRelatedItems;
	}

	/**
	 * 
	 * @param loadRelatedItems
	 */
	public void setLoadRelatedItems(boolean loadRelatedItems) {
		this.loadRelatedItems = loadRelatedItems;
	}	
	
	/**
	 * 
	 */
	public String toString() {
		StringBuffer str = new StringBuffer("\n[" + this.getClass().getCanonicalName());
		str.append(super.toString());
		if (source != null) {
			str.append("\nsource: " + source.getClassName()).append(" GUID:" + source.getGuid());
		}
		if (target != null) {
			str.append("\ntarget: " + target.getClassName()).append(" GUID:" + target.getGuid());
		}
		str.append("]");
		return str.toString();
	}
	
	/**
	 * 
	 * @param otherRelation
	 * @return boolean
	 */
	public boolean isIdentical(CIRelation otherRelation) {
		if (otherRelation == null) {
			return false;
		}
		
		String name = getRelationnum();
		String otherName = otherRelation.getRelationnum();	
		if (!name.equalsIgnoreCase(otherName)) {
			return false;
		}
		
		String srcGuid = getSourceGuid();
		String otherSrcGuid = otherRelation.getSourceGuid();		
		if ((srcGuid == null) 
				|| (otherSrcGuid == null)
				|| (!srcGuid.equalsIgnoreCase(otherSrcGuid))) {
			return false;
		}
		
		String trgGuid = getTargetGuid();
		String otherTrgGuid = otherRelation.getTargetGuid();	
		if ((trgGuid == null) 
				|| (otherTrgGuid == null)
				|| (!trgGuid.equalsIgnoreCase(otherTrgGuid))) {
			return false;
		}
			
		return true;
	}
	
}
