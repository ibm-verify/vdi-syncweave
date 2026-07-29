/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.bind;

import javax.xml.datatype.DatatypeConfigurationException;

public class BindUtil {
	
	public static ConfigTemplates fromConfigTemplates(ConfigTemplate[] tslist) throws DatatypeConfigurationException {
		ConfigTemplates stones = new ConfigTemplates();
		if(tslist == null)
			return stones;
		
		for(ConfigTemplate ts : tslist) {
			com.ibm.di.ui.webui.bind.ConfigTemplate t = new com.ibm.di.ui.webui.bind.ConfigTemplate();
			t.setName(ts.getName());
			stones.getConfigTemplate().add(ts);
		}
		return stones;
	}
}
