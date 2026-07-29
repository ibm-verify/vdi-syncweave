/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * Command line test harness for IDI SAP FC.
 * <p>
 * Uses the internal Sap Adapter form the RFC Function Component to invoke an
 * RFC based on a used supplied input XML file. The SAP JCo libraies must be in
 * the classpath and java.library.path.<br>
 * <b>NB: On UNIX, the JCo shared libraries, librfc.so and libsapjcorfc.so must
 * be added to the LD_LIBRARY_PATH environment variable.</b>
 * </p>
 * <p>
 * (ITDI_HOME)/_jvm/bin/java -cp
 * (SAP_JCO_HOME)/sapjaco.jar:(ITDI_HOME)/jars/functions/SapR3RfcFCV3.jar
 * -Djava.library.path=(SAP_JCO_HOME) com.ibm.di.fc.sapr3rfcv3.RfcXmlInvoker -f
 * [input XML file] -o [output XML file] -p [JCO Connection properties file]
 * </p>
 * <p>
 * The contents of the JCO Properties file represent the R/3 client connection
 * parameters for the R/3 system. <br>
 * An example of the values in the property file is shown below:<br>
 * <code>
 * jco.client.client=(R/3 CLIENT)<br>
 * jco.client.user=(R/3 USER NAME)<br>
 * jco.client.passwd=(R/3 USER PASSWORD)<br>
 * jco.client.sysnr=(R/3 SYSTEM NUMBER)<br>
 * jco.client.ashost=(R/3 APPLICATION SERVER HOSTNAME OR IP ADDRESS<br>
 * jco.client.trace=(RFC API TRACE: 1 == ON; 0 == OFF)<br>
 * </code>
 * </p>
 */
public final class RfcXmlInvoker {

	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/*
	 * The version string returned by getVersion(). The %% tokens are replaced
	 * at compile time by ANT script.
	 */
	private static final String VERSION_INFO = "2.0-di7.1.1 %I% 20%E%";

	/*
	 * The function component name as reported in the log files. The %% tokens are
	 * replaced at compile time by ANT script.
	 */
	private static final String COMPONENT_NAME = "SAP R/3 RFC Functional Component";

	private static final String OPTION_F = "-f";
	private static final String OPTION_O = "-o";
	private static final String OPTION_P = "-p";

	private Properties configProps;
	private File inXml;
	private File outXml;
	
	/**
	 * Command-line exit code.
	 */
	private int exitCode = 0;

	private RfcXmlInvoker() {
		super();
	}

	/**
	 * Create the RfcInvoker.
	 * 
	 * @param args
	 *            The commandline argument from main.
	 * @throws IOException
	 *             If the property file could not be read.
	 */
	public RfcXmlInvoker(String[] args) throws IOException {
		this();
		initFromCmdLine(args);
	}

	private void initFromCmdLine(String[] args) throws IOException {
		if (args.length < 6) {
			RfcXmlInvoker.usage();
			exitCode = 1;
		} else {

			int i = 0;
			while ((i + 1) < args.length) {
				if (args[i].equals(OPTION_F)) {
					++i;
					String filename = args[i];
					inXml = new File(filename);
				} else if (args[i].equals(OPTION_O)) {
					++i;
					outXml = new File(args[i]);
				} else if (args[i].equals(OPTION_P)) {
					++i;
					initProperties(new File(args[i]));
				} else {
					RfcXmlInvoker.usage();
					exitCode = 1;
					break;
				}
	
				++i;
			}
		}
	}

	private void initProperties(File f) throws IOException {
		FileInputStream is = null;
		try {
			is = new FileInputStream(f);
			configProps = new Properties();
			configProps.load(is);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException x) {
				x.printStackTrace();
			}
		}
	}

	/**
	 * Run the invoker.
	 * @throws IOException 
	 */
	public void run() throws IOException {
		SapClientConnection client;
		client = SapClientConnectionFactory.create(configProps);
		SapAdapter sa = new SapAdapter();
		LogProxy log = new LogProxyImpl();
		try {
			String xmlResult = sa.sendReceive(client, inXml, log);
			
			FileWriter fw = new FileWriter(outXml);
			PrintWriter pw = new PrintWriter(fw);
			try {
				pw.print(xmlResult);
				pw.flush();
			} finally {
				pw.close();
			}
		} catch (IOException x) {
			x.printStackTrace();
		} catch (SapR3RfcFCException x) {
			x.printStackTrace();
		}
	}

	/**
	 * Command line test harness for IDI SAP FC.
	 * <p>
	 * Uses the internal Sap Adapter form the RFC Function Component to invoke
	 * an RFC based on a used supplied input XML file. The SAP JCo libraies must
	 * be in the classpath and java.library.path. <b>NB: On UNIX, the JCo shared
	 * libraries, librfc.so and libsapjcorfc.so must be added to the
	 * LD_LIBRARY_PATH environment variable.</b>
	 * </p>
	 * <p>
	 * (ITDI_HOME)/_jvm/bin/java -cp
	 * (SAP_JCO_HOME)/sapjaco.jar:(ITDI_HOME)/jars/functions/SapR3RfcFCV3.jar
	 * -Djava.library.path=(SAP_JCO_HOME) com.ibm.di.fc.sapr3rfcv3.RfcXmlInvoker
	 * -f [input XML file] -o [output XML file] -p [JCO Connection properties
	 * file]
	 * </p>
	 * <p>
	 * The contents of the JCO Properties file represent the R/3 client
	 * connection parameters for the R/3 system. <br>
	 * An example of the values in the property file is shown below:<br>
	 * <code>
	 * jco.client.client=(R/3 CLIENT)<br>
	 * jco.client.user=(R/3 USER NAME)<br>
	 * jco.client.passwd=(R/3 USER PASSWORD)<br>
	 * jco.client.sysnr=(R/3 SYSTEM NUMBER)<br>
	 * jco.client.ashost=(R/3 APPLICATION SERVER HOSTNAME OR IP ADDRESS<br>
	 * jco.client.trace=(RFC API TRACE: 1 == ON; 0 == OFF)<br>
	 * </code>
	 * </p>
	 * 
	 * @param args
	 *            -f [input XML file] -o [output XML file] -p [JCO Connection
	 *            properties file]
	 */
	public static void main(String[] args) {

		int exitCode = 0;
		try {
			displayCopyright();
			RfcXmlInvoker rfcInvoker = new RfcXmlInvoker(args);
			rfcInvoker.run();
			exitCode = rfcInvoker.getExitCode();

		} catch (IOException x) {
			x.printStackTrace();
		}
		
		System.exit(exitCode);
	}

	private static void usage() {
//		System.out.println(sResHash.getString("SAPR3_RFCFC_RfcXmlInvoker_USAGE"));
	}

	private static void displayCopyright() {
		System.out.println(COPYRIGHT);
		System.out.println(COMPONENT_NAME);
		System.out.println(VERSION_INFO);
		System.out.println();
	}
	
	/**
	 * @return Command-line exit code;
	 */
	int getExitCode() {
		return exitCode;
	}

}
