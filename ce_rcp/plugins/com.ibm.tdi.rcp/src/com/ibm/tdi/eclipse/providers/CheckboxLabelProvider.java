/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import com.ibm.tdi.eclipse.Activator;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.graphics.Image;

public abstract class CheckboxLabelProvider extends ColumnLabelProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private static final String CHECKED_KEY = "checked";
	private static final String UNCHECK_KEY = "unchecked";

	public CheckboxLabelProvider(ColumnViewer viewer) {
		if (JFaceResources.getImageRegistry().getDescriptor(CHECKED_KEY) == null) {
			JFaceResources.getImageRegistry().put(UNCHECK_KEY,
					Activator.getImage(UNCHECK_KEY));
			JFaceResources.getImageRegistry().put(CHECKED_KEY,
					Activator.getImage(CHECKED_KEY));
		}
	}

	public Image getImage(Object element) {
		if (isChecked(element)) {
			return JFaceResources.getImageRegistry().get(CHECKED_KEY);
		} else {
			return JFaceResources.getImageRegistry().get(UNCHECK_KEY);
		}
	}

	protected abstract boolean isChecked(Object element);
}

