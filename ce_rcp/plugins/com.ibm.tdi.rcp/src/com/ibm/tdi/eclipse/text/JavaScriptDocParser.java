/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.text;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Name;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.ibm.di.config.base.ScriptConfigImpl;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.script.ScriptEngineOptions;
import com.ibm.icu.util.StringTokenizer;
import com.ibm.jscript.ParserResult;
import com.ibm.jscript.ASTTree.ASTFunction;
import com.ibm.jscript.ASTTree.ASTNode;
import com.ibm.jscript.ASTTree.DefaultNodeVisitor;
import com.ibm.jscript.ASTTree.ASTFunction.Parameter;
import com.ibm.jscript.parser.FBScript2;
import com.ibm.jscript.types.FBSBoolean;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSString;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;

/**
 * This class scans javascript text for javadoc style information and records
 * that information for later retrieval. In addition the javascript text is also
 * parsed by the ibmjs engine so we can discover function parameter/return types
 * as well as other relevant information about the script.
 * <p>
 * All functions discovered are placed in ScriptFunctionInfo objects.
 * <p>
 * 
 */
public class JavaScriptDocParser implements MetamergeConfigChangeListener {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// Default script category
	public final static String DEFAULT_SCRIPT_CATEGORY = "Script Library";
	
	// Static maps that caches parsed scripts and tracks modifications
	private static HashMap<String, List<ScriptFunctionInfo>> filemap = new HashMap<String, List<ScriptFunctionInfo>>();
	private static HashMap<String, JavaScriptDocParser> almap = new HashMap<String, JavaScriptDocParser>();

	// Instance vars based on assemblyline config object
	private AssemblyLineConfig context;
	private HashMap<String, ScriptFunctionInfo> map = new HashMap<String, ScriptFunctionInfo>();
	private IProject project;
	private ArrayList<Listener> listeners = new ArrayList<Listener>();

	// Code snippets from resource
	private static ScriptConfig codeSnippets;
	
	/*
	 * The class map contains the java class corresponding to the type qualifier
	 * name used when declaring the syntax for variables, parameters and return
	 * types.
	 */
	private static HashMap<String, Class<?>> classMap = new HashMap<String, Class<?>>();
	static {
		classMap.put("string", FBSString.class);
		classMap.put("number", FBSNumber.class);
		classMap.put("boolean", FBSBoolean.class);
		classMap.put("attribute", Attribute.class);
		classMap.put("entry", Entry.class);
	}

	/**
	 * Returns a shared JavaScriptDocParser object for the specified
	 * assemblyline. An assemblyline editor shares access to a single instance
	 * of this parser to optimize scanning and parsing of script code. When the
	 * AL no longer needs the parser it should release it by call
	 * releaseDocParser(config).
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static JavaScriptDocParser getDocParserFor(AssemblyLineConfig context) throws Exception {
		String path = getPathForConfig(context);
		JavaScriptDocParser parser = almap.get(path);
		if (parser == null) {
			parser = new JavaScriptDocParser(context);
			parser.addScriptFunctions();
			almap.put(path, parser);
		}
		return parser;
	}
	
	/**
	 * Removes the parser object asscociated with the assemblyline.
	 * 
	 * @param context
	 */
	public static void releaseDocParser(AssemblyLineConfig context) {
		almap.remove(getPathForConfig(context));
	}

	private JavaScriptDocParser(AssemblyLineConfig context) throws Exception {
		this.context = context;
		this.project = Utils.getProjectFor(context);

		if(codeSnippets == null) {
			try {
				codeSnippets = new ScriptConfigImpl();
				codeSnippets.setName("[system code snippets]");
				codeSnippets.init();
				
				InputStream is = Activator.getDefault().getResource("etc/CodeSnippets.js");
				StringBuffer buf = new StringBuffer();
				while(is.available() > 0) {
					buf.append((char)is.read());
				}
				is.close();
				codeSnippets.setScript(buf.toString());
			} catch (Exception e) {
				// Log only since it's not fatal
				EclipseAppender.logerror(e.toString(), e);
			}
		}
		
		if (this.project != null) {
			MetamergeConfig mc = Utils.getProjectMC(project);
			if (mc != null)
				mc.addListener(this);
		}
	}

