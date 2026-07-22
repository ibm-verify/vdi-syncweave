/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

import com.ibm.di.api.local.Session;
import com.ibm.di.automation.COMProxy;
import com.ibm.di.config.eclipse.MetamergeConfigCE;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.LibraryConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.parser.ParserImpl;
import com.ibm.di.script.ScriptEngineOptions;
import com.ibm.di.script.ScriptExitCode;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.AssemblyLineComponent;
import com.ibm.di.server.AttributeMapping;
import com.ibm.di.server.BranchingComponent;
import com.ibm.di.server.RS;
import com.ibm.di.server.SearchCriteria;
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JSInterpreter;
import com.ibm.jscript.ParserResult;
import com.ibm.jscript.ASTTree.ASTAssign;
import com.ibm.jscript.ASTTree.ASTCall;
import com.ibm.jscript.ASTTree.ASTFunction;
import com.ibm.jscript.ASTTree.ASTIdentifier;
import com.ibm.jscript.ASTTree.ASTLiteral;
import com.ibm.jscript.ASTTree.ASTMember;
import com.ibm.jscript.ASTTree.ASTNode;
import com.ibm.jscript.ASTTree.ASTProgram;
import com.ibm.jscript.ASTTree.ASTVariableDecl;
import com.ibm.jscript.ASTTree.DefaultNodeVisitor;
import com.ibm.jscript.parser.FBScript2;
import com.ibm.jscript.parser.ParseException;
import com.ibm.jscript.std.AbstractPrimitiveObject;
import com.ibm.jscript.types.Descriptor;
import com.ibm.jscript.types.FBSBoolean;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.Descriptor.Param;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.WorkEntryAttributesProvider;
import com.ibm.tdi.eclipse.text.JavaScriptDocParser.ScriptFunctionInfo;

public class JavaScriptContentAssistProcessor implements IContentAssistProcessor, IContextInformationValidator {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BaseConfiguration config;
	private AssemblyLineConfig alc;
	private ConnectorConfig cc;
	private List<String> topLevelObjects = new ArrayList<String>();
	private JavaScriptDocParser scriptContext;
	private ArrayList<ASTNode> javascriptNodes;
	private Map<String, Class<?>> classMap;
	private ITextViewer currentViewer;
	private FileWriter traceFile;

	/*
	 * The node is the parsed javascript in the editor.
	 */
	private ASTProgram mainNode;

	/*
	 * The current line and column is set before completion proposals
	 * are generated.
	 */
	private int currentLine;
	private int currentCol;

	/**
	 * To prevent looping while looking up the class for a JavaScript object, this contains the names
	 * of the top level JavaScript object we are trying to find the class for.
	 */
	private HashMap<String, Class<?>> currentJavaScriptObjects = new HashMap<String, Class<?>>();

	/**
	 * Lots of Strings with the names of all the objects we know
	 */
	private final static String SYSTEM = "system";
	private final static String SESSION = "session";
	private final static String MAIN = "main";
	private final static String TASK = "task";
	private final static String WORK = "work";
	private final static String CONN = "conn";
	private final static String CURRENT = "current";
	private final static String ERROR = "error";
	private final static String ENTRY = "entry";
	private final static String RESULT = "result";
	private final static String THISCONNECTOR = "thisConnector";
	private final static String THISCOMPONENT = "thisComponent";
	private final static String RET = "ret";
	private final static String SEARCH = "search";
	private final static String CONNECTOR = "connector";
	private final static String CONFIG = "config";
	private final static String OLD = "old";
	private final static String LIST = "list";
	private final static String OUT = "out";
	private final static String INP = "inp";
	private final static String PARSER = "parser";
	private final static String SOURCE = "source";
	private final static String COMPROXY = "COMProxy";
	private final static String METHOD = "method";
	private final static String SIMULATIONRESULT = "simulationResult";

	private final static List<String> onlyWorkHooks; // Only work in these
	private final static List<String> currentHooks; // work, conn & current

	// Names of some of the hooks that needs special handling
	final static String OR_GETNEXT = "override_getnext";
	final static String OR_LOOKUP = "override_lookup";
	final static String OR_MODIFY = "override_modify";
	final static String WILL_EXEC = "before_execute";
	final static String BEFORE_UPDATE = "before_update";
	final static String BEFORE_DELTA = "before_delta";
	final static String SIMULATION = "simulation_hook";

	static {
		onlyWorkHooks = Arrays.asList("before_getnextclient", "before_getnext", "before_initialize", "after_initialize",
				"initialize_fail", "connect_init", "on_connection_failure", "before_selectEntries", "after_selectEntries",
				"before_close", "after_close", "close_fail", "before_lookup", "lookup_multiple", "lookup_nomatch",
				"delete_multiple", "delete_nomatch", BEFORE_UPDATE, "update_multiple", BEFORE_DELTA);
		currentHooks = Arrays.asList("after_lookup", "lookup_ok", "after_update", "update_ok", "before_modify", "modify_nochange",
				"modify_apply", "after_modify", "after_delta", "delta_ok");
	}

	/**
	 * @param config
	 *            The configuration we use to know which objects to propose
	 * @param scriptContext2 
	 */
	public JavaScriptContentAssistProcessor(BaseConfiguration config, JavaScriptSourceViewerConfiguration svc) {
		setConfig(config);
		this.scriptContext = svc.getScriptContext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.jface.text.contentassist.IContentAssistProcessor#
	 * computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

		try {
			currentLine = viewer.getDocument().getLineOfOffset(offset) + 1;
			currentCol = offset - viewer.getDocument().getLineOffset(currentLine-1);
		} catch (BadLocationException e) {
			currentLine = -1;
		}

		List<String> objects = parseContext(viewer, offset);
		List<Class<?>> classes = parseObjects(objects);
		ArrayList<ICompletionProposal> list = new ArrayList<ICompletionProposal>();

		if (objects.size() == 0) {
			addTopLevelObjects("", offset, 0, list);
			sortCompletionList(list);
		} else {
			String obj = objects.get(0);
			Class<?> parent = (classes.size() > 1 ? classes.get(1) : classes.get(0));
			int start = offset - obj.length();

			if (objects.size() == 2 && objects.get(1).equals(WORK)) {
				addWorkEntryAttributes(obj, start, obj.length(), list);
			} else if (objects.size() >= 2 && objects.get(objects.size() - 1).equals(CONN)) {
				if (objects.size() == 2)
					addConnectorAttributes(obj, start, obj.length(), list);
				else
					addConnectorAttributes(objects, start, list);
			} else if (objects.size() == 2 && objects.get(1).equals(ERROR)) {
				addErrorEntryAttributes(obj, start, obj.length(), list);
			} else if (objects.size() == 1) {
				addTopLevelObjects(obj, start, obj.length(), list);
			}
			sortCompletionList(list);

			// Make sure class methods and props appear after connector attributes etc
			ArrayList<ICompletionProposal> list2 = new ArrayList<ICompletionProposal>();
			addClassMethods(parent, obj, start, obj.length(), obj, list2);
			sortCompletionList(list2);
			
			list.addAll(list2);
		}

		return list.toArray(new ICompletionProposal[list.size()]);

	}

