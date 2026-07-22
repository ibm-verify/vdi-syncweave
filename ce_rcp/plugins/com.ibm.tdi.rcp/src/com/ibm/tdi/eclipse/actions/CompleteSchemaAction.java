/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;

public class CompleteSchemaAction extends TDIAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Override
	public void run() {
		SchemaConfig sc = (SchemaConfig) getEditingConfiguration();
		for(BaseConfiguration b : getTargetConfigurationObjects()) {
			if(b instanceof SchemaItemConfig && "Unknown".equals(((SchemaItemConfig)b).getPresenceFlag())) {
				((SchemaItemConfig)b).setPresenceFlag(SchemaItemConfig.PRESENCE_OPTIONAL);
				sc.setItem(b.getShortName(), (SchemaItemConfig) b);
			}
		}
	}

	@Override
	public boolean isEnabled() {
		boolean enabled = false;
		if(!(getEditingConfiguration() instanceof SchemaConfig))
			return enabled;
		
		for(BaseConfiguration b : getTargetConfigurationObjects()) {
			if(b instanceof SchemaItemConfig && "Unknown".equals(((SchemaItemConfig)b).getPresenceFlag()))
				enabled = true;
		}
		return enabled;
	}

}
