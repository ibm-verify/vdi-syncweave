/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.Enumeration;

import javax.naming.Binding;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.LogConfigItem;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.ConfigLabelProvider;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.wizards.NewConnectorWizard;
import com.ibm.tdi.eclipse.wizards.NewFunctionWizard;

public class RawConnectorWidget extends BaseWidget {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ConfigLabelProvider clp;
	private FormWidget2 widget;
	private Form form;
	private boolean chooseButton;
	private ConnectorConfig cc;
	private String[] additionalLabels = new String[] { Messages.getString("LogConfigUI.Appender") }; //$NON-NLS-1$
	private Combo logAppender;
	private MetamergeConfigChangeListener listener;
	private Label errorLabel;

	private static final String LOG_CONNECTOR_CLASS = "com.ibm.di.connector.LogConnector"; //$NON-NLS-1$
	
	public void dispose() {
		disposeWidget();
		if (form != null)
			form.dispose();
		if (listener != null && cc != null)
			cc.removeListener(listener);

		super.dispose();
	}

	public RawConnectorWidget(Composite parent, int style, BaseConfiguration editingConfig) {
		this(parent, style, editingConfig, false);
	}

	public RawConnectorWidget(Composite parent, int style, BaseConfiguration editingConfig, boolean chooseButton) {
		this(parent, style, editingConfig, chooseButton, true);
	}

	public RawConnectorWidget(Composite parent, int style, BaseConfiguration editingConfig, boolean chooseButton,
			boolean extraControls) {
		super(parent, style, editingConfig);
		setLayout(new FillLayout());

		this.chooseButton = chooseButton;

		clp = new ConfigLabelProvider();
		clp.setConsultingInfFiles(true);

		try {
			createFormUI(chooseButton);
		} catch (Exception e) {
			errorLabel = new Label(this, SWT.WRAP | SWT.SHADOW_OUT);
			errorLabel.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			errorLabel.setText(Messages.getString("RawConnectorWidget.no.form"));
			EclipseAppender.logerror(e.getMessage(), e);
		}
	}

	private void createFormUI(boolean chooseButton) throws Exception {
		String loggerFormName = null;

		cc = Utils.getParentConfig(getEditingConfig(), ConnectorConfig.class);
		if (isLogConnector()) {
			loggerFormName = updateLoggerConfig(null);
		}

		if (listener == null && cc != null) {
			listener = new MetamergeConfigChangeListener() {
				public void configurationChanged(MetamergeConfigChange changeEvent) {
					if (changeEvent.getSource() == cc &&
							InternalSchema.CONNECTOR_MODE.equals(changeEvent.getKey())) {
						changeMode();
					} else if ("setInheritsFrom".equals(changeEvent.getUserObject())) {
						if(cc instanceof FunctionConfig && changeEvent.getSource() == ((FunctionConfig)cc).getFunctionConfig())
							changeConnectorInheritance();
						else if(changeEvent.getSource() == cc.getConnectionConfig())
							changeConnectorInheritance();
					}
				}
			};
			cc.addListener(listener);
		}

		widget = new FormWidget2(this, SWT.TITLE, getEditingConfig(), loggerFormName, false);

		TDIToolBar bar = new TDIToolBar(widget.getForm(), SWT.SINGLE);
		
		form = widget.getForm();
//		form.setText(getTitle());

		// form.setToolBarVerticalAlignment(SWT.BOTTOM);
		if (chooseButton) {
			bar.add(new Action() {
				@Override
				public void run() {
					if (cc instanceof FunctionConfig)
						selectFunction();
					else
						selectConnector();
				}

				@Override
				public String getText() {
					if (cc instanceof FunctionConfig)
						return Messages.getString("RawConnectorWidget.5"); //$NON-NLS-1$
					else
						return Messages.getString("RawConnectorWidget.6"); //$NON-NLS-1$
				}

			});
			form.getToolBarManager().update(true);
		}
		bar.addHelpButton(getEditingConfig());

		//
		// Need a drop-down to choose logger interface
		//
		if (loggerFormName != null) {
			addLoggerCombo(form.getBody(), loggerFormName);
		}

		//
		// Add the rest of the form
		//
		widget.initialize();
	}

