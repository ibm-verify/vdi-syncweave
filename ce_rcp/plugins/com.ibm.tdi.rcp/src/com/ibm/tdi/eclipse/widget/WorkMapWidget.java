/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.ide.IDE;

import com.ibm.di.config.base.AttributeMapItemImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.RawFunctionConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.config.interfaces.ValidatorConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.validate.ValidationIssue;
import com.ibm.di.server.validate.Validator;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.AddAttributeMapItemAction;
import com.ibm.tdi.eclipse.actions.ChangeInheritanceAction;
import com.ibm.tdi.eclipse.actions.CopyConfigAction;
import com.ibm.tdi.eclipse.actions.CutConfigAction;
import com.ibm.tdi.eclipse.actions.NullBehaviorAction;
import com.ibm.tdi.eclipse.actions.PasteConfigAction;
import com.ibm.tdi.eclipse.actions.SaveToLibraryAction;
import com.ibm.tdi.eclipse.dnd.ChangeInheritanceHandler;
import com.ibm.tdi.eclipse.editors.AssemblyLineEditor2;
import com.ibm.tdi.eclipse.editors.AssemblyLineEditor3;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.editors.ConnectorEditor;
import com.ibm.tdi.eclipse.editors.DataBrowserEditor;
import com.ibm.tdi.eclipse.editors.TDIConfigEditorInput;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.MapContentProvider;
import com.ibm.tdi.eclipse.providers.MapLabelProvider;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.util.TDIToolBar.PullDownButton;

