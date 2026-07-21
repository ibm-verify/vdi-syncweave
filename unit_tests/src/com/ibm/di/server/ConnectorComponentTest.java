
package com.ibm.di.server;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.junit.Test;

import com.ibm.di.config.base.AssemblyLineConfigImpl;
import com.ibm.di.config.base.ConnectorConfigImpl;
import com.ibm.di.config.base.LinkCriteriaConfigImpl;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.ReconnectRuleConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.entry.Entry;
import com.ibm.di.test.utils.NOOPLog;
import com.ibm.di.util.HooksUtil;

/**
 * Tests for AssemblyLineComponent wrapping a Connector.
 * TODO: cover all modes	
 * TODO: test Input Maps & Output Maps
 */
public class ConnectorComponentTest {
	
	public static final List<String> TESTED_MODES = Arrays.asList(ConnectorConfig.ITERATOR_MODE, ConnectorConfig.ADDONLY_MODE, ConnectorConfig.LOOKUP_MODE);

	@Test
	public void test_NormalAssemblyLineExecution() throws Exception {
		for (String mode : TESTED_MODES) {
			
			// no iterations
			test_Execution(mode, 0);
			
			// single iteration
			test_Execution(mode, 1);
			
			// multiple iterations
			test_Execution(mode, 10);
		}
	}

	private void test_Execution(String connectorMode, int iterationCount) throws Exception {
		ConnectorTestCase test = new ConnectorTestCase(connectorMode);
		test.setIterationCount(iterationCount);

		test.execute();

		assertEquals(1, test.getConnector().getCallCount("initialize"));
		int expectedCalls = iterationCount;
		if (ConnectorConfig.ITERATOR_MODE.equals(connectorMode)) {
			// getNextEntry is called one more time - when it returns null
			++expectedCalls;
		}
		assertEquals(expectedCalls, test.getConnector().getCallCount(getDefaultOperationForMode(connectorMode)));
	}

	@Test
	public void test_Hook_Invocation_Sequence_For_Normal_Execution() throws Exception {
		for (String mode : TESTED_MODES) {
			
			// no iterations
			test_Hook_Invocation_Sequence_For_Normal_Execution(mode, 0);
			
			// single iteration
			test_Hook_Invocation_Sequence_For_Normal_Execution(mode, 1);
			
			// multiple iterations
			test_Hook_Invocation_Sequence_For_Normal_Execution(mode, 2);
		}
	}

	private void test_Hook_Invocation_Sequence_For_Normal_Execution(String connectorMode, int iterationCount) throws Exception {
		ConnectorTestCase test = new ConnectorTestCase(connectorMode);
		test.setIterationCount(iterationCount);
		test.setRecordEvents(true);
		test.execute();
	}

	@Test
	public void test_Hook_Invocation_Sequence_With_OverrideOperation_Hook()
			throws Exception {
		for (String mode : TESTED_MODES) {
			
			// no iterations
			test_Hook_Invocation_Sequence_With_OverrideOperation_Hook(mode, 0);
			
			// single iteration
			test_Hook_Invocation_Sequence_With_OverrideOperation_Hook(mode, 1);
			
			// multiple iterations
			test_Hook_Invocation_Sequence_With_OverrideOperation_Hook(mode, 2);
		}
	}

	private void test_Hook_Invocation_Sequence_With_OverrideOperation_Hook(String connectorMode, int iterationCount)
			throws Exception {
		ConnectorTestCase test = new ConnectorTestCase(connectorMode);
		test.setIterationCount(iterationCount);
		test.setRecordEvents(true);
		test.setOverrideDefaultOperationWithHook(true);
		test.execute();
	}

	@Test
	public void test_Hooks_Are_Not_Invoked_When_Disabled_For_Iterator_Connector() throws Exception {
		ConnectorTestCase test = new ConnectorTestCase(ConnectorConfig.ITERATOR_MODE);
		test.setIterationCount(3);
		/*
		 * Event recording will inject script into each hook, so we don't need
		 * to add hooks manually for this test - just need to disable a few of
		 * them
		 */
		test.setRecordEvents(true);

		String[] disabledHooks = { "before_initialize", "before_getnext", "before_selectEntries", "before_execute", "before_close" };

		for (String hook : disabledHooks) {
			test.getConnectorConfig().getHooks().getHook(hook).setEnabled(false);
		}

		test.execute();
	}

	@Test
	public void test_No_Reconnect_If_Reconnect_Is_Not_Configured_For_Error_In_SelectEntries_For_Iterator_Connector()
			throws Exception {
		test_No_Reconnect_If_Reconnect_Is_Not_Configured(ConnectorConfig.ITERATOR_MODE, "selectEntries");
	}
	
	@Test
	public void test_No_Reconnect_If_Reconnect_Is_Not_Configured_For_Error_In_Initialize()
			throws Exception {
		for (String mode : TESTED_MODES) {
			test_No_Reconnect_If_Reconnect_Is_Not_Configured(mode, "initialize");
		}
	}

	@Test
	public void test_No_Reconnect_If_Reconnect_Is_Not_Configured_For_Error_In_DefaultOperation()
			throws Exception {
		for (String mode : TESTED_MODES) {
			final String op = getDefaultOperationForMode(mode);
			test_No_Reconnect_If_Reconnect_Is_Not_Configured(mode, op);
		}
	}

	private void test_No_Reconnect_If_Reconnect_Is_Not_Configured(String connectorMode, String operationName) throws Exception {
		ConnectorTestCase test = new ConnectorTestCase(connectorMode);
		test.setThrowOnOperation(operationName, 1);
		test.setExpectExecutionError(true);

		test.execute();

		assertEquals(1, test.getConnector().getCallCount(operationName));
	}

	@Test
	public void test_No_Reconnect_For_Error_In_Initialize_If_Only_AutoReconnect_Is_Configured() throws Exception {
		for (String mode : TESTED_MODES) {
			test_No_Reconnect_For_Error_If_Reconnect_Is_Enabled_But_Not_For_The_Operation_That_Failed(
					mode, "initialize");
		}
	}

	@Test
	public void test_No_Reconnect_For_Error_In_SelectEntries_If_Only_AutoReconnect_Is_Configured_For_Iterator_Connector()
			throws Exception {
		test_No_Reconnect_For_Error_If_Reconnect_Is_Enabled_But_Not_For_The_Operation_That_Failed(ConnectorConfig.ITERATOR_MODE,
				"selectEntries");
	}
	
	@Test
	public void test_No_Reconnect_For_Error_In_Default_Operation_If_Reconnect_Is_Enabled_But_Not_For_The_Operation_That_Failed() throws Exception {
		for (String mode : TESTED_MODES) {
			final String op = getDefaultOperationForMode(mode);
			test_No_Reconnect_For_Error_If_Reconnect_Is_Enabled_But_Not_For_The_Operation_That_Failed(mode, op);
		}
	}

	private void test_No_Reconnect_For_Error_If_Reconnect_Is_Enabled_But_Not_For_The_Operation_That_Failed(String connectorMode,
			String operationName) throws Exception {
		final boolean isPrologOperation = isPrologOperation(operationName);
		// make sure we don't have reconnect enabled for the particular
		// operation that is going to fail
		final boolean reconnectOnInitialize = !isPrologOperation;
		final boolean reconnectOnOperation = isPrologOperation;
		final int reconnectAttempts = 10;

		ConnectorTestCase test = new ConnectorTestCase(connectorMode);
		test.setReconnectRetries(reconnectAttempts);
		test.setReconnectOnInitialize(reconnectOnInitialize);
		test.setReconnectOnOperation(reconnectOnOperation);
		test.setThrowOnOperation(operationName, 1);
		test.setExpectExecutionError(true);

		test.execute();

		assertEquals(1, test.getConnector().getCallCount(operationName));
	}

