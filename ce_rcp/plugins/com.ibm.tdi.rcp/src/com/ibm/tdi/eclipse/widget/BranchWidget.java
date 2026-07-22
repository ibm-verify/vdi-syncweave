/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchCondition;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.WorkEntryAttributesProvider;
import com.ibm.tdi.eclipse.util.TDIToolBar;

/**
 * This class provides a UI to edit branch configurations. All IF branches and Switch/Case
 * branches are provided for.
 */
public class BranchWidget extends BaseWidget {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static String[] IF_LABELS = new String[]{
		Messages.getString("Branch.Type.IF"), //$NON-NLS-1$
		Messages.getString("Branch.Type.ELSEIF"), //$NON-NLS-1$		
		Messages.getString("Branch.Type.ELSE"),	//$NON-NLS-1$
	};
	
	/**
	 * 
	 */
	private SimpleTextEditor caseEditor;

	private Composite stack;

	private Control branchWidget;

	private BranchingConfig config;

	private BranchCondition switchCondition;

	private Button workAttribute;

	private Combo workCombo;

	private Button alOps;

	private Button workOps;

	private Button userDefined;

	private SimpleTextEditor userText;
	
	TDIToolBar toolBar;
	
	/**
	 * Remember this action to make it easy to enable/disable it.
	 */
	private Action defaultCaseAction;

	public BranchWidget(Composite parent, int style, BaseConfiguration editingConfig) {
		this(parent, style, editingConfig, null);
	}
	
	public BranchWidget(Composite parent, int style, BaseConfiguration editingConfig, BaseEditor editor) {
		super(parent, style, editingConfig, editor);
		this.config = (BranchingConfig) editingConfig;
		setLayout(new FillLayout());
		if(editingConfig instanceof LoopConfig && ((LoopConfig)editingConfig).getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
			createUI(this);
		} else {
			createForm(this, null).getBody().setLayout(new FillLayout());
			toolBar = new TDIToolBar(getForm());
			createUI(getForm().getBody());
		}
	}

	protected void createUI(Composite parent) {
		stack = new Composite(parent, SWT.FILL);
		stack.setLayout(new StackLayout());
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		setActiveControl(createComponent());
		if (getToolbar() != null)
			getToolbar().setImage(getEditingConfig());
	}

	protected Control createComponent() {
		return createComponent(stack);
	}

	protected void setActiveControl(Control c) {
		((StackLayout) stack.getLayout()).topControl = c;
		stack.layout();
	}

	protected Control getActiveControl() {
		return ((StackLayout) stack.getLayout()).topControl;
	}

	public TDIToolBar getToolbar() {
		return toolBar;
	}

	protected Control createComponent(Composite parent) {

		if (branchWidget != null)
			return branchWidget;

		BranchingConfig bc = (BranchingConfig) getEditingConfig();
		switch (bc.getBranchType()) {
		case BranchingConfig.BRANCH_IF:
		case BranchingConfig.BRANCH_ELSEIF:
			getToolbar().setText(IF_LABELS[bc.getBranchType()]);
			branchWidget = new BranchConditionWidget(bc, parent, 0);
			if (! (bc instanceof LoopConfig))
				addBranchType();
			break;
		case BranchingConfig.BRANCH_ELSE:
			getToolbar().setText(IF_LABELS[BranchingConfig.BRANCH_ELSE]);
			branchWidget = new Composite(parent, SWT.NONE);
			((Composite)branchWidget).setLayout(new FillLayout());
			((Composite)branchWidget).setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			addBranchType();
			break;
		case BranchingConfig.BRANCH_SWITCH:
			getToolbar().setText(Messages.getString("BranchWidget.22")); //$NON-NLS-1$
			branchWidget = createSwitchBranch(parent);
			break;
		case BranchingConfig.BRANCH_CASE:
			getToolbar().setText(Messages.getString("BranchWidget.23")); //$NON-NLS-1$
			branchWidget = createCaseBranch(parent);
			break;
		}

		if(getToolbar() != null) {
			getToolbar().add(new Action() {
				public String getText() {
					return Messages.getString("LBL.CLOSE"); //$NON-NLS-1$
				}
	
				@Override
				public String getActionDefinitionId() {
					return "com.ibm.tdi.rcp.quickeditor.close"; //$NON-NLS-1$
				}
	
				public void run() {
					getEditor().quickEdit(new StructuredSelection(getEditingConfig()));
				}
				
			});
		}
		
		return branchWidget;
	}

