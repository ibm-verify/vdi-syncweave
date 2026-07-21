package com.ibm.di.test.runner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.ibm.di.function.UserFunctions;
import com.ibm.di.test.framework.CVTSelector;
import com.ibm.di.test.framework.ConsoleInterface;
import com.ibm.di.test.utils.ProcessRunner;

/**
 * Runs TCs one by one based on a input list of tests (-t). The runner gathers
 * the specified list of logs (-g) to a specified directory (-o). The result
 * structure is as follows:
 * 
 * <pre>
 * &lt;outputDir&gt;
 * 	+---&gt;&lt;TC_Name&gt;
 * 	|	+---&gt;logFiles
 * 	....
 * </pre>
 * 
 * This runner executes a separate process for each TC. The executed command is
 * provided by the last arguments following the -c switch. Each line in that
 * file specifies a different argument of the executed command. The runner can
 * expand strings from the provided command as follows:
 * 
 * <ul>
 * <li>%TC% - expanded to the name of the TC</li>
 * </ul>
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TCLogsCollector {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static void main(String[] args) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		List<String> cmd = new LinkedList<String>();

		boolean readingCmd = false;
		String currentSwitch = null;

		for (String arg : args) {
			if (readingCmd) {
				cmd.add(arg);
			} else {
				if (currentSwitch == null) {
					if ("-c".equals(arg)) {
						readingCmd = true;
					} else if (arg.startsWith("-")) {
						currentSwitch = arg.substring(1);
					}
				} else {
					if (arg.startsWith("-")) {
						params.put(currentSwitch, "");
					} else {
						params.put(currentSwitch, arg);
					}
					currentSwitch = null;
				}
			}
		}

		if (currentSwitch != null) {
			params.put(currentSwitch, "");
			currentSwitch = null;
		}

		if (params.containsKey("?") || (params.size() == 0 && cmd.size() == 0)) {
			System.out.println("Usage: [-g <logs_list>][-t <tc_list>][-o <output_dir>][-?][-c <cmd_tmpl>]");
			System.out.println("\t-g <logs_list>   List of the logs to gather after each TC is being executed.");
			System.out.println("\t-t <tc_list>     List of TCs to execute. Omit to get asked interactively.");
			System.out.println("\t-o <output_dir>  The directory to collect the logs into. Omit to use current working dir.");
			System.out.println("\t-?               Show this help.");
			System.out
					.println("\t-c <cmd_tmpl>    Specifies the command template to use when executing each test. Must be last argument! Supported substitutions:");
			System.out
					.println("\t                       %TC% - substituted by the TC name for which the command will be executed.");
		} else {
			Set<String> tcNames = params.get("t") == null ? selectTestsInteractivly() : parseMultiValuedArgument(params.get("t"),
					",");

			if (cmd.isEmpty()) {

				boolean isWin = System.getProperty("os.name").regionMatches(true, 0, "win", 0, 3);

				// use default cmd
				cmd.add("bin" + File.separatorChar + "runcvt." + (isWin ? "bat" : "sh"));
				cmd.add("-testcase");
				cmd.add("%TC%");
				cmd.add("-o");
				cmd.add("std.out");
				System.out.println("No command provided will use default one:\n " + cmd);
			}

			File outputDir = null;
			if (params.get("o") != null) {
				outputDir = new File(params.get("o"));
				outputDir.mkdirs();
			} else {
				System.out.println("No output dir specified. Will use current working dir.");
				outputDir = new File(".");
			}

			Set<String> gatherLogs = params.get("g") == null ? new HashSet<String>() : parseMultiValuedArgument(params.get("g"),
					File.pathSeparator);

			System.out.println();
			runTests(tcNames, cmd, outputDir, gatherLogs);
		}
	}

	/**
	 * @param string
	 * @return
	 */
	private static Set<String> parseMultiValuedArgument(String string, String sepRegEx) {
		return new TreeSet<String>(Arrays.asList(string.split(sepRegEx)));
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private static Set<String> selectTestsInteractivly() throws IOException {
		ConsoleInterface console = new ConsoleInterface(System.in, System.out, System.err);
		console.println("No tests specified. Will use interactive selection based on the tests found on the classpath.");

		console.println("Searching classpath for TCs...");
		CVTSelector selector = new CVTSelector();
		Set<String> result = new TreeSet<String>();

		Set<String> relNames = selector.getReleasesNames();
		console.println("Found Releases: " + relNames);

		if (!console.getYesNo("Select all Releases?", false)) {
			// start picking releases.
			for (String relName : relNames) {
				if (console.getYesNo("Select release \"" + relName + "\"", false)) {

					Set<String> compNames = selector.getComponentsNamesByRelease(relName);
					console.println("Found Componenets: " + compNames);

					if (!console.getYesNo("Select all Components?", false)) {
						// start picking components
						for (String compName : compNames) {
							if (console.getYesNo("Select component \"" + compName + "\" from release \"" + relName + "\"", false)) {

								Set<String> testNames = selector.getTestsNamesByReleaseAndComponent(relName, compName);
								console.println("Found Tests: " + testNames);

								if (!console.getYesNo("Select all Tests?", false)) {
									// start picking tests
									for (String tc : selector.getTestsNamesByReleaseAndComponent(relName, compName)) {
										if (console.getYesNo("Select test \"" + tc + "\"", false)) {
											result.add(tc);
										}
									}
								} else {
									result.addAll(selector.getTestsNamesByReleaseAndComponent(relName, compName));
								}
							}
						}
					} else {
						for (String compName : compNames) {
							result.addAll(selector.getTestsNamesByReleaseAndComponent(relName, compName));
						}
					}
				}
			}
		} else {
			for (String relName : relNames) {
				for (String compName : selector.getComponentsNamesByRelease(relName)) {
					result.addAll(selector.getTestsNamesByReleaseAndComponent(relName, compName));
				}
			}
		}
		return result;
	}

	private static void runTests(Set<String> tcNames, List<String> cmdTemplate, File outputDir, Set<String> gatherLogs)
			throws Exception {

		for (String tcName : tcNames) {
			File logsDestDir = new File(outputDir, tcName);
			logsDestDir.mkdirs();
			List<String> cmd = expandCmdWithTCName(cmdTemplate, tcName);

			System.out.println(" --Starting TC: " + tcName);
			ProcessRunner runner = new ProcessRunner("Process", new File("."), cmd);
			runner.startProcess();
			runner.waitForProcess();
			System.out.println(" --TC process finished with status: " + runner.processExitValue());

			gatherFiles(gatherLogs, logsDestDir);
		}
	}

	/**
	 * @param gatherLogs
	 * @param outputDir
	 * @throws Exception
	 */
	private static void gatherFiles(Set<String> gatherLogs, File outputDir) throws Exception {
		for (String log : gatherLogs) {
			File from = new File(log);
			UserFunctions.copyFile(from, new File(outputDir, from.getName()), true);
		}
	}

	private static List<String> expandCmdWithTCName(List<String> cmdTemplate, String tcName) {
		List<String> cmd = new ArrayList<String>(cmdTemplate.size());

		String tmpl = null;
		for (int i = 0; i < cmdTemplate.size(); i++) {
			tmpl = cmdTemplate.get(i);
			tmpl = tmpl.replaceAll("%TC%", tcName);
			cmd.add(tmpl);
		}
		return cmd;
	}
}
