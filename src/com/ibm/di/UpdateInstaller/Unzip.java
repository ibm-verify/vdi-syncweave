/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.*;

/**
 * Provides several static utilities for use when unzipping files from an
 * archive.
 *
 * @author Alan Watkins
 *
 */
public class Unzip {
	/**
	 * The copyright notice for binary java code required by legal.
	 */
	private static final String COPYRIGHT = com.ibm.di.UpdateInstaller.FixUtils.OBJECT_CODE;

	/**
	 * Default extraction location. Files are extracted to the current directory
	 * by default.
	 */
	private static String defXLoc = ".";

	private static List<String> newFiles = new ArrayList<String>();
	
	/**
	 * Sets the extraction location.
	 *
	 * @param loc
	 *            The location to extract files
	 */
	public static void setDefaultExtractionLocation(String loc) {
		defXLoc = loc;
	}

	/**
	 * Gets the default extraction location.
	 *
	 * @return The default location to extract files
	 */
	public static String getDefaultExtractionLocation() {
		return defXLoc;
	}

	/**
	 * Indicates whether or not we should use the special CE algorithm to backup
	 * and replace files
	 */
	private static boolean useCEAlg = false;

	/**
	 * Set whether or not to use the CE algorithm
	 *
	 * @param useCEAlg
	 *            true or false
	 */
	public static void setUseCEAlg(boolean useCEAlg) {
		Unzip.useCEAlg = useCEAlg;
	}

	/**
	 * Get the value of the useCEAlg flag
	 *
	 * @return true or false
	 */
	public static boolean isUseCEAlg() {
		return useCEAlg;
	}

	/**
	 * Unzips an entire zip archive to a given directory and optionally backs up
	 * replaced files. If the CE algorithm for replacing and backing up files
	 * should be used, a call to setUseCEAlg(true) should have been made
	 * previously and a call to setUseCEAlg(false) should be made afterwards.
	 *
	 * @param filename
	 *            Name of the zip archive
	 * @param directory
	 *            Destination for extracted files from the archive
	 * @param backupDir
	 *            Directory to place backed up files or null if no backup should
	 *            occur
	 */
	public static int unzipToDirectory(String filename, String directory,
			String backupDir) {
		return unzipToDirectory(filename, directory, backupDir, null);
	}