	private void addBranchType() {
		final Combo combo = getToolbar().addCombo(SWT.DROP_DOWN|SWT.READ_ONLY);
		for(String str : IF_LABELS)
			combo.add(str);
		combo.select(getBC().getBranchType());
		combo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				BranchingConfig bc = getBC();
				int old = bc.getBranchType();
				bc.setBranchType(combo.getSelectionIndex());
				if (old == bc.getBranchType())
					return; // No change
				getToolbar().setText(IF_LABELS[bc.getBranchType()]);
				if (old == BranchingConfig.BRANCH_ELSE) {
					branchWidget.dispose();
					branchWidget = new BranchConditionWidget(bc, stack, 0);
					setActiveControl(branchWidget);
				} else if (bc.getBranchType() == BranchingConfig.BRANCH_ELSE) {
					branchWidget.dispose();
					branchWidget = new Composite(stack, SWT.NONE);
					((Composite)branchWidget).setLayout(new FillLayout());
					((Composite)branchWidget).setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));					
					setActiveControl(branchWidget);
				}
			}
		});
	}

	public BranchingConfig getBC() {
		return (BranchingConfig) getEditingConfig();
	}

	public BranchCondition getCondition() {
		ContainerConfig cc = getBC().getConditions();
		if (cc.size() > 0)
			return (BranchCondition) cc.getConfig(0);
		BranchCondition bc = getBC().newCondition();
		cc.addConfig(bc);
		return bc;
	}

	public void updateMatchAll(SelectionEvent event) {
		getBC().setMatchAny(((Button) event.widget).getSelection());
	}

	private Control createCaseBranch(Composite parent) {
		caseEditor = new SimpleTextEditor(parent, SWT.BORDER, null, false);
		caseEditor.setText(getCondition().getRightHand());
		caseEditor.getSourceViewer().addTextListener(new ITextListener() {
			public void textChanged(TextEvent event) {
				getCondition().setRightHand(caseEditor.getText());
			}
		});
		return caseEditor;
	}

	private Control createSwitchBranch(Composite parent) {
		getToolbar().setText(Messages.getString("BranchWidget.26")); //$NON-NLS-1$
		getToolbar().add(new Action() {
			public String getText() {
				return Messages.getString("AddCaseUI.label"); //$NON-NLS-1$
			}

			public void run() {
				addCaseComponents();
			}
		});
		
		defaultCaseAction = new Action() {
			public String getText() {
				return Messages.getString("SwitchedBranchConfigUI.DefaultCase.add.label"); //$NON-NLS-1$
			}

			public String getToolTipText() {
				return Messages.getString("SwitchedBranchConfigUI.DefaultCase.add.tooltip"); //$NON-NLS-1$
			}

			public void run() {
				addDefaultCaseComponent();
				setEnabled(false);
			}

			@Override
			public boolean isEnabled() {
				return !defaultCaseExists();
			}			
		};	
		getToolbar().add(defaultCaseAction);

		if (config.getConditions().size() == 0) {
			switchCondition = config.newCondition();
			config.getConditions().addConfig(switchCondition);
		} else {
			switchCondition = (BranchCondition) config.getConditions().getConfig(0);
		}

		SelectionListener sel = new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent event) {
			}

			public void widgetSelected(SelectionEvent event) {
				selectSwitchOption((Control) event.getSource());
			}
		};

		Composite c = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(1, false);
		gl.marginLeft = 15;
		c.setLayout(gl);

		workAttribute = new Button(c, SWT.RADIO);
		workAttribute.setText(Messages.getString("BranchWidget.30")); //$NON-NLS-1$

		workCombo = new Combo(c, SWT.BORDER|SWT.READ_ONLY);
		GridData gd = new GridData();
		gd.widthHint = 300;
		workCombo.setLayoutData(gd);
		updateWorkCombo();

		alOps = new Button(c, SWT.RADIO);
		alOps.setText(Messages.getString("AddCaseUI.ALOps.label")); //$NON-NLS-1$
		alOps.addSelectionListener(sel);

		workOps = new Button(c, SWT.RADIO);
		workOps.setText(Messages.getString("AddCaseUI.DeltaEntry.label")); //$NON-NLS-1$
		workOps.addSelectionListener(sel);

		userDefined = new Button(c, SWT.RADIO);
		userDefined.setText(Messages.getString("AddCaseUI.User.label")); //$NON-NLS-1$
		userDefined.addSelectionListener(sel);

		userText = new SimpleTextEditor(c, SWT.BORDER, null, false);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		userText.setLayoutData(gd);
		
		updateSwitchOptionFromConfig();

		userText.getSourceViewer().getTextWidget().addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				switchCondition.setRightHand(userText.getText());
			}
		});

		workCombo.addSelectionListener(sel);
		workAttribute.addSelectionListener(sel);
		workCombo.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				String sel = workCombo.getText();
				workCombo.removeAll();
				updateWorkCombo();
				int i = workCombo.indexOf(sel);
				if (i>=0)
					workCombo.select(i);
			}
		});
		return c;
	}

	private void updateWorkCombo() {
		ConnectorConfig cc = (ConnectorConfig) Utils.getParentConfig(getEditingConfig(), ConnectorConfig.class);
		AssemblyLineConfig alc = (AssemblyLineConfig) Utils.getParentConfig(getEditingConfig(), AssemblyLineConfig.class);
		
		WorkEntryAttributesProvider wep = new WorkEntryAttributesProvider(cc != null ? cc.getShortName() : null);
		wep.inputChanged(null, null, alc);
		for (String name : wep.getSortedAttributes())
			workCombo.add(name);
	}

	private void updateSwitchOptionFromConfig() {
		String expr = switchCondition.getRightHand();
		if (expr == null)
			expr = ""; //$NON-NLS-1$
		else
			userText.setText(expr);

		Control control = null;
		if (isALOpsXP(expr))
			control = alOps;
		else if (isConnOpsXP(expr))
			control = null;
		else if (isDeltaXP(expr))
			control = workOps;
		else if (isWorkAttrXP(expr))
			control = workAttribute;
		else
			control = userDefined;

		if (control == workAttribute) {
			boolean gotit = false;
			expr = expr.substring("{work.".length(), expr.length() - 1); //$NON-NLS-1$
			for (String str : workCombo.getItems()) {
				if (expr.equals(str)) {
					workCombo.select(workCombo.indexOf(str));
					gotit = true;
				}
			}
			if (!gotit)
				control = userDefined;
		}

		if (control != null)
			((Button) control).setSelection(true);

		selectSwitchOption(control);
	}

	private void selectSwitchOption(Control control) {
		userText.setEnabled(false);
		workCombo.setEnabled(false);
		if (control == userDefined) {
			userText.setEnabled(true);
		} else if (control == alOps) {
			setUserText("{op-entry.$operation}"); //$NON-NLS-1$
		} else if (control == workAttribute || control == workCombo) {
			workCombo.setEnabled(true);
			setUserText("{work." + workCombo.getText() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (control == workOps) {
			setUserText("{work.@operation}"); //$NON-NLS-1$
		}
	}

	private void setUserText(String string) {
		userText.setText(string);
		if(!string.equals(switchCondition.getRightHand()))
			switchCondition.setRightHand(string);
	}

	public void addCaseComponents() {
		String expr = switchCondition.getRightHand();
		if (expr == null)
			expr = ""; //$NON-NLS-1$

		String[] cases = null;
		if (isALOpsXP(expr)) {
			cases = getALOperationNames();
		} else if (isDeltaXP(expr)) {
			cases = new String[] {
					"add", "delete", "modify", "generic"};
		}

		if (cases == null) {
			String str = Utils.inputTextArea(getShell(), Messages.getString("AddCaseUI.label"), 
					Messages.getString("AddCaseUI.Manual"), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (str == null)
				return;
			else
				cases = str.split("\r\n"); //$NON-NLS-1$
		}

		//
		// Show a confirmation dialog where the new and old cases are shown
		//
		List<String> existing = new ArrayList<String>();
		List<String> newones = new ArrayList<String>();
		List<BranchingConfig> cclist = new ArrayList<BranchingConfig>();

		// - get list of current case components
		for (int j = 0; j < config.size(); j++) {
			BaseConfiguration bc = config.getConfig(j);
			if (bc instanceof BranchingConfig && ((BranchingConfig) bc).getBranchType() == BranchingConfig.BRANCH_CASE)
				cclist.add((BranchingConfig)bc);
		}

		// - check if case exists for an expression (e.g only one case comp pr
		// expression)
		for (int i = 0; i < cases.length; i++) {
			if (cases[i].trim().length() == 0)
				continue;

			int j;
			for (j = 0; j < cclist.size(); j++) {
				BranchCondition c = (BranchCondition) (cclist.get(j)).getConditions().getConfig(0);
				if (c != null && cases[i].equals(c.getRightHand())) {
					existing.add(cases[i] + " (" + (cclist.get(j)).getShortName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				}
			}
			if (j >= cclist.size())
				newones.add(cases[i]);
		}

		StringBuilder p1 = new StringBuilder(Messages.getString("AddCaseUI.AddCases"));//$NON-NLS-1$
		p1.append("\n"); //$NON-NLS-1$
		for (int i = 0; i < newones.size(); i++) {
			p1.append("  - "); //$NON-NLS-1$
			p1.append(newones.get(i));
			p1.append("\n"); //$NON-NLS-1$
		}
		
		if (existing.size() > 0) {
			p1.append("\n"); //$NON-NLS-1$
			p1.append(Messages.getString("AddCaseUI.OldCases")); //$NON-NLS-1$
			p1.append("\n"); //$NON-NLS-1$
			for (int i = 0; i < existing.size(); i++) {
				p1.append("  - "); //$NON-NLS-1$
				p1.append(existing.get(i));
				p1.append("\n"); //$NON-NLS-1$
			}
		}

		if (!MessageDialog.openConfirm(getShell(), Messages.getString("AddCaseUI.label"), p1.toString())) //$NON-NLS-1$
			return;

		for (int i = 0; i < newones.size(); i++) {
			int j = 1;
			String rh = newones.get(i);
			while (config.getConfig(config.getShortName() + "_" + newones.get(i)) != null) //$NON-NLS-1$
				newones.set(i, rh + "_" + j++); //$NON-NLS-1$
			addCaseComponent(newones.get(i), rh, false);
		}
	}

	/**
	 * Check if default case already exists
	 */
	public boolean defaultCaseExists() {
		// - check current case components
		for (int j = 0; j < config.size(); j++) {
			BaseConfiguration bc = config.getConfig(j);
			if (bc instanceof BranchingConfig && ((BranchingConfig) bc).getBranchType() == BranchingConfig.BRANCH_CASE) {
				// Is this a default case?
				BranchCondition c = (BranchCondition) ((BranchingConfig) bc).getConditions().getConfig(0);
				if ("*".equals(c.getOperator())) //$NON-NLS-1$
					return true;
			}
		}

		return false;
	}

	public void addDefaultCaseComponent() {
		
		int j = 0;
		String name = "default"; //$NON-NLS-1$
		while (config.getConfig(config.getShortName() + "_" + name) != null) //$NON-NLS-1$
			name = "default_" + ++j; //$NON-NLS-1$

		addCaseComponent(name, "", true); //$NON-NLS-1$
	}

	private void addCaseComponent(String name, String rh, boolean isDefault) {

		BranchingConfig cc = (BranchingConfig) config.getConfig(config.getName() + "_" + name, true); //$NON-NLS-1$
		if (cc != null) {
			((BranchCondition) cc.getConditions().getConfig(0)).setRightHand(rh);
			return;
		}

		try {
			cc = new com.ibm.di.config.base.BranchingConfigImpl();
			cc.init();
			cc.setName(config.getName() + "_" + name); //$NON-NLS-1$
			cc.setBranchType(BranchingConfig.BRANCH_CASE);

			BranchCondition b = cc.newCondition();
			if (isDefault)
				b.setOperator("*"); //$NON-NLS-1$
			else
				b.setRightHand(rh);

			cc.getConditions().addConfig(b);

			config.addConfig(cc);
		} catch (Exception err) {
			EclipseAppender.logerror(err.toString(), err, getShell());
		}
	}

	private boolean isDeltaXP(String str) {
		return (str.equals("{work.@operation}")); //$NON-NLS-1$
	}

	private boolean isALOpsXP(String str) {
		return (str.equals("{op-entry.$operation}")); //$NON-NLS-1$
	}

	private boolean isConnOpsXP(String str) {
		return (str.startsWith("{op-entry.$")); //$NON-NLS-1$
	}

	private boolean isWorkAttrXP(String str) {
		return (str.startsWith("{work.")); //$NON-NLS-1$
	}

	// private String[] getConnOps(String conn) {
	// String[] names = null;
	// BaseConfiguration c = config.getParent();
	// while (c != null && !(c instanceof AssemblyLineConfig))
	// c = c.getParent();
	//		
	// if (c instanceof AssemblyLineConfig) {
	// try {
	// ConnectorConfig ccc = ((AssemblyLineConfig)c).getConnectorByName(conn);
	// if (ccc == null)
	// return names;
	//				
	// ContainerConfig cc = ccc.getOperations();
	// if (cc.size() > 0) {
	// names = new String[cc.size()];
	// for (int i = 0; i < names.length; i++)
	// names[i] = cc.getConfig(i).getShortName();
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// return names;
	// }

	private String[] getALOperationNames() {
		String[] names = null;
		BaseConfiguration c = config.getParent();
		while (c != null && !(c instanceof AssemblyLineConfig))
			c = c.getParent();

		if (c != null) {
			AssemblyLineConfig alc = (AssemblyLineConfig) c;
			ContainerConfig cc = alc.getOperations();
			if (cc.size() > 0) {
				names = new String[cc.size()];
				for (int i = 0; i < names.length; i++)
					names[i] = cc.getConfig(i).getShortName();
			}
		}
		return names;
	}

	@Override
	public void setEditor(BaseEditor editor) {
		super.setEditor(editor);
		if(branchWidget instanceof BaseWidget)
			((BaseWidget)branchWidget).setEditor(editor);
	}
	
	public void updateToolBar() {
		if (defaultCaseAction != null)
			defaultCaseAction.setEnabled(!defaultCaseExists());
	}
}
