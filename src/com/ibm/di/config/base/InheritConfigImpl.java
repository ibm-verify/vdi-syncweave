/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.*;
import com.ibm.di.server.ResourceHash;
/**
 * This class is not used for anything.
 * @deprecated
 *
 */
public class InheritConfigImpl extends BaseConfigurationImpl implements
		InheritConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 9015532163983199487L;

	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	public InheritConfigImpl() {
		super();
	}

	public InheritConfigImpl(Object config) {
		super(config);
	}

	public BaseConfiguration getDefaultInherit() throws Exception {
		return getInheritFor(InheritConfig.DEFAULT_INHERIT);
	}

	public BaseConfiguration getInheritFor(Object name) throws Exception {
		Object obj = getParameter(name);
		if (obj == null)
			return null;

		MetamergeConfig mc = getMetamergeConfig();
		if (mc == null && getParent() != null)
			mc = getParent().getMetamergeConfig();

		if (mc == null) {
			throw new Exception(
					sResHash
							.getString(
									"MMCONFIG.INHERITCONFIMPL.NO.METAMERGE.CONFIG.IN.THIS.OR.PARENTS.OBJECT",
									obj));
		} else
			return (BaseConfiguration) mc.lookup(obj);
	}

	public void setInheritFor(Object forName, BaseConfiguration inheritFrom) {
		setParameter(forName, inheritFrom.getName());
	}

	public void setInheritFor(Object forName, String inheritFrom) {
		setParameter(forName, inheritFrom);
	}
}
