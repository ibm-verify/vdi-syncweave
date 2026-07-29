/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NameNotFoundException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;
import org.w3c.dom.Node;

import com.ibm.di.config.base.AttributeMapItemImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.eclipse.MetamergeConfigCE;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.LogConfigItem;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.PoolDefConfig;
import com.ibm.di.config.interfaces.PoolInstanceConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.RawFunctionConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.connector.Connector;
import com.ibm.di.entry.Attribute;
import com.ibm.di.function.SystemFunctions;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.StringTokenizer;
import com.ibm.tdi.eclipse.dialogs.InputTextAreaDialog;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.preferences.PreferenceConstants;
import com.ibm.tdi.eclipse.widget.FormWidget2;

public class Utils {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The marker ID we use for reporting problems to the problems view
	 */
	public static final String TDI_PROBLEM_MARKER = "com.ibm.tdi.rcp.tdiproblem";

	/**
	 * The marker ID we use for reporting validation problems to the problems
	 * view
	 */
	public static final String TDI_VALIDATE_PROBLEM_MARKER = "com.ibm.tdi.rcp.tdivalidationproblem";

	/**
	 * The project name hosting TDI server documents.
	 */
	public static final String TDI_SERVERS_PROJECT_NAME = "TDI Servers";

	/**
	 * Project specific install dir
	 */
	public static final QualifiedName TDI_INSTALL_DIR = new QualifiedName("http://www.ibm.com", Activator.TDI_PLUGIN_ID
			+ ".installdirectory");

	/**
	 * The property name to tag a project as having TDI server definitions.
	 */
	public static final QualifiedName TDI_SERVERS_PROJECT = new QualifiedName("http://www.ibm.com", Activator.TDI_PLUGIN_ID
			+ ".servers.project");

	// These are used for pattern matching
	private static final Pattern CONN_ATTR = Pattern.compile("\\bconn\\.([a-zA-Z_0-9\\.]+)\\b");
	private static final Pattern WORK_ATTR = Pattern.compile("\\bwork\\.([a-zA-Z_0-9\\.]+)\\b");
	private static final Pattern CONN_GETATTRIBUTE = Pattern.compile("\\bconn\\.getAttribute\\(\"([^\"]+)\"\\)");
	private static final Pattern WORK_GETATTRIBUTE = Pattern.compile("\\bwork\\.getAttribute\\(\"([^\"]+)\"\\)");
	private static final String CONN = "conn.";
	private static final String WORK = "work.";
	
	public static boolean isEntryFeedConnector(ConnectorConfig config) {
		return isEntryFeedMode(config.getMode());
	}

	public static boolean isEntryFeedMode(String mode) {
		return (ConnectorConfig.ITERATOR_MODE.equals(mode) || ConnectorConfig.SERVER_MODE.equals(mode));
	}

	public static boolean isInputConnector(BaseConfiguration config) {
		if (!(config instanceof ConnectorConfig))
			return false;
		String mode = ConfigUtils.getStdMode((ConnectorConfig) config);

		return (ConnectorConfig.ITERATOR_MODE.equals(mode) || ConnectorConfig.LOOKUP_MODE.equals(mode)
				|| ConnectorConfig.DELETE_MODE.equals(mode) || ConnectorConfig.CALL_REPLY_MODE.equals(mode)
				|| ConnectorConfig.MAPPING_MODE.equals(mode) || ConnectorConfig.SERVER_MODE.equals(mode) || ConnectorConfig.FUNCTION_MODE
				.equals(mode));
	}

	public static boolean isOutputConnector(BaseConfiguration config) {
		if (!(config instanceof ConnectorConfig))
			return false;
		String mode = ConfigUtils.getStdMode((ConnectorConfig) config);

		return (ConnectorConfig.ADDONLY_MODE.equals(mode) || ConnectorConfig.UPDATE_MODE.equals(mode)
				|| ConnectorConfig.DELTA_MODE.equals(mode) || ConnectorConfig.CALL_REPLY_MODE.equals(mode)
				|| ConnectorConfig.REPLY_MODE.equals(mode) || ConnectorConfig.SERVER_MODE.equals(mode) || ConnectorConfig.FUNCTION_MODE
				.equals(mode));
	}

	/**
	 * Returns true if the config is inside an assemblyline using a connector
	 * from the pool.
	 * 
	 * @param config
	 * @return true if config is using a pooled connector
	 */
	public static boolean isPooledConnector(BaseConfiguration config) {
		if (!(config instanceof ConnectorConfig))
			return false;

		ConnectorConfig cc = (ConnectorConfig) config;

		if (config instanceof FunctionConfig || ConnectorConfig.SERVER_MODE.equals(cc.getMode()))
			return false;

		if (Utils.getParentConfig(cc, AssemblyLineConfig.class) != null) {
			PoolDefConfig defConfig = cc.getPoolDefConfig();
			if (defConfig == null || !defConfig.getPoolEnabled())
				return false;

			PoolInstanceConfig pc = cc.getPoolInstanceConfig();
			if (pc == null || !pc.getPoolEnabled())
				return false;
			else
				return true;
		}

		return false;
	}

