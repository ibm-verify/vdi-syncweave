/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.util;

import java.util.Hashtable;

import com.ibm.di.web.common.atom.AtomText;
import com.ibm.di.config.bind.BindUtil;
import com.ibm.di.config.bind.ContainerBinding;
import com.ibm.di.config.bind.NamedBinding;
import com.ibm.di.config.bind.PropertyStoresBinding;
import com.ibm.di.config.bind.SolutionBinding;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class ConfigConvertor {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static NamedBinding fromConfig(BaseConfiguration cfg, String folder, String folderRelName, String baseUri)
			throws Exception {
		MetamergeConfig mc = getNewMC();
		mc.bind(folder + "/" + folderRelName, cfg);

		SolutionBinding sb = com.ibm.di.config.bind.BindUtil.fromMetamergeConfig(mc);
		ContainerBinding cont = null;
		for (ContainerBinding c : sb.getContainers()) {
			if (folder.equals(c.getName())) {
				cont = c;
				break;
			}
		}

		NamedBinding binding = null;
		if (cont != null){
			binding = cont.getConfigs().get(0);
			InheritFromRewriter.rewrite(binding, baseUri + "internal/");
		}
		return binding;
	}

	public static SolutionBinding fromConfig(MetamergeConfig mc, String baseUri) throws Exception {
		SolutionBinding sol = BindUtil.fromMetamergeConfig(mc);
		InheritFromRewriter.rewrite(sol, baseUri + "internal/");
		return sol;
	}

	public static MetamergeConfig toConfig(SolutionBinding cfg) throws Exception {
		InheritFromRewriter.rewrite(cfg, null);
		return BindUtil.toMetamergeConfig(cfg);
	}

	public static BaseConfiguration toConfig(NamedBinding cfg, String folder) throws Exception {
		SolutionBinding sb = new SolutionBinding();
		PropertyStoresBinding ps = new PropertyStoresBinding();
		ps.setName(folder);
		ps.getConfigs().add(cfg);
		sb.getContainers().add(ps);

		MetamergeConfig mc = toConfig(sb);
		return (BaseConfiguration) mc.lookup(folder + "/" + cfg.getName());
	}

	@SuppressWarnings("unchecked")
	private static MetamergeConfig getNewMC() throws Exception {
		Hashtable env = new Hashtable();
		env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
		env.put(MetamergeConfigFactory.MC_CREATE, false);
		env.put(MetamergeConfigFactory.MC_URL, "");
		return MetamergeConfigFactory.getInstance(env);
	}
}
