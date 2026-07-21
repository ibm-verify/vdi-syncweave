/*
 * IBM Confidential
 *
 *  OCO Source Materials
 *
 * 5724-D49
 *
 * (C) Copyright IBM Corporation. 2011, 2011
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     %I%, %G%
 * @owner       
 * @history
 */
package example_connector;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.*;
import com.ibm.di.server.*;
import com.ibm.di.connector.*;

/**
 * This example connector provides access to the file system through the standard add,delete,iterate and update methods of the
 * connector interface. The connector can only update files (e.g. create, overwrite and delete).
 * <p/>
 * Missing functionality is creating/deleting directories and
 * renaming files and directories. This can be accomplished by modifying the connector to deal with more attributes
 * than "path" and "data" in the modEntry(entry) and deleteEntry(entry) methods.
 *
 */

public class DirectoryConnector extends Connector implements FileFilter {
	
	private String path;
	private boolean recursive;
	private boolean wantsDirectories;
	private boolean readonlyfiles;
	private boolean hiddenfiles;
	private boolean filterfullpath;
	private Vector<String> filters = new Vector<String>();
	private File[] files;
	private int filesIndex = -1;
	private Vector<File> pendingDirectories = new Vector<File>();
	private Exception error = null;


	/**
	 * Constructor
	 */
	public DirectoryConnector() {
		super();

		// Set the supported modes
		setModes(new String[]{
				ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.UPDATE_MODE,
				ConnectorConfig.LOOKUP_MODE,
				ConnectorConfig.DELETE_MODE,
				});
	}


	/**
	 * selectEntries retrieves the first batch of file names from the configured "path"
	 *
	 * @exception  Exception  Description of the Exception
	 */
	public void selectEntries() throws Exception {
		getFiles(new File(path));
		if (error != null) {
			throw error;
		}
	}


	private void getFiles(File path) throws Exception {
		filesIndex = 0;
		files = path.listFiles(this);
	}


	/**
	 * This method is called by File.listFiles and is used to filter out files/directories.
	 *
	 */
	public boolean accept(File pathname) {
		try {
			debug("-- test: " + pathname);

			// Do we recurse into sub directories?
			if (pathname.isDirectory() && recursive) {
				debug("  -- directory will be scanned later");
				pendingDirectories.add(pathname);
			}

			// read-only files
			if (readonlyfiles && pathname.canWrite()) {
				return false;
			}

			// Hidden files
			if (!hiddenfiles && pathname.isHidden()) {
				return false;
			}

			// Want directory names?
			if (pathname.isDirectory() && !wantsDirectories) {
				debug("  Skipped - path is a directory");
				return false;
			}

			for (int i = 0; i < filters.size(); i++) {
				String str;
				if (filterfullpath) {
					str = pathname.toString();
				} else {
					str = pathname.getName();
				}
				debug("  -- pattern test: filter=" + filters.get(i) + ", value=" + str);
				if (Pattern.matches((String) filters.get(i), str)) {
					debug("  -- matches filter: " + filters.get(i));
					return true;
				}
			}
		} catch (Exception error) {
			this.error = error;
			return false;
		}

		// If no filters then we accept everything
		return (filters.size() == 0);
	}


	/**
	 * Initialize connector - we only retrieve and validate parameters here.
	 *
	 * @param  o              ConnectorMode
	 * @exception  Exception  Description of the Exception
	 */
	public void initialize(Object o) throws Exception {

		recursive = Boolean.valueOf(getParam("recursive")).booleanValue();
		wantsDirectories = Boolean.valueOf(getParam("wantsDirectories")).booleanValue();
		hiddenfiles = Boolean.valueOf(getParam("hiddenfiles")).booleanValue();
		readonlyfiles = Boolean.valueOf(getParam("readonlyfiles")).booleanValue();
		filterfullpath = Boolean.valueOf(getParam("filterfullpath")).booleanValue();

		path = getParam("path");
		if (path == null || path.trim().length() == 0) {
			path = ".";
		}

		String str = getParam("filters");
		if (str != null) {
			StringTokenizer st = new StringTokenizer(str, "\n");
			while (st.hasMoreTokens()) {
				String f = st.nextToken();
				if (f.trim().length() > 0) {
					filters.add(f.trim());
				}
			}
		}
	}


