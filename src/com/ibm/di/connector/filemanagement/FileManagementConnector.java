/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.filemanagement;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.SearchCriteria.rscSearch;
import com.ibm.di.server.criteria.DefaultSearchCriteriaMatcher;

/**
 * Connector which can read or modify files structure and metadata. It can
 * create, find and delete files and directories. It works on Iterator, Lookup,
 * Delete, AddOnly or Update modes.
 * 
 * @since 7.2
 */
public class FileManagementConnector extends Connector implements ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component name.
	 */
	private static final String myName = "File Management Connector";

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "filemanagementconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash resHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * Component parameter Start Directory Path name.
	 */
	private static final String START_DIRECTORY_PARAM_NAME = "startDirectory";

	/**
	 * Component parameter Maximum Depth name.
	 */
	private static final String DEPTH_PARAM_NAME = "depth";

	/**
	 * Component parameter User RegEx Flag name.
	 */
	private static final String REGULAR_EXPRESSION_PARAM_NAME = "regexFilter";

	/**
	 * Component parameter File List Filter name.
	 */
	private static final String FILTER_PARAM_NAME = "filter";

	/**
	 * Component parameter Return Follow Symbolics Links Flag name.
	 */
	private static final String FOLLOW_SYMBOLICS_LINK_PARAM_NAME = "followSymbolicLinks";

	/**
	 * Component parameter Return File Only Flag name.
	 */
	private static final String FILE_ONLY_PARAM_NAME = "fileOnly";

	/**
	 * Component parameter Force Delete Flag name.
	 */
	private static final String FORCE_DELETE_PARAM_NAME = "forceDelete";

	/**
	 * Component parameter Keep Original Files or Directories Flag name.
	 */
	private static final String KEEP_ORIGINAL_PARAM_NAME = "keepOriginal";

	/**
	 * Component parameter Create File Flag name.
	 */
	private static final String CREATE_FILE_PARAM_NAME = "createFile";

	/**
	 * Entry attribute containing the java.io.File of the file or directory.
	 */
	private static final String FILE_ATTR = "file";

	/**
	 * Entry attribute containing the file's local name.
	 */
	private static final String NAME_ATTR = "name";

	/**
	 * Entry attribute containing the file's parent path.
	 */
	private static final String PARENT_ATTR = "parent";

	/**
	 * Entry attribute containing Read Only flag.
	 */
	private static final String IS_READ_ONLY_ATTR = "isReadOnly";

	/**
	 * Entry attribute determining if a file is hidden or not.
	 */
	private static final String IS_HIDDEN_ATTR = "isHidden";

	/**
	 * Entry attribute used for distinguishing files from directories.
	 */
	private static final String IS_DIRECTORY_ATTR = "isDirectory";

	/**
	 * Entry attribute containing the last modification time stamp of the file.
	 */
	private static final String LAST_MODIFIED_ATTR = "lastModified";

	/**
	 * Entry attribute containing the file's length.
	 */
	private static final String LENGHT_ATTR = "length";

	/**
	 * Entry attribute containing the full path to the file.
	 */
	private static final String FULL_PATH_ATTR = "fullPath";

	/**
	 * Entry attribute determining if a file is symbolic link.
	 */
	private static final String IS_SYMBOLIC_LINK_ATTR = "isSymbolicLink";

	/**
	 * Entry attribute containing the content to by written.
	 */
	private static final String CONTENT_ATTR = "content";

	/**
	 * Entry attribute containing the char set of the content.
	 */
	private static final String CHAR_SET_ATTR = "charSet";

	/**
	 * Define value for search in all sub directories.
	 */
	private static final int ALL_SUB_DIRECTORIES = -1;

	/**
	 * Define value for search only start directory.
	 */
	private static final int ONLY_START_DIRECTORY = 0;

	/**
	 * List of all files and directories.
	 */
	private LinkedList<File> filesAndDirectories = null;

	/**
	 * Comparator used to sort files before directories.
	 */
	private Comparator<File> fileFirstComperator = null;

	/**
	 * Follow Symbolic Links.
	 */
	private boolean followSymbolicLinks;

	/**
	 * Return only files flag.
	 */
	private boolean filesOnly = false;

	/**
	 * Force deleting non-empty directories or read-only files.
	 */
	private boolean forceDelete = false;

	/**
	 * Move flag.
	 */
	private boolean keepOriginal = false;

	/**
	 * File list filter.
	 */
	private RegExFileFilter fileFilter = null;

	/**
	 * Maximum depth of sub-directories to look.
	 */
	private int maxDirDepth = ALL_SUB_DIRECTORIES;

	/**
	 * Start point for iterating through directories tree.
	 */
	private File startDirectory = null;

	/**
	 * Constructor. Initializes the connector to work in AddOnly, Iterator,
	 * Lookup, Update and Delete mode.
	 */
	public FileManagementConnector() {
		setName(myName);
		// Set the supported modes
		setModes(new String[] { //
		ConnectorConfig.ITERATOR_MODE, //
				ConnectorConfig.ADDONLY_MODE, //
				ConnectorConfig.UPDATE_MODE, //
				ConnectorConfig.LOOKUP_MODE, //
				ConnectorConfig.DELETE_MODE, //
		});
	}

	/**
	 * {@inheritDoc}
	 **/
	@Override
	public void initialize(Object o) throws Exception {
		String directoryPath = getParam(START_DIRECTORY_PARAM_NAME);
		if (!isStringSet(directoryPath)) {
			throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.NO.PATH.SPECIFIED"));
		}

		startDirectory = new File(directoryPath);
		startDirectory = normalizeFilePath(startDirectory);
		filesAndDirectories = new LinkedList<File>();

		String param = getParam(FOLLOW_SYMBOLICS_LINK_PARAM_NAME);
		followSymbolicLinks = Boolean.parseBoolean(param);
		param = getParam(FILE_ONLY_PARAM_NAME);
		filesOnly = Boolean.parseBoolean(param);
		param = getParam(FORCE_DELETE_PARAM_NAME);
		forceDelete = Boolean.parseBoolean(param);
		param = getParam(KEEP_ORIGINAL_PARAM_NAME);
		keepOriginal = Boolean.parseBoolean(param);
	}

	/**
	 * Normalize the file path resolving <code>'.'</code> and <code>'..'</code>
	 * 
	 * @param file
	 *            to be normalized
	 * @return file without <code>'.'</code> and <code>'..'</code> in its path.
	 */
	private File normalizeFilePath(File file) {
		URI uri = file.toURI().normalize();
		File normalizedFile = null;

		final String UNC_PREFIX = "\\\\";
		// Check if UNC path is used.
		if (!file.toString().startsWith(UNC_PREFIX)) {
			normalizedFile = new File(uri);
		} else {
			normalizedFile = new File(UNC_PREFIX, uri.getPath());
		}
		return normalizedFile;
	}

	/**
	 * Check String if exist and has a value.
	 * 
	 * @param value
	 *            to be checked
	 * @return true if it is valid string value, else false.
	 */
	private boolean isStringSet(String value) {
		return value != null && value.trim().length() > 0;
	}

	/**
	 * Prepare the Connector for sequential read. Setup start directory. When
	 * the Connector is used as an Iterator in an AssemblyLine, this method will
	 * be called.
	 * 
	 * @throws Exception
	 *             if startDirectory parameter is not name of existing directory
	 *             or maxDirDepth is negative.
	 */
	@Override
	public void selectEntries() throws Exception {
		if (!startDirectory.isDirectory()) {
			throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.PATH.IS.NOT.DIRECTORY", startDirectory));
		}

		String param = getParam(DEPTH_PARAM_NAME);
		if (isStringSet(param)) {
			int depth = Integer.parseInt(param.trim());
			if (depth >= 0) {
				maxDirDepth = depth;
			} else {
				throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.DEPTH.CANNOT.BE.NEGATIVE"));
			}
		}

		String filter = getParam(FILTER_PARAM_NAME);
		if (isStringSet(filter)) {
			filter = filter.trim();
			if (Boolean.parseBoolean(getParam(REGULAR_EXPRESSION_PARAM_NAME))) {
				fileFilter = new RegExFileFilter(startDirectory.getAbsolutePath(), filter);
			} else {
				// Optimization: if the provided filter does not contain **,
				// iteration will be limited only to the start directory.
				if (!filter.contains("**")) {
					maxDirDepth = ONLY_START_DIRECTORY;
				}
				try {
					fileFilter = new GlobFileFilter(startDirectory.getAbsolutePath(), filter);
				} catch (PatternSyntaxException e) {
					throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.INVALIDE.GLOB.PATTERN", filter), e);
				}
			}
		}

		fileFirstComperator = new Comparator<File>() {
			public int compare(File file1, File file2) {
				int result = 0;
				if (file1.isFile() && file2.isDirectory()) {
					result = -1;
				} else if (file1.isDirectory() && file2.isFile()) {
					result = 1;
				}
				return result;
			}
		};
		addDirectoryContent(startDirectory);
	}

	/**
	 * Add the contents of the provided directory to the list of
	 * files/directories which will be iterated.
	 * 
	 * @param directory
	 *            a directory which content will be added to the iterated tree.
	 */
	private void addDirectoryContent(File directory) {
		File[] fileArray = directory.listFiles(fileFilter);
		if (fileArray != null) { // can list contents
			if (fileArray.length > 0) {
				// Performance optimization. Measured memory reduction is 4
				// times.
				// We always add sorted data in the stack (files before
				// directories) and take the first element.
				Arrays.sort(fileArray, fileFirstComperator);
				filesAndDirectories.addAll(0, Arrays.asList(fileArray));
			}
		} else {
			logmsg(resHash.getString("CONNECTOR.FILEMANAGEMENT.SECURITY.VIOLATION", directory));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Entry getNextEntry() throws Exception {
		File file = null;
		Entry entry = null;

		if (filesAndDirectories.size() > 0) {
			// Take the first element.
			file = filesAndDirectories.pollFirst();
			boolean ignoreEntry = false;
			boolean isSymbolicLink = false;
			if (file.exists()) {
				isSymbolicLink = isSymbolicLink(file);
				if (file.isDirectory()) {
					if ((maxDirDepth == ALL_SUB_DIRECTORIES || getPathDepth(file) <= maxDirDepth)
							&& (!isSymbolicLink || followSymbolicLinks)) {
						addDirectoryContent(file);
					}
					if (filesOnly || (fileFilter != null && !fileFilter.checkPath(file.getAbsolutePath()))) {
						ignoreEntry = true;
					}
				}
			} else {
				// the file has been deleted since we first indexed it
				logmsg(resHash.getString("CONNECTOR.FILEMANAGEMENT.DOES.NOT.EXIST", file));
				ignoreEntry = true;
			}
			if (ignoreEntry) {
				entry = getNextEntry();
			} else {
				entry = generateEntry(file, isSymbolicLink);
			}
		}
		return entry;
	}

	/**
	 * Create Entry attributes from File attributes.
	 * 
	 * @param file
	 *            Source File used to generate Entry.
	 * @param isSymbolicLink
	 *            flag show if file is Symbolic Link
	 * @return Entry with same attributes like File attributes.
	 * @throws Exception
	 *             When file does not exist.
	 */
	private Entry generateEntry(File file, boolean isSymbolicLink) throws Exception {
		Entry e = new Entry();
		e.setAttribute(FULL_PATH_ATTR, file.getCanonicalPath());
		e.setAttribute(IS_SYMBOLIC_LINK_ATTR, isSymbolicLink);
		e.setAttribute(FILE_ATTR, file);
		e.setAttribute(NAME_ATTR, file.getName());
		e.setAttribute(PARENT_ATTR, file.getParent());
		e.setAttribute(IS_READ_ONLY_ATTR, isReadOnly(file));
		e.setAttribute(IS_HIDDEN_ATTR, file.isHidden());

		// If File.isDirectory() and File.isFile() have the same value, there is
		// a problem with File System! In this case the 'isDirecotry' attribute
		// is left null.
		if (file.isDirectory() != file.isFile()) {
			e.setAttribute(IS_DIRECTORY_ATTR, file.isDirectory());
		}
		e.setAttribute(LAST_MODIFIED_ATTR, new Date(file.lastModified()));
		e.setAttribute(LENGHT_ATTR, file.length());
		return e;
	}

	/**
	 * Check given a given file has a Symbolic Link.
	 * 
	 * @param file
	 *            to be checked for Symbolic Link.
	 * @return true if this file is a Symbolic Link, else false.
	 * @throws Exception
	 *             When file does not exist.
	 */
	private boolean isSymbolicLink(File file) throws Exception {
		boolean symbolicLink = false;
		if (!file.getAbsolutePath().equals(file.getCanonicalPath())) {
			File fileInCanonicalDir = null;
			if (file.getParent() == null) {
				fileInCanonicalDir = file;
			} else {
				File canonicalDir = file.getParentFile().getCanonicalFile();
				fileInCanonicalDir = new File(canonicalDir, file.getName());
			}
			if (!fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile())) {
				symbolicLink = true;
			}
		}
		return symbolicLink;
	}

	/**
	 * Determine current File depth in directory tree.
	 * 
	 * @param file
	 *            Source File used to determinate the depth.
	 * @return Current File depth
	 */
	private int getPathDepth(File file) {
		if (file.equals(startDirectory)) {
			return 0;
		}
		int depth = 0;
		String rootFilePath = startDirectory.getAbsolutePath();
		String subFilePath = file.getAbsolutePath();
		for (int i = rootFilePath.length() - 1; i < subFilePath.length(); i++) {
			if (subFilePath.charAt(i) == File.separatorChar) {
				depth++;
			}
		}
		return depth;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Entry findEntry(SearchCriteria searchCrit) throws Exception {
		clearFindEntries();

		String foundFullPath = getFullPath(searchCrit);
		Entry entry = null;
		if (foundFullPath != null) {
			// if fullPath is provided in the Link Criteria we skip the Lookup
			// altogether and return that file or directory (if it matches the
			// rest of the criteria).
			printDebugMessage("CONNECTOR.FILEMANAGEMENT.FULL.PATH.CRITERIA.FOUND", foundFullPath);
			File file = new File(foundFullPath);
			if (file.exists()) {
				entry = generateEntry(file, isSymbolicLink(file));
				if (checkCriteria(entry, searchCrit)) {
					addFindEntry(entry);
				}
			}
		} else {
			// perform a search on the directory tree
			selectEntries();
			filesOnly = false; // this option is only relevant for Iterator mode
			while ((entry = getNextEntry()) != null) {
				if (checkCriteria(entry, searchCrit)) {
					addFindEntry(entry);
				}
			}
		}

		if (getFindEntryCount() == 1) {
			return getFirstFindEntry();
		} else {
			return null;
		}
	}

	/**
	 * Check criteria if it has a fullPath element.
	 * 
	 * @param searchCrit
	 *            to be checked
	 * @return the fullPath attribute's value from the criteria or null if none
	 *         is found.
	 */
	private String getFullPath(SearchCriteria searchCrit) {
		Vector<?> criteria = searchCrit.getCriteria();
		int i = 0;
		String foundFullPath = null;
		if (searchCrit.getType() == SearchCriteria.SEARCH_AND) {
			while (i < criteria.size() && foundFullPath == null) {
				rscSearch rscSearch = (rscSearch) criteria.get(i++);
				if (rscSearch.match == SearchCriteria.EXACT && FULL_PATH_ATTR.equals(rscSearch.name)) {
					foundFullPath = (String) rscSearch.value;
				}
			}
		}
		return foundFullPath;
	}

	/**
	 * Check if the provided Entry and Criteria match.
	 * 
	 * @param entry
	 *            Entry to be checked.
	 * @param searchCriteria
	 *            Criteria that the Entry is matched against.
	 * @return true if the Entry matches the Criteria, otherwise return false.
	 */
	private boolean checkCriteria(Entry entry, SearchCriteria searchCriteria) {
		DefaultSearchCriteriaMatcher matcher = new DefaultSearchCriteriaMatcher();
		matcher.setCaseSensitive(File.separatorChar != '\\');
		return matcher.match(entry, searchCriteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteEntry(Entry entry, SearchCriteria searchCrit) throws Exception {
		File deleteFile = (File) entry.getObject(FILE_ATTR);
		if (!deleteFile.exists()) {
			throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.DOES.NOT.EXIST", deleteFile));
		}

		boolean isReadOnly = isReadOnly(deleteFile);

		if (deleteFile.isFile()) {
			if (forceDelete || !isReadOnly) {
				forceDelete(deleteFile);
				if (isReadOnly) {
					printDebugMessage("CONNECTOR.FILEMANAGEMENT.DELETED.READ.ONLY.FILE", deleteFile);
				}
			} else {
				throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.FILE.IS.READ.ONLY", deleteFile));
			}
		}
		if (deleteFile.isDirectory()) {
			boolean isEmpty = isEmptyDirectory(deleteFile);
			if (forceDelete || isEmpty) {
				forceDelete(deleteFile);
				if (!isEmpty) {
					printDebugMessage("CONNECTOR.FILEMANAGEMENT.DELETED.NON.EMPTY.DIRECTORY", deleteFile);
				}
			} else {
				throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.DIRECTORY.IS.NOT.EMPTY", deleteFile));
			}
		}
	}

	/**
	 * Delete the file or directory which path is provided. Both read-only files
	 * and non-empty directories will be deleted.
	 * 
	 * @param filePathToDelete
	 *            to be deleted.
	 * @throws Exception
	 *             if the file or directory cannot be deleted.
	 */
	public void forceDelete(String filePathToDelete) throws Exception {
		forceDelete(new File(filePathToDelete));
	}

	/**
	 * Delete the given file or directory. Both read-only files and non-empty
	 * directories will be deleted.
	 * 
	 * @param fileToDelete
	 *            to be deleted.
	 * @throws Exception
	 *             if the file or directory cannot be deleted.
	 */
	public void forceDelete(File fileToDelete) throws Exception {
		try {
			FileUtils.forceDelete(fileToDelete);
		} catch (Exception e) {
			throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.FORCE.DELETE.FAIL", fileToDelete), e);
		}
	}

	/**
	 * Check file if it is read-only
	 * 
	 * @param file
	 *            to be checked
	 * @return true if file is read-only, else false.
	 */
	private boolean isReadOnly(File file) {
		return file.canRead() && !file.canWrite();
	}

	/**
	 * Check if directory has content.
	 * 
	 * @param directory
	 *            to be checked.
	 * @return true if directory is empty, else false;
	 */
	private boolean isEmptyDirectory(File directory) {
		String[] directoryList = directory.list();
		return directoryList != null && (directoryList.length == 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putEntry(Entry newEntry) throws Exception {
		File newFile = null;
		String fullPath = newEntry.getString(FULL_PATH_ATTR);
		String localName = newEntry.getString(NAME_ATTR);
		if (fullPath != null) {
			newFile = new File(fullPath);
		} else if (localName != null) {
			String parent = newEntry.getString(PARENT_ATTR);
			if (parent != null) {
				newFile = new File(parent, localName);
			} else {
				if (startDirectory.isFile()) {
					throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.PATH.IS.NOT.DIRECTORY", startDirectory));
				}
				newFile = new File(startDirectory, localName);
			}
		} else {
			throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.FILE.OR.DIRECTORY.INFORMATION.IS.NOT.PROVIDED"));
		}

		if (newFile.exists()) {
			throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.FILE.OR.DIRECTORY.ALREADY.EXISTS", newFile));
		}

		if (isDirectory(newEntry)) {
			if (!newFile.mkdirs()) {
				throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.DIRECTORY.IS.NOT.CREATED", newFile));
			}
		} else {
			createParentPath(newFile);
			if (!newFile.createNewFile()) {
				throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.FILE.IS.NOT.CREATED", newFile));
			}
			if (newEntry.getObject(CONTENT_ATTR) != null) {
				addContent(newFile, newEntry);
			}
		}

		Boolean isReadOnly = (Boolean) newEntry.getObject(IS_READ_ONLY_ATTR);
		if (isReadOnly != null) {
			if (isReadOnly) {
				if (!newFile.setReadOnly()) {
					throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.COULD.NOT.BE.SET.READ.ONLY"));
				}
			} else {
				newFile.setWritable(true);
			}
		}
	}

	/**
	 * Add content if exist to new file.
	 * 
	 * @param newFile
	 *            that take the content.
	 * @param newEntry
	 *            to be checked for new content.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void addContent(File newFile, Entry newEntry) throws Exception {
		Object content = newEntry.getObject(CONTENT_ATTR);
		byte[] contentBytes = null;
		if (content instanceof byte[]) {
			contentBytes = (byte[]) content;
		} else if (content instanceof String) {
			Charset charSet = null;
			String charSetString = newEntry.getString(CHAR_SET_ATTR);
			if (charSetString != null) {
				charSet = Charset.forName(charSetString);
			} else {
				charSet = Charset.defaultCharset();
			}
			contentBytes = ((String) content).getBytes(charSet);
		}
		if (contentBytes != null) {
			FileOutputStream fileOS = null;
			try {
				fileOS = new FileOutputStream(newFile);
				fileOS.write(contentBytes);
			} finally {
				if (fileOS != null) {
					fileOS.close();
				}
			}
		}
	}

	/**
	 * Check parent directory of the file and if it does not exist, create it.
	 * 
	 * @param file
	 *            Which file's path to be checked.
	 * @throws Exception
	 *             if parent directory cannot be created.
	 */
	private void createParentPath(File file) throws Exception {
		File parentPath = file.getParentFile();
		if (!parentPath.exists() && !parentPath.mkdirs()) {
			throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.DIRECTORY.IS.NOT.CREATED", parentPath));
		}
	}

	/**
	 * Create java.io.File from the provided Entry.
	 * 
	 * @param oldFile
	 *            file used to get old path or old local name.
	 * @param newEntry
	 *            Entry that must be created.
	 * @return File generate from Entry.
	 */
	private File createFile(File oldFile, Entry newEntry) {
		File newFile = null;
		String fullPath = newEntry.getString(FULL_PATH_ATTR);
		String localName = newEntry.getString(NAME_ATTR);
		String parent = newEntry.getString(PARENT_ATTR);
		if (fullPath != null) {
			newFile = new File(fullPath);
		} else if (parent != null && localName != null) {
			newFile = new File(parent, localName);
		} else {
			if (parent != null) {
				newFile = new File(parent, oldFile.getName());
			}
			if (localName != null) {
				newFile = new File(oldFile.getParent(), localName);
			}
		}
		return newFile;
	}

	/**
	 * Check if the provided Entry is a Directory.
	 * 
	 * @param entry
	 *            Entry to be checked.
	 * @return true if Entry is Directory, else return false.
	 */
	private boolean isDirectory(Entry entry) {
		Boolean isDirectory = (Boolean) entry.getObject(IS_DIRECTORY_ATTR);
		if (isDirectory == null) {
			isDirectory = !Boolean.parseBoolean(getParam(CREATE_FILE_PARAM_NAME));
		}
		return isDirectory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modEntry(Entry newEntry, SearchCriteria searchCrit) throws Exception {
		modEntry(newEntry, searchCrit, findEntry(searchCrit));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modEntry(Entry newEntry, SearchCriteria searchCrit, Entry oldEntry) throws Exception {
		File oldFile = (File) oldEntry.getObject(FILE_ATTR);
		File newFile = createFile(oldFile, newEntry);
		boolean hasContent = newEntry.getObject(CONTENT_ATTR) != null;
		if (newFile != null) {
			if (keepOriginal) {
				try {
					if (oldFile.isDirectory()) {
						FileUtils.copyDirectory(oldFile, newFile);
					} else {
						FileUtils.copyFile(oldFile, newFile);
					}
				} catch (Exception e) {
					throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.COPY.OPERATION.FAIL", new Object[] { oldFile,
							newFile }), e);
				}
			} else {
				try {
					if (oldFile.isDirectory()) {
						createParentPath(newFile);
						FileUtils.moveDirectory(oldFile, newFile);
					} else {
						createParentPath(newFile);
						FileUtils.moveFile(oldFile, newFile);
					}
				} catch (Exception e) {
					throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.MOVE.OPERATION.FAIL", new Object[] { oldFile,
							newFile }), e);
				}
			}
			if (hasContent && newFile.isFile()) {
				addContent(newFile, newEntry);
			}
		} else {
			if (hasContent && oldFile.isFile()) {
				addContent(oldFile, newEntry);
			} else {
				throw new Exception(resHash.getString("CONNECTOR.FILEMANAGEMENT.FILE.OR.DIRECTORY.INFORMATION.IS.NOT.PROVIDED"));
			}
		}
	}

	/**
	 * Prints a debug message if debug mode for the Components is enabled.
	 * 
	 * @param msgKey
	 *            message key
	 * @param params
	 *            place holder for debug messages
	 */
	private void printDebugMessage(String msgKey, Object... params) {
		if (params == null || params.length == 0) {
			debug(resHash.getString(msgKey));
		} else if (params.length == 1) {
			debug(resHash.getString(msgKey, params[0]));
		} else {
			debug(resHash.getString(msgKey, params));
		}
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "1.0-di7.1.1 %I%, 20%E%";
	}
}