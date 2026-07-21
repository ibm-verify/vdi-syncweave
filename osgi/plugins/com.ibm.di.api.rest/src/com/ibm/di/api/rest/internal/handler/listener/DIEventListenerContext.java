/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.listener;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.di.web.common.atom.AtomText;
import com.ibm.di.web.common.atom.AtomCategory;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.ConfigFileListener;
import com.ibm.di.api.bind.Listener;
import com.ibm.di.api.bind.PollChannel;
import com.ibm.di.api.remote.ConfigurationFileListener;
import com.ibm.di.api.remote.DIEventListener;
import com.ibm.di.api.remote.RemoteListener;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.listener.ListenerContext;
import com.ibm.di.api.rest.internal.listener.ListenerFactory;
import com.ibm.di.api.rest.internal.registry.ListenerRegistry;
import com.ibm.di.api.rest.internal.registry.ListenerRegistry.ListenerRegistration;

/**
 * A context for handling {@link DIEventListener}s. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class DIEventListenerContext implements ListenerContext<Listener> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final List<AtomCategory> diPollCats = Arrays.asList(AppConstants.CAT_LISTENER_EVENT,
			AppConstants.CAT_LISTENER_POLL);
	private static final List<AtomCategory> diPushCats = Arrays.asList(AppConstants.CAT_LISTENER_EVENT,
			AppConstants.CAT_LISTENER_PUSH);
	private static final List<AtomCategory> configPollCats = Arrays.asList(AppConstants.CAT_LISTENER_CONFIG_FILE,
			AppConstants.CAT_LISTENER_POLL);
	private static final List<AtomCategory> configPushCats = Arrays.asList(AppConstants.CAT_LISTENER_CONFIG_FILE,
			AppConstants.CAT_LISTENER_PUSH);

	private final Session session;
	private final ListenerRegistry reg;
	private final ListenerFactory fact;

	public DIEventListenerContext(Session session, ListenerFactory fact, ListenerRegistry reg) {
		this.session = session;
		this.fact = fact;
		this.reg = reg;
	}

	public String create(Listener listener) throws DIException, RemoteException {

		RemoteListener baseListener;
		if (listener instanceof com.ibm.di.api.bind.DIEventListener) {
			baseListener = fact.newInstance((com.ibm.di.api.bind.DIEventListener) listener);
		} else if (listener instanceof ConfigFileListener) {
			baseListener = fact.newInstance((ConfigFileListener) listener);
		} else {
			throw new IllegalArgumentException(listener.toString());
		}

		// export the listener
		RemoteListener expListener = fact.export(baseListener);

		// register the listener
		if (baseListener instanceof DIEventListener) {
			session.addEventListener((DIEventListener) expListener, ((com.ibm.di.api.bind.DIEventListener) listener)
					.getTypeFilter(), ((com.ibm.di.api.bind.DIEventListener) listener).getIdFilter());
		} else {
			session.addEventListener((ConfigurationFileListener) expListener);
		}

		ListenerRegistration<RemoteListener, Listener> registration = reg.register(baseListener, expListener, listener);
		return registration.getListenerId();
	}

	public void delete(String lId) throws RemoteException, DIException {
		ListenerRegistration<RemoteListener, Listener> r = reg.unregister(lId);
		if (r != null) {
			if (r.getExportedListener() == null) {
				// dev msg only!
				throw new IllegalStateException("No export");
			}
			if (r.getExportedListener() instanceof DIEventListener) {
				session.removeEventListener((DIEventListener) r.getExportedListener());
			} else {
				session.removeEventListener((ConfigurationFileListener) r.getExportedListener());
			}
		}
	}

	public Listener get(String lId) {
		ListenerRegistration<RemoteListener, Listener> r = reg.getListenerReg(lId);
		return r == null ? null : r.getBinding();
	}

	public List<AtomCategory> getCategories(String listenerId) {
		Listener l = get(listenerId);
		if (l.getChannel() instanceof PollChannel) {
			return l instanceof ConfigFileListener ? configPollCats : diPollCats;
		}
		return l instanceof ConfigFileListener ? configPushCats : diPushCats;
	}

	public Map<String, Listener> list() {
		Collection<ListenerRegistration<RemoteListener, Listener>> regs = reg.getListenerRegs();
		Map<String, Listener> res = new HashMap<String, Listener>(regs.size());

		for (ListenerRegistration<RemoteListener, Listener> reg : regs) {
			res.put(reg.getListenerId(), reg.getBinding());
		}
		return res;
	}

	public List<AtomLink> getLinks(URI baseUri, String listenerId) {
		Listener l = get(listenerId);
		if (l.getChannel() instanceof PollChannel) {
			AtomLink link = new AtomLink();
			link.setRel(AppConstants.REL_POLL);
			link.setHref(baseUri.toString() + "listener/poll/" + listenerId);

			ArrayList<AtomLink> list = new ArrayList<AtomLink>(1);
			list.add(link);
			return list;
		}
		return Collections.emptyList();
	}
}
