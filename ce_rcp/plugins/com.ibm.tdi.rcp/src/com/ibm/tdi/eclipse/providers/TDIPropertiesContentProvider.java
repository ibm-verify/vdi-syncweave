/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.eclipse.TDIPropertiesCE;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.TDIPropertyStore;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.util.PropertiesFile;
import com.ibm.tdi.eclipse.editors.PropertiesEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class TDIPropertiesContentProvider implements ITreeContentProvider {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Object input;

	private HashMap<Object, ContainerConfig> map = new HashMap<Object, ContainerConfig>();
	
	public TDIPropertiesContentProvider() {
	}

	public Object[] getChildren(Object parentElement) {
		ArrayList<Object> list = new ArrayList<Object>();
		if(parentElement instanceof TDIPropertyStore) {
			// No children
			SystemFunctions.doNothing();
		} else if(parentElement instanceof BaseConfiguration) {
			try {
				list.addAll(TDIPropertiesCE.getPropertyStores((BaseConfiguration) parentElement));
			} catch (Throwable e1) {
				EclipseAppender.logerror(e1.getMessage(), e1);
				list.add(e1);
			}
			
			// -- get global/solution props
			try {
				list.addAll(TDIPropertiesCE.getServerPropertyStores((BaseConfiguration) parentElement));
			} catch (Exception e) {
				EclipseAppender.logerror(e.getMessage(), e);
			}
			
		} else if (parentElement instanceof File) {
			try {
				File file = (File) parentElement;
				PropertiesFile pfile = new PropertiesFile(CryptoUtils.getDefaultCrypto(), file.getAbsolutePath(), true);
				List<BaseConfiguration> props = new ArrayList<BaseConfiguration>();
				for(Iterator<String> i = pfile.keys(); i.hasNext(); ) {
					BaseConfiguration bc = new BaseConfigurationImpl();
					bc.init();
					bc.setName(i.next());
					bc.setParameter(PropertiesEditor.LOCAL_VALUE, pfile.getProperty(bc.getShortName()));
					bc.setParameter(PropertiesEditor.LOCAL_PROTECT, pfile.isPropertyProtected(bc.getShortName()));
					PropertiesEditor.verifyEncrypted(bc);
					// -- add this to avoid the (new) suffix
					bc.setParameter(PropertiesEditor.SERVER_NAME, bc.getShortName());
					bc.setParameter(PropertiesEditor.SERVER_VALUE, bc.getParameter(PropertiesEditor.LOCAL_VALUE));
					bc.setParameter(PropertiesEditor.PROPERTY_FILE_OBJECT, file);
					props.add(bc);
				}
				
				Collections.sort(props, new Comparator<BaseConfiguration>() {
					public int compare(BaseConfiguration o1,
							BaseConfiguration o2) {
						if (o1.getShortName() == null)
							return o2.getShortName() == null ? 0 : 1;
						if (o2.getShortName() == null)
							return -1;
						return o1.getShortName().compareTo(o2.getShortName());
					}
				});
				list.addAll(props);
				
			} catch (Exception e) {
				EclipseAppender.logerror(e.getMessage(), e);
			}

		} else if (parentElement instanceof IFile) {
			try {
				ContainerConfig data = map.get(parentElement);
				if(data == null) {
					TDIConfigurationFile cfg = TDIConfigurationFile.loadFile((IFile) parentElement);
					data = (ContainerConfig) ((ContainerConfig)cfg.getDefaultConfigObject()).getConfig("Data");
					map.put(parentElement, data);
				}
				
				List<BaseConfiguration> props = data.getConfigurations(null);
				Collections.sort(props, new Comparator<BaseConfiguration>() {
					public int compare(BaseConfiguration o1,
							BaseConfiguration o2) {
						if (o1.getShortName() == null)
							return o2.getShortName() == null ? 0 : 1;
						if (o2.getShortName() == null)
							return -1;
						return o1.getShortName().compareTo(o2.getShortName());
					}
				});
				list.addAll(props);
			} catch (Exception e) {
				list.add(e.toString());
			}
		}
		return list.toArray();
	}

	public Object getParent(Object element) {
		if(element instanceof IResource)
			return ((IResource)element).getParent();
		
		if(element instanceof BaseConfiguration) {
			BaseConfiguration b = (BaseConfiguration) element;
			TDIConfigurationFile file = (TDIConfigurationFile) b.getMetamergeConfig();
			if(file != null)
				return file.getFile();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		return(element instanceof IFile || element instanceof TDIPropertyStore || element instanceof File);
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		map = new HashMap<Object, ContainerConfig>();
		this.input = newInput;
	}

	public BaseConfiguration getChildForProperty(String property) {
		return findChildForProperty(property, input);
	}
	
	private BaseConfiguration findChildForProperty(String property, Object input) {
		if(property == null)
			return null;
		
		// -- <store>:<prop>
		if(property.indexOf(":") != -1) {
			String store = property.substring(0, property.indexOf(":")) + "." + TDIConfigurationFile.XT_PROPSTORE;
			String prop = property.substring(property.indexOf(":")+1);
			for(Object key : map.keySet()) {
				if(key instanceof IFile && store.equals(((IFile)key).getName()))
					return findChildForProperty(prop, key);
			}
		}
		
		BaseConfiguration b = null;
		for(Object obj : getChildren(input)) {
			if(obj instanceof BaseConfiguration &&
					property.equals(((BaseConfiguration)obj).getShortName()))
				b = (BaseConfiguration) obj;
			else if(hasChildren(obj))
				b = findChildForProperty(property, obj);
			if(b != null)
				break;
		}
		return b;
	}
	
}
