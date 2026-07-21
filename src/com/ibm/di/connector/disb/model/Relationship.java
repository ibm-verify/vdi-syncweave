/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.disb.model;

import com.ibm.tivoli.namereconciliation.guid.Guid;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1.1
 */
public class Relationship {

	private String source;
	private String target;
	private String relationShipType;

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target
	 *            the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * @return the relationShipType
	 */
	public String getRelationShipType() {
		return relationShipType;
	}

	/**
	 * @param relationShipType
	 *            the relationShipType to set
	 */
	public void setRelationShipType(String relationShipType) {
		this.relationShipType = relationShipType;
	}

	public String toString() {
		StringBuilder ciStringBuilder = new StringBuilder();
		ciStringBuilder.append("{RelationshipType = " + relationShipType + ", ");
		ciStringBuilder.append("source = " + source + ", ");
		ciStringBuilder.append("target =" + target + "}");
		return ciStringBuilder.toString();
	}

	public Guid[] getSourceTargetElementIndex(String[] configurationIds, ConfigurationItem[] configArray) {
		Guid guid[] = new Guid[2];
		if (configurationIds != null) {
			for (int i = 0; i < configurationIds.length; i++) {
				if (configurationIds[i].equals(source)) {
					guid[0] = configArray[i].getGuid();
				} else if (configurationIds[i].equals(target)) {
					guid[1] = configArray[i].getGuid();
				}
			}
		}
		return guid;
	}
}
