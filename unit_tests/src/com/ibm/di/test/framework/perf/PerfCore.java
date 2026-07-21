/**
 * 
 */
package com.ibm.di.test.framework.perf;

import java.util.Stack;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.ibm.di.test.framework.perf.result.PerfResult;

/**
 * @author kaloyan.kolev
 * 
 */
public class PerfCore {

	private final Class<?>[] classes;
	private JUnitCore jUnitCore;

	public PerfCore(Class<?>... classes) {
		this.classes = classes;
		jUnitCore = new JUnitCore();
	}

	public PerfResult run() {
		final PerfResult presult = new PerfResult();

		jUnitCore.addListener(new RunListener() {
			Stack<Long> stack = new Stack<Long>();

			@Override
			public void testStarted(Description desc) throws Exception {
				super.testStarted(desc);
				stack.push(System.currentTimeMillis());
			}

			@Override
			public void testFinished(Description desc) throws Exception {
				long end = System.currentTimeMillis();
				presult.addTime(desc.getDisplayName(), end - stack.pop());
				super.testFinished(desc);
			}

			@Override
			public void testIgnored(Description description1) throws Exception {
				stack.pop();
				super.testIgnored(description1);
			}

			@Override
			public void testFailure(Failure failure1) throws Exception {
				stack.pop();
				super.testFailure(failure1);
			}

		});
		presult.setResult(jUnitCore.run(classes));

		return presult;
	}
}
