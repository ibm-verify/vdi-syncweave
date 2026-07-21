/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.ibm.di.config.base.FormConfigImpl;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.LogConfigItem;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.form.FormEditorWidget;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.widget.FormWidget2;

public class FormsEditor extends BaseEditor {

	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private FormConfigImpl fc;

	private TDIConfigurationFile mc;

	private MetamergeConfigChangeListener configListener;

	private boolean formDeleted;

	private FormEditorWidget formEditor;
	
	public FormsEditor() {
	}

	@Override
	public void createPartControl(Composite parent) {

		BaseConfiguration config = getTDIConfiguration();
		try {
			initFormConfigObject(config);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
			return;
		}

		formEditor = new FormEditorWidget(parent, SWT.NONE, fc, this);
	}

	@Override
	public boolean isDirty() {
		if (mc.getModified())
			return true;
		else
			return super.isDirty();
	}


	private void initFormConfigObject(BaseConfiguration config) throws Exception {
		String str;
		if (config instanceof FunctionConfig)
			str = ((FunctionConfig) config).getFunctionConfig().getStringParameter(FormWidget2.EMBEDDED_FORM_NAME);
		else if (config instanceof ConnectorConfig)
			str = ((ConnectorConfig) config).getConnectionConfig().getStringParameter(FormWidget2.EMBEDDED_FORM_NAME);
		else
			str = config.getStringParameter(FormWidget2.EMBEDDED_FORM_NAME);

		if (str != null) {
			mc = new TDIConfigurationFile(new ByteArrayInputStream(str.getBytes("UTF-8")), false);
			fc = (FormConfigImpl) mc.getDefaultConfigObject();
			fc.setParent(config);
		} else {
			mc = new TDIConfigurationFile();
			mc.initializeConfig();

			FormConfig defaultForm = getDefaultForm(config);
			if(defaultForm != null && MessageDialog.openQuestion(getSite().getShell(), getPartName(), Messages.getString("FormsEditor.copy.default"))) {
				fc = (FormConfigImpl) defaultForm.getClone();
			} else {
				fc = new FormConfigImpl();
				fc.init();
			}
			fc.setName("Form");
			mc.setDefaultConfigObject(fc.getShortName(), fc);
			fc.setMetamergeConfig(mc);
		}

		configListener = new MetamergeConfigChangeListener() {
			public void configurationChanged(MetamergeConfigChange arg0) {
				firePropertyChange(PROP_DIRTY);
			}
		};
		fc.addListener(configListener);
	}
	
	private FormConfig getDefaultForm(BaseConfiguration editingConfiguration) {
		String formName = null;
		if (editingConfiguration instanceof RawConnectorConfig)
			formName = ((RawConnectorConfig) editingConfiguration).getJavaClass();
		else if (editingConfiguration instanceof ConnectorConfig)
			formName = ((ConnectorConfig) editingConfiguration).getConnectionConfig().getJavaClass();
		else if (editingConfiguration instanceof ParserConfig)
			formName = ((ParserConfig) editingConfiguration).getJavaClass();
		else if (editingConfiguration instanceof FunctionConfig)
			formName = ((FunctionConfig) editingConfiguration).getJavaClass();
		else if (Utils.getParentConfig(editingConfiguration, FunctionConfig.class) != null)
			formName = Utils.getParentConfig(editingConfiguration, FunctionConfig.class).getJavaClass();
		else if (editingConfiguration instanceof LogConfigItem)
			formName = Utils.getFormName((LogConfigItem) editingConfiguration);
		
		if(formName == null)
			return null;
		
		FormConfig formConfig = null;
		// Try system/internal namespaces
		try {
			formConfig = Utils.getSystemForm(formName);
			if (formConfig != null)
				return formConfig;
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}

		// Backwards compat code
		try {
			String f = (formName.indexOf(":") == -1 ? "system:/Forms/" + formName : formName);
			if (editingConfiguration != null)
				formConfig = (FormConfig) editingConfiguration.getMetamergeConfig().lookup(f);
			else
				formConfig = (FormConfig) MetamergeConfigFactory.lookup(null, f);
		} catch (Exception e) {
			formConfig = null;
		}
		
		return formConfig;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		BaseConfiguration config = getTDIConfiguration();
		String data = null;
		if(!formDeleted) {
			try {
				fc = (FormConfigImpl) mc.getDefaultConfigObject();
				mc.setDefaultConfigObject(fc.getShortName(), fc);
				ByteArrayOutputStream bos = mc.commitVersion(false);
				data = new String(bos.toByteArray(), "UTF-8");
				fc.setModified(false);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getSite().getShell());
				return;
			}

			if (data != null) {
				if (config instanceof FunctionConfig)
					((FunctionConfig) config).getFunctionConfig().setStringParameter(FormWidget2.EMBEDDED_FORM_NAME, data);
				else if (config instanceof ConnectorConfig)
					((ConnectorConfig) config).getConnectionConfig().setStringParameter(FormWidget2.EMBEDDED_FORM_NAME, data);
				else
					config.setStringParameter(FormWidget2.EMBEDDED_FORM_NAME, data);
			}
		}

		super.doSave(monitor);
	}

	public void deleteForm() {
		BaseConfiguration config = getTDIConfiguration();
		if (config instanceof FunctionConfig)
			((FunctionConfig) config).getFunctionConfig().removeParameter(FormWidget2.EMBEDDED_FORM_NAME);
		else if (config instanceof ConnectorConfig)
			((ConnectorConfig) config).getConnectionConfig().removeParameter(FormWidget2.EMBEDDED_FORM_NAME);
		else
			config.removeParameter(FormWidget2.EMBEDDED_FORM_NAME);
		
		formDeleted = true;
		formEditor.dispose();
	}

}
