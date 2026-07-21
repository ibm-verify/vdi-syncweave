package com.ibm.di.test.tp;

import java.util.HashMap;
import java.util.Map;

import com.ibm.di.api.DIException;
import com.ibm.di.api.connection.IServerAPIConnection;
import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.test.http.WinkHttpClientContext;
import com.ibm.di.tp.server.TPServerApplication;
import com.ibm.di.tp.server.config.TPServerConfig;
import com.ibm.di.tp.server.context.TPServerContext;
import com.ibm.di.tp.server.context.impl.ConcurrentTPServerContext;
import com.ibm.di.web.common.internal.wink.AtomServiceDocEnabler;

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
public class UnitTestTPClientContext extends WinkHttpClientContext {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ConcurrentTPServerContext tpCtx;
	private TPServerConfig tpCfg;

	private MockServerAPIConnectionServiceImpl connSrvc;

	protected final TpAppHelper app = new TpAppHelper(this);

	public UnitTestTPClientContext() {
		super(TPServerApplication.class, AtomServiceDocEnabler.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.test.http.WinkHttpClientContext#preInitHook()
	 */
	@Override
	protected void preInitHook() {
		// make sure IServerApiConnectionService is present
		getServerAPIConnectionService();
		super.preInitHook();
	}

	private TPServerContext getTPServerContext() {
		if (tpCtx == null) {
			tpCtx = new ConcurrentTPServerContext();
			servletConfig.getServletContext().setAttribute(TPServerContext.class.getCanonicalName(), tpCtx);
		}
		return tpCtx;
	}

	protected TPServerConfig getTPServerConfig() {
		if (tpCfg == null) {
			tpCfg = new TPServerConfig();
			if (tpCfg.getTemplateConfig().getBaseTemplate() == null) {
				tpCfg.getTemplateConfig().setBaseTemplate("resources/tp/server/TouchpointTemplate.xml");
			}

			getTPServerContext().setAttribute(TPServerConfig.class.getCanonicalName(), tpCfg);
		}

		return tpCfg;
	}

	protected void setServerAPIConnection(IServerAPIConnection conn) {
		getServerAPIConnectionService().map.put(null, conn);
	}

	protected void setServerAPIConnection(String host, int port, IServerAPIConnection conn) {
		getServerAPIConnectionService().map.put(concateHostAndPort(host, port), conn);
	}

	protected MockServerAPIConnectionServiceImpl getServerAPIConnectionService() {
		if (connSrvc == null) {
			connSrvc = new MockServerAPIConnectionServiceImpl();
			// the app is expecting the service to be in the servletContext
			servletConfig.getServletContext().setAttribute(IServerAPIConnectionService.class.getCanonicalName(), connSrvc);
		}

		return connSrvc;
	}

	private static class MockServerAPIConnectionServiceImpl implements IServerAPIConnectionService {

		private Map<String, IServerAPIConnection> map = new HashMap<String, IServerAPIConnection>();

		public IServerAPIConnection getConnection(String host, int port) throws DIException {
			return map.get(concateHostAndPort(host, port));
		}

		public IServerAPIConnection getConnection() throws DIException {
			return map.get(null);
		}

		public boolean isConnectionLocal(IServerAPIConnection conn) {
			return true;
		}

	}

	private static String concateHostAndPort(String host, int port) {
		if (host == null) {
			throw new IllegalArgumentException("Host is null");
		}

		if (port >= 0) {
			host += ":" + port;
		}
		return host;
	}
}
