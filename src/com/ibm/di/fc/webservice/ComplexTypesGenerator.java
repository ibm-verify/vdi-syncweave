/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Vector;

import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.fc.webservice.axis2.WSDL2Java;
import com.ibm.di.fc.webservice.axis2.WebServiceClient;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.util.FileUtils;
import com.ibm.icu.util.StringTokenizer;

/**
 * The Complex Types Generator Function Component is part of the TDI Web
 * Services suite. This Function Component is used for generating a JAR file,
 * which contains the Java class files implementing the complex data types
 * defined in a schema either internal to or referenced by a WSDL. This JAR file
 * can then be used by the other Web Service FCs in order to serialize and parse
 * SOAP messages containing these complex data types. Please note that this FC
 * is not supposed to be "run" as part of an AssemblyLine for example. Here is
 * the way this FC is supposed to be used:<br>
 * 1. Place it in an AssemblyLine <br>
 * 2. Fill in its parameters <br>
 * 3. Click the "Generate complex types" button to create the JAR file. <br>
 * After the desired JAR file has been created the FC can be either disabled or
 * deleted altogether from the AssemblyLine - the FC does not provide any
 * runtime functionality whatsoever.
 *
 */
public class ComplexTypesGenerator extends Function {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The extension of the java source files
	 */
	private static final String JAVA_SOURCE_FILES_EXTENSION = "java";

	/**
	 * The extension of the java class files
	 */
	private static final String JAVA_CLASS_FILES_EXTENSION = "class";

	/**
	 * The output directory for the java source files
	 */
	private static final String JAVA_FILES_OUTPUT_DIR = "temp/ComplexTypesJavaFiles";

	/**
	 * The output directory for the java class files
	 */
	private static final String CLASS_FILES_OUTPUT_DIR = "temp/ComplexTypesClassFiles";

	/**
	 * The system path separator
	 */
	private static final String PATH_SEPARATOR = System.getProperty("path.separator");

	/**
	 * This number must equal the number of arguments set by the
	 * setWsdlToJavaArgs() method. If you modify the the number of arguments set
	 * you must modify this number accordingly.
	 */
	private static final int WSDL2JAVA_BASE_ARGS_COUNT = 4;

	/**
	 * Resource Hash used to access TMS messages.
	 */
	private static ResourceHash sResHash = null;

	/**
	 * The name of the properties file
	 */
	private static final String PROPERTIES_FILE = "complextypesgeneratorfc";

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Generates complex java types by a given WSDL and options
	 *
	 * @param wsdlFile
	 *            The WSDL that defines the complex types
	 * @param aWsdlToJavaOptions
	 *            The options to the generator
	 * @throws Exception
	 */
	public void generateComplexTypesJavaFiles(String wsdlFile, String aWsdlToJavaOptions) throws Exception {
		createDir(JAVA_FILES_OUTPUT_DIR);

		String cmdParts[] = getCommandParts(wsdlFile, aWsdlToJavaOptions);

		for (int i = 0; i < cmdParts.length; i++) {
			logdebug(cmdParts[i]);
		}

		try {
			logmsg(sResHash.getString("FC.COMPLEXTYPESGEN.RUNNING.WSDL2JAVA"));

			WSDL2Java.generateFromArgs(cmdParts);
		} catch (Exception e) {
			String funcmsg = sResHash.getString("FC.COMPLEXTYPESGEN.COULD.NOT.GENERATE.JAVA.SOURCE.FILES", e.toString());
			logerror(funcmsg, new Exception(funcmsg));
			throw new Exception(funcmsg);
		}
		logmsg(sResHash.getString("FC.COMPLEXTYPESGEN.DONE"));
	}

	/**
	 * Returns the command needed to generate the java complex types from the
	 * given WSDL
	 *
	 * @param aWsdlFile
	 *            The WSDL that defines the complex types
	 * @param aOptions
	 *            The options for the generator
	 * @return The command split in a String[] array
	 * @throws Exception
	 */
	private String[] getCommandParts(String aWsdlFile, String aOptions) throws Exception {
		String strArray[] = null;
		if (aOptions == null) {
			strArray = new String[WSDL2JAVA_BASE_ARGS_COUNT];
			return setWsdlToJavaArgs(strArray, 0, aWsdlFile);
		}

		StringTokenizer st = new StringTokenizer(aOptions);
		int optionCount = st.countTokens();
		strArray = new String[optionCount + WSDL2JAVA_BASE_ARGS_COUNT];
		int index = 0;
		while (st.hasMoreTokens()) {
			strArray[index++] = st.nextToken();
		}

		strArray = setWsdlToJavaArgs(strArray, optionCount, aWsdlFile);

		return strArray;
	}