	@Test
	public void test_Reconnect_Succeeds_If_NumberOfRetries_Is_Enough_For_Error_In_Initialize() throws Exception {
		for (String mode : TESTED_MODES) {
			test_Reconnect_Succeeds_If_NumberOfRetries_Is_Enough_For_Error_In_Initialize(mode);
		}
	}

	@Test
	public void test_Reconnect_For_Error_In_SelectEntries_For_Iterator_Connector() throws Exception {
		test_Reconnect_Succeeds_If_NumberOfRetries_Is_Enough_For_Error_In_Operation(ConnectorConfig.ITERATOR_MODE, "selectEntries");
	}

	@Test
	public void test_Reconnect_For_Error_In_Default_Operation() throws Exception {
		for (String mode : TESTED_MODES) {
			final String op = getDefaultOperationForMode(mode);
			test_Reconnect_Succeeds_If_NumberOfRetries_Is_Enough_For_Error_In_Operation(mode, op);
		}
	}

	private void test_Reconnect_Succeeds_If_NumberOfRetries_Is_Enough_For_Error_In_Initialize(String connectorMode)
			throws Exception {
		final int timesToThrowInInitialize = 3;
		final int numberOfRetriesForReconnect = timesToThrowInInitialize;
		final boolean expectExecutionError = false;
		test_Reconnect_For_Error_In_Initialize(connectorMode, timesToThrowInInitialize, numberOfRetriesForReconnect,
				expectExecutionError);
	}

	private void test_Reconnect_Fails_If_NumberOfRetries_Is_Exhausted_For_Error_In_Initialize(String connectorMode)
			throws Exception {
		final int timesToThrowInInitialize = 3;
		final int numberOfRetriesForReconnect = timesToThrowInInitialize - 1;
		final boolean expectExecutionError = true;
		test_Reconnect_For_Error_In_Initialize(connectorMode, timesToThrowInInitialize, numberOfRetriesForReconnect,
				expectExecutionError);
	}

	private void test_Reconnect_For_Error_In_Initialize(String connectorMode, int timesToThrowInInitialize,
			int numberOfRetriesForReconnect, boolean expectExecutionError) throws Exception {

		ConnectorTestCase test = new ConnectorTestCase(connectorMode);
		test.setReconnectOnInitialize(true);
		test.setReconnectRetries(numberOfRetriesForReconnect);
		test.setThrowOnOperation("initialize", timesToThrowInInitialize);
		test.setExpectExecutionError(expectExecutionError);

		test.execute();

		/*
		 * For errors in 'initialize', the reconnect procedure invokes
		 * 'initialize' again. For errors in other operations, the reconnect
		 * procedure invokes 'reconnect'.
		 */
		assertEquals(numberOfRetriesForReconnect + 1, test.getConnector().getCallCount("initialize"));
		assertEquals(0, test.getConnector().getCallCount("reconnect"));
	}

	private void test_Reconnect_Succeeds_If_NumberOfRetries_Is_Enough_For_Error_In_Operation(String connectorMode,
			String operationName) throws Exception {
		final int timesToThrowInReconnect = 3;
		final int numberOfRetriesForReconnect = timesToThrowInReconnect + 1;
		final boolean expectExecutionError = false;
		ConnectorTestCase test = test_Reconnect_For_Error_In_Operation(connectorMode, operationName, timesToThrowInReconnect,
				numberOfRetriesForReconnect, expectExecutionError);

		/*
		 * We have done one iteration: 1 operation call fails, reconnect
		 * attempts succeed and the second operation call succeeds.
		 */
		// the first operation call fails
		int expectedOperationCalls = 1;
		/*
		 * After successful reconnect the operation is invoked again
		 * automatically, except for selectEntries. The base Connector
		 * implementation (com.ibm.di.connector.Connector) normally invokes it
		 * in reconnect for Iterators.
		 */
		if (!operationName.equalsIgnoreCase("selectEntries")) {
			++expectedOperationCalls;
		}
		if (operationName.equalsIgnoreCase("getNextEntry")) {
			// getNextEntry is called one extra time - when it returns null.
			++expectedOperationCalls;
		}

		assertEquals(expectedOperationCalls, test.getConnector().getCallCount(operationName));
	}

	private void test_Reconnect_Fails_If_NumberOfRetries_Is_Exhausted_For_Error_In_Operation(String connectorMode,
			String operationName) throws Exception {
		final int timesToThrowInReconnect = 3;
		final int numberOfRetriesForReconnect = timesToThrowInReconnect;
		final boolean expectExecutionError = true;
		ConnectorTestCase test = test_Reconnect_For_Error_In_Operation(connectorMode, operationName, timesToThrowInReconnect,
				numberOfRetriesForReconnect, expectExecutionError);
		assertEquals(1, test.getConnector().getCallCount(operationName));
	}

	private ConnectorTestCase test_Reconnect_For_Error_In_Operation(String connectorMode, String operationName,
			int timesToThrowInReconnect, int numberOfRetriesForReconnect, boolean expectExecutionError) throws Exception {

		ConnectorTestCase test = new ConnectorTestCase(connectorMode);
		test.setReconnectOnInitialize(true);
		test.setReconnectOnOperation(true);
		test.setReconnectRetries(numberOfRetriesForReconnect);
		test.setThrowOnOperation(operationName, 1);
		test.setThrowOnOperation("reconnect", timesToThrowInReconnect);
		test.setExpectExecutionError(expectExecutionError);
		
		test.execute();

		assertEquals(1, test.getConnector().getCallCount("initialize"));
		assertEquals(numberOfRetriesForReconnect, test.getConnector().getCallCount("reconnect"));
		return test;
	}

	@Test
	public void test_Reconnect_Fails_If_NumberOfRetries_Is_Exhausted_For_Error_In_Initialize()
			throws Exception {
		for (String mode : TESTED_MODES) {
			test_Reconnect_Fails_If_NumberOfRetries_Is_Exhausted_For_Error_In_Initialize(mode);
		}
	}

	@Test
	public void test_Reconnect_Fails_If_NumberOfRetries_Is_Exhausted_For_Error_In_SelectEntries_For_Iterator_Connector()
			throws Exception {
		test_Reconnect_Fails_If_NumberOfRetries_Is_Exhausted_For_Error_In_Operation(ConnectorConfig.ITERATOR_MODE, "selectEntries");
	}

	@Test
	public void test_Hook_Invocation_Sequence_For_Error_In_Initialize_For_Iterator_Connector() throws Exception {
		for (String mode : TESTED_MODES) {
			test_Hook_Invocation_Sequence_For_Error_In_Initialize(mode);
		}
	}

	private void test_Hook_Invocation_Sequence_For_Error_In_Initialize(String connectorMode) throws Exception {
		ConnectorTestCase test = new ConnectorTestCase(connectorMode);
		test.setRecordEvents(true);
		test.setThrowOnOperation("initialize", 1);
		test.setExpectExecutionError(true);
		
		test.execute();
	}

	@Test
	public void test_Hook_Invocation_Sequence_For_Failed_Reconnect_For_Error_In_Initialize()
			throws Exception {
		for (String mode : TESTED_MODES) {
			test_Hook_Invocation_Sequence_For_Failed_Reconnect_For_Error_In_Initialize(mode);
		}
	}

