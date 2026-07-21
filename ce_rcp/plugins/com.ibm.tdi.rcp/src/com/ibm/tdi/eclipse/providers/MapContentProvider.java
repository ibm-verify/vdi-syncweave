/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.di.config.base.AttributeMapItemImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;

/**
 * Provides a TreeViewer with the Mapping contents of an AssemblyLineConfig or ConnectorConfig.
 * 
 */
public class MapContentProvider implements ITreeContentProvider, MetamergeConfigChangeListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BaseConfiguration input = null; // for removing the listener
	
	private Viewer viewer;
	
	private boolean batchChange = false;
	
	public MapContentProvider() {
		super();
	}

	public void dispose() {
		if (input != null)
			input.removeListener(this);
	}

	/**
	 * Creates a place holder object used by the AL editor to create a new
	 * component
	 * 
	 * @param parent
	 *            The container in which the replaced object should appear
	 * @return
	 */
	private Object createPlaceHolder(BaseConfiguration parent) {
		AttributeMapItem bc = new AttributeMapItemImpl();
		try {
			bc.init();
			bc.setScript(Messages.getString("AttributeMap.empty.output"));
			bc.setParameter("%%PLACEHOLDER%%", parent);
		} catch (Exception e) {
			SystemFunctions.doNothing(); //Cannot happen
		}
		return bc;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.tdi.eclipse.providers.AbstractConfigProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (input != null) {
			input.removeListener(this);
			input = null;
		}
		if (newInput instanceof BaseConfiguration) {
			((BaseConfiguration)newInput).addListener(this);
			input = ((BaseConfiguration)newInput);
		}
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object element) {
		ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();

		if (element instanceof AssemblyLineConfig) {
			getChildren((AssemblyLineConfig)element, list);
		} else if (element instanceof ConnectorConfig) {
			getChildren((ConnectorConfig)element, list);
		} else if (element instanceof OperationConfig) {
			list.add(((OperationConfig)element).getAttributeMap(true));
			list.add(((OperationConfig)element).getAttributeMap(false));
		}
		
		return list.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();

		if (element instanceof AttributeMapConfig) {
			AttributeMapConfig amc = (AttributeMapConfig)element;
			List<String> attrNames = amc.getAttributeNames();
			Collections.sort(attrNames);
			for(String name:attrNames) {
				list.add(amc.getAttributeMapItem(name));
			}
			if(list.size() == 0) {
				list.add((BaseConfiguration) createPlaceHolder((AttributeMapConfig) element));
			}
		} else if (element instanceof AttributeMapItem) {
			list.addAll(((AttributeMapItem)element).getChildAttributeMaps());			
		}

		return list.toArray();
	}
	
	public void getChildren(AssemblyLineConfig alc, ArrayList<BaseConfiguration> list) {
		ContainerConfig ops = alc.getOperations();
		for (int i = 0; i < ops.size(); i++) {
			addOpsMap((OperationConfig)ops.getConfig(i), true, list);
		}
		ContainerConfig ef = alc.getEntryFeedComponents();
		for (int i = 0; i < ef.size(); i++) {
			ConnectorConfig cc = (ConnectorConfig)ef.getConfig(i);
			list.add(cc.getAttributeMap(true));
		}
		
		getChildren(alc.getDataFlowComponents(), list);
				
		for (int i = 0; i < ef.size(); i++) {
			ConnectorConfig cc = (ConnectorConfig)ef.getConfig(i);
			if (ConnectorConfig.SERVER_MODE.equals(cc.getMode()))
			list.add(cc.getAttributeMap(false));
		}
		for (int i = 0; i < ops.size(); i++) {
			addOpsMap((OperationConfig)ops.getConfig(i), false, list);
		}
	}
	
	public void getChildren(ConnectorConfig cc, ArrayList<BaseConfiguration> list) {
		if (ConnectorConfig.SERVER_MODE.equals(cc.getMode())) {
			list.add(cc.getAttributeMap(true));
			list.add(cc.getAttributeMap(false));			
		} else {
			if (Utils.isOutputConnector(cc))
				list.add(cc.getAttributeMap(false));
			if (Utils.isInputConnector(cc))
				list.add(cc.getAttributeMap(true));
		}
	}
	
	public void getChildren(ContainerConfig cc, ArrayList<BaseConfiguration> list) {
		for (int i = 0; i < cc.size(); i++) {
			BaseConfiguration bc = cc.getConfig(i);
			if (bc instanceof LoopConfig) {
				LoopConfig lc = (LoopConfig)bc;
				if (lc.getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
					try {
						list.add(lc.getLoopConnector().getAttributeMap(true));
					} catch (Exception e) {
						EclipseAppender.logerror(e.getMessage(), e);
					}
				}
			} else if (bc instanceof ALMappingConfig) {
				list.add(((ALMappingConfig)bc).getAttributeMap());
			} else if (bc instanceof ConnectorConfig) {
				getChildren((ConnectorConfig)bc, list);
			}
			if (bc instanceof ContainerConfig) {
				getChildren((ContainerConfig)bc, list);
			}
		}
	}
	
	private void addOpsMap(OperationConfig op, boolean input, List<BaseConfiguration> list){
		AttributeMapConfig amc = op.getAttributeMap(input);
		if (amc.getAttributeNames().size() > 0)
			list.add(amc);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		BaseConfiguration b = ((BaseConfiguration) element).getParent();
		if (b instanceof AttributeMapConfig || b instanceof AttributeMapItem)
			return b;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof AttributeMapConfig)
			return true;
		if (element instanceof AttributeMapItem)
			return ((AttributeMapItem)element).getChildAttributeMaps().size() > 0;
		return false;
	}

	public void configurationChanged(MetamergeConfigChange changeEvent) {
		int op = changeEvent.getOperation();
		if (op == MetamergeConfigChange.BEGIN_CHANGES) {
			batchChange = true;
		} else if (op == MetamergeConfigChange.END_CHANGES) {
			batchChange = false;
		}
		
		if (batchChange || !(changeEvent.getSource() instanceof BaseConfiguration))
			return;

		BaseConfiguration source = (BaseConfiguration) changeEvent.getSource();
		if ( source instanceof AttributeMapItem
				|| source instanceof AttributeMapConfig 
				|| source instanceof ContainerConfig )
				new RefreshThread(op == MetamergeConfigChange.END_CHANGES);
		else if (source instanceof ConnectorConfig && 
				InternalSchema.CONNECTOR_MODE.equals(changeEvent.getKey())) {
			// Ignore events while the user is creating a new Connector
			if (source.getParent() instanceof ContainerConfig) {
				ContainerConfig container = (ContainerConfig) source.getParent();
				if (container.indexOf(source) == -1)
					return;
			}

			new RefreshThread(true);
		}
	}

	/**
	 * Class to update the viewer in a UI thread. A thread is created to run
	 * this class in the constructor.
	 */
	private class RefreshThread implements Runnable {
		private boolean reset;
		public RefreshThread(boolean reset) {
			super();
			this.reset = reset;
			viewer.getControl().getDisplay().asyncExec(this);
		}

		public void run() {
			batchChange = true;
			if (reset) {
				viewer.setInput(input);
				if (viewer instanceof TreeViewer)
					((TreeViewer)viewer).expandAll();
			} else {
				viewer.refresh();
			}
			batchChange = false;
		}
	}
}
