/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.naming.Binding;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.LibraryConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.PropertyConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;

public class MetamergeFolderContentProvider extends AbstractConfigProvider implements ITreeContentProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String[] EXCLUDES = new String[] { 
		"EventHandlers",
		MetamergeConfig.DEFAULT_PROPERTY_FOLDER, 
	};

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Object[] getChildren(Object parentElement) {
		ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();
		if (parentElement instanceof MetamergeFolder) {
			MetamergeFolder f = (MetamergeFolder) parentElement;
			
			try {
				mainLoop: for (Enumeration<?> e = f.list(); e.hasMoreElements();) {
					Object en = e.nextElement();
					Object obj;
					if (en instanceof Binding)
						obj = ((Binding) en).getObject();
					else
						obj = en;

					if (obj instanceof MetamergeFolder && parentElement instanceof MetamergeConfig) {
						String name = ((MetamergeFolder) obj).getShortName();
						for (String str : EXCLUDES) {
							if (name.equals(str))
								continue mainLoop;
						}
						list.add((BaseConfiguration) obj);
					} else if (obj instanceof LibraryConfig && parentElement instanceof MetamergeConfig) {
						LibraryConfig lc = (LibraryConfig) obj;
						if(lc.size() > 0)
							list.add(lc);
					} else if (obj instanceof PropertyConfig && parentElement instanceof MetamergeConfig) {
						// Do nothing
					} else if (obj instanceof PropertyStoreConfig) {
						String name = ((PropertyStoreConfig) obj).getShortName();
						if (! (PropertyManager.STDCOLL_GLOBAL.equals(name)
								|| PropertyManager.STDCOLL_JAVA.equals(name)
								|| PropertyManager.STDCOLL_SOLUTION.equals(name)
								|| PropertyManager.STDCOLL_SYSTEM.equals(name)))
							list.add((BaseConfiguration) obj);

					} else if (obj instanceof BaseConfiguration) {
						list.add((BaseConfiguration) obj);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (parentElement instanceof Object[]) {
			for (Object obj : ((Object[]) parentElement)) {
				if (obj instanceof ConnectorConfig) {
					list.add((BaseConfiguration) obj);
				} else if (obj instanceof BranchingConfig) {
					list.add((BaseConfiguration) obj);
				} else if (obj instanceof ContainerConfig || obj instanceof MetamergeFolder) {
					for (Object b : getChildren(obj))
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
		if (element instanceof BaseConfiguration)
			return ((BaseConfiguration) element).getParent();
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
