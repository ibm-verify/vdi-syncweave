package com.ibm.di.script;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.entry.Entry;
import com.ibm.di.test.framework.perf.RepeatConstants;
import com.ibm.di.test.utils.TestUtils;
import com.ibm.jscript.JSExpression;
import com.ibm.jscript.JSInterpreter;

public class ScriptEngineExtPerf {

	private static String jScriptGetProperty =  "var a;\r\n" + 
												"a = e.attr1;\r\n" + 
												"a = e.attr2;\r\n" + 
												"a = e.attr3;\r\n"+ 
												"a = e.attr3.subattr31;\r\n" + 
												"a = e.attr3.subattr32;\r\n" + 
												"a = e.attr3.subattr33;\r\n" + 
												"a = e.attr4;\r\n"+ 
												"a = e.attr4.subattr41;\r\n" + 
												"a = e.attr4.subattr42;\r\n" + 
												"a = e.attr4.subattr43;\r\n" + 
												"a = e.attr5;\r\n"+ 
												"a = e.attr5.subattr[0];\r\n" + 
												"a = e.attr5.subattr[1];\r\n" + 
												"a = e.attr5.subattr[2];\r\n" +
												"a = e.attr5.@prop51;\r\n" + 
												"a = e.attr5.@prop52;\r\n" + 
												"a = e.attr5.@prop53;\r\n" +
												"a = e.attr6;\r\n"+ 
												"a = e.attr6.subattr61;\r\n" + 
												"a = e.attr6.subattr62;\r\n" + 
												"a = e.attr6.subattr63;\r\n" +
												"a = e.attr6.subattr61.ssattr[0];\r\n" + 
												"a = e.attr6.subattr61.ssattr[1];\r\n" + 
												"a = e.attr6.subattr61.ssattr[2];\r\n" +
												"a = e.attr6.subattr62.ssattr[0];\r\n" + 
												"a = e.attr6.subattr62.ssattr[1];\r\n" + 
												"a = e.attr6.subattr62.ssattr[2];\r\n" +
												"a = e.attr6.subattr63.ssattr[0];\r\n" + 
												"a = e.attr6.subattr63.ssattr[1];\r\n" + 
												"a = e.attr6.subattr63.ssattr[2];\r\n" +
												"a = e.attr7;\r\n"+ 
												"a = e.attr7.subattr71;\r\n" + 
												"a = e.attr7.subattr72;\r\n" + 
												"a = e.attr7.subattr73;\r\n" +
												"a = e.attr7.subattr71.@prop71;\r\n" +
												"a = e.attr7.subattr71.ssattr[0];\r\n" +
												"a = e.attr7.subattr71.ssattr[0].@prop711;\r\n" + 
												"a = e.attr7.subattr71.ssattr[1];\r\n" +
												"a = e.attr7.subattr71.ssattr[1].@prop712;\r\n" +
												"a = e.attr7.subattr71.ssattr[2];\r\n" +
												"a = e.attr7.subattr71.ssattr[2].@prop713;\r\n" +
												"a = e.attr7.subattr72.@prop72;\r\n" +
												"a = e.attr7.subattr72.ssattr[0];\r\n" + 
												"a = e.attr7.subattr71.ssattr[0].@prop721;\r\n" +
												"a = e.attr7.subattr72.ssattr[1];\r\n" + 
												"a = e.attr7.subattr71.ssattr[1].@prop722;\r\n" +
												"a = e.attr7.subattr72.ssattr[2];\r\n" +
												"a = e.attr7.subattr71.ssattr[2].@prop723;\r\n" +
												"a = e.attr7.subattr72.@prop72;\r\n" +
												"a = e.attr7.subattr73.ssattr[0];\r\n" + 
												"a = e.attr7.subattr71.ssattr[0].@prop731;\r\n" +
												"a = e.attr7.subattr73.ssattr[1];\r\n" + 
												"a = e.attr7.subattr71.ssattr[1].@prop732;\r\n" +
												"a = e.attr7.subattr73.ssattr[2];\r\n" +
												"a = e.attr7.subattr71.ssattr[2].@prop733;\r\n";

