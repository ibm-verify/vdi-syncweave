/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.net.URI;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IURIEditorInput;

public class URIEditorInput implements IURIEditorInput {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private URI uri;

	public URIEditorInput(URI uri) {
		super();
		this.uri = uri;
	}

	public URI getURI() {
		return uri;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return getToolTipText();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return uri.getHost() + ":" + uri.getPort(); //$NON-NLS-1$
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

}
