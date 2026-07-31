/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.eclipse.MetamergeConfigCE;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.TDIHelpMenuAction;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.util.TDIToolBar;

public class HookItemWidget extends BaseWidget implements IDocumentListener, MetamergeConfigChangeListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private HookConfig hook;
	private SimpleTextEditor text;
	private boolean inUpdate = false;
	private boolean verifyChange = false;

	public HookItemWidget(HookConfig hook, Composite parent, int style) {
		this(hook, parent, style, null);
	}

	public HookItemWidget(HookConfig hook, Composite parent, int style, BaseEditor editor) {
		super(parent, style, hook, editor);
		this.hook = hook;
		setLayout(new FillLayout());
		createUI();
	}

	private void createUI() {		
		Form form = createForm(this, hook);
		TDIToolBar bar = new TDIToolBar(form);			
		bar.setText(Messages.getString("Hook." + hook.getHookName()));
		bar.setImage(hook);
		Utils.setGridLayout(form.getBody(), 1, false);

		if((getStyle() & SWT.CLOSE) > 0 && getEditor() != null) {
			bar.add(new Action() {
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

			// -- Javascript help action
			bar.add(new Action() {

				@Override
				public String getActionDefinitionId() {
					return "com.ibm.tdi.open.external.javascript";
				}

				@Override
				public String getText() {
					return Messages.getString("intro.section.learning.5.label");
				}

				@Override
				public void run() {
					new TDIHelpMenuAction().run(this);
				}

			});
		}

		text = new SimpleTextEditor(form.getBody(), SWT.BORDER, hook);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setupHookInheritanceChain();
		text.setText(hook.getScript());
		text.getDocument().addDocumentListener(this);

		updateVerifyChange();

		// -- When inherited the text is read-only. Trap key presses to break inheritance.
		text.getSourceViewer().getTextWidget().addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(!verifyChange || e.character == 0)
					return;

				// only control chars to modify text are cntrl-v and ctrl-x
				if ((e.stateMask & SWT.CONTROL) > 0 && 
						e.character != 22 && e.character != 24 ) {
					return;
				}
				if(wantToBreakInheritance()) {
					text.getSourceViewer().setEditable(true);
					verifyChange = false;
					text.setAutoUpdate(true);
					text.setUpdateOnFocusOut(false);
				}
			}
		});


		String str = Messages.getString("HookIemWidget.available.tooltip");
		Label info = getFormToolKit().createLabel(form.getBody(), str); 
		info.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		hook.addListener(this);
	}

	private void setupHookInheritanceChain() {
		try {
			// Refresh inheritance, just in case
			BaseConfiguration bc = hook;
			while (bc != null) {
				bc.setupInheritanceChain();
				bc = bc.getInheritsFrom();
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
	}

	public SimpleTextEditor getText() {
		return text;
	}

	public void documentAboutToBeChanged(DocumentEvent event) {}

	public void documentChanged(DocumentEvent event) {
		if (inUpdate)
			return;
		inUpdate = true;
		if (! hook.isParameterLocal(InternalSchema.HC_SCRIPT)) {
			hook.setScript(text.getText());
		}

		// -- Only call set enabled if we really have to
		if(!hook.getEnabled() && text.getDocument().getLength() > 0) {
			hook.setEnabled(true);
		}

		// Set inheritance to NONE to keep the script if the Connector changes inheritance
		if (! BaseConfiguration.INHERIT_NONE.equals(hook.getInheritsFromRef()))
			hook.setInheritsFromRef(BaseConfiguration.INHERIT_NONE);

		inUpdate = false;
	}

	@Override
	public void dispose() {
		hook.removeListener(this);
		super.dispose();
	}

	public void configurationChanged(MetamergeConfigChange changeEvent) {
		if (inUpdate || isDisposed() || text.isUpdating())
			return;
		final MetamergeConfigChange mcc = changeEvent;
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateXX(mcc);
			}
		});
	}

	private void updateXX(MetamergeConfigChange changeEvent) {
		if (InternalSchema.HC_SCRIPT.equals(changeEvent.getKey())) {
			inUpdate = true;
			text.setText(hook.getScript());
			inUpdate = false;
		} else if ("setInheritsFrom".equals(changeEvent.getUserObject())) {
			inUpdate = true;
			// Inheritance changed, remove local script, probably empty anyway.
			//Maybe we should ask if the user really wants to do this
			if (hook.getInheritsFrom() != null) {
				hook.removeParameter(InternalSchema.HC_SCRIPT);
				setupHookInheritanceChain();
			}
			String s = hook.getScript();
			if ( s == null )
				s = "";
			text.setText(s);
			// Newly added by L3. 
			if (hook.getInheritsFrom() != null) {
				hook.removeParameter(InternalSchema.HC_SCRIPT);
			}// End newly added 
			hook.setEnabled(s.length() > 0);
			inUpdate = false;
		} else if ("".equals(changeEvent.getKey()) && 
				changeEvent.getOperation() == MetamergeConfigChange.MCC_REPLACE &&
				changeEvent.getSource() instanceof ScriptConfig) {
			// User saved a new copy of an inherited script
			inUpdate = true;
			boolean hasScript = hook.hasParameter(InternalSchema.HC_SCRIPT);
			setupHookInheritanceChain();
			if (hook.getScript() != null)
				text.setText(hook.getScript());
			if (!hasScript)
				hook.removeParameter(InternalSchema.HC_SCRIPT);
			inUpdate = false;			
		}
		updateVerifyChange();
	}

	private boolean wantToBreakInheritance() {
		BaseConfiguration inh = hook.getInheritsFrom();
		while (inh != null) {
			if (inh.hasParameter(InternalSchema.HC_SCRIPT))
				break;
			inh = inh.getInheritsFrom();
		}
		if (inh == null)
			return true;

		String[] buttons;

		MetamergeConfig mc = inh.getMetamergeConfig();
		if (mc instanceof MetamergeConfigCE || mc instanceof TDIConfigurationFile) {
			buttons = new String[] { IDialogConstants.OK_LABEL,
					IDialogConstants.CANCEL_LABEL,
					Messages.getString("ConfigBinding.EditOriginal")};
		} else {
			buttons = new String[] { IDialogConstants.OK_LABEL,
					IDialogConstants.CANCEL_LABEL };
		}

		boolean saveOut = text.isUpdateOnFocusOut();
		text.setUpdateOnFocusOut(false);

		MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				Messages.getString("ConfigBinding.BreakInh.Title"), null,
				Messages.getString("BreakInhScript.Msg"),
				MessageDialog.QUESTION, buttons, 0);
		int i = dialog.open();

		text.setUpdateOnFocusOut(saveOut);

		if ( i==0 )
			return true;
		text.setModified(false);
		if ( i == 2 )
			Utils.openEditorFor(inh);
		return false;
	}

	protected void updateVerifyChange() {
		verifyChange = ! getEditingConfig().isParameterLocal(InternalSchema.SCRIPT);
		text.getSourceViewer().setEditable(!verifyChange);
		if (verifyChange) {
			text.setUpdateOnFocusOut(true);
			text.setAutoUpdate(false);
		} else {
			text.setAutoUpdate(true);
			text.setUpdateOnFocusOut(false);
		}
	}
}
