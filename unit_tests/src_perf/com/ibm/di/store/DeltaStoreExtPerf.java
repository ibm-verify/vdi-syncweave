package com.ibm.di.store;

import java.lang.reflect.Method;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.test.framework.perf.RepeatConstants;
import com.ibm.di.test.utils.TestUtils;

public class DeltaStoreExtPerf {

	private static DeltaStore delta = null;
	private static Entry e = null;
	private static Entry b = null;
	private static String uniqueKey = "id";
	private static String dbName = "Test123";
	private static final long numEntries = RepeatConstants.get250k();

	@BeforeClass
	public static void initialize() throws Exception {
		configureDerbyDeltaStore();
		initDelta(dbName, false);

		e = TestUtils.createFlatEntry(3, "");
		b = TestUtils.createFlatEntry(3, "pref");
	}

	public static void configureDerbyDeltaStore() {
		System.setProperty("com.ibm.di.store.jdbc.driver", "org.apache.derby.jdbc.ClientDriver");
		System.setProperty("com.ibm.di.store.jdbc.user", "APP");
		System.setProperty("com.ibm.di.store.jdbc.password", "APP");
		System.setProperty("com.ibm.di.store.jdbc.host", "localhost");
		System.setProperty("com.ibm.di.store.jdbc.port", "1527");
		System.setProperty("com.ibm.di.store.jdbc.urlprefix", "jdbc:derby://localhost:1527/");
		System.setProperty("com.ibm.di.store.database", "jdbc:derby://localhost:1527/D:\\dev\\TestSysStore;create=true");
		System.setProperty("com.ibm.di.store.sysibm", "true");
		System.setProperty("com.ibm.di.store.varchar.length", "512");
		System.setProperty(
						"com.ibm.di.store.create.delta.store",
						"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, ENTRY BLOB );ALTER TABLE {0} ADD CONSTRAINT IDI_DS_{UNIQUE} Primary Key (ID)");
		System.setProperty(
						"com.ibm.di.store.create.delta.systable",
						"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, VERSION int);ALTER TABLE {0} ADD CONSTRAINT IDI_CS_{UNIQUE} PRIMARY KEY (ID)");
	}
	
	public static void initDelta(String dbName, boolean removeDeleted) throws Exception {
		if(removeDeleted) {
			try {
				// try the new method
				Method m = (DeltaSysTable.class).getMethod("getDeltaStore", String.class, boolean.class, Log.class, boolean.class);
				delta = (DeltaStore) m.invoke(delta, dbName, false, null, removeDeleted);
			} catch (NoSuchMethodException nsme) {
				// if not found continue with the old one
				delta = DeltaSysTable.getDeltaStore(dbName, false);
			}
		} else {
			delta = DeltaSysTable.getDeltaStore(dbName, false);
		}
		
		if (delta != null) {
			delta.setAllowDuplicateDeltaKeys(false);
			delta.setCommitMode("After every database operation");
			delta.setAllowDuplicateDeltaKeys(true);
		}
	}

	@Test
	public void testDeltaStoreInsertEntry() throws Exception {
		for (long i = 0; i < numEntries; ++i) {
			e.setAttribute(uniqueKey, "" + i);
			delta.insertEntry("" + i, e);
		}
	}

	@Test
	public void testDeltaStoreFindEntry() throws Exception {
		for (long i = 0; i < numEntries; ++i) {
			delta.findEntryVerify("" + i);
		}
	}

	@Test
	public void testDeltaStoreUpdateEntry() throws Exception {
		for (long i = 0; i < numEntries; ++i) {
			b.setAttribute(uniqueKey, "" + i);
			delta.updateEntry("" + i, b);
		}
	}

	@Test
	public void testDeltaStoreUpdateSequence() throws Exception {
		for (long i = 0; i < numEntries; ++i) {
			delta.updateSequence("" + i);
		}
	}
	
	@Test
	public void testDeltaStoreGetNextDeletedEntry() throws Exception {
		initDelta(dbName, false);

		// update only one Entry
		// all other will be considered deleted
		delta.updateSequence("1"); 
		
		delta.selectDeletedEntries();
		while (delta.getNextDeletedEntry(false) != null) {}
	}
	
	@Test
	public void testDeltaStoreDeleteNextEntry() throws Exception {
		initDelta(dbName, true);

		// update only one Entry
		// all other will be considered deleted
		delta.updateSequence("1"); 
		
		// select all deleted entries
		delta.selectDeletedEntries();
		while (delta.getNextDeletedEntry(true) != null) {}
		
		// leave empty delta store
		delta.deleteEntry("1");
	}

	@AfterClass
	public static void closeDeltaStore() throws Exception {
		if (delta != null) {
			DeltaSysTable.delete(dbName);
			delta.closeDelta();
		}
	}
}
