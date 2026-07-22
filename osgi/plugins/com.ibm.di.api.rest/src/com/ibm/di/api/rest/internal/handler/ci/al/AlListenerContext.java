/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.ci.al;

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
import com.ibm.di.api.bind.AssemblyLineListener;
import com.ibm.di.api.bind.Listener;
import com.ibm.di.api.bind.PollChannel;
import com.ibm.di.api.remote.AssemblyLine;
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
public class AlListenerContext implements ListenerContext<AssemblyLineListener> {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final List<AtomCategory> pollCats = Arrays.asList(AppConstants.CAT_LISTENER_AL, AppConstants.CAT_LISTENER_POLL);
	private static final List<AtomCategory> pushCats = Arrays.asList(AppConstants.CAT_LISTENER_AL, AppConstants.CAT_LISTENER_PUSH);

	private final AssemblyLine al;
	private final ListenerFactory fact;
	private final ListenerRegistry reg;
	private final String ciId;
	private final ALListenerAttacher att;

	public AlListenerContext(String ciId, AssemblyLine al, ListenerFactory fact, ListenerRegistry reg) {
		this.ciId = ciId;
		this.al = al;
		this.fact = fact;
		this.reg = reg;
		this.att = null;
	}

	public AlListenerContext(String ciId, ALListenerAttacher att, ListenerFactory fact, ListenerRegistry reg) {
		this.ciId = ciId;
		this.att = att;
		this.fact = fact;
		this.reg = reg;
		this.al = null;
	}

	public String create(AssemblyLineListener listener) throws DIException, RemoteException {
		com.ibm.di.api.remote.AssemblyLineListener baseListener = fact.newInstance(listener);
		// export the listener
		com.ibm.di.api.remote.AssemblyLineListener expListener = fact.export(baseListener);
		// register the listener
		if (al != null) {
			al.addListener(expListener, listener.isDeliverLogs(), listener.isDeliverEntry());
		} else {
			att.attachListener(expListener);
		}

		ListenerRegistration<com.ibm.di.api.remote.AssemblyLineListener, AssemblyLineListener> registration = reg.register(ciId,
				Integer.toString(al != null ? al.getUniqueCode() : att.getAL().getUniqueCode()), baseListener, expListener,
				listener);

		return registration.getListenerId();
	}

	public void delete(String lId) throws RemoteException, DIException {
		ListenerRegistration<com.ibm.di.api.remote.AssemblyLineListener, AssemblyLineListener> r = reg.unregister(ciId, Integer
				.toString(al != null ? al.getUniqueCode() : att.getAL().getUniqueCode()), lId);
		if (r != null) {
			if (al != null) {
				if (r.getExportedListener() == null) {
					// dev msg only!
					throw new IllegalStateException("No export");
				}
				al.removeListener(r.getExportedListener());
			} else {
				att.detachListener(r);
			}
		}
	}

	public AssemblyLineListener get(String lId) throws RemoteException, DIException {
		ListenerRegistration<com.ibm.di.api.remote.AssemblyLineListener, AssemblyLineListener> r = reg.getListenerReg(ciId, Integer
				.toString(al.getUniqueCode()), lId);
		return r == null ? null : r.getBinding();
	}

	public List<AtomCategory> getCategories(String listenerId) throws RemoteException, DIException {
		Listener l = get(listenerId);
		if (l.getChannel() instanceof PollChannel) {
			return pollCats;
		}
		return pushCats;
	}

	public Map<String, AssemblyLineListener> list() throws RemoteException, DIException {
		Collection<ListenerRegistration<com.ibm.di.api.remote.AssemblyLineListener, AssemblyLineListener>> regs = reg
				.getListenerRegs(ciId, Integer.toString(al.getUniqueCode()));
		Map<String, AssemblyLineListener> res = new HashMap<String, AssemblyLineListener>(regs.size());

		for (ListenerRegistration<com.ibm.di.api.remote.AssemblyLineListener, AssemblyLineListener> reg : regs) {
			res.put(reg.getListenerId(), reg.getBinding());
		}

		return res;
	}

	public List<AtomLink> getLinks(URI baseUri, String listenerId) throws RemoteException, DIException {
		if (al == null) {
			// seen by devs only as current code always has AL but future might
			// use ListenerAttacher and don't have AL.
			throw new NullPointerException("Need al!");
		}
		Listener l = get(listenerId);
		if (l.getChannel() instanceof PollChannel) {
			AtomLink link = new AtomLink();
			link.setRel(AppConstants.REL_POLL);
			try {
				link.setHref(baseUri.toString() + "listener/poll/" + URLEncoder.encode(ciId, "UTF-8") + "/"
						+ al.getUniqueCode() + "/" + listenerId);
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
	 * Interface for providing AL Listener registration details obtained during
	 * listener attaching process.
	 */
	static interface ALListenerAttacher extends ListenerAttacher<com.ibm.di.api.remote.AssemblyLineListener> {

		/**
		 * Obtains the AL created during attachment process.
		 * 
		 * @return
		 */
		public AssemblyLine getAL();
	}
}
