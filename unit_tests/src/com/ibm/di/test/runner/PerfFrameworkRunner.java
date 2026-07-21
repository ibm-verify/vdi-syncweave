package com.ibm.di.test.runner;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.ibm.di.test.framework.JUnitTestsClassLocator;
import com.ibm.di.test.framework.perf.PerfCore;
import com.ibm.di.test.framework.perf.RepeatConstants;
import com.ibm.di.test.framework.perf.result.PerfResult;
import com.ibm.di.test.framework.perf.result.ResultSerializer;
import com.ibm.di.test.framework.perf.result.ResultsHolder;
import com.ibm.di.util.ParamUtils;

/**
 * @author kaloyan.kolev
 */
public class PerfFrameworkRunner {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String DEFAULT_REPEAT_COUNT_STR = "1";
	private static final int DEFAULT_REPEAT_COUNT = Integer.parseInt(DEFAULT_REPEAT_COUNT_STR);

	private static final String DEFAULT_PRECISION_STR = "2";
	private static final int DEFAULT_PRECISION = Integer.parseInt(DEFAULT_PRECISION_STR);

	public static final String PARAMETER_KEY_BASELINE = "baseline";
	public static final String PARAMETER_KEY_COMPARE = "compare";
	public static final String PARAMETER_KEY_REPEAT = "repeat";
	public static final String PARAMETER_KEY_PRECISION = "precision";
	public static final String PARAMETER_KEY_PERF_CLASS = "perfClass";
	public static final String PARAMETER_KEY_EXEC_CONTEXT = "ctx";

	private static final String EXT_EXEC_CONTEXT = "ext";
	private static final String INT_EXEC_CONTEXT = "int";
	private static final String DEFAULT_EXEC_CONTEXT = EXT_EXEC_CONTEXT;

	public static void main(Properties args) throws Exception {
		List<String> argsList = new LinkedList<String>();

		for (Entry<Object, Object> e : args.entrySet()) {
			argsList.add(e.getKey().toString());
			if (e.getValue().toString() != null && e.getValue().toString().trim().length() > 0) {
				argsList.add(e.getValue().toString());
			}
		}
		String[] argsArr = new String[argsList.size()];
		argsArr = argsList.toArray(argsArr);
		main(argsArr);
	}

	public static void main(String[] args) throws Exception {
		Map params = ParamUtils.parseCommandLine(args);
		String baseLineStr = ParamUtils.getRequiredParam(params, PARAMETER_KEY_BASELINE);
		String repeatStr = ParamUtils.getOptionalParam(params, PARAMETER_KEY_REPEAT, DEFAULT_REPEAT_COUNT_STR);
		boolean compare = "".equals(ParamUtils.getOptionalParam(params, PARAMETER_KEY_COMPARE, null))
				|| Boolean.parseBoolean(ParamUtils.getOptionalParam(params, PARAMETER_KEY_COMPARE, null));
		String precisionStr = ParamUtils.getOptionalParam(params, PARAMETER_KEY_PRECISION, DEFAULT_PRECISION_STR);
		String perfClass = ParamUtils.getOptionalParam(params, PARAMETER_KEY_PERF_CLASS, null);
		String context = ParamUtils.getOptionalParam(params, PARAMETER_KEY_EXEC_CONTEXT, DEFAULT_EXEC_CONTEXT);

		boolean helpRequested = "".equals(ParamUtils.getOptionalParam(params, "?", null))
				|| "".equals(ParamUtils.getOptionalParam(params, "h", null))
				|| "".equals(ParamUtils.getOptionalParam(params, "help", null));

		if (helpRequested) {
			showHelp();
		} else {
			// if less then 1.0 then it will multiply the number of repeats for
			// every test thus allowing to decrease the repeats if/ needed
			int repeatCount = DEFAULT_REPEAT_COUNT;

			double rep = 0.0;
			try {
				rep = Float.parseFloat(repeatStr);
			} catch (NumberFormatException ignore) {
			}

			if (rep < 1.0) {
				RepeatConstants.multiplier = rep;
			}
			if (rep > 1.0) {
				repeatCount = (int) rep;
			}

			int precision = precisionStr.equals(DEFAULT_PRECISION_STR) ? DEFAULT_PRECISION : Integer.parseInt(precisionStr);
			if (precision <= 0) {
				precision = DEFAULT_PRECISION;
			}

			// find the performance classes in this JVM.
			Class<?>[] testClasses = null;

			if (perfClass == null || perfClass.trim().length() == 0) {

				JUnitTestsClassLocator locator = new JUnitTestsClassLocator();
				if (EXT_EXEC_CONTEXT.equalsIgnoreCase(context)) {
					testClasses = locator.getExtPerfClasses();
				} else if (INT_EXEC_CONTEXT.equalsIgnoreCase(context)) {
					testClasses = locator.getIntPerfClasses();
				}
			} else {
				testClasses = new Class<?>[1];
				testClasses[0] = Class.forName(perfClass);
			}

			// measure the performance...
			ResultsHolder results = new ResultsHolder(runPerfSuite(testClasses, repeatCount));
			ResultSerializer serializer = new ResultSerializer(results.getResults());
			serializer.serializeResult(System.out);

			// find out where to store the results to.
			File baseLineFile = new File(baseLineStr);
			File baseLineFileNew = null;
			if (baseLineFile.exists() && compare) {
				// The user wants to compare the results with
				// an existing baseline file.
				String name = baseLineFile.getName();
				int dotPos = name.indexOf('.');
				if (dotPos == -1) {
					baseLineFileNew = new File(baseLineFile.getParentFile(), name + ".new");
				} else {
					baseLineFileNew = new File(baseLineFile.getParentFile(), name.substring(0, dotPos) + ".new"
							+ name.substring(dotPos));
				}
				results.storeResultsTo(baseLineFileNew);
				results.compareResultsWith(new ResultsHolder(baseLineFile), precision);
			} else {
				results.storeResultsTo(baseLineFile);
			}
		}
	}