	/**
	 * When the project mc changes we check if this AL's scripts are affected.
	 * 
	 * @param changeEvent
	 */
	public void configurationChanged(MetamergeConfigChange e) {
		if(e.getSource() instanceof MetamergeConfig && e.getKey() != null) {
			try {
				Name name = (Name) MetamergeConfigFactory.parseName(e.getKey());
				if(name.get(0).equals(MetamergeConfig.DEFAULT_SCRIPT_FOLDER)) {
					String path = project.getName() + ":" + name.get(1);
					switch(e.getOperation()) {
					case MetamergeConfigChange.MCC_MODIFY:
					case MetamergeConfigChange.MCC_ADD:
						ScriptConfig script = (ScriptConfig) context.getMetamergeConfig().lookup(name);
						clearMap(script);
						filemap.remove(path);
						parseScript(script);
						break;
						
					case MetamergeConfigChange.MCC_DELETE:
						List<ScriptFunctionInfo> old = filemap.get(path);
						if(old != null && old.size() > 0) {
							clearMap(old.get(0).source);
						}
						filemap.remove(path);
	
					}
					
					fireContentsChanged();
				}
			} catch (Exception err) {
				err.printStackTrace();
			}
		}
	}

	/**
	 * Clears all references to functions in script.
	 * 
	 * @param script
	 */
	private void clearMap(ScriptConfig script) {
		ArrayList<String> deletedKeys = new ArrayList<String>();
		IProject p1 = Utils.getProjectFor(script);
		if (p1 == null)
			return;

		for (ScriptFunctionInfo sfi : map.values()) {
			if (sfi.source.getShortName().equals(script.getShortName())) {
				IProject p2 = Utils.getProjectFor(sfi.source);
				if (p1.equals(p2)) {
					deletedKeys.add(sfi.getName());
				}
			}
		}

		for (String str : deletedKeys)
			map.remove(str);
	}

	/**
	 * Runs through the assemblyline's list of included scripts and parses each
	 * one to obtain the list of functions in the scripts.
	 * 
	 * @throws Exception
	 */
	public void addScriptFunctions() throws Exception {
		ArrayList<String> include = null;
		map.clear();
		String str = context.getSettings().getStringParameter("includePrologs");
		if (str != null && str.trim().length() > 0) {
			include = new ArrayList<String>();
			for (String s : str.split("\n"))
				include.add(s);
		}

		MetamergeConfig mc = context.getMetamergeConfig();
		addScriptsFromConfig(mc, include);
		parseScript(codeSnippets);
	}

