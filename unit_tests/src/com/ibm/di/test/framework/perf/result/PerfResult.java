/**
 * 
 */
package com.ibm.di.test.framework.perf.result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * @author kaloyan.kolev
 * 
 */
public class PerfResult extends Result {

	private Map<String, Long> times = new HashMap<String, Long>();
	private Result result;

	public Map<String, Long> getTimes() {
		return times;
	}

	public void addTime(String key, long t) {
		times.put(key, t);
	}

	public void setResult(Result result) {
		this.result = result;
	}

	@Override
	public int getFailureCount() {
		return result.getFailureCount();
	}

	@Override
	public RunListener createListener() {
		return result.createListener();
	}

	@Override
	public List<Failure> getFailures() {
		return result.getFailures();
	}

	@Override
	public int getIgnoreCount() {
		return result.getIgnoreCount();
	}

	@Override
	public int getRunCount() {
		return result.getRunCount();
	}

	@Override
	public long getRunTime() {
		return result.getRunTime();
	}

	@Override
	public boolean wasSuccessful() {
		return result.wasSuccessful();
	}
}
