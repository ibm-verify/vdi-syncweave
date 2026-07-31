/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.navigator;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.navigator.CommonNavigator;

public class CustomNavigator extends CommonNavigator {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Override
	protected IAdaptable getInitialInput() {
		// TODO: Work around to show contents when no open editors exists
		IAdaptable ret = null;
		if (getSite() != null && getSite().getPage() != null)
			ret = getSite().getPage().getInput();
		
		return ret != null ? ret : ResourcesPlugin.getWorkspace().getRoot();
	}

}
