/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * This interface is not used.
 * @deprecated This interface is not used anywhere.
 *
 */
public interface InheritConfig extends BaseConfiguration {

	public final static String DEFAULT_INHERIT = "$default_inherit";

	public BaseConfiguration getDefaultInherit() throws Exception;

	public BaseConfiguration getInheritFor(Object name) throws Exception;

	public void setInheritFor(Object forName, BaseConfiguration inheritFrom);

	public void setInheritFor(Object forName, String inheritFrom);

}
