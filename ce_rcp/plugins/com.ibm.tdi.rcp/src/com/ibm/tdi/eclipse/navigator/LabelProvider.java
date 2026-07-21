/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.navigator;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.natures.TDINature;

/**
 * Note: This is not yet implemented but is planned for
 * 
 * This class is used by the TDI Navigator (CommonNavigator) to get the image and label for
 * the children returned by the {@link ContentProvider}.
 *
 */
public class LabelProvider implements ILabelProvider {
	
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String[] internalNames = {
		TDINature.RESOURCES_FOLDER,
		TDINature.ASSEMBLYLINES_FOLDER,
		TDINature.PROPERTIES_FOLDER,
		TDINature.SCRIPTS_FOLDER,
		TDINature.ATTRIBUTE_MAPS_FOLDER,
		TDINature.FUNCTIONS_FOLDER,
		TDINature.PARSERS_FOLDER,
		TDINature.CONNECTORS_FOLDER,
		TDINature.NAMESPACE_FOLDER,
		TDINature.SOLUTION_SETTINGS_FILE,
		"Reports",
		TDINature.SCHEMA_FOLDER,
		TDINature.SCHEDULER_FOLDER,
		TDINature.SEQUENCE_FOLDER,
	};
	
	private static final String[] externalNames = {
		Messages.getString("miadmin.foldernames.Resources"),
		Messages.getString("miadmin.foldernames.AssemblyLines"),
		Messages.getString("miadmin.foldernames.Properties"),
		Messages.getString("miadmin.foldernames.Scripts"),
		Messages.getString("miadmin.foldernames.AttributeMaps"),
		Messages.getString("miadmin.foldernames.Functions"),
		Messages.getString("miadmin.foldernames.Parsers"),
		Messages.getString("miadmin.foldernames.Connectors"),
		Messages.getString("miadmin.foldernames.References"),
		Messages.getString("miadmin.filename.SolutionSettings"),
		Messages.getString("miadmin.foldernames.Reports"),
		Messages.getString("miadmin.foldernames.Schema"),
		Messages.getString("miadmin.foldernames.Schedules"),
		Messages.getString("miadmin.foldernames.Sequences"),
	};
	
	/**
	 * Listeners for label changes
	 */
	private ArrayList<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if(element instanceof IResource) {
			// -- Hide file extension for known config object files
			IResource res = (IResource) element;
			String name = res.getName();
			for(String knownExtension : TDIConfigurationFile.FILE_EXTENSIONS) {
				if(knownExtension.equals(res.getFileExtension())) {
					return name.substring(0, name.lastIndexOf("."));
				}
			}
			// -- schema files are special
			if(TDINature.SCHEMA_FILEEXT.equals(res.getFileExtension()))
				return name.substring(0, name.lastIndexOf("."));
			
			for (int i =0 ; i < internalNames.length; i++)
				if (internalNames[i].equals(name))
					return externalNames[i];
			return name;
		} else {
			return element.toString();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		if(!listeners.contains(listener))
			listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

}
