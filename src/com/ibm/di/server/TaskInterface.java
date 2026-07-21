/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import com.ibm.di.script.*;
import com.ibm.di.entry.*;

public interface TaskInterface {

	public RSInterface getParent();

	public Log getLog();

	public AssemblyLineComponent getConnector(String name);

	public ScriptEngine getScriptEngine();

	public Object getConfig(String name);

	public void logmsg(Object msg);

	public Entry getWork();

	public Entry getCurrentWork();

	public Entry getResult();

	public void debugMsg(Object obj) throws Exception;

	public void debugBreak(Object obj) throws Exception;

	public String getNullBehavior();

	public String getNullBehaviorValue();

	public String getNullDefinition();

	public String getNullDefinitionValue();

}
