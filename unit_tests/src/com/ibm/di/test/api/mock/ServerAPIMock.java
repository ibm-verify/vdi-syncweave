package com.ibm.di.test.api.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.easymock.EasyMock;

import com.ibm.di.api.DIException;
import com.ibm.di.api.connection.internal.LocalServerAPIConnection;
import com.ibm.di.api.connection.internal.proxy.ApiAdapter;
import com.ibm.di.api.local.ServerInfo;
import com.ibm.di.api.local.Session;
import com.ibm.di.api.local.SessionFactory;

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
public class ServerAPIMock extends LocalServerAPIConnection {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	protected com.ibm.di.api.local.SessionFactory sfMock;
	protected Session sMock;
	protected ServerInfo siMock;

	private final ServerAPIMock reuse;

	/**
	 * Creates a mock which obtains a session factory only.
	 * 
	 * The caller is responsible for calling the {@link #defineMockCalls()} so
	 * the session and server info mocks get defined.
	 */
	public ServerAPIMock(ServerAPIMock reuse) throws Exception {
		super(reuse != null ? (com.ibm.di.api.remote.SessionFactory) reuse.getSessionFactory() : (SessionFactory) EasyMock
				.createMock(SessionFactory.class));

		if (reuse == null) {
			this.reuse = null;
			sfMock = getSFMock();
			sMock = EasyMock.createMock(Session.class);
			siMock = EasyMock.createMock(ServerInfo.class);
			EasyMock.expect(sfMock.createSession((String) EasyMock.anyObject(), (String) EasyMock.anyObject()))
					.andStubReturn(sMock);
			EasyMock.expect(sfMock.createSession()).andStubReturn(sMock);
			EasyMock.expect(sMock.getServerInfo()).andStubReturn(siMock);
		} else {
			this.reuse = reuse;
			sfMock = reuse.sfMock;
			sMock = reuse.sMock;
			siMock = reuse.siMock;
		}
	}

	public AftermathAccessor verifyMockCalls() {
		if (reuse != null) {
			return reuse.verifyMockCalls();
		} else {
			EasyMock.verify(sfMock, sMock, siMock);
			return new AftermathAccessor();
		}
	}

	/**
	 * Activates the session/session factory mocks of this object. This is
	 * called right after creation in order for the mock objects to be
	 * activated. Subclasses could override this method to extend the behavior
	 * of the mock objects, but they should always call {@link
	 * super#activateMocks()} when done.
	 * 
	 * @throws Exception
	 */
	public void activateMocks() throws Exception {
		if (reuse != null) {
			reuse.activateMocks();
		} else {
			EasyMock.replay(sfMock, sMock, siMock);
		}
	}

	public void resetMockCalls() {
		if (reuse != null) {
			reuse.resetMockCalls();
		} else {
			EasyMock.reset(sfMock, sMock, siMock);
		}
	}

	private com.ibm.di.api.local.SessionFactory getSFMock() {
		// get local sf instance out of the invocation handler using reflection
		InvocationHandler ih = null;
		try {
			ih = Proxy.getInvocationHandler(getSessionFactory());
		} catch (DIException e) {
			e.printStackTrace();
		}

		if (ih == null || !(ih instanceof ApiAdapter)) {
			throw new RuntimeException("Error Obtaining InvocationHandler for SF Proxy.");
		}

		Object localSf = null;
		try {
			localSf = ((ApiAdapter) ih).getAdaptedInstance();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		if (localSf == null) {
			throw new RuntimeException("Could Not get the localSf field.");
		}

		return (SessionFactory) localSf;
	}
}
