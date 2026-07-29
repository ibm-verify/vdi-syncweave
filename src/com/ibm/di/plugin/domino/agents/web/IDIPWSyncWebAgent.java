/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.domino.agents.web;

import lotus.domino.AgentBase;

/**
 * This code is placed directly in the Domino database. It is kept as a separate
 * java class for the purpose of easier additional developing.
 */
public class IDIPWSyncWebAgent extends AgentBase {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	/**
	 * {@inheritDoc}
	 */
	public void NotesMain() {
		new com.ibm.di.plugin.domino.agents.web.WebWorker().work();
	}
}
