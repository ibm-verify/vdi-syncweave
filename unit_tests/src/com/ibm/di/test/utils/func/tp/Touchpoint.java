package com.ibm.di.test.utils.func.tp;

import org.apache.wink.common.model.atom.AtomEntry;

import com.ibm.di.test.http.HttpClientContext;
import com.ibm.di.test.tp.TpAppHelper;

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
public abstract class Touchpoint {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	protected final HttpClientContext ctx;
	protected final TpAppHelper app;
	protected final String instanceEntryUrl;

	public Touchpoint(HttpClientContext ctx, String instanceEntryUrl) {
		this.ctx = ctx;
		app = new TpAppHelper(ctx);
		this.instanceEntryUrl = instanceEntryUrl;
	}

	public String getInstanceEntryUrl() {
		return instanceEntryUrl;
	}

	protected void putInstanceEntry(AtomEntry instEntry) throws Exception {
		app.putAtomEntry(instEntry);
	}

	protected AtomEntry getInstanceEntry() throws Exception {
		return app.getAtomEntry(instanceEntryUrl);
	}

	public boolean getEnabled() throws Exception {
		return TpAppHelper.getInstEnabled(getInstanceEntry());
	}

	public boolean getAvailable() throws Exception {
		return app.isTouchpointInstanceAvailable(getInstanceEntry());
	}

	public void setEnabled(boolean enabled) throws Exception {
		if (ctx == null) {
			throw new UnsupportedOperationException();
		}

		AtomEntry instEntry = getInstanceEntry();

		// this will change the isntEntry state.
		TpAppHelper.setInstEnabled(instEntry, enabled);

		// update the server's entry
		putInstanceEntry(instEntry);
	}

	public void deleteTouchpoint() throws Exception {
		if (ctx == null) {
			throw new UnsupportedOperationException();
		}

		AtomEntry instEntry = getInstanceEntry();

		// delete the server's entry
		app.deleteInstEntry(instEntry);
	}
}