	/**
	 * Unzips an entire zip archive to a given directory and optionally backs up
	 * replaced files. If the CE algorithm for replacing and backing up files
	 * should be used, a call to setUseCEAlg(true) should have been made
	 * previously and a call to setUseCEAlg(false) should be made afterwards.
	 * Also excludes list of files which need not to be unzipped.
	 *
	 * @param filename
	 *            Name of the zip archive
	 * @param directory
	 *            Destination for extracted files from the archive
	 * @param backupDir
	 *            Directory to place backed up files or null if no backup should
	 *            occur
	 * @param filestoExclude
	 * 			  List of files which need NOT to be unzipped from zip.
	 */
	public static int unzipToDirectory(String filename, String directory,
			String backupDir, String [] filestoExclude) {
		FileGarbageCan.add(defXLoc + "/" + filename);
		String cur_filename = "";
		try {
			ZipFile zf = new ZipFile(defXLoc + "/" + filename);
			Enumeration zipEnum = zf.entries();

			if (directory != null) {
				directory = directory.replace('\\', '/');
			} else
				directory = ".";

			if (directory.charAt(directory.length() - 1) != '/')
				directory += "/";

			if (backupDir != null) {
				backupDir = backupDir.replace('\\', '/');
				if (backupDir.charAt(backupDir.length() - 1) != '/')
					backupDir += "/";
			}

			while (zipEnum.hasMoreElements()) {
				ZipEntry item = (ZipEntry) zipEnum.nextElement();
				cur_filename = item.getName();

				if (filestoExclude != null) {
					boolean found = false;
					for(int i=0; i < filestoExclude.length; i++){
						if (cur_filename.equalsIgnoreCase(filestoExclude[i])){
							found = true;
							break;
						}
					}
					//If file need not to be unzipped we skip unzipping.
					if (found)
						continue;
				}

				if (item.isDirectory()) // Directory
				{
					File newdir = new File(directory + cur_filename);
					if (!newdir.exists()) { // No need to create if it is already there
						UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
								"CREATE.DIR", newdir),
								UpdateInstallerMsgs.DEBUG);
						boolean dirOK = true;
						dirOK = newdir.mkdirs();
						if (!dirOK) {
							UpdateInstallerMsgs.log(UpdateInstallerMsgs
									.getString("FILE.DIR.CREATE.ERROR", newdir,
											filename),
									UpdateInstallerMsgs.ERROR);
							System.out.println(UpdateInstallerMsgs.getString(
									"FILE.DIR.CREATE.ERROR", newdir, filename));
							zf.close();
							return -1;
						}
					}
				} else { // File
					String newfile = directory + cur_filename;
					// Check to see if we have special CE processing to do
					String ceMatch = null;
					if (useCEAlg == true)
						ceMatch = checkCEFile(directory, cur_filename);

					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString("WRITING.FILE",
									newfile), UpdateInstallerMsgs.DEBUG);

					// Create directory if it doesn't exist
					int last_slash = newfile.lastIndexOf('/');
					String target_dir = newfile.substring(0, last_slash);
					File cur_dir = new File(target_dir);
					boolean dirOK = true;
					if (!cur_dir.exists()) {
						dirOK = cur_dir.mkdirs();
					}
					if (!dirOK) {
						UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
								"FILE.DIR.CREATE.ERROR", cur_dir, filename),
								UpdateInstallerMsgs.ERROR);
						System.out.println(UpdateInstallerMsgs.getString(
								"FILE.DIR.CREATE.ERROR", cur_dir, filename));
						zf.close();
						return -1;
					}

					int ch;

					boolean isExecute = new File(newfile).canExecute();

					if (backupDir != null) { // Backup current file
						UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
								"LOOKING.AT.FILE", newfile),
								UpdateInstallerMsgs.DEBUG);
						File cur_file = new File(newfile);
						if (cur_file.exists() || ceMatch != null) {
							// Create directory if it doesn't exist
							String fix_target_dir = target_dir
									.substring(directory.length() - 1);
							cur_dir = new File(backupDir + "/" + fix_target_dir);
							dirOK = true;
							if (!cur_dir.exists())
								dirOK = cur_dir.mkdirs();
							if (!dirOK) {
								System.out.println(UpdateInstallerMsgs
										.getString("FILE.DIR.CREATE.ERROR",
												cur_dir, filename));
								UpdateInstallerMsgs.log(UpdateInstallerMsgs
										.getString("FILE.DIR.CREATE.ERROR",
												cur_dir, filename),
										UpdateInstallerMsgs.ERROR);
								zf.close();
								return -1;
							}

							String oldNewFile = newfile;
							if (ceMatch != null) {
								if (!cur_file.exists())
									newFiles.add(newfile);
								newfile = directory + ceMatch; // Set filename
								// to file we're
								// actually
								// backing up
								FileGarbageCan.add(newfile);
							}

							String fix_newfile = newfile.substring(directory
									.length());
							File backupFile = new File(backupDir, fix_newfile);

							UpdateInstallerMsgs.log(
									UpdateInstallerMsgs.getString(
											"BACKUP.FILE", newfile, backupFile),
									UpdateInstallerMsgs.DEBUG);

							if (backupFile.exists())
								backupFile.delete(); // On Windows rename may fail if target exists.
							new File(newfile).renameTo(backupFile);
							newfile = oldNewFile;
						} else {
							newFiles.add(newfile);
						}

						// End of backup
					}

					InputStream is = zf.getInputStream(item);
					FileOutputStream fos = new FileOutputStream(newfile);

					byte[] buf = new byte[8192];
					int n;
					while ((n = is.read(buf)) > 0) {
						fos.write(buf, 0, n);
					}

					is.close();
					fos.close();
					if (isExecute)
						new File(newfile).setExecutable(true, false);
				}// end of else // file section
			}// end of while

			zf.close();
			return 0;
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString("ERROR.DURING.UNZIP",
					cur_filename, filename, e.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString("ERROR.DURING.UNZIP",
					cur_filename, filename, e.getLocalizedMessage()), UpdateInstallerMsgs.ERROR);
			return -1;
		}

	}

	/**
	 * @param directory
	 * @param cur_filename
	 * @return f
	 */
	private static String checkCEFile(String directory, String cur_filename) {
		// If the file isn't of the form com.ibm.tdi.---x.x.x.x.jar there's no
		// special processing
		if (cur_filename
				.matches("^.*com\\.ibm\\.tdi\\..*\\d+\\.\\d+\\.\\d+\\.\\d\\.jar$")) {
			int i = cur_filename.lastIndexOf('/');
			String actualDirectory = directory;
			String origDir = cur_filename.substring(0, i);
			String filename2Cmp = cur_filename;
			if (i > 0) {
				actualDirectory += "/" + cur_filename.substring(0, i);
				filename2Cmp = cur_filename.substring(i + 1);
			}

			Pattern myPattern = Pattern.compile("^(.*)(\\d+\\.){4}jar$");
			Matcher myMatch = myPattern.matcher(filename2Cmp);
			if (myMatch.find())
				filename2Cmp = myMatch.group(1);

			File f = new File(actualDirectory);
			File[] files = f.listFiles();
			if (files != null)
				for (int x = 0; x < files.length; x++) {
					String fileName = files[x].getName();
					if (fileName.matches("^.*" + filename2Cmp
							+ "(\\d+\\.){3}\\d+\\.jar$")) {
						return origDir + "/" + fileName;
					}
				}
		}

		return null;
	}

	/**
	 * Reads a file in a zip archive without unzipping it. This method is only
	 * intended to be used on text files.
	 *
	 * @param filename
	 *            Filename inside of the archive to read
	 * @param zipfile
	 *            Name of the zip archive which contains the file to read
	 * @return String[] Lines of the target file
	 */
	public static String[] unzipAndReadSingleFile(String filename,
			String zipfile) {
		try {
			ZipFile zf = new ZipFile(zipfile);
			Enumeration zipEnum = zf.entries();
			boolean found = false;

			// Search for our file
			ZipEntry item = null;
			while (!found && zipEnum.hasMoreElements()) {
				item = (ZipEntry) zipEnum.nextElement();
				// Directory or just not a match?
				if (item.isDirectory() || !item.getName().equals(filename))
					continue;

				found = true;
			}

			if (!found) {
				System.out.println(UpdateInstallerMsgs.getString(
						"ARCHIVE.FILE.NOT.FOUND", filename, zipfile));
				UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
						"ARCHIVE.FILE.NOT.FOUND", filename, zipfile),
						UpdateInstallerMsgs.ERROR);
				zf.close();
				return null;
			}

			// Read our file
			InputStream is;

			is = zf.getInputStream(item);
			String lines[] = FixUtils.readFile(is);

			if (lines == null)
				UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
						"EMPTY.FILE", filename, zipfile),
						UpdateInstallerMsgs.DEBUG);

			zf.close();
			is.close();
			return lines;
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"ERROR.DURING.UNZIP", filename, zipfile, e
							.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"ERROR.DURING.UNZIP", filename, zipfile, e
							.getLocalizedMessage()), UpdateInstallerMsgs.ERROR);
		}
		return null;
	}

	/**
	 * Gets an input stream associated with a file inside a zip archive so it
	 * can be subsequently read.
	 *
	 * @param filename
	 *            Filename inside of the archive
	 * @param zipfile
	 *            Name of the zip archive which contains the file
	 * @return An input stream representing the file to be read
	 */
	public static InputStream getInputStreamFromFileInArchive(String filename,
			ZipFile zf,String zipfile) {
		InputStream is = null;
		try {
			//ZipFile zf = new ZipFile(zipfile);
			Enumeration zipEnum = zf.entries();
			boolean found = false;

			// Search for our file
			ZipEntry item = null;
			while (!found && zipEnum.hasMoreElements()) {
				item = (ZipEntry) zipEnum.nextElement();
				// Directory or just not a match?
				if (item.isDirectory() || !item.getName().equals(filename))
					continue;

				found = true;
			}

			if (!found) {
				System.out.println(UpdateInstallerMsgs.getString(
						"ARCHIVE.FILE.NOT.FOUND", filename, zipfile));
				UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
						"ARCHIVE.FILE.NOT.FOUND", filename, zipfile),
						UpdateInstallerMsgs.ERROR);
			} else
				is = zf.getInputStream(item);

		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"ERROR.DURING.UNZIP", filename, zipfile, e
							.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"ERROR.DURING.UNZIP", filename, zipfile, e
							.getLocalizedMessage()), UpdateInstallerMsgs.ERROR);
		}
		return is;
	}

	/**
	 * Tells if a file with the given name is contained in a zip archive.
	 *
	 * @param filename
	 *            Filename to search for in the zip archive
	 * @param zipfile
	 *            Name of the zip archive
	 * @return true if a file with the specified name is in the archive, false
	 *         otherwise
	 */
	public static boolean isFileInArchive(String filename, String zipfile) {
		boolean found = false;
		String osName = System.getProperty("os.name");
		boolean isWin = osName.startsWith("Win");
		String filename2 = filename;
		if (isWin)
			filename2 = filename2.replaceAll("/", "\\\\");
		else
			filename2 = filename2.replaceAll("\\\\", "/");

		ZipFile zf=null;
		try {
			zf = new ZipFile(zipfile);
			Enumeration zipEnum = zf.entries();

			// Search for our file
			ZipEntry item = null;
			while (!found && zipEnum.hasMoreElements()) {
				item = (ZipEntry) zipEnum.nextElement();

				if (item.isDirectory() || // Directory
						(!item.getName().equals(filename) && !item.getName()
								.equals(filename2))) // Not a match
					continue;

				found = true;
			}
			zf.close();
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"ERROR.DURING.UNZIP", filename, zipfile, e
							.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"ERROR.DURING.UNZIP", filename, zipfile, e
							.getLocalizedMessage()), UpdateInstallerMsgs.ERROR);
		}

		return found;
	}

	/**
	 * Tells if a file matching the given regular expression is contained in a
	 * zip archive.
	 *
	 * @param spec
	 *            A regular expression indicating a file to search for in a zip
	 *            archive
	 * @param zipfile
	 *            Name of the zip archive
	 * @return Name of the first matching file or the empty string if not found
	 */
	public static String findMatchingFileSpecInZip(String spec, String zipfile) {
		try {
			ZipFile zf = new ZipFile(zipfile);
			Enumeration zipEnum = zf.entries();
			boolean found = false;

			// Search for our file
			ZipEntry item = null;
			while (!found && zipEnum.hasMoreElements()) {
				item = (ZipEntry) zipEnum.nextElement();
				// Directory or just not a match?
				if (item.isDirectory() || !item.getName().matches(spec))
					continue;

				found = true;
			}

			if (!found) {
				System.out.println(UpdateInstallerMsgs.getString(
						"ARCHIVE.FILE.NOT.FOUND", spec, zipfile));
				UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
						"ARCHIVE.FILE.NOT.FOUND", spec, zipfile),
						UpdateInstallerMsgs.ERROR);
			} else
				return item.getName();

		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"ERROR.DURING.UNZIP", spec, zipfile, e
							.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"ERROR.DURING.UNZIP", spec, zipfile, e
							.getLocalizedMessage()), UpdateInstallerMsgs.ERROR);
		}

		return "";

	}

	/**
	 * Unzips a file from a specified archive with the same name as contained in
	 * the archive. The file can be optionally read into a buffer.
	 *
	 * @param filename
	 *            Name of the file to extract
	 * @param zipfile
	 *            Name of the zip archive
	 * @param isText
	 *            Indicates whether or not the file should be treated as text
	 *            and read into a buffer
	 * @param target_filename
	 *            Name of the file this archive should be extracted as.
	 *            Normally, this should match the original filename
	 * @return Lines of the file as an array if isText was true, null otherwise
	 */
	public static String[] unzipSingleBinaryFile(String filename,
			String zipfile, boolean isText, String target_filename) {
		String lines[] = null;
		FileOutputStream out = null;
		InputStream is = null;
		try {
			ZipFile zf=new ZipFile(zipfile);
			is = getInputStreamFromFileInArchive(filename, zf,zipfile);
			if (is == null)
				return null;

			// Read our file
			if (isText) { // We're reading a text file
				lines = FixUtils.readFile(is);

				if (lines == null) {
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"EMPTY.FILE", filename, zipfile),
							UpdateInstallerMsgs.DEBUG);
				}
			} else { // we're copying a binary file
				// Would need just the filename if this could have a dir
				File outFile = new File(defXLoc + "/" + target_filename);
				boolean fileOK = true;
				fileOK = outFile.createNewFile();
				if (!fileOK) {
					System.out.println(UpdateInstallerMsgs.getString(
							"FILE.DIR.CREATE.ERROR", target_filename, zipfile));
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"FILE.DIR.CREATE.ERROR", target_filename, zipfile),
							UpdateInstallerMsgs.ERROR);
					is.close();
					return lines;
				}
				out = new FileOutputStream(outFile);

				byte[] buf = new byte[1024];
				int len = 0;
				while ((len = is.read(buf)) > 0)
					out.write(buf, 0, len);
			}
			if (zf != null)
				zf.close();
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"ERROR.DURING.UNZIP", target_filename, zipfile, e
							.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"ERROR.DURING.UNZIP", target_filename, zipfile, e
							.getLocalizedMessage()), UpdateInstallerMsgs.ERROR);
		}
		/**
		 * If an IOException error occurs while creating file or writing to the file
		 * then IO stream object should be closed before returning.
		 */
		 finally{
		        	try{
		        		if(out != null)
		        			out.close();
		        		if(is != null)
		        			is.close();
		        	}catch(Exception e){
		        		System.out.println("Error: " + e.getLocalizedMessage());
		        	}
		     	}
		return lines;
	}

	/**
	 * Unzips a file from a specified archive. The file can be optionally read
	 * into a buffer.
	 *
	 * @param filename
	 *            Name of the file to extract
	 * @param zipfile
	 *            Name of the zip archive
	 * @param isText
	 *            Indicates whether or not the file should be treated as text
	 *            and read into a buffer
	 * @return Lines of the file as an array if isText was true, null otherwise
	 */
	public static String[] unzipSingleBinaryFile(String filename,
			String zipfile, boolean isText) {
		return unzipSingleBinaryFile(filename, zipfile, isText, filename);
	}

	/**
	 * Lists files in an archive. (DEBUG ONLY)
	 *
	 * @param zipfile
	 *            Name of the zip archive
	 */
	public static void listArchive(String zipfile) {
		boolean found = false;
		try {
			ZipFile zf = new ZipFile(zipfile);
			Enumeration zipEnum = zf.entries();

			// Search for our file
			ZipEntry item = null;
			while (!found && zipEnum.hasMoreElements()) {
				item = (ZipEntry) zipEnum.nextElement();
				System.out.println(item.getName());
			}
			zf.close();
		} catch (IOException ioe) {
			System.out.println("Error: " + ioe.getLocalizedMessage());
		}
	}

	/**
	 * Looks for a file that is in a zip within the main zip file. This function
	 * basically stops short of doing a full recursive search for a file, but
	 * could be expanded to do so.
	 *
	 * @param filename
	 *            Name of the file to search for
	 * @param zipfile
	 *            Name of the zip archive
	 * @param innerZipFile
	 *            Name of the zip within the zip file to check archive
	 * @return true or false indicating whether or not the file was found
	 */
	public static boolean findFileInsideInnerZip(String filename,
			String zipfile, String innerZipFile) {
		String lines[] = null;
		FileOutputStream out = null;
		InputStream is = null;
		boolean found = false;
		ZipFile zf=null;
		try {
			zf=new ZipFile(zipfile);
			is = getInputStreamFromFileInArchive(innerZipFile, zf, zipfile);
			if (is == null) {
				return false;
			}
			ZipInputStream zis = new ZipInputStream(is);

			// Search for our file
			ZipEntry item = zis.getNextEntry();
			while (!found && item != null) {
				if (item.isDirectory() || // Directory
						(!item.getName().equals(filename))) {
					item = zis.getNextEntry();
					continue;
				}

				found = true;
			}
			zis.close();
			is.close();
			zf.close();
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"ERROR.DURING.UNZIP", filename, zipfile, e
							.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"ERROR.DURING.UNZIP", filename, zipfile, e
							.getLocalizedMessage()), UpdateInstallerMsgs.ERROR);
		}
		return found;
	}
	
	public static void writeNewFiles(String backupDir) {
		if (newFiles.isEmpty())
			return;

		File outFile = new File(backupDir, ".newFiles");
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(outFile));
			for (String name: newFiles) {
				out.write(name);
				out.newLine();
			}
		} catch (Exception e) {
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e1) {}
		}

	}
}
