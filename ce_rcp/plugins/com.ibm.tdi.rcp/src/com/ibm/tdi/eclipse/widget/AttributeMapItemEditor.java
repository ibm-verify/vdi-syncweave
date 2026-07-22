/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.Stack;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.eclipse.MetamergeConfigCE;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.wizards.NullValueBehaviorWizard;

/**
 * A composite that provides editing for Attribute map items.
 * 
 */
public class AttributeMapItemEditor extends Composite implements MetamergeConfigChangeListener, ModifyListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Form editorForm;
	private SimpleTextEditor textEditor;
	private Label editorName;

	private SashForm sash;

	private Button enabled;

	private Button expression;

	//private SimpleTextEditor substEditor;
	private TDIExpressionEditor substEditor;

	private BaseConfiguration config;

	private Composite stack;

	private Label starLabel;
	
	private boolean verifyChange;

	private boolean ignoreSash = false;

	public AttributeMapItemEditor(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		createEditor(this);
	}

	public void configurationChanged(MetamergeConfigChange mcc) {
		if(mcc.getSource() != config)
			return;
		if(InternalSchema.ENABLED.equals(mcc.getKey())) {
			enabled.setSelection(config.getEnabled());
		} else if(InternalSchema.AMI_TYPE.equals(mcc.getKey())) {
			expression.setSelection(((AttributeMapItem)mcc.getSource()).isSubstitution());
			if(expression.getSelection())
				((StackLayout)stack.getLayout()).topControl = substEditor;
			else
				((StackLayout)stack.getLayout()).topControl = textEditor;
		} else if (InternalSchema.SCRIPT.equals(mcc.getKey())) {
			textEditor.setText(getEditingConfig().getScript());
		} else if (InternalSchema.INHERITS_FROM.equals(mcc.getKey())) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					config.removeListener(AttributeMapItemEditor.this);
					sash.setWeights(new int[]{100,0});
					textEditor.setEditingConfig(null);
					config = null;			
				}
			});
		}
	}

	public BaseConfiguration getEditingConfig() {
		return config;
	}
	
	private void createEditor(Composite parent) {
		BaseWidget editor = new BaseWidget(parent, SWT.NONE);
		editor.setLayout(new FillLayout());
		editorForm = editor.createForm(editor, null);
		editorForm.getBody().setLayout(new FillLayout());
		
		stack = new Composite(editorForm.getBody(), SWT.NONE);
		stack.setLayout(new StackLayout());
		
		// -- javascript editor
		textEditor = new SimpleTextEditor(stack, SWT.NONE);
		
		// -- substitution text
		//substEditor = new SimpleTextEditor(stack, SWT.NONE, null, false);
		
		substEditor = new TDIExpressionEditor(stack, getEditingConfig(),true);
		
		// -- When mapping star, we only need a Label
		starLabel = new Label(stack, SWT.None);
		starLabel.setText(Messages.getString("MapLabelProvider.star"));
		
		// -- toolbar with subst/javascript, enabled, nvb and close buttons
		Composite toolbar = new Composite(editorForm.getHead(), SWT.NONE);
		toolbar.setLayout(new GridLayout(9,false));
		editorName = new Label(toolbar, SWT.LEFT);
		editorName.setText("");
		editorName.setImage(Activator.getImage("AttributeMap"));
		editorName.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		expression = new Button(toolbar, SWT.CHECK);
		expression.setText(Messages.getString("AttributeMapItemEditor.substitute"));
		expression.setToolTipText(Messages.getString("AttributeMapItemEditor.substitute.tooltip"));
		expression.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectEditor(expression.getSelection());
			}
		});
		
		enabled = new Button(toolbar, SWT.CHECK);
		enabled.setText(Messages.getString("Hooks.enabled.label"));
		if(config != null)
			enabled.setSelection(config.getEnabled());
		enabled.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (config != null)
					config.setEnabled(enabled.getSelection());
			}
		});
		
		Button nvb = new Button(toolbar, SWT.PUSH);
		nvb.setText(Messages.getString("action.label.36"));
		nvb.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				NullValueBehaviorWizard wiz = new NullValueBehaviorWizard(config);
				WizardDialog dlg = new WizardDialog(getShell(), wiz);
				dlg.open();
			}
		});
		
		Button b = new Button(toolbar, SWT.PUSH);
		b.setText(Messages.getString("LBL.CLOSE"));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(ignoreSash)
					fireClose();
				else
					quickEdit(null);
			}
		});
		editorForm.setHeadClient(toolbar);

		// -- When inherited the text is read-only. Trap key presses to break inheritance.
		textEditor.getSourceViewer().getTextWidget().addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.character == 0)
					return;
				
				// cntrl-v and ctrl-x are only control characters that can modify the text?
				if ((e.stateMask & SWT.CONTROL) > 0 &&
						e.character != 22 && e.character != 24 ) {
					return;
				}
				if(verifyChange && wantToBreakInheritance()) {
					textEditor.getSourceViewer().setEditable(true);
					verifyChange = false;
				}
			}
		});
	}

	protected void selectEditor(boolean substitution) {
		AttributeMapItem ami = (AttributeMapItem)getEditingConfig();
		if(substitution) {
			ami.setType(AttributeMapItem.SUBSTITUTION_MAPPING);
			((StackLayout)stack.getLayout()).topControl = substEditor;
		} else {
			if (ami.isSubstitution())
				ami.setScript(textEditor.getText());
			((StackLayout)stack.getLayout()).topControl = textEditor;
		}
		stack.layout(true);
	}

	protected void quickEdit(BaseConfiguration config) {
		
		sash = Utils.getParentConfig(editorForm, SashForm.class);
		if(sash == null)
			return;
		
// if (sash != null) {
// int[] weights = (config != null ? new int[] { 60, 40 } : new int[] { 100, 0
// });
// sash.setWeights(weights);
// }
		
		verifyChange = false;

		if(this.config != null)
			this.config.removeListener(this);

		if(this.config == config || config == null) {
			sash.setWeights(new int[]{100,0});
			textEditor.setEditingConfig(null);
			this.config = null;
		} else {
			this.config = config;
			ConnectorConfig cc = Utils.getParentConfig(config, ConnectorConfig.class);
			String connectorName = (cc != null) ? cc.getShortName() + "." : "";
			if( config != null && config.getScript() != null) {
				editorName.setText(connectorName + config.getShortName());
				if(config instanceof AttributeMapItem) {
					AttributeMapItem ami = (AttributeMapItem) config;
					enabled.setSelection(ami.getEnabled());
					if ("*".equals(ami.getShortName())) {
						expression.setVisible(false);
						((StackLayout)stack.getLayout()).topControl = starLabel;
						stack.layout(true);
					} else {
						textEditor.init(config, getScript(ami));
						substEditor.init(config, ami.getSubstitution());
						expression.setVisible(true);
						expression.setSelection(ami.isSubstitution());
						selectEditor(ami.isSubstitution());

						if(!ami.hasParameter(InternalSchema.AMI_SCRIPT))
							verifyChange = ami.getParameter(InternalSchema.AMI_SCRIPT) != null;
					}
				} else {
					textEditor.init(config, config.getScript());
					selectEditor(false);
				}
				textEditor.getSourceViewer().setEditable(!verifyChange);
 				sash.setWeights(new int[]{50,50});
				config.addListener(this);
			}
		}
	}
	
	public void editAttribute(BaseConfiguration config) {
		verifyChange = false;
		ignoreSash = true;

		if(this.config != null)
			this.config.removeListener(this);

		if(this.config == config || config == null) {
			textEditor.setEditingConfig(null);
			this.config = null;
		} else {
			this.config = config;
			ConnectorConfig cc = Utils.getParentConfig(config, ConnectorConfig.class);
			String connectorName = (cc != null) ? cc.getShortName() + "." : "";
			if( config != null && config.getScript() != null) {
				editorName.setText(connectorName + config.getShortName());
				if(config instanceof AttributeMapItem) {
					AttributeMapItem ami = (AttributeMapItem) config;
					enabled.setSelection(ami.getEnabled());
					if ("*".equals(ami.getShortName())) {
						expression.setVisible(false);
						((StackLayout)stack.getLayout()).topControl = starLabel;
						stack.layout(true);
					} else {
						textEditor.init(config, getScript(ami));
						substEditor.init(config, ami.getSubstitution());
						expression.setVisible(true);
						expression.setSelection(ami.isSubstitution());
						selectEditor(ami.isSubstitution());

						if(!ami.hasParameter(InternalSchema.AMI_SCRIPT))
							verifyChange = ami.getParameter(InternalSchema.AMI_SCRIPT) != null;
					}
				} else {
					textEditor.init(config, config.getScript());
					selectEditor(false);
				}
				textEditor.getSourceViewer().setEditable(!verifyChange);
				config.addListener(this);
			}
		}
	}
	
	private String getScript(AttributeMapItem ami) {
		if (ami.isSimple()) {
			// TODO: We really should continue to support simple mapping.
			// But for now, just replace with advanced mapping.
			boolean input = Utils.isInputMap(Utils.getParentConfig(ami, AttributeMapConfig.class));
			return Utils.getScript( input ? "conn" : "work", ami.getSimple() );
		}
		if (ami.isSubstitution())
			return ami.getSubstitution();
		return ami.getScript();
	
	}
	
	public void toggleEditorVisible() {
		int[] weights = (sash.getWeights()[1] == 0 ? new int[] { 60, 40 } : new int[] { 100, 0 });
		sash.setWeights(weights);
	}

	public void modifyText(ModifyEvent e) {
	}

	private boolean wantToBreakInheritance() {
		BaseConfiguration inh = getEditingConfig().getInheritsFrom();
		if (inh == null)
			return true;

		MetamergeConfig inhMC = inh.getMetamergeConfig();
		boolean editOriginal = inhMC instanceof MetamergeConfigCE || inhMC instanceof TDIConfigurationFile;
		if (editOriginal) {
			// Extra check in case this is an external file
			AttributeMapConfig amc = Utils.getParentConfig(inh, AttributeMapConfig.class);
			if (amc != null && ! ConnectorConfig.INPUT_MAP_NAME.equals(amc.getShortName()) &&
					! ConnectorConfig.OUTPUT_MAP_NAME.equals(amc.getShortName()))
				editOriginal = false;
		}
		
		String[] buttons;
		if (editOriginal) {
			buttons = new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL,
					Messages.getString("ConfigBinding.EditOriginal") };
		} else {
			buttons = new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL };
		}

		boolean saveOut = textEditor.isUpdateOnFocusOut();
		textEditor.setUpdateOnFocusOut(false);

		MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				Messages.getString("ConfigBinding.BreakInh.Title"), null, Messages.getString("BreakInhScript.Msg"),
				MessageDialog.QUESTION, buttons, 0);
		int i = dialog.open();

		textEditor.setUpdateOnFocusOut(saveOut);

		if (i == 0)
			return true;
		else if (i == 2)
			Utils.openEditorFor(inh);

		return false;
	}

	Stack<Listener> closeListeners = new Stack<Listener>();
	public void addCloseListener(Listener listener) {
		if(!closeListeners.contains(listener))
			closeListeners.add(listener);
	}
	
	public void removeCloseListener(Listener listener) {
		closeListeners.remove(listener);
	}
	
	private void fireClose() {
		Event event = new Event();
		event.data = getEditingConfig();
		for(Listener l : closeListeners)
			l.handleEvent(event);
	}
}
