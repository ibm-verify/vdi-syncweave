package com.ibm.di.script;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.config.base.ScriptConfigImpl;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.TaskCallBlock;
import com.ibm.di.test.utils.TestUtils;

import static org.junit.Assert.*;

public class ScriptEngineTest {

	@Test
	public void test_constructor1() throws Exception {
		new ScriptEngine("");
	}

	@Test
	public void test_constructor2() throws Exception {
		new ScriptEngine("", null);
	}

	@Test
	public void test_constructor3() throws Exception {
		new ScriptEngine("", null, false);
	}

	@Test
	public void test_declareUserFunctions() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.declareUserFunctions();
	}

	@Test
	public void test_declareUserFunctions_twice() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.declareUserFunctions();
		se.declareUserFunctions();
	}

	@Test
	public void test_getLanguage() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		assertEquals("javascript", se.getLanguage().toLowerCase());
	}

	@Test
	public void test_exit_code_is_accessible_via_result_bean() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		Object resultBean = se.eval("result");
		assertEquals(resultBean, se.getExitCode());
	}

	@Test
	public void test_call_initial_exit_code_status_is_SEC_OK() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.eval("function myfunc() { return result.getStatus(); }");
		assertEquals((double) ScriptExitCode.SEC_OK, se.call("myfunc", new Object[] {}));
	}

	@Test
	public void test_exec_initial_exit_code_status_is_SEC_OK() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.exec("var i = result.getStatus();");
		assertEquals((double) ScriptExitCode.SEC_OK, se.eval("i"));
	}

	@Test
	public void test_declareTaskBean() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.declareTaskBean();
		Object task = se.eval("task");
		assertEquals(Thread.currentThread(), task);
	}

	@Test
	public void test_declareTaskBean_with_context_that_is_not_AssemblyLine() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		Object context = new Object();
		se.declareTaskBean(context);
		Object task = se.eval("task");
		assertEquals(Thread.currentThread(), task);
	}

	@Test
	public void test_declareTaskBean_with_context_that_is_AssemblyLine() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		AssemblyLine context = new AssemblyLine();
		se.declareTaskBean(context);
		Object task = se.eval("task");
		assertEquals(context, task);
	}

	@Test
	public void test_declareTaskBean__overwrites_existing_task_bean() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		AssemblyLine oldContext = new AssemblyLine();
		se.declareTaskBean(oldContext);
		AssemblyLine newContext = new AssemblyLine();
		se.declareTaskBean(newContext);
		Object task = se.eval("task");
		assertEquals(newContext, task);
	}

	@Test
	public void test_declareStaticBean_with_ASCII_name() throws Exception {
		test_declareStaticBean("asciibean");
	}

	@Test
	public void test_declareStaticBean_with_nonASCII_name() throws Exception {
		test_declareStaticBean("\u02dcbean");
	}

	private void test_declareStaticBean(String beanName) throws Exception {
		ScriptEngine se = new ScriptEngine("");
		Object bean = new Object();
		se.declareStaticBean(beanName, bean);
		Object beanFromEngine = se.eval(beanName);
		assertEquals(bean, beanFromEngine);
	}

	@Test
	public void test_declareStaticBean_overwrite_existing_bean() throws Exception {
		final String beanName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		Object bean1 = new Object();
		se.declareStaticBean(beanName, bean1);
		Object bean2 = new Object();
		se.declareStaticBean(beanName, bean2);
		Object beanFromEngine = se.eval(beanName);
		assertEquals(bean2, beanFromEngine);
	}

	@Test
	public void test_declareStaticBean_does_not_overwrite_existing_bean_when_the_new_bean_is_null() throws Exception {
		final String beanName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		Object bean1 = new Object();
		se.declareStaticBean(beanName, bean1);
		se.declareStaticBean(beanName, null);
		Object beanFromEngine = se.eval(beanName);
		assertEquals(bean1, beanFromEngine);
	}

	@Test
	public void test_declareStaticBean_ignores_class_parameter() throws Exception {
		final String beanName = "stringbean";
		ScriptEngine se = new ScriptEngine("");
		String bean = new String();
		se.declareStaticBean(beanName, bean, java.util.Date.class);
		Object beanFromEngine = se.eval(beanName);
		assertEquals(bean, beanFromEngine);
	}

	@Test
	public void test_undeclareStaticBean_undeclared_bean_is_null() throws Exception {
		final String beanName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		Object bean = new Object();
		se.declareStaticBean(beanName, bean);
		se.undeclareStaticBean(beanName);
		Object beanFromEngine = se.eval(beanName);
		assertNull(beanFromEngine);
	}

	@Test
	public void test_declareBean_with_ASCII_name() throws Exception {
		test_declareBean("asciibean");
	}

	@Test
	public void test_declareBean_with_nonASCII_name() throws Exception {
		test_declareBean("\u04ce");
	}

	void test_declareBean(String beanName) throws Exception {
		ScriptEngine se = new ScriptEngine("");
		Object bean = new Object();
		se.declareBean(beanName, bean);
		Object beanFromEngine = se.eval(beanName);
		assertEquals(bean, beanFromEngine);
	}

	@Test
	public void test_declareBean_overwrite_existing_bean() throws Exception {
		final String beanName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		Object bean1 = new Object();
		se.declareBean(beanName, bean1);
		Object bean2 = new Object();
		se.declareBean(beanName, bean2);
		Object beanFromEngine = se.eval(beanName);
		assertEquals(bean2, beanFromEngine);
	}

	@Test
	public void test_declareBean_ignores_class_parameter() throws Exception {
		final String beanName = "stringbean";
		ScriptEngine se = new ScriptEngine("");
		String bean = new String();
		se.declareBean(beanName, bean, java.util.Date.class);
		Object beanFromEngine = se.eval(beanName);
		assertEquals(bean, beanFromEngine);
	}

	@Test
	public void test_declareBean_overwrites_existing_bean_when_the_new_bean_is_null() throws Exception {
		final String beanName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		Object bean1 = new Object();
		se.declareBean(beanName, bean1);
		se.declareBean(beanName, null);
		Object beanFromEngine = se.eval(beanName);
		assertNull(beanFromEngine);
	}

	@Test
	public void test_declareBean_declare_same_bean_twice_has_no_effect() throws Exception {
		final String beanName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		Object bean = new Object();
		se.declareBean(beanName, bean);
		se.declareBean(beanName, bean);
		Object beanFromEngine = se.eval(beanName);
		assertEquals(bean, beanFromEngine);
	}

	@Test
	public void test_undeclareBean_undeclared_bean_is_null() throws Exception {
		final String beanName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		Object bean = new Object();
		se.declareBean(beanName, bean);
		se.undeclareBean(beanName);
		Object beanFromEngine = se.eval(beanName);
		assertNull(beanFromEngine);
	}

	@Test
	public void test_undeclareBean_non_existing_bean_has_no_effect() throws Exception {
		new ScriptEngine("").undeclareBean("nonexisting");
	}

	@Test
	public void test_popStackFrame_does_not_restore_zero_level_beans() throws Exception {
		/*
		 * Zero level beans are not saved by pushStackFrame() and not restored
		 * by popStackFrame(). This might look strange, but it is the status quo
		 * since release 5.1.
		 */
		final String beanName = "testbean";

		ScriptEngine se = new ScriptEngine("");

		String one = "one";
		String two = "two";

		se.declareBean(beanName, one);
		se.pushStackFrame();
		se.declareBean(beanName, two);
		se.popStackFrame();

		assertEquals(two, se.eval(beanName));
	}

	@Test
	public void test_popStackFrame_restores_non_zero_level_beans() throws Exception {
		final String beanName = "testbean";

		ScriptEngine se = new ScriptEngine("");

		String one = "one";
		String two = "two";

		se.pushStackFrame();
		se.declareBean(beanName, one);
		se.pushStackFrame();
		se.declareBean(beanName, two);
		se.popStackFrame();

		assertEquals(one, se.eval(beanName));
	}

	@Test
	public void test_eval_persists_variable_content() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.eval("var i = 123.0;");
		assertEquals(123.0, se.eval("i"));
	}

	@Test
	public void test_eval_function_call() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		assertEquals(3.0, se.eval("function increm( x ) { return x+1 } increm(2.0)"));
	}

	@Test
	public void test_call_previously_declared_function() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.exec("function increm( x ) { return x+1 }");
		assertEquals(3.0, se.call("increm", new Object[] { new Double(2.0) }));
	}

	@Test
	public void test_call_previously_declared_function_2() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.exec("function increm( x ) { return x+1 }");
		assertEquals(3.0, se.call("increm", new Object[] { new Double(2.0) }, true));
	}

	@Test(expected = Exception.class)
	public void test_call_non_declared_function() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.call("nonexisting", new Object[] {});
	}

	@Test(expected = Exception.class)
	public void test_call_non_declared_function_on_second_call() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.exec("function myfunc() {}");
		se.call("myfunc", new Object[] {});

		se.call("nonexisting", new Object[] {});
	}

	@Test(expected = Exception.class)
	public void test_call_non_declared_function_do_not_ignore_missing() throws Exception {
		final boolean ignoreMissingDeclaration = false;
		ScriptEngine se = new ScriptEngine("");
		se.call("nonexisting", new Object[] {}, ignoreMissingDeclaration);
	}

	@Test
	public void test_call_non_declared_function_ignore_missing() throws Exception {
		final boolean ignoreMissingDeclaration = true;
		ScriptEngine se = new ScriptEngine("");
		assertNull(se.call("nonexisting", new Object[] {}, ignoreMissingDeclaration));
	}

	@Test
	public void test_call_non_declared_function_ignore_missing_on_second_call() throws Exception {
		final boolean ignoreMissingDeclaration = true;
		ScriptEngine se = new ScriptEngine("");
		se.exec("function myfunc() {}");
		se.call("myfunc", new Object[] {});

		assertNull(se.call("nonexisting", new Object[] {}, ignoreMissingDeclaration));
	}

	@Test(expected = Exception.class)
	public void test_call_non_function_do_not_ignore_missing() throws Exception {
		final boolean ignoreMissingDeclaration = false;
		final String symbolName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		se.declareStaticBean(symbolName, new Object());
		se.call(symbolName, new Object[] {}, ignoreMissingDeclaration);
	}

	@Test(expected = Exception.class)
	public void test_call_non_function_do_not_ignore_missing_on_second_call() throws Exception {
		final boolean ignoreMissingDeclaration = false;
		final String symbolName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		se.exec("function myfunc() {}");
		se.call("myfunc", new Object[] {});

		se.declareStaticBean(symbolName, new Object());
		se.call(symbolName, new Object[] {}, ignoreMissingDeclaration);
	}

	@Test
	public void test_call_non_function_ignore_missing() throws Exception {
		final boolean ignoreMissingDeclaration = true;
		final String symbolName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		se.declareStaticBean(symbolName, new Object());
		assertNull(se.call(symbolName, new Object[] {}, ignoreMissingDeclaration));
	}

	@Test(expected = IOException.class)
	public void test_call_unwraps_java_io_IOException() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.exec("function complain( x ) { throw new java.io.IOException() }");
		se.call("complain", new Object[] {}, false);
	}

	@Test(expected = Exception.class)
	public void test_call_unwraps_thrown_string() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.exec("function complain( x ) { throw \"mymessage\"; }");
		se.call("complain", new Object[] {}, false);
	}

	@Test(expected = Exception.class)
	public void test_call_unwraps_java_lang_Error() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.exec("function complain( x ) { throw new java.lang.Error(); }");
		se.call("complain", new Object[] {}, false);
	}

	@Test
	public void test_eval_returns_null_for_empty_script() throws Exception {
		assertNull(new ScriptEngine("").eval(""));
	}

	@Test
	public void test_eval_interprets_script() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		assertEquals(125.0, se.eval("var i = 123.0; i+=2; i"));
	}

	@Test
	public void test_clear_removes_bean_declarations() throws Exception {
		final String beanName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		se.declareBean(beanName, new Object());
		se.clear();
		assertNull(se.eval(beanName));
	}

	@Test
	public void test_clear_does_not_remove_static_bean_declarations() throws Exception {
		final String beanName = "testbean";
		final Object bean = new Object();
		ScriptEngine se = new ScriptEngine("");
		se.declareStaticBean(beanName, bean);
		se.clear();
		assertEquals(bean, se.eval(beanName));
	}

	@Test
	public void test_clearAll_removes_bean_declarations() throws Exception {
		final String beanName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		se.declareBean(beanName, new Object());
		se.clearAll();
		assertNull(se.eval(beanName));
	}

	@Test
	public void test_clearAll_removes_static_bean_declarations() throws Exception {
		final String beanName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		se.declareStaticBean(beanName, new Object());
		se.clearAll();
		assertNull(se.eval(beanName));
	}

	@Test
	public void test_terminate_clears_the_underlying_jsengine() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.terminate();
		assertNull(se.getJsengine());
	}

	@Test
	public void test_getScriptPrefix() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		assertEquals("//@script", se.getScriptPrefix());
	}

	@Test
	public void test_includeScript_from_file() throws Exception {

		final String scriptInFile = "++i;";
		File jsFile = createTempScriptFile(scriptInFile);

		ScriptEngine se = new ScriptEngine("");
		se.exec("var i = 2.0;");
		se.includeScript(jsFile.getAbsolutePath());
		assertEquals(3.0, se.eval("i"));
	}

	@Test(expected = Exception.class)
	public void test_loadScript_from_server_missing_script_config() throws Exception {
		RSInterface server = createMockServerForGetScript(null, null, false);
		ScriptEngine se = new ScriptEngine("");
		se.loadScript(server, "", "", false);
	}

	@Test
	public void test_loadScript_from_server() throws Exception {
		RSInterface server = createMockServerForGetScript("++i", null, true);
		ScriptEngine se = new ScriptEngine("");
		se.exec("var i = 2.0;");
		se.loadScript(server, "", "myscript", false);
		assertEquals(3.0, se.eval("i"));
	}

	@Test
	public void test_loadScript_from_config_autoinclude_is_true_forceInclude_is_false() throws Exception {
		ScriptConfig sc = createScriptConfig("++i", null, true);
		ScriptEngine se = new ScriptEngine("");
		se.exec("var i = 2.0;");
		se.loadScript("", sc, false);
		assertEquals(3.0, se.eval("i"));
	}

	@Test
	public void test_loadScript_from_config_autoinclude_is_false_forceInclude_is_false() throws Exception {
		ScriptConfig sc = createScriptConfig("++i", null, false);
		ScriptEngine se = new ScriptEngine("");
		se.exec("var i = 2.0;");
		se.loadScript("", sc, false);
		assertEquals(2.0, se.eval("i"));
	}

	@Test
	public void test_loadScript_from_config_autoinclude_is_false_forceInclude_is_true() throws Exception {
		ScriptConfig sc = createScriptConfig("++i", null, true);
		ScriptEngine se = new ScriptEngine("");
		se.exec("var i = 2.0;");
		se.loadScript("", sc, false);
		assertEquals(3.0, se.eval("i"));
	}

	/**
	 * Verify defect 1855 (Impl. incl. Scripts get incl./evaluated twice if exp.
	 * included).
	 */
	@Test
	public void test_loadScript_from_config_multiple_times_evaluates_the_script_only_once() throws Exception {
		ScriptConfig sc = createScriptConfig("++i", null, true);
		ScriptEngine se = new ScriptEngine("");
		se.exec("var i = 2.0;");
		se.loadScript("", sc, true);
		se.loadScript("", sc, true);
		se.loadScript("", sc, true);
		assertEquals(3.0, se.eval("i"));
	}

	@Test
	public void test_loadScript_from_config_with_includefiles() throws Exception {
		File jsFile = createTempScriptFile("++i");
		ScriptConfig sc = createScriptConfig("++i", jsFile.getAbsolutePath(), true);
		ScriptEngine se = new ScriptEngine("");
		se.exec("var i = 2.0;");
		se.loadScript("", sc, true);
		assertEquals(4.0, se.eval("i"));
	}

	@Test
	public void test_includeAllScripts_loads_autoinclude_scripts_from_config() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.exec("var i = 0.0;");

		final String xmlConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<MetamergeConfig version=\"7.0\">"
				+ "	<Folder name=\"Scripts\">" + "		<Script name=\"myscript\">"
				+ "			<parameter name=\"autoInclude\">true</parameter>" + "			<parameter name=\"includeFiles\"/>"
				+ "			<parameter name=\"script\">i += 125.0;</parameter>" + "		</Script>" + "	</Folder>" + "</MetamergeConfig>";
		MetamergeConfig mc = loadMetamergeConfig(xmlConfig);

		se.includeAllScripts(mc);
		assertEquals(125.0, se.eval("i"));
	}

	@Test
	public void test_includeAllScripts_does_not_load_non_autoinclude_scripts_from_config() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.exec("var i = 0.0;");

		final String xmlConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<MetamergeConfig version=\"7.0\">"
				+ "	<Folder name=\"Scripts\">" + "		<Script name=\"myscript\">"
				+ "			<parameter name=\"autoInclude\">false</parameter>" + "			<parameter name=\"includeFiles\"/>"
				+ "			<parameter name=\"script\">i += 125.0;</parameter>" + "		</Script>" + "	</Folder>" + "</MetamergeConfig>";
		MetamergeConfig mc = loadMetamergeConfig(xmlConfig);

		se.includeAllScripts(mc);
		assertEquals(0.0, se.eval("i"));
	}

	@Test
	public void test_includeAllScripts_loads_autoinclude_scripts_from_referenced_config() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.exec("var i = 0.0;");

		String referencedConfigName = UUID.randomUUID().toString();

		final String xmlConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<MetamergeConfig version=\"7.0\">"
				+ "	<Folder name=\"Includes\">" + "		<Include name=\"" + referencedConfigName + "\">"
				+ "			<parameter name=\"java.naming.provider.url\">" + referencedConfigName + "</parameter>" + "		</Include>"
				+ "	</Folder>" + "</MetamergeConfig>";
		MetamergeConfig mc = loadMetamergeConfig(xmlConfig);

		final String xmlConfigOther = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<MetamergeConfig version=\"7.0\">"
				+ "	<Folder name=\"Scripts\">" + "		<Script name=\"myscript\">"
				+ "			<parameter name=\"autoInclude\">true</parameter>" + "			<parameter name=\"includeFiles\"/>"
				+ "			<parameter name=\"script\">i += 125.0;</parameter>" + "		</Script>" + "	</Folder>" + "</MetamergeConfig>";
		MetamergeConfig mcOther = loadMetamergeConfig(xmlConfigOther);
		MetamergeConfigFactory.registerNamespace(referencedConfigName, mcOther);

		se.includeAllScripts(mc);
		assertEquals(125.0, se.eval("i"));

		// cleanup
		MetamergeConfigFactory.unregisterNamespace(referencedConfigName);
	}

	/**
	 * Verify defect 2760 (53: "Includes" Connector Library Error).
	 */
	@Test
	public void test_includeAllScripts_does_not_chase_config_reference_loop() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.exec("var i = 0.0;");

		/*
		 * Have two configurations that include each other thus forming a
		 * reference loop.
		 */
		String referencedConfigName = UUID.randomUUID().toString();
		String referencedConfigNameOther = UUID.randomUUID().toString();

		final String xmlConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<MetamergeConfig version=\"7.0\">"
				+ "	<Folder name=\"Includes\">" + "		<Include name=\"" + referencedConfigNameOther + "\">"
				+ "			<parameter name=\"java.naming.provider.url\">" + referencedConfigNameOther + "</parameter>" + "		</Include>"
				+ "	</Folder>" + "</MetamergeConfig>";
		MetamergeConfig mc = loadMetamergeConfig(xmlConfig);
		MetamergeConfigFactory.registerNamespace(referencedConfigName, mc);

		final String xmlConfigOther = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<MetamergeConfig version=\"7.0\">"
				+ "	<Folder name=\"Scripts\">" + "		<Script name=\"myscript\">"
				+ "			<parameter name=\"autoInclude\">true</parameter>" + "			<parameter name=\"includeFiles\"/>"
				+ "			<parameter name=\"script\">i += 125.0;</parameter>" + "		</Script>" + "	</Folder>"
				+ "	<Folder name=\"Includes\">" + "		<Include name=\"" + referencedConfigName + "\">"
				+ "			<parameter name=\"java.naming.provider.url\">" + referencedConfigName + "</parameter>" + "		</Include>"
				+ "	</Folder>" + "</MetamergeConfig>";
		MetamergeConfig mcOther = loadMetamergeConfig(xmlConfigOther);
		MetamergeConfigFactory.registerNamespace(referencedConfigNameOther, mcOther);

		se.includeAllScripts(mc);
		assertEquals(125.0, se.eval("i"));

		// cleanup
		MetamergeConfigFactory.unregisterNamespace(referencedConfigName);
		MetamergeConfigFactory.unregisterNamespace(referencedConfigNameOther);
	}

	@Test
	public void test_includeAllScripts_cannot_find_referenced_config() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.exec("var i = 0.0;");

		String referencedConfigName = UUID.randomUUID().toString();

		final String xmlConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<MetamergeConfig version=\"7.0\">"
				+ "	<Folder name=\"Includes\">" + "		<Include name=\"" + referencedConfigName + "\" />" + "	</Folder>"
				+ "</MetamergeConfig>";
		MetamergeConfig mc = loadMetamergeConfig(xmlConfig);

		boolean reportedError = false;
		try {
			se.includeAllScripts(mc);
		} catch (Exception ex) {
			reportedError = true;
		}
		assertTrue(reportedError);
	}

	@Test
	public void test_getDebug() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		assertFalse(se.getDebug());
	}

	@Test
	public void test_interpret_executes_script() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.interpret("var i = 123.0; ++i;");
		assertEquals(124.0, se.eval("i"));
	}

	@Test
	public void test_interpret_executes_script_2() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.interpret("var i = 123.0; ++i;", false);
		assertEquals(124.0, se.eval("i"));
	}

	@Test
	public void test_interpret_executes_script_3() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.interpret("var i = 123.0; ++i;", false, "");
		assertEquals(124.0, se.eval("i"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_interpret_unwraps_script_exception() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.interpret("throw new java.lang.IllegalArgumentException();");
	}

	@Test
	public void test_getCompiledExpression_preserves_script_text() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		final String script = "var i = 1.0;";
		assertEquals(script, se.getCompiledExpression(script).getExpr());
	}

	@Test
	public void test_lastException_for_thrown_exception() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		try {
			se.interpret("throw new java.lang.IllegalArgumentException()");
		} catch (IllegalArgumentException iae) {
			assertNotNull(se.lastException(iae));
		}
	}

	/**
	 * Verify defect 4875 (IBMJS: Calling function before it is defined fails).
	 */
	@Test
	public void test_invoke_function_declared_later_in_the_same_script() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		// this is what the ScriptComponent uses:
		se.interpret("xys(); function xys() {}", false, "myname");
	}

	/**
	 * Verify defect 5962 (JS throw behaves differently in the jsengine.).
	 */
	@Test
	public void test_string_thrown_in_script_is_preserved_in_the_message_of_the_resulting_exception() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		boolean reportedError = false;
		try {
			se.interpret("throw \"mystring\";", false, "myname");
		} catch (Exception ex) {
			assertTrue(ex.getMessage().contains("mystring"));
			reportedError = true;
		}
		assertTrue(reportedError);
	}

	/**
	 * Verify defect 11879 (70-SVT:sendEmailFC throws err on reading non-eng
	 * chars).
	 */
	@Test
	public void test_parse_script_with_non_ascii_chars() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		final String nonASCIItext = "Text: \u02dc";
		assertEquals(nonASCIItext, se.eval("var str = \"" + nonASCIItext + "\"; str"));
	}

	@Test
	public void test_lastException_for_non_existing_exception() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		assertNull(se.lastException(new Exception()));
	}

	@Test
	public void test_isFunctionDefined_returns_true_for_declared_function() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.eval("function myfunc() {}");
		assertTrue(se.isFunctionDefined("myfunc"));
	}

	@Test
	public void test_isFunctionDefined_returns_false_for_non_existing_function() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		assertFalse(se.isFunctionDefined("myfunc"));
	}

	@Test
	public void test_isFunctionDefined_returns_false_for_bean_name() throws Exception {
		final String beanName = "testbean";
		ScriptEngine se = new ScriptEngine("");
		se.declareBean(beanName, new Object());
		assertFalse(se.isFunctionDefined(beanName));
	}

	@Test
	public void test_getJsengine() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		assertNotNull(se.getJsengine());
	}

	@Test
	public void test_getJSOptions() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		assertNotNull(se.getJSOptions());
	}

	@Test
	public void test_addDebugListener_listener_gets_notified_for_each_js_statement() throws Exception {
		RSInterface server = null;
		boolean debug = true;
		ScriptEngine se = new ScriptEngine("", server, debug);
		CountingDebugListener listener = new CountingDebugListener();
		se.addDebugListener(listener);
		se.interpret("var i = 1.0; ++i;");
		assertEquals(2, listener.getCallCount());
	}

	@Test
	public void test_removeDebugListener_removed_listener_is_not_notified() throws Exception {
		RSInterface server = null;
		boolean debug = true;
		ScriptEngine se = new ScriptEngine("", server, debug);
		CountingDebugListener listener = new CountingDebugListener();
		se.addDebugListener(listener);
		se.removeDebugListener(listener);
		se.interpret("var i = 1.0; ++i;");
		assertEquals(0, listener.getCallCount());
	}

	/**
	 * Test the standard 'slice' Javascript method on java.lang.String:
	 * https://developer
	 * .mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/String/slice
	 */
	@Test
	public void test_on_java_lang_String_slice_whole_string_using_one_arg() throws Exception {
		final String script = "var str = new java.lang.String(\"my string\"); str.slice(0);";
		ScriptEngine se = new ScriptEngine("");
		assertEquals("my string", se.eval(script));
	}

	/**
	 * Test the standard 'slice' Javascript method on java.lang.String:
	 * https://developer
	 * .mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/String/slice
	 */
	@Test
	public void test_on_java_lang_String_slice_from_the_middle_to_the_end_using_one_arg() throws Exception {
		final String script = "var str = new java.lang.String(\"my string\"); str.slice(3);";
		ScriptEngine se = new ScriptEngine("");
		assertEquals("string", se.eval(script));
	}

	/**
	 * Test the standard 'slice' Javascript method on java.lang.String:
	 * https://developer
	 * .mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/String/slice
	 */
	@Test
	public void test_on_java_lang_String_slice_last_character_using_one_arg() throws Exception {
		final String script = "var str = new java.lang.String(\"my string\"); str.slice(8);";
		ScriptEngine se = new ScriptEngine("");
		assertEquals("g", se.eval(script));
	}

	/**
	 * Test the standard 'slice' Javascript method on java.lang.String:
	 * https://developer
	 * .mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/String/slice
	 */
	@Test
	public void test_on_java_lang_String_slice_first_character_using_two_args() throws Exception {
		final String script = "var str = new java.lang.String(\"my string\"); str.slice(0, 1);";
		ScriptEngine se = new ScriptEngine("");
		assertEquals("m", se.eval(script));
	}

	/**
	 * Test the standard 'slice' Javascript method on java.lang.Strings:
	 * https://
	 * developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects
	 * /String/slice
	 */
	@Test
	public void test_on_java_lang_String_slice_middle_part_using_two_args() throws Exception {
		final String script = "var str = new java.lang.String(\"my string\"); str.slice(3, 6);";
		ScriptEngine se = new ScriptEngine("");
		assertEquals("str", se.eval(script));
	}

	/**
	 * Test the standard 'slice' Javascript method on java.lang.String:
	 * https://developer
	 * .mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/String/slice
	 */
	@Test
	public void test_on_java_lang_String_slice_last_character_using_two_args() throws Exception {
		final String script = "var str = new java.lang.String(\"my string\"); str.slice(8, 9);";
		ScriptEngine se = new ScriptEngine("");
		assertEquals("g", se.eval(script));
	}

	/**
	 * Test the standard 'slice' Javascript method on java.lang.String:
	 * https://developer
	 * .mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/String/slice
	 */
	@Test
	public void test_on_java_lang_String_slice_middle_part_using_two_args_and_last_arg_is_negative() throws Exception {
		final String script = "var str1 = \"The morning is upon us.\"; var str2 = str1.slice(4, -2); str2;";
		ScriptEngine se = new ScriptEngine("");
		assertEquals("morning is upon u", se.eval(script));
	}

	@Test
	public void test_on_java_lang_String_length_method() throws Exception {
		final String script = "var str = \"my string\"; str.length()";
		assertEquals(9.0, new ScriptEngine("").eval(script));
	}

	/**
	 * Test the standard 'search' Javascript method on java.lang.String:
	 * https://
	 * developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects
	 * /String/search
	 */
	@Test
	public void test_on_java_lang_String_search_regexp_string_with_match() throws Exception {
		final String script = "var str = \"aaabc\"; str.search(\"ab+c\")";
		assertEquals(2.0, new ScriptEngine("").eval(script));
	}

	/**
	 * Test the standard 'search' Javascript method on java.lang.String:
	 * https://
	 * developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects
	 * /String/search
	 */
	@Test
	public void test_on_java_lang_String_search_regexp_object_with_match() throws Exception {
		final String script = "var str = \"AAABC\"; str.search(new RegExp(\"ab+c\", \"i\"))";
		assertEquals(2.0, new ScriptEngine("").eval(script));
	}

	/**
	 * Test the standard 'search' Javascript method on java.lang.String:
	 * https://
	 * developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects
	 * /String/search
	 */
	@Test
	public void test_on_java_lang_String_search_regexp_string_with_no_match() throws Exception {
		final String script = "var str = \"aac\"; str.search(\"ab+c\")";
		assertEquals(-1.0, new ScriptEngine("").eval(script));
	}

	/**
	 * Test the standard 'substr' Javascript method on java.lang.String. Use the
	 * examples on:
	 * https://developer.mozilla.org/en/Core_JavaScript_1.5_Reference
	 * /Global_Objects/String/substr
	 */
	@Test
	public void test_on_java_lang_String_substr() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.eval("var str = \"abcdefghij\";");
		assertEquals("bc", se.eval("str.substr(1, 2)"));
		assertEquals("hi", se.eval("str.substr(-3, 2)"));
		assertEquals("hij", se.eval("str.substr(-3)"));
		assertEquals("bcdefghij", se.eval("str.substr(1)"));
		assertEquals("ab", se.eval("str.substr(-20, 2)"));
		assertEquals("", se.eval("str.substr(20, 2)"));
	}

	@Test
	public void test_on_java_util_List_access_element_using_array_subscript() throws Exception {
		final String script = "var list = new java.util.ArrayList(); list.add(123.0); list[0]";
		assertEquals(123.0, new ScriptEngine("").eval(script));
	}

	/**
	 * Verify defect 6644 (61: IBMJS: ArrayList compatibility problem).
	 */
	@Test
	public void test_on_list_literal_access_element_using_array_subscript() throws Exception {

		String script = "";
		script += "var monitorServerVector = new Packages.java.util.Vector();";
		script += "var tempTriplet;";
		script += "var monitorAttribute;";
		script += "tempTriplet = [\"monitorName\", \"eventTargetAddress\", \"eventTargetPort\", \"isSSL\"];";
		script += "monitorServerVector.add(tempTriplet);";
		script += "attributePortAddressTriplet = monitorServerVector.elementAt(0);";
		script += "monitorAttribute = attributePortAddressTriplet[1];";

		new ScriptEngine("").eval(script);
	}

	/**
	 * Verify defect 11586 (70-BETA: JavaScript Engine fails with some function
	 * names).
	 */
	@Test
	public void test_use_function_which_contains_print_in_its_name() throws Exception {

		String script = "";
		script += "function println( o ) { }";
		script += "println(\"Calling my println function...\");";
		script += "function print( o ) { }";
		script += "print(\"Calling my print function...\");";

		new ScriptEngine("").eval(script);
	}

	/**
	 * Verify defect 9877 (70-DEV: A taskCallBlock object is recognized as a
	 * Entry object.).
	 */
	@Test
	public void test_on_TaskCallBlock_invoke_method_which_is_not_declared_in_Entry_class() throws Exception {
		TaskCallBlock tcb = new TaskCallBlock();
		evalExpression(tcb, "obj.getResultEntry()");
	}

	/**
	 * Verify feature 12442 (70-BETA: QP539: Concatenating two attributes).
	 */
	@Test
	public void test_add_two_attributes_and_string_listeral() throws Exception {
		Attribute a = new Attribute("a");
		a.addValue("A");

		Attribute b = new Attribute("b");
		b.addValue("B");

		ScriptEngine se = new ScriptEngine("");
		se.declareBean("a", a);
		se.declareBean("b", b);
		assertEquals("AB.", se.eval("a+b+\".\""));
	}

	/**
	 * Verify defect 12872 (70-SUPINT: Recursive assignment loop with stack
	 * overflow).
	 */
	@Test
	public void test_compare_attribute_against_string_literal() throws Exception {

		String script = "";
		script += "work.isIBM = \"Y\";";
		script += "b_isIBM = work.isIBM == \"Y\" ? true : false;";
		script += "b_isIBM";

		Entry work = new Entry();
		ScriptEngine se = new ScriptEngine("");
		se.declareBean("work", work);
		assertEquals(true, se.eval(script));
	}

	@Test
	public void test_compare_attribute_against_number_literal() throws Exception {

		String script = "";
		script += "work.isIBM = 123.0;";
		script += "b_isIBM = work.isIBM == 123.0 ? true : false;";
		script += "b_isIBM";

		Entry work = new Entry();
		ScriptEngine se = new ScriptEngine("");
		se.declareBean("work", work);
		assertEquals(true, se.eval(script));
	}

	@Test
	public void test_compare_attribute_against_boolean_literal() throws Exception {

		String script = "";
		script += "work.isIBM = false;";
		script += "b_isIBM = work.isIBM == false ? true : false;";
		script += "b_isIBM";

		Entry work = new Entry();
		ScriptEngine se = new ScriptEngine("");
		se.declareBean("work", work);
		assertEquals(true, se.eval(script));
	}

	@Test
	public void test_on_java_util_List_invoke_size_method() throws Exception {
		final String script = "var list = new java.util.ArrayList(); list.add(123.0); list.size()";
		assertEquals(1.0, new ScriptEngine("").eval(script));
	}

	@Test
	public void test_on_entry_access_non_existing_property_using_localname() throws Exception {
		Entry entry = new Entry();
		test_on_object_access_non_existing_property_using_localname(entry);
	}

	@Test
	public void test_on_entry_access_property_which_has_no_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		String value = "myvalue";
		entry.setProperty("myprop", value);
		assertEquals(value, evalExpression(entry, "obj.@myprop"));
	}

	@Test
	public void test_on_entry_access_non_existing_child_attribute_using_localname() throws Exception {
		Entry entry = new Entry();
		test_on_object_access_non_existing_child_attribute_using_localname(entry);
	}

	@Test
	public void test_on_entry_access_single_child_attribute_which_has_no_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		test_on_object_access_single_child_attribute_which_has_no_namespace_using_localname(entry, entry);
	}

	@Test
	public void test_on_entry_access_single_child_attribute_which_has_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		test_on_object_access_single_child_attribute_which_has_namespace_using_localname(entry, entry);
	}

	@Test
	public void test_on_entry_access_multiple_child_attributes_which_have_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		test_on_object_access_multiple_child_attributes_which_have_namespace_using_localname(entry, entry);
	}

	@Test
	public void test_on_entry_access_non_existing_child_attribute_using_prefix_and_localname() throws Exception {
		Entry entry = new Entry();
		test_on_object_access_non_existing_child_attribute_using_prefix_and_localname(entry);
	}

	@Test
	public void test_on_entry_access_single_child_attribute_which_has_namespace_using_prefix_and_localname() throws Exception {
		Entry entry = new Entry();
		test_on_object_access_single_child_attribute_which_has_namespace_using_prefix_and_localname(entry, entry);
	}

	@Test
	public void test_on_entry_access_non_existing_child_attribute_using_namespace_uri_and_localname() throws Exception {
		Entry entry = new Entry();
		test_on_object_access_non_existing_child_attribute_using_namespace_uri_and_localname(entry);
	}

	@Test
	public void test_on_entry_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname()
			throws Exception {
		Entry entry = new Entry();
		test_on_object_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname(entry, entry);
	}

	@Test
	public void test_on_entry_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname()
			throws Exception {
		Entry entry = new Entry();
		test_on_object_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname(entry, entry);
	}

	@Test
	public void test_on_entry_access_non_existing_child_attribute_using_namespace_uri_and_localname_prefix_is_ignored()
			throws Exception {
		Entry entry = new Entry();
		test_on_object_access_non_existing_child_attribute_using_namespace_uri_and_localname_prefix_is_ignored(entry);
	}

	@Test
	public void test_on_entry_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname_prefix_is_ignored()
			throws Exception {
		Entry entry = new Entry();
		test_on_object_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname_prefix_is_ignored(entry,
				entry);
	}

	@Test
	public void test_on_entry_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname_prefix_is_ignored()
			throws Exception {
		Entry entry = new Entry();
		test_on_object_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname_prefix_is_ignored(
				entry, entry);
	}

	@Test
	public void test_on_attribute_access_non_existing_property_using_localname() throws Exception {
		Attribute attribute = new Attribute();
		test_on_object_access_non_existing_property_using_localname(attribute);
	}

	@Test
	public void test_on_attribute_access_property_which_has_no_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_property_which_has_no_namespace_using_localname(attribute, entry);
	}

	@Test
	public void test_on_attribute_access_property_which_has_namespace_using_prefix_and_localname() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_property_which_has_namespace_using_prefix_and_localname(attribute, entry);
	}

	@Test
	public void test_on_attribute_access_non_existing_property_using_prefix_and_localname() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_non_existing_property_using_prefix_and_localname(attribute);
	}

	@Test
	public void test_on_attribute_access_property_which_has_namespace_using_namespace_uri_and_localname() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_property_which_has_namespace_using_namespace_uri_and_localname(attribute, entry);
	}

	@Test
	public void test_on_attribute_access_non_existing_property_using_namespace_uri_and_localname() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_non_existing_property_using_namespace_uri_and_localname(attribute);
	}

	@Test
	public void test_on_attribute_access_non_existing_property_using_namespace_uri_and_localname_ignores_prefix() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_non_existing_property_using_namespace_uri_and_localname_ignores_prefix(attribute);
	}

	@Test
	public void test_on_attribute_access_value_using_array_subscript() throws Exception {
		Attribute attribute = new Attribute();
		Object value = new Object();
		attribute.addValue(value);
		assertEquals(value, evalExpression(attribute, "obj[0]"));
	}

	@Test
	public void test_on_attribute_access_non_existing_child_attribute_using_localname() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_non_existing_child_attribute_using_localname(attribute);
	}

	@Test
	public void test_on_attribute_access_single_child_attribute_which_has_no_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_single_child_attribute_which_has_no_namespace_using_localname(attribute, entry);
	}

	@Test
	public void test_on_attribute_access_single_child_attribute_which_has_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_single_child_attribute_which_has_namespace_using_localname(attribute, entry);
	}

	@Test
	public void test_on_attribute_access_multiple_child_attributes_which_have_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_multiple_child_attributes_which_have_namespace_using_localname(attribute, entry);
	}

	@Test
	public void test_on_attribute_access_non_existing_child_attribute_using_prefix_and_localname() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_non_existing_child_attribute_using_prefix_and_localname(attribute);
	}

	@Test
	public void test_on_attribute_access_single_child_attribute_which_has_namespace_using_prefix_and_localname() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_single_child_attribute_which_has_namespace_using_prefix_and_localname(attribute, entry);
	}

	@Test
	public void test_on_attribute_access_multiple_child_attributes_which_have_namespace_using_prefix_and_localname()
			throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_multiple_child_attributes_which_have_namespace_using_prefix_and_localname(attribute, entry);
	}

	@Test
	public void test_on_attribute_access_non_existing_child_attribute_using_namespace_uri_and_localname() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_non_existing_child_attribute_using_namespace_uri_and_localname(attribute);
	}

	@Test
	public void test_on_attribute_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname()
			throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname(attribute, entry);
	}

	@Test
	public void test_on_attribute_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname()
			throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname(attribute, entry);
	}

	@Test
	public void test_on_attribute_access_non_existing_child_attribute_using_namespace_uri_and_localname_prefix_is_ignored()
			throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_non_existing_child_attribute_using_namespace_uri_and_localname_prefix_is_ignored(attribute);
	}

	@Test
	public void test_on_attribute_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname_prefix_is_ignored()
			throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname_prefix_is_ignored(
				attribute, entry);
	}

	@Test
	public void test_on_attribute_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname_prefix_is_ignored()
			throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		test_on_object_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname_prefix_is_ignored(
				attribute, entry);
	}

	@Test
	public void test_on_nodelist_access_element_using_array_subscript() throws Exception {
		TestNodeList nodeList = new TestNodeList();
		Attribute element = new Attribute();
		nodeList.add(element);
		assertEquals(element, evalExpression(nodeList, "obj[0]"));
	}

	@Test
	public void test_on_nodelist_access_non_existing_property_using_localname() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_non_existing_property_using_localname(nodeList);
	}

	@Test
	public void test_on_nodelist_access_property_which_has_no_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_property_which_has_no_namespace_using_localname(nodeList, entry);
	}

	@Test
	public void test_on_nodelist_access_property_which_has_namespace_using_prefix_and_localname() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_property_which_has_namespace_using_prefix_and_localname(nodeList, entry);
	}

	@Test
	public void test_on_nodelist_access_non_existing_property_using_prefix_and_localname() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_non_existing_property_using_prefix_and_localname(nodeList);
	}

	@Test
	public void test_on_nodelist_access_property_which_has_namespace_using_namespace_uri_and_localname() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_property_which_has_namespace_using_namespace_uri_and_localname(nodeList, entry);
	}

	@Test
	public void test_on_nodelist_access_non_existing_property_using_namespace_uri_and_localname() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_non_existing_property_using_namespace_uri_and_localname(nodeList);
	}

	@Test
	public void test_on_nodelist_access_non_existing_property_using_namespace_uri_and_localname_ignores_prefix() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_non_existing_property_using_namespace_uri_and_localname_ignores_prefix(nodeList);
	}

	@Test
	public void test_on_nodelist_access_non_existing_child_attribute_using_localname() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_non_existing_child_attribute_using_localname(nodeList);
	}

	@Test
	public void test_on_nodelist_access_single_child_attribute_which_has_no_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_single_child_attribute_which_has_no_namespace_using_localname(nodeList, entry);
	}

	@Test
	public void test_on_nodelist_access_single_child_attribute_which_has_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_single_child_attribute_which_has_namespace_using_localname(nodeList, entry);
	}

	@Test
	public void test_on_nodelist_access_multiple_child_attributes_which_have_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_multiple_child_attributes_which_have_namespace_using_localname(nodeList, entry);
	}

	@Test
	public void test_on_nodelist_access_non_existing_child_attribute_using_prefix_and_localname() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_non_existing_child_attribute_using_prefix_and_localname(nodeList);
	}

	@Test
	public void test_on_nodelist_access_single_child_attribute_which_has_namespace_using_prefix_and_localname() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_single_child_attribute_which_has_namespace_using_prefix_and_localname(nodeList, entry);
	}

	@Test
	public void test_on_nodelist_access_multiple_child_attributes_which_have_namespace_using_prefix_and_localname()
			throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_multiple_child_attributes_which_have_namespace_using_prefix_and_localname(nodeList, entry);
	}

	@Test
	public void test_on_nodelist_access_non_existing_child_attribute_using_namespace_uri_and_localname() throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_non_existing_child_attribute_using_namespace_uri_and_localname(nodeList);
	}

	@Test
	public void test_on_nodelist_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname()
			throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname(nodeList, entry);
	}

	@Test
	public void test_on_nodelist_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname()
			throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname(nodeList, entry);
	}

	@Test
	public void test_on_nodelist_access_non_existing_child_attribute_using_namespace_uri_and_localname_prefix_is_ignored()
			throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_non_existing_child_attribute_using_namespace_uri_and_localname_prefix_is_ignored(nodeList);
	}

	@Test
	public void test_on_nodelist_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname_prefix_is_ignored()
			throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname_prefix_is_ignored(
				nodeList, entry);
	}

	@Test
	public void test_on_nodelist_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname_prefix_is_ignored()
			throws Exception {
		Entry entry = new Entry();
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(entry.createElement("attribute"));
		test_on_object_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname_prefix_is_ignored(
				nodeList, entry);
	}

	@Test
	public void test_on_nodelist_access_multiple_properties_which_have_no_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		Attr property1 = entry.createAttribute("myprop");
		Attr property2 = entry.createAttribute("myprop");
		Element elem1 = entry.createElement("attribute");
		elem1.setAttributeNode(property1);
		Element elem2 = entry.createElement("attribute");
		elem2.setAttributeNode(property2);
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(elem1);
		nodeList.add(elem2);

		Object result = evalExpression(nodeList, "obj.@myprop");
		assertTrue(result instanceof NodeList);
		assertNodeListEquals((NodeList) result, property1, property2);
	}

	@Test
	public void test_method_takes_precedence_when_name_is_the_same_as_entry_attribute() throws Exception {
		Entry entry = new Entry();
		entry.newAttribute("size");
		assertEquals(1.0, evalExpression(entry, "obj.size()"));
	}

	@Test
	public void test_entry_is_not_converted_to_hierarchical_when_no_namespaces_are_used() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.newAttribute("http.body");
		assertEquals(attribute, evalExpression(entry, "obj[\"http.body\"]"));
	}

	@Test
	public void test_via_assignment_on_entry_change_value_of_property() throws Exception {
		Entry entry = new Entry();
		entry.setProperty("myprop", "oldvalue");
		evalExpression(entry, "obj.@myprop=\"newvalue\";");
		assertEquals("newvalue", entry.getProperty("myprop"));
	}

	@Test
	public void test_via_assignment_on_entry_create_new_property() throws Exception {
		Entry entry = new Entry();
		evalExpression(entry, "obj.@newprop=\"value\";");
		assertEquals("value", entry.getProperty("newprop"));
	}

	@Test
	public void test_via_assignment_on_entry_change_value_of_attribute_which_has_no_namespace() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.newAttribute("a");
		attribute.addValue("oldvalue");
		evalExpression(entry, "obj.a=\"newvalue\";");
		assertEquals("newvalue", attribute.getValue(0));
	}

	@Test
	public void test_via_assignment_on_entry_change_value_of_attribute_which_has_namespace_using_prefix_and_localname()
			throws Exception {
		test_via_assignment_on_entry_change_value_of_attribute_which_has_namespace("obj[\"test:a\"]=\"newvalue\";");
	}

	@Test
	public void test_via_assignment_on_entry_change_value_of_attribute_which_has_namespace_using_namespace_uri_and_localname()
			throws Exception {
		test_via_assignment_on_entry_change_value_of_attribute_which_has_namespace("obj[\"{http://www.example.com}a\"]=\"newvalue\";");
	}

	@Test
	public void test_via_assignment_on_entry_change_value_of_attribute_which_has_namespace_using_namespace_uri_prefix_and_localname()
			throws Exception {
		test_via_assignment_on_entry_change_value_of_attribute_which_has_namespace("obj[\"{http://www.example.com}test:a\"]=\"newvalue\";");
	}

	private void test_via_assignment_on_entry_change_value_of_attribute_which_has_namespace(String expression) throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElementNS("http://www.example.com", "test:a");
		attribute.addValue("oldvalue");
		entry.appendChild(attribute);
		evalExpression(entry, expression);
		assertEquals("newvalue", attribute.getValue(0));
	}

	@Test
	public void test_via_assignment_on_entry_create_attribute_with_namespace() throws Exception {
		Entry entry = new Entry();
		evalExpression(entry, "obj[\"{http://www.example.com}test:a\"]=\"newvalue\";");
		Attribute attribute = entry.getAttribute("test:a");
		assertNotNull(attribute);
		assertEquals("http://www.example.com", attribute.getNamespaceURI());
		assertEquals("newvalue", attribute.getValue(0));
	}

	@Test
	public void test_via_assignment_on_attribute_change_value_of_property_which_has_no_namespace_using_localname() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		entry.appendChild(attribute);
		attribute.setAttribute("myprop", "oldvalue");
		evalExpression(attribute, "obj.@myprop=\"newvalue\";");
		assertEquals("newvalue", attribute.getAttribute("myprop"));
	}

	@Test
	public void test_via_assignment_on_attribute_change_value_of_property_which_has_namespace_using_prefix_and_localname()
			throws Exception {
		test_via_assignment_on_attribute_change_value_of_property_which_has_namespace("obj[\"@test:myprop\"]=\"newvalue\";");
	}

	@Test
	public void test_via_assignment_on_attribute_change_value_of_property_which_has_namespace_using_namespace_uri_and_localname()
			throws Exception {
		test_via_assignment_on_attribute_change_value_of_property_which_has_namespace("obj[\"@{http://www.example.com}myprop\"]=\"newvalue\";");
	}

	@Test
	public void test_via_assignment_on_attribute_change_value_of_property_which_has_namespace_using_namespace_uri_prefix_and_localname()
			throws Exception {
		test_via_assignment_on_attribute_change_value_of_property_which_has_namespace("obj[\"@{http://www.example.com}myprop\"]=\"newvalue\";");
	}

	private void test_via_assignment_on_attribute_change_value_of_property_which_has_namespace(String expression) throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		entry.appendChild(attribute);
		attribute.setAttributeNS("http://www.example.com", "test:myprop", "oldvalue");
		evalExpression(attribute, expression);
		assertEquals("newvalue", attribute.getAttributeNS("http://www.example.com", "myprop"));
	}

	@Test
	public void test_via_assignment_on_attribute_create_new_property_which_has_namespace() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		entry.appendChild(attribute);
		evalExpression(attribute, "obj[\"@{http://www.example.com}test:myprop\"]=\"newvalue\";");
		assertEquals("newvalue", attribute.getAttributeNS("http://www.example.com", "myprop"));
	}

	@Test
	public void test_via_assignment_on_attribute_replace_value() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		entry.appendChild(attribute);
		attribute.addValue("oldvalue");
		evalExpression(attribute, "obj[0]=\"newvalue\";");
		assertEquals("newvalue", attribute.getValue(0));
	}

	@Test
	public void test_via_assignment_on_attribute_add_value() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		entry.appendChild(attribute);
		attribute.addValue("firstvalue");
		evalExpression(attribute, "obj[1]=\"secondvalue\";");
		assertEquals("secondvalue", attribute.getValue(1));
	}

	@Test
	public void test_via_assignment_on_attribute_change_value_of_child_attribute_which_has_no_namespace() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("parent");
		Attribute child = entry.createElement("child");
		attribute.appendChild(child);
		entry.appendChild(attribute);
		child.addValue("oldvalue");

		evalExpression(attribute, "obj.child=\"newvalue\";");
		assertEquals("newvalue", child.getValue(0));
	}

	@Test
	public void test_via_assignment_on_attribute_change_value_of_child_attribute_which_has_namespace_using_prefix_and_localname()
			throws Exception {
		test_via_assignment_on_attribute_change_value_of_child_attribute_which_has_namespace("obj[\"test:child\"]=\"newvalue\";");
	}

	@Test
	public void test_via_assignment_on_attribute_change_value_of_child_attribute_which_has_namespace_using_namespace_uri_and_localname()
			throws Exception {
		test_via_assignment_on_attribute_change_value_of_child_attribute_which_has_namespace("obj[\"{http://www.example.com}child\"]=\"newvalue\";");
	}

	@Test
	public void test_via_assignment_on_attribute_change_value_of_child_attribute_which_has_namespace_using_namespace_uri_prefix_and_localname()
			throws Exception {
		test_via_assignment_on_attribute_change_value_of_child_attribute_which_has_namespace("obj[\"{http://www.example.com}test:child\"]=\"newvalue\";");
	}

	@Test
	public void test_for_in_loop_over_empty_attribute() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("a");

		String expression = "var list = new java.util.ArrayList();" + "for (x in obj) {" + "	list.add(x);" + "} " + "list";
		List list = (List) evalExpression(attribute, expression);
		assertEquals(0, list.size());
	}

	@Test
	public void test_for_in_loop_over_values_of_attribute() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("a");
		Object value1 = new Object();
		Object value2 = new Object();
		attribute.addValue(value1);
		attribute.addValue(value2);

		String expression = "var list = new java.util.ArrayList();" + "for (x in obj) {" + "	list.add(x);" + "} " + "list";
		List list = (List) evalExpression(attribute, expression);
		assertEquals(2, list.size());
		assertEquals(value1, unwrapAttributeValue(list.get(0)));
		assertEquals(value2, unwrapAttributeValue(list.get(1)));
	}

	@Test
	public void test_for_in_loop_over_empty_nodelist() throws Exception {
		TestNodeList nodeList = new TestNodeList();

		String expression = "var list = new java.util.ArrayList();" + "for (x in obj) {" + "	list.add(x);" + "} " + "list";
		List list = (List) evalExpression(nodeList, expression);
		assertEquals(0, list.size());
	}

	@Test
	public void test_for_in_loop_over_attributes_in_nodelist() throws Exception {
		Entry entry = new Entry();
		Attribute attribute1 = entry.createElement("a");
		Attribute attribute2 = entry.createElement("a");
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(attribute1);
		nodeList.add(attribute2);

		String expression = "var list = new java.util.ArrayList();" + "for (x in obj) {" + "	list.add(x);" + "} " + "list";
		List list = (List) evalExpression(nodeList, expression);
		assertEquals(2, list.size());
		assertEquals(attribute1, list.get(0));
		assertEquals(attribute2, list.get(1));
	}

	@Test
	public void test_for_in_loop_over_empty_entry() throws Exception {
		Entry entry = new Entry();

		String expression = "var list = new java.util.ArrayList();" + "for (x in obj) {" + "	list.add(x);" + "} " + "list";
		List list = (List) evalExpression(entry, expression);
		assertEquals(0, list.size());
	}

	@Test
	public void test_for_in_loop_over_attributes_in_entry() throws Exception {
		Entry entry = new Entry();
		Attribute attribute1 = entry.createElement("a");
		Attribute attribute2 = entry.createElement("b");
		entry.appendChild(attribute1);
		entry.appendChild(attribute2);

		String expression = "var list = new java.util.ArrayList();" + "for (x in obj) {" + "	list.add(x);" + "} " + "list";
		List list = (List) evalExpression(entry, expression);
		assertEquals(2, list.size());
		assertEquals(attribute1, list.get(0));
		assertEquals(attribute2, list.get(1));
	}

	@Test
	public void test_for_in_loop_over_entry_and_then_over_each_attribute() throws Exception {
		Entry entry = new Entry();
		Attribute a = entry.createElement("a");
		a.addValue("myvalue");
		entry.appendChild(a);

		Attribute b = entry.createElement("b");
		a.appendChild(b);

		String expression = "var list = new java.util.ArrayList();" + "for (a in obj) {" + "	for (b in a) {" + "		list.add(b);"
				+ "	}" + "} " + "list";
		List list = (List) evalExpression(entry, expression);
		assertEquals("" + list, 2, list.size());
		assertEquals("myvalue", unwrapAttributeValue(list.get(0)));
		assertEquals(b, list.get(1));
	}

	@Test
	public void test_for_in_loop_over_attribute_and_then_over_its_child_attributes() throws Exception {
		Entry entry = new Entry();

		Attribute a = entry.createElement("a");

		Attribute b = entry.createElement("b");
		a.appendChild(b);

		Attribute c = entry.createElement("c");
		b.appendChild(c);

		b.addValue("myvalue");

		String expression = "var list = new java.util.ArrayList();" + "for (b in obj) {" + "	for (c in b) {" + "		list.add(c);"
				+ "	}" + "} " + "list";
		List list = (List) evalExpression(a, expression);
		assertEquals(2, list.size());
		assertEquals(c, list.get(0));
		assertEquals("myvalue", unwrapAttributeValue(list.get(1)));
	}

	@Test
	public void test_for_in_loop_over_nodelist_and_then_over_its_attributes() throws Exception {
		Entry entry = new Entry();

		Attribute a = entry.createElement("a");
		a.addValue("myvalue");

		Attribute b = entry.createElement("b");
		a.appendChild(b);

		TestNodeList nodeList = new TestNodeList();
		nodeList.add(a);
		nodeList.add(b);

		String expression = "var list = new java.util.ArrayList();" + "for (a in obj) {" + "	for (b in a) {" + "		list.add(b);"
				+ "	}" + "} " + "list";
		List list = (List) evalExpression(nodeList, expression);
		assertEquals(2, list.size());
		assertEquals("myvalue", unwrapAttributeValue(list.get(0)));
		assertEquals(b, list.get(1));
	}

	@Test
	public void test_on_node_list_select_child_attributes_with_specified_name_from_multiple_list_elements() throws Exception {

		Entry entry = new Entry();

		Attribute a1 = entry.createElement("a");
		Attribute b1 = entry.createElement("b");
		a1.appendChild(b1);

		Attribute a2 = entry.createElement("a");
		Attribute b2 = entry.createElement("b");
		a2.appendChild(b2);

		TestNodeList nodeList = new TestNodeList();
		nodeList.add(a1);
		nodeList.add(a2);

		Object result = evalExpression(nodeList, "obj.b");
		assertTrue(result instanceof NodeList);
		assertNodeListEquals((NodeList) result, b1, b2);
	}

	private void test_via_assignment_on_attribute_change_value_of_child_attribute_which_has_namespace(String expression)
			throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("parent");
		Attribute child = entry.createElementNS("http://www.example.com", "test:child");
		attribute.appendChild(child);
		entry.appendChild(attribute);
		child.addValue("oldvalue");

		evalExpression(attribute, expression);
		assertEquals("newvalue", child.getValue(0));
	}

	@Test
	public void test_via_assignment_on_nodelist_replace_value_of_attribute() throws Exception {
		Entry entry = new Entry();
		Attribute attribute = entry.createElement("attribute");
		entry.appendChild(attribute);
		attribute.addValue("oldvalue");
		TestNodeList nodeList = new TestNodeList();
		nodeList.add(attribute);

		evalExpression(nodeList, "obj[0]=\"newvalue\";");
		assertEquals("newvalue", attribute.getValue(0));
	}

	@Test(expected = Exception.class)
	public void test_access_non_existing_field_on_custom_java_object() throws Exception {
		assertNull(evalExpression(new MyCustomClass(), "obj.nonexistingattribute"));
	}

	@Test(expected = Exception.class)
	public void test_assign_to_non_existing_field_on_custom_java_object() throws Exception {
		assertNull(evalExpression(new MyCustomClass(), "obj.nonexistingattribute=1.0;"));
	}

	@Test
	public void test_assign_one_attribute_to_another_copies_the_attribute_values() throws Exception {
		Entry entryA = new Entry();
		Attribute a = entryA.createElement("a");
		entryA.appendChild(a);

		Entry entryB = new Entry();
		Attribute b = entryB.createElement("b");
		entryB.appendChild(b);
		Object value0 = new Object();
		Object value1 = new Object();
		b.addValue(value0);
		b.addValue(value1);

		int expectedSize = b.size();

		ScriptEngine se = new ScriptEngine("");
		se.declareBean("entryA", entryA);
		se.declareBean("entryB", entryB);
		se.eval("entryA.a = entryB.b;");

		assertEquals(expectedSize, a.size());
		assertEquals(value0, a.getValue(0));
		assertEquals(value1, a.getValue(1));
	}

	@Test
	public void test_Assign_One_Hieararhical_Attribute_To_Another_Copies_Both_The_Attribute_Values_And_Its_Children()
			throws Exception {
		Entry entryA = new Entry();
		Attribute a = entryA.createElement("a");
		entryA.appendChild(a);

		Entry entryB = new Entry();
		Attribute b = entryB.createElement("b");
		Attribute c = entryB.createElement("c");
		b.appendChild(c);
		entryB.appendChild(b);
		Object value0 = new Object();
		Object value1 = new Object();
		Object value2 = new Object();
		b.addValue(value0);
		b.addValue(value1);
		c.addValue(value2);

		int expectedSize = b.size();

		ScriptEngine se = new ScriptEngine("");
		se.declareBean("entryA", entryA);
		se.declareBean("entryB", entryB);
		se.eval("entryA.a = entryB.b;");

		assertEquals(expectedSize, a.size());
		assertEquals(value0, a.getValue(0));
		assertEquals(value1, a.getValue(1));
		assertEquals(b.getChildNodes().getLength(), a.getChildNodes().getLength());
		assertEquals(value2, ((Attribute) b.getChildNodes().item(0)).getValue(0));
	}

	@Test
	public void test_Assign_One_Hieararhical_Attribute_To_Another_Copies_The_Children_And_Pertains_Their_NSs() throws Exception {
		Entry entryA = new Entry();
		Attribute a = entryA.createElementNS("ns1", "a");
		entryA.appendChild(a);

		Entry entryB = new Entry();
		Attribute b = entryB.createElementNS("ns2", "b");
		Attribute c = entryB.createElementNS("ns2", "c");
		b.appendChild(c);
		entryB.appendChild(b);
		Object value2 = new Object();
		c.addValue(value2);

		ScriptEngine se = new ScriptEngine("");
		se.declareBean("entryA", entryA);
		se.declareBean("entryB", entryB);
		se.eval("entryA.a = entryB.b;");

		assertEquals(value2, ((Attribute) b.getChildNodes().item(0)).getValue(0));

		assertEquals("ns1", a.getNamespaceURI());
		assertEquals("ns2", a.getChildNodes().item(0).getNamespaceURI());
	}

	@Test
	public void test_assign_one_attribute_to_another_does_not_change_the_right_side_attribute() throws Exception {
		Entry entryA = new Entry();
		Attribute a = entryA.createElement("a");

		Entry entryB = new Entry();
		Attribute b = entryB.createElement("b");
		Object value0 = new Object();
		Object value1 = new Object();
		b.addValue(value0);
		b.addValue(value1);

		int expectedSize = b.size();

		ScriptEngine se = new ScriptEngine("");
		se.declareBean("entryA", entryA);
		se.declareBean("entryB", entryB);
		se.eval("entryA.a = entryB.b;");

		assertEquals(expectedSize, b.size());
		assertEquals(value0, b.getValue(0));
		assertEquals(value1, b.getValue(1));
	}

	/**
	 * Verify defect 4876 (IBMJS: Non-boolean expressions not working with if
	 * statement).
	 */
	@Test
	public void test_use_non_boolean_expression_as_if_condition() throws Exception {
		ScriptEngine se = new ScriptEngine("");
		assertEquals(3.0, se.eval("var x = null; var i = 1.0; if (x) {i = 2.0;} else {i = 3.0;}; i"));
	}

	@Test
	public void test_access_attribute_on_entry_does_not_enable_dom() throws Exception {
		Entry entry = new Entry();
		Attribute a = entry.newAttribute("a");
		assertFalse(entry.isDOMEnabled());
		evalExpression(entry, "obj.a");
		assertFalse(entry.isDOMEnabled());
	}

	@Test
	public void test_assign_to_attribute_on_entry_does_not_enable_dom() throws Exception {
		Entry entry = new Entry();
		Attribute a = entry.newAttribute("a");
		assertFalse(entry.isDOMEnabled());
		evalExpression(entry, "obj.a = 1.0;");
		assertFalse(entry.isDOMEnabled());
	}

	@Test
	public void test_for_in_loop_over_entry_attributes_does_not_enable_dom() throws Exception {
		Entry entry = new Entry();
		entry.newAttribute("a");
		entry.newAttribute("b");
		assertFalse(entry.isDOMEnabled());
		evalExpression(entry, "for (a in obj) {}");
		assertFalse(entry.isDOMEnabled());
	}

	@Test
	public void test_for_in_loop_over_attribute_values_does_not_enable_dom() throws Exception {
		Entry entry = new Entry();
		Attribute a = entry.newAttribute("a");
		a.addValue(new Object());
		a.addValue(new Object());
		assertFalse(entry.isDOMEnabled());
		evalExpression(entry, "for (value in obj.a) {}");
		assertFalse(entry.isDOMEnabled());
	}

	public static class MyCustomClass {
	}

	private void test_on_object_access_non_existing_child_attribute_using_localname(Object obj) throws Exception {
		assertNull(evalExpression(obj, "obj.nonexistingchild"));
	}

	private void test_on_object_access_single_child_attribute_which_has_no_namespace_using_localname(Object obj, Document doc)
			throws Exception {
		Element child = doc.createElement("child");
		addNodeTo(child, obj);
		assertEquals(child, evalExpression(obj, "obj.child"));
	}

	private void test_on_object_access_single_child_attribute_which_has_namespace_using_localname(Object obj, Document doc)
			throws Exception {
		Element child = doc.createElementNS("http://www.example.com", "test:child");
		addNodeTo(child, obj);
		assertEquals(child, evalExpression(obj, "obj.child"));
	}

	private void test_on_object_access_multiple_child_attributes_which_have_namespace_using_localname(Object obj, Document doc)
			throws Exception {
		Element child1 = doc.createElementNS("http://www.example.com", "test1:child");
		Element child2 = doc.createElementNS("http://www.example.org", "test2:child");
		addNodeTo(child1, obj);
		addNodeTo(child2, obj);

		Object result = evalExpression(obj, "obj.child");
		assertTrue(result instanceof NodeList);
		assertNodeListEquals((NodeList) result, child1, child2);
	}

	private void test_on_object_access_non_existing_child_attribute_using_prefix_and_localname(Object obj) throws Exception {
		assertNull(evalExpression(obj, "obj[\"pref:nonexistingchild\"]"));
	}

	private void test_on_object_access_single_child_attribute_which_has_namespace_using_prefix_and_localname(Object obj,
			Document doc) throws Exception {
		Element child = doc.createElementNS("http://www.example.com", "test:child");
		addNodeTo(child, obj);

		assertEquals(child, evalExpression(obj, "obj[\"test:child\"]"));
	}

	private void test_on_object_access_multiple_child_attributes_which_have_namespace_using_prefix_and_localname(Object obj,
			Document doc) throws Exception {
		Element child1 = doc.createElementNS("http://www.example.com", "test:child");
		Element child2 = doc.createElementNS("http://www.example.org", "test:child");
		addNodeTo(child1, obj);
		addNodeTo(child2, obj);

		Object result = evalExpression(obj, "obj[\"test:child\"]");
		assertTrue(result instanceof NodeList);
		assertNodeListEquals((NodeList) result, child1, child2);
	}

	private void test_on_object_access_non_existing_child_attribute_using_namespace_uri_and_localname(Object obj) throws Exception {
		assertNull(evalExpression(obj, "obj[\"{http://www.example.com}nonexistingchild\"]"));
	}

	private void test_on_object_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname(Object obj,
			Document doc) throws Exception {
		Element child = doc.createElementNS("http://www.example.com", "test:child");
		addNodeTo(child, obj);

		assertEquals(child, evalExpression(obj, "obj[\"{http://www.example.com}child\"]"));
	}

	private void test_on_object_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname(Object obj,
			Document doc) throws Exception {
		Element child1 = doc.createElementNS("http://www.example.com", "test1:child");
		Element child2 = doc.createElementNS("http://www.example.com", "test2:child");
		addNodeTo(child1, obj);
		addNodeTo(child2, obj);

		Object result = evalExpression(obj, "obj[\"{http://www.example.com}child\"]");
		assertTrue(result instanceof NodeList);
		assertNodeListEquals((NodeList) result, child1, child2);
	}

	private void test_on_object_access_non_existing_child_attribute_using_namespace_uri_and_localname_prefix_is_ignored(Object obj)
			throws Exception {
		assertNull(evalExpression(obj, "obj[\"{http://www.example.com}someprefix:nonexistingchild\"]"));
	}

	private void test_on_object_access_single_child_attribute_which_has_namespace_using_namespace_uri_and_localname_prefix_is_ignored(
			Object obj, Document doc) throws Exception {
		Element child = doc.createElementNS("http://www.example.com", "test:child");
		addNodeTo(child, obj);

		assertEquals(child, evalExpression(obj, "obj[\"{http://www.example.com}someprefix:child\"]"));
	}

	private void test_on_object_access_multiple_child_attributes_which_have_namespace_using_namespace_uri_and_localname_prefix_is_ignored(
			Object obj, Document doc) throws Exception {
		Element child1 = doc.createElementNS("http://www.example.com", "test1:child");
		Element child2 = doc.createElementNS("http://www.example.com", "test2:child");
		addNodeTo(child1, obj);
		addNodeTo(child2, obj);

		Object result = evalExpression(obj, "obj[\"{http://www.example.com}someprefix:child\"]");
		assertTrue(result instanceof NodeList);
		assertNodeListEquals((NodeList) result, child1, child2);
	}

	private void test_on_object_access_non_existing_property_using_localname(Object obj) throws Exception {
		assertNull(evalExpression(obj, "obj.@nonexistingproperty"));
	}

	private void test_on_object_access_property_which_has_no_namespace_using_localname(Object obj, Document doc) throws Exception {
		Attr property = doc.createAttribute("myprop");
		addAttrTo(property, obj);
		assertEquals(property, evalExpression(obj, "obj.@myprop"));
	}

	private void test_on_object_access_property_which_has_namespace_using_prefix_and_localname(Object obj, Document doc)
			throws Exception {
		Attr property = doc.createAttributeNS("http://www.example.com", "test:myprop");
		addAttrTo(property, obj);
		assertEquals(property, evalExpression(obj, "obj[\"@test:myprop\"]"));
	}

	private void test_on_object_access_non_existing_property_using_prefix_and_localname(Object obj) throws Exception {
		assertNull(evalExpression(obj, "obj[\"@test:nonexistingproperty\"]"));
	}

	private void test_on_object_access_property_which_has_namespace_using_namespace_uri_and_localname(Object obj, Document doc)
			throws Exception {
		Attr property = doc.createAttributeNS("http://www.example.com", "test:myprop");
		addAttrTo(property, obj);
		assertEquals(property, evalExpression(obj, "obj[\"@{http://www.example.com}myprop\"]"));
	}

	private void test_on_object_access_non_existing_property_using_namespace_uri_and_localname(Object obj) throws Exception {
		assertNull(evalExpression(obj, "obj[\"@{http://www.example.com}nonexistingproperty\"]"));
	}

	private void test_on_object_access_non_existing_property_using_namespace_uri_and_localname_ignores_prefix(Object obj)
			throws Exception {
		assertNull(evalExpression(obj, "obj[\"@{http://www.example.com}someprefix:nonexistingproperty\"]"));
	}

	private Object evalExpression(Object obj, String expression) throws Exception {
		ScriptEngine se = new ScriptEngine("");
		se.declareBean("obj", obj);
		return se.eval(expression);
	}

	private void addNodeTo(Node node, Object dst) {
		if (dst instanceof Node) {
			Node nodeDst = (Node) dst;
			nodeDst.appendChild(node);
		} else if (dst instanceof TestNodeList) {
			TestNodeList nodeList = (TestNodeList) dst;
			Node firstNode = nodeList.item(0);
			addNodeTo(node, firstNode);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private void addAttrTo(Attr attr, Object dst) {
		if (dst instanceof Element) {
			Element elemDst = (Element) dst;
			if (attr.getNamespaceURI() != null && attr.getNamespaceURI().trim().length() > 0) {
				elemDst.setAttributeNodeNS(attr);
			} else {
				elemDst.setAttributeNode(attr);
			}
		} else if (dst instanceof TestNodeList) {
			TestNodeList nodeList = (TestNodeList) dst;
			Node firstNode = nodeList.item(0);
			addAttrTo(attr, firstNode);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private void assertNodeListEquals(NodeList nodeList, Node... nodes) {
		assertEquals(nodeList.getLength(), nodes.length);
		for (int i = 0; i < nodeList.getLength(); ++i) {
			assertEquals(nodeList.item(i), nodes[i]);
		}
	}

	private static class TestNodeList implements NodeList {

		private List<Node> nodes = new ArrayList<Node>();

		public void add(Node node) {
			nodes.add(node);
		}

		public int getLength() {
			return nodes.size();
		}

		public Node item(int index) {
			return nodes.get(index);
		}
	}

	private Object unwrapAttributeValue(Object value) {
		if (value instanceof AttributeValue) {
			return ((AttributeValue) value).getValue();
		} else {
			return value;
		}
	}

	private File createTempScriptFile(String content) throws Exception {
		File tempDir = TestUtils.createTempDir();
		File jsFile = File.createTempFile("testscript", "js", tempDir);
		jsFile.deleteOnExit();
		Writer out = new FileWriter(jsFile);
		out.write(content);
		out.close();
		return jsFile;
	}

	private RSInterface createMockServerForGetScript(final String script, final String includeFiles, final boolean autoInclude) {
		InvocationHandler h = new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				Object result = null;
				if (method.getName().equals("getScript")) {
					if (script != null) {
						result = createScriptConfig(script, includeFiles, autoInclude);
					}
				}
				return result;
			}
		};
		RSInterface mockServer = (RSInterface) Proxy.newProxyInstance(RSInterface.class.getClassLoader(),
				new Class[] { RSInterface.class }, h);
		return mockServer;
	}

	private ScriptConfig createScriptConfig(final String script, final String includeFiles, final boolean autoInclude)
			throws Exception {
		ScriptConfig sc = new ScriptConfigImpl();
		sc.init();
		sc.setScript(script);
		sc.setIncludeFiles(includeFiles);
		sc.setAutoInclude(autoInclude);
		return sc;
	}

	private MetamergeConfig loadMetamergeConfig(String xmlConfig) throws Exception {
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(javax.naming.Context.PROVIDER_URL, xmlConfig.getBytes("UTF-8"));
		env.put(MetamergeConfigFactory.MC_CREATE, "false");
		env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
		env.put(MetamergeConfigFactory.MC_ENCRYPT, "false");
		env.put(MetamergeConfigFactory.MC_NO_DEFAULT_FOLDERS, "true");
		return new MetamergeConfigXML(env);
	}

}