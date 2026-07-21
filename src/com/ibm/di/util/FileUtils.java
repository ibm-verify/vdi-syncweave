/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;

/**
 * Utility class used for file manipulation operations.
 */
public class FileUtils {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Loads the specified file and returns its content as String.
	 * 
	 * @param file
	 *            the path to the file to load.
	 * @return the content of the file.
	 * @throws Exception
	 *             if an I/O error occurs.
	 */
	public static String loadFile(String file) throws Exception {
		return loadFile(new File(file));
	}

	/**
	 * Loads the specified file and returns its content as String. The default
	 * platform character encoding will be used for this operation.
	 * 
	 * @param file
	 *            the file to load.
	 * @return the content of the file.
	 * @throws Exception
	 *             if an I/O error occurs.
	 */
	public static String loadFile(File file) throws Exception {
		return loadFile(file, null);
	}

	/**
	 * Loads the specified file and returns its content as String. The specified
	 * character encoding will be used for this operation.
	 * 
	 * @param file
	 *            the file to load.
	 * @param encoding
	 *            the name of the encoding to use, if this is null the platform
	 *            default encoding will be used.
	 * @return the content of the file.
	 * @throws Exception
	 *             if an I/O error occurs.
	 */
	public static String loadFile(File file, String encoding) throws Exception {
		FileInputStream fis = new FileInputStream(file);

		ByteArrayOutputStream buffer = readAsByteArrayStream(fis);

		fis.close();

		if (encoding != null)
			return buffer.toString(encoding);

		return buffer.toString();
	}

	/**
	 * Reads the provided input stream and returns it as an array of bytes. This
	 * method does not invoke the close() method on the provided
	 * {@link InputStream} object.
	 * 
	 * @param is
	 *            the input stream to read
	 * @return the bites from the input stream.
	 * @throws IOException
	 *             if reading error occurs.
	 */
	public static byte[] readInputStream(InputStream is) throws IOException {
		return readAsByteArrayStream(is).toByteArray();
	}

	private static ByteArrayOutputStream readAsByteArrayStream(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] temp = new byte[1024];
		int count = 0;
		while ((count = is.read(temp)) != -1) {
			buffer.write(temp, 0, count);
		}

