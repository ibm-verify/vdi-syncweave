/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.icu.util.StringTokenizer;

/**
 * Convenience class to get a list of all scripts used by an AssemblyLineConfig.
 * @author Administrator
 *
 */
public class AssemblyLineScripts {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String SCRIPTS = "/" + MetamergeConfig.DEFAULT_SCRIPT_FOLDER + "/";

	private Map<String, ScriptConfig> nameToConfig = new HashMap<String, ScriptConfig>();
	private List<String> names = new ArrayList<String>();
	private List<String> notFound = new ArrayList<String>();
	private Set<ScriptConfig> alreadyIncluded = new HashSet<ScriptConfig>();

	public AssemblyLineScripts(AssemblyLineConfig alc) {

		MetamergeConfig mc = alc.getMetamergeConfig();
		if (mc == null)
			return;

		String includeScripts = alc.getSettings().getStringParameter("includePrologs");
		if (includeScripts != null) {
			StringTokenizer st = new StringTokenizer(includeScripts, "\r\n");
			while(st.hasMoreTokens()) {
				String key = st.nextToken();
				boolean exclude = false;
				if (key.startsWith("-")) {
					exclude = true;
					key = key.substring(1);
				}
				key = key.trim();
				if (key.length() == 0)
					continue;
				if (!key.contains(SCRIPTS)) {
					if (key.contains(":"))
						key = key.replace(":", ":" + SCRIPTS);
					else
						key = SCRIPTS + key;
				}
				try {
					ScriptConfig sc = (ScriptConfig) mc.lookup(key);
					if (!exclude && !alreadyIncluded.contains(sc)) {
						nameToConfig.put(key, sc);
						names.add(key);
					}
					alreadyIncluded.add(sc);
				} catch (Exception e) {
					if (!exclude)
						notFound.add(key);
				}
			}
		}

		if (alc.getSettings().getBooleanParameter("includeGlobalPrologs", true)) {
			try {
				addScripts(mc, null);
				MetamergeFolder ns = mc.getDefaultFolder(MetamergeConfig.NAMESPACE_FOLDER);
				for (String str : ns.getNames()) {
					try {
						addScripts(MetamergeConfigFactory.loadNamespace(mc.getNamespace(str)), str);
					}  catch (Exception e) {
						SystemFunctions.doNothing();
					}
				}
			}  catch (Exception e) {
				SystemFunctions.doNothing();
			}
		}
	}

	private void addScripts(MetamergeConfig mc, String nameSpace) throws Exception {
		String prefix = SCRIPTS;
		if (nameSpace != null)
			prefix = nameSpace + ":" + prefix;

		MetamergeFolder folder = mc.getDefaultFolder(MetamergeConfig.SCRIPT_FOLDER);

		for (String str : folder.getNames()) {
			ScriptConfig sc = mc.getScript(str);
			if (sc == null || alreadyIncluded.contains(sc))
				continue;

			if (sc.getAutoInclude()) {
				String key = prefix + str;
				nameToConfig.put(key, sc);
				names.add(key);
				alreadyIncluded.add(sc);
			}
		}
	}

	/**
	 * Returns the ScriptConfig with the given name.
	 * @param key Name of script
	 * @return the ScriptConfig with the given name.
	 */
	public ScriptConfig getScript(String key) {
		return nameToConfig.get(key);
	}

	/**
	 * Returns the name of the ScriptConfig, as seen by the AssemblyLineConfig.
	 * @param config
	 * @return the name of the ScriptConfig, as seen by the AssemblyLineConfig.
	 */
	public String getName(ScriptConfig config) {
		for (Map.Entry<String, ScriptConfig> e: nameToConfig.entrySet()) {
			if (e.getValue() == config)
				return e.getKey();
		}
		return null;
	}

	/**
	 * Returns a List of all script names.
	 * @return a List of all script names.
	 */
	public List<String> getAllNames() {
		return new ArrayList<String>(names);
	}

	/**
	 * Returns true if there are no scripts.
	 * @return true if there are no scripts.
	 */
	public boolean isEmpty() {
		return names.isEmpty();
	}

	/**
	 * Returns a List of the ScriptConfig names that were not found
	 * @return List of names that were not found.
	 */
	public List<String> getNotFound() {
		return new ArrayList<String>(notFound);
	}
}