public class WorkMapWidget extends BaseWidget implements ISelectionChangedListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	/*
	 * Show work/assignment/target
	 */
	public final static int MAP_MODE_BOTH = 0;

	/*
	 * Show work/assignment
	 */
	public final static int MAP_MODE_INPUT = 1;

	/*
	 * Show assignment/target
	 */
	public final static int MAP_MODE_OUTPUT = 2;

	private TreeViewer tree;

	private DropTargetListener dtl;

	private DragSourceAdapter dsl;

	private Action cutHandler;

	private Action addHandler;

	private PasteAction pasteAction;
	
	private CutConfigAction cutAction;
	
	private CopyConfigAction copyAction;
	
	private CutConfigAction deleteAction;

	private Action browseHandler;

	private SashForm sash;

	private AttributeMapItemEditor editorForm;

	private boolean simpleLayout;

	private boolean quickEditorEnabled;

	private int mappingMode;

	private TreeViewerColumn modColumn;
	private TreeViewerColumn addColumn;
	private MapLabelProvider labelProvider;
	private TDIToolBar client;

	public WorkMapWidget(Composite parent, int style, BaseConfiguration editingConfig) {
		this(parent, style, editingConfig, false, false, 0);
	}

	public WorkMapWidget(Composite parent, int style, BaseConfiguration editingConfig, boolean simpleLayout, boolean quickEditor,
			int mappingMode) {
		this(parent, style, editingConfig, simpleLayout, quickEditor, mappingMode, null);
	}

	public WorkMapWidget(Composite parent, int style, BaseConfiguration editingConfig, boolean simpleLayout, boolean quickEditor,
			int mappingMode, BaseEditor editor) {
		super(parent, style, editingConfig);
		this.simpleLayout = simpleLayout;
		this.quickEditorEnabled = quickEditor;
		this.mappingMode = mappingMode;
		super.setEditor(editor);
		setLayout(new FillLayout());
		createUI(this);
		setEditor(editor);
	}

	private void createUI(Composite parent) {
		if (quickEditorEnabled) {
			sash = new SashForm(parent, SWT.VERTICAL);
			createMainForm(sash);
			editorForm = new AttributeMapItemEditor(sash, SWT.NONE);
			sash.setWeights(new int[] { 100, 0 });
		} else {
			createMainForm(this);
		}
	}

	protected void quickEdit(BaseConfiguration config) {
		if(!isPlaceHolder(config))
			editorForm.quickEdit(config);
	}

	private boolean isPlaceHolder(Object bc) {
		return bc instanceof BaseConfiguration && ((BaseConfiguration)bc).getParameter("%%PLACEHOLDER%%") != null;
	}

	private void createMainForm(Composite parent) {
		Form form = createForm(parent, null);

		// -- no title for simple layout
		if (!simpleLayout)
			form.setText(Messages.getString("WorkEntryWidget.2")); //$NON-NLS-1$

		GridLayout gl = new GridLayout(1, true);
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		form.getBody().setLayout(gl);

		MapContentProvider contentProvider = new MapContentProvider() {
			@Override
			public Object[] getElements(Object element) {
				Object[] result;
				if (element instanceof AttributeMapConfig) {
					result = super.getChildren(element);
					// -- only show empty-map placeholder if AL's implicit mapping is off (and list is empty)
					AssemblyLineConfig alc = Utils.getParentConfig(element, AssemblyLineConfig.class);
					boolean hidePlaceHolder = alc == null ? false : alc.getSettings().getBooleanParameter("automapattributes", false);
					if(hidePlaceHolder && result != null && result.length == 1 && isPlaceHolder(result[0]) &&
							(isOutputMapMode() || isInputMapMode()))
						return new Object[]{};
				} else {
					result = super.getElements(element);
				}
				return result;
			}

			@Override
			public void configurationChanged(MetamergeConfigChange changeEvent) {
				super.configurationChanged(changeEvent);
				if(changeEvent.getSource() instanceof AttributeMapConfig 
						&& editorForm != null 
						&& changeEvent.getOperation() == MetamergeConfigChange.MCC_REMOVE
						&& editorForm.getEditingConfig() instanceof AttributeMapItem
						&& changeEvent.getKey().equals(editorForm.getEditingConfig().getShortName())) {
					editorForm.quickEdit(null);
				}

				// -- Update "Map" title label to reflect NVB settings
				if(changeEvent.getKey() instanceof String) {
					String key = (String) changeEvent.getKey();
					if(key.startsWith(InternalSchema.NULL_BEHAVIOR) || key.startsWith(InternalSchema.NULL_DEFINITION)) {
						getDisplay().syncExec(new Runnable() {
							public void run() {
								updateTitleToolTip();
							}
						});
					}
				}
			}
		};

		labelProvider = new MapLabelProvider(getDisplay()) {
			public String getColumnText(Object element, int columnIndex) {
				if (element instanceof AttributeMapItem && isOutputMapMode()) {
					switch (columnIndex) {
					case 0:
						return super.getColumnText(element, 1);
					case 1:
						if(addColumn != null)
							return ""+((AttributeMapItem)element).getAdd();
						else
							return ((AttributeMapItem)element).getShortName();
					case 2:
						if(modColumn != null)
							return ""+((AttributeMapItem)element).getModify();
					case 3:
						return ((AttributeMapItem)element).getShortName();
					}
				}
				return super.getColumnText(element, columnIndex);
			}
		};

		// -- tree for attribute maps
		tree = new TreeViewer(form.getBody(), SWT.MULTI | SWT.FULL_SELECTION);
		tree.setContentProvider(contentProvider);
		tree.getControl().setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		tree.getTree().setLinesVisible(true);
		tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if (getEditor() != null)
			getEditor().addSelectionProvider(tree);


		// New code ---  DI01331 
		TreeViewerColumn col = new TreeViewerColumn(tree, SWT.LEFT);
		if (isOutputMapMode()) {
			col.getColumn().setText(Messages.getString("WorkEntryWidget.4")); //$NON-NLS-1$
		} else {
			col.getColumn().setText(Messages.getString("WorkEntryWidget.3")); //$NON-NLS-1$
		}
		col.setLabelProvider(labelProvider);
		// End of new code --



		col.getColumn().setWidth(200);
		// -- Add column editing support for work attribute name
		if (!isOutputMapMode())
			col.setEditingSupport(new WorkAttributeEditingSupport(tree));


		// Assignment
		col = new TreeViewerColumn(tree, SWT.LEFT);
		if (isOutputMapMode()) {
			col.getColumn().setText(Messages.getString("WorkEntryWidget.5")); //$NON-NLS-1$
			col.getColumn().setImage(Activator.getImage("arrow-right_51x16"));
			col.setEditingSupport(new ConnAttributeEditingSupport(tree));
			col.setLabelProvider(labelProvider);
		} else {
			if(isInputMapMode())
				col.getColumn().setImage(Activator.getImage("arrow-left_51x16"));
			col.getColumn().setText(Messages.getString("WorkEntryWidget.4")); //$NON-NLS-1$
			col.setLabelProvider(new ODP(col, labelProvider));
		}
		col.getColumn().setWidth(200);

		// Component
		if (!(isInputMapMode() || isOutputMapMode() || isAttributeMapComponent() )) {
			col = new TreeViewerColumn(tree, SWT.LEFT);
			col.getColumn().setText(Messages.getString("WorkEntryWidget.5")); //$NON-NLS-1$
			col.getColumn().setWidth(200);
			col.setLabelProvider(labelProvider);

			// -- Add column editing support for work attribute name
			col.setEditingSupport(new ConnAttributeEditingSupport(tree));
		}

		// Update mode needs add/mod columns
		updateAddModColumns();

		// Enable owner draw for this viewer
		OwnerDrawLabelProvider.setUpOwnerDraw(tree);

		tree.getTree().setHeaderVisible(true);

		// Enable per item tool-tips for this tree
		ColumnViewerToolTipSupport.enableFor(tree);

		// DND
		addDragDropSupport();

		BaseConfiguration inputConfig = getEditingConfig();
		if (isInputMapMode())
			inputConfig = ((ConnectorConfig) inputConfig).getAttributeMap(true);
		else if (isOutputMapMode())
			inputConfig = ((ConnectorConfig) inputConfig).getAttributeMap(false);

		inputConfig.notifyChange(inputConfig, "", MetamergeConfigChange.BEGIN_CHANGES);
		tree.setInput(inputConfig);
		tree.refresh();
		tree.expandAll();
		inputConfig.notifyChange(inputConfig, "", MetamergeConfigChange.END_CHANGES);


		tree.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.getFirstElement() instanceof BaseConfiguration
						&& ((BaseConfiguration) sel.getFirstElement()).getParameter("%%PLACEHOLDER%%") != null)
					addAttributeMapItem(((BaseConfiguration) sel.getFirstElement()).getParameter("%%PLACEHOLDER%%"));
				else if (sel.getFirstElement() instanceof AttributeMapItem) {
					if (quickEditorEnabled)
						quickEdit((AttributeMapItem) sel.getFirstElement());
				} else if (getEditor() instanceof AssemblyLineEditor2)
					((AssemblyLineEditor2) getEditor()).editItem(sel);
				else if (getEditor() != null && !isPlaceHolder(sel.getFirstElement()))
					getEditor().quickEdit(sel);
			}
		});

		createFormToolbarItems();

		if (getEditingConfig() instanceof ConnectorConfig) {
			ConnectorConfig cc = (ConnectorConfig) getEditingConfig();
			boolean input = Utils.isInputConnector(cc);
			// If both input and output, just use normal menus.
			if (!(input && Utils.isOutputConnector(cc))) {
				AttributeMapConfig map = cc.getAttributeMap(input);
				form.getMenuManager().add(new ChangeInheritanceAction(map));
				form.addTitleDropSupport(DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer() },
						new ChangeInheritanceHandler(map));
			}
		}

	}

	private boolean isAttributeMapComponent() {
		return getEditingConfig() instanceof ALMappingConfig;
	}

	private void updateAddModColumns() {
		if(!isOutputMapMode())
			return;

		if(getEditingConfig() instanceof ConnectorConfig) {
			ConnectorConfig cc = (ConnectorConfig) getEditingConfig();
			if(ConnectorConfig.UPDATE_MODE.equals(cc.getMode()) ||
				ConnectorConfig.DELTA_MODE.equals(cc.getMode())) {
				if(addColumn == null) {
					addColumn = new TreeViewerColumn(tree, SWT.LEFT, 1);
					addColumn.getColumn().setText(Messages.getString("AttributeMap.1.label"));
					addColumn.getColumn().setWidth(50);
					addColumn.setEditingSupport(new EditingSupport(tree) {
						protected boolean canEdit(Object element) {
							return element instanceof AttributeMapItem;
						}
						protected CellEditor getCellEditor(Object element) {
							return new CheckboxCellEditor(tree.getTree());
						}
						protected Object getValue(Object element) {
							return ((AttributeMapItem)element).getAdd();
						}
						protected void setValue(Object element, Object value) {
							((AttributeMapItem)element).setAdd(Boolean.valueOf(value.toString()));
						}
					});
					addColumn.setLabelProvider(labelProvider);

					modColumn = new TreeViewerColumn(tree, SWT.LEFT, 2);
					modColumn.getColumn().setText(Messages.getString("AttributeMap.2.label"));
					modColumn.getColumn().setWidth(50);
					modColumn.setEditingSupport(new EditingSupport(tree) {
						protected boolean canEdit(Object element) {
							return element instanceof AttributeMapItem;
						}
						protected CellEditor getCellEditor(Object element) {
							return new CheckboxCellEditor(tree.getTree());
						}
						protected Object getValue(Object element) {
							return ((AttributeMapItem)element).getModify();
						}
						protected void setValue(Object element, Object value) {
							((AttributeMapItem)element).setModify(Boolean.valueOf(value.toString()));
						}
					});
					modColumn.setLabelProvider(labelProvider);

					// -- reduce 1st column width
					tree.getTree().getColumn(0).setWidth(150);

				} else {
					if(addColumn != null) {
						addColumn.getColumn().dispose();
						addColumn = null;
						modColumn.getColumn().dispose();
						modColumn = null;
					}
				}
			}
		}
	}

	private boolean isOutputMapMode() {
		return mappingMode == MAP_MODE_OUTPUT;
	}

	private boolean isInputMapMode() {
		return mappingMode == MAP_MODE_INPUT;
	}

	protected void addAttributeMapItem(Object parameter) {
		if (parameter instanceof AttributeMapConfig) {
			AddAttributeMapItemAction map = new AddAttributeMapItemAction();
			map.setSelection(new StructuredSelection(parameter));
			map.setActivePart(null, getEditor());
			map.run(null);
		}
	}

	/**
	 * Create extra toolbar items when we are editing
	 */
	private void createFormToolbarItems() {
		addHandler = new Action() {
			@Override
			public String getText() {
				return Messages.getString("BranchingConfig.Add.label");
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("action.tooltip.2");
			}

			@Override
			public void run() {
				AddAttributeMapItemAction acc = new AddAttributeMapItemAction();
				acc.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				ISelection sel = tree.getSelection();
				if (sel.isEmpty() && getEditingConfig() instanceof ConnectorConfig) {
					ConnectorConfig cc = (ConnectorConfig) getEditingConfig();
					sel = new StructuredSelection(cc.getAttributeMap(isInputMapMode()));
				}
				acc.setSelection(sel);
				acc.run(null);
			}

		};
		addHandler.setEnabled(false);

		cutHandler = new Action() {
			@Override
			public void run() {
				IAction action = getEditor().getActionFor(ActionFactory.CUT.getId());
				if(action == null)
					action = getEditor().getActionFor(ActionFactory.DELETE.getId());
				if(action != null)
					action.run();
			}

			@Override
			public String getText() {
				return Messages.getString("general.delete.label");
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("general.delete.tooltip");
			}
		};
		cutHandler.setEnabled(false);

		browseHandler = new Action() {
			@Override
			public void run() {
				TDIConfigEditorInput input = null;
				if (getEditor() instanceof AssemblyLineEditor3) {
					Object bc = ((IStructuredSelection) tree.getSelection()).getFirstElement();
					if(bc == null)
						bc = getEditingConfig();
					ConnectorConfig cc = Utils.getParentConfig(bc, ConnectorConfig.class);
					input = new TDIConfigEditorInput(cc, DataBrowserEditor.EDITOR_ID);
				} else if (getEditor() instanceof ConnectorEditor) {
					ConnectorConfig cc = Utils.getParentConfig(getEditingConfig(), ConnectorConfig.class);
					input = new TDIConfigEditorInput(cc, DataBrowserEditor.EDITOR_ID);
				}
				try {
					IDE.openEditor(getEditor().getEditorSite().getPage(), input, DataBrowserEditor.EDITOR_ID, true);
				} catch (PartInitException e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}

			@Override
			public String getText() {
				return Messages.getString("DataBrowser.title");
			}
		};
		browseHandler.setEnabled(false);

		createToolbarButtons();

		//
		// Update items depending on selection
		//
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				AttributeMapItem mapItem = null;

				boolean override = isOutputMapMode() || isInputMapMode();
				addHandler.setEnabled(override);
				browseHandler.setEnabled(override);
				cutHandler.setEnabled(!sel.isEmpty());

				if(sel.toList().size() > 1) {
					boolean map = false;
					boolean normal = false;
					for(Object obj : sel.toArray()) {
						if(obj instanceof AttributeMapConfig)
							map = true;
						else
							normal = true;
					}
					cutHandler.setEnabled(!(map && normal));
				}

				if (!sel.isEmpty() && sel.toList().size() == 1) {
					addHandler.setEnabled(true);
					browseHandler.setEnabled(true);
					cutHandler.setEnabled(true);
					BaseConfiguration bc = (BaseConfiguration) ((IStructuredSelection) tree.getSelection()).getFirstElement();
					if (bc instanceof AttributeMapItem) {
						mapItem = (AttributeMapItem) bc;
						if (sash != null && sash.getWeights()[1] != 0 && 
								editorForm.getEditingConfig() != mapItem) {
							quickEdit(mapItem);
						}
					} else if (bc instanceof AttributeMapConfig &&
							getEditingConfig() instanceof ConnectorConfig &&
							getEditingConfig().getParent() == null) {
						cutHandler.setEnabled(false);
					}
				} else if (getEditor() instanceof ConnectorEditor) {
					browseHandler.setEnabled(true);
				}

				getForm().getToolBarManager().update(true);
			}
		});
	}

	private void createToolbarButtons() {
		if (getEditingConfig() instanceof OperationConfig)
			return;

		Form form = getForm();

		client = new TDIToolBar(form, SWT.LEFT|SWT.SINGLE|SWT.TITLE);

		// -- Use small text instead of toolbar's fat font
		client.setText(Messages.getString("WorkMapWidget.attributemap.title"));
		client.setTitleFont(null);
		updateTitleToolTip();

		client.add(addHandler);
		client.add(cutHandler);

		addHandler.setEnabled(isInputMapMode() || isOutputMapMode());
		cutHandler.setEnabled(false);

		if(isInputMapMode() || isOutputMapMode()) {
			Action validatorHandler = null;
			if (isInputMapMode()) {
				validatorHandler = getValidatorHandler("input");
			} else if (isOutputMapMode()) {
				validatorHandler = getValidatorHandler("output");
			}
			if (validatorHandler != null) {
				client.add(validatorHandler);
			}
			PullDownButton more = client.addMoreButton(Messages.getString("ComponentOptionsWidget.more"), null, null);

			// No browse data for AL Mapping.
			if (!isAttributeMapComponent())
				more.addMenuOption(browseHandler);

			final ConnectorConfig cc = (ConnectorConfig) getEditingConfig();
			ChangeInheritanceAction cia = new ChangeInheritanceAction(cc.getAttributeMap(isInputMapMode())) {
				@Override
				public void run() {
					super.run();
					updateTitleToolTip();
				}
			};
			more.addMenuOption(cia);
			Action copyToLibrary = new Action() {
				public String getText() {
					return Messages.getString("general.save.library.label");
				}
				public void run() {
					SaveToLibraryAction sal = new SaveToLibraryAction();
					sal.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
					sal.setSelection(new StructuredSelection(cc.getAttributeMap(isInputMapMode())));
					sal.run(this);
				}
			};
			more.addMenuOption(copyToLibrary);

			Action nvb = new Action() {
				public String getText() {
					return Messages.getString("NullBehavior.popup.Title");
				}
				public void run() {
					NullBehaviorAction sal = new NullBehaviorAction();
					sal.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
					sal.setSelection(new StructuredSelection(cc.getAttributeMap(isInputMapMode())));
					sal.run(this);
					updateTitleToolTip();
				}
			};
			more.addMenuOption(nvb);

			browseHandler.setEnabled(true);
		} else if (!isAttributeMapComponent()){
			client.add(browseHandler);
		}
	}

	/**
	 * Create new validation action if form config have validation
	 * configurations.
	 * 
	 * @param name
	 *            used to specify the validator.
	 * @return validation Action.
	 */
	private Action getValidatorHandler(String name) {
		final FormConfig formConfig = getFormConfig();
		if (formConfig == null) {
			return null;
		}
		List<ValidatorConfig> validatorConfigs = formConfig.getValidators();
		final List<Validator> validators = new ArrayList<Validator>();
		String validatorName = null;
		for (ValidatorConfig validatorConfig : validatorConfigs) {
			if (!"map".equals(validatorConfig.getType())) {
				continue;
			}
			validatorName = validatorConfig.getStringParameter("name");
			if (validatorName == null || validatorName.length() == 0 || validatorName.equals(name)) {
				Validator validator = getInstance(validatorConfig.getValidatorClass());
				if (validator != null) {
					validators.add(validator);
				}
			}
		}
		Action validatorHandler = null;
		if (validators.size() > 0) {
			validatorHandler = new Action() {
				@Override
				public void run() {
					new Thread(new ValidateRunnable(this, validators, getEditingConfig())).start();
				}

				@Override
				public String getText() {
					return Messages.getString("Action.AtrributeMap.Validate.label");
				}

				@Override
				public String getToolTipText() {
					return Messages.getString("Action.AtrributeMap.Validate.tooltip");
				}
			};
		}
		return validatorHandler;
	}

	/**
	 * Try to get Form Configuration.
	 * 
	 * @return Form Configuration or null if there is no Form Configuration.
	 */
	private FormConfig getFormConfig() {
		FormConfig formConfig = null;
		BaseConfiguration editingConfig = getEditingConfig();

		String clazz = null;
		if (editingConfig instanceof FunctionConfig) {
			RawFunctionConfig rawFuncConfig = ((FunctionConfig) getEditingConfig()).getFunctionConfig();
			if (rawFuncConfig != null) {
				clazz = rawFuncConfig.getJavaClass();
			}
		} else if (getEditingConfig() instanceof ConnectorConfig) {
			RawConnectorConfig rawConnConfig = ((ConnectorConfig) getEditingConfig()).getConnectionConfig();
			if (rawConnConfig != null) {
				clazz = rawConnConfig.getJavaClass();
				if (clazz != null && clazz.startsWith("@")) {
					if (rawConnConfig.getInheritsFrom() instanceof RawConnectorConfig)
						clazz = ((RawConnectorConfig) rawConnConfig.getInheritsFrom()).getJavaClass();
					else
						clazz = null;
				}
			}
		}

		if (clazz != null) {
			try {
				formConfig = (FormConfig) getEditingConfig().getMetamergeConfig().lookup("system:/Forms/" + clazz);
			} catch (Exception e) {
				return null;
			}
		}

		return formConfig;
	}

	/**
	 * Create new instance of provided class name.
	 * 
	 * @param validatorClass
	 *            to be instanced.
	 * @return new instance of provided class name.
	 */
	private Validator getInstance(String validatorClass) {
		Validator validator = null;
		try {
			validator = (Validator) Class.forName(validatorClass).newInstance();
		} catch (Exception e) {
			EclipseAppender.logerror(e.getLocalizedMessage(), e);
			// ignore non-existing validators
			validator = null;
		}
		return validator;
	}

	/**
	 * A separate thread that does the validation work.
	 */
	private class ValidateRunnable implements Runnable {

		/**
		 * List of validators.
		 */
		private List<Validator> validators = null;

		/**
		 * The action that runs this thread.
		 */
		private Action action = null;

		/**
		 * The configuration that contains information for validation.
		 */
		private BaseConfiguration config = null;

		/**
		 * Constructor.
		 * 
		 * @param action
		 *            the action that runs this thread.
		 * @param validators
		 *            list of validators.
		 * @param config
		 *            a configuration containing validation information.
		 */
		public ValidateRunnable(Action action, List<Validator> validators, BaseConfiguration config) {
			this.action = action;
			this.validators = validators;
			this.config = config;
		}

		/**
		 * {@inheritDoc}
		 */
		public void run() {
			action.setEnabled(false);
			deleteMarkers();
			List<ValidationIssue> issues = null;
			try {
				for (Validator validator : validators) {
					validator.initialize(config);
					issues = validator.validate();

					if (issues != null && issues.size() > 0) {
						for (ValidationIssue issue : issues) {
							Utils.logProblem(issue.getSeverity(), issue.getProblem(), issue.getConfig(), issue.getMessage(),
									Utils.TDI_VALIDATE_PROBLEM_MARKER);
						}
					}
				}
			} catch (final Exception e) {
				EclipseAppender.logerror(e.getMessage(), e);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							EclipseAppender.showError(Messages.getString("DiscoverSchemaWidget.13"), e, getShell());
						} catch (Exception e) {
							// Ignores, set focus operation that failed.
							SystemFunctions.doNothing();
						}
					}
				});
			} finally {
				action.setEnabled(true);
			}
			setProblemFocus();
		}

		/**
		 * Try to delete existing validation problem markers.
		 */
		private void deleteMarkers() {
			try {
				((TDIConfigurationFile) getEditingConfig().getMetamergeConfig()).getFile().deleteMarkers(
						Utils.TDI_VALIDATE_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				// Ignore the deletion of existing markers failed.
				SystemFunctions.doNothing();
			}
		}

		/**
		 * Try to set focus on the Problems View.
		 */
		private void setProblemFocus() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						IWorkbenchPage[] pages = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages();
						if (pages.length > 0) {
							pages[0].showView("org.eclipse.ui.views.ProblemView", null, IWorkbenchPage.VIEW_ACTIVATE);
						}
					} catch (Exception e) {
						// Ignores, set focus operation that failed.
						SystemFunctions.doNothing();
					}
				}
			});
		}
	}

	protected void updateTitleToolTip() {
		if(isInputMapMode() || isOutputMapMode()) {
			AttributeMapConfig map = ((ConnectorConfig)getEditingConfig()).getAttributeMap(isInputMapMode());
			String tt = labelProvider.getToolTipText(map);
			String inh = Utils.getInheritsFromExt(map);
			boolean nvb = labelProvider.hasNVB(map); 
			boolean inherited = (inh != null && !BaseConfiguration.INHERIT_PARENT.equals(inh));
			if(nvb || inherited)
				client.getTitleLabel().setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
			else
				client.getTitleLabel().setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			client.setTitleToolTipText(tt);
		}
	}

	private void addDragDropSupport() {

		dsl = new DragSourceAdapter() {
			private IStructuredSelection selection;

			@Override
			public void dragSetData(DragSourceEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
					LocalSelectionTransfer.getTransfer().setSelection(selection);
					event.data = selection;
					event.doit = true;
				}
			}

			@Override
			public void dragStart(DragSourceEvent event) {
				// Must catch the selection - otherwise it disappears on certain platforms
				selection = (IStructuredSelection) tree.getSelection();
				if(!selection.isEmpty())
					selection = new StructuredSelection(selection.toArray());
				event.data = selection; 
				super.dragStart(event);
			}
		};

		tree.addDragSupport(DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer() }, dsl);

		dtl = new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
					event.detail = DND.DROP_COPY;
				}
			}

			public void dragLeave(DropTargetEvent event) {
			}

			public void dragOperationChanged(DropTargetEvent event) {
			}

			public void dragOver(DropTargetEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
					if (event.item == null)
						event.feedback = DND.FEEDBACK_NONE | DND.FEEDBACK_EXPAND;
					else
						event.detail = DND.DROP_COPY;
				}
			}

			public void drop(DropTargetEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
					IStructuredSelection sel = (IStructuredSelection) event.data;
					Object target = null;
					if (event.item != null)
						target = ((TreeItem) event.item).getData();

					AttributeMapConfig amc = null;
					if (target == null && getEditingConfig() instanceof ConnectorConfig) {
						amc = ((ConnectorConfig) getEditingConfig()).getAttributeMap(isInputMapMode());
					} else if (target instanceof BaseConfiguration ) {
						BaseConfiguration bc = (BaseConfiguration) target;
						if (bc.getParameter("%%PLACEHOLDER%%") instanceof BaseConfiguration)
							bc = (BaseConfiguration) bc.getParameter("%%PLACEHOLDER%%");
						amc = Utils.getParentConfig(bc, AttributeMapConfig.class);
					}

					if (amc == null)
						return;

					AttributeMapItem dropOnItem = null;
					if (target instanceof AttributeMapItem && sel.size() == 1 && !isPlaceHolder(target))
						dropOnItem = (AttributeMapItem) target;

					amc.notifyChange(amc, "", MetamergeConfigChange.BEGIN_CHANGES);
					for (Iterator<?> i = sel.iterator(); i.hasNext();) {
						Object obj = i.next();
						if (obj instanceof SchemaItemConfig) {
							addAttributeMapItem(amc, (SchemaItemConfig) obj);

						} else if (obj instanceof AttributeMapItem) {
							if (dropOnItem != null && !Utils.isInputMap(amc))
								changeAttributeMapItem(amc, dropOnItem, (AttributeMapItem) obj);
							else
								addAttributeMapItem(amc, (AttributeMapItem) obj);

						} else if (obj instanceof AttributeMapConfig) {
							AttributeMapConfig source = (AttributeMapConfig) obj;
							for (String s : source.getAttributeNames()) {
								addAttributeMapItem(amc, source.getAttributeMapItem(s));
							}

						} else if (obj instanceof IFile && TDIConfigurationFile.XT_ATTRMAP.equalsIgnoreCase(((IFile)obj).getFileExtension())) {
							changeInheritance(amc, (IFile) obj);

						} else if (obj instanceof IFile && dropOnItem != null) {
							changeInheritance(dropOnItem, (IFile) obj);

						} else if (obj instanceof IFile && TDIConfigurationFile.XT_SCRIPT.equalsIgnoreCase(((IFile)obj).getFileExtension())) {
							IFile file = (IFile) obj;
							try {
								String name = file.getName();
								name = name.substring(0, name.length() - (file.getFileExtension().length()+1));
								int counter = 1;
								String attname = name;
								while(amc.hasAttributeMapItem(attname)) {
									attname = name + counter++;
								}
								AttributeMapItem ami = amc.newAttributeMapItem(attname);
								changeInheritance(ami, file, false);
							} catch (Exception e) {
								EclipseAppender.logerror(e.toString(), e, getShell());
							}
						}
					}
					amc.notifyChange(amc, "", MetamergeConfigChange.END_CHANGES);
				}
			}

			public void dropAccept(DropTargetEvent event) {
			}
		};
		tree.addDropSupport(DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer() }, dtl);
	}

	private void addAttributeMapItem(AttributeMapConfig amc, SchemaItemConfig sic) {
		String name = Utils.getScriptName(sic);
		if (amc.hasAttributeMapItem(name))
			return;

		try {
			AttributeMapItem ami = amc.newAttributeMapItem(name);
			if(amc.getParent() instanceof ALMappingConfig)
				ami.setScript(Utils.getScript("work", name));
			else
				ami.setSimple(name);
		} catch (Exception e) {
			EclipseAppender.logerror("", e);
		}
	}

	private boolean isInputMap(AttributeMapConfig amc) {
		if (amc != null && amc.getParent() instanceof ALMappingConfig)
			return false;
		return Utils.isInputMap(amc);
	}

	private void addAttributeMapItem(AttributeMapConfig amc, String name) {
		if (amc.hasAttributeMapItem(name))
			return;

		try {
			AttributeMapItem ami = amc.newAttributeMapItem(name);
			if(amc.getParent() instanceof ALMappingConfig)
				ami.setScript(Utils.getScript("work", name));
			else
				ami.setSimple(name);
		} catch (Exception e) {
			EclipseAppender.logerror("", e);
		}
	}

	public void addAttributeMapItem(AttributeMapConfig amc, AttributeMapItem source) {
		String name = source.getShortName();
		if (name == null || name.length() == 0 || amc.hasAttributeMapItem(name))
			return;

		AttributeMapConfig sourceMap = (AttributeMapConfig) Utils.getParentConfig(source, AttributeMapConfig.class);

		// Try to guess if we are copying from same type of map
		boolean input = isInputMap(amc);
		boolean same;
		if (! source.isAdvanced() ) {
			same = true;
		} else if (sourceMap != null) {
			same = (input == isInputMap(sourceMap));
		} else {
			String script = source.getScript();
			if (script == null) {
				same = true; //Cannot happen
			} else if (input) {
				same = script.indexOf("work.") < 0;
			} else {
				same = script.indexOf("conn.") < 0;				
			}
		}

		if ( same )
			amc.setAttributeMapItem(AttributeMapItemImpl.clone(source));
		else
			addAttributeMapItem(amc, name);
	}

	private void changeAttributeMapItem(AttributeMapConfig amc, AttributeMapItem target, AttributeMapItem source) {
		String name = source.getShortName();
		if (amc.hasAttributeMapItem(name))
			return;

		AttributeMapConfig sourceMap = (AttributeMapConfig) Utils.getParentConfig(source, AttributeMapConfig.class);
		if (!Utils.isInputMap(sourceMap)) {
			amc.setAttributeMapItem(AttributeMapItemImpl.clone(source));
		} else if (getEditor() != null){
			ChangeScriptOperation op = new ChangeScriptOperation(target, Utils.getScript("work", name));
			IUndoContext undoContext = getEditor().getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getUndoContext();
			op.addContext(undoContext);
			try {
				OperationHistoryFactory.getOperationHistory().execute(op, null, null);
			} catch (ExecutionException e) {
				SystemFunctions.doNothing();
			}
		} else {
			target.setScript(Utils.getScript("work", name));
		}
	}

	private void changeInheritance(AttributeMapItem ami, IFile file) {
		changeInheritance(ami, file, true);
	}

	private void changeInheritance(AttributeMapItem ami, IFile file, boolean prompt) {
		if(prompt && !MessageDialog.openConfirm(getShell(), Messages.getString("HooksWidget.0"), 
				Messages.getMessage("HooksWidget.1", file.getName())))
			return;

		try {
			ami.removeParameter(InternalSchema.AMI_SCRIPT);
			ami.setType(AttributeMapItem.ADVANCED_MAPPING);
			String internal = ((TDIConfigurationFile)ami.getMetamergeConfig()).addReference(file, null);
			ami.updateInheritsFrom(internal);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}

	}

	private void changeInheritance(AttributeMapConfig amc, IFile file) {
		if(!MessageDialog.openConfirm(getShell(), Messages.getString("HooksWidget.0"), 
				Messages.getMessage("HooksWidget.1", file.getName())))
			return;

		try {
			String internal = ((TDIConfigurationFile)amc.getMetamergeConfig()).addReference(file, null);
			amc.updateInheritsFrom(internal);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}

	}

	@Override
	public void setEditor(final BaseEditor editor) {
		super.setEditor(editor);

		if (editor != null) {
			editor.getSelectionProvider().addSelectionChangedListener(this);
			editor.addSelectionProvider(tree);

			//
			// -- Create a registered context menu on the tree control (for
			// object
			// contributions)
			//
			editor.registerContextMenu(tree, "#attributemaps"); //$NON-NLS-1$

			// -- Append the standard edit operations
			IAction action = editor.getActionFor(ActionFactory.DELETE.getId());
			if (action instanceof CutConfigAction)
				deleteAction = (CutConfigAction) action;
			else
				deleteAction = new CutConfigAction(Messages.getString("general.delete.label"), null);
			editor.getMenuManager().appendToGroup("group.edit", deleteAction);

			action = editor.getActionFor(ActionFactory.CUT.getId());
			if (action instanceof CutConfigAction)
				cutAction = (CutConfigAction) action;
			else
				cutAction = new CutConfigAction(Messages.getString("common.Cut.name"), null);
			editor.getMenuManager().appendToGroup("group.edit", cutAction);

			action = editor.getActionFor(ActionFactory.COPY.getId());
			if (action instanceof CopyConfigAction)
				copyAction = (CopyConfigAction) action;
			else
				copyAction = new CopyConfigAction(Messages.getString("common.Copy.name"));
			editor.getMenuManager().appendToGroup("group.edit", copyAction);

			pasteAction = new PasteAction();
			editor.getMenuManager().appendToGroup("group.edit", pasteAction);

			//
			// -- The paste action needs to be retargeted when we get focus in the tree,
			// -- and reset when we lose focus. 
			// Also, when we get focus, selection must be reset for delete, cut and copy actions.
			//
			tree.getTree().addFocusListener(new FocusListener() {
				private IAction saveAction;

				public void focusGained(FocusEvent e) {
					saveAction = editor.getActionFor(ActionFactory.PASTE.getId());
					editor.registerAction(ActionFactory.PASTE.getId(), pasteAction);
					cutAction.selectionChanged(editor, tree.getSelection());
					deleteAction.selectionChanged(editor, tree.getSelection());
					copyAction.selectionChanged(editor, tree.getSelection());
					editor.updateActionBars();
				}

				public void focusLost(FocusEvent e) {
					editor.registerAction(ActionFactory.PASTE.getId(), saveAction);
					editor.updateActionBars();
				}
			});

			// -- Add attribute?
			if(isInputMapMode() || isOutputMapMode()) {
				editor.getMenuManager().appendToGroup("group.tdi", new Action() {
					public String getText() {
						return Messages.getString("action.label.20");
					}
					public void run() {
						ISelection sel = getTree().getSelection();
						if(sel.isEmpty())
							sel = new StructuredSelection(getTree().getInput());
						AddAttributeMapItemAction ami = new AddAttributeMapItemAction();
						ami.selectionChanged(this, sel);
						ami.run(this);
					}
					@Override
					public String getActionDefinitionId() {
						return "com.ibm.tdi.rcp.attributemapconfig.addmapitem";
					}
				});
			}

		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
	}

	public TreeViewer getTree() {
		return tree;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (tree != null)
			tree.getTree().setEnabled(enabled);
	}

	private static class ODP extends OwnerDrawLabelProvider {

		private Image leftArrow = Activator.getImage("/icons/arrow-left_51x16.gif");
		private Image rightArrow = Activator.getImage("/icons/arrow-right_51x16.gif");
		private MapLabelProvider labelProvider;
		private TreeViewerColumn col;
		private int index;

		public ODP(TreeViewerColumn col, MapLabelProvider labelProvider) {
			this(col, labelProvider, 1);
		}
		public ODP(TreeViewerColumn col, MapLabelProvider labelProvider, int index) {
			this.labelProvider = labelProvider;
			this.col = col;
			this.index = index;
		}

		@Override
		protected void measure(Event event, Object element) {
			if (element instanceof AttributeMapConfig) {
				Point xt = new Point(col.getColumn().getWidth(), 16);
				event.setBounds(new Rectangle(event.x, event.y, xt.x, xt.y));
			} else {
				String str = labelProvider.getColumnText(element, index);
				if (str != null) {
					Point xt = event.gc.textExtent(removeNewLine(str));
					event.setBounds(new Rectangle(event.x, event.y, xt.x, xt.y));
				}
			}
		}

		@Override
		protected void paint(Event event, Object element) {
			if (element instanceof AttributeMapConfig) {
				// This should not happen
				if (leftArrow == null || rightArrow == null)
					return;

				int width = col.getColumn().getWidth();
				int pad = width - leftArrow.getBounds().width;
				if (pad > 0)
					pad = pad / 2;

				if (Utils.isInputMap((AttributeMapConfig) element))
					event.gc.drawImage(leftArrow, event.x + pad, event.y);
				else
					event.gc.drawImage(rightArrow, event.x + pad, event.y);
			} else {
				String str = labelProvider.getColumnText(element, index);
				Display display = col.getColumn().getDisplay();
				TextStyle plain = new TextStyle(JFaceResources
						.getFont(JFaceResources.DEFAULT_FONT),
						display.getSystemColor(SWT.COLOR_LIST_FOREGROUND),
						display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
				TextLayout layout = new TextLayout(display);
				str = removeNewLine(str);
				layout.setStyle(plain, 0, str.length());
				layout.setText(removeNewLine(str));
				layout.draw(event.gc, event.x + 5, event.y + 1);
			}
		}

	}

	private static String removeNewLine(String str) {
		// -- don't span more than one line in the tree view
		if (str == null)
			return "";
		int nl = str.indexOf("\n");
		if (nl == -1)
			return str;
		return str.substring(0, nl).trim() + "...";
	}

	private class PasteAction extends PasteConfigAction {

		AttributeMapConfig amc;

		public PasteAction() {
			super(Messages.getString("common.Paste.name")); //$NON-NLS-1$
		}

		@Override
		protected void performPaste(IStructuredSelection selection) {
			for (Iterator<?> i = selection.iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof SchemaItemConfig) {
					addAttributeMapItem(amc, (SchemaItemConfig) obj);
				} else if (obj instanceof AttributeMapItem) {
					addAttributeMapItem(amc, (AttributeMapItem) obj);
				} else if (obj instanceof AttributeMapConfig) {
					AttributeMapConfig source = (AttributeMapConfig) obj;
					for (String s : source.getAttributeNames()) {
						addAttributeMapItem(amc, source.getAttributeMapItem(s));
					}
				} else if (obj instanceof BaseConfiguration) {
					BaseConfiguration b = (BaseConfiguration) obj;
					addAttributeMapItem(amc, b.getShortName());
				}
			}
		}

		@Override
		protected boolean validatePaste(Object obj) {
			IStructuredSelection sel = (IStructuredSelection) tree.getSelection();
			if (!sel.isEmpty()) {
				amc = Utils.getParentConfig(sel.getFirstElement(), AttributeMapConfig.class);
				if (amc == null && sel.getFirstElement() instanceof BaseConfiguration) {
					BaseConfiguration bc = (BaseConfiguration) sel.getFirstElement();
					if (bc.getParameter("%%PLACEHOLDER%%") instanceof BaseConfiguration)
						amc = Utils.getParentConfig(bc.getParameter("%%PLACEHOLDER%%"), AttributeMapConfig.class);
				}
				if (amc == null && getEditingConfig() instanceof ConnectorConfig) {
					amc = ((ConnectorConfig)getEditingConfig()).getAttributeMap(isInputMapMode());
				}
				if (amc == null)
					return false;
			} else  {
				Object o = tree.getInput();
				if (o instanceof AttributeMapConfig)
					amc = (AttributeMapConfig) o;
				else
					return false;
			}
			boolean valid = (obj instanceof SchemaItemConfig || obj instanceof AttributeMapItem || obj instanceof AttributeMapConfig);
			if (!valid && obj instanceof BaseConfiguration) {
				BaseConfiguration b = (BaseConfiguration) obj;
				if (b.getShortName() != null) {
					valid = true;
				}
			}
			return valid;
		}
	}

	/**
	 * This editor enables for input AttributeMapItem objects. A simple text box
	 * is provided to enter the new work.<attribute> name.
	 * 
	 */
	private class WorkAttributeEditingSupport extends EditingSupport {

		private TextCellEditor tce;

		public WorkAttributeEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		protected boolean canEdit(Object element) {
			// -- Enable direct editing of target work attribute name
			if (element instanceof AttributeMapItem) {
				AttributeMapItem ami = (AttributeMapItem) element;
				AttributeMapConfig amc = Utils.getParentConfig(ami, AttributeMapConfig.class);
				return (amc != null && Utils.isInputMap(amc));
			}
			return false;
		}

		protected CellEditor getCellEditor(Object element) {
			if (tce == null) {
				tce = new TextCellEditor((Composite) getViewer().getControl());
				tce.getControl().addFocusListener(new SetDefaultHandlers(getEditor()));
			}
			return tce;
		}

		protected Object getValue(Object element) {
			return ((AttributeMapItem) element).getShortName();
		}

		protected void setValue(Object element, Object value) {
			try {
				AttributeMapItem ami = (AttributeMapItem) element;
				AttributeMapConfig amc = Utils.getParentConfig(ami, AttributeMapConfig.class);
				if (amc == null)
					return;

				if(value == null || value.toString().trim().length() == 0)
					return;

				if (ami.getShortName().equals(value.toString()))
					return;
				else
					amc.renameAttributeMapItem(value.toString(), ami);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getViewer().getControl().getShell());
			}
		}
	}

	/**
	 * This editor enables for output AttributeMapItem objects. A combo box is
	 * shown with all known attribute names from the output schema.
	 * 
	 * If the target attribute is not in the known list it is added to the combo
	 * box as a valid selection (due to the way the combobox editor works).
	 * 
	 * This editor will only change the name of the target attribute but not add
	 * unknown attributes to the current schema.
	 * 
	 */
	private class ConnAttributeEditingSupport extends EditingSupport {
		private ComboBoxCellEditor cbe;

		public ConnAttributeEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		protected boolean canEdit(Object element) {
			// -- Enable direct editing of target conn attribute name
			if (element instanceof AttributeMapItem) {
				AttributeMapItem ami = (AttributeMapItem) element;
				AttributeMapConfig amc = Utils.getParentConfig(ami, AttributeMapConfig.class);
				return (amc != null && !Utils.isInputMap(amc));
			}
			return false;
		}

		protected CellEditor getCellEditor(Object element) {
			if (cbe == null) {
				cbe = new ComboBoxCellEditor((Composite) getViewer().getControl(), new String[] {}, SWT.DROP_DOWN) {
					private CCombo combo;

					protected Object doGetValue() {
						Object value = super.doGetValue();
						if (value instanceof Integer && ((Integer) value) == -1)
							value = combo.getText();
						return value;
					}

					protected Control createControl(Composite parent) {
						combo = (CCombo) super.createControl(parent);
						combo.addKeyListener(new KeyListener() {
							public void keyPressed(KeyEvent e) {
								if(e.keyCode == SWT.DEL) {
									// -- SWT again ... doesn't understand DEL button ...

									//Select the next char if nothing selected
									Point p = combo.getSelection();
									String s = combo.getText();
									if (p != null && p.x == p.y && s != null && p.x < s.length()) {
										p.y = p.x + 1;
										combo.setSelection(p);
									}

									combo.cut();
									e.doit = false;
								}
							}
							public void keyReleased(KeyEvent e) {
								if(e.keyCode == SWT.DEL) {
									e.doit = false;
								}
							}
						});
						return combo;
					}
				};
				cbe.getControl().addFocusListener(new SetDefaultHandlers(getEditor()));
			}
			List<String> items = getValidItems(element);
			cbe.setItems(items.toArray(new String[0]));
			return cbe;
		}

		private List<String> getValidItems(Object element) {
			AttributeMapItem ami = (AttributeMapItem) element;
			List<String> items = new ArrayList<String>();
			items.add(ami.getShortName());
			ConnectorConfig cc = Utils.getParentConfig(ami, ConnectorConfig.class);
			if (cc == null)
				return items;

			AttributeMapConfig amc = cc.getAttributeMap(false);
			for (String name : cc.getSchema(false).getItemNames()) {
				if (!amc.hasAttributeMapItem(name))
					items.add(name);
			}

			return items;
		}

		protected Object getValue(Object element) {
			List<String> items = getValidItems(element);
			return items.indexOf(((AttributeMapItem) element).getShortName());
		}

		protected void setValue(Object element, Object value) {
			try {
				AttributeMapItem ami = (AttributeMapItem) element;
				String newval = null;
				if (value instanceof Integer) {
					newval = getValidItems(element).get((Integer) value);
				} else {
					newval = value.toString();
				}

				if(newval == null || newval.trim().length() == 0)
					return;

				AttributeMapConfig amc = Utils.getParentConfig(ami, AttributeMapConfig.class);
				if (amc == null)
					return;

				if (ami.getShortName().equals(newval))
					return;
				else
					amc.renameAttributeMapItem(newval, ami);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getViewer().getControl().getShell());
			}
		}
	}
	private static class SetDefaultHandlers implements FocusListener {
		private String[] defaultHandlers = new String [] {
				ActionFactory.CUT.getId(),
				ActionFactory.COPY.getId(),
				ActionFactory.PASTE.getId(),
				ActionFactory.DELETE.getId(),
		};
		BaseEditor editor;

		public SetDefaultHandlers(BaseEditor editor) {
			this.editor = editor;
		}

		public void focusGained(FocusEvent e) {
			IActionBars bars = editor.getEditorSite().getActionBars();
			if (bars != null) {
				// Install default handlers (remove special handlers)
				for (String handler: defaultHandlers) {
					bars.setGlobalActionHandler(handler, null);
				}
			}
		}

		public void focusLost(FocusEvent e) {
			// Restore the special handlers
			editor.updateActionBars();			
		}
	}

	private static class ChangeScriptOperation extends AbstractOperation {
		@SuppressWarnings("unused") //$NON-NLS-1$
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		private final AttributeMapItem config;
		private final String newScript;
		private final String oldScript;
		private final String oldType;

		public ChangeScriptOperation(AttributeMapItem config, String script) {
			super("ChangeScriptOperation");
			this.config = config;
			newScript = script;
			oldScript = config.getScript();
			oldType = config.getType();
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return redo(monitor, info);
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			config.setScript(newScript);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			if (oldScript == null)
				config.removeParameter(InternalSchema.AMI_SCRIPT);
			else
				config.setScript(oldScript);
			config.setType(oldType);
			return Status.OK_STATUS;
		}
	}

}