	/**
	 * Sets the given array with "-W", "-o", the output directory for the java
	 * source files and the given WSDL
	 *
	 * @param argsArray
	 *            The array in which the options will be set
	 * @param aStartIndex
	 *            The index of the first element in the array that has to be set
	 * @param aWsdlFile
	 *            The WSDL that defines the complex types
	 * @return String[] object
	 */
	// If you modify the number of arguments set by this method you must also
	// modify
	// the WSDL2JAVA_BASE_ARGS_COUNT number accordingly.
	private String[] setWsdlToJavaArgs(String[] argsArray, int aStartIndex, String aWsdlFile) {
		argsArray[aStartIndex++] = "-W";
		argsArray[aStartIndex++] = "-o";
		argsArray[aStartIndex++] = JAVA_FILES_OUTPUT_DIR;
		argsArray[aStartIndex++] = aWsdlFile;

		return argsArray;
	}

	/**
	 * Compiles and executes the java command which generates the complex java
	 * types
	 *
	 * @param aJavaCompilerExecutablePath
	 *            The Java Home Path
	 * @throws Exception
	 */
	public void compile(String aJavaCompilerExecutablePath) throws Exception {
		createDir(CLASS_FILES_OUTPUT_DIR);
		logmsg(sResHash.getString("FC.COMPLEXTYPESGEN.COMPILING.JAVA.FILES"));
		String tdiInstallDir = System.getProperty("IDILoader.jars");

		// Use Axis2 jars for compilation
		String classPath = "." + PATH_SEPARATOR + tdiInstallDir + "/jars/3rdparty/IBM/axis2/*.jar" + PATH_SEPARATOR + tdiInstallDir
				+ "/jars/3rdparty/IBM/wsdl4j.jar" + PATH_SEPARATOR + tdiInstallDir + "/jars/3rdparty/others/jaxrpc.jar";

		Vector<String> srcFileNames = recurseDirs(JAVA_FILES_OUTPUT_DIR, JAVA_SOURCE_FILES_EXTENSION);

		File srcFiles = new File(JAVA_FILES_OUTPUT_DIR, "srcFiles");
		if (!srcFiles.exists() && !srcFiles.createNewFile()) {
			throw new RuntimeException("unable to create file");
		}

		PrintWriter srcFilesOut = new PrintWriter(srcFiles);

		for (String fileName : srcFileNames) {
			srcFilesOut.println(fileName);
		}

		srcFilesOut.close();
		int initialArgs = 9;

		String[] compileArgs = new String[initialArgs];
		compileArgs[0] = aJavaCompilerExecutablePath;
		compileArgs[1] = "-classpath";
		compileArgs[2] = classPath;
		compileArgs[3] = "-source";
		compileArgs[4] = "1.6";  // Updated to Java 1.6 for Axis2 compatibility
		compileArgs[5] = "-Xlint:-options";
		compileArgs[6] = "-d";
		compileArgs[7] = CLASS_FILES_OUTPUT_DIR;
		compileArgs[8] = "@" + srcFiles.getPath();

		for (int i = 0; i < compileArgs.length; i++) {
			logdebug(compileArgs[i]);
		}
		Process process = Runtime.getRuntime().exec(compileArgs);
		InputStream err = process.getErrorStream();
		String errMsg = readInputStream(err);
		if (!"".equals(errMsg)) {
			String funcmsg = sResHash.getString("FC.COMPLEXTYPESGEN.COULD.NOT.COMPILE.GENERATED.JAVA.SOURCE.FILES", errMsg);
			logerror(funcmsg, new Exception(funcmsg));
			throw new Exception(errMsg);
		}

		logmsg("Finished compiling java files.");
	}

