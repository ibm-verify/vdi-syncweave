/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.io.File;
import java.util.Enumeration;

import javax.naming.Binding;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.eclipse.TDIPropertiesCE;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.PropertiesEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.PropertyContentProvider;
import com.ibm.tdi.eclipse.providers.TDIPropertiesContentProvider;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.wizards.NewPropertiesWizard;

public class ParameterSubstitutionWidget extends Canvas {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String JS_PREFIX = "{javascript"; //$NON-NLS-1$
	private static final String JS_SUFFIX = "}"; //$NON-NLS-1$
	private final static String OLD_JS_PREFIX = "{javascript<<%%\n"; //$NON-NLS-1$
	private static final String OLD_JS_SUFFIX = "\n%%\n}"; //$NON-NLS-1$
	private static final String PROP_PREFIX = "{property."; //$NON-NLS-1$
	private static final String PROP_SUFFIX = "}"; //$NON-NLS-1$

	private SimpleTextEditor text;
	private String paramName;
	private TreeViewer props;
	private StackLayout stack;
	private String title;
	private String toolTip;
	private boolean hasInheritedValue = false;

	//private Text subst;
	private TDIExpressionEditor subst;

	private Text propertyTextField;
	private ModifyListener modListener;

	private boolean multiLine;

	private ScrolledForm form;
	private FormToolkit formToolKit;
	private BaseConfiguration config;
	private String orgValue;
	
	public ParameterSubstitutionWidget(Composite parent, int style,
			BaseConfiguration editingConfig, String paramName,
			String toolTip, String label) {
		this(parent, style, editingConfig, paramName, toolTip, label, true);
	}

	public ParameterSubstitutionWidget(Composite parent, int style,
			BaseConfiguration editingConfig, String paramName,
			String toolTip, String label, boolean multiLine) {
		this(parent, style, editingConfig, paramName, toolTip, label, multiLine, null);
	}

	public ParameterSubstitutionWidget(Composite parent, int style,
			BaseConfiguration editingConfig, String paramName,
			String toolTip, String label, boolean multiLine, String orgValue) {
		super(parent, style);
		config = editingConfig;
		this.paramName = paramName;
		this.title = label;
		this.toolTip = toolTip;
		this.multiLine = multiLine;
		this.orgValue = orgValue;
		createUI();
	}

