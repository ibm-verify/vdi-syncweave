/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import org.eclipse.ui.INewWizard;

import com.ibm.di.config.base.ScriptConfigImpl;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.server.RestServerAPI;
import com.ibm.tdi.eclipse.server.ServerUtils;

public class NewServerWizard extends NewComponentBaseWizard implements INewWizard {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ScriptConfig infoRecord;

	public NewServerWizard() {
		super("tdiserver", "", "VersionTable.Server.Label"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setShowTypes(false);
	}

	@Override
	public void createConfigObject() {
		infoRecord = new ScriptConfigImpl();
		try {
			infoRecord.init();
			infoRecord.setStringParameter(RestServerAPI.TDI_ADDRESS, ServerUtils.getGlobalPropAddress());
			infoRecord.setStringParameter(RestServerAPI.TDI_SSL, "true");
			infoRecord.setStringParameter(RestServerAPI.TDI_TYPE, RestServerAPI.TYPE_RMI);
			infoRecord.setParameter(RestServerAPI.TDI_INSTALL, Activator.getInstallPath());
		} catch (Exception e) {}
		setConfigObject(infoRecord);
	}


}
