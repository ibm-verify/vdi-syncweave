package com.ibm.di.test.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

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
 * @since 7.1
 */
public class ConfigUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final MetamergeConfig deserializeConfig(File xmlConfig) throws Exception {
		Hashtable<String, Object> env = new Hashtable<String, Object>();

		if (xmlConfig != null) {
			// a temporary instance without an associated file
			env.put(javax.naming.Context.PROVIDER_URL, xmlConfig);
			env.put(MetamergeConfigFactory.MC_CREATE, "false");
			env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
			env.put(MetamergeConfigFactory.MC_ENCRYPT, "false");
		}
		return MetamergeConfigFactory.getInstance(env);
	}

	public static final MetamergeConfig deserializeConfig(String xmlConfig) throws Exception {
		Hashtable<String, Object> env = new Hashtable<String, Object>();

		if (xmlConfig != null) {
			// a temporary instance without an associated file
			env.put(javax.naming.Context.PROVIDER_URL, xmlConfig.getBytes("UTF-8"));
			env.put(MetamergeConfigFactory.MC_CREATE, "false");
			env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
			env.put(MetamergeConfigFactory.MC_ENCRYPT, "false");
		}
		return MetamergeConfigFactory.getInstance(env);
	}

	public static final String serializeConfig(MetamergeConfig mc) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mc.commitChanges(bos);
		return new String(bos.toByteArray(), "UTF-8");
	}
}
