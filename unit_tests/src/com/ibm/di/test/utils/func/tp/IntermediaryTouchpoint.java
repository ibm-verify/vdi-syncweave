package com.ibm.di.test.utils.func.tp;

import java.net.URL;

import com.ibm.di.test.http.HttpClientContext;

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
public class IntermediaryTouchpoint extends Touchpoint {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private ProviderTouchpoint providerTp;
	private InitiatorTouchpoint initiatorTp;

	/**
	 * @param ctx
	 * @param instEntry
	 * @throws Exception
	 */
	public IntermediaryTouchpoint(HttpClientContext ctx, String instanceEntryUrl) throws Exception {
		super(ctx, instanceEntryUrl);
		providerTp = new ProviderTouchpoint(ctx, instanceEntryUrl);
		initiatorTp = new InitiatorTouchpoint(ctx, instanceEntryUrl);
	}

	public URL getRequestInUrl() throws Exception {
		return providerTp.getRequestInUrl();
	}

	public ProviderTouchpointResponse get() throws Exception {
		return providerTp.get();
	}

	public ProviderTouchpointResponse get(int sizeLimit) throws Exception {
		return providerTp.get(sizeLimit);
	}

	public ProviderTouchpointResponse get(String query) throws Exception {
		return providerTp.get(query);
	}

	public ProviderTouchpointResponse get(String query, int sizeLimit) throws Exception {
		return providerTp.get(query, sizeLimit);
	}

	public ProviderTouchpointResponse post(TouchpointData data) throws Exception {
		return providerTp.post(data);
	}

	public ProviderTouchpointResponse put(String query, TouchpointData data) throws Exception {
		return providerTp.put(query, data);
	}

	public ProviderTouchpointResponse delete(String query) throws Exception {
		return providerTp.delete(query);
	}

	public void createExternalDestination(URL url) throws Exception {
		initiatorTp.createExternalDestination(url);
	}

	public DestinationService createDestinationService(int port) throws Exception {
		return initiatorTp.createDestinationService(port);
	}

	public void deleteDestinationService(DestinationService dest) throws Exception {
		initiatorTp.deleteDestinationService(dest);
	}

	public void deleteTouchpoint() throws Exception {
		initiatorTp.deleteTouchpoint();
	}
}
