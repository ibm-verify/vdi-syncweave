package com.ibm.di.test.runner;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.ibm.di.test.framework.JUnitTestsClassLocator;
import com.ibm.di.test.framework.perf.result.ResultSerializer;
import com.ibm.di.util.ParamUtils;

/**
 * @author kaloyan.kolev
 */
public class TestFrameworkRunner {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static void main(String[] args) throws Exception {
		Map params = ParamUtils.parseCommandLine(args);

		JUnitTestsClassLocator locator = new JUnitTestsClassLocator();
		Class<?>[] testClasses = locator.getTestClasses();
		Result result = JUnitCore.runClasses(testClasses);

		boolean helpRequested = "".equals(ParamUtils.getOptionalParam(params, "?", null))
				|| "".equals(ParamUtils.getOptionalParam(params, "h", null))
				|| "".equals(ParamUtils.getOptionalParam(params, "help", null));

		if (helpRequested) {
			showHelp();
		} else {
			String xmlOutput = ParamUtils.getOptionalParam(params, "o", null);
			String simpleOutput = ParamUtils.getOptionalParam(params, "simple", null);
			File xmlFile = xmlOutput != null ? new File(xmlOutput) : null;
			File simpleFile = simpleOutput != null ? new File(simpleOutput) : null;

			outputResult(result, simpleFile, xmlFile);
			System.exit(result.getFailureCount() + result.getIgnoreCount());
		}
	}

	private static void showHelp() {
		System.out.println("Usage: [OPTIONS...]");
		System.out.println();
		System.out.println("-o              Specifies the file where the test results/errors will be output to."
				+ " If it is missing the results XML will be dumped to the standard output. [Optional]");
		System.out.println("-simple         Specifies the file where the simple results will be output to. "
				+ "The file will contain one line only. If it is missing the results will not be output anywhre. [Optional]");
		System.out.println("-?, -h, -help   Show this help. [Optional]");
	}

	private static void outputResult(Result result, File simpleFile, File xmlFile) {
		ResultSerializer serializer = new ResultSerializer(result);
		if (xmlFile != null) {
			PrintStream out = null;
			try {
				out = new PrintStream(xmlFile, "UTF-8");
				serializer.serializeResult(out);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					out.close();
				}
			}
		} else {
			serializer.serializeResult(System.out);
		}

		if (simpleFile != null) {
			PrintStream out = null;
			try {
				out = new PrintStream(simpleFile, "UTF-8");
				out.print((result.getRunCount() - (result.getFailureCount() + result.getIgnoreCount())) + " "
						+ result.getRunCount() + " " + result.getFailureCount() + " " + result.getIgnoreCount() + " "
						+ (result.getRunTime() / 1000));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}
	}
}