	private static String jScriptPutProperty =  "var e = new com.ibm.di.entry.Entry();\r\n" + 
												"e.attr1 = \"val11\";\r\n" + 
												"e.attr2 = \"val21\";\r\n" + 
												"e.attr3 = \"val31\";\r\n" + 
												"e.@prop1 = \"property1\";\r\n" + 
												"e.@prop2 = \"property2\";\r\n" + 
												"e.@prop3 = \"property3\";\r\n" + 
												"e.attr1[1] = \"val12\";\r\n" + 
												"e.attr1[2] = \"val13\";\r\n" + 
												"e.attr1.@prop = \"prop1\";\r\n" + 
												"e.attr2[1] = \"val22\";\r\n" + 
												"e.attr2[2] = \"val23\";\r\n" + 
												"e.attr2.@prop = \"prop2\";\r\n" + 
												"e.attr3[1] = \"val32\";\r\n" + 
												"e.attr3[2] = \"val33\";\r\n" + 
												"e.attr3.@prop = \"prop3\";\r\n" + 
												"e.appendChild(e.createElement(\"attr4\"));\r\n" + 
												"e.attr4.appendChild(e.createElement(\"subattr\"));\r\n" + 
												"e.attr4.appendChild(e.createElement(\"subattr\"));\r\n" + 
												"e.attr4.appendChild(e.createElement(\"subattr\"));\r\n" + 
												"e.attr4.subattr[0] = \"val411\";\r\n" + 
												"e.attr4.subattr[0][1] = \"val412\";\r\n" + 
												"e.attr4.subattr[0][2] = \"val413\";\r\n" + 
												"e.attr4.subattr[1] = \"val421\";\r\n" + 
												"e.attr4.subattr[1][1] = \"val422\";\r\n" + 
												"e.attr4.subattr[1][2] = \"val423\";\r\n" + 
												"e.attr4.subattr[2] = \"val431\";\r\n"+ 
												"e.attr4.subattr[2][1] = \"val432\";\r\n" + 
												"e.attr4.subattr[2][2] = \"val433\";";

	private static String jScriptForInAttributeCycles = "for (val in e.attr2){\r\n" + 
														"	for (val in e.attr2){\r\n" +
														"		for (val in e.attr2){}\r\n" + 
														"	}\r\n" + "}";

	private static String jScriptForInNodeListCycles =  "for (subattr in e.attr5.subattr){\r\n" + 
														"	for (subattr in e.attr5.subattr){\r\n"+ 
														"		for (subattr in e.attr5.subattr){\r\n" +
														"            for (subattr in e.attr5.subattr){}\r\n"+
														"       }\r\n" + 
														"	}\r\n" + 
														"}";

	private static String jScriptForInEntryCycles = "for (attr in e){\r\n" + 
													"	for (attr in e){\r\n" + 
													"		for (attr in e){}\r\n" + 
													"	}\r\n" + "}";

	private static String jScript = "var i = 0, n = 7;\r\n" + 
									"var arr = new java.util.ArrayList();\r\n" + 
									"try{" +
									"   if (n < 0 || n > 100) throw new java.lang.Exception(\"Parameter 'n' must be between 0 and 100!\");\r\n" +
									"}catch(e){}\r\n" +
									"while(i < n) {\r\n" + 
									"	arr.add(fib(i++));\r\n" + 
									"}\r\n" + 
									"var sum = 0;\r\n" + 
									"for(elem in arr){\r\n" + 
									"	sum += elem; 	\r\n" + 
									"}\r\n" + 
									"show(sum);\r\n" + 
									"try{\r\n" + 
									"	throw new java.lang.Exception();\r\n" + 
									"}catch(e){}\r\n" + 
									"function fib(n) {\r\n" + 
									"	var s = 0;\r\n" + 
									"	if(n == 0) return(s);\r\n" + 
									"		if(n == 1) {\r\n" + 
									"		s += 1;\r\n" + 
									"		return(s);\r\n" + 
									"	}\r\n" + 
									"	else {\r\n" + 
									"		return(fib(n - 1) + fib(n - 2));\r\n" + 
									"    }\r\n" + 
									"}\r\n" + 
									"function show(n) {\r\n" + 
									"	var i, s = \"\";\r\n" + 
									"	\r\n" + 
									"	for(i = 0; i <= n; i++) {\r\n" + 
									"		s += \"  \" + fib(i);\r\n" + 
									"	}\r\n" + 
									"}";

