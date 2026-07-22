/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.di.config.base.OperationConfigImpl;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.actions.AddSchemaItemAction;
import com.ibm.tdi.eclipse.actions.PasteConfigAction;
import com.ibm.tdi.eclipse.dialogs.ComboInputDialog;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.WorkEntryAttributesProvider;
import com.ibm.tdi.eclipse.util.TDIToolBar;

public class OperationsWidget extends BaseWidget {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private AssemblyLineConfig alc;
	private TabFolder maps;
	private ArrayList<Control> controls = new ArrayList<Control>();
	private TabItem selectedMenuItem;
	private TabItem initItem;
	private final static String[] STANDARD_OPERATIONS = {
		"getNextEntry",
		"putEntry",
		"modEntry",
		"findEntry",
		"deleteEntry",
		"selectEntries",
		"queryReply",
		"initialize",
		"terminate",
		"Default"
	};
	
	public OperationsWidget(Composite parent, int style, AssemblyLineConfig config) {
		super(parent, style, config);
		this.alc = config;
		createUI();
	}

	public OperationsWidget(Composite parent, int style, AssemblyLineConfig config, BaseEditor editor) {
		super(parent, style, config, editor);
		this.alc = config;
		createUI();
	}

	private void createUI() {
		setLayout(new FillLayout());
		Form frm = createForm(this, null);
		TDIToolBar bar= new TDIToolBar(frm);
		bar.setText(Messages.getString("assemblyline.tabs.callreturn.label"));
		Composite c = frm.getBody();
		c.setLayout(new FillLayout());

		maps = new TabFolder(c, SWT.TOP);
		maps.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				for(Control c : controls)
					c.dispose();
			}
		});

		bar.add(new Action() {
			public String getText() {
				return Messages.getString("general.insert.label");
			}
			public String getToolTipText() {
				return Messages.getString("AssemblyLine.Operations.Add.label");
			}
			public void run() {
				ComboInputDialog id = new ComboInputDialog(getShell(), 
						getToolTipText(), 
						Messages.getString("SchemaEditor.18"), 
						Arrays.asList(STANDARD_OPERATIONS),
						new NameValidator());
			
				if(id.open() == Window.OK) {
					OperationConfigImpl oc = new OperationConfigImpl();
					try {
						oc.init();
						oc.setName(id.getValue());
						oc.getAttributeMap(true).setEnabled(true);
						oc.getAttributeMap(false).setEnabled(true);
						alc.getOperations().addConfig(oc);
						createOperationForm(oc);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
			}
		});

		final Action deleteAction = new Action() {
			public String getText() {
				return Messages.getString("general.delete.label");
			}
			public String getToolTipText() {
				return Messages.getString("AssemblyLine.Operations.Remove.label");
			}
			public void run() {
				if(maps.getSelectionIndex() < 1)
					return;

				if(!MessageDialog.openConfirm(getShell(), Messages.getString("miadmin.menu.Object.DeleteItem.label"), Messages.getString("SimpleListUI.prompt.Delete")))
					return;

				TabItem tab = maps.getItem(maps.getSelectionIndex());

				OperationConfig oc = (OperationConfig) tab.getData();
				alc.getOperations().removeConfig(oc);
				tab.dispose();
			}
		};


		maps.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				deleteAction.setEnabled(maps.getSelectionIndex() > 0);
			}
		});

		bar.add(deleteAction);
		deleteAction.setEnabled(maps.getSelectionIndex() > 0);

		addMenuActions();

		addAlOpInit();

		for(int i = 0; i < alc.getOperations().size(); i++) {
			OperationConfig oc = (OperationConfig) alc.getOperations().getConfig(i);
			createOperationForm(oc);
		}
	}

	private void addAlOpInit() {
		Form alinit = getFormToolKit().createForm(maps);
		alinit.setText(Messages.getString("Operations.initialize.label"));
		getFormToolKit().decorateFormHeading(alinit);
		alinit.getBody().setLayout(new GridLayout(1,false));

		Section s = createSchemaSection(alinit, alc.getPublishedInitParams(), true, true);
		s.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		initItem = new TabItem(maps, SWT.LEFT);
		initItem.setText(Messages.getString("Operations.initialize.label"));
		initItem.setControl(alinit);
	}

	protected TabItem createOperationForm(final OperationConfig oc) {
		SashForm form = new SashForm(maps, SWT.HORIZONTAL);
		WorkMapWidget wk = new WorkMapWidget(form, SWT.NONE, oc, false, true, 0);
		wk.setEditor(getEditor());
		Form schemas = getFormToolKit().createForm(form);
		TDIToolBar bar = new TDIToolBar(schemas);
		getFormToolKit().decorateFormHeading(schemas);
		bar.setText(Messages.getString("DiscoverSchemaWidget.0"));
		schemas.getBody().setLayout(new GridLayout(1,true));

		//
		// -- Discover ToolBar button
		//
		bar.add(new Action() {
			public String getText() {
				return Messages.getString("DiscoverSchemaWidget.5"); //$NON-NLS-1$
			}

			public void run() {
				WorkEntryAttributesProvider p = new WorkEntryAttributesProvider(null);
				p.inputChanged(null, null, alc);
				List<String> attrs = p.getSortedAttributes();
				copyToSchema(oc.getSchema(true), attrs);
				copyToSchema(oc.getSchema(false), attrs);
			}
		});

		createSchemaSection(schemas, oc, true);
		createSchemaSection(schemas, oc, false);
		form.setWeights(new int[]{60,40});

		TabItem tab = new TabItem(maps, SWT.LEFT);
		tab.setText(oc.getShortName());
		tab.setData(oc);
		tab.setControl(form);
		// We have to dispose this manually
		controls.add(wk);
		return tab;
	}

	private void copyToSchema(SchemaConfig sc, List<String> items) {
		sc.notifyChange(sc, "", MetamergeConfigChange.BEGIN_CHANGES);
		for (String name:items) {
			try {
				sc.newItem(name);
			} catch (Exception e) {
				// Exception means the name already existed. That's fine
				SystemFunctions.doNothing();
			}
		}
		sc.notifyChange(sc, "", MetamergeConfigChange.END_CHANGES);
	}

	private Section createSchemaSection(Form form, OperationConfig oc, boolean input) {
		return createSchemaSection(form, oc.getSchema(input), input, false);
	}

	private Section createSchemaSection(Form form, SchemaConfig schema, boolean input, boolean alInitParams) {

		Section section = getFormToolKit().createSection(form.getBody(), Section.TITLE_BAR);	
		section.setText(Messages.getString(input? "OperationsWidget.1" : "OperationsWidget.2" ));

		BaseWidget scw;
		TreeViewer viewer = null;
		if (alInitParams) {
			scw = new ALInitParamsWidget(schema, section, SWT.NONE);
			viewer = ((ALInitParamsWidget)scw).getViewer();
		} else {
			scw= new SchemaConfigWidget(schema, section, SWT.NONE);
			viewer = ((SchemaConfigWidget)scw).getSchemaTree();
		}

		final BaseEditor editor = getEditor();
		if (editor != null) {
			editor.registerContextMenu(viewer, "schemaitem"); //$NON-NLS-1$
			editor.addSelectionProvider(viewer);
			scw.setEditor(editor);

			final PasteAction pasteAction = new PasteAction(schema);
			editor.getMenuManager().appendToGroup("group.edit", pasteAction);
			viewer.getTree().addFocusListener(new FocusListener() {
				private IAction saveAction;

				public void focusGained(FocusEvent e) {
					saveAction = editor.getActionFor(ActionFactory.PASTE.getId());
					editor.registerAction(ActionFactory.PASTE.getId(), pasteAction);
					editor.updateActionBars();
				}

				public void focusLost(FocusEvent e) {
					editor.registerAction(ActionFactory.PASTE.getId(), saveAction);
					editor.updateActionBars();
				}
			});
		}
		section.setClient(scw);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// -- ToolBar contributions
		ToolBarManager tbm = new ToolBarManager();
		tbm.add(new AddSchemaItemAction(getShell(), schema));
		ToolBar tools = tbm.createControl(section);
		section.setTextClient(tools);
		tbm.update(true);
		return section;
	}

	protected void showSelection(ISelection selection) {
		if(!selection.isEmpty()) {
			OperationConfig oc = (OperationConfig) ((IStructuredSelection)selection).getFirstElement();
			for(TabItem item : maps.getItems()) {
				if(item.getData() == oc) {
					maps.setSelection(item);
					return;
				}
			}
			maps.setSelection(createOperationForm(oc));
		}
	}

	private class NameValidator implements IInputValidator {

		public String isValid(String newText) {
			if (alc.getOperation(newText) != null)
				return Messages.getString("OperationsWidget.alreadyExists");
			return null;
		}	
	}

	private void addMenuActions() {
		final Action deleteAction = new Action() {
			public String getText() {
				return Messages.getString("general.delete.label");
			}
			public String getToolTipText() {
				return Messages.getString("AssemblyLine.Operations.Remove.label");
			}
			public void run() {
				if(selectedMenuItem == null)
					return;

				if(!MessageDialog.openConfirm(getShell(), Messages.getString("miadmin.menu.Object.DeleteItem.label"), Messages.getString("SimpleListUI.prompt.Delete")))
					return;

				OperationConfig oc = (OperationConfig) selectedMenuItem.getData();
				alc.getOperations().removeConfig(oc);
				selectedMenuItem.dispose();
			}
		};

		final Action renameAction = new Action() {
			public String getText() {
				return Messages.getString("action.label.1");
			}
			public String getToolTipText() {
				return Messages.getString("action.tooltip.3");
			}
			public void run() {
				if(selectedMenuItem == null)
					return;

				final OperationConfig oc = (OperationConfig) selectedMenuItem.getData();
				String title = Messages.getString("general.rename.label");
				IInputValidator validator = new IInputValidator() {
					public String isValid(String newText) {
						if (newText == null)
							return null;
						newText = newText.trim();
						if (newText.equals(oc.getShortName()))
							return null;
						if (alc.getOperation(newText) != null)
							return Messages.getString("OperationsWidget.alreadyExists");
						return null;
					}					
				};

				while(true) {
					InputDialog id = new InputDialog(getShell(), title, title + ": ", oc.getShortName(), validator);
					if(id.open() == Window.CANCEL)
						return;
					String name = id.getValue();
					if (name == null || name.trim().length() == 0)
						return;
					name = name.trim();
					try {
						oc.setName(name);
						selectedMenuItem.setText(name);
						return;
					} catch (Exception e) {
						EclipseAppender.logerror(Messages.getMessage("RenameConfigAction.Error", name), e, getShell());
					}
				}
			}
		};

		MenuManager mm = new MenuManager();
		maps.setMenu(mm.createContextMenu(maps));
		mm.add(renameAction);
		mm.add(deleteAction);

		maps.addMenuDetectListener(new MenuDetectListener() {
			public void menuDetected(MenuDetectEvent e) {
				Point p = maps.getDisplay().map(null, maps, e.x, e.y);
				selectedMenuItem = maps.getItem(p);
				boolean enabled = selectedMenuItem != initItem && selectedMenuItem != null;
				deleteAction.setEnabled(enabled);
				renameAction.setEnabled(enabled);
			}
		});

	}
	
	private class PasteAction extends PasteConfigAction {

		private SchemaConfig schema;
		public PasteAction(SchemaConfig schema) {
			super(Messages.getString("common.Paste.name")); //$NON-NLS-1$
			this.schema = schema;
		}

		@Override
		protected void performPaste(IStructuredSelection selection) {
			for (Iterator<?> i = selection.iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof BaseConfiguration) {
					BaseConfiguration b = (BaseConfiguration) obj;
					try {
						if (b instanceof SchemaItemConfig) {
							SchemaItemConfig source = (SchemaItemConfig) b;
							SchemaItemConfig target = schema.newItem(b.getName());
							if (source.getExternalSyntax() != null)
								target.setExternalSyntax(source.getExternalSyntax());
							if (source.getJavaClass() != null)
								target.setJavaClass(source.getJavaClass());
							if (source.getPresenceFlag() != null)
								target.setPresenceFlag(source.getPresenceFlag());
						} else {
							schema.newItem(b.getShortName());
						}
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getShell());
					}
				}
			}
		}

		@Override
		protected boolean validatePaste(Object obj) {
			boolean valid = (obj instanceof SchemaItemConfig || obj instanceof AttributeMapItem);
			if (!valid && obj instanceof BaseConfiguration) {
				BaseConfiguration b = (BaseConfiguration) obj;
				if (b.getShortName() != null) {
					valid = true;
				}
			}
			return valid;
		}
	}
}