	private void sortCompletionList(ArrayList<ICompletionProposal> list) {
		Collections.sort(list, new Comparator<ICompletionProposal>() {
			public int compare(ICompletionProposal o1, ICompletionProposal o2) {
				return o1.getDisplayString().compareTo(o2.getDisplayString());
			}
		});
	}

	/*
	 * (non-Javadoc) Added new method for defect #13632
	 */
	private void addErrorEntryAttributes(String prefix, int start, int length, ArrayList<ICompletionProposal> list) {
		for (String str : new String[] { "message", "exception", "status", "connectorname", "operation" }) {
			if (str.startsWith(prefix.toLowerCase())) {
				list.add(new CompletionProposal(str, start, length, str.length(), null, str, null, null));
			}
		}
	}

	private void addConnectorAttributes(String prefix, int start, int length, ArrayList<ICompletionProposal> list) {
		if (cc == null)
			return;

		AttributeMapConfig amc = (AttributeMapConfig) Utils.getParentConfig(config, AttributeMapConfig.class);
		boolean input = amc != null ? Utils.isInputMap(amc) : Utils.isInputConnector(cc);

		SchemaConfig sc = cc.getSchema(input);
		List<String> names = sc.getItemNames();
		Collections.sort(names);
		Image img = Activator.getImage("Attribute");
		for (String name : names) {
			if (name.toLowerCase().startsWith(prefix.toLowerCase())) {
				String displayString = name;
				name = Utils.getScript(null, name);
				if (name.startsWith("["))
					list.add(new CompletionProposal(name, start - 1, length + 1, name.length(), img, displayString, null, null));
				else
					list.add(new CompletionProposal(name, start, length, name.length(), img, displayString, null, null));
			}
		}
	}

	private void addConnectorAttributes(List<String> objects, int start, ArrayList<ICompletionProposal> list) {

		if (cc == null)
			return;

		AttributeMapConfig amc = (AttributeMapConfig) Utils.getParentConfig(config, AttributeMapConfig.class);
		boolean input = amc != null ? Utils.isInputMap(amc) : Utils.isInputConnector(cc);

		SchemaConfig sc = cc.getSchema(input);
		SchemaItemConfig sic = sc.getItem(objects.get(objects.size() - 2));
		if (sic == null)
			return;
		ContainerConfig items = sic.getChildSchemaList();
		for (int i = objects.size() - 3; i >= 1; i--) {
			sic = (SchemaItemConfig) items.getConfig(objects.get(i));
			if (sic == null)
				return;
			items = sic.getChildSchemaList();
		}
		String prefix = objects.get(0).toLowerCase();
		List<String> names = items.getChildNames();
		Collections.sort(names);
		Image img = Activator.getImage("Attribute");
		for (String name : names) {
			if (name.toLowerCase().startsWith(prefix)) {
				String displayString = name;
				name = Utils.getScript(null, name);
				if (name.startsWith("["))
					list.add(new CompletionProposal(name, start - 1, prefix.length() + 1, name.length(), img, displayString, null,
							null));
				else
					list.add(new CompletionProposal(name, start, prefix.length(), name.length(), img, displayString, null, null));
			}
		}
	}

	private void addWorkEntryAttributes(String prefix, int start, int length, ArrayList<ICompletionProposal> list) {
		if (alc != null) {
			WorkEntryAttributesProvider wep = new WorkEntryAttributesProvider(null);
			wep.inputChanged(null, null, alc);
			Image img = Activator.getImage("Attribute");
			for (String str : wep.getSortedAttributes()) {
				if (str.toLowerCase().startsWith(prefix.toLowerCase())) {
					String displayString = str;
					str = Utils.getScript(null, str);
					if (str.startsWith("["))
						list.add(new CompletionProposal(str, start - 1, length + 1, str.length(), img, displayString, null, null));
					else
						list.add(new CompletionProposal(str, start, length, str.length(), img, displayString, null, null));
				}
			}
		}
	}

	private List<Class<?>> parseObjects(List<String> objects) {
		List<Class<?>> list = new ArrayList<Class<?>>();
		Class<?> parentClass = null;
		currentJavaScriptObjects.clear();
		for (int i = objects.size() - 1; i >= 0; i--) {
			String str = objects.get(i);
			Class<?> cls = getClassForObject(parentClass, str);
			list.add(0, cls);
			parentClass = cls;
		}
		return list;
	}

