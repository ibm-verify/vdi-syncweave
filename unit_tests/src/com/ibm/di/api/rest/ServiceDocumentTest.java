package com.ibm.di.api.rest;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.ibm.di.test.http.WinkHttpClientContext;
import com.ibm.di.test.rest.RestAppHelper;
import com.ibm.di.web.common.internal.wink.AtomServiceDocEnabler;

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
public class ServiceDocumentTest extends WinkHttpClientContext {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private RestAppHelper app = new RestAppHelper(this);

	public ServiceDocumentTest() {
		super(com.ibm.di.api.rest.internal.RestApplication.class, AtomServiceDocEnabler.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_ServiceDocument_Contains_Valid_Collections() throws Exception {
		assertThat(app.getConfigurationFeedURL(), is(not(anyOf(is(""), nullValue()))));
		assertThat(app.getCIFeedURL(), is(not(anyOf(is(""), nullValue()))));
		assertThat(app.getListenerFeedURL(), is(not(anyOf(is(""), nullValue()))));
		assertThat(app.getServerFeedURL(), is(not(anyOf(is(""), nullValue()))));
	}
}
