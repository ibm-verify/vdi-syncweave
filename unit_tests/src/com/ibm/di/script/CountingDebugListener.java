package com.ibm.di.script;

import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.ASTTree.ASTNode;
import com.ibm.jscript.engine.IExecutionContext;

public class CountingDebugListener implements ScriptEngineOptions.TDIDebugListener {
	private int callCount = 0;

	public void debugStatement(ASTNode statement, IExecutionContext context) throws JavaScriptException {
		++callCount;
	}

	public int getCallCount() {
		return callCount;
	}
}