	/**
	 * Returns all function names available in the AL context.
	 * 
	 * @return
	 */
	public ArrayList<String> getAllFunctionNames() {
		ArrayList<String> list = new ArrayList<String>();
		for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext();)
			list.add(iter.next());
		return list;
	}

	/**
	 * Returns all function info objects for functions available in the AL
	 * context.
	 * 
	 * @return
	 */
	public ArrayList<ScriptFunctionInfo> getAllFunctions() {
		ArrayList<ScriptFunctionInfo> list = new ArrayList<ScriptFunctionInfo>();
		for (Iterator<ScriptFunctionInfo> iter = map.values().iterator(); iter.hasNext();)
			list.add(iter.next());
		return list;
	}

	/**
	 * Returns the info object for a named function.
	 * 
	 * @param str
	 *            Function name
	 * @return
	 */
	public ScriptFunctionInfo getFunctionInfo(String str) {
		return getFunctionInfo(str, false);
	}

	/**
	 * Returns the info object for a named function.
	 * 
	 * @param str
	 *            function name
	 * @param create
	 *            if true, the info object is created if it does not exist
	 * @return
	 */
	public ScriptFunctionInfo getFunctionInfo(String str, boolean create) {
		ScriptFunctionInfo sfi = map.get(str);
		if (sfi == null && create) {
			sfi = new ScriptFunctionInfo();
			sfi.name = str;
			map.put(str, sfi);
		}
		return sfi;
	}

	/**
	 * Returns the script name with a namespace id if the script is not part of context's config.
	 * 
	 * @param cfg
	 * @param sc
	 * @return
	 */
	public String getScriptName(Object cfg, ScriptConfig sc) {
		String name = sc.getShortName();
		IProject targetProject = Utils.getProjectFor(sc);
		if(project != null && !project.equals(targetProject)) {
			String ns = (String) MetamergeConfigFactory.getLocalNamespaceFor(context.getMetamergeConfig(), sc);
			if (ns != null)
				name = ns + ":" + name;
			else if(targetProject != null)
				name = targetProject.getName() + ":" + name;
		} else if(project == null && targetProject != null) {
			name = targetProject.getName() + ":" + name;
		}
		return name;
	}
	
	/**
	 * Parses all scripts in the provided config including all refereneced
	 * configs.
	 * 
	 * @param mc
	 *            Main config object
	 * @param include
	 *            List of explicitly included scripts
	 * @throws Exception
	 */
	public void addScriptsFromConfig(MetamergeConfig mc, ArrayList<String> include) throws Exception {
		addScriptsFromConfig(mc, include, new HashSet<String>());
	}

	private void addScriptsFromConfig(MetamergeConfig mc, ArrayList<String> include, HashSet<String> exclude) throws Exception {
		MetamergeFolder table = null;
		try {
			table = (MetamergeFolder) mc.lookup(MetamergeConfig.DEFAULT_SCRIPT_FOLDER);
		} catch (Exception ignore) {
			SystemFunctions.doNothing();
		}

		if (table != null) {
			for (String str : table.getNames()) {
				ScriptConfig script = mc.getScript(str);
				String name = getScriptName(mc, script);
				// explicitly excluded?
				if(include != null && include.contains("-" + name)) {
					continue;
				}
				if ((include != null && include.contains(str)) || (script.getAutoInclude() && context.getSettings().getBooleanParameter("includeGlobalPrologs", true))) {
					try {
						parseScript(script);
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e);
					}
				}
			}
		}

		// Now include scripts from included configs
		try {
			table = (MetamergeFolder) mc.lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
		} catch (Exception e) {
			table = null;
		}

		if (table != null) {
			for (String name: table.getNames()) {
				if (!exclude.add(name))
					continue;
				MetamergeConfig inc = MetamergeConfigFactory.getLocalNamespace(table.getMetamergeConfig(), name);
				if (inc != null) {
					addScriptsFromConfig(inc, include, exclude);
				}
			}
		}
	}

	private static String getPathForConfig(BaseConfiguration config) {
		IProject p = Utils.getProjectFor(config);
		String append = config instanceof AssemblyLineConfig ? ".assemblyline" : "";
		if (p == null) {
			return config.getShortName() + append;
		} else {
			return p.getName() + ":" + config.getShortName() + append;
		}
	}

	/**
	 * Parse the script to obtain the javadoc style comments and the internally
	 * parsed script to retrieve function parameter types and return value
	 * types. The "map" variable is updated with this information.
	 * 
	 * @param script
	 */
	private void parseScript(final ScriptConfig script) throws Exception {

		// Check if the script has been parsed and compiled before
		// and is not modified; in case we don't bother to rescan it.
		final String path = getPathForConfig(script);
		if (filemap.containsKey(path)) {
			for (ScriptFunctionInfo sfi : filemap.get(path)) {
				map.put(sfi.getName(), sfi);
			}
			return;
		}

		if (script.getScript() == null)
			return;

		// create list
		final List<ScriptFunctionInfo> sfilist = new ArrayList<ScriptFunctionInfo>();

		/*
		 * First compile script to get all function names
		 */
		try {
			ParserResult pr = ScriptEngineOptions.get().parseScript(script.getScript(), true);
			FBScript2 parser = new FBScript2(new StringReader(script.getScript()));
			parser.jsContext = ScriptEngineOptions.get();
			parser.parsedScript = pr;
			final ASTNode main = parser.program();
			main.visitAllNodes(new DefaultNodeVisitor() {
				public Object visitFunction(ASTFunction x, Object param) {

					if (x == null || x.getName() == null)
						return super.visitFunction(x, param);
						
					// -- Don't include nested functions
					ASTNode parent = x.getParent();
					while(parent != null) {
						if(parent instanceof ASTFunction)
							return super.visitFunction(x, param);
						parent = parent.getParent();
					}

					ScriptFunctionInfo sfi = getFunctionInfo(x.getName(), true);
					sfi.source = script;
					try {
						sfi.descriptor = getFunctionParams(script.getScript().trim(), x.getName(), x);
					} catch (Exception e) {
						EclipseAppender.logerror(path + "." + x.getName() + " -> " + e.toString(), e);
					}
					
					if (sfi.descriptor == null)
						return super.visitFunction(x, param);

					map.put(sfi.name, sfi);
					if (x.getReturnType() != null) {
						sfi.descriptor.returns = new ParameterDescriptor(x.getReturnType(), null);
					}
					for (int i = 0; i < x.getParameterCount(); i++) {
						Parameter p = x.getParameterAt(i);
						if (p != null)
							sfi.descriptor.setParameterType(p.getName(), p.getType());
					}
					sfilist.add(sfi);
					return super.visitFunction(x, param);
				}
			});
		} catch (Exception e) {
			// If script does not compile then no need to fool the user
			// to believe it contains valid functions.
			EclipseAppender.logerror(path + ": " + e.toString(), e);
			
		} finally {
			filemap.put(path, sfilist);
			
		}
	}

	/**
	 * Returns the java class that a function returns. If the function has not
	 * declared its return type a null pointer is returned.
	 * 
	 * @param fname
	 *            Function name
	 * @return
	 */
	public Class<?> getFunctionReturnClass(String fname) {
		ScriptFunctionInfo sfi = map.get(fname);
		if (sfi != null && sfi.getDescriptor() != null) {
			return getClassForJSType(sfi.getDescriptor().returns.name);
		}
		return null;
	}

	/**
	 * Returns the java class for the string specified by type. The type param
	 * may be the standard type names such as "string", "number" etc or a java
	 * class name. If the type is unknown the FBSObject class is returned.
	 * 
	 * @param name
	 * @return
	 */
	public Class<?> getClassForJSType(String type) {
		Class<?> cls = null;
		if (type != null)
			cls = classMap.get(type.toLowerCase());

		return cls != null ? cls : FBSObject.class;
	}

	/**
	 * Returns the list of functions defined in all global scripts.
	 * 
	 * @param script
	 * @return
	 */
	public List<String> getFunctionNames(String script) {
		StringTokenizer st = new StringTokenizer(script == null ? "" : script, "\n");
		ArrayList<String> list = new ArrayList<String>();
		Pattern pattern = Pattern.compile("^function\\s*(\\w*)\\s*(\\s*).*");

		while (st.hasMoreTokens()) {
			String str = st.nextToken().trim();
			Matcher m = pattern.matcher(str);
			if (m.matches()) {
				list.add(m.group(1));
			}
		}
		return list;
	}

	/**
	 * Returns the function's parameter names and labels as defined by the
	 * "@param" and "@return" tags in the script code.
	 * 
	 * @param function
	 *            The function name
	 * @return Description of function
	 * @throws Exception
	 */
	public FunctionDescriptor getFunctionParams(String script, String function, ASTFunction fnode) throws Exception {
		StringTokenizer st = new StringTokenizer(script, "\n");
		ArrayList<String> docs = new ArrayList<String>();
		boolean collect = false;
		String pattern = "^function\\s*" + function + "\\s*(\\s*).*";

		while (st.hasMoreTokens()) {
			String str = st.nextToken().trim();
			if (str.startsWith("/**")) {
				docs = new ArrayList<String>();
				docs.add(str);
				collect = true;
			} else if (collect) {
				if (str.startsWith("*"))
					docs.add(str);
				if (str.endsWith("*/"))
					collect = false;
			} else {
				if (str.matches(pattern)) {
					FunctionDescriptor fd = parseDoc(function, docs);
					if(fd.isCodeSnippet()) {
						fd.code.description = collectFunctionCode(script, fnode);
					}
					return fd;
				}
			}
		}
		return null;
	}

	/**
	 * Computes the last line the parent node spans.
	 * @param parent
	 * @return
	 */
	protected int computeLastLine(ASTNode parent) {
		if(parent == null)
			return 0;
		
		int line = parent.getEndLine();
		for(int i = 0; i < parent.getSlotCount(); i++) {
			ASTNode node = parent.readSlotAt(i);
			line = Math.max(computeLastLine(node), line);
		}
		return line;
	}

	/**
	 * Collect the code between the function header (consumed on entry) up until the next
	 * function body or multi-line comment.
	 * @param script 
	 * @param st
	 * @param fnode 
	 * @return
	 */
	private String collectFunctionCode(String script, ASTFunction fnode) {
		// Need fnode to compute start/end of function
		if(fnode == null)
			return "";
		
		int start = fnode.getBeginLine();
		int end = computeLastLine(fnode);
		
		StringBuffer buf = new StringBuffer();
		String[] arr = script.split("\n");
		for(int i = start; i <= end && i < arr.length; i++) {
			buf.append(arr[i]);
			buf.append("\n");
		}
		return buf.toString();
	}

	/**
	 * Returns the Java doc string for a function declaration. The format mimics
	 * the generated javadoc format the TDI documentation uses.
	 * 
	 * @param function
	 * @return
	 * @throws Exception
	 */
	public String getFunctionJavaDoc(String script, String function) throws Exception {
		FunctionDescriptor fd = getFunctionParams(script, function, null);
		if (fd == null)
			return "";
		else
			return fd.toJavaDoc();
	}

	/**
	 * Parses the java doc style in a script.
	 * 
	 * @param func
	 * @param docs
	 * @return
	 */
	private FunctionDescriptor parseDoc(String func, ArrayList<String> docs) {
		Pattern retval = Pattern.compile("^\\*\\s*@return\\s*(.*)");
		Pattern param = Pattern.compile("^\\*\\s*@param\\s*(\\w*)\\s*(.*)");
		Pattern category = Pattern.compile("^\\*\\s*@category\\s*(.*)");
		Pattern code = Pattern.compile("^\\*\\s*@code\\s*(.*)");
		FunctionDescriptor desc = new FunctionDescriptor(func, "");

		for (String str : docs) {
			if (str.startsWith("/**"))
				continue;

			Matcher m = param.matcher(str);
			if (m.matches()) {
				desc.parameters.add(new ParameterDescriptor(m.group(1), m.group(2)));
				continue;
			}

			m = category.matcher(str);
			if (m.matches()) {
				desc.category = m.group(1);
				continue;
			}

			m = retval.matcher(str);
			if (m.matches()) {
				desc.returns = new ParameterDescriptor("", m.group(1));
				continue;
			}

			m = code.matcher(str);
			if (m.matches()) {
				desc.code = new ParameterDescriptor(m.group(1), "");
				continue;
			}
			
			if (str.equals("*/"))
				break;
			
			desc.description += str.substring(1) + "\n";
		}

		return desc;
	}

	public class ParameterDescriptor {
		public ParameterDescriptor(String name, String description) {
			this.name = name;
			this.description = description;
		}

		public String name;
		public String description;
		public Class<?> type;
		public String jstype;

		public String toString() {
			return name + " (" + description + ")";
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public Class<?> getType() {
			return type;
		}

		public String getJsType() {
			return jstype;
		}

	}

	public class FunctionDescriptor extends ParameterDescriptor {

		public ParameterDescriptor code;
		public ArrayList<ParameterDescriptor> parameters = new ArrayList<ParameterDescriptor>();
		public ParameterDescriptor returns = new ParameterDescriptor("object", "");
		public String category = DEFAULT_SCRIPT_CATEGORY;

		public FunctionDescriptor(String name, String description) {
			super(name, description);
		}

		public void setParameterType(String name, String type) {
			for (ParameterDescriptor pd : parameters) {
				if (pd.name.equals(name)) {
					pd.jstype = type;
					pd.type = getClassForJSType(type);
				}
			}
		}
		
		public boolean isCodeSnippet() {
			return code != null;
		}
		
		public String getCodeSnippet() {
			if(isCodeSnippet())
				return code.description;
			else
				return "";
		}
		
		public String toJavaDoc() {
			String template = "<A NAME=\"SIGNATURE\"><!-- --></A><H3>" + "FUNCTION_NAME</H3>"
					+ "<DL><DD>FUNCTION_DESCRIPTION<P><DD>" + "		<DL>" + "			<DT><B>Parameters:</B>FUNCTION_PARAMETERS"
					+ "			<DT><B>Returns:</B>FUNCTION_RETURNS" + "		</DL>" + "	</DD>" + "</DL>" + "<HR>";

			String str = template.replaceAll("SIGNATURE", getSignature());
			str = str.replaceAll("FUNCTION_NAME", getSignature());
			str = str.replaceAll("FUNCTION_DESCRIPTION", description == null ? "" : description);

			StringBuffer buf = new StringBuffer();
			buf.append("<DL>");
			for (ParameterDescriptor pd : parameters) {
				buf.append("<DD><B>" + pd.name + "</B>: " + (pd.description == null ? "" : pd.description) + "<P>");
			}
			buf.append("</DL>");
			str = str.replaceAll("FUNCTION_PARAMETERS", buf.toString());

			if (returns != null)
				str = str.replaceAll("FUNCTION_RETURNS", returns.toString());

			return str;
		}

		public ArrayList<ParameterDescriptor> getParameters() {
			return parameters;
		}

		public ParameterDescriptor getReturns() {
			return returns;
		}

		public String getCategory() {
			return category;
		}

		public String getSignature() {
			StringBuffer buf = new StringBuffer();
			buf.append(name);
			buf.append("(");
			for (ParameterDescriptor pd : parameters) {
				if (!buf.toString().endsWith("("))
					buf.append(", ");
				buf.append(pd.name);
			}
			buf.append(")");
			return buf.toString();
		}

		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("Function Descriptor\n");
			buf.append(super.toString() + "\n");
			buf.append("** Input Parameters\n");
			for (ParameterDescriptor pd : parameters)
				buf.append(pd.toString() + "\n");
			buf.append("** Return Parameter\n");
			if (returns != null)
				buf.append(returns.toString() + "\n");

			return buf.toString();
		}

		/**
		 * Creates a formatted code snippet prefixing each line with the indent string.
		 * The code snippet is assumed to start with at one tab/space, which is removed
		 * before inserting the indent prefix.
		 * @param indent
		 * @return
		 */
		public String createSnippet() {
			String str = getCodeSnippet();
			if(str == null)
				return "";
			
			StringTokenizer st = new StringTokenizer(str, "\n");
			StringBuffer buf = new StringBuffer();
			
			while(st.hasMoreTokens()) {
				String s = st.nextToken();
				if(s.startsWith("\t"))
					s = s.substring(1);
				buf.append(s);
				buf.append("\n");
			}
			return buf.toString();
		}
	}

	public class ScriptFunctionInfo {
		public String name;
		public FunctionDescriptor descriptor = new FunctionDescriptor(null, null);
		public ScriptConfig source;

		public String getJavaDocString() {
			if(descriptor != null)
				return descriptor.toJavaDoc();
			else
				return name;
		}

		public String getName() {
			return name;
		}

		public FunctionDescriptor getDescriptor() {
			return descriptor;
		}
		
		public boolean isCodeSnippet() {
			if(descriptor != null)
				return descriptor.isCodeSnippet();
			else
				return false;
		}

		public ScriptConfig getSource() {
			return source;
		}

		/**
		 * Returns javascript code for a call to this function on the form
		 * <i>functionname(params....)</i>
		 * 
		 * @return
		 */
		public String toJavaScript() {
			StringBuffer buf = new StringBuffer();
			if(descriptor != null) {
				for (ParameterDescriptor pd : descriptor.getParameters()) {
					if (buf.length() > 0)
						buf.append(", ");
					buf.append(pd.name);
				}
			}
			buf.append(")");
			buf.insert(0, "(");
			buf.insert(0, name);
			return buf.toString();
		}

		public String createSnippet(BaseConfiguration params) {
			if(getDescriptor() == null)
				return null;
			
			String str = getDescriptor().createSnippet();
			for(ParameterDescriptor pd : getDescriptor().getParameters()) {
				String val = params.getStringParameter(pd.name);
				if(val != null)
					str = str.replaceAll("\"\\$" + pd.name + "\"", val);
			}
			
			return str;
		}
	}

	/**
	 * Records listener to receive events when the contents of this instance has
	 * changed.
	 * 
	 * @param listener
	 */
	public void addListener(Listener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Removes listener from change events notification list.
	 * 
	 * @param listener
	 */
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	/**
	 * Rescan and notify all listeners that contents have changed.
	 */
	public void fireContentsChanged() {

		try {
			map.clear();
			addScriptFunctions();
		} catch (Exception e1) {
			return;
		}

		Event e = new Event();
		for (Listener l : listeners.toArray(new Listener[0]))
			l.handleEvent(e);
	}
}
