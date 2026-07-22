/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.aleidoc;

import java.util.List;

/**
 * Object that is created for tempory storage of a single transaction's TID and
 * IDoc list or function. The state of the transaction is tracked and checked
 * when onConfirmTID() is invoked.
 */
public class TIDManager {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String tid = null;

	private List idocEntries = null;

	private int tidStatus;

	public static final String TID_STAT_STR_INITIAL = "Initial";

	public static final String TID_STAT_STR_PROCESSING = "Processing";

	public static final String TID_STAT_STR_COMMIT = "Commit";

	public static final String TID_STAT_STR_ROLLBACK = "Rollback";

	public static final String TID_STAT_STR_CONFIRM = "Confirm";

	public static final String TID_STAT_STR_UNKNOWN = "Unknown";

	public static final int TID_STAT_INITIAL = 0;

	public static final int TID_STAT_PROCESSING = 1;

	public static final int TID_STAT_COMMIT = 2;

	public static final int TID_STAT_ROLLBACK = 3;

	public static final int TID_STAT_CONFIRM = 4;

	/**
	 * Default constructor
	 * 
	 * @param tid
	 * @param idocEntries
	 */
	public TIDManager(String tid, List idocEntries) {
		this.tid = tid;
		this.idocEntries = idocEntries;
		tidStatus = TID_STAT_INITIAL;
	}

	/**
	 * Disabled
	 */
	private TIDManager() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return Returns the tidStatus.
	 */
	public int getTidStatus() {
		return tidStatus;
	}

	/**
	 * @return Returns the tidStatus as a string.
	 */
	public String getTidStatusAsString() {
		switch (tidStatus) {
		case TIDManager.TID_STAT_INITIAL:
			return TIDManager.TID_STAT_STR_INITIAL;
		case TIDManager.TID_STAT_PROCESSING:
			return TIDManager.TID_STAT_STR_PROCESSING;
		case TIDManager.TID_STAT_COMMIT:
			return TIDManager.TID_STAT_STR_COMMIT;
		case TIDManager.TID_STAT_ROLLBACK:
			return TIDManager.TID_STAT_STR_ROLLBACK;
		case TIDManager.TID_STAT_CONFIRM:
			return TIDManager.TID_STAT_STR_CONFIRM;
		default:
			return TIDManager.TID_STAT_STR_UNKNOWN;
		}
	}

	/**
	 * @param tidStatus
	 *            The tidStatus to set.
	 */
	public void setTidStatus(int tidStatus) {
		this.tidStatus = tidStatus;
	}

	/**
	 * @return Returns the tid.
	 */
	public String getTid() {
		return tid;
	}

	/**
	 * @return Returns the TDI IDoc or RFM Entry list.
	 */
	public List getIdocEntries() {
		return idocEntries;
	}

	/**
	 * @param idocEntries
	 *            The TDI IDoc or RFM Entry list to set.
	 */
	public void setIdocEntries(List idocEntries) {
		this.idocEntries = idocEntries;
	}
}
