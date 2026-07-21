package com.ibm.di.test.framework.perf.result;

import java.io.PrintStream;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class ResultSerializer {

	private final Result[] results;
	private static long totalTime = 0; // in milliseconds

	public ResultSerializer(Result result) {
		this.results = new Result[] { result };
	}

	public ResultSerializer(Result... results) {
		this.results = results;
	}

	public void serializeResult(PrintStream out) {
		out.println("<?xml encoding=\"UTF-8\" version=\"1.0\" ?>");
		out.println();
		if (results != null) {
			if (results.length > 1) {
				out.println("<Results>");
				for (int i = 0; i < results.length; i++) {
					System.out.println("<!-- Run " + (i + 1) + " -->");
					outputSingleResult(results[i], out, 1);
				}

				out.println();
				indentOutput(out, 1);
				totalTime /= 1000; // transform in seconds
				String totalTimeStirng = String.format("%d:%02d:%02d", totalTime / 3600, (totalTime % 3600) / 60, (totalTime % 60));
				out.println("<TotalRunTime>" + totalTimeStirng + "</TotalRunTime>");

				out.println("</Results>");
			} else if (results.length == 1) {
				outputSingleResult(results[0], out, 0);
			}
		}
		out.flush();
	}

	private static void outputSingleResult(Result result, PrintStream out, int indentLevel) {
		indentOutput(out, indentLevel);
		out.println("<Result>");

		indentOutput(out, ++indentLevel);
		int runCount = (result.getRunCount() - (result.getFailureCount() + result.getIgnoreCount()));
		if (runCount < 0)
			runCount = 0;
		out.println("<SuccessCount>" + runCount + "</SuccessCount>");

		indentOutput(out, indentLevel);
		out.println("<RunCount>" + result.getRunCount() + "</RunCount>");

		indentOutput(out, indentLevel);
		out.println("<FailureCount>" + result.getFailureCount() + "</FailureCount>");

		indentOutput(out, indentLevel);
		out.println("<IgnoreCount>" + result.getIgnoreCount() + "</IgnoreCount>");

		totalTime += result.getRunTime(); // add to total time
		long dur = result.getRunTime() / 1000; // runtime in seconds

		String timeString = String.format("%d:%02d:%02d", dur / 3600, (dur % 3600) / 60, (dur % 60));
		indentOutput(out, indentLevel);
		out.println("<RunTime>" + timeString + "</RunTime>");

		out.println();
		indentOutput(out, indentLevel);
		out.println("<Failures>");

		for (Failure f : result.getFailures()) {
			indentOutput(out, ++indentLevel);
			out.println("<Failure>");

			indentOutput(out, ++indentLevel);
			out.println("<TestHeader>" + f.getTestHeader() + "</TestHeader>");

			indentOutput(out, indentLevel);
			out.println("<Message>" + f.getMessage() + "</Message>");

			indentOutput(out, indentLevel);
			out.println("<Description>" + f.getDescription() + "</Description>");

			indentOutput(out, indentLevel);
			out.println("<Exception>" + f.getException() + "</Exception>");

			indentOutput(out, indentLevel);
			out.println("<Trace>\n" + f.getTrace() + (f.getTrace().endsWith("\n") ? "" : "\n") + "\t\t\t\t</Trace>");

			indentOutput(out, --indentLevel);
			out.println("</Failure>");
			--indentLevel;
		}
		indentOutput(out, indentLevel);
		out.println("</Failures>");

		indentOutput(out, --indentLevel);
		out.println("</Result>");

		out.flush();
	}

	private static void indentOutput(PrintStream out, int indentLevel) {
		for (int i = 0; i < indentLevel; i++) {
			out.print("\t");
		}
	}
}