	/**
	 * Returns a Vector which contains the file names of all files with the
	 * given extension in the given directory and its subdirectories.
	 *
	 * @param aRootDirName
	 *            The directory to recurse into.
	 * @param aFilesExtension
	 *            The extension of the files.
	 * @return A Vector with the file names found.
	 */
	private Vector<String> recurseDirs(String aRootDirName, String aFilesExtension) {
		Vector<String> fileNames = new Vector<String>();

		File rootDir = new File(aRootDirName);
		if (rootDir.isDirectory()) {
			File[] files = rootDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isFile()) {
					if (file.getName().endsWith("." + aFilesExtension)) {
						fileNames.add(file.getPath());
					}
				} else if (file.isDirectory()) {
					Vector<String> children = recurseDirs(file.getPath(), aFilesExtension);
					fileNames.addAll(children);
				}
			}
		} else if (rootDir.isFile()) {
			fileNames.add(aRootDirName);
		}

		return fileNames;
	}

	/**
	 * Executes a command which creates a JAR archive from the generated types.
	 *
	 * @param aJarExecutablePath
	 *            The path to the "jar" command.
	 * @param aJarFileName
	 *            The name of the JAR you want to create.
	 * @throws Exception
	 */
	private void createJar(String aJarExecutablePath, String aJarFileName)
	throws Exception {

		logmsg(sResHash.getString("FC.COMPLEXTYPESGEN.CREATING.JAR.FILE"));

		int jarArgsLen = 6;
		String[] jarArgs = new String[jarArgsLen];
		jarArgs[0] = aJarExecutablePath;
		jarArgs[1] = "cMf";
		jarArgs[2] = aJarFileName;
		jarArgs[3] = "-C";
		jarArgs[4] = CLASS_FILES_OUTPUT_DIR;
		jarArgs[5] = ".";

		for (int i = 0; i < jarArgs.length; i++) {
			logdebug(jarArgs[i]);
		}

		Process process = Runtime.getRuntime().exec(jarArgs);

		InputStream err = process.getErrorStream();
		String errMsg = readInputStream(err);
		if (!"".equals(errMsg)) {
			String funcmsg = sResHash.getString("FC.COMPLEXTYPESGEN.COULD.NOT.GENERATE.JAR.FILE", errMsg);
			logerror(funcmsg, new Exception(funcmsg));
			throw new Exception(errMsg);
		}

		logmsg("Finished creating jar file.");
	}

	/**
	 * Reads from the given input stream until the end of the stream is reached
	 * and returns the read data.
	 *
	 * @param aInputStream
	 *            The input stream.
	 * @return The read data.
	 * @throws java.io.IOException
	 */
	public static String readInputStream(java.io.InputStream aInputStream) throws java.io.IOException {
		byte[] buffer = new byte[WebServiceClient.INITIAL_BUFFER_SIZE];
		int idx = 0;

		int b = aInputStream.read();
		while (b != -1) {
			while ((b != -1) && (idx < buffer.length)) {
				buffer[idx++] = (byte) b;
				b = aInputStream.read();
			}
			if (b != -1) {
				buffer = WebServiceClient.resizeByteArray(buffer, buffer.length + WebServiceClient.INITIAL_BUFFER_SIZE);
			}
		}
		buffer = WebServiceClient.resizeByteArray(buffer, idx);
		String str = new String(buffer);
		return str;
	}

	/**
	 * Deletes the generated source files.
	 */
	private void purgeJavaFiles() {
		logdebug(sResHash.getString("FC.COMPLEXTYPESGEN.PURGING.JAVA.FILES"));
		deleteRecursive(JAVA_FILES_OUTPUT_DIR);
	}

	/**
	 * Deletes the generated class files.
	 */
	private void purgeClassFiles() {
		logdebug(sResHash.getString("FC.COMPLEXTYPESGEN.PURGING.CLASS.FILES"));
		deleteRecursive(CLASS_FILES_OUTPUT_DIR);
	}

	/**
	 * Deletes the generated JAR file.
	 *
	 * @param aJarFileName
	 *            The name of the generated JAR file.
	 */
	private void deleteJar(String aJarFileName) {
		logdebug(sResHash.getString("FC.COMPLEXTYPESGEN.DELETING.JAR.FILE"));

		File jarFileName = new File(aJarFileName);
		if (jarFileName.exists() && !jarFileName.isDirectory()) {
			FileUtils.delete(jarFileName, logger);
		}
	}

	/**
	 * Deletes all files in the given directory and all of its subdirectories.
	 *
	 * @param aRootDirName
	 *            The name of the directory you want to empty
	 */
	private void deleteRecursive(String aRootDirName) {
		File rootDir = new File(aRootDirName);
		if (rootDir.isDirectory()) {
			File[] files = rootDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isFile()) {
					logdebug(sResHash.getString("FC.COMPLEXTYPESGEN.DELETING.FILE", file.getAbsolutePath()));
					FileUtils.delete(file, logger);
				} else if (file.isDirectory()) {
					deleteRecursive(file.getPath());
					FileUtils.delete(file, logger);
				}
			}
		} else if (rootDir.isFile()) {
			FileUtils.delete(rootDir, logger);
		}
	}

	/**
	 * Creates a directory by a given name.
	 *
	 * @param aDirName
	 *            The name of the directory.
	 */
	private void createDir(String aDirName) {
		File dir = new File(aDirName);
		if (!dir.exists()) {
			logmsg(sResHash.getString("FC.COMPLEXTYPESGEN.CREATING.DIRECTORY", aDirName));
			FileUtils.mkdirs(dir, logger);
		}
	}

	/**
	 * Does nothing in this class
	 *
	 * @param obj
	 * @return Entry object
	 * @throws Exception
	 */
	public Object perform(Object obj) throws Exception {
		return new Entry();
	}

	/**
	 * Version information.
	 *
	 * @return version information
	 */
	public String getVersion() {
		return "3.0-di7.1.1 1.29 2025/10/08";
	}

	/**
	 * Deletes all previous class files, generates new ones and archives them
	 * into a JAR file.
	 *
	 * @param wsdlUrl
	 *            The URL to the WSDL file
	 * @param aWsdlToJavaOptions
	 *            Any options you want to pass to the generator
	 * @param aJavaCompilerExecutablePath
	 *            Path to JDK
	 * @param aJarExecutablePath
	 *            Path to JDK
	 * @param aJarFileName
	 *            The name of the JAR you want to get in the end of the
	 *            operation
	 * @param aAutoGenJavaSrcFiles
	 *            If true, the program will generate Java source files for the
	 *            generated complex types
	 * @throws Exception
	 */
	private void run(String wsdlUrl, String aWsdlToJavaOptions, String aJavaCompilerExecutablePath, String aJarExecutablePath,
			String aJarFileName, boolean aAutoGenJavaSrcFiles) throws Exception {
		if (aAutoGenJavaSrcFiles) {
			purgeJavaFiles();
			generateComplexTypesJavaFiles(wsdlUrl, aWsdlToJavaOptions);
		}

		purgeClassFiles();
		compile(aJavaCompilerExecutablePath);

		deleteJar(aJarFileName);
		createJar(aJarExecutablePath, aJarFileName);
	}

	/**
	 * Executes the main logic in this class which generates the complex java
	 * types and saves them in a JAR archive.
	 *
	 * @param aWsdlUrl
	 *            The URL to the WSDL file
	 * @param aWsdlToJavaOptions
	 *            Any options you want to pass to the generator
	 * @param aJDKPath
	 *            Path to JDK
	 * @param aJarFileName
	 *            The name of the JAR you want to get in the end of the
	 *            operation
	 * @param aAutoGenJavaSrcFiles
	 *            If true, the program will generate Java source files for the
	 *            generated complex types
	 * @throws Exception
	 */
	public static void generateComplexTypes(String aWsdlUrl, String aWsdlToJavaOptions, String aJDKPath, String aJarFileName,
			boolean aAutoGenJavaSrcFiles) throws Exception {
		ComplexTypesGenerator generator = new ComplexTypesGenerator();
		String javaCompilerExecutablePath = "javac";
		String jarExecutablePath = "jar";
		if (aJDKPath != null && !aJDKPath.equals("")) {
			javaCompilerExecutablePath = aJDKPath + "/bin/javac";
			jarExecutablePath = aJDKPath + "/bin/jar";
		}

		generator.run(aWsdlUrl, aWsdlToJavaOptions, javaCompilerExecutablePath, jarExecutablePath, aJarFileName,
				aAutoGenJavaSrcFiles);
	}
	
	/**
	 * Helper method for logging debug messages
	 * 
	 * @param message The message to log
	 */
	private void logdebug(String message) {
		if (logger != null) {
			logger.logdebug(message);
		}
	}
	
	/**
	 * Helper method for logging error messages
	 * 
	 * @param message The message to log
	 * @param e The exception
	 */
	private void logerror(String message, Exception e) {
		if (logger != null) {
			logger.logerror(message, e);
		}
	}
	
	/**
	 * Helper method for logging messages
	 * 
	 * @param message The message to log
	 */
	 public void logmsg(String message) {
        if (logger != null) {
            logger.loginfo(message);
        }
    }
}
