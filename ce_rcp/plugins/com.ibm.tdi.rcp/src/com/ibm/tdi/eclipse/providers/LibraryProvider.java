/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.naming.Binding;
import javax.naming.NameNotFoundException;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeFolder;

public class LibraryProvider extends AbstractConfigProvider implements ITreeContentProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public Object[] getChildren(Object parentElement) {
		ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();
		if(parentElement instanceof MetamergeConfig) {
			MetamergeConfig mc = (MetamergeConfig) parentElement;
			String[] folders = new String[]{
					MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER, 
					MetamergeConfig.DEFAULT_CONNECTOR_FOLDER,
					MetamergeConfig.DEFAULT_FUNCTION_FOLDER,
					MetamergeConfig.DEFAULT_SCRIPT_FOLDER,
					MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER};
			try {
				for(String str : folders) {
					try {
						MetamergeFolder folder = (MetamergeFolder) mc.lookup(str);
						if(folder.size() > 0)
							list.add(folder);
					} catch (NameNotFoundException nfe) {
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(parentElement instanceof MetamergeFolder) {
			MetamergeFolder f = (MetamergeFolder) parentElement;
			try {
				for(Enumeration<Binding> e = f.list(); e.hasMoreElements(); ) {
					list.add((BaseConfiguration) e.nextElement().getObject());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(parentElement instanceof Object[]) {
			for(Object obj : ((Object[])parentElement)) {
				if(obj instanceof ConnectorConfig) {
					list.add((BaseConfiguration) obj);
				} else if(obj instanceof MetamergeConfig) {
					list.add((BaseConfiguration) obj);
				} else if(obj instanceof BranchingConfig) {
					list.add((BaseConfiguration) obj);
				} else if(obj instanceof ContainerConfig || obj instanceof MetamergeFolder) {
					for(Object b : getChildren(obj))
						list.add((BaseConfiguration) b);
				} else {
					list.add((BaseConfiguration) obj);
				}
			}
		}
		return list.toArray();
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public Object getParent(Object element) {
		if(element instanceof BaseConfiguration)
			return ((BaseConfiguration)element).getParent();
		else
			return null;
	}

	public boolean hasChildren(Object element) {
		return (element instanceof MetamergeFolder || element instanceof Object[]);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
