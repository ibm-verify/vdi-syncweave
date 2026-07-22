/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import javax.naming.*;
import java.util.*;
/**
 * An Exception used to signal a loop in the inheritance chain of a BaseConfiguration object.
 *
 */
public class InheritanceLoopException extends Exception {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -5977834080357995975L;

	private Name source;

	private Vector<Name> chain;

	public InheritanceLoopException(Name source, Vector<Name> chain) {
		super("");
		this.source = source;
		this.chain = chain;
	}

	public String toString() {
		StringBuffer str = new StringBuffer("Inheritance loop detected: ");
		str.append( "Source: " );
		str.append( source );
		str.append( "\n" );
		for (Name name:chain) {
			str.append( "  <-- " );
			str.append( name );
			str.append( "\n" );
		}
		return str.toString();
	}

}
