/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// Monitor.java
//
//
//
package com.ibm.di.server;

import java.awt.AWTEventMulticaster;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.di.event.ThreadEvent;

/**
 * This class is the base class for all IBM Tivoli Directory Integrator classes
 * which implement executable objects, like AssemblyLines. The
 * threadStarted(...) and threadStopped(...) methods are invoked at AssemblyLine
 * start and stop. These two methods make sure that the Server API is notified
 * of an AssemblyLine start/stop events, thus supporting the Server API event
 * model.
 */

public class Monitor extends Thread {

	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * A collection of the running threads.
	 */
	private static Vector<Thread> runningThreads = new Vector<Thread>();

	/**
	 * Whether the monitor had run an executable object (e.g. an AssemblyLine).
	 */
	private static boolean hasRunSomething = false;

	/**
	 * The ActionListener of the Monitor object.
	 */
	private static ActionListener actionListener = null;

	/**
	 * A Hashtable of the monitored threads.
	 */
	private static Hashtable<Integer, Thread> monitoredThreads = new Hashtable<Integer,Thread>();

	/**
	 * Constructor.
	 */
	public Monitor() {
	}

	/**
	 * Constructor with ThreadGroup and name.
	 */
	public Monitor(ThreadGroup group, String name) {
		super(group, name);
	}
	
	/**
	 * Sets the actionListener of the Monitor object.
	 * 
	 * @param l
	 *            the ActionListener to be added
	 */
	public static void addActionListener(ActionListener l) {
		actionListener = AWTEventMulticaster.add(actionListener, l);
	}

	/**
	 * Removes the actionListener of the Monitor object.
	 * 
	 * @param l
	 *            the ActionListener to be added
	 */
	public static void removeActionListener(ActionListener l) {
		actionListener = AWTEventMulticaster.remove(actionListener, l);
	}

	/**
	 * Invokes the actionListener with the given event.
	 * 
	 * @param e
	 *            the given ActionEvent object
	 */
	public synchronized void fireListener(ActionEvent e) {
		// System.out.println ("EVENT: " + e.toString());

		// *
		if (actionListener != null) {
			actionListener.actionPerformed(e);
		}
		// */
	}

	/**
	 * Adds a thread to the monitor.
	 * 
	 * @param uid
	 *            the id of the thread
	 * @param thread
	 *            the thread object
	 */
	public static synchronized void addThread(Integer uid, Thread thread) {
		monitoredThreads.put(uid, thread);
	}

	/**
	 * Removes a thread from the monitor.
	 * 
	 * @param uid
	 *            the thread's id
	 */
	public static synchronized void removeThread(Integer uid) {
		monitoredThreads.remove(uid);
	}

	/**
	 * Adds a given thread to the collection of running threads of the monitor.
	 * 
	 * @param thread
	 *            the thread that is starting
	 * @param text
	 *            a text message
	 */
	protected static void threadStarted(Object thread, String text) {
		synchronized (runningThreads) {
			if (thread instanceof Thread)
				runningThreads.add((Thread)thread);
		}
		hasRunSomething = true;
	}

	/**
	 * Removes a given thread to the collection of running threads of the
	 * monitor.
	 * 
	 * @param thread
	 *            the thread to be stopped
	 * @param text
	 *            a text message
	 * @param e
	 *            an exception
	 */
	protected static void threadStopped(Object thread, String text, Exception e) {
		synchronized (runningThreads) {
			runningThreads.remove(thread);
		}
	}

	/**
	 * Verifies if all threads have stopped.
	 * 
	 * @return <code>true</code> if all threads in the monitor have stopped,
	 *         otherwise <code>false</code>
	 */
	public static boolean allThreadsStopped() {
		synchronized (runningThreads) {
			return hasRunSomething && runningThreads.isEmpty();
		}
	}

	/**
	 * Returns a collection of the running AssemblyLines.
	 * 
	 * @return a collection of AssemblyLines
	 */
	public static Vector<AssemblyLine> runningALs() {
		Vector<AssemblyLine> v = new Vector<AssemblyLine>();
		synchronized (runningThreads) {
			for (Thread t:runningThreads) {
				if (t instanceof AssemblyLine)
					v.add((AssemblyLine)t);
			}
		}
		return v;
	}

	/**
	 * Returns a collection of the running Sequences.
	 * 
	 * @return a collection of Sequences
	 */
	public static Vector<Sequence> runningSequences() {
		Vector<Sequence> v = new Vector<Sequence>();
		synchronized (runningThreads) {
			for (Thread t:runningThreads) {
				if (t instanceof Sequence)
					v.add((Sequence)t);
			}
		}
		return v;
	}

	/**
	 * Sets a message for the monitor.
	 * 
	 * @param thread
	 *            a thread
	 * @param message
	 *            the new message
	 */
	public void setMonitorMessage(Object thread, String message) {
		fireListener(new ThreadEvent(thread, ThreadEvent.TE_LOGMSG, message,
				null));
	}
	// public static void

}
