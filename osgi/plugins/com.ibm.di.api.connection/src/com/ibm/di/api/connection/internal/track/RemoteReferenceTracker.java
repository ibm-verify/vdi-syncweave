/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal.track;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.rmi.server.Unreferenced;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks Remote objects exported on the client side if they implement the
 * {@link Unreferenced} to notify them when no code reference them any more. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public final class RemoteReferenceTracker implements Runnable {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final Logger log = LoggerFactory.getLogger(RemoteReferenceTracker.class);

	private static List<RemoteReference> remotes = new ArrayList<RemoteReference>();
	private static ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();

	private Thread thread;

	public RemoteReferenceTracker() {
		thread = new Thread(this, "RemoteReferenceTracker");
		thread.setDaemon(true);
		thread.start();
	}

	public void run() {
		Reference<? extends Object> ref;
		try {
			while (!thread.isInterrupted() && (ref = refQueue.remove()) != null) {
				try {
					((RemoteReference) ref).remote.unreferenced();
				} catch (RuntimeException e) {
					log.error(e.getMessage(), e);
				} finally {
					synchronized (remotes) {
						remotes.remove(ref);
					}
				}
			}
		} catch (InterruptedException e) {
			;
		}
	}

	public void track(Object instance, Unreferenced remote) {
		if (instance == remote) {
			// dev msg only
			throw new IllegalArgumentException("instance and remote must be different");
		}
		synchronized (remotes) {
			remotes.add(new RemoteReference(instance, remote, refQueue));
		}
	}

	public void dispose() {
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	private static class RemoteReference extends WeakReference<Object> {
		private final Unreferenced remote;

		public RemoteReference(Object instance, Unreferenced remote, ReferenceQueue<Object> refQueue) {
			super(instance, refQueue);
			this.remote = remote;
		}
	}
}
