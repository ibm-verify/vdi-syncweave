/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.jaxrs.storage.atom.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.naming.InvalidNameException;

import com.ibm.di.jaxrs.storage.atom.StorageException;

/**
 * Implementation of storage for binary data. Each key is treated like a file
 * system folder structure using forward slash as a separator (back slashes are
 * forbidden). Value associated with a key is stored in a file named
 * {@link #CONTENT_FILE_NAME}.
 * 
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class RawDataStorageFileSystemImpl implements RawDataStorage {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * The name of the file which contains the value associated with a key.
	 */
	public static final String CONTENT_FILE_NAME = "{content}.xml";

	/**
	 * Root folder where data is stored.
	 */
	private File storageLocation;

	/**
	 * Create storage which uses the specified file system folder.
	 * 
	 * @param rootDir
	 *            Storage location.
	 * @throws StorageException
	 *             The storage location does not exist and cannot be created or
	 *             exists an is not a folder.
	 */
	public RawDataStorageFileSystemImpl(File storageLocation) throws StorageException {
		this.storageLocation = storageLocation;
		if (!storageLocation.exists() && !storageLocation.mkdirs()) {
			throw new StorageException(AtomStorageImpl.L10N.getString("RAW.DATA.FS.STORAGE.LOCATION.DOES.NOT.EXIST",
					storageLocation.getAbsolutePath()));
		}
		if (!storageLocation.isDirectory()) {
			throw new StorageException(AtomStorageImpl.L10N.getString("RAW.DATA.FS.STORAGE.LOCATION.NOT.FOLDER",
					storageLocation.getAbsolutePath()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] get(String key) throws StorageException, InvalidNameException {

		validateKey(key);

		File keyFolder = getFolderForKey(key);

		File contentFile = new File(keyFolder, CONTENT_FILE_NAME);

		byte[] data;
		if (contentFile.exists()) {
			try {
				data = readData(contentFile);
			} catch (IOException io) {
				throw new StorageException(AtomStorageImpl.L10N.getString("RAW.DATA.FS.STORAGE.ERROR.READ.CONTENT.FILE",
						new Object[] { key, contentFile.getAbsolutePath(), io }), io);
			}
		} else {
			data = null;
		}

		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	public void remove(String key) throws StorageException, InvalidNameException {
		validateKey(key);
		File keyFolder = getFolderForKey(key);
		File contentFile = new File(keyFolder, CONTENT_FILE_NAME);
		deleteFile(contentFile);
		garbageCollectKeyFolder(keyFolder);
	}

	/**
	 * {@inheritDoc}
	 */
	public void put(String key, byte[] data) throws StorageException, InvalidNameException {

		validateKey(key);

		File keyFolder = getFolderForKey(key);

		// make sure the folders exist along the way
		if (!keyFolder.exists()) {
			boolean mkdirs = keyFolder.mkdirs();
			if (!mkdirs) {
				throw new StorageException(AtomStorageImpl.L10N.getString("RAW.DATA.FS.STORAGE.ERROR.MKDIRS",
						keyFolder.getAbsolutePath()));
			}
		} else if (!keyFolder.isDirectory()) {
			throw new StorageException(AtomStorageImpl.L10N.getString("RAW.DATA.FS.STORAGE.KEY.NOT.FOLDER",
					keyFolder.getAbsolutePath()));
		}

		File contentFile = new File(keyFolder, CONTENT_FILE_NAME);
		if (data != null) {
			try {
				writeData(contentFile, data);
			} catch (IOException io) {
				throw new StorageException(AtomStorageImpl.L10N.getString("RAW.DATA.FS.STORAGE.ERROR.WRITE.CONTENT.FILE",
						new Object[] { contentFile.getAbsolutePath(), io }), io);
			}
		} else {
			// nullify the node data - delete its content file
			if (contentFile.exists()) {
				deleteFile(contentFile);
			}
			garbageCollectKeyFolder(keyFolder);
		}
	}

	/**
	 * Validate the syntax of a key.
	 * 
	 * @param key
	 *            Key.
	 * @throws InvalidNameException
	 *             The key is invalid.
	 */
	private void validateKey(String key) throws InvalidNameException {

		if (key == null || "".equals(key.trim())) {
			throw new InvalidNameException(AtomStorageImpl.L10N.getString("RAW.DATA.FS.STORAGE.KEY.CANNOT.BE.EMPTY"));
		}

		String path = key.trim();

		if ("/".equals(path)) {
			throw new InvalidNameException(AtomStorageImpl.L10N.getString("RAW.DATA.FS.STORAGE.KEY.CANNOT.BE.SINGLE.SLASH"));
		}

		if (path.contains("//")) {
			throw new InvalidNameException(AtomStorageImpl.L10N.getString(
					"RAW.DATA.FS.STORAGE.KEY.CANNOT.CONTAIN.CONSECUTIVE.SLASHES", path));
		}

		if (path.contains("\\")) {
			throw new InvalidNameException(AtomStorageImpl.L10N.getString(
					"RAW.DATA.FS.STORAGE.KEY.CANNOT.CONTAIN.BACK.SLASHES", path));
		}

		if (path.contains("{") || path.contains("}")) {
			throw new InvalidNameException(AtomStorageImpl.L10N.getString(
					"RAW.DATA.FS.STORAGE.KEY.CANNOT.CONTAIN.CURLY.BRACKETS", path));
		}

		for (String token : path.split("/")) {
			if (".".equals(token)) {
				throw new InvalidNameException(AtomStorageImpl.L10N.getString(
						"RAW.DATA.FS.STORAGE.KEY.COMPONENT.CANNOT.BE.SINGLE.DOT", path));
			}
			if ("..".equals(token)) {
				throw new InvalidNameException(AtomStorageImpl.L10N.getString(
						"RAW.DATA.FS.STORAGE.KEY.COMPONENT.CANNOT.BE.DOUBLE.DOTS", path));
			}
		}
	}

	/**
	 * @param key
	 *            Key.
	 * @return Folder which corresponds to the node.
	 */
	private File getFolderForKey(String key) {
		return new File(storageLocation, key.trim());
	}

	/**
	 * Read the binary content of the specified file.
	 * 
	 * @param f
	 *            File.
	 * @return The content of the file.
	 * @throws IOException
	 *             I/O error.
	 */
	private byte[] readData(File f) throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		FileInputStream fis = new FileInputStream(f);
		try {

			byte[] buffer = new byte[1024];
			int byteCount = 0;

			while ((byteCount = fis.read(buffer)) > 0) {
				bos.write(buffer, 0, byteCount);
			}
		} finally {
			fis.close();
		}

		return bos.toByteArray();
	}

	/**
	 * Write binary data to the specified file.
	 * 
	 * @param f
	 *            File.
	 * @param data
	 *            Data.
	 * @throws IOException
	 *             I/O error.
	 */
	private void writeData(File f, byte[] data) throws IOException {
		FileOutputStream fos = new FileOutputStream(f);
		try {
			fos.write(data);
		} finally {
			fos.close();
		}
	}

	/**
	 * Delete a file or folder.
	 * 
	 * @param f
	 *            File or folder.
	 * 
	 * @throws StorageException
	 *             If the file/folder cannot be deleted.
	 */
	private void deleteFile(File f) throws StorageException {
		if (!f.exists()) {
			return;
		}
		boolean deleted = f.delete();
		if (!deleted) {
			throw new StorageException(AtomStorageImpl.L10N.getString("RAW.DATA.FS.STORAGE.CANNOT.DELETE.FILE", f.getAbsolutePath()));
		}
	}

	/**
	 * Recursively delete the specified file/folder.
	 * 
	 * @param f
	 *            File or folder.
	 * 
	 * @throws StorageException
	 *             Could not delete a file or folder.
	 */
	private void recursiveDelete(File f) throws StorageException {
		if (!f.exists()) {
			return;
		}
		if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				recursiveDelete(child);
			}
		}
		deleteFile(f);
	}

	/**
	 * If the folder for the key does not contain the folders of other keys and
	 * the key does not have a value - delete it.
	 * 
	 * @param keyFolder
	 *            Folder for a key.
	 */
	private void garbageCollectKeyFolder(File keyFolder) {
		File[] children = keyFolder.listFiles();
		boolean folderIsEmpty = (children == null || children.length == 0);
		if (folderIsEmpty) {
			deleteFile(keyFolder);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() throws StorageException {
		recursiveDelete(storageLocation);
	}
}
