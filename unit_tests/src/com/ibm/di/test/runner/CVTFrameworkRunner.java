package com.ibm.di.test.runner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;

import com.ibm.di.test.CVTComponent;
import com.ibm.di.test.CVTTest;
import com.ibm.di.test.framework.JUnitTestsClassLocator;
import com.ibm.di.test.utils.TestUtils;
import com.ibm.di.util.ParamUtils;

/**
 * Executes CVT test cases represented as JUnit tests. In order for a test case
 * to be executed the following must be fulfilled: the name of the test class
 * must end with CVT, the class must be annotated with the CVTComponent
 * annotation and each test method must be annotated with the CVTTest
 * annotation.
 */
public class CVTFrameworkRunner {

	public static void main(String[] args) throws Exception {

		Map params = ParamUtils.parseCommandLine(args);

		boolean helpRequested = "".equals(ParamUtils.getOptionalParam(params, "?", null))
				|| "".equals(ParamUtils.getOptionalParam(params, "h", null))
				|| "".equals(ParamUtils.getOptionalParam(params, "help", null));

		if (helpRequested) {
			showHelp();
			return;
		}

		JUnitTestsClassLocator locator = new JUnitTestsClassLocator();
		Class<?>[] testClasses = locator.getCVTClasses();

		String release = ParamUtils.getOptionalParam(params, "release", null);
		String component = ParamUtils.getOptionalParam(params, "component", null);
		String testcase = ParamUtils.getOptionalParam(params, "testcase", null);
		String simpleOutput = ParamUtils.getOptionalParam(params, "simple", null);
		String xmlOutput = ParamUtils.getOptionalParam(params, "o", null);
		
		File xmlFile = xmlOutput != null ? new File(xmlOutput) : null;
		File simpleFile = simpleOutput != null ? new File(simpleOutput) : null;
		
		testClasses = filterTestClassesByRelease(testClasses, release);
		testClasses = filterTestClassesByComponent(testClasses, component);

		if (testClasses.length > 0) {

			CVTFilter testFilter = new CVTFilter();
			if (testcase != null) {
				testFilter.addTestCase(testcase);
			}
			Request request = Request.classes(testClasses).filterWith(testFilter);

			JUnitCore junitCore = new JUnitCore();
			Result result = junitCore.run(request);

			TestUtils.outputResult(result, simpleFile, xmlFile);

			System.exit(result.getFailureCount() + result.getIgnoreCount());
		} else {
			System.out.println("No matching test cases found.");
		}
	}

	private static void showHelp() {
		System.out.println("Usage: [OPTIONS...]");
		System.out.println();
		System.out.println("-release              Specifies the TDI release whose CVT test cases will be run."
				+ " For example: -release 7.1. If missing, will run tests for all releases. [Optional]");
		System.out.println("-component            Specifies the name of the CVT component whose tests will be run. "
				+ " If missing, will run tests from all CVT components. [Optional]");
		System.out.println("-testcase         Specifies the name(s) of the Test case(s) to be run. [Optional]");
		System.out.println("-o              Specifies the file where the test results/errors will be output to."
				+ " If it is missing the results XML will be dumped to the standard output. [Optional]");
		System.out.println("-simple         Specifies the file where the simple results will be output to. "
				+ "The file will contain one line only. If it is missing the results will not be output anywhre. [Optional]");
		System.out.println("-?, -h, -help   Show this help. [Optional]");
	}

	/**
	 * Filter out test classes which do not match the specified TDI release
	 * number (e.g. "6.1.1"). Each release has its own package for CVT tests
	 * (e.g. com.ibm.di.cvt611).
	 */
	private static Class<?>[] filterTestClassesByRelease(Class<?>[] classes, String release) throws Exception {
		if (release == null) {
			// no filtering
			return classes;
		}
		String packageSuffix = "com.ibm.di.cvt" + release.replaceAll("\\.", "") + ".";
		List<Class<?>> result = new ArrayList<Class<?>>();
		for (Class c : classes) {
			if (c.getName().startsWith(packageSuffix)) {
				result.add(c);
			}
		}
		return result.toArray(new Class<?>[result.size()]);
	}

	/**
	 * Filter out test classes which do not match the specified component.
	 * (Component name is specified using the name attribute of the CVTComponent
	 * annotation.)
	 */
	private static Class<?>[] filterTestClassesByComponent(Class<?>[] classes, String component) throws Exception {
		if (component == null) {
			// no filtering
			return classes;
		}
		List<Class<?>> result = new ArrayList<Class<?>>();
		for (Class c : classes) {
			CVTComponent cvtComponentAnno = (CVTComponent) c.getAnnotation(CVTComponent.class);
			if (cvtComponentAnno != null) {
				if (component.equalsIgnoreCase(cvtComponentAnno.name())) {
					result.add(c);
				}
			}
		}
		return result.toArray(new Class<?>[result.size()]);
	}

	/**
	 * Filter for JUnit tests. Matches only test methods annotated with the
	 * com.ibm.di.test.CVTTest annotation.
	 */
	private static class CVTFilter extends Filter {

		/**
		 * A set of allowed test-case names. If empty - allow all test-cases.
		 */
		private Set<String> testcases = new HashSet<String>();

		public void addTestCase(String testcase) {
			testcases.add(testcase.toLowerCase());
		}

		@Override
		public String describe() {
			return "Filter for CVT JUnit tests";
		}

		@Override
		public boolean shouldRun(Description description) {

			boolean result;

			if (description.getChildren().size() == 0) {
				// test method - must have @CVTTest annotation
				CVTTest cvtTestAnno = description.getAnnotation(CVTTest.class);
				if (cvtTestAnno != null) {
					if (testcases.size() > 0) {
						result = testcases.contains(cvtTestAnno.name().toLowerCase());
						if(!result){
							result = testcases.contains(description.getMethodName().toLowerCase());
						}
					} else {
						// no filtering by test-case name
						result = true;
					}
				} else {
					// the test method does not have a test-case name
					result = false;
				}
			} else {
				result = true;
			}

			return result;
		}
	}
}