	private static void showHelp() {
		System.out.println("Usage: -" + PARAMETER_KEY_BASELINE + " [OPTIONS...]");
		System.out.println();

		System.out.println("-" + PARAMETER_KEY_BASELINE + "       Specifies where the performance results will be output to. "
				+ "If the specified file does not exist it will be created and the average performance times will be output there."
				+ " If this file already exists and -" + PARAMETER_KEY_COMPARE + " is specified it will not be overrided. "
				+ "In that case a new file will be created and the two baselines will be compared. [Required]");
		System.out.println("-" + PARAMETER_KEY_REPEAT + "         This specifies the number of times each test will be repeated. "
				+ "If a float number <0 zero is specified the configured time of each test will be reduced using this value as "
				+ "multiplier. [Optional]");
		System.out.println("-" + PARAMETER_KEY_COMPARE + "        This will tell the program to compare the existing baseline "
				+ "with the new performance times. This is only relevant if -" + PARAMETER_KEY_BASELINE + " is specified and that "
				+ "file exists. [Optional]");
		System.out.println("-" + PARAMETER_KEY_PRECISION + "      This specifies the acceptable deviation in performance results. "
				+ "This one is only considered when comparing baselines. This is in percents and if not provided the value of "
				+ DEFAULT_PRECISION_STR + "% will be used [Optional]");
		System.out.println("-" + PARAMETER_KEY_PERF_CLASS + "      This specifies the class to be run. If not specified the -"
				+ PARAMETER_KEY_EXEC_CONTEXT + " is used to decide what tests to run. [Optional]");
		System.out.println("-" + PARAMETER_KEY_EXEC_CONTEXT
				+ "            This specifies in what context this framework is used. The value \"" + EXT_EXEC_CONTEXT
				+ "\" means that the framework is executed outside of TDI. The value \"" + INT_EXEC_CONTEXT
				+ "\" means that the framework is executed inside the TDI's JVM. Default is \"" + DEFAULT_EXEC_CONTEXT
				+ "\". [Optional]");
		System.out.println("-?, -h, -help   Show this help. [Optional]");
	}

	private static PerfResult[] runPerfSuite(Class<?>[] testClasses, int repeatCount) {
		PerfCore core = new PerfCore(testClasses);
		PerfResult[] results = new PerfResult[repeatCount];

		for (int i = 0; i < repeatCount; i++) {
			results[i] = core.run();
		}

		return results;
	}
}
