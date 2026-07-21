/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.commands.CommandHandlerProxy;
import com.ibm.tdi.eclipse.commands.CommandID;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.wizards.NewParserWizard;
import com.ibm.tdi.eclipse.wizards.pages.ConfigTypePage;

public class ParserWidget extends BaseWidget {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private FormWidget2 widget;

	private Action changeParserAction;
	ConnectorConfig cc;
	MetamergeConfigChangeListener listener;

	public ParserWidget(Composite parent, int style, BaseConfiguration editingConfig) {
		this(parent, style, editingConfig, null);
	}
	
	public ParserWidget(Composite parent, int style, BaseConfiguration editingConfig, BaseEditor editor) {
		super(parent, style, editingConfig);
		setBackground(parent.getBackground());
		setLayout(new FillLayout());
		
		changeParserAction = new Action() {
			public void run() {
				changeParser();
			}
			public String getText() {
				return Messages.getString("Util.SelectComponent.3"); //$NON-NLS-1$
			}
		};
		
		cc = Utils.getParentConfig(editingConfig, ConnectorConfig.class);
		
		setEditor(editor);
		createUI();
		
		if (cc != null) {
			listener = new MetamergeConfigChangeListener() {
				public void configurationChanged(MetamergeConfigChange changeEvent) {
					if (changeEvent.getSource() == cc &&
							"setInheritsFrom".equals(changeEvent.getUserObject())) {
						recreateUI();
					}
				}
			};
			cc.addListener(listener);
		}
	}
	
	private void recreateUI() {
		if (!isDisposed()) {
			getDisplay().syncExec(new Runnable() {
				public void run() {
					if (! isDisposed())
						createUI();
				}
			});		
		}
	}
	
	public void changeParser() {
		NewParserWizard wiz = new NewParserWizard();
		wiz.init(null, new StructuredSelection(getEditingConfig()));
		wiz.setChooseFileName(false);
		wiz.setIncludeNullSelection(true);
		
		WizardDialog dlg = new WizardDialog(getShell(), wiz);
		if(dlg.open() == Window.OK) {
			String inherit = wiz.getConfigObject().getInheritsFromRef();
			getEditingConfig().setInheritsFromRef(inherit);
			try {
				getEditingConfig().setupInheritanceChain();
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}
			recreateUI();
		}
	}
	

	private void createUI() {
		ParserConfig config = (ParserConfig) getEditingConfig();
		String cls = Utils.getFormName(config); 
		
		if(getForm() != null) {
			getForm().dispose();
		}
		
		if(widget != null) {
			widget.dispose();
			widget = null;
		}
		
		if(cls == null || cls.length() == 0) {
			
			Form frm = createForm(this, null);
			frm.setText(Messages.getString("ParserWidget.undefined")); //$NON-NLS-1$
			frm.getBody().setLayout(new FillLayout());
			
			Composite c = new Composite(frm.getHead(), 0);
			c.setLayout(new GridLayout(1,false));
			
			final NewParserWizard npw = new NewParserWizard();
			npw.init(null, new StructuredSelection(getEditingConfig()));
			npw.setContainer(new IWizardContainer() {
				public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException,
						InterruptedException {
				}
				public void updateWindowTitle() {
				}
				public void updateTitleBar() {
				}
				public void updateMessage() {
				}
				public void updateButtons() {
					String inherit = npw.getConfigObject() != null ? npw.getConfigObject().getInheritsFromRef() : null;
					if(inherit != null)
						parserSelected(npw);
				}
				public void showPage(IWizardPage page) {
				}
				public Shell getShell() {
					return ParserWidget.this.getShell();
				}
				public IWizardPage getCurrentPage() {
					return npw.getPage("TypePage");
				}
			});
			
			// -- configure wizard
			npw.setNameRequested(false);
			npw.setChooseFileName(false);
			npw.setModeRequested(false);
			npw.addPages();
			((ConfigTypePage)npw.getPage("TypePage")).setShowFilter(true);
			((ConfigTypePage)npw.getPage("TypePage")).setAutoSelect(false);
			npw.createPageControls(frm.getBody());
			
			frm.setHeadClient(c);
			
		} else {
			try {
				widget = new FormWidget2(this, SWT.TITLE, config, cls, true);
				Form form = widget.getForm();
				TDIToolBar bar = new TDIToolBar(form);
				if (cls.startsWith("@")) {
					bar.setText(Messages.getMessage("ConfigLabelPriver.reusing", cls.substring(1)));
				} else {
					bar.setText(form.getText());
					Button change = bar.add(changeParserAction);
					change.setToolTipText(Messages.getString("HooksWidget.0") + ": " + getInheritanceString());
					bar.addHelpButton(getEditingConfig());
				}
				form.setText(null);
				widget.layout();
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}
		}
				
		layout(true);
	}
	
	protected void parserSelected(NewParserWizard npw) {
		String inherit = npw.getConfigObject().getInheritsFromRef();
		getEditingConfig().setInheritsFromRef(inherit);
		try {
			getEditingConfig().setupInheritanceChain();
		} catch (Exception err) {
			EclipseAppender.logerror(err.toString(), err, getShell());
		}
		recreateUI();
	}

	protected void changeParser(BaseConfiguration bc) {
		String inherit = (String) MetamergeConfigFactory.getLocalNamespaceFor(getEditingConfig().getMetamergeConfig(), bc);
		if(inherit != null)
			getEditingConfig().setInheritsFromRef(inherit + ":/" + bc.getName().toString());
		else
			getEditingConfig().setInheritsFromRef("/" + bc.getName().toString());
		try {
			getEditingConfig().setupInheritanceChain();
		} catch (Exception err) {
			EclipseAppender.logerror(err.toString(), err, getShell());
		}
	}
	
	private String getInheritanceString() {
		String str = getEditingConfig().getInheritsFromRef();
		if(str == null || str.equals("") || str.equals(BaseConfiguration.INHERIT_NONE))
			return Messages.getString("ConfigChooser.Localized.Inherit.None");
		else if (str.equals(BaseConfiguration.INHERIT_PARENT))
			return Messages.getString("ConfigChooser.Localized.Inherit.Parent");		
		return str;		
	}

	@Override
	public void dispose() {
		if (cc != null && listener != null)
			cc.removeListener(listener);
		super.dispose();
	}

	@Override
	public void setEditor(BaseEditor editor) {
		super.setEditor(editor);
		if(editor != null)
			new CommandHandlerProxy(editor.getEditorSite(), changeParserAction, CommandID.CHANGE_PARSER_ACTION_ID);
	}
	
	
}
