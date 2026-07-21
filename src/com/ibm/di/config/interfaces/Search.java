/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * This class is not used for anything and has no purpose whatsoever.
 * @deprecated
 *
 */
public class Search {

	private boolean caseSensitive;

	public Search() {
	}

	public Search(BaseConfiguration target) {
	}

	public void addTarget(BaseConfiguration target) {
	}

	public void removeTarget(BaseConfiguration target) {
	}

	public void setCaseSensitive(boolean caseSensitive) {
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public int search(String searchString) {
		return 0;
	}

	public void addResult() {
	}

	public void clearResults() {
	}
}
