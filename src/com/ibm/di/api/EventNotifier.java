/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ibm.di.api.local.DIEventListener;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

/**
 * <p>
 * This class implements event broadcasting for the use of the Server API.
 * Events are dispatched to registered event listeners. When a listener is being
 * registered, event filters are specified, so that the listener receives only
 * events of interest. Events are filtered based on their type and their id.
 * </p>
 * 
 * <p>
 * An atomic filter is a string, that does not contain whitespaces, newlines,
 * commas and semi-colons. If an atomic filter ends with '*', it matches all
 * texts, whose prefix is the same as the filter up to the '*' symbol. For
 * example a "di.al.*" filter matches both "di.al.start" and "di.al.stop". A
 * composite filter is a string that contains atomic filters, separated by
 * whitespaces. (Note that an atomic filter is a valid composite filter.) A
 * composite filter matches a piece of text, if any of its atomic filters
 * matches the text. An empty string is considered to be a match-none filter.
 * null is considered a match-all filter.
 * </p>
 * 
 * <p>
 * Two modes of operation are supported - synchronous and asynchronous. In
 * synchronous mode, the 'broadcastEvent' method completes only after all
 * matching listeners have handled the event. In asynchronous mode, there is no
 * such guarantee.
 * </p>
 */
public class EventNotifier {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * A filter that matches all events.
	 */
	public static final String MATCH_ALL_FILTER = null;

	/**
	 * A filter that matches no events.
	 */
	public static final String MATCH_NONE_FILTER = "";

	/**
	 * A set of allowed delimiters between the atomic filters in a composite
	 * filter.
	 */
	private static final String COMPOSITE_FILTER_DELIMITERS = "\r\n\t ;";

	/**
	 * A list of registered event listeners.
	 */
	private Vector<DIEventListener> listeners = null;

	/**
	 * A list of composite filters that match event type. The filter at position
	 * n corresponds to the listener at position n in the listeners list. Each
	 * type filter is represented as a list object.
	 */
	private Vector<List<String>> typeFiltersLists = null;

	/**
	 * A list of id filters: The filter at position n corresponds to the
	 * listener at position n in the listeners list.
	 */
	private Vector<String> idFilters = null;

	/**
	 * Whether the notifier operates in synchronous or in asynchronous mode.
	 */
	private boolean sync = true;

	/**
	 * A composite filter that matches suppressed event types.
	 */
	private List<String> suppressedEventTypes = null;

	private ExecutorService executor;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Create a new notifier object with an empty set of event listeners.
	 * 
	 * @param sync
	 *            whether the notifier will operate in synchronous mode
	 * @param suppressedEventTypesStr
	 *            a list of filters for event types that will be suppressed by
	 *            the notifier; suppressed events will not be propagated by the
	 *            notifier events will not be propagated to listeners
	 */
	public EventNotifier(boolean sync, String suppressedEventTypesStr) {
		this.sync = sync;
		if (!sync) {
			this.executor = Executors.newCachedThreadPool();
		}
		listeners = new Vector<DIEventListener>();
		typeFiltersLists = new Vector<List<String>>();
		idFilters = new Vector<String>();
		suppressedEventTypes = createFiltersList(suppressedEventTypesStr);
	}

	/**
	 * Register a new event listener accompanied with event filters. The method
	 * accepts a filter that matches event type and a filter that matches event
	 * id. Both the type filter and the id filter must match an event if the
	 * listener is to receive it. If, for example, the type filter matches an
	 * event, but the id filter does not, the event will not be passed to the
	 * listener. If the listener object is already registered, the method will
	 * ignore the request.
	 * 
	 * @param listener
	 *            an event listener
	 * @param typeFiltersStr
	 *            a composite filter that matches event type
	 * @param idFilter
	 *            an atomic filter that matches event id
	 * @throws DIException
	 *             if the listener is null
	 */
	public synchronized void addEventListener(DIEventListener listener, String typeFiltersStr, String idFilter) throws DIException {
		if (listener == null) {
			throw new DIException(sResHash.getString("SEVER.API.LISTENER.OBJECT.IS.NULL"));
		}

		List<String> typeFiltersList = createFiltersList(typeFiltersStr);
		String canonicalIdFilter = toCanonicalFilter(idFilter);

		if (!listeners.contains(listener)) {
			listeners.add(listener);
			typeFiltersLists.add(typeFiltersList);
			idFilters.add(canonicalIdFilter);
		}
	}

	/**
	 * Converts a filter, received as user input to a form that is suitable for
	 * internal usage.
	 * 
	 * @param filter
	 *            an atomic filter
	 * @return an atomic filter, fit for internal usage
	 */
	private static String toCanonicalFilter(String filter) {

		if (filter == null) {
			return MATCH_ALL_FILTER;
		}

		String canonicalFilter = filter.trim();

		if (canonicalFilter.equals("")) {
			return MATCH_NONE_FILTER;
		}

		if (canonicalFilter.equals("*")) {
			return MATCH_ALL_FILTER;
		}

		// strip trailing '*'
		if (canonicalFilter.endsWith("*")) {
			canonicalFilter = canonicalFilter.substring(0, canonicalFilter.length() - 1);
		}

		return canonicalFilter;
	}