	/**
	 * Returns true if the config is inside an assemblyline and it's inheritance
	 * points to a pooled connector in the library.
	 * 
	 * @param config
	 * @return true if config can use a pooled instance
	 */
	public static boolean canPoolConnector(BaseConfiguration config) {
		if (!(config instanceof ConnectorConfig))
			return false;

		ConnectorConfig cc = (ConnectorConfig) config;

		if (config instanceof FunctionConfig || ConnectorConfig.SERVER_MODE.equals(cc.getMode()))
			return false;

		if (Utils.getParentConfig(cc, AssemblyLineConfig.class) != null && cc.getInheritsFrom() instanceof ConnectorConfig) {
			cc = (ConnectorConfig) cc.getInheritsFrom();
			PoolDefConfig defConfig = cc.getPoolDefConfig();
			if (defConfig != null)
				return defConfig.getPoolEnabled();
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getParentConfig(Object config, Class<T> cls) {
		Object b = config;
		while (b != null) {
			if (cls.isAssignableFrom(b.getClass()))
				return (T) b;

			for (Class<?> src : b.getClass().getInterfaces()) {
				if (cls.isAssignableFrom(src))
					return (T) b;
			}

			if (b instanceof BaseConfiguration)
				b = ((BaseConfiguration) b).getParent();
			else if (b instanceof Control)
				b = ((Control) b).getParent();
			else
				return null;
		}
		return null;
	}

	public static boolean hasParserRequirements(BaseConfiguration config) {
		ConnectorConfig cc;

		if (config instanceof ALMappingConfig)
			return false;

		if (config instanceof FunctionConfig)
			return "com.ibm.di.fc.ParserFC".equals(((FunctionConfig) config).getJavaClass());

		if (config instanceof ConnectorConfig)
			cc = (ConnectorConfig) config;
		else
			return false;

		if (ConnectorConfig.SCRIPT_MODE.equals(cc.getMode()))
			return false;

		String ref = cc.getInheritsFromRef();
		if (ref != null && ref.startsWith("@"))
			return false;

		if (cc.getConnectionConfig().getParserOption() != RawConnectorConfig.PARSER_USELESS)
			return true;
		else
			return false;
	}

	public static boolean hasLinkRequirements(BaseConfiguration config) {
		if (config instanceof ALMappingConfig || config instanceof FunctionConfig)
			return false;

		if (config instanceof ConnectorConfig) {
			ConnectorConfig cc = (ConnectorConfig) config;
			if (ConnectorConfig.LOOKUP_MODE.equals(cc.getMode()) || ConnectorConfig.UPDATE_MODE.equals(cc.getMode())
					|| ConnectorConfig.DELTA_MODE.equals(cc.getMode()) || ConnectorConfig.DELETE_MODE.equals(cc.getMode()))
				return true;
		}
		return false;
	}

	public static boolean hasConnectorRequirements(ConnectorConfig cc) {
		if (cc instanceof ALMappingConfig)
			return false;
		return !ConnectorConfig.SCRIPT_MODE.equals(cc.getMode());
	}

	public static boolean isInputMap(AttributeMapConfig amc) {
		if (amc == null || amc.getShortName() == null)
			return true;
		return amc.getShortName().indexOf("Input") != -1;
	}

	public static void setClipboardData(String str) {
		Clipboard c = new Clipboard(PlatformUI.getWorkbench().getDisplay());
		c.setContents(new Object[] { str }, new Transfer[] { TextTransfer.getInstance() });
	}

	public static boolean isLDAPConnector(ConnectorConfig connectorConfig) {
		return "com.ibm.di.connector.LDAPConnector".equals(connectorConfig.getConnectionConfig().getJavaClass());
	}

	public static boolean isJDBCConnector(ConnectorConfig connectorConfig) {
		return "com.ibm.di.connector.JDBCConnector".equals(connectorConfig.getConnectionConfig().getJavaClass());
	}

	/**
	 * Returns the supported modes for a connector. If invocation of getModes
	 * fails all modes are returned.
	 * 
	 * @param config
	 *            Connector config
	 * @return the supported modes for the connector
	 */
	public static ArrayList<String> getSupportedModes(ConnectorConfig config) {
		ArrayList<String> mode = new ArrayList<String>();
		if (config instanceof FunctionConfig)
			return mode;

		String modes = config.getSupportedModes();
		if (modes != null) {
			for (String s: modes.split(","))
				mode.add(s.trim());
			return mode;
		}

		String className = config.getConnectionConfig().getJavaClass();
		if (className != null) {
			try {
				Class<?> cls = null;
				try {
					cls = Class.forName(className);
				} catch (ClassNotFoundException err) {
					cls = Activator.loadClass(className);
				}
				if (cls != null) {
					Object conn = cls.newInstance();
					if (conn instanceof Connector) {
						mode.addAll(((Connector) conn).getModes(config));
					}
				}
			} catch (Throwable e) {
				// No need to log an error just because the connector cannot be
				// loaded
				// EclipseAppender.logerror(className, e);
				SystemFunctions.doNothing();
			}
		}

		if (mode.size() == 0) {
			for (String str : Connector.ALL_MODES)
				mode.add(str);
		}

		return mode;

	}

	/**
	 * Verify that the Connector has a legal mode, and return the mode. If the
	 * mode was not legal, return first legal mode.
	 * 
	 * @param cc
	 *            The ConnectorConfig
	 * @return The mode of the connector, corrected if necessary
	 */
	public static String verifyMode(ConnectorConfig cc) {
		if (cc instanceof ALMappingConfig)
			return null;
		String mode = cc.getMode();
		if (ConnectorConfig.SCRIPT_MODE.equals(mode))
			return mode; // Not really a Connector, should have been a
		// ScriptConfig.

		ArrayList<String> legal = getSupportedModes(cc);
		if (legal.size() > 0 && legal.indexOf(mode) == -1) {
			mode = legal.get(0);
			cc.setMode(mode);
		}
		return mode;
	}

	/**
	 * Loads a connector through the IDILoader
	 * 
	 */
	public static Connector loadConnector(ConnectorConfig config) throws Exception {
		Class<?> cls = Activator.loadClass(config.getConnectionConfig().getJavaClass());
		Connector c = (Connector) cls.newInstance();
		c.setConfiguration(config);
		return c;
	}

	/**
	 * Convenience method for setting a FormData object on a control. All
	 * attachments are relative to parent (e.g. (0,x) and (100,y) where x/y is
	 * the offset)
	 * 
	 * @param control
	 *            The control to set the FormData on
	 * @param top
	 *            Offset from parent's top or Integer.MIN_VALUE to ignore
	 * @param left
	 *            Offset from parent's left or Integer.MIN_VALUE to ignore
	 * @param bottom
	 *            Offset from parent's bottom or Integer.MIN_VALUE to ignore
	 * @param right
	 *            Offset from parent's bottom or Integer.MIN_VALUE to ignore
	 */
	public static void setFormData(Control control, int top, int left, int bottom, int right) {
		FormData fd = new FormData();
		if (top != Integer.MIN_VALUE)
			fd.top = new FormAttachment(0, top);
		if (left != Integer.MIN_VALUE)
			fd.left = new FormAttachment(0, left);
		if (bottom != Integer.MIN_VALUE)
			fd.bottom = new FormAttachment(0, bottom);
		if (right != Integer.MIN_VALUE)
			fd.right = new FormAttachment(0, right);

		control.setLayoutData(fd);
	}

	/**
	 * This method returns the IExtension for a TDI point name
	 * 
	 * @param extensionPointName
	 *            The relative point name (prefixed by TDI_PLUGIN_NAME)
	 * @return null if not found or IExtension object if found
	 */
	public static IExtension getExtensionPointFor(String extensionPointName) {
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensions(Activator.TDI_PLUGIN_ID);
		if (extensions == null)
			return null;

		for (IExtension e : extensions) {
			if (e.getExtensionPointUniqueIdentifier().equals(Activator.TDI_PLUGIN_ID + "." + extensionPointName))
				return e;
		}

		return null;
	}

	public static IProject getProjectFor(BaseConfiguration config) {
		if (config == null)
			return null;
		if (config instanceof TDIConfigurationFile)
			return ((TDIConfigurationFile) config).getProject();
		MetamergeConfig mc = config.getMetamergeConfig();
		if (mc instanceof TDIConfigurationFile)
			return ((TDIConfigurationFile) mc).getProject();
		if (mc instanceof MetamergeConfigCE)
			return ((MetamergeConfigCE) mc).getProject();
		return null;
	}

	public static void openEditorFor(BaseConfiguration bc) {
		if (bc == null)
			return;
		while (true) {
			BaseConfiguration p = bc.getParent();
			if (p == null || p instanceof MetamergeFolder || p instanceof MetamergeConfig)
				break;
			bc = p;
		}
		String sName = bc.getShortName();
		String folder = TDINature.getDefaultFolder(bc);
		String ext = TDIConfigurationFile.getExtensionFor(bc);
		IProject project = getProjectFor(bc);
		if (project == null || sName == null || ext == null || folder == null)
			return;
		IFile f = project.getFile(new Path(TDINature.RESOURCES_FOLDER + "/" + folder + "/" + sName + "." + ext));
		if (!f.exists())
			return;
		FileEditorInput fei = new FileEditorInput(f);

		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(fei, Activator.getEditorFor(bc));
		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}
	}

	public static String getInfTitle(BaseConfiguration element) {
		if (element == null)
			return "";

		String javaclass = (element instanceof ConnectorConfig ? ((ConnectorConfig) element).getConnectionConfig().getJavaClass()
				: ((ParserConfig) element).getJavaClass());

		if (javaclass == null)
			return element.getShortName();

		try {
			FormConfig inf = (FormConfig) MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE).lookup(
					"Forms/" + javaclass);
			if (inf != null && inf.getTitle() != null)
				return inf.getTitle();
			else
				return element.getShortName();
		} catch (Exception e) {
			return element.getShortName();
		}

	}

