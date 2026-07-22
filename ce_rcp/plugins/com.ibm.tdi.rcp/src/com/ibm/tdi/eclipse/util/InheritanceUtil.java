/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.util.HookTree;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class InheritanceUtil {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static BaseConfiguration getConfigFromSelection(Object o) {
		if (o instanceof BaseConfiguration) {
			return (BaseConfiguration) o;
		} else if (o instanceof HookTree) {
			return ((HookTree)o).getHookConfig(false);
		}
		return null;
	}

	public static void changeInheritance(BaseConfiguration config) {
		Vector<Object> v = new Vector<Object>();
		Object target = null;
		try {
			if ( config instanceof AttributeMapConfig ) {
				v.add(BaseConfiguration.INHERIT_NONE);
				v.add(BaseConfiguration.INHERIT_PARENT);							
				ConfigUtils.getAllFiles(config, v, MetamergeConfig.ATTRIBUTEMAP_FOLDER);
				ConfigUtils.getAllFiles(config, v, MetamergeConfig.CONNECTOR_FOLDER);
				ConfigUtils.getAllFiles(config, v, MetamergeConfig.FUNCTION_FOLDER);
			} else if (config instanceof HookConfig || config instanceof ScriptConfig) {
				v.add(BaseConfiguration.INHERIT_NONE);
				v.add(BaseConfiguration.INHERIT_PARENT);							
				ConfigUtils.getAllFiles(config, v, MetamergeConfig.SCRIPT_FOLDER);
			} else if (config instanceof AttributeMapItem) {
				v.add(BaseConfiguration.INHERIT_PARENT);							
				ConfigUtils.getAllFiles(config, v, MetamergeConfig.ATTRIBUTEMAP_FOLDER);
				ConfigUtils.getAllFiles(config, v, MetamergeConfig.SCRIPT_FOLDER);
				ConfigUtils.getAllFiles(config, v, MetamergeConfig.CONNECTOR_FOLDER);
				ConfigUtils.getAllFiles(config, v, MetamergeConfig.FUNCTION_FOLDER);
			} else if (config instanceof SchemaConfig) {
				v.add(BaseConfiguration.INHERIT_NONE);
				v.add(BaseConfiguration.INHERIT_PARENT);			
				ConfigUtils.getAllFiles(config, v, MetamergeConfig.CONNECTOR_FOLDER);
				ConfigUtils.getAllFiles(config, v, MetamergeConfig.FUNCTION_FOLDER);
				ConfigUtils.getAllFiles(config, v, MetamergeConfig.PARSER_FOLDER);
			} else if (config instanceof FunctionConfig) {
				ConfigUtils.getAllFiles(config, v, MetamergeConfig.FUNCTION_FOLDER);
			} else if (config instanceof ConnectorConfig) {
				ConfigUtils.getAllFiles(config, v, MetamergeConfig.CONNECTOR_FOLDER);				
			} else {
				FilteredResourcesSelectionDialog fd = new FilteredResourcesSelectionDialog(
						Display.getCurrent().getActiveShell(),
						false, ((TDIConfigurationFile)config.getMetamergeConfig()).getProject().getParent(),
						IResource.FILE);
				if(fd.open() != Window.OK)
					return;
				target = fd.getResult()[0];
			}

			if ( target == null){
				String msg = Messages.getString("InheritanceUtil.1");
// TODO: If we want to add the name, we must use a proper translated string,
// we cannot just concatenate strings.
//				if (config.getShortName() != null &&
//					!(config instanceof SchemaConfig ||
//					  config instanceof AttributeMapConfig))
//					msg += " - " + config.getShortName();
				target = chooseFromList(msg, v, config.getInheritsFromRef(),
						(config instanceof AttributeMapConfig) && ! (config instanceof HooksConfig));
			}
			changeInheritance(config, target);
		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}
	}

		public static void changeInheritance(BaseConfiguration config, Object target) {
			try {
			String inh = null;
			if (target instanceof IFile) {
				TDIConfigurationFile source = (TDIConfigurationFile) config.getMetamergeConfig();			
				inh = source.addReference((IFile)target, null);
			} else if (target instanceof String) {
				inh = (String) target;
				if(! isValidInheritanceString(config, inh))
					return;
			} else {
				return;
			}

			// Verify remove script for hooks
			if (config instanceof HookConfig && ! fixHook((HookConfig)config, inh))
				return;
			
			// Verify remove script for Script configs
			if (config instanceof ScriptConfig && ! fixScriptConfig((ScriptConfig)config, inh))
				return;
			
			
			config.setInheritsFromRef(inh);
			config.setupInheritanceChain();
			
			// Some special code for AttributeMapItem
			if (config instanceof AttributeMapItem)
				fixAMI((AttributeMapItem)config);

		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}
	}

	private static boolean fixScriptConfig(ScriptConfig script, String inh) {
		if (!script.hasParameter(InternalSchema.HC_SCRIPT))
			return true;
		if (inh == null || inh.equals(BaseConfiguration.INHERIT_NONE) || inh.equals(script.getInheritsFromRef()))
			return true;
		if (!MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
				Messages.getString("miadmin.prompt.confirm"),
				Messages.getString("RemoveLocalScript.Msg")))
			return false;
		script.removeParameter(InternalSchema.SCRIPT);
		return true;
	}
	
	private static boolean fixHook(HookConfig hook, String inh) {
		if (!hook.hasParameter(InternalSchema.HC_SCRIPT))
			return true;
		if (inh == null || inh.equals(BaseConfiguration.INHERIT_NONE) || inh.equals(hook.getInheritsFromRef()))
			return true;
		if (!MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
				Messages.getString("miadmin.prompt.confirm"),
				Messages.getString("RemoveLocalScript.Msg")))
			return false;
		hook.removeParameter(InternalSchema.HC_SCRIPT);
		return true;
	}
	
	private static void fixAMI(AttributeMapItem ami) {
		if (ami.getInheritsFrom() == null)
			return;
		// Since this AttributeMapItem now inherits, better remove all local parameters,
		// since we probably want to use the inherited parameters.
		ami.removeParameter(InternalSchema.AMI_SCRIPT);
		ami.removeParameter(InternalSchema.AMI_SIMPLE);
		ami.removeParameter(InternalSchema.AMI_SUBSTITUTION);
		ami.removeParameter(InternalSchema.AMI_MODIFY);
		ami.removeParameter(InternalSchema.AMI_ADD);
		if (ami.getInheritsFrom() instanceof ScriptConfig)
			ami.setType(AttributeMapItem.ADVANCED_MAPPING);
		else
			ami.removeParameter(InternalSchema.AMI_TYPE);
	}

	public static Object chooseFromList(String prompt, List<Object> list, String defVal) {
		return chooseFromList(prompt, list, defVal, false);
	}
	
	public static Object chooseFromList(String prompt, List<Object> list, String defVal, boolean allowFileSelection) {
		if (defVal != null && list.indexOf(defVal) == -1)
			list.add(0, defVal);
		ArrayList<String> v = new ArrayList<String>(list.size());
		for (Object obj : list) {
			if ( obj instanceof String ) {
				if (obj.equals(BaseConfiguration.INHERIT_NONE))
					v.add(Messages.getString("ConfigChooser.Localized.Inherit.None"));
				else if (obj.equals(BaseConfiguration.INHERIT_PARENT))
					v.add(Messages.getString("ConfigChooser.Localized.Inherit.Parent"));
				else
					v.add((String)obj);
			} else if (obj != null) {
				v.add(obj.toString());
			}
		}

		ComboSelectionDialog dlg = new ComboSelectionDialog(Display.getCurrent().getActiveShell(),
				v, prompt, Messages.getString("action.label.24"), allowFileSelection, defVal);
		if(dlg.open() == Window.OK) {
			return dlg.getValue();
		}
		return null;
	}

	/**
	 * A combo selection dialog
	 */
	private static class ComboSelectionDialog extends Dialog {

		private Combo combo;
		private List<String> options;
		private String prompt;
		private String title;
		private String value;
		private boolean allowFileSelection;
		private String selection;

		public ComboSelectionDialog(Shell parent, List<String> options, String prompt, String title, boolean allowFileSelection, String selection) {
			super(parent);
			this.options = options;
			this.prompt = prompt;
			this.title = title;
			this.allowFileSelection = allowFileSelection;
			this.selection = selection;
		}

		protected Control createDialogArea(Composite parent) {
			Composite c = (Composite) super.createDialogArea(parent);
			new Label(c, SWT.LEFT).setText(prompt);
			combo = new Combo(c, SWT.NONE);
			if (options != null ) {
				for (String opt : options)
					combo.add(opt);
			}
			if(selection != null) {
				int index = combo.indexOf(selection);
				if(index == -1) {
					combo.add(selection, 0);
					index = 0;
				}
				combo.select(index);
			}
			getShell().setText(title);
			return c;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			if(allowFileSelection)
				createButton(parent, 99, Messages.getString("InheritanceUtil.select.file"), false);
		}

		@Override
		protected void buttonPressed(int buttonId) {
			if(buttonId == 99) {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				String path = fd.open();
				if(path != null) {
					path = "file:" + path;
					combo.add(path);
					combo.select(combo.indexOf(path));
					options.add(path);
				}
			} else {
				super.buttonPressed(buttonId);
			}
		}

		protected void okPressed() {
			value = combo.getText();
			super.okPressed();
		}

		protected void cancelPressed() {
			value = null;
			super.cancelPressed();
		}

		public String getValue() {
			return value;
		}
	}
		
	private static boolean isValidInheritanceString(BaseConfiguration config, String inh) {		
		if (BaseConfiguration.INHERIT_PARENT.equals(inh) ||
				BaseConfiguration.INHERIT_NONE.equals(inh))
			return true;
		// Try to guess if the given string is valid as an inheritance reference, by using a clone
		try {
			BaseConfiguration clone = (BaseConfiguration) config.getClone();
			clone.setParent(config.getParent());
			clone.setInheritsFromRef(inh);
			clone.setupInheritanceChain();
			return clone.getInheritsFrom() != null;
		} catch (Exception e) {
			return false;			
		}
	}
}
