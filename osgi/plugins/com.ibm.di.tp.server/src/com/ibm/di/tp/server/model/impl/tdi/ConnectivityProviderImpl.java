/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.impl.tdi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.naming.ServiceUnavailableException;

import com.ibm.di.api.DIException;
import com.ibm.di.api.connection.IServerAPIConnection;
import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.remote.SessionFactory;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.TPServerApplication;
import com.ibm.di.tp.server.config.node.TdiNodeConfig;
import com.ibm.di.tp.server.context.TPServerContext;
import com.ibm.di.tp.server.model.ConnectivityProvider;
import com.ibm.di.tp.server.model.TouchpointInstance;
import com.ibm.di.tp.server.model.TouchpointType;
import com.ibm.di.tp.server.model.exception.ErrorCode;
import com.ibm.di.tp.server.model.exception.SCMPException;
import com.ibm.di.tp.server.util.TDIUtils;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class ConnectivityProviderImpl implements ConnectivityProvider {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * This is the prefix of the name the CI handling the client requests will
	 * have. The complete name is composed by this prefix and the port the serve
	 * will listen on.
	 */
	public static final String CI_PROVIDER_SERVER_PREFIX = TemplateConfigLoader.AL_PROVIDER_SERVER + "_";

	private static final TemplateConfigLoader cfgLoader = new TemplateConfigLoader();

	private final TdiNodeConfig cfg;

	/**
	 * this is the name of the config instance that will be receiving the client
	 * requests and will be dispatching them to the configured AL.
	 */
	private final String reqDispatcherCiRunName;

	/**
	 * this is the port the request dispatcher server connector will be
	 * listening to
	 */
	private final int reqDispatcherSrvConnPort;

	private Session session;

	private final String tdiHttpServerUrl;
	private final TouchpointTypeLocator typeLocator;

	private final TPServerContext ctx;

	/**
	 * @param cfg
	 * @param ctx
	 * @throws Exception
	 */
	public ConnectivityProviderImpl(TdiNodeConfig cfg, TPServerContext ctx) throws SCMPException {
		this.cfg = cfg;
		this.ctx = ctx;

		// interpret the cfg parameters
		if (cfg.getProviderPort() == null) {
			cfg.setProviderPort(1097);
		}

		if (cfg.getProviderHost() == null) {
			if (cfg.getHost() != null) {
				cfg.setProviderHost(cfg.getHost());
			} else {
				throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, ServerActivator.L10N.getString(
						"TP.SERVER.CONFIG.MISSING.PARAMETER", "providerHost"), 500);
			}
		}

		this.reqDispatcherSrvConnPort = cfg.getProviderPort();
		this.reqDispatcherCiRunName = CI_PROVIDER_SERVER_PREFIX + reqDispatcherSrvConnPort;
		this.tdiHttpServerUrl = "http://" + cfg.getProviderHost() + ":" + reqDispatcherSrvConnPort + "/";

		try {
			typeLocator = new TouchpointTypeLocator(ctx, this);
		} catch (DIException e1) {
			throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, e1.getMessage(), 500, e1);
		}

		try {
			checkHttpServerAL(typeLocator.getTypes());
		} catch (RemoteException e) {
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("CONN.PROVIDER.ERROR.CREATING.SESSION", cfg.getId()),
					e);
		} catch (Exception e) {
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("CONN.PROVIDER.ERROR.IN.COMMUNICATION", cfg.getId()),
					e);
		}
	}

	/**
	 * @throws Exception
	 */
	private void checkHttpServerAL(Collection<TouchpointType> types) throws Exception {
		Session s = getSession();

		synchronized (this) {
			ConfigInstance psCi = s.getConfigInstance(reqDispatcherCiRunName);
			boolean startAL = true;

			if (psCi == null) {
				// the provider server AL is not running.
				String mc = getConfigLoader().getProviderServerConfig(typeLocator.getConfigTemplateForType(null),
						reqDispatcherSrvConnPort);
				psCi = s.startTempConfigInstance(mc, true, reqDispatcherCiRunName, null);

				// when starting the ci we also need to see whether there
				// are TP Instances that need to be started... this is only
				// done the first time or when the provider server ci is being
				// restarted by an external application

				// we first start the listener ALs - providers
				List<TouchpointInstanceImpl> providers = new LinkedList<TouchpointInstanceImpl>();
				// next we start the intermediaries
				List<TouchpointInstanceImpl> intermediaries = new LinkedList<TouchpointInstanceImpl>();
				// finally the initiators which will start spitting data once
				// brought up
				List<TouchpointInstanceImpl> initiators = new LinkedList<TouchpointInstanceImpl>();

				// first split the big pile into three small piles
				for (TouchpointType tt : types) {
					Collection<TouchpointInstance> tpInsts = tt.getInstances();
					for (TouchpointInstance ti : tpInsts) {
						switch (ti.getRole()) {
						case PROVIDER:
							providers.add((TouchpointInstanceImpl) ti);
							break;
						case INITIATOR:
							initiators.add((TouchpointInstanceImpl) ti);
							break;
						case INTERMEDIARY:
							intermediaries.add((TouchpointInstanceImpl) ti);
							break;
						}
					}
				}

				startTouchpointInstances(providers);
				startTouchpointInstances(initiators);
				startTouchpointInstances(intermediaries);
			}

			// the configuration might have the AL start automatically.
			startAL = !TDIUtils.isAssemblyLineActive(psCi, TemplateConfigLoader.AL_PROVIDER_SERVER);

			if (startAL) {
				psCi.startAssemblyLine(TemplateConfigLoader.AL_PROVIDER_SERVER);
			}
		}
	}

	private static void startTouchpointInstances(List<TouchpointInstanceImpl> instances) throws Exception {
		for (TouchpointInstanceImpl ti : instances) {
			if (ti.readyToStart() && !ti.isTouchpointRunning()) {
				ti.startTouchpoint();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.model.ConnectivityProvider#getTypes()
	 */
	public Collection<TouchpointType> getTypes() {
		Collection<TouchpointType> types = null;
		try {
			types = typeLocator.getTypes();
			checkHttpServerAL(types);
		} catch (RemoteException e) {
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("CONN.PROVIDER.ERROR.CREATING.SESSION", cfg.getId()),
					e);
		} catch (Exception e) {
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("CONN.PROVIDER.ERROR.IN.COMMUNICATION", cfg.getId()),
					e);
		} finally {
			if (types == null) {
				types = new LinkedList<TouchpointType>();
			}
		}
		return types;
	}

	/**
	 * @return
	 * @throws DIException
	 * @throws RemoteException
	 */
	Session getSession() throws RemoteException, DIException {
		synchronized (this) {
			if (session == null) {
				IServerAPIConnectionService connSrvc = (IServerAPIConnectionService) ctx
						.getAttribute(IServerAPIConnectionService.class.getCanonicalName());

				if (connSrvc == null) {
					throw new RuntimeException(
							new ServiceUnavailableException(IServerAPIConnectionService.class.getCanonicalName()));
				}

				IServerAPIConnection apiConn = cfg.isLocal() ? connSrvc.getConnection() : connSrvc.getConnection(cfg.getHost(), cfg
						.getPort());

				if (apiConn == null) {
					throw new RemoteException(ServerActivator.L10N.getString("SERVER.API.CONNECTION.ERROR.CONNECTING.TO.SERVER",
							getId()));
				}

				SessionFactory sf;
				try {
					sf = apiConn.getSessionFactory();
				} catch (NotBoundException e) {
					throw new RemoteException(e.getMessage(), e);
				}
				String user = null;
				String pass = null;

				if (cfg.getUser() != null && (user = cfg.getUser().trim()).length() == 0) {
					user = null;
				}

				if (user != null) {
					if (cfg.getPassword().getValue() == null || (pass = cfg.getPassword().getValue().trim()).length() == 0) {
						pass = "";
					}
					session = sf.createSession(user, pass);
				} else {
					session = sf.createSession();
				}
			}
			return session;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.model.ConnectivityProvider#getId()
	 */
	public String getId() {
		return this.cfg.getId();
	}

	/**
	 * @return the url of the http server - e.g. "http://tdi_host:http_port/".
	 *         This ends with a slash denoting the root context.
	 */
	String getTDIHttpServerUrl() {
		return tdiHttpServerUrl;
	}

	/**
	 * @return the cfgloader
	 */
	public static TemplateConfigLoader getConfigLoader() {
		return cfgLoader;
	}

	/**
	 * @return
	 */
	public TouchpointTypeLocator getTypeLocator() {
		return typeLocator;
	}
}
