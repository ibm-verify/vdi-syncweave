package com.ibm.di.fc;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.config.base.FunctionConfigImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.TaskStatistics;
import com.ibm.di.test.framework.perf.RepeatConstants;
import com.ibm.di.test.utils.TestUtils;

public class DeltaFCExtPerf {

	private static DeltaFC deltaFC = null;
	private static String deltaStoreName = "Test456";
	private static String uniqueKey = "id";
	private static long numEntries = RepeatConstants.get250k();

	private static Entry e = null;
	private static Entry b = null;

	@BeforeClass
	public static void initalize() throws Exception {
		configureDerbyDeltaStore();
		// configureSolidDBDeltaStore();
		initDeltaFC();

		e = TestUtils.createFlatEntry(3, "");
		b = TestUtils.createFlatEntry(3, "pref");
	}

	public static void initDeltaFC() throws Exception {
		FunctionConfig fc = new FunctionConfigImpl();
		fc.init();
		fc.setName("deltaFC");
		fc.getFunctionConfig().setParameter("javaclass", "com.ibm.di.fc.DeltaFC");

		FunctionInterface function = com.ibm.di.function.SystemFunctions.loadFunction(fc);
		function.setParam(InternalSchema.CONNECTOR_DELTA_UNIQUE_ATTR, uniqueKey);
		function.setParam(InternalSchema.CONNECTOR_DELTA_DB, deltaStoreName);
		function.setParam(InternalSchema.CONNECTOR_DELTA_ITER_DELETED, "true");
		function.setParam(InternalSchema.CONNECTOR_DELTA_REMOVE_DELETED, "true");
		function.setParam(InternalSchema.CONNECTOR_DELTA_RETURN_UNCHANGED, "true");
		function.setParam(InternalSchema.CONNECTOR_DELTA_ALLOW_DUPLICATE_KEYS, "false");
		function.initialize(null);
		
		deltaFC = (DeltaFC) function;
		deltaFC.stats = new TaskStatistics();
	}

	public static void configureDerbyDeltaStore() {
		System.setProperty("com.ibm.di.store.jdbc.driver", "org.apache.derby.jdbc.ClientDriver");
		System.setProperty("com.ibm.di.store.jdbc.user", "APP");
		System.setProperty("com.ibm.di.store.jdbc.password", "APP");
		System.setProperty("com.ibm.di.store.jdbc.host", "localhost");
		System.setProperty("com.ibm.di.store.jdbc.port", "1527");
		System.setProperty("com.ibm.di.store.jdbc.urlprefix", "jdbc:derby://localhost:1527/");
		System.setProperty("com.ibm.di.store.database", "jdbc:derby://localhost:1527/D:\\dev\\TestSysStore1;create=true");
		System.setProperty("com.ibm.di.store.sysibm", "true");
		System.setProperty("com.ibm.di.store.varchar.length", "512");
		System.setProperty("com.ibm.di.store.create.delta.store",
				"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL PRIMARY KEY, SEQUENCEID int, ENTRY BLOB );");
		System
				.setProperty(
						"com.ibm.di.store.create.delta.systable",
						"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, VERSION int);ALTER TABLE {0} ADD CONSTRAINT IDI_CS_{UNIQUE} PRIMARY KEY (ID)");

	}

	public static void configureSolidDBDeltaStore() {
		System.setProperty("com.ibm.di.store.jdbc.driver", "solid.jdbc.SolidDriver");
		System.setProperty("com.ibm.di.store.jdbc.user", "dba");
		System.setProperty("com.ibm.di.store.jdbc.password", "dba");
		System.setProperty("com.ibm.di.store.jdbc.host", "localhost");
		System.setProperty("com.ibm.di.store.jdbc.port", "2315");
		System.setProperty("com.ibm.di.store.jdbc.urlprefix", "");
		System.setProperty("com.ibm.di.store.database", "jdbc:solid://localhost:2315");
		System.setProperty("com.ibm.di.store.sysibm", "false");
		System.setProperty("com.ibm.di.store.varchar.length", "512");
		System.setProperty("com.ibm.di.store.create.delta.store",
				"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL PRIMARY KEY, SEQUENCEID int, ENTRY BLOB );");
		System.setProperty("com.ibm.di.store.create.delta.systable",
				"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL PRIMARY KEY, SEQUENCEID int, VERSION int);");

	}

	@Test
	public void testDeltaFCPerformNewEntry() throws Exception {
		for (long i = 0; i < numEntries; ++i) {
			e.setAttribute(uniqueKey, "" + i);
			deltaFC.perform(e);
		}
	}

	@Test
	public void testDeltaFCPerformUpdatedEntry() throws Exception {
		// restart delta fc to increment sequenceid
		initDeltaFC();
		for (long i = 0; i < numEntries; ++i) {
			b.setAttribute(uniqueKey, "" + i);
			deltaFC.perform(b);
		}
	}

	@Test
	public void testDeltaFCRollbackDeltaState() throws Exception {
		// restart delta engine to increment sequenceid
		initDeltaFC();
		for (long i = 0; i < numEntries; ++i) {
			e.setAttribute(uniqueKey, "" + i);
			deltaFC.perform(e);
			deltaFC.rollbackDeltaState();
		}
	}

	@Test
	public void testDeltaFCMarkEntryInDeltaStore() throws Exception {
		// restart delta engine to increment sequenceid
		initDeltaFC();
		for (long i = 0; i < numEntries; ++i) {
			e.setAttribute(uniqueKey, "" + i);
			deltaFC.markEntryInDeltaStore(e);
		}
	}

	@Test
	public void testDeltaFCSaveDeltaState() throws Exception {
		// restart delta engine to increment sequenceid
		initDeltaFC();
		for (long i = 0; i < numEntries; ++i) {
			b.setAttribute(uniqueKey, "" + i);
			deltaFC.perform(b);
			deltaFC.saveDeltaState();
		}
	}

	@Test
	public void testDeltaFCPerformDeletedEntries() throws Exception {
		// restart delta engine to increment sequenceid
		initDeltaFC();

		// modify one entry
		e.setAttribute(uniqueKey, "1");
		deltaFC.perform(e);

		do {
			// pass empty entry and start iterating over deleted entries
			e = (Entry) deltaFC.perform(null);
		} while (e != null && e.size() > 0);

	}

	@AfterClass
	public static void closeDeltaStore() throws Exception {
		if (deltaFC != null) {
			deltaFC.closeDelta();
		}
	}
}
