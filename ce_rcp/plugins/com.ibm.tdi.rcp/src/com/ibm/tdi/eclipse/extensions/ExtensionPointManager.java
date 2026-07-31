/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.extensions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class ExtensionPointManager implements SelectionListener {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	// -- The extension point names for the various standard components
	public static final String XP_ASSEMBLYLINE = "assemblyline"; //$NON-NLS-1$
	public static final String XP_JAVASCRIPT = "javascript"; //$NON-NLS-1$
	public static final String XP_PROPERTIES = "properties"; //$NON-NLS-1$
	public static final String XP_CONNECTOR = "connector"; //$NON-NLS-1$

	// -- The outline extension point name
	public static final String XP_OUTLINE = "outline"; //$NON-NLS-1$
	
	// -- The toolbar extension point name
	public static final String XP_TOOLBAR = "toolbar"; //$NON-NLS-1$
	
	public static final String TDI_ACTION_HANDLER = "TDI_ACTION_HANDLER"; //$NON-NLS-1$
	
	private ArrayList<BaseConfiguration> targetConfigObjects = null;
	private BaseConfiguration editingConfig;
	private Object source;
	
	public void invokeActionHandler(Object widget) {
		IAction action = null;
		if(widget instanceof Widget)
			action = (IAction) ((Widget)widget).getData(TDI_ACTION_HANDLER);
		
		if(action != null) {
			if(action instanceof TDIExtension) {
				((TDIExtension)action).setSource(getSource());
				((TDIExtension)action).setEditingConfiguration(getEditingConfig());
				((TDIExtension)action).setTargetConfigurationObjects(getTargetConfigObjects());
			}
			action.run();
		}
	}

	public void setEditingConfigObject(BaseConfiguration editingConfig) {
		this.editingConfig = editingConfig;
	}

	public BaseConfiguration getEditingConfig() {
		return editingConfig;
	}

	public ArrayList<BaseConfiguration> getTargetConfigObjects() {
		return targetConfigObjects;
	}
	
	public void setTargetConfigObjects(ArrayList<BaseConfiguration> targets) {
		targetConfigObjects = targets;
	}

	/**
	 * This method loads the menu extensions for the specified pointName. Each extension
	 * is added to the menu and the list of added items is returned to the caller.
	 * @param menu
	 */
	public ArrayList<MenuItem> addMenuExtensions(String pointName, String type, Menu menu) {
		IExtension e = Utils.getExtensionPointFor(pointName);
		if(e != null)
			return loadMenuExtensions(e, type, menu);
		else
			return new ArrayList<MenuItem>();
	}
	
	public void addActionToMenu(IAction action, Menu menu, SelectionListener listener) {
		MenuItem mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(action.getText());
		mi.setData(TDI_ACTION_HANDLER, action);
		if(listener != null)
			mi.addSelectionListener(listener);
		else
			mi.addSelectionListener(this);
	}
	
	private ArrayList<MenuItem>  loadMenuExtensions(IExtension extension, String type, Menu menu) {
		ArrayList<MenuItem> items = new ArrayList<MenuItem>();
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for(IConfigurationElement e : elements) {
			if(!e.getName().equals(type))
				continue;
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(e.getAttribute("label")); //$NON-NLS-1$

			// -- Icon
//			ImageDescriptor imageDescriptor = getImageDescriptor(e);
//			if(imageDescriptor != null)
//				item.setImage(imageDescriptor.createImage(true));
			
			try {
				IAction target = (IAction) e.createExecutableExtension("class"); //$NON-NLS-1$
				if(target != null) {
					target.setText(e.getAttribute("label")); //$NON-NLS-1$
					item.setData(TDI_ACTION_HANDLER, target);
				}
			}catch (Exception error) {
				EclipseAppender.logerror("LoadExtensions: " + error.toString(), error); //$NON-NLS-1$
			}
			items.add(item);
		}
		
		return items;
	}

	/**
	 * This method loads the toolbar extensions for the specified pointName. Each extension
	 * is added to the toolbar and the list of added items is returned to the caller.
	 */
	public ArrayList<ToolItem> addToolbarExtensions(String pointName, String type, ToolBar toolbar, SelectionListener listener) {
		IExtension e = Utils.getExtensionPointFor(pointName);
		if(e != null)
			return loadToolbarExtensions(e, type, toolbar, listener);
		else
			return new ArrayList<ToolItem>();
	}
	
	private ArrayList<ToolItem>  loadToolbarExtensions(IExtension extension, String type, ToolBar toolbar, SelectionListener listener) {
		ArrayList<ToolItem> items = new ArrayList<ToolItem>();
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for(IConfigurationElement e : elements) {
			if(!e.getName().equals(type))
				continue;
			try {
				String str = e.getAttribute("style"); //$NON-NLS-1$
				int style = SWT.PUSH;
				if("check".equalsIgnoreCase(str)) //$NON-NLS-1$
					style = SWT.CHECK;
				else if("dropdown".equalsIgnoreCase(str)) //$NON-NLS-1$
					style = SWT.DROP_DOWN;
				
				// -- Create ToolItem and add listener
				ToolItem item = new ToolItem(toolbar, style);
				if(listener != null)
					item.addSelectionListener(listener);
				else
					item.addSelectionListener(this);
				
				// -- ToolTipText
				if(e.getAttribute("tooltip") != null) //$NON-NLS-1$
					item.setToolTipText(e.getAttribute("tooltip")); //$NON-NLS-1$
				
				// -- Icon
				ImageDescriptor imageDescriptor = getImageDescriptor(e);
				if(imageDescriptor != null)
					item.setImage(imageDescriptor.createImage(true));
				
				Object target = e.createExecutableExtension("class"); //$NON-NLS-1$
				if(target instanceof Control)
					item.setControl((Control) target);
				if (target instanceof IAction) {
					item.setData(TDI_ACTION_HANDLER, target);
				}
				items.add(item);
			}catch (Exception error) {
				EclipseAppender.logerror(error.toString(), error);
			}
		}
		
		return items;
	}

	private ImageDescriptor getImageDescriptor(IConfigurationElement e) {
		String imageFilePath = e.getAttribute("icon"); //$NON-NLS-1$
		if(imageFilePath == null || imageFilePath.trim().length() == 0)
			return null;
		
		String pluginId = e.getDeclaringExtension().getNamespaceIdentifier();
		return AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, imageFilePath);
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e) {
		setTargetConfigObjects(null);
		invokeActionHandler(e.getSource());
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public void addTargetConfigObjects(BaseConfiguration[] objs, boolean clear) {
		if(targetConfigObjects == null || clear)
			targetConfigObjects = new ArrayList<BaseConfiguration>();
		for(BaseConfiguration b : objs)
			targetConfigObjects.add(b);
	}

	public void addTargetConfigObject(BaseConfiguration obj) {
		if(targetConfigObjects == null)
			targetConfigObjects = new ArrayList<BaseConfiguration>();
		targetConfigObjects.add(obj);
	}

	public List<IAction> createMenuActions(String pointName) {
		return createActions(pointName, XP_OUTLINE);		
	}
	
	public List<IAction> createToolBarActions(String pointName) {
		return createActions(pointName, XP_TOOLBAR);		
	}
	
	public List<IAction> createActions(String pointName, String type) {
		ArrayList<IAction> list = new ArrayList<IAction>();
		IExtension extension = Utils.getExtensionPointFor(pointName);
		if(extension == null)
			return list;
		
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for(IConfigurationElement e : elements) {
			if(!e.getName().equals(type))
				continue;

			try {
				IAction target = (IAction) e.createExecutableExtension("class"); //$NON-NLS-1$
				if(target != null) {
					target.setText(e.getAttribute("label")); //$NON-NLS-1$
					target.setToolTipText(e.getAttribute("tooltip")); //$NON-NLS-1$
					list.add(target);
				}
			}catch (Exception error) {
				EclipseAppender.logerror(error.toString(), error);
			}
		}
		return list;
	}

}
