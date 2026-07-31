/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.awt.event.ActionEvent;
/**
 * This was used by the very old Configuration Editor to signal that a new
 * namespace had been added.
 * @deprecated This was used by the old Configuration Editor.
 *
 */
public class NamespaceEvent extends ActionEvent {

	static final long serialVersionUID = -1857414661726671152L;

	private boolean added;

	private String ns;

	private MetamergeConfig mc;

	public NamespaceEvent(boolean added, String ns, MetamergeConfig mc) {
		super(MetamergeConfigFactory.class, ActionEvent.ACTION_PERFORMED,
				"namespaceEvent");
		this.added = added;
		this.ns = ns;
		this.mc = mc;
	}

	public boolean isAdded() {
		return added;
	}

	public MetamergeConfig getMc() {
		return mc;
	}

	public String getNs() {
		return ns;
	}

	public String toString() {
		return "[namespace=" + ns + ", added=" + added + ", url=" + mc + "]";
	}
}
