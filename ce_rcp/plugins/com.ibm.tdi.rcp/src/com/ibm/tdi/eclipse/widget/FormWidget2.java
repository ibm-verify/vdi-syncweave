/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NameNotFoundException;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.FormSection;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.fc.FunctionInterface;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.script.ScriptEngine;
import com.ibm.icu.util.StringTokenizer;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.dialogs.GenericFormDialog;
import com.ibm.tdi.eclipse.editors.form.FormPanelWidget;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.util.TextEditorContextMenu;
import com.ibm.tdi.eclipse.util.UndoRedoSupport;
import com.ibm.tdi.eclipse.wizards.NullValueBehaviorWizard;

/**
 * This class is used to create forms for TDI forms (tdi.xml, *.inf).
 *
 */
public class FormWidget2 extends Composite implements MetamergeConfigChangeListener {
	public static final String EMBEDDED_FORM_NAME = "$form$";

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	//
	// -- These strings are used to tag field names with source information
	// -- Tooltip should reveal the property/script used
	//
	private static final String JAVASCRIPT_PREFIX = "{javascript ";
	private static final String PROPERTY_PREFIX = "{property.";
	private static final String propertyTag = Messages.getString("FormWidget.tag.property");
	private static final String substitutionTag = Messages.getString("FormWidget.tag.substitution");
	private static final String javaScriptTag = Messages.getString("FormWidget.tag.javascript");

	// -- number of columns in the grid
	private static final int GRID_COLUMNS = 5;

	//
	private static final String SCRIPT_KEY = "TDI_SCRIPT";
	private static final String ATTRIBUTE_KEY = "TDI_ATTRIBUTE";

	//
	private static final String DROPDOWN_SYNTAX = "dropedit";
	private static final String DROPLIST_SYNTAX = "droplist";
	private static final String BOOLEAN_SYNTAX = "boolean";
	private static final String TEXTAREA_SYNTAX = "textarea";
	private static final String STATIC_SYNTAX = "static";
	private static final String PASSWORD_SYNTAX = "password";
	private static final String SCRIPT_SYNTAX = "script";
	private static final String COMPONENT_SYNTAX = "component";
	private static final String EDITOR_SYNTAX = "editorwindow";
	private static final String PANEL_SYNTAX = "panel";

	private static final String LOCALIZED = "FormUI.Localized.";

	private String formName;
	private FormConfig formConfig;
	private FormConfig global;
	private FormToolkit tk;
	private Form form;
	private BaseConfiguration editingConfig;
	private ScrolledComposite scrolledComposite;
	private StackLayout stackLayout;
	private Composite content;
	private ScriptEngine ibmjs = new ScriptEngine(null);
	private HashMap<String, Control> controls = new HashMap<String, Control>();
	private HashMap<String, Label> labels = new HashMap<String, Label>();
	private HashMap<String, Button> optionButtons = new HashMap<String, Button>();
	private HashMap<String, Button> extraButton1 = new HashMap<String, Button>();
	private HashMap<String, Button> extraButton2 = new HashMap<String, Button>();

	private HashMap<String, ConfigBinding> bindings = new HashMap<String, ConfigBinding>();
	private int maxLabel = 0;
	private String currentMode;

	private boolean editorWindowDisabled;

	/**
	 * TODO: For now, can only attach a listener to one item.
	 * We need to keep a reference so the listener is not removed.
	 * Maybe instead we should use an array? of listeners.
	 */
	private MetamergeConfigChangeListener listener;

	private HashMap<String, ToolTip> toolTips = new HashMap<String, ToolTip>();
	
	/**
	 * Constructor.
	 *
	 * @param parent
	 *            The parent container
	 * @param style
	 *            Style bits passed on to ScrolledComposite
	 * @param editingConfiguration
	 *            The configuration to edit
	 * @param form
	 *            The form name. Can be null to derive form name from
	 *            editingConfiguration
	 * @throws Exception
	 */
	public FormWidget2(Composite parent, int style, BaseConfiguration editingConfiguration, String form) throws Exception {
		this(parent, style, editingConfiguration, form, true);
	}

	/**
	 * If initialize is false, the initialize() method must be called to create
	 * the contents of the form. Before the form is populated with fields you
	 * can access the Form and add any fields before the contents of the form is
	 * added.
	 *
	 * @param parent
	 *            The parent container
	 * @param style
	 *            Style bits passed on to ScrolledComposite
	 * @param editingConfiguration
	 *            The configuration to edit
	 * @param form
	 *            The form name. Can be null to derive form name from
	 *            editingConfiguration
	 * @param initialize
	 *            true to create all objects, false to delay content creation
	 * @throws Exception
	 */
	public FormWidget2(Composite parent, int style, BaseConfiguration editingConfiguration, String form, boolean initialize)
			throws Exception {
		super(parent, SWT.NONE);
		this.editingConfig = editingConfiguration;
		this.formName = form;

		if (formName == null) {
			formName = Utils.getFormName(editingConfiguration);
		}

		if (formName == null) {
			super.dispose();
			throw new Exception(Messages.getMessage("FormWidget.form.unknown", editingConfiguration.getClass().getName()));
		}

		getForms();

		stackLayout = new StackLayout();
		setLayout(stackLayout);

		scrolledComposite = new ScrolledComposite(this, style | SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);

		stackLayout.topControl = scrolledComposite;

		// -- Form contents goes here
		content = new Composite(scrolledComposite, SWT.NONE);
		content.setLayout(new FillLayout());
		content.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		tk = new FormToolkit(getDisplay());
		this.form = tk.createForm(content);

		if (initialize)
			initialize();
	}

	/**
	 * Create a FormWidget2 with a given FormConfig
	 *
	 * @param parent
	 *            The parent container
	 * @param style
	 *            Style bits passed on to ScrolledComposite
	 * @param editingConfiguration
	 *            The configuration to edit
	 * @param formConfig
	 *            The FormConfig
	 * @throws Exception
	 */
	public FormWidget2(Composite parent, BaseConfiguration editingConfiguration, FormConfig formConfig) throws Exception {
		super(parent, SWT.NONE);
		this.editingConfig = editingConfiguration;
		this.formName = "";
		this.formConfig = formConfig;
		global = Utils.getSystemForm("__GLOBAL__");

		stackLayout = new StackLayout();
		setLayout(stackLayout);

		scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);

		stackLayout.topControl = scrolledComposite;

		// -- Form contents goes here
		content = new Composite(scrolledComposite, SWT.NONE);
		content.setLayout(new FillLayout());
		content.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		tk = new FormToolkit(getDisplay());
		this.form = tk.createForm(content);

