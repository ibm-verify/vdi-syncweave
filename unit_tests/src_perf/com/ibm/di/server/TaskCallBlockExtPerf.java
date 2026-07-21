package com.ibm.di.server;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.entry.Entry;
import com.ibm.di.entry.EntryExtPerf.NOOPOutputStream;
import com.ibm.di.test.framework.perf.RepeatConstants;
import com.ibm.di.test.utils.TestUtils;

public class TaskCallBlockExtPerf {
	private static TaskCallBlock tcb = null;
	private static byte[] serializedTCB = null;

	@BeforeClass
	public static void initTaskCallBlock() {
		tcb = new TaskCallBlock();
		tcb.setInitialWorkEntry(new Entry());
		tcb.setAttribute("attr", "val");

		serializedTCB = TestUtils.serializeObject(tcb);
	}

	@Test
	public void test_readObject() throws Exception { // 3 min
		ByteArrayInputStream bis = new ByteArrayInputStream(serializedTCB);
		for (long i = 0; i < RepeatConstants.get2m(); ++i) {
			bis.reset();
			ObjectInputStream objInp = new ObjectInputStream(bis);
			TaskCallBlock tcb = (TaskCallBlock) objInp.readObject();
			objInp.close();
		}
		bis.close();
	}

	@Test
	public void test_writeObject() throws Exception { // 4,9 min
		for (long i = 0; i < RepeatConstants.get15m(); ++i) {
			ObjectOutputStream objOut = new ObjectOutputStream(new NOOPOutputStream());
			objOut.writeObject(tcb);
			objOut.flush();
			objOut.close();
		}
	}
}
