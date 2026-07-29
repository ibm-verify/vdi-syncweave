/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchCondition;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.xml.BranchingFactory;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.PasteConfigAction;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.CheckboxLabelProvider;
import com.ibm.tdi.eclipse.providers.WorkEntryAttributesProvider;

public class BranchConditionWidget extends BaseWidget {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// This array must match the next
	private final static String[] operator_values = new String[] { BranchCondition.BRANCH_EQUALS, BranchCondition.BRANCH_CONTAINS,
			BranchCondition.BRANCH_STARTS_WITH, BranchCondition.BRANCH_ENDS_WITH, BranchCondition.BRANCH_HAS_VALUE,
			BranchCondition.BRANCH_EXISTS, BranchCondition.BRANCH_LT, BranchCondition.BRANCH_LTE, BranchCondition.BRANCH_GT,
			BranchCondition.BRANCH_GTE };

	private final static String[] operators = new String[] { Messages.getString("BranchingConfig.Conditions.equals"), //$NON-NLS-1$
			Messages.getString("BranchingConfig.Conditions.contains"), //$NON-NLS-1$
			Messages.getString("BranchingConfig.Conditions.startsWith"), //$NON-NLS-1$
			Messages.getString("BranchingConfig.Conditions.endsWith"), //$NON-NLS-1$
			Messages.getString("BranchingConfig.Conditions.hasValue"), //$NON-NLS-1$
			Messages.getString("BranchingConfig.Conditions.exists"), //$NON-NLS-1$
			Messages.getString("BranchingConfig.Conditions.less"), //$NON-NLS-1$
			Messages.getString("BranchingConfig.Conditions.lessequal"), //$NON-NLS-1$
			Messages.getString("BranchingConfig.Conditions.greater"), //$NON-NLS-1$
			Messages.getString("BranchingConfig.Conditions.greaterequal") //$NON-NLS-1$
	};

	private final static String SCRIPT_DELETED = BranchingFactory.SCRIPT_DELETED;
	private SimpleTextEditor linkEditor;

	private TableViewer table;

	private ArrayList<Action> actions;

	private SashForm splitPanel;

	private Action pasteAction = new PasteAction();

	private MetamergeConfigChangeListener listener;

	public BranchConditionWidget(BranchingConfig config, Composite parent, int style) {
		super(parent, style, config);
		setLayout(new GridLayout(1, false));
		createUI(this);
	}

	private BranchingConfig getBC() {
		return (BranchingConfig) getEditingConfig();
	}

	private void createUI(Composite parent) {
		createHeader().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		splitPanel = new SashForm(this, SWT.VERTICAL);
		splitPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

		// -- Conditions table
		createTable(splitPanel).getControl();

		// -- Scripted condition
		linkEditor = new SimpleTextEditor(splitPanel, SWT.NONE, getEditingConfig());
		String current = getBC().getScript();
		if (current == null || current.length() == 0)
			current = "return true";

		linkEditor.setText(current);
		linkEditor.getDocument().addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
			}