	private static String jScriptRecursion ="show(25);\r\n" + 
											"function fib(n) {\r\n" + 
											"	var s = 0;\r\n" + 
											"	if(n == 0) return(s);\r\n" + 
											"		if(n == 1) {\r\n" + 
											"		s += 1;\r\n" + 
											"		return(s);\r\n" + 
											"	}\r\n" + 
											"	else {\r\n" + 
											"		return(fib(n - 1) + fib(n - 2));\r\n" + 
											"    }\r\n" + 
											"}\r\n"+
											"function show(n) {\r\n" + 
											"	var i, s = \"\";\r\n" + 
											"	\r\n" + 
											"	for(i = 0; i <= n; i++) {\r\n" + 
											"		s += \"  \" + fib(i);\r\n" + 
											"	}\r\n" + 
											"}";
	private static String jScriptWhileLoop =    "var arr = new java.util.ArrayList();\r\n" + 
												"var i =1;\r\n"+
												"while(i < 25) {\r\n" + 
												"	arr.add(i++);\r\n" + 
												"}";
	
	private static String jScriptForInLoop ="var arr = [1,2,3,4,5,6,7,8,9,10]\r\n"+
											"for(elem in arr){\r\n" + 
											"	for(elem in arr){\r\n" + 
											"		for(elem in arr){}\r\n" + 
											"	}\r\n" + 
											"}";
	private static String jScriptTryCatchWithException = "try{" +
														"   throw new java.lang.Exception(\"Exception occured!\");\r\n" +
														"}catch(e){}";
	
	private static String jScriptTryCatchWithoutException = "try{" +
															"   var a = 2 + 2;" +
															"}catch(e){}";
	
	private static String jScriptForLoop =  "var i,k,l;\r\n" + 
											"for(i = 0; i<10; i++){\r\n" + 
											"	for(k = 0; k<10; k++){\r\n" + 
											"			for(l = 0; l<10; l++){}\r\n" + 
											"	}\r\n" + 
											"}";
	
	private static String jScriptIfElse =   "i=1; j=3; k=5;\r\n" + 
											"if(i>j){\r\n" + 
											"	if(i>k){\r\n" + 
											"		max = i;\r\n" + 
											"	} else {\r\n" + 
											"		max = k;\r\n" + 
											"	}\r\n" + 
											"} else {\r\n" + 
											"	if(j < k){\r\n" + 
											"		max = k;\r\n" + 
											"	} else {\r\n" + 
											"		max = j;\r\n" + 
											"	}\r\n" + 
											"}";
	
	private static String jScriptFunctionCalls ="i = add(mul(add(2,3),mul(3,4)),mul(div(9,3),sub(12,4)));\r\n" +
												"i = mul(add(mul(3,5),sub(2,1)),div(sub(12,4),mul(5,4)));"+
												"i = sub(div(mul(12,4),add(6,5)),sub(mul(16,2),add(15,5)));"+
												"i = div(mul(sub(15,9),mul(9,7)),add(div(16,7),div(35,5)));"+
												"function add(a,b){ return (a+b); }\r\n" + 
												"function div(a,b){ return (a/b); }\r\n" + 
												"function mul(a,b){ return (a*b); }\r\n" + 
												"function sub(a,b){ return (a-b); }";
	
	private static ScriptEngineOptions jsOptions = null;
	private static JSInterpreter jsengine = null;
	private static ScriptEngine se = null;

	private static InputStream is = null;
	private static Reader re = null;
	private static JSExpression expr = null;

	public static void declareStaticEntryBean(int complexity) throws Exception {
		Entry e = TestUtils.createHierarchicalEntry(complexity,"");
		se.declareStaticBean("e", e);
	}

	public static void declareEntryBean(int complexity) throws Exception {
		Entry e = TestUtils.createHierarchicalEntry(complexity,"");
		se.declareBean("e", e);
	}
	
	public static void undeclareEntryBean() throws Exception {
		se.undeclareBean("e");
	}

	public static void undeclareStaticEntryBean() throws Exception {
		se.undeclareStaticBean("e");
	}
	
