package com.ibm.di.test.utils;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Result;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.test.framework.perf.result.ResultSerializer;

/**
 * @author kaloyan.kolev
 * 
 */
public abstract class TestUtils {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String removeReturnCharacters(String src) {
		return src.replace("\r", "");
	}

	public static final void clearEntry(Entry entry) {
		entry.removeAllAttributes();

		String[] names = entry.getPropertyNames();

		for (String name : names) {
			entry.setProperty(name, null);
		}
	}

	public static final void compareLists(Object expected, Object actual) {
		Object[] exp, act;
		if (expected instanceof List<?>) {
			exp = new Object[((List) expected).size()];
			exp = ((List) expected).toArray(exp);
		} else if (expected instanceof Object[]) {
			exp = (Object[]) expected;
		} else {
			exp = new Object[] { expected };
		}

		if (actual instanceof List<?>) {
			act = new Object[((List) actual).size()];
			act = ((List) actual).toArray(exp);
		} else if (actual instanceof Object[]) {
			act = (Object[]) actual;
		} else {
			act = new Object[] { actual };
		}

		if (exp.length != act.length) {
			fail("Lists' size don't match! <" + exp.length + "> != <" + act.length + ">");
		}

		for (int i = 0; i < exp.length; i++) {
			if ((exp[i] != null && !exp[i].equals(act[i])) || (exp[i] == null && act[i] != null)) {
				fail("Lists' elements don't match! pos: " + i + " <" + exp[i] + "> != <" + act[i] + ">");
			}
		}
	}

	/**
	 * creates the temp directory
	 */
	public static File createTempDir() {
		File tempDir = new File("temp");

		if (!tempDir.exists() && !tempDir.mkdir()) {
			fail("Cannot create directory: " + tempDir.getAbsolutePath());
		}
		return tempDir;
	}

	public static URL getJarForClass(Class clazz) {
		ClassLoader cl = clazz.getClassLoader();
		String className = clazz.getName().replace('.', '/') + ".class";

		URL u = cl.getResource(className);

		return u != null && "jar".equals(u.getProtocol()) ? u : null;
	}

	public static String getClasspathForClass(Class clazz) {
		ClassLoader cl = clazz.getClassLoader();
		String result = null;
		String className = clazz.getName().replace('.', '/') + ".class";

		URL u = cl.getResource(className);

		if (u != null) {
			if ("jar".equals(u.getProtocol())) {
				String f = u.getFile();
				result = f.substring(0, f.lastIndexOf(className, f.length() - className.length()) - 2
				/*
				 * "!/" are used to separate the jar file and the class file.
				 */);
			} else if ("file".equals(u.getProtocol())) {
				String f = u.toString();
				result = f.substring(0, f.lastIndexOf(className, f.length() - className.length()));
			}

			if (result != null && result.startsWith("file:")) {
				result = result.substring(5 /* "file:".length() */);
			}
		}

		return result;
	}

	public static byte[] serializeObject(Object serializable) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;

		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(serializable);
			oos.flush();
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	public static Object deserializeObject(byte[] bytes) throws ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = null;
		Object result = null;

