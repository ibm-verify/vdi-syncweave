/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.impl.tdi;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.TPServerApplication;
import com.ibm.di.tp.server.model.TouchpointDestination;
import com.ibm.di.tp.server.model.TouchpointInstance;
import com.ibm.di.tp.server.model.TouchpointRole;
import com.ibm.di.tp.server.model.TouchpointType;
import com.ibm.di.tp.server.model.config.DestinationData;
import com.ibm.di.tp.server.model.config.EnumAdminState;
import com.ibm.di.tp.server.model.config.EnumOpState;
import com.ibm.di.tp.server.model.config.InstanceData;
import com.ibm.di.tp.server.model.config.StatusData;
import com.ibm.di.tp.server.model.config.TouchpointStatus;
import com.ibm.di.tp.server.model.exception.ErrorCode;
import com.ibm.di.tp.server.model.exception.SCMPException;
import com.ibm.di.tp.server.util.TDIUtils;

/**
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TouchpointInstanceImpl implements TouchpointInstance {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	private final String id;
	private final TouchpointTypeImpl tt;

	private final String ciRunName;
	private final String reqInUrl;
	private final TouchpointRole role;
	private InstanceData cfg;

	/** synchronized list of objects */
	private final List<TouchpointDestination> destinations;
	/** unmodifiable view of the unsync'd list of objects */
	private final List<TouchpointDestination> destView;

	private final Object cfgLock = new Object();

	/**
	 * @param instId
	 * @param role
	 * @param cfg
	 * @param touchpointTypeImpl
	 * @throws Exception
	 */
	public TouchpointInstanceImpl(String instId, TouchpointRole role, InstanceData cfg, TouchpointTypeImpl touchpointTypeImpl)
			throws SCMPException {
		this.id = instId;
		this.tt = touchpointTypeImpl;

		// check if the requested role is supported by the provided template
		if (!touchpointTypeImpl.getSupportedRoles().contains(role)) {
			throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, ServerActivator.L10N.getString(
					"TP.SERVER.RESOURCE.NO.AL.FOR.TP.ROLE", new Object[] { role.toString() }), -1);
		}

		this.role = role;
		this.ciRunName = TDIUtils.getTPInstanceRunNameFor(tt.getId(), getId());
		this.reqInUrl = role == TouchpointRole.PROVIDER || role == TouchpointRole.INTERMEDIARY ? tt.getConnectivityProviderImpl()
				.getTDIHttpServerUrl()
				+ ciRunName + "/" + ConnectivityProviderImpl.getConfigLoader().getAlNameForRole(role) : null;
		if (hasDestinations()) {
			List<TouchpointDestination> dests = new LinkedList<TouchpointDestination>();
			this.destinations = Collections.synchronizedList(dests);
			this.destView = Collections.unmodifiableList(dests);
		} else {
			this.destinations = null;
			this.destView = null;
		}
		setConfiguration(cfg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.model.TouchpointInstance#getId()
	 */
	public String getId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.model.TouchpointInstance#getRole()
	 */
	public TouchpointRole getRole() {
		return role;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.model.TouchpointInstance#createDestination(com.ibm
	 * .di.tp.server.model.config.DestinationData)
	 */
	public TouchpointDestination createDestination(DestinationData cfg) throws SCMPException {
		if (!hasDestinations()) {
			throw new IllegalStateException();
		}

		TouchpointDestination td = new TouchpointDestinationImpl(cfg, this);
		destinations.add(td);
		try {
			destinationChanged();
		} catch (Exception e) {
			throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, e.getMessage(), -1, e);
		}

		return td;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.model.TouchpointInstance#deleteDestination(com.ibm
	 * .di.tp.server.model.TouchpointDestination)
	 */
	public void deleteDestination(TouchpointDestination dest) throws SCMPException {
		if (hasDestinations()) {
			destinations.remove(dest);
			try {
				destinationChanged();
			} catch (Exception e) {
				throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, e.getMessage(), -1, e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.model.TouchpointInstance#getDestinations()
	 */
	public Collection<TouchpointDestination> getDestinations() {
		return destView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.model.TouchpointInstance#getConfiguration()
	 */
	public InstanceData getConfiguration() {
		synchronized (cfgLock) {
			// not acting on the object but still avoiding object caching by
			// threads
			return cfg;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.model.TouchpointInstance#setConfiguration(com.ibm
	 * .di.tp.server.model.config.InstanceData)
	 */
	public void setConfiguration(InstanceData cfg) throws SCMPException {
		if (cfg == null) {
			throw new NullPointerException();
		}

		synchronized (cfgLock) {
			try {
				if (this.cfg != null && this.cfg.getTouchpoint().getAdminState() == EnumAdminState.ENABLED) {
					// the TP is running. Stop it before re-configuring.
					stopTouchpoint();
				}

				this.cfg = cfg;
				startTouchpoint();
			} catch (Exception e) {
				throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, e.getMessage(), -1, e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.model.TouchpointInstance#getTouchpointType()
	 */
	public TouchpointType getTouchpointType() {
		return tt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.model.TouchpointInstance#getState()
	 */
	public StatusData getStatus() {
		StatusData status = new StatusData();
		status.setTouchpointStatus(new TouchpointStatus());
		status.getTouchpointStatus().setRequestIn(reqInUrl);

		try {
			ConfigInstance ci = getConfigInstance();
			if (ci != null) {
				TemplateConfigLoader cl = ConnectivityProviderImpl.getConfigLoader();

				if (!cl.isAlActiveForRole(role)) {
					// if the ci exist and the tp is provider - the handler AL
					// is not started, so we consider it active always.
					status.getTouchpointStatus().setOpState(EnumOpState.AVAILABLE);
				} else {
					// when the tp is a requestor we have far more choices...
					status.getTouchpointStatus().setOpState(
							TDIUtils.isAssemblyLineActive(ci, cl.getAlNameForRole(role)) ? EnumOpState.AVAILABLE
									: EnumOpState.UNAVAILABLE);
				}
			} else {
				// mising configuartions probably..
				status.getTouchpointStatus().setOpState(EnumOpState.UNAVAILABLE);
			}
		} catch (RemoteException e) {
			status.getTouchpointStatus().setOpState(EnumOpState.UNAVAILABLE);
			TPServerApplication.getLog().warn(e.getMessage(), e);
		} catch (DIException e) {
			status.getTouchpointStatus().setOpState(EnumOpState.UNAVAILABLE);
			TPServerApplication.getLog().warn(e.getMessage(), e);
		}

		return status;
	}

	private boolean hasDestinations() {
		return role == TouchpointRole.INITIATOR || role == TouchpointRole.INTERMEDIARY;
	}

	// sync'd by the Server API
	void stopTouchpoint() throws RemoteException, DIException {
		ConfigInstance ci = getConfigInstance();
		if (ci != null) {
			ci.stop(true);
		}
	}

	/**
	 * Starts only if {@link #readyToStart()} returns true.
	 * 
	 * @throws Exception
	 */
	// sync'd by the Server API
	void startTouchpoint() throws Exception {
		if (readyToStart()) {
			ConfigInstance ci = startConfigInstance();
			startAssemblyLine(ci);
		}
	}

	/**
	 * @return true if the TP instance has all the details needed to start and
	 *         if it is enabled
	 */
	boolean readyToStart() {
		synchronized (cfgLock) {
			return cfg.getTouchpoint().getAdminState() == EnumAdminState.ENABLED && (!hasDestinations() || destinations.size() > 0);
		}
	}

	// sync'd by the Server API
	private ConfigInstance getConfigInstance() throws RemoteException, DIException {
		ConnectivityProviderImpl cp = tt.getConnectivityProviderImpl();
		Session s = cp.getSession();

		return s.getConfigInstance(ciRunName);
	}

	// sync'd by the Server API
	private ConfigInstance startConfigInstance() throws Exception {
		ConnectivityProviderImpl cp = tt.getConnectivityProviderImpl();
		Session s = cp.getSession();
		TemplateConfigLoader cl = ConnectivityProviderImpl.getConfigLoader();

		String config = cl.getTouchpointConfig(tt.getConfigTemplate(), cfg.getTouchpoint().getPropertySheet(), role, tt, cp
				.getTypeLocator());
		ConfigInstance ci = s.startTempConfigInstance(config, true, ciRunName, null);
		if (hasDestinations() && destinations.size() > 0) {
			cl.sendDestinationsToTouchpoint(destinations, ci);
		}
		return ci;
	}

	// sync'd by the Server API
	private void startAssemblyLine(ConfigInstance ci) throws RemoteException, DIException {
		TemplateConfigLoader cl = ConnectivityProviderImpl.getConfigLoader();
		String alName = cl.getAlNameForRole(role);

		if (cl.isAlActiveForRole(role) && !TDIUtils.isAssemblyLineActive(ci, alName)) {
			// we start the al only when it is for a initiator TP, otherwise the
			// ProviderServer AL will handle the starting of the AL(s) for other
			// roles.
			ci.startAssemblyLine(alName);
		}
	}

	// sync'd by the Server API
	boolean isTouchpointRunning() throws RemoteException, DIException {
		return getConfigInstance() != null;
	}

	void destinationChanged() throws Exception {
		boolean enabled = false;
		synchronized (cfgLock) {
			enabled = this.cfg.getTouchpoint().getAdminState() == EnumAdminState.ENABLED;
		}

		if (enabled) {
			// We are only handling the case when the TP is running or is
			// about to start.
			ConfigInstance ci = getConfigInstance();

			// the TP is running.
			if (ci != null) {
				// no destinations left... the TP should not be running
				if (destinations.size() == 0) {
					ci.stop(true);
				}
				// there are destinations which should be send out to the
				// running TP
				else {
					ConnectivityProviderImpl.getConfigLoader().sendDestinationsToTouchpoint(destinations, ci);
				}
			}
			// the TP is stopped
			else if (destinations.size() > 0) {
				// the TP has not been running till now because a
				// destination was missing - start it
				startTouchpoint();
			}
		}
	}
}
