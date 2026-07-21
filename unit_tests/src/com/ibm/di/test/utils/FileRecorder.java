package com.ibm.di.test.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ibm.di.util.FileUtils;

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
public class FileRecorder {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private int id = 0;

	private final File backupDir;

	private List<Record> records = new ArrayList<Record>();
	
	public FileRecorder(File backupDir) {
		this.backupDir = backupDir;
		this.backupDir.mkdirs();
	}

	public File recordCreateFile(File file) {
		if (file.exists()) {
			throw new IllegalStateException("The file must not exist in order to be created.");
		}
		records.add(new CreateFileRecord(file));
		return file;
	}

	/**
	 * Delegates to {@link #recordModifyFile(File, boolean)} and assumes that
	 * the modifying code will create the missing file.
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public File recordModifyFile(File file) throws Exception {
		return recordModifyFile(file, true);
	}

	/**
	 * Records a file for modification. This method must be called before the
	 * actual modification is made. A check for existence will be made first.
	 * 
	 * @param file
	 *            the file to modify
	 * @param autoCreate
	 *            If the modifying code creates a file when it is missing,
	 *            specify true, otherwise false.
	 * @return
	 * @throws Exception
	 */
	public File recordModifyFile(File file, boolean autoCreate) throws Exception {
		if (!file.exists()) {
			if (!autoCreate) {
				throw new IllegalStateException("The file must exist in order to be able to modify it.");
			} else {
				recordCreateFile(file);
			}
		} else {
			records.add(new BackupFileRecord(file));
		}
		return file;
	}

	public File recordDeleteFile(File file) throws Exception {
		if (!file.exists()) {
			throw new IllegalStateException("The file must exist in order to be deleted.");
		}
		records.add(new DeleteFileRecord(file));
		return file;
	}

	public File recordCreateDir(File dir) {
		if (dir.exists()) {
			throw new IllegalStateException("The directory must not exist in order for it to be created.");
		}

		File parentDir = dir.getParentFile();

		if (!parentDir.exists()) {
			recordCreateDir(parentDir);
		}

		records.add(new CreateDirRecord(dir));
		return dir;
	}

	public File recordModifyDir(File dir, boolean autoCreate) throws Exception {
		if (!dir.exists()) {
			if (!autoCreate) {
				throw new IllegalStateException("The directory must exist in order to be able to modify it.");
			} else {
				recordCreateDir(dir);
			}
		} else {
			records.add(new BackupDirRecord(dir));
		}
		return dir;
	}

	public void rewind() {
		for (int i = records.size() - 1; i >= 0; i--) {
			try {
				records.get(i).rewind();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		records.clear();
	}

	public void destroy() {
		rewind();
		try {
			FileUtils.deleteRecursively(backupDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static interface Record {
		public void rewind() throws Exception;
	}

	private class BackupFileRecord implements Record {
		private final File toBackup;
		private final File backedUp;

		public BackupFileRecord(File toBackup) throws Exception {
			this.toBackup = toBackup;
			this.backedUp = new File(backupDir, Integer.toString(id++) + "." + toBackup.getName());

			if (!FileUtils.copyFile(toBackup, backedUp, true)) {
				throw new IllegalStateException("Couldn't backup up file: " + toBackup);
			}
		}

		public void rewind() throws Exception {
			if (!FileUtils.copyFile(backedUp, toBackup, true)) {
				throw new IllegalStateException("Couldn't restore file: " + backedUp);
			}
		}
	}

	private class CreateFileRecord implements Record {
		private final File fileToCreate;

		public CreateFileRecord(File fileToCreate) {
			this.fileToCreate = fileToCreate;
		}

		public void rewind() throws Exception {
			fileToCreate.delete();
		}
	}

	private class DeleteFileRecord implements Record {
		private final File toBackup;
		private final File backedUp;

		public DeleteFileRecord(File toBackup) throws Exception {
			this.toBackup = toBackup;
			this.backedUp = new File(backupDir, Integer.toString(id++) + "." + toBackup.getName());

			if (!FileUtils.copyFile(toBackup, backedUp, true)) {
				throw new IllegalStateException("Couldn't backup up file: " + toBackup);
			}
		}

		public void rewind() throws Exception {
			if (!FileUtils.copyFile(backedUp, toBackup, true)) {
				throw new IllegalStateException("Couldn't restore file: " + backedUp);
			}
		}
	}

	private class CreateDirRecord implements Record {

		private final File dirToCreate;

		public CreateDirRecord(File dirToCreate) {
			this.dirToCreate = dirToCreate;
		}

		public void rewind() throws Exception {
			FileUtils.deleteRecursively(dirToCreate);
		}
	}

	public class BackupDirRecord implements Record {

		private final File toBackup;
		private final File backedUp;

		/**
		 * @param dir
		 * @throws Exception
		 */
		public BackupDirRecord(File toBackup) throws Exception {
			this.toBackup = toBackup;
			this.backedUp = new File(backupDir, Integer.toString(id++) + "." + toBackup.getName());
			FileUtils.copyDir(this.toBackup, this.backedUp, true);
		}

		public void rewind() throws Exception {
			FileUtils.deleteRecursively(toBackup);
			FileUtils.copyDir(backedUp, toBackup, true);
		}
	}
}
