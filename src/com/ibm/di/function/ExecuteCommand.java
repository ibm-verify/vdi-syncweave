/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.function;

import java.io.*;

/**
 * This class is a helper class used to execute operating system commands. This
 * class is used by the UserFunctions class, which provides helper functions
 * through scripting to TDI users.
 */
public class ExecuteCommand {
	@SuppressWarnings("unused")
	private final static String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private StreamReader input;

	private StreamReader error;

	private int exitCode;

	private Exception exiterror;

	private Process process;
	
	private String encoding;
	

	/**
	 * Constructor for the ExecuteCommand object
	 */
	public ExecuteCommand() {
	}

	/**
	 * Execute command and wait for termination.
	 * 
	 * @param command
	 *            The command to execute
	 * @return The exit code
	 */
	public int exec(String command) {
		try {
			return exec(Runtime.getRuntime().exec(command));
		} catch (Exception e) {
			exiterror = e;
			exitCode = -1;
			return -1;
		}
	}

	/**
	 * Execute command and wait for termination.
	 * 
	 * @param cmdargs
	 *            The command (first item) and its arguments
	 * @return The exit code
	 */
	public int exec(String[] cmdargs) {
		try {
			return exec(Runtime.getRuntime().exec(cmdargs));
		} catch (Exception e) {
			exiterror = e;
			exitCode = -1;
			return -1;
		}
	}

	/**
	 * This method starts two background threads that read the input and error
	 * streams of the Process object. The returned value is the exit code of the
	 * process (this method waits for the process to complete).
	 * 
	 * @param p
	 *            The executing process
	 * @return The exit code
	 */
	public int exec(Process p) {
		this.process = p;
		try {
			if (encoding != null && encoding.length() > 0) {
				input = new StreamReader(p.getInputStream(), encoding);
				error = new StreamReader(p.getErrorStream(), encoding);
			} else {
				input = new StreamReader(p.getInputStream());
				error = new StreamReader(p.getErrorStream());				
			}
			input.start();
			error.start();

			p.getOutputStream().close();

			input.join();
			error.join();
			p.waitFor();
			exitCode = p.exitValue();
			return exitCode;
		} catch (Exception e) {
			exiterror = e;
			exitCode = -1;
			return -1;
		}
	}

	/**
	 * Returns the Process object of the current/last active process.
	 * 
	 * @return The process value
	 */
	public Process getProcess() {
		return process;
	}

	/**
	 * Returns the exit code for the last completed process.
	 * 
	 * @return The exitValue value. A value of -1 means that
	 *         the process failed with an Exception.
	 */
	public int getExitValue() {
		return exitCode;
	}

	/**
	 * Returns true if there was an error executing the last process
	 * 
	 * @return a boolean value - true if there was an error executing the last
	 *         process, false otherwise
	 */
	public boolean failed() {
		return (exiterror != null);
	}

	/**
	 * Returns the standard output from the process as a string.
	 * 
	 * @return The outputBuffer value
	 */
	public String getOutputBuffer() {
		if (input != null && input.buffer != null)
			return input.buffer.toString();
		else
			return null;
	}

	/**
	 * Returns the standard error from the process as a string.
	 * 
	 * @return The errorBuffer value
	 */
	public String getErrorBuffer() {
		if (error != null && error.buffer != null)
			return error.buffer.toString();
		else
			return null;
	}

	/**
	 * Returns the error object from the last execution.
	 * 
	 * @return The error value
	 */
	public Exception getError() {
		return exiterror;
	}

	/**
	 * Local thread that reads an input stream and buffers the stream in a
	 * string buffer.
	 */
	public static class StreamReader extends Thread {
		@SuppressWarnings("unused")
		private final static String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		/**
		 * the stream to read from
		 */
		public BufferedReader reader;

		/**
		 * the string buffer to buffer data into
		 */
		public StringBuffer buffer;

		/**
		 * Constructor for the StreamReader object
		 * 
		 * @param is
		 *            the reader to read from
		 */
		public StreamReader(InputStream is) {
			this.reader = new BufferedReader(new InputStreamReader(is));
			this.buffer = new StringBuffer();
		}

		public StreamReader(InputStream is, String encoding) {
			try {
				this.reader = new BufferedReader(new InputStreamReader(is, encoding));
			} catch (UnsupportedEncodingException e) {
				this.reader = new BufferedReader(new InputStreamReader(is));
			}
			this.buffer = new StringBuffer();
		}

		/**
		 * Main processing method for the StreamReader object
		 */
		public void run() {
			String str;
			try {
				while ((str = reader.readLine()) != null) {
					buffer.append(str + "\n");
				}
			} catch (IOException e) {
				buffer.append("reader: " + e.toString());
			}
		}
	}

	public void setEncoding(String cp) {
		encoding = cp;
		
	}

}
