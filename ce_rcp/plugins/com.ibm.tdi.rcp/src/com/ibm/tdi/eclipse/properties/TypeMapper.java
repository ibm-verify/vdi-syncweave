/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.properties;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.views.properties.tabbed.AbstractTypeMapper;

public class TypeMapper extends AbstractTypeMapper {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public Class mapType(Object object) {
		if(object instanceof TreeItem)
			return ((TreeItem)object).getData().getClass();
		else
			return object.getClass();
	}

}