			public void documentChanged(DocumentEvent event) {
				getBC().setScript(event.getDocument().get());
			}
		});

		// -- Only show scripted condition if there is data in the script
		String script = getBC().getScript();
		if (script != null && script.length() > 0 && !getBC().getBooleanParameter(SCRIPT_DELETED, false))
			splitPanel.setWeights(new int[] { 50, 50 });
		else
			splitPanel.setWeights(new int[] { 100, 0 });

		listener = new MetamergeConfigChangeListener() {
			public void configurationChanged(MetamergeConfigChange mcc) {
				if ("ComponentList".equals(mcc.getKey()) && table.getContentProvider() != null)
					table.setInput(getBC());
			}
		};
		getBC().getConditions().addListener(listener);
	}

	private void createActions() {
		actions = new ArrayList<Action>();
		Action action = new Action() {
			public void run() {
				BranchCondition condition = getBC().newCondition();
				condition.setLeftHand("");
				condition.setOperator(BranchCondition.BRANCH_EQUALS);
				condition.setRightHand("");
				getBC().getConditions().addConfig(condition);
				if (getBC().getConditions().size() == 1)
					getEditingConfig().notifyChange(getEditingConfig(), "", MetamergeConfigChange.MCC_MODIFY);
				table.refresh(true);
			}

			public String getText() {
				return Messages.getString("BranchingConfig.Add.label");
			}

			public String getToolTipText() {
				return Messages.getString("BranchingConfig.Add.tooltip");
			}
		};
		actions.add(action);

		action = new Action() {
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) table.getSelection();
				for (Object obj : sel.toArray()) {
					getBC().getConditions().removeConfig((BaseConfiguration) obj);
				}
				if (getBC().getConditions().size() == 0)
					getEditingConfig().notifyChange(getEditingConfig(), "", MetamergeConfigChange.MCC_MODIFY);
				table.refresh();
			}

			public String getText() {
				return Messages.getString("BranchingConfig.Remove.label");
			}

			public String getToolTipText() {
				return Messages.getString("BranchingConfig.Remove.tooltip");
			}
		};
		actions.add(action);

		action = new Action() {
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) table.getSelection();
				if (sel.size() == 1) {
					getBC().getConditions().moveConfig((BaseConfiguration) sel.getFirstElement(), true);
					table.refresh();
				}
			}

			public String getText() {
				return Messages.getString("general.moveup.label");
			}

			public String getToolTipText() {
				return Messages.getString("general.moveup.tooltip");
			}
		};
		actions.add(action);

		action = new Action() {
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) table.getSelection();
				if (sel.size() == 1) {
					getBC().getConditions().moveConfig((BaseConfiguration) sel.getFirstElement(), false);
					table.refresh();
				}
			}

			public String getText() {
				return Messages.getString("general.movedown.label");
			}

			public String getToolTipText() {
				return Messages.getString("general.movedown.tooltip");
			}
		};
		actions.add(action);

		action = new Action() {
			public void run() {
				if (splitPanel.getWeights()[1] == 0) {
					splitPanel.setWeights(new int[] { 50, 50 });
					getBC().removeParameter(SCRIPT_DELETED);
				} else {
					splitPanel.setWeights(new int[] { 100, 0 });
					getBC().setBooleanParameter(SCRIPT_DELETED, true);
				}
			}

			public String getText() {
				return Messages.getString("BaseEditor.21");
			}

			public String getToolTipText() {
				return Messages.getString("Localized.advanced");
			}
		};
		actions.add(action);
	}

	private Composite createHeader() {
		Composite toolbar = new Composite(this, SWT.NONE);
		toolbar.setLayout(new GridLayout(6, false));

		// -- Create action handlers
		createActions();

		// -- Create a button for each action
		for (Action action : actions) {
			Button item = new Button(toolbar, SWT.PUSH);
			item.setText(action.getText());
			item.setToolTipText(action.getToolTipText());
			item.setData("action", action);
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					((Action) e.widget.getData("action")).run();
				}
			});
		}

		Button check = new Button(toolbar, SWT.CHECK);
		check.setText(Messages.getString("BranchingConfig.matchall.label"));
		check.setToolTipText(Messages.getString("BranchingConfig.matchall.tooltip"));
		check.setSelection(!getBC().getMatchAny());
		check.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getBC().setMatchAny(!getBC().getMatchAny());
				table.refresh(true);
			}
		});

		// 

		return toolbar;
	}

	private TableViewer createTable(Composite parent) {
		table = new TableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
		TableViewerColumn tvc;

		// -- And/Or Column
		tvc = new TableViewerColumn(table, SWT.LEFT);
		tvc.getColumn().setText("");
		tvc.getColumn().setWidth(30);
		tvc.setLabelProvider(new CellLabelProvider() {
			public void update(ViewerCell cell) {
				if (getBC().getConditions().indexOf((BaseConfiguration) cell.getElement()) == 0)
					cell.setText("");
				else if (getBC().getMatchAny())
					cell.setText("|");
				else
					cell.setText("+");
			}
		});

		// -- Attribute name
		tvc = new TableViewerColumn(table, SWT.LEFT);
		tvc.getColumn().setText(Messages.getString("LinkCriteriaUI.0.label"));
		tvc.getColumn().setToolTipText(Messages.getString("BranchConditionWidget.attribute.tooltip"));
		tvc.getColumn().setWidth(100);
		tvc.setLabelProvider(new CellLabelProvider() {
			public void update(ViewerCell cell) {
				BranchCondition bc = (BranchCondition) cell.getElement();
				cell.setText(bc.getLeftHand() == null ? "" : bc.getLeftHand());
			}
		});

		tvc.setEditingSupport(new EditingSupport(table) {
			private ArrayList<String> values;

			protected boolean canEdit(Object element) {
				return element instanceof BranchCondition;
			}

			protected CellEditor getCellEditor(Object element) {
				BranchCondition bc = (BranchCondition) element;
				CBEditor ed = new CBEditor((Composite) getViewer().getControl());

				values = new ArrayList<String>();

				AssemblyLineConfig alc = (AssemblyLineConfig) Utils.getParentConfig(getBC(), AssemblyLineConfig.class);
				if (alc != null) {
					WorkEntryAttributesProvider wep = new WorkEntryAttributesProvider(getBC().getShortName());
					wep.inputChanged(null, null, alc);
					for (Object name : wep.getChildren(alc)) {
						String value = "" + name; //$NON-NLS-1$
						values.add(value);
					}
				}

				if (bc.getLeftHand() != null && !values.contains(bc.getLeftHand()))
					values.add(0, bc.getLeftHand());

				ed.setItems(values.toArray(new String[0]));

				if (getEditor() != null && getEditor().getSite() != null) {
					Object o = getEditor().getSite().getService(IFocusService.class);
					if (o instanceof IFocusService) {
						((IFocusService) o).addFocusTracker(ed.getCombo(), "com.ibm.tdi.text.control");
					}
				}
				
				return ed;
			}

			protected Object getValue(Object element) {
				BranchCondition bc = (BranchCondition) element;
				String str = bc.getLeftHand();
				return Integer.valueOf(values.indexOf(str));
			}

			protected void setValue(Object element, Object value) {
				BranchCondition bc = (BranchCondition) element;
				String str;
				if (value instanceof Integer) {
					str = values.get((Integer) value);
				} else {
					str = value.toString();
				}
				if (!str.equals(bc.getLeftHand())) {
					bc.setLeftHand(str);
					getViewer().refresh(bc, true);
				}
			}
		});

		// -- Not
		tvc = new TableViewerColumn(table, SWT.LEFT);
		tvc.getColumn().setText(Messages.getString("BranchConditionWidget.not.label"));
		tvc.getColumn().setToolTipText(Messages.getString("BranchConditionWidget.not.tooltip"));
		tvc.getColumn().setWidth(50);
		tvc.setLabelProvider(new CheckboxLabelProvider(table) {
			@Override
			protected boolean isChecked(Object element) {
				BranchCondition bc = (BranchCondition) element;
				return Boolean.valueOf(bc.getNegate());
			}

			@Override
			public String getText(Object element) {
				return "";
			}
		});

		tvc.setEditingSupport(new EditingSupport(table) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new CheckboxCellEditor((Composite) getViewer().getControl());
			}

			@Override
			protected Object getValue(Object element) {
				BranchCondition bc = (BranchCondition) element;
				return Boolean.valueOf(bc.getNegate());
			}

			@Override
			protected void setValue(Object element, Object value) {
				BranchCondition bc = (BranchCondition) element;
				bc.setNegate((Boolean) value);
				getViewer().refresh(bc, true);
			}

		});

		// -- Operator
		tvc = new TableViewerColumn(table, SWT.LEFT);
		tvc.getColumn().setText(Messages.getString("LinkCriteriaUI.1.label"));
		tvc.getColumn().setToolTipText(Messages.getString("BranchConditionWidget.operator.tooltip"));
		tvc.getColumn().setWidth(100);
		tvc.setLabelProvider(new CellLabelProvider() {
			public void update(ViewerCell cell) {
				BranchCondition bc = (BranchCondition) cell.getElement();
				String value = (bc.getOperator() == null ? "" : bc.getOperator());
				cell.setText(value);
				for (int i = 0; i < operator_values.length; i++) {
					if (operator_values[i].equals(value))
						cell.setText(operators[i]);
				}
			}
		});

		tvc.setEditingSupport(new EditingSupport(table) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				return new ComboBoxCellEditor((Composite) table.getControl(), operators, SWT.READ_ONLY);
			}

			@Override
			protected Object getValue(Object element) {
				BranchCondition bc = (BranchCondition) element;
				for (int i = 0; i < operator_values.length; i++) {
					if (operator_values[i].equals(bc.getOperator()))
						return Integer.valueOf(i);
				}
				return Integer.valueOf(-1);
			}

			@Override
			protected void setValue(Object element, Object value) {
				BranchCondition bc = (BranchCondition) element;
				bc.setOperator(operator_values[(Integer) value]);
				getViewer().refresh(bc, true);
			}

		});

		// -- Value
		tvc = new TableViewerColumn(table, SWT.LEFT);
		tvc.getColumn().setText(Messages.getString("LinkCriteriaUI.2.label"));
		tvc.getColumn().setToolTipText(Messages.getString("BranchConditionWidget.value.tooltip"));
		tvc.getColumn().setWidth(150);
		tvc.setLabelProvider(new CellLabelProvider() {
			public void update(ViewerCell cell) {
				BranchCondition bc = (BranchCondition) cell.getElement();
				cell.setText(bc.getRightHand() == null ? "" : bc.getRightHand());
			}
		});

		tvc.setEditingSupport(new EditingSupport(table) {
			private ArrayList<String> values;

			protected boolean canEdit(Object element) {
				return element instanceof BranchCondition;
			}

			protected CellEditor getCellEditor(Object element) {
				BranchCondition bc = (BranchCondition) element;
				CBEditor ed = new CBEditor((Composite) getViewer().getControl());
				final BaseConfiguration config = (BaseConfiguration) element;
				ed.getCombo().addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if ((e.stateMask & SWT.CONTROL) == SWT.CONTROL && e.keyCode == ' ') {
							String str = ParameterSubstitutionWidget.openPSDialog(getShell(), config,
									InternalSchema.BRANCH_CONDITION_RIGHT);
							if (str != null) {
								config.setParameterPropertySource(InternalSchema.BRANCH_CONDITION_RIGHT, str);
								getViewer().refresh(true);
							}
						}
					}
				});

				values = new ArrayList<String>();

				AssemblyLineConfig alc = (AssemblyLineConfig) Utils.getParentConfig(getBC(), AssemblyLineConfig.class);
				if (alc != null) {
					WorkEntryAttributesProvider wep = new WorkEntryAttributesProvider(getBC().getShortName());
					wep.inputChanged(null, null, alc);
					for (Object name : wep.getChildren(alc)) {
						String value = "$" + name; //$NON-NLS-1$
						values.add(value);
					}
				}

				if (bc.getRightHand() != null && !values.contains(bc.getRightHand()))
					values.add(0, bc.getRightHand());

				ed.setItems(values.toArray(new String[0]));

				if (getEditor() != null && getEditor().getSite() != null) {
					Object o = getEditor().getSite().getService(IFocusService.class);
					if (o instanceof IFocusService) {
						((IFocusService) o).addFocusTracker(ed.getCombo(), "com.ibm.tdi.text.control");
					}
				}

				return ed;
			}

			protected Object getValue(Object element) {
				BranchCondition bc = (BranchCondition) element;
				String str = bc.getRightHand();
				return Integer.valueOf(values.indexOf(str));
			}

			protected void setValue(Object element, Object value) {
				BranchCondition bc = (BranchCondition) element;
				String str;
				if (value instanceof Integer) {
					str = values.get((Integer) value);
				} else {
					str = value.toString();
				}
				if (!str.equals(bc.getRightHand())) {
					bc.setRightHand(str);
					getViewer().refresh(bc, true);
				}
			}
		});

		// -- Case Sensitive
		tvc = new TableViewerColumn(table, SWT.LEFT);
		tvc.getColumn().setText(Messages.getString("BranchWidget.10"));
		tvc.getColumn().setToolTipText(Messages.getString("BranchConditionWidget.case.tooltip"));
		tvc.getColumn().setWidth(120);
		tvc.setLabelProvider(new CheckboxLabelProvider(table) {
			@Override
			protected boolean isChecked(Object element) {
				BranchCondition bc = (BranchCondition) element;
				return Boolean.valueOf(bc.getCaseSensitive());
			}

			@Override
			public String getText(Object element) {
				return "";
			}
		});

		tvc.setEditingSupport(new EditingSupport(table) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new CheckboxCellEditor((Composite) getViewer().getControl());
			}

			@Override
			protected Object getValue(Object element) {
				BranchCondition bc = (BranchCondition) element;
				return Boolean.valueOf(bc.getCaseSensitive());
			}

			@Override
			protected void setValue(Object element, Object value) {
				BranchCondition bc = (BranchCondition) element;
				bc.setCaseSensitive((Boolean) value);
				getViewer().refresh(bc, true);
			}

		});

		// -- Match any
		tvc = new TableViewerColumn(table, SWT.LEFT);
		tvc.getColumn().setText(Messages.getString("BranchingConfig.matchany.label"));
		tvc.getColumn().setToolTipText(Messages.getString("BranchConditionWidget.matchany.tooltip"));
		tvc.getColumn().setWidth(120);
		tvc.setLabelProvider(new CheckboxLabelProvider(table) {
			@Override
			protected boolean isChecked(Object element) {
				BranchCondition bc = (BranchCondition) element;
				return Boolean.valueOf(bc.getMatchAny());
			}

			@Override
			public String getText(Object element) {
				return "";
			}
		});

		tvc.setEditingSupport(new EditingSupport(table) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new CheckboxCellEditor((Composite) getViewer().getControl());
			}

			@Override
			protected Object getValue(Object element) {
				BranchCondition bc = (BranchCondition) element;
				return Boolean.valueOf(bc.getMatchAny());
			}

			@Override
			protected void setValue(Object element, Object value) {
				BranchCondition bc = (BranchCondition) element;
				bc.setMatchAny((Boolean) value);
				getViewer().refresh(bc, true);
			}

		});

		table.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return getBC().getConditions().getConfigurations(null).toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		table.getTable().setHeaderVisible(true);
		table.setInput(getBC());

		if (getEditor() != null) {
			table.getTable().addFocusListener(new FocusListener() {
				private IAction saveAction;

				public void focusGained(FocusEvent e) {
					saveAction = getEditor().getActionFor(ActionFactory.PASTE.getId());
					getEditor().registerAction(ActionFactory.PASTE.getId(), pasteAction);
					getEditor().updateActionBars();
				}

				public void focusLost(FocusEvent e) {
					getEditor().registerAction(ActionFactory.PASTE.getId(), saveAction);
					getEditor().updateActionBars();
				}
			});
		}

		return table;
	}

	private static class CBEditor extends ComboBoxCellEditor {
		private CCombo combo;

		public CBEditor(Composite parent) {
			super(parent, new String[] {}, SWT.NONE);
		}

		protected Object doGetValue() {
			Object value = super.doGetValue();
			if (value instanceof Integer && ((Integer) value) == -1)
				value = combo.getText();
			return value;
		}

		public CCombo getCombo() {
			return combo;
		}

		protected Control createControl(Composite parent) {
			combo = (CCombo) super.createControl(parent);
			return combo;
		}
	}

	@Override
	public void setEditor(BaseEditor editor) {
		super.setEditor(editor);
		editor.registerContextMenu(table, "conditionsTable");
		// -- Append the standard edit operations
		if (editor.getActionFor(ActionFactory.DELETE.getId()) != null)
			editor.getMenuManager().appendToGroup("group.edit", editor.getActionFor(ActionFactory.DELETE.getId()));
		if (editor.getActionFor(ActionFactory.CUT.getId()) != null)
			editor.getMenuManager().appendToGroup("group.edit", editor.getActionFor(ActionFactory.CUT.getId()));
		if (editor.getActionFor(ActionFactory.COPY.getId()) != null)
			editor.getMenuManager().appendToGroup("group.edit", editor.getActionFor(ActionFactory.COPY.getId()));

		for (Action action : actions) {
			editor.getMenuManager().appendToGroup("group.tdi", action);
		}

		editor.addSelectionProvider(table);
	}

	private class PasteAction extends PasteConfigAction {

		public PasteAction() {
			super(Messages.getString("common.Paste.name")); //$NON-NLS-1$
		}

		@Override
		protected void performPaste(IStructuredSelection selection) {
			for (Iterator<?> i = selection.iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof BranchCondition) {
					try {
						getBC().getConditions().addConfig((BaseConfiguration) ((BranchCondition) obj).getClone());
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getShell());
					}
				}
				table.refresh();
			}
		}

		@Override
		protected boolean validatePaste(Object obj) {
			return obj instanceof BranchCondition;
		}
	}

	@Override
	public void dispose() {
		if (getBC() != null && getBC().getConditions() != null && listener != null) {
			getBC().getConditions().removeListener(listener);
		}
		super.dispose();
	}
}
