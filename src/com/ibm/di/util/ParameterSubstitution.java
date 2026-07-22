/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.AssemblyLinePool;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.text.MessageFormat;

/**
 * This class provides the "Parameter Substitution" features of TDI. If you are
 * expanding a pattern only once you can use the static method. If you expand a
 * pattern more than once using different params, you could create an instance
 * and call substitute(Map) to avoid parsing the template for every call.
 * 
 * The substitute call uses a Map object where you provide the available objects
 * for pattern expansion. You should at least provide "mc=MetamergeConfig" or
 * "config=BaseConfiguration" object, otherwise expansion of TDI-properties will
 * not work. If you want to expand alcomponent parameters, you need to provide a
 * "config=BaseConfiguration" object.
 * 
 * <pre>
 * expression = system.getTDIExpression(&quot;{work.cn} {property.myprop}&quot;);
 * map = new java.util.HashMap();
 * map.put(&quot;mc&quot;, main.getMetamergeConfig());
 * 
 * while ((work = nextentry()) != null) {
 * 	map.put(&quot;work&quot;, work);
 * 	task.logmsg(expression.substitute(map));
 * }
 * </pre>
 */
public class ParameterSubstitution {

	private final static char PATTERN_START = '{';

	private final static char PATTERN_END = '}';

	// The only special cases where we need to handle objects differently
	private final static String PROPERTY_PREFIX = "property";

	// This property is special and returns the file directory of the current MetamergeConfig
	private final static String CONFIG_DIRECTORY = "$directory";
	
	private final static String ALCOMP_PREFIX = "alcomponent";

	private final static String JAVASCRIPT_PREFIX = "{javascript<<";

	private final static String JAVASCRIPT_SHORT_PREFIX = "{javascript";

	private final static String JAVASCRIPT_SHORT_END = "}";
	
	private final static String JAVASCRIPT_SHORTER_PREFIX = "{=";
	
	private final static String JAVASCRIPT_RETURN = "return ";

	private final static String FUNCTION_PREFIX = "psubst";

	private final static char ESCAPE_CHAR = '\\';

	private final static String PROPERTIES_FILE = "miserver";

	private String pattern;

	private List<Parameter> patternArguments;

	private boolean isComplex = false;

	private boolean isPreparedStatement = false;
	private List<Parameter> preparedArgs;
	
	private ScriptEngine js;

	private List<String> cref;

	private static long idCounter = 0;

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Use this to return values the old fashoned way.
	 * It is better to use <code> return someValue; </code>
	 */
	public Object value;
	
	/**
	 * This constructor parses the pattern string for use in subsequent
	 * substitute calls.
	 * 
	 * @param pattern
	 *            The pattern string to expand
	 * 
	 * @throws Exception
	 */
	public ParameterSubstitution(String pattern) throws Exception {
		this.pattern = expandPattern(pattern);
	}

	/**
	 * This constructor parses the pattern string for special use.
	 * 
	 * @param pattern
	 *            The pattern string to expand
	 * @param type 0=normal, 1=JDBC Prepared statement
	 * 
	 * @throws Exception
	 * @since 7.0
	 */
	public ParameterSubstitution(String pattern, int type) throws Exception {
		if (type == 1) {
			isPreparedStatement = true;
			preparedArgs = new ArrayList<Parameter>();			
		}
		this.pattern = expandPattern(pattern);
	}
	
	public ParameterSubstitution(String pattern, Map<String,Object> params) throws Exception {
		if (params != null) {
			Object task = params.get("task");
			if (task instanceof AssemblyLine) {
				js = ((AssemblyLine) task).getScriptEngine();
			}

		}
		this.pattern = expandPattern(pattern);
	}

	/**
	 * This method expands the current pattern string using params as source for
	 * substitution values.
	 * 
	 * @param params
	 *            The available objects (e.g. conn, work, task etc)
	 * @return A string resulting from the pattern expansion (which may be
	 *         empty, null is never returned)
	 * @throws Exception
	 */
	public String substitute(Map<String,Object> params) throws Exception {
		return substitute(params, null);
	}