	/**
	 * Generate a list of objects based on the current position in the document.
	 * This method scans backwards to the start of the expression.
	 * 
	 */
	private List<String> parseContext(ITextViewer viewer, int offset) {
		List<String> list = new ArrayList<String>();

		int start = offset - 1;
		int end = start;
		int openParens = 0;
		int openBracket = 0;

		// parseJavaScriptContext(viewer, offset);

		openTrace();
		if (traceFile != null) {
			writeTrace("parseContext: offset=" + offset + "\n");
			writeTrace("-- Text Buffer\n");
			writeTrace(viewer.getDocument().get());
			writeTrace("\n[***]\n");
		}

		try {
			char oldch = 0xff;
			while (start >= 0) {
				char ch = viewer.getDocument().getChar(start--);

				if (traceFile != null)
					writeTrace(" -- nextch: " + ch + " (" + Integer.toHexString((int) ch) + "\n");

				if (ch == ')') {
					openParens++;
				} else if (ch == '(' && openParens > 0) {
					openParens--;
				} else if (ch == ']') {
					openBracket++;
				} else if (ch == '[' && openBracket > 0) {
					openBracket--;
					// -- This may an attribute: work["http.body"]
					try {
						String token = viewer.getDocument().get(start + 2, (end - start) - 2);
						list.add(token);
					} catch (BadLocationException ble) {
						SystemFunctions.doNothing();
					}
					// -- we should never include bracket contents in the
					// expression
					end = start;
					//
				} else if (openBracket > 0 || openParens > 0) {
					// Skip expressions inside brackets/parents.
					continue;
				} else if (ch == '.') {
					list.add(viewer.getDocument().get(start + 2, (end - start) - 1));
					end = start;
				} else if (Character.isWhitespace(ch) && oldch == '.') {
					// Permit expressions to cross line boundaries
					SystemFunctions.doNothing();
				} else if (Character.isJavaIdentifierPart(ch)) {
					// part of a javascript identifier
					SystemFunctions.doNothing();
				} else if (ch == '"') {
					continue;
				} else {
					start++;
					break;
				}
				oldch = ch;
			}

			if (traceFile != null)
				writeTrace(" -- end loop: start=" + start + ", end=" + end);

			if (start < end)
				list.add(viewer.getDocument().get(start + 1, (end - start)));

		} catch (BadLocationException e) {
			EclipseAppender.logerror(e.toString(), e);
		}

		if (traceFile != null) {
			writeTrace("ParseContext Returns\n");
			for (String str : list) {
				writeTrace(">> '" + str + "'");
			}

			closeTrace();
		}

		return list;
	}

	/**
	 * Add class method completions. This method also checks if the provided class has a javascript
	 * object prototype (e.g. string, number) and adds those properties in addition to the java methods.
	 * 
	 * Finally, this method checks if the class has extensions via the system namespace scripts.
	 * Extensions are script configs with a "extendsJavaClass" parameter specifying the class it extends.
	 * 
	 * @param clsForObject
	 * @param object
	 * @param start
	 * @param length
	 * @param prefix
	 * @param list
	 */
	private void addClassMethods(Class<?> clsForObject, String object, int start, int length, String prefix,
			ArrayList<ICompletionProposal> list) {

		Class<?> classForObject = clsForObject;
		if (classForObject == null)
			return;
		
		
		// Javascript always wraps these to internal objects
		if(clsForObject == String.class)
			classForObject = FBSString.class;
		else if(clsForObject == Boolean.class)
			classForObject = FBSBoolean.class;

		HashMap<String, String> sigmap = new HashMap<String, String>();
		if (FBSValue.class.isAssignableFrom(classForObject)) {
			classForObject = addJavascriptObjectProperties(classForObject, object, start, length, prefix, list, sigmap);
		}

		addClassFields(classForObject, object, start, length, prefix, list);

		ArrayList<Method> methods = new ArrayList<Method>();
		for (Method m : classForObject.getMethods()) {
			methods.add(m);
		}

		for (Method m : methods) {
			if (m.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
				String str = m.getName() + "(";
				String display = "";
				String jssig = str;
				for (Class<?> p : m.getParameterTypes()) {
					if (display.length() > 0) {
						display += ", ";
						jssig += ",";
					}
					display += shortClassName(p);
					
					// Java methods where a param is assignable from a string/int
					// will cause the javascript method to be used instead.
					if(Integer.class.isAssignableFrom(p))
						jssig += "int";
					else if(String.class.isAssignableFrom(p))
						jssig += "string";
					else
						jssig += shortClassName(p);
				}
				jssig += ")";
				str += ")";
				
				//
				// Check if this method is masked by a javascript function
				//
				if(sigmap.containsKey(jssig)) {
					continue;
				}
				

				IContextInformation comp = new JavaScriptContextInfo(display, null, display);

				display = m.getName() + "(" + display + ") ";

				String cls = shortClassName(m.getReturnType());
				display += cls + " - " + shortClassName(m.getDeclaringClass());
				int pos = str.length();
				if (m.getParameterTypes().length > 0)
					pos--;

				CompletionProposal proposal = new CompletionProposal(str, start, length, pos, null, display, comp, display);
				proposal.createAdditionalProposalInfo(classForObject, m);
				list.add(proposal);
			}
		}
	}

	private void addClassFields(Class<?> classForObject, String object, int start, int length, String prefix,
			ArrayList<ICompletionProposal> list) {
		
		Image img = Activator.getImage("localvariable_obj");

		ArrayList<Field> fields = new ArrayList<Field>();
		for (Field f : classForObject.getFields()) {
			fields.add(f);
		}
		
		/*
		 * Special case for this type. The constants are the keywords for the get method
		 * so from a javascript point of view we really want the keywords and not the field per se.
		 */
		if(classForObject == AssemblyLineComponent.class) {
			for (Field m : fields) {
				try {
					Object value = m.get(null);
					if(value instanceof String && value.toString().toLowerCase().startsWith(prefix.toLowerCase())) {
						String str = value.toString();
						if(str.startsWith("$")) {
							continue;
						}
						String display = str + " - AssemblyLineComponent";
						StringBuffer addl = JavaDocReader.getJavaDocs(classForObject, m);
						if(addl == null) {
							addl = new StringBuffer();
							addl.append(str);
						}
						IContextInformation comp = new JavaScriptContextInfo(str, null, addl.toString());
						list.add(new CompletionProposal(str, start, length, str.length(), img, display, comp, addl.toString()));
						continue;
					}
				} catch (Exception e) {
					SystemFunctions.doNothing();
					// fall through to providing the field as normal
				}
				if (m.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
					String str = m.getName();
					String cls = shortClassName(m.getType());
					String display = str + " - " + cls;
					IContextInformation comp = new JavaScriptContextInfo(display, null, display);
					list.add(new CompletionProposal(str, start, length, str.length(), img, display, comp, display));
				}
			}
		} else {
			for (Field m : fields) {
				if (m.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
					String str = m.getName();
					String cls = shortClassName(m.getType());
					String display = str + " - " + cls;
					IContextInformation comp = new JavaScriptContextInfo(display, null, display);
					list.add(new CompletionProposal(str, start, length, str.length(), img, display, comp, display));
				}
			}
		}
	}

	private String shortClassName(Class<?> clazz) {
		String name = clazz.getName();
		if (clazz.isArray()) {
			name = clazz.getComponentType().getName();
		}
		if (name.indexOf(".") != -1)
			name = name.substring(name.lastIndexOf(".") + 1);

		if (clazz.isArray())
			return name + "[]";
		else
			return name;
	}

