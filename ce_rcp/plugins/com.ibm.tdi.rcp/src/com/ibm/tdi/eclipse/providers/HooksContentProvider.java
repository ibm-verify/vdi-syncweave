/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.util.HookTree;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.widget.HookItemWidget;

public class HooksContentProvider extends ColumnLabelProvider implements IStructuredContentProvider, ITreeContentProvider, MetamergeConfigChangeListener {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Object input;
	private static Color BLUE = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
	
	private ConnectorConfig cc = null;

	private TreeViewer viewer;
	
	public HooksContentProvider() {
		super();
	}
	
	public Image getImage(Object element) {
		HookConfig hc = null;
		if(element instanceof HookTree) {
			HookTree ht = (HookTree) element;
			hc = ht.getHookConfig(false);
			
		} else if(element instanceof HookConfig) {
			hc = (HookConfig) element;
		}

		if(hc != null && hc.getScript() != null && hc.getScript().length() > 0)
			return Activator.getImage("Script");
		else
			return null;
	}

	public String getText(Object element) {
		if(element instanceof String)
			return Messages.getString("Hook." + element);
		
		if (element instanceof HookConfig) {
			HookConfig hc = (HookConfig) element;
			return hookLabel(hc);
		}
		
		if(element instanceof HookTree) {
			HookTree ht = (HookTree) element;
			String str = ht.toString();
			if(ht.getHookConfig(false) != null)
				return hookLabel(ht.getHookConfig(false));
			else
				return str;
		}
		
		if(element instanceof Object[]) {
			return HookTree.getArrayName(element);
		}

		return "" + element;
	}

    public Color getForeground(Object element){
		HookConfig hc = null;
		if(element instanceof HookTree)
			hc = ((HookTree) element).getHookConfig(false);			
		else if(element instanceof HookConfig)
			hc = (HookConfig) element;

		if (hc != null && !hc.isParameterLocal(InternalSchema.HC_SCRIPT))
			return BLUE;
		return null;
    }

	public static String hookLabel(HookConfig hc) {
		return Messages.getString("Hook." + hc.getHookName());
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	public Object[] getElements(Object inputElement) {
		HookTree ht = null;
		if(inputElement instanceof AssemblyLineConfig)
			ht = HookTree.getHookTree((AssemblyLineConfig)inputElement);
		else if (inputElement instanceof ConnectorConfig)
			ht = HookTree.getHookTree( (ConnectorConfig)inputElement);
		else if (inputElement instanceof Object[])
			return (Object[])inputElement;
		else if (inputElement instanceof HookTree)
			ht = ((HookTree)inputElement);
		else if (inputElement instanceof HooksConfig)
			ht = HookTree.getHookTree((HooksConfig)inputElement);
		
		if ( ht == null)
			return null;
		return ht.getChildrenArray();
	}

	public void dispose() {
		if ( cc != null )
			cc.removeListener(this);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if ( cc != null )
			cc.removeListener(this);
		this.viewer = viewer instanceof TreeViewer ? (TreeViewer) viewer : null;
		input = newInput;
		cc = (ConnectorConfig) Utils.getParentConfig(input, ConnectorConfig.class);
		if ( cc != null )
			cc.addListener(this);
	}

	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof Object[])
			return true;
		else if(element instanceof HookTree)
			return ((HookTree)element).hasChildren();
		else
			return false;
	}
	
	public HookTree getItemFor(String hook) {
		return getItemFor(hook, getElements(input));
	}
	
	private HookTree getItemFor(String hook, Object[] elements) {
		for(Object obj : elements) {
			if(obj instanceof HookTree) {
				HookTree ht = (HookTree) obj;
				HookTree result = null;
				if(hook.equals(ht.getName()))
					result = ht;
				else if(ht.hasChildren())
					result = getItemFor(hook, ht.getChildrenArray());
				
				if(result != null)
					return result;
			}
		}
		
		return null;
	}

	@Override
	public String getToolTipText(Object element) {
		HookTree ht = (HookTree) element;
		String resource = "Hook.tooltip." + ht.getName();
		String tooltip = Messages.getString(resource);
		String str = Utils.getInheritsFromExt(ht.getHookConfig(false));
		if(str != null) {
			if(tooltip == null)
				tooltip = "";
			else
				tooltip += "\n";
			tooltip += Messages.getString("HooksWidget.0") + ": " + str;
		}
		return tooltip;
	}

	public void configurationChanged(MetamergeConfigChange changeEvent) {
		Object source = changeEvent.getSource();
		String key = ""+changeEvent.getKey();
		
		if (viewer == null)
			return;
	
		boolean refresh = false;
		if (source == cc && InternalSchema.CONNECTOR_MODE.equals(key)) {
			refresh = true;
		} else if (source instanceof HookConfig && InternalSchema.INHERITS_FROM.equals(key)) {
			refresh = true;
		}
		
		if(refresh) {
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					viewer.refresh();	
				}	
			});
		}
	}
}
