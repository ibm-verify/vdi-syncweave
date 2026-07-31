/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.ci;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
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
import com.ibm.di.api.bind.Listener;
import com.ibm.di.api.bind.LogListener;
import com.ibm.di.api.bind.PollChannel;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.listener.ListenerContext;
import com.ibm.di.api.rest.internal.listener.ListenerFactory;
import com.ibm.di.api.rest.internal.registry.ListenerRegistry;
import com.ibm.di.api.rest.internal.registry.ListenerRegistry.ListenerRegistration;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class CiListenerContext implements ListenerContext<LogListener> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final List<AtomCategory> pollCats = Arrays.asList(AppConstants.CAT_LISTENER_LOG, AppConstants.CAT_LISTENER_POLL);
	private static final List<AtomCategory> pushCats = Arrays.asList(AppConstants.CAT_LISTENER_LOG, AppConstants.CAT_LISTENER_PUSH);

	private final ConfigInstance ci;
	private final ListenerFactory fact;
	private final ListenerRegistry reg;
	private final CIListenerAttacher att;

	public CiListenerContext(ConfigInstance ci, ListenerFactory fact, ListenerRegistry reg) {
		this.ci = ci;
		this.att = null;
		this.fact = fact;
		this.reg = reg;
	}

	public CiListenerContext(CIListenerAttacher att, ListenerFactory fact, ListenerRegistry reg) {
		this.att = att;
		this.ci = null;
		this.fact = fact;
		this.reg = reg;
	}

	public String create(LogListener listener) throws DIException, RemoteException {
		com.ibm.di.api.remote.LogListener baseListener = fact.newInstance(listener);

		// export the listener
		com.ibm.di.api.remote.LogListener expListener = fact.export(baseListener);
		// register the exported listener
		if (ci != null) {
			ci.addLogListener(expListener);
		} else {
			att.attachListener(expListener);
		}

		ListenerRegistration<com.ibm.di.api.remote.LogListener, LogListener> registration = reg.register(ci != null ? ci
				.getConfigId() : att.getConfigId(), baseListener, expListener, listener);

		return registration.getListenerId();
	}

	public void delete(String lId) throws RemoteException, DIException {
		ListenerRegistration<com.ibm.di.api.remote.LogListener, LogListener> r = reg.unregister(ci != null ? ci.getConfigId() : att
				.getConfigId(), lId);
		if (r != null) {
			if (ci != null) {
				if (r.getExportedListener() == null) {
					// dev msg only!
					throw new IllegalStateException("No export");
				}

				ci.removeLogListener(r.getExportedListener());
			} else {
				att.detachListener(r);
			}
		}
	}

	public LogListener get(String lId) throws RemoteException, DIException {
		ListenerRegistration<com.ibm.di.api.remote.LogListener, LogListener> r = reg.getListenerReg(ci.getConfigId(), lId);
		return r == null ? null : r.getBinding();
	}

	public List<AtomCategory> getCategories(String listenerId) throws RemoteException, DIException {
		Listener l = get(listenerId);
		if (l.getChannel() instanceof PollChannel) {
			return pollCats;
		}
		return pushCats;
	}

	public Map<String, LogListener> list() throws RemoteException, DIException {
		Collection<ListenerRegistration<com.ibm.di.api.remote.LogListener, LogListener>> regs = reg.getListenerRegs(ci
				.getConfigId());
		Map<String, LogListener> res = new HashMap<String, LogListener>(regs.size());

		for (ListenerRegistration<com.ibm.di.api.remote.LogListener, LogListener> reg : regs) {
			res.put(reg.getListenerId(), reg.getBinding());
		}

		return res;
	}

	public List<AtomLink> getLinks(URI baseUri, String listenerId) throws RemoteException, DIException {
		if (ci == null) {
			// seen by devs only as current code always has CI but future might
			// use ListenerAttacher and don't have CI.
			throw new NullPointerException("Need ci!");
		}

		Listener l = get(listenerId);
		if (l.getChannel() instanceof PollChannel) {
			AtomLink link = new AtomLink();
			link.setRel(AppConstants.REL_POLL);
			try {
				link.setHref(baseUri.toString() + "listener/poll/" + URLEncoder.encode(ci.getConfigId(), "UTF-8") + "/"
						+ listenerId);
			} catch (UnsupportedEncodingException e) {
				// should not happen
				e.printStackTrace();
			}

			ArrayList<AtomLink> list = new ArrayList<AtomLink>(1);
			list.add(link);
			return list;
		}
		return Collections.emptyList();
	}

	/**
	 * Interface for providing CI Listener registration details obtained during
	 * listener attaching process.
	 */
	static interface CIListenerAttacher extends ListenerAttacher<com.ibm.di.api.remote.LogListener> {
		/**
		 * Obtains the configId after
		 * {@link #attachListener(com.ibm.di.api.remote.LogListener)} has been
		 * called.
		 * 
		 * @return the configId
		 * @throws DIException
		 * @throws RemoteException
		 */
		public String getConfigId() throws RemoteException, DIException;
	}
}
