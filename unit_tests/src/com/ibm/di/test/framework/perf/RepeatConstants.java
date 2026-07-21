/**
 * 
 */
package com.ibm.di.test.framework.perf;

/**
 * @author kaloyan.kolev
 * 
 */
public abstract class RepeatConstants {

	public static double multiplier = 1.0;
	static {
		String repetitions = System.getProperty("com.ibm.di.test.framework.perf.repeat");
		if (repetitions != null && repetitions.trim().length() > 0) {
			multiplier = Double.parseDouble(repetitions);
		}
	}

	public static long get150() {
		return (long) (150 * multiplier);
	}

	public static long get100k() {
		return (long) (100000 * multiplier);
	}

	public static long get150k() {
		return get150() * 1000;
	}

	public static long get200k() {
		return get100k() * 2;
	}

	public static long get250k() {
		return (long) (250000 * multiplier);
	}

	public static long get500k() {
		return get100k() * 5;
	}

	public static long get750k() {
		return get250k() * 3;
	}

	public static long get1m() {
		return get500k() * 2;
	}

	public static long get2m() {
		return get1m() * 2;
	}

	public static long get3m() {
		return get1m() * 3;
	}

	public static long get4m() {
		return get1m() * 4;
	}

	public static long get5m() {
		return get1m() * 5;
	}

	public static long get7m() {
		return get1m() * 7;
	}

	public static long get10m() {
		return get5m() * 2;
	}

	public static long get15m() {
		return get5m() * 3;
	}

	public static long get25m() {
		return get5m() * 5;
	}

	public static long get40m() {
		return get10m() * 4;
	}

	public static long get50m() {
		return get25m() * 2;
	}

	public static long get100m() {
		return get50m() * 2;
	}

	public static long get150m() {
		return get50m() * 3;
	}

	public static long get250m() {
		return get100m() * 2;
	}

	public static long get300m() {
		return get150m() * 2;
	}

	public static long get500m() {
		return get100m() * 5;
	}

	public static long get750m() {
		return get250m() * 3;
	}

	public static long get1g() {
		return get500m() * 2;
	}

	public static long get2g() {
		return get1g() * 2;
	}

	public static long get4g() {
		return get2g() * 2;
	}

	public static long get20g() {
		return get2g() * 10;
	}

	public static long get100g() {
		return get20g() * 5;
	}

	public static long get200g() {
		return get100g() * 2;
	}

}
