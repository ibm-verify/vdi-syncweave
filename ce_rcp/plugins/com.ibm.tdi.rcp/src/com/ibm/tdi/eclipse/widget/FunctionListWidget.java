/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.FormConfigImpl;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.function.UserFunctions;
import com.ibm.icu.util.StringTokenizer;
import com.ibm.jscript.ASTTree.ASTAssign;
import com.ibm.jscript.ASTTree.ASTFunction;
import com.ibm.jscript.ASTTree.ASTIdentifier;
import com.ibm.jscript.ASTTree.ASTVariableDecl;
import com.ibm.jscript.ASTTree.DefaultNodeVisitor;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.preferences.JavaScriptPreferencePage;
import com.ibm.tdi.eclipse.preferences.PreferenceConstants;
import com.ibm.tdi.eclipse.providers.WorkEntryAttributesProvider;
import com.ibm.tdi.eclipse.text.JavaDocReader;
import com.ibm.tdi.eclipse.text.JavaScriptContentAssistProcessor;
import com.ibm.tdi.eclipse.text.JavaScriptDocParser;
import com.ibm.tdi.eclipse.text.JavaScriptDocParser.ParameterDescriptor;
import com.ibm.tdi.eclipse.text.JavaScriptDocParser.ScriptFunctionInfo;

public class FunctionListWidget extends BaseWidget implements Listener, IPropertyChangeListener {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	protected JavaScriptContentAssistProcessor cap;
	private TreeViewer tree;
	private SimpleTextEditor ted;
	private HashMap<String, NamedList> categories = new HashMap<String, NamedList>();

	private Action openSource;