	/**
	 * Return the next file entry.
	 *
	 * @return                The the next file entry.
	 * @exception  Exception  Description of the Exception
	 */
	public Entry getNextEntry() throws Exception {

		while (files != null && filesIndex >= files.length && pendingDirectories.size() > 0) {
			getFiles((File) pendingDirectories.remove(0));
			if (error != null) {
				throw error;
			}
		}

		// no more files
		if (files == null || filesIndex >= files.length) {
			return null;
		}

		Entry e = new Entry();
		e.setAttribute("file", files[filesIndex]);
		e.setAttribute("filename", files[filesIndex].getName());
		e.setAttribute("filedirectory", files[filesIndex].getParent());
		e.setAttribute("readonly", "" + !files[filesIndex].canWrite());
		e.setAttribute("isdirectory", "" + files[filesIndex].isDirectory());
		e.setAttribute("ishidden", "" + files[filesIndex].isHidden());
		e.setAttribute("lastModified", new Date(files[filesIndex].lastModified()));
		e.setAttribute("length", "" + files[filesIndex].length());

		filesIndex++;

		return e;
	}


	/**
	 * Overwrite old file with contents in entry. Entry must contain "path" and "data" attributes.
	 */
	public void modEntry(Entry entry) throws Exception {
		writeFile(entry, true);
	}


	/**
	 * Create new file file with contents in entry. Entry must contain "path" and "data" attributes.
	 */
	public void putEntry(Entry entry) throws Exception {
		writeFile(entry, false);
	}


	/**
	 * Write the contents of the "data" attribute to the file specified by the "path" attribute. The "data" attribute
	 * must be a java.lang.String or a byte[].
	 */
	public void writeFile(Entry entry, boolean overwrite) throws Exception {
		String path = entry.getString("path");
		if (path == null) {
			throw new Exception("No path or filename specified in add operation");
		}

		File out = new File(path);
		if (out.exists() && !overwrite) {
			throw new Exception("File already exists: " + out);
		} else if (!out.exists() && overwrite) {
			throw new Exception("File does not exist: " + out);
		} else if (overwrite) {
			if (!out.mkdirs()) {
				throw new Exception("Unable to create directory path to: " + out);
			}
		}

		Object data = entry.getObject("data");
		FileOutputStream fos = new FileOutputStream(out);
		if (data instanceof String) {
			fos.write(data.toString().getBytes());
		} else if (data instanceof byte[]) {
			fos.write((byte[]) data);
		} else if (data == null) {
			;
			// No data means create file only
		} else {
			throw new Exception("Unknown file content object: " + data.getClass().getName());
		}
		fos.close();
	}


	/**
	 * Delete the file named by the "path" attribute.
	 */
	public void deleteEntry(Entry entry) throws Exception {
		String path = entry.getString("path");
		if (path == null) {
			throw new Exception("No path or filename specified in add operation");
		}

		File out = new File(path);
		if (!out.exists()) {
			throw new java.io.FileNotFoundException(out.getAbsolutePath());
		} else if (out.isDirectory()) {
			throw new Exception("Cannot delete directories: " + out.getAbsolutePath());
		} else if (!out.delete()) {
			throw new Exception("Unable to delete " + out.getAbsolutePath());
		}

		out.delete();
	}


	/**
	 * Search the file system (path attribute) for files matching the regex expressions given in the
	 * "name" attribute. The name and path attributes default to the configured path and filters.
	 */
	public Entry findEntry(SearchCriteria search) throws Exception {
		Entry e;
		int saveIndex = filesIndex;
		File[] saveFiles = files;
		Vector<String> saveFilters = filters;

		// Clear list of multiple entries found
		clearFindEntries();

		try {
			// Get optional path and name regex filter from the search criteria
			filters = new Vector<String>();
			File searchpath = new File(path);
			for (int i = 0; i < search.size(); i++) {
				if (search.getCriteria(i).name.equals("path")) {
					searchpath = new File(search.getCriteria(i).value.toString());
				} else if (search.getCriteria(i).name.equals("name")) {
					// NOTE! Here you might translate the search.getCriteria(i).match for SearchCriteria.SUBSTRING etc
					// For simplicity, we assume that the value contains a regex expression and we silently ignore the match operator.
					filters.add(search.getCriteria(i).value.toString());
				} else {
					throw new Exception("Bad search name: " + search.getCriteria(i).name + " (use path or name)");
				}
			}

			// Get files
			getFiles(searchpath);

			// Only return as many entries as user has limited us to
			while ((e = getNextEntry()) != null) {
				// addFindEntry returns false when we've reached the max return limit
				if (!addFindEntry(e)) {
					break;
				}
			}

			// If only one entry is found we return that one. Otherwise, we return null to signal
			// that zero or more than one was found (caller uses getFindEntryCount() to get actual number).
			if (getFindEntryCount() == 1) {
				return getFirstFindEntry();
			} else {
				return null;
			}

		} finally {
			// Restore configured path, index and filters
			files = saveFiles;
			filesIndex = saveIndex;
			filters = saveFilters;
		}
	}


	/**
	 * Return version information
	 *
	 * @return    The version value
	 */
	public String getVersion() {
		return "1.0";
	}

}