		try {
			ois = new ObjectInputStream(bis);
			result = ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
				}
			}
		}

		return result;
	}

	public static Entry createHierarchicalEntry(int complexity, String prefix) {
		Entry e = new Entry();
		Attribute a = null;
		Attribute sa = null;
		Attribute ssa = null;

		switch (complexity) {
		case 7:
			a = e.appendChild(e.createElement(prefix + "attr7"));
			for (int i = 1; i < 4; i++) {
				sa = (Attribute) a.appendChild(e.createElement("subattr7" + i));
				for (int k = 1; k < 4; k++) {
					ssa = (Attribute) sa.appendChild(e.createElement("ssattr"));
					sa.addValue("val7" + i + k);
					ssa.setAttribute("prop7" + i + k, "property7" + i + k);
				}
				a.addValue("val7" + i);
				sa.setAttribute("prop7" + i, "property7" + i);
			}
		case 6:
			a = e.appendChild(e.createElement(prefix + "attr6"));
			for (int i = 1; i < 4; i++) {
				sa = (Attribute) a.appendChild(e.createElement("subattr6" + i));
				for (int k = 1; k < 4; k++) {
					sa.appendChild(e.createElement("ssattr"));
				}
			}
		case 5:
			a = e.appendChild(e.createElement(prefix + "attr5"));
			for (int i = 1; i < 4; i++) {
				a.appendChild(e.createElement("subattr"));
				a.addValue("val5" + i);
				a.setAttribute("prop5" + i, "property5" + i);
			}
		case 4:
			a = e.appendChild(e.createElement(prefix + "attr4"));
			for (int i = 1; i < 4; i++) {
				a.appendChild(e.createElement("subattr4" + i));
				a.addValue("val4" + i);
				e.setProperty("prop" + i, "property" + i);
			}
		case 3:
			a = e.appendChild(e.createElement(prefix + "attr3"));
			for (int i = 1; i < 4; i++) {
				sa = (Attribute) a.appendChild(e.createElement("subattr3" + i));
			}
		case 2:
			a = e.appendChild(e.createElement(prefix + "attr2"));
			for (int i = 1; i < 4; i++) {
				a.addValue("val2" + i);
			}
		case 1:
			e.setAttribute(prefix + "attr1", "val11");
			e.setProperty("prop1", "property1");
		case 0:
			break;
		}
		return e;
	}

	public static Entry createFlatEntry(int complexity, String prefix) {
		Entry e = new Entry();
		Attribute a = null;

		switch (complexity) {
		case 5:
			a = new Attribute();
			for (int i = 1; i < 6; i++) {
				a.addValue("val." + 1);
			}
			e.setAttribute(prefix + "attr5", a, true);
		case 4:
			a = new Attribute();
			for (int i = 1; i < 6; i++) {
				a.addValue("val." + 1);
			}
			e.setAttribute(prefix + "attr.4", a);
		case 3:
			a = new Attribute(prefix + "attr3");
			for (int i = 1; i < 5; i++) {
				a.addValue("val." + 1);
			}
			e.setAttribute(a);
		case 2:
			a = new Attribute(prefix + "attr.2");
			for (int i = 1; i < 4; i++) {
				a.addValue("val" + 1);
			}
			e.setAttribute(a);
		case 1:
			e.setAttribute(prefix + "attr1", "val1");
			e.setProperty("prop1", "property1");
		case 0:
			break;
		}
		return e;
	}

	public static void outputResult(Result result, File simpleFile, File xmlFile) {
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
				int runCount = (result.getRunCount() - (result.getFailureCount() + result.getIgnoreCount()));
				if (runCount < 0)
					runCount = 0;
				out.print(runCount + " " + result.getRunCount() + " " + result.getFailureCount() + " " + result.getIgnoreCount()
						+ " " + (result.getRunTime() / 1000));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}
	}

	/**
	 * Deletes all files and sub-directories under <code>dir</code>. // Returns
	 * 
	 * @param dir
	 * @return <code>true</code> if all deletions were successful; If a deletion
	 *         fails, the method stops attempting to delete and returns
	 *         <code>false</code>.
	 */
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// Now the directory is empty so delete it
		return dir.delete();
	}

	/**
	 * This method takes the log and checks the given strings are present in it.
	 * 
	 * @param inpFile
	 *            file to check
	 * @param str
	 * @throws IOException
	 *             if log file could not be found
	 */
	public static void checkFileContains(File inpFile, String str) throws IOException {
		boolean found = false;

		FileInputStream fstream = new FileInputStream(inpFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;

		try {
			while ((strLine = br.readLine()) != null) {
				if (strLine.contains(str)) {
					found = true;
				}
			}
		} finally {
			br.close();
		}
		br.close();
		assertEquals(true, found);
	}

	/**
	 * This method takes the log and checks the given strings are present in it.
	 * 
	 * @param inpFile
	 *            file to check
	 * @param str
	 * @throws IOException
	 */
	public static void checkFileNotContains(File inpFile, String str) throws IOException {
		boolean found = false;

		FileInputStream fstream = new FileInputStream(inpFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;

		try {
			while ((strLine = br.readLine()) != null) {
				if (strLine.contains(str)) {
					found = true;
				}
			}
		} finally {
			br.close();
		}
		br.close();
		assertEquals(false, found);
	}

	/**
	 * @param scriptDir
	 *            directory containing the executable script
	 * @param scriptName
	 *            name of the executable script
	 * @return the proper command for executing a script; the extension of the
	 *         script depends on the platform - for Windows - '.bat' else '.sh'.
	 * @see ProcessRunner#ProcessRunner(String, File, List)
	 */
	public static List<String> getScriptExecCmd(String scriptDir, String scriptName) {
		List<String> result = new ArrayList<String>();
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
			result.add(new File(scriptDir, scriptName + ".bat").getAbsolutePath());
		} else {
			result.add(new File(scriptDir, scriptName + ".sh").getAbsolutePath());
		}
		return result;
	}
}