		return buffer;
	}

	/**
	 * Simply calls the delete() method of the provided file object.
	 * Additionally the returned value is checked and logged if the file could
	 * not be deleted.
	 * 
	 * @param file
	 *            the file to delete
	 * @param log
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null or the specified file
	 *             does not exists.
	 * @throws SecurityException
	 *             if the file could not be deleted.
	 */
	public static void delete(File file, Log log) {
		if (file == null || !file.exists())
			throw new IllegalArgumentException();

		if (!file.delete() && log != null)
			log.logwarn(ResourceHash.getHash("miserver").getString("FILE.UTILS.UNABLE.TO.DELETE.FILE", file.getAbsolutePath()));
	}

	/**
	 * Simply calls the delete() method of the provided file object.
	 * Additionally the returned value is checked and logged if the file could
	 * not be deleted.
	 * 
	 * @param path
	 *            the file to delete
	 * @param log
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null.
	 * @throws SecurityException
	 *             if the file could not be deleted.
	 * @throws NullPointerException
	 *             if the provided path argument is null.
	 */
	public static void delete(String path, Log log) {
		delete(new File(path), log);
	}

	/**
	 * Simply calls the mkdirs() method of the provided file object.
	 * Additionally the returned value is checked and logged if the directories
	 * could not be created.
	 * 
	 * @param file
	 *            the directory to create
	 * @param log
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null
	 * @throws SecurityException
	 *             if the directories could not be created
	 */
	public static void mkdirs(File file, Log log) {
		if (file == null)
			throw new IllegalArgumentException();

		if (!file.mkdirs() && log != null)
			log.logwarn(ResourceHash.getHash("miserver").getString("FILE.UTILS.UNABLE.TO.CREATE.DIRS", file.getAbsolutePath()));
	}

	/**
	 * Simply calls the mkdirs() method of the provided file object.
	 * Additionally the returned value is checked and logged if the directories
	 * could not be created.
	 * 
	 * @param path
	 *            the directory to create
	 * @param log
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null.
	 * @throws SecurityException
	 *             if the directories could not be created
	 * @throws NullPointerException
	 *             if the provided path argument is null.
	 */
	public static void mkdirs(String path, Log log) {
		mkdirs(new File(path), log);
	}

	/**
	 * Simply calls the mkdir() method of the provided file object. Additionally
	 * the returned value is checked and logged if the directory could not be
	 * created.
	 * 
	 * @param file
	 *            the directory to create
	 * @param log
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null
	 * @throws SecurityException
	 *             if the directories could not be created
	 */
	public static void mkdir(File file, Log log) {
		if (file == null)
			throw new IllegalArgumentException();

		if (!file.mkdir() && log != null)
			log.logwarn(ResourceHash.getHash("miserver").getString("FILE.UTILS.UNABLE.TO.CREATE.DIRS", file.getAbsolutePath()));
	}

	/**
	 * Simply calls the mkdir() method of the provided file object. Additionally
	 * the returned value is checked and logged if the directories could not be
	 * created.
	 * 
	 * @param path
	 *            the directory to create
	 * @param log
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null.
	 * @throws SecurityException
	 *             if the directory could not be created
	 * @throws NullPointerException
	 *             if the provided path argument is null.
	 */
	public static void mkdir(String path, Log log) {
		mkdir(new File(path), log);
	}

	/**
	 * Simply calls the renameTo() method of the provided file object.
	 * Additionally the returned value is checked and logged if the file could
	 * not be renamed.
	 * 
	 * @param from
	 *            the file to rename
	 * @param to
	 *            the name of the file to set
	 * @param log
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null or the specified file
	 *             does not renamed.
	 * @throws SecurityException
	 *             if the file could not be deleted.
	 */
	public static void renameTo(File from, File to, Log log) {
		if (from == null || !from.exists())
			throw new IllegalArgumentException();

		if (!from.renameTo(to) && log != null)
			log.logwarn(ResourceHash.getHash("miserver").getString("FILE.UTILS.UNABLE.TO.RENAME.FILE", from.getAbsolutePath()));
	}

	/**
	 * Simply calls the renameTo() method of the provided file object.
	 * Additionally the returned value is checked and logged if the file could
	 * not be renamed.
	 * 
	 * @param from
	 *            the file to rename
	 * @param to
	 *            the name of the file to set
	 * @param log
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null.
	 * @throws SecurityException
	 *             if the file could not be renamed.
	 * @throws NullPointerException
	 *             if the provided path argument is null.
	 */
	public static void renameTo(String from, String to, Log log) {
		renameTo(new File(from), new File(to), log);
	}

	/**
	 * Simply calls the delete() method of the provided file object.
	 * Additionally the returned value is checked and an exception is thrown if
	 * the file could not be deleted.
	 * 
	 * @param file
	 *            the file to delete
	 * @throws Exception
	 *             if the file cannot be deleted.
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null or the specified file
	 *             does not exists.
	 * @throws SecurityException
	 *             if the file could not be deleted.
	 */
	public static void delete(File file) throws Exception {
		if (file == null || !file.exists())
			throw new IllegalArgumentException();

		if (!file.delete())
			throw new Exception(ResourceHash.getHash("miserver").getString("FILE.UTILS.UNABLE.TO.DELETE.FILE",
					file.getAbsolutePath()));
	}

	/**
	 * Deletes the provided {@link File}. If the {@link File} denotes a
	 * directory it will be deleted recursively whit all of the files it
	 * contains. Additionally the returned value is checked and an exception is
	 * thrown if the file could not be deleted.
	 * 
	 * @param file
	 *            the file to delete
	 * @throws Exception
	 *             if the file cannot be deleted.
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null or the specified file
	 *             does not exists.
	 * @throws SecurityException
	 *             if the file could not be deleted.
	 */
	public static void deleteRecursively(File toDelete) throws Exception {
		if (toDelete == null) {
			return;
		}

		if (toDelete.exists()) {
			if (toDelete.isDirectory()) {
				for (File child : toDelete.listFiles()) {
					deleteRecursively(child);
				}
			}
			delete(toDelete);
		} else {
			throw new FileNotFoundException(toDelete.getAbsolutePath());
		}
	}

	/**
	 * Convenience method that delegates to {@link #deleteRecursively(File)}.
	 * 
	 * @param toDelete
	 *            the file to delete
	 * @throws Exception
	 *             if the file cannot be deleted.
	 */
	public static void deleteRecursively(String toDelete) throws Exception {
		deleteRecursively(new File(toDelete));
	}

	/**
	 * Simply calls the delete() method of the provided file object.
	 * Additionally the returned value is checked and an exception is thrown if
	 * the file could not be deleted.
	 * 
	 * @param path
	 *            the file to delete
	 * @throws Exception
	 *             if the file could not be deleted.
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null.
	 * @throws SecurityException
	 *             if the file could not be deleted.
	 * @throws NullPointerException
	 *             if the provided path argument is null.
	 */
	public static void delete(String path) throws Exception {
		delete(new File(path));
	}

	/**
	 * Simply calls the mkdirs() method of the provided file object.
	 * Additionally the returned value is checked and an exception is thrown if
	 * the directories could not be created.
	 * 
	 * @param file
	 *            the directory to create
	 * @throws Exception
	 *             if the directories could not be created.
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null
	 * @throws SecurityException
	 *             if the directories could not be created
	 */
	public static void mkdirs(File file) throws Exception {
		if (file == null)
			throw new IllegalArgumentException();

		if (!file.mkdirs() && !file.exists())
			// throw only if the directory was not successfully created.
			throw new Exception(ResourceHash.getHash("miserver").getString("FILE.UTILS.UNABLE.TO.CREATE.DIRS",
					file.getAbsolutePath()));
	}

	/**
	 * Simply calls the mkdirs() method of the provided file object.
	 * Additionally the returned value is checked and an exception is thrown if
	 * the directories could not be created.
	 * 
	 * @param path
	 *            the directory to create
	 * @throws Exception
	 *             if the directories could not be created.
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null.
	 * @throws SecurityException
	 *             if the directories could not be created
	 * @throws NullPointerException
	 *             if the provided path argument is null.
	 */
	public static void mkdirs(String path) throws Exception {
		mkdirs(new File(path));
	}

	/**
	 * Simply calls the mkdir() method of the provided file object. Additionally
	 * the returned value is checked and an exception is thrown if the directory
	 * could not be created.
	 * 
	 * @param file
	 *            the directory to create
	 * @throws Exception
	 *             if the directory could not be created.
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null
	 * @throws SecurityException
	 *             if the directories could not be created
	 */
	public static void mkdir(File file) throws Exception {
		if (file == null)
			throw new IllegalArgumentException();

		if (!file.mkdir())
			throw new Exception(ResourceHash.getHash("miserver").getString("FILE.UTILS.UNABLE.TO.CREATE.DIRS",
					file.getAbsolutePath()));
	}

	/**
	 * Simply calls the mkdir() method of the provided file object. Additionally
	 * the returned value is checked and an exception is thrown if the
	 * directories could not be created.
	 * 
	 * @param path
	 *            the directory to create
	 * @throws Exception
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null.
	 * @throws SecurityException
	 *             if the directory could not be created
	 * @throws NullPointerException
	 *             if the provided path argument is null.
	 */
	public static void mkdir(String path) throws Exception {
		mkdir(new File(path));
	}

	/**
	 * Simply calls the renameTo() method of the provided file object.
	 * Additionally the returned value is checked and an exception is thrown if
	 * the file could not be renamed.
	 * 
	 * @param from
	 *            the file to rename
	 * @param to
	 *            the name of the file to set
	 * @throws Exception
	 *             if the file could not be renamed.
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null or the specified file
	 *             does not renamed.
	 * @throws SecurityException
	 *             if the file could not be renamed.
	 */
	public static void renameTo(File from, File to) throws Exception {
		if (from == null || !from.exists())
			throw new IllegalArgumentException();

		if (!from.renameTo(to))
			throw new Exception(ResourceHash.getHash("miserver").getString("FILE.UTILS.UNABLE.TO.RENAME.FILE",
					from.getAbsolutePath()));
	}

	/**
	 * Simply calls the renameTo() method of the provided file object.
	 * Additionally the returned value is checked and an exception is thrown if
	 * the file could not be renamed.
	 * 
	 * @param from
	 *            the file to rename
	 * @param to
	 *            the name of the file to set
	 * @throws Exception
	 *             if the file could not be renamed.
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null.
	 * @throws SecurityException
	 *             if the file could not be renamed.
	 * @throws NullPointerException
	 *             if the provided path argument is null.
	 */
	public static void renameTo(String from, String to) throws Exception {
		renameTo(new File(from), new File(to));
	}

	/**
	 * Copy file. This method copies fromPath to toPath. The overwrite flag
	 * specifies whether the destination file should be overwritten. This method
	 * is called by
	 * <code>com.ibm.di.function.UserFunctions.copyFile(String, String, boolean)</code>
	 * method.
	 * 
	 * @param fromPath
	 *            The source file
	 * @param toPath
	 *            The destination file
	 * @param overwrite
	 *            Specify true if destination should be overwritten.
	 * @return true if file was copied, false if toPath exists and
	 *         overwrite=false.
	 * @throws Exception
	 */
	public static boolean copyFile(String fromPath, String toPath, boolean overwrite) throws Exception {
		File fp = new File(fromPath);
		File tp = new File(toPath);

		return copyFile(fp, tp, overwrite);
	}

	/**
	 * Copy file. This method copies fromPath to toPath. The overwrite flag
	 * specifies whether the destination file should be overwritten. This method
	 * is called by
	 * <code>com.ibm.di.function.UserFunctions.copyFile(File, File, boolean)</code>
	 * method.
	 * <p>
	 * 
	 * @param fromFile
	 *            The source file
	 * @param toFile
	 *            The destination file
	 * @param overwrite
	 *            Specify true if destination should be overwritten.
	 * @return true if file was copied, false if toPath exists and
	 *         overwrite=false.
	 * @throws Exception
	 */
	public static boolean copyFile(File fromFile, File toFile, boolean overwrite) throws Exception {
		if (toFile == null || fromFile == null || (!overwrite && toFile.exists())) {
			return false;
		}

		File parentFile = toFile.getParentFile();
		if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
			throw new Exception(ResourceHash.getHash("miserver").getString("FILE.UTILS.UNABLE.TO.CREATE.DIRS",
					parentFile.getAbsolutePath()));
		}

		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(fromFile);
			fos = new FileOutputStream(toFile);
			byte[] buf = new byte[1024];
			int n;

			while ((n = fis.read(buf)) != -1) {
				fos.write(buf, 0, n);
			}
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			if (fos != null) {
				fos.close();
			}
		}

		return true;
	}

	/**
	 * Copies the provided {@link File}. The directory will be copied
	 * recursively whit all of the files it contains.
	 * 
	 * @param fromDir
	 *            the directory to copy from
	 * @param toDir
	 *            the directory to copy to (created if missing)
	 * @param overwrite
	 *            if <code>toDir</code> exists all of its conflicting children
	 *            will be overwritten.
	 * @throws Exception
	 *             if a directory cannot be created.
	 * 
	 * @throws IllegalArgumentException
	 *             - if either of the parameters is null or the specified file
	 *             does not exists.
	 * @throws SecurityException
	 *             if the file could not be deleted.
	 */
	public static void copyDir(File fromDir, File toDir, boolean overwrite) throws Exception {
		if (fromDir == null || toDir == null) {
			return;
		}

		if (fromDir.exists()) {
			if (!toDir.exists() && !toDir.mkdirs()) {
				throw new IOException(ResourceHash.getHash("miserver").getString("FILE.UTILS.UNABLE.TO.CREATE.DIRS",
						toDir.getAbsolutePath()));
			}

			if (fromDir.isDirectory()) {
				for (File child : fromDir.listFiles()) {
					if (child.isDirectory()) {
						copyDir(child, new File(toDir, child.getName()), overwrite);
					} else if (child.isFile()) {
						copyFile(child, new File(toDir, child.getName()), overwrite);
					}
				}
			}
		} else {
			throw new FileNotFoundException(fromDir.getAbsolutePath());
		}
	}
}
