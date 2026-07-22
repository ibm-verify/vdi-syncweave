/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.RawConnectorConfig;

public class FormUtils {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static FormConfig global;

	static {
		try {
			global = (FormConfig) MetamergeConfigFactory.lookup(null, MetamergeConfigFactory.STDFORMS_NAMESPACE
					+ ":/Forms/__GLOBAL__");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static FormConfig getGlobalForm() {
		return global;
	}

	public static FormConfig getForm(String formName, BaseConfiguration config) throws Exception {
		String f = null;

		if (formName != null) {
			f = formName;
		} else if (config instanceof FunctionConfig) {
			f = ((FunctionConfig) config).getJavaClass();
		} else if (config instanceof RawConnectorConfig) {
			f = ((RawConnectorConfig) config).getJavaClass();
		}
		if ( f == null )
			return null;

		f = (f.indexOf(":") == -1 ? "system:/Forms/" + f : f);
		if (config != null)
			return (FormConfig) config.getMetamergeConfig().lookup(f);
		else
			return (FormConfig) MetamergeConfigFactory.getNamespace("system").lookup(f);
	}

}
