package com.ibm.di.test.rest;

import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;

import com.ibm.di.api.DIException;
import com.ibm.di.api.connection.IServerAPIConnection;
import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.test.http.WinkHttpClientContext;
import com.ibm.di.web.common.internal.wink.AtomServiceDocEnabler;

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
public class UnitTestRestClientContext extends WinkHttpClientContext {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IServerAPIConnection conn;

	private IServerAPIConnectionService connSrvc = new IServerAPIConnectionService() {

		public IServerAPIConnection getConnection() throws DIException {
			return conn;
		}

		public IServerAPIConnection getConnection(String host, int port) throws DIException {
			throw new UnsupportedOperationException();
		}

		public boolean isConnectionLocal(IServerAPIConnection conn) {
			return true;
		}
	};

	protected RestAppHelper app = new RestAppHelper(this);

	public UnitTestRestClientContext() {
		super(com.ibm.di.api.rest.internal.RestApplication.class, AtomServiceDocEnabler.class);
	}

	public UnitTestRestClientContext(Class<? extends Application> app, Class<? extends ResourceConfig> dep) {
		super(app, dep);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		servletConfig.getServletContext().setAttribute(IServerAPIConnectionService.class.getCanonicalName(), connSrvc);
	}

	/**
	 * Sets the connection and automatically initializies the context.
	 * 
	 * @param conn
	 * @throws ServletException
	 */
	protected void setIServerAPIConnection(IServerAPIConnection conn) throws ServletException {
		this.conn = conn;
	}

	protected IServerAPIConnection getIServerAPIConnection() {
		return this.conn;
	}
}