	private void test_Hook_Invocation_Sequence_For_Failed_Reconnect_For_Error_In_Initialize(String connectorMode) throws Exception {

		final int timesToThrow = 3;
		final int timesToReconnect = timesToThrow - 1;
		
		ConnectorTestCase test = new ConnectorTestCase(connectorMode);
		test.setReconnectOnInitialize(true);
		test.setRecordEvents(true);
		test.setReconnectRetries(timesToReconnect);
		test.setThrowOnOperation("initialize", timesToThrow);
		test.setExpectExecutionError(true);
		
		test.execute();
	}

	@Test
	public void test_Hook_Invocation_Sequence_For_Successful_Reconnect_For_Error_In_Initialize_For_Iterator_Connector()
			throws Exception {
		test_Hook_Invocation_Sequence_For_Successful_Reconnect_For_Error_In_Initialize(ConnectorConfig.ITERATOR_MODE);
	}

	private void test_Hook_Invocation_Sequence_For_Successful_Reconnect_For_Error_In_Initialize(String connectorMode)
			throws Exception {
		
		final int timesToThrow = 3;
		final int timesToReconnect = timesToThrow;
		
		ConnectorTestCase test = new ConnectorTestCase(connectorMode);
		test.setReconnectOnInitialize(true);
		test.setRecordEvents(true);
		test.setReconnectRetries(timesToReconnect);
		test.setThrowOnOperation("initialize", timesToThrow);
		test.setExpectExecutionError(false);
		
		test.execute();
	}

	@Test
	public void test_Hook_Invocation_Sequence_For_Error_In_SelectEntries_For_Iterator_Connector() throws Exception {
		test_Hook_Invocation_Sequence_For_Error_In_Operation(ConnectorConfig.ITERATOR_MODE, "selectEntries");
	}

	@Test
	public void test_Hook_Invocation_Sequence_For_Error_In_Default_Operation() throws Exception {
		for (String mode : TESTED_MODES) {
			final String op = getDefaultOperationForMode(mode);
			test_Hook_Invocation_Sequence_For_Error_In_Operation(mode, op);
		}
	}

	private void test_Hook_Invocation_Sequence_For_Error_In_Operation(String connectorMode, String operationName) throws Exception {
		ConnectorTestCase test = new ConnectorTestCase(connectorMode);
		test.setReconnectOnOperation(true);
		test.setRecordEvents(true);
		test.setThrowOnOperation(operationName, 1);
		test.setExpectExecutionError(true);
		test.execute();
	}

	@Test
	public void test_Hook_Invocation_Sequence_For_Failed_Reconnect_For_Error_In_SelectEntries_For_Iterator_Connector()
			throws Exception {
		test_Hook_Invocation_Sequence_For_Failed_Reconnect_For_Error_In_Operation(ConnectorConfig.ITERATOR_MODE, "selectEntries");
	}

	@Test
	public void test_Hook_Invocation_Sequence_For_Failed_Reconnect_For_Error_In_Default_Operation()
			throws Exception {
		for (String mode : TESTED_MODES) {
			final String op = getDefaultOperationForMode(mode);
			test_Hook_Invocation_Sequence_For_Failed_Reconnect_For_Error_In_Operation(mode, op);
		}
	}

	private void test_Hook_Invocation_Sequence_For_Failed_Reconnect_For_Error_In_Operation(String connectorMode,
			String operationName) throws Exception {

		final int timesToThrowOnReconnect = 3;
		final int timesToReconnect = timesToThrowOnReconnect;
		
		ConnectorTestCase test = new ConnectorTestCase(connectorMode);
		test.setReconnectOnInitialize(true);
		test.setReconnectOnOperation(true);
		test.setRecordEvents(true);
		test.setReconnectRetries(timesToReconnect);
		test.setThrowOnOperation(operationName, 1);
		test.setThrowOnOperation("reconnect", timesToThrowOnReconnect);
		test.setExpectExecutionError(true);

		test.execute();
	}

	@Test
	public void test_Hook_Invocation_Sequence_For_Successful_Reconnect_For_Error_In_SelectEntries_For_Iterator_Connector()
			throws Exception {
		test_Hook_Invocation_Sequence_For_Successful_Reconnect_For_Error_In_Operation(ConnectorConfig.ITERATOR_MODE,
				"selectEntries");
	}

	@Test
	public void test_Hook_Invocation_Sequence_For_Successful_Reconnect_For_Error_In_Default_Operation()
			throws Exception {
		for (String mode : TESTED_MODES) {
			final String op = getDefaultOperationForMode(mode);
			test_Hook_Invocation_Sequence_For_Successful_Reconnect_For_Error_In_Operation(mode, op);
		}
	}

