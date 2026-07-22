/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.queue;

import java.util.Vector;

import com.ibm.di.server.Log;

public class QNotification extends Thread {
	/*
	 * arrays of callback reigtered with the queue one array for each
	 * empty/full/data available triggers
	 */
	private Vector cb_Empty;

	private Vector cb_Full;

	private Vector cb_DataAvailable;

	public final static int OP_FULL = 1;

	public final static int OP_EMPTY = 2;

	public final static int OP_AVAILABLE = 3;

	private int purpose = 0;

	private MemBufferQ source;

	private Log log;

	public QNotification(Log l, MemBufferQ src) {
		// callbacks
		cb_Empty = new Vector();
		cb_Full = new Vector();
		cb_DataAvailable = new Vector();

		log = l;

		source = src;
	}

	/**
	 * setPurpose set the operation that should be handled
	 * 
	 * @param p
	 *            full/empty/data available
	 */
	public void setPurpose(int p) {
		purpose = p;
	}

	/**
	 * register : registers a callback which will be called when memQ is
	 * empty/full/data available it is assumed that the callback implements the
	 * handle()function
	 * 
	 * @param a
	 *            object to be triggered
	 * @param purpose
	 *            full/empty/data available
	 */
	public void register(QCallback a, int purpose) {
		switch (purpose) {
		case OP_FULL:
			if (!cb_Full.contains(a))
				cb_Full.add(a);
			break;
		case OP_EMPTY:
			if (!cb_Empty.contains(a))
				cb_Empty.add(a);
			break;
		case OP_AVAILABLE:
			if (!cb_DataAvailable.contains(a))
				cb_DataAvailable.add(a);
			break;
		}
	}

	/**
	 * unregister : unregisters a callback
	 * 
	 * @param a
	 *            object to be unregistered
	 * @param purpose
	 *            full/empty/data available if -1 unregister the object from all
	 *            callbacks
	 * @throws Exception
	 *             if callback object is not found
	 */
	public void unregister(QCallback a, int purpose) throws Exception {
		switch (purpose) {
		case OP_FULL:
			if (cb_Full.contains(a))
				cb_Full.remove(a);
			else {
				log.error("queue.error.callbacknotfound");
				throw new Exception(log
						.getString("queue.error.callbacknotfound"));
			}
			break;
		case OP_EMPTY:
			if (cb_Empty.contains(a))
				cb_Empty.remove(a);
			else {
				log.error("queue.error.callbacknotfound");
				throw new Exception(log
						.getString("queue.error.callbacknotfound"));
			}
			break;
		case OP_AVAILABLE:
			if (cb_DataAvailable.contains(a))
				cb_DataAvailable.remove(a);
			else {
				log.error("queue.error.callbacknotfound");
				throw new Exception(log
						.getString("queue.error.callbacknotfound"));
			}
			break;
		case -1:
			if (cb_Full.contains(a))
				cb_Full.remove(a);
			if (cb_Empty.contains(a))
				cb_Empty.remove(a);
			if (cb_DataAvailable.contains(a))
				cb_DataAvailable.remove(a);
			break;
		}
	}

	/**
	 * run : the thread run method issues full/empty/data available triggers to
	 * objects registered for respective triggers
	 */
	public void run() {
		int i = 0;
		QCallbackEvent event = new QCallbackEvent(source, purpose);
		switch (purpose) {
		case OP_FULL:
			for (i = 0; i < cb_Full.size(); i++)
				((QCallback) cb_Full.elementAt(i)).handle(event);
			break;
		case OP_EMPTY:
			for (i = 0; i < cb_Empty.size(); i++)
				((QCallback) cb_Empty.elementAt(i)).handle(event);
			break;
		case OP_AVAILABLE:
			for (i = 0; i < cb_DataAvailable.size(); i++)
				((QCallback) cb_DataAvailable.elementAt(i)).handle(event);
			break;
		}
	}
}
