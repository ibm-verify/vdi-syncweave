/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.tm;

import java.util.Date;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.Tombstone;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.Calendar;

/**
 * This class implements the functionality provided by the
 * {@link TombstoneManager} of auto removing old tombstones. This object is
 * started by the TombstoneManager so it can act as a Garbage Collector of
 * tombstone records.
 */
public class TombstoneAutoCleaner extends Thread {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Time of the Server to boot.
	 */
	private static final long INITIAL_DELAY = 30000;

	/**
	 * Manages {@link Tombstone} objects
	 */
	private TombstoneManager mTombstoneManager = null;

	/**
	 * The maximum number of days a tombstone could be.that are older than this
	 * period are automatically deleted.
	 */
	private int mAge = 0;

	/**
	 * Specifies the time to the next run
	 */
	private long mTimeToNextRun = 0;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructs an object of this type.
	 * 
	 * @param aTombstoneManager
	 *            is a reference to the manager that have spawned the TombStone
	 *            Cleaner.
	 * @param aAge
	 *            the maximum number of days a tombstone could be. Tombstones
	 *            that are older than this period are automatically deleted.
	 */
	public TombstoneAutoCleaner(TombstoneManager aTombstoneManager, int aAge) {
		mTombstoneManager = aTombstoneManager;
		mAge = aAge;
	}

	/**
	 * This method is called when the {@link TombstoneAutoCleaner} thread is
	 * started. This method will run until the thread is interrupted. The thread
	 * will run on cycles and on each cycle will delete threads that are older
	 * than the specified period.
	 */
	public void run() {
		try {
			if (APIEngine.isDebugEnabled()) {
				APIEngine
						.logDebug(sResHash
								.getString(
										"SEVER.API.TOMBSTONEAUTOCLEANER.STARTED.WILL.DELETE.TOMBSTONES.OLDER.THAN",
										String.valueOf(mAge)));
			}
			Thread.sleep(INITIAL_DELAY); // give some time to the Server to
			// boot
			int count = mTombstoneManager.deleteTombstones(mAge);
			if (APIEngine.isDebugEnabled()) {
				APIEngine
						.logDebug(sResHash
								.getString(
										"SEVER.API.TOMBSTONEAUTOCLEANER.INITIAL.RUN.PERFORMED.DELETED",
										String.valueOf(count)));
			}
			mTimeToNextRun = getTimeToNextRun();

			while (true) {
				try {
					Thread.sleep(mTimeToNextRun);
					count = mTombstoneManager.deleteTombstones(mAge);
					if (APIEngine.isDebugEnabled()) {
						APIEngine
								.logDebug(sResHash
										.getString(
												"SEVER.API.TOMBSTONEAUTOCLEANER.SCHEDULED.RUN.PERFORMED.DELETED",
												String.valueOf(count)));
					}
					mTimeToNextRun = getTimeToNextRun();
				} catch (InterruptedException e) {
					if (APIEngine.isDebugEnabled()) {
						APIEngine
								.logDebug(sResHash
										.getString("SEVER.API.TOMBSTONEAUTOCLEANER.THREAD.INTERUPTED"));
					}
					mTimeToNextRun = getTimeToNextRun();
				}
			}
		} catch (Exception e) {
			APIEngine.logError(sResHash.getString(
					"SEVER.API.TOMBSTONEAUTOCLEANER.ERROR", e.toString()));
		}
	}

	/**
	 * Retrieves time to nex run.
	 * @return time to next run.
	 */
	private long getTimeToNextRun() {
		Calendar calendar = Calendar.getInstance();
		long now = calendar.getTimeInMillis();

		int hour = 24 - calendar.get(Calendar.HOUR_OF_DAY);
		int min = 60 - calendar.get(Calendar.MINUTE);

		long timeToNextRun = 1000L * 60 * (hour * 60 + min);
		if (APIEngine.isDebugEnabled()) {
			APIEngine.logDebug(sResHash.getString(
					"SEVER.API.TOMBSTONEAUTOCLEANER.TIME.TO.NEXT.RUN", String
							.valueOf(timeToNextRun)));
			APIEngine.logDebug(sResHash.getString(
					"SEVER.API.TOMBSTONEAUTOCLEANER.NEXT.RUN.IS.SCHEDULED.FOR",
					new Date(now + timeToNextRun)));
		}

		return timeToNextRun;
	}

}
