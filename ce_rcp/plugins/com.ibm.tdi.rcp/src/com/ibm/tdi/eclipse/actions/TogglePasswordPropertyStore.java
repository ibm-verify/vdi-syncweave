/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.PropertyManager;

public class TogglePasswordPropertyStore extends ToggleDefaultPropertyStore {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public TogglePasswordPropertyStore() {
	}

	@Override
	protected void updateStoreSetting(MetamergeConfig mc, String store, boolean checked) throws Exception {
		PropertyManager pm = (PropertyManager) mc.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
		if(checked)
			pm.setDefaultPasswordStore(pm.getPropertyStore(store));
		else
			pm.setDefaultPasswordStore(null);
		mc.commitChanges(null);
	}

	@Override
	protected boolean getStoreState(MetamergeConfig mc, String store) throws Exception {
		PropertyManager pm = (PropertyManager) mc.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
		if(pm.getPasswordPropertyStore() != null)
			return store.equals(pm.getPasswordPropertyStore().getShortName());
		else
			return false;
	}

}
