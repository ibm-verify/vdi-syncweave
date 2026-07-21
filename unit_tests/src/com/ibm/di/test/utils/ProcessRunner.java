package com.ibm.di.test.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class ProcessRunner {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Process proc = null;
	private ProcessBuilder procBuilder = new ProcessBuilder(new String[0]);;

	private File stdOut;
	private boolean appendStdOut;
	private File stdErr;
	private boolean appendStdErr;

	private final String name;

	public ProcessRunner(String name) {
		this.name = name;
	}

	public ProcessRunner(String name, File workDir, List<String> cmd) throws Exception {
		this(name);
		setCmd(cmd);
		setWorkDir(workDir);
	}

	public void setWorkDir(File workDir) {
		if (processRunning()) {
			throw new IllegalStateException("Process already running!");
		}
		procBuilder.directory(workDir);
	}

	/**
	 * @param cmd
	 */
	public void setCmd(List<String> cmd) {
		if (processRunning()) {
			throw new IllegalStateException("Process already running!");
		}
		String arg = null;
		boolean changed = false;
		for (int i = 0; i < cmd.size(); i++) {
			arg = cmd.get(i);

			if (arg.contains(" ")) {
				// make sure cmd line arguments are escaped with "
				if (!arg.startsWith("\"")) {
					arg = "\"" + arg;
					changed = true;
				}

				if (!arg.endsWith("\"")) {
					arg = arg + "\"";
					changed = true;
				}

				if (changed) {
					cmd.set(i, arg);
					changed = false;
				}
			}
		}

		procBuilder.command(cmd);
	}

	public void redirectStdOut(File to, boolean append) {
		if (processRunning()) {
			throw new IllegalStateException("Process already running!");
		}
		stdOut = to;
		appendStdOut = append;
	}

	public void redirectStdErr(File to, boolean append) {
		if (processRunning()) {
			throw new IllegalStateException("Process already running!");
		}
		stdErr = to;
		appendStdErr = append;
	}

	public void startProcess() throws IOException {
		if (processRunning()) {
			throw new IllegalStateException("Process already running!");
		}

		proc = procBuilder.start();

		/*
		 * must consume the stdout and stderr streams of the child process
		 * otherwise it may hang when it tries to do output; this seems to be a
		 * problem with the Java implementation - looks like they always open
		 * handles to the streams
		 */
		consumeStreams(proc.getInputStream(), proc.getErrorStream());
	}

	public boolean processRunning() {
		if (proc == null) {
			return false;
		}

		boolean running = false;

		try {
			proc.exitValue();
		} catch (IllegalThreadStateException ex) {
			running = true;
		}

		return running;
	}

	public int processExitValue() {
		if (proc == null) {
			throw new IllegalStateException("Process not started yet!");
		}
		return proc.exitValue();
	}

	public void waitForProcess() throws InterruptedException {
		if (processRunning()) {
			proc.waitFor();
		}
	}

	private void consumeStreams(InputStream stdOutStream, InputStream stdErrStream) throws FileNotFoundException {
		PrintStream out = System.out;
		PrintStream err = System.err;

		if (stdOut != null) {
			out = new PrintStream(new FileOutputStream(stdOut, appendStdOut));
		}

		if (stdErr != null) {
			if (stdErr.equals(stdOut)) {
				err = out;
			} else {
				err = new PrintStream(new FileOutputStream(stdErr, appendStdErr));
			}
		}

		new Thread(new StreamConsumer(name, stdOutStream, out)).start();
		new Thread(new StreamConsumer(name, stdErrStream, err)).start();
	}

	private static class StreamConsumer implements Runnable {
		private final BufferedReader inpReader;
		private final PrintStream out;
		private final String name;

		public StreamConsumer(String name, InputStream inp, PrintStream out) {
			this.name = name + ": ";
			this.out = out;
			this.inpReader = new BufferedReader(new InputStreamReader(inp));
		}

		public void run() {
			try {
				String line = null;

				while ((line = inpReader.readLine()) != null) {
					if (out != null && line.trim().length() > 0) {
						out.println(name + line);
					}
				}
				inpReader.close();
			} catch (IOException io) {
				io.printStackTrace();
			}
		}
	}
}
