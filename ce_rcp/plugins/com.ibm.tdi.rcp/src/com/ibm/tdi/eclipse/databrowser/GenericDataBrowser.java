/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.databrowser;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.providers.MetamergeFolderContentProvider;

public class GenericDataBrowser extends DataBrowser {

	@SuppressWarnings("unused") 
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public GenericDataBrowser(Composite parent, int style, BaseConfiguration editingConfig) {
		super(parent, style, editingConfig);
	}

	@Override
	protected void doInitialDiscovery() throws Exception {
	}

	@Override
	protected IContentProvider getNavigatorContentProvider() {
		return new MetamergeFolderContentProvider();
	}

	@Override
	protected Object getNavigatorInput() {
		return null;
	}

	@Override
	protected IBaseLabelProvider getNavigatorLabelProvider() {
		return null;
	}

	@Override
	protected void handleNavigatorSelectionChanged(SelectionChangedEvent event) {
	}

}
