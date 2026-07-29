/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This class is for internal use only. Users must not rely on it.
 * </p>
 * 
 * <p>
 * Thread safe container for listeners. Relies on double-dispatch via visitors
 * instead of using event objects.
 * </p>
 * 
 * @param <ListenerT>
 *            Type of listener.
 * 
 * @since 7.0
 */
public class ThreadSafeListenableImpl<ListenerT> implements Listenable<ListenerT> {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Visitor of listeners. This visitor can throw checked exceptions. Use it
	 * to invoke listener methods that throw.
	 */
	public static interface ThrowingVisitor<ListenerT> {
		void visit(ListenerT listener) throws Exception;
	}

	/**
	 * Visitor of listeners. Use it to invoke listener methods that do not
	 * throw.
	 */
	public static interface Visitor<ListenerT> {
		void visit(ListenerT listener);
	}

	/**
	 * List of registered listeners.
	 */
	private List<ListenerT> listeners = new ArrayList<ListenerT>();

	/**
	 * Register new listener.
	 * 
	 * @param listener
	 *            Listener.
	 */
	public void addListener(ListenerT listener) {
		if (listener == null) {
			return;
		}
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	/**
	 * Unregister listener. Due to implementation specifics, the listener may
	 * get notified a few more times after it is unregistered. It is guaranteed
	 * that the listener will not get notified after it is removed, if you have
	 * only one notification thread ( {@link #visitListeners(ThrowingVisitor)}
	 * is called always by the same thread) and removal happens on that
	 * notification thread.
	 * 
	 * @param listener
	 *            Registered listener.
	 * @return the actual listener being registered. This is useful when the
	 *         passed in instance is only used for identification and the actual
	 *         listener needs to be properly disposed of.
	 */
	public ListenerT removeListener(ListenerT listener) {
		if (listener == null) {
			return null;
		}
		synchronized (listeners) {
			for (ListenerT l : listeners) {
				if (listener.equals(l)) {
					return l;
				}
			}
		}
		return null;
	}

	/**
	 * Visit registered listeners with the specified visitor.
	 * 
	 * @param visitor
	 *            Visitor.
	 * @throws Exception
	 *             Error thrown by the visitor.
	 */
	public void visitListeners(ThrowingVisitor<ListenerT> visitor) throws Exception {
		/*
		 * Follow the behavior of java.util.Observable.notifyObservers: do not
		 * hold the lock while notifying the listeners, so that listeners can
		 * safely invoke add/remove listener methods on the Listenable without
		 * causing a deadlock. The price we pay is that some listeners may get
		 * notified even after they are removed. We do not use
		 * java.util.Observable directly because it is awkward:
		 * java.util.Observable has only one method so we have to use event
		 * objects to multiplex different notifications on that method, this
		 * means that each observer must have an ugly switch statement. Besides
		 * the setChanged() method must be called on java.util.Observable before
		 * each notification.
		 */
		List<ListenerT> copyOfListeners;
		synchronized (listeners) {
			if (listeners.size() < 1) {
				return;
			}
			copyOfListeners = new ArrayList<ListenerT>();
			copyOfListeners.addAll(listeners);
		}
		/*
		 * At this point some listeners may get removed, but they will still get
		 * notified.
		 */
		for (ListenerT l : copyOfListeners) {
			visitor.visit(l);
		}
	}

	/**
	 * Visit registered listeners with the specified visitor.
	 * 
	 * @param visitor
	 *            Visitor.
	 */
	public void visitListeners(Visitor<ListenerT> visitor) {
		List<ListenerT> copyOfListeners;
		synchronized (listeners) {
			if (listeners.size() < 1) {
				return;
			}
			copyOfListeners = new ArrayList<ListenerT>();
			copyOfListeners.addAll(listeners);
		}
		for (ListenerT l : copyOfListeners) {
			visitor.visit(l);
		}
	}
}
