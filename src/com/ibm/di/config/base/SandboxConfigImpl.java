/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.*;

/**
 * Implements {@link SanboxConfig}
 *
 */
public class SandboxConfigImpl extends BaseConfigurationImpl implements
		SandboxConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -399320124155373314L;

	public SandboxConfigImpl() {
		super();
	}

	public SandboxConfigImpl(Object data) {
		super(data);
	}

	/**
	 * Returns the sandbox identifier
	 */
	public String getIdentifier() {
		return getStringParameter(InternalSchema.SANDBOX_IDENTIFIER);
	}

	/**
	 * Sets the sandbox identifier
	 */
	public void setIdentifier(String identifier) {
		setStringParameter(InternalSchema.SANDBOX_IDENTIFIER, identifier);
	}

	/**
	 * Returns the Record enabled flag
	 */
	public boolean getRecordEnabled() {
		return getBooleanParameter(InternalSchema.SANDBOX_RECORD, false);
	}

	/**
	 * Sets the Record enabled flag
	 */
	public void setRecordEnabled(boolean enabled) {
		setBooleanParameter(InternalSchema.SANDBOX_RECORD, enabled);
	}

	/**
	 * Returns the Playback enabled flag
	 */
	public boolean getPlaybackEnabled() {
		return getBooleanParameter(InternalSchema.SANDBOX_PLAYBACK, false);
	}

	/**
	 * Sets the Record enabled flag
	 */
	public void setPlaybackEnabled(boolean enabled) {
		setBooleanParameter(InternalSchema.SANDBOX_PLAYBACK, enabled);
	}

}