	private void test_Hook_Invocation_Sequence_For_Successful_Reconnect_For_Error_In_Operation(String connectorMode,
			String operationName) throws Exception {

		final int timesToThrowOnReconnect = 3;
		final int timesToReconnect = timesToThrowOnReconnect + 1;
		
		ConnectorTestCase test = new ConnectorTestCase(connectorMode);
		test.setReconnectOnInitialize(true);
		test.setReconnectOnOperation(true);
		test.setRecordEvents(true);
		test.setReconnectRetries(timesToReconnect);
		test.setThrowOnOperation(operationName, 1);
		test.setThrowOnOperation("reconnect", timesToThrowOnReconnect);
		test.setExpectExecutionError(false);

		test.execute();
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// Utility methods and classes

	/**
	 * Add script to every Connector hook to verify the available script beans.
	 */
	private static void addBeanVerifyingHooks(ConnectorConfig cc, boolean overrideDefaultOperationWithHook) {
		List<String> hookNames = getHookNamesForConnectorMode(cc);
		String overrideHookName = getOverrideHookForOperation(getDefaultOperationForMode(cc.getMode()));
		for (String hook : hookNames) {
			
			if (!overrideDefaultOperationWithHook && hook.equalsIgnoreCase(overrideHookName)) {
				// skip the override hook
				continue;
			}
			
			boolean hookAlreadyExisted = cc.getHooks().getHook(hook, false) != null;
			
			addBeanVerifyingHook(cc.getHooks(), hook);

			if (hook.equalsIgnoreCase("initialize_fail") || hook.equalsIgnoreCase("default_fail")) {
				if (!hookAlreadyExisted) {
					/*
					 * Re-throw the error in the last error hook in the flow,
					 * otherwise we will consume the error and will alter the
					 * flow.
					 */
					appendScriptToHook(cc, hook, "throw error.getObject(\"exception\");");
				}
			}
		}
	}
	
	/**
	 * Add script to the specified hook. The script verifies that beans with the
	 * specified names are available in the hook.
	 */
	private static void addBeanVerifyingHook(HooksConfig hooksConfig, String hookName) {
		
		List<String> beanNames = HookBeanVerificationConnector.getBeansForHook(hookName);
		if (beanNames == null) {
			throw new UnsupportedOperationException("Does not know expected beans for hook "+hookName);
		}
		
		String script = "";
		for (String bean : beanNames) {
			script += "if (typeof "+bean+" == 'undefined') { throw \"Bean "+bean+" is not defined in hook "+hookName+" \"; }";
			script += "thisConnector.connector.verifyHookBean(\"" + hookName + "\", \"" + bean + "\", " + bean + ");";
		}
		addConnectorHook(hooksConfig, hookName, script);
	}

	private static void appendScriptToHook(ConnectorConfig cc, String hookName, String moreScript) {
		boolean createHook = false;
		HookConfig hook = cc.getHooks().getHook(hookName, createHook);
		String newScript = hook.getScript() + moreScript;
		hook.setScript(newScript);
	}

	/**
	 * Add script to every Connector hook to record if the hook is invoked.
	 */
	private static void addRecordingHooks(ConnectorConfig cc) {
		List<String> hookNames = getHookNamesForConnectorMode(cc);
		for (String hook : hookNames) {
			
			boolean hookAlreadyExisted = cc.getHooks().getHook(hook, false) != null;
			
			addRecordingConnectorHook(cc.getHooks(), hook);

			if (hook.equalsIgnoreCase("initialize_fail") || hook.equalsIgnoreCase("default_fail")) {
				if (!hookAlreadyExisted) {
					/*
					 * Re-throw the error in the last error hook in the flow,
					 * otherwise we will consume the error and will alter the
					 * flow.
					 */
					appendScriptToHook(cc, hook, "throw error.getObject(\"exception\");");
				}
			}
		}
	}

	private static void addRecordingHooks(ConnectorConfig cc, boolean overrideDefaultOperationWithHook) {
		addRecordingHooks(cc);
		if (!overrideDefaultOperationWithHook) {
			// make sure we don't override the Connector operation with hook
			cc.getHooks().removeHook(getOverrideHookForOperation(getDefaultOperationForMode(cc.getMode())));
		}
	}

	private static List<String> flattenObjectArray(Object[] array) {
		List<String> result = new ArrayList<String>();
		for (Object obj : array) {
			if (obj instanceof Object[]) {
				List<String> piece = flattenObjectArray((Object[]) obj);
				result.addAll(piece);
			} else {
				result.add(obj.toString());
			}
		}
		return result;
	}

	private static List<String> getHookNamesForConnectorMode(ConnectorConfig cc) {
		Object[] objs = HooksUtil.getHookTree(cc);
		List<String> result = flattenObjectArray(objs);
		result.removeAll(Arrays.asList("input_attribute_map", "output_attribute_map"));
		return result;
	}

	private static void addRecordingConnectorHook(HooksConfig hooksConfig, String hookName) {
		String script = "";
		script += "thisConnector.connector.recordEvent(\"" + hookName + "\");";
		addConnectorHook(hooksConfig, hookName, script);
	}

	private static void addConnectorHook(HooksConfig hooksConfig, String hookName, String script) {
		HookConfig hc = hooksConfig.getHook(hookName, false);
		if (hc != null) {
			// append script to existing hook
			String oldScript = hc.getScript();
			if (oldScript == null) {
				oldScript = "";
			}
			hc.setScript(oldScript+script);
		} else {
			hc = hooksConfig.getHook(hookName, true);
			hc.setScript(script);
			hc.setEnabled(true);
		}
	}

	/**
	 * Encapsulates common functionality for ConnectorComponent test scenarios:
	 * simulates a number of AssemblyLine cycles, failure of the underlying
	 * Connector certain times on certain operations, reconnect configuration,
	 * verifies whether the right hooks are invoked in the right order and so
	 * on.
	 */
	private static class ConnectorTestCase {

		private ConnectorConfig connectorConfig;
		private Exception executionError;
		private TestConnector connector;
		private int iterationCount = 1;
		private boolean overrideDefaultOperationWithHook = false;
		private boolean recordEvents = false;
		private boolean verifyHookBeans = false;
		private boolean expectExecutionError = false;
		private String throwingOperation = null;
		private int reconnectRetries = 0;

		public ConnectorTestCase(String connectorMode) throws Exception {
			this.connectorConfig = createConnectorConfig(connectorMode);

			// some settings for reconnect
			connectorConfig.getReconnectConfig().setIntegerParameter("retryDelay", 0);
			ReconnectRuleConfig rule = connectorConfig.getReconnectConfig().newReconnectRule();
			rule.setParameter("action", "reconnect");
			rule.setParameter("exceptionClass", "" + TestConnectorException.class.getName());
		}

		public Exception getExecutionError() {
			return executionError;
		}

		/**
		 * @return The underlying Connector object, which the ConnectorComponent
		 *         hosts. It is a special test Connector, which you can use to
		 *         do certain verifications.
		 */
		public TestConnector getConnector() {
			return connector;
		}

		/**
		 * Set the number of AssemblyLine iterations.
		 */
		public void setIterationCount(int iterationCount) {
			this.iterationCount = iterationCount;
			if (connectorConfig.getMode().equals(ConnectorConfig.ITERATOR_MODE)) {
				connectorConfig.getConnectionConfig().setIntegerParameter(TestConnector.PARAM_ITERATION_COUNT, iterationCount);
			}
		}

		public ConnectorConfig getConnectorConfig() {
			return connectorConfig;
		}

		public void setReconnectOnInitialize(boolean reconnect) {
			connectorConfig.getReconnectConfig().setBooleanParameter("initreconnect", reconnect);
		}

		public void setReconnectOnOperation(boolean reconnect) {
			connectorConfig.getReconnectConfig().setBooleanParameter("autoreconnect", reconnect);
		}

		public void setReconnectRetries(int retries) {
			reconnectRetries = retries;
		}

		/**
		 * @param expect
		 *            Whether the ConnectorComponent is supposed to throw an
		 *            exception during the test.
		 */
		public void setExpectExecutionError(boolean expect) {
			expectExecutionError = expect;
		}

		/**
		 * @param record
		 *            Whether the test will pay attention to the invocation
		 *            order of Connector methods and hooks.
		 */
		public void setRecordEvents(boolean record) {
			recordEvents = record;
			if (record) {
				verifyHookBeans = true;
			}
		}

		/**
		 * @param override
		 *            Whether to add a hook, which overrides the default
		 *            Connector operation for the mode. For example in Iterator
		 *            mode it will define a "override_getnext" hook, which will
		 *            effectively prevent the getNextEntry() method from
		 *            executing.
		 */
		public void setOverrideDefaultOperationWithHook(boolean override) {
			overrideDefaultOperationWithHook = override;
		}

		/**
		 * @param operationName
		 *            Name of a Connector operation ("initialize",
		 *            "selectEntries" ...).
		 * @param throwCount
		 *            How many times the Connector operation will throw an
		 *            exception before it succeeds.
		 */
		public void setThrowOnOperation(String operationName, int throwCount) {
			connectorConfig.getConnectionConfig().setIntegerParameter(
					TestConnector.PARAM_TIMES_TO_THROW_PREFIX + operationName.toLowerCase(), throwCount);
			
			if (!operationName.equalsIgnoreCase("reconnect")) {
				// don't care that reconnect throws
				if (throwingOperation != null) {
					throw new UnsupportedOperationException("Only one throwing operation is supported by this test class.");
				}
				throwingOperation = operationName;
			}
			
		}

		private void prepareConnectorConfigForExecution() throws Exception {
			
			connectorConfig.getReconnectConfig().setIntegerParameter("numberOfRetries", reconnectRetries);
			
			connectorConfig.getConnectionConfig().setBooleanParameter(TestConnector.PARAM_RECORD_EVENTS, recordEvents);
			if (recordEvents) {
				addRecordingHooks(connectorConfig, overrideDefaultOperationWithHook);
			}
			if (verifyHookBeans) {
				addBeanVerifyingHooks(connectorConfig, overrideDefaultOperationWithHook);
			}
			connectorConfig.getConnectionConfig().setIntegerParameter(TestConnector.PARAM_ITERATION_COUNT, iterationCount);

			if (overrideDefaultOperationWithHook) {

				if (connectorConfig.getMode().equals(ConnectorConfig.ITERATOR_MODE)) {
					/*
					 * If the getNextEntry() is overridden we cannot count the
					 * iterations in getNextEntry(). We need a way to know when
					 * the expected iteration count is reached, so that we stop.
					 * The solution is put the logic into the "override_getnext"
					 * hook. First initialize the counter variable.
					 */
					appendScriptToHook(connectorConfig, "before_initialize", "var i = 0; var iterationCount = " + iterationCount
							+ ";");

					/*
					 * Count the iterations and make sure the 'work' Entry is
					 * not empty for each but the last iteration. On the last
					 * iteration leave the 'work' empty, so that the Server
					 * knows the iteration is over.
					 */
					appendScriptToHook(connectorConfig, "override_getnext",
							"if (i < iterationCount) {work.setAttribute(\"attr\", \"value\"); ++i;} ");
				}
			}
			
			// if Connector is in lookup mode and there is no link criteria, we need to add a dummy one
			if (connectorConfig.getMode().endsWith(ConnectorConfig.LOOKUP_MODE) && connectorConfig.getLinkCriteria().getCriteriaNames().size() == 0) {
				LinkCriteriaConfigImpl crit = (LinkCriteriaConfigImpl) connectorConfig.getLinkCriteria();
				crit.setAdvancedLinkMode(true);
				crit.setAdvancedLinkCriteria("var x = 0;");
			}
			
		}

		/**
		 * Execute the test and perform standard verifications. Standard
		 * verifications include exception status and the invocation order of
		 * Connector methods/hooks. You can do more verifications yourself, once
		 * the test is over. Use the {@link #getExecutionError()} and
		 * {@link #getConnector()} methods to access test data for verification.
		 */
		public void execute() throws Exception {

			prepareConnectorConfigForExecution();

			executeImpl();

			verify();
		}

		/**
		 * Prepare and run an AssemblyLine which hosts the ConnectorComponent.
		 */
		private void executeImpl() throws Exception {
			
			AssemblyLineConfig alc = createTestAssemblyLineConfig();
			
			if (connectorConfig.getMode() != ConnectorConfig.ITERATOR_MODE && iterationCount != 1) {
				// we need a synthetic Iterator to reach the desired iteration count
				ConnectorConfig iterator = createConnectorConfig(ConnectorConfig.ITERATOR_MODE);
				iterator.setName("Synthetic Iterator");
				iterator.getConnectionConfig().setIntegerParameter(TestConnector.PARAM_ITERATION_COUNT, iterationCount);
				alc.addComponent(iterator);
			}
			
			alc.addComponent(connectorConfig);

			TaskCallBlock tcb = new TaskCallBlock();
			tcb.setRunMode(AssemblyLine.RUNMODE_NODEBUG);

			RSInterface server = new TestRS();
			String taskName = "testal";
			Log log = new NOOPLog();
			AssemblyLine al = new AssemblyLine(server, taskName, tcb, log, alc);
			al.executeInitializeAL();
			/*
			 * Do not execute the final steps of the AssemblyLine (which do
			 * termination and cleanup), so that we can retrieve the Connector
			 * object.
			 */
			al.executeMainLoop(ALState.MS_EPILOG);
			AssemblyLineComponent component = al.getConnector(connectorConfig.getShortName());
			connector = (TestConnector) component.getConnector();
			al.executeMainLoop();
			executionError = al.getStats().getError();
		}

		private void verify() {

			if (expectExecutionError) {
				if (getExecutionError() != null && !(getExecutionError() instanceof TestConnectorException)) {
					// this error is not produced intentionally by the test code
					throw new RuntimeException(getExecutionError());
				}
			} else {
				// unexpected error
				if (getExecutionError() != null) {
					throw new RuntimeException(getExecutionError());
				}
			}

			if (recordEvents) {
				
				ConnectorEventSequence eventSequence;
				if (throwingOperation == null) {
					eventSequence = getEventSequence(connectorConfig.getMode());
				} else {
					final boolean reconnectSuccess = !expectExecutionError;
					eventSequence = getEventSequenceWithError(connectorConfig.getMode(), throwingOperation, reconnectSuccess, reconnectRetries);
				}
				
				eventSequence.setIterationCount(iterationCount);
				eventSequence.setOverrideDefaultOperationWithHook(overrideDefaultOperationWithHook);

				List<String> expectedEvents = eventSequence.getExpectedEvents();

				// remove all occurrences of disabled hooks from the expected list of events
				for (String hook : connectorConfig.getHooks().getKeys(BaseConfiguration.RECURSIVE_SUBTREE)) {
					if (!connectorConfig.getHooks().getHook(hook).getEnabled()) {
						while (expectedEvents.remove(hook)) {
						}
					}
				}

				assertEquals(expectedEvents, connector.getRecordedEvents());
			}
		}

		private AssemblyLineConfig createTestAssemblyLineConfig() throws Exception {
			AssemblyLineConfig alc = new AssemblyLineConfigImpl();
			alc.init();
			alc.setName("testal");
			return alc;
		}

		private static ConnectorConfig createConnectorConfig(String connectorMode) throws Exception {
			ConnectorConfig cc = new ConnectorConfigImpl();
			cc.init();
			cc.setName("Test Connector");
			cc.setState(ConnectorConfig.ENABLED_STATE);
			cc.getConnectionConfig().setJavaClass(TestConnector.class.getName());
			cc.setMode(connectorMode);
			return cc;
		}
	}

	/**
	 * Connector used for testing. It combines utility features of several
	 * Connector classes.
	 */
	public static class TestConnector extends HookBeanVerificationConnector {
	}

	/**
	 * This Connector provides utility methods, which are intended for hooks to
	 * use for verification.
	 * 
	 */
	public static class HookBeanVerificationConnector extends RecordingConnector {
		
		private static Map<String, Class<?>> beanClassMap = new HashMap<String, Class<?>>();
		static {
			beanClassMap.put("work", com.ibm.di.entry.Entry.class);
			beanClassMap.put("conn", com.ibm.di.entry.Entry.class);
			beanClassMap.put("error", com.ibm.di.entry.Entry.class);
		}
		
		private static Map<String, List<String>> hookBeansMap = new HashMap<String, List<String>>();
		static {
			
			List<String> work = Arrays.asList("work");
			List<String> work_conn = Arrays.asList("work", "conn");
			List<String> error = Arrays.asList("error");
			List<String> none = Collections.EMPTY_LIST;
			
			hookBeansMap.put("before_initialize", none);
			hookBeansMap.put("before_selectEntries", none);
			hookBeansMap.put("after_selectEntries", none);
			hookBeansMap.put("after_initialize", none);
			
			hookBeansMap.put("before_execute", work);
			
			hookBeansMap.put("before_getnext", work);
			hookBeansMap.put("after_getnext", work_conn);
			
			hookBeansMap.put("before_lookup", work);
			hookBeansMap.put("after_lookup", work_conn);
			
			hookBeansMap.put("before_update", work);
			hookBeansMap.put("after_update", work_conn);
			
			hookBeansMap.put("before_delete", work);
			hookBeansMap.put("after_delete", work_conn);
			
			hookBeansMap.put("before_add", work);
			hookBeansMap.put("after_add", work_conn);
			
			hookBeansMap.put("before_modify", work);
			hookBeansMap.put("after_modify", work_conn);
			
			hookBeansMap.put("override_getnext", work);
			hookBeansMap.put("override_lookup", work);
			hookBeansMap.put("override_update", work);
			hookBeansMap.put("override_delete", work);
			hookBeansMap.put("override_add", work);
			hookBeansMap.put("override_modify", work);
			
			hookBeansMap.put("get_ok", work);
			hookBeansMap.put("lookup_ok", work);
			hookBeansMap.put("update_ok", work);
			hookBeansMap.put("delete_ok", work);
			hookBeansMap.put("addonly_ok", work);
			hookBeansMap.put("modify_ok", work);
			
			hookBeansMap.put("lookup_multiple", work);
			hookBeansMap.put("lookup_nomatch", work);
			
			hookBeansMap.put("default_ok", work);
			
			hookBeansMap.put("end_of_data", none);
			hookBeansMap.put("before_close", none);
			hookBeansMap.put("after_close", none);
			
			hookBeansMap.put("connect_init", error);
			hookBeansMap.put("initialize_fail", error);
			hookBeansMap.put("on_connection_failure", error);
			hookBeansMap.put("get_fail", error);
			hookBeansMap.put("lookup_fail", error);
			hookBeansMap.put("update_fail", error);
			hookBeansMap.put("delete_fail", error);
			hookBeansMap.put("addonly_fail", error);
			hookBeansMap.put("modify_fail", error);
			hookBeansMap.put("default_fail", error);
			hookBeansMap.put("close_fail", error);
		}
		
		public static List<String> getBeansForHook(String hookName) {
			return hookBeansMap.get(hookName);
		}
		
		public void verifyHookBean(String hookName, String beanName, Object obj) {
			
			// message to help debug
			String nullMsg = "Bean '" + beanName + "' is null in hook '" + hookName + "'.";
			assertNotNull(nullMsg, obj);
			
			Class<?> expectedBeanClass = beanClassMap.get(beanName);
			if (expectedBeanClass == null) {
				throw new RuntimeException("Unexpected script bean '"+beanName+"' in hook '"+hookName+"'.");
			}
			// message to help debug
			String classMsg = "Bean '" + beanName + "' in hook '" + hookName + "' is an object of Java class " + obj.getClass()
					+ ". Expected class is " + expectedBeanClass;
			assertTrue(classMsg, expectedBeanClass.isInstance(obj));
		}
		
	}

	/**
	 * This Connector records the name of events. Events are method invocations
	 * on the Connector object and hook invocations. To record hook invocations,
	 * we add script in the hooks which calls the {@link #recordEvent(String)}
	 * method.
	 */
	public static class RecordingConnector extends StatisticsConnector {

		public static final String PARAM_RECORD_EVENTS = "recordEvents";

		private boolean recordEvents = true;
		private List<String> events = new ArrayList<String>();

		public List<String> getRecordedEvents() {
			return events;
		}

		public void recordEvent(String eventName) {
			if (recordEvents) {
				events.add(eventName);
			}
		}

		@Override
		public void initialize(Object o) throws Exception {
			this.recordEvents = Boolean.valueOf(getParam(PARAM_RECORD_EVENTS)).booleanValue();

			recordEvent("initialize");
			super.initialize(o);
		}

		@Override
		public void selectEntries() throws Exception {
			recordEvent("selectEntries");
			super.selectEntries();
		}

		@Override
		public Entry getNextEntry() throws Exception {
			recordEvent("getNextEntry");
			return super.getNextEntry();
		}

		@Override
		public void putEntry(Entry entry) throws Exception {
			recordEvent("putEntry");
			super.putEntry(entry);
		}
		
		@Override
		public Entry findEntry(SearchCriteria search) throws Exception {
			recordEvent("findEntry");
			return super.findEntry(search);
		}

		@Override
		public void terminate() throws Exception {
			recordEvent("terminate");
			super.terminate();
		}

		@Override
		public void reconnect(Object o) throws Exception {
			recordEvent("reconnect");
			super.reconnect(o);
		}
	}

	/**
	 * This Connector keeps track how many times each of its methods is invoked.
	 */
	public static class StatisticsConnector extends ExceptionThrowingConnector {

		private Counters callCounts = new Counters();

		public int getCallCount(String methodName) {
			return callCounts.getCounter(methodName);
		}

		@Override
		public void initialize(Object o) throws Exception {
			callCounts.incCounter("initialize");
			super.initialize(o);
		}

		@Override
		public void selectEntries() throws Exception {
			callCounts.incCounter("selectEntries");
			super.selectEntries();
		}

		@Override
		public Entry getNextEntry() throws Exception {
			callCounts.incCounter("getNextEntry");
			return super.getNextEntry();
		}

		@Override
		public void putEntry(Entry entry) throws Exception {
			callCounts.incCounter("putEntry");
			super.putEntry(entry);
		}
		
		@Override
		public Entry findEntry(SearchCriteria search) throws Exception {
			callCounts.incCounter("findEntry");
			return super.findEntry(search);
		}

		@Override
		public void terminate() throws Exception {
			callCounts.incCounter("terminate");
			super.terminate();
		}

		@Override
		public void reconnect(Object o) throws Exception {
			callCounts.incCounter("reconnect");
			super.reconnect(o);
		}
	}

	/**
	 * Connector which can throw a test exception a number of times on each
	 * call. When the throwing count is exhausted, the call succeeds.
	 */
	public static class ExceptionThrowingConnector extends FixedEntryConnector {

		/**
		 * How many times to throw an exception when a method is invoked.
		 */
		public static final String PARAM_TIMES_TO_THROW_PREFIX = "timesToThrowOn";

		private static final String PARAM_TIMES_TO_THROW_PREFIX_LC = PARAM_TIMES_TO_THROW_PREFIX.toLowerCase();

		private Counters throwCounts = new Counters();
		private Counters expectedThrowCounts = new Counters();
		private boolean initialized = false;

		private void throwIfNecessary(String methodName) throws Exception {
			if (throwCounts.getCounter(methodName) < expectedThrowCounts.getCounter(methodName)) {
				throwCounts.incCounter(methodName);
				throw new TestConnectorException("Test error on " + methodName);
			}
		}

		@Override
		public void initialize(Object o) throws Exception {

			if (!initialized) {
				BaseConfiguration cc = getRawConnectorConfiguration();
				for (String param : cc.getKeys(BaseConfiguration.ONE_LEVEL)) {
					String lc = param.toLowerCase();
					if (lc.startsWith(PARAM_TIMES_TO_THROW_PREFIX_LC)) {
						int timesToThrow = Integer.parseInt(cc.getStringParameter(param));
						String methodName = param.substring(PARAM_TIMES_TO_THROW_PREFIX_LC.length());
						expectedThrowCounts.setCounter(methodName, timesToThrow);
						throwCounts.setCounter(methodName, 0);
					}
				}
				initialized = true;
			}

			throwIfNecessary("initialize");

			super.initialize(o);
		}

		@Override
		public void selectEntries() throws Exception {
			throwIfNecessary("selectEntries");
			super.selectEntries();
		}

		@Override
		public Entry getNextEntry() throws Exception {
			throwIfNecessary("getNextEntry");
			return super.getNextEntry();
		}

		@Override
		public void putEntry(Entry entry) throws Exception {
			throwIfNecessary("putEntry");
			super.putEntry(entry);
		}
		
		@Override
		public Entry findEntry(SearchCriteria search) throws Exception {
			throwIfNecessary("findEntry");
			return super.findEntry(search);
		}

		@Override
		public void terminate() throws Exception {
			throwIfNecessary("terminate");
			super.terminate();
		}

		@Override
		public void reconnect(Object o) throws Exception {
			throwIfNecessary("reconnect");
			super.reconnect(o);
		}
	}

	/**
	 * Connector, which can do a configurable number of iterations in Iterator
	 * mode. The Connector always returns the same Entry. The number of
	 * Attributes in the Entry is configurable.
	 */
	public static class FixedEntryConnector extends Connector {

		/**
		 * How many iterations to do the Connector in Iterator mode. Default is
		 * zero.
		 */
		public static final String PARAM_ITERATION_COUNT = "iterationCount";

		/**
		 * How many attributes to have in each Entry that the Connector returns.
		 * Attribute names would be "attr0", "attr1", etc. The corresponding
		 * attribute values would be "value0", value1", etc.
		 */
		public static final String PARAM_ATTRIBUTE_COUNT = "attributeCount";

		/**
		 * Parsed value of the {@link #PARAM_ITERATION_COUNT} parameter.
		 */
		private int iterationCount = 0;

		/**
		 * The number of the current iteration. It should grow no more than
		 * {@link #iterationCount}.
		 */
		private int currentIteration = 0;

		/**
		 * Parsed value of the {@link #PARAM_ATTRIBUTE_COUNT} parameter.
		 */
		private int attributeCount = 0;

		/**
		 * This is the Entry which the Connectors returns on each cycle.
		 */
		private Entry entry;

		public FixedEntryConnector() {
			setName("JUnit Test Connector 1.0");
			setModes(new String[] { ConnectorConfig.ITERATOR_MODE, ConnectorConfig.ADDONLY_MODE });
		}

		@Override
		public void initialize(Object o) throws Exception {
			if (null != getParam(PARAM_ITERATION_COUNT)) {
				iterationCount = Integer.parseInt(getParam(PARAM_ITERATION_COUNT));
			}
			entry = produceEntry();
		}

		@Override
		public Entry getNextEntry() throws Exception {
			Entry result = null;
			if (currentIteration < iterationCount) {
				result = entry;
				++currentIteration;
			}
			return result;
		}
		
		@Override
		public void putEntry(Entry entry) throws Exception {
		}
		
		@Override
		public Entry findEntry(SearchCriteria search) throws Exception {
			return produceEntry();
		}

		@Override
		public void reconnect(Object o) throws Exception {
		}

		public String getVersion() {
			return "JUnit Test Connector 1.0";
		}

		private Entry produceEntry() {
			Entry entry = new Entry();
			for (int i = 0; i < attributeCount; ++i) {
				String attrName = "attr" + i;
				String attrValue = "value" + i;
				entry.setAttribute(attrName, attrValue);
			}
			return entry;
		}
	}

	/**
	 * This exception is thrown by the test Connector to
	 * exercise the reconnect feature.
	 */
	public static class TestConnectorException extends Exception {
		public TestConnectorException(String message) {
			super(message);
		}
	}

	/**
	 * A set of named counters.
	 */
	private static class Counters {

		private Map<String, Integer> counters = new HashMap<String, Integer>();

		private String normalizeName(String name) {
			if (name == null) {
				return "";
			} else {
				return name.trim().toLowerCase();
			}
		}

		public int getCounter(String name) {
			Integer counterObj = counters.get(normalizeName(name));
			return counterObj != null ? counterObj.intValue() : 0;
		}

		public void incCounter(String name) {
			setCounter(name, getCounter(name) + 1);
		}

		public void setCounter(String name, int value) {
			counters.put(normalizeName(name), value);
		}
	}

	private static class TestRS extends RS {
		private Log log = new NOOPLog();

		public TestRS() throws Exception {
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

	private boolean isPrologOperation(String operationName) {
		return "initialize".equalsIgnoreCase(operationName) || "selectEntries".equalsIgnoreCase(operationName);
	}

	private static String getErrorHookForOperation(String operationName) {
		String hook;
		if ("initialize".equalsIgnoreCase(operationName)) {
			hook = "initialize_fail";
		} else if ("selectEntries".equalsIgnoreCase(operationName)) {
			hook = "initialize_fail";
		} else if ("getNextEntry".equalsIgnoreCase(operationName)) {
			hook = "get_fail";
		} else if ("putEntry".equalsIgnoreCase(operationName)) {
			hook = "addonly_fail";
		} else if ("findEntry".equalsIgnoreCase(operationName)) {
			hook = "lookup_fail";
		} else {
			throw new UnsupportedOperationException("Cannot determine error hook for operation '" + operationName + "'.");
		}
		return hook;
	}

	private static List<String> getErrorHookListForOperation(String operationName) {
		List<String> hooks = new ArrayList<String>();
		hooks.add(getErrorHookForOperation(operationName));

		final boolean initOrSelectOperation = operationName.equalsIgnoreCase("initialize")
				|| operationName.equalsIgnoreCase("selectEntries");
		// default error hook
		if (!initOrSelectOperation) {
			hooks.add("default_fail");
		}
		return hooks;
	}

	private static String getBeforeHookForFlowOperation(String operationName) {
		String hook;
		if ("getNextEntry".equalsIgnoreCase(operationName)) {
			hook = "before_getnext";
		} else if ("putEntry".equalsIgnoreCase(operationName)) {
			hook = "before_add";
		} else if ("findEntry".equalsIgnoreCase(operationName)) {
			hook = "before_lookup";
		} else {
			throw new UnsupportedOperationException("Cannot determine before hook for operation '" + operationName + "'.");
		}
		return hook;
	}

	private static String getAfterHookForFlowOperation(String operationName) {
		String hook;
		if ("getNextEntry".equalsIgnoreCase(operationName)) {
			hook = "after_getnext";
		} else if ("putEntry".equalsIgnoreCase(operationName)) {
			hook = "after_add";
		} else if ("findEntry".equalsIgnoreCase(operationName)) {
			hook = "after_lookup";
		} else {
			throw new UnsupportedOperationException("Cannot determine after hook for operation '" + operationName + "'.");
		}
		return hook;
	}

	private static String getOverrideHookForOperation(String operationName) {
		String hook;
		if (operationName.equalsIgnoreCase("getNextEntry")) {
			hook = AssemblyLineComponent.OR_GETNEXT;
		} else if (operationName.equalsIgnoreCase("putEntry")) {
			hook = AssemblyLineComponent.OR_ADD;
		} else if (operationName.equalsIgnoreCase("findEntry")) {
			hook = AssemblyLineComponent.OR_LOOKUP;
		} else {
			throw new UnsupportedOperationException("Cannot determine override hook for operation '" + operationName + "'.");
		}
		return hook;
	}
	
	private static String getOKHookForOperation(String operationName) {
		String hook;
		if (operationName.equalsIgnoreCase("getNextEntry")) {
			hook = "get_ok";
		} else if (operationName.equalsIgnoreCase("putEntry")) {
			hook = "addonly_ok";
		} else if (operationName.equalsIgnoreCase("findEntry")) {
			hook = "lookup_ok";
		} else {
			throw new UnsupportedOperationException("Cannot determine override hook for operation '" + operationName + "'.");
		}
		return hook;
	}

	private static String getDefaultOperationForMode(String connectorMode) {
		String defaultOperation;
		if (ConnectorConfig.ITERATOR_MODE.equals(connectorMode)) {
			defaultOperation = "getNextEntry";
		} else if (ConnectorConfig.ADDONLY_MODE.equals(connectorMode)) {
			defaultOperation = "putEntry";
		} else if (ConnectorConfig.LOOKUP_MODE.equals(connectorMode)) {
			defaultOperation = "findEntry";
		} else {
			throw new UnsupportedOperationException("Cannot determine default operation for Connector mode '" + connectorMode
					+ "'.");
		}
		return defaultOperation;
	}

	private static ConnectorEventSequence getEventSequence(String connectorMode) {
		ConnectorEventSequence eventSequence;
		if (ConnectorConfig.ITERATOR_MODE.equals(connectorMode)) {
			eventSequence = new IteratorEventSequence();
		} else {
			eventSequence = new ConnectorEventSequence(connectorMode);
		}
		return eventSequence;
	}

	private static ConnectorEventSequence getEventSequenceWithError(String connectorMode, String failedOperationName,
			boolean reconnectSuccess, int reconnectRetries) {
		ConnectorEventSequence normalEventSequence = getEventSequence(connectorMode);
		ConnectorEventSequence eventSequence;
		if (reconnectSuccess) {
			eventSequence = new ConnectorEventSequenceWithSuccessfulReconnect(normalEventSequence, failedOperationName,
					reconnectRetries);
		} else {
			eventSequence = new ConnectorEventSequenceWithFatalError(normalEventSequence, failedOperationName, reconnectRetries);
		}
		return eventSequence;
	}

	public static class ConnectorEventSequenceWithSuccessfulReconnect extends ConnectorEventSequenceWithError {

		public ConnectorEventSequenceWithSuccessfulReconnect(ConnectorEventSequence normalSequence, String failedOperationName,
				int reconnectRetries) {
			super(normalSequence, failedOperationName, reconnectRetries);
		}

		@Override
		public List<String> getExpectedEvents() {

			final boolean initOrSelectFailed = failedOperationName.equalsIgnoreCase("initialize")
					|| failedOperationName.equalsIgnoreCase("selectEntries");

			List<String> normalEvents = normalSequence.getExpectedEvents();
			int failedOperationIndex = normalEvents.indexOf(failedOperationName);
			List<String> beforeEvents = normalEvents.subList(0, failedOperationIndex);
			List<String> afterEvents = normalEvents.subList(failedOperationIndex + 1, normalEvents.size());

			if (afterEvents.get(0).equalsIgnoreCase("after_selectEntries")) {
				/*
				 * An odd thing about the after_selectEntries hook is that it is
				 * not invoked after reconnect.
				 */
				afterEvents = afterEvents.subList(1, afterEvents.size());
			}

			List<String> l = new ArrayList<String>();

			l.addAll(beforeEvents);
			l.add(failedOperationName);
			l.addAll(getReconnectEvents());
			if (!initOrSelectFailed) {
				l.add("before_execute");
				l.add(getBeforeHookForFlowOperation(failedOperationName));
				l.add(failedOperationName);
			}
			l.addAll(afterEvents);

			return l;
		}

	}

	public static class ConnectorEventSequenceWithFatalError extends ConnectorEventSequenceWithError {

		public ConnectorEventSequenceWithFatalError(ConnectorEventSequence normalSequence, String failedOperationName,
				int reconnectRetries) {
			super(normalSequence, failedOperationName, reconnectRetries);
		}

		@Override
		public List<String> getExpectedEvents() {

			final boolean initFailed = failedOperationName.equalsIgnoreCase("initialize");
			final boolean initOrSelectFailed = initFailed || failedOperationName.equalsIgnoreCase("selectEntries");

			List<String> normalEvents = normalSequence.getExpectedEvents();
			int failedOperationIndex = normalEvents.indexOf(failedOperationName);
			if (failedOperationIndex < 0) {
				throw new RuntimeException("Could not find operation '"+failedOperationName+"' in list "+normalEvents);
			}
			List<String> beforeEvents = normalEvents.subList(0, failedOperationIndex);

			List<String> l = new ArrayList<String>();

			l.addAll(beforeEvents);
			l.add(failedOperationName);
			l.addAll(getReconnectEvents());

			// operation-specific error hooks
			l.addAll(getErrorHookListForOperation(failedOperationName));

			l.addAll(getEpilogEvents());

			if (initOrSelectFailed) {
				l.remove("terminate");
			}

			return l;
		}
	}

	public static abstract class ConnectorEventSequenceWithError extends ConnectorEventSequence {
		protected ConnectorEventSequence normalSequence;
		protected String failedOperationName;
		protected int reconnectRetries;

		protected ConnectorEventSequenceWithError(ConnectorEventSequence normalSequence, String failedOperationName,
				int reconnectRetries) {
			super(normalSequence.connectorMode);
			this.normalSequence = normalSequence;
			this.failedOperationName = failedOperationName;
			this.reconnectRetries = reconnectRetries;
		}

		protected List<String> getReconnectEvents() {

			final boolean initFailed = failedOperationName.equalsIgnoreCase("initialize");
			final boolean initOrSelectFailed = initFailed || failedOperationName.equalsIgnoreCase("selectEntries");

			List<String> l = new ArrayList<String>();

			final String reconnectStartHook;
			if (initOrSelectFailed) {
				reconnectStartHook = "connect_init";
			} else {
				reconnectStartHook = "on_connection_failure";
			}
			l.add(reconnectStartHook);

			final String reconnectOperation;
			if (initFailed) {
				reconnectOperation = "initialize";
			} else {
				reconnectOperation = "reconnect";
			}
			for (int i = 0; i < reconnectRetries; ++i) {
				l.add(reconnectOperation);
			}

			return l;
		}
	}

	public static class IteratorEventSequence extends ConnectorEventSequence {

		public IteratorEventSequence() {
			super(ConnectorConfig.ITERATOR_MODE);
		}

		@Override
		public List<String> getExpectedEvents() {
			List<String> l = new ArrayList<String>();

			l.addAll(getPrologEvents());

			for (int i = 0; i < iterationCount; ++i) {
				l.addAll(getFlowEvents());
			}
			l.add("before_execute");
			l.addAll(getEventsOnEndIteration());
			l.add("end_of_data");

			l.addAll(getEpilogEvents());

			return l;
		}

		@Override
		protected List<String> getPrologEvents() {
			// Only Iterator mode has selectEntries in the Prolog phase
			List<String> l = new ArrayList<String>();
			l.add("before_initialize");
			l.add("initialize");
			l.add("before_selectEntries");
			l.add("selectEntries");
			l.add("after_selectEntries");
			l.add("after_initialize");
			return l;
		}

		private List<String> getEventsOnEndIteration() {
			List<String> l = new ArrayList<String>();
			final String op = getDefaultOperationForMode(connectorMode);
			if (overrideDefaultOperationWithHook) {
				l.add(getOverrideHookForOperation(op));
			} else {
				l.add(getBeforeHookForFlowOperation(op));
				l.add(op);
			}
			return l;
		}
	}

	/**
	 * Event sequence in the life-time of a Connector. The invoked methods and
	 * hooks are considered events.
	 */
	public static class ConnectorEventSequence {

		protected final String connectorMode;
		protected boolean overrideDefaultOperationWithHook = false;
		protected int iterationCount = 1;

		private ConnectorEventSequence(String connectorMode) {
			this.connectorMode = connectorMode;
		}

		public void setOverrideDefaultOperationWithHook(boolean overrideDefaultOperationWithHook) {
			this.overrideDefaultOperationWithHook = overrideDefaultOperationWithHook;
		}

		public void setIterationCount(int iterationCount) {
			this.iterationCount = iterationCount;
		}
		
		public List<String> getExpectedEvents() {
			List<String> l = new ArrayList<String>();

			l.addAll(getPrologEvents());

			for (int i = 0; i < iterationCount; ++i) {
				l.addAll(getFlowEvents());
			}

			l.addAll(getEpilogEvents());

			return l;
		}

		protected List<String> getPrologEvents() {
			List<String> l = new ArrayList<String>();
			l.add("before_initialize");
			l.add("initialize");
			l.add("after_initialize");
			return l;
		}
		
		protected List<String> getFlowEvents() {
			final String op = getDefaultOperationForMode(connectorMode);
			
			List<String> l = new ArrayList<String>();
			l.add("before_execute");
			if (overrideDefaultOperationWithHook) {
				l.add(getOverrideHookForOperation(op));
			} else {
				l.add(getBeforeHookForFlowOperation(op));
				l.add(op);
				l.add(getAfterHookForFlowOperation(op));
			}			
			l.add(getOKHookForOperation(op));
			l.add("default_ok");
			return l;
		}

		protected List<String> getEpilogEvents() {
			List<String> l = new ArrayList<String>();
			l.add("before_close");
			l.add("terminate");
			l.add("after_close");
			return l;
		}
	}

}
