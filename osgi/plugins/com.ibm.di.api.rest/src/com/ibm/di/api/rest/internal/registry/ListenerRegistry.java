/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.ibm.di.web.common.atom.AtomText;
import com.ibm.di.api.bind.Listener;
import com.ibm.di.api.remote.AssemblyLineListener;
import com.ibm.di.api.remote.DIEventListener;
import com.ibm.di.api.remote.LogListener;
import com.ibm.di.api.remote.RemoteListener;

/**
 * A registry of the api listeners. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class ListenerRegistry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// ListenerId : Listener
	private Map<String, ListenerRegistration<RemoteListener, Listener>> sessionListeners = new HashMap<String, ListenerRegistration<RemoteListener, Listener>>();

	// configId : (ListenerId : Listener)
	private Map<String, Map<String, ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener>>> ciListeners = new HashMap<String, Map<String, ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener>>>();

	// configId : (AlName : (ListenerId : Listener))
	private Map<String, Map<String, Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>>>> alListeners = new HashMap<String, Map<String, Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>>>>();

	// start counter from current time to assure id uniqueness in case
	// persistent loggers are implemented later.
	private AtomicLong idCounter = new AtomicLong(System.currentTimeMillis());

	/**
	 * Registers a listener a server listener.
	 * 
	 * @param listener
	 *            the listener
	 * @return
	 */
	public ListenerRegistration<RemoteListener, Listener> register(RemoteListener listener, RemoteListener exported, Listener rep) {
		String key = Long.toString(idCounter.incrementAndGet());
		ListenerRegistration<RemoteListener, Listener> reg = new ListenerRegistration<RemoteListener, Listener>("srv", key,
				listener, exported, rep);
		synchronized (sessionListeners) {
			sessionListeners.put(key, reg);
		}
		return reg;
	}

	/**
	 * Unregister a server listener
	 * 
	 * @param listenerId
	 *            the listener id received on registration.
	 * @return the listener registration as is before unregistering
	 */
	public ListenerRegistration<RemoteListener, Listener> unregister(String listenerId) {
		synchronized (sessionListeners) {
			return sessionListeners.remove(listenerId);
		}
	}

	/**
	 * Lists server listeners registrations.
	 * 
	 * @return a copy of the collection of registrations.
	 */
	public Collection<ListenerRegistration<RemoteListener, Listener>> getListenerRegs() {
		synchronized (sessionListeners) {
			ArrayList<ListenerRegistration<RemoteListener, Listener>> l = new ArrayList<ListenerRegistration<RemoteListener, Listener>>(
					sessionListeners.size());
			l.addAll(sessionListeners.values());
			return l;
		}
	}

	public ListenerRegistration<RemoteListener, Listener> getListenerReg(String listenerId) {
		synchronized (sessionListeners) {
			return sessionListeners.get(listenerId);
		}
	}

	/**
	 * Registers a listener for the specified config instance id
	 * 
	 * @param configId
	 *            the configInstance id whose listener to register
	 * @param listener
	 *            the listener to register
	 * @return the listener identifier valid in the scope of the config instance
	 */
	public ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener> register(String configId, LogListener listener,
			LogListener exported, com.ibm.di.api.bind.LogListener binding) {

		Map<String, ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener>> map;
		synchronized (ciListeners) {
			map = ciListeners.get(configId);
			if (map == null) {
				map = new HashMap<String, ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener>>();
				ciListeners.put(configId, map);
			}
		}

		String listenerId = Long.toString(idCounter.incrementAndGet());
		ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener> reg;
		synchronized (map) {
			reg = new ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener>("ci/" + configId, listenerId, listener,
					exported, binding);
			map.put(listenerId, reg);
		}

		return reg;
	}

	/**
	 * Unregister a comnfigInstance listener
	 * 
	 * @param configId
	 *            the configInstance id whose listener to unregister
	 * @param listenerId
	 *            the listener id received on registration.
	 * @return the listener registration as is before unregistering
	 */
	public ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener> unregister(String configId, String listenerId) {
		Map<String, ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener>> map = null;
		synchronized (ciListeners) {
			map = ciListeners.get(configId);
		}

		if (map != null) {
			synchronized (map) {
				ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener> reg = map.remove(listenerId);
				if (map.size() == 0) {
					synchronized (ciListeners) {
						ciListeners.remove(configId);
					}
				}
				return reg;
			}
		}
		return null;
	}

	/**
	 * Lists the listeners registrations for the particular configInstance.
	 * 
	 * @return a copy of the collection of registrations.
	 */
	public Collection<ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener>> getListenerRegs(String configId) {
		Map<String, ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener>> map;
		synchronized (ciListeners) {
			map = ciListeners.get(configId);
		}

		if (map != null) {
			synchronized (map) {
				return map.values();
			}
		}
		return Collections.emptyList();
	}

	public ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener> getListenerReg(String configId, String listenerId) {
		Map<String, ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener>> map;
		synchronized (ciListeners) {
			map = ciListeners.get(configId);
		}

		if (map != null) {
			synchronized (map) {
				return map.get(listenerId);
			}
		}
		return null;
	}

	/**
	 * Registers a listener for the specified AL
	 * 
	 * @param configId
	 *            the configInstance id
	 * @param alName
	 *            the AL name whose listener to register
	 * @param listener
	 *            the listener to register
	 * @return the listener identifier valid in the scope of the AL
	 */
	public ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener> register(String configId,
			String alName, AssemblyLineListener listener, AssemblyLineListener exported,
			com.ibm.di.api.bind.AssemblyLineListener binding) {

		Map<String, Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>>> als = null;
		synchronized (alListeners) {
			als = alListeners.get(configId);
			if (als == null) {
				als = new HashMap<String, Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>>>();
				alListeners.put(configId, als);
			}
		}

		Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>> map;
		synchronized (als) {
			map = als.get(alName);
			if (map == null) {
				map = new HashMap<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>>();
				als.put(alName, map);
			}
		}

		String listenerId = Long.toString(idCounter.incrementAndGet());
		ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener> reg = null;
		synchronized (map) {
			reg = new ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>("al/" + configId + "/"
					+ alName, listenerId, listener, exported, binding);
			map.put(listenerId, reg);
		}

		return reg;
	}

	/**
	 * Unregister an AL listener
	 * 
	 * @param configId
	 *            the configInstance id
	 * @param alName
	 *            the AL name whose listener to unregister
	 * @param listenerId
	 *            the listener id received on registration.
	 * @return the listener registration as is before unregistering
	 */
	public ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener> unregister(String configId,
			String alName, String listenerId) {
		Map<String, Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>>> als;
		synchronized (alListeners) {
			als = alListeners.get(configId);
		}

		if (als != null) {
			Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>> map;
			synchronized (als) {
				map = als.get(alName);
			}

			if (map != null) {
				synchronized (map) {
					ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener> reg = map
							.remove(listenerId);

					if (map.size() == 0) {
						synchronized (als) {
							als.remove(alName);

							if (als.size() == 0) {
								synchronized (alListeners) {
									alListeners.remove(configId);
								}
							}
						}
					}
					return reg;
				}
			}
		}
		return null;
	}

	/**
	 * Lists the listeners registrations for the particular AssemblyLine.
	 * 
	 * @return a copy of the collection of registrations.
	 */
	public Collection<ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>> getListenerRegs(
			String configId, String alId) {

		Map<String, Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>>> als;
		synchronized (alListeners) {
			als = alListeners.get(configId);
		}

		if (als != null) {
			Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>> map;
			synchronized (als) {
				map = als.get(alId);
			}
			if (map != null) {
				synchronized (map) {
					return map.values();
				}
			}
		}
		return Collections.emptyList();
	}

	public ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener> getListenerReg(String configId,
			String alId, String listenerId) {
		Map<String, Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>>> als;
		synchronized (alListeners) {
			als = alListeners.get(configId);
		}

		if (als != null) {
			Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>> map;
			synchronized (als) {
				map = als.get(alId);
			}
			if (map != null) {
				synchronized (map) {
					return map.get(listenerId);
				}
			}
		}
		return null;
	}

	public void unregister(RemoteListener baseListener) {
		if (baseListener instanceof DIEventListener) {
			synchronized (sessionListeners) {
				for (ListenerRegistration<RemoteListener, Listener> reg : sessionListeners.values()) {
					if (reg.getListener() == baseListener) {
						unregister(reg.getListenerId());
					}
				}
			}
		} else if (baseListener instanceof AssemblyLineListener) {
			ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener> registration = null;
			Collection<Map<String, Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>>>> copy1 = null;
			synchronized (alListeners) {
				copy1 = new ArrayList<Map<String, Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>>>>(
						alListeners.values());
			}

			Collection<Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>>> copy2 = new ArrayList<Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>>>();
			top_most: for (Map<String, Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>>> map1 : copy1) {
				copy2.clear();
				synchronized (map1) {
					copy2.addAll(map1.values());
				}

				for (Map<String, ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener>> map2 : copy2) {
					synchronized (map2) {
						for (ListenerRegistration<AssemblyLineListener, com.ibm.di.api.bind.AssemblyLineListener> reg : map2
								.values()) {
							if (reg.getListener() == baseListener) {
								// unregister but don't hold the lock
								registration = reg;
								break top_most;
							}
						}
					}
				}
			}
			if (registration != null) {
				int start = registration.getContextId().indexOf('/') + 1;
				int end = registration.getContextId().indexOf('/', start);
				unregister(registration.getContextId().substring(start, end), registration.getContextId().substring(end + 1),
						registration.getListenerId());
			}
		} else if (baseListener instanceof LogListener) {
			ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener> registration = null;
			Collection<Map<String, ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener>>> copy = null;
			synchronized (ciListeners) {
				copy = new ArrayList<Map<String, ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener>>>(ciListeners
						.values());
			}

			top_most: for (Map<String, ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener>> map : copy) {
				synchronized (map) {
					for (ListenerRegistration<LogListener, com.ibm.di.api.bind.LogListener> reg : map.values()) {
						if (reg.getListener() == baseListener) {
							// unregister but don't hold the lock
							registration = reg;
							break top_most;
						}
					}
				}
			}

			if (registration != null) {
				unregister(registration.getContextId().substring(registration.getContextId().indexOf('/') + 1), registration
						.getListenerId());
			}
		}
	}

	public static class ListenerRegistration<L extends RemoteListener, B extends Listener> {

		private final L exported;
		private final L listener;
		private final B binding;
		private final String contextId;
		private final String listenerId;

		private ListenerRegistration(String contextId, String listenerId, L listener, L exported, B binding) {
			this.contextId = contextId;
			this.listenerId = listenerId;
			this.listener = listener;
			this.exported = exported;
			this.binding = binding;
		}

		/**
		 * Uniqueness guaranteed for the ListenerRegistry
		 * 
		 * @return the regName
		 */
		public String getContextId() {
			return contextId;
		}

		/**
		 * @return the listener
		 */
		public L getListener() {
			return listener;
		}

		/**
		 * @return the binding
		 */
		public B getBinding() {
			return binding;
		}

		/**
		 * Unique in the scope of the object it listens on (CI, AL, etc.)
		 * 
		 * @return the listenerId
		 */
		public String getListenerId() {
			return listenerId;
		}

		/**
		 * @return the exported
		 */
		public L getExportedListener() {
			return exported;
		}
	}
}
