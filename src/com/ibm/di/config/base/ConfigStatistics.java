/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.io.Serializable;

/**
 * Class providing statistics for config such as :<br>
 * <li>number of bind calls</li><br>
 * <li>number of rebind calls</li><br>
 * <li>number of unbind calls</li><br>
 * <li>number of lookup calls</li><br>
 * <li>number of modify calls</li><br>
 */
public class ConfigStatistics implements Serializable {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = -1271645457384911249L;

	/**
	 * Number of bind calls.
	 */
	private int bindCount;

	/**
	 * Number of rebind calls.
	 */
	private int rebindCount;

	/**
	 * Number of unbind calls.
	 */
	private int unbindCount;

	/**
	 * Number of lookup calls/
	 */
	private int lookupCount;

	/**
	 * Non-arg constructor.
	 */
	public ConfigStatistics() {
		reset();
	}

	/**
	 * This method increments number of rebind calls.
	 */
	public void rebind() {
		rebindCount++;
	}

	/**
	 * This method increments number of bind calls.
	 */
	public void bind() {
		bindCount++;
	}

	/**
	 * This method increments number of lookup calls.
	 */
	public void lookup() {
		lookupCount++;
	}

	/**
	 * This method increments number of unbind calls.
	 */
	public void unbind() {
		unbindCount++;
	}

	/**
	 * @return number of modify calls
	 */
	public int getModCount() {
		return (bindCount + rebindCount + unbindCount);
	}

	/**
	 * @return number of lookup calls
	 */
	public int getLookup() {
		return lookupCount;
	}

	/**
	 * Reset all counters.
	 */
	public void reset() {
		bindCount = 0;
		rebindCount = 0;
		unbindCount = 0;
		lookupCount = 0;
	}

}
