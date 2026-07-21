/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.Properties;

import javax.naming.CompoundName;
import javax.naming.InvalidNameException;

/**
 * A CompoundName that can be used to access hierarchical SchemaItems.
 *
 */
public class SchemaName extends CompoundName {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Properties namesyntax = new Properties();
	static {
		namesyntax.put("jndi.syntax.direction", "left_to_right");
		namesyntax.put("jndi.syntax.separator", ".");
		namesyntax.put("jndi.syntax.ignorecase", "false");
		namesyntax.put("jndi.syntax.escape", "\\");
		namesyntax.put("jndi.syntax.trimblanks", "true");
		namesyntax.put("jndi.syntax.separator.ava", ",");
		namesyntax.put("jndi.syntax.separator.typeval", "=");
	}

	public SchemaName() throws InvalidNameException {
		super("", namesyntax);
	}

	public SchemaName(String ref) throws InvalidNameException {
		super(ref, namesyntax);
	}

}
