/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.eclipse.MetamergeConfigCE;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.function.SystemFunctions;

public class ScriptWidget extends BaseWidget implements MetamergeConfigChangeListener {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private SimpleTextEditor scriptEditor;
	private boolean verifyChange = false;

	private boolean inUpdate;

	public ScriptWidget(BaseConfiguration config, Composite parent, int style) {
		this(config, parent, style, null);
	}

	public ScriptWidget(BaseConfiguration config, Composite parent, int style, BaseEditor editor) {
		super(parent, style, config, editor);
		setLayout(new FillLayout());
		Form frm = createForm(this, config);
		frm.getBody().setLayout(new FillLayout());
		createUI(frm.getBody());
	}

	public void createUI(Composite parent) {
		BaseConfiguration bc = getEditingConfig();
		try {
			// Refresh inheritance, for safety
			if (bc != null)
				bc.setupInheritanceChain();
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		scriptEditor = new SimpleTextEditor(parent, SWT.BORDER, bc);
		if (bc == null)
			return;

		updateVerifyChange();

		// -- When inherited the text is read-only. Trap key presses to break
		// inheritance.
		scriptEditor.getSourceViewer().getTextWidget().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.character == 0)
					return;
				if ((e.stateMask & SWT.CONTROL) > 0 && 
						e.character != 22 && e.character != 24 ) {
					return;
				}
				if (verifyChange && wantToBreakInheritance()) {
					scriptEditor.getSourceViewer().setEditable(true);
					verifyChange = false;
					if (getEditingConfig() != null ) {
						inUpdate = true;
						try {
							getEditingConfig().updateInheritsFrom(BaseConfiguration.INHERIT_NONE);
						} catch (Exception err) {
							SystemFunctions.doNothing(); // Cannot happen
						}
						inUpdate = false;
					}
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		bc.addListener(this);
	}

	@Override
	public boolean setFocus() {
		if (scriptEditor != null)
			return scriptEditor.setFocus();
		else
			return false;
	}

	public SimpleTextEditor getScriptEditor() {
		return scriptEditor;
	}

	private boolean wantToBreakInheritance() {
		BaseConfiguration inh = getEditingConfig().getInheritsFrom();
		while (inh != null) {
			if (inh.hasParameter(InternalSchema.SCRIPT))
				break;
			inh = inh.getInheritsFrom();
		}
		if (inh == null)
			return true;

		String[] buttons;
		
		MetamergeConfig inhMC = inh.getMetamergeConfig();
		if (inhMC instanceof MetamergeConfigCE || inhMC instanceof TDIConfigurationFile) {
			buttons = new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL,
					Messages.getString("ConfigBinding.EditOriginal") };
		} else {
			buttons = new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL };
		}

		boolean saveOut = scriptEditor.isUpdateOnFocusOut();
		scriptEditor.setUpdateOnFocusOut(false);

		MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages
				.getString("ConfigBinding.BreakInh.Title"), null, Messages.getString("BreakInhScript.Msg"), MessageDialog.QUESTION,
				buttons, 0);
		int i = dialog.open();

		scriptEditor.setUpdateOnFocusOut(saveOut);

		if (i == 0)
			return true;
		else if (i == 2)
			Utils.openEditorFor(inh);

		return false;
	}

	public void configurationChanged(MetamergeConfigChange changeEvent) {
		if (inUpdate || isDisposed())
			return;
		final MetamergeConfigChange mcc = changeEvent;
		getDisplay().syncExec(new Runnable() {
			public void run() {
				updateXX(mcc);
				updateVerifyChange();
			}
		});

	}

	protected void updateVerifyChange() {
		verifyChange = !getEditingConfig().isParameterLocal(InternalSchema.SCRIPT);
		scriptEditor.getSourceViewer().setEditable(!verifyChange);
		if (!verifyChange && getEditingConfig() instanceof ScriptConfig)
			scriptEditor.setAutoUpdate(true);
	}

	private void updateXX(MetamergeConfigChange changeEvent) {
		if (InternalSchema.HC_SCRIPT.equals(changeEvent.getKey()) || InternalSchema.SCRIPT.equals(changeEvent.getKey())) {
			inUpdate = true;
			scriptEditor.setText(getEditingConfig().getScript());
			inUpdate = false;
		} else if ("setInheritsFrom".equals(changeEvent.getUserObject())) {
			inUpdate = true;
			// Inheritance changed, remove local script, probably empty anyway.
			// Maybe we should ask if the user really wants to do this
			if (getEditingConfig().getInheritsFrom() != null) {
				getEditingConfig().removeParameter(InternalSchema.HC_SCRIPT);
			}
			String s = getEditingConfig().getScript();
			if (s == null)
				s = "";
			scriptEditor.setText(s);
			// Need to remove the script again, as scriptEditor.setText() sets a local copy of inherited script.
			if (getEditingConfig().getInheritsFrom() != null) {
				getEditingConfig().removeParameter(InternalSchema.HC_SCRIPT);
			}
			inUpdate = false;
			scriptEditor.setModified(false);
		} else if ("".equals(changeEvent.getKey()) && 
				changeEvent.getOperation() == MetamergeConfigChange.MCC_REPLACE &&
				changeEvent.getSource() instanceof ScriptConfig) {
			// User saved a new copy of an inherited script
			inUpdate = true;
			try {
				// Need to refresh the inherit from the new copy
				getEditingConfig().setupInheritanceChain();
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
			boolean hasScript = getEditingConfig().hasParameter(InternalSchema.SCRIPT);
			scriptEditor.setText(getEditingConfig().getScript());
			if (!hasScript)
				getEditingConfig().removeParameter(InternalSchema.SCRIPT);
			inUpdate = false;			
		}
		updateVerifyChange();
	}

	@Override
	public void dispose() {
		if (getEditingConfig() != null)
			getEditingConfig().removeListener(this);
		super.dispose();
	}

}