	public String substitute(Map<String,Object> params, List<String> incref) throws Exception {

		// Verify that we dont have circular references
		cref = incref;
		if (cref == null)
			cref = new ArrayList<String>();

		for (Parameter p: patternArguments) {
			if (PROPERTY_PREFIX.equals(p.object)
					|| (JAVASCRIPT_PREFIX.equals(p.object)))
				continue;

			Object obj = params.get(p.object);
			if ((obj instanceof BaseConfiguration)
					&& (cref.contains(p.object + "." + p.name))) {
				throw new Exception(sResHash.getString(
						"MISERVER.PARAMSUBST.CIRCULAR.REFERENCE", new Object[] {
								p.object, p.name }));
			}
		}

		// Add new references to the list
		for (Parameter p: patternArguments) {
			if (PROPERTY_PREFIX.equals(p.object)
					|| (JAVASCRIPT_PREFIX.equals(p.object)))
				continue;

			Object obj = params.get(p.object);
			if (obj instanceof BaseConfiguration) {
				cref.add(p.object + "." + p.name);
			}
		}

		Object args[] = buildArgumentList(params);

		// If there is only one TDI reference dont call MessageFormat
		if (!isComplex) {
			if (args.length == 1) {
				if (args[0] == null)
					return null;
				else
					return "" + args[0];
			}
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < args.length; i++)
				buf.append((args[i] == null ? "" : args[i].toString()));
			return buf.toString();
		} else {
			for (int i = 0; i < args.length; i++)
				if (args[i] == null)
					args[i] = "";
		}

