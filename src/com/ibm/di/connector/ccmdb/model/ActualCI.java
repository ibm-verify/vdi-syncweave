/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.ibm.di.connector.ccmdb.model.def.Classification;
import com.ibm.di.connector.ccmdb.provider.CCMDBActualCISchema;

/**
 * This class is a representation of an actual configuration item in CCMDB
 * 
 * @author yavor.gologanov
 *
 */
public class ActualCI extends ModelObject {

	private boolean loadAttributes = true;
	private boolean loadSrcRelation = false;
	private boolean loadTrgRelation = false;
	private boolean loadOMPRelation = false;
	private boolean loadDeletedActualCIRelation = false;	
	
	private String className = null;
	private Map<String, ClassAttribute> attributes = null;
	private RelationSet sourceRelations = null;
	private RelationSet targetRelations = null;
	private OMPRelation ompRelation = null;
	private ModelObject deletedActualCI = null;
	
	/**
	 * Clears all references
	 */
	public void clear() {
		if (attributes != null) {
			attributes = null;
		}
		
		if (sourceRelations != null) {
			sourceRelations = null;
		}
		
		if (targetRelations != null) {
			targetRelations = null;
		}
		
		ompRelation = null;
		deletedActualCI = null;
	}		
	
	/**
	 * Sets the primary key value and generates values for all required 
	 * properties and attributes that are missing.
	 * 
	 * @param actciid
	 * 			 the primary key value
	 */
	public void adjust(Integer actciid) {
		setProperty(CCMDBActualCISchema.ACTCI_ACTCIID, actciid);
		
		Object lng = getProperty(CCMDBActualCISchema.ACTCI_LANGCODE);
		if (lng == null) {
			setProperty(CCMDBActualCISchema.ACTCI_LANGCODE, "EN");
		}
		
		String actciName = getActciname();
		if (actciName == null) {
			String name = getAttributeStringValue(CCMDBActualCISchema.MODELOBJECT_DISPLAYNAME);
			if (name == null) {
				name = "unavailable";
			}
			if (name.length() > 192) {
				name = name.substring(0, 192);
			}			
			setProperty(CCMDBActualCISchema.ACTCI_ACTCINAME, name.toUpperCase());
		}
		
		String changeby = (String) getProperty(CCMDBActualCISchema.ACTCI_CHANGEBY);
		if ((changeby == null) && (attributes != null)) {
			ClassAttribute lmb = attributes.get(CCMDBActualCISchema.MODELOBJECT_LASTMODIFIEDBY);
			if (lmb != null) {
				changeby = (String) lmb.getValue();
			} else {
				changeby = "UNKNOWN";
			}
			setProperty(CCMDBActualCISchema.ACTCI_CHANGEBY, changeby);
		}
		
		Object changedate = getProperty(CCMDBActualCISchema.ACTCI_CHANGEDATE);
		if (changedate == null) {
			/*ClassAttribute lmt = attributes.get(CCMDBActualCISchema.MODELOBJECT_LASTMODIFIEDTIME);
			if (lmt != null) {
				changedate = lmt.getValue();
				long ltm = tm.getTime();
			      ltm -= ltm%1000;
			    changedate = new Timestamp(ltm);
			    setProperty(CCMDBActualCISchema.ACTCI_CHANGEDATE, changedate);
			} 	*/
			setProperty(CCMDBActualCISchema.ACTCI_CHANGEDATE, new Timestamp(System.currentTimeMillis()));			
		}
				
        if (getActcinum() == null) {
        	String name = getAttributeStringValue(CCMDBActualCISchema.MODELOBJECT_DISPLAYNAME);
			if (name == null) {
				name = "unavailable";
			}
			int length = 1 + actciid.toString().trim().length();
		    if (name.length() + length > 150)
		    {
		    	name = name.substring(0, 150 - length);
		    }
		    name = name + "~" + actciid;
        	setProperty(CCMDBActualCISchema.ACTCI_ACTCINUM, name.toUpperCase()); 
        }		

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
	 * @param classification
	 */
	public void setClassification(Classification classification) {
		this.className = classification.getClassName();
		setProperty(CCMDBActualCISchema.ACTCI_CLASSSTRUCTUREID, classification.getClassstructureId());
	}	
	
	/**
	 * 
	 * @param attribute
	 */
	public void addAttribute(ClassAttribute attribute) {
		if (attributes == null) {
			attributes = new TreeMap<String, ClassAttribute>();
		}
		this.attributes.put(attribute.getName(), attribute);
	}	
	
	/**
	 * 
	 * @return int
	 */
	public int getAttributeCount() {
		if (attributes != null) {
			return attributes.size();
		}
		
		return 0;
	}		
	
	/**
	 * 
	 * @return Set<String>
	 */
	public Set<String> getAttributeNames() {
		if (attributes != null) {
			return attributes.keySet();
		}
		
		return null;
	}	
	
	/**
	 * 
	 * @param attributeName
	 * @return ClassAttribute
	 */
	public ClassAttribute getAttribute(String attributeName) {
		if (attributes != null) {
			return attributes.get(attributeName);
		}
		
		return null;				
	}
	
	/**
	 * 
	 * @param attributeName
	 * @return Object
	 */
	public Object getAttributeValue(String attributeName) {
		if (attributes != null) {
			ClassAttribute attr = attributes.get(attributeName);
			if (attr != null) {
				return attr.getValue();
			}
		}
		
		return null;				
	}

	/**
	 * 
	 * @param attributeName
	 * @return String
	 */
	public String getAttributeStringValue(String attributeName) {
		if (attributes != null) {
			ClassAttribute attr = attributes.get(attributeName);
			if (attr != null) {
				return (String) attr.getValue();
			}
		}
		
		return null;				
	}
	
	/**
	 * 
	 * @return RelationSet
	 */
	public RelationSet getSourceRelations() {
		return sourceRelations;
	}

	/**
	 * 
	 * @param sourceRelations
	 */
	public void setSourceRelations(RelationSet sourceRelations) {
		this.sourceRelations = sourceRelations;
	}

	/**
	 * 
	 * @return RelationSet
	 */
	public RelationSet getTargetRelations() {
		return targetRelations;
	}

	/**
	 * 
	 * @param targetRelations
	 */
	public void setTargetRelations(RelationSet targetRelations) {
		this.targetRelations = targetRelations;
	}
	
	/**
	 * 
	 * @return Set<String>
	 */
	public Set<String> getRelationNames() {
		Set<String> relationNames = new HashSet<String>();
		if ((sourceRelations != null) && (!sourceRelations.isEmpty())) {
			relationNames.addAll(sourceRelations.getRelationNames());
		}
		if ((targetRelations != null) && (!targetRelations.isEmpty())) {
			relationNames.addAll(targetRelations.getRelationNames());
		}
		
		if (relationNames.size() > 0) {
			return relationNames;
		}
		return null;
	}	
	
	/**
	 * 
	 * @return OMPRelation
	 */
	public OMPRelation getOmpRelation() {
		return ompRelation;
	}

	/**
	 * 
	 * @param ompRelation
	 */
	public void setOmpRelation(OMPRelation ompRelation) {
		this.ompRelation = ompRelation;
	}	
	
	/**
	 * 
	 * @return ModelObject
	 */
	public ModelObject getDeletedActualCI() {
		return deletedActualCI;
	}

	/**
	 * 
	 * @param deletedActualCI
	 */
	public void setDeletedActualCI(ModelObject deletedActualCI) {
		this.deletedActualCI = deletedActualCI;
	}	
	
	/**
	 * 
	 * @param relationName
	 * @return List<CIRelation>
	 */
	public List<CIRelation> getSourceRelations(String relationName) {
		if (sourceRelations != null) {
			return sourceRelations.getRelations(relationName);
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param relationName
	 * @return List<CIRelation>
	 */
	public List<CIRelation> getTargetRelations(String relationName) {
		if (targetRelations != null) {
			return targetRelations.getRelations(relationName);
		}
		
		return null;
	}	
	
	/**
	 * 
	 * @return String
	 */
	public String getGuid() {
		return getStringProperty(CCMDBActualCISchema.ACTCI_GUID);
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getActcinum() {
		return getStringProperty(CCMDBActualCISchema.ACTCI_ACTCINUM);
	}	
		
	/**
	 * 
	 * @return String
	 */
	public String getActciname() {
		return getStringProperty(CCMDBActualCISchema.ACTCI_ACTCINAME);
	}	
	
	/**
	 * 
	 * @return String
	 */
	public String getClassstructureId() {
		return getStringProperty(CCMDBActualCISchema.ACTCI_CLASSSTRUCTUREID);
	}	
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isLoadAttributes() {
		return loadAttributes;
	}

	/**
	 * 
	 * @param loadAttributes
	 */
	public void setLoadAttributes(boolean loadAttributes) {
		this.loadAttributes = loadAttributes;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isLoadSrcRelation() {
		return loadSrcRelation;
	}

	/**
	 * 
	 * @param loadSrcRelation
	 */
	public void setLoadSrcRelation(boolean loadSrcRelation) {
		this.loadSrcRelation = loadSrcRelation;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isLoadTrgRelation() {
		return loadTrgRelation;
	}

	/**
	 * 
	 * @param loadTrgRelation
	 */
	public void setLoadTrgRelation(boolean loadTrgRelation) {
		this.loadTrgRelation = loadTrgRelation;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isLoadOMPRelation() {
		return loadOMPRelation;
	}

	/**
	 * 
	 * @param loadOMPRelation
	 */
	public void setLoadOMPRelation(boolean loadOMPRelation) {
		this.loadOMPRelation = loadOMPRelation;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isLoadDeletedActualCIRelation() {
		return loadDeletedActualCIRelation;
	}

	/**
	 * 
	 * @param loadDeletedActualCIRelation
	 */
	public void setLoadDeletedActualCIRelation(boolean loadDeletedActualCIRelation) {
		this.loadDeletedActualCIRelation = loadDeletedActualCIRelation;
	}	
	
	/**
	 * 
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append(super.toString());
		str.append("\nAttributes:");		
		if (attributes != null) {
			Collection<ClassAttribute> attrs = attributes.values();
			for (ClassAttribute nextAttr : attrs) {
				str.append(nextAttr.toString());
			}
		}
		
		str.append("\n");
		if (sourceRelations != null) {
			str.append("\nsourceRelations: " + sourceRelations.getRelationCount());
		}
		if (targetRelations != null) {
			str.append("\ntargetRelations: " + targetRelations.getRelationCount());
		}		
		str.append("]");
		return str.toString();
	}	
	
	/**
	 * 
	 */
	public String toUniqueString() {
		StringBuffer str = new StringBuffer();
		str.append(this.getClass().getCanonicalName());
		str.append("|").append(this.className);	
		str.append("|").append(getProperty(CCMDBActualCISchema.ACTCI_CLASSSTRUCTUREID));	
		str.append("|").append(getProperty(CCMDBActualCISchema.ACTCI_ACTCIID));	
		str.append("|").append(getProperty(CCMDBActualCISchema.ACTCI_ACTCINUM));	
		return str.toString();
	}	
	
}