	@BeforeClass
	public static void initScriptEngine() throws Exception {
		try {
			// debug is false
			se = new ScriptEngine("javascript", null);

			// Create JSContext for reuse
			jsOptions = new ScriptEngineOptions(true, false);

			// Create new jsengine
			jsengine = new JSInterpreter(jsOptions);

			is = new ByteArrayInputStream(jScript.getBytes());

			re = new InputStreamReader(is);
		} catch (Exception e) {
			throw new RuntimeException("Could not initialize script engine.", e);
		}
	}

	// Test parsing of Scripts

	@Test
	public void test_Parsing_Script_With_Recursion() throws Exception {
		// 2,5 min
		for (long i = 0; i < RepeatConstants.get1g(); i++) {
			jsOptions.getExpression(jScriptRecursion, JSExpression.DYNAMIC_SOURCE_ID);
		}
	}
	
	@Test
	public void test_Parsing_Script_With_TryCatch_And_Exception() throws Exception {
		// 4,9 min
		for (long i = 0; i < RepeatConstants.get2g(); i++) {
			jsOptions.getExpression(jScriptTryCatchWithException, JSExpression.DYNAMIC_SOURCE_ID);
		}
	}
	
	@Test
	public void test_Parsing_Script_With_TryCatch_And_No_Exception() throws Exception {
		// 4,9 min
		for (long i = 0; i < RepeatConstants.get2g(); i++) {
			jsOptions.getExpression(jScriptTryCatchWithoutException, JSExpression.DYNAMIC_SOURCE_ID);
		}
	}
	
	@Test
	public void test_Parsing_Script_With_While_Loop() throws Exception {
		// 5 min
		for (long i = 0; i < RepeatConstants.get2g(); i++) {
			jsOptions.getExpression(jScriptWhileLoop, JSExpression.DYNAMIC_SOURCE_ID);
		}
	}
	
	@Test
	public void test_Parsing_Script_With_For_Loop() throws Exception {
		// 5 min
		for (long i = 0; i < RepeatConstants.get2g(); i++) {
			jsOptions.getExpression(jScriptForLoop, JSExpression.DYNAMIC_SOURCE_ID);
		}
	}
	
	@Test
	public void test_Parsing_Script_With_ForIn_Loop() throws Exception {
		// 4,9 min		
		for (long i = 0; i < RepeatConstants.get2g(); i++) {
			jsOptions.getExpression(jScriptForInLoop, JSExpression.DYNAMIC_SOURCE_ID);
		}
	}
	
	@Test
	public void test_Parsing_Script_With_IfElse() throws Exception {
		// 4,9 min		
		for (long i = 0; i < RepeatConstants.get2g(); i++) {
			jsOptions.getExpression(jScriptIfElse, JSExpression.DYNAMIC_SOURCE_ID);
		}
	}
	
	@Test
	public void test_Parsing_Script_With_Function_Calls() throws Exception {
		// 5  min
		for (long i = 0; i < RepeatConstants.get2g(); i++) {
			jsOptions.getExpression(jScriptFunctionCalls, JSExpression.DYNAMIC_SOURCE_ID);
		}
	}
	
	@Test
	public void test_Parsing_Script_Using_String() throws Exception {
		// 4,40 min
		for (long i = 0; i < RepeatConstants.get2g(); i++) {
			jsOptions.getExpression(jScript, JSExpression.DYNAMIC_SOURCE_ID);
		}
	}

	@Test
	public void test_Parsing_Script_Using_InputStream() throws Exception { 
		//2,80 min
		for (long i = 0; i < RepeatConstants.get25m(); i++) {
			jsOptions.getExpression(is, JSExpression.DYNAMIC_SOURCE_ID);
		}
	}

	@Test
	public void test_Parsing_Script_Using_Reader() throws Exception { 
		// 4,00 min
		for (long i = 0; i < RepeatConstants.get150m(); i++) {
			jsOptions.getExpression(re, JSExpression.DYNAMIC_SOURCE_ID);
		}
	}

	// Test interpreting Scripts

	@Test
	public void test_Interpreting_Script_With_Recursion() throws Exception {
		// 3,1 min
		expr = jsOptions.getExpression(jScriptRecursion);
		for (long i = 0; i < RepeatConstants.get150(); i++) {
			jsengine.interpret(expr);
		}
	}
	
