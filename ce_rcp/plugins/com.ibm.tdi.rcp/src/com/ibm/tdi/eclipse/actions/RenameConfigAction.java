/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.operations.RenamePropertiesOperation;
import com.ibm.tdi.eclipse.dialogs.CheckboxInputDialog;
import com.ibm.tdi.eclipse.dialogs.ComboInputDialog;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.editors.PropertiesEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.validators.IllegalCharValidator;

public class RenameConfigAction extends BaseAction implements IInputValidator {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private AttributeMapItem ami;
	private AttributeMapConfig amc;

	private SchemaItemConfig sci;
	private SchemaConfig sc;
	private ContainerConfig container;
	private BaseConfiguration item;
	private AssemblyLineConfig alc;
	private IInputValidator validator;

	private static String title = Messages.getString("general.rename.label");

	public RenameConfigAction() {
	}

	public void run(IAction action) {
		if (action != null && !action.isEnabled())
			return;
		item = (BaseConfiguration) getFirstSelection();
		sci = null;
		sc = null;
		container = null;
		ami = null;
		amc = null;

		if(item instanceof HookConfig || item instanceof HooksConfig || item instanceof AttributeMapConfig || item instanceof SchemaConfig)
			return;
		if(item instanceof AttributeMapItem) {
			renameAttributeMapItem(item);
			return;
		}

		if (item instanceof SchemaItemConfig) {
			sci = (SchemaItemConfig) item;
			if (sci.getParent() instanceof SchemaConfig)
				sc = (SchemaConfig) sci.getParent();
		}

		if (item.getParent() instanceof ContainerConfig)
			container = (ContainerConfig) item.getParent();

		if (item instanceof ConnectorConfig || item instanceof ScriptConfig || item instanceof BranchingConfig) {
			alc = Utils.getParentConfig(item, AssemblyLineConfig.class);
			validator = new IllegalCharValidator();
		}

		if (isPropertyItem()) {
			return;
		}

		while(true) {
			InputDialog id = new InputDialog(getShell(), title, title + ": ", item.getShortName(), this);
			if(id.open() == Window.CANCEL)
				return;
			String name = id.getValue();
			if (name == null || name.trim().length() == 0)
				return;
			name = name.trim();
			try {
				if(sci != null) {
					String oldName = sci.getAttributeName();
					if (name.equals(oldName))
						return;
					if (sc != null) { // No rename method.
						sc.removeItem(oldName);
						sc.setItem(name, sci);
					}
					sci.setAttributeName(name);
				}
				item.setName(name);
				return;
			} catch (Exception e) {
				EclipseAppender.logerror(Messages.getMessage("RenameConfigAction.Error", name), e, getShell());
			}
		}
	}

	private boolean isPropertyItem() {
		if (!PropertiesEditor.isProperty(item) || container == null)
			return false;
		if (PropertiesEditor.isDeleted(item))
			return true; // true means that it cannot be renamed.
		MetamergeConfig mc = item.getMetamergeConfig();
		if (! (mc instanceof TDIConfigurationFile))
			return false;
		TDIConfigurationFile file = (TDIConfigurationFile)mc;
		if (file.getFile() == null ||
				! TDIConfigurationFile.XT_PROPSTORE.equals(file.getFile().getFileExtension()))
			return false;

		changePropertyName(file);
		return true;
	}