	/**
	 * Add the completion proposals based on an FBSObject.
	 * 
	 * @param cls
	 * @param object
	 * @param start
	 * @param length
	 * @param prefix
	 * @param list
	 * @return The java class that backs the javascript class (e.g. FBSString
	 *         --> java.lang.String)
	 */
	private Class<?> addJavascriptObjectProperties(Class<?> cls, String object, int start, int length, String prefix,
			ArrayList<ICompletionProposal> list, HashMap<String, String> sigmap) {
		AbstractPrimitiveObject proto;
		Class<?> javaClass;
		if (cls == FBSNumber.class) {
			proto = ScriptEngineOptions.get().getRegistry().numberPrototype;
			javaClass = Double.class;
		} else if (cls.getName().endsWith("FBSNumber$FBSNumberInt")) {
			proto = ScriptEngineOptions.get().getRegistry().numberPrototype;
			javaClass = Integer.class;
		} else if (cls == FBSBoolean.class) {
			proto = ScriptEngineOptions.get().getRegistry().booleanPrototype;
			javaClass = Boolean.class;
		} else if (cls == FBSString.class) {
			proto = ScriptEngineOptions.get().getRegistry().stringPrototype;
			javaClass = String.class;
		} else {
			return Object.class;
		}

		HashSet<Object> members = new HashSet<Object>();
		proto.getAllPropertiesForHelp(ScriptEngineOptions.get(), members);
		for (Iterator<Object> iter = members.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof Descriptor.Method) {
				Descriptor.Method m = (com.ibm.jscript.types.Descriptor.Method) obj;
				if (m.getName().toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase())) {
					String str = m.getName() + "()";
					String display = m.toString();
					CompletionProposal cp = new CompletionProposal(str, start, length, str.length() - 1, Activator
							.getImage("Script_16"), display, null, display);
					list.add(cp);
					
					// Add a signature without the param names to the sigmap
					StringBuffer buf = new StringBuffer();
					buf.append(m.getName() + "(");
					for(int i = 0; i < m.getParameterCount(); i++) {
						Param p = m.getParameter(i);
						if(i>0)
							buf.append(",");
						buf.append(p.getType().getJSName());
					}
					buf.append(")");
					sigmap.put(buf.toString(), "");
				}
				
			} else if (obj instanceof Descriptor.Field) {
				Descriptor.Field field = (com.ibm.jscript.types.Descriptor.Field) obj;
				if (field.getName().toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase())) {
					String str = field.getName();
					CompletionProposal cp = new CompletionProposal(str, start, length, str.length(), Activator
							.getImage("localvariable_obj"), str, null, str);
					list.add(cp);
				}
			}
		}

		return javaClass;
	}

	private void addTopLevelObjects(String prefix, int start, int length, ArrayList<ICompletionProposal> list) {
		String pref = prefix.toLowerCase();
		for (String str : topLevelObjects) {
			if (str.toLowerCase(Locale.ENGLISH).startsWith(pref)) {
				Class<?> cls = getClassForTopLevelObject(str);
				Image img = Activator.getImage("javafile_obj");
				if(cls == Entry.class)
					img = Activator.getImage("ibm_entryheader_pal16");
				else if(cls == AssemblyLineComponent.class)
					img = Activator.getImage("Connector_16");
				else if(cls == Attribute.class)
					img = Activator.getImage("Attribute");
				else if(cls == RS.class)
					img = Activator.getImage("Neo_16");
				else if(cls == AssemblyLine.class)
					img = Activator.getImage("AssemblyLine_16");
				else if(cls == SearchCriteria.class)
					img = Activator.getImage("Connector_Lookup_Enabled");
				
				list.add(new CompletionProposal(str, start, length, str.length(), img, null, null, null));
			}
		}
		
		//
		// Add script functions from library
		//
		if (scriptContext != null) {
			for (ScriptFunctionInfo sfi : scriptContext.getAllFunctions()) {
				String str = sfi.name;
				if (str.startsWith("sys_code_snippet_"))
					continue;
				if (str.toLowerCase(Locale.ENGLISH).startsWith(pref)) {
					if(sfi.getDescriptor() != null)
						str = sfi.getDescriptor().getSignature();
					else
						str = sfi.getName();
					CompletionProposal cp = new CompletionProposal(str, start, length, str.length(), Activator
							.getImage("Script_16"), str, null, str);
					cp.setAdditionalProposalInfo(sfi.getJavaDocString());
					list.add(cp);
				}
			}
		}
		
		//
		// Add defined variables in the script
		//
		if(mainNode != null) {
			final ArrayList<String> vars = new ArrayList<String>();
			final HashMap<String, String> addl = new HashMap<String, String>();
			mainNode.visitAllNodes(new DefaultNodeVisitor() {
				@Override
				public Object visitAssign(ASTAssign x, Object param) {
					// Check if the assignment has a matching identifier as its left node
					// Note that identifiers are global in nature as opposed to variable declarations
					if(x.getBeginLine() != currentLine) {
						ASTAssign assign = (ASTAssign) x;
						if (assign.getLeftNode() instanceof ASTIdentifier) {
							String key = ((ASTIdentifier) assign.getLeftNode()).getIdentifierName();
							vars.add(key);
							ASTNode node = assign.getRightNode();
							if (node instanceof ASTLiteral) {
								ASTLiteral literal = (ASTLiteral) node;
								addl.put(key, "=" + literal.getValue().toString());
							} else if (node instanceof ASTIdentifier) {
								addl.put(key, "=" + ((ASTIdentifier)node).getIdentifierName());
							} else if (node instanceof ASTMember || node instanceof ASTCall) {
								addl.put(key, "=" + getNodeText(node));
							}
						}
					}
					return super.visitAssign(x, param);
				}

				@Override
				public Object visitVariableDecl(ASTVariableDecl x, Object param) {
					// Check if assignment is in a different block than the current line
					// var statements are block private
					if(x.getBeginLine() != currentLine && isNodeInSameBlock(x, currentLine) == null) {
						// A variable decl can have multiple variables declared
						ASTVariableDecl var = (ASTVariableDecl) x;
						for(int i = 0; i < var.getEntryCount(); i++) {
							vars.add(var.getEntryAt(i).getName());
						}
					}
					return super.visitVariableDecl(x, param);
				}
				
			});
			
			for(String str : vars) {
				if (str.toLowerCase(Locale.ENGLISH).startsWith(pref)) {
					String info = addl.get(str) != null ? addl.get(str) : str + " - script variable";
					CompletionProposal cp = new CompletionProposal(str, start, length, str.length(), Activator
							.getImage("localvariable_obj"), str, null, info);
					list.add(cp);
				}
			}
		}
	}

	/**
	 * Return the full name of the node. The node's name is appended to its parent nodes name
	 * to provide the full path of the node (e.g. object.functionCall(x,y).toString()).
	 * 
	 * @param node ASTMember, ASTIdentifier, ASTCall or ASTLiteral node. 
	 * @return The full name/path of the node
	 */
	protected String getNodeText(ASTNode node) {
		ASTNode nextNode = null;
		String name = null;
		if(node instanceof ASTMember) {
			ASTMember member = (ASTMember) node;
			name = member.getMemberName();
			nextNode = member.getLeftNode();
		} else if (node instanceof ASTIdentifier) {
			name = ((ASTIdentifier)node).getIdentifierName();
		} else if (node instanceof ASTCall) {
			ASTCall call = (ASTCall) node;
			nextNode = call.getLeftNode();
		} else if (node instanceof ASTLiteral) {
			FBSValue value = ((ASTLiteral)node).getValue();
			if(value.isString()) {
				String s = value.stringValue();
				if(s.contains("\""))
					name = "'" + s + "'";
				else
					name = "\"" + s + "\"";
			} else {
				name = value.stringValue();
			}
		}
		
		if(nextNode != null) {
			String str = getNodeText(nextNode); 
			if(name != null && name.length() > 0)
				name = str + "." + name;
			else
				name = str;
			
			if(node instanceof ASTCall) {
				ASTCall call = (ASTCall) node;
				StringBuilder params = new StringBuilder("(");
				if (call.getASTArgumentList() != null) {
					for(int i = 0; i < call.getASTArgumentList().getSlotCount(); i++) {
						String p = getNodeText(call.getASTArgumentList().readSlotAt(i));
						if(i > 0)
							params.append(", ");
						params.append(p);
					}
				}
				params.append(")");
				name += params.toString();
			}
		}
			
		return name;
	}

	private void addAssemblyLineObjects() {
		if (cc != null) {
			topLevelObjects.add(THISCONNECTOR);
			topLevelObjects.add(THISCOMPONENT);
		}

		topLevelObjects.add(ERROR);

		if (alc == null)
			return;

		for (BaseConfiguration b : alc.getEntryFeedComponents().getConfigurations(null)) {
			String s = b.getShortName();
			topLevelObjects.add(s);
			classMap.put(s, AssemblyLineComponent.class);
		}

		for (BaseConfiguration b : alc.getDataFlowComponents().getConfigurations(null)) {
			if (b instanceof ConnectorConfig && b.getEnabled()) {
				String s = b.getShortName();
				topLevelObjects.add(b.getShortName());
				classMap.put(s, AssemblyLineComponent.class);
			}
		}
	}

	/**
	 * This metohd returns the class for the specified object. If parent is
	 * null, object is a top level object derived from the list of predefined
	 * objects or the script engine itself. If parent is not null, it is
	 * searched for a matching method/property.
	 * 
	 * @param parent
	 * @param object
	 * @return
	 */
	private Class<?> getClassForObject(Class<?> parent, String object) {
		if (parent == null)
			return getClassForTopLevelObject(object);

		String str = object;
		int parens = str.indexOf("(");
		if (parens != -1) {
			str = str.substring(0, parens);
		}

		// May be a javascript object
		AbstractPrimitiveObject proto = null;
		Class<?> javaClass = parent;
		if (FBSValue.class.isAssignableFrom(parent)) {
			if (parent == FBSNumber.class) {
				proto = ScriptEngineOptions.get().getRegistry().numberPrototype;
				javaClass = Double.class;
			} else if (parent.getName().endsWith("FBSNumber$FBSNumberInt")) {
				proto = ScriptEngineOptions.get().getRegistry().numberPrototype;
				javaClass = Integer.class;
			} else if (parent == FBSBoolean.class) {
				proto = ScriptEngineOptions.get().getRegistry().booleanPrototype;
				javaClass = Boolean.class;
			} else if (parent == FBSString.class) {
				proto = ScriptEngineOptions.get().getRegistry().stringPrototype;
				javaClass = String.class;
			}
		}

		// Check for prototype methods and properties
		if (proto != null) {
			HashSet<Object> members = new HashSet<Object>();
			proto.getAllPropertiesForHelp(ScriptEngineOptions.get(), members);
			for (Iterator<Object> iter = members.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (obj instanceof Descriptor.Method) {
					Descriptor.Method m = (com.ibm.jscript.types.Descriptor.Method) obj;
					if (m.getName().equals(str)) {
						return jsTypeToFBS(m.getType());
					}
				} else if (obj instanceof Descriptor.Field) {
					Descriptor.Field field = (com.ibm.jscript.types.Descriptor.Field) obj;
					if (field.getName().equals(str)) {
						return jsTypeToFBS(field.getType());
					}
				}
			}
		}

		// For now we return the first match
		// TODO: Be more intelligent about the possible matches
		for (Method m : javaClass.getMethods()) {
			if (m.getName().equals(str))
				return m.getReturnType();
		}

		// Check fields
		for (Field f : javaClass.getFields()) {
			if (f.getName().equals(str))
				return f.getType();
		}

		// If the name is unknown then we assume it is an attribute
		// if the parent class is either Entry or Attribute
		if (javaClass == Entry.class || javaClass == Attribute.class)
			return Attribute.class;

		// Return top level class
		return Object.class;
	}

	/**
	 * Returns the FBS<class> for the FBSType or the java class if FBSType is
	 * not a string, number of boolean.
	 * 
	 * @param type
	 * @return
	 */
	private Class<?> jsTypeToFBS(FBSType type) {
		if (type == null)
			return Object.class;
		else if (type.isString())
			return FBSString.class;
		else if (type.isNumber())
			return FBSNumber.class;
		else if (type.isBoolean())
			return FBSBoolean.class;
		else
			return type.getJavaClass();
	}

	/**
	 * Returns the class for a top-level object.
	 * 
	 */
	private Class<?> getClassForTopLevelObject(String object) {
		Class<?> c = classMap.get(object);
		if (c != null)
			return c;

		Class<?> cls = getJavaScriptObjectClass(object);
		if(cls != null)
			return cls;
		else
			return Object.class;
	}

	/**
	 * Returns the class for a top-level script engine object
	 * 
	 */
	
	private Class<?> getJavaScriptObjectClass(String object) {
		if (currentJavaScriptObjects.containsKey(object))
			return currentJavaScriptObjects.get(object);
		currentJavaScriptObjects.put(object, null);
		
		JSInterpreter js = new JSInterpreter(ScriptEngineOptions.get());
		try {
			FBSValue obj = js.getGlobalObject().get(object);
			if (obj != null) {
				switch (obj.getType()) {
				case FBSValue.BOOLEAN_TYPE:
					currentJavaScriptObjects.put(object, FBSBoolean.class);
					return FBSBoolean.class;
				case FBSValue.NUMBER_TYPE:
					currentJavaScriptObjects.put(object, FBSNumber.class);
					return FBSNumber.class;
				case FBSValue.STRING_TYPE:
					currentJavaScriptObjects.put(object, FBSString.class);
					return FBSString.class;
				}
			}
		} catch (InterpretException e) {
			SystemFunctions.doNothing();
		}
		
		// Could be a string constant
		if(object.startsWith("\"") && object.endsWith("\"")) {
			currentJavaScriptObjects.put(object, FBSString.class);
			return FBSString.class;
		}
		
		// Number constant
		try {
			Integer.parseInt(object);
			currentJavaScriptObjects.put(object, FBSNumber.class);
			return FBSNumber.class;
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		
		String fname = object.indexOf("(") != -1 ? object.substring(0, object.indexOf("(")) : object;

		Class<?> retClass = null;
		
		if (javascriptNodes != null) {
			for (ASTNode node : javascriptNodes) {
				
				// If an assignment or definition is after the assignment we
				// cannot use it
				if (node.getBeginLine() == currentLine && node.getBeginCol() > currentCol) {
					continue;
				}
				
				if (node instanceof ASTFunction) {
					ASTFunction f = (ASTFunction) node;
					if (scriptContext != null && f.getName().equals(fname)) {
						retClass = scriptContext.getClassForJSType(f.getReturnType());
					}

				} else if (node instanceof ASTVariableDecl) {

					// Check if assignment is in a different block than the current line
					// var statements are block private
					if(isNodeInSameBlock(node, currentLine) != null)
						continue;

					// A variable decl can have multiple variables declared
					ASTVariableDecl var = (ASTVariableDecl) node;
					for(int i = 0; i < var.getEntryCount(); i++) {
						com.ibm.jscript.ASTTree.ASTVariableDecl.Entry entry = var.getEntryAt(i);
						if(entry.getName().equals(object)) {
							Class<?> cls = deriveClassFromNode(entry.getInitNode());
							if(cls != null)
								retClass = cls;
						}
					}
					

				} else if (node instanceof ASTAssign) {
					
					// Check if the assignment has a matching identifier as its left node
					// Note that identifiers are global in nature as opposed to variable declarations
					ASTAssign assign = (ASTAssign) node;
					if (assign.getLeftNode() instanceof ASTIdentifier &&
							((ASTIdentifier) assign.getLeftNode()).getIdentifierName().equals(object)) {
						Class<?> cls = deriveClassFromNode(assign.getRightNode());
						if(cls != null)
							retClass = cls;
					}
				}
			}
		}
		
		if(retClass == null && scriptContext != null)
			retClass = scriptContext.getFunctionReturnClass(fname);

		currentJavaScriptObjects.put(object, retClass);
		return retClass;
	}

	/**
	 * Returns the list of top level objects
	 * 
	 * @return
	 */
	public List<String> getTopLevelObjects() {
		return topLevelObjects;
	}

	/**
	 * Returns the JavaScriptDocParser for the current script. The doc parser scans included scripts
	 * for functions and extracts the documentation from the header.
	 * 
	 * @return
	 */
	public JavaScriptDocParser getScriptContext() {
		return scriptContext;
	}

	/**
	 * Checks if the provided node is in the same block as <i>line</i>
	 * 
	 * @param node ASTFunction of other block or null if node is in the same block as line
	 * @param line The line number to compare against
	 * @return
	 */
	private ASTFunction isNodeInSameBlock(ASTNode node, int line) {
		ASTNode parent = node;
		while(parent != null) {
			if(parent instanceof ASTFunction) {
				int lastLine = computeLastLine(parent);
				if(line < parent.getBeginLine() || line > lastLine) {
					return (ASTFunction) parent;
				}
			}
			parent = parent.getParent();
		}
		return null;
	}
	
	/**
	 * Follows the path of a node to determine the class of the derived value. The node can
	 * be a literal, an identifier or a member/call.
	 * 
	 * @param node
	 * @return
	 */
	private Class<?> deriveClassFromNode(ASTNode node) {
		if (node instanceof ASTLiteral) {
			ASTLiteral literal = (ASTLiteral) node;
			return literal.getValue().getClass();

		} else if (node instanceof ASTIdentifier) {
			return getJavaScriptObjectClass(((ASTIdentifier) node).getIdentifierName());

		} else if (node instanceof ASTMember || node instanceof ASTCall) {
			ArrayList<ASTNode> path = new ArrayList<ASTNode>();
			ASTNode first = null;
			if (node instanceof ASTMember)
				first = ((ASTMember) node).getLeftNode();
			else
				first = ((ASTCall) node).getLeftNode();

			for (ASTNode n = first; n != null;) {
				path.add(0, n);
				if (n instanceof ASTIdentifier) {
					n = null;
				} else if (n instanceof ASTMember) {
					n = ((ASTMember) n).getLeftNode();
				} else if (n instanceof ASTCall) {
					path.remove(n);
					n = ((ASTCall) n).getLeftNode();
					path.add(0, n);
					if (n instanceof ASTIdentifier) {
						n = null;
					} else if (n instanceof ASTMember) {
						n = ((ASTMember) n).getLeftNode();
					}
				} else {
					path.remove(n);
					n = null;
				}
			}

			Class<?> cls = null;
			for (ASTNode n : path) {
				if (n instanceof ASTIdentifier)
					cls = getClassForObject(cls, ((ASTIdentifier) n).getIdentifierName());
				else if (n instanceof ASTMember)
					cls = getClassForObject(cls, ((ASTMember) n).getMemberName());
			}

			if (cls != null)
				return cls;
		}
		
		return null;		
	}
	
	/**
	 * Computes the last line of a node based on the location of its children.
	 * 
	 * @param parent
	 * @return line for the child with the highest line number
	 */
	private int computeLastLine(ASTNode parent) {
		if(parent == null)
			return 0;
		
		int line = parent.getEndLine();
		for(int i = 0; i < parent.getSlotCount(); i++) {
			ASTNode node = parent.readSlotAt(i);
			line = Math.max(computeLastLine(node), line);
		}
		return line;
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		// IContextInformation comp = new IContextInformation() {
		// public String getContextDisplayString() {
		// return "Context display string";
		// }
		//
		// public Image getImage() {
		// return null;
		// }
		//
		// public String getInformationDisplayString() {
		// The <b> tag is not recognized
		// return
		// "<b>Information</b> display multi string\nThe second line goes here";
		// }
		// };
		// return new IContextInformation[]{comp};
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.' };
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { '(' };
	}

	public IContextInformationValidator getContextInformationValidator() {
		return this;
	}

	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public BaseConfiguration getConfig() {
		return config;
	}

	public void setConfig(BaseConfiguration config) {
		this.config = config;
		initClassMap();
		topLevelObjects.clear();
		addGeneralObjects();
		if (config == null)
			return;
		cc = (ConnectorConfig) Utils.getParentConfig(config, ConnectorConfig.class);
		alc = (AssemblyLineConfig) Utils.getParentConfig(config, AssemblyLineConfig.class);
		boolean separateScriptEngine = false;

		if (config instanceof HookConfig) {
			addTopLevelObjects((HookConfig) config);
		} else if (config instanceof AttributeMapItem) {
			addTopLevelObjects((AttributeMapItem) config);
		} else if (config instanceof RawConnectorConfig) {
			separateScriptEngine = true;
			addTopLevelForScriptConnector();
		} else if (config instanceof ParserConfig) {
			separateScriptEngine = true;
			addTopLevelForScriptParser();
		} else if (config instanceof LinkCriteriaConfig) {
			topLevelObjects.add(RET);
			classMap.put(RET, SearchCriteria.class);
		} else if (config instanceof BranchingConfig) {
			topLevelObjects.add(RET);
			classMap.put(RET, BranchingComponent.class);
		} else if (config != null && config.getParent() instanceof FunctionConfig) {
			separateScriptEngine = true;
			addTopLevelForScriptFunction();
		}

		if (separateScriptEngine)
			topLevelObjects.remove(WORK);
		else
			addAssemblyLineObjects();

		scriptContext = null;
		try {
			if (alc != null)
				scriptContext = JavaScriptDocParser.getDocParserFor(alc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Collections.sort(topLevelObjects);
	}

	/**
	 * Add objects that are predefined by the script engine
	 * 
	 */
	private void addGeneralObjects() {
		topLevelObjects.add(TASK);
		topLevelObjects.add(MAIN);
		topLevelObjects.add(RESULT);
		topLevelObjects.add(WORK);
		topLevelObjects.add(SYSTEM);
		topLevelObjects.add(SESSION);
		
		String os = System.getProperty("os.name");
		if (os != null && os.startsWith("Windows")) {
			topLevelObjects.add(COMPROXY);
			classMap.put(COMPROXY, COMProxy.class);
		}

		LibraryConfig lib = null;
		try {
			IProject p = Utils.getProjectFor(config);
			if (p == null)
				return;
			IFile file = Utils.getSolutionProps(p);
			if (file == null)
				return;
			MetamergeConfig mc = new MetamergeConfigCE(file);
			lib = (LibraryConfig) mc.lookup(MetamergeConfig.DEFAULT_LIBRARY_FOLDER);
		} catch (Exception e) {
			return;
		}
		if (lib == null)
			return;

		for (Iterator<String> i = lib.getDataIterator(); i.hasNext();) {
			String key = i.next();
			topLevelObjects.add(key);
			try {
				classMap.put(key, Class.forName(lib.getStringParameter(key)));
			} catch (ClassNotFoundException e) {
				// Do nothing, this is not a problem
				SystemFunctions.doNothing();
			}
		}
	}

	/**
	 * Add script object names to topLevelObjects that are present for hooks.
	 * 
	 * @param hc
	 */
	private void addTopLevelObjects(HookConfig hc) {
		String hookName = (String) hc.getHookName();
		if (hookName == null || hookName.equals(WILL_EXEC))
			return;

		// complicated? sure
		if (Utils.hasLinkRequirements(cc) && !hookName.equals(BEFORE_UPDATE) && !hookName.equals(BEFORE_DELTA))
			topLevelObjects.add(SEARCH);

		if (hookName.startsWith("override_")) {
			if (hookName.equals(OR_GETNEXT) || hookName.equals(OR_LOOKUP)) {
				topLevelObjects.add(ENTRY);
			}
			if (hookName.equals(OR_MODIFY)) {
				topLevelObjects.add(CURRENT);
			}
			return;
		}

		if (hookName.endsWith("nextclient"))
			topLevelObjects.remove(WORK);

		if (onlyWorkHooks.indexOf(hookName) >= 0)
			return;

		topLevelObjects.add(CONN);

		if (currentHooks.indexOf(hookName) >= 0)
			topLevelObjects.add(CURRENT);
		else if (hookName.endsWith("_ok") || hookName.endsWith("_fail")) {
			if (cc == null)
				return;
			if (ConnectorConfig.UPDATE_MODE.equals(cc.getMode()) || ConnectorConfig.DELTA_MODE.equals(cc.getMode())
					|| ConnectorConfig.DELETE_MODE.equals(cc.getMode()))
				topLevelObjects.add(CURRENT);
		}

		if (hookName.equals(SIMULATION)) {
			topLevelObjects.add(METHOD);
			topLevelObjects.add(SIMULATIONRESULT);
		}
	}

	/**
	 * Add script object names to topLevelObjects that are present for attribute maps.
	 * 
	 * @param ami
	 */
	private void addTopLevelObjects(AttributeMapItem ami) {
		topLevelObjects.add(CONN);

		topLevelObjects.add(RET);
		classMap.put(RET, AttributeMapping.class);

		if (Utils.hasLinkRequirements(cc))
			topLevelObjects.add(SEARCH);
		else
			return;

		if (ConnectorConfig.LOOKUP_MODE.equals(cc.getMode()))
			return;

		if (ConnectorConfig.DELETE_MODE.equals(cc.getMode()) || ami.getModify())
			topLevelObjects.add(CURRENT);
	}

	/**
	 * Add script object names to topLevelObjects that are present for script connectors.
	 * 
	 */
	private void addTopLevelForScriptConnector() {
		topLevelObjects.add(CONNECTOR);
		topLevelObjects.add(CONFIG);
		topLevelObjects.add(ENTRY);
		topLevelObjects.add(SEARCH);
		topLevelObjects.add(OLD);
		topLevelObjects.add(LIST);
	}

	/**
	 * Add script object names to topLevelObjects that are present for scripted parsers.
	 * 
	 */
	private void addTopLevelForScriptParser() {
		topLevelObjects.add(CONNECTOR);
		topLevelObjects.add(PARSER);
		topLevelObjects.add(CONFIG);
		topLevelObjects.add(OUT);
		topLevelObjects.add(INP);
		topLevelObjects.add(SOURCE);
		topLevelObjects.add(ENTRY);
		topLevelObjects.add(LIST);
	}

	/**
	 * Add script object names to topLevelObjects that are present for scripted functions.
	 * 
	 */
	private void addTopLevelForScriptFunction() {
		topLevelObjects.add(CONFIG);
	}

	/**
	 * Initialize the class map with classes for known/predefined script objects.
	 * 
	 */
	private void initClassMap() {
		classMap = new ConcurrentHashMap<String, Class<?>>();
		classMap.put(SYSTEM, UserFunctions.class);
		classMap.put(SESSION, Session.class);
		classMap.put(MAIN, RS.class);
		classMap.put(TASK, AssemblyLine.class);
		classMap.put(WORK, Entry.class);
		classMap.put(CONN, Entry.class);
		classMap.put(CURRENT, Entry.class);
		classMap.put(ERROR, Entry.class);
		classMap.put(ENTRY, Entry.class);
		classMap.put(RESULT, ScriptExitCode.class);
		classMap.put(THISCONNECTOR, AssemblyLineComponent.class);
		classMap.put(THISCOMPONENT, AssemblyLineComponent.class);
		classMap.put(SEARCH, SearchCriteria.class);
		classMap.put(CONNECTOR, Connector.class);
		classMap.put(CONFIG, BaseConfiguration.class);
		classMap.put(OLD, Entry.class);
		classMap.put(LIST, Vector.class);
		classMap.put(OUT, BufferedWriter.class);
		classMap.put(INP, BufferedReader.class);
		classMap.put(PARSER, ParserImpl.class); // Really a ScriptParser
		classMap.put(SOURCE, Object.class); // For querySchema
		classMap.put(METHOD, String.class); // For Simulation
		classMap.put(SIMULATIONRESULT, Entry.class);
	}

	public void install(IContextInformation info, ITextViewer viewer, int offset) {
		currentViewer = viewer;
	}

	public boolean isContextInformationValid(int offset) {
		int index = offset - 1;
		char ch = '.';
		try {
			while (index > 0 && ch != '(') {
				ch = currentViewer.getDocument().getChar(index--);
				if (ch == ')' || ch == '.')
					return false;
			}
			if (ch != '(')
				return false;

			index = offset;
			while (index < currentViewer.getDocument().getLength()) {
				ch = currentViewer.getDocument().getChar(index++);
				if (ch == ')')
					return true;
				else if (ch == '(' || ch == '.')
					return false;
			}
		} catch (BadLocationException ble) {
			SystemFunctions.doNothing();
		}

		// we got to the end of doc without breaking the context
		return true;
	}

	private void openTrace() {
		String trace = System.getProperty("com.ibm.tdi.eclipse.jsdebug");
		if (trace != null && trace.length() > 0) {
			try {
				traceFile = new FileWriter(trace);
			} catch (IOException e) {
				EclipseAppender.logerror(trace + ":" + e.toString(), e);
				traceFile = null;
			}
		}
	}

	private void writeTrace(String str) {
		if (traceFile != null) {
			try {
				traceFile.write(str);
				traceFile.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void closeTrace() {
		if (traceFile != null) {
			try {
				traceFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			traceFile = null;
		}
	}

	/**
	 * Adds a new top level object for code completion
	 * 
	 * @param name
	 *            The name of the object
	 * @param cls
	 *            The object class
	 */
	public void addTopLevelObject(String name, Class<?> cls) {
		classMap.put(name, cls);
		topLevelObjects.add(name);
	}

	/**
	 * Removes a top level object from the code completion list
	 * 
	 * @param name
	 */
	public void removeTopLevelObject(String name) {
		classMap.remove(name);
		topLevelObjects.remove(name);
	}

	/**
	 * Parses the javascript code and saves the function and assignments
	 * ASTNodes. These are used to provide completion on javascript objects
	 * defined in the current script.
	 * 
	 * @param script
	 * @throws ParseException 
	 */
	public void parseJavascriptSource(String script, ParserResult ps) throws ParseException {
		ParserResult result = ScriptEngineOptions.get().parseScript("a=1", true);
		FBScript2 parser = new FBScript2(new StringReader(script));
		parser.initContext(ScriptEngineOptions.get());
		parser.parsedScript = result;
		mainNode = (ASTProgram) parser.program();
		final ArrayList<ASTNode> nodes = new ArrayList<ASTNode>();
		DefaultNodeVisitor visitor = new DefaultNodeVisitor() {
			@Override
			public Object visitFunction(ASTFunction x, Object param) {
				nodes.add(x);
				return super.visitFunction(x, param);
			}

			@Override
			public Object visitAssign(ASTAssign x, Object param) {
				nodes.add(x);
				return super.visitAssign(x, param);
			}

			@Override
			public Object visitVariableDecl(ASTVariableDecl x, Object param) {
				nodes.add(x);
				return super.visitVariableDecl(x, param);
			}
			
		};
		mainNode.visitAllNodes(visitor, nodes);
		this.javascriptNodes = nodes;
	}
	
	/**
	 * Returns the ASTProgram for the script being edited.
	 * 
	 * @return
	 */
	public ASTProgram getJavaScriptMainNode() {
		return mainNode;
	}
}