	@Test
	public void test_Interpreting_Script_With_TryCatch_And_Exception() throws Exception {
		// 3,15 min
		expr = jsOptions.getExpression(jScriptTryCatchWithException);
		for (long i = 0; i < RepeatConstants.get25m(); i++) {
			jsengine.interpret(expr);
		}
	}
	
	@Test
	public void test_Interpreting_Script_With_TryCatch_And_No_Exception() throws Exception {
		// 4,4 min
		expr = jsOptions.getExpression(jScriptTryCatchWithException);
		for (long i = 0; i < RepeatConstants.get25m(); i++) {
			jsengine.interpret(expr);
		}
	}
	
	@Test
	public void test_Interpreting_Script_With_For_Loop() throws Exception {
		// 4,85 min
		expr = jsOptions.getExpression(jScriptForLoop);
		for (long i = 0; i < RepeatConstants.get2m(); i++) {
			jsengine.interpret(expr);
		}
	}
	
	@Test
	public void test_Interpreting_Script_With_ForIn_Loop() throws Exception {
		// 4,2 min
		expr = jsOptions.getExpression(jScriptForInLoop);
		for (long i = 0; i < RepeatConstants.get4m(); i++) {
			jsengine.interpret(expr);
		}
	}
	
	@Test
	public void test_Interpreting_Script_With_While_Loop() throws Exception {
		// 3,75 min
		expr = jsOptions.getExpression(jScriptWhileLoop);
		for (long i = 0; i < RepeatConstants.get15m(); i++) {
			jsengine.interpret(expr);
		}
	}
	
	@Test
	public void test_Interpreting_Script_With_IfElse() throws Exception {
		// 3 min
		expr = jsOptions.getExpression(jScriptIfElse);
		for (long i = 0; i < RepeatConstants.get300m(); i++) {
			jsengine.interpret(expr);
		}
	}
	
	@Test
	public void test_Interpreting_Script_With_Functions_And_No_RegisterFunctions() throws Exception {
		// 4,75 min
		expr = jsOptions.getExpression(jScriptFunctionCalls);
		for (long i = 0; i < RepeatConstants.get10m(); i++) {
			jsengine.interpret(expr);
		}
	}

	@Test
	public void test_Interpreting_Script_With_Functions_And_RegisterFunctions() throws Exception {
		// 3,3 min
		expr = jsOptions.getExpression(jScriptFunctionCalls);
		for (long i = 0; i < RepeatConstants.get7m(); i++) {
			jsengine.interpret(expr, true);
		}
	}

	@Test
	public void test_ScriptEngineOptions_GetProperty() throws Exception { 
		//2,85 min
		declareEntryBean(7);
		for (double i = 0.0; i < RepeatConstants.get500k(); i++) {
			se.interpret(jScriptGetProperty);
		}
		undeclareEntryBean();
	}

	@Test
	public void test_ScriptEngineOptions_PutProperty() throws Exception { 
		// 3,3 min
		for (double i = 0.0; i < RepeatConstants.get2m(); i++) {
			se.interpret(jScriptPutProperty);
		}
	}

	@Test
	public void test_ForIn_Attribute_Cycle() throws Exception { // 3,35 min
		declareStaticEntryBean(5);
		for (double i = 0.0; i < RepeatConstants.get15m(); i++) {
			se.interpret(jScriptForInAttributeCycles);
		}
	}

	@Test
	public void test_ForIn_NodeList_Cycle() throws Exception { // 3,00 min
		for (double i = 0.0; i < RepeatConstants.get1m(); i++) {
			se.interpret(jScriptForInNodeListCycles);
		}
	}

	@Test
	public void test_ForIn_Entry_Cycle() throws Exception { // 3,2 min
		for (double i = 0.0; i < RepeatConstants.get10m(); i++) {
			se.interpret(jScriptForInEntryCycles);
		}
		undeclareStaticEntryBean();
	}

	@Test
	public void test_Invoking_Method() throws Exception { // 2,4 min
		se.interpret("var e = new com.ibm.di.entry.Entry();");
		for (double i = 0.0; i < RepeatConstants.get250m(); i++) {
			se.interpret("e.getAttribute(null);");
		}
	}
}
