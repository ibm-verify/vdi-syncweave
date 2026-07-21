package com.ibm.di.test.utils;

import java.lang.reflect.Field;

import com.ibm.di.server.Log;
import com.ibm.di.server.RS;
import com.ibm.di.server.ReconnectRuleEngine;

public class RSMock extends RS {
	private Log log = new NOOPLog();

	public RSMock() throws Exception {
		super();
		/*
		 * For a lack of a better way, use reflection to set a
		 * ReconnectRuleEngine for the JVM.
		 */
		if (RS.getReconnectRuleEngine() == null) {
			Field rreField = RS.class.getDeclaredField("reconnectRuleEngine");
			rreField.setAccessible(true);
			rreField.set(null, new ReconnectRuleEngine(log));
			rreField.setAccessible(false);
		}
	}

	@Override
	public Log getLog() {
		return log;
	}
}
