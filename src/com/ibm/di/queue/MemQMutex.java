/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.queue;

public class MemQMutex {
	private Thread curOwner = null;

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Acquires lock
	 * 
	 * @throws InterruptedException
	 */
	public synchronized void acquire() throws InterruptedException {

		while (curOwner != Thread.currentThread() && curOwner != null)
			// Re-entrant lock
			wait();
		curOwner = Thread.currentThread();
	}

	/**
	 * Checks current availability of the lock
	 * 
	 * @return true if a lock is available, false otherwise
	 */
	public synchronized boolean isLockAvalailable() {
		if ((curOwner == null) || (curOwner == Thread.currentThread()))
			return true;
		else
			return false;
	}

	/**
	 * Releases lock
	 * 
	 */
	public synchronized void release() {

		if (curOwner == Thread.currentThread()) {
			curOwner = null;
			notify();
			// } else
			// throw Exception("not owner of mutex");
		}
	}
}