		initialize();
	}

	/**
	 * Loads the script engine and executes "etc/ASForms.txt".
	 *
	 * @throws Exception
	 */
	private void loadScriptEngine() throws Exception {
		StringBuilder buf = new StringBuilder();
		BufferedReader inp = new BufferedReader(new InputStreamReader(Activator.getDefault().getResource("etc/ASForms.txt")));
		String str;
		while ((str = inp.readLine()) != null) {
			buf.append(str);
			buf.append("\n");
		}
		inp.close();

		ibmjs.declareStaticBean("form", this);
		ibmjs.declareStaticBean("util", this);
		ibmjs.declareStaticBean("config", getEditingConfig());
		ibmjs.declareStaticBean("system", new UserFunctions());
		ibmjs.declareStaticBean("main", SystemFunctions.getServer());

		try {
			ibmjs.exec(buf);
		} catch (Exception e) {
			EclipseAppender.logerror(formName + ": " + e.toString(), e);
		}

		if (formConfig != null && formConfig.getFormEventHandler() != null) {
			try {
				ibmjs.eval(formConfig.getFormEventHandler());
			} catch (Exception e) {
				EclipseAppender.logerror(formName + ": " + e.toString(), e);
			}
		}
		getEditingConfig().addListener(this);
	}

	/**
	 * Called when one of the configuration keys has changed. This method will
	 * then call the Form's onchange script, and then the input items' onchange
	 * script.
	 */
	public void configurationChanged(MetamergeConfigChange mcc) {
		if (isDisposed())
			return;
		// Call this form's event handler
		formEvent(mcc);
	}

	/**
	 * Form event dispatcher. This method calls form and item methods based on
	 * the change event.
	 */
	public void formEvent(Object event) {
		String method = null;
		String param = null;
		boolean inheritedChange = false;
		if (event instanceof MetamergeConfigChange) {
			Object key = ((MetamergeConfigChange) event).getKey();
			if (key == null)
				return;
			param = key.toString();
			method = param.replace(".", "_") + "_changed";
			Object src = ((MetamergeConfigChange) event).getSource();
			if (src instanceof BaseConfiguration && ((BaseConfiguration)src).getMetamergeConfig() != getEditingConfig().getMetamergeConfig())
				inheritedChange = true;
		} else {
			method = event.toString();
		}
 
		callMethodInUIThread(method);

		if (param == null) {
			updateRequiredFieldsMessagesFromEvent();
			return;
		}
		if (formConfig == null)
			return;
		FormItemConfig fic = formConfig.getFormItem(param);
		if (fic == null)
			fic = global.getFormItem(param);

		if (fic != null) {
			if (inheritedChange)
				updateControlFromEvent(param);
			else if (fic.isRequired() || "number".equalsIgnoreCase(fic.getSyntax()))
				updateRequiredFieldsMessagesFromEvent();
		}
	}

	private void callMethodInUIThread(final String method) {
		// The method needs to be called in the UI Thread,
		// as it may e.g. enable or disable controls, which can
		// only be done in the UI Thread.
		if (Display.getCurrent() == null) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {					
					try {
						ibmjs.call(method, null, true);
					} catch (Exception error) {
						EclipseAppender.logerror(method, error);
					}
				}
			});			
		} else {
			try {
				ibmjs.call(method, null, true);
			} catch (Exception error) {
				MessageDialog.openError(getShell(), Messages.getString("general.error.label"), error.toString());
			}
		}		
	}

	private void updateControlFromEvent(final String param) {
		// Run the method in the correct thread, to avoid errors
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateControl(param);
			}
		});
	}

	private void updateRequiredFieldsMessagesFromEvent() {
		// Run the method in the correct thread, to avoid errors
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateRequiredFieldsMessages();
			}
		});
	}

	/**
	 * This method updates the form title with messages indicating required
	 * fields missing values. Controls that are missing (null) or disabled will
	 * have their required message removed (if any) and the background reset to
	 * white.
	 */
	public void updateRequiredFieldsMessages() {
		if (isDisposed() || form.isDisposed())
			return;
		form.setMessage(null);
		ArrayList<IMessage> msgs = new ArrayList<IMessage>();
		for (String str : controls.keySet()) {
			FormItemConfig fic = formConfig.getFormItem(str);
			if (fic == null)
				fic = global.getFormItem(str);

			// Disabled controls cannot be required
			Control control = controls.get(str);
			if (control == null || control.isDisposed())
				continue;
			if (!control.isEnabled()) {
				control.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
				continue;
			}

			// If this is a panel widget let it do the checking internally (could have more than one control)
			if(control instanceof FormPanelWidget) {
				((FormPanelWidget)control).updateRequiredFieldsMessages(msgs);
			} else {
				// Check if the value causes an exception
				Object val = getEditingConfig().getParameter(str);
				IMessage msg;
				if (val instanceof Throwable) {
					msg = new FieldErrorMessage(fic, (Throwable)val);
				} else if (fic != null && fic.isRequired() && (val == null || val.toString().length() == 0)) {
					msg = new RequiredFieldMessage(fic);
				} else {
					msg = checkNumber(fic, val);
				}
				if (msg == null) {
					if (! (control instanceof Combo)) {
						control.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));					
						control.setForeground(getForeground());
					}
					continue;
				}

				msgs.add(msg);
				//
				if (! (control instanceof Combo)) {
					control.setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
					control.setForeground(getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
				}
			}
		}
		if (msgs.size() > 0) {
			String message = Messages.getMessage("FormWidget2.missing.values", String.valueOf(msgs.size()));
			if (msgs.size() == 1)
				message = msgs.get(0).getMessage();
			// Avoid multi-line messages
			if (message.indexOf("\n") != -1)
				message = message.substring(0, message.indexOf("\n"));
			form.setMessage(message, IMessage.ERROR, msgs.toArray(new IMessage[0]));
		}
	}

	private IMessage checkNumber(FormItemConfig fic, Object o) {
		if (fic == null || o == null || ! "number".equalsIgnoreCase(fic.getSyntax()))
			return null;

		String val = o.toString();
		if (val.length() == 0)
			return null;
		
		long l;
		try {
			l = Long.parseLong(val);
		} catch (NumberFormatException nfe) {
			return new NumberErrorMessage(fic, Messages.getMessage("FormWidget2.notNumber", fic.getLabel()));
		}
		String s = fic.getStringParameter("minValue");
		if (s != null && l < Long.parseLong(s)) {
			return new NumberErrorMessage(fic, Messages.getMessage("FormWidget2.minValue", fic.getLabel(), s));
		}
		s = fic.getStringParameter("maxValue");
		if (s != null && l > Long.parseLong(s)) {
			return new NumberErrorMessage(fic, Messages.getMessage("FormWidget2.maxValue", fic.getLabel(), s));
		}
		return null;
	}

	/**
	 * This method disposes the eclipse form that contains the TDI form
	 * controls. Use this method to reinitialize the form. This method is
	 * typically used when the user change the connector mode, which should
	 * refresh the form with a potentially different set of form fields.
	 */
	public void resetForm() {
		if (form != null)
			form.dispose();
		labels.clear();
		controls.clear();
		extraButton1.clear();
		extraButton2.clear();
		optionButtons.clear();
		bindings.clear();

		this.form = tk.createForm(content);
	}

	/**
	 * Initializes the form. When a complete refresh of the form is needed this
	 * method can be called to recreate the form.
	 *
	 * @throws Exception
	 */
	public void initialize() throws Exception {
		createUI(content);
		scrolledComposite.setContent(content);
		updateMinSize();

		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				updateMinSize();
			}
		});

		loadScriptEngine();
		updateRequiredFieldsMessages();
	}

	/**
	 * Returns true if the form has sections
	 *
	 * @return
	 */
	public boolean hasSections() {
		return formConfig.getSectionNames().size() > 0;
	}

	/**
	 * Update the minimum size so the parent container can refresh scroll bars.
	 */
	private void updateMinSize() {
		scrolledComposite.setMinSize(scrolledComposite.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * Retrieve user form and global form.
	 *
	 * @throws Exception
	 */
	private void getForms() throws Exception {

		if (global == null)
			global = Utils.getSystemForm("__GLOBAL__");

		if ("".equals(formName) || formName.startsWith("@"))
			return;

		if (formName.equals(EMBEDDED_FORM_NAME)) {
			formConfig = loadCustomForm(getEditingConfig().getStringParameter(EMBEDDED_FORM_NAME));
		} else {

			// Try system/internal namespaces
			try {
				formConfig = Utils.getSystemForm(formName);
				if (formConfig != null)
					return;
			} catch (Exception e) {
			}

			// Backwards compat code
			String f = (formName.indexOf(":") == -1 ? "system:/Forms/" + formName : formName);
			if (getEditingConfig() != null)
				formConfig = (FormConfig) getEditingConfig().getMetamergeConfig().lookup(f);
			else
				formConfig = (FormConfig) MetamergeConfigFactory.lookup(null, f);
		}
	}

	/**
	 * Adds a control to the controls table.
	 *
	 * @param shortName
	 *            The name of the control
	 * @param control
	 *            The control
	 */
	private void setControl(String shortName, Control control) {
		controls.put(shortName, control);
	}

	/**
	 * Creates a tabbed pane with forms
	 *
	 * @param parent
	 * @throws Exception
	 */
	private void createTabbedUI(Composite parent) throws Exception {
		TabFolder tabs = new TabFolder(parent, SWT.TOP);
		for (String str : formConfig.getTabNames()) {
			TabItem item = new TabItem(tabs, SWT.LEFT);
			String title = formConfig.getTabTitle(str);
			item.setText(title == null ? str : title);
			item.setToolTipText(formConfig.getTabToolTip(str));
			FormWidget2 frm = new FormWidget2(tabs, getEditingConfig(), Utils.getSystemForm(str));
			item.setControl(frm);
		}
	}

	/**
	 * Create sections and their fields.
	 *
	 * @param parent
	 * @throws Exception
	 */
	private void createUI(Composite parent) throws Exception {

		if (formConfig == null)
			return;

		if (formConfig.getTitle() != null && formConfig.getTitle().length() > 0) {
			form.setText(formConfig.getTitle());
			tk.decorateFormHeading(form);
		}

		if (formConfig.getTabNames() != null && formConfig.getTabNames().size() > 0) {
			form.getBody().setLayout(new FillLayout());
			createTabbedUI(form.getBody());
			return;
		}

		if (formConfig.getParameter("CustomJavaClass") != null) {
			form.getBody().setLayout(new FillLayout());
			createCustomForm(form.getBody());
			return;
		}
		form.getBody().setLayout(new GridLayout(GRID_COLUMNS, false));

		//
		// Get mode if it is a connector
		//
		ConnectorConfig cc = Utils.getParentConfig(editingConfig, ConnectorConfig.class);
		if (cc != null)
			currentMode = cc.getMode();
		else
			currentMode = null;

		maxLabel = findLongestLabel();

		//
		// Create sections
		//
		if (formConfig.getSectionNames().size() > 0) {

			for (String section : formConfig.getSectionNames()) {

				String sectionName = section;
				boolean shouldExist = true;
				if (currentMode != null && sectionName.startsWith("$Mode-")) {
					sectionName = currentMode + sectionName.substring(5);
					shouldExist = false;
				}

				FormSection fsec = formConfig.getSection(sectionName);
				if (fsec == null) {
					// this should not happen but custom forms may have errors
					// in them
					if (shouldExist)
						throw new NameNotFoundException(formName + "." + sectionName);
					continue;
				}

				int flags = 0;
				if (fsec.getTitle() != null && fsec.getTitle().length() > 0) {
					flags |= Section.TITLE_BAR | Section.TWISTIE;
				}

				if (fsec.initiallyExpanded())
					flags |= Section.EXPANDED;

				Section sec = tk.createSection(form.getBody(), flags);
				if (fsec.getTitle() != null) {
					sec.setText(fsec.getTitle());
				}

				sec.addExpansionListener(new IExpansionListener() {
					public void expansionStateChanged(ExpansionEvent e) {
						updateMinSize();
					}

					public void expansionStateChanging(ExpansionEvent e) {
					}
				});

				GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
				gd.horizontalSpan = GRID_COLUMNS;
				sec.setLayoutData(gd);

				Composite client = tk.createComposite(sec);
				Utils.setGridLayout(client, GRID_COLUMNS, false);
				sec.setClient(client);
				addFormFields(fsec, client);
				// Remove the section if it contains nothing
				if (client.getChildren().length == 0)
					sec.dispose();
			}
		} else {
			Composite client = tk.createComposite(form.getBody());
			Utils.setGridLayout(client, GRID_COLUMNS, false);

			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.horizontalSpan = GRID_COLUMNS;
			client.setLayoutData(gd);

			addFormFields(formConfig, client);
		}

	}

	/**
	 * Creates a custom panel
	 *
	 * @param parent
	 * @throws Exception
	 */
	private void createCustomForm(Composite parent) throws Exception {
		String clazz = formConfig.getStringParameter("CustomJavaClass");
		Class<?>[] params = new Class[] { Composite.class, BaseConfiguration.class, FormConfig.class };
		Object[] envp = new Object[] { parent, editingConfig, formConfig };

		Class.forName(clazz).getConstructor(params).newInstance(envp);
	}

	/**
	 * Creates a FormConfig object from the embedded form definition in the
	 * configuration.
	 *
	 * @param str
	 * @return
	 * @throws Exception
	 */
	private FormConfig loadCustomForm(String str) throws Exception {
		TDIConfigurationFile mc = new TDIConfigurationFile(new ByteArrayInputStream(str.trim().getBytes("UTF-8")), false);
		return (FormConfig) mc.getDefaultConfigObject();
	}

	/**
	 * Computes the width (GC.textExtent) for the longest label in the form
	 * using the current font.
	 *
	 * @return
	 */
	public int findLongestLabel() {
		return findLongestLabel(new String[] { propertyTag, substitutionTag, javaScriptTag });
	}

	/**
	 * Computes the width (GC.textExtent) for the longest label in the form
	 * using the current font.
	 *
	 * @param additionalLabels
	 *            Test for additional strings that are not part of the form
	 * @return
	 */
	public int findLongestLabel(String[] additionalLabels) {
		//
		if (formConfig == null)
			return -1;

		GC gc = new GC(getDisplay());
		gc.setFont(form.getBody().getFont());

		ConnectorConfig cc = Utils.getParentConfig(editingConfig, ConnectorConfig.class);
		int max = 0;

		for (String section : formConfig.getSectionNames()) {

			String sectionName = section;
			if (cc != null && sectionName.startsWith("$Mode-"))
				sectionName = cc.getMode() + sectionName.substring(5);

			FormSection fsec = formConfig.getSection(sectionName);
			if (fsec == null) {
				continue;
			}

			for (String str : (Vector<String>) fsec.getNames()) {
				FormItemConfig fic = null;
				if (str.startsWith("$GLOBAL.")) {
					fic = global.getFormItem(str.substring(8));
				} else {
					fic = fsec.getFormItem(str);
					if (fic == null)
						fic = formConfig.getFormItem(str);
				}
				if (fic != null && fic.getLabel() != null) {
					String label = fic.getLabel();
					if (fic.isRequired())
						label += " *";

					max = Math.max(max, gc.textExtent(label).x);
				}
			}
		}

		for (String str : formConfig.getFormItemNames()) {
			FormItemConfig fic = null;
			if (str.startsWith("$GLOBAL.")) {
				fic = global.getFormItem(str.substring(8));
			} else {
				fic = formConfig.getFormItem(str);
			}
			if (fic != null && fic.getLabel() != null) {
				String label = fic.getLabel();
				if (fic.isRequired())
					label += " *";
				max = Math.max(max, gc.textExtent(label).x);
			}
		}

		if (additionalLabels != null) {
			for (String str : additionalLabels) {
				max = Math.max(max, gc.textExtent(str).x);
			}
		}

		gc.dispose();

		return max;
	}

	/**
	 * Adds fields from a FormSection. An expandable composite is created if the
	 * section has a title.
	 *
	 * @param section
	 * @param parent
	 */
	private void addFormFields(FormSection section, Composite parent) {
		for (String str : (Vector<String>) section.getNames()) {
			FormItemConfig fic = null;
			if (str.startsWith("$GLOBAL.")) {
				fic = global.getFormItem(str.substring(8));
			} else {
				fic = section.getFormItem(str);
				if (fic == null)
					fic = formConfig.getFormItem(str);
			}
			if (fic != null)
				addFormField(fic, parent);
			else
				EclipseAppender.logerror(str, new NameNotFoundException(str), getShell());
		}
	}

	/**
	 * Add form fields from the main config
	 *
	 * @param config
	 * @param parent
	 */
	private void addFormFields(FormConfig config, Composite parent) {
		for (String str : (List<String>) config.getFormItemNames()) {
			FormItemConfig fic = null;
			if (str.startsWith("$GLOBAL.")) {
				fic = global.getFormItem(str.substring(8));
			} else {
				fic = config.getFormItem(str);
				if (fic == null)
					fic = formConfig.getFormItem(str);
			}
			if (fic != null)
				addFormField(fic, parent);
			else
				EclipseAppender.logerror(str, new NameNotFoundException(str), getShell());
		}
	}

	/**
	 * Returns the title from the form config
	 *
	 * @return
	 */
	public String getTitle() {
		String title = formConfig.getTitle();
		if (title == null)
			return "";
		else
			return title;
	}

	/**
	 * Translates a String using the form config's translate method
	 *
	 * @param str
	 *            The String to translate
	 * @return The translated String
	 */
	public String translate(String str) {
		return formConfig.translate(str);
	}

	@Override
	public void dispose() {
		tk.dispose();
		getEditingConfig().removeListener(this);
		if (scrolledComposite != null)
			scrolledComposite.dispose();
		super.dispose();
	}

	private BaseConfiguration getEditingConfig() {
		return editingConfig;
	}

	/**
	 * Returns the Form used by this widget
	 *
	 * @return
	 */
	public Form getForm() {
		return form;
	}

	/**
	 * Returns the form toolkit used by this widget
	 *
	 * @return
	 */
	public FormToolkit getFormToolkit() {
		return tk;
	}

	/**
	 * Add a field to a composite
	 *
	 * @param formItem
	 * @param parent
	 */
	private void addFormField(FormItemConfig formItem, Composite parent) {
		GridData gd;

		if (isExcludedByModeParameter(formItem))
			return;

		if (EDITOR_SYNTAX.equalsIgnoreCase(formItem.getSyntax()) && isEditorWindowDisabled())
			return;

		final String paramName = formItem.getShortName();
		final String toolTip = formItem.getToolTip();
		final String labelString = formItem.getLabel();

		//
		// Section items can override top-level form item configs.
		//
		FormItemConfig fcItem = formConfig.getFormItem(paramName);
		if (fcItem != formItem)
			formItem.setInheritsFrom(fcItem);

		boolean noLabel = formItem.getBooleanParameter("noLabel", false);

		Label label = null;
		if (!noLabel) {
			label = new Label(parent, SWT.RIGHT);
			label.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
			String str = labelString;
			if (str == null)
				str = " ";
			if (formItem.isRequired())
				str += " *";
			label.setText(str);
			label.setBackground(parent.getBackground());
			gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			if (maxLabel > 0)
				gd.widthHint = maxLabel;
			label.setLayoutData(gd);

			// Add a mouse click listener to bring up param subst editor
			if (!formConfig.getBooleanParameter("noParameterSubstitutionEditor", false)) //added by L3 defect 14182
			label.addMouseListener(new MouseAdapter() {
				boolean md = false;

				public void mouseDown(MouseEvent e) {
					md = true;
				}

				public void mouseUp(MouseEvent e) {
					if (md)
						showParamSubstEditor(paramName, toolTip, labelString);
					md = false;
				}
			});

			labels.put(paramName, label);
			setLabelColor(paramName);
		}

		//
		// Add a lead-in text that appears right above the control
		//
		String leadText = formItem.getLeadText();
		if (leadText != null && leadText.length() > 0) {
			tk.createText(parent, "");
			Text lt = tk.createText(parent, leadText);
			gd = new GridData();
			gd.horizontalSpan = GRID_COLUMNS - (noLabel ? 0 : 1);
			lt.setLayoutData(gd);
		}

		Control control = null;
		ConfigBinding cb = new ConfigBinding(formItem, getEditingConfig(), this);
		String defaultValue = getDefaultValue(formItem, cb);

		String syntax = formItem.getSyntax();
		if (syntax == null && paramName.equalsIgnoreCase("help")) {
			return;

		} else if (BOOLEAN_SYNTAX.equalsIgnoreCase(syntax)) {
			control = tk.createButton(parent, "", SWT.CHECK);
			((Button) control).setSelection(Boolean.valueOf(defaultValue));

		} else if (DROPLIST_SYNTAX.equalsIgnoreCase(syntax) || DROPDOWN_SYNTAX.equalsIgnoreCase(syntax)) {
			boolean dropList = DROPLIST_SYNTAX.equalsIgnoreCase(syntax);
			int flags = dropList || formItem.isReadOnly() ? SWT.READ_ONLY : SWT.NONE;
			Combo combo = new Combo(parent, SWT.DROP_DOWN | flags);
			setComboValues(combo, formItem, dropList, defaultValue, false);

			// Guess a size that might be enough to hold all characters to be shown.
			// Or we could try to compute a size, but I don't know how.
			// Luckily we only need to do this if the Combo is editable.
			if ( (flags&SWT.READ_ONLY) == 0 && defaultValue != null && defaultValue.length() > 10)
				combo.setSize(400, SWT.DEFAULT);

			// -- Add a focus tracker to Combo so we can handle cut/copy/paste
			if (getSite() != null) {
				Object o = getSite().getService(IFocusService.class);
				if (o instanceof IFocusService)
					((IFocusService) o).addFocusTracker(combo, "com.ibm.tdi.text.control");
			}

			control = combo;

		} else if (TEXTAREA_SYNTAX.equalsIgnoreCase(syntax)) {
			control = createText(parent, defaultValue, SWT.BORDER | SWT.MULTI | SWT.WRAP);

		} else if (STATIC_SYNTAX.equalsIgnoreCase(syntax)) {
			control = tk.createText(parent, toolTip, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);

		} else if (PASSWORD_SYNTAX.equalsIgnoreCase(syntax)) {
			control = tk.createText(parent, defaultValue, SWT.BORDER);
			((Text) control).setEchoChar('*');

		} else if (SCRIPT_SYNTAX.equalsIgnoreCase(syntax)) {
			control = addFormButton(formItem.getDefaultValue(), formItem.getScriptLabel(), formItem.getScriptToolTip(), paramName,
					parent);

		} else if (COMPONENT_SYNTAX.equalsIgnoreCase(syntax)) {
			control = addComponent(formItem, parent);

		} else if (EDITOR_SYNTAX.equalsIgnoreCase(syntax)) {

			final FormScriptEditor fse = new FormScriptEditor(FormWidget2.this, labelString != null ? labelString : paramName);

			cb.setControlNoListener(fse.getEditor().getSourceViewer().getTextWidget());
			fse.getEditor().setConfigBinding(cb);
			bindings.put(paramName, cb);
			controls.put(paramName, fse.getEditor());

			Button btn = tk.createButton(parent, Messages.getString("FormWidget.script"), SWT.PUSH);
			final ConfigBinding configBinding = cb;

			btn.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					fse.getEditor().setText(String.valueOf(configBinding.getValue()));
					stackLayout.topControl = fse;
					fse.getEditor().setFocus();
					 // TODO: updateMinSize();
					FormWidget2.this.layout();
				}
			});

			control = btn;

		} else if (PANEL_SYNTAX.equalsIgnoreCase(syntax)) {
			control = createPanel(parent, formItem);
		} else {
			control = createText(parent, defaultValue, SWT.BORDER);
		}

		assert control != null;

		// -- Add context menu for text fields (override native platform context
		// menu)
		IOperationHistory operationHistory = null;
		IUndoContext undoContext = null;
		if(getSite() != null) {
			operationHistory = getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getOperationHistory();
			undoContext = getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getUndoContext();
		}

		if (control instanceof Text) {
			new TextEditorContextMenu((Text) control);
			new UndoRedoSupport((Text)control, formItem.getLabel(), operationHistory, undoContext);
		} else if (control instanceof StyledText) {
			new TextEditorContextMenu((StyledText) control);
			new UndoRedoSupport((StyledText)control, formItem.getLabel(), operationHistory, undoContext);
		} else if (control instanceof Combo) {
			new TextEditorContextMenu((Combo) control);
		}

		// -- focus listener
		control.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				for(String str : controls.keySet()) {
					if(controls.get(str) == e.widget) {
						formEvent(str + "_focusin");
					}
				}
			}
			public void focusLost(FocusEvent e) {
				for(String str : controls.keySet()) {
					if(controls.get(str) == e.widget) {
						formEvent(str + "_focusout");
					}
				}
				// set selection to nothing when losing focus
				if (e.widget instanceof StyledText) {
					int i = ((StyledText)e.widget).getCaretOffset();
					((StyledText)e.widget).setSelectionRange(i,0);
				} else if (e.widget instanceof Text) {
					// Not sure if this case is possible?
					int i = ((Text)e.widget).getCaretPosition();
					((Text)e.widget).setSelection(i,i);
				}
				
			}
		});

		if (toolTip != null)
			createToolTip(toolTip, label);

		control.setBackground(parent.getBackground());

		int extraColumns = noLabel ? 3 : 2;

		// Button1
		String buttonScript = formItem.getScript();
		if (buttonScript != null && buttonScript.length() > 0) {
			extraButton1.put(paramName,
					addFormButton(buttonScript, formItem.getScriptLabel(), formItem.getScriptToolTip(), paramName, parent));
			extraColumns--;
		}

		// Button2
		buttonScript = formItem.getScript2();
		if (buttonScript != null && buttonScript.length() > 0) {
			extraButton2.put(paramName,
					addFormButton(buttonScript, formItem.getScriptLabel2(), formItem.getScriptToolTip2(), paramName, parent));
			extraColumns--;
		}

		boolean noOptionButton = formItem.getBooleanParameter("noOptionButton", false) ||
		formConfig.getBooleanParameter("noParameterSubstitutionEditor", false); //added by L3 defect 14182
		if (noOptionButton)
			extraColumns++;

		gd = new GridData(control instanceof Button ? SWT.LEFT : SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = 1 + extraColumns;
		if (TEXTAREA_SYNTAX.equalsIgnoreCase(formItem.getSyntax())) {
			int lines = formItem.getIntegerParameter("lines", 7);
			gd.heightHint = (lines * 15);
			if (label != null)
				((GridData) label.getLayoutData()).verticalAlignment = SWT.TOP;
		}
		if (DROPDOWN_SYNTAX.equalsIgnoreCase(formItem.getSyntax()) && defaultValue != null && defaultValue.length() > 4) {
			gd.minimumWidth = 300;
		}
		control.setLayoutData(gd);

		if (!EDITOR_SYNTAX.equalsIgnoreCase(syntax)) {
		cb.setControl(control);
		bindings.put(paramName, cb);

		setControl(paramName, control);
		}
		if (formItem.isRequired() && ! (control instanceof Combo)) {
			control.setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			control.setForeground(getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		}

		if (noOptionButton)
			return;

		Button options = new Button(parent, SWT.PUSH);
		options.setImage(Activator.getImage("edit"));
		createToolTip(Messages.getString("FormWidget.field.options.tooltip"), options);
		options.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showParamSubstEditor(paramName, toolTip, labelString);
			}
		});
		optionButtons.put(paramName, options);
	
	}

	/**
	 * Creates a mini-form for the provided form item. A mini form is simply a component where one parameter is
	 * represented by many UI controls.
	 *
	 * @param formItem
	 * @return
	 * @throws Exception
	 */
	public Control createPanel(Composite parent, FormItemConfig formItem) {
		try {
			return new FormPanelWidget(parent, getEditingConfig(), formItem, this);
		} catch (Exception e) {
			Label label = new Label(parent, SWT.LEFT);
			label.setText(e.toString());
			return label;
		}
	}

	/**
	 * This method was introduced to override FormToolKit.createText since
	 * native widgets on certain unix platforms are more or less foobared.
	 * Instead we create a StyledText widget to avoid the native widget.
	 *
	 * @param parent
	 * @param defaultValue
	 * @param style
	 * @return
	 */
	private Control createText(Composite parent, String defaultValue, int style) {
		if ((style & SWT.MULTI) == 0)
			style |= SWT.SINGLE;
		else
			style |= SWT.V_SCROLL; //TODO: Make a better decision on when to use scrollbars
		StyledText text = new StyledText(parent, style);
		if (defaultValue != null)
			text.setText(defaultValue);
		return text;
	}

	private boolean isExcludedByModeParameter(FormItemConfig formItem) {
		if (currentMode == null)
			return false;
		return !formItem.isValidForMode(currentMode);
	}

	private Button addFormButton(String script, String scriptLabel, String scriptToolTip, String attributeName, Composite parent) {
		// Button b1 = new Button(parent, SWT.PUSH);
		Button b1 = tk.createButton(parent, scriptLabel, SWT.PUSH);
		b1.setText(scriptLabel);
		if (scriptToolTip != null)
			createToolTip(scriptToolTip, b1);
		b1.setLayoutData(new GridData(SWT.DEFAULT, SWT.TOP, false, false));
		b1.setData(SCRIPT_KEY, script);
		b1.setData(ATTRIBUTE_KEY, attributeName);
		b1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				executeScript((Button) e.widget);
			}
		});
		return b1;
	}

	private Control addComponent(FormItemConfig formItem, Composite parent) {
		try {
			String className = formItem.getComponentClass();
			if (className == null)
				throw new Exception(Messages.getMessage("MIADMIN.FORMUI.FORM.REFERENCES.NO.COMPONENT.PROVIDED", formItem
						.getShortName()));
			Object[] cp = new Object[] { this, parent, getEditingConfig(), formItem.getShortName() };
			Class<?>[] args = new Class[] { FormWidget2.class, Composite.class, BaseConfiguration.class, String.class };
			return (Control) Class.forName(className).getConstructor(args).newInstance(cp);
		} catch (Throwable err) {
			return tk.createText(parent, err.getMessage(), SWT.BORDER);
		}
	}

	protected void executeScript(Button button) {
		String script = (String) button.getData(SCRIPT_KEY);
		form.setMessage(null);
		try {
			String str = formConfig.getFormScript();
			if (str != null)
				ibmjs.interpret(str);

			ibmjs.declareBean("attributeName", button.getData(ATTRIBUTE_KEY));
			ibmjs.declareBean("button", new DummyButton((String) button.getData(ATTRIBUTE_KEY)));
			ibmjs.call(script, null, false);
		} catch (Exception err) {
			if (!isDisposed()) {
				this.form.setMessage(err.toString());
				EclipseAppender.logerror(err.toString(), err, button.getShell());
			} else {
				EclipseAppender.logerror(err.toString(), err);
			}
		}
	}

	private void setComboValues(final Combo combo, final FormItemConfig formItem, final boolean dropList, String defaultValue, boolean isRefresh) {
		if (isRefresh) {
			defaultValue = combo.getText();
			combo.removeAll();
		} else if (formItem.getValues().contains("@ATTRS") || formItem.getValues().contains("@SCHEMA")) {
			final ConnectorConfig cc = Utils.getParentConfig(getEditingConfig(), ConnectorConfig.class);
			if (cc != null)	{
				listener = new MetamergeConfigChangeListener() {
					boolean batch = false;
					public void configurationChanged(MetamergeConfigChange changeEvent) {
						if (combo.isDisposed()) {
							cc.getAttributeMap(true).removeListener(this);
							cc.getSchema(true).removeListener(this);
							return;
						}
						int op = changeEvent.getOperation();
						if (op == MetamergeConfigChange.BEGIN_CHANGES) {
							batch = true;
						} else if (op == MetamergeConfigChange.END_CHANGES || (!batch &&
								(op == MetamergeConfigChange.MCC_REPLACE ||
								 op == MetamergeConfigChange.MCC_REMOVE))) {
							batch = false;
							getDisplay().asyncExec(new Runnable() {
								public void run() {
									setComboValues(combo, formItem, dropList, null, true);
								}							
							});
						}
					}
				};
				if (formItem.getValues().contains("@ATTRS"))
					cc.getAttributeMap(true).addListener(listener);
				if (formItem.getValues().contains("@SCHEMA"))
					cc.getSchema(true).addListener(listener);
			}
		}

		for (String val : getFieldValues(formItem, dropList))
			combo.add(val);

		if (defaultValue != null) {
			int current = -1;
			if (formItem.isIndexBased()) {
				try {
					current = Integer.parseInt(defaultValue);
				} catch (NumberFormatException nfe) {
					current = -1;
				}
			}
			if (current == -1 && dropList)
				current = formItem.getValues().indexOf(defaultValue);
			if (current == -1)
				current = combo.indexOf(defaultValue);
			if (current == -1 &&
				(!dropList || formItem.getValues().size() == 0)) {
				combo.add(defaultValue, 0);
				current = 0;
			}
			combo.select(current);
		}
		if (isRefresh)
			combo.layout();
	}

	private List<String> getFieldValues(FormItemConfig formItem, boolean dropList) {
		// We cannot expand macros for a droplist, that would make it impossible
		// to know
		// which value was selected, since they may be translated
		if (dropList)
			return formItem.getLocalizedValues();

		List<String> list = new ArrayList<String>();
		for (String str : formItem.getValues()) {
			if ("@PARSERS@".equals(str)) {
				list.addAll(ConfigUtils.getComponentsAsStrings(getEditingConfig(), MetamergeConfig.PARSER_FOLDER));

			} else if ("@CONNECTORS@".equals(str)) {
				list.addAll(ConfigUtils.getComponentsAsStrings(getEditingConfig(), MetamergeConfig.CONNECTOR_FOLDER));

			} else if ("@FUNCTIONS@".equals(str)) {
				list.addAll(ConfigUtils.getComponentsAsStrings(getEditingConfig(), MetamergeConfig.FUNCTION_FOLDER));

			} else if ("@ATTRIBUTEMAPS@".equals(str)) {
				list.addAll(ConfigUtils.getComponentsAsStrings(getEditingConfig(), MetamergeConfig.ATTRIBUTEMAP_FOLDER));

			} else if ("@REUSE_CONNECTORS@".equals(str)) {
				list.addAll(ConfigUtils.getReuseConnectors(getEditingConfig()));

			} else if ("@LIB_CONNECTORS@".equals(str)) {
				list.addAll(ConfigUtils.getLibConnectors(getEditingConfig()));

			} else if ("@ASSEMBLY_LINES@".equals(str) || "@ASSEMBLYLINES@".equals(str)) {
				list.addAll(ConfigUtils.getComponentsAsStrings(getEditingConfig(), MetamergeConfig.ASSEMBLYLINE_FOLDER));

			} else if ("@ATTRS".equals(str)) {
				ConnectorConfig cc = Utils.getParentConfig(getEditingConfig(), ConnectorConfig.class);
				if (cc != null) {
					boolean autoMap = false;
					try {
						AssemblyLineConfig alc = Utils.getParentConfig(cc, AssemblyLineConfig.class);
						if (alc != null)
							autoMap = alc.autoMapAllAttributes(cc.getShortName());
					} catch (Exception e) {
						autoMap = false;
					}
					if (autoMap)
						list.addAll(cc.getSchema(true).getItemNames());
					else
						list.addAll(cc.getAttributeMap(true).getAttributeNames());
				}
				Collections.sort(list);
			} else if ("@SCHEMA".equals(str)) {
				ConnectorConfig cc = Utils.getParentConfig(getEditingConfig(), ConnectorConfig.class);
				if (cc != null) {
					SchemaConfig schema = cc.getSchema(true);
					for (String name:schema.getItemNames()) {
						addToList(list, schema.getItem(name));
					}
				}
				Collections.sort(list);
			} else {
				list.add(str);
			}
		}
		return list;
	}

	private void addToList(List<String> list, SchemaItemConfig item) {
		ContainerConfig cc = item.getChildSchemaList();
		if (cc.size() == 0) {
			String name = item.getName().toString();
			// Change schema names like "phoneNumber.phoneNumber.value" to "phoneNumber.value"
			int index = name.indexOf('.') + 1;
			if (index > 0 && name.startsWith(name.substring(0, index), index))
				name = name.substring(index);
			list.add(name);
		} else {
			for (int i = 0; i < cc.size(); i++)
				addToList(list, (SchemaItemConfig) cc.getConfig(i));
		}
	}

	/**
	 * Returns the FormConfig used to populate the form
	 *
	 * @return The form configuration
	 */
	public FormConfig getFormConfig() {
		return formConfig;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.custom.ScrolledComposite#getContent()
	 */
	// public Composite getContent() {
	// return content;
	// }
	/**
	 * Creates and returns an instance of the connector based on the editing
	 * configuration.
	 *
	 * @return Connector instance or null if configuration is unknown
	 * @throws Exception
	 */
	public Object loadConnector() throws Exception {
		if (editingConfig.getParent() instanceof PropertyStoreConfig)
			return SystemFunctions.loadConnector((PropertyStoreConfig) editingConfig.getParent());
		else if (editingConfig instanceof PropertyStoreConfig)
			return SystemFunctions.loadConnector((PropertyStoreConfig) editingConfig);
		else if (editingConfig.getParent() instanceof ConnectorConfig)
			return SystemFunctions.loadConnector((ConnectorConfig) editingConfig.getParent());
		else
			throw new Exception(Messages.getMessage("MIADMIN.FORMUI.CANNOT.LOAD.A.CONNECTOR.WITH.CONFIGURATION.TYPE", editingConfig
					.getParent().getClass()));
	}

	/**
	 * Returns the default value for the specified parameter.
	 *
	 * @param paramName
	 * @return
	 */
	public String getDefaultValue(FormItemConfig fic, ConfigBinding cb) {
		Object defobj = cb.getValue();
		if (defobj == null) {
			String a = fic.getDefaultValue();
			if (a != null) {
				// Empty userComment should not be set.
				if (a.length() > 0 || ! InternalSchema.USER_COMMENT.equals(fic.getShortName()))
					cb.setValue(a);
				return a;
			}
		}
		if (defobj instanceof Vector<?>) {
			StringBuilder defval = new StringBuilder();
			Vector<?> v = (Vector<?>) defobj;
			for (int k = 0; k < v.size(); k++) {
				if (k > 0)
					defval.append("\n");
				defval.append(v.elementAt(k).toString());
			}
			return defval.toString();
		} else if (defobj instanceof Throwable) {
			return null;
		} else if (defobj != null) {
			return defobj.toString();
		}
		return "";
	}

	/**
	 * Prompts user for an existing file
	 *
	 * @param path
	 * @param filter
	 * @return
	 */
	public String selectFile(String path, String filter) {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		fd.setFileName(path);
		if (filter != null) {
			if (filter.equals("xml"))
				filter = "*.xml";
			if (filter.equals("jar"))
				filter = "*.jar";
			fd.setFilterExtensions(new String[] { filter });
		}
		return fd.open();
	}
	
	/**
	 * Prompts user for an existing directory
	 *
	 * @param Default start path.
	 * @return The selected directory path.
	 */
	public String selectDirectory(String path) {
		DirectoryDialog dd = new DirectoryDialog(getShell(), SWT.OPEN);
		dd.setMessage(Messages.getString("Connector.ChooseDirectoryDialog.Label"));
		dd.setFilterPath(path);
		return dd.open();
	}

	/**
	 * Update the configuration object by setting the named parameter to the given value.
	 *
	 * @param param
	 *            The parameter name
	 * @param value
	 *            The new value
	 */
	public void setConfig(String param, Object value) {
		ConfigBinding cnb = bindings.get(param);
		if (cnb != null && value instanceof String)
			cnb.setValue((String)value);
		else
			getConfigObject().setParameter(param, value);
	}

	/**
	 * Return the configuration being edited
	 *
	 * @return BaseConfiguration being edited
	 */
	public BaseConfiguration getConfigObject() {
		return getEditingConfig();
	}

	/**
	 * Returns the value for a configuration parameter.
	 *
	 * @param param
	 *            Parameter name
	 * @return
	 */
	public Object getConfigValue(String param) {
		return getConfig(param);
	}

	/**
	 * Returns the value for a configuration parameter.
	 *
	 * @param param
	 *            Parameter name
	 * @return
	 */
	public Object getConfig(String param) {
		return getEditingConfig().getParameter(param);
	}

	/**
	 * Updates the control associated with parameter <i>name</i>.
	 *
	 * @param name
	 */
	public void updateControl(String name) {
		if (isDisposed())
			return;
		
		ConfigBinding bnd = bindings.get(name);

		Object obj = (bnd == null ? getConfigObject().getParameter(name) : bnd.getValue());
		if (obj instanceof Exception) {
			String msg = obj.toString();
			if (msg.indexOf("\n") != -1)
				msg = msg.substring(0, msg.indexOf("\n"));
			FormItemConfig fic = formConfig.getFormItem(name);
			if (fic == null)
				fic = global.getFormItem(name);
			if (fic == null)
				return;
			getForm().setMessage(translate(fic.getLabel()) + ": " + msg, IMessage.ERROR,
					new IMessage[] { new FieldErrorMessage(fic, (Throwable) obj) });
			
			// Update control to show a blank/none value rather than the old value which is no longer valid
			obj = null;
		}

		// Nothing to update if we don't have a control.
		Control c = controls.get(name);
		if (c == null) {
			return;
		}

		// CB will respond by setting the value so we disable updates
		if (bnd != null)
			bnd.setEnabled(false);

		String val = (obj == null ? "" : obj.toString());
		if (c instanceof StyledText) {
			((StyledText) c).setText(val);
		} else if (c instanceof SimpleTextEditor) {
			((SimpleTextEditor) c).setText(val);
		} else if (c instanceof Text) {
			((Text) c).setText(val);
		} else if (c instanceof Button) {
			((Button) c).setSelection(Boolean.valueOf(val));
		} else if (c instanceof Combo) {
			((Combo) c).setText(val);
		} else if (c instanceof FormPanelWidget) {
			((FormPanelWidget)c).callOnLoad(null);
		} else if (c instanceof SQLStatementEditor) {
			((SQLStatementEditor)c).setText(val);
		}

		if (bnd != null)
			bnd.setEnabled(true);

		updateRequiredFieldsMessages();
		setLabelColor(name);
	}

	/**
	 * Returns the control that edits the value for a parameter.
	 *
	 * @param name
	 *            The parameter name
	 * @return
	 */
	public Object getControl(String name) {
		return controls.get(name);
	}

	/**
	 * Opens a modal dialog where the user can choose from a list of values.
	 *
	 * @param prompt
	 *            The prompt
	 * @param list
	 *            List of values
	 * @return Selected value or null if dialog was canceled
	 */
	public Object chooseFromList(String prompt, List<?> list) {
		if (prompt == null) {
			prompt = "";
		} else {
			String translated = Messages.getString(LOCALIZED + prompt.replaceAll(" ", "."));
			if (translated != null)
				prompt = translated;
			else
				prompt = translate(prompt);
		}

		ArrayList<String> v = new ArrayList<String>(list.size());
		for (Object obj : list) {
			String s = Messages.getString(LOCALIZED + obj);
			if (s != null)
				v.add(s);
			else
				v.add(translate(obj.toString()));
		}

		ComboSelectionDialog dlg = new ComboSelectionDialog(getShell(), v, prompt);
		dlg.open();
		String choice = dlg.getValue();
		if (choice == null)
			return null;

		for (int i = 0; i < v.size(); i++) {
			if (choice.equals(v.get(i)))
				return list.get(i);
		}

		return choice;
	}

	/**
	 * Shows a modal information dialog with the provided message
	 *
	 * @param message
	 */
	public void alert(Object message) {
		MessageDialog.openInformation(getShell(), Messages.getString("miadmin.title.information"), "" + message);
	}

	/**
	 * Shows a modal question dialog with Yes/No buttons
	 *
	 * @param message
	 *            The question/message
	 * @return
	 */
	public boolean prompt(String message) {
		return MessageDialog.openQuestion(getShell(), Messages.getString("miadmin.prompt.confirm"), message);
	}

	/**
	 * Shows a modal input dialog with one input text field.
	 *
	 * @param prompt
	 *            The prompt/message
	 * @return entered value or null if canceled
	 */
	public String input(String prompt) {
		InputDialog id = new InputDialog(getShell(), Messages.getString("miadmin.prompt.input"), prompt, "", null);
		if (id.open() == Window.OK) {
			return id.getValue();
		} else {
			return null;
		}
	}

	/**
	 * Opens a parameter substitution dialog for the specified parameter.
	 *
	 * @param param
	 *            The parameter name
	 */
	public void showParamSubstEditor(String param, String toolTip, String label) {
		try {
			BaseConfiguration config = getEditingConfig();

			FormItemConfig fic = formConfig.getFormItem(param);
			if(fic == null && global != null)
				fic = global.getFormItem(param);

			boolean multiLine = false;
			if(fic != null) {
				if(SCRIPT_SYNTAX.equalsIgnoreCase(fic.getSyntax()) || TEXTAREA_SYNTAX.equalsIgnoreCase(fic.getSyntax()) || EDITOR_SYNTAX.equalsIgnoreCase(fic.getSyntax()))
					multiLine = true;
			} else {
				multiLine = true;
			}

			// Show translated origvalue for droplists
			String origValue = null;
			if(config.getInheritsFrom() != null)
				origValue = config.getInheritsFrom().getStringParameter(param);
			if (origValue != null && DROPLIST_SYNTAX.equalsIgnoreCase(fic.getSyntax())) {
				List<String> values = fic.getValues();
				List<String> translated = fic.getLocalizedValues();
				if (values != null && translated != null && values.indexOf(origValue) >= 0)
					origValue = translated.get(values.indexOf(origValue));
				else
					origValue = translate(origValue);
			}

			String str = ParameterSubstitutionWidget.openPSDialog(getShell(), config, param,
					toolTip, label, multiLine, origValue);
			if (str != null) {
				ConfigBinding cs = bindings.get(param);
				if (cs != null ) {
					if (str.equals("")) {
						cs.setValue("");
					} else {
						cs.setParameterPropertySource(str);
					}
				} else if (str.equals("")) {
					config.setParameter(param, "");
				} else {
					config.setParameterPropertySource(param, str);
				}
			}
			updateControl(param);
			if (controls.get(param) == null)
				setLabelColor(param);
		} catch (Exception err) {
			EclipseAppender.logerror(err.toString(), err, getShell());
		}
	}

	/**
	 * Opens a form with the provided configuration object.
	 *
	 * @param config
	 * @param formName
	 * @return The close option (0 = ok, 1 = cancel)
	 */
	public int openFormDialog(BaseConfiguration config, String formName) {
		GenericFormDialog dlg = new GenericFormDialog(getShell(), formName, config);
		return dlg.open();
	}

	/**
	 * Opens a NVB dialog for the provided configuration.
	 *
	 * @param config
	 * @return The close option (0 = ok, 1 = cancel)
	 */
	public int showNullBehaviorDialog(BaseConfiguration config) {
		NullValueBehaviorWizard wiz = new NullValueBehaviorWizard(config);
		WizardDialog dlg = new WizardDialog(getShell(), wiz);
		return dlg.open();
	}

	public String addFiles(String oldPaths) {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
		if (fd.open() == null)
			return null;

		StringBuilder ret = new StringBuilder();
		if (oldPaths != null)
			ret.append(oldPaths);

		for (String path : fd.getFileNames()) {
			if (ret.length() > 0)
				ret.append("\r\n");
			ret.append(fd.getFilterPath() + File.separator + path);
		}
		if (ret.length() == 0)
			return null;

		return ret.toString();
	}

	/**
	 * Class used with Form.setMessages().
	 */
	private class RequiredFieldMessage implements IMessage {

		protected FormItemConfig fic;

		public RequiredFieldMessage(FormItemConfig fic) {
			super();
			this.fic = fic;
		}

		public Control getControl() {
			return labels.get(fic.getShortName());
		}

		public Object getData() {
			return fic;
		}

		public Object getKey() {
			return fic.getShortName();
		}

		public String getPrefix() {
			return null; // labels.get(fic.getShortName()).getText();
		}

		public String getMessage() {
			return Messages.getMessage("FormWidget2.required", fic.getLabel());
		}

		public int getMessageType() {
			return IMessage.ERROR;
		}

	}

	private class FieldErrorMessage extends RequiredFieldMessage {

		private Throwable t;

		public FieldErrorMessage(FormItemConfig fic, Throwable t) {
			super(fic);
			this.t = t;
		}

		@Override
		public String getMessage() {
			return fic.getLabel() + ": " + t.toString();
		}
	}

	private class NumberErrorMessage extends RequiredFieldMessage {
		private String msg;

		public NumberErrorMessage(FormItemConfig fic, String msg) {
			super(fic);
			this.msg = msg;
		}

		@Override
		public String getMessage() {
			return msg;
		}
	}
	
	/**
	 * A class to let old javascript code reference a button object
	 */
	static class DummyButton {
		private String name;

		public DummyButton(String s) {
			name = s;
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * A combo selection dialog
	 */
	private static class ComboSelectionDialog extends Dialog {

		public String value = null;
		private Combo combo;
		private List<?> options;
		private String msg;

		public ComboSelectionDialog(Shell parent, List<?> options, String msg) {
			super(parent);
			this.options = options;
			this.msg = msg;
		}

		protected Control createDialogArea(Composite parent) {
			Composite c = (Composite) super.createDialogArea(parent);
			new Label(c, SWT.LEFT).setText(msg);
			combo = new Combo(c, SWT.NONE);
			if (options != null) {
				for (Object opt : options)
					combo.add((String) opt);
				if (options.size() > 0)
					combo.select(0);
			}
			getShell().setText(msg);
			return c;
		}

		protected void okPressed() {
			value = combo.getText();
			super.okPressed();
		}

		public String getValue() {
			return value;
		}
	}

	private class FormScriptEditor extends Composite {

		private Form scriptForm;
		private SimpleTextEditor editor;

		public FormScriptEditor(Composite parent, String title) {
			super(parent, SWT.NONE);

			setLayout(new FillLayout());

			scriptForm = tk.createForm(this);
			tk.decorateFormHeading(scriptForm);
			TDIToolBar scriptBar = new TDIToolBar(scriptForm);
			scriptBar.setText(title);

			scriptForm.getBody().setLayout(new FillLayout());
			editor = new SimpleTextEditor(scriptForm.getBody(), SWT.NONE, getEditingConfig(), true);

			// -- Focus out will cause an update to the object before the user
			// gets
			// -- a chance to prevent it.
			editor.setUpdateOnFocusOut(false);

			Action close = new Action() {
				public String getText() {
					return Messages.getString("LBL.CLOSE");
				}

				public void run() {
					stackLayout.topControl = scrolledComposite;
					updateMinSize();
					FormWidget2.this.layout();
				}
			};
			scriptBar.add(close);

			/*   Defect #13682
			MenuManager mm = new MenuManager();
			Menu menu = mm.createContextMenu(editor.getSourceViewer().getTextWidget());
			editor.getSourceViewer().getTextWidget().setMenu(menu);
			mm.add(close);
			*/

			editor.addCloseListener(new Listener() {
				public void handleEvent(Event event) {
					stackLayout.topControl = scrolledComposite;
					updateMinSize();
					FormWidget2.this.layout();
				}
			});
		}

		public SimpleTextEditor getEditor() {
			return editor;
		}
	}

	public boolean isEditorWindowDisabled() {
		return editorWindowDisabled;
	}

	public void setEditorWindowDisabled(boolean editorWindowDisabled) {
		this.editorWindowDisabled = editorWindowDisabled;
	}

	public String selectGlobalScripts(String curval) {
		TDIConfigurationFile cfg = (TDIConfigurationFile) getEditingConfig().getMetamergeConfig();
		IProject project = cfg.getProject();
		IFolder folder = project.getFolder(new Path(TDINature.RESOURCES_FOLDER + "/" + TDINature.SCRIPTS_FOLDER));
		final ArrayList<String> list = new ArrayList<String>();
		if (folder.exists()) {
			try {
				for (IResource res : folder.members()) {
					if (TDIConfigurationFile.XT_SCRIPT.equals(res.getFileExtension()))
						list.add(res.getName().substring(0, res.getName().indexOf(TDIConfigurationFile.XT_SCRIPT) - 1));
				}
			} catch (CoreException e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
				return null;
			}
		}

		CheckedListDialog dlg = new CheckedListDialog(getShell(), curval, list);
		if (dlg.open() != Window.OK)
			return null;

		StringBuilder str = new StringBuilder();
		for (Object s : dlg.selection) {
			if (str.length() > 0)
				str.append("\n");
			str.append(s.toString());
		}

		return str.toString();

	}

	public void setWaitCursor() {
		getShell().setCursor(getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
	}

	public void setNormalCursor() {
		getShell().setCursor(null);
	}

	public void setLabelColor(String paramName) {
		Label l = labels.get(paramName);
		if (l == null || isDisposed())
			return;
		String str = l.getText();
		String org = str.split("\n")[0];
		String ext = getEditingConfig().getParameterPropertySource(paramName);
		String tooltip = null;

		if (ext != null) {
			if (ext.startsWith(PROPERTY_PREFIX)) {
				org += "\n" + propertyTag;
				tooltip = ext.substring(PROPERTY_PREFIX.length(), ext.length() - 1);
			} else if (ext.startsWith(JAVASCRIPT_PREFIX)) {
				org += "\n" + javaScriptTag;
				tooltip = ext.substring(JAVASCRIPT_PREFIX.length(), ext.length() - 1);
			} else {
				org += "\n" + substitutionTag;
			}

			if (getEditingConfig().getParameter(paramName) == null) {
				l.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
			} else if (!getEditingConfig().isParameterLocal(paramName)) {
				l.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
			} else {
				l.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			}
		} else if (!getEditingConfig().isParameterLocal(paramName)) {
			l.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
		} else {
			l.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		}

		// -- no changes to label
		if (org.equals(str))
			return;

		// -- update label/tooltip and refresh layout
		l.setText(org);
		ToolTip oldTip = toolTips.get(paramName);
		if (oldTip != null)
			oldTip.deactivate();
		toolTips.put(paramName, createToolTip(tooltip, controls.get(paramName)));
		layout(true, true);
	}

	/**
	 * Set label and control for this parameter visible or invisible.
	 */
	public void setVisible(String parameterName, boolean visible) {
		setVisible(labels.get(parameterName), visible);
		setVisible(controls.get(parameterName), visible);
		setVisible(extraButton1.get(parameterName), visible);
		setVisible(extraButton2.get(parameterName), visible);
		setVisible(optionButtons.get(parameterName), visible);
		updateMinSize();
		layout(true, true);
	}

	/**
	 * Set a Control visible or not.
	 * @param c
	 * @param visible
	 */
	private void setVisible(Control c, boolean visible) {
		if (c == null)
			return;
		c.setVisible(visible);
		GridData gd = (GridData) c.getLayoutData();
		gd.exclude = !visible;
	}
	/**
	 * Loads the Function defined by the parent config.
	 *
	 * @return The initialized FunctionInterface
	 * @throws Exception
	 *             - If the Function cannot be found.
	 */
	public FunctionInterface loadFunction() throws Exception {
		FunctionInterface function = SystemFunctions.loadFunction((FunctionConfig) getEditingConfig().getParent());
		function.initialize(null);
		return function;
	}

	private static class CheckedListDialog extends Dialog {
		private CheckboxTableViewer table;
		public Object[] selection;
		private String current;
		private List<String> input;

		public CheckedListDialog(Shell parentShell, String current, List<String> input) {
			super(parentShell);
			this.current = current;
			this.input = input;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite c = (Composite) super.createDialogArea(parent);
			table = CheckboxTableViewer.newCheckList(c, SWT.FULL_SELECTION);
			GridData gd = new GridData();
			gd.widthHint = 400;
			gd.heightHint = 300;
			table.getControl().setLayoutData(gd);
			table.setContentProvider(new ArrayContentProvider());
			table.setInput(input);

			if (current != null && current.length() > 0) {
				StringTokenizer st = new StringTokenizer(current, "\r\n");
				while (st.hasMoreTokens()) {
					String s = st.nextToken();
					if (!table.setChecked(s, true)) {
						input.add(0, s);
						table.setInput(input);
						table.setChecked(s, true);
					}
				}
			}
			getShell().setText(Messages.getString("miadmin.foldernames.Scripts"));

			return c;
		}

		@Override
		protected void okPressed() {
			this.selection = table.getCheckedElements();
			super.okPressed();
		}
	};

	/**
	 * Creates a custom tooltip handler instead of the default to cope with
	 * large tooltip texts that run off the edge of the screen.
	 *
	 * @param toolTip
	 * @param control
	 */
	public static ToolTip createToolTip(final String toolTip, final Control control) {
		if (control == null || toolTip == null)
			return null;

		DefaultToolTip tt = new DefaultToolTip(control, ToolTip.RECREATE, false) {
			protected Composite createToolTipContentArea(Event event, Composite parent) {
				StyledText label = new StyledText(parent, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY) {
					public Point computeSize(int wHint, int hHint, boolean changed) {
						Point p = super.computeSize(wHint, hHint, changed);
						int w = Math.min(getDisplay().getClientArea().width, getDisplay().getBounds().width);
						if (p.x > w)
							p = super.computeSize(w - 50, SWT.DEFAULT, changed);
						return p;
					}
				};
				label.setForeground(getForegroundColor(event));
				label.setBackground(getBackgroundColor(event));
				label.setText(toolTip);
				return label;
			}
		};
		tt.setText(toolTip);
		tt.setPopupDelay(500);
		return tt;
	}

	public IWorkbenchPartSite getSite() {
		Composite parent = getParent();
		while (parent != null) {
			if (parent instanceof BaseWidget) {
				BaseWidget base = (BaseWidget) parent;
				if (base.getEditor() != null)
					return base.getEditor().getSite();
			}
			parent = parent.getParent();
		}

		try {
			IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			if (part instanceof EditorPart) {
				return part.getSite();
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		return null;
	}

	public void logdebug(String msg) {
		messageInfo(msg);
	}

	public void logerror(String msg, Throwable t) {
		messageError(msg, t);
	}

	public void logmsg(String msg) {
		messageInfo(msg);
	}

	public void logwarn(String msg) {
		messageInfo(msg);
	}

	public void messageError(String err) {
		EclipseAppender.logerror(err, new Exception(err), getShell());
	}

	public void messageError(String msg, Throwable t) {
		EclipseAppender.logerror(msg, t, getShell());
	}

	public void messageError(Throwable t) {
		EclipseAppender.logerror(t.getLocalizedMessage(), t, getShell());
	}

	public void messageInfo(String msg) {
		EclipseAppender.loginfo(msg);
	}

	/**
	 * Log a problem in the Problems view
	 * @param param Name of parameter
	 * @param t The Throwable that was caught, representing the problem to log.
	 */
	public void logProblem(String param, Throwable t) {
		// Error from javascript is often wrapped in InvocationTargetException...
		if (t instanceof InvocationTargetException)
			t = ((InvocationTargetException)t).getTargetException();
		
		StringBuilder msg = new StringBuilder();
		FormItemConfig fic = formConfig.getFormItem(param);
		if (fic != null && fic.getLabel() != null) {
			msg.append(fic.getLabel());
		} else {
			msg.append(param);
		}
		if (t!=null) {
			msg.append(": ");
			msg.append(t.toString());
		}
		Utils.logProblem(IMarker.SEVERITY_ERROR, "invalid.value", editingConfig, msg.toString());
		EclipseAppender.logerror(msg.toString(), t);
	}

	/**
	 * Opens a dialog where the input pattern determines which fields are visible. The pattern is returned with the input values
	 * replacing the pattern. The pattern must be a string with fields quoted by "<>".
	 * <pre>
	 *        openSubstForm("jdbc:db2://<hostname>:<port>/<database>");
	 * </pre>
	 * The above would result in a dialog with three fields. The field definition (e.g. labels tooltips) is retrieved from the current
	 * form object. If no FormItem is defined for a field, the field name is used as label and a text box is used to input data.
	 *
	 * @param pattern
	 * @return The pattern substituted with values from input dialog or null if use cancelled.
	 *
	 */
	public String openSubstForm(String pattern) {

		Pattern xp = Pattern.compile(".*?<([^>]*?)>");
		Matcher m = xp.matcher(pattern);
		final ArrayList<String> items = new ArrayList<String>();
		final ArrayList<String> values = new ArrayList<String>();
		while(m.find()) {
			for (int i = 1; i <= m.groupCount(); i++) {
				if(!items.contains(m.group(i))) {
					items.add(m.group(i));
					values.add(m.group(i));
				}
			}
		}

		Dialog dlg = new Dialog(getSite().getShell()) {

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite c = (Composite) super.createDialogArea(parent);
				c.setLayout(new GridLayout(2, false));

				for(int i = 0; i < items.size(); i++) {
					new Label(c, SWT.LEFT).setText(items.get(i));
					Text t = new Text(c, SWT.BORDER);
					t.setText(items.get(i));
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					final int index = i;
					t.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent e) {
							values.set(index, ((Text)e.widget).getText());
						}
					});
				}

				return c;
			}

		};

		if(dlg.open() == Window.OK) {
			String pat = pattern;
			for(int i = 0; i < items.size(); i++) {
				pat = pat.replaceAll("<" + items.get(i) + ">", values.get(i));
			}
			return pat;
		} else {
			return null;
		}

	}
	
	/**
	 * Stores the current FormConfig into the BaseConfiguration being edited.
	 * This allows for the form to be updated dynamically.
	 *  
	 */
	public void saveEmbeddedForm() {
		try {
			TDIConfigurationFile mc = new TDIConfigurationFile();
			mc.initializeConfig();
			mc.setDefaultConfigObject(formConfig.getShortName(), formConfig);
			String data = new String(mc.commitVersion(false).toByteArray(), "UTF-8");
			getEditingConfig().setStringParameter(EMBEDDED_FORM_NAME, data);
		} catch (Exception e) {
			messageError(e);
		}
	}
}
