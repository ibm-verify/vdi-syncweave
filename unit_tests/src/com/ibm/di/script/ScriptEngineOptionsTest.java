package com.ibm.di.script;

import org.junit.Test;

import com.ibm.jscript.IValue;
import com.ibm.jscript.JSContext;

import static org.junit.Assert.*;

public class ScriptEngineOptionsTest {
	
	@Test
	public void test_get_noargs() throws Exception {
		JSContext x = ScriptEngineOptions.get();
		assertFalse(x.isDebugAllowed());
	}
	
	@Test
	public void test_get_debug_is_false() throws Exception {
		test_get_with_debug_arg(false);
	}
	
	@Test
	public void test_get_debug_is_true() throws Exception {
		test_get_with_debug_arg(true);
	}
	
	private void test_get_with_debug_arg(boolean debug) throws Exception {
		JSContext x = ScriptEngineOptions.get(debug);
		assertEquals(debug, x.isDebugAllowed());
	}
	
	@Test
	public void test_constructor1() throws Exception {
		ScriptEngineOptions seo = new ScriptEngineOptions();
		assertFalse(seo.isDebugAllowed());
	}
	
	@Test
	public void test_constructor2_debug_is_false() throws Exception {
		test_constructor2(false);
	}
	
	@Test
	public void test_constructor2_debug_is_true() throws Exception {
		test_constructor2(true);
	}
	
	private void test_constructor2(boolean debug) throws Exception {
		ScriptEngineOptions seo = new ScriptEngineOptions(false, debug);
		assertEquals(debug, seo.isDebugAllowed());
	}
	
	@Test
	public void test_get_returns_unique_object_when_debug_is_true() throws Exception {
		/*
		 * This assertion is important, because debug listeners are associated
		 * with the options object and we don't want a debugger to receive
		 * breaks from all script engines in the JVM, but only from the script
		 * engine of the AssemblyLine to which the debugger is attached.
		 */
		assertTrue(ScriptEngineOptions.get(true) != ScriptEngineOptions.get(true));
	}
	
	@Test
	public void test_addDebugListener_listener_gets_notified() throws Exception {
		ScriptEngineOptions seo = new ScriptEngineOptions(false, true);
		CountingDebugListener listener = new CountingDebugListener();
		seo.addDebugListener(listener);
		seo.debugStatement(null, null);
		assertEquals(1, listener.getCallCount());
	}

	@Test
	public void test_removeDebugListener_removed_listener_is_not_notified() throws Exception {
		ScriptEngineOptions seo = new ScriptEngineOptions(false, true);
		CountingDebugListener listener = new CountingDebugListener();
		seo.addDebugListener(listener);
		seo.removeDebugListener(listener);
		seo.debugStatement(null, null);
		assertEquals(0, listener.getCallCount());
	}
	
	@Test
	public void test_hasStringLengthAsMethod() throws Exception {
		assertEquals(true, new ScriptEngineOptions().hasStringLengthAsMethod());
	}
	
	@Test
	public void test_hasStringExtendedMethods() throws Exception {
		assertEquals(true, new ScriptEngineOptions().hasStringExtendedMethods());
	}
	
	@Test
	public void test_hasGlobalObjectExtensions() throws Exception {
		assertEquals(false, new ScriptEngineOptions().hasGlobalObjectExtensions());
	}
	
	@Test
	public void test_hasJUnitExtensions() throws Exception {
		assertEquals(false, new ScriptEngineOptions().hasJUnitExtensions());
	}
	
	@Test
	public void test_hasObjectPrototypeExtensions() throws Exception {
		assertEquals(true, new ScriptEngineOptions().hasObjectPrototypeExtensions());
	}
	
	@Test
	public void test_hasMathExtensions() throws Exception {
		assertEquals(true, new ScriptEngineOptions().hasMathExtensions());
	}
	
	@Test
	public void test_hasListOperator() throws Exception {
		assertEquals(false, new ScriptEngineOptions().hasListOperator());
	}
	
	@Test
	public void test_hasRhinoExtensions() throws Exception {
		assertEquals(true, new ScriptEngineOptions().hasRhinoExtensions());
	}
	
	@Test
	public void test_hasJavaBeanAccess() throws Exception {
		assertEquals(true, new ScriptEngineOptions().hasJavaBeanAccess());
	}
	
	@Test
	public void test_autoConvertJavaArgsToString() throws Exception {
		assertEquals(true, new ScriptEngineOptions().autoConvertJavaArgsToString());
	}
	
	@Test
	public void test_ignoreJavaCallAmbiguities() throws Exception {
		assertEquals(true, new ScriptEngineOptions().ignoreJavaCallAmbiguities());
	}
	
	/*
	 * 'getProperty' and 'setProperty' methods will be tested indirectly by tests for the ScriptEngine class,
	 * because we do not want to delve too deep into the JS engine API.
	 */
	
	// getProperty Entry @prop
	// getProperty Entry method of com.ibm.di.entry.Entry
	// getProperty 

	
}
