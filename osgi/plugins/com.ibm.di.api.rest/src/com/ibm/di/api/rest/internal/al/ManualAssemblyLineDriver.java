/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.al;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.ibm.di.web.common.atom.AtomText;
import com.ibm.di.web.common.atom.AtomTextType;//import java.util.concurrent.Future;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.ALHandle;
import com.ibm.di.api.bind.ALHandleStateEnum;
import com.ibm.di.api.bind.BindUtil;
import com.ibm.di.api.remote.AssemblyLine;
import com.ibm.di.api.remote.AssemblyLineHandler;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.entry.Entry;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class ManualAssemblyLineDriver {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final AssemblyLineHandler alh;

	private final ExecutorService executor;

//	private Future<Entry> future;

	private ExecCycle reusedCallable = new ExecCycle();

	private volatile boolean closed = false;

	private Entry entry;

	public ManualAssemblyLineDriver(AssemblyLineHandler alh, ExecutorService executor) {
		this.alh = alh;
		this.executor = executor;
	}

	public void executeCycle() throws DIException, RemoteException {
		executeCycle(null);
	}

	public boolean executeCycle(Entry e) throws DIException, RemoteException {
		return executeCycle(null, false);
	}

	public boolean executeCycle(Entry e, boolean processTcb) throws DIException, RemoteException {
		synchronized (alh) {
			if (closed) {
				throw new DIException(AppConstants.L10N.getString("REST.API.AL.HANDLE.CLOSED"));
			}

//			if (future != null && !future.isDone()) {
//				return false;
//			}

			reusedCallable.e = e;
			reusedCallable.processTcb = processTcb;
//			future = executor.submit(reusedCallable);
			try {
				this.entry = reusedCallable.call();
			} catch (Exception err) {
				throw new DIException(err);
			}
		}
		return true;
	}

	public AssemblyLine getAssemblyLine() throws DIException, RemoteException {
		return alh.getAssemblyLine();
	}

	public void close() throws DIException, RemoteException {
		if (!closed) {
			synchronized (alh) {
				if (!closed) {
//					if (future != null) {
//						future.cancel(true);
//						future = null;
//					}

					closed = true;
					// call this one synchronously
					alh.close();
				}
			}
		}
	}

	public ALHandle getStatus() throws DIException, RemoteException {
		ALHandle stat = new ALHandle();

		synchronized (alh) {
			if (closed) {
				stat.setState(ALHandleStateEnum.CLOSED);
			} else {
				stat.setState(ALHandleStateEnum.DONE);
				if(this.entry != null)
					stat.setResultEntry(BindUtil.fromEntry(this.entry));
			}

//			if (future == null) {
//				stat.setState(ALHandleStateEnum.INIT);
//			} else if (!future.isDone()) {
//				stat.setState(ALHandleStateEnum.PROCESSING);
//			} else {
//				stat.setState(ALHandleStateEnum.DONE);
//				try {
//					stat.setResultEntry(BindUtil.fromEntry(future.get()));
//				} catch (ExecutionException e) {
//					if (e.getCause() instanceof DIException) {
//						throw (DIException) e.getCause();
//					} else if (e.getCause() instanceof RemoteException) {
//						throw (RemoteException) e.getCause();
//					} else if (e.getCause() instanceof RuntimeException) {
//						throw (RuntimeException) e.getCause();
//					} else {
//						throw new RuntimeException(e.getCause());
//					}
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
		}

		return stat;
	}

	private class ExecCycle implements Callable<Entry> {

		private Entry e;
		private boolean processTcb;

		public ExecCycle() {
		}

		public Entry call() throws Exception {
			return e != null ? (processTcb ? alh.executeCycle(e, true) : alh.executeCycle(e)) : alh.executeCycle();
		}
	}

	/**
	 * @param script
	 * @return
	 */
	public Serializable eval(String script) throws DIException, RemoteException {
		synchronized (alh) {
			if (closed) {
				throw new DIException(AppConstants.L10N.getString("REST.API.AL.HANDLE.CLOSED"));
			}

//			if (future != null && !future.isDone()) {
//				return false;
//			}

			return alh.eval(script);
		}
	}
}
