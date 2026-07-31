/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;

public class TDIConfigEditorInput implements IEditorInput {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private BaseConfiguration config;
	private ImageDescriptor image;
	private String editor;

	public TDIConfigEditorInput(BaseConfiguration config) {
		this.config = config;
	}

	public TDIConfigEditorInput(BaseConfiguration config, String editor) {
		this.config = config;
		this.editor = editor;
	}

	public boolean exists() {
		return (config != null);
	}

	public ImageDescriptor getImageDescriptor() {
		return image;
	}

	public String getName() {
		return config.getShortName();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		StringBuilder ret = new StringBuilder();
		if (config.getMetamergeConfig() instanceof TDIConfigurationFile) {
			TDIConfigurationFile cf = (TDIConfigurationFile)config.getMetamergeConfig();
			if (cf.getProject() != null) {
				ret.append(cf.getProject().getName());
				ret.append(":");
			}
			String s = cf.getFile().getName();
			if (s.indexOf('.') > 0)
				s = s.substring(0, s.lastIndexOf('.'));
			ret.append(s);
			ret.append('#');
		}
		ret.append(getName());
		return ret.toString();
	}

	public String getTitle() {
		StringBuilder ret = new StringBuilder();
		if (config.getMetamergeConfig() instanceof TDIConfigurationFile) {
			String s = ((TDIConfigurationFile)config.getMetamergeConfig()).getFile().getName();
			if (s.indexOf('.') > 0)
				s = s.substring(0, s.lastIndexOf('.'));
			ret.append(s);
			ret.append('#');
		}
		ret.append(getName());
		return ret.toString();
	}
	
	public Object getAdapter(Class adapter) {
		return null;
	}

	public BaseConfiguration getConfiguration() {
		return config;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TDIConfigEditorInput) {
			TDIConfigEditorInput other = (TDIConfigEditorInput) obj;
			if(editor != null) {
				if(!editor.equals(other.getEditor()))
					return false;
			} else if (other.getEditor() != null) {
				return false;
			}
			return ((TDIConfigEditorInput)obj).config == config;
		}

		return false;
	}

	@Override
	public int hashCode() {
		if (config == null)
			return 42;
		return config.hashCode();
	}
	
	public void setImage(ImageDescriptor image) {
		this.image = image;
	}

	public String getEditor() {
		return editor;
	}

}
