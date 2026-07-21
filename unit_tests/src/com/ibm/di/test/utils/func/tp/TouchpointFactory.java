package com.ibm.di.test.utils.func.tp;

import java.net.URL;
import java.util.Map;

import org.apache.wink.common.model.atom.AtomEntry;

import com.ibm.di.test.http.FuncTestHttpClientContext;
import com.ibm.di.test.http.HttpClientContext;
import com.ibm.di.test.tp.TpAppHelper;
import com.ibm.di.tp.server.model.TouchpointRole;

public class TouchpointFactory {

	private final HttpClientContext ctx;
	private final TpAppHelper app;

	/**
	 * Creates a factory with a predefined client context.
	 * 
	 * @param ctx
	 */
	public TouchpointFactory(HttpClientContext ctx) {
		this.ctx = ctx;
		this.app = new TpAppHelper(ctx);
	}

	/**
	 * Creates a factory that will communicate with the HTTP Server on specified
	 * by the provided URL.
	 * 
	 * @param serviceDocumentUrl
	 */
	public TouchpointFactory(URL serviceDocumentUrl) {
		FuncTestHttpClientContext httpCtx = new FuncTestHttpClientContext();
		httpCtx.setHttpRootUri(serviceDocumentUrl.toExternalForm());

		ctx = httpCtx;
		app = new TpAppHelper(ctx);
	}

	public ProviderTouchpoint createProviderTouchpoint(String nodeId, String touchpointType, String connectorMode,
			Map<String, String> connectorParams) throws Exception {
		final boolean enabled = true;
		return (ProviderTouchpoint) createTouchpoint(nodeId, touchpointType, connectorParams, TouchpointRole.PROVIDER, enabled);
	}

	public InitiatorTouchpoint createInitiatorTouchpoint(String nodeId, String touchpointType, Map<String, String> connectorParams)
			throws Exception {
		final boolean enabled = true;
		return (InitiatorTouchpoint) createTouchpoint(nodeId, touchpointType, connectorParams, TouchpointRole.INITIATOR, enabled);
	}

	public IntermediaryTouchpoint createIntermediaryTouchpoint(String nodeId, String touchpointType,
			Map<String, String> connectorParams) throws Exception {
		final boolean enabled = true;
		return (IntermediaryTouchpoint) createTouchpoint(nodeId, touchpointType, connectorParams, TouchpointRole.INTERMEDIARY,
				enabled);
	}

	public Touchpoint createTouchpoint(String nodeId, String touchpointType, Map<String, String> connectorParams,
			TouchpointRole role, boolean enabled) throws Exception {
		if (nodeId == null) {
			nodeId = "default";
		}

		AtomEntry instEntry = app.createTPInstance(nodeId, touchpointType, role, connectorParams, enabled);

		String instanceEntryUrl = instEntry.getLinksByRelation("self").get(0).getHref();

		switch (role) {
		case PROVIDER:
			return new ProviderTouchpoint(ctx, instanceEntryUrl);
		case INITIATOR:
			return new InitiatorTouchpoint(ctx, instanceEntryUrl);
		case INTERMEDIARY:
			return new IntermediaryTouchpoint(ctx, instanceEntryUrl);
		default:
			throw new UnsupportedOperationException();
		}
	}
}
