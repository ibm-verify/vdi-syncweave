/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.report.aloverview;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class DataTable {

	private List<String> headers = null;
	private List<List<String>> rows = new ArrayList<List<String>>();
	
	/**
	 * 
	 * @param headers
	 */
	public DataTable(List<String> headers) {
		this.headers = new ArrayList<String>(headers);
	}
	
	/**
	 * 
	 * @param row
	 */
	public void addRow(List<String> row) {
		if (row == null) {
			return;
		}
		
		if (row.size() != headers.size()) {
			throw new RuntimeException("Incompatible data row size!");
		}
		
		rows.add(new ArrayList<String>(row));
	}
	
	/**
	 * 
	 * @return List<String>
	 */
	public List<String> getHeaders() {
		return headers;
	}

	/**
	 * 
	 * @return List<List<String>>
	 */
	public List<List<String>> getRows() {
		return rows;
	}	
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isEmpty() {
		return rows.isEmpty();
	}
	
}
