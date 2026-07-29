/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.editors.PropertiesEditor;

public class PropertyContentProvider extends AbstractConfigProvider implements ITreeContentProvider, ITableLabelProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Viewer viewer;
	private boolean notificationsEnabled = true;
	
	public void configurationChanged(MetamergeConfigChange changeEvent) {
		if(viewer != null && isNotificationsEnabled()) {
			((TableViewer)viewer).refresh(changeEvent.getSource());
		}
	}

	public boolean isNotificationsEnabled() {
		return notificationsEnabled;
	}

	public void setNotificationsEnabled(boolean notificationsEnabled) {
		this.notificationsEnabled = notificationsEnabled;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		
		if(oldInput != null && oldInput instanceof BaseConfiguration)
			((BaseConfiguration)oldInput).removeListener(this);
		if(newInput instanceof BaseConfiguration)
			((BaseConfiguration)newInput).addListener(this);
		if(newInput instanceof ContainerConfig)
			sort((ContainerConfig)newInput);
	}

	private void sort(ContainerConfig config) {
		ArrayList<String> list = new ArrayList<String>();
		for(int i = 0; i < config.size(); i++) {
			list.add(config.getConfig(i).getShortName());
		}
		
		BaseConfiguration parent = config.getParent();
		config.setParent(null);
		boolean modified = config.getModified();

		Collections.sort(list);
		for(int i = 0; i < list.size(); i++) {
			config.moveConfig(config.getConfig(list.get(i)), i);
		}
		
		config.setModified(modified);
		config.setParent(parent);
	}

	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof ContainerConfig)
			return ((ContainerConfig)inputElement).getConfigurations(null).toArray();
		else
			return null;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		BaseConfiguration e = (BaseConfiguration) element;
		switch(columnIndex) {
		case 0:
			return getName(e);
		case 1:
			String str = e.getStringParameter(PropertiesEditor.LOCAL_PROTECT);
			if(str == null || str.equals(""))
				str = "false";
			return str;
		case 2:
			if(Boolean.valueOf(e.getStringParameter(PropertiesEditor.LOCAL_PROTECT)))
				return "********";
			else
				return e.getStringParameter(PropertiesEditor.LOCAL_VALUE);
		case 3:
			if(Boolean.valueOf(e.getStringParameter(PropertiesEditor.LOCAL_PROTECT)))
				return "********";
			else
				return e.getStringParameter(PropertiesEditor.SERVER_VALUE);
		}
		return e.toString();
	}

	public static String getName(BaseConfiguration e) {
		String s = e.getShortName();
		if (s == null)
			s = "";
		if (PropertiesEditor.isDeleted(e))
			return Messages.getMessage("ConfigLabelProvider.deleted", s);
		String serverName = e.getStringParameter(PropertiesEditor.SERVER_NAME);
		if (serverName == null)
			return Messages.getMessage("ConfigLabelProvider.new", s);
		if (!s.equals(serverName))
			return Messages.getMessage("ConfigLabelProvider.was", s, serverName);
		return s;
	}
	
	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		if(element instanceof BaseConfiguration)
			return ((BaseConfiguration)element).getParent();
		else
			return null;
	}

	public boolean hasChildren(Object element) {
		return (element instanceof ContainerConfig);
	}

	public void addListener(ILabelProviderListener listener) {}
	public void removeListener(ILabelProviderListener listener) {}
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

}
