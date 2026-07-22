/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.swt.widgets.Composite;

import com.ibm.di.config.interfaces.BaseConfiguration;

/**
 * This class provides a code completion text editor that provides output
 * attributes, link criteria and a few other common TDI expressions. Completion
 * list is displayed when the user hits CTRL-<space> or types a question mark.
 * The class is intended for writing MQL statements.
 * 
 */
public class MQLStatementEditor extends SQLStatementEditor {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	/**
	 * MQL Key Word string array.
	 */
	private static final String[] DEFAULT_MQL_KEYWORDS = new String[] { "SELECT", "FROM", "ONLY", "WHERE", "FETCH FIRST", "ROW", "ROWS",
		"ORDER BY", "EXISTS", "NOT", "IN", "!=", "==", ">", "<", ">=", "<=", "CONTAINS", "STARTS-WITH", "ENDS-WITH", "EQUALS",
		"NOT-EQUALS", "INSTANCEOF", "AND", "OR", "&&", "||", "IS-NULL", "IS-NOT-NULL", "LOWER", "UPPER", "ASC", "ASCENDING",
		"DESC", "DESCENDING" };

	/**
	 * Constructor
	 * 
	 * @param form
	 *            The (LDB: make a link) FormWidget2 that displays this widget
	 * @param parent
	 *            The parent Composite for this widget
	 * @param cfg
	 *            The Configuration where widget is editing a parameter
	 * @param param
	 *            The name of the parameter in the configuration
	 */
	public MQLStatementEditor(FormWidget2 form, Composite parent, BaseConfiguration cfg, String param) {
		super(form, parent, cfg, param, DEFAULT_MQL_KEYWORDS);
	}

}