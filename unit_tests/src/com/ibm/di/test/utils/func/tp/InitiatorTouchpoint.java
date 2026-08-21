package com.ibm.di.test.utils.func.tp;

import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.test.http.HttpClientContext;
import com.ibm.di.test.utils.atom.AtomAppHelper;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.model.config.Destination;

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
public class InitiatorTouchpoint extends Touchpoint {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Map<DestinationService, String> servDests = new HashMap<DestinationService, String>();

	/**
	 * @param ctx
	 * @param instEntry
	 */
	public InitiatorTouchpoint(HttpClientContext ctx, String instanceEntryUrl) {
		super(ctx, instanceEntryUrl);
	}

	@Override
	public void deleteTouchpoint() throws Exception {
		for (DestinationService dest : servDests.keySet()) {
			dest.close();
		}

		super.deleteTouchpoint();
	}

	public void createExternalDestination(URL url) throws Exception {
		addDestination(getDestFeedUrl(), url);
	}

	public DestinationService replaceDestinationService(DestinationService oldService, int newPort) throws Exception {

		String editUrl = servDests.remove(oldService);

		DestinationService newService = new DestinationService(newPort);
		try {
			String newRequestOut = newService.getRequestOutUrl().toString();

			// update the destination entry
			AtomEntry destinationEntry = app.getAtomEntry(editUrl);
			assertNotNull(destinationEntry);
			Destination destination = app.getDestination(destinationEntry);
			destination.setRequestOut(newRequestOut);
			app.setDestination(destinationEntry, destination);
			app.putAtomEntry(destinationEntry, editUrl);

		} catch (Exception ex) {
			newService.close();
			throw ex;
		}

		servDests.put(newService, editUrl);

		return newService;
	}

	public DestinationService createDestinationService(int port) throws Exception {
		String destFeed = getDestFeedUrl();

		AtomEntry destEntry;
		DestinationService dest = new DestinationService(port);
		try {
			destEntry = addDestination(destFeed, dest.getRequestOutUrl());
		} catch (Exception ex) {
			dest.close();
			throw ex;
		}

		servDests.put(dest, app.getResourceUrlFromEntry(destEntry, Constants.REL_EDIT));

		return dest;
	}

	public void deleteDestinationService(DestinationService dest) throws Exception {
		String editFeed = servDests.remove(dest);

		if (editFeed != null) {
			AtomAppHelper.checkSuccess(ctx.invoke(ctx.constructMockRequest(HttpMethod.DELETE, editFeed, MediaType.WILDCARD)));
		}
	}

	private String getDestFeedUrl() throws Exception {
		List<AtomLink> destUrls = com.ibm.di.test.utils.atom.AtomUtils.findLinksByRel(getInstanceEntry().getLinks(),
				Constants.REL_DESTINATION_FEED);
		return destUrls.get(0).getHref();
	}

	private AtomEntry addDestination(String destFeed, URL reqOut) throws Exception {
		return app.addDestination(destFeed, reqOut);
	}

}
