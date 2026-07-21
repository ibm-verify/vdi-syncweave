/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.decorators;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.navigator.LabelProvider;

public class PropertyStoreDecorator implements ILabelDecorator {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ArrayList<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();
	
	// -- eclipse 3.5.1 no longer calls label providers that don't provide content so we do our display mods here instead
	private LabelProvider lp = new LabelProvider();

	public Image decorateImage(Image image, Object element) {
		return null;
	}

	public String decorateText(String text, Object element) {
		StringBuffer buf = new StringBuffer();
		if (isStdStore(element)) {
			buf.append(Messages.getString("general.properties.defstore"));
		}
		if (isPwdStore(element)) {
			if (buf.length() > 0)
				buf.append(", ");

			buf.append(Messages.getString("general.properties.pwdstore"));
		}

		String txt = null;
		if(element instanceof IFile) {
			String str = lp.getText(element);
			if(str != null && !str.equals(text))
				txt = str;
		}

		return buf.length() > 0 ? text + " (" + buf.toString() + ")" : txt;
	}

	public void addListener(ILabelProviderListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	public boolean isStdStore(Object element) {
		if (element instanceof IFile) {
			try {
				IFile file = (IFile) element;
				if (!file.getProject().hasNature(TDINature.TDI_NATURE_ID))
					return false;

				if (TDIConfigurationFile.XT_PROPSTORE.equals(file.getFileExtension())) {
					MetamergeConfig mc = Utils.getProjectMC(file.getProject());
					if (mc == null)
						return false;

					PropertyManager pm = (PropertyManager) mc.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
					if (pm.getDefaultPropertyStore() == null)
						return false;

					String name = file.getName();
					String store = name.substring(0, name.length() - (file.getFileExtension().length() + 1));
					return store.equals(pm.getDefaultPropertyStore().getShortName());
				}
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	public boolean isPwdStore(Object element) {
		if (element instanceof IFile) {
			try {
				IFile file = (IFile) element;
				if (!file.getProject().hasNature(TDINature.TDI_NATURE_ID))
					return false;

				if (TDIConfigurationFile.XT_PROPSTORE.equals(file.getFileExtension())) {
					MetamergeConfig mc = Utils.getProjectMC(file.getProject());
					if (mc == null)
						return false;

					PropertyManager pm = (PropertyManager) mc.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
					if (pm.getPasswordPropertyStore() == null)
						return false;

					String name = file.getName();
					String store = name.substring(0, name.length() - (file.getFileExtension().length() + 1));
					return store.equals(pm.getPasswordPropertyStore().getShortName());
				}
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}
}
