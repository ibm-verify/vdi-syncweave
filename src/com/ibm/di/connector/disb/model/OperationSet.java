/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.disb.model;

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
public class OperationSet {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String opId;
	private Refresh refresh;
	private Create create;
	private Delete delete;
	private Modify modify;
	private Reference reference;

	/**
	 * @return the opId
	 */
	public String getOpId() {
		return opId;
	}

	/**
	 * @param opId
	 *            the opId to set
	 */
	public void setOpId(String opId) {
		this.opId = opId;
	}

	/**
	 * @return the refresh
	 */
	public Refresh getRefresh() {
		return refresh;
	}

	/**
	 * @param refresh
	 *            the refresh to set
	 */
	public void setRefresh(Refresh refresh) {
		this.refresh = refresh;
	}

	/**
	 * @return the create
	 */
	public Create getCreate() {
		return create;
	}

	/**
	 * @param create
	 *            the create to set
	 */
	public void setCreate(Create create) {
		this.create = create;
	}

	/**
	 * @return the delete
	 */
	public Delete getDelete() {
		return delete;
	}

	/**
	 * @param delete
	 *            the delete to set
	 */
	public void setDelete(Delete delete) {
		this.delete = delete;
	}

	/**
	 * @return the modify
	 */
	public Modify getModify() {
		return modify;
	}

	/**
	 * @param modify
	 *            the modify to set
	 */
	public void setModify(Modify modify) {
		this.modify = modify;
	}

	/**
	 * @return the reference
	 */
	public Reference getReference() {
		return reference;
	}

	/**
	 * @param reference
	 *            the reference to set
	 */
	public void setReference(Reference reference) {
		this.reference = reference;
	}

}