		return MessageFormat.format(pattern, args);
	}

	/**
	 * This method performs a one-time parsing and substitution of pattern with
	 * the objects available in params.
	 * 
	 * @param pattern
	 *            The pattern string to expand
	 * @param params
	 *            The available objects (e.g. conn, work, task etc)
	 * @return The expanded string
	 * @throws Exception
	 */
	public static String substitute(String pattern, Map<String,Object> params)
			throws Exception {
		return new ParameterSubstitution(pattern).substitute(params);
	}

	/**
	 * This method performs a one-time parsing and substitution of pattern with
	 * the objects available in params.
	 * 
	 * @param pattern
	 *            The pattern string to expand
	 * @param params
	 *            The available objects (e.g. conn, work, task etc) "
	 * @param cref
	 *            The list of previously used config object expressions (avoid
	 *            circular references)
	 * @return The expanded string
	 * @throws Exception
	 */
	public static String substitute(String pattern, Map<String,Object> params, List<String> cref)
			throws Exception {
		return new ParameterSubstitution(pattern, params).substitute(params, cref);
	}

	/**
	 * Test1: simple.extprop Test2: {work.cn} Test3: {propstore:simple.extprop}
	 * is a {work.cn}
	 * 
	 * @param pattern
	 *            the pattern
	 * @return the expanded pattern
	 * @throws Exception
	 */
	private String expandPattern(String pattern) throws Exception {
		int pos = 0;
		StringBuffer result = new StringBuffer();
		StringBuffer buf;
		String terminator;
		boolean javascript;

		patternArguments = new ArrayList<Parameter>();
		
		while (pos < pattern.length()) {
			char currentChar = pattern.charAt(pos);
			if (currentChar == ESCAPE_CHAR && pos + 1 < pattern.length()
					&& pattern.charAt(pos + 1) == PATTERN_START) {
				isComplex = true;
				result.append('\'');
				result.append(PATTERN_START);
				result.append('\'');
				pos += 2;
			} else if (currentChar == PATTERN_START) {
				buf = new StringBuffer();
				if (pattern.regionMatches(true, pos, JAVASCRIPT_SHORT_PREFIX, 0, JAVASCRIPT_SHORT_PREFIX.length()) ||
						pattern.regionMatches(true, pos, JAVASCRIPT_SHORTER_PREFIX, 0, JAVASCRIPT_SHORTER_PREFIX.length())) {
					int end;
					boolean needReturn = false;
					if (pattern.regionMatches(true, pos, JAVASCRIPT_PREFIX, 0, JAVASCRIPT_PREFIX.length())) {
						pos += JAVASCRIPT_PREFIX.length();
						terminator = "";
						boolean eof = false;
						while (!eof && pos < pattern.length()) {
							switch (pattern.charAt(pos)) {
							case 13:
								break;
							case 10:
								eof = true;
								break;
							default:
								terminator += pattern.charAt(pos);
							}
							pos++;
						}
						if (!eof) {
							throw new Exception(sResHash
									.getString("expecting.crlf.after.js.start"));
						}

						end = pattern.indexOf("\n" + terminator + "\r", pos);
						if (end == -1)
							end = pattern
									.indexOf("\n" + terminator + "\n", pos);
						if (end == -1)
							throw new Exception(sResHash.getString(
									"cannot.find.js.terminator.line",
									terminator));
					} else {
						if (pattern.regionMatches(true, pos, JAVASCRIPT_SHORTER_PREFIX, 0, JAVASCRIPT_SHORTER_PREFIX.length()) ) {
							pos += JAVASCRIPT_SHORTER_PREFIX.length();
							needReturn = true;
						} else {
							pos += JAVASCRIPT_SHORT_PREFIX.length();
						}
						terminator = JAVASCRIPT_SHORT_END;
						end = pos;
						int braces = 0;
						while (end < pattern.length()) {
							if (pattern.charAt(end) == '}') {
								if (braces == 0)
									break;
								braces --;
							} else if (pattern.charAt(end) == '{') {
								braces++;
							}
							end++;
						}
						if (end == pattern.length())
							throw new Exception(sResHash.getString(
									"cannot.find.js.terminator.line",
									terminator));
					}
					javascript = true;
					// Grab script engine from AL if possible
					if (js == null) {				
						if (Thread.currentThread() instanceof AssemblyLine) {
							js = ((AssemblyLine) Thread.currentThread())
									.getScriptEngine();
						} else if (Thread.currentThread() instanceof AssemblyLinePool.ALWorker) {
								js = ((AssemblyLinePool.ALWorker) Thread.currentThread())
										.getScriptEngine();
						} else {
							js = new ScriptEngine(null);
							js.declareUserFunctions();
						}
					}

					// The parameter will execute the generated function (e.g.
					// psubst0, psubst1 etc)
					buf.append(FUNCTION_PREFIX);
					buf.append(getUniqueID(pattern.substring(pos, end)));

					// Generate function wrapper
					StringBuffer script = new StringBuffer();
					script.append("function ");
					script.append(buf);
					script.append("(params, args) {");
					if (needReturn)
						script.append(JAVASCRIPT_RETURN);
					script.append(pattern.substring(pos, end));
					script.append("\n}");
					
					if(js == null) {
						js = new ScriptEngine(null);
						js.declareUserFunctions();					
						
					}
								   
								   
					   

					js.eval(script.toString());

					// Move position to after EOF }
					pos = end + terminator.length();
					if (terminator != JAVASCRIPT_SHORT_END) {
						int endIndex = pattern.indexOf("}", pos);
						if (endIndex > 0)
							pos = endIndex + 1;
					}

				} else {
					javascript = false;
					do {
						buf.append(pattern.charAt(pos));
					} while (pattern.charAt(pos++) != PATTERN_END);
				}

				Parameter p = new Parameter(buf.toString(), javascript);
				if (isPreparedStatement) {
					// If the expression follows a question mark, just save the Parameter.
					String s = result.toString().trim();
					if (s.length() > 0 && s.charAt(s.length() -1 ) == '?') {
						preparedArgs.add(p);
						continue;
					}
				}
				if (p.isMessageFormatRef()) {
					// MessageFormat reference
					result.append(p.toMessageFormat());
				} else {
					// TDIReference
					result.append(p.toMessageFormat(patternArguments.size()));
					patternArguments.add(p);
				}

			} else {
				isComplex = true;
				result.append(currentChar);
				if (currentChar == '\'')
					result.append(currentChar);
				pos++;
			}
		}
		return result.toString();
	}

	/**
	 * Builds an array of objects based on the parameter references
	 * 
	 * @param params
	 * @return the resultant array of objects
	 * @throws Exception
	 */
	private Object[] buildArgumentList(Map<String,Object> params) throws Exception {

		Object[] args = new Object[patternArguments.size()];
		for (int i = 0; i < patternArguments.size(); i++) {
			args[i] = patternArguments.get(i).expand(params, args);
		}

		return args;
	}

	/**
	 * Builds an array of objects based for prepared statements
	 * 
	 * @param params
	 * @return the resultant array of objects
	 * @throws Exception
	 * @since 7.0
	 */
	public Object[] getPreparedArgList(Map<String,Object> params) throws Exception {
		if (preparedArgs == null)
			return null;
		
		Object[] args = new Object[preparedArgs.size()];
		for (int i = 0; i < preparedArgs.size(); i++) {
			args[i] = preparedArgs.get(i).expand(params, args);
		}

		return args;
	}

	/**
	 * Return the name for the n'th prepared argument
	 */
	public String getPreparedArgName(int index) {
		Parameter p = preparedArgs.get(index);
		return p.name != null ? p.name : p.pattern;
	}
	
	/**
	 * This class contains the parameter reference details for a TDI reference.
	 * 
	 * Construct an instance with a TDI reference ({ref}) and use the expand(Map
	 * map) method to have it return the object it refers to.
	 * 
	 */
	private class Parameter {

		public String pattern;

		// The name or attribute of the reference
		public String name = null;

		// The object of the reference (null means simple property reference)
		public String object = null;

		// The name qualifier (e.g. property store name etc)
		public String objectQualifier = null;

		// The optional index of resolved object, -1 means no index
		public int index = -1;

		// The MessageFormat parameters
		public String params = null;

		public Parameter(String pattern, boolean isJavaScript) throws Exception {
			this.pattern = pattern;

			if (isJavaScript) {
				object = JAVASCRIPT_PREFIX;
				return;
			}

			int i = pattern.indexOf(",");

			// 1. {object.name_or_attribute[index], message_format_params}
			if (i != -1) {
				name = pattern.substring(1, i);
				params = pattern.substring(i + 1, pattern.length() - 1);
			} else {
				name = pattern.substring(1, pattern.length() - 1);
				if(name.equals("whereClause")){ // WhereClause fix 
					object = name;
				}
			}

			// 2. object.name_or_attribute[index]
			i = name.indexOf(".");
			if (i != -1) {
				object = name.substring(0, i);
				name = name.substring(i + 1);
			}

			// 3. name_or_attribute[index]
			i = name.indexOf("[");
			if (i != -1) {
				int j = name.indexOf("]");
				if (j < i + 1) {
					String errorMessage = sResHash.getString(
							"param.subs.bad.index.spec", name);
					throw new Exception(errorMessage);
				}
				String str = name.substring(i + 1, j);
				index = Integer.parseInt(str);
				name = name.substring(0, i);
			}

			// 4. object:qualifier
			if (object != null && (i = object.indexOf(":")) != -1) {
				objectQualifier = object.substring(i + 1);
				object = object.substring(0, i);
			}

			// 5. Javascript
			// if (name.startsWith
		}

		public Object expand(Map<String,Object> params, Object[] args) throws Exception {

			Object obj = null;

			// Special case: TDI Properties
			if (PROPERTY_PREFIX.equals(object) || object == null) {
				obj = getProperty(params, objectQualifier, name);
			} else if (ALCOMP_PREFIX.equals(object)) {
				obj = getALComponentParam(params, objectQualifier, name);
			} else if (JAVASCRIPT_PREFIX.equals(object)) {
				js.pushStackFrame();
				boolean changedTask = false; // did we have to change task object?
				Object saveTask = js.getBean("task");
				try {
					js.declareBean("ret", ParameterSubstitution.this);
					Object task = params.get("task");
					if (task != null && ! task.equals(saveTask)) {
						changedTask = true;
						js.declareStaticBean("task", task);
					}
					value = null;
					obj = js.call(pattern, new Object[] { params, args }, false);
					if (obj == null)
						obj = value;
				} finally {
					js.popStackFrame();
					if (changedTask && saveTask != null)
						js.declareStaticBean("task", saveTask);
				}
			} else {
				obj = params.get(object);
			}

			// Resolve value
			if (obj instanceof Entry) {
				if (InternalSchema.CONNECTOR_CONNECTOR_OPCARRIER.equals(name))
					obj = ((Entry) obj).getOperation();
				else
					obj = ((Entry) obj).getAttribute(name);
			} else if (obj instanceof BaseConfiguration) {
				if(CONFIG_DIRECTORY.equals(name)) {
					MetamergeConfig mc = ((BaseConfiguration)obj).getMetamergeConfig();
					if(mc != null) 
						return mc.getDirectory();
					else
						return ".";
				} else {
					obj = ((BaseConfiguration) obj).getParameter(name, null, cref);
				}
			}

			// If value is null or we dont have an index then return asis
			if ((obj instanceof Attribute) && index == -1)
				index = 0;

			if (obj == null || index == -1)
				return obj;

			// Resolve index based on object type
			if (obj instanceof Attribute)
				return ((Attribute) obj).getValue(index);
			else if (obj instanceof Collection)
				return ((Collection<?>) obj).toArray()[index];
			else if (obj instanceof int[])
				return Integer.valueOf(((int[]) obj)[index]);
			else if (obj instanceof double[])
				return Double.valueOf(((double[]) obj)[index]);
			else if (obj instanceof long[])
				return Long.valueOf(((long[]) obj)[index]);
			else if (obj instanceof char[])
				return "" + ((char[]) obj)[index];
			else if (obj instanceof float[])
				return Float.valueOf(((float[]) obj)[index]);
			else if (obj instanceof byte[])
				return Byte.valueOf(((byte[]) obj)[index]);

			String errorMessage = sResHash.getString(
					"param.subs.not.know.index", obj.getClass().getName());
			throw new Exception(errorMessage);
		}

		public String toString() {
			return sResHash.getString("MISERVER.PARAMSUBST.TOSTRING",
					new Object[] { pattern, object, name, objectQualifier,
							Integer.valueOf(index), params });
		}

		public String toMessageFormat() {
			if (params != null)
				return PATTERN_START + name + "," + params + PATTERN_END;
			else
				return PATTERN_START + name + PATTERN_END;
		}

		public String toMessageFormat(int index) {
			if (params != null)
				return "" + PATTERN_START + index + "," + params + PATTERN_END;
			else
				return "" + PATTERN_START + index + PATTERN_END;
		}

		public boolean isMessageFormatRef() {
			if (name != null && name.length() == 1
					&& Character.isDigit(name.charAt(0)))
				return true;
			else
				return false;
		}

		/**
		 * Returns the property value for a given store/name. The method
		 * requires access to the metamergeconfig object either explicitly
		 * through the "mc" object or by way of the "config" object.
		 */
		public Object getProperty(Map<String,Object> params, String store, String name)
				throws Exception {
			MetamergeConfig mc = (MetamergeConfig) params.get("mc");
			if ((mc == null)
					&& (params.get("config") instanceof BaseConfiguration)) {
				mc = ((BaseConfiguration) params.get("config"))
						.getMetamergeConfig();
			}
			if (mc == null) {
				String errorMessage = sResHash
						.getString("no.mconfig.or.config.obj.provided");
				throw new Exception(errorMessage);
			}

			Object prop = null;
			if (store != null) {
				prop = mc.getTDIProperties().getProperty(store, name);
			} else {
				prop = mc.getTDIProperties().getProperty(name);
			}

			if (prop instanceof Entry)
				return ((Entry) prop).getAttribute("value");
			else
				return prop;
		}

		/**
		 * Returns the parameter value for a given alcomponent. The method
		 * requires access to the "config" object.
		 */
		public Object getALComponentParam(Map<String,Object> params, String compName,
				String paramName) throws Exception {
			BaseConfiguration config = null;
			if (params.get("config") instanceof BaseConfiguration)
				config = (BaseConfiguration) params.get("config");

			if (config == null) {
				String errorMessage = sResHash.getString(
						"no.config.obj.provided", pattern);
				throw new Exception(errorMessage);
			}

			if (paramName == null)
				return null;

			if (compName == null) {
				int i = paramName.indexOf(".");
				if (i == -1)
					i = paramName.indexOf(":");
				if (i > 0) {
					compName = paramName.substring(0, i);
					paramName = paramName.substring(i + 1);
				}
			}

			BaseConfiguration comp;
			if (compName != null)
				comp = getALComponent(config, compName);
			else
				comp = getALComponent(config);

			if (comp == null) {
				String errorMessage = sResHash.getString(
						"ParamSubst.no.alcomponent", pattern);
				throw new Exception(errorMessage);
			}

			return comp.getParameter(paramName);
		}

		private BaseConfiguration getALComponent(BaseConfiguration config,
				String name) throws Exception {
			while (config != null && !(config instanceof AssemblyLineConfig))
				config = config.getParent();

			if (config != null)
				return getALComponent(((AssemblyLineConfig) config)
						.getComponent(name));

			return null;
		}

		private BaseConfiguration getALComponent(BaseConfiguration config)
				throws Exception {
			while (config != null) {
				if (config instanceof FunctionConfig)
					return ((FunctionConfig) config).getFunctionConfig();
				if (config instanceof ConnectorConfig) {
					if (ConnectorConfig.SCRIPT_MODE
							.equals(((ConnectorConfig) config).getMode()))
						return config;
					return ((ConnectorConfig) config).getConnectionConfig();
				}
				if (config instanceof RawConnectorConfig)
					return config;
				if (config instanceof LoopConfig) {
					if (((LoopConfig) config).getLoopType() == LoopConfig.LOOP_CONNECTOR_FC)
						return ((LoopConfig) config).getLoopConnector();
					return config;
				}
				if (config instanceof BranchingConfig)
					return config;
				if (config instanceof ALMappingConfig)
					return config;

				config = config.getParent();
			}
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
		HashMap<String,Object> map = new HashMap<String,Object>();
		Entry conn = new Entry();
		Entry work = new Entry();
		conn.setAttribute("sn", "surname-one");
		conn.addAttributeValue("sn", "surname-two");
		work.setAttribute("Last Name", "last name");
		work.setAttribute("today", new java.util.Date());

		map.put("work", work);
		map.put("conn", conn);
		for (int i = 0; i < args.length; i++) {
			System.out.println(sResHash.getString("MISERVER.PARAMSUBST.NEXT",
					args[i]));
			System.out.println(sResHash.getString(
					"MISERVER.PARAMSUBST.NEXT.RESULT", ParameterSubstitution
							.substitute(args[i], map)));
		}
	}

	private static Map<String, Long> functionID = new HashMap<String, Long>();
	private static long getUniqueID(String body) {
		synchronized (functionID) {
			Long ret = functionID.get(body);
			if (ret == null) {
				ret = Long.valueOf(idCounter++);
				functionID.put(body, ret);
			}
			return ret;
		}
	}

}