	private void createUI() {
		setLayout(new FillLayout());
		createScrolledForm(this);
		TDIToolBar bar = new TDIToolBar(form.getForm());

		String internalName = paramName; //Messages.getMessage("ParameterSubstitution.internal", paramName);
		if (title != null)
			bar.setText(title);
		else
			bar.setText(internalName);

		final Composite body = form.getBody();
		body.setLayout(new GridLayout());
		body.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		addInfo(body, internalName);

		getFormToolKit().createLabel(body, Messages.getString("ParameterSubstitutionUI.ChooseMethod"));

		Composite options = getFormToolKit().createComposite(body);
		options.setLayout(new RowLayout(SWT.HORIZONTAL));
		options.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, false));

		final Button optionProp = getFormToolKit().createButton(options, Messages.getString("ParameterSubstitutionUI.Property"), SWT.RADIO); //$NON-NLS-1$
		final Button optionJS = getFormToolKit().createButton(options, Messages.getString("Localized.advanced"), SWT.RADIO); //$NON-NLS-1$
		final Button optionSubst = getFormToolKit().createButton(options, Messages.getString("ParameterSubstitutionUI.Substitution"), SWT.RADIO); //$NON-NLS-1$
		final Button optionReset = getFormToolKit().createButton(options, Messages.getString("ParameterSubstitutionUI.Reset"), SWT.RADIO); //$NON-NLS-1$

		final Composite mainArea = new Composite(body, SWT.NONE);
		mainArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		stack = new StackLayout();
		mainArea.setLayout(stack);

		//
		// LinkCriteriaItems have expression as their value and not as parameter prop source
		//
		String pps;
		if(getEditingConfig() instanceof LinkCriteriaItem) {
			pps = getEditingConfig().getStringParameter(paramName);
		} else {
			pps = getEditingConfig().getParameterPropertySource(paramName);
		}

		String script = ""; //$NON-NLS-1$
		String property = null;
		boolean javascript = false;
		boolean substitution = false;
		if (pps != null) {
			if (isComplexSubst(pps)) {
				script = pps;
				substitution = true;
			} else if (pps.startsWith(OLD_JS_PREFIX) && pps.endsWith(OLD_JS_SUFFIX)) {
				javascript = true;
				script = pps.substring(OLD_JS_PREFIX.length(), pps.length() - OLD_JS_SUFFIX.length()).trim();
			} else if (pps.startsWith(JS_PREFIX) && pps.endsWith(JS_SUFFIX)) {
				javascript = true;
				script = pps.substring(JS_PREFIX.length(), pps.length() - JS_SUFFIX.length()).trim();
			} else if (pps.startsWith(PROP_PREFIX) && pps.endsWith(PROP_SUFFIX)) {
				property = pps.substring(PROP_PREFIX.length(), pps.length() - PROP_SUFFIX.length());
			} else {
				script = pps;
				substitution = true;
			}
		}

		//
		// JavaScript Editor
		//

		// We are not editing the script in the config, so use a dummy config to avoid overwriting a possible 'script' parameter.
		// It is still good to provide a config, so that the JavaScriptContentAssistProcessor may show available objects.
		BaseConfiguration dummy = new BaseConfigurationImpl();
		dummy.setParent(getEditingConfig());
		text = new SimpleTextEditor(mainArea, SWT.BORDER, dummy);
		if (script.length() == 0)
			text.setText("// e.g. return \"string value\"");
		else
			text.setText(script);

		//
		// Properties tree
		//
		final Composite propsArea = new Composite(mainArea, SWT.NONE);
		propsArea.setBackground(body.getBackground());
		addPropsTree(propsArea, property);
		if(property != null) {
			propertyTextField.removeModifyListener(modListener);
			propertyTextField.setText(property);
			propertyTextField.addModifyListener(modListener);
		}

		//
		// Expression editor
		//
		subst = new TDIExpressionEditor(mainArea , getEditingConfig(), multiLine);


		if (pps == null || pps.length() == 0)
			subst.setText("{work.<attrname>}\n{conn.<attrname>}\n{config.<param>}\n{config.$directory}\n");
		else
			subst.setText(pps);

		//
		// Select initial widget
		//
		if(javascript) {
			stack.topControl = text;
		} else if (substitution) {
			stack.topControl = subst;
		} else if (property != null) {
			stack.topControl = propsArea;
		} else {
			stack.topControl = null;
		}

		optionJS.setSelection(javascript);
		optionJS.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stack.topControl = text;
				mainArea.layout(true);
			}
		});

		optionProp.setSelection(property != null);
		optionProp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stack.topControl = propsArea;
				mainArea.layout(true);
			}
		});

		optionSubst.setSelection(substitution);
		optionSubst.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stack.topControl = subst;
				mainArea.layout(true);
			}
		});

		optionReset.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stack.topControl = null;
				mainArea.layout(true);
			}
		});

		if(stack.topControl == null)
			optionReset.setSelection(true);

		if(getEditingConfig().hasParameter(paramName) && hasInheritedValue) {

			bar.add(new Action() {
				@Override
				public String getText() {
					return Messages.getString("ParameterSubstitution.Revert.Value");
				}
				@Override
				public String getToolTipText() {
					return Messages.getString("action.label.22");
				}
				@Override
				public void run() {
					getEditingConfig().removeParameter(paramName);
					setEnabled(false);
					props.setSelection(StructuredSelection.EMPTY);
				}
			});
		}
	}

	/**
	 * Counts the number of starting and ending braces to try to guess if this is a complex substitution.
	 * @param s
	 * @return true if this is not a simple property or JavaScript substitution
	 */
	private boolean isComplexSubst(String s) {
		if (!(s.startsWith("{") && s.endsWith("}")))
			return true;
		int braceCounter = 1; // Count the starting brace
		int index = 1;
		while (index < s.length() && braceCounter > 0) {
			char c = s.charAt(index);
			if (c == '{')
				braceCounter++;
			if (c == '}')
				braceCounter--;
			index++;
		}
		return index < s.length() || braceCounter != 0;
	}

	/**
	 * Returns the property selection or JavaScript script formatted as a TDI
	 * expression
	 *
	 * @return formatted expression
	 */
	public String getText() {
		if (stack.topControl == text)
			return JS_PREFIX + " " + text.getText().trim() + JS_SUFFIX;
		else if (stack.topControl == subst)
			return subst.getText();
		else if (stack.topControl == null)
			return null;

		// -- property
		String str = propertyTextField.getText();
		if(!str.startsWith(PROP_PREFIX))
			str = PROP_PREFIX + str;
		if(!str.endsWith(PROP_SUFFIX))
			str += PROP_SUFFIX;

		return str;
	}

	private void addInfo(Composite body, String internalName) {
		Composite head = new Composite(form.getForm().getHead(), SWT.NONE);
		head.setLayout(new GridLayout(2,false));

		if (toolTip != null) {
			new Label(head, SWT.LEFT).setText(Messages.getString("ParameterSubstitution.description"));
			Label l = new Label(head, SWT.LEAD|SWT.WRAP);
			l.setText(toolTip);
			l.pack();
			// -- Silly SWT doesn't wrap so we set the tooltip to display text that runs off the screen
			// -- And to top it off, SWT tooltips doesn't wrap either so we use custom tooltip code.
			FormWidget2.createToolTip(toolTip, l);
		}

		// -- Original value
		String orgvalue = this.orgValue;
		if(orgValue == null && getEditingConfig().getInheritsFrom() != null)
			orgvalue = getEditingConfig().getInheritsFrom().getStringParameter(paramName);

		if (orgvalue != null) {
			new Label(head, SWT.LEFT).setText(Messages.getString("ParameterSubstitution.original"));
			Label value = new Label(head, SWT.LEFT);
			// -- this one can be long as well
			FormWidget2.createToolTip(orgvalue, value);
			if (orgvalue.indexOf('\n')>0) {
				orgvalue = orgvalue.substring(0,orgvalue.indexOf('\n')) + "...";
			}
			value.setText(orgvalue);
			hasInheritedValue = true;
		}

		// -- Internal name
		new Label(head, SWT.LEFT).setText(Messages.getString("ParameterSubstitution.internal"));
		new Label(head, SWT.LEFT|SWT.WRAP).setText(internalName);

		form.setHeadClient(head);
	}

	//
	// Properties tree
	//
	private void addPropsTree(Composite parent, String property) {
		// No margins
		GridLayout gl = new GridLayout(1,false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		parent.setLayout(gl);

		// -- filter out based on current text contents of property name
		final ViewerFilter filter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if(propertyTextField == null)
					return true;

				String str = propertyTextField.getText();
				if(str.equals(""))
					return true;

				int i = str.indexOf(':');
				if (i > 0)
					str = str.substring(i+1);
				if(element instanceof BaseConfiguration) {
					return ((BaseConfiguration)element).getShortName().toLowerCase().contains(str.toLowerCase());
				}
				return true;
			}
		};

		final Job refreshFilter = new Job("") {
			protected IStatus run(IProgressMonitor monitor) {
				getDisplay().syncExec(new Runnable() {
					public void run() {
						props.refresh();
					}
				});
				return Status.OK_STATUS;
			}
		};

		// -- schedule refresh after 50 msec
		modListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				refreshFilter.cancel();
				refreshFilter.schedule(500);
			}
		};

		props = new TreeViewer(parent, SWT.SINGLE|SWT.BORDER);
		props.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		props.setContentProvider(new TDIPropertiesContentProvider());
		props.setLabelProvider(new TLP(Utils.getProjectFor(getEditingConfig())));
		props.setInput(getEditingConfig().getMetamergeConfig());
		props.addFilter(filter);

		props.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) props.getSelection();
				if (sel.isEmpty())
					return;
				String str = "";
				Object obj = sel.getFirstElement();
				if (obj instanceof BaseConfiguration) {
					BaseConfiguration cfg = (BaseConfiguration) obj;
					String prop = cfg.getShortName();

					File file = (File)cfg.getParameter(PropertiesEditor.PROPERTY_FILE_OBJECT);
					if(file != null) {
						if(file.getName().equals("global.properties"))
							str = PropertyManager.STDCOLL_GLOBAL + ":" + prop;
						else if(file.getName().equals("solution.properties"))
							str = PropertyManager.STDCOLL_SOLUTION + ":" + prop;
						else
							str = prop;
					} else {
						IProject base = Utils.getProjectFor(getEditingConfig());
						try {
							// -- [store]:property-name[@RefMC]
							IFile store = (IFile) ((TDIPropertiesContentProvider)props.getContentProvider()).getParent(cfg);
							str = store.getName().substring(0, store.getName().lastIndexOf(".")) + ":" + prop;
							String extns = getLocalNamespaceFor(Utils.getProjectMC(base), cfg);
							if(extns != null) {
								str += "@" + extns;
							}
						} catch (Exception e) {
							//Ignore stores we cannot find
							SystemFunctions.doNothing();
						}
					}
				}
				if(propertyTextField != null) {
					// -- remove filter so we don't hide everything ... only when user changes it manually do we filter
					propertyTextField.removeModifyListener(modListener);
					propertyTextField.setText(str);
					propertyTextField.addModifyListener(modListener);
				}
			}

		});

		TreeColumn tc = new TreeColumn(props.getTree(), SWT.LEFT);
		tc.setText(Messages.getString("PropertyStoreUI.Localized.Name")); //$NON-NLS-1$
		tc.setWidth(300);

		tc = new TreeColumn(props.getTree(), SWT.LEFT);
		tc.setText(Messages.getString("PropertyStoreUI.Localized.Value")); //$NON-NLS-1$
		tc.setWidth(250);

		props.getTree().setHeaderVisible(true);
		props.expandAll();

		BaseConfiguration sel = ((TDIPropertiesContentProvider) props.getContentProvider()).getChildForProperty(property);
		if (sel != null) {
			props.setSelection(new StructuredSelection(sel), true);
		}

		//
		// -- Text field showing the selected property expression (fully qualified)
		//
		Composite propComp = new Composite(parent, SWT.NONE);
		Utils.setGridLayout(propComp, 2, false);
		propComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		propComp.setBackground(parent.getBackground());
		propComp.setForeground(parent.getForeground());

		Label propLabel = new Label(propComp, SWT.LEFT);
		propLabel.setText(Messages.getString("PropertyStoreUI.Localized.Name") + ": ");
		propLabel.setBackground(parent.getBackground());
		propLabel.setForeground(parent.getForeground());

		propertyTextField = new Text(propComp, SWT.BORDER|SWT.SINGLE);
		propertyTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		propertyTextField.addModifyListener(modListener);

		//
		// -- Add property button
		//

		Button addButton = getFormToolKit().createButton(parent, Messages.getString("outline.label.0"), SWT.PUSH);//$NON-NLS-1$
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					if (TDIPropertiesCE.getPropertyStores(getEditingConfig()).size() == 0) {
						NewPropertiesWizard wiz = new NewPropertiesWizard();
						StructuredSelection sel = new StructuredSelection(TDIPropertiesCE.getPropertyStoreFolder(getEditingConfig()));
						wiz.init(PlatformUI.getWorkbench(), sel);
						WizardDialog dlg = new WizardDialog(getShell(), wiz);
						if (dlg.open() != Window.OK)
							return;
					}
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}

				AddPropertyDialog dlg = new AddPropertyDialog(getShell());
				dlg.selectStoreName((IStructuredSelection) props.getSelection());
				if (dlg.open() != Window.OK)
					return;

				try {
					String storeName = dlg.getStore();
					TDIConfigurationFile file = TDIPropertiesCE.loadPropertyStore(getEditingConfig(), storeName);
					ContainerConfig data = (ContainerConfig) ((ContainerConfig) file.getDefaultConfigObject()).getConfig("Data"); //$NON-NLS-1$
					BaseConfiguration bc = new BaseConfigurationImpl();
					bc.init();
					bc.setName(dlg.getPropertyName());
					bc.setStringParameter(PropertiesEditor.PROPERTY, "true");
					bc.setStringParameter(PropertiesEditor.LOCAL_VALUE, dlg.getPropertyValue());
					data.addConfig(bc);
					file.commitChanges(null);
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}

				props.setInput(getEditingConfig().getMetamergeConfig());
				props.expandAll();

				BaseConfiguration sel = ((TDIPropertiesContentProvider) props.getContentProvider()).getChildForProperty(dlg.getPropertyName());
				if (sel != null) {
					props.setSelection(new StructuredSelection(sel), true);
				}
			}
		});
	}

	private static class TLP extends LabelProvider implements ITableLabelProvider {

		private IProject base;

		public TLP(IProject base) {
			this.base = base;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {

			String val = null;
			if (element instanceof BaseConfiguration) {
				BaseConfiguration b = (BaseConfiguration) element;
				if (columnIndex == 0) {
					return PropertyContentProvider.getName(b);
				} else {
					val = b.getStringParameter(PropertiesEditor.LOCAL_VALUE);
					if (b.getBooleanParameter(PropertiesEditor.LOCAL_PROTECT, false))
						val = "********";
					if(val == null)
						val = "";
				}
			} else if (element instanceof IFile) {
				IFile file = (IFile) element;
				val = file.getName();
				if(val.endsWith(".tdiproperties"))
					val = val.substring(0, val.lastIndexOf("."));
				if(!file.getProject().equals(base)) {
					try {
						String str = getLocalNamespaceFor(Utils.getProjectMC(base), file);
						val += " (" + str + ")";
					} catch (Exception e) {
						val += " (" + e.toString() + ")";
					}
				}
			} else if (element instanceof File) {
				File file = (File) element;
				val = file.getName();
				if("solution.properties".equals(val))
					val = PropertyManager.STDCOLL_SOLUTION;
				else if("global.properties".equals(val))
					val = PropertyManager.STDCOLL_GLOBAL;
			}

			return val;
		}

		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}

	}

	private class AddPropertyDialog extends Dialog {

		private String propertyName;
		private String propertyValue;
		private String storeName;

		public AddPropertyDialog(Shell parentShell) {
			super(parentShell);
		}

		public void selectStoreName(IStructuredSelection selection) {
			if (selection == null || selection.isEmpty())
				return;
			Object obj = selection.getFirstElement();
			if (obj instanceof BaseConfiguration) {
				obj = ((TDIPropertiesContentProvider)props.getContentProvider()).getParent(obj);
			}
			if (obj instanceof IFile) {
				storeName = ((IFile) obj).getName();
			}		
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite c = (Composite) super.createDialogArea(parent);
			c.setLayout(new GridLayout(2, false));

			Label label;

			//
			// -- Property store
			//
			label = new Label(c, SWT.LEFT);
			label.setText(Messages.getString("ParameterSubstitutionWidget.StoreName")); //$NON-NLS-1$

			Combo store = new Combo(c, SWT.DROP_DOWN | SWT.READ_ONLY);
			store.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			store.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					storeName = ((Combo) e.widget).getItem(((Combo) e.widget).getSelectionIndex());
				}
			});
			try {
				for (IFile file : TDIPropertiesCE.getPropertyStores(getEditingConfig())) {
					store.add(file.getName());
				}
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
			int index = (storeName != null ? store.indexOf(storeName) : 0);
			if (index < 0)
				index = 0;
			store.select(index);
			storeName = store.getItem(index);

			//
			// -- Name
			//
			label = new Label(c, SWT.LEFT);
			label.setText(Messages.getString("PropertyStoreUI.Localized.Name")); //$NON-NLS-1$

			Text name = new Text(c, SWT.BORDER);
			name.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			name.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					propertyName = ((Text) e.widget).getText();
					updateOKButton();
				}
			});

			//
			// -- Value
			//
			label = new Label(c, SWT.LEFT);
			label.setText(Messages.getString("PropertyStoreUI.Localized.Value")); //$NON-NLS-1$

			Text value = new Text(c, SWT.BORDER);
			value.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			value.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					propertyValue = ((Text) e.widget).getText();
					updateOKButton();
				}
			});

			getShell().setText(Messages.getString("outline.label.0"));

			return c;
		}

		protected void updateOKButton() {
			boolean enabled = getPropertyName() != null && getPropertyName().length() > 0 && getPropertyValue() != null
					&& getPropertyValue().length() > 0;
			getButton(IDialogConstants.OK_ID).setEnabled(enabled);
		}

		public String getStore() {
			return storeName;
		}

		public String getPropertyName() {
			return propertyName;
		}

		public String getPropertyValue() {
			return propertyValue;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			updateOKButton();
		}
	}

	public static String openPSDialog(Shell shell, BaseConfiguration config, String parameter) {
		return openPSDialog(shell, config, parameter, null, null);
	}

	public static String openPSDialog(Shell shell, BaseConfiguration config, String parameter,
			String toolTip, String title) {
		return openPSDialog(shell, config, parameter, toolTip, title, true);
	}

	public static String openPSDialog(Shell shell, BaseConfiguration config, String parameter,
			String toolTip, String title, boolean multiLineValue) {
		return openPSDialog(shell, config, parameter, toolTip, title, multiLineValue, null);
	}

	public static String openPSDialog(Shell shell, BaseConfiguration config, String parameter,
			String toolTip, String title, boolean multiLineValue, String orgValue) {

		PSDialog dlg = new PSDialog(shell, config, parameter, toolTip, title, multiLineValue, orgValue);
		if(dlg.open() != Window.OK)
			return null;

		String s = dlg.getValue();
		if (s == null && dlg.wasReset() && parameter != null)
			config.removeParameter(parameter);
		else
			return s;

		return null;
	}

	/**
	 * This is the parameter substitution dialog.
	 */
	private static class PSDialog extends Dialog {
		private ParameterSubstitutionWidget ps;
		private String value;
		private String param;
		private String title;
		private String toolTip;
		private BaseConfiguration config;
		private boolean wasReset;
		private boolean multiLine;
		private String orgValue;
		
		public PSDialog(Shell shell, BaseConfiguration config, String param, String toolTip, String title,
				boolean multiLine, String orgValue) {
			super(shell);
			this.param = param;
			this.title = title;
			this.toolTip = toolTip;
			this.config = config;
			this.multiLine = multiLine;
			this.orgValue = orgValue;
		}

		public boolean wasReset() {
			return wasReset;
		}

		protected Point getInitialSize() {
			return new Point(650, 400);
		}

		protected Control createDialogArea(Composite parent) {
			Composite c = (Composite) super.createDialogArea(parent);
			ps = new ParameterSubstitutionWidget(c, SWT.NONE, config, param, toolTip, title, multiLine, orgValue);
			ps.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			getShell().setText(Messages.getString("AddCaseUI.PSE.label") + " - " + (title != null ? title : param)); //$NON-NLS-1$
			return c;
		}

		protected void okPressed() {
			value = ps.getText();
			wasReset = ps.wasReset();
			super.okPressed();
		}

		public String getValue() {
			return value;
		}

		@Override
		protected int getShellStyle() {
			return super.getShellStyle() | SWT.RESIZE;
		}

	}

	public boolean wasReset() {
		return stack.topControl == null;
	}

	public static String getLocalNamespaceFor(MetamergeConfig projectMC, Object cfgOrFile) throws Exception {
		BaseConfiguration cfg = (BaseConfiguration) (cfgOrFile instanceof BaseConfiguration ? cfgOrFile : null);
		IFile file = (IFile) (cfgOrFile instanceof IFile ? cfgOrFile : null);

		// -- Externally defined config file
		if(cfg != null) {
			Object extns = MetamergeConfigFactory.getLocalNamespaceFor(projectMC, cfg);
			if(extns != null)
				return extns.toString();
		}

		String project = null;
		if(cfg != null && cfg.getMetamergeConfig() instanceof TDIConfigurationFile)
			project = ((TDIConfigurationFile)cfg.getMetamergeConfig()).getProject().getName();
		else if (file != null)
			project = file.getProject().getName();
		else
			return null;


		// -- Check reference to local projects
		MetamergeFolder refs = (MetamergeFolder) projectMC.lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
		if(refs == null)
			return null;

		for(Enumeration<Binding> l = refs.list(); l.hasMoreElements(); ) {
			NamespaceConfig nsc = (NamespaceConfig) l.nextElement().getObject();
			String url = nsc.getURL();
			if(url != null && url.equals(project))
				return nsc.getShortName();
		}
		return null;
	}

	/**
	 * Crates the form toolkit and a form with this as parent.
	 *
	 * @return Newly created form
	 */
	private void createScrolledForm(Composite parent) {
		formToolKit = new FormToolkit(getDisplay());
		form = formToolKit.createScrolledForm(parent);
		formToolKit.decorateFormHeading(form.getForm());
	}

	private FormToolkit getFormToolKit() {
		return formToolKit;
	}

	private BaseConfiguration getEditingConfig() {
		return config;
	}
}