	private void changePropertyName(TDIConfigurationFile file) {
		while(true) {
			CheckboxInputDialog id = new CheckboxInputDialog(getShell(), title, title + ": ", 
					item.getShortName(), 
					Messages.getString("RenameConfigAction.update"), 
					Messages.getString("RenameConfigAction.update.tooltip"), true, this);
			if(id.open() == Window.CANCEL)
				return;
			String name = id.getValue();
			if (name == null || name.trim().length() == 0)
				return;
			name = name.trim();
			String oldName = item.getShortName();
			if (name.equals(oldName))
				return;
			BaseConfiguration bc = container.getConfig(name);
			if (bc == item)
				return;
			item.setParameter(PropertiesEditor.DELETED, "true");
			try {
				if (bc != null) {
					bc.removeParameter(PropertiesEditor.DELETED);
					bc.setParameter(PropertiesEditor.LOCAL_VALUE, item.getParameter(PropertiesEditor.LOCAL_VALUE));
					bc.setParameter(PropertiesEditor.LOCAL_PROTECT, item.getParameter(PropertiesEditor.LOCAL_PROTECT));
				} else {
					bc = new BaseConfigurationImpl();
					bc.init();
					bc.setName(name);
					bc.setStringParameter(PropertiesEditor.PROPERTY, "true");
					bc.setParameter(PropertiesEditor.LOCAL_VALUE, item.getParameter(PropertiesEditor.LOCAL_VALUE));
					bc.setParameter(PropertiesEditor.LOCAL_PROTECT, item.getParameter(PropertiesEditor.LOCAL_PROTECT));
					container.addConfig(bc);
				}

				if (id.getSelection()) {
					// Save first
					if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
						FileEditorInput input = new FileEditorInput(file.getFile());
						IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findEditor(input);
						if ( part instanceof BaseEditor) {
							((BaseEditor)part).doSave(null);
						}
					}

					RenamePropertiesOperation op = new RenamePropertiesOperation(file.getFile(), oldName, name);
					op.processAllFilesInProject();
				}
				return;
			} catch (Exception e) {
				EclipseAppender.logerror(Messages.getMessage("RenameConfigAction.Error", name), e, getShell());
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		Object o = getFirstSelection();
		if (! (o instanceof BaseConfiguration)) {
			action.setEnabled(false);
		} else if(o instanceof HookConfig || o instanceof HooksConfig || o instanceof AttributeMapConfig || o instanceof SchemaConfig) {
			action.setEnabled(false);
		} else if(o instanceof ContainerConfig) {
			ContainerConfig cc = (ContainerConfig) o;
			if(cc.getParent() instanceof AssemblyLineConfig)
				action.setEnabled(false);
		} else {
			BaseConfiguration bc = (BaseConfiguration) o;
			if (PropertiesEditor.isProperty(bc) && PropertiesEditor.isDeleted(bc))
				action.setEnabled(false);
		}
	}

	private void renameAttributeMapItem(BaseConfiguration b) {
		ami = (AttributeMapItem)b;
		amc = (AttributeMapConfig)Utils.getParentConfig(ami, AttributeMapConfig.class);

		List<String> list = null;
		if (!Utils.isInputMap(amc)) {
			ConnectorConfig cc = (ConnectorConfig) Utils.getParentConfig(amc, ConnectorConfig.class);
			if ( cc != null)
				list = cc.getSchema(false).getItemNames();
		}
		String newName = ami.getShortName();
		if ( list != null && list.size() > 0 ) {
			if (list.indexOf(newName) == -1 )
				list.add(0, newName);
			ComboInputDialog cid = new ComboInputDialog(getShell(), title, Messages.getString("RenameWorkAttributeItem.1"), list);
			if(cid.open() == Window.OK)
				newName = cid.getValue();

		} else {
			InputDialog id = new InputDialog(getShell(), title, Messages.getString("RenameWorkAttributeItem.1"), newName, this);
			if(id.open() == Window.OK)
				newName = id.getValue();
		}

		if(newName != null && newName.trim().length() > 0) {
			newName = newName.trim();
			if ( newName.equals(ami.getShortName()) )
				return;
			try {
				if(amc == null)
					ami.setName(newName);
				else if (amc.hasAttributeMapItem(newName) 
						&& amc.getAttributeMapItem(newName) != ami)
					MessageDialog.openError(getShell(), title, Messages.getString("RenameConfigAction.AlreadyExists"));
				else
					amc.renameAttributeMapItem(newName, ami);
			} catch (Exception e) {
				EclipseAppender.logerror(Messages.getMessage("RenameConfigAction.Error", newName), e, getShell());
			}
		}
	}

	public String isValid(String newText) {
		if (newText == null)
			return null;
		newText = newText.trim();
		if (sci != null && newText.equals(sci.getAttributeName()))
			return null;
		if (ami != null && newText.equalsIgnoreCase(ami.getShortName()))
			return null;
		if (sc !=  null && sc.getItemNames().indexOf(newText)>=0)
			return Messages.getString("RenameConfigAction.AlreadyExists");			
		if (amc != null && amc.hasAttributeMapItem(newText))
			return Messages.getString("RenameConfigAction.AlreadyExists");
		if (container != null) {
			BaseConfiguration existing = container.getConfig(newText);
			if (existing != null && existing != item && ! 
					(PropertiesEditor.isDeleted(existing) && PropertiesEditor.isProperty(existing)))
				return Messages.getString("RenameConfigAction.AlreadyExists");	
		}
		if (alc != null) {
			BaseConfiguration existing = alc.getComponent(newText);
			if (existing != null && existing != item)
				return Messages.getString("RenameConfigAction.AlreadyExists");
			return validator.isValid(newText);
		}

		return null;
	}

}