	/**
	 * Unregisters an event listener. After unregistering, the listener will no
	 * longer receive events from the notifier.
	 * 
	 * @param listener
	 *            an event listener
	 * @return whether the listener was unregistered successfully
	 */
	public synchronized boolean removeEventListener(DIEventListener listener) {
		if (listener == null) {
			return false;
		}

		boolean removed = false;
		for (int i = 0; i < listeners.size(); i++) {
			if (listener.equals(listeners.get(i))) {
				listeners.remove(i);
				typeFiltersLists.remove(i);
				idFilters.remove(i);
				removed = true;
				break;
			}
		}

		return removed;
	}

	/**
	 * Converts a composite filter from a string to a list.
	 * 
	 * @param filtersStr
	 *            a composite filter as string
	 * @return a composite filter as a list
	 */
	private static List<String> createFiltersList(String filtersStr) {
		ArrayList<String> result = new ArrayList<String>();

		if (filtersStr != null) {
			StringTokenizer st = new StringTokenizer(filtersStr, COMPOSITE_FILTER_DELIMITERS);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				token = toCanonicalFilter(token);
				if (!result.contains(token)) {
					result.add(token);
				}
			}
		} else {
			// treat as a match-all filter
			result.add(MATCH_ALL_FILTER);
		}
		return result;
	}

	/**
	 * Matches a piece of text against an atomic filter.
	 * 
	 * @param token
	 *            a piece of text
	 * @param filter
	 *            an atomic filter in canonical form
	 * @return whether the filter matches the token
	 */
	private static boolean match(String token, String filter) {

		// match-all ?
		if (filter == null) {
			return true;
		}

		// match-none ?
		if (filter.equals("")) {
			return false;
		}

		return token != null && token.startsWith(filter);
	}

	/**
	 * Matches a piece of text against a composite filter.
	 * 
	 * @param token
	 *            a piece of text
	 * @param filtersList
	 *            a non-null composite filter; the filter must contain only
	 *            atomic filters in canonical form
	 * @return whether the filter matches the token
	 */
	private static boolean match(String token, List<String> filtersList) {

		boolean result = false;

		if (filtersList != null) {

			Iterator<String> it = filtersList.iterator();
			while (it.hasNext()) {

				String filter = it.next();

				if (match(token, filter)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Matches an event against a type filter and an id filter. The method
	 * returns true, if the event's type matches the type filter and the event's
	 * id matches the id filter.
	 * 
	 * @param event
	 *            an event
	 * @param typeFiltersList
	 *            a composite filter to match event type; the filter must
	 *            contain only atomic filters in canonical form
	 * @param idFilter
	 *            an atomic filter in canonical form to match event id
	 * @return whether the filters match the event
	 */
	private static boolean match(DIEvent event, List<String> typeFiltersList, String idFilter) {

		return match(event.getType(), typeFiltersList) && match(event.getId(), idFilter);
	}

	/**
	 * Broadcast an event to all registered listeners, whose filters match the
	 * event. If the event's type is suppressed, the method will ignore the
	 * event. If the notifier operates in synchronous mode, the method returns
	 * only after all listeners have handled the event.
	 * 
	 * @param event
	 *            an event
	 */
	public void broadcastEvent(final DIEvent event) {

		/*
		 * Event suppression does not need to be synchronized
		 */

		if (!match(event.getType(), suppressedEventTypes)) {

			// Broadcast only non-suppressed events.
			broadcastEventImpl(event);
		}

	}

	/**
	 * Broadcast an event to all registered listeners, whose filters match the
	 * event. Event suppression is not taken into consideration.
	 * 
	 * @param event
	 *            an event
	 */
	private synchronized void broadcastEventImpl(final DIEvent event) {

		List<String> typeFiltersList = null;
		String idFilter = null;

		for (int i = listeners.size() - 1; i >= 0; i--) {
			typeFiltersList = typeFiltersLists.get(i);
			idFilter = (String) idFilters.get(i);

			if (match(event, typeFiltersList, idFilter)) {

				final DIEventListener listener = (DIEventListener) listeners.get(i);
				if (sync) {
					try {
						listener.handleEvent(event);
					} catch (DIException e) {
						APIEngine.logError(sResHash.getString("SEVER.API.ERROR.ON.HANDLEEVENT.1", e.toString()));
					} catch (RuntimeException re) {
						APIEngine.logError(sResHash.getString("SEVER.API.ERROR.ON.HANDLEEVENT.1", re.toString()));
						removeEventListener(listener);
						throw re;
					}
				} else {
					executor.submit(new Runnable() {
						public void run() {
							try {
								listener.handleEvent(event);
							} catch (DIException e) {
								APIEngine.logError(sResHash.getString("SEVER.API.ERROR.ON.HANDLEEVENT.2", e.toString()));
							} catch (RuntimeException re) {
								APIEngine.logError(sResHash.getString("SEVER.API.ERROR.ON.HANDLEEVENT.2", re.toString()));
								removeEventListener(listener);
							}
						}
					});
				}
			}
		}
	}
}
