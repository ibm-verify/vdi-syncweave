/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.lang.reflect.Method;
import java.util.UUID;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.eclipse.MetamergeConfigCE;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.eclipse.TDIPropertiesCE;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.PropertiesEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class ConfigBinding implements Listener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private FormItemConfig formItem;
	private BaseConfiguration config;

	private boolean enabled = true;
	private String paramName;

	private Method setter = null;
	private Method getter = null;
	private boolean isBool;
	private Control control;

	private FormWidget2 form = null;
	
	public ConfigBinding(FormItemConfig formItem, BaseConfiguration editingConfig, FormWidget2 form) {
		this.formItem = formItem;
		this.config = editingConfig;
		this.form = form;
		paramName = formItem.getShortName();
		if (formItem.getReflect() != null) {
			isBool = "boolean".equalsIgnoreCase(formItem.getSyntax());
			getReflect(config.getClass(), formItem.getReflect());
		}
	}

	public void setControl(Control control) {
		this.control = control;
		control.addListener(SWT.Selection, this);
		control.addListener(SWT.Modify, this);
	}
	
	public void setControlNoListener(Control control) {
		this.control = control;
	}
	
	public void setValue(String value) {
		// XML behaves badly if we store lines with CRLF, so replace with single LF
		if (value.indexOf('\r')>=0)
			value = value.replace("\r\n", "\n");
		
		// -- Password field
		boolean protect = false;
		if(control instanceof Text && ((Text)control).getEchoChar() != 0) {
			protect = config.getMetamergeConfig() != null && !formItem.getDontProtect();
		}
		
		if (setter == null) {
			if (config.isParameterLocal(paramName) || wantToBreakInheritance()) {
				if(protect) {
					setProtectedParameter(value);
				} else {
					config.setParameter(paramName, value);
				}
				if (form != null)
					form.setLabelColor(paramName);
			} else if (form != null) {
				form.updateControl(paramName);
			}
		} else {
			try {
				if (isBool)
					setter.invoke(config, new Object[] {Boolean.valueOf(value)});
				else
					setter.invoke(config, new Object[] {value});
			} catch (Exception e) {
				EclipseAppender.logerror(paramName, e);
			}	
		}
	}

	public void setParameterPropertySource(String source) {
		if (config.isParameterLocal(paramName) || wantToBreakInheritance()) {

			// XML behaves badly if we store lines with CRLF, so replace with single LF
			if (source.indexOf('\r')>=0)
				source = source.replace("\r\n", "\n");

			config.setParameterPropertySource(paramName, source);
		}
	}

	public Object getValue() {
		if (getter == null) {
			return config.getParameter(paramName);
		} else {
			try {
				return getter.invoke(config, new Object[0]);
			} catch (Exception e) {
				EclipseAppender.logerror(paramName, e);
			}
			return null;
		}
	}

	protected void updateConfigValue(String value, int index) {
		if ( index < 0 )
			return;
		if(formItem.isIndexBased())
			setValue( ""+index );
		else if ("droplist".equalsIgnoreCase(formItem.getSyntax()) &&
				formItem.getValues().size() > index)
			setValue( formItem.getValues().get(index) );
		else
			setValue(value);
	}
	
	public void handleEvent(Event event) {
		if(!isEnabled())
			return;
		
		switch(event.type) {
		case SWT.Selection:
			if(event.widget instanceof Button)
				setValue("" + ((Button)event.widget).getSelection());
			else if(event.widget instanceof Combo)
				updateConfigValue(((Combo)event.widget).getText(), ((Combo)event.widget).getSelectionIndex());
			break;
		case SWT.Modify:
			if(event.widget instanceof Text)
				setValue(((Text)event.widget).getText());
			else if(event.widget instanceof Combo)
				setValue(((Combo)event.widget).getText());
			else if(event.widget instanceof SimpleTextEditor)
				setValue(((SimpleTextEditor)event.widget).getText());
			else if(event.widget instanceof StyledText)
				setValue(((StyledText)event.widget).getText());
			break;
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	private void getReflect(Class<?> cc, String reflect) {
		String getMethod = "get" + reflect;
		String setMethod = "set" + reflect;
		Class<?>[] params = { isBool ? Boolean.TYPE : String.class };
		try {
			setter = cc.getMethod(setMethod, params);
			getter = cc.getMethod(getMethod, new Class<?>[0]);
		} catch (Exception e) {
			EclipseAppender.logerror(paramName, e);
		}
 	}
	
	private boolean justSaidYes;

	public boolean mayModifyText() {
		if (config.isParameterLocal(paramName))
			return true;
		justSaidYes = wantToBreakInheritance();
		return justSaidYes;
	}
	
	private boolean wantToBreakInheritance() {
		if (justSaidYes) {
			justSaidYes = false;
			return true;
		}

		BaseConfiguration inh = config.getInheritsFrom();
		while (inh != null) {
			if (inh.hasParameter(paramName))
				break;
			inh = inh.getInheritsFrom();
		}
		
		String[] buttons;
		
		MetamergeConfig inhMC = (inh == null) ? null : inh.getMetamergeConfig();
		if (inhMC instanceof MetamergeConfigCE || inhMC instanceof TDIConfigurationFile) {
			buttons = new String[] { IDialogConstants.OK_LABEL,
                IDialogConstants.CANCEL_LABEL,
                Messages.getString("ConfigBinding.EditOriginal")};
		} else {
			buttons = new String[] { IDialogConstants.OK_LABEL,
                IDialogConstants.CANCEL_LABEL };
		}
		
		MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				Messages.getString("ConfigBinding.BreakInh.Title"), null,
				Messages.getString("ConfigBinding.BreakInh.Msg"),
				MessageDialog.QUESTION, buttons, 0);
		int i = dialog.open();
		if (control != null)
			control.setFocus();
		if ( i==0 )
			return true;
		if ( i == 2 )
			Utils.openEditorFor(inh);
		return false;
	}
	
	private void setProtectedParameter(String value) {
		try {
			MetamergeConfig mc = Utils.getProjectMC(Utils.getProjectFor(config));
			PropertyManager pm = (PropertyManager) mc.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
			String storeName = null;
			if(pm != null && pm.getPasswordPropertyStore() != null)
				storeName = pm.getPasswordPropertyStore().getShortName();
			if (storeName != null) {
				String propName = config.getParameterPropertySource(paramName);
				String pref = "{property." + storeName + ":";
				if (propName != null && propName.startsWith(pref) && propName.endsWith("}")) {
					propName = propName.substring(pref.length(), propName.length() - 1);
				} else {
					propName = paramName + "-" + UUID.randomUUID();
					config.setParameterPropertySource(paramName, pref + propName + "}");
				}

				TDIConfigurationFile file = TDIPropertiesCE.loadPropertyStore(config, storeName);
				ContainerConfig cc = (ContainerConfig) file.getDefaultConfigObject();
				ContainerConfig data = (ContainerConfig) cc.getConfig("Data"); //$NON-NLS-1$
				BaseConfiguration bc = data.getConfig(propName);
				if (bc == null) {
					bc = new BaseConfigurationImpl();
					bc.init();
					bc.setName(propName);
					bc.setStringParameter(PropertiesEditor.PROPERTY, "true");
					data.addConfig(bc);
				} else {
					bc.removeParameter(PropertiesEditor.DELETED);
				}
				bc.setStringParameter(PropertiesEditor.LOCAL_VALUE, value);
				bc.setStringParameter(PropertiesEditor.LOCAL_PROTECT, "true");
				PropertiesEditor.verifyEncrypted(bc);
				file.commitChanges(null);
				return;
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, control.getShell());
		}
		config.setParameter(paramName, value);
		config.setProtectedParameter(paramName);
	}
}