	public static IProject getTDIServersProject(boolean create) throws Exception {
		IProject serversProject = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject p : root.getProjects()) {
			if (!p.isOpen())
				continue;
			String pp = p.getPersistentProperty(TDI_SERVERS_PROJECT);
			if (pp != null && pp.equals("true")) {
				serversProject = p;
				break;
			}
		}

		if (serversProject == null) {
			serversProject = root.getProject(TDI_SERVERS_PROJECT_NAME);
			if (!serversProject.exists())
				serversProject.create(null);
			if (!serversProject.isOpen())
				serversProject.open(null);
			serversProject.setPersistentProperty(TDI_SERVERS_PROJECT, "true");
		}

		if (!serversProject.isOpen())
			serversProject.open(null);

		return serversProject;
	}

	public static String loadTextFile(File sp) throws FileNotFoundException, IOException {
		char[] buf = new char[(int) sp.length()];
		FileReader r = new FileReader(sp);
		try {
			r.read(buf);
		} finally {
			r.close();
		}
		return new String(buf);
	}

	public static String loadBundleResource(String path) throws Exception {
		Bundle bundle = Platform.getBundle(Activator.TDI_PLUGIN_ID);
		URL url = bundle.getEntry(path);
		StringBuilder buf = new StringBuilder();
		BufferedReader inp = new BufferedReader(new InputStreamReader(url.openStream()));
		String str;
		while ((str = inp.readLine()) != null) {
			buf.append(str);
			buf.append("\n");
		}
		inp.close();
		return buf.toString();
	}

	public static String inputTextArea(Shell shell, String title, String prompt, String defval) {
		InputTextAreaDialog dlg = new InputTextAreaDialog(shell, title, prompt, defval);
		if (dlg.open() == Window.OK)
			return dlg.getValue();
		else
			return null;
	}

	public static Control exceptionWidget(Composite parent, Exception error) {
		Text t = new Text(parent, SWT.MULTI);
		StringBuilder sb = new StringBuilder();
		sb.append(Messages.getMessage("Connector.ModeCB.label", error.toString())); //$NON-NLS-1$
		StringWriter sw = new StringWriter();
		error.printStackTrace(new PrintWriter(sw));
		sb.append(sw.toString());
		return t;
	}

	public static String exceptionText(Throwable t) {
		StringBuilder sb = new StringBuilder();
		sb.append(t.getMessage());
		sb.append("\n"); //$NON-NLS-1$
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		sb.append(sw.toString());
		return sb.toString();
	}

	public static IFile createServerEntry(String name, ScriptConfig config) throws Exception {
		IProject serversProject = Utils.getTDIServersProject(true);

		String str = config.getStringParameter("address");
		if (name != null)
			str = name;

		str = str.replace(':', ' ');
		if (!str.endsWith(".tdiserver"))
			str += ".tdiserver";

		IFile file = serversProject.getFile(str);
		TDIConfigurationFile tdi = new TDIConfigurationFile(file);
		tdi.setDefaultConfigObject(str, config);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		tdi.commitChanges(bos);
		return file;
	}

	public static MetamergeConfig loadRuntimeRS(IProject project) throws Exception {
		IFile path = getRuntimeRS(project);
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(MetamergeConfigFactory.MC_URL, new File(path.getLocation().toOSString()));
		return new MetamergeConfigXML(env);
	}

	/**
	 * Returns the MetamergeConfigCE object for the specific project
	 * 
	 * @param project
	 * @return the MetamergeConfigCE object
	 * @throws Exception
	 */
	public static MetamergeConfig getProjectMC(IProject project) throws Exception {
		IFile rs = getRuntimeRS(project);
		MetamergeConfig mc = null;
		if (rs != null) {
			String ns = rs.getRawLocation().toOSString();
			mc = MetamergeConfigFactory.getNamespace(ns);
			if (mc == null) {
				mc = new MetamergeConfigCE(project);
			}
			// getRuntimeRS may create the file (in case someone removed it)
			MetamergeConfigFactory.registerNamespace(ns, mc);
		}
		return mc;
	}

	/**
	 * Returns the solution properties file for the project of this resouce.
	 * 
	 * @param resource
	 * @return the solution properties IFile
	 */
	public static IFile getSolutionProps(IResource resource) {
		return resource.getProject().getFile(TDINature.SOLUTION_SETTINGS_FILE);
	}

	/**
	 * Returns the project's rs.xml runtime configuration file
	 * 
	 * @param project
	 * @return the project's rs.xml IFile.
	 * @throws CoreException
	 */
	public static IFile getRuntimeRS(IProject project) throws CoreException {
		IFile file = project.getFile(".rs.xml");
		if (!file.exists()) {
			// Rename existing - no reason to use the project name
			file = project.getFile("." + project.getName() + ".xml");
			if (file.exists())
				file.move(new Path(".rs.xml"), true, null);

			file = project.getFile(".rs.xml");
			if (!file.exists())
				file.create(new ByteArrayInputStream(new byte[] {}), IResource.DERIVED, null);
		}

		file.refreshLocal(IResource.DEPTH_INFINITE, null);
		return file;
	}

	public static String getTDIInstallDirectory(IProject project) throws CoreException {
		// first try project specific setting
		String dir = project.getPersistentProperty(TDI_INSTALL_DIR);
		if (dir != null && dir.length() > 0)
			return dir;
		else
			return Activator.getInstallPath();
	}

	public static IResource getTDIServer(String string) throws Exception {
		String name = string;
		if (name == null)
			name = TDINature.DEFAULT_SERVER_NAME;

		if (!name.endsWith(".tdiserver"))
			name += ".tdiserver";
		return getTDIServersProject(true).getFile(name);
	}

	public static ArrayList<String> getScriptReferences(boolean input, String script) {
		ArrayList<String> list = new ArrayList<String>();
		if (script == null || script.length() == 0)
			return list;

		int index = script.indexOf(input? CONN : WORK);
		if (index < 0)
			return list;
		script = script.substring(index);

		Pattern p = input ? CONN_ATTR : WORK_ATTR;
		Matcher m = p.matcher(script);
		while (m.find()) {
			for (int i = 1; i <= m.groupCount(); i++) {
				if (!m.group(i).equals("getAttribute"))
					list.add(m.group(i));
			}
		}
		p = input ? CONN_GETATTRIBUTE : WORK_GETATTRIBUTE;
		m = p.matcher(script);
		while (m.find()) {
			for (int i = 1; i <= m.groupCount(); i++) {
				list.add(m.group(i));
			}
		}

		return list;
	}

	/**
	 * Log the problem marker in the problem view.
	 * 
	 * @param severity
	 *            the level of the problem.
	 * @param problem
	 *            the problem.
	 * @param config
	 *            The configuration that has the problem.
	 * @param message
	 *            that will be displayed.
	 * @return the problem marker.
	 */
	public static IMarker logProblem(int severity, String problem, BaseConfiguration config, String message) {
		return logProblem(severity, problem, config, message, TDI_PROBLEM_MARKER);
	}

	/**
	 * Log the problem marker in the problem view.
	 * 
	 * @param severity
	 *            the level of the problem.
	 * @param problem
	 *            the problem.
	 * @param config
	 *            The configuration that has the problem.
	 * @param message
	 *            that will be displayed.
	 * @param problemMarkerType
	 *            problem marker type.
	 * @return the problem marker.
	 */
	public static IMarker logProblem(int severity, String problem, BaseConfiguration config, String message,
			String problemMarkerType) {
		IMarker marker = null;
		try {
			marker = ((TDIConfigurationFile) config.getMetamergeConfig()).getFile().createMarker(problemMarkerType);
			marker.setAttribute(IMarker.LOCATION, config.getPath());
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			marker.setAttribute(IMarker.PROBLEM, problem);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return marker;
	}

	public static ArrayList<BaseConfiguration> pathToObjects(BaseConfiguration top, String loc) {
		ArrayList<BaseConfiguration> arr = new ArrayList<BaseConfiguration>();
		try {
			BaseConfiguration child = top.getChildForPath(loc);
			while (child != null) {
				arr.add(0, child);
				child = child.getParent();
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, Display.getCurrent().getActiveShell());
		}
		return arr;
	}

	public static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	/**
	 * Creates a new Composite with a zero-bordered GridLayout.
	 * 
	 * @param parent
	 * @param flags
	 * @param cols
	 * @param equalWidth
	 * @return the new Composite.
	 */
	public static Composite newComposite(Composite parent, int flags, int cols, boolean equalWidth) {
		Composite c = new Composite(parent, flags);
		GridLayout layout = new GridLayout(cols, equalWidth);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		c.setLayout(layout);
		return c;
	}

	/**
	 * Sets a zero-bordered GridLayout on the composite
	 * 
	 * @param parent
	 * @param cols
	 * @param equalWidth
	 * @return the GridLayout
	 */
	public static GridLayout setGridLayout(Composite parent, int cols, boolean equalWidth) {
		GridLayout layout = new GridLayout(cols, equalWidth);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);
		return layout;
	}

	/**
	 * Returns the preferred server for the configfile's project
	 * 
	 * @param configFile
	 * @return the preferred server.
	 * @throws CoreException
	 */
	public static String getTDIServer(IFile configFile) throws CoreException {
		IProject p = configFile.getProject();
		return getTDIServer(p);
	}

	/**
	 * Returns the preferred server for the provided project
	 * 
	 * @param project
	 * @return the preferred server.
	 * @throws CoreException
	 */
	public static String getTDIServer(IProject project) throws CoreException {
		String s = project.getPersistentProperty(TDI.PROJECT_PREF_SERVER_QNAME);
		if (s == null) {
			s = TDINature.DEFAULT_SERVER_PROPERTY;
			project.setPersistentProperty(TDI.PROJECT_PREF_SERVER_QNAME, s);
		}
		return s;
	}

	/**
	 * Returns a system form from either system or internal
	 * 
	 */
	public static FormConfig getSystemForm(String name) throws Exception {
		String formName = "/Forms/" + name;
		try {
			return (FormConfig) MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE).lookup(formName);
		} catch (NameNotFoundException nfn) {
			return (FormConfig) MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.STDFORMS_NAMESPACE).lookup(formName);
		}
	}

	/**
	 * Find the name of the Form that should be used for this BaseConfiguration.
	 * 
	 * @param bc
	 *            The BaseConfiguration
	 * @return The name of the Form, or null if nothing found.
	 * @since 7.1
	 */
	public static String getFormName(BaseConfiguration bc) {
		if (bc.getStringParameter(FormWidget2.EMBEDDED_FORM_NAME) != null)
			return FormWidget2.EMBEDDED_FORM_NAME;
		if (bc instanceof LogConfigItem)
			return getFormName((LogConfigItem) bc);
		if (bc instanceof ParserConfig)
			return ((ParserConfig) bc).getJavaClass();

		if (bc instanceof RawFunctionConfig)
			bc = getParentConfig(bc, FunctionConfig.class);
		if (bc instanceof FunctionConfig)
			return ((FunctionConfig) bc).getJavaClass();

		if (!(bc instanceof RawConnectorConfig))
			return null;

		String javaClass = ((RawConnectorConfig) bc).getJavaClass();

		MetamergeConfig system = MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE);
		while (bc != null) {
			BaseConfiguration current = bc;
			String name;
			if (current.getShortName() != null) {
				name = javaClass + "." + current.getShortName();
				try {
					system.lookup("/Forms/" + name);
					return name;
				} catch (Exception notFound) {
					SystemFunctions.doNothing();
				}
			}
			current = bc.getParent(); // Try parent
			if (current != null && current.getShortName() != null) {
				name = current.getShortName();
				if (name.startsWith("ibmdi.")) {
					try {
						system.lookup("/Forms/" + name);
						return name;
					} catch (Exception notFound) {
						SystemFunctions.doNothing();
					}
				}
				name = javaClass + "." + name;
				try {
					system.lookup("/Forms/" + name);
					return name;
				} catch (Exception notFound) {
					SystemFunctions.doNothing();
				}
			}
			bc = bc.getInheritsFrom();
		}

		try {
			system.lookup("/Forms/" + javaClass);
			return javaClass;
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}

		return null;
	}

	/**
	 * Find the name of the Form that should be used for this
	 * AttributeMapConfig, which must be used in a Connector or Function.
	 * 
	 * @param bc
	 *            The AttributeMapConfig
	 * @return The name of the Form, or null if nothing found
	 * @since 7.1
	 */
	public static String getFormName(ConnectorConfig cc, boolean input) {
		String suffix = input ? ".InputAttributeMap" : ".OutputAttributeMap";
		String javaClass;
		if (cc instanceof FunctionConfig) {
			javaClass = ((FunctionConfig) cc).getJavaClass();
		} else {
			javaClass = cc.getConnectionConfig().getJavaClass();
		}

		MetamergeConfig system = MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE);
		BaseConfiguration bc = cc;
		String name;
		while (bc != null) {
			try {
				name = javaClass + "." + bc.getShortName() + suffix;
				system.lookup("/Forms/" + name);
				return name;
			} catch (Exception notFound) {
				bc = bc.getInheritsFrom();
			}
		}

		try {
			name = javaClass + suffix;
			system.lookup("/Forms/" + name);
			return name;
		} catch (Exception notFound) {
			return null;
		}
	}

	/**
	 * Encode URI
	 * 
	 * From RFC2396
	 * 
	 * 2.3. Unreserved Characters
	 * 
	 * Data characters that are allowed in a URI but do not have a reserved
	 * purpose are called unreserved. These include upper and lower case
	 * letters, decimal digits, and a limited set of punctuation marks and
	 * symbols.
	 * 
	 * unreserved = alphanum | mark
	 * 
	 * mark = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
	 * 
	 * Unreserved characters can be escaped without changing the semantics of
	 * the URI, but this should not be done unless the URI is being used in a
	 * context that does not allow the unescaped character to appear.
	 */
	public static String encodeURI(String uri) {
		String mark = "-_.!~*'()";
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < uri.length(); i++) {
			char ch = uri.charAt(i);
			if (Character.isLetter(ch) || Character.isDigit(ch) || mark.indexOf(ch) != -1)
				buf.append(ch);
			else if (ch == ' ')
				buf.append('+');
			else
				buf.append("%" + Integer.toHexString((int) ch));
		}
		return buf.toString();
	}

	private final static List<String> reservedWords = Arrays.asList(new String[] { "break", "case", "catch", "continue", "default",
			"delete", "do", "else", "false", "finally", "for", "function", "if", "in", "infinity", "instanceof", "new", "null",
			"undefined", "return", "switch", "this", "throw", "true", "try", "typeof", "var", "void", "while", "with" });

	/**
	 * Return true if the parameter could be a JavaScript identifier. For now,
	 * we say that it must start with a letter, and only contain letters,
	 * digits, period or underscore.
	 * 
	 * @param str
	 *            The String to check
	 * @return true if this could be an identifier
	 */
	public static boolean isIdentifier(String str) {
		if (str == null || str.length() == 0)
			return false;
		if (!Character.isLetter(str.charAt(0)) && str.charAt(0) != '_')
			return false;

		for (char c : str.toCharArray()) {
			if (!Character.isLetterOrDigit(c) && c != '_' && c != '.')
				return false;
		}
		if (reservedWords.indexOf(str) >= 0)
			return false;
		return true;
	}

	/**
	 * Returns a name to reference an object based on the object. This method
	 * returns either the toString() value or calls the overloaded methods for
	 * Attribute and SchemaItemConfig.
	 * 
	 * @see Utils#getScriptName(Attribute)
	 * @see #getScriptName(SchemaItemConfig)
	 * @return Name suitable for use in a script expression
	 */
	public static String getScriptName(Object obj) {
		if (obj instanceof Attribute)
			return getScriptName((Attribute) obj);
		else if (obj instanceof SchemaItemConfig)
			return getScriptName((SchemaItemConfig) obj);
		else
			return (obj == null ? null : obj.toString());
	}

	/**
	 * Returns a name to reference an object based on the attribute name. The
	 * name includes any parent attribute items in case the attribute is part of
	 * a hierarchical entry.
	 * 
	 * This method uses DOM methods (getParentNode).
	 * 
	 * @param conn
	 *            If not null this is prepended to the referenced attribute
	 * @param attr
	 *            Schema item config
	 * @return A string without escaped dots in the name
	 */
	public static String getScriptName(Attribute attr) {
		StringBuilder ret = new StringBuilder(attr.getName());
		Node n = attr.getParentNode();
		while (n instanceof Attribute) {
			ret.insert(0, '.');
			ret.insert(0, ((Attribute) n).getName());
			n = n.getParentNode();
		}
		return ret.toString();
	}

	/**
	 * Returns a name to reference an object based on the schema item name. The
	 * name includes any parent schema items in case the schemaitem is part of a
	 * hierarchy.
	 * 
	 * @param conn
	 *            If not null this is prepended to the referenced attribute
	 * @param sic
	 *            Schema item config
	 * @return A string without escaped dots in the name
	 */
	public static String getScriptName(SchemaItemConfig sic) {
		String name = sic.getShortName();
		if (sic.getName() != null && sic.getParent() != null)
			name = sic.getName().toString();

		// -- This is just to make things look prettier
		// -- If the schema isn't hierarchical then we don't have to escape dots
		if (name.indexOf("\\.") >= 0 && !isSchemaHierarchical(sic))
			name = name.replace("\\.", ".");

		return name;
	}

	/**
	 * Returns true if the SchemaConfig sic belongs to is hierarchical
	 * 
	 * @param sic
	 * @return
	 */
	private static boolean isSchemaHierarchical(SchemaItemConfig sic) {
		SchemaConfig top = null;
		BaseConfiguration b = sic;
		while (b != null && b.getParent() != null) {
			if (b.getParent() instanceof SchemaConfig)
				top = (SchemaConfig) b.getParent();
			b = b.getParent();
		}

		if (top != null) {
			for (Object obj : top.getItemNames()) {
				SchemaItemConfig child = top.getItem(obj);
				if (child.getChildSchemaList().size() > 0)
					return true;
			}
		}

		return false;
	}

	/**
	 * Return a script for a simple Attribute mapping. Basically we return
	 * "conn.id" if id is an identifier, otherwise conn.getAttribute("id");
	 */
	public static String getScript(String obj, String id) {
		String conn = (obj == null ? "" : obj);

		StringBuilder ret = new StringBuilder();
		if (id.indexOf("\n") < 0) {
			ret.append(conn);
			if (isIdentifier(id) && id.indexOf(".") == -1) {
				if (conn.length() > 0 && !conn.endsWith("."))
					ret.append(".");
				ret.append(id);
			} else {
				// -- Remove trailing dot (e.g. not dot between obj and ref as
				// in work["attr"])
				if (conn.endsWith("."))
					ret.deleteCharAt(ret.length() - 1);
				ret.append("[\"");
				for (int i = 0; i < id.length(); i++) {
					char c = id.charAt(i);
					if ( c == '\\')
						ret.append("\\\\");
					else
						ret.append(c);
				}
				ret.append("\"]");
			}
		} else {
			ret.append("[");
			for (String s : id.split("\n")) {
				if (ret.length() > 1)
					ret.append(", ");
				ret.append(getScript(conn, s));
			}
			ret.append("]");
		}
		return ret.toString();
	}

	/**
	 * Returns the folder name (relative to Resources folder) in which a
	 * configuration object belongs. AttributeMapConfig is treated as
	 * ALMappingConfig.
	 * 
	 * @param bc
	 * @return Folder name or null if configuration object cannot appear by
	 *         itself in a file
	 */
	public static String getFolderForConfig(BaseConfiguration bc) {
		String folder = TDINature.getDefaultFolder(bc);
		if (folder == null && bc instanceof AttributeMapConfig)
			folder = TDINature.ATTRIBUTE_MAPS_FOLDER;
		return folder;
	}

	/**
	 * This method creates a file from the configuration. The file name
	 * 
	 * @param target
	 * @param folder
	 * @param bc
	 * @throws CoreException
	 */
	public static void createFileFromConfig(Object target, String folder, BaseConfiguration bc, Shell shell) throws Exception {
		createFileFromConfig(target, folder, bc, shell, true);
	}

	public static void createFileFromConfig(Object target, String folder, BaseConfiguration bc, Shell shell, boolean prompt)
	throws Exception {

		// -- TDI Project
		IProject project = ((IResource) target).getProject();
		IFolder resources = project.getFolder(TDINature.RESOURCES_FOLDER);
		if (!resources.exists())
			resources.create(true, true, null);

		// -- Subfolder in Resources directory
		IFolder subdir = resources.getFolder(folder);
		if (!subdir.exists())
			subdir.create(true, true, null);

		// -- Special case for AttributeMapConfig (clone and put it in an
		// ALMappingConfig)
		if (bc instanceof AttributeMapConfig) {
			ALMappingConfig amc = (ALMappingConfig) bc.getMetamergeConfig().newInstanceOf(MetamergeConfig.ATTRIBUTEMAP_FOLDER);
			amc.init();
			amc.setName(bc.getParent().getShortName() + "_" + bc.getShortName());
			for (Object obj : ((AttributeMapConfig) bc).getAttributeNames()) {
				AttributeMapItem clone = AttributeMapItemImpl.clone(((AttributeMapConfig) bc).getAttributeMapItem(obj));
				clone.setName(obj.toString());
				amc.getAttributeMap().setAttributeMapItem(clone);
			}
			bc = amc;
		}

		// -- Prompt for file name to save to
		String name = bc.getShortName();
		if (prompt) {
			InputDialog id = new InputDialog(shell, Messages.getString("general.save.library.label"), Messages
					.getString("ConfigTable.Name"), bc.getShortName(), null);
			if (id.open() != Window.OK)
				return;
			name = id.getValue();
		}

		// -- Check if file exists and open confirmation dialog
		IFile file = subdir.getFile(name + "." + TDIConfigurationFile.getExtensionFor(bc)); //$NON-NLS-1$
		if (file.exists()
				&& !MessageDialog.openQuestion(shell, Messages.getString("general.save.library.label"), Messages.getMessage(
						"general.resource.exists", new Object[] { file.getFullPath().toOSString(),
								new Date(file.getLocalTimeStamp()).toString() }))) {
			return;
		}

		// -- Create/Overwrite the file
		TDIConfigurationFile cfg = new TDIConfigurationFile(file);
		BaseConfiguration clone = (BaseConfiguration) bc.getClone();
		cfg.setDefaultConfigObject(name, clone);
		cfg.commitVersion(true);
	}

	public static String getFormName(LogConfigItem lci) {
		String s = lci.getStringParameter("com.ibm.di.formName");
		if (s != null && s.length() > 0)
			return s;
		s = lci.getStringParameter("com.ibm.di.log.appender");
		if (s == null)
			return null;
		if (s.equals("IDIFileRoller"))
			s = "FileRoller";
		return "ibmdi." + s + "Appender";
	}

	/**
	 * Returns the string pointing to the "real" object from which this element
	 * is inherited. Special handling for hooks and attmap items (e.g. check for
	 * overridden script)
	 * 
	 * @param element
	 * @return the string pointing to the "real" object.
	 */
	public static String getInheritsFromExt(Object element) {
		if (!(element instanceof BaseConfiguration))
			return null;

		BaseConfiguration b = (BaseConfiguration) element;

		if (b instanceof HookConfig && b.isParameterLocal(InternalSchema.HC_SCRIPT))
			return null;

		if (b instanceof AttributeMapItem && b.isParameterLocal(InternalSchema.AMI_SCRIPT))
			return null;

		if (b instanceof ScriptConfig && b.isParameterLocal(InternalSchema.SCRIPT))
			return null;

		while (b != null) {
			String str = b.getInheritsFromRef();

			if (str != null) {
				if (BaseConfiguration.INHERIT_NONE.equals(str))
					return null;
				else if (str.startsWith("system:"))
					return null;
				else if (!BaseConfiguration.INHERIT_PARENT.equals(str))
					return str;
			}
			b = b.getParent();
		}
		return null;
	}

	public static boolean isAssemblyLine(BaseConfiguration bc) {
		if (bc instanceof FunctionConfig) {
			return "com.ibm.di.fc.AssemblyLineFC".equals(((FunctionConfig) bc).getJavaClass());
		}
		if (!(bc instanceof ConnectorConfig))
			return false;
		RawConnectorConfig rcc = ((ConnectorConfig) bc).getConnectionConfig();
		return rcc != null && "com.ibm.di.connector.AssemblyLineConnector".equals(rcc.getJavaClass());
	}

	public static void setName(Control control, String key) {
		final String msg = Messages.getString(key);
		if (control == null || msg == null)
			return;
		control.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent event) {
				event.result = msg;
			}
		});
	}

	/**
	 * Take a small nap, to help with timing issues
	 * 
	 * @param ms
	 *            number of milliseconds to sleep
	 */
	public static void nap(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ie) {
			// If we are interrupted, just go back to sleep.
			nap(ms);
		}
	}

	public static String openEditorFor(String str, boolean wait, boolean confirm) {
		File tmp = null;
		try {
			tmp = File.createTempFile("ibmditk", ".js");
			BufferedWriter f = new BufferedWriter(new FileWriter(tmp));
			StringTokenizer st = new StringTokenizer(str, "\n", true);
			while (st.hasMoreTokens()) {
				String nt = st.nextToken();
				if (nt.startsWith("\n")) {
					int newline = nt.length();
					for (int i = 0; i < newline; i++)
						f.newLine();
				} else
					f.write(nt);
			}
			f.close();

			if (!openEditorFor(tmp, wait))
				return null;

			if (wait) {
				if (confirm
						&& !MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages
								.getString("notepad.prompt.UpdateEditor"), Messages.getString("notepad.prompt.UpdateEditor")))
					return null;

				BufferedReader inp = new BufferedReader(new FileReader(tmp));
				StringBuffer buf = new StringBuffer();
				String s;
				while ((s = inp.readLine()) != null) {
					buf.append(s);
					buf.append("\n");
				}
				inp.close();
				if (buf.toString().equals(str))
					return null;
				else
					return buf.toString();
			}

		} catch (Exception error) {
			EclipseAppender.logerror(error.toString(), error);
		} finally {
			if (tmp != null && !tmp.delete())
				SystemFunctions.doNothing(); // It is ok if we cannot delete the
			// file
		}

		return null;
	}

	/**
	 * @param path
	 * @param wait
	 * @return
	 */
	public static boolean openEditorFor(File path, boolean wait) {
		String editor = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_EXTERNAL_EDITOR);
		if (editor == null || editor.trim().length() == 0) {
			if (System.getProperty("os.name").indexOf("Windows") == -1)
				editor = "/bin/vi";
			else
				editor = "notepad";
		}

		try {
			Process p = Runtime.getRuntime().exec(editor + " " + path.getAbsolutePath());
			if (wait)
				p.waitFor();
		} catch (Exception error) {
			EclipseAppender.logerror(error.toString(), error);
			return false;
		}
		return true;
	}

	private final static String[] internalModes = { ConnectorConfig.ITERATOR_MODE, ConnectorConfig.ADDONLY_MODE,
		ConnectorConfig.DELETE_MODE, ConnectorConfig.LOOKUP_MODE, ConnectorConfig.UPDATE_MODE, ConnectorConfig.CALL_REPLY_MODE,
		ConnectorConfig.SERVER_MODE, ConnectorConfig.DELTA_MODE, };

	private final static String[] externalModes = { Messages.getString("Localized.Iterator"),
		Messages.getString("Localized.AddOnly"), Messages.getString("Localized.Delete"),
		Messages.getString("Localized.Lookup"), Messages.getString("Localized.Update"),
		Messages.getString("Localized.CallReply"), Messages.getString("Localized.Server"),
		Messages.getString("Localized.Delta"), };

	public static String externalMode(String mode) {
		for (int i = 0; i < internalModes.length; i++)
			if (internalModes[i].equals(mode))
				return externalModes[i];
		return mode;
	}

	public static String internalMode(String mode) {
		for (int i = 0; i < externalModes.length; i++)
			if (externalModes[i].equals(mode))
				return internalModes[i];
		return mode;
	}

	/**
	 * This method removes the AL log file if it is a temporary one. The path is
	 * checked against an existing Project/Logs/<file> and if present it is not
	 * deleted.
	 * 
	 * @param logfile
	 */
	public static void removeALLogFile(File logfile) {
		try {
			String path = logfile.getAbsolutePath();
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
			if (file != null) {
				file.getParent().refreshLocal(IFile.DEPTH_INFINITE, null);
				file.refreshLocal(IFile.DEPTH_INFINITE, null);
				file.refreshLocal(IFile.DEPTH_INFINITE, null);
				if (!file.exists())
					return;
			}
			if (!logfile.delete())
				SystemFunctions.doNothing();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a File object. The file either is a temporary (delete on exit) or
	 * a file in the Project's Log folder. The method consults the
	 * P_SAVE_AL_LOGS_COUNT preference to determine wether a temporary file or a
	 * permanent file is created. This method also cleans up the Logs folder so
	 * that only the configured number of log files are saved.
	 * 
	 * If Project is null a temporary file is always returned.
	 * 
	 * @param project
	 *            Project or null if temporary is
	 * @param alname
	 * @return
	 * @throws Exception
	 */
	public static File getALLogFile(IProject project, String alname) throws Exception {
		int savecount = Activator.getPrefs().getInt(PreferenceConstants.P_SAVE_AL_LOGS_COUNT);

		// -- If savecount is less than 1 or we have no project we create a temp
		// file
		if (savecount < 1 || project == null) {
			File logFile = File.createTempFile("tdi_ce_al_log", ".log");
			logFile.deleteOnExit();
			return logFile;
		} else {
			// -- Make sure we have the Logs folder in the project and clean
			// up/generate log file
			IFolder folder = project.getFolder("Logs");
			ArrayList<IFile> logFiles = new ArrayList<IFile>();
			try {
				if (!folder.exists())
					folder.create(true, false, null);

				for (IResource res : folder.members()) {
					if (res instanceof IFile && res.getName().startsWith(alname + "-")) {
						logFiles.add((IFile) res);
					}
				}

				// -- Sort list on names ascending
				Collections.sort(logFiles, new Comparator<IFile>() {
					public int compare(IFile o1, IFile o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});

				// -- Remove oldest file(s)
				while (logFiles.size() > savecount) {
					logFiles.get(0).delete(true, null);
					logFiles.remove(0);
				}

				// -- Create a new file for the new AL
				String ts = new SimpleDateFormat("yyyyMMddHHmmSSS").format(Calendar.getInstance());
				IFile logFile = folder.getFile(alname + "-" + ts + ".log");
				logFile.create(new ByteArrayInputStream("".getBytes()), true, null);
				return new File(logFile.getRawLocation().toPortableString());
			} catch (CoreException e) {
				return getALLogFile(null, alname);
			}
		}
	}

	/**
	 * Convert a long representing a Date to a local String
	 * @param date The long to convert
	 * @return The local String representing the date.
	 */
	public static String dateToString(long date) {
		return DateFormat.getDateTimeInstance().format(new Date(date));
	}

	/**
	 * Returns all config files in a project.
	 * @param project
	 * @return
	 * @throws CoreException
	 */
	public static List<IFile> getAllConfigFiles(IProject project) throws CoreException {
		List<IFile> list = new ArrayList<IFile>();
	
		for (IResource res : project.members()) {
			getFiles(res, list, "");
		}
		return list;
	}

	private static void getFiles(IResource res, List<IFile> list, String folder) throws CoreException {
		if (res instanceof IFolder) {
			for (IResource r: ((IFolder) res).members())
				getFiles(r, list, res.getName());
		} else if (res instanceof IFile){
			String ext = res.getFileExtension();
			if (folder.equals(TDIConfigurationFile.getFolderForExtension(ext)))
				list.add((IFile) res);
		}
	}
}