	private String updateLoggerConfig(String logger) {
		String loggerFormName = cc.getConnectionConfig().getStringParameter("com.ibm.di.formName"); //$NON-NLS-1$
		if (loggerFormName == null || logger != null) {
			updateLoggerConfig(cc.getConnectionConfig(), logger);
		}
		return cc.getConnectionConfig().getStringParameter("com.ibm.di.formName"); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	private void updateLoggerConfig(RawConnectorConfig cc, String logger) {
		try {
			Enumeration<Binding> list = MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE).list(
					MetamergeConfig.DEFAULT_LOGGER_FOLDER);
			LogConfigItem lci = null;

			while (list.hasMoreElements()) {
				Binding obj = list.nextElement();
				lci = (LogConfigItem) obj.getObject();
				if (logger == null || lci.getShortName().equals(logger))
					break;
			}
			for (String key : lci.getKeys(BaseConfiguration.ONE_LEVEL)) {
				cc.setStringParameter(key, lci.getStringParameter(key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void addLoggerCombo(Composite header, String selection) {
		Label l = new Label(header, SWT.RIGHT);
		l.setText(Messages.getString("LogConfigUI.Appender")); //$NON-NLS-1$
		l.setBackground(header.getBackground());
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		int max = widget.findLongestLabel(additionalLabels);
		if (max != -1)
			gd.widthHint = max;
		l.setLayoutData(gd);

		logAppender = new Combo(header, SWT.READ_ONLY);
		try {
			Enumeration<Binding> list = MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE).list(
					MetamergeConfig.DEFAULT_LOGGER_FOLDER);
			while (list.hasMoreElements()) {
				Binding obj = list.nextElement();
				LogConfigItem lci = (LogConfigItem) obj.getObject();
				logAppender.add(lci.getShortName());
			}
		} catch (Exception e) {
		}
		logAppender.setText(selection);

		gd = new GridData();
		gd.horizontalSpan = 3;
		logAppender.setLayoutData(gd);
		logAppender.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLoggerConfig(logAppender.getText());
				disposeWidget();
				try {
					createFormUI(chooseButton);
					RawConnectorWidget.this.layout();
				} catch (Exception err) {
					EclipseAppender.logerror(err.getMessage(), err);
				}
			}
		});
	}

//	private String getTitle() {
//		String str = clp.getText(getEditingConfig());
//		if (str == null)
//			return Messages.getString("RawConnectorWidget.24"); //$NON-NLS-1$
//		else
//			return str;
//	}

	public void changeMode() {
		disposeWidget();
		try {
			createFormUI(chooseButton);
		} catch (Exception e) {
			e.printStackTrace();
		}
		layout();
	}

	public void selectFunction() {
		NewFunctionWizard wiz = new NewFunctionWizard();
		wiz.init(null, new StructuredSelection(getEditingConfig()));
		wiz.setChooseFileName(false);

		WizardDialog dlg = new WizardDialog(getShell(), wiz);
		if (dlg.open() == Window.OK) {
			updateInheritsFrom(wiz.getConfigObject().getInheritsFromRef());
		}
	}

	public void selectConnector() {
		NewConnectorWizard wiz = new NewConnectorWizard();
		wiz.init(null, new StructuredSelection(getEditingConfig()));
		wiz.setChooseFileName(false);
		wiz.setModeRequested(false);

		WizardDialog dlg = new WizardDialog(getShell(), wiz);
		if (dlg.open() == Window.OK) {
			updateInheritsFrom(wiz.getConfigObject().getInheritsFromRef());
		}
	}

	private void updateInheritsFrom(String inherit) {
		try {
			getEditingConfig().setInheritsFromRef(inherit);
			getEditingConfig().setupInheritanceChain();
			disposeWidget();
			createFormUI(chooseButton);
			layout(true, true);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

	private void changeConnectorInheritance() {
		if (isDisposed()) {
			if (listener != null && cc != null)
				cc.removeListener(listener);
			return;
		}
		getDisplay().syncExec(new Runnable() {
			public void run() {
				if (isDisposed())
					return;
				disposeWidget();
				try {
					createFormUI(chooseButton);	
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
				layout(true, true);
			}
		});
	}
	
	@Override
	public Form getForm() {
		return form;
	}
	
	public boolean isLogConnector() {
		return cc != null && cc.getConnectionConfig() != null && LOG_CONNECTOR_CLASS.equals(cc.getConnectionConfig().getJavaClass());
	}

	private void disposeWidget() {
		if (widget != null) {
			widget.dispose();
			widget = null;
		}
		if (errorLabel != null) {
			errorLabel.dispose();
			errorLabel = null;
		}
	}
}
