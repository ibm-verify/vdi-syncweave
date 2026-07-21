package com.ibm.di.test.utils.func.tp;

public class ProviderTouchpointResponse {

	private final int responseCode;

	private final TouchpointData data;

	private final String errorMessage;

	public ProviderTouchpointResponse(int responseCode, byte[] responseBytes) throws Exception {
		this.responseCode = responseCode;

		if (responseBytes != null) {
			String responseString = new String(responseBytes, "UTF-8");
			if (isError(responseCode)) {
				errorMessage = responseString;
				data = null;
			} else {
				data = new TouchpointData(responseString);
				errorMessage = null;
			}
		} else {
			data = null;
			errorMessage = null;
		}
	}

	public boolean isError() {
		return isError(responseCode);
	}

	public int getResponseCode() {
		return responseCode;
	}

	public TouchpointData getData() {
		return data;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	private static boolean isError(int responseCode) {
		return responseCode < 200 || responseCode > 299;
	}
}