	public FunctionListWidget(Composite parent, SimpleTextEditor simpleTextEditor) {

		super(parent, 0);

		this.ted = simpleTextEditor;

		setLayout(new FillLayout());

		Form frm = createForm(this, null);
		frm.setToolBarVerticalAlignment(SWT.BOTTOM);
		final Text search = new Text(frm.getHead(), SWT.SEARCH);
		frm.setHeadClient(search);
		search.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				tree.refresh();
				tree.expandAll();
			}
		});
		
		frm.getToolBarManager().add(new Action() {
			public ImageDescriptor getImageDescriptor() {
				return Activator.getImageDescriptor("icons/Settings_16.gif");
			}

			public String getToolTipText() {
				return Messages.getString("ConnectorFlowWidget_configure");
			}

			public void run() {
				openConfigurationDialog();
			}
		});

		frm.getToolBarManager().update(true);
		frm.getBody().setLayout(new FillLayout());

		tree = new TreeViewer(frm.getBody(), SWT.V_SCROLL | SWT.MULTI);
		tree.setContentProvider(new ContentProvider());
		tree.setLabelProvider(new FunctionLabelProvider());
		ColumnViewerToolTipSupport.enableFor(tree, ToolTip.NO_RECREATE);
		tree.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (obj instanceof Method) {
					Method m = (Method) obj;
					try {
						StringBuffer message = JavaDocReader.getJavaDocs(UserFunctions.class, m);
						if (message != null)
							MessageDialog.openConfirm(getShell(), Messages.getString("ConnectorWidget3.17"), message.toString());
					} catch (Exception e) {
						SystemFunctions.doNothing();
					}

				} else if (obj instanceof ScriptFunctionInfo) {
					insertFunctionInfoObject((ScriptFunctionInfo) obj);

				} else if (obj instanceof String) {
					gotoLocalSymbol(obj.toString());
//					String str = getStringForSelection();
//					if (str.length() > 0) {
//						Point p = ted.getSourceViewer().getSelectedRange();
//						try {
//							ted.getSourceViewer().getDocument().replace(p.x, p.y, str);
//							ted.getSourceViewer().getTextWidget().setCaretOffset(p.x + str.length());
//							ted.getSourceViewer().getControl().setFocus();
//						} catch (BadLocationException e) {
//							SystemFunctions.doNothing();
//						}
//					}
				}
			}
		});
		
		
		//
		// Add a filter that only shows items matching the search box
		//
		tree.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if(element instanceof NamedList)
					return true;
				
				String str = search.getText();
				if(str == null || str.equals(""))
					return true;
				
				String lbl = ((FunctionLabelProvider)tree.getLabelProvider()).getText(element);
				if(lbl == null)
					return false;
				return lbl.toLowerCase().indexOf(str.toLowerCase()) != -1;
			}
		});
		addDragSupport(tree);

		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);

		addAsSelectionProvider(tree);

		MenuManager mm = new MenuManager();
		tree.getControl().setMenu(mm.createContextMenu(tree.getControl()));
		openSource = new Action() {
			public String getText() {
				return Messages.getString("FunctionWidget.open.script");
			}

			@Override
			public boolean isEnabled() {
				Object sel = ((IStructuredSelection) tree.getSelection()).getFirstElement();
				if (sel instanceof ScriptFunctionInfo) {
					return Utils.getProjectFor(((ScriptFunctionInfo) sel).getSource()) != null;
				}
				return false;
			}

			public void run() {
				Object sel = ((IStructuredSelection) tree.getSelection()).getFirstElement();
				if (sel instanceof ScriptFunctionInfo) {
					Utils.openEditorFor(((ScriptFunctionInfo) sel).source);
				}
			}
		};
		mm.add(openSource);
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				openSource.setEnabled(openSource.isEnabled());
			}
		});

	}

	protected void gotoLocalSymbol(String str) {
		int position = getPositionForLocalSymbol(str);
		if(position != -1) {
			int length = str.indexOf('(');
			if (length < 0)
				length = str.length();
			ted.getSourceViewer().setSelection(new TextSelection(position, length), true);
			ted.getSourceViewer().getControl().setFocus();
		} else {
			getDisplay().beep();
		}
	}

	protected void openConfigurationDialog() {
		Dialog dlg = new Dialog(getShell()) {
			private JavaScriptPreferencePage page = null;
			private ScriptLibraryControl libControl;

			protected Control createDialogArea(Composite parent) {
				Composite c = (Composite) super.createDialogArea(parent);
				TabFolder tabs = new TabFolder(c, SWT.TOP);

				TabItem item;
				if(Utils.getParentConfig(ted.getEditingConfig(), AssemblyLineConfig.class) != null) {
					item = new TabItem(tabs, SWT.LEFT);
					item.setText(Messages.getString("FunctionWidget.list.library"));
					libControl = new ScriptLibraryControl(tabs, ted.getEditingConfig());
					item.setControl(libControl);
				}

				item = new TabItem(tabs, SWT.LEFT);
				item.setText(Messages.getString("FunctionWidget.list.categories"));
				page = new JavaScriptPreferencePage(false);
				page.createControl(tabs);
				item.setControl(page.getControl());

				tabs.setLayoutData(new GridData(GridData.FILL_BOTH));
				return c;
			}

			@Override
			protected void okPressed() {
				if (libControl != null)
					libControl.updateALSettings();
				if (page != null)
					page.performOk();
				if(cap != null && cap.getScriptContext() != null) {
					try {
						cap.getScriptContext().addScriptFunctions();
						refreshCategories();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				super.okPressed();
			}

			@Override
			protected void configureShell(Shell newShell) {
				newShell.setText(Messages.getString("ConnectorFlowWidget_configure"));
				super.configureShell(newShell);
			}

			@Override
			protected int getShellStyle() {
				return super.getShellStyle() | SWT.RESIZE;
			}
		};
		dlg.open();

	}

	/**
	 * Called when a property changes in the prefs store
	 * 
	 * @param event
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if(isDisposed()) {
			Activator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		} else if (event.getProperty().startsWith(PreferenceConstants.P_JS_FUNCTION_CATEGORIES)) {
			refreshCategories();
		}
	}
	
	@Override
	public void dispose() {
		if (this.cap != null && this.cap.getScriptContext() != null)
			this.cap.getScriptContext().removeListener(this);
		super.dispose();
	}

	private void addDragSupport(TreeViewer tree) {
		DragSourceListener dsl = new DragSourceListener() {
			public void dragStart(DragSourceEvent event) {
				String str = getStringForSelection();
				event.doit = str != null && str.length() > 0;
			}

			public void dragSetData(DragSourceEvent event) {
				String str = getStringForSelection();
				event.data = str;
				event.doit = str != null && str.length() > 0;
			}

			public void dragFinished(DragSourceEvent event) {
			}
		};
		tree.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { TextTransfer.getInstance() }, dsl);
	}

	protected String getStringForSelection() {
		IStructuredSelection sel = (IStructuredSelection) tree.getSelection();
		StringBuffer buf = new StringBuffer();
		for (Object obj : sel.toList()) {
			if (obj instanceof ScriptFunctionInfo) {
				ScriptFunctionInfo sfi = (ScriptFunctionInfo) obj;
				if (buf.length() > 0)
					buf.append("\n");
				if(sfi.getDescriptor() != null && sfi.getDescriptor().isCodeSnippet())
					return sfi.getDescriptor().getCodeSnippet();
				else
					buf.append(sfi.toJavaScript());
			} else if (obj instanceof String) {
				if (buf.length() > 0)
					buf.append("\n");
				buf.append(obj.toString());
			}
		}
		return buf.toString();
	}

	protected void insertFunctionInfoObject(final ScriptFunctionInfo sfi) {
		final FormConfig fc = new FormConfigImpl();
		final BaseConfiguration params = new BaseConfigurationImpl();
		fc.setStringParameter("title", sfi.getDescriptor().description);

		try {
			fc.init();
			params.init();
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}

		fc.setBooleanParameter("noParameterSubstitutionEditor", true);

		StyledText text = ted.getSourceViewer().getTextWidget();
		boolean hasparams = false;

		for (ParameterDescriptor pd : sfi.getDescriptor().getParameters()) {
			hasparams = true;
			FormItemConfig item = fc.newFormItem(pd.name);
			item.setLabel(pd.description);

			if ("boolean".equalsIgnoreCase(pd.getJsType()))
				item.setSyntax("boolean");
			else
				item.setSyntax("dropedit");

			final List<String> values = item.getValues();
			try {
				for (String str : getLocalScriptVariablesAndWorkAttributes()) {
					if (!values.contains(str)) {
						values.add(str);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String snippet;

		if(hasparams) {
			Dialog dlg = new Dialog(getShell()) {
				protected Control createDialogArea(Composite parent) {
					Composite c = (Composite) super.createDialogArea(parent);
					try {
						new FormWidget2(c, params, fc).setLayoutData(new GridData(GridData.FILL_BOTH));
					} catch (Exception e) {
						e.printStackTrace();
					}
					getShell().setText(Messages.getString("FunctionListWidget.Parameters"));
					return c;
				}

				@Override
				protected int getShellStyle() {
					return super.getShellStyle() | SWT.RESIZE;
				}
			};

			if (dlg.open() != Window.OK)
				return;

			if(sfi.isCodeSnippet()) {
				snippet = sfi.createSnippet(params);
				for (ParameterDescriptor pd : sfi.getDescriptor().getParameters()) {
					String val = params.getStringParameter(pd.name);
					if (val == null || val.length() == 0)
						val = "\"\"";
					snippet = snippet.replaceAll("\\b"+pd.name+"\\b", val);
				}
			} else {
				StringBuilder buf = new StringBuilder(sfi.name);
				buf.append("(");
				boolean first = true;
				for (ParameterDescriptor pd : sfi.getDescriptor().getParameters()) {
					String val = params.getStringParameter(pd.name);
					if (first)
						first = false;
					else
						buf.append(", ");
					if (val == null || val.length() == 0)
						buf.append("\"\"");
					else
						buf.append(val);
				}
				buf.append(")\n");
				snippet = buf.toString();
			}

		} else if(sfi.isCodeSnippet()) {
			snippet = sfi.createSnippet(params);
		} else {
			snippet = sfi.toJavaScript();
		}
		
		text.insert(snippet);
		text.setSelection(text.getSelection().x + snippet.length());
		text.setFocus();
	}

	protected int getPositionForLocalSymbol(String name) {
		
		final String string = (name.indexOf("(") != -1 ? name.substring(0, name.indexOf("(")) : name);
		
		if(cap != null) {
			try {
				final Point loc = new Point(-1, -1);
				cap.getJavaScriptMainNode().visitAllNodes(new DefaultNodeVisitor() {

					@Override
					public Object visitAssign(ASTAssign x, Object param) {
						if (loc.x == -1 && x.getLeftNode() instanceof ASTIdentifier) {
							ASTIdentifier y = (ASTIdentifier) x.getLeftNode();
							if (string.equals(y.getIdentifierName())) {
								loc.x = y.getBeginLine();
								loc.y = y.getBeginCol();
							}
						}
						return super.visitAssign(x, param);
					}

					@Override
					public Object visitFunction(ASTFunction x, Object param) {
						if(loc.x == -1 && string.equals(x.getName())) {
							loc.x = x.getBeginLine();
							loc.y = x.getBeginCol();
						}
						return super.visitFunction(x, param);
					}

					@Override
					public Object visitVariableDecl(ASTVariableDecl x, Object param) {
						for (int i = 0; i < x.getEntryCount(); i++) {
							if(loc.x == -1 && string.equals(x.getEntryAt(i).getName())) {
								loc.x = x.getBeginLine();
								loc.y = x.getBeginCol();
							}
						}
						return super.visitVariableDecl(x, param);
					}
				});

				if (loc.x == -1)
					return -1;

				IDocument doc = ted.getDocument();
				int position = doc.getLineOffset(loc.x - 1);
				position += loc.y - 1;
				
				return position;
				
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
		}
		return -1;
	}

	protected List<String> getLocalScriptVariables() {
		final List<String> values = new ArrayList<String>();
		cap.getJavaScriptMainNode().visitAllNodes(new DefaultNodeVisitor() {
			@Override
			public Object visitAssign(ASTAssign x, Object param) {
				if (x.getLeftNode() instanceof ASTIdentifier) {
					ASTIdentifier y = (ASTIdentifier) x.getLeftNode();
					if (!values.contains(y.getIdentifierName())) {
						values.add(y.getIdentifierName());
					}
				}
				return super.visitAssign(x, param);
			}

			@Override
			public Object visitVariableDecl(ASTVariableDecl x, Object param) {
				for (int i = 0; i < x.getEntryCount(); i++) {
					values.add(x.getEntryAt(i).getName());
				}
				return super.visitVariableDecl(x, param);
			}

		});
		Collections.sort(values);
		return values;
	}
	
	protected List<String> getLocalScriptVariablesAndWorkAttributes() {
		final List<String> values = getLocalScriptVariables();
		BaseConfiguration bc = ted.getEditingConfig();
		AssemblyLineConfig alc = Utils.getParentConfig(bc, AssemblyLineConfig.class);
		if (alc != null) {
			String name = null;
			while (bc != null) {
				if (bc instanceof ScriptConfig || bc instanceof ConnectorConfig || bc instanceof LoopConfig) {
					name = bc.getShortName();
					break;
				}
				bc = bc.getParent();
			}
			WorkEntryAttributesProvider wp = new WorkEntryAttributesProvider(name);
			wp.inputChanged(null, null, alc);
			for(String attr : wp.getSortedAttributes()) {
				values.add(Utils.getScript("work", attr));
			}		
		}
		return values;
	}

	protected List<String> getLocalFunctions() {
		final List<String> values = new ArrayList<String>();
		cap.getJavaScriptMainNode().visitAllNodes(new DefaultNodeVisitor() {

			@Override
			public Object visitFunction(ASTFunction x, Object param) {
				StringBuffer buf = new StringBuffer();
				buf.append(x.getName() + "(");
				for (int i = 0; i < x.getParameterCount(); i++) {
					if (i > 0)
						buf.append(", ");
					buf.append(x.getParameterAt(i).getName());
				}
				buf.append(")");
				values.add(buf.toString());
				return super.visitFunction(x, param);
			}

		});
		Collections.sort(values);
		return values;
	}

	/**
	 * Callback from JavaScriptDocParser that contents have changed.
	 * 
	 * @param event
	 */
	public void handleEvent(Event event) {
		if (isDisposed()) {
			this.cap.getScriptContext().removeListener(this);
			return;
		}
		computeLists(cap.getScriptContext());
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				tree.setInput(categories);
				tree.expandAll();
			}
		});
	}

	public void setContentAssistProcessor(JavaScriptContentAssistProcessor cap) {
		if (isDisposed())
			return;

		if (this.cap != null && this.cap.getScriptContext() != null)
			this.cap.getScriptContext().removeListener(this);

		if (this.cap == null || this.cap != cap) {
			this.cap = cap;
			computeLists(cap.getScriptContext());
			tree.setInput(categories);
			tree.expandAll();
		} else {
			if (computeLocalVarsFunctions())
				tree.refresh(categories.get(JavaScriptPreferencePage.CAT_LOCAL_VARS_FUNCTIONS), true);
		}

		if (this.cap != null && this.cap.getScriptContext() != null)
			this.cap.getScriptContext().addListener(this);
	}

	private boolean computeLocalVarsFunctions() {
		if (cap == null)
			return false;

		if (JavaScriptPreferencePage.isFunctionWidgetCategoryEnabled(JavaScriptPreferencePage.CAT_LOCAL_VARS_FUNCTIONS)) {
			NamedList list = categories.get(JavaScriptPreferencePage.CAT_LOCAL_VARS_FUNCTIONS);
			if (list == null) {
				list = new NamedList(JavaScriptPreferencePage.CAT_LOCAL_VARS_FUNCTIONS);
				categories.put(JavaScriptPreferencePage.CAT_LOCAL_VARS_FUNCTIONS, list);
			} else {
				list.clear();
			}

			if(cap.getJavaScriptMainNode() != null) {
				for (String str : getLocalScriptVariables()) {
					list.add(str);
				}
				for (String str : getLocalFunctions()) {
					list.add(str);
				}
			}
			return true;
		}

		return false;
	}

	private boolean computeGlobalVarsFunctions() {
		if (cap == null)
			return false;

		if (JavaScriptPreferencePage.isFunctionWidgetCategoryEnabled(JavaScriptPreferencePage.CAT_GLOBAL_VARS)) {
			NamedList list = categories.get(JavaScriptPreferencePage.CAT_GLOBAL_VARS);
			if (list == null) {
				list = new NamedList(JavaScriptPreferencePage.CAT_GLOBAL_VARS);
				categories.put(JavaScriptPreferencePage.CAT_GLOBAL_VARS, list);
			} else {
				list.clear();
			}

			for (String str : cap.getTopLevelObjects()) {
				list.add(str);
			}
			return true;
		}

		return false;
	}
	
	protected void refreshCategories() {
		if (cap == null)
			return;
		computeLists(cap.getScriptContext());
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				tree.setInput(categories);
				tree.refresh();
				tree.expandAll();
			}
		});
	}

	private void computeLists(JavaScriptDocParser docs) {
		for (NamedList list : categories.values()) {
			list.clear();
		}

		if (docs != null) {
			for (ScriptFunctionInfo sfi : docs.getAllFunctions()) {
				String cat = JavaScriptDocParser.DEFAULT_SCRIPT_CATEGORY;
				if(sfi.getDescriptor() != null) {
					cat = sfi.getDescriptor().getCategory();
					if(sfi.getDescriptor().isCodeSnippet())
						cat = "Code Snippets";
				}
				
				// Only add the category if it is enabled
				if (JavaScriptPreferencePage.isFunctionWidgetCategoryEnabled(cat)) {
					NamedList list = categories.get(cat);
					if (list == null) {
						list = new NamedList(cat);
						categories.put(cat, list);
					}
					list.add(sfi);
				}
			}
		}

		computeGlobalVarsFunctions();
		computeLocalVarsFunctions();
	}

	/**
	 * Class with a name and List object used in the tree view to represent a
	 * category with a list of function info objects.
	 * 
	 */
	private static class NamedList {
		String name;
		List<Object> list = new ArrayList<Object>();

		public NamedList(String name) {
			this.name = name;
		}

		public void add(Object sfi) {
			if (!list.contains(sfi))
				list.add(sfi);
		}

		public String getName() {
			return name;
		}

		public void clear() {
			list.clear();
		}

		public Object[] getItems() {
			return list.toArray();
		}
	}

	/**
	 * Label provider for the function list widget tree
	 * 
	 */
	private class FunctionLabelProvider extends CellLabelProvider {
		public Image getImage(Object element) {
			if (element instanceof String) {
				if (element.toString().indexOf("(") != -1) {
					return Activator.getImage("Script_16");
				}
				return Activator.getImage("localvariable_obj");

			} else if (element instanceof ScriptFunctionInfo) {
				return Activator.getImage("Script_16");

			}

			return null;
		}

		public String getText(Object element) {
			if (element instanceof Class<?>)
				return ((Class<?>) element).getSimpleName();

			else if (element instanceof Method)
				return ((Method) element).getName();

			else if (element instanceof ScriptFunctionInfo) {
				ScriptFunctionInfo sfi = (ScriptFunctionInfo) element;
				if(sfi.getDescriptor() != null) {
					if(sfi.getDescriptor().isCodeSnippet())
						return sfi.getDescriptor().code.name;
					else
						return sfi.getDescriptor().getSignature();
				} else {
					return sfi.getName();
				}

			} else if (element instanceof NamedList) {
				return ((NamedList) element).getName();

			} else {
				return element.toString();

			}
		}
		
		public String getToolTipText(Object element) {
			if(element instanceof ScriptFunctionInfo) {
				ScriptFunctionInfo sfi = (ScriptFunctionInfo) element;
				StringBuffer buf = new StringBuffer();
				if(sfi.getDescriptor() != null) {
					buf.append(sfi.getDescriptor().getDescription());
				} else {
					buf.append(sfi.getName());
				}
				buf.append("\n");
				buf.append(cap.getScriptContext().getScriptName(sfi.getSource().getMetamergeConfig(), sfi.getSource()));
				// Double ampersands, as they are escape characters in tooltips
				return buf.toString().replace("&", "&&");
			}
			return null;
		}

		public void update(ViewerCell cell) {
			cell.setText(getText(cell.getElement()));
			cell.setImage(getImage(cell.getElement()));
		}
	}


	/**
	 * Content provider for the function widget tree view.
	 * 
	 */
	private static class ContentProvider implements ITreeContentProvider {

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Class<?>) {
				Class<?> cls = (Class<?>) inputElement;
				ArrayList<Method> arr = new ArrayList<Method>();
				for (Method m : cls.getMethods()) {
					arr.add(m);
				}
				Collections.sort(arr, new Comparator<Method>() {
					public int compare(Method o1, Method o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
				return arr.toArray();

			} else if (inputElement instanceof HashMap<?, ?>) {
				return ((HashMap<?, ?>) inputElement).values().toArray();

			} else if (inputElement instanceof JavaScriptDocParser) {
				return ((JavaScriptDocParser) inputElement).getAllFunctions().toArray();

			} else if (inputElement instanceof Object[]) {
				return (Object[]) inputElement;

			} else if (inputElement instanceof JavaScriptContentAssistProcessor) {
				JavaScriptContentAssistProcessor js = (JavaScriptContentAssistProcessor) inputElement;
				if (js.getScriptContext() != null) {
					return new Object[] { js.getScriptContext() };
				} else {
					return new Object[] {};
				}

			} else if (inputElement instanceof NamedList) {
				return ((NamedList) inputElement).getItems();
			} else {
				return new Object[0];
			}
		}

		public boolean hasChildren(Object element) {
			return element instanceof Class<?> || element instanceof Object[] || element instanceof NamedList
					|| element instanceof HashMap<?, ?>;
		}

		public Object getParent(Object element) {
			return null;
		}

		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}
	}

	private static class ScriptLibraryControl extends Composite {

		private AssemblyLineConfig alc;
		private ITreeContentProvider provider;
		private CheckboxTreeViewer tree;

		public ScriptLibraryControl(Composite parent, BaseConfiguration config) {
			super(parent, 0);
			this.alc = Utils.getParentConfig(config, AssemblyLineConfig.class);
			setLayout(new GridLayout(1, false));
			new Label(this, SWT.LEFT).setText(Messages.getString("FunctionWidget.autoincluded.scripts"));

			tree = new CheckboxTreeViewer(this, SWT.V_SCROLL);
			tree.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
			provider = new ITreeContentProvider() {
				private HashMap<Object, Object> parents = new HashMap<Object, Object>();
				public Object[] getChildren(Object parentElement) {
					return getElements(parentElement);
				}

				public Object getParent(Object element) {
					return parents.get(element);
				}

				public boolean hasChildren(Object element) {
					return element instanceof NamespaceConfig;
				}

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

				public void dispose() {
				}

				public Object[] getElements(Object inputElement) {
					ArrayList<Object> list = new ArrayList<Object>();
					if (inputElement instanceof AssemblyLineConfig) {
						try {
							addScripts(((AssemblyLineConfig) inputElement).getMetamergeConfig(), list);
							MetamergeFolder ns = ((AssemblyLineConfig) inputElement).getMetamergeConfig().getDefaultFolder(
									MetamergeConfig.NAMESPACE_FOLDER);
							for (String str : ns.getNames()) {
								NamespaceConfig nsc = ns.getMetamergeConfig().getNamespace(str);
								list.add(nsc);
							}
						} catch (Exception e) {
							list.add(e.toString());
						}

					} else if (inputElement instanceof NamespaceConfig) {
						try {
							addScripts(MetamergeConfigFactory.loadNamespace((NamespaceConfig) inputElement), list);
							for(Object obj : list)
								parents.put(obj, inputElement);
						} catch (Exception e) {
							list.add(e);
						}
					}

					return list.toArray();
				}

				private void addScripts(MetamergeConfig mc, ArrayList<Object> list) {
					try {
						MetamergeFolder folder = mc.getDefaultFolder(MetamergeConfig.SCRIPT_FOLDER);
						for (String str : folder.getNames()) {
							ScriptConfig sc = folder.getMetamergeConfig().getScript(str);
							list.add(sc);
						}
					} catch (Exception e) {
						list.add(e);
					}
				}
			};
			tree.setContentProvider(provider);

			tree.setLabelProvider(new LabelProvider() {

				@Override
				public Image getImage(Object element) {
					if (element instanceof ScriptConfig)
						return Activator.getImage("Script_16");
					else if (element instanceof NamespaceConfig)
						return Activator.getImage("Neo_16");
					else
						return Activator.getImage("Stop");
				}

				@Override
				public String getText(Object element) {
					if (element instanceof BaseConfiguration)
						return ((BaseConfiguration) element).getShortName();
					else
						return super.getText(element);
				}

			});
			tree.setInput(alc);

			boolean includeAll = alc.getSettings().getBooleanParameter("includeGlobalPrologs", true);
			String includeScripts = alc.getSettings().getStringParameter("includePrologs");
			if (includeScripts == null)
				includeScripts = "";
			ArrayList<String> includeList = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(includeScripts, "\r\n");
			while(st.hasMoreTokens()) {
				includeList.add(st.nextToken());
			}

			checkTreeItems(alc, includeAll, includeList);
			
			tree.addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent event) {
					if(event.getElement() instanceof NamespaceConfig) {
						for(Object obj : provider.getChildren(event.getElement())) {
							tree.setChecked(obj, event.getChecked());
						}
					}
				}
			});
		}

		private String getScriptName(Object cfg, ScriptConfig sc) {
			String name = sc.getShortName();
			if (cfg != alc) {
				String ns = (String) MetamergeConfigFactory.getLocalNamespaceFor(alc.getMetamergeConfig(), sc);
				if (ns != null)
					name = ns + ":" + name;
			}
			return name;
		}

		private boolean checkTreeItems(Object cfg, boolean includeAll, ArrayList<String> includeList) {
			boolean anyChecked = false;
			for (Object obj : provider.getChildren(cfg)) {
				if (obj instanceof ScriptConfig) {
					ScriptConfig sc = (ScriptConfig) obj;
					boolean auto = sc.getAutoInclude();
					String name = getScriptName(cfg, sc);
					if (auto && includeAll) {
						if(includeList.contains("-" + name)) {
							tree.setChecked(obj, false);
						} else {
							tree.setChecked(obj, true);
							anyChecked = true;
						}
					} else if (includeList.contains(name)) {
						tree.setChecked(obj, true);
						anyChecked = true;
					} else {
						tree.setChecked(obj, false);
					}
				} else if (obj instanceof NamespaceConfig) {
					boolean any = checkTreeItems(obj, includeAll, includeList);
					// if all children are unchecked the we uncheck parent ns as well
					tree.setChecked(obj, any);
				}
			}

			return anyChecked;
		}

		private void updateALSettings(Object cfg, ArrayList<String> includes, boolean includeAll) {
			for (Object obj : provider.getChildren(cfg)) {
				if (obj instanceof ScriptConfig) {
					ScriptConfig sc = (ScriptConfig) obj;
					String name = getScriptName(cfg, sc);
					boolean auto = includeAll && ((ScriptConfig) obj).getAutoInclude();
					if (!auto && tree.getChecked(obj))
						includes.add(name);
					else if (auto && !tree.getChecked(obj))
						includes.add("-" + name);

				} else if (obj instanceof NamespaceConfig) {
					updateALSettings(obj, includes, includeAll);

				}
			}
		}

		public void updateALSettings() {
			boolean includeAll = alc.getSettings().getBooleanParameter("includeGlobalPrologs", true);
			ArrayList<String> includes = new ArrayList<String>();

			updateALSettings(alc, includes, includeAll);

			StringBuffer buf = new StringBuffer();
			for (String str : includes) {
				if (buf.length() > 0)
					buf.append("\n");
				buf.append(str);
			}
			alc.getSettings().setStringParameter("includePrologs", buf.toString());
			
		}

	}
}
