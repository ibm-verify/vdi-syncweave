package com.ibm.di.test.utils.func.tp;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.ibm.di.entry.Entry;
import com.ibm.di.test.utils.func.HttpReceiver;

public class DestinationService {

	private HttpReceiver receiver;
	private URL tpUrl;

	public DestinationService(int port) throws Exception {
		this.receiver = new HttpReceiver(port);
		this.tpUrl = new URL("http://localhost:" + port);
	}

	public URL getRequestOutUrl() {
		return tpUrl;
	}

	
	public List<TouchpointData> consume(int sizeLimit) throws Exception {
		final long milliseconds = 60000;
		return consume(sizeLimit, milliseconds);
	}
	
	/**
	 * Consume requests until the size limit is reached or the timeout expires.
	 * 
	 * @param sizeLimit
	 *            Max number of requests to consume. Negative means no limit.
	 * @param timeoutMilliseconds
	 *            Timeout in milliseconds.
	 * @return Never null.
	 * @throws Exception
	 */
	public List<TouchpointData> consume(int sizeLimit, long timeoutMilliseconds) throws Exception {
		List<TouchpointData> result = new ArrayList<TouchpointData>();
		if (sizeLimit < 0) {
			sizeLimit = Integer.MAX_VALUE;
		}
		for (int i = 0; i < sizeLimit; ++i) {
			Entry request = receiver.receive(timeoutMilliseconds);
			if (request == null) {
				// timeout
				break;
			}
			String content = request.getString("http.body");
			TouchpointData data = new TouchpointData(content);
			result.add(data);
		}
		return result;
	}

	public void close() {
		receiver.close();
	}
}
