/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.util;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.regex.Pattern;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.AssemblyLine;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.syslog.LogUtils;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;

public class TDIUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final String AL_FOLDER_SLASHED = MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/";

	public static String configToString(MetamergeConfig mc) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mc.commitChanges(bos);
		return new String(bos.toByteArray(), "UTF-8");
	}

	public static boolean isAssemblyLineActive(ConfigInstance ci, String name) throws RemoteException, DIException {
		AssemblyLine al = getAssemblyLineByName(ci, name);
		return al != null && al.isActive();
	}

	public static AssemblyLine getAssemblyLineByName(ConfigInstance ci, String name) throws RemoteException, DIException {
		// al.getName() returns the AL name inside the ALs folder.
		name = AL_FOLDER_SLASHED + name;

		if (ci != null && ci.getAssemblyLines() != null) {
			for (AssemblyLine al : ci.getAssemblyLines()) {
				if (name.equals(al.getName())) {
					return al;
				}
			}
		}
		return null;
	}

	/**
	 * we know that the server api don't tolerate colons and any kind of
	 * slashes, so we need to make sure we don't send a runName containing those
	 * chars.
	 */
	public static String escapeRunName(String rawRunName) {
		return LogUtils.getCleanConfigId(rawRunName);
	}

	public static String getTPInstanceRunNameFor(String typeId, String instId) {
		return escapeRunName(typeId + "_" + instId);
	}

	public static MetamergeConfig cloneMetamergeConfig(MetamergeConfig mc) throws Exception {
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put(MetamergeConfigFactory.MC_DEBUG, "false");
		ht.put(MetamergeConfigFactory.MC_NO_DEFAULT_FOLDERS, "");
		ht.put(MetamergeConfigFactory.MC_ENCRYPT, "false");

		Class<? extends MetamergeConfig> mcClass = mc.getClass();
		Constructor<? extends MetamergeConfig> mcCon = mcClass.getConstructor(new Class<?>[] { Hashtable.class });
		MetamergeConfig res = mcCon.newInstance(new Object[] { ht });

		MetamergeConfigFactory.copy(mc, res, null, false);
		return res;
	}
}
