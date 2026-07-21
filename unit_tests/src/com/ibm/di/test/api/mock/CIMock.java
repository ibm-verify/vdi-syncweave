package com.ibm.di.test.api.mock;

import org.easymock.EasyMock;

import com.ibm.di.api.local.ConfigInstance;

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
public class CIMock {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	ConfigInstance ciMock;

	public CIMock() {
		ciMock = EasyMock.createMock(ConfigInstance.class);
	}

	/**
	 * @param cleanConfigId
	 * @param serverAPIMockBuilder
	 */
	CIMock(String cleanConfigId, ServerAPIMockBuilder serverAPIMockBuilder) {
	}

	public void verifyMockCalls() {
		EasyMock.verify(ciMock);
	}

	public void activateMocks() throws Exception {
		EasyMock.replay(ciMock);
	}

	public void resetMockCalls() {
		EasyMock.reset(ciMock);
	}

}
