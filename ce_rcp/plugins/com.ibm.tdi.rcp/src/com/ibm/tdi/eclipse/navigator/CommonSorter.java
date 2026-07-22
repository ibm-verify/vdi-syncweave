/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.navigator;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;

import org.eclipse.jface.viewers.ViewerSorter;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.HookConfig;

public class CommonSorter extends ViewerSorter implements Comparator {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static ArrayList<String> hook_order = new ArrayList<String>();
	static {
		hook_order.add(InternalSchema.AL_PROLOG_INIT);
		hook_order.add(InternalSchema.AL_PROLOG);
		hook_order.add(InternalSchema.AL_STARTCYCLE);
		hook_order.add(InternalSchema.AL_EPILOG);
		hook_order.add(InternalSchema.AL_EPILOG2);
		hook_order.add(InternalSchema.AL_ONSUCCESS);
		hook_order.add(InternalSchema.AL_ONFAILURE);
		hook_order.add(InternalSchema.AL_SHUTDOWN);
	};

	public CommonSorter() {
	}

	public CommonSorter(Collator collator) {
		super(collator);
	}

	@Override
	protected Comparator getComparator() {
		return this;
	}

	public int compare(Object o1, Object o2) {
		if(o1 instanceof HookConfig) {
			if(o2 instanceof HookConfig) {
				if(hook_order.indexOf(o1.toString()) < hook_order.indexOf(o2.toString()))
					return -1;
				else
					return 1;
			} else {
				return -1;
			}
		}
		if((o1 instanceof BaseConfiguration) && (o2 instanceof BaseConfiguration)
				&& (((BaseConfiguration)o1).getParent() instanceof ContainerConfig)) {
				ContainerConfig cc = (ContainerConfig) ((BaseConfiguration)o1).getParent();
			if(cc.indexOf((BaseConfiguration) o1) < cc.indexOf((BaseConfiguration) o2))
				return -1;
			else
				return 1;
		}
		return 0;
	}

	@Override
	public boolean isSorterProperty(Object element, String property) {
		System.out.println("isSorter: " + element + "; prop=" + property);
		return super.isSorterProperty(element, property);
	}

}
