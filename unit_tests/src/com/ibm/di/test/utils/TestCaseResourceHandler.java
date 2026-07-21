package com.ibm.di.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.ibm.di.test.CVTComponent;

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
public class TestCaseResourceHandler {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private File sourceDir = null;
	private File targetDir = null;

	private File tdiInstallDir = null;
	private File tdiSolutionDir = null;
	private String componentName = null;

	private LinkedList<File> iterateDirList = null;
	private List<File> filesToDelete = null;
	private FileRecorder fr = null;
	private String sep = null;

	public static final String TEST_CASE = "TestCases";
	public static final String UNIT_TEST = "unit_tests";
	public static final String RESOURCES = "resources";

	public TestCaseResourceHandler(File instDir, File solDir, String componentName) throws Exception {
		this.sep = File.separator;
		this.iterateDirList = new LinkedList<File>();
		this.filesToDelete = new ArrayList<File>();

		this.tdiInstallDir = instDir;
		this.tdiSolutionDir = solDir;
		this.componentName = componentName;
	}

	public TestCaseResourceHandler(File instDir, File solDir, Class<?> testClass) throws Exception {
		this(instDir, solDir, testClass.getAnnotation(CVTComponent.class).name());
	}

	/**
	 * Check if TestCase in TDI Installation directory exist in TDI Solution
	 * directory. If they do not exist, copy them into TDI Solution directory.
	 * If they exist, check if they are equal? If they are not equal, backup the
	 * files from TDI Solution directory and replace them with the one from TDI
	 * Installation directory. If they exist and are equal do nothing.
	 * 
	 * @throws Exception
	 */
	public void initResource() throws Exception {
		String testComponentDir = TEST_CASE + sep + componentName;
		File resourcesDir = new File(tdiInstallDir, UNIT_TEST + sep + RESOURCES);
		this.sourceDir = new File(resourcesDir, testComponentDir);
		this.targetDir = new File(tdiSolutionDir, testComponentDir);

		if (!sourceDir.exists() || !sourceDir.isDirectory()) {
			throw new FileNotFoundException("Source directory " + sourceDir.getAbsolutePath() + " does not exist!");
		}

		addSubDir(sourceDir);

		File srcFile = null;
		File trgtFile = null;
		File backupDir = new File(targetDir, "BackUp");
		fr = new FileRecorder(backupDir);

		while ((srcFile = getNextFile()) != null) {
			trgtFile = new File(targetDir, srcFile.getPath().substring(sourceDir.getPath().length()));
			if (trgtFile.exists() && trgtFile.isFile()) {
				boolean noEqual = !contentEquals(srcFile, trgtFile);
				if (noEqual) {
					fr.recordModifyFile(trgtFile);
					filesToDelete.add(trgtFile);
					org.apache.commons.io.FileUtils.copyFile(srcFile, trgtFile);
				}
			} else {
				filesToDelete.add(trgtFile);
				org.apache.commons.io.FileUtils.copyFile(srcFile, trgtFile);
			}
		}
	}

	/**
	 * Delete previous copied files and restore if some file is backup before.
	 * 
	 * @throws Exception
	 */
	public void restoreResources() throws Exception {
		for (File fileToDelete : filesToDelete) {
			fileToDelete.delete();
			File containingFolder = new File(fileToDelete.getParent());
			if (containingFolder.exists() && containingFolder.isDirectory()) {
				File[] content = containingFolder.listFiles();
				if (content != null && content.length == 0) {
					containingFolder.delete();
				}
			}
		}
		fr.destroy();
	}

	/**
	 * 
	 * @param testCaseName
	 *            Name of the Test Case
	 * @return String path to XML configuration with quotes
	 * @throws Exception
	 */
	public String getConfigurationXML(String testCaseName) throws Exception {
		return getConfigurationXML(testCaseName, testCaseName);
	}

	/**
	 * 
	 * @param testCaseName
	 *            Name of the Test Case
	 * @param testCaseDir
	 *            Name of the Test Case directory
	 * @return String path to XML configuration with quotes
	 * @throws Exception
	 */
	public String getConfigurationXML(String testCaseName, String testCaseDir) throws Exception {
		String configFileName = null;
		File configFile = new File(tdiSolutionDir, sep + TEST_CASE + sep + componentName + sep + testCaseDir + sep + testCaseName
				+ ".xml");
		if (configFile.exists()) {
			configFileName = configFile.getCanonicalPath();
		} else {
			throw new FileNotFoundException("Configuration file " + configFile.getAbsolutePath() + " does not exist!");
		}
		return "\"" + configFileName + "\"";
	}

	private void addSubDir(File subDir) {
		if (subDir.isDirectory()) {
			File[] fileArray = subDir.listFiles();

			if (fileArray != null && fileArray.length > 0) {
				iterateDirList.addAll(Arrays.asList(fileArray));
			}
			if (fileArray == null) {
				System.out.println("Security violation!");
			}
		}
	}

	private File getNextFile() throws Exception {
		File file = null;
		if (iterateDirList.size() > 0) {
			file = iterateDirList.poll();
			if (file.isDirectory()) {
				addSubDir(file);
				return getNextFile();
			}
		}
		return file;
	}

	private boolean contentEquals(File srcFile, File trgtFile) throws Exception {
		boolean equal = false;
		if (srcFile.length() != trgtFile.length()) {
			equal = false;
		}
		InputStream srcStream = null;
		InputStream trgtStream = null;
		try {
			srcStream = new FileInputStream(srcFile);
			trgtStream = new FileInputStream(trgtFile);
			equal = org.apache.commons.io.IOUtils.contentEquals(srcStream, trgtStream);
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(srcStream);
			org.apache.commons.io.IOUtils.closeQuietly(trgtStream);
		}
		return equal;
	}
}
