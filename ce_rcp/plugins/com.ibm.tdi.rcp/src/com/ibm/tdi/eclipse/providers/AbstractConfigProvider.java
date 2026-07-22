/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.tdi.eclipse.Utils;

public abstract class AbstractConfigProvider implements MetamergeConfigChangeListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	protected Viewer viewer;
	private BaseConfiguration config;
	private boolean feedFlowShown;
	protected boolean batchChange = false;
	
	public void configurationChanged(MetamergeConfigChange changeEvent) {
		int op = changeEvent.getOperation();
		if (op == MetamergeConfigChange.BEGIN_CHANGES) {
			batchChange = true;
			return;
		} else if (op == MetamergeConfigChange.END_CHANGES) {
			batchChange = false;
			if (viewer != null)
				viewer.refresh();
			return;
		} else if (batchChange) {
			return;
		}
		
		Object source = changeEvent.getSource();
		Object[] userdata = null;
		if(changeEvent.getUserObject() instanceof Object[])
			userdata = (Object[]) changeEvent.getUserObject();
		
		switch(op) {
		case MetamergeConfigChange.MCC_MODIFY:
		case MetamergeConfigChange.MCC_REPLACE:
			((TreeViewer)viewer).refresh(source, true);
			break;
			
		case MetamergeConfigChange.MCC_DELETE:
		case MetamergeConfigChange.MCC_REMOVE:
			if(userdata != null)
				((TreeViewer)viewer).remove(userdata[1]);
			else
				((TreeViewer)viewer).remove(source);
			break;
			
		case MetamergeConfigChange.MCC_ADD:
		case MetamergeConfigChange.MCC_SET:
			if(userdata != null) {
				BaseConfiguration target = (BaseConfiguration) userdata[1];
				AssemblyLineConfig alc = (AssemblyLineConfig) Utils.getParentConfig(target, AssemblyLineConfig.class);
				// TODO: Use insert() to position correctly
				if(!isFeedFlowShown()) {
					if(target.getParent() == alc.getDataFlowComponents() || target.getParent() == alc.getEntryFeedComponents())
						((TreeViewer)viewer).add(alc, target);
					else
						((TreeViewer)viewer).add(source, target);
				} else { 
					((TreeViewer)viewer).add(source, target);
				}
			} else if (viewer instanceof TreeViewer) {
				((TreeViewer)viewer).refresh(source, true);
			} else {
				viewer.refresh();
			}
			break;
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		if(oldInput != null)
			((BaseConfiguration)oldInput).removeListener(this);
		if(newInput instanceof BaseConfiguration)
			((BaseConfiguration)newInput).addListener(this);
		
		config = (BaseConfiguration) newInput;
	}

	public Viewer getViewer() {
		return viewer;
	}
	
	public void dispose() {
		if(config != null)
			config.removeListener(this);
	}
	
	public void setFeedFlowShown(boolean selection) {
		this.feedFlowShown = selection;
	}

	public boolean isFeedFlowShown() {
		return feedFlowShown;
	}

}
